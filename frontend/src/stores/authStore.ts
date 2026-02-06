import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import axios from '@/plugins/axios';
import router from '@/router';
import { useTenantStore } from './tenantStore';
import { extractUserIdFromToken } from '@/utils/tokenUtils';
import type { UserDto, UserResponse } from '@/types/auth';

// Identity Hub Login Response type
interface IdentityLoginResponse {
    accessToken: string;
    refreshToken: string;
    tokenType: string;
    expiresIn: number;
}

export const useAuthStore = defineStore('auth', () => {
  // --- 1. State ---
  const token = ref<string | null>(localStorage.getItem('accessToken'));
  const refreshToken = ref<string | null>(localStorage.getItem('refreshToken'));
  const user = ref<UserDto | null>(null);

  // --- 2. Getters ---
  const isLoggedIn = computed(() => !!token.value);
  const isAdmin = computed(() => user.value?.systemRole === 'ADMIN');
  const userId = computed(() => user.value?.id || extractUserIdFromToken(token.value || ''));

  // --- 3. Actions ---

  /**
   * Khởi tạo trạng thái Auth (Được gọi từ main.ts)
   * Đọc token và user từ localStorage để khôi phục phiên làm việc
   */
  const initialize = () => {
    const savedToken = localStorage.getItem('accessToken');
    const savedRefreshToken = localStorage.getItem('refreshToken');
    const savedUser = localStorage.getItem('user');

    if (savedToken) {
      token.value = savedToken;
    }

    if (savedRefreshToken) {
      refreshToken.value = savedRefreshToken;
    }

    if (savedUser) {
      try {
        user.value = JSON.parse(savedUser);
      } catch (e) {
        console.error("Không thể phân giải thông tin user từ storage", e);
        localStorage.removeItem('user');
      }
    }
  };

  /**
   * Xử lý đăng nhập thành công - supports both old and new formats
   */
  const login = async (authData: UserResponse | IdentityLoginResponse) => {
    // Check if it's Identity Hub response (has accessToken)
    if ('accessToken' in authData) {
      // Identity Hub format
      token.value = authData.accessToken;
      refreshToken.value = authData.refreshToken;
      
      localStorage.setItem('accessToken', authData.accessToken);
      localStorage.setItem('refreshToken', authData.refreshToken);
      
      // Extract user info from token (no user object in response)
      const userId = extractUserIdFromToken(authData.accessToken);
      if (userId) {
        user.value = {
          id: userId,
          email: '', // Will be populated later if needed
          systemRole: 'USER' // Default, will be updated if needed
        };
      }
    } else {
      // Legacy format
      token.value = authData.token;
      user.value = authData.user;

      localStorage.setItem('accessToken', authData.token);
      localStorage.setItem('user', JSON.stringify(authData.user));
    }
  };

  /**
   * Đăng xuất và dọn dẹp dữ liệu
   */
  const logout = () => {
    const tenantStore = useTenantStore();
    
    // Xóa sạch context của Tenant và Auth
    tenantStore.clearTenant();
    token.value = null;
    refreshToken.value = null;
    user.value = null;

    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    localStorage.removeItem('currentTenantId'); // Xóa cả tenant cũ nếu có

    router.push({ name: 'login' });
  };

  /**
   * Lấy lại thông tin user profile từ Backend
   */
  const fetchUser = async () => {
    if (!token.value) return;

    try {
      const { data } = await axios.get<UserDto>('/api/auth/me');
      user.value = data;
      localStorage.setItem('user', JSON.stringify(data));
    } catch (error) {
      console.error('Lỗi khi lấy thông tin user:', error);
      logout(); 
    }
  };

  /**
   * Cập nhật thông tin user cục bộ (ví dụ đổi avatar, đổi tên)
   */
  const updateAuthUser = (updates: Partial<UserDto>) => {
    if (!user.value) return;
    user.value = { ...user.value, ...updates };
    localStorage.setItem('user', JSON.stringify(user.value));
  };

  // --- 4. Return ---
  // QUAN TRỌNG: Tất cả các hàm/biến muốn sử dụng ở ngoài (như main.ts) PHẢI nằm ở đây
  return {
    // State & Getters
    token,
    refreshToken,
    user,
    isLoggedIn,
    isAdmin,
    userId,
    
    // Actions
    initialize,
    login,
    logout,
    fetchUser,
    updateAuthUser
  };
});