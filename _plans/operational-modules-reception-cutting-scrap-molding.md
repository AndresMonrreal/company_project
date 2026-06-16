    # Plan: Operational Modules — Reception, Cutting, Scrap, Molding

## Context

This plan implements the six operational modules that form the core traceability flow (Reception → Inventory → Cutting → Scrap + Molding Output) plus the Activity cross-cut. All PostgreSQL tables exist in `V1__create_initial_schema.sql` — no Flyway migrations are added. The `CuttingQuantities` value object already exists at `tesla-api/src/main/java/com/example/company/cutting/domain/model/CuttingQuantities.java`.

## Package base

`com.example.company.<module>` under `tesla-api/src/main/java/com/example/company/`

## Build order rationale

`inventory` output ports are injected by `reception` and `cutting` application services. Inventory must be complete before those use cases compile. Scrap and molding are independent of each other. Activity depends on all four operational modules' JPA entities for its native SQL queries.

---

## Phase 1 — Inventory Module

### 1.1 InventoryItemStatus enum
**File:** `inventory/domain/model/InventoryItemStatus.java`.

Pure Java enum with values `AVAILABLE` and `CUT`. No imports .

### 1.2 InventoryItem domain record
**File:** `inventory/domain/model/InventoryItem.java`

Java record with fields: `Long id`, `Long receptionId`, `int availableQuantity`, `InventoryItemStatus status`. No Spring/JPA imports.

### 1.3 AvailableInventoryResult domain record
**File:** `inventory/domain/model/AvailableInventoryResult.java`

Java record with fields: `Long inventoryItemId`, `String containerCode`, `String profileCode`, `String lot`, `int availableQuantity`. No Spring/JPA imports.

### 1.4 InventoryNotAvailableException
**File:** `inventory/domain/exception/InventoryNotAvailableException.java`

Extends `com.example.company.shared.domain.exception.DomainException`. Constructor takes `String containerCode`. Passes `DomainErrorType.NOT_FOUND`, code `"inventory.not-available"`, message `"No available inventory item for container: " + containerCode`.

### 1.5 GetAvailableInventoryUseCase input port
**File:** `inventory/domain/port/in/GetAvailableInventoryUseCase.java`

Interface with single method: `AvailableInventoryResult findAvailableByContainerCode(String containerCode)`.

### 1.6 InventoryItemCreationPort output port
**File:** `inventory/domain/port/out/InventoryItemCreationPort.java`

Interface with single method: `void createInventoryItem(Long receptionId, int availableQuantity)`. Called by the reception use case after saving a reception.

### 1.7 InventoryItemUpdatePort output port
**File:** `inventory/domain/port/out/InventoryItemUpdatePort.java`

Interface with single method: `void markAsCut(Long inventoryItemId)`. Called by the cutting use case after saving a cutting record.

### 1.8 InventoryItemRepositoryPort output port
**File:** `inventory/domain/port/out/InventoryItemRepositoryPort.java`

Interface with single method: `Optional<AvailableInventoryResult> findAvailableByContainerCode(String containerCode)`. Returns `Optional.empty()` when no `AVAILABLE` item exists for the given container code.

### 1.9 InventoryItemJpaEntity
**File:** `inventory/adapter/out/persistence/InventoryItemJpaEntity.java`

`@Entity @Table(name = "inventory_items")`. Fields: `@Id @GeneratedValue Long id`, `@Column(name = "reception_id") Long receptionId`, `@Column(name = "available_quantity") int availableQuantity`, `@Column String status` (stored as VARCHAR matching enum name), `@Version Long version`, `@Column(name = "updated_at") LocalDateTime updatedAt`. Add `@PrePersist` and `@PreUpdate` to set `updatedAt`. Use package-private constructor and package-private getters/setters following the `ProfileJpaEntity` pattern. No JPA relationships — scalar FKs only.

### 1.10 InventoryItemSpringRepository
**File:** `inventory/adapter/out/persistence/InventoryItemSpringRepository.java`

Extends `JpaRepository<InventoryItemJpaEntity, Long>`. Two custom JPQL queries using `@Query`:

- `findAvailableByContainerCode`: joins `inventory_items ii → receptions r → containers c` and `r → profiles p`. Filters `ii.status = 'AVAILABLE'` and `c.code = :containerCode`. Projects a Spring Data Projection interface or returns `Object[]` projected to `AvailableInventoryResult` — simplest approach: return `List<Object[]>` with columns `(ii.id, c.code, p.code, r.lot, ii.availableQuantity)` and map in the adapter.

- `findById` is inherited from `JpaRepository`.

- `markAsCut`: `@Modifying @Query("UPDATE InventoryItemJpaEntity i SET i.status = 'CUT', i.updatedAt = CURRENT_TIMESTAMP WHERE i.id = :id")`.

### 1.11 InventoryPersistenceAdapter
**File:** `inventory/adapter/out/persistence/InventoryPersistenceAdapter.java`

`@Repository`. Implements `InventoryItemCreationPort`, `InventoryItemUpdatePort`, and `InventoryItemRepositoryPort`.

- `createInventoryItem(Long receptionId, int availableQuantity)`: creates new `InventoryItemJpaEntity` with status `"AVAILABLE"` and saves.
- `markAsCut(Long inventoryItemId)`: calls repository's `markAsCut` update query with `@Modifying @Transactional`.
- `findAvailableByContainerCode(String containerCode)`: calls the JPQL query, maps `Object[]` result to `AvailableInventoryResult`, wraps in `Optional`.

### 1.12 GetAvailableInventoryService
**File:** `inventory/application/usecase/GetAvailableInventoryService.java`

`@Service`. Implements `GetAvailableInventoryUseCase`. Injects `InventoryItemRepositoryPort`. Calls `findAvailableByContainerCode`, throws `InventoryNotAvailableException` if `Optional.empty()`.

### 1.13 AvailableInventoryResponse REST DTO
**File:** `inventory/adapter/in/web/dto/AvailableInventoryResponse.java`

Java record with fields matching `AvailableInventoryResult`: `inventoryItemId`, `containerCode`, `profileCode`, `lot`, `availableQuantity`.

### 1.14 InventoryRestController
**File:** `inventory/adapter/in/web/InventoryRestController.java`

`@RestController @RequestMapping("/api/inventory")`. Injects `GetAvailableInventoryUseCase`. One endpoint:

- `GET /available?containerCode={code}` → calls `findAvailableByContainerCode`, maps result to `AvailableInventoryResponse`, returns 200.

---

## Phase 2 — Reception Module

