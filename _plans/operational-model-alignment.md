# Plan: Operational Model Alignment

## Context
Scrap records currently reference `cutting_records` — a model that no longer reflects production reality (molding scrap is independent). Machines and profiles lack foundational fields needed for plant traceability (processesType, type, position, status, code). This spec restructures the scrap model around shift+profile+operator, adds those catalog fields end-to-end, fixes two broken native queries, and narrows POST security to ADMIN+SUPERVISOR only.

## Package base
`com.example.company.<module>` — modules: `profiles`, `machines`, `scrap`, `shifts`, `activity`, `security`

---

## Files that must not be modified or deleted

> **Reason:** A prior plan execution without this rule silently overwrote the reception, cutting, scrap, molding-output, and activity modules. Every file not explicitly named in this plan's change list is off limits.

Before starting, the executing agent must run the following commands and paste the output into this section:

```powershell
Get-ChildItem .\tesla-api\src\main\java\com\example\company -Recurse -Filter "*.java" | Select-Object Name
Get-ChildItem .\tesla-web-app\src\app -Recurse -Filter "*.ts" | Select-Object Name
```

Only **CREATE new files** or **MODIFY files explicitly named in this plan**. Do not touch any file whose name does not appear in one of this plan's phase headings.

---

## Phase 1 — Migration V4

### 1.1 Create V4 migration
**File:** `tesla-api/src/main/resources/db/migration/V4__operational_model_alignment.sql`

**Scrap table restructure** (in order — FK drop must precede column drop):

1. Drop FK from `scrap_records` to `cutting_records`: `ALTER TABLE scrap_records DROP CONSTRAINT scrap_records_cutting_record_id_fkey;`
2. Drop quantity check: `ALTER TABLE scrap_records DROP CONSTRAINT scrap_records_quantity_check;`
3. Drop old column: `ALTER TABLE scrap_records DROP COLUMN cutting_record_id;`
4. Add new columns:
   - `shift_id BIGINT NOT NULL REFERENCES shifts(id)`
   - `profile_id BIGINT NOT NULL REFERENCES profiles(id)`
   - `operator_id BIGINT NOT NULL REFERENCES users(id)` — V1 already declares this FK on `molding_outputs`
5. Re-add check: `ADD CONSTRAINT scrap_quantity_check CHECK (quantity > 0)` — spec requires `> 0`, not `>= 0`

**Profiles table — new columns:**

a. Add both columns NULLABLE with no DEFAULT:
   - `ADD COLUMN type VARCHAR(10)` — no DEFAULT; NULL means pre-spec legacy row
   - `ADD COLUMN profile_position VARCHAR(10)` — use `profile_position`, not `position` (reserved keyword in PostgreSQL/Hibernate)
b. Backfill `type` and `profile_position` for the four canonical profiles via the UPSERT seed statements in the Physical Statement Order section below.
c. Soft-delete ALL profiles whose code is not one of the canonical four, regardless of whether they are referenced by receptions (soft-delete does not break FKs; the activity feed joins profiles without filtering on `active`, so deactivated rows remain readable):
   ```sql
   UPDATE profiles SET active = false
   WHERE code NOT IN ('EXTE00036','EXTE00037','EXTE00038','EXTE00039');
   ```
d. Add three CHECK constraints (do NOT apply `ALTER COLUMN ... SET NOT NULL` — inactive legacy rows hold NULL and must remain valid):
   ```sql
   ALTER TABLE profiles ADD CONSTRAINT profiles_type_check
     CHECK (type IS NULL OR type IN ('HEADER','LOWER'));
   ALTER TABLE profiles ADD CONSTRAINT profiles_position_check
     CHECK (profile_position IS NULL OR profile_position IN ('FRONT','REAR'));
   ALTER TABLE profiles ADD CONSTRAINT profiles_active_requires_type_position
     CHECK (active = false OR (type IS NOT NULL AND profile_position IS NOT NULL));
   ```
   Required-ness for new active profiles is enforced at the DTO layer (`@NotBlank` on `ProfileCreateRequest`) and by `profiles_active_requires_type_position` at the database layer.

**Machines table — new columns** (status has a `DEFAULT` so existing rows are valid; others nullable):

- `ADD COLUMN code VARCHAR(20) UNIQUE` (nullable — optional field; spec Data and Validation table: max 20 chars)
- `ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'OPERATIONAL'` with `ADD CONSTRAINT machines_status_check CHECK (status IN ('OPERATIONAL', 'MAINTENANCE', 'OUT_OF_SERVICE'))`
- `ADD COLUMN processes_type VARCHAR(10)` (nullable — legacy machines stay NULL) with `ADD CONSTRAINT machines_processes_type_check CHECK (processes_type IN ('HEADER', 'LOWER'))`
- `ADD COLUMN cycle_time_seconds INTEGER`
- `ADD COLUMN last_maintenance_date DATE`
- `ADD COLUMN observations VARCHAR(500)`

**Seed — Tecma and Milan** using `INSERT … ON CONFLICT (name) DO UPDATE`:

- Tecma: `name='Tecma', code='TECMA004', processes_type='HEADER', status='OPERATIONAL', active=true`
- Milan: `name='Milan', code='MILAN', processes_type='LOWER', status='OPERATIONAL', active=true`

**Seed — soft-delete unreferenced legacy machines:**
```sql
UPDATE machines SET active = false
WHERE name NOT IN ('Tecma', 'Milan')
  AND id NOT IN (SELECT DISTINCT machine_id FROM cutting_records);
```
CUT-02 is referenced by `cutting_records` → stays active; `processes_type` remains NULL. Cut1231 is already inactive — no action needed.

**Seed — Profiles** (UPSERT on code — supplies the backfill for profiles table step b above; required for Acceptance Criterion 6):

```sql
INSERT INTO profiles (code, name, type, profile_position, active)
VALUES
  ('EXTE00036', 'Header-frontal', 'HEADER', 'FRONT', true),
  ('EXTE00037', 'Header-REAR',    'HEADER', 'REAR',  true),
  ('EXTE00038', 'LOWER-REAR',     'LOWER',  'REAR',  true),
  ('EXTE00039', 'LOWER-FRONT',    'LOWER',  'FRONT', true)
ON CONFLICT (code) DO UPDATE SET
  name             = EXCLUDED.name,
  type             = EXCLUDED.type,
  profile_position = EXCLUDED.profile_position,
  active           = true;
```

**Seed — Shifts** (UPSERT on name — required for Acceptance Criterion 6):

