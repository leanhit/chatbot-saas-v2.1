import axios from '@/plugins/axios'

export const tenantWebhookApi = {
  async listWebhooks() {
    const response = await axios.get('/tenant/webhooks')
    return response.data
  },

  async createWebhook(webhook) {
    const response = await axios.post('/tenant/webhooks', webhook)
    return response.data
  },

  async updateWebhook(id, webhook) {
    const response = await axios.put(`/tenant/webhooks/${id}`, webhook)
    return response.data
  },

  async deleteWebhook(id) {
    const response = await axios.delete(`/tenant/webhooks/${id}`)
    return response.data
  }
}
