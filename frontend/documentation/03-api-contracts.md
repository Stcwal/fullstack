# API Contracts — IK-Kontrollsystem

**Frontend**: Vue 3 + TypeScript (Composition API)
**Backend target**: Java 21 + Spring Boot 3 + MySQL
**Last updated**: 2026-03-30

This document defines every HTTP endpoint the frontend expects the backend to expose. All request/response shapes are derived directly from the TypeScript types in `src/types/index.ts` and the service-layer mock data in `src/services/`. The backend must satisfy these contracts exactly for the frontend to wire up without changes.

---

## General Conventions

### Base URL

All endpoints are prefixed with `/api`. The Axios instance in `src/services/api.ts` sets `baseURL: '/api'`, so Spring Boot should expose endpoints under that path (e.g. via a reverse proxy or by configuring `server.servlet.context-path=/api`).

### Authentication

Every protected endpoint requires:

```
Authorization: Bearer {accessToken}
```

The token is a JWT. The backend reads `organizationId`, `userId`, and `role` from the JWT claims and uses them to scope every query. The frontend never sends `organizationId` as a request parameter — the backend derives it from the token.

### Error Response Format

All error responses use this shape:

```json
{
  "error": "ERROR_CODE",
  "message": "Human-readable description"
}
```

Example error codes: `INVALID_CREDENTIALS`, `TOKEN_EXPIRED`, `NOT_FOUND`, `FORBIDDEN`, `VALIDATION_FAILED`, `UNIT_NOT_FOUND`, `USER_NOT_FOUND`, `DEVIATION_NOT_FOUND`, `CHECKLIST_NOT_FOUND`, `DOCUMENT_NOT_FOUND`.

### HTTP Status Codes

| Code | Meaning |
|------|---------|
| 200 | OK — successful read or update |
| 201 | Created — successful POST that produces a new resource |
| 400 | Validation error — malformed request body or invalid parameter |
| 401 | Unauthenticated — missing or expired token |
| 403 | Forbidden — authenticated but insufficient role/permission |
| 404 | Not found — resource does not exist (within this organization) |
| 500 | Internal server error |

### Pagination

Paginated endpoints accept:

```
?page=0&size=20
```

Paginated responses always include:

```json
{
  "content": [...],
  "totalElements": 42,
  "totalPages": 3,
  "page": 0,
  "size": 20
}
```

`page` is zero-indexed. Default `size` is 20. Maximum `size` is 100.

### Timestamps

All timestamps are ISO 8601 strings: `"2026-03-30T08:15:00"` (local time, no timezone suffix) or `"2026-03-30T08:15:00Z"` (UTC). The backend should store in UTC and return UTC. The frontend displays in the browser's local timezone via JavaScript's `Date` handling.

### Multi-tenancy

The backend scopes every query to the `organizationId` extracted from the JWT. A user from organization A can never read or modify data belonging to organization B, regardless of the IDs they supply in the URL or request body.

---

## 1. Authentication — `/api/auth`

### POST `/api/auth/login`

Public endpoint — no auth header required.

**Request body:**

```json
{
  "email": "kari@everestsushi.no",
  "password": "admin123"
}
```

**Response `200 OK`:**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...",
  "user": {
    "id": 1,
    "firstName": "Kari",
    "lastName": "Larsen",
    "email": "kari@everestsushi.no",
    "role": "ADMIN",
    "organizationId": 1,
    "permissions": {
      "temperatureLogging": true,
      "checklists": true,
      "reports": true,
      "deviations": true,
      "userAdmin": true,
      "settings": true
    }
  }
}
```

The frontend stores `accessToken` in `sessionStorage` as `"token"` and the `user` object as `"user"`. The `accessToken` is attached as `Authorization: Bearer {accessToken}` on subsequent requests via the Axios interceptor in `src/services/api.ts`.

**Error `401`:**

```json
{
  "error": "INVALID_CREDENTIALS",
  "message": "Feil e-post eller passord"
}
```

**Rate limiting:** Max 10 failed attempts per IP per 15 minutes. Return `429 Too Many Requests` with `Retry-After` header.

---

### POST `/api/auth/refresh`

Exchange a refresh token for a new access token.

**Request body:**

```json
{
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4..."
}
```

**Response `200 OK`:**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "bmV3UmVmcmVzaFRva2Vu..."
}
```

**Error `401`:** Token expired or revoked.

---

### POST `/api/auth/logout`

Invalidate the refresh token server-side. Requires `Authorization` header.

**Request body:**

```json
{
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4..."
}
```

**Response `200 OK`:** Empty body.

---

### GET `/api/users/me`

Return the currently authenticated user, re-reading from the database (useful for permission changes taking effect).

**Response `200 OK`:**

```json
{
  "id": 1,
  "firstName": "Kari",
  "lastName": "Larsen",
  "email": "kari@everestsushi.no",
  "role": "ADMIN",
  "organizationId": 1,
  "permissions": {
    "temperatureLogging": true,
    "checklists": true,
    "reports": true,
    "deviations": true,
    "userAdmin": true,
    "settings": true
  }
}
```

---

## 2. Users — `/api/users`

Role access: all roles can read their own profile. ADMIN only for listing all users and all mutation endpoints.

### GET `/api/users`

Returns all users in the organization. Supports pagination.

**Query params:** `page`, `size`, `role` (optional filter), `active` (optional boolean filter)

