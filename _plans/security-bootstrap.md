# Plan: Security Bootstrap Data

Source spec:

```text
_specs/security-bootstrap.md
```

## Scope

Implement only the bootstrap data foundation needed before auth, user management, endpoint authorization, and frontend RBAC can work.

Included now:

- Required platform role seeding for `ADMIN`, `SUPERVISOR`, `OPERADOR`, and `CONSULTA`.
- Runtime application bootstrap for the initial `admin` user.
- Optional runtime application bootstrap for demo users in local/dev/test only.
- Password hashing for bootstrap-created users.
- Idempotency and tests.
- `security.bootstrap.enabled=false` disables runtime bootstrap.

Out of scope:

- Login endpoint.
- JWT generation.
- JWT validation filter.
- Password reset.
- Full user management backend or UI.
- Endpoint authorization implementation.
- Spring Security web/filter configuration.
- Login, JWT, filters, endpoint authorization, and frontend code.
- Angular frontend.
- GraphQL.
- Modifying `V1__create_initial_schema.sql`.

## Implementation Boundary

Required roles:

- Implement with a new append-only Flyway migration.
- Do not edit `V1__create_initial_schema.sql`.
- Use conflict-safe inserts on `roles.name`.
- Seed only non-secret role data.

Initial `ADMIN` user:

- Runtime bootstrap can be disabled with `security.bootstrap.enabled=false`.
- Do not create with static SQL because the password comes from environment/configuration.
- Create with an application bootstrap component that runs after Flyway.
- Read `SECURITY_BOOTSTRAP_ADMIN_PASSWORD`.
- Hash the password before saving to `users.password_hash`.
- Do not overwrite an existing `admin` user.
- If bootstrap is enabled and `admin` does not exist, require `SECURITY_BOOTSTRAP_ADMIN_PASSWORD`.

Optional demo users:

- Create only through application/profile-controlled bootstrap.
- Require both `security.bootstrap.demo-users.enabled=true` and an allowed active profile: `local`, `dev`, or `test`.
- Do not create in production.
- Hash demo passwords and keep them out of source/migrations.

## Phase 1: Role Seed Migration

Create:

- `src/main/resources/db/migration/V2__seed_required_roles.sql`

Expected behavior:

- Insert `ADMIN`, `SUPERVISOR`, `OPERADOR`, and `CONSULTA`.
- Populate `name`, `description`, and `active`.
- Let `id`, `version`, `created_at`, and `updated_at` use database defaults unless conflict behavior updates `updated_at`.
- Use PostgreSQL `ON CONFLICT (name)` for idempotency.
- Recommended conflict behavior: keep required roles active and keep descriptions aligned with the bootstrap spec.
- Do not insert users or password hashes in this migration.

## Phase 2: Password Hashing Boundary

Modify:

- `build.gradle`

Create:

- `src/main/java/com/example/company/security_bootstrap/domain/port/out/PasswordHashingPort.java`
- `src/main/java/com/example/company/security_bootstrap/adapter/out/security/BCryptPasswordHashingAdapter.java`

Expected behavior:

- Add only the password hashing dependency needed for BCrypt, preferably `org.springframework.security:spring-security-crypto`.
- Do not add Spring Security web configuration or auth filters.
- Do not add login, JWT, endpoint authorization, or frontend code.
- `PasswordHashingPort` exposes a small domain-side contract such as `String hash(String rawPassword)`.
- `BCryptPasswordHashingAdapter` implements the port and stores only hashed passwords.

## Phase 3: Bootstrap Domain And Ports

Create:

- `src/main/java/com/example/company/security_bootstrap/domain/model/BootstrapRoleName.java`
- `src/main/java/com/example/company/security_bootstrap/domain/model/BootstrapUserDefinition.java`
- `src/main/java/com/example/company/security_bootstrap/domain/port/in/RunSecurityBootstrapUseCase.java`
- `src/main/java/com/example/company/security_bootstrap/domain/port/in/SecurityBootstrapCommand.java`
- `src/main/java/com/example/company/security_bootstrap/domain/port/in/SecurityBootstrapResult.java`
- `src/main/java/com/example/company/security_bootstrap/domain/port/out/SecurityBootstrapRoleLookupPort.java`
- `src/main/java/com/example/company/security_bootstrap/domain/port/out/SecurityBootstrapUserPort.java`

Expected behavior:

- Keep domain classes pure Java.
- `BootstrapRoleName` defines the required platform role names.
- `BootstrapUserDefinition` represents username, full name, role name, raw password, and whether the user is optional/demo.
- `SecurityBootstrapCommand` carries the configured admin password and demo-user configuration into the use case.
- `SecurityBootstrapRoleLookupPort` resolves role IDs by role name and can report missing required roles.
- `SecurityBootstrapUserPort` checks users by username and inserts bootstrap users.
- Ports must not depend on Spring, JPA, JDBC, or adapters.

