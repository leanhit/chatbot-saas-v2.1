import api from '@/plugins/axios'

export const authApi = {
  login: (credentials) => api.post('/identity/login', credentials).then(r => r.data),
  register: (userData) => api.post('/identity/register', userData).then(r => r.data),
  getCurrentUser: () => api.get('/identity/me').then(r => r.data),
  refresh: (refreshToken) => api.post('/identity/refresh', { refreshToken }).then(r => r.data)
}