**Response `200 OK`:**

```json
{
  "content": [
    {
      "id": 1,
      "firstName": "Kari",
      "lastName": "Larsen",
      "email": "kari@everestsushi.no",
      "role": "ADMIN",
      "active": true,
      "organizationId": 1,
      "permissions": {
        "temperatureLogging": true,
        "checklists": true,
        "reports": true,
        "deviations": true,
        "userAdmin": true,
        "settings": true
      }
    },
    {
      "id": 2,
      "firstName": "Ola",
      "lastName": "Nordmann",
      "email": "ola@everestsushi.no",
      "role": "MANAGER",
      "active": true,
      "organizationId": 1,
      "permissions": {
        "temperatureLogging": true,
        "checklists": true,
        "reports": true,
        "deviations": true,
        "userAdmin": false,
        "settings": false
      }
    },
    {
      "id": 3,
      "firstName": "Per",
      "lastName": "Martinsen",
      "email": "per@everestsushi.no",
      "role": "STAFF",
      "active": true,
      "organizationId": 1,
      "permissions": {
        "temperatureLogging": true,
        "checklists": true,
        "reports": false,
        "deviations": true,
        "userAdmin": false,
        "settings": false
      }
    }
  ],
  "totalElements": 3,
  "totalPages": 1,
  "page": 0,
  "size": 20
}
```

---

### GET `/api/users/{id}`

**Response `200 OK`:** Single user object (same shape as the objects in `content[]` above).

**Error `404`:** User not found within this organization.

---

### POST `/api/users`

ADMIN only. Creates a new user in the organization.

**Request body:**

```json
{
  "firstName": "Nina",
  "lastName": "Hagen",
  "email": "nina@everestsushi.no",
  "role": "STAFF",
  "permissions": {
    "temperatureLogging": true,
    "checklists": true,
    "reports": false,
    "deviations": true,
    "userAdmin": false,
    "settings": false
  }
}
```

The backend generates an initial random password and emails it to the new user.

**Response `201 Created`:** Full user object including the generated `id`.

**Error `400`:** `email` already exists in the organization.

---

### PUT `/api/users/{id}`

ADMIN only. Full replacement update of a user record (except password).

**Request body:** Same shape as POST, including `permissions`.

**Response `200 OK`:** Updated user object.

---

### PATCH `/api/users/{id}/role`

ADMIN only. Change a user's role.

**Request body:**

```json
{
  "role": "MANAGER"
}
```

`role` must be one of: `ADMIN | MANAGER | STAFF`

**Response `200 OK`:** Updated user object.

---

### PATCH `/api/users/{id}/status`

ADMIN only. Activate or deactivate a user account.

**Request body:**

```json
{
  "active": false
}
```

**Response `200 OK`:** Updated user object.

---

### POST `/api/users/{id}/reset-password`

ADMIN only. Triggers a password reset email to the user.

**Request body:** Empty.

**Response `200 OK`:**

```json
{
  "message": "Passord-tilbakestilling sendt til nina@everestsushi.no"
}
```

---

### Permissions Reference

The `permissions` object controls fine-grained UI access within a role. All fields are booleans.

| Permission key | Grants access to |
|---|---|
| `temperatureLogging` | Log and view temperature readings |
| `checklists` | View and complete checklists |
| `reports` | View reports and temperature graphs |
| `deviations` | Create and view deviations |
| `userAdmin` | Manage users (settings/users screen) |
| `settings` | Manage org settings and storage units |

Default permissions by role:

| Permission | ADMIN | MANAGER | STAFF |
|---|---|---|---|
| `temperatureLogging` | true | true | true |
| `checklists` | true | true | true |
| `reports` | true | true | false |
| `deviations` | true | true | true |
| `userAdmin` | true | false | false |
| `settings` | true | false | false |

---

## 3. Storage Units — `/api/units`

Role access: all roles can read. ADMIN only for create, update, deactivate, delete.

### GET `/api/units`

Returns all storage units in the organization, both active and inactive.

**Query params:** `active` (optional boolean — `true` returns only active units)

**Response `200 OK`:**

```json
[
  {
    "id": 1,
    "name": "Fryser #1",
    "type": "FREEZER",
    "targetTemp": -18,
    "minTemp": -22,
    "maxTemp": -16,
    "contents": "Sjømat, Kjøtt, Ferdigvarer",
    "active": true,
    "hasAlert": false
  },
  {
    "id": 2,
    "name": "Fryser #2",
    "type": "FREEZER",
    "targetTemp": -18,
    "minTemp": -22,
    "maxTemp": -16,
    "contents": "Grønnsaker, Desserter",
    "active": true,
    "hasAlert": true
  },
  {
    "id": 3,
    "name": "Kjøleskap #1 (kjøkken)",
    "type": "FRIDGE",
    "targetTemp": 3,
    "minTemp": 1,
    "maxTemp": 4,
    "contents": "Fisk, Meieriprodukter, Sauser",
    "active": true,
    "hasAlert": false
  },
  {
    "id": 4,
    "name": "Kjøleskap #2 (bar)",
    "type": "FRIDGE",
    "targetTemp": 3,
    "minTemp": 1,
    "maxTemp": 4,
    "contents": "Juice, Melk, Frukt",
    "active": true,
    "hasAlert": false
  },
  {
    "id": 5,
    "name": "Visningskjøler",
    "type": "COOLER",
    "targetTemp": 5,
    "minTemp": 3,
    "maxTemp": 6,
    "contents": "Sushi-display, Drikke",
    "active": true,
    "hasAlert": false
  },
  {
    "id": 6,
    "name": "Kjøleskap #3 (lager)",
    "type": "FRIDGE",
    "targetTemp": 3,
    "minTemp": 1,
    "maxTemp": 4,
    "contents": "Lagervarer",
    "active": false,
    "hasAlert": false
  }
]
```

