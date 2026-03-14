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

    <!-- Main Content -->
    <div class="flex gap-4" style="min-height: 600px;">
      <!-- Conversations List -->
      <div class="w-full lg:w-1/3 bg-white dark:bg-gray-800 rounded-lg border dark:border-gray-700">
        <div class="p-4 border-b dark:border-gray-700">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-lg font-semibold text-gray-900 dark:text-gray-200">Conversations</h2>
            <div class="flex items-center gap-2">
              <!-- Filter Controls -->
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
          
          <!-- Selection Controls -->
          <div v-if="conversations.length > 0" class="flex items-center justify-between">
            <!-- Select All -->
            <div class="flex items-center gap-2">
              <input
                type="checkbox"
                :checked="allSelected"
                @change="toggleSelectAll"
                class="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
              />
              <label 
                @click="toggleSelectAll"
                class="text-sm text-gray-600 dark:text-gray-400 cursor-pointer hover:text-gray-800 dark:hover:text-gray-200"
              >
                select all
              </label>
            </div>
            
            <!-- Delete with Count -->
            <div v-if="selectedConversations.size > 0" class="flex items-center gap-2">
              <button
                @click="deleteSelectedConversations"
                class="text-sm bg-red-600 hover:bg-red-700 text-white px-3 py-1 rounded flex items-center gap-1"
              >
                <Icon icon="mdi:delete" />
                delete
              </button>
              <span class="text-sm text-gray-600 dark:text-gray-400">
                - {{ selectedConversations.size }} conversations selected
              </span>
            </div>
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
              :is-selected-for-deletion="isConversationSelected(conversation.id)"
              @select="selectConversation(conversation)"
              @toggle-select="toggleConversationSelection(conversation.id)"
            />
          </div>
          
          <!-- Pagination -->
          <div v-if="totalPages > 1" class="p-4 border-t dark:border-gray-700 bg-gray-50 dark:bg-gray-700">
            <div class="flex items-center justify-between">
              <div class="text-sm text-gray-600 dark:text-gray-400">
                Showing {{ conversations.length }} of {{ totalElements }} conversations
              </div>
              <div class="flex items-center gap-2">
                <!-- First Page -->
                <button
                  @click="firstPage"
                  :disabled="currentPage === 0"
                  class="p-1 rounded hover:bg-gray-200 dark:hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed"
                  title="First page"
                >
                  <Icon icon="mdi:page-first" class="text-gray-600 dark:text-gray-400" />
                </button>
                
                <!-- Previous Page -->
                <button
                  @click="prevPage"
                  :disabled="currentPage === 0"
                  class="p-1 rounded hover:bg-gray-200 dark:hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed"
                  title="Previous page"
                >
                  <Icon icon="mdi:chevron-left" class="text-gray-600 dark:text-gray-400" />
                </button>
                
                <!-- Page Info -->
                <span class="text-sm text-gray-600 dark:text-gray-400 px-2">
                  Page {{ currentPage + 1 }} of {{ totalPages }}
                </span>
                
                <!-- Next Page -->
                <button
                  @click="nextPage"
                  :disabled="currentPage >= totalPages - 1"
                  class="p-1 rounded hover:bg-gray-200 dark:hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed"
                  title="Next page"
                >
                  <Icon icon="mdi:chevron-right" class="text-gray-600 dark:text-gray-400" />
                </button>
                
                <!-- Last Page -->
                <button
                  @click="lastPage"
                  :disabled="currentPage >= totalPages - 1"
                  class="p-1 rounded hover:bg-gray-200 dark:hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed"
                  title="Last page"
                >
                  <Icon icon="mdi:page-last" class="text-gray-600 dark:text-gray-400" />
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Chat Area -->
      <div class="flex-1 bg-white dark:bg-gray-800 rounded-lg border dark:border-gray-700 flex flex-col" style="min-height: 600px;">
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
          <div ref="messagesContainer" class="messages-area flex-1 overflow-y-auto p-4 bg-white dark:bg-gray-800 flex flex-col">
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
            
            <div v-else class="flex-1 flex flex-col justify-end">
              <!-- Render all messages with RealTimeMessageBubble for consistency -->
              <RealTimeMessageBubble
                v-for="message in sortedMessages"
                :key="message.id || `${message.timestamp}-${message.content}`"
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
                @keydown.enter.prevent="sendMessage"
                @blur="stopTyping"
                type="text"
                placeholder="Type your message..."
                :disabled="sendingMessage || connectionStatus !== 'connected'"
                class="flex-1 px-4 py-2 border rounded-lg dark:bg-gray-700 dark:border-gray-600 dark:text-white disabled:opacity-50"
              />
              <button
                @click="sendMessage"
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
import ConversationItem from '../conversations/components/ConversationItem.vue'
import RealTimeMessageIndicator from './components/RealTimeMessageIndicator.vue'
import RealTimeMessageBubble from './components/RealTimeMessageBubble.vue'

