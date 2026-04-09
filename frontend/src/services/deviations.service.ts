import type { Deviation, DeviationComment, DeviationStatus, NewDeviation } from '@/types'
import api from './api'

export const deviationsService = {
  async getAll(): Promise<Deviation[]> {
    const response = await api.get<Deviation[]>('/deviations')
    return response.data
  },

  async getById(id: number): Promise<Deviation> {
    const response = await api.get<Deviation>(`/deviations/${id}`)
    return response.data
  },

  async create(data: NewDeviation): Promise<Deviation> {
    const response = await api.post<Deviation>('/deviations', data)
    return response.data
  },

  async updateStatus(id: number, status: DeviationStatus): Promise<Deviation> {
    const response = await api.patch<Deviation>(`/deviations/${id}/status`, { status })
    return response.data
  },

  async resolve(id: number, resolution: string): Promise<Deviation> {
    const response = await api.patch<Deviation>(`/deviations/${id}/resolve`, { resolution })
    return response.data
  },

  async addComment(id: number, text: string): Promise<DeviationComment> {
    const response = await api.post<DeviationComment>(`/deviations/${id}/comments`, { text })
    return response.data
  },
}
