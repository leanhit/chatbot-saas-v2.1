<template>
  <div class="penny-chat p-6">
    <!-- Header -->
    <div class="mb-6">
      <div class="flex justify-between items-center">
        <div>
          <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">
            Chat with {{ currentBot?.botName || 'Bot' }}
          </h1>
          <p class="text-gray-600 dark:text-gray-400">
            Interactive chat interface for your Penny bot
          </p>
        </div>
        
        <div class="flex gap-3">
          <button 
            @click="showBotSelector = true"
            class="px-4 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors flex items-center gap-2"
          >
            <Icon icon="ic:baseline-robot" />
            Switch Bot
          </button>
          <button 
            @click="showHistory = true"
            class="px-4 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors flex items-center gap-2"
          >
            <Icon icon="ic:baseline-history" />
            History
          </button>
          <button 
            @click="goToDashboard"
            class="px-4 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors flex items-center gap-2"
          >
            <Icon icon="ic:baseline-arrow-back" />
            Back to Dashboard
          </button>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="grid grid-cols-1 lg:grid-cols-4 gap-6">
      <!-- Chat Area -->
      <div class="lg:col-span-3">
        <div v-if="currentBot" class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 overflow-hidden">
          <ChatInterface 
            :bot="currentBot"
            @close="goToDashboard"
          />
        </div>
        
        <div v-else class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-12 text-center">
          <Icon icon="ic:baseline-robot" class="text-6xl text-gray-400 mb-4" />
          <h3 class="text-xl font-semibold text-gray-900 dark:text-gray-100 mb-2">
            No Bot Selected
          </h3>
          <p class="text-gray-600 dark:text-gray-400 mb-6">
            Select a bot to start chatting
          </p>
          <button 
            @click="showBotSelector = true"
            class="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center gap-2 mx-auto"
          >
            <Icon icon="ic:baseline-robot" />
            Select Bot
          </button>
        </div>
      </div>

      <!-- Sidebar -->
      <div class="lg:col-span-1 space-y-6">
        <!-- Bot Info -->
        <div v-if="currentBot" class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-6">
          <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">
            Bot Information
          </h3>
          
          <div class="space-y-4">
            <div class="flex items-center gap-3">
              <div :class="getBotTypeIconClass(currentBot.botType)" class="p-2 rounded-lg">
                <Icon :icon="getBotTypeIcon(currentBot.botType)" class="text-lg" />
              </div>
              <div>
                <p class="font-medium text-gray-900 dark:text-gray-100">{{ currentBot.botName }}</p>
                <p class="text-sm text-gray-600 dark:text-gray-400">{{ getBotTypeDisplayName(currentBot.botType) }}</p>
              </div>
            </div>
            
            <div class="space-y-2">
              <div class="flex justify-between text-sm">
                <span class="text-gray-600 dark:text-gray-400">Status:</span>
                <span :class="getStatusBadgeClass(currentBot)" class="px-2 py-1 rounded-full text-xs font-medium">
                  {{ getStatusText(currentBot) }}
                </span>
              </div>
              <div class="flex justify-between text-sm">
                <span class="text-gray-600 dark:text-gray-400">Botpress ID:</span>
                <span class="font-mono text-gray-900 dark:text-gray-100">{{ currentBot.botpressBotId || 'N/A' }}</span>
              </div>
              <div class="flex justify-between text-sm">
                <span class="text-gray-600 dark:text-gray-400">Created:</span>
                <span class="text-gray-900 dark:text-gray-100">{{ formatDate(currentBot.createdAt) }}</span>
              </div>
            </div>
            
            <div v-if="currentBot.description" class="pt-2 border-t border-gray-200 dark:border-gray-700">
              <p class="text-sm text-gray-600 dark:text-gray-400 mb-1">Description:</p>
              <p class="text-sm text-gray-900 dark:text-gray-100">{{ currentBot.description }}</p>
            </div>
          </div>
        </div>

        <!-- Quick Actions -->
        <div v-if="currentBot" class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-6">
          <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">
            Quick Actions
          </h3>
          
          <div class="space-y-2">
            <button 
              @click="viewBotDetails"
              class="w-full px-3 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors flex items-center gap-2"
            >
              <Icon icon="ic:baseline-visibility" />
              View Details
            </button>
            
            <button 
              @click="viewAnalytics"
              class="w-full px-3 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors flex items-center gap-2"
            >
              <Icon icon="ic:baseline-bar-chart" />
              View Analytics
            </button>
            
            <button 
              @click="testBot"
              class="w-full px-3 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors flex items-center gap-2"
            >
              <Icon icon="ic:baseline-bug-report" />
              Test Bot
            </button>
            
            <button 
              @click="configureBot"
              class="w-full px-3 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors flex items-center gap-2"
            >
              <Icon icon="ic:baseline-settings" />
              Configure
            </button>
          </div>
        </div>

        <!-- Recent Bots -->
        <div class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-6">
          <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">
            Recent Bots
          </h3>
          
          <div class="space-y-2">
            <button 
              v-for="bot in recentBots" 
              :key="bot.id"
              @click="selectBot(bot)"
              class="w-full p-3 text-left bg-gray-50 dark:bg-gray-700 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-600 transition-colors"
            >
              <div class="flex items-center gap-2">
                <div :class="getBotTypeIconClass(bot.botType)" class="p-1 rounded">
                  <Icon :icon="getBotTypeIcon(bot.botType)" class="text-sm" />
                </div>
                <div class="flex-1">
                  <p class="text-sm font-medium text-gray-900 dark:text-gray-100">{{ bot.botName }}</p>
                  <p class="text-xs text-gray-600 dark:text-gray-400">{{ getBotTypeDisplayName(bot.botType) }}</p>
                </div>
                <span :class="getStatusDotClass(bot)" class="w-2 h-2 rounded-full"></span>
              </div>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Bot Selector Modal -->
    <BotSelectorModal 
      v-if="showBotSelector"
      :bots="availableBots"
      @close="showBotSelector = false"
      @selected="onBotSelected"
    />

    <!-- Chat History Modal -->
    <ChatHistoryModal 
      v-if="showHistory"
      :bot="currentBot"
      @close="showHistory = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { usePennyBotStore } from '@/stores/penny/pennyBotStore'
