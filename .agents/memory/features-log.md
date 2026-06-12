# Features Log

Append completed features and changes here. Entries are append-only and use ISO dates.

## 2026-06-11 [Hexagonal profiles pilot]

**Files created:**
- `C:\Donatello\company\src\main\java\com\example\company\profiles\domain\model\Profile.java`
- `C:\Donatello\company\src\main\java\com\example\company\profiles\domain\exception\DuplicateProfileCodeException.java`
- `C:\Donatello\company\src\main\java\com\example\company\profiles\domain\exception\ProfileNotFoundException.java`
- `C:\Donatello\company\src\main\java\com\example\company\profiles\domain\port\in\CreateProfileCommand.java`
- `C:\Donatello\company\src\main\java\com\example\company\profiles\domain\port\in\CreateProfileUseCase.java`
- `C:\Donatello\company\src\main\java\com\example\company\profiles\domain\port\in\DeleteProfileUseCase.java`
- `C:\Donatello\company\src\main\java\com\example\company\profiles\domain\port\in\GetProfileUseCase.java`
- `C:\Donatello\company\src\main\java\com\example\company\profiles\domain\port\in\ProfileResult.java`
- `C:\Donatello\company\src\main\java\com\example\company\profiles\domain\port\in\UpdateProfileCommand.java`
- `C:\Donatello\company\src\main\java\com\example\company\profiles\domain\port\in\UpdateProfileUseCase.java`
- `C:\Donatello\company\src\main\java\com\example\company\profiles\domain\port\out\ProfileRepositoryPort.java`
- `C:\Donatello\company\src\main\java\com\example\company\profiles\application\usecase\CreateProfileService.java`
- `C:\Donatello\company\src\main\java\com\example\company\profiles\application\usecase\DeleteProfileService.java`
- `C:\Donatello\company\src\main\java\com\example\company\profiles\application\usecase\GetProfileService.java`
- `C:\Donatello\company\src\main\java\com\example\company\profiles\application\usecase\UpdateProfileService.java`
- `C:\Donatello\company\src\main\java\com\example\company\profiles\application\mapper\ProfileResultMapper.java`
- `C:\Donatello\company\src\main\java\com\example\company\profiles\adapter\in\web\ProfileRestController.java`
- `C:\Donatello\company\src\main\java\com\example\company\profiles\adapter\in\web\ProfileWebMapper.java`
- `C:\Donatello\company\src\main\java\com\example\company\profiles\adapter\in\web\dto\ProfileCreateRequest.java`
- `C:\Donatello\company\src\main\java\com\example\company\profiles\adapter\in\web\dto\ProfileResponse.java`
- `C:\Donatello\company\src\main\java\com\example\company\profiles\adapter\in\web\dto\ProfileUpdateRequest.java`
- `C:\Donatello\company\src\main\java\com\example\company\profiles\adapter\out\persistence\ProfileJpaEntity.java`
- `C:\Donatello\company\src\main\java\com\example\company\profiles\adapter\out\persistence\ProfilePersistenceAdapter.java`
- `C:\Donatello\company\src\main\java\com\example\company\profiles\adapter\out\persistence\ProfilePersistenceMapper.java`
- `C:\Donatello\company\src\main\java\com\example\company\profiles\adapter\out\persistence\SpringDataProfileRepository.java`
- `C:\Donatello\company\src\main\java\com\example\company\cutting\domain\model\CuttingQuantities.java`
- `C:\Donatello\company\src\main\java\com\example\company\shared\domain\exception\DomainException.java`
- `C:\Donatello\company\src\main\java\com\example\company\shared\domain\exception\DomainErrorType.java`
- `C:\Donatello\company\src\main\java\com\example\company\shared\adapter\in\web\ApiErrorResponse.java`
- `C:\Donatello\company\src\main\java\com\example\company\shared\adapter\in\web\GlobalExceptionHandler.java`
- `C:\Donatello\company\src\main\java\com\example\company\shared\adapter\in\web\HealthController.java`
- `C:\Donatello\company\src\test\java\com\example\company\architecture\HexagonalArchitectureTest.java`
- `C:\Donatello\company\src\test\java\com\example\company\cutting\domain\model\CuttingQuantitiesTest.java`

