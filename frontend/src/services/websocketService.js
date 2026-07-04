import { useNotificationStore } from '@/stores/notification/notificationStore'

class WebSocketService {
  constructor() {
    this.socket = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 1000
    this.reconnectInterval = 5000
    this.notificationStore = null
    this.token = null
  }

  connect(token) {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      return
    }

    if (token) {
      this.token = token
    } else {
      token = this.token
    }

    if (!token) {
      console.warn('No authentication token available for WebSocket connection')
      return
    }

    this.notificationStore = useNotificationStore()

    // Extract base URL from VUE_APP_WS_URL (e.g. ws://localhost:8080/ws/takeover -> ws://localhost:8080)
    let baseUrl = process.env.VUE_APP_WS_URL || 'ws://localhost:8080'
    if (baseUrl.includes('/ws/takeover')) {
      baseUrl = baseUrl.replace('/ws/takeover', '')
    }
    if (baseUrl.endsWith('/')) {
      baseUrl = baseUrl.slice(0, -1)
    }

    const tenantKey = localStorage.getItem('active_tenant_id') || ''
    const wsUrl = `${baseUrl}/ws/notifications?token=${encodeURIComponent(token)}&tenantKey=${encodeURIComponent(tenantKey)}`
    
    try {
      this.socket = new WebSocket(wsUrl)
      
      this.socket.onopen = () => {
        this.reconnectAttempts = 0
        this.notificationStore.isConnected = true
        console.log('✅ WebSocket connected successfully')
      }

      this.socket.onmessage = (event) => {
        this.handleMessage(event.data)
      }

      this.socket.onclose = (event) => {
        this.notificationStore.isConnected = false
        console.warn('⚠️ WebSocket closed:', event.code, event.reason)
        this.attemptReconnect()
      }

      this.socket.onerror = (error) => {
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
      setTimeout(() => {
        this.connect()
      }, backoffDelay)
    } else if (!this.token) {
      console.log('ℹ️ WebSocket reconnection aborted (no token/logged out)')
    } else {
      console.error('❌ WebSocket reconnection failed after maximum attempts')
    }
  }

  disconnect() {
    this.token = null // Clear token to prevent reconnecting
    if (this.socket) {
      this.socket.close()
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
