# Feature: Dashboard

## Overview

| Property  | Value                                  |
|-----------|----------------------------------------|
| Route     | `/dashboard`                           |
| Component | `src/views/DashboardView.vue`          |
| Store     | `useDashboardStore` (`src/stores/dashboard.ts`) |
| Service   | `src/services/dashboard.service.ts`    |
| Access    | All authenticated users                |

The dashboard is the application's entry point after login. It gives every authenticated user an at-a-glance operational overview for the current day: how many tasks are done, whether any storage units have temperature alerts, how many deviations are open, and the organisation's compliance score. It is intentionally read-only — no data is entered here. The page is the canonical answer to the question "is anything wrong right now?"

---

## What the Dashboard Shows

### Stat Cards (metric grid)

Four cards are rendered in a 2×2 (desktop) or stacked (mobile) grid. Each card has a label, a primary numeric value, and a contextual sub-line.

| Card label    | Field(s) displayed                     | Color logic                                                      |
|---------------|----------------------------------------|------------------------------------------------------------------|
| Oppgaver i dag | `tasksCompleted / tasksTotal`         | Green when all completed, warning-yellow if any remain           |
| Temp-varsler  | `tempAlerts`                           | Red when > 0, green when 0                                       |
| Åpne avvik    | `openDeviations`                       | Warning-yellow when > 0, muted when 0                            |
| Samsvar       | `compliancePercent` + progress bar     | Always rendered in green; progress fill is green ≥ 90 %, yellow ≥ 60 %, red < 60 % |

### Today's Tasks (Dagens oppgaver)

A list of `DashboardTask` items. Each row shows a small checkbox-style indicator, the task name, and a status badge on the right. Completed tasks are rendered with a strikethrough name, a green "Fullført" badge, and the completing user's name and time. Pending tasks show a yellow "Venter" badge. Not-started tasks show muted "Ikke startet" text.

A special heuristic marks any `PENDING` task whose name contains "alderskontroll" with the `.is-alert` CSS class, giving it an amber left-border highlight.

### Alerts (Varsler)

A card that is only rendered when `alerts.length > 0`. Each alert is rendered using the reusable `AppAlert` component, which styles itself according to the `type` field (`danger`, `warning`, or `info`).

### Active Modules (Moduler aktive)

A static card showing which compliance modules the organisation has enabled. Currently always shows both IK-Mat and IK-Alkohol. Future work: drive this from the organisation profile.

---

## Type Definitions

All types live in `src/types/index.ts`.

### `DashboardStats`

```typescript
interface DashboardStats {
  tasksCompleted:   number   // tasks finished today
  tasksTotal:       number   // total tasks scheduled for today
  tempAlerts:       number   // storage units with out-of-range readings
  openDeviations:   number   // deviations with status OPEN or IN_PROGRESS
  compliancePercent: number  // rolling compliance score, 0–100
}
```

### `DashboardTask`

```typescript
interface DashboardTask {
  id:           number
  name:         string
  status:       'COMPLETED' | 'PENDING' | 'NOT_STARTED'
  completedBy?: string   // display name of the user who completed it
  completedAt?: string   // ISO timestamp or HH:MM string
}
```

`completedBy` and `completedAt` are only present when `status === 'COMPLETED'`. The component formats `completedAt` to HH:MM for display.

### `DashboardAlert`

```typescript
interface DashboardAlert {
  id:      number
  message: string
  type:    'danger' | 'warning' | 'info'
  time:    string  // HH:MM — when the alert was raised
}
```

`type` maps directly to the variant prop accepted by `AppAlert.vue`.

---

## Mock Data

The service (`dashboard.service.ts`) currently returns hardcoded mock data with a 400 ms artificial delay. The mock represents a realistic mid-morning state for a restaurant kitchen.

**Stats:**

| Field             | Mock value |
|-------------------|------------|
| tasksCompleted    | 7          |
| tasksTotal        | 12         |
| tempAlerts        | 2          |
| openDeviations    | 3          |
| compliancePercent | 87         |

**Tasks (5 items):**

