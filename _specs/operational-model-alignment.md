# Spec: Operational Model Alignment

## Summary

Align the profiles, machines, shifts, scrap, and access-role configuration with how the plant actually operates: add the missing catalog fields (type, position, code, status, processes type), rebuild the scrap module around molding scrap (shift + profile + quantity + reason, no cutting-record dependency), restrict operational write endpoints to ADMIN and SUPERVISOR, and expose the new fields in the Angular catalogs and a new Register Scrap page.

## Problem

The current data model does not reflect plant reality in four ways:

1. **Profiles** have no type (HEADER/LOWER) or position (FRONT/REAR), so the four real profiles cannot be distinguished in the UI or by any future business rule.
2. **Machines** have only a name. The plant-floor attributes (status, profile type processed, cycle time, last maintenance, observations, short code) are absent.
3. **Scrap** is wired to `cutting_records` and tracks cutting scrap. The real need is to record molding scrap — pieces rejected during molding, tallied per shift by a lead or supervisor. No system support exists today.
4. **Access roles** allow `OPERADOR` to write operational records. Only leads, supervisors, and area engineers actually use the app; operators fill paper forms.

## Goals

- Add `type` and `position` to profiles with CHECK constraints and seed the 4 real profiles.
- Add `code`, `status`, `processes_type`, `cycle_time_seconds`, `last_maintenance_date`, `observations` to machines and seed Tecma and Milan.
- Seed the 2 real shifts (Matutino and Nocturno). Do not deactivate existing shift rows — historical cutting records reference them via FK and deactivating breaks historical reads.
- Rebuild `scrap_records` to record molding scrap: `shift_id`, `profile_id`, `operator_id`, `quantity`, `reason`, `created_at`.
- Restrict `POST /api/receptions`, `/api/cutting`, `/api/scrap`, `/api/molding-outputs` to ADMIN and SUPERVISOR.
- Update the activity module to query scrap directly from the new `scrap_records` structure.
- Expose all new catalog fields in the Angular catalog screens.
- Add a Register Scrap page for ADMIN and SUPERVISOR.
- Handle scrap rows with no container in the My Activity table and detail modal.

## Non-Goals

- Molding press catalog (front-left, front-right, rear-left, rear-right presses are deferred).
- Any change to Register Molding Output — leave that feature exactly as it is.
- Barcode scanning of EXTE codes (they are warehouse identifiers, not barcodes).
- Digitising the paper parameter and quality sheets filled by molding operators.
- Enforcing that a profile's type matches the cutting machine's `processes_type`. The field is recorded now; the validation comes in a later spec.

## Users and Flows

**Who uses the app:** Leads, supervisors, and area engineers only. The app runs on the FLEX computer. Molding operators record parameters and quality on paper; those stay out of scope.

**Register Scrap flow:**
1. Lead or supervisor logs in (ADMIN or SUPERVISOR role).
2. Opens Register Scrap from the sidebar (between Register Molding Output and Reports).
3. Selects shift (defaults to the current shift from `GET /api/shifts/current` when one is active; no preselection when that endpoint returns 204), selects active profile, enters quantity and free-text reason.
4. Submits. Backend resolves `operatorId` from the JWT principal — it is never in the request body.
5. On success the form resets. On error a clear message is shown.

**My Activity flow (updated):**
1. User views My Activity table. Scrap rows appear with a profile code but no container.
2. Opening a scrap detail modal shows shift, profile, quantity, reason, and recorded-at. The detail modal has no container row.

## Backend Scope

### Migration V4

File: `V4__operational_model_alignment.sql`. V1, V2, V3 must not be modified.

**profiles table — add columns:**

Adding `type` and `position` with a silent `DEFAULT` would mislabel all existing legacy profiles as HEADER/FRONT. This is not acceptable. The migration must fail loudly rather than guess.

