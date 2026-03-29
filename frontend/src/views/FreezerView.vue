<template>
  <div class="freezer-view">
    <div class="flex items-center gap-3 mb-4">
      <h1 class="page-title mb-0">Fryser</h1>
      <span class="mod-badge ik-mat">IK-Mat</span>
    </div>

    <div v-if="unitsStore.loading" class="text-muted text-sm mb-4">Laster enheter…</div>

    <template v-else-if="freezerUnits.length">
      <nav class="sub-nav mb-4">
        <button
          v-for="unit in freezerUnits"
          :key="unit.id"
          class="sub-nav-item"
          :class="{ active: selectedUnitId === unit.id }"
          @click="selectUnit(unit.id)"
        >
          {{ unit.name }}
        </button>
      </nav>

      <template v-if="selectedUnit">
        <div
          v-if="selectedUnit.hasAlert"
          class="alert-banner danger mb-4"
        >
          Siste måling utenfor grenseverdi! Ta tiltak umiddelbart.
        </div>

        <!-- Unit info -->
        <div class="card mb-4">
          <div class="card-header">
            <span class="section-title">{{ selectedUnit.name }}</span>
          </div>
          <div class="unit-info-grid">
            <div class="info-item">
              <span class="text-muted text-sm">Måltemperatur</span>
              <span class="info-value">{{ selectedUnit.targetTemp }}&nbsp;°C</span>
            </div>
            <div class="info-item">
              <span class="text-muted text-sm">Akseptabelt område</span>
              <span class="info-value">{{ selectedUnit.minTemp }}&nbsp;°C – {{ selectedUnit.maxTemp }}&nbsp;°C</span>
            </div>
            <div class="info-item">
              <span class="text-muted text-sm">Innhold</span>
              <span class="info-value">{{ selectedUnit.contents || '—' }}</span>
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
                <label for="temperature">Temperatur (°C) <span class="text-danger">*</span></label>
                <input
                  id="temperature"
                  v-model.number="form.temperature"
                  type="number"
                  step="0.1"
                  placeholder="-18.0"
                  :class="{ 'input-error': formError }"
                  required
                />
                <span v-if="formError" class="text-danger text-xs">{{ formError }}</span>
              </div>

              <div class="form-group">
                <label for="recordedAt">Tidspunkt</label>
                <input
                  id="recordedAt"
                  v-model="form.recordedAt"
                  type="time"
                />
              </div>
            </div>

            <div class="form-group mb-3">
              <label for="note">Merknad</label>
              <input
                id="note"
                v-model="form.note"
                type="text"
                placeholder="Merknad — valgfritt"
              />
            </div>

            <div class="flex items-center justify-between">
              <span class="text-muted text-sm">
                Logges som: <strong>{{ loggedInName }}</strong>
              </span>
              <button
                type="submit"
                class="btn btn-primary"
                :disabled="readingsStore.saving"
              >
                {{ readingsStore.saving ? 'Lagrer…' : 'Lagre måling' }}
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

          <template v-else-if="recentReadings.length">
            <div
              v-for="reading in recentReadings"
              :key="reading.id"
              class="status-row"
              :class="{ 'is-alert': reading.isOutOfRange }"
            >
              <div class="flex-1 min-w-0">
                <div class="text-sm">{{ formatDateTime(reading.recordedAt) }}</div>
                <div class="text-xs text-muted">{{ reading.recordedBy }}</div>
                <div v-if="reading.note" class="text-xs text-muted mt-1">{{ reading.note }}</div>
              </div>
              <div
                class="reading-temp"
                :class="reading.isOutOfRange ? 'text-danger' : 'text-success'"
              >
                {{ reading.temperature.toFixed(1) }}&nbsp;°C
              </div>
            </div>
          </template>

          <div v-else class="text-muted text-sm">Ingen målinger registrert ennå.</div>
        </div>
      </template>
    </template>

    <div v-else class="text-muted text-sm">Ingen fryserenheter funnet.</div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useUnitsStore } from '@/stores/units'
import { useReadingsStore } from '@/stores/readings'
import { useAuthStore } from '@/stores/auth'

// ─── Stores ────────────────────────────────────────────────────────────────
const unitsStore = useUnitsStore()
const readingsStore = useReadingsStore()
const authStore = useAuthStore()

