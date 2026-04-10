# Backend API-krav — IK-kontrollsystem

**Prosjekt:** IDATT2105 Fullstack — Everest Sushi & Fusion AS  
**Fra:** Frontend-team  
**Til:** Backend/DevOps  
**Stack:** Java 21 + Spring Boot 3 | MySQL 8 | JWT + Spring Security  
**Arkitektur:** Microservices (feature-based packages)  
**Frontend:** Vue 3 + TypeScript  

---

## 1. Autentisering og autorisasjon

### Krav
- JWT-basert autentisering (access + refresh token)
- Tre roller: `ADMIN`, `MANAGER`, `STAFF`
- Multi-tenant: hver request scopes til brukerens `organizationId`
- Session storage på frontend for kort-levd sesjon (ref. kravspekk 4.7)

### Endepunkter

| Metode | Sti | Beskrivelse | Roller |
|--------|-----|-------------|--------|
| POST | `/api/auth/login` | Innlogging, returnerer JWT-par | Alle |
| POST | `/api/auth/refresh` | Forny access token | Alle |
| POST | `/api/auth/logout` | Invaliderer refresh token | Alle |
| GET | `/api/auth/me` | Returnerer innlogget bruker + rolle + org | Alle |

### Respons `POST /api/auth/login`
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "user": {
    "id": 1,
    "firstName": "Kari",
    "lastName": "Larsen",
    "email": "kari@everestsushi.no",
    "role": "ADMIN",
    "organizationId": 1
  }
}
```

---

## 2. Brukerhåndtering

### Krav
- CRUD på brukere, scoped til organisasjon
- Kun ADMIN kan opprette/endre/deaktivere brukere
- Rolletildeling og tilgangsstyring per bruker
- Passordresetfunksjon

### Endepunkter

| Metode | Sti | Beskrivelse | Roller |
|--------|-----|-------------|--------|
| GET | `/api/users` | Liste over alle brukere i organisasjon | ADMIN, MANAGER |
| GET | `/api/users/{id}` | Hent en bruker | ADMIN, MANAGER |
| POST | `/api/users` | Opprett ny bruker | ADMIN |
| PUT | `/api/users/{id}` | Oppdater bruker (navn, e-post, rolle, status) | ADMIN |
| PATCH | `/api/users/{id}/role` | Endre rolle | ADMIN |
| PATCH | `/api/users/{id}/status` | Aktiver/deaktiver bruker | ADMIN |
| POST | `/api/users/{id}/reset-password` | Tilbakestill passord | ADMIN |

### Request `POST /api/users`
```json
{
  "firstName": "Per",
  "lastName": "Martinsen",
  "email": "per@everestsushi.no",
  "role": "STAFF",
  "permissions": ["TEMPERATURE_LOG", "CHECKLISTS"]
}
```

### Respons `GET /api/users/{id}`
```json
{
  "id": 3,
  "firstName": "Per",
  "lastName": "Martinsen",
  "email": "per@everestsushi.no",
  "role": "STAFF",
  "status": "ACTIVE",
  "permissions": ["TEMPERATURE_LOG", "CHECKLISTS"],
  "createdAt": "2026-01-15T10:00:00Z"
}
```

---

## 3. Enhetsadministrasjon (temperaturenheter)

### Krav
- CRUD for temperaturenheter (frysere, kjøleskap, kjølere, etc.)
- Hvert enhet har måltemperatur og min/maks grenseverdier
- Brukes til å beregne avvik automatisk ved temperaturlogging
- Enheter kan deaktiveres uten å slette historikk

### Endepunkter

| Metode | Sti | Beskrivelse | Roller |
|--------|-----|-------------|--------|
| GET | `/api/units` | Alle enheter for organisasjon | Alle |
| GET | `/api/units/{id}` | Hent en enhet med detaljer | Alle |
| POST | `/api/units` | Opprett ny enhet | ADMIN |
| PUT | `/api/units/{id}` | Oppdater enhet (navn, grenseverdier, beskrivelse) | ADMIN |
| PATCH | `/api/units/{id}/status` | Aktiver/deaktiver enhet | ADMIN |
| DELETE | `/api/units/{id}` | Slett enhet (soft delete) | ADMIN |

### Request `POST /api/units`
```json
{
  "name": "Fryser #1",
  "type": "FREEZER",
  "targetTemperature": -18.0,
  "minThreshold": -20.0,
  "maxThreshold": -16.0,
  "description": "Sjømat, Kjøtt, Ferdigvarer",
  "active": true
}
```

### Enums for `type`
```
FREEZER | FRIDGE | COOLER | DISPLAY | OTHER
```

---

## 4. Temperaturlogging

### Krav
- Ansatte logger temperatur per enhet
- Systemet sammenligner automatisk mot enhetens grenseverdier
- Avvik flagges automatisk (`isDeviation: true`)
- Tidspunkt defaults til nåtid, men kan overstyres
- Loggen inkluderer alltid hvem som registrerte

### Endepunkter

| Metode | Sti | Beskrivelse | Roller |
|--------|-----|-------------|--------|
| GET | `/api/units/{unitId}/readings` | Alle målinger for en enhet | Alle |
| GET | `/api/readings` | Alle målinger (filtrerbar) | ADMIN, MANAGER |
| POST | `/api/units/{unitId}/readings` | Registrer ny temperaturmåling | Alle |

### Query-parametre for `GET /api/readings`
- `unitId` — filtrer per enhet
- `from` / `to` — datoperiode (ISO 8601)
- `deviationsOnly` — `true` for kun avvik
- `page` / `size` — paginering

### Request `POST /api/units/{unitId}/readings`
```json
{
  "temperature": -12.1,
  "recordedAt": "2026-03-20T08:10:00Z",
  "note": "Dør sto åpen"
}
```

### Respons
```json
{
  "id": 42,
  "unitId": 2,
  "unitName": "Fryser #2",
  "temperature": -12.1,
  "targetTemperature": -18.0,
  "minThreshold": -20.0,
  "maxThreshold": -16.0,
  "isDeviation": true,
  "recordedAt": "2026-03-20T08:10:00Z",
  "note": "Dør sto åpen",
  "recordedBy": {
    "id": 1,
    "name": "Kari Larsen"
  }
}
```

---

## 5. Temperaturgrafer (statistikk)

### Krav
- Frontend trenger tidsseriedata for Chart.js-grafen
- Gruppert per enhet, filtrert på periode
- Avvikspunkt markert

### Endepunkter

| Metode | Sti | Beskrivelse | Roller |
|--------|-----|-------------|--------|
| GET | `/api/readings/stats` | Tidsseriedata for grafer | ADMIN, MANAGER |

### Query-parametre
- `unitIds` — kommaseparert liste (f.eks. `1,2,3`)
- `from` / `to` — datoperiode
- `groupBy` — `HOUR`, `DAY`, `WEEK`

### Respons
```json
{
  "series": [
    {
      "unitId": 1,
      "unitName": "Fryser #1",
      "dataPoints": [
        { "timestamp": "2026-03-14T08:00:00Z", "avgTemperature": -18.4, "isDeviation": false },
        { "timestamp": "2026-03-15T08:00:00Z", "avgTemperature": -18.2, "isDeviation": false }
      ]
    }
  ],
  "deviations": [
    {
      "id": 12,
      "unitId": 2,
      "unitName": "Fryser #2",
      "temperature": -12.1,
      "threshold": -16.0,
      "timestamp": "2026-03-20T08:10:00Z"
    }
  ]
}
```

---

## 6. Sjekklister

### Krav
- Sjekkliste-maler definert av ADMIN (daglig/ukentlig/månedlig)
- Ansatte fullfører sjekkliste-instanser per dag/uke/måned
- Hvert sjekk-element har status (utført/ikke utført)
- Logges hvem som fullførte og når

### Endepunkter

| Metode | Sti | Beskrivelse | Roller |
|--------|-----|-------------|--------|
| GET | `/api/checklists/templates` | Alle maler | Alle |
| POST | `/api/checklists/templates` | Opprett ny mal | ADMIN |
| PUT | `/api/checklists/templates/{id}` | Rediger mal | ADMIN |
| DELETE | `/api/checklists/templates/{id}` | Slett mal | ADMIN |
| GET | `/api/checklists/instances` | Aktive sjekkliste-instanser (filtrert) | Alle |
| GET | `/api/checklists/instances/{id}` | En instans med items | Alle |
| PATCH | `/api/checklists/instances/{id}/items/{itemId}` | Kryss av/fjern kryss på et element | Alle |

### Query-parametre `GET /api/checklists/instances`
- `frequency` — `DAILY`, `WEEKLY`, `MONTHLY`
- `date` — dato for instansen
- `status` — `PENDING`, `IN_PROGRESS`, `COMPLETED`

### Respons `GET /api/checklists/instances/{id}`
```json
{
  "id": 101,
  "templateId": 5,
  "title": "Åpning kjøkken",
  "frequency": "DAILY",
  "date": "2026-03-20",
  "completedCount": 5,
  "totalCount": 5,
  "status": "COMPLETED",
  "items": [
    {
      "id": 1001,
      "text": "Vask og desinfiser arbeidsflater",
      "completed": true,
      "completedBy": { "id": 2, "name": "Ola Nordmann" },
      "completedAt": "2026-03-20T07:45:00Z"
    }
  ]
}
```

---

## 7. Avvikshåndtering

### Krav
- Manuell rapportering av avvik
- Automatiske avvik fra temperaturlogging (linket til reading)
- Status-flyt: `OPEN` → `IN_PROGRESS` → `RESOLVED`
- Kategorisering: modul (IK-Mat / IK-Alkohol) og alvorlighetsgrad
- Kommentarlogg for oppfølging

### Endepunkter

| Metode | Sti | Beskrivelse | Roller |
|--------|-----|-------------|--------|
| GET | `/api/deviations` | Alle avvik (filtrerbar) | Alle |
| GET | `/api/deviations/{id}` | Detaljer om et avvik | Alle |
| POST | `/api/deviations` | Rapporter nytt avvik | Alle |
| PATCH | `/api/deviations/{id}/status` | Endre status | ADMIN, MANAGER |
| POST | `/api/deviations/{id}/comments` | Legg til kommentar | Alle |

### Query-parametre `GET /api/deviations`
- `status` — `OPEN`, `IN_PROGRESS`, `RESOLVED`
- `severity` — `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
- `module` — `IK_MAT`, `IK_ALKOHOL`
- `from` / `to` — datoperiode

