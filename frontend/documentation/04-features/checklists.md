# Feature: Checklists

## Overview

| Property  | Value                                               |
|-----------|-----------------------------------------------------|
| Route     | `/generelt`                                         |
| Component | `src/views/ChecklistView.vue`                       |
| Store     | `useChecklistsStore` (`src/stores/checklists.ts`)   |
| Service   | `src/services/checklists.service.ts`                |
| Access    | All authenticated users (`UserPermissions.checklists`) |
| Modules   | IK-Mat and IK-Alkohol                               |

Checklists are the day-to-day task compliance mechanism. Staff work through lists of procedural items — cleaning steps, equipment checks, age-verification logs — and tick them off as completed. The view is frequency-filtered: a tab bar lets the user switch between daily, weekly, and monthly checklists. Progress is tracked per checklist with a progress bar and a completed/total counter. Every item records who completed it and when.

The route `/generelt` (meaning "general") is the single entry point for all checklist frequencies and both IK-Mat and IK-Alkohol module checklists.

---

## Checklist Types

### Frequency

```typescript
type ChecklistFrequency = 'DAILY' | 'WEEKLY' | 'MONTHLY'
```

| Value   | Norwegian label | Typical use                                     |
|---------|-----------------|-------------------------------------------------|
| DAILY   | Daglig          | Kitchen opening, closing, service-period checks |
| WEEKLY  | Ukentlig        | Deep cleaning, equipment descaling              |
| MONTHLY | Månedlig        | Equipment calibration, HACCP documentation review |

### Module Type

```typescript
type ModuleType = 'IK_MAT' | 'IK_ALKOHOL'
```

| Value       | Description                                      |
|-------------|--------------------------------------------------|
| IK_MAT      | Food safety (Internkontroll Mat) checklists      |
| IK_ALKOHOL  | Alcohol service and age-verification checklists  |

Each checklist belongs to exactly one module type. The module type is displayed as a coloured badge next to the checklist title: green for IK-Mat, amber for IK-Alkohol.

---

## Type Definitions

All types live in `src/types/index.ts`.

### `Checklist`

```typescript
interface Checklist {
  id:           number
  title:        string
  frequency:    ChecklistFrequency
  items:        ChecklistItem[]
  completedBy?: string   // display name of the user who completed the whole list
  completedAt?: string   // ISO 8601 timestamp when the last item was checked
  moduleType:   ModuleType
}
```

`completedBy` and `completedAt` at the checklist level represent the whole-list completion (shown in the footer of a fully-completed card). They are distinct from the per-item `completedBy`/`completedAt` fields on individual `ChecklistItem` objects.

### `ChecklistItem`

```typescript
interface ChecklistItem {
  id:           number
  text:         string
  completed:    boolean
  completedBy?: string   // display name of the user who ticked this item
  completedAt?: string   // ISO 8601 timestamp
}
```

---

## Mock Checklists

Six checklists are defined in `checklists.service.ts`. The service filters by frequency, so only the matching subset is returned per request.

### DAILY checklists (3 × IK-Mat, 1 × IK-Alkohol)

**1. Åpning kjøkken** — IK-Mat — COMPLETED (all 5 items ticked by Ola Nordmann at 07:45)

| id | Text                                     | Done |
|----|------------------------------------------|------|
| 11 | Vask og desinfiser arbeidsflater         | Yes  |
| 12 | Sjekk såpedispensere og håndsprit        | Yes  |
| 13 | Kontroller skadedyrfeller                | Yes  |
| 14 | Sjekk holdbarhetsdatoer                  | Yes  |
| 15 | Registrer ansatte på vakt                | Yes  |

**2. Matdisplay og servering** — IK-Mat — PARTIAL (1 of 4 items complete)

| id | Text                                     | Done | By          |
|----|------------------------------------------|------|-------------|
| 21 | Sjekk temperatur i sushi-display         | Yes  | Kari Larsen |
| 22 | Renhold serveringsområde                 | No   | —           |
| 23 | Fyll på engangshansker                   | No   | —           |
| 24 | Kontroll matmerking og allergener        | No   | —           |

**3. Kveldsstenging** — IK-Mat — NOT STARTED (0 of 4 items complete)

| id | Text                                     | Done |
|----|------------------------------------------|------|
| 31 | Dyprenhold grillstasjon                  | No   |
| 32 | Tøm og desinfiser avfallsbeholdere       | No   |
| 33 | Mopp gulv med desinfeksjon               | No   |
| 34 | Siste temp-sjekk — alle enheter          | No   |

