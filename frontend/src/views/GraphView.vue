<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Line } from 'vue-chartjs'
import { useAuthStore } from '@/stores/auth'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js'
import { reportsService } from '@/services/reports.service'
import type { ChartPeriod } from '@/types'
import type { ChartData as ServiceChartData } from '@/services/reports.service'

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend)

const authStore = useAuthStore()
const canExport = computed(() =>
  authStore.user?.role === 'ADMIN' || authStore.user?.role === 'MANAGER'
)

// ── State ────────────────────────────────────────────────────────────────────
const period    = ref<ChartPeriod>('WEEK')
const chartData = ref<ServiceChartData | null>(null)
const loading   = ref(false)
const exporting = ref<'pdf' | 'json' | null>(null)

// ── Filtering ─────────────────────────────────────────────────────────────────
const typeFilter  = ref<string>('ALL')
const hiddenUnits = ref<Set<string>>(new Set())

const unitTypeLabels: Record<string, string> = {
  FREEZER: 'Frysere',
  FRIDGE:  'Kjøleskap',
  COOLER:  'Kjølere',
  DISPLAY: 'Utstilling',
  OTHER:   'Annet',
}

const availableTypes = computed(() => {
  if (!chartData.value) return []
  return [...new Set(chartData.value.datasets.map(ds => ds.unitType))]
})

const typeFilteredDatasets = computed(() => {
  if (!chartData.value) return []
  return chartData.value.datasets.filter(ds =>
    typeFilter.value === 'ALL' || ds.unitType === typeFilter.value
  )
})

const visibleDatasets = computed(() =>
  typeFilteredDatasets.value.filter(ds => !hiddenUnits.value.has(ds.label))
)

function setTypeFilter(type: string) {
  typeFilter.value = type
  hiddenUnits.value = new Set()
}

function toggleUnit(label: string) {
  const next = new Set(hiddenUnits.value)
  if (next.has(label)) next.delete(label)
  else next.add(label)
  hiddenUnits.value = next
}

// ── Data fetching ─────────────────────────────────────────────────────────────
async function loadData() {
  loading.value = true
  try {
    chartData.value  = await reportsService.getChartData(period.value)
    typeFilter.value = 'ALL'
    hiddenUnits.value = new Set()
  } finally {
    loading.value = false
  }
}

async function setPeriod(p: ChartPeriod) {
  if (period.value === p) return
  period.value = p
  await loadData()
}

onMounted(loadData)

// ── Export ───────────────────────────────────────────────────────────────────
async function handleExport(format: 'pdf' | 'json') {
  if (exporting.value) return
  const to   = new Date()
  const from = new Date()
  if (period.value === 'WEEK') from.setDate(from.getDate() - 7)
  else from.setDate(from.getDate() - 30)
  exporting.value = format
  try {
    const fromStr = from.toISOString().split('T')[0]
    const toStr   = to.toISOString().split('T')[0]
    if (format === 'pdf') await reportsService.exportPdf(fromStr, toStr)
    else await reportsService.exportJson(fromStr, toStr)
  } finally {
    exporting.value = null
  }
}

// ── Chart.js data ─────────────────────────────────────────────────────────────
const lineChartData = computed(() => {
  if (!chartData.value) return { labels: [], datasets: [] }

  return {
    labels: chartData.value.labels,
    datasets: visibleDatasets.value.map(ds => {
      // Which indices are outside this unit's thresholds
      const outOfRange = new Set<number>()
      ds.data.forEach((val, i) => {
        if (val !== null && (val < ds.minThreshold || val > ds.maxThreshold))
          outOfRange.add(i)
      })

      return {
        label: ds.label,
        data: ds.data,
        borderColor: ds.color,
        backgroundColor: 'transparent',
        borderWidth: 2,
        // Segment turns red when either endpoint is out of range
        segment: {
          borderColor: (ctx: any) =>
            outOfRange.has(ctx.p0DataIndex) || outOfRange.has(ctx.p1DataIndex)
              ? '#EF4444'
              : ds.color,
        },
        pointRadius: ds.data.map((val, i) =>
          val === null ? 0 : outOfRange.has(i) ? 6 : 2
        ),
        pointBackgroundColor: ds.data.map((val, i) =>
          val === null ? 'transparent' : outOfRange.has(i) ? '#EF4444' : ds.color
        ),
        pointBorderColor: ds.data.map((val, i) =>
          val === null ? 'transparent' : outOfRange.has(i) ? '#fff' : ds.color
        ),
        pointBorderWidth: ds.data.map((_, i) => (outOfRange.has(i) ? 2 : 0)),
        tension: 0.3,
      }
    }),
  }
})

const lineChartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  interaction: {
    mode: 'index' as const,
    intersect: false,
  },
  plugins: {
    legend: { display: false },
    tooltip: {
      callbacks: {
        label: (ctx: any) => {
          const ds = visibleDatasets.value[ctx.datasetIndex]
          if (!ds || ctx.parsed.y == null) return undefined
          const val: number = ctx.parsed.y
          const hasThresholds = ds.minThreshold != null && ds.maxThreshold != null
          const isOut = hasThresholds && (val < ds.minThreshold || val > ds.maxThreshold)
          return ` ${ctx.dataset.label}: ${val}°C${isOut ? '  ⚠ avvik' : ''}`
        },
        afterLabel: (ctx: any) => {
          const ds = visibleDatasets.value[ctx.datasetIndex]
          if (!ds || ctx.parsed.y == null || ds.minThreshold == null || ds.maxThreshold == null) return undefined
          return `  Grense: ${ds.minThreshold}°C – ${ds.maxThreshold}°C`
        },
      },
    },
  },
  scales: {
    y: {
      grid: { color: 'rgba(0,0,0,0.05)' },
    },
    x: {
      grid: { display: false },
    },
  },
}))
</script>