### Request `POST /api/deviations`
```json
{
  "title": "Fryser #2 over grenseverdi",
  "description": "Målt -12.1°C, grenseverdi er -18°C. Mulig kompressorfeil.",
  "severity": "CRITICAL",
  "module": "IK_MAT",
  "relatedReadingId": 42
}
```

---

## 8. Dashboard (oversikt)

### Krav
- Aggregert data for dashboardet
- Én request for alle nøkkeltall

### Endepunkter

| Metode | Sti | Beskrivelse | Roller |
|--------|-----|-------------|--------|
| GET | `/api/dashboard/summary` | Nøkkeltall for i dag | Alle |
| GET | `/api/dashboard/notifications` | Siste varsler | Alle |

### Respons `GET /api/dashboard/summary`
```json
{
  "tasksCompleted": 7,
  "tasksTotal": 12,
  "temperatureAlerts": 2,
  "openDeviations": 3,
  "compliancePercentage": 87
}
```

---

## 9. Dokumentlagring (opplæring og info)

### Krav
- Laste opp og laste ned dokumenter (PDF, DOC, video-lenker)
- Kategorisering: opplæring, rutinebeskrivelse, sertifisering
- Sporet per ansatt: hvem har fullført hvilke opplæringer

### Endepunkter

| Metode | Sti | Beskrivelse | Roller |
|--------|-----|-------------|--------|
| GET | `/api/documents` | Alle dokumenter | Alle |
| POST | `/api/documents` | Last opp dokument | ADMIN, MANAGER |
| DELETE | `/api/documents/{id}` | Slett dokument | ADMIN |
| GET | `/api/documents/{id}/download` | Last ned fil | Alle |
| GET | `/api/certifications` | Sertifiseringsstatus per ansatt | ADMIN, MANAGER |
| PATCH | `/api/certifications/{userId}/{docId}` | Marker opplæring som fullført | ADMIN, MANAGER |

