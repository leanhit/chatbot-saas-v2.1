import api from './api'

export default {
  /**
   * Trigger AI analysis for a specific conversation
   */
  triggerAIAnalysis(conversationId) {
    return api.post(`/ai-escalation/analyze/${conversationId}`)
  },

  /**
   * Get AI analysis results for a conversation
   */
  getAIAnalysis(conversationId) {
    return api.get(`/ai-escalation/analysis/${conversationId}`)
  },

  /**
   * Check if LLM client is enabled
   */
  getStatus() {
    return api.get('/ai-escalation/status')
  }
}
