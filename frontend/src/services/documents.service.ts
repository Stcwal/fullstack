// TODO: Replace mock with real API calls — /api/documents
import type { TrainingDocument, EmployeeCertification } from '@/types'

const mockDocuments: TrainingDocument[] = [
  {
    id: 1, title: 'Mattrygghet — grunnkurs',
    subtitle: 'Obligatorisk for alle ansatte · Sist oppdatert jan 2026',
    type: 'PDF', actionLabel: 'Last ned',
    colorBg: '#EDE9FE', colorText: '#5B21B6'
  },
  {
    id: 2, title: 'Ansvarlig alkoholservering',
    subtitle: 'IK-Alkohol · Alle med skjenkebevilling',
    type: 'PDF', actionLabel: 'Last ned',
    colorBg: '#EFF6FF', colorText: '#1D4ED8'
  },
  {
    id: 3, title: 'HACCP-plan — Everest Sushi',
    subtitle: 'Fareanalyse og kritiske kontrollpunkt',
    type: 'DOC', actionLabel: 'Last ned',
    colorBg: '#F0FDF4', colorText: '#15803D'
  },
  {
    id: 4, title: 'Bruk av temperaturmåler',
    subtitle: 'Videoguide · 4 min',
    type: 'VID', actionLabel: 'Se video',
    colorBg: '#FFFBEB', colorText: '#92400E'
  },
  {
    id: 5, title: 'Allergenhåndtering',
    subtitle: 'Rutine for merking og informasjon til gjester',
    type: 'PDF', actionLabel: 'Last ned',
    colorBg: '#FEF2F2', colorText: '#991B1B'
  }
]

const mockCertifications: EmployeeCertification[] = [
  { id: 1, name: 'Kari Larsen',   status: 'COMPLETE',  expiredCount: 0, missingCount: 0 },
  { id: 2, name: 'Ola Nordmann',  status: 'EXPIRING',  expiredCount: 1, missingCount: 0 },
  { id: 3, name: 'Per Martinsen', status: 'MISSING',   expiredCount: 0, missingCount: 2 }
]

function delay(ms = 250) { return new Promise(r => setTimeout(r, ms)) }

export const documentsService = {
  async getTrainingDocs(): Promise<TrainingDocument[]> {
    await delay()
    return [...mockDocuments]
    // Real: return (await api.get<TrainingDocument[]>('/documents/training')).data
  },

  async getCertifications(): Promise<EmployeeCertification[]> {
    await delay()
    return [...mockCertifications]
    // Real: return (await api.get<EmployeeCertification[]>('/certifications')).data
  }
}
