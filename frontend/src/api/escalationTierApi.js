import api from './api'

export default {
  /**
   * Get all escalation tiers for current tenant
   */
  getEscalationTiers() {
    return api.get('/escalation-tiers')
  },

  /**
   * Get escalation tier by level
   */
  getEscalationTierByLevel(level) {
    return api.get(`/escalation-tiers/level/${level}`)
  },

  /**
   * Create a new escalation tier
   */
  createEscalationTier(tier) {
    return api.post('/escalation-tiers', tier)
  },

  /**
   * Update an existing escalation tier
   */
  updateEscalationTier(id, tier) {
    return api.put(`/escalation-tiers/${id}`, tier)
  },

  /**
   * Delete an escalation tier
   */
  deleteEscalationTier(id) {
    return api.delete(`/escalation-tiers/${id}`)
  },

  /**
   * Create default escalation tiers for current tenant
   */
  createDefaultEscalationTiers() {
    return api.post('/escalation-tiers/defaults')
  }
}