Correct sequence within V4:
1. Add `type VARCHAR(10)` as NULLABLE (no DEFAULT) and `profile_position VARCHAR(10)` as NULLABLE (no DEFAULT). The DB column is named `profile_position` because `position` is a reserved keyword in PostgreSQL and a hazard with Hibernate-generated SQL. The Java field and JSON property remain `position`; only the DB column and its `@Column(name = "profile_position")` mapping change.
2. Backfill `type` and `profile_position` via the seed UPDATE statements for the 4 real EXTE profiles (see Seed data below).
3. Backfill any remaining legacy profile rows with explicit known values. If unknown rows exist in production with no assigned type/position, the migration must raise an error rather than apply a default silently.
4. Apply `ALTER COLUMN type SET NOT NULL`, `ADD CHECK (type IN ('HEADER', 'LOWER'))`, `ALTER COLUMN profile_position SET NOT NULL`, `ADD CHECK (profile_position IN ('FRONT', 'REAR'))`.

**machines table — add columns:**
- `code VARCHAR(20) UNIQUE` — nullable; unique when present. The plant has not supplied a coding scheme for all machines, and inventing identifiers in a traceability catalog is worse than leaving the field empty.
- `status VARCHAR(20) NOT NULL DEFAULT 'OPERATIONAL'` with `CHECK (status IN ('OPERATIONAL', 'MAINTENANCE', 'OUT_OF_SERVICE'))`.
- `processes_type VARCHAR(10)` with `CHECK (processes_type IN ('HEADER', 'LOWER'))` — nullable at DB level because existing rows predate this column; required on creation, optional on update (legacy machines referenced by cutting records may have NULL and must remain editable without being forced to supply a value).
- `cycle_time_seconds INTEGER`.
- `last_maintenance_date DATE`.
- `observations VARCHAR(500)`.

**scrap_records table — rebuild:**

Because `scrap_records` is empty (COUNT = 0 verified), drop the existing FK to `cutting_records` and add the new structure:
- Drop `cutting_record_id` column.
- Add `shift_id BIGINT NOT NULL REFERENCES shifts(id)`.
- Add `profile_id BIGINT NOT NULL REFERENCES profiles(id)`.
- Add `operator_id BIGINT NOT NULL REFERENCES users(id)`.
- Enforce `quantity > 0` (replace `>= 0` with `> 0`).
- Add indexes on `(operator_id, shift_id)` and `(profile_id)`.

**Seed data (within V4, after schema changes):**

Shifts — seed the two canonical plant shifts by name in Spanish (as plant supervisors read them); insert if not present:
- Matutino: `start_time = '07:30'`, `end_time = '17:00'`
- Nocturno: `start_time = '19:30'`, `end_time = '07:30'` (crosses midnight)

Do not deactivate any existing shift rows. `cutting_records` references shifts by FK; deactivating a row would orphan historical cutting records.

Profiles — update or insert each of the 4 real profiles (code, name, type, profile_position):
- EXTE00036: name="Header-frontal", type=HEADER, position=FRONT
- EXTE00037: name="Header-REAR",    type=HEADER, position=REAR
- EXTE00038: name="LOWER-REAR",     type=LOWER,  position=REAR
- EXTE00039: name="LOWER-FRONT",    type=LOWER,  position=FRONT

Note: `profiles.code` is `VARCHAR(10)`. EXTE00036 is 9 characters and fits, but there is almost no headroom for longer codes in the future.

Any existing profile rows not in this canonical list that are not referenced by other tables (receptions, cutting records) must be soft-deleted (`active = false`). Rows referenced by data must be left active; they will still appear in historical records but will not appear in catalog picker dropdowns.

Machines — insert Tecma and Milan if not present; update if present:
- Tecma: name="Tecma", code="TECMA004", processes_type=HEADER, status=OPERATIONAL
- Milan: name="Milan",  code="MILAN",    processes_type=LOWER,  status=OPERATIONAL

