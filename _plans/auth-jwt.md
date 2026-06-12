# Plan: Auth JWT Module

Source spec:

```text
_specs/auth-jwt.md
```

## Scope

Implement the login-only JWT authentication slice.

Included now:

- `POST /api/auth/login`.
- BCrypt password verification against `users.password_hash`.
- JWT access token generation using HMAC.
- JWT claims: `sub`, `userId`, `username`, `role`, `iat`, `exp`, and `iss` only when configured.
- Safe authenticated user response.
- Generic invalid-credentials behavior for unknown username and wrong password.
- Rejection of inactive users.
- Rejection of missing or inactive roles.
- Token configuration through environment/properties.
- Focused tests.
- Useful comments only for non-obvious security/architecture boundaries.

Out of scope:

- Spring Security filter chain configuration.
- JWT request filters.
- Protected endpoint rules.
- Role-based authorization.
- Refresh tokens.
- Password reset.
- User management.
- Registration.
- Angular frontend.
- GraphQL.
- New tables.
- Changes to existing Flyway migrations.

## Implementation Boundary

- Do not modify `V1__create_initial_schema.sql`.
- Do not create a Flyway migration.
- Use existing `users` and `roles` tables from V1.
- Keep domain pure Java.
- Follow hexagonal package structure under `src/main/java/com/example/company/auth/`.
- REST controller must call `LoginUseCase`.
- Persistence adapter may use JDBC projection for the narrow read-only login query.
- Do not expose `password_hash` in API responses, logs, result objects, or JWT claims.
- Do not log raw passwords.
- JWT signing secret must come from environment/configuration and must not be committed.
- Use HMAC for JWT signing.
- Do not add Spring Security web/filter configuration.
- Do not add a JWT dependency unless necessary. Prefer Java crypto and Jackson already available through Spring Web MVC.

## Phase 1: Domain Models And Exceptions

Create:

- `src/main/java/com/example/company/auth/domain/model/LoginCredentials.java`
- `src/main/java/com/example/company/auth/domain/model/AuthUserRecord.java`
- `src/main/java/com/example/company/auth/domain/model/AuthenticatedUser.java`
- `src/main/java/com/example/company/auth/domain/model/AuthenticatedUserSummary.java`
- `src/main/java/com/example/company/auth/domain/model/JwtAccessToken.java`
- `src/main/java/com/example/company/auth/domain/exception/InvalidCredentialsException.java`
- `src/main/java/com/example/company/auth/domain/exception/InactiveUserException.java`
- `src/main/java/com/example/company/auth/domain/exception/InactiveRoleException.java`
- `src/main/java/com/example/company/auth/domain/exception/RoleUnavailableException.java`
- `src/main/java/com/example/company/auth/domain/exception/TokenConfigurationException.java`

Expected behavior:

- Validate username and password are required.
- Keep raw password transient and redact from `toString`.
- Keep `password_hash` only in the internal lookup model and redact it from `toString`.
- Define auth-specific exceptions with stable codes:
  - `auth.invalid-credentials`
  - `auth.inactive-user`
  - `auth.inactive-role`
  - `auth.role-unavailable`
  - `auth.token-config-invalid`

## Phase 2: Domain Ports

Create:

- `src/main/java/com/example/company/auth/domain/port/in/LoginCommand.java`
- `src/main/java/com/example/company/auth/domain/port/in/LoginResult.java`
- `src/main/java/com/example/company/auth/domain/port/in/LoginUseCase.java`
- `src/main/java/com/example/company/auth/domain/port/out/AuthUserLookupPort.java`
- `src/main/java/com/example/company/auth/domain/port/out/PasswordVerificationPort.java`
- `src/main/java/com/example/company/auth/domain/port/out/JwtTokenPort.java`

Expected behavior:

- Input command contains username and raw password only.
- Result contains token, token type, expiration timestamp, and safe user summary.
- Output ports keep persistence, BCrypt, and JWT concerns outside the application use case.

## Phase 3: Login Use Case

Create:

- `src/main/java/com/example/company/auth/application/usecase/LoginService.java`
- `src/main/java/com/example/company/auth/application/mapper/LoginResultMapper.java`

Expected behavior:

- `LoginService` implements `LoginUseCase`.
- Load user by username through `AuthUserLookupPort`.
- Unknown username throws `InvalidCredentialsException`.
- Inactive user throws `InactiveUserException`.
- Missing role throws `RoleUnavailableException`.
- Inactive role throws `InactiveRoleException`.
- Wrong password throws `InvalidCredentialsException`.
- Successful login generates token through `JwtTokenPort`.
- Add the required concise comment explaining why username/password failures return the same generic error.

