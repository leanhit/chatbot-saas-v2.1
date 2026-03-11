<template>
  <div class="messages-page p-4">
    <!-- Header -->
    <div class="mb-6">
      <div class="flex justify-between items-center">
        <div>
          <p class="uppercase text-xs text-gray-700 dark:text-gray-300 font-semibold">messages</p>
          <h1 class="text-2xl text-gray-900 dark:text-gray-200 font-medium">
            Conversation Management
          </h1>
        </div>
        <div class="flex gap-2">
          <button
            @click="refreshConversations"
            :disabled="loading"
            class="bg-white dark:bg-gray-800 hover:border-gray-200 dark:hover:bg-gray-700 dark:text-white dark:border-gray-700 border rounded py-2 px-5 flex items-center gap-2"
          >
            <Icon v-if="loading" icon="mdi:loading" class="animate-spin" />
            <Icon v-else icon="mdi:refresh" />
            Refresh
          </button>
          <button
            @click="showSearchModal = true"
            class="bg-primary border flex gap-2 text-white hover:bg-primary/80 dark:border-gray-700 rounded py-3 px-5"
          >
            <Icon icon="mdi:magnify" />
            Search
          </button>
        </div>
      </div>
    </div>

    <!-- Statistics Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <div class="bg-white dark:bg-gray-800 p-4 rounded-lg border dark:border-gray-700">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Total Conversations</p>
            <p class="text-2xl font-bold text-gray-900 dark:text-gray-200">{{ stats.totalConversations }}</p>
          </div>
          <div class="bg-blue-100 dark:bg-blue-900 p-3 rounded-full">
            <Icon icon="mdi:chat" class="text-blue-600 dark:text-blue-400 text-xl" />
          </div>
        </div>
      </div>
      
      <div class="bg-white dark:bg-gray-800 p-4 rounded-lg border dark:border-gray-700">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Active Takeovers</p>
            <p class="text-2xl font-bold text-gray-900 dark:text-gray-200">{{ stats.activeTakeovers }}</p>
          </div>
          <div class="bg-green-100 dark:bg-green-900 p-3 rounded-full">
            <Icon icon="mdi:hand-right" class="text-green-600 dark:text-green-400 text-xl" />
          </div>
        </div>
      </div>
      
      <div class="bg-white dark:bg-gray-800 p-4 rounded-lg border dark:border-gray-700">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Pending Messages</p>
            <p class="text-2xl font-bold text-gray-900 dark:text-gray-200">{{ stats.pendingMessages }}</p>
          </div>
          <div class="bg-yellow-100 dark:bg-yellow-900 p-3 rounded-full">
            <Icon icon="mdi:clock" class="text-yellow-600 dark:text-yellow-400 text-xl" />
          </div>
        </div>
      </div>
      
      <div class="bg-white dark:bg-gray-800 p-4 rounded-lg border dark:border-gray-700">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Today's Messages</p>
            <p class="text-2xl font-bold text-gray-900 dark:text-gray-200">{{ stats.todayMessages }}</p>
          </div>
          <div class="bg-purple-100 dark:bg-purple-900 p-3 rounded-full">
            <Icon icon="mdi:message" class="text-purple-600 dark:text-purple-400 text-xl" />
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="flex gap-4">
      <!-- Conversations List -->
      <div class="w-full lg:w-1/3 bg-white dark:bg-gray-800 rounded-lg border dark:border-gray-700">
        <div class="p-4 border-b dark:border-gray-700">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-lg font-semibold text-gray-900 dark:text-gray-200">Conversations</h2>
            <div class="flex items-center gap-2">
              <select v-model="filterBot" @change="loadConversations" 
                class="text-sm border rounded px-2 py-1 dark:bg-gray-700 dark:border-gray-600 dark:text-white">
                <option value="all">All Bots</option>
                <option v-for="bot in pennyBotStore.activeBots" :key="bot.id" :value="bot.id">
                  {{ bot.name || bot.botName || `Bot ${bot.id}` }}
                </option>
              </select>
              <select v-model="filterConnection" @change="loadConversations" 
                class="text-sm border rounded px-2 py-1 dark:bg-gray-700 dark:border-gray-600 dark:text-white">
                <option value="all">All Connections</option>
                <option v-for="connection in pennyConnectionStore.activeConnections" :key="connection.id" :value="connection.id">
                  {{ connection.name || connection.connectionName || `${connection.connectionType} - ${connection.id}` }}
                </option>
              </select>
            </div>
          </div>
          
          <!-- Search -->
          <div class="relative">
            <input
              v-model="searchQuery"
              @input="debouncedSearch"
              type="text"
              placeholder="Search conversations..."
              class="w-full pl-10 pr-4 py-2 border rounded-lg dark:bg-gray-700 dark:border-gray-600 dark:text-white"
            />
            <Icon icon="mdi:magnify" class="absolute left-3 top-2.5 text-gray-400" />
          </div>
        </div>
        
        <!-- Conversations List -->
        <div class="overflow-y-auto" style="max-height: 600px;">
          <div v-if="loadingConversations" class="p-8 text-center">
            <Icon icon="mdi:loading" class="animate-spin text-2xl text-gray-400" />
            <p class="mt-2 text-gray-500">Loading conversations...</p>
          </div>
          
          <div v-else-if="conversations.length === 0" class="p-8 text-center">
            <Icon icon="mdi:chat-off" class="text-4xl text-gray-300" />
            <p class="mt-2 text-gray-500">No conversations found</p>
          </div>
          
          <div v-else>
            <ConversationItem
              v-for="conversation in conversations"
              :key="conversation.id"
              :conversation="conversation"
              :is-selected="selectedConversation?.id === conversation.id"
              @select="selectConversation(conversation)"
            />
          </div>
        </div>
      </div>

      <!-- Chat Area -->
      <div class="flex-1 bg-white dark:bg-gray-800 rounded-lg border dark:border-gray-700">
        <div v-if="!selectedConversation" class="h-full flex items-center justify-center">
          <div class="text-center">
            <Icon icon="mdi:chat" class="text-6xl text-gray-300" />
            <p class="mt-4 text-gray-500">Select a conversation to start messaging</p>
          </div>
        </div>
        
        <div v-else class="h-full flex flex-col">
          <!-- Chat Header -->
          <div class="p-4 border-b dark:border-gray-700 flex items-center justify-between">
            <div class="flex items-center gap-3">
              <!-- User Avatar -->
              <div class="flex-shrink-0">
                <img 
                  v-if="selectedConversation.userAvatar"
                  :src="selectedConversation.userAvatar" 
                  :alt="selectedConversation.userName || selectedConversation.externalUserId"
                  class="w-12 h-12 rounded-full object-cover"
                  @error="handleImageError"
                />
                <div 
                  v-else
                  class="w-12 h-12 rounded-full bg-gray-300 dark:bg-gray-600 flex items-center justify-center"
                >
                  <Icon icon="mdi:account" class="text-gray-600 dark:text-gray-300 text-2xl" />
                </div>
              </div>
              
              <div>
                <h3 class="font-semibold text-gray-900 dark:text-gray-200">
                  {{ selectedConversation.userName || selectedConversation.externalUserId || 'Unknown User' }}
                </h3>
                <p class="text-sm text-gray-500">
                  {{ selectedConversation.channel }} • {{ getRelativeTime(selectedConversation.lastMessageAt) }}
                </p>
              </div>
            </div>
            
            <div class="flex items-center gap-2">
              <span v-if="selectedConversation.isTakenOver" 
                class="bg-green-100 text-green-800 text-sm px-3 py-1 rounded-full">
                <Icon icon="mdi:hand-right" class="inline mr-1" />
                You are in control
              </span>
              
              <button
                v-if="!selectedConversation.isTakenOver"
                @click="takeOverConversation"
                :disabled="takingOver"
                class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center gap-2 disabled:opacity-50"
              >
                <Icon v-if="takingOver" icon="mdi:loading" class="animate-spin" />
                <Icon v-else icon="mdi:hand-right" />
                Take Over
              </button>
              
              <button
                v-else
                @click="releaseConversation"
                :disabled="releasing"
                class="bg-gray-600 hover:bg-gray-700 text-white px-4 py-2 rounded-lg flex items-center gap-2 disabled:opacity-50"
              >
                <Icon v-if="releasing" icon="mdi:loading" class="animate-spin" />
                <Icon v-else icon="mdi:hand-left" />
                Release
              </button>
            </div>
          </div>
          
          <!-- Messages Area -->
          <div ref="messagesContainer" class="flex-1 overflow-y-auto p-4 space-y-4">
            <!-- Real-time Message Indicator -->
            <RealTimeMessageIndicator
              v-if="selectedConversation"
              :conversation-id="String(selectedConversation.id)"
            />
            
            <div v-if="loadingMessages" class="text-center py-8">
              <Icon icon="mdi:loading" class="animate-spin text-2xl text-gray-400" />
              <p class="mt-2 text-gray-500">Loading messages...</p>
            </div>
            
            <div v-else-if="messages.length === 0" class="text-center py-8">
              <Icon icon="mdi:message-text" class="text-4xl text-gray-300" />
              <p class="mt-2 text-gray-500">No messages in this conversation</p>
            </div>
            
            <div v-else>
              <!-- Use RealTimeMessageBubble for messages with real-time features -->
              <RealTimeMessageBubble
                v-for="message in messages"
                :key="message.id || `${message.timestamp}-${message.content}`"
                :message="message"
              />
              <!-- Fallback to original MessageBubble for compatibility -->
              <MessageBubble
                v-for="message in messages.filter(m => !m.isRealtime)"
                :key="message.id"
                :message="message"
              />
            </div>
          </div>
          
          <!-- Message Input -->
          <div v-if="selectedConversation.isTakenOver" class="p-4 border-t dark:border-gray-700">
            <div class="flex items-center gap-2">
              <input
                v-model="newMessage"
                @input="startTyping"
                @keydown.enter="sendRealTimeMessage"
                @blur="stopTyping"
                type="text"
                placeholder="Type your message..."
                :disabled="sendingMessage || connectionStatus !== 'connected'"
                class="flex-1 px-4 py-2 border rounded-lg dark:bg-gray-700 dark:border-gray-600 dark:text-white"
              />
              <button
                @click="sendRealTimeMessage(newMessage)"
                :disabled="sendingMessage || !newMessage.trim() || connectionStatus !== 'connected'"
                class="bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 text-white px-4 py-2 rounded-lg flex items-center gap-2"
              >
                <Icon v-if="sendingMessage" icon="mdi:loading" class="animate-spin" />
                <Icon v-else icon="mdi:send" />
                Send
              </button>
            </div>
          </div>
          
          <div v-else class="p-4 border-t dark:border-gray-700 bg-gray-50 dark:bg-gray-900">
            <p class="text-center text-gray-500 text-sm">
              <Icon icon="mdi:information" class="inline mr-1" />
              Take over this conversation to send messages
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- Search Modal -->
    <div v-if="showSearchModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-md">
        <h3 class="text-lg font-semibold mb-4">Search Conversations</h3>
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium mb-1">Search Query</label>
            <input
              v-model="searchModal.query"
              type="text"
              placeholder="Enter search terms..."
              class="w-full px-3 py-2 border rounded-lg dark:bg-gray-700 dark:border-gray-600 dark:text-white"
            />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">Channel</label>
            <select v-model="searchModal.channel" 
              class="w-full px-3 py-2 border rounded-lg dark:bg-gray-700 dark:border-gray-600 dark:text-white">
              <option value="">All Channels</option>
              <option value="FACEBOOK">Facebook</option>
              <option value="WEBSITE">Website</option>
              <option value="ZALO">Zalo</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">Date Range</label>
            <input
              v-model="searchModal.dateRange"
              type="date"
              class="w-full px-3 py-2 border rounded-lg dark:bg-gray-700 dark:border-gray-600 dark:text-white"
            />
          </div>
        </div>
        <div class="flex justify-end gap-2 mt-6">
          <button
            @click="showSearchModal = false"
            class="px-4 py-2 border rounded-lg dark:border-gray-600 dark:text-white"
          >
            Cancel
          </button>
          <button
            @click="performSearch"
            class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg"
          >
            Search
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed, watch, onUnmounted } from 'vue'
import { Icon } from '@iconify/vue'
import { appApi as takeoverApi } from '@/api/takeoverApi'
import { usePennyBotStore } from '@/stores/pennyBotStore'
import { usePennyConnectionStore } from '@/stores/pennyConnectionStore'
import { getRelativeTime } from '@/utils/dateUtils'
import takeoverWebSocketService from '@/services/takeoverWebSocketService'
import ConversationItem from './messages/ConversationItem.vue'
import MessageBubble from './messages/MessageBubble.vue'
import RealTimeMessageIndicator from '@/components/RealTimeMessageIndicator.vue'
import RealTimeMessageBubble from '@/components/RealTimeMessageBubble.vue'

