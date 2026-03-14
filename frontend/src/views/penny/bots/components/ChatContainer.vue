<template>
  <div class="bg-white dark:bg-gray-800 px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
    <div class="flex flex-col h-96">
      <!-- Messages Area -->
      <ChatMessages
        ref="chatMessagesRef"
        :messages="messages"
        :is-typing="isTyping"
        :bot-name="botName"
        @scroll-to-bottom="handleScrollToBottom"
      />
      
      <!-- Message Input -->
      <ChatInput
        :disabled="disabled"
        :loading="loading"
        @send-message="handleSendMessage"
      />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import ChatMessages from './ChatMessages.vue'
import ChatInput from './ChatInput.vue'

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
  },
  disabled: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['send-message', 'scroll-to-bottom'])

const chatMessagesRef = ref(null)

const handleSendMessage = (message) => {
  emit('send-message', message)
}

const handleScrollToBottom = () => {
  emit('scroll-to-bottom')
}

// Expose methods
defineExpose({
  scrollToBottom: () => {
    if (chatMessagesRef.value) {
      chatMessagesRef.value.scrollToBottom()
    }
  },
  focusInput: () => {
    // Focus input logic if needed
  }
})
</script>
