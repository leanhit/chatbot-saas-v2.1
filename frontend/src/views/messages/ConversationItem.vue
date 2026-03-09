<template>
  <div
    @click="$emit('select')"
    :class="[
      'p-4 border-b dark:border-gray-700 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors',
      isSelected ? 'bg-blue-50 dark:bg-blue-900/20 border-l-4 border-l-blue-500' : ''
    ]"
  >
    <div class="flex items-start justify-between">
      <div class="flex-1 min-w-0">
        <div class="flex items-center gap-2 mb-1">
          <h3 class="font-medium text-gray-900 dark:text-gray-200 truncate">
            {{ conversation.externalUserId || 'Unknown User' }}
          </h3>
          <span v-if="conversation.isTakenOver" 
            class="bg-green-100 text-green-800 text-xs px-2 py-1 rounded-full">
            <Icon icon="mdi:hand-right" class="inline mr-1" />
            Taken Over
          </span>
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400 truncate">
          {{ conversation.lastMessage || 'No messages yet' }}
        </p>
        <div class="flex items-center gap-2 mt-1">
          <span class="text-xs text-gray-500">
            {{ formatTime(conversation.lastMessageAt) }}
          </span>
          <span class="text-xs text-gray-400">
            {{ conversation.channel }}
          </span>
        </div>
      </div>
      <div class="flex flex-col items-end gap-1">
        <div v-if="conversation.unreadCount > 0" 
          class="bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
          {{ conversation.unreadCount }}
        </div>
        <Icon :icon="getChannelIcon(conversation.channel)" 
          class="text-gray-400" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { Icon } from '@iconify/vue'

const props = defineProps({
  conversation: {
    type: Object,
    required: true
  },
  isSelected: {
    type: Boolean,
    default: false
  }
})

defineEmits(['select'])

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
  
  return date.toLocaleDateString()
}

const getChannelIcon = (channel) => {
  switch (channel) {
    case 'FACEBOOK':
      return 'mdi:facebook'
    case 'WEBSITE':
      return 'mdi:web'
    case 'ZALO':
      return 'mdi:message-text'
    default:
      return 'mdi:chat'
  }
}
</script>
