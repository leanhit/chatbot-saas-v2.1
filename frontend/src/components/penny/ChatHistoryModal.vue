<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white dark:bg-gray-800 rounded-lg max-w-5xl w-full mx-4 max-h-[90vh] overflow-y-auto">
      <!-- Header -->
      <div class="p-6 border-b border-gray-200 dark:border-gray-700">
        <div class="flex justify-between items-center">
          <div>
            <h3 class="text-xl font-semibold text-gray-900 dark:text-gray-100">
              Chat History
            </h3>
            <p class="text-gray-600 dark:text-gray-400">
              {{ bot?.botName || 'Bot' }} - {{ totalConversations }} conversations
            </p>
          </div>
          <button 
            @click="$emit('close')"
            class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
          >
            <Icon icon="ic:baseline-close" class="text-xl" />
          </button>
        </div>
      </div>

      <!-- Filters and Search -->
      <div class="p-6 border-b border-gray-200 dark:border-gray-700">
        <div class="flex flex-col lg:flex-row gap-4">
          <!-- Search -->
          <div class="flex-1">
            <div class="relative">
              <Icon icon="ic:baseline-search" class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
              <input 
                v-model="searchQuery"
                type="text"
                placeholder="Search conversations..."
                class="w-full pl-10 pr-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
          </div>

          <!-- Date Range Filter -->
          <select 
            v-model="dateRange"
            class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            <option value="">All Time</option>
            <option value="today">Today</option>
            <option value="yesterday">Yesterday</option>
            <option value="7days">Last 7 Days</option>
            <option value="30days">Last 30 Days</option>
            <option value="90days">Last 90 Days</option>
          </select>

          <!-- Export Button -->
          <button 
            @click="exportHistory"
            class="px-4 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors flex items-center gap-2"
          >
            <Icon icon="ic:baseline-file-download" />
            Export
          </button>
        </div>
      </div>

      <!-- Conversations List -->
      <div class="p-6">
        <div v-if="isLoading" class="text-center py-12">
          <Icon icon="ic:baseline-refresh" class="animate-spin text-4xl text-gray-400" />
          <p class="text-gray-600 dark:text-gray-400 mt-2">Loading conversations...</p>
        </div>

        <div v-else-if="filteredConversations.length === 0" class="text-center py-12">
          <Icon icon="ic:baseline-history" class="text-4xl text-gray-400" />
          <h3 class="text-lg font-medium text-gray-900 dark:text-gray-100 mt-4">
            No conversations found
          </h3>
          <p class="text-gray-600 dark:text-gray-400 mt-2">
            {{ searchQuery ? 'Try adjusting your search' : 'No conversations available' }}
          </p>
        </div>

        <div v-else class="space-y-4">
          <div 
            v-for="conversation in paginatedConversations" 
            :key="conversation.id"
            class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4 hover:shadow-md transition-shadow cursor-pointer"
            @click="viewConversation(conversation)"
          >
            <!-- Conversation Header -->
            <div class="flex justify-between items-start mb-3">
              <div class="flex items-center gap-3">
                <div class="p-2 bg-blue-100 dark:bg-blue-900/20 rounded-lg">
                  <Icon icon="ic:baseline-chat" class="text-blue-600 dark:text-blue-400" />
                </div>
                <div>
                  <h4 class="font-semibold text-gray-900 dark:text-gray-100">
                    {{ conversation.title || 'Untitled Conversation' }}
                  </h4>
                  <p class="text-sm text-gray-600 dark:text-gray-400">
                    {{ formatDate(conversation.createdAt) }} • {{ conversation.messageCount }} messages
                  </p>
                </div>
              </div>
              
              <!-- Status Badge -->
              <span :class="getConversationStatusClass(conversation)" class="px-2 py-1 rounded-full text-xs font-medium">
                {{ getConversationStatusText(conversation) }}
              </span>
            </div>

            <!-- Last Message Preview -->
            <div class="mb-3">
              <p class="text-sm text-gray-600 dark:text-gray-400 line-clamp-2">
                <span class="font-medium">{{ conversation.lastMessage?.sender }}:</span>
                {{ conversation.lastMessage?.content || 'No messages' }}
              </p>
            </div>

            <!-- Conversation Stats -->
            <div class="flex gap-4 text-xs text-gray-600 dark:text-gray-400">
              <div class="flex items-center gap-1">
                <Icon icon="ic:baseline-chat" class="text-blue-500" />
                <span>{{ conversation.messageCount }} messages</span>
              </div>
              <div class="flex items-center gap-1">
                <Icon icon="ic:baseline-speed" class="text-green-500" />
                <span>{{ conversation.avgResponseTime }}ms avg</span>
              </div>
              <div class="flex items-center gap-1">
                <Icon icon="ic:baseline-thumb-up" class="text-purple-500" />
                <span>{{ conversation.satisfactionRate }}% satisfaction</span>
              </div>
              <div class="flex items-center gap-1">
                <Icon icon="ic:baseline-access-time" class="text-orange-500" />
                <span>{{ getDuration(conversation.createdAt, conversation.endedAt) }}</span>
              </div>
            </div>

            <!-- Action Buttons -->
            <div class="flex gap-2 mt-3">
              <button 
                @click.stop="viewConversation(conversation)"
                class="flex-1 px-3 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center justify-center gap-2"
              >
                <Icon icon="ic:baseline-visibility" />
                View
              </button>
              <button 
                @click.stop="exportConversation(conversation)"
                class="px-3 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors flex items-center justify-center gap-2"
              >
                <Icon icon="ic:baseline-download" />
                Export
              </button>
              <button 
                @click.stop="deleteConversation(conversation)"
                class="px-3 py-2 bg-red-100 dark:bg-red-900/20 text-red-600 dark:text-red-400 rounded-lg hover:bg-red-200 dark:hover:bg-red-900/30 transition-colors flex items-center justify-center gap-2"
              >
                <Icon icon="ic:baseline-delete" />
                Delete
              </button>
            </div>
          </div>
        </div>

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="flex justify-between items-center mt-6 pt-6 border-t border-gray-200 dark:border-gray-700">
          <div class="text-sm text-gray-700 dark:text-gray-300">
            Showing {{ (currentPage - 1) * pageSize + 1 }} to {{ Math.min(currentPage * pageSize, filteredConversations.length) }} of {{ filteredConversations.length }} conversations
          </div>
          <div class="flex gap-2">
            <button 
              @click="currentPage = Math.max(1, currentPage - 1)"
              :disabled="currentPage === 1"
              class="px-3 py-1 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Previous
            </button>
            <span class="px-3 py-1 text-gray-700 dark:text-gray-300">
              {{ currentPage }} / {{ totalPages }}
            </span>
            <button 
              @click="currentPage = Math.min(totalPages, currentPage + 1)"
              :disabled="currentPage === totalPages"
              class="px-3 py-1 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Next
            </button>
          </div>
        </div>
      </div>

      <!-- Footer -->
      <div class="p-6 border-t border-gray-200 dark:border-gray-700">
        <div class="flex justify-between items-center">
          <div class="flex gap-4">
            <label class="flex items-center gap-2">
              <input 
                type="checkbox" 
                v-model="showOnlyActive"
                class="rounded"
              />
              <span class="text-sm text-gray-700 dark:text-gray-300">
                Show only active conversations
              </span>
            </label>
            
            <label class="flex items-center gap-2">
              <input 
                type="checkbox" 
                v-model="groupByDate"
                class="rounded"
              />
              <span class="text-sm text-gray-700 dark:text-gray-300">
                Group by date
              </span>
            </label>
          </div>
          
          <button 
            @click="$emit('close')"
            class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-600 transition-colors"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const props = defineProps({
  bot: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'viewConversation'])