// ─── Units ─────────────────────────────────────────────────────────────────
const freezerUnits = computed(() => unitsStore.getByType('FREEZER'))
const selectedUnitId = ref<number | null>(null)
const selectedUnit = computed(
  () => freezerUnits.value.find(u => u.id === selectedUnitId.value) ?? null
)

function selectUnit(id: number) {
  selectedUnitId.value = id
  readingsStore.fetchByUnit(id)
  resetForm()
  successFlash.value = false
}

// ─── Auth ──────────────────────────────────────────────────────────────────
const loggedInName = computed(() => {
  const u = authStore.user
  if (!u) return '—'
  return [u.firstName, u.lastName].filter(Boolean).join(' ') || u.email || '—'
})

// ─── Form ──────────────────────────────────────────────────────────────────
function currentTimeHHMM(): string {
  const now = new Date()
  return `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
}

const form = ref({ temperature: null as number | null, recordedAt: currentTimeHHMM(), note: '' })
const formError = ref('')
const successFlash = ref(false)
let flashTimer: ReturnType<typeof setTimeout> | null = null

function resetForm() {
  form.value = { temperature: null, recordedAt: currentTimeHHMM(), note: '' }
  formError.value = ''
}

async function submitReading() {
  formError.value = ''
  if (form.value.temperature === null || form.value.temperature === undefined || isNaN(form.value.temperature as number)) {
    formError.value = 'Temperatur er påkrevd.'
    return
  }
  if (!selectedUnit.value) return

  const today = new Date()
  const [h, m] = form.value.recordedAt.split(':').map(Number)
  today.setHours(h, m, 0, 0)

  await readingsStore.addReading({
    unitId: selectedUnit.value.id,
    temperature: form.value.temperature as number,
    recordedAt: today.toISOString(),
    note: form.value.note || undefined,
  })

  resetForm()
  successFlash.value = true
  if (flashTimer) clearTimeout(flashTimer)
  flashTimer = setTimeout(() => { successFlash.value = false }, 3000)
  readingsStore.fetchByUnit(selectedUnit.value.id)
}

// ─── Readings ──────────────────────────────────────────────────────────────
const recentReadings = computed(() => {
  if (!selectedUnit.value) return []
  return readingsStore.getByUnit(selectedUnit.value.id).slice(0, 5)
})

// ─── Datetime helper ───────────────────────────────────────────────────────
function formatDateTime(iso: string): string {
  const d = new Date(iso)
  const today = new Date()
  const yesterday = new Date(); yesterday.setDate(today.getDate() - 1)
  const time = `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
  if (d.toDateString() === today.toDateString()) return `I dag ${time}`
  if (d.toDateString() === yesterday.toDateString()) return `I går ${time}`
  return `${d.getDate()}. ${['jan','feb','mar','apr','mai','jun','jul','aug','sep','okt','nov','des'][d.getMonth()]} ${time}`
}

// ─── Lifecycle ─────────────────────────────────────────────────────────────
onMounted(async () => {
  await unitsStore.fetchUnits()
  if (freezerUnits.value.length) {
    selectUnit(freezerUnits.value[0].id)
  }
})

watch(freezerUnits, (units) => {
  if (units.length && selectedUnitId.value === null) {
    selectUnit(units[0].id)
  }
})
</script>

<style scoped>
.freezer-view {
  max-width: 720px;
}

.unit-info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  padding: 1rem;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.info-value {
  font-weight: 600;
  color: var(--c-text);
}

.reading-temp {
  font-weight: 700;
  font-size: 1.05rem;
  white-space: nowrap;
  padding-left: 1rem;
}

input[type='number'],
input[type='time'],
input[type='text'] {
  width: 100%;
  padding: 0.5rem 0.75rem;
  background: var(--c-surface-2);
  border: 1px solid var(--c-border);
  border-radius: var(--r-sm);
  color: var(--c-text);
  font-size: 0.95rem;
  transition: border-color 0.15s;
}

input[type='number']:focus,
input[type='time']:focus,
input[type='text']:focus {
  outline: none;
  border-color: var(--c-primary);
}

input.input-error {
  border-color: var(--c-danger);
}

label {
  display: block;
  font-size: 0.875rem;
  color: var(--c-text-2);
  margin-bottom: 0.375rem;
}

@media (max-width: 520px) {
  .unit-info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
