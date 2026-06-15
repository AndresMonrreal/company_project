# Plan: Operational Modules — Reception, Cutting, Scrap & Molding

## Context

The manufacturing traceability flow (Reception → Inventory → Cutting → Scrap → Molding Output) has no backend endpoints. The V1 schema already defines all tables (`receptions`, `inventory_items`, `cutting_records`, `scrap_records`, `molding_outputs`). This plan implements the domain, application, persistence, and REST layers for all four operational modules, the cross-cutting `activity` aggregator endpoint, and wires `MyActivityPageComponent` to the real API.

## Package base

`com.example.company.<module>` under `tesla-api/src/main/java/`


## Phase 1 — Inventory Foundation

Inventory owns `InventoryItem`. It exposes no REST but provides two output ports consumed by `reception` and `cutting`.

### 1.1 `InventoryItemStatus` enum

**File:** `tesla-api/src/main/java/com/example/company/inventory/domain/model/InventoryItemStatus.java`

Declare enum with values `AVAILABLE` and `CUT`. Pure Java, no imports.

### 1.2 `InventoryItem` aggregate

**File:** `tesla-api/src/main/java/com/example/company/inventory/domain/model/InventoryItem.java`

Record or final class with fields: `id (Long)`, `receptionId (Long)`, `availableQuantity (int)`, `status (InventoryItemStatus)`. Static factory `create(Long receptionId, int availableQuantity)` sets `status = AVAILABLE`. Instance method `markAsCut(int goodQuantity)` returns a new `InventoryItem` with `availableQuantity = goodQuantity` and `status = CUT`. No Spring/JPA imports.

### 1.3 `InventoryItemNotFoundException`

**File:** `tesla-api/src/main/java/com/example/company/inventory/domain/exception/InventoryItemNotFoundException.java`

Extends `DomainException` (from `com.example.company.shared.domain.exception`). Constructor accepts `Long id`. `type()` returns `DomainErrorType.NOT_FOUND`. `errorCode()` returns `"inventory.not-found"`. Message: `"Inventory item not found: " + id`.

### 1.4 `InventoryItemCreationPort`

**File:** `tesla-api/src/main/java/com/example/company/inventory/domain/port/out/InventoryItemCreationPort.java`

Interface with single method: `InventoryItem create(Long receptionId, int availableQuantity)`. Called by `reception`'s application service.

### 1.5 `InventoryItemUpdatePort`

**File:** `tesla-api/src/main/java/com/example/company/inventory/domain/port/out/InventoryItemUpdatePort.java`

Interface with single method: `void updateToCut(Long inventoryItemId, int goodQuantity)`. Implementations must set `available_quantity = goodQuantity` and `status = 'CUT'` in `inventory_items`. Called by `cutting`'s application service.

### 1.6 `InventoryItemRepositoryPort`

**File:** `tesla-api/src/main/java/com/example/company/inventory/domain/port/out/InventoryItemRepositoryPort.java`

Interface: `InventoryItem save(InventoryItem item)`, `Optional<InventoryItem> findById(Long id)`.

### 1.7 `InventoryItemJpaEntity`

**File:** `tesla-api/src/main/java/com/example/company/inventory/adapter/out/persistence/InventoryItemJpaEntity.java`

`@Entity @Table(name = "inventory_items")`. Columns: `id (BIGSERIAL PK)`, `receptionId (Long, @Column("reception_id"))`, `availableQuantity (int)`, `status (String)`, `version (Long, @Version)`, `updatedAt (LocalDateTime)`. No JPA relationship to reception — use scalar `Long receptionId`.

### 1.8 `InventoryItemSpringRepository`

**File:** `tesla-api/src/main/java/com/example/company/inventory/adapter/out/persistence/InventoryItemSpringRepository.java`

Extends `JpaRepository<InventoryItemJpaEntity, Long>`. No custom methods needed beyond base CRUD.

### 1.9 `InventoryPersistenceMapper`

**File:** `tesla-api/src/main/java/com/example/company/inventory/adapter/out/persistence/InventoryPersistenceMapper.java`

`@Component`. Method `toDomain(InventoryItemJpaEntity)` returns `InventoryItem`. Method `toNewEntity(Long receptionId, int availableQuantity)` returns a new `InventoryItemJpaEntity` with `status = "AVAILABLE"`.

### 1.10 `InventoryPersistenceAdapter`

**File:** `tesla-api/src/main/java/com/example/company/inventory/adapter/out/persistence/InventoryPersistenceAdapter.java`

`@Repository`. Implements `InventoryItemCreationPort`, `InventoryItemUpdatePort`, `InventoryItemRepositoryPort`. `create()` delegates to `InventoryItemSpringRepository.save()`. `updateToCut()` loads entity by id, sets `availableQuantity = goodQuantity`, `status = "CUT"`, calls `save()`. Throws `InventoryItemNotFoundException` if entity is absent.

---

## Phase 2 — Reception Module

### 2.1 `ReceptionStatus` enum

**File:** `tesla-api/src/main/java/com/example/company/reception/domain/model/ReceptionStatus.java`

Enum with single value `RECEIVED`.

### 2.2 `Reception` aggregate

**File:** `tesla-api/src/main/java/com/example/company/reception/domain/model/Reception.java`

Record with fields: `id (Long)`, `containerId (Long)`, `profileId (Long)`, `operatorId (Long)`, `lot (String)`, `receivedQuantity (int)`, `status (ReceptionStatus)`, `receivedAt (LocalDateTime)`. Static factory `create(Long containerId, Long profileId, Long operatorId, String lot, int receivedQuantity)` — sets `status = RECEIVED`, `receivedAt = LocalDateTime.now()`, validates `receivedQuantity > 0` via `IllegalArgumentException`. No Spring/JPA imports.

### 2.3 `ReceptionNotFoundException`

**File:** `tesla-api/src/main/java/com/example/company/reception/domain/exception/ReceptionNotFoundException.java`

Extends `DomainException`. `type()` = `NOT_FOUND`. `errorCode()` = `"reception.not-found"`.

### 2.4 `RegisterReceptionCommand`

**File:** `tesla-api/src/main/java/com/example/company/reception/domain/port/in/RegisterReceptionCommand.java`

