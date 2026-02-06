import { defineStore } from 'pinia'
import { ref } from 'vue'
import { tenantApi } from '@/api/tenantApi'
import router from '@/router'

export const useGatewayCreateTenantStore = defineStore(
  'gateway-create-tenant',
  () => {
    const loading = ref(false)
    const error = ref(null)

    const createTenant = async (payload) => {
      loading.value = true
      error.value = null
      
      try {
        const response = await tenantApi.createTenant(payload)
        // ElMessage.success('Tạo Workspace thành công') // Comment out for Windzo
        return { success: true, data: response.data }
      } catch (error) {
        console.error('createTenant error:', error)
        
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
