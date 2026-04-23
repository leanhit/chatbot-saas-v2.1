import axios from '@/plugins/axios'
import router from '@/router'

const handleApiError = (error) => {
  if (error.response && error.response.status === 401) {
    alert('Phiên đăng nhập của bạn đã hết hạn. Vui lòng đăng nhập lại.')
    router.push('/login')
  }
  throw error
}

export const pennyRuleApi = {
  // Get all rules for a bot
  getRules(botId) {
    return axios.get(`/penny/bots/${botId}/rules`).catch(handleApiError)
  },

  // Get rules by trigger type
  getRulesByType(botId, triggerType) {
    return axios.get(`/penny/bots/${botId}/rules/by-type/${triggerType}`).catch(handleApiError)
  },

  // Create new rule
  createRule(botId, ruleData) {
    console.log('🚀 pennyRuleApi.createRule - botId:', botId)
    console.log('🚀 pennyRuleApi.createRule - botId type:', typeof botId)
    console.log('🚀 pennyRuleApi.createRule - botId JSON:', JSON.stringify(botId))
    
    // Force string conversion if object
    if (typeof botId === 'object') {
      console.log('🔧 API: Converting botId object to string')
      botId = botId.id || botId.botId || ''
      console.log('🔧 API: Converted botId to:', botId)
    }
    
    const url = `/penny/bots/${botId}/rules`
    console.log('🚀 pennyRuleApi.createRule - Final URL:', url)
    return axios.post(url, ruleData).catch(handleApiError)
  },

  // Update existing rule
  updateRule(botId, ruleId, ruleData) {
    return axios.put(`/penny/bots/${botId}/rules/${ruleId}`, ruleData).catch(handleApiError)
  },

  // Delete rule
  deleteRule(botId, ruleId) {
    return axios.delete(`/penny/bots/${botId}/rules/${ruleId}`).catch(handleApiError)
  },

  // Test rule
  testRule(botId, ruleId, testData) {
    return axios.post(`/penny/bots/${botId}/rules/${ruleId}/test`, testData).catch(handleApiError)
  },

  // Get rule statistics
  getRuleStatistics(botId) {
    return axios.get(`/penny/bots/${botId}/rules/statistics`).catch(handleApiError)
  }
}
