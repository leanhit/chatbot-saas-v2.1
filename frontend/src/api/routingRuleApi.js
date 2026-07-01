import api from './api'

export default {
  /**
   * Get all routing rules for current tenant
   */
  getRoutingRules() {
    return api.get('/api/routing-rules')
  },

  /**
   * Get active routing rules for current tenant
   */
  getActiveRoutingRules() {
    return api.get('/api/routing-rules/active')
  },

  /**
   * Create a new routing rule
   */
  createRoutingRule(rule) {
    return api.post('/api/routing-rules', rule)
  },

  /**
   * Update an existing routing rule
   */
  updateRoutingRule(id, rule) {
    return api.put(`/api/routing-rules/${id}`, rule)
  },

  /**
   * Delete a routing rule
   */
  deleteRoutingRule(id) {
    return api.delete(`/api/routing-rules/${id}`)
  },

  /**
   * Create default routing rules for current tenant
   */
  createDefaultRoutingRules() {
    return api.post('/api/routing-rules/defaults')
  }
}