Record: `containerId (Long)`, `profileId (Long)`, `operatorId (Long)`, `lot (String)`, `receivedQuantity (int)`.

### 2.5 `ReceptionResult`

**File:** `tesla-api/src/main/java/com/example/company/reception/domain/port/in/ReceptionResult.java`

Record: `id (Long)`, `containerCode (String)`, `profileCode (String)`, `lot (String)`, `receivedQuantity (int)`, `status (String)`, `receivedAt (LocalDateTime)`.

### 2.6 `RegisterReceptionUseCase`

**File:** `tesla-api/src/main/java/com/example/company/reception/domain/port/in/RegisterReceptionUseCase.java`

Interface: `ReceptionResult register(RegisterReceptionCommand command)`.

### 2.7 `GetMyReceptionsUseCase`

**File:** `tesla-api/src/main/java/com/example/company/reception/domain/port/in/GetMyReceptionsUseCase.java`

Interface: `List<ReceptionResult> findByOperatorAndShift(Long operatorId, Long shiftId)`.

### 2.8 `ReceptionRepositoryPort`

**File:** `tesla-api/src/main/java/com/example/company/reception/domain/port/out/ReceptionRepositoryPort.java`

Interface:
- `ReceptionResult save(Reception reception)` — saves and returns a result that includes `containerCode` and `profileCode` resolved via a JPQL JOIN query on `containers.code` and `profiles.code`.
- `List<ReceptionResult> findByOperatorAndWindow(Long operatorId, LocalDateTime start, LocalDateTime end)`.

### 2.9 `ReceptionResultMapper`

**File:** `tesla-api/src/main/java/com/example/company/reception/application/mapper/ReceptionResultMapper.java`

Static utility that maps `ReceptionResult` to `ReceptionResponse` DTO (only needed if the result is also used as domain output; primarily the web mapper handles the DTO conversion from `ReceptionResult`).

### 2.10 `RegisterReceptionService`

**File:** `tesla-api/src/main/java/com/example/company/reception/application/usecase/RegisterReceptionService.java`

`@Service`. Implements `RegisterReceptionUseCase`. Injects `ReceptionRepositoryPort` and `InventoryItemCreationPort` (from `com.example.company.inventory.domain.port.out`). `@Transactional`. Steps: (1) create `Reception` domain object, (2) call `receptionRepository.save(reception)` → `ReceptionResult`, (3) call `inventoryItemCreationPort.create(savedReceptionId, receivedQuantity)`. Returns the `ReceptionResult` from step 2.

### 2.11 `GetMyReceptionsService`

**File:** `tesla-api/src/main/java/com/example/company/reception/application/usecase/GetMyReceptionsService.java`

`@Service`. Implements `GetMyReceptionsUseCase`. Injects `ReceptionRepositoryPort` and `ShiftRepositoryPort` (from `com.example.company.shifts.domain.port.out`). Looks up shift by `shiftId` → computes `windowStart = today + shift.startTime()`, `windowEnd` accounting for overnight (if `endTime < startTime`, `windowEnd = tomorrow + endTime`). Delegates to `receptionRepository.findByOperatorAndWindow(operatorId, windowStart, windowEnd)`.

### 2.12 `ReceptionJpaEntity`

**File:** `tesla-api/src/main/java/com/example/company/reception/adapter/out/persistence/ReceptionJpaEntity.java`

`@Entity @Table(name = "receptions")`. Columns: `id`, `containerId` (`@Column("container_id")`), `profileId` (`@Column("profile_id")`), `operatorId` (`@Column("operator_id")`), `lot`, `receivedQuantity` (`@Column("received_quantity")`), `status` (String), `receivedAt` (`@Column("received_at")`), `version` (`@Version`).

### 2.13 `SpringDataReceptionRepository`

**File:** `tesla-api/src/main/java/com/example/company/reception/adapter/out/persistence/SpringDataReceptionRepository.java`

Extends `JpaRepository<ReceptionJpaEntity, Long>`. Add `@Query` method:
```
findByOperatorIdAndReceivedAtBetween(Long operatorId, LocalDateTime start, LocalDateTime end)
```
And a separate `@Query` with `JOIN` to `containers c` and `profiles p` for fetching codes alongside the entity (needed when building `ReceptionResult`). Use a named JPQL projection or query returning a custom result set joining `containers.code` as `containerCode` and `profiles.code` as `profileCode`.

### 2.14 `ReceptionPersistenceMapper`

**File:** `tesla-api/src/main/java/com/example/company/reception/adapter/out/persistence/ReceptionPersistenceMapper.java`

`@Component`. Methods: `toDomain(ReceptionJpaEntity)` → `Reception`, `toNewEntity(Reception)` → `ReceptionJpaEntity`.

### 2.15 `ReceptionPersistenceAdapter`

**File:** `tesla-api/src/main/java/com/example/company/reception/adapter/out/persistence/ReceptionPersistenceAdapter.java`

`@Repository`. Implements `ReceptionRepositoryPort`. `save()`: (1) maps Reception → new JpaEntity, (2) calls `repository.save()`, (3) executes JOIN JPQL to fetch `containers.code` and `profiles.code` by `receptionId`, (4) builds and returns `ReceptionResult` including codes. `findByOperatorAndWindow()`: uses the time-range + JOIN query, maps results to `ReceptionResult`.

### 2.16 Request/response DTOs

**File:** `tesla-api/src/main/java/com/example/company/reception/adapter/in/web/dto/RegisterReceptionRequest.java`

Fields with validation: `containerId (@NotNull Long)`, `profileId (@NotNull Long)`, `lot (@NotBlank String)`, `receivedQuantity (@Positive int)`.

**File:** `tesla-api/src/main/java/com/example/company/reception/adapter/in/web/dto/ReceptionResponse.java`

Record: `id`, `containerCode`, `profileCode`, `lot`, `receivedQuantity`, `status`, `receivedAt`.

### 2.17 `ReceptionWebMapper`

**File:** `tesla-api/src/main/java/com/example/company/reception/adapter/in/web/ReceptionWebMapper.java`

`@Component`. Maps `RegisterReceptionRequest` → `RegisterReceptionCommand` (sets `operatorId` from caller-supplied principal). Maps `ReceptionResult` → `ReceptionResponse`.

