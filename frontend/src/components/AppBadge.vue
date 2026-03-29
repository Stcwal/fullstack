<script setup lang="ts">
defineProps<{
  variant?: 'success' | 'warning' | 'danger' | 'info' | 'neutral' | 'purple'
  text: string
}>()
</script>

<template>
  <span class="badge" :class="`badge-${variant ?? 'neutral'}`">{{ text }}</span>
</template>
