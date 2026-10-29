import api from './api'

export default {
  /**
   * Get all agents for current tenant (paginated)
   */
  getAgents(page = 0, size = 20, sortBy = 'createdAt', sortDir = 'desc') {
    return api.get('/agents', { params: { page, size, sortBy, sortDir } })
  },

  /**
   * Get active agents for current tenant
   */
  getActiveAgents() {
    return api.get('/agents/active')
  },

  /**
   * Get available agents (online and can accept more conversations)
   */
  getAvailableAgents() {
    return api.get('/agents/available')
  },

  /**
   * Get agent statistics for current tenant
   */
  getAgentStats() {
    return api.get('/agents/stats')
  },

  /**
   * Get agent by ID
   */
  getAgent(id) {
    return api.get(`/agents/${id}`)
  },

  /**
   * Get conversation statistics for a specific agent (OWNER only)
   */
  getAgentConversationStats(id, period = '7d') {
    return api.get(`/agents/${id}/conversation-stats`, { params: { period } })
  },

  /**
   * Get agent leaderboard (OWNER only)
   */
  getAgentLeaderboard(period = '7d') {
    return api.get('/agents/leaderboard', { params: { period } })
  },

  /**
   * Create new agent
   */
  createAgent(agent) {
    return api.post('/agents', agent)
  },

  /**
   * Update existing agent
   */
  updateAgent(id, agent) {
    return api.put(`/agents/${id}`, agent)
  },

  /**
   * Delete agent
   */
  deleteAgent(id) {
    return api.delete(`/agents/${id}`)
  },

  /**
   * Update agent status
   */
  updateAgentStatus(id, status) {
    return api.patch(`/agents/${id}/status`, { status })
  },

  /**
   * Set agent online
   */
  setAgentOnline(id) {
    return api.post(`/agents/${id}/online`)
  },

  /**
   * Set agent offline
   */
  setAgentOffline(id) {
    return api.post(`/agents/${id}/offline`)
  }
}
