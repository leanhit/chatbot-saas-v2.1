import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { pennyApi } from '@/api/pennyApi'

export const usePennyBotStore = defineStore('penny-bot', () => {
    // State
    const pennyBots = ref([])
    const currentPennyBot = ref(null)
    const loadingBots = ref(false)
    const creatingBot = ref(false)
    const updatingBot = ref(false)
    const deletingBot = ref(false)
    const chatLoading = ref(false)
    const analyticsLoading = ref(false)
    const healthLoading = ref(false)

    // Getters
    const activeBots = computed(() => 
        pennyBots.value.filter(bot => bot.isActive && bot.isEnabled)
    )
    
    const inactiveBots = computed(() => 
        pennyBots.value.filter(bot => !bot.isActive || !bot.isEnabled)
    )
    
    const botsByType = computed(() => {
        const grouped = {}
        pennyBots.value.forEach(bot => {
            if (!grouped[bot.botType]) {
                grouped[bot.botType] = []
            }
            grouped[bot.botType].push(bot)
        })
        return grouped
    })

    // Actions
    const fetchPennyBots = async () => {
        loadingBots.value = true
        try {
            const { data } = await pennyApi.getMyPennyBots()
            pennyBots.value = data
        } catch (error) {
            console.error('Failed to fetch Penny bots:', error)
            throw error
        } finally {
            loadingBots.value = false
        }
    }

    const createPennyBot = async (botData) => {
        creatingBot.value = true
        try {
            const { data } = await pennyApi.createPennyBot(botData)
            pennyBots.value.push(data)
            return data
        } catch (error) {
            console.error('Failed to create Penny bot:', error)
            throw error
        } finally {
            creatingBot.value = false
        }
    }

    const updatePennyBot = async (botId, botData) => {
        updatingBot.value = true
        try {
            const { data } = await pennyApi.updatePennyBot(botId, botData)
            const index = pennyBots.value.findIndex(bot => bot.botId === botId)
            if (index !== -1) {
                pennyBots.value[index] = data
            }
            if (currentPennyBot.value?.botId === botId) {
                currentPennyBot.value = data
            }
            return data
        } catch (error) {
            console.error('Failed to update Penny bot:', error)
            throw error
        } finally {
            updatingBot.value = false
        }
    }

    const deletePennyBot = async (botId) => {
        deletingBot.value = true
        try {
            await pennyApi.deletePennyBot(botId)
            pennyBots.value = pennyBots.value.filter(bot => bot.botId !== botId)
            if (currentPennyBot.value?.botId === botId) {
                currentPennyBot.value = null
            }
            return true
        } catch (error) {
            console.error('Failed to delete Penny bot:', error)
            throw error
        } finally {
            deletingBot.value = false
        }
    }

    const togglePennyBotStatus = async (botId, enabled) => {
        try {
            const { data } = await pennyApi.togglePennyBotStatus(botId, enabled)
            const index = pennyBots.value.findIndex(bot => bot.botId === botId)
            if (index !== -1) {
                pennyBots.value[index] = data
            }
            if (currentPennyBot.value?.botId === botId) {
                currentPennyBot.value = data
            }
            return data
        } catch (error) {
            console.error('Failed to toggle Penny bot status:', error)
            throw error
        }
    }

    const getPennyBotHealth = async (botId) => {
        healthLoading.value = true
        try {
            const { data } = await pennyApi.getPennyBotHealth(botId)
            return data
        } catch (error) {
            console.error('Failed to get Penny bot health:', error)
            throw error
        } finally {
            healthLoading.value = false
        }
    }

    const getPennyBotAnalytics = async (botId, timeRange = '7days') => {
        analyticsLoading.value = true
        try {
            const { data } = await pennyApi.getPennyBotAnalytics(botId, timeRange)
            return data
        } catch (error) {
            console.error('Failed to get Penny bot analytics:', error)
            throw error
        } finally {
            analyticsLoading.value = false
        }
    }

    const chatWithPennyBot = async (botId, message) => {
        chatLoading.value = true
        try {
            const { data } = await pennyApi.chatWithPennyBot(botId, message)
            return data
        } catch (error) {
            console.error('Failed to chat with Penny bot:', error)
            throw error
        } finally {
            chatLoading.value = false
        }
    }

    const chatWithPennyBotPublic = async (botId, message) => {
        chatLoading.value = true
        try {
            const { data } = await pennyApi.chatWithPennyBotPublic(botId, message)
            return data
        } catch (error) {
            console.error('Failed to chat with Penny bot (public):', error)
            throw error
        } finally {
            chatLoading.value = false
        }
    }

    const autoCreatePennyBot = async (pageId) => {
        creatingBot.value = true
        try {
            const { data } = await pennyApi.autoCreatePennyBot(pageId)
            pennyBots.value.push(data)
            return data
        } catch (error) {
            console.error('Failed to auto-create Penny bot:', error)
            throw error
        } finally {
            creatingBot.value = false
        }
    }

    const setCurrentPennyBot = (bot) => {
        currentPennyBot.value = bot
    }

    const clearCurrentPennyBot = () => {
        currentPennyBot.value = null
    }

    const resetStore = () => {
        pennyBots.value = []
        currentPennyBot.value = null
        loadingBots.value = false
        creatingBot.value = false
        updatingBot.value = false
        deletingBot.value = false
        chatLoading.value = false
        analyticsLoading.value = false
        healthLoading.value = false
    }

    return {
        // State
        pennyBots,
        currentPennyBot,
        loadingBots,
        creatingBot,
        updatingBot,
        deletingBot,
        chatLoading,
        analyticsLoading,
        healthLoading,

        // Getters
        activeBots,
        inactiveBots,
        botsByType,

        // Actions
        fetchPennyBots,
        createPennyBot,
        updatePennyBot,
        deletePennyBot,
        togglePennyBotStatus,
        getPennyBotHealth,
        getPennyBotAnalytics,
        chatWithPennyBot,
        chatWithPennyBotPublic,
        autoCreatePennyBot,
        setCurrentPennyBot,
        clearCurrentPennyBot,
        resetStore
    }
})
