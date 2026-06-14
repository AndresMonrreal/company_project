---
name: explore-codebase
description: Explore this repository before planning or implementing a feature. Use to locate real Spring Boot hexagonal modules, domain models, input ports, output ports, use cases, web/GraphQL adapters, persistence adapters, Flyway migrations, tests, future frontend files, and project conventions.
---

# Explore Codebase

Use this skill when the implementation area is not obvious.

## Steps

1. Read `AGENTS.md` and `ARCHITECTURE.md`.
2. Search for the relevant module or feature name:
   - On Windows: `Get-ChildItem -Recurse -Filter "*.java" | Select-String "<module>"`
   - On Unix: `rg --files | rg "<module>"`
3. Read the smallest set of files that shows the pattern.
4. Map current flow:
   - Entry adapter: REST or GraphQL.
   - Request/response DTOs or GraphQL payloads.
   - Domain input port and command/result.
   - Application use case and transaction boundary.
   - Domain output port.
   - Persistence adapter, JPA entity, mapper, and Spring Data repository.
   - Domain model and invariants.
   - Flyway table/column names.
   - Tests.
5. Return exact files and a short recommendation.

## Rule

Do not invent paths. If the desired module does not exist, say what needs to be created.
