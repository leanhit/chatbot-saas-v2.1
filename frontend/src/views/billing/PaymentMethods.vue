<template>
  <div class="payment-methods-page p-6">
    <!-- Header -->
    <div class="mb-6">
      <div class="flex justify-between items-center">
        <div>
          <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">
            Payment Methods
          </h1>
          <p class="text-gray-600 dark:text-gray-400">
            Manage your payment methods and billing information
          </p>
        </div>
        
        <div class="flex gap-3">
          <button 
            @click="showAddPaymentMethod = true"
            class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center gap-2"
          >
            <Icon icon="ic:baseline-add" />
            Add Payment Method
          </button>
          <button 
            @click="refreshPaymentMethods"
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

    <!-- Payment Methods List -->
    <div v-if="isLoading" class="text-center py-12">
      <Icon icon="ic:baseline-refresh" class="animate-spin text-4xl text-gray-400" />
      <p class="text-gray-600 dark:text-gray-400 mt-2">Loading payment methods...</p>
    </div>

    <div v-else-if="paymentMethods.length === 0" class="text-center py-12">
      <Icon icon="ic:baseline-credit-card" class="text-4xl text-gray-400" />
      <h3 class="text-lg font-medium text-gray-900 dark:text-gray-100 mt-4">
        No Payment Methods
      </h3>
      <p class="text-gray-600 dark:text-gray-400 mt-2">
        Add a payment method to manage your subscriptions
      </p>
      <button 
        @click="showAddPaymentMethod = true"
        class="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
      >
        Add Your First Payment Method
      </button>
    </div>

    <div v-else class="space-y-6">
      <div 
        v-for="method in paymentMethods" 
        :key="method.id"
        class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 overflow-hidden"
      >
        <!-- Payment Method Header -->
        <div class="p-6 border-b border-gray-200 dark:border-gray-700">
          <div class="flex justify-between items-start">
            <div class="flex items-center gap-4">
              <div :class="getPaymentMethodIconClass(method.type)" class="p-3 rounded-lg">
                <Icon :icon="getPaymentMethodIcon(method.type)" class="text-xl" />
              </div>
              <div>
                <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100">
                  {{ getPaymentMethodName(method) }}
                </h3>
                <p class="text-sm text-gray-600 dark:text-gray-400">
                  Added {{ formatDate(method.createdAt) }}
                </p>
                <div class="flex items-center gap-2 mt-2">
                  <span v-if="method.isDefault" class="px-2 py-1 bg-blue-100 text-blue-600 dark:bg-blue-900/20 dark:text-blue-400 rounded-full text-xs font-medium">
                    Default
                  </span>
                  <span :class="getStatusClass(method.status)" class="px-2 py-1 rounded-full text-xs font-medium">
                    {{ method.status }}
                  </span>
                </div>
              </div>
            </div>
            
            <!-- Actions -->
            <div class="flex gap-2">
              <button 
                v-if="!method.isDefault"
                @click="setDefault(method)"
                :disabled="isSettingDefault"
                class="px-3 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50 transition-colors text-sm"
              >
                {{ isSettingDefault ? 'Setting...' : 'Set Default' }}
              </button>
              
              <button 
                @click="editPaymentMethod(method)"
                class="px-3 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors text-sm"
              >
                Edit
              </button>
              
              <button 
                @click="deletePaymentMethod(method)"
                class="px-3 py-2 bg-red-100 dark:bg-red-900/20 text-red-600 dark:text-red-400 rounded-lg hover:bg-red-200 dark:hover:bg-red-900/30 transition-colors text-sm"
              >
                Delete
              </button>
            </div>
          </div>
        </div>

        <!-- Payment Method Details -->
        <div class="p-6">
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <!-- Payment Method Info -->
            <div>
              <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">Payment Method Details</h4>
              <div class="space-y-2 text-sm">
                <div v-if="method.type === 'CREDIT_CARD' || method.type === 'DEBIT_CARD'">
                  <div class="flex justify-between">
                    <span class="text-gray-600 dark:text-gray-400">Card Type:</span>
                    <span class="font-medium text-gray-900 dark:text-gray-100">{{ method.cardType }}</span>
                  </div>
                  <div class="flex justify-between">
                    <span class="text-gray-600 dark:text-gray-400">Last 4 Digits:</span>
                    <span class="font-medium text-gray-900 dark:text-gray-100">•••• {{ method.last4 }}</span>
                  </div>
                  <div class="flex justify-between">
                    <span class="text-gray-600 dark:text-gray-400">Expires:</span>
                    <span class="font-medium text-gray-900 dark:text-gray-100">{{ method.expiryMonth }}/{{ method.expiryYear }}</span>
                  </div>
                  <div class="flex justify-between">
                    <span class="text-gray-600 dark:text-gray-400">Cardholder:</span>
                    <span class="font-medium text-gray-900 dark:text-gray-100">{{ method.cardholderName }}</span>
                  </div>
                </div>
                
                <div v-else-if="method.type === 'PAYPAL'">
                  <div class="flex justify-between">
                    <span class="text-gray-600 dark:text-gray-400">Email:</span>
                    <span class="font-medium text-gray-900 dark:text-gray-100">{{ method.email }}</span>
                  </div>
                  <div class="flex justify-between">
                    <span class="text-gray-600 dark:text-gray-400">Account Type:</span>
                    <span class="font-medium text-gray-900 dark:text-gray-100">PayPal</span>
                  </div>
                </div>
                
                <div v-else-if="method.type === 'BANK_TRANSFER'">
                  <div class="flex justify-between">
                    <span class="text-gray-600 dark:text-gray-400">Bank Name:</span>
                    <span class="font-medium text-gray-900 dark:text-gray-100">{{ method.bankName }}</span>
                  </div>
                  <div class="flex justify-between">
                    <span class="text-gray-600 dark:text-gray-400">Account:</span>
                    <span class="font-medium text-gray-900 dark:text-gray-100">•••• {{ method.last4 }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Usage Information -->
            <div v-if="method.usage">
              <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">Usage Information</h4>
              <div class="space-y-2 text-sm">
                <div class="flex justify-between">
                  <span class="text-gray-600 dark:text-gray-400">Active Subscriptions:</span>
                  <span class="font-medium text-gray-900 dark:text-gray-100">{{ method.usage.activeSubscriptions }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-gray-600 dark:text-gray-400">Last Used:</span>
                  <span class="font-medium text-gray-900 dark:text-gray-100">
                    {{ method.usage.lastUsed ? formatDate(method.usage.lastUsed) : 'Never' }}
                  </span>
                </div>
                <div class="flex justify-between">
                  <span class="text-gray-600 dark:text-gray-400">Total Spent:</span>
                  <span class="font-medium text-gray-900 dark:text-gray-100">
                    ${{ formatCurrency(method.usage.totalSpent) }}
                  </span>
                </div>
              </div>
            </div>

            <!-- Security Information -->
            <div>
              <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">Security</h4>
              <div class="space-y-2 text-sm">
                <div class="flex items-center gap-2">
                  <Icon icon="ic:baseline-verified" class="text-green-500" />
                  <span class="text-gray-600 dark:text-gray-400">Verified Payment Method</span>
                </div>
                <div class="flex items-center gap-2">
                  <Icon icon="ic:baseline-security" class="text-blue-500" />
                  <span class="text-gray-600 dark:text-gray-400">Secure Encryption</span>
                </div>
                <div class="flex items-center gap-2">
                  <Icon icon="ic:baseline-fingerprint" class="text-purple-500" />
                  <span class="text-gray-600 dark:text-gray-400">2FA Enabled</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Warning for Active Payment Methods -->
          <div v-if="method.usage?.activeSubscriptions > 0" class="mt-6 p-4 bg-yellow-50 dark:bg-yellow-900/20 rounded-lg">
            <div class="flex items-start gap-2">
              <Icon icon="ic:baseline-warning" class="text-yellow-600 dark:text-yellow-400 mt-0.5" />
              <div class="text-sm text-yellow-800 dark:text-yellow-200">
                <p class="font-medium mb-1">Active Subscriptions</p>
                <p>This payment method is currently used by {{ method.usage.activeSubscriptions }} subscription(s). Removing or deactivating it may affect your active services.</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Add Payment Method Modal -->
    <AddPaymentMethodModal 
      v-if="showAddPaymentMethod"
      @close="showAddPaymentMethod = false"
      @added="onPaymentMethodAdded"
    />

    <!-- Edit Payment Method Modal -->
    <EditPaymentMethodModal 
      v-if="showEditPaymentMethod"
      :payment-method="editingPaymentMethod"
      @close="showEditPaymentMethod = false"
      @updated="onPaymentMethodUpdated"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useBillingStore } from '@/stores/billing/billingStore'
import AddPaymentMethodModal from '@/components/billing/AddPaymentMethodModal.vue'
import EditPaymentMethodModal from '@/components/billing/EditPaymentMethodModal.vue'

const billingStore = useBillingStore()

// State
const isLoading = ref(false)
const isSettingDefault = ref(false)
const showAddPaymentMethod = ref(false)
const showEditPaymentMethod = ref(false)
const editingPaymentMethod = ref(null)

// Mock payment methods data
const paymentMethods = ref([
  {
    id: 'pm_1',
    type: 'CREDIT_CARD',
    cardType: 'VISA',
    last4: '4242',
    expiryMonth: '12',
    expiryYear: '2025',
    cardholderName: 'John Doe',
    isDefault: true,
    status: 'ACTIVE',
    createdAt: '2024-01-15T10:30:00Z',
    usage: {
      activeSubscriptions: 2,
      lastUsed: '2024-01-20T14:30:00Z',
      totalSpent: 29900 // $299.00 in cents
    }
  },
  {
    id: 'pm_2',
    type: 'PAYPAL',
    email: 'john.doe@example.com',
    isDefault: false,
    status: 'ACTIVE',
    createdAt: '2024-01-10T09:15:00Z',
    usage: {
      activeSubscriptions: 0,
      lastUsed: '2024-01-15T10:30:00Z',
      totalSpent: 15000 // $150.00 in cents
    }
  },
  {
    id: 'pm_3',
    type: 'BANK_TRANSFER',
    bankName: 'Chase Bank',
    last4: '7890',
    isDefault: false,
    status: 'PENDING',
    createdAt: '2024-01-20T16:45:00Z',
    usage: {
      activeSubscriptions: 0,
      lastUsed: null,
      totalSpent: 0
    }
  }
])

// Methods
const getPaymentMethodIcon = (type) => {
  const iconMap = {
    'CREDIT_CARD': 'ic:baseline-credit-card',
    'DEBIT_CARD': 'ic:baseline-credit-card',
    'PAYPAL': 'ic:baseline-paypal',
    'BANK_TRANSFER': 'ic:baseline-account-balance',
    'APPLE_PAY': 'ic:baseline-apple',
    'GOOGLE_PAY': 'ic:baseline-google-pay'
  }
  return iconMap[type] || 'ic:baseline-credit-card'
}

const getPaymentMethodIconClass = (type) => {
  const classMap = {
    'CREDIT_CARD': 'bg-blue-100 text-blue-600 dark:bg-blue-900/20 dark:text-blue-400',
    'DEBIT_CARD': 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400',
    'PAYPAL': 'bg-yellow-100 text-yellow-600 dark:bg-yellow-900/20 dark:text-yellow-400',
    'BANK_TRANSFER': 'bg-purple-100 text-purple-600 dark:bg-purple-900/20 dark:text-purple-400',
    'APPLE_PAY': 'bg-gray-100 text-gray-600 dark:bg-gray-900/20 dark:text-gray-400',
    'GOOGLE_PAY': 'bg-gray-100 text-gray-600 dark:bg-gray-900/20 dark:text-gray-400'
  }
  return classMap[type] || 'bg-gray-100 text-gray-600'
}

const getPaymentMethodName = (method) => {
  if (method.type === 'CREDIT_CARD' || method.type === 'DEBIT_CARD') {
    return `${method.cardType} ending in ${method.last4}`
  }
  if (method.type === 'PAYPAL') {
    return `PayPal (${method.email})`
  }
  if (method.type === 'BANK_TRANSFER') {
    return `${method.bankName} ending in ${method.last4}`
  }
  return method.type
}

const getStatusClass = (status) => {
  switch (status?.toUpperCase()) {
    case 'ACTIVE':
      return 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400'
    case 'PENDING':
      return 'bg-yellow-100 text-yellow-600 dark:bg-yellow-900/20 dark:text-yellow-400'
    case 'FAILED':
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
  }).format(amount / 100) // Convert from cents
}

