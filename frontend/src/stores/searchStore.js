import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useSearchStore = defineStore('search', () => {
  // Global search state
  const searchQuery = ref('')
  const searchContext = ref('') // 'members', 'projects', 'files', etc.
  const isSearchActive = ref(false)

  // Search history for autocomplete
  const searchHistory = ref([])
  const maxHistoryItems = 10

  // Search filters (context-specific)
  const filters = ref({})
  
  // Computed properties
  const hasQuery = computed(() => searchQuery.value.trim().length > 0)
  const isEmpty = computed(() => !hasQuery.value)
  
  // Actions
  const setSearchQuery = (query) => {
    searchQuery.value = query
    isSearchActive.value = hasQuery.value
    
    // Add to history if query is not empty and not already in history
    if (query.trim() && !searchHistory.value.includes(query)) {
      searchHistory.value.unshift(query)
      if (searchHistory.value.length > maxHistoryItems) {
        searchHistory.value = searchHistory.value.slice(0, maxHistoryItems)
      }
    }
  }

  const clearSearch = () => {
    searchQuery.value = ''
    isSearchActive.value = false
  }

  const setSearchContext = (context) => {
    searchContext.value = context
    // Reset filters when context changes
    filters.value = {}
  }

  const setFilter = (key, value) => {
    filters.value[key] = value
  }

  const clearFilters = () => {
    filters.value = {}
  }

  const getFilter = (key) => {
    return filters.value[key] || ''
  }

  const removeFromHistory = (query) => {
    const index = searchHistory.value.indexOf(query)
    if (index > -1) {
      searchHistory.value.splice(index, 1)
    }
  }

  const clearHistory = () => {
    searchHistory.value = []
  }

  // Reset search state (useful when navigating away)
  const resetSearch = () => {
    searchQuery.value = ''
    searchContext.value = ''
    isSearchActive.value = false
    filters.value = {}
  }

  return {
    // State
    searchQuery,
    searchContext,
    isSearchActive,
    searchHistory,
    filters,
    
    // Computed
    hasQuery,
    isEmpty,
    
    // Actions
    setSearchQuery,
    clearSearch,
    setSearchContext,
    setFilter,
    clearFilters,
    getFilter,
    removeFromHistory,
    clearHistory,
    resetSearch
  }
})
