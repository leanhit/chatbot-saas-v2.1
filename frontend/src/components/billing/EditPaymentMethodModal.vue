<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white dark:bg-gray-800 rounded-lg max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
      <!-- Header -->
      <div class="p-6 border-b border-gray-200 dark:border-gray-700">
        <div class="flex justify-between items-center">
          <h3 class="text-xl font-semibold text-gray-900 dark:text-gray-100">
            Edit Payment Method
          </h3>
          <button 
            @click="$emit('close')"
            class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
          >
            <Icon icon="ic:baseline-close" class="text-xl" />
          </button>
        </div>
      </div>

      <!-- Payment Method Info -->
      <div class="p-6">
        <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4 mb-6">
          <div class="flex items-center gap-3">
            <div :class="getPaymentMethodIconClass(paymentMethod.type)" class="p-2 rounded-lg">
              <Icon :icon="getPaymentMethodIcon(paymentMethod.type)" class="text-xl" />
            </div>
            <div>
              <h4 class="font-semibold text-gray-900 dark:text-gray-100">
                {{ getPaymentMethodName(paymentMethod) }}
              </h4>
              <p class="text-sm text-gray-600 dark:text-gray-400">
                Added {{ formatDate(paymentMethod.createdAt) }}
              </p>
            </div>
          </div>
        </div>

        <!-- Editable Fields -->
        <div class="space-y-4">
          <!-- Cardholder Name (for cards) -->
          <div v-if="paymentMethod.type === 'CREDIT_CARD' || paymentMethod.type === 'DEBIT_CARD'">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Cardholder Name
            </label>
            <input 
              v-model="editData.cardholderName"
              type="text"
              placeholder="John Doe"
              class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              :class="{ 'border-red-300 dark:border-red-600': errors.cardholderName }"
            />
            <p v-if="errors.cardholderName" class="text-red-600 dark:text-red-400 text-sm mt-1">
              {{ errors.cardholderName }}
            </p>
          </div>

          <!-- PayPal Email -->
          <div v-else-if="paymentMethod.type === 'PAYPAL'">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              PayPal Email
            </label>
            <input 
              v-model="editData.email"
              type="email"
              placeholder="john.doe@example.com"
              class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              :class="{ 'border-red-300 dark:border-red-600': errors.email }"
            />
            <p v-if="errors.email" class="text-red-600 dark:text-red-400 text-sm mt-1">
              {{ errors.email }}
            </p>
          </div>

          <!-- Bank Name -->
          <div v-else-if="paymentMethod.type === 'BANK_TRANSFER'">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Bank Name
            </label>
            <input 
              v-model="editData.bankName"
              type="text"
              placeholder="Chase Bank"
              class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              :class="{ 'border-red-300 dark:border-red-600': errors.bankName }"
            />
            <p v-if="errors.bankName" class="text-red-600 dark:text-red-400 text-sm mt-1">
              {{ errors.bankName }}
            </p>
          </div>

          <!-- Billing Address -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Billing Address
            </label>
            <textarea 
              v-model="editData.billingAddress"
              rows="3"
              placeholder="123 Main St, City, State 12345"
              class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            ></textarea>
          </div>

          <!-- Nickname -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Nickname (Optional)
            </label>
            <input 
              v-model="editData.nickname"
              type="text"
              placeholder="Personal Card"
              class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>

          <!-- Set as Default -->
          <div>
            <label class="flex items-center gap-2">
              <input 
                type="checkbox" 
                v-model="editData.isDefault"
                class="rounded"
              />
              <span class="text-sm text-gray-700 dark:text-gray-300">
                Set as default payment method
              </span>
            </label>
          </div>
        </div>

        <!-- Status Information -->
        <div class="mt-6 p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
          <h5 class="font-medium text-blue-900 dark:text-blue-100 mb-2">Payment Method Status</h5>
          <div class="space-y-2 text-sm">
            <div class="flex justify-between">
              <span class="text-blue-700 dark:text-blue-300">Current Status:</span>
              <span :class="getStatusClass(paymentMethod.status)" class="px-2 py-1 rounded-full text-xs font-medium">
                {{ paymentMethod.status }}
              </span>
            </div>
            <div v-if="paymentMethod.usage" class="flex justify-between">
              <span class="text-blue-700 dark:text-blue-300">Active Subscriptions:</span>
              <span class="text-blue-900 dark:text-blue-100 font-medium">{{ paymentMethod.usage.activeSubscriptions }}</span>
            </div>
            <div v-if="paymentMethod.usage" class="flex justify-between">
              <span class="text-blue-700 dark:text-blue-300">Last Used:</span>
              <span class="text-blue-900 dark:text-blue-100 font-medium">{{ formatDate(paymentMethod.usage.lastUsed) }}</span>
            </div>
          </div>
        </div>

        <!-- Warning for Active Payment Methods -->
        <div v-if="paymentMethod.usage?.activeSubscriptions > 0" class="mt-4 p-4 bg-yellow-50 dark:bg-yellow-900/20 rounded-lg">
          <div class="flex items-start gap-2">
            <Icon icon="ic:baseline-warning" class="text-yellow-600 dark:text-yellow-400 mt-0.5" />
            <div class="text-sm text-yellow-800 dark:text-yellow-200">
              <p class="font-medium mb-1">Active Subscriptions</p>
              <p>This payment method is currently used by {{ paymentMethod.usage.activeSubscriptions }} subscription(s). Removing or deactivating it may affect your active services.</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Footer -->
      <div class="p-6 border-t border-gray-200 dark:border-gray-700">
        <div class="flex justify-between">
          <div class="flex gap-3">
            <button 
              v-if="paymentMethod.status === 'ACTIVE'"
              @click="deactivatePaymentMethod"
              :disabled="isSubmitting"
              class="px-4 py-2 bg-yellow-600 text-white rounded-lg hover:bg-yellow-700 disabled:opacity-50 transition-colors"
            >
              {{ isSubmitting ? 'Deactivating...' : 'Deactivate' }}
            </button>
            
            <button 
              @click="deletePaymentMethod"
              :disabled="isSubmitting"
              class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:opacity-50 transition-colors"
            >
              {{ isSubmitting ? 'Deleting...' : 'Delete' }}
            </button>
          </div>
          
          <div class="flex gap-3">
            <button 
              @click="$emit('close')"
              class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-600 transition-colors"
            >
              Cancel
            </button>
            
            <button 
              @click="updatePaymentMethod"
              :disabled="!canUpdate || isSubmitting"
              class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              {{ isSubmitting ? 'Updating...' : 'Update' }}
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
  paymentMethod: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'updated'])