```sql
INSERT INTO shifts (name, start_time, end_time, active)
VALUES
  ('Matutino', '07:30', '17:00', true),
  ('Nocturno', '19:30', '07:30', true)
ON CONFLICT (name) DO UPDATE SET
  start_time = EXCLUDED.start_time,
  end_time   = EXCLUDED.end_time;
```

**Seed — soft-delete all non-canonical shifts:**

```sql
UPDATE shifts SET active = false
WHERE name NOT IN ('Matutino', 'Nocturno');
```

Soft-delete preserves the `cutting_records.shift_id` FK so historical cutting records keep resolving their shift. Reads that need a deactivated shift use the unfiltered `findById` added in Phase 2.17; only pickers and dropdowns filter on `active`. Verified: no class under `com.example.company.cutting` reads through `ShiftRepositoryPort` or `findActiveById` — deactivating a shift cannot break any cutting read path. This mirrors the profiles soft-delete in Physical Statement Order step 8.

**Indexes on `scrap_records`:**

```sql
CREATE INDEX IF NOT EXISTS idx_scrap_records_operator_shift
  ON scrap_records (operator_id, shift_id);
CREATE INDEX IF NOT EXISTS idx_scrap_records_profile_id
  ON scrap_records (profile_id);
```

**Physical statement order inside `V4__operational_model_alignment.sql`** (constraints must follow the data that satisfies them):

1. `scrap_records` restructure: drop FK, drop check, drop column, add `shift_id`/`profile_id`/`operator_id` columns, re-add `CHECK (quantity > 0)`
2. `profiles`: `ADD COLUMN type VARCHAR(10)`, `ADD COLUMN profile_position VARCHAR(10)` — both nullable, no DEFAULT
3. `machines`: add `code`, `status`, `processes_type`, `cycle_time_seconds`, `last_maintenance_date`, `observations`, plus `machines_status_check` and `machines_processes_type_check` constraints
4. Seed profiles UPSERT (`ON CONFLICT (code) DO UPDATE`)
5. Seed shifts UPSERT (`ON CONFLICT (name) DO UPDATE`)
6. Soft-delete non-canonical shifts: `UPDATE shifts SET active = false WHERE name NOT IN ('Matutino', 'Nocturno')`
7. Seed machines UPSERT (`ON CONFLICT (name) DO UPDATE`)
8. Soft-delete non-canonical profiles: `UPDATE profiles SET active = false WHERE code NOT IN (...)`
9. Soft-delete unreferenced legacy machines: `UPDATE machines SET active = false WHERE name NOT IN (...) AND id NOT IN (...)`
10. `profiles` constraints: `profiles_type_check`, `profiles_position_check`, `profiles_active_requires_type_position` — must come after steps 4 and 8 so that seeded rows and deactivated rows already satisfy the constraints
11. Indexes on `scrap_records`: `idx_scrap_records_operator_shift`, `idx_scrap_records_profile_id`

---

## Phase 2 — Domain + Ports

### 2.1 Profile domain model
**File:** `tesla-api/src/main/java/com/example/company/profiles/domain/model/Profile.java`

Add fields `String type` (nullable) and `String position` (logical name; maps to `profile_position` at DB level).

Update `create(String code, String name, String description)` → `create(String code, String name, String description, String type, String position)`.

Update `restore(Long id, String code, String name, String description, boolean active)` → add `String type, String position`.

Update `update(String name, String description)` → `update(String name, String description, String type, String position)`.

### 2.2 ProfileResult
**File:** `tesla-api/src/main/java/com/example/company/profiles/domain/port/in/ProfileResult.java`

Add `String type` and `String position` fields. New signature: `(Long id, String code, String name, String description, boolean active, String type, String position)`.

### 2.3 CreateProfileCommand
**File:** `tesla-api/src/main/java/com/example/company/profiles/domain/port/in/CreateProfileCommand.java`

Add `String type` (nullable) and `String position` (nullable).

### 2.4 UpdateProfileCommand
**File:** `tesla-api/src/main/java/com/example/company/profiles/domain/port/in/UpdateProfileCommand.java`

Add `String type` (nullable) and `String position` (nullable).

### 2.5 ProfileRepositoryPort — add findById
**File:** `tesla-api/src/main/java/com/example/company/profiles/domain/port/out/ProfileRepositoryPort.java`

Add method: `Optional<Profile> findById(Long id)` — no active filter; used by scrap READ path. Do not remove or alter `findActiveById`.

### 2.6 Machine domain model
**File:** `tesla-api/src/main/java/com/example/company/machines/domain/model/Machine.java`

Add fields: `String code` (nullable), `String status` (required), `String processesType` (nullable for legacy), `Integer cycleTimeSeconds`, `LocalDate lastMaintenanceDate`, `String observations`.

Update `create(String name)` → `create(String name, String code, String status, String processesType, Integer cycleTimeSeconds, LocalDate lastMaintenanceDate, String observations)`.

Update `restore(Long id, String name, boolean active)` → add all new fields.

Update `update(String name)` → `update(String name, String code, String status, String processesType, Integer cycleTimeSeconds, LocalDate lastMaintenanceDate, String observations)` — `processesType` may be `null` (legacy machines must be editable without supplying it).

### 2.7 MachineResult
**File:** `tesla-api/src/main/java/com/example/company/machines/domain/port/in/MachineResult.java`

New signature: `(Long id, String name, boolean active, String code, String status, String processesType, Integer cycleTimeSeconds, LocalDate lastMaintenanceDate, String observations)`.

### 2.8 CreateMachineCommand
**File:** `tesla-api/src/main/java/com/example/company/machines/domain/port/in/CreateMachineCommand.java`

New fields: `String name, String code, String status, String processesType, Integer cycleTimeSeconds, LocalDate lastMaintenanceDate, String observations`. `processesType` is required at create (validated in REST layer, not domain).

### 2.9 UpdateMachineCommand
**File:** `tesla-api/src/main/java/com/example/company/machines/domain/port/in/UpdateMachineCommand.java`

Same fields as CreateMachineCommand but `processesType` may be null (legacy machines).

### 2.10 MachineRepositoryPort — add code-uniqueness methods
**File:** `tesla-api/src/main/java/com/example/company/machines/domain/port/out/MachineRepositoryPort.java`

Add: `boolean existsByCode(String code)` and `boolean existsByCodeAndIdNot(String code, Long id)`.

### 2.11 Scrap domain model
**File:** `tesla-api/src/main/java/com/example/company/scrap/domain/model/ScrapRecord.java`

Replace `cuttingRecordId` with `shiftId`, `profileId`, `operatorId`.

New `create` factory: `create(Long shiftId, Long profileId, Long operatorId, int quantity, String reason)`.

