# Project Context Memory

Last updated: 2026-06-13

This is the living project snapshot for Codex work in this repository. Update this file in place when the architecture, module state, established patterns, or GraphQL context changes.

## Project Purpose

This repository contains a Spring Boot backend for a manufacturing traceability system for rubber profile cutting.

Business flow:

```text
Reception -> Inventory -> Cutting -> Scrap -> Molding Output
```

Core modules are `auth`, `profiles`, `machines`, `shifts`, `containers`, `reception`, `inventory`, `cutting`, `scrap`, `molding`, and `reports`.

Support modules are `users`, `roles`, `traceability`, and `exports`.

Profiles 36, 37, 38, and 39 all follow the same production flow.

## Current Stack

- Java 26 toolchain.
- Spring Boot 4.0.6.
- Gradle Wrapper.
- Spring Web MVC.
- Spring Validation.
- Spring Data JPA.
- Spring Security Crypto for BCrypt password hashing and password verification.
- Spring Security web for stateless JWT request validation and endpoint authorization.
- PostgreSQL.
- Flyway migrations.
- JUnit with Spring Boot test support.
- ArchUnit for automated architecture boundary tests.

## Architecture Decisions In Effect

- Use Hexagonal Architecture / Ports and Adapters.
- Use feature-based packages under `src/main/java/com/example/company/<module>/`.
- Keep domain code pure Java.
- Domain code must not import Spring, JPA, Jackson, GraphQL, servlet APIs, repositories, controllers, DTO classes, or adapters.
- Put input and output ports in `domain/port/in` and `domain/port/out`.
- Put transactions and orchestration in `application/usecase`.
- Keep repositories inside outbound persistence adapters.
- Return response DTOs from REST and future GraphQL adapters, never JPA entities.
- Existing Flyway migrations are append-only history. Do not modify `V1__create_initial_schema.sql`.
- The `roles` catalog is base catalog data only. Auth/JWT, users, role seeding, and endpoint authorization remain separate future specs/tasks.
- Security bootstrap boundary: required platform roles may be seeded with a new append-only Flyway migration, but the initial `ADMIN` user and optional demo users must be created by application bootstrap after Flyway because passwords come from environment/configuration.
- Auth/JWT boundary: the `auth` module implements login and HMAC JWT issuance; the `security` module handles JWT request validation, Spring Security filter chain setup, and current endpoint role rules.
- JWT access tokens are signed with HMAC-SHA256 using Java crypto and a configured `security.jwt.secret` / `SECURITY_JWT_SECRET`; no dedicated JWT library has been added.
- GraphQL is not runtime-enabled yet. Add Spring GraphQL only when explicitly requested.
- The `frontend/` boundary folder exists with the planned Angular `core/shared/features` skeleton, but the Angular app has not been scaffolded yet.
- The selected future frontend stack is Angular + TypeScript under `frontend/`.

## Standard Module Structure

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

## Critical Business Rule

The cutting invariant belongs in domain before persistence:

```text
initial_quantity = good_quantity + scrap_quantity
```

The database also enforces this rule with the `cutting_quantity_rule` check constraint in `src/main/resources/db/migration/V1__create_initial_schema.sql`.

Current domain implementation:

- `C:\Donatello\company\src\main\java\com\example\company\cutting\domain\model\CuttingQuantities.java`
- Rejects non-positive `initialQuantity`.
- Rejects negative `goodQuantity`.
- Rejects negative `scrapQuantity`.
- Rejects any value where `initialQuantity != goodQuantity + scrapQuantity`.

## Completed Modules And Current State

### profiles

`profiles` is the first migrated hexagonal pilot module.

Current state:

- Pure domain model exists in `profiles/domain/model/Profile.java`.
- Domain exceptions exist for duplicate code and missing profile.
- Input ports, commands, and results live in `profiles/domain/port/in`.
- Output repository port lives in `profiles/domain/port/out/ProfileRepositoryPort.java`.
- Application use cases are split by operation under `profiles/application/usecase`.
- REST adapter lives under `profiles/adapter/in/web`.
- Request and response DTOs live under `profiles/adapter/in/web/dto`.
- JPA entity, Spring Data repository, mapper, and persistence adapter live under `profiles/adapter/out/persistence`.
- No existing Flyway migration was modified for this migration.

