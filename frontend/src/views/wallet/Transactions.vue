<template>
  <div class="transactions-page min-h-screen bg-gray-50 dark:bg-gray-900">
    <!-- Header -->
    <div class="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Transactions</h1>
            <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
              View your complete transaction history
            </p>
          </div>
          <div class="flex items-center space-x-3">
            <!-- Filter Dropdown -->
            <select v-model="filterType" class="text-sm border rounded px-3 py-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white">
              <option value="">All Types</option>
              <option value="TOPUP">Top Up</option>
              <option value="PURCHASE">Purchase</option>
              <option value="TRANSFER_IN">Transfer In</option>
              <option value="TRANSFER_OUT">Transfer Out</option>
              <option value="REFUND">Refund</option>
              <option value="FEE">Fee</option>
            </select>

            <!-- Status Filter -->
            <select v-model="filterStatus" class="text-sm border rounded px-3 py-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white">
              <option value="">All Status</option>
              <option value="COMPLETED">Completed</option>
              <option value="PENDING">Pending</option>
              <option value="FAILED">Failed</option>
            </select>

            <!-- Date Range -->
            <input
              v-model="dateFilter"
              type="date"
              class="text-sm border rounded px-3 py-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
            />

            <!-- Search -->
            <div class="relative">
              <input
                v-model="searchQuery"
                type="text"
                placeholder="Search transactions..."
                class="text-sm border rounded pl-10 pr-3 py-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
              />
              <Icon icon="mdi:magnify" class="absolute left-3 top-2.5 w-4 h-4 text-gray-400" />
            </div>

            <!-- Export Button -->
            <button
              @click="exportTransactions"
              class="flex items-center px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700"
            >
              <Icon icon="mdi:download" class="w-4 h-4 mr-2" />
              Export
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Stats Cards -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-600 dark:text-gray-400">Total Transactions</p>
              <p class="text-2xl font-bold text-gray-900 dark:text-white">{{ totalTransactions }}</p>
            </div>
            <div class="w-12 h-12 bg-blue-100 dark:bg-blue-900 rounded-full flex items-center justify-center">
              <Icon icon="mdi:receipt" class="w-6 h-6 text-blue-600 dark:text-blue-400" />
            </div>
          </div>
        </div>

        <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-600 dark:text-gray-400">This Month</p>
              <p class="text-2xl font-bold text-green-600 dark:text-green-400">{{ formatCurrency(monthlyTotal, 'USD') }}</p>
            </div>
            <div class="w-12 h-12 bg-green-100 dark:bg-green-900 rounded-full flex items-center justify-center">
              <Icon icon="mdi:trending-up" class="w-6 h-6 text-green-600 dark:text-green-400" />
            </div>
          </div>
        </div>

        <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-600 dark:text-gray-400">Pending</p>
              <p class="text-2xl font-bold text-yellow-600 dark:text-yellow-400">{{ pendingTransactions }}</p>
            </div>
            <div class="w-12 h-12 bg-yellow-100 dark:bg-yellow-900 rounded-full flex items-center justify-center">
              <Icon icon="mdi:clock" class="w-6 h-6 text-yellow-600 dark:text-yellow-400" />
            </div>
          </div>
        </div>

        <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-600 dark:text-gray-400">Failed</p>
              <p class="text-2xl font-bold text-red-600 dark:text-red-400">{{ failedTransactions }}</p>
            </div>
            <div class="w-12 h-12 bg-red-100 dark:bg-red-900 rounded-full flex items-center justify-center">
              <Icon icon="mdi:alert-circle" class="w-6 h-6 text-red-600 dark:text-red-400" />
            </div>
          </div>
        </div>
      </div>

      <!-- Transactions List -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow">
        <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
          <div class="flex items-center justify-between">
            <h2 class="text-lg font-semibold text-gray-900 dark:text-white">
              Transaction History
            </h2>
            <div class="flex items-center space-x-2">
              <span class="text-sm text-gray-500 dark:text-gray-400">
                {{ filteredTransactions.length }} of {{ totalTransactions }} transactions
              </span>
              <button
                @click="refreshTransactions"
                :disabled="loading"
                class="p-2 text-gray-400 hover:text-gray-500 dark:hover:text-gray-300"
              >
                <Icon :icon="loading ? 'mdi:loading' : 'mdi:refresh'" 
                      :class="loading ? 'animate-spin' : ''" class="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>
        
        <div class="divide-y divide-gray-200 dark:divide-gray-700">
          <!-- Loading State -->
          <div v-if="loading" class="p-8 text-center">
            <Icon icon="mdi:loading" class="w-8 h-8 animate-spin text-primary-600 mx-auto mb-3" />
            <p class="text-gray-500 dark:text-gray-400">Loading transactions...</p>
          </div>

          <!-- Empty State -->
          <div v-else-if="filteredTransactions.length === 0" class="p-8 text-center">
            <Icon icon="mdi:receipt-text-outline" class="w-12 h-12 text-gray-400 mx-auto mb-3" />
            <p class="text-gray-500 dark:text-gray-400">No transactions found</p>
            <p class="text-sm text-gray-400 dark:text-gray-500 mt-1">
              Try adjusting your filters or make a transaction to see history here.
            </p>
          </div>

          <!-- Transaction Items -->
          <TransactionItem
            v-for="transaction in paginatedTransactions"
            :key="transaction.id"
            :transaction="transaction"
          />
        </div>

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="px-6 py-4 border-t border-gray-200 dark:border-gray-700">
          <div class="flex items-center justify-between">
            <span class="text-sm text-gray-700 dark:text-gray-300">
              Showing {{ (currentPage - 1) * pageSize + 1 }} to {{ Math.min(currentPage * pageSize, filteredTransactions.length) }} of {{ totalTransactions }}
            </span>
            <div class="flex space-x-2">
              <button
                @click="goToPage(currentPage - 1)"
                :disabled="currentPage <= 1"
                class="px-3 py-1 border border-gray-300 dark:border-gray-600 rounded-md text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Previous
              </button>
              <button
                v-for="page in visiblePages"
                :key="page"
                @click="goToPage(page)"
                :class="[
                  'px-3 py-1 border rounded-md text-sm font-medium',
                  page === currentPage
                    ? 'border-primary-500 bg-primary-500 text-white'
                    : 'border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700'
                ]"
              >
                {{ page }}
              </button>
              <button
                @click="goToPage(currentPage + 1)"
                :disabled="currentPage >= totalPages"
                class="px-3 py-1 border border-gray-300 dark:border-gray-600 rounded-md text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Next
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useWalletStore } from '@/stores/walletStore'
import TransactionItem from '@/components/wallet/TransactionItem.vue'

