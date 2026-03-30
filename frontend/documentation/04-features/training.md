# Training and Certifications (Opplæring og info)

Feature for accessing training materials, procedure documentation, and tracking employee certification status.

---

## Route and component

| Property  | Value                  |
|-----------|------------------------|
| Route     | `/opplaering`          |
| Name      | `opplaering`           |
| Component | `TrainingView.vue`     |
| Meta      | `requiresAuth: true`   |

All authenticated users can view training documents and certification status. Document upload is intended to be restricted to ADMIN and MANAGER, but this access control is not yet enforced — the upload form itself does not yet exist.

---

## Service

**Service:** `documentsService` (`src/services/documents.service.ts`)

No Pinia store is used. The view manages its own local state (`trainingDocs`, `certifications`, `loadingDocs`, `loadingCerts`) and calls the service directly on mount.

---

## Data models

### `TrainingDocument`

Defined in `src/types/index.ts`.

```ts
type DocumentFileType = 'PDF' | 'DOC' | 'VID'

interface TrainingDocument {
  id: number
  title: string
  subtitle: string                // secondary line, e.g. category and audience info
  type: DocumentFileType
  actionLabel: string             // label for the action button, e.g. "Last ned" or "Se video"
  colorBg: string                 // hex background colour for the type icon
  colorText: string               // hex text/icon colour for the type icon
}
```

`colorBg` and `colorText` are frontend-only display properties. They control the coloured square icon showing the document type (PDF, DOC, VID) in the document row. These values are hardcoded in the mock and should not be stored on the backend — see known limitations.

### `EmployeeCertification`

Defined in `src/types/index.ts`.

```ts
type CertificationStatus = 'COMPLETE' | 'EXPIRING' | 'MISSING'

interface EmployeeCertification {
  id: number
  name: string                   // full display name, e.g. "Kari Larsen"
  status: CertificationStatus
  expiredCount: number           // number of certifications that have expired
  missingCount: number           // number of required certifications not yet completed
}
```

---

## Tabs

The view renders three tabs via a `.sub-nav` button group. The active tab is tracked in `activeTab` (local `ref`).

| Tab key          | Norwegian label        | Content                                      |
|------------------|------------------------|----------------------------------------------|
| `materiale`      | Opplæringsmateriale    | List of training documents (default tab)     |
| `rutiner`        | Rutinebeskrivelser     | Placeholder — not yet implemented            |
| `sertifiseringer`| Sertifiseringer        | Employee certification status list           |

Tabs are rendered conditionally with `v-if` / `v-else-if`. Data for documents and certifications is fetched in parallel on `onMounted`.

---

## Mock data

### Training documents

Defined in `src/services/documents.service.ts`. Five documents covering the core compliance topics for a restaurant operating under IK-Mat and IK-Alkohol.

| id | Title                          | Type | Subtitle                                                  | Action label | colorBg   | colorText |
|----|--------------------------------|------|-----------------------------------------------------------|--------------|-----------|-----------|
| 1  | Mattrygghet — grunnkurs        | PDF  | Obligatorisk for alle ansatte · Sist oppdatert jan 2026   | Last ned     | `#EDE9FE` | `#5B21B6` |
| 2  | Ansvarlig alkoholservering     | PDF  | IK-Alkohol · Alle med skjenkebevilling                    | Last ned     | `#EFF6FF` | `#1D4ED8` |
| 3  | HACCP-plan — Everest Sushi     | DOC  | Fareanalyse og kritiske kontrollpunkt                     | Last ned     | `#F0FDF4` | `#15803D` |
| 4  | Bruk av temperaturmåler        | VID  | Videoguide · 4 min                                        | Se video     | `#FFFBEB` | `#92400E` |
| 5  | Allergenhåndtering             | PDF  | Rutine for merking og informasjon til gjester             | Last ned     | `#FEF2F2` | `#991B1B` |

Note: the task brief lists different document titles ("HACCP og mattrygghetsplan", "Sjekklisterutiner", "Alkoholopplæring", "Allergenguide", "Hygieneregler kjøkken"). The actual mock data in the service uses the titles above. Documentation reflects what the code actually contains.

### Employee certifications

| id | Name            | Status    | expiredCount | missingCount |
|----|-----------------|-----------|--------------|--------------|
| 1  | Kari Larsen     | COMPLETE  | 0            | 0            |
| 2  | Ola Nordmann    | EXPIRING  | 1            | 0            |
| 3  | Per Martinsen   | MISSING   | 0            | 2            |

---

## UI

### Tab: Opplæringsmateriale

Documents are rendered in a single `.card` as a list of `.doc-row` items.

