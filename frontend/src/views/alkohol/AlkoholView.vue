<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useLayoutStore } from '@/stores/layout'
import ModuleBanner from '@/components/ModuleBanner.vue'

const route  = useRoute()
const router = useRouter()
const layout = useLayoutStore()

const isTablet = computed(() => layout.isTabletMode)

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
  <div class="alkohol-view module-ik-alkohol" :class="{ 'alkohol-view--tablet': isTablet }">

    <ModuleBanner v-if="!isTablet" module="IK_ALKOHOL" />

    <nav v-if="!isTablet" class="alkohol-tabs" aria-label="IK-Alkohol navigasjon">
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

    <main class="alkohol-content" :class="{ 'alkohol-content--tablet': isTablet }">
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

.alkohol-view--tablet {
  background: transparent;
}

.alkohol-tabs {
  display: flex;
  gap: 0;
  padding: 0;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.alkohol-tab {
  flex: 1;
  padding: 12px 8px;
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
  background: transparent;
  border: none;
  border-bottom: 2px solid transparent;
  cursor: pointer;
  min-height: 44px;
  transition: color 0.12s, border-color 0.12s;
  font-family: inherit;
}
.alkohol-tab:hover { color: #334155; }
.alkohol-tab--active {
  color: #92400e;
  border-bottom-color: #b45309;
}
.alkohol-tab:focus-visible {
  outline: 2px solid #b45309;
  outline-offset: -2px;
}

.alkohol-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.alkohol-content--tablet {
  padding: 0;
  overflow: visible;
  display: flex;
  flex-direction: column;
}
</style>
