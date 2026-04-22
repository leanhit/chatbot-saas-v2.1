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
export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref(null)
  const token = ref(localStorage.getItem('accessToken') || null)
  const refreshToken = ref(localStorage.getItem('refreshToken') || null)
  const isLoading = ref(false)
  const error = ref(null)
  const isRefreshing = ref(false)
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
    const savedRefreshToken = localStorage.getItem('refreshToken')
    const savedUser = localStorage.getItem('user')
    if (savedToken) {
      token.value = savedToken
    }
    if (savedRefreshToken) {
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
    token.value = authData.token
    refreshToken.value = authData.refreshToken
    user.value = authData.user
    localStorage.setItem('accessToken', authData.token)
    localStorage.setItem('refreshToken', authData.refreshToken)
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
      // 5. Determine redirect based on tenant data
      const tenantStore = useGatewayTenantStore()
      
      console.log('Login - User tenants:', tenantStore.userTenants)
      console.log('Login - Current tenant:', tenantStore.currentTenant)
      console.log('Login - Tenant list length:', tenantStore.userTenants.length)
      
      // Always try to get stored tenant first
      const storedTenantKey = localStorage.getItem('active_tenant_id')
      const storedTenantData = localStorage.getItem('tenant_data')
      console.log('Login - Stored tenant key:', storedTenantKey)
      console.log('Login - Stored tenant data:', storedTenantData)
      console.log('Login - All localStorage keys:', Object.keys(localStorage))
      
      // Re-enable hydrate for future logins
      localStorage.setItem('should_hydrate_tenant', 'true')
      
      if (storedTenantKey && tenantStore.currentTenant) {
        // Has stored active tenant, go to dashboard directly
        console.log('Login - Using stored tenant, going to dashboard')
        await router.push('/dashboard')
      } else if (tenantStore.userTenants.length === 1) {
        // Only one tenant, auto-switch and go to dashboard (same as Enter tenant)
        const onlyTenant = tenantStore.userTenants[0]
        console.log('Login - Auto-switching to only tenant:', onlyTenant.tenantKey)
        await tenantStore.switchTenant(onlyTenant.tenantKey)
        await router.push('/dashboard')
      } else if (tenantStore.userTenants.length > 1) {
        // Multiple tenants, go to tenant gateway
        console.log('Login - Multiple tenants, going to gateway')
        await router.push({ name: 'tenant-gateway' })
      } else {
        // No tenants, go to tenant gateway
        console.log('Login - No tenants, going to gateway')
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
      const authData = response.data
      if (!authData.token) {
        throw new Error("No token received")
      }
      
      // Login with received token
      await login(authData)
      
      // Auto-create workspace with email-based name
      try {
        const tenantStore = useGatewayTenantStore()
        
        // Extract username from email for workspace name
        const email = userData.email
        const workspaceName = email.substring(0, email.indexOf('@')) + "'s Workspace"
        
        // Create workspace
        console.log('Creating default workspace:', workspaceName)
        const createResponse = await tenantApi.createTenant({
          name: workspaceName,
          visibility: 'PUBLIC'
        })
        
        console.log('Workspace created successfully:', createResponse.data)
        
        // Fetch updated tenant list
        await tenantStore.fetchUserTenants()
        
        console.log('User tenants after fetch:', tenantStore.userTenants)
        console.log('Looking for workspace name:', workspaceName)
        
        // Auto-switch to newly created tenant
        let newTenant = tenantStore.userTenants.find(tenant => tenant.name === workspaceName)
        
        // If not found by name, try to get the first tenant (fallback)
        if (!newTenant && tenantStore.userTenants.length > 0) {
          newTenant = tenantStore.userTenants[0]
          console.log('Using first tenant as fallback:', newTenant)
        }
        
        if (newTenant) {
          console.log('Attempting to switch to tenant:', newTenant)
          
          // Apply same logic as Enter tenant button
          await tenantStore.switchTenant(newTenant.tenantKey)
          console.log('Switched to new tenant successfully')
          
          // Redirect to dashboard (same as Enter tenant button)
          await router.push('/dashboard')
        } else {
          // Fallback to tenant gateway if something goes wrong
          console.log('No tenant found, redirecting to tenant gateway')
          await router.push({ name: 'tenant-gateway' })
        }
        
      } catch (workspaceErr) {
        console.error('Failed to create workspace:', workspaceErr)
        // Still consider registration successful, redirect to tenant gateway
        await router.push({ name: 'tenant-gateway' })
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
    if (!refreshToken.value || isRefreshing.value) {
      return false
    }

    isRefreshing.value = true
    try {
      const response = await usersApi.refreshToken({ refreshToken: refreshToken.value })
      const authData = response.data
      
      // Update tokens
      token.value = authData.accessToken
      refreshToken.value = authData.refreshToken
      localStorage.setItem('accessToken', authData.accessToken)
      localStorage.setItem('refreshToken', authData.refreshToken)
      
      return true
    } catch (error) {
      console.error('Refresh token failed:', error)
      // Refresh token expired or invalid - logout
      logout()
      return false
    } finally {
      isRefreshing.value = false
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
      
      console.log('Logout completed - all data cleared, hydrate disabled')
      
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
