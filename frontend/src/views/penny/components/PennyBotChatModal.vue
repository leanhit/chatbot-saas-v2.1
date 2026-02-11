<template>
  <div class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full z-50">
    <div class="relative min-h-screen flex items-center justify-center p-4">
      <div class="relative bg-white dark:bg-gray-800 rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:w-full sm:max-w-3xl">
        <!-- Header -->
        <div class="bg-white dark:bg-gray-800 px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
          <div class="flex items-center justify-between">
            <h3 class="text-lg leading-6 font-medium text-gray-900 dark:text-white">
              {{ $t('Chat with') }} {{ bot.name }}
            </h3>
            <button
              @click="$emit('close')"
              class="rounded-md text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              <Icon icon="mdi:close" class="h-6 w-6" />
            </button>
          </div>
        </div>

        <!-- Chat Messages -->
        <div class="bg-white dark:bg-gray-800 px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
          <div class="flex flex-col h-96">
            <!-- Messages Area -->
            <div ref="messagesContainer" class="flex-1 overflow-y-auto space-y-4 p-4 bg-gray-50 dark:bg-gray-700 rounded-lg mb-4">
              <div
                v-for="message in messages"
                :key="message.id"
                :class="[
                  'flex',
                  message.sender === 'user' ? 'justify-end' : 'justify-start'
                ]"
              >
                <div
                  :class="[
                    'max-w-xs lg:max-w-md px-4 py-2 rounded-lg',
                    message.sender === 'user'
                      ? 'bg-blue-600 text-white'
                      : 'bg-gray-200 dark:bg-gray-600 text-gray-900 dark:text-white'
                  ]"
                >
                  <p class="text-sm">{{ message.text }}</p>
                  <p class="text-xs mt-1 opacity-70">
                    {{ formatTime(message.timestamp) }}
                  </p>
                </div>
              </div>

              <!-- Typing Indicator -->
              <div v-if="isTyping" class="flex justify-start">
                <div class="bg-gray-200 dark:bg-gray-600 text-gray-900 dark:text-white max-w-xs lg:max-w-md px-4 py-2 rounded-lg">
                  <div class="flex items-center space-x-1">
                    <div class="flex space-x-1">
                      <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce"></div>
                      <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 0.1s"></div>
                      <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 0.2s"></div>
                    </div>
                    <span class="text-sm ml-2">{{ bot.name }} is typing...</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Message Input -->
            <div class="flex items-center space-x-2">
              <input
                v-model="newMessage"
                @keydown.enter="sendMessage"
                type="text"
                :placeholder="$t('Type your message...')"
                :disabled="sending"
                class="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
              />
              <button
                @click="sendMessage"
                :disabled="!newMessage.trim() || sending"
                class="inline-flex items-center px-4 py-2 border border-transparent rounded-lg shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <Icon v-if="sending" icon="mdi:loading" class="animate-spin h-4 w-4 mr-2" />
                <Icon v-else icon="mdi:send" class="h-4 w-4" />
              </button>
            </div>
          </div>
        </div>

        <!-- Footer -->
        <div class="bg-gray-50 dark:bg-gray-700 px-4 py-3 sm:px-6">
          <div class="flex items-center justify-between">
            <div class="text-sm text-gray-500 dark:text-gray-400">
              <p>{{ $t('Start a conversation with') }} {{ bot.name }}</p>
              <p class="text-xs mt-1">{{ $t('Type your message below to begin chatting') }}</p>
            </div>
            <div class="flex items-center space-x-2">
              <button
                @click="clearChat"
                class="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 dark:bg-gray-800 dark:border-gray-600 dark:text-white dark:hover:bg-gray-700"
              >
                <Icon icon="mdi:delete-sweep" class="h-4 w-4 mr-1" />
                {{ $t('Clear') }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { Icon } from '@iconify/vue'

const { t } = useI18n()

const props = defineProps({
  bot: {
    type: Object,
    required: true
  },
  show: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close'])

// State
const messages = ref([])
const newMessage = ref('')
const sending = ref(false)
const isTyping = ref(false)
const messagesContainer = ref(null)

// Mock responses for demo
const botResponses = [
  "Hello! I'm here to help you. What can I assist you with today?",
  "I understand your question. Let me help you with that.",
  "That's a great question! Based on what you've told me, I would suggest...",
  "Thank you for your message. I'm processing your request now.",
  "I'm here to help! Could you provide more details about what you need?"
]

// Methods
const sendMessage = async () => {
  if (!newMessage.value.trim() || sending.value) return

  const userMessage = {
    id: Date.now().toString(),
    sender: 'user',
    text: newMessage.value,
    timestamp: new Date()
  }

  messages.value.push(userMessage)
  const messageText = newMessage.value
  newMessage.value = ''
  sending.value = true

  // Scroll to bottom
  await nextTick()
  scrollToBottom()

  // Simulate bot typing
  isTyping.value = true
  await nextTick()
  scrollToBottom()

  // Simulate bot response
  setTimeout(() => {
    const botMessage = {
      id: (Date.now() + 1).toString(),
      sender: 'bot',
      text: botResponses[Math.floor(Math.random() * botResponses.length)],
      timestamp: new Date()
    }

    messages.value.push(botMessage)
    isTyping.value = false
    sending.value = false

    nextTick(() => {
      scrollToBottom()
    })
  }, 1500 + Math.random() * 1500)
}

const clearChat = () => {
  messages.value = []
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const formatTime = (timestamp) => {
  return new Intl.DateTimeFormat('en-US', {
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(timestamp))
}

// Watch for show prop changes
watch(() => props.show, (newShow) => {
  if (newShow) {
    // Add welcome message when chat opens
    if (messages.value.length === 0) {
      const welcomeMessage = {
        id: '0',
        sender: 'bot',
        text: `Hello! I'm ${props.bot.name}. ${t('How can I help you today?')}`,
        timestamp: new Date()
      }
      messages.value = [welcomeMessage]
    }
    
    nextTick(() => {
      scrollToBottom()
    })
  }
})

// Close modal when ESC key is pressed
onMounted(() => {
  const handleEscape = (e) => {
    if (e.key === 'Escape' && props.show) {
      emit('close')
    }
  }
  document.addEventListener('keydown', handleEscape)
  
  return () => {
    document.removeEventListener('keydown', handleEscape)
  }
})
</script>

<style scoped>
.animate-bounce {
  animation: bounce 1.4s infinite ease-in-out both;
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

.animate-spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
