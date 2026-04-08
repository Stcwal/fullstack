import type { DashboardStats, DashboardTask, DashboardAlert } from '@/types'
import api from './api'

export const dashboardService = {
  async get(): Promise<{ stats: DashboardStats; tasks: DashboardTask[]; alerts: DashboardAlert[] }> {
    const response = await api.get<{ stats: DashboardStats; tasks: DashboardTask[]; alerts: DashboardAlert[] }>('/dashboard')
    return response.data
  }
}
