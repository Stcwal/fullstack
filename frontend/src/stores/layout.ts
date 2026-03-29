import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useLayoutStore = defineStore('layout', () => {
  const isTabletMode = ref(false)
  const isSidebarCollapsed = ref(false)

  function toggleTabletMode() {
    isTabletMode.value = !isTabletMode.value
  }

  function toggleSidebar() {
    isSidebarCollapsed.value = !isSidebarCollapsed.value
  }

  return { isTabletMode, isSidebarCollapsed, toggleTabletMode, toggleSidebar }
})
