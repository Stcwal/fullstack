# Architecture Diagrams — IK-Kontrollsystem

All diagrams use [Mermaid](https://mermaid.js.org/) syntax. GitHub renders these natively in markdown files.

---

## 1. System Architecture

High-level view of how the full stack fits together — browser through to database.

```mermaid
flowchart TD
    subgraph client["Client"]
        Browser["Browser / Kitchen iPad"]
    end

    subgraph fe["Frontend — Vue 3 (Vite :5173)"]
        Router["Vue Router\nbeforeEach auth + admin guards"]
        Stores["Pinia Stores\nauth · units · readings · checklists\ndeviations · shift · alkohol · dashboard"]
        Services["Service Layer\nAxios instance\nJWT injector · 401 handler\nApiResponse envelope unwrapper"]
    end

    subgraph be["Backend — Spring Boot (:8080)"]
        Filter["JwtAuthFilter\nSpring Security filter chain"]
        Controllers["REST Controllers  /api/**\nauth · units · readings · checklists\ndeviations · alcohol · dashboard\nusers · organization · export · documents"]
        ServiceLayer["Domain Service Layer\nmulti-tenant org scoping"]
        Repos["Spring JPA Repositories"]
    end

    subgraph db["Database"]
        MySQL[("MySQL 8\nFlyway schema migrations\ndev seed data")]
    end

    subgraph cicd["CI/CD — GitHub Actions"]
        Lint["lint.yml\nvue-tsc type-check on PR"]
        Tests["test.yml\nmvn clean test on PR"]
    end

    Browser --> Router
    Router --> Stores
    Stores --> Services
    Services -- "Authorization: Bearer {token}" --> Filter
    Filter -- "JwtPrincipal\nuserId · role · organizationId" --> Controllers
    Controllers --> ServiceLayer
    ServiceLayer --> Repos
    Repos --> MySQL
```

---

## 2. Backend Domain Model

Entity relationships across the full backend domain.

```mermaid
erDiagram
    ORGANIZATION ||--o{ USER : "employs"
    ORGANIZATION ||--o{ LOCATION : "operates at"
    ORGANIZATION ||--o{ TEMPERATURE_UNIT : "owns"
    ORGANIZATION ||--o{ CHECKLIST_TEMPLATE : "defines"
    ORGANIZATION ||--o{ DEVIATION : "tracks"
    ORGANIZATION ||--o{ DOCUMENT : "stores"
    ORGANIZATION ||--o{ ALCOHOL_LICENSE : "holds"
    ORGANIZATION ||--o{ AGE_VERIFICATION_LOG : "records"
    ORGANIZATION ||--o{ ALCOHOL_SERVING_INCIDENT : "records"

    LOCATION ||--o{ TEMPERATURE_UNIT : "contains"

    USER ||--o{ TEMPERATURE_READING : "records"
    USER ||--o{ CHECKLIST_INSTANCE_ITEM : "completes"
    USER ||--o{ DEVIATION : "reports"
    USER ||--o{ DEVIATION_COMMENT : "writes"
    USER ||--o{ TRAINING_RECORD : "has"
    USER ||--o{ AGE_VERIFICATION_LOG : "performs"

    TEMPERATURE_UNIT ||--o{ TEMPERATURE_READING : "has"
    TEMPERATURE_READING |o--o| DEVIATION : "may trigger"

    CHECKLIST_TEMPLATE ||--o{ CHECKLIST_TEMPLATE_ITEM : "contains"
    CHECKLIST_TEMPLATE ||--o{ CHECKLIST_INSTANCE : "generates"
    CHECKLIST_INSTANCE ||--o{ CHECKLIST_INSTANCE_ITEM : "has"

    DEVIATION ||--o{ DEVIATION_COMMENT : "has"
```

---

## 3. Frontend Route Structure

All application routes, access guards, and which component renders at each path.

```mermaid
flowchart TD
    Root(["/"])
    Root -->|redirect| DB

    Login["/login\nLoginView\npublic"]

    Guard{"beforeEach\nauth guard\nrequiresAuth"}

    Root --> Guard
    Guard -->|no token| Login
    Login -->|valid login| Guard

    Guard -->|authenticated| DB["/dashboard\nDashboardView"]
    Guard -->|authenticated| FR["/fryser\nFreezerView"]
    Guard -->|authenticated| KJ["/kjoeleskap\nFridgeView"]
    Guard -->|authenticated| GE["/generelt\nChecklistView"]
    Guard -->|authenticated| AV["/avvik\nDeviationsView"]
    Guard -->|authenticated| GR["/grafer\nGraphView"]
    Guard -->|authenticated| OP["/opplaering\nTrainingView"]

    Guard -->|authenticated| ALK["/alkohol\nAlkoholView"]
    ALK --> ALK1["Alderskontroll tab"]
    ALK --> ALK2["Sjekklister tab"]
    ALK --> ALK3["Hendelseslogg tab"]

    Guard -->|authenticated| AdminGuard{"role ===\nADMIN?"}
    AdminGuard -->|"MANAGER / STAFF"| DB
    AdminGuard -->|ADMIN| INN["/innstillinger\nSettingsView"]
    INN -->|default| EN["/enheter\nUnitsTab"]
    INN --> BR["/brukere\nUsersTab"]
    INN --> OR["/org\nOrgTab"]
    INN --> SJ["/sjekklister\nChecklistsTab"]
```

---

## 4. User Flow — Staff: Temperature Logging

Typical path when a kitchen worker logs a temperature reading at the start of a shift.

```mermaid
flowchart TD
    A([Worker arrives, picks up tablet]) --> B{Session active?}
    B -->|No| C["/login — enter credentials"]
    C --> D[POST /api/auth/login\nJWT stored in sessionStorage]
    B -->|Yes| E
    D --> E["/dashboard — check alerts"]
    E --> F{Temperature alert shown?}
    F -->|Yes — unit flagged| G[Navigate to /fryser or /kjoeleskap]
    F -->|No| G
    G --> H[Select unit from sub-nav]
    H --> I[Enter temperature reading\noptional: select shift worker]
    I --> J{Valid number entered?}
    J -->|No| K[Inline validation error\n'Temperatur er påkrevd']
    K --> I
    J -->|Yes| L[POST /api/units/:id/readings]
    L --> M{'Lagret!' flash — reading appears in history}
    M --> N{Reading out of range?}
    N -->|Yes — isOutOfRange| O[Alert banner shown\nSiste måling utenfor grenseverdi]
    O --> P[Navigate to /avvik\nReport deviation]
    P --> Q[POST /api/deviations]
    N -->|No| R([Done — continue with shift])
    Q --> R
```

---

## 5. User Flow — Staff: Daily Checklist Completion

How a worker works through the daily opening checklist.

```mermaid
flowchart TD
    A([Start of shift]) --> B[Navigate to /generelt]
    B --> C[Daglig tab — default on load]
    C --> D[Checklist cards load\nGET /api/checklists/instances?frequency=DAILY]
    D --> E{Any items incomplete?}
    E -->|No| F([All done — checklist shows 100%])
    E -->|Yes| G[Tick a checkbox]
    G --> H[Optimistic update:\ncheckbox flips instantly\nprogress bar animates]
    H --> I[PATCH /api/checklists/instances/:id/items/:itemId]
    I --> J{API success?}
    J -->|Yes| K{All items now done?}
    J -->|No — network error| L[State reverts to previous\ncheckbox flips back]
    L --> G
    K -->|No| G
    K -->|Yes| M[Completion footer appears\nFullført av: Name — time]
    M --> F
```

---

## 6. User Flow — Manager: Deviation Lifecycle

Full lifecycle of a reported non-conformance from discovery to resolution.

```mermaid
flowchart TD
    A([Deviation discovered]) --> B{Source?}
    B -->|Automatic — temp reading out of range| C[Alert shown on /fryser or /kjoeleskap]
    B -->|Manual — staff notices issue| D[Navigate to /avvik]
    C --> D
    D --> E[Click 'Nytt avvik']
    E --> F[Fill report form\nTitle · Description · Severity · Module]
    F --> G[POST /api/deviations]
    G --> H[Deviation created\nstatus: OPEN\nappears top of list]
    H --> I{Manager reviews}
    I -->|Begins investigation| J[Status → IN_PROGRESS\nPATCH /api/deviations/:id/status]
    J --> K{Issue resolved?}
    K -->|Yes| L[Click 'Løs avvik'\nEnter resolution text]
    L --> M[PATCH /api/deviations/:id/status\nstatus: RESOLVED]
    M --> N([Deviation archived\nTimestamp + resolution recorded])
    K -->|No — escalate| O[Add comment\nPOST /api/deviations/:id/comments]
    O --> K
```

---

## 7. User Flow — Admin: Setting Up a New Checklist Template

How an administrator creates a checklist that staff will use going forward.

```mermaid
flowchart TD
    A([Admin logged in]) --> B[Navigate to /innstillinger/sjekklister]
    B --> C[GET /api/checklists/templates\nAll templates load]
    C --> D[Click '+ Legg til ny mal']
    D --> E[Modal opens — empty form]
    E --> F[Enter title]
    F --> G[Select frequency\nDAILY / WEEKLY / MONTHLY]
    G --> H[Add checklist items\nclick '+ Legg til punkt' per item]
    H --> I[Click 'Lagre']
    I --> J{Validation passes?}
    J -->|No title or no items| K[Inline error message]
    K --> F
    J -->|Yes| L[POST /api/checklists/templates\ntitle · frequency · itemTexts array]
    L --> M[Template added to list]
    M --> N([Backend generates today's instance\nStaff see new checklist immediately])
```

---

## 8. Authentication and Session Flow

How the JWT session is established, maintained, and terminated.

```mermaid
sequenceDiagram
    participant User
    participant Vue as Vue App
    participant Store as authStore (Pinia)
    participant API as Axios (api.ts)
    participant BE as Spring Boot

    User->>Vue: Enter email + password
    Vue->>Store: login(email, password)
    Store->>API: POST /api/auth/login
    API->>BE: { email, password }
    BE-->>API: { token, userId, firstName, role, ... }
    API-->>Store: normalised { token, user }
    Store->>Store: sessionStorage.setItem('token')\nsessionStorage.setItem('user')
    Store-->>Vue: isAuthenticated = true
    Vue->>User: Redirect to /dashboard

    Note over API,BE: Every subsequent request
    User->>Vue: Navigate / perform action
    Vue->>API: service call
    API->>API: interceptor reads sessionStorage\nadds Authorization: Bearer {token}
    API->>BE: request with JWT header
    BE->>BE: JwtAuthFilter validates token\nextract organizationId, role
    BE-->>API: ApiResponse<T> envelope
    API->>API: interceptor unwraps .data field
    API-->>Vue: typed payload

    Note over API,BE: On token expiry
    BE-->>API: 401 Unauthorized
    API->>API: 401 interceptor fires\nclear sessionStorage
    API->>User: Hard redirect to /login
```
