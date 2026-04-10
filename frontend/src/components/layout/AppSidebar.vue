<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useLayoutStore } from '@/stores/layout'
import { useLocationStore } from '@/stores/location'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const layout = useLayoutStore()
const locationStore = useLocationStore()

const canSwitchLocation = computed(() =>
  auth.user?.role === 'ADMIN' || auth.user?.role === 'SUPERVISOR'
)

onMounted(() => {
  if (canSwitchLocation.value) locationStore.fetchLocations()
})

const collapsed = computed(() => layout.isSidebarCollapsed)

const user = computed(() => auth.user)
const initials = computed(() => {
  if (!user.value) return '?'
  return (user.value.firstName[0] + user.value.lastName[0]).toUpperCase()
})

const roleName = computed(() => {
  const map = { ADMIN: 'Administrator', SUPERVISOR: 'Veileder', MANAGER: 'Leder', STAFF: 'Ansatt' }
  return user.value ? map[user.value.role] : ''
})

interface NavItem {
  name: string
  route: string
  icon: string
  alert?: boolean
}

const navItems: NavItem[] = [
  { name: 'Oversikt',       route: 'dashboard',    icon: 'grid' },
  { name: 'Temperaturlogging', route: 'fryser',          icon: 'thermometer' },
  { name: 'Temperaturlogg',   route: 'temperatur-logg',  icon: 'chart' },
  { name: 'Generelt',       route: 'generelt',     icon: 'checklist' },
  { name: 'Avvik',          route: 'avvik',        icon: 'warning', alert: true },
  { name: 'Temperaturgrafer', route: 'grafer',     icon: 'chart' },
  { name: 'Opplæring',      route: 'opplaering',   icon: 'book' },
]

const settingsItem: NavItem = { name: 'Innstillinger', route: 'settings-units', icon: 'settings' }

const alkoholNavItems = [
  { name: 'Alderskontroll', route: 'alkohol-alderskontroll' },
  { name: 'Sjekklister',    route: 'alkohol-sjekklister' },
  { name: 'Hendelseslogg',  route: 'alkohol-hendelser' },
]

function isActive(routeName: string): boolean {
  if (routeName === 'settings-units') {
    return route.path.startsWith('/innstillinger')
  }
  return route.name === routeName
}

function navigate(routeName: string) {
  router.push({ name: routeName })
}

