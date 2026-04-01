// TODO: Replace mock with real API calls — /api/checklists
import type { Checklist, ChecklistFrequency } from '@/types'

const mockChecklists: Checklist[] = [
  {
    id: 1,
    title: 'Åpning kjøkken',
    frequency: 'DAILY',
    moduleType: 'IK_MAT',
    completedBy: 'Ola Nordmann',
    completedAt: today('07:45'),
    items: [
      { id: 11, text: 'Vask og desinfiser arbeidsflater', completed: true, completedBy: 'Ola Nordmann' },
      { id: 12, text: 'Sjekk såpedispensere og håndsprit', completed: true, completedBy: 'Ola Nordmann' },
      { id: 13, text: 'Kontroller skadedyrfeller', completed: true, completedBy: 'Ola Nordmann' },
      { id: 14, text: 'Sjekk holdbarhetsdatoer', completed: true, completedBy: 'Ola Nordmann' },
      { id: 15, text: 'Registrer ansatte på vakt', completed: true, completedBy: 'Ola Nordmann' },
    ]
  },
  {
    id: 2,
    title: 'Matdisplay og servering',
    frequency: 'DAILY',
    moduleType: 'IK_MAT',
    items: [
      { id: 21, text: 'Sjekk temperatur i sushi-display', completed: true, completedBy: 'Kari Larsen' },
      { id: 22, text: 'Renhold serveringsområde', completed: false },
      { id: 23, text: 'Fyll på engangshansker', completed: false },
      { id: 24, text: 'Kontroll matmerking og allergener', completed: false },
    ]
  },
  {
    id: 3,
    title: 'Kveldsstenging',
    frequency: 'DAILY',
    moduleType: 'IK_MAT',
    items: [
      { id: 31, text: 'Dyprenhold grillstasjon', completed: false },
      { id: 32, text: 'Tøm og desinfiser avfallsbeholdere', completed: false },
      { id: 33, text: 'Mopp gulv med desinfeksjon', completed: false },
      { id: 34, text: 'Siste temp-sjekk — alle enheter', completed: false },
    ]
  },
  {
    id: 4,
    title: 'Ukentlig kjøkkenrenhold',
    frequency: 'WEEKLY',
    moduleType: 'IK_MAT',
    items: [
      { id: 41, text: 'Rengjør filter over stekeovn', completed: false },
      { id: 42, text: 'Avkalk kaffemaskiner', completed: false },
      { id: 43, text: 'Rengjør kjøleromsdørene', completed: false },
      { id: 44, text: 'Sjekk og rengjør iskremmaskin', completed: false },
    ]
  },
  {
    id: 5,
    title: 'Alkohol-logg kveldssjift',
    frequency: 'DAILY',
    moduleType: 'IK_ALKOHOL',
    items: [
      { id: 51, text: 'Sjekk ID for alle gjester under 25 år', completed: false },
      { id: 52, text: 'Logg antall avviste alderskontroller', completed: false },
      { id: 53, text: 'Kontroller mengde alkohol på lager', completed: false },
    ]
  },
  {
    id: 6,
    title: 'Månedlig utstyrskontroll',
    frequency: 'MONTHLY',
    moduleType: 'IK_MAT',
    items: [
      { id: 61, text: 'Kontroller termometre — kalibrering', completed: false },
      { id: 62, text: 'Rens og kontroller kjøleaggregater', completed: false },
      { id: 63, text: 'Oppdater HACCP-dokumentasjon', completed: false },
      { id: 64, text: 'Gjennomgå allergeninformasjon på meny', completed: false },
    ]
  }
]

function delay(ms = 300) { return new Promise(r => setTimeout(r, ms)) }
function today(time: string) { return new Date().toISOString().split('T')[0] + 'T' + time + ':00' }

export const checklistsService = {
  async getByFrequency(frequency: ChecklistFrequency): Promise<Checklist[]> {
    await delay()
    return mockChecklists.filter(c => c.frequency === frequency)
    // Real: return (await api.get<Checklist[]>(`/checklists?frequency=${frequency}`)).data
  },

  async toggleItem(_checklistId: number, _itemId: number, _completed: boolean): Promise<void> {
    await delay(150)
    // TODO: Replace mock with real API call
    // Real: await api.patch(`/checklists/${_checklistId}/items/${_itemId}`, { completed: _completed })
  }
}
