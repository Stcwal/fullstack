<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useDeviationsStore } from '@/stores/deviations'
import AppModal from '@/components/AppModal.vue'
import type { DeviationSeverity, ModuleType, NewDeviation } from '@/types'

const deviationsStore = useDeviationsStore()

// ── Modal state ──────────────────────────────────────────────────────────────
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

function closeModal() {
  showModal.value = false
}

async function submitDeviation() {
  if (!form.value.title.trim() || !form.value.description.trim()) return
  await deviationsStore.report({ ...form.value })
  closeModal()
}

// ── Date formatting ───────────────────────────────────────────────────────────
function fmtDate(iso: string) {
  const d = new Date(iso)
  const today = new Date()
  const yesterday = new Date(); yesterday.setDate(today.getDate() - 1)
  const t = `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
  if (d.toDateString() === today.toDateString()) return `I dag ${t}`
  if (d.toDateString() === yesterday.toDateString()) return `I går ${t}`
  return `${d.getDate()}. ${['jan','feb','mar','apr','mai','jun','jul','aug','sep','okt','nov','des'][d.getMonth()]}`
}

onMounted(() => deviationsStore.fetchAll())
</script>

<template>
  <!-- ── Page header ─────────────────────────────────────────────────────── -->
  <div class="flex items-center justify-between mb-4">
    <div>
      <h1 class="page-title">Avvik</h1>
      <p class="page-subtitle">Rapporter og følg opp avvik</p>
    </div>
    <button class="btn btn-primary" @click="openModal">
      Rapporter nytt avvik
    </button>
  </div>

  <!-- ── Loading ─────────────────────────────────────────────────────────── -->
  <div v-if="deviationsStore.loading" class="text-muted text-sm">
    Laster avvik…
  </div>

  <!-- ── Empty state ─────────────────────────────────────────────────────── -->
  <div
    v-else-if="!deviationsStore.loading && deviationsStore.deviations.length === 0"
    class="card"
    style="text-align: center; padding: 2.5rem 1.5rem;"
  >
    <p class="text-muted mb-1">Ingen avvik registrert</p>
    <p class="text-xs text-muted">Trykk «Rapporter nytt avvik» for å opprette det første.</p>
  </div>

  <!-- ── Deviation list ──────────────────────────────────────────────────── -->
  <template v-else>
    <div
      v-for="dev in deviationsStore.deviations"
      :key="dev.id"
      class="card mb-3"
    >
      <!-- Card header: title + status badge -->
      <div class="card-header flex items-center justify-between gap-3">
        <h3 class="font-semibold flex-1 min-w-0 truncate" style="font-size: 0.9375rem;">
          {{ dev.title }}
        </h3>
        <span
          class="badge"
          :class="{
            'badge-danger':  dev.status === 'OPEN',
            'badge-warning': dev.status === 'IN_PROGRESS',
            'badge-success': dev.status === 'RESOLVED',
          }"
        >
          <template v-if="dev.status === 'OPEN'">Åpen</template>
          <template v-else-if="dev.status === 'IN_PROGRESS'">Under arbeid</template>
          <template v-else>Løst</template>
        </span>
      </div>

      <!-- Meta: reported by / date -->
      <p class="text-muted text-xs mb-2">
        Rapportert: {{ fmtDate(dev.reportedAt) }} av {{ dev.reportedBy }}
      </p>

      <!-- Description -->
      <p class="text-sm mb-3">{{ dev.description }}</p>

      <!-- Footer: module badge + severity badge -->
      <div class="flex items-center gap-2">
        <span
          class="mod-badge"
          :class="dev.moduleType === 'IK_MAT' ? 'ik-mat' : 'ik-alkohol'"
        >
          {{ dev.moduleType === 'IK_MAT' ? 'IK-Mat' : 'IK-Alkohol' }}
        </span>
        <span
          class="badge"
          :class="{
            'badge-danger':  dev.severity === 'CRITICAL',
            'badge-warning': dev.severity === 'MEDIUM',
            'badge-neutral': dev.severity === 'LOW',
          }"
        >
          <template v-if="dev.severity === 'CRITICAL'">Kritisk</template>
          <template v-else-if="dev.severity === 'MEDIUM'">Medium</template>
          <template v-else>Lav</template>
        </span>
      </div>
    </div>
  </template>

  <!-- ── New deviation modal ─────────────────────────────────────────────── -->
  <AppModal
    :show="showModal"
    title="Rapporter nytt avvik"
    @close="closeModal"
  >
    <div style="padding: 1.25rem 1.5rem; display: flex; flex-direction: column; gap: 1rem;">
      <!-- Title -->
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

      <!-- Description -->
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

      <!-- Severity + Module (2-column grid) -->
      <div class="form-grid-2">
        <div class="form-group">
          <label for="dev-severity" style="font-size: 0.875rem; font-weight: 500;">Alvorlighetsgrad</label>
          <select id="dev-severity" v-model="form.severity">
            <option value="CRITICAL">Kritisk</option>
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
      <button class="btn btn-secondary" @click="closeModal">Avbryt</button>
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
