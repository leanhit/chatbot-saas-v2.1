<template>
  <div class="subscriptions-manager">
    <!-- Header -->
    <div class="flex justify-between items-center mb-6">
      <div>
        <h2 class="text-xl font-semibold text-gray-900 dark:text-gray-100">
          Subscriptions
        </h2>
        <p class="text-gray-600 dark:text-gray-400">
          Manage your active and inactive subscriptions
        </p>
      </div>
      <button 
        @click="showAddSubscription = true"
        class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center gap-2"
      >
        <Icon icon="ic:baseline-add" />
        Add Subscription
      </button>
    </div>

    <!-- Filter Tabs -->
    <div class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 mb-6">
      <div class="flex border-b border-gray-200 dark:border-gray-700">
        <button 
          v-for="tab in statusTabs" 
          :key="tab.key"
          @click="activeStatusTab = tab.key"
          :class="[
            'px-6 py-3 text-sm font-medium border-b-2 transition-colors',
            activeStatusTab === tab.key 
              ? 'border-blue-500 text-blue-600 dark:text-blue-400' 
              : 'border-transparent text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-200'
          ]"
        >
          {{ tab.label }}
          <span class="ml-2 bg-gray-100 dark:bg-gray-700 px-2 py-0.5 rounded-full text-xs">
            {{ tab.count }}
          </span>
        </button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="isLoading" class="text-center py-12">
      <Icon icon="ic:baseline-refresh" class="animate-spin text-4xl text-gray-400" />
      <p class="text-gray-600 dark:text-gray-400 mt-2">Loading subscriptions...</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="filteredSubscriptions.length === 0" class="text-center py-12">
      <Icon icon="ic:baseline-subscriptions" class="text-4xl text-gray-400" />
      <h3 class="text-lg font-medium text-gray-900 dark:text-gray-100 mt-4">
        No {{ activeStatusTab }} subscriptions
      </h3>
      <p class="text-gray-600 dark:text-gray-400 mt-2">
        {{ getEmptyStateMessage() }}
      </p>
      <button 
        v-if="activeStatusTab === 'ACTIVE'"
        @click="showAddSubscription = true"
        class="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
      >
        Browse Plans
      </button>
    </div>

    <!-- Subscriptions Grid -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div 
        v-for="subscription in filteredSubscriptions" 
        :key="subscription.id"
        class="subscription-card bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 hover:shadow-lg transition-shadow"
      >
        <!-- Card Header -->
        <div class="p-6 border-b border-gray-200 dark:border-gray-700">
          <div class="flex justify-between items-start mb-4">
            <div class="flex items-center gap-3">
              <div :class="getPlanIconClass(subscription.plan)" class="p-3 rounded-lg">
                <Icon :icon="getPlanIcon(subscription.plan)" class="text-2xl" />
              </div>
              <div>
                <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100">
                  {{ subscription.plan }}
                </h3>
                <p class="text-sm text-gray-600 dark:text-gray-400">
                  {{ subscription.description || 'Standard subscription plan' }}
                </p>
              </div>
            </div>
            <span :class="getStatusClass(subscription.status)" class="px-2 py-1 rounded-full text-xs font-medium">
              {{ subscription.status }}
            </span>
          </div>

          <!-- Price Info -->
          <div class="flex justify-between items-center">
            <div>
              <p class="text-2xl font-bold text-gray-900 dark:text-gray-100">
                ${{ subscription.monthlyPrice || 0 }}
              </p>
              <p class="text-sm text-gray-600 dark:text-gray-400">per month</p>
            </div>
            <div v-if="subscription.annualPrice && subscription.annualPrice < subscription.monthlyPrice * 12" class="text-right">
              <p class="text-xs text-green-600 dark:text-green-400 font-medium">
                Save 20%
              </p>
              <p class="text-sm text-gray-600 dark:text-gray-400">
                ${{ subscription.annualPrice }}/year
              </p>
            </div>
          </div>
        </div>

        <!-- Card Body -->
        <div class="p-6">
          <!-- Features -->
          <div class="mb-6">
            <h4 class="text-sm font-medium text-gray-900 dark:text-gray-100 mb-3">Features</h4>
            <div class="space-y-2">
              <div 
                v-for="feature in subscription.features" 
                :key="feature.name"
                class="flex items-center gap-2 text-sm"
              >
                <Icon 
                  :icon="feature.included ? 'ic:baseline-check-circle' : 'ic:baseline-cancel'" 
                  :class="feature.included ? 'text-green-600 dark:text-green-400' : 'text-gray-400'"
                />
                <span :class="feature.included ? 'text-gray-900 dark:text-gray-100' : 'text-gray-400'">
                  {{ feature.name }}
                </span>
                <span v-if="feature.limit" class="text-xs text-gray-500 dark:text-gray-400">
                  ({{ feature.limit }})
                </span>
              </div>
            </div>
          </div>

          <!-- Billing Info -->
          <div class="space-y-2 text-sm text-gray-600 dark:text-gray-400 mb-6">
            <div class="flex justify-between">
              <span>Billing Cycle:</span>
              <span class="font-medium text-gray-900 dark:text-gray-100">
                {{ subscription.billingCycle || 'Monthly' }}
              </span>
            </div>
            <div class="flex justify-between">
              <span>Next Payment:</span>
              <span class="font-medium text-gray-900 dark:text-gray-100">
                {{ formatDate(subscription.nextBillingDate) }}
              </span>
            </div>
            <div class="flex justify-between">
              <span>Started:</span>
              <span class="font-medium text-gray-900 dark:text-gray-100">
                {{ formatDate(subscription.startDate) }}
              </span>
            </div>
          </div>

          <!-- Actions -->
          <div class="flex gap-2">
            <button 
              v-if="subscription.status === 'ACTIVE'"
              @click="manageSubscription(subscription)"
              class="flex-1 px-3 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors text-sm font-medium"
            >
              Manage
            </button>
            <button 
              v-if="subscription.status === 'ACTIVE'"
              @click="cancelSubscription(subscription)"
              class="flex-1 px-3 py-2 bg-red-100 dark:bg-red-900/20 text-red-600 dark:text-red-400 rounded-lg hover:bg-red-200 dark:hover:bg-red-900/30 transition-colors text-sm font-medium"
            >
              Cancel
            </button>
            <button 
              v-if="subscription.status === 'CANCELLED' || subscription.status === 'EXPIRED'"
              @click="reactivateSubscription(subscription)"
              class="flex-1 px-3 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors text-sm font-medium"
            >
              Reactivate
            </button>
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
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useBillingStore } from '@/stores/billing/billingStore'
import AddSubscriptionModal from './AddSubscriptionModal.vue'
import ManageSubscriptionModal from './ManageSubscriptionModal.vue'

