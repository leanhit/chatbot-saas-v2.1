<template>
  <div class="subscriptions-page p-6">
    <!-- Header -->
    <div class="mb-6">
      <div class="flex justify-between items-center">
        <div>
          <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">
            Subscriptions
          </h1>
          <p class="text-gray-600 dark:text-gray-400">
            Manage your active and inactive subscriptions
          </p>
        </div>
        
        <div class="flex gap-3">
          <button 
            @click="showAddSubscription = true"
            class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center gap-2"
          >
            <Icon icon="ic:baseline-add" />
            Add Subscription
          </button>
          <button 
            @click="refreshSubscriptions"
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

    <!-- Filters -->
    <div class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-6 mb-6">
      <div class="flex flex-col lg:flex-row gap-4">
        <div class="flex-1">
          <div class="relative">
            <Icon icon="ic:baseline-search" class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
            <input 
              v-model="searchQuery"
              type="text"
              placeholder="Search subscriptions..."
              class="w-full pl-10 pr-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
        </div>

        <select 
          v-model="statusFilter"
          class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        >
          <option value="">All Status</option>
          <option value="ACTIVE">Active</option>
          <option value="PENDING">Pending</option>
          <option value="CANCELLED">Cancelled</option>
          <option value="EXPIRED">Expired</option>
        </select>

        <select 
          v-model="typeFilter"
          class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        >
          <option value="">All Types</option>
          <option value="BASIC">Basic</option>
          <option value="PROFESSIONAL">Professional</option>
          <option value="ENTERPRISE">Enterprise</option>
          <option value="API">API Access</option>
          <option value="STORAGE">Storage</option>
          <option value="USERS">Users</option>
        </select>

        <button 
          @click="resetFilters"
          class="px-4 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors"
        >
          Reset Filters
        </button>
      </div>
    </div>

    <!-- Subscriptions List -->
    <div v-if="isLoading" class="text-center py-12">
      <Icon icon="ic:baseline-refresh" class="animate-spin text-4xl text-gray-400" />
      <p class="text-gray-600 dark:text-gray-400 mt-2">Loading subscriptions...</p>
    </div>

    <div v-else-if="filteredSubscriptions.length === 0" class="text-center py-12">
      <Icon icon="ic:baseline-subscriptions" class="text-4xl text-gray-400" />
      <h3 class="text-lg font-medium text-gray-900 dark:text-gray-100 mt-4">
        No subscriptions found
      </h3>
      <p class="text-gray-600 dark:text-gray-400 mt-2">
        {{ searchQuery ? 'Try adjusting your search' : 'Get started by adding your first subscription' }}
      </p>
      <button 
        v-if="!searchQuery"
        @click="showAddSubscription = true"
        class="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
      >
        Add Your First Subscription
      </button>
    </div>

    <div v-else class="space-y-6">
      <div 
        v-for="subscription in filteredSubscriptions" 
        :key="subscription.id"
        class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 overflow-hidden"
      >
        <!-- Subscription Header -->
        <div class="p-6 border-b border-gray-200 dark:border-gray-700">
          <div class="flex justify-between items-start">
            <div class="flex items-center gap-4">
              <div :class="getSubscriptionIconClass(subscription.planType)" class="p-3 rounded-lg">
                <Icon :icon="getSubscriptionIcon(subscription.planType)" class="text-xl" />
              </div>
              <div>
                <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100">
                  {{ subscription.planName }}
                </h3>
                <p class="text-sm text-gray-600 dark:text-gray-400">
                  {{ subscription.description }}
                </p>
                <div class="flex items-center gap-2 mt-2">
                  <span :class="getStatusClass(subscription.status)" class="px-2 py-1 rounded-full text-xs font-medium">
                    {{ subscription.status }}
                  </span>
                  <span class="px-2 py-1 bg-gray-100 dark:bg-gray-700 rounded-full text-xs font-medium text-gray-600 dark:text-gray-400">
                    {{ subscription.billingCycle }}
                  </span>
                </div>
              </div>
            </div>
            
            <!-- Actions -->
            <div class="flex gap-2">
              <button 
                @click="editSubscription(subscription)"
                class="p-2 text-gray-600 dark:text-gray-400 hover:text-blue-600 dark:hover:text-blue-400 transition-colors"
                title="Edit Subscription"
              >
                <Icon icon="ic:baseline-edit" />
              </button>
              <button 
                @click="manageSubscription(subscription)"
                class="p-2 text-gray-600 dark:text-gray-400 hover:text-green-600 dark:hover:text-green-400 transition-colors"
                title="Manage Subscription"
              >
                <Icon icon="ic:baseline-settings" />
              </button>
              <button 
                v-if="subscription.status === 'ACTIVE'"
                @click="cancelSubscription(subscription)"
                class="p-2 text-gray-600 dark:text-gray-400 hover:text-red-600 dark:hover:text-red-400 transition-colors"
                title="Cancel Subscription"
              >
                <Icon icon="ic:baseline-cancel" />
              </button>
            </div>
          </div>
        </div>

        <!-- Subscription Details -->
        <div class="p-6">
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <!-- Pricing -->
            <div>
              <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Pricing</h4>
              <div class="space-y-1">
                <div class="flex justify-between text-sm">
                  <span class="text-gray-600 dark:text-gray-400">Amount:</span>
                  <span class="font-medium text-gray-900 dark:text-gray-100">
                    ${{ formatCurrency(subscription.amount) }}
                  </span>
                </div>
                <div class="flex justify-between text-sm">
                  <span class="text-gray-600 dark:text-gray-400">Billing Cycle:</span>
                  <span class="font-medium text-gray-900 dark:text-gray-100">
                    {{ subscription.billingCycle }}
                  </span>
                </div>
                <div class="flex justify-between text-sm">
                  <span class="text-gray-600 dark:text-gray-400">Next Payment:</span>
                  <span class="font-medium text-gray-900 dark:text-gray-100">
                    {{ formatDate(subscription.nextBillingDate) }}
                  </span>
                </div>
              </div>
            </div>

            <!-- Usage -->
            <div>
              <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Usage</h4>
              <div class="space-y-1">
                <div class="flex justify-between text-sm">
                  <span class="text-gray-600 dark:text-gray-400">Current Usage:</span>
                  <span class="font-medium text-gray-900 dark:text-gray-100">
                    {{ formatNumber(subscription.currentUsage) }} {{ subscription.unit }}
                  </span>
                </div>
                <div class="flex justify-between text-sm">
                  <span class="text-gray-600 dark:text-gray-400">Limit:</span>
                  <span class="font-medium text-gray-900 dark:text-gray-100">
                    {{ formatNumber(subscription.limit) }} {{ subscription.unit }}
                  </span>
                </div>
                <!-- Usage Meter -->
                <div class="mt-2">
                  <UsageMeter 
                    :used="subscription.currentUsage"
                    :limit="subscription.limit"
                    :unit="subscription.unit"
                    :height="6"
                    :show-percentage="false"
                    :show-labels="false"
                  />
                </div>
              </div>
            </div>

            <!-- Features -->
            <div>
              <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Features</h4>
              <ul class="space-y-1 text-sm">
                <li 
                  v-for="feature in subscription.features.slice(0, 3)" 
                  :key="feature"
                  class="flex items-center gap-1 text-gray-600 dark:text-gray-400"
                >
                  <Icon icon="ic:baseline-check" class="text-green-500" />
                  {{ feature }}
                </li>
                <li v-if="subscription.features.length > 3" class="text-gray-500 dark:text-gray-400">
                  +{{ subscription.features.length - 3 }} more features
                </li>
              </ul>
            </div>

            <!-- Dates -->
            <div>
              <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Important Dates</h4>
              <div class="space-y-1 text-sm">
                <div class="flex justify-between">
                  <span class="text-gray-600 dark:text-gray-400">Started:</span>
                  <span class="font-medium text-gray-900 dark:text-gray-100">
                    {{ formatDate(subscription.startDate) }}
                  </span>
                </div>
                <div class="flex justify-between">
                  <span class="text-gray-600 dark:text-gray-400">Renews:</span>
                  <span class="font-medium text-gray-900 dark:text-gray-100">
                    {{ formatDate(subscription.nextBillingDate) }}
                  </span>
                </div>
                <div v-if="subscription.endDate" class="flex justify-between">
                  <span class="text-gray-600 dark:text-gray-400">Ends:</span>
                  <span class="font-medium text-gray-900 dark:text-gray-100">
                    {{ formatDate(subscription.endDate) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Add Subscription Modal -->
    <AddSubscriptionModal 
      v-if="showAddSubscription"
      @close="showAddSubscription = false"
      @added="onSubscriptionAdded"
    />

    <!-- Manage Subscription Modal -->
    <ManageSubscriptionModal 
      v-if="showManageModal"
      :subscription="selectedSubscription"
      @close="showManageModal = false"
      @updated="onSubscriptionUpdated"
      @cancelled="onSubscriptionCancelled"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useBillingStore } from '@/stores/billing/billingStore'
import AddSubscriptionModal from '@/components/billing/AddSubscriptionModal.vue'
import ManageSubscriptionModal from '@/components/billing/ManageSubscriptionModal.vue'
import UsageMeter from '@/components/billing/UsageMeter.vue'

const billingStore = useBillingStore()

// State
const isLoading = ref(false)
const searchQuery = ref('')
const statusFilter = ref('')
const typeFilter = ref('')
const showAddSubscription = ref(false)
const showManageModal = ref(false)
const selectedSubscription = ref(null)

// Mock subscriptions data
const subscriptions = ref([
  {
    id: '1',
    planName: 'Professional Plan',
    planType: 'PROFESSIONAL',
    description: 'Advanced features for growing teams',
    amount: 99.99,
    billingCycle: 'Monthly',
    status: 'ACTIVE',
    currentUsage: 8500,
    limit: 10000,
    unit: 'API calls',
    features: ['Unlimited Users', 'Advanced Analytics', 'Priority Support', 'API Access', 'Custom Integrations'],
    startDate: '2024-01-01T00:00:00Z',
    nextBillingDate: '2024-02-01T00:00:00Z',
    endDate: null
  },
  {
    id: '2',
    planName: 'Storage Plus',
    planType: 'STORAGE',
    description: 'Additional cloud storage',
    amount: 29.99,
    billingCycle: 'Monthly',
    status: 'ACTIVE',
    currentUsage: 75,
    limit: 100,
    unit: 'GB',
    features: ['100GB Storage', 'Automatic Backup', 'File Sharing', 'Version History'],
    startDate: '2024-01-15T00:00:00Z',
    nextBillingDate: '2024-02-15T00:00:00Z',
    endDate: null
  },
  {
    id: '3',
    planName: 'Basic Plan',
    planType: 'BASIC',
    description: 'Essential features for small teams',
    amount: 29.99,
    billingCycle: 'Monthly',
    status: 'CANCELLED',
    currentUsage: 0,
    limit: 1000,
    unit: 'API calls',
    features: ['5 Users', 'Basic Analytics', 'Email Support'],
    startDate: '2023-12-01T00:00:00Z',
    nextBillingDate: null,
    endDate: '2024-01-01T00:00:00Z'
  }
])

// Computed
const filteredSubscriptions = computed(() => {
  let filtered = subscriptions.value

  // Apply search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(sub => 
      sub.planName.toLowerCase().includes(query) ||
      sub.description.toLowerCase().includes(query) ||
      sub.planType.toLowerCase().includes(query)
    )
  }

  // Apply status filter
  if (statusFilter.value) {
    filtered = filtered.filter(sub => sub.status === statusFilter.value)
  }

  // Apply type filter
  if (typeFilter.value) {
    filtered = filtered.filter(sub => sub.planType === typeFilter.value)
  }

  return filtered.sort((a, b) => new Date(b.startDate) - new Date(a.startDate))
})

