# Spec: Base Catalogs Module

## Summary

Define the base catalog behavior for the manufacturing traceability backend in this exact order:

1. roles
2. container_types
3. containers
4. machines
5. shifts

These catalogs support the operational flow:

```text
Reception -> Inventory -> Cutting -> Scrap -> Molding Output
```

This specification documents the expected domain rules, use cases, REST APIs, RBAC expectations, dependencies, acceptance criteria, and current implementation status. It does not modify `V1__create_initial_schema.sql`; existing schema history remains append-only.

Important scope boundary: the `roles` module in this spec is only the roles catalog/base-data module. It must not implement JWT, login, password handling, Spring Security configuration, method security, filters, token validation, or endpoint protection. Auth/JWT and endpoint authorization must remain separate specs.

## Global Architecture Rules

- Follow the package structure defined in `AGENTS.md`:

```text
src/main/java/com/example/company/<module>/
  domain/
    model/
    event/
    exception/
    port/
      in/
      out/
    service/
  application/
    usecase/
    mapper/
  adapter/
    in/
      web/
        dto/
        graphql/
    out/
      persistence/
      external/
```

- Domain code must be pure Java.
- Domain code must not import Spring, JPA, Jackson, GraphQL, servlet APIs, repositories, controllers, DTO classes, or adapters.
- Input ports and command/result records belong under `domain/port/in`.
- Output ports belong under `domain/port/out`.
- Transactional orchestration belongs in `application/usecase`.
- REST controllers must depend on input ports, not persistence adapters or Spring Data repositories.
- REST adapters must return response DTOs, never JPA entities.
- Persistence adapters must implement output ports.
- JPA entities must stay inside `adapter/out/persistence`.
- Existing Flyway migrations must not be modified.
- Any future schema change must be a new append-only migration.
- Soft delete means `active = false`, never physical delete.

## RBAC Roles

### ADMIN

- Manage catalogs: roles, profiles, machines, shifts, containers.
- Manage users.
- View all reports.
- Export data.
- View full history for any lot or container.

### SUPERVISOR

- View operational catalogs but not modify them.
- View all movements of the day.
- View reports for own shift and general reports.
- View full history.
- Register receptions.
- Approve or cancel receptions when required.

### OPERADOR

- Register receptions.
- Register cutting records.
- Register scrap.
- Register molding output.
- View own history for the current shift.
- Cannot view general reports or catalog management screens.

### CONSULTA

- Read-only.
- View reports.
- View history.
- Read catalog data only when needed as report/history filters, not for catalog management.
- Cannot register anything.

## Required System Roles

The required system roles are:

- `ADMIN`
- `SUPERVISOR`
- `OPERADOR`
- `CONSULTA`

These roles are mandatory base data. Production deployments must not rely on users manually creating them through the roles catalog UI or API.

Role seeding is out of scope for this base-catalogs spec and must be handled in a separate role-seeding spec. Preferred implementation is an append-only Flyway migration, for example:

```text
V2__seed_default_roles.sql
```

That role-seeding spec must define idempotent inserts or conflict-safe SQL for the four required roles without modifying `V1__create_initial_schema.sql`.

## Catalog Management vs Operational Lookup

Catalog management endpoints are not the same as operational lookup endpoints.

Catalog management endpoints are for creating, editing, reviewing, and soft deleting catalog records. They are administrative screens. `OPERADOR` must not access catalog management endpoints.

Operational lookup endpoints are restricted read-only endpoints used by production flows to select active records. For example, an `OPERADOR` may need active containers while registering a reception or active machines and shifts while registering cutting records. Those lookup endpoints should return only active records and minimal fields needed for the assigned production flow.

`SUPERVISOR` may read operational catalogs but must not create, update, or soft delete catalog records.

`CONSULTA` may read catalog data only when that data is needed for report/history filters. `CONSULTA` must not use catalog management endpoints for administrative catalog work.

Recommended future lookup endpoint shape:

```text
GET /api/lookups/container-types
GET /api/lookups/containers
GET /api/lookups/machines
GET /api/lookups/shifts
```

The role catalog is security-sensitive and should not have an operational lookup endpoint unless a later auth/users/security spec explicitly requires one.

The authorization rules in this spec are requirements for future endpoint authorization. They do not mean this spec should implement Spring Security, JWT, filters, or endpoint protection.

## Module 1: roles

### 1. Current Implementation Status

Status: missing.

Current code search did not find Java or test files under:

```text
src/main/java/com/example/company/roles
src/test/java/com/example/company/roles
```

The database table already exists in `V1__create_initial_schema.sql`.

This module is only the roles catalog/base-data module. It defines how roles are represented and managed as catalog records; it does not authenticate users or protect endpoints.

Do not duplicate:

- Do not create a second roles schema table.
- Do not modify `V1__create_initial_schema.sql`.
- Do not implement JWT login, password handling, or Spring Security configuration in this catalog spec.
- Do not implement endpoint protection from this catalog spec.

### 2. Purpose

`roles` represents authorization roles that users will have in the system.

