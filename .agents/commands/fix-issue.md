---
description: Diagnose a bug by hexagonal layer and propose or apply the smallest safe fix
argument-hint: <error-description-or-stack-trace>
---

# Fix Issue

Input: `$ARGUMENTS`

Use `$debug-hexagonal-issue`.

## Diagnosis Order

1. Entry adapter: REST or GraphQL mapping.
2. Input port: command/result shape.
3. Use case: transaction, orchestration, authorization.
4. Domain: invariant, aggregate, domain exception.
5. Output port: missing contract or wrong abstraction.
6. Persistence adapter: JPA mapping, N+1, Flyway mismatch.
7. Config/security: bean wiring, filters, CORS.

## Known Patterns

- Wrong dependency direction.
- LazyInitializationException.
- `@Transactional` self-invocation.
- ArchUnit failure.
- GraphQL N+1.
- `NoSuchBeanDefinitionException` for a port.
- Circular dependency between adapters.
- Flyway checksum or schema mismatch.
- Cutting quantity rule violation.

## Output

```text
Symptom:
Layer:
Root cause:
Boundary violation: yes/no
Fix:
Files:
Verification:
```

## Memory Update

After resolving the issue, append a new entry to `.agents/memory/issues-log.md` with the symptom, root cause, exact fix applied, and prevention rule.