## Phase 4: Bootstrap Use Case

Create:

- `src/main/java/com/example/company/security_bootstrap/application/usecase/SecurityBootstrapService.java`

Expected behavior:

- Implement `RunSecurityBootstrapUseCase`.
- Verify all required roles exist after Flyway.
- If the `admin` user already exists, leave it unchanged.
- If the `admin` user does not exist, require `SECURITY_BOOTSTRAP_ADMIN_PASSWORD`.
- Hash the admin password through `PasswordHashingPort`.
- Save the `admin` user with role `ADMIN`, full name `Initial Administrator`, and `active = true`.
- Create demo users only when explicitly enabled for local/dev/test.
- Do not overwrite existing demo users.
- Return a result that can be logged without secrets.

Error handling expectations:

- Missing required roles should fail fast with a clear exception because the migration did not run or data was changed.
- Missing admin password should fail fast only when `admin` does not already exist.
- If bootstrap is disabled, the runner must skip the use case and must not require `SECURITY_BOOTSTRAP_ADMIN_PASSWORD`.
- Exceptions must not include raw passwords.

## Phase 5: Persistence Adapter For Existing Tables

Create:

- `src/main/java/com/example/company/security_bootstrap/adapter/out/persistence/JdbcSecurityBootstrapRoleLookupAdapter.java`
- `src/main/java/com/example/company/security_bootstrap/adapter/out/persistence/JdbcSecurityBootstrapUserAdapter.java`

Expected behavior:

- Use existing `roles` and `users` tables from `V1__create_initial_schema.sql`.
- Query roles by `roles.name`.
- Query users by `users.username`.
- Insert users into `users(role_id, full_name, username, password_hash, active)`.
- Let `id`, `version`, `created_at`, and `updated_at` use database defaults.
- Keep the adapter narrowly scoped to bootstrap persistence.
- Do not create REST endpoints.
- Do not expose JPA entities.
- Do not implement full user management.

Recommended implementation note:

- A JDBC adapter is acceptable here because bootstrap needs a small, idempotent table operation and there is no full `users` module yet.
- If a future `users` module is implemented first, this adapter can be replaced by a `UserRepositoryPort` without changing the bootstrap use case.

## Phase 6: Startup Adapter And Configuration

Create:

- `src/main/java/com/example/company/security_bootstrap/adapter/in/startup/SecurityBootstrapProperties.java`
- `src/main/java/com/example/company/security_bootstrap/adapter/in/startup/SecurityBootstrapRunner.java`

Expected behavior:

- `SecurityBootstrapRunner` runs after Flyway during application startup.
- The runner calls `RunSecurityBootstrapUseCase`.
- Bind configuration/environment values without committing secrets.
- Required admin password key: `SECURITY_BOOTSTRAP_ADMIN_PASSWORD`.
- Demo user creation is disabled by default.
- Demo user creation requires both `security.bootstrap.demo-users.enabled=true` and an allowed active profile: `local`, `dev`, or `test`.
- Do not log raw passwords or password hashes.

Suggested properties:

```text
security.bootstrap.enabled=true
security.bootstrap.admin.username=admin
security.bootstrap.admin.full-name=Initial Administrator
security.bootstrap.demo-users.enabled=false
```

Environment-only secret:

```text
SECURITY_BOOTSTRAP_ADMIN_PASSWORD
```

Optional demo secrets may use environment keys such as:

```text
SECURITY_BOOTSTRAP_SUPERVISOR_DEMO_PASSWORD
SECURITY_BOOTSTRAP_OPERADOR_DEMO_PASSWORD
SECURITY_BOOTSTRAP_CONSULTA_DEMO_PASSWORD
```

Do not add real secret values to repository files.

## Phase 7: Tests

Create focused tests:

- `src/test/java/com/example/company/security_bootstrap/application/usecase/SecurityBootstrapServiceTest.java`
- `src/test/java/com/example/company/security_bootstrap/domain/model/SecurityBootstrapUserDefinitionTest.java`
- `src/test/java/com/example/company/security_bootstrap/adapter/out/security/BCryptPasswordHashingAdapterTest.java`
- `src/test/java/com/example/company/security_bootstrap/adapter/in/startup/SecurityBootstrapPropertiesTest.java`

Test cases:

- Bootstrap disabled skips the use case and does not require a real admin password.
- All required roles are checked before user creation.
- Missing required role fails fast.
- Existing `admin` user is not overwritten.
- Missing admin password fails when `admin` does not exist.
- Admin user is created with role `ADMIN` when password exists.
- Admin password is hashed and raw password is not stored.
- Demo users are skipped by default.
- Demo users are created only when enabled and an allowed profile is active.
- Demo users are skipped when enabled but no allowed profile is active.
- Existing demo users are not overwritten.
- Result/log data does not contain raw passwords.
- Tests must not fail due to a missing real admin password; use test properties or disabled bootstrap in startup tests.