const walletStore = useWalletStore()

// State
const loading = ref(false)
const filterType = ref('')
const filterStatus = ref('')
const dateFilter = ref('')
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(20)

// Computed
const transactions = computed(() => walletStore.transactions)

const filteredTransactions = computed(() => {
  let filtered = transactions.value

  // Filter by type
  if (filterType.value) {
    filtered = filtered.filter(t => t.transactionType === filterType.value)
  }

  // Filter by status
  if (filterStatus.value) {
    filtered = filtered.filter(t => t.status === filterStatus.value)
  }

  // Filter by date
  if (dateFilter.value) {
    const filterDate = new Date(dateFilter.value)
    filtered = filtered.filter(t => {
      const transactionDate = new Date(t.createdAt)
      return transactionDate.toDateString() === filterDate.toDateString()
    })
  }

  // Search
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(t => 
      t.description?.toLowerCase().includes(query) ||
      t.transactionReference?.toLowerCase().includes(query) ||
      t.externalReference?.toLowerCase().includes(query)
    )
  }

  return filtered
})

const paginatedTransactions = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredTransactions.value.slice(start, end)
})

const totalTransactions = computed(() => transactions.value.length)
const monthlyTotal = computed(() => {
  const currentMonth = new Date().getMonth()
  const currentYear = new Date().getFullYear()
  
  return transactions.value
    .filter(t => {
      const date = new Date(t.createdAt)
      return date.getMonth() === currentMonth && date.getFullYear() === currentYear
    })
    .reduce((total, t) => {
      return t.transactionType === 'TOPUP' || t.transactionType === 'TRANSFER_IN' || t.transactionType === 'REFUND'
        ? total + t.amount
        : total - t.amount
    }, 0)
})

const pendingTransactions = computed(() => 
  transactions.value.filter(t => t.status === 'PENDING').length
)

const failedTransactions = computed(() => 
  transactions.value.filter(t => t.status === 'FAILED').length
)

const totalPages = computed(() => Math.ceil(filteredTransactions.value.length / pageSize.value))

const visiblePages = computed(() => {
  const pages = []
  const maxVisible = 5
  const start = Math.max(1, currentPage.value - Math.floor(maxVisible / 2))
  const end = Math.min(totalPages.value, start + maxVisible - 1)
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  
  return pages
})

// Methods
const formatCurrency = (amount, currency) => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: currency
  }).format(amount)
}

const goToPage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

const refreshTransactions = async () => {
  loading.value = true
  try {
    // In real app, this would fetch from API
    // For now, we'll just use existing data
    await new Promise(resolve => setTimeout(resolve, 500))
  } finally {
    loading.value = false
  }
}

const exportTransactions = () => {
  // Create CSV content
  const headers = ['Date', 'Type', 'Description', 'Amount', 'Status', 'Reference']
  const csvContent = [
    headers.join(','),
    ...filteredTransactions.value.map(t => [
      t.createdAt,
      t.transactionType,
      t.description || '',
      t.amount,
      t.status,
      t.transactionReference || ''
    ])
  ].join('\n')

  // Download CSV
  const blob = new Blob([csvContent], { type: 'text/csv' })
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `transactions-${new Date().toISOString().split('T')[0]}.csv`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  window.URL.revokeObjectURL(url)
}

// Lifecycle
onMounted(() => {
  // Fetch initial transactions
  refreshTransactions()
})
</script>