### container_types

`container_types` is present as a hexagonal catalog module in the working tree.

Current state:

- Pure domain model exists in `container_types/domain/model/ContainerType.java`.
- Domain exceptions exist for duplicate name and missing container type.
- Input ports, commands, and results live in `container_types/domain/port/in`.
- Output repository port lives in `container_types/domain/port/out/ContainerTypeRepositoryPort.java`.
- Application use cases are split by operation under `container_types/application/usecase`.
- REST adapter lives under `container_types/adapter/in/web`.
- Request and response DTOs live under `container_types/adapter/in/web/dto`.
- JPA entity, Spring Data repository, mapper, and persistence adapter live under `container_types/adapter/out/persistence`.
- Domain tests exist for creation, blank-name rejection, and soft delete behavior.
- Focused application use case tests exist for create, get/list, update, duplicate-name rejection, missing-record rejection, and soft delete behavior.

### containers

`containers` is present as a hexagonal catalog module in the working tree.

Current state:

- Pure domain model exists in `containers/domain/model/Container.java`.
- Domain exceptions exist for duplicate code and missing container.
- Input ports, commands, and results live in `containers/domain/port/in`.
- Output repository port lives in `containers/domain/port/out/ContainerRepositoryPort.java`.
- Application use cases are split by operation under `containers/application/usecase`.
- REST adapter lives under `containers/adapter/in/web`.
- Request and response DTOs live under `containers/adapter/in/web/dto`.
- JPA entity, Spring Data repository, mapper, and persistence adapter live under `containers/adapter/out/persistence`.
- Domain tests exist for creation, required container type id, blank-code rejection, and soft delete behavior.
- Focused application use case tests exist for create, get/list, update, duplicate-code rejection, missing-record rejection, and soft delete behavior.

### cutting

`cutting` currently has the domain value object for the core quantity invariant.

Current state:

- `CuttingQuantities` exists under `cutting/domain/model`.
- Domain tests exist for the quantity rule.
- Future cutting use cases must construct `CuttingQuantities` before saving any cutting record.

### container_types

`container_types` is implemented as a complete hexagonal module backed by the existing `container_types` table in `V1__create_initial_schema.sql`.

Current state:

- Pure domain model exists in `container_types/domain/model/ContainerType.java`.
- Domain exceptions exist for duplicate name and missing container type.
- Input ports, commands, and results live in `container_types/domain/port/in`.
- Output repository port lives in `container_types/domain/port/out/ContainerTypeRepositoryPort.java`.
- Application use cases are split by operation under `container_types/application/usecase`.
- REST adapter lives under `container_types/adapter/in/web`.
- Request and response DTOs live under `container_types/adapter/in/web/dto`.
- JPA entity, Spring Data repository, mapper, and persistence adapter live under `container_types/adapter/out/persistence`.
- Operations implemented: create, get by id, list active, update, and soft delete with `active = false`.
- No Flyway migration was modified.

### containers

`containers` is implemented as a complete hexagonal module backed by the existing `containers` table in `V1__create_initial_schema.sql`.

Current state:

- Pure domain model exists in `containers/domain/model/Container.java`.
- The domain model references `containerTypeId` as a `Long`.
- The persistence adapter maps `container_type_id` as a `Long`; it does not create a JPA relationship to `ContainerTypeJpaEntity`.
- Domain exceptions exist for duplicate code and missing container.
- Input ports, commands, and results live in `containers/domain/port/in`.
- Output repository port lives in `containers/domain/port/out/ContainerRepositoryPort.java`.
- Application use cases are split by operation under `containers/application/usecase`.
- REST adapter lives under `containers/adapter/in/web`.
- Request and response DTOs live under `containers/adapter/in/web/dto`.
- JPA entity, Spring Data repository, mapper, and persistence adapter live under `containers/adapter/out/persistence`.
- Operations implemented: create, get by id, list active, update, and soft delete with `active = false`.
- No Flyway migration was modified.

