# Spec: Auth JWT Module

## Summary

Define the authentication module that lets existing active users log in and receive a JWT access token.

This spec builds on security bootstrap:

- `security_bootstrap` creates required roles and the initial `admin` user.
- `auth-jwt` validates credentials and issues a signed access token.

This spec does not implement endpoint authorization, refresh tokens, password reset, user management, Angular frontend, GraphQL, or Flyway schema changes.

## 1. Purpose

The security bootstrap module ensures the database has required platform roles and at least one initial `ADMIN` user. That data alone does not let anyone authenticate.

The `auth-jwt` module exists so a user, including the bootstrapped `admin`, can submit credentials through a login endpoint and receive a JWT access token. Future modules can later consume that token for endpoint authorization, user context, audit fields, and role-based UI behavior.

Initial goal:

- Make the bootstrapped `admin` account usable through login.
- Validate credentials against the existing `users` table.
- Include the user's active role in the JWT.
- Avoid leaking password hashes or credential failure details.

## 2. Database Target

The existing schema is defined in:

```text
src/main/resources/db/migration/V1__create_initial_schema.sql
```

Relevant `users` table fields:

| Column | Type | Constraint | Login usage |
|---|---|---|---|
| id | BIGSERIAL | primary key | used as stable user identity |
| role_id | BIGINT | not null, references roles(id) | joins to `roles.id` |
| full_name | VARCHAR(120) | not null | returned in authenticated user summary |
| username | VARCHAR(80) | not null, unique | login identifier |
| password_hash | VARCHAR(255) | not null | BCrypt hash used for verification |
| active | BOOLEAN | not null, default true | inactive users cannot log in |
| version | BIGINT | not null, default 0 | not used by login |
| created_at | TIMESTAMP | not null, default current timestamp | not used by login |
| updated_at | TIMESTAMP | not null, default current timestamp | not used by login |

Relevant `roles` table fields:

| Column | Type | Constraint | Login usage |
|---|---|---|---|
| id | BIGSERIAL | primary key | target of `users.role_id` |
| name | VARCHAR(80) | not null, unique | role claim value |
| description | VARCHAR(255) | nullable | not used by login |
| active | BOOLEAN | not null, default true | inactive roles cannot log in |
| version | BIGINT | not null, default 0 | not used by login |
| created_at | TIMESTAMP | not null, default current timestamp | not used by login |
| updated_at | TIMESTAMP | not null, default current timestamp | not used by login |

Login reads:

- `users.username`
- `users.password_hash`
- `users.active`
- `users.role_id`
- `roles.name`
- `roles.active`

No database schema change is required for this spec. Do not modify `V1__create_initial_schema.sql`.

## 3. Login Use Case

Use case name: `LoginUseCase`

Actor:

- Unauthenticated user.

Input:

```json
{
  "username": "admin",
  "password": "raw-password-from-request"
}
```

Business rules:

- `username` is required.
- `password` is required.
- User must exist by `users.username`.
- User must be active.
- User's role must exist.
- User's role must be active.
- Raw password must match `users.password_hash` using BCrypt verification.
- Wrong username and wrong password must return the same generic invalid-credentials error.
- Login must not return or log the raw password.
- Login must not return or log `password_hash`.

Output:

```json
{
  "accessToken": "<jwt>",
  "tokenType": "Bearer",
  "expiresAt": "2026-06-12T18:30:00Z",
  "user": {
    "userId": 1,
    "username": "admin",
    "fullName": "Initial Administrator",
    "role": "ADMIN"
  }
}
```

Error cases:

| Case | Rule | HTTP status | Response code |
|---|---|---:|---|
| Malformed request | JSON invalid or required fields missing | 400 | `auth.validation-error` |
| Unknown username | User does not exist | 401 | `auth.invalid-credentials` |
| Wrong password | Password does not match hash | 401 | `auth.invalid-credentials` |
| Inactive user | `users.active = false` | 403 | `auth.inactive-user` |
| Missing role | `users.role_id` does not resolve to a role | 403 | `auth.role-unavailable` |
| Inactive role | `roles.active = false` | 403 | `auth.inactive-role` |
| Token configuration missing | JWT signing secret not configured | startup/config failure, not login response | `auth.token-config-invalid` |

Security note:

- Unknown username and wrong password must both use `auth.invalid-credentials`. The response must not reveal which part of the credential pair was wrong.

## 4. Password Verification

Password verification must use the same BCrypt strategy introduced by `security_bootstrap`.

Rules:

- Use BCrypt verification against `users.password_hash`.
- Do not compare raw strings.
- Do not store raw passwords.
- Do not log raw passwords.
- Do not return raw passwords.
- Do not return `password_hash`.
- Wrong username and wrong password must produce the same generic invalid-credentials response.

Expected port:

```text
auth/domain/port/out/PasswordVerificationPort.java
```

Expected adapter:

```text
auth/adapter/out/security/BCryptPasswordVerificationAdapter.java
```

The adapter may use `org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder` from the already introduced `spring-security-crypto` dependency. This spec does not require Spring Security web configuration.

## 5. JWT Claims

Subject choice:

- Use `userId` as the JWT subject.

Reason:

- `users.id` is the stable database identity for a user. `username` is unique today, but it is a human-facing login identifier and may become changeable in future user management. Keeping `sub` as user ID makes tokens stable across potential username changes while still including `username` as a separate claim.

Required claims:

| Claim | Value | Required | Notes |
|---|---|---:|---|
| `sub` | user ID as string | yes | stable token subject |
| `userId` | user ID as number | yes | convenient client/server claim |
| `username` | `users.username` | yes | display/login context |
| `role` | `roles.name` | yes | e.g. `ADMIN`, `SUPERVISOR`, `OPERADOR`, `CONSULTA` |
| `iat` | issued-at timestamp | yes | JWT standard issued-at |
| `exp` | expiration timestamp | yes | JWT standard expiration |
| `iss` | configured issuer | optional | include if configured |

Do not include:

- `password_hash`
- raw password
- database version fields
- secrets
- personally unnecessary data

Future endpoint authorization may consume `role`, but this spec does not implement endpoint authorization rules.

## 6. API Endpoints

### POST `/api/auth/login`

Purpose:

- Authenticate a username/password pair and issue a JWT access token.

Allowed actor:

- Unauthenticated user.

Request body:

```json
{
  "username": "admin",
  "password": "Admin123!"
}
```

Request validation:

- `username` is required and must not be blank.
- `password` is required and must not be blank.

Success response:

HTTP status: `200 OK`

```json
{
  "accessToken": "<signed-jwt>",
  "tokenType": "Bearer",
  "expiresAt": "2026-06-12T18:30:00Z",
  "user": {
    "userId": 1,
    "username": "admin",
    "fullName": "Initial Administrator",
    "role": "ADMIN"
  }
}
```

Error responses:

| Status | When | Response code |
|---:|---|---|
| 400 | malformed JSON or validation failure | `auth.validation-error` |
| 401 | unknown username or wrong password | `auth.invalid-credentials` |
| 403 | inactive user | `auth.inactive-user` |
| 403 | missing role | `auth.role-unavailable` |
| 403 | inactive role | `auth.inactive-role` |
| 500 or startup failure | token signing configuration is invalid | `auth.token-config-invalid` |

Example generic invalid-credentials response:

```json
{
  "code": "auth.invalid-credentials",
  "message": "Invalid username or password"
}
```

## 7. Token Configuration

Required configuration:

| Setting | Purpose | Required |
|---|---|---:|
| `security.jwt.secret` or `SECURITY_JWT_SECRET` | signing key/secret | yes |
| `security.jwt.expiration` | access token lifetime | yes |
| `security.jwt.issuer` | issuer claim | optional but recommended |

Rules:

- Use an HMAC secret for now unless a future spec explicitly chooses asymmetric keys.
- JWT signing secret must come from environment/configuration.
- JWT signing secret must never be committed to source code, tests with real secrets, migrations, `.env` committed content, or documentation.
- Implementation should fail fast at startup when token signing config is missing or unsafe.
- Expiration must be finite.
- Default expiration, if provided for local development, must be short and clearly documented as non-production.

Suggested local/test-only values may be injected through test properties, not committed secrets.

## 8. Security Rules

- Do not add role-based endpoint authorization in this spec.
- Do not implement refresh tokens yet.
- Do not implement password reset yet.
- Do not implement registration yet.
- Do not expose `password_hash`.
- Do not include `password_hash` in JWT claims.
- Do not leak whether username or password was wrong.
- Do not allow inactive users to login.
- Do not allow users with inactive roles to login.
- Do not allow users with missing roles to login.
- Raw passwords must remain request-only transient values.
- Auth responses must contain only the token, expiration, token type, and safe user summary.

## 9. Hexagonal Structure

Expected module root:

```text
src/main/java/com/example/company/auth/
```

Expected structure:

