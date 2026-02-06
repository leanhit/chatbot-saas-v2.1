<template>
  <div class="chat-container">
    <div class="conversation-list">
      <ConversationList 
        :conversations="conversations"
        :selected-conversation-id="selectedConversationId"
        @select="handleSelectConversation"
      />
    </div>
    <div class="chat-box">
      <ChatBox 
        v-if="selectedConversation"
        :conversation="selectedConversation"
        @send-message="handleSendMessage"
      />
      <div v-else class="no-conversation">
        Select a conversation to start chatting
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue';
import ConversationList from './conversationList.vue';
import ChatBox from './chatbox.vue';

export default defineComponent({
  name: 'ChatList',
  components: {
    ConversationList,
    ChatBox
  },
  setup() {
    const conversations = ref([
      { id: '1', name: 'Conversation 1', lastMessage: 'Last message 1', unread: 2 },
      { id: '2', name: 'Conversation 2', lastMessage: 'Last message 2', unread: 0 },
    ]);

    const selectedConversationId = ref<string | null>(null);

    const selectedConversation = computed(() => {
      return conversations.value.find(conv => conv.id === selectedConversationId.value) || null;
    });

    const handleSelectConversation = (conversationId: string) => {
      selectedConversationId.value = conversationId;
    };

    const handleSendMessage = (message: string) => {
      // Handle sending message
      console.log('Sending message:', message);
    };

    return {
      conversations,
      selectedConversationId,
      selectedConversation,
      handleSelectConversation,
      handleSendMessage
    };
  }
});
</script>

<style scoped>
.chat-container {
  display: flex;
  height: 100vh;
}

.conversation-list {
  width: 300px;
  border-right: 1px solid #e0e0e0;
  overflow-y: auto;
}

.chat-box {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.no-conversation {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #9e9e9e;
  font-size: 1.2em;
}
</style>
