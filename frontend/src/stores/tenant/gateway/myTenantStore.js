import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { tenantApi } from '@/api/tenantApi'
import { usersApi } from '@/api/usersApi'
import { useAuthStore } from '@/stores/authStore'
import { TenantStatus } from '@/types/tenant'
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
    console.error('Error reading from localStorage:', error);
  }
  // state
  const userTenants = ref([])
  const currentTenant = ref(
    storedTenant ? JSON.parse(storedTenant) : null
  )
  const loadingTenants = ref(false)
  const switchingTenant = ref(false)
  // getters
  const activeTenantId = computed(() => currentTenant.value?.tenantKey)
  // actions
  const fetchUserTenants = async () => {
    loadingTenants.value = true
    try {
      const { data } = await tenantApi.getUserTenants()
      userTenants.value = data
    } catch (error) {
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
  const switchTenant = async (tenantKey) => {
    switchingTenant.value = true
    try {
      // Use new endpoint with tenantKey
      const { data } = await tenantApi.getTenant(tenantKey)
      currentTenant.value = data
      
      // Reset payment store to prevent cross-tenant data contamination
      try {
        const { usePaymentStore } = await import('@/stores/paymentStore')
        const paymentStore = usePaymentStore()
        paymentStore.resetState()
        // Reload payment data for new tenant context
        await paymentStore.loadPaymentHistory()
        await paymentStore.loadPackages()
        await paymentStore.loadCurrentPackage()
        console.log('Payment store reset and reloaded on tenant switch')
      } catch (paymentError) {
        console.warn('Failed to reset payment store:', paymentError)
      }
      
      // Wallet and billing stores removed - only simple payment available
      
      // Fetch user profile now that tenant context is set
      try {
        const authStore = useAuthStore()
        const profileResponse = await usersApi.getProfile()
        authStore.user = profileResponse.data
        localStorage.setItem('user', JSON.stringify(profileResponse.data))
      } catch (profileError) {
        // Don't block tenant switch for profile errors
      }
      // Defensive localStorage access
      try {
        if (typeof localStorage !== 'undefined') {
          localStorage.setItem(TENANT_DATA, JSON.stringify(data))
          localStorage.setItem(ACTIVE_TENANT_ID, data.tenantKey) // ✅ Lưu tenantKey
        }
      } catch (storageError) {
        console.error('Error saving tenant data:', storageError);
      }
    } catch (error) {
      console.error('Error switching tenant:', error);
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
    }
  }
  const suspendTenant = async (tenantKey) => {
    await tenantApi.suspendTenant(tenantKey);
    await fetchUserTenants(); // Cập nhật lại danh sách để thấy status thay đổi
  };
  const activateTenant = async (tenantKey) => {
    await tenantApi.activateTenant(tenantKey);
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
