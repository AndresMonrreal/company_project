---
name: workflow-execute-phase
description: Execute one phase from a _plans/*.md file efficiently. Pre-loads context once, classifies steps by agent/skill, identifies parallel vs sequential work, and dispatches each agent with a full brief. Invoked by /execute-plan before each phase — do not call directly.
---

# Workflow Execute Phase

## Purpose

Execute one plan phase at a time — pre-load context once, classify steps, identify parallelizable work, dispatch agents with full briefs. Never launch agents cold.

---

## Step A — Pre-load context ONCE before dispatching

Read in parallel — current phase only:

- `_plans/<slug>.md` (current phase section only — not the full file)
- `tesla-web-app/CLAUDE.md` (if phase touches frontend)
- `tesla-api/CLAUDE.md` (if phase touches backend)
- `.impeccable/PRODUCT.md` (if phase touches frontend and file exists)
- `.impeccable/DESIGN.md` (if phase touches frontend and file exists)
- `.claude/memory/project-context.md`
- Every file the plan marks as "Modify" in this phase

Do not read files from phases not yet executing.

---

## Step B — Classify each step by tool

| Work type | Tool |
|---|---|
| New Angular component, page, service, model | `Agent: frontend-developer` + `$create-angular-feature` |
| New Angular auth, interceptor, guard, token storage | `Agent: frontend-developer` + `$create-angular-auth-flow` |
| Review Angular changes before commit | `$review-angular-changes` |
| New hexagonal domain model, port, exception | `Agent: hexagonal-domain-developer` |
| New use case, application service | `Agent: hexagonal-application-developer` |
| New JPA entity, persistence adapter, migration | `Agent: hexagonal-persistence-adapter` |
| New REST controller, DTO, web mapper | `Agent: hexagonal-web-adapter` |
| New GraphQL resolver, schema | `Agent: hexagonal-graphql-adapter` |
| Tests | `Agent: hexagonal-test-engineer` |
| Backend security audit | `Agent: hexagonal-security-reviewer` |
| Frontend security audit | `$review-angular-changes` |
| Small targeted edit to already-read file | Direct Edit |

---

## Step C — Identify parallel vs sequential

**Parallel** when steps touch different files with no dependency between them.

**Sequential** when the second step imports or extends something the first creates.

Sequential examples for this project:
- `auth.models.ts` → must exist before `AuthTokenStorage`
- `AuthTokenStorage` → must exist before `AuthSession`
- `AuthSession` → must exist before `AuthService`
- `SidebarComponent` + `TopBarComponent` → can run in parallel
- `AppShellComponent` → must wait for both sidebar and top bar
- Domain model → must exist before use case
- Use case → must exist before REST controller

When in doubt — sequential.

---

## Step D — Full brief for every agent

Every agent call must include:

```
## Your task
<exact step text copied from the plan — do not summarize>

## Files to create or modify
<exact paths from the plan>

## Project conventions
<relevant section from tesla-web-app/CLAUDE.md or tesla-api/CLAUDE.md read in Step A>

## Current file content (if modifying)
<content already read in Step A — do not re-read>

## Design brief (frontend steps only)
<full content of .impeccable/PRODUCT.md>
<full content of .impeccable/DESIGN.md>
These design tokens override any generic Tailwind defaults in the plan.

## Resolved dependencies
<full content of files created in previous steps that this agent needs>

## Instruction
Implement directly. You have full context — do not explore, investigate, or ask clarifying questions.
```

Never launch an agent without a full brief. An agent without context uses 3-5x more tokens exploring.

---

## Step E — Security review after auth/query steps

After any step touching auth, JWT, interceptors, guards, token storage, or database queries:

- Frontend → run `$review-angular-changes` on modified files
- Backend → launch `Agent: hexagonal-security-reviewer` on modified files

Do this before continuing to the next step.

---

## Step F — Post-phase verification

Before marking a phase completed:

- Frontend phase: run `ng build` inside `tesla-web-app/` — confirm no new errors
- Backend phase: run `.\gradlew.bat compileJava` inside `tesla-api/` — confirm it passes
- Use `code-review-graph` MCP tools to check impact radius before marking done

---

## Golden rules

1. One read per file per phase — pre-load in Step A, never re-read the same file
2. Full brief or don't launch the agent — no context = wasted tokens
3. Parallel only when no dependency exists — when in doubt, sequential
4. Security review is mandatory after auth/interceptor/query changes
5. Use `code-review-graph` before any Grep/Glob/Read
6. `.impeccable/DESIGN.md` tokens override Tailwind defaults in every frontend component