### 2.18 `ReceptionRestController`

**File:** `tesla-api/src/main/java/com/example/company/reception/adapter/in/web/ReceptionRestController.java`

`@RestController @RequestMapping("/api/receptions")`. Injects `RegisterReceptionUseCase`, `GetMyReceptionsUseCase`, `ReceptionWebMapper`. Methods:
- `@PostMapping @ResponseStatus(CREATED)` — extracts `operatorId` from `Principal` (cast to `AuthenticatedUserPrincipal`, call `.userId()`); calls `registerReception.register(mapper.toCommand(request, principal.userId()))`.
- `@GetMapping("/my")` — accepts `@RequestParam Long shiftId`; calls `getMyReceptions.findByOperatorAndShift(principal.userId(), shiftId)`.

---

## Phase 3 — Cutting Extension

The `cutting` module already has `CuttingQuantities`. Extend it with the full operational module.

### 3.1 `CuttingQuantityInvariantException`

**File:** `tesla-api/src/main/java/com/example/company/cutting/domain/exception/CuttingQuantityInvariantException.java`

Extends `DomainException`. `type()` = `BUSINESS_RULE` (maps to 422). `errorCode()` = `"cutting.quantity-invariant"`. Message: `"initial_quantity must equal good_quantity + scrap_quantity"`.

### 3.2 Modify `CuttingQuantities`

**File:** `tesla-api/src/main/java/com/example/company/cutting/domain/model/CuttingQuantities.java` *(exists)*

In the compact constructor, replace the final `IllegalArgumentException` for the invariant check (`initialQuantity != goodQuantity + scrapQuantity`) with `throw new CuttingQuantityInvariantException()`. Keep the preceding `IllegalArgumentException` checks for non-positive `initialQuantity` and negative quantities — DTO validation handles those at the boundary, but the domain guard remains as a safeguard.

### 3.3 `CuttingRecord` aggregate

**File:** `tesla-api/src/main/java/com/example/company/cutting/domain/model/CuttingRecord.java`

Record with fields: `id (Long)`, `inventoryItemId (Long)`, `machineId (Long)`, `operatorId (Long)`, `shiftId (Long)`, `quantities (CuttingQuantities)`, `cutAt (LocalDateTime)`. Static factory `create(Long inventoryItemId, Long machineId, Long operatorId, Long shiftId, int initial, int good, int scrap)` — constructs `CuttingQuantities` (invariant enforced here), sets `cutAt = LocalDateTime.now()`. No Spring/JPA imports.

### 3.4 `CuttingRecordNotFoundException`

**File:** `tesla-api/src/main/java/com/example/company/cutting/domain/exception/CuttingRecordNotFoundException.java`

Extends `DomainException`. `type()` = `NOT_FOUND`. `errorCode()` = `"cutting.not-found"`.

### 3.5 `RegisterCutCommand`

**File:** `tesla-api/src/main/java/com/example/company/cutting/domain/port/in/RegisterCutCommand.java`

Record: `inventoryItemId (Long)`, `machineId (Long)`, `operatorId (Long)`, `shiftId (Long)`, `initialQuantity (int)`, `goodQuantity (int)`, `scrapQuantity (int)`.

### 3.6 `CuttingResult`

**File:** `tesla-api/src/main/java/com/example/company/cutting/domain/port/in/CuttingResult.java`

Record: `id (Long)`, `containerCode (String)`, `profileCode (String)`, `machineCode (String)`, `initialQuantity (int)`, `goodQuantity (int)`, `scrapQuantity (int)`, `cutAt (LocalDateTime)`.

### 3.7 `RegisterCutUseCase`

**File:** `tesla-api/src/main/java/com/example/company/cutting/domain/port/in/RegisterCutUseCase.java`

Interface: `CuttingResult register(RegisterCutCommand command)`.

### 3.8 `GetMyCuttingRecordsUseCase`

**File:** `tesla-api/src/main/java/com/example/company/cutting/domain/port/in/GetMyCuttingRecordsUseCase.java`

Interface: `List<CuttingResult> findByOperatorAndShift(Long operatorId, Long shiftId)`.

### 3.9 `CuttingRepositoryPort`

**File:** `tesla-api/src/main/java/com/example/company/cutting/domain/port/out/CuttingRepositoryPort.java`

Interface:
- `CuttingResult save(CuttingRecord record)` — saves and returns `CuttingResult` with `containerCode`, `profileCode`, `machineCode` resolved via JPQL JOIN (cutting_records → inventory_items → receptions → containers + profiles, and cutting_records → machines).
- `List<CuttingResult> findByOperatorAndWindow(Long operatorId, LocalDateTime start, LocalDateTime end)`.
- `boolean existsById(Long id)`.

### 3.10 `RegisterCutService`

**File:** `tesla-api/src/main/java/com/example/company/cutting/application/usecase/RegisterCutService.java`

`@Service`. Implements `RegisterCutUseCase`. Injects `CuttingRepositoryPort` and `InventoryItemUpdatePort` (from `com.example.company.inventory.domain.port.out`). `@Transactional`. Steps: (1) call `CuttingRecord.create(...)` — throws `CuttingQuantityInvariantException` if invariant violated, (2) call `cuttingRepository.save(record)` → `CuttingResult`, (3) call `inventoryItemUpdatePort.updateToCut(command.inventoryItemId(), command.goodQuantity())`. Returns `CuttingResult` from step 2.

### 3.11 `GetMyCuttingRecordsService`

**File:** `tesla-api/src/main/java/com/example/company/cutting/application/usecase/GetMyCuttingRecordsService.java`

`@Service`. Implements `GetMyCuttingRecordsUseCase`. Injects `CuttingRepositoryPort` and `ShiftRepositoryPort` (from `com.example.company.shifts.domain.port.out`). Computes shift time window (overnight-aware), delegates to `cuttingRepository.findByOperatorAndWindow(...)`.

### 3.12 `CuttingRecordJpaEntity`

**File:** `tesla-api/src/main/java/com/example/company/cutting/adapter/out/persistence/CuttingRecordJpaEntity.java`

