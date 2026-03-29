import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { DashboardStats, DashboardTask, DashboardAlert } from '@/types'
import { dashboardService } from '@/services/dashboard.service'

export const useDashboardStore = defineStore('dashboard', () => {
  const stats = ref<DashboardStats | null>(null)
  const tasks = ref<DashboardTask[]>([])
  const alerts = ref<DashboardAlert[]>([])
  const loading = ref(false)

  async function fetchDashboard() {
    loading.value = true
    try {
      const data = await dashboardService.get()
      stats.value = data.stats
      tasks.value = data.tasks
      alerts.value = data.alerts
    } finally {
      loading.value = false
    }
  }

  return { stats, tasks, alerts, loading, fetchDashboard }
})