It exists to support role-based access control for catalog management, user management, operational registration, reports, exports, and history access.

Modules that depend on roles:

- `users`, through `users.role_id`.
- future `auth`, through authenticated user authorities.
- future endpoint authorization, through role checks.

### 3. Domain Model

Suggested domain model: `Role`.

| Domain field | Java type | DB column | DB type | Constraints |
|---|---:|---|---|---|
| id | Long | id | BIGSERIAL | primary key |
| name | String | name | VARCHAR(80) | not null, unique |
| description | String | description | VARCHAR(255) | nullable |
| active | boolean | active | BOOLEAN | not null, default true |
| version | Long | version | BIGINT | not null, default 0, persistence concern |
| createdAt | LocalDateTime | created_at | TIMESTAMP | not null, default current timestamp, persistence concern |
| updatedAt | LocalDateTime | updated_at | TIMESTAMP | not null, default current timestamp, persistence concern |

Domain invariants:

- `name` is required and trimmed.
- `name` length must not exceed 80 characters.
- `description` may be null or blank, but when present it must be trimmed and must not exceed 255 characters.
- `active` defaults to true for new roles.
- Soft delete sets `active = false`.
- Role names must be unique among active and persisted records.
- Seed/required role names are `ADMIN`, `SUPERVISOR`, `OPERADOR`, and `CONSULTA`.
- Required system roles must not depend on manual production creation.
- Required system role seeding must be specified separately, preferably with an append-only Flyway migration such as `V2__seed_default_roles.sql`.

Status values: not applicable.

### 4. Use Cases

| Use case | Actor | Input | Business rules | Output | Error cases | Authorization |
|---|---|---|---|---|---|---|
| CreateRoleUseCase | ADMIN | name, description | validate name, trim fields, reject duplicate name, create active role | RoleResult | 400 validation, 409 duplicate name | ADMIN only |
| GetRoleUseCase.findById | ADMIN | id | load active role by id | RoleResult | 404 not found | ADMIN only |
| GetRoleUseCase.findAllActive | ADMIN | none | return only active roles ordered by name | List<RoleResult> | none | ADMIN only |
| UpdateRoleUseCase | ADMIN | id, name, description | role must exist and be active, reject duplicate name for another id, trim fields | RoleResult | 400 validation, 404 not found, 409 duplicate name | ADMIN only |
| DeleteRoleUseCase | ADMIN | id | role must exist and be active, set `active = false`, never physical delete | void | 404 not found, 409 if role is assigned to active users and policy blocks deactivation | ADMIN only |

### 5. API Endpoints

| Use case | Method and URL | Request | Success | Errors | Allowed roles |
|---|---|---|---|---|---|
| Create role | POST `/api/roles` | `{ "name": "ADMIN", "description": "System administrator" }` | 201 RoleResponse | 400, 401, 403, 409 | ADMIN |
| Get role by id | GET `/api/roles/{id}` | path id | 200 RoleResponse | 401, 403, 404 | ADMIN |
| List active roles | GET `/api/roles` | none | 200 List<RoleResponse> | 401, 403 | ADMIN |
| Update role | PUT `/api/roles/{id}` | `{ "name": "SUPERVISOR", "description": "Shift supervisor" }` | 200 RoleResponse | 400, 401, 403, 404, 409 | ADMIN |
| Soft delete role | DELETE `/api/roles/{id}` | path id | 204 No Content | 401, 403, 404, 409 | ADMIN |

## Module 2: container_types

### 1. Current Implementation Status

Status: already implemented as a hexagonal module.

Existing files include:

```text
src/main/java/com/example/company/container_types/domain/model/ContainerType.java
src/main/java/com/example/company/container_types/domain/exception/ContainerTypeNotFoundException.java
src/main/java/com/example/company/container_types/domain/exception/DuplicateContainerTypeNameException.java
src/main/java/com/example/company/container_types/domain/port/in/ContainerTypeResult.java
src/main/java/com/example/company/container_types/domain/port/in/CreateContainerTypeCommand.java
src/main/java/com/example/company/container_types/domain/port/in/CreateContainerTypeUseCase.java
src/main/java/com/example/company/container_types/domain/port/in/DeleteContainerTypeUseCase.java
src/main/java/com/example/company/container_types/domain/port/in/GetContainerTypeUseCase.java
src/main/java/com/example/company/container_types/domain/port/in/UpdateContainerTypeCommand.java
src/main/java/com/example/company/container_types/domain/port/in/UpdateContainerTypeUseCase.java
src/main/java/com/example/company/container_types/domain/port/out/ContainerTypeRepositoryPort.java
src/main/java/com/example/company/container_types/application/mapper/ContainerTypeResultMapper.java
src/main/java/com/example/company/container_types/application/usecase/CreateContainerTypeService.java
src/main/java/com/example/company/container_types/application/usecase/DeleteContainerTypeService.java
src/main/java/com/example/company/container_types/application/usecase/GetContainerTypeService.java
src/main/java/com/example/company/container_types/application/usecase/UpdateContainerTypeService.java
src/main/java/com/example/company/container_types/adapter/in/web/ContainerTypeRestController.java
src/main/java/com/example/company/container_types/adapter/in/web/ContainerTypeWebMapper.java
src/main/java/com/example/company/container_types/adapter/in/web/dto/ContainerTypeCreateRequest.java
src/main/java/com/example/company/container_types/adapter/in/web/dto/ContainerTypeResponse.java
src/main/java/com/example/company/container_types/adapter/in/web/dto/ContainerTypeUpdateRequest.java
src/main/java/com/example/company/container_types/adapter/out/persistence/ContainerTypeJpaEntity.java
src/main/java/com/example/company/container_types/adapter/out/persistence/ContainerTypePersistenceAdapter.java
src/main/java/com/example/company/container_types/adapter/out/persistence/ContainerTypePersistenceMapper.java
src/main/java/com/example/company/container_types/adapter/out/persistence/SpringDataContainerTypeRepository.java
src/test/java/com/example/company/container_types/domain/model/ContainerTypeTest.java
```

