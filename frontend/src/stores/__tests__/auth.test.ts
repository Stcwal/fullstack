import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import * as authServiceModule from '@/services/auth.service'

const MOCK_USER = { id: 1, firstName: 'Kari', lastName: 'Larsen', email: 'kari@test.no', role: 'ADMIN' as const }
const FULL_USER  = { id: 1, firstName: 'Kari', lastName: 'Larsen', email: 'kari@test.no', role: 'ADMIN' as const, organizationId: 10 }

describe('useAuthStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    sessionStorage.clear()
    vi.restoreAllMocks()
  })

  it('is not authenticated on fresh init', () => {
    const auth = useAuthStore()
    expect(auth.isAuthenticated).toBe(false)
    expect(auth.user).toBeNull()
    expect(auth.token).toBeNull()
  })

  it('hydrates from sessionStorage when tokens exist', () => {
    sessionStorage.setItem('token', 'existing-tok')
    sessionStorage.setItem('user', JSON.stringify(MOCK_USER))
    // Create a fresh store after seeding sessionStorage
    setActivePinia(createPinia())
    const auth = useAuthStore()
    expect(auth.token).toBe('existing-tok')
    expect(auth.user?.email).toBe('kari@test.no')
    expect(auth.isAuthenticated).toBe(true)
  })

  it('login() stores token and user in state and sessionStorage', async () => {
    vi.spyOn(authServiceModule.authService, 'login').mockResolvedValue({ token: 'tok-123', user: MOCK_USER })
    vi.spyOn(authServiceModule.authService, 'me').mockResolvedValue(FULL_USER)

    const auth = useAuthStore()
    await auth.login('kari@test.no', 'pw')

    expect(auth.token).toBe('tok-123')
    expect(auth.isAuthenticated).toBe(true)
    expect(sessionStorage.getItem('token')).toBe('tok-123')
  })

  it('login() updates user with me() response when me() succeeds', async () => {
    vi.spyOn(authServiceModule.authService, 'login').mockResolvedValue({ token: 'tok', user: MOCK_USER })
    vi.spyOn(authServiceModule.authService, 'me').mockResolvedValue(FULL_USER)

    const auth = useAuthStore()
    await auth.login('kari@test.no', 'pw')

    expect(auth.user?.organizationId).toBe(10)
  })

  it('login() keeps login-response user when me() throws', async () => {
    vi.spyOn(authServiceModule.authService, 'login').mockResolvedValue({ token: 'tok', user: MOCK_USER })
    vi.spyOn(authServiceModule.authService, 'me').mockRejectedValue(new Error('me() failed'))

    const auth = useAuthStore()
    await auth.login('kari@test.no', 'pw')

    expect(auth.user?.firstName).toBe('Kari')
    expect(auth.token).toBe('tok')
  })

  it('logout() clears state and sessionStorage', async () => {
    vi.spyOn(authServiceModule.authService, 'login').mockResolvedValue({ token: 'tok', user: MOCK_USER })
    vi.spyOn(authServiceModule.authService, 'me').mockRejectedValue(new Error())

    const auth = useAuthStore()
    await auth.login('kari@test.no', 'pw')
    auth.logout()

    expect(auth.user).toBeNull()
    expect(auth.token).toBeNull()
    expect(auth.isAuthenticated).toBe(false)
    expect(sessionStorage.getItem('token')).toBeNull()
    expect(sessionStorage.getItem('user')).toBeNull()
  })
})