function logout() {
  auth.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <nav class="sidebar" :class="{ collapsed }">
    <!-- Brand -->
    <div class="sidebar-brand">
      <div class="brand-icon">
        <svg width="18" height="18" fill="none" viewBox="0 0 24 24">
          <path d="M9 3H5a2 2 0 00-2 2v4m6-6h10a2 2 0 012 2v4M9 3v18m0 0h10a2 2 0 002-2V9M9 21H5a2 2 0 01-2-2V9m0 0h18" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
      <div class="brand-text">
        <div class="brand-name">IK-System</div>
        <div class="brand-sub">{{ user?.organizationName ?? 'IK-Kontrollsystem' }}</div>
      </div>
    </div>

    <!-- Location switcher (ADMIN / SUPERVISOR only) -->
    <div v-if="canSwitchLocation && !collapsed" class="sidebar-location-switcher">
      <select
        :value="locationStore.activeLocationId ?? ''"
        @change="locationStore.setLocation(($event.target as HTMLSelectElement).value ? Number(($event.target as HTMLSelectElement).value) : null)"
        class="location-select"
        aria-label="Velg lokasjon"
      >
        <option value="">Alle lokasjoner</option>
        <option v-for="loc in locationStore.locations" :key="loc.id" :value="loc.id">
          {{ loc.name }}
        </option>
      </select>
    </div>

    <!-- Navigation -->
    <div class="sidebar-nav">
      <div class="sidebar-group-label">Hovedmeny</div>

      <button
        v-for="item in navItems"
        :key="item.route"
        class="sidebar-item"
        :class="{ active: isActive(item.route) }"
        @click="navigate(item.route)"
        :title="collapsed ? item.name : undefined"
      >
        <span class="sidebar-item-icon">
          <SidebarIcon :name="item.icon" />
        </span>
        <span class="sidebar-item-label">{{ item.name }}</span>
        <span v-if="item.alert && !collapsed" class="alert-dot" />
      </button>

      <!-- IK-Alkohol section -->
      <div class="sidebar-group-label">IK-Alkohol</div>
      <button
        v-for="item in alkoholNavItems"
        :key="item.route"
        class="sidebar-item sidebar-item--alkohol"
        :class="{ active: isActive(item.route) }"
        :aria-current="isActive(item.route) ? 'page' : undefined"
        @click="navigate(item.route)"
        :title="collapsed ? item.name : undefined"
      >
        <span class="sidebar-item-icon" aria-hidden="true">&#x1F37A;</span>
        <span class="sidebar-item-label">{{ item.name }}</span>
      </button>

      <template v-if="user?.role === 'ADMIN'">
        <div class="sidebar-group-label">Administrasjon</div>
        <button
          class="sidebar-item"
          :class="{ active: isActive(settingsItem.route) }"
          @click="navigate(settingsItem.route)"
          :title="collapsed ? settingsItem.name : undefined"
        >
          <span class="sidebar-item-icon">
            <SidebarIcon :name="settingsItem.icon" />
          </span>
          <span class="sidebar-item-label">{{ settingsItem.name }}</span>
        </button>
      </template>
    </div>

    <!-- Footer: user + logout -->
    <div class="sidebar-footer">
      <div class="sidebar-user">
        <div class="user-avatar">{{ initials }}</div>
        <div class="user-info">
          <div class="user-name">{{ user?.firstName }} {{ user?.lastName }}</div>
          <div class="user-role">{{ roleName }}</div>
          <div v-if="user?.primaryLocationName" class="user-location">
            <svg width="10" height="10" fill="none" viewBox="0 0 24 24" style="opacity:0.6;flex-shrink:0"><path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5A2.5 2.5 0 1 1 12 6.5a2.5 2.5 0 0 1 0 5z" fill="currentColor"/></svg>
            {{ user.primaryLocationName }}
          </div>
        </div>
      </div>

      <button class="sidebar-item" style="margin-top: 0.25rem;" @click="logout" :title="collapsed ? 'Logg ut' : undefined">
        <span class="sidebar-item-icon">
          <svg width="16" height="16" fill="none" viewBox="0 0 24 24">
            <path d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </span>
        <span class="sidebar-item-label">Logg ut</span>
      </button>

      <!-- Collapse toggle -->
      <button class="sidebar-toggle" @click="layout.toggleSidebar" :title="collapsed ? 'Utvid' : 'Skjul'">
        <svg width="16" height="16" fill="none" viewBox="0 0 24 24" :style="{ transform: collapsed ? 'rotate(180deg)' : '' }" style="transition: transform 0.3s">
          <path d="M11 19l-7-7 7-7m8 14l-7-7 7-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
    </div>
  </nav>
</template>

<script lang="ts">
import { h, defineComponent } from 'vue'

const ICON_PATHS: Record<string, string[]> = {
  grid:        ['M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zm10 0a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zm10 0a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z'],
  snowflake:   ['M12 2v20M2 12h20M4.93 4.93l14.14 14.14M19.07 4.93L4.93 19.07M12 6l-2-2m2 2l2-2M12 18l-2 2m2-2l2 2M6 12l-2-2m2 2l-2 2M18 12l2-2m-2 2l2 2'],
  thermometer: ['M14 14.76V3.5a2.5 2.5 0 00-5 0v11.26a4.5 4.5 0 105 0z'],
  checklist:   ['M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4'],
  warning:     ['M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z'],
  chart:       ['M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z'],
  book:        ['M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253'],
  settings:    ['M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065zM15 12a3 3 0 11-6 0 3 3 0 016 0z'],
}

const SidebarIcon = defineComponent({
  name: 'SidebarIcon',
  props: ['name'],
  render() {
    const paths = ICON_PATHS[this.name] ?? []
    return h('svg', { width: 16, height: 16, fill: 'none', viewBox: '0 0 24 24' },
      paths.map(d => h('path', { d, stroke: 'currentColor', 'stroke-width': '1.75', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' }))
    )
  }
})
</script>
