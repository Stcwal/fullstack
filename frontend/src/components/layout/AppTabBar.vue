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
  { id: 'temp',     label: 'Enheter', route: 'fryser',     icon: 'thermometer', primary: false },
  { id: 'sjekk',    label: 'SJEKK',  route: 'generelt',   icon: 'checklist',   primary: true  },
  { id: 'avvik',    label: 'Avvik',  route: 'avvik',      icon: 'alert',       primary: false },
  { id: 'mer',      label: 'Mer',    route: '',           icon: 'menu',        primary: false },
] as const

// Items in the "Mer" drawer
const merItems = computed(() => {
  const items = [
    { id: 'grafer',     label: 'Grafer',       route: 'grafer' },
    { id: 'opplaering', label: 'Opplæring',    route: 'opplaering' },
    { id: 'alkohol',    label: 'IK-Alkohol',   route: 'alkohol-alderskontroll' },
  ]
  if (auth.user?.role === 'ADMIN') {
    items.push({ id: 'innstillinger', label: 'Innstillinger', route: 'settings-units' })
  }
  return items
})

function isActive(tabId: string): boolean {
  if (tabId === 'mer') return false
  if (tabId === 'oversikt') return route.name === 'dashboard'
  if (tabId === 'temp') return route.name === 'fryser' || route.name === 'kjoeleskap'
  if (tabId === 'sjekk') return route.name === 'generelt'
  if (tabId === 'avvik') return route.name === 'avvik'
  return false
}

function onTabClick(tabId: string, tabRoute: string) {
  if (tabId === 'mer') {
    merOpen.value = !merOpen.value
    return
  }
  merOpen.value = false
  router.push({ name: tabRoute })
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
      :data-tab="tab.id"
      :class="{
        'tab-bar-item--active': isActive(tab.id),
        'tab-bar-item--primary': tab.primary,
        'tab-bar-item--mer-open': tab.id === 'mer' && merOpen
      }"
      :aria-current="isActive(tab.id) ? 'page' : undefined"
      :aria-expanded="tab.id === 'mer' ? merOpen : undefined"
      :aria-haspopup="tab.id === 'mer' ? 'menu' : undefined"
      @click="onTabClick(tab.id, tab.route)"
    >
      <span class="tab-icon" aria-hidden="true">
        <svg width="22" height="22" fill="none" viewBox="0 0 24 24">
          <path v-if="tab.icon === 'grid'" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zm10 0a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zm10 0a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          <path v-else-if="tab.icon === 'thermometer'" d="M14 14.76V3.5a2.5 2.5 0 00-5 0v11.26a4.5 4.5 0 105 0z" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
          <path v-else-if="tab.icon === 'checklist'" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
          <path v-else-if="tab.icon === 'alert'" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
          <path v-else-if="tab.icon === 'menu'" d="M4 6h16M4 12h16M4 18h16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </span>
      <span class="tab-label">{{ tab.label }}</span>
    </button>
  </nav>
</template>

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
/* Muted base colours — always visible on dark background */
.tab-bar-item[data-tab="oversikt"] { color: #5b8dd9; }
.tab-bar-item[data-tab="temp"]     { color: #22a8c0; }
.tab-bar-item[data-tab="sjekk"]    { color: #4ab870; }
.tab-bar-item[data-tab="avvik"]    { color: #d06060; }

/* Vibrant active fills — same intensity for all tabs including SJEKK */
.tab-bar-item[data-tab="oversikt"].tab-bar-item--active { background: #2563eb; color: #fff; }
.tab-bar-item[data-tab="temp"].tab-bar-item--active     { background: #0891b2; color: #fff; }
.tab-bar-item[data-tab="sjekk"].tab-bar-item--active    { background: #16a34a; color: #fff; }
.tab-bar-item[data-tab="avvik"].tab-bar-item--active    { background: #dc2626; color: #fff; }
.tab-bar-item--mer-open { background: #1e293b; color: #94a3b8; }

/* Primary (SJEKK) — shape only, colour comes from data-tab selectors above */
.tab-bar-item--primary {
  flex: 1;
  border-radius: 12px;
  padding: 8px 4px;
}
.tab-bar-item--primary:focus-visible { outline-color: #fff; }

.tab-icon { display: flex; align-items: center; justify-content: center; }
.tab-label { font-size: 9px; font-weight: 600; text-align: center; }
.tab-bar-item--primary .tab-label { font-size: 9px; font-weight: 800; letter-spacing: 0.04em; }

/* Mer drawer */
.mer-backdrop {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: var(--tabbar-h, 76px);
  z-index: 49;
}
.mer-drawer {
  position: absolute;
  bottom: var(--tabbar-h, 76px);
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
