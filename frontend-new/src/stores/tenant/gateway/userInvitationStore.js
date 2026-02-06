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
      console.error('fetchUserInvitations error:', error)
      error.value = error.response?.data?.message || 'Không thể lấy lời mời'
    } finally {
      loading.value = false
    }
  }

  const acceptInvitation = async (invitationId) => {
    try {
      await tenantApi.acceptInvitation(invitationId)
      invitations.value = invitations.value.filter(inv => inv.id !== invitationId)
    } catch (error) {
      console.error('acceptInvitation error:', error)
      throw error
    }
  }

  const rejectInvitation = async (invitationId) => {
    try {
      await tenantApi.rejectInvitation(invitationId)
      invitations.value = invitations.value.filter(inv => inv.id !== invitationId)
    } catch (error) {
      console.error('rejectInvitation error:', error)
      throw error
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
