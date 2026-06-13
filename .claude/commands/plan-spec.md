---
description: Create a plan file from a spec file
argument-hint: Spec slug or filename (e.g. auth-jwt or auth-jwt.md)
allowed-tools: Read, Write, Glob, Bash(git log:*), Bash(ls:*), Agent
---

You are helping to turn a feature spec into a detailed, actionable implementation plan. Always read the root `CLAUDE.md` and relevant sub-package `CLAUDE.md` files before proceeding.

User input: $ARGUMENTS

## Step 1. Resolve the spec file

Extract the slug from `$ARGUMENTS` (strip `.md` if present). The spec is at `_specs/<slug>.md`.

If `$ARGUMENTS` is empty, list all files in `_specs/` and ask which to plan. Stop without a valid slug.

If `_specs/<slug>.md` does not exist, tell the user and stop.

## Step 2. Read project context

Read in parallel:
1. `_specs/<slug>.md`
2. Root `CLAUDE.md`
3. `tesla-api/CLAUDE.md` (if spec touches backend)
4. `tesla-web-app/CLAUDE.md` (if spec touches frontend)
5. `.claude/memory/project-context.md`
6. `.claude/memory/decisions-log.md`

Use `$explore-codebase` to scan relevant source directories so the plan references real file paths, not guesses.

## Step 3. Produce the plan

The plan must be:
- **Specific**: every step names exact files with paths, function signatures, and line numbers where relevant
- **Ordered**: steps in the exact sequence to implement
- **Phased**: group steps into named phases (e.g. Phase 1 — Domain, Phase 2 — Use Cases, Phase 3 — Persistence, Phase 4 — REST, Phase 5 — Tests)
- **Grounded**: verify all file paths exist before referencing them
- **Self-contained**: a developer reading only the plan should be able to implement without reading the spec

Required plan sections:

```markdown
# Plan: <Feature Title>

## Context
<1-3 sentences: why this feature exists, what problem it solves>

## Package base
`com.example.company.<module>`

## Phase N — <Phase Name>
### N.M <Step title>
**File:** `<path/to/file>`

<What to add/change and why. Include field names, method signatures, import statements.>

## Agent routing
<Which agent to use for each phase>

## Implementation order
<Numbered list of every step in exact sequence>

## Critical files
| File | Action |
|------|--------|

## Verification
<Numbered checklist of commands and manual checks>
```

Do not invent file paths. Do not include verbatim code blocks. Describe what to write, not the exact code.

## Step 4. Save the plan

Write to `_plans/<slug>.md`. Do not ask for approval — save directly.

## Step 5. Report

```text
Plan file: _plans/<slug>.md
Phases: <comma-separated phase names>
Steps: <total steps>
```

Do not repeat the full plan in chat.
