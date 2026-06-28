import { ref, reactive } from 'vue'

/**
 * Presence WebSocket Service
 * Tracks online/offline status of team members in real-time
 */
class PresenceWebSocketService {
  constructor() {
    this.socket = null
    this.connectionStatus = ref('disconnected') // disconnected, connecting, connected, error
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = 3000
    this.heartbeatInterval = null
    this.onlineMembers = ref(new Map()) // userId -> member info
    this.tenantKey = null
    
    // Event callbacks
    this.onMemberOnline = null
    this.onMemberOffline = null
    this.onConnectionStatusChanged = null
  }

  /**
   * Connect to Presence WebSocket
   */
  async connect(tenantKey) {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      if (this.tenantKey === tenantKey) {
        return // Already connected to this tenant
      }
      this.disconnect() // Disconnect from previous tenant
    }

    this.tenantKey = tenantKey
    this.connectionStatus.value = 'connecting'

    try {
      let wsUrl = process.env.VUE_APP_PRESENCE_WS_URL
      if (!wsUrl) {
        const baseWsUrl = process.env.VUE_APP_WS_URL || 'ws://localhost:8080/ws/presence'
        if (baseWsUrl.includes('/ws/takeover')) {
          wsUrl = baseWsUrl.replace('/ws/takeover', '/ws/presence')
        } else if (baseWsUrl.includes('/ws/')) {
          const index = baseWsUrl.lastIndexOf('/ws/')
          wsUrl = baseWsUrl.substring(0, index) + '/ws/presence'
        } else {
          wsUrl = 'ws://localhost:8080/ws/presence'
        }
      }
      
      if (!wsUrl || typeof wsUrl !== 'string') {
        throw new Error('Invalid WebSocket URL')
      }
      
      const cleanUrl = wsUrl.split('#')[0].trim()

      // Append JWT token and tenant key for handshake authentication
      const { useAuthStore } = await import('@/stores/authStore')
      const authStore = useAuthStore()
      const token = authStore.token || ''
      const separator = cleanUrl.includes('?') ? '&' : '?'
      const finalUrl = `${cleanUrl}${separator}token=${encodeURIComponent(token)}&tenantKey=${encodeURIComponent(tenantKey)}`
      
      this.socket = new WebSocket(finalUrl)
      
      this.socket.onopen = () => {
        this.connectionStatus.value = 'connected'
        this.reconnectAttempts = 0
        
        // Start heartbeat
        this.startHeartbeat()
        
        // Notify connection status change
        if (this.onConnectionStatusChanged) {
          this.onConnectionStatusChanged('connected')
        }
        
        console.log('✅ Presence WebSocket connected for tenant:', tenantKey)
      }

      this.socket.onmessage = (event) => {
        this.handleMessage(event.data)
      }

      this.socket.onclose = (event) => {
        this.connectionStatus.value = 'disconnected'
        this.stopHeartbeat()
        
        // Notify connection status change
        if (this.onConnectionStatusChanged) {
          this.onConnectionStatusChanged('disconnected')
        }
        
        console.log('❌ Presence WebSocket disconnected:', event.code)
        
        // Attempt reconnection
        this.attemptReconnect()
      }

      this.socket.onerror = (error) => {
        console.error('🚨 Presence WebSocket error:', error)
        this.connectionStatus.value = 'error'
        
        if (this.onConnectionStatusChanged) {
          this.onConnectionStatusChanged('error')
        }
      }

    } catch (error) {
      console.error('🚨 Failed to connect Presence WebSocket:', error)
      this.connectionStatus.value = 'error'
      this.attemptReconnect()
    }
  }

  /**
   * Handle incoming WebSocket messages
   */
  handleMessage(data) {
    try {
      const message = JSON.parse(data)
      
      switch (message.type) {
        case 'MEMBER_ONLINE':
          this.handleMemberOnline(message.data)
          break
        case 'MEMBER_OFFLINE':
          this.handleMemberOffline(message.data)
          break
        case 'HEARTBEAT':
          // Respond to heartbeat
          this.send({ type: 'HEARTBEAT_PONG', timestamp: Date.now() })
          break
        case 'HEARTBEAT_PONG':
          // Handle heartbeat response from server
          break
        default:
          console.log('Unknown message type:', message.type)
      }
    } catch (error) {
      console.error('🚨 Error handling Presence WebSocket message:', error)
    }
  }

  /**
   * Handle member coming online
   */
  handleMemberOnline(data) {
    const { userId, email, fullName, timestamp } = data
    
    this.onlineMembers.value.set(userId, {
      userId,
      email,
      fullName,
      timestamp,
      status: 'online'
    })
    
    if (this.onMemberOnline) {
      this.onMemberOnline(data)
    }
    
    console.log('✅ Member online:', fullName)
  }

  /**
   * Handle member going offline
   */
  handleMemberOffline(data) {
    const { userId, timestamp } = data
    
    this.onlineMembers.value.delete(userId)
    
    if (this.onMemberOffline) {
      this.onMemberOffline(data)
    }
    
    console.log('❌ Member offline:', userId)
  }

  /**
   * Send message through WebSocket
   */
  send(data) {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      const message = typeof data === 'string' ? data : JSON.stringify(data)
      this.socket.send(message)
      return true
    } else {
      console.warn('⚠️ Presence WebSocket not connected, message not sent:', data)
      return false
    }
  }

  /**
   * Start heartbeat to keep connection alive
   */
  startHeartbeat() {
    this.stopHeartbeat()
    this.heartbeatInterval = setInterval(() => {
      if (this.socket && this.socket.readyState === WebSocket.OPEN) {
        this.send({ type: 'HEARTBEAT', timestamp: Date.now() })
      }
    }, 30000) // 30 seconds
  }

  /**
   * Stop heartbeat
   */
  stopHeartbeat() {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval)
      this.heartbeatInterval = null
    }
  }

  /**
   * Attempt to reconnect WebSocket
   */
  attemptReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts && this.tenantKey) {
      this.reconnectAttempts++
      this.connectionStatus.value = 'connecting'
      
      const backoffDelay = Math.min(1000 * Math.pow(2, this.reconnectAttempts - 1), 30000)
      
      setTimeout(() => {
        this.connect(this.tenantKey)
      }, backoffDelay)
    } else {
      this.connectionStatus.value = 'error'
    }
  }

  /**
   * Disconnect from WebSocket
   */
  disconnect() {
    this.stopHeartbeat()
    
    if (this.socket) {
      this.socket.close()
      this.socket = null
    }
    
    this.connectionStatus.value = 'disconnected'
    this.tenantKey = null
    
    // Clear online members
    this.onlineMembers.value.clear()
  }

  /**
   * Get all online members
   */
  getOnlineMembers() {
    return Array.from(this.onlineMembers.value.values())
  }

  /**
   * Get connection status
   */
  getConnectionStatus() {
    return this.connectionStatus.value
  }

  /**
   * Check if member is online
   */
  isMemberOnline(userId) {
    return this.onlineMembers.value.has(userId)
  }
}

// Create singleton instance
export const presenceWebSocketService = new PresenceWebSocketService()

export default presenceWebSocketService
