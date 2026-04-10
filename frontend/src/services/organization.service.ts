import type { Organization, SettingsUser, UserPermissions, UserRole } from '@/types'
import api from './api'

// ── Colour tokens per role (frontend-only display) ──────────────────────────
const ROLE_COLORS: Record<UserRole, { colorBg: string; colorText: string }> = {
  ADMIN:      { colorBg: '#EDE9FE', colorText: '#5B21B6' },
  SUPERVISOR: { colorBg: '#FEF3C7', colorText: '#92400E' },
  MANAGER:    { colorBg: '#F0FDF4', colorText: '#15803D' },
  STAFF:      { colorBg: '#F1F5F9', colorText: '#475569' },
}

// ── Default permissions per role (frontend-only, backend has none) ───────────
const ROLE_PERMISSIONS: Record<UserRole, UserPermissions> = {
  ADMIN:      { temperatureLogging: true,  checklists: true,  reports: true,  deviations: true,  userAdmin: true,  settings: true  },
  SUPERVISOR: { temperatureLogging: true,  checklists: true,  reports: true,  deviations: true,  userAdmin: false, settings: false },
  MANAGER:    { temperatureLogging: true,  checklists: true,  reports: true,  deviations: true,  userAdmin: false, settings: false },
  STAFF:      { temperatureLogging: true,  checklists: true,  reports: false, deviations: true,  userAdmin: false, settings: false },
}

interface BackendLocation {
  id: number
  name: string
  address: string
}

interface BackendUser {
  id: number
  firstName: string
  lastName: string
  email: string
  role: UserRole
  isActive: boolean
}

interface BackendOrg {
  id: number
  name: string
  organizationNumber: string
}

// Frontend-only org fields that the backend does not persist
const ORG_DEFAULTS = {
  industry: 'Restaurant',
  address: 'Innherredsveien 1, 7014 Trondheim',
  modules: { ikMat: true, ikAlkohol: true },
  notifications: {
    emailOnTempDeviation: true,
    dailySummaryToManagers: true,
    smsOnCritical: false,
  },
} as const

export const organizationService = {
  async getOrg(): Promise<Organization> {
    const res = await api.get<BackendOrg>('/organization/me')
    return {
      name: res.data.name,
      orgNumber: res.data.organizationNumber,
      ...ORG_DEFAULTS,
    }
  },

  async updateOrg(data: Partial<Organization>): Promise<Organization> {
    await api.put('/organization/me', {
      name: data.name,
      organizationNumber: data.orgNumber?.replace(/\s/g, ''),
    })
    return this.getOrg()
  },

  async getUsers(): Promise<SettingsUser[]> {
    const res = await api.get<BackendUser[]>('/users')
    return res.data.map(u => ({
      id: u.id,
      firstName: u.firstName,
      lastName: u.lastName,
      email: u.email,
      role: u.role,
      isActive: u.isActive,
      ...ROLE_COLORS[u.role],
      permissions: ROLE_PERMISSIONS[u.role],
    }))
  },

  async getLocations(): Promise<BackendLocation[]> {
    const res = await api.get<BackendLocation[]>('/locations')
    return res.data
  },

  async updateUser(id: number, data: Partial<SettingsUser> & { locationId?: number }): Promise<SettingsUser> {
    // Name update
    if (data.firstName !== undefined || data.lastName !== undefined) {
      await api.put(`/users/${id}`, {
        firstName: data.firstName,
        lastName: data.lastName,
      })
    }
    // Role update (separate endpoint) — locationId required for MANAGER/STAFF
    if (data.role !== undefined) {
      await api.put(`/users/${id}/role`, { role: data.role, locationId: data.locationId ?? null })
    }
    // Re-fetch the updated user from the list
    const users = await this.getUsers()
    const updated = users.find(u => u.id === id)
    if (!updated) throw new Error('Bruker ikke funnet etter oppdatering')
    return updated
  },

  async createUser(data: Omit<SettingsUser, 'id'>): Promise<SettingsUser> {
    const res = await api.post<BackendUser>('/users', {
      firstName: data.firstName,
      lastName: data.lastName,
      email: data.email,
      role: data.role,
    })
    return {
      id: res.data.id,
      firstName: res.data.firstName,
      lastName: res.data.lastName,
      email: res.data.email,
      role: res.data.role,
      isActive: res.data.isActive,
      ...ROLE_COLORS[res.data.role],
      permissions: ROLE_PERMISSIONS[res.data.role],
    }
  },
}
