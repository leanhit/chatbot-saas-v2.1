import api from './api'

export default {
  /**
   * Get all SLA configurations for current tenant
   */
  getSLAConfigurations() {
    return api.get('/api/sla-configurations')
  },

  /**
   * Get SLA configuration by customer tier
   */
  getSLAConfigurationByTier(customerTier) {
    return api.get(`/api/sla-configurations/tier/${customerTier}`)
  },

  /**
   * Create a new SLA configuration
   */
  createSLAConfiguration(config) {
    return api.post('/api/sla-configurations', config)
  },

  /**
   * Update an existing SLA configuration
   */
  updateSLAConfiguration(id, config) {
    return api.put(`/api/sla-configurations/${id}`, config)
  },

  /**
   * Delete an SLA configuration
   */
  deleteSLAConfiguration(id) {
    return api.delete(`/api/sla-configurations/${id}`)
  },

  /**
   * Create default SLA configurations for current tenant
   */
  createDefaultSLAConfigurations() {
    return api.post('/api/sla-configurations/defaults')
  }
}
