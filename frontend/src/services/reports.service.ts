// TODO: Implement report generation — GET /api/reports/export
import type { ChartPeriod } from '@/types'

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

function delay(ms = 300) { return new Promise(r => setTimeout(r, ms)) }

const WEEK_LABELS  = ['Tor 14', 'Fre 15', 'Lør 16', 'Søn 17', 'Man 18', 'Tir 19', 'Ons 20']
const MONTH_LABELS = Array.from({ length: 30 }, (_, i) => `${i + 1}`).slice(-30)

export const reportsService = {
  async getChartData(period: ChartPeriod): Promise<ChartData> {
    await delay()

    if (period === 'WEEK') {
      return {
        labels: WEEK_LABELS,
        datasets: [
          { label: 'Fryser #1',     data: [-18.4,-18.2,-18.1,-18.3,-18.5,-18.2,-18.4], color: '#3B82F6' },
          { label: 'Fryser #2',     data: [-18.3,-18.1,-17.9,-18.0,-17.8,-16.5,-12.1], color: '#6366F1' },
          { label: 'Kjøleskap #1',  data: [3.2, 3.5, 3.8, 5.2, 3.4, 3.1, 3.2],         color: '#16A34A' },
          { label: 'Visningskjøler',data: [4.8, 5.0, 4.9, 5.1, 5.0, 4.7, 5.1],         color: '#F59E0B' },
        ],
        alerts: [
          { index: 3, unitName: 'Kjøleskap #1', value: 5.2, status: 'RESOLVED' },
          { index: 6, unitName: 'Fryser #2',    value: -12.1, status: 'OPEN' }
        ]
      }
    }

    // 30-day mock
    const f1 = Array.from({ length: 30 }, () => -18 + (Math.random() - 0.5))
    const f2 = [...Array.from({ length: 27 }, () => -18 + (Math.random() - 0.5)), -16.5, -14.2, -12.1]
    const k1 = Array.from({ length: 30 }, () => 3 + Math.random())
    const vk = Array.from({ length: 30 }, () => 5 + (Math.random() - 0.5))

    return {
      labels: MONTH_LABELS,
      datasets: [
        { label: 'Fryser #1',     data: f1, color: '#3B82F6' },
        { label: 'Fryser #2',     data: f2, color: '#6366F1' },
        { label: 'Kjøleskap #1',  data: k1, color: '#16A34A' },
        { label: 'Visningskjøler',data: vk, color: '#F59E0B' },
      ],
      alerts: [
        { index: 27, unitName: 'Fryser #2', value: -16.5, status: 'RESOLVED' },
        { index: 29, unitName: 'Fryser #2', value: -12.1, status: 'OPEN' }
      ]
    }
    // Real: return (await api.get<ChartData>(`/reports/chart?period=${period}`)).data
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
