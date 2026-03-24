import { defineStore } from 'pinia'
import { ref } from 'vue'
import { tenantApi } from '@/api/tenantApi'
import { TenantSearchRequest, createDefaultTenantSearchRequest } from '@/types/tenant'
export const useGatewaySearchTenantStore = defineStore('gateway-search-tenant', () => {
  const loading = ref(false)
  const searchResults = ref([])
  const error = ref(null)
  
  const searchTenants = async (searchParams) => {
    // Handle case where searchParams is a string (keyword)
    const keywordParam = typeof searchParams === 'string' ? searchParams : searchParams?.keyword || searchParams;
    
    // Create search request with defaults
    const searchRequest = {
      ...createDefaultTenantSearchRequest(),
      keyword: keywordParam
    }
    
    console.log('🔍 Search Debug - searchRequest:', searchRequest)
    
    if (!searchRequest.keyword?.trim()) {
      console.log('🔍 Search Debug - Empty keyword, clearing results')
      searchResults.value = []
      return
    }
    
    loading.value = true
    error.value = null
    try {
      console.log('🔍 Search Debug - Calling API with:', searchRequest)
      const { data } = await tenantApi.searchTenants(searchRequest)
      console.log('🔍 Search Debug - API Response:', data)
      // Backend returns Page<TenantSearchResponse> with content field
      searchResults.value = data.content || []
      console.log('🔍 Search Debug - Final results:', searchResults.value)
    } catch (error) {
      console.error('🔍 Search Debug - Error:', error)
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
