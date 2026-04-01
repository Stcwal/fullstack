# Plan 1: Foundation + IK-Alkohol Module

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Establish the CSS theming system, `usePermission` composable, and the complete IK-Alkohol module (Alderskontroll-logg, Alkohol-sjekklister, Hendelseslogg) including navigation, routing, and dashboard block.

**Architecture:** CSS custom properties with a single `--ik-alkohol-hue` integer drive all module colours. IK-Alkohol is a single route (`/alkohol`) with three child tabs reusing existing Pinia patterns. Navigation is extended in both sidebar (desktop) and tab bar (iPad).

**Tech Stack:** Vue 3 `<script setup lang="ts">`, Pinia, Vue Router 4, Vitest + @vue/test-utils, pure scoped CSS.

**Pre-flight:** Read `frontend/documentation/00-requirements.md` and `frontend/documentation/07-design-spec-2026-03-31.md` before starting.

**This is Plan 1 of 3.**
- Plan 2: iPad UX overhaul (temperature cards, WCAG, empty/loading states, login)
- Plan 3: Backend wiring + remaining features (permissions UI, document upload, report export, checklist templates, deviation comments)

---

## File Map

| Action | Path | Responsibility |
|--------|------|----------------|
| Modify | `src/assets/main.css` | Add hue variables + `.module-ik-alkohol` token set |
| Modify | `src/types/index.ts` | Add `AlderskontrollEntry`, `AlkoholIncident`, `AlkoholStats`, `AgeVerificationOutcome` |
| Create | `src/composables/usePermission.ts` | `can(key)` helper reading auth store |
| Create | `src/stores/alkohol.ts` | Pinia store: alderskontroll list, incidents list, loading states |
| Create | `src/services/alkohol.service.ts` | Mock service with TODO comments for real endpoints |
| Create | `src/components/ModuleBanner.vue` | Amber banner strip shown at top of every IK-Alkohol view |
| Create | `src/views/alkohol/AlkoholView.vue` | Container: module banner + sub-tab nav + `<router-view>` |
| Create | `src/views/alkohol/AlderskontrollTab.vue` | Age verification log + inline entry form |
| Create | `src/views/alkohol/AlkoholSjekklisterTab.vue` | Thin wrapper: loads ChecklistView with `moduleType='IK_ALKOHOL'` |
| Create | `src/views/alkohol/HendelsesloggTab.vue` | Incident log + inline report form |
| Modify | `src/router/index.ts` | Add `/alkohol` route with three named children |
| Modify | `src/components/layout/AppSidebar.vue` | Add IK-Alkohol section below divider |
| Modify | `src/components/layout/AppTabBar.vue` | Restructure to 5 tabs + Mer bottom drawer |
| Modify | `src/views/DashboardView.vue` | Add IK-Alkohol summary block (hidden when module disabled) |
| Create | `src/composables/__tests__/usePermission.test.ts` | Unit tests for permission composable |
| Create | `vitest.config.ts` | Vitest configuration |

---

## Task 0: Install Vitest and Vue Test Utils

No test framework exists yet. This task sets it up.

**Files:**
- Modify: `frontend/package.json`
- Create: `frontend/vitest.config.ts`

- [ ] **Step 1: Install test dependencies**

```bash
cd frontend
npm install --save-dev vitest @vue/test-utils @vitejs/plugin-vue jsdom @vitest/coverage-v8
```

- [ ] **Step 2: Create vitest config**

Create `frontend/vitest.config.ts`:

```typescript
import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  test: {
    environment: 'jsdom',
    globals: true,
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html'],
      exclude: ['node_modules/', 'src/main.ts']
    }
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  }
})
```

- [ ] **Step 3: Add test scripts to package.json**

Add to the `"scripts"` block:

```json
"test": "vitest run",
"test:watch": "vitest",
"test:coverage": "vitest run --coverage"
```

- [ ] **Step 4: Verify setup works**

Create `src/composables/__tests__/.gitkeep` so the directory exists, then run:

```bash
npm run test
```

Expected: "No test files found" (not an error — just no tests yet).

- [ ] **Step 5: Commit**

```bash
git add vitest.config.ts package.json package-lock.json src/composables/__tests__/
git commit -m "chore: add vitest + vue test utils for frontend testing"
```

---

## Task 1: CSS Theming System

**Files:**
- Modify: `src/assets/main.css`

- [ ] **Step 1: Add hue variables and module token sets to `src/assets/main.css`**

Find the `:root {` block (it already exists). Add these lines inside it:

```css
/* Module hue values — change one integer to retheme the entire module */
--ik-mat-hue: 142;
--ik-alkohol-hue: 38;
```

Then add these two new rule blocks **after** the `:root` block:

```css
/* IK-Mat module tokens (currently just aliases to existing green tokens) */
.module-ik-mat {
  --mod-bg:     hsl(var(--ik-mat-hue), 100%, 97%);
  --mod-border: hsl(var(--ik-mat-hue), 60%,  75%);
  --mod-accent: hsl(var(--ik-mat-hue), 70%,  40%);
  --mod-text:   hsl(var(--ik-mat-hue), 80%,  20%);
  --mod-dark:   hsl(var(--ik-mat-hue), 90%,  12%);
}

/* IK-Alkohol module tokens — amber by default */
.module-ik-alkohol {
  --mod-bg:     hsl(var(--ik-alkohol-hue), 100%, 97%);
  --mod-border: hsl(var(--ik-alkohol-hue), 90%,  70%);
  --mod-accent: hsl(var(--ik-alkohol-hue), 85%,  45%);
  --mod-text:   hsl(var(--ik-alkohol-hue), 80%,  25%);
  --mod-dark:   hsl(var(--ik-alkohol-hue), 90%,  15%);
}
```

- [ ] **Step 2: Verify build still passes**

```bash
npm run build
```

Expected: no errors.

- [ ] **Step 3: Commit**

```bash
git add src/assets/main.css
git commit -m "feat: add CSS hue-variable module theming system for IK-Mat and IK-Alkohol"
```

---

## Task 2: TypeScript Types for IK-Alkohol

**Files:**
- Modify: `src/types/index.ts`

- [ ] **Step 1: Add IK-Alkohol types to end of `src/types/index.ts`**

Append after the last existing export:

```typescript
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
```

- [ ] **Step 2: Verify TypeScript compiles**

```bash
npm run type-check
```

Expected: no errors.

- [ ] **Step 3: Commit**

```bash
git add src/types/index.ts
git commit -m "feat: add IK-Alkohol TypeScript types"
```

---

## Task 3: usePermission Composable

**Files:**
- Create: `src/composables/usePermission.ts`
- Create: `src/composables/__tests__/usePermission.test.ts`

- [ ] **Step 1: Write the failing test**

Create `src/composables/__tests__/usePermission.test.ts`:

```typescript
import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { usePermission } from '../usePermission'
import { useAuthStore } from '@/stores/auth'

describe('usePermission', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('returns false for any permission when no user is logged in', () => {
    const { can } = usePermission()
    expect(can('temperatureLogging')).toBe(false)
    expect(can('settings')).toBe(false)
  })

  it('returns true when user has the permission', () => {
    const auth = useAuthStore()
    auth.user = {
      id: 1, firstName: 'Kari', lastName: 'Larsen',
      email: 'kari@test.no', role: 'ADMIN',
      permissions: {
        temperatureLogging: true, checklists: true, reports: true,
        deviations: true, userAdmin: true, settings: true
      }
    }
    const { can } = usePermission()
    expect(can('temperatureLogging')).toBe(true)
    expect(can('settings')).toBe(true)
  })

  it('returns false when user exists but permission is false', () => {
    const auth = useAuthStore()
    auth.user = {
      id: 2, firstName: 'Per', lastName: 'Hansen',
      email: 'per@test.no', role: 'STAFF',
      permissions: {
        temperatureLogging: true, checklists: true, reports: false,
        deviations: true, userAdmin: false, settings: false
      }
    }
    const { can } = usePermission()
    expect(can('reports')).toBe(false)
    expect(can('userAdmin')).toBe(false)
  })

  it('returns false when user has no permissions object', () => {
    const auth = useAuthStore()
    auth.user = {
      id: 3, firstName: 'Ola', lastName: 'Berg',
      email: 'ola@test.no', role: 'MANAGER'
    }
    const { can } = usePermission()
    expect(can('temperatureLogging')).toBe(false)
  })
})
```

- [ ] **Step 2: Run test to verify it fails**

```bash
npm run test
```

Expected: FAIL — "Cannot find module '../usePermission'"

- [ ] **Step 3: Create the composable**

Create `src/composables/usePermission.ts`:

```typescript
import { useAuthStore } from '@/stores/auth'
import type { UserPermissions } from '@/types'

export function usePermission() {
  const auth = useAuthStore()

  function can(key: keyof UserPermissions): boolean {
    return auth.user?.permissions?.[key] ?? false
  }

  return { can }
}
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
npm run test
```

Expected: 4 tests pass.

- [ ] **Step 5: Commit**

```bash
git add src/composables/usePermission.ts src/composables/__tests__/usePermission.test.ts
git commit -m "feat: add usePermission composable with tests"
```

---

## Task 4: Alkohol Service and Store

**Files:**
- Create: `src/services/alkohol.service.ts`
- Create: `src/stores/alkohol.ts`

- [ ] **Step 1: Create the mock service**

Create `src/services/alkohol.service.ts`:

```typescript
import type {
  AlderskontrollEntry,
  NewAlderskontrollEntry,
  AlkoholIncident,
  NewAlkoholIncident,
  AlkoholStats
} from '@/types'

// Mock data
const mockEntries: AlderskontrollEntry[] = [
  {
    id: 1,
    recordedAt: new Date(Date.now() - 3600000).toISOString(),
    recordedBy: 'Kari Larsen',
    outcome: 'APPROVED'
  },
  {
    id: 2,
    recordedAt: new Date(Date.now() - 7200000).toISOString(),
    recordedBy: 'Per Hansen',
    outcome: 'DENIED',
    note: 'Manglende legitimasjon'
  }
]

const mockIncidents: AlkoholIncident[] = [
  {
    id: 1,
    incidentType: 'BERUSET_GJEST',
    description: 'Gjest virket beruset, ble bedt om å forlate lokalet.',
    occurredAt: new Date(Date.now() - 86400000).toISOString(),
    reportedBy: 'Kari Larsen',
    followUpRequired: false
  }
]

export const alkoholService = {
  // TODO: Replace mock with real API call — GET /api/alkohol/age-verifications
  async getAlderskontrollEntries(): Promise<AlderskontrollEntry[]> {
    await new Promise(r => setTimeout(r, 300))
    return [...mockEntries]
  },

  // TODO: Replace mock with real API call — POST /api/alkohol/age-verifications
  async createAlderskontrollEntry(data: NewAlderskontrollEntry): Promise<AlderskontrollEntry> {
    await new Promise(r => setTimeout(r, 200))
    const entry: AlderskontrollEntry = {
      id: Date.now(),
      recordedAt: data.recordedAt ?? new Date().toISOString(),
      recordedBy: 'Innlogget bruker', // TODO: Replace with auth.user name
      outcome: data.outcome,
      note: data.note
    }
    mockEntries.unshift(entry)
    return entry
  },

  // TODO: Replace mock with real API call — GET /api/alkohol/incidents
  async getIncidents(): Promise<AlkoholIncident[]> {
    await new Promise(r => setTimeout(r, 300))
    return [...mockIncidents]
  },

  // TODO: Replace mock with real API call — POST /api/alkohol/incidents
  async createIncident(data: NewAlkoholIncident): Promise<AlkoholIncident> {
    await new Promise(r => setTimeout(r, 200))
    const incident: AlkoholIncident = {
      id: Date.now(),
      incidentType: data.incidentType,
      description: data.description,
      occurredAt: data.occurredAt ?? new Date().toISOString(),
      reportedBy: 'Innlogget bruker', // TODO: Replace with auth.user name
      followUpRequired: data.followUpRequired
    }
    mockIncidents.unshift(incident)
    return incident
  },

  // TODO: Replace mock with real API call — GET /api/alkohol/stats (or derive from dashboard)
  async getStats(): Promise<AlkoholStats> {
    await new Promise(r => setTimeout(r, 200))
    return {
      ageChecksToday: mockEntries.filter(e =>
        new Date(e.recordedAt).toDateString() === new Date().toDateString()
      ).length,
      incidentsThisWeek: mockIncidents.length,
      checklistCompletionPct: 75
    }
  }
}
```

- [ ] **Step 2: Create the Pinia store**

Create `src/stores/alkohol.ts`:

```typescript
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { alkoholService } from '@/services/alkohol.service'
import type {
  AlderskontrollEntry,
  NewAlderskontrollEntry,
  AlkoholIncident,
  NewAlkoholIncident,
  AlkoholStats
} from '@/types'

export const useAlkoholStore = defineStore('alkohol', () => {
  const entries = ref<AlderskontrollEntry[]>([])
  const incidents = ref<AlkoholIncident[]>([])
  const stats = ref<AlkoholStats | null>(null)
  const loading = ref(false)
  const saving = ref(false)
  const error = ref<string | null>(null)

  async function fetchEntries() {
    loading.value = true
    error.value = null
    try {
      entries.value = await alkoholService.getAlderskontrollEntries()
    } catch {
      error.value = 'Kunne ikke laste alderskontroll-logg.'
    } finally {
      loading.value = false
    }
  }

  async function addEntry(data: NewAlderskontrollEntry) {
    saving.value = true
    try {
      const entry = await alkoholService.createAlderskontrollEntry(data)
      entries.value.unshift(entry)
    } finally {
      saving.value = false
    }
  }

  async function fetchIncidents() {
    loading.value = true
    error.value = null
    try {
      incidents.value = await alkoholService.getIncidents()
    } catch {
      error.value = 'Kunne ikke laste hendelseslogg.'
    } finally {
      loading.value = false
    }
  }

  async function addIncident(data: NewAlkoholIncident) {
    saving.value = true
    try {
      const incident = await alkoholService.createIncident(data)
      incidents.value.unshift(incident)
    } finally {
      saving.value = false
    }
  }

  async function fetchStats() {
    stats.value = await alkoholService.getStats()
  }

  return {
    entries, incidents, stats, loading, saving, error,
    fetchEntries, addEntry, fetchIncidents, addIncident, fetchStats
  }
})
```

