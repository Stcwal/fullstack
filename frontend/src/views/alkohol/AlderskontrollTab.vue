<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAlkoholStore } from '@/stores/alkohol'
import { useLayoutStore } from '@/stores/layout'
import type { AgeVerificationOutcome } from '@/types'

const store  = useAlkoholStore()
const layout = useLayoutStore()

const isTablet = computed(() => layout.isTabletMode)

// ── Desktop ───────────────────────────────────────────────────────────────────
const selectedOutcome = ref<AgeVerificationOutcome>('APPROVED')
const note            = ref('')
const saving          = ref(false)

// ── Tablet quick-tap ──────────────────────────────────────────────────────────
const quickNote    = ref('')
const flashingBtn  = ref<AgeVerificationOutcome | null>(null)

onMounted(() => store.fetchEntries())

const todayStats = computed(() => {
  const today = new Date().toDateString()
  const t = store.entries.filter(e => new Date(e.recordedAt).toDateString() === today)
  return {
    total:    t.length,
    approved: t.filter(e => e.outcome === 'APPROVED').length,
    denied:   t.filter(e => e.outcome === 'DENIED').length,
  }
})

const recentEntries = computed(() => store.entries.slice(0, 4))

const outcomes: { key: AgeVerificationOutcome; label: string }[] = [
  { key: 'APPROVED', label: 'Godkjent' },
  { key: 'DENIED',   label: 'Nektet'   },
  { key: 'UNSURE',   label: 'Usikker'  },
]

const submitLabel = computed(() =>
  outcomes.find(o => o.key === selectedOutcome.value)?.label ?? ''
)

function formatTime(iso: string): string {
  return new Date(iso).toLocaleTimeString('nb-NO', { hour: '2-digit', minute: '2-digit' })
}

function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString('nb-NO', { day: 'numeric', month: 'short' })
}

// ── Tablet tap ────────────────────────────────────────────────────────────────
async function tapLog(outcome: AgeVerificationOutcome) {
  if (store.saving) return
  flashingBtn.value = outcome
  await store.addEntry({ outcome, note: quickNote.value.trim() || undefined })
  quickNote.value = ''
  setTimeout(() => { flashingBtn.value = null }, 320)
}

// ── Desktop submit ────────────────────────────────────────────────────────────
async function submit() {
  if (saving.value) return
  saving.value = true
  try {
    await store.addEntry({ outcome: selectedOutcome.value, note: note.value.trim() || undefined })
    note.value = ''
    selectedOutcome.value = 'APPROVED'
  } finally {
    saving.value = false
  }
}
</script>

