import type { Unit } from '@/types'
import api from './api'

export const unitsService = {
  async getAll(): Promise<Unit[]> {
    const res = await api.get<Unit[]>('/units')
    return res.data.map(u => ({ ...u, hasAlert: false }))
  },

  async create(data: Omit<Unit, 'id'>): Promise<Unit> {
    const res = await api.post<Unit>('/units', {
      name: data.name,
      type: data.type,
      targetTemperature: data.targetTemperature,
      minThreshold: data.minThreshold,
      maxThreshold: data.maxThreshold,
      description: data.description,
    })
    return { ...res.data, hasAlert: false }
  },

  async update(id: number, data: Partial<Unit>): Promise<Unit> {
    const res = await api.put<Unit>(`/units/${id}`, {
      name: data.name,
      type: data.type,
      targetTemperature: data.targetTemperature,
      minThreshold: data.minThreshold,
      maxThreshold: data.maxThreshold,
      description: data.description,
    })
    return { ...res.data, hasAlert: false }
  },

  async remove(id: number): Promise<void> {
    await api.delete(`/units/${id}`)
  }
}
