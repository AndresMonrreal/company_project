# Spec: Operational Modules — Reception, Cutting, Scrap & Molding

## Summary

Implement the four core operational modules (Reception, Cutting, Scrap, Molding Output) as full hexagonal backend modules with REST endpoints, plus wire the existing `my-activity` Angular page to the real API through a unified activity endpoint.

## Problem

The manufacturing traceability flow — Reception → Inventory → Cutting → Scrap → Molding Output — has no backend endpoints. Operators cannot register movements, and the `my-activity` page shows only mock data. The V1 database schema and catalog endpoints already exist; what is missing is the domain, use-case, persistence, and REST layers for each operational step, and the frontend wiring.

## Goals

- Register a reception and automatically create an inventory item (AVAILABLE).
- Register a cut, enforce the quantity invariant in domain, and update the inventory item to CUT.
- Register scrap linked to a cutting record.
- Register a molding output linked to a cutting record.
- Expose a unified `/api/activity/my` endpoint that aggregates all four movement types for the authenticated operator in a shift.
- Wire `MyActivityPageComponent` to the real activity API with loading and error states.

## Non-Goals

- Angular forms for Register Reception, Register Cut, Register Molding Output (next spec).
- Unit/integration tests (separate spec).
- GraphQL resolvers (separate spec).
- Inventory management beyond status updates triggered by operations (AVAILABLE → CUT).
- Modifying V1 or V2 Flyway migrations — use existing tables as-is.

## Users and Flows

**Primary actor:** OPERADOR (or SUPERVISOR)

**Reception flow:**
1. Operator submits `POST /api/receptions` with container, profile, lot, and received quantity.
2. Backend creates a `receptions` row (status = RECEIVED) and an `inventory_items` row (status = AVAILABLE, available_quantity = receivedQuantity).
3. Backend returns reception summary.

**Cutting flow:**
1. Operator submits `POST /api/cutting` with inventory item, machine, shift, and three quantities.
2. Backend validates `initialQuantity = goodQuantity + scrapQuantity` in domain (`CuttingQuantities` already exists).
3. Backend creates a `cutting_records` row and updates the `inventory_items` row: available_quantity = goodQuantity, status = CUT.
4. Backend returns cutting summary with container and profile codes.

**Scrap flow:**
1. Operator submits `POST /api/scrap` with cutting record ID, quantity, and optional reason.
2. Backend creates a `scrap_records` row.
3. Backend returns scrap record summary.

**Molding Output flow:**
1. Operator submits `POST /api/molding-outputs` with cutting record ID and quantity sent.
2. Backend creates a `molding_outputs` row.
3. Backend returns molding output summary.

**Activity view (frontend):**
1. `MyActivityPageComponent` calls `ActivityService` on init with current shift ID.
2. Page shows loading spinner, then the unified timeline or an error message.
3. Dev toggle allows switching to mock data.

## Backend Scope

### New modules

#### `com.example.company.reception`

- **Domain:** `Reception` aggregate, `ReceptionStatus` enum (RECEIVED), `ReceptionNotFoundException`, `reception.not-found` error code
- **Ports in:** `RegisterReceptionUseCase` (command: `RegisterReceptionCommand`, result: `ReceptionResult`), `GetMyReceptionsUseCase`
- **Ports out:** `ReceptionRepositoryPort`, `InventoryItemCreationPort` (from `inventory` module)
- **Use cases:** `RegisterReceptionService` (creates reception then calls `InventoryItemCreationPort` — single transaction), `GetMyReceptionsService`
- **Persistence:** `ReceptionJpaEntity`, `ReceptionSpringRepository`, `ReceptionPersistenceAdapter`
- **REST:** `POST /api/receptions` (201), `GET /api/receptions/my?shiftId={shiftId}` (200)
- **Web DTO:** `RegisterReceptionRequest` (containerId, profileId, lot, receivedQuantity), `ReceptionResponse`
- **Security:** requires OPERADOR or SUPERVISOR role

#### `com.example.company.inventory` (new module)

Owns `InventoryItemJpaEntity` so neither `reception` nor `cutting` hold it directly.

