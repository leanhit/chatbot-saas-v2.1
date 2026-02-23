<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white dark:bg-gray-800 rounded-lg max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
      <!-- Header -->
      <div class="p-6 border-b border-gray-200 dark:border-gray-700">
        <div class="flex justify-between items-center">
          <div>
            <h3 class="text-xl font-semibold text-gray-900 dark:text-gray-100">
              Payment
            </h3>
            <p class="text-gray-600 dark:text-gray-400">
              Invoice #{{ invoice.invoiceNumber }}
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

      <!-- Payment Content -->
      <div class="p-6">
        <!-- Invoice Summary -->
        <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-6 mb-6">
          <h4 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">Invoice Summary</h4>
          <div class="space-y-3">
            <div class="flex justify-between">
              <span class="text-gray-600 dark:text-gray-400">Invoice Number:</span>
              <span class="font-medium text-gray-900 dark:text-gray-100">{{ invoice.invoiceNumber }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600 dark:text-gray-400">Due Date:</span>
              <span class="font-medium text-gray-900 dark:text-gray-100">{{ formatDate(invoice.dueDate) }}</span>
            </div>
            <div class="flex justify-between text-lg font-bold">
              <span class="text-gray-900 dark:text-gray-100">Amount Due:</span>
              <span class="text-gray-900 dark:text-gray-100">${{ formatCurrency(invoice.amount) }}</span>
            </div>
          </div>
        </div>

        <!-- Payment Method Selection -->
        <div class="mb-6">
          <h4 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">Payment Method</h4>
          
          <!-- Existing Payment Methods -->
          <div class="space-y-3 mb-6">
            <div 
              v-for="method in paymentMethods" 
              :key="method.id"
              @click="selectedPaymentMethod = method.id"
              :class="[
                'p-4 border rounded-lg cursor-pointer transition-colors',
                selectedPaymentMethod === method.id 
                  ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20' 
                  : 'border-gray-200 dark:border-gray-600 hover:border-gray-300 dark:hover:border-gray-500'
              ]"
            >
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-3">
                  <div :class="getPaymentMethodIconClass(method.type)" class="p-2 rounded-lg">
                    <Icon :icon="getPaymentMethodIcon(method.type)" class="text-xl" />
                  </div>
                  <div>
                    <p class="font-medium text-gray-900 dark:text-gray-100">
                      {{ getPaymentMethodName(method) }}
                    </p>
                    <p class="text-sm text-gray-600 dark:text-gray-400">
                      {{ getPaymentMethodDetails(method) }}
                    </p>
                  </div>
                </div>
                <div class="flex items-center gap-2">
                  <div v-if="method.isDefault" class="px-2 py-1 bg-blue-100 text-blue-600 dark:bg-blue-900/20 dark:text-blue-400 rounded-full text-xs font-medium">
                    Default
                  </div>
                  <div 
                    :class="[
                      'w-4 h-4 rounded-full border-2',
                      selectedPaymentMethod === method.id 
                        ? 'border-blue-500 bg-blue-500' 
                        : 'border-gray-300 dark:border-gray-600'
                    ]"
                  >
                    <div v-if="selectedPaymentMethod === method.id" class="w-2 h-2 bg-white rounded-full m-0.5"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Add New Payment Method -->
          <button 
            @click="showAddPaymentMethod = true"
            class="w-full p-4 border-2 border-dashed border-gray-300 dark:border-gray-600 rounded-lg text-gray-600 dark:text-gray-400 hover:border-gray-400 dark:hover:border-gray-500 hover:text-gray-700 dark:hover:text-gray-300 transition-colors flex items-center justify-center gap-2"
          >
            <Icon icon="ic:baseline-add" />
            Add New Payment Method
          </button>
        </div>

        <!-- Card Details Form -->
        <div v-if="selectedPaymentMethod && getSelectedPaymentMethod().type === 'CREDIT_CARD'" class="mb-6">
          <h4 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">Card Details</h4>
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Card Number
              </label>
              <input 
                v-model="cardDetails.number"
                type="text"
                placeholder="1234 5678 9012 3456"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
            
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Expiry Date
                </label>
                <input 
                  v-model="cardDetails.expiry"
                  type="text"
                  placeholder="MM/YY"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  CVV
                </label>
                <input 
                  v-model="cardDetails.cvv"
                  type="text"
                  placeholder="123"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Cardholder Name
              </label>
              <input 
                v-model="cardDetails.name"
                type="text"
                placeholder="John Doe"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
          </div>
        </div>

        <!-- Billing Address -->
        <div class="mb-6">
          <h4 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">Billing Address</h4>
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Full Name
              </label>
              <input 
                v-model="billingAddress.name"
                type="text"
                placeholder="John Doe"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Email
              </label>
              <input 
                v-model="billingAddress.email"
                type="email"
                placeholder="john.doe@example.com"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Address
              </label>
              <input 
                v-model="billingAddress.address"
                type="text"
                placeholder="123 Main St"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
            
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  City
                </label>
                <input 
                  v-model="billingAddress.city"
                  type="text"
                  placeholder="New York"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  State
                </label>
                <input 
                  v-model="billingAddress.state"
                  type="text"
                  placeholder="NY"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
            </div>
            
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  ZIP Code
                </label>
                <input 
                  v-model="billingAddress.zip"
                  type="text"
                  placeholder="10001"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Country
                </label>
                <select 
                  v-model="billingAddress.country"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="US">United States</option>
                  <option value="CA">Canada</option>
                  <option value="UK">United Kingdom</option>
                  <option value="AU">Australia</option>
                </select>
              </div>
            </div>
          </div>
        </div>

        <!-- Terms and Conditions -->
        <div class="mb-6">
          <label class="flex items-start gap-2">
            <input 
              v-model="acceptTerms"
              type="checkbox"
              class="mt-1 rounded"
            />
            <span class="text-sm text-gray-600 dark:text-gray-400">
              I agree to the 
              <a href="#" class="text-blue-600 dark:text-blue-400 hover:underline">Terms and Conditions</a>
              and 
              <a href="#" class="text-blue-600 dark:text-blue-400 hover:underline">Privacy Policy</a>
            </span>
          </label>
        </div>
      </div>

      <!-- Footer -->
      <div class="p-6 border-t border-gray-200 dark:border-gray-700">
        <div class="flex justify-between items-center">
          <div class="text-sm text-gray-600 dark:text-gray-400">
            <div class="flex items-center gap-2">
              <Icon icon="ic:baseline-lock" class="text-green-500" />
              <span>Secure payment powered by Stripe</span>
            </div>
          </div>
          
          <div class="flex gap-3">
            <button 
              @click="$emit('close')"
              class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-600 transition-colors"
            >
              Cancel
            </button>
            
            <button 
              @click="processPayment"
              :disabled="!canPay || isProcessing"
              class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center gap-2"
            >
              <Icon v-if="isProcessing" icon="ic:baseline-refresh" class="animate-spin" />
              {{ isProcessing ? 'Processing...' : `Pay $${formatCurrency(invoice.amount)}` }}
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
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import AddPaymentMethodModal from './AddPaymentMethodModal.vue'

