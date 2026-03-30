# Feature: Temperature Logging

## Overview

| Property  | Value                                                        |
|-----------|--------------------------------------------------------------|
| Routes    | `/fryser` (FreezerView.vue), `/kjoeleskap` (FridgeView.vue)  |
| Components | `src/views/FreezerView.vue`, `src/views/FridgeView.vue`     |
| Stores    | `useUnitsStore` (`src/stores/units.ts`), `useReadingsStore` (`src/stores/readings.ts`) |
| Services  | `src/services/units.service.ts`, `src/services/readings.service.ts` |
| Access    | All authenticated users (`UserPermissions.temperatureLogging`) |
| Module    | IK-Mat                                                       |

Temperature logging is the operational core of the IK-Mat compliance module. Kitchen staff navigate to either `/fryser` (freezers) or `/kjoeleskap` (fridges and display coolers), select a storage unit, and record a temperature reading. Any reading outside the unit's configured min/max thresholds is flagged immediately as a deviation. The history list provides an auditable record of all measurements for a given unit.

The two routes are functionally identical — they differ only in which unit types they surface: FreezerView shows `FREEZER` units, FridgeView shows `FRIDGE` and `COOLER` units.

---

## Unit Types

```typescript
type UnitType = 'FREEZER' | 'FRIDGE' | 'COOLER' | 'DISPLAY' | 'OTHER'
```

| Type     | Norwegian label  | Typical target temp | Use case                            |
|----------|------------------|---------------------|-------------------------------------|
| FREEZER  | Fryser           | −18 °C              | Frozen seafood, meat, ready meals   |
| FRIDGE   | Kjøleskap        | +3 °C               | Fish, dairy, sauces, raw ingredients |
| COOLER   | Kjølerom         | +5 °C               | Display and pass-through cooling    |
| DISPLAY  | Visningskjøler   | +5 °C               | Customer-facing sushi/deli display  |
| OTHER    | Annet            | Varies              | Edge cases                          |

Note: the frontend `types/index.ts` currently defines `UnitType` as `'FREEZER' | 'FRIDGE' | 'COOLER' | 'OTHER'` — `DISPLAY` is listed in the backend spec but not yet in the frontend type union. See the type gaps section below.

---

## Type Definitions

All types live in `src/types/index.ts`.

### `Unit`

```typescript
interface Unit {
  id:        number
  name:      string
  type:      UnitType
  targetTemp: number   // ideal operating temperature (°C)
  minTemp:   number    // lower acceptance threshold (°C)
  maxTemp:   number    // upper acceptance threshold (°C)
  contents:  string    // free-text description of stored items
  active:    boolean   // inactive units are hidden from logging views
  hasAlert?: boolean   // true when the latest reading is out of range
}
```

`hasAlert` is set by the service layer (currently the mock; in production the backend will set this). It causes the view to show a red danger banner above the unit detail card.

### `TemperatureReading`

```typescript
interface TemperatureReading {
  id:           number
  unitId:       number
  temperature:  number   // °C, one decimal place convention
  recordedAt:   string   // ISO 8601 datetime string
  recordedBy:   string   // display name of the logging user
  note?:        string   // optional free-text comment
  isOutOfRange: boolean  // true if temp < minTemp or temp > maxTemp
}
```

`isOutOfRange` is computed by the backend (or by the mock service) at insertion time by comparing the submitted temperature against the unit's `minTemp`/`maxTemp`. The frontend does not recalculate this on the client — it trusts the value returned from the API.

### `NewReading`

```typescript
interface NewReading {
  unitId:      number
  temperature: number
  recordedAt:  string   // ISO 8601 datetime string
  note?:       string
}
```

This is the payload sent from the form to the service's `create()` method. The `recordedBy` field is resolved server-side from the authenticated user's session; the frontend does not send it.

---

## Mock Units

Six units are defined in `units.service.ts`. Inactive units are excluded from the logging views by `useUnitsStore.getByType()`, which filters `active === true`.

