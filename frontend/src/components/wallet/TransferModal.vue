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
                Transfer Funds
              </h3>
              <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
                Transfer funds to another wallet
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
            <!-- From Wallet -->
            <div class="mb-6">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                From Wallet
              </label>
              <div class="p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
                <div class="flex items-center justify-between">
                  <div class="flex items-center space-x-3">
                    <div class="w-8 h-8 bg-blue-100 dark:bg-blue-900 rounded-full flex items-center justify-center">
                      <Icon icon="mdi:wallet" class="w-4 h-4 text-blue-600 dark:text-blue-400" />
                    </div>
                    <div>
                      <p class="font-medium text-gray-900 dark:text-white">{{ fromWallet?.currency }} Wallet</p>
                      <p class="text-sm text-gray-500 dark:text-gray-400">Balance: {{ formatCurrency(fromWallet?.balance || 0, fromWallet?.currency) }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- To Wallet -->
            <div class="mb-6">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                To Wallet
              </label>
              <select
                v-model="toWalletId"
                class="block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm dark:bg-gray-700 dark:text-white"
              >
                <option value="">Select destination wallet</option>
                <option v-for="wallet in availableWallets" :key="wallet.id" :value="wallet.id">
                  {{ wallet.currency }} Wallet - {{ formatCurrency(wallet.balance, wallet.currency) }}
                </option>
              </select>
            </div>

            <!-- Amount -->
            <div class="mb-6">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Amount
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
                  class="block w-full pl-12 pr-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm dark:bg-gray-700 dark:text-white"
                />
              </div>
              <div class="mt-2 flex items-center justify-between text-sm">
                <span class="text-gray-500 dark:text-gray-400">Available: {{ formatCurrency(fromWallet?.balance || 0, fromWallet?.currency) }}</span>
                <span v-if="amount" class="text-gray-500 dark:text-gray-400">
                  Fee: {{ formatCurrency(calculateFee(), fromWallet?.currency) }}
                </span>
              </div>
              <p v-if="errorMessage" class="mt-2 text-sm text-red-600 dark:text-red-400">
                {{ errorMessage }}
              </p>
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
                class="block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm dark:bg-gray-700 dark:text-white"
              />
            </div>

            <!-- Summary -->
            <div v-if="amount && toWalletId" class="mb-6 p-4 bg-gray-50 dark:bg-gray-700 rounded-lg">
              <h4 class="text-sm font-medium text-gray-900 dark:text-white mb-3">Transfer Summary</h4>
              <div class="space-y-2 text-sm">
                <div class="flex justify-between">
                  <span class="text-gray-600 dark:text-gray-400">Amount:</span>
                  <span class="font-medium text-gray-900 dark:text-white">{{ formatCurrency(amount, fromWallet?.currency) }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-gray-600 dark:text-gray-400">Fee:</span>
                  <span class="font-medium text-gray-900 dark:text-white">{{ formatCurrency(calculateFee(), fromWallet?.currency) }}</span>
                </div>
                <div class="flex justify-between border-t border-gray-200 dark:border-gray-600 pt-2">
                  <span class="text-gray-600 dark:text-gray-400">Total:</span>
                  <span class="font-medium text-gray-900 dark:text-white">{{ formatCurrency(amount + calculateFee(), fromWallet?.currency) }}</span>
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
                {{ loading ? 'Processing...' : 'Transfer' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useWalletStore } from '@/stores/walletStore'

const props = defineProps({
  wallet: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'success'])

const walletStore = useWalletStore()

// State
const toWalletId = ref('')
const amount = ref(null)
const description = ref('')
const loading = ref(false)
const errorMessage = ref('')

// Computed
const fromWallet = computed(() => props.wallet)

const availableWallets = computed(() => {
  return walletStore.balances.filter(w => w.id !== props.wallet.id)
})

const isValid = computed(() => {
  return amount.value > 0 && 
         toWalletId.value && 
         amount.value <= (fromWallet.value?.balance || 0) &&
         !errorMessage.value
})

// Methods
const calculateFee = () => {
  // 1.5% fee with minimum $0.50
  if (!amount.value) return 0
  const fee = amount.value * 0.015
  return Math.max(fee, 0.50)
}

const formatCurrency = (amount, currency) => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: currency
  }).format(amount)
}

const validateAmount = () => {
  if (amount.value && amount.value <= 0) {
    errorMessage.value = 'Amount must be greater than 0'
  } else if (amount.value > (fromWallet.value?.balance || 0)) {
    errorMessage.value = 'Insufficient balance'
  } else if (amount.value && amount.value < 0.01) {
    errorMessage.value = 'Minimum amount is $0.01'
  } else {
    errorMessage.value = ''
  }
}

const handleSubmit = async () => {
  if (!isValid.value) return

  loading.value = true
  errorMessage.value = ''

  try {
    // Call wallet store to transfer
    await walletStore.transfer(props.wallet.id, toWalletId.value, amount.value, description.value)
    
    emit('success', {
      amount: amount.value,
      toWalletId: toWalletId.value,
      description: description.value
    })
  } catch (error) {
    errorMessage.value = error.message || 'Failed to transfer funds'
  } finally {
    loading.value = false
  }
}

// Watch amount changes
const unwatchAmount = computed(() => amount.value)
unwatchAmount.value && unwatchAmount.value(() => validateAmount())
</script>