New `restore` factory: add `shiftId`, `profileId`, `operatorId`; remove `cuttingRecordId`.

### 2.12 ScrapResult
**File:** `tesla-api/src/main/java/com/example/company/scrap/domain/port/in/ScrapResult.java`

New signature: `(Long id, Long shiftId, String shiftName, Long profileId, String profileCode, Long operatorId, int quantity, String reason, LocalDateTime createdAt)`. Remove `cuttingRecordId`.

### 2.13 RegisterScrapCommand
**File:** `tesla-api/src/main/java/com/example/company/scrap/domain/port/in/RegisterScrapCommand.java`

New signature: `(Long shiftId, Long profileId, Long operatorId, int quantity, String reason)`. `operatorId` comes from the JWT principal (extracted by controller, passed in command). Remove `cuttingRecordId`.

### 2.14 ScrapRepositoryPort — change findByOperatorAndShift return type
**File:** `tesla-api/src/main/java/com/example/company/scrap/domain/port/out/ScrapRepositoryPort.java`

Change `findByOperatorAndShift(Long operatorId, Long shiftId)` return type from `List<ScrapRecord>` to `List<ScrapResult>` — the persistence adapter uses a native join to return all display fields in one round trip. `findById(Long)` keeps returning `Optional<ScrapRecord>`.

> **Design note (MINOR 3):** Returning `ScrapResult` (declared in `domain.port.in`) from a `domain.port.out` port couples the output port to the input port contract. ArchUnit will NOT fail — the existing rules only forbid Spring/JPA/servlet/adapter imports in the domain, not `port.in ↔ port.out` cross-references. The cleaner alternative is a dedicated `domain.port.out.ScrapReadRow` record that the application layer maps to `ScrapResult`. This plan keeps the simpler approach (one record type, one round trip, no extra mapping); revisit if the read contract diverges from the write contract.

### 2.15 New exception — ScrapShiftInactiveException
**File:** `tesla-api/src/main/java/com/example/company/scrap/domain/exception/ScrapShiftInactiveException.java`

Extends `DomainException`. `DomainErrorType.BUSINESS_RULE`, error code `scrap.shift-inactive`. Constructor takes `Long shiftId`.

### 2.16 New exception — ScrapProfileInactiveException
**File:** `tesla-api/src/main/java/com/example/company/scrap/domain/exception/ScrapProfileInactiveException.java`

Extends `DomainException`. `DomainErrorType.BUSINESS_RULE`, error code `scrap.profile-inactive`. Constructor takes `Long profileId`.

### 2.17 ShiftRepositoryPort — add findById
**File:** `tesla-api/src/main/java/com/example/company/shifts/domain/port/out/ShiftRepositoryPort.java`

Add: `Optional<Shift> findById(Long id)` — no active filter; used by scrap READ path. Do not remove or alter `findActiveById`.

---

## Phase 3 — Persistence Layer

### 3.1 ProfileJpaEntity — new columns
**File:** `tesla-api/src/main/java/com/example/company/profiles/adapter/out/persistence/ProfileJpaEntity.java`

Add `@Column(name = "type", length = 10) private String type` and `@Column(name = "profile_position", length = 10) private String position`. Update package-private constructor and `updateFromDomain()` to accept both fields. Add package-private getters `getType()` and `getPosition()`.

### 3.2 ProfilePersistenceMapper — update mapping
**File:** `tesla-api/src/main/java/com/example/company/profiles/adapter/out/persistence/ProfilePersistenceMapper.java`

`toDomain()`: add `entity.getType()` and `entity.getPosition()` to `Profile.restore()` call.

`toNewEntity()`: add `profile.type()` and `profile.position()` to `ProfileJpaEntity` constructor call.

### 3.3 ProfilePersistenceAdapter — add findById + updateFromDomain signature
**File:** `tesla-api/src/main/java/com/example/company/profiles/adapter/out/persistence/ProfilePersistenceAdapter.java`

Add `findById(Long id)` implementation: `return profileRepository.findById(id).map(mapper::toDomain)`.

In `save()`, update `entity.updateFromDomain(...)` call to pass `profile.type()` and `profile.position()` alongside the existing fields.

### 3.4 MachineJpaEntity — new columns
**File:** `tesla-api/src/main/java/com/example/company/machines/adapter/out/persistence/MachineJpaEntity.java`

Add fields: `@Column(unique = true, length = 20) private String code`, `@Column(nullable = false, length = 20) private String status`, `@Column(name = "processes_type", length = 10) private String processesType`, `@Column(name = "cycle_time_seconds") private Integer cycleTimeSeconds`, `@Column(name = "last_maintenance_date") private LocalDate lastMaintenanceDate`, `@Column(length = 500) private String observations`.

Update package-private constructor to accept all new fields. Update `updateFromDomain(String name, boolean active)` → `updateFromDomain(String name, boolean active, String code, String status, String processesType, Integer cycleTimeSeconds, LocalDate lastMaintenanceDate, String observations)`. Add package-private getters for all new fields.

### 3.5 MachinePersistenceMapper — update mapping
**File:** `tesla-api/src/main/java/com/example/company/machines/adapter/out/persistence/MachinePersistenceMapper.java`

`toDomain()`: add all new fields from entity to `Machine.restore()`.
`toNewEntity()`: add all new fields from domain to `MachineJpaEntity` constructor.

### 3.6 MachinePersistenceAdapter — update save + add existsByCode
**File:** `tesla-api/src/main/java/com/example/company/machines/adapter/out/persistence/MachinePersistenceAdapter.java`

In `save()`: change `entity.updateFromDomain(machine.name(), machine.active())` to pass all new fields.

Add:
```
existsByCode(String code) → machineRepository.existsByCode(code)
existsByCodeAndIdNot(String code, Long id) → machineRepository.existsByCodeAndIdNot(code, id)
```

### 3.7 SpringDataMachineRepository — add code uniqueness queries
**File:** `tesla-api/src/main/java/com/example/company/machines/adapter/out/persistence/SpringDataMachineRepository.java`

Add: `boolean existsByCode(String code)` and `boolean existsByCodeAndIdNot(String code, Long id)` — Spring Data derives both from the column name; no `@Query` needed.

### 3.8 ShiftPersistenceAdapter — add findById
**File:** `tesla-api/src/main/java/com/example/company/shifts/adapter/out/persistence/ShiftPersistenceAdapter.java`

Add `findById(Long id)` implementing `ShiftRepositoryPort.findById`: `return shiftRepository.findById(id).map(mapper::toDomain)`. The inherited `JpaRepository.findById` on `SpringDataShiftRepository` is already available; no change to that interface needed.

