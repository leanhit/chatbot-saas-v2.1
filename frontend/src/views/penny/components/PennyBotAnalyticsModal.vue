<template>
  <div class="penny-bot-analytics-modal-backdrop" @click="closeOnBackdrop">
    <div class="penny-bot-analytics-modal" @click.stop>
      <div class="modal-header">
        <div class="header-content">
          <div class="bot-info">
            <Icon :icon="getBotTypeIcon(bot.botType)" class="h-6 w-6 mr-3" />
            <div>
              <h2 class="modal-title">{{ bot.botName }}</h2>
              <p class="bot-type">{{ getBotTypeDisplayName(bot.botType) }}</p>
            </div>
          </div>
          <button @click="$emit('close')" class="close-button">
            <Icon icon="mdi:close" class="h-5 w-5" />
          </button>
        </div>
      </div>

      <div class="modal-body">
        <!-- Time Range Selector -->
        <div class="time-range-selector">
          <label class="time-label">{{ $t('Time Range') }}:</label>
          <div class="time-buttons">
            <button
              v-for="range in timeRanges"
              :key="range.value"
              @click="selectTimeRange(range.value)"
              :class="['time-button', { active: selectedTimeRange === range.value }]"
            >
              {{ range.label }}
            </button>
          </div>
        </div>

        <!-- Loading State -->
        <div v-if="analyticsLoading" class="loading-state">
          <div class="animate-pulse">
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              <div v-for="i in 6" :key="i" class="skeleton-metric">
                <div class="h-4 bg-gray-300 dark:bg-gray-600 rounded w-1/2 mb-2"></div>
                <div class="h-8 bg-gray-300 dark:bg-gray-600 rounded"></div>
              </div>
            </div>
          </div>
        </div>

        <!-- Analytics Content -->
        <div v-else-if="analytics" class="analytics-content">
          <!-- Key Metrics -->
          <div class="metrics-grid">
            <div class="metric-card">
              <div class="metric-icon conversations">
                <Icon icon="mdi:chat" class="h-6 w-6" />
              </div>
              <div class="metric-content">
                <p class="metric-label">{{ $t('Total Conversations') }}</p>
                <p class="metric-value">{{ formatNumber(analytics.totalConversations) }}</p>
              </div>
            </div>

            <div class="metric-card">
              <div class="metric-icon messages">
                <Icon icon="mdi:message" class="h-6 w-6" />
              </div>
              <div class="metric-content">
                <p class="metric-label">{{ $t('Total Messages') }}</p>
                <p class="metric-value">{{ formatNumber(analytics.totalMessages) }}</p>
              </div>
            </div>

            <div class="metric-card">
              <div class="metric-icon response-time">
                <Icon icon="mdi:clock" class="h-6 w-6" />
              </div>
              <div class="metric-content">
                <p class="metric-label">{{ $t('Avg Response Time') }}</p>
                <p class="metric-value">{{ formatTime(analytics.averageResponseTime) }}</p>
              </div>
            </div>

            <div class="metric-card">
              <div class="metric-icon satisfaction">
                <Icon icon="mdi:emoticon-happy" class="h-6 w-6" />
              </div>
              <div class="metric-content">
                <p class="metric-label">{{ $t('Satisfaction Rate') }}</p>
                <p class="metric-value">{{ analytics.satisfactionRate }}%</p>
              </div>
            </div>

            <div class="metric-card">
              <div class="metric-icon resolution">
                <Icon icon="mdi:check-circle" class="h-6 w-6" />
              </div>
              <div class="metric-content">
                <p class="metric-label">{{ $t('Resolution Rate') }}</p>
                <p class="metric-value">{{ analytics.resolutionRate }}%</p>
              </div>
            </div>

            <div class="metric-card">
              <div class="metric-icon uptime">
                <Icon icon="mdi:server" class="h-6 w-6" />
              </div>
              <div class="metric-content">
                <p class="metric-label">{{ $t('Uptime') }}</p>
                <p class="metric-value">{{ analytics.uptime }}%</p>
              </div>
            </div>
          </div>

          <!-- Performance Charts -->
          <div class="charts-section">
            <div class="chart-container">
              <h3 class="chart-title">{{ $t('Conversation Trend') }}</h3>
              <div class="chart-placeholder">
                <Icon icon="mdi:chart-line" class="h-12 w-12 text-gray-400" />
                <p class="chart-placeholder-text">{{ $t('Chart visualization would go here') }}</p>
              </div>
            </div>

            <div class="chart-container">
              <h3 class="chart-title">{{ $t('Response Time Distribution') }}</h3>
              <div class="chart-placeholder">
                <Icon icon="mdi:chart-bar" class="h-12 w-12 text-gray-400" />
                <p class="chart-placeholder-text">{{ $t('Chart visualization would go here') }}</p>
              </div>
            </div>
          </div>

          <!-- Bot Health -->
          <div class="health-section">
            <h3 class="section-title">{{ $t('Bot Health Status') }}</h3>
            <div class="health-grid">
              <div class="health-item">
                <div class="health-indicator" :class="getHealthClass(analytics.overall)">
                  <Icon :icon="getHealthIcon(analytics.overall)" class="h-4 w-4" />
                </div>
                <span class="health-label">{{ $t('Overall Status') }}</span>
                <span class="health-value">{{ analytics.overall }}</span>
              </div>

              <div class="health-item">
                <div class="health-indicator" :class="getHealthClass(analytics.context)">
                  <Icon :icon="getHealthIcon(analytics.context)" class="h-4 w-4" />
                </div>
                <span class="health-label">{{ $t('Context Engine') }}</span>
                <span class="health-value">{{ analytics.context }}</span>
              </div>

              <div class="health-item">
                <div class="health-indicator" :class="getHealthClass(analytics.analytics)">
                  <Icon :icon="getHealthIcon(analytics.analytics)" class="h-4 w-4" />
                </div>
                <span class="health-label">{{ $t('Analytics System') }}</span>
                <span class="health-value">{{ analytics.analytics }}</span>
              </div>
            </div>
          </div>

          <!-- Bot Info -->
          <div class="info-section">
            <h3 class="section-title">{{ $t('Bot Information') }}</h3>
            <div class="info-grid">
              <div class="info-item">
                <span class="info-label">{{ $t('Bot ID') }}:</span>
                <span class="info-value">{{ analytics.botId }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">{{ $t('Bot Type') }}:</span>
                <span class="info-value">{{ analytics.botType }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">{{ $t('Status') }}:</span>
                <span class="info-value">
                  <span :class="['status-badge', analytics.isActive ? 'active' : 'inactive']">
                    {{ analytics.isActive ? $t('Active') : $t('Inactive') }}
                  </span>
                </span>
              </div>
              <div class="info-item">
                <span class="info-label">{{ $t('Created') }}:</span>
                <span class="info-value">{{ formatDateTime(analytics.createdAt) }}</span>
              </div>
              <div v-if="analytics.lastUsedAt" class="info-item">
                <span class="info-label">{{ $t('Last Used') }}:</span>
                <span class="info-value">{{ formatDateTime(analytics.lastUsedAt) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Error State -->
        <div v-else class="error-state">
          <Icon icon="mdi:alert-circle" class="h-12 w-12 text-red-500 mb-4" />
          <p class="error-message">{{ $t('Failed to load analytics data') }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { useI18n } from 'vue-i18n'
import { formatDateTime } from '@/utils/dateUtils'
import { usePennyBotStore } from '@/stores/pennyBotStore'

export default {
  name: 'PennyBotAnalyticsModal',
  components: {
    Icon
  },
  props: {
    bot: {
      type: Object,
      required: true
    }
  },
  emits: ['close'],
  setup(props) {
    const { t } = useI18n()
    const pennyBotStore = usePennyBotStore()

    const analytics = ref(null)
    const selectedTimeRange = ref('7days')
    const analyticsLoading = ref(false)

    const timeRanges = [
      { value: '1day', label: '1 Day' },
      { value: '7days', label: '7 Days' },
      { value: '30days', label: '30 Days' },
      { value: '90days', label: '90 Days' }
    ]

    const fetchAnalytics = async () => {
      if (!props.bot) return

      analyticsLoading.value = true
      try {
        const data = await pennyBotStore.getPennyBotAnalytics(props.bot.botId, selectedTimeRange.value)
        analytics.value = data
      } catch (error) {
        console.error('Failed to fetch analytics:', error)
        analytics.value = null
      } finally {
        analyticsLoading.value = false
      }
    }

    const selectTimeRange = (range) => {
      selectedTimeRange.value = range
      fetchAnalytics()
    }

    const closeOnBackdrop = (event) => {
      if (event.target === event.currentTarget) {
        props.$emit('close')
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
      const names = {
        'CUSTOMER_SERVICE': 'Customer Service',
        'SALES': 'Sales',
        'SUPPORT': 'Technical Support',
        'MARKETING': 'Marketing',
        'HR': 'Human Resources',
        'FINANCE': 'Finance',
        'GENERAL': 'General Purpose'
      }
      return names[botType] || botType
    }

    const getHealthClass = (status) => {
      return {
        'health-healthy': status === 'healthy',
        'health-unhealthy': status === 'unhealthy'
      }
    }

    const getHealthIcon = (status) => {
      return status === 'healthy' ? 'mdi:check' : 'mdi:alert'
    }

    const formatNumber = (num) => {
      if (!num) return '0'
      return new Intl.NumberFormat().format(num)
    }

    const formatTime = (ms) => {
      if (!ms) return '0ms'
      if (ms < 1000) return `${ms}ms`
      return `${(ms / 1000).toFixed(1)}s`
    }

    // Watch for bot changes and fetch analytics
    watch(() => props.bot, () => {
      if (props.bot) {
        fetchAnalytics()
      }
    }, { immediate: true })

    return {
      analytics,
      selectedTimeRange,
      analyticsLoading,
      timeRanges,
      selectTimeRange,
      closeOnBackdrop,
      getBotTypeIcon,
      getBotTypeDisplayName,
      getHealthClass,
      getHealthIcon,
      formatNumber,
      formatTime,
      formatDateTime
    }
  }
}
</script>

<style scoped>
.penny-bot-analytics-modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  z-index: 1000;
  padding: 40px 20px 20px;
}

.penny-bot-analytics-modal {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  max-width: 900px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  margin-top: 20px;
}

.dark .penny-bot-analytics-modal {
  background: #1f2937;
}

.modal-header {
  padding: 24px;
  border-bottom: 1px solid #e5e7eb;
}

.dark .modal-header {
  border-bottom-color: #374151;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.bot-info {
  display: flex;
  align-items: center;
  flex: 1;
}

.close-button {
  background: none;
  border: none;
  padding: 8px;
  border-radius: 6px;
  cursor: pointer;
  color: #6b7280;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.close-button:hover {
  background-color: #f3f4f6;
  color: #1f2937;
}

.dark .close-button {
  color: #9ca3af;
}

.dark .close-button:hover {
  background-color: #374151;
  color: white;
}

.modal-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.dark .modal-title {
  color: white;
}

.bot-type {
  margin: 4px 0 0 0;
  font-size: 14px;
  color: #6b7280;
}

.dark .bot-type {
  color: #9ca3af;
}

.modal-body {
  padding: 24px;
}

.time-range-selector {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
}

.dark .time-range-selector {
  background: #374151;
}

.time-label {
  font-weight: 500;
  color: #374151;
}

.dark .time-label {
  color: #d1d5db;
}

.time-buttons {
  display: flex;
  gap: 8px;
}

.time-button {
  padding: 8px 16px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: white;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
}

.dark .time-button {
  background: #1f2937;
  border-color: #4b5563;
  color: #d1d5db;
}

.time-button:hover {
  background: #f3f4f6;
}

.dark .time-button:hover {
  background: #374151;
}

.time-button.active {
  background: #3b82f6;
  border-color: #3b82f6;
  color: white;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 32px;
}

.metric-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.dark .metric-card {
  background: #1f2937;
  border-color: #374151;
}

.metric-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.metric-icon.conversations {
  background: #dbeafe;
  color: #1e40af;
}

.dark .metric-icon.conversations {
  background: #1e3a8a;
  color: #93c5fd;
}

.metric-icon.messages {
  background: #dcfce7;
  color: #166534;
}

.dark .metric-icon.messages {
  background: #14532d;
  color: #86efac;
}

.metric-icon.response-time {
  background: #fef3c7;
  color: #92400e;
}

.dark .metric-icon.response-time {
  background: #78350f;
  color: #fde68a;
}

.metric-icon.satisfaction {
  background: #fce7f3;
  color: #9f1239;
}

.dark .metric-icon.satisfaction {
  background: #831843;
  color: #fbcfe8;
}

.metric-icon.resolution {
  background: #e0e7ff;
  color: #3730a3;
}

.dark .metric-icon.resolution {
  background: #312e81;
  color: #c7d2fe;
}

.metric-icon.uptime {
  background: #f3e8ff;
  color: #6b21a8;
}

.dark .metric-icon.uptime {
  background: #581c87;
  color: #e9d5ff;
}

.metric-content {
  flex: 1;
}

.metric-label {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 4px;
}

.dark .metric-label {
  color: #9ca3af;
}

.metric-value {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.dark .metric-value {
  color: white;
}

.charts-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 32px;
}

.chart-container {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 20px;
}

.dark .chart-container {
  background: #1f2937;
  border-color: #374151;
}

.chart-title {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.dark .chart-title {
  color: white;
}

.chart-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  text-align: center;
}

.chart-placeholder-text {
  margin-top: 8px;
  color: #6b7280;
  font-size: 14px;
}

.dark .chart-placeholder-text {
  color: #9ca3af;
}

.health-section {
  margin-bottom: 32px;
}

.section-title {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.dark .section-title {
  color: white;
}

.health-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.health-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.dark .health-item {
  background: #1f2937;
  border-color: #374151;
}

.health-indicator {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  color: white;
}

.health-indicator.health-healthy {
  background: #10b981;
}

.health-indicator.health-unhealthy {
  background: #ef4444;
}

.health-label {
  flex: 1;
  font-size: 14px;
  color: #374151;
}

.dark .health-label {
  color: #d1d5db;
}

.health-value {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}

.dark .health-value {
  color: white;
}

.info-section {
  margin-bottom: 32px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 8px;
}

.dark .info-item {
  background: #374151;
}

.info-label {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
}

.dark .info-label {
  color: #9ca3af;
}

.info-value {
  font-size: 14px;
  color: #1f2937;
  font-weight: 500;
}

.dark .info-value {
  color: #d1d5db;
}

.status-badge {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.active {
  background: #dcfce7;
  color: #166534;
}

.dark .status-badge.active {
  background: #14532d;
  color: #86efac;
}

.status-badge.inactive {
  background: #f3f4f6;
  color: #6b7280;
}

.dark .status-badge.inactive {
  background: #374151;
  color: #9ca3af;
}

.loading-state {
  padding: 40px;
}

.skeleton-metric {
  padding: 20px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
}

.dark .skeleton-metric {
  background: #1f2937;
  border-color: #374151;
}

.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  text-align: center;
}

.error-message {
  color: #ef4444;
  font-size: 16px;
}
</style>
