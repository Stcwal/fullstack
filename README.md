# IK-Kontrollsystem

Digital internal control system for Norwegian restaurants. Replaces paper-based logbooks for food safety (IK-Mat) and alcohol service compliance (IK-Alkohol) with a structured, role-aware web application.

Built for **Everest Sushi & Fusion AS**, Innherredsveien 1, Trondheim, as part of IDATT2105 Full-Stack Application Development at NTNU.

**Team:** Dennis Moe · Oleander Tengesdal · Stian Walmann · Sindre Jentoft Bøe

**Live demo:** https://stain.no hosted on Railway — log in with the test credentials below.

---

## What it does

- **Temperature logging** — Daily freezer and refrigerator readings with automatic out-of-range detection
- **Checklists** — Daily, weekly, and monthly task lists for kitchen opening, closing, and HACCP compliance
- **Deviations** — Report, track, and resolve non-conformances with severity levels and a resolution workflow
- **Temperature graphs** — 7-day and 30-day chart views of all storage unit readings with alert markers
- **Training documents** — Employee certification tracking and document library
- **IK-Alkohol module** — Age-check logs, beverage storage inspections, and bartender compliance checklists
- **Admin settings** — Storage unit CRUD, user management with per-user permissions, organisation profile, and checklist template management

---

## Tech stack

| Layer | Technology |
|---|---|
| Frontend | Vue 3 (Composition API, `<script setup lang="ts">`) + Vite |
| State | Pinia |
| Routing | Vue Router 4 |
| HTTP | Axios with JWT interceptors |
| Charts | Chart.js + vue-chartjs |
| CSS | Hand-crafted design system — no Tailwind, no Bootstrap |
| Backend | Java 21 + Spring Boot 3 + Spring Security + JWT |
| Database | MySQL 8 with Flyway migrations |
| Build | Maven 3.9 |
| Tests | Vitest (frontend unit), Cypress (frontend E2E), JUnit 5 (backend) |
| CI/CD | GitHub Actions — lint on PR, backend tests on PR |
| Deployment | Docker Compose |

---

## Running the project

### Docker (recommended — runs everything with one command)

Requirements: Docker Desktop (or Docker Engine + Compose plugin).

```bash
cp .env.example .env
docker compose up --build
```

The `.env.example` file contains working defaults — no edits needed for a local demo run. The first build takes a few minutes while Maven downloads dependencies.

Services after startup:
- Frontend: http://localhost:5173
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- MySQL: localhost:3306

> **Note:** Seed data (demo users, units, readings, deviations) is loaded automatically on first startup via Flyway migrations. Login credentials are listed below.

### Local development (without Docker)

**Backend**

Requirements: Java 21, Maven 3.9, MySQL 8 running locally.

Create the database and user:

```sql
CREATE DATABASE ik_kontroll CHARACTER SET utf8mb4;
CREATE USER 'ik_user'@'localhost' IDENTIFIED BY 'ik_pass';
GRANT ALL PRIVILEGES ON ik_kontroll.* TO 'ik_user'@'localhost';
```

Start with the `local` and `dev` Spring profiles (`dev` runs seed migrations, `local` provides the datasource URL):

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local,dev
```

The backend starts on port 8080. Flyway runs schema and seed migrations automatically on first start.

**Frontend**

Requirements: Node 20+.

```bash
cd frontend
npm install
npm run dev
```

The frontend starts on http://localhost:5173. Vite proxies all `/api` requests to `http://localhost:8080`, so no CORS configuration is needed in development.

### Environment variables reference

| Variable | Required | Description |
|---|---|---|
| `MYSQL_ROOT_PASSWORD` | Docker only | MySQL root password for the database container |
| `MYSQL_DATABASE` | Docker only | Database name (default: `ik_kontroll`) |
| `MYSQL_USER` | Docker only | App database user (default: `ik_user`) |
| `MYSQL_PASSWORD` | Docker only | App database password (default: `ik_pass`) |
| `JWT_SECRET` | Production | JWT signing secret — must be 32+ characters |
| `MAIL_USERNAME` | Optional | Gmail address for sending invite emails |
| `MAIL_PASSWORD` | Optional | Gmail App Password (not your account password) |

### Deploying to Railway (or other cloud platforms)

Set the following environment variables on the backend service. If using Railway's MySQL plugin, reference its variables for the datasource:

```
SPRING_PROFILES_ACTIVE=dev
SPRING_DATASOURCE_URL=jdbc:mysql://<host>:<port>/<database>?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8&useUnicode=true
SPRING_DATASOURCE_USERNAME=<db-user>
SPRING_DATASOURCE_PASSWORD=<db-password>
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
JWT_SECRET=<random-32-char-secret>
MAIL_USERNAME=noreply.iksys@gmail.com
MAIL_PASSWORD=<gmail-app-password>
```

---

## Test credentials

All accounts belong to the demo organisation "Everest Sushi & Fusion AS".