Milan's real short code is non-blocking — implementation may proceed with "MILAN" while awaiting plant confirmation (see Open Questions).

Legacy machines not in this canonical list: soft-delete (`active = false`) any machine row not named "Tecma" or "Milan" that is NOT referenced by `cutting_records`. Machine rows that ARE referenced by `cutting_records` must stay active so historical records still resolve; their `processes_type` remains NULL and no value is backfilled. Verified state: "CUT-02" is active and is referenced by cutting records — it stays active with NULL `processes_type`. "Cut1231" is already inactive — no action needed.

### profiles module

Propagate `type` and `position` through:
- `Profile` domain model (add fields, add validation: both required).
- `CreateProfileCommand` and `UpdateProfileCommand` (add `type`, `position`).
- `ProfileResult` (add `type`, `position`).
- `ProfileJpaEntity`: add `type` field (maps to DB column `type`); add Java field `position` mapped to DB column `profile_position` via `@Column(name = "profile_position")`.
- `ProfilePersistenceMapper` (map both fields).
- `ProfileCreateRequest` and `ProfileUpdateRequest` REST DTOs (add `type`, `position` with `@NotBlank`/enum validation).
- `ProfileResponse` REST DTO (add `type`, `position`).

### machines module

Propagate `code`, `status`, `processesType`, `cycleTimeSeconds`, `lastMaintenanceDate`, `observations` through:
- `Machine` domain model: add fields; add domain enum or string constants for `MachineStatus` (`OPERATIONAL`, `MAINTENANCE`, `OUT_OF_SERVICE`) and `ProcessesType` (`HEADER`, `LOWER`).
- `CreateMachineCommand` and `UpdateMachineCommand`.
- `MachineResult`.
- `MachineJpaEntity`.
- `MachinePersistenceMapper`.
- `MachineCreateRequest` REST DTO — `status` required; `processesType` required; `code` optional (nullable, unique when present); others optional.
- `MachineUpdateRequest` REST DTO — `status` required; `processesType` optional (nullable; legacy machines with NULL must be editable without supplying it); `code` optional; others optional.
- `MachineResponse` REST DTO.
- Domain error: `machine.duplicate-code` for unique code violation (separate from `machine.duplicate-name`).

`processes_type` is foundational: it records the plant fact that Tecma processes HEADER profiles and Milan processes LOWER profiles. It will later back a validation preventing a LOWER profile from being cut on Tecma. That validation is out of scope for this spec — the field is captured now, enforced later.

### scrap module — full rewrite

**Domain:**
- `ScrapRecord`: replace `cuttingRecordId` with `shiftId`, `profileId`, `operatorId`. Add domain factory `create(shiftId, profileId, operatorId, quantity, reason)`. Validate `quantity > 0`.
- `RegisterScrapCommand`: `shiftId`, `profileId`, `quantity`, `reason`. No `operatorId` — resolved from principal.
- `ScrapResult`: `id`, `shiftId`, `shiftName`, `profileId`, `profileCode`, `operatorId`, `quantity`, `reason`, `createdAt`.
- Domain exception `ScrapShiftInactiveException` (error code `scrap.shift-inactive`, `DomainErrorType.BUSINESS_RULE`).
- Domain exception `ScrapProfileInactiveException` (error code `scrap.profile-inactive`, `DomainErrorType.BUSINESS_RULE`).
- Existing `ScrapNotFoundException` remains.

**Application:**
- `RegisterScrapService` (WRITE path): resolve `operatorId` from `AuthenticatedUserContext` (passed by the controller, extracted from `principal.userId()`). Look up shift with `findActiveById` — reject inactive shift with `ScrapShiftInactiveException` (422 `scrap.shift-inactive`). Look up profile with `findActiveById` — reject inactive profile with `ScrapProfileInactiveException` (422 `scrap.profile-inactive`). Save.
- `GetScrapService` (READ path): resolve shift and profile **without the active filter** (`findById`, not `findActiveById`) so that a scrap record whose shift or profile was later deactivated remains readable. Update persistence query.
- `ShiftRepositoryPort`: add `findById(Long)` returning `Optional<Shift>`. **Do not remove or alter `findActiveById`.** `ShiftPersistenceAdapter` implements the new method. Pickers and write-path validation continue to use `findActiveById`.
- `ProfileRepositoryPort`: add `findById(Long)` for the read path if not already present.

