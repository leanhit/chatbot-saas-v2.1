// stores/tenant-admin/tenantAdminContext.store.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { tenantApi } from '@/api/tenantApi'
import { ACTIVE_TENANT_ID } from '@/utils/constant'
export const useTenantAdminContextStore = defineStore('tenantAdminContext', () => {
  const tenant = ref(null)
  const loading = ref(false)
  // Get tenantKey from localStorage (same as gateway store)
  const activeTenantKey = ref(localStorage.getItem(ACTIVE_TENANT_ID) || null)
  const loadTenant = async () => {
    loading.value = true
    try {
      if (!activeTenantKey.value) {
        console.warn('No active tenant key found')
        return
      }
      const { data } = await tenantApi.getTenant(activeTenantKey.value)
      tenant.value = data
    } catch (error) {
      console.error('Failed to load tenant:', error)
    } finally {
      loading.value = false
    }
  }
  return {
    tenant,
    loading,
    loadTenant,
    activeTenantKey
  }
})
