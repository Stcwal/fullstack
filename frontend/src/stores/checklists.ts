import { defineStore } from 'pinia'
import { ref, watch } from 'vue'
import type { Checklist, ChecklistFrequency } from '@/types'
import { checklistsService } from '@/services/checklists.service'
import { useLocationStore } from '@/stores/location'

export const useChecklistsStore = defineStore('checklists', () => {
  const checklists = ref<Checklist[]>([])
  const loading = ref(false)
  const activeFrequency = ref<ChecklistFrequency>('DAILY')

  const locationStore = useLocationStore()

  async function fetchAll(frequency: ChecklistFrequency = 'DAILY') {
    loading.value = true
    activeFrequency.value = frequency
    try {
      checklists.value = await checklistsService.getByFrequency(frequency, locationStore.activeLocationId)
    } finally {
      loading.value = false
    }
  }

  watch(() => locationStore.activeLocationId, () => { fetchAll(activeFrequency.value) })

  async function toggleItem(checklistId: number, itemId: number, performedByUserId?: number) {
    const checklist = checklists.value.find(c => c.id === checklistId)
    if (!checklist) return
    const item = checklist.items.find(i => i.id === itemId)
    if (!item) return

    // Optimistic update
    item.completed = !item.completed

    try {
      await checklistsService.toggleItem(checklistId, itemId, item.completed, performedByUserId)
    } catch {
      // Revert on error
      item.completed = !item.completed
    }
  }

  return { checklists, loading, activeFrequency, fetchAll, toggleItem }
})
