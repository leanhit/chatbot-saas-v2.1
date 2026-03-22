<template>
  <div class="transfer-page min-h-screen bg-gray-50 dark:bg-gray-900">
    <!-- Header -->
    <div class="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Transfer Funds</h1>
            <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
              Transfer funds between your wallets
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
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <!-- From Wallet -->
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">From Wallet</h2>
          
          <div class="space-y-3">
            <div
              v-for="wallet in wallets"
              :key="wallet.id"
              @click="fromWallet = wallet"
              :class="[
                'p-4 rounded-lg border-2 cursor-pointer transition-colors',
                fromWallet?.id === wallet.id
                  ? 'border-red-500 bg-red-50 dark:bg-red-900/20'
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
                      Available: {{ formatCurrency(wallet.balance, wallet.currency) }}
                    </p>
                  </div>
                </div>
                <div v-if="fromWallet?.id === wallet.id" class="w-5 h-5 bg-red-500 rounded-full flex items-center justify-center">
                  <Icon icon="mdi:minus" class="w-3 h-3 text-white" />
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- To Wallet -->
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">To Wallet</h2>
          
          <div class="space-y-3">
            <div
              v-for="wallet in availableToWallets"
              :key="wallet.id"
              @click="toWallet = wallet"
              :class="[
                'p-4 rounded-lg border-2 cursor-pointer transition-colors',
                toWallet?.id === wallet.id
                  ? 'border-green-500 bg-green-50 dark:bg-green-900/20'
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
                      Current: {{ formatCurrency(wallet.balance, wallet.currency) }}
                    </p>
                  </div>
                </div>
                <div v-if="toWallet?.id === wallet.id" class="w-5 h-5 bg-green-500 rounded-full flex items-center justify-center">
                  <Icon icon="mdi:plus" class="w-3 h-3 text-white" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Transfer Form -->
      <div class="mt-8 bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-6">Transfer Details</h2>
        
        <form @submit.prevent="handleTransfer">
          <!-- Amount -->
          <div class="mb-6">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Transfer Amount
            </label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <span class="text-gray-500 dark:text-gray-400 sm:text-sm">{{ fromWallet?.currency }}</span>
              </div>
              <input
                v-model.number="amount"
                type="number"
                step="0.01"
                min="0.01"
                :max="fromWallet?.balance"
                placeholder="Enter amount"
                class="block w-full pl-12 pr-3 py-3 border border-gray-300 dark:border-gray-600 rounded-lg shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm dark:bg-gray-700 dark:text-white"
              />
            </div>
            <div class="mt-2 flex items-center justify-between text-sm">
              <span class="text-gray-500 dark:text-gray-400">
                Available: {{ formatCurrency(fromWallet?.balance || 0, fromWallet?.currency) }}
              </span>
              <span class="text-gray-500 dark:text-gray-400">
                Fee: {{ formatCurrency(calculateFee(), fromWallet?.currency) }}
              </span>
            </div>
            <p v-if="errorMessage" class="mt-2 text-sm text-red-600 dark:text-red-400">
              {{ errorMessage }}
            </p>
          </div>

          <!-- Quick Amount Buttons -->
          <div class="mb-6">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Quick Amount
            </label>
            <div class="grid grid-cols-4 gap-3">
              <button
                v-for="percentage in [25, 50, 75, 100]"
                :key="percentage"
                type="button"
                @click="setPercentageAmount(percentage)"
                class="py-2 px-3 bg-gray-100 dark:bg-gray-700 rounded-lg text-sm font-medium text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600"
              >
                {{ percentage }}%
              </button>
            </div>
          </div>

          <!-- Description -->
          <div class="mb-6">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Description (Optional)
            </label>
            <input
              v-model="description"
              type="text"
              placeholder="What's this transfer for?"
              class="block w-full px-3 py-3 border border-gray-300 dark:border-gray-600 rounded-lg shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm dark:bg-gray-700 dark:text-white"
            />
          </div>

          <!-- Exchange Rate Info -->
          <div v-if="fromWallet && toWallet && fromWallet.currency !== toWallet.currency" class="mb-6 p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
            <h3 class="text-sm font-medium text-blue-900 dark:text-blue-200 mb-2">Exchange Information</h3>
            <div class="space-y-1 text-sm text-blue-700 dark:text-blue-300">
              <p>Exchange Rate: 1 {{ fromWallet.currency }} = {{ getExchangeRate() }} {{ toWallet.currency }}</p>
              <p>You will receive: {{ formatCurrency(calculateReceivedAmount(), toWallet.currency) }}</p>
              <p class="text-xs mt-2">Exchange rates are updated in real-time</p>
            </div>
          </div>

          <!-- Transfer Summary -->
          <div v-if="amount > 0 && toWallet" class="mb-6 p-4 bg-gray-50 dark:bg-gray-700 rounded-lg">
            <h3 class="text-sm font-medium text-gray-900 dark:text-white mb-3">Transfer Summary</h3>
            <div class="space-y-2 text-sm">
              <div class="flex justify-between">
                <span class="text-gray-600 dark:text-gray-400">From:</span>
                <span class="font-medium text-gray-900 dark:text-white">
                  {{ fromWallet?.currency }} Wallet ({{ formatCurrency(amount, fromWallet?.currency) }})
                </span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600 dark:text-gray-400">To:</span>
                <span class="font-medium text-gray-900 dark:text-white">
                  {{ toWallet?.currency }} Wallet ({{ formatCurrency(calculateReceivedAmount(), toWallet?.currency) }})
                </span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600 dark:text-gray-400">Fee:</span>
                <span class="font-medium text-gray-900 dark:text-white">
                  {{ formatCurrency(calculateFee(), fromWallet?.currency) }}
                </span>
              </div>
              <div class="flex justify-between border-t border-gray-200 dark:border-gray-600 pt-2">
                <span class="text-gray-600 dark:text-gray-400">Total:</span>
                <span class="font-medium text-gray-900 dark:text-white">
                  {{ formatCurrency(amount + calculateFee(), fromWallet?.currency) }}
                </span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600 dark:text-gray-400">Remaining Balance:</span>
                <span class="font-medium text-gray-900 dark:text-white">
                  {{ formatCurrency((fromWallet?.balance || 0) - amount - calculateFee(), fromWallet?.currency) }}
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
              {{ loading ? 'Processing...' : 'Transfer Now' }}
            </button>
          </div>
        </form>
      </div>

      <!-- Recent Transfers -->
      <div class="mt-8 bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">Recent Transfers</h2>
        <div class="space-y-3">
          <TransactionItem
            v-for="transaction in recentTransfers"
            :key="transaction.id"
            :transaction="transaction"
            compact
          />
        </div>
        <div v-if="recentTransfers.length === 0" class="text-center py-8">
          <Icon icon="mdi:swap-horizontal" class="w-12 h-12 text-gray-400 mx-auto mb-3" />
          <p class="text-gray-500 dark:text-gray-400">No recent transfers</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useWalletStore } from '@/stores/walletStore'
