import api from './api'

export default {
  /**
   * Get all agents for current tenant
   */
  getAgents() {
    return api.get('/api/agents')
  },

  /**
   * Get active agents for current tenant
   */
  getActiveAgents() {
    return api.get('/api/agents/active')
  },

  /**
   * Get available agents (online and can accept more conversations)
   */
  getAvailableAgents() {
    return api.get('/api/agents/available')
  },

  /**
   * Get agent statistics for current tenant
   */
  getAgentStats() {
    return api.get('/api/agents/stats')
  },

  /**
   * Get agent by ID
   */
  getAgent(id) {
    return api.get(`/api/agents/${id}`)
  },

  /**
   * Create new agent
   */
  createAgent(agent) {
    return api.post('/api/agents', agent)
  },

  /**
   * Update existing agent
   */
  updateAgent(id, agent) {
    return api.put(`/api/agents/${id}`, agent)
  },

  /**
   * Delete agent
   */
  deleteAgent(id) {
    return api.delete(`/api/agents/${id}`)
  },

  /**
   * Update agent status
   */
  updateAgentStatus(id, status) {
    return api.patch(`/api/agents/${id}/status`, { status })
  },

  /**
   * Set agent online
   */
  setAgentOnline(id) {
    return api.post(`/api/agents/${id}/online`)
  },

  /**
   * Set agent offline
   */
  setAgentOffline(id) {
    return api.post(`/api/agents/${id}/offline`)
  }
}
