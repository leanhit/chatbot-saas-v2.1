<template>
  <div class="transactions-page p-6">
    <!-- Header -->
    <div class="mb-6">
      <div class="flex justify-between items-center">
        <div>
          <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">
            Transactions
          </h1>
          <p class="text-gray-600 dark:text-gray-400">
            View and manage your wallet transactions
          </p>
        </div>
        
        <div class="flex gap-3">
          <button 
            @click="exportTransactions"
            class="px-4 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors flex items-center gap-2"
          >
            <Icon icon="ic:baseline-file-download" />
            Export
          </button>
          <button 
            @click="refreshTransactions"
            :disabled="isLoading"
            class="px-4 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors flex items-center gap-2"
          >
            <Icon v-if="!isLoading" icon="ic:baseline-refresh" />
            <Icon v-else icon="ic:baseline-refresh" class="animate-spin" />
            Refresh
          </button>
        </div>
      </div>
    </div>

    <!-- Filters -->
    <div class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-6 mb-6">
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

        <!-- Wallet Filter -->
        <select 
          v-model="selectedWallet"
          class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        >
          <option value="">All Wallets</option>
          <option v-for="wallet in wallets" :key="wallet.id" :value="wallet.id">
            {{ wallet.name }}
          </option>
        </select>

        <!-- Type Filter -->
        <select 
          v-model="selectedType"
          class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        >
          <option value="">All Types</option>
          <option value="CREDIT">Credit</option>
          <option value="DEBIT">Debit</option>
          <option value="TRANSFER">Transfer</option>
          <option value="TOPUP">Top-up</option>
        </select>

        <!-- Status Filter -->
        <select 
          v-model="selectedStatus"
          class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        >
          <option value="">All Status</option>
          <option value="COMPLETED">Completed</option>
          <option value="PENDING">Pending</option>
          <option value="FAILED">Failed</option>
          <option value="CANCELLED">Cancelled</option>
        </select>

        <!-- Date Range Filter -->
        <select 
          v-model="dateRange"
          class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        >
          <option value="">All Time</option>
          <option value="7days">Last 7 Days</option>
          <option value="30days">Last 30 Days</option>
          <option value="90days">Last 90 Days</option>
          <option value="6months">Last 6 Months</option>
          <option value="1year">Last Year</option>
        </select>

        <!-- Reset Filters -->
        <button 
          @click="resetFilters"
          class="px-4 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors"
        >
          Reset Filters
        </button>
      </div>
    </div>

    <!-- Summary Cards -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-6">
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700">
        <div class="flex items-center gap-3">
          <div class="p-3 bg-blue-100 dark:bg-blue-900/20 rounded-lg">
            <Icon icon="ic:baseline-receipt-long" class="text-blue-600 dark:text-blue-400 text-2xl" />
          </div>
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Total Transactions</p>
            <p class="text-2xl font-bold text-gray-900 dark:text-gray-100">{{ totalTransactions }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700">
        <div class="flex items-center gap-3">
          <div class="p-3 bg-green-100 dark:bg-green-900/20 rounded-lg">
            <Icon icon="ic:baseline-trending-up" class="text-green-600 dark:text-green-400 text-2xl" />
          </div>
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Total Credits</p>
            <p class="text-2xl font-bold text-green-600 dark:text-green-400">${{ formatCurrency(totalCredits) }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700">
        <div class="flex items-center gap-3">
          <div class="p-3 bg-red-100 dark:bg-red-900/20 rounded-lg">
            <Icon icon="ic:baseline-trending-down" class="text-red-600 dark:text-red-400 text-2xl" />
          </div>
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Total Debits</p>
            <p class="text-2xl font-bold text-red-600 dark:text-red-400">${{ formatCurrency(totalDebits) }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700">
        <div class="flex items-center gap-3">
          <div class="p-3 bg-purple-100 dark:bg-purple-900/20 rounded-lg">
            <Icon icon="ic:baseline-account-balance" class="text-purple-600 dark:text-purple-400 text-2xl" />
          </div>
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Net Balance</p>
            <p class="text-2xl font-bold text-purple-600 dark:text-purple-400">${{ formatCurrency(netBalance) }}</p>
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
        {{ searchQuery ? 'Try adjusting your search' : 'No transactions available' }}
      </p>
    </div>

    <div v-else class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 overflow-hidden">
      <div class="overflow-x-auto">
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
                Wallet
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                Amount
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                Status
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
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
              <td class="px-6 py-4 whitespace-nowrap">
                <div>
                  <p class="text-sm font-medium text-gray-900 dark:text-gray-100">
                    {{ transaction.description }}
                  </p>
                  <p class="text-xs text-gray-600 dark:text-gray-400">
                    ID: {{ transaction.transactionId }}
                  </p>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span :class="getTransactionTypeClass(transaction.type)" class="px-2 py-1 rounded-full text-xs font-medium">
                  {{ transaction.type }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-100">
                {{ getWalletName(transaction.walletId) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span :class="getTransactionAmountClass(transaction)" class="text-sm font-medium">
                  {{ transaction.type === 'CREDIT' ? '+' : '-' }}{{ transaction.currency }} {{ formatCurrency(transaction.amount) }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span :class="getTransactionStatusClass(transaction.status)" class="px-2 py-1 rounded-full text-xs font-medium">
                  {{ transaction.status }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm">
                <div class="flex gap-2">
                  <button 
                    @click="viewTransaction(transaction)"
                    class="text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300"
                    title="View Details"
                  >
                    <Icon icon="ic:baseline-visibility" />
                  </button>
                  <button 
                    v-if="transaction.status === 'PENDING'"
                    @click="cancelTransaction(transaction)"
                    class="text-red-600 dark:text-red-400 hover:text-red-700 dark:hover:text-red-300"
                    title="Cancel Transaction"
                  >
                    <Icon icon="ic:baseline-cancel" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="p-6 border-t border-gray-200 dark:border-gray-700">
        <div class="flex justify-between items-center">
          <div class="text-sm text-gray-700 dark:text-gray-300">
            Showing {{ (currentPage - 1) * pageSize + 1 }} to {{ Math.min(currentPage * pageSize, filteredTransactions.length) }} of {{ filteredTransactions.length }} transactions
          </div>
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

    <!-- Transaction Detail Modal -->
    <TransactionDetailModal 
      v-if="showDetailModal"
      :transaction="selectedTransaction"
      @close="showDetailModal = false"
      @cancelled="onTransactionCancelled"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useWalletStore } from '@/stores/wallet/walletStore'
import TransactionDetailModal from '@/components/wallet/TransactionDetailModal.vue'

const walletStore = useWalletStore()

// State
const isLoading = ref(false)
const searchQuery = ref('')
const selectedWallet = ref('')
const selectedType = ref('')
const selectedStatus = ref('')
const dateRange = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const showDetailModal = ref(false)
const selectedTransaction = ref(null)

// Mock data
const wallets = ref([
  { id: '1', name: 'Primary Wallet' },
  { id: '2', name: 'Business Wallet' },
  { id: '3', name: 'Savings Wallet' }
])

const transactions = ref([
  {
    id: '1',
    transactionId: 'TXN-2024-001',
    description: 'Monthly Subscription',
    type: 'DEBIT',
    amount: 99.99,
    currency: 'USD',
    walletId: '1',
    status: 'COMPLETED',
    createdAt: '2024-01-20T14:30:00Z',
    completedAt: '2024-01-20T14:31:00Z'
  },
  {
    id: '2',
    transactionId: 'TXN-2024-002',
    description: 'Wallet Top-up',
    type: 'CREDIT',
    amount: 500.00,
    currency: 'USD',
    walletId: '1',
    status: 'COMPLETED',
    createdAt: '2024-01-19T10:15:00Z',
    completedAt: '2024-01-19T10:16:00Z'
  },
  {
    id: '3',
    transactionId: 'TXN-2024-003',
    description: 'Transfer to Business Wallet',
    type: 'TRANSFER',
    amount: 250.00,
    currency: 'USD',
    walletId: '1',
    status: 'COMPLETED',
    createdAt: '2024-01-18T09:00:00Z',
    completedAt: '2024-01-18T09:01:00Z'
  },
  {
    id: '4',
    transactionId: 'TXN-2024-004',
    description: 'API Usage Charge',
    type: 'DEBIT',
    amount: 15.50,
    currency: 'USD',
    walletId: '2',
    status: 'PENDING',
    createdAt: '2024-01-17T16:45:00Z',
    completedAt: null
  },
  {
    id: '5',
    transactionId: 'TXN-2024-005',
    description: 'Refund',
    type: 'CREDIT',
    amount: 25.00,
    currency: 'USD',
    walletId: '1',
    status: 'FAILED',
    createdAt: '2024-01-16T11:30:00Z',
    completedAt: null
  }
])

// Computed
const filteredTransactions = computed(() => {
  let filtered = transactions.value

  // Apply search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(transaction => 
      transaction.description.toLowerCase().includes(query) ||
      transaction.transactionId.toLowerCase().includes(query)
    )
  }

  // Apply wallet filter
  if (selectedWallet.value) {
    filtered = filtered.filter(transaction => transaction.walletId === selectedWallet.value)
  }

  // Apply type filter
  if (selectedType.value) {
    filtered = filtered.filter(transaction => transaction.type === selectedType.value)
  }

  // Apply status filter
  if (selectedStatus.value) {
    filtered = filtered.filter(transaction => transaction.status === selectedStatus.value)
  }

  // Apply date range filter
  if (dateRange.value) {
    const now = new Date()
    let cutoffDate = new Date()

    switch (dateRange.value) {
      case '7days':
        cutoffDate.setDate(cutoffDate.getDate() - 7)
        break
      case '30days':
        cutoffDate.setDate(cutoffDate.getDate() - 30)
        break
      case '90days':
        cutoffDate.setDate(cutoffDate.getDate() - 90)
        break
      case '6months':
        cutoffDate.setMonth(cutoffDate.getMonth() - 6)
        break
      case '1year':
        cutoffDate.setFullYear(cutoffDate.getFullYear() - 1)
        break
    }

    filtered = filtered.filter(transaction => new Date(transaction.createdAt) >= cutoffDate)
  }

  return filtered.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
})

const totalTransactions = computed(() => filteredTransactions.value.length)
const totalCredits = computed(() => {
  return filteredTransactions.value
    .filter(t => t.type === 'CREDIT')
    .reduce((total, t) => total + t.amount, 0)
})
const totalDebits = computed(() => {
  return filteredTransactions.value
    .filter(t => t.type === 'DEBIT')
    .reduce((total, t) => total + t.amount, 0)
})
const netBalance = computed(() => totalCredits.value - totalDebits.value)

const totalPages = computed(() => Math.ceil(filteredTransactions.value.length / pageSize.value))

const paginatedTransactions = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredTransactions.value.slice(start, end)
})

// Methods
const getWalletName = (walletId) => {
  const wallet = wallets.value.find(w => w.id === walletId)
  return wallet ? wallet.name : 'Unknown Wallet'
}

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

const getTransactionAmountClass = (transaction) => {
  return transaction.type === 'CREDIT' 
    ? 'text-green-600 dark:text-green-400' 
    : 'text-red-600 dark:text-red-400'
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

const resetFilters = () => {
  searchQuery.value = ''
  selectedWallet.value = ''
  selectedType.value = ''
  selectedStatus.value = ''
  dateRange.value = ''
  currentPage.value = 1
}

const refreshTransactions = async () => {
  isLoading.value = true
  
  try {
    await walletStore.fetchTransactions()
    // Update local state with store data
    // transactions.value = walletStore.transactions
  } catch (error) {
    console.error('Failed to refresh transactions:', error)
  } finally {
    isLoading.value = false
  }
}

const viewTransaction = (transaction) => {
  selectedTransaction.value = transaction
  showDetailModal.value = true
}

const cancelTransaction = (transaction) => {
  if (confirm(`Are you sure you want to cancel transaction ${transaction.transactionId}?`)) {
    // Implement cancellation logic
    console.log('Cancel transaction:', transaction)
  }
}

const exportTransactions = () => {
  const exportData = {
    transactions: filteredTransactions.value,
    exportedAt: new Date().toISOString(),
    filters: {
      searchQuery: searchQuery.value,
      selectedWallet: selectedWallet.value,
      selectedType: selectedType.value,
      selectedStatus: selectedStatus.value,
      dateRange: dateRange.value
    }
  }

  const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `transactions-${new Date().toISOString().split('T')[0]}.json`
  link.click()
  URL.revokeObjectURL(url)
}

const onTransactionCancelled = (cancelledTransaction) => {
  showDetailModal.value = false
  selectedTransaction.value = null
  
  // Update in local state
  const index = transactions.value.findIndex(t => t.id === cancelledTransaction.id)
  if (index !== -1) {
    transactions.value[index] = cancelledTransaction
  }
}

// Lifecycle
onMounted(() => {
  refreshTransactions()
})
</script>

<style scoped>
.transactions-page {
  max-width: 1400px;
  margin: 0 auto;
}
</style>
