<template>
  <div class="penny-bot-management">
    <!-- Header -->
    <div class="flex justify-between items-center mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white">
          {{ $t('Penny Bot Management') }}
        </h1>
        <p class="text-gray-600 dark:text-gray-400 mt-1">
          {{ $t('Manage your intelligent Penny bots') }}
        </p>
      </div>
      <button
        @click="showCreateModal = true"
        class="inline-flex items-center px-4 py-2 bg-primary text-white rounded-md hover:bg-primary/80 transition-colors"
      >
        <Icon icon="mdi:plus" class="mr-2" />
        {{ $t('Create Penny Bot') }}
      </button>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="p-3 bg-green-100 dark:bg-green-900 rounded-full">
            <Icon icon="mdi:robot" class="h-6 w-6 text-green-600 dark:text-green-400" />
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-600 dark:text-gray-400">
              {{ $t('Active Bots') }}
            </p>
            <p class="text-2xl font-bold text-gray-900 dark:text-white">
              {{ activeBots.length }}
            </p>
          </div>
        </div>
      </div>

      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="p-3 bg-blue-100 dark:bg-blue-900 rounded-full">
            <Icon icon="mdi:pause-circle" class="h-6 w-6 text-blue-600 dark:text-blue-400" />
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-600 dark:text-gray-400">
              {{ $t('Inactive Bots') }}
            </p>
            <p class="text-2xl font-bold text-gray-900 dark:text-white">
              {{ inactiveBots.length }}
            </p>
          </div>
        </div>
      </div>

      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="p-3 bg-purple-100 dark:bg-purple-900 rounded-full">
            <Icon icon="mdi:chart-line" class="h-6 w-6 text-purple-600 dark:text-purple-400" />
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-600 dark:text-gray-400">
              {{ $t('Total Bots') }}
            </p>
            <p class="text-2xl font-bold text-gray-900 dark:text-white">
              {{ pennyBots.length }}
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loadingBots" class="loading-state">
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
        <div v-for="i in 6" :key="i" class="skeleton-card">
          <div class="animate-pulse">
            <div class="flex items-center space-x-4 mb-4">
              <div class="w-12 h-12 bg-gray-300 dark:bg-gray-600 rounded-full"></div>
              <div class="flex-1 space-y-2">
                <div class="h-4 bg-gray-300 dark:bg-gray-600 rounded w-3/4"></div>
                <div class="h-3 bg-gray-300 dark:bg-gray-600 rounded w-1/2"></div>
              </div>
            </div>
            <div class="space-y-2">
              <div class="h-3 bg-gray-300 dark:bg-gray-600 rounded"></div>
              <div class="h-3 bg-gray-300 dark:bg-gray-600 rounded w-5/6"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else-if="pennyBots.length === 0" class="empty-state">
      <div class="text-center py-12">
        <Icon icon="mdi:robot-outline" class="h-16 w-16 text-gray-400 mx-auto mb-4" />
        <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">
          {{ $t('No Penny Bots Yet') }}
        </h3>
        <p class="text-gray-600 dark:text-gray-400 mb-4">
          {{ $t('Create your first intelligent Penny bot to get started') }}
        </p>
        <button
          @click="showCreateModal = true"
          class="inline-flex items-center px-4 py-2 bg-primary text-white rounded-md hover:bg-primary/80 transition-colors"
        >
          <Icon icon="mdi:plus" class="mr-2" />
          {{ $t('Create Your First Penny Bot') }}
        </button>
      </div>
    </div>

    <!-- Bot Grid -->
    <div v-else class="bot-grid">
      <div
        v-for="bot in pennyBots"
        :key="bot.id"
        v-show="bot && bot.id"
        class="bg-white dark:bg-gray-800 rounded-lg shadow-md border border-gray-200 dark:border-gray-700 hover:shadow-lg transition-shadow duration-200 p-6"
      >
        <div class="card-header">
          <div class="bot-avatar" :class="{ 'is-inactive': !bot.isFullyActive() }">
            <div class="avatar-content">
              <Icon :icon="getBotTypeIcon(bot.botType)" class="h-8 w-8" />
            </div>
          </div>
          <div class="header-main">
            <h3 class="bot-name" :title="bot.botName">
              {{ bot.botName }}
            </h3>
            <div class="flex items-center space-x-2">
              <span
                :class="[
                  'text-xs py-1 px-2 rounded-md',
                  bot.isFullyActive()
                    ? 'bg-green-600 text-white' 
                    : 'bg-red-600 text-white'
                ]"
              >
                {{ bot.isFullyActive() ? $t('Active') : $t('Inactive') }}
              </span>
              <span class="text-xs py-1 px-2 rounded-md bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-200">
                {{ getBotTypeDisplayName(bot.botType) }}
              </span>
            </div>
          </div>
        </div>

        <div class="card-content">
          <div class="info-item">
            <span class="label">{{ $t('Type') }}:</span>
            <span class="value">{{ getBotTypeDisplayName(bot.botType) }}</span>
          </div>
          <div class="info-item">
            <span class="label">{{ $t('Created') }}:</span>
            <span class="value">{{ formatDateTime(bot.createdAt) }}</span>
          </div>
          <div v-if="bot.description" class="info-item">
            <span class="label">{{ $t('Description') }}:</span>
            <span class="value text-truncate">{{ bot.description }}</span>
          </div>
          <div class="info-item">
            <span class="label">{{ $t('Botpress ID') }}:</span>
            <span class="value text-truncate">{{ bot.getBotpressBotId() }}</span>
          </div>
        </div>

        <div class="card-footer">
          <div class="action-buttons">
            <div class="grid grid-cols-2 gap-2">
              <!-- Connection Button -->
              <button
                @click="goToConnections(bot)"
                class="flex items-center justify-center px-3 py-2 bg-blue-600 text-white text-sm font-medium rounded-md hover:bg-blue-700 transition-colors"
              >
                <Icon icon="mdi:link-variant" class="h-4 w-4 mr-1" />
                {{ $t('Connection') }}
              </button>
              
              <!-- Rules Management Button -->
              <button
                @click="goToRules(bot)"
                class="flex items-center justify-center px-3 py-2 bg-green-600 text-white text-sm font-medium rounded-md hover:bg-green-700 transition-colors"
              >
                <Icon icon="mdi:book-open-variant" class="h-4 w-4 mr-1" />
                {{ $t('Manage Rules') }}
              </button>
              
              <!-- Test Rule Button -->
              <button
                @click="testRule(bot)"
                :disabled="!bot.isFullyActive()"
                class="flex items-center justify-center px-3 py-2 bg-orange-600 text-white text-sm font-medium rounded-md hover:bg-orange-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed col-span-2"
              >
                <Icon icon="mdi:test-tube" class="h-4 w-4 mr-1" />
                {{ $t('Test Rule (Chat)') }}
              </button>
            </div>
            
            <!-- Additional Actions -->
            <div class="flex space-x-2 mt-3 pt-3 border-t border-gray-200 dark:border-gray-700">
              <button
                @click="toggleBotStatus(bot)"
                :disabled="updatingBot"
                :class="[
                  'text-sm font-medium transition-colors',
                  bot.isActive && bot.isEnabled
                    ? 'text-red-600 dark:text-red-400 hover:text-red-700 dark:hover:text-red-300'
                    : 'text-green-600 dark:text-green-400 hover:text-green-700 dark:hover:text-green-300'
                ]"
              >
                {{ bot.isActive && bot.isEnabled ? $t('Disable') : $t('Enable') }}
              </button>
              <button
                @click="editBot(bot)"
                class="text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 text-sm font-medium"
              >
                {{ $t('Edit Bot') }}
              </button>
              <button
                @click="viewAnalytics(bot)"
                class="text-purple-600 dark:text-purple-400 hover:text-purple-700 dark:hover:text-purple-300 text-sm font-medium"
              >
                {{ $t('Analytics') }}
              </button>
              <button
                @click="deleteBot(bot)"
                class="text-red-600 dark:text-red-400 hover:text-red-700 dark:hover:text-red-300 text-sm font-medium"
              >
                {{ $t('Delete') }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <PennyBotModal
      v-if="showCreateModal || editingBot"
      :bot="editingBot"
      @close="closeModal"
      @saved="onBotSaved"
    />

    <!-- Analytics Modal -->
    <PennyBotAnalyticsModal
      v-if="showAnalyticsModal && selectedBot"
      :bot="selectedBot"
      @close="showAnalyticsModal = false"
    />

    <!-- Chat Modal -->
    <PennyBotChatModal
      v-if="showChatModal && selectedBot"
      :bot="selectedBot"
      @close="showChatModal = false"
    />

    <!-- Rule Modal -->
    <PennyRuleModal
      v-if="showRuleModal && selectedBot"
      :bot="selectedBot"
      :rule="editingRule"
      @close="showRuleModal = false"
      @saved="onRuleSaved"
    />

    <!-- Rules Management Modal -->
    <PennyRulesModal
      v-if="showRulesModal && selectedBot"
      :bot="selectedBot"
      @close="showRulesModal = false"
    />

    <!-- Connections Modal -->
    <PennyConnectionsModal
      v-if="showConnectionsModal && selectedBot"
      :bot="selectedBot"
      @close="showConnectionsModal = false"
      @createConnection="createConnection"
      @saved="onConnectionSaved"
    />

    <!-- Connection Modal (for creating new connections) -->
    <PennyConnectionModal
      v-if="showConnectionModal && selectedBot"
      :bot="selectedBot"
      :connection="editingConnection"
      @close="showConnectionModal = false"
      @saved="onConnectionSaved"
    />

    <!-- Auto Connect Modal -->
    <PennyAutoConnectModal
      v-if="showAutoConnectModal && selectedBot"
      :bot="selectedBot"
      @close="showAutoConnectModal = false"
      @connected="handleAutoConnect"
    />
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { formatDate, formatDateTime } from '@/utils/dateUtils'
import { usePennyBotStore } from '@/stores/pennyBotStore'
import { usePennyRuleStore } from '@/stores/pennyRuleStore'
import { usePennyConnectionStore } from '@/stores/pennyConnectionStore'
import {
  PennyBotDto,
  PennyBotType,
  PennyBotTypeDisplay,
  PennyBotTypeBotpressId
} from '@/types/penny'
import PennyBotModal from './components/PennyBotModal.vue'
import PennyBotAnalyticsModal from './components/PennyBotAnalyticsModal.vue'
import PennyBotChatModal from './components/PennyBotChatModal.vue'
import PennyRuleModal from './components/PennyRuleModal.vue'
import PennyRulesModal from './components/PennyRulesModal.vue'
import PennyConnectionModal from './components/PennyConnectionModal.vue'
import PennyConnectionsModal from './components/PennyConnectionsModal.vue'
import PennyAutoConnectModal from './components/PennyAutoConnectModal.vue'

export default {
  name: 'PennyBotManagement',
  components: {
    PennyBotModal,
    PennyBotAnalyticsModal,
    PennyBotChatModal,
    PennyRuleModal,
    PennyRulesModal,
    PennyConnectionModal,
    PennyConnectionsModal,
    PennyAutoConnectModal
  },
  setup() {
    const router = useRouter()
    const { t } = useI18n()
    const pennyBotStore = usePennyBotStore()
    const pennyRuleStore = usePennyRuleStore()
    const pennyConnectionStore = usePennyConnectionStore()

    // Computed - Use DTOs directly from store and filter invalid bots
    const pennyBots = computed(() => 
      pennyBotStore.pennyBots.filter(bot => bot && bot.id && bot.id !== undefined)
    )
    const activeBots = computed(() => 
      pennyBots.value.filter(bot => bot.isFullyActive())
    )
    const inactiveBots = computed(() => 
      pennyBots.value.filter(bot => !bot.isFullyActive())
    )
    const loadingBots = computed(() => pennyBotStore.loadingBots)
    const updatingBot = computed(() => pennyBotStore.updatingBot)
    const deletingBot = computed(() => pennyBotStore.deletingBot)

    // Rule related computed
    const botRules = computed(() => pennyRuleStore.rules)
    const loadingRules = computed(() => pennyRuleStore.loadingRules)

    // Connection related computed
    const botConnections = computed(() => pennyConnectionStore.connections)
    const loadingConnections = computed(() => pennyConnectionStore.loadingConnections)

    // Modal states
    const showCreateModal = ref(false)
    const showAnalyticsModal = ref(false)
    const showChatModal = ref(false)
    const showRuleModal = ref(false)
    const showRulesModal = ref(false)
    const showConnectionsModal = ref(false)
    const showConnectionModal = ref(false)
    const showAutoConnectModal = ref(false)
    const showRulesList = ref(false)
    const editingBot = ref(null)
    const editingRule = ref(null)
    const editingConnection = ref(null)
    const selectedBot = ref(null)

    // Methods
    const fetchBots = async () => {
      try {
        await pennyBotStore.fetchPennyBots()
      } catch (error) {
        console.error('Failed to fetch Penny bots:', error)
      }
    }

    const toggleBotStatus = async (bot) => {
      try {
        await pennyBotStore.togglePennyBotStatus(bot.id, !bot.isEnabled)
      } catch (error) {
        console.error('Failed to toggle bot status:', error)
      }
    }

    const editBot = (bot) => {
      editingBot.value = { ...bot }
    }

    const deleteBot = async (bot) => {
      if (confirm(`Are you sure you want to delete "${bot.botName}"?`)) {
        try {
          await pennyBotStore.deletePennyBot(bot.id)
        } catch (error) {
          console.error('Failed to delete bot:', error)
        }
      }
    }

    const viewAnalytics = (bot) => {
      selectedBot.value = bot
      showAnalyticsModal.value = true
    }

    const chatWithBot = (bot) => {
      console.log('chatWithBot called with:', bot)
      console.log('bot status:', bot.isActive, bot.isEnabled)
      selectedBot.value = bot
      showChatModal.value = true
      console.log('showChatModal set to true, selectedBot:', selectedBot.value)
    }

    const openConnections = (bot) => {
      console.log('openConnections called with bot:', bot)
      if (!bot || !bot.id) {
        console.warn('Invalid bot for connection:', bot)
        return
      }
      selectedBot.value = bot
      showConnectionsModal.value = true
      console.log('showConnectionsModal set to true, selectedBot:', selectedBot.value)
    }

    const goToConnections = (bot) => {
      console.log('goToConnections called with bot:', bot)
      if (!bot || !bot.id) {
        console.warn('Invalid bot for connections:', bot)
        return
      }
      // Set current bot in store
      pennyBotStore.setCurrentBotId(bot.id)
      // Redirect without botId parameter
      router.push({ name: 'penny-connections' })
    }

    const goToRules = (bot) => {
      console.log('goToRules called with bot:', bot)
      if (!bot || !bot.id) {
        console.warn('Invalid bot for rules:', bot)
        return
      }
      // Set current bot in store
      pennyBotStore.setCurrentBotId(bot.id)
      // Redirect without botId parameter
      router.push({ name: 'penny-rules' })
    }

    const createConnection = (bot) => {
      console.log('createConnection called with bot:', bot)
      if (!bot || !bot.id) {
        console.warn('Invalid bot for connection:', bot)
        return
      }
      selectedBot.value = bot
      showConnectionModal.value = true
      console.log('showConnectionModal set to true, selectedBot:', selectedBot.value)
    }

    const createRule = (bot) => {
      selectedBot.value = bot
      showRulesModal.value = true  // Open rules management modal
    }

    const editRules = (bot) => {
      if (!bot || !bot.id || bot.id === undefined) {
        console.warn('Invalid bot selected for editing rules:', bot)
        return
      }
      selectedBot.value = bot
      showRulesModal.value = true
    }

    const deleteRules = (bot) => {
      if (!bot || !bot.id || bot.id === undefined) {
        console.warn('Invalid bot selected for deleting rules:', bot)
        return
      }
      selectedBot.value = bot
      showRulesModal.value = true
      // Note: The actual deletion will be handled in the rules modal
    }

    const testRule = (rule) => {
      if (!rule || !rule.id || rule.id === undefined) {
        console.warn('Invalid rule selected for testing rule:', rule)
        return
      }
      if (!rule.id) {
        console.warn('Invalid rule ID for testing rule:', rule)
        return
      }
      selectedBot.value = rule.bot
      editingRule.value = rule
      showChatModal.value = true
      // This opens chat modal for testing rules
    }

    const handleAutoConnect = (results) => {
      console.log('Auto-connect completed:', results)
      showAutoConnectModal.value = false
      // Refresh connections list
      if (selectedBot.value) {
        pennyConnectionStore.fetchConnections(selectedBot.value.id)
      }
    }

    const editRule = (rule) => {
      console.log('editRule called with:', rule)
      editingRule.value = rule
      showRuleModal.value = true
    }

    const deleteRuleConfirm = (rule) => {
      if (confirm(`Are you sure you want to delete rule "${rule.name}"?`)) {
        deleteRule(rule)
      }
    }

    const deleteRule = async (rule) => {
      try {
        await pennyRuleStore.deleteRule(selectedBot.value.id, rule.id)
        console.log('Rule deleted successfully')
      } catch (error) {
        console.error('Failed to delete rule:', error)
        alert('Failed to delete rule: ' + error.message)
      }
    }

    const closeModal = () => {
      showCreateModal.value = false
      editingBot.value = null
    }

    const onBotSaved = () => {
      closeModal()
      fetchBots()
    }

    const onRuleSaved = () => {
      showRuleModal.value = false
      editingRule.value = null
      fetchRules() // Refresh rules list
    }

    const onConnectionSaved = () => {
      showConnectionModal.value = false
      showConnectionsModal.value = false  // Also close connections modal after creating
      editingConnection.value = null
      // TODO: Refresh connections list if needed
    }

    const fetchRules = async () => {
      if (!selectedBot.value) return
      try {
        await pennyRuleStore.fetchRules(selectedBot.value.id)
      } catch (error) {
        console.error('Failed to fetch rules:', error)
      }
    }

    const getBotTypeIcon = (botType) => {
      const icons = {
        'CUSTOMER_SERVICE': 'mdi:headset',
        'SALES': 'mdi:cash-register',
        'SUPPORT': 'mdi:tools',
        'MARKETING': 'mdi:bullhorn',
        'HR': 'mdi:account-tie',
        'FINANCE': 'mdi:currency-usd',
        'GENERAL': 'mdi:robot'
      }
      return icons[botType] || 'mdi:robot'
    }

    const getBotTypeDisplayName = (botType) => {
      return PennyBotTypeDisplay[botType] || botType
    }

    const copyToClipboard = async (text, fieldName) => {
      try {
        await navigator.clipboard.writeText(text)
        // You could add a toast notification here if needed
        console.log(`Copied ${fieldName} to clipboard`)
      } catch (error) {
        console.error('Failed to copy to clipboard:', error)
        // Fallback for older browsers
        const textArea = document.createElement('textarea')
        textArea.value = text
        document.body.appendChild(textArea)
        textArea.select()
        document.execCommand('copy')
        document.body.removeChild(textArea)
        console.log(`Copied ${fieldName} to clipboard (fallback)`)
      }
    }

    // Lifecycle
    onMounted(() => {
      fetchBots()
    })

    return {
      // Data
      pennyBots,
      activeBots,
      inactiveBots,
      loadingBots,
      updatingBot,
      deletingBot,
      botRules,
      loadingRules,
      botConnections,
      loadingConnections,
      showCreateModal,
      showAnalyticsModal,
      showChatModal,
      showRuleModal,
      showRulesModal,
      showConnectionsModal,
      showConnectionModal,
      showAutoConnectModal,
      showRulesList,
      editingBot,
      editingRule,
      editingConnection,
      selectedBot,

      // Methods
      fetchBots,
      toggleBotStatus,
      editBot,
      deleteBot,
      viewAnalytics,
      chatWithBot,
      openConnections,
      goToConnections,
      goToRules,
      createConnection,
      handleAutoConnect,
      createRule,
      editRule,
      editRules,
      deleteRules,
      deleteRuleConfirm,
      copyToClipboard,
      closeModal,
      onBotSaved,
      onRuleSaved,
      onConnectionSaved,
      fetchRules,
      getBotTypeIcon,
      getBotTypeDisplayName,
      formatDate,
      formatDateTime
    }
  }
}
</script>

<style scoped>
.penny-bot-management {
  width: 100%;
  padding: 20px;
}

.bot-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  margin-top: 10px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}