const formatDate = (dateString) => {
  if (!dateString) return 'Never'
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

const refreshPaymentMethods = async () => {
  isLoading.value = true
  
  try {
    await billingStore.fetchPaymentMethods()
    // Update local state with store data
    // paymentMethods.value = billingStore.paymentMethods
  } catch (error) {
    console.error('Failed to refresh payment methods:', error)
  } finally {
    isLoading.value = false
  }
}

const setDefault = async (method) => {
  if (confirm(`Set ${getPaymentMethodName(method)} as your default payment method?`)) {
    isSettingDefault.value = true
    
    try {
      // API call to set default
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      // Update local state
      paymentMethods.value.forEach(pm => {
        pm.isDefault = pm.id === method.id
      })
      
    } catch (error) {
      console.error('Failed to set default payment method:', error)
    } finally {
      isSettingDefault.value = false
    }
  }
}

const editPaymentMethod = (method) => {
  editingPaymentMethod.value = method
  showEditPaymentMethod.value = true
}

const deletePaymentMethod = (method) => {
  const methodName = getPaymentMethodName(method)
  
  if (confirm(`Are you sure you want to delete ${methodName}? This action cannot be undone.`)) {
    // API call to delete
    console.log('Delete payment method:', method)
    
    // Remove from local state
    const index = paymentMethods.value.findIndex(pm => pm.id === method.id)
    if (index !== -1) {
      paymentMethods.value.splice(index, 1)
    }
  }
}

const onPaymentMethodAdded = (newMethod) => {
  showAddPaymentMethod.value = false
  paymentMethods.value.push(newMethod)
}

const onPaymentMethodUpdated = (updatedMethod) => {
  showEditPaymentMethod.value = false
  editingPaymentMethod.value = null
  
  // Update in local state
  const index = paymentMethods.value.findIndex(pm => pm.id === updatedMethod.id)
  if (index !== -1) {
    paymentMethods.value[index] = updatedMethod
  }
}

// Lifecycle
onMounted(() => {
  refreshPaymentMethods()
})
</script>

<style scoped>
.payment-methods-page {
  max-width: 1400px;
  margin: 0 auto;
}
</style>
