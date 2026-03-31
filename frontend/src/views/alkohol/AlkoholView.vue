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
