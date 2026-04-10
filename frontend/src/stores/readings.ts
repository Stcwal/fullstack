import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { TemperatureReading, NewReading } from '@/types'
import { readingsService } from '@/services/readings.service'
import { useDashboardStore } from '@/stores/dashboard'

export const useReadingsStore = defineStore('readings', () => {
  const readings = ref<TemperatureReading[]>([])
  const loading = ref(false)
  const saving = ref(false)
  const error = ref<string | null>(null)

  async function fetchByUnit(unitId: number) {
    loading.value = true
    error.value = null
    try {
      readings.value = await readingsService.getByUnit(unitId)
    } catch {
      error.value = 'Kunne ikke laste målinger'
    } finally {
      loading.value = false
    }
  }

  async function addReading(data: NewReading): Promise<TemperatureReading> {
    saving.value = true
    try {
      const created = await readingsService.create(data)
      readings.value.unshift(created)
      if (created.isDeviation) {
        useDashboardStore().fetchDashboard()
      }
      return created
    } finally {
      saving.value = false
    }
  }

  function getByUnit(unitId: number): TemperatureReading[] {
    return readings.value.filter(r => r.unitId === unitId)
  }

  return { readings, loading, saving, error, fetchByUnit, addReading, getByUnit }
})