// Stores
const pennyBotStore = usePennyBotStore()
const pennyConnectionStore = usePennyConnectionStore()

// WebSocket Service
const wsService = takeoverWebSocketService

// State
const conversations = ref([])
const selectedConversation = ref(null)
const messages = ref([])
const loading = ref(false)
const loadingConversations = ref(false)
const loadingMessages = ref(false)
const takingOver = ref(false)
const releasing = ref(false)
const sendingMessage = ref(false)
const searchQuery = ref('')
const filterBot = ref('all')
const filterConnection = ref('all')
const newMessage = ref('')
const showSearchModal = ref(false)
const messagesContainer = ref(null)
const searchTimeout = ref(null)

// Real-time state
const connectionStatus = ref('disconnected')
const activeAgents = ref([])
const typingAgents = ref([])
const newMessageCount = ref(0)
const isTyping = ref(false)
const typingTimeout = ref(null)

// Search modal state
const searchModal = ref({
  query: '',
  channel: '',
  dateRange: ''
})

// Statistics
const stats = ref({
  totalConversations: 0,
  activeTakeovers: 0,
  pendingMessages: 0,
  todayMessages: 0
})

// Debounced search
const debouncedSearch = () => {
  clearTimeout(searchTimeout.value)
  searchTimeout.value = setTimeout(() => {
    loadConversations()
  }, 500)
}