`hasAlert` is a computed field: `true` if the unit's most recent temperature reading is outside `[minTemp, maxTemp]`. The backend computes this at query time.

This endpoint returns a plain array (not paginated) because the UI renders all units at once on the dashboard and settings screens.

---

### GET `/api/units/{id}`

**Response `200 OK`:** Single unit object.

**Error `404`:** Unit not found within this organization.

---

### POST `/api/units`

ADMIN only. Create a new storage unit.

**Request body:**

```json
{
  "name": "Kjøleskap #4",
  "type": "FRIDGE",
  "targetTemp": 3,
  "minTemp": 1,
  "maxTemp": 4,
  "contents": "Drikkevarer",
  "active": true
}
```

`type` must be one of: `FREEZER | FRIDGE | COOLER | DISPLAY | OTHER`

**Response `201 Created`:** Full unit object including the generated `id`.

**Error `400`:** Invalid `type`, or `minTemp >= maxTemp`.

---

### PUT `/api/units/{id}`

ADMIN only. Full replacement update.

**Request body:** Same shape as POST.

**Response `200 OK`:** Updated unit object.

---

### PATCH `/api/units/{id}/status`

ADMIN only. Activate or deactivate a unit without deleting it.

**Request body:**

```json
{
  "active": false
}
```

**Response `200 OK`:** Updated unit object.

---

### DELETE `/api/units/{id}`

ADMIN only. Soft delete — marks the unit as deleted. Historical readings are preserved. Deleted units do not appear in any list responses.

**Response `200 OK`:** Empty body.

**Error `404`:** Unit not found.

---

## 4. Temperature Readings — `/api/units/{unitId}/readings`

Role access: all roles can create and read.

### GET `/api/units/{unitId}/readings`

Returns all readings for a single unit, newest first (flat array, no pagination).

**Response `200 OK`:**

```json
[
  {
    "id": 1,
    "unitId": 1,
    "temperature": -18.4,
    "recordedAt": "2026-03-30T08:15:00",
    "recordedBy": "Kari Larsen",
    "note": null,
    "isOutOfRange": false
  },
  {
    "id": 2,
    "unitId": 1,
    "temperature": -18.2,
    "recordedAt": "2026-03-29T20:00:00",
    "recordedBy": "Per Martinsen",
    "note": null,
    "isOutOfRange": false
  }
]
```

`isOutOfRange` is computed by the backend: `temperature < unit.minThreshold || temperature > unit.maxThreshold`.
`recordedBy` is the full name string of the user resolved from the JWT.

**Error `404`:** Unit not found or does not belong to this organization.

---

### POST `/api/units/{unitId}/readings`

Create a new temperature reading. `unitId` is a path parameter — do **not** include it in the request body.

**Request body:**

```json
{
  "temperature": -12.1,
  "recordedAt": "2026-03-30T08:10:00",
  "note": "Dør sto åpen"
}
```

`note` is optional. `recordedAt` is the timestamp of the actual measurement. The backend resolves `recordedBy` from the JWT.

**Response `201 Created`:**

```json
{
  "id": 100,
  "unitId": 2,
  "temperature": -12.1,
  "recordedAt": "2026-03-30T08:10:00",
  "recordedBy": "Kari Larsen",
  "note": "Dør sto åpen",
  "isOutOfRange": true
}
```

**Error `404`:** Unit not found.

**Error `400`:** `temperature` is null, or `recordedAt` is null.

---

## 5. Temperature Statistics — `/api/readings/stats`

Used exclusively by the GraphView (chart screen) to render Chart.js line charts. Returns pre-aggregated series data rather than raw readings.

### GET `/api/readings/stats`

**Query params:**

| Param | Type | Required | Description |
|---|---|---|---|
| `unitIds` | string | no | Comma-separated unit IDs, e.g. `1,2,3`. Omit to include all units. |
| `from` | ISO date string | yes | Start of range |
| `to` | ISO date string | yes | End of range |
| `groupBy` | string | yes | `HOUR`, `DAY`, or `WEEK` |

**Response `200 OK`:**

```json
{
  "labels": ["Tor 14", "Fre 15", "Lør 16", "Søn 17", "Man 18", "Tir 19", "Ons 20"],
  "series": [
    {
      "unitId": 1,
      "unitName": "Fryser #1",
      "color": "#3B82F6",
      "data": [-18.4, -18.2, -18.1, -18.3, -18.5, -18.2, -18.4]
    },
    {
      "unitId": 2,
      "unitName": "Fryser #2",
      "color": "#6366F1",
      "data": [-18.3, -18.1, -17.9, -18.0, -17.8, -16.5, -12.1]
    },
    {
      "unitId": 3,
      "unitName": "Kjøleskap #1",
      "color": "#16A34A",
      "data": [3.2, 3.5, 3.8, 5.2, 3.4, 3.1, 3.2]
    },
    {
      "unitId": 5,
      "unitName": "Visningskjøler",
      "color": "#F59E0B",
      "data": [4.8, 5.0, 4.9, 5.1, 5.0, 4.7, 5.1]
    }
  ],
  "deviations": [
    {
      "index": 3,
      "unitId": 3,
      "unitName": "Kjøleskap #1",
      "value": 5.2,
      "status": "RESOLVED"
    },
    {
      "index": 6,
      "unitId": 2,
      "unitName": "Fryser #2",
      "value": -12.1,
      "status": "OPEN"
    }
  ]
}
```

