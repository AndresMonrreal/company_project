---
name: review-angular-changes
description: Review Angular frontend changes before commit, including frontend/ placement, component/service/API boundaries, token handling, interceptors, guards, role-aware UI, backend error handling, secrets, tests, and build commands.
---

# Review Angular Changes

Review staged and unstaged Angular changes before commit. Lead with findings by severity.

## Scope Checks

- Angular app lives under `frontend/`.
- Backend source files were not touched unless explicitly requested.
- No Angular files exist under backend `src/`.
- Backend was not moved into `backend/` unless a separate restructure task was approved.
- Flyway migrations were not modified for frontend work.

## Architecture Checks

- Components do not call `HttpClient` directly.
- Components do not access `localStorage` or `sessionStorage` directly.
- UI calls feature services or facades.
- Feature services call typed API clients.
- API clients own backend URLs and request/response models.
- Token handling is centralized under `core/auth`.
- Interceptor does not attach tokens to external URLs.
- Guards are used on protected routes.
- Tests live next to code under test using `.spec.ts`.

## Role And Security Checks

Role UI checks must match backend permissions:

- `ADMIN`: manage catalogs, users, reports, export, history.
- `SUPERVISOR`: read catalogs, daily movements, reports, history, register and approve receptions.
- `OPERADOR`: register operational movements, own shift history, no admin catalogs or general reports.
- `CONSULTA`: read-only reports and history.

Check that:

- Backend remains the authorization source of truth.
- Hidden buttons are not treated as security.
- `401` handling clears session or redirects intentionally.
- `403` handling shows a forbidden state intentionally.
- Secrets are not committed.
- API models do not include `password_hash`, raw passwords, JWT secrets, or unnecessary sensitive backend fields.
- Tokens and passwords are not logged.
- Backend error responses are handled consistently.

## Wrong: Component Calls API Directly

```ts
export class ContainersPage {
  private readonly http = inject(HttpClient);

  containers$ = this.http.get('/api/containers');
}
```

Bug: transport and endpoint knowledge leak into presentation code.

## Correct: Component Uses Service

```ts
export class ContainersPage {
  private readonly containers = inject(ContainersService);

  protected readonly containers$ = this.containers.listActive();
}
```

## Wrong: Token In Every Request

```ts
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  return next(request.clone({
    setHeaders: { Authorization: `Bearer ${localStorage.getItem('token')}` },
  }));
};
```

Bug: token can be sent to external domains and storage access is scattered.

## Correct: Central Storage And API Scope

```ts
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const apiBaseUrl = inject(API_BASE_URL);
  const token = inject(AuthTokenStorage).getAccessToken();

  if (!token || !request.url.startsWith(apiBaseUrl)) {
    return next(request);
  }

  return next(request.clone({
    setHeaders: { Authorization: `Bearer ${token}` },
  }));
};
```

## Wrong: UI Role Check Only

```html
@if (role === 'ADMIN') {
  <button>Delete role</button>
}
```

Bug: the backend endpoint still needs authorization because browser code can be changed.

## Correct: Guard Plus UI State Plus Backend Expectation

```ts
{
  path: 'roles',
  canActivate: [authGuard, roleGuard],
  data: { roles: ['ADMIN', 'SUPERVISOR'] },
  loadComponent: () => import('./roles-page').then((m) => m.RolesPage),
}
```

The page can hide write actions for non-admin users, and the backend must still reject unauthorized writes.

## Output Format

```text
Findings:
- BLOCKER: file:line - issue, impact, fix
- WARNING: file:line - issue, impact, fix
- SUGGESTION: file:line - issue, impact, fix

Verification:
- npm test: result or not run
- npm run build: result or not run

Residual risk:
- ...
```

If no issues are found, say that clearly and list any test or build commands not run.
