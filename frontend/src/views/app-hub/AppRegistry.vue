<template>
  <div class="app-registry">
    <div class="registry-header">
      <div class="header-left">
        <h1>App Registry</h1>
        <p class="header-description">Manage and monitor all applications in the system</p>
      </div>
      <div class="header-actions">
        <button @click="createApp" class="btn btn-primary">
          <Icon icon="mdi:plus" />
          Create App
        </button>
        <button @click="exportApps" class="btn btn-secondary">
          <Icon icon="mdi:download" />
          Export
        </button>
      </div>
    </div>

    <!-- Search and Filters -->
    <div class="filters-section">
      <div class="search-bar">
        <div class="search-input">
          <Icon icon="mdi:magnify" />
          <input 
            v-model="searchQuery" 
            @input="handleSearch"
            placeholder="Search apps by name, description..."
          />
        </div>
      </div>
      
      <div class="filters">
        <select v-model="filters.type" @change="handleFilter">
          <option value="">All Types</option>
          <option value="CHATBOT">Chatbot</option>
          <option value="INTEGRATION">Integration</option>
          <option value="ANALYTICS">Analytics</option>
          <option value="AUTOMATION">Automation</option>
        </select>
        
        <select v-model="filters.status" @change="handleFilter">
          <option value="">All Status</option>
          <option value="ACTIVE">Active</option>
          <option value="INACTIVE">Inactive</option>
          <option value="PENDING">Pending</option>
        </select>
        
        <select v-model="filters.visibility" @change="handleFilter">
          <option value="">All Visibility</option>
          <option value="true">Public</option>
          <option value="false">Private</option>
        </select>
        
        <button @click="resetFilters" class="btn btn-outline">
          <Icon icon="mdi:refresh" />
          Reset
        </button>
      </div>
    </div>

    <!-- Apps Table -->
    <div class="table-container">
      <div class="table-header">
        <div class="table-info">
          <span class="total-count">{{ filteredApps.length }} apps</span>
          <span class="selected-count" v-if="selectedApps.length > 0">
            {{ selectedApps.length }} selected
          </span>
        </div>
        <div class="table-actions" v-if="selectedApps.length > 0">
          <button @click="bulkActivate" class="btn btn-sm btn-success">
            <Icon icon="mdi:check" />
            Activate
          </button>
          <button @click="bulkDeactivate" class="btn btn-sm btn-warning">
            <Icon icon="mdi:pause" />
            Deactivate
          </button>
          <button @click="bulkDelete" class="btn btn-sm btn-danger">
            <Icon icon="mdi:delete" />
            Delete
          </button>
        </div>
      </div>

      <div class="apps-table">
        <table>
          <thead>
            <tr>
              <th width="40">
                <input 
                  type="checkbox" 
                  @change="toggleSelectAll"
                  :checked="isAllSelected"
                />
              </th>
              <th @click="sortBy('name')" class="sortable">
                Name
                <Icon icon="mdi:arrow-up" v-if="sortField === 'name' && sortDirection === 'asc'" />
                <Icon icon="mdi:arrow-down" v-if="sortField === 'name' && sortDirection === 'desc'" />
              </th>
              <th @click="sortBy('appType')" class="sortable">
                Type
                <Icon icon="mdi:arrow-up" v-if="sortField === 'appType' && sortDirection === 'asc'" />
                <Icon icon="mdi:arrow-down" v-if="sortField === 'appType' && sortDirection === 'desc'" />
              </th>
              <th @click="sortBy('status')" class="sortable">
                Status
                <Icon icon="mdi:arrow-up" v-if="sortField === 'status' && sortDirection === 'asc'" />
                <Icon icon="mdi:arrow-down" v-if="sortField === 'status' && sortDirection === 'desc'" />
              </th>
              <th @click="sortBy('isPublic')" class="sortable">
                Visibility
                <Icon icon="mdi:arrow-up" v-if="sortField === 'isPublic' && sortDirection === 'asc'" />
                <Icon icon="mdi:arrow-down" v-if="sortField === 'isPublic' && sortDirection === 'desc'" />
              </th>
              <th @click="sortBy('subscriptionCount')" class="sortable">
                Subscriptions
                <Icon icon="mdi:arrow-up" v-if="sortField === 'subscriptionCount' && sortDirection === 'asc'" />
                <Icon icon="mdi:arrow-down" v-if="sortField === 'subscriptionCount' && sortDirection === 'desc'" />
              </th>
              <th @click="sortBy('createdAt')" class="sortable">
                Created
                <Icon icon="mdi:arrow-up" v-if="sortField === 'createdAt' && sortDirection === 'asc'" />
                <Icon icon="mdi:arrow-down" v-if="sortField === 'createdAt' && sortDirection === 'desc'" />
              </th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="app in paginatedApps" :key="app.id" :class="{ 'selected': selectedApps.includes(app.id) }">
              <td>
                <input 
                  type="checkbox" 
                  :checked="selectedApps.includes(app.id)"
                  @change="toggleSelection(app.id)"
                />
              </td>
              <td>
                <div class="app-name-cell">
                  <div class="app-icon">
                    <Icon icon="mdi:application" />
                  </div>
                  <div class="app-info">
                    <div class="app-name">{{ app.name }}</div>
                    <div class="app-display-name">{{ app.displayName }}</div>
                  </div>
                </div>
              </td>
              <td>
                <span class="app-type" :class="getTypeClass(app.appType)">
                  {{ app.appType }}
                </span>
              </td>
              <td>
                <span class="status" :class="getStatusClass(app.status)">
                  {{ app.status }}
                </span>
              </td>
              <td>
                <span class="visibility" :class="{ 'public': app.isPublic }">
                  {{ app.isPublic ? 'Public' : 'Private' }}
                </span>
              </td>
              <td>
                <span class="subscription-count">{{ app.subscriptionCount || 0 }}</span>
              </td>
              <td>
                <span class="date">{{ formatDate(app.createdAt) }}</span>
              </td>
              <td>
                <div class="action-buttons">
                  <button @click="viewApp(app)" class="btn btn-sm btn-outline" title="View Details">
                    <Icon icon="mdi:eye" />
                  </button>
                  <button @click="editApp(app)" class="btn btn-sm btn-outline" title="Edit">
                    <Icon icon="mdi:pencil" />
                  </button>
                  <button @click="viewSubscriptions(app)" class="btn btn-sm btn-outline" title="Subscriptions">
                    <Icon icon="mdi:credit-card" />
                  </button>
                  <button @click="toggleAppStatus(app)" :class="getStatusToggleClass(app.status)" class="btn btn-sm" :title="app.status === 'ACTIVE' ? 'Deactivate' : 'Activate'">
                    <Icon :icon="app.status === 'ACTIVE' ? 'mdi:pause' : 'mdi:play'" />
                  </button>
                  <button @click="deleteApp(app)" class="btn btn-sm btn-danger" title="Delete">
                    <Icon icon="mdi:delete" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="pagination">
        <div class="pagination-info">
          <span>Showing {{ startIndex + 1 }} to {{ endIndex }} of {{ filteredApps.length }} apps</span>
        </div>
        <div class="pagination-controls">
          <button 
            @click="prevPage" 
            :disabled="currentPage === 1"
            class="btn btn-outline"
          >
            <Icon icon="mdi:chevron-left" />
            Previous
          </button>
          
          <div class="page-numbers">
            <button 
              v-for="page in visiblePages" 
              :key="page"
              @click="goToPage(page)"
              :class="{ active: currentPage === page }"
              class="btn btn-outline btn-sm"
            >
              {{ page }}
            </button>
          </div>
          
          <button 
            @click="nextPage" 
            :disabled="currentPage === totalPages"
            class="btn btn-outline"
          >
            Next
            <Icon icon="mdi:chevron-right" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAppStore } from '@/stores/appStore'

