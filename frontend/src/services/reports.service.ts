import type { ChartPeriod } from '@/types'
import api from './api'

export interface ChartDataset {
  label: string
  data: (number | null)[]
  color: string
  minThreshold: number
  maxThreshold: number
  unitType: string
}

export interface ChartData {
  labels: string[]
  datasets: ChartDataset[]
  alerts: { index: number; unitName: string; value: number; status: 'OPEN' | 'RESOLVED' }[]
}

function triggerDownload(blob: Blob, filename: string): void {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

export const reportsService = {
  async getChartData(period: ChartPeriod, locationId?: number | null): Promise<ChartData> {
    const params: Record<string, string | number> = { period }
    if (locationId != null) params.locationId = locationId
    const response = await api.get<ChartData>('/reports/chart', { params })
    return response.data
  },

  async exportPdf(from: string, to: string): Promise<void> {
    const response = await api.post('/export', { module: 'TEMPERATURE_LOGS', format: 'PDF', from, to }, { responseType: 'blob' })
    triggerDownload(response.data as Blob, `temperatur-rapport-${to}.pdf`)
  },

  async exportJson(from: string, to: string): Promise<void> {
    const response = await api.post('/export', { module: 'TEMPERATURE_LOGS', format: 'JSON', from, to }, { responseType: 'blob' })
    triggerDownload(response.data as Blob, `temperatur-rapport-${to}.json`)
  },
}