- `labels` contains the display labels for the x-axis, formatted per `groupBy` (e.g. `"Man 18"` for DAY, `"08:00"` for HOUR, `"Uke 12"` for WEEK).
- `series[].data` aligns with `labels` by index. Use `null` for time buckets with no reading.
- `deviations` are the out-of-range data points annotated with their `index` (position in `labels`/`data`) for overlay markers on the chart.
- `color` is a suggestion from the backend based on unit order; the frontend passes it directly to Chart.js.

---

## 6. Checklists — `/api/checklists`

Checklists are instantiated from templates. Each day/week/month the backend creates an instance from each active template for the organization.

### GET `/api/checklists`

Returns checklist instances for the current organization.

**Query params:**

| Param | Type | Description |
|---|---|---|
| `frequency` | string | `DAILY`, `WEEKLY`, or `MONTHLY` |
| `date` | ISO date string | Date to fetch instances for (default: today) |
| `status` | string | `PENDING`, `IN_PROGRESS`, or `COMPLETED` |
| `moduleType` | string | `IK_MAT` or `IK_ALKOHOL` |

**Response `200 OK`:**

```json
[
  {
    "id": 1,
    "title": "Åpning kjøkken",
    "frequency": "DAILY",
    "moduleType": "IK_MAT",
    "status": "COMPLETED",
    "completedBy": "Ola Nordmann",
    "completedAt": "2026-03-30T07:45:00",
    "items": [
      {
        "id": 11,
        "text": "Vask og desinfiser arbeidsflater",
        "completed": true,
        "completedBy": "Ola Nordmann",
        "completedAt": "2026-03-30T07:42:00"
      },
      {
        "id": 12,
        "text": "Sjekk såpedispensere og håndsprit",
        "completed": true,
        "completedBy": "Ola Nordmann",
        "completedAt": "2026-03-30T07:43:00"
      },
      {
        "id": 13,
        "text": "Kontroller skadedyrfeller",
        "completed": true,
        "completedBy": "Ola Nordmann",
        "completedAt": "2026-03-30T07:44:00"
      },
      {
        "id": 14,
        "text": "Sjekk holdbarhetsdatoer",
        "completed": true,
        "completedBy": "Ola Nordmann",
        "completedAt": "2026-03-30T07:44:00"
      },
      {
        "id": 15,
        "text": "Registrer ansatte på vakt",
        "completed": true,
        "completedBy": "Ola Nordmann",
        "completedAt": "2026-03-30T07:45:00"
      }
    ]
  },
  {
    "id": 2,
    "title": "Matdisplay og servering",
    "frequency": "DAILY",
    "moduleType": "IK_MAT",
    "status": "IN_PROGRESS",
    "completedBy": null,
    "completedAt": null,
    "items": [
      {
        "id": 21,
        "text": "Sjekk temperatur i sushi-display",
        "completed": true,
        "completedBy": "Kari Larsen",
        "completedAt": "2026-03-30T08:15:00"
      },
      {
        "id": 22,
        "text": "Renhold serveringsområde",
        "completed": false,
        "completedBy": null,
        "completedAt": null
      },
      {
        "id": 23,
        "text": "Fyll på engangshansker",
        "completed": false,
        "completedBy": null,
        "completedAt": null
      },
      {
        "id": 24,
        "text": "Kontroll matmerking og allergener",
        "completed": false,
        "completedBy": null,
        "completedAt": null
      }
    ]
  }
]
```

`status` is computed: `PENDING` (no items done), `IN_PROGRESS` (some items done), `COMPLETED` (all items done and `completedAt` set).

This endpoint returns a plain array (not paginated) because the UI renders all checklists for a given frequency/date at once.

---

### GET `/api/checklists/{id}`

Returns a single checklist instance with all items.

**Response `200 OK`:** Single checklist object (same shape as objects in the array above).

---

### POST `/api/checklists/templates`

ADMIN only. Create a new checklist template. The backend will auto-instantiate it for the correct period.

**Request body:**

```json
{
  "title": "Kveldsstenging bar",
  "frequency": "DAILY",
  "moduleType": "IK_ALKOHOL",
  "items": [
    { "text": "Tøm og rengjør isbøtter" },
    { "text": "Logg restbeholdning alkohol" },
    { "text": "Kontroller ID-skanner" }
  ]
}
```

**Response `201 Created`:**

```json
{
  "id": 10,
  "title": "Kveldsstenging bar",
  "frequency": "DAILY",
  "moduleType": "IK_ALKOHOL",
  "items": [
    { "id": 101, "text": "Tøm og rengjør isbøtter" },
    { "id": 102, "text": "Logg restbeholdning alkohol" },
    { "id": 103, "text": "Kontroller ID-skanner" }
  ]
}
```

---

### PUT `/api/checklists/templates/{id}`

ADMIN only. Update a template title, items, or frequency.

