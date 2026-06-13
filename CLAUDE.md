# tesla-system — Monorepo

## What this project is

Manufacturing traceability backend for rubber profile cutting, plus a future Angular frontend.

Business flow:
Reception → Inventory → Cutting → Scrap → Molding Output

| Package | Description |
|---|---|
| `tesla-api/` | Spring Boot 4.0.6 hexagonal backend, Java 26, PostgreSQL, Flyway |
| `tesla-web-app/` | Angular frontend (not scaffolded yet — skeleton only) |

Read the relevant sub-package `CLAUDE.md` before working in that package.

## Monorepo structure

The two packages are independent. Always `cd` into the target package before running commands.

## Feature development workflow

| Command | Purpose |
|---|---|
| `/spec <idea>` | Creates `_specs/<slug>.md` and switches to a new feature branch |
| `/plan-spec <slug>` | Reads a spec and produces `_plans/<slug>.md` |

Spec files live in `_specs/`, plan files live in `_plans/`. Both are at the monorepo root.

Standard flow: `/spec` → review → `/plan-spec` → review → implement using agents.

## Specialized agents — use these automatically, never do the work inline

| Agent | When to use |
|---|---|
| `hexagonal-domain-developer` | Domain model, aggregates, value objects, domain events, domain exceptions, ports |
| `hexagonal-application-developer` | Use cases, transaction boundaries, command/result mapping, port wiring |
| `hexagonal-persistence-adapter` | JPA entities, Spring Data repos, persistence mappers, Flyway planning, N+1 fixes |
| `hexagonal-web-adapter` | REST controllers, request/response DTOs, validation, HTTP status semantics |
| `hexagonal-graphql-adapter` | GraphQL schema, resolvers, DataLoader/BatchMapping (when GraphQL is enabled) |
| `hexagonal-exception-handler` | Global exception handler, domain error mapping, REST/GraphQL error responses |
| `hexagonal-security-reviewer` | Auth, JWT filters, SecurityFilterChain, authorization placement, password handling |
| `hexagonal-test-engineer` | Domain tests, use case tests with mocked ports, adapter tests, ArchUnit rules |
| `frontend-developer` | Angular pages, components, services, API clients, guards, interceptors, forms |

## Mandatory behavior — always follow this before any work

Before writing any code or editing any file, Claude must:

1. **Read memory first**
   - `.claude/memory/project-context.md`
   - `.claude/memory/decisions-log.md`

2. **Use the graph before Grep/Glob/Read**
   - Use `code-review-graph` MCP tools to find related files, callers, impact radius
   - Only fall back to Grep/Glob when the graph doesn't cover it

3. **Route to the correct agent — never do the work inline**
   - Check the agents table above and launch the right agent
   - Pass full context in the brief: task, file paths, CLAUDE.md conventions, current file content, resolved dependencies

4. **Use skills when creating new files**
   - Check the skills table above before creating any new file
   - Invoke the matching skill with full context

5. **Run security review after any auth/query/middleware change**
   - Launch `hexagonal-security-reviewer` before continuing to the next phase

6. **Update memory after completing any feature**
   - Update `.claude/memory/project-context.md` with new module state
   - Append to `.claude/memory/features-log.md`
   - Append to `.claude/memory/decisions-log.md` if an architectural decision was made

Never skip these steps even for small changes. An agent without full context wastes 3-5x more tokens.

## Skills — use these when creating new files

| Skill | Creates |
|---|---|
| `$create-hexagonal-feature` | Complete hexagonal module: domain → ports → use case → persistence → REST |
| `$create-angular-feature` | Angular feature: routes, API client, service, page, form, tests |
| `$create-angular-auth-flow` | Angular auth: token storage, interceptor, guards, login page, 401/403 handling |
| `$enforce-hexagonal-boundaries` | Boundary check with rg + ArchUnit rules |
| `$review-hexagonal-changes` | Pre-commit review: domain purity, port direction, adapter bypass, migration safety |
| `$review-angular-changes` | Pre-commit review: component/service separation, interceptor scope, role guard |
| `$graphql-mcp-workflow` | GraphQL schema sync, resolver audit, MCP context summary |
| `$explore-codebase` | Codebase exploration before planning |
| `$debug-hexagonal-issue` | Diagnose boundary violations, port wiring failures, Flyway issues |
| `$refactor-to-hexagonal` | Migrate legacy layered code to hexagonal structure |

## Running the project

- Backend: `cd tesla-api && .\gradlew.bat bootRun`
- Backend tests: `cd tesla-api && .\gradlew.bat test`
- Frontend: `cd tesla-web-app && ng serve` (after Angular is scaffolded)

## Memory and decisions

- Project context: `.claude/memory/project-context.md`
- Architecture decisions: `.claude/memory/decisions-log.md`
- Features log: `.claude/memory/features-log.md`

Read memory files at the start of any session involving architecture changes or new features.