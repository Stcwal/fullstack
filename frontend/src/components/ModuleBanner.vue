<script setup lang="ts">
import type { ModuleType } from '@/types'

const props = defineProps<{
  module: ModuleType
}>()

const labels: Record<ModuleType, string> = {
  IK_MAT: 'IK-Mat',
  IK_ALKOHOL: 'IK-Alkohol'
}
</script>

<template>
  <div
    class="module-banner"
    :class="module === 'IK_ALKOHOL' ? 'module-banner--alkohol' : 'module-banner--mat'"
    role="banner"
    :aria-label="`Aktiv modul: ${labels[module]}`"
  >
    <span class="module-banner__dot" aria-hidden="true"></span>
    <span class="module-banner__label">{{ labels[module] }}</span>
  </div>
</template>

<style scoped>
.module-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  border-bottom: 2px solid;
}

.module-banner__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

/* IK-Mat variant */
.module-banner--mat {
  background: var(--color-success-bg, #f0fdf4);
  color: var(--color-success-dark, #166534);
  border-color: var(--color-success-border, #bbf7d0);
}
.module-banner--mat .module-banner__dot {
  background: var(--color-success, #16A34A);
}

/* IK-Alkohol variant — uses module tokens from .module-ik-alkohol parent,
   but also works standalone via fallback values */
.module-banner--alkohol {
  background: hsl(var(--ik-alkohol-hue, 38), 100%, 97%);
  color: hsl(var(--ik-alkohol-hue, 38), 80%, 25%);
  border-color: hsl(var(--ik-alkohol-hue, 38), 90%, 70%);
}
.module-banner--alkohol .module-banner__dot {
  background: hsl(var(--ik-alkohol-hue, 38), 85%, 45%);
}
</style>
