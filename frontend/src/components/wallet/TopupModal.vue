<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white dark:bg-gray-800 rounded-lg max-w-md w-full mx-4 max-h-[90vh] overflow-y-auto">
      <!-- Header -->
      <div class="p-6 border-b border-gray-200 dark:border-gray-700">
        <div class="flex justify-between items-center">
          <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100">
            Top Up Wallet
          </h3>
          <button 
            @click="$emit('close')"
            class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
          >
            <Icon icon="ic:baseline-close" class="text-xl" />
          </button>
        </div>
      </div>

      <!-- Body -->
      <div class="p-6">
        <!-- Current Balance -->
        <div class="bg-blue-50 dark:bg-blue-900/20 rounded-lg p-4 mb-6">
          <div class="flex justify-between items-center">
            <div>
              <p class="text-sm text-blue-600 dark:text-blue-400 font-medium">Current Balance</p>
              <p class="text-2xl font-bold text-blue-900 dark:text-blue-100">
                ${{ formatCurrency(wallet?.balance || 0) }}
              </p>
            </div>
            <div class="p-2 bg-blue-100 dark:bg-blue-900/30 rounded-lg">
              <Icon icon="ic:baseline-account-balance-wallet" class="text-blue-600 dark:text-blue-400 text-2xl" />
            </div>
          </div>
        </div>

        <!-- Amount Selection -->
        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Select Amount
          </label>
          
          <!-- Quick Amount Buttons -->
          <div class="grid grid-cols-3 gap-2 mb-4">
            <button 
              v-for="amount in quickAmounts" 
              :key="amount"
              @click="selectedAmount = amount"
              :class="[
                'p-3 rounded-lg border-2 font-medium transition-colors',
                selectedAmount === amount 
                  ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400' 
                  : 'border-gray-200 dark:border-gray-600 hover:border-gray-300 dark:hover:border-gray-500'
              ]"
            >
              ${{ amount }}
            </button>
          </div>

          <!-- Custom Amount Input -->
          <div class="relative">
            <span class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-500 text-lg">$</span>
            <input 
              v-model="customAmount"
              type="number"
              placeholder="Enter custom amount"
              min="1"
              step="0.01"
              class="w-full pl-8 pr-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent text-lg"
              @input="onCustomAmountChange"
            />
          </div>
          <p v-if="errorMessage" class="text-red-600 dark:text-red-400 text-sm mt-2">
            {{ errorMessage }}
          </p>
        </div>

        <!-- Payment Method Selection -->
        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Payment Method
          </label>
          
          <div class="space-y-3">
            <label 
              v-for="method in paymentMethods" 
              :key="method.id"
              class="flex items-center p-3 border border-gray-200 dark:border-gray-600 rounded-lg cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700"
            >
              <input 
                type="radio" 
                v-model="selectedPaymentMethod" 
                :value="method.id"
                class="mr-3"
              />
              <div class="flex-1 flex items-center gap-3">
                <div class="p-2 bg-gray-100 dark:bg-gray-700 rounded-lg">
                  <Icon :icon="getPaymentMethodIcon(method.type)" class="text-lg" />
                </div>
                <div>
                  <p class="font-medium text-gray-900 dark:text-gray-100">
                    {{ getPaymentMethodName(method) }}
                  </p>
                  <p class="text-sm text-gray-600 dark:text-gray-400">
                    {{ method.description }}
                  </p>
                </div>
              </div>
              <div v-if="method.isDefault" class="px-2 py-1 bg-green-100 dark:bg-green-900/20 text-green-600 dark:text-green-400 rounded-full text-xs font-medium">
                Default
              </div>
            </label>
          </div>

          <button 
            @click="showAddPaymentMethod = true"
            class="w-full mt-3 px-4 py-2 border-2 border-dashed border-gray-300 dark:border-gray-600 rounded-lg text-gray-600 dark:text-gray-400 hover:border-gray-400 dark:hover:border-gray-500 hover:text-gray-700 dark:hover:text-gray-300 transition-colors"
          >
            <Icon icon="ic:baseline-add" class="mr-2" />
            Add Payment Method
          </button>
        </div>

        <!-- Order Summary -->
        <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4 mb-6">
          <h4 class="font-medium text-gray-900 dark:text-gray-100 mb-3">Order Summary</h4>
          <div class="space-y-2">
            <div class="flex justify-between text-sm">
              <span class="text-gray-600 dark:text-gray-400">Top-up Amount:</span>
              <span class="font-medium text-gray-900 dark:text-gray-100">
                ${{ formatCurrency(finalAmount) }}
              </span>
            </div>
            <div class="flex justify-between text-sm">
              <span class="text-gray-600 dark:text-gray-400">Processing Fee:</span>
              <span class="font-medium text-gray-900 dark:text-gray-100">
                ${{ formatCurrency(processingFee) }}
              </span>
            </div>
            <div class="border-t border-gray-200 dark:border-gray-600 pt-2">
              <div class="flex justify-between">
                <span class="font-medium text-gray-900 dark:text-gray-100">Total:</span>
                <span class="font-bold text-lg text-gray-900 dark:text-gray-100">
                  ${{ formatCurrency(totalAmount) }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Terms and Conditions -->
        <div class="mb-6">
          <label class="flex items-start gap-2">
            <input 
              type="checkbox" 
              v-model="agreeToTerms"
              class="mt-1"
            />
            <span class="text-sm text-gray-600 dark:text-gray-400">
              I agree to the 
              <a href="#" class="text-blue-600 dark:text-blue-400 hover:underline">Terms and Conditions</a>
              and 
              <a href="#" class="text-blue-600 dark:text-blue-400 hover:underline">Refund Policy</a>
            </span>
          </label>
        </div>
      </div>

      <!-- Footer -->
      <div class="p-6 border-t border-gray-200 dark:border-gray-700">
        <div class="flex gap-3">
          <button 
            @click="$emit('close')"
            class="flex-1 px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
          >
            Cancel
          </button>
          <button 
            @click="processTopup"
            :disabled="!canProcess"
            class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center justify-center gap-2"
          >
            <Icon v-if="isProcessing" icon="ic:baseline-refresh" class="animate-spin" />
            {{ isProcessing ? 'Processing...' : 'Top Up' }}
          </button>
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
import { useWalletStore } from '@/stores/wallet/walletStore'
import AddPaymentMethodModal from '@/components/billing/AddPaymentMethodModal.vue'

const props = defineProps({
  wallet: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'topped-up'])

const walletStore = useWalletStore()

// State
const selectedAmount = ref(null)
const customAmount = ref('')
const selectedPaymentMethod = ref(null)
const agreeToTerms = ref(false)
const isProcessing = ref(false)
const errorMessage = ref('')
const showAddPaymentMethod = ref(false)

// Quick amount options
const quickAmounts = [10, 25, 50, 100, 250, 500]

// Mock payment methods (in real app, these would come from API)
const paymentMethods = ref([
  {
    id: 'card_1',
    type: 'CREDIT_CARD',
    description: 'Visa ending in 4242',
    last4: '4242',
    isDefault: true
  },
  {
    id: 'card_2',
    type: 'CREDIT_CARD',
    description: 'Mastercard ending in 5555',
    last4: '5555',
    isDefault: false
  },
  {
    id: 'paypal_1',
    type: 'PAYPAL',
    description: 'PayPal account',
    email: 'user@example.com',
    isDefault: false
  }
])

// Computed
const finalAmount = computed(() => {
  const amount = selectedAmount.value || parseFloat(customAmount.value) || 0
  return Math.max(amount, 1) // Minimum $1
})

const processingFee = computed(() => {
  const amount = finalAmount.value
  if (amount <= 0) return 0
  
  // 2.9% + $0.30 fee (typical Stripe fee)
  return Math.max((amount * 0.029) + 0.30, 0)
})

const totalAmount = computed(() => {
  return finalAmount.value + processingFee.value
})

const canProcess = computed(() => {
  return finalAmount.value > 0 && 
         selectedPaymentMethod.value && 
         agreeToTerms.value && 
         !errorMessage.value && 
         !isProcessing.value
})

// Methods
const onCustomAmountChange = () => {
  selectedAmount.value = null // Clear quick amount selection
  errorMessage.value = ''
  
  const amount = parseFloat(customAmount.value)
  if (amount && amount < 1) {
    errorMessage.value = 'Minimum top-up amount is $1'
  } else if (amount && amount > 10000) {
    errorMessage.value = 'Maximum top-up amount is $10,000'
  } else {
    errorMessage.value = ''
  }
}

const getPaymentMethodIcon = (type) => {
  const iconMap = {
    'CREDIT_CARD': 'ic:baseline-credit-card',
    'DEBIT_CARD': 'ic:baseline-credit-card',
    'PAYPAL': 'ic:baseline-paypal',
    'BANK_TRANSFER': 'ic:baseline-account-balance',
    'CRYPTO': 'ic:baseline-currency-bitcoin'
  }
  return iconMap[type] || 'ic:baseline-credit-card'
}

const getPaymentMethodName = (method) => {
  if (method.type === 'CREDIT_CARD' || method.type === 'DEBIT_CARD') {
    return `${method.type === 'CREDIT_CARD' ? 'Credit Card' : 'Debit Card'} ending in ${method.last4}`
  }
  if (method.type === 'PAYPAL') {
    return `PayPal (${method.email})`
  }
  return method.description || method.type
}

const formatCurrency = (amount) => {
  if (!amount) return '0.00'
  return new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(amount)
}

const processTopup = async () => {
  if (!canProcess.value) return
  
  isProcessing.value = true
  
  try {
    const result = await walletStore.topup(
      props.wallet.id,
      Math.round(finalAmount.value * 100), // Convert to cents
      selectedPaymentMethod.value
    )
    
    emit('topped-up', result)
    emit('close')
    
    // Reset form
    selectedAmount.value = null
    customAmount.value = ''
    selectedPaymentMethod.value = null
    agreeToTerms.value = false
    errorMessage.value = ''
    
  } catch (error) {
    console.error('Topup failed:', error)
    errorMessage.value = error.response?.data?.message || 'Topup failed. Please try again.'
  } finally {
    isProcessing.value = false
  }
}

const onPaymentMethodAdded = (newMethod) => {
  showAddPaymentMethod.value = false
  paymentMethods.value.push(newMethod)
  selectedPaymentMethod.value = newMethod.id
}

// Initialize
if (paymentMethods.value.length > 0 && !selectedPaymentMethod.value) {
  const defaultMethod = paymentMethods.value.find(m => m.isDefault)
  selectedPaymentMethod.value = defaultMethod?.id || paymentMethods.value[0].id
}
</script>

<style scoped>
.modal-backdrop {
  backdrop-filter: blur(4px);
}

input[type="number"]::-webkit-inner-spin-button,
input[type="number"]::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

input[type="number"] {
  -moz-appearance: textfield;
}
</style>
