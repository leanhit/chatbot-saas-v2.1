import axios from '@/plugins/axios';
import router from '@/router';
import {
  CreateTenantRequest,
  TenantBasicInfoRequest,
  TenantSearchRequest,
  TenantProfileRequest,
  InviteMemberRequest,
  validateCreateTenantRequest,
  validateInviteMemberRequest,
  validateTenantBasicInfoRequest,
  validateTenantProfileRequest,
  validateTenantSearchRequest
} from '@/types/tenant';

export const tenantApi = {
  // Tenant core
  async getTenant(tenantKey) {
    try {
      const response = await axios.get(`/tenants/key/${tenantKey}/full`);
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
    // Validate request using types
    const errors = validateCreateTenantRequest(data);
    if (errors.length > 0) {
      throw new Error(errors.join(', '));
    }
    
    try {
      const response = await axios.post('/tenants', data);
      return response;
    } catch (error) {
      handleTenantError(error);
      throw error;
    }
  },
  async deleteTenant(tenantKey) {
    try {
      const response = await axios.delete(`/tenants/key/${tenantKey}`);
      return response;
    } catch (error) {
      handleTenantError(error);
      throw error;
    }
  },
  async updateTenant(tenantKey, data) {
    // Validate basic info request
    const errors = validateTenantBasicInfoRequest(data);
    if (errors.length > 0) {
      throw new Error(errors.join(', '));
    }
    
    try {
      const response = await axios.put(`/tenants/key/${tenantKey}`, data);
      return response;
    } catch (error) {
      handleTenantError(error);
      throw error;
    }
  },
  async updateTenantProfile(tenantKey, profileData) {
    // Validate profile request
    const errors = validateTenantProfileRequest(profileData);
    if (errors.length > 0) {
      throw new Error(errors.join(', '));
    }
    
    try {
      const response = await axios.put(`/tenant/${tenantKey}/profile`, profileData);
      return response;
    } catch (error) {
      handleTenantError(error);
      throw error;
    }
  },
  async uploadTenantLogo(tenantKey, file) {
    // Create FormData with correct parameter name for backend
    const formData = new FormData()
    formData.append('logo', file)
    
    try {
      const response = await axios.put(`/tenant/${tenantKey}/logo`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      return response;
    } catch (error) {
      handleTenantError(error);
      throw error;
    }
  },
  async uploadTenantFavicon(tenantKey, formData) {
    try {
      const response = await axios.post(`/tenants/key/${tenantKey}/favicon`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      return response;
    } catch (error) {
      handleTenantError(error);
      throw error;
    }
  },
  async searchTenants(searchRequest) {
    // Validate search request
    const errors = validateTenantSearchRequest(searchRequest);
    if (errors.length > 0) {
      throw new Error(errors.join(', '));
    }
    
    try {
      const params = new URLSearchParams();
      if (searchRequest.keyword) params.append('keyword', searchRequest.keyword);
      params.append('page', searchRequest.page);
      params.append('size', searchRequest.size);
      params.append('sortBy', searchRequest.sortBy);
      params.append('sortDirection', searchRequest.sortDirection);
      
      const response = await axios.get(`/tenants/search?${params.toString()}`);
      return response;
    } catch (error) {
      handleTenantError(error);
      throw error;
    }
  },
  async getTenantStats(tenantKey) {
    try {
      const response = await axios.get(`/tenants/key/${tenantKey}/stats`);
      return response;
    } catch (error) {
      handleTenantError(error);
      throw error;
    }
  },
  // Tenant membership
  async getTenantMembers(tenantKey) {
    return axios.get(`/tenants/key/${tenantKey}/members`);
  },
  async updateMemberRole(tenantKey, memberId, role) {
    return axios.put(`/tenants/key/${tenantKey}/members/${memberId}/role`, { role });
  },
  async removeMember(tenantKey, memberId) {
    return axios.delete(`/tenants/key/${tenantKey}/members/${memberId}`);
  },
  async inviteMember(tenantKey, inviteData) {
    return axios.post(`/tenants/key/${tenantKey}/invitations`, inviteData);
  },
  async getTenantInvitations(tenantKey) {
    return axios.get(`/tenants/key/${tenantKey}/invitations`);
  },
  async revokeInvitation(tenantKey, invitationId) {
    return axios.delete(`/tenants/key/${tenantKey}/invitations/${invitationId}`);
  },
  async resendInvitation(tenantKey, invitationId) {
    return axios.post(`/tenants/key/${tenantKey}/invitations/${invitationId}/resend`);
  },
  async requestJoinTenant(tenantKey) {
    const url = `/tenants/key/${tenantKey}/members/join-requests`;
    console.log('requestJoinTenant URL:', url);
    console.log('Making POST request to:', url);
    const result = axios.post(url);
    console.log('Request result:', result);
    return result;
  },
  async getMyJoinRequests() {
    return axios.get('/tenants/members/join-requests');
  },
  async cancelJoinRequest(requestId) {
    return axios.delete(`/tenants/members/join-requests/${requestId}`);
  },
  async getMyInvitations() {
    return axios.get('/tenants/members/my-invitations');
  },
  async acceptInvitation(invitationId) {
    return axios.post(`/tenants/members/invitations/${invitationId}/accept`);
  },
  async rejectInvitation(invitationId) {
    return axios.post(`/tenants/members/invitations/${invitationId}/reject`);
  },
  async leaveTenant(tenantKey) {
    return axios.post(`/tenants/key/${tenantKey}/leave`);
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
   * Revoke invitation
   */
  async revokeInvitation(tenantKey, invitationId) {
    return axios.delete(`/tenants/key/${tenantKey}/invitations/${invitationId}`);
  },
  /**
   * Request to join a tenant
   */
  async requestJoinTenant(tenantKey) {
    const url = `/tenants/key/${tenantKey}/members/join-requests`;
    console.log('requestJoinTenant URL:', url);
    console.log('Making POST request to:', url);
    const result = axios.post(url);
    console.log('Request result:', result);
    return result;
  },
  /**
   * Get pending join requests for a tenant
   */
  async getPendingJoinRequests(tenantKey) {
    return axios.get(`/tenants/key/${tenantKey}/members/join-requests/pending`);
  },
  /**
   * Get pending invitations for a tenant
   */
  async getPendingInvitations(tenantKey) {
    return axios.get(`/tenants/key/${tenantKey}/invitations/pending`);
  },
  /**
   * Get member statistics
   */
  async getMemberStats(tenantKey) {
    return axios.get(`/tenants/key/${tenantKey}/members/stats`);
  },
  /**
   * Bulk update member roles
   */
  async bulkUpdateMemberRoles(tenantKey, updates) {
    return axios.put(`/tenants/key/${tenantKey}/members/bulk-update-roles`, updates);
  },
  /**
   * Export member list
   */
  async exportMembers(tenantKey, format = 'csv') {
    return axios.get(`/tenants/key/${tenantKey}/members/export?format=${format}`, {
      responseType: 'blob'
    });
  },
  /**
   * Import members from CSV
   */
  async importMembers(tenantKey, formData) {
    return axios.post(`/tenants/key/${tenantKey}/members/import`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },
  /**
   * Get member activity log
   */
  async getMemberActivityLog(tenantKey, memberId) {
    return axios.get(`/tenants/key/${tenantKey}/members/${memberId}/activity`);
  },
  /**
   * Update member profile
   */
  async updateMemberProfile(tenantKey, memberId, profileData) {
    return axios.put(`/tenants/key/${tenantKey}/members/${memberId}/profile`, profileData);
  },
  /**
   * Deactivate member
   */
  async deactivateMember(tenantKey, memberId) {
    return axios.patch(`/tenants/key/${tenantKey}/members/${memberId}/deactivate`);
  },
  /**
   * Reactivate member
   */
  async reactivateMember(tenantKey, memberId) {
    return axios.patch(`/tenants/key/${tenantKey}/members/${memberId}/reactivate`);
  }
};

// Error handler
function handleTenantError(error) {
  if (error.response?.status === 401) {
    // Token expired or invalid
    router.push('/login');
  } else if (error.response?.status === 403) {
    // Forbidden access
    console.error('Access forbidden:', error.response?.data?.message || 'Access denied');
  } else if (error.response?.status === 404) {
    // Resource not found
    console.error('Resource not found:', error.response?.data?.message || 'Resource not found');
  } else if (error.response?.status >= 500) {
    // Server error
    console.error('Server error:', error.response?.data?.message || 'Internal server error');
  } else {
    // Other errors
    console.error('API error:', error.message || 'Unknown error occurred');
  }
}

export default tenantApi;