const billingStore = useBillingStore()

// State
const activeStatusTab = ref('ACTIVE')
const showAddSubscription = ref(false)
const showManageModal = ref(false)
const selectedSubscription = ref(null)

// Status tabs with counts
const statusTabs = computed(() => [
  {
    key: 'ACTIVE',
    label: 'Active',
    count: billingStore.subscriptions.filter(s => s.status === 'ACTIVE').length
  },
  {
    key: 'TRIAL',
    label: 'Trial',
    count: billingStore.subscriptions.filter(s => s.status === 'TRIAL').length
  },
  {
    key: 'CANCELLED',
    label: 'Cancelled',
    count: billingStore.subscriptions.filter(s => s.status === 'CANCELLED').length
  },
  {
    key: 'EXPIRED',
    label: 'Expired',
    count: billingStore.subscriptions.filter(s => s.status === 'EXPIRED').length
  }
])

// Filtered subscriptions based on active tab
const filteredSubscriptions = computed(() => {
  return billingStore.subscriptions.filter(s => s.status === activeStatusTab.value)
})

const isLoading = computed(() => billingStore.isLoading)

// Methods
const getPlanIcon = (plan) => {
  const iconMap = {
    'FREE': 'ic:baseline-star',
    'STARTER': 'ic:baseline-rocket-launch',
    'PROFESSIONAL': 'ic:baseline-business',
    'ENTERPRISE': 'ic:baseline-corporate-fare',
    'CUSTOM': 'ic:baseline-settings'
  }
  return iconMap[plan?.toUpperCase()] || 'ic:baseline-subscriptions'
}

