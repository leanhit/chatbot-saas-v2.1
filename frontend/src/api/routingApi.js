import axios from '@/plugins/axios'

const routingApi = {
  // Routing Rules
  async getRoutingRules(params = {}) {
    const response = await axios.get('/routing/rules', { params })
    return response.data
  },

  async getRoutingRuleById(id) {
    const response = await axios.get(`/routing/rules/${id}`)
    return response.data
  },

  async createRoutingRule(data) {
    const response = await axios.post('/routing/rules', data)
    return response.data
  },

  async updateRoutingRule(id, data) {
    const response = await axios.put(`/routing/rules/${id}`, data)
    return response.data
  },

  async deleteRoutingRule(id) {
    const response = await axios.delete(`/routing/rules/${id}`)
    return response.data
  },

  async getRoutingRulesByType(ruleType) {
    const response = await axios.get(`/routing/rules/type/${ruleType}`)
    return response.data
  },

  async getRoutingRulesByTargetType(targetType) {
    const response = await axios.get(`/routing/rules/target/${targetType}`)
    return response.data
  },

  async getDefaultRoutingRule() {
    const response = await axios.get('/routing/rules/default')
    return response.data
  },

  // Escalation Rules
  async getEscalationRules(params = {}) {
    const response = await axios.get('/routing/escalation-rules', { params })
    return response.data
  },

  async getEscalationRuleById(id) {
    const response = await axios.get(`/routing/escalation-rules/${id}`)
    return response.data
  },

  async createEscalationRule(data) {
    const response = await axios.post('/routing/escalation-rules', data)
    return response.data
  },

  async updateEscalationRule(id, data) {
    const response = await axios.put(`/routing/escalation-rules/${id}`, data)
    return response.data
  },

  async deleteEscalationRule(id) {
    const response = await axios.delete(`/routing/escalation-rules/${id}`)
    return response.data
  },

  async getEscalationRulesByTriggerType(triggerType) {
    const response = await axios.get(`/routing/escalation-rules/trigger/${triggerType}`)
    return response.data
  },

  async getEscalationRulesByAction(action) {
    const response = await axios.get(`/routing/escalation-rules/action/${action}`)
    return response.data
  },

  // Supervisor Sessions
  async getSupervisorSessions(params = {}) {
    const response = await axios.get('/routing/supervisor-sessions', { params })
    return response.data
  },

  async getSupervisorSessionById(id) {
    const response = await axios.get(`/routing/supervisor-sessions/${id}`)
    return response.data
  },

  async startSupervisorSession(data) {
    const response = await axios.post('/routing/supervisor-sessions', data)
    return response.data
  },

  async endSupervisorSession(id, data = {}) {
    const response = await axios.put(`/routing/supervisor-sessions/${id}/end`, data)
    return response.data
  },

  async recordIntervention(id) {
    const response = await axios.put(`/routing/supervisor-sessions/${id}/intervention`)
    return response.data
  },

  async getSupervisorSessionsBySupervisor(supervisorId) {
    const response = await axios.get(`/routing/supervisor-sessions/supervisor/${supervisorId}`)
    return response.data
  },

  async getSupervisorSessionsByConversation(conversationId) {
    const response = await axios.get(`/routing/supervisor-sessions/conversation/${conversationId}`)
    return response.data
  },

  async getActiveSupervisorSessions() {
    const response = await axios.get('/routing/supervisor-sessions/active')
    return response.data
  },

  async getActiveSessionForSupervisorAndConversation(supervisorId, conversationId) {
    const response = await axios.get(`/routing/supervisor-sessions/active/supervisor/${supervisorId}/conversation/${conversationId}`)
    return response.data
  },

  async getActiveSessionCount(supervisorId) {
    const response = await axios.get(`/routing/supervisor-sessions/active/count/${supervisorId}`)
    return response.data
  },

  async deleteSupervisorSession(id) {
    const response = await axios.delete(`/routing/supervisor-sessions/${id}`)
    return response.data
  }
}

export default routingApi