**4. Alkohol-logg kveldssjift** — IK-Alkohol — NOT STARTED (0 of 3 items complete)

| id | Text                                            | Done |
|----|-------------------------------------------------|------|
| 51 | Sjekk ID for alle gjester under 25 år           | No   |
| 52 | Logg antall avviste alderskontroller            | No   |
| 53 | Kontroller mengde alkohol på lager              | No   |

### WEEKLY checklists (1 × IK-Mat)

**5. Ukentlig kjøkkenrenhold** — IK-Mat — NOT STARTED (0 of 4 items complete)

| id | Text                                     | Done |
|----|------------------------------------------|------|
| 41 | Rengjør filter over stekeovn             | No   |
| 42 | Avkalk kaffemaskiner                     | No   |
| 43 | Rengjør kjøleromsdørene                  | No   |
| 44 | Sjekk og rengjør iskremmaskin            | No   |

### MONTHLY checklists (1 × IK-Mat)

**6. Månedlig utstyrskontroll** — IK-Mat — NOT STARTED (0 of 4 items complete)

| id | Text                                               | Done |
|----|----------------------------------------------------|------|
| 61 | Kontroller termometre — kalibrering                | No   |
| 62 | Rens og kontroller kjøleaggregater                 | No   |
| 63 | Oppdater HACCP-dokumentasjon                       | No   |
| 64 | Gjennomgå allergeninformasjon på meny              | No   |

---

## UI Structure

### Frequency Tab Filter

Three buttons rendered as a `sub-nav` bar at the top of the page:

- **Daglig** → `DAILY`
- **Ukentlig** → `WEEKLY`
- **Månedlig** → `MONTHLY`

Selecting a tab calls `setFrequency(value)`, which updates the local `activeFrequency` ref and calls `checklistsStore.fetchAll(freq)`. The store sets `activeFrequency` and replaces `checklists` with the filtered result. The view defaults to `DAILY` on mount.

### Checklist Cards

One card per checklist in the current frequency. Each card contains:

**Card header:**
- Checklist title (truncated with `text-overflow: ellipsis` if too long)
- Module badge (green IK-Mat or amber IK-Alkohol)
- Completed/total counter on the right: e.g., "3 / 5"

**Progress bar:**
A thin horizontal bar immediately below the header. Fill colour is determined by completion percentage:
- 100% — green (`full`)
- 1–99% — yellow (`partial`)
- 0% — no fill (`empty`, renders as background colour)

The bar width is animated with a CSS `transition: width 0.3s ease`, so ticking items produces a visible progress update.

**Item list:**
Each `ChecklistItem` is rendered as a list row containing:
- A native `<input type="checkbox">` styled with `accent-color: var(--c-primary)`
- The item text (struck through when `completed === true`)
- When `completed && completedBy`: a small muted attribution line below the text showing the user's name and optionally the formatted timestamp

Clicking a checkbox calls `checklistsStore.toggleItem(checklist.id, item.id)`.

**Completion footer (conditional):**
When all items in a checklist are checked, a footer appears below a divider line: "Fullført av: [completedBy] — [formatted time]". This uses the checklist-level `completedBy`/`completedAt` fields, not the last item's fields.

### Empty State

When no checklists exist for the selected frequency, a dashed-border empty state is shown: "Ingen sjekklister for denne perioden."

---

## Optimistic Toggle Pattern

Item completion is updated immediately in the UI before the server confirms, then reverted if the request fails.

```typescript
// In useChecklistsStore.toggleItem():

// 1. Find the item in local state and flip its completed flag immediately
item.completed = !item.completed

try {
  // 2. Fire the PATCH request to the server
  await checklistsService.toggleItem(checklistId, itemId, item.completed)
} catch {
  // 3. On error: revert the local state back to its original value
  item.completed = !item.completed
}
```

This pattern gives instant visual feedback — the checkbox changes state and the progress bar animates on every tap — without waiting for a network round-trip. On a slow or failed network, the item snaps back to its previous state.

The `completedBy` and `completedAt` fields are NOT optimistically set on toggle — the store only flips `completed`. These attribution fields would need to be set by the server response and patched back into local state when the real API is wired.

---

## Template vs Instance Distinction

This is the most significant gap between the current frontend implementation and the backend specification.