- **Domain:** `InventoryItem` aggregate, `InventoryItemStatus` enum (AVAILABLE, CUT), `InventoryItemNotFoundException`, `inventory.not-found` error code
- **Ports in:** none (no direct REST exposure in this spec)
- **Ports out:** `InventoryItemCreationPort` (called by `reception`), `InventoryItemUpdatePort` (called by `cutting`), `InventoryItemRepositoryPort`
- **Use cases:** `CreateInventoryItemService`, `UpdateInventoryItemService` (used internally — not exposed as REST endpoints in this spec)
- **Persistence:** `InventoryItemJpaEntity`, `InventoryItemSpringRepository`, `InventoryPersistenceAdapter` implements both `InventoryItemCreationPort` and `InventoryItemUpdatePort`
- No REST controller in this spec.

#### `com.example.company.scrap`

- **Domain:** `ScrapRecord` aggregate, `ScrapRecordNotFoundException`, `scrap.not-found` error code
- **Ports in:** `RegisterScrapUseCase` (command: `RegisterScrapCommand`, result: `ScrapResult`), `GetMyScrapUseCase`
- **Ports out:** `ScrapRepositoryPort`
- **Use cases:** `RegisterScrapService`, `GetMyScrapService`
- **Persistence:** `ScrapRecordJpaEntity`, `ScrapSpringRepository`, `ScrapPersistenceAdapter`
- **REST:** `POST /api/scrap` (201), `GET /api/scrap/my?shiftId={shiftId}` (200)
- **Web DTO:** `RegisterScrapRequest` (cuttingRecordId, quantity, reason?), `ScrapResponse`
- **Security:** requires OPERADOR or SUPERVISOR role

#### `com.example.company.molding`

- **Domain:** `MoldingOutput` aggregate, `MoldingOutputNotFoundException`, `molding.not-found` error code
- **Ports in:** `RegisterMoldingOutputUseCase` (command: `RegisterMoldingOutputCommand`, result: `MoldingOutputResult`), `GetMyMoldingOutputsUseCase`
- **Ports out:** `MoldingOutputRepositoryPort`
- **Use cases:** `RegisterMoldingOutputService`, `GetMyMoldingOutputsService`
- **Persistence:** `MoldingOutputJpaEntity`, `MoldingOutputSpringRepository`, `MoldingOutputPersistenceAdapter`
- **REST:** `POST /api/molding-outputs` (201), `GET /api/molding-outputs/my?shiftId={shiftId}` (200)
- **Web DTO:** `RegisterMoldingOutputRequest` (cuttingRecordId, quantitySent), `MoldingOutputResponse`
- **Security:** requires OPERADOR or SUPERVISOR role

### Extended module

#### `com.example.company.cutting` (extend existing)

Existing: `CuttingQuantities` value object only.

Add:
- **Domain:** `CuttingRecord` aggregate referencing `CuttingQuantities`, `CuttingRecordNotFoundException`, `cutting.not-found` error code, `cutting.quantity-invariant` for invariant violations
- **Ports in:** `RegisterCutUseCase` (command: `RegisterCutCommand`, result: `CuttingResult`), `GetMyCuttingRecordsUseCase`
- **Ports out:** `CuttingRepositoryPort`, `InventoryItemUpdatePort` (from `inventory` module)
- **Use cases:** `RegisterCutService` (enforces `CuttingQuantities` invariant, then calls `InventoryItemUpdatePort`), `GetMyCuttingRecordsService`
- **Persistence:** `CuttingRecordJpaEntity`, `CuttingSpringRepository`, `CuttingPersistenceAdapter`
- **REST:** `POST /api/cutting` (201), `GET /api/cutting/my?shiftId={shiftId}` (200)
- **Web DTO:** `RegisterCutRequest` (inventoryItemId, machineId, shiftId, initialQuantity, goodQuantity, scrapQuantity), `CuttingResponse`
- **Security:** requires OPERADOR or SUPERVISOR role

### Activity endpoint

#### `com.example.company.activity` (new module)

- **REST:** `GET /api/activity/my?shiftId={shiftId}` (200)
- Queries reception, cutting, scrap, and molding output repositories for the authenticated operator. Filters each record by comparing its timestamp (`received_at`, `cut_at`, `created_at`, `sent_at`) against the window formed by combining today's date with `shifts.start_time` and `shifts.end_time` for the given `shiftId`. Overnight shifts (endTime before startTime) span two calendar dates.
- Merges and sorts results chronologically.
- **Response per item:** `time` (HH:mm), `containerCode`, `profileCode`, `action` (RECEPTION | CUT | SCRAP | MOLDING_OUTPUT), `quantities` (formatted string), `status`
- `quantities` format examples:
  - Reception: `"120 pcs"`
  - Cut: `"240 → 228 good · 12 scrap"`
  - Scrap: `"12 pcs"`
  - Molding output: `"228 pcs"`
