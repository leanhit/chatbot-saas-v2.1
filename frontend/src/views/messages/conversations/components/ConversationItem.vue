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
          <div class="flex items-center gap-2 mb-1 flex-wrap">
            <h3 class="font-medium text-gray-900 dark:text-gray-200 truncate">
              {{ conversation.userName || conversation.externalUserId || $t('messages.unknownUser') }}
            </h3>
            
            <!-- Agent Assigned Badge -->
            <span v-if="conversation.agentAssignedId" 
              class="bg-blue-100 text-blue-800 text-xs px-2 py-1 rounded-full flex items-center gap-1">
              <Icon icon="mdi:account-tie" class="inline" />
              {{ assignedAgentName || 'Agent' }}
            </span>
            
            <!-- Taken Over Badge -->
            <span v-if="conversation.isTakenOver" 
              class="bg-green-100 text-green-800 text-xs px-2 py-1 rounded-full">
              <Icon icon="mdi:hand-right" class="inline mr-1" />
              {{ $t('messages.takenOver') }}
            </span>
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400 truncate">
            {{ conversation.lastMessage || $t('messages.noMessagesYet') }}
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
        <!-- Assign/Release Button -->
        <button
          @click.stop="handleAssignAction"
          :class="[
            'text-xs px-2 py-1 rounded transition-colors',
            conversation.agentAssignedId 
              ? 'bg-red-100 text-red-700 hover:bg-red-200' 
              : 'bg-green-100 text-green-700 hover:bg-green-200'
          ]"
        >
          <Icon :icon="conversation.agentAssignedId ? 'mdi:lock-open' : 'mdi:hand-right'" class="inline mr-1" />
          {{ conversation.agentAssignedId ? 'Release' : 'Take' }}
        </button>
        
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
import { ref, computed } from 'vue'
import { Icon } from '@iconify/vue'
import { getRelativeTime } from '@/utils/dateUtils'
import { appApi } from '@/api/takeoverApi'
import { useAuthStore } from '@/stores/authStore'

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
  },
  agents: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['select', 'toggle-select', 'assign-changed'])

const authStore = useAuthStore()
const isAssigning = ref(false)

const assignedAgentName = computed(() => {
  if (!props.conversation.agentAssignedId) return null
  const agent = props.agents.find(a => a.id === props.conversation.agentAssignedId)
  return agent ? agent.fullName || agent.email : null
})

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
  event.target.style.display = 'none'
  const parent = event.target.parentElement
  if (parent) {
    const fallback = parent.querySelector('.bg-gray-300, .dark\\:bg-gray-600')
    if (fallback) {
      fallback.style.display = 'flex'
    }
  }
}

const handleAssignAction = async () => {
  if (isAssigning.value) return
  
  isAssigning.value = true
  
  try {
    const currentUserId = authStore.user?.id
    const isAssignedToMe = props.conversation.agentAssignedId === currentUserId
    
    // If assigned to me or unassigned, assign to me (self-assign)
    // If assigned to someone else, release (only if I'm the assigned agent or admin)
    let agentId
    
    if (!props.conversation.agentAssignedId || isAssignedToMe) {
      // Self-assign
      agentId = currentUserId
    } else {
      // Release
      agentId = null
    }
    
    await appApi.assignConversation(props.conversation.id, agentId)
    
    // Emit event to refresh conversation list
    emit('assign-changed', {
      conversationId: props.conversation.id,
      agentId: agentId
    })
    
  } catch (error) {
    console.error('Failed to assign/release conversation:', error)
    // Show error notification
    if (error.response?.status === 403) {
      alert('Bạn không có quyền thực hiện hành động này')
    } else {
      alert('Có lỗi xảy ra, vui lòng thử lại')
    }
  } finally {
    isAssigning.value = false
  }
}
</script>
