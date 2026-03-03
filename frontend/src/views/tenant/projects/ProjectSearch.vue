<template>
  <div class="project-search-container">
    <!-- Search Header -->
    <div class="mb-6">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-xl font-semibold text-gray-900 dark:text-white">Projects</h2>
        <GlobalSearchBar 
          placeholder="Search projects by name or description..."
        />
      </div>
      
      <!-- Search Context Indicator -->
      <div v-if="searchStore.isSearchActive" class="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400">
        <Icon icon="mdi:information" class="h-4 w-4" />
        <span>Searching for "{{ searchStore.searchQuery }}" in projects</span>
        <button
          @click="clearSearch"
          class="text-primary hover:text-primary-600"
        >
          Clear
        </button>
      </div>
    </div>

    <!-- Filter Options -->
    <div class="mb-6 flex gap-4">
      <select
        :value="searchStore.getFilter('status')"
        @change="updateFilter('status', $event.target.value)"
        class="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-primary focus:border-primary dark:bg-gray-700 dark:border-gray-600 dark:text-white"
      >
        <option value="">All Status</option>
        <option value="active">Active</option>
        <option value="archived">Archived</option>
        <option value="draft">Draft</option>
      </select>
      
      <select
        :value="searchStore.getFilter('type')"
        @change="updateFilter('type', $event.target.value)"
        class="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-primary focus:border-primary dark:bg-gray-700 dark:border-gray-600 dark:text-white"
      >
        <option value="">All Types</option>
        <option value="web">Web App</option>
        <option value="mobile">Mobile App</option>
        <option value="api">API</option>
      </select>
    </div>

    <!-- Results -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <div
        v-for="project in filteredProjects"
        :key="project.id"
        class="bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg p-4 hover:shadow-md transition-shadow"
      >
        <h3 class="font-medium text-gray-900 dark:text-white">{{ project.name }}</h3>
        <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">{{ project.description }}</p>
        <div class="mt-3 flex items-center justify-between">
          <span class="text-xs px-2 py-1 rounded-full" :class="getStatusClass(project.status)">
            {{ project.status }}
          </span>
          <span class="text-xs text-gray-500">{{ project.type }}</span>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-if="filteredProjects.length === 0" class="text-center py-12">
      <Icon icon="mdi:folder-search" class="mx-auto h-12 w-12 text-gray-400" />
      <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">
        {{ searchStore.hasQuery ? 'No projects found matching your search.' : 'No projects available.' }}
      </p>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Icon } from '@iconify/vue'
import { useSearchStore } from '@/stores/searchStore'
import GlobalSearchBar from '@/components/common/GlobalSearchBar.vue'

export default {
  name: 'ProjectSearch',
  components: {
    Icon,
    GlobalSearchBar
  },
  setup() {
    const searchStore = useSearchStore()
    
    // Mock project data
    const projects = ref([
      { id: 1, name: 'E-commerce Platform', description: 'Online shopping platform with payment integration', status: 'active', type: 'web' },
      { id: 2, name: 'Mobile Banking App', description: 'iOS and Android banking application', status: 'active', type: 'mobile' },
      { id: 3, name: 'Analytics API', description: 'RESTful API for data analytics', status: 'draft', type: 'api' },
      { id: 4, name: 'Customer Portal', description: 'Customer self-service portal', status: 'archived', type: 'web' },
      { id: 5, name: 'Inventory Management', description: 'Warehouse and inventory tracking system', status: 'active', type: 'web' }
    ])
    
    // Computed filtered projects
    const filteredProjects = computed(() => {
      let filtered = projects.value
      
      // Apply search query
      if (searchStore.hasQuery) {
        const query = searchStore.searchQuery.toLowerCase()
        filtered = filtered.filter(project => 
          project.name.toLowerCase().includes(query) ||
          project.description.toLowerCase().includes(query)
        )
      }
      
      // Apply filters
      const statusFilter = searchStore.getFilter('status')
      if (statusFilter) {
        filtered = filtered.filter(project => project.status === statusFilter)
      }
      
      const typeFilter = searchStore.getFilter('type')
      if (typeFilter) {
        filtered = filtered.filter(project => project.type === typeFilter)
      }
      
      return filtered
    })
    
    const updateFilter = (key, value) => {
      searchStore.setFilter(key, value)
    }
    
    const clearSearch = () => {
      searchStore.clearSearch()
    }
    
    const getStatusClass = (status) => {
      const classes = {
        active: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
        archived: 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200',
        draft: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200'
      }
      return classes[status] || 'bg-gray-100 text-gray-800'
    }
    
    // Set search context and cleanup on unmount
    onMounted(() => {
      searchStore.setSearchContext('projects')
    })
    
    onUnmounted(() => {
      // Optionally reset search when leaving the component
      // searchStore.resetSearch()
    })
    
    return {
      searchStore,
      projects,
      filteredProjects,
      updateFilter,
      clearSearch,
      getStatusClass
    }
  }
}
</script>

<style scoped>
.project-search-container {
  /* Component-specific styles */
}
</style>
