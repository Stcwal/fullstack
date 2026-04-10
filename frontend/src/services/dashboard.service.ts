import type { DashboardStats, DashboardTask, DashboardAlert } from '@/types'
import api from './api'

export const dashboardService = {
  async get(locationId?: number | null): Promise<{ stats: DashboardStats; tasks: DashboardTask[]; alerts: DashboardAlert[] }> {
    const params: Record<string, number> = {}
    if (locationId != null) params.locationId = locationId
    const response = await api.get<{ stats: DashboardStats; tasks: DashboardTask[]; alerts: DashboardAlert[] }>('/dashboard', { params })
    return response.data
  }
}
