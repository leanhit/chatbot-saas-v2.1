<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white dark:bg-gray-800 rounded-lg max-w-4xl w-full mx-4 max-h-[90vh] overflow-y-auto">
      <!-- Header -->
      <div class="p-6 border-b border-gray-200 dark:border-gray-700">
        <div class="flex justify-between items-center">
          <div>
            <h3 class="text-xl font-semibold text-gray-900 dark:text-gray-100">
              Bot Details
            </h3>
            <p class="text-gray-600 dark:text-gray-400">
              {{ bot.botName }}
            </p>
          </div>
          <button 
            @click="$emit('close')"
            class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
          >
            <Icon icon="ic:baseline-close" class="text-xl" />
          </button>
        </div>
      </div>

      <!-- Tabs -->
      <div class="border-b border-gray-200 dark:border-gray-700">
        <nav class="flex space-x-8 px-6">
          <button 
            v-for="tab in tabs" 
            :key="tab.id"
            @click="activeTab = tab.id"
            :class="[
              'py-4 px-1 border-b-2 font-medium text-sm transition-colors',
              activeTab === tab.id 
                ? 'border-blue-500 text-blue-600 dark:text-blue-400' 
                : 'border-transparent text-gray-500 hover:text-gray-700 dark:hover:text-gray-300'
            ]"
          >
            <Icon :icon="tab.icon" class="mr-2" />
            {{ tab.name }}
          </button>
        </nav>
      </div>

      <!-- Tab Content -->
      <div class="p-6">
        <!-- Overview Tab -->
        <div v-show="activeTab === 'overview'" class="space-y-6">
          <!-- Basic Information -->
          <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-6">
            <h4 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">
              Basic Information
            </h4>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="text-sm text-gray-600 dark:text-gray-400">Bot Name</label>
                <p class="font-medium text-gray-900 dark:text-gray-100">{{ bot.botName }}</p>
              </div>
              <div>
                <label class="text-sm text-gray-600 dark:text-gray-400">Bot Type</label>
                <div class="flex items-center gap-2">
                  <div :class="getBotTypeIconClass(bot.botType)" class="p-1 rounded">
                    <Icon :icon="getBotTypeIcon(bot.botType)" class="text-sm" />
                  </div>
                  <span class="font-medium text-gray-900 dark:text-gray-100">
                    {{ getBotTypeDisplayName(bot.botType) }}
                  </span>
                </div>
              </div>
              <div>
                <label class="text-sm text-gray-600 dark:text-gray-400">Status</label>
                <span :class="getStatusBadgeClass(bot)" class="px-2 py-1 rounded-full text-xs font-medium">
                  {{ getStatusText(bot) }}
                </span>
              </div>
              <div>
                <label class="text-sm text-gray-600 dark:text-gray-400">Botpress ID</label>
                <p class="font-mono text-sm text-gray-900 dark:text-gray-100">
                  {{ bot.botpressBotId || 'Not connected' }}
                </p>
              </div>
              <div>
                <label class="text-sm text-gray-600 dark:text-gray-400">Created</label>
                <p class="font-medium text-gray-900 dark:text-gray-100">{{ formatDate(bot.createdAt) }}</p>
              </div>
              <div>
                <label class="text-sm text-gray-600 dark:text-gray-400">Last Used</label>
                <p class="font-medium text-gray-900 dark:text-gray-100">
                  {{ bot.lastUsedAt ? formatDate(bot.lastUsedAt) : 'Never' }}
                </p>
              </div>
            </div>
            
            <div v-if="bot.description" class="mt-4">
              <label class="text-sm text-gray-600 dark:text-gray-400">Description</label>
              <p class="text-gray-900 dark:text-gray-100 mt-1">{{ bot.description }}</p>
            </div>
          </div>

          <!-- Quick Actions -->
          <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-6">
            <h4 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">
              Quick Actions
            </h4>
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              <button 
                @click="chatWithBot"
                :disabled="!isBotActive"
                class="flex flex-col items-center p-4 bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-600 hover:border-blue-500 transition-colors"
              >
                <Icon icon="ic:baseline-chat" class="text-2xl text-blue-600 dark:text-blue-400 mb-2" />
                <span class="text-sm font-medium text-gray-900 dark:text-gray-100">Chat</span>
              </button>
              
              <button 
                @click="viewAnalytics"
                class="flex flex-col items-center p-4 bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-600 hover:border-blue-500 transition-colors"
              >
                <Icon icon="ic:baseline-bar-chart" class="text-2xl text-green-600 dark:text-green-400 mb-2" />
                <span class="text-sm font-medium text-gray-900 dark:text-gray-100">Analytics</span>
              </button>
              
              <button 
                @click="editConfiguration"
                class="flex flex-col items-center p-4 bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-600 hover:border-blue-500 transition-colors"
              >
                <Icon icon="ic:baseline-settings" class="text-2xl text-purple-600 dark:text-purple-400 mb-2" />
                <span class="text-sm font-medium text-gray-900 dark:text-gray-100">Configure</span>
              </button>
              
              <button 
                @click="testBot"
                :disabled="!isBotActive"
                class="flex flex-col items-center p-4 bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-600 hover:border-blue-500 transition-colors"
              >
                <Icon icon="ic:baseline-bug-report" class="text-2xl text-orange-600 dark:text-orange-400 mb-2" />
                <span class="text-sm font-medium text-gray-900 dark:text-gray-100">Test</span>
              </button>
            </div>
          </div>
        </div>

        <!-- Configuration Tab -->
        <div v-show="activeTab === 'configuration'" class="space-y-6">
          <div class="flex justify-between items-center mb-4">
            <h4 class="text-lg font-semibold text-gray-900 dark:text-gray-100">
              Bot Configuration
            </h4>
            <div class="flex gap-2">
              <button 
                @click="formatConfiguration"
                class="px-3 py-1 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded hover:bg-gray-200 dark:hover:bg-gray-600"
              >
                <Icon icon="ic:baseline-auto-fix-high" class="mr-1" />
                Format
              </button>
              <button 
                @click="saveConfiguration"
                :disabled="isSaving"
                class="px-3 py-1 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
              >
                <Icon v-if="isSaving" icon="ic:baseline-refresh" class="animate-spin mr-1" />
                Save
              </button>
            </div>
          </div>
          
          <div class="relative">
            <textarea 
              v-model="configurationText"
              rows="20"
              class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-gray-900 dark:bg-gray-800 text-gray-100 font-mono text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            ></textarea>
            <div v-if="configurationError" class="absolute top-2 right-2 px-2 py-1 bg-red-100 dark:bg-red-900/20 text-red-600 dark:text-red-400 rounded text-xs">
              {{ configurationError }}
            </div>
          </div>
        </div>

        <!-- Analytics Tab -->
        <div v-show="activeTab === 'analytics'" class="space-y-6">
          <div class="flex justify-between items-center mb-4">
            <h4 class="text-lg font-semibold text-gray-900 dark:text-gray-100">
              Bot Analytics
            </h4>
            <select 
              v-model="analyticsTimeRange"
              class="px-3 py-1 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100"
            >
              <option value="7days">Last 7 Days</option>
              <option value="30days">Last 30 Days</option>
              <option value="90days">Last 90 Days</option>
            </select>
          </div>

          <!-- Analytics Cards -->
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <div class="bg-white dark:bg-gray-800 rounded-lg p-4 border border-gray-200 dark:border-gray-700">
              <div class="flex items-center gap-3">
                <div class="p-2 bg-blue-100 dark:bg-blue-900/20 rounded-lg">
                  <Icon icon="ic:baseline-chat" class="text-blue-600 dark:text-blue-400 text-xl" />
                </div>
                <div>
                  <p class="text-sm text-gray-600 dark:text-gray-400">Total Messages</p>
                  <p class="text-2xl font-bold text-gray-900 dark:text-gray-100">{{ analytics.totalMessages }}</p>
                </div>
              </div>
            </div>

            <div class="bg-white dark:bg-gray-800 rounded-lg p-4 border border-gray-200 dark:border-gray-700">
              <div class="flex items-center gap-3">
                <div class="p-2 bg-green-100 dark:bg-green-900/20 rounded-lg">
                  <Icon icon="ic:baseline-thumb-up" class="text-green-600 dark:text-green-400 text-xl" />
                </div>
                <div>
                  <p class="text-sm text-gray-600 dark:text-gray-400">Satisfaction Rate</p>
                  <p class="text-2xl font-bold text-gray-900 dark:text-gray-100">{{ analytics.satisfactionRate }}%</p>
                </div>
              </div>
            </div>

            <div class="bg-white dark:bg-gray-800 rounded-lg p-4 border border-gray-200 dark:border-gray-700">
              <div class="flex items-center gap-3">
                <div class="p-2 bg-orange-100 dark:bg-orange-900/20 rounded-lg">
                  <Icon icon="ic:baseline-speed" class="text-orange-600 dark:text-orange-400 text-xl" />
                </div>
                <div>
                  <p class="text-sm text-gray-600 dark:text-gray-400">Avg Response Time</p>
                  <p class="text-2xl font-bold text-gray-900 dark:text-gray-100">{{ analytics.avgResponseTime }}ms</p>
                </div>
              </div>
            </div>

            <div class="bg-white dark:bg-gray-800 rounded-lg p-4 border border-gray-200 dark:border-gray-700">
              <div class="flex items-center gap-3">
                <div class="p-2 bg-purple-100 dark:bg-purple-900/20 rounded-lg">
                  <Icon icon="ic:baseline-people" class="text-purple-600 dark:text-purple-400 text-xl" />
                </div>
                <div>
                  <p class="text-sm text-gray-600 dark:text-gray-400">Active Users</p>
                  <p class="text-2xl font-bold text-gray-900 dark:text-gray-100">{{ analytics.activeUsers }}</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Charts Placeholder -->
          <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-6">
            <h5 class="font-medium text-gray-900 dark:text-gray-100 mb-4">Message Volume Over Time</h5>
            <div class="h-64 flex items-center justify-center text-gray-500">
              <div class="text-center">
                <Icon icon="ic:baseline-bar-chart" class="text-4xl mb-2" />
                <p>Analytics charts will be displayed here</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Health Tab -->
        <div v-show="activeTab === 'health'" class="space-y-6">
          <div class="flex justify-between items-center mb-4">
            <h4 class="text-lg font-semibold text-gray-900 dark:text-gray-100">
              Bot Health Status
            </h4>
            <button 
              @click="refreshHealth"
              :disabled="isRefreshingHealth"
              class="px-3 py-1 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded hover:bg-gray-200 dark:hover:bg-gray-600"
            >
              <Icon v-if="!isRefreshingHealth" icon="ic:baseline-refresh" class="mr-1" />
              <Icon v-else icon="ic:baseline-refresh" class="animate-spin mr-1" />
              Refresh
            </button>
          </div>

          <!-- Health Status -->
          <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-6">
            <div class="flex items-center gap-4 mb-6">
              <div :class="getHealthStatusClass(health.status)" class="p-4 rounded-lg">
                <Icon :icon="getHealthIcon(health.status)" class="text-3xl" />
              </div>
              <div>
                <h5 class="text-lg font-semibold text-gray-900 dark:text-gray-100">
                  {{ health.status }}
                </h5>
                <p class="text-gray-600 dark:text-gray-400">
                  {{ health.message }}
                </p>
                <p class="text-sm text-gray-500 dark:text-gray-500 mt-1">
                  Last checked: {{ health.lastChecked }}
                </p>
              </div>
            </div>

            <!-- Health Metrics -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div class="bg-white dark:bg-gray-800 rounded-lg p-4">
                <div class="flex justify-between items-center">
                  <span class="text-gray-600 dark:text-gray-400">Response Time</span>
                  <span class="font-medium text-gray-900 dark:text-gray-100">{{ health.responseTime }}ms</span>
                </div>
              </div>
              <div class="bg-white dark:bg-gray-800 rounded-lg p-4">
                <div class="flex justify-between items-center">
                  <span class="text-gray-600 dark:text-gray-400">Uptime</span>
                  <span class="font-medium text-gray-900 dark:text-gray-100">{{ health.uptime }}%</span>
                </div>
              </div>
              <div class="bg-white dark:bg-gray-800 rounded-lg p-4">
                <div class="flex justify-between items-center">
                  <span class="text-gray-600 dark:text-gray-400">Error Rate</span>
                  <span class="font-medium text-gray-900 dark:text-gray-100">{{ health.errorRate }}%</span>
                </div>
              </div>
              <div class="bg-white dark:bg-gray-800 rounded-lg p-4">
                <div class="flex justify-between items-center">
                  <span class="text-gray-600 dark:text-gray-400">Memory Usage</span>
                  <span class="font-medium text-gray-900 dark:text-gray-100">{{ health.memoryUsage }}%</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Footer Actions -->
      <div class="p-6 border-t border-gray-200 dark:border-gray-700">
        <div class="flex justify-between">
          <div class="flex gap-2">
            <button 
              @click="toggleBot"
              :disabled="isToggling"
              class="px-4 py-2 rounded-lg transition-colors flex items-center gap-2"
              :class="bot.isEnabled 
                ? 'bg-red-600 text-white hover:bg-red-700' 
                : 'bg-green-600 text-white hover:bg-green-700'"
            >
              <Icon v-if="isToggling" icon="ic:baseline-refresh" class="animate-spin" />
              {{ bot.isEnabled ? 'Disable Bot' : 'Enable Bot' }}
            </button>
            
            <button 
              @click="deleteBot"
              :disabled="isDeleting"
              class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:opacity-50 transition-colors flex items-center gap-2"
            >
              <Icon v-if="isDeleting" icon="ic:baseline-refresh" class="animate-spin" />
              Delete Bot
            </button>
          </div>
          
          <button 
            @click="$emit('close')"
            class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-600 transition-colors"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { usePennyBotStore } from '@/stores/penny/pennyBotStore'