**Persistence:**
- `ScrapRecordJpaEntity`: replace `cuttingRecordId` with `shiftId`, `profileId`, `operatorId`.
- `ScrapSpringRepository`: entity-based methods are sufficient for write and single-row fetch. List reads use a native join query in `ScrapPersistenceAdapter` to avoid 2N queries per list.
- `ScrapPersistenceAdapter`: update save mapping. For list reads (`findByOperatorId`, `findByOperatorIdAndShiftId`), use a native SQL query that joins shifts and profiles and returns all display fields in one round trip:
  ```sql
  SELECT sr.id, sr.shift_id, s.name AS shift_name,
         sr.profile_id, p.code AS profile_code,
         sr.operator_id, sr.quantity, sr.reason, sr.created_at
  FROM scrap_records sr
  JOIN shifts s ON sr.shift_id = s.id
  JOIN profiles p ON sr.profile_id = p.id
  WHERE sr.operator_id = :operatorId
  ```
  The join must NOT filter on `s.active` or `p.active` — records whose shift or profile was later deactivated must still resolve. Apply the defensive `Object[]` unwrap (see Native-query row-mapper warning above) before casting any column value.

> **Native-query row-mapper warning:** Spring Data JPA sometimes wraps a single-row `Object[]` result inside another `Object[]`, producing `ClassCastException: [Ljava.lang.Object; cannot be cast to java.lang.Number`. This has already occurred in `InventoryPersistenceAdapter` and `CuttingPersistenceAdapter` and was fixed with:
> ```java
> Object[] cols = (row[0] instanceof Object[]) ? (Object[]) row[0] : row;
> ```
> Every native-query row mapper introduced by this spec — including `mapScrapRow` in the activity module — must apply this defensive unwrap before casting any column value.

**REST:**
- `ScrapRequest`: `shiftId` (required), `profileId` (required), `quantity` (required, `> 0`), `reason` (optional free text, `@Size(max = 255)`).
- `ScrapResponse`: `id`, `shiftId`, `shiftName`, `profileId`, `profileCode`, `operatorId`, `quantity`, `reason`, `createdAt`.
- `ScrapRestController`: `POST /api/scrap` — extract `operatorId` from `principal.userId()`, not from request body.

### activity module

**Backend query update — `findScrapByOperatorAndShift`:**

Old query joins through `cutting_records → inventory_items → receptions → containers/profiles`.
New query:
```
SELECT sr.id, p.code, sr.quantity, sr.created_at
FROM scrap_records sr
JOIN profiles p ON sr.profile_id = p.id
WHERE sr.operator_id = :operatorId AND sr.shift_id = :shiftId
```

**`mapScrapRow` update:**
- Apply the defensive `Object[]` unwrap (see Native-query row-mapper warning in the scrap module section) before casting any column value.
- `containerCode` → `null` (scrap has no container).
- `profileCode` → `row[1]` (profiles.code).
- `primaryQuantity` → `row[2]`.
- `recordedAt` → `row[3]`.

### SecurityConfiguration

Change only the four operational POST rules. No other rules change.

