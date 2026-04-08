import type {
  AlderskontrollEntry,
  NewAlderskontrollEntry,
  AlkoholIncident,
  NewAlkoholIncident,
  AlkoholStats
} from '@/types'

// Mock data
const mockEntries: AlderskontrollEntry[] = [
  {
    id: 1,
    recordedAt: new Date(Date.now() - 3600000).toISOString(),
    recordedBy: 'Kari Larsen',
    outcome: 'APPROVED'
  },
  {
    id: 2,
    recordedAt: new Date(Date.now() - 7200000).toISOString(),
    recordedBy: 'Per Hansen',
    outcome: 'DENIED',
    note: 'Manglende legitimasjon'
  }
]

const mockIncidents: AlkoholIncident[] = [
  {
    id: 1,
    incidentType: 'BERUSET_GJEST',
    description: 'Gjest virket beruset, ble bedt om å forlate lokalet.',
    occurredAt: new Date(Date.now() - 86400000).toISOString(),
    reportedBy: 'Kari Larsen',
    followUpRequired: false
  }
]

export const alkoholService = {
  // TODO: Replace mock with real API call — GET /api/alkohol/age-verifications
  async getAlderskontrollEntries(): Promise<AlderskontrollEntry[]> {
    await new Promise(r => setTimeout(r, 300))
    return [...mockEntries]
  },

  // TODO: Replace mock with real API call — POST /api/alkohol/age-verifications
  async createAlderskontrollEntry(data: NewAlderskontrollEntry): Promise<AlderskontrollEntry> {
    await new Promise(r => setTimeout(r, 200))
    const entry: AlderskontrollEntry = {
      id: Date.now(),
      recordedAt: data.recordedAt ?? new Date().toISOString(),
      recordedBy: 'Innlogget bruker', // TODO: Replace with auth.user name
      outcome: data.outcome,
      note: data.note
    }
    mockEntries.unshift(entry)
    return entry
  },

  // TODO: Replace mock with real API call — GET /api/alkohol/incidents
  async getIncidents(): Promise<AlkoholIncident[]> {
    await new Promise(r => setTimeout(r, 300))
    return [...mockIncidents]
  },

  // TODO: Replace mock with real API call — POST /api/alkohol/incidents
  async createIncident(data: NewAlkoholIncident): Promise<AlkoholIncident> {
    await new Promise(r => setTimeout(r, 200))
    const incident: AlkoholIncident = {
      id: Date.now(),
      incidentType: data.incidentType,
      description: data.description,
      occurredAt: data.occurredAt ?? new Date().toISOString(),
      reportedBy: 'Innlogget bruker', // TODO: Replace with auth.user name
      followUpRequired: data.followUpRequired
    }
    mockIncidents.unshift(incident)
    return incident
  },

  // TODO: Replace mock with real API call — GET /api/alkohol/stats (or derive from dashboard)
  async getStats(): Promise<AlkoholStats> {
    await new Promise(r => setTimeout(r, 200))
    return {
      ageChecksToday: mockEntries.filter(e =>
        new Date(e.recordedAt).toDateString() === new Date().toDateString()
      ).length,
      incidentsThisWeek: mockIncidents.length,
      checklistCompletionPct: 75
    }
  }
}
