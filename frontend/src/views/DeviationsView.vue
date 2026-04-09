<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useDeviationsStore } from '@/stores/deviations'
import { useAuthStore } from '@/stores/auth'
import { useShiftStore } from '@/stores/shift'
import { deviationsService } from '@/services/deviations.service'
import AppModal from '@/components/AppModal.vue'
import type { DeviationComment, NewDeviation } from '@/types'

const deviationsStore = useDeviationsStore()
const authStore = useAuthStore()
const shiftStore = useShiftStore()

// ── Permissions ───────────────────────────────────────────────────────────────
const canManage = computed(() => {
  const r = authStore.user?.role
  return r === 'ADMIN' || r === 'MANAGER' || r === 'SUPERVISOR'
})

// ── Filters ───────────────────────────────────────────────────────────────────
type StatusFilter   = 'ALL' | 'OPEN' | 'IN_PROGRESS' | 'RESOLVED'
type ModuleFilter   = 'ALL' | 'IK_MAT' | 'IK_ALKOHOL'
type SeverityFilter = 'ALL' | 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW'

const filterStatus   = ref<StatusFilter>('ALL')
const filterModule   = ref<ModuleFilter>('ALL')
const filterSeverity = ref<SeverityFilter>('ALL')

const filteredDeviations = computed(() =>
  deviationsStore.deviations.filter(d => {
    if (filterStatus.value   !== 'ALL' && d.status     !== filterStatus.value)   return false
    if (filterModule.value   !== 'ALL' && d.moduleType !== filterModule.value)   return false
    if (filterSeverity.value !== 'ALL' && d.severity   !== filterSeverity.value) return false
    return true
  })
)

const hasActiveFilter = computed(
  () => filterStatus.value !== 'ALL' || filterModule.value !== 'ALL' || filterSeverity.value !== 'ALL'
)

function clearFilters() {
  filterStatus.value   = 'ALL'
  filterModule.value   = 'ALL'
  filterSeverity.value = 'ALL'
}

// ── Expand / comments ─────────────────────────────────────────────────────────
const expanded = ref<Record<number, boolean>>({})
const commentsMap = ref<Record<number, DeviationComment[]>>({})
const commentsLoading = ref<Record<number, boolean>>({})

async function toggleExpand(id: number) {
  const isOpening = !expanded.value[id]
  expanded.value = { ...expanded.value, [id]: isOpening }
  if (isOpening) {
    // Initialise draft so v-model works on first open
    if (commentDraft.value[id] === undefined) {
      commentDraft.value = { ...commentDraft.value, [id]: '' }
    }
    if (commentsMap.value[id] === undefined) {
      await loadComments(id)
    }
  }
}

async function loadComments(id: number) {
  commentsLoading.value = { ...commentsLoading.value, [id]: true }
  try {
    const dev = await deviationsService.getById(id)
    commentsMap.value = { ...commentsMap.value, [id]: dev.comments ?? [] }
  } catch {
    commentsMap.value = { ...commentsMap.value, [id]: [] }
  } finally {
    const next = { ...commentsLoading.value }
    delete next[id]
    commentsLoading.value = next
  }
}

// ── Status change ─────────────────────────────────────────────────────────────
const statusSaving = ref<Record<number, boolean>>({})

async function markInProgress(id: number) {
  statusSaving.value = { ...statusSaving.value, [id]: true }
  try {
    await deviationsStore.updateStatus(id, 'IN_PROGRESS')
  } finally {
    const next = { ...statusSaving.value }; delete next[id]
    statusSaving.value = next
  }
}

// ── Resolve ───────────────────────────────────────────────────────────────────
const resolvingId = ref<number | null>(null)
const resolutionText = ref('')
const resolveSaving = ref(false)

function startResolve(id: number) {
  resolvingId.value = id
  resolutionText.value = ''
}

function cancelResolve() {
  resolvingId.value = null
  resolutionText.value = ''
}

async function confirmResolve(id: number) {
  if (!resolutionText.value.trim()) return
  resolveSaving.value = true
  try {
    await deviationsStore.resolve(id, resolutionText.value.trim())
    resolvingId.value = null
    resolutionText.value = ''
  } finally {
    resolveSaving.value = false
  }
}

// ── Comments ──────────────────────────────────────────────────────────────────
const commentDraft = ref<Record<number, string>>({})
const commentSaving = ref<Record<number, boolean>>({})

