# State Management

The application uses [Pinia](https://pinia.vuejs.org/) as its state management library.

---

## Why Pinia

- **Vue 3 official**: Pinia is the officially recommended store for Vue 3, superseding Vuex. It is maintained by the Vue core team.
- **Composition API native**: All stores in this project use the setup-function syntax (`defineStore('id', () => { ... })`), which mirrors the Composition API and feels like writing a plain composable.
- **TypeScript-friendly**: No extra type gymnastics are needed. State refs, computed properties, and actions are all typed automatically from their definitions.
- **Devtools support**: Pinia integrates with Vue Devtools for time-travel debugging, state inspection, and action tracking.
- **No mutations**: Unlike Vuex, state can be mutated directly inside actions. There is no concept of mutations or commit calls.

---

## Store Overview

| Store | File | What it manages |
|---|---|---|
| `useAuthStore` | `stores/auth.ts` | Authenticated user identity, JWT token, login/logout |
| `useLayoutStore` | `stores/layout.ts` | UI shell mode: tablet simulator toggle, sidebar collapsed state |
| `useDashboardStore` | `stores/dashboard.ts` | Overview stats, today's task summaries, active alerts |
| `useUnitsStore` | `stores/units.ts` | Storage unit configuration (freezers, fridges, coolers) |
| `useReadingsStore` | `stores/readings.ts` | Temperature readings per unit |
| `useDeviationsStore` | `stores/deviations.ts` | Non-conformance reports and their resolution status |
| `useChecklistsStore` | `stores/checklists.ts` | Checklist templates and per-item completion state |

---

## Store Details

---

### `useAuthStore`

**File:** `src/stores/auth.ts`

Manages the authenticated session. It is the only store that persists data to `sessionStorage`.

#### State

| Field | Type | Description |
|---|---|---|
| `user` | `User \| null` | The authenticated user object |
| `token` | `string \| null` | JWT bearer token; initialized from `sessionStorage.getItem('token')` on store creation |

`User` shape (from `src/types/index.ts`):

```ts
interface User {
  id: number
  firstName: string
  lastName: string
  email: string
  role: UserRole                    // 'ADMIN' | 'MANAGER' | 'STAFF'
  organizationId?: number
  permissions?: UserPermissions
}
```

#### Computed

| Property | Type | Logic |
|---|---|---|
| `isAuthenticated` | `boolean` | `!!token.value && !!user.value` — both must be present |

#### Re-hydration on page reload

When the store module is first imported (i.e., when the app boots), the following runs synchronously before any component mounts:

```ts
const stored = sessionStorage.getItem('user')
if (stored) {
  try { user.value = JSON.parse(stored) } catch { /* noop */ }
}
```

Combined with `token.value` being initialized from `sessionStorage` in the `ref()` call, this means a user who refreshes the page is immediately re-authenticated without a round-trip to the server. `isAuthenticated` will be `true` before the first render if valid session data exists.

If the stored JSON is malformed, the parse error is silently swallowed and `user` remains `null`, which causes `isAuthenticated` to be `false` and the router guard to redirect to `/login`.

#### Actions

**`login(email: string, password: string): Promise<void>`**
- Calls `authService.login({ email, password })`
- On success: sets `token`, sets `user`, writes both to `sessionStorage`
- Throws on failure (HTTP error propagates from the service layer; the login view catches and displays it)

**`logout(): void`**
- Sets `user` and `token` to `null`
- Removes both keys from `sessionStorage`
- Synchronous — no API call

#### Persistence

`sessionStorage` is used (not `localStorage`). The session ends when the browser tab is closed, which is appropriate for a shared kitchen device.

---

### `useLayoutStore`

**File:** `src/stores/layout.ts`

Pure UI state. No API calls, no persistence, no error handling. Resets to defaults on every page load.

#### State

| Field | Type | Default | Description |
|---|---|---|---|
| `isTabletMode` | `boolean` | `false` | When `true`, the app renders inside the iPad simulator frame |
| `isSidebarCollapsed` | `boolean` | `false` | When `true`, the desktop sidebar collapses to 64px icon-only mode |

#### Actions

**`toggleTabletMode(): void`** — flips `isTabletMode`.

**`toggleSidebar(): void`** — flips `isSidebarCollapsed`.

Both are consumed by `AppShell.vue`. The sidebar state drives a CSS class on `.app-main` and on `.sidebar`. The tablet mode state determines which shell template branch renders.

---

### `useDashboardStore`

**File:** `src/stores/dashboard.ts`

Aggregates cross-domain summary data for the Overview page. All data is fetched together in a single service call.

#### State

| Field | Type | Description |
|---|---|---|
| `stats` | `DashboardStats \| null` | Numeric KPIs shown in the metric grid |
| `tasks` | `DashboardTask[]` | Today's task list with completion status |
| `alerts` | `DashboardAlert[]` | Active alerts shown in the alert banner list |
| `loading` | `boolean` | `true` while the fetch is in flight |

`DashboardStats` shape:

```ts
interface DashboardStats {
  tasksCompleted: number
  tasksTotal: number
  tempAlerts: number
  openDeviations: number
  compliancePercent: number
}
```

`DashboardTask` shape:

```ts
interface DashboardTask {
  id: number
  name: string
  status: 'COMPLETED' | 'PENDING' | 'NOT_STARTED'
  completedBy?: string
  completedAt?: string
}
```

`DashboardAlert` shape:

```ts
interface DashboardAlert {
  id: number
  message: string
  type: 'danger' | 'warning' | 'info'
  time: string
}
```

#### Actions

**`fetchDashboard(): Promise<void>`**
- Sets `loading = true`
- Calls `dashboardService.get(locationStore.activeLocationId)` — passes the active location so the backend scopes stats to that location (null = all locations for ADMIN)
- Destructures the response into `stats`, `tasks`, and `alerts`
- Sets `loading = false` in `finally` (always runs even on error)

The store watches `locationStore.activeLocationId` and re-calls `fetchDashboard()` automatically when the location changes.

`fetchDashboard()` is also called from `useReadingsStore.addReading()` whenever the new reading has `isDeviation === true`, keeping the open-deviation count on the dashboard current without a manual refresh.

#### Error handling

There is no `error` field in this store. If `fetchDashboard()` throws, `loading` is reset to `false` and the exception propagates to the caller. The Overview view handles this with a try/catch in its `onMounted` hook.

---

### `useUnitsStore`

**File:** `src/stores/units.ts`

Manages the full list of storage units (freezers, fridges, coolers). Units are the central configuration entity — they are referenced by readings, alerts, and dashboard stats.

#### State

| Field | Type | Description |
|---|---|---|
| `units` | `Unit[]` | All units for the organization |
| `loading` | `boolean` | `true` during `fetchUnits()` |
| `error` | `string \| null` | Norwegian error message if fetch fails |

`Unit` shape:

```ts
interface Unit {
  id: number
  name: string
  type: UnitType              // 'FREEZER' | 'FRIDGE' | 'COOLER' | 'OTHER'
  targetTemp: number          // ideal temperature in °C
  minTemp: number             // lower alarm threshold
  maxTemp: number             // upper alarm threshold
  contents: string            // free-text description of stored goods
  active: boolean
  hasAlert?: boolean          // true if latest reading is out of range
}
```

#### Actions

**`fetchUnits(): Promise<void>`**
- Clears `error`, sets `loading = true`
- Calls `unitsService.getAll()`
- On catch: sets `error = 'Kunne ikke laste enheter'`
- Resets `loading` in `finally`

**`createUnit(data: Omit<Unit, 'id'>): Promise<Unit>`**
- Calls `unitsService.create(data)`
- Pushes the returned unit onto `units` (no re-fetch needed)
- Returns the created unit

**`updateUnit(id: number, data: Partial<Unit>): Promise<Unit>`**
- Calls `unitsService.update(id, data)`
- Finds the matching entry in `units` by index and replaces it with the returned value
- Returns the updated unit

**`deleteUnit(id: number): Promise<void>`**
- Calls `unitsService.remove(id)`
- Filters `units` to remove the entry with the matching `id`

**`getByType(type: UnitType): Unit[]`** (synchronous)
- Returns `units` filtered by `type` and `active === true`
- Used by temperature logging views to populate unit selector dropdowns

#### Error handling

Only `fetchUnits` sets the `error` string. Mutation actions (`createUnit`, `updateUnit`, `deleteUnit`) do not catch errors internally — they let exceptions bubble to the calling component, which shows its own modal-level error feedback.

---

### `useReadingsStore`

**File:** `src/stores/readings.ts`

Manages temperature readings. Readings are fetched per unit and stored in a single flat array that can be filtered client-side.

#### State

| Field | Type | Description |
|---|---|---|
| `readings` | `TemperatureReading[]` | All readings currently loaded |
| `loading` | `boolean` | `true` during `fetchByUnit()` |
| `saving` | `boolean` | `true` during `addReading()` — separate flag allows the submit button to show a spinner without affecting the list |
| `error` | `string \| null` | Norwegian error message if fetch fails |

`TemperatureReading` shape:

```ts
interface TemperatureReading {
  id: number
  unitId: number
  temperature: number
  recordedAt: string          // ISO 8601 datetime string
  recordedBy: string          // display name of the staff member
  note?: string
  isOutOfRange: boolean       // true if temperature is outside unit min/max
}
```

`NewReading` (input to `addReading`):

```ts
interface NewReading {
  unitId: number
  temperature: number
  recordedAt: string
  note?: string
}
```

#### Actions

**`fetchByUnit(unitId: number): Promise<void>`**
- Clears `error`, sets `loading = true`
- Calls `readingsService.getByUnit(unitId)`
- Merges the response into `readings` by replacing only the slice for that unit (`readings.filter(r => r.unitId !== unitId)` + fetched) — concurrent fetches for different units cannot clobber each other
- On catch: sets `error = 'Kunne ikke laste målinger'`

**`addReading(data: NewReading): Promise<TemperatureReading>`**
- Sets `saving = true`
- Calls `readingsService.create(data)`
- Uses `readings.value.unshift(created)` so the new reading appears at the top of the list without a page reload
- If `created.isDeviation === true`, triggers `useDashboardStore().fetchDashboard()` so the dashboard stat and alert list update immediately
- Returns the created reading (used by the caller to possibly open a deviation modal)
- Resets `saving` in `finally`

**`getByUnit(unitId: number): TemperatureReading[]`** (synchronous)
- Filters `readings` by `unitId`
- Used when multiple units' readings have been loaded and a view needs a subset

#### Optimistic update pattern

`addReading` is not strictly optimistic — it waits for the server response before inserting into the list. The `saving` flag provides UI feedback during the wait. If the server call fails, `saving` is reset and the reading is never added, so no rollback is needed. This is a "pessimistic insert with immediate feedback" pattern.

---

### `useDeviationsStore`

**File:** `src/stores/deviations.ts`

Manages non-conformance (avvik) reports. Deviations are central to the compliance workflow: they are reported when something goes wrong (e.g., a temperature breach) and resolved once corrective action is taken.

#### State

| Field | Type | Description |
|---|---|---|
| `deviations` | `Deviation[]` | All deviations for the organization |
| `loading` | `boolean` | `true` during `fetchAll()` |
| `saving` | `boolean` | `true` during `report()` |

`Deviation` shape:

```ts
interface Deviation {
  id: number
  title: string
  description: string
  status: DeviationStatus           // 'OPEN' | 'IN_PROGRESS' | 'RESOLVED'
  severity: DeviationSeverity       // 'CRITICAL' | 'MEDIUM' | 'LOW'
  reportedBy: string
  reportedAt: string                // ISO 8601
  moduleType: ModuleType            // 'IK_MAT' | 'IK_ALKOHOL'
  resolvedAt?: string
  resolution?: string
}
```

`NewDeviation` (input to `report`):

```ts
interface NewDeviation {
  title: string
  description: string
  severity: DeviationSeverity
  moduleType: ModuleType
}
```

#### Actions

**`fetchAll(): Promise<void>`**
- Sets `loading = true`, calls `deviationsService.getAll()`
- Replaces `deviations` with the full list
- Resets `loading` in `finally`; no `error` state (exceptions propagate)

**`report(data: NewDeviation): Promise<Deviation>`**
- Sets `saving = true`
- Calls `deviationsService.create(data)`
- Inserts the result at the front of `deviations` (newest first)
- Returns the created deviation so the caller can display a confirmation
- Resets `saving` in `finally`

**`resolve(id: number, resolution: string): Promise<void>`**
- Calls `deviationsService.resolve(id, resolution)` on the server first
- Then finds the deviation in `deviations` and mutates it in place:
  - `status = 'RESOLVED'`
  - `resolution = resolution`
  - `resolvedAt = new Date().toISOString()`
- This is a **local optimistic update after server confirmation** — the server write happens first, then the local state is updated to avoid a full re-fetch. If the server call throws, the local state remains unchanged.

**`openCount(): number`** (synchronous)
- Returns `deviations.filter(d => d.status !== 'RESOLVED').length`
- Used by the sidebar to show an alert dot when there are unresolved deviations

#### Error handling

`fetchAll` and `report` do not define an `error` ref — exceptions propagate to the calling view. The `resolve` action leaves local state untouched on server failure, which is safe because the user sees a normal error and can retry.

---

### `useChecklistsStore`

**File:** `src/stores/checklists.ts`

Manages checklist templates and their per-item completion state. Supports three frequencies: `DAILY`, `WEEKLY`, and `MONTHLY`.

#### State

| Field | Type | Description |
|---|---|---|
| `checklists` | `Checklist[]` | Checklists for the currently-selected frequency |
| `loading` | `boolean` | `true` during `fetchAll()` |
| `activeFrequency` | `ChecklistFrequency` | The currently displayed frequency tab; defaults to `'DAILY'` |

`Checklist` shape:

```ts
interface Checklist {
  id: number
  title: string
  frequency: ChecklistFrequency
  items: ChecklistItem[]
  completedBy?: string
  completedAt?: string
  moduleType: ModuleType
}

interface ChecklistItem {
  id: number
  text: string
  completed: boolean
  completedBy?: string
  completedAt?: string
}
```

#### Actions

**`fetchAll(frequency: ChecklistFrequency = 'DAILY'): Promise<void>`**
- Sets `loading = true` and `activeFrequency = frequency`
- Calls `checklistsService.getByFrequency(frequency)`
- Replaces `checklists` with the response
- Resets `loading` in `finally`

Called when the user switches frequency tabs. Each switch replaces the full list.

**`toggleItem(checklistId: number, itemId: number): Promise<void>`**
- Finds the checklist and item in local state
- **Optimistic update**: flips `item.completed` immediately before the API call
- Calls `checklistsService.toggleItem(checklistId, itemId, item.completed)`
- On catch: **rolls back** by flipping `item.completed` back to its previous value
- If the checklist or item is not found, returns early with no effect

This is the classic optimistic UI pattern: the user sees instant visual feedback when they tap a checkbox, and the UI silently corrects itself only if the network request fails.

---

## Auth Store Re-hydration

On a hard page reload:

1. The Pinia store module is re-imported fresh — all reactive refs start at their default values.
2. `useAuthStore` initializes `token.value` from `sessionStorage.getItem('token')` inside the `ref()` call.
3. Immediately after, a synchronous block reads `sessionStorage.getItem('user')`, parses it, and sets `user.value`.
4. `isAuthenticated` (a computed) evaluates to `true` because both `token` and `user` are now populated.
5. The router navigation guard runs, finds `isAuthenticated === true`, and allows navigation to the requested route.

The entire re-hydration sequence is synchronous and completes before Vue mounts any component. No loading spinner or intermediate redirect is needed for returning sessions.

---

## Data Flow Patterns

### Fetching data on view mount

```
onMounted() in view component
  → calls store.fetchXxx()
    → store sets loading = true
    → calls service.getXxx()
      → service calls apiClient (axios instance with auth header injected)
        → HTTP GET /api/...
      → service returns typed data
    → store updates reactive state
    → store sets loading = false
  → template re-renders via Vue reactivity
```

### Submitting a form (e.g., logging a temperature)

```
User fills form and taps "Lagre"
  → component calls readingsStore.addReading(formData)
    → store sets saving = true (button shows spinner)
    → calls readingsService.create(formData)
    → server returns created TemperatureReading
    → store unshifts reading into readings array
    → store sets saving = false
  → component inspects returned reading
    → if isOutOfRange: open deviation modal
    → else: reset form, show success message
```

### Optimistic checklist toggle

```
User taps a checklist item checkbox
  → component calls checklistsStore.toggleItem(checklistId, itemId)
    → store finds item, flips item.completed (UI updates instantly)
    → calls checklistsService.toggleItem(...)
      → if success: no further action needed
      → if error: store flips item.completed back (UI reverts)
```

---

## Store Relationships

The stores are largely independent, but they represent related domains that are aggregated in specific views:

```
useUnitsStore ────────────────────────┐
                                       │
useReadingsStore ─── (unitId FK) ──────┤
                                       ▼
                              useDashboardStore
useChecklistsStore ────────────────── (aggregated stats)
                                       │
useDeviationsStore ────────────────────┘
```

- **`useDashboardStore`** does not import or depend on other stores. Instead, `dashboardService.get()` returns a pre-aggregated payload computed by the backend, drawing from readings, checklists, and deviations.
- **`useReadingsStore`** depends on unit IDs from `useUnitsStore`. The temperature logging view fetches units first, then uses `getByType()` to build the unit selector, then calls `fetchByUnit()` with the selected unit ID.
- **`useDeviationsStore.openCount()`** drives the alert dot on the sidebar's Deviations nav item. The sidebar component imports `useDeviationsStore` directly to read this count reactively.
- **`useChecklistsStore`** and **`useUnitsStore`** are independent of each other but both contribute to `DashboardStats.compliancePercent` on the backend.

---

## Testing Considerations

### Auth store

- Mock `authService.login` to return a fixture `AuthResponse`
- Verify `sessionStorage` is written on `login()` and cleared on `logout()`
- Test re-hydration by pre-populating `sessionStorage` before store initialization
- Verify `isAuthenticated` is `false` when only `token` is set (not `user`)

### Layout store

- Trivial: test that `toggleTabletMode` and `toggleSidebar` flip their respective booleans

### Dashboard store

- Mock `dashboardService.get()` — verify `stats`, `tasks`, and `alerts` are populated
- Verify `loading` is `true` during the fetch and `false` after
- Verify `loading` resets to `false` even when the service throws

### Units store

- Test `getByType()` filters by both `type` and `active === true`
- Test `createUnit()` appends to the array without re-fetching
- Test `updateUnit()` replaces the correct index in-place
- Test `deleteUnit()` removes the item by `id`
- Test that `error` is set on `fetchUnits()` failure and cleared on the next call

### Readings store

- Test `addReading()` inserts at position 0 (newest first)
- Test `getByUnit()` returns only readings with the matching `unitId`
- Test `saving` is reset even when `readingsService.create()` throws

### Deviations store

- Test `resolve()` mutates the deviation in local state after the server call succeeds
- Test `resolve()` does not mutate local state if the server call throws
- Test `openCount()` returns 0 when all deviations are `RESOLVED`

### Checklists store

- Test the optimistic toggle: verify `item.completed` flips before the service call
- Test rollback: mock `checklistsService.toggleItem` to throw, verify `item.completed` is restored to its original value
- Test `fetchAll()` sets `activeFrequency` before the async call
