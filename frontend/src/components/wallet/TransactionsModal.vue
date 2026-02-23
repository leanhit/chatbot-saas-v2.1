<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white dark:bg-gray-800 rounded-lg max-w-6xl w-full mx-4 max-h-[90vh] overflow-y-auto">
      <!-- Header -->
      <div class="p-6 border-b border-gray-200 dark:border-gray-700">
        <div class="flex justify-between items-center">
          <div>
            <h3 class="text-xl font-semibold text-gray-900 dark:text-gray-100">
              Transaction History
            </h3>
            <p class="text-gray-600 dark:text-gray-400">
              {{ wallet.name }} - ${{ formatCurrency(wallet.balance) }}
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

      <!-- Filters and Search -->
      <div class="p-6 border-b border-gray-200 dark:border-gray-700">
        <div class="flex flex-col lg:flex-row gap-4">
          <!-- Search -->
          <div class="flex-1">
            <div class="relative">
              <Icon icon="ic:baseline-search" class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
              <input 
                v-model="searchQuery"
                type="text"
                placeholder="Search transactions..."
                class="w-full pl-10 pr-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
          </div>

          <!-- Transaction Type Filter -->
          <select 
            v-model="typeFilter"
            class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            <option value="">All Types</option>
            <option value="TOPUP">Top-up</option>
            <option value="PURCHASE">Purchase</option>
            <option value="TRANSFER_IN">Transfer In</option>
            <option value="TRANSFER_OUT">Transfer Out</option>
            <option value="REFUND">Refund</option>
            <option value="FEE">Fee</option>
          </select>

          <!-- Status Filter -->
          <select 
            v-model="statusFilter"
            class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            <option value="">All Status</option>
            <option value="COMPLETED">Completed</option>
            <option value="PENDING">Pending</option>
            <option value="FAILED">Failed</option>
          </select>

          <!-- Date Range Filter -->
          <select 
            v-model="dateFilter"
            class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            <option value="">All Time</option>
            <option value="7">Last 7 Days</option>
            <option value="30">Last 30 Days</option>
            <option value="90">Last 90 Days</option>
            <option value="365">Last Year</option>
          </select>

          <!-- Export Button -->
          <button 
            @click="exportTransactions"
            class="px-4 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors flex items-center gap-2"
          >
            <Icon icon="ic:baseline-file-download" />
            Export
          </button>
        </div>
      </div>

      <!-- Transaction List -->
      <div class="p-6">
        <!-- Summary Stats -->
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
          <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
            <div class="flex items-center gap-3">
              <div class="p-2 bg-blue-100 dark:bg-blue-900/20 rounded-lg">
                <Icon icon="ic:baseline-receipt-long" class="text-blue-600 dark:text-blue-400 text-xl" />
              </div>
              <div>
                <p class="text-sm text-gray-600 dark:text-gray-400">Total Transactions</p>
                <p class="text-xl font-bold text-gray-900 dark:text-gray-100">
                  {{ filteredTransactions.length }}
                </p>
              </div>
            </div>
          </div>

          <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
            <div class="flex items-center gap-3">
              <div class="p-2 bg-green-100 dark:bg-green-900/20 rounded-lg">
                <Icon icon="ic:baseline-trending-up" class="text-green-600 dark:text-green-400 text-xl" />
              </div>
              <div>
                <p class="text-sm text-gray-600 dark:text-gray-400">Total Income</p>
                <p class="text-xl font-bold text-green-600 dark:text-green-400">
                  +${{ formatCurrency(totalIncome) }}
                </p>
              </div>
            </div>
          </div>

          <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
            <div class="flex items-center gap-3">
              <div class="p-2 bg-red-100 dark:bg-red-900/20 rounded-lg">
                <Icon icon="ic:baseline-trending-down" class="text-red-600 dark:text-red-400 text-xl" />
              </div>
              <div>
                <p class="text-sm text-gray-600 dark:text-gray-400">Total Expenses</p>
                <p class="text-xl font-bold text-red-600 dark:text-red-400">
                  -${{ formatCurrency(totalExpenses) }}
                </p>
              </div>
            </div>
          </div>

          <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
            <div class="flex items-center gap-3">
              <div class="p-2 bg-purple-100 dark:bg-purple-900/20 rounded-lg">
                <Icon icon="ic:baseline-account-balance-wallet" class="text-purple-600 dark:text-purple-400 text-xl" />
              </div>
              <div>
                <p class="text-sm text-gray-600 dark:text-gray-400">Net Change</p>
                <p :class="netChangeClass" class="text-xl font-bold">
                  {{ netChangePrefix }}${{ formatCurrency(Math.abs(netChange)) }}
                </p>
              </div>
            </div>
          </div>
        </div>

        <!-- Transactions Table -->
        <div v-if="isLoading" class="text-center py-12">
          <Icon icon="ic:baseline-refresh" class="animate-spin text-4xl text-gray-400" />
          <p class="text-gray-600 dark:text-gray-400 mt-2">Loading transactions...</p>
        </div>

        <div v-else-if="filteredTransactions.length === 0" class="text-center py-12">
          <Icon icon="ic:baseline-receipt-long" class="text-4xl text-gray-400" />
          <h3 class="text-lg font-medium text-gray-900 dark:text-gray-100 mt-4">
            No transactions found
          </h3>
          <p class="text-gray-600 dark:text-gray-400 mt-2">
            {{ searchQuery ? 'Try adjusting your search' : 'No transactions match your criteria' }}
          </p>
        </div>

        <div v-else class="overflow-x-auto">
          <table class="w-full">
            <thead class="bg-gray-50 dark:bg-gray-700 border-b border-gray-200 dark:border-gray-700">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  Date
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  Description
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  Type
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  Status
                </th>
                <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  Amount
                </th>
                <th class="px-6 py-3 text-center text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-200 dark:divide-gray-700">
              <tr 
                v-for="transaction in paginatedTransactions" 
                :key="transaction.id"
                class="hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
              >
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-100">
                  {{ formatDate(transaction.createdAt) }}
                </td>
                <td class="px-6 py-4 text-sm text-gray-900 dark:text-gray-100">
                  <div>
                    <p class="font-medium">{{ transaction.description || getTransactionDescription(transaction.transactionType) }}</p>
                    <p v-if="transaction.metadata?.reference" class="text-xs text-gray-600 dark:text-gray-400">
                      Ref: {{ transaction.metadata.reference }}
                    </p>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span :class="getTransactionTypeClass(transaction.transactionType)" class="px-2 py-1 rounded-full text-xs font-medium">
                    {{ formatTransactionType(transaction.transactionType) }}
                  </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span :class="getStatusClass(transaction.status)" class="px-2 py-1 rounded-full text-xs font-medium">
                    {{ transaction.status }}
                  </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-right">
                  <span :class="getAmountClass(transaction.transactionType)" class="font-semibold">
                    {{ getAmountPrefix(transaction.transactionType) }}${{ formatCurrency(transaction.amount) }}
                  </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-center">
                  <div class="flex justify-center gap-2">
                    <button 
                      @click="viewTransaction(transaction)"
                      class="p-2 text-gray-600 dark:text-gray-400 hover:text-blue-600 dark:hover:text-blue-400 transition-colors"
                      title="View Details"
                    >
                      <Icon icon="ic:baseline-visibility" />
                    </button>
                    <button 
                      v-if="transaction.status === 'COMPLETED' && transaction.transactionType === 'PURCHASE'"
                      @click="refundTransaction(transaction)"
                      class="p-2 text-gray-600 dark:text-gray-400 hover:text-orange-600 dark:hover:text-orange-400 transition-colors"
                      title="Request Refund"
                    >
                      <Icon icon="ic:baseline-refresh" />
                    </button>
                    <button 
                      @click="downloadReceipt(transaction)"
                      class="p-2 text-gray-600 dark:text-gray-400 hover:text-green-600 dark:hover:text-green-400 transition-colors"
                      title="Download Receipt"
                    >
                      <Icon icon="ic:baseline-download" />
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="px-6 py-4 border-t border-gray-200 dark:border-gray-700">
          <div class="flex justify-between items-center">
            <p class="text-sm text-gray-700 dark:text-gray-300">
              Showing {{ (currentPage - 1) * pageSize + 1 }} to {{ Math.min(currentPage * pageSize, filteredTransactions.length) }} of {{ filteredTransactions.length }} results
            </p>
            <div class="flex gap-2">
              <button 
                @click="currentPage = Math.max(1, currentPage - 1)"
                :disabled="currentPage === 1"
                class="px-3 py-1 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Previous
              </button>
              <span class="px-3 py-1 text-gray-700 dark:text-gray-300">
                {{ currentPage }} / {{ totalPages }}
              </span>
              <button 
                @click="currentPage = Math.min(totalPages, currentPage + 1)"
                :disabled="currentPage === totalPages"
                class="px-3 py-1 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Next
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Transaction Detail Modal -->
    <TransactionDetailModal 
      v-if="showDetailModal"
      :transaction="selectedTransaction"
      @close="showDetailModal = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useWalletStore } from '@/stores/wallet/walletStore'
