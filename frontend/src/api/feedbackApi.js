import axios from '@/plugins/axios'

const feedbackApi = {
  // Get all feedback with filters
  async getFeedback(params = {}) {
    const response = await axios.get('/customer/feedback', { params })
    return response.data
  },

  // Get feedback by ID
  async getFeedbackById(id) {
    const response = await axios.get(`/customer/feedback/${id}`)
    return response.data
  },

  // Create feedback
  async createFeedback(data) {
    const response = await axios.post('/customer/feedback', data)
    return response.data
  },

  // Update feedback
  async updateFeedback(id, data) {
    const response = await axios.put(`/customer/feedback/${id}`, data)
    return response.data
  },

  // Delete feedback
  async deleteFeedback(id) {
    const response = await axios.delete(`/customer/feedback/${id}`)
    return response.data
  },

  // Get feedback by customer profile ID
  async getFeedbackByCustomerProfileId(customerProfileId) {
    const response = await axios.get('/customer/feedback', { params: { customerProfileId } })
    return response.data
  },

  // Get feedback by conversation ID
  async getFeedbackByConversationId(conversationId) {
    const response = await axios.get('/customer/feedback', { params: { conversationId } })
    return response.data
  },

  // Get feedback by status
  async getFeedbackByStatus(status) {
    const response = await axios.get('/customer/feedback', { params: { status } })
    return response.data
  },

  // Get feedback by type
  async getFeedbackByType(type) {
    const response = await axios.get('/customer/feedback', { params: { type } })
    return response.data
  },

  // Get recent feedback
  async getRecentFeedback(limit = 10) {
    const response = await axios.get('/customer/feedback/recent', { params: { limit } })
    return response.data
  },

  // Get feedback by date range
  async getFeedbackByDateRange(startDate, endDate) {
    const response = await axios.get('/customer/feedback', {
      params: { startDate, endDate }
    })
    return response.data
  }
}

export default feedbackApi