### machines

`machines` is implemented as a complete hexagonal catalog module backed by the existing `machines` table in `V1__create_initial_schema.sql`.

Current state:

- Pure domain model exists in `machines/domain/model/Machine.java`.
- Domain exceptions exist for duplicate name and missing machine.
- Stable domain error codes are `machine.duplicate-name` and `machine.not-found`.
- Input ports, commands, and results live in `machines/domain/port/in`.
- Output repository port lives in `machines/domain/port/out/MachineRepositoryPort.java`.
- Application use cases are split by operation under `machines/application/usecase`.
- REST adapter lives under `machines/adapter/in/web`.
- Request and response DTOs live under `machines/adapter/in/web/dto`.
- JPA entity, Spring Data repository, mapper, and persistence adapter live under `machines/adapter/out/persistence`.
- Operations implemented: create, get by id, list active, update, and soft delete with `active = false`.
- Tests cover domain rules and application use cases.
- No Flyway migration was modified.

### shifts

`shifts` is implemented as a complete hexagonal catalog module backed by the existing `shifts` table in `V1__create_initial_schema.sql`.

Current state:

- Pure domain model exists in `shifts/domain/model/Shift.java`.
- Domain exceptions exist for duplicate name and missing shift.
- Stable domain error codes are `shift.duplicate-name` and `shift.not-found`.
- Input ports, commands, and results live in `shifts/domain/port/in`.
- Output repository port lives in `shifts/domain/port/out/ShiftRepositoryPort.java`.
- Application use cases are split by operation under `shifts/application/usecase`.
- REST adapter lives under `shifts/adapter/in/web`.
- Request and response DTOs live under `shifts/adapter/in/web/dto`.
- JPA entity, Spring Data repository, mapper, and persistence adapter live under `shifts/adapter/out/persistence`.
- Operations implemented: create, get by id, list active, update, and soft delete with `active = false`.
- Domain allows overnight shifts by permitting `endTime` before `startTime`, but rejects equal start/end times.
- Tests cover domain rules and application use cases.
- No Flyway migration was modified.

### roles

`roles` is implemented as a complete hexagonal catalog module backed by the existing `roles` table in `V1__create_initial_schema.sql`.

Current state:

- Pure domain model exists in `roles/domain/model/Role.java`.
- Domain exceptions exist for duplicate name and missing role.
- Stable domain error codes are `role.duplicate-name` and `role.not-found`.
- Input ports, commands, and results live in `roles/domain/port/in`.
- Output repository port lives in `roles/domain/port/out/RoleRepositoryPort.java`.
- Application use cases are split by operation under `roles/application/usecase`.
- REST adapter lives under `roles/adapter/in/web`.
- Request and response DTOs live under `roles/adapter/in/web/dto`.
- JPA entity, Spring Data repository, mapper, and persistence adapter live under `roles/adapter/out/persistence`.
- Operations implemented: create, get by id, list active, update, and soft delete with `active = false`.
- This module is catalog CRUD only; it does not seed ADMIN/SUPERVISOR/OPERADOR/CONSULTA and does not implement auth, JWT, users, or endpoint authorization.
- Tests cover domain rules and application use cases.
- No Flyway migration was modified.

### security_bootstrap

`security_bootstrap` is implemented as a narrow hexagonal bootstrap module for required security platform data.

Current state:

- Required roles `ADMIN`, `SUPERVISOR`, `OPERADOR`, and `CONSULTA` are seeded by append-only migration `V2__seed_required_roles.sql`.
- `V1__create_initial_schema.sql` remains unchanged.
- The initial `admin` user is created by application bootstrap after Flyway, not by static SQL.
- Runtime bootstrap can be disabled with `security.bootstrap.enabled=false`.
- If bootstrap is enabled and `admin` does not exist, `SECURITY_BOOTSTRAP_ADMIN_PASSWORD` is required.
- Existing `admin` is left unchanged and is not overwritten.
- Optional demo users require both `security.bootstrap.demo-users.enabled=true` and an active profile of `local`, `dev`, or `test`.
- Optional demo users default to `supervisor`, `operador`, and `consulta`, mapped to `SUPERVISOR`, `OPERADOR`, and `CONSULTA`.
- Demo user passwords are read from configuration/environment, hashed with BCrypt, and existing demo users are not overwritten.
- Passwords are hashed through `spring-security-crypto` BCrypt via `PasswordHashingPort`.
- No Spring Security web configuration, login endpoint, JWT generation/validation, filters, endpoint authorization, frontend, or GraphQL code was added.
- Domain model and ports live under `security_bootstrap/domain`.
- Application orchestration lives in `security_bootstrap/application/usecase/SecurityBootstrapService.java`.
- Startup adapter and properties live under `security_bootstrap/adapter/in/startup`.
- JDBC persistence adapters for existing `roles` and `users` tables live under `security_bootstrap/adapter/out/persistence`.
- Focused tests cover the use case, password hashing adapter, bootstrap properties, and domain redaction/validation.

### auth

`auth` is implemented as a narrow login/JWT module backed by the existing `users` and `roles` tables in `V1__create_initial_schema.sql`.

Current state:

- `POST /api/auth/login` is implemented in `auth/adapter/in/web/AuthRestController.java`.
- The REST controller calls the `LoginUseCase` input port.
- Login verifies raw credentials against `users.password_hash` through BCrypt.
- Login reads users and roles with a JDBC adapter against the existing V1 schema; no auth migration was created.
- Unknown username and wrong password both return generic invalid credentials behavior.
- Inactive users are rejected.
- Missing roles are rejected.
- Inactive roles are rejected.
- Login validation and malformed JSON failures return auth-specific `auth.validation-error` behavior.
- Successful login returns a safe response with `accessToken`, `tokenType`, `expiresAt`, and user summary.
- JWT access tokens are signed with HMAC-SHA256.
- JWT claims include `sub` as user ID, `userId`, `username`, `role`, `iat`, `exp`, and optional `iss`.
- `password_hash`, raw password, version fields, and secrets are excluded from API responses and JWT claims.
- Token configuration is bound through `security.jwt.secret`, `security.jwt.expiration`, and optional `security.jwt.issuer`.
- The auth module itself does not configure Spring Security filters or endpoint authorization; it only issues login responses and HMAC JWT access tokens.
- Focused tests cover login use case behavior, credential redaction, BCrypt verification, HMAC JWT claims, and safe web mapping.

### shared

Shared web and domain support exists.

Current state:

- `DomainException` and `DomainErrorType` live under `shared/domain/exception`.
- `GlobalExceptionHandler` maps domain and validation errors in the inbound web adapter.
- `ApiErrorResponse` is the standard REST error response.
- `HealthController` lives under the shared inbound web adapter.

## Frontend State

No Angular app has been created yet.

Current frontend decision:

- Future frontend stack is Angular + TypeScript.
- Future frontend code must live under `frontend/`.
- Backend remains at the repository root for now.
- Do not put Angular files under backend `src/`.
- Do not move the backend into `backend/` unless a separate restructure task is approved.
- Future Angular app architecture should use `frontend/src/app/core/`, `frontend/src/app/shared/`, and `frontend/src/app/features/`.
- Angular code should follow official `angular.dev` guidance for style, standalone components, HttpClient, functional interceptors, route guards, reactive forms, testing, and security.
- Backend hexagonal architecture remains unchanged.

## Existing Ports And Use Cases

### profiles input ports

- `CreateProfileUseCase`
- `GetProfileUseCase`
- `UpdateProfileUseCase`
- `DeleteProfileUseCase`

### profiles input command/result records

- `CreateProfileCommand`
- `UpdateProfileCommand`
- `ProfileResult`

