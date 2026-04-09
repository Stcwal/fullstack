import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useAuthStore } from './auth'
import { organizationService } from '@/services/organization.service'

export interface ShiftWorker {
  id: number
  firstName: string
  lastName: string
  role: string
}

export const useShiftStore = defineStore('shift', () => {
  const authStore = useAuthStore()

  const workers    = ref<ShiftWorker[]>([])
  const activeId   = ref<number | null>(null)
  const loaded     = ref(false)

  async function loadWorkers() {
    if (loaded.value) return
    try {
      const users = await organizationService.getUsers()
      workers.value = users
        .filter(u => u.isActive)
        .map(u => ({ id: u.id, firstName: u.firstName, lastName: u.lastName, role: u.role }))
    } catch {
      // fallback: only the current auth user
    } finally {
      // always add auth user if not present
      const authUser = authStore.user
      if (authUser && !workers.value.find(w => w.id === authUser.id)) {
        workers.value.unshift({
          id: authUser.id,
          firstName: authUser.firstName,
          lastName: authUser.lastName,
          role: authUser.role,
        })
      }
      loaded.value = true
    }
  }

  function setActive(id: number) {
    activeId.value = id
  }

  /** Name of the active shift worker; falls back to auth user */
  const activeWorkerName = computed<string>(() => {
    const worker = workers.value.find(w => w.id === activeId.value)
    if (worker) return [worker.firstName, worker.lastName].filter(Boolean).join(' ')
    const u = authStore.user
    return u ? ([u.firstName, u.lastName].filter(Boolean).join(' ') || u.email || '—') : '—'
  })

  const activeWorker = computed<ShiftWorker | null>(
    () => workers.value.find(w => w.id === activeId.value) ?? null
  )

  /** ID of the active shift worker; null if none explicitly selected */
  const activeWorkerId = computed<number | null>(() => activeId.value)

  return { workers, activeId, loaded, loadWorkers, setActive, activeWorkerName, activeWorker, activeWorkerId }
})