**Current frontend model (flat):**

The frontend treats each `Checklist` object as a self-contained, directly-mutable record. There is no separation between the template (the definition of what a checklist contains) and the instance (today's copy of that checklist for a specific date and shift). The mock service returns the same 6 objects every time the page loads, and `toggleItem()` mutates them in memory.

**Backend spec model (template + instance):**

The backend will separate these concerns:

- **Template** — an admin-created definition of a checklist: its title, frequency, module type, and ordered list of item texts. Templates are reusable and do not carry completion state.
- **Instance** — a per-day (or per-shift) materialisation of a template. Each instance has its own completion state, recorded-by metadata, and a date. Staff work on instances, not templates.

When the real API is integrated, the frontend will need to:
1. Fetch instances for today's date at the current frequency, not templates.
2. Update the `Checklist` type or add a separate `ChecklistInstance` type with a `date` field and a link back to the template.
3. Handle the case where today's instance does not exist yet (the backend may auto-create instances on first access, or the frontend may need to trigger instance creation).

---

## Store: `useChecklistsStore`

**State:**

| Ref               | Type                  | Initial value |
|-------------------|-----------------------|---------------|
| `checklists`      | `Checklist[]`         | `[]`          |
| `loading`         | `boolean`             | `false`       |
| `activeFrequency` | `ChecklistFrequency`  | `'DAILY'`     |

**Actions:**

| Action                                 | Description                                                             |
|----------------------------------------|-------------------------------------------------------------------------|
| `fetchAll(frequency)`                  | Sets `activeFrequency`, fetches and replaces `checklists` for that frequency |
| `toggleItem(checklistId, itemId)`      | Optimistically toggles `item.completed`, calls service, reverts on error |

There is no dedicated error state. If `fetchAll()` throws, `checklists` remains at its previous value.

---

## Service: `checklists.service.ts`

```typescript
checklistsService.getByFrequency(frequency: ChecklistFrequency): Promise<Checklist[]>
checklistsService.toggleItem(checklistId: number, itemId: number, completed: boolean): Promise<void>
```

Both methods are mocked with a short artificial delay (300 ms for fetch, 150 ms for toggle). `toggleItem()` currently does nothing except resolve — the mock does not persist the state change across re-fetches, which means switching frequency tabs and coming back will reset completion state.

---

## API Endpoints (Real — Not Yet Wired)

| Method | Endpoint                                                    | Purpose                                           |
|--------|-------------------------------------------------------------|---------------------------------------------------|
| GET    | `/api/checklists/instances?frequency=DAILY`                 | Fetch today's checklist instances for a frequency |
| PATCH  | `/api/checklists/instances/:id/items/:itemId`               | Toggle a single checklist item's completion state |

The `frequency` query parameter on the GET endpoint must match one of `DAILY`, `WEEKLY`, `MONTHLY`. The backend is expected to scope instances to the calling user's organisation and the current date automatically.

The PATCH body should send at minimum `{ completed: boolean }`. The response should include the updated item (with `completedBy` and `completedAt` populated by the server) so the frontend can update the attribution display without a full re-fetch.

---

## What Still Needs Implementation

| Gap                              | Detail                                                                                                                                      |
|----------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| Real API wiring                  | Both service methods are fully mocked. Needs integration once the backend checklist instance endpoints are live.                            |
| Template vs instance model       | The frontend type `Checklist` blurs the template/instance distinction. A `ChecklistInstance` type with a `date` field needs to be introduced at integration time. |
| Server-side completedBy/At       | After `toggleItem()` succeeds, the store does not patch `completedBy` or `completedAt` onto the item. The attribution line only shows for items that were pre-seeded with those values. |
| Error state in the store         | `fetchAll()` and `toggleItem()` have no error state. A failed fetch leaves the previous list in place with no user feedback.               |
| Checklist template admin         | Implemented. `/innstillinger/sjekklister` (ChecklistsTab.vue) allows ADMIN users to create, edit, and delete templates. Calls `POST/PUT/DELETE /api/checklists/templates`. |
| Per-shift instances              | If a kitchen runs multiple shifts (morning, evening), the backend may need to support multiple instances per day per template. The frontend currently has no shift concept. |
| Offline / optimistic persistence | The optimistic toggle reverts on error, but there is no queue or retry mechanism for poor connectivity environments (tablets in kitchens may have weak Wi-Fi). |