- This endpoint is read-only; requires authentication (any authenticated role).

### Security configuration

In `SecurityConfiguration.java`, add to `authorizeHttpRequests`:

```
POST /api/receptions       → hasAnyRole("OPERADOR", "SUPERVISOR")
GET  /api/receptions/my    → authenticated
POST /api/cutting          → hasAnyRole("OPERADOR", "SUPERVISOR")
GET  /api/cutting/my       → authenticated
POST /api/scrap            → hasAnyRole("OPERADOR", "SUPERVISOR")
GET  /api/scrap/my         → authenticated
POST /api/molding-outputs  → hasAnyRole("OPERADOR", "SUPERVISOR")
GET  /api/molding-outputs/my → authenticated
GET  /api/activity/my      → authenticated
```

`operator_id` always comes from `principal.userId()` — never from the request body.

## Frontend Scope

### `tesla-web-app/src/app/features/my-activity/`

**`models/activity-record.model.ts`** — update/extend:
- Keep `ActivityRecord`, `ActivityAction`, `ActivityStatus`, badge color maps.
- Add: `ReceptionResponse`, `CuttingResponse`, `ScrapResponse`, `MoldingOutputResponse` interfaces matching backend response shapes.

**`data-access/activity-api.client.ts`** — new file:
- `ActivityApiClient` injectable.
- `getMyActivity(shiftId: number): Observable<ActivityRecord[]>` — `GET /api/activity/my?shiftId={shiftId}`.
- Uses `inject(HttpClient)` and `inject(API_BASE_URL)`.
- Never called directly from components.

**`services/activity.service.ts`** — new file:
- `ActivityService` injectable.
- `loadMyActivity(shiftId: number): Observable<ActivityRecord[]>` — wraps `ActivityApiClient`.
- Handles error normalization if needed.

**`pages/my-activity.page.ts`** — update:
- Inject `ActivityService`.
- On `ngOnInit`, call `loadMyActivity(shiftId)` where `shiftId` comes from `AuthSession` if available, or defaults to `1`.
- Track loading state with a signal: `isLoading = signal(false)`.
- Track error state with a signal: `error = signal<string | null>(null)`.
- Show loading spinner while `isLoading()` is true.
- Show error message block when `error()` is non-null.
- Keep existing dev toggle to switch between real data and mock data.

## API Contract

### POST /api/receptions

**Request:**
```json
{ "containerId": 1, "profileId": 2, "lot": "L-2026-001", "receivedQuantity": 120 }
```

**Response 201:**
```json
{
  "id": 1,
  "containerCode": "C-01",
  "profileCode": "P-36",
  "lot": "L-2026-001",
  "receivedQuantity": 120,
  "status": "RECEIVED",
  "receivedAt": "2026-06-13T08:30:00"
}
```

### POST /api/cutting

**Request:**
```json
{ "inventoryItemId": 1, "machineId": 2, "shiftId": 1, "initialQuantity": 120, "goodQuantity": 108, "scrapQuantity": 12 }
```

**Response 201:**
```json
{
  "id": 1,
  "containerCode": "C-01",
  "profileCode": "P-36",
  "machineCode": "M-01",
  "initialQuantity": 120,
  "goodQuantity": 108,
  "scrapQuantity": 12,
  "cutAt": "2026-06-13T09:15:00"
}
```

**Error 422** when `initialQuantity != goodQuantity + scrapQuantity`:
```json
{ "code": "cutting.quantity-invariant", "message": "..." }
```

### POST /api/scrap

**Request:**
```json
{ "cuttingRecordId": 1, "quantity": 12, "reason": "Dimensional defect" }
```

**Response 201:**
```json
{ "id": 1, "cuttingRecordId": 1, "quantity": 12, "reason": "Dimensional defect", "createdAt": "2026-06-13T09:20:00" }
```

### POST /api/molding-outputs

**Request:**
```json
{ "cuttingRecordId": 1, "quantitySent": 108 }
```