const router = useRouter()
const appStore = useAppStore()

const searchQuery = ref('')
const filters = ref({
  type: '',
  status: '',
  visibility: ''
})

const sortField = ref('createdAt')
const sortDirection = ref('desc')
const currentPage = ref(1)
const pageSize = ref(10)
const selectedApps = ref([])

// Computed properties
const filteredApps = computed(() => {
  let apps = [...appStore.apps]
  
  // Apply search
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    apps = apps.filter(app => 
      app.name.toLowerCase().includes(query) ||
      app.displayName.toLowerCase().includes(query) ||
      app.description?.toLowerCase().includes(query)
    )
  }
  
  // Apply filters
  if (filters.value.type) {
    apps = apps.filter(app => app.appType === filters.value.type)
  }
  if (filters.value.status) {
    apps = apps.filter(app => app.status === filters.value.status)
  }
  if (filters.value.visibility !== '') {
    apps = apps.filter(app => app.isPublic === (filters.value.visibility === 'true'))
  }
  
  // Apply sorting
  apps.sort((a, b) => {
    let aValue = a[sortField.value]
    let bValue = b[sortField.value]
    
    if (typeof aValue === 'string') {
      aValue = aValue.toLowerCase()
      bValue = bValue.toLowerCase()
    }
    
    if (sortDirection.value === 'asc') {
      return aValue > bValue ? 1 : -1
    } else {
      return aValue < bValue ? 1 : -1
    }
  })
  
  return apps
})