| Email | Password | Role | Access level |
|---|---|---|---|
| kari@everestsushi.no | admin123 | ADMIN | Full access including settings, user management, and checklist template management |
| ola@everestsushi.no | leder123 | MANAGER | All operational views — temperature logging, checklists, deviations, graphs, training |
| per@everestsushi.no | ansatt123 | STAFF | Core operational views — temperature logging, checklists, deviations |

---

## Running tests

### Frontend unit tests (Vitest)

33 tests covering auth store hydration, readings store, deviations store, and the `usePermission` composable.

```bash
cd frontend
npm test                    # run once
npm run test:watch          # watch mode
npm run test:coverage       # with coverage report
```

Coverage is approximately 84% across tested modules (stores and composables).

### Frontend E2E tests (Cypress)

23 specs across 4 test files. Requires the full application stack (frontend + backend + seeded database) to be running.

```bash
# Start the stack first, then:
cd frontend
npm run cy:open    # interactive browser mode
npm run cy:run     # headless CI mode
```

Test files:
- `cypress/e2e/auth.cy.ts` — login, logout, redirect guards
- `cypress/e2e/temperature.cy.ts` — unit selection, form validation, successful reading submission
- `cypress/e2e/checklists.cy.ts` — frequency filtering, item toggling, optimistic updates
- `cypress/e2e/deviations.cy.ts` — report form, severity selection, list display

### Backend tests (JUnit 5 + Spring Boot Test)

```bash
cd backend
mvn clean test
```

Test reports are written to `backend/target/surefire-reports/`. Coverage exceeds 50% as required by the course specification.

### CI/CD

Two GitHub Actions workflows run on every pull request to `main`:

- **lint.yml** — TypeScript type-check (`npm run type-check`) on the frontend
- **test.yml** — `mvn clean test` on the backend, publishes JUnit XML results as PR check

---

## Project structure

```
/
├── .github/workflows/      GitHub Actions CI
├── backend/                Spring Boot application
│   ├── src/main/java/      Feature-package architecture
│   │   ├── auth/           JWT authentication
│   │   ├── checklist/      Checklist templates + instances
│   │   ├── temperature/    Temperature readings
│   │   ├── deviations/     Deviation tracking
│   │   ├── alcohol/        IK-Alkohol compliance
│   │   ├── units/          Storage unit management
│   │   ├── user/           User management + permissions
│   │   ├── dashboard/      Aggregated summary data
│   │   ├── reports/        PDF/JSON export
│   │   └── config/         Security, JWT, Swagger
│   └── src/main/resources/
│       ├── db/migration/       Flyway schema migrations
│       └── db/migration-dev/   Development seed data
├── frontend/               Vue 3 application
│   ├── src/
│   │   ├── views/          Page components (one per route)
│   │   ├── stores/         Pinia state stores
│   │   ├── services/       API abstraction layer
│   │   ├── types/          TypeScript interfaces
│   │   ├── components/     Shared UI components
│   │   └── assets/main.css Design system (CSS custom properties)
│   ├── cypress/            E2E tests
│   └── documentation/      Frontend architecture docs
├── db/                     Database Dockerfile
└── docker-compose.yaml     Full-stack Docker setup
```

---

## API documentation

When the backend is running, Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```

The OpenAPI spec (JSON) is at `http://localhost:8080/v3/api-docs`.

---

## Architecture and design decisions

Detailed documentation lives in `frontend/documentation/`:

| File | Contents |
|---|---|
| `08-architecture-diagrams.md` | **Architecture diagrams** — system overview, backend domain model (ER), frontend route structure, 5 user flow diagrams, JWT session sequence |
| `10-prioritization.md` | **Prioritization and development process** — how the team approached the project, phase-by-phase decisions, what was built and why, what was intentionally left out |
| `01-architecture.md` | System architecture, directory structure, layout modes, state management patterns, key design decisions |
| `02-auth-permissions.md` | JWT authentication flow, role-based access control, two-layer permissions system |
| `03-api-contracts.md` | Full REST API contract — every endpoint, request/response shape, error codes |
| `04-features/` | Feature-level documentation for each module (temperature, checklists, deviations, graphs, training, settings) |
| `05-design-system.md` | CSS design system, tokens, component classes |
| `06-state-management.md` | Pinia store patterns, data flow |
| `09-testing.md` | Testing guide — how to run all tests, what each test suite covers, known limitations |

---

## Security

- JWT stored in `sessionStorage` (not `localStorage`) — sessions expire when the browser tab is closed, per assignment specification section 4.7
- All API endpoints require `Authorization: Bearer {token}` except `POST /api/auth/login`
- Multi-tenant isolation enforced at the backend — every query is scoped to the authenticated user's `organizationId` from the JWT claims
- Input validation using Spring Validation (`@Valid`, `@NotBlank`, `@Size`, etc.) on all request bodies
- CORS configured for the frontend origin
- SQL injection prevented by JPA prepared statements
- XSS prevention via Content-Security-Policy response headers

---

## Course context

**Course**: IDATT2105 Full-Stack Application Development  
**Institution**: NTNU  
**Deadline**: Friday 10 April 2026, kl. 14:00 (Inspera)  
**Client**: Everest Sushi & Fusion AS (org.nr. 937 219 997)