**Request body:** Same shape as POST.

**Response `200 OK`:** Updated template object.

---

### DELETE `/api/checklists/templates/{id}`

ADMIN only. Deletes the template. Existing instances are preserved for audit history.

**Response `200 OK`:** Empty body.

---

### PATCH `/api/checklists/{checklistId}/items/{itemId}`

Toggle an item as completed or uncompleted. Available to all authenticated roles.

**Request body:**

```json
{
  "completed": true
}
```

The backend resolves `completedBy` and `completedAt` from the JWT and server time. If `completed: false`, both fields are cleared.

**Response `200 OK`:**

```json
{
  "id": 22,
  "text": "Renhold serveringsområde",
  "completed": true,
  "completedBy": "Kari Larsen",
  "completedAt": "2026-03-30T10:30:00"
}
```

After all items in a checklist are completed, the backend sets the checklist `status` to `COMPLETED` and `completedAt` to the timestamp of the last item.

---

## 7. Deviations — `/api/deviations`

Role access: all roles can create and read. ADMIN and MANAGER can change status and add comments.

### GET `/api/deviations`

Returns all deviations in the organization.

**Query params:**

| Param | Type | Description |
|---|---|---|
| `status` | string | `OPEN`, `IN_PROGRESS`, or `RESOLVED` |
| `severity` | string | `LOW`, `MEDIUM`, `HIGH`, or `CRITICAL` |
| `moduleType` | string | `IK_MAT` or `IK_ALKOHOL` |
| `from` | ISO date string | Filter by `reportedAt` start |
| `to` | ISO date string | Filter by `reportedAt` end |
| `page` | number | Zero-indexed page |
| `size` | number | Page size |

**Response `200 OK`:**

```json
{
  "content": [
    {
      "id": 1,
      "title": "Fryser #2 over grenseverdi",
      "description": "Målt -12.1°C, grenseverdi er -18°C. Mulig kompressorfeil.",
      "status": "OPEN",
      "severity": "CRITICAL",
      "moduleType": "IK_MAT",
      "reportedBy": "Kari Larsen",
      "reportedAt": "2026-03-30T08:12:00",
      "resolvedBy": null,
      "resolvedAt": null,
      "resolution": null
    },
    {
      "id": 2,
      "title": "Manglende alderskontroll-logg",
      "description": "Barpersonalet fullførte ikke alderskontrollsjekkliste for kveldsvakt.",
      "status": "IN_PROGRESS",
      "severity": "MEDIUM",
      "moduleType": "IK_ALKOHOL",
      "reportedBy": "Per Martinsen",
      "reportedAt": "2026-03-29T22:00:00",
      "resolvedBy": null,
      "resolvedAt": null,
      "resolution": null
    },
    {
      "id": 3,
      "title": "Utgått soyasaus på lager",
      "description": "Vare kastet, leverandør varslet. FIFO-rutine gjennomgått med personalet.",
      "status": "RESOLVED",
      "severity": "LOW",
      "moduleType": "IK_MAT",
      "reportedBy": "Ola Nordmann",
      "reportedAt": "2026-03-27T14:30:00",
      "resolvedBy": "Kari Larsen",
      "resolvedAt": "2026-03-28T09:00:00",
      "resolution": "Vare kastet, leverandør varslet. FIFO gjennomgått."
    }
  ]
}
```

`reportedBy` and `resolvedBy` are full name strings resolved from the JWT. `resolvedBy` is `null` for unresolved deviations.

---

### GET `/api/deviations/{id}`

Returns a single deviation with full comments array.

**Response `200 OK`:** Single deviation object (same shape as objects in `content[]`).

**Error `404`:** Deviation not found.

---

### POST `/api/deviations`

Create a new deviation. All authenticated roles.

**Request body:**

```json
{
  "title": "Ødelagt låsmekanisme på kjøleskapsdør",
  "description": "Kjøleskap #1 kjøkkendøren lukker ikke ordentlig. Tetting skadet.",
  "severity": "HIGH",
  "moduleType": "IK_MAT"
}
```

`severity` must be one of: `LOW | MEDIUM | HIGH | CRITICAL`

The backend resolves `reportedBy` and `reportedAt` from the JWT/server time and sets `status: OPEN`.

**Response `201 Created`:** Full deviation object including generated `id`.

---

### PATCH `/api/deviations/{id}/status`

ADMIN or MANAGER only. Advance or change the status.

**Request body:**

```json
{
  "status": "IN_PROGRESS"
}
```

Status flow: `OPEN` → `IN_PROGRESS` → `RESOLVED`. To resolve, use the dedicated resolve endpoint below. Direct status changes are for `OPEN` → `IN_PROGRESS` transitions.

**Response `200 OK`:** Updated deviation object.

---

### PATCH `/api/deviations/{id}/resolve`

ADMIN, MANAGER, or SUPERVISOR only. Mark as resolved with a resolution note.

**Request body:**

```json
{
  "resolution": "Kompressor skiftet ut av servicetekniker. Kontrollert OK."
}
```

The backend sets `status: RESOLVED` and `resolvedAt` to server time.

**Response `200 OK`:** Updated deviation object.

---

### POST `/api/deviations/{id}/comments`

ADMIN or MANAGER only. Add a comment to a deviation.

**Request body:**

```json
{
  "text": "Servicetekniker varslet — forventer besøk innen 24 timer."
}
```

