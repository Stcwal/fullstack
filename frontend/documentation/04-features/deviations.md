# Deviations (Avvik)

Feature for reporting and tracking non-conformances (avvik) across IK-Mat and IK-Alkohol modules.

---

## Route and component

| Property  | Value                  |
|-----------|------------------------|
| Route     | `/avvik`               |
| Name      | `avvik`                |
| Component | `DeviationsView.vue`   |
| Meta      | `requiresAuth: true`   |

The route requires authentication but has no role restriction — any logged-in user can reach `/avvik` and submit a new deviation. Status transitions (OPEN → IN_PROGRESS → RESOLVED) are restricted to ADMIN and MANAGER in the intended data model, though this guard is not yet enforced in the frontend.

---

## State management

**Store:** `useDeviationsStore` (`src/stores/deviations.ts`)
**Service:** `deviationsService` (`src/services/deviations.service.ts`)

### Store shape

```ts
deviations: Deviation[]   // full list, newest first
loading: boolean          // true while fetchAll() is in flight
saving: boolean           // true while report() is in flight
```

### Store actions

| Action                          | Description                                                         |
|---------------------------------|---------------------------------------------------------------------|
| `fetchAll()`                    | Loads all deviations from the service; sets `loading` around call  |
| `report(data: NewDeviation)`    | Creates a new deviation; prepends it to the list; sets `saving`     |
| `resolve(id, resolution)`       | Marks a deviation RESOLVED and sets `resolvedAt` / `resolution`    |

### Derived helper

`openCount()` — returns the number of deviations whose status is not `RESOLVED`. Used by the dashboard to display the open-deviations counter.

---

## Data models

### `Deviation`

Defined in `src/types/index.ts`.

```ts
interface Deviation {
  id: number
  title: string
  description: string
  status: DeviationStatus          // 'OPEN' | 'IN_PROGRESS' | 'RESOLVED'
  severity: DeviationSeverity      // 'CRITICAL' | 'MEDIUM' | 'LOW'
  reportedBy: string               // display name, e.g. "Kari Larsen"
  reportedAt: string               // ISO 8601 timestamp
  moduleType: ModuleType           // 'IK_MAT' | 'IK_ALKOHOL'
  resolvedAt?: string              // ISO 8601 timestamp, set when RESOLVED
  resolution?: string              // free-text description of resolution
}
```

### `NewDeviation`

Submitted by the user via the report modal. The service layer fills in `id`, `status`, `reportedBy`, and `reportedAt`.

```ts
interface NewDeviation {
  title: string
  description: string
  severity: DeviationSeverity
  moduleType: ModuleType
}
```

### Type aliases

```ts
type DeviationStatus   = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED'
type DeviationSeverity = 'CRITICAL' | 'MEDIUM' | 'LOW'
type ModuleType        = 'IK_MAT' | 'IK_ALKOHOL'
```

---

## Status flow

```
OPEN  →  IN_PROGRESS  →  RESOLVED
```

- **OPEN** — deviation has been reported and has not been assigned to anyone yet.
- **IN_PROGRESS** — someone is actively working on the resolution. Status badge: "Under arbeid".
- **RESOLVED** — the issue has been closed. A `resolution` text and `resolvedAt` timestamp are recorded.

Backwards transitions (e.g. reopening a resolved deviation) are not modelled in the current frontend. The intended permission rule is that only ADMIN or MANAGER can move a deviation out of OPEN.

---

## Severity levels

| Value      | Norwegian label | Badge class     | Notes                                       |
|------------|-----------------|-----------------|---------------------------------------------|
| `CRITICAL` | Kritisk         | `badge-danger`  | Highest severity; shown in red              |
| `MEDIUM`   | Medium          | `badge-warning` | Moderate impact                             |
| `LOW`      | Lav             | `badge-neutral` | Minor issue; informational                  |
| `HIGH`     | —               | —               | **Defined in backend spec; missing in frontend.** The `DeviationSeverity` type does not include `HIGH`. The report modal dropdown also omits it. |

