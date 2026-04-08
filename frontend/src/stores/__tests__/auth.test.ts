import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import * as authServiceModule from '@/services/auth.service'

describe('useAuthStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    sessionStorage.clear()
    vi.restoreAllMocks()
  })

  it('keeps login-response user when me() throws', async () => {
    vi.spyOn(authServiceModule.authService, 'login').mockResolvedValue({
      token: 'tok',
      user: { id: 1, firstName: 'Test', lastName: 'User', email: 't@t.no', role: 'STAFF' }
    })
    vi.spyOn(authServiceModule.authService, 'me').mockRejectedValue(new Error('me() failed'))

    const auth = useAuthStore()
    await auth.login('t@t.no', 'pw')

    expect(auth.user?.firstName).toBe('Test')
    expect(auth.token).toBe('tok')
  })
})
