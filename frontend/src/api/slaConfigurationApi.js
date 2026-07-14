import api from './api'

export default {
  /**
   * Get all SLA configurations for current tenant
   */
  getSLAConfigurations() {
    return api.get('/sla-configurations')
  },

  /**
   * Get SLA configuration by customer tier
   */
  getSLAConfigurationByTier(customerTier) {
    return api.get(`/sla-configurations/tier/${customerTier}`)
  },

  /**
   * Create a new SLA configuration
   */
  createSLAConfiguration(config) {
    return api.post('/sla-configurations', config)
  },

  /**
   * Update an existing SLA configuration
   */
  updateSLAConfiguration(id, config) {
    return api.put(`/sla-configurations/${id}`, config)
  },

  /**
   * Delete an SLA configuration
   */
  deleteSLAConfiguration(id) {
    return api.delete(`/sla-configurations/${id}`)
  },

  /**
   * Create default SLA configurations for current tenant
   */
  createDefaultSLAConfigurations() {
    return api.post('/sla-configurations/defaults')
  }
}