### 2.1 ReceptionStatus enum
**File:** `reception/domain/model/ReceptionStatus.java`

Pure Java enum with value `RECEIVED`.

### 2.2 Reception domain record
**File:** `reception/domain/model/Reception.java`

Java record: `Long id`, `Long containerId`, `Long profileId`, `Long operatorId`, `String lot`, `int receivedQuantity`, `ReceptionStatus status`, `java.time.LocalDateTime receivedAt`. Static factory `Reception.create(containerId, profileId, operatorId, lot, receivedQuantity)` returns record with `id = null`, `status = RECEIVED`, `receivedAt = null` (set by persistence).

### 2.3 ReceptionNotFoundException
**File:** `reception/domain/exception/ReceptionNotFoundException.java`

Extends `DomainException`. `DomainErrorType.NOT_FOUND`, code `"reception.not-found"`, message `"Reception not found: " + id`.

### 2.4 RegisterReceptionCommand
**File:** `reception/domain/port/in/RegisterReceptionCommand.java`

Java record: `Long containerId`, `Long profileId`, `String lot`, `int receivedQuantity`, `Long operatorId`.

### 2.5 ReceptionResult
**File:** `reception/domain/port/in/ReceptionResult.java`

Java record: `Long id`, `Long containerId`, `Long profileId`, `Long operatorId`, `String lot`, `int receivedQuantity`, `String status`, `java.time.LocalDateTime receivedAt`.

### 2.6 RegisterReceptionUseCase
**File:** `reception/domain/port/in/RegisterReceptionUseCase.java`

Interface: `ReceptionResult register(RegisterReceptionCommand command)`.

### 2.7 GetReceptionUseCase
**File:** `reception/domain/port/in/GetReceptionUseCase.java`

Interface with two methods:
- `ReceptionResult findById(Long id)`
- `List<ReceptionResult> findByOperatorAndShift(Long operatorId, Long shiftId)`

### 2.8 ReceptionRepositoryPort
**File:** `reception/domain/port/out/ReceptionRepositoryPort.java`

Interface:
- `Reception save(Reception reception)`
- `Optional<Reception> findById(Long id)`
- `List<Reception> findByOperatorAndTimeWindow(Long operatorId, java.time.LocalTime startTime, java.time.LocalTime endTime, boolean overnight)`

### 2.9 ReceptionJpaEntity
**File:** `reception/adapter/out/persistence/ReceptionJpaEntity.java`

`@Entity @Table(name = "receptions")`. Fields: `Long id` (generated), `Long containerId`, `Long profileId`, `Long operatorId`, `String lot`, `int receivedQuantity`, `String status`, `@Column(name = "received_at") LocalDateTime receivedAt`, `@Version Long version`. `@PrePersist` sets `receivedAt = LocalDateTime.now()`. Package-private getters, no JPA relationships.

### 2.10 ReceptionSpringRepository
**File:** `reception/adapter/out/persistence/ReceptionSpringRepository.java`

Extends `JpaRepository<ReceptionJpaEntity, Long>`. Two custom `@Query` methods:

- `findByOperatorAndSameDayWindow(Long operatorId, LocalTime start, LocalTime end)`: filters `r.operatorId = :operatorId AND CAST(r.receivedAt AS java.time.LocalTime) BETWEEN :startTime AND :endTime`. Use `FUNCTION('CAST', r.receivedAt, 'time')` or a native query.

- `findByOperatorAndOvernightWindow(Long operatorId, LocalTime start, LocalTime end)`: filters `r.operatorId = :operatorId AND (CAST(r.receivedAt AS LocalTime) >= :startTime OR CAST(r.receivedAt AS LocalTime) <= :endTime)`.

Both can be combined into a single native SQL method taking a boolean, or kept as two separate named methods selected by the adapter.

### 2.11 ReceptionPersistenceAdapter
**File:** `reception/adapter/out/persistence/ReceptionPersistenceAdapter.java`

`@Repository`. Implements `ReceptionRepositoryPort`.
- `save`: creates entity from domain, saves, maps back.
- `findById`: calls repository, maps to domain, throws `ReceptionNotFoundException` if absent.
- `findByOperatorAndTimeWindow`: calls the appropriate repository query based on `overnight` flag.

Includes a `PersistenceMapper` as a private inner mapper or separate `@Component`.

### 2.12 RegisterReceptionService
**File:** `reception/application/usecase/RegisterReceptionService.java`

`@Service`. Implements `RegisterReceptionUseCase`. Injects `ReceptionRepositoryPort` and `InventoryItemCreationPort` (from inventory module output port).

`@Transactional register(command)`:
1. `Reception.create(...)` domain factory
2. `receptionRepository.save(reception)` → saved reception with id
3. `inventoryItemCreationPort.createInventoryItem(savedReception.id(), savedReception.receivedQuantity())`
4. Return mapped `ReceptionResult`

### 2.13 GetReceptionService
**File:** `reception/application/usecase/GetReceptionService.java`

`@Service`. Implements `GetReceptionUseCase`. Injects `ReceptionRepositoryPort` and `com.example.company.shifts.domain.port.out.ShiftRepositoryPort`.

`findByOperatorAndShift(operatorId, shiftId)`:
1. Load shift via `ShiftRepositoryPort.findActiveById(shiftId)` — throw `ShiftNotFoundException` if absent (from shifts module)
2. Determine `overnight = shift.endTime().compareTo(shift.startTime()) <= 0`
3. Call `receptionRepository.findByOperatorAndTimeWindow(operatorId, shift.startTime(), shift.endTime(), overnight)`
4. Map each to `ReceptionResult`

### 2.14 ReceptionRequest DTO
**File:** `reception/adapter/in/web/dto/ReceptionRequest.java`

Java record: `@NotNull Long containerId`, `@NotNull Long profileId`, `@NotBlank @Size(max=80) String lot`, `@Min(1) int receivedQuantity`.

### 2.15 ReceptionResponse DTO
**File:** `reception/adapter/in/web/dto/ReceptionResponse.java`

Java record: `Long id`, `Long containerId`, `Long profileId`, `Long operatorId`, `String lot`, `int receivedQuantity`, `String status`, `java.time.LocalDateTime receivedAt`.

### 2.16 ReceptionRestController
**File:** `reception/adapter/in/web/ReceptionRestController.java`

`@RestController @RequestMapping("/api/receptions")`. Injects `RegisterReceptionUseCase` and `GetReceptionUseCase`.

