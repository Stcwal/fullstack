<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useShiftStore } from '@/stores/shift'
import { useAuthStore } from '@/stores/auth'

const shiftStore = useShiftStore()
const authStore  = useAuthStore()

const open    = ref(false)
const rootRef = ref<HTMLElement | null>(null)

onMounted(async () => {
  await shiftStore.loadWorkers()
  if (!shiftStore.activeId && authStore.user) {
    shiftStore.setActive(authStore.user.id)
  }
  document.addEventListener('click', onOutsideClick)
})

onUnmounted(() => {
  document.removeEventListener('click', onOutsideClick)
})

function onOutsideClick(e: MouseEvent) {
  if (rootRef.value && !rootRef.value.contains(e.target as Node)) {
    open.value = false
  }
}

function select(id: number) {
  shiftStore.setActive(id)
  open.value = false
}

const initials = computed(() => {
  const w = shiftStore.activeWorker
  if (!w) return '?'
  return ((w.firstName[0] ?? '') + (w.lastName[0] ?? '')).toUpperCase()
})
</script>

<template>
  <div ref="rootRef" class="shift-switcher">
    <button
      class="shift-btn"
      :aria-expanded="open"
      aria-haspopup="listbox"
      :aria-label="`Aktiv vakt: ${shiftStore.activeWorkerName}. Bytt ansatt.`"
      @click.stop="open = !open"
    >
      <span class="shift-avatar" aria-hidden="true">{{ initials }}</span>
      <span class="shift-name">{{ shiftStore.activeWorkerName }}</span>
      <span class="shift-caret" aria-hidden="true">{{ open ? '▴' : '▾' }}</span>
    </button>

    <div v-if="open" class="shift-dropdown" role="listbox" :aria-label="`Velg ansatt`">
      <button
        v-for="w in shiftStore.workers"
        :key="w.id"
        class="shift-option"
        :class="{ 'shift-option--active': w.id === shiftStore.activeId }"
        role="option"
        :aria-selected="w.id === shiftStore.activeId"
        @click.stop="select(w.id)"
      >
        <span class="shift-option__avatar" aria-hidden="true">
          {{ (w.firstName[0] ?? '') + (w.lastName[0] ?? '') }}
        </span>
        <span class="shift-option__name">{{ w.firstName }} {{ w.lastName }}</span>
        <span class="shift-option__role">{{ w.role === 'ADMIN' ? 'Admin' : w.role === 'MANAGER' ? 'Leder' : 'Ansatt' }}</span>
        <span v-if="w.id === shiftStore.activeId" class="shift-option__check" aria-hidden="true">✓</span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.shift-switcher {
  position: relative;
}

/* ── Trigger button ── */
.shift-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px 6px 6px;
  background: #1e293b;
  border: none;
  border-radius: 6px;
  color: #e2e8f0;
  cursor: pointer;
  font-family: inherit;
  font-size: 14px;
  font-weight: 500;
  transition: background 0.1s;
  white-space: nowrap;
}
.shift-btn:hover         { background: #334155; }
.shift-btn:focus-visible { outline: 2px solid #475569; outline-offset: 2px; }

.shift-avatar {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
  color: #fff;
  flex-shrink: 0;
}

.shift-name  { max-width: 140px; overflow: hidden; text-overflow: ellipsis; }
.shift-caret { font-size: 10px; opacity: 0.7; flex-shrink: 0; }

/* ── Dropdown ── */
.shift-dropdown {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  min-width: 220px;
  background: #1e293b;
  border: 1px solid #334155;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.35);
  z-index: 200;
  overflow: hidden;
  padding: 4px;
}

/* ── Worker options ── */
.shift-option {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 10px 12px;
  background: transparent;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-family: inherit;
  color: #cbd5e1;
  text-align: left;
  min-height: 44px;
  transition: background 0.08s;
}
.shift-option:hover    { background: rgba(255, 255, 255, 0.07); }
.shift-option:focus-visible { outline: 2px solid rgba(255, 255, 255, 0.4); outline-offset: -2px; }

.shift-option--active {
  background: rgba(255, 255, 255, 0.06);
  color: #fff;
}

.shift-option__avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #334155;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  color: #94a3b8;
  flex-shrink: 0;
  text-transform: uppercase;
}
.shift-option--active .shift-option__avatar {
  background: #166534;
  color: #fff;
}

.shift-option__name {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.shift-option__role {
  font-size: 11px;
  color: #64748b;
  flex-shrink: 0;
}
.shift-option--active .shift-option__role { color: #4ade80; }

.shift-option__check {
  font-size: 13px;
  color: #4ade80;
  font-weight: 700;
  flex-shrink: 0;
}
</style>
