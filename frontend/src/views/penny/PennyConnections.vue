<template>
  <div class="penny-connections">
    <!-- Header -->
    <div class="bg-white dark:bg-gray-800 shadow rounded-lg mb-6">
      <div class="px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-2xl font-bold text-gray-900 dark:text-white">
              {{ $t('Penny Connections') }}
            </h1>
            <p class="text-gray-600 dark:text-gray-400 mt-1">
              {{ $t('Manage your bot connections') }}
              <span v-if="currentBot" class="ml-2 text-blue-600 dark:text-blue-400">
                - {{ currentBot.botName }}
              </span>
            </p>
          </div>
          <div class="flex items-center space-x-4">
            <!-- Bot Selection -->
            <div v-if="availableBots && availableBots.length > 0" class="flex items-center space-x-2">
              <label class="text-sm font-medium text-gray-700 dark:text-gray-300">
                {{ $t('Select Bot:') }}
              </label>
              <select
                v-model="selectedBot"
                @change="selectBot(selectedBot)"
                class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-800 text-gray-900 dark:text-white"
              >
                <option value="" disabled>{{ $t('Select a bot') }}</option>
                <option
                  v-for="bot in availableBots"
                  :key="bot.id"
                  :value="bot"
                >
                  {{ bot.botName }} - {{ getBotTypeDisplayName(bot.botType) }}
                </option>
              </select>
              <Icon icon="mdi:link-variant" class="h-6 w-6 text-green-600 dark:text-green-400" />
            </div>
            
            <!-- AutoConnect Button -->
            <button
              v-if="selectedBot"
              @click="showAutoConnectModal = true"
              class="inline-flex items-center px-4 py-2 bg-green-600 text-white text-sm font-medium rounded-md hover:bg-green-700 transition-colors"
            >
              <Icon icon="mdi:facebook" class="mr-2" />
              {{ $t('Auto Connect') }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Stats Cards -->
    <div v-if="selectedBot" class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
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
              {{ connections?.length || 0 }}
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
              {{ activeConnections?.length || 0 }}
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
              {{ healthyConnections?.length || 0 }}
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- No Bot Selected -->
    <div v-if="!selectedBot" class="bg-white dark:bg-gray-800 shadow rounded-lg">
      <div class="px-4 py-8 text-center">
        <Icon icon="mdi:robot-off" class="h-16 w-16 text-gray-400 mx-auto mb-4" />
        <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">
          {{ $t('Select a Bot to View Connections') }}
        </h3>
        <p class="text-gray-500 dark:text-gray-400">
          {{ $t('Please select a bot from the dropdown above to view and manage its connections.') }}
        </p>
      </div>
    </div>

    <!-- Connections List -->
    <div v-else class="bg-white dark:bg-gray-800 shadow rounded-lg">
      <div class="px-4 py-5 sm:px-6">
        <h3 class="text-lg leading-6 font-medium text-gray-900 dark:text-white">
          {{ $t('Connections List') }}
        </h3>
      </div>
      <div class="border-t border-gray-200 dark:border-gray-700">
        <div v-if="loadingConnections" class="px-4 py-8 text-center">
          <div class="inline-flex items-center">
            <Icon icon="mdi:loading" class="animate-spin h-5 w-5 mr-3" />
            {{ $t('Loading...') }}
          </div>
        </div>
        
        <div v-else-if="filteredConnections && filteredConnections.length === 0" class="px-4 py-8 text-center">
          <Icon icon="mdi:link-off" class="h-12 w-12 text-gray-400 mx-auto mb-4" />
          <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">
            {{ $t('No Facebook Connections Yet') }}
          </h3>
          <p class="text-gray-500 dark:text-gray-400">
            {{ $t('Use the Auto Connect button to automatically connect your Facebook pages') }}
          </p>
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
                    <Icon :icon="getConnectionIcon(connection.platform)" class="h-6 w-6 text-blue-600 dark:text-blue-400" />
                  </div>
                </div>
                <div class="ml-4">
                  <div class="flex items-center">
                    <h4 class="text-lg font-medium text-gray-900 dark:text-white">
                      {{ connection.name }}
                    </h4>
                    <span
                      :class="[
                        'ml-2 inline-flex px-2 py-1 text-xs font-semibold rounded-full',
                        connection.isActive ? 'bg-green-100 text-green-800 dark:bg-green-800 dark:text-green-100' : 'bg-red-100 text-red-800 dark:bg-red-800 dark:text-red-100'
                      ]"
                    >
                      {{ connection.isActive ? $t('Active') : $t('Inactive') }}
                    </span>
                  </div>
                  <p class="text-sm text-gray-500 dark:text-gray-400">
                    {{ connection.description || $t('No description available') }}
                  </p>
                  <div class="flex items-center mt-1 space-x-4 text-sm text-gray-500 dark:text-gray-400">
                    <span>
                      <Icon icon="mdi:calendar" class="h-4 w-4 mr-1" />
                      {{ formatDate(connection.createdAt) }}
                    </span>
                    <span v-if="connection.lastUsedAt">
                      <Icon icon="mdi:clock" class="h-4 w-4 mr-1" />
                      {{ formatDateTime(connection.lastUsedAt) }}
                    </span>
                  </div>
                </div>
              </div>
              
              <div class="flex items-center space-x-2">
                <button
                  @click="editConnection(connection)"
                  class="text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 text-sm font-medium"
                >
                  {{ $t('Edit') }}
                </button>
                <button
                  @click="toggleConnectionStatus(connection)"
                  :class="[
                    'text-sm font-medium',
                    connection.isActive 
                      ? 'text-red-600 dark:text-red-400 hover:text-red-700 dark:hover:text-red-300' 
                      : 'text-green-600 dark:text-green-400 hover:text-green-700 dark:hover:text-green-300'
                  ]"
                >
                  {{ connection.isActive ? $t('Disable') : $t('Enable') }}
                </button>
                <button
                  @click="deleteConnection(connection)"
                  class="text-red-600 dark:text-red-400 hover:text-red-700 dark:hover:text-red-300 text-sm font-medium"
                >
                  {{ $t('Delete') }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Connection Modal -->
    <PennyConnectionModal
      v-if="showCreateModal || editingConnection"
      :connection="editingConnection"
      :bot="selectedBot"
      @close="closeModal"
      @saved="onConnectionSaved"
    />

    <!-- AutoConnect Modal -->
    <PennyAutoConnectModal
      v-if="showAutoConnectModal"
      :bot="selectedBot"
      @close="closeAutoConnectModal"
      @connected="onAutoConnectCompleted"
    />
  </div>
</template>

<script>
import { computed, ref, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import { useI18n } from 'vue-i18n'
import { formatDate, formatDateTime } from '@/utils/dateUtils'
import { usePennyConnectionStore } from '@/stores/pennyConnectionStore'
import { usePennyBotStore } from '@/stores/pennyBotStore'
import PennyConnectionModal from './components/PennyConnectionModal.vue'
import PennyAutoConnectModal from './components/PennyAutoConnectModal.vue'

export default {
  name: 'PennyConnections',
  components: {
    Icon,
    PennyConnectionModal,
    PennyAutoConnectModal
  },
  setup() {
    const { t } = useI18n()
    const pennyConnectionStore = usePennyConnectionStore()
    const pennyBotStore = usePennyBotStore()

    // Computed
    const connections = computed(() => pennyConnectionStore.connections)
    const activeConnections = computed(() => 
      connections.value.filter(conn => conn.isActive)
    )
    const healthyConnections = computed(() => 
      connections.value.filter(conn => conn.isActive && conn.isHealthy)
    )
    const loadingConnections = computed(() => pennyConnectionStore.loadingConnections)
    const updatingConnection = computed(() => pennyConnectionStore.updatingConnection)
    const deletingConnection = computed(() => pennyConnectionStore.deletingConnection)
    const availableBots = computed(() => pennyBotStore.pennyBots)
    const currentBot = computed(() => pennyBotStore.currentBot)
    const currentBotId = computed(() => pennyBotStore.currentBotId)

    // Since we only have Facebook connections, no need for filtering
    const filteredConnections = computed(() => connections.value)

    // Modal states
    const showCreateModal = ref(false)
    const showAutoConnectModal = ref(false)
    const editingConnection = ref(null)
    const selectedBot = ref(null)

    // Methods
    const fetchConnections = async () => {
      if (!selectedBot.value) {
        console.warn('No bot selected for fetching connections')
        return
      }
      
      try {
        await pennyConnectionStore.fetchConnections(selectedBot.value.id)
      } catch (error) {
        console.error('Failed to fetch connections:', error)
      }
    }

    const selectBot = (bot) => {
      selectedBot.value = bot
      fetchConnections()
    }

    const toggleConnectionStatus = async (connection) => {
      try {
        await pennyConnectionStore.toggleConnectionStatus(connection.id, !connection.isActive)
      } catch (error) {
        console.error('Failed to toggle connection status:', error)
      }
    }

    const editConnection = (connection) => {
      editingConnection.value = { ...connection }
    }

    const deleteConnection = async (connection) => {
      if (confirm(`Are you sure you want to delete "${connection.name}"?`)) {
        try {
          await pennyConnectionStore.deleteConnection(connection.id)
        } catch (error) {
          console.error('Failed to delete connection:', error)
        }
      }
    }

    const testConnection = async (connection) => {
      try {
        await pennyConnectionStore.testConnection(connection.id)
        alert('Connection test successful!')
      } catch (error) {
        console.error('Connection test failed:', error)
        alert('Connection test failed: ' + error.message)
      }
    }

    const closeModal = () => {
      showCreateModal.value = false
      editingConnection.value = null
    }

    const onConnectionSaved = () => {
      closeModal()
      fetchConnections()
    }

    const closeAutoConnectModal = () => {
      showAutoConnectModal.value = false
    }

    const onAutoConnectCompleted = (results) => {
      closeAutoConnectModal()
      fetchConnections()
    }

    const getConnectionIcon = (platform) => {
      const icons = {
        'facebook': 'mdi:facebook',
        'zalo': 'mdi:message-text',
        'website': 'mdi:web',
        'api': 'mdi:api'
      }
      return icons[platform] || 'mdi:link'
    }

    const getBotTypeDisplayName = (botType) => {
      const names = {
        'CUSTOMER_SERVICE': 'Customer Service',
        'SALES': 'Sales',
        'SUPPORT': 'Support',
        'MARKETING': 'Marketing',
        'HR': 'HR',
        'FINANCE': 'Finance',
        'GENERAL': 'General'
      }
      return names[botType] || botType
    }

    // Lifecycle
    onMounted(() => {
      // Load available bots first
      if (!availableBots.value || availableBots.value.length === 0) {
        pennyBotStore.fetchPennyBots().then(() => {
          // Auto-select current bot if available
          if (currentBot.value) {
            selectedBot.value = currentBot.value
            fetchConnections()
          }
        })
      } else {
        // Auto-select current bot if available
        if (currentBot.value) {
          selectedBot.value = currentBot.value
          fetchConnections()
        }
      }
    })

    return {
      // Store data
      connections,
      activeConnections,
      healthyConnections,
      loadingConnections,
      updatingConnection,
      deletingConnection,
      availableBots,
      currentBot,
      currentBotId,
      filteredConnections,
      showCreateModal,
      showAutoConnectModal,
      editingConnection,
      selectedBot,

      // Methods
      fetchConnections,
      selectBot,
      toggleConnectionStatus,
      editConnection,
      deleteConnection,
      testConnection,
      closeModal,
      onConnectionSaved,
      closeAutoConnectModal,
      onAutoConnectCompleted,
      getConnectionIcon,
      formatDate,
      formatDateTime,
      getBotTypeDisplayName
    }
  }
}
</script>

<style scoped>
  /* ... */
.penny-connections {
  width: 100%;
  padding: 20px;
}

.connections-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  margin-top: 10px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}

.connection-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  background: #3b82f6;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.connection-avatar.is-inactive {
  filter: grayscale(1);
  opacity: 0.6;
}

.avatar-content {
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-main {
  flex: 1;
  overflow: hidden;
}

.connection-name {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dark .connection-name {
  color: white;
}

.card-content {
  background-color: #f9fafb;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 20px;
}

.dark .card-content {
  background-color: #374151;
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  font-size: 13px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.label {
  color: #6b7280;
  font-weight: 500;
}

.dark .label {
  color: #9ca3af;
}

.value {
  color: #374151;
  font-weight: 500;
}

.dark .value {
  color: #d1d5db;
}

.text-truncate {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-footer {
  border-top: 1px solid #f3f4f6;
  padding-top: 15px;
}

.dark .card-footer {
  border-top-color: #374151;
}

.action-buttons {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.skeleton-card {
  background: white;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 20px;
}

.dark .skeleton-card {
  background: #1f2937;
  border-color: #374151;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
}

.loading-state {
  margin-top: 20px;
}
</style>
