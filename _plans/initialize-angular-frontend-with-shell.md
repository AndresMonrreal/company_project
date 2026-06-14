# Plan: Initialize Angular Frontend with Shell

## Context

`tesla-web-app/` is a skeleton of empty directories — no Angular project exists yet. This plan scaffolds the full Angular 17+ app (RubberTrace OPERATIONS), wires auth against the existing `POST /api/auth/login` endpoint in `tesla-api`, builds a role-aware app shell, and delivers a My Activity page driven by mock data. No backend changes are needed.

## Package base

`tesla-web-app/src/app/`

---

## Phase 1 — Scaffold & Config

### 1.1 Run `ng new` inside `tesla-web-app/`

**Action:** From inside `tesla-web-app/`, run:
```
ng new rubbertrace-web --directory . --standalone --routing --style=css --skip-git --force
```
`--force` is required because the directory is non-empty (skeleton `.gitkeep` files exist). This generates `package.json`, `angular.json`, `tsconfig.json`, `tsconfig.app.json`, `src/main.ts`, `src/app/app.component.ts`, `src/app/app.config.ts`, `src/app/app.routes.ts`, `src/styles.css`, `src/index.html`.

After scaffolding, delete `.gitkeep` files from all skeleton directories — they are no longer needed.

### 1.2 Install Tailwind CSS

**Files affected:** `tesla-web-app/package.json`, `tesla-web-app/tailwind.config.js`, `tesla-web-app/src/styles.css`

Run `npm install -D tailwindcss postcss autoprefixer` then `npx tailwindcss init`. Configure `tailwind.config.js` `content` array to cover `./src/**/*.{html,ts}`. In `src/styles.css` add the three Tailwind directives (`@tailwind base`, `@tailwind components`, `@tailwind utilities`).

### 1.3 Install Apollo Client

**Files affected:** `tesla-web-app/package.json`

Run `npm install apollo-angular @apollo/client graphql`. Apollo will be wired into providers in Phase 6 — install only here.

### 1.4 Create environment files

**Files:**
- `tesla-web-app/src/environments/environment.ts`
- `tesla-web-app/src/environments/environment.prod.ts`

Each exports an `environment` object with `apiBaseUrl: string`. Development value: `http://localhost:8080`. Production value: left as placeholder `''`. Register the file replacement in `angular.json` under `fileReplacements` for the production configuration.

### 1.5 Replace root `AppComponent`

**File:** `tesla-web-app/src/app/app.component.ts`

Replace the generated component with a minimal standalone component: selector `app-root`, template contains only `<router-outlet />`, imports `RouterOutlet`. No styles. This component is just the mount point.

---

## Phase 2 — Core Auth Layer

### 2.1 Define auth models

**File:** `tesla-web-app/src/app/core/auth/auth.models.ts`

Define and export three TypeScript interfaces:
- `UserSession` — fields: `userId: number`, `username: string`, `fullName: string`, `role: 'ADMIN' | 'SUPERVISOR' | 'OPERADOR' | 'CONSULTA'`
- `StoredToken` — fields: `accessToken: string`, `expiresAt: string` (ISO string from API)
- `LoginResponse` — matches the API response shape: `accessToken`, `tokenType`, `expiresAt`, `user: UserSession`

### 2.2 Create `AuthTokenStorage`

**File:** `tesla-web-app/src/app/core/auth/auth-token-storage.ts`

Injectable service (`providedIn: 'root'`). Wraps all localStorage access behind methods:
- `save(token: StoredToken): void` — serializes to JSON, writes to key `rubbertrace_token`
- `load(): StoredToken | null` — reads and parses; returns `null` if missing or malformed
- `clear(): void` — removes the key
- `isExpired(): boolean` — compares `expiresAt` ISO string against `Date.now()`

No other class may read or write `localStorage` directly.

### 2.3 Create `AuthSession`

**File:** `tesla-web-app/src/app/core/auth/auth-session.ts`

