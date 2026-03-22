<template>
  <div class="topup-page min-h-screen bg-gray-50 dark:bg-gray-900">
    <!-- Header -->
    <div class="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Top Up Wallet</h1>
            <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
              Add funds to your wallet
            </p>
          </div>
          <button
            @click="$router.push('/wallet/dashboard')"
            class="flex items-center px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700"
          >
            <Icon icon="mdi:arrow-left" class="w-4 h-4 mr-2" />
            Back to Wallet
          </button>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- Wallet Selection -->
        <div class="lg:col-span-1">
          <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">Select Wallet</h2>
            
            <div class="space-y-3">
              <div
                v-for="wallet in wallets"
                :key="wallet.id"
                @click="selectedWallet = wallet"
                :class="[
                  'p-4 rounded-lg border-2 cursor-pointer transition-colors',
                  selectedWallet?.id === wallet.id
                    ? 'border-primary-500 bg-primary-50 dark:bg-primary-900/20'
                    : 'border-gray-200 dark:border-gray-600 hover:border-gray-300 dark:hover:border-gray-500'
                ]"
              >
                <div class="flex items-center justify-between">
                  <div class="flex items-center space-x-3">
                    <div class="w-10 h-10 rounded-full flex items-center justify-center"
                         :class="getCurrencyGradient(wallet.currency)">
                      <Icon :icon="getCurrencyIcon(wallet.currency)" class="w-5 h-5 text-white" />
                    </div>
                    <div>
                      <p class="font-medium text-gray-900 dark:text-white">{{ wallet.currency }} Wallet</p>
                      <p class="text-sm text-gray-500 dark:text-gray-400">
                        {{ formatCurrency(wallet.balance, wallet.currency) }}
                      </p>
                    </div>
                  </div>
                  <div v-if="selectedWallet?.id === wallet.id" class="w-5 h-5 bg-primary-500 rounded-full flex items-center justify-center">
                    <Icon icon="mdi:check" class="w-3 h-3 text-white" />
                  </div>
                </div>
              </div>
            </div>

            <!-- Add New Wallet -->
            <button
              @click="showAddWalletModal = true"
              class="w-full mt-4 flex items-center justify-center px-4 py-2 border-2 border-dashed border-gray-300 dark:border-gray-600 rounded-md text-sm font-medium text-gray-700 dark:text-gray-300 hover:border-gray-400 dark:hover:border-gray-500"
            >
              <Icon icon="mdi:plus" class="w-4 h-4 mr-2" />
              Add New Wallet
            </button>
          </div>
        </div>

        <!-- Topup Form -->
        <div class="lg:col-span-2">
          <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-6">Top Up Amount</h2>
            
            <form @submit.prevent="handleTopup">
              <!-- Quick Amount Selection -->
              <div class="mb-6">
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">
                  Quick Amount
                </label>
                <div class="grid grid-cols-4 gap-3">
                  <button
                    v-for="amount in quickAmounts"
                    :key="amount"
                    type="button"
                    @click="selectAmount(amount)"
                    :class="[
                      'py-3 px-4 rounded-lg font-medium transition-colors',
                      selectedAmount === amount
                        ? 'bg-primary-600 text-white'
                        : 'bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'
                    ]"
                  >
                    {{ formatCurrency(amount, selectedWallet?.currency || 'USD') }}
                  </button>
                </div>
              </div>

              <!-- Custom Amount -->
              <div class="mb-6">
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Custom Amount
                </label>
                <div class="relative">
                  <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <span class="text-gray-500 dark:text-gray-400 sm:text-sm">{{ selectedWallet?.currency || 'USD' }}</span>
                  </div>
                  <input
                    v-model.number="customAmount"
                    type="number"
                    step="0.01"
                    min="1"
                    :max="maxAmount"
                    placeholder="Enter amount"
                    class="block w-full pl-12 pr-3 py-3 border border-gray-300 dark:border-gray-600 rounded-lg shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm dark:bg-gray-700 dark:text-white"
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
                  class="block w-full px-3 py-3 border border-gray-300 dark:border-gray-600 rounded-lg shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm dark:bg-gray-700 dark:text-white"
                >
                  <option value="">Select payment method</option>
                  <option v-for="method in paymentMethods" :key="method.id" :value="method.id">
                    {{ method.type }} - {{ method.last4 }}
                  </option>
                </select>
                <button
                  type="button"
                  @click="showAddPaymentModal = true"
                  class="mt-2 text-sm text-primary-600 dark:text-primary-400 hover:text-primary-700 dark:hover:text-primary-300"
                >
                  <Icon icon="mdi:plus" class="w-4 h-4 mr-1 inline" />
                  Add Payment Method
                </button>
              </div>

              <!-- Summary -->
              <div v-if="finalAmount > 0" class="mb-6 p-4 bg-gray-50 dark:bg-gray-700 rounded-lg">
                <h3 class="text-sm font-medium text-gray-900 dark:text-white mb-3">Top Up Summary</h3>
                <div class="space-y-2">
                  <div class="flex justify-between text-sm">
                    <span class="text-gray-600 dark:text-gray-400">Amount:</span>
                    <span class="font-medium text-gray-900 dark:text-white">
                      {{ formatCurrency(finalAmount, selectedWallet?.currency || 'USD') }}
                    </span>
                  </div>
                  <div class="flex justify-between text-sm">
                    <span class="text-gray-600 dark:text-gray-400">Processing Fee:</span>
                    <span class="font-medium text-gray-900 dark:text-white">
                      {{ formatCurrency(calculateFee(), selectedWallet?.currency || 'USD') }}
                    </span>
                  </div>
                  <div class="flex justify-between text-sm pt-2 border-t border-gray-200 dark:border-gray-600">
                    <span class="text-gray-600 dark:text-gray-400">Total:</span>
                    <span class="font-medium text-gray-900 dark:text-white">
                      {{ formatCurrency(finalAmount + calculateFee(), selectedWallet?.currency || 'USD') }}
                    </span>
                  </div>
                  <div class="flex justify-between text-sm pt-2">
                    <span class="text-gray-600 dark:text-gray-400">New Balance:</span>
                    <span class="font-medium text-green-600 dark:text-green-400">
                      {{ formatCurrency((selectedWallet?.balance || 0) + finalAmount, selectedWallet?.currency || 'USD') }}
                    </span>
                  </div>
                </div>
              </div>

              <!-- Action Buttons -->
              <div class="flex space-x-3">
                <button
                  type="button"
                  @click="$router.push('/wallet/dashboard')"
                  class="flex-1 px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg shadow-sm text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  :disabled="!isValid || loading"
                  class="flex-1 px-4 py-3 border border-transparent rounded-lg shadow-sm text-sm font-medium text-white bg-primary-600 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <Icon v-if="loading" icon="mdi:loading" class="w-4 h-4 mr-2 animate-spin" />
                  {{ loading ? 'Processing...' : 'Top Up Now' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>

      <!-- Recent Topups -->
      <div class="mt-8 bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">Recent Top Ups</h2>
        <div class="space-y-3">
          <TransactionItem
            v-for="transaction in recentTopups"
            :key="transaction.id"
            :transaction="transaction"
            compact
          />
        </div>
        <div v-if="recentTopups.length === 0" class="text-center py-8">
          <Icon icon="mdi:cash-plus" class="w-12 h-12 text-gray-400 mx-auto mb-3" />
          <p class="text-gray-500 dark:text-gray-400">No recent top ups</p>
        </div>
      </div>
    </div>

    <!-- Modals -->
    <AddWalletModal
      v-if="showAddWalletModal"
      @close="showAddWalletModal = false"
      @success="handleAddWallet"
    />
    
    <AddPaymentMethodModal
      v-if="showAddPaymentModal"
      @close="showAddPaymentModal = false"
      @add="handleAddPaymentMethod"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useWalletStore } from '@/stores/walletStore'
import { useBillingStore } from '@/stores/billingStore'
import TransactionItem from '@/components/wallet/TransactionItem.vue'
import AddWalletModal from '@/components/wallet/AddWalletModal.vue'
import AddPaymentMethodModal from '@/components/billing/AddPaymentMethodModal.vue'

const walletStore = useWalletStore()
const billingStore = useBillingStore()

// State
const selectedWallet = ref(null)
const selectedAmount = ref(null)
const customAmount = ref(null)
const selectedPaymentMethod = ref('')
const loading = ref(false)
const errorMessage = ref('')
const showAddWalletModal = ref(false)
const showAddPaymentModal = ref(false)

// Quick amount options
const quickAmounts = [10, 25, 50, 100, 250, 500]

// Computed
const wallets = computed(() => walletStore.balances)
const paymentMethods = computed(() => billingStore.paymentMethods)
const recentTopups = computed(() => 
  walletStore.transactions.filter(t => t.transactionType === 'TOPUP').slice(0, 5)
)

const finalAmount = computed(() => {
  return selectedAmount.value || customAmount.value || 0
})

const maxAmount = computed(() => 10000) // Maximum topup amount

const isValid = computed(() => {
  return selectedWallet.value &&
         finalAmount.value > 0 &&
         finalAmount.value <= maxAmount.value &&
         selectedPaymentMethod.value &&
         !errorMessage.value
})

// Methods
const selectAmount = (amount) => {
  selectedAmount.value = amount
  customAmount.value = null
  errorMessage.value = ''
}

const calculateFee = () => {
  // 2.9% + $0.30 processing fee (typical Stripe fee)
  if (!finalAmount.value) return 0
  const percentageFee = finalAmount.value * 0.029
  const fixedFee = 0.30
  return percentageFee + fixedFee
}

const formatCurrency = (amount, currency) => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: currency
  }).format(amount)
}

