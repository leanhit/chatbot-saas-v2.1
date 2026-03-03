<template>
  <div 
    v-if="searchStore.isSearchActive && searchStore.searchContext"
    class="search-context-indicator flex items-center gap-2 px-3 py-1 text-xs bg-primary/10 text-primary rounded-full ml-2"
  >
    <Icon icon="mdi:information" class="h-3 w-3" />
    <span>Searching in {{ getContextLabel() }}</span>
    <button
      @click="clearSearch"
      class="ml-1 hover:text-primary/80"
      title="Clear search"
    >
      <Icon icon="mdi:close" class="h-3 w-3" />
    </button>
  </div>
</template>

<script>
import { Icon } from '@iconify/vue'
import { useSearchStore } from '@/stores/searchStore'

export default {
  name: 'SearchContextIndicator',
  components: {
    Icon
  },
  setup() {
    const searchStore = useSearchStore()
    
    const getContextLabel = () => {
      const context = searchStore.searchContext
      const labels = {
        'members': 'Members',
        'projects': 'Projects',
        'files': 'Files',
        'tenants': 'Tenants',
        'users': 'Users'
      }
      return labels[context] || context
    }
    
    const clearSearch = () => {
      searchStore.clearSearch()
    }
    
    return {
      searchStore,
      getContextLabel,
      clearSearch
    }
  }
}
</script>

<style scoped>
.search-context-indicator {
  animation: fadeIn 0.2s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
