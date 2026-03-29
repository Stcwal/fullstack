// TODO: Replace mock with real API call — GET /api/dashboard
import type { DashboardStats, DashboardTask, DashboardAlert } from '@/types'

function delay(ms = 400) { return new Promise(r => setTimeout(r, ms)) }

export const dashboardService = {
  async get(): Promise<{ stats: DashboardStats; tasks: DashboardTask[]; alerts: DashboardAlert[] }> {
    await delay()
    return {
      stats: {
        tasksCompleted: 7,
        tasksTotal: 12,
        tempAlerts: 2,
        openDeviations: 3,
        compliancePercent: 87
      },
      tasks: [
        { id: 1, name: 'Morgen kjøkkenrenhold',         status: 'COMPLETED',   completedBy: 'Ola N.', completedAt: '07:45' },
        { id: 2, name: 'Temperatursjekk kjøleskap',     status: 'COMPLETED',   completedBy: 'Kari L.', completedAt: '08:15' },
        { id: 3, name: 'Logg varemottak-temperaturer',  status: 'PENDING' },
        { id: 4, name: 'Alderskontroll-logg (IK-Alkohol)', status: 'NOT_STARTED' },
        { id: 5, name: 'Kveldsstenging sjekkliste',     status: 'NOT_STARTED' },
      ],
      alerts: [
        { id: 1, message: 'Fryser #2 over grenseverdi — 15 min siden', type: 'danger', time: '08:10' },
        { id: 2, message: 'Ukentlig renholdssjekkliste forfalt — 2t siden', type: 'warning', time: '06:00' },
      ]
    }
    // Real: return (await api.get('/dashboard')).data
  }
}