Injectable service (`providedIn: 'root'`). Uses Angular `signal<UserSession | null>(null)` to hold current session. Exposes:
- `session` — read-only signal (use `asReadonly()`)
- `role` — computed signal derived from `session()?.role ?? null`
- `initials` — computed signal: from `fullName` take first char of first word + first char of last word (split on space); if only one word, take first two chars of `username`; uppercase result
- `set(user: UserSession): void` — writes to the signal
- `clear(): void` — sets signal to `null`

On service construction, call `AuthTokenStorage.load()` — if a non-expired token exists, parse the user from the stored session (see note below) and call `set()`. Because `StoredToken` only stores the token string and expiry, also store the `UserSession` separately under key `rubbertrace_session` (JSON) in `AuthTokenStorage` so `AuthSession` can restore it on page reload.

Update `AuthTokenStorage.save()` to accept an optional `user: UserSession` and store it under `rubbertrace_session`. Add `loadUser(): UserSession | null` to `AuthTokenStorage`. Add `clear()` to also remove `rubbertrace_session`.

### 2.4 Create `AuthApiClient`

**File:** `tesla-web-app/src/app/core/auth/auth-api.client.ts`

Injectable service (`providedIn: 'root'`). Injects `HttpClient` and `API_BASE_URL` token (defined in Phase 3). Single method:
- `login(username: string, password: string): Observable<LoginResponse>` — `POST` to `${apiBaseUrl}/api/auth/login` with body `{ username, password }`

No error handling here — let errors propagate to `AuthService`.

### 2.5 Create `AuthService`

**File:** `tesla-web-app/src/app/core/auth/auth.service.ts`

Injectable service (`providedIn: 'root'`). Injects `AuthApiClient`, `AuthTokenStorage`, `AuthSession`, `Router`. Exposes:
- `login(username: string, password: string): Observable<void>` — calls `AuthApiClient.login()`, on success calls `AuthTokenStorage.save({ accessToken, expiresAt }, user)` and `AuthSession.set(user)`, then navigates to `/dashboard`; maps the observable to `void`
- `logout(): void` — calls `AuthTokenStorage.clear()`, `AuthSession.clear()`, navigates to `/login`
- `isAuthenticated(): boolean` — returns `!AuthTokenStorage.isExpired() && AuthSession.session() !== null`

---

## Phase 3 — HTTP Infrastructure

### 3.1 Create `API_BASE_URL` injection token

**File:** `tesla-web-app/src/app/core/http/api-url.token.ts`

Define and export `API_BASE_URL = new InjectionToken<string>('API_BASE_URL')`. This token is provided in `app.config.ts` (Phase 6) with `environment.apiBaseUrl`.

### 3.2 Create `authInterceptor`

**File:** `tesla-web-app/src/app/core/http/auth.interceptor.ts`

Functional HTTP interceptor (`HttpInterceptorFn`). Injects `AuthTokenStorage` and `API_BASE_URL`. If the request URL starts with the `API_BASE_URL` value AND a token exists and is not expired, clone the request adding `Authorization: Bearer <token>`. Otherwise pass through unchanged. Never adds the header to requests that do not match `API_BASE_URL`.

### 3.3 Create `authErrorInterceptor`

**File:** `tesla-web-app/src/app/core/http/auth-error.interceptor.ts`

Functional HTTP interceptor (`HttpInterceptorFn`). Injects `AuthSession`, `AuthTokenStorage`, `Router`. Uses `catchError` on the response stream. On `HttpErrorResponse` with `status === 401`: call `AuthTokenStorage.clear()`, `AuthSession.clear()`, navigate to `/login`, rethrow. On `status === 403`: do not clear session, rethrow. All other errors: rethrow as-is.

---

## Phase 4 — Route Guards

### 4.1 Create `authGuard`

**File:** `tesla-web-app/src/app/core/guards/auth.guard.ts`

Functional guard (`CanActivateFn`). Injects `AuthService` and `Router`. If `AuthService.isAuthenticated()` returns `true`, return `true`. Otherwise return `router.createUrlTree(['/login'])`.

### 4.2 Create `roleGuard`

**File:** `tesla-web-app/src/app/core/guards/role.guard.ts`

