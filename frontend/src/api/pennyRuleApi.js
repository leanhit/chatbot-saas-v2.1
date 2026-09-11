import axios from '@/plugins/axios'
import router from '@/router'

const handleApiError = (error) => {
  if (error.response && error.response.status === 401) {
    alert('Phiên đăng nhập của bạn đã hết hạn. Vui lòng đăng nhập lại.')
    router.push('/login')
  }
  throw error
}

const isValidBotId = (botId) => {
  if (!botId) return false;
  if (typeof botId === 'string' && (botId === 'undefined' || botId === 'null' || botId.trim() === '')) return false;
  return true;
};

export const pennyRuleApi = {
  // Get all rules for a bot
  getRules(botId) {
    if (!isValidBotId(botId)) return Promise.reject(new Error(`Invalid botId: ${botId}`));
    return axios.get(`/penny/bots/${botId}/rules`).catch(handleApiError)
  },

  // Get rules by trigger type
  getRulesByType(botId, triggerType) {
    if (!isValidBotId(botId)) return Promise.reject(new Error(`Invalid botId: ${botId}`));
    return axios.get(`/penny/bots/${botId}/rules/by-type/${triggerType}`).catch(handleApiError)
  },

  // Create new rule
  createRule(botId, ruleData) {
    // Force string conversion if object
    if (typeof botId === 'object') {
      botId = botId.id || botId.botId || ''
    }
    if (!isValidBotId(botId)) return Promise.reject(new Error(`Invalid botId: ${botId}`));
    
    const url = `/penny/bots/${botId}/rules`
    return axios.post(url, ruleData).catch(handleApiError)
  },

  // Update existing rule
  updateRule(botId, ruleId, ruleData) {
    if (!isValidBotId(botId)) return Promise.reject(new Error(`Invalid botId: ${botId}`));
    return axios.put(`/penny/bots/${botId}/rules/${ruleId}`, ruleData).catch(handleApiError)
  },

  // Delete rule
  deleteRule(botId, ruleId) {
    if (!isValidBotId(botId)) return Promise.reject(new Error(`Invalid botId: ${botId}`));
    return axios.delete(`/penny/bots/${botId}/rules/${ruleId}`).catch(handleApiError)
  },

  // Test rule
  testRule(botId, ruleId, testData) {
    if (!isValidBotId(botId)) return Promise.reject(new Error(`Invalid botId: ${botId}`));
    return axios.post(`/penny/bots/${botId}/rules/${ruleId}/test`, testData).catch(handleApiError)
  },

  // Get rule statistics
  getRuleStatistics(botId) {
    if (!isValidBotId(botId)) return Promise.reject(new Error(`Invalid botId: ${botId}`));
    return axios.get(`/penny/bots/${botId}/rules/statistics`).catch(handleApiError)
  }
}
