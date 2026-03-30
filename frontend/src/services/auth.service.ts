// TODO: Replace mock with real API call — POST /api/auth/login
import type { LoginCredentials, AuthResponse, User } from '@/types'

const MOCK_USERS: (AuthResponse & { password: string })[] = [
  {
    password: 'admin123',
    token: 'mock-jwt-admin',
    user: {
      id: 1, firstName: 'Kari', lastName: 'Larsen', email: 'kari@everestsushi.no', role: 'ADMIN',
      organizationId: 1,
      permissions: { temperatureLogging: true, checklists: true, reports: true, deviations: true, userAdmin: true, settings: true }
    }
  },
  {
    password: 'leder123',
    token: 'mock-jwt-manager',
    user: {
      id: 2, firstName: 'Ola', lastName: 'Nordmann', email: 'ola@everestsushi.no', role: 'MANAGER',
      organizationId: 1,
      permissions: { temperatureLogging: true, checklists: true, reports: true, deviations: true, userAdmin: false, settings: false }
    }
  },
  {
    password: 'ansatt123',
    token: 'mock-jwt-staff',
    user: {
      id: 3, firstName: 'Per', lastName: 'Martinsen', email: 'per@everestsushi.no', role: 'STAFF',
      organizationId: 1,
      permissions: { temperatureLogging: true, checklists: true, reports: false, deviations: true, userAdmin: false, settings: false }
    }
  }
]

export const authService = {
  async login(credentials: LoginCredentials): Promise<AuthResponse> {
    await delay(400)
    const match = MOCK_USERS.find(
      u => u.user.email === credentials.email && u.password === credentials.password
    )
    if (!match) throw new Error('Feil e-post eller passord')
    return { token: match.token, user: match.user }
    // Real: return (await api.post<AuthResponse>('/auth/login', credentials)).data
  },

  async me(): Promise<User> {
    await delay(200)
    const stored = sessionStorage.getItem('user')
    if (!stored) throw new Error('Not authenticated')
    return JSON.parse(stored)
    // Real: return (await api.get<User>('/auth/me')).data
  }
}

function delay(ms: number) {
  return new Promise(resolve => setTimeout(resolve, ms))
}
