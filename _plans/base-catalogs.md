# Plan: Base Catalogs Module

## Context

Implement only the base catalog work that belongs to `_specs/base-catalogs.md`: `machines`, `shifts`, and the `roles` catalog. Keep `container_types` and `containers` as already implemented modules; only add focused missing tests for them if needed. Do not implement auth/JWT, endpoint authorization, users, role seeding, frontend, GraphQL, or operational lookup endpoints in this plan.

---

## Scope Boundaries

### Modules To Implement Now

1. `machines`
2. `shifts`
3. `roles` catalog/base-data module
4. Focused application use case tests for existing `container_types` and `containers`

### Existing Modules To Leave Untouched

1. `container_types`
   - Already has domain, ports, application use cases, REST adapter, persistence adapter, and domain test.
   - Do not duplicate source files.
   - Do not recreate REST CRUD or persistence.
2. `containers`
   - Already has domain, ports, application use cases, REST adapter, persistence adapter, and domain test.
   - Do not duplicate source files.
   - Keep `containerTypeId` as a scalar `Long`.
   - Do not create a JPA relationship to `ContainerTypeJpaEntity`.

### Future Specs And Tasks

1. `role-seeding` spec for required system roles, preferably with an append-only migration such as `V2__seed_default_roles.sql`.
2. `users` spec.
3. `auth-jwt` spec.
4. `endpoint-authorization` spec.
5. Operational lookup endpoints for production flows.
6. Optional security error response spec.

---

## Phase 1 - Machines Catalog

### 1.1 Create Machine Domain Model

**File:** `src/main/java/com/example/company/machines/domain/model/Machine.java`

Create a pure Java domain model with:

- `Long id`
- `String name`
- `boolean active`
- `create(String name)`
- `restore(Long id, String name, boolean active)`
- `update(String name)`
- `deactivate()`
- getters as domain-style methods: `id()`, `name()`, `active()`

Rules:

- `name` is required.
- `name` is trimmed.
- `name` maximum length is 80.
- new machines default to `active = true`.
- soft delete sets `active = false`.
- no Spring, JPA, DTO, adapter, or repository imports.

### 1.2 Create Machine Domain Exceptions

**Files:**

- `src/main/java/com/example/company/machines/domain/exception/DuplicateMachineNameException.java`
- `src/main/java/com/example/company/machines/domain/exception/MachineNotFoundException.java`

Use existing shared domain exception style:

- duplicate name maps to `DomainErrorType.CONFLICT`.
- not found maps to `DomainErrorType.NOT_FOUND`.
- use stable lowercase dot-separated error codes: `machine.duplicate-name` and `machine.not-found`.

### 1.3 Create Machine Input Ports And Records

**Files:**

- `src/main/java/com/example/company/machines/domain/port/in/CreateMachineCommand.java`
- `src/main/java/com/example/company/machines/domain/port/in/CreateMachineUseCase.java`
- `src/main/java/com/example/company/machines/domain/port/in/DeleteMachineUseCase.java`
- `src/main/java/com/example/company/machines/domain/port/in/GetMachineUseCase.java`
- `src/main/java/com/example/company/machines/domain/port/in/MachineResult.java`
- `src/main/java/com/example/company/machines/domain/port/in/UpdateMachineCommand.java`
- `src/main/java/com/example/company/machines/domain/port/in/UpdateMachineUseCase.java`

Required operations:

- create
- get by id
- list active
- update
- soft delete

Do not add authorization code here; role rules remain documented until the endpoint-authorization spec.

### 1.4 Create Machine Output Port

**File:** `src/main/java/com/example/company/machines/domain/port/out/MachineRepositoryPort.java`

Methods:

- `List<Machine> findAllActiveOrderByNameAsc()`
- `Optional<Machine> findActiveById(Long id)`
- `boolean existsByName(String name)`
- `boolean existsByNameAndIdNot(String name, Long id)`
- `Machine save(Machine machine)`

### 1.5 Create Machine Application Mapper

**File:** `src/main/java/com/example/company/machines/application/mapper/MachineResultMapper.java`

Map domain `Machine` to `MachineResult`.

### 1.6 Create Machine Application Use Cases

**Files:**

- `src/main/java/com/example/company/machines/application/usecase/CreateMachineService.java`
- `src/main/java/com/example/company/machines/application/usecase/DeleteMachineService.java`
- `src/main/java/com/example/company/machines/application/usecase/GetMachineService.java`
- `src/main/java/com/example/company/machines/application/usecase/UpdateMachineService.java`

