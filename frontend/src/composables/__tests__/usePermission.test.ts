import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { usePermission } from '../usePermission'
import { useAuthStore } from '@/stores/auth'

describe('usePermission', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('returns false for any permission when no user is logged in', () => {
    const { can } = usePermission()
    expect(can('temperatureLogging')).toBe(false)
    expect(can('settings')).toBe(false)
  })

  it('returns true when user has the permission explicitly set', () => {
    const auth = useAuthStore()
    auth.user = {
      id: 1, firstName: 'Kari', lastName: 'Larsen',
      email: 'kari@test.no', role: 'ADMIN',
      permissions: {
        temperatureLogging: true, checklists: true, reports: true,
        deviations: true, userAdmin: true, settings: true
      }
    }
    const { can } = usePermission()
    expect(can('temperatureLogging')).toBe(true)
    expect(can('settings')).toBe(true)
  })

  it('returns false when user exists but permission is explicitly false', () => {
    const auth = useAuthStore()
    auth.user = {
      id: 2, firstName: 'Per', lastName: 'Hansen',
      email: 'per@test.no', role: 'STAFF',
      permissions: {
        temperatureLogging: true, checklists: true, reports: false,
        deviations: true, userAdmin: false, settings: false
      }
    }
    const { can } = usePermission()
    expect(can('reports')).toBe(false)
    expect(can('userAdmin')).toBe(false)
  })

  it('falls back to role-derived defaults when user has no permissions object', () => {
    const auth = useAuthStore()
    auth.user = {
      id: 3, firstName: 'Ola', lastName: 'Berg',
      email: 'ola@test.no', role: 'MANAGER'
    }
    const { can } = usePermission()
    // MANAGER defaults: temperatureLogging true, settings false
    expect(can('temperatureLogging')).toBe(true)
    expect(can('settings')).toBe(false)
    expect(can('userAdmin')).toBe(false)
    expect(can('reports')).toBe(true)
  })

  it('STAFF role defaults deny reports, userAdmin, and settings', () => {
    const auth = useAuthStore()
    auth.user = {
      id: 4, firstName: 'Anne', lastName: 'Dahl',
      email: 'anne@test.no', role: 'STAFF'
    }
    const { can } = usePermission()
    expect(can('temperatureLogging')).toBe(true)
    expect(can('checklists')).toBe(true)
    expect(can('deviations')).toBe(true)
    expect(can('reports')).toBe(false)
    expect(can('userAdmin')).toBe(false)
    expect(can('settings')).toBe(false)
  })

  it('ADMIN role defaults grant all permissions', () => {
    const auth = useAuthStore()
    auth.user = {
      id: 5, firstName: 'Admin', lastName: 'User',
      email: 'admin@test.no', role: 'ADMIN'
    }
    const { can } = usePermission()
    expect(can('temperatureLogging')).toBe(true)
    expect(can('checklists')).toBe(true)
    expect(can('reports')).toBe(true)
    expect(can('deviations')).toBe(true)
    expect(can('userAdmin')).toBe(true)
    expect(can('settings')).toBe(true)
  })

  it('SUPERVISOR role defaults: reports true, userAdmin and settings false', () => {
    const auth = useAuthStore()
    auth.user = {
      id: 6, firstName: 'Supervisor', lastName: 'User',
      email: 'supervisor@test.no', role: 'SUPERVISOR'
    }
    const { can } = usePermission()
    expect(can('temperatureLogging')).toBe(true)
    expect(can('checklists')).toBe(true)
    expect(can('reports')).toBe(true)
    expect(can('deviations')).toBe(true)
    expect(can('userAdmin')).toBe(false)
    expect(can('settings')).toBe(false)
  })
})
