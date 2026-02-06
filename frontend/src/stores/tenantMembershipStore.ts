import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { tenantMembershipApi } from '@/api/tenantMembershipApi';
import type {
  MemberResponse,
  TenantPendingResponse,
  TenantRole
} from '@/types/tenant';

export const useTenantMembershipStore = defineStore('tenantMembership', () => {
  // State
  const tenantUsers = ref<MemberResponse[]>([]);
  const pendingTenants = ref<TenantPendingResponse[]>([]);
  const loading = ref(false);

  // Getters
  const activeMembers = computed(() =>
    tenantUsers.value.filter(u => u.status === 'ACTIVE')
  );

  const pendingRequests = computed(() =>
    tenantUsers.value.filter(u => u.status === 'PENDING')
  );

  // Actions: Member Management
  const fetchTenantUsers = async (tenantId: string) => {
    loading.value = true;
    try {
      const { data } = await tenantMembershipApi.getTenantUsers(tenantId);
      tenantUsers.value = data;
    } finally {
      loading.value = false;
    }
  };

  const updateUserRole = async (tenantId: string, userId: number, role: TenantRole) => {
    try {
      await tenantMembershipApi.updateUserRole(tenantId, userId, role);
      // Cập nhật ngay lập tức trong mảng local để UI phản hồi nhanh
      const user = tenantUsers.value.find(u => u.userId === userId);
      if (user) user.role = role;
      ElMessage.success('Cập nhật vai trò thành công');
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || 'Lỗi cập nhật');
    }
  };

  const removeUserFromTenant = async (tenantId: string, userId: number) => {
    try {
      await tenantMembershipApi.removeUserFromTenant(tenantId, userId);
      tenantUsers.value = tenantUsers.value.filter(u => u.userId !== userId);
      ElMessage.success('Đã xóa thành viên');
    } catch (error: any) {
      ElMessage.error('Không thể xóa thành viên');
    }
  };

  // Actions: Join Requests (Admin duyệt)
  const processJoinRequest = async (tenantId: string, requestId: string, status: 'ACTIVE' | 'REJECTED') => {
    loading.value = true;
    try {
      if (status === 'ACTIVE') {
        await tenantMembershipApi.approveJoinRequest(tenantId, requestId);
        ElMessage.success('Đã phê duyệt tham gia');
      } else {
        await tenantMembershipApi.rejectJoinRequest(tenantId, requestId);
        ElMessage.success('Đã từ chối yêu cầu');
      }
      // Load lại danh sách để đồng bộ trạng thái
      await fetchTenantUsers(tenantId);
    } finally {
      loading.value = false;
    }
  };

  // Actions: Self-Service (User cá nhân)
  const fetchMyPendingTenants = async () => {
    loading.value = true;
    try {
      const { data } = await tenantMembershipApi.getMyPending();
      pendingTenants.value = data;
    } finally {
      loading.value = false;
    }
  };

  const requestJoinTenant = async (tenantId: string) => {
    try {
      await tenantMembershipApi.requestJoinTenant(tenantId);
      ElMessage.success('Đã gửi yêu cầu tham gia, vui lòng chờ duyệt');
      await fetchMyPendingTenants();
    } catch (error: any) {
      ElMessage.warning('Bạn đã gửi yêu cầu cho tổ chức này rồi');
    }
  };

  return {
    tenantUsers,
    pendingTenants,
    activeMembers,
    pendingRequests,
    loading,
    fetchTenantUsers,
    updateUserRole,
    removeUserFromTenant,
    processJoinRequest,
    fetchMyPendingTenants,
    requestJoinTenant
  };
});