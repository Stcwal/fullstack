<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUnitsStore } from '@/stores/units'
import { useReadingsStore } from '@/stores/readings'

const unitsStore    = useUnitsStore()
const readingsStore = useReadingsStore()

const PAGE_SIZE = 10

const tempUnits = computed(() =>
  unitsStore.units.filter(u => u.active && ['FREEZER', 'FRIDGE', 'COOLER'].includes(u.type))
)

// Per-unit current page (0-indexed)
const pages = ref<Record<number, number>>({})

function getPage(unitId: number): number {
  return pages.value[unitId] ?? 0
}

function setPage(unitId: number, page: number) {
  pages.value = { ...pages.value, [unitId]: page }
}

function allReadings(unitId: number) {
  return readingsStore.getByUnit(unitId)
}

function pagedReadings(unitId: number) {
  const page = getPage(unitId)
  return allReadings(unitId).slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE)
}

function totalPages(unitId: number) {
  return Math.max(1, Math.ceil(allReadings(unitId).length / PAGE_SIZE))
}

const typeLabel: Record<string, string> = {
  FREEZER: 'Fryser', FRIDGE: 'Kjøleskap', COOLER: 'Kjøler', OTHER: 'Annet'
}

function formatDateTime(iso: string): string {
  const d = new Date(iso)
  const today = new Date()
  const yesterday = new Date(); yesterday.setDate(today.getDate() - 1)
  const time = `${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
  if (d.toDateString() === today.toDateString()) return `I dag ${time}`
  if (d.toDateString() === yesterday.toDateString()) return `I går ${time}`
  const months = ['jan','feb','mar','apr','mai','jun','jul','aug','sep','okt','nov','des']
  return `${d.getDate()}. ${months[d.getMonth()]} ${time}`
}

onMounted(async () => {
  await unitsStore.fetchUnits()
  for (const unit of tempUnits.value) {
    readingsStore.fetchByUnit(unit.id)
  }
})
</script>

<template>
  <div class="logg-view">
    <div class="flex items-center gap-3 mb-4">
      <h1 class="page-title mb-0">Temperaturlogg</h1>
      <span class="mod-badge ik-mat">IK-Mat</span>
    </div>

    <div v-if="unitsStore.loading" class="text-muted text-sm">Laster enheter…</div>

    <template v-else-if="tempUnits.length">
      <section
        v-for="unit in tempUnits"
        :key="unit.id"
        class="unit-section card mb-4"
        :aria-label="unit.name"
      >
        <!-- Unit header -->
        <div class="card-header">
          <div class="unit-header-left">
            <span class="section-title">{{ unit.name }}</span>
            <span class="badge badge-neutral">{{ typeLabel[unit.type] ?? unit.type }}</span>
          </div>
          <div class="unit-header-right">
            <span class="text-muted text-sm">
              Mål: {{ unit.targetTemp }}&nbsp;°C &nbsp;·&nbsp;
              Område: {{ unit.minTemp }}–{{ unit.maxTemp }}&nbsp;°C
            </span>
          </div>
        </div>

        <!-- Readings table -->
        <div v-if="readingsStore.loading" class="text-muted text-sm p-row">Laster målinger…</div>

        <template v-else-if="allReadings(unit.id).length">
          <table class="readings-table" aria-label="Temperaturmålinger">
            <thead>
              <tr>
                <th>Tidspunkt</th>
                <th>Temperatur</th>
                <th>Registrert av</th>
                <th>Merknad</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="reading in pagedReadings(unit.id)"
                :key="reading.id"
                :class="{ 'row-alert': reading.isOutOfRange }"
              >
                <td class="col-time">{{ formatDateTime(reading.recordedAt) }}</td>
                <td class="col-temp" :class="reading.isOutOfRange ? 'text-danger' : 'text-success'">
                  {{ reading.temperature >= 0 ? '+' : '' }}{{ reading.temperature.toFixed(1) }}&nbsp;°C
                </td>
                <td class="col-user">{{ reading.recordedBy }}</td>
                <td class="col-note text-muted">{{ reading.note || '—' }}</td>
                <td class="col-status">
                  <span v-if="reading.isOutOfRange" class="status-badge status-alert">Avvik</span>
                  <span v-else class="status-badge status-ok">OK</span>
                </td>
              </tr>
            </tbody>
          </table>

          <!-- Pagination -->
          <div v-if="totalPages(unit.id) > 1" class="pagination">
            <button
              class="page-btn"
              :disabled="getPage(unit.id) === 0"
              aria-label="Forrige side"
              @click="setPage(unit.id, getPage(unit.id) - 1)"
            >
              ←
            </button>
            <span class="page-info">
              Side {{ getPage(unit.id) + 1 }} av {{ totalPages(unit.id) }}
            </span>
            <button
              class="page-btn"
              :disabled="getPage(unit.id) >= totalPages(unit.id) - 1"
              aria-label="Neste side"
              @click="setPage(unit.id, getPage(unit.id) + 1)"
            >
              →
            </button>
          </div>

          <div class="readings-count text-muted text-sm">
            Totalt {{ allReadings(unit.id).length }} målinger
          </div>
        </template>

        <div v-else class="text-muted text-sm p-row">Ingen målinger registrert ennå.</div>
      </section>
    </template>

    <div v-else class="text-muted text-sm">Ingen temperaturenheter funnet.</div>
  </div>
</template>

<style scoped>
.logg-view {
  max-width: 900px;
}

.unit-header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.unit-header-right {
  margin-left: auto;
}

.p-row {
  padding: 12px 16px;
}

/* Table */
.readings-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.875rem;
}

.readings-table th {
  text-align: left;
  padding: 10px 16px;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--c-text-2);
  text-transform: uppercase;
  letter-spacing: 0.04em;
  border-bottom: 1px solid var(--c-border);
  background: var(--c-surface-2);
}

.readings-table th:first-child { border-radius: 0; }

.readings-table td {
  padding: 10px 16px;
  border-bottom: 1px solid var(--c-border);
  vertical-align: middle;
}

.readings-table tbody tr:last-child td {
  border-bottom: none;
}

.readings-table tbody tr:hover {
  background: var(--c-surface-2);
}

.row-alert {
  background: #fff8f8;
}
.row-alert:hover {
  background: #fff1f2 !important;
}

.col-time  { white-space: nowrap; color: var(--c-text); }
.col-temp  { font-weight: 700; font-size: 0.9375rem; white-space: nowrap; }
.col-user  { color: var(--c-text-2); }
.col-note  { max-width: 220px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.col-status { white-space: nowrap; }

/* Status badges */
.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 0.6875rem;
  font-weight: 700;
  letter-spacing: 0.03em;
}
.status-ok    { background: #f0fdf4; color: #16a34a; }
.status-alert { background: #fff1f2; color: #dc2626; }

/* Pagination */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 12px 16px;
  border-top: 1px solid var(--c-border);
}

.page-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: 1px solid var(--c-border);
  border-radius: var(--r-sm);
  background: var(--c-surface);
  color: var(--c-text);
  font-size: 1rem;
  cursor: pointer;
  transition: background 0.1s, border-color 0.1s;
}
.page-btn:hover:not(:disabled) { background: var(--c-surface-2); border-color: var(--c-text-3); }
.page-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.page-btn:focus-visible { outline: 2px solid var(--c-primary); outline-offset: 2px; }

.page-info {
  font-size: 0.875rem;
  color: var(--c-text-2);
  min-width: 100px;
  text-align: center;
}

.readings-count {
  padding: 8px 16px 12px;
  text-align: right;
}
</style>