Functional guard (`CanActivateFn`). Reads `data['roles']` from the activated route snapshot — expects `string[]`. Injects `AuthSession` and `Router`. If `AuthSession.role()` is in the allowed roles array, return `true`. Otherwise return `router.createUrlTree(['/dashboard'])`. This is a UI hint only.

---

## Phase 5 — App Shell Layout

### 5.1 Define nav item model

**File:** `tesla-web-app/src/app/core/layout/nav-item.model.ts`

Define and export interface `NavItem` with fields: `label: string`, `route: string`, `roles: Array<'ADMIN' | 'SUPERVISOR' | 'OPERADOR' | 'CONSULTA'>`. Export `const NAV_ITEMS: NavItem[]` with all seven nav entries per the spec nav matrix:

| label | route | roles |
|---|---|---|
| Dashboard | /dashboard | ADMIN, SUPERVISOR, OPERADOR, CONSULTA |
| My Activity | /my-activity | ADMIN, SUPERVISOR, OPERADOR |
| Register Reception | /coming-soon | ADMIN, SUPERVISOR, OPERADOR |
| Register Cut | /coming-soon | ADMIN, SUPERVISOR, OPERADOR |
| Register Molding Output | /coming-soon | ADMIN, SUPERVISOR, OPERADOR |
| Reports | /coming-soon | ADMIN, SUPERVISOR, CONSULTA |
| Catalogs | /coming-soon | ADMIN |


### 5.2 Create `SidebarComponent`

**File:** `tesla-web-app/src/app/core/layout/sidebar.component.ts`

Standalone component. Injects `AuthSession`. Template structure (Tailwind classes):
- Outer: fixed left sidebar, dark background (`bg-gray-900`), full height, width `w-64`, flex column
- Top section: logo area — "RubberTrace" in white bold, "OPERATIONS" in smaller gray text, below that the role label from `AuthSession.role()` styled as a small uppercase badge
- Nav section: `@for` over `NAV_ITEMS` filtered by `item.roles.includes(AuthSession.role())`. Each item renders as a `routerLink` block with hover highlight. Use `RouterLinkActive` to highlight the active route.
- Bottom: logout button that calls `AuthService.logout()`

Imports: `RouterLink`, `RouterLinkActive`, `NgClass`, `NgFor` (or use control flow `@for`/`@if`).

### 5.3 Create `TopBarComponent`

**File:** `tesla-web-app/src/app/core/layout/top-bar.component.ts`

Standalone component. Injects `AuthSession`. Template (Tailwind):
- Outer: full-width top bar, white or dark background, flex row, items aligned right
- Shift placeholder: static text "Shift A · 06:00–14:00" (hardcoded for this spec)
- Date: "Today · " + current date formatted as `DD/M/YYYY` using `DatePipe` or `new Date()` inline
- Bell icon: SVG bell or Unicode character `🔔`, clickable, no action wired yet
- Avatar: circle div, dark background, white text showing `AuthSession.initials()` computed signal

Imports: `DatePipe` or compute the date string in the component class.

### 5.4 Create `AppShellComponent`

**File:** `tesla-web-app/src/app/core/layout/app-shell.component.ts`

Standalone component. Template: flex row full screen height. Left: `<app-sidebar />` (fixed width `w-64`). Right: flex column full width. Top: `<app-top-bar />`. Body: `<router-outlet />` in a scrollable content area. Imports: `SidebarComponent`, `TopBarComponent`, `RouterOutlet`.

---

## Phase 6 — App Config & Routes

### 6.1 Wire `app.config.ts`

**File:** `tesla-web-app/src/app/app.config.ts`

Replace the generated config. Providers array must include:
- `provideRouter(routes, withComponentInputBinding())` — import `routes` from `app.routes.ts`
- `provideHttpClient(withInterceptors([authInterceptor, authErrorInterceptor]))` — interceptors in this order (auth first, error handler second)
- `{ provide: API_BASE_URL, useValue: environment.apiBaseUrl }`
- Apollo provider: `provideApollo(() => ({ link: new HttpLink(inject(HttpClient)).create({ uri: environment.apiBaseUrl + '/graphql' }), cache: new InMemoryCache() }))` — Apollo is wired but will produce no traffic until GraphQL is enabled in `tesla-api`

