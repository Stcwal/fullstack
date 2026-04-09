import type { TrainingDocument, EmployeeCertification } from '@/types'
import api from './api'

interface BackendDocument {
  id: number
  title: string
  description?: string
  category: string
  fileName: string
  contentType: string
  fileSize: number
  uploadedBy: string
  createdAt: string
}

const CATEGORY_COLORS: Record<string, { colorBg: string; colorText: string }> = {
  TRAINING_MATERIAL: { colorBg: '#EDE9FE', colorText: '#5B21B6' },
  POLICY:            { colorBg: '#EFF6FF', colorText: '#1D4ED8' },
  CERTIFICATION:     { colorBg: '#F0FDF4', colorText: '#15803D' },
  INSPECTION_REPORT: { colorBg: '#FEF2F2', colorText: '#991B1B' },
  OTHER:             { colorBg: '#FFFBEB', colorText: '#92400E' },
}

function deriveFileType(contentType: string): 'PDF' | 'DOC' | 'VID' {
  if (contentType.startsWith('video/')) return 'VID'
  if (contentType === 'application/pdf') return 'PDF'
  return 'DOC'
}

function mapDocument(doc: BackendDocument): TrainingDocument {
  const type = deriveFileType(doc.contentType)
  const colors = CATEGORY_COLORS[doc.category] ?? CATEGORY_COLORS['OTHER']
  return {
    id: doc.id,
    title: doc.title,
    subtitle: doc.description ?? doc.fileName,
    type,
    actionLabel: type === 'VID' ? 'Se video' : 'Last ned',
    ...colors,
  }
}

// Mock certifications — no backend endpoint exists yet
const mockCertifications: EmployeeCertification[] = [
  { id: 1, name: 'Kari Larsen',   status: 'COMPLETE',  expiredCount: 0, missingCount: 0 },
  { id: 2, name: 'Ola Nordmann',  status: 'EXPIRING',  expiredCount: 1, missingCount: 0 },
  { id: 3, name: 'Per Martinsen', status: 'MISSING',   expiredCount: 0, missingCount: 2 },
]

export const documentsService = {
  async getTrainingDocs(): Promise<TrainingDocument[]> {
    const res = await api.get<BackendDocument[]>('/documents', {
      params: { category: 'TRAINING_MATERIAL' },
    })
    return res.data.map(mapDocument)
  },

  async getCertifications(): Promise<EmployeeCertification[]> {
    // No backend endpoint — return mock data
    return [...mockCertifications]
  },
}