Endpoints:
- `POST /api/receptions` — `@ResponseStatus(CREATED)`, `@Valid @RequestBody ReceptionRequest`, `@AuthenticationPrincipal AuthenticatedUserPrincipal principal`. Build `RegisterReceptionCommand` using `principal.userId()` as operatorId.
- `GET /api/receptions/my?shiftId={id}` — `@RequestParam Long shiftId`, `@AuthenticationPrincipal` principal. Calls `getReception.findByOperatorAndShift(principal.userId(), shiftId)`, maps to `List<ReceptionResponse>`.
- `GET /api/receptions/{id}` — calls `findById(id)`, maps to `ReceptionResponse`.

Import `com.example.company.security.model.AuthenticatedUserPrincipal` from the security module.

---

## Phase 3 — Cutting Module

### 3.1 CuttingQuantityInvariantException
**File:** `cutting/domain/exception/CuttingQuantityInvariantException.java`

Extends `DomainException`. `DomainErrorType.BUSINESS_RULE`, code `"cutting.quantity-invariant"`. Constructor takes the original `IllegalArgumentException` message from `CuttingQuantities` and passes it as the message.

### 3.2 CuttingNotAvailableException
**File:** `cutting/domain/exception/CuttingNotAvailableException.java`

Extends `DomainException`. `DomainErrorType.NOT_FOUND`, code `"cutting.not-available"`, message `"No available cutting record for container: " + containerCode`.

### 3.3 CuttingRecord domain record
**File:** `cutting/domain/model/CuttingRecord.java`

Java record: `Long id`, `Long inventoryItemId`, `Long machineId`, `Long operatorId`, `Long shiftId`, `CuttingQuantities quantities`, `java.time.LocalDateTime cutAt`. Static factory `CuttingRecord.create(inventoryItemId, machineId, operatorId, shiftId, CuttingQuantities quantities)` with `id = null`, `cutAt = null`.

### 3.4 AvailableCuttingResult domain record
**File:** `cutting/domain/model/AvailableCuttingResult.java`

Java record: `Long cuttingRecordId`, `String containerCode`, `String profileCode`, `int initialQuantity`, `int goodQuantity`, `int scrapQuantity`, `java.time.LocalDateTime cutAt`.

### 3.5 RegisterCuttingCommand
**File:** `cutting/domain/port/in/RegisterCuttingCommand.java`

Java record: `Long inventoryItemId`, `Long machineId`, `Long shiftId`, `int initialQuantity`, `int goodQuantity`, `int scrapQuantity`, `Long operatorId`.

### 3.6 CuttingResult
**File:** `cutting/domain/port/in/CuttingResult.java`

Java record: `Long id`, `Long inventoryItemId`, `Long machineId`, `Long operatorId`, `Long shiftId`, `int initialQuantity`, `int goodQuantity`, `int scrapQuantity`, `java.time.LocalDateTime cutAt`.

### 3.7 RegisterCuttingUseCase
**File:** `cutting/domain/port/in/RegisterCuttingUseCase.java`

Interface: `CuttingResult register(RegisterCuttingCommand command)`.

### 3.8 GetCuttingUseCase
**File:** `cutting/domain/port/in/GetCuttingUseCase.java`

Interface:
- `CuttingResult findById(Long id)`
- `List<CuttingResult> findByOperatorAndShift(Long operatorId, Long shiftId)`
- `AvailableCuttingResult findAvailableByContainerCode(String containerCode)`

### 3.9 CuttingRepositoryPort
**File:** `cutting/domain/port/out/CuttingRepositoryPort.java`

Interface:
- `CuttingRecord save(CuttingRecord record)`
- `Optional<CuttingRecord> findById(Long id)`
- `List<CuttingRecord> findByOperatorAndShift(Long operatorId, Long shiftId)`
- `Optional<AvailableCuttingResult> findAvailableByContainerCode(String containerCode)`

### 3.10 CuttingRecordJpaEntity
**File:** `cutting/adapter/out/persistence/CuttingRecordJpaEntity.java`

`@Entity @Table(name = "cutting_records")`. Fields: `Long id`, `Long inventoryItemId`, `Long machineId`, `Long operatorId`, `Long shiftId`, `int initialQuantity`, `int goodQuantity`, `int scrapQuantity`, `LocalDateTime cutAt`, `@Version Long version`. `@PrePersist` sets `cutAt`. Package-private getters. No JPA relationships.

### 3.11 CuttingSpringRepository
**File:** `cutting/adapter/out/persistence/CuttingSpringRepository.java`

Extends `JpaRepository<CuttingRecordJpaEntity, Long>`. Custom queries:

- `findByOperatorIdAndShiftId(Long operatorId, Long shiftId)`: derived query method.
- `findAvailableByContainerCode`: JPQL joining `cutting_records cr → inventory_items ii → receptions r → containers c → profiles p`. Filters via join to `inventory_items ii JOIN receptions r ON ii.reception_id = r.id JOIN containers c ON r.container_id = c.id JOIN profiles p ON r.profile_id = p.id` where `c.code = :containerCode`. Returns `Object[]` with columns `(cr.id, c.code, p.code, cr.initialQuantity, cr.goodQuantity, cr.scrapQuantity, cr.cutAt)`.

### 3.12 CuttingPersistenceAdapter
**File:** `cutting/adapter/out/persistence/CuttingPersistenceAdapter.java`

`@Repository`. Implements `CuttingRepositoryPort`.

Mapper: converts `CuttingRecordJpaEntity ↔ CuttingRecord`. The `CuttingRecord` holds a `CuttingQuantities` value object; the entity holds three separate int fields. Map `quantities.initialQuantity()`, `quantities.goodQuantity()`, `quantities.scrapQuantity()` to/from the entity fields.

### 3.13 RegisterCuttingService
**File:** `cutting/application/usecase/RegisterCuttingService.java`

`@Service`. Implements `RegisterCuttingUseCase`. Injects `CuttingRepositoryPort` and `InventoryItemUpdatePort`.

`@Transactional register(command)`:
1. Attempt `new CuttingQuantities(command.initialQuantity(), command.goodQuantity(), command.scrapQuantity())`. Catch `IllegalArgumentException`, rethrow as `CuttingQuantityInvariantException`.
2. `CuttingRecord.create(...)` with the validated `CuttingQuantities`
3. `cuttingRepository.save(cuttingRecord)`
4. `inventoryItemUpdatePort.markAsCut(command.inventoryItemId())`
5. Map saved record to `CuttingResult`

### 3.14 GetCuttingService
**File:** `cutting/application/usecase/GetCuttingService.java`

`@Service`. Implements `GetCuttingUseCase`. Injects `CuttingRepositoryPort`.

