import { ref, reactive } from 'vue'
import { useNotificationStore } from '@/stores/notification/notificationStore'

/**
 * Advanced Takeover WebSocket Service
 * Supports real-time messaging, multi-agent scenarios, connection status, and delivery confirmations
 */
class TakeoverWebSocketService {
  constructor() {
    this.socket = null
    this.currentConversationId = ref(null)
    this.connectionStatus = ref('disconnected') // disconnected, connecting, connected, error
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = 3000
    this.heartbeatInterval = null
    this.messageQueue = ref([])
    this.deliveryConfirmations = reactive(new Map())
    this.activeAgents = ref(new Map()) // conversationId -> Set of agent info
    this.typingIndicators = ref(new Map()) // conversationId -> Set of typing agents
    this.notificationStore = null
    
    // Event callbacks
    this.onMessageReceived = null
    this.onConnectionStatusChanged = null
    this.onAgentJoined = null
    this.onAgentLeft = null
    this.onTypingStarted = null
    this.onTypingStopped = null
    this.onMessageDelivered = null
    this.onMessageRead = null
  }

  /**
   * Connect to WebSocket and start tracking conversation
   */
  async connect(conversationId) {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      if (this.currentConversationId.value === conversationId) {
        return // Already connected to this conversation
      }
      this.disconnect() // Disconnect from previous conversation
    }

    this.currentConversationId.value = conversationId
    this.connectionStatus.value = 'connecting'
    this.notificationStore = useNotificationStore()