What should not be duplicated:

- Do not create a second container type module.
- Do not create another `ContainerTypeJpaEntity`.
- Do not create another repository port outside `domain/port/out`.
- Do not modify the existing `container_types` table in V1.

Remaining acceptance criteria:

- Add security/authorization checks once auth/JWT is implemented.
- Add web/use case/persistence tests if not already present.
- Add operational lookup endpoint only if required by UI flows.
- Future implementation plans must not recreate existing domain, application, REST CRUD, persistence, or migration work for this module.

### 2. Purpose

`container_types` represents the type or category of a physical container.

It exists so the plant can classify containers consistently before assigning actual containers to receptions.

Modules that depend on it:

- `containers`, through `container_type_id`.
- `reception`, indirectly through selected containers.
- future operational lookup screens.

### 3. Domain Model

Existing domain model: `ContainerType`.

| Domain field | Java type | DB column | DB type | Constraints |
|---|---:|---|---|---|
| id | Long | id | BIGSERIAL | primary key |
| name | String | name | VARCHAR(80) | not null, unique |
| active | boolean | active | BOOLEAN | not null, default true |
| version | Long | version | BIGINT | not null, default 0, persistence concern |
| createdAt | LocalDateTime | created_at | TIMESTAMP | default current timestamp, persistence concern |
| updatedAt | LocalDateTime | updated_at | TIMESTAMP | default current timestamp, persistence concern |

Domain invariants:

- `name` is required and trimmed.
- `name` length must not exceed 80 characters.
- `active` defaults to true for new container types.
- Soft delete sets `active = false`.
- Names must be unique among persisted records.

Status values: not applicable.

### 4. Use Cases

| Use case | Actor | Input | Business rules | Output | Error cases | Authorization |
|---|---|---|---|---|---|---|
| CreateContainerTypeUseCase | ADMIN | name | validate name, trim, reject duplicate name, create active type | ContainerTypeResult | 400 validation, 409 duplicate name | ADMIN only |
| GetContainerTypeUseCase.findById | ADMIN, SUPERVISOR, CONSULTA | id | load active container type by id | ContainerTypeResult | 404 not found | ADMIN, SUPERVISOR, CONSULTA |
| GetContainerTypeUseCase.findAllActive | ADMIN, SUPERVISOR, CONSULTA | none | return only active types ordered by name | List<ContainerTypeResult> | none | ADMIN, SUPERVISOR, CONSULTA |
| UpdateContainerTypeUseCase | ADMIN | id, name | type must exist and be active, reject duplicate name for another id | ContainerTypeResult | 400 validation, 404 not found, 409 duplicate name | ADMIN only |
| DeleteContainerTypeUseCase | ADMIN | id | type must exist and be active, set `active = false`, never physical delete | void | 404 not found, 409 if active containers still depend on it and policy blocks deactivation | ADMIN only |

### 5. API Endpoints

Existing endpoint base: `/api/container-types`.

| Use case | Method and URL | Request | Success | Errors | Allowed roles |
|---|---|---|---|---|---|
| Create container type | POST `/api/container-types` | `{ "name": "Rack" }` | 201 ContainerTypeResponse | 400, 401, 403, 409 | ADMIN |
| Get container type by id | GET `/api/container-types/{id}` | path id | 200 ContainerTypeResponse | 401, 403, 404 | ADMIN, SUPERVISOR, CONSULTA |
| List active container types | GET `/api/container-types` | none | 200 List<ContainerTypeResponse> | 401, 403 | ADMIN, SUPERVISOR, CONSULTA |
| Update container type | PUT `/api/container-types/{id}` | `{ "name": "Rack" }` | 200 ContainerTypeResponse | 400, 401, 403, 404, 409 | ADMIN |
| Soft delete container type | DELETE `/api/container-types/{id}` | path id | 204 No Content | 401, 403, 404, 409 | ADMIN |
| Operational lookup | GET `/api/lookups/container-types` | none | 200 minimal active list | 401, 403 | ADMIN, SUPERVISOR, OPERADOR, CONSULTA when required by an operational UI |