### 3.9 ScrapRecordJpaEntity — replace cuttingRecordId
**File:** `tesla-api/src/main/java/com/example/company/scrap/adapter/out/persistence/ScrapRecordJpaEntity.java`

Remove `cuttingRecordId` field. Add:
- `@Column(name = "shift_id", nullable = false) private Long shiftId`
- `@Column(name = "profile_id", nullable = false) private Long profileId`
- `@Column(name = "operator_id", nullable = false) private Long operatorId`

Update constructor, getters, and `updateFromDomain` (or factory method) accordingly.

### 3.10 ScrapSpringRepository — replace broken native query
**File:** `tesla-api/src/main/java/com/example/company/scrap/adapter/out/persistence/ScrapSpringRepository.java`

> **Ordering constraint (MINOR 5):** The existing `findByOperatorAndShift` query joins through `scrap_records.cutting_record_id` → `cutting_records.id`. Once V4 drops that column the query fails at **startup**, not at call time. This repository change and the V4 migration **must land in the same commit**. Never commit the migration without the repository fix, or vice versa.

Remove the `findByOperatorAndShift` native query (it joins through `cutting_records` — broken after migration).

Add a new native query returning `List<Object[]>` (not `List<ScrapRecordJpaEntity>`) for the list read:

```sql
SELECT sr.id, s.name, sr.shift_id, p.code, sr.profile_id,
       sr.operator_id, sr.quantity, sr.reason, sr.created_at
FROM scrap_records sr
JOIN shifts s ON sr.shift_id = s.id
JOIN profiles p ON sr.profile_id = p.id
WHERE sr.operator_id = :operatorId AND sr.shift_id = :shiftId
```

Method name: `findWithDetailsByOperatorAndShift(@Param("operatorId") Long operatorId, @Param("shiftId") Long shiftId)` returning `List<Object[]>`, annotated `@Query(nativeQuery = true)`.

No active filter on the joins — deactivated shifts/profiles still appear in historical reads.

### 3.11 ScrapPersistenceAdapter — rewrite
**File:** `tesla-api/src/main/java/com/example/company/scrap/adapter/out/persistence/ScrapPersistenceAdapter.java`

**`save(ScrapRecord)`**: map `record.shiftId()`, `record.profileId()`, `record.operatorId()` to entity. Remove `cuttingRecordId` mapping.

**`findById(Long)`**: unchanged; returns `Optional<ScrapRecord>` mapped from entity (no join needed — service resolves names separately via port calls).

**`findByOperatorAndShift(Long, Long)`**: call `scrapRepository.findWithDetailsByOperatorAndShift(operatorId, shiftId)` returning `List<Object[]>`, then map each row to `ScrapResult`:

Apply Object[] defensive unwrap before indexing:
```
Object[] cols = (row[0] instanceof Object[]) ? (Object[]) row[0] : row;
```
Then: `id=cols[0]`, `shiftName=cols[1]`, `shiftId=cols[2]`, `profileCode=cols[3]`, `profileId=cols[4]`, `operatorId=cols[5]`, `quantity=cols[6]`, `reason=cols[7]`, `createdAt=cols[8]`.

Cast `id`/`shiftId`/`profileId`/`operatorId` via `((Number) cols[N]).longValue()`, `quantity` via `((Number) cols[6]).intValue()`, `createdAt` via `(LocalDateTime) cols[8]` or `((Timestamp) cols[8]).toLocalDateTime()`.

### 3.12 ActivityPersistenceAdapter — fix scrap query
**File:** `tesla-api/src/main/java/com/example/company/activity/adapter/out/persistence/ActivityPersistenceAdapter.java`

In `findScrapByOperatorAndShift()` (currently around line 59): replace the broken join-through-cutting_records native query with:

```sql
SELECT sr.id, p.code, sr.quantity, sr.created_at
FROM scrap_records sr
JOIN profiles p ON sr.profile_id = p.id
WHERE sr.operator_id = :operatorId AND sr.shift_id = :shiftId
```

In `mapScrapRow()` (currently around line 127): apply Object[] defensive unwrap, then:
- `containerCode` → `null` (no container for scrap rows)
- `profileCode` → `(String) cols[1]`
- `quantity` → `((Number) cols[2]).intValue()`
- `recordedAt` → cast `cols[3]` as `LocalDateTime`

Remove old column references to `cutting_records`/`inventory_items`/`containers`.

---

## Phase 4 — Application Layer

### 4.1 ProfileResultMapper — add new fields
**File:** `tesla-api/src/main/java/com/example/company/profiles/application/mapper/ProfileResultMapper.java`

Add `profile.type()` and `profile.position()` to the `ProfileResult` constructor call.

### 4.2 CreateProfileService — pass type and position
**File:** `tesla-api/src/main/java/com/example/company/profiles/application/usecase/CreateProfileService.java`

Pass `command.type()` and `command.position()` to `Profile.create(...)`. No other logic change.

### 4.3 UpdateProfileService — pass type and position
**File:** `tesla-api/src/main/java/com/example/company/profiles/application/usecase/UpdateProfileService.java`

Pass `command.type()` and `command.position()` to `profile.update(...)`. No other logic change.

### 4.4 MachineResultMapper — add new fields
**File:** `tesla-api/src/main/java/com/example/company/machines/application/mapper/MachineResultMapper.java`

Add all new fields to `MachineResult` constructor: `machine.code()`, `machine.status()`, `machine.processesType()`, `machine.cycleTimeSeconds()`, `machine.lastMaintenanceDate()`, `machine.observations()`.

### 4.5 CreateMachineService — add code uniqueness check
**File:** `tesla-api/src/main/java/com/example/company/machines/application/usecase/CreateMachineService.java`

After the existing name-duplicate check, add: if `command.code() != null && machineRepository.existsByCode(command.code())` → throw `DuplicateMachineCodeException` (new exception, see below). Then pass all fields to `Machine.create(...)`.

Create new exception `DuplicateMachineCodeException` at `tesla-api/src/main/java/com/example/company/machines/domain/exception/DuplicateMachineCodeException.java` — `DomainErrorType.CONFLICT`, code `machine.duplicate-code`.

### 4.6 UpdateMachineService — add code uniqueness check
**File:** `tesla-api/src/main/java/com/example/company/machines/application/usecase/UpdateMachineService.java`

After existing name-duplicate check, add: if `command.code() != null && machineRepository.existsByCodeAndIdNot(command.code(), id)` → throw `DuplicateMachineCodeException`. Then pass all fields (including nullable `processesType`) to `machine.update(...)`.

### 4.7 RegisterScrapService — full rewrite
**File:** `tesla-api/src/main/java/com/example/company/scrap/application/usecase/RegisterScrapService.java`