// Methods
const getSubscriptionIcon = (planType) => {
  const iconMap = {
    'BASIC': 'ic:baseline-star',
    'PROFESSIONAL': 'ic:baseline-star',
    'ENTERPRISE': 'ic:baseline-star',
    'API': 'ic:baseline-code',
    'STORAGE': 'ic:baseline-cloud',
    'USERS': 'ic:baseline-people'
  }
  return iconMap[planType] || 'ic:baseline-subscriptions'
}

const getSubscriptionIconClass = (planType) => {
  const classMap = {
    'BASIC': 'bg-gray-100 text-gray-600 dark:bg-gray-900/20 dark:text-gray-400',
    'PROFESSIONAL': 'bg-blue-100 text-blue-600 dark:bg-blue-900/20 dark:text-blue-400',
    'ENTERPRISE': 'bg-purple-100 text-purple-600 dark:bg-purple-900/20 dark:text-purple-400',
    'API': 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400',
    'STORAGE': 'bg-yellow-100 text-yellow-600 dark:bg-yellow-900/20 dark:text-yellow-400',
    'USERS': 'bg-orange-100 text-orange-600 dark:bg-orange-900/20 dark:text-orange-400'
  }
  return classMap[planType] || 'bg-gray-100 text-gray-600'
}

const getStatusClass = (status) => {
  switch (status?.toUpperCase()) {
    case 'ACTIVE':
      return 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400'
    case 'PENDING':
      return 'bg-yellow-100 text-yellow-600 dark:bg-yellow-900/20 dark:text-yellow-400'
    case 'CANCELLED':
      return 'bg-red-100 text-red-600 dark:bg-red-900/20 dark:text-red-400'
    case 'EXPIRED':
      return 'bg-gray-100 text-gray-600 dark:bg-gray-900/20 dark:text-gray-400'
    default:
      return 'bg-gray-100 text-gray-600'
  }
}

