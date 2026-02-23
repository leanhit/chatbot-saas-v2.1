<template>
  <div class="entitlements-page p-6">
    <!-- Header -->
    <div class="mb-6">
      <div class="flex justify-between items-center">
        <div>
          <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">
            Usage & Entitlements
          </h1>
          <p class="text-gray-600 dark:text-gray-400">
            Monitor your feature usage and entitlements
          </p>
        </div>
        
        <div class="flex gap-3">
          <button 
            @click="exportUsage"
            class="px-4 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors flex items-center gap-2"
          >
            <Icon icon="ic:baseline-file-download" />
            Export Usage
          </button>
          <button 
            @click="refreshEntitlements"
            :disabled="isLoading"
            class="px-4 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors flex items-center gap-2"
          >
            <Icon v-if="!isLoading" icon="ic:baseline-refresh" />
            <Icon v-else icon="ic:baseline-refresh" class="animate-spin" />
            Refresh
          </button>
        </div>
      </div>
    </div>

    <!-- Usage Overview Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700">
        <div class="flex items-center gap-3">
          <div class="p-3 bg-blue-100 dark:bg-blue-900/20 rounded-lg">
            <Icon icon="ic:baseline-data-usage" class="text-blue-600 dark:text-blue-400 text-2xl" />
          </div>
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Total Features</p>
            <p class="text-2xl font-bold text-gray-900 dark:text-gray-100">{{ totalEntitlements }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700">
        <div class="flex items-center gap-3">
          <div class="p-3 bg-green-100 dark:bg-green-900/20 rounded-lg">
            <Icon icon="ic:baseline-check-circle" class="text-green-600 dark:text-green-400 text-2xl" />
          </div>
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Active Entitlements</p>
            <p class="text-2xl font-bold text-green-600 dark:text-green-400">{{ activeEntitlements }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700">
        <div class="flex items-center gap-3">
          <div class="p-3 bg-yellow-100 dark:bg-yellow-900/20 rounded-lg">
            <Icon icon="ic:baseline-warning" class="text-yellow-600 dark:text-yellow-400 text-2xl" />
          </div>
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Near Limits</p>
            <p class="text-2xl font-bold text-yellow-600 dark:text-yellow-400">{{ nearLimits }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700">
        <div class="flex items-center gap-3">
          <div class="p-3 bg-red-100 dark:bg-red-900/20 rounded-lg">
            <Icon icon="ic:baseline-error" class="text-red-600 dark:text-red-400 text-2xl" />
          </div>
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Exceeded</p>
            <p class="text-2xl font-bold text-red-600 dark:text-red-400">{{ exceededEntitlements }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Filters and Search -->
    <div class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-6 mb-6">
      <div class="flex flex-col lg:flex-row gap-4">
        <!-- Search -->
        <div class="flex-1">
          <div class="relative">
            <Icon icon="ic:baseline-search" class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
            <input 
              v-model="searchQuery"
              type="text"
              placeholder="Search entitlements..."
              class="w-full pl-10 pr-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
        </div>

        <!-- Category Filter -->
        <select 
          v-model="selectedCategory"
          class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        >
          <option value="">All Categories</option>
          <option value="MESSAGING">Messaging</option>
          <option value="API">API Calls</option>
          <option value="STORAGE">Storage</option>
          <option value="USERS">Users</option>
          <option value="BOTS">Bots</option>
        </select>

        <!-- Status Filter -->
        <select 
          v-model="selectedStatus"
          class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        >
          <option value="">All Status</option>
          <option value="HEALTHY">Healthy</option>
          <option value="WARNING">Warning</option>
          <option value="CRITICAL">Critical</option>
          <option value="EXCEEDED">Exceeded</option>
        </select>

        <!-- Reset Filters -->
        <button 
          @click="resetFilters"
          class="px-4 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors"
        >
          Reset Filters
        </button>
      </div>
    </div>

    <!-- Entitlements Grid -->
    <div v-if="isLoading" class="text-center py-12">
      <Icon icon="ic:baseline-refresh" class="animate-spin text-4xl text-gray-400" />
      <p class="text-gray-600 dark:text-gray-400 mt-2">Loading entitlements...</p>
    </div>

    <div v-else-if="filteredEntitlements.length === 0" class="text-center py-12">
      <Icon icon="ic:baseline-data-usage" class="text-4xl text-gray-400" />
      <h3 class="text-lg font-medium text-gray-900 dark:text-gray-100 mt-4">
        No entitlements found
      </h3>
      <p class="text-gray-600 dark:text-gray-400 mt-2">
        {{ searchQuery ? 'Try adjusting your search' : 'No entitlements available' }}
      </p>
    </div>

    <div v-else class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div 
        v-for="entitlement in filteredEntitlements" 
        :key="entitlement.id"
        class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 overflow-hidden hover:shadow-lg transition-shadow"
      >
        <!-- Entitlement Header -->
        <div class="p-6 border-b border-gray-200 dark:border-gray-700">
          <div class="flex justify-between items-start">
            <div class="flex items-center gap-3">
              <div :class="getEntitlementIconClass(entitlement.category)" class="p-2 rounded-lg">
                <Icon :icon="getEntitlementIcon(entitlement.category)" class="text-xl" />
              </div>
              <div>
                <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100">
                  {{ entitlement.featureName }}
                </h3>
                <p class="text-sm text-gray-600 dark:text-gray-400">
                  {{ entitlement.description }}
                </p>
              </div>
            </div>
            
            <!-- Status Badge -->
            <span :class="getStatusClass(entitlement)" class="px-2 py-1 rounded-full text-xs font-medium">
              {{ getStatusText(entitlement) }}
            </span>
          </div>
        </div>

        <!-- Entitlement Details -->
        <div class="p-6">
          <!-- Usage Overview -->
          <div class="mb-6">
            <div class="flex justify-between items-center mb-3">
              <span class="text-sm text-gray-600 dark:text-gray-400">Current Usage</span>
              <span class="text-sm font-medium text-gray-900 dark:text-gray-100">
                {{ formatNumber(entitlement.currentUsage) }} / {{ formatNumber(entitlement.limit) }} {{ entitlement.unit }}
              </span>
            </div>
            
            <!-- Usage Meter -->
            <UsageMeter 
              :used="entitlement.currentUsage"
              :limit="entitlement.limit"
              :unit="entitlement.unit"
              :show-percentage="true"
              :show-labels="true"
              :show-status="true"
            />
          </div>

          <!-- Additional Information -->
          <div class="grid grid-cols-2 gap-4 mb-6 text-sm">
            <div>
              <span class="text-gray-600 dark:text-gray-400">Cost per Unit:</span>
              <span class="font-medium text-gray-900 dark:text-gray-100 ml-2">
                ${{ formatCurrency(entitlement.costPerUnit) }}
              </span>
            </div>
            <div>
              <span class="text-gray-600 dark:text-gray-400">Monthly Cost:</span>
              <span class="font-medium text-gray-900 dark:text-gray-100 ml-2">
                ${{ formatCurrency(entitlement.monthlyCost) }}
              </span>
            </div>
            <div>
              <span class="text-gray-600 dark:text-gray-400">Last Reset:</span>
              <span class="font-medium text-gray-900 dark:text-gray-100 ml-2">
                {{ formatDate(entitlement.lastReset) }}
              </span>
            </div>
            <div>
              <span class="text-gray-600 dark:text-gray-400">Next Reset:</span>
              <span class="font-medium text-gray-900 dark:text-gray-100 ml-2">
                {{ formatDate(entitlement.nextReset) }}
              </span>
            </div>
          </div>

          <!-- Actions -->
          <div class="flex gap-2">
            <button 
              @click="viewDetails(entitlement)"
              class="flex-1 px-3 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center justify-center gap-2"
            >
              <Icon icon="ic:baseline-visibility" />
              View Details
            </button>
            <button 
              @click="upgradeEntitlement(entitlement)"
              class="flex-1 px-3 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors flex items-center justify-center gap-2"
            >
              <Icon icon="ic:baseline-arrow-upward" />
              Upgrade
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Entitlement Details Modal -->
    <EntitlementDetailsModal 
      v-if="showDetailsModal"
      :entitlement="selectedEntitlement"
      @close="showDetailsModal = false"
      @upgraded="onEntitlementUpgraded"
    />

    <!-- Upgrade Modal -->
    <UpgradeEntitlementModal 
      v-if="showUpgradeModal"
      :entitlement="selectedEntitlement"
      @close="showUpgradeModal = false"
      @upgraded="onEntitlementUpgraded"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useBillingStore } from '@/stores/billing/billingStore'
import UsageMeter from '@/components/billing/UsageMeter.vue'
import EntitlementDetailsModal from '@/components/billing/EntitlementDetailsModal.vue'
import UpgradeEntitlementModal from '@/components/billing/UpgradeEntitlementModal.vue'

const billingStore = useBillingStore()

// State
const isLoading = ref(false)
const searchQuery = ref('')
const selectedCategory = ref('')
const selectedStatus = ref('')
const showDetailsModal = ref(false)
const showUpgradeModal = ref(false)
const selectedEntitlement = ref(null)

// Mock entitlements data
const entitlements = ref([
  {
    id: '1',
    featureName: 'API Calls',
    description: 'Number of API requests per month',
    category: 'API',
    currentUsage: 8500,
    limit: 10000,
    unit: 'calls',
    costPerUnit: 0.001,
    monthlyCost: 10.00,
    lastReset: '2024-01-01T00:00:00Z',
    nextReset: '2024-02-01T00:00:00Z',
    resetPeriod: 'MONTHLY'
  },
  {
    id: '2',
    featureName: 'Storage',
    description: 'Cloud storage space for files and data',
    category: 'STORAGE',
    currentUsage: 75,
    limit: 100,
    unit: 'GB',
    costPerUnit: 0.10,
    monthlyCost: 10.00,
    lastReset: '2024-01-01T00:00:00Z',
    nextReset: '2024-02-01T00:00:00Z',
    resetPeriod: 'MONTHLY'
  },
  {
    id: '3',
    featureName: 'Team Members',
    description: 'Number of active team members',
    category: 'USERS',
    currentUsage: 8,
    limit: 10,
    unit: 'users',
    costPerUnit: 5.00,
    monthlyCost: 40.00,
    lastReset: '2024-01-01T00:00:00Z',
    nextReset: '2024-02-01T00:00:00Z',
    resetPeriod: 'MONTHLY'
  },
  {
    id: '4',
    featureName: 'Penny Bots',
    description: 'Number of active Penny bots',
    category: 'BOTS',
    currentUsage: 5,
    limit: 5,
    unit: 'bots',
    costPerUnit: 20.00,
    monthlyCost: 100.00,
    lastReset: '2024-01-01T00:00:00Z',
    nextReset: '2024-02-01T00:00:00Z',
    resetPeriod: 'MONTHLY'
  },
  {
    id: '5',
    featureName: 'Messages',
    description: 'Monthly message limit for all bots',
    category: 'MESSAGING',
    currentUsage: 9500,
    limit: 10000,
    unit: 'messages',
    costPerUnit: 0.0001,
    monthlyCost: 1.00,
    lastReset: '2024-01-01T00:00:00Z',
    nextReset: '2024-02-01T00:00:00Z',
    resetPeriod: 'MONTHLY'
  }
])

// Computed
const filteredEntitlements = computed(() => {
  let filtered = entitlements.value

  // Apply search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(entitlement => 
      entitlement.featureName.toLowerCase().includes(query) ||
      entitlement.description.toLowerCase().includes(query) ||
      entitlement.category.toLowerCase().includes(query)
    )
  }

  // Apply category filter
  if (selectedCategory.value) {
    filtered = filtered.filter(entitlement => entitlement.category === selectedCategory.value)
  }

  // Apply status filter
  if (selectedStatus.value) {
    filtered = filtered.filter(entitlement => {
      const status = getEntitlementStatus(entitlement)
      return status === selectedStatus.value
    })
  }

  return filtered
})

const totalEntitlements = computed(() => entitlements.value.length)
const activeEntitlements = computed(() => 
  entitlements.value.filter(e => getEntitlementStatus(e) === 'HEALTHY').length
)
const nearLimits = computed(() => 
  entitlements.value.filter(e => getEntitlementStatus(e) === 'WARNING').length
)
const exceededEntitlements = computed(() => 
  entitlements.value.filter(e => getEntitlementStatus(e) === 'EXCEEDED').length
)

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

const getEntitlementStatus = (entitlement) => {
  const usagePercentage = (entitlement.currentUsage / entitlement.limit) * 100
  
  if (usagePercentage >= 100) return 'EXCEEDED'
  if (usagePercentage >= 90) return 'CRITICAL'
  if (usagePercentage >= 75) return 'WARNING'
  return 'HEALTHY'
}

const getStatusClass = (entitlement) => {
  const status = getEntitlementStatus(entitlement)
  
  switch (status) {
    case 'HEALTHY':
      return 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400'
    case 'WARNING':
      return 'bg-yellow-100 text-yellow-600 dark:bg-yellow-900/20 dark:text-yellow-400'
    case 'CRITICAL':
      return 'bg-orange-100 text-orange-600 dark:bg-orange-900/20 dark:text-orange-400'
    case 'EXCEEDED':
      return 'bg-red-100 text-red-600 dark:bg-red-900/20 dark:text-red-400'
    default:
      return 'bg-gray-100 text-gray-600'
  }
}

const getStatusText = (entitlement) => {
  const status = getEntitlementStatus(entitlement)
  
  switch (status) {
    case 'HEALTHY':
      return 'Healthy'
    case 'WARNING':
      return 'Warning'
    case 'CRITICAL':
      return 'Critical'
    case 'EXCEEDED':
      return 'Exceeded'
    default:
      return 'Unknown'
  }
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

const resetFilters = () => {
  searchQuery.value = ''
  selectedCategory.value = ''
  selectedStatus.value = ''
}

const refreshEntitlements = async () => {
  isLoading.value = true
  
  try {
    await billingStore.fetchEntitlements()
    // Update local state with store data
    // entitlements.value = billingStore.entitlements
  } catch (error) {
    console.error('Failed to refresh entitlements:', error)
  } finally {
    isLoading.value = false
  }
}

const viewDetails = (entitlement) => {
  selectedEntitlement.value = entitlement
  showDetailsModal.value = true
}

const upgradeEntitlement = (entitlement) => {
  selectedEntitlement.value = entitlement
  showUpgradeModal.value = true
}

const exportUsage = () => {
  const exportData = {
    entitlements: filteredEntitlements.value,
    exportedAt: new Date().toISOString(),
    filters: {
      searchQuery: searchQuery.value,
      selectedCategory: selectedCategory.value,
      selectedStatus: selectedStatus.value
    }
  }

  const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `usage-entitlements-${new Date().toISOString().split('T')[0]}.json`
  link.click()
  URL.revokeObjectURL(url)
}

const onEntitlementUpgraded = (updatedEntitlement) => {
  // Update local state
  const index = entitlements.value.findIndex(e => e.id === updatedEntitlement.id)
  if (index !== -1) {
    entitlements.value[index] = updatedEntitlement
  }
  
  showDetailsModal.value = false
  showUpgradeModal.value = false
}

// Lifecycle
onMounted(() => {
  refreshEntitlements()
})
</script>

<style scoped>
.entitlements-page {
  max-width: 1400px;
  margin: 0 auto;
}
</style>
