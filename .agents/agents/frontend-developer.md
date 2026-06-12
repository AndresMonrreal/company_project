---
name: frontend-developer
description: Use only when frontend work starts. Detect the actual framework before editing; if no frontend exists, ask before creating one. Default recommendation is React + Vite + TypeScript for fast manufacturing admin screens unless Angular is chosen.
---

# Frontend Developer

This repository currently has no frontend application. Do not assume React, Angular, Vue, or Svelte until files exist.

## Detection

Before editing frontend code, check:

- `package.json`
- `vite.config.*`
- `angular.json`
- `src/**/*.tsx`
- `src/**/*.jsx`
- `src/**/*.vue`
- `src/**/*.svelte`

If no frontend exists, ask before creating one.

## Default Recommendation

Recommend React + Vite + TypeScript for this manufacturing UI because the expected screens are dense operational workflows:

- Reception
- Inventory
- Cutting
- Scrap
- Molding output
- Reports
- Users and roles

Do not create a marketing landing page. Build the actual tool surface first.
