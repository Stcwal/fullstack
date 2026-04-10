import type { LoginCredentials, AuthResponse, User } from '@/types'
import api from './api'

interface LoginResponseData {
  userId: number
  email: string
  firstName: string
  lastName: string
  role: User['role']
  organizationId: number
  primaryLocationId?: number
  token: string
}

// Shape returned by GET /api/users/me
interface BackendUserResponse {
  id: number
  firstName: string
  lastName: string
  email: string
  role: User['role']
  organizationId: number
  organizationName?: string
  homeLocationId?: number
  homeLocationName?: string
  isActive: boolean
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
        organizationId: data.organizationId,
        primaryLocationId: data.primaryLocationId
      }
    }
  },

  /** Fetches the current user's fresh profile from GET /api/users/me */
  async me(): Promise<User> {
    const response = await api.get<BackendUserResponse>('/users/me')
    const d = response.data
    return {
      id: d.id,
      firstName: d.firstName,
      lastName: d.lastName,
      email: d.email,
      role: d.role,
      organizationId: d.organizationId,
      organizationName: d.organizationName,
      primaryLocationId: d.homeLocationId,
      primaryLocationName: d.homeLocationName,
    }
  }
}
