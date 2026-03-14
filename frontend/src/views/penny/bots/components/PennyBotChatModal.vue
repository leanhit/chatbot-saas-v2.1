<template>
  <div class="penny-bot-chat-modal-backdrop" @click="closeOnBackdrop">
    <div class="penny-bot-chat-modal" @click.stop>
      <div class="modal-header">
        <div class="header-content">
          <div class="bot-info">
            <Icon :icon="getBotTypeIcon(bot.botType)" class="h-6 w-6 mr-3" />
            <div>
              <h2 class="modal-title">
                {{ bot.botName }}
                <span v-if="isTestMode" class="test-mode-badge">TEST MODE</span>
              </h2>
              <p class="bot-type">{{ getBotTypeDisplayName(bot.botType) }}</p>
            </div>
          </div>
          <div class="header-actions">
            <!-- Mode Selector -->
            <div class="mode-selector">
              <button
                @click="switchMode(false)"
                :class="['mode-btn', { active: !isTestMode }]"
                class="text-xs"
              >
                {{ $t('Chat') }}
              </button>
              <button
                @click="switchMode(true)"
                :class="['mode-btn', { active: isTestMode }]"
                class="text-xs"
              >
                {{ $t('Test') }}
              </button>
            </div>
            <div class="status-indicator" :class="{ online: bot.isFullyActive() }">
              <div class="status-dot"></div>
              <span class="status-text">
                {{ bot.isFullyActive() ? $t('Online') : $t('Offline') }}
              </span>
            </div>
            <button @click="$emit('close')" class="close-button">
              <Icon icon="mdi:close" class="h-5 w-5" />
            </button>
          </div>
        </div>
      </div>

      <div class="modal-body">
        <!-- Chat Container Component -->
        <ChatContainer
          ref="chatContainerRef"
          :messages="messages"
          :is-typing="isTyping"
          :bot-name="bot.botName"
          :disabled="!bot.isFullyActive() || chatLoading"
          :loading="chatLoading"
          @send-message="handleSendMessage"
          @scroll-to-bottom="handleScrollToBottom"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { Icon } from '@iconify/vue'
import { useI18n } from 'vue-i18n'
import { getRelativeTime } from '@/utils/dateUtils'
import { usePennyBotStore } from '@/stores/pennyBotStore'
import ChatContainer from './ChatContainer.vue'
import {
  PennyBotType,
  PennyBotTypeDisplay,
  MiddlewareRequest,
  MiddlewareResponse
} from '@/types/penny'

