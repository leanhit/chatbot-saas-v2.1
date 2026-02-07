import axios from '@/plugins/axios';
import router from '@/router';

export const tenantApi = {
  // Tenant core
  async getTenant(tenantId) {
    try {
      const response = await axios.get(`/tenants/${tenantId}`);
      return response;
    } catch (error) {
      handleTenantError(error);
      throw error;
    }
  },

  async switchTenant(tenantId) {
    try {
      const response = await axios.post(`/tenants/${tenantId}/switch`);
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
      console.log('[TenantAPI] Creating tenant with data:', data);
      console.log('[TenantAPI] Active tenant key from localStorage:', localStorage.getItem('ACTIVE_TENANT_ID'));
      
      const response = await axios.post('/tenants', data);
      
      console.log('[TenantAPI] Create tenant success:', response.data);
      // ElMessage.success('Tạo workspace mới thành công'); // Comment out for Windzo
      return response;
    } catch (error) {
      console.error('[TenantAPI] Create tenant error:', error);
      handleTenantError(error);
      throw error;
    }
  },

  async deleteTenant(tenantId) {
    return axios.delete(`/tenants/${tenantId}`);
  },

  async searchTenant(keyword) {
    return axios.get('/tenants/search', { params: { keyword } });
  },

  /**
   * Lấy thông tin chi tiết tenant bao gồm profile và địa chỉ
   * @param tenantId UUID của tenant cần lấy thông tin
   * @returns Thông tin chi tiết của tenant
   */
  async getTenantDetail(tenantId) {
    return axios.get(`/tenants/${tenantId}`);
  },

  /**
   * Lấy thông tin chi tiết tenant bằng tenantId (cho frontend)
   * @param tenantId UUID của tenant cần lấy thông tin
   * @returns Thông tin chi tiết của tenant
   */
  async getTenantDetailByTenantKey(tenantId) {
    return axios.get(`/tenants/${tenantId}`);
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
  async getTenantProfile(tenantId) {
    return axios.get(`/v1/tenant/profile/${tenantId}`);
  },

  async updateTenantProfile(tenantId, data) {
    return axios.put(`/v1/tenant/profile/${tenantId}`, data);
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
  async updateTenantBasicInfo(tenantId, data) {
    return axios.put(`/tenants/${tenantId}`, data);
  },

  // Professional Info - Similar to user professional info  
  async updateTenantProfessionalInfo(tenantId, data) {
    // This would go to TenantProfessionalController if it exists
    return axios.put(`/v1/tenant/professional/${tenantId}`, data);
  },

  // Legacy method for backward compatibility
  async updateTenantProfile_old(tenantId, data) {
    return axios.put(`/tenant-profile/${tenantId}`, data);
  },

  // Address
  async updateTenantAddress(tenantId, addressId, data) {
    console.log('Updating tenant address:', tenantId, addressId, data);
    return axios.put(`/addresses/${addressId}`, {
      ...data,
      ownerType: 'TENANT',
      ownerId: tenantId // Use tenantId instead of tenantKey
    }, {
      headers: {
        'X-Tenant-Key': tenantId  // Thêm header X-Tenant-Key
      }
    });
  },

  /**
   * Lấy danh sách yêu cầu đang chờ (Admin)
   */
  async getJoinRequests(tenantId) {
    return axios.get(`/tenants/${tenantId}/members/join-requests`);
  },

  /**
   * Phê duyệt hoặc Từ chối yêu cầu
   * @param status 'APPROVED' | 'REJECTED'
   */
  async updateJoinRequestStatus(tenantId, requestId, status) {
    return axios.patch(`/tenants/${tenantId}/members/join-requests/${requestId}`, {
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
  },

  // Members management
  async getTenantMembers(tenantId, params = {}) {
    return axios.get(`/tenants/${tenantId}/members`, { params });
  },

  async removeTenantMember(tenantId, memberId) {
    return axios.delete(`/tenants/${tenantId}/members/${memberId}`);
  },

  async updateTenantMemberRole(tenantId, memberId, role) {
    return axios.put(`/tenants/${tenantId}/members/${memberId}/role`, role);
  },

  async inviteTenantMember(tenantId, inviteData) {
    return axios.post(`/tenants/${tenantId}/invitations`, inviteData);
  },

  // Join Requests (Admin view)
  async getTenantJoinRequests(tenantId) {
    return axios.get(`/tenants/${tenantId}/members/join-requests`);
  },

  async approveJoinRequest(tenantId, requestId) {
    return axios.patch(`/tenants/${tenantId}/members/join-requests/${requestId}`, {
      status: 'APPROVED'
    });
  },

  async rejectJoinRequest(tenantId, requestId) {
    return axios.patch(`/tenants/${tenantId}/members/join-requests/${requestId}`, {
      status: 'REJECTED'
    });
  },

  // Invitations (Admin view) - Note: Backend controller disabled in v0.1
  async getTenantInvitations(tenantId) {
    return axios.get(`/tenants/${tenantId}/invitations`);
  },

  async revokeInvitation(tenantId, invitationId) {
    return axios.delete(`/tenants/${tenantId}/invitations/${invitationId}`);
  }
};

// Xử lý lỗi chung cho các API liên quan đến tenant
function handleTenantError(error) {
  console.error('Tenant API Error:', error);
  
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
      console.error('API Error:', data.message);
    } else {
      // ElMessage.error('Đã có lỗi xảy ra. Vui lòng thử lại sau.'); // Comment out for Windzo
      console.error('Unknown API error');
    }
  } else if (error.request) {
    // Lỗi không nhận được phản hồi từ server
    // ElMessage.error('Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng của bạn.'); // Comment out for Windzo
    console.error('Network error');
  } else {
    // Lỗi khi thiết lập request
    // ElMessage.error('Đã có lỗi xảy ra khi gửi yêu cầu.'); // Comment out for Windzo
    console.error('Request setup error');
  }
}
