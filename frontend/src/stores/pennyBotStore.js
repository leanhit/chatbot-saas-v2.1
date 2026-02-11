import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { pennyApi } from '@/api/pennyApi'

export const usePennyBotStore = defineStore('pennyBot', () => {
  // State
  const bots = ref([])
  const loading = ref(false)
  const error = ref(null)
  const selectedBot = ref(null)

  // Computed
  const activeBots = computed(() => bots.value.filter(bot => bot.isActive))
  const inactiveBots = computed(() => bots.value.filter(bot => !bot.isActive))
  const totalBots = computed(() => bots.value.length)
  const botById = computed(() => {
    const map = {}
    bots.value.forEach(bot => {
      map[bot.id] = bot
    })
    return map
  })

  // Actions
  const fetchBots = async () => {
    loading.value = true
    error.value = null
    
    try {
      const response = await pennyApi.getBots()
      bots.value = response.data
    } catch (err) {
      error.value = err.message || 'Failed to fetch bots'
      console.error('Failed to fetch bots:', err)
    } finally {
      loading.value = false
    }
  }

  const createBot = async (botData) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await pennyApi.createBot(botData)
      bots.value.push(response.data)
      return response.data
    } catch (err) {
      error.value = err.message || 'Failed to create bot'
      console.error('Failed to create bot:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const updateBot = async (id, botData) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await pennyApi.updateBot(id, botData)
      const index = bots.value.findIndex(bot => bot.id === id)
      if (index !== -1) {
        bots.value[index] = response.data
      }
      return response.data
    } catch (err) {
      error.value = err.message || 'Failed to update bot'
      console.error('Failed to update bot:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const deleteBot = async (id) => {
    loading.value = true
    error.value = null
    
    try {
      await pennyApi.deleteBot(id)
      bots.value = bots.value.filter(bot => bot.id !== id)
      return true
    } catch (err) {
      error.value = err.message || 'Failed to delete bot'
      console.error('Failed to delete bot:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const toggleBotStatus = async (id, isActive) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await pennyApi.toggleBotStatus(id, isActive)
      const index = bots.value.findIndex(bot => bot.id === id)
      if (index !== -1) {
        bots.value[index] = response.data
      }
      return response.data
    } catch (err) {
      error.value = err.message || 'Failed to toggle bot status'
      console.error('Failed to toggle bot status:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const selectBot = (bot) => {
    selectedBot.value = bot
  }

  const clearSelectedBot = () => {
    selectedBot.value = null
  }

  const clearError = () => {
    error.value = null
  }

  // Analytics actions
  const fetchBotAnalytics = async (id) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await pennyApi.getBotAnalytics(id)
      return response.data
    } catch (err) {
      error.value = err.message || 'Failed to fetch bot analytics'
      console.error('Failed to fetch bot analytics:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // Chat actions
  const sendMessage = async (botId, message) => {
    try {
      const response = await pennyApi.sendMessage(botId, message)
      return response.data
    } catch (err) {
      error.value = err.message || 'Failed to send message'
      console.error('Failed to send message:', err)
      throw err
    }
  }

  const fetchChatHistory = async (botId, page = 1, limit = 50) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await pennyApi.getChatHistory(botId, page, limit)
      return response.data
    } catch (err) {
      error.value = err.message || 'Failed to fetch chat history'
      console.error('Failed to fetch chat history:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // Rules actions
  const fetchRules = async (botId) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await pennyApi.getRules(botId)
      return response.data
    } catch (err) {
      error.value = err.message || 'Failed to fetch rules'
      console.error('Failed to fetch rules:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const createRule = async (botId, ruleData) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await pennyApi.createRule(botId, ruleData)
      return response.data
    } catch (err) {
      error.value = err.message || 'Failed to create rule'
      console.error('Failed to create rule:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const updateRule = async (botId, ruleId, ruleData) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await pennyApi.updateRule(botId, ruleId, ruleData)
      return response.data
    } catch (err) {
      error.value = err.message || 'Failed to update rule'
      console.error('Failed to update rule:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const deleteRule = async (botId, ruleId) => {
    loading.value = true
    error.value = null
    
    try {
      await pennyApi.deleteRule(botId, ruleId)
      return true
    } catch (err) {
      error.value = err.message || 'Failed to delete rule'
      console.error('Failed to delete rule:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const toggleRuleStatus = async (botId, ruleId, isActive) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await pennyApi.toggleRuleStatus(botId, ruleId, isActive)
      return response.data
    } catch (err) {
      error.value = err.message || 'Failed to toggle rule status'
      console.error('Failed to toggle rule status:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const testRule = async (botId, ruleId) => {
    try {
      const response = await pennyApi.testRule(botId, ruleId)
      return response.data
    } catch (err) {
      error.value = err.message || 'Failed to test rule'
      console.error('Failed to test rule:', err)
      throw err
    }
  }

  // Connections actions
  const fetchConnections = async (botId) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await pennyApi.getConnections(botId)
      return response.data
    } catch (err) {
      error.value = err.message || 'Failed to fetch connections'
      console.error('Failed to fetch connections:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const createConnection = async (botId, connectionData) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await pennyApi.createConnection(botId, connectionData)
      return response.data
    } catch (err) {
      error.value = err.message || 'Failed to create connection'
      console.error('Failed to create connection:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const updateConnection = async (botId, connectionId, connectionData) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await pennyApi.updateConnection(botId, connectionId, connectionData)
      return response.data
    } catch (err) {
      error.value = err.message || 'Failed to update connection'
      console.error('Failed to update connection:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const deleteConnection = async (botId, connectionId) => {
    loading.value = true
    error.value = null
    
    try {
      await pennyApi.deleteConnection(botId, connectionId)
      return true
    } catch (err) {
      error.value = err.message || 'Failed to delete connection'
      console.error('Failed to delete connection:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const testConnection = async (botId, connectionId) => {
    try {
      const response = await pennyApi.testConnection(botId, connectionId)
      return response.data
    } catch (err) {
      error.value = err.message || 'Failed to test connection'
      console.error('Failed to test connection:', err)
      throw err
    }
  }

  const checkConnectionHealth = async (botId, connectionId) => {
    try {
      const response = await pennyApi.checkConnectionHealth(botId, connectionId)
      return response.data
    } catch (err) {
      error.value = err.message || 'Failed to check connection health'
      console.error('Failed to check connection health:', err)
      throw err
    }
  }

  return {
    // State
    bots,
    loading,
    error,
    selectedBot,
    
    // Computed
    activeBots,
    inactiveBots,
    totalBots,
    botById,
    
    // Actions
    fetchBots,
    createBot,
    updateBot,
    deleteBot,
    toggleBotStatus,
    selectBot,
    clearSelectedBot,
    clearError,
    fetchBotAnalytics,
    sendMessage,
    fetchChatHistory,
    fetchRules,
    createRule,
    updateRule,
    deleteRule,
    toggleRuleStatus,
    testRule,
    fetchConnections,
    createConnection,
    updateConnection,
    deleteConnection,
    testConnection,
    checkConnectionHealth
  }
})