<template>

  <!-- ════════════════════════════════════════
       TABLET — quick-tap interface
  ════════════════════════════════════════ -->
  <section v-if="isTablet" class="ald-tablet" aria-label="Alderskontroll">

    <div class="t-stats" role="status" aria-live="polite">
      <span class="t-stats__total"><strong>{{ todayStats.total }}</strong> i dag</span>
      <span class="t-stats__sep" aria-hidden="true">·</span>
      <span class="t-stats__ok"><strong>{{ todayStats.approved }}</strong> godkjent</span>
      <span class="t-stats__sep" aria-hidden="true">·</span>
      <span class="t-stats__deny"><strong>{{ todayStats.denied }}</strong> nektet</span>
    </div>

    <input
      v-model="quickNote"
      type="text"
      class="t-note"
      placeholder="Legg til merknad (valgfritt)…"
      aria-label="Merknad til neste registrering"
      maxlength="200"
    />

    <div class="t-grid" role="group" aria-label="Registrer alderskontroll">

      <button
        class="t-btn t-btn--denied"
        :class="{ 't-btn--flash': flashingBtn === 'DENIED' }"
        :disabled="store.saving"
        :aria-busy="store.saving"
        aria-label="Logg nektet"
        @click="tapLog('DENIED')"
      >
        <span class="t-btn__check" aria-hidden="true">✗</span>
        <span class="t-btn__label">NEKTET</span>
      </button>

      <button
        class="t-btn t-btn--unsure"
        :class="{ 't-btn--flash': flashingBtn === 'UNSURE' }"
        :disabled="store.saving"
        :aria-busy="store.saving"
        aria-label="Logg usikker"
        @click="tapLog('UNSURE')"
      >
        <span class="t-btn__check" aria-hidden="true">?</span>
        <span class="t-btn__label">USIKKER</span>
      </button>

      <button
        class="t-btn t-btn--approved"
        :class="{ 't-btn--flash': flashingBtn === 'APPROVED' }"
        :disabled="store.saving"
        :aria-busy="store.saving"
        aria-label="Logg godkjent"
        @click="tapLog('APPROVED')"
      >
        <span class="t-btn__check" aria-hidden="true">✓</span>
        <span class="t-btn__label">GODKJENT</span>
      </button>

    </div>

    <div class="t-recent" aria-label="Siste registreringer">
      <p v-if="store.loading" class="t-recent__empty">Laster…</p>
      <p v-else-if="recentEntries.length === 0" class="t-recent__empty">Ingen kontroller registrert i dag</p>
      <div v-else>
        <div v-for="e in recentEntries" :key="e.id" class="t-recent__row">
          <span class="t-recent__time">{{ formatTime(e.recordedAt) }}</span>
          <span class="t-recent__tag" :class="`t-tag--${e.outcome.toLowerCase()}`">
            {{ e.outcome === 'APPROVED' ? 'Godkjent' : e.outcome === 'DENIED' ? 'Nektet' : 'Usikker' }}
          </span>
          <span class="t-recent__staff">{{ e.recordedBy }}</span>
          <span v-if="e.note" class="t-recent__note">— {{ e.note }}</span>
        </div>
      </div>
    </div>

  </section>


  <!-- ════════════════════════════════════════
       DESKTOP — form interface
  ════════════════════════════════════════ -->
  <section v-else class="ald" aria-label="Alderskontroll-logg">

    <div class="stats-bar" role="status" aria-live="polite">
      <span class="stats-bar__item">
        <strong>{{ todayStats.total }}</strong> kontroller i dag
      </span>
      <span class="stats-bar__dot" aria-hidden="true">·</span>
      <span class="stats-bar__item stats-bar__item--ok">
        <strong>{{ todayStats.approved }}</strong> godkjent
      </span>
      <span class="stats-bar__dot" aria-hidden="true">·</span>
      <span class="stats-bar__item stats-bar__item--deny">
        <strong>{{ todayStats.denied }}</strong> nektet
      </span>
    </div>

    <div class="form-panel" role="form" aria-label="Registrer alderskontroll">

      <div class="outcome-row" role="group" aria-label="Velg resultat">
        <button
          v-for="o in outcomes"
          :key="o.key"
          class="outcome-btn"
          :class="`outcome-btn--${o.key.toLowerCase()}`"
          :aria-pressed="selectedOutcome === o.key"
          @click="selectedOutcome = o.key"
        >{{ o.label }}</button>
      </div>

      <input
        v-model="note"
        type="text"
        class="note-input"
        placeholder="Merknad (valgfritt)"
        maxlength="200"
        aria-label="Merknad"
      />

      <button
        class="submit-btn"
        :class="`submit-btn--${selectedOutcome.toLowerCase()}`"
        :disabled="saving"
        :aria-busy="saving"
        @click="submit"
      >{{ saving ? 'Lagrer…' : `Logg — ${submitLabel}` }}</button>

    </div>

    <div class="log">
      <p class="log__heading">Logg</p>

      <p v-if="store.loading" class="log__empty">Laster…</p>
      <p v-else-if="store.entries.length === 0" class="log__empty">Ingen kontroller registrert i dag.</p>

      <ul v-else class="log__list" aria-label="Registrerte alderskontroller">
        <li v-for="entry in store.entries" :key="entry.id" class="log__row">
          <span class="log__time">{{ formatTime(entry.recordedAt) }}<em>{{ formatDate(entry.recordedAt) }}</em></span>
          <span class="outcome-tag" :class="`outcome-tag--${entry.outcome.toLowerCase()}`">
            {{ entry.outcome === 'APPROVED' ? 'Godkjent' : entry.outcome === 'DENIED' ? 'Nektet' : 'Usikker' }}
          </span>
          <span class="log__staff">{{ entry.recordedBy }}</span>
          <span v-if="entry.note" class="log__note">— {{ entry.note }}</span>
        </li>
      </ul>
    </div>

  </section>
