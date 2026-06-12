---
description: Review current changes against hexagonal architecture, manufacturing invariants, security, GraphQL drift, and tests
argument-hint: [--staged]
---

# Code Review

Use `$review-hexagonal-changes`.

## Commands To Inspect

- `git status --short --branch`
- `git diff --staged`
- `git diff`

## Always Check

- Domain purity.
- Port direction.
- Adapter isolation.
- Cutting invariant.
- JPA entity separation.
- Existing migrations not edited.
- GraphQL schema/resolver drift.
- Security identity/role handling.
- Domain/use case/adapter tests.
- ArchUnit coverage.

## Severity

- `BLOCKER`: boundary violation, migration history edit, security bypass, missing domain invariant.
- `WARNING`: missing tests, missing schema update, weak exception mapping.
- `SUGGESTION`: naming, duplication, organization.

## Output

```text
Findings:
- BLOCKER: file:line - issue, impact, fix
- WARNING: file:line - issue, impact, fix
- SUGGESTION: file:line - issue, impact, fix

Open questions:
- ...

Verification:
- ...
```

## Memory Update

If a `BLOCKER` is found and fixed, append the fix pattern to `.agents/memory/issues-log.md`.

If an architectural decision was made during review, append it to `.agents/memory/decisions-log.md`.
