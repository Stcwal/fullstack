<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAlkoholStore } from '@/stores/alkohol'
import type { AlkoholIncidentType } from '@/types'

const store = useAlkoholStore()
onMounted(() => store.fetchIncidents())

const showForm = ref(false)
const incidentType = ref<AlkoholIncidentType>('BERUSET_GJEST')
const description = ref('')
const followUpRequired = ref(false)
const saving = ref(false)

const incidentTypeLabels: Record<AlkoholIncidentType, string> = {
  NEKTET_SERVERING: 'Nektet servering',
  BERUSET_GJEST:    'Beruset gjest',
  POLITIKONTAKT:    'Politikontakt',
  ANNET:            'Annet'
}

function formatDateTime(iso: string): string {
  return new Date(iso).toLocaleString('nb-NO', {
    day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit'
  })
}

async function submit() {
  if (!description.value.trim() || saving.value) return
  saving.value = true
  try {
    await store.addIncident({
      incidentType: incidentType.value,
      description: description.value.trim(),
      followUpRequired: followUpRequired.value
    })
    description.value = ''
    followUpRequired.value = false
    incidentType.value = 'BERUSET_GJEST'
    showForm.value = false
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <section aria-label="Hendelseslogg">

    <div class="section-header">
      <h1 class="section-title">Hendelseslogg</h1>
      <button class="btn btn--primary" @click="showForm = !showForm" :aria-expanded="showForm">
        {{ showForm ? 'Avbryt' : '+ Rapporter hendelse' }}
      </button>
    </div>

    <!-- Inline form -->
    <div v-if="showForm" class="entry-form card" role="form" aria-label="Rapporter hendelse">

      <div class="form-group">
        <label for="incident-type" class="form-label">Hendelsestype</label>
        <select id="incident-type" v-model="incidentType" class="form-select">
          <option v-for="(label, key) in incidentTypeLabels" :key="key" :value="key">
            {{ label }}
          </option>
        </select>
      </div>

      <div class="form-group">
        <label for="incident-desc" class="form-label">Beskrivelse <span aria-hidden="true">*</span></label>
        <textarea
          id="incident-desc"
          v-model="description"
          class="form-textarea"
          rows="3"
          placeholder="Beskriv hva som skjedde..."
          required
          aria-required="true"
          maxlength="1000"
        ></textarea>
      </div>

      <div class="form-group form-group--inline">
        <input
          id="followup"
          v-model="followUpRequired"
          type="checkbox"
          class="form-checkbox"
        />
        <label for="followup" class="form-label form-label--inline">
          Krever oppfølging
        </label>
      </div>

      <button
        class="btn btn--save"
        :disabled="!description.trim() || saving"
        @click="submit"
        :aria-busy="saving"
      >
        {{ saving ? 'Lagrer...' : 'Lagre hendelse' }}
      </button>
    </div>

    <!-- Incident list -->
    <div v-if="store.loading" class="loading-text" aria-live="polite">Laster hendelser...</div>

    <ul v-else class="incident-list" aria-label="Registrerte hendelser">
      <li
        v-for="incident in store.incidents"
        :key="incident.id"
        class="incident-card"
        :class="{ 'incident-card--followup': incident.followUpRequired }"
      >
        <div class="incident-header">
          <span class="incident-type-badge">{{ incidentTypeLabels[incident.incidentType] }}</span>
          <span v-if="incident.followUpRequired" class="followup-badge" aria-label="Krever oppfølging">
            Oppfølging
          </span>
          <span class="incident-time">{{ formatDateTime(incident.occurredAt) }}</span>
        </div>
        <p class="incident-desc">{{ incident.description }}</p>
        <p class="incident-staff">Rapportert av {{ incident.reportedBy }}</p>
      </li>
    </ul>

    <p v-if="!store.loading && store.incidents.length === 0" class="empty-state">
      Ingen hendelser registrert. Trykk "+ Rapporter hendelse" hvis noe oppstår.
    </p>

  </section>
</template>

<style scoped>
.section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.section-title { font-size: 18px; font-weight: 700; color: #0f172a; }

.entry-form {
  margin-bottom: 20px;
  padding: 16px;
  border: 2px solid hsl(var(--ik-alkohol-hue, 38), 90%, 70%);
  border-radius: 12px;
  background: hsl(var(--ik-alkohol-hue, 38), 100%, 98%);
}

.form-label { display: block; font-size: 13px; font-weight: 600; color: #0f172a; margin-bottom: 6px; }
.form-label--inline { display: inline; margin-bottom: 0; margin-left: 8px; font-weight: 400; }

.form-select, .form-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1.5px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  color: #0f172a;
  font-family: inherit;
}
.form-select:focus, .form-textarea:focus {
  outline: 2px solid hsl(var(--ik-alkohol-hue, 38), 85%, 45%);
  outline-offset: 1px;
  border-color: transparent;
}
.form-textarea { resize: vertical; min-height: 80px; }

.form-group--inline { display: flex; align-items: center; }
.form-checkbox { width: 18px; height: 18px; cursor: pointer; accent-color: hsl(var(--ik-alkohol-hue, 38), 85%, 45%); }

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
.btn--save:disabled { opacity: 0.5; cursor: not-allowed; }
.btn--save:focus-visible { outline: 2px solid #0f172a; outline-offset: 2px; }

.incident-list { list-style: none; padding: 0; display: flex; flex-direction: column; gap: 10px; }
.incident-card {
  background: #fff;
  border: 1.5px solid #e2e8f0;
  border-radius: 12px;
  padding: 14px;
}
.incident-card--followup { border-color: #fca5a5; background: #fef2f2; }

.incident-header { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; margin-bottom: 8px; }
.incident-type-badge {
  background: hsl(var(--ik-alkohol-hue, 38), 100%, 95%);
  color: hsl(var(--ik-alkohol-hue, 38), 80%, 25%);
  border: 1px solid hsl(var(--ik-alkohol-hue, 38), 80%, 75%);
  padding: 3px 10px; border-radius: 99px; font-size: 11px; font-weight: 700;
}
.followup-badge {
  background: #fef2f2; color: #991b1b;
  border: 1px solid #fca5a5;
  padding: 3px 10px; border-radius: 99px; font-size: 11px; font-weight: 700;
}
.incident-time { font-size: 11px; color: #94a3b8; margin-left: auto; }
.incident-desc { font-size: 13px; color: #0f172a; margin: 0 0 6px; line-height: 1.4; }
.incident-staff { font-size: 11px; color: #64748b; margin: 0; }

.loading-text, .empty-state { color: #94a3b8; font-size: 14px; padding: 32px 0; text-align: center; }
</style>
