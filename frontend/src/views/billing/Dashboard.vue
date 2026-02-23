<template>
  <div class="billing-dashboard p-6">
    <!-- Header -->
    <div class="mb-6">
      <div class="flex justify-between items-center">
        <div>
          <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">
            Billing Dashboard
          </h1>
          <p class="text-gray-600 dark:text-gray-400">
            Manage your subscriptions, payments, and usage
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
            @click="refreshData"
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

    <!-- Overview Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700">
        <div class="flex items-center gap-3">
          <div class="p-3 bg-blue-100 dark:bg-blue-900/20 rounded-lg">
            <Icon icon="ic:baseline-subscriptions" class="text-blue-600 dark:text-blue-400 text-2xl" />
          </div>
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Active Subscriptions</p>
            <p class="text-2xl font-bold text-gray-900 dark:text-gray-100">{{ activeSubscriptions }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700">
        <div class="flex items-center gap-3">
          <div class="p-3 bg-green-100 dark:bg-green-900/20 rounded-lg">
            <Icon icon="ic:baseline-attach-money" class="text-green-600 dark:text-green-400 text-2xl" />
          </div>
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Monthly Spending</p>
            <p class="text-2xl font-bold text-gray-900 dark:text-gray-100">${{ formatCurrency(monthlySpending) }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700">
        <div class="flex items-center gap-3">
          <div class="p-3 bg-yellow-100 dark:bg-yellow-900/20 rounded-lg">
            <Icon icon="ic:baseline-receipt" class="text-yellow-600 dark:text-yellow-400 text-2xl" />
          </div>
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Pending Invoices</p>
            <p class="text-2xl font-bold text-gray-900 dark:text-gray-100">{{ pendingInvoices }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700">
        <div class="flex items-center gap-3">
          <div class="p-3 bg-purple-100 dark:bg-purple-900/20 rounded-lg">
            <Icon icon="ic:baseline-data-usage" class="text-purple-600 dark:text-purple-400 text-2xl" />
          </div>
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Usage Alerts</p>
            <p class="text-2xl font-bold text-gray-900 dark:text-gray-100">{{ usageAlerts }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <button 
        @click="navigateToSubscriptions"
        class="p-4 bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 hover:border-blue-500 hover:shadow-lg transition-all duration-200"
      >
        <div class="flex flex-col items-center text-center">
          <Icon icon="ic:baseline-subscriptions" class="text-3xl text-blue-600 dark:text-blue-400 mb-2" />
          <h3 class="font-semibold text-gray-900 dark:text-gray-100">Subscriptions</h3>
          <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">Manage your plans</p>
        </div>
      </button>

      <button 
        @click="navigateToInvoices"
        class="p-4 bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 hover:border-blue-500 hover:shadow-lg transition-all duration-200"
      >
        <div class="flex flex-col items-center text-center">
          <Icon icon="ic:baseline-receipt" class="text-3xl text-green-600 dark:text-green-400 mb-2" />
          <h3 class="font-semibold text-gray-900 dark:text-gray-100">Invoices</h3>
          <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">View billing history</p>
        </div>
      </button>

      <button 
        @click="navigateToPaymentMethods"
        class="p-4 bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 hover:border-blue-500 hover:shadow-lg transition-all duration-200"
      >
        <div class="flex flex-col items-center text-center">
          <Icon icon="ic:baseline-credit-card" class="text-3xl text-purple-600 dark:text-purple-400 mb-2" />
          <h3 class="font-semibold text-gray-900 dark:text-gray-100">Payment Methods</h3>
          <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">Manage payment options</p>
        </div>
      </button>

      <button 
        @click="navigateToEntitlements"
        class="p-4 bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 hover:border-blue-500 hover:shadow-lg transition-all duration-200"
      >
        <div class="flex flex-col items-center text-center">
          <Icon icon="ic:baseline-data-usage" class="text-3xl text-yellow-600 dark:text-yellow-400 mb-2" />
          <h3 class="font-semibold text-gray-900 dark:text-gray-100">Usage & Entitlements</h3>
          <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">Monitor usage limits</p>
        </div>
      </button>
    </div>

    <!-- Recent Activity -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- Recent Subscriptions -->
      <div class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700">
        <div class="p-6 border-b border-gray-200 dark:border-gray-700">
          <div class="flex justify-between items-center">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100">
              Recent Subscriptions
            </h3>
            <button 
              @click="navigateToSubscriptions"
              class="text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 text-sm"
            >
              View All
            </button>
          </div>
        </div>
        
        <div class="p-6">
          <div v-if="recentSubscriptions.length === 0" class="text-center py-8">
            <Icon icon="ic:baseline-subscriptions" class="text-4xl text-gray-400 mb-2" />
            <p class="text-gray-600 dark:text-gray-400">No recent subscriptions</p>
          </div>
          
          <div v-else class="space-y-4">
            <div 
              v-for="subscription in recentSubscriptions" 
              :key="subscription.id"
              class="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700 rounded-lg"
            >
              <div class="flex items-center gap-3">
                <div :class="getSubscriptionIconClass(subscription.planType)" class="p-2 rounded-lg">
                  <Icon :icon="getSubscriptionIcon(subscription.planType)" class="text-lg" />
                </div>
                <div>
                  <h4 class="font-medium text-gray-900 dark:text-gray-100">
                    {{ subscription.planName }}
                  </h4>
                  <p class="text-sm text-gray-600 dark:text-gray-400">
                    {{ formatDate(subscription.createdAt) }}
                  </p>
                </div>
              </div>
              
              <div class="text-right">
                <p class="font-semibold text-gray-900 dark:text-gray-100">
                  ${{ formatCurrency(subscription.amount) }}
                </p>
                <span :class="getStatusClass(subscription.status)" class="px-2 py-1 rounded-full text-xs font-medium">
                  {{ subscription.status }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Recent Invoices -->
      <div class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700">
        <div class="p-6 border-b border-gray-200 dark:border-gray-700">
          <div class="flex justify-between items-center">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100">
              Recent Invoices
            </h3>
            <button 
              @click="navigateToInvoices"
              class="text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 text-sm"
            >
              View All
            </button>
          </div>
        </div>
        
        <div class="p-6">
          <div v-if="recentInvoices.length === 0" class="text-center py-8">
            <Icon icon="ic:baseline-receipt" class="text-4xl text-gray-400 mb-2" />
            <p class="text-gray-600 dark:text-gray-400">No recent invoices</p>
          </div>
          
          <div v-else class="space-y-4">
            <div 
              v-for="invoice in recentInvoices" 
              :key="invoice.id"
              class="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700 rounded-lg"
            >
              <div class="flex items-center gap-3">
                <div :class="getInvoiceIconClass(invoice.status)" class="p-2 rounded-lg">
                  <Icon :icon="getInvoiceIcon(invoice.status)" class="text-lg" />
                </div>
                <div>
                  <h4 class="font-medium text-gray-900 dark:text-gray-100">
                    Invoice #{{ invoice.invoiceNumber }}
                  </h4>
                  <p class="text-sm text-gray-600 dark:text-gray-400">
                    {{ formatDate(invoice.dueDate) }}
                  </p>
                </div>
              </div>
              
              <div class="text-right">
                <p class="font-semibold text-gray-900 dark:text-gray-100">
                  ${{ formatCurrency(invoice.amount) }}
                </p>
                <span :class="getInvoiceStatusClass(invoice.status)" class="px-2 py-1 rounded-full text-xs font-medium">
                  {{ invoice.status }}
                </span>
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useBillingStore } from '@/stores/billing/billingStore'
import AddSubscriptionModal from '@/components/billing/AddSubscriptionModal.vue'

const router = useRouter()
const billingStore = useBillingStore()

// State
const isLoading = ref(false)
const showAddSubscription = ref(false)

// Mock data
const recentSubscriptions = ref([
  {
    id: '1',
    planName: 'Professional Plan',
    planType: 'PROFESSIONAL',
    amount: 99.99,
    status: 'ACTIVE',
    createdAt: '2024-01-15T10:30:00Z'
  },
  {
    id: '2',
    planName: 'API Access',
    planType: 'API',
    amount: 49.99,
    status: 'ACTIVE',
    createdAt: '2024-01-10T14:20:00Z'
  }
])

const recentInvoices = ref([
  {
    id: '1',
    invoiceNumber: 'INV-2024-001',
    amount: 149.98,
    status: 'PAID',
    dueDate: '2024-01-20T00:00:00Z'
  },
  {
    id: '2',
    invoiceNumber: 'INV-2024-002',
    amount: 149.98,
    status: 'PENDING',
    dueDate: '2024-02-20T00:00:00Z'
  }
])

// Computed
const activeSubscriptions = computed(() => {
  return billingStore.subscriptions?.filter(s => s.status === 'ACTIVE').length || recentSubscriptions.value.length
})

const monthlySpending = computed(() => {
  return billingStore.monthlySpending || 149.98
})

const pendingInvoices = computed(() => {
  return billingStore.invoices?.filter(i => i.status === 'PENDING').length || 1
})

const usageAlerts = computed(() => {
  return billingStore.usageAlerts || 2
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
    default:
      return 'bg-gray-100 text-gray-600'
  }
}

const getInvoiceIcon = (status) => {
  switch (status?.toUpperCase()) {
    case 'PAID':
      return 'ic:baseline-check-circle'
    case 'PENDING':
      return 'ic:baseline-schedule'
    case 'OVERDUE':
      return 'ic:baseline-warning'
    default:
      return 'ic:baseline-receipt'
  }
}

const getInvoiceIconClass = (status) => {
  switch (status?.toUpperCase()) {
    case 'PAID':
      return 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400'
    case 'PENDING':
      return 'bg-yellow-100 text-yellow-600 dark:bg-yellow-900/20 dark:text-yellow-400'
    case 'OVERDUE':
      return 'bg-red-100 text-red-600 dark:bg-red-900/20 dark:text-red-400'
    default:
      return 'bg-gray-100 text-gray-600'
  }
}

const getInvoiceStatusClass = (status) => {
  switch (status?.toUpperCase()) {
    case 'PAID':
      return 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400'
    case 'PENDING':
      return 'bg-yellow-100 text-yellow-600 dark:bg-yellow-900/20 dark:text-yellow-400'
    case 'OVERDUE':
      return 'bg-red-100 text-red-600 dark:bg-red-900/20 dark:text-red-400'
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

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

const refreshData = async () => {
  isLoading.value = true
  
  try {
    await billingStore.fetchSubscriptions()
    await billingStore.fetchInvoices()
    await billingStore.fetchUsageStats()
  } catch (error) {
    console.error('Failed to refresh billing data:', error)
  } finally {
    isLoading.value = false
  }
}

const navigateToSubscriptions = () => {
  router.push('/billing/subscriptions')
}

const navigateToInvoices = () => {
  router.push('/billing/invoices')
}

const navigateToPaymentMethods = () => {
  router.push('/billing/payment-methods')
}

const navigateToEntitlements = () => {
  router.push('/billing/entitlements')
}

const onSubscriptionAdded = (subscription) => {
  showAddSubscription.value = false
  recentSubscriptions.value.unshift(subscription)
  refreshData()
}

// Lifecycle
onMounted(() => {
  refreshData()
})
</script>

<style scoped>
.billing-dashboard {
  max-width: 1400px;
  margin: 0 auto;
}
</style>