Rules:

- one service class per use case group, matching existing catalog style.
- services implement domain input ports.
- services inject `MachineRepositoryPort`.
- write transactions on create, update, and delete.
- read-only transactions on get/list.
- create rejects duplicate names.
- update rejects duplicate names for another id.
- get/update/delete operate on active records only.
- delete calls `deactivate()` and saves.

### 1.7 Create Machine Persistence Adapter

**Files:**

- `src/main/java/com/example/company/machines/adapter/out/persistence/MachineJpaEntity.java`
- `src/main/java/com/example/company/machines/adapter/out/persistence/MachinePersistenceAdapter.java`
- `src/main/java/com/example/company/machines/adapter/out/persistence/MachinePersistenceMapper.java`
- `src/main/java/com/example/company/machines/adapter/out/persistence/SpringDataMachineRepository.java`

Map to the existing `machines` table:

- `id`
- `name`
- `active`
- `version`
- `created_at`
- `updated_at`

Rules:

- do not modify Flyway.
- use `@Version`.
- set timestamps with `@PrePersist` and `@PreUpdate`, matching existing catalog adapters.
- return domain models from the adapter.
- repository stays package-local unless existing style requires otherwise.

### 1.8 Create Machine REST Adapter

**Files:**

- `src/main/java/com/example/company/machines/adapter/in/web/MachineRestController.java`
- `src/main/java/com/example/company/machines/adapter/in/web/MachineWebMapper.java`
- `src/main/java/com/example/company/machines/adapter/in/web/dto/MachineCreateRequest.java`
- `src/main/java/com/example/company/machines/adapter/in/web/dto/MachineResponse.java`
- `src/main/java/com/example/company/machines/adapter/in/web/dto/MachineUpdateRequest.java`

Endpoints:

- `GET /api/machines`
- `GET /api/machines/{id}`
- `POST /api/machines`
- `PUT /api/machines/{id}`
- `DELETE /api/machines/{id}`

Rules:

- controller depends on input ports.
- request DTOs use Jakarta validation.
- response DTO does not expose JPA.
- create returns HTTP 201.
- delete returns HTTP 204.
- do not add security annotations in this plan.

### 1.9 Add Machine Tests

**Files:**

- `src/test/java/com/example/company/machines/domain/model/MachineTest.java`
- `src/test/java/com/example/company/machines/application/usecase/CreateMachineServiceTest.java`
- `src/test/java/com/example/company/machines/application/usecase/UpdateMachineServiceTest.java`
- `src/test/java/com/example/company/machines/application/usecase/DeleteMachineServiceTest.java`
- `src/test/java/com/example/company/machines/application/usecase/GetMachineServiceTest.java`

Coverage:

- creation trims name and defaults active.
- blank name is rejected.
- too-long name is rejected.
- soft delete sets active false.
- create rejects duplicate name.
- update rejects missing machine.
- update rejects duplicate name for another id.
- delete rejects missing machine.
- list returns active records from the port.

---

## Phase 2 - Shifts Catalog

### 2.1 Create Shift Domain Model

**File:** `src/main/java/com/example/company/shifts/domain/model/Shift.java`

Create a pure Java domain model with:

- `Long id`
- `String name`
- `LocalTime startTime`
- `LocalTime endTime`
- `boolean active`
- `create(String name, LocalTime startTime, LocalTime endTime)`
- `restore(Long id, String name, LocalTime startTime, LocalTime endTime, boolean active)`
- `update(String name, LocalTime startTime, LocalTime endTime)`
- `deactivate()`
- getters as domain-style methods

Rules:

- `name` is required.
- `name` is trimmed.
- `name` maximum length is 90.
- `startTime` is required.
- `endTime` is required.
- `startTime` must not equal `endTime`.
- overnight shifts are allowed when `endTime` is before `startTime`.
- new shifts default to `active = true`.
- soft delete sets `active = false`.
- no Spring, JPA, DTO, adapter, or repository imports.

### 2.2 Create Shift Domain Exceptions

**Files:**

- `src/main/java/com/example/company/shifts/domain/exception/DuplicateShiftNameException.java`
- `src/main/java/com/example/company/shifts/domain/exception/ShiftNotFoundException.java`

Use `DomainErrorType.CONFLICT` for duplicate names and `DomainErrorType.NOT_FOUND` for missing shifts.

Use stable lowercase dot-separated error codes: `shift.duplicate-name` and `shift.not-found`.

### 2.3 Create Shift Input Ports And Records

**Files:**

