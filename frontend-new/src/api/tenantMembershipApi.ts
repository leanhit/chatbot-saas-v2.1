import axios from '@/plugins/axios';
import type { 
  MemberResponse, 
  TenantPendingResponse, 
  InviteMemberRequest, 
  InvitationResponse 
} from '@/types/tenant';

export const tenantMembershipApi = {
  // ========================
  // INVITATIONS (API Mời)
  // ========================

  /** Admin gửi lời mời cho User thông qua Email */
  async inviteMember(tenantId: number | string, data: InviteMemberRequest): Promise<void> {
    return axios.post(`/tenants/${tenantId}/invitations`, data);
  },

  /** Lấy danh sách lời mời của một Tenant (Admin view) */
  async getTenantInvitations(tenantId: number | string): Promise<{ data: InvitationResponse[] }> {
    return axios.get(`/tenants/${tenantId}/invitations`);
  },

  /** User chấp nhận lời mời tham gia Tenant */
  async acceptInvitation(tenantId: number | string, token: string): Promise<void> {
    return axios.post(`/tenants/${tenantId}/invitations/${token}/accept`);
  },

  /** User từ chối lời mời tham gia Tenant */
  async rejectInvitation(tenantId: number | string, token: string): Promise<void> {
    return axios.post(`/tenants/${tenantId}/invitations/${token}/reject`);
  },

  /** Admin thu hồi lời mời đã gửi */
  async revokeInvitation(tenantId: number | string, invitationId: string): Promise<void> {
    return axios.delete(`/tenants/${tenantId}/invitations/${invitationId}`);
  },

  // ========================
  // USER INVITATIONS (Current user's invitations)
  // ========================

  /** Get all pending invitations for current user */
  async getUserInvitations(): Promise<{ data: Array<{
    id: number;
    tenantId: number;
    tenantName: string;
    role: string;
    status: string;
    expiresAt: string;
    invitedByName: string;
  }> }> {
    return axios.get('/tenants/members/my-invitations');
  },

  // ========================
  // MEMBER MANAGEMENT (Admin/Owner)
  // ========================

  async getTenantUsers(tenantId: string): Promise<{ data: MemberResponse[] }> {
    return axios.get(`/tenants/${tenantId}/members`);
  },

  async updateUserRole(tenantId: string, userId: string, role: string): Promise<void> {
    return axios.put(`/tenants/${tenantId}/members/${userId}/role`, { role });
  },

  async removeUserFromTenant(tenantId: string, userId: string): Promise<void> {
    return axios.delete(`/tenants/${tenantId}/members/${userId}`);
  },

  // ========================
  // JOIN REQUESTS (Yêu cầu xin gia nhập)
  // ========================

  async getMyPending(): Promise<{ data: TenantPendingResponse[] }> {
    return axios.get('/tenants/members/pending-tenants');
  },

  async requestJoinTenant(tenantId: string): Promise<void> {
    return axios.post(`/tenants/${tenantId}/members/join-requests`);
  },

  async getTenantJoinRequests(tenantId: string): Promise<{ data: any[] }> {
    return axios.get(`/tenants/${tenantId}/members/join-requests`);
  },

  async approveJoinRequest(tenantId: string, requestId: string): Promise<void> {
    return axios.patch(`/tenants/${tenantId}/members/join-requests/${requestId}`, {
      status: 'APPROVED'
    });
  },

  async rejectJoinRequest(tenantId: string, requestId: string): Promise<void> {
    return axios.patch(`/tenants/${tenantId}/members/join-requests/${requestId}`, {
      status: 'REJECTED'
    });
  }
};