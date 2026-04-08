import type { TemperatureReading, NewReading } from '@/types'
import api from './api'

export const readingsService = {
  async getByUnit(unitId: number): Promise<TemperatureReading[]> {
    const response = await api.get<TemperatureReading[]>(`/units/${unitId}/readings`)
    return response.data
  },

  async create(data: NewReading): Promise<TemperatureReading> {
    const { unitId, ...body } = data
    const response = await api.post<TemperatureReading>(`/units/${unitId}/readings`, body)
    return response.data
  }
}
