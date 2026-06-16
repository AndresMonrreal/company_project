# Spec: Operational Modules — Reception, Cutting, Scrap, Molding

## Summary

Implement the five operational modules (`reception`, `inventory`, `cutting`, `scrap`, `molding`) and one cross-cutting read module (`activity`) that form the core traceability flow: Reception → Inventory → Cutting → Scrap + Molding Output. All tables already exist in V1.

## Problem

The system has catalog data (profiles, machines, shifts, containers) and auth, but no way to record the day-to-day operational movements that constitute traceability. Operators have no way to register a reception, cut rubber profiles, register scrap, or send to molding.

## Goals

- Register incoming rubber reception and auto-create an inventory item
- Register a cutting operation against an available inventory item (enforcing quantity invariant)
- Register scrap generated during cutting
- Register molding output from a cut batch
- Query available inventory or available cutting records by container barcode (for scanner-based UX)
- View a chronological unified activity list per operator per shift

## Non-Goals

- Editing or deleting operational records after creation
- Batch registration of multiple containers
- Offline/queue-based submission
- Reporting or aggregation beyond the `/activity/my` endpoint
- Any new Flyway migrations (schema is complete in V1)
- Frontend pages (Angular scope is separate)

## Users and Flows

**Primary actor:** OPERADOR (also ADMIN, SUPERVISOR)

**Reception flow:**
1. Operator POSTs to `/api/receptions` with containerId, profileId, lot, receivedQuantity
2. System sets operatorId from JWT (`principal.userId()`)
3. System saves reception (status RECEIVED) and auto-creates inventory item (status AVAILABLE, availableQuantity = receivedQuantity)
4. Returns 201 with ReceptionResponse

**Cutting flow:**
1. Operator POSTs to `/api/cutting` with inventoryItemId, machineId, shiftId, initialQuantity, goodQuantity, scrapQuantity
2. Domain enforces `initialQuantity = goodQuantity + scrapQuantity` via `CuttingQuantities` value object (422 on violation)
3. System updates inventory item status to CUT
4. Returns 201 with CuttingResponse

**Scrap flow:**
1. Operator POSTs to `/api/scrap` with cuttingRecordId, quantity, reason (optional)
2. Returns 201 with ScrapResponse

**Molding flow:**
1. Operator POSTs to `/api/molding-outputs` with cuttingRecordId, quantitySent
2. operatorId from JWT
3. Returns 201 with MoldingOutputResponse

**Scanner lookup flows:**
- GET `/api/inventory/available?containerCode={code}` → find AVAILABLE inventory item for barcode
- GET `/api/cutting/available?containerCode={code}` → find CUT cutting record for barcode

## Backend Scope

### Module: `inventory` (`com.example.company.inventory`)

Table: `inventory_items` (id, reception_id, available_quantity, status, version, updated_at)

**Domain:**
- `InventoryItem` record: id, receptionId, availableQuantity, status
- `InventoryItemStatus` enum: `AVAILABLE`, `CUT`
- `AvailableInventoryResult` record: inventoryItemId, containerCode, profileCode, lot, availableQuantity
- `InventoryNotAvailableException` → `DomainErrorType.NOT_FOUND`, code `inventory.not-available`

**Ports:**
- `InventoryItemCreationPort` (out) — `createInventoryItem(Long receptionId, int availableQuantity)` — called by reception use case
- `InventoryItemUpdatePort` (out) — `markAsCut(Long inventoryItemId)` — called by cutting use case
- `InventoryItemRepositoryPort` (out) — `findAvailableByContainerCode(String containerCode): Optional<AvailableInventoryResult>`
- `GetAvailableInventoryUseCase` (in) — `findAvailableByContainerCode(String containerCode): AvailableInventoryResult`

**Application:**
- `GetAvailableInventoryService` implements `GetAvailableInventoryUseCase`

**Persistence:**
- `InventoryItemJpaEntity` — maps `inventory_items`; scalar `receptionId` (no FK object)
- `InventoryItemSpringRepository` — Spring Data, custom JPQL for join to containers/receptions/profiles
- `InventoryPersistenceAdapter` — implements all three output ports