const formatCurrency = (amount) => {
  if (!amount) return '0.00'
  return new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(amount)
}

const formatNumber = (num) => {
  if (!num) return '0'
  return new Intl.NumberFormat('en-US').format(num)
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
  statusFilter.value = ''
  typeFilter.value = ''
}

const refreshSubscriptions = async () => {
  isLoading.value = true
  
  try {
    await billingStore.fetchSubscriptions()
    // Update local state with store data
    // subscriptions.value = billingStore.subscriptions
  } catch (error) {
    console.error('Failed to refresh subscriptions:', error)
  } finally {
    isLoading.value = false
  }
}

const editSubscription = (subscription) => {
  selectedSubscription.value = subscription
  showManageModal.value = true
}

const manageSubscription = (subscription) => {
  selectedSubscription.value = subscription
  showManageModal.value = true
}

const cancelSubscription = (subscription) => {
  if (confirm(`Are you sure you want to cancel ${subscription.planName}? This action cannot be undone.`)) {
    // Implement cancellation logic
    console.log('Cancel subscription:', subscription)
  }
}

const onSubscriptionAdded = (newSubscription) => {
  showAddSubscription.value = false
  subscriptions.value.unshift(newSubscription)
  refreshSubscriptions()
}

const onSubscriptionUpdated = (updatedSubscription) => {
  showManageModal.value = false
  selectedSubscription.value = null
  
  // Update in local state
  const index = subscriptions.value.findIndex(sub => sub.id === updatedSubscription.id)
  if (index !== -1) {
    subscriptions.value[index] = updatedSubscription
  }
}

const onSubscriptionCancelled = (cancelledSubscription) => {
  showManageModal.value = false
  selectedSubscription.value = null
  
  // Update in local state
  const index = subscriptions.value.findIndex(sub => sub.id === cancelledSubscription.id)
  if (index !== -1) {
    subscriptions.value[index] = cancelledSubscription
  }
}

// Lifecycle
onMounted(() => {
  refreshSubscriptions()
})
</script>

<style scoped>
.subscriptions-page {
  max-width: 1400px;
  margin: 0 auto;
}
</style>
