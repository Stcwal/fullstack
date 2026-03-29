<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { computed } from 'vue'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

interface Tab {
  id: string
  label: string
  route: string
  icon: string
  adminOnly?: boolean
}

const tabs: Tab[] = [
  { id: 'oversikt',      label: 'Oversikt',   route: 'dashboard',    icon: 'grid' },
  { id: 'fryser',        label: 'Fryser',     route: 'fryser',       icon: 'snowflake' },
  { id: 'kjoeleskap',    label: 'Kjøleskap',  route: 'kjoeleskap',   icon: 'thermometer' },
  { id: 'generelt',      label: 'Generelt',   route: 'generelt',     icon: 'checklist' },
  { id: 'avvik',         label: 'Avvik',      route: 'avvik',        icon: 'alert' },
  { id: 'graf',          label: 'Grafer',     route: 'grafer',       icon: 'chart' },
  { id: 'opplaering',    label: 'Opplæring',  route: 'opplaering',   icon: 'book' },
  { id: 'innstillinger', label: 'Innst.',     route: 'settings-units', icon: 'settings', adminOnly: true },
]

const visibleTabs = computed(() =>
  tabs.filter(t => !t.adminOnly || auth.user?.role === 'ADMIN')
)

function isActive(tab: Tab): boolean {
  if (tab.route === 'settings-units') return route.path.startsWith('/innstillinger')
  return route.name === tab.route
}

function navigate(tab: Tab) {
  router.push({ name: tab.route })
}
</script>

<template>
  <nav class="tab-bar">
    <button
      v-for="tab in visibleTabs"
      :key="tab.id"
      class="tab-bar-item"
      :class="{ active: isActive(tab) }"
      :data-tab="tab.id"
      @click="navigate(tab)"
    >
      <span class="tab-icon">
        <TabIcon :name="tab.icon" />
      </span>
      <span class="tab-label">{{ tab.label }}</span>
    </button>
  </nav>
</template>

<script lang="ts">
const TabIcon = {
  props: ['name'],
  template: `
    <svg width="22" height="22" fill="none" viewBox="0 0 24 24">
      <path v-if="name==='grid'" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zm10 0a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zm10 0a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      <path v-else-if="name==='snowflake'" d="M12 2v20M2 12h20M4.93 4.93l14.14 14.14M19.07 4.93L4.93 19.07M12 6l-2-2m2 2l2-2M12 18l-2 2m2-2l2 2M6 12l-2-2m2 2l-2 2M18 12l2-2m-2 2l2 2" stroke="currentColor" stroke-width="1.75" stroke-linecap="round"/>
      <path v-else-if="name==='thermometer'" d="M14 14.76V3.5a2.5 2.5 0 00-5 0v11.26a4.5 4.5 0 105 0z" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
      <path v-else-if="name==='checklist'" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
      <path v-else-if="name==='alert'" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
      <path v-else-if="name==='chart'" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
      <path v-else-if="name==='book'" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
      <path v-else-if="name==='settings'" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065zM15 12a3 3 0 11-6 0 3 3 0 016 0z" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>
  `
}
</script>
