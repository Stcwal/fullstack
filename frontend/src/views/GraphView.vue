<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Line } from 'vue-chartjs'
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

// ── State ────────────────────────────────────────────────────────────────────
const period = ref<ChartPeriod>('WEEK')
const chartData = ref<ServiceChartData | null>(null)
const loading = ref(false)
const exporting = ref<'pdf' | 'json' | null>(null)

// ── Data fetching ─────────────────────────────────────────────────────────────
async function loadData() {
  loading.value = true
  try {
    chartData.value = await reportsService.getChartData(period.value)
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
  const to = new Date()
  const from = new Date()
  if (period.value === 'WEEK') from.setDate(from.getDate() - 7)
  else from.setDate(from.getDate() - 30)
  const fromStr = from.toISOString().split('T')[0]
  const toStr = to.toISOString().split('T')[0]
  exporting.value = format
  try {
    if (format === 'pdf') await reportsService.exportPdf(fromStr, toStr)
    else await reportsService.exportJson(fromStr, toStr)
  } finally {
    exporting.value = null
  }
}

// ── Chart.js data & options ──────────────────────────────────────────────────
const lineChartData = computed(() => {
  if (!chartData.value) return { labels: [], datasets: [] }

  return {
    labels: chartData.value.labels,
    datasets: chartData.value.datasets.map(ds => ({
      label: ds.label,
      data: ds.data,
      borderColor: ds.color,
      backgroundColor: 'transparent',
      borderWidth: 2,
      pointRadius: chartData.value!.labels.map((_, i) => {
        // Use a larger red point for alert positions in any dataset
        const isAlert = chartData.value!.alerts.some(a => a.index === i)
        return isAlert ? 6 : 3
      }),
      pointBackgroundColor: chartData.value!.labels.map((_, i) => {
        const alert = chartData.value!.alerts.find(a => a.index === i && a.status === 'OPEN')
        return alert ? '#EF4444' : ds.color
      }),
      pointBorderColor: chartData.value!.labels.map((_, i) => {
        const alert = chartData.value!.alerts.find(a => a.index === i && a.status === 'OPEN')
        return alert ? '#EF4444' : ds.color
      }),
      tension: 0.3,
    })),
  }
})

const lineChartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: false, // We render a custom legend below
    },
    tooltip: {
      callbacks: {
        label: (ctx: any) => `${ctx.dataset.label}: ${ctx.parsed.y}°C`,
      },
    },
  },
  scales: {
    y: {
      grid: {
        color: 'rgba(0,0,0,0.05)',
      },
    },
    x: {
      grid: {
        display: false,
      },
    },
  },
}))
</script>

<template>
  <!-- ── Page header ─────────────────────────────────────────────────────── -->
  <div class="flex items-center justify-between mb-4">
    <h1 class="page-title">Temperaturgrafer</h1>
    <div class="flex gap-2">
      <button
        class="btn btn-secondary btn-sm"
        :disabled="!!exporting"
        @click="handleExport('pdf')"
      >
        {{ exporting === 'pdf' ? 'Laster…' : 'Eksporter PDF' }}
      </button>
      <button
        class="btn btn-secondary btn-sm"
        :disabled="!!exporting"
        @click="handleExport('json')"
      >
        {{ exporting === 'json' ? 'Laster…' : 'Eksporter JSON' }}
      </button>
    </div>
  </div>

  <!-- ── Period selector ────────────────────────────────────────────────── -->
  <nav class="sub-nav mb-4">
    <button
      class="sub-nav-item"
      :class="{ active: period === 'WEEK' }"
      @click="setPeriod('WEEK')"
    >
      Siste 7 dager
    </button>
    <button
      class="sub-nav-item"
      :class="{ active: period === 'MONTH' }"
      @click="setPeriod('MONTH')"
    >
      Siste 30 dager
    </button>
  </nav>

  <!-- ── Chart card ─────────────────────────────────────────────────────── -->
  <div class="card mb-4">
    <!-- Loading state -->
    <div v-if="loading" style="height: 280px; display: flex; align-items: center; justify-content: center;">
      <span class="text-muted text-sm">Laster data…</span>
    </div>

    <template v-else-if="chartData">
      <!-- Chart -->
      <div style="position: relative; height: 280px;">
        <Line :data="lineChartData" :options="lineChartOptions" />
      </div>

      <!-- Custom legend -->
      <div class="flex items-center gap-4 mb-1" style="flex-wrap: wrap; margin-top: 1rem;">
        <div
          v-for="ds in chartData.datasets"
          :key="ds.label"
          class="flex items-center gap-2"
        >
          <span
            style="display: inline-block; width: 12px; height: 12px; border-radius: 2px; flex-shrink: 0;"
            :style="{ backgroundColor: ds.color }"
          />
          <span class="text-sm">{{ ds.label }}</span>
        </div>
        <!-- Alert legend indicator -->
        <div class="flex items-center gap-2">
          <span
            style="display: inline-block; width: 12px; height: 12px; border-radius: 50%; background: #EF4444; flex-shrink: 0;"
          />
          <span class="text-sm text-muted">Avvik (åpent)</span>
        </div>
      </div>
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
            {{ alert.value }}°C
            &mdash;
            {{ chartData.labels[alert.index] }}
          </span>
        </div>
        <span
          class="badge"
          :class="alert.status === 'OPEN' ? 'badge-danger' : 'badge-success'"
        >
          {{ alert.status === 'OPEN' ? 'Åpen' : 'Løst' }}
        </span>
      </div>
    </div>
  </div>
</template>
