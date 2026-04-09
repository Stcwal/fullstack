<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useUnitsStore } from '@/stores/units'
import { useReadingsStore } from '@/stores/readings'
import { useLayoutStore } from '@/stores/layout'
import { useShiftStore } from '@/stores/shift'
import type { UnitType } from '@/types'

const unitsStore    = useUnitsStore()
const readingsStore = useReadingsStore()
const layout        = useLayoutStore()
const shiftStore    = useShiftStore()

const isTablet = computed(() => layout.isTabletMode)

// ── All active temperature-monitoring units ──────────────────────────────────
const tempUnits = computed(() =>
  unitsStore.units.filter(u => u.active && ['FREEZER', 'FRIDGE', 'COOLER'].includes(u.type))
)

// ── Active/selected unit (shared by both layouts) ────────────────────────────
const activeUnitId = ref<number | null>(null)
const activeUnit = computed(() =>
  tempUnits.value.find(u => u.id === activeUnitId.value) ?? null
)

const loggerPanelRef = ref<HTMLElement | null>(null)

function selectUnit(id: number) {
  activeUnitId.value = id
  readingsStore.fetchByUnit(id)
  resetForm()
  successFlash.value = false
}

function openLogger(id: number) {
  if (activeUnitId.value === id) { activeUnitId.value = null; return }
  activeUnitId.value = id
  resetForm()
  nextTick(() => {
    loggerPanelRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  })
}

function cancelLog() { activeUnitId.value = null }

// ── Auth / active shift worker ────────────────────────────────────────────────
const loggedInName = computed(() => shiftStore.activeWorkerName)

// ── Form (shared) ─────────────────────────────────────────────────────────────
function currentTimeHHMM(): string {
  const now = new Date()
  return `${String(now.getHours()).padStart(2,'0')}:${String(now.getMinutes()).padStart(2,'0')}`
}

const form = ref<{ temperature: number | null; note: string; recordedAt: string }>({
  temperature: null, note: '', recordedAt: currentTimeHHMM()
})
const formError    = ref('')
const successFlash = ref(false)
const saving       = ref(false)
let flashTimer: ReturnType<typeof setTimeout> | null = null

function resetForm() {
  form.value = { temperature: null, note: '', recordedAt: currentTimeHHMM() }
  formError.value = ''
}

async function submitReading() {
  formError.value = ''
  if (!Number.isFinite(form.value.temperature as number)) {
    formError.value = 'Temperatur er påkrevd.'
    return
  }
  if (!activeUnit.value) return

  let recordedAt: string
  if (isTablet.value) {
    recordedAt = new Date().toISOString()
  } else {
    const today = new Date()
    const [h, m] = form.value.recordedAt.split(':').map(Number)
    today.setHours(h, m, 0, 0)
    recordedAt = today.toISOString()
  }

  saving.value = true
  try {
    await readingsStore.addReading({
      unitId: activeUnit.value.id,
      temperature: form.value.temperature as number,
      recordedAt,
      note: form.value.note || undefined,
      performedByUserId: shiftStore.activeWorkerId ?? undefined,
    })
    if (isTablet.value) {
      activeUnitId.value = null
    } else {
      resetForm()
      successFlash.value = true
      if (flashTimer) clearTimeout(flashTimer)
      flashTimer = setTimeout(() => { successFlash.value = false }, 3000)
      readingsStore.fetchByUnit(activeUnit.value.id)
    }
  } finally {
    saving.value = false
  }
}

// ── Desktop: recent readings ──────────────────────────────────────────────────
const recentReadings = computed(() => {
  if (!activeUnit.value) return []
  return readingsStore.getByUnit(activeUnit.value.id).slice(0, 5)
})

// ── Tablet: card state helpers ────────────────────────────────────────────────
function lastReading(unitId: number) {
  const r = readingsStore.getByUnit(unitId)
  return r.length ? r[0] : null
}

function cardState(unitId: number): 'active' | 'ok' | 'alert' | 'empty' {
  if (activeUnitId.value === unitId) return 'active'
  const r = lastReading(unitId)
  if (!r) return 'empty'
  return r.isDeviation ? 'alert' : 'ok'
}

