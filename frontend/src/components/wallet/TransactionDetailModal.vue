<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white dark:bg-gray-800 rounded-lg max-w-4xl w-full mx-4 max-h-[90vh] overflow-y-auto">
      <!-- Header -->
      <div class="p-6 border-b border-gray-200 dark:border-gray-700">
        <div class="flex justify-between items-center">
          <div>
            <h3 class="text-xl font-semibold text-gray-900 dark:text-gray-100">
              Transaction Details
            </h3>
            <p class="text-gray-600 dark:text-gray-400">
              Transaction #{{ transaction.transactionId }}
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

      <!-- Transaction Content -->
      <div class="p-6">
        <!-- Transaction Overview -->
        <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-6 mb-6">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">Transaction Information</h4>
              <div class="space-y-2 text-sm">
                <div class="flex justify-between">
                  <span class="text-gray-600 dark:text-gray-400">Transaction ID:</span>
                  <span class="font-medium text-gray-900 dark:text-gray-100">{{ transaction.transactionId }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-gray-600 dark:text-gray-400">Type:</span>
                  <span :class="getTransactionTypeClass(transaction.type)" class="px-2 py-1 rounded-full text-xs font-medium">
                    {{ transaction.type }}
                  </span>
                </div>
                <div class="flex justify-between">
                  <span class="text-gray-600 dark:text-gray-400">Status:</span>
                  <span :class="getTransactionStatusClass(transaction.status)" class="px-2 py-1 rounded-full text-xs font-medium">
                    {{ transaction.status }}
                  </span>
                </div>
                <div class="flex justify-between">
                  <span class="text-gray-600 dark:text-gray-400">Date:</span>
                  <span class="font-medium text-gray-900 dark:text-gray-100">{{ formatDate(transaction.createdAt) }}</span>
                </div>
                <div v-if="transaction.completedAt" class="flex justify-between">
                  <span class="text-gray-600 dark:text-gray-400">Completed:</span>
                  <span class="font-medium text-gray-900 dark:text-gray-100">{{ formatDate(transaction.completedAt) }}</span>
                </div>
              </div>
            </div>

            <div>
              <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">Amount</h4>
              <div class="space-y-2 text-sm">
                <div class="flex justify-between">
                  <span class="text-gray-600 dark:text-gray-400">Amount:</span>
                  <span :class="getTransactionAmountClass(transaction)" class="text-lg font-bold">
                    {{ transaction.type === 'CREDIT' ? '+' : '-' }}{{ transaction.currency }} {{ formatCurrency(transaction.amount) }}
                  </span>
                </div>
                <div v-if="transaction.fee" class="flex justify-between">
                  <span class="text-gray-600 dark:text-gray-400">Fee:</span>
                  <span class="font-medium text-gray-900 dark:text-gray-100">{{ transaction.currency }} {{ formatCurrency(transaction.fee) }}</span>
                </div>
                <div v-if="transaction.fee" class="flex justify-between text-base font-bold">
                  <span class="text-gray-900 dark:text-gray-100">Net Amount:</span>
                  <span :class="getTransactionAmountClass(transaction)">
                    {{ transaction.type === 'CREDIT' ? '+' : '-' }}{{ transaction.currency }} {{ formatCurrency(transaction.amount - transaction.fee) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Wallet Information -->
        <div class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-6 mb-6">
          <h4 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">Wallet Information</h4>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h5 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">From Wallet</h5>
              <div class="space-y-1 text-sm">
                <p class="font-medium text-gray-900 dark:text-gray-100">{{ transaction.fromWallet.name }}</p>
                <p class="text-gray-600 dark:text-gray-400">Balance: {{ transaction.fromWallet.currency }} {{ formatCurrency(transaction.fromWallet.balance) }}</p>
              </div>
            </div>

            <div v-if="transaction.toWallet">
              <h5 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">To Wallet</h5>
              <div class="space-y-1 text-sm">
                <p class="font-medium text-gray-900 dark:text-gray-100">{{ transaction.toWallet.name }}</p>
                <p class="text-gray-600 dark:text-gray-400">Balance: {{ transaction.toWallet.currency }} {{ formatCurrency(transaction.toWallet.balance) }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Transaction Details -->
        <div class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-6 mb-6">
          <h4 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">Transaction Details</h4>
          <div class="space-y-4">
            <div>
              <h5 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Description</h5>
              <p class="text-sm text-gray-600 dark:text-gray-400">{{ transaction.description }}</p>
            </div>

            <div v-if="transaction.category">
              <h5 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Category</h5>
              <p class="text-sm text-gray-600 dark:text-gray-400">{{ transaction.category }}</p>
            </div>

            <div v-if="transaction.reference">
              <h5 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Reference</h5>
              <p class="text-sm text-gray-600 dark:text-gray-400">{{ transaction.reference }}</p>
            </div>

            <div v-if="transaction.metadata">
              <h5 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Additional Information</h5>
              <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
                <pre class="text-xs text-gray-600 dark:text-gray-400 whitespace-pre-wrap">{{ JSON.stringify(transaction.metadata, null, 2) }}</pre>
              </div>
            </div>
          </div>
        </div>

        <!-- Timeline -->
        <div v-if="transaction.timeline" class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-6 mb-6">
          <h4 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">Transaction Timeline</h4>
          <div class="space-y-4">
            <div 
              v-for="(event, index) in transaction.timeline" 
              :key="index"
              class="flex gap-4"
            >
              <div class="flex flex-col items-center">
                <div :class="getTimelineIconClass(event.status)" class="w-8 h-8 rounded-full flex items-center justify-center">
                  <Icon :icon="getTimelineIcon(event.status)" class="text-sm" />
                </div>
                <div v-if="index < transaction.timeline.length - 1" class="w-0.5 h-8 bg-gray-300 dark:bg-gray-600"></div>
              </div>
              <div class="flex-1">
                <p class="text-sm font-medium text-gray-900 dark:text-gray-100">{{ event.title }}</p>
                <p class="text-xs text-gray-600 dark:text-gray-400">{{ formatDate(event.timestamp) }}</p>
                <p v-if="event.description" class="text-sm text-gray-600 dark:text-gray-400 mt-1">{{ event.description }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Actions -->
        <div class="flex justify-between items-center">
          <div class="text-sm text-gray-600 dark:text-gray-400">
            Last updated: {{ formatDate(transaction.updatedAt) }}
          </div>
          
          <div class="flex gap-3">
            <button 
              @click="downloadReceipt"
              class="px-4 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors flex items-center gap-2"
            >
              <Icon icon="ic:baseline-download" />
              Download Receipt
            </button>
            
            <button 
              v-if="transaction.status === 'PENDING'"
              @click="cancelTransaction"
              class="px-4 py-2 bg-red-100 dark:bg-red-900/20 text-red-600 dark:text-red-400 rounded-lg hover:bg-red-200 dark:hover:bg-red-900/30 transition-colors flex items-center gap-2"
            >
              <Icon icon="ic:baseline-cancel" />
              Cancel Transaction
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  transaction: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'cancelled'])

// Methods
const getTransactionTypeClass = (type) => {
  switch (type?.toUpperCase()) {
    case 'CREDIT':
      return 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400'
    case 'DEBIT':
      return 'bg-red-100 text-red-600 dark:bg-red-900/20 dark:text-red-400'
    case 'TRANSFER':
      return 'bg-blue-100 text-blue-600 dark:bg-blue-900/20 dark:text-blue-400'
    case 'TOPUP':
      return 'bg-purple-100 text-purple-600 dark:bg-purple-900/20 dark:text-purple-400'
    default:
      return 'bg-gray-100 text-gray-600'
  }
}

const getTransactionStatusClass = (status) => {
  switch (status?.toUpperCase()) {
    case 'COMPLETED':
      return 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400'
    case 'PENDING':
      return 'bg-yellow-100 text-yellow-600 dark:bg-yellow-900/20 dark:text-yellow-400'
    case 'FAILED':
      return 'bg-red-100 text-red-600 dark:bg-red-900/20 dark:text-red-400'
    case 'CANCELLED':
      return 'bg-gray-100 text-gray-600 dark:bg-gray-900/20 dark:text-gray-400'
    default:
      return 'bg-gray-100 text-gray-600'
  }
}

const getTransactionAmountClass = (transaction) => {
  return transaction.type === 'CREDIT' 
    ? 'text-green-600 dark:text-green-400' 
    : 'text-red-600 dark:text-red-400'
}

const getTimelineIcon = (status) => {
  const iconMap = {
    'INITIATED': 'ic:baseline-play-arrow',
    'PROCESSING': 'ic:baseline-refresh',
    'COMPLETED': 'ic:baseline-check-circle',
    'FAILED': 'ic:baseline-error',
    'CANCELLED': 'ic:baseline-cancel'
  }
  return iconMap[status] || 'ic:baseline-info'
}

const getTimelineIconClass = (status) => {
  const classMap = {
    'INITIATED': 'bg-blue-100 text-blue-600 dark:bg-blue-900/20 dark:text-blue-400',
    'PROCESSING': 'bg-yellow-100 text-yellow-600 dark:bg-yellow-900/20 dark:text-yellow-400',
    'COMPLETED': 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400',
    'FAILED': 'bg-red-100 text-red-600 dark:bg-red-900/20 dark:text-red-400',
    'CANCELLED': 'bg-gray-100 text-gray-600 dark:bg-gray-900/20 dark:text-gray-400'
  }
  return classMap[status] || 'bg-gray-100 text-gray-600'
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
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const downloadReceipt = () => {
  // Simulate receipt download
  const receiptData = {
    transaction: props.transaction,
    downloadedAt: new Date().toISOString()
  }

  const blob = new Blob([JSON.stringify(receiptData, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `receipt-${props.transaction.transactionId}.json`
  link.click()
  URL.revokeObjectURL(url)
}

const cancelTransaction = () => {
  if (confirm(`Are you sure you want to cancel transaction ${props.transaction.transactionId}?`)) {
    // Simulate cancellation
    const cancelledTransaction = {
      ...props.transaction,
      status: 'CANCELLED',
      updatedAt: new Date().toISOString()
    }
    
    emit('cancelled', cancelledTransaction)
  }
}
</script>

<style scoped>
.modal-backdrop {
  backdrop-filter: blur(4px);
}
</style>
