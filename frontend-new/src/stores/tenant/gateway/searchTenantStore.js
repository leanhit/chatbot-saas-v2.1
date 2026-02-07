import { defineStore } from 'pinia'
import { ref } from 'vue'
import { tenantApi } from '@/api/tenantApi'

export const useGatewaySearchTenantStore = defineStore('gateway-search-tenant', () => {
  const loading = ref(false)
  const searchResults = ref([])
  const error = ref(null)

  const searchTenants = async (keyword) => {
    if (!keyword?.trim()) {
      searchResults.value = []
      return
    }

    loading.value = true
    error.value = null

    try {
      const { data } = await tenantApi.searchTenant(keyword)
      searchResults.value = data || []
    } catch (error) {
      console.error('searchTenants error:', error)
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

  return {
    loading,
    searchResults,
    error,
    searchTenants,
    clearResults
  }
})