The `HIGH` severity gap means the frontend cannot display or report HIGH-severity deviations. When the backend returns a deviation with `severity: 'HIGH'`, the badge rendering falls through to the default (unstyled) case in the template.

---

## Module types

| Value        | Norwegian label | Badge class  |
|--------------|-----------------|--------------|
| `IK_MAT`     | IK-Mat          | `ik-mat`     |
| `IK_ALKOHOL` | IK-Alkohol      | `ik-alkohol` |

Module badges use the `.mod-badge` CSS class with a `.ik-mat` or `.ik-alkohol` colour modifier, matching the style used across the rest of the application.

---

## Mock data

Defined in `src/services/deviations.service.ts`. Three seeded deviations cover the three possible statuses and represent realistic restaurant scenarios.

### Deviation 1 — OPEN, CRITICAL, IK-Mat

```
id:          1
title:       "Fryser #2 over grenseverdi"
description: "Målt -12.1°C, grenseverdi er -18°C. Mulig kompressorfeil."
status:      OPEN
severity:    CRITICAL
reportedBy:  "Kari Larsen"
reportedAt:  today at 08:12
moduleType:  IK_MAT
```

Note: the task brief calls this "Fryser #2 temperaturavvik" but the actual service title is "Fryser #2 over grenseverdi". This deviation is linked to the same temperature spike visible in the graphs feature (Fryser #2, -12.1°C on the final day of the week dataset).

### Deviation 2 — IN_PROGRESS, MEDIUM, IK-Alkohol

```
id:          2
title:       "Manglende alderskontroll-logg"
description: "Barpersonalet fullførte ikke alderskontrollsjekkliste for kveldsvakt."
status:      IN_PROGRESS
severity:    MEDIUM
reportedBy:  "Per Martinsen"
reportedAt:  yesterday at 22:00
moduleType:  IK_ALKOHOL
```

### Deviation 3 — RESOLVED, LOW, IK-Mat

```
id:          3
title:       "Utgått soyasaus på lager"
description: "Vare kastet, leverandør varslet. FIFO-rutine gjennomgått med personalet."
status:      RESOLVED
severity:    LOW
reportedBy:  "Ola Nordmann"
reportedAt:  3 days ago at 14:30
moduleType:  IK_MAT
resolvedAt:  2 days ago at 09:00
resolution:  "Vare kastet, leverandør varslet. FIFO gjennomgått."
```

New deviations submitted during a session are prepended to the list and are assigned IDs starting from 10 (incremented by `nextId`).

---

## UI

### Page layout

The page renders a header row with the title "Avvik" / subtitle "Rapporter og følg opp avvik" on the left, and a "Rapporter nytt avvik" primary button on the right.

Below the header, the deviation list is rendered directly (no card wrapper around the whole list — each deviation is its own card).

### Deviation card structure

Each `Deviation` is rendered as a `.card` containing:

1. **Card header** — title (truncated) on the left; status badge on the right.
2. **Meta line** — "Rapportert: {fmtDate} av {reportedBy}" in muted small text.
3. **Description** — full description text.
4. **Footer row** — module badge + severity badge side by side.

### Status badges

| Status        | Badge class     | Norwegian label |
|---------------|-----------------|-----------------|
| `OPEN`        | `badge-danger`  | Åpen            |
| `IN_PROGRESS` | `badge-warning` | Under arbeid    |
| `RESOLVED`    | `badge-success` | Løst            |

### Severity badges

| Severity   | Badge class     | Norwegian label |
|------------|-----------------|-----------------|
| `CRITICAL` | `badge-danger`  | Kritisk         |
| `MEDIUM`   | `badge-warning` | Medium          |
| `LOW`      | `badge-neutral` | Lav             |

### Date formatting

The `fmtDate(iso)` helper in the component applies contextual relative labels:

- Today → "I dag HH:MM"
- Yesterday → "I går HH:MM"
- Older → "{day}. {month abbreviation}" (e.g. "27. mar")

### Loading and empty states

- **Loading:** "Laster avvik…" shown while `deviationsStore.loading` is true.
- **Empty:** A centred card with "Ingen avvik registrert" and a hint to click the report button.

### "Rapporter nytt avvik" modal

Triggered by the primary button in the page header. Uses the shared `AppModal` component.

Form fields:

| Field              | Type     | Default    | Required | Validation                     |
|--------------------|----------|------------|----------|--------------------------------|
| Tittel             | text     | ""         | Yes      | Must not be blank              |
| Beskrivelse        | textarea | ""         | Yes      | Must not be blank              |
| Alvorlighetsgrad   | select   | MEDIUM     | Yes      | Options: Kritisk, Medium, Lav  |
| Modul              | select   | IK_MAT     | Yes      | Options: IK-Mat, IK-Alkohol    |

The "Rapporter avvik" submit button is disabled when `saving` is true or either required text field is blank. On submit, `deviationsStore.report()` is called and the modal closes on resolution.

The `reportedBy` field is not in the form — the service reads the current user from `sessionStorage.getItem('user')` and constructs `"${firstName} ${lastName}"`.

### Status change (ADMIN/MANAGER)

The `deviationsStore.resolve()` action and `deviationsService.resolve()` are implemented, but there is no UI for triggering a status change in the current component. The deviation cards do not show action buttons for changing status. This is a known gap — status management UI is yet to be built.

---

## API endpoints

These are the planned real API endpoints. All current calls use mock data in the service layer.

| Method | Endpoint                         | Description                                   |
|--------|----------------------------------|-----------------------------------------------|
| GET    | `/api/deviations`                | Fetch all deviations for the organisation     |
| POST   | `/api/deviations`                | Create a new deviation (`NewDeviation` body)  |
| PATCH  | `/api/deviations/:id/status`     | Update deviation status (ADMIN/MANAGER only)  |

The service currently uses an internal `resolve()` method that would map to a `PATCH /api/deviations/:id/resolve` or `PATCH /api/deviations/:id/status` call — the exact endpoint path should be confirmed with the backend spec.

---

## Automatic deviations

The backend has the ability to auto-create deviations when a temperature reading is recorded outside the unit's `minTemp`–`maxTemp` range. When this happens:

- A `Deviation` is created server-side with `status: OPEN`, `severity` derived from how far out of range the reading is, and `moduleType: IK_MAT`.
- The frontend will pick it up on the next `fetchAll()` call.
- The `reportedBy` field in this case would be set to a system user or the name of the staff member who recorded the reading, depending on backend implementation.

These auto-created deviations appear in the list identically to user-reported ones. There is currently no visual indicator distinguishing auto-created deviations from manually reported ones.

---

## Known gaps vs backend spec

| Gap                        | Frontend behaviour                                          | Backend spec                                                              |
|----------------------------|-------------------------------------------------------------|---------------------------------------------------------------------------|
| `HIGH` severity missing    | `DeviationSeverity` type only has CRITICAL, MEDIUM, LOW. Report form has no HIGH option. Badge rendering has no case for HIGH. | Backend spec includes HIGH as a valid severity level. |
| `relatedReadingId` missing | Not present on the `Deviation` interface or anywhere in the service mock. | Backend may link a deviation to the temperature reading that triggered it via a `relatedReadingId` field. |
| `comments` array missing   | No comments field on `Deviation`. No comments UI.           | Backend spec may include a `comments` array for follow-up notes on a deviation. |
| `reportedBy` as string     | `reportedBy` is stored and displayed as a plain string (e.g. "Kari Larsen"). | Backend likely returns `reportedBy` as an object `{ id: number, name: string }` or a user ID reference. The frontend would need to be updated to handle an object shape. |
| Status change UI missing   | `deviationsStore.resolve()` is implemented but there are no status-change buttons on deviation cards. ADMIN/MANAGER cannot change status through the UI. | Backend spec requires PATCH /api/deviations/:id/status restricted to ADMIN/MANAGER. |