- `src/main/java/com/example/company/shifts/domain/port/in/CreateShiftCommand.java`
- `src/main/java/com/example/company/shifts/domain/port/in/CreateShiftUseCase.java`
- `src/main/java/com/example/company/shifts/domain/port/in/DeleteShiftUseCase.java`
- `src/main/java/com/example/company/shifts/domain/port/in/GetShiftUseCase.java`
- `src/main/java/com/example/company/shifts/domain/port/in/ShiftResult.java`
- `src/main/java/com/example/company/shifts/domain/port/in/UpdateShiftCommand.java`
- `src/main/java/com/example/company/shifts/domain/port/in/UpdateShiftUseCase.java`

Required operations:

- create
- get by id
- list active
- update
- soft delete

### 2.4 Create Shift Output Port

**File:** `src/main/java/com/example/company/shifts/domain/port/out/ShiftRepositoryPort.java`

Methods:

- `List<Shift> findAllActiveOrderByNameAsc()`
- `Optional<Shift> findActiveById(Long id)`
- `boolean existsByName(String name)`
- `boolean existsByNameAndIdNot(String name, Long id)`
- `Shift save(Shift shift)`

### 2.5 Create Shift Application Mapper

**File:** `src/main/java/com/example/company/shifts/application/mapper/ShiftResultMapper.java`

Map domain `Shift` to `ShiftResult`.

### 2.6 Create Shift Application Use Cases

**Files:**

- `src/main/java/com/example/company/shifts/application/usecase/CreateShiftService.java`
- `src/main/java/com/example/company/shifts/application/usecase/DeleteShiftService.java`
- `src/main/java/com/example/company/shifts/application/usecase/GetShiftService.java`
- `src/main/java/com/example/company/shifts/application/usecase/UpdateShiftService.java`

Rules:

- services implement domain input ports.
- services inject `ShiftRepositoryPort`.
- write transactions on create, update, and delete.
- read-only transactions on get/list.
- create rejects duplicate names.
- update rejects duplicate names for another id.
- get/update/delete operate on active records only.
- delete calls `deactivate()` and saves.

### 2.7 Create Shift Persistence Adapter

**Files:**

- `src/main/java/com/example/company/shifts/adapter/out/persistence/ShiftJpaEntity.java`
- `src/main/java/com/example/company/shifts/adapter/out/persistence/ShiftPersistenceAdapter.java`
- `src/main/java/com/example/company/shifts/adapter/out/persistence/ShiftPersistenceMapper.java`
- `src/main/java/com/example/company/shifts/adapter/out/persistence/SpringDataShiftRepository.java`

Map to the existing `shifts` table:

- `id`
- `name`
- `start_time`
- `end_time`
- `active`
- `version`
- `created_at`
- `updated_at`

Rules:

- do not modify Flyway.
- use `LocalTime` for `start_time` and `end_time`.
- use `@Version`.
- set timestamps with `@PrePersist` and `@PreUpdate`.
- return domain models from the adapter.

### 2.8 Create Shift REST Adapter

**Files:**

- `src/main/java/com/example/company/shifts/adapter/in/web/ShiftRestController.java`
- `src/main/java/com/example/company/shifts/adapter/in/web/ShiftWebMapper.java`
- `src/main/java/com/example/company/shifts/adapter/in/web/dto/ShiftCreateRequest.java`
- `src/main/java/com/example/company/shifts/adapter/in/web/dto/ShiftResponse.java`
- `src/main/java/com/example/company/shifts/adapter/in/web/dto/ShiftUpdateRequest.java`

Endpoints:

- `GET /api/shifts`
- `GET /api/shifts/{id}`
- `POST /api/shifts`
- `PUT /api/shifts/{id}`
- `DELETE /api/shifts/{id}`

Rules:

- controller depends on input ports.
- request DTOs use Jakarta validation.
- request time fields use `LocalTime`.
- response DTO does not expose JPA.
- create returns HTTP 201.
- delete returns HTTP 204.
- do not add security annotations in this plan.

### 2.9 Add Shift Tests

**Files:**

- `src/test/java/com/example/company/shifts/domain/model/ShiftTest.java`
- `src/test/java/com/example/company/shifts/application/usecase/CreateShiftServiceTest.java`
- `src/test/java/com/example/company/shifts/application/usecase/UpdateShiftServiceTest.java`
- `src/test/java/com/example/company/shifts/application/usecase/DeleteShiftServiceTest.java`
- `src/test/java/com/example/company/shifts/application/usecase/GetShiftServiceTest.java`

Coverage:

- creation trims name and defaults active.
- blank name is rejected.
- missing start or end time is rejected.
- equal start and end time is rejected.
- overnight shift is allowed.
- soft delete sets active false.
- create rejects duplicate name.
- update rejects missing shift.
- update rejects duplicate name for another id.
- delete rejects missing shift.

---

## Phase 3 - Roles Catalog

### 3.1 Create Role Domain Model

**File:** `src/main/java/com/example/company/roles/domain/model/Role.java`

Create a pure Java role catalog model with:

- `Long id`
- `String name`
- `String description`
- `boolean active`
- `create(String name, String description)`
- `restore(Long id, String name, String description, boolean active)`
- `update(String name, String description)`
- `deactivate()`
- getters as domain-style methods

Rules:

- `name` is required.
- `name` is trimmed.
- `name` maximum length is 80.
- `description` is optional.
- non-null description is trimmed.
- description maximum length is 255.
- new roles default to `active = true`.
- soft delete sets `active = false`.
- this module is catalog/base data only.
- do not implement login, JWT, Spring Security, endpoint protection, password handling, or role seeding.

### 3.2 Create Role Domain Exceptions

**Files:**

- `src/main/java/com/example/company/roles/domain/exception/DuplicateRoleNameException.java`
- `src/main/java/com/example/company/roles/domain/exception/RoleNotFoundException.java`

Use `DomainErrorType.CONFLICT` for duplicate names and `DomainErrorType.NOT_FOUND` for missing roles.

Use stable lowercase dot-separated error codes: `role.duplicate-name` and `role.not-found`.

Do not add a role-assigned-to-user guard in this plan because `users` is out of scope. That policy belongs to a later users/security spec.

### 3.3 Create Role Input Ports And Records

**Files:**

- `src/main/java/com/example/company/roles/domain/port/in/CreateRoleCommand.java`
- `src/main/java/com/example/company/roles/domain/port/in/CreateRoleUseCase.java`
- `src/main/java/com/example/company/roles/domain/port/in/DeleteRoleUseCase.java`
- `src/main/java/com/example/company/roles/domain/port/in/GetRoleUseCase.java`
- `src/main/java/com/example/company/roles/domain/port/in/RoleResult.java`
- `src/main/java/com/example/company/roles/domain/port/in/UpdateRoleCommand.java`
- `src/main/java/com/example/company/roles/domain/port/in/UpdateRoleUseCase.java`

Required operations:

- create
- get by id
- list active
- update
- soft delete

### 3.4 Create Role Output Port

**File:** `src/main/java/com/example/company/roles/domain/port/out/RoleRepositoryPort.java`

Methods:

- `List<Role> findAllActiveOrderByNameAsc()`
- `Optional<Role> findActiveById(Long id)`
- `boolean existsByName(String name)`
- `boolean existsByNameAndIdNot(String name, Long id)`
- `Role save(Role role)`

### 3.5 Create Role Application Mapper

**File:** `src/main/java/com/example/company/roles/application/mapper/RoleResultMapper.java`

Map domain `Role` to `RoleResult`.

### 3.6 Create Role Application Use Cases

**Files:**

- `src/main/java/com/example/company/roles/application/usecase/CreateRoleService.java`
- `src/main/java/com/example/company/roles/application/usecase/DeleteRoleService.java`
- `src/main/java/com/example/company/roles/application/usecase/GetRoleService.java`
- `src/main/java/com/example/company/roles/application/usecase/UpdateRoleService.java`

Rules:

- services implement domain input ports.
- services inject `RoleRepositoryPort`.
- write transactions on create, update, and delete.
- read-only transactions on get/list.
- create rejects duplicate names.
- update rejects duplicate names for another id.
- get/update/delete operate on active records only.
- delete calls `deactivate()` and saves.
- do not seed `ADMIN`, `SUPERVISOR`, `OPERADOR`, or `CONSULTA` here.

### 3.7 Create Role Persistence Adapter

**Files:**

- `src/main/java/com/example/company/roles/adapter/out/persistence/RoleJpaEntity.java`
- `src/main/java/com/example/company/roles/adapter/out/persistence/RolePersistenceAdapter.java`
- `src/main/java/com/example/company/roles/adapter/out/persistence/RolePersistenceMapper.java`
- `src/main/java/com/example/company/roles/adapter/out/persistence/SpringDataRoleRepository.java`

Map to the existing `roles` table:

- `id`
- `name`
- `description`
- `active`
- `version`
- `created_at`
- `updated_at`

Rules:

- do not modify Flyway.
- do not create a seed migration in this plan.
- use `@Version`.
- set timestamps with `@PrePersist` and `@PreUpdate`.
- return domain models from the adapter.

### 3.8 Create Role REST Adapter

**Files:**

- `src/main/java/com/example/company/roles/adapter/in/web/RoleRestController.java`
- `src/main/java/com/example/company/roles/adapter/in/web/RoleWebMapper.java`
- `src/main/java/com/example/company/roles/adapter/in/web/dto/RoleCreateRequest.java`
- `src/main/java/com/example/company/roles/adapter/in/web/dto/RoleResponse.java`
- `src/main/java/com/example/company/roles/adapter/in/web/dto/RoleUpdateRequest.java`

Endpoints:

- `GET /api/roles`
- `GET /api/roles/{id}`
- `POST /api/roles`
- `PUT /api/roles/{id}`
- `DELETE /api/roles/{id}`

Rules:

- controller depends on input ports.
- request DTOs use Jakarta validation.
- response DTO does not expose JPA.
- create returns HTTP 201.
- delete returns HTTP 204.
- do not add security annotations in this plan.
- endpoint authorization belongs to a future spec.

### 3.9 Add Role Tests

**Files:**

- `src/test/java/com/example/company/roles/domain/model/RoleTest.java`
- `src/test/java/com/example/company/roles/application/usecase/CreateRoleServiceTest.java`
- `src/test/java/com/example/company/roles/application/usecase/UpdateRoleServiceTest.java`
- `src/test/java/com/example/company/roles/application/usecase/DeleteRoleServiceTest.java`
- `src/test/java/com/example/company/roles/application/usecase/GetRoleServiceTest.java`

Coverage:

- creation trims name and description.
- blank name is rejected.
- too-long name is rejected.
- too-long description is rejected.
- null description is accepted.
- soft delete sets active false.
- create rejects duplicate name.
- update rejects missing role.
- update rejects duplicate name for another id.
- delete rejects missing role.

---

## Phase 4 - Focused Tests For Existing Catalog Modules

### 4.1 Add Container Type Application Tests

**Files:**

- `src/test/java/com/example/company/container_types/application/usecase/CreateContainerTypeServiceTest.java`
- `src/test/java/com/example/company/container_types/application/usecase/UpdateContainerTypeServiceTest.java`
- `src/test/java/com/example/company/container_types/application/usecase/DeleteContainerTypeServiceTest.java`
- `src/test/java/com/example/company/container_types/application/usecase/GetContainerTypeServiceTest.java`

Reason:

- `container_types` already has a domain test.
- This phase adds focused use case coverage only.
- Do not modify `container_types` source code unless a test exposes an actual defect and the user separately approves implementation changes.

Coverage:

- create rejects duplicate name.
- update rejects missing type.
- update rejects duplicate name for another id.
- delete rejects missing type.
- delete deactivates and saves.
- list active delegates to repository port.

### 4.2 Add Container Application Tests

**Files:**

- `src/test/java/com/example/company/containers/application/usecase/CreateContainerServiceTest.java`
- `src/test/java/com/example/company/containers/application/usecase/UpdateContainerServiceTest.java`
- `src/test/java/com/example/company/containers/application/usecase/DeleteContainerServiceTest.java`
- `src/test/java/com/example/company/containers/application/usecase/GetContainerServiceTest.java`

Reason:

- `containers` already has a domain test.
- This phase adds focused use case coverage only.
- Do not modify `containers` source code unless a test exposes an actual defect and the user separately approves implementation changes.

Coverage:

- create rejects duplicate code.
- update rejects missing container.
- update rejects duplicate code for another id.
- delete rejects missing container.
- delete deactivates and saves.
- list active delegates to repository port.
- `containerTypeId` remains scalar and no JPA relationship is introduced.

---

## Phase 5 - Verification

### 5.1 Compile And Test Classes

**Command:**

```powershell
.\gradlew.bat compileJava testClasses
```

Purpose:

- verify Java compilation.
- verify test compilation.
- catch package, import, validation, and mapper wiring errors.

### 5.2 Run Targeted Domain And Use Case Tests

**Commands:**

```powershell
.\gradlew.bat test --tests "*MachineTest"
.\gradlew.bat test --tests "*ShiftTest"
.\gradlew.bat test --tests "*RoleTest"
.\gradlew.bat test --tests "*MachineServiceTest"
.\gradlew.bat test --tests "*ShiftServiceTest"
.\gradlew.bat test --tests "*RoleServiceTest"
.\gradlew.bat test --tests "*ContainerTypeServiceTest"
.\gradlew.bat test --tests "*ContainerServiceTest"
```

