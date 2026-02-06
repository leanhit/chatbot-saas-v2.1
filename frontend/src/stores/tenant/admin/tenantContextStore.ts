// stores/tenant-admin/tenantAdminContext.store.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { tenantApi } from '@/api/tenantApi'
import type { TenantDetailResponse } from '@/types/tenant'
import { ACTIVE_TENANT_ID } from '@/utils/constant'

export const useTenantAdminContextStore = defineStore('tenantAdminContext', () => {
  const tenant = ref<TenantDetailResponse | null>(null)
  const loading = ref(false)
  const activeTenantId = localStorage.getItem(ACTIVE_TENANT_ID) || '';

  const loadTenant = async () => {
    loading.value = true
    try {
      const { data } = await tenantApi.getTenantDetail(activeTenantId.toString())
      tenant.value = data
    } finally {
      loading.value = false
    }
  }

  return {
    tenant,
    loading,
    loadTenant
  }
})
