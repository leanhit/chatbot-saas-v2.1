<template>
  <div class="bg-white dark:bg-gray-800 px-4 pt-5 pb-4 sm:p-6 sm:pb-4 flex flex-col h-[500px] max-h-[70vh]">
    <!-- Messages Area -->
    <ChatMessages
      ref="chatMessagesRef"
      :messages="messages"
      :is-typing="isTyping"
      :bot-name="botName"
      @scroll-to-bottom="handleScrollToBottom"
      class="flex-1 min-h-0"
    />
    
    <!-- Message Input - Fixed at bottom -->
    <div class="flex-shrink-0 mt-4 pt-4 border-t border-gray-200 dark:border-gray-600">
      <ChatInput
        ref="chatInputRef"
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
const chatInputRef = ref(null)

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
    if (chatInputRef.value) {
      chatInputRef.value.focus()
    }
  }
})
</script>
