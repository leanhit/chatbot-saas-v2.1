import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { tenantApi } from '@/api/tenantApi'
import type { TenantDetailResponse } from '@/types/tenant'

import { ACTIVE_TENANT_ID, TENANT_DATA } from '@/utils/constant'

export const useGatewayTenantStore = defineStore('gateway-tenant', () => {
  // hydrate from localStorage
  const storedTenant = localStorage.getItem(TENANT_DATA)

  // state
  const userTenants = ref<TenantDetailResponse[]>([])
  const currentTenant = ref<TenantDetailResponse | null>(
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
    } catch (error: any) {
      console.error('fetchUserTenants error:', error)
      if (error.response?.data?.message?.includes('SUSPENDED')) {
        // Handle suspended tenant
        ElMessage.error('Tài khoản của bạn đã bị tạm dừng. Vui lòng liên hệ quản trị viên.')
        // Clear stored tenant data
        clearTenant()
      }
      throw error
    } finally {
      loadingTenants.value = false
    }
  }

  const switchTenant = async (tenantId: string) => {
    switchingTenant.value = true
    try {
      const { data } = await tenantApi.getTenantDetail(tenantId)
      currentTenant.value = data

      localStorage.setItem(TENANT_DATA, JSON.stringify(data))
      localStorage.setItem(ACTIVE_TENANT_ID, tenantId)
    } catch (error) {
      console.error('switchTenant error:', error)
      throw error
    } finally {
      switchingTenant.value = false
    }
  }

  const clearTenant = () => {
    currentTenant.value = null
    localStorage.removeItem(TENANT_DATA)
    localStorage.removeItem(ACTIVE_TENANT_ID)
  }

  const suspendTenant = async (id: string) => {
    await tenantApi.suspendTenant(id);
    await fetchUserTenants(); // Cập nhật lại danh sách để thấy status thay đổi
  };

  const activateTenant = async (id: string) => {
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
