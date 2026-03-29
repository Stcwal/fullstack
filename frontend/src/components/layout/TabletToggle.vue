<script setup lang="ts">
import { useLayoutStore } from '@/stores/layout'

const layout = useLayoutStore()
</script>

<template>
  <button
    class="tablet-toggle-btn"
    :class="{ active: layout.isTabletMode }"
    @click="layout.toggleTabletMode"
    title="Bytt mellom desktop og nettbrettvisning"
  >
    <svg width="14" height="14" fill="none" viewBox="0 0 24 24">
      <rect x="2" y="4" width="20" height="16" rx="2" stroke="currentColor" stroke-width="2"/>
      <line x1="12" y1="17" x2="12" y2="17" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
    </svg>
    {{ layout.isTabletMode ? 'Desktop' : 'Nettbrett' }}
  </button>
</template>
