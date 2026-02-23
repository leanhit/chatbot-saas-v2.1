<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white dark:bg-gray-800 rounded-lg max-w-md w-full mx-4 max-h-[90vh] overflow-y-auto">
      <!-- Header -->
      <div class="p-6 border-b border-gray-200 dark:border-gray-700">
        <div class="flex justify-between items-center">
          <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100">
            Transfer Funds
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
              <p class="text-sm text-blue-600 dark:text-blue-400 font-medium">Available Balance</p>
              <p class="text-2xl font-bold text-blue-900 dark:text-blue-100">
                ${{ formatCurrency(availableBalance) }}
              </p>
            </div>
            <div class="p-2 bg-blue-100 dark:bg-blue-900/30 rounded-lg">
              <Icon icon="ic:baseline-account-balance-wallet" class="text-blue-600 dark:text-blue-400 text-2xl" />
            </div>
          </div>
        </div>

        <!-- Transfer Type -->
        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Transfer Type
          </label>
          <div class="grid grid-cols-2 gap-3">
            <button 
              v-for="type in transferTypes" 
              :key="type.value"
              @click="transferType = type.value"
              :class="[
                'p-3 rounded-lg border-2 font-medium transition-colors',
                transferType === type.value 
                  ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400' 
                  : 'border-gray-200 dark:border-gray-600 hover:border-gray-300 dark:hover:border-gray-500'
              ]"
            >
              <Icon :icon="type.icon" class="text-xl mb-2" />
              <p class="text-sm">{{ type.label }}</p>
            </button>
          </div>
        </div>

        <!-- To Wallet (Internal Transfer) -->
        <div v-if="transferType === 'INTERNAL'" class="mb-6">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            To Wallet
          </label>
          <select 
            v-model="toWalletId"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            <option value="">Select destination wallet</option>
            <option 
              v-for="wallet in availableWallets" 
              :key="wallet.id"
              :value="wallet.id"
              :disabled="wallet.id === fromWallet?.id"
            >
              {{ wallet.name }} - ${{ formatCurrency(wallet.balance) }} ({{ wallet.currency }})
            </option>
          </select>
        </div>

        <!-- To External (External Transfer) -->
        <div v-if="transferType === 'EXTERNAL'" class="space-y-4 mb-6">
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Recipient Email
            </label>
            <input 
              v-model="recipientEmail"
              type="email"
              placeholder="Enter recipient email"
              class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Recipient Name
            </label>
            <input 
              v-model="recipientName"
              type="text"
              placeholder="Enter recipient name"
              class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
        </div>

        <!-- Amount -->
        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Amount
          </label>
          
          <!-- Quick Amount Buttons -->
          <div class="grid grid-cols-4 gap-2 mb-4">
            <button 
              v-for="amount in quickAmounts" 
              :key="amount"
              @click="transferAmount = amount"
              :class="[
                'p-2 rounded-lg border-2 font-medium transition-colors',
                transferAmount === amount 
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
              placeholder="Enter amount"
              min="1"
              step="0.01"
              class="w-full pl-8 pr-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent text-lg"
              @input="onAmountChange"
            />
          </div>
          <p v-if="errorMessage" class="text-red-600 dark:text-red-400 text-sm mt-2">
            {{ errorMessage }}
          </p>
        </div>

        <!-- Transfer Fee -->
        <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4 mb-6">
          <h4 class="font-medium text-gray-900 dark:text-gray-100 mb-3">Transfer Details</h4>
          <div class="space-y-2">
            <div class="flex justify-between text-sm">
              <span class="text-gray-600 dark:text-gray-400">Transfer Amount:</span>
              <span class="font-medium text-gray-900 dark:text-gray-100">
                ${{ formatCurrency(finalAmount) }}
              </span>
            </div>
            <div class="flex justify-between text-sm">
              <span class="text-gray-600 dark:text-gray-400">Transfer Fee:</span>
              <span class="font-medium text-gray-900 dark:text-gray-100">
                ${{ formatCurrency(transferFee) }}
              </span>
            </div>
            <div class="border-t border-gray-200 dark:border-gray-600 pt-2">
              <div class="flex justify-between">
                <span class="font-medium text-gray-900 dark:text-gray-100">Total Amount:</span>
                <span class="font-bold text-lg text-gray-900 dark:text-gray-100">
                  ${{ formatCurrency(totalAmount) }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Description -->
        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Description (Optional)
          </label>
          <textarea 
            v-model="description"
            rows="3"
            placeholder="Add a note for this transfer"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          ></textarea>
        </div>

        <!-- 2FA Verification -->
        <div v-if="requires2FA" class="mb-6">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Two-Factor Authentication Code
          </label>
          <input 
            v-model="twoFACode"
            type="text"
            placeholder="Enter 6-digit code"
            maxlength="6"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
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
            @click="processTransfer"
            :disabled="!canTransfer"
            class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center justify-center gap-2"
          >
            <Icon v-if="isProcessing" icon="ic:baseline-refresh" class="animate-spin" />
            {{ isProcessing ? 'Processing...' : 'Transfer' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useWalletStore } from '@/stores/wallet/walletStore'

const props = defineProps({
  wallet: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'transferred'])

const walletStore = useWalletStore()

// State
const transferType = ref('INTERNAL')
const toWalletId = ref('')
const recipientEmail = ref('')
const recipientName = ref('')
const transferAmount = ref(null)
const customAmount = ref('')
const description = ref('')
const twoFACode = ref('')
const isProcessing = ref(false)
const errorMessage = ref('')
const requires2FA = ref(false)

// Transfer types
const transferTypes = [
  { value: 'INTERNAL', label: 'Internal Transfer', icon: 'ic:baseline-swap-horiz' },
  { value: 'EXTERNAL', label: 'External Transfer', icon: 'ic:baseline-send' }
]

// Quick amount options
const quickAmounts = [10, 25, 50, 100, 250, 500]

// Computed
const availableBalance = computed(() => props.wallet?.availableBalance || props.wallet?.balance || 0)

const availableWallets = computed(() => {
  return walletStore.wallets.filter(w => w.id !== props.wallet.id)
})

const finalAmount = computed(() => {
  const amount = transferAmount.value || parseFloat(customAmount.value) || 0
  return Math.max(amount, 1) // Minimum $1
})

const transferFee = computed(() => {
  const amount = finalAmount.value
  if (amount <= 0) return 0
  
  // Internal transfers: $0.50 flat fee
  // External transfers: 2% + $0.50
  if (transferType.value === 'INTERNAL') {
    return 50 // $0.50 in cents
  } else {
    return Math.max((amount * 0.02) + 50, 100) // Min $1.00 fee
  }
})

const totalAmount = computed(() => {
  return finalAmount.value + transferFee.value
})

const canTransfer = computed(() => {
  const hasValidRecipient = transferType.value === 'INTERNAL' 
    ? toWalletId.value 
    : recipientEmail.value && recipientName.value
  
  return finalAmount.value > 0 && 
         finalAmount.value <= availableBalance.value &&
         hasValidRecipient && 
         !errorMessage.value && 
         !isProcessing.value &&
         (requires2FA.value ? twoFACode.value.length === 6 : true)
})

// Methods
const onAmountChange = () => {
  transferAmount.value = null // Clear quick amount selection
  errorMessage.value = ''
  
  const amount = parseFloat(customAmount.value)
  if (amount && amount < 1) {
    errorMessage.value = 'Minimum transfer amount is $1'
  } else if (amount && amount > availableBalance.value) {
    errorMessage.value = 'Insufficient balance'
  } else {
    errorMessage.value = ''
  }
}

const formatCurrency = (amount) => {
  if (!amount) return '0.00'
  return new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(amount / 100) // Convert from cents
}

const processTransfer = async () => {
  if (!canTransfer.value) return
  
  isProcessing.value = true
  
  try {
    const transferData = {
      fromWalletId: props.wallet.id,
      amount: Math.round(finalAmount.value * 100), // Convert to cents
      description: description.value
    }
    
    if (transferType.value === 'INTERNAL') {
      transferData.toWalletId = toWalletId.value
      transferData.type = 'INTERNAL'
    } else {
      transferData.recipientEmail = recipientEmail.value
      transferData.recipientName = recipientName.value
      transferData.type = 'EXTERNAL'
    }
    
    if (requires2FA.value) {
      transferData.twoFACode = twoFACode.value
    }
    
    const result = await walletStore.transfer(
      props.wallet.id,
      transferData.toWalletId || transferData.recipientEmail,
      Math.round(finalAmount.value * 100),
      description.value
    )
    
    emit('transferred', result)
    emit('close')
    
    // Reset form
    resetForm()
    
  } catch (error) {
    console.error('Transfer failed:', error)
    errorMessage.value = error.response?.data?.message || 'Transfer failed. Please try again.'
    
    // Check if 2FA is required
    if (error.response?.status === 403 && error.response?.data?.requires2FA) {
      requires2FA.value = true
    }
  } finally {
    isProcessing.value = false
  }
}

const resetForm = () => {
  transferType.value = 'INTERNAL'
  toWalletId.value = ''
  recipientEmail.value = ''
  recipientName.value = ''
  transferAmount.value = null
  customAmount.value = ''
  description.value = ''
  twoFACode.value = ''
  errorMessage.value = ''
  requires2FA.value = false
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