## Phase 4: Persistence Adapter

Create:

- `src/main/java/com/example/company/auth/adapter/out/persistence/JdbcAuthUserLookupAdapter.java`

Expected behavior:

- Read existing V1 `users` and `roles` tables.
- Query by `users.username`.
- Use a `LEFT JOIN` so a missing role can be detected as `RoleUnavailableException`.
- Return a domain lookup model.
- Add the required concise comment explaining no schema changes are made here.
- Do not expose JPA entities.
- Do not implement full user management.

## Phase 5: Password Verification Adapter

Create:

- `src/main/java/com/example/company/auth/adapter/out/security/BCryptPasswordVerificationAdapter.java`

Expected behavior:

- Use `BCryptPasswordEncoder.matches(rawPassword, passwordHash)`.
- Add the required concise comment explaining raw passwords must never be logged, returned, or stored.
- Do not add Spring Security web/filter configuration.

## Phase 6: JWT Token Adapter And Configuration

Create:

- `src/main/java/com/example/company/auth/adapter/out/security/JwtProperties.java`
- `src/main/java/com/example/company/auth/adapter/out/security/HmacJwtTokenAdapter.java`

Expected behavior:

- Use HMAC-SHA256 JWT signing.
- Use Java crypto and Base64 URL encoding; no new JWT dependency unless implementation requires it.
- Bind `security.jwt.secret` / `SECURITY_JWT_SECRET`.
- Bind `security.jwt.expiration`.
- Bind optional `security.jwt.issuer`.
- Fail when secret is blank or expiration is invalid.
- Claims:
  - `sub` as user ID string.
  - `userId`.
  - `username`.
  - `role`.
  - `iat`.
  - `exp`.
  - `iss` only if configured.
- Add the required concise comments about secret source and excluding sensitive claims.

## Phase 7: REST Adapter

Create:

- `src/main/java/com/example/company/auth/adapter/in/web/AuthRestController.java`
- `src/main/java/com/example/company/auth/adapter/in/web/AuthWebMapper.java`
- `src/main/java/com/example/company/auth/adapter/in/web/AuthExceptionHandler.java`
- `src/main/java/com/example/company/auth/adapter/in/web/dto/LoginRequest.java`
- `src/main/java/com/example/company/auth/adapter/in/web/dto/LoginResponse.java`
- `src/main/java/com/example/company/auth/adapter/in/web/dto/AuthenticatedUserResponse.java`
- `src/main/java/com/example/company/auth/adapter/in/web/dto/AuthErrorResponse.java`

Expected behavior:

- `POST /api/auth/login`.
- Controller calls `LoginUseCase`.
- Request uses Jakarta validation.
- Response returns token type `Bearer`, access token, expiration, and safe user summary.
- Invalid credentials map to HTTP 401 with generic message.
- Inactive user, inactive role, and missing role map to HTTP 403.
- Token configuration failure maps to HTTP 500 if it reaches the web layer.
- Do not implement endpoint protection.

## Phase 8: Focused Tests

Create:

- `src/test/java/com/example/company/auth/application/usecase/AuthLoginServiceTest.java`
- `src/test/java/com/example/company/auth/domain/model/AuthLoginCredentialsTest.java`
- `src/test/java/com/example/company/auth/adapter/out/security/AuthBCryptPasswordVerificationAdapterTest.java`
- `src/test/java/com/example/company/auth/adapter/out/security/AuthHmacJwtTokenAdapterTest.java`
- `src/test/java/com/example/company/auth/adapter/in/web/AuthWebMapperTest.java`

Test cases:

- Admin-style active user can login with matching password.
- Unknown username returns generic invalid credentials.
- Wrong password returns the same generic invalid credentials.
- Inactive user is rejected.
- Missing role is rejected.
- Inactive role is rejected.
- Raw password is redacted in domain `toString`.
- BCrypt verifies matching hashes and rejects wrong password.
- JWT includes required claims.
- JWT uses `sub` as user ID.
- JWT includes `iss` only when configured.
- JWT does not include `password_hash`.
- Missing secret is rejected.
- REST mapper returns safe response only.
- Tests use descriptive names documenting the security rule.

## Phase 9: Verification

Run after implementation:

