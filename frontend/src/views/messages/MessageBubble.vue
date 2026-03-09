<template>
  <div
    :class="[
      'flex',
      message.sender === 'user' ? 'justify-end' : 'justify-start'
    ]"
  >
    <div
      :class="[
        'max-w-md px-4 py-2 rounded-lg',
        message.sender === 'user'
          ? 'bg-blue-600 text-white'
          : 'bg-gray-200 dark:bg-gray-700 text-gray-900 dark:text-white'
      ]"
    >
      <p class="text-sm">{{ message.content }}</p>
      <div class="flex items-center gap-2 mt-1">
        <p class="text-xs opacity-70">
          {{ formatTime(message.createdAt) }}
        </p>
        <span v-if="message.sender === 'bot'" class="text-xs opacity-60">
          Bot
        </span>
        <span v-else-if="message.sender === 'agent'" class="text-xs opacity-60">
          Agent
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Icon } from '@iconify/vue'

const props = defineProps({
  message: {
    type: Object,
    required: true
  }
})

const formatTime = (dateString) => {
  if (!dateString) return 'Unknown'
  const date = new Date(dateString)
  const now = new Date()
  const diffMs = now - date
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)
  
  if (diffMins < 1) return 'Just now'
  if (diffMins < 60) return `${diffMins}m ago`
  if (diffHours < 24) return `${diffHours}h ago`
  if (diffDays < 7) return `${diffDays}d ago`
  
  return date.toLocaleTimeString()
}
</script>