### Query-parametre `GET /api/documents`
- `category` — `TRAINING`, `PROCEDURE`, `CERTIFICATION`, `OTHER`
- `module` — `IK_MAT`, `IK_ALKOHOL`, `SHARED`

---

## 10. Eksport (rapporter)

### Krav
- Eksporter compliance-rapporter som PDF og JSON
- Filtrerbar på periode og modul
- PDF-rapporten skal inneholde sjekklister, temperaturlogg, avvik

### Endepunkter

| Metode | Sti | Beskrivelse | Roller |
|--------|-----|-------------|--------|
| GET | `/api/reports/export/pdf` | Generer PDF-rapport | ADMIN, MANAGER |
| GET | `/api/reports/export/json` | Eksporter rådata som JSON | ADMIN, MANAGER |
| GET | `/api/reports` | Liste over genererte rapporter | ADMIN, MANAGER |

### Query-parametre
- `from` / `to` — periode
- `module` — `IK_MAT`, `IK_ALKOHOL`, `ALL`
- `include` — kommaseparert: `checklists,readings,deviations`

---

## 11. Organisasjonsinnstillinger

### Krav
- Organisasjonsdetaljer (navn, org.nr, adresse)
- Modul-toggle (IK-Mat / IK-Alkohol)
- Varslingsinnstillinger

