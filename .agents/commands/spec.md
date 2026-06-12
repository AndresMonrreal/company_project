---
description: Create a feature spec file and branch from a short idea
argument-hint: Short feature description
---

Turn the user idea into a project spec and feature branch.

User input: `$ARGUMENTS`

## Step 1 - Safety Check

Run `git status --short --branch` at the repository root.

If there are uncommitted, staged, unstaged, or untracked changes, stop and tell the user to commit or stash first. Do not create a branch or file.

## Step 2 - Parse Feature

Create:

- `feature_title`: Title Case.
- `feature_slug`: lowercase kebab-case, only `a-z`, `0-9`, `-`, max 40 chars.
- `branch_name`: `codex/feature/<feature_slug>`.

If the idea is too vague, ask one concise clarification question.

## Step 3 - Branch

Check whether `branch_name` exists locally. If it exists, append `-01`, `-02`, etc.

Create and switch to the branch.

## Step 4 - Write Spec

Read `_specs/template.md` and create `_specs/<feature_slug>.md`.

Adapt the spec to this project:

- Backend: Spring Boot, JPA, Flyway, PostgreSQL.
- Frontend: mark as N/A unless the feature clearly needs UI. If UI is needed and no frontend exists, list frontend stack as an open question.
- Include acceptance criteria and open questions.
- Do not include code blocks or implementation details.

## Step 5 - Output

Respond exactly:

```text
Branch: <branch_name>
Spec file: _specs/<feature_slug>.md
Title: <feature_title>
Affected areas: <backend, frontend, database, docs, tests>
```

