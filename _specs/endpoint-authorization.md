# Spec: Endpoint Authorization And JWT Request Validation

## Summary

Define how the backend validates JWT access tokens on incoming requests and enforces role-based access for REST endpoints.

This spec follows the completed security bootstrap and auth JWT work:

- `security_bootstrap` creates required roles and the initial `admin` user.
- `auth-jwt` implements `POST /api/auth/login` and issues HMAC-signed JWT access tokens.
- `endpoint-authorization` validates those tokens on protected requests and applies role rules.

This spec is documentation only. It does not implement Spring Security configuration, filters, or authorization code yet.

Dependency clarification:

- This spec may add Spring Security web dependencies required for filter chain, request authentication, and authorization.
- Do not add OAuth2 server/client dependencies unless a future spec explicitly needs OAuth2 behavior.

## 1. Purpose

Endpoint authorization comes after auth JWT because token issuance and token consumption are separate concerns.

The current `auth` module authenticates username/password credentials and issues a signed JWT. That proves a user could log in at token creation time, but it does not protect any business endpoint by itself.

This module exists to make the issued JWT usable by the backend:

- Read and validate JWT access tokens from incoming HTTP requests.
- Reject missing, malformed, expired, or invalid-signature tokens.
- Build a safe authenticated user context from token claims.
- Enforce role-based access for catalog and future operational endpoints.
- Keep public endpoints public.
- Keep sensitive data out of user context and error responses.

## 2. Current Auth State

Implemented security state:

- `security_bootstrap` seeds required roles: `ADMIN`, `SUPERVISOR`, `OPERADOR`, and `CONSULTA`.
- `security_bootstrap` creates the initial `admin` user after Flyway using environment/configuration driven password handling.
- `auth` implements `POST /api/auth/login`.
- Login verifies BCrypt hashes stored in `users.password_hash`.
- Login rejects unknown users and wrong passwords with the same generic invalid-credentials behavior.
- Login rejects inactive users, missing roles, and inactive roles.
- Login issues HMAC-SHA256 JWT access tokens.
- JWT claims currently include:
  - `sub` as user ID string.
  - `userId`.
  - `username`.
  - `role`.
  - `iat`.
  - `exp`.
  - optional `iss`.
- Login responses contain the token, token type, expiration, and safe user summary.
- No endpoint protection exists yet.
- No JWT request validation filter exists yet.
- No role-based authorization exists yet.

JWT compatibility requirement:

- Token validation must remain compatible with the existing `HmacJwtTokenAdapter` token format.
- Current generated tokens use compact JWT format with Base64 URL encoded header/payload/signature, `alg=HS256`, `typ=JWT`, HMAC-SHA256 signing, and the claims listed above.
- If code is duplicated between token generation and validation, prefer extracting a small shared internal JWT utility inside the security/auth adapter boundary.
- Any shared JWT utility must not be placed in domain packages.

Relevant existing schema:

- `users.id` is the stable user identifier.
- `users.username` is the login/display identifier.
- `users.password_hash` must never leave credential verification boundaries.
- `users.active` exists, but per-request active-user revalidation is out of scope for this spec unless explicitly added later.
- `roles.name` stores the role code used in the JWT claim.
- `roles.active` is checked at login time by the current auth module.

## 3. Public Endpoints

The following endpoints must remain public:

| Method | URL | Reason |
|---|---|---|
| `POST` | `/api/auth/login` | Users need this endpoint before they have a token. |
| `GET` | `/api/health` | Health checks must work without user credentials. |

Public endpoints must not require an `Authorization` header.

If a public endpoint receives an invalid `Authorization` header, the implementation may ignore it for that public request. It must not let an invalid token break health checks or login unless a future security policy intentionally changes that behavior.

## 4. Protected Endpoints

All business endpoints must require a valid Bearer token unless explicitly listed as public.

Protected endpoint strategy:

- Deny by default.
- Permit only documented public endpoints without authentication.
- Require authentication for every `/api/**` endpoint not listed as public.
- Enforce role rules after token validation.
- Treat catalog management endpoints separately from future operational lookup endpoints.

Current protected endpoint groups:

- `/api/profiles/**`
- `/api/roles/**`
- `/api/machines/**`
- `/api/shifts/**`
- `/api/container-types/**`
- `/api/containers/**`