**REST:**
- `InventoryRestController` — `GET /api/inventory/available?containerCode={code}` → `AvailableInventoryResponse`

---

### Module: `reception` (`com.example.company.reception`)

Table: `receptions` (id, container_id, profile_id, operator_id, lot, received_quantity, status, received_at, version)

**Domain:**
- `Reception` record: id, containerId, profileId, operatorId, lot, receivedQuantity, status, receivedAt
- `ReceptionStatus` enum: `RECEIVED`
- `ReceptionResult` record: id, containerId, profileId, operatorId, lot, receivedQuantity, status, receivedAt
- `ReceptionNotFoundException` → `DomainErrorType.NOT_FOUND`, code `reception.not-found`

**Ports:**
- `RegisterReceptionUseCase` (in) — `register(RegisterReceptionCommand): ReceptionResult`
- `GetReceptionUseCase` (in) — `findById(Long): ReceptionResult`, `findByOperatorAndShift(Long operatorId, Long shiftId): List<ReceptionResult>`
- `RegisterReceptionCommand` record: containerId, profileId, lot, receivedQuantity, operatorId
- `ReceptionRepositoryPort` (out) — save, findById, findByOperatorId + shift time filter

**Application:**
- `RegisterReceptionService` — saves reception, then calls `InventoryItemCreationPort`
- `GetReceptionService`

**Persistence:**
- `ReceptionJpaEntity` — maps `receptions`; scalar foreign keys (no JPA relationships)
- `ReceptionSpringRepository`
- `ReceptionPersistenceAdapter`

**REST:**
- `ReceptionRestController`
  - `POST /api/receptions` (201) — body: `ReceptionRequest { containerId, profileId, lot, receivedQuantity }`, operatorId from `@AuthenticationPrincipal`
  - `GET /api/receptions/my?shiftId={id}` — operatorId from principal
  - `GET /api/receptions/{id}`

---

### Module: `cutting` (`com.example.company.cutting`)

Table: `cutting_records` (id, inventory_item_id, machine_id, operator_id, shift_id, initial_quantity, good_quantity, scrap_quantity, cut_at, version)

**Domain (extends existing):**
- `CuttingRecord` record: id, inventoryItemId, machineId, operatorId, shiftId, quantities (CuttingQuantities), cutAt
- `CuttingQuantities` already exists — reuse as-is
- `CuttingResult` record: id, inventoryItemId, machineId, operatorId, shiftId, initialQuantity, goodQuantity, scrapQuantity, cutAt
- `AvailableCuttingResult` record: cuttingRecordId, containerCode, profileCode, initialQuantity, goodQuantity, scrapQuantity, cutAt
- `CuttingNotAvailableException` → `DomainErrorType.NOT_FOUND`, code `cutting.not-available`
- `CuttingQuantityInvariantException` → `DomainErrorType.BUSINESS_RULE`, code `cutting.quantity-invariant`

**Ports:**
- `RegisterCuttingUseCase` (in) — `register(RegisterCuttingCommand): CuttingResult`
- `GetCuttingUseCase` (in) — `findById(Long)`, `findByOperatorAndShift(Long, Long)`, `findAvailableByContainerCode(String)`
- `RegisterCuttingCommand` record: inventoryItemId, machineId, shiftId, initialQuantity, goodQuantity, scrapQuantity, operatorId
- `CuttingRepositoryPort` (out) — save, findById, findByOperatorId+shift, findCutByContainerCode

**Application:**
- `RegisterCuttingService` — constructs `CuttingQuantities` (throws `CuttingQuantityInvariantException` wrapping the IllegalArgumentException on violation), saves, calls `InventoryItemUpdatePort`
- `GetCuttingService`

**Persistence:**
- `CuttingRecordJpaEntity` — maps `cutting_records`; scalar FKs
- `CuttingSpringRepository`
- `CuttingPersistenceAdapter`

**REST:**
- `CuttingRestController`
  - `POST /api/cutting` (201) — body: `CuttingRequest { inventoryItemId, machineId, shiftId, initialQuantity, goodQuantity, scrapQuantity }`, operatorId from principal
  - `GET /api/cutting/my?shiftId={id}`
  - `GET /api/cutting/{id}`
  - `GET /api/cutting/available?containerCode={code}` → `AvailableCuttingResponse`