export default {
  name: 'PennyBotChatModal',
  components: {
    Icon,
    ChatContainer
  },
  props: {
    bot: {
      type: Object,
      required: true
    },
    isTestMode: {
      type: Boolean,
      default: false
    }
  },
  emits: ['close', 'modeChanged'],
  setup(props, { emit }) {
    const { t } = useI18n()
    const pennyBotStore = usePennyBotStore()

    const messages = ref([])
    const newMessage = ref('')
    const isTyping = ref(false)
    const chatContainerRef = ref(null)
    const chatLoading = computed(() => pennyBotStore.chatLoading)
    const quickReplies = ref([])

    const quickActions = [
      { text: 'Hello' },
      { text: 'How are you?' },
      { text: 'What can you do?' },
      { text: 'Help' },
      { text: 'Thank you' }
    ]

    const scrollToBottom = () => {
      nextTick(() => {
        if (chatContainerRef.value) {
          chatContainerRef.value.scrollToBottom()
        }
      })
    }

    const addMessage = (message) => {
      messages.value.push(message)
      scrollToBottom()
    }

    // Handle events from ChatContainer
    const handleSendMessage = (message) => {
      // Add user message to chat
      const userChatMessage = {
        type: 'user',
        text: message,
        timestamp: new Date()
      }
      addMessage(userChatMessage)

      // Show typing indicator
      isTyping.value = true

      // Send message to Penny bot
      sendToPennyBot(message)
    }

    const handleScrollToBottom = () => {
      // Handle scroll events if needed
    }

    const sendToPennyBot = async (message) => {
      try {
        // Send message to Penny bot
        const response = await pennyBotStore.chatWithPennyBot(props.bot.id, message, props.isTestMode)
        
        console.log('🔍 Full API response:', response)
        console.log('🔍 Response data:', response.data)
        
        // Backend returns data directly in response, not in response.data
        const responseData = response.data || response || {}
        console.log('🔍 Parsed response data:', responseData)
        console.log('🔍 Response text:', responseData.response)
        
        // Add bot response to chat
        const botChatMessage = {
          type: 'bot',
          text: responseData.response || responseData.message || 'Xin lỗi, tôi không hiểu yêu cầu của bạn.',
          timestamp: new Date(),
          metadata: {
            providerUsed: responseData.providerUsed || 'PENNYBOT',
            confidence: responseData.confidence || 0.8,
            intentAnalysis: responseData.intentAnalysis || {},
            processingMetrics: responseData.processingMetrics || {}
          }
        }
        addMessage(botChatMessage)

        // Add quick replies if available
        if (responseData.quickReplies && responseData.quickReplies.length > 0) {
          quickReplies.value = responseData.quickReplies
        }

      } catch (error) {
        console.error('Failed to send message:', error)
        
        // Add error message
        const errorMessage = {
          type: 'bot',
          text: 'Xin lỗi, đã xảy ra lỗi khi xử lý tin nhắn của bạn. Vui lòng thử lại sau.',
          timestamp: new Date(),
          isError: true
        }
        addMessage(errorMessage)
      } finally {
        isTyping.value = false
        scrollToBottom()
      }
    }

    // Listen for WebSocket conversation messages
    const handleConversationMessage = (event) => {
      const { conversationId, sender, message, timestamp } = event.detail
      
      // Add WebSocket message to chat if it belongs to current conversation
      // (Assuming bot.id represents the conversation context)
      if (sender && message) {
        const websocketMessage = {
          type: sender === 'user' ? 'user' : 'bot',
          text: message,
          timestamp: new Date(timestamp),
          isRealtime: true
        }
        addMessage(websocketMessage)
      }
    }

    // Setup WebSocket listener
    onMounted(() => {
      window.addEventListener('conversationMessage', handleConversationMessage)
      scrollToBottom() // Initial scroll to bottom
    })

    // Cleanup WebSocket listener
    onUnmounted(() => {
      window.removeEventListener('conversationMessage', handleConversationMessage)
    })

    const sendMessage = async () => {
      if (!newMessage.value.trim() || chatLoading.value) return

      const userMessage = newMessage.value.trim()
      newMessage.value = ''

      // Add user message to chat
      const userChatMessage = {
        type: 'user',
        text: userMessage,
        timestamp: new Date()
      }
      addMessage(userChatMessage)

      // Show typing indicator
      isTyping.value = true

      try {
        // Send message to Penny bot
        const response = await pennyBotStore.chatWithPennyBot(props.bot.id, userMessage, props.isTestMode)
        
        console.log('🔍 Full API response:', response)
        console.log('🔍 Response data:', response.data)
        
        // Backend returns data directly in response, not in response.data
        const responseData = response.data || response || {}
        console.log('🔍 Parsed response data:', responseData)
        console.log('🔍 Response text:', responseData.response)
        
        // Add bot response to chat
        const botChatMessage = {
          type: 'bot',
          text: responseData.response || responseData.message || 'Xin lỗi, tôi không hiểu yêu cầu của bạn.',
          timestamp: new Date(),
          metadata: {
            providerUsed: responseData.providerUsed || 'PENNYBOT',
            confidence: responseData.confidence || 0.8,
            intentAnalysis: responseData.intentAnalysis || {},
            processingMetrics: responseData.processingMetrics || {}
          }
        }
        addMessage(botChatMessage)

        // Add quick replies if available
        if (responseData.quickReplies && responseData.quickReplies.length > 0) {
          quickReplies.value = responseData.quickReplies
        }

      } catch (error) {
        console.error('Failed to send message:', error)
        
        // Add error message
        const errorMessage = {
          type: 'bot',
          text: 'Xin lỗi, đã xảy ra lỗi khi xử lý tin nhắn của bạn. Vui lòng thử lại sau.',
          timestamp: new Date(),
          isError: true
        }
        addMessage(errorMessage)
      } finally {
        isTyping.value = false
        scrollToBottom()
      }
    }

    const sendQuickMessage = (message) => {
      newMessage.value = message
      sendMessage()
    }

    const closeOnBackdrop = (event) => {
      if (event.target === event.currentTarget) {
        emit('close')
      }
    }

    const getBotTypeIcon = (botType) => {
      const icons = {
        'CUSTOMER_SERVICE': 'mdi:headset',
        'SALES': 'mdi:cash-register',
        'SUPPORT': 'mdi:tools',
        'MARKETING': 'mdi:bullhorn',
        'HR': 'mdi:account-tie',
        'FINANCE': 'mdi:currency-usd',
        'GENERAL': 'mdi:robot'
      }
      return icons[botType] || 'mdi:robot'
    }

    const getBotTypeDisplayName = (botType) => {
      return PennyBotTypeDisplay[botType] || botType
    }

    const switchMode = (testMode) => {
      emit('modeChanged', testMode)
    }

    // Add welcome message when modal opens
    onMounted(() => {
      if (props.bot.isFullyActive()) {
        const welcomeMessage = {
          type: 'bot',
          text: props.isTestMode 
            ? `🧪 Test Mode: I'm ${props.bot.botName}, your ${getBotTypeDisplayName(props.bot.botType)} assistant. Send test messages to verify functionality!`
            : `Hello! I'm ${props.bot.botName}, your ${getBotTypeDisplayName(props.bot.botType)} assistant. How can I help you today?`,
          timestamp: new Date()
        }
        addMessage(welcomeMessage)
      }
    })

    return {
      messages,
      newMessage,
      isTyping,
      chatContainer,
      chatLoading,
      quickActions,
      sendMessage,
      sendQuickMessage,
      closeOnBackdrop,
      getBotTypeIcon,
      getBotTypeDisplayName,
      getRelativeTime,
      switchMode
    }
  }
}
</script>