## Module 3: containers

### 1. Current Implementation Status

Status: already implemented as a hexagonal module.

Existing files include:

```text
src/main/java/com/example/company/containers/domain/model/Container.java
src/main/java/com/example/company/containers/domain/exception/ContainerNotFoundException.java
src/main/java/com/example/company/containers/domain/exception/DuplicateContainerCodeException.java
src/main/java/com/example/company/containers/domain/port/in/ContainerResult.java
src/main/java/com/example/company/containers/domain/port/in/CreateContainerCommand.java
src/main/java/com/example/company/containers/domain/port/in/CreateContainerUseCase.java
src/main/java/com/example/company/containers/domain/port/in/DeleteContainerUseCase.java
src/main/java/com/example/company/containers/domain/port/in/GetContainerUseCase.java
src/main/java/com/example/company/containers/domain/port/in/UpdateContainerCommand.java
src/main/java/com/example/company/containers/domain/port/in/UpdateContainerUseCase.java
src/main/java/com/example/company/containers/domain/port/out/ContainerRepositoryPort.java
src/main/java/com/example/company/containers/application/mapper/ContainerResultMapper.java
src/main/java/com/example/company/containers/application/usecase/CreateContainerService.java
src/main/java/com/example/company/containers/application/usecase/DeleteContainerService.java
src/main/java/com/example/company/containers/application/usecase/GetContainerService.java
src/main/java/com/example/company/containers/application/usecase/UpdateContainerService.java
src/main/java/com/example/company/containers/adapter/in/web/ContainerRestController.java
src/main/java/com/example/company/containers/adapter/in/web/ContainerWebMapper.java
src/main/java/com/example/company/containers/adapter/in/web/dto/ContainerCreateRequest.java
src/main/java/com/example/company/containers/adapter/in/web/dto/ContainerResponse.java
src/main/java/com/example/company/containers/adapter/in/web/dto/ContainerUpdateRequest.java
src/main/java/com/example/company/containers/adapter/out/persistence/ContainerJpaEntity.java
src/main/java/com/example/company/containers/adapter/out/persistence/ContainerPersistenceAdapter.java
src/main/java/com/example/company/containers/adapter/out/persistence/ContainerPersistenceMapper.java
src/main/java/com/example/company/containers/adapter/out/persistence/SpringDataContainerRepository.java
src/test/java/com/example/company/containers/domain/model/ContainerTest.java
```

What should not be duplicated:

- Do not create a second containers module.
- Do not create another `ContainerJpaEntity`.
- Do not create a JPA relationship from `ContainerJpaEntity` to `ContainerTypeJpaEntity`.
- Keep the reference to container type by scalar ID only: `containerTypeId` in domain and `container_type_id` in persistence.
- Do not modify the existing `containers` table in V1.

Remaining acceptance criteria:

- Add security/authorization checks once auth/JWT is implemented.
- Add web/use case/persistence tests if not already present.
- Add operational lookup endpoint for reception flows.
- Future implementation plans must not recreate existing domain, application, REST CRUD, persistence, or migration work for this module.

### 2. Purpose

`containers` represents physical containers used to receive and track material.

It exists so each reception can reference the actual container used for a lot of rubber profiles.

Modules that depend on it:

- `reception`, through `receptions.container_id`.
- `traceability` and reports, through lot/container history.
- future operational lookup screens for reception registration.

### 3. Domain Model

Existing domain model: `Container`.

| Domain field | Java type | DB column | DB type | Constraints |
|---|---:|---|---|---|
| id | Long | id | BIGSERIAL | primary key |
| containerTypeId | Long | container_type_id | BIGINT | not null, references container_types(id) |
| code | String | code | VARCHAR(80) | not null, unique |
| active | boolean | active | BOOLEAN | not null, default true |
| version | Long | version | BIGINT | not null, default 0, persistence concern |
| createdAt | LocalDateTime | created_at | TIMESTAMP | default current timestamp, persistence concern |
| updatedAt | LocalDateTime | updated_at | TIMESTAMP | default current timestamp, persistence concern |

Domain invariants:

- `containerTypeId` is required.
- `code` is required and trimmed.
- `code` length must not exceed 80 characters.
- `active` defaults to true for new containers.
- Soft delete sets `active = false`.
- Codes must be unique among persisted records.
- Container type dependency is by ID only.
- Do not model the container type as a JPA relationship in `ContainerJpaEntity`.

Status values: not applicable.

### 4. Use Cases