---

### Module: `scrap` (`com.example.company.scrap`)

Table: `scrap_records` (id, cutting_record_id, quantity, reason, created_at)

**Domain:**
- `ScrapRecord` record: id, cuttingRecordId, quantity, reason, createdAt
- `ScrapResult` record: id, cuttingRecordId, quantity, reason, createdAt
- `ScrapNotFoundException` → `DomainErrorType.NOT_FOUND`, code `scrap.not-found`

**Ports:**
- `RegisterScrapUseCase` (in) — `register(RegisterScrapCommand): ScrapResult`
- `GetScrapUseCase` (in) — `findById(Long)`, `findByOperatorAndShift(Long, Long)`
- `RegisterScrapCommand` record: cuttingRecordId, quantity, reason
- `ScrapRepositoryPort` (out) — save, findById, findByCuttingRecordIdAndShift

**Note on /my?shiftId:** scrap_records has no direct operator_id. Query joins scrap → cutting_records → operator_id and filters by shift time window.

**Application:**
- `RegisterScrapService`
- `GetScrapService`

**Persistence:**
- `ScrapRecordJpaEntity`
- `ScrapSpringRepository`
- `ScrapPersistenceAdapter`

**REST:**
- `ScrapRestController`
  - `POST /api/scrap` (201) — body: `ScrapRequest { cuttingRecordId, quantity, reason? }`
  - `GET /api/scrap/my?shiftId={id}` — operator derived from cutting → operator_id
  - `GET /api/scrap/{id}`

---

### Module: `molding` (`com.example.company.molding`)

Table: `molding_outputs` (id, cutting_record_id, quantity_sent, operator_id, sent_at)

**Domain:**
- `MoldingOutput` record: id, cuttingRecordId, quantitySent, operatorId, sentAt
- `MoldingOutputResult` record: id, cuttingRecordId, quantitySent, operatorId, sentAt
- `MoldingOutputNotFoundException` → `DomainErrorType.NOT_FOUND`, code `molding.not-found`

**Ports:**
- `RegisterMoldingOutputUseCase` (in) — `register(RegisterMoldingOutputCommand): MoldingOutputResult`
- `GetMoldingOutputUseCase` (in) — `findById(Long)`, `findByOperatorAndShift(Long, Long)`
- `RegisterMoldingOutputCommand` record: cuttingRecordId, quantitySent, operatorId
- `MoldingOutputRepositoryPort` (out) — save, findById, findByOperatorIdAndShift

**Application:**
- `RegisterMoldingOutputService`
- `GetMoldingOutputService`

**Persistence:**
- `MoldingOutputJpaEntity`
- `MoldingOutputSpringRepository`
- `MoldingOutputPersistenceAdapter`

**REST:**
- `MoldingOutputRestController`
  - `POST /api/molding-outputs` (201) — body: `MoldingOutputRequest { cuttingRecordId, quantitySent }`, operatorId from principal
  - `GET /api/molding-outputs/my?shiftId={id}`
  - `GET /api/molding-outputs/{id}`

---

### Module: `activity` (`com.example.company.activity`)

No table — read-only aggregation across all operational tables via JPQL.

**Domain:**
- `ActivityRecord` record: id, time (LocalTime), containerCode, profileCode, action (ActivityAction enum), quantities (String), status
- `ActivityAction` enum: `RECEPTION`, `CUT`, `SCRAP`, `MOLDING_OUTPUT`
- `ActivityResult` record — same fields

**Ports:**
- `GetActivityUseCase` (in) — `findByOperatorAndShift(Long operatorId, Long shiftId): List<ActivityResult>`
- `ActivityQueryPort` (out) — `findReceptionsByOperatorAndShift(...)`, `findCuttingByOperatorAndShift(...)`, `findScrapByOperatorAndShift(...)`, `findMoldingByOperatorAndShift(...)`

**Application:**
- `GetActivityService` — calls all 4 query methods, merges by timestamp, formats quantity strings:
  - Reception: `"{receivedQuantity} pcs"`
  - Cut: `"{initial} → {good} good · {scrap} scrap"`
  - Scrap: `"{quantity} pcs"`
  - Molding: `"{quantitySent} pcs"`

