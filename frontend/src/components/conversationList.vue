<template>
  <div class="conversation-list">
    <div 
      v-for="conversation in conversations" 
      :key="conversation.id"
      class="conversation-item"
      :class="{ 'active': isActive(conversation.id) }"
      @click="selectConversation(conversation.id)"
    >
      <div class="avatar-container" v-if="conversation.userAvatar">
        <img :src="conversation.userAvatar" :alt="conversation.userName || 'User'" class="avatar" />
      </div>
      <div class="avatar-placeholder" v-else>
        {{ (conversation.userName || 'U').charAt(0).toUpperCase() }}
      </div>
      <div class="conversation-info">
        <div class="conversation-name">{{ conversation.userName || 'Người dùng' }}</div>
        <div class="conversation-preview">{{ conversation.lastMessage || 'Không có tin nhắn' }}</div>
      </div>
      <div v-if="conversation.unread > 0" class="unread-badge">
        {{ conversation.unread }}
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue';

export interface Conversation {
  id: string;
  name?: string;
  userName?: string;
  userAvatar?: string;
  lastMessage?: string;
  unread: number;
  updatedAt?: string;
}

export default defineComponent({
  name: 'ConversationList',
  props: {
    conversations: {
      type: Array as PropType<Conversation[]>,
      required: true
    },
    selectedConversationId: {
      type: String,
      default: null
    }
  },
  setup(props, { emit }) {
    const isActive = (conversationId: string) => {
      return props.selectedConversationId === conversationId;
    };

    const selectConversation = (conversationId: string) => {
      emit('select', conversationId);
    };

    return {
      isActive,
      selectConversation
    };
  }
});
</script>

<style scoped>
.conversation-list {
  padding: 10px 0;
}

.conversation-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.2s;
  gap: 12px;
}

.conversation-item:hover {
  background-color: #f5f5f5;
}

.conversation-item.active {
  background-color: #e3f2fd;
}

.conversation-info {
  flex: 1;
  min-width: 0;
}

.conversation-name {
  font-weight: 500;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conversation-preview {
  font-size: 0.9em;
  color: #757575;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.avatar-container {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
  color: #757575;
  flex-shrink: 0;
}

.unread-badge {
  background-color: #1976d2;
  color: white;
  border-radius: 10px;
  padding: 2px 8px;
  font-size: 0.8em;
  font-weight: 500;
  margin-left: auto;
}
</style>
