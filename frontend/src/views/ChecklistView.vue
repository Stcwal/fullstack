<template>
  <div class="checklist-view">
    <div class="mb-4">
      <h1 class="page-title">Sjekklister</h1>
    </div>

    <!-- Frequency filter -->
    <nav class="sub-nav mb-5">
      <button
        v-for="option in frequencyOptions"
        :key="option.value"
        class="sub-nav-item"
        :class="{ active: activeFrequency === option.value }"
        @click="setFrequency(option.value)"
      >
        {{ option.label }}
      </button>
    </nav>

    <div v-if="checklistsStore.loading" class="text-muted text-sm">Laster sjekklister…</div>

    <template v-else-if="checklistsStore.checklists.length">
      <div
        v-for="checklist in checklistsStore.checklists"
        :key="checklist.id"
        class="card mb-4"
      >
        <!-- Card header -->
        <div class="card-header">
          <div class="flex items-center gap-3 flex-1 min-w-0">
            <h3 class="checklist-title">{{ checklist.title }}</h3>
            <span
              class="mod-badge"
              :class="checklist.moduleType === 'IK_MAT' ? 'ik-mat' : 'ik-alkohol'"
            >
              {{ checklist.moduleType === 'IK_MAT' ? 'IK-Mat' : 'IK-Alkohol' }}
            </span>
          </div>
          <span class="completion-count text-sm text-muted">
            {{ completedCount(checklist) }}&nbsp;/&nbsp;{{ checklist.items.length }}
          </span>
        </div>

        <!-- Progress bar -->
        <div class="progress-bar mb-3">
          <div
            class="progress-fill"
            :class="progressClass(checklist)"
            :style="{ width: progressPercent(checklist) + '%' }"
          />
        </div>

        <!-- Items -->
        <ul class="checklist-items">
          <li
            v-for="item in checklist.items"
            :key="item.id"
            class="checklist-item"
            :class="{ 'is-done': item.completed }"
          >
            <label class="checklist-label">
              <input
                type="checkbox"
                class="checklist-checkbox"
                :checked="item.completed"
                @change="checklistsStore.toggleItem(checklist.id, item.id)"
              />
              <span class="checklist-text">{{ item.text }}</span>
            </label>
            <span v-if="item.completed && item.completedBy" class="text-xs text-muted completed-by">
              {{ item.completedBy }}<template v-if="item.completedAt">&nbsp;·&nbsp;{{ formatDateTime(item.completedAt) }}</template>
            </span>
          </li>
        </ul>

        <!-- Fully completed footer -->
        <template v-if="isFullyCompleted(checklist)">
          <div class="divider" />
          <p class="text-sm text-muted completed-footer">
            Fullført av:
            <strong>{{ checklist.completedBy ?? '—' }}</strong>
            <template v-if="checklist.completedAt">
              &nbsp;—&nbsp;{{ formatDateTime(checklist.completedAt) }}
            </template>
          </p>
        </template>
      </div>
    </template>

    <!-- Empty state -->
    <div v-else class="empty-state">
      <p class="text-muted">Ingen sjekklister for denne perioden.</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useChecklistsStore } from '@/stores/checklists'
import type { ChecklistFrequency, Checklist } from '@/types'

// ─── Store ─────────────────────────────────────────────────────────────────
const checklistsStore = useChecklistsStore()

// ─── Frequency filter ──────────────────────────────────────────────────────
const frequencyOptions: { label: string; value: ChecklistFrequency }[] = [
  { label: 'Daglig',    value: 'DAILY'   },
  { label: 'Ukentlig', value: 'WEEKLY'  },
  { label: 'Månedlig', value: 'MONTHLY' },
]

const activeFrequency = ref<ChecklistFrequency>('DAILY')

function setFrequency(freq: ChecklistFrequency) {
  activeFrequency.value = freq
  checklistsStore.fetchAll(freq)
}

// ─── Progress helpers ──────────────────────────────────────────────────────
function completedCount(checklist: Checklist): number {
  return checklist.items.filter(i => i.completed).length
}

function progressPercent(checklist: Checklist): number {
  if (!checklist.items.length) return 0
  return Math.round((completedCount(checklist) / checklist.items.length) * 100)
}

function progressClass(checklist: Checklist): string {
  const pct = progressPercent(checklist)
  if (pct === 100) return 'full'
  if (pct === 0)   return 'empty'
  return 'partial'
}

function isFullyCompleted(checklist: Checklist): boolean {
  return checklist.items.length > 0 && completedCount(checklist) === checklist.items.length
}

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
onMounted(() => {
  checklistsStore.fetchAll(activeFrequency.value)
})
</script>

<style scoped>
.checklist-view {
  max-width: 720px;
}

.checklist-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--c-text);
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.completion-count {
  white-space: nowrap;
  flex-shrink: 0;
}

/* Progress bar sits just below the card header, full-width inside card padding */
.progress-bar {
  height: 6px;
  background: var(--c-surface-3);
  border-radius: var(--r-full);
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: var(--r-full);
  transition: width 0.3s ease;
}

.progress-fill.full    { background: var(--c-success); }
.progress-fill.partial { background: var(--c-warning); }
.progress-fill.empty   { background: var(--c-surface-3); width: 0 !important; }

/* Checklist items */
.checklist-items {
  list-style: none;
  margin: 0;
  padding: 0;
}

.checklist-item {
  display: flex;
  flex-direction: column;
  gap: 0.125rem;
  padding: 0.6rem 0;
  border-bottom: 1px solid var(--c-border);
  transition: opacity 0.2s;
}

.checklist-item:last-child {
  border-bottom: none;
}

.checklist-item.is-done .checklist-text {
  text-decoration: line-through;
  color: var(--c-text-3);
}

.checklist-label {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  cursor: pointer;
}

.checklist-checkbox {
  width: 1.1rem;
  height: 1.1rem;
  flex-shrink: 0;
  accent-color: var(--c-primary);
  cursor: pointer;
}

.checklist-text {
  font-size: 0.95rem;
  color: var(--c-text);
  line-height: 1.4;
}

.completed-by {
  padding-left: 1.85rem;
}

.completed-footer {
  margin: 0;
  padding-top: 0.75rem;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 3rem 1rem;
  background: var(--c-surface);
  border-radius: var(--r);
  border: 1px dashed var(--c-border);
}
</style>