// Methods
const loadConversations = async () => {
  try {
    loadingConversations.value = true
    
    console.log('Filter values - Bot:', filterBot.value, 'Connection:', filterConnection.value, 'Search:', searchQuery.value)
    
    let response
    
    // Use different endpoints based on filters
    if (filterConnection.value !== 'all' && filterConnection.value !== undefined && filterConnection.value !== null) {
      // Use connection-specific endpoint
      const params = {
        page: 0,
        limit: 50,
        search: searchQuery.value || undefined
      }
      console.log('Using connection-specific endpoint with params:', params)
      response = await takeoverApi.getConversationsByConnectionId(filterConnection.value, params)
    } else if (filterBot.value !== 'all' && filterBot.value !== undefined && filterBot.value !== null) {
      // Use bot-specific endpoint
      const params = {
        ownerId: filterBot.value,
        page: 0,
        limit: 50,
        search: searchQuery.value || undefined
      }
      console.log('Using bot-specific endpoint with params:', params)
      response = await takeoverApi.getConversationsByOwnerId(params)
    } else {
      // Use general endpoint
      const params = {
        page: 0,
        limit: 50,
        search: searchQuery.value || undefined
      }
      console.log('Using general endpoint with params:', params)
      response = await takeoverApi.getConversations(params)
    }
    
    conversations.value = response.data.content || response.data || []
    console.log('Loaded conversations:', conversations.value.length, 'items')
    
  } catch (error) {
    console.error('Error loading conversations:', error)
    conversations.value = []
  } finally {
    loadingConversations.value = false
  }
}