- `findById(id)`: load by id, throw `IllegalStateException` (or a not-found variant) if absent. Map to `CuttingResult`.
- `findByOperatorAndShift(operatorId, shiftId)`: direct query since cutting_records has `shift_id` — no time window needed.
- `findAvailableByContainerCode(containerCode)`: calls repository, throws `CuttingNotAvailableException` if `Optional.empty()`.

### 3.15 CuttingRequest DTO
**File:** `cutting/adapter/in/web/dto/CuttingRequest.java`

Java record: `@NotNull Long inventoryItemId`, `@NotNull Long machineId`, `@NotNull Long shiftId`, `@Min(1) int initialQuantity`, `@Min(0) int goodQuantity`, `@Min(0) int scrapQuantity`.

### 3.16 CuttingResponse DTO
**File:** `cutting/adapter/in/web/dto/CuttingResponse.java`

Java record: `Long id`, `Long inventoryItemId`, `Long machineId`, `Long operatorId`, `Long shiftId`, `int initialQuantity`, `int goodQuantity`, `int scrapQuantity`, `java.time.LocalDateTime cutAt`.

### 3.17 AvailableCuttingResponse DTO
**File:** `cutting/adapter/in/web/dto/AvailableCuttingResponse.java`

Java record: `Long cuttingRecordId`, `String containerCode`, `String profileCode`, `int initialQuantity`, `int goodQuantity`, `int scrapQuantity`, `java.time.LocalDateTime cutAt`.

### 3.18 CuttingRestController
**File:** `cutting/adapter/in/web/CuttingRestController.java`

`@RestController @RequestMapping("/api/cutting")`. Injects `RegisterCuttingUseCase` and `GetCuttingUseCase`.

Endpoints:
- `POST /api/cutting` — `@ResponseStatus(CREATED)`, `@Valid @RequestBody CuttingRequest`, `@AuthenticationPrincipal` principal. Builds `RegisterCuttingCommand` with `principal.userId()`.
- `GET /api/cutting/my?shiftId={id}` — maps to `List<CuttingResponse>`.
- `GET /api/cutting/{id}` — maps to `CuttingResponse`.
- `GET /api/cutting/available?containerCode={code}` — maps result to `AvailableCuttingResponse`.

---

## Phase 4 — Scrap Module

### 4.1 ScrapRecord domain record
**File:** `scrap/domain/model/ScrapRecord.java`

Java record: `Long id`, `Long cuttingRecordId`, `int quantity`, `String reason`, `java.time.LocalDateTime createdAt`. Static factory `ScrapRecord.create(cuttingRecordId, quantity, reason)` with `id = null`, `createdAt = null`.

### 4.2 ScrapNotFoundException
**File:** `scrap/domain/exception/ScrapNotFoundException.java`

Extends `DomainException`. `DomainErrorType.NOT_FOUND`, code `"scrap.not-found"`, message `"Scrap record not found: " + id`.

### 4.3 RegisterScrapCommand
**File:** `scrap/domain/port/in/RegisterScrapCommand.java`

Java record: `@NotNull Long cuttingRecordId`, `@Min(1) int quantity`, `String reason`.

### 4.4 ScrapResult
**File:** `scrap/domain/port/in/ScrapResult.java`

Java record: `Long id`, `Long cuttingRecordId`, `int quantity`, `String reason`, `java.time.LocalDateTime createdAt`.

### 4.5 RegisterScrapUseCase
**File:** `scrap/domain/port/in/RegisterScrapUseCase.java`

Interface: `ScrapResult register(RegisterScrapCommand command)`.

### 4.6 GetScrapUseCase
**File:** `scrap/domain/port/in/GetScrapUseCase.java`

Interface:
- `ScrapResult findById(Long id)`
- `List<ScrapResult> findByOperatorAndShift(Long operatorId, Long shiftId)`

### 4.7 ScrapRepositoryPort
**File:** `scrap/domain/port/out/ScrapRepositoryPort.java`

Interface:
- `ScrapRecord save(ScrapRecord record)`
- `Optional<ScrapRecord> findById(Long id)`
- `List<ScrapRecord> findByOperatorAndShift(Long operatorId, Long shiftId)`

### 4.8 ScrapRecordJpaEntity
**File:** `scrap/adapter/out/persistence/ScrapRecordJpaEntity.java`

`@Entity @Table(name = "scrap_records")`. Fields: `Long id`, `Long cuttingRecordId`, `int quantity`, `String reason`, `LocalDateTime createdAt`. `@PrePersist` sets `createdAt`. No version (table has no version column).

### 4.9 ScrapSpringRepository
**File:** `scrap/adapter/out/persistence/ScrapSpringRepository.java`

Extends `JpaRepository<ScrapRecordJpaEntity, Long>`. Custom `@Query` for operator+shift filter: joins `scrap_records s → cutting_records cr` on `s.cuttingRecordId = cr.id` and filters `cr.operatorId = :operatorId AND cr.shiftId = :shiftId`.

### 4.10 ScrapPersistenceAdapter
**File:** `scrap/adapter/out/persistence/ScrapPersistenceAdapter.java`

`@Repository`. Implements `ScrapRepositoryPort`. `save`, `findById` (throws `ScrapNotFoundException`), `findByOperatorAndShift`.

### 4.11 RegisterScrapService
**File:** `scrap/application/usecase/RegisterScrapService.java`

`@Service`. Implements `RegisterScrapUseCase`. Injects `ScrapRepositoryPort`. `@Transactional register(command)`: creates `ScrapRecord.create(...)`, saves, maps to `ScrapResult`.

### 4.12 GetScrapService
**File:** `scrap/application/usecase/GetScrapService.java`

`@Service`. Implements `GetScrapUseCase`. Injects `ScrapRepositoryPort`.

### 4.13 ScrapRequest DTO
**File:** `scrap/adapter/in/web/dto/ScrapRequest.java`

Java record: `@NotNull Long cuttingRecordId`, `@Min(1) int quantity`, `@Size(max=255) String reason` (nullable).

### 4.14 ScrapResponse DTO
**File:** `scrap/adapter/in/web/dto/ScrapResponse.java`

Java record: `Long id`, `Long cuttingRecordId`, `int quantity`, `String reason`, `java.time.LocalDateTime createdAt`.

### 4.15 ScrapRestController
**File:** `scrap/adapter/in/web/ScrapRestController.java`

`@RestController @RequestMapping("/api/scrap")`. Injects `RegisterScrapUseCase` and `GetScrapUseCase`.

