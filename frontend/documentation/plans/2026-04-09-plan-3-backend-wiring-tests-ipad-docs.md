# Plan 3: Backend Wiring, Tests, iPad UI, Architecture Docs

**Date:** 2026-04-09
**Deadline:** Friday 10 April 2026, kl. 14:00

> **For agentic workers:** Use `superpowers:executing-plans` to implement this plan task-by-task.

**Goal:**
1. Remove all mock data — wire every frontend service to the real Railway backend
2. Add Vitest unit tests + Cypress E2E tests to hit the coverage requirement
3. Simplify the iPad tab bar (remove "Mer" drawer clutter)
4. Write the architecture diagram and remaining required frontend documentation

**DO NOT implement notifications** — backend partner (Oleander) is still building that.

---

## Context: What Is Already Real vs Still Mocked

| Service | Status | Notes |
|---------|--------|-------|
| `auth.service.ts` | ✅ Real | Correctly maps `LoginResponseData` → `AuthResponse`. `me()` returns from sessionStorage only — needs `/api/users/me` call |
| `dashboard.service.ts` | ✅ Real | Calls `GET /api/dashboard`. May need response shape verification |
| `readings.service.ts` | ✅ Real | `GET /api/units/:id/readings`, `POST /api/units/:id/readings` |
| `deviations.service.ts` | ✅ Real | `GET /api/deviations`, `POST /api/deviations`, `PATCH /api/deviations/:id/resolve` |
| `reports.service.ts` | ⚠️ Partial | Chart endpoint real; export stubs remain |
| `units.service.ts` | ❌ Mocked | Ready to wire — field names already match backend |
| `checklists.service.ts` | ❌ Mocked | Needs instance response mapping |
| `organization.service.ts` | ❌ Mocked | Needs org + users endpoints |
| `documents.service.ts` | ❌ Mocked | Backend returns different shape — needs mapping |
| `alkohol.service.ts` | ❌ Mocked | URL bug: `/alkohol/` → must be `/alcohol/` |

---

## Priority 1: Wire Units Service

**Backend endpoint:** `GET /api/units` → `ApiResponse<List<UnitResponse>>`

Backend `UnitResponse` fields (from `UnitController.java`):
```
id, organizationId, name, type, targetTemperature, minThreshold, maxThreshold,
description, active, deletedAt, createdAt, updatedAt
```

Frontend `Unit` type already uses these exact field names. `hasAlert` is frontend-only (not in backend) — default it to `false`.

### Steps

- [ ] **1.1** Replace `units.service.ts` mock `getAll()` with `api.get<Unit[]>('/units')`, add `hasAlert: false` to each item
- [ ] **1.2** Replace `create()` with `api.post<Unit>('/units', { name, type, targetTemperature, minThreshold, maxThreshold, description })`
- [ ] **1.3** Replace `update()` with `api.put<Unit>('/units/${id}', data)` (same fields)
- [ ] **1.4** Replace `remove()` with `api.delete('/units/${id}')` — backend soft-deletes (sets `deletedAt`)
- [ ] **1.5** Remove the `mockUnits`, `nextId`, and `delay()` helpers

---

## Priority 2: Wire Checklists Service

**Backend endpoints:**
- `GET /api/checklists/instances?frequency=DAILY|WEEKLY|MONTHLY` → `ApiResponse<List<ChecklistInstanceResponse>>`
- `PATCH /api/checklists/instances/:id/items/:itemId` with body `{ completed: boolean }`

The backend distinguishes templates (definitions) from instances (per-day records with completion state). The frontend's `Checklist` type maps to an instance.

**Expected `ChecklistInstanceResponse` shape** (from `ChecklistController.java`):
```
id, title, frequency, moduleType, items[{ id, text, completed, completedBy: {id,name}, completedAt }]
```

### Steps

- [ ] **2.1** Replace `getByFrequency()` with real API call: `api.get('/checklists/instances', { params: { frequency } })`
- [ ] **2.2** Map response: `completedBy` comes as `{ id, name }` object — extract `.name` for the `ChecklistItem.completedBy` string field
- [ ] **2.3** Replace `toggleItem()` with real `api.patch('/checklists/instances/${checklistId}/items/${itemId}', { completed })`
- [ ] **2.4** Update the optimistic update in `useChecklistsStore` `toggleItem()` action to still apply immediately but also catch errors and revert
- [ ] **2.5** Remove mock checklist array and `delay()` helper

---

## Priority 3: Wire Organization + Users Service

**Backend endpoints:**
- `GET /api/organization/me` → `ApiResponse<OrganizationResponse>` (fields: `name`, `organizationNumber`, `contactEmail`, `createdAt`)
- `PUT /api/organization/me` → same
- `GET /api/users` → `ApiResponse<List<UserResponse>>` (fields: `id`, `firstName`, `lastName`, `email`, `role`, `isActive`)
- `POST /api/users` → `ApiResponse<UserResponse>`
- `PUT /api/users/:id` → update `firstName`, `lastName`
- `PUT /api/users/:id/role` → update role with `{ role }` body

