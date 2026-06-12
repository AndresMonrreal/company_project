---
description: Analyze staged git changes and propose a clean commit message
argument-hint: [MESSAGE=""] [PUSH=true|false]
---

Review the current git repository from the current working directory.

Run:
- `git status`
- `git diff --staged`
- `git diff --staged --stat`

Analyze only staged changes.

## Summary Rules

Group staged changes by:

1. Type of change: new, modified, deleted, renamed.
2. Package or directory: root config, `src/main/java`, `src/main/resources`, `src/test`, `.agents`, `_specs`, `_plans`, frontend directory if present.

If `.env`, secrets, credentials, private keys, or production URLs are staged, call that out before proposing the commit message.

## Commit Types

Use only:

- `✨ feat:`
- `🐛 fix:`
- `🔨 refactor:`
- `📝 docs:`
- `🎨 style:`
- `✅ test:`
- `⚡ perf:`
- `🗑️ chore:`
- `🔒 security:`
- `🚀 ci:`
- `♻️ revert:`

## Message Format

```text
<emoji> <type>: <concise_description>
<optional_body_explaining_why>
```

Use present tense. Include the body only when the reason is not obvious from the summary.

## Output Order

1. Summary of staged changes.
2. Secret/config warning if warranted.
3. Proposed commit message.
4. Ask for confirmation before running `git commit`.

Do not auto-commit. If `MESSAGE` is provided, use it unless it conflicts with the diff. Do not push unless `PUSH=true` and the commit succeeds.