| Use case | Actor | Input | Business rules | Output | Error cases | Authorization |
|---|---|---|---|---|---|---|
| CreateContainerUseCase | ADMIN | containerTypeId, code | validate type id, validate code, trim code, reject duplicate code, create active container | ContainerResult | 400 validation, 409 duplicate code, 404 container type not found if validated through a port | ADMIN only |
| GetContainerUseCase.findById | ADMIN, SUPERVISOR, CONSULTA | id | load active container by id | ContainerResult | 404 not found | ADMIN, SUPERVISOR, CONSULTA |
| GetContainerUseCase.findAllActive | ADMIN, SUPERVISOR, CONSULTA | none | return only active containers ordered by code | List<ContainerResult> | none | ADMIN, SUPERVISOR, CONSULTA |
| UpdateContainerUseCase | ADMIN | id, containerTypeId, code | container must exist and be active, type id required, reject duplicate code for another id | ContainerResult | 400 validation, 404 not found, 409 duplicate code | ADMIN only |
| DeleteContainerUseCase | ADMIN | id | container must exist and be active, set `active = false`, never physical delete | void | 404 not found, 409 if open receptions/history policy blocks deactivation | ADMIN only |

### 5. API Endpoints

Existing endpoint base: `/api/containers`.

| Use case | Method and URL | Request | Success | Errors | Allowed roles |
|---|---|---|---|---|---|
| Create container | POST `/api/containers` | `{ "containerTypeId": 1, "code": "BOX-001" }` | 201 ContainerResponse | 400, 401, 403, 404, 409 | ADMIN |
| Get container by id | GET `/api/containers/{id}` | path id | 200 ContainerResponse | 401, 403, 404 | ADMIN, SUPERVISOR, CONSULTA |
| List active containers | GET `/api/containers` | none | 200 List<ContainerResponse> | 401, 403 | ADMIN, SUPERVISOR, CONSULTA |
| Update container | PUT `/api/containers/{id}` | `{ "containerTypeId": 1, "code": "BOX-001" }` | 200 ContainerResponse | 400, 401, 403, 404, 409 | ADMIN |
| Soft delete container | DELETE `/api/containers/{id}` | path id | 204 No Content | 401, 403, 404, 409 | ADMIN |
| Operational lookup | GET `/api/lookups/containers` | optional filters such as containerTypeId | 200 minimal active list | 401, 403 | ADMIN, SUPERVISOR, OPERADOR, CONSULTA when required by reception/report flows |

## Module 4: machines

### 1. Current Implementation Status

Status: missing.

Current code search did not find Java or test files under:

```text
src/main/java/com/example/company/machines
src/test/java/com/example/company/machines
```

The database table already exists in `V1__create_initial_schema.sql`.

Do not duplicate:

- Do not create a second machines schema table.
- Do not modify `V1__create_initial_schema.sql`.

### 2. Purpose

`machines` represents the cutting or production machines used in the manufacturing process.

It exists so cutting records can identify where the operation happened.

Modules that depend on it:

- `cutting`, through `cutting_records.machine_Id` in V1. This unquoted PostgreSQL identifier resolves as `machine_id`.
- reports, for production by machine.
- traceability, for history by machine.

### 3. Domain Model

Suggested domain model: `Machine`.

| Domain field | Java type | DB column | DB type | Constraints |
|---|---:|---|---|---|
| id | Long | id | BIGSERIAL | primary key |
| name | String | name | VARCHAR(80) | not null, unique |
| active | boolean | active | BOOLEAN | not null, default true |
| version | Long | version | BIGINT | not null, default 0, persistence concern |
| createdAt | LocalDateTime | created_at | TIMESTAMP | default current timestamp, persistence concern |
| updatedAt | LocalDateTime | updated_at | TIMESTAMP | default current timestamp, persistence concern |

Domain invariants:

- `name` is required and trimmed.
- `name` length must not exceed 80 characters.
- `active` defaults to true for new machines.
- Soft delete sets `active = false`.
- Names must be unique among persisted records.

Status values: not applicable.

### 4. Use Cases

| Use case | Actor | Input | Business rules | Output | Error cases | Authorization |
|---|---|---|---|---|---|---|
| CreateMachineUseCase | ADMIN | name | validate name, trim, reject duplicate name, create active machine | MachineResult | 400 validation, 409 duplicate name | ADMIN only |
| GetMachineUseCase.findById | ADMIN, SUPERVISOR, CONSULTA | id | load active machine by id | MachineResult | 404 not found | ADMIN, SUPERVISOR, CONSULTA |
| GetMachineUseCase.findAllActive | ADMIN, SUPERVISOR, CONSULTA | none | return only active machines ordered by name | List<MachineResult> | none | ADMIN, SUPERVISOR, CONSULTA |
| UpdateMachineUseCase | ADMIN | id, name | machine must exist and be active, reject duplicate name for another id | MachineResult | 400 validation, 404 not found, 409 duplicate name | ADMIN only |
| DeleteMachineUseCase | ADMIN | id | machine must exist and be active, set `active = false`, never physical delete | void | 404 not found, 409 if policy blocks deactivation for machine with active/current cutting usage | ADMIN only |

### 5. API Endpoints

Recommended endpoint base: `/api/machines`.

