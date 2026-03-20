import axios from '@/plugins/axios'

const customerApi = {
  // Get all customers with pagination
  async getCustomers(params = {}) {
    const { page = 0, size = 20, sort = 'updatedAt', direction = 'desc' } = params
    const response = await axios.get('/odoo/customers', {
      params: { page, size, sort, direction }
    })
    return response.data
  },

  // Get customer by PSID
  async getCustomerByPsid(psid) {
    const response = await axios.get(`/odoo/customers/${psid}`)
    return response.data
  },

  // Search customers
  async searchCustomers(keyword, page = 0, size = 20) {
    const response = await axios.get('/odoo/customers/search', {
      params: { keyword, page, size }
    })
    return response.data
  },

  // Get customers by status
  async getCustomersByStatus(status, page = 0, size = 20) {
    const response = await axios.get(`/odoo/customers/status/${status}`, {
      params: { page, size }
    })
    return response.data
  },

  // Get customer statistics
  async getCustomerStats() {
    const response = await axios.get('/odoo/customers/stats')
    return response.data
  },

  // Get available statuses
  async getAvailableStatuses() {
    const response = await axios.get('/odoo/customers/statuses')
    return response.data
  }
}

export default customerApi