The backend resolves `authorId`, `authorName`, and `createdAt` from the JWT/server time.

**Response `201 Created`:**

```json
{
  "id": 2,
  "text": "Servicetekniker varslet — forventer besøk innen 24 timer.",
  "authorId": 2,
  "authorName": "Ola Nordmann",
  "createdAt": "2026-03-30T09:00:00"
}
```

---

## 8. Dashboard — `/api/dashboard`

### GET `/api/dashboard`

Returns a combined summary of today's status. All roles.

**Response `200 OK`:**

```json
{
  "stats": {
    "tasksCompleted": 7,
    "tasksTotal": 12,
    "tempAlerts": 2,
    "openDeviations": 3,
    "compliancePercent": 87
  },
  "tasks": [
    {
      "id": 1,
      "name": "Morgen kjøkkenrenhold",
      "status": "COMPLETED",
      "completedBy": "Ola N.",
      "completedAt": "07:45"
    },
    {
      "id": 2,
      "name": "Temperatursjekk kjøleskap",
      "status": "COMPLETED",
      "completedBy": "Kari L.",
      "completedAt": "08:15"
    },
    {
      "id": 3,
      "name": "Logg varemottak-temperaturer",
      "status": "PENDING",
      "completedBy": null,
      "completedAt": null
    },
    {
      "id": 4,
      "name": "Alderskontroll-logg (IK-Alkohol)",
      "status": "NOT_STARTED",
      "completedBy": null,
      "completedAt": null
    },
    {
      "id": 5,
      "name": "Kveldsstenging sjekkliste",
      "status": "NOT_STARTED",
      "completedBy": null,
      "completedAt": null
    }
  ],
  "alerts": [
    {
      "id": 1,
      "message": "Fryser #2 over grenseverdi — 15 min siden",
      "type": "danger",
      "time": "08:10"
    },
    {
      "id": 2,
      "message": "Manglende alderskontroll-logg — 2t siden",
      "type": "warning",
      "time": "06:00"
    }
  ]
}
```

**Stats fields:**

| Field | Description |
|---|---|
| `tasksCompleted` | Checklist instances with status `COMPLETED` today |
| `tasksTotal` | Total checklist instances for today |
| `tempAlerts` | Today's temperature readings where `isOutOfRange = true` |
| `openDeviations` | Deviations with status `OPEN` |
| `compliancePercent` | `(tasksCompleted / tasksTotal) * 100`, rounded to nearest integer |

**Task statuses:** `COMPLETED | PENDING | NOT_STARTED`
- `COMPLETED` — all checklist items done
- `PENDING` — at least one item done
- `NOT_STARTED` — no items touched

**Notification types:** `danger | warning | info`

---

### GET `/api/dashboard/notifications`

Returns only the notifications array from the dashboard response, for polling/refresh without re-fetching stats.

**Response `200 OK`:**

```json
[
  {
    "id": 1,
    "message": "Fryser #2 over grenseverdi — 15 min siden",
    "type": "danger",
    "time": "08:10"
  }
]
```

---

## 9. Documents and Certifications — `/api/documents`, `/api/certifications`

### GET `/api/documents`

Returns training and procedure documents available to the organization.

**Query params:**

| Param | Type | Description |
|---|---|---|
| `category` | string | `TRAINING`, `PROCEDURE`, `CERTIFICATION`, or `OTHER` |
| `moduleType` | string | `IK_MAT`, `IK_ALKOHOL`, or `SHARED` |

**Response `200 OK`:**

```json
[
  {
    "id": 1,
    "title": "Mattrygghet — grunnkurs",
    "subtitle": "Obligatorisk for alle ansatte · Sist oppdatert jan 2026",
    "category": "TRAINING",
    "moduleType": "SHARED",
    "fileType": "PDF",
    "fileSizeBytes": 204800,
    "uploadedAt": "2026-01-15T10:00:00",
    "uploadedBy": "Kari Larsen",
    "downloadUrl": "/api/documents/1/download"
  },
  {
    "id": 2,
    "title": "Ansvarlig alkoholservering",
    "subtitle": "IK-Alkohol · Alle med skjenkebevilling",
    "category": "TRAINING",
    "moduleType": "IK_ALKOHOL",
    "fileType": "PDF",
    "fileSizeBytes": 512000,
    "uploadedAt": "2026-01-20T09:00:00",
    "uploadedBy": "Kari Larsen",
    "downloadUrl": "/api/documents/2/download"
  },
  {
    "id": 3,
    "title": "HACCP-plan — Everest Sushi",
    "subtitle": "Fareanalyse og kritiske kontrollpunkt",
    "category": "PROCEDURE",
    "moduleType": "IK_MAT",
    "fileType": "DOC",
    "fileSizeBytes": 102400,
    "uploadedAt": "2026-02-01T11:00:00",
    "uploadedBy": "Kari Larsen",
    "downloadUrl": "/api/documents/3/download"
  },
  {
    "id": 4,
    "title": "Bruk av temperaturmåler",
    "subtitle": "Videoguide · 4 min",
    "category": "TRAINING",
    "moduleType": "SHARED",
    "fileType": "VID",
    "fileSizeBytes": 52428800,
    "uploadedAt": "2026-01-10T14:00:00",
    "uploadedBy": "Kari Larsen",
    "downloadUrl": "/api/documents/4/download"
  },
  {
    "id": 5,
    "title": "Allergenhåndtering",
    "subtitle": "Rutine for merking og informasjon til gjester",
    "category": "PROCEDURE",
    "moduleType": "IK_MAT",
    "fileType": "PDF",
    "fileSizeBytes": 153600,
    "uploadedAt": "2026-01-25T13:00:00",
    "uploadedBy": "Kari Larsen",
    "downloadUrl": "/api/documents/5/download"
  }
]
```

