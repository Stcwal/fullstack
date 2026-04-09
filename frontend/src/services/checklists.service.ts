import type { Checklist, ChecklistFrequency } from '@/types'
import api from './api'

interface BackendItem {
  id: number
  text: string
  completed: boolean
  completedBy?: { id: number; name: string } | null
  completedAt?: string | null
}

interface BackendInstance {
  id: number
  title: string
  frequency: ChecklistFrequency
  items: BackendItem[]
}

function mapInstance(inst: BackendInstance): Checklist {
  return {
    id: inst.id,
    title: inst.title,
    frequency: inst.frequency,
    moduleType: 'IK_MAT',
    items: inst.items.map(item => ({
      id: item.id,
      text: item.text,
      completed: item.completed,
      completedBy: item.completedBy?.name,
      completedAt: item.completedAt ?? undefined,
    })),
  }
}

export const checklistsService = {
  async getByFrequency(frequency: ChecklistFrequency): Promise<Checklist[]> {
    const res = await api.get<BackendInstance[]>('/checklists/instances', {
      params: { frequency },
    })
    return res.data.map(mapInstance)
  },

  async toggleItem(checklistId: number, itemId: number, completed: boolean, performedByUserId?: number): Promise<void> {
    await api.patch(`/checklists/instances/${checklistId}/items/${itemId}`, {
      completed,
      ...(performedByUserId !== undefined && { performedByUserId }),
    })
  },
}
