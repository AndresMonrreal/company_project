---
description: Synchronize GraphQL schema/resolver context and report MCP-impacting drift
argument-hint: [module]
---

# Sync GraphQL Schema

Use `$graphql-mcp-workflow`.

## Steps

1. Find schema and resolver files:

```powershell
rg --files src/main/resources src/main/java | rg "graphql|Graphql|GraphQL|\\.graphqls"
```

2. If no files exist, report:

```text
GraphQL status: not configured
Runtime dependency: not added
Next step: ask before adding spring-boot-starter-graphql
```

3. If files exist, verify:

- `.graphqls` query and mutation names.
- Resolver method names.
- Resolver calls input ports.
- Resolver does not call repositories.
- Resolver does not return JPA entities.
- DataLoader or `@BatchMapping` exists for nested list/object fields that can cause N+1.

4. Produce an MCP summary:

```text
type -> fields
query -> resolver -> input port
mutation -> resolver -> input port
known N+1 risks
schema drift warnings
```

## Output

```text
GraphQL status:
Schema files:
Queries:
Mutations:
Resolvers:
Input ports:
Violations:
MCP/context summary:
```

## Memory Update

After syncing, update the GraphQL section in `.agents/memory/project-context.md` with the current schema state and any resolver-to-port mapping changes.
