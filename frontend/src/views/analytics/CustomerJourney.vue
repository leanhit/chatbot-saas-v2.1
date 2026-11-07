<template>
  <div class="p-6">
    <h1 class="text-2xl font-bold text-gray-900 dark:text-white mb-6">Customer Journey</h1>

    <div class="max-w-xl mb-6">
      <input v-model.number="customerId" type="number" placeholder="Customer profile ID"
        class="w-full border rounded px-3 py-2 dark:bg-gray-700 dark:border-gray-600" />
      <button @click="load" :disabled="!customerId"
        class="mt-2 px-4 py-2 bg-blue-600 text-white rounded-lg disabled:opacity-50">
        Load Journey
      </button>
    </div>

    <div v-if="loading" class="text-center py-12">
      <Icon icon="mdi:loading" class="animate-spin text-4xl text-blue-600" />
    </div>

    <div v-else-if="events.length" class="relative">
      <div v-for="(event, i) in events" :key="i" class="flex gap-4 mb-4">
        <div class="flex flex-col items-center">
          <div class="w-3 h-3 rounded-full mt-1.5" :class="dotClass(event.type)"></div>
          <div v-if="i < events.length - 1" class="w-px flex-1 bg-gray-300 dark:bg-gray-600"></div>
        </div>
        <div class="pb-4">
          <p class="font-medium text-gray-900 dark:text-white">{{ label(event.type) }}</p>
          <p class="text-sm text-gray-500">{{ event.description }}</p>
          <p class="text-xs text-gray-400">{{ formatDate(event.timestamp) }}</p>
        </div>
      </div>
    </div>

    <div v-else-if="searched" class="text-center py-12 text-gray-500">
      No journey events found for this customer.
    </div>
  </div>
</template>

<script>
import { Icon } from '@iconify/vue'
import analyticsApi from '@/api/analyticsApi'

const LABELS = {
  PROFILE_CREATED: 'Profile Created',
  CONVERSATION_STARTED: 'Conversation Started',
  AGENT_ASSIGNED: 'Agent Assigned',
  ESCALATED: 'Escalated',
  CONVERSATION_CLOSED: 'Conversation Closed',
  FEEDBACK_RECEIVED: 'Feedback Received',
  NEGATIVE_SENTIMENT: 'Negative Sentiment',
}

export default {
  name: 'CustomerJourney',
  components: { Icon },
  data() {
    return {
      customerId: null,
      loading: false,
      searched: false,
      events: [],
    }
  },
  methods: {
    async load() {
      this.loading = true
      this.searched = true
      try {
        this.events = await analyticsApi.getCustomerJourney(this.customerId)
      } catch (e) {
        this.events = []
      } finally {
        this.loading = false
      }
    },
    label(type) {
      return LABELS[type] || type
    },
    dotClass(type) {
      return {
        'bg-blue-500': type === 'PROFILE_CREATED' || type === 'CONVERSATION_STARTED',
        'bg-green-500': type === 'AGENT_ASSIGNED' || type === 'CONVERSATION_CLOSED',
        'bg-yellow-500': type === 'ESCALATED' || type === 'FEEDBACK_RECEIVED',
        'bg-red-500': type === 'NEGATIVE_SENTIMENT',
      }
    },
    formatDate(d) {
      return d ? new Date(d).toLocaleString() : ''
    },
  },
}
</script>
