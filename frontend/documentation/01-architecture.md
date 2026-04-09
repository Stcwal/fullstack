# Architecture — IK-Kontrollsystem Frontend

**Project**: IK-Kontrollsystem — Digital internal control system for Norwegian restaurants
**Course**: IDATT2105 Fullstack, NTNU
**Last updated**: 2026-04-09

---

## 1. System Overview and Purpose

IK-Kontrollsystem is a web-based internal control (internkontroll) platform designed for Norwegian food-service businesses. It digitises the legal obligations that restaurants carry under the Norwegian Food Safety Authority (Mattilsynet) and the Norwegian Alcohol Act.

The system replaces paper-based logbooks and spreadsheets with a structured, role-aware interface that kitchen staff and managers can operate quickly — even under time pressure during a service. Every design and architecture decision reflects this operational reality: the UI must communicate status instantly, actions must require minimal steps, and the application must work equally well on a wall-mounted tablet in the kitchen as on a manager's desktop browser.

**Core functional areas**:

- **Temperature logging** — Daily recording of freezer and refrigerator readings with automatic out-of-range detection
- **Checklists** — Daily, weekly, and monthly task lists (opening, closing, cleaning, HACCP compliance)
- **Deviations** — Reporting and tracking of non-conformances with severity classification and resolution workflow
- **Temperature graphs** — 7-day and 30-day chart views of all storage unit readings, with alert markers
- **Training documents** — Employee certification tracking and document library (HACCP plans, procedure PDFs, video guides)
- **Settings (ADMIN only)** — Storage unit configuration, user management with per-user permissions, organisation profile, and checklist template management

**Modules**:

- **IK-Mat** — Food safety internkontroll (checklists, temperature logging, deviations)
- **IK-Alkohol** — Alcohol service compliance (age-check logs, beverage storage, bartender checklists)

Data is tagged with `moduleType: 'IK_MAT' | 'IK_ALKOHOL'` so the same views serve both compliance domains.

---

