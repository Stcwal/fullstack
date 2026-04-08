# Authentication and Permissions — IK-Kontrollsystem

**Date**: 2026-03-30

---

## 1. Overview

The authentication system is JWT-based, designed to integrate with a Spring Boot backend. During the frontend development phase, all auth logic is mocked in `src/services/auth.service.ts`. The architecture is structured so that switching to real API calls requires changing only that service file — no stores, guards, or interceptors need modification.

**Key properties**:

- Tokens are stored in `sessionStorage` (not `localStorage`) — sessions expire when the browser tab is closed
- All API requests automatically carry the Bearer token via an Axios request interceptor
- A 401 response from any endpoint triggers immediate logout and redirect to `/login`
- On page reload, auth state is re-hydrated from `sessionStorage` before the first router guard runs
- Two permission layers: coarse role (route-level) and fine-grained `UserPermissions` (feature-level)

---

## 2. Data Types

Defined in `src/types/index.ts`.

```typescript
export type UserRole = 'ADMIN' | 'MANAGER' | 'STAFF'

export interface UserPermissions {
  temperatureLogging: boolean
  checklists: boolean
  reports: boolean
  deviations: boolean
  userAdmin: boolean
  settings: boolean
}

export interface User {
  id: number
  firstName: string
  lastName: string
  email: string
  role: UserRole
  organizationId?: number    // optional until multi-tenant backend scoping is implemented
  permissions?: UserPermissions  // optional until GET /users/me is integrated
}

export interface LoginCredentials {
  email: string
  password: string
}

export interface AuthResponse {
  token: string
  user: User
}
```

`organizationId` and `permissions` are marked optional on `User` as a pragmatic frontend decision: the mock data provides both, but the type system should not break if the backend omits them during early integration. Once `GET /api/users/me` is fully implemented, both fields should be made required.

---

## 3. Mock Credentials

Defined in `src/services/auth.service.ts`. All three accounts belong to the demo organisation "Everest Sushi & Fusion AS" (organizationId: 1).

| Email | Password | Role | Notes |
|---|---|---|---|
| kari@everestsushi.no | admin123 | ADMIN | Full access. All permissions true. |
| ola@everestsushi.no | leder123 | MANAGER | All operational views. No userAdmin or settings. |
| per@everestsushi.no | ansatt123 | STAFF | Core operational views. No reports, no userAdmin, no settings. |

**Permission matrix**:

| Permission | ADMIN (Kari) | MANAGER (Ola) | STAFF (Per) |
|---|---|---|---|
| temperatureLogging | true | true | true |
| checklists | true | true | true |
| reports | true | true | false |
| deviations | true | true | true |
| userAdmin | true | false | false |
| settings | true | false | false |

Note: STAFF has `reports: false`. Per can log temperatures and complete checklists but cannot access the graph/reports view. This permission is not yet enforced in the UI components (see section 9, Pending Work).

---

## 4. API Endpoints

These are the real Spring Boot endpoints the service layer will call once integrated. All mock services have the real call commented out immediately below the mock return statement.

| Method | Path | Description | Auth required |
|---|---|---|---|
| POST | /api/auth/login | Submit credentials, receive token + user | No |
| POST | /api/auth/refresh | Exchange refresh token for new access token | No (uses refresh token) |
| POST | /api/auth/logout | Invalidate the refresh token server-side | Yes |
| GET | /api/users/me | Fetch full User object with current permissions | Yes |

**Login response shape** (AuthResponse):

```json
{
  "token": "eyJhbGci...",
  "user": {
    "id": 1,
    "firstName": "Kari",
    "lastName": "Larsen",
    "email": "kari@everestsushi.no",
    "role": "ADMIN",
    "organizationId": 1,
    "permissions": {
      "temperatureLogging": true,
      "checklists": true,
      "reports": true,
      "deviations": true,
      "userAdmin": true,
      "settings": true
    }
  }
}
```

The current frontend `AuthResponse` type uses a field named `token`. If the Spring Boot backend returns `accessToken` instead, the service layer should map it: `return { token: data.accessToken, user: data.user }`. Do not change the Pinia store or interceptor — keep `token` as the internal field name.

---

## 5. Auth Store (`src/stores/auth.ts`)