### 6.2 Define `app.routes.ts`

**File:** `tesla-web-app/src/app/app.routes.ts`

Route table in order:
1. `{ path: 'login', loadComponent: () => import('./features/auth/login.page').then(m => m.LoginPageComponent) }` — public
2. `{ path: '', canActivate: [authGuard], component: AppShellComponent, children: [` — protected shell wrapper
   - `{ path: 'dashboard', loadComponent: () => import('./features/dashboard/dashboard.page').then(m => m.DashboardPageComponent) }`
   - `{ path: 'my-activity', canActivate: [roleGuard], data: { roles: ['ADMIN', 'SUPERVISOR', 'OPERADOR'] }, loadComponent: () => import('./features/my-activity/pages/my-activity.page').then(m => m.MyActivityPageComponent) }`
   - `{ path: 'coming-soon', loadComponent: () => import('./shared/ui/coming-soon.component').then(m => m.ComingSoonComponent) }`
   - `{ path: '', redirectTo: 'dashboard', pathMatch: 'full' }`
   - `{ path: '**', redirectTo: 'dashboard' }`
3. `{ path: '**', redirectTo: '' }` — catch-all outside shell

`AppShellComponent` is imported directly (not lazy) since it is the persistent shell.

---

## Phase 7 — Login Feature

### 7.1 Create `LoginPageComponent`

**File:** `tesla-web-app/src/app/features/auth/login.page.ts`

Standalone component. Imports: `ReactiveFormsModule`, `RouterLink`. Injects `AuthService`.

Form: `FormGroup` with `username: FormControl` (required, non-empty validator) and `password: FormControl` (required, non-empty validator).

State signals or properties: `loading = false`, `errorMessage: string | null = null`.

`submit()` method:
- If form invalid: mark all touched and return
- Set `loading = true`, `errorMessage = null`
- Call `AuthService.login(username, password)` — subscribe
- On error: set `loading = false`; if `HttpErrorResponse.status === 401` set `errorMessage = 'Invalid credentials. Please try again.'`; if network error set `errorMessage = 'Connection error. Check your network.'`
- On complete: `loading = false` (navigation already happened in `AuthService`)

Template (Tailwind, centered card layout, dark or light theme):
- App logo/title "RubberTrace" + "OPERATIONS" subtitle at top of card
- Username input bound to form control, label "Username"
- Password input type=password bound to form control, label "Password"
- Error message div: visible when `errorMessage` is set, styled in red
- Submit button: disabled when `loading` is true or form is pristine and invalid; shows "Signing in…" when loading
- No "forgot password" link — out of scope

### 7.2 Create `ComingSoonComponent` (shared placeholder)

**File:** `tesla-web-app/src/app/shared/ui/coming-soon.component.ts`

Standalone component. Template: centered message "This feature is coming soon." with a back link to `/dashboard`. Used by all placeholder nav routes.

---

## Phase 8 — Dashboard Feature

### 8.1 Create `DashboardPageComponent`

**File:** `tesla-web-app/src/app/features/dashboard/dashboard.page.ts`

Standalone component. Template: page heading "Dashboard" in large text. No data, no cards. Serves as the authenticated landing page. Injects `AuthSession` — optionally shows "Welcome, {fullName}" subtitle.

---

## Phase 9 — My Activity Feature

### 9.1 Define `ActivityRecord` model

**File:** `tesla-web-app/src/app/features/my-activity/models/activity-record.model.ts`

Export interface `ActivityRecord`:
```
time: string
container: string
profile: string
action: 'RECEPTION' | 'CUT' | 'SCRAP' | 'MOLDING_OUTPUT'
quantities: string
status: 'RECEIVED' | 'CUT' | 'CLOSED' | 'SENT_TO_MOLDING' | 'IN_CUTTING'
```

Export type aliases `ActivityAction` and `ActivityStatus` for the union types.

