// TODO: Replace mock with real API calls — /api/deviations
import type { Deviation, NewDeviation } from '@/types'

const mockDeviations: Deviation[] = [
  {
    id: 1,
    title: 'Fryser #2 over grenseverdi',
    description: 'Målt -12.1°C, grenseverdi er -18°C. Mulig kompressorfeil.',
    status: 'OPEN',
    severity: 'CRITICAL',
    reportedBy: 'Kari Larsen',
    reportedAt: todayAt('08:12'),
    moduleType: 'IK_MAT'
  },
  {
    id: 2,
    title: 'Manglende alderskontroll-logg',
    description: 'Barpersonalet fullførte ikke alderskontrollsjekkliste for kveldsvakt.',
    status: 'IN_PROGRESS',
    severity: 'MEDIUM',
    reportedBy: 'Per Martinsen',
    reportedAt: yesterdayAt('22:00'),
    moduleType: 'IK_ALKOHOL'
  },
  {
    id: 3,
    title: 'Utgått soyasaus på lager',
    description: 'Vare kastet, leverandør varslet. FIFO-rutine gjennomgått med personalet.',
    status: 'RESOLVED',
    severity: 'LOW',
    reportedBy: 'Ola Nordmann',
    reportedAt: daysAgoAt(3, '14:30'),
    moduleType: 'IK_MAT',
    resolvedAt: daysAgoAt(2, '09:00'),
    resolution: 'Vare kastet, leverandør varslet. FIFO gjennomgått.'
  }
]

let nextId = 10

function delay(ms = 350) { return new Promise(r => setTimeout(r, ms)) }
function todayAt(t: string) { return new Date().toISOString().split('T')[0]+'T'+t+':00' }
function yesterdayAt(t: string) { const d=new Date(); d.setDate(d.getDate()-1); return d.toISOString().split('T')[0]+'T'+t+':00' }
function daysAgoAt(n: number, t: string) { const d=new Date(); d.setDate(d.getDate()-n); return d.toISOString().split('T')[0]+'T'+t+':00' }

export const deviationsService = {
  async getAll(): Promise<Deviation[]> {
    await delay()
    return [...mockDeviations]
    // Real: return (await api.get<Deviation[]>('/deviations')).data
  },

  async create(data: NewDeviation): Promise<Deviation> {
    await delay()
    const user = JSON.parse(sessionStorage.getItem('user') || '{}')
    const deviation: Deviation = {
      id: nextId++,
      ...data,
      status: 'OPEN',
      reportedBy: user.firstName ? `${user.firstName} ${user.lastName}` : 'Ukjent',
      reportedAt: new Date().toISOString()
    }
    mockDeviations.unshift(deviation)
    return deviation
    // Real: return (await api.post<Deviation>('/deviations', data)).data
  },

  async resolve(id: number, resolution: string): Promise<void> {
    await delay()
    const dev = mockDeviations.find(d => d.id === id)
    if (dev) {
      dev.status = 'RESOLVED'
      dev.resolution = resolution
      dev.resolvedAt = new Date().toISOString()
    }
    // Real: await api.patch(`/deviations/${id}/resolve`, { resolution })
  }
}
