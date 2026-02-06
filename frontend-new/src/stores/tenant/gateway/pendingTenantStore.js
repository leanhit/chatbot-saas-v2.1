import { defineStore } from 'pinia'
import { ref } from 'vue'
import { tenantApi } from '@/api/tenantApi'

export const useGatewayPendingTenantStore = defineStore('gateway-pending-tenant', () => {
  const loading = ref(false)
  const pendingRequests = ref([])
  const error = ref(null)

  const fetchPendingRequests = async () => {
    loading.value = true
    error.value = null

    try {
      const { data } = await tenantApi.getPendingRequests()
      pendingRequests.value = data || []
    } catch (error) {
      console.error('fetchPendingRequests error:', error)
      error.value = error.response?.data?.message || 'Không thể lấy yêu cầu đang chờ'
    } finally {
      loading.value = false
    }
  }

  const approveRequest = async (requestId) => {
    try {
      await tenantApi.approvePendingRequest(requestId)
      pendingRequests.value = pendingRequests.value.filter(req => req.id !== requestId)
    } catch (error) {
      console.error('approveRequest error:', error)
      throw error
    }
  }

  const rejectRequest = async (requestId) => {
    try {
      await tenantApi.rejectPendingRequest(requestId)
      pendingRequests.value = pendingRequests.value.filter(req => req.id !== requestId)
    } catch (error) {
      console.error('rejectRequest error:', error)
      throw error
    }
  }

  return {
    loading,
    pendingRequests,
    error,
    fetchPendingRequests,
    approveRequest,
    rejectRequest
  }
})
