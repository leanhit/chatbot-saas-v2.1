<template>
  <div class="conversation-list">
    <!-- Conversation list content -->
    <div class="space-y-2">
      <ConversationItem 
        v-for="conversation in conversations" 
        :key="conversation.id"
        :conversation="conversation"
        @select="selectConversation"
      />
    </div>
  </div>
</template>

<script setup>
import ConversationItem from './components/ConversationItem.vue'

defineProps({
  conversations: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['select-conversation'])

const selectConversation = (conversation) => {
  emit('select-conversation', conversation)
}
</script>
