// stores/tenant-admin/tenantAdminContext.store.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { tenantApi } from '@/api/tenantApi'
import type { TenantDetailResponse } from '@/types/tenant'
import { ACTIVE_TENANT_ID } from '@/utils/constant'

export const useTenantAdminContextStore = defineStore('tenantAdminContext', () => {
  const tenant = ref<TenantDetailResponse | null>(null)
  const loading = ref(false)
  const activeTenantId = ref(localStorage.getItem(ACTIVE_TENANT_ID) || '1') // Default to '1' for testing

  const loadTenant = async () => {
    loading.value = true
    try {
      console.log('Loading tenant with ID:', activeTenantId.value)
      const { data } = await tenantApi.getTenantDetail(activeTenantId.value.toString())
      console.log('Tenant API response:', data)
      tenant.value = data
      console.log('Tenant set in store:', tenant.value)
    } catch (error) {
      console.error('Error loading tenant:', error)
    } finally {
      loading.value = false
    }
  }

  return {
    tenant,
    loading,
    loadTenant,
    activeTenantId
  }
})