Endpoints:
- `POST /api/scrap` — `@ResponseStatus(CREATED)`, `@Valid @RequestBody ScrapRequest`. No principal needed (no operator_id column on scrap_records).
- `GET /api/scrap/my?shiftId={id}` — `@AuthenticationPrincipal` principal, maps to `List<ScrapResponse>`.
- `GET /api/scrap/{id}` — maps to `ScrapResponse`.

---

## Phase 5 — Molding Module

### 5.1 MoldingOutput domain record
**File:** `molding/domain/model/MoldingOutput.java`

Java record: `Long id`, `Long cuttingRecordId`, `int quantitySent`, `Long operatorId`, `java.time.LocalDateTime sentAt`. Static factory `MoldingOutput.create(cuttingRecordId, quantitySent, operatorId)` with `id = null`, `sentAt = null`.

### 5.2 MoldingOutputNotFoundException
**File:** `molding/domain/exception/MoldingOutputNotFoundException.java`

Extends `DomainException`. `DomainErrorType.NOT_FOUND`, code `"molding.not-found"`, message `"Molding output not found: " + id`.

### 5.3 RegisterMoldingOutputCommand
**File:** `molding/domain/port/in/RegisterMoldingOutputCommand.java`

Java record: `Long cuttingRecordId`, `int quantitySent`, `Long operatorId`.

### 5.4 MoldingOutputResult
**File:** `molding/domain/port/in/MoldingOutputResult.java`

Java record: `Long id`, `Long cuttingRecordId`, `int quantitySent`, `Long operatorId`, `java.time.LocalDateTime sentAt`.

### 5.5 RegisterMoldingOutputUseCase
**File:** `molding/domain/port/in/RegisterMoldingOutputUseCase.java`

Interface: `MoldingOutputResult register(RegisterMoldingOutputCommand command)`.

### 5.6 GetMoldingOutputUseCase
**File:** `molding/domain/port/in/GetMoldingOutputUseCase.java`

Interface:
- `MoldingOutputResult findById(Long id)`
- `List<MoldingOutputResult> findByOperatorAndShift(Long operatorId, Long shiftId)`

### 5.7 MoldingOutputRepositoryPort
**File:** `molding/domain/port/out/MoldingOutputRepositoryPort.java`

Interface:
- `MoldingOutput save(MoldingOutput output)`
- `Optional<MoldingOutput> findById(Long id)`
- `List<MoldingOutput> findByOperatorAndTimeWindow(Long operatorId, java.time.LocalTime startTime, java.time.LocalTime endTime, boolean overnight)`

### 5.8 MoldingOutputJpaEntity
**File:** `molding/adapter/out/persistence/MoldingOutputJpaEntity.java`

`@Entity @Table(name = "molding_outputs")`. Fields: `Long id`, `Long cuttingRecordId`, `int quantitySent`, `Long operatorId`, `LocalDateTime sentAt`. `@PrePersist` sets `sentAt`. No version column in schema.

### 5.9 MoldingOutputSpringRepository
**File:** `molding/adapter/out/persistence/MoldingOutputSpringRepository.java`

Extends `JpaRepository<MoldingOutputJpaEntity, Long>`. Two custom `@Query` methods for same-day and overnight time window filtering by `operatorId` and `CAST(sentAt AS LocalTime)`.

### 5.10 MoldingOutputPersistenceAdapter
**File:** `molding/adapter/out/persistence/MoldingOutputPersistenceAdapter.java`

`@Repository`. Implements `MoldingOutputRepositoryPort`.

### 5.11 RegisterMoldingOutputService
**File:** `molding/application/usecase/RegisterMoldingOutputService.java`

`@Service`. Implements `RegisterMoldingOutputUseCase`. Injects `MoldingOutputRepositoryPort`. `@Transactional register(command)`: creates domain object, saves, maps result.

### 5.12 GetMoldingOutputService
**File:** `molding/application/usecase/GetMoldingOutputService.java`

`@Service`. Implements `GetMoldingOutputUseCase`. Injects `MoldingOutputRepositoryPort` and `ShiftRepositoryPort`. Resolves shift time window before calling `findByOperatorAndTimeWindow`.

### 5.13 MoldingOutputRequest DTO
**File:** `molding/adapter/in/web/dto/MoldingOutputRequest.java`

Java record: `@NotNull Long cuttingRecordId`, `@Min(1) int quantitySent`.

### 5.14 MoldingOutputResponse DTO
**File:** `molding/adapter/in/web/dto/MoldingOutputResponse.java`

Java record: `Long id`, `Long cuttingRecordId`, `int quantitySent`, `Long operatorId`, `java.time.LocalDateTime sentAt`.

### 5.15 MoldingOutputRestController
**File:** `molding/adapter/in/web/MoldingOutputRestController.java`

`@RestController @RequestMapping("/api/molding-outputs")`. Endpoints:
- `POST /api/molding-outputs` — `@ResponseStatus(CREATED)`, principal provides operatorId.
- `GET /api/molding-outputs/my?shiftId={id}` — `@AuthenticationPrincipal` + `@RequestParam Long shiftId`.
- `GET /api/molding-outputs/{id}`.

---

## Phase 6 — Activity Module

### 6.1 ActivityAction enum
**File:** `activity/domain/model/ActivityAction.java`

Pure Java enum: `RECEPTION`, `CUT`, `SCRAP`, `MOLDING_OUTPUT`.

### 6.2 ActivityRawEntry domain record
**File:** `activity/domain/model/ActivityRawEntry.java`

Internal record used only within the activity module: `Long id`, `String containerCode`, `String profileCode`, `ActivityAction action`, `java.time.LocalDateTime recordedAt`, `int primaryQuantity`, `Integer secondaryQuantity`, `Integer tertiaryQuantity`. Secondary and tertiary are nullable (only used by CUT for good/scrap).

### 6.3 ActivityResult domain record
**File:** `activity/domain/model/ActivityResult.java`

Java record: `Long id`, `String time` (formatted "HH:mm"), `String containerCode`, `String profileCode`, `ActivityAction action`, `String quantities`, `String status`.

### 6.4 GetActivityUseCase
**File:** `activity/domain/port/in/GetActivityUseCase.java`

Interface: `List<ActivityResult> findByOperatorAndShift(Long operatorId, Long shiftId)`.

### 6.5 ActivityQueryPort
**File:** `activity/domain/port/out/ActivityQueryPort.java`

Interface with four methods, each returning `List<ActivityRawEntry>`:
- `findReceptionsByOperatorAndTimeWindow(Long operatorId, java.time.LocalTime start, java.time.LocalTime end, boolean overnight)`
- `findCuttingByOperatorAndShift(Long operatorId, Long shiftId)` — cutting already has shift_id, use direct match
- `findScrapByOperatorAndShift(Long operatorId, Long shiftId)` — join scrap → cutting → shiftId and operatorId
- `findMoldingByOperatorAndTimeWindow(Long operatorId, java.time.LocalTime start, java.time.LocalTime end, boolean overnight)`