</template>

<style scoped>
/* ════════════════════════════════════════
   TABLET — quick-tap
════════════════════════════════════════ */
.ald-tablet {
  display: flex;
  flex-direction: column;
  gap: 10px;
  height: 100%;
  padding: 0;
}

/* Stats strip */
.t-stats {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #475569;
  padding: 9px 14px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  flex-shrink: 0;
}
.t-stats__total strong { font-weight: 700; color: #0f172a; }
.t-stats__ok strong    { font-weight: 700; color: #15803d; }
.t-stats__deny strong  { font-weight: 700; color: #dc2626; }
.t-stats__sep          { color: #cbd5e1; }

/* Note input */
.t-note {
  flex-shrink: 0;
  width: 100%;
  padding: 11px 14px;
  border: 1.5px solid #e2e8f0;
  border-radius: 6px;
  font-size: 14px;
  color: #0f172a;
  background: #f8fafc;
  box-sizing: border-box;
  font-family: inherit;
}
.t-note::placeholder { color: #94a3b8; }
.t-note:focus        { outline: none; border-color: #334155; background: #fff; }

/* Tap grid */
.t-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  flex: 1;
  min-height: 180px;
}

.t-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  color: #fff;
  font-family: inherit;
  -webkit-tap-highlight-color: transparent;
  transition: filter 0.08s;
  user-select: none;
}

.t-btn:active:not(:disabled) { filter: brightness(1.18); transform: scale(0.985); }
.t-btn:disabled               { opacity: 0.55; cursor: not-allowed; }
.t-btn:focus-visible          { outline: 2px solid rgba(255,255,255,0.7); outline-offset: -3px; }

.t-btn--denied   { background: #991b1b; grid-row: 1; }
.t-btn--unsure   { background: #92400e; grid-row: 1; }
.t-btn--approved { background: #166534; grid-column: 1 / -1; grid-row: 2; min-height: 80px; flex-direction: row; gap: 16px; }

@keyframes tap-pop {
  0%   { transform: scale(1);    filter: brightness(1); }
  25%  { transform: scale(0.96); filter: brightness(1.05); }
  70%  { transform: scale(1.01); filter: brightness(1.12); }
  100% { transform: scale(1);    filter: brightness(1); }
}

.t-btn--flash { animation: tap-pop 0.32s ease-out; }

.t-btn__check {
  font-size: 2.75rem;
  font-weight: 700;
  line-height: 1;
}
.t-btn__label {
  font-size: 1.375rem;
  font-weight: 800;
  letter-spacing: 0.05em;
}

/* Recent log */
.t-recent {
  flex-shrink: 0;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  overflow: hidden;
  background: #fff;
}

.t-recent__empty {
  font-size: 12px;
  color: #94a3b8;
  text-align: center;
  padding: 10px;
  margin: 0;
}

.t-recent__row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  font-size: 12px;
  border-bottom: 1px solid #f1f5f9;
  min-height: 36px;
}
.t-recent__row:last-child { border-bottom: none; }

.t-recent__time  { font-weight: 700; color: #0f172a; min-width: 38px; flex-shrink: 0; }
.t-recent__staff { color: #475569; font-weight: 500; }
.t-recent__note  { color: #94a3b8; font-size: 11px; }

.t-recent__tag {
  padding: 2px 7px;
  border-radius: 3px;
  font-size: 10px;
  font-weight: 700;
  flex-shrink: 0;
}
.t-tag--approved { background: #f0fdf4; color: #15803d; }
.t-tag--denied   { background: #fef2f2; color: #dc2626; }
.t-tag--unsure   { background: #fffbeb; color: #92400e; }


/* ════════════════════════════════════════
   DESKTOP — form
════════════════════════════════════════ */
.ald {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.stats-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #475569;
  padding: 10px 14px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
}
.stats-bar__item strong      { font-weight: 700; color: #0f172a; }
.stats-bar__item--ok strong  { color: #15803d; }
.stats-bar__item--deny strong { color: #dc2626; }
.stats-bar__dot              { color: #cbd5e1; font-weight: 300; }

.form-panel {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 16px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
}

.outcome-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.outcome-btn {
  padding: 14px 8px;
  border-radius: 4px;
  border: 1.5px solid #e2e8f0;
  background: #f8fafc;
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  cursor: pointer;
  min-height: 52px;
  transition: background 0.1s, border-color 0.1s, color 0.1s;
  font-family: inherit;
}
.outcome-btn:focus-visible { outline: 2px solid #334155; outline-offset: 2px; }
.outcome-btn:hover:not([aria-pressed="true"]) { background: #f1f5f9; border-color: #cbd5e1; }

.outcome-btn--approved[aria-pressed="true"] { background: #15803d; border-color: #15803d; color: #fff; }
.outcome-btn--denied[aria-pressed="true"]   { background: #dc2626; border-color: #dc2626; color: #fff; }
.outcome-btn--unsure[aria-pressed="true"]   { background: #b45309; border-color: #b45309; color: #fff; }

.note-input {
  width: 100%;
  padding: 11px 12px;
  border: 1.5px solid #e2e8f0;
  border-radius: 4px;
  font-size: 14px;
  color: #0f172a;
  background: #f8fafc;
  box-sizing: border-box;
  font-family: inherit;
}
.note-input::placeholder { color: #94a3b8; }
.note-input:focus        { outline: none; border-color: #334155; background: #fff; }

.submit-btn {
  width: 100%;
  padding: 15px;
  border: none;
  border-radius: 4px;
  font-size: 15px;
  font-weight: 700;
  color: #fff;
  cursor: pointer;
  min-height: 52px;
  transition: opacity 0.1s;
  font-family: inherit;
}
.submit-btn:disabled      { opacity: 0.55; cursor: not-allowed; }
.submit-btn:focus-visible { outline: 2px solid #0f172a; outline-offset: 2px; }
.submit-btn--approved     { background: #15803d; }
.submit-btn--denied       { background: #dc2626; }
.submit-btn--unsure       { background: #b45309; }

.log__heading {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.07em;
  color: #94a3b8;
  margin: 0 0 6px;
}
.log__empty { color: #94a3b8; font-size: 13px; text-align: center; padding: 16px 0; }

.log__list {
  list-style: none;
  padding: 0;
  margin: 0;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  overflow: hidden;
}

.log__row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  font-size: 13px;
  border-bottom: 1px solid #f1f5f9;
  background: #fff;
  min-height: 44px;
}
.log__row:last-child { border-bottom: none; }

.log__time {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  min-width: 36px;
  flex-shrink: 0;
  font-size: 13px;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.1;
}
.log__time em {
  font-style: normal;
  font-size: 10px;
  font-weight: 400;
  color: #94a3b8;
}

.outcome-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 3px;
  font-size: 11px;
  font-weight: 700;
  flex-shrink: 0;
}
.outcome-tag--approved { background: #f0fdf4; color: #15803d; }
.outcome-tag--denied   { background: #fef2f2; color: #dc2626; }
.outcome-tag--unsure   { background: #fffbeb; color: #92400e; }

.log__staff { font-size: 13px; color: #334155; font-weight: 500; }
.log__note  { font-size: 12px; color: #94a3b8; }
</style>