const props = defineProps({
  invoice: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'paid'])

// State
const selectedPaymentMethod = ref('')
const showAddPaymentMethod = ref(false)
const isProcessing = ref(false)
const acceptTerms = ref(false)

// Form data
const cardDetails = ref({
  number: '',
  expiry: '',
  cvv: '',
  name: ''
})

const billingAddress = ref({
  name: '',
  email: '',
  address: '',
  city: '',
  state: '',
  zip: '',
  country: 'US'
})

// Mock payment methods
const paymentMethods = ref([
  {
    id: 'pm_1',
    type: 'CREDIT_CARD',
    cardType: 'VISA',
    last4: '4242',
    expiryMonth: '12',
    expiryYear: '2025',
    cardholderName: 'John Doe',
    isDefault: true
  },
  {
    id: 'pm_2',
    type: 'PAYPAL',
    email: 'john.doe@example.com',
    isDefault: false
  },
  {
    id: 'pm_3',
    type: 'BANK_TRANSFER',
    bankName: 'Chase Bank',
    last4: '7890',
    isDefault: false
  }
])

// Computed
const canPay = computed(() => {
  return selectedPaymentMethod.value && acceptTerms.value
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

const getPaymentMethodDetails = (method) => {
  if (method.type === 'CREDIT_CARD' || method.type === 'DEBIT_CARD') {
    return `Expires ${method.expiryMonth}/${method.expiryYear}`
  }
  if (method.type === 'PAYPAL') {
    return 'PayPal account'
  }
  if (method.type === 'BANK_TRANSFER') {
    return 'Bank account'
  }
  return ''
}

const getSelectedPaymentMethod = () => {
  return paymentMethods.value.find(method => method.id === selectedPaymentMethod.value)
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

const onPaymentMethodAdded = (newMethod) => {
  paymentMethods.value.push(newMethod)
  showAddPaymentMethod.value = false
}

const processPayment = async () => {
  if (!canPay.value) return
  
  isProcessing.value = true
  
  try {
    // Simulate payment processing
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    const paidInvoice = {
      ...props.invoice,
      status: 'PAID',
      paidAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    }
    
    emit('paid', paidInvoice)
    
  } catch (error) {
    console.error('Payment failed:', error)
  } finally {
    isProcessing.value = false
  }
}
</script>

<style scoped>
.modal-backdrop {
  backdrop-filter: blur(4px);
}
</style>