Each row contains:
1. **Type icon** — a small coloured square badge showing the file type abbreviation (PDF, DOC, VID). Background and text colours come from `doc.colorBg` and `doc.colorText`.
2. **Title and subtitle** — title in medium weight, subtitle in muted small text. Both are truncated.
3. **Action button** — a ghost button with `doc.actionLabel`. Currently links to `href="#"` with `@click.prevent` — no real download is triggered.

Loading state: "Laster inn..." shown while `loadingDocs` is true.

### Tab: Rutinebeskrivelser

Renders a card with a heading and the placeholder text: "Rutinebeskrivelser kommer snart. Her vil du finne detaljerte beskrivelser av alle rutiner og prosedyrer knyttet til internkontroll."

No data is fetched for this tab.

### Tab: Sertifiseringer

Certifications are rendered in a single `.card` as a list of `.status-row` items.

Each row contains:
1. **Avatar** — a circular badge showing the employee's initials (first letter of first name + first letter of last name). Background and text colour reflect certification status:
   - COMPLETE: `var(--c-success-bg)` / `var(--c-success-text)`
   - EXPIRING: `var(--c-warning-bg)` / `var(--c-warning-text)`
   - MISSING: `var(--c-danger-bg)` / `var(--c-danger-text)`
2. **Name** — full name in medium weight, truncated.
3. **Status badge** — on the right.

### Certification badge labels and classes

| Status    | Badge class      | Label logic                              | Example         |
|-----------|------------------|------------------------------------------|-----------------|
| COMPLETE  | `badge-success`  | "Alt fullført"                           | Alt fullført    |
| EXPIRING  | `badge-warning`  | `"{expiredCount} utløpt"`                | 1 utløpt        |
| MISSING   | `badge-danger`   | `"{missingCount} mangler"`               | 2 mangler       |

Loading state: "Laster inn..." shown while `loadingCerts` is true.

---

## API endpoints

These are the planned real API endpoints. All current calls use mock data in the service layer.

| Method | Endpoint                                   | Description                                                  |
|--------|--------------------------------------------|--------------------------------------------------------------|
| GET    | `/api/documents?category=TRAINING`         | Fetch all training documents                                 |
| GET    | `/api/certifications`                      | Fetch all employee certifications for the organisation       |
| POST   | `/api/documents`                           | Upload a new document (multipart/form-data; ADMIN/MANAGER)   |
| GET    | `/api/documents/:id/download`              | Download or stream a document file                           |
| PATCH  | `/api/certifications/:userId/:docId`       | Mark a specific certification as complete for a user         |

---

## Known frontend limitations

### `colorBg` / `colorText` on `TrainingDocument`

These two fields are purely a frontend display concern — they control the visual appearance of the file-type icon in the document list. The backend should not store or return these fields. When the frontend is connected to a real API, the colour should be derived from `type` in the frontend layer, for example:

```ts
const typeColours: Record<DocumentFileType, { bg: string; text: string }> = {
  PDF: { bg: '#EDE9FE', text: '#5B21B6' },
  DOC: { bg: '#F0FDF4', text: '#15803D' },
  VID: { bg: '#FFFBEB', text: '#92400E' },
}
```

Until this mapping is moved to the frontend, the `TrainingDocument` interface will need to be adjusted to remove `colorBg` and `colorText` before real API integration.

---

## What still needs implementation

| Item                          | Notes                                                                                                           |
|-------------------------------|-----------------------------------------------------------------------------------------------------------------|
| Real document download        | The action button in each document row links to `href="#"` with `@click.prevent`. A real implementation would call `GET /api/documents/:id/download` and trigger a file download. |
| Document upload form          | No upload UI exists. The intended flow (ADMIN/MANAGER only) would open a modal with file picker, title, type, and category fields, then `POST /api/documents`. |
| Document deletion             | No delete button exists on document rows. Needs a confirmation step and `DELETE /api/documents/:id`. |
| Certification marking by ADMIN | There are no action controls on certification rows. An ADMIN should be able to manually mark a certification as complete for a user (`PATCH /api/certifications/:userId/:docId`). |
| Rutinebeskrivelser tab content | Currently shows a static placeholder. Needs documents fetched with a `category=ROUTINE` filter and rendered the same way as the training tab. |
| `colorBg` / `colorText` cleanup | These fields should be removed from `TrainingDocument` and derived from `type` in the frontend before real API integration. |
| Permission enforcement         | Upload access should be restricted to ADMIN/MANAGER. This is not enforced anywhere currently — neither by router guard nor by conditional rendering in the UI. |