const totalPages = computed(() => Math.ceil(filteredApps.value.length / pageSize.value))
const startIndex = computed(() => (currentPage.value - 1) * pageSize.value)
const endIndex = computed(() => Math.min(startIndex.value + pageSize.value, filteredApps.value.length))
const paginatedApps = computed(() => filteredApps.value.slice(startIndex.value, endIndex.value))

const visiblePages = computed(() => {
  const pages = []
  const maxVisible = 5
  let start = Math.max(1, currentPage.value - Math.floor(maxVisible / 2))
  let end = Math.min(totalPages.value, start + maxVisible - 1)
  
  if (end - start + 1 < maxVisible) {
    start = Math.max(1, end - maxVisible + 1)
  }
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  
  return pages
})

const isAllSelected = computed(() => 
  paginatedApps.value.length > 0 && paginatedApps.value.every(app => selectedApps.value.includes(app.id))
)

onMounted(async () => {
  await loadApps()
})

const loadApps = async () => {
  try {
    await appStore.fetchApps()
  } catch (error) {
    console.error('Failed to load apps:', error)
  }
}

const handleSearch = () => {
  currentPage.value = 1
}

const handleFilter = () => {
  currentPage.value = 1
}

const resetFilters = () => {
  searchQuery.value = ''
  filters.value = { type: '', status: '', visibility: '' }
  currentPage.value = 1
}

const sortBy = (field) => {
  if (sortField.value === field) {
    sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortField.value = field
    sortDirection.value = 'desc'
  }
}

const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedApps.value = []
  } else {
    selectedApps.value = paginatedApps.value.map(app => app.id)
  }
}

const toggleSelection = (appId) => {
  const index = selectedApps.value.indexOf(appId)
  if (index > -1) {
    selectedApps.value.splice(index, 1)
  } else {
    selectedApps.value.push(appId)
  }
}

const createApp = () => {
  router.push('/app-hub/apps/create')
}

const viewApp = (app) => {
  router.push(`/app-hub/apps/${app.id}`)
}

const editApp = (app) => {
  router.push(`/app-hub/apps/${app.id}/edit`)
}

const viewSubscriptions = (app) => {
  router.push(`/app-hub/subscriptions?appId=${app.id}`)
}

const toggleAppStatus = async (app) => {
  try {
    const newStatus = app.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
    await appStore.updateApp(app.id, { status: newStatus })
    await loadApps()
  } catch (error) {
    console.error('Failed to toggle app status:', error)
  }
}

const deleteApp = async (app) => {
  if (confirm(`Are you sure you want to delete "${app.name}"?`)) {
    try {
      await appStore.deleteApp(app.id)
      await loadApps()
    } catch (error) {
      console.error('Failed to delete app:', error)
    }
  }
}

const bulkActivate = async () => {
  try {
    await Promise.all(selectedApps.value.map(id => appStore.updateApp(id, { status: 'ACTIVE' })))
    selectedApps.value = []
    await loadApps()
  } catch (error) {
    console.error('Failed to bulk activate apps:', error)
  }
}

const bulkDeactivate = async () => {
  try {
    await Promise.all(selectedApps.value.map(id => appStore.updateApp(id, { status: 'INACTIVE' })))
    selectedApps.value = []
    await loadApps()
  } catch (error) {
    console.error('Failed to bulk deactivate apps:', error)
  }
}

const bulkDelete = async () => {
  if (confirm(`Are you sure you want to delete ${selectedApps.value.length} apps?`)) {
    try {
      await Promise.all(selectedApps.value.map(id => appStore.deleteApp(id)))
      selectedApps.value = []
      await loadApps()
    } catch (error) {
    console.error('Failed to bulk delete apps:', error)
    }
  }
}

const exportApps = () => {
  // Export functionality
  const data = filteredApps.value.map(app => ({
    name: app.name,
    displayName: app.displayName,
    type: app.appType,
    status: app.status,
    visibility: app.isPublic ? 'Public' : 'Private',
    subscriptions: app.subscriptionCount,
    createdAt: app.createdAt
  }))
  
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'apps-export.json'
  a.click()
  URL.revokeObjectURL(url)
}

const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
  }
}

const goToPage = (page) => {
  currentPage.value = page
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleDateString()
}

const getStatusClass = (status) => {
  return {
    'status-active': status === 'ACTIVE',
    'status-inactive': status === 'INACTIVE',
    'status-pending': status === 'PENDING'
  }
}