Inject `ShiftRepositoryPort shiftRepository` and `ProfileRepositoryPort profileRepository` alongside existing `ScrapRepositoryPort`.

In `register(RegisterScrapCommand command)`:
1. Lookup shift: `shiftRepository.findActiveById(command.shiftId())` → throw `ScrapShiftInactiveException(command.shiftId())` if empty.
2. Lookup profile: `profileRepository.findActiveById(command.profileId())` → throw `ScrapProfileInactiveException(command.profileId())` if empty (422 `scrap.profile-inactive`, not 404).
3. Create record: `ScrapRecord.create(command.shiftId(), command.profileId(), command.operatorId(), command.quantity(), command.reason())`.
4. Save: `ScrapRecord saved = scrapRepository.save(record)`.
5. Build result using the already-fetched `Shift` and `Profile` domain objects (no extra queries): `new ScrapResult(saved.id(), saved.shiftId(), shift.name(), saved.profileId(), profile.code(), saved.operatorId(), saved.quantity(), saved.reason(), saved.createdAt())`.

### 4.8 GetScrapService — full rewrite
**File:** `tesla-api/src/main/java/com/example/company/scrap/application/usecase/GetScrapService.java`

Inject `ShiftRepositoryPort shiftRepository` and `ProfileRepositoryPort profileRepository` (READ path — both use `findById`, not `findActiveById`).

**`findById(Long id)`** path:
1. `scrapRepository.findById(id)` → throw `ScrapNotFoundException(id)` if empty.
2. `shiftRepository.findById(record.shiftId())` — no active filter.
3. `profileRepository.findById(record.profileId())` — no active filter.
4. Build `ScrapResult` from record + shift.name() + profile.code().

**`findByOperatorAndShift(Long, Long)`** path:
- Return `scrapRepository.findByOperatorAndShift(operatorId, shiftId)` directly — already returns `List<ScrapResult>` from the native join in the adapter. Remove the old `toResult()` stream mapping.

---

## Phase 5 — REST Adapters

### 5.1 ProfileCreateRequest — add type and position
**File:** `tesla-api/src/main/java/com/example/company/profiles/adapter/in/web/dto/ProfileCreateRequest.java`

Add `@NotBlank @Size(max = 10) String type` with a pattern check limiting values to `'HEADER'` or `'LOWER'`, and `@NotBlank @Size(max = 10) String position` with a pattern check limiting values to `'FRONT'` or `'REAR'`. Both are required — after migration the columns are NOT NULL.

### 5.2 ProfileUpdateRequest — add type and position
**File:** `tesla-api/src/main/java/com/example/company/profiles/adapter/in/web/dto/ProfileUpdateRequest.java`

Add `@NotBlank @Size(max = 10) String type` with a pattern check limiting values to `'HEADER'` or `'LOWER'`, and `@NotBlank @Size(max = 10) String position` with a pattern check limiting values to `'FRONT'` or `'REAR'`. Both required on update — columns are NOT NULL after migration.

### 5.3 ProfileResponse — add type and position
**File:** `tesla-api/src/main/java/com/example/company/profiles/adapter/in/web/dto/ProfileResponse.java`

Add `String type` and `String position` fields.

### 5.4 ProfileWebMapper — update toCommand and toResponse
**File:** `tesla-api/src/main/java/com/example/company/profiles/adapter/in/web/ProfileWebMapper.java`

`toCommand(ProfileCreateRequest)`: add `request.type()`, `request.position()` to `CreateProfileCommand`.
`toCommand(ProfileUpdateRequest)`: add `request.type()`, `request.position()` to `UpdateProfileCommand`.
`toResponse(ProfileResult)`: add `result.type()`, `result.position()` to `ProfileResponse`.

### 5.5 MachineCreateRequest — add new fields
**File:** `tesla-api/src/main/java/com/example/company/machines/adapter/in/web/dto/MachineCreateRequest.java`

Add:
- `@Size(max = 20) String code` — nullable (optional)
- `@NotBlank @Size(max = 20) String status` — required
- `@NotBlank @Size(max = 10) String processesType` — required on create
- `Integer cycleTimeSeconds` — nullable
- `LocalDate lastMaintenanceDate` — nullable
- `@Size(max = 500) String observations` — nullable

### 5.6 MachineUpdateRequest — add new fields
**File:** `tesla-api/src/main/java/com/example/company/machines/adapter/in/web/dto/MachineUpdateRequest.java`

Same fields as CreateRequest, except `processesType` is `String` without `@NotBlank` (nullable — legacy machines with NULL must be editable without supplying a value).

### 5.7 MachineResponse — add new fields
**File:** `tesla-api/src/main/java/com/example/company/machines/adapter/in/web/dto/MachineResponse.java`

Add: `String code`, `String status`, `String processesType`, `Integer cycleTimeSeconds`, `LocalDate lastMaintenanceDate`, `String observations`.

### 5.8 MachineWebMapper — update all mappings
**File:** `tesla-api/src/main/java/com/example/company/machines/adapter/in/web/MachineWebMapper.java`

`toCommand(MachineCreateRequest)`: pass all new fields to `CreateMachineCommand`.
`toCommand(MachineUpdateRequest)`: pass all new fields to `UpdateMachineCommand`.
`toResponse(MachineResult)`: pass all new fields to `MachineResponse`.

### 5.9 ScrapRequest — replace cuttingRecordId
**File:** `tesla-api/src/main/java/com/example/company/scrap/adapter/in/web/dto/ScrapRequest.java`

Remove `@NotNull Long cuttingRecordId`. Add `@NotNull Long shiftId` and `@NotNull Long profileId`. Verify `@Size(max = 255)` is present on `reason`; add if missing.

### 5.10 ScrapResponse — replace cuttingRecordId
**File:** `tesla-api/src/main/java/com/example/company/scrap/adapter/in/web/dto/ScrapResponse.java`

Remove `cuttingRecordId`. Add: `Long shiftId`, `String shiftName`, `Long profileId`, `String profileCode`, `Long operatorId`.

### 5.11 ScrapRestController — inject principal for POST + update toResponse
**File:** `tesla-api/src/main/java/com/example/company/scrap/adapter/in/web/ScrapRestController.java`

In `register()`: add `@AuthenticationPrincipal AuthenticatedUserPrincipal principal` parameter. Build command: `new RegisterScrapCommand(request.shiftId(), request.profileId(), principal.userId(), request.quantity(), request.reason())`.

In `toResponse(ScrapResult)`: update to map new fields — `result.shiftId()`, `result.shiftName()`, `result.profileId()`, `result.profileCode()`, `result.operatorId()`; remove `result.cuttingRecordId()`.

