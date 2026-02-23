<template>
  <div class="chat-interface flex flex-col h-full bg-white dark:bg-gray-800">
    <!-- Chat Header -->
    <div class="flex items-center justify-between p-4 border-b border-gray-200 dark:border-gray-700">
      <div class="flex items-center gap-3">
        <div :class="getBotTypeIconClass(bot.botType)" class="p-2 rounded-lg">
          <Icon :icon="getBotTypeIcon(bot.botType)" class="text-lg" />
        </div>
        <div>
          <h3 class="font-semibold text-gray-900 dark:text-gray-100">{{ bot.botName }}</h3>
          <div class="flex items-center gap-2">
            <div :class="getStatusDotClass(bot)" class="w-2 h-2 rounded-full"></div>
            <span class="text-sm text-gray-600 dark:text-gray-400">
              {{ getStatusText(bot) }}
            </span>
          </div>
        </div>
      </div>
      
      <div class="flex items-center gap-2">
        <button 
          @click="clearChat"
          class="p-2 text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-100 transition-colors"
          title="Clear Chat"
        >
          <Icon icon="ic:baseline-clear-all" />
        </button>
        <button 
          @click="downloadChat"
          class="p-2 text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-100 transition-colors"
          title="Download Chat"
        >
          <Icon icon="ic:baseline-download" />
        </button>
        <button 
          @click="$emit('close')"
          class="p-2 text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-100 transition-colors"
          title="Close Chat"
        >
          <Icon icon="ic:baseline-close" />
        </button>
      </div>
    </div>

    <!-- Messages Container -->
    <div 
      ref="messagesContainer"
      class="flex-1 overflow-y-auto p-4 space-y-4"
      @scroll="handleScroll"
    >
      <!-- Welcome Message -->
      <div v-if="messages.length === 0" class="text-center py-8">
        <div :class="getBotTypeIconClass(bot.botType)" class="p-4 rounded-full inline-block mb-4">
          <Icon :icon="getBotTypeIcon(bot.botType)" class="text-3xl" />
        </div>
        <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-2">
          Welcome to {{ bot.botName }}
        </h3>
        <p class="text-gray-600 dark:text-gray-400">
          I'm here to help you. Feel free to ask me anything!
        </p>
      </div>

      <!-- Messages -->
      <div 
        v-for="message in messages" 
        :key="message.id"
        class="flex gap-3"
        :class="message.sender === 'user' ? 'justify-end' : 'justify-start'"
      >
        <!-- Bot Avatar -->
        <div 
          v-if="message.sender === 'bot'"
          :class="getBotTypeIconClass(bot.botType)"
          class="p-2 rounded-lg flex-shrink-0"
        >
          <Icon :icon="getBotTypeIcon(bot.botType)" class="text-sm" />
        </div>

        <!-- Message Bubble -->
        <div 
          class="max-w-xs lg:max-w-md px-4 py-2 rounded-lg"
          :class="getMessageBubbleClass(message)"
        >
          <!-- Message Content -->
          <div class="text-sm">
            <p v-if="message.type === 'text'" class="text-gray-900 dark:text-gray-100">
              {{ message.content }}
            </p>
            
            <!-- File Message -->
            <div v-else-if="message.type === 'file'" class="flex items-center gap-2">
              <Icon icon="ic:baseline-attach-file" class="text-gray-500" />
              <span class="text-gray-900 dark:text-gray-100">{{ message.fileName }}</span>
              <button 
                @click="downloadFile(message)"
                class="text-blue-600 dark:text-blue-400 hover:underline"
              >
                Download
              </button>
            </div>
            
            <!-- Image Message -->
            <div v-else-if="message.type === 'image'" class="space-y-2">
              <img 
                :src="message.imageUrl" 
                :alt="message.fileName"
                class="max-w-full rounded-lg"
              />
              <p class="text-gray-900 dark:text-gray-100">{{ message.content }}</p>
            </div>
            
            <!-- Typing Indicator -->
            <div v-else-if="message.type === 'typing'" class="flex items-center gap-1">
              <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 0ms"></div>
              <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 150ms"></div>
              <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 300ms"></div>
            </div>
          </div>
          
          <!-- Message Metadata -->
          <div class="flex items-center gap-2 mt-1">
            <span class="text-xs text-gray-500 dark:text-gray-400">
              {{ formatTime(message.timestamp) }}
            </span>
            <span v-if="message.status" :class="getStatusIconClass(message.status)">
              <Icon :icon="getStatusIcon(message.status)" class="text-xs" />
            </span>
          </div>
        </div>

        <!-- User Avatar -->
        <div 
          v-if="message.sender === 'user'"
          class="p-2 bg-gray-200 dark:bg-gray-700 rounded-lg flex-shrink-0"
        >
          <Icon icon="ic:baseline-person" class="text-sm text-gray-600 dark:text-gray-400" />
        </div>
      </div>

      <!-- Typing Indicator -->
      <div v-if="isTyping" class="flex gap-3 justify-start">
        <div :class="getBotTypeIconClass(bot.botType)" class="p-2 rounded-lg flex-shrink-0">
          <Icon :icon="getBotTypeIcon(bot.botType)" class="text-sm" />
        </div>
        <div class="bg-gray-100 dark:bg-gray-700 px-4 py-2 rounded-lg">
          <div class="flex items-center gap-1">
            <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 0ms"></div>
            <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 150ms"></div>
            <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 300ms"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- Message Input -->
    <div class="border-t border-gray-200 dark:border-gray-700 p-4">
      <!-- File Upload Preview -->
      <div v-if="uploadedFile" class="mb-3 flex items-center gap-2 p-2 bg-gray-50 dark:bg-gray-700 rounded-lg">
        <Icon icon="ic:baseline-attach-file" class="text-gray-500" />
        <span class="text-sm text-gray-900 dark:text-gray-100">{{ uploadedFile.name }}</span>
        <button 
          @click="removeUploadedFile"
          class="ml-auto text-red-600 dark:text-red-400 hover:text-red-700 dark:hover:text-red-300"
        >
          <Icon icon="ic:baseline-close" />
        </button>
      </div>

      <!-- Input Area -->
      <div class="flex gap-2">
        <div class="flex-1 relative">
          <textarea 
            v-model="messageInput"
            @keydown="handleKeyDown"
            @input="handleInput"
            placeholder="Type your message..."
            rows="1"
            class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
            :disabled="isSending || !isBotActive"
          ></textarea>
          
          <!-- Character Count -->
          <div v-if="messageInput.length > 0" class="absolute bottom-2 right-2 text-xs text-gray-500">
            {{ messageInput.length }}/1000
          </div>
        </div>
        
        <!-- Action Buttons -->
        <div class="flex gap-2">
          <button 
            @click="triggerFileUpload"
            :disabled="isSending || !isBotActive"
            class="p-2 text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-100 transition-colors"
            title="Attach File"
          >
            <Icon icon="ic:baseline-attach-file" />
          </button>
          
          <button 
            @click="sendMessage"
            :disabled="!canSend"
            class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center gap-2"
          >
            <Icon v-if="isSending" icon="ic:baseline-refresh" class="animate-spin" />
            <Icon v-else icon="ic:baseline-send" />
          </button>
        </div>
      </div>

      <!-- Quick Actions -->
      <div v-if="quickActions.length > 0" class="mt-3 flex flex-wrap gap-2">
        <button 
          v-for="action in quickActions" 
          :key="action.id"
          @click="sendQuickAction(action)"
          class="px-3 py-1 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-full text-sm hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors"
        >
          {{ action.label }}
        </button>
      </div>
    </div>

    <!-- Hidden File Input -->
    <input 
      ref="fileInput"
      type="file"
      @change="handleFileUpload"
      class="hidden"
      accept="image/*,.pdf,.doc,.docx,.txt"
    />
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue'
import { usePennyBotStore } from '@/stores/penny/pennyBotStore'

