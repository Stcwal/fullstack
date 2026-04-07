import { useAuthStore } from '@/stores/auth'
import type { UserPermissions, UserRole } from '@/types'

const ROLE_DEFAULTS: Record<UserRole, UserPermissions> = {
  ADMIN:      { temperatureLogging: true,  checklists: true,  reports: true,  deviations: true,  userAdmin: true,  settings: true  },
  SUPERVISOR: { temperatureLogging: true,  checklists: true,  reports: true,  deviations: true,  userAdmin: false, settings: false },
  MANAGER:    { temperatureLogging: true,  checklists: true,  reports: true,  deviations: true,  userAdmin: false, settings: false },
  STAFF:      { temperatureLogging: true,  checklists: true,  reports: false, deviations: true,  userAdmin: false, settings: false },
}

export function usePermission() {
  const auth = useAuthStore()

  function can(key: keyof UserPermissions): boolean {
    if (!auth.user) return false
    if (auth.user.permissions?.[key] !== undefined) {
      return auth.user.permissions[key]
    }
    return ROLE_DEFAULTS[auth.user.role]?.[key] ?? false
  }

  return { can }
}