```
Before:
.requestMatchers(HttpMethod.POST, "/api/receptions").hasAnyRole("ADMIN", "OPERADOR", "SUPERVISOR")
.requestMatchers(HttpMethod.POST, "/api/cutting").hasAnyRole("ADMIN", "OPERADOR", "SUPERVISOR")
.requestMatchers(HttpMethod.POST, "/api/scrap").hasAnyRole("ADMIN", "OPERADOR", "SUPERVISOR")
.requestMatchers(HttpMethod.POST, "/api/molding-outputs").hasAnyRole("ADMIN", "OPERADOR", "SUPERVISOR")

After:
.requestMatchers(HttpMethod.POST, "/api/receptions").hasAnyRole("ADMIN", "SUPERVISOR")
.requestMatchers(HttpMethod.POST, "/api/cutting").hasAnyRole("ADMIN", "SUPERVISOR")
.requestMatchers(HttpMethod.POST, "/api/scrap").hasAnyRole("ADMIN", "SUPERVISOR")
.requestMatchers(HttpMethod.POST, "/api/molding-outputs").hasAnyRole("ADMIN", "SUPERVISOR")
```

GET endpoints (`/api/receptions/my`, `/api/cutting/my`, etc.) remain `.authenticated()`.
Catalog GET/POST/PUT/DELETE rules unchanged.

## Frontend Scope

### Register Scrap page

- Route: `/register-scrap`, lazy-loaded, guarded by `authGuard` + `roleGuard(['ADMIN', 'SUPERVISOR'])`.
- Nav item: added to `NAV_ITEMS` between Register Molding Output and Reports; visible only to ADMIN and SUPERVISOR.
- Page component: `RegisterScrapPageComponent` under `features/scrap/pages/`.
- API client: `ScrapApiClient` under `features/scrap/data-access/`; typed `POST /api/scrap`.
- Form fields:
  - **Shift** — dropdown populated from `GET /api/shifts` (all active shifts). On load, attempt `GET /api/shifts/current`; if it returns a shift, preselect it; if it returns 204, leave nothing preselected. The form is usable in both cases.
  - **Profile** — dropdown bound to active profiles from `GET /api/profiles`.
  - **Quantity** — number input, required, minimum 1.
  - **Reason** — textarea, optional.
- On submit: POST body is `{ shiftId, profileId, quantity, reason }` — no operatorId.
- On success: reset form (shift reverts to the current-shift default when one exists; otherwise the dropdown returns to no selection).
- On error: display API error message.

### Profile catalog updates

Profiles catalog page and form must show and accept `type` (HEADER/LOWER) and `position` (FRONT/REAR).
- List/table: add Type and Position columns.
- Create/edit form: add Type and Position dropdowns (required).

### Machine catalog updates

Machines catalog page and form must show and accept the new fields.
- List/table: add Code, Status, Processes columns.
- Create form: Code (optional, unique when present), Status (required, OPERATIONAL/MAINTENANCE/OUT_OF_SERVICE), Processes Type (required, HEADER/LOWER), Cycle Time (optional integer seconds), Last Maintenance Date (optional date), Observations (optional textarea).
- Edit form: same fields but Processes Type is optional (legacy machines with a NULL value must be editable without forcing the user to supply one).

### My Activity — scrap rows without container

- The `ActivityRecord` model must allow `containerCode` to be `null` or `undefined`.
- Activity table: when `containerCode` is absent for a SCRAP row, display "—" in the container column instead of a link.
- `activity-detail-modal.component.ts` has a hardcoded SCRAP case in `detailRows()` whose first row is `{ label: 'Cutting Record ID', value: d.cuttingRecordId ?? '—' }`. After the scrap rewrite that field no longer exists, so the row renders a meaningless em dash, and the modal never shows shift or profile. Replace the SCRAP rows with: Shift (`shiftName`), Profile (`profileCode`), Quantity, Reason, Created At. No container row, no cutting-record row.

### Activity filter

- `activity-filter.service.ts` calls `r.containerCode.toLowerCase()` and `r.profileCode.toLowerCase()` with no null guard. With a null `containerCode` on SCRAP rows, typing in the search box throws a `TypeError` and breaks the page. Both `containerCode` and `profileCode` must be null-guarded before calling `toLowerCase()`.

### Uncovered time window (17:00–19:30)

