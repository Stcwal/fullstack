import { defineStore } from 'pinia'
import { ref } from 'vue'
import { organizationService } from '@/services/organization.service'

interface Location { id: number; name: string }

export const useLocationStore = defineStore('location', () => {
  const locations = ref<Location[]>([])
  const activeLocationId = ref<number | null>(null)

  async function fetchLocations() {
    try {
      locations.value = await organizationService.getLocations()
    } catch {
      // non-fatal — location switcher just won't populate
    }
  }

  function setLocation(id: number | null) {
    activeLocationId.value = id
  }

  return { locations, activeLocationId, fetchLocations, setLocation }
})
