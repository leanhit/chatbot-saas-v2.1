import { defineStore } from 'pinia'
import { ref } from 'vue'
import { tenantMembershipApi } from '@/api/tenantMembershipApi'
import type { TenantPendingResponse } from '@/types/tenant'

export const useGatewayPendingStore = defineStore('gateway-pending', () => {
  const pendingTenants = ref<TenantPendingResponse[]>([])
  const loading = ref(false)

  const fetchMyPendingTenants = async () => {
    loading.value = true
    try {
      const { data } = await tenantMembershipApi.getMyPending()
      pendingTenants.value = data
    } catch (error) {
      console.error('fetchMyPendingTenants error:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const clearPending = () => {
    pendingTenants.value = []
  }

  return {
    pendingTenants,
    loading,
    fetchMyPendingTenants,
    clearPending
  }
})