## 2. Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Browser / Tablet                            │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                      Vue 3 SPA (Vite)                        │   │
│  │                                                              │   │
│  │  ┌─────────────┐   ┌──────────────────────────────────────┐ │   │
│  │  │  App.vue    │   │           Vue Router                  │ │   │
│  │  │  (layout    │   │  /login  /dashboard  /fryser  etc.   │ │   │
│  │  │   shell)    │   │  beforeEach guard: auth + role checks│ │   │
│  │  └──────┬──────┘   └───────────────┬──────────────────────┘ │   │
│  │         │                          │                         │   │
│  │  ┌──────▼──────────────────────────▼──────────────────────┐ │   │
│  │  │                      Views                             │ │   │
│  │  │  DashboardView  FreezerView  FridgeView  ChecklistView │ │   │
│  │  │  DeviationsView  GraphView  TrainingView  AlkoholView  │ │   │
│  │  │  settings/ (UnitsTab, UsersTab, OrgTab, ChecklistsTab) │ │   │
│  │  └──────────────────────┬─────────────────────────────────┘ │   │
│  │                         │ calls                              │   │
│  │  ┌──────────────────────▼─────────────────────────────────┐ │   │
│  │  │                   Pinia Stores                         │ │   │
│  │  │  authStore  layoutStore  unitsStore  readingsStore     │ │   │
│  │  │  checklistsStore  deviationsStore  dashboardStore      │ │   │
│  │  │  shiftStore  alkoholStore                              │ │   │
│  │  └──────────────────────┬─────────────────────────────────┘ │   │
│  │                         │ calls                              │   │
│  │  ┌──────────────────────▼─────────────────────────────────┐ │   │
│  │  │                  Service Layer                         │ │   │
│  │  │  auth.service   units.service   readings.service       │ │   │
│  │  │  checklists.service  deviations.service                │ │   │
│  │  │  dashboard.service  organization.service               │ │   │
│  │  │  reports.service  documents.service                    │ │   │
│  │  └──────────────────────┬─────────────────────────────────┘ │   │
│  │                         │                                    │   │
│  │  ┌──────────────────────▼─────────────────────────────────┐ │   │
│  │  │          api.ts — Axios instance                       │ │   │
│  │  │  baseURL: /api  |  Bearer token interceptor            │ │   │
│  │  │  401 response → clear session + redirect /login        │ │   │
│  │  └──────────────────────┬─────────────────────────────────┘ │   │
│  └─────────────────────────┼────────────────────────────────────┘   │
└────────────────────────────┼────────────────────────────────────────┘
                             │ HTTP (proxied by Vite dev server)
                             ▼
              ┌──────────────────────────────┐
              │  Spring Boot Backend         │
              │  localhost:8080              │
              │  /api/**                     │
              └──────────────────────────────┘
```

All requests from the service layer flow through the single Axios instance in `api.ts`. During development, Vite proxies `/api` to `http://localhost:8080`, so the frontend never handles CORS directly.

---

## 3. Frontend Tech Stack

| Technology | Version | Justification |
|---|---|---|
| Vue 3 | ^3.4 | Composition API gives clean, testable logic separation; `<script setup>` reduces boilerplate. Required by course. |
| TypeScript | ^5.3 | Full type safety across stores, services, and component props. Catches contract mismatches between frontend and backend early. |
| Vite | ^5.0 | Near-instant HMR, ES module-native build. Much faster dev cycle than Webpack/Vue CLI for a project of this size. |
| Pinia | ^2.1 | Official Vue state management. Composition-style stores are more maintainable and TypeScript-friendly than Vuex options API. |
| Vue Router | ^4.3 | Official router. Supports nested routes (settings tabs), navigation guards, and lazy-loaded views. |
| Axios | ^1.6 | HTTP client with interceptor support. Interceptors are used for JWT injection and 401 auto-logout — difficult to implement cleanly with raw `fetch`. |
| Chart.js + vue-chartjs | ^4.4 / ^5.3 | Chart.js is the industry standard for Canvas-based time-series charts. `vue-chartjs` provides idiomatic Vue wrappers. No alternative was considered necessary given the project scope. |
| No CSS framework | — | A CSS framework (Tailwind, Bootstrap) would introduce visual opinions and utility-class noise that conflicts with the clarity-first design philosophy. A hand-crafted design system (`main.css`) gives full control over every visual state: status colours, touch targets, spacing rhythm. |

---

## 4. Directory Structure

```
frontend/
├── index.html                  Entry HTML. Single <div id="app"> mount point.
├── vite.config.ts              Vite config: Vue plugin, @-alias, /api dev proxy to :8080.
├── tsconfig.json               TypeScript config for application code.
├── tsconfig.node.json          TypeScript config for Vite config file itself.
├── package.json                Dependencies: vue, pinia, vue-router, axios, chart.js.
│
└── src/
    ├── main.ts                 App bootstrap: createApp → use(pinia) → use(router) → mount.
    ├── App.vue                 Root component. Renders desktop (sidebar) or tablet (tab bar)
    │                           shell based on layoutStore.isTabletMode. Hosts RouterView.
    │
    ├── assets/
    │   └── main.css            Global design system. CSS custom properties (tokens) for
    │                           colours, shadows, radii, spacing. Base reset and typography.
    │                           No utility classes — tokens only. All component styles are scoped.
    │
    ├── types/
    │   └── index.ts            Single source of truth for all TypeScript types and interfaces.
    │                           Covers: Auth (User, UserRole, UserPermissions, AuthResponse),
    │                           Units (Unit, UnitType), Readings (TemperatureReading, NewReading),
    │                           Checklists (Checklist, ChecklistItem, ChecklistFrequency),
    │                           Deviations (Deviation, NewDeviation, DeviationStatus, DeviationSeverity),
    │                           Dashboard (DashboardStats, DashboardTask, DashboardAlert),
    │                           Training (TrainingDocument, EmployeeCertification),
    │                           Organisation (Organization, SettingsUser, SettingsUnit),
    │                           Reports (ChartPeriod), Modules (ModuleType).
    │
    ├── router/
    │   └── index.ts            Vue Router configuration. All routes, meta flags (requiresAuth,
    │                           requiresAdmin), and the beforeEach navigation guard.
    │
    ├── stores/
    │   ├── auth.ts             useAuthStore — user, token, isAuthenticated. Login/logout.
    │   │                       Re-hydrates from sessionStorage on store init.
    │   ├── layout.ts           useLayoutStore — isTabletMode, isSidebarCollapsed. Toggle fns.
    │   ├── units.ts            useUnitsStore — storage unit CRUD, getByType filter.
    │   ├── readings.ts         useReadingsStore — temperature readings by unit, addReading.
    │   ├── checklists.ts       useChecklistsStore — checklists by frequency, toggleItem
    │   │                       with optimistic update and error revert.
    │   ├── deviations.ts       useDeviationsStore — list/report/resolve deviations. openCount().
    │   └── dashboard.ts        useDashboardStore — stats, tasks, alerts snapshot for overview.
    │   ├── shift.ts            useShiftStore — tracks the currently active shift worker. The
    │   │                       logged-in user may record entries on behalf of the worker shown
    │   │                       on the current shift. Exposes activeWorkerId (number | null) for
    │   │                       inclusion in request bodies. Does not change JWT or permissions.
    │   └── alkohol.ts          useAlkoholStore — IK-Alkohol module state: age verification
    │                           entries, serving incidents, compliance stats. Handles 403 gracefully
    │                           when STAFF role lacks access to alcohol endpoints.
    │
    ├── services/
    │   ├── api.ts              Axios instance. Base URL /api, 10s timeout. Request interceptor
    │   │                       adds Bearer token. Response interceptor unwraps ApiResponse<T>
    │   │                       envelope and handles 401 auto-logout.
    │   ├── auth.service.ts     authService.login(), authService.me(). Wired to real backend.
    │   ├── units.service.ts    unitsService CRUD: getAll, create, update, remove. Wired.
    │   ├── readings.service.ts readingsService: getByUnit, create (with range check). Wired.
    │   ├── checklists.service.ts checklistsService: getByFrequency, toggleItem. Template CRUD:
    │   │                       getTemplates, createTemplate, updateTemplate, deleteTemplate.
    │   ├── deviations.service.ts deviationsService: getAll, create, resolve. Wired.
    │   ├── dashboard.service.ts dashboardService: get() returns stats+tasks+alerts. Wired.
    │   ├── organization.service.ts organizationService: getOrg, updateOrg, getUsers,
    │   │                       updateUser, createUser.
    │   ├── alkohol.service.ts  alkoholService: getAlderskontrollEntries, getIncidents,
    │   │                       getStats. Individual failures caught silently — 403 on
    │   │                       STAFF role does not crash the page.
    │   ├── reports.service.ts  reportsService: getChartData(period), exportPdf(), exportJson().
    │   │                       Chart data wired; export stubs not yet implemented.
    │   └── documents.service.ts documentsService: getTrainingDocs, getCertifications.
    │
    ├── composables/
    │   └── usePermission.ts    Returns a computed boolean for a named UserPermissions key.
    │                           Falls back to role-derived defaults when permissions object is
    │                           absent. Used to gate feature access within views.
    │
    ├── components/
    │   ├── layout/
    │   │   ├── AppSidebar.vue  Desktop navigation. Collapsible (icon-only or expanded).
    │   │   │                   Shows nav items + admin section for ADMIN role. User info + logout
    │   │   │                   in footer. Inline SidebarIcon component (no external icon library).
    │   │   ├── AppTabBar.vue   Tablet bottom tab bar. Horizontally scrollable tabs with per-tab
    │   │   │                   accent colours. All labels uppercase. adminOnly tabs filtered
    │   │   │                   by role. Sticky to bottom inside tablet device frame.
    │   │   └── TabletToggle.vue Dev tool button (always visible). Switches layoutStore between
    │   │                       desktop and tablet simulator mode.
    │   ├── AppModal.vue        Generic modal overlay with slot content and close button.
    │   ├── AppBadge.vue        Colour-coded status badge. Used for deviation severity, status,
    │   │                       checklist frequency, module type tags.
    │   ├── AppAlert.vue        In-page alert banner (danger / warning / info variants).
    │   └── AppProgressBar.vue  Horizontal progress bar. Used on dashboard compliance metric.
    │
    └── views/
        ├── LoginView.vue           Login form. Calls authStore.login(). Redirects to /dashboard.
        ├── DashboardView.vue       Overview: compliance stats, today's task list, active alerts.
        ├── FreezerView.vue         Freezer unit cards. Log reading form. Recent readings table.
        │                           Supports shift worker attribution via shiftStore.activeWorkerId.
        ├── FridgeView.vue          Refrigerator unit cards. Log reading form. Recent readings table.
        ├── TemperaturLoggView.vue  Tablet-optimised full-screen temperature logging view.
        ├── ChecklistView.vue       DAILY / WEEKLY / MONTHLY tab filter. Checklist cards with
        │                           individual item toggle (optimistic update). Shift attribution.
        ├── DeviationsView.vue      Deviation list with severity/status filters. Report form.
        │                           Resolve workflow (modal with resolution text field).
        ├── GraphView.vue           Line chart of all unit readings. WEEK / MONTH toggle.
        │                           Alert markers on chart. Export PDF/JSON stubs.
        ├── TrainingView.vue        Training document library. Employee certification status table.
        ├── alkohol/
        │   ├── AlkoholView.vue         IK-Alkohol module shell with sub-tab navigation.
        │   ├── AlderskontrollTab.vue   Age-check log: record and list age verifications.
        │   ├── AlkoholSjekklisterTab.vue  Alcohol-module checklists.
        │   └── HendelsesloggTab.vue    Alcohol serving incident log.
        └── settings/
            ├── SettingsView.vue    Settings shell: sub-nav (Enheter / Brukere / Organisasjon / Sjekklister).
            │                       ADMIN-only route. Renders child RouterView.
            ├── UnitsTab.vue        Storage unit list. Add/edit/delete units via modal form.
            ├── UsersTab.vue        User list. Add/edit users. Per-user permission toggles.
            ├── OrgTab.vue          Organisation profile and notification preferences.
            └── ChecklistsTab.vue   Checklist template CRUD. Lists all templates with frequency
                                    and item count. Create/edit via modal with dynamic item list.
                                    Calls POST/PUT/DELETE /api/checklists/templates. ADMIN only.
```

---

## 5. Layout Architecture

The application supports two layout modes, toggled at runtime by `useLayoutStore`.

### Desktop Mode (default)

```
┌────────────────────────────────────────────────────────┐
│  AppSidebar (240px)  │  <main class="app-main">        │
│  ─────────────────   │  ──────────────────────────     │
│  IK-System brand     │  RouterView (active view)       │
│  Everest Sushi       │                                 │
│                      │                                 │
│  Oversikt            │                                 │
│  Fryser              │                                 │
│  Kjøleskap           │                                 │
│  Generelt            │                                 │
│  Avvik               │                                 │
│  Temperaturgrafer    │                                 │
│  Opplæring           │                                 │
│                      │                                 │
│  ── Administrasjon ──│                                 │
│  Innstillinger       │                                 │
│  (ADMIN only)        │                                 │
│                      │                                 │
│  ── User footer ─────│                                 │
│  Kari Larsen         │                                 │
│  Administrator       │                                 │
│  Logg ut             │                                 │
│  [collapse toggle]   │                                 │
└──────────────────────┴─────────────────────────────────┘
```

The sidebar collapses to icon-only mode (narrower width) via `layout.isSidebarCollapsed`. When collapsed, item labels are hidden and `title` tooltips provide accessibility. The `app-main` class adds a `sidebar-collapsed` modifier that adjusts its left margin accordingly.

### Tablet Mode (simulator)

```
┌─────────────────────────────────────────────┐
│            (dark simulator background)      │
│  ┌─────────────────────────────────────┐    │
│  │        tablet-device frame          │    │
│  │  ┌───────────────────────────────┐  │    │
│  │  │  <main class="app-main-tablet">│  │    │
│  │  │  RouterView (active view)     │  │    │
│  │  │                               │  │    │
│  │  │                               │  │    │
│  │  └───────────────────────────────┘  │    │
│  │  ┌───────────────────────────────┐  │    │
│  │  │  AppTabBar (sticky bottom)    │  │    │
│  │  │  Oversikt Fryser Kjøleskap... │  │    │
│  │  └───────────────────────────────┘  │    │
│  └─────────────────────────────────────┘    │
└─────────────────────────────────────────────┘
```

Tablet mode is activated by `TabletToggle`, a development utility always rendered by `App.vue` regardless of auth state. It wraps the entire app in a dark background with a device frame, and replaces the sidebar with a bottom tab bar (`AppTabBar`). This allows developers and stakeholders to preview the tablet experience without a physical device.

The `AppTabBar` uses per-tab CSS custom properties (`--tab-oversikt`, `--tab-fryser`, etc.) to apply distinct accent colours per section, making it easier for kitchen staff to identify their current location at a glance.

---

## 6. Responsive Design Approach

The application does not use media queries for its primary responsive behaviour. Instead, responsive mode is explicitly controlled by `useLayoutStore.isTabletMode`. This was a deliberate decision:

**Why not CSS breakpoints?**
- The app is used on wall-mounted tablets with fixed orientations. A CSS breakpoint approach would trigger layout changes based on viewport width, which could fire unexpectedly if a manager resizes their browser window.
- Explicit mode switching gives the restaurant operator (via system configuration) or the developer (via the toggle button) predictable control over which layout is rendered.
- The tablet device frame serves as a preview environment during development without requiring a physical device or browser devtools resize.

Within individual views, scoped CSS handles minor spacing adjustments for narrower viewports where appropriate. Touch targets in the tablet tab bar are sized to a minimum of 44px height, following accessibility best practices for touch interfaces.

The global CSS custom property `--tabbar-h: 76px` is used to pad the main content area above the tab bar so content is never hidden beneath it.

---

## 7. State Management Architecture

All application state is managed through Pinia stores using the Composition API style (the `() => {}` factory pattern). Stores are thin orchestration layers — they hold state, call services, and expose computed values and actions. Business logic lives in services, not stores.

### Store overview

| Store | State | Purpose |
|---|---|---|
| `useAuthStore` | `user`, `token` | Manages authentication state. Persists to / re-hydrates from `sessionStorage`. Exposes `isAuthenticated` computed. |
| `useLayoutStore` | `isTabletMode`, `isSidebarCollapsed` | Controls shell layout switching. No persistence — resets on page reload. |
| `useUnitsStore` | `units`, `loading`, `error` | CRUD for storage units. `getByType(type)` returns active units of a given type. |
| `useReadingsStore` | `readings`, `loading`, `saving` | Temperature readings scoped to a unit. `addReading()` prepends to list for immediate UI feedback. |
| `useChecklistsStore` | `checklists`, `loading`, `activeFrequency` | Checklists filtered by frequency. `toggleItem()` uses optimistic update with error revert. |
| `useDeviationsStore` | `deviations`, `loading`, `saving` | Deviation lifecycle: report → in-progress → resolve. `openCount()` for sidebar badge. |
| `useDashboardStore` | `stats`, `tasks`, `alerts`, `loading` | Dashboard snapshot data. Fetched on mount, not reactively updated. |
| `useShiftStore` | `workers`, `activeId` | Active shift worker selection. Does not change JWT or permissions — attribution only. Exposes `activeWorkerId` for use in request bodies. |
| `useAlkoholStore` | `stats`, `entries`, `incidents`, `loading` | IK-Alkohol module data. `fetchStats()` is wrapped in try/catch — 403 on STAFF role is silently ignored so the page does not crash. |

### Patterns

**Loading state**: Every store that performs async operations exposes a `loading` ref. Views use this to show skeleton states or disable submit buttons.

**Error state**: Stores that can fail silently (e.g. units fetch) expose an `error` ref with a human-readable Norwegian message.

**Optimistic updates**: `useChecklistsStore.toggleItem()` flips the item immediately in local state, then calls the service. If the service call fails, the flip is reverted. This gives the kitchen worker instant feedback without waiting for a network round-trip.

---

## 8. Service Layer Pattern

All API calls are encapsulated in service modules under `src/services/`. No component or store makes HTTP calls directly. The convention is:

```
Component/View
    → calls Store action
        → store calls Service function
            → service uses api.ts (Axios instance)
                → HTTP request to Spring Boot backend
```

Each service module exports a plain object with async methods. Services import `api` from `api.ts` for real calls and have all real calls commented out next to the mock implementation.

Example pattern (from `units.service.ts`):

```typescript
async getAll(): Promise<Unit[]> {
  await delay()
  return [...mockUnits]
  // Real: return (await api.get<Unit[]>('/units')).data
},
```

This pattern means swapping from mock to real is a one-line change per method — uncomment the real call and remove the mock body.

Services do not import from stores. If a service needs the current user (e.g. to populate `recordedBy`), it reads directly from `sessionStorage` — keeping the dependency direction strictly one-way.

---

## 9. Backend Integration Status

The frontend services are wired to the real Spring Boot backend. The Axios instance in `api.ts` sends requests to `/api`, which Vite proxies to `http://localhost:8080` in development.

**Services fully wired to real endpoints:**
- `auth.service.ts` — `POST /api/auth/login`, `GET /api/auth/me`
- `units.service.ts` — CRUD on `/api/units`
- `readings.service.ts` — `GET/POST /api/units/:unitId/readings`
- `checklists.service.ts` — instances (`GET/PATCH`) and templates (`GET/POST/PUT/DELETE`)
- `deviations.service.ts` — `GET/POST/PATCH /api/deviations`
- `dashboard.service.ts` — `GET /api/dashboard/summary` and `/api/dashboard/notifications`
- `alkohol.service.ts` — age verifications, incidents (with graceful 403 handling)

**Services with stub implementations (feature not fully implemented):**
- `reports.service.ts` — chart data wired; PDF/JSON export stubs (endpoints exist but file download not implemented)
- `documents.service.ts` — document list and certifications (read only; upload not implemented)
- `organization.service.ts` — org settings and user management

**Seed data**: The backend `dev` Spring profile runs Flyway migrations in `db/migration-dev/` which seed the demo organisation, users, units, checklist templates, temperature readings, and deviations needed for the frontend to display real data.

---

## 10. Authentication Architecture

See `02-auth-permissions.md` for the full auth and permissions reference. Summary of the technical pieces:

**`src/services/api.ts`** — Axios instance with two interceptors:
- **Request**: Reads `token` from `sessionStorage` and adds `Authorization: Bearer {token}` to every outgoing request.
- **Response (error)**: If a 401 is received, clears `sessionStorage` (token + user) and performs a hard redirect to `/login` via `window.location.href`.

**`src/stores/auth.ts`** — Pinia store:
- Initialises `token` ref from `sessionStorage.getItem('token')` at store creation time (re-hydration).
- Re-hydrates `user` ref from `sessionStorage.getItem('user')` (JSON-parsed) at store creation time.
- `isAuthenticated` is a computed that requires both `token` and `user` to be non-null.
- `login()` calls `authService.login()`, then stores both token and user to Pinia state and `sessionStorage`.
- `logout()` nulls Pinia state and removes both `sessionStorage` keys.

**`src/router/index.ts`** — `beforeEach` guard:
- Any route without `meta.requiresAuth: false` requires authentication. Unauthenticated access redirects to `{ name: 'login' }`.
- Authenticated users visiting `/login` are redirected to `{ name: 'dashboard' }`.
- Routes with `meta.requiresAdmin: true` require `user.role === 'ADMIN'`; others are redirected to `{ name: 'dashboard' }`.

**sessionStorage vs localStorage**: `sessionStorage` was chosen because assignment specification section 4.7 calls for short-lived sessions. Tokens do not persist across browser sessions (new tab or browser restart requires re-login).

---

## 11. Module System

The application is designed to serve both IK-Mat (food safety) and IK-Alkohol (alcohol service) compliance requirements under one roof. Every domain type that is module-specific carries a `moduleType: ModuleType` field (`'IK_MAT' | 'IK_ALKOHOL'`).

Currently affected types: `Checklist`, `Deviation`.

The organisation settings (`Organization.modules`) control which modules are active for a given restaurant:

```typescript
modules: {
  ikMat: boolean
  ikAlkohol: boolean
}
```

In the current implementation, both modules are active for the mock organisation. Once the backend is integrated, module availability should gate which checklist categories and deviation types are visible.

Checklist mock data includes both IK-Mat checklists (kitchen opening, cleaning, HACCP tasks) and IK-Alkohol checklists (age-check logs, beverage storage), demonstrating that the same `ChecklistView` handles both domains by filtering on `moduleType`.

---

## 12. Multi-Tenant Considerations

The `User` type carries an `organizationId?: number` field. This is intentionally optional in the current frontend type definition — the backend will make it required once multi-tenant scoping is implemented.

The `authService.me()` endpoint (currently mocked) is intended to return the full user object including `organizationId` from the session. All subsequent API calls will be scoped to the organisation at the backend level — the frontend does not need to append `organizationId` to every request, as it will be derived from the JWT claims on the backend.

Organisation-specific settings (name, org number, address, modules, notifications) are managed through the `/innstillinger/org` tab, backed by `organizationService.getOrg()` and `organizationService.updateOrg()`.

---

## 13. Key Architectural Decisions

### No icon library dependency

All icons in `AppSidebar` and `AppTabBar` are inline SVG paths defined in local component `<script lang="ts">` blocks as minimal helper components (`SidebarIcon`, `TabIcon`). This avoids adding an icon library dependency (Hero Icons, Lucide, Font Awesome) which would either bundle all unused icons or require tree-shaking configuration. The set of icons used is small and stable.

### Single types file

All TypeScript interfaces and types live in `src/types/index.ts`. The alternative — co-locating types with their feature module — was rejected because the types form a shared contract between stores, services, and views. A single file makes it easy to see the full data model and ensures types are not accidentally duplicated.

### Services do not import stores

The dependency direction is: Views → Stores → Services. Services never import from stores. When a service needs contextual data (current user's name for `recordedBy`), it reads from `sessionStorage` directly. This prevents circular dependencies and keeps services independently testable.

### Axios over fetch

`fetch` is available natively but does not support interceptors. The JWT injection and 401 auto-logout patterns are cleanest as Axios interceptors. Writing equivalent middleware for `fetch` would add complexity with no benefit at this project scale.

### Vite proxy for API

`vite.config.ts` proxies `/api` to `http://localhost:8080` during development. This means the frontend code uses relative `/api` paths throughout and never has CORS issues in development. The production deployment should configure the same proxy at the web server (nginx/Apache) level, keeping all service code unchanged.

### sessionStorage re-hydration at store init

The `useAuthStore` re-hydrates from `sessionStorage` at the moment Pinia creates the store (top-level statements in the factory function), not inside `onMounted` or a setup hook. This ensures `isAuthenticated` is correct on the very first router guard evaluation, preventing a flash-redirect to `/login` on page reload.

### Optimistic checklist toggles

Checklist item toggling uses an optimistic update strategy: the store flips the item state immediately, then calls the service in the background. On error, the flip is reverted. This is appropriate for checklist items because the operation is low-risk (a brief incorrect visual state is acceptable) and the improvement to perceived performance on a tablet with variable network is significant.

### ApiResponse envelope unwrapping

The Spring Boot backend wraps all responses in an `ApiResponse<T>` envelope:

```json
{ "success": true, "message": "...", "data": { ... } }
```

The Axios response interceptor in `api.ts` detects this shape and replaces `response.data` with the inner `data` field before returning to the caller. This means service methods can simply write `res.data` and receive the unwrapped payload — the envelope handling is invisible above the service layer.

### Shift worker attribution

Any logged-in user can select a different worker from the shift selector in `FreezerView`, `FridgeView`, and `ChecklistView`. This is an *attribution* feature — it records which worker performed an action, but it does not change the JWT, the session, or any permissions. The backend honors the `performedByUserId` field only when the JWT caller is ADMIN or MANAGER; STAFF submissions are always attributed to the JWT holder.

This design allows a supervisor to log readings on behalf of a kitchen worker who does not have access to the system, without sharing session credentials.

### Alcohol module graceful degradation

The IK-Alkohol module endpoints (`GET /api/alcohol/age-verifications`, `GET /api/alcohol/incidents`) are restricted to ADMIN and MANAGER roles. The STAFF role receives a 403 from these endpoints.

`useAlkoholStore.fetchStats()` is wrapped in a try/catch. Each individual API call within `alkoholService.getStats()` uses `.catch(() => [])` so a 403 on one endpoint does not fail the others. This prevents the IK-Alkohol page from crashing for STAFF users while still showing whatever data is accessible.