**Files modified:**
- `C:\Donatello\company\build.gradle`
- `C:\Donatello\company\AGENTS.md`
- `C:\Donatello\company\ARCHITECTURE.md`
- `C:\Donatello\company\.agents\settings.json`

**Ports added:**
- Input: `CreateProfileUseCase`, `GetProfileUseCase`, `UpdateProfileUseCase`, `DeleteProfileUseCase`.
- Output: `ProfileRepositoryPort`.

**Tests added:**
- `CuttingQuantitiesTest` covers the cutting quantity invariant.
- `HexagonalArchitectureTest` covers domain purity, application-to-adapter isolation, and inbound/outbound adapter separation.

**Notes:** `profiles` is the current hexagonal pilot. Existing Flyway migration history remains append-only.

## 2026-06-11 [Persistent agent memory system]

**Files created:**
- `C:\Donatello\company\.agents\memory\project-context.md`
- `C:\Donatello\company\.agents\memory\issues-log.md`
- `C:\Donatello\company\.agents\memory\decisions-log.md`
- `C:\Donatello\company\.agents\memory\features-log.md`

**Files modified:**
- `C:\Donatello\company\AGENTS.md`
- `C:\Donatello\company\.agents\commands\new-feature.md`
- `C:\Donatello\company\.agents\commands\fix-issue.md`
- `C:\Donatello\company\.agents\commands\code-review.md`
- `C:\Donatello\company\.agents\commands\sync-graphql-schema.md`

**Ports added:** None.

**Tests added:** None. Verified by checking that the memory files exist and that `AGENTS.md` plus the four requested commands reference the correct memory files.

**Notes:** `project-context.md` is a living snapshot. `issues-log.md`, `decisions-log.md`, and `features-log.md` are append-only logs.

## 2026-06-11 [Container types hexagonal feature]

**Files created:**
- `C:\Donatello\company\src\main\java\com\example\company\container_types\domain\model\ContainerType.java`
- `C:\Donatello\company\src\main\java\com\example\company\container_types\domain\exception\ContainerTypeNotFoundException.java`
- `C:\Donatello\company\src\main\java\com\example\company\container_types\domain\exception\DuplicateContainerTypeNameException.java`
- `C:\Donatello\company\src\main\java\com\example\company\container_types\domain\port\in\ContainerTypeResult.java`
- `C:\Donatello\company\src\main\java\com\example\company\container_types\domain\port\in\CreateContainerTypeCommand.java`
- `C:\Donatello\company\src\main\java\com\example\company\container_types\domain\port\in\CreateContainerTypeUseCase.java`
- `C:\Donatello\company\src\main\java\com\example\company\container_types\domain\port\in\DeleteContainerTypeUseCase.java`
- `C:\Donatello\company\src\main\java\com\example\company\container_types\domain\port\in\GetContainerTypeUseCase.java`
- `C:\Donatello\company\src\main\java\com\example\company\container_types\domain\port\in\UpdateContainerTypeCommand.java`
- `C:\Donatello\company\src\main\java\com\example\company\container_types\domain\port\in\UpdateContainerTypeUseCase.java`
- `C:\Donatello\company\src\main\java\com\example\company\container_types\domain\port\out\ContainerTypeRepositoryPort.java`
- `C:\Donatello\company\src\main\java\com\example\company\container_types\application\mapper\ContainerTypeResultMapper.java`
- `C:\Donatello\company\src\main\java\com\example\company\container_types\application\usecase\CreateContainerTypeService.java`
- `C:\Donatello\company\src\main\java\com\example\company\container_types\application\usecase\DeleteContainerTypeService.java`
- `C:\Donatello\company\src\main\java\com\example\company\container_types\application\usecase\GetContainerTypeService.java`
- `C:\Donatello\company\src\main\java\com\example\company\container_types\application\usecase\UpdateContainerTypeService.java`
- `C:\Donatello\company\src\main\java\com\example\company\container_types\adapter\in\web\ContainerTypeRestController.java`
- `C:\Donatello\company\src\main\java\com\example\company\container_types\adapter\in\web\ContainerTypeWebMapper.java`
- `C:\Donatello\company\src\main\java\com\example\company\container_types\adapter\in\web\dto\ContainerTypeCreateRequest.java`
- `C:\Donatello\company\src\main\java\com\example\company\container_types\adapter\in\web\dto\ContainerTypeResponse.java`
- `C:\Donatello\company\src\main\java\com\example\company\container_types\adapter\in\web\dto\ContainerTypeUpdateRequest.java`
- `C:\Donatello\company\src\main\java\com\example\company\container_types\adapter\out\persistence\ContainerTypeJpaEntity.java`
- `C:\Donatello\company\src\main\java\com\example\company\container_types\adapter\out\persistence\ContainerTypePersistenceAdapter.java`
- `C:\Donatello\company\src\main\java\com\example\company\container_types\adapter\out\persistence\ContainerTypePersistenceMapper.java`
- `C:\Donatello\company\src\main\java\com\example\company\container_types\adapter\out\persistence\SpringDataContainerTypeRepository.java`
- `C:\Donatello\company\src\test\java\com\example\company\container_types\domain\model\ContainerTypeTest.java`