Migration review:

- Verify `V2__seed_required_roles.sql` contains only role data.
- Verify no password or user insert appears in the migration.
- Verify `V1__create_initial_schema.sql` is unchanged.

Architecture verification:

- Existing `HexagonalArchitectureTest` must continue to pass.
- Domain packages must not import Spring, JDBC, JPA, Jackson, servlet APIs, DTOs, repositories, or adapters.
- Application package must not depend on adapters.

## Phase 8: Verification Commands

Run after implementation:

```powershell
.\gradlew.bat compileJava testClasses
.\gradlew.bat test --tests "*SecurityBootstrap*"
.\gradlew.bat test --tests "*HexagonalArchitectureTest"
```

Also run a scoped git review:

```powershell
git diff -- _specs/security-bootstrap.md _plans/security-bootstrap.md build.gradle src/main/resources/db/migration src/main/java/com/example/company/security_bootstrap src/test/java/com/example/company/security_bootstrap
git status --short --branch
```

## Expected Files Created

- `src/main/resources/db/migration/V2__seed_required_roles.sql`
- `src/main/java/com/example/company/security_bootstrap/domain/model/BootstrapRoleName.java`
- `src/main/java/com/example/company/security_bootstrap/domain/model/BootstrapUserDefinition.java`
- `src/main/java/com/example/company/security_bootstrap/domain/port/in/RunSecurityBootstrapUseCase.java`
- `src/main/java/com/example/company/security_bootstrap/domain/port/in/SecurityBootstrapCommand.java`
- `src/main/java/com/example/company/security_bootstrap/domain/port/in/SecurityBootstrapResult.java`
- `src/main/java/com/example/company/security_bootstrap/domain/port/out/PasswordHashingPort.java`
- `src/main/java/com/example/company/security_bootstrap/domain/port/out/SecurityBootstrapRoleLookupPort.java`
- `src/main/java/com/example/company/security_bootstrap/domain/port/out/SecurityBootstrapUserPort.java`
- `src/main/java/com/example/company/security_bootstrap/application/usecase/SecurityBootstrapService.java`
- `src/main/java/com/example/company/security_bootstrap/adapter/in/startup/SecurityBootstrapProperties.java`
- `src/main/java/com/example/company/security_bootstrap/adapter/in/startup/SecurityBootstrapRunner.java`
- `src/main/java/com/example/company/security_bootstrap/adapter/out/persistence/JdbcSecurityBootstrapRoleLookupAdapter.java`
- `src/main/java/com/example/company/security_bootstrap/adapter/out/persistence/JdbcSecurityBootstrapUserAdapter.java`
- `src/main/java/com/example/company/security_bootstrap/adapter/out/security/BCryptPasswordHashingAdapter.java`
- `src/test/java/com/example/company/security_bootstrap/application/usecase/SecurityBootstrapServiceTest.java`
- `src/test/java/com/example/company/security_bootstrap/domain/model/SecurityBootstrapUserDefinitionTest.java`
- `src/test/java/com/example/company/security_bootstrap/adapter/out/security/BCryptPasswordHashingAdapterTest.java`
- `src/test/java/com/example/company/security_bootstrap/adapter/in/startup/SecurityBootstrapPropertiesTest.java`

## Expected Files Modified

- `_specs/security-bootstrap.md`
- `build.gradle`
- `.agents/memory/project-context.md`
- `.agents/memory/decisions-log.md`
- `.agents/memory/features-log.md`

Do not modify:

- `src/main/resources/db/migration/V1__create_initial_schema.sql`
- Existing auth/JWT/security filter code, unless a future approved auth spec asks for it.
- Frontend files.
- GraphQL files.

## Acceptance Checklist

- Required roles are seeded by append-only migration.
- No migration contains user passwords or password hashes.
- Admin user is created only through application bootstrap.
- Bootstrap runs after Flyway.
- Bootstrap is disableable with `security.bootstrap.enabled=false`.
- Bootstrap reads `SECURITY_BOOTSTRAP_ADMIN_PASSWORD`.
- Bootstrap requires `SECURITY_BOOTSTRAP_ADMIN_PASSWORD` only when enabled and `admin` does not exist.
- Bootstrap hashes passwords before insert.
- Bootstrap does not overwrite an existing `admin`.
- Demo users are disabled by default.
- Demo users require both `security.bootstrap.demo-users.enabled=true` and active profile `local`, `dev`, or `test`.
- Implementation is idempotent.
- No login/JWT/filter/endpoint authorization behavior is implemented.
- No frontend code is implemented.
- `V1__create_initial_schema.sql` is unchanged.