<template>
  <!-- ── Page header ─────────────────────────────────────────────────────── -->
  <div class="flex items-center justify-between mb-4">
    <h1 class="page-title">Temperaturgrafer</h1>
    <div v-if="canExport" class="flex gap-2">
      <button class="btn btn-secondary btn-sm" :disabled="!!exporting" @click="handleExport('pdf')">
        {{ exporting === 'pdf' ? 'Laster…' : 'Eksporter PDF' }}
      </button>
      <button class="btn btn-secondary btn-sm" :disabled="!!exporting" @click="handleExport('json')">
        {{ exporting === 'json' ? 'Laster…' : 'Eksporter JSON' }}
      </button>
    </div>
  </div>

  <!-- ── Period + type filters ──────────────────────────────────────────── -->
  <div class="filter-row mb-4">
    <nav class="sub-nav">
      <button class="sub-nav-item" :class="{ active: period === 'WEEK' }"  @click="setPeriod('WEEK')">Siste 7 dager</button>
      <button class="sub-nav-item" :class="{ active: period === 'MONTH' }" @click="setPeriod('MONTH')">Siste 30 dager</button>
    </nav>

    <nav class="sub-nav" v-if="availableTypes.length > 1">
      <button class="sub-nav-item" :class="{ active: typeFilter === 'ALL' }" @click="setTypeFilter('ALL')">Alle</button>
      <button
        v-for="t in availableTypes"
        :key="t"
        class="sub-nav-item"
        :class="{ active: typeFilter === t }"
        @click="setTypeFilter(t)"
      >{{ unitTypeLabels[t] ?? t }}</button>
    </nav>
  </div>

  <!-- ── Chart card ─────────────────────────────────────────────────────── -->
  <div class="card mb-4">
    <div v-if="loading" class="chart-placeholder">
      <span class="text-muted text-sm">Laster data…</span>
    </div>

    <template v-else-if="chartData">
      <div class="chart-wrapper">
        <Line :data="lineChartData" :options="lineChartOptions" />
      </div>

      <!-- Unit toggle chips -->
      <div class="unit-chips">
        <button
          v-for="ds in typeFilteredDatasets"
          :key="ds.label"
          class="unit-chip"
          :class="{ 'unit-chip--hidden': hiddenUnits.has(ds.label) }"
          @click="toggleUnit(ds.label)"
        >
          <span class="unit-chip-swatch" :style="{ background: ds.color }" />
          {{ ds.label }}
        </button>
      </div>

      <!-- Deviation legend hint -->
      <p class="deviation-hint">
        <span class="deviation-hint-line" />
        Rød linje = temperatur utenfor godkjent grense. Klikk enhet for å vise/skjule.
      </p>
    </template>
  </div>

  <!-- ── Deviation log ──────────────────────────────────────────────────── -->
  <div v-if="chartData && chartData.alerts.length" class="card">
    <div class="card-header">
      <h2 class="section-title" style="margin: 0;">Avvikslogg</h2>
    </div>
    <div
      v-for="(alert, i) in chartData.alerts"
      :key="i"
      class="status-row"
      :class="{ 'is-alert': alert.status === 'OPEN' }"
    >
      <div class="flex items-center justify-between gap-3 flex-1 min-w-0">
        <div class="flex-1 min-w-0">
          <span class="font-semibold text-sm">{{ alert.unitName }}</span>
          <span class="text-muted text-xs" style="margin-left: 0.5rem;">
            {{ alert.value }}°C &mdash; {{ chartData.labels[alert.index] }}
          </span>
        </div>
        <span class="badge" :class="alert.status === 'OPEN' ? 'badge-danger' : 'badge-success'">
          {{ alert.status === 'OPEN' ? 'Åpen' : 'Løst' }}
        </span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  align-items: center;
}

.chart-placeholder {
  height: 340px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-wrapper {
  position: relative;
  height: 340px;
}

/* Unit toggle chips */
.unit-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 0.375rem;
  margin-top: 1rem;
}

.unit-chip {
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.25rem 0.625rem;
  font-size: 0.75rem;
  font-weight: 500;
  border: 1px solid var(--c-border);
  border-radius: var(--r-full);
  background: var(--c-surface);
  color: var(--c-text);
  cursor: pointer;
  transition: opacity 0.15s, background 0.15s;
  user-select: none;
}

.unit-chip:hover {
  background: var(--c-surface-2);
}

.unit-chip--hidden {
  opacity: 0.35;
}

.unit-chip-swatch {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 2px;
  flex-shrink: 0;
}

/* Hint line */
.deviation-hint {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  margin-top: 0.625rem;
  font-size: 0.72rem;
  color: var(--c-text-3);
}

.deviation-hint-line {
  display: inline-block;
  width: 1.5rem;
  height: 2px;
  background: #EF4444;
  border-radius: 1px;
  flex-shrink: 0;
}
</style>
