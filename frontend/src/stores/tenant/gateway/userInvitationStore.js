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
      const { data } = await tenantApi.getUserInvitations()
      invitations.value = data || []
    } catch (error) {
      error.value = error.response?.data?.message || 'Không thể lấy lời mời'
    } finally {
      loading.value = false
    }
  }
  const acceptInvitation = async (invitationId, token) => {
    try {
      await tenantApi.acceptInvitation(token)
      // Remove from local state after accepting
      invitations.value = invitations.value.filter(inv => inv.id !== invitationId)
    } catch (error) {
      error.value = error.response?.data?.message || 'Không thể chấp nhận lời mời'
    }
  }
  const rejectInvitation = async (invitationId, token) => {
    try {
      await tenantApi.rejectInvitation(token)
      // Remove from local state after rejecting
      invitations.value = invitations.value.filter(inv => inv.id !== invitationId)
    } catch (error) {
      error.value = error.response?.data?.message || 'Không thể từ chối lời mời'
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
