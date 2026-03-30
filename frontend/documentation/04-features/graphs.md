# Temperature Graphs (Temperaturgrafer)

Feature for visualising temperature trends over time across all registered storage units, with deviation highlighting and an event log.

---

## Route and component

| Property  | Value                |
|-----------|----------------------|
| Route     | `/grafer`            |
| Name      | `grafer`             |
| Component | `GraphView.vue`      |
| Meta      | `requiresAuth: true` |

All authenticated users with the `reports` permission can access this view. There is no role-based restriction beyond authentication — the route meta only sets `requiresAuth: true`, and no `requiresAdmin` guard is applied. The `reports` permission on `UserPermissions` is the intended capability gate, but permission checking is not enforced by the router guard at this time.

---

## Service

**Service:** `reportsService` (`src/services/reports.service.ts`)

No Pinia store is used for this feature. The view manages its own local state (`chartData`, `period`, `loading`) and calls the service directly.

---

## Chart library

The view uses **Chart.js** via **vue-chartjs**. The `Line` component from `vue-chartjs` is rendered inside a fixed-height container (280px).

Chart.js modules registered:

```ts
CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend
```

The built-in Chart.js legend is disabled (`plugins.legend.display: false`). A custom legend is rendered below the chart area as a row of coloured squares with dataset labels.

---

## Data model

### `ChartPeriod`

```ts
type ChartPeriod = 'WEEK' | 'MONTH'
```

Defined in `src/types/index.ts`. Controls which time window the service returns data for.

### `ChartDataset` (service-internal type)

```ts
interface ChartDataset {
  label: string
  data: (number | null)[]
  color: string               // hex colour string, e.g. '#3B82F6'
}
```

### `ChartData` (service-internal type)

```ts
interface ChartData {
  labels: string[]
  datasets: ChartDataset[]
  alerts: {
    index: number             // position in the labels/data arrays
    unitName: string          // display name of the unit
    value: number             // temperature value at that point
    status: 'OPEN' | 'RESOLVED'
  }[]
}
```

`alerts` are temperature readings that were out of range. Each alert carries an `index` that maps back to a specific label/data position, allowing the chart renderer to highlight that point.

---

## Mock data

Defined in `src/services/reports.service.ts`. The service returns different datasets depending on the selected period.

### WEEK period

Labels: `['Tor 14', 'Fre 15', 'Lør 16', 'Søn 17', 'Man 18', 'Tir 19', 'Ons 20']`

| Dataset        | Values (°C)                                              | Colour    |
|----------------|----------------------------------------------------------|-----------|
| Fryser #1      | -18.4, -18.2, -18.1, -18.3, -18.5, -18.2, -18.4        | `#3B82F6` |
| Fryser #2      | -18.3, -18.1, -17.9, -18.0, -17.8, -16.5, **-12.1**    | `#6366F1` |
| Kjøleskap #1   | 3.2, 3.5, 3.8, **5.2**, 3.4, 3.1, 3.2                  | `#16A34A` |
| Visningskjøler | 4.8, 5.0, 4.9, 5.1, 5.0, 4.7, 5.1                      | `#F59E0B` |

Alerts for WEEK:
- Index 3, Kjøleskap #1, 5.2°C — status `RESOLVED`
- Index 6, Fryser #2, -12.1°C — status `OPEN`

### MONTH period

Labels: `['1', '2', ..., '30']` (30 day indices)

| Dataset        | Values                                             | Colour    |
|----------------|----------------------------------------------------|-----------|
| Fryser #1      | 30 random values around -18°C ± 0.5               | `#3B82F6` |
| Fryser #2      | 27 normal values, then -16.5, -14.2, **-12.1**    | `#6366F1` |
| Kjøleskap #1   | 30 random values in the 3–4°C range               | `#16A34A` |
| Visningskjøler | 30 random values around 5°C ± 0.5                 | `#F59E0B` |

Alerts for MONTH:
- Index 27, Fryser #2, -16.5°C — status `RESOLVED`
- Index 29, Fryser #2, -12.1°C — status `OPEN`

The MONTH data for Fryser #1, Kjøleskap #1, and Visningskjøler is randomised on each fetch call. Fryser #2 has a deterministic spike at the end to tell a consistent story.

---

## UI

### Page layout

The header row contains the title "Temperaturgrafer" on the left and two export buttons on the right:
- "Eksporter (PDF)"
- "Eksporter (JSON)"

Below the header, a period selector nav is shown, then the chart card, and finally the deviation log card.

### Period selector

Two toggle buttons in a `.sub-nav`:

| Button label    | `ChartPeriod` value |
|-----------------|---------------------|
| Siste 7 dager   | `WEEK`              |
| Siste 30 dager  | `MONTH`             |

The active button receives the `.active` class. Selecting a different period calls `setPeriod(p)` which updates state and triggers a fresh `loadData()`.

### Chart rendering

The `Line` component receives `lineChartData` and `lineChartOptions` as computed properties.

**Alert point rendering:** Points on the chart are sized and coloured based on whether an alert exists at that index:
- Normal points: radius 3, filled with the dataset colour.
- Alert positions: radius 6, filled red (`#EF4444`) if the alert status is `OPEN`. Resolved alert positions use the radius 6 but retain the dataset colour (they are enlarged but not red).

The alert `index` field aligns with the `labels` array — so if an alert has `index: 6`, it corresponds to `labels[6]` and `dataset.data[6]`.

**Custom legend:** Rendered below the chart as a flex row. Each dataset contributes a coloured 12×12px square and its label. An additional "Avvik (åpent)" legend entry shows a red circle to explain the red point markers.

**Chart options:**
- `responsive: true`, `maintainAspectRatio: false`
- Y-axis: light grid lines (`rgba(0,0,0,0.05)`)
- X-axis: no grid lines
- Tooltip callback: `"{label}: {value}°C"`

### Deviation log

Rendered below the chart card if `chartData.alerts.length > 0`. Each alert appears as a `.status-row` showing:
- Unit name (bold)
- Temperature value and the date label from the chart
- Status badge: "Åpen" (`badge-danger`) or "Løst" (`badge-success`)

Rows with `status === 'OPEN'` receive an `.is-alert` class for visual emphasis.

### Export buttons

Both buttons are implemented as stubs. Clicking them triggers `alert()` calls:
- PDF: `"PDF-eksport er ikke implementert ennå."`
- JSON: `"JSON-eksport er ikke implementert ennå."`

Neither button triggers a download or any backend call.

---

## API endpoints

These are the planned real API endpoints. All current calls return mock data from the service.

| Method | Endpoint                                                            | Description                                             |
|--------|---------------------------------------------------------------------|---------------------------------------------------------|
| GET    | `/api/readings/stats?unitIds=1,2,3&from=...&to=...&groupBy=DAY`    | Fetch aggregated temperature data for the chart         |

The `from` and `to` query parameters would be ISO 8601 date strings derived from the selected `ChartPeriod`. `groupBy=DAY` collapses multiple readings per day into a single averaged data point.

### Expected backend response shape

```json
{
  "series": [
    {
      "unitId": 2,
      "unitName": "Fryser #2",
      "dataPoints": [
        { "timestamp": "2026-03-24T00:00:00Z", "avgTemperature": -18.3, "isDeviation": false },
        { "timestamp": "2026-03-30T00:00:00Z", "avgTemperature": -12.1, "isDeviation": true }
      ]
    }
  ],
  "deviations": [
    {
      "id": 1,
      "unitId": 2,
      "unitName": "Fryser #2",
      "temperature": -12.1,
      "timestamp": "2026-03-30T08:12:00Z",
      "status": "OPEN"
    }
  ]
}
```

The frontend `ChartData` shape (with `labels`, `datasets`, `alerts`) is the internal representation that the service layer will need to transform from this backend response. The transformation would align all series to the same label array and build the `alerts` list with array indices rather than timestamps.

---

## What still needs implementation

| Item                    | Notes                                                                                       |
|-------------------------|---------------------------------------------------------------------------------------------|
| Real export (PDF)       | `reportsService.exportPdf()` calls `alert()`. Needs `POST /api/reports/export/pdf` and a download trigger (e.g. `window.open` or a blob URL). |
| Real export (JSON)      | `reportsService.exportJson()` calls `alert()`. Needs `GET /api/reports/export/json` and a file download. |
| Unit selector           | Currently all four hardcoded units are always shown. A unit multi-select would let users focus on specific units. |
| Date range picker       | Period is either last 7 or last 30 days. A custom date range picker would enable arbitrary reporting windows. |
| Real API integration    | `reportsService.getChartData()` returns mock data. The service comment (`// Real: return (await api.get<ChartData>(`/reports/chart?period=${period}`)).data`) indicates where the real call goes, but the backend response shape differs from the frontend `ChartData` format — a mapping layer is needed. |
| Loading state for chart | The loading state shows "Laster data…" in the chart area, but the period toggle does not disable itself during a fetch — rapid toggling could result in out-of-order responses. |
