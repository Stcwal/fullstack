<script setup lang="ts">
import { computed } from 'vue'
import { RouterView } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useLayoutStore } from '@/stores/layout'
import AppSidebar from '@/components/layout/AppSidebar.vue'
import AppTabBar from '@/components/layout/AppTabBar.vue'
import TabletToggle from '@/components/layout/TabletToggle.vue'

const authStore = useAuthStore()
const layoutStore = useLayoutStore()
const isAuthenticated = computed(() => authStore.isAuthenticated)
</script>

<template>
  <!-- Tablet simulator mode: dark bg + device frame -->
  <template v-if="layoutStore.isTabletMode">
    <div class="tablet-simulator-bg">
      <div class="tablet-device">
        <div class="app-shell-tablet" v-if="isAuthenticated">
          <main class="app-main-tablet">
            <RouterView />
          </main>
          <AppTabBar />
        </div>
        <RouterView v-else />
      </div>
    </div>
  </template>

  <!-- Desktop mode -->
  <template v-else>
    <div v-if="isAuthenticated" class="app-shell">
      <AppSidebar />
      <main class="app-main" :class="{ 'sidebar-collapsed': layoutStore.isSidebarCollapsed }">
        <RouterView />
      </main>
    </div>
    <RouterView v-else />
  </template>

  <!-- Dev tool: tablet mode toggle (always visible) -->
  <TabletToggle />
</template>
