<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAlkoholStore } from '@/stores/alkohol'
import type { AgeVerificationOutcome } from '@/types'

const store = useAlkoholStore()

const showForm = ref(false)
const selectedOutcome = ref<AgeVerificationOutcome>('APPROVED')
const note = ref('')
const saving = ref(false)

onMounted(() => store.fetchEntries())

const outcomeLabels: Record<AgeVerificationOutcome, string> = {
  APPROVED: 'Godkjent',
  DENIED:   'Nektet',
  UNSURE:   'Usikker'
}

const outcomeClasses: Record<AgeVerificationOutcome, string> = {
  APPROVED: 'badge--success',
  DENIED:   'badge--danger',
  UNSURE:   'badge--warning'
}

function formatTime(iso: string): string {
  return new Date(iso).toLocaleTimeString('nb-NO', { hour: '2-digit', minute: '2-digit' })
}

function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString('nb-NO', { day: 'numeric', month: 'short' })
}

async function submit() {
  if (saving.value) return
  saving.value = true
  try {
    await store.addEntry({
      outcome: selectedOutcome.value,
      note: note.value.trim() || undefined
    })
    note.value = ''
    selectedOutcome.value = 'APPROVED'
    showForm.value = false
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <section aria-label="Alderskontroll-logg">

    <div class="section-header">
      <h1 class="section-title">Alderskontroll-logg</h1>
      <button class="btn btn--primary" @click="showForm = !showForm" :aria-expanded="showForm">
        {{ showForm ? 'Avbryt' : '+ Registrer kontroll' }}
      </button>
    </div>

    <!-- Inline entry form -->
    <div v-if="showForm" class="entry-form card" role="form" aria-label="Registrer alderskontroll">
      <div class="form-group">
        <label class="form-label" id="outcome-label">Resultat</label>
        <div class="outcome-buttons" role="group" aria-labelledby="outcome-label">
          <button
            v-for="(label, key) in outcomeLabels"
            :key="key"
            class="outcome-btn"
            :class="{ 'outcome-btn--active': selectedOutcome === key }"
            :aria-pressed="selectedOutcome === key"
            @click="selectedOutcome = key as AgeVerificationOutcome"
          >
            {{ label }}
          </button>
        </div>
      </div>

      <div class="form-group">
        <label for="ald-note" class="form-label">Merknad (valgfritt)</label>
        <input
          id="ald-note"
          v-model="note"
          type="text"
          class="form-input"
          placeholder="F.eks. manglende legitimasjon"
          maxlength="200"
        />
      </div>

      <button
        class="btn btn--save"
        :disabled="saving"
        @click="submit"
        :aria-busy="saving"
      >
        {{ saving ? 'Lagrer...' : 'Lagre' }}
      </button>
    </div>

    <!-- Entry list -->
    <div v-if="store.loading" class="loading-text" aria-live="polite">Laster logg...</div>

    <ul v-else class="entry-list" aria-label="Registrerte alderskontroller">
      <li
        v-for="entry in store.entries"
        :key="entry.id"
        class="entry-card"
      >
        <div class="entry-time">
          <span class="entry-time__time">{{ formatTime(entry.recordedAt) }}</span>
          <span class="entry-time__date">{{ formatDate(entry.recordedAt) }}</span>
        </div>
        <div class="entry-body">
          <span class="badge" :class="outcomeClasses[entry.outcome]">
            {{ outcomeLabels[entry.outcome] }}
          </span>
          <span class="entry-staff">{{ entry.recordedBy }}</span>
          <p v-if="entry.note" class="entry-note">{{ entry.note }}</p>
        </div>
      </li>
    </ul>

    <p v-if="!store.loading && store.entries.length === 0" class="empty-state">
      Ingen kontroller registrert i dag. Trykk "+ Registrer kontroll" for å logge.
    </p>

  </section>
</template>

<style scoped>
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.section-title { font-size: 18px; font-weight: 700; color: #0f172a; }

.entry-form {
  margin-bottom: 20px;
  padding: 16px;
  border: 2px solid hsl(var(--ik-alkohol-hue, 38), 90%, 70%);
  border-radius: 12px;
  background: hsl(var(--ik-alkohol-hue, 38), 100%, 98%);
}

.form-label { display: block; font-size: 13px; font-weight: 600; color: #0f172a; margin-bottom: 6px; }

.outcome-buttons { display: flex; gap: 8px; }
.outcome-btn {
  flex: 1;
  padding: 10px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  background: #fff;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  color: #334155;
  transition: border-color 0.1s, background 0.1s;
  min-height: 44px;
}
.outcome-btn--active {
  border-color: hsl(var(--ik-alkohol-hue, 38), 85%, 45%);
  background: hsl(var(--ik-alkohol-hue, 38), 100%, 96%);
  color: hsl(var(--ik-alkohol-hue, 38), 80%, 25%);
  font-weight: 700;
}
.outcome-btn:focus-visible { outline: 2px solid hsl(var(--ik-alkohol-hue, 38), 85%, 45%); outline-offset: 2px; }

.form-input {
  width: 100%;
  padding: 10px 12px;
  border: 1.5px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  color: #0f172a;
}
.form-input:focus { outline: 2px solid hsl(var(--ik-alkohol-hue, 38), 85%, 45%); outline-offset: 1px; border-color: transparent; }

.btn--save {
  width: 100%;
  margin-top: 12px;
  padding: 14px;
  background: hsl(var(--ik-alkohol-hue, 38), 85%, 45%);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  min-height: 44px;
}
.btn--save:disabled { opacity: 0.6; cursor: not-allowed; }
.btn--save:focus-visible { outline: 2px solid #0f172a; outline-offset: 2px; }

.entry-list { list-style: none; padding: 0; display: flex; flex-direction: column; gap: 8px; }
.entry-card {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 12px 14px;
}
.entry-time { display: flex; flex-direction: column; align-items: center; min-width: 42px; }
.entry-time__time { font-size: 14px; font-weight: 700; color: #0f172a; }
.entry-time__date { font-size: 10px; color: #94a3b8; }
.entry-body { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.entry-staff { font-size: 12px; color: #475569; }
.entry-note { font-size: 12px; color: #64748b; margin: 0; }

.badge { display: inline-block; padding: 3px 10px; border-radius: 99px; font-size: 11px; font-weight: 700; }
.badge--success { background: #f0fdf4; color: #166534; }
.badge--danger  { background: #fef2f2; color: #991b1b; }
.badge--warning { background: #fffbeb; color: #92400e; }

.loading-text { color: #94a3b8; font-size: 14px; padding: 20px 0; text-align: center; }
.empty-state  { color: #94a3b8; font-size: 14px; padding: 32px 0; text-align: center; }
</style>