const loadMessages = async (conversationId) => {
  try {
    loadingMessages.value = true
    const response = await takeoverApi.getMessagesByConversationId(conversationId, {
      page: 0,
      limit: 100
    })
    messages.value = response.data.content || response.data || []
    
    // Scroll to bottom
    await nextTick()
    scrollToBottom()
  } catch (error) {
    console.error('Error loading messages:', error)
    messages.value = []
  } finally {
    loadingMessages.value = false
  }
}

const selectConversation = async (conversation) => {
  selectedConversation.value = conversation
  
  // Disconnect from previous conversation if any
  if (wsService.currentConversationId.value) {
    wsService.disconnect()
  }
  
  // Connect to WebSocket for real-time updates
  await wsService.connect(conversation.id)
  
  // Load messages
  await loadMessages(conversation.id)
}

const takeOverConversation = async () => {
  if (!selectedConversation.value) return
  
  try {
    takingOver.value = true
    await takeoverApi.takeOverConversation(selectedConversation.value.id)
    
    // Update local state
    selectedConversation.value.isTakenOver = true
    
    // Update in conversations list
    const index = conversations.value.findIndex(c => c.id === selectedConversation.value.id)
    if (index !== -1) {
      conversations.value[index].isTakenOver = true
    }
    
    // Reload stats
    loadStats()
  } catch (error) {
    console.error('Error taking over conversation:', error)
  } finally {
    takingOver.value = false
  }
}

