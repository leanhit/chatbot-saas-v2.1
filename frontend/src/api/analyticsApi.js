import axios from '@/plugins/axios'

const analyticsApi = {
  // Customer journey timeline
  async getCustomerJourney(customerProfileId) {
    const response = await axios.get(`/analytics/journey/${customerProfileId}`)
    return response.data
  },

  // Funnel
  async getFunnel() {
    const response = await axios.get('/analytics/funnel')
    return response.data
  },
  async listFunnelStages() {
    const response = await axios.get('/analytics/funnel/stages')
    return response.data
  },
  async createFunnelStage(data) {
    const response = await axios.post('/analytics/funnel/stages', data)
    return response.data
  },
  async updateFunnelStage(id, data) {
    const response = await axios.put(`/analytics/funnel/stages/${id}`, data)
    return response.data
  },
  async deleteFunnelStage(id) {
    const response = await axios.delete(`/analytics/funnel/stages/${id}`)
    return response.data
  },

  // Custom reports
  async listReports() {
    const response = await axios.get('/reports')
    return response.data
  },
  async createReport(data) {
    const response = await axios.post('/reports', data)
    return response.data
  },
  async updateReport(id, data) {
    const response = await axios.put(`/reports/${id}`, data)
    return response.data
  },
  async deleteReport(id) {
    const response = await axios.delete(`/reports/${id}`)
    return response.data
  },
  async generateReport(id) {
    const response = await axios.post(`/reports/${id}/generate`)
    return response.data
  },
  async getReportResult(id) {
    const response = await axios.get(`/reports/${id}/result`)
    return response.data
  },

  // Report schedules
  async listSchedules() {
    const response = await axios.get('/reports/schedules')
    return response.data
  },
  async createSchedule(data) {
    const response = await axios.post('/reports/schedules', data)
    return response.data
  },
  async updateSchedule(id, data) {
    const response = await axios.put(`/reports/schedules/${id}`, data)
    return response.data
  },
  async deleteSchedule(id) {
    const response = await axios.delete(`/reports/schedules/${id}`)
    return response.data
  },
}

export default analyticsApi
