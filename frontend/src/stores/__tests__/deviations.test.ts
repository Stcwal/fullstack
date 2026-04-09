import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useDeviationsStore } from '@/stores/deviations'
import { deviationsService } from '@/services/deviations.service'

vi.mock('@/services/deviations.service', () => ({
  deviationsService: {
    getAll:  vi.fn(),
    create:  vi.fn(),
    resolve: vi.fn(),
  }
}))

const makeDeviation = (overrides = {}) => ({
  id: 1,
  title: 'Feil temperatur',
  description: 'Fryser 1 viser +5°C',
  status: 'OPEN' as const,
  severity: 'CRITICAL' as const,
  reportedBy: 'Kari Larsen',
  reportedAt: '2026-04-09T10:00:00Z',
  moduleType: 'IK_MAT' as const,
  ...overrides,
})

describe('useDeviationsStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('starts empty', () => {
    const store = useDeviationsStore()
    expect(store.deviations).toHaveLength(0)
    expect(store.loading).toBe(false)
  })

  it('fetchAll() populates the deviations list', async () => {
    const list = [makeDeviation({ id: 1 }), makeDeviation({ id: 2, title: 'Manglende renhold' })]
    vi.mocked(deviationsService.getAll).mockResolvedValue(list)

    const store = useDeviationsStore()
    await store.fetchAll()

    expect(store.deviations).toHaveLength(2)
    expect(store.deviations[0].title).toBe('Feil temperatur')
  })

  it('fetchAll() clears loading flag even if it fails', async () => {
    vi.mocked(deviationsService.getAll).mockRejectedValue(new Error('network'))

    const store = useDeviationsStore()
    await store.fetchAll().catch(() => {})

    expect(store.loading).toBe(false)
  })

  it('report() prepends new deviation to the list', async () => {
    const existing = makeDeviation({ id: 1 })
    vi.mocked(deviationsService.getAll).mockResolvedValue([existing])
    const created = makeDeviation({ id: 99, title: 'Ny avvik' })
    vi.mocked(deviationsService.create).mockResolvedValue(created)

    const store = useDeviationsStore()
    await store.fetchAll()
    await store.report({ title: 'Ny avvik', description: 'desc', severity: 'MEDIUM', moduleType: 'IK_MAT' })

    expect(store.deviations[0].id).toBe(99)
    expect(store.deviations[0].title).toBe('Ny avvik')
    expect(store.deviations).toHaveLength(2)
  })

  it('resolve() updates status, resolution and resolvedAt on the matching deviation', async () => {
    const dev = makeDeviation({ id: 5, status: 'OPEN' as const })
    vi.mocked(deviationsService.getAll).mockResolvedValue([dev])
    vi.mocked(deviationsService.resolve).mockResolvedValue(undefined as any)

    const store = useDeviationsStore()
    await store.fetchAll()
    await store.resolve(5, 'Fikset ved rengjøring')

    const updated = store.deviations.find(d => d.id === 5)
    expect(updated?.status).toBe('RESOLVED')
    expect(updated?.resolution).toBe('Fikset ved rengjøring')
    expect(updated?.resolvedAt).toBeDefined()
  })

  it('openCount() returns count of non-RESOLVED deviations', async () => {
    vi.mocked(deviationsService.getAll).mockResolvedValue([
      makeDeviation({ id: 1, status: 'OPEN' }),
      makeDeviation({ id: 2, status: 'IN_PROGRESS' }),
      makeDeviation({ id: 3, status: 'RESOLVED' }),
    ])
    vi.mocked(deviationsService.resolve).mockResolvedValue(undefined as any)

    const store = useDeviationsStore()
    await store.fetchAll()

    expect(store.openCount()).toBe(2)
  })
})