| id | Name                    | Type    | Target | Min  | Max  | Alert | Active |
|----|-------------------------|---------|--------|------|------|-------|--------|
| 1  | Fryser #1               | FREEZER | −18 °C | −22  | −16  | No    | Yes    |
| 2  | Fryser #2               | FREEZER | −18 °C | −22  | −16  | Yes   | Yes    |
| 3  | Kjøleskap #1 (kjøkken)  | FRIDGE  |  +3 °C |  +1  |  +4  | No    | Yes    |
| 4  | Kjøleskap #2 (bar)      | FRIDGE  |  +3 °C |  +1  |  +4  | No    | Yes    |
| 5  | Visningskjøler          | COOLER  |  +5 °C |  +3  |  +6  | No    | Yes    |
| 6  | Kjøleskap #3 (lager)    | FRIDGE  |  +3 °C |  +1  |  +4  | No    | No     |

Fryser #2 has `hasAlert: true` because its most recent mock reading (`−12.1 °C`) is above the −16 °C upper limit. Its mock reading includes a note: `"Dør sto åpen"`.

Kjøleskap #3 is inactive and will not appear in the FridgeView tab list.

---

## Mock Readings

`readings.service.ts` seeds 11 readings across 5 active units. Readings are ordered newest-first. The view displays the most recent 5.

Selected notable readings:

| id | Unit     | Temperature | isOutOfRange | Note           |
|----|----------|-------------|--------------|----------------|
| 4  | Fryser #2 | −12.1 °C   | true         | Dør sto åpen   |
| 1  | Fryser #1 | −18.4 °C   | false        | —              |
| 6  | Kjøleskap #1 | +3.2 °C | false        | —              |
| 10 | Visningskjøler | +5.1 °C | false      | —              |

Reading 4 is the one that triggers `hasAlert: true` on Fryser #2.

---

## Deviation Detection

In the current mock implementation, deviation detection is performed in `readingsService.create()` on the client side using a hardcoded `unitMap` lookup:

```typescript
const isOutOfRange = data.temperature < limits.min || data.temperature > limits.max
```

In production, this logic must live on the backend. The real flow will be:

1. Client POSTs `NewReading` to `/api/units/:unitId/readings`.
2. Backend looks up the unit's `minTemp`/`maxTemp`.
3. Backend sets `isDeviation = true` on the stored reading if temperature is outside the range.
4. Backend optionally creates a linked `Deviation` record and triggers notifications.
5. Backend returns the persisted `TemperatureReading` with `isOutOfRange` set.

The frontend never decides whether a reading is out of range — it renders whatever `isOutOfRange` the API returns.

When `isOutOfRange` is `true`, the reading row receives the `.is-alert` CSS class, which adds a red left border and red text to the temperature value.

---

## UI Structure

Both FreezerView and FridgeView follow the same layout pattern:

### 1. Unit Sub-Navigation (sub-nav tabs)

A horizontal scrollable tab bar listing all active units of the relevant type(s). Selecting a tab calls `selectUnit(id)`, which:
- Sets `selectedUnitId`
- Calls `readingsStore.fetchByUnit(id)` to load readings
- Resets the new-reading form
- Clears the success flash

### 2. Alert Banner

Conditionally rendered above the unit detail card when `selectedUnit.hasAlert === true`. Red danger banner with the message "Siste måling utenfor grenseverdi! Ta tiltak umiddelbart."

### 3. Unit Detail Card

Displays three info fields in a 3-column grid:
- **Måltemperatur** — the `targetTemp` value
- **Akseptabelt område** — `minTemp °C – maxTemp °C`
- **Innhold** — the `contents` string

FridgeView additionally shows a `badge-neutral` badge indicating the unit's type label (Kjøleskap or Kjølerom). Freezer view omits this since all units on that page are the same type.

### 4. New Reading Form

Fields:
- **Temperatur (°C)** — required number input, step 0.1. Shows an inline validation error if submitted empty.
- **Tidspunkt** — time input, defaults to the current HH:MM time.
- **Merknad** — optional free-text note input.

Below the form, a "Logges som: [Name]" line shows the authenticated user's name pulled from `useAuthStore`. This field is read-only — users cannot log readings on behalf of others.

On successful submission, a green "Lagret!" info banner flashes for 3 seconds before auto-dismissing.

### 5. Readings History List