### Endepunkter

| Metode | Sti | Beskrivelse | Roller |
|--------|-----|-------------|--------|
| GET | `/api/organization` | Hent organisasjonsdetaljer | ADMIN |
| PUT | `/api/organization` | Oppdater organisasjon | ADMIN |
| GET | `/api/organization/settings` | Hent innstillinger (varsler, moduler) | ADMIN |
| PUT | `/api/organization/settings` | Oppdater innstillinger | ADMIN |

### Respons `GET /api/organization/settings`
```json
{
  "modules": {
    "ikMat": true,
    "ikAlkohol": true
  },
  "notifications": {
    "emailOnTemperatureDeviation": true,
    "dailySummaryToManagers": true,
    "smsOnCriticalDeviation": false
  }
}
```

---

## Tverrgående krav

### Alle endepunkter
- JWT i `Authorization: Bearer <token>` header
- Multi-tenant: backend filtrerer alltid på innlogget brukers `organizationId`
- Standard feilrespons: `{ "error": "KODE", "message": "Beskrivelse" }`
- HTTP statuskoder: 200, 201, 400 (validering), 401 (ikke autentisert), 403 (ikke autorisert), 404, 500
- Paginering på liste-endepunkter: `?page=0&size=20` → respons inkluderer `totalElements`, `totalPages`

### Sikkerhet (OWASP)
- Input-validering på alle felt (Spring Validation)
- Rate limiting på auth-endepunkter
- SQL injection-beskyttelse via JPA/prepared statements
- XSS-beskyttelse i respons-headers
- CORS konfigurert for frontend-domene

### Database
- Flyway-migrasjonskript for skjema og testdata
- Soft delete der det er hensiktsmessig (enheter, brukere)
- Tidsstempler: `createdAt`, `updatedAt` på alle entiteter
- Indekser på: `organizationId`, `unitId`, `recordedAt`, `status`

### Testdata
- Minst 2 organisasjoner
- 3+ brukere per org med forskjellige roller
- 5+ enheter med temperaturhistorikk (7 dager)
- Ferdig-utfylte sjekklister og et par avvik
- Standard innlogging: `admin@everestsushi.no` / `admin123`
