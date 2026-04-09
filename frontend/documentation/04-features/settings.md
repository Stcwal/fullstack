# Settings (Innstillinger)

Administration panel for managing storage units, users, and organisation configuration. Restricted to ADMIN role.

---

## Route and access

| Property       | Value                                               |
|----------------|-----------------------------------------------------|
| Base route     | `/innstillinger`                                    |
| Default child  | Redirects to `/innstillinger/enheter`               |
| Container      | `SettingsView.vue`                                  |
| Meta           | `requiresAuth: true`, `requiresAdmin: true`         |

The router guard in `src/router/index.ts` checks `to.meta.requiresAdmin && auth.user?.role !== 'ADMIN'` and redirects non-admin users to `/dashboard`. MANAGER and STAFF users cannot reach any settings route.

`SettingsView.vue` is a shell component — it renders the tab navigation (Enheter / Brukere / Organisasjon / Sjekklister) using `RouterLink` and a `<RouterView />` outlet for the active child tab.

---

## Tab navigation

Three tabs are rendered as `RouterLink` elements pointing to the child routes:

| Tab label     | Route                            | Component            |
|---------------|----------------------------------|----------------------|
| Enheter       | `/innstillinger/enheter`         | `UnitsTab.vue`       |
| Brukere       | `/innstillinger/brukere`         | `UsersTab.vue`       |
| Organisasjon  | `/innstillinger/org`             | `OrgTab.vue`         |
| Sjekklister   | `/innstillinger/sjekklister`     | `ChecklistsTab.vue`  |

The active class is applied with `:class="{ active: route.path.startsWith('/innstillinger/...' ) }"` using `useRoute()`.

---

## Units Tab (`/innstillinger/enheter`)

**Component:** `UnitsTab.vue`
**Store:** `useUnitsStore` — referenced but the tab calls `unitsService` directly via an `import`. The `useUnitsStore` is used elsewhere (dashboard, freezer/fridge views) but the settings tab uses the service directly.
**Service:** `unitsService` (`src/services/units.service.ts`)

### Purpose

Allows ADMIN to view, create, edit, and delete the organisation's registered storage units (freezers, fridges, coolers).

### Data model: `SettingsUnit`

Defined in `src/types/index.ts`. Shares most fields with `Unit` but is a separate type representing the settings-scoped view.

```ts
type UnitType = 'FREEZER' | 'FRIDGE' | 'COOLER' | 'OTHER'

interface SettingsUnit {
  id: number
  name: string
  type: UnitType
  targetTemp: number      // target operating temperature in °C
  minTemp: number         // lower alert threshold in °C
  maxTemp: number         // upper alert threshold in °C
  contents: string        // description of what is stored
  active: boolean         // false = soft-deleted / inactive
}
```

### Mock units

Defined in `src/services/units.service.ts`. Six units — five active, one inactive.

| id | Name                         | Type    | Target | Min  | Max  | Contents                           | Active |
|----|------------------------------|---------|--------|------|------|------------------------------------|--------|
| 1  | Fryser #1                    | FREEZER | -18°C  | -22°C| -16°C| Sjømat, Kjøtt, Ferdigvarer         | Yes    |
| 2  | Fryser #2                    | FREEZER | -18°C  | -22°C| -16°C| Grønnsaker, Desserter              | Yes    |
| 3  | Kjøleskap #1 (kjøkken)       | FRIDGE  | 3°C    | 1°C  | 4°C  | Fisk, Meieriprodukter, Sauser      | Yes    |
| 4  | Kjøleskap #2 (bar)           | FRIDGE  | 3°C    | 1°C  | 4°C  | Juice, Melk, Frukt                 | Yes    |
| 5  | Visningskjøler               | COOLER  | 5°C    | 3°C  | 6°C  | Sushi-display, Drikke              | Yes    |
| 6  | Kjøleskap #3 (lager)         | FRIDGE  | 3°C    | 1°C  | 4°C  | Lagervarer                         | No     |

Fryser #2 has `hasAlert: true` in the `Unit` type (used in dashboard/reading views), but `SettingsUnit` does not carry `hasAlert`. New units created in the session are assigned IDs starting from 7 (incremented by `nextId`).

### UI

The tab header shows the "Enheter" section title on the left and a "+ Legg til ny enhet" button on the right.

Each unit is rendered as a `.status-row` inside a `.card`, showing:
- Unit name (with an "Inaktiv" badge if `active === false`)
- Type label, target temperature, min/max temperatures in muted small text
- "Rediger" ghost button (blue) and "Slett" ghost button (danger)

