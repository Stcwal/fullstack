<template>
  <div class="checklist-view" :class="{ 'checklist-view--tablet': isTablet }">
    <div class="mb-4">
      <h1 class="page-title">Sjekklister</h1>
    </div>

    <!-- Frequency filter -->
    <nav class="sub-nav mb-3">
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

    <!-- Module filter -->
    <nav class="sub-nav mb-5" aria-label="Velg modul">
      <button
        class="sub-nav-item"
        :class="{ active: activeModule === 'IK_MAT' }"
        @click="activeModule = 'IK_MAT'"
      >IK-Mat</button>
      <button
        class="sub-nav-item"
        :class="{ active: activeModule === 'IK_ALKOHOL' }"
        @click="activeModule = 'IK_ALKOHOL'"
      >IK-Alkohol</button>
    </nav>

    <div v-if="checklistsStore.loading" class="text-muted text-sm">Laster sjekklister…</div>

    <template v-else-if="filteredChecklists.length">
      <div
        v-for="checklist in filteredChecklists"
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
            <label class="checklist-row">
              <div class="checklist-body">
                <span class="checklist-text">{{ item.text }}</span>
                <span class="text-xs text-muted completed-by" :class="{ 'completed-by--hidden': !(item.completed && item.completedBy) }">
                  <template v-if="item.completed && item.completedBy">{{ item.completedBy }}<template v-if="item.completedAt">&nbsp;·&nbsp;{{ formatDateTime(item.completedAt) }}</template></template>
                  <template v-else>&nbsp;</template>
                </span>
              </div>
              <input
                type="checkbox"
                class="checklist-checkbox-input"
                :checked="item.completed"
                @change="checklistsStore.toggleItem(checklist.id, item.id, shiftStore.activeWorkerId ?? undefined)"
              />
              <span class="checklist-check-box" aria-hidden="true" />
            </label>
          </li>
        </ul>

        <!-- Fully completed footer -->
        <template v-if="isFullyCompleted(checklist)">
          <div class="divider" />
          <p class="text-sm text-muted completed-footer">
            <template v-if="resolvedCompletedBy(checklist)">
              Fullført av: <strong>{{ resolvedCompletedBy(checklist) }}</strong>
            </template>
            <template v-else>Fullført</template>
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
import { ref, computed, onMounted } from 'vue'
import { useChecklistsStore } from '@/stores/checklists'
import { useLayoutStore } from '@/stores/layout'
import { useShiftStore } from '@/stores/shift'
import type { ChecklistFrequency, Checklist, ModuleType } from '@/types'

// ─── Store ─────────────────────────────────────────────────────────────────
const checklistsStore = useChecklistsStore()
const shiftStore = useShiftStore()
const isTablet = computed(() => useLayoutStore().isTabletMode)

// ─── Frequency filter ──────────────────────────────────────────────────────
const frequencyOptions: { label: string; value: ChecklistFrequency }[] = [
  { label: 'Daglig',    value: 'DAILY'   },
  { label: 'Ukentlig', value: 'WEEKLY'  },
  { label: 'Månedlig', value: 'MONTHLY' },
]

const activeFrequency = ref<ChecklistFrequency>('DAILY')
const activeModule = ref<ModuleType>('IK_MAT')

const filteredChecklists = computed(() =>
  checklistsStore.checklists.filter(c => c.moduleType === activeModule.value)
)

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

function resolvedCompletedBy(checklist: Checklist): string {
  if (checklist.completedBy) return checklist.completedBy
  const last = [...checklist.items].reverse().find(i => i.completed && i.completedBy)
  return last?.completedBy ?? ''
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

.checklist-view--tablet {
  max-width: none;
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
  flex-direction: row;
  align-items: stretch;
  padding: 0;
  border-bottom: 1px solid var(--c-border);
  transition: opacity 0.2s;
}

.checklist-item:last-child {
  border-bottom: none;
}

.checklist-item.is-done .checklist-text {
  color: var(--c-text-3);
}

/* Left body: text + attribution stacked */
.checklist-body {
  flex: 1;
  min-width: 0;
  padding: 0.6rem 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 0.2rem;
}

/* Full-row label — entire line is clickable */
.checklist-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  width: 100%;
  cursor: pointer;
}

/* Visually hidden real checkbox */
.checklist-checkbox-input {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
  pointer-events: none;
}

/* Visual square block */
.checklist-check-box {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 2.5rem;
  height: 2.5rem;
  background: var(--c-surface, #fff);
  border: 1.5px solid var(--c-border-2, #94a3b8);
  border-radius: var(--r-sm);
  position: relative;
  transition: background 0.15s, border-color 0.15s;
}

.checklist-row:hover .checklist-check-box {
  border-color: var(--c-primary);
}

.checklist-checkbox-input:focus-visible + .checklist-check-box {
  outline: 2px solid var(--c-primary);
  outline-offset: -2px;
}

/* Checked: green background */
.checklist-checkbox-input:checked + .checklist-check-box {
  background: var(--c-primary);
  border-color: var(--c-primary);
}

/* Checkmark symbol — sized relative to the box */
.checklist-checkbox-input:checked + .checklist-check-box::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 45%;
  height: 25%;
  border-left: 2px solid #fff;
  border-bottom: 2px solid #fff;
  transform: translate(-50%, -62%) rotate(-45deg);
}

.checklist-text {
  font-size: 0.95rem;
  color: var(--c-text);
  line-height: 1.4;
}

.completed-by {
  text-decoration: none;
}

.completed-by--hidden {
  visibility: hidden;
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
