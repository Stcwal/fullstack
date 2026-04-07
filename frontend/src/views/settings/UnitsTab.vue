<template>
  <div>
    <div class="flex items-center justify-between mb-4">
      <h2 class="section-title">Enheter</h2>
      <button class="btn btn-primary btn-sm" @click="openAddModal">
        + Legg til ny enhet
      </button>
    </div>

    <div class="card">
      <div v-if="loading" class="text-muted text-sm">Laster inn...</div>
      <template v-else>
        <div
          v-for="unit in units"
          :key="unit.id"
          class="status-row"
        >
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2">
              <span class="font-medium truncate">{{ unit.name }}</span>
              <span v-if="!unit.active" class="badge badge-neutral">Inaktiv</span>
            </div>
            <div class="text-muted text-sm mt-1">
              {{ unitTypeLabel(unit.type) }} &middot;
              Måltemp: {{ unit.targetTemp }}&deg;C &middot;
              Min/maks: {{ unit.minTemp }}&deg;C / {{ unit.maxTemp }}&deg;C
            </div>
          </div>
          <div class="flex items-center gap-3">
            <button
              class="btn btn-ghost btn-sm"
              style="color: #2563EB"
              @click="openEditModal(unit)"
            >
              Rediger
            </button>
            <button
              class="btn btn-ghost btn-sm text-danger"
              @click="deleteUnit(unit)"
            >
              Slett
            </button>
          </div>
        </div>
        <div v-if="units.length === 0" class="text-muted text-sm">
          Ingen enheter registrert.
        </div>
      </template>
    </div>

    <AppModal
      :show="showModal"
      :title="isEditing ? 'Rediger enhet' : 'Legg til ny enhet'"
      @close="showModal = false"
    >
      <div class="form-group">
        <label class="font-medium text-sm">Navn</label>
        <input
          v-model="form.name"
          type="text"
          class="form-control"
          placeholder="Enhetsnavn"
        />
      </div>

      <div class="form-group mt-3">
        <label class="font-medium text-sm">Type</label>
        <select v-model="form.type" class="form-control">
          <option value="FREEZER">Fryser</option>
          <option value="FRIDGE">Kjøleskap</option>
          <option value="COOLER">Kjøler</option>
          <option value="DISPLAY">Visningsenhet</option>
          <option value="OTHER">Annet</option>
        </select>
      </div>

      <div class="form-grid-3 mt-3">
        <div class="form-group">
          <label class="font-medium text-sm">Måltemperatur (&deg;C)</label>
          <input
            v-model.number="form.targetTemp"
            type="number"
            step="0.1"
            class="form-control"
          />
        </div>
        <div class="form-group">
          <label class="font-medium text-sm">Minimumstemperatur (&deg;C)</label>
          <input
            v-model.number="form.minTemp"
            type="number"
            step="0.1"
            class="form-control"
          />
        </div>
        <div class="form-group">
          <label class="font-medium text-sm">Maksimumstemperatur (&deg;C)</label>
          <input
            v-model.number="form.maxTemp"
            type="number"
            step="0.1"
            class="form-control"
          />
        </div>
      </div>

      <div class="form-group mt-3">
        <label class="font-medium text-sm">Innhold</label>
        <textarea
          v-model="form.contents"
          class="form-control"
          rows="1"
          placeholder="Hva lagres i enheten?"
        />
      </div>

      <div class="flex items-center gap-2 mt-3">
        <input
          id="unit-active"
          v-model="form.active"
          type="checkbox"
        />
        <label for="unit-active" class="font-medium text-sm">Aktiv</label>
      </div>

      <template #footer>
        <button class="btn btn-secondary" @click="showModal = false">Avbryt</button>
        <button class="btn btn-primary" @click="save">Lagre</button>
      </template>
    </AppModal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import AppModal from '@/components/AppModal.vue'
import { unitsService } from '@/services/units.service'
import type { SettingsUnit, UnitType } from '@/types'

const units = ref<SettingsUnit[]>([])
const loading = ref(false)
const showModal = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)

const emptyForm = (): Omit<SettingsUnit, 'id'> => ({
  name: '',
  type: 'FRIDGE',
  targetTemp: 4,
  minTemp: 0,
  maxTemp: 8,
  contents: '',
  active: true,
})

const form = reactive<Omit<SettingsUnit, 'id'>>(emptyForm())

async function fetchUnits() {
  loading.value = true
  try {
    units.value = await unitsService.getAll()
  } finally {
    loading.value = false
  }
}

function unitTypeLabel(type: UnitType): string {
  const labels: Record<UnitType, string> = {
    FREEZER: 'Fryser',
    FRIDGE: 'Kjøleskap',
    COOLER: 'Kjøler',
    DISPLAY: 'Visningsenhet',
    OTHER: 'Annet',
  }
  return labels[type]
}

function openAddModal() {
  isEditing.value = false
  editingId.value = null
  Object.assign(form, emptyForm())
  showModal.value = true
}

function openEditModal(unit: SettingsUnit) {
  isEditing.value = true
  editingId.value = unit.id
  Object.assign(form, {
    name: unit.name,
    type: unit.type,
    targetTemp: unit.targetTemp,
    minTemp: unit.minTemp,
    maxTemp: unit.maxTemp,
    contents: unit.contents,
    active: unit.active,
  })
  showModal.value = true
}

async function save() {
  if (isEditing.value && editingId.value !== null) {
    const updated = await unitsService.update(editingId.value, { ...form })
    const idx = units.value.findIndex((u) => u.id === editingId.value)
    if (idx !== -1) units.value[idx] = updated
  } else {
    const created = await unitsService.create({ ...form })
    units.value.push(created)
  }
  showModal.value = false
}

async function deleteUnit(unit: SettingsUnit) {
  if (!window.confirm(`Er du sikker på at du vil slette "${unit.name}"?`)) return
  await unitsService.remove(unit.id)
  units.value = units.value.filter((u) => u.id !== unit.id)
}

onMounted(fetchUnits)
</script>