```typescript
export const useAuthStore = defineStore('auth', () => {
  const user  = ref<User | null>(null)
  const token = ref<string | null>(sessionStorage.getItem('token'))

  const isAuthenticated = computed(() => !!token.value && !!user.value)

  // Re-hydration: runs at store creation time, before any router guard
  const stored = sessionStorage.getItem('user')
  if (stored) {
    try { user.value = JSON.parse(stored) } catch { /* noop */ }
  }

  async function login(email: string, password: string): Promise<void> {
    const response = await authService.login({ email, password })
    token.value = response.token
    user.value  = response.user
    sessionStorage.setItem('token', response.token)
    sessionStorage.setItem('user', JSON.stringify(response.user))
  }

  function logout(): void {
    user.value  = null
    token.value = null
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('user')
  }

  return { user, token, isAuthenticated, login, logout }
})
```

**Re-hydration timing**: The top-level `sessionStorage.getItem` calls happen when the store is first accessed by any component or the router guard. Because Pinia stores are lazily instantiated, this is guaranteed to happen before `router.beforeEach` evaluates `auth.isAuthenticated` — preventing a false redirect to `/login` on page reload.

**isAuthenticated**: Requires both `token` and `user` to be non-null. This protects against a corrupted state where a token exists in storage but the user JSON failed to parse (the try/catch in re-hydration leaves `user.value` null in that case).

---

## 6. Axios Interceptors (`src/services/api.ts`)

```typescript
const api = axios.create({
  baseURL: '/api',
  timeout: 10_000,
  headers: { 'Content-Type': 'application/json' }
})

// Request interceptor — inject Bearer token
api.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Response interceptor — handle 401
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)
```

The request interceptor reads from `sessionStorage` directly (not from the Pinia store) to avoid a circular dependency: `api.ts` is imported by services which are imported by stores. Reading from `sessionStorage` keeps the module dependency graph acyclic.

The 401 handler uses `window.location.href` (hard redirect) rather than `router.push()`. This ensures all Pinia store state is also cleared (the page reload destroys in-memory state) and avoids any risk of the router navigating while a component is in a broken auth state.

---

## 7. Router Guards (`src/router/index.ts`)

```typescript
router.beforeEach((to) => {
  const auth = useAuthStore()
  const requiresAuth = to.meta.requiresAuth !== false

  // Default: all routes require auth unless explicitly opted out
  if (requiresAuth && !auth.isAuthenticated) {
    return { name: 'login' }
  }

  // Prevent authenticated users from seeing the login page
  if (to.name === 'login' && auth.isAuthenticated) {
    return { name: 'dashboard' }
  }

  // Admin-only routes
  if (to.meta.requiresAdmin && auth.user?.role !== 'ADMIN') {
    return { name: 'dashboard' }
  }
})
```

**Route meta flags**:

| Meta flag | Type | Default | Effect |
|---|---|---|---|
| `requiresAuth` | `boolean` | `true` (unless set to `false`) | Redirects to /login if not authenticated |
| `requiresAdmin` | `boolean` | — (unset = false) | Redirects to /dashboard if role !== ADMIN |

Routes with `requiresAuth: false`:

- `/login` — the only public route

Routes with `requiresAdmin: true`:

- `/innstillinger` (and all child routes: `/innstillinger/enheter`, `/innstillinger/brukere`, `/innstillinger/org`)

All other authenticated routes rely on the default `requiresAuth: true` behaviour.

**Guard logic note**: The `requiresAuth` check uses `!== false` (not `=== true`). This means a route that does not set `meta.requiresAuth` at all is treated as requiring auth. Only an explicit `false` opts out. This is a safe default — a new route added without a meta flag will require login rather than accidentally becoming public.

---

## 8. Auth Flow — Step by Step

```
1. User opens /dashboard (or any protected route)
   └── router.beforeEach: isAuthenticated = false
       └── redirect to /login

2. User submits login form (email + password)
   └── LoginView calls authStore.login(email, password)
       └── authStore.login calls authService.login({ email, password })
           └── [MOCK] Match against MOCK_USERS array
               └── [REAL] POST /api/auth/login → { token, user }
           └── AuthResponse { token, user } returned
       └── authStore stores:
           ├── token.value = response.token
           ├── user.value  = response.user
           ├── sessionStorage.setItem('token', response.token)
           └── sessionStorage.setItem('user', JSON.stringify(response.user))
   └── LoginView router.push({ name: 'dashboard' })

3. User navigates within the app
   └── Every API call → Axios request interceptor
       └── Reads token from sessionStorage
       └── Adds: Authorization: Bearer {token}

4. Backend returns 401 (token expired or invalid)
   └── Axios response interceptor
       └── sessionStorage.removeItem('token')
       └── sessionStorage.removeItem('user')
       └── window.location.href = '/login'

5. User reloads the page
   └── Pinia store is re-created
   └── useAuthStore init:
       ├── token.value = sessionStorage.getItem('token')  ← re-hydrated
       └── user.value  = JSON.parse(sessionStorage.getItem('user'))  ← re-hydrated
   └── router.beforeEach: isAuthenticated = true
   └── Navigation proceeds to the requested route

6. User clicks "Logg ut"
   └── AppSidebar calls authStore.logout()
       ├── user.value  = null
       ├── token.value = null
       ├── sessionStorage.removeItem('token')
       └── sessionStorage.removeItem('user')
   └── router.push({ name: 'login' })
```

