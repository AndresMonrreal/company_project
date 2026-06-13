# Plan: Endpoint Authorization And JWT Request Validation

Source spec:

```text
_specs/endpoint-authorization.md
```

## Scope

Implement endpoint protection for the current REST API using the JWT access tokens issued by `POST /api/auth/login`.

Included now:

- Spring Security web dependency for filter chain, request authentication, and authorization.
- JWT request filter.
- HMAC JWT validation compatible with `HmacJwtTokenAdapter`.
- Authenticated principal/context containing only `userId`, `username`, `role`, and token expiration.
- Public endpoints:
  - `POST /api/auth/login`.
  - `GET /api/health`.
- Protected-by-default behavior for all other `/api/**` endpoints.
- Current catalog role authorization:
  - `ADMIN` can create, read, update, and soft delete catalog records.
  - `SUPERVISOR` can read catalog records only.
  - `OPERADOR` cannot access current administrative catalog endpoints.
  - `CONSULTA` cannot access current administrative catalog endpoints.
- Safe 401 responses for missing, malformed, expired, invalid-signature, invalid-issuer, unsupported-algorithm, and missing-claim tokens.
- Safe 403 responses for valid tokens with insufficient role.
- Focused tests for token validation, filter behavior, public endpoints, protected endpoints, and role rules.
- Security review using `.agents/agents/hexagonal-security-reviewer.md`.

Out of scope:

- Login behavior changes unless required for compatibility.
- Refresh tokens.
- Password reset.
- User management.
- Angular frontend.
- Angular route guards.
- GraphQL.
- Operational modules.
- Reports.
- Exports.
- History.
- Operator lookup endpoints.
- New database tables.
- New Flyway migrations.
- Changes to `V1__create_initial_schema.sql`.
- OAuth2 server/client.

## Implementation Boundary

- Keep domain and application modules independent from Spring Security.
- Put Spring Security-specific code under `src/main/java/com/example/company/security/`.
- REST controllers continue returning DTOs, not JPA entities.
- Do not expose `password_hash`, raw passwords, JWT secrets, stack traces, token internals, or unnecessary user data.
- Do not log raw tokens or JWT secrets.
- Token validation uses the same `security.jwt.secret` and optional `security.jwt.issuer` configuration as token generation.
- Deny unspecified `/api/**` endpoints by default unless explicitly public.
- Do not modify Flyway migrations.

## Phase 1: Spec Clarifications

Modify:

- `_specs/endpoint-authorization.md`

Expected updates:

- Add Spring Security web dependency allowance.
- Explicitly exclude OAuth2 server/client dependencies.
- Add JWT compatibility with `HmacJwtTokenAdapter`.
- Prefer a shared internal JWT utility if duplication becomes meaningful.
- Clarify that `CONSULTA` has no access to current administrative catalog endpoints in this implementation.

## Phase 2: Dependencies

Modify:

- `build.gradle`

Expected behavior:

- Add `org.springframework.boot:spring-boot-starter-security`.
- Do not add OAuth2 server/client dependencies.
- Keep existing `spring-security-crypto` dependency available for BCrypt.

## Phase 3: JWT Validation Model

Create:

- `src/main/java/com/example/company/security/model/AuthenticatedUserContext.java`
- `src/main/java/com/example/company/security/model/AuthenticatedUserPrincipal.java`
- `src/main/java/com/example/company/security/adapter/out/jwt/JwtValidationResult.java`
- `src/main/java/com/example/company/security/adapter/out/jwt/JwtValidationException.java`

Expected behavior:

- Principal/context expose only `userId`, `username`, `role`, and token expiration.
- Validation errors carry safe internal categories for mapping to 401 responses.
- No passwords, hashes, secrets, or raw token text appear in models or messages.

## Phase 4: JWT Validation Adapter

Create:

- `src/main/java/com/example/company/security/adapter/out/jwt/JwtRequestValidationAdapter.java`

Expected behavior:

- Validate compact JWT structure.
- Validate `alg=HS256` and optional `typ=JWT`.
- Validate HMAC-SHA256 signature with `JwtProperties`.
- Validate `exp`.
- Validate optional configured issuer.
- Require `sub`, `userId`, `username`, `role`, `iat`, and `exp`.
- Require `sub` to match `userId`.
- Require role to be one of `ADMIN`, `SUPERVISOR`, `OPERADOR`, or `CONSULTA`.
- Remain compatible with `HmacJwtTokenAdapter`.
- Do not log tokens or secrets.

## Phase 5: Security Error Handling

Create:

- `src/main/java/com/example/company/security/adapter/in/web/SecurityErrorResponse.java`
- `src/main/java/com/example/company/security/adapter/in/web/SecurityErrorWriter.java`

Expected behavior:

- Write safe 401/403 JSON responses.
- Avoid stack traces, parser details, token contents, and secrets.
- Map expired tokens to `security.expired-token`.
- Map other token failures to safe generic invalid/missing token codes.
- Map insufficient role to `security.forbidden`.

## Phase 6: JWT Request Filter

Create:

- `src/main/java/com/example/company/security/filter/JwtAuthenticationFilter.java`

Expected behavior:

- Skip public endpoints.
- Read `Authorization` header for protected endpoints.
- Require `Bearer <token>`.
- Reject missing/malformed/invalid tokens with safe 401 response.
- Build `AuthenticatedUserPrincipal`.
- Set `SecurityContextHolder` authentication for valid tokens.
- Clear security context after request processing.

## Phase 7: Security Configuration

Create:

- `src/main/java/com/example/company/security/config/SecurityConfiguration.java`

Expected behavior:

- Stateless Spring Security filter chain.
- CSRF disabled for stateless REST API.
- `POST /api/auth/login` public.
- `GET /api/health` public.
- Catalog reads permit `ADMIN` and `SUPERVISOR`.
- Catalog writes permit `ADMIN`.
- `OPERADOR` and `CONSULTA` denied from current administrative catalog endpoints.
- Unspecified `/api/**` requests require authentication and are denied unless future rules are added.
- Safe authentication entry point and access denied handler.

## Phase 8: Tests

Create focused tests:

- `src/test/java/com/example/company/security/adapter/out/jwt/SecurityJwtRequestValidationAdapterTest.java`
- `src/test/java/com/example/company/security/filter/SecurityJwtAuthenticationFilterTest.java`
- `src/test/java/com/example/company/security/config/SecurityConfigurationTest.java`

Test cases:

- Valid token generated by `HmacJwtTokenAdapter` validates successfully.
- Malformed token is rejected.
- Unsupported algorithm is rejected.
- Invalid signature is rejected.
- Expired token is rejected.
- Invalid issuer is rejected when issuer is configured.
- Missing required claims are rejected.
- Missing token on protected endpoint returns 401.
- Malformed token on protected endpoint returns 401.
- Expired token on protected endpoint returns 401.
- Invalid signature on protected endpoint returns 401.
- `POST /api/auth/login` remains public.
- `GET /api/health` remains public.
- Valid `ADMIN` token can access catalog reads and writes.
- Valid `SUPERVISOR` token can read catalogs but cannot write.
- Valid `OPERADOR` token cannot access current admin catalog endpoints.
- Valid `CONSULTA` token cannot access current admin catalog endpoints.
- Safe 401/403 bodies do not expose secrets or parser details.

Test constraints:

- Use test-only JWT secrets.
- Disable or satisfy bootstrap where Spring context tests need it.
- Do not require a real admin password.

## Phase 9: Security Review

Run the reviewer guidance from:

```text
.agents/agents/hexagonal-security-reviewer.md
```

Review checklist:

- Controllers do not return JPA entities.
- Controllers do not expose `password_hash`, raw passwords, JWT secrets, stack traces, token internals, or unnecessary fields.
- Authenticated user context exposes only `userId`, `username`, `role`, and token expiration.
- 401/403 responses do not leak parser internals.
- Catalog write endpoints are `ADMIN` only.
- Catalog read endpoints are `ADMIN` and `SUPERVISOR` only.
- `OPERADOR` and `CONSULTA` cannot access current administrative catalog endpoints.
- `/api/auth/login` and `/api/health` remain public.
- All other `/api/**` endpoints are denied by default.

Address findings before completion.

## Phase 10: Verification

Run:

```powershell
.\gradlew.bat compileJava testClasses
.\gradlew.bat test --tests "*Security*"
.\gradlew.bat test --tests "*Auth*"
.\gradlew.bat test --tests "*HexagonalArchitectureTest"
```

Also review:

```powershell
git diff -- _specs/endpoint-authorization.md _plans/endpoint-authorization.md build.gradle src/main/java/com/example/company/security src/test/java/com/example/company/security src/main/resources/db/migration/V1__create_initial_schema.sql
git status --short --branch
```

## Expected Files Created

- `_plans/endpoint-authorization.md`
- `src/main/java/com/example/company/security/model/AuthenticatedUserContext.java`
- `src/main/java/com/example/company/security/model/AuthenticatedUserPrincipal.java`
- `src/main/java/com/example/company/security/adapter/out/jwt/JwtValidationResult.java`
- `src/main/java/com/example/company/security/adapter/out/jwt/JwtValidationException.java`
- `src/main/java/com/example/company/security/adapter/out/jwt/JwtRequestValidationAdapter.java`
- `src/main/java/com/example/company/security/adapter/in/web/SecurityErrorResponse.java`
- `src/main/java/com/example/company/security/adapter/in/web/SecurityErrorWriter.java`
- `src/main/java/com/example/company/security/filter/JwtAuthenticationFilter.java`
- `src/main/java/com/example/company/security/config/SecurityConfiguration.java`
- `src/test/java/com/example/company/security/adapter/out/jwt/SecurityJwtRequestValidationAdapterTest.java`
- `src/test/java/com/example/company/security/filter/SecurityJwtAuthenticationFilterTest.java`
- `src/test/java/com/example/company/security/config/SecurityConfigurationTest.java`

## Expected Files Modified

- `_specs/endpoint-authorization.md`
- `build.gradle`
- `.agents/memory/project-context.md`
- `.agents/memory/features-log.md`
- `.agents/memory/decisions-log.md`
- `.agents/memory/issues-log.md` only if a bug or security issue is found and fixed.

Do not modify:

- `src/main/resources/db/migration/V1__create_initial_schema.sql`
- Existing Flyway migrations.
- Operational modules.
- Frontend files.
- GraphQL files.

## Acceptance Checklist

- Spring Security filter chain protects REST endpoints.
- JWT request filter validates HMAC tokens compatible with existing auth token generation.
- Public endpoints remain public.
- All other `/api/**` endpoints are protected by default.
- Catalog read endpoints allow `ADMIN` and `SUPERVISOR`.
- Catalog write endpoints allow only `ADMIN`.
- `OPERADOR` and `CONSULTA` cannot access current administrative catalog endpoints.
- Missing/invalid/expired tokens return safe 401 responses.
- Insufficient role returns safe 403 responses.
- Authenticated principal/context exposes only `userId`, `username`, `role`, and token expiration.
- Domain/application packages remain free of Spring Security dependencies.
- No OAuth2 dependencies are added.
- No migrations or tables are added.
- Security review is completed and findings are addressed.