const getCurrencyIcon = (currency) => {
  const icons = {
    USD: 'mdi:currency-usd',
    EUR: 'mdi:currency-eur',
    GBP: 'mdi:currency-gbp',
    JPY: 'mdi:currency-jpy',
    VND: 'mdi:currency-sign'
  }
  return icons[currency] || 'mdi:currency-sign'
}

const getCurrencyGradient = (currency) => {
  const gradients = {
    USD: 'bg-gradient-to-br from-green-500 to-green-600',
    EUR: 'bg-gradient-to-br from-blue-500 to-blue-600',
    GBP: 'bg-gradient-to-br from-purple-500 to-purple-600',
    JPY: 'bg-gradient-to-br from-red-500 to-red-600',
    VND: 'bg-gradient-to-br from-orange-500 to-orange-600'
  }
  return gradients[currency] || 'bg-gradient-to-br from-gray-500 to-gray-600'
}

const handleTopup = async () => {
  if (!isValid.value) return

  loading.value = true
  errorMessage.value = ''

  try {
    await walletStore.topup(selectedWallet.value.id, finalAmount.value, selectedPaymentMethod.value)
    
    // Reset form
    selectedAmount.value = null
    customAmount.value = null
    selectedPaymentMethod.value = ''
    
    // Show success message (you could use a toast notification here)
    alert('Top up successful!')
    
    // Navigate back to wallet dashboard
    setTimeout(() => {
      $router.push('/wallet/dashboard')
    }, 1500)
  } catch (error) {
    errorMessage.value = error.message || 'Failed to top up wallet'
  } finally {
    loading.value = false
  }
}

