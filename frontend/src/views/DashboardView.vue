<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useDashboardStore } from '@/stores/dashboard'
import { useAlkoholStore } from '@/stores/alkohol'
import AppAlert from '@/components/AppAlert.vue'
import type { DashboardTask } from '@/types'

const dashboardStore = useDashboardStore()
const alkoholStore = useAlkoholStore()

onMounted(() => {
  dashboardStore.fetchDashboard()
  alkoholStore.fetchStats()
})

// ---- Date formatting ----
const norwegianDays = ['Søndag', 'Mandag', 'Tirsdag', 'Onsdag', 'Torsdag', 'Fredag', 'Lørdag']
const norwegianMonths = [
  'januar', 'februar', 'mars', 'april', 'mai', 'juni',
  'juli', 'august', 'september', 'oktober', 'november', 'desember'
]

const formattedDate = computed(() => {
  const now = new Date()
  const day = norwegianDays[now.getDay()]
  const date = now.getDate()
  const month = norwegianMonths[now.getMonth()]
  const year = now.getFullYear()
  return `${day} ${date}. ${month} ${year}`
})

// ---- Metric helpers ----
const stats = computed(() => dashboardStore.stats)

const taskCountClass = computed(() => {
  const s = stats.value
  if (!s) return ''
  if (s.tasksCompleted === s.tasksTotal) return 'text-success'
  return 'text-warning'
})

const tempAlertClass = computed(() => {
  const s = stats.value
  if (!s) return ''
  return s.tempAlerts > 0 ? 'text-danger' : 'text-success'
})

const deviationClass = computed(() => {
  const s = stats.value
  if (!s) return ''
  return s.openDeviations > 0 ? 'text-warning' : 'text-success'
})

// ---- Task helpers ----
function isAlertTask(task: DashboardTask): boolean {
  return task.status === 'PENDING' && task.name.toLowerCase().includes('alderskontroll')
}

function taskIndicatorStyle(task: DashboardTask): Record<string, string> {
  if (task.status === 'COMPLETED') {
    return {
      background: 'var(--c-success-bg)',
      border: '1.5px solid var(--c-success-border)',
    }
  }
  if (task.status === 'PENDING') {
    return {
      background: 'transparent',
      border: '1.5px solid var(--c-border-2)',
    }
  }
  return {
    background: 'transparent',
    border: '1.5px solid var(--c-border)',
  }
}

function formatCompletedAt(iso?: string): string {
  if (!iso) return ''
  try {
    const d = new Date(iso)
    const hh = String(d.getHours()).padStart(2, '0')
    const mm = String(d.getMinutes()).padStart(2, '0')
    return `${hh}:${mm}`
  } catch {
    return iso
  }
}
</script>

