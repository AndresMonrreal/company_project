# tesla-api — Spring Boot Backend

## Stack

- Spring Boot 4.0.6, Java 26, Gradle Wrapper
- PostgreSQL + Flyway (migrations only — `ddl-auto=validate`)
- Spring Security Crypto (BCrypt password hashing and verification)
- Spring Security Web (JWT request filter and endpoint authorization — `security` module)
- Spring Data JPA + Spring Web MVC + Spring Validation
- JUnit 5 + Spring Boot Test + ArchUnit
- GraphQL: NOT enabled yet — add `spring-boot-starter-graphql` only when explicitly requested

## Real package base

```
com.example.company.<module>
```

Agent examples use `com.empresa.app` — always adapt to `com.example.company` before editing source files.

## Hexagonal architecture — standard module structure

```
tesla-api/src/main/java/com/example/company/<module>/

  domain/
    model/          ← pure Java, zero Spring/JPA/Jackson/GraphQL imports
    event/
    exception/      ← extend DomainException, expose DomainErrorType + stable error code
    port/
      in/           ← use case interfaces + commands + results
      out/          ← repository/external service interfaces
    service/        ← pure domain services (multi-aggregate logic only)

  application/
    usecase/        ← one class per use case, @Transactional here only
    mapper/         ← LoginResultMapper, ProfileResultMapper, etc.

  adapter/
    in/
      web/
        dto/        ← REST request/response DTOs
        graphql/    ← GraphQL resolvers (when enabled)
    out/
      persistence/  ← JPA entity, Spring Data repo, persistence mapper, adapter
      external/     ← external service adapters
```

## Rules — non-negotiable

- Domain has ZERO Spring, JPA, Jackson, GraphQL, servlet, DTO, or adapter imports
- Application layer imports only domain — never adapters
- Controllers call input ports — never application classes directly
- Application services inject output ports — never Spring Data repos directly
- JPA entities stay inside `adapter/out/persistence`
- REST DTOs stay inside `adapter/in/web/dto`
- Never modify existing Flyway migrations — append new ones only
- Migration naming: `V{number}__{description}.sql`
- Never expose `password_hash` in API responses, logs, result objects, or JWT claims
- Raw passwords must be request-only transient values — never logged, stored, or returned
- JWT secret comes from environment/configuration — never committed to source

## Completed modules

| Module | State |
|---|---|
| `profiles` | Complete — domain, ports, use cases, REST, JPA, tests |
| `container_types` | Complete — domain, ports, use cases, REST, JPA, tests |
| `containers` | Complete — domain, ports, use cases, REST, JPA, tests |
| `machines` | Complete — domain, ports, use cases, REST, JPA, tests |
| `shifts` | Complete — domain, ports, use cases, REST, JPA, tests |
| `roles` | Complete — catalog CRUD only, no auth/seeding |
| `security_bootstrap` | Complete — role seeding (V2 migration) + admin/demo user bootstrap |
| `auth` | Complete — `POST /api/auth/login`, BCrypt verification, HMAC-SHA256 JWT |
| `cutting` | Partial — `CuttingQuantities` value object + domain invariant only |
| `shared` | Complete — `DomainException`, `GlobalExceptionHandler`, `HealthController` |

## Critical business rule

The cutting invariant must be enforced in domain before persistence:

```java
if (initialQuantity != goodQuantity + scrapQuantity) {
    throw new IllegalArgumentException("initial_quantity must equal good_quantity + scrap_quantity");
}
```

Also enforced by `cutting_quantity_rule` check constraint in V1 migration. Domain enforcement comes first.

## Error code conventions

- Format: `<module>.<error-type>` — e.g. `machine.duplicate-name`, `shift.not-found`, `auth.invalid-credentials`
- `DomainErrorType.NOT_FOUND` → 404
- `DomainErrorType.CONFLICT` → 409
- `DomainErrorType.BUSINESS_RULE` → 422
- Auth: unknown username and wrong password both return `auth.invalid-credentials` — never reveal which part failed

## Agent routing

Use these agents — do not do the work inline:

- Domain model/invariants/ports/events → `hexagonal-domain-developer`
- Use cases/transactions/orchestration → `hexagonal-application-developer`
- JPA/Flyway/persistence mapping/N+1 → `hexagonal-persistence-adapter`
- REST controllers/DTOs/validation → `hexagonal-web-adapter`
- GraphQL schema/resolvers/DataLoader → `hexagonal-graphql-adapter`
- REST/GraphQL error mapping → `hexagonal-exception-handler`
- Tests and ArchUnit → `hexagonal-test-engineer`
- Security/auth/JWT/roles → `hexagonal-security-reviewer`

## Mandatory behavior for every backend task

1. Read `.claude/memory/project-context.md` before starting
2. Use `code-review-graph` MCP tools before any Grep/Glob/Read
3. Route to the correct agent — never implement inline
4. Use `$create-hexagonal-feature` when creating a new module
5. Use `$enforce-hexagonal-boundaries` after any domain/adapter change
6. Use `$review-hexagonal-changes` before any commit
7. Run `hexagonal-security-reviewer` after any auth, JWT, or query change
8. Never modify existing Flyway migrations — append only
9. Update `.claude/memory/project-context.md` after completing any module

## Flyway state

- `V1__create_initial_schema.sql` — full schema, all tables, cutting_quantity_rule constraint — DO NOT MODIFY
- `V2__seed_required_roles.sql` — seeds ADMIN, SUPERVISOR, OPERADOR, CONSULTA roles

## JWT configuration

- Signing: HMAC-SHA256 via Java crypto (no external JWT library)
- Claims: `sub` (userId as string), `userId`, `username`, `role`, `iat`, `exp`, optional `iss`
- Config: `security.jwt.secret` / `SECURITY_JWT_SECRET`, `security.jwt.expiration`, `security.jwt.issuer`
- Endpoint authorization is in the `security` module — NOT in `auth`

## Commands

```powershell
cd tesla-api
.\gradlew.bat bootRun           # run backend
.\gradlew.bat test              # run all tests
.\gradlew.bat test --tests "*Auth*"
.\gradlew.bat test --tests "*HexagonalArchitectureTest"
.\gradlew.bat compileJava testClasses
```
