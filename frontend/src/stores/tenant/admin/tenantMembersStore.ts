import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { tenantMembershipApi } from '@/api/tenantMembershipApi'
import type {
  MemberResponse,
  TenantRole,
  InvitationResponse,
  InviteMemberRequest,
  TenantPendingResponse
} from '@/types/tenant'
import { ACTIVE_TENANT_ID } from '@/utils/constant'

export const useTenantAdminMembersStore = defineStore(
  'tenantAdminMembers',
  () => {
    // ======================
    // STATE
    // ======================
    const members = ref<MemberResponse[]>([])
    const invitations = ref<InvitationResponse[]>([])
    const joinRequests = ref<TenantPendingResponse[]>([])
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
          : (res.data as any)?.content || []
      } finally {
        loading.value = false
      }
    }

    const updateRole = async (userId: number, role: TenantRole) => {
      await tenantMembershipApi.updateUserRole(activeTenantId, userId, role)
      const user = members.value.find(m => m.userId === userId)
      if (user) user.role = role
      ElMessage.success('Đã cập nhật vai trò')
    }

    const removeMember = async (userId: number) => {
      await tenantMembershipApi.removeUserFromTenant(activeTenantId, userId)
      members.value = members.value.filter(m => m.userId !== userId)
      ElMessage.success('Đã xóa thành viên')
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
        console.error('Failed to fetch invitations', error)
      }
    }

    const inviteUser = async (payload: InviteMemberRequest) => {
      try {
        await tenantMembershipApi.inviteMember(activeTenantId, payload)
        ElMessage.success('Đã gửi lời mời')
        await fetchInvitations()
      } catch (error: any) {
        ElMessage.error(
          error.response?.data?.message || 'Không thể gửi lời mời'
        )
      }
    }

    const revokeInvitationAction = async (invitationId: number) => {
      try {
        await tenantMembershipApi.revokeInvitation(
          activeTenantId,
          invitationId
        )
        invitations.value = invitations.value.filter(
          i => i.id !== invitationId
        )
        ElMessage.success('Đã thu hồi lời mời')
      } catch {
        ElMessage.error('Không thể thu hồi lời mời')
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

    const approveJoin = async (requestId: string) => {
      await tenantMembershipApi.approveJoinRequest(
        activeTenantId,
        requestId
      )
      joinRequests.value = joinRequests.value.filter(
        r => r.id !== requestId
      )
      ElMessage.success('Đã duyệt yêu cầu tham gia')
      await fetchMembers() // user trở thành member
    }

    const rejectJoin = async (requestId: string) => {
      await tenantMembershipApi.rejectJoinRequest(
        activeTenantId,
        requestId
      )
      joinRequests.value = joinRequests.value.filter(
        r => r.id !== requestId
      )
      ElMessage.success('Đã từ chối yêu cầu')
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
