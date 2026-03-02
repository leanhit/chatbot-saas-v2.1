import axios from '@/plugins/axios';
import router from '@/router';
export const tenantApi = {
  // Tenant core
  async getTenant(tenantKey) {
    try {
      const response = await axios.get(`/tenants/key/${tenantKey}`);
      return response;
    } catch (error) {
      handleTenantError(error);
      throw error;
    }
  },
  async switchTenant(tenantKey) {
    try {
      const response = await axios.post(`/tenants/key/${tenantKey}/switch`);
      return response;
    } catch (error) {
      handleTenantError(error);
      throw error;
    }
  },
  async getUserTenants() {
    return axios.get('/tenants/me');
  },
  async createTenant(data) {
    try {
      const response = await axios.post('/tenants', data);
      // ElMessage.success('Tạo workspace mới thành công'); // Comment out for Windzo
      return response;
    } catch (error) {
      handleTenantError(error);
      throw error;
    }
  },
  async deleteTenant(tenantKey) {
    return axios.delete(`/tenants/key/${tenantKey}`);
  },
  async searchTenant(keyword) {
    return axios.get('/tenants/search', { params: { keyword } });
  },
  /**
   * Lấy thông tin chi tiết tenant bao gồm profile và địa chỉ
   * @param tenantKey UUID của tenant cần lấy thông tin
   * @returns Thông tin chi tiết của tenant
   */
  async getTenantDetail(tenantKey) {
    return axios.get(`/tenants/key/${tenantKey}/full`);
  },
  /**
   * Lấy thông tin chi tiết tenant bằng tenantKey (cho frontend)
   * @param tenantKey UUID của tenant cần lấy thông tin
   * @returns Thông tin chi tiết của tenant
   */
  async getTenantDetailByTenantKey(tenantKey) {
    return axios.get(`/tenants/key/${tenantKey}/full`);
  },
  async suspendTenant(id) {
    try {
      await axios.post(`/tenants/${id}/suspend`);
      // ElMessage.success('Đã tạm dừng workspace thành công'); // Comment out for Windzo
    } catch (error) {
      handleTenantError(error);
      throw error;
    }
  },
  async activateTenant(id) {
    try {
      await axios.post(`/tenants/${id}/activate`);
      // ElMessage.success('Đã kích hoạt lại workspace thành công'); // Comment out for Windzo
    } catch (error) {
      handleTenantError(error);
      throw error;
    }
  },
  // Profile - Similar to user profile endpoints
  async getTenantProfile(tenantKey) {
    return axios.get(`/v1/tenant/profile/${tenantKey}`);
  },
  async updateTenantProfile(tenantKey, data) {
    return axios.put(`/v1/tenant/profile/${tenantKey}`, data);
  },
  async updateTenantLogo(file) {
    const formData = new FormData();
    formData.append('logo', file);
    return axios.put('/v1/tenant/logo', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
  },
  // Basic Info - Similar to user basic info
  async updateTenantBasicInfo(tenantKey, data) {
    return axios.put(`/tenants/key/${tenantKey}`, data);
  },
  // Professional Info - Similar to user professional info  
  async updateTenantProfessionalInfo(tenantKey, data) {
    // This would go to TenantProfessionalController if it exists
    return axios.put(`/v1/tenant/professional/${tenantKey}`, data);
  },
  // Legacy method for backward compatibility
  async updateTenantProfile_old(tenantKey, data) {
    return axios.put(`/tenant-profile/${tenantKey}`, data);
  },
  // Address
  async updateTenantAddress(tenantKey, addressId, data) {
    return axios.put(`/addresses/${addressId}`, {
      ...data,
      ownerType: 'TENANT',
      ownerId: tenantKey // Use tenantKey instead of tenantId
    }, {
      headers: {
        'X-Tenant-Key': tenantKey  // Thêm header X-Tenant-Key
      }
    });
  },
  // Status Management
  async suspendTenant(tenantKey) {
    return axios.post(`/tenants/key/${tenantKey}/suspend`);
  },
  async activateTenant(tenantKey) {
    return axios.post(`/tenants/key/${tenantKey}/activate`);
  },
  async deactivateTenant(tenantKey) {
    return axios.post(`/tenants/key/${tenantKey}/deactivate`);
  },
  /**
   * Lấy danh sách yêu cầu đang chờ (Admin)
   */
  async getJoinRequests(tenantKey) {
    return axios.get(`/tenants/key/${tenantKey}/members/join-requests`);
  },
  /**
   * Phê duyệt hoặc Từ chối yêu cầu
   * @param status 'APPROVED' | 'REJECTED'
   */
  async updateJoinRequestStatus(tenantKey, requestId, status) {
    return axios.patch(`/tenants/key/${tenantKey}/members/join-requests/${requestId}`, {
      status: status
    });
  },
  /**
   * Lấy danh sách yêu cầu đang chờ (cho user)
   */
  async getPendingRequests() {
    return axios.get('/tenants/pending-requests');
  },
  /**
   * Phê duyệt yêu cầu tham gia
   */
  async approvePendingRequest(requestId) {
    return axios.post(`/tenants/pending-requests/${requestId}/approve`);
  },
  /**
   * Từ chối yêu cầu tham gia
   */
  async rejectPendingRequest(requestId) {
    return axios.post(`/tenants/pending-requests/${requestId}/reject`);
  },
  /**
   * Lấy danh sách lời mời của user
   */
  async getUserInvitations() {
    return axios.get('/tenants/invitations/me');
  },
  /**
   * Chấp nhận lời mời
   */
  async acceptInvitation(invitationId) {
    return axios.post(`/tenants/invitations/${invitationId}/accept`);
  },
  /**
   * Từ chối lời mời
   */
  async rejectInvitation(invitationId) {
    return axios.post(`/tenants/invitations/${invitationId}/reject`);
  }
};
// Xử lý lỗi chung cho các API liên quan đến tenant
function handleTenantError(error) {
  if (error.response) {
    const { status, data } = error.response;
    if (status === 500 && data?.message?.includes('SUSPENDED')) {
      // ElMessage.error('Workspace này đã bị tạm dừng. Vui lòng liên hệ quản trị viên.'); // Comment out for Windzo
      // Xóa thông tin tenant đang lưu trong localStorage
      localStorage.removeItem('TENANT_DATA');
      localStorage.removeItem('ACTIVE_TENANT_ID');
      // Chuyển hướng về trang đăng nhập
      router.push('/login');
      return;
    }
    if (status === 401) {
      // Xử lý lỗi xác thực
      localStorage.clear();
      router.push('/login');
      return;
    }
    // Hiển thị thông báo lỗi từ server nếu có
    if (data?.message) {
      // ElMessage.error(data.message); // Comment out for Windzo
    } else {
      // ElMessage.error('Đã có lỗi xảy ra. Vui lòng thử lại sau.'); // Comment out for Windzo
    }
  } else if (error.request) {
    // Lỗi không nhận được phản hồi từ server
    // ElMessage.error('Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng của bạn.'); // Comment out for Windzo
  } else {
    // Lỗi khi thiết lập request
    // ElMessage.error('Đã có lỗi xảy ra khi gửi yêu cầu.'); // Comment out for Windzo
  }
}