<template>
  <div class="dashboard-page">
    <!-- ---- Page header ---- -->
    <div class="page-header mb-4">
      <div class="flex items-center gap-3 flex-1">
        <div>
          <h1 class="page-title">Oversikt</h1>
          <p class="page-subtitle">{{ formattedDate }}</p>
        </div>
      </div>
      <div class="flex items-center gap-2">
        <span class="mod-badge ik-mat">IK-Mat</span>
        <span class="mod-badge ik-alkohol">IK-Alkohol</span>
      </div>
    </div>

    <!-- ---- Loading skeleton ---- -->
    <template v-if="dashboardStore.loading">
      <div class="metric-grid mb-4">
        <div v-for="n in 4" :key="n" class="metric-card skeleton-card">
          <div class="skeleton skeleton-label"></div>
          <div class="skeleton skeleton-value"></div>
        </div>
      </div>

      <div class="card mb-4">
        <div class="card-header">
          <div class="skeleton skeleton-heading"></div>
        </div>
        <div class="skeleton-rows">
          <div v-for="n in 4" :key="n" class="skeleton-row-item">
            <div class="skeleton skeleton-row-left"></div>
            <div class="skeleton skeleton-row-right"></div>
          </div>
        </div>
      </div>
    </template>

    <!-- ---- Loaded content ---- -->
    <template v-else>
      <!-- Metric cards -->
      <div class="metric-grid mb-4">
        <!-- Oppgaver i dag -->
        <div class="metric-card">
          <div class="metric-label">Oppgaver i dag</div>
          <div class="metric-value" :class="taskCountClass">
            <template v-if="stats">
              {{ stats.tasksCompleted }} / {{ stats.tasksTotal }}
            </template>
            <template v-else>—</template>
          </div>
          <div v-if="stats" class="metric-sub text-sm text-muted mt-1">
            <template v-if="stats.tasksCompleted === stats.tasksTotal && stats.tasksTotal > 0">
              Alle fullført
            </template>
            <template v-else-if="stats.tasksTotal > 0">
              {{ stats.tasksTotal - stats.tasksCompleted }} gjenstår
            </template>
            <template v-else>
              Ingen oppgaver
            </template>
          </div>
        </div>

        <!-- Temp-varsler -->
        <div class="metric-card">
          <div class="metric-label">Temp-varsler</div>
          <div class="metric-value" :class="tempAlertClass">
            <template v-if="stats !== null">{{ stats.tempAlerts }}</template>
            <template v-else>—</template>
          </div>
          <div v-if="stats !== null" class="metric-sub text-sm mt-1" :class="stats.tempAlerts > 0 ? 'text-danger' : 'text-muted'">
            {{ stats.tempAlerts > 0 ? 'Krever oppfølging' : 'Ingen avvik' }}
          </div>
        </div>

        <!-- Åpne avvik -->
        <div class="metric-card">
          <div class="metric-label">Åpne avvik</div>
          <div class="metric-value" :class="deviationClass">
            <template v-if="stats !== null">{{ stats.openDeviations }}</template>
            <template v-else>—</template>
          </div>
          <div v-if="stats !== null" class="metric-sub text-sm mt-1" :class="stats.openDeviations > 0 ? 'text-warning' : 'text-muted'">
            {{ stats.openDeviations > 0 ? 'Under behandling' : 'Alt OK' }}
          </div>
        </div>

        <!-- Samsvar -->
        <div class="metric-card">
          <div class="metric-label">Samsvar</div>
          <div class="metric-value text-success">
            <template v-if="stats !== null">{{ stats.compliancePercent }}%</template>
            <template v-else>—</template>
          </div>
          <div v-if="stats !== null" class="metric-sub mt-1">
            <div class="progress-bar">
              <div
                class="progress-fill"
                :class="stats.compliancePercent >= 90 ? 'full' : stats.compliancePercent >= 60 ? 'partial' : 'empty'"
                :style="{ width: stats.compliancePercent + '%' }"
              ></div>
            </div>
          </div>
        </div>
      </div>

      <!-- Dagens oppgaver -->
      <div class="card mb-4">
        <div class="card-header">
          <h2 class="section-title">Dagens oppgaver</h2>
          <span v-if="stats" class="badge" :class="stats.tasksCompleted === stats.tasksTotal && stats.tasksTotal > 0 ? 'badge-success' : 'badge-neutral'">
            {{ stats.tasksCompleted }}/{{ stats.tasksTotal }}
          </span>
        </div>

        <div v-if="dashboardStore.tasks.length === 0" class="empty-state">
          <p class="text-muted text-sm">Ingen oppgaver registrert for i dag.</p>
        </div>

        <div v-else class="task-list">
          <div
            v-for="task in dashboardStore.tasks"
            :key="task.id"
            class="status-row"
            :class="{ 'is-alert': isAlertTask(task) }"
          >
            <!-- Left: indicator + name -->
            <div class="flex items-center gap-3 flex-1">
              <div class="task-indicator" :style="taskIndicatorStyle(task)">
                <span v-if="task.status === 'COMPLETED'" class="task-check" aria-hidden="true">✓</span>
              </div>
              <span class="task-name" :class="task.status === 'COMPLETED' ? 'text-muted' : ''">
                {{ task.name }}
              </span>
            </div>

            <!-- Right: status -->
            <div class="task-status flex items-center gap-2">
              <template v-if="task.status === 'COMPLETED'">
                <span class="badge badge-success">Fullført</span>
                <span v-if="task.completedBy || task.completedAt" class="text-sm text-muted task-meta">
                  <template v-if="task.completedBy">{{ task.completedBy }}</template>
                  <template v-if="task.completedBy && task.completedAt"> · </template>
                  <template v-if="task.completedAt">{{ formatCompletedAt(task.completedAt) }}</template>
                </span>
              </template>

              <template v-else-if="task.status === 'PENDING'">
                <span class="badge badge-warning">Venter</span>
              </template>

              <template v-else>
                <span class="text-sm text-muted">Ikke startet</span>
              </template>
            </div>
          </div>
        </div>
      </div>

      <!-- Varsler -->
      <div v-if="dashboardStore.alerts.length > 0" class="card mb-4">
        <div class="card-header">
          <h2 class="section-title">Varsler</h2>
          <span class="badge badge-danger">{{ dashboardStore.alerts.length }}</span>
        </div>
        <div class="alerts-list">
          <AppAlert
            v-for="alert in dashboardStore.alerts"
            :key="alert.id"
            :type="alert.type"
            :message="alert.message"
          />
        </div>
      </div>

      <!-- Aktive moduler -->
      <div class="card">
        <div class="card-header">
          <h2 class="section-title">Moduler aktive</h2>
        </div>
        <div class="flex gap-3 modules-body">
          <div class="module-pill">
            <span class="mod-badge ik-mat">IK-Mat</span>
            <span class="text-sm text-muted">Næringsmiddeltilsyn</span>
          </div>
          <div class="module-pill">
            <span class="mod-badge ik-alkohol">IK-Alkohol</span>
            <span class="text-sm text-muted">Skjenkekontroll</span>
          </div>
        </div>
      </div>

      <!-- IK-Alkohol summary — shown when module is active -->
      <section
        v-if="alkoholStore.stats"
        class="alkohol-summary module-ik-alkohol"
        aria-label="IK-Alkohol oversikt"
      >
        <div class="alkohol-summary__header">
          <span class="alkohol-summary__dot" aria-hidden="true"></span>
          <h2 class="alkohol-summary__title">IK-Alkohol</h2>
          <router-link :to="{ name: 'alkohol-alderskontroll' }" class="alkohol-summary__link">
            Se alle →
          </router-link>
        </div>
        <div class="alkohol-summary__stats">
          <div class="alkohol-stat">
            <span class="alkohol-stat__value">{{ alkoholStore.stats.ageChecksToday }}</span>
            <span class="alkohol-stat__label">ID-kontroller i dag</span>
          </div>
          <div class="alkohol-stat">
            <span class="alkohol-stat__value">{{ alkoholStore.stats.incidentsThisWeek }}</span>
            <span class="alkohol-stat__label">Hendelser denne uken</span>
          </div>
          <div class="alkohol-stat">
            <span class="alkohol-stat__value">{{ alkoholStore.stats.checklistCompletionPct }}%</span>
            <span class="alkohol-stat__label">Sjekkliste fullført</span>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<style scoped>