Future business endpoint groups should be protected by default when added:

- `/api/receptions/**`
- `/api/inventory/**`
- `/api/cutting/**`
- `/api/scrap/**`
- `/api/molding/**`
- `/api/reports/**`
- `/api/exports/**`
- `/api/users/**`
- `/api/history/**`

## 5. JWT Request Validation

Protected endpoint validation rules:

1. Read the `Authorization` request header.
2. Require the header for protected endpoints.
3. Require the value to use the `Bearer <token>` scheme.
4. Reject blank tokens.
5. Validate compact JWT structure with exactly three Base64 URL encoded parts:
   - header.
   - payload.
   - signature.
6. Decode the JWT header and payload as JSON.
7. Require expected header values:
   - `alg` must be `HS256`.
   - `typ` should be `JWT` when present.
8. Validate the HMAC-SHA256 signature using the configured JWT secret.
9. Validate `exp` against the current clock.
10. Validate `iss` only if `security.jwt.issuer` is configured.
11. Extract required claims:
    - `sub`.
    - `userId`.
    - `username`.
    - `role`.
    - `iat`.
    - `exp`.
12. Require `sub` to match the user ID identity represented by `userId`.
13. Require `role` to be one of:
    - `ADMIN`.
    - `SUPERVISOR`.
    - `OPERADOR`.
    - `CONSULTA`.
14. Reject missing required claims.
15. Reject malformed claim types.
16. Reject malformed, expired, unsupported-algorithm, invalid-issuer, or invalid-signature tokens.

Security rules:

- JWT signing secret must come from environment/configuration.
- JWT signing secret must not be logged.
- JWT signing secret must not be returned in responses.
- JWT signing secret must not be committed to repository files.
- Do not accept `alg=none`.
- Do not accept asymmetric algorithms unless a future spec explicitly changes signing strategy.
- Do not expose parsing internals in error responses.

## 6. Authenticated User Context

The backend should represent the authenticated user with a small request-scoped principal/context.

Required fields:

| Field | Source | Notes |
|---|---|---|
| `userId` | JWT `userId` and `sub` | Stable database user ID. |
| `username` | JWT `username` | Login/display identifier. |
| `role` | JWT `role` | Role code, e.g. `ADMIN`. |
| `expiresAt` | JWT `exp` | Token expiration instant. |

The authenticated context must not contain:

- `password_hash`.
- Raw password.
- JWT signing secret.
- Full token signing internals.
- Database version fields.
- Unnecessary personal data such as `fullName` unless a future use case explicitly needs it.

Spring Security integration may map the token role claim to an authority such as `ROLE_ADMIN` internally. The JWT claim itself should remain `ADMIN`, `SUPERVISOR`, `OPERADOR`, or `CONSULTA`.

Domain and application modules should remain independent from Spring Security classes unless a future use case explicitly needs current-user context. If a future use case needs current-user data, pass a small application command value or port-owned context rather than Spring Security framework types.

## 7. Role-Based Access Matrix

System role expectations:

| Capability | ADMIN | SUPERVISOR | OPERADOR | CONSULTA |
|---|---:|---:|---:|---:|
| Manage catalog records | yes | no | no | no |
| Read administrative catalog endpoints | yes | yes | no | no |
| Use restricted operational lookup endpoints | yes | yes | yes | future decision |
| Manage users | future yes | no | no | no |
| Register receptions | future yes | future yes | future yes | no |
| Approve/cancel receptions | future yes | future yes | no | no |
| Register cutting records | future yes | future limited | future yes | no |
| Register scrap | future yes | future limited | future yes | no |
| Register molding output | future yes | future limited | future yes | no |
| View own current-shift history | yes | yes | future yes | no |
| View full history | future yes | future yes | no | future yes |
| View reports | future yes | future yes | no | future yes |
| Export data | future yes | future decision | no | future decision |

Role details:

### ADMIN

- Can manage catalogs:
  - roles.
  - profiles.
  - machines.
  - shifts.
  - container types.
  - containers.
- Can manage users later when user management exists.
- Can view reports later.
- Can export data later.
- Can view full history later.

### SUPERVISOR

- Can read catalogs.
- Cannot create, update, or soft delete catalogs.
- Can view movements of the day later.
- Can view reports later.
- Can view full history later.
- Can register receptions later.
- Can approve or cancel receptions later if implemented.