import TransactionDetailModal from './TransactionDetailModal.vue'

const props = defineProps({
  wallet: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close'])

const walletStore = useWalletStore()

// State
const searchQuery = ref('')
const typeFilter = ref('')
const statusFilter = ref('')
const dateFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const isLoading = ref(false)
const showDetailModal = ref(false)
const selectedTransaction = ref(null)

// Mock transactions data (in real app, this would come from API)
const transactions = ref([
  {
    id: '1',
    description: 'Monthly subscription payment',
    transactionType: 'PURCHASE',
    amount: 9900, // $99.00 in cents
    status: 'COMPLETED',
    createdAt: '2024-01-15T10:30:00Z',
    metadata: { reference: 'SUB-2024-01' }
  },
  {
    id: '2',
    description: 'Wallet top-up',
    transactionType: 'TOPUP',
    amount: 5000, // $50.00 in cents
    status: 'COMPLETED',
    createdAt: '2024-01-10T14:20:00Z'
  },
  {
    id: '3',
    description: 'Transfer from John Doe',
    transactionType: 'TRANSFER_IN',
    amount: 2500, // $25.00 in cents
    status: 'COMPLETED',
    createdAt: '2024-01-08T09:15:00Z'
  }
])

// Computed
const filteredTransactions = computed(() => {
  let filtered = transactions.value

  // Apply search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(transaction => 
      transaction.description?.toLowerCase().includes(query) ||
      transaction.metadata?.reference?.toLowerCase().includes(query)
    )
  }

  // Apply type filter
  if (typeFilter.value) {
    filtered = filtered.filter(transaction => transaction.transactionType === typeFilter.value)
  }

  // Apply status filter
  if (statusFilter.value) {
    filtered = filtered.filter(transaction => transaction.status === statusFilter.value)
  }

  // Apply date filter
  if (dateFilter.value) {
    const days = parseInt(dateFilter.value)
    const cutoffDate = new Date()
    cutoffDate.setDate(cutoffDate.getDate() - days)
    
    filtered = filtered.filter(transaction => 
      new Date(transaction.createdAt) >= cutoffDate
    )
  }

  return filtered.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
})