<style scoped>
.penny-bot-chat-modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center; /* ← THAY ĐỔI: Center vertical thay vì flex-start */
  justify-content: center;
  z-index: 1000;
  padding: 20px;
  overflow: auto;
  max-height: 100vh;
}

.penny-bot-chat-modal {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  width: 100%;
  max-width: 600px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  margin: 20px 0; /* ← THAY ĐỔI: Thay vì chỉ margin-top */
}

.dark .penny-bot-chat-modal {
  background: #1f2937;
}

.modal-header {
  padding: 20px;
  border-bottom: 1px solid #e5e7eb;
  flex-shrink: 0;
}

.dark .modal-header {
  border-bottom-color: #374151;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.bot-info {
  display: flex;
  align-items: center;
}

.modal-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.dark .modal-title {
  color: white;
}

.bot-type {
  margin: 4px 0 0 0;
  font-size: 14px;
  color: #6b7280;
}

.dark .bot-type {
  color: #9ca3af;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  background: #f3f4f6;
}

.dark .status-indicator {
  background: #374151;
}

.status-indicator.online {
  background: #dcfce7;
}

.dark .status-indicator.online {
  background: #14532d;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #6b7280;
}

.status-indicator.online .status-dot {
  background: #16a34a;
  animation: pulse 2s infinite;
}

.status-text {
  font-size: 12px;
  font-weight: 500;
  color: #6b7280;
}

.status-indicator.online .status-text {
  color: #166534;
}

.dark .status-text {
  color: #9ca3af;
}

.dark .status-indicator.online .status-text {
  color: #86efac;
}

.close-button {
  background: none;
  border: none;
  padding: 8px;
  border-radius: 6px;
  cursor: pointer;
  color: #6b7280;
  transition: all 0.2s;
}

.close-button:hover {
  background-color: #f3f4f6;
  color: #1f2937;
}

.dark .close-button {
  color: #9ca3af;
}

.dark .close-button:hover {
  background-color: #374151;
  color: white;
}

.modal-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  max-height: 70vh;
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message {
  display: flex;
  align-items: flex-start;
}

.message.user {
  flex-direction: row-reverse;
}