Purpose:

- verify catalog domain rules.
- verify duplicate checks.
- verify not-found behavior.
- verify soft delete behavior.

### 5.3 Run Architecture Test

**Command:**

```powershell
.\gradlew.bat test --tests "*HexagonalArchitectureTest"
```

Purpose:

- verify domain purity.
- verify application does not depend on adapters.
- verify inbound adapters do not depend on outbound adapters.

### 5.4 Review Git Diff

**Commands:**

```powershell
git diff -- _plans/base-catalogs.md
git status --short --branch
```

Purpose:

- confirm no Flyway migration was modified.
- confirm no out-of-scope files were changed.
- confirm `container_types` and `containers` source files were not duplicated.

---

## Implementation Order

1. Implement `machines` domain model.
2. Implement `machines` exceptions.
3. Implement `machines` input ports, commands, and result.
4. Implement `machines` output port.
5. Implement `machines` application mapper and use cases.
6. Implement `machines` persistence adapter.
7. Implement `machines` REST adapter.
8. Add `machines` tests.
9. Implement `shifts` domain model.
10. Implement `shifts` exceptions.
11. Implement `shifts` input ports, commands, and result.
12. Implement `shifts` output port.
13. Implement `shifts` application mapper and use cases.
14. Implement `shifts` persistence adapter.
15. Implement `shifts` REST adapter.
16. Add `shifts` tests.
17. Implement `roles` catalog domain model.
18. Implement `roles` exceptions.
19. Implement `roles` input ports, commands, and result.
20. Implement `roles` output port.
21. Implement `roles` application mapper and use cases.
22. Implement `roles` persistence adapter.
23. Implement `roles` REST adapter.
24. Add `roles` catalog tests.
25. Add focused application use case tests for `container_types`.
26. Add focused application use case tests for `containers`.
27. Run compile and test-class verification.
28. Run targeted tests.
29. Run architecture test.
30. Review git diff and status.

---

## Critical Files