const props = defineProps({
  bot: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close'])

const pennyBotStore = usePennyBotStore()

// State
const messages = ref([])
const messageInput = ref('')
const isSending = ref(false)
const isTyping = ref(false)
const uploadedFile = ref(null)
const messagesContainer = ref(null)
const fileInput = ref(null)

// Quick actions for different bot types
const quickActions = computed(() => {
  const actionMap = {
    'CUSTOMER_SERVICE': [
      { id: 'faq', label: 'View FAQ' },
      { id: 'contact', label: 'Contact Support' },
      { id: 'ticket', label: 'Create Ticket' }
    ],
    'SALES': [
      { id: 'products', label: 'View Products' },
      { id: 'pricing', label: 'Pricing' },
      { id: 'demo', label: 'Request Demo' }
    ],
    'SUPPORT': [
      { id: 'troubleshoot', label: 'Troubleshoot' },
      { id: 'docs', label: 'Documentation' },
      { id: 'escalate', label: 'Escalate' }
    ],
    'MARKETING': [
      { id: 'campaigns', label: 'View Campaigns' },
      { id: 'newsletter', label: 'Newsletter' },
      { id: 'offers', label: 'Special Offers' }
    ]
  }
  return actionMap[props.bot.botType] || []
})

// Computed
const isBotActive = computed(() => {
  return props.bot.isActive && props.bot.isEnabled
})

const canSend = computed(() => {
  return messageInput.value.trim() !== '' && 
         !isSending.value && 
         isBotActive.value &&
         messageInput.value.length <= 1000
})

// Methods
const getBotTypeIcon = (botType) => {
  const iconMap = {
    'CUSTOMER_SERVICE': 'ic:baseline-support-agent',
    'SALES': 'ic:baseline-shopping-cart',
    'SUPPORT': 'ic:baseline-headset-mic',
    'MARKETING': 'ic:baseline-campaign',
    'HR': 'ic:baseline-people',
    'FINANCE': 'ic:baseline-account-balance',
    'GENERAL': 'ic:baseline-settings'
  }
  return iconMap[botType] || 'ic:baseline-settings'
}

const getBotTypeIconClass = (botType) => {
  const classMap = {
    'CUSTOMER_SERVICE': 'bg-blue-100 text-blue-600 dark:bg-blue-900/20 dark:text-blue-400',
    'SALES': 'bg-emerald-100 text-emerald-600 dark:bg-emerald-900/20 dark:text-emerald-400',
    'SUPPORT': 'bg-amber-100 text-amber-600 dark:bg-amber-900/20 dark:text-amber-400',
    'MARKETING': 'bg-purple-100 text-purple-600 dark:bg-purple-900/20 dark:text-purple-400',
    'HR': 'bg-pink-100 text-pink-600 dark:bg-pink-900/20 dark:text-pink-400',
    'FINANCE': 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400',
    'GENERAL': 'bg-gray-100 text-gray-600'
  }
  return classMap[botType] || 'bg-gray-100 text-gray-600'
}

const getStatusDotClass = (bot) => {
  if (!bot.isActive || !bot.isEnabled) {
    return 'bg-gray-400'
  }
  return 'bg-green-500'
}

const getStatusText = (bot) => {
  if (!bot.isActive) return 'Inactive'
  if (!bot.isEnabled) return 'Disabled'
  return 'Online'
}

const getMessageBubbleClass = (message) => {
  if (message.sender === 'user') {
    return 'bg-blue-600 text-white'
  }
  return 'bg-gray-100 dark:bg-gray-700 text-gray-900 dark:text-gray-100'
}

const getStatusIcon = (status) => {
  const iconMap = {
    'sent': 'ic:baseline-check',
    'delivered': 'ic:baseline-check-double',
    'read': 'ic:baseline-check-double',
    'failed': 'ic:baseline-error'
  }
  return iconMap[status] || 'ic:baseline-check'
}

const getStatusIconClass = (status) => {
  const classMap = {
    'sent': 'text-gray-400',
    'delivered': 'text-blue-600 dark:text-blue-400',
    'read': 'text-blue-600 dark:text-blue-400',
    'failed': 'text-red-600 dark:text-red-400'
  }
  return classMap[status] || 'text-gray-400'
}

const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleTimeString('en-US', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

const handleKeyDown = (event) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

const handleInput = (event) => {
  // Auto-resize textarea
  event.target.style.height = 'auto'
  event.target.style.height = Math.min(event.target.scrollHeight, 120) + 'px'
}

const sendMessage = async () => {
  if (!canSend.value) return

  const userMessage = {
    id: Date.now(),
    sender: 'user',
    type: 'text',
    content: messageInput.value.trim(),
    timestamp: new Date().toISOString(),
    status: 'sent'
  }

  messages.value.push(userMessage)
  const messageContent = messageInput.value
  messageInput.value = ''
  
  // Reset textarea height
  const textarea = messagesContainer.value?.querySelector('textarea')
  if (textarea) {
    textarea.style.height = 'auto'
  }

  // Scroll to bottom
  await scrollToBottom()

  // Show typing indicator
  isTyping.value = true

  try {
    isSending.value = true
    
    // Send message to bot
    const response = await pennyBotStore.chatWithBot(props.bot.id, messageContent)
    
    // Remove typing indicator
    isTyping.value = false
    
    // Add bot response
    const botMessage = {
      id: Date.now() + 1,
      sender: 'bot',
      type: 'text',
      content: response.response,
      timestamp: response.timestamp,
      status: 'delivered'
    }
    
    messages.value.push(botMessage)
    
    // Update message status
    userMessage.status = 'delivered'
    
  } catch (error) {
    isTyping.value = false
    
    // Add error message
    const errorMessage = {
      id: Date.now() + 1,
      sender: 'bot',
      type: 'text',
      content: 'Sorry, I encountered an error. Please try again.',
      timestamp: new Date().toISOString(),
      status: 'failed'
    }
    
    messages.value.push(errorMessage)
    userMessage.status = 'failed'
  } finally {
    isSending.value = false
    await scrollToBottom()
  }
}

const sendQuickAction = async (action) => {
  messageInput.value = action.label
  await sendMessage()
}

const triggerFileUpload = () => {
  fileInput.value?.click()
}

const handleFileUpload = (event) => {
  const file = event.target.files[0]
  if (file) {
    uploadedFile.value = file
    
    // Auto-send file message
    const fileMessage = {
      id: Date.now(),
      sender: 'user',
      type: 'file',
      content: `Shared file: ${file.name}`,
      fileName: file.name,
      fileSize: file.size,
      timestamp: new Date().toISOString(),
      status: 'sent'
    }
    
    messages.value.push(fileMessage)
    uploadedFile.value = null
    event.target.value = ''
  }
}

const removeUploadedFile = () => {
  uploadedFile.value = null
}

const downloadFile = (message) => {
  // Implement file download logic
  console.log('Download file:', message.fileName)
}

const clearChat = () => {
  if (confirm('Are you sure you want to clear this chat?')) {
    messages.value = []
  }
}

const downloadChat = () => {
  const chatData = {
    bot: props.bot.botName,
    botType: props.bot.botType,
    timestamp: new Date().toISOString(),
    messages: messages.value
  }
  
  const blob = new Blob([JSON.stringify(chatData, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `chat-${props.bot.botName}-${new Date().toISOString().split('T')[0]}.json`
  link.click()
  URL.revokeObjectURL(url)
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const handleScroll = () => {
  // Handle scroll events for loading older messages
}

// Lifecycle
onMounted(() => {
  scrollToBottom()
})

onUnmounted(() => {
  // Cleanup
})
</script>

<style scoped>
.chat-interface {
  height: 600px;
  max-height: 80vh;
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

.animate-bounce {
  animation: bounce 1.4s infinite;
}
</style>
