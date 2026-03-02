<template>
  <div class="penny-connections-modal-backdrop" @click="closeOnBackdrop">
    <div class="penny-connections-modal" @click.stop>
      <div class="modal-header">
        <div class="header-content">
          <div class="connections-info">
            <Icon icon="mdi:connection" class="h-6 w-6 mr-3" />
            <div>
              <h2 class="modal-title">{{ $t('Connections Management') }}</h2>
              <p class="bot-name">{{ bot?.botName }}</p>
            </div>
          </div>
          <div class="header-actions">
            <button
              @click="createNewConnection"
              class="create-connection-btn"
            >
              <Icon icon="mdi:plus" class="h-4 w-4 mr-2" />
              {{ $t('Create Connection') }}
            </button>
            <button @click="$emit('close')" class="close-button">
              <Icon icon="mdi:close" class="h-5 w-5" />
            </button>
          </div>
        </div>
      </div>

      <div class="modal-body">
        <!-- Connections List -->
        <div v-if="loadingConnections" class="loading-connections">
          <div class="animate-pulse">
            <div v-for="i in 3" :key="i" class="connection-skeleton">
              <div class="skeleton-header">
                <div class="skeleton-line long"></div>
                <div class="skeleton-line short"></div>
              </div>
              <div class="skeleton-content">
                <div class="skeleton-line"></div>
                <div class="skeleton-line medium"></div>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="connections.length === 0" class="no-connections">
          <Icon icon="mdi:connection" class="h-16 w-16 text-gray-400 mb-4" />
          <h3 class="no-connections-title">{{ $t('No Connections Yet') }}</h3>
          <p class="no-connections-text">{{ $t('Create your first connection to get started') }}</p>
          <button @click="createNewConnection" class="create-first-connection-btn">
            <Icon icon="mdi:plus" class="h-5 w-5 mr-2" />
            {{ $t('Create Your First Connection') }}
          </button>
        </div>

        <div v-else class="connections-list">
          <!-- Statistics Cards -->
          <div class="stats-cards">
            <div class="stat-card">
              <div class="stat-icon">
                <Icon icon="mdi:connection" />
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ connections.length }}</div>
                <div class="stat-label">{{ $t('Total Connections') }}</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon active">
                <Icon icon="mdi:check-circle" />
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ activeConnections.length }}</div>
                <div class="stat-label">{{ $t('Active Connections') }}</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon healthy">
                <Icon icon="mdi:heart-pulse" />
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ healthyConnections.length }}</div>
                <div class="stat-label">{{ $t('Healthy Connections') }}</div>
              </div>
            </div>
          </div>

          <!-- Connections Table -->
          <div class="connections-table">
            <div class="table-header">
              <h3 class="table-title">{{ $t('Connections List') }}</h3>
              <div class="table-actions">
                <select v-model="filterType" class="filter-select">
                  <option value="">{{ $t('All Types') }}</option>
                  <option value="FACEBOOK">{{ $t('Facebook Messenger') }}</option>
                  <option value="WEBHOOK">{{ $t('Webhook') }}</option>
                  <option value="API">{{ $t('REST API') }}</option>
                  <option value="DATABASE">{{ $t('Database') }}</option>
                </select>
                <button @click="refreshConnections" class="refresh-btn">
                  <Icon icon="mdi:refresh" class="h-4 w-4" />
                </button>
              </div>
            </div>

            <div class="table-container">
              <table class="connections-table-content">
                <thead>
                  <tr>
                    <th>{{ $t('Name') }}</th>
                    <th>{{ $t('Type') }}</th>
                    <th>{{ $t('Status') }}</th>
                    <th>{{ $t('Health') }}</th>
                    <th>{{ $t('Priority') }}</th>
                    <th>{{ $t('Last Used') }}</th>
                    <th>{{ $t('Actions') }}</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="connection in filteredConnections"
                    :key="connection.id"
                    :class="{ 'connection-inactive': !connection.isEnabled }"
                  >
                    <td class="connection-name-cell">
                      <div class="connection-name">
                        {{ connection.connectionName }}
                      </div>
                      <div class="connection-description" v-if="connection.description">
                        {{ connection.description }}
                      </div>
                    </td>
                    <td>
                      <span class="connection-type-badge" :class="connection.connectionType.toLowerCase()">
                        {{ getConnectionTypeDisplayName(connection.connectionType) }}
                      </span>
                    </td>
                    <td>
                      <div class="status-toggle">
                        <label class="switch">
                          <input
                            type="checkbox"
                            v-model="connection.isEnabled"
                            @change="toggleConnectionStatus(connection)"
                            class="switch-input"
                          />
                          <span class="switch-slider"></span>
                        </label>
                        <span class="status-text" :class="{ active: connection.isEnabled }">
                          {{ connection.isEnabled ? $t('Enabled') : $t('Disabled') }}
                        </span>
                      </div>
                    </td>
                    <td>
                      <div class="health-indicator">
                        <div
                          class="health-dot"
                          :class="getHealthClass(connection.healthStatus)"
                        ></div>
                        <span class="health-text">{{ getHealthText(connection.healthStatus) }}</span>
                      </div>
                    </td>
                    <td>
                      <span class="priority-badge" :class="getPriorityClass(connection.priority)">
                        {{ connection.priority }}
                      </span>
                    </td>
                    <td>
                      <span class="last-used">
                        {{ formatDateTime(connection.lastUsedAt) }}
                      </span>
                    </td>
                    <td class="actions-cell">
                      <div class="action-buttons">
                        <button
                          @click="testConnection(connection)"
                          :disabled="testingConnection"
                          class="action-btn test"
                          :title="$t('Test Connection')"
                        >
                          <Icon icon="mdi:test-tube" class="h-4 w-4" />
                        </button>
                        <button
                          @click="checkHealth(connection)"
                          :disabled="checkingHealth"
                          class="action-btn health"
                          :title="$t('Check Health')"
                        >
                          <Icon icon="mdi:heart-pulse" class="h-4 w-4" />
                        </button>
                        <button
                          @click="editConnection(connection)"
                          class="action-btn edit"
                          :title="$t('Edit Connection')"
                        >
                          <Icon icon="mdi:pencil" class="h-4 w-4" />
                        </button>
                        <button
                          @click="deleteConnectionConfirm(connection)"
                          class="action-btn delete"
                          :title="$t('Delete Connection')"
                        >
                          <Icon icon="mdi:delete" class="h-4 w-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- Connection Modal -->
  <PennyConnectionModal
    v-if="showConnectionModal"
    :bot="bot"
    :connection="editingConnection"
    @close="showConnectionModal = false"
    @saved="onConnectionSaved"
  />
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { useI18n } from 'vue-i18n'
import { formatDateTime } from '@/utils/dateUtils'
import { usePennyConnectionStore } from '@/stores/pennyConnectionStore'
import PennyConnectionModal from './PennyConnectionModal.vue'

