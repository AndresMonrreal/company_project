# tesla-web-app — Angular Frontend

## Stack

- Angular (latest stable — to be scaffolded with `ng new`)
- TypeScript
- Apollo Client for GraphQL queries to `tesla-api`
- Structure already created: `src/app/core/`, `features/`, `shared/`

## Architecture conventions

```
tesla-web-app/src/app/

  core/
    auth/         ← AuthService, AuthTokenStorage, AuthSession
    http/         ← authInterceptor, authErrorInterceptor, API_BASE_URL token
    guards/       ← authGuard, roleGuard
    layout/       ← app shell, nav, sidebar

  shared/
    ui/           ← reusable presentational components
    forms/        ← shared form controls
    utils/        ← pipes, helpers

  features/
    auth/         ← login page
    <feature>/    ← one folder per feature, lazy loaded
      <feature>.routes.ts
      models/
      data-access/   ← typed API client
      services/
      pages/
      components/
      forms/
```

## Rules

- Components NEVER call `HttpClient` directly — always through a typed API client
- Components NEVER access `localStorage`/`sessionStorage` directly — through `AuthTokenStorage`
- Token interceptor attaches `Authorization` only to `tesla-api` base URL — never external domains
- `401` clears session and redirects to `/login`
- `403` shows forbidden state without clearing a valid session
- Guards are UI hints only — backend enforces authorization
- Use standalone components unless NgModules are explicitly chosen
- All GraphQL operations in `*.graphql` files per feature

## Mandatory behavior for every frontend task

1. Read `.claude/memory/project-context.md` before starting
2. Use `$create-angular-feature` when creating any new feature module
3. Use `$create-angular-auth-flow` for any auth-related work
4. Use `$review-angular-changes` before any commit
5. Components never call HttpClient directly — always through typed API client
6. Components never access localStorage directly — always through AuthTokenStorage
7. Every new feature must include: API client, service, page, guard if needed, tests
8. Update `.claude/memory/project-context.md` after completing any feature

## Roles

| Role | Access |
|---|---|
| `ADMIN` | Catalog management, users, reports, export, full history |
| `SUPERVISOR` | Read catalogs, movements, reports, history, register/approve receptions |
| `OPERADOR` | Own operational movements and shift history only |
| `CONSULTA` | Read-only reports and history |

## Backend contract

- Login: `POST /api/auth/login` → returns `accessToken`, `tokenType`, `expiresAt`, user summary
- JWT in `Authorization: Bearer <token>` header for all protected requests
- Roles in JWT `role` claim: `ADMIN`, `SUPERVISOR`, `OPERADOR`, `CONSULTA`

## Commands

```bash
cd tesla-web-app
ng serve          # dev server
ng build          # production build
ng test           # unit tests
```