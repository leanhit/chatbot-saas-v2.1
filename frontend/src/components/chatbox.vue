<template>
  <div class="chat-box">
    <div class="chat-header">
      <h3>{{ conversation.name }}</h3>
    </div>
    <div class="messages" ref="messagesContainer">
      <div 
        v-for="(message, index) in messages" 
        :key="index"
        class="message"
        :class="{ 'sent': message.sent, 'received': !message.sent }"
      >
        <div class="message-content">{{ message.text }}</div>
        <div class="message-time">{{ formatTime(message.timestamp) }}</div>
      </div>
    </div>
    <div class="message-input">
      <input
        v-model="newMessage"
        type="text"
        placeholder="Type a message..."
        @keyup.enter="sendMessage"
      />
      <button @click="sendMessage">
        <span>Send</span>
      </button>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted, watch, nextTick } from 'vue';

export interface Message {
  id: string;
  text: string;
  sent: boolean;
  timestamp: Date;
}

export interface Conversation {
  id: string;
  name: string;
  lastMessage: string;
  unread: number;
}

export default defineComponent({
  name: 'ChatBox',
  props: {
    conversation: {
      type: Object as () => Conversation,
      required: true
    }
  },
  setup(props, { emit }) {
    const messages = ref<Message[]>([]);
    const newMessage = ref('');
    const messagesContainer = ref<HTMLElement | null>(null);

    // Mock messages - in a real app, these would come from an API
    const loadMessages = () => {
      // This would be an API call in a real app
      messages.value = [
        { id: '1', text: 'Hello! How can I help you today?', sent: false, timestamp: new Date(Date.now() - 3600000) },
        { id: '2', text: 'I need help with my account', sent: true, timestamp: new Date(Date.now() - 1800000) },
        { id: '3', text: 'I can help with that. What seems to be the problem?', sent: false, timestamp: new Date() },
      ];
    };

    const sendMessage = () => {
      if (newMessage.value.trim() === '') return;
      
      const message: Message = {
        id: Date.now().toString(),
        text: newMessage.value,
        sent: true,
        timestamp: new Date()
      };
      
      messages.value.push(message);
      emit('send-message', newMessage.value);
      newMessage.value = '';
      
      // Auto-scroll to bottom
      nextTick(() => {
        scrollToBottom();
      });
    };

    const formatTime = (date: Date) => {
      return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    };

    const scrollToBottom = () => {
      if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
      }
    };

    onMounted(() => {
      loadMessages();
      nextTick(() => {
        scrollToBottom();
      });
    });

    // Watch for conversation changes
    watch(() => props.conversation, () => {
      loadMessages();
    }, { immediate: true });

    return {
      messages,
      newMessage,
      messagesContainer,
      sendMessage,
      formatTime
    };
  }
});
</script>

<style scoped>
.chat-box {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: #f9f9f9;
}

.chat-header {
  padding: 16px;
  background-color: #fff;
  border-bottom: 1px solid #e0e0e0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  z-index: 1;
}

.chat-header h3 {
  margin: 0;
  font-size: 1.2em;
  color: #333;
}

.messages {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 18px;
  position: relative;
  word-wrap: break-word;
  line-height: 1.4;
}

.message.sent {
  align-self: flex-start;
  background-color: #e0e0e0;
  color: #333;
  border-bottom-left-radius: 4px;
}

.message.received {
  align-self: flex-end;
  background-color: #1976d2;
  color: white;
  border-bottom-right-radius: 4px;
}

.message-time {
  font-size: 0.7em;
  opacity: 0.8;
  margin-top: 4px;
  text-align: right;
}

.message.sent .message-time {
  color: rgba(255, 255, 255, 0.8);
}

.message.received .message-time {
  color: #666;
}

.message-input {
  display: flex;
  padding: 12px 16px;
  background-color: #fff;
  border-top: 1px solid #e0e0e0;
}

.message-input input {
  flex: 1;
  padding: 10px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 24px;
  outline: none;
  font-size: 0.95em;
  transition: border-color 0.2s;
}

.message-input input:focus {
  border-color: #1976d2;
}

.message-input button {
  margin-left: 12px;
  padding: 0 20px;
  background-color: #1976d2;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.2s;
}

.message-input button:hover {
  background-color: #1565c0;
}
</style>
