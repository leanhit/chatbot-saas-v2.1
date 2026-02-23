<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white dark:bg-gray-800 rounded-lg max-w-4xl w-full mx-4 max-h-[90vh] overflow-y-auto">
      <!-- Header -->
      <div class="p-6 border-b border-gray-200 dark:border-gray-700">
        <div class="flex justify-between items-center">
          <h3 class="text-xl font-semibold text-gray-900 dark:text-gray-100">
            Payment Methods
          </h3>
          <button 
            @click="$emit('close')"
            class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
          >
            <Icon icon="ic:baseline-close" class="text-xl" />
          </button>
        </div>
      </div>

      <!-- Payment Methods List -->
      <div class="p-6">
        <div class="flex justify-between items-center mb-6">
          <h4 class="text-lg font-medium text-gray-900 dark:text-gray-100">
            Your Payment Methods
          </h4>
          <button 
            @click="showAddPaymentMethod = true"
            class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center gap-2"
          >
            <Icon icon="ic:baseline-add" />
            Add Payment Method
          </button>
        </div>

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

        <div v-else class="space-y-4">
          <div 
            v-for="method in paymentMethods" 
            :key="method.id"
            class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4 border border-gray-200 dark:border-gray-600"
            :class="{ 'border-blue-500': method.isDefault }"
          >
            <div class="flex justify-between items-start">
              <div class="flex items-center gap-4">
                <!-- Payment Method Icon -->
                <div :class="getPaymentMethodIconClass(method.type)" class="p-3 rounded-lg">
                  <Icon :icon="getPaymentMethodIcon(method.type)" class="text-xl" />
                </div>

                <!-- Payment Method Details -->
                <div>
                  <div class="flex items-center gap-2 mb-1">
                    <h5 class="font-semibold text-gray-900 dark:text-gray-100">
                      {{ getPaymentMethodName(method) }}
                    </h5>
                    <span v-if="method.isDefault" class="px-2 py-1 bg-blue-100 text-blue-600 dark:bg-blue-900/20 dark:text-blue-400 rounded-full text-xs font-medium">
                      Default
                    </span>
                  </div>
                  
                  <div class="space-y-1 text-sm text-gray-600 dark:text-gray-400">
                    <div v-if="method.type === 'CREDIT_CARD' || method.type === 'DEBIT_CARD'">
                      <p>•••• •••• •••• {{ method.last4 }}</p>
                      <p>Expires {{ method.expiryMonth }}/{{ method.expiryYear }}</p>
                      <p>{{ method.cardholderName }}</p>
                    </div>
                    
                    <div v-else-if="method.type === 'PAYPAL'">
                      <p>{{ method.email }}</p>
                      <p>PayPal account</p>
                    </div>
                    
                    <div v-else-if="method.type === 'BANK_TRANSFER'">
                      <p>{{ method.bankName }}</p>
                      <p>Account ending in {{ method.last4 }}</p>
                    </div>
                    
                    <div class="flex items-center gap-2 mt-2">
                      <span :class="getStatusClass(method.status)" class="px-2 py-1 rounded-full text-xs font-medium">
                        {{ method.status }}
                      </span>
                      <span class="text-xs">
                        Added {{ formatDate(method.createdAt) }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Action Buttons -->
              <div class="flex gap-2">
                <button 
                  v-if="!method.isDefault"
                  @click="setDefault(method)"
                  :disabled="isSettingDefault"
                  class="px-3 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50 transition-colors text-sm"
                >
                  Set Default
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

            <!-- Usage Information -->
            <div v-if="method.usage" class="mt-4 pt-4 border-t border-gray-200 dark:border-gray-600">
              <p class="text-sm text-gray-600 dark:text-gray-400 mb-2">Usage Information</p>
              <div class="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
                <div>
                  <p class="text-gray-500">Active Subscriptions</p>
                  <p class="font-medium text-gray-900 dark:text-gray-100">{{ method.usage.activeSubscriptions }}</p>
                </div>
                <div>
                  <p class="text-gray-500">Last Used</p>
                  <p class="font-medium text-gray-900 dark:text-gray-100">{{ formatDate(method.usage.lastUsed) }}</p>
                </div>
                <div>
                  <p class="text-gray-500">Total Spent</p>
                  <p class="font-medium text-gray-900 dark:text-gray-100">${{ formatCurrency(method.usage.totalSpent) }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Footer -->
      <div class="p-6 border-t border-gray-200 dark:border-gray-700">
        <div class="flex justify-between items-center">
          <div class="text-sm text-gray-600 dark:text-gray-400">
            {{ paymentMethods.length }} payment method{{ paymentMethods.length !== 1 ? 's' : '' }}
          </div>
          
          <div class="flex gap-3">
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
import AddPaymentMethodModal from './AddPaymentMethodModal.vue'
import EditPaymentMethodModal from './EditPaymentMethodModal.vue'

const emit = defineEmits(['close'])

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

const formatDate = (dateString) => {
  if (!dateString) return 'Never'
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

const formatCurrency = (amount) => {
  if (!amount) return '0.00'
  return new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(amount / 100) // Convert from cents
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
  // Load payment methods
  isLoading.value = true
  setTimeout(() => {
    isLoading.value = false
  }, 1000)
})
</script>

<style scoped>
.modal-backdrop {
  backdrop-filter: blur(4px);
}
</style>
