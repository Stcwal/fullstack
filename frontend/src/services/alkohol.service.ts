import type {
  AlderskontrollEntry,
  NewAlderskontrollEntry,
  AlkoholIncident,
  AlkoholIncidentType,
  NewAlkoholIncident,
  AlkoholStats,
} from '@/types'
import api from './api'
import { useAuthStore } from '@/stores/auth'

// ── Incident type mapping: Norwegian frontend ↔ English backend ──────────────
const INCIDENT_TO_BACKEND: Record<AlkoholIncidentType, string> = {
  NEKTET_SERVERING: 'REFUSED_SERVICE',
  BERUSET_GJEST:    'INTOXICATED_PERSON',
  POLITIKONTAKT:    'DISTURBANCE',
  ANNET:            'OTHER',
}

const INCIDENT_FROM_BACKEND: Record<string, AlkoholIncidentType> = {
  REFUSED_SERVICE:   'NEKTET_SERVERING',
  INTOXICATED_PERSON:'BERUSET_GJEST',
  UNDERAGE_ATTEMPT:  'ANNET',
  OVER_SERVING:      'ANNET',
  DISTURBANCE:       'POLITIKONTAKT',
  OTHER:             'ANNET',
}

// ── Backend response shapes ───────────────────────────────────────────────────
interface BackendVerification {
  id: number
  verifiedByName: string
  wasRefused: boolean
  note?: string
  verifiedAt: string
}

interface BackendIncident {
  id: number
  incidentType: string
  description: string
  occurredAt: string
  reportedByName: string
  status: string  // OPEN | RESOLVED | IN_PROGRESS
}

function getLocationId(): number {
  const auth = useAuthStore()
  return auth.user?.primaryLocationId ?? 1
}

export const alkoholService = {
  async getAlderskontrollEntries(): Promise<AlderskontrollEntry[]> {
    const res = await api.get<BackendVerification[]>('/alcohol/age-verifications')
    return res.data.map(v => ({
      id: v.id,
      recordedAt: v.verifiedAt,
      recordedBy: v.verifiedByName,
      outcome: v.wasRefused ? 'DENIED' : 'APPROVED',
      note: v.note,
    }))
  },

  async createAlderskontrollEntry(data: NewAlderskontrollEntry): Promise<AlderskontrollEntry> {
    const res = await api.post<BackendVerification>('/alcohol/age-verifications', {
      locationId: getLocationId(),
      verificationMethod: 'ID_CHECKED',
      guestAppearedUnderage: true,
      idWasValid: data.outcome === 'APPROVED' ? true : data.outcome === 'DENIED' ? false : null,
      wasRefused: data.outcome === 'DENIED',
      note: data.note,
      verifiedAt: data.recordedAt ?? new Date().toISOString(),
    })
    return {
      id: res.data.id,
      recordedAt: res.data.verifiedAt,
      recordedBy: res.data.verifiedByName,
      outcome: res.data.wasRefused ? 'DENIED' : 'APPROVED',
      note: res.data.note,
    }
  },

  async getIncidents(): Promise<AlkoholIncident[]> {
    const res = await api.get<BackendIncident[]>('/alcohol/incidents')
    return res.data.map(i => ({
      id: i.id,
      incidentType: INCIDENT_FROM_BACKEND[i.incidentType] ?? 'ANNET',
      description: i.description,
      occurredAt: i.occurredAt,
      reportedBy: i.reportedByName,
      followUpRequired: i.status === 'OPEN',
    }))
  },

  async createIncident(data: NewAlkoholIncident): Promise<AlkoholIncident> {
    const res = await api.post<BackendIncident>('/alcohol/incidents', {
      locationId: getLocationId(),
      incidentType: INCIDENT_TO_BACKEND[data.incidentType] ?? 'OTHER',
      severity: data.followUpRequired ? 'HIGH' : 'MEDIUM',
      description: data.description,
      occurredAt: data.occurredAt ?? new Date().toISOString(),
    })
    return {
      id: res.data.id,
      incidentType: INCIDENT_FROM_BACKEND[res.data.incidentType] ?? 'ANNET',
      description: res.data.description,
      occurredAt: res.data.occurredAt,
      reportedBy: res.data.reportedByName,
      followUpRequired: res.data.status === 'OPEN',
    }
  },

  async getStats(): Promise<AlkoholStats> {
    const [entries, incidents] = await Promise.all([
      this.getAlderskontrollEntries().catch(() => [] as AlderskontrollEntry[]),
      this.getIncidents().catch(() => [] as AlkoholIncident[]),
    ])
    const today = new Date().toDateString()
    const weekAgo = Date.now() - 7 * 24 * 60 * 60 * 1000
    return {
      ageChecksToday: entries.filter(e => new Date(e.recordedAt).toDateString() === today).length,
      incidentsThisWeek: incidents.filter(i => new Date(i.occurredAt).getTime() >= weekAgo).length,
      checklistCompletionPct: 0,
    }
  },
}
