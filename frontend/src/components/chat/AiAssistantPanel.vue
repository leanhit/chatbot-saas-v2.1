<template>
  <div class="h-full flex flex-col bg-white dark:bg-gray-800 border-l dark:border-gray-700">
    <!-- Header -->
    <div class="p-3 border-b dark:border-gray-700 flex items-center justify-between">
      <h3 class="font-semibold text-gray-900 dark:text-white flex items-center gap-2">
        <Icon icon="mdi:robot-outline" class="text-blue-500" />
        AI Assistant
      </h3>
      <button @click="$emit('close')" class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-200">
        <Icon icon="mdi:close" />
      </button>
    </div>

    <div class="flex-1 overflow-y-auto p-3 space-y-4">
      <!-- Sentiment indicator -->
      <div>
        <div class="flex items-center justify-between mb-1">
          <span class="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase">Customer sentiment</span>
          <button
            @click="refreshSentiment"
            :disabled="scoringSentiment"
            class="text-xs text-blue-600 hover:underline disabled:opacity-50"
          >
            {{ scoringSentiment ? 'Scoring...' : sentiment ? 'Re-score' : 'Score now' }}
          </button>
        </div>
        <div
          v-if="sentiment"
          class="flex items-center gap-2 px-3 py-2 rounded-lg text-sm"
          :class="sentimentClass"
        >
          <Icon :icon="sentimentIcon" />
          <span class="font-medium">{{ sentiment.label }}</span>
          <span class="text-xs opacity-75">({{ formatScore(sentiment.score) }})</span>
        </div>
        <p v-else class="text-xs text-gray-400">No sentiment data yet.</p>
      </div>

      <!-- Suggestions -->
      <div>
        <div class="flex items-center justify-between mb-2">
          <span class="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase">Reply suggestions</span>
          <button
            @click="generate"
            :disabled="generating"
            class="text-xs px-2 py-1 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50 flex items-center gap-1"
          >
            <Icon v-if="generating" icon="mdi:loading" class="animate-spin" />
            <Icon v-else icon="mdi:creation" />
            {{ generating ? 'Generating...' : 'Generate' }}
          </button>
        </div>

        <p v-if="genMessage" class="text-xs mb-2" :class="genOk ? 'text-green-600' : 'text-amber-600'">
          {{ genMessage }}
        </p>

        <div v-if="suggestions.length === 0" class="text-xs text-gray-400">
          No suggestions yet. Click Generate to ask AI for a reply draft.
        </div>

        <div v-for="s in suggestions" :key="s.id" class="border dark:border-gray-700 rounded-lg p-3 mb-2">
          <p class="text-sm text-gray-800 dark:text-gray-200 whitespace-pre-wrap">{{ s.suggestionText }}</p>
          <div class="flex items-center justify-between mt-2">
            <span class="text-xs text-gray-400">
              {{ s.provider || '' }}{{ s.model ? ' / ' + s.model : '' }}
              <template v-if="s.confidence != null"> • confidence {{ Math.round(s.confidence * 100) }}%</template>
            </span>
            <span
              class="text-xs px-2 py-0.5 rounded-full"
              :class="statusClass(s.status)"
            >{{ s.status }}</span>
          </div>
          <div v-if="s.status === 'PENDING'" class="flex gap-2 mt-2">
            <button
              @click="accept(s)"
              :disabled="responding === s.id"
              class="flex-1 text-xs px-2 py-1 bg-green-600 text-white rounded hover:bg-green-700 disabled:opacity-50"
            >Accept</button>
            <button
              @click="reject(s)"
              :disabled="responding === s.id"
              class="flex-1 text-xs px-2 py-1 bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-200 rounded hover:bg-gray-300 dark:hover:bg-gray-600 disabled:opacity-50"
            >Reject</button>
          </div>
          <p v-if="s.responseNote" class="text-xs text-gray-400 mt-1">Note: {{ s.responseNote }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import aiApi from '@/api/aiApi'
import { useAuthStore } from '@/stores/authStore'

const props = defineProps({
  conversationId: { type: [Number, String], required: true },
})
defineEmits(['close'])

const authStore = useAuthStore()
const sentiment = ref(null)
const suggestions = ref([])
const scoringSentiment = ref(false)
const generating = ref(false)
const responding = ref(null)
const genMessage = ref('')
const genOk = ref(true)

const sentimentClass = computed(() => {
  const label = (sentiment.value?.label || '').toUpperCase()
  if (label.includes('NEGATIVE') || label === 'ANGRY' || label === 'FRUSTRATED')
    return 'bg-red-100 text-red-800 dark:bg-red-900/40 dark:text-red-200'
  if (label.includes('POSITIVE') || label === 'HAPPY')
    return 'bg-green-100 text-green-800 dark:bg-green-900/40 dark:text-green-200'
  return 'bg-gray-100 text-gray-700 dark:bg-gray-700 dark:text-gray-200'
})

const sentimentIcon = computed(() => {
  const label = (sentiment.value?.label || '').toUpperCase()
  if (label.includes('NEGATIVE') || label === 'ANGRY') return 'mdi:emoticon-sad-outline'
  if (label.includes('POSITIVE') || label === 'HAPPY') return 'mdi:emoticon-happy-outline'
  return 'mdi:emoticon-neutral-outline'
})

function statusClass(status) {
  return {
    PENDING: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/40 dark:text-yellow-200',
    ACCEPTED: 'bg-green-100 text-green-800 dark:bg-green-900/40 dark:text-green-200',
    REJECTED: 'bg-red-100 text-red-800 dark:bg-red-900/40 dark:text-red-200',
    EXPIRED: 'bg-gray-100 text-gray-600 dark:bg-gray-700 dark:text-gray-300',
  }[status] || 'bg-gray-100 text-gray-600'
}

function formatScore(score) {
  if (score == null) return '—'
  return score.toFixed(2)
}

async function loadSentiment() {
  try {
    const { data } = await aiApi.getLatestSentiment(props.conversationId)
    sentiment.value = data
  } catch {
    sentiment.value = null
  }
}

async function refreshSentiment() {
  scoringSentiment.value = true
  try {
    const { data } = await aiApi.scoreConversation(props.conversationId)
    if (data && data.id) {
      sentiment.value = data
    } else {
      await loadSentiment()
    }
  } catch (e) {
    console.error('Sentiment scoring failed', e)
  } finally {
    scoringSentiment.value = false
  }
}

async function loadSuggestions() {
  try {
    const { data } = await aiApi.getSuggestionsForConversation(props.conversationId)
    suggestions.value = Array.isArray(data) ? data : []
  } catch (e) {
    console.error('Failed to load suggestions', e)
    suggestions.value = []
  }
}

async function generate() {
  generating.value = true
  genMessage.value = ''
  try {
    const agentId = authStore.user?.id || null
    const { data } = await aiApi.generateSuggestion(props.conversationId, agentId)
    if (data && data.id) {
      genMessage.value = 'Suggestion generated'
      genOk.value = true
      await loadSuggestions()
    } else {
      genMessage.value = data?.message || 'AI suggestion unavailable'
      genOk.value = false
    }
  } catch (e) {
    genMessage.value = 'Failed to generate suggestion'
    genOk.value = false
  } finally {
    generating.value = false
  }
}

async function accept(s) {
  responding.value = s.id
  try {
    await aiApi.acceptSuggestion(s.id)
    await loadSuggestions()
  } catch (e) {
    console.error('Accept failed', e)
  } finally {
    responding.value = null
  }
}

async function reject(s) {
  responding.value = s.id
  try {
    await aiApi.rejectSuggestion(s.id)
    await loadSuggestions()
  } catch (e) {
    console.error('Reject failed', e)
  } finally {
    responding.value = null
  }
}

watch(() => props.conversationId, () => {
  sentiment.value = null
  suggestions.value = []
  loadSentiment()
  loadSuggestions()
})

onMounted(() => {
  loadSentiment()
  loadSuggestions()
})
</script>