// State
const isLoading = ref(false)
const searchQuery = ref('')
const dateRange = ref('')
const showOnlyActive = ref(false)
const groupByDate = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)

// Mock conversation data
const conversations = ref([
  {
    id: '1',
    title: 'Product Inquiry',
    createdAt: '2024-01-15T10:30:00Z',
    endedAt: '2024-01-15T10:45:00Z',
    messageCount: 12,
    avgResponseTime: 850,
    satisfactionRate: 95,
    status: 'completed',
    lastMessage: {
      sender: 'User',
      content: 'Thank you for your help!'
    }
  },
  {
    id: '2',
    title: 'Technical Support',
    createdAt: '2024-01-14T14:20:00Z',
    endedAt: null,
    messageCount: 8,
    avgResponseTime: 1200,
    satisfactionRate: 88,
    status: 'active',
    lastMessage: {
      sender: 'Bot',
      content: 'Is there anything else I can help you with?'
    }
  },
  {
    id: '3',
    title: 'Billing Question',
    createdAt: '2024-01-13T09:15:00Z',
    endedAt: '2024-01-13T09:30:00Z',
    messageCount: 6,
    avgResponseTime: 650,
    satisfactionRate: 92,
    status: 'completed',
    lastMessage: {
      sender: 'User',
      content: 'That answers my question, thanks!'
    }
  }
])

