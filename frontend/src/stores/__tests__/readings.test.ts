import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useReadingsStore } from '@/stores/readings'
import { readingsService } from '@/services/readings.service'

vi.mock('@/services/readings.service', () => ({
  readingsService: {
    getByUnit: vi.fn(),
    create:    vi.fn(),
  }
}))

const makeReading = (overrides = {}) => ({
  id: 1,
  unitId: 10,
  temperature: -18.5,
  recordedAt: '2026-04-09T10:00:00Z',
  recordedBy: { id: 1, name: 'Kari Larsen' },
  isDeviation: false,
  ...overrides,
})

describe('useReadingsStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('starts with empty readings', () => {
    const store = useReadingsStore()
    expect(store.readings).toHaveLength(0)
    expect(store.loading).toBe(false)
    expect(store.saving).toBe(false)
  })

  it('fetchByUnit() populates readings for that unit', async () => {
    const list = [makeReading({ id: 1 }), makeReading({ id: 2 })]
    vi.mocked(readingsService.getByUnit).mockResolvedValue(list)

    const store = useReadingsStore()
    await store.fetchByUnit(10)

    expect(store.readings).toHaveLength(2)
    expect(vi.mocked(readingsService.getByUnit)).toHaveBeenCalledWith(10)
  })

  it('fetchByUnit() clears loading flag on success', async () => {
    vi.mocked(readingsService.getByUnit).mockResolvedValue([])

    const store = useReadingsStore()
    await store.fetchByUnit(10)

    expect(store.loading).toBe(false)
  })

  it('addReading() prepends the created reading to the list', async () => {
    const existing = makeReading({ id: 1, unitId: 10 })
    vi.mocked(readingsService.getByUnit).mockResolvedValue([existing])
    const newReading = makeReading({ id: 99, unitId: 10, temperature: -20.0 })
    vi.mocked(readingsService.create).mockResolvedValue(newReading)

    const store = useReadingsStore()
    await store.fetchByUnit(10)
    await store.addReading({ unitId: 10, temperature: -20.0, recordedAt: new Date().toISOString() })

    expect(store.readings[0].id).toBe(99)
    expect(store.readings[0].temperature).toBe(-20.0)
    expect(store.readings).toHaveLength(2)
  })

  it('addReading() clears saving flag after completion', async () => {
    vi.mocked(readingsService.create).mockResolvedValue(makeReading({ id: 5 }))

    const store = useReadingsStore()
    await store.addReading({ unitId: 10, temperature: -18, recordedAt: new Date().toISOString() })

    expect(store.saving).toBe(false)
  })

  it('getByUnit() returns only readings for the given unitId', async () => {
    vi.mocked(readingsService.getByUnit).mockResolvedValue([
      makeReading({ id: 1, unitId: 10 }),
      makeReading({ id: 2, unitId: 10 }),
    ])

    const store = useReadingsStore()
    await store.fetchByUnit(10)
    // Manually inject a reading for a different unit to test filter
    store.readings.push(makeReading({ id: 3, unitId: 20 }))

    expect(store.getByUnit(10)).toHaveLength(2)
    expect(store.getByUnit(20)).toHaveLength(1)
    expect(store.getByUnit(99)).toHaveLength(0)
  })
})
