<script setup lang="ts">
defineProps<{
  title: string
  show: boolean
  size?: 'sm' | 'md' | 'lg'
}>()

const emit = defineEmits<{
  close: []
}>()
</script>

<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="show" class="modal-backdrop" @click.self="emit('close')">
        <div class="modal" :style="{ maxWidth: size === 'lg' ? '640px' : size === 'sm' ? '380px' : '480px' }">
          <div class="modal-header">
            <h2 style="font-size: 1.0625rem;">{{ title }}</h2>
            <button class="btn btn-ghost btn-sm" style="padding: 0.25rem; border-radius: 6px;" @click="emit('close')">
              <svg width="18" height="18" fill="none" viewBox="0 0 24 24">
                <path d="M6 18L18 6M6 6l12 12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </button>
          </div>
          <slot />
          <div class="modal-footer" v-if="$slots.footer">
            <slot name="footer" />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>