### 6.6 GetActivityService
**File:** `activity/application/usecase/GetActivityService.java`

`@Service`. Implements `GetActivityUseCase`. Injects `ActivityQueryPort` and `ShiftRepositoryPort`.

`findByOperatorAndShift(operatorId, shiftId)`:
1. Load shift via `ShiftRepositoryPort.findActiveById(shiftId)` — throws `ShiftNotFoundException` if absent
2. Compute `overnight = shift.endTime().compareTo(shift.startTime()) <= 0`
3. Call all 4 `ActivityQueryPort` methods
4. Merge all raw entries into one list
5. Sort by `recordedAt` ascending
6. Map each `ActivityRawEntry` to `ActivityResult`:
   - `time` = `DateTimeFormatter.ofPattern("HH:mm").format(entry.recordedAt())`
   - `quantities`:
     - RECEPTION → `"{primaryQuantity} pcs"`
     - CUT → `"{primaryQuantity} → {secondaryQuantity} good · {tertiaryQuantity} scrap"`
     - SCRAP → `"{primaryQuantity} pcs"`
     - MOLDING_OUTPUT → `"{primaryQuantity} pcs"`
   - `status` = `entry.action().name()`

### 6.7 ActivityPersistenceAdapter
**File:** `activity/adapter/out/persistence/ActivityPersistenceAdapter.java`

`@Repository`. Implements `ActivityQueryPort`. Uses **native SQL** (`nativeQuery = true`) to avoid importing JPA entity classes across modules. Injects a custom Spring Data interface or `EntityManager`.

Create an inner Spring Data repository interface `ActivityNativeRepository extends JpaRepository` (blank extends just for Spring context), but prefer injecting `EntityManager` for flexible native SQL.

Each query method builds an `ActivityRawEntry` list from `Object[]` result rows:

- Receptions query: `SELECT r.id, c.code, p.code, r.received_quantity, r.received_at FROM receptions r JOIN containers c ON r.container_id = c.id JOIN profiles p ON r.profile_id = p.id WHERE r.operator_id = :operatorId AND [time window filter]`
- Cutting query: `SELECT cr.id, c.code, p.code, cr.initial_quantity, cr.good_quantity, cr.scrap_quantity, cr.cut_at FROM cutting_records cr JOIN inventory_items ii ON cr.inventory_item_id = ii.id JOIN receptions r ON ii.reception_id = r.id JOIN containers c ON r.container_id = c.id JOIN profiles p ON r.profile_id = p.id WHERE cr.operator_id = :operatorId AND cr.shift_id = :shiftId`
- Scrap query: `SELECT sr.id, c.code, p.code, sr.quantity, sr.created_at FROM scrap_records sr JOIN cutting_records cr ON sr.cutting_record_id = cr.id JOIN inventory_items ii ON cr.inventory_item_id = ii.id JOIN receptions r ON ii.reception_id = r.id JOIN containers c ON r.container_id = c.id JOIN profiles p ON r.profile_id = p.id WHERE cr.operator_id = :operatorId AND cr.shift_id = :shiftId`
- Molding query: `SELECT mo.id, c.code, p.code, mo.quantity_sent, mo.sent_at FROM molding_outputs mo JOIN cutting_records cr ON mo.cutting_record_id = cr.id JOIN inventory_items ii ON cr.inventory_item_id = ii.id JOIN receptions r ON ii.reception_id = r.id JOIN containers c ON r.container_id = c.id JOIN profiles p ON r.profile_id = p.id WHERE mo.operator_id = :operatorId AND [time window filter]`

The time window filter for overnight shifts uses: `CAST(timestamp_col AS TIME) >= :startTime OR CAST(timestamp_col AS TIME) <= :endTime`. For same-day: `CAST(timestamp_col AS TIME) BETWEEN :startTime AND :endTime`.

### 6.8 ActivityResponse DTO
**File:** `activity/adapter/in/web/dto/ActivityResponse.java`

Java record: `Long id`, `String time`, `String containerCode`, `String profileCode`, `String action`, `String quantities`, `String status`.

### 6.9 ActivityRestController
**File:** `activity/adapter/in/web/ActivityRestController.java`

`@RestController @RequestMapping("/api/activity")`. Injects `GetActivityUseCase`.

- `GET /api/activity/my?shiftId={id}` — `@AuthenticationPrincipal` principal, `@RequestParam Long shiftId`. Calls `findByOperatorAndShift(principal.userId(), shiftId)`. Maps `ActivityResult.action().name()` to `ActivityResponse.action`. Returns `List<ActivityResponse>`.

---

## Phase 7 — Shifts: GET /api/shifts/current

### 7.1 Extend GetShiftUseCase
**File:** `shifts/domain/port/in/GetShiftUseCase.java` (modify existing)

Add method: `Optional<ShiftResult> findCurrent()`. Returns the first active shift whose time window contains `LocalTime.now()`, overnight-aware.

### 7.2 Extend ShiftRepositoryPort
**File:** `shifts/domain/port/out/ShiftRepositoryPort.java` (modify existing)

Add method: `List<Shift> findAllActive()` if not already present. The `findCurrent` logic will iterate active shifts and check time containment in the application service. (Alternatively, add `Optional<Shift> findByCurrentTime(LocalTime now)` with JPQL — choose the simpler option.)

Preferred: add `Optional<Shift> findCurrentByTime(LocalTime now)` using a JPQL query that checks `(s.startTime <= :now AND s.endTime > :now)` for same-day and a separate condition for overnight.

### 7.3 Extend ShiftPersistenceAdapter
**File:** `shifts/adapter/out/persistence/ShiftPersistenceAdapter.java` (modify existing)

Implement `findCurrentByTime(LocalTime now)`. Delegates to a new `SpringDataShiftRepository` method with appropriate JPQL.

### 7.4 Extend SpringDataShiftRepository
**File:** `shifts/adapter/out/persistence/SpringDataShiftRepository.java` (modify existing)

Add `@Query` for finding the current active shift: `SELECT s FROM ShiftJpaEntity s WHERE s.active = true AND ((s.startTime <= :now AND s.endTime > :now) OR (s.startTime > s.endTime AND (s.startTime <= :now OR s.endTime > :now)))`.

### 7.5 Extend GetShiftService
**File:** `shifts/application/usecase/GetShiftService.java` (modify existing)