Also export two constant maps (or functions):
- `ACTION_BADGE_COLOR: Record<ActivityAction, string>` — Tailwind classes: RECEPTION → blue, CUT → yellow, SCRAP → orange, MOLDING_OUTPUT → purple
- `STATUS_BADGE_COLOR: Record<ActivityStatus, string>` — RECEIVED → blue, IN_CUTTING → yellow, CUT → green, SENT_TO_MOLDING → purple, CLOSED → gray

### 9.2 Create mock data

**File:** `tesla-web-app/src/app/features/my-activity/data-access/activity-mock.data.ts`

Export `const ACTIVITY_MOCK_DATA: ActivityRecord[]` — an array of at least 8 records covering all action types and all status values. Use realistic-looking values: times between 06:00–14:00, container codes like `CNT-00481`, profiles like `P-36`, `P-37`, quantities like `"120 pcs"`, `"45 pcs"`. Vary action and status across rows so all badge colors are visible.

Export `const ACTIVITY_EMPTY_DATA: ActivityRecord[] = []` for the dev toggle empty state.

### 9.3 Create `ActivityFilterService`

**File:** `tesla-web-app/src/app/features/my-activity/services/activity-filter.service.ts`

Injectable service (not `providedIn: 'root'` — provide in the My Activity routes or component). Holds filter state using signals:
- `actionFilter = signal<ActivityAction | 'ALL'>('ALL')`
- `timeFrom = signal<string>('')`
- `timeTo = signal<string>('')`
- `searchTerm = signal<string>('')`

Exposes:
- `filter(records: ActivityRecord[]): ActivityRecord[]` — applies all active filters in sequence: action tab filter (exact match unless 'ALL'), time range filter (string comparison on `time`), search filter (case-insensitive `includes` on `container` or `profile`)
- `reset(): void` — resets all signals to defaults

### 9.4 Create `ActivitySummaryCardsComponent`

**File:** `tesla-web-app/src/app/features/my-activity/components/activity-summary-cards.component.ts`

Standalone component. Input: `records: ActivityRecord[]`. Template: 4-column grid of cards (Tailwind `grid grid-cols-4 gap-4`). Each card:
- Title: "My Receptions", "My Cuts", "My Scrap", "My Molding Outputs"
- Count: derived from `records.filter(r => r.action === '<ACTION>').length`
- Subtitle: "this shift"
- Simple rounded card with subtle background

Derive counts in the component class, not the template, for clarity.

### 9.5 Create `ActivityFilterBarComponent`

**File:** `tesla-web-app/src/app/features/my-activity/components/activity-filter-bar.component.ts`

Standalone component. Injects `ActivityFilterService`. Imports: `FormsModule`.

Template:
- Tab strip: All / Reception / Cut / Molding Output / Scrap — clicking sets `ActivityFilterService.actionFilter`. Active tab highlighted.
- Time range: two `<input type="time">` inputs bound to `timeFrom` and `timeTo` signals via two-way binding wrapper
- Search input: text input bound to `searchTerm` signal, placeholder "Search container or lot…"
- Reset button: calls `ActivityFilterService.reset()`

Emits no output events — filter state lives in the service.

### 9.6 Create `ActivityTableComponent`

**File:** `tesla-web-app/src/app/features/my-activity/components/activity-table.component.ts`

Standalone component. Input: `records: ActivityRecord[]`. Imports: `RouterLink`, `NgClass`.

Template:
- If `records.length === 0`: show empty state div "No activity records match your filters."
- Otherwise: `<table>` with columns: Time, Container, Profile, Action, Quantities, Status, (eye icon)
- Container cell: `<a [routerLink]="['/coming-soon']">{{ record.container }}</a>` — clickable link styled with underline
- Action cell: `<span>` with Tailwind classes from `ACTION_BADGE_COLOR[record.action]`, displays human-readable label ("Reception", "Cut", "Scrap", "Molding Output")
- Status cell: `<span>` with Tailwind classes from `STATUS_BADGE_COLOR[record.status]`
- Eye icon cell: SVG eye icon or `👁` character; clickable, no action wired for this spec

### 9.7 Create `MyActivityPageComponent`

**File:** `tesla-web-app/src/app/features/my-activity/pages/my-activity.page.ts`

