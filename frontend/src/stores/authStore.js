import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import router from '@/router'
import { usersApi } from '@/api/usersApi'
import { useGatewayTenantStore } from './tenant/gateway/myTenantStore'
import axios from '@/plugins/axios'
// Import constants from tenant store (giống frontend)
const TENANT_DATA = 'tenant_data'
const ACTIVE_TENANT_ID = 'active_tenant_id'
export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref(null)
  const token = ref(localStorage.getItem('accessToken') || null)
  const isLoading = ref(false)
  const error = ref(null)
  const rememberMe = ref(false)
  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.systemRole === 'ADMIN')
  const userId = computed(() => user.value?.id)
  const currentUser = computed(() => user.value)
  // Actions
  /**
   * Khởi tạo trạng thái Auth (Được gọi từ main.ts)
   * Đọc token và user từ localStorage để khôi phục phiên làm việc
   */
  const initialize = () => {
    const savedToken = localStorage.getItem('accessToken')
    const savedUser = localStorage.getItem('user')
    const savedRememberMe = localStorage.getItem('rememberMe')
    
    if (savedToken) {
      token.value = savedToken
    }
    if (savedUser) {
      try {
        user.value = JSON.parse(savedUser)
      } catch (e) {
        localStorage.removeItem('user')
      }
    }
    
    // Khôi phục trạng thái remember me
    if (savedRememberMe) {
      try {
        rememberMe.value = JSON.parse(savedRememberMe)
      } catch (e) {
        localStorage.removeItem('rememberMe')
      }
    }
  }
  /**
   * Xử lý đăng nhập thành công
   */
  const login = async (authData) => {
    token.value = authData.token
    user.value = authData.user
    localStorage.setItem('accessToken', authData.token)
    localStorage.setItem('user', JSON.stringify(authData.user))
    
    // Lưu trạng thái remember me
    localStorage.setItem('rememberMe', JSON.stringify(rememberMe.value))
  }
  /**
   * Đăng nhập với credentials
   */
  const loginWithCredentials = async (credentials) => {
    isLoading.value = true
    error.value = null
    
    // Cập nhật rememberMe state trước khi login
    if (credentials.rememberMe !== undefined) {
      rememberMe.value = credentials.rememberMe
    }
    
    try {
      // 1. Gọi API Login
      const res = await usersApi.login(credentials)
      // API returns { data: UserResponse } so we need to access res.data
      const authData = res.data
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
      // 5. Điều hướng dựa trên dữ liệu Tenant đã fetch
      const tenantStore = useGatewayTenantStore()
      if (!tenantStore.currentTenant) {
        // No active tenant, redirect to tenant gateway
        await router.push({ name: 'tenant-gateway' })
      } else {
        // Has active tenant, go to home
        await router.push('/')
      }
      return { success: true, data: authData }
    } catch (error) {
      console.error('Login error:', error)
      error.value = error.response?.data?.message || error.message || 'Login failed'
      return { success: false, error: error.message }
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
      const authData = response.data
      if (!authData.token) {
        throw new Error("No token received")
      }
      await login(authData)
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
   * Đăng xuất và dọn dẹp dữ liệu
   */
  const logout = () => {
    token.value = null
    user.value = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('user')
    localStorage.removeItem('rememberMe') // Xóa remember me khi logout
    const tenantStore = useGatewayTenantStore()
    tenantStore.currentTenant = null
    localStorage.removeItem(TENANT_DATA)
    localStorage.removeItem(ACTIVE_TENANT_ID)
    router.push({ name: 'login' })
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
    isLoading,
    error,
    rememberMe,
    // Getters
    isLoggedIn,
    isAdmin,
    userId,
    currentUser,
    // Actions
    initialize,
    login,
    loginWithCredentials,
    register,
    logout,
    fetchUser,
    updateAuthUser
  }
})
