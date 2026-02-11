// Penny API Service
import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

// Create axios instance with default config
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Add auth token to requests
apiClient.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    
    // Add tenant header if available
    if (authStore.currentTenant) {
      config.headers['X-Tenant-Key'] = authStore.currentTenant.key
    }
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Handle response errors
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const authStore = useAuthStore()
      authStore.logout()
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

const handleApiError = (error) => {
  console.error('API Error:', error)
  throw error
}

export const pennyApi = {
  // Bot Management
  getBots: () => apiClient.get('/api/penny/bots'),
  createBot: (botData) => apiClient.post('/api/penny/bots', botData),
  updateBot: (id, botData) => apiClient.put(`/api/penny/bots/${id}`, botData),
  deleteBot: (id) => apiClient.delete(`/api/penny/bots/${id}`),
  toggleBotStatus: (id, isActive) => apiClient.patch(`/api/penny/bots/${id}/status`, { isActive }),
  
  // Bot Analytics
  getBotAnalytics: (id) => apiClient.get(`/api/penny/bots/${id}/analytics`),
  
  // Chat
  sendMessage: (botId, message) => apiClient.post(`/api/penny/bots/${botId}/chat`, { message }),
  getChatHistory: (botId, page = 1, limit = 50) => 
    apiClient.get(`/api/penny/bots/${botId}/chat/history?page=${page}&limit=${limit}`),
  
  // Rules Management
  getRules: (botId) => apiClient.get(`/api/penny/bots/${botId}/rules`),
  createRule: (botId, ruleData) => apiClient.post(`/api/penny/bots/${botId}/rules`, ruleData),
  updateRule: (botId, ruleId, ruleData) => apiClient.put(`/api/penny/bots/${botId}/rules/${ruleId}`, ruleData),
  deleteRule: (botId, ruleId) => apiClient.delete(`/api/penny/bots/${botId}/rules/${ruleId}`),
  toggleRuleStatus: (botId, ruleId, isActive) => apiClient.patch(`/api/penny/bots/${botId}/rules/${ruleId}/status`, { isActive }),
  testRule: (botId, ruleId) => apiClient.post(`/api/penny/bots/${botId}/rules/${ruleId}/test`),
  
  // Connections Management
  getConnections: (botId) => apiClient.get(`/api/penny/bots/${botId}/connections`),
  createConnection: (botId, connectionData) => apiClient.post(`/api/penny/bots/${botId}/connections`, connectionData),
  updateConnection: (botId, connectionId, connectionData) => apiClient.put(`/api/penny/bots/${botId}/connections/${connectionId}`, connectionData),
  deleteConnection: (botId, connectionId) => apiClient.delete(`/api/penny/bots/${botId}/connections/${connectionId}`),
  testConnection: (botId, connectionId) => apiClient.post(`/api/penny/bots/${botId}/connections/${connectionId}/test`),
  checkConnectionHealth: (botId, connectionId) => apiClient.get(`/api/penny/bots/${botId}/connections/${connectionId}/health`)
}