// Stores
const pennyBotStore = usePennyBotStore()
const pennyConnectionStore = usePennyConnectionStore()

// WebSocket Service
const wsService = takeoverWebSocketService

// State
const conversations = ref([])
const selectedConversation = ref(null)
const selectedConversations = ref(new Set()) // For multi-selection
const messages = ref([])
const loading = ref(false)
const loadingConversations = ref(false)
const loadingMessages = ref(false)
const takingOver = ref(false)
const releasing = ref(false)
const sendingMessage = ref(false)
const filterBot = ref('all')
const filterConnection = ref('all')

// Pagination
const currentPage = ref(0)
const totalPages = ref(0)
const pageSize = ref(20)
const totalElements = ref(0)

// Computed properties for selection
const allSelected = computed(() => {
  return conversations.value.length > 0 && selectedConversations.value.size === conversations.value.length
})

// Selection methods
const toggleSelectAll = () => {
  if (allSelected.value) {
    selectedConversations.value.clear()
  } else {
    conversations.value.forEach(conv => selectedConversations.value.add(conv.id))
  }
}

const toggleConversationSelection = (conversationId) => {
  if (selectedConversations.value.has(conversationId)) {
    selectedConversations.value.delete(conversationId)
  } else {
    selectedConversations.value.add(conversationId)
  }
}

const isConversationSelected = (conversationId) => {
  return selectedConversations.value.has(conversationId)
}

const deleteSelectedConversations = async () => {
  if (selectedConversations.value.size === 0) return
  
  if (!confirm(`Are you sure you want to delete ${selectedConversations.value.size} conversation(s)?`)) {
    return
  }
  
  try {
    loading.value = true
    
    // Store current selected conversation ID before clearing
    const currentSelectedId = selectedConversation.value?.id
    const deletedIds = Array.from(selectedConversations.value)
    
    // Delete each selected conversation
    for (const conversationId of selectedConversations.value) {
      await takeoverApi.deleteConversation(conversationId)
    }
    
    // Clear selection and reload
    selectedConversations.value.clear()
    await loadConversations()
    
    // Clear selected conversation if it was deleted
    if (currentSelectedId && deletedIds.includes(currentSelectedId)) {
      selectedConversation.value = null
      messages.value = []
    }
    
  } catch (error) {
    console.error('Error deleting conversations:', error)
    alert('Failed to delete conversations. Please try again.')
  } finally {
    loading.value = false
  }
}
const newMessage = ref('')
const showSearchModal = ref(false)
const messagesContainer = ref(null)

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

// Helper functions
const isDuplicateMessage = (newMessage, existingMessages) => {
  return existingMessages.some(existing => {
    // Check by ID if available
    if (existing.id && newMessage.id && existing.id === newMessage.id) {
      return true
    }
    
    // Check by timestamp and content as fallback
    const timeMatch = existing.timestamp === newMessage.timestamp || 
                     existing.createdAt === newMessage.createdAt
    const contentMatch = existing.content === newMessage.content
    const senderMatch = existing.sender === newMessage.sender
    
    return timeMatch && contentMatch && senderMatch
  })
}

