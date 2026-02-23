<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white dark:bg-gray-800 rounded-lg max-w-4xl w-full mx-4 max-h-[90vh] overflow-y-auto">
      <!-- Header -->
      <div class="p-6 border-b border-gray-200 dark:border-gray-700">
        <div class="flex justify-between items-center">
          <div>
            <h3 class="text-xl font-semibold text-gray-900 dark:text-gray-100">
              Entitlement Details
            </h3>
            <p class="text-gray-600 dark:text-gray-400">
              {{ entitlement.featureName }}
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

      <!-- Entitlement Overview -->
      <div class="p-6">
        <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-6 mb-6">
          <div class="flex items-center gap-4 mb-4">
            <div :class="getEntitlementIconClass(entitlement.category)" class="p-3 rounded-lg">
              <Icon :icon="getEntitlementIcon(entitlement.category)" class="text-2xl" />
            </div>
            <div>
              <h4 class="text-lg font-semibold text-gray-900 dark:text-gray-100">
                {{ entitlement.featureName }}
              </h4>
              <p class="text-gray-600 dark:text-gray-400">
                {{ entitlement.description }}
              </p>
              <div class="flex items-center gap-2 mt-2">
                <span class="px-2 py-1 bg-gray-100 dark:bg-gray-600 rounded-full text-xs font-medium text-gray-600 dark:text-gray-300">
                  {{ entitlement.category }}
                </span>
                <span :class="getStatusClass(entitlement)" class="px-2 py-1 rounded-full text-xs font-medium">
                  {{ getStatusText(entitlement) }}
                </span>
              </div>
            </div>
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
          <!-- Usage Tab -->
          <div v-show="activeTab === 'usage'" class="space-y-6">
            <h4 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">
              Current Usage
            </h4>
            
            <!-- Usage Overview -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
              <div class="bg-white dark:bg-gray-800 rounded-lg p-4 border border-gray-200 dark:border-gray-700">
                <div class="flex justify-between items-center mb-3">
                  <span class="text-sm text-gray-600 dark:text-gray-400">Current Usage</span>
                  <span class="text-lg font-bold text-gray-900 dark:text-gray-100">
                    {{ formatNumber(entitlement.currentUsage) }} {{ entitlement.unit }}
                  </span>
                </div>
                <div class="flex justify-between items-center mb-3">
                  <span class="text-sm text-gray-600 dark:text-gray-400">Limit</span>
                  <span class="text-lg font-bold text-gray-900 dark:text-gray-100">
                    {{ formatNumber(entitlement.limit) }} {{ entitlement.unit }}
                  </span>
                </div>
                <div class="flex justify-between items-center">
                  <span class="text-sm text-gray-600 dark:text-gray-400">Remaining</span>
                  <span :class="getRemainingClass(entitlement)" class="text-lg font-bold">
                    {{ formatNumber(entitlement.limit - entitlement.currentUsage) }} {{ entitlement.unit }}
                  </span>
                </div>
              </div>

              <div class="bg-white dark:bg-gray-800 rounded-lg p-4 border border-gray-200 dark:border-gray-700">
                <div class="mb-3">
                  <div class="flex justify-between items-center mb-2">
                    <span class="text-sm text-gray-600 dark:text-gray-400">Usage Percentage</span>
                    <span :class="getUsagePercentageClass(entitlement)" class="text-lg font-bold">
                      {{ getUsagePercentage(entitlement) }}%
                    </span>
                  </div>
                  <div class="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
                    <div 
                      :class="getUsageBarClass(entitlement)"
                      class="h-2 rounded-full transition-all duration-300"
                      :style="{ width: getUsagePercentage(entitlement) + '%' }"
                    ></div>
                  </div>
                </div>
                <div class="space-y-2 text-sm">
                  <div class="flex justify-between">
                    <span class="text-gray-600 dark:text-gray-400">Status:</span>
                    <span :class="getStatusClass(entitlement)" class="px-2 py-1 rounded-full text-xs font-medium">
                      {{ getStatusText(entitlement) }}
                    </span>
                  </div>
                  <div class="flex justify-between">
                    <span class="text-gray-600 dark:text-gray-400">Reset Period:</span>
                    <span class="text-gray-900 dark:text-gray-100">{{ entitlement.resetPeriod }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Usage Meter -->
            <div class="bg-white dark:bg-gray-800 rounded-lg p-4 border border-gray-200 dark:border-gray-700">
              <h5 class="font-medium text-gray-900 dark:text-gray-100 mb-3">Usage Meter</h5>
              <UsageMeter 
                :used="entitlement.currentUsage"
                :limit="entitlement.limit"
                :show-percentage="true"
                :show-labels="true"
                :height="60"
              />
            </div>

            <!-- Reset Information -->
            <div class="bg-blue-50 dark:bg-blue-900/20 rounded-lg p-4">
              <h5 class="font-medium text-blue-900 dark:text-blue-100 mb-2">Reset Information</h5>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
                <div>
                  <span class="text-blue-700 dark:text-blue-300">Last Reset:</span>
                  <span class="text-blue-900 dark:text-blue-100 font-medium ml-2">
                    {{ formatDate(entitlement.lastReset) }}
                  </span>
                </div>
                <div>
                  <span class="text-blue-700 dark:text-blue-300">Next Reset:</span>
                  <span class="text-blue-900 dark:text-blue-100 font-medium ml-2">
                    {{ formatDate(entitlement.nextReset) }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- History Tab -->
          <div v-show="activeTab === 'history'" class="space-y-6">
            <h4 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">
              Usage History
            </h4>
            
            <!-- History Chart -->
            <div class="bg-white dark:bg-gray-800 rounded-lg p-4 border border-gray-200 dark:border-gray-700 mb-6">
              <h5 class="font-medium text-gray-900 dark:text-gray-100 mb-3">Usage Trend (Last 30 Days)</h5>
              <div class="h-64 flex items-center justify-center bg-gray-50 dark:bg-gray-700 rounded-lg">
                <div class="text-center text-gray-500">
                  <Icon icon="ic:baseline-bar-chart" class="text-4xl mb-2" />
                  <p>Usage chart will be displayed here</p>
                </div>
              </div>
            </div>

            <!-- History Table -->
            <div class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700">
              <div class="overflow-x-auto">
                <table class="w-full">
                  <thead class="bg-gray-50 dark:bg-gray-700 border-b border-gray-200 dark:border-gray-700">
                    <tr>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                        Date
                      </th>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                        Usage
                      </th>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                        Cost
                      </th>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                        Reset
                      </th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-gray-200 dark:divide-gray-700">
                    <tr v-for="record in usageHistory" :key="record.id">
                      <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-100">
                        {{ formatDate(record.date) }}
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-100">
                        {{ formatNumber(record.usage) }} {{ entitlement.unit }}
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-100">
                        ${{ formatCurrency(record.cost) }}
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-100">
                        {{ formatDate(record.resetDate) }}
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          <!-- Settings Tab -->
          <div v-show="activeTab === 'settings'" class="space-y-6">
            <h4 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">
              Entitlement Settings
            </h4>
            
            <!-- Notifications -->
            <div class="bg-white dark:bg-gray-800 rounded-lg p-4 border border-gray-200 dark:border-gray-700 mb-6">
              <h5 class="font-medium text-gray-900 dark:text-gray-100 mb-3">Usage Notifications</h5>
              <div class="space-y-3">
                <label class="flex items-center gap-2">
                  <input 
                    type="checkbox" 
                    v-model="settings.emailNotifications"
                    class="rounded"
                  />
                  <span class="text-sm text-gray-700 dark:text-gray-300">
                    Email notifications when usage exceeds threshold
                  </span>
                </label>
                
                <div v-if="settings.emailNotifications" class="ml-6 space-y-3">
                  <div>
                    <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">
                      Notification Threshold (%)
                    </label>
                    <input 
                      v-model="settings.notificationThreshold"
                      type="number"
                      min="1"
                      max="100"
                      class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>
                </div>
                
                <label class="flex items-center gap-2">
                  <input 
                    type="checkbox" 
                    v-model="settings.weeklyReports"
                    class="rounded"
                  />
                  <span class="text-sm text-gray-700 dark:text-gray-300">
                    Weekly usage reports
                  </span>
                </label>
              </div>
            </div>

            <!-- Auto-Upgrade -->
            <div class="bg-white dark:bg-gray-800 rounded-lg p-4 border border-gray-200 dark:border-gray-700 mb-6">
              <h5 class="font-medium text-gray-900 dark:text-gray-100 mb-3">Auto-Upgrade Settings</h5>
              <div class="space-y-3">
                <label class="flex items-center gap-2">
                  <input 
                    type="checkbox" 
                    v-model="settings.autoUpgrade"
                    class="rounded"
                  />
                  <span class="text-sm text-gray-700 dark:text-gray-300">
                    Automatically upgrade when limit is reached
                  </span>
                </label>
                
                <div v-if="settings.autoUpgrade" class="ml-6 space-y-3">
                  <div>
                    <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">
                      Upgrade to
                    </label>
                    <select 
                      v-model="settings.upgradeTier"
                      class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    >
                      <option value="PROFESSIONAL">Professional</option>
                      <option value="ENTERPRISE">Enterprise</option>
                      <option value="CUSTOM">Custom Limit</option>
                    </select>
                  </div>
                  
                  <div v-if="settings.upgradeTier === 'CUSTOM'">
                    <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">
                      Custom Limit
                    </label>
                    <input 
                      v-model="settings.customLimit"
                      type="number"
                      min="1"
                      class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Footer -->
      <div class="p-6 border-t border-gray-200 dark:border-gray-700">
        <div class="flex justify-between">
          <div class="text-sm text-gray-600 dark:text-gray-400">
            Last updated: {{ formatDate(entitlement.updatedAt) }}
          </div>
          
          <div class="flex gap-3">
            <button 
              @click="$emit('close')"
              class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-600 transition-colors"
            >
              Close
            </button>
            
            <button 
              v-if="activeTab === 'settings'"
              @click="saveSettings"
              :disabled="isSaving"
              class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors flex items-center gap-2"
            >
              <Icon v-if="isSaving" icon="ic:baseline-refresh" class="animate-spin" />
              {{ isSaving ? 'Saving...' : 'Save Settings' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  entitlement: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'upgraded'])

// State
const activeTab = ref('usage')
const isSaving = ref(false)

// Settings
const settings = ref({
  emailNotifications: true,
  notificationThreshold: 80,
  weeklyReports: false,
  autoUpgrade: false,
  upgradeTier: 'PROFESSIONAL',
  customLimit: ''
})

// Mock usage history
const usageHistory = ref([
  {
    id: '1',
    date: '2024-01-20T00:00:00Z',
    usage: 8500,
    cost: 8.50,
    resetDate: '2024-01-01T00:00:00Z'
  },
  {
    id: '2',
    date: '2024-01-19T00:00:00Z',
    usage: 7200,
    cost: 7.20,
    resetDate: '2024-01-01T00:00:00Z'
  },
  {
    id: '3',
    date: '2024-01-18T00:00:00Z',
    usage: 6800,
    cost: 6.80,
    resetDate: '2024-01-01T00:00:00Z'
  }
])

// Tabs
const tabs = [
  { id: 'usage', name: 'Usage', icon: 'ic:baseline-data-usage' },
  { id: 'history', name: 'History', icon: 'ic:baseline-history' },
  { id: 'settings', name: 'Settings', icon: 'ic:baseline-settings' }
]

// Methods
const getEntitlementIcon = (category) => {
  const iconMap = {
    'MESSAGING': 'ic:baseline-chat',
    'API': 'ic:baseline-code',
    'STORAGE': 'ic:baseline-cloud',
    'USERS': 'ic:baseline-people',
    'BOTS': 'ic:baseline-robot'
  }
  return iconMap[category] || 'ic:baseline-settings'
}

const getEntitlementIconClass = (category) => {
  const classMap = {
    'MESSAGING': 'bg-blue-100 text-blue-600 dark:bg-blue-900/20 dark:text-blue-400',
    'API': 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400',
    'STORAGE': 'bg-purple-100 text-purple-600 dark:bg-purple-900/20 dark:text-purple-400',
    'USERS': 'bg-orange-100 text-orange-600 dark:bg-orange-900/20 dark:text-orange-400',
    'BOTS': 'bg-pink-100 text-pink-600 dark:bg-pink-900/20 dark:text-pink-400'
  }
  return classMap[category] || 'bg-gray-100 text-gray-600'
}

const getStatusClass = (entitlement) => {
  const usagePercentage = (entitlement.currentUsage / entitlement.limit) * 100
  
  if (usagePercentage >= 100) return 'bg-red-100 text-red-600 dark:bg-red-900/20 dark:text-red-400'
  if (usagePercentage >= 90) return 'bg-orange-100 text-orange-600 dark:bg-orange-900/20 dark:text-orange-400'
  if (usagePercentage >= 75) return 'bg-yellow-100 text-yellow-600 dark:bg-yellow-900/20 dark:text-yellow-400'
  return 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400'
}

const getStatusText = (entitlement) => {
  const usagePercentage = (entitlement.currentUsage / entitlement.limit) * 100
  
  if (usagePercentage >= 100) return 'Exceeded'
  if (usagePercentage >= 90) return 'Critical'
  if (usagePercentage >= 75) return 'Warning'
  return 'Healthy'
}

const getUsagePercentage = (entitlement) => {
  return Math.round((entitlement.currentUsage / entitlement.limit) * 100)
}

const getUsageBarClass = (entitlement) => {
  const percentage = getUsagePercentage(entitlement)
  
  if (percentage >= 100) return 'bg-red-600'
  if (percentage >= 90) return 'bg-orange-600'
  if (percentage >= 75) return 'bg-yellow-600'
  return 'bg-green-600'
}

const getRemainingClass = (entitlement) => {
  const remaining = entitlement.limit - entitlement.currentUsage
  
  if (remaining <= 0) return 'text-red-600 dark:text-red-400'
  if (remaining <= entitlement.limit * 0.1) return 'text-orange-600 dark:text-orange-400'
  if (remaining <= entitlement.limit * 0.25) return 'text-yellow-600 dark:text-yellow-400'
  return 'text-green-600 dark:text-green-400'
}

const formatNumber = (num) => {
  if (!num) return '0'
  return new Intl.NumberFormat('en-US').format(num)
}

const formatCurrency = (amount) => {
  if (!amount) return '0.00'
  return new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(amount)
}

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

const saveSettings = async () => {
  isSaving.value = true
  
  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // Update entitlement with new settings
    const updatedEntitlement = {
      ...props.entitlement,
      settings: settings.value,
      updatedAt: new Date().toISOString()
    }
    
    emit('upgraded', updatedEntitlement)
    
  } catch (error) {
    console.error('Failed to save settings:', error)
  } finally {
    isSaving.value = false
  }
}
</script>

<style scoped>
.modal-backdrop {
  backdrop-filter: blur(4px);
}
</style>
