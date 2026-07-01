import api from './api'

export default {
  /**
   * Get all escalation tiers for current tenant
   */
  getEscalationTiers() {
    return api.get('/api/escalation-tiers')
  },

  /**
   * Get escalation tier by level
   */
  getEscalationTierByLevel(level) {
    return api.get(`/api/escalation-tiers/level/${level}`)
  },

  /**
   * Create a new escalation tier
   */
  createEscalationTier(tier) {
    return api.post('/api/escalation-tiers', tier)
  },

  /**
   * Update an existing escalation tier
   */
  updateEscalationTier(id, tier) {
    return api.put(`/api/escalation-tiers/${id}`, tier)
  },

  /**
   * Delete an escalation tier
   */
  deleteEscalationTier(id) {
    return api.delete(`/api/escalation-tiers/${id}`)
  },

  /**
   * Create default escalation tiers for current tenant
   */
  createDefaultEscalationTiers() {
    return api.post('/api/escalation-tiers/defaults')
  }
}
