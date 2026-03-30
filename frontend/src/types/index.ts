// ============================================================
// Auth
// ============================================================
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
  organizationId?: number
  permissions?: UserPermissions
}

export interface LoginCredentials {
  email: string
  password: string
}

export interface AuthResponse {
  token: string
  user: User
}

// ============================================================
// Units
// ============================================================
export type UnitType = 'FREEZER' | 'FRIDGE' | 'COOLER' | 'OTHER'
export type ModuleType = 'IK_MAT' | 'IK_ALKOHOL'

export interface Unit {
  id: number
  name: string
  type: UnitType
  targetTemp: number
  minTemp: number
  maxTemp: number
  contents: string
  active: boolean
  hasAlert?: boolean
}

// ============================================================
// Temperature Readings
// ============================================================
export interface TemperatureReading {
  id: number
  unitId: number
  temperature: number
  recordedAt: string
  recordedBy: string
  note?: string
  isOutOfRange: boolean
}

export interface NewReading {
  unitId: number
  temperature: number
  recordedAt: string
  note?: string
}

// ============================================================
// Checklists
// ============================================================
export type ChecklistFrequency = 'DAILY' | 'WEEKLY' | 'MONTHLY'

export interface ChecklistItem {
  id: number
  text: string
  completed: boolean
  completedBy?: string
  completedAt?: string
}

export interface Checklist {
  id: number
  title: string
  frequency: ChecklistFrequency
  items: ChecklistItem[]
  completedBy?: string
  completedAt?: string
  moduleType: ModuleType
}

// ============================================================
// Deviations
// ============================================================
export type DeviationStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED'
export type DeviationSeverity = 'CRITICAL' | 'MEDIUM' | 'LOW'

export interface Deviation {
  id: number
  title: string
  description: string
  status: DeviationStatus
  severity: DeviationSeverity
  reportedBy: string
  reportedAt: string
  moduleType: ModuleType
  resolvedAt?: string
  resolution?: string
}

export interface NewDeviation {
  title: string
  description: string
  severity: DeviationSeverity
  moduleType: ModuleType
}

// ============================================================
// Dashboard
// ============================================================
export interface DashboardStats {
  tasksCompleted: number
  tasksTotal: number
  tempAlerts: number
  openDeviations: number
  compliancePercent: number
}

export interface DashboardTask {
  id: number
  name: string
  status: 'COMPLETED' | 'PENDING' | 'NOT_STARTED'
  completedBy?: string
  completedAt?: string
}

export interface DashboardAlert {
  id: number
  message: string
  type: 'danger' | 'warning' | 'info'
  time: string
}

// ============================================================
// Training / Documents
// ============================================================
export type DocumentFileType = 'PDF' | 'DOC' | 'VID'

export interface TrainingDocument {
  id: number
  title: string
  subtitle: string
  type: DocumentFileType
  actionLabel: string
  colorBg: string
  colorText: string
}

export type CertificationStatus = 'COMPLETE' | 'EXPIRING' | 'MISSING'

export interface EmployeeCertification {
  id: number
  name: string
  status: CertificationStatus
  expiredCount: number
  missingCount: number
}

// ============================================================
// Organization
// ============================================================
export interface Organization {
  name: string
  orgNumber: string
  industry: string
  address: string
  modules: {
    ikMat: boolean
    ikAlkohol: boolean
  }
  notifications: {
    emailOnTempDeviation: boolean
    dailySummaryToManagers: boolean
    smsOnCritical: boolean
  }
}

// ============================================================
// Settings – Users
// ============================================================
export interface SettingsUser {
  id: number
  firstName: string
  lastName: string
  email: string
  role: UserRole
  active: boolean
  colorBg: string
  colorText: string
  permissions: UserPermissions
}

// ============================================================
// Settings – Storage Units
// ============================================================
export interface SettingsUnit {
  id: number
  name: string
  type: UnitType
  targetTemp: number
  minTemp: number
  maxTemp: number
  contents: string
  active: boolean
}

// ============================================================
// Temperature Chart
// ============================================================
export type ChartPeriod = 'WEEK' | 'MONTH'
