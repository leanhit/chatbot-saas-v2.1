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
      error.value = error.response?.data?.message || 'Không thể lấy yêu cầu đang chờ'
    } finally {
      loading.value = false
    }
  }
    // Temporarily disable user invitations API due to 404 error
    // const fetchUserInvitations = async () => {
    //   loading.value = true
    //   error.value = null
    //   try {
    //     const { data } = await tenantApi.getUserInvitations()
    //     invitations.value = data || []
    //   } catch (error) {
    //     error.value = error.response?.data?.message || 'Không thể lấy lời mời'
    //   } finally {
    //     loading.value = false
    //   }
    // }
  const approveRequest = async (requestId) => {
    // Users cannot approve their own requests - this is for tenant admins only
    console.log('Users cannot approve their own join requests. This action is only available to tenant administrators.')
    throw new Error('Cannot approve own request')
  }
  
  const rejectRequest = async (requestId) => {
    try {
      // Cancel/withdraw user's own join request
      console.log('Canceling join request:', requestId)
      
      // Call the new backend endpoint
      await tenantApi.cancelJoinRequest(requestId)
      
      // Remove from local state
      pendingRequests.value = pendingRequests.value.filter(req => req.id !== requestId)
      
      return { success: true, message: 'Join request cancelled' }
    } catch (error) {
      console.error('Failed to cancel request:', error)
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