const getPlanIconClass = (plan) => {
  const classMap = {
    'FREE': 'bg-gray-100 text-gray-600 dark:bg-gray-900/20 dark:text-gray-400',
    'STARTER': 'bg-blue-100 text-blue-600 dark:bg-blue-900/20 dark:text-blue-400',
    'PROFESSIONAL': 'bg-purple-100 text-purple-600 dark:bg-purple-900/20 dark:text-purple-400',
    'ENTERPRISE': 'bg-orange-100 text-orange-600 dark:bg-orange-900/20 dark:text-orange-400',
    'CUSTOM': 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400'
  }
  return classMap[plan?.toUpperCase()] || 'bg-gray-100 text-gray-600'
}

const getStatusClass = (status) => {
  switch (status?.toUpperCase()) {
    case 'ACTIVE':
      return 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400'
    case 'TRIAL':
      return 'bg-blue-100 text-blue-600 dark:bg-blue-900/20 dark:text-blue-400'
    case 'CANCELLED':
      return 'bg-gray-100 text-gray-600 dark:bg-gray-900/20 dark:text-gray-400'
    case 'EXPIRED':
      return 'bg-red-100 text-red-600 dark:bg-red-900/20 dark:text-red-400'
    case 'SUSPENDED':
      return 'bg-yellow-100 text-yellow-600 dark:bg-yellow-900/20 dark:text-yellow-400'
    default:
      return 'bg-gray-100 text-gray-600 dark:bg-gray-900/20 dark:text-gray-400'
  }
}

const getEmptyStateMessage = () => {
  switch (activeStatusTab.value) {
    case 'ACTIVE':
      return 'You don\'t have any active subscriptions. Browse our plans to get started.'
    case 'TRIAL':
      return 'You don\'t have any trial subscriptions.'
    case 'CANCELLED':
      return 'You don\'t have any cancelled subscriptions.'
    case 'EXPIRED':
      return 'You don\'t have any expired subscriptions.'
    default:
      return 'No subscriptions found.'
  }
}

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

const manageSubscription = (subscription) => {
  selectedSubscription.value = subscription
  showManageModal.value = true
}

const cancelSubscription = async (subscription) => {
  if (!confirm(`Are you sure you want to cancel your ${subscription.plan} subscription?`)) {
    return
  }

  try {
    await billingStore.cancelSubscription(subscription.id)
    // Show success message
    console.log('Subscription cancelled successfully')
  } catch (error) {
    console.error('Failed to cancel subscription:', error)
    // Show error message
  }
}

const reactivateSubscription = async (subscription) => {
  try {
    await billingStore.reactivateSubscription(subscription.id)
    // Show success message
    console.log('Subscription reactivated successfully')
  } catch (error) {
    console.error('Failed to reactivate subscription:', error)
    // Show error message
  }
}

const onSubscriptionAdded = (newSubscription) => {
  showAddSubscription.value = false
  // Refresh subscriptions
  billingStore.fetchTenantBilling(getCurrentTenantId())
}

const onSubscriptionUpdated = (updatedSubscription) => {
  showManageModal.value = false
  // Refresh subscriptions
  billingStore.fetchTenantBilling(getCurrentTenantId())
}

const getCurrentTenantId = () => {
  // Get current tenant ID from store or route
  return localStorage.getItem('ACTIVE_TENANT_ID')
}

// Lifecycle
onMounted(() => {
  // Load subscriptions if not already loaded
  if (billingStore.subscriptions.length === 0) {
    billingStore.fetchTenantBilling(getCurrentTenantId())
  }
})
</script>

<style scoped>
.subscription-card {
  transition: all 0.2s ease-in-out;
}

.subscription-card:hover {
  transform: translateY(-2px);
}
</style>
