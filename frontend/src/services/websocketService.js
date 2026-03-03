import { useNotificationStore } from '@/stores/notification/notificationStore'

class WebSocketService {
  constructor() {
    this.socket = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = 5000
    this.notificationStore = null
  }

  connect(token) {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      return
    }

    this.notificationStore = useNotificationStore()

    // WebSocket URL - adjust based on your backend configuration
    const wsUrl = `${process.env.VITE_WS_URL || 'ws://localhost:8080'}/ws/notifications?token=${token}`
    
    try {
      this.socket = new WebSocket(wsUrl)
      
      this.socket.onopen = () => {
        console.log('WebSocket connected')
        this.reconnectAttempts = 0
        this.notificationStore.isConnected = true
      }

      this.socket.onmessage = (event) => {
        this.handleMessage(event.data)
      }

      this.socket.onclose = () => {
        console.log('WebSocket disconnected')
        this.notificationStore.isConnected = false
        this.attemptReconnect()
      }

      this.socket.onerror = (error) => {
        console.error('WebSocket error:', error)
        this.notificationStore.isConnected = false
      }
    } catch (error) {
      console.error('Failed to connect WebSocket:', error)
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
        default:
          console.log('Unknown notification type:', message.type)
          this.notificationStore.addNotification({
            type: 'info',
            title: 'Notification',
            message: message.message || 'You have a new notification'
          })
      }
    } catch (error) {
      console.error('Error handling WebSocket message:', error)
    }
  }

  attemptReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      console.log(`Attempting to reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts})`)
      
      setTimeout(() => {
        this.connect()
      }, this.reconnectInterval)
    } else {
      console.log('Max reconnection attempts reached')
    }
  }

  disconnect() {
    if (this.socket) {
      this.socket.close()
      this.socket = null
    }
    this.notificationStore.isConnected = false
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
