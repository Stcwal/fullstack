// TODO: Replace mock with real API calls — /api/organization
import type { Organization, SettingsUser } from '@/types'

let mockOrg: Organization = {
  name: 'Everest Sushi & Fusion AS',
  orgNumber: '937 219 997',
  industry: 'Restaurant',
  address: 'Innherredsveien 1, 7014 Trondheim',
  modules: { ikMat: true, ikAlkohol: true },
  notifications: {
    emailOnTempDeviation: true,
    dailySummaryToManagers: true,
    smsOnCritical: false
  }
}

let mockUsers: SettingsUser[] = [
  {
    id: 1, firstName: 'Kari',  lastName: 'Larsen',    email: 'kari@everestsushi.no',
    role: 'ADMIN',   isActive: true, colorBg: '#EDE9FE', colorText: '#5B21B6',
    permissions: { temperatureLogging: true, checklists: true, reports: true, deviations: true, userAdmin: true, settings: true }
  },
  {
    id: 2, firstName: 'Ola',   lastName: 'Nordmann',  email: 'ola@everestsushi.no',
    role: 'MANAGER', isActive: true, colorBg: '#F0FDF4', colorText: '#15803D',
    permissions: { temperatureLogging: true, checklists: true, reports: true, deviations: true, userAdmin: false, settings: false }
  },
  {
    id: 3, firstName: 'Per',   lastName: 'Martinsen', email: 'per@everestsushi.no',
    role: 'STAFF',   isActive: true, colorBg: '#F1F5F9', colorText: '#475569',
    permissions: { temperatureLogging: true, checklists: true, reports: false, deviations: true, userAdmin: false, settings: false }
  }
]

function delay(ms = 300) { return new Promise(r => setTimeout(r, ms)) }

export const organizationService = {
  async getOrg(): Promise<Organization> {
    await delay()
    return { ...mockOrg }
    // Real: return (await api.get<Organization>('/organization')).data
  },

  async updateOrg(data: Partial<Organization>): Promise<Organization> {
    await delay()
    mockOrg = { ...mockOrg, ...data }
    return { ...mockOrg }
    // Real: return (await api.put<Organization>('/organization', data)).data
  },

  async getUsers(): Promise<SettingsUser[]> {
    await delay()
    return [...mockUsers]
    // Real: return (await api.get<SettingsUser[]>('/users')).data
  },

  async updateUser(id: number, data: Partial<SettingsUser>): Promise<SettingsUser> {
    await delay()
    const idx = mockUsers.findIndex(u => u.id === id)
    if (idx === -1) throw new Error('Bruker ikke funnet')
    mockUsers[idx] = { ...mockUsers[idx], ...data }
    return { ...mockUsers[idx] }
    // Real: return (await api.put<SettingsUser>(`/users/${id}`, data)).data
  },

  async createUser(data: Omit<SettingsUser, 'id'>): Promise<SettingsUser> {
    await delay()
    const user: SettingsUser = { ...data, id: Date.now() }
    mockUsers.push(user)
    return user
    // Real: return (await api.post<SettingsUser>('/users', data)).data
  }
}
