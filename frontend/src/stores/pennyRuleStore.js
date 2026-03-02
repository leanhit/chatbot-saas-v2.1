import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { pennyRuleApi } from '@/api/pennyRuleApi'

export const usePennyRuleStore = defineStore('penny-rule', () => {
  // State
  const rules = ref([])
  const selectedRule = ref(null)
  const loadingRules = ref(false)
  const creatingRule = ref(false)
  const updatingRule = ref(false)
  const deletingRule = ref(false)
  const testingRule = ref(false)
  const statistics = ref(null)

  // Getters
  const activeRules = computed(() => rules.value.filter(rule => rule.isActive))
  const rulesByType = computed(() => {
    const grouped = {}
    rules.value.forEach(rule => {
      if (!grouped[rule.triggerType]) {
        grouped[rule.triggerType] = []
      }
      grouped[rule.triggerType].push(rule)
    })
    return grouped
  })

  const rulesByPriority = computed(() => {
    return [...rules.value].sort((a, b) => b.priority - a.priority)
  })

  // Actions
  const fetchRules = async (botId) => {
    loadingRules.value = true
    try {
      const response = await pennyRuleApi.getRules(botId)
      rules.value = response.data
      return response.data
    } catch (error) {
      console.error('Failed to fetch rules:', error)
      throw error
    } finally {
      loadingRules.value = false
    }
  }

  const fetchRulesByType = async (botId, triggerType) => {
    loadingRules.value = true
    try {
      const response = await pennyRuleApi.getRulesByType(botId, triggerType)
      return response.data
    } catch (error) {
      console.error('Failed to fetch rules by type:', error)
      throw error
    } finally {
      loadingRules.value = false
    }
  }

  const createRule = async (botId, ruleData) => {
    creatingRule.value = true
    try {
      const response = await pennyRuleApi.createRule(botId, ruleData)
      // Refresh rules list
      await fetchRules(botId)
      return response.data
    } catch (error) {
      console.error('Failed to create rule:', error)
      throw error
    } finally {
      creatingRule.value = false
    }
  }

  const updateRule = async (botId, ruleId, ruleData) => {
    updatingRule.value = true
    try {
      const response = await pennyRuleApi.updateRule(botId, ruleId, ruleData)
      // Refresh rules list
      await fetchRules(botId)
      return response.data
    } catch (error) {
      console.error('Failed to update rule:', error)
      throw error
    } finally {
      updatingRule.value = false
    }
  }

  const deleteRule = async (botId, ruleId) => {
    deletingRule.value = true
    try {
      const response = await pennyRuleApi.deleteRule(botId, ruleId)
      // Remove from local state
      rules.value = rules.value.filter(rule => rule.id !== ruleId)
      return response.data
    } catch (error) {
      console.error('Failed to delete rule:', error)
      throw error
    } finally {
      deletingRule.value = false
    }
  }

  const testRule = async (botId, ruleId, testData) => {
    testingRule.value = true
    try {
      const response = await pennyRuleApi.testRule(botId, ruleId, testData)
      return response.data
    } catch (error) {
      console.error('Failed to test rule:', error)
      throw error
    } finally {
      testingRule.value = false
    }
  }

  const fetchStatistics = async (botId) => {
    try {
      const response = await pennyRuleApi.getRuleStatistics(botId)
      statistics.value = response.data
      return response.data
    } catch (error) {
      console.error('Failed to fetch rule statistics:', error)
      throw error
    }
  }

  const setSelectedRule = (rule) => {
    selectedRule.value = rule
  }

  const clearSelectedRule = () => {
    selectedRule.value = null
  }

  const resetStore = () => {
    rules.value = []
    selectedRule.value = null
    loadingRules.value = false
    creatingRule.value = false
    updatingRule.value = false
    deletingRule.value = false
    testingRule.value = false
    statistics.value = null
  }

  return {
    // State
    rules,
    selectedRule,
    loadingRules,
    creatingRule,
    updatingRule,
    deletingRule,
    testingRule,
    statistics,

    // Getters
    activeRules,
    rulesByType,
    rulesByPriority,

    // Actions
    fetchRules,
    fetchRulesByType,
    createRule,
    updateRule,
    deleteRule,
    testRule,
    fetchStatistics,
    setSelectedRule,
    clearSelectedRule,
    resetStore
  }
})