---

## 9. Roles

### ADMIN

Full system access. Can access all views including `/innstillinger` (settings). In `AppSidebar`, the "Administrasjon" group (containing the Innstillinger link) is only rendered when `user.role === 'ADMIN'`. In `AppTabBar`, the "Innst." tab is only shown when `user.role === 'ADMIN'` (`adminOnly: true` flag filtered by `visibleTabs` computed).

### MANAGER

Access to all operational views: dashboard, temperature logging (freezer and fridge), checklists, deviations, graphs, training. Cannot access `/innstillinger`. Permissions `userAdmin: false`, `settings: false`.

### STAFF

Access to core operational views: dashboard, temperature logging, checklists, deviations, training. Cannot access graphs/reports (`reports: false`). Cannot access settings. Cannot manage users.

---

## 10. Permissions System

The application uses a two-layer permissions model.

### Layer 1 — Role (coarse, route-level)

Enforced by the router guard and by conditional rendering in the navigation components. A STAFF user navigating directly to `/innstillinger` will be redirected to `/dashboard` by the router guard (`requiresAdmin: true`).

### Layer 2 — UserPermissions (fine-grained, feature-level)

A `UserPermissions` object is returned as part of the `User` object from `GET /api/users/me` (and embedded in the mock `AuthResponse`). These permissions are configurable per-user by an ADMIN in the `/innstillinger/brukere` tab.

```typescript
interface UserPermissions {
  temperatureLogging: boolean
  checklists: boolean
  reports: boolean
  deviations: boolean
  userAdmin: boolean
  settings: boolean
}
```

**Permission semantics**:

| Permission | Gates access to |
|---|---|
| `temperatureLogging` | FreezerView, FridgeView — logging and reading temperature data |
| `checklists` | ChecklistView — viewing and completing checklist items |
| `reports` | GraphView — temperature history charts and data export |
| `deviations` | DeviationsView — reporting and resolving non-conformances |
| `userAdmin` | UsersTab in settings — creating and editing users |
| `settings` | Entire `/innstillinger` area — units, users, organisation |

**Why permissions come from `/users/me` and not the JWT**:

Permissions are configurable by ADMIN at any time. If permissions were embedded in the JWT, they would be stale until the token expired and was refreshed. By fetching them from `GET /api/users/me`, the system reflects permission changes immediately on the next page load or session. The JWT remains lightweight (identity + role + expiry only).

**Admin management**: The `UsersTab` renders a permission toggle UI for each user. Toggling a permission calls `organizationService.updateUser(id, { permissions: {...} })`. Currently mocked. When the backend is integrated, this will call `PUT /api/users/:id`.

---

## 11. Pending Work

The following auth and permissions features are designed and architecturally accounted for but not yet implemented:

**Refresh token flow**

`POST /api/auth/refresh` is defined in the API contract but not yet implemented on the frontend. When implemented, the Axios 401 response interceptor should attempt a token refresh before clearing storage and redirecting to login. The recommended approach:

1. On 401, call `authService.refresh()` with the refresh token (stored separately in `sessionStorage`)
2. If refresh succeeds, retry the original failed request with the new token
3. If refresh fails (e.g. refresh token also expired), proceed with logout and redirect

**Feature-gating with `user.permissions` in UI components**

`user.permissions` is available in the auth store but is not yet used to conditionally render or disable UI elements. Currently, role alone gates navigation; permissions do not suppress individual features within a page.

Recommended implementation pattern using a composable:

```typescript
// src/composables/usePermission.ts
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import type { keyof UserPermissions } from '@/types'

export function usePermission(key: keyof UserPermissions) {
  const auth = useAuthStore()
  return computed(() => auth.user?.permissions?.[key] ?? false)
}
```

Usage in a view:
```typescript
const canViewReports = usePermission('reports')
// then in template: v-if="canViewReports"
```

**Multi-tenant backend scoping via `organizationId`**

`user.organizationId` is present in the mock data and in the `User` type. When the backend is ready, all API endpoints will derive the organisation scope from the JWT claims. The frontend currently does not pass `organizationId` as a query parameter and should not need to — the backend is responsible for scoping responses to the authenticated user's organisation.
