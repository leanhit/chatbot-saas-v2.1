<template>
  <div class="global-search-bar">
    <div class="relative" :class="{ 'max-w-md': !fullWidth, 'w-full': fullWidth }">
      <!-- Search Icon -->
      <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
        <Icon icon="mdi:magnify" class="h-5 w-5 text-gray-400" />
      </div>
      
      <!-- Search Input -->
      <input
        v-model="searchStore.searchQuery"
        type="text"
        :placeholder="placeholder"
        class="block w-full pl-10 pr-10 py-2 border border-gray-300 rounded-md leading-5 bg-white placeholder-gray-500 focus:outline-none focus:placeholder-gray-400 focus:ring-1 focus:ring-primary focus:border-primary dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white"
        @focus="showHistory = true"
        @blur="hideHistory"
      />
      
      <!-- Clear Button -->
      <div 
        v-if="searchStore.hasQuery"
        class="absolute inset-y-0 right-0 pr-3 flex items-center"
      >
        <button
          @click="clearSearch"
          class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 focus:outline-none"
        >
          <Icon icon="mdi:close" class="h-4 w-4" />
        </button>
      </div>
      
      <!-- Search History Dropdown -->
      <div
        v-if="showHistory && searchStore.searchHistory.length > 0"
        class="absolute top-full left-0 right-0 mt-1 bg-white border border-gray-300 rounded-md shadow-lg z-50 dark:bg-gray-700 dark:border-gray-600"
      >
        <div class="py-1">
          <div class="px-3 py-2 text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
            Recent Searches
          </div>
          <button
            v-for="query in searchStore.searchHistory"
            :key="query"
            @click="selectFromHistory(query)"
            class="w-full px-3 py-2 text-left text-sm text-gray-700 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-gray-600 flex items-center justify-between group"
          >
            <span>{{ query }}</span>
            <button
              @click.stop="removeFromHistory(query)"
              class="opacity-0 group-hover:opacity-100 text-gray-400 hover:text-red-500 dark:text-gray-500 dark:hover:text-red-400"
            >
              <Icon icon="mdi:close" class="h-3 w-3" />
            </button>
          </button>
          <button
            @click="clearHistory"
            class="w-full px-3 py-2 text-left text-xs text-gray-500 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-gray-600"
          >
            Clear History
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { Icon } from '@iconify/vue'
import { useSearchStore } from '@/stores/searchStore'

export default {
  name: 'GlobalSearchBar',
  components: {
    Icon
  },
  props: {
    placeholder: {
      type: String,
      default: 'Search...'
    },
    fullWidth: {
      type: Boolean,
      default: false
    }
  },
  setup() {
    const searchStore = useSearchStore()
    const showHistory = ref(false)
    
    const clearSearch = () => {
      searchStore.clearSearch()
      showHistory.value = false
    }
    
    const selectFromHistory = (query) => {
      searchStore.setSearchQuery(query)
      showHistory.value = false
    }
    
    const removeFromHistory = (query) => {
      searchStore.removeFromHistory(query)
    }
    
    const clearHistory = () => {
      searchStore.clearHistory()
      showHistory.value = false
    }
    
    const hideHistory = () => {
      // Delay hiding to allow click events to register
      setTimeout(() => {
        showHistory.value = false
      }, 200)
    }
    
    return {
      searchStore,
      showHistory,
      clearSearch,
      selectFromHistory,
      removeFromHistory,
      clearHistory,
      hideHistory
    }
  }
}
</script>

<style scoped>
.global-search-bar {
  /* Add any custom styles here */
}
</style>