No shift covers 17:00 to 19:30. In that window `GET /api/shifts/current` returns 204. This is plant reality, not a defect.

- **Register Scrap:** Populate the shift dropdown from `GET /api/shifts` (all active shifts). Use `GET /api/shifts/current` only to preselect a default when one exists. A 204 response must not block the form or surface an error message — the dropdown shows all shifts with nothing preselected and the user picks manually.
- **Register Cut:** Same behavior: a 204 from `/api/shifts/current` must not prevent the user from choosing a shift manually.
- **Dashboard:** Must render without throwing when `/api/shifts/current` returns 204. An informational "No active shift" state is acceptable.

## Data and Validation

### profiles

| Field | Required | Constraint |
|---|---|---|
| code | yes | unique, max 10 chars |
| name | yes | max 100 chars |
| type | yes | HEADER or LOWER |
| position | yes | FRONT or REAR |
| description | no | max 255 chars |

### machines

| Field | Required | Constraint |
|---|---|---|
| name | yes | unique, max 80 chars |
| code | no | unique when present, max 20 chars |
| status | yes | OPERATIONAL, MAINTENANCE, or OUT_OF_SERVICE |
| processesType | yes (create) | HEADER or LOWER; optional on update (NULL allowed for legacy rows) |
| cycleTimeSeconds | no | positive integer |
| lastMaintenanceDate | no | date, not in future |
| observations | no | max 500 chars |

### scrap_records

| Field | Required | Constraint |
|---|---|---|
| shiftId | yes | must reference an active shift |
| profileId | yes | must reference an active profile |
| operatorId | from JWT | never from request body |
| quantity | yes | > 0 |
| reason | no | max 255 chars |

Inactive shift reference → `scrap.shift-inactive` (422).
Inactive profile reference → `scrap.profile-inactive` (422).

### Seeded data correctness

- Tecma (code TECMA004): processes HEADER profiles (EXTE00036, EXTE00037).
- Milan (code MILAN): processes LOWER profiles (EXTE00038, EXTE00039).
- Nocturno shift crosses midnight: `start_time = 19:30`, `end_time = 07:30` (existing domain already allows this).

## API Contract

### POST /api/scrap

**Request:**
```json
{
  "shiftId": 1,
  "profileId": 2,
  "quantity": 5,
  "reason": "Deformación en prensa"
}
```

**Response 201:**
```json
{
  "id": 42,
  "shiftId": 1,
  "shiftName": "Nocturno",
  "profileId": 2,
  "profileCode": "EXTE00038",
  "operatorId": 7,
  "quantity": 5,
  "reason": "Deformación en prensa",
  "createdAt": "2026-07-09T22:14:00"
}
```

**Error 422 — inactive shift:**
```json
{ "error": "scrap.shift-inactive", "message": "..." }
```

**Error 422 — inactive profile:**
```json
{ "error": "scrap.profile-inactive", "message": "..." }
```

**Error 403 — OPERADOR role:**
Standard forbidden response (no change to error handler needed).

### GET /api/profiles (updated response shape)

Each profile object gains `"type": "HEADER"` and `"position": "FRONT"`.

### GET /api/machines (updated response shape)

Each machine object gains `"code"`, `"status"`, `"processesType"`, `"cycleTimeSeconds"`, `"lastMaintenanceDate"`, `"observations"` (nullable fields return `null`).

## Security and Access

- Operational writes (POST receptions, cutting, scrap, molding-outputs) restricted to ADMIN and SUPERVISOR. OPERADOR role loses write access. GET endpoints for those resources remain `.authenticated()` — any valid JWT may read.
- `operatorId` on scrap records always sourced from `principal.userId()` in the controller, never accepted from the client.
- Catalog write rules (POST/PUT/DELETE) remain ADMIN-only. Catalog read rules remain ADMIN/SUPERVISOR/OPERADOR.
- Security reviewer (`hexagonal-security-reviewer`) must run after `SecurityConfiguration` changes.