.bot-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  background: #3b82f6;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.bot-avatar.is-inactive {
  filter: grayscale(1);
  opacity: 0.6;
}

.avatar-content {
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-main {
  flex: 1;
  overflow: hidden;
}

.bot-name {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dark .bot-name {
  color: white;
}

.card-content {
  background-color: #f9fafb;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 20px;
}

.dark .card-content {
  background-color: #374151;
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  font-size: 13px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.label {
  color: #6b7280;
  font-weight: 500;
}

.dark .label {
  color: #9ca3af;
}

.value {
  color: #374151;
  font-weight: 500;
}

.dark .value {
  color: #d1d5db;
}

.text-truncate {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-footer {
  border-top: 1px solid #e5e7eb;
  padding-top: 16px;
  margin-top: 16px;
}

.dark .card-footer {
  border-top-color: #374151;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-buttons .grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.action-buttons button {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s ease;
  border: none;
  cursor: pointer;
}

.action-buttons button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.action-buttons button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.action-buttons button.col-span-2 {
  grid-column: span 2;
}

.bg-indigo-600 { background-color: #4f46e5; }
.bg-indigo-600:hover { background-color: #4338ca; }
.bg-blue-600 { background-color: #2563eb; }
.bg-blue-600:hover { background-color: #1d4ed8; }
.bg-green-600 { background-color: #16a34a; }
.bg-green-600:hover { background-color: #15803d; }
.bg-purple-600 { background-color: #9333ea; }
.bg-purple-600:hover { background-color: #7c3aed; }
.bg-red-600 { background-color: #dc2626; }
.bg-red-600:hover { background-color: #b91c1c; }
.bg-orange-600 { background-color: #ea580c; }
.bg-orange-600:hover { background-color: #c2410c; }

.skeleton-card {
  background: white;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 20px;
}

.dark .skeleton-card {
  background: #1f2937;
  border-color: #374151;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
}

.loading-state {
  margin-top: 20px;
}
</style>