- [ ] **Step 3: Verify TypeScript**

```bash
npm run type-check
```

Expected: no errors.

- [ ] **Step 4: Commit**

```bash
git add src/services/alkohol.service.ts src/stores/alkohol.ts
git commit -m "feat: add alkohol service (mock) and Pinia store"
```

---

## Task 5: ModuleBanner Component

**Files:**
- Create: `src/components/ModuleBanner.vue`

- [ ] **Step 1: Create the component**

Create `src/components/ModuleBanner.vue`:

```vue
<script setup lang="ts">
import type { ModuleType } from '@/types'

const props = defineProps<{
  module: ModuleType
}>()

const labels: Record<ModuleType, string> = {
  IK_MAT: 'IK-Mat',
  IK_ALKOHOL: 'IK-Alkohol'
}
</script>

<template>
  <div
    class="module-banner"
    :class="module === 'IK_ALKOHOL' ? 'module-banner--alkohol' : 'module-banner--mat'"
    role="banner"
    :aria-label="`Aktiv modul: ${labels[module]}`"
  >
    <span class="module-banner__dot" aria-hidden="true"></span>
    <span class="module-banner__label">{{ labels[module] }}</span>
  </div>
</template>

<style scoped>
.module-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  border-bottom: 2px solid;
}

.module-banner__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

/* IK-Mat variant */
.module-banner--mat {
  background: var(--color-success-bg, #f0fdf4);
  color: var(--color-success-dark, #166534);
  border-color: var(--color-success-border, #bbf7d0);
}
.module-banner--mat .module-banner__dot {
  background: var(--color-success, #16A34A);
}

/* IK-Alkohol variant — uses module tokens from .module-ik-alkohol parent,
   but also works standalone via fallback values */
.module-banner--alkohol {
  background: hsl(var(--ik-alkohol-hue, 38), 100%, 97%);
  color: hsl(var(--ik-alkohol-hue, 38), 80%, 25%);
  border-color: hsl(var(--ik-alkohol-hue, 38), 90%, 70%);
}
.module-banner--alkohol .module-banner__dot {
  background: hsl(var(--ik-alkohol-hue, 38), 85%, 45%);
}
</style>
```

- [ ] **Step 2: Verify build**

```bash
npm run build
```

Expected: no errors.

- [ ] **Step 3: Commit**

```bash
git add src/components/ModuleBanner.vue
git commit -m "feat: add ModuleBanner component for module identity display"
```

---

## Task 6: AlkoholView Container and Router

**Files:**
- Create: `src/views/alkohol/AlkoholView.vue`
- Modify: `src/router/index.ts`

- [ ] **Step 1: Create the AlkoholView container**

Create `src/views/alkohol/AlkoholView.vue`:

```vue
<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import ModuleBanner from '@/components/ModuleBanner.vue'

const route = useRoute()
const router = useRouter()

const tabs = [
  { name: 'alkohol-alderskontroll', label: 'Alderskontroll' },
  { name: 'alkohol-sjekklister',    label: 'Sjekklister' },
  { name: 'alkohol-hendelser',      label: 'Hendelseslogg' }
]

function isActive(routeName: string): boolean {
  return route.name === routeName
}

function navigate(routeName: string) {
  router.push({ name: routeName })
}
</script>

<template>
  <div class="alkohol-view module-ik-alkohol">
    <ModuleBanner module="IK_ALKOHOL" />

    <nav class="alkohol-tabs" aria-label="IK-Alkohol navigasjon">
      <button
        v-for="tab in tabs"
        :key="tab.name"
        class="alkohol-tab"
        :class="{ 'alkohol-tab--active': isActive(tab.name) }"
        :aria-current="isActive(tab.name) ? 'page' : undefined"
        @click="navigate(tab.name)"
      >
        {{ tab.label }}
      </button>
    </nav>

    <main class="alkohol-content">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.alkohol-view {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;
}

.alkohol-tabs {
  display: flex;
  gap: 0;
  border-bottom: 2px solid #e2e8f0;
  padding: 0 16px;
  background: #fff;
}

.alkohol-tab {
  padding: 12px 18px;
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  background: none;
  border: none;
  border-bottom: 3px solid transparent;
  margin-bottom: -2px;
  cursor: pointer;
  transition: color 0.15s, border-color 0.15s;
}

.alkohol-tab:hover {
  color: hsl(var(--ik-alkohol-hue, 38), 80%, 30%);
}

.alkohol-tab--active {
  color: hsl(var(--ik-alkohol-hue, 38), 80%, 25%);
  border-bottom-color: hsl(var(--ik-alkohol-hue, 38), 85%, 45%);
  font-weight: 600;
}

.alkohol-tab:focus-visible {
  outline: 2px solid hsl(var(--ik-alkohol-hue, 38), 85%, 45%);
  outline-offset: -2px;
  border-radius: 4px;
}

.alkohol-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px 16px;
}
</style>
```

- [ ] **Step 2: Add IK-Alkohol routes to `src/router/index.ts`**

In the routes array, add after the `/opplaering` route and before `/innstillinger`:

```typescript
{
  path: '/alkohol',
  component: () => import('@/views/alkohol/AlkoholView.vue'),
  meta: { requiresAuth: true },
  children: [
    { path: '', redirect: 'alderskontroll' },
    {
      path: 'alderskontroll',
      name: 'alkohol-alderskontroll',
      component: () => import('@/views/alkohol/AlderskontrollTab.vue')
    },
    {
      path: 'sjekklister',
      name: 'alkohol-sjekklister',
      component: () => import('@/views/alkohol/AlkoholSjekklisterTab.vue')
    },
    {
      path: 'hendelser',
      name: 'alkohol-hendelser',
      component: () => import('@/views/alkohol/HendelsesloggTab.vue')
    }
  ]
},
```

- [ ] **Step 3: Create stub tab files so the router resolves**

Create `src/views/alkohol/AlderskontrollTab.vue` (stub — full implementation in Task 7):

```vue
<template><div>Alderskontroll laster...</div></template>
```

Create `src/views/alkohol/AlkoholSjekklisterTab.vue` (stub — full implementation in Task 8):

```vue
<template><div>Sjekklister laster...</div></template>
```

Create `src/views/alkohol/HendelsesloggTab.vue` (stub — full implementation in Task 9):

```vue
<template><div>Hendelseslogg laster...</div></template>
```

- [ ] **Step 4: Verify build and type-check**

```bash
npm run type-check && npm run build
```

Expected: no errors.

- [ ] **Step 5: Commit**

```bash
git add src/views/alkohol/ src/router/index.ts
git commit -m "feat: add AlkoholView container with sub-tab nav and router routes"
```

