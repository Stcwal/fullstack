<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useLocationStore } from '@/stores/location'
import { useAuthStore } from '@/stores/auth'

const locationStore = useLocationStore()
const authStore = useAuthStore()

const open = ref(false)
const rootRef = ref<HTMLElement | null>(null)

onMounted(() => {
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

function select(id: number | null) {
  locationStore.setLocation(id)
  open.value = false
}

const activeLabel = computed(() => {
  if (locationStore.activeLocationId === null) return 'Alle lokasjoner'
  return locationStore.locations.find(l => l.id === locationStore.activeLocationId)?.name ?? 'Alle lokasjoner'
})

const activeShort = computed(() => {
  if (locationStore.activeLocationId === null) return 'AL'
  const name = locationStore.locations.find(l => l.id === locationStore.activeLocationId)?.name ?? ''
  return name.slice(0, 2).toUpperCase()
})
</script>

<template>
  <div
    v-if="authStore.user?.role === 'ADMIN'"
    ref="rootRef"
    class="loc-switcher"
  >
    <button
      class="loc-btn"
      :aria-expanded="open"
      aria-haspopup="listbox"
      :aria-label="`Aktiv lokasjon: ${activeLabel}. Bytt lokasjon.`"
      @click.stop="open = !open"
    >
      <span class="loc-avatar" aria-hidden="true">{{ activeShort }}</span>
      <span class="loc-name">{{ locationStore.activeLocationId === null ? 'Alle' : activeLabel }}</span>
      <span class="loc-caret" aria-hidden="true">{{ open ? '▴' : '▾' }}</span>
    </button>

    <div v-if="open" class="loc-dropdown" role="listbox" aria-label="Velg lokasjon">
      <button
        class="loc-option"
        :class="{ 'loc-option--active': locationStore.activeLocationId === null }"
        role="option"
        :aria-selected="locationStore.activeLocationId === null"
        @click.stop="select(null)"
      >
        <span class="loc-option__avatar" aria-hidden="true">AL</span>
        <span class="loc-option__name">Alle lokasjoner</span>
        <span v-if="locationStore.activeLocationId === null" class="loc-option__check" aria-hidden="true">✓</span>
      </button>

      <button
        v-for="loc in locationStore.locations"
        :key="loc.id"
        class="loc-option"
        :class="{ 'loc-option--active': loc.id === locationStore.activeLocationId }"
        role="option"
        :aria-selected="loc.id === locationStore.activeLocationId"
        @click.stop="select(loc.id)"
      >
        <span class="loc-option__avatar" aria-hidden="true">{{ loc.name.slice(0, 2).toUpperCase() }}</span>
        <span class="loc-option__name">{{ loc.name }}</span>
        <span v-if="loc.id === locationStore.activeLocationId" class="loc-option__check" aria-hidden="true">✓</span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.loc-switcher {
  position: relative;
}

/* ── Trigger button ── */
.loc-btn {
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
.loc-btn:hover         { background: #334155; }
.loc-btn:focus-visible { outline: 2px solid #475569; outline-offset: 2px; }

.loc-avatar {
  width: 26px;
  height: 26px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 700;
  color: #fff;
  flex-shrink: 0;
}

.loc-name  { max-width: 120px; overflow: hidden; text-overflow: ellipsis; }
.loc-caret { font-size: 10px; opacity: 0.7; flex-shrink: 0; }

/* ── Dropdown ── */
.loc-dropdown {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  min-width: 200px;
  background: #1e293b;
  border: 1px solid #334155;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.35);
  z-index: 200;
  overflow: hidden;
  padding: 4px;
}

/* ── Location options ── */
.loc-option {
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
.loc-option:hover         { background: rgba(255, 255, 255, 0.07); }
.loc-option:focus-visible { outline: 2px solid rgba(255, 255, 255, 0.4); outline-offset: -2px; }

.loc-option--active {
  background: rgba(255, 255, 255, 0.06);
  color: #fff;
}

.loc-option__avatar {
  width: 30px;
  height: 30px;
  border-radius: 6px;
  background: #334155;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 700;
  color: #94a3b8;
  flex-shrink: 0;
  text-transform: uppercase;
}
.loc-option--active .loc-option__avatar {
  background: #166534;
  color: #fff;
}

.loc-option__name {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.loc-option__check {
  font-size: 13px;
  color: #4ade80;
  font-weight: 700;
  flex-shrink: 0;
}
</style>
