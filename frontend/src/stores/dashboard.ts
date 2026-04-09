import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { DashboardStats, DashboardTask, DashboardAlert } from '@/types'
import { dashboardService } from '@/services/dashboard.service'
import api from '@/services/api'

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

  async function toggleTask(taskId: number) {
    const task = tasks.value.find(t => t.id === taskId)
    if (!task) return

    const completing = task.status !== 'COMPLETED'
    const prevStatus = task.status

    // Optimistic update
    task.status = completing ? 'COMPLETED' : 'NOT_STARTED'
    if (stats.value) stats.value.tasksCompleted += completing ? 1 : -1

    try {
      const res = await api.get<{ items: { id: number; completed: boolean }[] }>(`/checklists/instances/${taskId}`)
      const items = res.data.items
      await Promise.all(
        items.map((item) =>
          api.patch(`/checklists/instances/${taskId}/items/${item.id}`, { completed: completing })
        )
      )
    } catch {
      // Revert on failure
      task.status = prevStatus
      if (stats.value) stats.value.tasksCompleted += completing ? -1 : 1
    }
  }

  return { stats, tasks, alerts, loading, fetchDashboard, toggleTask }
})
