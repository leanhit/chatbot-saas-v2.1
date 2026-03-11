<template>
  <div
    :class="[
      'flex',
      (message.sender === 'agent' || message.sender === 'bot') ? 'justify-end' : 'justify-start'
    ]"
  >
    <div
      :class="[
        'max-w-md px-4 py-2 rounded-lg',
        (message.sender === 'agent' || message.sender === 'bot')
          ? 'bg-blue-600 text-white'
          : 'bg-gray-200 dark:bg-gray-700 text-gray-900 dark:text-white'
      ]"
    >
      <p class="text-sm">{{ message.content }}</p>
      <div class="flex items-center gap-2 mt-1">
        <p class="text-xs opacity-70">
          {{ getRelativeTime(message.createdAt) }}
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
import { getRelativeTime } from '@/utils/dateUtils'

const props = defineProps({
  message: {
    type: Object,
    required: true
  }
})
</script>