// ── Shared helpers ────────────────────────────────────────────────────────────
const typeLabel: Record<string, string> = {
  FREEZER: 'FRYSER', FRIDGE: 'KJØLESKAP', COOLER: 'KJØLER', OTHER: 'ANNET'
}

function unitTypeName(type: UnitType): string {
  const map: Record<string, string> = { FREEZER: 'Fryser', FRIDGE: 'Kjøleskap', COOLER: 'Kjøler', OTHER: 'Annet' }
  return map[type] ?? type
}

function formatTime(iso: string): string {
  const d = new Date(iso)
  return `${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

function formatDateTime(iso: string): string {
  const d = new Date(iso)
  const today = new Date()
  const yesterday = new Date(); yesterday.setDate(today.getDate() - 1)
  const time = formatTime(iso)
  if (d.toDateString() === today.toDateString()) return `I dag ${time}`
  if (d.toDateString() === yesterday.toDateString()) return `I går ${time}`
  const months = ['jan','feb','mar','apr','mai','jun','jul','aug','sep','okt','nov','des']
  return `${d.getDate()}. ${months[d.getMonth()]} ${time}`
}

// ── Tablet header date ────────────────────────────────────────────────────────
const now = new Date()
const months = ['jan','feb','mar','apr','mai','jun','jul','aug','sep','okt','nov','des']
const dateLabel = `${now.getDate()}. ${months[now.getMonth()]} · ${String(now.getHours()).padStart(2,'0')}:${String(now.getMinutes()).padStart(2,'0')}`

// ── Lifecycle ─────────────────────────────────────────────────────────────────
onMounted(async () => {
  await unitsStore.fetchUnits()
  if (tempUnits.value.length) {
    if (!isTablet.value) {
      selectUnit(tempUnits.value[0].id)
    } else {
      for (const unit of tempUnits.value) readingsStore.fetchByUnit(unit.id)
    }
  }
})

watch(isTablet, (tablet) => {
  activeUnitId.value = null
  if (!tablet && tempUnits.value.length) selectUnit(tempUnits.value[0].id)
  if (tablet) for (const unit of tempUnits.value) readingsStore.fetchByUnit(unit.id)
})

watch(tempUnits, (units) => {
  if (units.length && activeUnitId.value === null && !isTablet.value) {
    selectUnit(units[0].id)
  }
})
</script>

<template>
  <!-- ═══════════════════════════════════════════════
       TABLET LAYOUT — card grid + inline logger
  ════════════════════════════════════════════════ -->
  <div v-if="isTablet" class="temp-view">
    <div class="temp-header">
      <h1 class="temp-title">Temperaturlogging</h1>
      <span class="temp-date">{{ dateLabel }}</span>
    </div>

    <div v-if="unitsStore.loading" class="temp-loading">Laster enheter…</div>

    <template v-else>
      <div class="unit-grid" role="list">
        <button
          v-for="unit in tempUnits"
          :key="unit.id"
          class="unit-card"
          :class="`unit-card--${cardState(unit.id)}`"
          role="listitem"
          :aria-label="`${unit.name}`"
          @click="openLogger(unit.id)"
        >
          <div class="card-type">{{ typeLabel[unit.type] ?? unit.type }}</div>
          <div class="card-name">{{ unit.name }}</div>

          <template v-if="cardState(unit.id) === 'ok'">
            <div class="card-temp card-temp--ok">
              {{ lastReading(unit.id)!.temperature >= 0 ? '+' : '' }}{{ lastReading(unit.id)!.temperature.toFixed(1) }}°
            </div>
            <div class="card-meta">✓ {{ formatTime(lastReading(unit.id)!.recordedAt) }} · {{ lastReading(unit.id)!.recordedBy.name }}</div>
          </template>

          <template v-else-if="cardState(unit.id) === 'alert'">
            <div class="card-temp card-temp--alert">
              {{ lastReading(unit.id)!.temperature >= 0 ? '+' : '' }}{{ lastReading(unit.id)!.temperature.toFixed(1) }}°
            </div>
            <div class="card-meta card-meta--alert">⚠️ Avvik · {{ formatTime(lastReading(unit.id)!.recordedAt) }} · {{ lastReading(unit.id)!.recordedBy.name }}</div>
          </template>

          <template v-else-if="cardState(unit.id) === 'active'">
            <div class="card-logging">▼ Logger nå…</div>
          </template>

          <template v-else>
            <div class="card-empty">Ikke logget</div>
            <div class="card-cta">Trykk for å logge →</div>
          </template>
        </button>
      </div>

      <!-- Inline logger -->
      <div v-if="activeUnit" ref="loggerPanelRef" class="logger-panel" role="form" :aria-label="`Logger for ${activeUnit.name}`">
        <div class="logger-header">🌡️ Logger — {{ activeUnit.name }}</div>
        <div class="logger-row">
          <div class="temp-side">
            <input
              v-model.number="form.temperature"
              type="number"
              step="0.1"
              class="temp-input"
              :class="{ 'temp-input--error': formError }"
              placeholder="0"
              aria-label="Temperatur i grader Celsius"
              @keydown.enter="submitReading"
            />
            <span class="temp-unit-label">°C</span>
          </div>
          <div class="note-side">
            <input
              v-model="form.note"
              type="text"
              class="note-input"
              placeholder="Merknad (valgfritt)"
              aria-label="Merknad"
              @keydown.enter="submitReading"
            />
            <button class="cancel-btn" @click="cancelLog" aria-label="Avbryt logging">×</button>
          </div>
        </div>
        <p v-if="formError" class="form-error" role="alert">{{ formError }}</p>
        <button class="lagre-btn" :disabled="saving" @click="submitReading">
          {{ saving ? 'Lagrer…' : 'Lagre' }}
        </button>
      </div>

      <div v-if="!tempUnits.length" class="temp-loading">Ingen temperaturenheter funnet.</div>
    </template>
  </div>

  <!-- ═══════════════════════════════════════════════
       DESKTOP LAYOUT — sidebar sub-nav + form
  ════════════════════════════════════════════════ -->
  <div v-else class="desktop-view">
    <div class="flex items-center gap-3 mb-4">
      <h1 class="page-title mb-0">Temperaturlogging</h1>
      <span class="mod-badge ik-mat">IK-Mat</span>
    </div>

    <div v-if="unitsStore.loading" class="text-muted text-sm mb-4">Laster enheter…</div>

    <template v-else-if="tempUnits.length">
      <nav class="sub-nav mb-4" aria-label="Velg enhet">
        <button
          v-for="unit in tempUnits"
          :key="unit.id"
          class="sub-nav-item"
          :class="{ active: activeUnitId === unit.id }"
          @click="selectUnit(unit.id)"
        >
          {{ unit.name }}
          <span class="sub-nav-type">{{ unitTypeName(unit.type) }}</span>
        </button>
      </nav>

      <template v-if="activeUnit">
        <div v-if="activeUnit.hasAlert" class="alert-banner danger mb-4">
          Siste måling utenfor grenseverdi! Ta tiltak umiddelbart.
        </div>

        <!-- Unit info -->
        <div class="card mb-4">
          <div class="card-header">
            <span class="section-title">{{ activeUnit.name }}</span>
            <span class="badge badge-neutral">{{ unitTypeName(activeUnit.type) }}</span>
          </div>
          <div class="unit-info-grid">
            <div class="info-item">
              <span class="text-muted text-sm">Måltemperatur</span>
              <span class="info-value">{{ activeUnit.targetTemperature }}&nbsp;°C</span>
            </div>
            <div class="info-item">
              <span class="text-muted text-sm">Akseptabelt område</span>
              <span class="info-value">{{ activeUnit.minThreshold }}&nbsp;°C – {{ activeUnit.maxThreshold }}&nbsp;°C</span>
            </div>
            <div class="info-item">
              <span class="text-muted text-sm">Innhold</span>
              <span class="info-value">{{ activeUnit.description || '—' }}</span>
            </div>
          </div>
        </div>

        <!-- New measurement form -->
        <div class="card mb-4">
          <div class="card-header">
            <span class="section-title">Ny måling</span>
          </div>

          <div v-if="successFlash" class="alert-banner info mb-3">Lagret!</div>

          <form @submit.prevent="submitReading">
            <div class="form-grid-2 mb-3">
              <div class="form-group">
                <label for="d-temperature">Temperatur (°C) <span class="text-danger">*</span></label>
                <input
                  id="d-temperature"
                  v-model.number="form.temperature"
                  type="number"
                  step="0.1"
                  placeholder="-18.0"
                  :class="{ 'input-error': formError }"
                />
                <span v-if="formError" class="text-danger text-xs">{{ formError }}</span>
              </div>
              <div class="form-group">
                <label for="d-recordedAt">Tidspunkt</label>
                <input id="d-recordedAt" v-model="form.recordedAt" type="time" />
              </div>
            </div>
            <div class="form-group mb-3">
              <label for="d-note">Merknad</label>
              <input id="d-note" v-model="form.note" type="text" placeholder="Merknad — valgfritt" />
            </div>
            <div class="flex items-center justify-between">
              <span class="text-muted text-sm">Logges som: <strong>{{ loggedInName }}</strong></span>
              <button type="submit" class="btn btn-primary" :disabled="saving || readingsStore.saving">
                {{ (saving || readingsStore.saving) ? 'Lagrer…' : 'Lagre måling' }}
              </button>
            </div>
          </form>
        </div>

        <!-- Recent measurements -->
        <div class="card">
          <div class="card-header">
            <span class="section-title">Siste målinger</span>
          </div>
          <div v-if="readingsStore.loading" class="text-muted text-sm">Laster målinger…</div>
          <div v-else-if="readingsStore.error" class="text-xs" style="color: var(--c-danger)">{{ readingsStore.error }}</div>
          <template v-else-if="recentReadings.length">
            <div
              v-for="reading in recentReadings"
              :key="reading.id"
              class="status-row"
              :class="{ 'is-alert': reading.isDeviation }"
            >
              <div class="flex-1 min-w-0">
                <div class="text-sm">{{ formatDateTime(reading.recordedAt) }}</div>
                <div class="text-xs text-muted">{{ reading.recordedBy.name }}</div>
                <div v-if="reading.note" class="text-xs text-muted mt-1">{{ reading.note }}</div>
              </div>
              <div class="reading-temp" :class="reading.isDeviation ? 'text-danger' : 'text-success'">
                {{ reading.temperature.toFixed(1) }}&nbsp;°C
              </div>
            </div>
          </template>
          <div v-else class="text-muted text-sm">Ingen målinger registrert ennå.</div>
        </div>
      </template>
    </template>

    <div v-else class="text-muted text-sm">Ingen temperaturenheter funnet.</div>
  </div>
</template>

<style scoped>
/* ════════════════════════════════════════
   DESKTOP styles
════════════════════════════════════════ */
.desktop-view {
  max-width: 720px;
}

.sub-nav-type {
  font-size: 0.6875rem;
  font-weight: 400;
  color: var(--c-text-3);
  margin-left: 4px;
}

.unit-info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  padding: 1rem;
}

.info-item { display: flex; flex-direction: column; gap: 0.25rem; }
.info-value { font-weight: 600; color: var(--c-text); }

.reading-temp {
  font-weight: 700;
  font-size: 1.05rem;
  white-space: nowrap;
  padding-left: 1rem;
}

.desktop-view input[type='number'],
.desktop-view input[type='time'],
.desktop-view input[type='text'] {
  width: 100%;
  padding: 0.5rem 0.75rem;
  background: var(--c-surface-2);
  border: 1px solid var(--c-border);
  border-radius: var(--r-sm);
  color: var(--c-text);
  font-size: 0.95rem;
  transition: border-color 0.15s;
  font-family: inherit;
}
.desktop-view input[type='number']:focus,
.desktop-view input[type='time']:focus,
.desktop-view input[type='text']:focus { outline: none; border-color: var(--c-primary); }
.desktop-view input.input-error { border-color: var(--c-danger); }

.desktop-view label {
  display: block;
  font-size: 0.875rem;
  color: var(--c-text-2);
  margin-bottom: 0.375rem;
}

@media (max-width: 520px) {
  .unit-info-grid { grid-template-columns: 1fr; }
}

/* ════════════════════════════════════════
   TABLET styles
════════════════════════════════════════ */
.temp-view {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 4px 0;
}

.temp-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
}
.temp-title {
  font-size: 1.375rem;
  font-weight: 700;
  color: var(--c-text);
  margin: 0;
}
.temp-date {
  font-size: 0.8125rem;
  color: var(--c-text-3);
  white-space: nowrap;
}
.temp-loading {
  font-size: 0.9rem;
  color: var(--c-text-3);
  padding: 24px 0;
  text-align: center;
}

/* Card grid */
.unit-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.unit-card {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 14px 16px;
  border-radius: 14px;
  border: 1.5px solid var(--c-border);
  background: var(--c-surface);
  cursor: pointer;
  text-align: left;
  transition: border-color 0.15s, background 0.15s;
  min-height: 110px;
}
.unit-card:focus-visible { outline: 2px solid var(--c-primary); outline-offset: 2px; }

.unit-card--ok    { background: #f0fdf4; border-color: #86efac; }
.unit-card--alert { background: #fff1f2; border-color: #fca5a5; }
.unit-card--active { background: #eef2ff; border-color: #818cf8; border-left-width: 4px; }

.card-type {
  font-size: 0.6875rem;
  font-weight: 700;
  letter-spacing: 0.06em;
  color: #94a3b8;
}
.unit-card--ok    .card-type { color: #4ade80; }
.unit-card--alert .card-type { color: #f87171; }
.unit-card--active .card-type { color: #818cf8; }

.card-name { font-size: 1rem; font-weight: 700; color: var(--c-text); line-height: 1.2; }

.card-temp {
  font-size: 1.875rem;
  font-weight: 800;
  letter-spacing: -0.02em;
  line-height: 1;
  margin: 4px 0 2px;
}
.card-temp--ok    { color: #16a34a; }
.card-temp--alert { color: #dc2626; }

.card-meta { font-size: 0.75rem; color: #4ade80; font-weight: 500; }
.card-meta--alert { color: #f87171; }

.card-logging { font-size: 0.875rem; font-weight: 600; color: #6366f1; margin-top: 8px; }
.card-empty { font-size: 1rem; font-weight: 600; color: #94a3b8; margin-top: 8px; }
.card-cta { font-size: 0.8125rem; font-weight: 600; color: #3b82f6; }

/* Logger panel */
.logger-panel {
  background: var(--c-surface);
  border: 2px solid #818cf8;
  border-radius: 14px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.logger-header { font-size: 1rem; font-weight: 700; color: #6366f1; }

.logger-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.temp-side {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.note-side {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.temp-input {
  width: 120px;
  height: 64px;
  font-size: 2.5rem;
  font-weight: 800;
  text-align: center;
  border: 2px solid #818cf8;
  border-radius: 10px;
  background: #fff;
  color: var(--c-text);
  outline: none;
  flex-shrink: 0;
  transition: border-color 0.15s;
  font-family: inherit;
  padding: 0 4px;
}
.temp-input:focus { border-color: #6366f1; }
.temp-input--error { border-color: #dc2626; }

.temp-unit-label { font-size: 1.5rem; font-weight: 600; color: #94a3b8; flex-shrink: 0; }

.note-input {
  flex: 1;
  min-width: 0;
  height: 64px;
  padding: 0 14px;
  font-size: 0.9375rem;
  border: 1.5px solid var(--c-border);
  border-radius: 10px;
  background: var(--c-surface-2);
  color: var(--c-text);
  outline: none;
  transition: border-color 0.15s;
  font-family: inherit;
}
.note-input:focus { border-color: #818cf8; }

.cancel-btn {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  background: #dc2626;
  border: none;
  border-radius: 8px;
  color: #fff;
  cursor: pointer;
  flex-shrink: 0;
  transition: background 0.1s;
}
.cancel-btn:hover { background: #b91c1c; }

.form-error { font-size: 0.8125rem; color: #dc2626; margin: 0; }

.lagre-btn {
  width: 100%;
  min-height: 52px;
  background: #16a34a;
  color: #fff;
  font-size: 1.0625rem;
  font-weight: 700;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.1s;
  font-family: inherit;
}
.lagre-btn:hover:not(:disabled) { background: #15803d; }
.lagre-btn:disabled { opacity: 0.6; cursor: not-allowed; }
</style>