const releaseConversation = async () => {
  if (!selectedConversation.value) return
  
  try {
    releasing.value = true
    await takeoverApi.releaseConversation(selectedConversation.value.id)
    
    // Update local state
    selectedConversation.value.isTakenOver = false
    
    // Update in conversations list
    const index = conversations.value.findIndex(c => c.id === selectedConversation.value.id)
    if (index !== -1) {
      conversations.value[index].isTakenOver = false
    }
    
    // Reload stats
    loadStats()
  } catch (error) {
    console.error('Error releasing conversation:', error)
  } finally {
    releasing.value = false
  }
}

const sendMessage = async () => {
  if (!newMessage.value.trim() || !selectedConversation.value) return
  
  try {
    sendingMessage.value = true
    
    const payload = {
      conversationId: selectedConversation.value.id,
      content: newMessage.value,
      sender: 'agent'
    }
    
    await takeoverApi.sendMessage(payload)
    
    // Add message to local state
    const message = {
      id: Date.now(),
      content: newMessage.value,
      sender: 'agent',
      createdAt: new Date().toISOString()
    }
    messages.value.push(message)
    
    newMessage.value = ''
    
    // Scroll to bottom
    await nextTick()
    scrollToBottom()
  } catch (error) {
    console.error('Error sending message:', error)
  } finally {
    sendingMessage.value = false
  }
}

const loadStats = async () => {
  try {
    const response = await takeoverApi.getConversationStatistics()
    stats.value = response.data || {
      totalConversations: 0,
      activeTakeovers: 0,
      pendingMessages: 0,
      todayMessages: 0
    }
  } catch (error) {
    console.error('Error loading stats:', error)
  }
}

const refreshConversations = async () => {
  loading.value = true
  await Promise.all([
    loadConversations(),
    loadStats()
  ])
  loading.value = false
}

const performSearch = async () => {
  try {
    const searchParams = {
      query: searchModal.value.query,
      channel: searchModal.value.channel,
      dateRange: searchModal.value.dateRange
    }
    
    const response = await takeoverApi.searchConversations(searchParams)
    conversations.value = response.data.content || response.data || []
    showSearchModal.value = false
  } catch (error) {
    console.error('Error searching conversations:', error)
  }
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    setTimeout(() => {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }, 100)
  }
}

const getChannelIcon = (channel) => {
  switch (channel) {
    case 'FACEBOOK':
      return 'mdi:facebook'
    case 'WEBSITE':
      return 'mdi:web'
    case 'ZALO':
      return 'mdi:message-text'
    default:
      return 'mdi:chat'
  }
}

const handleImageError = (event) => {
  // Fallback to default avatar if image fails to load
  event.target.style.display = 'none'
  const parent = event.target.parentElement
  if (parent) {
    const fallback = parent.querySelector('.bg-gray-300, .dark\\:bg-gray-600')
    if (fallback) {
      fallback.style.display = 'flex'
    }
  }
}

// Watchers
watch(filterBot, async (newBotId) => {
  // Reset connection filter when bot changes
  filterConnection.value = 'all'
  
  // Fetch connections for the selected bot
  if (newBotId !== 'all') {
    try {
      await pennyConnectionStore.fetchConnections(newBotId)
    } catch (error) {
      console.warn('Failed to fetch connections for bot:', newBotId, error)
    }
  } else {
    // If "all bots" selected, try to fetch connections for first active bot
    const activeBots = pennyBotStore.activeBots
    if (activeBots.length > 0) {
      try {
        await pennyConnectionStore.fetchConnections(activeBots[0].id)
      } catch (error) {
        console.warn('Failed to fetch connections:', error)
      }
    }
  }
  
  // Reload conversations with new filters
  loadConversations()
})

