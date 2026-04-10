<template>
  <div>
    <div class="flex items-center justify-between mb-4">
      <h2 class="section-title">Lokasjoner</h2>
      <button class="btn btn-primary btn-sm" @click="openAddModal">+ Legg til lokasjon</button>
    </div>

    <div class="card">
      <div v-if="loading" class="text-muted text-sm">Laster inn...</div>
      <template v-else>
        <div v-for="loc in locations" :key="loc.id" class="status-row">
          <div class="flex items-center gap-3 flex-1 min-w-0">
            <span class="loc-icon" aria-hidden="true">
              <svg width="16" height="16" fill="none" viewBox="0 0 24 24"><path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5A2.5 2.5 0 1 1 12 6.5a2.5 2.5 0 0 1 0 5z" fill="currentColor"/></svg>
            </span>
            <div class="min-w-0">
              <div class="font-medium truncate">{{ loc.name }}</div>
              <div class="text-muted text-sm truncate">{{ loc.address }}</div>
            </div>
          </div>
          <button class="btn btn-ghost btn-sm" style="color:#2563EB" @click="openEditModal(loc)">
            Rediger
          </button>
        </div>
        <div v-if="locations.length === 0" class="text-muted text-sm">
          Ingen lokasjoner registrert.
        </div>
      </template>
    </div>

    <AppModal
      :show="showModal"
      :title="isEditing ? 'Rediger lokasjon' : 'Legg til lokasjon'"
      @close="showModal = false"
    >
      <div class="form-group">
        <label class="font-medium text-sm">Navn</label>
        <input v-model="form.name" type="text" class="form-control" placeholder="f.eks. Innherredsveien" />
      </div>
      <div class="form-group mt-3">
        <label class="font-medium text-sm">Adresse</label>
        <input v-model="form.address" type="text" class="form-control" placeholder="Gateadresse 1, 0000 By" />
      </div>

      <template #footer>
        <button
          v-if="isEditing"
          class="btn btn-ghost btn-sm"
          style="margin-right:auto;color:#DC2626"
          @click="remove"
        >
          Slett lokasjon
        </button>
        <span v-if="saveError" class="text-sm text-red-500 mr-auto">{{ saveError }}</span>
        <button class="btn btn-secondary" @click="showModal = false">Avbryt</button>
        <button class="btn btn-primary" @click="save">Lagre</button>
      </template>
    </AppModal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import AppModal from '@/components/AppModal.vue'
import { organizationService } from '@/services/organization.service'

interface Location { id: number; name: string; address: string }

const locations = ref<Location[]>([])
const loading = ref(false)
const showModal = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const saveError = ref<string | null>(null)

const form = reactive({ name: '', address: '' })

async function fetchLocations() {
  loading.value = true
  try {
    locations.value = await organizationService.getLocations()
  } finally {
    loading.value = false
  }
}

function openAddModal() {
  isEditing.value = false
  editingId.value = null
  saveError.value = null
  form.name = ''
  form.address = ''
  showModal.value = true
}

function openEditModal(loc: Location) {
  isEditing.value = true
  editingId.value = loc.id
  saveError.value = null
  form.name = loc.name
  form.address = loc.address
  showModal.value = true
}

async function save() {
  saveError.value = null
  if (!form.name.trim()) { saveError.value = 'Navn er påkrevd.'; return }
  try {
    if (isEditing.value && editingId.value !== null) {
      const updated = await organizationService.updateLocation(editingId.value, form)
      const idx = locations.value.findIndex(l => l.id === editingId.value)
      if (idx !== -1) locations.value[idx] = updated
    } else {
      const created = await organizationService.createLocation(form)
      locations.value.push(created)
    }
    showModal.value = false
  } catch {
    saveError.value = 'Noe gikk galt. Prøv igjen.'
  }
}

async function remove() {
  if (editingId.value === null) return
  try {
    await organizationService.deleteLocation(editingId.value)
    locations.value = locations.value.filter(l => l.id !== editingId.value)
    showModal.value = false
  } catch {
    saveError.value = 'Kunne ikke slette lokasjon. Prøv igjen.'
  }
}

onMounted(fetchLocations)
</script>

<style scoped>
.loc-icon {
  width: 32px;
  height: 32px;
  border-radius: var(--r-md);
  background: color-mix(in srgb, var(--c-primary) 10%, transparent);
  color: var(--c-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
</style>