Implement `findCurrent()`. Calls the new repository method with `LocalTime.now()`. Maps result to `Optional<ShiftResult>`.

### 7.6 Add /current endpoint to ShiftRestController
**File:** `shifts/adapter/in/web/ShiftRestController.java` (modify existing)

Add: `GET /api/shifts/current` → calls `getShift.findCurrent()`, returns `ShiftResponse` if present or 204/empty body if none active. Return `ResponseEntity<ShiftResponse>` with `ok()` when present, `noContent()` when absent.

---

## Agent routing

| Phase | Agent |
|---|---|
| Phase 1 (Inventory domain/ports) | `hexagonal-domain-developer` |
| Phase 1 (Inventory persistence) | `hexagonal-persistence-adapter` |
| Phase 1 (Inventory REST) | `hexagonal-web-adapter` |
| Phase 1 (Inventory use case) | `hexagonal-application-developer` |
| Phase 2 (Reception domain/ports) | `hexagonal-domain-developer` |
| Phase 2 (Reception persistence) | `hexagonal-persistence-adapter` |
| Phase 2 (Reception use cases) | `hexagonal-application-developer` |
| Phase 2 (Reception REST) | `hexagonal-web-adapter` |
| Phase 3 (Cutting domain/ports) | `hexagonal-domain-developer` |
| Phase 3 (Cutting persistence) | `hexagonal-persistence-adapter` |
| Phase 3 (Cutting use cases) | `hexagonal-application-developer` |
| Phase 3 (Cutting REST) | `hexagonal-web-adapter` |
| Phase 4 (Scrap) | same pattern |
| Phase 5 (Molding) | same pattern |
| Phase 6 (Activity) | `hexagonal-domain-developer` → `hexagonal-application-developer` → `hexagonal-persistence-adapter` → `hexagonal-web-adapter` |
| Phase 7 (Shifts current) | `hexagonal-web-adapter` + `hexagonal-persistence-adapter` |

---

## Implementation order

1. `inventory/domain/model/InventoryItemStatus.java` (1.1)
2. `inventory/domain/model/InventoryItem.java` (1.2)
3. `inventory/domain/model/AvailableInventoryResult.java` (1.3)
4. `inventory/domain/exception/InventoryNotAvailableException.java` (1.4)
5. `inventory/domain/port/in/GetAvailableInventoryUseCase.java` (1.5)
6. `inventory/domain/port/out/InventoryItemCreationPort.java` (1.6)
7. `inventory/domain/port/out/InventoryItemUpdatePort.java` (1.7)
8. `inventory/domain/port/out/InventoryItemRepositoryPort.java` (1.8)
9. `inventory/adapter/out/persistence/InventoryItemJpaEntity.java` (1.9)
10. `inventory/adapter/out/persistence/InventoryItemSpringRepository.java` (1.10)
11. `inventory/adapter/out/persistence/InventoryPersistenceAdapter.java` (1.11)
12. `inventory/application/usecase/GetAvailableInventoryService.java` (1.12)
13. `inventory/adapter/in/web/dto/AvailableInventoryResponse.java` (1.13)
14. `inventory/adapter/in/web/InventoryRestController.java` (1.14)
15. `reception/domain/model/ReceptionStatus.java` (2.1)
16. `reception/domain/model/Reception.java` (2.2)
17. `reception/domain/exception/ReceptionNotFoundException.java` (2.3)
18. `reception/domain/port/in/RegisterReceptionCommand.java` (2.4)
19. `reception/domain/port/in/ReceptionResult.java` (2.5)
20. `reception/domain/port/in/RegisterReceptionUseCase.java` (2.6)
21. `reception/domain/port/in/GetReceptionUseCase.java` (2.7)
22. `reception/domain/port/out/ReceptionRepositoryPort.java` (2.8)
23. `reception/adapter/out/persistence/ReceptionJpaEntity.java` (2.9)
24. `reception/adapter/out/persistence/ReceptionSpringRepository.java` (2.10)
25. `reception/adapter/out/persistence/ReceptionPersistenceAdapter.java` (2.11)
26. `reception/application/usecase/RegisterReceptionService.java` (2.12)
27. `reception/application/usecase/GetReceptionService.java` (2.13)
28. `reception/adapter/in/web/dto/ReceptionRequest.java` (2.14)
29. `reception/adapter/in/web/dto/ReceptionResponse.java` (2.15)
30. `reception/adapter/in/web/ReceptionRestController.java` (2.16)
31. `cutting/domain/exception/CuttingQuantityInvariantException.java` (3.1)
32. `cutting/domain/exception/CuttingNotAvailableException.java` (3.2)
33. `cutting/domain/model/CuttingRecord.java` (3.3)
34. `cutting/domain/model/AvailableCuttingResult.java` (3.4)
35. `cutting/domain/port/in/RegisterCuttingCommand.java` (3.5)
36. `cutting/domain/port/in/CuttingResult.java` (3.6)
37. `cutting/domain/port/in/RegisterCuttingUseCase.java` (3.7)
38. `cutting/domain/port/in/GetCuttingUseCase.java` (3.8)
39. `cutting/domain/port/out/CuttingRepositoryPort.java` (3.9)
40. `cutting/adapter/out/persistence/CuttingRecordJpaEntity.java` (3.10)
41. `cutting/adapter/out/persistence/CuttingSpringRepository.java` (3.11)
42. `cutting/adapter/out/persistence/CuttingPersistenceAdapter.java` (3.12)
43. `cutting/application/usecase/RegisterCuttingService.java` (3.13)
44. `cutting/application/usecase/GetCuttingService.java` (3.14)
45. `cutting/adapter/in/web/dto/CuttingRequest.java` (3.15)
46. `cutting/adapter/in/web/dto/CuttingResponse.java` (3.16)
47. `cutting/adapter/in/web/dto/AvailableCuttingResponse.java` (3.17)
48. `cutting/adapter/in/web/CuttingRestController.java` (3.18)
49. `scrap/domain/model/ScrapRecord.java` (4.1)
50. `scrap/domain/exception/ScrapNotFoundException.java` (4.2)
51. `scrap/domain/port/in/RegisterScrapCommand.java` (4.3)
52. `scrap/domain/port/in/ScrapResult.java` (4.4)
53. `scrap/domain/port/in/RegisterScrapUseCase.java` (4.5)
54. `scrap/domain/port/in/GetScrapUseCase.java` (4.6)
55. `scrap/domain/port/out/ScrapRepositoryPort.java` (4.7)
56. `scrap/adapter/out/persistence/ScrapRecordJpaEntity.java` (4.8)
57. `scrap/adapter/out/persistence/ScrapSpringRepository.java` (4.9)
58. `scrap/adapter/out/persistence/ScrapPersistenceAdapter.java` (4.10)
59. `scrap/application/usecase/RegisterScrapService.java` (4.11)
60. `scrap/application/usecase/GetScrapService.java` (4.12)
61. `scrap/adapter/in/web/dto/ScrapRequest.java` (4.13)
62. `scrap/adapter/in/web/dto/ScrapResponse.java` (4.14)
63. `scrap/adapter/in/web/ScrapRestController.java` (4.15)
64. `molding/domain/model/MoldingOutput.java` (5.1)
65. `molding/domain/exception/MoldingOutputNotFoundException.java` (5.2)
66. `molding/domain/port/in/RegisterMoldingOutputCommand.java` (5.3)
67. `molding/domain/port/in/MoldingOutputResult.java` (5.4)
68. `molding/domain/port/in/RegisterMoldingOutputUseCase.java` (5.5)
69. `molding/domain/port/in/GetMoldingOutputUseCase.java` (5.6)
70. `molding/domain/port/out/MoldingOutputRepositoryPort.java` (5.7)
71. `molding/adapter/out/persistence/MoldingOutputJpaEntity.java` (5.8)
72. `molding/adapter/out/persistence/MoldingOutputSpringRepository.java` (5.9)
73. `molding/adapter/out/persistence/MoldingOutputPersistenceAdapter.java` (5.10)
74. `molding/application/usecase/RegisterMoldingOutputService.java` (5.11)
75. `molding/application/usecase/GetMoldingOutputService.java` (5.12)
76. `molding/adapter/in/web/dto/MoldingOutputRequest.java` (5.13)
77. `molding/adapter/in/web/dto/MoldingOutputResponse.java` (5.14)
78. `molding/adapter/in/web/MoldingOutputRestController.java` (5.15)
79. `activity/domain/model/ActivityAction.java` (6.1)
80. `activity/domain/model/ActivityRawEntry.java` (6.2)
81. `activity/domain/model/ActivityResult.java` (6.3)
82. `activity/domain/port/in/GetActivityUseCase.java` (6.4)
83. `activity/domain/port/out/ActivityQueryPort.java` (6.5)
84. `activity/application/usecase/GetActivityService.java` (6.6)
85. `activity/adapter/out/persistence/ActivityPersistenceAdapter.java` (6.7)
86. `activity/adapter/in/web/dto/ActivityResponse.java` (6.8)
87. `activity/adapter/in/web/ActivityRestController.java` (6.9)
88. Modify `shifts/domain/port/in/GetShiftUseCase.java` — add `findCurrent()` (7.1)
89. Modify `shifts/domain/port/out/ShiftRepositoryPort.java` — add `findCurrentByTime(LocalTime)` (7.2)
90. Modify `shifts/adapter/out/persistence/SpringDataShiftRepository.java` — add JPQL query (7.4)
91. Modify `shifts/adapter/out/persistence/ShiftPersistenceAdapter.java` — implement delegate (7.3)
92. Modify `shifts/application/usecase/GetShiftService.java` — implement `findCurrent()` (7.5)
93. Modify `shifts/adapter/in/web/ShiftRestController.java` — add `GET /api/shifts/current` (7.6)

