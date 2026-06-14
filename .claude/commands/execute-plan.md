---
description: Execute a feature plan from the _plans/ directory using all available agents and skills.
argument-hint: Plan slug or filename (e.g. initialize-angular-frontend-with-shell)
allowed-tools: Read, Write, Edit, Glob, Grep, Bash, Agent, Skill, TaskCreate, TaskUpdate, TaskGet, TaskList
---

You are helping to fully implement a feature by executing an existing plan file. Always adhere to any rules and conventions set out in any CLAUDE.md files.

User input: $ARGUMENTS

## Step 1. Resolve the plan file

Strip any `.md` extension from `$ARGUMENTS`. The plan file is at `_plans/<slug>.md`.

If `$ARGUMENTS` is empty, list all files in `_plans/` and ask the user which plan to execute.

If `_plans/<slug>.md` does not exist, tell the user and stop.

## Step 2. Read all necessary context in parallel

1. `_plans/<slug>.md`
2. `_specs/<slug>.md` (if exists)
3. `CLAUDE.md`
4. `tesla-api/CLAUDE.md` (if plan touches backend)
5. `tesla-web-app/CLAUDE.md` (if plan touches frontend)
6. `.claude/memory/project-context.md`
7. `.impeccable/PRODUCT.md` (if exists)
8. `.impeccable/DESIGN.md` (if exists)

Do not start implementation until all files are read and understood.

## Step 3. Create a task list

Break the plan into tasks matching its phases and steps. Use TaskCreate to track them. Mark each task `in_progress` when starting and `completed` when done so the user can follow progress.

## Step 4. Execute each phase in order

Work through phases sequentially. Before dispatching any agent for a phase, invoke:

**Skill: workflow-execute-phase** — passing the current phase text and slug.

Within a phase, steps that modify independent files can run in parallel. Steps with dependencies must remain sequential.

### Routing rules

| Work type | Tool |
|---|---|
| New Angular component, page, service, model | `Agent: frontend-developer` + `$create-angular-feature` |
| New Angular auth, interceptor, guard, token storage | `Agent: frontend-developer` + `$create-angular-auth-flow` |
| New hexagonal domain model, port, exception | `Agent: hexagonal-domain-developer` |
| New use case, application service | `Agent: hexagonal-application-developer` |
| New JPA entity, persistence adapter, migration | `Agent: hexagonal-persistence-adapter` |
| New REST controller, DTO, web mapper | `Agent: hexagonal-web-adapter` |
| New GraphQL resolver, schema | `Agent: hexagonal-graphql-adapter` |
| Tests | `Agent: hexagonal-test-engineer` |
| Small targeted edit to already-read file | Direct Edit |

### Agent brief requirements

Every agent call must include:
- Exact step text from the plan
- File paths to create or modify
- Relevant CLAUDE.md conventions
- Current file content if modifying
- Full content of `.impeccable/PRODUCT.md` and `.impeccable/DESIGN.md` for frontend work
- Resolved dependencies from previous steps

Never launch an agent without a full brief.

### Security review

After any step touching auth, JWT, interceptors, guards, or database queries:
- Frontend: run `$review-angular-changes` on modified files
- Backend: launch `Agent: hexagonal-security-reviewer` on modified files

## Step 5. Visual validation (frontend only)

After all frontend phases complete:
1. Tell user to run `cd tesla-web-app && ng serve` if not already running
2. Use Playwright MCP to navigate every added or modified page
3. Capture screenshots at 1280×800 desktop and 390×844 mobile
4. Fix any layout breaks or visual regressions found
5. Re-validate after fixes

## Step 6. Verification checklist

Run every item in the plan's Verification section:
- Automated checks: run via Bash and report pass/fail
- Manual checks: use Playwright to confirm behavior

Fix failures before marking complete.

## Step 7. Update memory

After all phases complete:
- Update `.claude/memory/project-context.md` with new module/feature state
- Append to `.claude/memory/features-log.md`
- Append to `.claude/memory/decisions-log.md` if an architectural decision was made

## Step 8. Final report

```
Plan executed: _plans/<slug>.md
Phases completed: <list>
Files created: <count>
Files modified: <count>
Visual validation: <pass/fail or N/A>
```