`fileType` values: `PDF | DOC | VID`
`category` values: `TRAINING | PROCEDURE | CERTIFICATION | OTHER`
`moduleType` values: `IK_MAT | IK_ALKOHOL | SHARED`

---

### GET `/api/documents/{id}/download`

Returns the file as a binary blob download. The `Content-Disposition` header should be `attachment; filename="<title>.<ext>"`.

**Response `200 OK`:** Binary file content with appropriate `Content-Type` header.

---

### POST `/api/documents`

ADMIN only. Upload a new document. Uses `multipart/form-data`.

**Request (multipart):**

| Field | Type | Description |
|---|---|---|
| `file` | file | The uploaded file |
| `title` | string | Display title |
| `subtitle` | string | Optional — description or audience hint |
| `category` | string | `TRAINING`, `PROCEDURE`, `CERTIFICATION`, `OTHER` |
| `moduleType` | string | `IK_MAT`, `IK_ALKOHOL`, `SHARED` |

**Response `201 Created`:** Full document object including generated `id` and `downloadUrl`.

---

### DELETE `/api/documents/{id}`

ADMIN only. Soft delete — removes from listings but preserves the file for audit purposes.

**Response `200 OK`:** Empty body.

---

### GET `/api/certifications`

Returns the certification status summary per employee.

**Response `200 OK`:**

```json
[
  {
    "id": 1,
    "userId": 1,
    "name": "Kari Larsen",
    "status": "COMPLETE",
    "expiredCount": 0,
    "missingCount": 0,
    "certifications": [
      {
        "documentId": 1,
        "title": "Mattrygghet — grunnkurs",
        "completedAt": "2026-01-20T00:00:00",
        "expiresAt": "2027-01-20T00:00:00",
        "status": "COMPLETE"
      }
    ]
  },
  {
    "id": 2,
    "userId": 2,
    "name": "Ola Nordmann",
    "status": "EXPIRING",
    "expiredCount": 1,
    "missingCount": 0,
    "certifications": [
      {
        "documentId": 1,
        "title": "Mattrygghet — grunnkurs",
        "completedAt": "2025-03-01T00:00:00",
        "expiresAt": "2026-04-01T00:00:00",
        "status": "EXPIRING"
      }
    ]
  },
  {
    "id": 3,
    "userId": 3,
    "name": "Per Martinsen",
    "status": "MISSING",
    "expiredCount": 0,
    "missingCount": 2,
    "certifications": []
  }
]
```

`status` per employee: `COMPLETE` (all required certs valid), `EXPIRING` (at least one expires within 30 days), `MISSING` (at least one required cert not completed).

`status` per certification: `COMPLETE | EXPIRING | EXPIRED | MISSING`

---

### PATCH `/api/certifications/{employeeId}/documents/{documentId}`

ADMIN or MANAGER only. Mark a certification as completed for an employee.

**Request body:**

```json
{
  "completedAt": "2026-03-30T00:00:00",
  "expiresAt": "2027-03-30T00:00:00"
}
```

**Response `200 OK`:** Updated certification entry.

---

## 10. Reports — `/api/reports`

Role access: ADMIN and MANAGER only for all report endpoints.

### GET `/api/reports`

List previously generated reports.

**Query params:** `page`, `size`, `from`, `to`, `module` (`IK_MAT | IK_ALKOHOL`)

**Response `200 OK`:**

```json
{
  "content": [
    {
      "id": 1,
      "title": "Temperaturrapport mars 2026",
      "generatedAt": "2026-03-30T09:00:00",
      "generatedBy": "Kari Larsen",
      "module": "IK_MAT",
      "from": "2026-03-01",
      "to": "2026-03-30",
      "includes": ["checklists", "readings", "deviations"],
      "downloadUrl": "/api/reports/1/download"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "page": 0,
  "size": 20
}
```

---

### GET `/api/reports/export/pdf`

Generate and download a PDF report. Returns a binary blob.

**Query params:**

| Param | Type | Required | Description |
|---|---|---|---|
| `from` | ISO date string | yes | Start of report period |
| `to` | ISO date string | yes | End of report period |
| `module` | string | no | `IK_MAT` or `IK_ALKOHOL` — omit for combined |
| `include` | string | no | Comma-separated: `checklists,readings,deviations` |

**Response `200 OK`:** Binary PDF blob.

```
Content-Type: application/pdf
Content-Disposition: attachment; filename="rapport-2026-03-30.pdf"
```

---

### GET `/api/reports/export/json`

Generate and download a structured JSON export of the same data as the PDF.

**Query params:** Same as `/export/pdf`.

**Response `200 OK`:**

```json
{
  "organization": "Everest Sushi & Fusion AS",
  "period": { "from": "2026-03-01", "to": "2026-03-30" },
  "generatedAt": "2026-03-30T09:00:00",
  "checklists": [...],
  "readings": [...],
  "deviations": [...]
}
```

```
Content-Disposition: attachment; filename="rapport-2026-03-30.json"
```

