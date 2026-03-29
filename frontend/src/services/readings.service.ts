// TODO: Replace mock with real API calls — /api/readings
import type { TemperatureReading, NewReading } from '@/types'

const mockReadings: TemperatureReading[] = [
  // Fryser #1 (id 1)
  { id: 1,  unitId: 1, temperature: -18.4, recordedAt: today('08:15'), recordedBy: 'Kari Larsen', isOutOfRange: false },
  { id: 2,  unitId: 1, temperature: -18.2, recordedAt: yesterday('20:00'), recordedBy: 'Per Martinsen', isOutOfRange: false },
  { id: 3,  unitId: 1, temperature: -18.5, recordedAt: daysAgo(2, '08:10'), recordedBy: 'Ola Nordmann', isOutOfRange: false },
  // Fryser #2 (id 2) — has alert
  { id: 4,  unitId: 2, temperature: -12.1, recordedAt: today('08:10'), recordedBy: 'Kari Larsen', note: 'Dør sto åpen', isOutOfRange: true },
  { id: 5,  unitId: 2, temperature: -18.3, recordedAt: yesterday('20:00'), recordedBy: 'Per Martinsen', isOutOfRange: false },
  // Kjøleskap #1 (id 3)
  { id: 6,  unitId: 3, temperature: 3.2, recordedAt: today('08:15'), recordedBy: 'Kari Larsen', isOutOfRange: false },
  { id: 7,  unitId: 3, temperature: 3.8, recordedAt: yesterday('20:00'), recordedBy: 'Per Martinsen', isOutOfRange: false },
  // Kjøleskap #2 (id 4)
  { id: 8,  unitId: 4, temperature: 3.5, recordedAt: today('08:15'), recordedBy: 'Kari Larsen', isOutOfRange: false },
  { id: 9,  unitId: 4, temperature: 3.1, recordedAt: yesterday('20:00'), recordedBy: 'Ola Nordmann', isOutOfRange: false },
  // Visningskjøler (id 5)
  { id: 10, unitId: 5, temperature: 5.1, recordedAt: today('08:15'), recordedBy: 'Kari Larsen', isOutOfRange: false },
  { id: 11, unitId: 5, temperature: 4.9, recordedAt: yesterday('20:00'), recordedBy: 'Per Martinsen', isOutOfRange: false },
]

let nextId = 100

function delay(ms = 300) { return new Promise(r => setTimeout(r, ms)) }
function today(time: string) { return new Date().toISOString().split('T')[0] + 'T' + time + ':00' }
function yesterday(time: string) { const d = new Date(); d.setDate(d.getDate()-1); return d.toISOString().split('T')[0]+'T'+time+':00' }
function daysAgo(n: number, time: string) { const d = new Date(); d.setDate(d.getDate()-n); return d.toISOString().split('T')[0]+'T'+time+':00' }

export const readingsService = {
  async getByUnit(unitId: number): Promise<TemperatureReading[]> {
    await delay()
    return mockReadings.filter(r => r.unitId === unitId).sort(
      (a, b) => new Date(b.recordedAt).getTime() - new Date(a.recordedAt).getTime()
    )
    // Real: return (await api.get<TemperatureReading[]>(`/readings?unitId=${unitId}`)).data
  },

  async create(data: NewReading): Promise<TemperatureReading> {
    await delay()
    const user = JSON.parse(sessionStorage.getItem('user') || '{}')
    const unitMap: Record<number, {min:number, max:number}> = {
      1: {min:-22,max:-16}, 2: {min:-22,max:-16},
      3: {min:1,max:4},     4: {min:1,max:4},
      5: {min:3,max:6}
    }
    const limits = unitMap[data.unitId] ?? { min: -999, max: 999 }
    const isOutOfRange = data.temperature < limits.min || data.temperature > limits.max
    const reading: TemperatureReading = {
      id: nextId++,
      unitId: data.unitId,
      temperature: data.temperature,
      recordedAt: data.recordedAt,
      recordedBy: user.firstName ? `${user.firstName} ${user.lastName}` : 'Ukjent',
      note: data.note,
      isOutOfRange
    }
    mockReadings.unshift(reading)
    return reading
    // Real: return (await api.post<TemperatureReading>('/readings', data)).data
  }
}
