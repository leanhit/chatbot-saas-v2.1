// Auth API Service
// This service handles all authentication related API calls
import axios from '@/plugins/axios'
class AuthAPI {
  // Login API call
  async login(credentials) {
    try {
      const response = await axios.post('/auth/login', credentials)
      return response
    } catch (error) {
      throw error
    }
  }
  // Register API call
  async register(userData) {
    try {
      const response = await axios.post('/auth/register', userData)
      return response
    } catch (error) {
      throw error
    }
  }
  // Logout API call
  async logout() {
    try {
      const response = await axios.post('/auth/logout')
      // Clear local storage regardless of API response
      localStorage.removeItem('accessToken')
      localStorage.removeItem('user')
      return response
    } catch (error) {
      // Still clear local storage on error
      localStorage.removeItem('accessToken')
      localStorage.removeItem('user')
      throw error
    }
  }
  // Get current user profile
  async getCurrentUser() {
    try {
      const response = await axios.get('/auth/me')
      return response
    } catch (error) {
      throw error
    }
  }
  // Refresh token
  async refreshToken() {
    try {
      const response = await axios.post('/auth/refresh')
      // Update token in localStorage
      if (response.data.token) {
        localStorage.setItem('accessToken', response.data.token)
      }
      return response
    } catch (error) {
      // Clear local storage on refresh failure
      localStorage.removeItem('accessToken')
      localStorage.removeItem('user')
      throw error
    }
  }
  // Forgot password
  async forgotPassword(email) {
    try {
      const response = await axios.post('/auth/forgot-password', { email })
      return response
    } catch (error) {
      throw error
    }
  }
  // Reset password
  async resetPassword(token, newPassword) {
    try {
      const response = await axios.post('/auth/reset-password', {
        token,
        password: newPassword,
      })
      return response
    } catch (error) {
      throw error
    }
  }
  // Change password
  async changePassword(currentPassword, newPassword) {
    try {
      const response = await axios.post('/auth/change-password', {
        currentPassword,
        newPassword,
      })
      return response
    } catch (error) {
      throw error
    }
  }
  // Check email availability
  async checkEmail(email) {
    try {
      const response = await axios.get('/auth/check-email', {
        params: { email }
      })
      return response
    } catch (error) {
      throw error
    }
  }
  // Check username availability
  async checkUsername(username) {
    try {
      const response = await axios.get('/auth/check-username', {
        params: { username }
      })
      return response
    } catch (error) {
      throw error
    }
  }
}
// Create and export singleton instance
const authAPI = new AuthAPI()
export default authAPI