export default {
  name: 'PennyConnectionsModal',
  components: {
    Icon,
    PennyConnectionModal
  },
  props: {
    bot: {
      type: Object,
      required: true
    }
  },
  emits: ['close'],
  setup(props, { emit }) {
    const { t } = useI18n()
    const pennyConnectionStore = usePennyConnectionStore()

    // State
    const showConnectionModal = ref(false)
    const editingConnection = ref(null)
    const filterType = ref('')
    const testingConnection = ref(false)
    const checkingHealth = ref(false)

    // Computed
    const connections = computed(() => pennyConnectionStore.connections)
    const loadingConnections = computed(() => pennyConnectionStore.loadingConnections)
    const activeConnections = computed(() => pennyConnectionStore.activeConnections)
    const healthyConnections = computed(() => pennyConnectionStore.healthyConnections)

    const filteredConnections = computed(() => {
      if (!filterType.value) return connections.value
      return connections.value.filter(conn => conn.connectionType === filterType.value)
    })

    // Methods
    const fetchConnections = async () => {
      try {
        await pennyConnectionStore.fetchConnections(props.bot.id)
      } catch (error) {
        console.error('Failed to fetch connections:', error)
      }
    }

    const refreshConnections = () => {
      fetchConnections()
    }

    const createNewConnection = () => {
      editingConnection.value = null
      showConnectionModal.value = true
    }

    const editConnection = (connection) => {
      editingConnection.value = connection
      showConnectionModal.value = true
    }

    const deleteConnectionConfirm = (connection) => {
      if (confirm(`Are you sure you want to delete connection "${connection.connectionName}"?`)) {
        deleteConnection(connection)
      }
    }

    const deleteConnection = async (connection) => {
      try {
        await pennyConnectionStore.deleteConnection(props.bot.id, connection.id)
        console.log('Connection deleted successfully')
      } catch (error) {
        console.error('Failed to delete connection:', error)
        alert('Failed to delete connection: ' + error.message)
      }
    }

    const toggleConnectionStatus = async (connection) => {
      try {
        await pennyConnectionStore.toggleConnectionStatus(props.bot.id, connection.id)
      } catch (error) {
        console.error('Failed to toggle connection status:', error)
        alert('Failed to update connection status: ' + error.message)
      }
    }

    const testConnection = async (connection) => {
      testingConnection.value = true
      try {
        const testData = {
          connectionType: connection.connectionType,
          config: getConnectionConfig(connection)
        }

        const result = await pennyConnectionStore.testConnection(props.bot.id, connection.id, testData)
        
        if (result.success) {
          alert(`✅ Connection test successful!\n\nConnection: ${connection.connectionName}\nResponse: ${result.message}`)
        } else {
          alert(`❌ Connection test failed.\n\nError: ${result.error}`)
        }
      } catch (error) {
        console.error('Failed to test connection:', error)
        alert('Failed to test connection: ' + (error.response?.data?.error || error.message))
      } finally {
        testingConnection.value = false
      }
    }

    const checkHealth = async (connection) => {
      checkingHealth.value = true
      try {
        await pennyConnectionStore.fetchConnectionHealth(props.bot.id, connection.id)
        console.log('Health check completed for connection:', connection.connectionName)
      } catch (error) {
        console.error('Failed to check connection health:', error)
        alert('Failed to check connection health: ' + error.message)
      } finally {
        checkingHealth.value = false
      }
    }

    const getConnectionConfig = (connection) => {
      // Return connection-specific config for testing
      return {
        pageId: connection.pageId,
        webhookUrl: connection.webhookUrl,
        apiUrl: connection.apiUrl,
        dbHost: connection.dbHost
      }
    }

    const getConnectionTypeDisplayName = (type) => {
      const names = {
        'FACEBOOK': 'Facebook',
        'WEBHOOK': 'Webhook',
        'API': 'API',
        'DATABASE': 'Database'
      }
      return names[type] || type
    }

    const getHealthClass = (status) => {
      switch (status) {
        case 'HEALTHY':
          return 'healthy'
        case 'WARNING':
          return 'warning'
        case 'ERROR':
          return 'error'
        default:
          return 'unknown'
      }
    }

    const getHealthText = (status) => {
      switch (status) {
        case 'HEALTHY':
          return t('Healthy')
        case 'WARNING':
          return t('Warning')
        case 'ERROR':
          return t('Error')
        default:
          return t('Unknown')
      }
    }

    const getPriorityClass = (priority) => {
      if (priority >= 80) return 'high'
      if (priority >= 50) return 'medium'
      return 'low'
    }

    const closeOnBackdrop = (event) => {
      if (event.target === event.currentTarget) {
        emit('close')
      }
    }

    const onConnectionSaved = () => {
      showConnectionModal.value = false
      editingConnection.value = null
      fetchConnections() // Refresh connections list
    }

    // Lifecycle
    onMounted(() => {
      fetchConnections()
    })

    return {
      // State
      showConnectionModal,
      editingConnection,
      filterType,
      testingConnection,
      checkingHealth,

      // Computed
      connections,
      loadingConnections,
      activeConnections,
      healthyConnections,
      filteredConnections,

      // Methods
      fetchConnections,
      refreshConnections,
      createNewConnection,
      editConnection,
      deleteConnectionConfirm,
      deleteConnection,
      toggleConnectionStatus,
      testConnection,
      checkHealth,
      getConnectionConfig,
      getConnectionTypeDisplayName,
      getHealthClass,
      getHealthText,
      getPriorityClass,
      formatDateTime,
      closeOnBackdrop,
      onConnectionSaved
    }
  }
}
</script>