### OPERADOR

- Can register operational movements later:
  - receptions.
  - cutting.
  - scrap.
  - molding output.
- Can view own current-shift history later.
- Must not manage catalogs.
- Must not view general reports.
- Must not access administrative catalog management endpoints.

### CONSULTA

- Is read-only.
- Can view reports later.
- Can view history later.
- Must not register operational movements.
- Must not manage catalogs.
- Has no access to current administrative catalog endpoints in this implementation.
- May read catalog data later only when needed for report/history filters; whether this uses separate report lookup endpoints is a future decision.

## 8. Current Endpoint Rules

Current implemented REST controllers:

- `AuthRestController`
- `HealthController`
- `ProfileRestController`
- `RoleRestController`
- `MachineRestController`
- `ShiftRestController`
- `ContainerTypeRestController`
- `ContainerRestController`

### Public Current Endpoints

| Method | URL | Allowed roles |
|---|---|---|
| `POST` | `/api/auth/login` | public |
| `GET` | `/api/health` | public |

### Catalog Management Endpoints

Catalog management endpoints are administrative endpoints. They are not the same as operational dropdown/lookup endpoints.

If `OPERADOR` users later need active machines, shifts, profiles, containers, or container types to register production movements, create separate restricted lookup endpoints. Do not expose full admin catalog management endpoints to operators as a shortcut.

Current catalog endpoint rules:

| Module | Method | URL | Operation | Allowed roles |
|---|---|---|---|---|
| profiles | `GET` | `/api/profiles` | list active | `ADMIN`, `SUPERVISOR` |
| profiles | `GET` | `/api/profiles/{id}` | get by id | `ADMIN`, `SUPERVISOR` |
| profiles | `POST` | `/api/profiles` | create | `ADMIN` |
| profiles | `PUT` | `/api/profiles/{id}` | update | `ADMIN` |
| profiles | `DELETE` | `/api/profiles/{id}` | soft delete | `ADMIN` |
| roles | `GET` | `/api/roles` | list active | `ADMIN`, `SUPERVISOR` |
| roles | `GET` | `/api/roles/{id}` | get by id | `ADMIN`, `SUPERVISOR` |
| roles | `POST` | `/api/roles` | create | `ADMIN` |
| roles | `PUT` | `/api/roles/{id}` | update | `ADMIN` |
| roles | `DELETE` | `/api/roles/{id}` | soft delete | `ADMIN` |
| machines | `GET` | `/api/machines` | list active | `ADMIN`, `SUPERVISOR` |
| machines | `GET` | `/api/machines/{id}` | get by id | `ADMIN`, `SUPERVISOR` |
| machines | `POST` | `/api/machines` | create | `ADMIN` |
| machines | `PUT` | `/api/machines/{id}` | update | `ADMIN` |
| machines | `DELETE` | `/api/machines/{id}` | soft delete | `ADMIN` |
| shifts | `GET` | `/api/shifts` | list active | `ADMIN`, `SUPERVISOR` |
| shifts | `GET` | `/api/shifts/{id}` | get by id | `ADMIN`, `SUPERVISOR` |
| shifts | `POST` | `/api/shifts` | create | `ADMIN` |
| shifts | `PUT` | `/api/shifts/{id}` | update | `ADMIN` |
| shifts | `DELETE` | `/api/shifts/{id}` | soft delete | `ADMIN` |
| container_types | `GET` | `/api/container-types` | list active | `ADMIN`, `SUPERVISOR` |
| container_types | `GET` | `/api/container-types/{id}` | get by id | `ADMIN`, `SUPERVISOR` |
| container_types | `POST` | `/api/container-types` | create | `ADMIN` |
| container_types | `PUT` | `/api/container-types/{id}` | update | `ADMIN` |
| container_types | `DELETE` | `/api/container-types/{id}` | soft delete | `ADMIN` |
| containers | `GET` | `/api/containers` | list active | `ADMIN`, `SUPERVISOR` |
| containers | `GET` | `/api/containers/{id}` | get by id | `ADMIN`, `SUPERVISOR` |
| containers | `POST` | `/api/containers` | create | `ADMIN` |
| containers | `PUT` | `/api/containers/{id}` | update | `ADMIN` |
| containers | `DELETE` | `/api/containers/{id}` | soft delete | `ADMIN` |

