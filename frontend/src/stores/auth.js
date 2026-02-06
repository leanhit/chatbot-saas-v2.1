import { defineStore } from 'pinia'
import { authApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: localStorage.getItem('access_token'),
    refreshToken: localStorage.getItem('refreshToken'),
    isLoading: false,
    error: null
  }),

  getters: {
    isAuthenticated: (state) => !!state.token
  },

  actions: {
    async login(credentials) {
      this.isLoading = true
      this.error = null
      
      try {
        const result = await authApi.login(credentials)
        const { token, refreshToken, user } = result
        
        console.log('[AUTH STORE] Login response:', { token, refreshToken, user });
        
        this.token = token
        this.refreshToken = refreshToken
        this.user = user
        
        localStorage.setItem('access_token', token)
        localStorage.setItem('refreshToken', refreshToken)
        
        console.log('[AUTH STORE] token saved:', token)
        console.log('[AUTH STORE] Token set in state:', this.token);
        
        return { token, refreshToken, user }
      } catch (error) {
        this.error = error.response?.data?.message || 'Login failed'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async register(userData) {
      this.isLoading = true
      this.error = null
      
      try {
        await authApi.register(userData)
        
        // No token handling for register - SaaS-correct behavior
        return { success: true }
      } catch (error) {
        this.error = error.response?.data?.message || 'Registration failed'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async loadCurrentUser() {
      console.log('[AUTH STORE] loadCurrentUser called, token exists:', !!this.token);
      if (!this.token) return
      
      try {
        console.log('[AUTH STORE] Calling getCurrentUser with token:', this.token.substring(0, 20) + '...');
        this.user = await authApi.getCurrentUser()
        console.log('[AUTH STORE] Current user loaded:', this.user);
        return this.user
      } catch (error) {
        console.error('[AUTH STORE] loadCurrentUser failed:', error);
        this.logout()
        throw error
      }
    },

    async refreshTokens() {
      if (!this.refreshToken) throw new Error('No refresh token')
      
      try {
        const result = await authApi.refresh(this.refreshToken)
        const { token, refreshToken } = result
        
        this.token = token
        this.refreshToken = refreshToken
        
        localStorage.setItem('access_token', token)
        localStorage.setItem('refreshToken', refreshToken)
        
        return { token, refreshToken }
      } catch (error) {
        this.logout()
        throw error
      }
    },

    logout() {
      this.user = null
      this.token = null
      this.refreshToken = null
      this.error = null
      
      localStorage.removeItem('access_token')
      localStorage.removeItem('refreshToken')
    },

    clearTokens() {
      this.token = null
      this.refreshToken = null
      this.error = null
      
      localStorage.removeItem('access_token')
      localStorage.removeItem('refreshToken')
      
      console.log('[AUTH STORE] Tokens cleared - ready for fresh login')
    }
  }
})
