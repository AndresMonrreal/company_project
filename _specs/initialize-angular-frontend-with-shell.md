# Spec: Initialize Angular Frontend with Shell

## Summary

Scaffold the `tesla-web-app` Angular application (RubberTrace OPERATIONS) with a working login page, role-aware app shell, empty dashboard route, and a My Activity page driven by mock data.

## Problem

`tesla-web-app/` exists as a skeleton only — no Angular app has been created. Operators and supervisors have no UI to authenticate or perform their shift activities. This spec delivers the structural foundation and first operational screen so that subsequent specs can add real API integration.

## Goals

- Scaffold Angular 17+ app inside `tesla-web-app/` with standalone components, Tailwind CSS, and Apollo Client wired up
- Implement working login page backed by `POST /api/auth/login`
- Implement role-aware app shell: fixed dark sidebar + top bar
- Provide empty `/dashboard` route visible after login
- Implement `/my-activity` page with mock data showing full activity table and summary cards
- Session state managed through `AuthTokenStorage` and `AuthSession` — never raw localStorage

## Non-Goals

- Register Reception, Register Cut, Register Molding Output forms
- Reports module
- Catalog management screens (Profiles, Machines, Shifts, Containers, Roles)
- Real GraphQL or REST integration beyond login (next spec)
- Unit and integration tests (separate spec)

## Users and Flows

**Primary users:** OPERADOR, SUPERVISOR, ADMIN, CONSULTA

**Login flow:**
1. Unauthenticated user lands on `/login`
2. Submits username + password
3. `POST /api/auth/login` returns `{ accessToken, tokenType, expiresAt, user: { userId, username, fullName, role } }`
4. Token stored via `AuthTokenStorage`; session stored in `AuthSession`
5. User redirected to `/dashboard`

**Authenticated shell flow:**
1. `authGuard` checks for valid token; redirects to `/login` if absent
2. App shell renders dark sidebar (role-filtered nav) + top bar
3. Sidebar shows role label beneath logo
4. Top bar shows hardcoded shift placeholder, today's date, bell icon, user initials avatar
5. `/my-activity` route renders summary cards + filterable activity table from mock data

**401 flow:** Token rejected → `authErrorInterceptor` clears session → redirect `/login`

**403 flow:** Forbidden response → forbidden state shown; session preserved

## Backend Scope

No backend changes in this spec. Consumes existing `POST /api/auth/login` endpoint from `tesla-api`.

Auth contract:
```
POST /api/auth/login
Body:  { "username": string, "password": string }
200:   { accessToken, tokenType, expiresAt, user: { userId, username, fullName, role } }
401:   { code: "auth.invalid-credentials", ... }
```

## Frontend Scope

**Package:** `tesla-web-app/`  
**Skill:** `$create-angular-auth-flow` for auth module; `$create-angular-feature` for dashboard and my-activity

### App scaffold (`ng new` inside `tesla-web-app/`)
- Standalone components, no NgModules, routing enabled
- Tailwind CSS configured
- Apollo Client configured (HttpLink pointed at `API_BASE_URL/graphql` — inactive until GraphQL enabled)
- `API_BASE_URL` injection token pointing to `tesla-api`

### `core/auth/`
- `AuthTokenStorage` — reads/writes token; never exposes raw localStorage to components
- `AuthSession` — holds `{ userId, username, fullName, role }` as a signal/observable
- `AuthService` — calls `AuthApiClient`, stores result via `AuthTokenStorage` + `AuthSession`, exposes `login()` and `logout()`
- `AuthApiClient` — typed REST client, single method `login(username, password)`

### `core/http/`
- `authInterceptor` — attaches `Authorization: Bearer <token>` only to requests to `API_BASE_URL`
- `authErrorInterceptor` — on 401 clears session and navigates to `/login`; on 403 does not clear session

### `core/guards/`
- `authGuard` — redirects to `/login` if no valid token
- `roleGuard` — blocks route if user role not in allowed list (UI hint only)

### `core/layout/`
- `AppShellComponent` — standalone shell wrapping sidebar + top bar + `<router-outlet>`
- `SidebarComponent` — dark fixed sidebar; role-filtered nav items (see nav matrix below)
- `TopBarComponent` — hardcoded shift placeholder "Shift A · 06:00–14:00" for this spec (real shift data from JWT or API in a future spec), today's date, bell icon, initials avatar derived from `fullName` (first letter of first word + first letter of last word, e.g. "Mario Rodriguez" → "MR"; falls back to first two chars of `username` if `fullName` has only one word)

**Nav matrix:**