```text
auth/
  domain/
    model/
      AuthenticatedUser.java
      AuthenticatedUserSummary.java
      JwtAccessToken.java
      LoginCredentials.java
    exception/
      InvalidCredentialsException.java
      InactiveUserException.java
      InactiveRoleException.java
      RoleUnavailableException.java
      TokenConfigurationException.java
    port/
      in/
        LoginCommand.java
        LoginResult.java
        LoginUseCase.java
      out/
        AuthUserLookupPort.java
        PasswordVerificationPort.java
        JwtTokenPort.java
  application/
    usecase/
      LoginService.java
    mapper/
      LoginResultMapper.java
  adapter/
    in/
      web/
        AuthRestController.java
        AuthWebMapper.java
        dto/
          LoginRequest.java
          LoginResponse.java
          AuthenticatedUserResponse.java
    out/
      persistence/
        JdbcAuthUserLookupAdapter.java
      security/
        BCryptPasswordVerificationAdapter.java
        JwtTokenAdapter.java
        JwtProperties.java
```

Architecture rules:

- Domain must stay pure Java.
- Domain must not import Spring, JPA, JDBC, Jackson, servlet APIs, repositories, DTO classes, or adapters.
- Input ports belong under `domain/port/in`.
- Output ports belong under `domain/port/out`.
- Login transaction/orchestration belongs in `application/usecase`.
- REST controller must call `LoginUseCase`.
- REST DTOs must stay under `adapter/in/web/dto`.
- Persistence adapter may query existing V1 `users` and `roles` tables.
- Password verification adapter must implement `PasswordVerificationPort`.
- JWT token adapter must implement `JwtTokenPort`.
- No JPA entities are required for this spec unless the future plan chooses them. JDBC projection is acceptable because login is a narrow read-only query across existing `users` and `roles`.

## 10. Code Documentation And Comments

Future implementation must include concise comments only where they clarify non-obvious security or architecture behavior.

Required comments:

- In `LoginService`, add a short comment explaining why username/password failures return the same generic invalid-credentials error.
- In `BCryptPasswordVerificationAdapter`, add a short comment explaining that raw passwords must never be logged, returned, or stored.
- In `JwtTokenAdapter`, add a short comment explaining why `password_hash` and sensitive user data must never be included in claims.
- In `JdbcAuthUserLookupAdapter`, add a short comment explaining that users and roles are read from existing V1 tables and no schema changes are made here.
- In `JwtProperties` or equivalent configuration class, add a short comment explaining that the JWT secret must come from environment/configuration and must never be committed.
- In tests, use descriptive test names that document the security rule being verified.

Do not add obvious comments such as:

- `This method logs in the user`
- `This returns the token`
- `This sets the username`

Comment style rule:

- Comments should explain why a security or architecture decision exists, not repeat what the code already says.

## 11. Acceptance Criteria

1. `POST /api/auth/login` is documented and implemented later as the only endpoint in this spec.
2. Bootstrapped `admin` can login using the configured bootstrap password.
3. Wrong password is rejected.
4. Unknown username is rejected with the same generic invalid-credentials error as wrong password.
5. Inactive user cannot login.
6. Missing role cannot login.
7. Inactive role cannot login.
8. Password verification uses BCrypt against `users.password_hash`.
9. Raw password is never logged, returned, stored, or included in JWT claims.
10. `password_hash` is never exposed in API responses.
11. JWT contains `sub`, `userId`, `username`, `role`, `iat`, and `exp`.
12. JWT uses user ID as subject.
13. JWT does not contain `password_hash`.
14. JWT signing secret is not hardcoded.
15. JWT expiration is finite and configurable.
16. `V1__create_initial_schema.sql` remains unchanged.
17. No Flyway migration is modified or required by this auth spec.
18. No endpoint authorization is implemented yet.
19. No refresh token behavior is implemented.
20. No password reset behavior is implemented.
21. REST responses use DTOs, not persistence records or JPA entities.
22. Domain remains pure Java and passes existing architecture tests.
23. Useful security/architecture comments are included only where behavior is non-obvious.
24. Tests use descriptive names that document the security rule being verified.

## 12. Out Of Scope

This spec explicitly excludes:

- User management.
- Refresh tokens.
- Password reset.
- Registration.
- Endpoint authorization.
- Role-based access rules for catalogs or operational endpoints.
- Spring Security filter chain configuration, protected endpoint rules, JWT request filters, and role-based authorization.
- Angular frontend.
- Angular route guards.
- GraphQL.
- Modifying existing Flyway migrations.
- Creating new tables or changing existing tables.
- Demo user creation.
- Security bootstrap behavior changes.

The next step after approval should be creating an implementation plan from this spec, not writing implementation code immediately.
