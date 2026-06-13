---
name: create-angular-feature
description: Create Angular feature pages, catalog screens, forms, services, typed API clients, routes, guards, and tests under tesla-web-app/ for this manufacturing traceability system.
---

# Create Angular Feature

Create Angular + TypeScript feature code under `tesla-web-app/`. Do not create or edit backend source unless the user explicitly asks.

Use standalone components unless the project intentionally chooses NgModules later.

## Required Flow

```text
UI component -> feature service/facade -> typed API client -> backend
```

Do not use:

```text
component -> HttpClient directly
component -> localStorage directly
component -> hardcoded backend URL scattered in templates/services
```

## Expected Feature Structure

For `tesla-web-app/src/app/features/<feature>/`:

```text
<feature>.routes.ts
models/
  <feature>.models.ts
data-access/
  <feature>-api.client.ts
  <feature>-api.client.spec.ts
services/
  <feature>.service.ts
  <feature>.service.spec.ts
pages/
  <feature>-page/
    <feature>-page.ts
    <feature>-page.html
    <feature>-page.css
    <feature>-page.spec.ts
components/
  <specific-widget>/
forms/
  <feature>-form/
```

Use smaller folders only when the feature grows enough to justify them.

## Feature Route Pattern

```ts
import { Routes } from '@angular/router';
import { adminOrSupervisorGuard } from '../../core/guards/role.guard';

export const machinesRoutes: Routes = [
  {
    path: '',
    canActivate: [adminOrSupervisorGuard],
    loadComponent: () => import('./pages/machines-page/machines-page')
      .then((m) => m.MachinesPage),
  },
];
```

Use route guards for protected pages. Backend endpoint authorization remains the source of truth.

## Typed API Client

```ts
export interface MachineResponse {
  id: number;
  name: string;
  active: boolean;
}

export interface CreateMachineRequest {
  name: string;
}
```

```ts
@Injectable({ providedIn: 'root' })
export class MachinesApiClient {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  listActive() {
    return this.http.get<MachineResponse[]>(`${this.apiBaseUrl}/api/machines`);
  }

  create(request: CreateMachineRequest) {
    return this.http.post<MachineResponse>(`${this.apiBaseUrl}/api/machines`, request);
  }
}
```

Keep backend URLs in API clients and shared config, not in components.

## Reactive Form Pattern

```ts
protected readonly form = new FormGroup({
  name: new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.maxLength(100)],
  }),
});
```

Submit through the feature service. Disable submit while invalid or loading.

## Loading, Empty, And Error States

Each page that loads backend data should represent:

- Loading state.
- Empty state.
- Backend validation or domain errors.
- Forbidden state when applicable.
- Retry or refresh action when useful.

Map backend errors in a shared helper before displaying them. Do not show stack traces, JWTs, password fields, SQL details, or raw backend exception text.

## Role-Aware Actions

Hide or disable actions the current role cannot use, but always expect the backend to enforce the same rule.

Catalog management examples:

- `ADMIN`: create, update, soft delete, read.
- `SUPERVISOR`: read only.
- `OPERADOR`: no admin catalog screens.
- `CONSULTA`: no admin catalog screens.

## Wrong: Component Calls HttpClient

```ts
export class ProfilesPage {
  private readonly http = inject(HttpClient);

  save() {
    this.http.post('/api/profiles', this.form.value).subscribe();
  }
}
```

Bug: the component mixes UI, API URL, transport, and persistence concerns.

## Correct: Component Calls Feature Service

```ts
export class ProfilesPage {
  private readonly profiles = inject(ProfilesService);

  save() {
    if (this.form.invalid) {
      return;
    }

    this.profiles.create(this.form.getRawValue()).subscribe();
  }
}
```

```ts
@Injectable({ providedIn: 'root' })
export class ProfilesService {
  private readonly api = inject(ProfilesApiClient);

  create(request: CreateProfileRequest) {
    return this.api.create(request);
  }
}
```

## Wrong: Component Reads localStorage

```ts
export class CatalogActions {
  protected readonly role = localStorage.getItem('role');
}
```

Bug: storage format leaks into UI code and becomes hard to change or test.

## Correct: Component Uses Session Service

```ts
export class CatalogActions {
  private readonly session = inject(AuthSession);

  protected readonly canManageCatalogs = this.session.hasAnyRole(['ADMIN']);
}
```

## Tests

Add or update tests next to the code under test:

- API client tests for URL, method, request body, typed response handling, and error mapping.
- Service/facade tests for orchestration and role-aware behavior.
- Component tests for form validation, loading, empty, error, and action visibility.
- Guard tests for allowed roles and redirects.

Run from `tesla-web-app/` when the app exists:

```powershell
npm test
npm run build
```
