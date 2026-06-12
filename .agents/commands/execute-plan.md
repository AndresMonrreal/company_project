---
description: Execute one phase of a saved plan using strict hexagonal routing
argument-hint: <plan-file> [phase]
---

# Execute Plan

## Routing

- Feature creation: `$create-hexagonal-feature`.
- Debugging: `$debug-hexagonal-issue`.
- Boundary enforcement: `$enforce-hexagonal-boundaries`.
- Layered migration: `$refactor-to-hexagonal`.
- GraphQL/MCP: `$graphql-mcp-workflow`.
- Review: `$review-hexagonal-changes`.

## Rules

1. Run `git status --short --branch`.
2. Read the requested plan phase only.
3. Implement in this order: project code, agents, skills, commands, settings/AGENTS.
4. Never edit existing Flyway migrations.
5. Verify with the smallest meaningful tests.
6. Report changed files and remaining risks.