// State
const isSubmitting = ref(false)

// Edit data
const editData = ref({
  cardholderName: '',
  email: '',
  bankName: '',
  billingAddress: '',
  nickname: '',
  isDefault: false
})

const errors = ref({})

// Initialize edit data
const initializeEditData = () => {
  editData.value = {
    cardholderName: props.paymentMethod.cardholderName || '',
    email: props.paymentMethod.email || '',
    bankName: props.paymentMethod.bankName || '',
    billingAddress: props.paymentMethod.billingAddress || '',
    nickname: props.paymentMethod.nickname || '',
    isDefault: props.paymentMethod.isDefault || false
  }
}

// Computed
const canUpdate = computed(() => {
  // Check if any field has changed
  return JSON.stringify(editData.value) !== JSON.stringify({
    cardholderName: props.paymentMethod.cardholderName || '',
    email: props.paymentMethod.email || '',
    bankName: props.paymentMethod.bankName || '',
    billingAddress: props.paymentMethod.billingAddress || '',
    nickname: props.paymentMethod.nickname || '',
    isDefault: props.paymentMethod.isDefault || false
  })
})

// Methods
const getPaymentMethodIcon = (type) => {
  const iconMap = {
    'CREDIT_CARD': 'ic:baseline-credit-card',
    'DEBIT_CARD': 'ic:baseline-credit-card',
    'PAYPAL': 'ic:baseline-paypal',
    'BANK_TRANSFER': 'ic:baseline-account-balance'
  }
  return iconMap[type] || 'ic:baseline-credit-card'
}

const getPaymentMethodIconClass = (type) => {
  const classMap = {
    'CREDIT_CARD': 'bg-blue-100 text-blue-600 dark:bg-blue-900/20 dark:text-blue-400',
    'DEBIT_CARD': 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400',
    'PAYPAL': 'bg-yellow-100 text-yellow-600 dark:bg-yellow-900/20 dark:text-yellow-400',
    'BANK_TRANSFER': 'bg-purple-100 text-purple-600 dark:bg-purple-900/20 dark:text-purple-400'
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

const validateForm = () => {
  errors.value = {}
  
  if (props.paymentMethod.type === 'CREDIT_CARD' || props.paymentMethod.type === 'DEBIT_CARD') {
    if (!editData.value.cardholderName.trim()) {
      errors.value.cardholderName = 'Cardholder name is required'
    }
  }
  
  if (props.paymentMethod.type === 'PAYPAL') {
    if (!editData.value.email.trim()) {
      errors.value.email = 'Email is required'
    } else if (!editData.value.email.includes('@')) {
      errors.value.email = 'Invalid email address'
    }
  }
  
  if (props.paymentMethod.type === 'BANK_TRANSFER') {
    if (!editData.value.bankName.trim()) {
      errors.value.bankName = 'Bank name is required'
    }
  }
  
  return Object.keys(errors.value).length === 0
}

const updatePaymentMethod = async () => {
  if (!validateForm()) return
  
  isSubmitting.value = true
  
  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    const updatedMethod = {
      ...props.paymentMethod,
      ...editData.value,
      updatedAt: new Date().toISOString()
    }
    
    emit('updated', updatedMethod)
    
  } catch (error) {
    console.error('Failed to update payment method:', error)
  } finally {
    isSubmitting.value = false
  }
}

const deactivatePaymentMethod = async () => {
  if (!confirm('Are you sure you want to deactivate this payment method?')) return
  
  isSubmitting.value = true
  
  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    const updatedMethod = {
      ...props.paymentMethod,
      status: 'INACTIVE',
      updatedAt: new Date().toISOString()
    }
    
    emit('updated', updatedMethod)
    
  } catch (error) {
    console.error('Failed to deactivate payment method:', error)
  } finally {
    isSubmitting.value = false
  }
}

const deletePaymentMethod = async () => {
  const methodName = getPaymentMethodName(props.paymentMethod)
  
  if (!confirm(`Are you sure you want to delete ${methodName}? This action cannot be undone.`)) return
  
  isSubmitting.value = true
  
  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // In a real app, this would emit a 'deleted' event
    // For now, we'll emit 'updated' with a deleted status
    const updatedMethod = {
      ...props.paymentMethod,
      status: 'DELETED',
      updatedAt: new Date().toISOString()
    }
    
    emit('updated', updatedMethod)
    
  } catch (error) {
    console.error('Failed to delete payment method:', error)
  } finally {
    isSubmitting.value = false
  }
}

// Initialize on mount
initializeEditData()
</script>

<style scoped>
.modal-backdrop {
  backdrop-filter: blur(4px);
}
</style>
