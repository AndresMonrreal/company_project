---
name: review-hexagonal-changes
description: Review current changes in this Spring Boot hexagonal manufacturing backend before commit or merge. Use to catch domain purity violations, wrong port direction, adapter bypasses, missing ArchUnit coverage, exposed JPA entities, migration mistakes, GraphQL schema drift, security gaps, and missing tests.
---

# Review Hexagonal Changes

Lead with findings by severity.

## Severity

- `BLOCKER`: boundary violation, migration history edit, invalid domain invariant, security bypass.
- `WARNING`: missing tests, weak error mapping, missing GraphQL schema update.
- `SUGGESTION`: naming, organization, readability.

## Checklist

Domain:

- No Spring/JPA/GraphQL/DTO imports.
- Invariants live in domain.
- Cutting uses `initial_quantity = good_quantity + scrap_quantity`.

Application:

- One use case class per use case.
- `@Transactional` on use case methods/classes.
- Depends on domain ports, not adapters.

Persistence:

- Adapter implements output port.
- JPA entity separate from domain.
- Existing migrations not edited.

Web:

- Controllers depend on input ports.
- DTOs live in adapter.
- Response does not expose JPA entity.

GraphQL:

- Schema updated when payload changes.
- Resolvers thin.
- Batch loading used for nested collections.

Tests:

- Domain tests pure JUnit.
- Use cases mock output ports.
- Adapter tests use slices.
- ArchUnit exists for boundaries.

## Finding Example

```text
BLOCKER: src/main/java/com/empresa/app/cutting/domain/model/CuttingRecord.java:4 imports jakarta.persistence.Entity.
Impact: domain depends on JPA and violates hexagonal independence.
Fix: move persistence annotations to CuttingRecordJpaEntity under adapter/out/persistence.
```

## Output Format

```text
Findings:
- BLOCKER: file:line - issue, impact, fix

Open questions:
- ...

Verification:
- command and result
```

If no issues exist, say so clearly and list residual risk.
