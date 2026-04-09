import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useChecklistsStore } from '@/stores/checklists'
import { checklistsService } from '@/services/checklists.service'

vi.mock('@/services/checklists.service', () => ({
  checklistsService: {
    getByFrequency: vi.fn(),
    toggleItem:     vi.fn(),
  }
}))

const makeChecklist = (overrides = {}) => ({
  id: 1,
  title: 'Åpning kjøkken',
  frequency: 'DAILY' as const,
  moduleType: 'IK_MAT' as const,
  items: [
    { id: 10, text: 'Sjekk temperatur fryser', completed: false },
    { id: 11, text: 'Tøm søppel',              completed: true  },
  ],
  ...overrides,
})

describe('useChecklistsStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('starts empty', () => {
    const store = useChecklistsStore()
    expect(store.checklists).toHaveLength(0)
    expect(store.loading).toBe(false)
  })

  it('fetchAll() populates checklists and sets activeFrequency', async () => {
    const list = [makeChecklist({ id: 1 }), makeChecklist({ id: 2, title: 'Stenging' })]
    vi.mocked(checklistsService.getByFrequency).mockResolvedValue(list)

    const store = useChecklistsStore()
    await store.fetchAll('DAILY')

    expect(store.checklists).toHaveLength(2)
    expect(store.activeFrequency).toBe('DAILY')
  })

  it('fetchAll() uses DAILY as default frequency', async () => {
    vi.mocked(checklistsService.getByFrequency).mockResolvedValue([])

    const store = useChecklistsStore()
    await store.fetchAll()

    expect(vi.mocked(checklistsService.getByFrequency)).toHaveBeenCalledWith('DAILY')
  })

  it('fetchAll() clears loading flag after success', async () => {
    vi.mocked(checklistsService.getByFrequency).mockResolvedValue([])

    const store = useChecklistsStore()
    await store.fetchAll('WEEKLY')

    expect(store.loading).toBe(false)
  })

  it('toggleItem() flips item.completed optimistically before service responds', async () => {
    vi.mocked(checklistsService.getByFrequency).mockResolvedValue([makeChecklist()])
    vi.mocked(checklistsService.toggleItem).mockResolvedValue(undefined as any)

    const store = useChecklistsStore()
    await store.fetchAll()

    const item = store.checklists[0].items[0] // completed: false
    expect(item.completed).toBe(false)

    const togglePromise = store.toggleItem(1, 10)
    // Optimistic: should flip immediately (synchronously before await resolves)
    expect(store.checklists[0].items[0].completed).toBe(true)

    await togglePromise
    expect(store.checklists[0].items[0].completed).toBe(true)
  })

  it('toggleItem() reverts on service error', async () => {
    vi.mocked(checklistsService.getByFrequency).mockResolvedValue([makeChecklist()])
    vi.mocked(checklistsService.toggleItem).mockRejectedValue(new Error('network error'))

    const store = useChecklistsStore()
    await store.fetchAll()

    const item = store.checklists[0].items[0] // completed: false
    await store.toggleItem(1, 10)

    // Should have reverted back to false
    expect(item.completed).toBe(false)
  })

  it('toggleItem() does nothing when checklist id does not exist', async () => {
    vi.mocked(checklistsService.getByFrequency).mockResolvedValue([makeChecklist()])

    const store = useChecklistsStore()
    await store.fetchAll()

    // Should not throw or call service
    await store.toggleItem(999, 10)
    expect(vi.mocked(checklistsService.toggleItem)).not.toHaveBeenCalled()
  })

  it('toggleItem() does nothing when item id does not exist', async () => {
    vi.mocked(checklistsService.getByFrequency).mockResolvedValue([makeChecklist()])

    const store = useChecklistsStore()
    await store.fetchAll()

    await store.toggleItem(1, 999)
    expect(vi.mocked(checklistsService.toggleItem)).not.toHaveBeenCalled()
  })
})
