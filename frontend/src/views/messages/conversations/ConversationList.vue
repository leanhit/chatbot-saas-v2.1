  <div class="conversation-list">
    <!-- Loading State -->
    <div v-if="loading" class="p-8 text-center">
      <Icon icon="mdi:loading" class="animate-spin text-2xl text-gray-400" />
      <p class="mt-2 text-sm text-gray-500">{{ $t('messages.loadingConversations') || 'Loading conversations...' }}</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="conversations.length === 0" class="p-8 text-center">
      <Icon icon="mdi:chat-off" class="text-4xl text-gray-300 mx-auto" />
      <p class="mt-2 text-sm text-gray-500">{{ $t('messages.noConversationsFound') || 'No conversations found.' }}</p>
    </div>

    <!-- Conversation list content -->
    <div v-else class="space-y-2">
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
import { Icon } from '@iconify/vue'
import ConversationItem from './components/ConversationItem.vue'

defineProps({
  conversations: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['select-conversation'])

const selectConversation = (conversation) => {
  emit('select-conversation', conversation)
}
</script>
