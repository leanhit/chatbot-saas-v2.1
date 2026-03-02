import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { pennyConnectionApi } from '@/api/pennyConnectionApi'

export const usePennyConnectionStore = defineStore('penny-connection', () => {
  // State
  const connections = ref([])
  const selectedConnection = ref(null)
  const loadingConnections = ref(false)
  const creatingConnection = ref(false)
  const updatingConnection = ref(false)
  const deletingConnection = ref(false)
  const testingConnection = ref(false)
  const statistics = ref(null)

  // Getters
  const activeConnections = computed(() => connections.value.filter(conn => conn.isEnabled))
  const connectionsByType = computed(() => {
    const grouped = {}
    connections.value.forEach(conn => {
      if (!grouped[conn.connectionType]) {
        grouped[conn.connectionType] = []
      }
      grouped[conn.connectionType].push(conn)
    })
    return grouped
  })

  const healthyConnections = computed(() => {
    return connections.value.filter(conn => conn.healthStatus === 'HEALTHY')
  })

  // Actions
  const fetchConnections = async (botId) => {
    loadingConnections.value = true
    try {
      const response = await pennyConnectionApi.getConnections(botId)
      connections.value = response.data
      return response.data
    } catch (error) {
      console.error('Failed to fetch connections:', error)
      throw error
    } finally {
      loadingConnections.value = false
    }
  }

  const fetchConnectionsByType = async (botId, connectionType) => {
    loadingConnections.value = true
    try {
      const response = await pennyConnectionApi.getConnectionsByType(botId, connectionType)
      return response.data
    } catch (error) {
      console.error('Failed to fetch connections by type:', error)
      throw error
    } finally {
      loadingConnections.value = false
    }
  }

  const createConnection = async (botId, connectionData) => {
    creatingConnection.value = true
    try {
      const response = await pennyConnectionApi.createConnection(botId, connectionData)
      // Refresh connections list
      await fetchConnections(botId)
      return response.data
    } catch (error) {
      console.error('Failed to create connection:', error)
      throw error
    } finally {
      creatingConnection.value = false
    }
  }

  const updateConnection = async (botId, connectionId, connectionData) => {
    updatingConnection.value = true
    try {
      const response = await pennyConnectionApi.updateConnection(botId, connectionId, connectionData)
      // Refresh connections list
      await fetchConnections(botId)
      return response.data
    } catch (error) {
      console.error('Failed to update connection:', error)
      throw error
    } finally {
      updatingConnection.value = false
    }
  }

  const deleteConnection = async (botId, connectionId) => {
    deletingConnection.value = true
    try {
      const response = await pennyConnectionApi.deleteConnection(botId, connectionId)
      // Remove from local state
      connections.value = connections.value.filter(conn => conn.id !== connectionId)
      return response.data
    } catch (error) {
      console.error('Failed to delete connection:', error)
      throw error
    } finally {
      deletingConnection.value = false
    }
  }

  const testConnection = async (botId, connectionId, testData) => {
    testingConnection.value = true
    try {
      const response = await pennyConnectionApi.testConnection(botId, connectionId, testData)
      return response.data
    } catch (error) {
      console.error('Failed to test connection:', error)
      throw error
    } finally {
      testingConnection.value = false
    }
  }

  const toggleConnectionStatus = async (botId, connectionId) => {
    try {
      const response = await pennyConnectionApi.toggleConnectionStatus(botId, connectionId)
      // Update local state
      const connection = connections.value.find(conn => conn.id === connectionId)
      if (connection) {
        connection.isEnabled = !connection.isEnabled
      }
      return response.data
    } catch (error) {
      console.error('Failed to toggle connection status:', error)
      // Revert change
      const connection = connections.value.find(conn => conn.id === connectionId)
      if (connection) {
        connection.isEnabled = !connection.isEnabled
      }
      throw error
    }
  }

  const fetchStatistics = async (botId) => {
    try {
      const response = await pennyConnectionApi.getConnectionStatistics(botId)
      statistics.value = response.data
      return response.data
    } catch (error) {
      console.error('Failed to fetch connection statistics:', error)
      throw error
    }
  }

  const fetchConnectionHealth = async (botId, connectionId) => {
    try {
      const response = await pennyConnectionApi.getConnectionHealth(botId, connectionId)
      // Update local state
      const connection = connections.value.find(conn => conn.id === connectionId)
      if (connection) {
        connection.healthStatus = response.data.status
        connection.lastHealthCheck = response.data.timestamp
      }
      return response.data
    } catch (error) {
      console.error('Failed to fetch connection health:', error)
      throw error
    }
  }

  const setSelectedConnection = (connection) => {
    selectedConnection.value = connection
  }

  const clearSelectedConnection = () => {
    selectedConnection.value = null
  }

  const resetStore = () => {
    connections.value = []
    selectedConnection.value = null
    loadingConnections.value = false
    creatingConnection.value = false
    updatingConnection.value = false
    deletingConnection.value = false
    testingConnection.value = false
    statistics.value = null
  }

  return {
    // State
    connections,
    selectedConnection,
    loadingConnections,
    creatingConnection,
    updatingConnection,
    deletingConnection,
    testingConnection,
    statistics,

    // Getters
    activeConnections,
    connectionsByType,
    healthyConnections,

    // Actions
    fetchConnections,
    fetchConnectionsByType,
    createConnection,
    updateConnection,
    deleteConnection,
    testConnection,
    toggleConnectionStatus,
    fetchStatistics,
    fetchConnectionHealth,
    setSelectedConnection,
    clearSelectedConnection,
    resetStore
  }
})
