import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Deviation, NewDeviation } from '@/types'
import { deviationsService } from '@/services/deviations.service'

export const useDeviationsStore = defineStore('deviations', () => {
  const deviations = ref<Deviation[]>([])
  const loading = ref(false)
  const saving = ref(false)

  async function fetchAll() {
    loading.value = true
    try {
      deviations.value = await deviationsService.getAll()
    } finally {
      loading.value = false
    }
  }

  async function report(data: NewDeviation): Promise<Deviation> {
    saving.value = true
    try {
      const created = await deviationsService.create(data)
      deviations.value.unshift(created)
      return created
    } finally {
      saving.value = false
    }
  }

  async function resolve(id: number, resolution: string): Promise<void> {
    await deviationsService.resolve(id, resolution)
    const dev = deviations.value.find(d => d.id === id)
    if (dev) {
      dev.status = 'RESOLVED'
      dev.resolution = resolution
      dev.resolvedAt = new Date().toISOString()
    }
  }

  const openCount = () => deviations.value.filter(d => d.status !== 'RESOLVED').length

  return { deviations, loading, saving, fetchAll, report, resolve, openCount }
})