---

## Critical files

| File | Action |
|---|---|
| `inventory/domain/port/out/InventoryItemCreationPort.java` | Create — reception depends on it |
| `inventory/domain/port/out/InventoryItemUpdatePort.java` | Create — cutting depends on it |
| `inventory/adapter/out/persistence/InventoryPersistenceAdapter.java` | Create — implements both ports above |
| `reception/application/usecase/RegisterReceptionService.java` | Create — cross-module: injects `InventoryItemCreationPort` |
| `cutting/application/usecase/RegisterCuttingService.java` | Create — cross-module: injects `InventoryItemUpdatePort`, wraps `CuttingQuantities` |
| `cutting/domain/model/CuttingQuantities.java` | Exists — do NOT modify |
| `reception/application/usecase/GetReceptionService.java` | Create — cross-module: injects `ShiftRepositoryPort` |
| `molding/application/usecase/GetMoldingOutputService.java` | Create — cross-module: injects `ShiftRepositoryPort` |
| `activity/application/usecase/GetActivityService.java` | Create — cross-module: injects `ShiftRepositoryPort` + `ActivityQueryPort` |
| `activity/adapter/out/persistence/ActivityPersistenceAdapter.java` | Create — native SQL across all 4 tables |
| `shifts/adapter/in/web/ShiftRestController.java` | Modify — add `GET /api/shifts/current` |
| `shifts/domain/port/in/GetShiftUseCase.java` | Modify — add `findCurrent()` |
| `shifts/domain/port/out/ShiftRepositoryPort.java` | Modify — add `findCurrentByTime(LocalTime)` |

---

## Cross-module dependency map

```
reception/application → inventory/domain/port/out/InventoryItemCreationPort
cutting/application   → inventory/domain/port/out/InventoryItemUpdatePort
reception/application → shifts/domain/port/out/ShiftRepositoryPort
molding/application   → shifts/domain/port/out/ShiftRepositoryPort
activity/application  → shifts/domain/port/out/ShiftRepositoryPort
activity/adapter/out  → native SQL (no entity imports needed)
```

All application → domain/port dependencies are allowed by ArchUnit rules (application may import domain from any module; it must not import adapters or Spring Data repositories directly).

---

## Verification

1. `cd tesla-api && .\gradlew.bat compileJava testClasses` — zero compilation errors
2. `.\gradlew.bat test --tests "*HexagonalArchitectureTest"` — zero boundary violations
3. `.\gradlew.bat test` — all tests pass
4. Manual: `POST /api/auth/login` → get token; `POST /api/receptions` → 201; `GET /api/inventory/available?containerCode=X` → 200 with inventoryItemId
5. Manual: `POST /api/cutting` with bad quantities (e.g. initial=10, good=5, scrap=3) → 422 with code `cutting.quantity-invariant`
6. Manual: `POST /api/cutting` with valid quantities → 201; `GET /api/cutting/available?containerCode=X` → 200
7. Manual: `POST /api/scrap` → 201; `POST /api/molding-outputs` → 201
8. Manual: `GET /api/activity/my?shiftId=1` → sorted list with formatted quantities
9. Manual: `GET /api/shifts/current` → 200 with current shift or 204 when none active
10. Manual: Try all `/my` and `/{id}` endpoints without Authorization header → 401