<style scoped>
.penny-connections-modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  z-index: 1000;
  padding: 40px 20px 20px;
}

.penny-connections-modal {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  max-width: 1200px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  margin-top: 20px;
}

.dark .penny-connections-modal {
  background: #1f2937;
}

.modal-header {
  padding: 24px;
  border-bottom: 1px solid #e5e7eb;
}

.dark .modal-header {
  border-bottom-color: #374151;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.connections-info {
  display: flex;
  align-items: center;
  flex: 1;
}

.modal-title {
  margin: 0 0 4px 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.dark .modal-title {
  color: white;
}

.bot-name {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
}

.dark .bot-name {
  color: #9ca3af;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.create-connection-btn {
  padding: 8px 16px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
}

.create-connection-btn:hover {
  background: #2563eb;
}

.close-button {
  background: none;
  border: none;
  padding: 8px;
  border-radius: 6px;
  cursor: pointer;
  color: #6b7280;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.close-button:hover {
  background-color: #f3f4f6;
  color: #1f2937;
}

.dark .close-button {
  color: #9ca3af;
}

.dark .close-button:hover {
  background-color: #374151;
  color: white;
}

.modal-body {
  padding: 24px;
}

.loading-connections {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.connection-skeleton {
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.skeleton-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.skeleton-line {
  height: 16px;
  background: #e5e7eb;
  border-radius: 4px;
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.skeleton-line.long {
  width: 60%;
}

.skeleton-line.short {
  width: 30%;
}

.skeleton-line.medium {
  width: 80%;
}

.skeleton-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.no-connections {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.no-connections-title {
  margin: 16px 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #374151;
}

.dark .no-connections-title {
  color: #d1d5db;
}

.no-connections-text {
  margin: 0 0 24px 0;
  color: #6b7280;
}

.dark .no-connections-text {
  color: #9ca3af;
}

.create-first-connection-btn {
  padding: 12px 24px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
}

.create-first-connection-btn:hover {
  background: #2563eb;
}

.connections-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 16px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.dark .stat-card {
  background: #374151;
  border-color: #4b5563;
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #3b82f6;
  color: white;
  margin-right: 12px;
}

.stat-icon.active {
  background: #10b981;
}

.stat-icon.healthy {
  background: #ef4444;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.dark .stat-number {
  color: white;
}

.stat-label {
  font-size: 12px;
  color: #6b7280;
}

.dark .stat-label {
  color: #9ca3af;
}

.connections-table {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.dark .connections-table {
  background: #1f2937;
  border-color: #374151;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
}

.dark .table-header {
  border-bottom-color: #374151;
}

.table-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.dark .table-title {
  color: white;
}

.table-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-select {
  padding: 6px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  background: white;
  color: #1f2937;
}

.dark .filter-select {
  background: #374151;
  border-color: #4b5563;
  color: white;
}

.refresh-btn {
  padding: 8px;
  background: none;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  cursor: pointer;
  color: #6b7280;
  transition: all 0.2s;
}

.refresh-btn:hover {
  background: #f3f4f6;
  color: #1f2937;
}

.dark .refresh-btn {
  border-color: #4b5563;
  color: #9ca3af;
}

.dark .refresh-btn:hover {
  background: #374151;
  color: white;
}

.table-container {
  overflow-x: auto;
}

.connections-table-content {
  width: 100%;
  border-collapse: collapse;
}

.connections-table-content th {
  padding: 12px 16px;
  text-align: left;
  font-weight: 600;
  color: #374151;
  border-bottom: 1px solid #e5e7eb;
  font-size: 14px;
}

.dark .connections-table-content th {
  color: #d1d5db;
  border-bottom-color: #374151;
}

.connections-table-content td {
  padding: 12px 16px;
  border-bottom: 1px solid #e5e7eb;
  font-size: 14px;
  vertical-align: middle;
}

.dark .connections-table-content td {
  border-bottom-color: #374151;
  color: #d1d5db;
}

.connection-inactive {
  opacity: 0.6;
}

.connection-name-cell {
  max-width: 200px;
}

.connection-name {
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 4px;
}

.dark .connection-name {
  color: white;
}

.connection-description {
  font-size: 12px;
  color: #6b7280;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dark .connection-description {
  color: #9ca3af;
}

.connection-type-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  text-transform: uppercase;
}

.connection-type-badge.facebook {
  background: #1877f2;
  color: white;
}

.connection-type-badge.webhook {
  background: #10b981;
  color: white;
}

.connection-type-badge.api {
  background: #f59e0b;
  color: white;
}

.connection-type-badge.database {
  background: #8b5cf6;
  color: white;
}

.status-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
}

.switch {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 24px;
}

.switch-input {
  opacity: 0;
  width: 0;
  height: 0;
}

.switch-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: 0.4s;
  border-radius: 24px;
}

.switch-slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: 0.4s;
  border-radius: 50%;
}

.switch-input:checked + .switch-slider {
  background-color: #3b82f6;
}

.switch-input:checked + .switch-slider:before {
  transform: translateX(20px);
}

.status-text {
  font-size: 12px;
  font-weight: 500;
}

.status-text.active {
  color: #10b981;
}

.status-text:not(.active) {
  color: #6b7280;
}

.health-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
}

.health-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.health-dot.healthy {
  background: #10b981;
}

.health-dot.warning {
  background: #f59e0b;
}

.health-dot.error {
  background: #ef4444;
}

.health-dot.unknown {
  background: #6b7280;
}

.health-text {
  font-size: 12px;
  font-weight: 500;
  color: #374151;
}

.dark .health-text {
  color: #d1d5db;
}

.priority-badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.priority-badge.high {
  background: #fecaca;
  color: #dc2626;
}

.priority-badge.medium {
  background: #fed7aa;
  color: #d97706;
}

.priority-badge.low {
  background: #dbeafe;
  color: #2563eb;
}

.last-used {
  font-size: 12px;
  color: #6b7280;
}

.dark .last-used {
  color: #9ca3af;
}

.actions-cell {
  padding: 8px 16px;
}

.action-buttons {
  display: flex;
  gap: 4px;
}

.action-btn {
  padding: 6px;
  background: none;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-btn:hover {
  background: #f3f4f6;
}

.dark .action-btn {
  border-color: #4b5563;
  color: #9ca3af;
}

.dark .action-btn:hover {
  background: #374151;
}

.action-btn.test:hover {
  border-color: #3b82f6;
  color: #3b82f6;
}

.action-btn.health:hover {
  border-color: #10b981;
  color: #10b981;
}

.action-btn.edit:hover {
  border-color: #f59e0b;
  color: #f59e0b;
}

.action-btn.delete:hover {
  border-color: #ef4444;
  color: #ef4444;
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
