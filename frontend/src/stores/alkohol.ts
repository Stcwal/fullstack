import { defineStore } from 'pinia'
import { ref } from 'vue'
import { alkoholService } from '@/services/alkohol.service'
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

  async function fetchEntries() {
    loading.value = true
    error.value = null
    try {
      entries.value = await alkoholService.getAlderskontrollEntries()
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
    loading.value = true
    error.value = null
    try {
      incidents.value = await alkoholService.getIncidents()
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

  return {
    entries, incidents, stats, loading, saving, error,
    fetchEntries, addEntry, fetchIncidents, addIncident, fetchStats
  }
})
