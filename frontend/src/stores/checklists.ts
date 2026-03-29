import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Checklist, ChecklistFrequency } from '@/types'
import { checklistsService } from '@/services/checklists.service'

export const useChecklistsStore = defineStore('checklists', () => {
  const checklists = ref<Checklist[]>([])
  const loading = ref(false)
  const activeFrequency = ref<ChecklistFrequency>('DAILY')

  async function fetchAll(frequency: ChecklistFrequency = 'DAILY') {
    loading.value = true
    activeFrequency.value = frequency
    try {
      checklists.value = await checklistsService.getByFrequency(frequency)
    } finally {
      loading.value = false
    }
  }

  async function toggleItem(checklistId: number, itemId: number) {
    const checklist = checklists.value.find(c => c.id === checklistId)
    if (!checklist) return
    const item = checklist.items.find(i => i.id === itemId)
    if (!item) return

    // Optimistic update
    item.completed = !item.completed

    try {
      // TODO: POST /api/checklists/:checklistId/items/:itemId/toggle
      await checklistsService.toggleItem(checklistId, itemId, item.completed)
    } catch {
      // Revert on error
      item.completed = !item.completed
    }
  }

  return { checklists, loading, activeFrequency, fetchAll, toggleItem }
})
