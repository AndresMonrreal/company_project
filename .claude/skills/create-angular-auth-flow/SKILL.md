---
name: create-angular-auth-flow
description: Implement Angular login, token storage, auth/session state, HTTP interceptors, route guards, role guards, logout, 401/403 handling, and tests for the Spring Boot JWT backend.
---

# Create Angular Auth Flow

Implement Angular auth under `tesla-web-app/src/app/core/auth`, `tesla-web-app/src/app/core/http`, `tesla-web-app/src/app/core/guards`, and `tesla-web-app/src/app/features/auth`.

Do not change backend auth behavior unless explicitly requested.

## Backend Contract

Login endpoint:

```text
POST /api/auth/login
```

Expected response:

```ts
export interface LoginResponse {
  accessToken: string;
  tokenType: 'Bearer';
  expiresAt: string;
  user: {
    id: number;
    username: string;
    role: 'ADMIN' | 'SUPERVISOR' | 'OPERADOR' | 'CONSULTA';
  };
}
```

Do not store raw passwords. Do not log tokens or passwords. Do not expose `password_hash`.

## Structure

```text
tesla-web-app/src/app/core/auth/
  auth-session.ts
  auth-token-storage.ts
  auth.models.ts
tesla-web-app/src/app/core/http/
  api-base-url.ts
  auth.interceptor.ts
  auth-error.interceptor.ts
tesla-web-app/src/app/core/guards/
  auth.guard.ts
  role.guard.ts
tesla-web-app/src/app/features/auth/
  login/
```

## Token Storage Abstraction

Create a single storage service. Components and guards must not call `localStorage` or `sessionStorage` directly.

## Wrong: Component Uses localStorage

```ts
export class LoginPage {
  saveToken(response: LoginResponse) {
    localStorage.setItem('accessToken', response.accessToken);
  }
}
```

Bug: token persistence spreads into UI code and becomes hard to clear consistently on `401`, logout, or expiry.

## Correct: AuthTokenStorage Service

```ts
@Injectable({ providedIn: 'root' })
export class AuthTokenStorage {
  private readonly key = 'company.accessToken';

  getAccessToken(): string | null {
    return localStorage.getItem(this.key);
  }

  setAccessToken(token: string): void {
    localStorage.setItem(this.key, token);
  }

  clear(): void {
    localStorage.removeItem(this.key);
  }
}
```

Keep the storage key in one place. If requirements later prefer memory-only storage, change only this service.

## Login Service

```ts
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = inject(AuthApiClient);
  private readonly tokens = inject(AuthTokenStorage);
  private readonly session = inject(AuthSession);

  login(request: LoginRequest) {
    return this.api.login(request).pipe(
      tap((response) => {
        this.tokens.setAccessToken(response.accessToken);
        this.session.setCurrentUser(response.user, response.expiresAt);
      })
    );
  }
}
```

## HTTP Interceptor

Use a functional interceptor. Attach `Authorization: Bearer <token>` only to configured backend API URLs.

## Wrong: Sends Token To External Domains

```ts
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const token = localStorage.getItem('accessToken');
  return next(request.clone({
    setHeaders: { Authorization: `Bearer ${token}` },
  }));
};
```

Bug: a request to maps, analytics, or another third-party URL can receive the JWT.

## Correct: Scope To Backend API Base URL

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

## Guards

Use guards for protected routes and role-specific areas, but do not treat guards as the security boundary.

## Wrong: Guard Only Checks Token Existence

```ts
export const authGuard: CanActivateFn = () => {
  return !!localStorage.getItem('accessToken');
};
```

Bug: a stale or manually inserted value can pass the UI check.

## Correct: Auth And Role Guard

```ts
export const authGuard: CanActivateFn = () => {
  const session = inject(AuthSession);
  const router = inject(Router);
  return session.isAuthenticated()
    ? true
    : router.createUrlTree(['/login']);
};
```

```ts
export const roleGuard: CanActivateFn = (route) => {
  const session = inject(AuthSession);
  const router = inject(Router);
  const roles = route.data['roles'] as string[] | undefined;

  if (!session.isAuthenticated()) {
    return router.createUrlTree(['/login']);
  }

  return roles?.some((role) => session.hasRole(role))
    ? true
    : router.createUrlTree(['/forbidden']);
};
```

Protected route example:

```ts
{
  path: 'catalogs',
  canActivate: [authGuard, roleGuard],
  data: { roles: ['ADMIN', 'SUPERVISOR'] },
  loadChildren: () => import('../features/catalogs/catalogs.routes')
    .then((m) => m.catalogsRoutes),
}
```

Backend endpoint authorization must enforce the same rule.

## 401 And 403 Handling

- `401`: clear token/session and redirect to `/login` when the failed request is protected.
- `403`: keep the session, route to `/forbidden`, or show a forbidden state.
- Do not display raw tokens, stack traces, SQL details, or backend exception internals.

## Logout

Logout clears token storage, current user state, and redirects to login. Do not call a backend logout endpoint unless one exists.

## Tests

Add tests for:

- Login request/response mapping.
- Token storage set/get/clear.
- Interceptor attaches token only to backend URLs.
- Interceptor does not attach token to external URLs.
- Auth guard redirects anonymous users.
- Role guard allows and denies expected roles.
- `401` clears session.
- `403` shows forbidden handling without clearing a valid session.