---

## Phase 6 — Security

### 6.1 Narrow POST rules to ADMIN + SUPERVISOR
**File:** `tesla-api/src/main/java/com/example/company/security/config/SecurityConfiguration.java`

Read `SecurityConfiguration.java` and locate the four POST rules by matching on the `requestMatchers` path string — `"/api/receptions"`, `"/api/cutting"`, `"/api/scrap"`, `"/api/molding-outputs"` — not by line number (line numbers drift across edits). Confirm all four POST rules exist before editing. Each rule changes from `hasAnyRole("ADMIN", "SUPERVISOR", "OPERADOR")` to `hasAnyRole("ADMIN", "SUPERVISOR")`. All four are already declared per the spec; do not add new entries.

---

## Phase 7 — Tests

### 7.1 Profile domain + service tests
**Files (CREATE — none of these exist; no profile tests are present in the test tree):**
- `tesla-api/src/test/java/com/example/company/profiles/domain/model/ProfileTest.java`
- `tesla-api/src/test/java/com/example/company/profiles/application/usecase/CreateProfileServiceTest.java`
- `tesla-api/src/test/java/com/example/company/profiles/application/usecase/UpdateProfileServiceTest.java`

`ProfileTest`: `create()` sets type and position; `update()` replaces type and position; `restore()` preserves all fields.
`CreateProfileServiceTest`: happy path → profile saved with type and position; duplicate code → exception thrown.
`UpdateProfileServiceTest`: update replaces type and position; not-found → exception thrown.

### 7.2 Machine tests — update five existing test files
**Files (UPDATE — all five exist; expanding `Machine.create()` to a 7-arg factory and `Machine.restore()` to a 9-arg factory breaks every one of them at compile time):**
- `tesla-api/src/test/java/com/example/company/machines/domain/model/MachineTest.java`
- `tesla-api/src/test/java/com/example/company/machines/application/usecase/CreateMachineServiceTest.java`
- `tesla-api/src/test/java/com/example/company/machines/application/usecase/UpdateMachineServiceTest.java`
- `tesla-api/src/test/java/com/example/company/machines/application/usecase/GetMachineServiceTest.java`
- `tesla-api/src/test/java/com/example/company/machines/application/usecase/DeleteMachineServiceTest.java`

In each test, update all calls to `Machine.create(String name)` → `Machine.create(String name, String code, String status, String processesType, Integer cycleTimeSeconds, LocalDate lastMaintenanceDate, String observations)` and all calls to `Machine.restore(Long id, String name, boolean active)` → `Machine.restore(Long id, String name, boolean active, String code, String status, String processesType, Integer cycleTimeSeconds, LocalDate lastMaintenanceDate, String observations)`.

Also add to `MachineTest`: `create()` sets status, processesType, code; `update()` allows null processesType (legacy machine).
Also add to `CreateMachineServiceTest`: duplicate-code exception thrown when code uniqueness violated.

### 7.3 Scrap domain tests
**File (CREATE — does not exist; no scrap tests are present in the test tree):** `tesla-api/src/test/java/com/example/company/scrap/domain/model/ScrapRecordTest.java`

Test that `create()` sets shiftId, profileId, operatorId; does not include cuttingRecordId; quantity > 0 enforced.

### 7.4 RegisterScrapService tests
**File (CREATE — does not exist):** `tesla-api/src/test/java/com/example/company/scrap/application/usecase/RegisterScrapServiceTest.java`

Cases:
- Inactive shift → throws `ScrapShiftInactiveException`
- Inactive profile → throws `ScrapProfileInactiveException` (not `ProfileNotFoundException` — see BLOCKER 6; the correct code is `scrap.profile-inactive`, status 422)
- Happy path → result carries shiftName and profileCode from the fetched domain objects

### 7.5 GetScrapService tests
**File (CREATE — does not exist):** `tesla-api/src/test/java/com/example/company/scrap/application/usecase/GetScrapServiceTest.java`

`findById` case: shift was deactivated after creation → still resolves shiftName via `findById` (not `findActiveById`); result returned with populated shiftName.

### 7.6 ArchUnit — verify no direct repo imports in services
**File:** `tesla-api/src/test/java/com/example/company/architecture/HexagonalArchitectureTest.java` (existing)

No new ArchUnit rules needed if existing boundary tests cover `application` → `domain.port.out` only. Verify the new `RegisterScrapService` injecting two new ports still passes.

