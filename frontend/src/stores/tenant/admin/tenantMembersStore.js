import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { tenantMembershipApi } from '@/api/tenantMembershipApi'
import { ACTIVE_TENANT_ID } from '@/utils/constant'
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
      members.value.filter(m => m.status === 'ACTIVE')
    )
    /** Lời mời đang chờ */
    const pendingInvitations = computed(() =>
      invitations.value.filter(i => i.status === 'PENDING')
    )
    /** User xin vào tenant */
    const pendingMembers = computed(() =>
      joinRequests.value.filter(r => r.status === 'PENDING')
    )
    // ======================
    // ACTIONS - MEMBERS
    // ======================
    const fetchMembers = async () => {
      if (!activeTenantId) return
      loading.value = true
      try {
        const res = await tenantMembershipApi.getTenantUsers(activeTenantId)
        members.value = Array.isArray(res.data)
          ? res.data
          : (res.data)?.content || []
      } finally {
        loading.value = false
      }
    }
    const updateRole = async (userId, role) => {
      await tenantMembershipApi.updateUserRole(activeTenantId, userId, role)
      const user = members.value.find(m => m.userId === userId)
      if (user) user.role = role
      // ElMessage.success('Đã cập nhật vai trò') // Comment out for Windzo
    }
    const removeMember = async (userId) => {
      await tenantMembershipApi.removeUserFromTenant(activeTenantId, userId)
      members.value = members.value.filter(m => m.userId !== userId)
      // ElMessage.success('Đã xóa thành viên') // Comment out for Windzo
    }
    // ======================
    // ACTIONS - INVITATIONS
    // ======================
    const fetchInvitations = async () => {
      if (!activeTenantId) return
      try {
        const res = await tenantMembershipApi.getTenantInvitations(activeTenantId)
        invitations.value = res.data || []
      } catch (error) {
      }
    }
    const inviteUser = async (payload) => {
      try {
        await tenantMembershipApi.inviteMember(activeTenantId, payload)
        // ElMessage.success('Đã gửi lời mời') // Comment out for Windzo
        await fetchInvitations()
      } catch (error) {
        // ElMessage.error(
        //   error.response?.data?.message || 'Không thể gửi lời mời'
        // )
      }
    }
    const revokeInvitationAction = async (invitationId) => {
      try {
        await tenantMembershipApi.revokeInvitation(
          activeTenantId,
          invitationId
        )
        invitations.value = invitations.value.filter(
          i => i.id !== invitationId
        )
        // ElMessage.success('Đã thu hồi lời mời') // Comment out for Windzo
      } catch {
        // ElMessage.error('Không thể thu hồi lời mời') // Comment out for Windzo
      }
    }
    // ======================
    // ACTIONS - JOIN REQUESTS
    // ======================
    const fetchJoinRequests = async () => {
      if (!activeTenantId) return
      loading.value = true
      try {
        const res =
          await tenantMembershipApi.getTenantJoinRequests(activeTenantId)
        joinRequests.value = res.data || []
      } finally {
        loading.value = false
      }
    }
    const approveJoin = async (requestId) => {
      await tenantMembershipApi.approveJoinRequest(
        activeTenantId,
        requestId
      )
      joinRequests.value = joinRequests.value.filter(
        r => r.id !== requestId
      )
      // ElMessage.success('Đã duyệt yêu cầu tham gia') // Comment out for Windzo
      await fetchMembers() // user trở thành member
    }
    const rejectJoin = async (requestId) => {
      await tenantMembershipApi.rejectJoinRequest(
        activeTenantId,
        requestId
      )
      joinRequests.value = joinRequests.value.filter(
        r => r.id !== requestId
      )
      // ElMessage.success('Đã từ chối yêu cầu') // Comment out for Windzo
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
