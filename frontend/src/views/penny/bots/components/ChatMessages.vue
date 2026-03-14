<template>
  <div class="flex flex-col h-96">
    <!-- Messages Area -->
    <div ref="messagesContainer" class="flex-1 overflow-y-auto space-y-4 p-4 bg-gray-50 dark:bg-gray-700 rounded-lg mb-4">
      <div v-if="messages.length === 0" class="empty-chat text-center">
        <Icon icon="mdi:chat-outline" class="h-12 w-12 text-gray-400 mb-4 mx-auto" />
        <p class="empty-chat-text">{{ $t('Start a conversation with') }} {{ botName }}</p>
        <p class="empty-chat-hint">{{ $t('Type your message below to begin chatting') }}</p>
      </div>

      <div v-else>
        <div
          v-for="(message, index) in messages"
          :key="index"
          :class="[
            'flex',
            message.type === 'user' ? 'justify-end' : 'justify-start'
          ]"
        >
          <div
            :class="[
              'max-w-xs lg:max-w-md px-4 py-2 rounded-lg',
              message.type === 'user'
                ? 'bg-blue-600 text-white'
                : 'bg-gray-200 dark:bg-gray-600 text-gray-900 dark:text-white'
            ]"
          >
            <p class="text-sm">{{ message.text }}</p>
            <p class="text-xs mt-1 opacity-70">
              {{ getRelativeTime(message.timestamp) }}
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
              <span class="text-sm ml-2">{{ botName }} is typing...</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { Icon } from '@iconify/vue'
import { getRelativeTime } from '@/utils/dateUtils'

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  },
  isTyping: {
    type: Boolean,
    default: false
  },
  botName: {
    type: String,
    required: true
  }
})

const emit = defineEmits(['scroll-to-bottom'])

const messagesContainer = ref(null)

// Auto scroll to bottom
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// Watch for messages changes
watch(() => props.messages, () => {
  scrollToBottom()
}, { deep: true })

// Watch for typing changes
watch(() => props.isTyping, () => {
  scrollToBottom()
})

// Initial scroll
onMounted(() => {
  scrollToBottom()
})

// Expose method to parent
defineExpose({
  scrollToBottom
})
</script>

<style scoped>
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
</style>