**Persistence:**
- `ActivityPersistenceAdapter` — implements `ActivityQueryPort` with JPQL joins; no new entity needed; reads from existing JPA entities across modules (or uses `@Query` on existing repositories)

**REST:**
- `ActivityRestController`
  - `GET /api/activity/my?shiftId={id}` — returns `List<ActivityResponse>`, sorted by time ASC

---

### Shift time window logic

For all `/my?shiftId={id}` queries: load shift (startTime, endTime). If `endTime > startTime` → same day window. If `endTime <= startTime` → overnight shift; records with timestamp after shift start today OR before shift end today are included.

---

### Principal extraction pattern

All controllers that need the authenticated operator ID use:

```java
@AuthenticationPrincipal AuthenticatedUserPrincipal principal
// then: principal.userId()
```

Never accept operatorId from the request body.

---

### Error codes

| Code | HTTP | Trigger |
|---|---|---|
| `inventory.not-available` | 404 | No AVAILABLE inventory item for container code |
| `cutting.not-available` | 404 | No CUT cutting record for container code |
| `cutting.quantity-invariant` | 422 | initial ≠ good + scrap |
| `reception.not-found` | 404 | Reception id not found |
| `scrap.not-found` | 404 | Scrap record id not found |
| `molding.not-found` | 404 | Molding output id not found |

## Frontend Scope

N/A — this spec covers the Spring Boot backend only. Angular pages for Register Cut and Register Molding Output are specified separately in `_specs/register-cut-and-molding-output.md`.

## Data and Validation

- `receivedQuantity` / `initialQuantity` / `goodQuantity` / `scrapQuantity` / `quantitySent` / `quantity`: all `@Min(1)` or `@Min(0)` as appropriate per table CHECK constraints
- `lot`: `@NotBlank`, max 80 chars
- `reason` on scrap: optional, max 255 chars
- `containerId`, `profileId`, `machineId`, `shiftId`, `inventoryItemId`, `cuttingRecordId`: `@NotNull`
- Cutting invariant validated in domain (`CuttingQuantities`) before persistence; also enforced by DB CHECK

## Security and Access

- `POST /api/receptions`, `POST /api/cutting`, `POST /api/scrap`, `POST /api/molding-outputs`: `hasAnyRole("ADMIN", "OPERADOR", "SUPERVISOR")`
- `GET /api/receptions/my`, `GET /api/receptions/{id}`: `authenticated()`
- `GET /api/cutting/my`, `GET /api/cutting/{id}`, `GET /api/cutting/available`: `authenticated()`
- `GET /api/scrap/my`, `GET /api/scrap/{id}`: `authenticated()`
- `GET /api/molding-outputs/my`, `GET /api/molding-outputs/{id}`: `authenticated()`
- `GET /api/activity/my`: `authenticated()`
- `GET /api/inventory/available`: `authenticated()`
- All security rules already configured in `SecurityConfiguration.java`
- `operatorId` always sourced from `principal.userId()` — never from request body

## Acceptance Criteria

1. `POST /api/receptions` with valid body returns 201 and auto-creates an AVAILABLE inventory item
2. `GET /api/inventory/available?containerCode=X` returns the inventory item when status is AVAILABLE; 404 with `inventory.not-available` when missing or CUT
3. `POST /api/cutting` with `initialQuantity ≠ goodQuantity + scrapQuantity` returns 422 with `cutting.quantity-invariant`
4. `POST /api/cutting` with valid quantities returns 201 and marks the inventory item as CUT
5. `GET /api/cutting/available?containerCode=X` returns the cutting record when status is CUT; 404 with `cutting.not-available` otherwise
6. `POST /api/scrap` and `POST /api/molding-outputs` return 201
7. `GET /api/activity/my?shiftId=X` returns all movements for the authenticated operator within the shift time window, sorted by time ASC
8. Overnight shifts are handled correctly: records spanning midnight are included
9. All `operatorId` values come from JWT — no client-supplied operator override accepted

## Open Questions

- None
