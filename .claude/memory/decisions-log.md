# Decisions Log

Append architectural and design decisions here. Entries are append-only and use ISO dates.

## 2026-06-11 [Use Hexagonal Architecture]

**Context:** The backend must support manufacturing traceability modules without coupling business rules to REST, JPA, or future GraphQL adapters.
**Options considered:** Keep a traditional Spring controller-service-repository structure; use Hexagonal Architecture / Ports and Adapters.
**Decision:** Use Hexagonal Architecture / Ports and Adapters.
**Reason:** The business rules and use cases must be reusable across REST, future GraphQL, tests, and background jobs.
**Consequences:** Domain stays pure Java, adapters translate external concerns, and all new modules follow the standard hexagonal package structure.

## 2026-06-11 [Keep Ports In Domain]

**Context:** The project needed one consistent location for use case interfaces and output contracts.
**Options considered:** Put ports under `application/port`; put ports under `domain/port/in` and `domain/port/out`.
**Decision:** Put ports under `domain/port/in` and `domain/port/out`.
**Reason:** This matches the repository guide and makes ports part of the core contract instead of an adapter detail.
**Consequences:** Controllers call input ports from domain, use cases implement input ports, and persistence adapters implement output ports from domain.

## 2026-06-11 [Keep Flyway Migrations Append-Only]

**Context:** The database schema is already defined and includes the manufacturing tables and cutting quantity constraint.
**Options considered:** Modify existing migrations when code changes; only add new migrations when schema changes are explicitly needed.
**Decision:** Do not modify existing migrations. Add a new migration only for explicit schema changes.
**Reason:** Existing migration history must remain stable once shared.
**Consequences:** Java persistence mapping must adapt to the current schema, and changes to `V1__create_initial_schema.sql` are not allowed.

## 2026-06-11 [Split Application Services By Use Case]

**Context:** The profile pilot needed clear transaction boundaries and reusable operations.
**Options considered:** Keep one large service class per module; create one application service class per use case.
**Decision:** Create one application service class per use case.
**Reason:** Smaller use case classes make transactions, authorization, tests, and port wiring easier to reason about.
**Consequences:** New modules should prefer names like `CreateProfileService`, `GetProfileService`, `UpdateProfileService`, and `DeleteProfileService`.

## 2026-06-11 [Use ArchUnit For Architecture Boundaries]

**Context:** Hexagonal boundaries are easy to break accidentally with imports from Spring, JPA, or adapters.
**Options considered:** Rely on manual review only; add automated architecture tests.
**Decision:** Add ArchUnit architecture tests.
**Reason:** Automated tests catch dependency direction violations before merge.
**Consequences:** Domain, application, and adapter dependency rules are enforced by `HexagonalArchitectureTest`.

## 2026-06-11 [Defer GraphQL Runtime]

**Context:** Agent and skill workflows include GraphQL guidance, but the backend has no schema or resolver files yet.
**Options considered:** Add Spring GraphQL immediately; keep only the workflow guidance until a GraphQL feature is requested.
**Decision:** Do not add GraphQL runtime dependencies until explicitly requested.
**Reason:** Avoid adding unused runtime surface area before there is a real GraphQL requirement.
**Consequences:** GraphQL work starts with schema files, resolvers that call input ports, and a schema-sync pass when the user asks for it.

## 2026-06-11 [Do Not Create Frontend Yet]

**Context:** The repository currently contains only the backend.
**Options considered:** Scaffold a frontend immediately; wait until the user chooses or approves a frontend.
**Decision:** Do not create a frontend without explicit user approval.
**Reason:** The correct frontend framework should be detected or chosen before files are created.
**Consequences:** When frontend work begins, detect existing files first. If none exist, recommend React + Vite + TypeScript for fast operational UI work unless Angular is chosen.

## 2026-06-12 [Keep Roles Catalog Separate From Auth]

**Context:** The base catalogs spec includes `roles`, but authentication, user management, role seeding, and endpoint protection have separate security concerns.
**Options considered:** Implement roles catalog together with JWT, users, seeding, and authorization; implement only catalog CRUD and leave security behavior to dedicated specs.
**Decision:** Keep the `roles` module as base catalog CRUD only for now.
**Reason:** Catalog behavior can be implemented against the existing `roles` table without introducing password handling, token issuance, method security, or production seed-data policy.
**Consequences:** Required roles such as ADMIN, SUPERVISOR, OPERADOR, and CONSULTA still need a separate role-seeding spec, and endpoint authorization needs a separate security spec.

## 2026-06-12 [Split Security Bootstrap Static Data From Credentials]

**Context:** Required roles are static platform data, but the initial admin password must come from environment/configuration.
**Options considered:** Seed roles and users together in SQL; seed only roles with Flyway and create users through application bootstrap.
**Decision:** Required roles may be seeded with a new append-only Flyway migration, while the initial `ADMIN` user and optional demo users must be created by an idempotent application bootstrap component after Flyway.
**Reason:** Static SQL must not contain environment-derived passwords, password hashes, or production secrets.
**Consequences:** The bootstrap implementation must read `SECURITY_BOOTSTRAP_ADMIN_PASSWORD`, hash it, avoid overwriting existing users, and keep demo users profile-controlled for local/dev/test only.

## 2026-06-12 [Keep Auth JWT Login Separate From Endpoint Authorization]

**Context:** The backend needs a usable login endpoint for bootstrapped users, but protected endpoint rules and JWT request filters have broader authorization implications.
**Options considered:** Implement login, JWT issuance, filters, and role authorization together; implement only login and token issuance now.
**Decision:** Implement only `POST /api/auth/login`, BCrypt verification, and HMAC JWT issuance in the `auth` module.
**Reason:** The login boundary can be tested against existing `users` and `roles` tables without introducing request filter behavior, endpoint protection, or role policy before those specs are approved.
**Consequences:** Future endpoint authorization must add Spring Security filter chain configuration, JWT request filters, protected endpoint rules, and role-based authorization in a separate spec/task.

## 2026-06-12 [Use HMAC JWT Without Additional JWT Library]

**Context:** The auth JWT spec requires HMAC signing for access tokens and allows adding only the smallest focused dependency if needed.
**Options considered:** Add a dedicated JWT dependency; implement compact JWT generation with Java crypto and existing Jackson serialization.
**Decision:** Use HMAC-SHA256 through Java crypto and Base64 URL encoding, with Jackson for JSON serialization.
**Reason:** The current requirement is narrow token generation, so a new JWT library is not necessary yet.
**Consequences:** `security.jwt.secret` / `SECURITY_JWT_SECRET` must provide the signing secret, endpoint authorization validates the existing HMAC token format, and a future asymmetric-key spec may revisit the library choice.

## 2026-06-13 [Use Angular Frontend Under frontend/]

**Context:** Frontend work is about to start and the repository currently has the Spring Boot backend at root.
**Options considered:** Move backend into backend/ now; keep backend at root and create frontend under frontend/.
**Decision:** Keep backend at root for now and create the Angular app under frontend/ later.
**Reason:** This avoids a risky backend path/Gradle/agent rewrite while creating a clear frontend boundary.
**Consequences:** Future frontend commands must operate under frontend/. Backend commands remain at repo root. A backend/ frontend/ monorepo restructure can be planned separately later.