| Use case | Method and URL | Request | Success | Errors | Allowed roles |
|---|---|---|---|---|---|
| Create machine | POST `/api/machines` | `{ "name": "CUT-01" }` | 201 MachineResponse | 400, 401, 403, 409 | ADMIN |
| Get machine by id | GET `/api/machines/{id}` | path id | 200 MachineResponse | 401, 403, 404 | ADMIN, SUPERVISOR, CONSULTA |
| List active machines | GET `/api/machines` | none | 200 List<MachineResponse> | 401, 403 | ADMIN, SUPERVISOR, CONSULTA |
| Update machine | PUT `/api/machines/{id}` | `{ "name": "CUT-01" }` | 200 MachineResponse | 400, 401, 403, 404, 409 | ADMIN |
| Soft delete machine | DELETE `/api/machines/{id}` | path id | 204 No Content | 401, 403, 404, 409 | ADMIN |
| Operational lookup | GET `/api/lookups/machines` | none | 200 minimal active list | 401, 403 | ADMIN, SUPERVISOR, OPERADOR, CONSULTA when required by cutting/report flows |

## Module 5: shifts

### 1. Current Implementation Status

Status: missing.

Current code search did not find Java or test files under:

```text
src/main/java/com/example/company/shifts
src/test/java/com/example/company/shifts
```

The database table already exists in `V1__create_initial_schema.sql`.

Do not duplicate:

- Do not create a second shifts schema table.
- Do not modify `V1__create_initial_schema.sql`.

### 2. Purpose

`shifts` represents production shifts such as first shift, second shift, or night shift.

It exists to associate manufacturing movements and reports with the working time window in which they happened.

Modules that depend on it:

- `cutting`, through `cutting_records.shift_id`.
- reports, for production by shift.
- traceability/history, for filtering movements by shift.
- future user/session context, when operators work in an active shift.

### 3. Domain Model

Suggested domain model: `Shift`.

| Domain field | Java type | DB column | DB type | Constraints |
|---|---:|---|---|---|
| id | Long | id | BIGSERIAL | primary key |
| name | String | name | VARCHAR(90) | not null, unique |
| startTime | LocalTime | start_time | TIME | not null |
| endTime | LocalTime | end_time | TIME | not null |
| active | boolean | active | BOOLEAN | not null, default true |
| version | Long | version | BIGINT | not null, default 0, persistence concern |
| createdAt | LocalDateTime | created_at | TIMESTAMP | not null, default current timestamp, persistence concern |
| updatedAt | LocalDateTime | updated_at | TIMESTAMP | not null, default current timestamp, persistence concern |

Domain invariants:

- `name` is required and trimmed.
- `name` length must not exceed 90 characters.
- `startTime` is required.
- `endTime` is required.
- `startTime` and `endTime` may define a same-day shift or an overnight shift. Overnight shifts are allowed when `endTime` is before `startTime`.
- `startTime` must not equal `endTime`.
- `active` defaults to true for new shifts.
- Soft delete sets `active = false`.
- Names must be unique among persisted records.

Status values: not applicable.

### 4. Use Cases

| Use case | Actor | Input | Business rules | Output | Error cases | Authorization |
|---|---|---|---|---|---|---|
| CreateShiftUseCase | ADMIN | name, startTime, endTime | validate name and times, reject same start/end, reject duplicate name, create active shift | ShiftResult | 400 validation, 409 duplicate name | ADMIN only |
| GetShiftUseCase.findById | ADMIN, SUPERVISOR, CONSULTA | id | load active shift by id | ShiftResult | 404 not found | ADMIN, SUPERVISOR, CONSULTA |
| GetShiftUseCase.findAllActive | ADMIN, SUPERVISOR, CONSULTA | none | return only active shifts ordered by name or start time; choose one and test it | List<ShiftResult> | none | ADMIN, SUPERVISOR, CONSULTA |
| UpdateShiftUseCase | ADMIN | id, name, startTime, endTime | shift must exist and be active, validate times, reject duplicate name for another id | ShiftResult | 400 validation, 404 not found, 409 duplicate name | ADMIN only |
| DeleteShiftUseCase | ADMIN | id | shift must exist and be active, set `active = false`, never physical delete | void | 404 not found, 409 if policy blocks deactivation for shift currently used by active production | ADMIN only |

### 5. API Endpoints

Recommended endpoint base: `/api/shifts`.

| Use case | Method and URL | Request | Success | Errors | Allowed roles |
|---|---|---|---|---|---|
| Create shift | POST `/api/shifts` | `{ "name": "First Shift", "startTime": "06:00:00", "endTime": "14:00:00" }` | 201 ShiftResponse | 400, 401, 403, 409 | ADMIN |
| Get shift by id | GET `/api/shifts/{id}` | path id | 200 ShiftResponse | 401, 403, 404 | ADMIN, SUPERVISOR, CONSULTA |
| List active shifts | GET `/api/shifts` | none | 200 List<ShiftResponse> | 401, 403 | ADMIN, SUPERVISOR, CONSULTA |
| Update shift | PUT `/api/shifts/{id}` | `{ "name": "First Shift", "startTime": "06:00:00", "endTime": "14:00:00" }` | 200 ShiftResponse | 400, 401, 403, 404, 409 | ADMIN |
| Soft delete shift | DELETE `/api/shifts/{id}` | path id | 204 No Content | 401, 403, 404, 409 | ADMIN |
| Operational lookup | GET `/api/lookups/shifts` | none | 200 minimal active list | 401, 403 | ADMIN, SUPERVISOR, OPERADOR, CONSULTA when required by cutting/report flows |

