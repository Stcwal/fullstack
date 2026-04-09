# Prioritization and Development Process

**Project**: IK-Kontrollsystem — IDATT2105 Fullstack, NTNU  
**Team**: Dennis Moe (frontend), Stian Walmann (DevOps + backend), Oleander Tengesdal (backend), Sindre Jentoft Bøe (backend)

---

## How we approached the project

### Phase 1 — Planning and role division (March 20)

Before writing any code, the group spent time mapping the problem space. The assignment described two compliance modules (IK-Mat, IK-Alkohol) and a list of possible features. We worked through these together and identified what was genuinely essential versus optional.

We decided early to split ownership cleanly:
- **Dennis** owned the entire frontend — Vue 3, UI/UX, routing, state management, design system
- **Stian** owned DevOps infrastructure (Docker, CI/CD, deployment pipeline) and backend foundations
- **Oleander** and **Sindre** owned backend domain features (deviations, temperature readings, alcohol module, reports, tests)

This split kept dependencies minimal. The frontend would work from mock data until the backend was ready, and the backend team could build and test endpoints independently.

### Phase 2 — Wireframe (March 20)

Dennis created an interactive HTML wireframe for the full application before any Vue code was written. The wireframe went through several iterations to settle on the information architecture: which views existed, what each contained, how navigation worked, and which data appeared where.

The final wireframe (`ik_system_wireframe_norsk_v3.html`) covered all eight primary views: dashboard, freezer logging, fridge logging, checklists, deviations, temperature graphs, training, and settings. It was the contract between Dennis's implementation work and the backend team's API design.

Simultaneously, Stian set up the backend Maven project, Docker infrastructure, MySQL, and Swagger — the scaffolding the backend team would build on.

### Phase 3 — Frontend MVP (March 29)

With the wireframe as a guide, Dennis built the entire frontend in a focused sprint: project setup and design system, authentication with sessionStorage and route guards, the layout system (desktop sidebar + tablet simulator), temperature logging, checklists, deviations, dashboard, and temperature charts. All features ran on mock data with simulated API responses.

The mock data layer was deliberately structured so that swapping to real API calls later would be a one-line change per service method — the real Axios call commented out next to the mock return.

### Phase 4 — Realization about kitchen UX

After building the initial dashboard-centric design, a practical problem became apparent: the overview dashboard is not how kitchen staff actually interact with compliance systems. Having worked six years in a café, Dennis recognized that kitchen workers don't sit at a computer — they glance at a wall-mounted tablet between tasks, log what they need to log in as few taps as possible, and move on.

This drove the development of the tablet view: a full-screen iPad simulator mode with a sticky bottom tab bar, color-coded per section, large touch targets, and a dedicated quick-tap temperature logging view. The design shift prioritized immediacy and clarity over information density. The desktop view was retained for managers and administrators who work at a desk.

### Phase 5 — Backend domain features (April 8–9)

Sindre implemented temperature reading statistics, deviation comments, and extended the deviation domain. Oleander built the IK-Alkohol module (age verifications, serving incidents, alcohol licenses) and the PDF/JSON export functionality. Stian handled security tests and exception handling.

Dennis wired the frontend services to the real backend, resolved CORS issues, fixed Flyway migration compatibility, and integrated the auth flow with the actual JWT response shape from Spring Boot.

### Phase 6 — Shift attribution and polish (April 9)

A late addition based on real operational insight: when a manager logs a reading on behalf of a kitchen worker, the system should record which worker performed the task, not just who was logged in. The shift worker selector was added to temperature logging, checklist toggling, and deviation reporting. This does not change permissions — it is attribution only.

The IK-Alkohol tablet interface received a quick-tap age verification form optimized for one-handed use during service.

### Phase 7 — Tests and documentation (April 9)

Vitest unit tests were written for the stores and composables most critical to correctness (auth hydration, readings, deviations, permissions). Cypress E2E tests were written for the four main user flows. Documentation was written to cover architecture, API contracts, feature behavior, and a testing guide for assessors.

---

## Prioritization decisions

### What we prioritized and why

| Priority | Feature | Reason |
|---|---|---|
| 1 | Authentication + route guards | Everything else depends on knowing who is logged in and what they can access |
| 2 | Temperature logging | The most time-critical daily task for food safety compliance |
| 3 | Checklists | The second core operational feature used every shift |
| 4 | Deviations | Required for HACCP compliance — non-conformances must be traceable |
| 5 | Tablet/mobile UX | Kitchen staff do not use desktops; a desktop-only UI would fail in practice |
| 6 | IK-Alkohol module | Separate compliance domain, important but secondary to IK-Mat core |
| 7 | Admin settings (units, users, templates) | Needed for the system to be self-configuring, but not blocking daily use |
| 8 | Tests | Required by the course spec (>50% coverage); added after features were stable |
| 9 | Export (PDF/JSON) | Compliance reporting is valuable but not part of daily operations |

### What we chose not to fully implement

| Feature | Status | Reason |
|---|---|---|
| PDF/JSON export | Stub (endpoint exists, no file download UI) | Low daily operational value; deprioritized in favor of core logging flows |
| Document upload | Read-only (download links exist) | Storage backend complexity; not blocking for the MVP |
| Token refresh flow | Not implemented | 401 triggers re-login; acceptable for a short-session school project |
| Pagination on long lists | Not implemented | Demo dataset is small; would add complexity without visible benefit |
| Real-time notifications | Infrastructure stub only | Requires WebSocket or polling; out of scope for the delivery |

---

## Team division of commits

| Developer | Primary contributions |
|---|---|
| Dennis Moe | All frontend code, design system, tablet UI, frontend-backend wiring, Cypress + Vitest tests, documentation, Flyway migration fixes, CORS configuration |
| Stian Walmann | DevOps (Docker, CI/CD), backend auth (JWT, Spring Security), exception handling, Swagger, backend security tests, deployment |
| Oleander Tengesdal | IK-Alkohol module (age verifications, incidents, licenses), PDF/JSON export, backend test coverage |
| Sindre Jentoft Bøe | Temperature reading statistics, deviation comments and status flow, backend unit tests |
