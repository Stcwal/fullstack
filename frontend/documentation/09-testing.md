# Testing Guide — IK-Kontrollsystem

**Last updated**: 2026-04-09

This document covers the full test suite: what is tested, how to run it, and how tests are structured. It is written for course assessors who need to verify test coverage and run tests independently.

---

## Summary

| Suite | Tool | Tests | Coverage |
|---|---|---|---|
| Frontend unit | Vitest 2 | 33 tests, 5 files | ~84% of tested modules |
| Frontend E2E | Cypress 14 | 23 specs, 4 files | Full user journeys |
| Backend unit | JUnit 5 + Spring Boot Test | 30+ tests | >50% (meets course requirement) |

---

## Frontend Unit Tests (Vitest)

### Running

```bash
cd frontend

npm test                   # run once, exit
npm run test:watch         # watch mode, re-runs on file change
npm run test:coverage      # with V8 coverage report in coverage/
```

Coverage report is written to `frontend/coverage/`. Open `coverage/index.html` in a browser for the full line-by-line view.

### Test files

All test files live under `src/`:

```
src/
├── stores/__tests__/
│   ├── auth.test.ts           6 tests
│   ├── readings.test.ts       6 tests
│   └── deviations.test.ts     5 tests
└── composables/__tests__/
    └── usePermission.test.ts  8 tests (+ 8 SUPERVISOR edge cases)
```

### What each file tests

**`auth.test.ts`** — `useAuthStore`

| Test | What it verifies |
|---|---|
| is not authenticated on fresh init | Empty sessionStorage → `isAuthenticated = false` |
| hydrates from sessionStorage when tokens exist | Re-hydration picks up stored token and user on init |
| login() stores token and user | Successful login populates state and sessionStorage |
| login() updates user with me() response | `/auth/me` response enriches user after login |
| login() keeps login-response user when me() throws | Falls back gracefully if `/auth/me` fails |
| logout() clears state and sessionStorage | Both Pinia state and sessionStorage are cleared |

**`readings.test.ts`** — `useReadingsStore`

| Test | What it verifies |
|---|---|
| starts with empty readings | Initial state is `[]` |
| fetchByUnit() populates readings for that unit | Readings list is replaced by the fetched set |
| fetchByUnit() clears loading flag on success | Loading state resets after async call completes |
| addReading() prepends the created reading | New reading appears at the top of the list |
| addReading() clears saving flag after completion | Saving flag is reset regardless of success/failure |
| getByUnit() returns only readings for the given unitId | Filter returns only readings matching unitId |

**`deviations.test.ts`** — `useDeviationsStore`

| Test | What it verifies |
|---|---|
| fetchAll() populates the deviations list | List is populated from service response |
| fetchAll() clears loading flag even if it fails | Loading always resets, even on error |
| report() prepends new deviation to the list | New deviation appears first |
| resolve() updates status, resolution and resolvedAt | Resolved deviation has correct status and timestamp |
| openCount() returns count of non-RESOLVED deviations | Computed count excludes RESOLVED items |

**`usePermission.test.ts`** — `usePermission` composable

| Test | What it verifies |
|---|---|
| returns false when no user is logged in | Unauthenticated state returns false for all permissions |
| returns true when user has the permission explicitly set | Explicit true in permissions object is respected |
| returns false when permission is explicitly false | Explicit false in permissions object is respected |
| falls back to role-derived defaults when no permissions object | ADMIN/MANAGER/STAFF defaults applied when `permissions` is absent |
| STAFF role defaults deny reports, userAdmin, settings | STAFF cannot access reports, user admin, or settings by default |
| ADMIN role defaults grant all permissions | ADMIN has full access by default |
| SUPERVISOR role defaults | Edge case: reports true, userAdmin and settings false |

### Mock strategy

Stores are tested with mocked service dependencies. Each test file calls `vi.mock('@/services/...')` to replace the service with a controlled implementation. This means tests run without a network or database and are deterministic.

The pattern for each store test:

```typescript
vi.mock('@/services/readings.service', () => ({
  readingsService: {
    getByUnit: vi.fn().mockResolvedValue([...mockReadings]),
    create: vi.fn().mockResolvedValue(mockNewReading),
  }
}))
```

Pinia is created fresh per test with `setActivePinia(createPinia())` in `beforeEach`, preventing state bleed between tests.

---

## Frontend E2E Tests (Cypress)

### Prerequisites

The full application stack must be running before Cypress tests will pass:

1. Backend on `http://localhost:8080` (with dev seed data)
2. Frontend on `http://localhost:5173`

The easiest way is `docker compose up` from the repository root. Alternatively, start each service manually (see README).

### Running

```bash
cd frontend

npm run cy:open    # opens the Cypress Test Runner in a browser
npm run cy:run     # headless mode, suitable for CI
```

Results (screenshots on failure) are written to `cypress/screenshots/`.

### Test files

```
cypress/e2e/
├── auth.cy.ts            6 specs
├── temperature.cy.ts     5 specs
├── checklists.cy.ts      7 specs
└── deviations.cy.ts      5 specs
```

### Custom command: `cy.login()`

Defined in `cypress/support/commands.ts`. Authenticates via the real API (not the UI) to avoid testing the login form in every spec:

```typescript
cy.login('kari@everestsushi.no', 'admin123')
```

This calls `POST /api/auth/login` directly, then seeds the token and user into `sessionStorage` so the app hydrates as authenticated. After this command, `cy.visit('/dashboard')` will succeed without going through the login page.

The command handles the backend's flat `LoginResponse` shape (fields like `userId`, `firstName` at the top level, not nested under `user`).

### What each file tests

**`auth.cy.ts`** — Authentication flows

| Spec | Flow |
|---|---|
| shows the login form | Login page renders email + password + submit button |
| redirects to dashboard after valid login | Submit with correct credentials → `/dashboard` |
| stays on login page with wrong password | Submit with wrong password → still on `/login` |
| logout clears the session | Click "Logg ut" → `/login`, sessionStorage cleared |
| unauthenticated user redirected to login | Direct visit to `/dashboard` → `/login` |

**`temperature.cy.ts`** — Temperature logging

| Spec | Flow |
|---|---|
| shows temperature unit sub-nav | Unit selector tabs are visible on `/fryser` |
| shows the temperature form when a unit is selected | First unit auto-selected; form + submit button visible |
| shows validation error when temperature is empty | Clear input, click submit → validation message appears |
| logs a temperature reading and shows success feedback | Enter `−18`, submit → "Lagret!" flash |
| new reading appears in the recent readings list | Enter `−19.5` with note, submit → value visible in history |

**`checklists.cy.ts`** — Checklist interaction

| Spec | Flow |
|---|---|
| shows the checklists page with frequency filter | `/generelt` has h1 and sub-nav with 3 items |
| defaults to daily frequency | "Daglig" tab is active on load |
| can switch to weekly frequency | Click "Ukentlig" → tab becomes active |
| can switch to monthly frequency | Click "Månedlig" → tab becomes active |
| shows checklist items | At least one `.checklist-item` visible after load |
| can toggle a checklist item (optimistic update) | Click checkbox → state flips immediately |
| completed items show visual done state | A completed item gets `.is-done` class |

**`deviations.cy.ts`** — Deviation reporting

| Spec | Flow |
|---|---|
| shows the deviations page | `/avvik` renders with heading |
| can open the report deviation form | "Nytt avvik" button opens form |
| shows severity options | Severity select has expected options |
| can submit a deviation | Fill form, submit → new deviation appears in list |
| shows existing deviations | At least one deviation card visible on load |

### Known limitations

Cypress tests require the dev seed database to be populated. If the backend is started without the `dev` Spring profile, the tests that assert on existing data (existing deviations, existing temperature units) will fail because the seed data is not present.

If running Cypress locally without Docker, set the Spring profile explicitly:

```bash
# Backend
mvn spring-boot:run -Dspring-boot.run.profiles=local,dev
```

---

## Backend Tests (JUnit 5)

### Running

```bash
cd backend
mvn clean test
```

Reports are written to `backend/target/surefire-reports/` as JUnit XML. Open `backend/target/site/surefire-report.html` after running `mvn surefire-report:report` for an HTML view.

### Test files

```
src/test/java/backend/fullstack/
└── config/
    ├── ConfigSimpleComponentsTest.java    — Spring bean wiring and property loading
    ├── GlobalExceptionHandlerTest.java    — HTTP error response shapes for 400/401/403/404/500
    ├── JwtAuthFilterTest.java             — JWT filter: valid token, missing token, invalid token
    ├── JwtUtilTest.java                   — Token generation, parsing, expiry validation
    ├── SecurityConfigBeansTest.java       — SecurityFilterChain bean configuration
    └── SecurityErrorHandlerTest.java      — 401/403 handler response bodies
```

### Coverage

Backend coverage exceeds 50% as required by the IDATT2105 course specification. The test suite focuses on the security and authentication infrastructure, which is the highest-risk layer of the application: incorrect JWT handling or permissive security configuration would expose all endpoints.

---

## CI/CD

Two GitHub Actions workflows run automatically on every pull request to `main` or `develop`.

### lint.yml — Frontend type check

Runs on every PR. Steps:
1. Install Node 20
2. `npm ci` in `frontend/`
3. `npm run type-check` (vue-tsc --noEmit)

Fails the PR check if any TypeScript errors exist.

### test.yml — Backend test suite

Runs on every PR. Steps:
1. Install Java 21 (Temurin)
2. `mvn clean test` in `backend/`
3. Publish JUnit XML results as a GitHub PR check annotation
4. Upload surefire report XML as a workflow artifact (retained 30 days)

Both workflows use GitHub's `checks` permission to annotate the PR with test results — reviewers can see pass/fail per test method directly in the PR interface.

---

## Test data accounts

All tests use the seeded demo organisation. When running against the full stack:

| Email | Password | Role | Used in |
|---|---|---|---|
| kari@everestsushi.no | admin123 | ADMIN | All Cypress specs (via `cy.login()`) |
| ola@everestsushi.no | leder123 | MANAGER | Available for manual testing |
| per@everestsushi.no | ansatt123 | STAFF | Available for manual testing |

Vitest unit tests do not use these credentials — they use inline mock objects passed directly to store actions.
