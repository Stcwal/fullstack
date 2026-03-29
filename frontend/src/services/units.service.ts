// TODO: Replace mock with real API calls — /api/units
import type { Unit } from '@/types'

let mockUnits: Unit[] = [
  { id: 1, name: 'Fryser #1',            type: 'FREEZER', targetTemp: -18, minTemp: -22, maxTemp: -16, contents: 'Sjømat, Kjøtt, Ferdigvarer',  active: true,  hasAlert: false },
  { id: 2, name: 'Fryser #2',            type: 'FREEZER', targetTemp: -18, minTemp: -22, maxTemp: -16, contents: 'Grønnsaker, Desserter',        active: true,  hasAlert: true  },
  { id: 3, name: 'Kjøleskap #1 (kjøkken)', type: 'FRIDGE', targetTemp:   3, minTemp:   1, maxTemp:   4, contents: 'Fisk, Meieriprodukter, Sauser', active: true,  hasAlert: false },
  { id: 4, name: 'Kjøleskap #2 (bar)',   type: 'FRIDGE', targetTemp:   3, minTemp:   1, maxTemp:   4, contents: 'Juice, Melk, Frukt',            active: true,  hasAlert: false },
  { id: 5, name: 'Visningskjøler',       type: 'COOLER', targetTemp:   5, minTemp:   3, maxTemp:   6, contents: 'Sushi-display, Drikke',         active: true,  hasAlert: false },
  { id: 6, name: 'Kjøleskap #3 (lager)', type: 'FRIDGE', targetTemp:   3, minTemp:   1, maxTemp:   4, contents: 'Lagervarer',                    active: false, hasAlert: false }
]

let nextId = 7

function delay(ms = 300) { return new Promise(r => setTimeout(r, ms)) }

export const unitsService = {
  async getAll(): Promise<Unit[]> {
    await delay()
    return [...mockUnits]
    // Real: return (await api.get<Unit[]>('/units')).data
  },

  async create(data: Omit<Unit, 'id'>): Promise<Unit> {
    await delay()
    const unit: Unit = { ...data, id: nextId++ }
    mockUnits.push(unit)
    return unit
    // Real: return (await api.post<Unit>('/units', data)).data
  },

  async update(id: number, data: Partial<Unit>): Promise<Unit> {
    await delay()
    const idx = mockUnits.findIndex(u => u.id === id)
    if (idx === -1) throw new Error('Enhet ikke funnet')
    mockUnits[idx] = { ...mockUnits[idx], ...data }
    return { ...mockUnits[idx] }
    // Real: return (await api.put<Unit>(`/units/${id}`, data)).data
  },

  async remove(id: number): Promise<void> {
    await delay()
    mockUnits = mockUnits.filter(u => u.id !== id)
    // Real: await api.delete(`/units/${id}`)
  }
}
