import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from '@/plugins/axios'

export const usePennyBotStore = defineStore('pennyBot', () => {
  // State
  const bots = ref([])
  const currentBot = ref(null)
  const isLoading = ref(false)
  const error = ref(null)
  const selectedBotType = ref(null)
  const searchQuery = ref('')
  const statusFilter = ref('')
  const currentPage = ref(1)
  const pageSize = ref(10)
  const sortBy = ref('createdAt')
  const sortOrder = ref('desc')

  // Getters
  const filteredBots = computed(() => {
    let filtered = bots.value

    // Apply search filter
    if (searchQuery.value) {
      const query = searchQuery.value.toLowerCase()
      filtered = filtered.filter(bot => 
        bot.botName.toLowerCase().includes(query) ||
        bot.description?.toLowerCase().includes(query) ||
        bot.botType?.toLowerCase().includes(query)
      )
    }

    // Apply status filter
    if (statusFilter.value) {
      filtered = filtered.filter(bot => {
        if (statusFilter.value === 'active') return bot.isActive && bot.isEnabled
        if (statusFilter.value === 'inactive') return !bot.isActive || !bot.isEnabled
        if (statusFilter.value === 'enabled') return bot.isEnabled
        if (statusFilter.value === 'disabled') return !bot.isEnabled
        return true
      })
    }

    // Apply bot type filter
    if (selectedBotType.value) {
      filtered = filtered.filter(bot => bot.botType === selectedBotType.value)
    }

    // Apply sorting
    filtered.sort((a, b) => {
      let comparison = 0
      const aValue = getSortValue(a, sortBy.value)
      const bValue = getSortValue(b, sortBy.value)
      
      if (aValue < bValue) comparison = -1
      if (aValue > bValue) comparison = 1
      
      return sortOrder.value === 'desc' ? comparison : -comparison
    })

    return filtered
  })

  const activeBots = computed(() => 
    filteredBots.value.filter(bot => bot.isActive && bot.isEnabled)
  )

  const inactiveBots = computed(() => 
    filteredBots.value.filter(bot => !bot.isActive || !bot.isEnabled)
  )

  const totalBots = computed(() => bots.value.length)
  const totalPages = computed(() => 
    Math.ceil(filteredBots.value.length / pageSize.value)
  )

  const paginatedBots = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    return filteredBots.value.slice(start, end)
  })

  const botsByType = computed(() => {
    const grouped = {}
    bots.value.forEach(bot => {
      if (!grouped[bot.botType]) {
        grouped[bot.botType] = []
      }
      grouped[bot.botType].push(bot)
    })
    return grouped
  })

  // Actions
  const fetchBots = async () => {
    isLoading.value = true
    error.value = null
    
    try {
      const response = await axios.get('/api/penny/bots')
      bots.value = response.data
    } catch (err) {
      error.value = err.response?.data?.message || err.message || 'Failed to fetch bots'
      console.error('Error fetching bots:', err)
    } finally {
      isLoading.value = false
    }
  }

  const createBot = async (botData) => {
    isLoading.value = true
    error.value = null
    
    try {
      const response = await axios.post('/api/penny/bots', botData)
      bots.value.push(response.data)
      return response.data
    } catch (err) {
      error.value = err.response?.data?.message || err.message || 'Failed to create bot'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const updateBot = async (botId, updates) => {
    isLoading.value = true
    error.value = null
    
    try {
      const response = await axios.put(`/api/penny/bots/${botId}`, updates)
      
      // Update local state
      const index = bots.value.findIndex(bot => bot.id === botId)
      if (index !== -1) {
        bots.value[index] = { ...bots.value[index], ...response.data }
      }
      
      return response.data
    } catch (err) {
      error.value = err.response?.data?.message || err.message || 'Failed to update bot'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const deleteBot = async (botId) => {
    isLoading.value = true
    error.value = null
    
    try {
      await axios.delete(`/api/penny/bots/${botId}`)
      
      // Remove from local state
      bots.value = bots.value.filter(bot => bot.id !== botId)
      
      // Clear current bot if it was deleted
      if (currentBot.value?.id === botId) {
        currentBot.value = null
      }
      
      return true
    } catch (err) {
      error.value = err.response?.data?.message || err.message || 'Failed to delete bot'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const toggleBotStatus = async (botId, enabled) => {
    isLoading.value = true
    error.value = null
    
    try {
      const response = await axios.put(`/api/penny/bots/${botId}/toggle`, { enabled })
      
      // Update local state
      const index = bots.value.findIndex(bot => bot.id === botId)
      if (index !== -1) {
        bots.value[index] = { ...bots.value[index], isEnabled: enabled }
      }
      
      return response.data
    } catch (err) {
      error.value = err.response?.data?.message || err.message || 'Failed to toggle bot status'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const getBotDetails = async (botId) => {
    isLoading.value = true
    error.value = null
    
    try {
      const response = await axios.get(`/api/penny/bots/${botId}`)
      currentBot.value = response.data
      return response.data
    } catch (err) {
      error.value = err.response?.data?.message || err.message || 'Failed to fetch bot details'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const getBotHealth = async (botId) => {
    try {
      const response = await axios.get(`/api/penny/bots/${botId}/health`)
      return response.data
    } catch (err) {
      error.value = err.response?.data?.message || err.message || 'Failed to fetch bot health'
      throw err
    }
  }

  const getBotAnalytics = async (botId, timeRange = '7days') => {
    try {
      const response = await axios.get(`/api/penny/bots/${botId}/analytics`, {
        params: { timeRange }
      })
      return response.data
    } catch (err) {
      error.value = err.response?.data?.message || err.message || 'Failed to fetch bot analytics'
      throw err
    }
  }

  const chatWithBot = async (botId, message) => {
    try {
      const response = await axios.post(`/api/penny/bots/${botId}/chat`, { message })
      return response.data
    } catch (err) {
      error.value = err.response?.data?.message || err.message || 'Failed to send message'
      throw err
    }
  }

  const autoCreateBotForConnection = async (pageId) => {
    isLoading.value = true
    error.value = null
    
    try {
      const response = await axios.post('/api/penny/bots/auto', { pageId })
      bots.value.push(response.data)
      return response.data
    } catch (err) {
      error.value = err.response?.data?.message || err.message || 'Failed to auto-create bot'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const setCurrentBot = (bot) => {
    currentBot.value = bot
  }

  const clearCurrentBot = () => {
    currentBot.value = null
  }

  const resetFilters = () => {
    searchQuery.value = ''
    statusFilter.value = ''
    selectedBotType.value = null
    currentPage.value = 1
  }

  // Helper functions
  const getSortValue = (bot, field) => {
    switch (field) {
      case 'botName':
        return bot.botName?.toLowerCase() || ''
      case 'botType':
        return bot.botType?.toLowerCase() || ''
      case 'createdAt':
        return new Date(bot.createdAt).getTime()
      case 'updatedAt':
        return new Date(bot.updatedAt).getTime()
      case 'lastUsedAt':
        return bot.lastUsedAt ? new Date(bot.lastUsedAt).getTime() : 0
      default:
        return ''
    }
  }

  const getBotTypeDisplayName = (botType) => {
    const typeMap = {
      'CUSTOMER_SERVICE': 'Customer Service',
      'SALES': 'Sales',
      'SUPPORT': 'Technical Support',
      'MARKETING': 'Marketing',
      'HR': 'Human Resources',
      'FINANCE': 'Finance',
      'GENERAL': 'General Purpose'
    }
    return typeMap[botType] || botType
  }

  const getBotTypeIcon = (botType) => {
    const iconMap = {
      'CUSTOMER_SERVICE': 'ic:baseline-support-agent',
      'SALES': 'ic:baseline-shopping-cart',
      'SUPPORT': 'ic:baseline-headset-mic',
      'MARKETING': 'ic:baseline-campaign',
      'HR': 'ic:baseline-people',
      'FINANCE': 'ic:baseline-account-balance',
      'GENERAL': 'ic:baseline-settings'
    }
    return iconMap[botType] || 'ic:baseline-settings'
  }

  const getBotTypeColor = (botType) => {
    const colorMap = {
      'CUSTOMER_SERVICE': 'bg-blue-100 text-blue-600 dark:bg-blue-900/20 dark:text-blue-400',
      'SALES': 'bg-emerald-100 text-emerald-600 dark:bg-emerald-900/20 dark:text-emerald-400',
      'SUPPORT': 'bg-amber-100 text-amber-600 dark:bg-amber-900/20 dark:text-amber-400',
      'MARKETING': 'bg-purple-100 text-purple-600 dark:bg-purple-900/20 dark:text-purple-400',
      'HR': 'bg-pink-100 text-pink-600 dark:bg-pink-900/20 dark:text-pink-400',
      'FINANCE': 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400',
      'GENERAL': 'bg-gray-100 text-gray-600 dark:bg-gray-900/20 dark:text-gray-400'
    }
    return colorMap[botType] || 'bg-gray-100 text-gray-600'
  }

  const getStatusBadgeClass = (bot) => {
    if (!bot.isActive || !bot.isEnabled) {
      return 'bg-gray-100 text-gray-600 dark:bg-gray-900/20 dark:text-gray-400'
    }
    return 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400'
  }

  const getStatusText = (bot) => {
    if (!bot.isActive) return 'Inactive'
    if (!bot.isEnabled) return 'Disabled'
    return 'Active'
  }

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A'
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  return {
    // State
    bots,
    currentBot,
    isLoading,
    error,
    searchQuery,
    statusFilter,
    currentPage,
    pageSize,
    sortBy,
    sortOrder,
    
    // Getters
    filteredBots,
    activeBots,
    inactiveBots,
    totalBots,
    totalPages,
    paginatedBots,
    botsByType,
    
    // Actions
    fetchBots,
    createBot,
    updateBot,
    deleteBot,
    toggleBotStatus,
    getBotDetails,
    getBotHealth,
    getBotAnalytics,
    chatWithBot,
    autoCreateBotForConnection,
    setCurrentBot,
    clearCurrentBot,
    resetFilters,
    
    // Helpers
    getBotTypeDisplayName,
    getBotTypeIcon,
    getBotTypeColor,
    getStatusBadgeClass,
    getStatusText,
    formatDate
  }
})
