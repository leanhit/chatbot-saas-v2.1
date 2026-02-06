import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { tenantMembershipApi } from '@/api/tenantMembershipApi'
import { ElMessage } from 'element-plus'
import type { InvitationResponse } from '@/types/tenant'

export const useUserInvitationStore = defineStore('userInvitation', () => {
  const invitations = ref<InvitationResponse[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  const fetchInvitations = async () => {
    loading.value = true
    error.value = null
    try {
      const response = await tenantMembershipApi.getUserInvitations()
      invitations.value = response.data || []
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Failed to fetch invitations'
      ElMessage.error(error.value || 'An error occurred')
      console.error('Error fetching user invitations:', err)
    } finally {
      loading.value = false
    }
  }

  const acceptInvitation = async (invitationId: number) => {
    try {
      const invitation = invitations.value.find(i => i.id === invitationId)
      if (!invitation) throw new Error('Invitation not found')
      
      await tenantMembershipApi.acceptInvitation(invitation.tenantId, invitationId.toString())
      await fetchInvitations()
      ElMessage.success('Đã chấp nhận lời mời tham gia tenant')
      return true
    } catch (err: any) {
      const errorMsg = err.response?.data?.message || 'Không thể chấp nhận lời mời'
      ElMessage.error(errorMsg)
      console.error('Error accepting invitation:', err)
      return false
    }
  }

  const rejectInvitation = async (invitationId: number) => {
    try {
      const invitation = invitations.value.find(i => i.id === invitationId)
      if (!invitation) throw new Error('Invitation not found')
      
      await tenantMembershipApi.rejectInvitation(invitation.tenantId, invitationId.toString())
      await fetchInvitations()
      ElMessage.success('Đã từ chối lời mời')
      return true
    } catch (err: any) {
      const errorMsg = err.response?.data?.message || 'Không thể từ chối lời mời'
      ElMessage.error(errorMsg)
      console.error('Error rejecting invitation:', err)
      return false
    }
  }

  const pendingInvitations = computed(() => 
    invitations.value.filter(i => i.status === 'PENDING')
  )

  return {
    invitations,
    pendingInvitations,
    loading,
    error,
    fetchInvitations,
    acceptInvitation,
    rejectInvitation
  }
})