`CONSULTA` has no access to current administrative catalog endpoints in this implementation. Future report/history workflows may add separate lookup or filter endpoints for `CONSULTA` if needed.

`OPERADOR` must not access these administrative catalog endpoints. Future operator dropdown data should come from separate lookup endpoints with minimal response fields.

## 9. HTTP Error Behavior

Authentication and authorization failures must be consistent and intentionally low-detail.

| Case | HTTP status | Suggested code | Message rule |
|---|---:|---|---|
| Missing Bearer token on protected endpoint | 401 | `security.missing-token` | Generic authentication required message. |
| Malformed Authorization header | 401 | `security.invalid-token` | Do not expose parser details. |
| Malformed JWT | 401 | `security.invalid-token` | Do not expose parser details. |
| Unsupported JWT algorithm | 401 | `security.invalid-token` | Do not reveal accepted algorithms beyond documentation. |
| Invalid signature | 401 | `security.invalid-token` | Do not distinguish from other invalid token cases in detail. |
| Expired token | 401 | `security.expired-token` | Safe to say token expired. |
| Invalid issuer | 401 | `security.invalid-token` | Do not expose expected issuer value. |
| Missing required claims | 401 | `security.invalid-token` | Do not list missing claim names in response. |
| Valid token but insufficient role | 403 | `security.forbidden` | Generic insufficient permissions message. |
| Inactive/deleted user during request | future decision | future decision | User revalidation belongs to a future users/security spec unless implemented here. |

Error responses must not leak:

- JWT signing secret.
- Raw token contents.
- Password hashes.
- Raw passwords.
- Stack traces.
- Internal parser exception messages.
- Whether a username exists.
- Internal authorization rule names that reveal implementation details.

## 10. Security Review Requirement

The future implementation plan and implementation must explicitly run the security reviewer/agent after implementation.

Use the project security reviewer guidance, currently represented by:

```text
.agents/agents/hexagonal-security-reviewer.md
```

The security review must check:

- Controllers do not return JPA entities.
- Controllers do not return `password_hash`.
- Controllers do not return raw passwords.
- Controllers do not return JWT secret values.
- Controllers do not return internal token signing data.
- Controllers do not return stack traces.
- Controllers do not return unnecessary fields.
- Authenticated user context does not expose sensitive data.
- Error responses do not reveal token parsing internals.
- Error responses do not reveal whether a username exists.
- Role checks cannot be bypassed by calling controllers directly through the Spring-managed endpoint path or security proxy.
- Catalog write endpoints are not accessible to `SUPERVISOR`, `OPERADOR`, or `CONSULTA`.
- Operator access does not accidentally expose admin catalog management.
- Future lookup endpoints stay separate from admin catalog endpoints when operators need dropdown data.
- `/api/auth/login` remains public.
- `/api/health` remains public.
- All other `/api/**` endpoints are denied by default unless explicitly configured.

Security review findings must be addressed before the feature is considered complete.

## 11. Hexagonal / Spring Security Boundary

Expected implementation root:

```text
src/main/java/com/example/company/security/
```

This spec explicitly covers endpoint protection, so implementation may add Spring Security web configuration here.

Expected structure:

```text
security/
  adapter/
    in/
      web/
        SecurityErrorResponse.java
        SecurityExceptionHandler.java
    out/
      jwt/
        JwtRequestValidationAdapter.java
        JwtValidationProperties.java
  config/
    SecurityConfiguration.java
    RoleAuthorizationConfiguration.java
  filter/
    JwtAuthenticationFilter.java
  model/
    AuthenticatedUserPrincipal.java
    AuthenticatedUserContext.java
```

Expected responsibilities:

- Spring Security dependency
  - Add only the Spring Security web dependencies needed for `SecurityFilterChain`, request authentication, and authorization.
  - Do not add OAuth2 server/client dependencies unless a future spec explicitly requests them.

- `JwtRequestValidationAdapter`
  - Validates token structure, HMAC signature, expiration, issuer, and required claims.
  - Uses the same HMAC secret configuration as token generation.
  - Does not log tokens or secrets.
  - Remains compatible with tokens produced by `HmacJwtTokenAdapter`.