The 5 most recent readings for the selected unit, sorted newest-first (`readingsStore.getByUnit(unitId).slice(0, 5)`). Each row shows:
- Left column: formatted datetime (relative: "I dag 08:15" / "I går 20:00"), recorder name, and optional note
- Right column: temperature value in °C

Rows with `isOutOfRange === true` receive the `.is-alert` CSS class (red left-border accent). The temperature value is coloured red (`text-danger`) for out-of-range readings and green (`text-success`) for normal readings.

---

## Optimistic Update Pattern

New readings are not optimistically inserted into the list before the server confirms them. Instead, the flow is:

1. User submits the form.
2. `readingsStore.addReading(data)` calls `readingsService.create(data)`.
3. The service resolves with the created `TemperatureReading` (including `isOutOfRange`).
4. `addReading()` does `readings.value.unshift(created)` — the confirmed record is prepended to the store.
5. The component then calls `readingsStore.fetchByUnit(selectedUnit.id)` again to re-sync (a belt-and-suspenders refresh).

`readingsStore.saving` is `true` during step 2–3, which disables the submit button and changes its label to "Lagrer…".

There is no rollback path on error — if `readingsService.create()` throws, the error propagates out of `addReading()` and is currently unhandled by the view.

---

## Store: `useUnitsStore`

**State:** `units: Unit[]`, `loading: boolean`, `error: string | null`

**Actions:**

| Action                              | Description                                               |
|-------------------------------------|-----------------------------------------------------------|
| `fetchUnits()`                      | Loads all units from the service into `units`             |
| `createUnit(data)`                  | Calls service, pushes the created unit into `units`       |
| `updateUnit(id, data)`              | Calls service, replaces the matching unit in `units`      |
| `deleteUnit(id)`                    | Calls service, filters the unit out of `units`            |
| `getByType(type: UnitType)`         | Returns active units of the given type (used by views)    |

`getByType()` is a synchronous computed helper, not an async action. It filters `active === true`, which is why Kjøleskap #3 (inactive) is excluded from the FridgeView tab list.

## Store: `useReadingsStore`

**State:** `readings: TemperatureReading[]`, `loading: boolean`, `saving: boolean`, `error: string | null`

**Actions:**

| Action                       | Description                                                      |
|------------------------------|------------------------------------------------------------------|
| `fetchByUnit(unitId)`        | Replaces `readings` with all readings for the given unit         |
| `addReading(data)`           | Creates a reading via service, prepends it to `readings`         |
| `getByUnit(unitId)`          | Synchronous filter helper returning readings for a given unit    |

`saving` and `loading` are separate flags. `loading` covers fetching the history list; `saving` covers the form submission. This allows the history skeleton and the submit button to reflect their respective states independently.

---

## API Endpoints (Real — Not Yet Wired)

| Method | Endpoint                          | Purpose                                      |
|--------|-----------------------------------|----------------------------------------------|
| GET    | `/api/units`                      | Fetch all units for the organisation         |
| GET    | `/api/units/:unitId/readings`     | Fetch readings for a specific unit           |
| POST   | `/api/units/:unitId/readings`     | Submit a new temperature reading             |

The current mock comments reference `/api/readings?unitId=...` (flat collection) and `/api/units` separately. When the real backend is integrated, the nested path `/api/units/:unitId/readings` is the preferred REST convention and matches the intent in `readings.service.ts`.

---

## Known Type Gaps vs Backend Spec

The frontend `Unit` interface uses field names that may diverge from the backend entity. Known mismatches to resolve at integration time:

| Frontend field  | Likely backend field      | Notes                                              |
|-----------------|---------------------------|----------------------------------------------------|
| `targetTemp`    | `targetTemperature`       | Backend may use the full word                      |
| `minTemp`       | `minTemperature`          | Same pattern                                       |
| `maxTemp`       | `maxTemperature`          | Same pattern                                       |
| `hasAlert`      | Not a backend field       | Derived at query time; backend may not persist this |
| `isOutOfRange`  | `isDeviation`             | Backend spec refers to deviations, not "out of range" |
| `UnitType`      | Missing `DISPLAY`         | Backend may include DISPLAY; frontend union does not |

A DTO mapping layer in the service file is the right place to handle these differences at integration time — do not rename the frontend types to match the backend; map at the service boundary.
