import { defineStore } from 'pinia'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { tenantApi } from '@/api/tenantApi'
import type { CreateTenantRequest } from '@/types/tenant'
import router from '@/router'

export const useGatewayCreateTenantStore = defineStore(
  'gateway-create-tenant',
  () => {
    const loading = ref(false)
    const error = ref<string | null>(null)

    const createTenant = async (payload: CreateTenantRequest) => {
      loading.value = true
      error.value = null
      
      try {
        const response = await tenantApi.createTenant(payload)
        ElMessage.success('Tạo Workspace thành công')
        return { success: true, data: response.data }
      } catch (error: any) {
        console.error('createTenant error:', error)
        
        if (error.response?.data?.message?.includes('SUSPENDED')) {
          // Handle suspended tenant
          ElMessage.error('Tài khoản của bạn đã bị tạm dừng. Vui lòng liên hệ quản trị viên.')
          // Clear any stored tenant data
          localStorage.removeItem('TENANT_DATA')
          localStorage.removeItem('ACTIVE_TENANT_ID')
          // Redirect to login
          router.push('/login')
          return { success: false, error: 'TENANT_SUSPENDED' }
        }
        
        const errorMessage = error.response?.data?.message || 'Không thể tạo Workspace'
        ElMessage.error(errorMessage)
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