const handleAddWallet = (wallet) => {
  showAddWalletModal.value = false
  // Select the newly added wallet
  selectedWallet.value = wallet
}

const handleAddPaymentMethod = (paymentMethod) => {
  showAddPaymentModal.value = false
  // Select the newly added payment method
  selectedPaymentMethod.value = paymentMethod.id
}

// Watch for validation
const validateAmount = () => {
  if (customAmount.value && customAmount.value < 1) {
    errorMessage.value = 'Minimum amount is $1'
  } else if (customAmount.value && customAmount.value > maxAmount.value) {
    errorMessage.value = `Maximum amount is ${formatCurrency(maxAmount.value, 'USD')}`
  } else {
    errorMessage.value = ''
  }
}

// Watch custom amount
const unwatchCustomAmount = computed(() => customAmount.value)
unwatchCustomAmount.value && unwatchCustomAmount.value(() => validateAmount())

// Lifecycle
onMounted(async () => {
  // Only fetch wallets if not initialized
  if (!walletStore.isInitialized) {
    await walletStore.fetchWallets()
  }
  await billingStore.fetchPaymentMethods()
  
  // Select first wallet by default
  if (wallets.value.length > 0 && !selectedWallet.value) {
    selectedWallet.value = wallets.value[0]
  }
})
</script>
