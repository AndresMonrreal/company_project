---
name: graphql-mcp-workflow
description: Use when adding, reviewing, or synchronizing GraphQL schema and MCP context for this hexagonal Spring Boot backend. Applies to schema-first GraphQL files, resolvers, input-port wiring, schema summaries, MCP query examples, and reducing unnecessary repository file reads.
---

# GraphQL MCP Workflow

GraphQL is an inbound web adapter. MCP is a context layer that should summarize schema and resolver wiring before reading implementation files.

## Daily Workflow

1. Run `rg --files src/main/resources src/main/java | rg "graphql|Graphql|GraphQL|\\.graphqls"`.
2. If no schema exists, report `GraphQL status: not configured`.
3. If schema exists, list query/mutation/type names.
4. Map each resolver method to an input port.
5. Flag resolvers that call repositories or return JPA entities.
6. Refresh the MCP/context summary used by Codex.

## Schema Example

```graphql
type Query {
  cuttingRecord(id: ID!): CuttingRecord
}

type Mutation {
  createCutting(input: CreateCuttingInput!): CuttingRecord!
}

input CreateCuttingInput {
  inventoryItemId: ID!
  machineId: ID!
  shiftId: ID!
  initialQuantity: Int!
  goodQuantity: Int!
  scrapQuantity: Int!
}

type CuttingRecord {
  id: ID!
  initialQuantity: Int!
  goodQuantity: Int!
  scrapQuantity: Int!
}
```

## Resolver Rule

Wrong:

```java
@MutationMapping
CuttingRecordJpaEntity createCutting(@Argument CreateCuttingInput input) {
    return repository.save(mapper.toEntity(input));
}
```

Correct:

```java
@MutationMapping
CuttingPayload createCutting(@Argument CreateCuttingInput input) {
    return mapper.toPayload(createCutting.create(mapper.toCommand(input)));
}
```

## MCP Query Examples

Use MCP/schema summary to answer:

```text
Which mutations create manufacturing traceability records?
Which resolver calls CreateCuttingUseCase?
Which GraphQL types expose cutting quantities?
Which schema fields would break if CuttingResult changes?
```

This reduces file reads by starting from schema and resolver metadata, then opening only the relevant port/use case/adapter files.

## Output

```text
GraphQL status:
Schema files:
Queries:
Mutations:
Resolvers:
Input ports:
Boundary violations:
MCP summary:
```