`@Entity @Table(name = "cutting_records")`. Columns: `id`, `inventoryItemId` (`@Column("inventory_item_id")`), `machineId` (`@Column(name="machine_Id")` — preserve casing from V1 schema), `operatorId` (`@Column("operator_id")`), `shiftId` (`@Column("shift_id")`), `initialQuantity`, `goodQuantity`, `scrapQuantity`, `cutAt`, `version (@Version)`. All stored as scalar Longs/ints.

### 3.13 `CuttingSpringRepository`

**File:** `tesla-api/src/main/java/com/example/company/cutting/adapter/out/persistence/CuttingSpringRepository.java`

Extends `JpaRepository<CuttingRecordJpaEntity, Long>`. Add `@Query` method for operator+window filter; add `@Query` with multi-table JOIN to fetch `containers.code`, `profiles.code`, `machines.name` alongside cutting record data (needed for `CuttingResult`). Join path: `cutting_records cr → inventory_items ii ON cr.inventory_item_id = ii.id → receptions r ON ii.reception_id = r.id → containers c ON r.container_id = c.id → profiles p ON r.profile_id = p.id → machines m ON cr.machine_Id = m.id`.

### 3.14 `CuttingPersistenceMapper`

**File:** `tesla-api/src/main/java/com/example/company/cutting/adapter/out/persistence/CuttingPersistenceMapper.java`

`@Component`. Maps `CuttingRecord` → new `CuttingRecordJpaEntity` and `CuttingRecordJpaEntity` → `CuttingRecord`.

### 3.15 `CuttingPersistenceAdapter`

**File:** `tesla-api/src/main/java/com/example/company/cutting/adapter/out/persistence/CuttingPersistenceAdapter.java`

`@Repository`. Implements `CuttingRepositoryPort`. `save()`: saves entity, then executes JOIN query to build `CuttingResult` with codes. `findByOperatorAndWindow()`: uses JOIN query, maps results to `CuttingResult`. `existsById()`: delegates to Spring repo.

### 3.16 `RegisterCutRequest` DTO

**File:** `tesla-api/src/main/java/com/example/company/cutting/adapter/in/web/dto/RegisterCutRequest.java`

Fields: `inventoryItemId (@NotNull Long)`, `machineId (@NotNull Long)`, `shiftId (@NotNull Long)`, `initialQuantity (@Positive int)`, `goodQuantity (@PositiveOrZero int)`, `scrapQuantity (@PositiveOrZero int)`.

### 3.17 `CuttingResponse` DTO

**File:** `tesla-api/src/main/java/com/example/company/cutting/adapter/in/web/dto/CuttingResponse.java`

Record: `id`, `containerCode`, `profileCode`, `machineCode`, `initialQuantity`, `goodQuantity`, `scrapQuantity`, `cutAt`.

### 3.18 `CuttingWebMapper`

**File:** `tesla-api/src/main/java/com/example/company/cutting/adapter/in/web/CuttingWebMapper.java`

`@Component`. `toCommand(RegisterCutRequest, Long operatorId)` → `RegisterCutCommand`. `toResponse(CuttingResult)` → `CuttingResponse`.

### 3.19 `CuttingRestController`

**File:** `tesla-api/src/main/java/com/example/company/cutting/adapter/in/web/CuttingRestController.java`

`@RestController @RequestMapping("/api/cutting")`. Methods:
- `@PostMapping @ResponseStatus(CREATED)` — extracts `operatorId` from principal, calls `registerCut.register(mapper.toCommand(request, operatorId))`.
- `@GetMapping("/my")` — accepts `@RequestParam Long shiftId`.

---

## Phase 4 — Scrap Module

### 4.1 `ScrapRecord` aggregate

**File:** `tesla-api/src/main/java/com/example/company/scrap/domain/model/ScrapRecord.java`

Record with fields: `id (Long)`, `cuttingRecordId (Long)`, `quantity (int)`, `reason (String, nullable)`, `createdAt (LocalDateTime)`. Static factory `create(Long cuttingRecordId, int quantity, String reason)` — sets `createdAt = LocalDateTime.now()`.

### 4.2 `ScrapRecordNotFoundException`

**File:** `tesla-api/src/main/java/com/example/company/scrap/domain/exception/ScrapRecordNotFoundException.java`

Extends `DomainException`. `type()` = `NOT_FOUND`. `errorCode()` = `"scrap.not-found"`.

### 4.3 Input ports + command + result

**Files:** `tesla-api/src/main/java/com/example/company/scrap/domain/port/in/`

- `RegisterScrapCommand.java` — record: `cuttingRecordId (Long)`, `operatorId (Long)`, `quantity (int)`, `reason (String, nullable)`.
- `ScrapResult.java` — record: `id (Long)`, `cuttingRecordId (Long)`, `quantity (int)`, `reason (String)`, `createdAt (LocalDateTime)`.
- `RegisterScrapUseCase.java` — `ScrapResult register(RegisterScrapCommand command)`.
- `GetMyScrapUseCase.java` — `List<ScrapResult> findByOperatorAndShift(Long operatorId, Long shiftId)`.

### 4.4 `ScrapRepositoryPort`

**File:** `tesla-api/src/main/java/com/example/company/scrap/domain/port/out/ScrapRepositoryPort.java`

Interface:
- `ScrapRecord save(ScrapRecord record)`.
- `List<ScrapResult> findByOperatorAndWindow(Long operatorId, LocalDateTime start, LocalDateTime end)` — filters scrap records by joining to `cutting_records.operator_id`.

### 4.5 `CuttingRecordExistsPort`

**File:** `tesla-api/src/main/java/com/example/company/scrap/domain/port/out/CuttingRecordExistsPort.java`

Interface: `boolean existsById(Long cuttingRecordId)`. Allows `RegisterScrapService` to validate the cutting record exists before saving.

### 4.6 `RegisterScrapService`

**File:** `tesla-api/src/main/java/com/example/company/scrap/application/usecase/RegisterScrapService.java`

`@Service`. Implements `RegisterScrapUseCase`. Injects `ScrapRepositoryPort` and `CuttingRecordExistsPort`. `@Transactional`. Throws `ScrapRecordNotFoundException` ("cutting.not-found" variant — or reuse CuttingRecordNotFoundException from cutting module if visible) if cutting record not found; otherwise saves scrap record and maps to `ScrapResult`.

### 4.7 `GetMyScrapService`

