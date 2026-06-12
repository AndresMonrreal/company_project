# MEMORY RULE

Before starting any task, read all files under `.agents/memory/` in this order:

1. `.agents/memory/project-context.md`
2. `.agents/memory/issues-log.md`
3. `.agents/memory/decisions-log.md`
4. `.agents/memory/features-log.md`

After completing any task, update the relevant memory files before closing the conversation.

# Project Agent Guide

This file is the root working guide for Codex in this repository.

## Project

This repository contains a Spring Boot backend for a manufacturing traceability system for rubber profile cutting.

Business flow:

```text
Reception -> Inventory -> Cutting -> Scrap -> Molding Output
```

Core modules: `auth`, `profiles`, `machines`, `shifts`, `containers`, `reception`, `inventory`, `cutting`, `scrap`, `molding`, and `reports`.

Support modules: `users`, `roles`, `traceability`, and `exports`.

All profiles such as 36, 37, 38, and 39 follow the same production flow.

## Current Stack

- Java 26 toolchain
- Spring Boot 4.0.6
- Gradle Wrapper
- Spring Web MVC
- Spring Validation
- Spring Data JPA
- PostgreSQL
- Flyway migrations
- JUnit with Spring Boot test support
- ArchUnit for automated architecture boundary tests

There is no frontend app in this repository yet. When frontend work starts, detect the actual framework from files before editing. If no frontend exists, ask before creating one. Default recommendation for fast business UI work is React + Vite + TypeScript unless the user explicitly chooses Angular.

## Architecture

Use Hexagonal Architecture / Ports and Adapters.

Standard module structure:

```text
src/main/java/com/example/company/<module>/
  domain/
    model/
    event/
    exception/
    port/
      in/
      out/
    service/
  application/
    usecase/
    mapper/
  adapter/
    in/
      web/
        dto/
        graphql/
    out/
      persistence/
      external/
```

Domain code must not import Spring, JPA, Jackson, GraphQL, servlet APIs, repositories, controllers, DTO classes, or adapters.

## Critical Business Rule

The cutting rule is mandatory and belongs in domain:

```text
initial_quantity = good_quantity + scrap_quantity
```

The database already enforces this with Flyway, but the domain must reject invalid values before persistence.

## Quality Standard For Agents And Skills

Do not write generic advice such as "keep controllers thin" unless the file also shows:

- Wrong code.
- Correct code.
- The concrete bug caused by the wrong code.
- The exact preferred structure, naming, or response format.

Examples in `.agents` use `com.empresa.app` by convention. Real project code currently uses `com.example.company`; adapt examples before editing source files.

## Repository Workflow

- Specs live in `_specs/`.
- Plans live in `_plans/`.
- Shared project agent guidance lives in `.agents/agents/`.
- Reusable workflow prompts live in `.agents/commands/`.
- Codex project skills live in `.agents/skills/<skill-name>/SKILL.md`.
- Agent memory notes live in `.agents/agent-memory/`.

## Primary Agents

- Domain model, ports, events, invariants: `.agents/agents/hexagonal-domain-developer.md`.
- Use cases, transactions, orchestration: `.agents/agents/hexagonal-application-developer.md`.
- JPA/Flyway persistence adapters and N+1 fixes: `.agents/agents/hexagonal-persistence-adapter.md`.
- REST adapters and DTOs: `.agents/agents/hexagonal-web-adapter.md`.
- GraphQL adapters and MCP schema sync: `.agents/agents/hexagonal-graphql-adapter.md`.
- REST/GraphQL error response format: `.agents/agents/hexagonal-exception-handler.md`.
- Security/JWT/roles/CORS: `.agents/agents/hexagonal-security-reviewer.md`.
- Tests and ArchUnit checks: `.agents/agents/hexagonal-test-engineer.md`.
- Frontend: `.agents/agents/frontend-developer.md`.

## Primary Skills

- Complete backend feature: `$create-hexagonal-feature`.
- Debug hexagonal errors: `$debug-hexagonal-issue`.
- Enforce boundaries: `$enforce-hexagonal-boundaries`.
- Refactor layered code: `$refactor-to-hexagonal`.
- GraphQL/MCP workflow: `$graphql-mcp-workflow`.
- Review changes: `$review-hexagonal-changes`.

## Commands

- `.agents/commands/new-feature.md`: produce an exact hexagonal file plan.
- `.agents/commands/fix-issue.md`: diagnose bugs by hexagonal layer.
- `.agents/commands/code-review.md`: review current changes by severity.
- `.agents/commands/run-tests.md`: run tests in hexagonal order.
- `.agents/commands/sync-graphql-schema.md`: summarize GraphQL schema/resolver context for MCP.
- `.agents/commands/commit-message.md`: propose commit messages from staged diff.

## Git Rules

- Do not overwrite or revert user changes.
- Check `git status --short --branch` before branch, commit, or plan execution work.
- Use branch prefix `codex/feature/` for new feature branches unless the user asks for another prefix.
- Do not commit automatically. Show staged summary, propose a message, and ask for confirmation.
- Do not push unless the user explicitly requests it.
- Do not commit secrets. Keep `.env` local and prefer examples such as `.env.example` for documented variables.

## Backend Conventions

- Main package: `com.example.company`.
- Use feature-based hexagonal packages under `src/main/java/com/example/company/<module>/`.
- Put business invariants in domain models or value objects.
- Put input and output ports in `domain/port/in` and `domain/port/out`.
- Put transactions and orchestration in application use cases.
- Keep repositories inside outbound persistence adapters.
- Use request DTOs with Jakarta validation in inbound web adapters.
- Return response DTOs from REST/GraphQL adapters, never JPA entities.
- Use Flyway for schema changes and keep migrations append-only after they are shared.
- Do not modify existing migrations. Add a new migration only when schema changes are explicitly needed.
- Use `FetchType.LAZY` by default and load relationships intentionally with projections, fetch joins, or `@EntityGraph`.

## Verification

Use the smallest meaningful verification first:

- Backend compile/test classes: `.\gradlew.bat compileJava testClasses`
- Domain test example: `.\gradlew.bat test --tests "*CuttingQuantitiesTest"`
- Architecture test: `.\gradlew.bat test --tests "*HexagonalArchitectureTest"`
- Full backend tests when DB/config is ready: `.\gradlew.bat test`
- Git review: `git diff --staged`, `git diff`, `git status --short --branch`

If frontend work is added later, run the framework's lint/test/build commands and use the in-app Browser for visual validation on desktop and mobile.
