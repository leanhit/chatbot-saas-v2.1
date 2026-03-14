<template>
  <div class="message-list">
    <!-- Message list content -->
    <div class="space-y-4">
      <RealTimeMessageBubble
        v-for="message in messages"
        :key="message.id || `${message.timestamp}-${message.content}`"
        :message="message"
      />
    </div>
  </div>
</template>

<script setup>
import RealTimeMessageBubble from './RealTimeMessageBubble.vue'

defineProps({
  messages: {
    type: Array,
    default: () => []
  }
})
</script>