## RBAC Matrix

Legend:

- Yes: role may perform the action.
- No: role may not perform the action.
- Lookup: role may only access restricted operational lookup endpoints, not catalog management endpoints.
- Module-specific: role-specific access differs by catalog.

| Role | Create catalog records | Read catalog records | Update catalog records | Soft delete catalog records | Use catalog records indirectly in operational flows |
|---|---|---|---|---|---|
| ADMIN | Yes, all base catalogs | Yes, all base catalogs | Yes, all base catalogs | Yes, all base catalogs | Yes |
| SUPERVISOR | No | Yes for operational catalogs; roles read is ADMIN only unless security spec says otherwise | No | No | Yes, especially reception approvals and reports |
| OPERADOR | No | No catalog management screens | No | No | Lookup only for active containers, machines, shifts, and other operational selections required by assigned flows |
| CONSULTA | No | Only when catalog data is needed for report/history filters; roles read is ADMIN only unless security spec says otherwise | No | No | Lookup/read-only filters for reports and history |

Per-module management permissions:

| Module | ADMIN | SUPERVISOR | OPERADOR | CONSULTA |
|---|---|---|---|---|
| roles | create/read/update/soft delete | no | no | no |
| container_types | create/read/update/soft delete | read operational catalog only | lookup only if needed | report/history filter only |
| containers | create/read/update/soft delete | read operational catalog only | lookup only for reception | report/history filter only |
| machines | create/read/update/soft delete | read operational catalog only | lookup only for cutting | report/history filter only |
| shifts | create/read/update/soft delete | read operational catalog only | lookup only for shift-scoped production actions | report/history filter only |

## Acceptance Criteria

### Domain Rules

1. Role creation rejects blank names and trims valid names.
2. Role creation rejects names longer than 80 characters.
3. Role description is optional and rejects values longer than 255 characters.
4. Container type creation rejects blank names and trims valid names.
5. Container creation rejects missing `containerTypeId`.
6. Container creation rejects blank codes and trims valid codes.
7. Container domain and persistence continue to reference container type by scalar ID only.
8. Machine creation rejects blank names and trims valid names.
9. Shift creation rejects blank names and trims valid names.
10. Shift creation rejects missing `startTime` or `endTime`.
11. Shift creation rejects equal `startTime` and `endTime`.
12. Soft delete for every catalog sets `active = false`.
13. No catalog use case physically deletes a row.
14. Uniqueness is enforced through use case checks before persistence and by database unique constraints.

### Use Cases

1. Each missing module has one input port per operation: create, get/list, update, soft delete.
2. Each missing module has an output repository port under `domain/port/out`.
3. Each operation has one application service class under `application/usecase`.
4. Application services depend on output ports, not Spring Data repositories.
5. Create use cases return result records from `domain/port/in`.
6. Update use cases return updated result records.
7. Soft delete use cases return void and persist `active = false`.
8. Get-by-id use cases only return active records.
9. List use cases only return active records.
10. Duplicate values return a domain conflict error.

### REST Endpoints

1. REST controllers live under `adapter/in/web`.
2. Request and response DTOs live under `adapter/in/web/dto`.
3. REST controllers depend on input ports only.
4. REST responses never expose JPA entities.
5. Create endpoints return HTTP 201.
6. Get/list/update endpoints return HTTP 200.
7. Soft delete endpoints return HTTP 204.
8. Validation failures return HTTP 400 through the shared error handler.
9. Not-found domain errors return HTTP 404.
10. Duplicate/conflict domain errors return HTTP 409.
11. Future unauthenticated requests return HTTP 401.
12. Future unauthorized requests return HTTP 403.

### Authorization Behavior

1. `ADMIN` can create, read, update, and soft delete all base catalogs.
2. `SUPERVISOR` can read operational catalogs but cannot create, update, or soft delete them.
3. `OPERADOR` cannot access catalog management screens or endpoints.
4. `OPERADOR` can use restricted operational lookup endpoints required for reception, cutting, scrap, and molding flows.
5. `CONSULTA` can read catalog data only when needed for report/history filters and cannot use catalog management endpoints.
6. Roles catalog endpoints are ADMIN-only until a separate security spec says otherwise.
7. Authorization must be enforced at the inbound adapter or method-security boundary once auth/JWT is implemented.
8. Endpoint protection itself is not implemented from this base-catalogs spec.

### Soft Delete Behavior

1. Soft deleted records no longer appear in list-active endpoints.
2. Soft deleted records cannot be updated through normal update endpoints.
3. Get-by-id returns 404 for inactive records unless a future audit/history endpoint explicitly supports inactive records.
4. Soft deletion never removes rows from the database.
5. Soft deletion does not break historical references from operational records.