**File:** `tesla-api/src/main/java/com/example/company/scrap/application/usecase/GetMyScrapService.java`

`@Service`. Injects `ScrapRepositoryPort` and `ShiftRepositoryPort`. Computes window, delegates to repository.

### 4.8 Persistence layer

**Files:** `tesla-api/src/main/java/com/example/company/scrap/adapter/out/persistence/`

- `ScrapRecordJpaEntity.java` — `@Entity @Table("scrap_records")`. Columns: `id`, `cuttingRecordId` (`@Column("cutting_record_id")`), `quantity`, `reason`, `createdAt`. No `version` column (absent in V1 schema).
- `ScrapSpringRepository.java` — Extends `JpaRepository<ScrapRecordJpaEntity, Long>`. Add `@Query` for operator+window filter via `JOIN cutting_records cr ON scrap_records.cutting_record_id = cr.id WHERE cr.operator_id = :operatorId AND scrap_records.created_at BETWEEN :start AND :end`.
- `ScrapPersistenceMapper.java` — maps entity ↔ domain.
- `ScrapPersistenceAdapter.java` — `@Repository`. Implements `ScrapRepositoryPort` and `CuttingRecordExistsPort`. `existsById()` uses `cuttingSpringRepository.existsById()` — inject `CuttingSpringRepository` directly (both in the same adapter layer; no ArchUnit violation). Alternatively, inject `CuttingRecordJpaEntity` repository.

### 4.9 REST layer

**Files:** `tesla-api/src/main/java/com/example/company/scrap/adapter/in/web/`

- `RegisterScrapRequest.java` — DTO: `cuttingRecordId (@NotNull Long)`, `quantity (@Positive int)`, `reason (String, optional)`.
- `ScrapResponse.java` — DTO: `id`, `cuttingRecordId`, `quantity`, `reason`, `createdAt`.
- `ScrapWebMapper.java` — `@Component`. `toCommand(request, operatorId)` → `RegisterScrapCommand`. `toResponse(ScrapResult)` → `ScrapResponse`.
- `ScrapRestController.java` — `@RestController @RequestMapping("/api/scrap")`. `@PostMapping @ResponseStatus(CREATED)`. `@GetMapping("/my")` with `@RequestParam Long shiftId`.

---

## Phase 5 — Molding Module

### 5.1 `MoldingOutput` aggregate

**File:** `tesla-api/src/main/java/com/example/company/molding/domain/model/MoldingOutput.java`

Record: `id (Long)`, `cuttingRecordId (Long)`, `operatorId (Long)`, `quantitySent (int)`, `sentAt (LocalDateTime)`. Factory `create(Long cuttingRecordId, Long operatorId, int quantitySent)`.

### 5.2 `MoldingOutputNotFoundException`

**File:** `tesla-api/src/main/java/com/example/company/molding/domain/exception/MoldingOutputNotFoundException.java`

`type()` = `NOT_FOUND`. `errorCode()` = `"molding.not-found"`.

### 5.3 Input ports + command + result

**Files:** `tesla-api/src/main/java/com/example/company/molding/domain/port/in/`

- `RegisterMoldingOutputCommand.java` — record: `cuttingRecordId (Long)`, `operatorId (Long)`, `quantitySent (int)`.
- `MoldingOutputResult.java` — record: `id (Long)`, `cuttingRecordId (Long)`, `quantitySent (int)`, `sentAt (LocalDateTime)`.
- `RegisterMoldingOutputUseCase.java` — `MoldingOutputResult register(RegisterMoldingOutputCommand command)`.
- `GetMyMoldingOutputsUseCase.java` — `List<MoldingOutputResult> findByOperatorAndShift(Long operatorId, Long shiftId)`.

### 5.4 `MoldingOutputRepositoryPort`

**File:** `tesla-api/src/main/java/com/example/company/molding/domain/port/out/MoldingOutputRepositoryPort.java`

Interface:
- `MoldingOutput save(MoldingOutput output)`.
- `List<MoldingOutputResult> findByOperatorAndWindow(Long operatorId, LocalDateTime start, LocalDateTime end)`.

### 5.5 `CuttingRecordExistsPort` for molding

**File:** `tesla-api/src/main/java/com/example/company/molding/domain/port/out/CuttingRecordExistsPort.java`

Identical interface to the scrap one: `boolean existsById(Long cuttingRecordId)`. Each module defines its own port interface — same contract, independent definition.

### 5.6 `RegisterMoldingOutputService`

**File:** `tesla-api/src/main/java/com/example/company/molding/application/usecase/RegisterMoldingOutputService.java`

`@Service`. Implements `RegisterMoldingOutputUseCase`. Injects `MoldingOutputRepositoryPort` and `CuttingRecordExistsPort`. `@Transactional`. Validates cutting record exists, saves, maps to result.

### 5.7 `GetMyMoldingOutputsService`

**File:** `tesla-api/src/main/java/com/example/company/molding/application/usecase/GetMyMoldingOutputsService.java`

`@Service`. Injects `MoldingOutputRepositoryPort` and `ShiftRepositoryPort`. Computes window, delegates.

### 5.8 Persistence layer

**Files:** `tesla-api/src/main/java/com/example/company/molding/adapter/out/persistence/`

- `MoldingOutputJpaEntity.java` — `@Entity @Table("molding_outputs")`. Columns: `id`, `cuttingRecordId` (`@Column("cutting_record_id")`), `operatorId` (`@Column("operator_id")`), `quantitySent` (`@Column("quantity_sent")`), `sentAt`. No `version` column (absent in V1 schema).
- `MoldingOutputSpringRepository.java` — `findByOperatorIdAndSentAtBetween(@Query JOIN method for results)`.
- `MoldingOutputPersistenceMapper.java`.
- `MoldingOutputPersistenceAdapter.java` — `@Repository`. Implements `MoldingOutputRepositoryPort` and `CuttingRecordExistsPort`. `existsById()` queries `CuttingSpringRepository`.

### 5.9 REST layer

**Files:** `tesla-api/src/main/java/com/example/company/molding/adapter/in/web/`