Loading state: "Laster inn..." while `loading` is true.
Empty state: "Ingen enheter registrert." if the list is empty after loading.

**Unit type labels** (Norwegian display):

| `UnitType` | Norwegian label |
|------------|-----------------|
| FREEZER    | Fryser          |
| FRIDGE     | Kjøleskap       |
| COOLER     | Kjøler          |
| OTHER      | Annet           |

### Add / Edit modal

Uses the shared `AppModal` component. The same modal is used for both add and edit — the title changes between "Legg til ny enhet" and "Rediger enhet" based on the `isEditing` flag.

Form fields:

| Field                      | Type     | Default (add) | Notes                                       |
|----------------------------|----------|---------------|---------------------------------------------|
| Navn                       | text     | ""            | Unit display name                           |
| Type                       | select   | FRIDGE        | Options: Fryser, Kjøleskap, Kjøler, Annet  |
| Måltemperatur (°C)         | number   | 4             | Step 0.1                                    |
| Minimumstemperatur (°C)    | number   | 0             | Step 0.1                                    |
| Maksimumstemperatur (°C)   | number   | 8             | Step 0.1                                    |
| Innhold                    | textarea | ""            | Single-row textarea                         |
| Aktiv                      | checkbox | true          | Unchecked = soft delete / inactive          |

The "Lagre" button calls `unitsService.update()` for edits or `unitsService.create()` for new units, then updates the local list reactively without a full refetch.

### Delete

Clicking "Slett" shows a `window.confirm()` dialog with the unit name. On confirmation, `unitsService.remove()` is called and the unit is removed from the local list. The service's `remove()` mock filters the unit out of `mockUnits` (in-memory deletion). The real API call would be `DELETE /api/units/:id`.

Note: In the current mock, deletion is hard — the unit is removed from the array. The backend should implement soft deletion (setting `active: false`) to preserve historical temperature reading records linked to the unit.

### API endpoints

| Method | Endpoint         | Description                                              |
|--------|------------------|----------------------------------------------------------|
| GET    | `/api/units`     | Fetch all units for the organisation                     |
| POST   | `/api/units`     | Create a new unit                                        |
| PUT    | `/api/units/:id` | Update an existing unit (full replacement)               |
| DELETE | `/api/units/:id` | Delete (or soft-delete) a unit                           |

---

## Users Tab (`/innstillinger/brukere`)

**Component:** `UsersTab.vue`
**Service:** `organizationService` (`src/services/organization.service.ts`)

### Purpose

Allows ADMIN to view all organisation users, create new users, edit user details (name, email, role, status, permissions), and trigger a password reset.

### Data model: `SettingsUser`

Defined in `src/types/index.ts`.

```ts
interface SettingsUser {
  id: number
  firstName: string
  lastName: string
  email: string
  role: UserRole                 // 'ADMIN' | 'MANAGER' | 'STAFF'
  active: boolean
  colorBg: string                // avatar background colour (frontend display only)
  colorText: string              // avatar text colour (frontend display only)
  permissions: UserPermissions
}
```

### `UserPermissions`

Defined in `src/types/index.ts`. Fine-grained permission flags that are independent of the `role` field.

```ts
interface UserPermissions {
  temperatureLogging: boolean
  checklists: boolean
  reports: boolean
  deviations: boolean
  userAdmin: boolean
  settings: boolean
}
```

### Mock users

Defined in `src/services/organization.service.ts`. Three users representing the three role levels.

**Kari Larsen — ADMIN**

```
id:          1
email:       kari@everestsushi.no
role:        ADMIN
active:      true
colorBg:     #EDE9FE  (purple tint)
colorText:   #5B21B6  (purple)
permissions: all true
```

**Ola Nordmann — MANAGER**

```
id:          2
email:       ola@everestsushi.no
role:        MANAGER
active:      true
colorBg:     #F0FDF4  (green tint)
colorText:   #15803D  (green)
permissions: temperatureLogging ✓, checklists ✓, reports ✓, deviations ✓
             userAdmin ✗, settings ✗
```

**Per Martinsen — STAFF**

```
id:          3
email:       per@everestsushi.no
role:        STAFF
active:      true
colorBg:     #F1F5F9  (slate tint)
colorText:   #475569  (slate)
permissions: temperatureLogging ✓, checklists ✓, deviations ✓
             reports ✗, userAdmin ✗, settings ✗
```

### UI

The tab header shows the "Brukere" section title and a "+ Legg til ny bruker" button.