| id | Name                                  | Status        | completedBy   | completedAt |
|----|---------------------------------------|---------------|---------------|-------------|
| 1  | Morgen kjøkkenrenhold                 | COMPLETED     | Ola N.        | 07:45       |
| 2  | Temperatursjekk kjøleskap             | COMPLETED     | Kari L.       | 08:15       |
| 3  | Logg varemottak-temperaturer          | PENDING       | —             | —           |
| 4  | Alderskontroll-logg (IK-Alkohol)      | NOT_STARTED   | —             | —           |
| 5  | Kveldsstenging sjekkliste             | NOT_STARTED   | —             | —           |

Task 4 triggers the `.is-alert` heuristic because its name contains "alderskontroll" and its status is not `COMPLETED`.

**Alerts (2 items):**

| id | Message                                            | type    | time  |
|----|----------------------------------------------------|---------|-------|
| 1  | Fryser #2 over grenseverdi — 15 min siden          | danger  | 08:10 |
| 2  | Ukentlig renholdssjekkliste forfalt — 2t siden     | warning | 06:00 |

---

## Store: `useDashboardStore`

Defined in `src/stores/dashboard.ts` as a Pinia composition store.

**State:**

| Ref       | Type                      | Initial value |
|-----------|---------------------------|---------------|
| `stats`   | `DashboardStats \| null`  | `null`        |
| `tasks`   | `DashboardTask[]`         | `[]`          |
| `alerts`  | `DashboardAlert[]`        | `[]`          |
| `loading` | `boolean`                 | `false`       |

**Actions:**

`fetchDashboard()` — Sets `loading = true`, calls `dashboardService.get()`, writes the returned `stats`, `tasks`, and `alerts` into state, then sets `loading = false` in the `finally` block. No error state is managed yet; errors will leave the previous state in place.

---

## Service: `dashboard.service.ts`

Single exported object `dashboardService` with one method:

```typescript
dashboardService.get(): Promise<{
  stats:  DashboardStats
  tasks:  DashboardTask[]
  alerts: DashboardAlert[]
}>
```

**Current implementation:** Returns mock data after a 400 ms delay.

**Real API endpoints (not yet wired):**

| Method | Endpoint                        | Purpose                         |
|--------|---------------------------------|---------------------------------|
| GET    | `/api/dashboard/summary`        | Stats, today's tasks            |
| GET    | `/api/dashboard/notifications`  | Active alerts / notifications   |

When the real backend is integrated, the single `get()` call should be split into two parallel requests (one for summary, one for notifications) or the backend should provide a combined endpoint.

---

## Data Flow

```
DashboardView.vue
  └─ onMounted()
       └─ useDashboardStore.fetchDashboard()
            └─ dashboardService.get()          ← mock delay / future: HTTP
                 └─ returns { stats, tasks, alerts }
            ← store writes stats, tasks, alerts
            ← loading = false
  └─ template re-renders with reactive store state
```

The component uses `computed(() => dashboardStore.stats)` etc. to derive display values. Color classes for each metric card are also computed properties that react to the stat values.

**Loading state:** While `loading` is `true`, the component renders skeleton cards (shimmer animation) in place of the metric grid and task list, using CSS `@keyframes shimmer`. This prevents layout shift.

---

## What Still Needs Implementation

| Gap                              | Detail                                                                                                                                 |
|----------------------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| Real API wiring                  | `dashboard.service.ts` is fully mocked. Both endpoints need to be connected once the backend is ready.                                 |
| Error handling in the store      | `fetchDashboard()` has no `catch` block. A network failure leaves the UI in an empty/stale state with no user feedback.                |
| Real-time notifications          | Alerts are fetched once on mount. There is no WebSocket or polling mechanism to push new temperature alerts to the dashboard in real time. |
| Task list interactivity          | Tasks are read-only. Completing a task from the dashboard (rather than from the checklist view) is not yet supported.                   |
| Module visibility from org profile | The "Moduler aktive" section is hardcoded. It should read the organisation's `modules.ikMat` and `modules.ikAlkohol` flags.          |
| Compliance calculation           | `compliancePercent` is a backend-supplied number. The calculation logic (what counts toward compliance) is not yet defined.             |
| Refresh interval                 | Dashboard data is fetched once on mount. A periodic refresh or manual refresh button is not implemented.                               |
