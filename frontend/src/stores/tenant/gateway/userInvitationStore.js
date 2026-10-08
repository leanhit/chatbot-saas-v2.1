import { defineStore } from 'pinia'
import { ref } from 'vue'
import { tenantApi } from '@/api/tenantApi'
export const useGatewayUserInvitationStore = defineStore('gateway-user-invitation', () => {
  const loading = ref(false)
  const invitations = ref([])
  const error = ref(null)
  const fetchUserInvitations = async () => {
    loading.value = true
    error.value = null
    try {
      const { data } = await tenantApi.getMyInvitations()
      invitations.value = data || []
    } catch (error) {
      error.value = error.response?.data?.message || 'Không thể lấy lời mời'
    } finally {
      loading.value = false
    }
  }
  const acceptInvitation = async (invitationId, token) => {
    try {
      const targetToken = token || invitationId
      await tenantApi.acceptInvitation(targetToken)
      // Remove from local state after accepting
      invitations.value = invitations.value.filter(inv => inv.id !== invitationId && inv.token !== targetToken)
    } catch (err) {
      error.value = err.response?.data?.message || 'Không thể chấp nhận lời mời'
      throw err
    }
  }
  const rejectInvitation = async (invitationId, token) => {
    try {
      const targetToken = token || invitationId
      await tenantApi.rejectInvitation(targetToken)
      // Remove from local state after rejecting
      invitations.value = invitations.value.filter(inv => inv.id !== invitationId && inv.token !== targetToken)
    } catch (err) {
      error.value = err.response?.data?.message || 'Không thể từ chối lời mời'
      throw err
    }
  }
  return {
    loading,
    invitations,
    error,
    fetchUserInvitations,
    acceptInvitation,
    rejectInvitation
  }
})
