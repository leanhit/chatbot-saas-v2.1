import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from '@/plugins/axios'

export const useBillingStore = defineStore('billing', () => {
  // State
  const subscriptions = ref([])
  const entitlements = ref([])
  const billingAccounts = ref([])
  const invoices = ref([])
  const currentSubscription = ref(null)
  const isLoading = ref(false)
  const error = ref(null)

  // Getters
  const activeSubscriptions = computed(() => 
    subscriptions.value.filter(sub => sub.status === 'ACTIVE')
  )

  const totalMonthlyCost = computed(() => 
    activeSubscriptions.value.reduce((total, sub) => total + (sub.monthlyPrice || 0), 0)
  )

  const entitlementsByFeature = computed(() => {
    const grouped = {}
    entitlements.value.forEach(ent => {
      if (!grouped[ent.feature]) {
        grouped[ent.feature] = []
      }
      grouped[ent.feature].push(ent)
    })
    return grouped
  })

  const nearLimitEntitlements = computed(() => 
    entitlements.value.filter(ent => ent.isNearLimit && ent.isEnabled)
  )

  const overLimitEntitlements = computed(() => 
    entitlements.value.filter(ent => ent.isOverLimit && ent.isEnabled)
  )

  // Actions
  const fetchTenantBilling = async (tenantId) => {
    isLoading.value = true
    error.value = null
    
    try {
      // Fetch billing data from existing backend endpoints
      const [subsRes, entRes, accRes, invRes] = await Promise.all([
        axios.get(`/billing/subscriptions?tenantId=${tenantId}`),
        axios.get(`/billing/entitlements?tenantId=${tenantId}`),
        axios.get(`/billing/accounts?tenantId=${tenantId}`),
        axios.get(`/billing/invoices?tenantId=${tenantId}`)
      ])

      subscriptions.value = subsRes.data || []
      entitlements.value = entRes.data || []
      billingAccounts.value = accRes.data || []
      invoices.value = invRes.data || []
      
      // Set current active subscription
      currentSubscription.value = subscriptions.value.find(s => s.status === 'ACTIVE')
      
    } catch (err) {
      error.value = err.response?.data?.message || err.message
      console.error('Failed to fetch billing data:', err)
    } finally {
      isLoading.value = false
    }
  }

  const upgradeSubscription = async (subscriptionId, newPlanId) => {
    isLoading.value = true
    error.value = null
    
    try {
      const response = await axios.post(`/billing/subscriptions/${subscriptionId}/upgrade`, {
        newPlanId
      })
      
      // Update local state
      const index = subscriptions.value.findIndex(s => s.id === subscriptionId)
      if (index !== -1) {
        subscriptions.value[index] = response.data
      }
      
      return response.data
    } catch (err) {
      error.value = err.response?.data?.message || err.message
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const cancelSubscription = async (subscriptionId) => {
    isLoading.value = true
    error.value = null
    
    try {
      await axios.delete(`/billing/subscriptions/${subscriptionId}`)
      
      // Update local state
      const index = subscriptions.value.findIndex(s => s.id === subscriptionId)
      if (index !== -1) {
        subscriptions.value[index].status = 'CANCELLED'
      }
      
    } catch (err) {
      error.value = err.response?.data?.message || err.message
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const checkUsage = async (feature, amount) => {
    try {
      const response = await axios.post(`/billing/entitlements/check`, {
        feature,
        amount
      })
      return response.data
    } catch (err) {
      error.value = err.response?.data?.message || err.message
      throw err
    }
  }

  const addUsage = async (feature, amount) => {
    try {
      const response = await axios.post(`/billing/entitlements/usage`, {
        feature,
        amount
      })
      
      // Update local entitlement
      const entitlement = entitlements.value.find(e => e.feature === feature)
      if (entitlement) {
        Object.assign(entitlement, response.data)
      }
      
      return response.data
    } catch (err) {
      error.value = err.response?.data?.message || err.message
      throw err
    }
  }

  const createBillingAccount = async (tenantId, accountData) => {
    isLoading.value = true
    error.value = null
    
    try {
      const response = await axios.post(`/billing/accounts`, {
        tenantId,
        ...accountData
      })
      
      billingAccounts.value.push(response.data)
      return response.data
    } catch (err) {
      error.value = err.response?.data?.message || err.message
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const getBillingSummary = async (tenantId) => {
    try {
      const response = await axios.get(`/billing/summary?tenantId=${tenantId}`)
      return response.data
    } catch (err) {
      error.value = err.response?.data?.message || err.message
      throw err
    }
  }

  const getUsageHistory = async (feature, period = '30d') => {
    try {
      const response = await axios.get(`/billing/entitlements/usage/${feature}?period=${period}`)
      return response.data
    } catch (err) {
      error.value = err.response?.data?.message || err.message
      throw err
    }
  }

  // Reset state
  const resetBillingState = () => {
    subscriptions.value = []
    entitlements.value = []
    billingAccounts.value = []
    invoices.value = []
    currentSubscription.value = null
    error.value = null
  }

  return {
    // State
    subscriptions,
    entitlements,
    billingAccounts,
    invoices,
    currentSubscription,
    isLoading,
    error,
    
    // Getters
    activeSubscriptions,
    totalMonthlyCost,
    entitlementsByFeature,
    nearLimitEntitlements,
    overLimitEntitlements,
    
    // Actions
    fetchTenantBilling,
    upgradeSubscription,
    cancelSubscription,
    checkUsage,
    addUsage,
    createBillingAccount,
    getBillingSummary,
    getUsageHistory,
    resetBillingState
  }
})