```powershell
.\gradlew.bat compileJava testClasses
.\gradlew.bat test --tests "*Auth*"
.\gradlew.bat test --tests "*HexagonalArchitectureTest"
```

Also run a scoped review:

```powershell
git diff -- _specs/auth-jwt.md _plans/auth-jwt.md build.gradle src/main/java/com/example/company/auth src/test/java/com/example/company/auth src/main/resources/db/migration/V1__create_initial_schema.sql
git status --short --branch
```

## Expected Files Created

- `src/main/java/com/example/company/auth/domain/model/LoginCredentials.java`
- `src/main/java/com/example/company/auth/domain/model/AuthUserRecord.java`
- `src/main/java/com/example/company/auth/domain/model/AuthenticatedUser.java`
- `src/main/java/com/example/company/auth/domain/model/AuthenticatedUserSummary.java`
- `src/main/java/com/example/company/auth/domain/model/JwtAccessToken.java`
- `src/main/java/com/example/company/auth/domain/exception/InvalidCredentialsException.java`
- `src/main/java/com/example/company/auth/domain/exception/InactiveUserException.java`
- `src/main/java/com/example/company/auth/domain/exception/InactiveRoleException.java`
- `src/main/java/com/example/company/auth/domain/exception/RoleUnavailableException.java`
- `src/main/java/com/example/company/auth/domain/exception/TokenConfigurationException.java`
- `src/main/java/com/example/company/auth/domain/port/in/LoginCommand.java`
- `src/main/java/com/example/company/auth/domain/port/in/LoginResult.java`
- `src/main/java/com/example/company/auth/domain/port/in/LoginUseCase.java`
- `src/main/java/com/example/company/auth/domain/port/out/AuthUserLookupPort.java`
- `src/main/java/com/example/company/auth/domain/port/out/PasswordVerificationPort.java`
- `src/main/java/com/example/company/auth/domain/port/out/JwtTokenPort.java`
- `src/main/java/com/example/company/auth/application/usecase/LoginService.java`
- `src/main/java/com/example/company/auth/application/mapper/LoginResultMapper.java`
- `src/main/java/com/example/company/auth/adapter/out/persistence/JdbcAuthUserLookupAdapter.java`
- `src/main/java/com/example/company/auth/adapter/out/security/BCryptPasswordVerificationAdapter.java`
- `src/main/java/com/example/company/auth/adapter/out/security/JwtProperties.java`
- `src/main/java/com/example/company/auth/adapter/out/security/HmacJwtTokenAdapter.java`
- `src/main/java/com/example/company/auth/adapter/in/web/AuthRestController.java`
- `src/main/java/com/example/company/auth/adapter/in/web/AuthWebMapper.java`
- `src/main/java/com/example/company/auth/adapter/in/web/AuthExceptionHandler.java`
- `src/main/java/com/example/company/auth/adapter/in/web/dto/LoginRequest.java`
- `src/main/java/com/example/company/auth/adapter/in/web/dto/LoginResponse.java`
- `src/main/java/com/example/company/auth/adapter/in/web/dto/AuthenticatedUserResponse.java`
- `src/main/java/com/example/company/auth/adapter/in/web/dto/AuthErrorResponse.java`
- focused auth tests under `src/test/java/com/example/company/auth/`.

## Expected Files Modified

- `_specs/auth-jwt.md`
- `_plans/auth-jwt.md`
- `.agents/memory/project-context.md`
- `.agents/memory/features-log.md`
- `.agents/memory/decisions-log.md`

No build dependency change is expected unless implementation chooses a small JWT dependency. No migration change is expected.

## Acceptance Checklist

- `POST /api/auth/login` implemented.
- BCrypt password verification against `users.password_hash` implemented.
- HMAC JWT access token generation implemented.
- JWT includes `sub`, `userId`, `username`, `role`, `iat`, `exp`, and optional `iss`.
- Unknown username and wrong password return the same generic invalid-credentials behavior.
- Inactive user is rejected.
- Missing role is rejected.
- Inactive role is rejected.
- Password hash is not exposed in API responses, logs, result objects, or JWT claims.
- Raw password is not logged.
- JWT secret comes from environment/configuration.
- No Spring Security filter chain, request filter, protected endpoint rules, or role authorization are implemented.
- No refresh token, password reset, user management, registration, frontend, GraphQL, new tables, or Flyway migration changes.
- Existing `HexagonalArchitectureTest` passes.
