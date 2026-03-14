<template>
  <div
    @click="$emit('select')"
    :class="[
      'p-4 border-b dark:border-gray-700 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors',
      isSelected ? 'bg-blue-50 dark:bg-blue-900/20 border-l-4 border-l-blue-500' : ''
    ]"
  >
    <div class="flex items-start justify-between">
      <div class="flex items-start gap-3 flex-1 min-w-0">
        <!-- Selection Checkbox -->
        <div class="flex-shrink-0 mt-1">
          <input
            type="checkbox"
            :checked="isSelectedForDeletion"
            @change="$emit('toggle-select')"
            @click.stop
            class="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
          />
        </div>
        
        <!-- User Avatar -->
        <div class="flex-shrink-0">
          <img 
            v-if="conversation.userAvatar"
            :src="conversation.userAvatar" 
            :alt="conversation.userName || conversation.externalUserId"
            class="w-10 h-10 rounded-full object-cover"
            @error="handleImageError"
          />
          <div 
            v-else
            class="w-10 h-10 rounded-full bg-gray-300 dark:bg-gray-600 flex items-center justify-center"
          >
            <Icon icon="mdi:account" class="text-gray-600 dark:text-gray-300 text-xl" />
          </div>
        </div>
        
        <!-- Message Content -->
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-2 mb-1">
            <h3 class="font-medium text-gray-900 dark:text-gray-200 truncate">
              {{ conversation.userName || conversation.externalUserId || 'Unknown User' }}
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
              {{ getRelativeTime(conversation.lastMessageAt) }}
            </span>
            <span class="text-xs text-gray-400">
              {{ conversation.channel }}
            </span>
          </div>
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
import { getRelativeTime } from '@/utils/dateUtils'

const props = defineProps({
  conversation: {
    type: Object,
    required: true
  },
  isSelected: {
    type: Boolean,
    default: false
  },
  isSelectedForDeletion: {
    type: Boolean,
    default: false
  }
})

defineEmits(['select', 'toggle-select'])

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

const handleImageError = (event) => {
  // Fallback to default avatar if image fails to load
  event.target.style.display = 'none'
  const parent = event.target.parentElement
  if (parent) {
    const fallback = parent.querySelector('.bg-gray-300, .dark\\:bg-gray-600')
    if (fallback) {
      fallback.style.display = 'flex'
    }
  }
}
</script>