## Acceptance Criteria

1. `V4__operational_model_alignment.sql` applies cleanly on top of V1, V2, V3 with no errors.
2. `profiles` table has `type` and `profile_position` columns with CHECK constraints; `GET /api/profiles` returns `type` and `position` in the JSON payload.
3. Profiles catalog shows Type and Position columns and allows creating/editing them.
4. `machines` table has `code`, `status`, `processes_type`, `cycle_time_seconds`, `last_maintenance_date`, `observations`. `GET /api/machines` returns all new fields.
5. Machines catalog shows Code, Status, Processes, Cycle Time, Last Maintenance, Observations and allows creating/editing them.
6. Seeded: Matutino shift (07:30–17:00), Nocturno shift (19:30–07:30), profiles EXTE00036–39 with correct type/position/name, machines Tecma (HEADER, code TECMA004) and Milan (LOWER, code MILAN).
7. `scrap_records` has `shift_id`, `profile_id`, `operator_id`, `quantity`, `reason`, `created_at`. Old `cutting_record_id` column is gone.
8. `POST /api/scrap` with valid `{ shiftId, profileId, quantity, reason }` returns 201 and a response containing `shiftName`, `profileCode`, and `operatorId` (resolved from JWT, not from the request).
9. `POST /api/scrap` with an inactive shiftId returns 422 with `scrap.shift-inactive`.
10. `POST /api/scrap` with an inactive profileId returns 422 with `scrap.profile-inactive`.
11. `POST /api/receptions`, `POST /api/cutting`, `POST /api/scrap`, `POST /api/molding-outputs` return 403 for a valid JWT with OPERADOR role.
12. GET endpoints for those same resources return 200 for a valid OPERADOR JWT.
13. Register Scrap page loads for ADMIN/SUPERVISOR, is not accessible to OPERADOR, and submits successfully.
14. Register Scrap shift dropdown defaults to the current shift on page load when `GET /api/shifts/current` returns a shift; shows all active shifts with no preselection when it returns 204.
15. My Activity table displays scrap rows with "—" in the container column (no link).
16. `GET /api/activity/my` returns scrap entries with a null `containerCode` and a valid `profileCode` for the authenticated operator.
17. `.\gradlew.bat test` passes. ArchUnit boundary test passes.
18. Typing in the My Activity search box with SCRAP rows present does not throw a `TypeError`. Search filters correctly when `containerCode` is null.
19. With the clock inside the 17:00–19:30 gap: Register Scrap loads, the dropdown lists both active shifts with none preselected, and a submission with a manually selected shift succeeds.
20. With the clock inside the 17:00–19:30 gap, the Dashboard renders without error or uncaught exception.
21. Opening the detail modal on a SCRAP row shows shift name, profile code, quantity, reason, and created-at; no container row and no cutting-record row.
22. A scrap record whose shift was deactivated after creation is still returned by `GET /api/scrap/{id}` with a populated `shiftName`, and its detail modal renders without error.
23. Editing a legacy machine (e.g., CUT-02) whose `processes_type` is NULL succeeds without being forced to supply a value. Creating a new machine requires `processesType`.

## Open Questions

- **Milan's real short code (non-blocking):** Seed uses `"MILAN"` as a placeholder. Confirm the plant-assigned identifier or accept the placeholder. Implementation may proceed without this answer.
- **Shift attribution at 18:00 (non-blocking):** If a Matutino supervisor captures molding scrap at 18:00 — outside both shift windows — which shift should be attributed? The form lets the user pick manually, so this is a user-training question, not a system constraint. Clarify for documentation and training materials.
- **EXTE00036 vs EXTE00037 physical length (non-blocking, out of scope for this spec):** The plant contact gave contradictory statements about which header profile is physically longer. Physical length is not stored in this spec. Must be settled before adding a length field in a future spec.