---

### GET `/api/reports/chart`

Returns chart data for the GraphView. This is an alias to `/api/readings/stats` formatted for Chart.js. See section 5.

**Query params:** `period` — `WEEK` or `MONTH`. The backend maps this to the appropriate `from`/`to`/`groupBy` parameters internally.

**Response `200 OK`:** Same shape as `/api/readings/stats` response.

---

## 11. Organization — `/api/organization`

Role access: ADMIN only for all mutation endpoints. All roles can read (needed to check which modules are active).

### GET `/api/organization`

Returns the current organization's details and settings.

**Response `200 OK`:**

```json
{
  "id": 1,
  "name": "Everest Sushi & Fusion AS",
  "orgNumber": "937 219 997",
  "industry": "Restaurant",
  "address": "Innherredsveien 1, 7014 Trondheim",
  "modules": {
    "ikMat": true,
    "ikAlkohol": true
  },
  "notifications": {
    "emailOnTempDeviation": true,
    "dailySummaryToManagers": true,
    "smsOnCritical": false
  }
}
```

---

### PUT `/api/organization`

ADMIN only. Update organization details.

**Request body:**

```json
{
  "name": "Everest Sushi & Fusion AS",
  "orgNumber": "937 219 997",
  "industry": "Restaurant",
  "address": "Innherredsveien 1, 7014 Trondheim"
}
```

**Response `200 OK`:** Updated organization object.

---

### GET `/api/organization/settings`

ADMIN only. Returns module toggles and notification settings separately from core org info. This is the same data as embedded in `GET /api/organization`, exposed as its own endpoint for the settings screen.

**Response `200 OK`:**

```json
{
  "modules": {
    "ikMat": true,
    "ikAlkohol": true
  },
  "notifications": {
    "emailOnTempDeviation": true,
    "dailySummaryToManagers": true,
    "smsOnCritical": false
  }
}
```

---

### PUT `/api/organization/settings`

ADMIN only. Update module toggles and notification preferences.

**Request body:**

```json
{
  "modules": {
    "ikMat": true,
    "ikAlkohol": false
  },
  "notifications": {
    "emailOnTempDeviation": true,
    "dailySummaryToManagers": false,
    "smsOnCritical": false
  }
}
```

**Response `200 OK`:** Updated settings object (same shape as GET response).

Disabling a module (`ikMat: false` or `ikAlkohol: false`) hides the corresponding sidebar navigation items in the frontend. The data is not deleted — re-enabling the module restores full access.

---

## Security Notes

### Rate Limiting

- `/api/auth/login`: max 10 requests per IP per 15 minutes. Return `429 Too Many Requests` with `Retry-After: 900` header.
- `/api/auth/refresh`: max 30 requests per IP per hour.
- All other endpoints: standard Spring Boot / reverse proxy rate limits apply.

### Input Validation

All request bodies are validated with Spring Validation (`@Valid`, `@NotBlank`, `@Size`, `@Email`, `@Min`, `@Max`). Validation failures return `400` with the error body:

```json
{
  "error": "VALIDATION_FAILED",
  "message": "email: must be a valid email address; password: must not be blank"
}
```

### SQL Injection Protection

All database access is via JPA/Hibernate with parameterized queries. No native SQL string concatenation.

### CORS

```
Access-Control-Allow-Origin: http://localhost:5173   (dev)
Access-Control-Allow-Origin: https://app.yourdomain.no  (prod)
Access-Control-Allow-Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
Access-Control-Allow-Headers: Authorization, Content-Type
Access-Control-Allow-Credentials: true
```

### XSS Protection Headers

```
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Content-Security-Policy: default-src 'self'
```

### JWT Claims

The JWT must include at minimum:

```json
{
  "sub": "1",
  "organizationId": 1,
  "role": "ADMIN",
  "iat": 1743300000,
  "exp": 1743303600
}
```

Access token expiry: 1 hour. Refresh token expiry: 7 days. Refresh tokens are stored in the database and invalidated on logout.

### Password Storage

Passwords hashed with BCrypt, cost factor 12. Plaintext passwords never logged.

---

## Appendix — Enum Reference

| Enum | Values |
|---|---|
| `UserRole` | `ADMIN`, `MANAGER`, `STAFF` |
| `UnitType` | `FREEZER`, `FRIDGE`, `COOLER`, `DISPLAY`, `OTHER` |
| `ModuleType` | `IK_MAT`, `IK_ALKOHOL`, `SHARED` |
| `ChecklistFrequency` | `DAILY`, `WEEKLY`, `MONTHLY` |
| `ChecklistStatus` | `PENDING`, `IN_PROGRESS`, `COMPLETED` |
| `DeviationStatus` | `OPEN`, `IN_PROGRESS`, `RESOLVED` |
| `DeviationSeverity` | `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` |
| `CertificationStatus` | `COMPLETE`, `EXPIRING`, `EXPIRED`, `MISSING` |
| `DocumentCategory` | `TRAINING`, `PROCEDURE`, `CERTIFICATION`, `OTHER` |
| `DocumentFileType` | `PDF`, `DOC`, `VID` |
| `NotificationType` | `danger`, `warning`, `info` |
| `TaskStatus` | `COMPLETED`, `PENDING`, `NOT_STARTED` |
| `ChartGroupBy` | `HOUR`, `DAY`, `WEEK` |
