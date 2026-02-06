import axios from '@/plugins/axios';
import { ElMessage } from 'element-plus';
import router from '@/router';
import type {
  TenantResponse,
  CreateTenantRequest,
  TenantDetailResponse,
  TenantProfileRequest,
  AddressDetailResponseDTO,
  TenantBasicInfoRequest
} from '@/types/tenant';

export const tenantApi = {
  // Tenant core
  async getTenant(tenantId: string): Promise<{ data: TenantResponse }> {
    try {
      const response = await axios.get(`/tenants/${tenantId}`);
      return response;
    } catch (error: any) {
      handleTenantError(error);
      throw error;
    }
  },

  async switchTenant(tenantId: string): Promise<{ data: TenantResponse }> {
    try {
      const response = await axios.post(`/tenants/${tenantId}/switch`);
      return response;
    } catch (error: any) {
      handleTenantError(error);
      throw error;
    }
  },

  async getUserTenants(): Promise<{ data: TenantResponse[] }> {
    return axios.get('/tenants/me');
  },

  async createTenant(data: CreateTenantRequest): Promise<{ data: TenantResponse }> {
    try {
      const response = await axios.post('/tenants', data, {
        headers: {
          'X-Tenant-ID': '' // Clear tenant context for tenant creation
        }
      });
      ElMessage.success('Tạo workspace mới thành công');
      return response;
    } catch (error: any) {
      handleTenantError(error);
      throw error;
    }
  },

  async deleteTenant(tenantId: string): Promise<void> {
    return axios.delete(`/tenants/${tenantId}`);
  },

  async searchTenant(keyword: string): Promise<{ data: TenantResponse[] }> {
    return axios.get('/tenants/search', { params: { keyword } });
  },

  /**
   * Lấy thông tin chi tiết tenant bao gồm profile và địa chỉ
   * @param tenantId ID của tenant cần lấy thông tin
   * @returns Thông tin chi tiết của tenant
   */
  async getTenantDetail(tenantId: string): Promise<{ data: TenantDetailResponse }> {
    return axios.get(`/tenants/${tenantId}/full`);
  },

  async suspendTenant(id: number | string): Promise<void> {
    try {
      await axios.post(`/tenants/${id}/suspend`);
      ElMessage.success('Đã tạm dừng workspace thành công');
    } catch (error: any) {
      handleTenantError(error);
      throw error;
    }
  },

  async activateTenant(id: number | string): Promise<void> {
    try {
      await axios.post(`/tenants/${id}/activate`);
      ElMessage.success('Đã kích hoạt lại workspace thành công');
    } catch (error: any) {
      handleTenantError(error);
      throw error;
    }
  },

  // Profile
  async updateTenantProfile(tenantId: string, data: TenantProfileRequest): Promise<{ data: any }> {
    return axios.put(`/tenant-profile/${tenantId}`, data);
  },

  // Address
  async updateTenantAddress(tenantId: string, addressId: string, data: AddressDetailResponseDTO): Promise<{ data: any }> {
    console.log('Updating tenant address:', tenantId, addressId, data);
    return axios.put(`/addresses/${addressId}`, {
      ...data,
      tenantId: parseInt(tenantId, 10),
      ownerType: 'TENANT',
      ownerId: parseInt(tenantId, 10)
    }, {
      headers: {
        'X-Tenant-Id': tenantId  // Thêm header X-Tenant-Id
      }
    });
  },

  // Basic Info
  async updateTenantBasicInfo(tenantId: string, data: TenantBasicInfoRequest): Promise<{ data: Tenant }> {
    return axios.put(`/tenants/${tenantId}`, data);
  },

  /**
   * Lấy danh sách yêu cầu đang chờ (Admin)
   */
  async getJoinRequests(tenantId: string): Promise<{ data: any }> {
    return axios.get(`/tenants/${tenantId}/members/join-requests`);
  },

  /**
   * Phê duyệt hoặc Từ chối yêu cầu
   * @param status 'APPROVED' | 'REJECTED'
   */
  async updateJoinRequestStatus(tenantId: string, requestId: number, status: string): Promise<void> {
    return axios.patch(`/tenants/${tenantId}/members/join-requests/${requestId}`, {
      status: status
    });
  }
};

// Xử lý lỗi chung cho các API liên quan đến tenant
function handleTenantError(error: any) {
  console.error('Tenant API Error:', error);
  
  if (error.response) {
    const { status, data } = error.response;
    
    if (status === 500 && data?.message?.includes('SUSPENDED')) {
      ElMessage.error('Workspace này đã bị tạm dừng. Vui lòng liên hệ quản trị viên.');
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
      ElMessage.error(data.message);
    } else {
      ElMessage.error('Đã có lỗi xảy ra. Vui lòng thử lại sau.');
    }
  } else if (error.request) {
    // Lỗi không nhận được phản hồi từ server
    ElMessage.error('Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng của bạn.');
  } else {
    // Lỗi khi thiết lập request
    ElMessage.error('Đã có lỗi xảy ra khi gửi yêu cầu.');
  }
}
