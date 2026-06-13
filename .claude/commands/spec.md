---
description: Create a feature spec file and branch from a short idea
argument-hint: Short feature description
allowed-tools: Read, Write, Glob, Bash(git switch:*), Bash(git status:*), Bash(git -C *:*)
---

You are helping to spin up a new feature spec for this project. Always read the root `CLAUDE.md` before proceeding.

User input: $ARGUMENTS

## Step 1. Check git status

Check the current git branch of the monorepo root. Abort if there are any uncommitted, unstaged, or untracked files. Tell the user to commit or stash first. DO NOT CONTINUE.

## Step 2. Parse arguments

Extract from `$ARGUMENTS`:

1. `feature_title` — short Title Case human-readable title
2. `feature_slug` — lowercase, kebab-case, only `a-z 0-9 -`, max 40 chars
3. `branch_name` — format: `claude/feature/<feature_slug>`

If you cannot infer a sensible title and slug, ask the user to clarify.

## Step 3. Identify affected packages

| Directory | When affected |
|---|---|
| `tesla-api/` | New or changed API endpoints, domain models, use cases, migrations, business logic |
| `tesla-web-app/` | New or changed Angular pages, components, services, API client, auth flows |

For each affected package: check its git status. If uncommitted changes exist, abort and tell the user. DO NOT CONTINUE.

## Step 4. Switch to new git branches

Switch to `branch_name` in the monorepo root and every affected package. If the branch already exists, append `-01`, `-02`, etc. Use the same final branch name across all repos.

## Step 5. Draft the spec

Create a markdown spec under `_specs/<feature_slug>.md`. Use the structure from `_specs/template.md`. Do not include implementation code.

The spec must cover:
- Purpose and problem being solved
- Affected packages
- Business rules and edge cases
- API contract (if backend is involved)
- Angular pages/flows (if frontend is involved)
- Acceptance criteria
- Out of scope

## Step 6. Report

```text
Branch: <branch_name>
Spec file: _specs/<feature_slug>.md
Title: <feature_title>
Affected packages: <comma-separated list>
```

Do not repeat the full spec in chat.