---
name: frontend-developer
description: Use for all Angular work in tesla-web-app/ — standalone components, pages, services, typed API clients, HTTP interceptors, route guards, reactive forms, and role-aware UI. Follow $create-angular-feature and $create-angular-auth-flow skills.
---

# Angular Frontend Developer

This repository currently has no frontend application. When frontend work starts, use Angular + TypeScript and create the app under `tesla-web-app/`.

Do not put Angular files under backend `src/`. Do not move the Spring Boot backend into `backend/` unless a separate restructure task is approved.

## Project Context

The frontend supports the manufacturing traceability flow:

```text
Reception -> Inventory -> Cutting -> Scrap -> Molding Output
```

Backend integration starts with:

- `POST /api/auth/login`
- JWT access token from login
- `Authorization: Bearer <token>` for protected backend API calls
- Roles: `ADMIN`, `SUPERVISOR`, `OPERADOR`, `CONSULTA`

Backend remains the authorization source of truth. Frontend role checks improve usability but are not security boundaries.

## Angular Architecture

Use Angular's feature-based organization and standalone components unless the project intentionally chooses NgModules later.

Expected root:

```text
tesla-web-app/
  angular.json
  package.json
  src/
    app/
      core/
      shared/
      features/
```

Recommended structure:

```text
tesla-web-app/src/app/core/auth/
tesla-web-app/src/app/core/http/
tesla-web-app/src/app/core/guards/
tesla-web-app/src/app/core/layout/
tesla-web-app/src/app/shared/ui/
tesla-web-app/src/app/shared/forms/
tesla-web-app/src/app/shared/utils/
tesla-web-app/src/app/features/auth/
tesla-web-app/src/app/features/catalogs/
```

Use:

- Angular Router for navigation.
- `provideHttpClient` and typed `HttpClient` API clients.
- Functional HTTP interceptors where appropriate.
- Functional route guards where appropriate.
- Reactive Forms for business forms.
- `.spec.ts` tests next to the code under test.

## Rules

- No direct `HttpClient` calls from components.
- Components call feature services or facades.
- API clients stay typed with request/response models.
- Token handling must be centralized in `core/auth`.
- Components must not access `localStorage` or `sessionStorage` directly.
- HTTP interceptor must add `Authorization` only for backend API URLs.
- Do not expose `password_hash`, raw passwords, JWT secrets, or sensitive backend fields.
- A `401` should clear session and redirect to login when appropriate.
- A `403` should show a forbidden state.
- Hide disabled actions by role, but never rely only on UI security.
- Prefer feature-based folders over generic type folders.
- Keep Angular architecture separate from backend hexagonal package names.

## Wrong: Component Calls HttpClient Directly

```ts
import { Component, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-machines-page',
  templateUrl: './machines-page.html',
})
export class MachinesPage {
  private readonly http = inject(HttpClient);

  machines$ = this.http.get<Machine[]>('/api/machines');
}
```

Bug: the component owns backend URL knowledge, transport errors, and API typing details. This spreads integration logic across the UI.

## Correct: Component Uses Feature Service

```ts
import { Component, inject } from '@angular/core';
import { MachinesService } from './machines.service';

@Component({
  selector: 'app-machines-page',
  templateUrl: './machines-page.html',
})
export class MachinesPage {
  private readonly machines = inject(MachinesService);

  protected readonly machines$ = this.machines.listActive();
}
```

```ts
import { Injectable, inject } from '@angular/core';
import { MachinesApiClient } from './machines-api.client';

@Injectable({ providedIn: 'root' })
export class MachinesService {
  private readonly api = inject(MachinesApiClient);

  listActive() {
    return this.api.listActive();
  }
}
```

## Wrong: Interceptor Sends Token Everywhere

```ts
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const token = localStorage.getItem('accessToken');
  return next(request.clone({
    setHeaders: { Authorization: `Bearer ${token}` },
  }));
};
```

Bug: this can leak the token to external domains and couples token storage to the interceptor implementation.

## Correct: Interceptor Is Scoped To Backend API

```ts
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const tokenStorage = inject(AuthTokenStorage);
  const apiBaseUrl = inject(API_BASE_URL);

  if (!request.url.startsWith(apiBaseUrl)) {
    return next(request);
  }

  const token = tokenStorage.getAccessToken();
  if (!token) {
    return next(request);
  }

  return next(request.clone({
    setHeaders: { Authorization: `Bearer ${token}` },
  }));
};
```

## Wrong: Role Check Only Hides A Button

```html
@if (currentUser.role === 'ADMIN') {
  <button (click)="deleteMachine(machine.id)">Delete</button>
}
```

Bug: a user can still call the backend directly. Hidden buttons are not authorization.

## Correct: UI Permission Plus Guard Plus Backend Enforcement

```ts
export const adminGuard: CanActivateFn = () => {
  const session = inject(AuthSession);
  const router = inject(Router);
  return session.hasRole('ADMIN') ? true : router.createUrlTree(['/forbidden']);
};
```

```ts
export const catalogRoutes: Routes = [
  {
    path: 'machines',
    canActivate: [adminOrSupervisorGuard],
    loadComponent: () => import('./machines/machines-page')
      .then((m) => m.MachinesPage),
  },
];
```

The backend must still enforce the role rule for the endpoint.

## Role-Aware UI

- `ADMIN`: catalog management, users, reports, export, full history.
- `SUPERVISOR`: read catalogs, daily movements, reports, history, register/approve receptions.
- `OPERADOR`: operational movements and own shift history; no admin catalogs or general reports.
- `CONSULTA`: read-only reports and history.

## Verification

When a frontend exists, run Angular commands from `tesla-web-app/`:

```powershell
npm test
npm run build
```

Use `$review-angular-changes` before frontend commits.
