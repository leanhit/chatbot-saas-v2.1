<template>
  <div class="transaction-item border-b border-gray-200 dark:border-gray-700 last:border-b-0">
    <div class="p-4">
      <div class="flex items-center justify-between">
        <!-- Transaction Icon & Info -->
        <div class="flex items-center space-x-3">
          <div class="w-10 h-10 rounded-full flex items-center justify-center"
               :class="getIconBackgroundClass()">
            <Icon :icon="getTransactionIcon()" class="w-5 h-5" :class="getIconClass()" />
          </div>
          <div>
            <p class="font-medium text-gray-900 dark:text-white">
              {{ transaction.description || getTransactionTypeText() }}
            </p>
            <p class="text-sm text-gray-500 dark:text-gray-400">
              {{ formatDate(transaction.createdAt) }}
            </p>
            <p v-if="transaction.externalReference" class="text-xs text-gray-400 dark:text-gray-500">
              Ref: {{ transaction.externalReference }}
            </p>
          </div>
        </div>

        <!-- Amount & Status -->
        <div class="text-right">
          <p class="font-semibold" :class="getAmountClass()">
            {{ getAmountPrefix() }}{{ formatCurrency(transaction.amount, transaction.currency) }}
          </p>
          <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium mt-1"
                :class="getStatusClass()">
            {{ transaction.status }}
          </span>
        </div>
      </div>

      <!-- Expandable Details (if not compact) -->
      <div v-if="!compact && showDetails" class="mt-4 pt-4 border-t border-gray-200 dark:border-gray-700">
        <div class="grid grid-cols-2 gap-4 text-sm">
          <div>
            <span class="text-gray-500 dark:text-gray-400">Transaction ID:</span>
            <span class="ml-2 font-mono text-gray-900 dark:text-white">{{ transaction.transactionReference }}</span>
          </div>
          <div>
            <span class="text-gray-500 dark:text-gray-400">Wallet ID:</span>
            <span class="ml-2 font-mono text-gray-900 dark:text-white">#{{ transaction.walletId }}</span>
          </div>
          <div>
            <span class="text-gray-500 dark:text-gray-400">Type:</span>
            <span class="ml-2 text-gray-900 dark:text-white">{{ transaction.transactionType }}</span>
          </div>
          <div>
            <span class="text-gray-500 dark:text-gray-400">Currency:</span>
            <span class="ml-2 text-gray-900 dark:text-white">{{ transaction.currency }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  transaction: {
    type: Object,
    required: true
  },
  compact: {
    type: Boolean,
    default: false
  }
})

const showDetails = ref(false)

// Transaction type configurations
const transactionConfig = {
  TOPUP: {
    icon: 'mdi:bank-transfer-in',
    iconBg: 'bg-green-100 dark:bg-green-900',
    iconColor: 'text-green-600 dark:text-green-400',
    amountClass: 'text-green-600 dark:text-green-400',
    prefix: '+',
    text: 'Top Up'
  },
  PURCHASE: {
    icon: 'mdi:shopping',
    iconBg: 'bg-red-100 dark:bg-red-900',
    iconColor: 'text-red-600 dark:text-red-400',
    amountClass: 'text-red-600 dark:text-red-400',
    prefix: '-',
    text: 'Purchase'
  },
  TRANSFER_IN: {
    icon: 'mdi:bank-transfer-in',
    iconBg: 'bg-blue-100 dark:bg-blue-900',
    iconColor: 'text-blue-600 dark:text-blue-400',
    amountClass: 'text-blue-600 dark:text-blue-400',
    prefix: '+',
    text: 'Transfer In'
  },
  TRANSFER_OUT: {
    icon: 'mdi:bank-transfer-out',
    iconBg: 'bg-orange-100 dark:bg-orange-900',
    iconColor: 'text-orange-600 dark:text-orange-400',
    amountClass: 'text-orange-600 dark:text-orange-400',
    prefix: '-',
    text: 'Transfer Out'
  },
  REFUND: {
    icon: 'mdi:cash-refund',
    iconBg: 'bg-purple-100 dark:bg-purple-900',
    iconColor: 'text-purple-600 dark:text-purple-400',
    amountClass: 'text-purple-600 dark:text-purple-400',
    prefix: '+',
    text: 'Refund'
  },
  FEE: {
    icon: 'mdi:cash-minus',
    iconBg: 'bg-red-100 dark:bg-red-900',
    iconColor: 'text-red-600 dark:text-red-400',
    amountClass: 'text-red-600 dark:text-red-400',
    prefix: '-',
    text: 'Fee'
  },
  REWARD: {
    icon: 'mdi:gift',
    iconBg: 'bg-yellow-100 dark:bg-yellow-900',
    iconColor: 'text-yellow-600 dark:text-yellow-400',
    amountClass: 'text-yellow-600 dark:text-yellow-400',
    prefix: '+',
    text: 'Reward'
  },
  ADJUSTMENT: {
    icon: 'mdi:cash-sync',
    iconBg: 'bg-gray-100 dark:bg-gray-900',
    iconColor: 'text-gray-600 dark:text-gray-400',
    amountClass: 'text-gray-600 dark:text-gray-400',
    prefix: '',
    text: 'Adjustment'
  }
}

const getTransactionIcon = () => {
  const config = transactionConfig[props.transaction.transactionType]
  return config?.icon || 'mdi:help-circle'
}

const getIconBackgroundClass = () => {
  const config = transactionConfig[props.transaction.transactionType]
  return config?.iconBg || 'bg-gray-100 dark:bg-gray-900'
}

const getIconClass = () => {
  const config = transactionConfig[props.transaction.transactionType]
  return config?.iconColor || 'text-gray-600 dark:text-gray-400'
}

const getAmountClass = () => {
  const config = transactionConfig[props.transaction.transactionType]
  return config?.amountClass || 'text-gray-600 dark:text-gray-400'
}

const getAmountPrefix = () => {
  const config = transactionConfig[props.transaction.transactionType]
  return config?.prefix || ''
}

const getTransactionTypeText = () => {
  const config = transactionConfig[props.transaction.transactionType]
  return config?.text || props.transaction.transactionType
}

const getStatusClass = () => {
  const statusClasses = {
    COMPLETED: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
    PENDING: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200',
    FAILED: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200',
    CANCELLED: 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
  }
  return statusClasses[props.transaction.status] || 'bg-gray-100 text-gray-800'
}

const formatCurrency = (amount, currency) => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: currency
  }).format(amount)
}

const formatDate = (date) => {
  if (!date) return 'N/A'
  const now = new Date()
  const transactionDate = new Date(date)
  const diffMs = now - transactionDate
  const diffMins = Math.floor(diffMs / 60000)
  
  if (diffMins < 1) return 'Just now'
  if (diffMins < 60) return `${diffMins}m ago`
  
  const diffHours = Math.floor(diffMins / 60)
  if (diffHours < 24) return `${diffHours}h ago`
  
  const diffDays = Math.floor(diffHours / 24)
  if (diffDays < 7) return `${diffDays}d ago`
  
  // For older transactions, show full date
  return new Intl.DateTimeFormat('en-US', {
    month: 'short',
    day: 'numeric',
    year: transactionDate.getFullYear() !== now.getFullYear() ? 'numeric' : undefined
  }).format(transactionDate)
}
</script>
