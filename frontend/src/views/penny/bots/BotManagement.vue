<template>
  <div class="penny-bot-management">
    <!-- Header -->
    <div class="flex justify-between items-center mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white">
          {{ $t('penny.title') }}
        </h1>
        <p class="text-gray-600 dark:text-gray-400 mt-1">
          {{ $t('penny.subtitle') }}
        </p>
      </div>
      <div class="flex items-center space-x-2">
        <button
          id="btn-tour-guide"
          @click="startTour"
          class="inline-flex items-center px-4 py-2 border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-200 bg-white dark:bg-gray-800 rounded-md hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors shadow-sm"
        >
          <Icon icon="mdi:help-circle-outline" class="mr-2 text-primary" />
          {{ $t('penny.guide') }}
        </button>
        <button
          id="btn-create-bot"
          @click="showCreateModal = true"
          class="inline-flex items-center px-4 py-2 bg-primary text-white rounded-md hover:bg-primary/80 transition-colors"
        >
          <Icon icon="mdi:plus" class="mr-2" />
          {{ $t('penny.createBot') }}
        </button>
      </div>
    </div>

    <!-- Stats Cards -->
    <div id="penny-stats-container" class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="p-3 bg-green-100 dark:bg-green-900 rounded-full">
            <Icon icon="mdi:robot" class="h-6 w-6 text-green-600 dark:text-green-400" />
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-600 dark:text-gray-400">
              {{ $t('penny.activeBots') }}
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
              {{ $t('penny.inactiveBots') }}
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
              {{ $t('penny.totalBots') }}
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
          {{ $t('penny.noBotsYet') }}
        </h3>
        <p class="text-gray-600 dark:text-gray-400 mb-4">
          {{ $t('penny.createFirstBot') }}
        </p>
        <button
          @click="showCreateModal = true"
          class="inline-flex items-center px-4 py-2 bg-primary text-white rounded-md hover:bg-primary/80 transition-colors"
        >
          <Icon icon="mdi:plus" class="mr-2" />
          {{ $t('penny.createFirstBotButton') }}
        </button>
      </div>
    </div>

    <!-- Bot Grid -->
    <div v-else class="bot-grid" id="penny-bot-grid">
      <div
        v-for="(bot, index) in pennyBots"
        :key="bot.id"
        v-show="bot && bot.id"
        :id="index === 0 ? 'first-bot-card' : null"
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
                {{ bot.isFullyActive() ? $t('penny.active') : $t('penny.inactive') }}
              </span>
              <span class="text-xs py-1 px-2 rounded-md bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-200">
                {{ getBotTypeDisplayName(bot.botType) }}
              </span>
            </div>
          </div>
        </div>

        <div class="card-content">
          <div class="info-item">
            <span class="label">{{ $t('penny.type') }}:</span>
            <span class="value">{{ getBotTypeDisplayName(bot.botType) }}</span>
          </div>
          <div class="info-item">
            <span class="label">{{ $t('penny.created') }}:</span>
            <span class="value">{{ formatDateTime(bot.createdAt) }}</span>
          </div>
          <div v-if="bot.description" class="info-item">
            <span class="label">{{ $t('penny.description') }}:</span>
            <span class="value text-truncate">{{ bot.description }}</span>
          </div>
          <div class="info-item">
            <span class="label">{{ $t('penny.botpressId') }}:</span>
            <span class="value text-truncate">{{ bot.getBotpressBotId() }}</span>
          </div>
        </div>

        <div class="card-footer">
          <div class="action-buttons">
            <div class="grid grid-cols-2 gap-2">
              <!-- Connection Button -->
              <button
                @click="goToConnections(bot)"
                class="tour-btn-connections flex items-center justify-center px-3 py-2 bg-blue-600 text-white text-sm font-medium rounded-md hover:bg-blue-700 transition-colors"
              >
                <Icon icon="mdi:link-variant" class="h-4 w-4 mr-1" />
                {{ $t('penny.connection') }}
              </button>
              
              <!-- Rules Management Button -->
              <button
                @click="goToRules(bot)"
                class="tour-btn-rules flex items-center justify-center px-3 py-2 bg-green-600 text-white text-sm font-medium rounded-md hover:bg-green-700 transition-colors"
              >
                <Icon icon="mdi:book-open-variant" class="h-4 w-4 mr-1" />
                {{ $t('penny.manageRules') }}
              </button>
              
              <!-- Knowledge Base Button -->
              <button
                @click="goToKnowledgeBase(bot)"
                class="tour-btn-kb flex items-center justify-center px-3 py-2 bg-indigo-600 text-white text-sm font-medium rounded-md hover:bg-indigo-700 transition-colors"
              >
                <Icon icon="mdi:bookshelf" class="h-4 w-4 mr-1" />
                Knowledge Base
              </button>
              
              <!-- Escalation Tickets Button -->
              <button
                @click="goToEscalationTickets(bot)"
                class="tour-btn-escalation flex items-center justify-center px-3 py-2 bg-orange-600 text-white text-sm font-medium rounded-md hover:bg-orange-700 transition-colors"
              >
                <Icon icon="mdi:ticket-account" class="h-4 w-4 mr-1" />
                Escalation
              </button>
              
              <!-- Bot Configuration Button -->
              <button
                @click="goToBotConfig(bot)"
                class="tour-btn-config flex items-center justify-center px-3 py-2 bg-teal-600 text-white text-sm font-medium rounded-md hover:bg-teal-700 transition-colors col-span-2"
              >
                <Icon icon="mdi:cog" class="h-4 w-4 mr-1" />
                Configuration
              </button>
              
              <!-- Chat/Test Button -->
              <button
                @click="openChatModal(bot)"
                :disabled="!bot.isFullyActive()"
                class="tour-btn-chat flex items-center justify-center px-3 py-2 bg-purple-600 text-white text-sm font-medium rounded-md hover:bg-purple-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed col-span-2"
              >
                <Icon icon="mdi:chat" class="h-4 w-4 mr-1" />
                {{ $t('penny.chat') }}
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
                {{ bot.isActive && bot.isEnabled ? $t('penny.disable') : $t('penny.enable') }}
              </button>
              <button
                @click="editBot(bot)"
                class="text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 text-sm font-medium"
              >
                {{ $t('penny.editBot') }}
              </button>
              <button
                @click="viewAnalytics(bot)"
                class="text-purple-600 dark:text-purple-400 hover:text-purple-700 dark:hover:text-purple-300 text-sm font-medium"
              >
                {{ $t('penny.analytics') }}
              </button>
              <button
                @click="deleteBot(bot)"
                class="text-red-600 dark:text-red-400 hover:text-red-700 dark:hover:text-red-300 text-sm font-medium"
              >
                {{ $t('penny.delete') }}
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
      :isTestMode="isTestMode"
      @close="showChatModal = false"
      @modeChanged="isTestMode = $event"
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
import { driver } from 'driver.js'
import 'driver.js/dist/driver.css'
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
import PennyRuleModal from '../rules/components/PennyRuleModal.vue'
import PennyRulesModal from '../rules/components/PennyRulesModal.vue'
import PennyConnectionModal from '../connections/components/PennyConnectionModal.vue'
import PennyConnectionsModal from '../connections/components/PennyConnectionsModal.vue'
import PennyAutoConnectModal from '../connections/components/PennyAutoConnectModal.vue'

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
    const isTestMode = ref(false)
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
      if (!bot || !bot.id) {
        console.warn('Invalid bot for analytics:', bot)
        return
      }
      
      selectedBot.value = bot
      showAnalyticsModal.value = true
      }

    const openChatModal = (bot) => {
      if (!bot || !bot.id) {
        console.warn('Invalid bot for chat:', bot)
        return
      }
      selectedBot.value = bot
      isTestMode.value = false // Default to chat mode
      showChatModal.value = true
    }

    const openConnections = (bot) => {
      if (!bot || !bot.id) {
        console.warn('Invalid bot for connection:', bot)
        return
      }
      selectedBot.value = bot
      showConnectionsModal.value = true
      }

    const goToConnections = (bot) => {
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
      if (!bot || !bot.id) {
        console.warn('Invalid bot for rules:', bot)
        return
      }
      // Set current bot in store
      pennyBotStore.setCurrentBotId(bot.id)
      // Redirect without botId parameter
      router.push({ name: 'penny-rules' })
    }

    const goToKnowledgeBase = (bot) => {
      if (!bot || !bot.id) {
        console.warn('Invalid bot for knowledge base:', bot)
        return
      }
      // Navigate with botId parameter
      router.push({ name: 'penny-knowledge-base', params: { botId: bot.id } })
    }

    const goToEscalationTickets = (bot) => {
      if (!bot || !bot.id) {
        console.warn('Invalid bot for escalation tickets:', bot)
        return
      }
      // Navigate with botId parameter
      router.push({ name: 'penny-escalation', params: { botId: bot.id } })
    }

    const goToBotConfig = (bot) => {
      if (!bot || !bot.id) {
        console.warn('Invalid bot for configuration:', bot)
        return
      }
      // Navigate with botId parameter
      router.push({ name: 'penny-bot-config', params: { botId: bot.id } })
    }

    const createConnection = (bot) => {
      if (!bot || !bot.id) {
        console.warn('Invalid bot for connection:', bot)
        return
      }
      selectedBot.value = bot
      showConnectionModal.value = true
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

    const handleAutoConnect = (results) => {
      showAutoConnectModal.value = false
      // Refresh connections list
      if (selectedBot.value) {
        pennyConnectionStore.fetchConnections(selectedBot.value.id)
      }
    }

    const editRule = (rule) => {
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

    const startTour = () => {
      const tourDriver = driver({
        showProgress: true,
        animate: true,
        allowClose: true,
        overlayColor: 'rgba(0, 0, 0, 0.75)',
        nextBtnText: t('penny.next') || 'Tiếp theo',
        prevBtnText: t('penny.prev') || 'Quay lại',
        doneBtnText: t('penny.done') || 'Xong',
        steps: [
          {
            element: '#btn-tour-guide',
            popover: {
              title: 'Chào mừng đến với Penny Bot! 👋',
              description: 'Penny Bot là giải pháp chatbot AI tiên tiến cho doanh nghiệp của bạn, hỗ trợ RAG (Knowledge Base), Rules động và Chuyển giao hỗ trợ (Escalation).',
              side: 'bottom',
              align: 'end'
            }
          },
          {
            element: '#btn-create-bot',
            popover: {
              title: 'Tạo Bot Mới 🤖',
              description: 'Click vào đây để bắt đầu tạo chú bot AI đầu tiên của bạn chỉ trong vài bước.',
              side: 'bottom',
              align: 'end'
            }
          },
          {
            element: '#penny-stats-container',
            popover: {
              title: 'Thống kê Bot 📊',
              description: 'Nơi hiển thị tổng số bot của bạn, bao gồm số bot đang hoạt động và số bot tạm dừng.',
              side: 'bottom',
              align: 'center'
            }
          },
          ...(pennyBots.value.length > 0 ? [
            {
              element: '#first-bot-card',
              popover: {
                title: 'Thẻ Quản lý Bot 📇',
                description: 'Mỗi bot được biểu diễn bằng một thẻ chứa thông tin chi tiết và các phím tắt quản trị nhanh.',
                side: 'right',
                align: 'start'
              }
            },
            {
              element: '#first-bot-card .tour-btn-kb',
              popover: {
                title: 'Cơ sở Tri thức (RAG) 📚',
                description: 'Nạp tri thức cho bot bằng cách tải lên tài liệu FAQ hoặc tài liệu nghiệp vụ. AI sẽ đọc và trả lời khách hàng dựa trên dữ liệu này.',
                side: 'top',
                align: 'center'
              }
            },
            {
              element: '#first-bot-card .tour-btn-rules',
              popover: {
                title: 'Quản lý Quy tắc (Rules) ⚙️',
                description: 'Định nghĩa các từ khóa hoặc kịch bản trả lời tĩnh (ví dụ: khách hỏi "STK" -> gửi thông tin ngân hàng) để phản hồi lập tức và tiết kiệm chi phí LLM.',
                side: 'top',
                align: 'center'
              }
            },
            {
              element: '#first-bot-card .tour-btn-connections',
              popover: {
                title: 'Kết nối Kênh 🔗',
                description: 'Cấu hình kết nối bot của bạn với Facebook Fanpage hoặc các kênh chat khác để tự động chăm sóc khách hàng.',
                side: 'top',
                align: 'center'
              }
            },
            {
              element: '#first-bot-card .tour-btn-escalation',
              popover: {
                title: 'Chuyển giao cho Người thật (Escalation) 🚨',
                description: 'Quản lý các yêu cầu hỗ trợ khi AI không tự tin trả lời hoặc khách hàng yêu cầu gặp tư vấn viên.',
                side: 'top',
                align: 'center'
              }
            },
            {
              element: '#first-bot-card .tour-btn-config',
              popover: {
                title: 'Cấu hình AI 🛠️',
                description: 'Thiết lập Prompt hệ thống (System Prompt) và Ngưỡng độ tin cậy tối thiểu trước khi chuyển cho người thật.',
                side: 'top',
                align: 'center'
              }
            },
            {
              element: '#first-bot-card .tour-btn-chat',
              popover: {
                title: 'Chat Thử nghiệm 💬',
                description: 'Kiểm tra độ thông minh của bot bằng cách trò chuyện trực tiếp trước khi kết nối chính thức ra fanpage.',
                side: 'top',
                align: 'center'
              }
            }
          ] : [
            {
              element: '#penny-stats-container',
              popover: {
                title: 'Thiết lập Bot Đầu Tiên 🎉',
                description: 'Sau khi bạn tạo thành công bot đầu tiên, các nút chức năng RAG, Rules, Connections, Escalation và Chat thử nghiệm sẽ xuất hiện trên thẻ của Bot.',
                side: 'bottom',
                align: 'center'
              }
            }
          ])
        ]
      })

      tourDriver.drive()
    }

    const copyToClipboard = async (text, fieldName) => {
      try {
        await navigator.clipboard.writeText(text)
        // You could add a toast notification here if needed
        } catch (error) {
        console.error('Failed to copy to clipboard:', error)
        // Fallback for older browsers
        const textArea = document.createElement('textarea')
        textArea.value = text
        document.body.appendChild(textArea)
        textArea.select()
        document.execCommand('copy')
        document.body.removeChild(textArea)
        }
    }

    // Lifecycle
    onMounted(() => {
      fetchBots()
      
      // Auto-start tour if not completed
      setTimeout(() => {
        if (!localStorage.getItem('penny_tour_completed')) {
          startTour()
          localStorage.setItem('penny_tour_completed', 'true')
        }
      }, 1000)
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
      isTestMode,
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
      startTour,
      fetchBots,
      toggleBotStatus,
      editBot,
      deleteBot,
      viewAnalytics,
      openChatModal,
      openConnections,
      goToConnections,
      goToRules,
      goToKnowledgeBase,
      goToEscalationTickets,
      goToBotConfig,
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

/* Style overrides for driver.js popover to support Windzo theme & dark mode */
:deep(.driver-popover) {
  background-color: #ffffff !important;
  color: #1f2937 !important;
  border-radius: 8px !important;
  padding: 16px !important;
  font-family: inherit !important;
  border: 1px solid #e5e7eb !important;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05) !important;
  max-width: 350px !important;
}

.dark :deep(.driver-popover) {
  background-color: #1f2937 !important;
  color: #f3f4f6 !important;
  border-color: #374151 !important;
}

:deep(.driver-popover-title) {
  font-size: 16px !important;
  font-weight: 700 !important;
  color: #111827 !important;
  margin-bottom: 8px !important;
}

.dark :deep(.driver-popover-title) {
  color: #ffffff !important;
}

:deep(.driver-popover-description) {
  font-size: 14px !important;
  color: #4b5563 !important;
  line-height: 1.5 !important;
}

.dark :deep(.driver-popover-description) {
  color: #d1d5db !important;
}

:deep(.driver-popover-footer) {
  margin-top: 14px !important;
  display: flex !important;
  justify-content: space-between !important;
  align-items: center !important;
}

:deep(.driver-popover-btn) {
  background-color: #f3f4f6 !important;
  color: #374151 !important;
  font-size: 12px !important;
  font-weight: 600 !important;
  border-radius: 6px !important;
  border: 1px solid #d1d5db !important;
  padding: 6px 12px !important;
  text-shadow: none !important;
  transition: all 0.2s ease !important;
}

:deep(.driver-popover-btn:hover) {
  background-color: #e5e7eb !important;
}

.dark :deep(.driver-popover-btn) {
  background-color: #374151 !important;
  color: #e5e7eb !important;
  border-color: #4b5563 !important;
}

.dark :deep(.driver-popover-btn:hover) {
  background-color: #4b5563 !important;
}

:deep(.driver-popover-next-btn) {
  background-color: #2563eb !important;
  color: #ffffff !important;
  border: none !important;
}

:deep(.driver-popover-next-btn:hover) {
  background-color: #1d4ed8 !important;
}

:deep(.driver-popover-progress-text) {
  color: #9ca3af !important;
  font-size: 12px !important;
}
</style>