watch(filterConnection, () => {
  // Reload conversations when connection filter changes
  loadConversations()
})

// Real-time WebSocket event handlers
const setupWebSocketHandlers = () => {
  // Handle real-time messages
  wsService.onMessageReceived = (message) => {
    if (message.conversationId === selectedConversation.value?.id) {
      // Add message to current conversation
      messages.value.push(message)
      
      // Scroll to bottom
      nextTick(() => {
        scrollToBottom()
      })
      
      // Mark message as read
      if (message.id) {
        wsService.markMessageRead(message.id)
      }
    }
    
    // Update conversation in list
    const conversationIndex = conversations.value.findIndex(c => c.id === message.conversationId)
    if (conversationIndex !== -1) {
      conversations.value[conversationIndex].lastMessageAt = message.timestamp
      conversations.value[conversationIndex].lastMessage = message.content
    }
    
    // Update stats
    loadStats()
  }
  
  // Handle connection status changes
  wsService.onConnectionStatusChanged = (status) => {
    connectionStatus.value = status
  }
  
  // Handle agent joining/leaving
  wsService.onAgentJoined = (conversationId, agent) => {
    if (conversationId === selectedConversation.value?.id) {
      activeAgents.value = wsService.getActiveAgents(conversationId)
    }
  }
  
  wsService.onAgentLeft = (conversationId, agent) => {
    if (conversationId === selectedConversation.value?.id) {
      activeAgents.value = wsService.getActiveAgents(conversationId)
    }
  }
  
  // Handle typing indicators
  wsService.onTypingStarted = (conversationId, agent) => {
    if (conversationId === selectedConversation.value?.id) {
      typingAgents.value = wsService.getTypingAgents(conversationId)
    }
  }
  
  wsService.onTypingStopped = (conversationId, agent) => {
    if (conversationId === selectedConversation.value?.id) {
      typingAgents.value = wsService.getTypingAgents(conversationId)
    }
  }
}

// Real-time message sending
const sendRealTimeMessage = async (content) => {
  if (!content.trim() || !selectedConversation.value) return
  
  try {
    sendingMessage.value = true
    
    const message = {
      conversationId: selectedConversation.value.id,
      content: content.trim(),
      sender: 'agent'
    }
    
    const sentMessage = wsService.sendMessage(message)
    if (sentMessage) {
      // Add to local messages immediately for better UX
      messages.value.push(sentMessage)
      
      // Clear input
      newMessage.value = ''
      
      // Scroll to bottom
      nextTick(() => {
        scrollToBottom()
      })
    }
  } catch (error) {
    console.error('Error sending message:', error)
  } finally {
    sendingMessage.value = false
  }
}

// Typing indicator handlers
const startTyping = () => {
  if (!isTyping.value && selectedConversation.value) {
    isTyping.value = true
    wsService.sendTypingStart(selectedConversation.value.id)
  }
  
  // Clear existing timeout
  if (typingTimeout.value) {
    clearTimeout(typingTimeout.value)
  }
  
  // Set new timeout to stop typing after 3 seconds
  typingTimeout.value = setTimeout(() => {
    stopTyping()
  }, 3000)
}

const stopTyping = () => {
  if (isTyping.value && selectedConversation.value) {
    isTyping.value = false
    wsService.sendTypingStop(selectedConversation.value.id)
  }
  
  if (typingTimeout.value) {
    clearTimeout(typingTimeout.value)
    typingTimeout.value = null
  }
}

// Lifecycle
onMounted(async () => {
  // Setup WebSocket handlers
  setupWebSocketHandlers()
  
  // Fetch bots data first
  await pennyBotStore.fetchPennyBots()
  // Fetch connections data (we'll fetch for all bots or first available bot)
  try {
    const activeBots = pennyBotStore.activeBots
    if (activeBots.length > 0) {
      await pennyConnectionStore.fetchConnections(activeBots[0].id)
    }
  } catch (error) {
    console.warn('Failed to fetch connections:', error)
  }
  // Load conversations with initial filters
  loadConversations()
})

onUnmounted(() => {
  // Cleanup WebSocket connection
  wsService.disconnect()
})
</script>

<style scoped>
.animate-spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
