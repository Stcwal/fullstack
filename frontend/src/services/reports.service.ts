import type { ChartPeriod } from '@/types'
import api from './api'

export interface ChartDataset {
  label: string
  data: (number | null)[]
  color: string
}

export interface ChartData {
  labels: string[]
  datasets: ChartDataset[]
  alerts: { index: number; unitName: string; value: number; status: 'OPEN' | 'RESOLVED' }[]
}

export const reportsService = {
  async getChartData(period: ChartPeriod): Promise<ChartData> {
    const response = await api.get<ChartData>(`/reports/chart?period=${period}`)
    return response.data
  },

  exportPdf(): void {
    // TODO: POST /api/reports/export/pdf — trigger download
    alert('PDF-eksport er ikke implementert ennå.')
  },

  exportJson(): void {
    // TODO: GET /api/reports/export/json — trigger download
    alert('JSON-eksport er ikke implementert ennå.')
  }
}