### profiles output ports

- `ProfileRepositoryPort`

### profiles application use cases

- `CreateProfileService`
- `GetProfileService`
- `UpdateProfileService`
- `DeleteProfileService`

### container_types input ports

- `CreateContainerTypeUseCase`
- `GetContainerTypeUseCase`
- `UpdateContainerTypeUseCase`
- `DeleteContainerTypeUseCase`

### container_types input command/result records

- `CreateContainerTypeCommand`
- `UpdateContainerTypeCommand`
- `ContainerTypeResult`

### container_types output ports

- `ContainerTypeRepositoryPort`

### container_types application use cases

- `CreateContainerTypeService`
- `GetContainerTypeService`
- `UpdateContainerTypeService`
- `DeleteContainerTypeService`

### containers input ports

- `CreateContainerUseCase`
- `GetContainerUseCase`
- `UpdateContainerUseCase`
- `DeleteContainerUseCase`

### containers input command/result records

- `CreateContainerCommand`
- `UpdateContainerCommand`
- `ContainerResult`

### containers output ports

- `ContainerRepositoryPort`

### containers application use cases

- `CreateContainerService`
- `GetContainerService`
- `UpdateContainerService`
- `DeleteContainerService`

### machines input ports

- `CreateMachineUseCase`
- `GetMachineUseCase`
- `UpdateMachineUseCase`
- `DeleteMachineUseCase`

### machines input command/result records

- `CreateMachineCommand`
- `UpdateMachineCommand`
- `MachineResult`

### machines output ports

- `MachineRepositoryPort`

### machines application use cases

- `CreateMachineService`
- `GetMachineService`
- `UpdateMachineService`
- `DeleteMachineService`

### shifts input ports

- `CreateShiftUseCase`
- `GetShiftUseCase`
- `UpdateShiftUseCase`
- `DeleteShiftUseCase`

### shifts input command/result records

- `CreateShiftCommand`
- `UpdateShiftCommand`
- `ShiftResult`

### shifts output ports

- `ShiftRepositoryPort`

### shifts application use cases

- `CreateShiftService`
- `GetShiftService`
- `UpdateShiftService`
- `DeleteShiftService`

### roles input ports

- `CreateRoleUseCase`
- `GetRoleUseCase`
- `UpdateRoleUseCase`
- `DeleteRoleUseCase`

### roles input command/result records

- `CreateRoleCommand`
- `UpdateRoleCommand`
- `RoleResult`

### roles output ports

- `RoleRepositoryPort`

### roles application use cases

- `CreateRoleService`
- `GetRoleService`
- `UpdateRoleService`
- `DeleteRoleService`

### security_bootstrap input ports

- `RunSecurityBootstrapUseCase`

### security_bootstrap input command/result records

- `SecurityBootstrapCommand`
- `SecurityBootstrapResult`

### security_bootstrap output ports

- `PasswordHashingPort`
- `SecurityBootstrapRoleLookupPort`
- `SecurityBootstrapUserPort`

### security_bootstrap application use cases

- `SecurityBootstrapService`

### auth input ports

- `LoginUseCase`

### auth input command/result records

- `LoginCommand`
- `LoginResult`

### auth output ports

- `AuthUserLookupPort`
- `PasswordVerificationPort`
- `JwtTokenPort`

### auth application use cases

- `LoginService`

## Established Patterns

