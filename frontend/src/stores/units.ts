import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Unit, UnitType } from '@/types'
import { unitsService } from '@/services/units.service'

export const useUnitsStore = defineStore('units', () => {
  const units = ref<Unit[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function fetchUnits() {
    loading.value = true
    error.value = null
    try {
      units.value = await unitsService.getAll()
    } catch (e) {
      error.value = 'Kunne ikke laste enheter'
    } finally {
      loading.value = false
    }
  }

  async function createUnit(data: Omit<Unit, 'id'>) {
    const created = await unitsService.create(data)
    units.value.push(created)
    return created
  }

  async function updateUnit(id: number, data: Partial<Unit>) {
    const updated = await unitsService.update(id, data)
    const idx = units.value.findIndex(u => u.id === id)
    if (idx !== -1) units.value[idx] = updated
    return updated
  }

  async function deleteUnit(id: number) {
    await unitsService.remove(id)
    units.value = units.value.filter(u => u.id !== id)
  }

  function getByType(type: UnitType) {
    return units.value.filter(u => u.type === type && u.active)
  }

  return { units, loading, error, fetchUnits, createUnit, updateUnit, deleteUnit, getByType }
})