Standalone component. Provides `ActivityFilterService` in its own providers array. Imports: `ActivitySummaryCardsComponent`, `ActivityFilterBarComponent`, `ActivityTableComponent`.

State:
- `populated = signal(true)` — dev toggle state
- `source = computed(() => populated() ? ACTIVITY_MOCK_DATA : ACTIVITY_EMPTY_DATA)`
- `filtered = computed(() => activityFilterService.filter(source()))` — recomputed whenever filter signals change

Template:
- Header row: `<h1>My Activity</h1>` + shift badge span "Shift A · Active" (hardcoded for this spec)
- Dev toggle button: label shows "Toggle state: populated" or "Toggle state: empty" based on `populated()`; clicking flips the signal
- `<app-activity-summary-cards [records]="source()" />`
- `<app-activity-filter-bar />`
- `<app-activity-table [records]="filtered()" />`

---

## Agent routing

| Phase | Agent / Skill |
|---|---|
| Phase 1 — Scaffold | Run commands manually or use `frontend-developer` agent |
| Phase 2 — Core Auth | `$create-angular-auth-flow` skill → `frontend-developer` agent |
| Phase 3 — HTTP | `$create-angular-auth-flow` skill → `frontend-developer` agent |
| Phase 4 — Guards | `$create-angular-auth-flow` skill → `frontend-developer` agent |
| Phase 5 — Layout | `frontend-developer` agent |
| Phase 6 — Config/Routes | `frontend-developer` agent |
| Phase 7 — Login | `$create-angular-auth-flow` skill → `frontend-developer` agent |
| Phase 8 — Dashboard | `$create-angular-feature` skill → `frontend-developer` agent |
| Phase 9 — My Activity | `$create-angular-feature` skill → `frontend-developer` agent |
| Pre-commit | `$review-angular-changes` skill |

---

## Implementation order

1. Run `ng new` inside `tesla-web-app/` (step 1.1)
2. Install and configure Tailwind CSS (step 1.2)
3. Install Apollo Client (step 1.3)
4. Create environment files (step 1.4)
5. Replace root `AppComponent` (step 1.5)
6. Define `API_BASE_URL` token (step 3.1)
7. Define auth models (step 2.1)
8. Create `AuthTokenStorage` (step 2.2)
9. Create `AuthSession` (step 2.3)
10. Create `AuthApiClient` (step 2.4)
11. Create `AuthService` (step 2.5)
12. Create `authInterceptor` (step 3.2)
13. Create `authErrorInterceptor` (step 3.3)
14. Create `authGuard` (step 4.1)
15. Create `roleGuard` (step 4.2)
16. Define nav item model and `NAV_ITEMS` constant (step 5.1)
17. Create `SidebarComponent` (step 5.2)
18. Create `TopBarComponent` (step 5.3)
19. Create `AppShellComponent` (step 5.4)
20. Wire `app.config.ts` (step 6.1)
21. Define `app.routes.ts` (step 6.2)
22. Create `LoginPageComponent` (step 7.1)
23. Create `ComingSoonComponent` (step 7.2)
24. Create `DashboardPageComponent` (step 8.1)
25. Define `ActivityRecord` model and badge color maps (step 9.1)
26. Create mock data (step 9.2)
27. Create `ActivityFilterService` (step 9.3)
28. Create `ActivitySummaryCardsComponent` (step 9.4)
29. Create `ActivityFilterBarComponent` (step 9.5)
30. Create `ActivityTableComponent` (step 9.6)
31. Create `MyActivityPageComponent` (step 9.7)
32. Run `ng serve` and smoke-test all acceptance criteria
33. Run `$review-angular-changes` before commit

---

## Critical files

