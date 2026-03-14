<template>
  <div class="flex items-center space-x-2">
    <input
      v-model="inputMessage"
      @keydown.enter="sendMessage"
      type="text"
      :placeholder="placeholder"
      :disabled="disabled"
      class="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
    />
    <button
      @click="sendMessage"
      :disabled="!inputMessage.trim() || disabled"
      class="inline-flex items-center px-4 py-2 border border-transparent rounded-lg shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
    >
      <Icon v-if="loading" icon="mdi:loading" class="animate-spin h-4 w-4 mr-2" />
      <Icon v-else icon="mdi:send" class="h-4 w-4" />
    </button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Icon } from '@iconify/vue'

const props = defineProps({
  disabled: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  },
  placeholder: {
    type: String,
    default: 'Type your message...'
  }
})

const emit = defineEmits(['send-message'])

const inputMessage = ref('')

const sendMessage = () => {
  if (!inputMessage.value.trim() || props.disabled) return
  
  const message = inputMessage.value.trim()
  inputMessage.value = ''
  emit('send-message', message)
}

// Expose method to clear input
defineExpose({
  clearInput: () => {
    inputMessage.value = ''
  },
  focus: () => {
    // Focus logic if needed
  }
})
</script>
