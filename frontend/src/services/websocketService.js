import { useNotificationStore } from '@/stores/notification/notificationStore'
import { useAuthStore } from '@/stores/authStore'

class WebSocketService {
  constructor() {
    this.socket = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 1000
    this.reconnectInterval = 5000
    this.notificationStore = null
    this.token = null
    this.connectedToken = null
    this.connectedTenantKey = null
  }

  connect(token) {
    if (token) {
      this.token = token
    } else {
      token = this.token
    }

    if (!token) {
      console.warn('No authentication token available for WebSocket connection')
      return
    }

    const tenantKey = localStorage.getItem('active_tenant_id') || ''

    // Prevent duplicate connections if already connected or connecting with same token & tenantKey
    if (this.socket && 
        (this.socket.readyState === WebSocket.OPEN || this.socket.readyState === WebSocket.CONNECTING) && 
        this.connectedToken === token && 
        this.connectedTenantKey === tenantKey) {
      return
    }

    // Clean up any existing connection first
    if (this.socket) {
      console.log('🔄 Closing existing WebSocket connection before reconnecting...')
      try {
        this.socket.onopen = null
        this.socket.onmessage = null
        this.socket.onerror = null
        this.socket.onclose = null
        this.socket.close()
      } catch (e) {
        console.warn('Error closing WebSocket:', e)
      }
      this.socket = null
    }

    this.connectedToken = token
    this.connectedTenantKey = tenantKey
    this.notificationStore = useNotificationStore()

    // Extract base URL from VUE_APP_WS_URL (e.g. ws://localhost:8080/ws/takeover -> ws://localhost:8080)
    let baseUrl = process.env.VUE_APP_WS_URL || process.env.VITE_WS_URL
    const isBrowserNonLocalhost = typeof window !== 'undefined' && window.location.hostname !== 'localhost' && window.location.hostname !== '127.0.0.1'
    if (!baseUrl || (baseUrl.includes('localhost') && isBrowserNonLocalhost)) {
      const protocol = typeof window !== 'undefined' && window.location.protocol === 'https:' ? 'wss:' : 'ws:'
      const host = typeof window !== 'undefined' ? window.location.host : 'localhost:8080'
      baseUrl = `${protocol}//${host}`
    }
    if (baseUrl.includes('/ws/takeover')) {
      baseUrl = baseUrl.replace('/ws/takeover', '')
    }
    if (baseUrl.endsWith('/')) {
      baseUrl = baseUrl.slice(0, -1)
    }

    const wsUrl = `${baseUrl}/ws/notifications?token=${encodeURIComponent(token)}&tenantKey=${encodeURIComponent(tenantKey)}`
    
    try {
      const currentSocket = new WebSocket(wsUrl)
      this.socket = currentSocket
      
      currentSocket.onopen = () => {
        if (this.socket !== currentSocket) return
        this.reconnectAttempts = 0
        this.notificationStore.isConnected = true
        console.log('✅ WebSocket connected successfully')
      }

      currentSocket.onmessage = (event) => {
        if (this.socket !== currentSocket) return
        this.handleMessage(event.data)
      }

      currentSocket.onclose = (event) => {
        if (this.socket !== currentSocket) return
        this.notificationStore.isConnected = false
        console.warn('⚠️ WebSocket closed:', event.code, event.reason)
        this.attemptReconnect()
      }

      currentSocket.onerror = (error) => {
        if (this.socket !== currentSocket) return
        console.error('❌ WebSocket error:', error)
        this.notificationStore.isConnected = false
        this.notificationStore.addNotification({
          type: 'error',
          title: 'Connection Error',
          message: 'Failed to connect to notification server. Retrying...'
        })
      }
    } catch (error) {
      console.error('❌ Failed to connect WebSocket:', error)
      this.notificationStore.addNotification({
        type: 'error',
        title: 'Connection Failed',
        message: 'Unable to establish WebSocket connection'
      })
      this.attemptReconnect()
    }
  }