| File | Action |
|------|--------|
| `src/main/java/com/example/company/machines/domain/model/Machine.java` | Create |
| `src/main/java/com/example/company/machines/domain/exception/DuplicateMachineNameException.java` | Create |
| `src/main/java/com/example/company/machines/domain/exception/MachineNotFoundException.java` | Create |
| `src/main/java/com/example/company/machines/domain/port/in/CreateMachineCommand.java` | Create |
| `src/main/java/com/example/company/machines/domain/port/in/CreateMachineUseCase.java` | Create |
| `src/main/java/com/example/company/machines/domain/port/in/DeleteMachineUseCase.java` | Create |
| `src/main/java/com/example/company/machines/domain/port/in/GetMachineUseCase.java` | Create |
| `src/main/java/com/example/company/machines/domain/port/in/MachineResult.java` | Create |
| `src/main/java/com/example/company/machines/domain/port/in/UpdateMachineCommand.java` | Create |
| `src/main/java/com/example/company/machines/domain/port/in/UpdateMachineUseCase.java` | Create |
| `src/main/java/com/example/company/machines/domain/port/out/MachineRepositoryPort.java` | Create |
| `src/main/java/com/example/company/machines/application/mapper/MachineResultMapper.java` | Create |
| `src/main/java/com/example/company/machines/application/usecase/CreateMachineService.java` | Create |
| `src/main/java/com/example/company/machines/application/usecase/DeleteMachineService.java` | Create |
| `src/main/java/com/example/company/machines/application/usecase/GetMachineService.java` | Create |
| `src/main/java/com/example/company/machines/application/usecase/UpdateMachineService.java` | Create |
| `src/main/java/com/example/company/machines/adapter/out/persistence/MachineJpaEntity.java` | Create |
| `src/main/java/com/example/company/machines/adapter/out/persistence/MachinePersistenceAdapter.java` | Create |
| `src/main/java/com/example/company/machines/adapter/out/persistence/MachinePersistenceMapper.java` | Create |
| `src/main/java/com/example/company/machines/adapter/out/persistence/SpringDataMachineRepository.java` | Create |
| `src/main/java/com/example/company/machines/adapter/in/web/MachineRestController.java` | Create |
| `src/main/java/com/example/company/machines/adapter/in/web/MachineWebMapper.java` | Create |
| `src/main/java/com/example/company/machines/adapter/in/web/dto/MachineCreateRequest.java` | Create |
| `src/main/java/com/example/company/machines/adapter/in/web/dto/MachineResponse.java` | Create |
| `src/main/java/com/example/company/machines/adapter/in/web/dto/MachineUpdateRequest.java` | Create |
| `src/main/java/com/example/company/shifts/domain/model/Shift.java` | Create |
| `src/main/java/com/example/company/shifts/domain/exception/DuplicateShiftNameException.java` | Create |
| `src/main/java/com/example/company/shifts/domain/exception/ShiftNotFoundException.java` | Create |
| `src/main/java/com/example/company/shifts/domain/port/in/CreateShiftCommand.java` | Create |
| `src/main/java/com/example/company/shifts/domain/port/in/CreateShiftUseCase.java` | Create |
| `src/main/java/com/example/company/shifts/domain/port/in/DeleteShiftUseCase.java` | Create |
| `src/main/java/com/example/company/shifts/domain/port/in/GetShiftUseCase.java` | Create |
| `src/main/java/com/example/company/shifts/domain/port/in/ShiftResult.java` | Create |
| `src/main/java/com/example/company/shifts/domain/port/in/UpdateShiftCommand.java` | Create |
| `src/main/java/com/example/company/shifts/domain/port/in/UpdateShiftUseCase.java` | Create |
| `src/main/java/com/example/company/shifts/domain/port/out/ShiftRepositoryPort.java` | Create |
| `src/main/java/com/example/company/shifts/application/mapper/ShiftResultMapper.java` | Create |
| `src/main/java/com/example/company/shifts/application/usecase/CreateShiftService.java` | Create |
| `src/main/java/com/example/company/shifts/application/usecase/DeleteShiftService.java` | Create |
| `src/main/java/com/example/company/shifts/application/usecase/GetShiftService.java` | Create |
| `src/main/java/com/example/company/shifts/application/usecase/UpdateShiftService.java` | Create |
| `src/main/java/com/example/company/shifts/adapter/out/persistence/ShiftJpaEntity.java` | Create |
| `src/main/java/com/example/company/shifts/adapter/out/persistence/ShiftPersistenceAdapter.java` | Create |
| `src/main/java/com/example/company/shifts/adapter/out/persistence/ShiftPersistenceMapper.java` | Create |
| `src/main/java/com/example/company/shifts/adapter/out/persistence/SpringDataShiftRepository.java` | Create |
| `src/main/java/com/example/company/shifts/adapter/in/web/ShiftRestController.java` | Create |
| `src/main/java/com/example/company/shifts/adapter/in/web/ShiftWebMapper.java` | Create |
| `src/main/java/com/example/company/shifts/adapter/in/web/dto/ShiftCreateRequest.java` | Create |
| `src/main/java/com/example/company/shifts/adapter/in/web/dto/ShiftResponse.java` | Create |
| `src/main/java/com/example/company/shifts/adapter/in/web/dto/ShiftUpdateRequest.java` | Create |
| `src/main/java/com/example/company/roles/domain/model/Role.java` | Create |
| `src/main/java/com/example/company/roles/domain/exception/DuplicateRoleNameException.java` | Create |
| `src/main/java/com/example/company/roles/domain/exception/RoleNotFoundException.java` | Create |
| `src/main/java/com/example/company/roles/domain/port/in/CreateRoleCommand.java` | Create |
| `src/main/java/com/example/company/roles/domain/port/in/CreateRoleUseCase.java` | Create |
| `src/main/java/com/example/company/roles/domain/port/in/DeleteRoleUseCase.java` | Create |
| `src/main/java/com/example/company/roles/domain/port/in/GetRoleUseCase.java` | Create |
| `src/main/java/com/example/company/roles/domain/port/in/RoleResult.java` | Create |
| `src/main/java/com/example/company/roles/domain/port/in/UpdateRoleCommand.java` | Create |
| `src/main/java/com/example/company/roles/domain/port/in/UpdateRoleUseCase.java` | Create |
| `src/main/java/com/example/company/roles/domain/port/out/RoleRepositoryPort.java` | Create |
| `src/main/java/com/example/company/roles/application/mapper/RoleResultMapper.java` | Create |
| `src/main/java/com/example/company/roles/application/usecase/CreateRoleService.java` | Create |
| `src/main/java/com/example/company/roles/application/usecase/DeleteRoleService.java` | Create |
| `src/main/java/com/example/company/roles/application/usecase/GetRoleService.java` | Create |
| `src/main/java/com/example/company/roles/application/usecase/UpdateRoleService.java` | Create |
| `src/main/java/com/example/company/roles/adapter/out/persistence/RoleJpaEntity.java` | Create |
| `src/main/java/com/example/company/roles/adapter/out/persistence/RolePersistenceAdapter.java` | Create |
| `src/main/java/com/example/company/roles/adapter/out/persistence/RolePersistenceMapper.java` | Create |
| `src/main/java/com/example/company/roles/adapter/out/persistence/SpringDataRoleRepository.java` | Create |
| `src/main/java/com/example/company/roles/adapter/in/web/RoleRestController.java` | Create |
| `src/main/java/com/example/company/roles/adapter/in/web/RoleWebMapper.java` | Create |
| `src/main/java/com/example/company/roles/adapter/in/web/dto/RoleCreateRequest.java` | Create |
| `src/main/java/com/example/company/roles/adapter/in/web/dto/RoleResponse.java` | Create |
| `src/main/java/com/example/company/roles/adapter/in/web/dto/RoleUpdateRequest.java` | Create |
| `src/test/java/com/example/company/machines/domain/model/MachineTest.java` | Create |
| `src/test/java/com/example/company/machines/application/usecase/CreateMachineServiceTest.java` | Create |
| `src/test/java/com/example/company/machines/application/usecase/UpdateMachineServiceTest.java` | Create |
| `src/test/java/com/example/company/machines/application/usecase/DeleteMachineServiceTest.java` | Create |
| `src/test/java/com/example/company/machines/application/usecase/GetMachineServiceTest.java` | Create |
| `src/test/java/com/example/company/shifts/domain/model/ShiftTest.java` | Create |
| `src/test/java/com/example/company/shifts/application/usecase/CreateShiftServiceTest.java` | Create |
| `src/test/java/com/example/company/shifts/application/usecase/UpdateShiftServiceTest.java` | Create |
| `src/test/java/com/example/company/shifts/application/usecase/DeleteShiftServiceTest.java` | Create |
| `src/test/java/com/example/company/shifts/application/usecase/GetShiftServiceTest.java` | Create |
| `src/test/java/com/example/company/roles/domain/model/RoleTest.java` | Create |
| `src/test/java/com/example/company/roles/application/usecase/CreateRoleServiceTest.java` | Create |
| `src/test/java/com/example/company/roles/application/usecase/UpdateRoleServiceTest.java` | Create |
| `src/test/java/com/example/company/roles/application/usecase/DeleteRoleServiceTest.java` | Create |
| `src/test/java/com/example/company/roles/application/usecase/GetRoleServiceTest.java` | Create |
| `src/test/java/com/example/company/container_types/application/usecase/CreateContainerTypeServiceTest.java` | Create |
| `src/test/java/com/example/company/container_types/application/usecase/UpdateContainerTypeServiceTest.java` | Create |
| `src/test/java/com/example/company/container_types/application/usecase/DeleteContainerTypeServiceTest.java` | Create |
| `src/test/java/com/example/company/container_types/application/usecase/GetContainerTypeServiceTest.java` | Create |
| `src/test/java/com/example/company/containers/application/usecase/CreateContainerServiceTest.java` | Create |
| `src/test/java/com/example/company/containers/application/usecase/UpdateContainerServiceTest.java` | Create |
| `src/test/java/com/example/company/containers/application/usecase/DeleteContainerServiceTest.java` | Create |
| `src/test/java/com/example/company/containers/application/usecase/GetContainerServiceTest.java` | Create |
| `src/main/resources/db/migration/V1__create_initial_schema.sql` | Leave untouched |
| `src/main/resources/db/migration/V2__seed_default_roles.sql` | Do not create in this plan |
| `src/main/java/com/example/company/container_types/**` | Leave source untouched |
| `src/main/java/com/example/company/containers/**` | Leave source untouched |

---

## Files Expected To Be Modified

No existing source or migration files are expected to be modified.

Expected modifications are limited to adding new files for:

- `machines`
- `shifts`
- `roles`
- focused test-only coverage under `container_types`
- focused test-only coverage under `containers`

---

## Intentionally Out Of Scope

- JWT.
- login.
- password handling.
- Spring Security.
- method security.
- endpoint authorization implementation.
- users implementation.
- role seeding implementation.
- `V2__seed_default_roles.sql`.
- frontend work.
- GraphQL.
- operational lookup endpoints, except as a future task.
- modifying `V1__create_initial_schema.sql`.
- duplicating `container_types` source.
- duplicating `containers` source.
- adding a JPA relationship between `ContainerJpaEntity` and `ContainerTypeJpaEntity`.
