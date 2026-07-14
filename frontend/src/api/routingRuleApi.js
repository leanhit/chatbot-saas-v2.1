import api from './api'

export default {
  /**
   * Get all routing rules for current tenant
   */
  getRoutingRules() {
    return api.get('/routing-rules')
  },

  /**
   * Get active routing rules for current tenant
   */
  getActiveRoutingRules() {
    return api.get('/routing-rules/active')
  },

  /**
   * Create a new routing rule
   */
  createRoutingRule(rule) {
    return api.post('/routing-rules', rule)
  },

  /**
   * Update an existing routing rule
   */
  updateRoutingRule(id, rule) {
    return api.put(`/routing-rules/${id}`, rule)
  },

  /**
   * Delete a routing rule
   */
  deleteRoutingRule(id) {
    return api.delete(`/routing-rules/${id}`)
  },

  /**
   * Create default routing rules for current tenant
   */
  createDefaultRoutingRules() {
    return api.post('/routing-rules/defaults')
  }
}