- One application service class per use case.
- Controllers depend on domain input ports, not application classes directly when an input port exists.
- Application services inject output ports, not adapters or Spring Data repositories.
- Persistence adapters implement output ports and translate between domain models and JPA entities.
- JPA entities stay inside `adapter/out/persistence`.
- REST request and response DTOs stay inside `adapter/in/web/dto`.
- Application result mapping lives in `application/mapper`.
- Domain exceptions extend `DomainException` and expose a `DomainErrorType` plus a stable error code.
- Catalog domain error codes use lowercase dot-separated values such as `machine.duplicate-name`, `shift.not-found`, and `role.duplicate-name`.
- `DomainErrorType.NOT_FOUND` maps to not found behavior.
- `DomainErrorType.CONFLICT` maps to duplicate/conflict behavior.
- `DomainErrorType.BUSINESS_RULE` maps to business-rule rejection behavior.
- Cross-module references should prefer scalar IDs unless an explicit aggregate boundary requires otherwise. `containers` references `container_types` through `containerTypeId` and `container_type_id`, not through a JPA entity relationship.
- Runtime bootstrap code that needs credentials must keep secrets out of migrations, source code, logs, and result objects.
- Security bootstrap demo users require both an explicit enable flag and an allowed active profile: `local`, `dev`, or `test`.
- Auth login keeps username-not-found and wrong-password failures indistinguishable with `auth.invalid-credentials`.
- Auth JWT claims must stay limited to safe identity/role data; never include `password_hash`, raw passwords, secrets, or unnecessary personal data.
- Endpoint authorization lives in the `security` module; auth login remains token issuance only.
- Use ArchUnit to enforce domain purity, application independence from adapters, and inbound/outbound adapter separation.
- Agent and skill files must include concrete wrong code, correct code, the bug caused by the wrong code, and exact preferred structure or response format.
- Examples in `.agents` use `com.empresa.app` by convention. Adapt examples to `com.example.company` before editing source files.

## GraphQL And MCP State

GraphQL is not configured at runtime.

Current policy:

- Do not add `spring-boot-starter-graphql` unless explicitly requested.
- When GraphQL is enabled, schema files belong under `src/main/resources/graphql/`.
- Resolver classes belong under `adapter/in/web/graphql`.
- Resolvers must call domain input ports and must not call repositories.
- Resolvers must not return JPA entities.
- Use `.agents/commands/sync-graphql-schema.md` and `$graphql-mcp-workflow` to summarize schema files, resolvers, input ports, N+1 risks, and MCP/context drift.

Current schema state:

- No GraphQL schema files are present.
- No GraphQL resolver files are present.
- No resolver-to-port mappings exist yet.

## Agent System State

Primary project agents:

- `hexagonal-domain-developer`
- `hexagonal-application-developer`
- `hexagonal-persistence-adapter`
- `hexagonal-web-adapter`
- `hexagonal-graphql-adapter`
- `hexagonal-exception-handler`
- `hexagonal-security-reviewer`
- `hexagonal-test-engineer`
- `frontend-developer`

Primary project skills:

- `$create-hexagonal-feature`
- `$debug-hexagonal-issue`
- `$enforce-hexagonal-boundaries`
- `$refactor-to-hexagonal`
- `$graphql-mcp-workflow`
- `$review-hexagonal-changes`
- `$explore-codebase`
- `$create-angular-feature`
- `$create-angular-auth-flow`
- `$review-angular-changes`

## Things Tried And Discarded

- The earlier layered Spring package structure (`controller`, `service`, `repository`, `entity`, `dto`, `mapper`) was replaced for migrated code by feature-based hexagonal packages.
- Spring-specific agents and skills were replaced by hexagonal agents and skills tailored to this backend.
- Ports outside `domain/port` were rejected for this repository. The current standard is `domain/port/in` and `domain/port/out`.
- Generic agent advice was rejected. Agent guidance must use concrete wrong code, correct code, the exact bug, and the preferred structure.
- GraphQL runtime setup was deferred. Only the schema sync workflow exists until GraphQL is explicitly requested.

## Verification Baseline

Known meaningful verification commands:

- `.\gradlew.bat compileJava testClasses`
- `.\gradlew.bat test --tests "*CuttingQuantitiesTest"`
- `.\gradlew.bat test --tests "*SecurityBootstrap*"`
- `.\gradlew.bat test --tests "*Auth*"`
- `.\gradlew.bat test --tests "*HexagonalArchitectureTest"`
- `.\gradlew.bat test`

The architecture boundary test is:

- `C:\Donatello\company\src\test\java\com\example\company\architecture\HexagonalArchitectureTest.java`