**Files modified:**
- `C:\Donatello\company\.agents\memory\project-context.md`
- `C:\Donatello\company\.agents\memory\features-log.md`

**Ports added:**
- Input: `CreateContainerTypeUseCase`, `GetContainerTypeUseCase`, `UpdateContainerTypeUseCase`, `DeleteContainerTypeUseCase`.
- Output: `ContainerTypeRepositoryPort`.

**Tests added:** `ContainerTypeTest` covers creation, blank-name rejection, and soft delete behavior.

**Notes:** Uses the existing `container_types` table from `V1__create_initial_schema.sql`. No migration was modified.

## 2026-06-11 [Containers hexagonal feature]

**Files created:**
- `C:\Donatello\company\src\main\java\com\example\company\containers\domain\model\Container.java`
- `C:\Donatello\company\src\main\java\com\example\company\containers\domain\exception\ContainerNotFoundException.java`
- `C:\Donatello\company\src\main\java\com\example\company\containers\domain\exception\DuplicateContainerCodeException.java`
- `C:\Donatello\company\src\main\java\com\example\company\containers\domain\port\in\ContainerResult.java`
- `C:\Donatello\company\src\main\java\com\example\company\containers\domain\port\in\CreateContainerCommand.java`
- `C:\Donatello\company\src\main\java\com\example\company\containers\domain\port\in\CreateContainerUseCase.java`
- `C:\Donatello\company\src\main\java\com\example\company\containers\domain\port\in\DeleteContainerUseCase.java`
- `C:\Donatello\company\src\main\java\com\example\company\containers\domain\port\in\GetContainerUseCase.java`
- `C:\Donatello\company\src\main\java\com\example\company\containers\domain\port\in\UpdateContainerCommand.java`
- `C:\Donatello\company\src\main\java\com\example\company\containers\domain\port\in\UpdateContainerUseCase.java`
- `C:\Donatello\company\src\main\java\com\example\company\containers\domain\port\out\ContainerRepositoryPort.java`
- `C:\Donatello\company\src\main\java\com\example\company\containers\application\mapper\ContainerResultMapper.java`
- `C:\Donatello\company\src\main\java\com\example\company\containers\application\usecase\CreateContainerService.java`
- `C:\Donatello\company\src\main\java\com\example\company\containers\application\usecase\DeleteContainerService.java`
- `C:\Donatello\company\src\main\java\com\example\company\containers\application\usecase\GetContainerService.java`
- `C:\Donatello\company\src\main\java\com\example\company\containers\application\usecase\UpdateContainerService.java`
- `C:\Donatello\company\src\main\java\com\example\company\containers\adapter\in\web\ContainerRestController.java`
- `C:\Donatello\company\src\main\java\com\example\company\containers\adapter\in\web\ContainerWebMapper.java`
- `C:\Donatello\company\src\main\java\com\example\company\containers\adapter\in\web\dto\ContainerCreateRequest.java`
- `C:\Donatello\company\src\main\java\com\example\company\containers\adapter\in\web\dto\ContainerResponse.java`
- `C:\Donatello\company\src\main\java\com\example\company\containers\adapter\in\web\dto\ContainerUpdateRequest.java`
- `C:\Donatello\company\src\main\java\com\example\company\containers\adapter\out\persistence\ContainerJpaEntity.java`
- `C:\Donatello\company\src\main\java\com\example\company\containers\adapter\out\persistence\ContainerPersistenceAdapter.java`
- `C:\Donatello\company\src\main\java\com\example\company\containers\adapter\out\persistence\ContainerPersistenceMapper.java`
- `C:\Donatello\company\src\main\java\com\example\company\containers\adapter\out\persistence\SpringDataContainerRepository.java`
- `C:\Donatello\company\src\test\java\com\example\company\containers\domain\model\ContainerTest.java`

