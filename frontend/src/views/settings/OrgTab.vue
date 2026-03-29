<template>
  <div class="flex flex-col gap-4">
    <!-- Organisasjonsdetaljer -->
    <div class="card">
      <div class="card-header mb-4">
        <h2 class="section-title">Organisasjonsdetaljer</h2>
      </div>

      <div v-if="loading" class="text-muted text-sm">Laster inn...</div>
      <template v-else-if="orgForm">
        <div class="form-group">
          <label class="font-medium text-sm">Bedriftsnavn</label>
          <input
            v-model="orgForm.name"
            type="text"
            class="form-control"
            placeholder="Restaurantnavn AS"
          />
        </div>

        <div class="form-grid-2 mt-3">
          <div class="form-group">
            <label class="font-medium text-sm">Org.nummer</label>
            <input
              v-model="orgForm.orgNumber"
              type="text"
              class="form-control"
              placeholder="123 456 789"
            />
          </div>
          <div class="form-group">
            <label class="font-medium text-sm">Bransje</label>
            <select v-model="orgForm.industry" class="form-control">
              <option value="Restaurant">Restaurant</option>
              <option value="Bar">Bar</option>
              <option value="Kafé">Kafé</option>
              <option value="Kantine">Kantine</option>
            </select>
          </div>
        </div>

        <div class="form-group mt-3">
          <label class="font-medium text-sm">Adresse</label>
          <input
            v-model="orgForm.address"
            type="text"
            class="form-control"
            placeholder="Gateadresse 1, 0000 By"
          />
        </div>

        <div v-if="savedOrg" class="text-success font-medium text-sm mt-3">Lagret!</div>

        <div class="flex justify-end mt-4">
          <button class="btn btn-primary" @click="saveOrg">Lagre</button>
        </div>
      </template>
    </div>

    <!-- Aktive moduler -->
    <div class="card">
      <div class="card-header mb-4">
        <h2 class="section-title">Aktive moduler</h2>
      </div>

      <template v-if="orgForm">
        <div class="status-row">
          <div class="flex items-center gap-3 flex-1">
            <input
              id="module-ik-mat"
              v-model="orgForm.modules.ikMat"
              type="checkbox"
            />
            <div>
              <label for="module-ik-mat" class="font-medium flex items-center gap-2">
                <span class="mod-badge ik-mat">IK-Mat</span>
                IK-Mat
              </label>
              <p class="text-muted text-sm mt-1">
                Internkontroll for næringsmiddelhygiene – temperaturlogging, rengjøring og
                sjekklister.
              </p>
            </div>
          </div>
        </div>

        <div class="divider" />

        <div class="status-row">
          <div class="flex items-center gap-3 flex-1">
            <input
              id="module-ik-alkohol"
              v-model="orgForm.modules.ikAlkohol"
              type="checkbox"
            />
            <div>
              <label for="module-ik-alkohol" class="font-medium flex items-center gap-2">
                <span class="mod-badge ik-alkohol">IK-Alkohol</span>
                IK-Alkohol
              </label>
              <p class="text-muted text-sm mt-1">
                Internkontroll for skjenkebevilgning – alderskontroll, opplæring og
                bevillingslogg.
              </p>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- Varslingsinnstillinger -->
    <div class="card">
      <div class="card-header mb-4">
        <h2 class="section-title">Varslingsinnstillinger</h2>
      </div>

      <template v-if="orgForm">
        <div class="flex flex-col gap-2">
          <label class="checklist-item">
            <input
              v-model="orgForm.notifications.emailOnTempDeviation"
              type="checkbox"
            />
            E-postvarsling ved temperaturavvik
          </label>
          <label class="checklist-item">
            <input
              v-model="orgForm.notifications.dailySummaryToManagers"
              type="checkbox"
            />
            Daglig oppsummering til ledere
          </label>
          <label class="checklist-item">
            <input
              v-model="orgForm.notifications.smsOnCritical"
              type="checkbox"
            />
            SMS-varsling ved kritiske avvik
          </label>
        </div>

        <div v-if="savedNotifications" class="text-success font-medium text-sm mt-3">Lagret!</div>

        <div class="flex justify-end mt-4">
          <button class="btn btn-primary" @click="saveNotifications">
            Lagre innstillinger
          </button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { organizationService } from '@/services/organization.service'
import type { Organization } from '@/types'

const loading = ref(false)
const orgForm = ref<Organization | null>(null)
const savedOrg = ref(false)
const savedNotifications = ref(false)

async function fetchOrg() {
  loading.value = true
  try {
    orgForm.value = await organizationService.getOrg()
  } finally {
    loading.value = false
  }
}

async function saveOrg() {
  if (!orgForm.value) return
  await organizationService.updateOrg(orgForm.value)
  savedOrg.value = true
  setTimeout(() => {
    savedOrg.value = false
  }, 3000)
}

async function saveNotifications() {
  if (!orgForm.value) return
  await organizationService.updateOrg(orgForm.value)
  savedNotifications.value = true
  setTimeout(() => {
    savedNotifications.value = false
  }, 3000)
}

onMounted(fetchOrg)
</script>
