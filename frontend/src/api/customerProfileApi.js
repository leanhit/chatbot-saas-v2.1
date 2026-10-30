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
  }
}

export default customerProfileApi
