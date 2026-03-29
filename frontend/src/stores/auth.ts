import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/types'
import { authService } from '@/services/auth.service'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(sessionStorage.getItem('token'))

  const isAuthenticated = computed(() => !!token.value && !!user.value)

  // Re-hydrate user from session on store init
  const stored = sessionStorage.getItem('user')
  if (stored) {
    try { user.value = JSON.parse(stored) } catch { /* noop */ }
  }

  async function login(email: string, password: string): Promise<void> {
    const response = await authService.login({ email, password })
    token.value = response.token
    user.value = response.user
    sessionStorage.setItem('token', response.token)
    sessionStorage.setItem('user', JSON.stringify(response.user))
  }

  function logout(): void {
    user.value = null
    token.value = null
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('user')
  }

  return { user, token, isAuthenticated, login, logout }
})