import { formatCurrency } from '@/utils/currency'
import TransactionItem from '@/components/wallet/TransactionItem.vue'

const walletStore = useWalletStore()

// State
const fromWallet = ref(null)
const toWallet = ref(null)
const amount = ref(null)
const description = ref('')
const loading = ref(false)
const errorMessage = ref('')

// Computed
const wallets = computed(() => walletStore.balances)
const availableToWallets = computed(() => 
  wallets.value.filter(w => w.id !== fromWallet.value?.id)
)

const recentTransfers = computed(() => 
  walletStore.transactions.filter(t => 
    t.transactionType === 'TRANSFER_OUT' || t.transactionType === 'TRANSFER_IN'
  ).slice(0, 5)
)

const isValid = computed(() => {
  return fromWallet.value &&
         toWallet.value &&
         amount.value > 0 &&
         amount.value <= (fromWallet.value?.balance || 0) &&
         !errorMessage.value
})

// Mock exchange rates (in real app, these would come from an API)
const exchangeRates = {
  'USD-EUR': 0.92,
  'USD-GBP': 0.79,
  'USD-JPY': 149.50,
  'USD-VND': 24500,
  'EUR-USD': 1.09,
  'EUR-GBP': 0.86,
  'EUR-JPY': 162.89,
  'EUR-VND': 26739,
  'GBP-USD': 1.27,
  'GBP-EUR': 1.16,
  'GBP-JPY': 189.50,
  'GBP-VND': 31100,
  'JPY-USD': 0.0067,
  'JPY-EUR': 0.0061,
  'JPY-GBP': 0.0053,
  'JPY-VND': 163.89,
  'VND-USD': 0.000041,
  'VND-EUR': 0.000037,
  'VND-GBP': 0.000032,
  'VND-JPY': 0.0061
}

// Methods
const setPercentageAmount = (percentage) => {
  if (fromWallet.value) {
    amount.value = (fromWallet.value.balance * percentage) / 100
    errorMessage.value = ''
  }
}

const calculateFee = () => {
  // 1% transfer fee with minimum $0.50
  if (!amount.value) return 0
  const fee = amount.value * 0.01
  return Math.max(fee, 0.50)
}

const getExchangeRate = () => {
  if (!fromWallet.value || !toWallet.value) return 1
  
  const key = `${fromWallet.value.currency}-${toWallet.value.currency}`
  return exchangeRates[key] || 1
}

const calculateReceivedAmount = () => {
  if (!amount.value || !fromWallet.value || !toWallet.value) return 0
  
  if (fromWallet.value.currency === toWallet.value.currency) {
    return amount.value
  }
  
  return amount.value * getExchangeRate()
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

const handleTransfer = async () => {
  if (!isValid.value) return

  loading.value = true
  errorMessage.value = ''

  try {
    await walletStore.transfer(fromWallet.value.id, toWallet.value.id, amount.value, description.value)
    
    // Reset form
    amount.value = null
    description.value = ''
    
    // Show success message
    alert('Transfer successful!')
    
    // Navigate back to wallet dashboard
    setTimeout(() => {
      $router.push('/wallet/dashboard')
    }, 1500)
  } catch (error) {
    errorMessage.value = error.message || 'Failed to transfer funds'
  } finally {
    loading.value = false
  }
}

const validateAmount = () => {
  if (amount.value && amount.value <= 0) {
    errorMessage.value = 'Amount must be greater than 0'
  } else if (amount.value > (fromWallet.value?.balance || 0)) {
    errorMessage.value = 'Insufficient balance'
  } else {
    errorMessage.value = ''
  }
}

// Watch amount changes
const unwatchAmount = computed(() => amount.value)
unwatchAmount.value && unwatchAmount.value(() => validateAmount())

// Lifecycle
onMounted(async () => {
  // Only fetch wallets if not initialized
  if (!walletStore.isInitialized) {
    await walletStore.fetchWallets()
  }
  
  // Select first wallet as default "from" wallet
  if (wallets.value.length >= 2) {
    fromWallet.value = wallets.value[0]
    toWallet.value = wallets.value[1]
  }
})
</script>