- `RegisterMoldingOutputRequest.java` — `cuttingRecordId (@NotNull Long)`, `quantitySent (@Positive int)`.
- `MoldingOutputResponse.java` — `id`, `cuttingRecordId`, `quantitySent`, `sentAt`.
- `MoldingOutputWebMapper.java` — `@Component`.
- `MoldingOutputRestController.java` — `@RestController @RequestMapping("/api/molding-outputs")`. `@PostMapping @ResponseStatus(CREATED)`. `@GetMapping("/my")` with `@RequestParam Long shiftId`.

---

## Phase 6 — Activity Endpoint

### 6.1 `ActivityItem` domain record

**File:** `tesla-api/src/main/java/com/example/company/activity/domain/model/ActivityItem.java`

Record: `time (String, HH:mm)`, `containerCode (String)`, `profileCode (String)`, `action (String)`, `quantities (String)`, `status (String, nullable)`, `timestamp (LocalDateTime)` — for sorting; excluded from API response.

### 6.2 `GetMyActivityUseCase`

**File:** `tesla-api/src/main/java/com/example/company/activity/domain/port/in/GetMyActivityUseCase.java`

Interface: `List<ActivityItem> getMyActivity(Long operatorId, Long shiftId)`.

### 6.3 `ActivityQueryPort`

**File:** `tesla-api/src/main/java/com/example/company/activity/domain/port/out/ActivityQueryPort.java`

Interface with four methods, each returning `List<ActivityItem>`:
- `findReceptions(Long operatorId, LocalDateTime start, LocalDateTime end)`
- `findCuttingRecords(Long operatorId, LocalDateTime start, LocalDateTime end)`
- `findScrapRecords(Long operatorId, LocalDateTime start, LocalDateTime end)`
- `findMoldingOutputs(Long operatorId, LocalDateTime start, LocalDateTime end)`

### 6.4 `ShiftWindowPort`

**File:** `tesla-api/src/main/java/com/example/company/activity/domain/port/out/ShiftWindowPort.java`

Interface: `Optional<ShiftWindow> findWindowByShiftId(Long shiftId)`. `ShiftWindow` is an inner record (or nested class) with `startTime (LocalTime)` and `endTime (LocalTime)`.

### 6.5 `GetMyActivityService`

**File:** `tesla-api/src/main/java/com/example/company/activity/application/usecase/GetMyActivityService.java`

`@Service`. Implements `GetMyActivityUseCase`. Injects `ActivityQueryPort` and `ShiftWindowPort`. Logic:
1. Load shift window; throw `ShiftNotFoundException` (from shifts module or a local exception) if absent.
2. Compute `windowStart = LocalDate.now().atTime(window.startTime())`.
3. Compute `windowEnd`: if `endTime > startTime`, `= LocalDate.now().atTime(endTime)`; if `endTime <= startTime` (overnight), `= LocalDate.now().plusDays(1).atTime(endTime)`.
4. Call all four `ActivityQueryPort` methods in sequence (no parallelism needed for V1).
5. Merge lists, sort by `ActivityItem.timestamp()` ascending, return sorted list.

### 6.6 `ActivityPersistenceAdapter`

**File:** `tesla-api/src/main/java/com/example/company/activity/adapter/out/persistence/ActivityPersistenceAdapter.java`

`@Repository`. Implements `ActivityQueryPort`. Four `@PersistenceContext EntityManager` (or `@Autowired` Spring repo injections) JPQL queries:

**Receptions query** — `SELECT r.received_at, c.code, p.code, r.received_quantity, r.status FROM receptions r JOIN containers c ON r.container_id = c.id JOIN profiles p ON r.profile_id = p.id WHERE r.operator_id = :operatorId AND r.received_at BETWEEN :start AND :end`. Maps to `ActivityItem` with `action = "RECEPTION"`, `quantities = "${received_quantity} pcs"`, `status = "RECEIVED"`.

**Cutting records query** — joins `cutting_records → inventory_items → receptions → containers + profiles + machines`. Maps to `ActivityItem` with `action = "CUT"`, `quantities = "${initial} → ${good} good · ${scrap} scrap"`, `status = "CUT"`.

**Scrap records query** — joins `scrap_records → cutting_records → inventory_items → receptions → containers + profiles`. Filters by `cutting_records.operator_id`. Maps to `ActivityItem` with `action = "SCRAP"`, `quantities = "${quantity} pcs"`, `status = null`.

**Molding outputs query** — joins `molding_outputs → cutting_records → inventory_items → receptions → containers + profiles`. Filters by `molding_outputs.operator_id`. Maps to `ActivityItem` with `action = "MOLDING_OUTPUT"`, `quantities = "${quantitySent} pcs"`, `status = null`.

### 6.7 `ShiftWindowAdapter`

**File:** `tesla-api/src/main/java/com/example/company/activity/adapter/out/persistence/ShiftWindowAdapter.java`

`@Repository`. Implements `ShiftWindowPort`. Injects `ShiftSpringRepository` (from `shifts` adapter — note: adapter.out → adapter.out cross-module, allowed by ArchUnit). Alternatively, use a standalone JPQL query for `SELECT s.start_time, s.end_time FROM shifts WHERE s.id = :shiftId`.

### 6.8 `ActivityResponse` DTO + `ActivityRestController`

**Files:** `tesla-api/src/main/java/com/example/company/activity/adapter/in/web/`

- `ActivityResponse.java` — record: `time (String)`, `containerCode (String)`, `profileCode (String)`, `action (String)`, `quantities (String)`, `status (String)`. No `timestamp` field.
- `ActivityRestController.java` — `@RestController @RequestMapping("/api/activity")`. `@GetMapping("/my")` with `@RequestParam Long shiftId`. Extracts `operatorId` from principal. Maps `ActivityItem` list → `ActivityResponse` list (drop `timestamp`, keep other fields). Returns `ResponseEntity<List<ActivityResponse>>`.

---

## Phase 7 — Security Configuration

### 7.1 Update `SecurityConfiguration`

**File:** `tesla-api/src/main/java/com/example/company/security/config/SecurityConfiguration.java` *(exists)*

In `authorizeHttpRequests`, insert the new rules BEFORE the final `.requestMatchers("/api/**").denyAll()` catch-all. The rules block at lines 64-65 currently. Add after line 64:

