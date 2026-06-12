---
description: Design the exact file plan for a new hexagonal manufacturing feature
argument-hint: <feature-name-or-module>
---

# New Feature

Input: `$ARGUMENTS`

## Steps

1. Run `git status --short --branch`.
2. Read `AGENTS.md`, `ARCHITECTURE.md`, and `src/main/resources/db/migration/V1__create_initial_schema.sql`.
3. Identify module and aggregate name.
4. Produce exact paths, class names, port names, use case names, DTO names, migration filename if needed, and tests.
5. Do not write code unless the user confirms implementation.

## Output Format

```text
Feature:
Module:
Aggregate:

Domain model:
- src/main/java/com/example/company/<module>/domain/model/<Aggregate>.java
- src/main/java/com/example/company/<module>/domain/event/<Event>.java
- src/main/java/com/example/company/<module>/domain/exception/<Exception>.java

Ports:
- src/main/java/com/example/company/<module>/domain/port/in/<UseCase>.java
- src/main/java/com/example/company/<module>/domain/port/out/<OutputPort>.java

Use cases:
- src/main/java/com/example/company/<module>/application/usecase/<UseCaseService>.java

Adapters:
- src/main/java/com/example/company/<module>/adapter/in/web/<RestController>.java
- src/main/java/com/example/company/<module>/adapter/in/web/dto/<Request>.java
- src/main/java/com/example/company/<module>/adapter/in/web/dto/<Response>.java
- src/main/java/com/example/company/<module>/adapter/out/persistence/<JpaEntity>.java
- src/main/java/com/example/company/<module>/adapter/out/persistence/<PersistenceAdapter>.java

GraphQL:
- src/main/resources/graphql/<module>.graphqls
- src/main/java/com/example/company/<module>/adapter/in/web/graphql/<GraphqlController>.java

Migration:
- none, existing schema covers this
  or
- src/main/resources/db/migration/V<N>__<description>.sql

Tests:
- domain test
- use case test
- persistence adapter test
- web adapter test
- architecture test if boundaries change

Verification:
- .\gradlew.bat compileJava testClasses
```

## Rule

If the feature touches cutting, include `CuttingQuantities` and enforce `initial_quantity = good_quantity + scrap_quantity` in domain.

## Memory Update

After completing the feature, append a new entry to `.agents/memory/features-log.md` with all files created, files modified, ports added, tests written, and any unusual notes.