// Computed
const filteredConversations = computed(() => {
  let filtered = conversations.value

  // Apply search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(conv => 
      conv.title?.toLowerCase().includes(query) ||
      conv.lastMessage?.content?.toLowerCase().includes(query)
    )
  }

  // Apply date range filter
  if (dateRange.value) {
    const now = new Date()
    let cutoffDate = new Date()

    switch (dateRange.value) {
      case 'today':
        cutoffDate.setHours(0, 0, 0, 0)
        break
      case 'yesterday':
        cutoffDate.setDate(cutoffDate.getDate() - 1)
        cutoffDate.setHours(0, 0, 0, 0)
        break
      case '7days':
        cutoffDate.setDate(cutoffDate.getDate() - 7)
        break
      case '30days':
        cutoffDate.setDate(cutoffDate.getDate() - 30)
        break
      case '90days':
        cutoffDate.setDate(cutoffDate.getDate() - 90)
        break
    }

    filtered = filtered.filter(conv => new Date(conv.createdAt) >= cutoffDate)
  }

  // Apply active filter
  if (showOnlyActive.value) {
    filtered = filtered.filter(conv => conv.status === 'active')
  }

  return filtered.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
})

const totalConversations = computed(() => conversations.value.length)
const totalPages = computed(() => Math.ceil(filteredConversations.value.length / pageSize.value))

const paginatedConversations = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredConversations.value.slice(start, end)
})

// Methods
const getConversationStatusClass = (conversation) => {
  switch (conversation.status) {
    case 'active':
      return 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400'
    case 'completed':
      return 'bg-blue-100 text-blue-600 dark:bg-blue-900/20 dark:text-blue-400'
    case 'abandoned':
      return 'bg-gray-100 text-gray-600 dark:bg-gray-900/20 dark:text-gray-400'
    default:
      return 'bg-gray-100 text-gray-600'
  }
}

const getConversationStatusText = (conversation) => {
  switch (conversation.status) {
    case 'active':
      return 'Active'
    case 'completed':
      return 'Completed'
    case 'abandoned':
      return 'Abandoned'
    default:
      return 'Unknown'
  }
}

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getDuration = (startDate, endDate) => {
  const start = new Date(startDate)
  const end = endDate ? new Date(endDate) : new Date()
  const duration = end - start

  if (duration < 60000) {
    return Math.round(duration / 1000) + 's'
  } else if (duration < 3600000) {
    return Math.round(duration / 60000) + 'm'
  } else {
    return Math.round(duration / 3600000) + 'h'
  }
}

const viewConversation = (conversation) => {
  emit('viewConversation', conversation)
}

const exportConversation = (conversation) => {
  const exportData = {
    conversation: conversation,
    bot: props.bot,
    exportedAt: new Date().toISOString()
  }

  const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `conversation-${conversation.id}-${new Date().toISOString().split('T')[0]}.json`
  link.click()
  URL.revokeObjectURL(url)
}

const deleteConversation = (conversation) => {
  if (confirm(`Are you sure you want to delete this conversation?`)) {
    // Implement delete logic
    console.log('Delete conversation:', conversation)
  }
}

const exportHistory = () => {
  const exportData = {
    conversations: filteredConversations.value,
    bot: props.bot,
    exportedAt: new Date().toISOString(),
    filters: {
      searchQuery: searchQuery.value,
      dateRange: dateRange.value,
      showOnlyActive: showOnlyActive.value
    }
  }

  const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `chat-history-${props.bot?.botName}-${new Date().toISOString().split('T')[0]}.json`
  link.click()
  URL.revokeObjectURL(url)
}

// Lifecycle
onMounted(() => {
  // Load conversation history
  isLoading.value = true
  setTimeout(() => {
    isLoading.value = false
  }, 1000)
})
</script>

<style scoped>
.modal-backdrop {
  backdrop-filter: blur(4px);
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