**Response 201:**
```json
{ "id": 1, "cuttingRecordId": 1, "quantitySent": 108, "sentAt": "2026-06-13T10:00:00" }
```

### GET /api/activity/my?shiftId=1

**Response 200:**
```json
[
  { "time": "08:30", "containerCode": "C-01", "profileCode": "P-36", "action": "RECEPTION", "quantities": "120 pcs", "status": "RECEIVED" },
  { "time": "09:15", "containerCode": "C-01", "profileCode": "P-36", "action": "CUT", "quantities": "120 → 108 good · 12 scrap", "status": "CUT" },
  { "time": "09:20", "containerCode": "C-01", "profileCode": "P-36", "action": "SCRAP", "quantities": "12 pcs", "status": null },
  { "time": "10:00", "containerCode": "C-01", "profileCode": "P-36", "action": "MOLDING_OUTPUT", "quantities": "108 pcs", "status": null }
]
```

## Data and Validation

| Field | Rule |
|---|---|
| `receivedQuantity` | Positive integer (≥ 1) |
| `initialQuantity` | Positive integer (≥ 1) |
| `goodQuantity` | Non-negative integer (≥ 0) |
| `scrapQuantity` | Non-negative integer (≥ 0) |
| `initialQuantity = goodQuantity + scrapQuantity` | Domain invariant — enforced in `CuttingQuantities` before persistence |
| `quantitySent` | Positive integer (≥ 1) |
| `quantity` (scrap) | Positive integer (≥ 1) |
| `lot` | Non-blank string |
| `reason` (scrap) | Optional string, may be null/absent |
| `operator_id` | Always from `principal.userId()` — never from request body |
| `shiftId` filter | Used to compute shift start/end datetime window for filtering records |

Cross-module foreign key constraints (already enforced by V1 schema):
- `inventory_items.reception_id` → `receptions.id`
- `cutting_records.inventory_item_id` → `inventory_items.id`
- `scrap_records.cutting_record_id` → `cutting_records.id`
- `molding_outputs.cutting_record_id` → `cutting_records.id`

## Security and Access

- All endpoints require a valid JWT Bearer token.
- `POST` endpoints for operational mutations require role `OPERADOR` or `SUPERVISOR`.
- `GET /my` endpoints require authentication; backend returns only records owned by `principal.userId()`.
- `operator_id` is always derived from the authenticated principal — the request body must not contain it.
- Authorization rules added to `SecurityConfiguration.java` in the `security` module.
- After any controller or security config change: run `hexagonal-security-reviewer`.

## Acceptance Criteria

1. `POST /api/receptions` with valid payload returns 201 with reception summary; a corresponding `inventory_items` row exists with status AVAILABLE.
2. `POST /api/receptions` without authentication returns 401.
3. `POST /api/receptions` with role CONSULTA returns 403.
4. `POST /api/cutting` with `initialQuantity != goodQuantity + scrapQuantity` returns 422 with `cutting.quantity-invariant` code.
5. `POST /api/cutting` with valid payload returns 201; the linked `inventory_items` row has status CUT and available_quantity = goodQuantity.
6. `POST /api/scrap` with valid payload returns 201 with scrap summary.
7. `POST /api/molding-outputs` with valid payload returns 201 with molding output summary.
8. `GET /api/activity/my?shiftId=1` returns a chronologically sorted list of all four movement types for the authenticated operator.
9. `GET /api/activity/my` returns only the authenticated operator's own records — never other operators' data.
10. `MyActivityPageComponent` shows a loading spinner while the API call is in-flight.
11. `MyActivityPageComponent` shows an error message when the API call fails.
12. `MyActivityPageComponent` renders real data from `/api/activity/my` on successful load.
13. Dev toggle in `MyActivityPageComponent` switches between real data and mock data.
14. ArchUnit boundary tests pass: domain modules have no Spring/JPA/Jackson imports.

## Agents and Skills to Use

| Layer | Agent / Skill |
|---|---|
| Domain (reception, cutting extension, scrap, molding) | `hexagonal-domain-developer` |
| Use cases + port wiring | `hexagonal-application-developer` |
| JPA entities, repos, persistence adapters | `hexagonal-persistence-adapter` |
| REST controllers, DTOs, web mappers | `hexagonal-web-adapter` |
| Security config update | `hexagonal-security-reviewer` |
| Angular models, API client, service, page update | `frontend-developer` + `$create-angular-feature` |