const props = defineProps({
  bot: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'updated', 'deleted', 'toggled'])

const pennyBotStore = usePennyBotStore()

// State
const activeTab = ref('overview')
const configurationText = ref('')
const configurationError = ref('')
const isSaving = ref(false)
const isToggling = ref(false)
const isDeleting = ref(false)
const analyticsTimeRange = ref('7days')
const isRefreshingHealth = ref(false)

// Mock data
const analytics = ref({
  totalMessages: 1234,
  satisfactionRate: 94,
  avgResponseTime: 850,
  activeUsers: 156
})

const health = ref({
  status: 'Healthy',
  message: 'All systems operational',
  lastChecked: new Date().toLocaleString(),
  responseTime: 245,
  uptime: 99.9,
  errorRate: 0.1,
  memoryUsage: 45
})

// Tabs configuration
const tabs = [
  { id: 'overview', name: 'Overview', icon: 'ic:baseline-info' },
  { id: 'configuration', name: 'Configuration', icon: 'ic:baseline-settings' },
  { id: 'analytics', name: 'Analytics', icon: 'ic:baseline-bar-chart' },
  { id: 'health', name: 'Health', icon: 'ic:baseline-health-check' }
]

// Computed
const isBotActive = computed(() => {
  return props.bot.isActive && props.bot.isEnabled
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

const getHealthStatusClass = (status) => {
  switch (status?.toLowerCase()) {
    case 'healthy':
      return 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400'
    case 'warning':
      return 'bg-yellow-100 text-yellow-600 dark:bg-yellow-900/20 dark:text-yellow-400'
    case 'error':
      return 'bg-red-100 text-red-600 dark:bg-red-900/20 dark:text-red-400'
    default:
      return 'bg-gray-100 text-gray-600'
  }
}

const getHealthIcon = (status) => {
  switch (status?.toLowerCase()) {
    case 'healthy':
      return 'ic:baseline-check-circle'
    case 'warning':
      return 'ic:baseline-warning'
    case 'error':
      return 'ic:baseline-error'
    default:
      return 'ic:baseline-help'
  }
}

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatConfiguration = () => {
  try {
    const parsed = JSON.parse(configurationText.value)
    configurationText.value = JSON.stringify(parsed, null, 2)
    configurationError.value = ''
  } catch (error) {
    configurationError.value = 'Invalid JSON format'
  }
}

const saveConfiguration = async () => {
  try {
    const parsed = JSON.parse(configurationText.value)
    isSaving.value = true
    
    await pennyBotStore.updateBot(props.bot.id, { configuration: parsed })
    emit('updated', { ...props.bot, configuration: parsed })
    
  } catch (error) {
    configurationError.value = error.message
  } finally {
    isSaving.value = false
  }
}

const toggleBot = async () => {
  if (!confirm(`Are you sure you want to ${props.bot.isEnabled ? 'disable' : 'enable'} this bot?`)) {
    return
  }
  
  isToggling.value = true
  
  try {
    const updatedBot = await pennyBotStore.toggleBotStatus(props.bot.id, !props.bot.isEnabled)
    emit('toggled', updatedBot)
  } catch (error) {
    console.error('Failed to toggle bot:', error)
  } finally {
    isToggling.value = false
  }
}

const deleteBot = async () => {
  if (!confirm('Are you sure you want to delete this bot? This action cannot be undone.')) {
    return
  }
  
  isDeleting.value = true
  
  try {
    await pennyBotStore.deleteBot(props.bot.id)
    emit('deleted', props.bot)
    emit('close')
  } catch (error) {
    console.error('Failed to delete bot:', error)
  } finally {
    isDeleting.value = false
  }
}

const chatWithBot = () => {
  // Navigate to chat interface
  console.log('Navigate to chat for bot:', props.bot.id)
}

const viewAnalytics = () => {
  activeTab.value = 'analytics'
}

const editConfiguration = () => {
  activeTab.value = 'configuration'
}

const testBot = () => {
  // Open testing interface
  console.log('Test bot:', props.bot.id)
}

const refreshHealth = async () => {
  isRefreshingHealth.value = true
  
  try {
    const healthData = await pennyBotStore.getBotHealth(props.bot.id)
    health.value = healthData
  } catch (error) {
    console.error('Failed to fetch health:', error)
  } finally {
    isRefreshingHealth.value = false
  }
}

// Lifecycle
onMounted(() => {
  // Initialize configuration text
  if (props.bot.configuration) {
    try {
      configurationText.value = JSON.stringify(JSON.parse(props.bot.configuration), null, 2)
    } catch {
      configurationText.value = props.bot.configuration
    }
  } else {
    configurationText.value = '{}'
  }
  
  // Load initial health status
  refreshHealth()
})
</script>

<style scoped>
.modal-backdrop {
  backdrop-filter: blur(4px);
}

.tab-content {
  min-height: 400px;
}
</style>
