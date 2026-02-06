import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'

/**
 * Billing Store - Manages entitlement information
 * Source: Billing Hub API (READ-ONLY)
 * Responsibility: Fetch and cache billing entitlement data for UI display only
 */
export const useBillingStore = defineStore('billing', () => {
  const entitlements = ref({})
  const currentPlan = ref(null)
  const loading = ref(false)
  const error = ref(null)

  // Computed properties
  const planAllowsApp = computed(() => {
    return (appCode) => {
      return entitlements.value[appCode]?.allowed || false
    }
  })

  const getEntitlementReason = computed(() => {
    return (appCode) => {
      return entitlements.value[appCode]?.reason || 'Unknown'
    }
  })

  // Actions
  async function fetchEntitlements() {
    loading.value = true
    error.value = null
    
    try {
      // Backend automatically uses current tenant context
      const response = await api.get('/api/billing/entitlements')
      
      // Transform array to object for easier lookup
      const entitlementMap = {}
      response.data.forEach(entitlement => {
        entitlementMap[entitlement.appCode] = entitlement
      })
      
      entitlements.value = entitlementMap
    } catch (err) {
      error.value = err.message
      console.error('Failed to fetch entitlements:', err)
    } finally {
      loading.value = false
    }
  }

  async function fetchCurrentPlan() {
    loading.value = true
    error.value = null
    
    try {
      // Backend automatically uses current tenant context
      const response = await api.get('/api/billing/tenants/current/plan')
      currentPlan.value = response.data
    } catch (err) {
      error.value = err.message
      console.error('Failed to fetch current plan:', err)
    } finally {
      loading.value = false
    }
  }

  function isAppEntitled(appCode) {
    return planAllowsApp.value(appCode)
  }

  return {
    // State
    entitlements,
    currentPlan,
    loading,
    error,
    
    // Computed
    planAllowsApp,
    getEntitlementReason,
    
    // Actions
    fetchEntitlements,
    fetchCurrentPlan,
    isAppEntitled
  }
})
