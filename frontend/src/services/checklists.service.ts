import type { Checklist, ChecklistFrequency } from '@/types'
import api from './api'

export interface ChecklistTemplateItem {
  id: number
  text: string
}

export interface ChecklistTemplate {
  id: number
  title: string
  frequency: ChecklistFrequency
  items: ChecklistTemplateItem[]
}

export interface ChecklistTemplateUpsertRequest {
  title: string
  frequency: ChecklistFrequency
  itemTexts: string[]
}

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
  async getByFrequency(frequency: ChecklistFrequency, locationId?: number | null): Promise<Checklist[]> {
    const res = await api.get<BackendInstance[]>('/checklists/instances', {
      params: { frequency, ...(locationId != null && { locationId }) },
    })
    return res.data.map(mapInstance)
  },

  async toggleItem(checklistId: number, itemId: number, completed: boolean, performedByUserId?: number): Promise<void> {
    await api.patch(`/checklists/instances/${checklistId}/items/${itemId}`, {
      completed,
      ...(performedByUserId !== undefined && { performedByUserId }),
    })
  },

  async getTemplates(): Promise<ChecklistTemplate[]> {
    const res = await api.get<ChecklistTemplate[]>('/checklists/templates')
    return res.data
  },

  async createTemplate(data: ChecklistTemplateUpsertRequest): Promise<ChecklistTemplate> {
    const res = await api.post<ChecklistTemplate>('/checklists/templates', data)
    return res.data
  },

  async updateTemplate(id: number, data: ChecklistTemplateUpsertRequest): Promise<ChecklistTemplate> {
    const res = await api.put<ChecklistTemplate>(`/checklists/templates/${id}`, data)
    return res.data
  },

  async deleteTemplate(id: number): Promise<void> {
    await api.delete(`/checklists/templates/${id}`)
  },
}
