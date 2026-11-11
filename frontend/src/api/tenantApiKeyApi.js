import axios from '@/plugins/axios'

export const tenantApiKeyApi = {
  async listApiKeys() {
    const response = await axios.get('/tenant/api-keys')
    return response.data
  },

  async createApiKey(payload) {
    const response = await axios.post('/tenant/api-keys', payload)
    return response.data
  },

  async revokeApiKey(id) {
    const response = await axios.delete(`/tenant/api-keys/${id}`)
    return response.data
  }
}
