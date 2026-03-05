import axios from '@/plugins/axios'
import router from '@/router'

const handleApiError = (error) => {
  if (error.response && error.response.status === 401) {
    alert('Phiên đăng nhập của bạn đã hết hạn. Vui lòng đăng nhập lại.')
    router.push('/login')
  }
  throw error
}

export const pennyConnectionApi = {
  // Get all connections for a bot
  getConnections(botId) {
    return axios.get(`/connection/facebook/bot/${botId}/list`).catch(handleApiError)
  },

  // Get connections by type
  getConnectionsByType(botId, connectionType) {
    if (connectionType === 'facebook') {
      return axios.get(`/connection/facebook/bot/${botId}/list`).catch(handleApiError)
    }
    return axios.get(`/penny/bots/${botId}/connections/by-type/${connectionType}`).catch(handleApiError)
  },

  // Create new connection (Facebook style - matches backend DTO)
  createConnection(botId, connectionData) {
    // For Facebook connections, use the existing Facebook connection API
    if (connectionData.connectionType === 'FACEBOOK') {
      const fbData = {
        botId: botId,
        botName: connectionData.connectionName || connectionData.botName,
        connectionName: connectionData.connectionName,
        description: connectionData.description,
        priority: connectionData.priority,
        connectionType: connectionData.connectionType,
        pageId: connectionData.pageId,
        fanpageUrl: connectionData.fanpageUrl,
        pageAccessToken: connectionData.pageAccessToken,
        appSecret: connectionData.appSecret,
        verifyToken: connectionData.verifyToken,
        urlCallback: connectionData.urlCallback || 'https://chat.truyenthongviet.vn/webhooks/facebook/pennybot',
        isEnabled: true,
        chatbotProvider: 'PENNYBOT', // Use PENNYBOT instead of BOTPRESS
        config: connectionData.config || {}
      }
      return axios.post(`/connection/facebook`, fbData).catch(handleApiError)
    }
    
    // For other connection types, use generic structure
    return axios.post(`/penny/bots/${botId}/connections`, connectionData).catch(handleApiError)
  },

  // Update existing connection
  updateConnection(botId, connectionId, connectionData) {
    // For Facebook connections, use the existing Facebook connection API
    if (connectionData.connectionType === 'FACEBOOK') {
      const fbData = {
        botId: botId,
        botName: connectionData.connectionName || connectionData.botName,
        connectionName: connectionData.connectionName,
        description: connectionData.description,
        priority: connectionData.priority,
        connectionType: connectionData.connectionType,
        pageId: connectionData.pageId,
        fanpageUrl: connectionData.fanpageUrl,
        pageAccessToken: connectionData.pageAccessToken,
        appSecret: connectionData.appSecret,
        verifyToken: connectionData.verifyToken,
        urlCallback: connectionData.urlCallback || 'https://chat.truyenthongviet.vn/webhooks/facebook/pennybot',
        isEnabled: connectionData.isEnabled !== undefined ? connectionData.isEnabled : true,
        chatbotProvider: 'PENNYBOT', // Use PENNYBOT instead of BOTPRESS
        config: connectionData.config || {}
      }
      return axios.put(`/connection/facebook/${connectionId}`, fbData).catch(handleApiError)
    }
    
    // For other connection types, use generic structure
    return axios.put(`/penny/bots/${botId}/connections/${connectionId}`, connectionData).catch(handleApiError)
  },

  // Delete connection
  deleteConnection(botId, connectionId) {
    return axios.delete(`/connection/facebook/${connectionId}`).catch(handleApiError)
  },

  // Test connection
  testConnection(botId, connectionId, testData) {
    return axios.post(`/penny/bots/${botId}/connections/${connectionId}/test`, testData).catch(handleApiError)
  },

  // Get connection statistics
  getConnectionStatistics(botId) {
    return axios.get(`/penny/bots/${botId}/connections/statistics`).catch(handleApiError)
  },

  // Toggle connection status
  toggleConnectionStatus(botId, connectionId) {
    return axios.post(`/penny/bots/${botId}/connections/${connectionId}/toggle`).catch(handleApiError)
  },

  // Get connection health status
  getConnectionHealth(botId, connectionId) {
    return axios.get(`/penny/bots/${botId}/connections/${connectionId}/health`).catch(handleApiError)
  },

  // Facebook specific methods (from frontend-old)
  subscribePageToWebhook: (pageId, pageAccessToken) => {
    return axios.post(
      `https://graph.facebook.com/v18.0/${pageId}/subscribed_apps`,
      null,
      {
        params: {
          access_token: pageAccessToken,
          subscribed_fields: 'messages,messaging_postbacks',
        },
      }
    ).catch(handleApiError)
  },

  unsubscribePageFromWebhook: (pageId, pageAccessToken) => {
    return axios.delete(
      `https://graph.facebook.com/v18.0/${pageId}/subscribed_apps`,
      {
        params: {
          access_token: pageAccessToken,
        },
      }
    ).catch(handleApiError)
  },

  // Auto-connect Facebook pages
  autoConnectFacebook: (payload) => {
    return axios.post(`/connection/facebook/auto-connect/auto`, payload).catch(handleApiError)
  },

  // Auto-connect selected Facebook pages (client-side selection)
  autoConnectFacebookClient: (payload) => {
    return axios.post(`/connection/facebook/auto-connect/client`, payload).catch(handleApiError)
  }
}