    try {
      // VITE_WS_URL should already include the full WebSocket endpoint
      const wsUrl = process.env.VITE_WS_URL || 'ws://localhost:8080/ws/takeover'
      
      // Validate and clean URL
      if (!wsUrl || typeof wsUrl !== 'string') {
        throw new Error('Invalid WebSocket URL')
      }
      
      // Remove any potential fragments or invalid characters
      const cleanUrl = wsUrl.split('#')[0].trim()
      
      console.log('🔌 Connecting to WebSocket:', cleanUrl)
      console.log('Conversation ID:', conversationId)
      
      this.socket = new WebSocket(cleanUrl)
      
      this.socket.onopen = () => {
        console.log(`✅ Takeover WebSocket connected for conversation: ${conversationId}`)
        this.connectionStatus.value = 'connected'
        this.reconnectAttempts = 0
        
        // Send conversation ID to start tracking
        this.send(conversationId)
        
        // Start heartbeat
        this.startHeartbeat()
        
        // Notify connection status change
        if (this.onConnectionStatusChanged) {
          this.onConnectionStatusChanged('connected')
        }
      }

      this.socket.onmessage = (event) => {
        this.handleMessage(event.data)
      }

      this.socket.onclose = (event) => {
        console.log(`❌ Takeover WebSocket disconnected: ${event.code} - ${event.reason}`)
        this.connectionStatus.value = 'disconnected'
        this.stopHeartbeat()
        
        // Clean up agents for this conversation
        this.activeAgents.value.delete(conversationId)
        this.typingIndicators.value.delete(conversationId)
        
        // Notify connection status change
        if (this.onConnectionStatusChanged) {
          this.onConnectionStatusChanged('disconnected')
        }
        
        // Analyze close code for better handling
        const closeCode = event.code
        if (closeCode === 1000) {
          console.log('📝 Normal closure - client initiated close')
        } else if (closeCode === 1001) {
          console.log('🏃 Endpoint going away - server initiated close')
        } else if (closeCode === 1006) {
          console.log('🚫 Abnormal closure - connection lost')
        } else {
          console.log(`❓ Unexpected close code: ${closeCode}`)
        }
        
        // Attempt reconnection
        this.attemptReconnect()
      }

      this.socket.onerror = (error) => {
        console.error('🚨 Takeover WebSocket error:', error)
        this.connectionStatus.value = 'error'
        
        if (this.onConnectionStatusChanged) {
          this.onConnectionStatusChanged('error')
        }
      }

    } catch (error) {
      console.error('🚨 Failed to connect Takeover WebSocket:', error)
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
      
      // Handle different message types
      switch (message.type) {
        case 'CONVERSATION_MESSAGE':
          this.handleConversationMessage(message.data)
          break
        case 'TAKEOVER_MESSAGE':
          this.handleTakeoverMessage(message.data)
          break
        case 'AGENT_JOINED':
          this.handleAgentJoined(message.data)
          break
        case 'AGENT_LEFT':
          this.handleAgentLeft(message.data)
          break
        case 'TYPING_STARTED':
          this.handleTypingStarted(message.data)
          break
        case 'TYPING_STOPPED':
          this.handleTypingStopped(message.data)
          break
        case 'MESSAGE_DELIVERED':
          this.handleMessageDelivered(message.data)
          break
        case 'MESSAGE_READ':
          this.handleMessageRead(message.data)
          break
        case 'HEARTBEAT':
          // Respond to heartbeat
          this.send({ type: 'HEARTBEAT_PONG', timestamp: Date.now() })
          break
        case 'HEARTBEAT_PONG':
          // Handle heartbeat response from server
          console.log('💓 Received heartbeat PONG from server')
          break
        default:
          console.log('📨 Unknown message type:', message.type)
      }
    } catch (error) {
      console.error('🚨 Error handling WebSocket message:', error)
    }
  }

  /**
   * Handle real-time conversation messages from Facebook/Bot
   */
  handleConversationMessage(message) {
    console.log(`📨 New conversation message:`, message)
    
    // Add message to queue
    this.messageQueue.value.push({
      ...message,
      receivedAt: Date.now(),
      isRealtime: true
    })
    
    // Add delivery confirmation
    if (message.id) {
      this.deliveryConfirmations.set(message.id, {
        status: 'delivered',
        timestamp: Date.now()
      })
    }
    
    // Notify message received
    if (this.onMessageReceived) {
      this.onMessageReceived(message)
    }
    
    // Show notification for new messages
    if (message.sender === 'user' && this.currentConversationId.value === message.conversationId) {
      this.notificationStore.addNotification({
        type: 'info',
        title: 'New Message',
        message: `New message from ${message.sender === 'user' ? 'User' : message.sender}`,
        duration: 3000
      })
    }
  }

  /**
   * Handle real-time takeover messages
   */
  handleTakeoverMessage(message) {
    console.log(`📨 New real-time message:`, message)
    
    // Add message to queue
    this.messageQueue.value.push({
      ...message,
      receivedAt: Date.now(),
      isRealtime: true
    })
    
    // Add delivery confirmation
    if (message.id) {
      this.deliveryConfirmations.set(message.id, {
        status: 'delivered',
        timestamp: Date.now()
      })
    }
    
    // Notify message received
    if (this.onMessageReceived) {
      this.onMessageReceived(message)
    }
    
    // Show notification for new messages
    if (message.sender === 'user' && this.currentConversationId.value === message.conversationId) {
      this.notificationStore.addNotification({
        type: 'info',
        title: 'New Message',
        message: `New message from ${message.sender === 'user' ? 'User' : message.sender}`,
        duration: 3000
      })
    }
  }

  /**
   * Handle agent joining conversation
   */
  handleAgentJoined(data) {
    const { conversationId, agent } = data
    
    if (!this.activeAgents.value.has(conversationId)) {
      this.activeAgents.value.set(conversationId, new Set())
    }
    this.activeAgents.value.get(conversationId).add(agent)
    
    console.log(`👤 Agent joined conversation ${conversationId}:`, agent)
    
    if (this.onAgentJoined) {
      this.onAgentJoined(conversationId, agent)
    }
  }

  /**
   * Handle agent leaving conversation
   */
  handleAgentLeft(data) {
    const { conversationId, agent } = data
    
    if (this.activeAgents.value.has(conversationId)) {
      this.activeAgents.value.get(conversationId).delete(agent)
      if (this.activeAgents.value.get(conversationId).size === 0) {
        this.activeAgents.value.delete(conversationId)
      }
    }
    
    console.log(`👤 Agent left conversation ${conversationId}:`, agent)
    
    if (this.onAgentLeft) {
      this.onAgentLeft(conversationId, agent)
    }
  }

  /**
   * Handle typing started indicator
   */
  handleTypingStarted(data) {
    const { conversationId, agent } = data
    
    if (!this.typingIndicators.value.has(conversationId)) {
      this.typingIndicators.value.set(conversationId, new Set())
    }
    this.typingIndicators.value.get(conversationId).add(agent)
    
    if (this.onTypingStarted) {
      this.onTypingStarted(conversationId, agent)
    }
  }

  /**
   * Handle typing stopped indicator
   */
  handleTypingStopped(data) {
    const { conversationId, agent } = data
    
    if (this.typingIndicators.value.has(conversationId)) {
      this.typingIndicators.value.get(conversationId).delete(agent)
      if (this.typingIndicators.value.get(conversationId).size === 0) {
        this.typingIndicators.value.delete(conversationId)
      }
    }
    
    if (this.onTypingStopped) {
      this.onTypingStopped(conversationId, agent)
    }
  }

  /**
   * Handle message delivery confirmation
   */
  handleMessageDelivered(data) {
    const { messageId, timestamp } = data
    
    if (this.deliveryConfirmations.has(messageId)) {
      this.deliveryConfirmations.get(messageId).status = 'delivered'
      this.deliveryConfirmations.get(messageId).deliveredAt = timestamp
    }
    
    if (this.onMessageDelivered) {
      this.onMessageDelivered(messageId, timestamp)
    }
  }

  /**
   * Handle message read confirmation
   */
  handleMessageRead(data) {
    const { messageId, timestamp } = data
    
    if (this.deliveryConfirmations.has(messageId)) {
      this.deliveryConfirmations.get(messageId).status = 'read'
      this.deliveryConfirmations.get(messageId).readAt = timestamp
    }
    
    if (this.onMessageRead) {
      this.onMessageRead(messageId, timestamp)
    }
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
      console.warn('⚠️ Takeover WebSocket not connected, message not sent:', data)
      return false
    }
  }

  /**
   * Send message with delivery tracking
   */
  sendMessage(message) {
    const messageWithId = {
      ...message,
      id: this.generateMessageId(),
      timestamp: Date.now(),
      sender: 'agent'
    }
    
    const sent = this.send(messageWithId)
    
    if (sent && messageWithId.id) {
      // Track message for delivery confirmation
      this.deliveryConfirmations.set(messageWithId.id, {
        status: 'sent',
        timestamp: Date.now()
      })
    }
    
    return sent ? messageWithId : null
  }

  /**
   * Send typing indicator
   */
  sendTypingStart(conversationId) {
    this.send({
      type: 'TYPING_STARTED',
      data: {
        conversationId,
        agent: this.getCurrentAgentInfo()
      }
    })
  }

  /**
   * Send typing stop indicator
   */
  sendTypingStop(conversationId) {
    this.send({
      type: 'TYPING_STOPPED',
      data: {
        conversationId,
        agent: this.getCurrentAgentInfo()
      }
    })
  }

  /**
   * Mark message as read
   */
  markMessageRead(messageId) {
    this.send({
      type: 'MESSAGE_READ',
      data: {
        messageId,
        timestamp: Date.now()
      }
    })
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
    if (this.reconnectAttempts < this.maxReconnectAttempts && this.currentConversationId.value) {
      this.reconnectAttempts++
      this.connectionStatus.value = 'connecting'
      
      // Exponential backoff for better stability
      const backoffDelay = Math.min(1000 * Math.pow(2, this.reconnectAttempts - 1), 30000)
      
      console.log(`🔄 Attempting to reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts}) in ${backoffDelay}ms`)
      
      setTimeout(() => {
        this.connect(this.currentConversationId.value)
      }, backoffDelay)
    } else {
      console.log('❌ Max reconnection attempts reached')
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
    this.currentConversationId.value = null
    
    // Clean up
    this.activeAgents.value.clear()
    this.typingIndicators.value.clear()
    this.messageQueue.value = []
  }

  /**
   * Get current agent info
   */
  getCurrentAgentInfo() {
    // This should get current user info from auth store
    return {
      id: localStorage.getItem('userId') || 'unknown',
      name: localStorage.getItem('userName') || 'Agent',
      avatar: localStorage.getItem('userAvatar') || null
    }
  }

  /**
   * Generate unique message ID
   */
  generateMessageId() {
    return `msg_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  }

  /**
   * Get active agents for conversation
   */
  getActiveAgents(conversationId) {
    return Array.from(this.activeAgents.value.get(conversationId) || [])
  }

  /**
   * Get typing agents for conversation
   */
  getTypingAgents(conversationId) {
    return Array.from(this.typingIndicators.value.get(conversationId) || [])
  }

  /**
   * Get message delivery status
   */
  getMessageDeliveryStatus(messageId) {
    return this.deliveryConfirmations.get(messageId) || null
  }

  /**
   * Get connection status
   */
  getConnectionStatus() {
    return this.connectionStatus.value
  }

  /**
   * Check if connected to specific conversation
   */
  isConnectedTo(conversationId) {
    return this.connectionStatus.value === 'connected' && 
           this.currentConversationId.value === conversationId
  }
}

// Create singleton instance
export const takeoverWebSocketService = new TakeoverWebSocketService()

export default takeoverWebSocketService
