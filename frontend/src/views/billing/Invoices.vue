<template>
  <div class="invoices-page p-6">
    <!-- Header -->
    <div class="mb-6">
      <div class="flex justify-between items-center">
        <div>
          <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">
            Invoices
          </h1>
          <p class="text-gray-600 dark:text-gray-400">
            View and manage your billing invoices
          </p>
        </div>
        
        <div class="flex gap-3">
          <button 
            @click="downloadAllInvoices"
            class="px-4 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors flex items-center gap-2"
          >
            <Icon icon="ic:baseline-download" />
            Download All
          </button>
          <button 
            @click="refreshInvoices"
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
        <div class="flex-1">
          <div class="relative">
            <Icon icon="ic:baseline-search" class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
            <input 
              v-model="searchQuery"
              type="text"
              placeholder="Search invoices..."
              class="w-full pl-10 pr-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
        </div>

        <select 
          v-model="statusFilter"
          class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        >
          <option value="">All Status</option>
          <option value="PAID">Paid</option>
          <option value="PENDING">Pending</option>
          <option value="OVERDUE">Overdue</option>
          <option value="CANCELLED">Cancelled</option>
        </select>

        <select 
          v-model="dateRange"
          class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        >
          <option value="">All Time</option>
          <option value="30days">Last 30 Days</option>
          <option value="90days">Last 90 Days</option>
          <option value="6months">Last 6 Months</option>
          <option value="1year">Last Year</option>
        </select>

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
            <Icon icon="ic:baseline-receipt" class="text-blue-600 dark:text-blue-400 text-2xl" />
          </div>
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Total Invoices</p>
            <p class="text-2xl font-bold text-gray-900 dark:text-gray-100">{{ totalInvoices }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700">
        <div class="flex items-center gap-3">
          <div class="p-3 bg-green-100 dark:bg-green-900/20 rounded-lg">
            <Icon icon="ic:baseline-check-circle" class="text-green-600 dark:text-green-400 text-2xl" />
          </div>
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Paid</p>
            <p class="text-2xl font-bold text-green-600 dark:text-green-400">{{ paidInvoices }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700">
        <div class="flex items-center gap-3">
          <div class="p-3 bg-yellow-100 dark:bg-yellow-900/20 rounded-lg">
            <Icon icon="ic:baseline-schedule" class="text-yellow-600 dark:text-yellow-400 text-2xl" />
          </div>
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Pending</p>
            <p class="text-2xl font-bold text-yellow-600 dark:text-yellow-400">{{ pendingInvoices }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700">
        <div class="flex items-center gap-3">
          <div class="p-3 bg-red-100 dark:bg-red-900/20 rounded-lg">
            <Icon icon="ic:baseline-warning" class="text-red-600 dark:text-red-400 text-2xl" />
          </div>
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Overdue</p>
            <p class="text-2xl font-bold text-red-600 dark:text-red-400">{{ overdueInvoices }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Invoices Table -->
    <div v-if="isLoading" class="text-center py-12">
      <Icon icon="ic:baseline-refresh" class="animate-spin text-4xl text-gray-400" />
      <p class="text-gray-600 dark:text-gray-400 mt-2">Loading invoices...</p>
    </div>

    <div v-else-if="filteredInvoices.length === 0" class="text-center py-12">
      <Icon icon="ic:baseline-receipt" class="text-4xl text-gray-400" />
      <h3 class="text-lg font-medium text-gray-900 dark:text-gray-100 mt-4">
        No invoices found
      </h3>
      <p class="text-gray-600 dark:text-gray-400 mt-2">
        {{ searchQuery ? 'Try adjusting your search' : 'No invoices available' }}
      </p>
    </div>

    <div v-else class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead class="bg-gray-50 dark:bg-gray-700 border-b border-gray-200 dark:border-gray-700">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                Invoice Number
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                Date
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                Due Date
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
              v-for="invoice in paginatedInvoices" 
              :key="invoice.id"
              class="hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
            >
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex items-center gap-2">
                  <Icon :icon="getInvoiceIcon(invoice.status)" :class="getInvoiceIconClass(invoice.status)" class="text-lg" />
                  <span class="text-sm font-medium text-gray-900 dark:text-gray-100">
                    #{{ invoice.invoiceNumber }}
                  </span>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-100">
                {{ formatDate(invoice.createdAt) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-100">
                {{ formatDate(invoice.dueDate) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900 dark:text-gray-100">
                ${{ formatCurrency(invoice.amount) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span :class="getInvoiceStatusClass(invoice.status)" class="px-2 py-1 rounded-full text-xs font-medium">
                  {{ invoice.status }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm">
                <div class="flex gap-2">
                  <button 
                    @click="viewInvoice(invoice)"
                    class="text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300"
                    title="View Invoice"
                  >
                    <Icon icon="ic:baseline-visibility" />
                  </button>
                  <button 
                    @click="downloadInvoice(invoice)"
                    class="text-green-600 dark:text-green-400 hover:text-green-700 dark:hover:text-green-300"
                    title="Download Invoice"
                  >
                    <Icon icon="ic:baseline-download" />
                  </button>
                  <button 
                    v-if="invoice.status === 'PENDING'"
                    @click="payInvoice(invoice)"
                    class="text-purple-600 dark:text-purple-400 hover:text-purple-700 dark:hover:text-purple-300"
                    title="Pay Invoice"
                  >
                    <Icon icon="ic:baseline-attach-money" />
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
            Showing {{ (currentPage - 1) * pageSize + 1 }} to {{ Math.min(currentPage * pageSize, filteredInvoices.length) }} of {{ filteredInvoices.length }} invoices
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

    <!-- Invoice Detail Modal -->
    <InvoiceDetailModal 
      v-if="showDetailModal"
      :invoice="selectedInvoice"
      @close="showDetailModal = false"
      @paid="onInvoicePaid"
    />

    <!-- Payment Modal -->
    <PaymentModal 
      v-if="showPaymentModal"
      :invoice="selectedInvoice"
      @close="showPaymentModal = false"
      @paid="onInvoicePaid"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useBillingStore } from '@/stores/billing/billingStore'
import InvoiceDetailModal from '@/components/billing/InvoiceDetailModal.vue'
import PaymentModal from '@/components/billing/PaymentModal.vue'

const billingStore = useBillingStore()

// State
const isLoading = ref(false)
const searchQuery = ref('')
const statusFilter = ref('')
const dateRange = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const showDetailModal = ref(false)
const showPaymentModal = ref(false)
const selectedInvoice = ref(null)

// Mock invoices data
const invoices = ref([
  {
    id: '1',
    invoiceNumber: 'INV-2024-001',
    amount: 149.98,
    status: 'PAID',
    createdAt: '2024-01-01T00:00:00Z',
    dueDate: '2024-01-15T00:00:00Z',
    paidAt: '2024-01-14T10:30:00Z'
  },
  {
    id: '2',
    invoiceNumber: 'INV-2024-002',
    amount: 149.98,
    status: 'PENDING',
    createdAt: '2024-02-01T00:00:00Z',
    dueDate: '2024-02-15T00:00:00Z',
    paidAt: null
  },
  {
    id: '3',
    invoiceNumber: 'INV-2024-003',
    amount: 199.99,
    status: 'OVERDUE',
    createdAt: '2023-12-01T00:00:00Z',
    dueDate: '2023-12-15T00:00:00Z',
    paidAt: null
  },
  {
    id: '4',
    invoiceNumber: 'INV-2024-004',
    amount: 99.99,
    status: 'CANCELLED',
    createdAt: '2023-11-01T00:00:00Z',
    dueDate: '2023-11-15T00:00:00Z',
    paidAt: null
  }
])

// Computed
const filteredInvoices = computed(() => {
  let filtered = invoices.value

  // Apply search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(invoice => 
      invoice.invoiceNumber.toLowerCase().includes(query)
    )
  }

  // Apply status filter
  if (statusFilter.value) {
    filtered = filtered.filter(invoice => invoice.status === statusFilter.value)
  }

  // Apply date range filter
  if (dateRange.value) {
    const now = new Date()
    let cutoffDate = new Date()

    switch (dateRange.value) {
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

    filtered = filtered.filter(invoice => new Date(invoice.createdAt) >= cutoffDate)
  }

  return filtered.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
})

const totalInvoices = computed(() => invoices.value.length)
const paidInvoices = computed(() => invoices.value.filter(i => i.status === 'PAID').length)
const pendingInvoices = computed(() => invoices.value.filter(i => i.status === 'PENDING').length)
const overdueInvoices = computed(() => invoices.value.filter(i => i.status === 'OVERDUE').length)

const totalPages = computed(() => Math.ceil(filteredInvoices.value.length / pageSize.value))

const paginatedInvoices = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredInvoices.value.slice(start, end)
})

// Methods
const getInvoiceIcon = (status) => {
  switch (status?.toUpperCase()) {
    case 'PAID':
      return 'ic:baseline-check-circle'
    case 'PENDING':
      return 'ic:baseline-schedule'
    case 'OVERDUE':
      return 'ic:baseline-warning'
    case 'CANCELLED':
      return 'ic:baseline-cancel'
    default:
      return 'ic:baseline-receipt'
  }
}

const getInvoiceIconClass = (status) => {
  switch (status?.toUpperCase()) {
    case 'PAID':
      return 'text-green-600 dark:text-green-400'
    case 'PENDING':
      return 'text-yellow-600 dark:text-yellow-400'
    case 'OVERDUE':
      return 'text-red-600 dark:text-red-400'
    case 'CANCELLED':
      return 'text-gray-600 dark:text-gray-400'
    default:
      return 'text-gray-600'
  }
}

const getInvoiceStatusClass = (status) => {
  switch (status?.toUpperCase()) {
    case 'PAID':
      return 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400'
    case 'PENDING':
      return 'bg-yellow-100 text-yellow-600 dark:bg-yellow-900/20 dark:text-yellow-400'
    case 'OVERDUE':
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
  statusFilter.value = ''
  dateRange.value = ''
  currentPage.value = 1
}

const refreshInvoices = async () => {
  isLoading.value = true
  
  try {
    await billingStore.fetchInvoices()
    // Update local state with store data
    // invoices.value = billingStore.invoices
  } catch (error) {
    console.error('Failed to refresh invoices:', error)
  } finally {
    isLoading.value = false
  }
}

const viewInvoice = (invoice) => {
  selectedInvoice.value = invoice
  showDetailModal.value = true
}

const downloadInvoice = (invoice) => {
  // Simulate invoice download
  const invoiceData = {
    invoice: invoice,
    downloadedAt: new Date().toISOString()
  }

  const blob = new Blob([JSON.stringify(invoiceData, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `invoice-${invoice.invoiceNumber}.json`
  link.click()
  URL.revokeObjectURL(url)
}

const payInvoice = (invoice) => {
  selectedInvoice.value = invoice
  showPaymentModal.value = true
}

const downloadAllInvoices = () => {
  // Simulate bulk download
  const allInvoicesData = {
    invoices: filteredInvoices.value,
    downloadedAt: new Date().toISOString(),
    filters: {
      searchQuery: searchQuery.value,
      statusFilter: statusFilter.value,
      dateRange: dateRange.value
    }
  }

  const blob = new Blob([JSON.stringify(allInvoicesData, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `invoices-${new Date().toISOString().split('T')[0]}.json`
  link.click()
  URL.revokeObjectURL(url)
}

const onInvoicePaid = (paidInvoice) => {
  showDetailModal.value = false
  showPaymentModal.value = false
  selectedInvoice.value = null
  
  // Update in local state
  const index = invoices.value.findIndex(inv => inv.id === paidInvoice.id)
  if (index !== -1) {
    invoices.value[index] = paidInvoice
  }
}

// Lifecycle
onMounted(() => {
  refreshInvoices()
})
</script>

<style scoped>
.invoices-page {
  max-width: 1400px;
  margin: 0 auto;
}
</style>
