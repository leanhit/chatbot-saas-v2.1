<template>
  <div class="invoices-page min-h-screen bg-gray-50 dark:bg-gray-900">
    <!-- Header -->
    <div class="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Invoices</h1>
            <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
              View and download your billing invoices
            </p>
          </div>
          <div class="flex items-center space-x-3">
            <!-- Status Filter -->
            <select v-model="filterStatus" class="text-sm border rounded px-3 py-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white">
              <option value="">All Status</option>
              <option value="PAID">Paid</option>
              <option value="PENDING">Pending</option>
              <option value="OVERDUE">Overdue</option>
              <option value="CANCELLED">Cancelled</option>
            </select>

            <!-- Date Range -->
            <input
              v-model="dateFilter"
              type="month"
              class="text-sm border rounded px-3 py-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
            />

            <!-- Search -->
            <div class="relative">
              <input
                v-model="searchQuery"
                type="text"
                placeholder="Search invoices..."
                class="text-sm border rounded pl-10 pr-3 py-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
              />
              <Icon icon="mdi:magnify" class="absolute left-3 top-2.5 w-4 h-4 text-gray-400" />
            </div>

            <!-- Export Button -->
            <button
              @click="exportInvoices"
              class="flex items-center px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700"
            >
              <Icon icon="mdi:download" class="w-4 h-4 mr-2" />
              Export
            </button>

            <button
              @click="$router.push('/billing/overview')"
              class="flex items-center px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700"
            >
              <Icon icon="mdi:arrow-left" class="w-4 h-4 mr-2" />
              Back to Overview
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
              <p class="text-sm font-medium text-gray-600 dark:text-gray-400">Total Invoices</p>
              <p class="text-2xl font-bold text-gray-900 dark:text-white">{{ totalInvoices }}</p>
            </div>
            <div class="w-12 h-12 bg-blue-100 dark:bg-blue-900 rounded-full flex items-center justify-center">
              <Icon icon="mdi:receipt" class="w-6 h-6 text-blue-600 dark:text-blue-400" />
            </div>
          </div>
        </div>

        <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-600 dark:text-gray-400">Total Billed</p>
              <p class="text-2xl font-bold text-gray-900 dark:text-white">{{ formatCurrency(totalBilled, 'USD') }}</p>
            </div>
            <div class="w-12 h-12 bg-green-100 dark:bg-green-900 rounded-full flex items-center justify-center">
              <Icon icon="mdi:cash-usd" class="w-6 h-6 text-green-600 dark:text-green-400" />
            </div>
          </div>
        </div>

        <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-600 dark:text-gray-400">Pending</p>
              <p class="text-2xl font-bold text-yellow-600 dark:text-yellow-400">{{ pendingInvoices }}</p>
            </div>
            <div class="w-12 h-12 bg-yellow-100 dark:bg-yellow-900 rounded-full flex items-center justify-center">
              <Icon icon="mdi:clock" class="w-6 h-6 text-yellow-600 dark:text-yellow-400" />
            </div>
          </div>
        </div>

        <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-600 dark:text-gray-400">Overdue</p>
              <p class="text-2xl font-bold text-red-600 dark:text-red-400">{{ overdueInvoices }}</p>
            </div>
            <div class="w-12 h-12 bg-red-100 dark:bg-red-900 rounded-full flex items-center justify-center">
              <Icon icon="mdi:alert-circle" class="w-6 h-6 text-red-600 dark:text-red-400" />
            </div>
          </div>
        </div>
      </div>

      <!-- Invoices List -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow">
        <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
          <div class="flex items-center justify-between">
            <h2 class="text-lg font-semibold text-gray-900 dark:text-white">
              Invoice History
            </h2>
            <div class="flex items-center space-x-2">
              <span class="text-sm text-gray-500 dark:text-gray-400">
                {{ filteredInvoices.length }} of {{ totalInvoices }} invoices
              </span>
              <button
                @click="refreshInvoices"
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
            <p class="text-gray-500 dark:text-gray-400">Loading invoices...</p>
          </div>

          <!-- Empty State -->
          <div v-else-if="filteredInvoices.length === 0" class="p-8 text-center">
            <Icon icon="mdi:file-document-outline" class="w-12 h-12 text-gray-400 mx-auto mb-3" />
            <p class="text-gray-500 dark:text-gray-400">No invoices found</p>
            <p class="text-sm text-gray-400 dark:text-gray-500 mt-1">
              Try adjusting your filters or check back later for new invoices.
            </p>
          </div>

          <!-- Invoice Items -->
          <InvoiceItem
            v-for="invoice in paginatedInvoices"
            :key="invoice.id"
            :invoice="invoice"
          />
        </div>

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="px-6 py-4 border-t border-gray-200 dark:border-gray-700">
          <div class="flex items-center justify-between">
            <span class="text-sm text-gray-700 dark:text-gray-300">
              Showing {{ (currentPage - 1) * pageSize + 1 }} to {{ Math.min(currentPage * pageSize, filteredInvoices.length) }} of {{ totalInvoices }}
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

      <!-- Billing Summary -->
      <div class="mt-8 bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">Billing Summary</h2>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div>
            <h3 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">This Month</h3>
            <p class="text-2xl font-bold text-gray-900 dark:text-white">{{ formatCurrency(thisMonthTotal, 'USD') }}</p>
            <p class="text-sm text-gray-500 dark:text-gray-400">{{ thisMonthInvoices }} invoices</p>
          </div>
          <div>
            <h3 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Last Month</h3>
            <p class="text-2xl font-bold text-gray-900 dark:text-white">{{ formatCurrency(lastMonthTotal, 'USD') }}</p>
            <p class="text-sm text-gray-500 dark:text-gray-400">{{ lastMonthInvoices }} invoices</p>
          </div>
          <div>
            <h3 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Year to Date</h3>
            <p class="text-2xl font-bold text-gray-900 dark:text-white">{{ formatCurrency(yearToDateTotal, 'USD') }}</p>
            <p class="text-sm text-gray-500 dark:text-gray-400">{{ yearToDateInvoices }} invoices</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useBillingStore } from '@/stores/billingStore'