### Architecture Boundaries

1. Domain packages do not import Spring, JPA, Jackson, GraphQL, servlet APIs, DTOs, repositories, or adapters.
2. Application packages do not import adapter packages.
3. Inbound adapters do not import outbound adapters.
4. Persistence adapters implement domain output ports.
5. Existing `HexagonalArchitectureTest` passes.

### Migration Safety

1. `src/main/resources/db/migration/V1__create_initial_schema.sql` is not modified.
2. Existing tables are reused for all five base catalogs.
3. Any future schema change is documented as an append-only migration such as `V2__...`.
4. This spec does not require a new migration.

### Current Implemented Modules

1. `container_types` remains treated as already implemented.
2. `containers` remains treated as already implemented.
3. Future work must not duplicate those two modules.
4. Future work for those two modules is limited to missing tests, authorization integration, or operational lookup endpoints.

## Dependencies

### roles

Depends on:

- No other base catalog.
- Future auth/JWT context for authorization enforcement.

Depended on by:

- `users` through `users.role_id`.
- future auth/JWT token authority mapping.
- endpoint authorization across all modules.

Dependency type:

- `users` references roles by database foreign key.
- Application code should prefer role IDs or role names through domain ports, not adapter dependencies.

### container_types

Depends on:

- No other base catalog.

Depended on by:

- `containers` through `containers.container_type_id`.
- reception indirectly through selected containers.

Dependency type:

- `containers` references `container_types` by ID only.
- No JPA relationship between `ContainerJpaEntity` and `ContainerTypeJpaEntity`.
- If validation is needed, use a domain output port or application-level port, not direct adapter access.

### containers

Depends on:

- `container_types` by scalar ID.

Depended on by:

- `reception` through `receptions.container_id`.
- future traceability/history.
- reports.

Dependency type:

- `Container` stores `containerTypeId` as `Long`.
- `ContainerJpaEntity` maps `container_type_id` as `Long`.
- `reception` should reference containers by ID or by a domain port.

### machines

Depends on:

- No other base catalog.

Depended on by:

- `cutting` through `cutting_records.machine_Id` in V1. Because the identifier is unquoted in PostgreSQL, it should be treated as `machine_id` in mappings.
- reports by machine.
- traceability/history by machine.

Dependency type:

- Cutting records should reference machines by ID.
- If cutting must validate active machines before recording, use a machine output port or a dedicated validation port.

### shifts

Depends on:

- No other base catalog.

Depended on by:

- `cutting` through `cutting_records.shift_id`.
- reports by shift.
- traceability/history by shift.
- future operator session context.

Dependency type:

- Cutting records should reference shifts by ID.
- If operational flows must validate current/active shift, use a shift output port or a dedicated validation port.

## Out Of Scope

This spec documents catalog behavior and RBAC expectations only.

Out of scope:

- JWT login.
- Password hashing.
- User creation and credential management.
- Spring Security configuration.
- Token generation.
- Refresh tokens.
- Method security implementation.
- CORS security policy.
- Frontend screens.
- GraphQL schema/resolvers.
- Report generation.
- Export generation.
- Changing `V1__create_initial_schema.sql`.

Auth, users, JWT, roles seeding, and endpoint authorization must be handled in separate auth/users/security specs.

The four required system roles must be seeded through a separate role-seeding spec. Production behavior must not depend on someone manually creating `ADMIN`, `SUPERVISOR`, `OPERADOR`, or `CONSULTA`.

## Suggested Verification Commands

Use the smallest meaningful checks after implementation:

```powershell
.\gradlew.bat compileJava testClasses
.\gradlew.bat test --tests "*HexagonalArchitectureTest"
```

For implemented or new domain models, add targeted domain tests such as:

```powershell
.\gradlew.bat test --tests "*ContainerTypeTest"
.\gradlew.bat test --tests "*ContainerTest"
.\gradlew.bat test --tests "*MachineTest"
.\gradlew.bat test --tests "*ShiftTest"
.\gradlew.bat test --tests "*RoleTest"
```

## Recommended Implementation And Spec Order After This Spec

Recommended order:

1. `machines` implementation from this base-catalogs spec.
2. `shifts` implementation from this base-catalogs spec.
3. `roles` catalog implementation from this base-catalogs spec.
4. `_specs/role-seeding.md` for required system roles, preferably through an append-only migration such as `V2__seed_default_roles.sql`.
5. `_specs/users.md` for users, user lifecycle, role assignment, active/inactive users, and user persistence.
6. `_specs/auth-jwt.md` for login, JWT claims, token validation, password hashing, authentication filters, and token parsing.
7. `_specs/endpoint-authorization.md` for mapping roles to REST endpoints across catalogs and operational modules.
8. Operational lookup endpoints for active containers, container types, machines, shifts, and any other catalog required by assigned production flows.

Optional follow-up spec:

- `_specs/security-error-responses.md` for 401/403 response formats and integration with `GlobalExceptionHandler`.