.message-content {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  max-width: 80%;
}

.message.user .message-content {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
  flex-shrink: 0;
}

.message.bot .message-avatar {
  background: #3b82f6;
  color: white;
}

.message.user .message-avatar {
  background: #10b981;
  color: white;
}

.message-bubble {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 12px 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.dark .message-bubble {
  background: #374151;
  border-color: #4b5563;
  color: #f3f4f6;
}

.message.user .message-bubble {
  background: #3b82f6;
  border-color: #3b82f6;
  color: white;
}

.message-text {
  margin: 0;
  font-size: 14px;
  line-height: 1.4;
  word-wrap: break-word;
}

.message-time {
  margin: 4px 0 0 0;
  font-size: 11px;
  opacity: 0.7;
}

/* Real-time message styling */
.message.realtime .message-bubble {
  position: relative;
  border: 2px solid #10b981;
  box-shadow: 0 2px 4px rgba(16, 185, 129, 0.2);
}

.message.realtime .message-bubble::before {
  content: '';
  position: absolute;
  top: -2px;
  right: -2px;
  width: 8px;
  height: 8px;
  background: #10b981;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.7);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(16, 185, 129, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(16, 185, 129, 0);
  }
}

.typing-bubble {
  padding: 12px 16px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
}

.dark .typing-bubble {
  background: #374151;
  border-color: #4b5563;
}

.typing-indicator {
  display: flex;
  gap: 4px;
}

.typing-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #6b7280;
  animation: typing 1.4s infinite;
}

.dark .typing-dot {
  background: #9ca3af;
}

.typing-dot:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dot:nth-child(3) {
  animation-delay: 0.4s;
}

.message-input-container {
  padding: 20px;
  border-top: 1px solid #e5e7eb;
  background: white;
  flex-shrink: 0;
}

.dark .message-input-container {
  background: #1f2937;
  border-top-color: #374151;
}

.message-form {
  margin-bottom: 12px;
}

.input-wrapper {
  display: flex;
  gap: 8px;
}

.message-input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background: white;
  font-size: 14px;
  line-height: 1.5;
  resize: horizontal;
  min-width: 200px;
  max-width: 100%;
  transition: all 0.2s;
}

.dark .message-input {
  background: #374151;
  border-color: #4b5563;
  color: white;
}

.message-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.message-input:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  resize: none;
}

/* Resize handle styling */
.message-input::-webkit-resizer {
  background: #9ca3af;
  border-radius: 2px;
}

.message-input::-moz-resizer {
  background: #9ca3af;
  border-radius: 2px;
}

.dark .message-input::-webkit-resizer {
  background: #6b7280;
}

.dark .message-input::-moz-resizer {
  background: #6b7280;
}

.test-mode-badge {
  display: inline-block;
  padding: 2px 8px;
  margin-left: 8px;
  font-size: 10px;
  font-weight: 600;
  background: #f59e0b;
  color: white;
  border-radius: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.mode-selector {
  display: flex;
  background: #f3f4f6;
  border-radius: 6px;
  padding: 2px;
  margin-right: 12px;
}

.dark .mode-selector {
  background: #374151;
}

.mode-btn {
  padding: 4px 8px;
  border: none;
  background: transparent;
  color: #6b7280;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 500;
}

.mode-btn.active {
  background: white;
  color: #3b82f6;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.dark .mode-btn.active {
  background: #1f2937;
  color: #60a5fa;
}

.send-button {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: none;
  background: #3b82f6;
  color: white;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.send-button:hover:not(:disabled) {
  background: #2563eb;
}

.send-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.quick-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.quick-action-button {
  padding: 6px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  background: white;
  color: #6b7280;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.dark .quick-action-button {
  background: #374151;
  border-color: #4b5563;
  color: #9ca3af;
}

.quick-action-button:hover:not(:disabled) {
  background: #f3f4f6;
  border-color: #3b82f6;
  color: #3b82f6;
}

.dark .quick-action-button:hover:not(:disabled) {
  background: #4b5563;
  color: #93c5fd;
}

.quick-action-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-10px);
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.animate-spin {
  animation: spin 1s linear infinite;
}
</style>
