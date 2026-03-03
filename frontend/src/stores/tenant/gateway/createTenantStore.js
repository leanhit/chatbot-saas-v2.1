import { defineStore } from 'pinia'
import { ref } from 'vue'
import { tenantApi } from '@/api/tenantApi'
import router from '@/router'
import { CreateTenantRequest, validateCreateTenantRequest } from '@/types/tenant'
export const useGatewayCreateTenantStore = defineStore(
  'gateway-create-tenant',
  () => {
    const loading = ref(false)
    const error = ref(null)
    const createTenant = async (payload) => {
      // Validate payload using types
      const errors = validateCreateTenantRequest(payload);
      if (errors.length > 0) {
        error.value = errors.join(', ');
        return { success: false, error: errors.join(', ') };
      }
      
      loading.value = true
      error.value = null
      try {
        const response = await tenantApi.createTenant(payload)
        return { success: true, data: response.data }
      } catch (error) {
        if (error.response?.data?.message?.includes('SUSPENDED')) {
          // Handle suspended tenant
          // ElMessage.error('Tài khoản của bạn đã bị tạm dừng. Vui lòng liên hệ quản trị viên.') // Comment out for Windzo
          // Clear any stored tenant data
          localStorage.removeItem('TENANT_DATA')
          localStorage.removeItem('ACTIVE_TENANT_ID')
          // Redirect to login
          router.push('/login')
          return { success: false, error: 'TENANT_SUSPENDED' }
        }
        const errorMessage = error.response?.data?.message || 'Không thể tạo Workspace'
        // ElMessage.error(errorMessage) // Comment out for Windzo
        return { success: false, error: errorMessage }
      } finally {
        loading.value = false
      }
    }
    const clearError = () => {
      error.value = null
    }
    return {
      loading,
      error,
      createTenant,
      clearError
    }
  }
)
