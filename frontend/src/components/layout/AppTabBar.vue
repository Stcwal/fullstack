<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route  = useRoute()
const auth   = useAuthStore()

const merOpen = ref(false)

const primaryTabs = [
  { id: 'alkohol', label: 'Alkohol', route: 'alkohol-alderskontroll', icon: 'wine',        primary: false },
  { id: 'temp',    label: 'Enheter', route: 'fryser',                 icon: 'thermometer', primary: false },
  { id: 'sjekk',   label: 'SJEKK',  route: 'generelt',               icon: 'checklist',   primary: true  },
  { id: 'avvik',   label: 'Avvik',  route: 'avvik',                  icon: 'alert',       primary: false },
  { id: 'mer',     label: 'Mer',    route: '',                        icon: 'menu',        primary: false },
] as const

const merItems = computed(() => {
  const items = [
    { id: 'oversikt',   label: 'Dashboard',  route: 'dashboard' },
    { id: 'grafer',     label: 'Grafer',     route: 'grafer' },
    { id: 'opplaering', label: 'Opplæring',  route: 'opplaering' },
  ]
  if (auth.user?.role === 'ADMIN') {
    items.push({ id: 'innstillinger', label: 'Innstillinger', route: 'settings-units' })
  }
  return items
})

function isActive(tabId: string): boolean {
  if (tabId === 'mer') return false
  if (tabId === 'alkohol') return String(route.name ?? '').startsWith('alkohol')
  if (tabId === 'temp') return route.name === 'fryser' || route.name === 'kjoeleskap'
  if (tabId === 'sjekk') return route.name === 'generelt'
  if (tabId === 'avvik') return route.name === 'avvik'
  return false
}

function onTabClick(tabId: string, tabRoute: string) {
  if (tabId === 'mer') { merOpen.value = !merOpen.value; return }
  merOpen.value = false
  router.push({ name: tabRoute })
}

function navigateMer(routeName: string) {
  merOpen.value = false
  router.push({ name: routeName })
}

function closeMer() { merOpen.value = false }
</script>

<template>
  <div v-if="merOpen" class="mer-backdrop" aria-hidden="true" @click="closeMer"></div>

  <nav v-if="merOpen" class="mer-drawer" aria-label="Mer navigasjon">
    <button
      v-for="item in merItems"
      :key="item.id"
      class="mer-item"
      :aria-current="route.name === item.route ? 'page' : undefined"
      @click="navigateMer(item.route)"
    >{{ item.label }}</button>
  </nav>

  <nav class="tab-bar" aria-label="Primærnavigasjon">
    <button
      v-for="tab in primaryTabs"
      :key="tab.id"
      class="tab-bar-item"
      :data-tab="tab.id"
      :class="{
        'tab-bar-item--active':   isActive(tab.id),
        'tab-bar-item--primary':  tab.primary,
        'tab-bar-item--mer-open': tab.id === 'mer' && merOpen,
      }"
      :aria-current="isActive(tab.id) ? 'page' : undefined"
      :aria-expanded="tab.id === 'mer' ? merOpen : undefined"
      :aria-haspopup="tab.id === 'mer' ? 'menu' : undefined"
      @click="onTabClick(tab.id, tab.route)"
    >
      <span class="tab-icon" aria-hidden="true">
        <svg width="40" height="40" fill="none" viewBox="0 0 24 24">
          <path v-if="tab.icon === 'wine'"             d="M5 2l3 7a4 4 0 008 0l3-7H5zm7 9v7M9 18h6"                                                                                                                                                                                  stroke="#fff" stroke-width="2"   stroke-linecap="round" stroke-linejoin="round"/>
          <path v-else-if="tab.icon === 'thermometer'" d="M14 14.76V3.5a2.5 2.5 0 00-5 0v11.26a4.5 4.5 0 105 0z"                                                                                                                                                                    stroke="#fff" stroke-width="2"   stroke-linecap="round" stroke-linejoin="round"/>
          <path v-else-if="tab.icon === 'checklist'"   d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"                                                                           stroke="#fff" stroke-width="2"   stroke-linecap="round" stroke-linejoin="round"/>
          <path v-else-if="tab.icon === 'alert'"       d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"                                                                                     stroke="#fff" stroke-width="2"   stroke-linecap="round" stroke-linejoin="round"/>
          <path v-else-if="tab.icon === 'menu'"        d="M4 6h16M4 12h16M4 18h16"                                                                                                                                                                                                  stroke="#fff" stroke-width="2.5" stroke-linecap="round"/>
        </svg>
      </span>
      <span class="tab-label">{{ tab.label }}</span>
    </button>
  </nav>
</template>

<style scoped>
/* ── Tab bar: full-bleed sections, zero gaps ── */
.tab-bar {
  display: flex;
  padding: 0;
  gap: 0;
  align-items: stretch;
  height: 92px;
}

.tab-bar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1px;
  padding: 0;
  border-radius: 0;
  border: none;
  cursor: pointer;
  color: #fff;
  transition: filter 0.12s;
}

.tab-bar-item:focus-visible {
  outline: 2px solid rgba(255,255,255,0.7);
  outline-offset: -3px;
}

/* ── Per-tab colors: inactive = mid-tone, active = vibrant ── */
.tab-bar-item[data-tab="alkohol"]                      { background: #92400e; }
.tab-bar-item[data-tab="alkohol"].tab-bar-item--active { background: #b45309; }

.tab-bar-item[data-tab="temp"]                         { background: #155e75; }
.tab-bar-item[data-tab="temp"].tab-bar-item--active    { background: #0891b2; }

.tab-bar-item[data-tab="sjekk"]                        { background: #166534; }
.tab-bar-item[data-tab="sjekk"].tab-bar-item--active   { background: #16a34a; }

.tab-bar-item[data-tab="avvik"]                        { background: #991b1b; }
.tab-bar-item[data-tab="avvik"].tab-bar-item--active   { background: #dc2626; }

.tab-bar-item[data-tab="mer"]                          { background: #334155; }
.tab-bar-item[data-tab="mer"].tab-bar-item--active,
.tab-bar-item--mer-open                                { background: #475569; }

/* ── SJEKK: slightly taller label, no extra shape ── */
.tab-bar-item--primary .tab-label {
  font-weight: 800;
  letter-spacing: 0.05em;
}

.tab-icon { display: flex; align-items: center; justify-content: center; }

.tab-label {
  font-size: clamp(9px, 2.8vw, 21px);
  font-weight: 800;
  color: #fff !important;
  letter-spacing: 0.02em;
  text-align: center;
  text-transform: uppercase;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}

.tab-icon svg {
  width: clamp(22px, 5vw, 40px);
  height: clamp(22px, 5vw, 40px);
}

/* ── Mer drawer ── */
.mer-backdrop {
  position: absolute;
  inset: 0 0 92px 0;
  z-index: 49;
}

.mer-drawer {
  position: absolute;
  bottom: 92px;
  left: 0;
  right: 0;
  background: #1e293b;
  border-top: 1px solid #334155;
  border-radius: 12px 12px 0 0;
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
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  color: #e2e8f0;
  text-align: left;
  cursor: pointer;
  min-height: 44px;
  transition: background 0.1s;
}

.mer-item:hover                   { background: #334155; }
.mer-item[aria-current="page"]    { background: rgba(255,255,255,0.08); font-weight: 600; }
.mer-item:focus-visible           { outline: 2px solid rgba(255,255,255,0.5); outline-offset: -2px; }
</style>