Each user is rendered as a `.status-row` showing:
- Avatar (circular, initials derived from `firstName[0] + lastName[0]`, colours from `colorBg`/`colorText`)
- Full name and email
- Role badge (right side)
- "Rediger" ghost button (blue)

**Role badge classes:**

| Role    | Badge class    | Norwegian label |
|---------|----------------|-----------------|
| ADMIN   | `badge-purple` | Admin           |
| MANAGER | `badge-info`   | Leder           |
| STAFF   | `badge-neutral`| Ansatt          |

### Add / Edit modal

The `FormData` type used in the modal omits `id`, `colorBg`, and `colorText` — these are not editable through the form.

Form fields:

| Field              | Type     | Default (add)  | Notes                                     |
|--------------------|----------|----------------|-------------------------------------------|
| Fornavn            | text     | ""             |                                           |
| Etternavn          | text     | ""             |                                           |
| E-post             | email    | ""             |                                           |
| Rolle              | select   | STAFF          | Options: Admin, Leder, Ansatt             |
| Status             | select   | true (Aktiv)   | Options: Aktiv / Inaktiv                  |
| Tillatelser        | checkboxes | all false    | Six checkboxes, one per `UserPermissions` field |

Permission checkboxes:

| Checkbox label         | `permissions` key      |
|------------------------|------------------------|
| Temperaturlogging      | `temperatureLogging`   |
| Sjekklister            | `checklists`           |
| Rapporter              | `reports`              |
| Avviksrapportering     | `deviations`           |
| Brukeradministrasjon   | `userAdmin`            |
| Innstillinger          | `settings`             |

The modal footer has three buttons: "Tilbakestill passord" (left, ghost), "Avbryt" (right, secondary), and "Lagre" (right, primary).

### "Tilbakestill passord"

Not yet implemented. Clicking it shows `alert('Ikke implementert ennå')`. The real implementation would call `POST /api/users/:id/reset-password`.

### API endpoints

| Method | Endpoint                          | Description                                          |
|--------|-----------------------------------|------------------------------------------------------|
| GET    | `/api/users`                      | Fetch all users for the organisation                 |
| POST   | `/api/users`                      | Create a new user                                    |
| PUT    | `/api/users/:id`                  | Update user details, role, status, and permissions   |
| PATCH  | `/api/users/:id/role`             | Update role only (planned; not separately called in current code) |
| POST   | `/api/users/:id/reset-password`   | Trigger a password reset email (not yet implemented) |

---

## Organisation Tab (`/innstillinger/org`)

**Component:** `OrgTab.vue`
**Service:** `organizationService` (`src/services/organization.service.ts`)

### Purpose

Allows ADMIN to update the organisation's legal and contact details, toggle active compliance modules, and configure notification behaviour.

### Data model: `Organization`

Defined in `src/types/index.ts`.

```ts
interface Organization {
  name: string
  orgNumber: string
  industry: string
  address: string
  modules: {
    ikMat: boolean
    ikAlkohol: boolean
  }
  notifications: {
    emailOnTempDeviation: boolean
    dailySummaryToManagers: boolean
    smsOnCritical: boolean
  }
}
```

### Mock organisation

Defined in `src/services/organization.service.ts`.

```
name:        "Everest Sushi & Fusion AS"
orgNumber:   "937 219 997"
industry:    "Restaurant"
address:     "Innherredsveien 1, 7014 Trondheim"
modules:     ikMat: true, ikAlkohol: true
notifications:
  emailOnTempDeviation:      true
  dailySummaryToManagers:    true
  smsOnCritical:             false
```

### UI structure

The tab renders three separate cards stacked vertically. Each card has a `card-header` with a section title and its own save button. There is no single "save all" — each section saves independently via a call to `organizationService.updateOrg()` with the full current form state.

#### Card 1: Organisasjonsdetaljer

Form fields:

| Field        | Type   | Notes                                                       |
|--------------|--------|-------------------------------------------------------------|
| Bedriftsnavn | text   | Free text, placeholder "Restaurantnavn AS"                  |
| Org.nummer   | text   | Free text, placeholder "123 456 789"                        |
| Bransje      | select | Options: Restaurant, Bar, Kafé, Kantine                     |
| Adresse      | text   | Free text, placeholder "Gateadresse 1, 0000 By"            |

Save button: "Lagre" (primary, right-aligned). On success, a "Lagret!" confirmation message appears in the form for 3 seconds (`setTimeout` 3000ms).

#### Card 2: Aktive moduler

Two toggle rows, each with a checkbox, a module badge, a heading, and a description:

| Module      | Checkbox model                | Badge class   | Description                                                                 |
|-------------|-------------------------------|---------------|-----------------------------------------------------------------------------|
| IK-Mat      | `orgForm.modules.ikMat`       | `ik-mat`      | Internkontroll for næringsmiddelhygiene – temperaturlogging, rengjøring og sjekklister. |
| IK-Alkohol  | `orgForm.modules.ikAlkohol`   | `ik-alkohol`  | Internkontroll for skjenkebevilgning – alderskontroll, opplæring og bevillingslogg. |

There is no dedicated save button for this card in the current implementation. Module toggle changes are saved implicitly when the user clicks "Lagre" in the Organisasjonsdetaljer card, since `saveOrg()` sends the entire `orgForm` object. This is a UX gap — the modules card has no visual indication that changes need to be explicitly saved.

#### Card 3: Varslingsinnstillinger

Three checkboxes:

| Label                               | Field                                      |
|-------------------------------------|--------------------------------------------|
| E-postvarsling ved temperaturavvik  | `orgForm.notifications.emailOnTempDeviation` |
| Daglig oppsummering til ledere      | `orgForm.notifications.dailySummaryToManagers` |
| SMS-varsling ved kritiske avvik     | `orgForm.notifications.smsOnCritical`      |

Save button: "Lagre innstillinger" (primary, right-aligned). Also calls `organizationService.updateOrg()` with the full `orgForm`. On success, "Lagret!" appears for 3 seconds.

### Save behaviour

Both `saveOrg()` and `saveNotifications()` call `organizationService.updateOrg(orgForm.value)`. The service merges the payload with `mockOrg` using spread. In real integration, this maps to `PUT /api/organization` with the full organisation object.

Because both save actions send the entire `orgForm`, editing the org name and clicking "Lagre innstillinger" (notifications save button) will also persist the updated org name — and vice versa. This is an intentional design consequence of having a single shared form ref, not a bug.

### API endpoints

| Method | Endpoint           | Description                                          |
|--------|--------------------|------------------------------------------------------|
| GET    | `/api/organization`| Fetch organisation details                           |
| PUT    | `/api/organization`| Update organisation (full object replacement)        |

---

---

## ChecklistsTab

Route: `/innstillinger/sjekklister`
Component: `src/views/settings/ChecklistsTab.vue`
Access: ADMIN only

Allows administrators to manage the checklist templates that generate daily/weekly/monthly instances for kitchen staff.

### UI

The tab header shows the "Sjekklistemaler" section title and a "+ Legg til ny mal" button.

Each template is rendered as a `.status-row` showing:
- Template title
- Frequency and item count in muted text below
- "Rediger" ghost button (blue) — opens the edit modal
- "Slett" ghost button (red) — deletes with a confirmation dialog

### Add / Edit modal

Form fields:

| Field     | Type     | Validation               |
|-----------|----------|--------------------------|
| Tittel    | text     | Required, max 120 chars  |
| Frekvens  | select   | DAILY / WEEKLY / MONTHLY |
| Punkter   | repeated text inputs | At least one non-empty item required, max 250 chars each |

Items are managed as a dynamic list: "+ Legg til punkt" appends a new empty input row, and the × button on each row removes that item. Empty rows are filtered out on submit — they are not sent to the backend.

### Save behaviour

**Create**: `POST /api/checklists/templates` with `{ title, frequency, itemTexts: string[] }`. The created template is appended to the local list.

**Edit**: `PUT /api/checklists/templates/:id` with the same payload. The updated template replaces its entry in the local list.

**Delete**: `DELETE /api/checklists/templates/:id` after a `window.confirm()` dialog. The template is filtered out of the local list.

All three operations show a "Lagrer..." button state during the request. Errors display an inline message in the modal.

### API endpoints

| Method | Endpoint                           | Description                    |
|--------|------------------------------------|--------------------------------|
| GET    | `/api/checklists/templates`        | Fetch all templates for the org |
| POST   | `/api/checklists/templates`        | Create a new template          |
| PUT    | `/api/checklists/templates/:id`    | Update an existing template    |
| DELETE | `/api/checklists/templates/:id`    | Delete a template              |

All endpoints require ADMIN role (`@PreAuthorize("hasRole('ADMIN')")` on the Spring controller).

---

## Services summary

| Service                  | File                                  | Used by tabs              |
|--------------------------|---------------------------------------|---------------------------|
| `unitsService`           | `src/services/units.service.ts`       | UnitsTab                  |
| `organizationService`    | `src/services/organization.service.ts`| UsersTab, OrgTab          |
| `checklistsService`      | `src/services/checklists.service.ts`  | ChecklistsTab (templates) |
