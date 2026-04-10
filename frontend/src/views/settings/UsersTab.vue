<template>
  <div>
    <div class="flex items-center justify-between mb-4">
      <h2 class="section-title">Brukere</h2>
      <button class="btn btn-primary btn-sm" @click="openAddModal">
        + Legg til ny bruker
      </button>
    </div>

    <div class="card">
      <div v-if="loading" class="text-muted text-sm">Laster inn...</div>
      <template v-else>
        <div
          v-for="user in users"
          :key="user.id"
          class="status-row"
        >
          <div class="flex items-center gap-3 flex-1 min-w-0">
            <span
              class="avatar"
              :style="{ backgroundColor: user.colorBg, color: user.colorText }"
            >
              {{ initials(user.firstName, user.lastName) }}
            </span>
            <div class="min-w-0">
              <div class="font-medium truncate">{{ user.firstName }} {{ user.lastName }}</div>
              <div class="text-muted text-sm truncate">{{ user.email }}</div>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <span class="badge" :class="roleBadgeClass(user.role)">
              {{ roleLabel(user.role) }}
            </span>
            <button
              class="btn btn-ghost btn-sm"
              style="color: #2563EB"
              @click="openEditModal(user)"
            >
              Rediger
            </button>
          </div>
        </div>
        <div v-if="users.length === 0" class="text-muted text-sm">
          Ingen brukere registrert.
        </div>
      </template>
    </div>

    <AppModal
      :show="showModal"
      :title="isEditing ? 'Rediger bruker' : 'Legg til ny bruker'"
      @close="showModal = false"
    >
      <div class="form-grid-2">
        <div class="form-group">
          <label class="font-medium text-sm">Fornavn</label>
          <input v-model="form.firstName" type="text" class="form-control" placeholder="Fornavn" />
        </div>
        <div class="form-group">
          <label class="font-medium text-sm">Etternavn</label>
          <input v-model="form.lastName" type="text" class="form-control" placeholder="Etternavn" />
        </div>
      </div>

      <div class="form-group mt-3">
        <label class="font-medium text-sm">E-post</label>
        <input v-model="form.email" type="email" class="form-control" placeholder="epost@eksempel.no" />
      </div>

      <div class="form-grid-2 mt-3">
        <div class="form-group">
          <label class="font-medium text-sm">Rolle</label>
          <select v-model="form.role" class="form-control">
            <option value="ADMIN">Admin</option>
            <option value="SUPERVISOR">Veileder</option>
            <option value="MANAGER">Leder</option>
            <option value="STAFF">Ansatt</option>
          </select>
        </div>
        <div class="form-group">
          <label class="font-medium text-sm">Status</label>
          <select v-model="form.isActive" class="form-control">
            <option :value="true">Aktiv</option>
            <option :value="false">Inaktiv</option>
          </select>
        </div>
      </div>

      <div
        v-if="(form.role === 'MANAGER' || form.role === 'STAFF') && form.role !== originalRole"
        class="form-group mt-3"
      >
        <label class="font-medium text-sm">Lokasjon</label>
        <select v-model="form.locationId" class="form-control">
          <option :value="null" disabled>Velg lokasjon</option>
          <option v-for="loc in locations" :key="loc.id" :value="loc.id">{{ loc.name }}</option>
        </select>
      </div>

      <div class="divider mt-4 mb-3" />
      <p class="font-semibold text-sm mb-3">Tillatelser</p>

      <div class="flex flex-col gap-2">
        <label class="checklist-item">
          <input v-model="form.permissions.temperatureLogging" type="checkbox" />
          Temperaturlogging
        </label>
        <label class="checklist-item">
          <input v-model="form.permissions.checklists" type="checkbox" />
          Sjekklister
        </label>
        <label class="checklist-item">
          <input v-model="form.permissions.reports" type="checkbox" />
          Rapporter
        </label>
        <label class="checklist-item">
          <input v-model="form.permissions.deviations" type="checkbox" />
          Avviksrapportering
        </label>
        <label class="checklist-item">
          <input v-model="form.permissions.userAdmin" type="checkbox" />
          Brukeradministrasjon
        </label>
        <label class="checklist-item">
          <input v-model="form.permissions.settings" type="checkbox" />
          Innstillinger
        </label>
      </div>

      <template #footer>
        <button
          class="btn btn-ghost btn-sm"
          style="margin-right: auto"
          @click="resetPassword"
        >
          Tilbakestill passord
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
import type { SettingsUser, UserRole } from '@/types'

