// ============================================================
// Auth
// ============================================================
export type UserRole = 'ADMIN' | 'SUPERVISOR' | 'MANAGER' | 'STAFF'

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
  organizationName?: string
  primaryLocationId?: number
  primaryLocationName?: string
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
export type UnitType = 'FREEZER' | 'FRIDGE' | 'COOLER' | 'DISPLAY' | 'OTHER'
export type ModuleType = 'IK_MAT' | 'IK_ALKOHOL' | 'SHARED'

export interface Unit {
  id: number
  name: string
  type: UnitType
  targetTemperature: number
  minThreshold: number
  maxThreshold: number
  description: string
  active: boolean
  hasAlert?: boolean
  organizationId?: number
  createdAt?: string
  updatedAt?: string
}

// ============================================================
// Temperature Readings
// ============================================================
export interface RecordedBy {
  id: number
  name: string
}

export interface TemperatureReading {
  id: number
  unitId: number
  unitName?: string
  temperature: number
  targetTemperature?: number
  minThreshold?: number
  maxThreshold?: number
  recordedAt: string
  recordedBy: RecordedBy
  note?: string
  isDeviation: boolean
  createdAt?: string
  updatedAt?: string
}

export interface NewReading {
  unitId: number
  temperature: number
  recordedAt: string
  note?: string
  performedByUserId?: number
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
export type DeviationSeverity = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW'

export interface DeviationComment {
  id: number
  comment: string
  createdById: number
  createdBy: string
  createdAt: string
}

export interface Deviation {
  id: number
  title: string
  description: string
  status: DeviationStatus
  severity: DeviationSeverity
  reportedBy: string
  reportedAt: string
  moduleType: ModuleType
  locationName?: string
  resolvedBy?: string
  resolvedAt?: string
  resolution?: string
  comments?: DeviationComment[]
}

export interface NewDeviation {
  title: string
  description: string
  severity: DeviationSeverity
  moduleType: ModuleType
  performedByUserId?: number
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
  isActive: boolean
  colorBg: string
  colorText: string
  permissions: UserPermissions
  homeLocationName: string | null
}

// ============================================================
// Settings – Storage Units
// ============================================================
export interface SettingsUnit {
  id: number
  name: string
  type: UnitType
  targetTemperature: number
  minThreshold: number
  maxThreshold: number
  description: string
  active: boolean
}

// ============================================================
// Temperature Chart
// ============================================================
export type ChartPeriod = 'WEEK' | 'MONTH'

// ============================================================
// IK-Alkohol
// ============================================================

export type AgeVerificationOutcome = 'APPROVED' | 'DENIED' | 'UNSURE'

export interface AlderskontrollEntry {
  id: number
  recordedAt: string        // ISO 8601
  recordedBy: string        // staff member name
  outcome: AgeVerificationOutcome
  note?: string
}

export interface NewAlderskontrollEntry {
  outcome: AgeVerificationOutcome
  note?: string
  recordedAt?: string       // defaults to now if omitted
}

export type AlkoholIncidentType =
  | 'NEKTET_SERVERING'
  | 'BERUSET_GJEST'
  | 'POLITIKONTAKT'
  | 'ANNET'

export interface AlkoholIncident {
  id: number
  incidentType: AlkoholIncidentType
  description: string
  occurredAt: string        // ISO 8601
  reportedBy: string
  followUpRequired: boolean
  relatedDeviationId?: number
}

export interface NewAlkoholIncident {
  incidentType: AlkoholIncidentType
  description: string
  followUpRequired: boolean
  occurredAt?: string       // defaults to now
}

export interface AlkoholStats {
  ageChecksToday: number
  incidentsThisWeek: number
  checklistCompletionPct: number
}