```
.requestMatchers(HttpMethod.POST, "/api/receptions").hasAnyRole("OPERADOR", "SUPERVISOR")
.requestMatchers(HttpMethod.GET, "/api/receptions/my").authenticated()
.requestMatchers(HttpMethod.POST, "/api/cutting").hasAnyRole("OPERADOR", "SUPERVISOR")
.requestMatchers(HttpMethod.GET, "/api/cutting/my").authenticated()
.requestMatchers(HttpMethod.POST, "/api/scrap").hasAnyRole("OPERADOR", "SUPERVISOR")
.requestMatchers(HttpMethod.GET, "/api/scrap/my").authenticated()
.requestMatchers(HttpMethod.POST, "/api/molding-outputs").hasAnyRole("OPERADOR", "SUPERVISOR")
.requestMatchers(HttpMethod.GET, "/api/molding-outputs/my").authenticated()
.requestMatchers(HttpMethod.GET, "/api/activity/my").authenticated()
```

After this phase: run `hexagonal-security-reviewer` before proceeding to frontend work.

---

## Phase 8 — Angular Frontend Wiring

### 8.1 Update `ActivityRecord` model

**File:** `tesla-web-app/src/app/features/my-activity/models/activity-record.model.ts` *(exists)*

Rename field `container` → `containerCode` and `profile` → `profileCode` in the `ActivityRecord` interface to match backend response shape (`containerCode`, `profileCode`). Add `status: ActivityStatus | null` (currently typed as `ActivityStatus`, make it nullable to handle SCRAP and MOLDING_OUTPUT). Update `STATUS_BADGE_COLOR` guard logic to handle null.

### 8.2 `ActivityApiClient`

**File:** `tesla-web-app/src/app/features/my-activity/data-access/activity-api.client.ts` *(new)*

`@Injectable({ providedIn: 'root' })`. Injects `HttpClient` via `inject(HttpClient)` and `API_BASE_URL` via `inject(API_BASE_URL)`. Single method: `getMyActivity(shiftId: number): Observable<ActivityRecord[]>` — calls `GET ${apiBaseUrl}/api/activity/my?shiftId=${shiftId}`.

### 8.3 `ActivityService`

**File:** `tesla-web-app/src/app/features/my-activity/services/activity.service.ts` *(new)*

`@Injectable({ providedIn: 'root' })`. Injects `ActivityApiClient`. Method: `loadMyActivity(shiftId: number): Observable<ActivityRecord[]>` — returns `activityApiClient.getMyActivity(shiftId)` (passthrough; no error transform in V1). `ActivityFilterService` remains a separate scoped service.

### 8.4 Update `MyActivityPageComponent`

**File:** `tesla-web-app/src/app/features/my-activity/pages/my-activity.page.ts` *(exists)*

Add imports: `ActivityService`, `OnInit`. Inject `ActivityService` via `inject(ActivityService)`. Add signals: `isLoading = signal(false)`, `error = signal<string | null>(null)`, `apiData = signal<ActivityRecord[]>([])`. Update `source` computed:
```
source = computed(() => this.useMock()
  ? (this.populated() ? ACTIVITY_MOCK_DATA : ACTIVITY_EMPTY_DATA)
  : this.apiData()
);
```
Rename `populated` signal to `useMock` (or add a separate `useMock` signal for the dev toggle). Implement `ngOnInit`: set `isLoading(true)`, call `activityService.loadMyActivity(1)` (default shiftId = 1 until auth session exposes shift), subscribe: on next set `apiData(records)`, on error set `error('Failed to load activity')`, on finalize set `isLoading(false)`. In the template, add a loading spinner block (`@if (isLoading()) { ... }`) and an error block (`@if (error()) { ... }`) before the summary cards.

### 8.5 Update `ActivityTableComponent` field references

**File:** `tesla-web-app/src/app/features/my-activity/components/activity-table.component.ts` *(exists)*

Wherever the template or component code references `record.container`, change to `record.containerCode`. Wherever it references `record.profile`, change to `record.profileCode`. Update null-safety guard for `record.status` where used in badge rendering.

---

## Agent routing

| Phase | Agent |
|---|---|
| Phase 1 — Domain + Persistence | `hexagonal-domain-developer` then `hexagonal-persistence-adapter` |
| Phase 2 — Domain + App | `hexagonal-domain-developer` then `hexagonal-application-developer` |
| Phase 2 — Persistence + REST | `hexagonal-persistence-adapter` then `hexagonal-web-adapter` |
| Phase 3 — Cutting Extension | Same routing as Phase 2 |
| Phase 4 — Scrap | Same routing as Phase 2 |
| Phase 5 — Molding | Same routing as Phase 2 |
| Phase 6 — Activity | `hexagonal-domain-developer` → `hexagonal-application-developer` → `hexagonal-persistence-adapter` → `hexagonal-web-adapter` |
| Phase 7 — Security | `hexagonal-security-reviewer` |
| Phase 8 — Angular | `frontend-developer` |

---

## Implementation order

