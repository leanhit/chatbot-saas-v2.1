// stores/tenant-admin/tenantAdminContext.store.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { tenantApi } from '@/api/tenantApi'
import { ACTIVE_TENANT_ID } from '@/utils/constant'
export const useTenantAdminContextStore = defineStore('tenantAdminContext', () => {
  const tenant = ref(null)
  const loading = ref(false)
  const activeTenantId = ref(localStorage.getItem(ACTIVE_TENANT_ID) || '1') // Default to '1' for testing
  const loadTenant = async () => {
    loading.value = true
    try {
      const { data } = await tenantApi.getTenantDetail(activeTenantId.value.toString())
      tenant.value = data
    } catch (error) {
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
