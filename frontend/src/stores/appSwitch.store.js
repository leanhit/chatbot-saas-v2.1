import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useAppStore } from './app.store'
import { useBillingStore } from './billing.store'
import { useWalletStore } from './wallet.store'

/**
 * App Switch Store - UI aggregation only
 * Responsibility: Combine data from multiple hubs for App Switch UI display
 * Note: This store does NOT make decisions, only aggregates for UI purposes
 */
export const useAppSwitchStore = defineStore('appSwitch', () => {
  const loading = ref(false)
  const error = ref(null)

  // Get other stores
  const appStore = useAppStore()
  const billingStore = useBillingStore()
  const walletStore = useWalletStore()

  // Computed properties - UI aggregation only
  const appsWithStatus = computed(() => {
    return appStore.apps.map(app => {
      const appCode = app.appCode
      const isEnabled = app.enabled
      const isEntitled = billingStore.isAppEntitled(appCode)
      const walletStatus = walletStore.walletStatus
      
      // UI-only computation - backend will re-validate
      const isUsable = isEnabled && isEntitled && walletStatus === 'OK'
      
      // Determine status message for UI display
      let statusMessage = 'Enabled'
      let statusType = 'success'
      
      if (!isEnabled) {
        statusMessage = 'Disabled by workspace'
        statusType = 'warning'
      } else if (!isEntitled) {
        statusMessage = billingStore.getEntitlementReason(appCode)
        statusType = 'error'
      } else if (walletStatus !== 'OK') {
        statusMessage = walletStore.getWalletStatusMessage()
        statusType = 'warning'
      }
      
      return {
        ...app,
        isUsable, // UI-only state
        statusMessage,
        statusType,
        // Raw data for debugging
        _debug: {
          enabled: isEnabled,
          entitled: isEntitled,
          walletStatus,
          billingReason: billingStore.getEntitlementReason(appCode)
        }
      }
    })
  })

  const usableApps = computed(() => {
    return appsWithStatus.value.filter(app => app.isUsable)
  })

  const hasLoadingIssues = computed(() => {
    return appStore.error || billingStore.error || walletStore.error
  })

  const isLoading = computed(() => {
    return appStore.loading || billingStore.loading || walletStore.loading || loading.value
  })

  // Actions
  async function loadAppSwitchData() {
    loading.value = true
    error.value = null
    
    try {
      // Load data from all hubs in parallel
      await Promise.all([
        appStore.fetchApps(),
        billingStore.fetchEntitlements(),
        walletStore.fetchWallet()
      ])
    } catch (err) {
      error.value = err.message
      console.error('Failed to load app switch data:', err)
    } finally {
      loading.value = false
    }
  }

  function getAppStatus(appCode) {
    const app = appsWithStatus.value.find(app => app.appCode === appCode)
    return app ? {
      isUsable: app.isUsable,
      statusMessage: app.statusMessage,
      statusType: app.statusType
    } : null
  }

  function canNavigateToApp(appCode) {
    // UI-only check - backend will validate
    const status = getAppStatus(appCode)
    return status?.isUsable || false
  }

  function navigateToApp(appCode, router) {
    if (canNavigateToApp(appCode)) {
      // Navigate to app - backend will validate access
      router.push(`/apps/${appCode.toLowerCase()}`)
    }
  }

  return {
    // State
    loading,
    error,
    
    // Computed
    appsWithStatus,
    usableApps,
    hasLoadingIssues,
    isLoading,
    
    // Actions
    loadAppSwitchData,
    getAppStatus,
    canNavigateToApp,
    navigateToApp
  }
})
