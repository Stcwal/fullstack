// TODO: Replace mock with real API calls — /api/units
import type { Unit } from '@/types'

let mockUnits: Unit[] = [
  { id: 1, name: 'Fryser #1',            type: 'FREEZER', targetTemperature: -18, minThreshold: -22, maxThreshold: -16, description: 'Sjømat, Kjøtt, Ferdigvarer',  active: true,  hasAlert: false },
  { id: 2, name: 'Fryser #2',            type: 'FREEZER', targetTemperature: -18, minThreshold: -22, maxThreshold: -16, description: 'Grønnsaker, Desserter',        active: true,  hasAlert: true  },
  { id: 3, name: 'Kjøleskap #1 (kjøkken)', type: 'FRIDGE', targetTemperature:   3, minThreshold:   1, maxThreshold:   4, description: 'Fisk, Meieriprodukter, Sauser', active: true,  hasAlert: false },
  { id: 4, name: 'Kjøleskap #2 (bar)',   type: 'FRIDGE', targetTemperature:   3, minThreshold:   1, maxThreshold:   4, description: 'Juice, Melk, Frukt',            active: true,  hasAlert: false },
  { id: 5, name: 'Visningskjøler',       type: 'COOLER', targetTemperature:   5, minThreshold:   3, maxThreshold:   6, description: 'Sushi-display, Drikke',         active: true,  hasAlert: false },
  { id: 6, name: 'Kjøleskap #3 (lager)', type: 'FRIDGE', targetTemperature:   3, minThreshold:   1, maxThreshold:   4, description: 'Lagervarer',                    active: false, hasAlert: false }
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