**Mapping notes:**
- `Organization.orgNumber` ↔ backend `organizationNumber`
- `Organization.modules` and `notifications` are frontend-only — keep them hardcoded/local (backend doesn't persist these)
- `SettingsUser.colorBg/colorText` are frontend-only — compute from role (`ADMIN` = purple, `MANAGER` = green, `STAFF` = slate)
- `SettingsUser.permissions` — backend doesn't return this; derive defaults from role using the default permissions table in CLAUDE.md
- `SettingsUser.isActive` matches backend `isActive` field name directly

### Steps

- [ ] **3.1** Replace `getOrg()` with `api.get('/organization/me')` and map `organizationNumber → orgNumber`; keep `modules` and `notifications` as hardcoded frontend defaults (not persisted)
- [ ] **3.2** Replace `updateOrg()` with `api.put('/organization/me', { name, organizationNumber: data.orgNumber })`
- [ ] **3.3** Replace `getUsers()` with `api.get<UserResponse[]>('/users')` and map: compute `colorBg`/`colorText` from role, derive `permissions` from role defaults, use `isActive` directly
- [ ] **3.4** Replace `updateUser()`: split into two calls if role changed — `PUT /users/:id` for name, `PUT /users/:id/role` for role change
- [ ] **3.5** Replace `createUser()` with `api.post('/users', { firstName, lastName, email, role, password })` — check what `CreateUserRequest` requires in backend
- [ ] **3.6** Add helper `deriveColorFromRole(role)` and `derivePermissionsFromRole(role)` inline in the service (3-5 lines each, not abstracted elsewhere)
- [ ] **3.7** Remove mock `mockOrg`, `mockUsers`, and `delay()` helper

---

## Priority 4: Wire Documents Service

**Backend endpoints:**
- `GET /api/documents?category=TRAINING` → `ApiResponse<List<DocumentResponse>>`
- No backend certifications endpoint — keep that mock for now

**Backend `DocumentResponse` shape** (from `DocumentController.java`):
```
id, title, description, contentType, fileSize, fileName, category, uploadedAt, uploadedBy
```

This does NOT match `TrainingDocument` (which has `subtitle`, `type`, `actionLabel`, `colorBg`, `colorText`). Need to map.

### Steps

- [ ] **4.1** Replace `getTrainingDocs()` with `api.get('/documents', { params: { category: 'TRAINING' } })`
- [ ] **4.2** Map `DocumentResponse` → `TrainingDocument`:
  - `title` → `title`
  - `description` → `subtitle`
  - Derive `type` from `contentType`: `video/*` → `'VID'`, `application/pdf` → `'PDF'`, else → `'DOC'`
  - `actionLabel`: `'VID'` → `'Se video'`, else → `'Last ned'`
  - `colorBg`/`colorText`: assign based on `category` (TRAINING = purple, POLICY = blue, etc.)
- [ ] **4.3** Leave `getCertifications()` mock — no backend endpoint exists yet
- [ ] **4.4** Remove mock `mockDocuments` array and `delay()`

---

## Priority 5: Fix Alkohol Service URL + Wire

**URL bug:** Frontend uses `/api/alkohol/` but backend is at `/api/alcohol/` (English).

**Backend endpoints:**
- `POST /api/alcohol/age-verifications`
- `GET /api/alcohol/age-verifications`
- `POST /api/alcohol/incidents`  
- `GET /api/alcohol/incidents`

### Steps

- [ ] **5.1** Replace `getAlderskontrollEntries()` with `api.get('/alcohol/age-verifications')`
- [ ] **5.2** Replace `createAlderskontrollEntry()` with `api.post('/alcohol/age-verifications', data)`
- [ ] **5.3** Replace `getIncidents()` with `api.get('/alcohol/incidents')`
- [ ] **5.4** Replace `createIncident()` with `api.post('/alcohol/incidents', data)`
- [ ] **5.5** For `getStats()`: derive from the loaded lists (ageChecksToday from entries filtered to today, etc.) — no dedicated stats endpoint
- [ ] **5.6** Remove mock arrays and `setTimeout` delays

---

## Priority 6: Fix Auth `me()` + Permissions

Currently `authService.me()` just reads from sessionStorage. This means permissions are never fresh.

### Steps

- [ ] **6.1** Update `authService.me()` to call `GET /api/users/me` and return the real `UserResponse`
- [ ] **6.2** In `useAuthStore.login()`, after successful auth, call `authService.me()` to hydrate `user.permissions` (backend `UserResponse` may include permissions or they come from role)
- [ ] **6.3** Update `usePermission.ts` composable to read from `useAuthStore().user.permissions` — if undefined, fall back to role defaults

---

## Priority 7: Dashboard Response Shape Verification

The backend `DashboardController` returns `ApiResponse<DashboardResponse>` where `DashboardResponse` contains `stats`, `tasks`, `alerts`. The `api.ts` interceptor already unwraps the `ApiResponse` envelope.

The frontend `DashboardStats`, `DashboardTask`, and `DashboardAlert` types must match what the backend's `DashboardStats`, `DashboardTask`, `DashboardAlert` records contain.

### Steps

- [ ] **7.1** Read `backend/.../dashboard/api/dto/DashboardStats.java`, `DashboardTask.java`, `DashboardAlert.java` to verify field names match the frontend types
- [ ] **7.2** If field names differ, add a mapping layer in `dashboard.service.ts` (currently it passes through raw)
- [ ] **7.3** Verify the `GET /api/dashboard` path (controller maps to `/api/dashboard`, service calls `/dashboard` — this is correct since baseURL is `/api`)

---

## Priority 8: Vitest Unit Tests

Coverage target: ≥50% on meaningful logic. Focus on stores and services.

### Files to test

| Test file | What to cover |
|-----------|--------------|
| `src/stores/__tests__/auth.test.ts` | `login()` stores token+user; `logout()` clears sessionStorage; `isAuthenticated` computed |
| `src/stores/__tests__/deviations.test.ts` | `fetchAll()` populates list; `report()` adds to list; `resolve()` updates status |
| `src/stores/__tests__/readings.test.ts` | `fetchByUnit()` populates; `addReading()` optimistic update + rollback on error |
| `src/stores/__tests__/checklists.test.ts` | `fetchAll()` populates; `toggleItem()` optimistic + revert |
| `src/composables/__tests__/usePermission.test.ts` | `can('reports')` returns true for MANAGER, false for STAFF |

### Setup

- [ ] **8.1** Install `@vue/test-utils`, `vitest`, `@pinia/testing` if not present: `npm install -D vitest @vue/test-utils @pinia/testing`
- [ ] **8.2** Add `vitest.config.ts` or extend `vite.config.ts` with `test: { environment: 'jsdom', globals: true }`
- [ ] **8.3** Add `"test": "vitest run"` and `"test:coverage": "vitest run --coverage"` to `package.json` scripts
- [ ] **8.4** Mock `api.ts` in tests using `vi.mock('@/services/api')` — never hit the real backend in unit tests
- [ ] **8.5** Write auth store tests (`src/stores/__tests__/auth.test.ts`)
- [ ] **8.6** Write deviations store tests
- [ ] **8.7** Write readings store tests (including optimistic rollback path)
- [ ] **8.8** Write `usePermission` composable tests
- [ ] **8.9** Run `npm run test:coverage` — confirm ≥50%

---

## Priority 9: Cypress E2E Tests

Cypress tests run against the real deployed stack (Railway backend + Railway frontend, or locally via `npm run dev`).

### Test scenarios

| Spec file | Scenario |
|-----------|---------|
| `cypress/e2e/auth.cy.ts` | Login with valid creds → lands on dashboard; invalid creds → shows error; logout clears session |
| `cypress/e2e/temperature.cy.ts` | Log a temperature reading for Fryser #1 → reading appears in list |
| `cypress/e2e/deviations.cy.ts` | Report a new deviation → appears in open list; resolve it → moves to resolved |
| `cypress/e2e/checklists.cy.ts` | Toggle a checklist item → persists after page reload |

### Setup

- [ ] **9.1** Install Cypress: `npm install -D cypress`
- [ ] **9.2** Add `"cy:open": "cypress open"` and `"cy:run": "cypress run"` to `package.json`
- [ ] **9.3** Configure `cypress.config.ts`: `baseUrl: 'http://localhost:5173'` for local, override with Railway URL for CI
- [ ] **9.4** Add `cypress/support/commands.ts` with `cy.login(email, password)` custom command (POST to `/api/auth/login`, set token in sessionStorage)
- [ ] **9.5** Write `cypress/e2e/auth.cy.ts`
- [ ] **9.6** Write `cypress/e2e/temperature.cy.ts`
- [ ] **9.7** Write `cypress/e2e/deviations.cy.ts`
- [ ] **9.8** Write `cypress/e2e/checklists.cy.ts`

---

## Priority 10: iPad Tab Bar Simplification

**Current state:** 5 tabs — Hjem, Enheter, SJEKK, Avvik, Mer (drawer with Grafer, Opplæring, IK-Alkohol, Innstillinger)

**Goal:** Remove the "Mer" drawer entirely. Move essential secondary navigation directly into a simplified 5th tab or just remove Mer.

**Decision:** Replace "Mer" tab with direct "Alkohol" tab. Move Grafer, Opplæring, Innstillinger into the sidebar (desktop-only use cases — not needed on tablet for restaurant workers doing basic operations).

**New tablet tab bar:** Hjem | Enheter | SJEKK | Avvik | Alkohol

### Steps

- [ ] **10.1** In `AppTabBar.vue`, remove the `merOpen` ref and all "Mer" drawer markup (`.mer-backdrop`, `.mer-drawer`, `.mer-item` elements and their styles)
- [ ] **10.2** Replace the `'mer'` tab entry with `{ id: 'alkohol', label: 'Alkohol', route: 'alkohol-alderskontroll', icon: 'wine', primary: false }`
- [ ] **10.3** Add a wine glass SVG path for the `'wine'` icon (or reuse an existing simple icon)
- [ ] **10.4** Update `isActive()` to handle `tabId === 'alkohol'`
- [ ] **10.5** Remove `merItems` computed, `onTabClick` "mer" branch, `navigateMer()`, and `closeMer()` — all related to the drawer
- [ ] **10.6** Remove `tab-bar-item--mer-open` CSS class and `.mer-backdrop`, `.mer-drawer`, `.mer-item` CSS rules
- [ ] **10.7** Style the Alkohol tab: `color: #c0692a` base, `background: #92400e; color: #fff` active (amber/warning tones matching IK-Alkohol module)
- [ ] **10.8** Test: ADMIN users still reach Innstillinger via desktop sidebar (not needed on tablet)

---

## Priority 11: Architecture Diagram

The assignment requires "Diagram som viser struktur på desktop og nettbrett" (diagram showing desktop and tablet structure).

Create an SVG-based or Mermaid diagram embedded in the documentation.

### Steps

- [ ] **11.1** Create `frontend/documentation/08-architecture-diagram.md`
- [ ] **11.2** Write a **Desktop layout diagram** (Mermaid `graph LR`):
  ```
  Browser → AppSidebar (240px) + <router-view> content area
  AppSidebar → nav items (Dashboard, Fryser, Kjøleskap, SJEKK, Avvik, Grafer, Opplæring, IK-Alkohol, Innstillinger[ADMIN])
  ```
- [ ] **11.3** Write a **Tablet layout diagram** (iPad simulator, 834px, bottom tabs):
  ```
  TabletFrame → AppTabBar (bottom) + <router-view> content
  AppTabBar → [Hjem, Enheter, SJEKK, Avvik, Alkohol]
  ```
- [ ] **11.4** Write a **Frontend architecture diagram** (Mermaid `graph TD`):
  ```
  Vue Router → Views → Pinia Stores → Services → Axios (api.ts) → Railway Backend
  sessionStorage ↔ useAuthStore
  ```
- [ ] **11.5** Write a **Data flow diagram** for temperature logging:
  ```
  FreezerView → useReadingsStore.addReading() → readingsService.create() → POST /api/units/:id/readings → MySQL
  ```

---

## Priority 12: Remaining Frontend Documentation

- [ ] **12.1** Update `frontend/documentation/03-api-contracts.md` to reflect the real backend endpoints (currently has some mismatches from pre-wiring era)
- [ ] **12.2** Update `frontend/documentation/01-architecture.md` with the current tech stack and Railway deployment info
- [ ] **12.3** Document test credentials in a visible place for the assessors (currently in CLAUDE.md, should be in submitted docs)
- [ ] **12.4** Add a `frontend/documentation/09-testing.md` with:
  - How to run Vitest (`npm run test`)
  - How to run Cypress locally (`npm run cy:open`)
  - Test account credentials

---

## Implementation Order (time-boxed for 1 day)

```
AM:
  P1  Units service (~20 min)
  P2  Checklists service (~30 min)
  P3  Organization + Users service (~40 min)
  P4  Documents service (~20 min)
  P5  Alkohol URL fix + wire (~20 min)
  P6  Auth me() fix (~10 min)
  P7  Dashboard shape verify (~15 min)
  P10 iPad tab bar simplification (~20 min)

PM:
  P8  Vitest unit tests (~90 min)
  P9  Cypress E2E tests (~60 min)
  P11 Architecture diagram (~45 min)
  P12 Documentation updates (~30 min)
```

---

## Key Constraints

- **No Tailwind, no Bootstrap** — pure scoped CSS only
- **No notifications** — backend not ready
- **ApiResponse envelope** is already unwrapped by `api.ts` interceptor — services receive `.data` directly, not `{ success, message, data }`
- Backend is at `https://fullstack-production-<hash>.up.railway.app` — Vite proxy (`/api` → backend) handles local dev; production uses `VITE_API_BASE_URL`
- Keep `sessionStorage` for auth — never localStorage
