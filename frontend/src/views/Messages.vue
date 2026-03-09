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
              <select v-model="filterStatus" @change="loadConversations" 
                class="text-sm border rounded px-2 py-1 dark:bg-gray-700 dark:border-gray-600 dark:text-white">
                <option value="all">All</option>
                <option value="active">Active</option>
                <option value="taken_over">Taken Over</option>
                <option value="bot">Bot Only</option>
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
              <div class="bg-gray-200 dark:bg-gray-600 rounded-full p-2">
                <Icon :icon="getChannelIcon(selectedConversation.channel)" class="text-gray-600 dark:text-gray-300" />
              </div>
              <div>
                <h3 class="font-semibold text-gray-900 dark:text-gray-200">
                  {{ selectedConversation.externalUserId || 'Unknown User' }}
                </h3>
                <p class="text-sm text-gray-500">
                  {{ selectedConversation.channel }} • {{ formatTime(selectedConversation.lastMessageAt) }}
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
            <div v-if="loadingMessages" class="text-center py-8">
              <Icon icon="mdi:loading" class="animate-spin text-2xl text-gray-400" />
              <p class="mt-2 text-gray-500">Loading messages...</p>
            </div>
            
            <div v-else-if="messages.length === 0" class="text-center py-8">
              <Icon icon="mdi:message-text" class="text-4xl text-gray-300" />
              <p class="mt-2 text-gray-500">No messages in this conversation</p>
            </div>
            
            <div v-else>
              <MessageBubble
                v-for="message in messages"
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
                @keydown.enter="sendMessage"
                type="text"
                placeholder="Type your message..."
                :disabled="sendingMessage"
                class="flex-1 px-4 py-2 border rounded-lg dark:bg-gray-700 dark:border-gray-600 dark:text-white"
              />
              <button
                @click="sendMessage"
                :disabled="!newMessage.trim() || sendingMessage"
                class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg disabled:opacity-50"
              >
                <Icon v-if="sendingMessage" icon="mdi:loading" class="animate-spin" />
                <Icon v-else icon="mdi:send" />
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
import { ref, onMounted, nextTick, computed } from 'vue'
import { Icon } from '@iconify/vue'
import { appApi as takeoverApi } from '@/api/takeoverApi'
import ConversationItem from './messages/ConversationItem.vue'
import MessageBubble from './messages/MessageBubble.vue'

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
const filterStatus = ref('all')
const newMessage = ref('')
const showSearchModal = ref(false)
const messagesContainer = ref(null)

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
let searchTimeout
const debouncedSearch = () => {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    loadConversations()
  }, 500)
}

// Methods
const loadConversations = async () => {
  try {
    loadingConversations.value = true
    const params = {
      page: 1,
      limit: 50,
      status: filterStatus.value !== 'all' ? filterStatus.value : undefined,
      search: searchQuery.value || undefined
    }
    
    const response = await takeoverApi.getConversations(params)
    conversations.value = response.data.content || response.data || []
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
      page: 1,
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

const selectConversation = (conversation) => {
  selectedConversation.value = conversation
  loadMessages(conversation.id)
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
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
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

// Lifecycle
onMounted(() => {
  refreshConversations()
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