async function submitComment(id: number) {
  const text = (commentDraft.value[id] ?? '').trim()
  if (!text) return
  commentSaving.value = { ...commentSaving.value, [id]: true }
  try {
    const comment = await deviationsStore.addComment(id, text)
    commentsMap.value = {
      ...commentsMap.value,
      [id]: [...(commentsMap.value[id] ?? []), comment],
    }
    commentDraft.value = { ...commentDraft.value, [id]: '' }
  } finally {
    const next = { ...commentSaving.value }; delete next[id]
    commentSaving.value = next
  }
}

// ── New deviation modal ───────────────────────────────────────────────────────
const showModal = ref(false)
const form = ref<NewDeviation>({
  title: '',
  description: '',
  severity: 'MEDIUM',
  moduleType: 'IK_MAT',
})

function openModal() {
  form.value = { title: '', description: '', severity: 'MEDIUM', moduleType: 'IK_MAT' }
  showModal.value = true
}

async function submitDeviation() {
  if (!form.value.title.trim() || !form.value.description.trim()) return
  await deviationsStore.report({
    ...form.value,
    performedByUserId: shiftStore.activeWorkerId ?? undefined,
  })
  showModal.value = false
}

// ── Helpers ───────────────────────────────────────────────────────────────────
function fmtDate(iso: string) {
  const d = new Date(iso)
  const today = new Date()
  const yesterday = new Date(); yesterday.setDate(today.getDate() - 1)
  const t = `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
  if (d.toDateString() === today.toDateString()) return `I dag ${t}`
  if (d.toDateString() === yesterday.toDateString()) return `I går ${t}`
  return `${d.getDate()}. ${['jan','feb','mar','apr','mai','jun','jul','aug','sep','okt','nov','des'][d.getMonth()]} ${t}`
}

function statusLabel(s: string) {
  return { OPEN: 'Åpen', IN_PROGRESS: 'Under arbeid', RESOLVED: 'Løst' }[s] ?? s
}
function statusBadge(s: string) {
  return { OPEN: 'badge-danger', IN_PROGRESS: 'badge-warning', RESOLVED: 'badge-success' }[s] ?? 'badge-neutral'
}
function severityLabel(s: string) {
  return { CRITICAL: 'Kritisk', HIGH: 'Høy', MEDIUM: 'Medium', LOW: 'Lav' }[s] ?? s
}
function severityBadge(s: string) {
  return { CRITICAL: 'badge-danger', HIGH: 'badge-warning', MEDIUM: 'badge-info', LOW: 'badge-neutral' }[s] ?? 'badge-neutral'
}

onMounted(() => deviationsStore.fetchAll())
</script>

<template>
  <!-- ── Page header ──────────────────────────────────────────────────────── -->
  <div class="flex items-center justify-between mb-4">
    <div>
      <h1 class="page-title">Avvik</h1>
      <p class="page-subtitle">Rapporter og følg opp avvik</p>
    </div>
    <button class="btn btn-primary" @click="openModal">Rapporter nytt avvik</button>
  </div>

  <!-- ── Filters ─────────────────────────────────────────────────────────── -->
  <div v-if="!deviationsStore.loading" class="filter-bar">
    <!-- Status -->
    <div class="filter-group">
      <button
        v-for="opt in ([
          { value: 'ALL',         label: 'Alle' },
          { value: 'OPEN',        label: 'Åpen' },
          { value: 'IN_PROGRESS', label: 'Under arbeid' },
          { value: 'RESOLVED',    label: 'Løst' },
        ] as const)"
        :key="opt.value"
        class="filter-pill"
        :class="{ 'filter-pill--active': filterStatus === opt.value }"
        @click="filterStatus = opt.value"
      >{{ opt.label }}</button>
    </div>

    <!-- Module -->
    <div class="filter-group">
      <button
        v-for="opt in ([
          { value: 'ALL',        label: 'Alle moduler' },
          { value: 'IK_MAT',     label: 'IK-Mat' },
          { value: 'IK_ALKOHOL', label: 'IK-Alkohol' },
        ] as const)"
        :key="opt.value"
        class="filter-pill"
        :class="{ 'filter-pill--active': filterModule === opt.value }"
        @click="filterModule = opt.value"
      >{{ opt.label }}</button>
    </div>

    <!-- Severity -->
    <div class="filter-group">
      <button
        v-for="opt in ([
          { value: 'ALL',      label: 'Alle' },
          { value: 'CRITICAL', label: 'Kritisk' },
          { value: 'HIGH',     label: 'Høy' },
          { value: 'MEDIUM',   label: 'Medium' },
          { value: 'LOW',      label: 'Lav' },
        ] as const)"
        :key="opt.value"
        class="filter-pill"
        :class="{ 'filter-pill--active': filterSeverity === opt.value }"
        @click="filterSeverity = opt.value"
      >{{ opt.label }}</button>
    </div>

    <!-- Clear + count -->
    <div class="filter-meta">
      <span class="text-muted text-xs">
        {{ filteredDeviations.length }} av {{ deviationsStore.deviations.length }} avvik
      </span>
      <button
        v-if="hasActiveFilter"
        class="filter-clear"
        @click="clearFilters"
      >Nullstill</button>
    </div>
  </div>

  <!-- ── Loading ──────────────────────────────────────────────────────────── -->
  <div v-if="deviationsStore.loading" class="text-muted text-sm">Laster avvik…</div>

  <!-- ── Empty state ──────────────────────────────────────────────────────── -->
  <div
    v-else-if="deviationsStore.deviations.length === 0"
    class="card"
    style="text-align: center; padding: 2.5rem 1.5rem;"
  >
    <p class="text-muted mb-1">Ingen avvik registrert</p>
    <p class="text-xs text-muted">Trykk «Rapporter nytt avvik» for å opprette det første.</p>
  </div>

  <!-- ── No results after filtering ──────────────────────────────────────── -->
  <div
    v-else-if="filteredDeviations.length === 0"
    class="card"
    style="text-align: center; padding: 2rem 1.5rem;"
  >
    <p class="text-muted text-sm">Ingen avvik matcher filteret.</p>
    <button class="filter-clear" style="margin-top: 0.5rem;" @click="clearFilters">Nullstill filter</button>
  </div>

  <!-- ── Deviation list ───────────────────────────────────────────────────── -->
  <template v-else>
    <div
      v-for="dev in filteredDeviations"
      :key="dev.id"
      class="card mb-3 dev-card"
    >
      <!-- ── Clickable summary (always visible) ── -->
      <button
        class="dev-trigger"
        :aria-expanded="!!expanded[dev.id]"
        @click="toggleExpand(dev.id)"
      >
        <div class="dev-trigger-row">
          <span class="dev-title">{{ dev.title }}</span>
          <div class="dev-badges">
            <span class="badge" :class="statusBadge(dev.status)">{{ statusLabel(dev.status) }}</span>
            <span class="badge" :class="severityBadge(dev.severity)">{{ severityLabel(dev.severity) }}</span>
            <svg
              class="dev-chevron"
              :class="{ 'dev-chevron--open': !!expanded[dev.id] }"
              width="16" height="16" viewBox="0 0 16 16" fill="none"
              aria-hidden="true"
            >
              <path d="M4 6l4 4 4-4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
        </div>
        <p class="dev-meta">
          {{ fmtDate(dev.reportedAt) }} &middot; {{ dev.reportedBy }}
        </p>
      </button>

      <!-- ── Expandable body ── -->
      <div class="dev-body" :class="{ 'dev-body--open': !!expanded[dev.id] }">
        <div class="dev-body-inner">
          <div class="dev-body-content">

            <!-- Module badge + description -->
            <span
              class="mod-badge mb-2"
              :class="dev.moduleType === 'IK_MAT' ? 'ik-mat' : 'ik-alkohol'"
              style="display: inline-block;"
            >
              {{ dev.moduleType === 'IK_MAT' ? 'IK-Mat' : 'IK-Alkohol' }}
            </span>
            <p class="text-sm" style="line-height: 1.55; margin-bottom: 1rem;">{{ dev.description }}</p>

            <!-- Resolution block (shown when resolved) -->
            <div v-if="dev.status === 'RESOLVED' && dev.resolution" class="resolution-block">
              <p class="resolution-label">
                Løsning
                <span v-if="dev.resolvedAt"> &mdash; {{ fmtDate(dev.resolvedAt) }}</span>
              </p>
              <p class="text-sm">{{ dev.resolution }}</p>
            </div>

            <!-- Action buttons (managers, not yet resolved, not currently resolving) -->
            <div
              v-if="canManage && dev.status !== 'RESOLVED' && resolvingId !== dev.id"
              class="action-row"
            >
              <button
                v-if="dev.status === 'OPEN'"
                class="btn btn-secondary btn-sm"
                :disabled="!!statusSaving[dev.id]"
                @click.stop="markInProgress(dev.id)"
              >
                {{ statusSaving[dev.id] ? 'Lagrer…' : 'Merk som under arbeid' }}
              </button>
              <button
                class="btn btn-primary btn-sm"
                @click.stop="startResolve(dev.id)"
              >
                Løs avvik
              </button>
            </div>

            <!-- Resolve form (inline, slides into the card) -->
            <div v-if="resolvingId === dev.id" class="resolve-form">
              <label class="resolve-form-label" :for="`resolve-${dev.id}`">
                Beskriv hva som ble gjort
              </label>
              <textarea
                :id="`resolve-${dev.id}`"
                v-model="resolutionText"
                rows="3"
                placeholder="Beskriv tiltaket som løste avviket…"
                style="width: 100%; resize: vertical;"
              />
              <div class="flex gap-2" style="margin-top: 0.5rem;">
                <button class="btn btn-secondary btn-sm" @click.stop="cancelResolve">Avbryt</button>
                <button
                  class="btn btn-primary btn-sm"
                  :disabled="!resolutionText.trim() || resolveSaving"
                  @click.stop="confirmResolve(dev.id)"
                >
                  {{ resolveSaving ? 'Lagrer…' : 'Bekreft løsning' }}
                </button>
              </div>
            </div>

            <!-- Comments section -->
            <div class="comments-section">
              <div class="divider" style="margin: 0.875rem 0 0.75rem;" />

              <div v-if="commentsLoading[dev.id]" class="text-muted text-xs">
                Laster kommentarer…
              </div>

              <template v-else>
                <p
                  v-if="!commentsMap[dev.id]?.length"
                  class="text-muted text-xs"
                  style="margin-bottom: 0.625rem;"
                >
                  Ingen kommentarer ennå.
                </p>
                <div
                  v-for="comment in commentsMap[dev.id]"
                  :key="comment.id"
                  class="comment-item"
                >
                  <div class="comment-meta">
                    <span class="font-medium" style="font-size: 0.8125rem;">{{ comment.createdBy }}</span>
                    <span class="text-muted text-xs">{{ fmtDate(comment.createdAt) }}</span>
                  </div>
                  <p class="text-sm" style="line-height: 1.5;">{{ comment.comment }}</p>
                </div>
              </template>

              <!-- Comment compose (managers only) -->
              <div v-if="canManage" class="comment-compose">
                <textarea
                  v-model="commentDraft[dev.id]"
                  rows="2"
                  placeholder="Legg til en kommentar…"
                  style="width: 100%; resize: none;"
                  :aria-label="`Kommentar til avvik: ${dev.title}`"
                />
                <button
                  class="btn btn-secondary btn-sm"
                  style="margin-top: 0.375rem;"
                  :disabled="!(commentDraft[dev.id] ?? '').trim() || !!commentSaving[dev.id]"
                  @click.stop="submitComment(dev.id)"
                >
                  {{ commentSaving[dev.id] ? 'Lagrer…' : 'Send kommentar' }}
                </button>
              </div>
            </div>

          </div><!-- /dev-body-content -->
        </div><!-- /dev-body-inner -->
      </div><!-- /dev-body -->
    </div>
  </template>

  <!-- ── New deviation modal ───────────────────────────────────────────────── -->
  <AppModal
    :show="showModal"
    title="Rapporter nytt avvik"
    @close="showModal = false"
  >
    <div style="padding: 1.25rem 1.5rem; display: flex; flex-direction: column; gap: 1rem;">
      <div class="form-group">
        <label for="dev-title" style="font-size: 0.875rem; font-weight: 500;">
          Tittel <span style="color: var(--c-danger)">*</span>
        </label>
        <input
          id="dev-title"
          v-model="form.title"
          type="text"
          placeholder="Kort beskrivelse av avviket"
          required
        />
      </div>

      <div class="form-group">
        <label for="dev-desc" style="font-size: 0.875rem; font-weight: 500;">
          Beskrivelse <span style="color: var(--c-danger)">*</span>
        </label>
        <textarea
          id="dev-desc"
          v-model="form.description"
          rows="4"
          placeholder="Detaljer om avviket…"
          required
          style="resize: vertical;"
        />
      </div>

      <div class="form-grid-2">
        <div class="form-group">
          <label for="dev-severity" style="font-size: 0.875rem; font-weight: 500;">Alvorlighetsgrad</label>
          <select id="dev-severity" v-model="form.severity">
            <option value="CRITICAL">Kritisk</option>
            <option value="HIGH">Høy</option>
            <option value="MEDIUM">Medium</option>
            <option value="LOW">Lav</option>
          </select>
        </div>

        <div class="form-group">
          <label for="dev-module" style="font-size: 0.875rem; font-weight: 500;">Modul</label>
          <select id="dev-module" v-model="form.moduleType">
            <option value="IK_MAT">IK-Mat</option>
            <option value="IK_ALKOHOL">IK-Alkohol</option>
          </select>
        </div>
      </div>
    </div>

    <template #footer>
      <button class="btn btn-secondary" @click="showModal = false">Avbryt</button>
      <button
        class="btn btn-primary"
        :disabled="deviationsStore.saving || !form.title.trim() || !form.description.trim()"
        @click="submitDeviation"
      >
        {{ deviationsStore.saving ? 'Lagrer…' : 'Rapporter avvik' }}
      </button>
    </template>
  </AppModal>
</template>

<style scoped>
/* ── Card: no default padding so we control it fully ── */
.dev-card {
  padding: 0;
  overflow: hidden;
}

/* ── Trigger: full-width click area ── */
.dev-trigger {
  display: block;
  width: 100%;
  background: none;
  border: none;
  padding: 1rem 1.25rem 0.75rem;
  cursor: pointer;
  text-align: left;
  transition: background 0.1s;
}
.dev-trigger:hover { background: var(--c-surface-2); }
.dev-trigger:focus-visible {
  outline: 2px solid var(--c-primary);
  outline-offset: -2px;
}

.dev-trigger-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 0.75rem;
  margin-bottom: 0.25rem;
}

.dev-title {
  font-weight: 600;
  font-size: 0.9375rem;
  flex: 1;
  min-width: 0;
  line-height: 1.4;
}

.dev-badges {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  flex-shrink: 0;
}

.dev-meta {
  font-size: 0.75rem;
  color: var(--c-text-2);
  margin: 0;
}

/* ── Chevron ── */
.dev-chevron {
  color: var(--c-text-3);
  transition: transform 0.2s ease;
  flex-shrink: 0;
}
.dev-chevron--open {
  transform: rotate(180deg);
}

/* ── Expand animation (grid-template-rows trick) ── */
.dev-body {
  display: grid;
  grid-template-rows: 0fr;
  transition: grid-template-rows 0.24s ease;
}
.dev-body--open {
  grid-template-rows: 1fr;
}
.dev-body-inner {
  overflow: hidden;
}
.dev-body-content {
  padding: 0.25rem 1.25rem 1.25rem;
}

/* ── Resolution block ── */
.resolution-block {
  background: var(--c-success-bg);
  border-radius: var(--radius, 6px);
  padding: 0.625rem 0.875rem;
  margin-bottom: 1rem;
}
.resolution-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--c-success-text);
  margin: 0 0 0.25rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

/* ── Action row ── */
.action-row {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  margin-bottom: 0.875rem;
}

/* ── Resolve form ── */
.resolve-form {
  background: var(--c-surface-2);
  border-radius: var(--radius, 6px);
  padding: 0.875rem;
  margin-bottom: 0.875rem;
}
.resolve-form-label {
  display: block;
  font-size: 0.875rem;
  font-weight: 500;
  margin-bottom: 0.5rem;
}

/* ── Comments ── */
.comment-item {
  padding: 0.5rem 0;
}
.comment-meta {
  display: flex;
  gap: 0.5rem;
  align-items: baseline;
  margin-bottom: 0.2rem;
}

.comment-compose {
  margin-top: 0.75rem;
}

/* ── Filters ── */
.filter-bar {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1.25rem;
}

.filter-group {
  display: flex;
  gap: 0.375rem;
  flex-wrap: wrap;
}

.filter-pill {
  padding: 0.3rem 0.75rem;
  border-radius: 999px;
  border: 1px solid var(--c-border);
  background: var(--c-surface);
  color: var(--c-text-2);
  font-size: 0.8125rem;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.1s, border-color 0.1s, color 0.1s;
  white-space: nowrap;
}
.filter-pill:hover {
  background: var(--c-surface-2);
  color: var(--c-text);
}
.filter-pill--active {
  background: var(--c-primary);
  border-color: var(--c-primary);
  color: #fff;
}

.filter-meta {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.filter-clear {
  background: none;
  border: none;
  color: var(--c-primary);
  font-size: 0.8125rem;
  font-weight: 500;
  cursor: pointer;
  padding: 0;
  text-decoration: underline;
  text-underline-offset: 2px;
}
.filter-clear:hover { opacity: 0.75; }
</style>
