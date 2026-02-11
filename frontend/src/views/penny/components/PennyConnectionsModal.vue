<template>
  <div class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full z-50">
    <div class="relative min-h-screen flex items-center justify-center p-4">
      <div class="relative bg-white dark:bg-gray-800 rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:w-full sm:max-w-5xl">
        <!-- Header -->
        <div class="bg-white dark:bg-gray-800 px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
          <div class="flex items-center justify-between">
            <h3 class="text-lg leading-6 font-medium text-gray-900 dark:text-white">
              {{ $t('Connections Management') }} - {{ bot.name }}
            </h3>
            <button
              @click="$emit('close')"
              class="rounded-md text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              <Icon icon="mdi:close" class="h-6 w-6" />
            </button>
          </div>
        </div>

        <!-- Body -->
        <div class="bg-white dark:bg-gray-800 px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
          <!-- Stats Cards -->
          <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
              <div class="flex items-center">
                <div class="flex-shrink-0">
                  <Icon icon="mdi:link-variant" class="h-8 w-8 text-blue-600 dark:text-blue-400" />
                </div>
                <div class="ml-4">
                  <p class="text-sm font-medium text-gray-500 dark:text-gray-400">
                    {{ $t('Total Connections') }}
                  </p>
                  <p class="text-2xl font-semibold text-gray-900 dark:text-white">
                    {{ stats.total }}
                  </p>
                </div>
              </div>
            </div>

            <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
              <div class="flex items-center">
                <div class="flex-shrink-0">
                  <Icon icon="mdi:check-circle" class="h-8 w-8 text-green-600 dark:text-green-400" />
                </div>
                <div class="ml-4">
                  <p class="text-sm font-medium text-gray-500 dark:text-gray-400">
                    {{ $t('Active Connections') }}
                  </p>
                  <p class="text-2xl font-semibold text-gray-900 dark:text-white">
                    {{ stats.active }}
                  </p>
                </div>
              </div>
            </div>

            <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
              <div class="flex items-center">
                <div class="flex-shrink-0">
                  <Icon icon="mdi:heart-pulse" class="h-8 w-8 text-red-600 dark:text-red-400" />
                </div>
                <div class="ml-4">
                  <p class="text-sm font-medium text-gray-500 dark:text-gray-400">
                    {{ $t('Healthy Connections') }}
                  </p>
                  <p class="text-2xl font-semibold text-gray-900 dark:text-white">
                    {{ stats.healthy }}
                  </p>
                </div>
              </div>
            </div>
          </div>

          <!-- Search and Filter -->
          <div class="bg-white dark:bg-gray-800 shadow rounded-lg mb-6">
            <div class="p-4">
              <div class="flex flex-col sm:flex-row gap-4">
                <div class="flex-1">
                  <input
                    v-model="searchQuery"
                    type="text"
                    :placeholder="$t('Search connections...')"
                    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  />
                </div>
                <select
                  v-model="typeFilter"
                  class="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                >
                  <option value="">{{ $t('All Types') }}</option>
                  <option value="facebook">{{ $t('Facebook Messenger') }}</option>
                  <option value="webhook">{{ $t('Webhook') }}</option>
                  <option value="api">{{ $t('REST API') }}</option>
                  <option value="database">{{ $t('Database') }}</option>
                </select>
              </div>
            </div>
          </div>

          <!-- Connections List -->
          <div class="bg-white dark:bg-gray-800 shadow rounded-lg">
            <div class="px-4 py-5 sm:px-6">
              <h3 class="text-lg leading-6 font-medium text-gray-900 dark:text-white">
                {{ $t('Connections List') }}
              </h3>
            </div>
            <div class="border-t border-gray-200 dark:border-gray-700">
              <div v-if="loading" class="px-4 py-8 text-center">
                <div class="inline-flex items-center">
                  <Icon icon="mdi:loading" class="animate-spin h-5 w-5 mr-3" />
                  {{ $t('Loading...') }}
                </div>
              </div>
              
              <div v-else-if="filteredConnections.length === 0" class="px-4 py-8 text-center">
                <Icon icon="mdi:link-off" class="h-12 w-12 text-gray-400 mx-auto mb-4" />
                <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">
                  {{ $t('No Connections Yet') }}
                </h3>
                <p class="text-gray-500 dark:text-gray-400">
                  {{ $t('Create Your First Connection') }}
                </p>
                <div class="mt-6">
                  <button
                    @click="showCreateModal = true"
                    class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
                  >
                    <Icon icon="mdi:plus" class="mr-2" />
                    {{ $t('Create Connection') }}
                  </button>
                </div>
              </div>

              <div v-else class="divide-y divide-gray-200 dark:divide-gray-700">
                <div
                  v-for="connection in filteredConnections"
                  :key="connection.id"
                  class="px-4 py-4 sm:px-6 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
                >
                  <div class="flex items-center justify-between">
                    <div class="flex items-center">
                      <div class="flex-shrink-0 h-10 w-10">
                        <div class="h-10 w-10 rounded-full bg-blue-100 dark:bg-blue-900 flex items-center justify-center">
                          <Icon :icon="getConnectionIcon(connection.type)" class="h-6 w-6 text-blue-600 dark:text-blue-400" />
                        </div>
                      </div>
                      <div class="ml-4">
                        <div class="flex items-center">
                          <h4 class="text-lg font-medium text-gray-900 dark:text-white">
                            {{ connection.name }}
                          </h4>
                          <span
                            :class="[
                              'ml-2 inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
                              getConnectionStatusClass(connection.health)
                            ]"
                          >
                            {{ connection.health }}
                          </span>
                        </div>
                        <p class="text-sm text-gray-500 dark:text-gray-400">
                          {{ getConnectionTypeLabel(connection.type) }}
                        </p>
                        <div class="mt-2 flex items-center text-sm text-gray-500 dark:text-gray-400">
                          <Icon icon="mdi:clock" class="h-4 w-4 mr-1" />
                          {{ $t('Last Used') }}: {{ formatDate(connection.lastUsed) }}
                        </div>
                      </div>
                    </div>

                    <div class="flex items-center space-x-2">
                      <button
                        @click="testConnection(connection)"
                        class="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white dark:hover:bg-gray-600"
                      >
                        <Icon icon="mdi:test-tube" class="h-4 w-4 mr-1" />
                        {{ $t('Test Connection') }}
                      </button>
                      
                      <button
                        @click="checkHealth(connection)"
                        class="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white dark:hover:bg-gray-600"
                      >
                        <Icon icon="mdi:heart-pulse" class="h-4 w-4 mr-1" />
                        {{ $t('Check Health') }}
                      </button>

                      <div class="relative">
                        <button
                          @click.stop="toggleDropdown(connection.id)"
                          class="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white dark:hover:bg-gray-600"
                        >
                          <Icon icon="mdi:dots-vertical" class="h-4 w-4" />
                        </button>

                        <!-- Dropdown Menu -->
                        <div
                          v-if="dropdownOpen === connection.id"
                          class="absolute right-0 z-10 mt-2 w-48 rounded-md shadow-lg bg-white dark:bg-gray-800 ring-1 ring-black ring-opacity-5"
                        >
                          <div class="py-1">
                            <button
                              @click.stop="editConnection(connection)"
                              class="flex items-center w-full px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-gray-700"
                            >
                              <Icon icon="mdi:pencil" class="h-4 w-4 mr-2" />
                              {{ $t('Edit Connection') }}
                            </button>
                            <button
                              @click.stop="deleteConnection(connection)"
                              class="flex items-center w-full px-4 py-2 text-sm text-red-600 hover:bg-gray-100 dark:text-red-400 dark:hover:bg-gray-700"
                            >
                              <Icon icon="mdi:delete" class="h-4 w-4 mr-2" />
                              {{ $t('Delete Connection') }}
                            </button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Create/Edit Connection Modal -->
    <PennyConnectionModal
      v-if="showCreateModal"
      :connection="editingConnection"
      :bot-id="bot.id"
      :show="showCreateModal"
      @close="closeConnectionModal"
      @saved="handleConnectionSaved"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { Icon } from '@iconify/vue'