// Computed properties
const sortedMessages = computed(() => {
  if (!messages.value) return []
  
  return [...messages.value].sort((a, b) => {
    // Sort by timestamp (oldest first, newest last)
    const timeA = new Date(a.timestamp || a.createdAt)
    const timeB = new Date(b.timestamp || b.createdAt)
    return timeA - timeB
  })
})

// Methods
const loadConversations = async () => {
  try {
    loadingConversations.value = true
    
    console.log('Filter values - Bot:', filterBot.value, 'Connection:', filterConnection.value)
    
    let response
    
    // Use different endpoints based on filters
    if (filterConnection.value !== 'all') {
      const params = {
        page: currentPage.value,
        limit: pageSize.value
      }
      console.log('Using connection-specific endpoint with params:', params)
      response = await takeoverApi.getConversationsByConnectionId(filterConnection.value, params)
    } else if (filterBot.value !== 'all') {
      const params = {
        ownerId: filterBot.value,
        page: currentPage.value,
        limit: pageSize.value
      }
      console.log('Using bot-specific endpoint with params:', params)
      response = await takeoverApi.getConversationsByOwnerId(params)
    } else {
      const params = {
        page: currentPage.value,
        limit: pageSize.value
      }
      console.log('Using general endpoint with params:', params)
      response = await takeoverApi.getConversations(params)
    }
    
    conversations.value = response.data.content || response.data || []
    
    // Update pagination info
    if (response.data.totalElements !== undefined) {
      totalElements.value = response.data.totalElements
      totalPages.value = Math.ceil(totalElements.value / pageSize.value)
    } else {
      // Fallback for non-paginated responses
      totalElements.value = conversations.value.length
      totalPages.value = 1
    }
    
    console.log('Loaded conversations:', conversations.value.length, 'items')
    console.log('Pagination info:', {
      page: currentPage.value,
      totalPages: totalPages.value,
      totalElements: totalElements.value,
      pageSize: pageSize.value
    })
    
    // Auto-select first conversation if none selected and conversations exist
    if (!selectedConversation.value && conversations.value.length > 0) {
      await selectConversation(conversations.value[0])
      console.log('Auto-selected first conversation:', conversations.value[0].id)
    }
    
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
    const newMessages = response.data.content || response.data || []
    
    // Clear and reload messages for fresh conversation
    messages.value = newMessages
    
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
  
  // Clear current messages when switching conversations
  messages.value = []
  
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
    
    const response = await takeoverApi.sendMessage(payload)
    console.log('Message sent successfully:', response.data)
    
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
    // Show error to user
    alert('Failed to send message: ' + (error.response?.data?.error || error.message))
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

// Pagination methods
const goToPage = (page) => {
  if (page >= 0 && page < totalPages.value) {
    currentPage.value = page
    loadConversations()
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value - 1) {
    currentPage.value++
    loadConversations()
  }
}

const prevPage = () => {
  if (currentPage.value > 0) {
    currentPage.value--
    loadConversations()
  }
}

const firstPage = () => {
  if (currentPage.value !== 0) {
    currentPage.value = 0
    loadConversations()
  }
}

const lastPage = () => {
  if (currentPage.value !== totalPages.value - 1) {
    currentPage.value = totalPages.value - 1
    loadConversations()
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
    console.log('📡 WebSocket message received:', message)
    
    if (String(message.conversationId) === String(selectedConversation.value?.id)) {
      // Format message for RealTimeMessageBubble component
      const formattedMessage = {
        id: message.id || `ws-${Date.now()}`,
        content: message.content || message.text || message.message || '',
        sender: message.sender || 'bot',
        timestamp: message.timestamp || new Date(),
        isRealtime: true, // Mark as real-time
        read: false
      }
      
      console.log('📝 WebSocket formatted message:', formattedMessage)
      
      // Check for duplicates before adding
      const isDuplicate = messages.value.some(existing => 
        existing.id === formattedMessage.id || 
        (existing.content === formattedMessage.content && 
         Math.abs(new Date(existing.timestamp) - new Date(formattedMessage.timestamp)) < 1000)
      )
      
      if (!isDuplicate) {
        console.log('➕ Adding WebSocket message to chat')
        messages.value.push(formattedMessage)
        
        // Scroll to bottom
        nextTick(() => {
          scrollToBottom()
        })
        
        // Mark message as read
        if (formattedMessage.id) {
          wsService.markMessageRead(formattedMessage.id)
        }
      } else {
        console.log('🚫 Duplicate WebSocket message detected, skipping')
      }
    } else {
      console.log('⚠️ WebSocket message for different conversation, ignoring')
    }
    
    // Update conversation in list
    const conversationIndex = conversations.value.findIndex(c => String(c.id) === String(message.conversationId))
    if (conversationIndex !== -1) {
      conversations.value[conversationIndex].lastMessageAt = message.timestamp
      conversations.value[conversationIndex].lastMessage = message.content || message.message
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

// Global event listener for conversation messages
const handleConversationMessage = (event) => {
  const { conversationId, sender, message, timestamp } = event.detail
  
  console.log('📡 Global conversation message received:', { conversationId, sender, message, timestamp })
  
  if (conversationId === selectedConversation.value?.id) {
    // Format message for RealTimeMessageBubble component
    const formattedMessage = {
      id: `global-${Date.now()}`,
      content: message || '',
      sender: sender === 'user' ? 'user' : 'bot',
      timestamp: new Date(timestamp),
      isRealtime: true, // Mark as real-time
      read: false
    }
    
    console.log('📝 Formatted message:', formattedMessage)
    
    // Check for duplicates before adding
    const isDuplicate = messages.value.some(existing => 
      existing.content === formattedMessage.content && 
      Math.abs(new Date(existing.timestamp) - new Date(formattedMessage.timestamp)) < 1000
    )
    
    if (!isDuplicate) {
      console.log('➕ Adding real-time message to chat')
      messages.value.push(formattedMessage)
      
      // Scroll to bottom
      nextTick(() => {
        scrollToBottom()
      })
    } else {
      console.log('🚫 Duplicate message detected, skipping')
    }
  } else {
    console.log('⚠️ Message for different conversation, ignoring')
  }
}

// Lifecycle
onMounted(async () => {
  // Setup WebSocket handlers
  setupWebSocketHandlers()
  
  // Add global event listener for conversation messages
  window.addEventListener('conversationMessage', handleConversationMessage)
  
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
  // Remove global event listener
  window.removeEventListener('conversationMessage', handleConversationMessage)
  
  // Cleanup WebSocket connection
  wsService.disconnect()
})
</script>

<style scoped>
/* Messages container */
.messages-container {
  @apply flex-1 flex flex-col h-full;
}

/* Message area with proper scrolling and height constraints */
.messages-area {
  @apply flex-1 overflow-y-auto p-4 flex flex-col;
  max-height: 600px; /* Fixed max height */
  overscroll-behavior-y: contain; /* Prevent page scroll */
  scroll-behavior: smooth; /* Smooth scrolling */
  background-color: #ffffff; /* Light mode */
}

.dark .messages-area {
  background-color: #1f2937; /* Dark mode - gray-800 */
}

/* Custom scrollbar styling */
.messages-area::-webkit-scrollbar {
  width: 6px;
}

.messages-area::-webkit-scrollbar-track {
  background: #f1f5f9; /* Light mode track */
  border-radius: 3px;
}

.dark .messages-area::-webkit-scrollbar-track {
  background: #374151; /* Dark mode track - gray-700 */
}

.messages-area::-webkit-scrollbar-thumb {
  background: #cbd5e1; /* Light mode thumb */
  border-radius: 3px;
}

.messages-area::-webkit-scrollbar-thumb:hover {
  background: #94a3b8; /* Light mode thumb hover */
}

.dark .messages-area::-webkit-scrollbar-thumb {
  background: #4b5563; /* Dark mode thumb - gray-600 */
}

.dark .messages-area::-webkit-scrollbar-thumb:hover {
  background: #6b7280; /* Dark mode thumb hover - gray-500 */
}

.animate-spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Global override for all message indicators */
.message-bubble.unread::after,
.message-bubble.realtime::before,
.realtime-badge {
  display: none !important;
  content: none !important;
  visibility: hidden !important;
  opacity: 0 !important;
}
</style>
