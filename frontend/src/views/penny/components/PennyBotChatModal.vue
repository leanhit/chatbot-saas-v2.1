<template>
  <div class="penny-bot-chat-modal-backdrop" @click="closeOnBackdrop">
    <div class="penny-bot-chat-modal" @click.stop>
      <div class="modal-header">
        <div class="header-content">
          <div class="bot-info">
            <Icon :icon="getBotTypeIcon(bot.botType)" class="h-6 w-6 mr-3" />
            <div>
              <h2 class="modal-title">{{ bot.botName }}</h2>
              <p class="bot-type">{{ getBotTypeDisplayName(bot.botType) }}</p>
            </div>
          </div>
          <div class="header-actions">
            <div class="status-indicator" :class="{ online: bot.isActive && bot.isEnabled }">
              <div class="status-dot"></div>
              <span class="status-text">
                {{ bot.isActive && bot.isEnabled ? $t('Online') : $t('Offline') }}
              </span>
            </div>
            <button @click="$emit('close')" class="close-button">
              <Icon icon="mdi:close" class="h-5 w-5" />
            </button>
          </div>
        </div>
      </div>

      <div class="modal-body">
        <!-- Chat Messages -->
        <div ref="chatContainer" class="chat-container">
          <div v-if="messages.length === 0" class="empty-chat">
            <Icon icon="mdi:chat-outline" class="h-12 w-12 text-gray-400 mb-4" />
            <p class="empty-chat-text">{{ $t('Start a conversation with') }} {{ bot.botName }}</p>
            <p class="empty-chat-hint">{{ $t('Type a message below to begin chatting') }}</p>
          </div>

          <div v-else class="messages-list">
            <div
              v-for="(message, index) in messages"
              :key="index"
              :class="['message', message.type]"
            >
              <div class="message-content">
                <div class="message-avatar">
                  <Icon
                    :icon="message.type === 'user' ? 'mdi:account' : getBotTypeIcon(bot.botType)"
                    class="h-5 w-5"
                  />
                </div>
                <div class="message-bubble">
                  <p class="message-text">{{ message.text }}</p>
                  <p class="message-time">{{ formatTime(message.timestamp) }}</p>
                </div>
              </div>
            </div>

            <!-- Typing Indicator -->
            <div v-if="isTyping" class="message bot">
              <div class="message-content">
                <div class="message-avatar">
                  <Icon :icon="getBotTypeIcon(bot.botType)" class="h-5 w-5" />
                </div>
                <div class="message-bubble typing-bubble">
                  <div class="typing-indicator">
                    <div class="typing-dot"></div>
                    <div class="typing-dot"></div>
                    <div class="typing-dot"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Message Input -->
        <div class="message-input-container">
          <form @submit.prevent="sendMessage" class="message-form">
            <div class="input-wrapper">
              <input
                v-model="newMessage"
                type="text"
                :placeholder="$t('Type your message...')"
                class="message-input"
                :disabled="!bot.isActive || !bot.isEnabled || chatLoading"
                @keydown.enter.prevent="sendMessage"
              />
              <button
                type="submit"
                :disabled="!newMessage.trim() || !bot.isActive || !bot.isEnabled || chatLoading"
                class="send-button"
              >
                <Icon
                  v-if="chatLoading"
                  icon="mdi:loading"
                  class="animate-spin h-5 w-5"
                />
                <Icon
                  v-else
                  icon="mdi:send"
                  class="h-5 w-5"
                />
              </button>
            </div>
          </form>

          <!-- Quick Actions -->
          <div class="quick-actions">
            <button
              v-for="action in quickActions"
              :key="action.text"
              @click="sendQuickMessage(action.text)"
              :disabled="!bot.isActive || !bot.isEnabled || chatLoading"
              class="quick-action-button"
            >
              {{ action.text }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { useI18n } from 'vue-i18n'
import { getRelativeTime, formatTime } from '@/utils/dateUtils'
import { usePennyBotStore } from '@/stores/pennyBotStore'

export default {
  name: 'PennyBotChatModal',
  components: {
    Icon
  },
  props: {
    bot: {
      type: Object,
      required: true
    }
  },
  emits: ['close'],
  setup(props) {
    const { t } = useI18n()
    const pennyBotStore = usePennyBotStore()

    const messages = ref([])
    const newMessage = ref('')
    const isTyping = ref(false)
    const chatContainer = ref(null)
    const chatLoading = computed(() => pennyBotStore.chatLoading)

    const quickActions = [
      { text: 'Hello' },
      { text: 'How are you?' },
      { text: 'What can you do?' },
      { text: 'Help' },
      { text: 'Thank you' }
    ]

    const scrollToBottom = () => {
      nextTick(() => {
        if (chatContainer.value) {
          chatContainer.value.scrollTop = chatContainer.value.scrollHeight
        }
      })
    }

    const addMessage = (text, type = 'user') => {
      messages.value.push({
        text,
        type,
        timestamp: new Date()
      })
      scrollToBottom()
    }

    const sendMessage = async () => {
      if (!newMessage.value.trim() || !props.bot.isActive || !props.bot.isEnabled) {
        return
      }

      const userMessage = newMessage.value.trim()
      newMessage.value = ''

      // Add user message
      addMessage(userMessage, 'user')

      // Show typing indicator
      isTyping.value = true
      scrollToBottom()

      try {
        // Get bot response
        const response = await pennyBotStore.chatWithPennyBot(props.bot.botId, userMessage)
        
        // Hide typing indicator
        isTyping.value = false

        // Add bot response
        addMessage(response.response, 'bot')
      } catch (error) {
        isTyping.value = false
        console.error('Failed to send message:', error)
        
        // Add error message
        addMessage('Sorry, I encountered an error. Please try again.', 'bot')
      }
    }

    const sendQuickMessage = (message) => {
      newMessage.value = message
      sendMessage()
    }

    const closeOnBackdrop = (event) => {
      if (event.target === event.currentTarget) {
        props.$emit('close')
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
      const names = {
        'CUSTOMER_SERVICE': 'Customer Service',
        'SALES': 'Sales',
        'SUPPORT': 'Technical Support',
        'MARKETING': 'Marketing',
        'HR': 'Human Resources',
        'FINANCE': 'Finance',
        'GENERAL': 'General Purpose'
      }
      return names[botType] || botType
    }

    // Add welcome message when modal opens
    onMounted(() => {
      if (props.bot.isActive && props.bot.isEnabled) {
        addMessage(`Hello! I'm ${props.bot.botName}, your ${getBotTypeDisplayName(props.bot.botType)} assistant. How can I help you today?`, 'bot')
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
      formatTime
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
  align-items: flex-start;
  justify-content: center;
  z-index: 1000;
  padding: 40px 20px 20px;
}

.penny-bot-chat-modal {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  width: 100%;
  max-width: 600px;
  height: 600px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  margin-top: 20px;
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
}

.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f8fafc;
}

.dark .chat-container {
  background: #111827;
}

.empty-chat {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
}

.empty-chat-text {
  font-size: 16px;
  color: #374151;
  margin-bottom: 8px;
}

.dark .empty-chat-text {
  color: #d1d5db;
}

.empty-chat-hint {
  font-size: 14px;
  color: #6b7280;
}

.dark .empty-chat-hint {
  color: #9ca3af;
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
  border-radius: 24px;
  font-size: 14px;
  background: white;
  color: #1f2937;
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
