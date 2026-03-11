<template>
  <div class="message-bubble-wrapper">
    <div
      :class="[
        'message-bubble',
        {
          'sent': message.sender === 'agent',
          'received': message.sender !== 'agent',
          'realtime': message.isRealtime,
          'unread': !message.read
        }
      ]"
    >
      <!-- Message Content -->
      <div class="message-content">
        <div class="message-text">{{ message.content }}</div>
        
        <!-- Message Timestamp -->
        <div class="message-timestamp">
          {{ formatTime(message.timestamp) }}
          <span v-if="message.isRealtime" class="realtime-badge">Live</span>
        </div>
      </div>

      <!-- Delivery Status -->
      <div class="delivery-status">
        <Icon
          :icon="deliveryStatusIcon"
          :class="deliveryStatusClass"
          class="w-4 h-4"
        />
        <span class="text-xs">{{ deliveryStatusText }}</span>
      </div>

      <!-- Read Receipt -->
      <div v-if="showReadReceipt" class="read-receipt">
        <Icon icon="mdi:check-all" class="w-4 h-4 text-blue-500" />
        <span class="text-xs text-gray-500">Read</span>
      </div>
    </div>

    <!-- Typing Indicator (for this message) -->
    <div v-if="isTyping" class="typing-indicator">
      <div class="typing-dots">
        <span></span>
        <span></span>
        <span></span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Icon } from '@iconify/vue'
import { getRelativeTime } from '@/utils/dateUtils'
import takeoverWebSocketService from '@/services/takeoverWebSocketService'

// Props
const props = defineProps({
  message: {
    type: Object,
    required: true
  }
})

// Computed properties
const deliveryStatus = computed(() => {
  if (!props.message.id) return null
  return takeoverWebSocketService.getMessageDeliveryStatus(props.message.id)
})

const deliveryStatusIcon = computed(() => {
  if (!deliveryStatus.value) return 'mdi:clock'
  
  switch (deliveryStatus.value.status) {
    case 'sent': return 'mdi:check'
    case 'delivered': return 'mdi:check-all'
    case 'read': return 'mdi:check-all'
    default: return 'mdi:clock'
  }
})

const deliveryStatusClass = computed(() => {
  if (!deliveryStatus.value) return 'text-gray-400'
  
  switch (deliveryStatus.value.status) {
    case 'sent': return 'text-gray-400'
    case 'delivered': return 'text-blue-500'
    case 'read': return 'text-blue-600'
    default: return 'text-gray-400'
  }
})

const deliveryStatusText = computed(() => {
  if (!deliveryStatus.value) return 'Sending...'
  
  switch (deliveryStatus.value.status) {
    case 'sent': return 'Sent'
    case 'delivered': return 'Delivered'
    case 'read': return 'Read'
    default: return 'Sending...'
  }
})

const showReadReceipt = computed(() => {
  return deliveryStatus.value && deliveryStatus.value.status === 'read'
})

const isTyping = computed(() => {
  // This would be set by parent component when user is typing
  return false
})

// Methods
const formatTime = (timestamp) => {
  return getRelativeTime(new Date(timestamp))
}
</script>

<style scoped>
.message-bubble-wrapper {
  @apply mb-4;
}

.message-bubble {
  @apply flex flex-col max-w-xs lg:max-w-md;
}

.message-bubble.sent {
  @apply ml-auto items-end;
}

.message-bubble.received {
  @apply mr-auto items-start;
}

.message-bubble.realtime {
  @apply relative;
}

.message-bubble.realtime::before {
  content: '';
  @apply absolute -top-1 -right-1 w-2 h-2 bg-green-500 rounded-full animate-pulse;
}

.message-bubble.unread {
  @apply relative;
}

.message-bubble.unread::after {
  content: '';
  @apply absolute -top-1 -left-1 w-2 h-2 bg-blue-500 rounded-full;
}

.message-content {
  @apply rounded-lg px-4 py-2;
}

.message-bubble.sent .message-content {
  @apply bg-blue-600 text-white;
}

.message-bubble.received .message-content {
  @apply bg-gray-200 dark:bg-gray-700 text-gray-900 dark:text-white;
}

.message-text {
  @apply text-sm break-words;
}

.message-timestamp {
  @apply text-xs mt-1 opacity-70 flex items-center space-x-1;
}

.realtime-badge {
  @apply bg-green-500 text-white text-xs px-1 py-0.5 rounded-full;
}

.delivery-status {
  @apply flex items-center space-x-1 mt-1;
}

.message-bubble.sent .delivery-status {
  @apply justify-end;
}

.message-bubble.received .delivery-status {
  @apply justify-start;
}

.read-receipt {
  @apply flex items-center space-x-1 mt-1;
}

.message-bubble.sent .read-receipt {
  @apply justify-end;
}

.message-bubble.received .read-receipt {
  @apply justify-start;
}

.typing-indicator {
  @apply flex items-center space-x-2 mt-2;
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
</style>