| File | Action |
|---|---|
| `tesla-web-app/src/app/app.config.ts` | Rewrite — add router, HttpClient with interceptors, API_BASE_URL, Apollo |
| `tesla-web-app/src/app/app.routes.ts` | Rewrite — full route table with shell wrapper and lazy routes |
| `tesla-web-app/src/app/app.component.ts` | Rewrite — minimal router-outlet only |
| `tesla-web-app/src/styles.css` | Add Tailwind directives |
| `tesla-web-app/tailwind.config.js` | Create — content paths covering all TS/HTML |
| `tesla-web-app/src/environments/environment.ts` | Create — apiBaseUrl pointing to localhost:8080 |
| `tesla-web-app/src/app/core/auth/auth-token-storage.ts` | Create — sole localStorage accessor |
| `tesla-web-app/src/app/core/auth/auth-session.ts` | Create — signal-based session state |
| `tesla-web-app/src/app/core/auth/auth-api.client.ts` | Create — typed login API call |
| `tesla-web-app/src/app/core/auth/auth.service.ts` | Create — login/logout orchestration |
| `tesla-web-app/src/app/core/http/api-url.token.ts` | Create — injection token |
| `tesla-web-app/src/app/core/http/auth.interceptor.ts` | Create — bearer token injection |
| `tesla-web-app/src/app/core/http/auth-error.interceptor.ts` | Create — 401/403 handling |
| `tesla-web-app/src/app/core/guards/auth.guard.ts` | Create — unauthenticated redirect |
| `tesla-web-app/src/app/core/guards/role.guard.ts` | Create — role-based route guard |
| `tesla-web-app/src/app/core/layout/nav-item.model.ts` | Create — NAV_ITEMS constant |
| `tesla-web-app/src/app/core/layout/sidebar.component.ts` | Create — role-filtered nav |
| `tesla-web-app/src/app/core/layout/top-bar.component.ts` | Create — shift/date/avatar bar |
| `tesla-web-app/src/app/core/layout/app-shell.component.ts` | Create — shell layout wrapper |
| `tesla-web-app/src/app/features/auth/login.page.ts` | Create — login form |
| `tesla-web-app/src/app/shared/ui/coming-soon.component.ts` | Create — placeholder route |
| `tesla-web-app/src/app/features/dashboard/dashboard.page.ts` | Create — empty dashboard |
| `tesla-web-app/src/app/features/my-activity/models/activity-record.model.ts` | Create — model + badge maps |
| `tesla-web-app/src/app/features/my-activity/data-access/activity-mock.data.ts` | Create — mock records |
| `tesla-web-app/src/app/features/my-activity/services/activity-filter.service.ts` | Create — filter state |
| `tesla-web-app/src/app/features/my-activity/components/activity-summary-cards.component.ts` | Create — 4 count cards |
| `tesla-web-app/src/app/features/my-activity/components/activity-filter-bar.component.ts` | Create — filter controls |
| `tesla-web-app/src/app/features/my-activity/components/activity-table.component.ts` | Create — activity table |
| `tesla-web-app/src/app/features/my-activity/pages/my-activity.page.ts` | Create — page orchestrator |

---

## Verification

1. `cd tesla-web-app && ng serve` — starts with no compilation errors
2. Open `http://localhost:4200/` — redirects to `/login`
3. Open `http://localhost:4200/dashboard` — redirects to `/login`
4. Submit login with valid `tesla-api` credentials (`admin` / configured password) — redirects to `/dashboard`, sidebar visible
5. Sidebar nav items match the role of the logged-in user (test with ADMIN → all items; test with OPERADOR → no Reports, no Catalogs)
6. Role label in sidebar matches authenticated user's role
7. Top bar shows "Shift A · 06:00–14:00", today's date, bell icon, user initials
8. Navigate to `/my-activity` — 4 summary cards show counts, table renders 8+ rows with colored badges
9. Click action tab "Cut" — table filters to CUT rows only; click "All" — all rows return
10. Click Reset — filters clear
11. Toggle dev button — table empties; toggle again — data returns
12. Container link (`CNT-xxxxx`) is clickable (routes to `/coming-soon`)
13. Click "Register Reception" in sidebar — navigates to `/coming-soon` page
14. Submit bad credentials — inline error "Invalid credentials. Please try again." appears; form stays usable
15. `localStorage` inspection in DevTools — only keys `rubbertrace_token` and `rubbertrace_session` exist; no other direct storage calls
16. Sign out via logout button — clears storage, redirects to `/login`
17. With valid session, artificially delete `rubbertrace_token` from DevTools — next navigation triggers 401 intercept → redirect to `/login`