- `JwtAuthenticationFilter`
  - Reads the `Authorization` header.
  - Skips documented public endpoints.
  - Rejects protected requests with missing or invalid tokens.
  - Builds an authenticated principal/context for valid tokens.
- `AuthenticatedUserPrincipal` or equivalent
  - Carries `userId`, `username`, `role`, and token expiration only.
  - Exposes Spring Security authorities derived from role.
- `SecurityConfiguration`
  - Permits public endpoints.
  - Requires authentication for protected endpoints.
  - Denies unspecified `/api/**` endpoints by default.
  - Configures stateless request handling.
- `RoleAuthorizationConfiguration`
  - Defines role access for current catalog endpoints.
  - Keeps catalog write endpoints `ADMIN` only.
  - Keeps catalog read endpoints `ADMIN` and `SUPERVISOR` only for now.
- `SecurityExceptionHandler`
  - Maps authentication failures to 401.
  - Maps authorization failures to 403.
  - Returns safe error responses.

Implementation may choose another class naming scheme if the same boundaries are preserved.

Domain/application boundary:

- Domain modules must not import Spring Security.
- Application use cases must not depend on Spring Security classes for this spec.
- REST controllers should continue to call input ports and return DTOs.
- Security configuration may protect URL patterns and/or Spring-managed controller methods.
- If method-level authorization is used, keep annotations in inbound adapters or security configuration boundaries, not domain models.
- If future use cases need authenticated user data, pass a small command/context object from the inbound adapter rather than passing Spring Security framework objects into domain/application code.

## 12. Acceptance Criteria

Future implementation must satisfy these testable statements:

1. `POST /api/auth/login` remains public.
2. `GET /api/health` remains public.
3. Protected endpoints reject missing tokens with HTTP 401.
4. Protected endpoints reject malformed tokens with HTTP 401.
5. Protected endpoints reject invalid-signature tokens with HTTP 401.
6. Protected endpoints reject expired tokens with HTTP 401.
7. Protected endpoints reject tokens missing required claims with HTTP 401.
8. Protected endpoints reject invalid issuer when issuer is configured with HTTP 401.
9. A valid `ADMIN` token can create, read, update, and soft delete catalog records.
10. A valid `SUPERVISOR` token can read catalog records.
11. A valid `SUPERVISOR` token cannot create, update, or soft delete catalog records.
12. A valid `OPERADOR` token cannot access administrative catalog management endpoints.
13. A valid `CONSULTA` token cannot access current administrative catalog endpoints.
14. A valid `CONSULTA` token cannot create, update, or soft delete anything.
15. Controllers do not expose JPA entities.
16. Responses do not expose `password_hash`.
17. Responses do not expose raw passwords.
18. Responses do not expose JWT secrets.
19. Responses do not expose internal token signing data.
20. Security error responses do not expose stack traces.
21. Security error responses do not expose token parsing internals.
22. Authenticated user context contains only `userId`, `username`, `role`, and token expiration.
23. Catalog write endpoints are `ADMIN` only.
24. Catalog read endpoints are `ADMIN` and `SUPERVISOR` only for current admin catalog controllers.
25. Operator dropdown requirements are handled by future lookup endpoints, not admin catalog endpoints.
26. Role checks cannot be bypassed through Spring-managed controller invocation or endpoint routing.
27. The security reviewer/agent is run after implementation.
28. Security reviewer findings are addressed before completion.
29. Existing auth login tests continue to pass.
30. Existing architecture tests continue to pass.
31. `V1__create_initial_schema.sql` remains unchanged.
32. No Flyway migration is created for this endpoint authorization spec.

## 13. Out Of Scope

This spec explicitly excludes:

- Login implementation changes unless required for compatibility with request validation.
- Refresh tokens.
- Password reset.
- User management.
- Angular frontend.
- Angular route guards.
- GraphQL.
- Operational modules not yet implemented.
- Reception implementation.
- Inventory implementation.
- Cutting implementation beyond existing domain value object.
- Scrap implementation.
- Molding output implementation.
- Reports implementation.
- Exports implementation.
- History implementation.
- New database tables.
- Modifying existing Flyway migrations.
- Creating new Flyway migrations.
- Reworking catalog CRUD behavior.
- Adding operator lookup endpoints in this task.

The next step after approval should be creating an implementation plan from this spec, not writing implementation code immediately.
