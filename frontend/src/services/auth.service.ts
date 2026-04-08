import type { LoginCredentials, AuthResponse, User } from '@/types'
import api from './api'

interface LoginResponseData {
  userId: number
  email: string
  firstName: string
  lastName: string
  role: User['role']
  organizationId: number
  token: string
}

export const authService = {
  async login(credentials: LoginCredentials): Promise<AuthResponse> {
    const response = await api.post<LoginResponseData>('/auth/login', credentials)
    const data = response.data
    return {
      token: data.token,
      user: {
        id: data.userId,
        firstName: data.firstName,
        lastName: data.lastName,
        email: data.email,
        role: data.role,
        organizationId: data.organizationId
      }
    }
  },

  async me(): Promise<User> {
    const stored = sessionStorage.getItem('user')
    if (!stored) throw new Error('Not authenticated')
    return JSON.parse(stored)
  }
}
