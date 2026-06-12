import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import router from '@/router'
import { usersApi } from '@/api/usersApi'
import { useGatewayTenantStore } from './tenant/gateway/myTenantStore'
import { tenantApi } from '@/api/tenantApi'
import axios from '@/plugins/axios'
// Import constants from tenant store (giống frontend)
const TENANT_DATA = 'tenant_data'
const ACTIVE_TENANT_ID = 'active_tenant_id'
// Immediate cleanup of "null" strings in localStorage before store initialization
const savedToken = localStorage.getItem('accessToken')
if (savedToken === 'null' || savedToken === null) {
  localStorage.removeItem('accessToken')
}
const savedRefreshToken = localStorage.getItem('refreshToken')
if (savedRefreshToken === 'null' || savedRefreshToken === null) {
  localStorage.removeItem('refreshToken')
}

export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref(null)
  const token = ref(null)
  const refreshToken = ref(null)
  const isLoading = ref(false)
  const error = ref(null)
  const isRefreshing = ref(false)
  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.systemRole === 'ADMIN')
  const isSystemAdmin = computed(() => user.value?.systemRole === 'SYSTEM_ADMIN')
  const isAnyAdmin = computed(() => isAdmin.value || isSystemAdmin.value)
  const userId = computed(() => user.value?.id)
  const currentUser = computed(() => user.value)
  // Actions
  /**
   * Khởi tạo trạng thái Auth (Được gọi từ main.ts)
   * Đọc token và user từ localStorage để khôi phục phiên làm việc
   */
  const initialize = () => {
    const savedToken = localStorage.getItem('accessToken')
    const savedRefreshToken = localStorage.getItem('refreshToken')
    const savedUser = localStorage.getItem('user')
    
    // Clean up invalid "null" string values
    if (savedToken === 'null' || savedToken === null || savedToken === '') {
      localStorage.removeItem('accessToken')
      token.value = null
    } else if (savedToken) {
      token.value = savedToken
    }
    
    if (savedRefreshToken === 'null' || savedRefreshToken === null || savedRefreshToken === '') {
      localStorage.removeItem('refreshToken')
      refreshToken.value = null
    } else if (savedRefreshToken) {
      refreshToken.value = savedRefreshToken
    }
    
    if (savedUser) {
      try {
        user.value = JSON.parse(savedUser)
      } catch (e) {
        localStorage.removeItem('user')
      }
    }
  }
  /**
   * Xử lý đăng nhập thành công
   */
  const login = async (authData) => {
    // Ensure token is not the string "null"
    const validToken = authData.token && authData.token !== 'null' ? authData.token : null
    const validRefreshToken = authData.refreshToken && authData.refreshToken !== 'null' ? authData.refreshToken : null
    
    token.value = validToken
    refreshToken.value = validRefreshToken
    user.value = authData.user
    
    if (validToken) {
      localStorage.setItem('accessToken', validToken)
    } else {
      localStorage.removeItem('accessToken')
    }
    
    if (validRefreshToken) {
      localStorage.setItem('refreshToken', validRefreshToken)
    } else {
      localStorage.removeItem('refreshToken')
    }
    
    localStorage.setItem('user', JSON.stringify(authData.user))
  }
  /**
   * Đăng nhập với credentials
   */
  const loginWithCredentials = async (credentials) => {
    isLoading.value = true
    error.value = null
    try {
      // 1. Gọi API Login
      const res = await usersApi.login(credentials)
      // API returns { data: { data: UserResponse } } so we need to access res.data.data
      const authData = res.data.data || res.data
      if (!authData.token) {
        throw new Error("No token received")
      }
      // 2. Lưu token và thông tin user vào Store & LocalStorage
      await login(authData)
      // 3. Lấy thông tin Tenant
      try {
        const tenantStore = useGatewayTenantStore()
        await tenantStore.fetchUserTenants()
      } catch (tenantErr) {
        // Có thể bỏ qua lỗi này hoặc xử lý riêng để không làm gián đoạn luồng login
      }
      // 4. Lấy thông tin User Profile - CHỈ SAU KHI CÓ TENANT
      // Skip profile fetch during login as it requires tenant context
      // Profile will be fetched when tenant is selected
      // 5. Determine redirect based on tenant data
      const tenantStore = useGatewayTenantStore()
      
      // Always try to get stored tenant first
      const storedTenantKey = localStorage.getItem('active_tenant_id')
      const storedTenantData = localStorage.getItem('tenant_data')
      // Re-enable hydrate for future logins
      localStorage.setItem('should_hydrate_tenant', 'true')
      
      if (storedTenantKey && tenantStore.currentTenant) {
        // Has stored active tenant, go to dashboard directly
        await router.push('/dashboard')
      } else if (tenantStore.userTenants.length === 1) {
        // Only one tenant, auto-switch and go to dashboard (same as Enter tenant)
        const onlyTenant = tenantStore.userTenants[0]
        await tenantStore.switchTenant(onlyTenant.tenantKey)
        await router.push('/dashboard')
      } else if (tenantStore.userTenants.length > 1) {
        // Multiple tenants, go to tenant gateway
        await router.push({ name: 'tenant-gateway' })
      } else {
        // No tenants, go to tenant gateway
        await router.push({ name: 'tenant-gateway' })
      }
      return { success: true, data: authData }
    } catch (err) {
      const message = err.response?.data?.message || err.message || 'Login failed'
      error.value = message
      return { success: false, error: message }
    } finally {
      isLoading.value = false
    }
  }
  /**
   * Đăng ký tài khoản mới
   */
  const register = async (userData) => {
    isLoading.value = true
    error.value = null
    try {
      const response = await usersApi.register(userData)
      const authData = response.data.data || response.data
      if (!authData.token) {
        throw new Error("No token received")
      }
      
      // Login with received token
      await login(authData)
      
      // Auto-create tenant after successful registration
      const tenantStore = useGatewayTenantStore();
      
      // Extract tenant name from email (e.g., user@example.com -> "user")
      const email = userData.email;
      const tenantName = email.split('@')[0];
      
      console.log('🚀 Auto-creating tenant for:', email, 'with name:', tenantName);
      
      try {
        // Create tenant with name based on email
        const createTenantResponse = await tenantApi.createTenant({
          name: tenantName,
          visibility: 'PUBLIC',
          description: `Workspace for ${email}`
        });
        
        console.log('✅ Create tenant response:', createTenantResponse);
        
        const newTenantKey = createTenantResponse.data?.data?.tenantKey || 
                             createTenantResponse.data?.tenantKey ||
                             createTenantResponse.data?.key;
        
        console.log('🔑 Extracted tenantKey:', newTenantKey);
        
        if (newTenantKey) {
          // Switch to the newly created tenant
          await tenantStore.switchTenant(newTenantKey);
          // Redirect to dashboard
          await router.push('/dashboard');
          console.log('✅ Successfully switched to tenant and redirected to dashboard');
        } else {
          console.error('❌ No tenantKey found in response');
          // Fallback to tenant gateway if tenant creation failed
          await tenantStore.fetchUserTenants();
          await router.push({ name: 'tenant-gateway' });
        }
      } catch (tenantErr) {
        console.error('❌ Auto-create tenant failed:', tenantErr);
        console.error('Error response:', tenantErr.response?.data);
        // Fallback: refresh tenant list and go to tenant gateway
        await tenantStore.fetchUserTenants();
        await router.push({ name: 'tenant-gateway' });
      }
      
      return { success: true, data: authData }
    } catch (err) {
      const message = err.response?.data?.message || err.message || 'Registration failed'
      error.value = message
      return { success: false, error: message }
    } finally {
      isLoading.value = false
    }
  }
  /**
   * Refresh access token
   */
  const refreshAccessToken = async () => {
    if (!refreshToken.value) {
      return null
    }

    try {
      const response = await usersApi.refreshToken({ refreshToken: refreshToken.value })
      const authData = response.data
      
      // Ensure tokens are not the string "null"
      const validAccessToken = authData.accessToken && authData.accessToken !== 'null' ? authData.accessToken : null
      const validRefreshToken = authData.refreshToken && authData.refreshToken !== 'null' ? authData.refreshToken : null
      
      // Update tokens
      token.value = validAccessToken
      refreshToken.value = validRefreshToken
      
      if (validAccessToken) {
        localStorage.setItem('accessToken', validAccessToken)
      } else {
        localStorage.removeItem('accessToken')
      }
      
      if (validRefreshToken) {
        localStorage.setItem('refreshToken', validRefreshToken)
      } else {
        localStorage.removeItem('refreshToken')
      }
      
      return validAccessToken
    } catch (error) {
      console.error('Refresh token failed:', error)
      // Refresh token expired or invalid - logout
      logout()
      return null
    }
  }

  /**
   * Đăng xuất và dọn dẹp dữ liệu
   */
  const logout = async () => {
    try {
      // Call backend logout if we have a token
      if (token.value) {
        await usersApi.logout()
      }
    } catch (error) {
      console.error('Logout API call failed:', error)
    } finally {
      const tenantStore = useGatewayTenantStore()
      
      // Clear ALL tenant data first
      tenantStore.clearTenant()
      tenantStore.userTenants = [] // Clear tenant list in memory
      
      // Clear ALL auth data
      token.value = null
      refreshToken.value = null
      user.value = null
      
      // Clear ALL localStorage data
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user')
      localStorage.removeItem(ACTIVE_TENANT_ID)
      localStorage.removeItem(TENANT_DATA)
      
      // Prevent tenant store from hydrating old data on next login
      localStorage.setItem('should_hydrate_tenant', 'false')
      
      // Redirect to login
      await router.push({ name: 'login' })
    }
  }
  /**
   * Lấy lại thông tin user profile từ Backend
   */
  const fetchUser = async () => {
    if (!token.value) return
    isLoading.value = true
    try {
      const response = await usersApi.getProfile()
      user.value = response.data
      localStorage.setItem('user', JSON.stringify(response.data))
      return response
    } catch (error) {
      // Don't logout on 400/404 errors, only on auth errors (401/403)
      if (error.response?.status === 401 || error.response?.status === 403) {
        logout()
      }
      // For other errors, just log but don't logout
      throw error
    } finally {
      isLoading.value = false
    }
  }
  /**
   * Cập nhật thông tin user cục bộ (ví dụ đổi avatar, đổi tên)
   */
  const updateAuthUser = (updates) => {
    if (!user.value) return
    user.value = { ...user.value, ...updates }
    localStorage.setItem('user', JSON.stringify(user.value))
  }
  return {
    // State
    user,
    token,
    refreshToken,
    isLoading,
    error,
    isRefreshing,
    // Getters
    isLoggedIn,
    isAdmin,
    isSystemAdmin,
    isAnyAdmin,
    userId,
    currentUser,
    // Actions
    initialize,
    login,
    loginWithCredentials,
    register,
    refreshAccessToken,
    logout,
    fetchUser,
    updateAuthUser
  }
})
