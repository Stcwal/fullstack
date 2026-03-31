import { useAuthStore } from '@/stores/auth'
import type { UserPermissions } from '@/types'

export function usePermission() {
  const auth = useAuthStore()

  function can(key: keyof UserPermissions): boolean {
    return auth.user?.permissions?.[key] ?? false
  }

  return { can }
}