| Route label | OPERADOR | SUPERVISOR | ADMIN | CONSULTA |
|---|---|---|---|---|
| Dashboard | ✓ | ✓ | ✓ | ✓ |
| My Activity | ✓ | ✓ | ✓ | — |
| Register Reception | ✓ | ✓ | ✓ | — |
| Register Cut | ✓ | ✓ | ✓ | — |
| Register Molding Output | ✓ | ✓ | ✓ | — |
| Reports | — | ✓ | ✓ | ✓ |
| Catalogs | — | — | ✓ | — |

Register Reception/Cut/Molding Output routes are rendered in sidebar but link to a placeholder "coming soon" page for this spec.

### `features/auth/` — Login page
- `LoginPageComponent` — standalone, reactive form, username + password fields, submit calls `AuthService.login()`, loading state, error display for invalid credentials

### `features/dashboard/` — Dashboard (empty)
- `DashboardPageComponent` — standalone placeholder "Dashboard" heading; no data

### `features/my-activity/` — My Activity page

**Mock data shape:**
```typescript
interface ActivityRecord {
  time: string;         // e.g. "08:32"
  container: string;    // e.g. "CNT-00481"
  profile: string;      // e.g. "P-36"
  action: 'RECEPTION' | 'CUT' | 'SCRAP' | 'MOLDING_OUTPUT';
  quantities: string;   // e.g. "120 pcs"
  status: 'RECEIVED' | 'CUT' | 'CLOSED' | 'SENT_TO_MOLDING' | 'IN_CUTTING';
}
```

**Page layout:**
- Header row: "My Activity" title + active shift badge (e.g. "Shift A · Active")
- Dev toggle button: "Toggle state: populated / empty" — switches between populated mock and empty state
- 4 summary cards: My Receptions, My Cuts, My Scrap, My Molding Outputs — each shows count + "this shift"
- Filter bar: All / Reception / Cut / Molding Output / Scrap tab strip + time range inputs + container/lot search box + Reset button
- Activity table: Time, Container (clickable link), Profile, Action (badge), Quantities, Status (badge), eye icon action column
- Action badge colors: Reception → blue, Cut → yellow, Scrap → orange, Molding Output → purple
- Status badge colors: RECEIVED → blue, IN_CUTTING → yellow, CUT → green, SENT_TO_MOLDING → purple, CLOSED → gray
- Empty state: friendly message when no records match filters

### Routes
```
/login               → LoginPageComponent      (public)
/dashboard           → DashboardPageComponent  (authGuard)
/my-activity         → MyActivityPageComponent (authGuard, roles: OPERADOR, SUPERVISOR, ADMIN)
/                    → redirect to /dashboard
/**                  → redirect to /dashboard
```

## Data and Validation

**Login form:**
- `username`: required, non-empty string
- `password`: required, non-empty string
- Submit disabled while loading
- Error shown on 401: "Invalid credentials. Please try again."
- Error shown on network failure: "Connection error. Check your network."

**Token storage:**
- `accessToken` stored in `AuthTokenStorage` (localStorage key: `rubbertrace_token`)
- `expiresAt` stored alongside token for expiry check
- `AuthSession` populated from token payload on app init

**My Activity filters (client-side only, mock data):**
- Action filter: mutually exclusive tab selection
- Time range: optional start/end time strings
- Search: partial match on container code or lot number
- All filters composable; Reset clears to defaults

## Security and Access

- `authInterceptor` must attach `Authorization` header **only** to requests matching `API_BASE_URL` — never to third-party or CDN requests
- `AuthTokenStorage` is the sole point of contact with localStorage; no component or service may read/write it directly
- `authGuard` and `roleGuard` are UI-layer hints; backend enforces real authorization
- No token refresh in this spec — expired token triggers 401 flow (clear + redirect)
- `password` field value must never be logged, stored beyond the HTTP request, or included in error messages
- `role` read from JWT `role` claim via `AuthSession` — do not re-fetch role from API on every route

## Acceptance Criteria

1. `ng new` completes successfully inside `tesla-web-app/`; `ng serve` starts without errors
2. Navigating to `/` or any protected route while unauthenticated redirects to `/login`
3. Submitting valid credentials against `tesla-api` stores the token and redirects to `/dashboard`
4. Submitting invalid credentials shows an inline error; the form remains usable
5. After login, the sidebar renders only the nav items allowed for the authenticated role
6. Role label in sidebar reflects the authenticated user's role
7. Top bar displays shift name/hours placeholder, today's date, bell icon, and user initials
8. `/my-activity` renders 4 summary cards with counts derived from mock data
9. Action and status badges render with correct colors per the spec
10. Container values in the table are rendered as clickable links (route placeholder for this spec)
11. Filtering by action tab reduces visible rows; Reset restores all rows
12. Dev toggle switches between populated mock data and empty state
13. On 401 response, session is cleared and user is redirected to `/login`
14. `AuthTokenStorage` is the only code that touches localStorage — no direct localStorage calls elsewhere
