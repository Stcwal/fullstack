import { defineStore } from 'pinia'
import { ref, watch } from 'vue'
import { alkoholService } from '@/services/alkohol.service'
import { useLocationStore } from '@/stores/location'
import { useAuthStore } from '@/stores/auth'
import type {
  AlderskontrollEntry,
  NewAlderskontrollEntry,
  AlkoholIncident,
  NewAlkoholIncident,
  AlkoholStats
} from '@/types'

export const useAlkoholStore = defineStore('alkohol', () => {
  const entries = ref<AlderskontrollEntry[]>([])
  const incidents = ref<AlkoholIncident[]>([])
  const stats = ref<AlkoholStats | null>(null)
  const loading = ref(false)
  const saving = ref(false)
  const error = ref<string | null>(null)

  const locationStore = useLocationStore()
  const authStore = useAuthStore()

  function canListAlcohol(): boolean {
    const role = authStore.user?.role
    return role === 'ADMIN' || role === 'MANAGER' || role === 'SUPERVISOR'
  }

  async function fetchEntries() {
    if (!canListAlcohol()) return
    loading.value = true
    error.value = null
    try {
      entries.value = await alkoholService.getAlderskontrollEntries(locationStore.activeLocationId)
    } catch {
      error.value = 'Kunne ikke laste alderskontroll-logg.'
    } finally {
      loading.value = false
    }
  }

  async function addEntry(data: NewAlderskontrollEntry) {
    saving.value = true
    try {
      const entry = await alkoholService.createAlderskontrollEntry(data)
      entries.value.unshift(entry)
    } finally {
      saving.value = false
    }
  }

  async function fetchIncidents() {
    if (!canListAlcohol()) return
    loading.value = true
    error.value = null
    try {
      incidents.value = await alkoholService.getIncidents(locationStore.activeLocationId)
    } catch {
      error.value = 'Kunne ikke laste hendelseslogg.'
    } finally {
      loading.value = false
    }
  }

  async function addIncident(data: NewAlkoholIncident) {
    saving.value = true
    try {
      const incident = await alkoholService.createIncident(data)
      incidents.value.unshift(incident)
    } finally {
      saving.value = false
    }
  }

  async function fetchStats() {
    try {
      stats.value = await alkoholService.getStats()
    } catch {
      // stats remain at defaults if endpoint is unavailable
    }
  }

  watch(() => locationStore.activeLocationId, () => {
    fetchEntries()
    fetchIncidents()
  })

  return {
    entries, incidents, stats, loading, saving, error,
    fetchEntries, addEntry, fetchIncidents, addIncident, fetchStats
  }
})
