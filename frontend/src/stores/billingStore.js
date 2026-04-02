import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { billingApi } from '@/api/billingApi'
import { ACTIVE_TENANT_ID } from '@/utils/constant'

export const useBillingStore = defineStore('billing', () => {
  // State
  const billingAccount = ref(null)
  const subscription = ref(null)
  const currentPlan = ref(null)
  const entitlements = ref([])
  const usage = ref({})
  const paymentMethods = ref([])
  const recentInvoices = ref([])
  const availablePlans = ref([])
  const loading = ref(false)
  const error = ref(null)

  // Getters
  const isSubscriptionActive = computed(() => {
    return subscription.value?.status === 'ACTIVE'
  })
  
  const daysUntilExpiry = computed(() => {
    if (!subscription.value?.endsAt) return null
    const today = new Date()
    const expiryDate = new Date(subscription.value.endsAt)
    const diffTime = Math.abs(expiryDate - today)
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  })

  // Actions
  const fetchBillingAccount = async () => {
    const tenantKey = localStorage.getItem(ACTIVE_TENANT_ID)
    if (!tenantKey) {
      error.value = 'No tenant selected'
      return
    }

    loading.value = true
    try {
      console.log('Fetching billing account for tenant:', tenantKey)
      const response = await billingApi.getBillingAccount(tenantKey)
      billingAccount.value = response.data
    } catch (err) {
      error.value = err.message || 'Failed to fetch billing account'
      throw err
    } finally {
      loading.value = false
    }
  }

  const fetchSubscription = async () => {
    const tenantKey = localStorage.getItem(ACTIVE_TENANT_ID)
    if (!tenantKey) {
      error.value = 'No tenant selected'
      return
    }

    loading.value = true
    try {
      console.log('🔄 [BillingStore] Fetching subscription for tenant:', tenantKey)
      
      // Get tenant data from /tenants/me instead of billing API
      const response = await fetch(`/api/tenants/me`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        }
      })
      
      if (!response.ok) {
        throw new Error('Failed to fetch tenant data')
      }
      
      const tenants = await response.json()
      if (tenants && tenants.length > 0) {
        const tenant = tenants[0]
        console.log('📦 [BillingStore] Tenant data:', tenant)
        
        // Map tenant data to subscription format
        subscription.value = {
          status: tenant.status === 'ACTIVE' ? 'ACTIVE' : 'INACTIVE',
          endsAt: tenant.expiresAt,
          plan: {
            id: tenant.currentPackageId,
            name: tenant.currentPackageName,
            packageId: tenant.currentPackageId
          }
        }
        
        currentPlan.value = subscription.value.plan
        console.log('✅ [BillingStore] Subscription loaded:', subscription.value)
      }
    } catch (err) {
      console.error('❌ [BillingStore] Error fetching subscription:', err)
      error.value = err.message || 'Failed to fetch subscription'
      throw err
    } finally {
      loading.value = false
    }
  }

  const fetchEntitlements = async () => {
    const tenantKey = localStorage.getItem(ACTIVE_TENANT_ID)
    if (!tenantKey) {
      error.value = 'No tenant selected'
      return
    }

    loading.value = true
    try {
      console.log('Fetching entitlements for tenant:', tenantKey)
      const response = await billingApi.getEntitlements(tenantKey)
      entitlements.value = response.data
    } catch (err) {
      error.value = err.message || 'Failed to fetch entitlements'
      throw err
    } finally {
      loading.value = false
    }
  }

  const fetchUsage = async () => {
    const tenantKey = localStorage.getItem(ACTIVE_TENANT_ID)
    if (!tenantKey) {
      error.value = 'No tenant selected'
      return
    }

    loading.value = true
    try {
      console.log('Fetching usage for tenant:', tenantKey)
      const response = await billingApi.getUsage(tenantKey)
      usage.value = response.data
    } catch (err) {
      error.value = err.message || 'Failed to fetch usage'
      throw err
    } finally {
      loading.value = false
    }
  }

  const fetchPaymentMethods = async () => {
    const tenantKey = localStorage.getItem(ACTIVE_TENANT_ID)
    if (!tenantKey) {
      error.value = 'No tenant selected'
      return
    }

    loading.value = true
    try {
      console.log('Fetching payment methods for tenant:', tenantKey)
      const response = await billingApi.getPaymentMethods(tenantKey)
      paymentMethods.value = response.data
    } catch (err) {
      error.value = err.message || 'Failed to fetch payment methods'
      throw err
    } finally {
      loading.value = false
    }
  }

  const fetchRecentInvoices = async () => {
    const tenantKey = localStorage.getItem(ACTIVE_TENANT_ID)
    if (!tenantKey) {
      error.value = 'No tenant selected'
      return
    }

    loading.value = true
    try {
      console.log('Fetching recent invoices for tenant:', tenantKey)
      const response = await billingApi.getRecentInvoices(tenantKey)
      recentInvoices.value = response.data
    } catch (err) {
      error.value = err.message || 'Failed to fetch recent invoices'
      throw err
    } finally {
      loading.value = false
    }
  }

  const fetchAvailablePlans = async () => {
    loading.value = true
    try {
      const response = await billingApi.getAvailablePlans()
      availablePlans.value = response.data
    } catch (err) {
      error.value = err.message || 'Failed to fetch available plans'
      throw err
    } finally {
      loading.value = false
    }
  }

  const upgradeSubscription = async (planId) => {
    loading.value = true
    try {
      const response = await billingApi.upgradeSubscription(planId)
      subscription.value = response.data
      currentPlan.value = response.data.plan
      await fetchEntitlements()
      return response.data
    } catch (err) {
      error.value = err.message || 'Failed to upgrade subscription'
      throw err
    } finally {
      loading.value = false
    }
  }

  const cancelSubscription = async (reason) => {
    loading.value = true
    try {
      const response = await billingApi.cancelSubscription(reason)
      subscription.value = response.data
      return response.data
    } catch (err) {
      error.value = err.message || 'Failed to cancel subscription'
      throw err
    } finally {
      loading.value = false
    }
  }

  const addPaymentMethod = async (paymentData) => {
    loading.value = true
    try {
      const response = await billingApi.addPaymentMethod(paymentData)
      paymentMethods.value.push(response.data)
      return response.data
    } catch (err) {
      error.value = err.message || 'Failed to add payment method'
      throw err
    } finally {
      loading.value = false
    }
  }

  const removePaymentMethod = async (methodId) => {
    loading.value = true
    try {
      await billingApi.removePaymentMethod(methodId)
      paymentMethods.value = paymentMethods.value.filter(m => m.id !== methodId)
    } catch (err) {
      error.value = err.message || 'Failed to remove payment method'
      throw err
    } finally {
      loading.value = false
    }
  }

  const setDefaultPaymentMethod = async (methodId) => {
    loading.value = true
    try {
      await billingApi.setDefaultPaymentMethod(methodId)
      paymentMethods.value.forEach(method => {
        method.isDefault = method.id === methodId
      })
    } catch (err) {
      error.value = err.message || 'Failed to set default payment method'
      throw err
    } finally {
      loading.value = false
    }
  }

  const toggleAutoRenewal = async (enabled) => {
    loading.value = true
    try {
      const response = await billingApi.toggleAutoRenewal(enabled)
      subscription.value.autoRenew = enabled
      return response.data
    } catch (err) {
      error.value = err.message || 'Failed to toggle auto renewal'
      throw err
    } finally {
      loading.value = false
    }
  }

  const clearError = () => {
    error.value = null
  }

  return {
    // State
    billingAccount,
    subscription,
    currentPlan,
    entitlements,
    usage,
    paymentMethods,
    recentInvoices,
    availablePlans,
    loading,
    error,
    
    // Getters
    isSubscriptionActive,
    daysUntilExpiry,
    
    // Actions
    fetchBillingAccount,
    fetchSubscription,
    fetchEntitlements,
    fetchUsage,
    fetchPaymentMethods,
    fetchRecentInvoices,
    fetchAvailablePlans,
    upgradeSubscription,
    cancelSubscription,
    addPaymentMethod,
    removePaymentMethod,
    setDefaultPaymentMethod,
    toggleAutoRenewal,
    clearError
  }
})
