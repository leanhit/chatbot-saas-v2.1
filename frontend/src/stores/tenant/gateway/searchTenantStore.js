import { defineStore } from 'pinia'
import { ref } from 'vue'
import { tenantApi } from '@/api/tenantApi'
import { TenantSearchRequest, createDefaultTenantSearchRequest } from '@/types/tenant'
export const useGatewaySearchTenantStore = defineStore('gateway-search-tenant', () => {
  const loading = ref(false)
  const searchResults = ref([])
  const error = ref(null)
  
  const searchTenants = async (searchParams) => {
    // Create search request with defaults
    const searchRequest = {
      ...createDefaultTenantSearchRequest(),
      ...searchParams
    }
    
    if (!searchRequest.keyword?.trim()) {
      searchResults.value = []
      return
    }
    
    loading.value = true
    error.value = null
    try {
      const { data } = await tenantApi.searchTenants(searchRequest)
      // Backend returns Page<TenantSearchResponse> with content field
      searchResults.value = data.content || []
    } catch (error) {
      error.value = error.response?.data?.message || 'Không thể tìm kiếm tenant'
      searchResults.value = []
    } finally {
      loading.value = false
    }
  }
  const clearResults = () => {
    searchResults.value = []
    error.value = null
  }
  const requestJoinTenant = async (tenantKey) => {
    console.log('requestJoinTenant called with tenantKey:', tenantKey)
    try {
      console.log('Calling tenantApi.requestJoinTenant...')
      await tenantApi.requestJoinTenant(tenantKey)
      console.log('API call completed successfully')
    } catch (error) {
      console.error('API call failed:', error)
      error.value = error.response?.data?.message || 'Không thể gửi yêu cầu tham gia'
      throw error
    }
  }
  return {
    loading,
    searchResults,
    error,
    searchTenants,
    clearResults,
    requestJoinTenant
  }
})
