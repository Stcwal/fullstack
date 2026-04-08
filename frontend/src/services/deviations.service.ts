import type { Deviation, NewDeviation } from '@/types'
import api from './api'

export const deviationsService = {
  async getAll(): Promise<Deviation[]> {
    const response = await api.get<Deviation[]>('/deviations')
    return response.data
  },

  async create(data: NewDeviation): Promise<Deviation> {
    const response = await api.post<Deviation>('/deviations', data)
    return response.data
  },

  async resolve(id: number, resolution: string): Promise<void> {
    await api.patch(`/deviations/${id}/resolve`, { resolution })
  }
}
