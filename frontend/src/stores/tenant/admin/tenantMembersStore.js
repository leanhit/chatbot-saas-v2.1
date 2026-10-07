import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { tenantApi } from '@/api/tenantApi'
import { ACTIVE_TENANT_ID } from '@/utils/constant'
import { MembershipStatus, InvitationStatus, TenantRole } from '@/types/tenant'

export const useTenantAdminMembersStore = defineStore(
  'tenantAdminMembers',
  () => {
    // ======================
    // STATE
    // ======================
    const members = ref([])
    const invitations = ref([])
    const joinRequests = ref([])
    const loading = ref(false)
    const activeTenantId = localStorage.getItem(ACTIVE_TENANT_ID) || ''

    // ======================
    // GETTERS
    // ======================
    /** Thành viên chính thức */
    const activeMembers = computed(() =>
      members.value.filter(m => m.status === MembershipStatus.ACTIVE || !m.status)
    )
    /** Lời mời đang chờ */
    const pendingInvitations = computed(() =>
      invitations.value.filter(i => i.status === InvitationStatus.PENDING || i.status === 'PENDING')
    )
    /** User xin vào tenant */
    const pendingMembers = computed(() =>
      joinRequests.value.filter(r => r.status === MembershipStatus.PENDING || r.status === 'PENDING')
    )

    // ======================
    // ACTIONS - MEMBERS
    // ======================
    const fetchMembers = async () => {
      if (!activeTenantId) return
      loading.value = true
      try {
        const res = await tenantApi.getTenantMembers(activeTenantId)
        members.value = Array.isArray(res.data)
          ? res.data
          : (res.data)?.content || []
      } finally {
        loading.value = false
      }
    }

    const updateRole = async (userId, role) => {
      await tenantApi.updateMemberRole(activeTenantId, userId, role)
      const user = members.value.find(m => m.userId === userId || m.id === userId)
      if (user) user.role = role
    }

    const removeMember = async (userId) => {
      await tenantApi.removeMember(activeTenantId, userId)
      members.value = members.value.filter(m => m.userId !== userId && m.id !== userId)
    }

    // ======================
    // ACTIONS - INVITATIONS
    // ======================
    const fetchInvitations = async () => {
      if (!activeTenantId) return
      try {
        const res = await tenantApi.getTenantInvitations(activeTenantId)
        invitations.value = res.data || []
      } catch (error) {
        console.error('Failed to fetch invitations:', error)
      }
    }

    const inviteUser = async (payload) => {
      try {
        await tenantApi.inviteMember(activeTenantId, payload)
        await fetchInvitations()
      } catch (error) {
        console.error('Failed to invite user:', error)
        throw error
      }
    }

    const revokeInvitationAction = async (invitationId) => {
      try {
        await tenantApi.revokeInvitation(
          activeTenantId,
          invitationId
        )
        invitations.value = invitations.value.filter(
          i => i.id !== invitationId
        )
      } catch (error) {
        console.error('Failed to revoke invitation:', error)
        throw error
      }
    }

    // ======================
    // ACTIONS - JOIN REQUESTS
    // ======================
    const fetchJoinRequests = async () => {
      if (!activeTenantId) return
      loading.value = true
      try {
        const res = await tenantApi.getJoinRequests(activeTenantId)
        joinRequests.value = res.data || []
      } finally {
        loading.value = false
      }
    }

    const approveJoin = async (requestId) => {
      await tenantApi.updateJoinRequestStatus(
        activeTenantId,
        requestId,
        'APPROVED'
      )
      joinRequests.value = joinRequests.value.filter(
        r => r.id !== requestId
      )
      await fetchMembers() // user trở thành member
    }

    const rejectJoin = async (requestId) => {
      await tenantApi.updateJoinRequestStatus(
        activeTenantId,
        requestId,
        'REJECTED'
      )
      joinRequests.value = joinRequests.value.filter(
        r => r.id !== requestId
      )
    }

    // ======================
    // EXPORT
    // ======================
    return {
      // state
      members,
      invitations,
      joinRequests,
      loading,
      activeTenantId,
      // getters
      activeMembers,
      pendingInvitations,
      pendingMembers,
      // actions
      fetchMembers,
      updateRole,
      removeMember,
      fetchInvitations,
      inviteUser,
      revokeInvitationAction,
      fetchJoinRequests,
      approveJoin,
      rejectJoin
    }
  }
)
