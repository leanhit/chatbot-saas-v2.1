<template>
  <div class="bot-agent-inbox">
    <!-- Tab Navigation -->
    <div class="flex border-b border-gray-200 mb-4">
      <button
        @click="activeTab = 'bot'"
        :class="[
          'px-4 py-2 font-medium text-sm transition-colors',
          activeTab === 'bot'
            ? 'text-blue-600 border-b-2 border-blue-600'
            : 'text-gray-500 hover:text-gray-700'
        ]"
      >
        <Icon icon="mdi:robot" class="inline-block mr-1" />
        Bot Inbox ({{ botConversations.length }})
      </button>
      <button
        @click="activeTab = 'agent'"
        :class="[
          'px-4 py-2 font-medium text-sm transition-colors',
          activeTab === 'agent'
            ? 'text-blue-600 border-b-2 border-blue-600'
            : 'text-gray-500 hover:text-gray-700'
        ]"
      >
        <Icon icon="mdi:account-tie" class="inline-block mr-1" />
        Agent Inbox ({{ agentConversations.length }})
      </button>
    </div>

    <!-- Bot Inbox -->
    <div v-if="activeTab === 'bot'" class="bot-inbox">
      <div v-if="loading" class="p-8 text-center">
        <Icon icon="mdi:loading" class="animate-spin text-2xl text-gray-400" />
        <p class="mt-2 text-sm text-gray-500">Loading bot conversations...</p>
      </div>
      <div v-else-if="botConversations.length === 0" class="p-8 text-center">
        <Icon icon="mdi:robot-off" class="text-4xl text-gray-300 mx-auto" />
        <p class="mt-2 text-sm text-gray-500">No bot conversations found.</p>
      </div>
      <div v-else class="space-y-2">
        <ConversationItem
          v-for="conversation in botConversations"
          :key="conversation.id"
          :conversation="conversation"
          @select="selectConversation"
        />
      </div>
    </div>

    <!-- Agent Inbox -->
    <div v-if="activeTab === 'agent'" class="agent-inbox">
      <div v-if="loading" class="p-8 text-center">
        <Icon icon="mdi:loading" class="animate-spin text-2xl text-gray-400" />
        <p class="mt-2 text-sm text-gray-500">Loading agent conversations...</p>
      </div>
      <div v-else-if="agentConversations.length === 0" class="p-8 text-center">
        <Icon icon="mdi:account-off" class="text-4xl text-gray-300 mx-auto" />
        <p class="mt-2 text-sm text-gray-500">No agent conversations found.</p>
      </div>
      <div v-else class="space-y-2">
        <ConversationItem
          v-for="conversation in agentConversations"
          :key="conversation.id"
          :conversation="conversation"
          @select="selectConversation"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import ConversationItem from './components/ConversationItem.vue'
import conversationApi from '@/api/takeoverApi'

const activeTab = ref('bot')
const loading = ref(false)
const allConversations = ref([])

const botConversations = computed(() => {
  return allConversations.value.filter(c => !c.isTakenOverByAgent)
})

const agentConversations = computed(() => {
  return allConversations.value.filter(c => c.isTakenOverByAgent)
})

const emit = defineEmits(['select-conversation'])

const selectConversation = (conversation) => {
  emit('select-conversation', conversation)
}

const loadConversations = async () => {
  loading.value = true
  try {
    const response = await conversationApi.getConversations()
    allConversations.value = response.data || []
  } catch (error) {
    console.error('Error loading conversations:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadConversations()
})
</script>

<style scoped>
.bot-agent-inbox {
  height: 100%;
  overflow-y: auto;
}
</style>