import InvoiceItem from '@/components/billing/InvoiceItem.vue'

const billingStore = useBillingStore()

// State
const loading = ref(false)
const filterStatus = ref('')
const dateFilter = ref('')
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(20)

// Computed
const invoices = computed(() => billingStore.recentInvoices)

const filteredInvoices = computed(() => {
  let filtered = invoices.value

  // Filter by status
  if (filterStatus.value) {
    filtered = filtered.filter(i => i.status === filterStatus.value)
  }

  // Filter by date
  if (dateFilter.value) {
    const filterMonth = new Date(dateFilter.value)
    filtered = filtered.filter(i => {
      const invoiceDate = new Date(i.createdAt)
      return invoiceDate.getMonth() === filterMonth.getMonth() &&
             invoiceDate.getFullYear() === filterMonth.getFullYear()
    })
  }

  // Search
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(i => 
      i.invoiceNumber?.toLowerCase().includes(query) ||
      i.subscriptionName?.toLowerCase().includes(query) ||
      i.paymentMethod?.toLowerCase().includes(query)
    )
  }

  return filtered
})

const paginatedInvoices = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredInvoices.value.slice(start, end)
})

const totalInvoices = computed(() => invoices.value.length)
const totalBilled = computed(() => 
  invoices.value.reduce((total, i) => total + (i.amount || 0), 0)
)
const pendingInvoices = computed(() => 
  invoices.value.filter(i => i.status === 'PENDING').length
)
const overdueInvoices = computed(() => 
  invoices.value.filter(i => i.status === 'OVERDUE').length
)

// Monthly calculations
const thisMonthInvoices = computed(() => {
  const now = new Date()
  return invoices.value.filter(i => {
    const date = new Date(i.createdAt)
    return date.getMonth() === now.getMonth() && date.getFullYear() === now.getFullYear()
  }).length
})

const thisMonthTotal = computed(() => {
  const now = new Date()
  return invoices.value
    .filter(i => {
      const date = new Date(i.createdAt)
      return date.getMonth() === now.getMonth() && date.getFullYear() === now.getFullYear()
    })
    .reduce((total, i) => total + (i.amount || 0), 0)
})

const lastMonthInvoices = computed(() => {
  const now = new Date()
  const lastMonth = new Date(now.getFullYear(), now.getMonth() - 1, 1)
  return invoices.value.filter(i => {
    const date = new Date(i.createdAt)
    return date.getMonth() === lastMonth.getMonth() && date.getFullYear() === lastMonth.getFullYear()
  }).length
})

const lastMonthTotal = computed(() => {
  const now = new Date()
  const lastMonth = new Date(now.getFullYear(), now.getMonth() - 1, 1)
  return invoices.value
    .filter(i => {
      const date = new Date(i.createdAt)
      return date.getMonth() === lastMonth.getMonth() && date.getFullYear() === lastMonth.getFullYear()
    })
    .reduce((total, i) => total + (i.amount || 0), 0)
})

const yearToDateInvoices = computed(() => {
  const now = new Date()
  return invoices.value.filter(i => {
    const date = new Date(i.createdAt)
    return date.getFullYear() === now.getFullYear()
  }).length
})

const yearToDateTotal = computed(() => {
  const now = new Date()
  return invoices.value
    .filter(i => {
      const date = new Date(i.createdAt)
      return date.getFullYear() === now.getFullYear()
    })
    .reduce((total, i) => total + (i.amount || 0), 0)
})

const totalPages = computed(() => Math.ceil(filteredInvoices.value.length / pageSize.value))

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

const refreshInvoices = async () => {
  loading.value = true
  try {
    await billingStore.fetchRecentInvoices()
  } finally {
    loading.value = false
  }
}

const exportInvoices = () => {
  // Create CSV content
  const headers = ['Invoice Number', 'Date', 'Status', 'Amount', 'Currency', 'Due Date', 'Paid Date']
  const csvContent = [
    headers.join(','),
    ...filteredInvoices.value.map(i => [
      i.invoiceNumber || '',
      i.createdAt || '',
      i.status || '',
      i.amount || 0,
      i.currency || 'USD',
      i.dueDate || '',
      i.paidAt || ''
    ])
  ].join('\n')

  // Download CSV
  const blob = new Blob([csvContent], { type: 'text/csv' })
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `invoices-${new Date().toISOString().split('T')[0]}.csv`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  window.URL.revokeObjectURL(url)
}

// Lifecycle
onMounted(async () => {
  await refreshInvoices()
})
</script>