> **Warning — SecurityAuthorizationPolicyTest is self-referential and does NOT verify Acceptance Criteria 11–12:**
> `tesla-api/src/test/java/com/example/company/security/config/SecurityAuthorizationPolicyTest.java` builds a hardcoded `Map` inside the test and asserts against it — it never loads `SecurityConfiguration`. The test would pass even if `SecurityConfiguration.java` were deleted. It covers only the six catalog endpoints and never touches `/api/receptions`, `/api/cutting`, `/api/scrap`, or `/api/molding-outputs`. Removing `OPERADOR` from the four POST rules will NOT break this test.
> **Follow-up (outside this plan's scope):** `SecurityAuthorizationPolicyTest` asserts that `OPERADOR` cannot `GET /api/containers` while `SecurityConfiguration` actually permits it — the test contradicts live code and passes anyway. It should be rewritten or deleted.

### 7.7 Security integration test — operational POST rules
**File (CREATE):** `tesla-api/src/test/java/com/example/company/security/OperationalEndpointAccessTest.java`

Use `@SpringBootTest` + `MockMvc` (or `@WebMvcTest` with `SecurityConfiguration` loaded and a JWT test fixture). Assert:
- `POST /api/receptions` returns 403 for a JWT with role `OPERADOR`
- `POST /api/cutting` returns 403 for a JWT with role `OPERADOR`
- `POST /api/scrap` returns 403 for a JWT with role `OPERADOR`
- `POST /api/molding-outputs` returns 403 for a JWT with role `OPERADOR`
- `GET /api/receptions/my` (or the applicable read endpoint) returns 200 for the same `OPERADOR` JWT

Without this test, Acceptance Criteria 11 and 12 cannot be verified mechanically.

---

## Phase 8 — Frontend

### 8.1 Catalog models — profile type/position, machine new fields
**File:** `tesla-web-app/src/app/features/catalogs/models/catalogs.models.ts`

`Profile` interface: add `type: string | null` and `position: 'FRONT' | 'REAR' | null`.
`Machine` interface: add `code: string | null`, `status: string`, `processesType: string | null`, `cycleTimeSeconds: number | null`, `lastMaintenanceDate: string | null`, `observations: string | null`.

Also add `MachineStatus = 'OPERATIONAL' | 'MAINTENANCE' | 'OUT_OF_SERVICE'` and `ProcessesType = 'HEADER' | 'LOWER'` type aliases.

### 8.2 Catalogs page — machine form update
**File:** `tesla-web-app/src/app/features/catalogs/pages/catalogs.page.ts`

Machine create form (`machineForm`): add controls `code` (optional), `status` (required), `processesType` (required on create — add `Validators.required`), `cycleTimeSeconds` (optional), `lastMaintenanceDate` (optional), `observations` (optional).

Machine edit form: add same controls but `processesType` has no `Validators.required` — a null value is acceptable for legacy machines.

Template: add corresponding form fields for both create and edit modals. Status dropdown: OPERATIONAL / MAINTENANCE / OUT_OF_SERVICE. Processes Type dropdown: HEADER / LOWER (mark as required only in create form).

### 8.3 Catalogs page — profile form update
**File:** `tesla-web-app/src/app/features/catalogs/pages/catalogs.page.ts`

Profile create form (`profileCreateForm`): add `type` (required dropdown: HEADER / LOWER, `Validators.required`) and `position` (required dropdown: FRONT / REAR, `Validators.required`).

Profile edit form (`profileEditForm`): same additions — both required; columns are NOT NULL after migration.

Template: add corresponding form fields for both create and edit modals.

### 8.4 Activity record model — containerCode nullable
**File:** `tesla-web-app/src/app/features/my-activity/models/activity-record.model.ts`

Change `containerCode: string` → `containerCode: string | null`.

### 8.5 Activity filter service — null guards
**File:** `tesla-web-app/src/app/features/my-activity/services/activity-filter.service.ts`

Line 18: replace `r.containerCode.toLowerCase()` with `(r.containerCode ?? '').toLowerCase()` and `r.profileCode.toLowerCase()` with `(r.profileCode ?? '').toLowerCase()`. Prevents `TypeError` when scrap rows have null containerCode.

### 8.6 Activity detail modal — fix SCRAP case
**File:** `tesla-web-app/src/app/features/my-activity/components/activity-detail-modal.component.ts`

SCRAP case in `detailRows()` (lines 129–134): replace the current `{ label: 'Cutting Record ID', value: d.cuttingRecordId ?? '—' }` row with:

```
{ label: 'Shift', value: d.shiftName ?? '—' }
{ label: 'Profile', value: d.profileCode ?? '—' }
{ label: 'Quantity', value: d.quantity ?? '—' }
{ label: 'Reason', value: d.reason ?? '—' }
{ label: 'Created At', value: d.createdAt ?? '—' }
```

No container row. No cutting-record row.

Also update the `ActivityDetail` type/interface (if local to the component) to replace `cuttingRecordId` with `shiftId`, `shiftName`, `profileId`, `profileCode`.

### 8.7 Register Scrap page — new feature
**File (new):** `tesla-web-app/src/app/features/register-scrap/` (full feature dir)

Create using `$create-angular-feature` skill. The feature includes:
- API client: `POST /api/scrap` (body: shiftId, profileId, quantity, reason); `GET /api/scrap/my?shiftId=`
- Service: wraps API client, exposes observable for form submission
- Page component: form with shift selector (dropdown from `GET /api/shifts`), profile selector (dropdown from `GET /api/profiles`), quantity (number ≥ 1), reason (textarea, max 255)
- On submit: POST; on success reset form (if current shift still active, pre-select it; otherwise leave shift blank)
- Route: lazy-loaded at `/register-scrap`, guarded by `authGuard` + `roleGuard` (with `data: { roles: ['ADMIN', 'SUPERVISOR'] }`)

### 8.8 Nav item — add Register Scrap
**File:** `tesla-web-app/src/app/core/layout/nav-item.model.ts`

Add `{ label: 'Register Scrap', route: '/register-scrap', roles: ['ADMIN', 'SUPERVISOR'] }` between Register Molding Output and Reports.

### 8.9 App routes — add register-scrap route
**File:** `tesla-web-app/src/app/app.routes.ts`

Add the route under the authGuard-protected shell route, following the same pattern as `register-cut` and `register-molding-output`. `roleGuard` in this codebase is a `CanActivateFn` that reads allowed roles from `route.data` — it is **not** a factory function:

```typescript
{
  path: 'register-scrap',
  canActivate: [roleGuard],
  data: { roles: ['ADMIN', 'SUPERVISOR'] },
  loadComponent: () => import('./features/register-scrap/pages/register-scrap.page').then(m => m.RegisterScrapPage)
}
```

Match the placement under the authGuard-protected shell route exactly as the existing register-cut and register-molding-output routes.

---

## Agent routing

| Phase | Agent |
|-------|-------|
| 1 — Migration V4 | `hexagonal-persistence-adapter` |
| 2 — Domain + Ports | `hexagonal-domain-developer` |
| 3 — Persistence | `hexagonal-persistence-adapter` |
| 4 — Application | `hexagonal-application-developer` |
| 5 — REST Adapters | `hexagonal-web-adapter` |
| 6 — Security | `hexagonal-security-reviewer` |
| 7 — Tests | `hexagonal-test-engineer` |
| 8 — Frontend | `frontend-developer` |

---

## Implementation order

1. Write V4 migration (Phase 1.1) — schema must exist before any JPA entity change compiles
2. Add `type`/`position` to `Profile` domain model (2.1)
3. Update `ProfileResult` (2.2), `CreateProfileCommand` (2.3), `UpdateProfileCommand` (2.4)
4. Add `ProfileRepositoryPort.findById` (2.5)
5. Add new fields to `Machine` domain model (2.6)
6. Update `MachineResult` (2.7), `CreateMachineCommand` (2.8), `UpdateMachineCommand` (2.9)
7. Add `MachineRepositoryPort` code-uniqueness methods (2.10)
8. Rewrite `ScrapRecord` domain model (2.11)
9. Rewrite `ScrapResult` (2.12), `RegisterScrapCommand` (2.13)
10. Change `ScrapRepositoryPort.findByOperatorAndShift` return type (2.14)
11. Create `ScrapShiftInactiveException` (2.15)
12. Create `ScrapProfileInactiveException` (2.16)
13. Add `ShiftRepositoryPort.findById` (2.17)
14. Update `ProfileJpaEntity` (3.1), `ProfilePersistenceMapper` (3.2), `ProfilePersistenceAdapter` (3.3)
15. Update `MachineJpaEntity` (3.4), `MachinePersistenceMapper` (3.5), `MachinePersistenceAdapter` (3.6), `SpringDataMachineRepository` (3.7)
16. Add `ShiftPersistenceAdapter.findById` (3.8)
17. Rewrite `ScrapRecordJpaEntity` (3.9)
18. Replace `ScrapSpringRepository` query (3.10) — must be in the same commit as the V4 migration
19. Rewrite `ScrapPersistenceAdapter` (3.11)
20. Fix `ActivityPersistenceAdapter` scrap query (3.12)
21. Update `ProfileResultMapper` (4.1), `CreateProfileService` (4.2), `UpdateProfileService` (4.3)
22. Update `MachineResultMapper` (4.4), `CreateMachineService` (4.5), `UpdateMachineService` (4.6)
23. Create `DuplicateMachineCodeException` (4.5 prerequisite)
24. Rewrite `RegisterScrapService` (4.7)
25. Rewrite `GetScrapService` (4.8)
26. Update profile DTOs: `ProfileCreateRequest` (5.1), `ProfileUpdateRequest` (5.2), `ProfileResponse` (5.3), `ProfileWebMapper` (5.4)
27. Update machine DTOs: `MachineCreateRequest` (5.5), `MachineUpdateRequest` (5.6), `MachineResponse` (5.7), `MachineWebMapper` (5.8)
28. Update scrap DTOs: `ScrapRequest` (5.9), `ScrapResponse` (5.10), `ScrapRestController` (5.11)
29. Narrow POST security rules (6.1)
30. Write/update backend tests (7.1–7.7)
31. Update catalog models (8.1), catalogs page machine form (8.2), catalogs page profile form (8.3)
32. Fix activity record model (8.4), filter null guards (8.5), detail modal SCRAP case (8.6)
33. Scaffold Register Scrap feature (8.7), nav item (8.8), app route (8.9)

---

## Critical files

| File | Action |
|------|--------|
| `tesla-api/src/main/resources/db/migration/V4__operational_model_alignment.sql` | Create (new) |
| `com/example/company/scrap/domain/model/ScrapRecord.java` | Full rewrite |
| `com/example/company/scrap/domain/port/in/ScrapResult.java` | Full rewrite |
| `com/example/company/scrap/domain/port/in/RegisterScrapCommand.java` | Full rewrite |
| `com/example/company/scrap/domain/port/out/ScrapRepositoryPort.java` | Change return type |
| `com/example/company/scrap/domain/exception/ScrapShiftInactiveException.java` | Create (new) |
| `com/example/company/scrap/domain/exception/ScrapProfileInactiveException.java` | Create (new) |
| `com/example/company/machines/domain/exception/DuplicateMachineCodeException.java` | Create (new) |
| `com/example/company/machines/domain/model/Machine.java` | Add 6 fields |
| `com/example/company/machines/domain/port/in/MachineResult.java` | Add 6 fields |
| `com/example/company/machines/domain/port/in/CreateMachineCommand.java` | Add 6 fields |
| `com/example/company/machines/domain/port/in/UpdateMachineCommand.java` | Add 6 fields |
| `com/example/company/machines/domain/port/out/MachineRepositoryPort.java` | Add 2 methods |
| `com/example/company/profiles/domain/model/Profile.java` | Add 2 fields |
| `com/example/company/profiles/domain/port/out/ProfileRepositoryPort.java` | Add findById |
| `com/example/company/shifts/domain/port/out/ShiftRepositoryPort.java` | Add findById |
| `com/example/company/shifts/adapter/out/persistence/ShiftPersistenceAdapter.java` | Add findById impl |
| `com/example/company/scrap/adapter/out/persistence/ScrapRecordJpaEntity.java` | Full rewrite |
| `com/example/company/scrap/adapter/out/persistence/ScrapSpringRepository.java` | Replace query |
| `com/example/company/scrap/adapter/out/persistence/ScrapPersistenceAdapter.java` | Full rewrite |
| `com/example/company/activity/adapter/out/persistence/ActivityPersistenceAdapter.java` | Fix scrap query |
| `com/example/company/scrap/adapter/in/web/ScrapRestController.java` | Add principal injection |
| `com/example/company/security/config/SecurityConfiguration.java` | Remove OPERADOR from 4 rules |
| `tesla-web-app/.../my-activity/models/activity-record.model.ts` | containerCode nullable |
| `tesla-web-app/.../my-activity/services/activity-filter.service.ts` | Add null guards |
| `tesla-web-app/.../my-activity/components/activity-detail-modal.component.ts` | Fix SCRAP case |
| `tesla-web-app/.../catalogs/models/catalogs.models.ts` | Add new fields |
| `tesla-web-app/.../catalogs/pages/catalogs.page.ts` | Expand machine/profile forms |

---

## Verification

1. `cd tesla-api && .\gradlew.bat compileJava` — must compile clean; no references to removed `cuttingRecordId`
2. `.\gradlew.bat test` — all tests green; no `ClassCastException` in native query tests
3. Start backend: `POST /api/scrap` with `{ "shiftId": X, "profileId": Y, "quantity": 1, "reason": "test" }` as SUPERVISOR → 201 with `shiftName` and `profileCode` populated
4. `GET /api/scrap/my?shiftId=X` → list with `shiftName`, `profileCode`; no `cuttingRecordId`
5. `GET /api/scrap/{id}` where shift was deactivated after creation → 200 with populated `shiftName` (uses `findById`, not `findActiveById`)
6. `PUT /api/machines/{id}` for CUT-02 (legacy, NULL processesType) with body omitting `processesType` → 200 (no forced value)
7. `POST /api/machines` without `processesType` → 400 validation error
8. `POST /api/machines` with duplicate `code` → 409 `machine.duplicate-code`
9. `GET /api/profiles` → returns only the four canonical profiles (EXTE00036–39), each with `type` and `position` populated. Legacy profiles are deactivated (`active = false`) and absent from picker responses. `profiles` table has `profiles_type_check`, `profiles_position_check`, and `profiles_active_requires_type_position` constraints — NOT `NOT NULL` on `type` or `profile_position`.
10. Flyway `validate` mode on startup — no `SchemaValidationException` for profiles/machines/scrap_records
11. Frontend: open My Activity with a SCRAP row → detail modal shows Shift / Profile / Quantity / Reason / Created At; no container row, no Cutting Record ID row
12. Frontend: type "xx" in My Activity filter → no `TypeError: Cannot read properties of null (reading 'toLowerCase')`
13. Frontend: Register Scrap page visible to ADMIN/SUPERVISOR, absent from OPERADOR nav
14. `.\gradlew.bat test --tests "*HexagonalArchitectureTest"` — boundary rules still pass
