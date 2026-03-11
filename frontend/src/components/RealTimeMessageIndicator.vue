<template>
  <div class="real-time-message-indicator">
    <!-- Connection Status Indicator -->
    <div class="connection-status" :class="connectionStatusClass">
      <Icon :icon="connectionStatusIcon" class="w-4 h-4" />
      <span class="text-xs">{{ connectionStatusText }}</span>
    </div>

    <!-- Active Agents Indicator -->
    <div v-if="activeAgents.length > 0" class="active-agents">
      <div class="flex items-center space-x-1">
        <Icon icon="mdi:account-multiple" class="w-4 h-4 text-green-500" />
        <span class="text-xs text-gray-600 dark:text-gray-400">
          {{ activeAgents.length }} agent{{ activeAgents.length > 1 ? 's' : '' }} online
        </span>
      </div>
      <div class="flex -space-x-2">
        <div
          v-for="agent in activeAgents.slice(0, 3)"
          :key="agent.id"
          class="w-6 h-6 rounded-full border-2 border-white dark:border-gray-800 flex items-center justify-center"
          :title="agent.name"
        >
          <img
            v-if="agent.avatar"
            :src="agent.avatar"
            :alt="agent.name"
            class="w-full h-full rounded-full object-cover"
            @error="handleImageError"
          />
          <Icon
            v-else
            icon="mdi:account"
            class="w-4 h-4 text-gray-500"
          />
        </div>
        <div
          v-if="activeAgents.length > 3"
          class="w-6 h-6 rounded-full border-2 border-white dark:border-gray-800 bg-gray-200 dark:bg-gray-700 flex items-center justify-center"
        >
          <span class="text-xs text-gray-600 dark:text-gray-400">
            +{{ activeAgents.length - 3 }}
          </span>
        </div>
      </div>
    </div>

    <!-- Typing Indicator -->
    <div v-if="typingAgents.length > 0" class="typing-indicator">
      <div class="flex items-center space-x-2">
        <div class="typing-dots">
          <span></span>
          <span></span>
          <span></span>
        </div>
        <span class="text-xs text-gray-600 dark:text-gray-400">
          {{ typingText }}
        </span>
      </div>
    </div>

    <!-- Message Queue Status -->
    <div v-if="messageQueue.length > 0" class="message-queue">
      <div class="flex items-center space-x-1">
        <Icon icon="mdi:email-alert" class="w-4 h-4 text-blue-500 animate-pulse" />
        <span class="text-xs text-gray-600 dark:text-gray-400">
          {{ messageQueue.length }} new message{{ messageQueue.length > 1 ? 's' : '' }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Icon } from '@iconify/vue'
import takeoverWebSocketService from '@/services/takeoverWebSocketService'

// Props
const props = defineProps({
  conversationId: {
    type: String,
    required: true
  }
})

// Connection status
const connectionStatus = computed(() => takeoverWebSocketService.getConnectionStatus())

const connectionStatusClass = computed(() => {
  return {
    'connected': connectionStatus.value === 'connected',
    'connecting': connectionStatus.value === 'connecting',
    'disconnected': connectionStatus.value === 'disconnected',
    'error': connectionStatus.value === 'error'
  }
})

const connectionStatusIcon = computed(() => {
  switch (connectionStatus.value) {
    case 'connected': return 'mdi:wifi'
    case 'connecting': return 'mdi:loading'
    case 'disconnected': return 'mdi:wifi-off'
    case 'error': return 'mdi:wifi-alert'
    default: return 'mdi:wifi-off'
  }
})

const connectionStatusText = computed(() => {
  switch (connectionStatus.value) {
    case 'connected': return 'Connected'
    case 'connecting': return 'Connecting...'
    case 'disconnected': return 'Disconnected'
    case 'error': return 'Connection Error'
    default: return 'Unknown'
  }
})

// Active agents
const activeAgents = computed(() => {
  return takeoverWebSocketService.getActiveAgents(props.conversationId)
})

// Typing agents
const typingAgents = computed(() => {
  return takeoverWebSocketService.getTypingAgents(props.conversationId)
})

const typingText = computed(() => {
  if (typingAgents.value.length === 0) return ''
  if (typingAgents.value.length === 1) {
    return `${typingAgents.value[0].name} is typing...`
  }
  if (typingAgents.value.length === 2) {
    return `${typingAgents.value[0].name} and ${typingAgents.value[1].name} are typing...`
  }
  return `${typingAgents.value.length} agents are typing...`
})

// Message queue
const messageQueue = computed(() => {
  return takeoverWebSocketService.messageQueue.value.filter(
    msg => msg.conversationId === props.conversationId && msg.isRealtime
  )
})

// Methods
const handleImageError = (event) => {
  event.target.style.display = 'none'
  const parent = event.target.parentElement
  if (parent) {
    const icon = document.createElement('div')
    icon.innerHTML = '<i data-icon="mdi:account" class="w-4 h-4 text-gray-500"></i>'
    parent.appendChild(icon)
  }
}
</script>

<style scoped>
.real-time-message-indicator {
  @apply flex items-center justify-between p-2 bg-gray-50 dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700;
}

.connection-status {
  @apply flex items-center space-x-1;
}

.connection-status.connected {
  @apply text-green-600 dark:text-green-400;
}

.connection-status.connecting {
  @apply text-yellow-600 dark:text-yellow-400;
}

.connection-status.disconnected {
  @apply text-gray-600 dark:text-gray-400;
}

.connection-status.error {
  @apply text-red-600 dark:text-red-400;
}

.active-agents {
  @apply flex items-center space-x-2;
}

.typing-indicator {
  @apply flex items-center;
}

.typing-dots {
  @apply flex space-x-1;
}

.typing-dots span {
  @apply w-2 h-2 bg-gray-400 rounded-full;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-dots span:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-dots span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.message-queue {
  @apply flex items-center;
}

.animate-pulse {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}
</style>
