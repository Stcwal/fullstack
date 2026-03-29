<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  value: number   // 0–100
  total?: number  // alternatively pass count/total and compute
  count?: number
}>()

const pct = computed(() => {
  if (props.value !== undefined) return Math.round(props.value)
  if (props.count !== undefined && props.total) {
    return Math.round((props.count / props.total) * 100)
  }
  return 0
})

const fillClass = computed(() => {
  if (pct.value === 100) return 'full'
  if (pct.value > 0)     return 'partial'
  return 'empty'
})
</script>

<template>
  <div class="progress-bar">
    <div class="progress-fill" :class="fillClass" :style="{ width: pct + '%' }" />
  </div>
</template>
