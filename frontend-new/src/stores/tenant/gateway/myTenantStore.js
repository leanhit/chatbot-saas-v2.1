import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { tenantApi } from '@/api/tenantApi'
import { usersApi } from '@/api/usersApi'
import { useAuthStore } from '@/stores/authStore'

// Constants (moved from utils/constant to avoid import issues)
const ACTIVE_TENANT_ID = 'active_tenant_id'
const TENANT_DATA = 'tenant_data'

export const useGatewayTenantStore = defineStore('gateway-tenant', () => {
  // hydrate from localStorage with defensive check
  let storedTenant = null
  try {
    if (typeof localStorage !== 'undefined') {
      storedTenant = localStorage.getItem(TENANT_DATA)
    }
  } catch (error) {
    console.warn('localStorage access failed:', error)
  }

  // state
  const userTenants = ref([])
  const currentTenant = ref(
    storedTenant ? JSON.parse(storedTenant) : null
  )

  const loadingTenants = ref(false)
  const switchingTenant = ref(false)

  // getters
  const activeTenantId = computed(() => currentTenant.value?.id)

  // actions
  const fetchUserTenants = async () => {
    loadingTenants.value = true
    try {
      const { data } = await tenantApi.getUserTenants()
      userTenants.value = data
    } catch (error) {
      console.error('fetchUserTenants error:', error)
      if (error.response?.data?.message?.includes('SUSPENDED')) {
        // Handle suspended tenant
        // ElMessage.error('Tài khoản của bạn đã bị tạm dừng. Vui lòng liên hệ quản trị viên.') // Comment out for Windzo
        // Clear stored tenant data
        clearTenant()
      }
      throw error
    } finally {
      loadingTenants.value = false
    }
  }

  const switchTenant = async (tenantId) => {
    switchingTenant.value = true
    try {
      // Use tenantId instead of tenantKey since backend only returns id
      const { data } = await tenantApi.getTenantDetail(tenantId)
      console.log('🔍 Tenant data received:', data)
      console.log('🔍 Tenant ID types:', {
        tenantId: typeof tenantId,
        dataId: typeof data.id,
        dataTenantKey: typeof data.tenantKey,
        dataTenantKeyType: typeof data.tenantKey
      })
      currentTenant.value = data

      // Fetch user profile now that tenant context is set
      try {
        const authStore = useAuthStore()
        console.log('Fetching user profile after tenant switch...')
        const profileResponse = await usersApi.getProfile()
        authStore.user = profileResponse.data
        localStorage.setItem('user', JSON.stringify(profileResponse.data))
        console.log('✅ User profile updated after tenant switch')
      } catch (profileError) {
        console.warn('Could not fetch user profile after tenant switch:', profileError)
        // Don't block tenant switch for profile errors
      }

      // Defensive localStorage access
      try {
        if (typeof localStorage !== 'undefined') {
          localStorage.setItem(TENANT_DATA, JSON.stringify(data))
          localStorage.setItem(ACTIVE_TENANT_ID, data.id) // Use data.id instead of data.tenantKey
          console.log('✅ Saved tenant ID to localStorage:', data.id)
        }
      } catch (storageError) {
        console.warn('localStorage write failed:', storageError)
      }
    } catch (error) {
      console.error('switchTenant error:', error)
      throw error
    } finally {
      switchingTenant.value = false
    }
  }

  const clearTenant = () => {
    currentTenant.value = null
    
    // Defensive localStorage access
    try {
      if (typeof localStorage !== 'undefined') {
        localStorage.removeItem(TENANT_DATA)
        localStorage.removeItem(ACTIVE_TENANT_ID)
      }
    } catch (error) {
      console.warn('localStorage clear failed:', error)
    }
  }

  const suspendTenant = async (id) => {
    await tenantApi.suspendTenant(id);
    await fetchUserTenants(); // Cập nhật lại danh sách để thấy status thay đổi
  };

  const activateTenant = async (id) => {
    await tenantApi.activateTenant(id);
    await fetchUserTenants();
  };

  return {
    // state
    userTenants,
    currentTenant,
    loadingTenants,
    switchingTenant,

    // getters
    activeTenantId,

    // actions
    fetchUserTenants,
    switchTenant,
    clearTenant,
    suspendTenant,
    activateTenant
  }
})