const users = ref<SettingsUser[]>([])
const locations = ref<{ id: number; name: string }[]>([])
const loading = ref(false)
const showModal = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const originalRole = ref<UserRole | null>(null)
const saveError = ref<string | null>(null)

type FormData = Omit<SettingsUser, 'id' | 'colorBg' | 'colorText'> & { locationId: number | null }

const emptyForm = (): FormData => ({
  firstName: '',
  lastName: '',
  email: '',
  role: 'STAFF',
  locationId: null,
  isActive: true,
  permissions: {
    temperatureLogging: false,
    checklists: false,
    reports: false,
    deviations: false,
    userAdmin: false,
    settings: false,
  },
})

const form = reactive<FormData>(emptyForm())

async function fetchUsers() {
  loading.value = true
  try {
    ;[users.value, locations.value] = await Promise.all([
      organizationService.getUsers(),
      organizationService.getLocations(),
    ])
  } finally {
    loading.value = false
  }
}

function initials(firstName: string, lastName: string): string {
  return `${firstName[0] ?? ''}${lastName[0] ?? ''}`.toUpperCase()
}

function roleBadgeClass(role: UserRole): string {
  if (role === 'ADMIN') return 'badge-purple'
  if (role === 'SUPERVISOR') return 'badge-info'
  if (role === 'MANAGER') return 'badge-info'
  return 'badge-neutral'
}

function roleLabel(role: UserRole): string {
  if (role === 'ADMIN') return 'Admin'
  if (role === 'SUPERVISOR') return 'Veileder'
  if (role === 'MANAGER') return 'Leder'
  return 'Ansatt'
}

function getAvatarColors(role: UserRole): { bg: string; text: string } {
  const roleColors: Record<UserRole, { bg: string; text: string }> = {
    ADMIN: { bg: '#A855F7', text: '#FFFFFF' },
    SUPERVISOR: { bg: '#3B82F6', text: '#FFFFFF' },
    MANAGER: { bg: '#3B82F6', text: '#FFFFFF' },
    STAFF: { bg: '#6B7280', text: '#FFFFFF' },
  }
  return roleColors[role]
}

function openAddModal() {
  isEditing.value = false
  editingId.value = null
  saveError.value = null
  originalRole.value = null
  Object.assign(form, emptyForm())
  showModal.value = true
}

function openEditModal(user: SettingsUser) {
  isEditing.value = true
  editingId.value = user.id
  saveError.value = null
  originalRole.value = user.role
  Object.assign(form, {
    firstName: user.firstName,
    lastName: user.lastName,
    email: user.email,
    role: user.role,
    isActive: user.isActive,
    permissions: { ...user.permissions },
    locationId: null,
  })
  showModal.value = true
}

async function save() {
  saveError.value = null
  const needsLocation = form.role === 'MANAGER' || form.role === 'STAFF'
  const roleChanged = form.role !== originalRole.value
  if (needsLocation && (roleChanged || !isEditing.value) && !form.locationId) {
    saveError.value = 'Velg en lokasjon for denne rollen.'
    return
  }

  const colors = getAvatarColors(form.role)
  const userData: Partial<SettingsUser> & { colorBg: string; colorText: string; locationId?: number | null } = {
    ...form,
    colorBg: colors.bg,
    colorText: colors.text,
  }
  // Only send role if it actually changed — role endpoint returns 403 otherwise
  if (isEditing.value && !roleChanged) {
    delete userData.role
  }

  try {
    if (isEditing.value && editingId.value !== null) {
      const updated = await organizationService.updateUser(editingId.value, userData)
      const idx = users.value.findIndex((u) => u.id === editingId.value)
      if (idx !== -1) users.value[idx] = { ...users.value[idx], ...updated }
    } else {
      const created = await organizationService.createUser(userData)
      users.value.push(created)
    }
    showModal.value = false
  } catch (e: unknown) {
    saveError.value = e instanceof Error ? e.message : 'Noe gikk galt. Prøv igjen.'
  }
}

function resetPassword() {
  alert('Ikke implementert ennå')
}

onMounted(fetchUsers)
</script>