---

## Task 7: AlderskontrollTab — Age Verification Log

**Files:**
- Modify: `src/views/alkohol/AlderskontrollTab.vue` (replace stub)

- [ ] **Step 1: Implement the full component**

Replace `src/views/alkohol/AlderskontrollTab.vue` with:

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAlkoholStore } from '@/stores/alkohol'
import type { AgeVerificationOutcome } from '@/types'

const store = useAlkoholStore()

const showForm = ref(false)
const selectedOutcome = ref<AgeVerificationOutcome>('APPROVED')
const note = ref('')
const saving = ref(false)

onMounted(() => store.fetchEntries())

const outcomeLabels: Record<AgeVerificationOutcome, string> = {
  APPROVED: 'Godkjent',
  DENIED:   'Nektet',
  UNSURE:   'Usikker'
}

const outcomeClasses: Record<AgeVerificationOutcome, string> = {
  APPROVED: 'badge--success',
  DENIED:   'badge--danger',
  UNSURE:   'badge--warning'
}

function formatTime(iso: string): string {
  return new Date(iso).toLocaleTimeString('nb-NO', { hour: '2-digit', minute: '2-digit' })
}

function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString('nb-NO', { day: 'numeric', month: 'short' })
}

async function submit() {
  if (saving.value) return
  saving.value = true
  try {
    await store.addEntry({
      outcome: selectedOutcome.value,
      note: note.value.trim() || undefined
    })
    note.value = ''
    selectedOutcome.value = 'APPROVED'
    showForm.value = false
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <section aria-label="Alderskontroll-logg">

    <div class="section-header">
      <h1 class="section-title">Alderskontroll-logg</h1>
      <button class="btn btn--primary" @click="showForm = !showForm" :aria-expanded="showForm">
        {{ showForm ? 'Avbryt' : '+ Registrer kontroll' }}
      </button>
    </div>

    <!-- Inline entry form -->
    <div v-if="showForm" class="entry-form card" role="form" aria-label="Registrer alderskontroll">
      <div class="form-group">
        <label class="form-label" id="outcome-label">Resultat</label>
        <div class="outcome-buttons" role="group" aria-labelledby="outcome-label">
          <button
            v-for="(label, key) in outcomeLabels"
            :key="key"
            class="outcome-btn"
            :class="{ 'outcome-btn--active': selectedOutcome === key }"
            :aria-pressed="selectedOutcome === key"
            @click="selectedOutcome = key as AgeVerificationOutcome"
          >
            {{ label }}
          </button>
        </div>
      </div>

      <div class="form-group">
        <label for="ald-note" class="form-label">Merknad (valgfritt)</label>
        <input
          id="ald-note"
          v-model="note"
          type="text"
          class="form-input"
          placeholder="F.eks. manglende legitimasjon"
          maxlength="200"
        />
      </div>

      <button
        class="btn btn--save"
        :disabled="saving"
        @click="submit"
        aria-busy="saving"
      >
        {{ saving ? 'Lagrer...' : 'Lagre' }}
      </button>
    </div>

    <!-- Entry list -->
    <div v-if="store.loading" class="loading-text" aria-live="polite">Laster logg...</div>

    <ul v-else class="entry-list" aria-label="Registrerte alderskontroller">
      <li
        v-for="entry in store.entries"
        :key="entry.id"
        class="entry-card"
      >
        <div class="entry-time">
          <span class="entry-time__time">{{ formatTime(entry.recordedAt) }}</span>
          <span class="entry-time__date">{{ formatDate(entry.recordedAt) }}</span>
        </div>
        <div class="entry-body">
          <span class="badge" :class="outcomeClasses[entry.outcome]">
            {{ outcomeLabels[entry.outcome] }}
          </span>
          <span class="entry-staff">{{ entry.recordedBy }}</span>
          <p v-if="entry.note" class="entry-note">{{ entry.note }}</p>
        </div>
      </li>
    </ul>

    <p v-if="!store.loading && store.entries.length === 0" class="empty-state">
      Ingen kontroller registrert i dag. Trykk "+ Registrer kontroll" for å logge.
    </p>

  </section>
</template>

<style scoped>
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.section-title { font-size: 18px; font-weight: 700; color: #0f172a; }

.entry-form {
  margin-bottom: 20px;
  padding: 16px;
  border: 2px solid hsl(var(--ik-alkohol-hue, 38), 90%, 70%);
  border-radius: 12px;
  background: hsl(var(--ik-alkohol-hue, 38), 100%, 98%);
}

.form-label { display: block; font-size: 13px; font-weight: 600; color: #0f172a; margin-bottom: 6px; }

.outcome-buttons { display: flex; gap: 8px; }
.outcome-btn {
  flex: 1;
  padding: 10px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  background: #fff;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  color: #334155;
  transition: border-color 0.1s, background 0.1s;
}
.outcome-btn--active {
  border-color: hsl(var(--ik-alkohol-hue, 38), 85%, 45%);
  background: hsl(var(--ik-alkohol-hue, 38), 100%, 96%);
  color: hsl(var(--ik-alkohol-hue, 38), 80%, 25%);
  font-weight: 700;
}
.outcome-btn:focus-visible { outline: 2px solid hsl(var(--ik-alkohol-hue, 38), 85%, 45%); outline-offset: 2px; }

.form-input {
  width: 100%;
  padding: 10px 12px;
  border: 1.5px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  color: #0f172a;
}
.form-input:focus { outline: 2px solid hsl(var(--ik-alkohol-hue, 38), 85%, 45%); outline-offset: 1px; border-color: transparent; }

.btn--save {
  width: 100%;
  margin-top: 12px;
  padding: 14px;
  background: hsl(var(--ik-alkohol-hue, 38), 85%, 45%);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  min-height: 44px;
}
.btn--save:disabled { opacity: 0.6; cursor: not-allowed; }
.btn--save:focus-visible { outline: 2px solid #0f172a; outline-offset: 2px; }

.entry-list { list-style: none; padding: 0; display: flex; flex-direction: column; gap: 8px; }
.entry-card {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 12px 14px;
}
.entry-time { display: flex; flex-direction: column; align-items: center; min-width: 42px; }
.entry-time__time { font-size: 14px; font-weight: 700; color: #0f172a; }
.entry-time__date { font-size: 10px; color: #94a3b8; }
.entry-body { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.entry-staff { font-size: 12px; color: #475569; }
.entry-note { font-size: 12px; color: #64748b; margin: 0; }

.badge { display: inline-block; padding: 3px 10px; border-radius: 99px; font-size: 11px; font-weight: 700; }
.badge--success { background: #f0fdf4; color: #166534; }
.badge--danger  { background: #fef2f2; color: #991b1b; }
.badge--warning { background: #fffbeb; color: #92400e; }

.loading-text { color: #94a3b8; font-size: 14px; padding: 20px 0; text-align: center; }
.empty-state  { color: #94a3b8; font-size: 14px; padding: 32px 0; text-align: center; }
</style>
```

- [ ] **Step 2: Verify build**

```bash
npm run build
```

Expected: no errors.

- [ ] **Step 3: Commit**

```bash
git add src/views/alkohol/AlderskontrollTab.vue
git commit -m "feat: implement AlderskontrollTab with inline entry form and log list"
```

---

## Task 8: AlkoholSjekklisterTab

**Files:**
- Modify: `src/views/alkohol/AlkoholSjekklisterTab.vue` (replace stub)

- [ ] **Step 1: Check how ChecklistView currently receives its data**

Read `src/views/ChecklistView.vue` lines 1–40 to understand what props/store it uses before writing this wrapper.

- [ ] **Step 2: Implement the wrapper**

The existing `useChecklistsStore` and `ChecklistView` handle both `IK_MAT` and `IK_ALKOHOL` because `moduleType` is already on the `Checklist` type. This tab fetches only `IK_ALKOHOL` checklists.

Replace `src/views/alkohol/AlkoholSjekklisterTab.vue` with:

```vue
<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useChecklistsStore } from '@/stores/checklists'
import AppProgressBar from '@/components/AppProgressBar.vue'

const store = useChecklistsStore()

// Filter to only IK_ALKOHOL checklists
const alkoholChecklists = computed(() =>
  store.checklists.filter(c => c.moduleType === 'IK_ALKOHOL')
)

const completedCount = computed(() =>
  alkoholChecklists.value.reduce((acc, cl) =>
    acc + cl.items.filter(i => i.completed).length, 0)
)

const totalCount = computed(() =>
  alkoholChecklists.value.reduce((acc, cl) => acc + cl.items.length, 0)
)

onMounted(() => store.fetchAll())
</script>

<template>
  <section aria-label="Alkohol-sjekklister">
    <h1 class="section-title">Alkohol-sjekklister</h1>

    <div v-if="totalCount > 0" class="progress-summary" aria-label="Fremdrift">
      <AppProgressBar :value="completedCount" :max="totalCount" />
      <p class="progress-text">{{ completedCount }} av {{ totalCount }} fullført</p>
    </div>

    <div v-if="store.loading" class="loading-text" aria-live="polite">Laster sjekklister...</div>

    <p v-else-if="alkoholChecklists.length === 0" class="empty-state">
      Ingen alkohol-sjekklister konfigurert. En administrator kan opprette maler under Innstillinger.
    </p>

    <div v-else class="checklist-list">
      <div
        v-for="checklist in alkoholChecklists"
        :key="checklist.id"
        class="checklist-card card"
        :aria-label="checklist.title"
      >
        <h2 class="checklist-title">{{ checklist.title }}</h2>
        <ul class="item-list" :aria-label="`Elementer i ${checklist.title}`">
          <li
            v-for="item in checklist.items"
            :key="item.id"
            class="checklist-item"
            :class="{ 'checklist-item--done': item.completed }"
          >
            <button
              class="checklist-check"
              :aria-pressed="item.completed"
              :aria-label="`${item.completed ? 'Fjern avkryssing' : 'Kryss av'}: ${item.text}`"
              @click="store.toggleItem(checklist.id, item.id)"
            >
              <span class="check-icon" aria-hidden="true">{{ item.completed ? '✓' : '' }}</span>
            </button>
            <span class="item-text" :class="{ 'item-text--done': item.completed }">
              {{ item.text }}
            </span>
            <span v-if="item.completedBy" class="item-meta">{{ item.completedBy }}</span>
          </li>
        </ul>
      </div>
    </div>
  </section>
</template>

<style scoped>
.section-title { font-size: 18px; font-weight: 700; color: #0f172a; margin-bottom: 16px; }
.progress-summary { margin-bottom: 16px; }
.progress-text { font-size: 12px; color: #64748b; margin-top: 4px; }
.loading-text, .empty-state { color: #94a3b8; font-size: 14px; padding: 32px 0; text-align: center; }
.checklist-list { display: flex; flex-direction: column; gap: 12px; }
.checklist-card { padding: 16px; }
.checklist-title { font-size: 15px; font-weight: 700; color: #0f172a; margin-bottom: 10px; }
.item-list { list-style: none; padding: 0; display: flex; flex-direction: column; gap: 4px; }
.checklist-item { display: flex; align-items: center; gap: 10px; padding: 8px 10px; border-radius: 8px; }
.checklist-item--done { background: hsl(var(--ik-alkohol-hue, 38), 100%, 97%); }
.checklist-check {
  width: 22px; height: 22px; min-width: 22px; min-height: 22px;
  border-radius: 5px; border: 2px solid #cbd5e1;
  background: #fff; display: flex; align-items: center; justify-content: center;
  cursor: pointer; color: #fff; font-size: 12px; font-weight: 700;
}
.checklist-item--done .checklist-check {
  background: hsl(var(--ik-alkohol-hue, 38), 85%, 45%);
  border-color: hsl(var(--ik-alkohol-hue, 38), 85%, 45%);
}
.checklist-check:focus-visible { outline: 2px solid hsl(var(--ik-alkohol-hue, 38), 85%, 45%); outline-offset: 2px; }
.item-text { font-size: 14px; color: #0f172a; flex: 1; }
.item-text--done { text-decoration: line-through; color: #94a3b8; }
.item-meta { font-size: 11px; color: #94a3b8; }
</style>
```

- [ ] **Step 3: Verify build**

```bash
npm run build
```

Expected: no errors.

- [ ] **Step 4: Commit**

```bash
git add src/views/alkohol/AlkoholSjekklisterTab.vue
git commit -m "feat: implement AlkoholSjekklisterTab reusing checklist store"
```

---

## Task 9: HendelsesloggTab — Incident Log

**Files:**
- Modify: `src/views/alkohol/HendelsesloggTab.vue` (replace stub)

- [ ] **Step 1: Implement the full component**

Replace `src/views/alkohol/HendelsesloggTab.vue` with:

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAlkoholStore } from '@/stores/alkohol'
import type { AlkoholIncidentType } from '@/types'

const store = useAlkoholStore()
onMounted(() => store.fetchIncidents())

const showForm = ref(false)
const incidentType = ref<AlkoholIncidentType>('BERUSET_GJEST')
const description = ref('')
const followUpRequired = ref(false)
const saving = ref(false)

const incidentTypeLabels: Record<AlkoholIncidentType, string> = {
  NEKTET_SERVERING: 'Nektet servering',
  BERUSET_GJEST:    'Beruset gjest',
  POLITIKONTAKT:    'Politikontakt',
  ANNET:            'Annet'
}

function formatDateTime(iso: string): string {
  return new Date(iso).toLocaleString('nb-NO', {
    day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit'
  })
}

async function submit() {
  if (!description.value.trim() || saving.value) return
  saving.value = true
  try {
    await store.addIncident({
      incidentType: incidentType.value,
      description: description.value.trim(),
      followUpRequired: followUpRequired.value
    })
    description.value = ''
    followUpRequired.value = false
    incidentType.value = 'BERUSET_GJEST'
    showForm.value = false
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <section aria-label="Hendelseslogg">

    <div class="section-header">
      <h1 class="section-title">Hendelseslogg</h1>
      <button class="btn btn--primary" @click="showForm = !showForm" :aria-expanded="showForm">
        {{ showForm ? 'Avbryt' : '+ Rapporter hendelse' }}
      </button>
    </div>

    <!-- Inline form -->
    <div v-if="showForm" class="entry-form card" role="form" aria-label="Rapporter hendelse">

      <div class="form-group">
        <label for="incident-type" class="form-label">Hendelsestype</label>
        <select id="incident-type" v-model="incidentType" class="form-select">
          <option v-for="(label, key) in incidentTypeLabels" :key="key" :value="key">
            {{ label }}
          </option>
        </select>
      </div>

      <div class="form-group">
        <label for="incident-desc" class="form-label">Beskrivelse <span aria-hidden="true">*</span></label>
        <textarea
          id="incident-desc"
          v-model="description"
          class="form-textarea"
          rows="3"
          placeholder="Beskriv hva som skjedde..."
          required
          aria-required="true"
          maxlength="1000"
        ></textarea>
      </div>

      <div class="form-group form-group--inline">
        <input
          id="followup"
          v-model="followUpRequired"
          type="checkbox"
          class="form-checkbox"
        />
        <label for="followup" class="form-label form-label--inline">
          Krever oppfølging
        </label>
      </div>

      <button
        class="btn btn--save"
        :disabled="!description.trim() || saving"
        @click="submit"
        :aria-busy="saving"
      >
        {{ saving ? 'Lagrer...' : 'Lagre hendelse' }}
      </button>
    </div>

    <!-- Incident list -->
    <div v-if="store.loading" class="loading-text" aria-live="polite">Laster hendelser...</div>

    <ul v-else class="incident-list" aria-label="Registrerte hendelser">
      <li
        v-for="incident in store.incidents"
        :key="incident.id"
        class="incident-card"
        :class="{ 'incident-card--followup': incident.followUpRequired }"
      >
        <div class="incident-header">
          <span class="incident-type-badge">{{ incidentTypeLabels[incident.incidentType] }}</span>
          <span v-if="incident.followUpRequired" class="followup-badge" aria-label="Krever oppfølging">
            Oppfølging
          </span>
          <span class="incident-time">{{ formatDateTime(incident.occurredAt) }}</span>
        </div>
        <p class="incident-desc">{{ incident.description }}</p>
        <p class="incident-staff">Rapportert av {{ incident.reportedBy }}</p>
      </li>
    </ul>

    <p v-if="!store.loading && store.incidents.length === 0" class="empty-state">
      Ingen hendelser registrert. Trykk "+ Rapporter hendelse" hvis noe oppstår.
    </p>

  </section>
</template>

<style scoped>
.section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.section-title { font-size: 18px; font-weight: 700; color: #0f172a; }

.entry-form {
  margin-bottom: 20px;
  padding: 16px;
  border: 2px solid hsl(var(--ik-alkohol-hue, 38), 90%, 70%);
  border-radius: 12px;
  background: hsl(var(--ik-alkohol-hue, 38), 100%, 98%);
}

.form-label { display: block; font-size: 13px; font-weight: 600; color: #0f172a; margin-bottom: 6px; }
.form-label--inline { display: inline; margin-bottom: 0; margin-left: 8px; font-weight: 400; }

.form-select, .form-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1.5px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  color: #0f172a;
  font-family: inherit;
}
.form-select:focus, .form-textarea:focus {
  outline: 2px solid hsl(var(--ik-alkohol-hue, 38), 85%, 45%);
  outline-offset: 1px;
  border-color: transparent;
}
.form-textarea { resize: vertical; min-height: 80px; }

.form-group--inline { display: flex; align-items: center; }
.form-checkbox { width: 18px; height: 18px; cursor: pointer; accent-color: hsl(var(--ik-alkohol-hue, 38), 85%, 45%); }

.btn--save {
  width: 100%;
  margin-top: 12px;
  padding: 14px;
  background: hsl(var(--ik-alkohol-hue, 38), 85%, 45%);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  min-height: 44px;
}
.btn--save:disabled { opacity: 0.5; cursor: not-allowed; }
.btn--save:focus-visible { outline: 2px solid #0f172a; outline-offset: 2px; }

.incident-list { list-style: none; padding: 0; display: flex; flex-direction: column; gap: 10px; }
.incident-card {
  background: #fff;
  border: 1.5px solid #e2e8f0;
  border-radius: 12px;
  padding: 14px;
}
.incident-card--followup { border-color: #fca5a5; background: #fef2f2; }

.incident-header { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; margin-bottom: 8px; }
.incident-type-badge {
  background: hsl(var(--ik-alkohol-hue, 38), 100%, 95%);
  color: hsl(var(--ik-alkohol-hue, 38), 80%, 25%);
  border: 1px solid hsl(var(--ik-alkohol-hue, 38), 80%, 75%);
  padding: 3px 10px; border-radius: 99px; font-size: 11px; font-weight: 700;
}
.followup-badge {
  background: #fef2f2; color: #991b1b;
  border: 1px solid #fca5a5;
  padding: 3px 10px; border-radius: 99px; font-size: 11px; font-weight: 700;
}
.incident-time { font-size: 11px; color: #94a3b8; margin-left: auto; }
.incident-desc { font-size: 13px; color: #0f172a; margin: 0 0 6px; line-height: 1.4; }
.incident-staff { font-size: 11px; color: #64748b; margin: 0; }

.loading-text, .empty-state { color: #94a3b8; font-size: 14px; padding: 32px 0; text-align: center; }
</style>
```

- [ ] **Step 2: Verify build**

```bash
npm run build
```

Expected: no errors.

- [ ] **Step 3: Commit**

```bash
git add src/views/alkohol/HendelsesloggTab.vue
git commit -m "feat: implement HendelsesloggTab with incident reporting form"
```

---

## Task 10: Sidebar — IK-Alkohol Section (Desktop)

**Files:**
- Modify: `src/components/layout/AppSidebar.vue`

- [ ] **Step 1: Add IK-Alkohol nav items to the script block**

In `src/components/layout/AppSidebar.vue`, after the `navItems` array definition, add:

```typescript
const alkoholNavItems: NavItem[] = [
  { name: 'Alderskontroll', route: 'alkohol-alderskontroll', icon: 'id-check' },
  { name: 'Sjekklister',    route: 'alkohol-sjekklister',    icon: 'checklist' },
  { name: 'Hendelseslogg',  route: 'alkohol-hendelser',      icon: 'warning' },
]
```

Also add a helper to check if an alkohol route is active:

```typescript
function isAlkoholActive(): boolean {
  return route.path.startsWith('/alkohol')
}
```

- [ ] **Step 2: Add the IK-Alkohol section to the template**

In the `<template>` of `AppSidebar.vue`, find the `<nav>` block containing the `v-for` over `navItems`. After that loop and before the settings item, add:

```html
<!-- IK-Alkohol section -->
<div class="nav-section-divider">
  <span class="nav-section-label">IK-Alkohol</span>
</div>
<button
  v-for="item in alkoholNavItems"
  :key="item.route"
  class="nav-item"
  :class="{ active: isActive(item.route), 'nav-item--alkohol': true }"
  :aria-current="isActive(item.route) ? 'page' : undefined"
  @click="navigate(item.route)"
>
  <span class="nav-icon" aria-hidden="true">🍺</span>
  <span v-if="!collapsed" class="nav-label">{{ item.name }}</span>
</button>
```

- [ ] **Step 3: Add scoped CSS for the new elements**

In the `<style scoped>` block of `AppSidebar.vue`, add:

```css
.nav-section-divider {
  padding: 12px 12px 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.nav-section-label {
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: #475569;
  white-space: nowrap;
}

.nav-item--alkohol.active {
  background: hsl(var(--ik-alkohol-hue, 38), 90%, 15%);
  color: hsl(var(--ik-alkohol-hue, 38), 90%, 75%);
}
```

- [ ] **Step 4: Verify build**

```bash
npm run build
```

Expected: no errors.

- [ ] **Step 5: Commit**

```bash
git add src/components/layout/AppSidebar.vue
git commit -m "feat: add IK-Alkohol section to desktop sidebar"
```

---

## Task 11: AppTabBar — 5 Tabs + Mer Drawer (iPad)

**Files:**
- Modify: `src/components/layout/AppTabBar.vue`

- [ ] **Step 1: Rewrite `src/components/layout/AppTabBar.vue`**

Replace the entire file with:

```vue
<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route  = useRoute()
const auth   = useAuthStore()

const merOpen = ref(false)

// The 5 primary tabs — always visible
const primaryTabs = [
  { id: 'oversikt', label: 'Hjem',   route: 'dashboard',  icon: 'grid',        primary: false },
  { id: 'temp',     label: 'Temp',   route: 'fryser',     icon: 'thermometer', primary: false },
  { id: 'sjekk',    label: 'SJEKK',  route: 'generelt',   icon: 'checklist',   primary: true  },
  { id: 'avvik',    label: 'Avvik',  route: 'avvik',      icon: 'alert',       primary: false },
  { id: 'mer',      label: 'Mer',    route: '',           icon: 'menu',        primary: false },
]

// Items in the "Mer" drawer
const merItems = computed(() => {
  const items = [
    { id: 'grafer',    label: 'Grafer',       route: 'grafer' },
    { id: 'opplaering',label: 'Opplæring',    route: 'opplaering' },
    { id: 'alkohol',   label: 'IK-Alkohol',   route: 'alkohol-alderskontroll' },
  ]
  if (auth.user?.role === 'ADMIN') {
    items.push({ id: 'innstillinger', label: 'Innstillinger', route: 'settings-units' })
  }
  return items
})

function isActive(tab: typeof primaryTabs[0]): boolean {
  if (tab.id === 'mer') return false
  if (tab.id === 'oversikt') return route.name === 'dashboard'
  if (tab.id === 'temp') return route.name === 'fryser' || route.name === 'kjoeleskap'
  if (tab.id === 'sjekk') return route.name === 'generelt'
  if (tab.id === 'avvik') return route.name === 'avvik'
  return false
}

function onTabClick(tab: typeof primaryTabs[0]) {
  if (tab.id === 'mer') {
    merOpen.value = !merOpen.value
    return
  }
  merOpen.value = false
  router.push({ name: tab.route })
}

function navigateMer(routeName: string) {
  merOpen.value = false
  router.push({ name: routeName })
}

function closeMer() {
  merOpen.value = false
}
</script>

<template>
  <!-- Mer drawer backdrop -->
  <div
    v-if="merOpen"
    class="mer-backdrop"
    aria-hidden="true"
    @click="closeMer"
  ></div>

  <!-- Mer drawer -->
  <nav
    v-if="merOpen"
    class="mer-drawer"
    aria-label="Mer navigasjon"
  >
    <button
      v-for="item in merItems"
      :key="item.id"
      class="mer-item"
      :aria-current="route.name === item.route ? 'page' : undefined"
      @click="navigateMer(item.route)"
    >
      {{ item.label }}
    </button>
  </nav>

  <!-- Primary tab bar -->
  <nav class="tab-bar" aria-label="Primærnavigasjon">
    <button
      v-for="tab in primaryTabs"
      :key="tab.id"
      class="tab-bar-item"
      :class="{
        'tab-bar-item--active': isActive(tab),
        'tab-bar-item--primary': tab.primary,
        'tab-bar-item--mer-open': tab.id === 'mer' && merOpen
      }"
      :aria-current="isActive(tab) ? 'page' : undefined"
      :aria-expanded="tab.id === 'mer' ? merOpen : undefined"
      :aria-haspopup="tab.id === 'mer' ? 'menu' : undefined"
      :data-tab="tab.id"
      @click="onTabClick(tab)"
    >
      <span class="tab-icon" aria-hidden="true">
        <TabIcon :name="tab.icon" />
      </span>
      <span class="tab-label">{{ tab.label }}</span>
    </button>
  </nav>
</template>

<script lang="ts">
// Inline SVG icon component — same approach as existing code
const TabIcon = {
  props: ['name'],
  template: `
    <svg width="22" height="22" fill="none" viewBox="0 0 24 24">
      <path v-if="name==='grid'" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zm10 0a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zm10 0a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      <path v-else-if="name==='thermometer'" d="M14 14.76V3.5a2.5 2.5 0 00-5 0v11.26a4.5 4.5 0 105 0z" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
      <path v-else-if="name==='checklist'" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
      <path v-else-if="name==='alert'" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
      <path v-else-if="name==='menu'" d="M4 6h16M4 12h16M4 18h16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
    </svg>
  `
}
</script>

<style scoped>
.tab-bar {
  display: flex;
  background: #0f172a;
  padding: 6px 4px 4px;
  gap: 2px;
  align-items: flex-end;
}

.tab-bar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 6px 2px;
  border-radius: 8px;
  background: none;
  border: none;
  cursor: pointer;
  color: #64748b;
  min-height: 52px;
  min-width: 44px;
  transition: background 0.1s, color 0.1s;
}
.tab-bar-item:focus-visible {
  outline: 2px solid #6366f1;
  outline-offset: -2px;
}
.tab-bar-item--active { background: #1e293b; color: #60a5fa; }
.tab-bar-item--mer-open { background: #1e293b; color: #94a3b8; }

/* Primary (SJEKK) — raised green button */
.tab-bar-item--primary {
  flex: 0 0 60px;
  background: #16A34A;
  border-radius: 12px;
  padding: 8px 4px;
  color: #fff;
  margin-bottom: 0;
}
.tab-bar-item--primary:hover { background: #15803d; }
.tab-bar-item--primary:focus-visible { outline-color: #fff; }

.tab-icon { display: flex; align-items: center; justify-content: center; }
.tab-label { font-size: 9px; font-weight: 600; text-align: center; }
.tab-bar-item--primary .tab-label { font-size: 9px; font-weight: 800; letter-spacing: 0.04em; }

/* Mer drawer */
.mer-backdrop {
  position: fixed;
  inset: 0;
  z-index: 49;
}
.mer-drawer {
  position: fixed;
  bottom: 76px; /* above tab bar */
  left: 0;
  right: 0;
  background: #1e293b;
  border-top: 1px solid #334155;
  border-radius: 16px 16px 0 0;
  padding: 8px 8px 4px;
  z-index: 50;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.mer-item {
  padding: 14px 16px;
  background: none;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 500;
  color: #e2e8f0;
  text-align: left;
  cursor: pointer;
  min-height: 44px;
  transition: background 0.1s;
}
.mer-item:hover { background: #334155; }
.mer-item[aria-current="page"] {
  background: hsl(var(--ik-alkohol-hue, 38), 90%, 15%);
  color: hsl(var(--ik-alkohol-hue, 38), 90%, 75%);
}
.mer-item:focus-visible { outline: 2px solid #6366f1; outline-offset: -2px; }
</style>
```

- [ ] **Step 2: Verify build**

```bash
npm run build
```

Expected: no errors.

- [ ] **Step 3: Commit**

```bash
git add src/components/layout/AppTabBar.vue
git commit -m "feat: restructure iPad tab bar to 5 tabs with Mer drawer"
```

---

## Task 12: Dashboard — IK-Alkohol Summary Block

**Files:**
- Modify: `src/views/DashboardView.vue`

- [ ] **Step 1: Read the current DashboardView structure**

Read `src/views/DashboardView.vue` in full to understand where to insert the block.

- [ ] **Step 2: Import and add the alkohol store**

In the `<script setup>` block of `DashboardView.vue`, add:

```typescript
import { useAlkoholStore } from '@/stores/alkohol'

const alkoholStore = useAlkoholStore()
// Fetch stats when dashboard loads (add inside onMounted if it exists, or add onMounted)
onMounted(() => {
  // ... existing fetch calls ...
  alkoholStore.fetchStats()
})
```

If `onMounted` is not yet imported, add it to the Vue import line.

- [ ] **Step 3: Add the IK-Alkohol summary block to the template**

Find the stats grid in the template. After the existing stats cards, add this block (it should be hidden when stats are null or module is disabled — for now just check `alkoholStore.stats`):

```html
<!-- IK-Alkohol summary — shown when module is active -->
<section
  v-if="alkoholStore.stats"
  class="alkohol-summary module-ik-alkohol"
  aria-label="IK-Alkohol oversikt"
>
  <div class="alkohol-summary__header">
    <span class="alkohol-summary__dot" aria-hidden="true"></span>
    <h2 class="alkohol-summary__title">IK-Alkohol</h2>
    <router-link :to="{ name: 'alkohol-alderskontroll' }" class="alkohol-summary__link">
      Se alle →
    </router-link>
  </div>
  <div class="alkohol-summary__stats">
    <div class="alkohol-stat">
      <span class="alkohol-stat__value">{{ alkoholStore.stats.ageChecksToday }}</span>
      <span class="alkohol-stat__label">ID-kontroller i dag</span>
    </div>
    <div class="alkohol-stat">
      <span class="alkohol-stat__value">{{ alkoholStore.stats.incidentsThisWeek }}</span>
      <span class="alkohol-stat__label">Hendelser denne uken</span>
    </div>
    <div class="alkohol-stat">
      <span class="alkohol-stat__value">{{ alkoholStore.stats.checklistCompletionPct }}%</span>
      <span class="alkohol-stat__label">Sjekkliste fullført</span>
    </div>
  </div>
</section>
```

- [ ] **Step 4: Add scoped CSS for the summary block**

In the `<style scoped>` block of `DashboardView.vue`, add:

```css
.alkohol-summary {
  background: hsl(var(--ik-alkohol-hue, 38), 100%, 97%);
  border: 1.5px solid hsl(var(--ik-alkohol-hue, 38), 90%, 75%);
  border-radius: 12px;
  padding: 16px;
  margin-top: 16px;
}
.alkohol-summary__header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}
.alkohol-summary__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: hsl(var(--ik-alkohol-hue, 38), 85%, 45%);
  flex-shrink: 0;
}
.alkohol-summary__title {
  font-size: 14px;
  font-weight: 700;
  color: hsl(var(--ik-alkohol-hue, 38), 80%, 20%);
  flex: 1;
}
.alkohol-summary__link {
  font-size: 12px;
  font-weight: 600;
  color: hsl(var(--ik-alkohol-hue, 38), 80%, 35%);
  text-decoration: none;
}
.alkohol-summary__link:hover { text-decoration: underline; }
.alkohol-summary__stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}
.alkohol-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}
.alkohol-stat__value {
  font-size: 24px;
  font-weight: 800;
  color: hsl(var(--ik-alkohol-hue, 38), 80%, 25%);
  line-height: 1;
}
.alkohol-stat__label {
  font-size: 10px;
  color: hsl(var(--ik-alkohol-hue, 38), 50%, 45%);
  text-align: center;
}
```

- [ ] **Step 5: Verify full build and type-check**

```bash
npm run type-check && npm run build
```

Expected: no errors.

- [ ] **Step 6: Run all tests**

```bash
npm run test
```

Expected: 4 tests pass (usePermission tests from Task 3).

- [ ] **Step 7: Final commit for Plan 1**

```bash
git add src/views/DashboardView.vue
git commit -m "feat: add IK-Alkohol summary block to dashboard"
```

---

## Self-Review

**Spec coverage check:**

| Spec requirement | Covered by task |
|------------------|-----------------|
| CSS hue variable theming | Task 1 |
| IK-Alkohol TypeScript types | Task 2 |
| `usePermission` composable | Task 3 |
| Alkohol service (mock + TODO) | Task 4 |
| Alkohol Pinia store | Task 4 |
| ModuleBanner component | Task 5 |
| AlkoholView container + routes | Task 6 |
| Alderskontroll screen | Task 7 |
| Alkohol sjekklister screen | Task 8 |
| Hendelseslogg screen | Task 9 |
| Sidebar IK-Alkohol section | Task 10 |
| 5-tab bar + Mer drawer | Task 11 |
| Dashboard IK-Alkohol block | Task 12 |
| Vitest setup | Task 0 |

**Not covered in Plan 1 — covered in Plans 2 and 3:**
- iPad temperature card UX overhaul
- WCAG audit
- Empty/loading/error states on all views
- Permission gating in UI (using `usePermission`)
- Document upload
- Report export
- Checklist template management
- Deviation comments
- Backend wiring
- Login page redesign

**Placeholder scan:** No TBD, no TODO in implementation steps. Service files intentionally have `// TODO: Replace mock with real API call` — these are the API wiring markers, not plan placeholders.

**Type consistency:** `AlderskontrollEntry`, `NewAlderskontrollEntry`, `AlkoholIncident`, `NewAlkoholIncident`, `AgeVerificationOutcome`, `AlkoholIncidentType`, `AlkoholStats` are defined in Task 2 and used consistently in Tasks 4, 7, 8, 9.