import { useRoute, useRouter } from 'vue-router'
import ChatInterface from '@/components/penny/ChatInterface.vue'
import BotSelectorModal from '@/components/penny/BotSelectorModal.vue'
import ChatHistoryModal from '@/components/penny/ChatHistoryModal.vue'

const route = useRoute()
const router = useRouter()
const pennyBotStore = usePennyBotStore()

// State
const showBotSelector = ref(false)
const showHistory = ref(false)

// Computed
const bots = computed(() => pennyBotStore.bots)
const currentBot = computed(() => {
  const botId = route.params.botId
  return bots.value.find(bot => bot.id === botId)
})

const availableBots = computed(() => {
  return bots.value.filter(bot => bot.isActive && bot.isEnabled)
})

const recentBots = computed(() => {
  // Return bots sorted by last used, excluding current bot
  return bots.value
    .filter(bot => bot.id !== currentBot.value?.id)
    .sort((a, b) => new Date(b.lastUsedAt || 0) - new Date(a.lastUsedAt || 0))
    .slice(0, 5)
})

// Methods
const getBotTypeIcon = (botType) => {
  const iconMap = {
    'CUSTOMER_SERVICE': 'ic:baseline-support-agent',
    'SALES': 'ic:baseline-shopping-cart',
    'SUPPORT': 'ic:baseline-headset-mic',
    'MARKETING': 'ic:baseline-campaign',
    'HR': 'ic:baseline-people',
    'FINANCE': 'ic:baseline-account-balance',
    'GENERAL': 'ic:baseline-settings'
  }
  return iconMap[botType] || 'ic:baseline-settings'
}

const getBotTypeIconClass = (botType) => {
  const classMap = {
    'CUSTOMER_SERVICE': 'bg-blue-100 text-blue-600 dark:bg-blue-900/20 dark:text-blue-400',
    'SALES': 'bg-emerald-100 text-emerald-600 dark:bg-emerald-900/20 dark:text-emerald-400',
    'SUPPORT': 'bg-amber-100 text-amber-600 dark:bg-amber-900/20 dark:text-amber-400',
    'MARKETING': 'bg-purple-100 text-purple-600 dark:bg-purple-900/20 dark:text-purple-400',
    'HR': 'bg-pink-100 text-pink-600 dark:bg-pink-900/20 dark:text-pink-400',
    'FINANCE': 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400',
    'GENERAL': 'bg-gray-100 text-gray-600'
  }
  return classMap[botType] || 'bg-gray-100 text-gray-600'
}

const getBotTypeDisplayName = (botType) => {
  const nameMap = {
    'CUSTOMER_SERVICE': 'Customer Service',
    'SALES': 'Sales',
    'SUPPORT': 'Technical Support',
    'MARKETING': 'Marketing',
    'HR': 'Human Resources',
    'FINANCE': 'Finance',
    'GENERAL': 'General Purpose'
  }
  return nameMap[botType] || botType
}

const getStatusBadgeClass = (bot) => {
  if (!bot.isActive || !bot.isEnabled) {
    return 'bg-gray-100 text-gray-600 dark:bg-gray-900/20 dark:text-gray-400'
  }
  return 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400'
}

const getStatusText = (bot) => {
  if (!bot.isActive) return 'Inactive'
  if (!bot.isEnabled) return 'Disabled'
  return 'Active'
}

const getStatusDotClass = (bot) => {
  if (!bot.isActive || !bot.isEnabled) {
    return 'bg-gray-400'
  }
  return 'bg-green-500'
}

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

const selectBot = (bot) => {
  router.push(`/penny/chat/${bot.id}`)
}

const onBotSelected = (bot) => {
  showBotSelector.value = false
  router.push(`/penny/chat/${bot.id}`)
}

const goToDashboard = () => {
  router.push('/penny')
}

const viewBotDetails = () => {
  if (currentBot.value) {
    // Open bot details modal
    console.log('View details for:', currentBot.value)
  }
}

const viewAnalytics = () => {
  if (currentBot.value) {
    router.push(`/penny/bots/${currentBot.value.id}/analytics`)
  }
}

const testBot = () => {
  if (currentBot.value) {
    router.push(`/penny/bots/${currentBot.value.id}/test`)
  }
}

const configureBot = () => {
  if (currentBot.value) {
    router.push(`/penny/bots/${currentBot.value.id}/configure`)
  }
}

// Lifecycle
onMounted(async () => {
  // Fetch bots if not already loaded
  if (bots.value.length === 0) {
    await pennyBotStore.fetchBots()
  }
  
  // If no bot is selected and we have available bots, select the first one
  if (!currentBot.value && availableBots.value.length > 0) {
    router.push(`/penny/chat/${availableBots.value[0].id}`)
  }
})
</script>

<style scoped>
.penny-chat {
  max-width: 1400px;
  margin: 0 auto;
  min-height: calc(100vh - 120px);
}
</style>
