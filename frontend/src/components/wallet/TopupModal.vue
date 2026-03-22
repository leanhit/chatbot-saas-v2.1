<template>
  <div class="fixed inset-0 z-50 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
      <!-- Background overlay -->
      <div class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" @click="$emit('close')"></div>

      <!-- Modal panel -->
      <div class="inline-block align-bottom bg-white dark:bg-gray-800 rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
        <!-- Header -->
        <div class="bg-white dark:bg-gray-800 px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
          <div class="flex items-center justify-between">
            <div>
              <h3 class="text-lg leading-6 font-medium text-gray-900 dark:text-white">
                Top Up Wallet
              </h3>
              <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
                Add funds to your {{ wallet?.currency || 'USD' }} wallet
              </p>
            </div>
            <button
              @click="$emit('close')"
              class="text-gray-400 hover:text-gray-500 dark:hover:text-gray-300"
            >
              <Icon icon="mdi:close" class="w-6 h-6" />
            </button>
          </div>
        </div>

        <!-- Body -->
        <div class="bg-white dark:bg-gray-800 px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
          <form @submit.prevent="handleSubmit">
            <!-- Amount Selection -->
            <div class="mb-6">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Amount
              </label>
              <div class="grid grid-cols-4 gap-2 mb-3">
                <button
                  v-for="amount in quickAmounts"
                  :key="amount"
                  type="button"
                  @click="selectAmount(amount)"
                  :class="[
                    'py-2 px-3 rounded-md text-sm font-medium transition-colors',
                    selectedAmount === amount
                      ? 'bg-primary-600 text-white'
                      : 'bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'
                  ]"
                >
                  {{ formatCurrency(amount, wallet?.currency || 'USD') }}
                </button>
              </div>
              <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <span class="text-gray-500 dark:text-gray-400 sm:text-sm">{{ wallet?.currency || 'USD' }}</span>
                </div>
                <input
                  v-model.number="customAmount"
                  type="number"
                  step="0.01"
                  min="1"
                  placeholder="Enter custom amount"
                  class="block w-full pl-12 pr-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm dark:bg-gray-700 dark:text-white"
                />
              </div>
              <p v-if="errorMessage" class="mt-2 text-sm text-red-600 dark:text-red-400">
                {{ errorMessage }}
              </p>
            </div>

            <!-- Payment Method -->
            <div class="mb-6">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Payment Method
              </label>
              <select
                v-model="selectedPaymentMethod"
                class="block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm dark:bg-gray-700 dark:text-white"
              >
                <option value="">Select payment method</option>
                <option v-for="method in paymentMethods" :key="method.id" :value="method.id">
                  {{ method.type }} - {{ method.last4 }}
                </option>
              </select>
              <button
                type="button"
                @click="showAddPaymentMethod = true"
                class="mt-2 text-sm text-primary-600 dark:text-primary-400 hover:text-primary-700 dark:hover:text-primary-300"
              >
                <Icon icon="mdi:plus" class="w-4 h-4 mr-1 inline" />
                Add Payment Method
              </button>
            </div>

            <!-- Current Balance -->
            <div class="mb-6 p-4 bg-gray-50 dark:bg-gray-700 rounded-lg">
              <div class="flex items-center justify-between">
                <span class="text-sm text-gray-600 dark:text-gray-400">Current Balance</span>
                <span class="text-lg font-semibold text-gray-900 dark:text-white">
                  {{ formatCurrency(wallet?.balance || 0, wallet?.currency || 'USD') }}
                </span>
              </div>
              <div class="flex items-center justify-between mt-2">
                <span class="text-sm text-gray-600 dark:text-gray-400">After Top Up</span>
                <span class="text-lg font-semibold text-green-600 dark:text-green-400">
                  {{ formatCurrency((wallet?.balance || 0) + getFinalAmount(), wallet?.currency || 'USD') }}
                </span>
              </div>
            </div>

            <!-- Action Buttons -->
            <div class="flex space-x-3">
              <button
                type="button"
                @click="$emit('close')"
                class="flex-1 px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
              >
                Cancel
              </button>
              <button
                type="submit"
                :disabled="loading || !isValid"
                class="flex-1 px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-primary-600 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <Icon v-if="loading" icon="mdi:loading" class="w-4 h-4 mr-2 animate-spin" />
                {{ loading ? 'Processing...' : 'Top Up' }}
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- Add Payment Method Modal -->
      <AddPaymentMethodModal
        v-if="showAddPaymentMethod"
        @close="showAddPaymentMethod = false"
        @add="handleAddPaymentMethod"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useWalletStore } from '@/stores/walletStore'
import AddPaymentMethodModal from '@/components/billing/AddPaymentMethodModal.vue'

const props = defineProps({
  wallet: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'success'])

const walletStore = useWalletStore()

// State
const selectedAmount = ref(null)
const customAmount = ref(null)
const selectedPaymentMethod = ref('')
const loading = ref(false)
const errorMessage = ref('')
const showAddPaymentMethod = ref(false)

// Quick amount options
const quickAmounts = [10, 25, 50, 100]

// Mock payment methods - in real app would come from store
const paymentMethods = ref([
  { id: 1, type: 'Credit Card', last4: '4242' },
  { id: 2, type: 'PayPal', last4: 'example@paypal.com' }
])

// Computed
const finalAmount = computed(() => {
  return selectedAmount.value || customAmount.value || 0
})

const isValid = computed(() => {
  return finalAmount.value > 0 && selectedPaymentMethod.value && !errorMessage.value
})

// Methods
const selectAmount = (amount) => {
  selectedAmount.value = amount
  customAmount.value = null
  errorMessage.value = ''
}

const getFinalAmount = () => {
  return finalAmount.value
}

const formatCurrency = (amount, currency) => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: currency
  }).format(amount)
}

const handleAddPaymentMethod = (paymentMethod) => {
  paymentMethods.value.push(paymentMethod)
  selectedPaymentMethod.value = paymentMethod.id
  showAddPaymentMethod.value = false
}

const handleSubmit = async () => {
  if (!isValid.value) return

  loading.value = true
  errorMessage.value = ''

  try {
    // Call wallet store to top up
    await walletStore.topup(props.wallet.id, finalAmount.value, selectedPaymentMethod.value)
    
    emit('success', {
      amount: finalAmount.value,
      paymentMethod: selectedPaymentMethod.value
    })
  } catch (error) {
    errorMessage.value = error.message || 'Failed to top up wallet'
  } finally {
    loading.value = false
  }
}

// Watch for custom amount changes
const validateAmount = () => {
  if (customAmount.value && customAmount.value < 1) {
    errorMessage.value = 'Minimum amount is $1'
  } else if (customAmount.value && customAmount.value > 10000) {
    errorMessage.value = 'Maximum amount is $10,000'
  } else {
    errorMessage.value = ''
  }
}

// Watch custom amount
const unwatchCustomAmount = computed(() => customAmount.value)
unwatchCustomAmount.value && unwatchCustomAmount.value(() => validateAmount())
</script>