const getTypeClass = (type) => {
  return {
    'type-chatbot': type === 'CHATBOT',
    'type-integration': type === 'INTEGRATION',
    'type-analytics': type === 'ANALYTICS',
    'type-automation': type === 'AUTOMATION'
  }
}

const getStatusToggleClass = (status) => {
  return {
    'btn-success': status === 'INACTIVE',
    'btn-warning': status === 'ACTIVE'
  }
}
</script>

<style scoped>
.app-registry {
  padding: 24px;
}

.registry-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
}

.header-left h1 {
  font-size: 2rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.header-description {
  color: #6b7280;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.filters-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  display: flex;
  gap: 24px;
  align-items: center;
  flex-wrap: wrap;
}

.search-bar {
  flex: 1;
  min-width: 300px;
}

.search-input {
  position: relative;
  display: flex;
  align-items: center;
}

.search-input input {
  width: 100%;
  padding: 10px 12px 10px 40px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 0.875rem;
}

.search-input svg {
  position: absolute;
  left: 12px;
  color: #6b7280;
}

.filters {
  display: flex;
  gap: 12px;
  align-items: center;
}

.filters select {
  padding: 8px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-size: 0.875rem;
  background: white;
}

.table-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
}

.table-info {
  display: flex;
  gap: 16px;
  align-items: center;
}

.total-count {
  font-weight: 500;
  color: #374151;
}

.selected-count {
  background: #3b82f6;
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
}

.table-actions {
  display: flex;
  gap: 8px;
}

.apps-table {
  overflow-x: auto;
}

.apps-table table {
  width: 100%;
  border-collapse: collapse;
}

.apps-table th {
  background: #f9fafb;
  padding: 12px;
  text-align: left;
  font-weight: 500;
  color: #374151;
  border-bottom: 1px solid #e5e7eb;
  font-size: 0.875rem;
}

.apps-table th.sortable {
  cursor: pointer;
  user-select: none;
}

.apps-table th.sortable:hover {
  background: #f3f4f6;
}

.apps-table td {
  padding: 16px 12px;
  border-bottom: 1px solid #f3f4f6;
  vertical-align: middle;
}

.apps-table tr:hover {
  background: #f9fafb;
}

.apps-table tr.selected {
  background: #eff6ff;
}

.app-name-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.app-icon {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
}

.app-name {
  font-weight: 500;
  color: #1f2937;
  margin: 0 0 2px 0;
}

.app-display-name {
  font-size: 0.875rem;
  color: #6b7280;
}

.app-type {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 500;
}

.type-chatbot {
  background: #dbeafe;
  color: #1e40af;
}

.type-integration {
  background: #f3e8ff;
  color: #6b21a8;
}

.type-analytics {
  background: #fef3c7;
  color: #92400e;
}

.type-automation {
  background: #ecfdf5;
  color: #059669;
}

.status {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 500;
}

.status-active {
  background: #d1fae5;
  color: #059669;
}

.status-inactive {
  background: #fee2e2;
  color: #dc2626;
}

.status-pending {
  background: #fef3c7;
  color: #d97706;
}

.visibility {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.75rem;
  background: #f3f4f6;
  color: #6b7280;
}

.visibility.public {
  background: #dbeafe;
  color: #1e40af;
}

.subscription-count {
  font-weight: 500;
  color: #374151;
}

.date {
  color: #6b7280;
  font-size: 0.875rem;
}

.action-buttons {
  display: flex;
  gap: 4px;
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-top: 1px solid #e5e7eb;
}

.pagination-info {
  color: #6b7280;
  font-size: 0.875rem;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-numbers {
  display: flex;
  gap: 4px;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-primary {
  background: #3b82f6;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #2563eb;
}

.btn-secondary {
  background: #f3f4f6;
  color: #374151;
}

.btn-secondary:hover:not(:disabled) {
  background: #e5e7eb;
}

.btn-outline {
  background: white;
  color: #374151;
  border: 1px solid #e5e7eb;
}

.btn-outline:hover:not(:disabled) {
  background: #f9fafb;
}

.btn-success {
  background: #10b981;
  color: white;
}

.btn-warning {
  background: #f59e0b;
  color: white;
}

.btn-danger {
  background: #ef4444;
  color: white;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 0.875rem;
}

@media (max-width: 768px) {
  .registry-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .filters-section {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filters {
    flex-wrap: wrap;
  }
  
  .table-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .pagination {
    flex-direction: column;
    gap: 12px;
  }
  
  .pagination-controls {
    justify-content: center;
  }
}
</style>