**Files modified:**
- `C:\Donatello\company\.agents\memory\project-context.md`
- `C:\Donatello\company\.agents\memory\features-log.md`

**Ports added:**
- Input: `CreateContainerUseCase`, `GetContainerUseCase`, `UpdateContainerUseCase`, `DeleteContainerUseCase`.
- Output: `ContainerRepositoryPort`.

**Tests added:** `ContainerTest` covers creation with `containerTypeId`, missing type id rejection, blank-code rejection, and soft delete behavior.

**Notes:** Uses the existing `containers` table from `V1__create_initial_schema.sql`. `Container` and `ContainerJpaEntity` store `containerTypeId` as a scalar `Long`; no JPA relationship to `ContainerTypeJpaEntity` was created.

## 2026-06-12 [Base catalogs implementation]

**Files created:**
- `C:\Donatello\company\src\main\java\com\example\company\machines\domain\model\Machine.java`
- `C:\Donatello\company\src\main\java\com\example\company\machines\domain\exception\DuplicateMachineNameException.java`
- `C:\Donatello\company\src\main\java\com\example\company\machines\domain\exception\MachineNotFoundException.java`
- `C:\Donatello\company\src\main\java\com\example\company\machines\domain\port\in\`
- `C:\Donatello\company\src\main\java\com\example\company\machines\domain\port\out\MachineRepositoryPort.java`
- `C:\Donatello\company\src\main\java\com\example\company\machines\application\`
- `C:\Donatello\company\src\main\java\com\example\company\machines\adapter\in\web\`
- `C:\Donatello\company\src\main\java\com\example\company\machines\adapter\out\persistence\`
- `C:\Donatello\company\src\main\java\com\example\company\shifts\domain\model\Shift.java`
- `C:\Donatello\company\src\main\java\com\example\company\shifts\domain\exception\DuplicateShiftNameException.java`
- `C:\Donatello\company\src\main\java\com\example\company\shifts\domain\exception\ShiftNotFoundException.java`
- `C:\Donatello\company\src\main\java\com\example\company\shifts\domain\port\in\`
- `C:\Donatello\company\src\main\java\com\example\company\shifts\domain\port\out\ShiftRepositoryPort.java`
- `C:\Donatello\company\src\main\java\com\example\company\shifts\application\`
- `C:\Donatello\company\src\main\java\com\example\company\shifts\adapter\in\web\`
- `C:\Donatello\company\src\main\java\com\example\company\shifts\adapter\out\persistence\`
- `C:\Donatello\company\src\main\java\com\example\company\roles\domain\model\Role.java`
- `C:\Donatello\company\src\main\java\com\example\company\roles\domain\exception\DuplicateRoleNameException.java`
- `C:\Donatello\company\src\main\java\com\example\company\roles\domain\exception\RoleNotFoundException.java`
- `C:\Donatello\company\src\main\java\com\example\company\roles\domain\port\in\`
- `C:\Donatello\company\src\main\java\com\example\company\roles\domain\port\out\RoleRepositoryPort.java`
- `C:\Donatello\company\src\main\java\com\example\company\roles\application\`
- `C:\Donatello\company\src\main\java\com\example\company\roles\adapter\in\web\`
- `C:\Donatello\company\src\main\java\com\example\company\roles\adapter\out\persistence\`
- `C:\Donatello\company\src\test\java\com\example\company\machines\domain\model\MachineTest.java`
- `C:\Donatello\company\src\test\java\com\example\company\machines\application\usecase\`
- `C:\Donatello\company\src\test\java\com\example\company\shifts\domain\model\ShiftTest.java`
- `C:\Donatello\company\src\test\java\com\example\company\shifts\application\usecase\`
- `C:\Donatello\company\src\test\java\com\example\company\roles\domain\model\RoleTest.java`
- `C:\Donatello\company\src\test\java\com\example\company\roles\application\usecase\`
- `C:\Donatello\company\src\test\java\com\example\company\container_types\application\usecase\`
- `C:\Donatello\company\src\test\java\com\example\company\containers\application\usecase\`

**Files modified:**
- `C:\Donatello\company\_plans\base-catalogs.md`
- `C:\Donatello\company\.agents\memory\project-context.md`
- `C:\Donatello\company\.agents\memory\features-log.md`
- `C:\Donatello\company\.agents\memory\decisions-log.md`

**Ports added:**
- Input: `CreateMachineUseCase`, `GetMachineUseCase`, `UpdateMachineUseCase`, `DeleteMachineUseCase`.
- Input: `CreateShiftUseCase`, `GetShiftUseCase`, `UpdateShiftUseCase`, `DeleteShiftUseCase`.
- Input: `CreateRoleUseCase`, `GetRoleUseCase`, `UpdateRoleUseCase`, `DeleteRoleUseCase`.
- Output: `MachineRepositoryPort`, `ShiftRepositoryPort`, `RoleRepositoryPort`.

**Tests added:**
- Domain tests for `Machine`, `Shift`, and `Role`.
- Application use case tests for create, get/list, update, and soft delete in `machines`, `shifts`, and `roles`.
- Focused application use case tests for existing `container_types` and `containers`.

**Notes:** Implemented only base catalog CRUD for machines, shifts, and roles. Corrected the plan to use lowercase dot-separated `DomainException` error codes: `machine.duplicate-name`, `machine.not-found`, `shift.duplicate-name`, `shift.not-found`, `role.duplicate-name`, and `role.not-found`. Auth, JWT, users, role seeding, frontend, GraphQL, endpoint authorization, and operational lookup endpoints remain out of scope. No Flyway migration was modified.

## 2026-06-12 [Security bootstrap spec and plan]

**Files created:**
- `C:\Donatello\company\_plans\security-bootstrap.md`

**Files modified:**
- `C:\Donatello\company\_specs\security-bootstrap.md`
- `C:\Donatello\company\.agents\memory\project-context.md`
- `C:\Donatello\company\.agents\memory\decisions-log.md`
- `C:\Donatello\company\.agents\memory\features-log.md`

**Ports added:** None.

**Tests added:** None.

**Notes:** Clarified that required roles may be seeded with an append-only Flyway migration, while the initial `ADMIN` user and optional demo users must be application-bootstrap controlled because passwords come from environment/configuration. Created an implementation plan only; no source code or migrations were implemented.

## 2026-06-12 [Security bootstrap implementation]

**Files created:**
- `C:\Donatello\company\src\main\resources\db\migration\V2__seed_required_roles.sql`
- `C:\Donatello\company\src\main\java\com\example\company\security_bootstrap\domain\model\BootstrapRoleName.java`
- `C:\Donatello\company\src\main\java\com\example\company\security_bootstrap\domain\model\BootstrapUserDefinition.java`
- `C:\Donatello\company\src\main\java\com\example\company\security_bootstrap\domain\port\in\RunSecurityBootstrapUseCase.java`
- `C:\Donatello\company\src\main\java\com\example\company\security_bootstrap\domain\port\in\SecurityBootstrapCommand.java`
- `C:\Donatello\company\src\main\java\com\example\company\security_bootstrap\domain\port\in\SecurityBootstrapResult.java`
- `C:\Donatello\company\src\main\java\com\example\company\security_bootstrap\domain\port\out\PasswordHashingPort.java`
- `C:\Donatello\company\src\main\java\com\example\company\security_bootstrap\domain\port\out\SecurityBootstrapRoleLookupPort.java`
- `C:\Donatello\company\src\main\java\com\example\company\security_bootstrap\domain\port\out\SecurityBootstrapUserPort.java`
- `C:\Donatello\company\src\main\java\com\example\company\security_bootstrap\application\usecase\SecurityBootstrapService.java`
- `C:\Donatello\company\src\main\java\com\example\company\security_bootstrap\adapter\in\startup\SecurityBootstrapProperties.java`
- `C:\Donatello\company\src\main\java\com\example\company\security_bootstrap\adapter\in\startup\SecurityBootstrapRunner.java`
- `C:\Donatello\company\src\main\java\com\example\company\security_bootstrap\adapter\out\persistence\JdbcSecurityBootstrapRoleLookupAdapter.java`
- `C:\Donatello\company\src\main\java\com\example\company\security_bootstrap\adapter\out\persistence\JdbcSecurityBootstrapUserAdapter.java`
- `C:\Donatello\company\src\main\java\com\example\company\security_bootstrap\adapter\out\security\BCryptPasswordHashingAdapter.java`
- `C:\Donatello\company\src\test\java\com\example\company\security_bootstrap\application\usecase\SecurityBootstrapServiceTest.java`
- `C:\Donatello\company\src\test\java\com\example\company\security_bootstrap\domain\model\SecurityBootstrapUserDefinitionTest.java`
- `C:\Donatello\company\src\test\java\com\example\company\security_bootstrap\adapter\out\security\BCryptPasswordHashingAdapterTest.java`
- `C:\Donatello\company\src\test\java\com\example\company\security_bootstrap\adapter\in\startup\SecurityBootstrapPropertiesTest.java`

**Files modified:**
- `C:\Donatello\company\_specs\security-bootstrap.md`
- `C:\Donatello\company\_plans\security-bootstrap.md`
- `C:\Donatello\company\build.gradle`
- `C:\Donatello\company\.agents\memory\project-context.md`
- `C:\Donatello\company\.agents\memory\features-log.md`

**Ports added:**
- Input: `RunSecurityBootstrapUseCase`.
- Output: `PasswordHashingPort`, `SecurityBootstrapRoleLookupPort`, `SecurityBootstrapUserPort`.

**Tests added:**
- `SecurityBootstrapServiceTest` covers disabled bootstrap, missing roles, admin password requirement, idempotent existing admin/demo users, demo dual gate, and secret redaction.
- `SecurityBootstrapUserDefinitionTest` covers domain validation, trimming, and password redaction.
- `BCryptPasswordHashingAdapterTest` covers BCrypt hashing and blank password rejection.
- `SecurityBootstrapPropertiesTest` covers disabled bootstrap, environment password binding, fallback test password, and demo profile gating.

**Verification:**
- `.\gradlew.bat compileJava testClasses`
- `.\gradlew.bat test --tests "*SecurityBootstrap*"`
- `.\gradlew.bat test --tests "*HexagonalArchitectureTest"`

**Notes:** Added only `spring-security-crypto` for BCrypt hashing. Did not add Spring Security web configuration, login, JWT, filters, endpoint authorization, frontend, or GraphQL. `V1__create_initial_schema.sql` was not modified. The role migration contains only required role data and no users or password hashes.
