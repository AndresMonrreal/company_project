---
name: global-project-guide
description: Use for project-wide guidance before specs, plans, agents, skills, commands, or broad architecture changes in this Spring Boot hexagonal manufacturing traceability system.
---

# Global Project Guide

This repository is a Spring Boot backend for rubber profile cutting traceability.

Business flow:

```text
Reception -> Inventory -> Cutting -> Scrap -> Molding Output
```

Modules: `auth`, `profiles`, `machines`, `shifts`, `containers`, `reception`, `inventory`, `cutting`, `scrap`, `molding`, `reports`, plus support modules such as `users`, `roles`, `traceability`, and `exports`.

Examples in `.claude` use `com.empresa.app`; real source code uses `com.tesla.api`. Adapt examples before editing source files.

## Architecture

Use Hexagonal Architecture:

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

Domain must not import Spring, JPA, Jackson, GraphQL, servlet APIs, DTOs, or adapters.

## Core Business Rule

The cutting invariant is mandatory:

```java
if (initialQuantity != goodQuantity + scrapQuantity) {
    throw new IllegalArgumentException("initial_quantity must equal good_quantity + scrap_quantity");
}
```

The database also has a Flyway check constraint, but the rule must be enforced in domain first so invalid commands fail before persistence.

## Routing

- Domain model/invariants/ports/events: `hexagonal-domain-developer`.
- Use cases/transactions/orchestration: `hexagonal-application-developer`.
- JPA/Flyway/persistence mapping/N+1: `hexagonal-persistence-adapter`.
- REST controllers/DTOs/validation: `hexagonal-web-adapter`.
- GraphQL schema/resolvers/DataLoader/MCP: `hexagonal-graphql-adapter`.
- REST/GraphQL error mapping: `hexagonal-exception-handler`.
- Tests and ArchUnit checks: `hexagonal-test-engineer`.
- Security/auth/role checks: `hexagonal-security-reviewer`.

Do not modify existing Flyway migrations. If schema must change, add a new migration after explaining why.