1. Phase 1.1 — InventoryItemStatus enum
2. Phase 1.2 — InventoryItem aggregate
3. Phase 1.3 — InventoryItemNotFoundException
4. Phase 1.4 — InventoryItemCreationPort
5. Phase 1.5 — InventoryItemUpdatePort
6. Phase 1.6 — InventoryItemRepositoryPort
7. Phase 1.7 — InventoryItemJpaEntity
8. Phase 1.8 — InventoryItemSpringRepository
9. Phase 1.9 — InventoryPersistenceMapper
10. Phase 1.10 — InventoryPersistenceAdapter
11. Phase 2.1 — ReceptionStatus enum
12. Phase 2.2 — Reception aggregate
13. Phase 2.3 — ReceptionNotFoundException
14. Phase 2.4 — RegisterReceptionCommand
15. Phase 2.5 — ReceptionResult
16. Phase 2.6 — RegisterReceptionUseCase
17. Phase 2.7 — GetMyReceptionsUseCase
18. Phase 2.8 — ReceptionRepositoryPort
19. Phase 2.9 — ReceptionResultMapper
20. Phase 2.10 — RegisterReceptionService
21. Phase 2.11 — GetMyReceptionsService
22. Phase 2.12 — ReceptionJpaEntity
23. Phase 2.13 — SpringDataReceptionRepository
24. Phase 2.14 — ReceptionPersistenceMapper
25. Phase 2.15 — ReceptionPersistenceAdapter
26. Phase 2.16 — Request/response DTOs (reception)
27. Phase 2.17 — ReceptionWebMapper
28. Phase 2.18 — ReceptionRestController
29. Phase 3.1 — CuttingQuantityInvariantException
30. Phase 3.2 — Modify CuttingQuantities (invariant throw)
31. Phase 3.3 — CuttingRecord aggregate
32. Phase 3.4 — CuttingRecordNotFoundException
33. Phase 3.5 — RegisterCutCommand
34. Phase 3.6 — CuttingResult
35. Phase 3.7 — RegisterCutUseCase
36. Phase 3.8 — GetMyCuttingRecordsUseCase
37. Phase 3.9 — CuttingRepositoryPort
38. Phase 3.10 — RegisterCutService
39. Phase 3.11 — GetMyCuttingRecordsService
40. Phase 3.12 — CuttingRecordJpaEntity
41. Phase 3.13 — CuttingSpringRepository
42. Phase 3.14 — CuttingPersistenceMapper
43. Phase 3.15 — CuttingPersistenceAdapter
44. Phase 3.16 — RegisterCutRequest DTO
45. Phase 3.17 — CuttingResponse DTO
46. Phase 3.18 — CuttingWebMapper
47. Phase 3.19 — CuttingRestController
48. Phase 4.1 — ScrapRecord aggregate
49. Phase 4.2 — ScrapRecordNotFoundException
50. Phase 4.3 — Scrap ports + command + result
51. Phase 4.4 — ScrapRepositoryPort
52. Phase 4.5 — CuttingRecordExistsPort (scrap)
53. Phase 4.6 — RegisterScrapService
54. Phase 4.7 — GetMyScrapService
55. Phase 4.8 — Scrap persistence layer
56. Phase 4.9 — Scrap REST layer
57. Phase 5.1 — MoldingOutput aggregate
58. Phase 5.2 — MoldingOutputNotFoundException
59. Phase 5.3 — Molding ports + command + result
60. Phase 5.4 — MoldingOutputRepositoryPort
61. Phase 5.5 — CuttingRecordExistsPort (molding)
62. Phase 5.6 — RegisterMoldingOutputService
63. Phase 5.7 — GetMyMoldingOutputsService
64. Phase 5.8 — Molding persistence layer
65. Phase 5.9 — Molding REST layer
66. Phase 6.1 — ActivityItem domain record
67. Phase 6.2 — GetMyActivityUseCase
68. Phase 6.3 — ActivityQueryPort
69. Phase 6.4 — ShiftWindowPort
70. Phase 6.5 — GetMyActivityService
71. Phase 6.6 — ActivityPersistenceAdapter (four JPQL queries)
72. Phase 6.7 — ShiftWindowAdapter
73. Phase 6.8 — ActivityResponse DTO + ActivityRestController
74. Phase 7.1 — SecurityConfiguration update
75. Phase 8.1 — Update ActivityRecord model (field rename + nullable status)
76. Phase 8.2 — ActivityApiClient
77. Phase 8.3 — ActivityService
78. Phase 8.4 — Update MyActivityPageComponent (isLoading, error, real API)
79. Phase 8.5 — Update ActivityTableComponent (field references)

---

## Critical files

| File | Action |
|---|---|
| `tesla-api/src/main/java/com/example/company/cutting/domain/model/CuttingQuantities.java` | Modify — change invariant throw to `CuttingQuantityInvariantException` |
| `tesla-api/src/main/java/com/example/company/security/config/SecurityConfiguration.java` | Modify — add 9 new endpoint authorization rules |
| `tesla-web-app/src/app/features/my-activity/models/activity-record.model.ts` | Modify — rename fields, make status nullable |
| `tesla-web-app/src/app/features/my-activity/pages/my-activity.page.ts` | Modify — add real API call, loading/error signals |
| `tesla-web-app/src/app/features/my-activity/components/activity-table.component.ts` | Modify — update field references |
| All `inventory/**` files | New |
| All `reception/**` files | New |
| All `cutting/domain/exception/`, `cutting/domain/model/CuttingRecord*`, `cutting/domain/port/`, `cutting/application/`, `cutting/adapter/` files | New (except CuttingQuantities) |
| All `scrap/**` files | New |
| All `molding/**` files | New |
| All `activity/**` files | New |
| `tesla-web-app/src/app/features/my-activity/data-access/activity-api.client.ts` | New |
| `tesla-web-app/src/app/features/my-activity/services/activity.service.ts` | New |

---

## Verification

1. `cd tesla-api && .\gradlew.bat compileJava` — must compile clean.
2. `.\gradlew.bat test --tests "*CuttingQuantitiesTest"` — existing quantity invariant tests still pass.
3. `.\gradlew.bat test --tests "*HexagonalArchitectureTest"` — all three ArchUnit rules pass (domain no Spring/JPA/adapters; application no adapters; inbound no outbound).
4. `.\gradlew.bat test` — full test suite green.
5. `.\gradlew.bat bootRun` — application starts, Flyway validates existing schema without migration errors.
6. `curl -X POST /api/auth/login` to get JWT. Then:
   - `POST /api/receptions` with valid payload → 201 with `containerCode` and `profileCode`.
   - `POST /api/receptions` no auth → 401.
   - `POST /api/receptions` with CONSULTA role → 403.
   - `POST /api/cutting` with `initialQuantity=10, goodQuantity=6, scrapQuantity=3` → 422 with `"code": "cutting.quantity-invariant"`.
   - `POST /api/cutting` with `initialQuantity=10, goodQuantity=8, scrapQuantity=2` → 201.
   - Verify `inventory_items` row has `status = 'CUT'` and `available_quantity = 8`.
   - `POST /api/scrap` → 201.
   - `POST /api/molding-outputs` → 201.
   - `GET /api/activity/my?shiftId=1` → 200, array sorted by time, all four actions present.
   - `GET /api/activity/my?shiftId=1` as different operator → returns only own records.
7. `cd tesla-web-app && ng build` — Angular compiles without TypeScript errors.
8. `ng serve` — open `/my-activity`: loading spinner appears, then real data from API renders; error state shown when API is down; dev toggle switches to mock data.
