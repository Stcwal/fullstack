<script setup lang="ts">
defineProps<{
  type: 'danger' | 'warning' | 'info'
  message: string
}>()
</script>

<template>
  <div class="alert-banner" :class="type">
    <svg width="16" height="16" fill="none" viewBox="0 0 24 24" style="flex-shrink:0; margin-top:1px;">
      <path v-if="type === 'danger' || type === 'warning'" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      <path v-else d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>
    <span>{{ message }}</span>
  </div>
</template>
