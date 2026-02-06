import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'

/**
 * App Store - Manages app data and enabled state
 * Source: App Hub API
 * Responsibility: Fetch and cache app list with enabled status
 */
export const useAppStore = defineStore('app', () => {
  const apps = ref([])
  const loading = ref(false)
  const error = ref(null)

  // Computed properties
  const appsById = computed(() => {
    const map = {}
    apps.value.forEach(app => {
      map[app.appCode] = app
    })
    return map
  })

  const enabledApps = computed(() => {
    return apps.value.filter(app => app.enabled)
  })

  // Actions
  async function fetchApps() {
    loading.value = true
    error.value = null
    
    try {
      // Backend automatically uses current tenant context
      const response = await api.get('/api/tenants/current/apps')
      apps.value = response.data
    } catch (err) {
      error.value = err.message
      console.error('Failed to fetch apps:', err)
    } finally {
      loading.value = false
    }
  }

  function getAppByCode(appCode) {
    return appsById.value[appCode]
  }

  function isAppEnabled(appCode) {
    const app = getAppByCode(appCode)
    return app?.enabled || false
  }

  return {
    // State
    apps,
    loading,
    error,
    
    // Computed
    appsById,
    enabledApps,
    
    // Actions
    fetchApps,
    getAppByCode,
    isAppEnabled
  }
})
