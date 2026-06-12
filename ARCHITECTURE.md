# Hexagonal Spring Boot Architecture

This project uses Hexagonal Architecture / Ports and Adapters for a manufacturing traceability backend.

## Business Flow

```text
Reception -> Inventory -> Cutting -> Scrap -> Molding Output
```

Profiles 36, 37, 38, and 39 all follow this flow.

The critical cutting invariant is:

```text
initial_quantity = good_quantity + scrap_quantity
```

This rule exists in the database constraint and must also exist in domain code.

## Module Structure

Each business module should follow this shape:

```text
<module>/
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

## Layer Responsibilities

- `domain/model`: pure Java aggregates, entities, and value objects.
- `domain/event`: pure Java domain events.
- `domain/exception`: business failures such as duplicate profile code or invalid cutting quantities.
- `domain/port/in`: use case interfaces, commands, and results.
- `domain/port/out`: persistence or external-system contracts needed by use cases.
- `domain/service`: pure business policies involving multiple domain objects.
- `application/usecase`: orchestration, transaction boundaries, authorization checks when needed, and calls to output ports.
- `application/mapper`: mapping between domain objects and use case result DTOs.
- `adapter/in/web`: REST controllers, request DTOs, response DTOs, validation, and web mappers.
- `adapter/in/web/graphql`: GraphQL resolvers, payloads, and schema mapping when GraphQL is enabled.
- `adapter/out/persistence`: JPA entities, Spring Data repositories, persistence mappers, and persistence adapters.
- `adapter/out/external`: third-party system adapters.

## Current Implemented Pilot

`profiles` is the first migrated module:

```text
profiles/
  domain/model/Profile.java
  domain/exception/
  domain/port/in/
  domain/port/out/ProfileRepositoryPort.java
  application/usecase/
  application/mapper/ProfileResultMapper.java
  adapter/in/web/
  adapter/out/persistence/
```

`cutting/domain/model/CuttingQuantities.java` contains the core quantity invariant so future cutting work starts from domain behavior.

## Architecture Enforcement

ArchUnit rules live in:

```text
src/test/java/com/example/company/architecture/HexagonalArchitectureTest.java
```

They enforce:

- domain does not depend on Spring, JPA, servlet APIs, or adapters.
- application does not depend on adapters.
- inbound adapters do not depend on outbound adapters.

## Flyway Rules

- Existing migrations are append-only history.
- Do not edit `V1__create_initial_schema.sql`.
- If schema must change, create a new migration such as `V2__add_<feature>.sql`.
- Java persistence adapters must adapt to the existing database schema, not the other way around.

## GraphQL And MCP

GraphQL is not enabled at runtime yet. When enabled:

- add `spring-boot-starter-graphql`.
- put schema files under `src/main/resources/graphql/`.
- put resolvers under `adapter/in/web/graphql/`.
- resolvers call domain input ports, never repositories.
- `sync-graphql-schema` summarizes schema, resolvers, and input ports for MCP/context use.

## Frontend

No frontend exists yet. Detect the actual framework before editing. If none exists, ask before creating one. The default recommendation for fast manufacturing admin screens is React + Vite + TypeScript unless Angular is explicitly chosen.
