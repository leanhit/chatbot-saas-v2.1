import { defineStore } from 'pinia'
import { ref } from 'vue'
import { tenantApi } from '@/api/tenantApi'
import { tenantMembershipApi } from '@/api/tenantMembershipApi'
import type { TenantSearchResponse } from '@/types/tenant'

export const useGatewaySearchStore = defineStore('gateway-search', () => {
  const results = ref<TenantSearchResponse[]>([])
  const loading = ref(false)

  const searchTenants = async (keyword: string) => {
    if (!keyword.trim()) {
      results.value = []
      return
    }

    loading.value = true
    try {
      console.log('🔍 Search keyword:', keyword)
      const { data } = await tenantApi.searchTenant(keyword)
      console.log('📦 API response data:', data)
      console.log('📊 Data type:', typeof data)
      console.log('📏 Data length:', Array.isArray(data) ? data.length : 'Not array')
      
      // API returns array directly, not wrapped in .content
      results.value = data || []
      console.log('💾 Store results after update:', results.value)
    } catch (error) {
      console.error('searchTenants error:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const requestJoinTenant = async (tenantId: string) => {
    try {
      await tenantMembershipApi.requestJoinTenant(tenantId)
    } catch (error) {
      console.error('requestJoinTenant error:', error)
      throw error
    }
  }

  const clearResults = () => {
    results.value = []
  }

  return {
    results,
    loading,
    searchTenants,
    requestJoinTenant,
    clearResults
  }
})
