import axios from '@/plugins/axios'

const customerProfileApi = {
  // Get all customer profiles with filters
  async getCustomerProfiles(params = {}) {
    const response = await axios.get('/customer/profiles', { params })
    return response.data
  },

  // Get customer profile by ID
  async getCustomerProfileById(id) {
    const response = await axios.get(`/customer/profiles/${id}`)
    return response.data
  },

  // Create customer profile
  async createCustomerProfile(data) {
    const response = await axios.post('/customer/profiles', data)
    return response.data
  },

  // Update customer profile
  async updateCustomerProfile(id, data) {
    const response = await axios.put(`/customer/profiles/${id}`, data)
    return response.data
  },

  // Delete customer profile
  async deleteCustomerProfile(id) {
    const response = await axios.delete(`/customer/profiles/${id}`)
    return response.data
  },

  // Update customer conversation statistics
  async updateCustomerStats(id, data) {
    const response = await axios.put(`/customer/profiles/${id}/stats`, data)
    return response.data
  },

  // Get customer profiles by status
  async getCustomerProfilesByStatus(status) {
    const response = await axios.get('/customer/profiles', { params: { status } })
    return response.data
  },

  // Get customer profiles by tier
  async getCustomerProfilesByTier(tier) {
    const response = await axios.get('/customer/profiles', { params: { tier } })
    return response.data
  },
  // Phase 4: customer timeline
  async getCustomerTimeline(id) {
    const response = await axios.get(`/customer/profiles/${id}/timeline`)
    return response.data
  },

  // Phase 4: customer notes
  async getCustomerNotes(id) {
    const response = await axios.get(`/customer/profiles/${id}/notes`)
    return response.data
  },

  async createCustomerNote(id, data) {
    const response = await axios.post(`/customer/profiles/${id}/notes`, data)
    return response.data
  },

  async deleteCustomerNote(noteId) {
    const response = await axios.delete(`/customer/profiles/notes/${noteId}`)
    return response.data
  },

  // Phase 4: tier recalculation
  async recalculateTier(id, force = false) {
    const response = await axios.post(`/customer/profiles/${id}/recalculate-tier`, null, { params: { force } })
    return response.data
  },
}

export default customerProfileApi