const paginatedTransactions = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredTransactions.value.slice(start, end)
})

const totalPages = computed(() => 
  Math.ceil(filteredTransactions.value.length / pageSize.value)
)

const totalIncome = computed(() => {
  return filteredTransactions.value
    .filter(t => ['TOPUP', 'TRANSFER_IN', 'REFUND'].includes(t.transactionType))
    .reduce((total, t) => total + t.amount, 0)
})

const totalExpenses = computed(() => {
  return filteredTransactions.value
    .filter(t => ['PURCHASE', 'TRANSFER_OUT', 'FEE'].includes(t.transactionType))
    .reduce((total, t) => total + t.amount, 0)
})

const netChange = computed(() => totalIncome.value - totalExpenses.value)

const netChangeClass = computed(() => {
  return netChange.value >= 0 
    ? 'text-green-600 dark:text-green-400'
    : 'text-red-600 dark:text-red-400'
})

const netChangePrefix = computed(() => {
  return netChange.value >= 0 ? '+' : '-'
})

// Methods
const getTransactionTypeClass = (type) => {
  const classMap = {
    'TOPUP': 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400',
    'PURCHASE': 'bg-red-100 text-red-600 dark:bg-red-900/20 dark:text-red-400',
    'TRANSFER_IN': 'bg-blue-100 text-blue-600 dark:bg-blue-900/20 dark:text-blue-400',
    'TRANSFER_OUT': 'bg-orange-100 text-orange-600 dark:bg-orange-900/20 dark:text-orange-400',
    'REFUND': 'bg-purple-100 text-purple-600 dark:bg-purple-900/20 dark:text-purple-400',
    'FEE': 'bg-gray-100 text-gray-600 dark:bg-gray-900/20 dark:text-gray-400'
  }
  return classMap[type] || 'bg-gray-100 text-gray-600'
}