.dashboard-page {
  padding: 24px;
  max-width: 960px;
  margin: 0 auto;
}

/* ---- Page header ---- */
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

/* ---- Metric sub-line ---- */
.metric-sub {
  font-size: 0.75rem;
}

/* ---- Progress bar override for inline use ---- */
.progress-bar {
  height: 5px;
  background: var(--c-border);
  border-radius: var(--r-full);
  overflow: hidden;
  margin-top: 4px;
}

.progress-fill {
  height: 100%;
  border-radius: var(--r-full);
  transition: width 0.4s ease;
}

.progress-fill.full    { background: var(--c-success); }
.progress-fill.partial { background: var(--c-warning); }
.progress-fill.empty   { background: var(--c-danger); }

/* ---- Task list ---- */
.task-list {
  display: flex;
  flex-direction: column;
}

.task-indicator {
  width: 16px;
  height: 16px;
  border-radius: 3px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s ease, border-color 0.15s ease;
}

.task-check {
  font-size: 10px;
  line-height: 1;
  color: var(--c-success-text);
  font-weight: 700;
}

.task-name {
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--c-text);
  transition: color 0.15s ease;
}

.task-name.text-muted {
  text-decoration: line-through;
  text-decoration-color: var(--c-border-2);
}

.task-status {
  flex-shrink: 0;
}

.task-meta {
  white-space: nowrap;
  font-size: 0.75rem;
}

/* ---- Alerts list ---- */
.alerts-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 0 16px 16px;
}

/* ---- Empty state ---- */
.empty-state {
  padding: 24px 16px;
  text-align: center;
}

/* ---- Modules ---- */
.modules-body {
  padding: 12px 16px 16px;
  flex-wrap: wrap;
}

.module-pill {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: var(--c-surface-2);
  border: 1px solid var(--c-border);
  border-radius: var(--r);
}

/* ---- Skeleton loading ---- */
.skeleton {
  background: linear-gradient(90deg, var(--c-border) 25%, var(--c-surface-3) 50%, var(--c-border) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
  border-radius: var(--r-sm);
}

@keyframes shimmer {
  0%   { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.skeleton-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 20px;
}

.skeleton-label {
  height: 12px;
  width: 70%;
}

.skeleton-value {
  height: 28px;
  width: 50%;
}

.skeleton-heading {
  height: 16px;
  width: 160px;
}

.skeleton-rows {
  padding: 8px 16px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.skeleton-row-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.skeleton-row-left {
  height: 14px;
  width: 55%;
}

.skeleton-row-right {
  height: 14px;
  width: 20%;
}

/* ---- IK-Alkohol summary ---- */
.alkohol-summary {
  background: hsl(var(--ik-alkohol-hue, 38), 100%, 97%);
  border: 1.5px solid hsl(var(--ik-alkohol-hue, 38), 90%, 75%);
  border-radius: 12px;
  padding: 16px;
  margin-top: 16px;
}
.alkohol-summary__header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}
.alkohol-summary__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: hsl(var(--ik-alkohol-hue, 38), 85%, 45%);
  flex-shrink: 0;
}
.alkohol-summary__title {
  font-size: 14px;
  font-weight: 700;
  color: hsl(var(--ik-alkohol-hue, 38), 80%, 20%);
  flex: 1;
}
.alkohol-summary__link {
  font-size: 12px;
  font-weight: 600;
  color: hsl(var(--ik-alkohol-hue, 38), 80%, 35%);
  text-decoration: none;
}
.alkohol-summary__link:hover { text-decoration: underline; }
.alkohol-summary__stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}
.alkohol-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}
.alkohol-stat__value {
  font-size: 24px;
  font-weight: 800;
  color: hsl(var(--ik-alkohol-hue, 38), 80%, 25%);
  line-height: 1;
}
.alkohol-stat__label {
  font-size: 10px;
  color: hsl(var(--ik-alkohol-hue, 38), 50%, 45%);
  text-align: center;
}

/* ---- Responsive ---- */
@media (max-width: 600px) {
  .dashboard-page {
    padding: 16px;
  }

  .task-meta {
    display: none;
  }

  .module-pill {
    flex: 1;
    min-width: 140px;
  }
}
</style>
