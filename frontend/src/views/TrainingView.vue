<template>
  <div>
    <div class="mb-4">
      <h1 class="page-title">Opplæring og info</h1>
    </div>

    <nav class="sub-nav mb-4">
      <button
        class="sub-nav-item"
        :class="{ active: activeTab === 'materiale' }"
        @click="activeTab = 'materiale'"
      >
        Opplæringsmateriale
      </button>
      <button
        class="sub-nav-item"
        :class="{ active: activeTab === 'rutiner' }"
        @click="activeTab = 'rutiner'"
      >
        Rutinebeskrivelser
      </button>
      <button
        class="sub-nav-item"
        :class="{ active: activeTab === 'sertifiseringer' }"
        @click="activeTab = 'sertifiseringer'"
      >
        Sertifiseringer
      </button>
    </nav>

    <!-- Opplæringsmateriale -->
    <div v-if="activeTab === 'materiale'">
      <div class="card">
        <div v-if="loadingDocs" class="text-muted text-sm">Laster inn...</div>
        <template v-else>
          <div
            v-for="doc in trainingDocs"
            :key="doc.id"
            class="doc-row"
          >
            <span
              class="doc-icon"
              :style="{ backgroundColor: doc.colorBg, color: doc.colorText }"
            >
              {{ doc.type }}
            </span>
            <div class="flex-1 min-w-0">
              <div class="font-medium truncate">{{ doc.title }}</div>
              <div class="text-muted text-sm truncate">{{ doc.subtitle }}</div>
            </div>
            <a
              href="#"
              class="btn btn-ghost btn-sm"
              @click.prevent
            >
              {{ doc.actionLabel }}
            </a>
          </div>
        </template>
      </div>
    </div>

    <!-- Rutinebeskrivelser -->
    <div v-else-if="activeTab === 'rutiner'">
      <div class="card">
        <div class="card-header">
          <h2 class="section-title">Rutinebeskrivelser</h2>
        </div>
        <p class="text-muted mt-2">
          Rutinebeskrivelser kommer snart. Her vil du finne detaljerte beskrivelser av alle
          rutiner og prosedyrer knyttet til internkontroll.
        </p>
      </div>
    </div>

    <!-- Sertifiseringer -->
    <div v-else-if="activeTab === 'sertifiseringer'">
      <div class="card">
        <div v-if="loadingCerts" class="text-muted text-sm">Laster inn...</div>
        <template v-else>
          <div
            v-for="emp in certifications"
            :key="emp.id"
            class="status-row"
          >
            <div class="flex items-center gap-3 flex-1 min-w-0">
              <span
                class="avatar"
                :style="avatarStyle(emp.status)"
              >
                {{ initials(emp.name) }}
              </span>
              <span class="font-medium truncate">{{ emp.name }}</span>
            </div>
            <span
              class="badge"
              :class="certBadgeClass(emp.status)"
            >
              {{ certBadgeLabel(emp) }}
            </span>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { documentsService } from '@/services/documents.service'
import type { TrainingDocument, EmployeeCertification, CertificationStatus } from '@/types'

const activeTab = ref<'materiale' | 'rutiner' | 'sertifiseringer'>('materiale')

const trainingDocs = ref<TrainingDocument[]>([])
const certifications = ref<EmployeeCertification[]>([])
const loadingDocs = ref(false)
const loadingCerts = ref(false)

async function fetchDocs() {
  loadingDocs.value = true
  try {
    trainingDocs.value = await documentsService.getTrainingDocs()
  } finally {
    loadingDocs.value = false
  }
}

async function fetchCerts() {
  loadingCerts.value = true
  try {
    certifications.value = await documentsService.getCertifications()
  } finally {
    loadingCerts.value = false
  }
}

function initials(name: string): string {
  return name
    .split(' ')
    .map((n) => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2)
}

function avatarStyle(status: CertificationStatus): Record<string, string> {
  if (status === 'COMPLETE') return { backgroundColor: 'var(--c-success-bg)', color: 'var(--c-success-text)' }
  if (status === 'EXPIRING') return { backgroundColor: 'var(--c-warning-bg)', color: 'var(--c-warning-text)' }
  return { backgroundColor: 'var(--c-danger-bg)', color: 'var(--c-danger-text)' }
}

function certBadgeClass(status: CertificationStatus): string {
  if (status === 'COMPLETE') return 'badge-success'
  if (status === 'EXPIRING') return 'badge-warning'
  return 'badge-danger'
}

function certBadgeLabel(emp: EmployeeCertification): string {
  if (emp.status === 'COMPLETE') return 'Alt fullført'
  if (emp.status === 'EXPIRING') return `${emp.expiredCount} utløpt`
  return `${emp.missingCount} mangler`
}

onMounted(() => {
  fetchDocs()
  fetchCerts()
})
</script>
