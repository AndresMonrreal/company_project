---
description: Create a detailed implementation plan from a spec file
argument-hint: Spec slug or filename
---

Create an actionable implementation plan from `_specs/<slug>.md`.

User input: `$ARGUMENTS`

## Step 1 - Resolve Spec

- Strip `.md` if present.
- Read `_specs/<slug>.md`.
- If no slug is provided, list `_specs/*.md` and ask which one to plan.
- If the file does not exist, stop.

## Step 2 - Read Context

Read:

- `AGENTS.md`
- `ARCHITECTURE.md`
- `build.gradle`
- Relevant files under `src/main/java`, `src/main/resources`, and `src/test`

Use `rg --files` and focused reads. Do not invent paths.

## Step 3 - Produce Plan

Save to `_plans/<slug>.md` using this structure:

```markdown
# Plan: <Feature Title>

## Context
<1-3 sentences>

---

## Phase N - <Phase Name>
### N.M <Step title>
**File:** `<path>` [(line N if useful)]
<What to change and why. Mention classes, methods, fields, endpoints, migrations, imports, and tests as needed.>

---

## Implementation order
1. <Every step in execution order>

---

## Critical files
| File | Action |
|------|--------|
| ... | ... |

---

## Verification
1. <Automated or manual check>
```

Rules:

- Be specific and ordered.
- Group by phases such as Database, Backend, Frontend, Tests, Verification.
- Do not add speculative steps.
- Do not include full code blocks.
- Do not plan work already implemented.

## Step 4 - Report

Respond only:

```text
Plan file: _plans/<slug>.md
Phases: <comma-separated phase names>
Steps: <total number>
```

