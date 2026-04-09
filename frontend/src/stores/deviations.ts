import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Deviation, DeviationComment, DeviationStatus, NewDeviation } from '@/types'
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

  async function updateStatus(id: number, status: DeviationStatus): Promise<void> {
    const updated = await deviationsService.updateStatus(id, status)
    const idx = deviations.value.findIndex(d => d.id === id)
    if (idx !== -1) deviations.value[idx] = { ...deviations.value[idx], ...updated }
  }

  async function resolve(id: number, resolution: string): Promise<void> {
    const updated = await deviationsService.resolve(id, resolution)
    const idx = deviations.value.findIndex(d => d.id === id)
    if (idx !== -1) deviations.value[idx] = { ...deviations.value[idx], ...updated }
  }

  async function addComment(deviationId: number, text: string): Promise<DeviationComment> {
    return await deviationsService.addComment(deviationId, text)
  }

  const openCount = () => deviations.value.filter(d => d.status !== 'RESOLVED').length

  return { deviations, loading, saving, fetchAll, report, updateStatus, resolve, addComment, openCount }
})