const getTransactionDescription = (type) => {
  const descMap = {
    'TOPUP': 'Wallet Top-up',
    'PURCHASE': 'Purchase',
    'TRANSFER_IN': 'Transfer Received',
    'TRANSFER_OUT': 'Transfer Sent',
    'REFUND': 'Refund',
    'FEE': 'Transaction Fee'
  }
  return descMap[type] || 'Transaction'
}

const formatTransactionType = (type) => {
  return type.replace(/_/g, ' ').toLowerCase()
}

const getStatusClass = (status) => {
  switch (status?.toUpperCase()) {
    case 'COMPLETED':
      return 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400'
    case 'PENDING':
      return 'bg-yellow-100 text-yellow-600 dark:bg-yellow-900/20 dark:text-yellow-400'
    case 'FAILED':
      return 'bg-red-100 text-red-600 dark:bg-red-900/20 dark:text-red-400'
    default:
      return 'bg-gray-100 text-gray-600 dark:bg-gray-900/20 dark:text-gray-400'
  }
}

const getAmountClass = (type) => {
  const positiveTypes = ['TOPUP', 'TRANSFER_IN', 'REFUND']
  return positiveTypes.includes(type) 
    ? 'text-green-600 dark:text-green-400'
    : 'text-red-600 dark:text-red-400'
}

const getAmountPrefix = (type) => {
  const positiveTypes = ['TOPUP', 'TRANSFER_IN', 'REFUND']
  return positiveTypes.includes(type) ? '+' : '-'
}

const formatCurrency = (amount) => {
  if (!amount) return '0.00'
  return new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(amount / 100) // Assuming amount is in cents
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

const viewTransaction = (transaction) => {
  selectedTransaction.value = transaction
  showDetailModal.value = true
}

const refundTransaction = (transaction) => {
  if (!confirm(`Are you sure you want to request a refund for $${formatCurrency(transaction.amount)}?`)) {
    return
  }
  console.log('Request refund for:', transaction)
}

const downloadReceipt = (transaction) => {
  // Generate and download receipt
  const receiptData = {
    id: transaction.id,
    description: transaction.description,
    amount: transaction.amount,
    date: transaction.createdAt,
    status: transaction.status
  }
  
  const blob = new Blob([JSON.stringify(receiptData, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `receipt-${transaction.id}.json`
  link.click()
  URL.revokeObjectURL(url)
}

const exportTransactions = () => {
  // Export filtered transactions to CSV
  const csvContent = [
    ['Date', 'Description', 'Type', 'Status', 'Amount'],
    ...filteredTransactions.value.map(t => [
      formatDate(t.createdAt),
      t.description || getTransactionDescription(t.transactionType),
      formatTransactionType(t.transactionType),
      t.status,
      getAmountPrefix(t.transactionType) + formatCurrency(t.amount)
    ])
  ].map(row => row.join(',')).join('\n')
  
  const blob = new Blob([csvContent], { type: 'text/csv' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `transactions-${new Date().toISOString().split('T')[0]}.csv`
  link.click()
  URL.revokeObjectURL(url)
}

// Lifecycle
onMounted(() => {
  // Load transactions for the wallet
  isLoading.value = true
  setTimeout(() => {
    isLoading.value = false
  }, 1000) // Simulate API call
})
</script>

<style scoped>
.modal-backdrop {
  backdrop-filter: blur(4px);
}
</style>