import PennyConnectionModal from './PennyConnectionModal.vue'

const { t } = useI18n()

const props = defineProps({
  bot: {
    type: Object,
    required: true
  },
  show: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close'])

// State
const connections = ref([])
const loading = ref(false)
const searchQuery = ref('')
const typeFilter = ref('')
const showCreateModal = ref(false)
const editingConnection = ref(null)
const dropdownOpen = ref(null)

// Mock data
const mockConnections = [
  {
    id: '1',
    name: 'Facebook Page - Customer Service',
    type: 'facebook',
    health: 'Healthy',
    lastUsed: new Date('2024-01-15T10:30:00')
  },
  {
    id: '2',
    name: 'Webhook - API Integration',
    type: 'webhook',
    health: 'Warning',
    lastUsed: new Date('2024-01-14T15:45:00')
  },
  {
    id: '3',
    name: 'REST API - External Service',
    type: 'api',
    health: 'Error',
    lastUsed: new Date('2024-01-13T09:20:00')
  }
]

// Computed
const stats = computed(() => ({
  total: connections.value.length,
  active: connections.value.filter(c => c.health === 'Healthy').length,
  healthy: connections.value.filter(c => c.health === 'Healthy').length
}))

const filteredConnections = computed(() => {
  let filtered = connections.value

  if (searchQuery.value) {
    filtered = filtered.filter(conn => 
      conn.name.toLowerCase().includes(searchQuery.value.toLowerCase())
    )
  }

  if (typeFilter.value) {
    filtered = filtered.filter(conn => conn.type === typeFilter.value)
  }

  return filtered
})

// Methods
const loadConnections = async () => {
  loading.value = true
  try {
    // TODO: Replace with actual API call
    // const response = await pennyApi.getConnections(props.bot.id)
    // connections.value = response.data
    
    // Mock data for now
    await new Promise(resolve => setTimeout(resolve, 1000))
    connections.value = mockConnections
  } catch (error) {
    console.error('Failed to load connections:', error)
  } finally {
    loading.value = false
  }
}

const getConnectionIcon = (type) => {
  const icons = {
    facebook: 'mdi:facebook-messenger',
    webhook: 'mdi:webhook',
    api: 'mdi:api',
    database: 'mdi:database'
  }
  return icons[type] || 'mdi:link-variant'
}

const getConnectionTypeLabel = (type) => {
  const labels = {
    facebook: t('Facebook Messenger'),
    webhook: t('Webhook'),
    api: t('REST API'),
    database: t('Database')
  }
  return labels[type] || type
}

const getConnectionStatusClass = (health) => {
  const classes = {
    Healthy: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
    Warning: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200',
    Error: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
  }
  return classes[health] || 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
}

const formatDate = (date) => {
  return new Intl.DateTimeFormat('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(date))
}

const toggleDropdown = (connectionId) => {
  dropdownOpen.value = dropdownOpen.value === connectionId ? null : connectionId
}

const closeDropdowns = () => {
  dropdownOpen.value = null
}

const testConnection = async (connection) => {
  try {
    // TODO: Replace with actual API call
    // await pennyApi.testConnection(connection.id)
    console.log('Testing connection:', connection.name)
  } catch (error) {
    console.error('Failed to test connection:', error)
  }
  closeDropdowns()
}

const checkHealth = async (connection) => {
  try {
    // TODO: Replace with actual API call
    // await pennyApi.checkConnectionHealth(connection.id)
    console.log('Checking health for:', connection.name)
  } catch (error) {
    console.error('Failed to check connection health:', error)
  }
  closeDropdowns()
}

const editConnection = (connection) => {
  editingConnection.value = connection
  showCreateModal.value = true
  closeDropdowns()
}

const closeConnectionModal = () => {
  showCreateModal.value = false
  editingConnection.value = null
}

const handleConnectionSaved = (savedConnection) => {
  if (editingConnection.value) {
    // Update existing connection
    const index = connections.value.findIndex(c => c.id === savedConnection.id)
    if (index !== -1) {
      connections.value[index] = savedConnection
    }
  } else {
    // Add new connection
    connections.value.push(savedConnection)
  }
  closeConnectionModal()
}

const deleteConnection = async (connection) => {
  if (confirm(`Are you sure you want to delete ${connection.name}?`)) {
    try {
      // TODO: Replace with actual API call
      // await pennyApi.deleteConnection(connection.id)
      connections.value = connections.value.filter(c => c.id !== connection.id)
    } catch (error) {
      console.error('Failed to delete connection:', error)
    }
  }
  closeDropdowns()
}

// Watch for show prop changes
watch(() => props.show, (newShow) => {
  if (newShow) {
    loadConnections()
  }
})

// Lifecycle
onMounted(() => {
  if (props.show) {
    loadConnections()
  }
  
  // Close dropdowns when clicking outside
  document.addEventListener('click', closeDropdowns)
})

// Watch for show prop changes
watch(() => props.show, (newShow) => {
  if (newShow) {
    loadConnections()
  }
})
</script>

<style scoped>
.animate-spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