  handleMessage(data) {
    try {
      const message = JSON.parse(data)
      
      switch (message.type) {
        case 'TENANT_INVITATION':
          this.notificationStore.handleTenantInvitation(message.data)
          break
        case 'TENANT_JOIN_REQUEST':
          this.notificationStore.handleJoinRequest(message.data)
          break
        case 'TENANT_INVITATION_ACCEPTED':
          this.notificationStore.handleInvitationAccepted(message.data)
          break
        case 'TENANT_JOIN_REQUEST_APPROVED':
          this.notificationStore.handleJoinRequestApproved(message.data)
          break
        case 'CONVERSATION_MESSAGE':
          this.notificationStore.handleConversationMessage(message.data)
          break
        case 'TENANT_MEMBER_ADDED':
          this.notificationStore.addNotification({
            type: 'success',
            title: 'New Member',
            message: `${message.data.memberName || 'A new member'} has joined "${message.data.tenantName}"`,
            data: message.data
          })
          break
        case 'TENANT_MEMBER_REMOVED':
          this.notificationStore.addNotification({
            type: 'warning',
            title: 'Member Removed',
            message: `${message.data.memberName || 'A member'} has been removed from "${message.data.tenantName}"`,
            data: message.data
          })
          break
        case 'TENANT_ROLE_CHANGED':
          this.notificationStore.addNotification({
            type: 'info',
            title: 'Role Changed',
            message: `Your role in "${message.data.tenantName}" has been changed to ${message.data.newRole}`,
            data: message.data
          })
          break
        case 'TENANT_UPDATED':
          this.notificationStore.addNotification({
            type: 'info',
            title: 'Tenant Updated',
            message: `"${message.data.tenantName}" has been updated`,
            data: message.data
          })
          break
        case 'PAYMENT_SUCCESS':
          this.notificationStore.addNotification({
            type: 'success',
            title: 'Payment Successful',
            message: `Your payment of ${message.data.amount} has been processed successfully`,
            data: message.data
          })
          break
        case 'PAYMENT_FAILED':
          this.notificationStore.addNotification({
            type: 'error',
            title: 'Payment Failed',
            message: `Payment processing failed: ${message.data.reason || 'Unknown error'}`,
            data: message.data
          })
          break
        case 'LICENSE_EXPIRED':
          this.notificationStore.addNotification({
            type: 'error',
            title: 'License Expired',
            message: `Your license for "${message.data.tenantName}" has expired. Please renew to continue service.`,
            data: message.data
          })
          break
        case 'SYSTEM_ALERT':
          this.notificationStore.addNotification({
            type: 'warning',
            title: 'System Alert',
            message: message.data.message || 'System alert',
            data: message.data
          })
          break
        default:
          this.notificationStore.addNotification({
            type: 'info',
            title: 'Notification',
            message: message.data?.message || message.message || 'You have a new notification',
            data: message.data
          })
      }
    } catch (error) {
      console.error('Error handling WebSocket message:', error)
    }
  }

  attemptReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts && this.token) {
      this.reconnectAttempts++
      // Exponential backoff for better stability (max 30s)
      const backoffDelay = Math.min(1000 * Math.pow(2, this.reconnectAttempts - 1), 30000)
      
      console.warn(`⚠️ WebSocket reconnecting attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts} in ${backoffDelay}ms...`)
      setTimeout(async () => {
        try {
          // Check if token is expired before reconnecting
          const authStore = useAuthStore()
          let currentToken = authStore.token

          if (currentToken && this._isTokenExpired(currentToken)) {
            console.log('🔄 [WebSocket] Token expired, refreshing before reconnect...')
            const newToken = await authStore.refreshAccessToken()
            if (newToken) {
              currentToken = newToken
              console.log('✅ [WebSocket] Token refreshed successfully')
            } else {
              console.error('❌ [WebSocket] Token refresh failed, stopping reconnect')
              this.token = null
              return
            }
          }

          // Use the latest token for reconnection
          this.connectedToken = null // Force new connection
          this.connect(currentToken)
        } catch (err) {
          console.error('❌ [WebSocket] Error during reconnect:', err)
        }
      }, backoffDelay)
    } else if (!this.token) {
      console.log('ℹ️ WebSocket reconnection aborted (no token/logged out)')
    } else {
      console.error('❌ WebSocket reconnection failed after maximum attempts')
    }
  }

  /**
   * Check if a JWT token is expired (with 60s buffer)
   */
  _isTokenExpired(token) {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]))
      const expMs = payload.exp * 1000
      // Consider expired if less than 60 seconds remaining
      return Date.now() >= (expMs - 60000)
    } catch (e) {
      // If we can't decode, assume expired to trigger refresh
      return true
    }
  }

  disconnect() {
    this.token = null // Clear token to prevent reconnecting
    this.connectedToken = null
    this.connectedTenantKey = null
    if (this.socket) {
      try {
        this.socket.onopen = null
        this.socket.onmessage = null
        this.socket.onerror = null
        this.socket.onclose = null
        this.socket.close()
      } catch (e) {
        console.warn('Error closing WebSocket:', e)
      }
      this.socket = null
    }
    if (this.notificationStore) {
      this.notificationStore.isConnected = false
    }
  }

  send(message) {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      this.socket.send(JSON.stringify(message))
    } else {
      console.warn('WebSocket not connected')
    }
  }
}

// Create singleton instance
export const websocketService = new WebSocketService()

export default websocketService
