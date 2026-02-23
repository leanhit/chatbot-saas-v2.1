<template>
  <div class="invoices-list">
    <!-- Header -->
    <div class="flex justify-between items-center mb-6">
      <div>
        <h2 class="text-xl font-semibold text-gray-900 dark:text-gray-100">
          Invoices
        </h2>
        <p class="text-gray-600 dark:text-gray-400">
          View and download your billing invoices
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
          @click="showFilterModal = true"
          class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center gap-2"
        >
          <Icon icon="ic:baseline-filter-list" />
          Filter
        </button>
      </div>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
      <div class="bg-white dark:bg-gray-800 rounded-lg p-4 border border-gray-200 dark:border-gray-700">
        <div class="flex items-center gap-3">
          <div class="p-2 bg-blue-100 dark:bg-blue-900/20 rounded-lg">
            <Icon icon="ic:baseline-receipt-long" class="text-blue-600 dark:text-blue-400 text-xl" />
          </div>
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Total Invoices</p>
            <p class="text-xl font-bold text-gray-900 dark:text-gray-100">{{ invoices.length }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white dark:bg-gray-800 rounded-lg p-4 border border-gray-200 dark:border-gray-700">
        <div class="flex items-center gap-3">
          <div class="p-2 bg-green-100 dark:bg-green-900/20 rounded-lg">
            <Icon icon="ic:baseline-check-circle" class="text-green-600 dark:text-green-400 text-xl" />
          </div>
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Paid</p>
            <p class="text-xl font-bold text-gray-900 dark:text-gray-100">{{ paidCount }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white dark:bg-gray-800 rounded-lg p-4 border border-gray-200 dark:border-gray-700">
        <div class="flex items-center gap-3">
          <div class="p-2 bg-yellow-100 dark:bg-yellow-900/20 rounded-lg">
            <Icon icon="ic:baseline-pending" class="text-yellow-600 dark:text-yellow-400 text-xl" />
          </div>
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Pending</p>
            <p class="text-xl font-bold text-gray-900 dark:text-gray-100">{{ pendingCount }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white dark:bg-gray-800 rounded-lg p-4 border border-gray-200 dark:border-gray-700">
        <div class="flex items-center gap-3">
          <div class="p-2 bg-red-100 dark:bg-red-900/20 rounded-lg">
            <Icon icon="ic:baseline-error" class="text-red-600 dark:text-red-400 text-xl" />
          </div>
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Overdue</p>
            <p class="text-xl font-bold text-gray-900 dark:text-gray-100">{{ overdueCount }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Search and Filter -->
    <div class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-4 mb-6">
      <div class="flex flex-col sm:flex-row gap-4">
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
          v-model="dateFilter"
          class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        >
          <option value="">All Time</option>
          <option value="7">Last 7 Days</option>
          <option value="30">Last 30 Days</option>
          <option value="90">Last 90 Days</option>
          <option value="365">Last Year</option>
        </select>
      </div>
    </div>

    <!-- Invoices Table -->
    <div class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700">
      <div v-if="isLoading" class="text-center py-12">
        <Icon icon="ic:baseline-refresh" class="animate-spin text-4xl text-gray-400" />
        <p class="text-gray-600 dark:text-gray-400 mt-2">Loading invoices...</p>
      </div>

      <div v-else-if="filteredInvoices.length === 0" class="text-center py-12">
        <Icon icon="ic:baseline-receipt-long" class="text-4xl text-gray-400" />
        <h3 class="text-lg font-medium text-gray-900 dark:text-gray-100 mt-4">
          No invoices found
        </h3>
        <p class="text-gray-600 dark:text-gray-400 mt-2">
          {{ searchQuery ? 'Try adjusting your search' : 'No invoices match your criteria' }}
        </p>
      </div>

      <div v-else class="overflow-x-auto">
        <table class="w-full">
          <thead class="bg-gray-50 dark:bg-gray-700 border-b border-gray-200 dark:border-gray-700">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                Invoice
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
              <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
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
                <div class="flex items-center gap-3">
                  <div class="p-2 bg-gray-100 dark:bg-gray-700 rounded-lg">
                    <Icon icon="ic:baseline-receipt-long" class="text-gray-600 dark:text-gray-400" />
                  </div>
                  <div>
                    <p class="font-medium text-gray-900 dark:text-gray-100">
                      #{{ invoice.invoiceNumber }}
                    </p>
                    <p class="text-sm text-gray-600 dark:text-gray-400">
                      {{ invoice.description || 'Monthly subscription' }}
                    </p>
                  </div>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-100">
                {{ formatDate(invoice.createdAt) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-100">
                {{ formatDate(invoice.dueDate) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <p class="text-sm font-semibold text-gray-900 dark:text-gray-100">
                  ${{ formatCurrency(invoice.amount) }}
                </p>
                <p v-if="invoice.taxAmount" class="text-xs text-gray-600 dark:text-gray-400">
                  incl. ${{ formatCurrency(invoice.taxAmount) }} tax
                </p>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span :class="getStatusClass(invoice.status)" class="px-2 py-1 rounded-full text-xs font-medium">
                  {{ invoice.status }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-right">
                <div class="flex justify-end gap-2">
                  <button 
                    @click="downloadInvoice(invoice)"
                    class="p-2 text-gray-600 dark:text-gray-400 hover:text-blue-600 dark:hover:text-blue-400 transition-colors"
                    title="Download Invoice"
                  >
                    <Icon icon="ic:baseline-download" />
                  </button>
                  <button 
                    @click="viewInvoice(invoice)"
                    class="p-2 text-gray-600 dark:text-gray-400 hover:text-blue-600 dark:hover:text-blue-400 transition-colors"
                    title="View Invoice"
                  >
                    <Icon icon="ic:baseline-visibility" />
                  </button>
                  <button 
                    v-if="invoice.status === 'PENDING' || invoice.status === 'OVERDUE'"
                    @click="payInvoice(invoice)"
                    class="p-2 text-gray-600 dark:text-gray-400 hover:text-green-600 dark:hover:text-green-400 transition-colors"
                    title="Pay Invoice"
                  >
                    <Icon icon="ic:baseline-credit-card" />
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
            Showing {{ (currentPage - 1) * pageSize + 1 }} to {{ Math.min(currentPage * pageSize, filteredInvoices.length) }} of {{ filteredInvoices.length }} results
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

    <!-- Filter Modal -->
    <InvoiceFilterModal 
      v-if="showFilterModal"
      :filters="currentFilters"
      @close="showFilterModal = false"
      @apply="applyFilters"
    />

    <!-- Invoice Detail Modal -->
    <InvoiceDetailModal 
      v-if="showDetailModal"
      :invoice="selectedInvoice"
      @close="showDetailModal = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useBillingStore } from '@/stores/billing/billingStore'
import InvoiceFilterModal from './InvoiceFilterModal.vue'
import InvoiceDetailModal from './InvoiceDetailModal.vue'

const billingStore = useBillingStore()

// State
const searchQuery = ref('')
const statusFilter = ref('')
const dateFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const showFilterModal = ref(false)
const showDetailModal = ref(false)
const selectedInvoice = ref(null)

// Computed
const invoices = computed(() => billingStore.invoices)
const isLoading = computed(() => billingStore.isLoading)

const filteredInvoices = computed(() => {
  let filtered = invoices.value

  // Apply search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(invoice => 
      invoice.invoiceNumber?.toLowerCase().includes(query) ||
      invoice.description?.toLowerCase().includes(query)
    )
  }

  // Apply status filter
  if (statusFilter.value) {
    filtered = filtered.filter(invoice => invoice.status === statusFilter.value)
  }

  // Apply date filter
  if (dateFilter.value) {
    const days = parseInt(dateFilter.value)
    const cutoffDate = new Date()
    cutoffDate.setDate(cutoffDate.getDate() - days)
    
    filtered = filtered.filter(invoice => 
      new Date(invoice.createdAt) >= cutoffDate
    )
  }

  return filtered.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
})

const paginatedInvoices = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredInvoices.value.slice(start, end)
})

const totalPages = computed(() => 
  Math.ceil(filteredInvoices.value.length / pageSize.value)
)

const paidCount = computed(() => 
  invoices.value.filter(i => i.status === 'PAID').length
)

const pendingCount = computed(() => 
  invoices.value.filter(i => i.status === 'PENDING').length
)

const overdueCount = computed(() => 
  invoices.value.filter(i => i.status === 'OVERDUE').length
)

const currentFilters = computed(() => ({
  search: searchQuery.value,
  status: statusFilter.value,
  dateRange: dateFilter.value
}))

// Methods
const getStatusClass = (status) => {
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
      return 'bg-gray-100 text-gray-600 dark:bg-gray-900/20 dark:text-gray-400'
  }
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
    day: 'numeric'
  })
}

const downloadInvoice = async (invoice) => {
  try {
    // Generate PDF download URL
    const url = `/billing/invoices/${invoice.id}/download`
    const link = document.createElement('a')
    link.href = url
    link.download = `invoice-${invoice.invoiceNumber}.pdf`
    link.click()
  } catch (error) {
    console.error('Failed to download invoice:', error)
  }
}

const viewInvoice = (invoice) => {
  selectedInvoice.value = invoice
  showDetailModal.value = true
}

const payInvoice = (invoice) => {
  // Navigate to payment page or open payment modal
  console.log('Pay invoice:', invoice)
}

const downloadAllInvoices = async () => {
  try {
    // Generate ZIP download URL
    const url = `/billing/invoices/download-all`
    const link = document.createElement('a')
    link.href = url
    link.download = `invoices-${new Date().toISOString().split('T')[0]}.zip`
    link.click()
  } catch (error) {
    console.error('Failed to download all invoices:', error)
  }
}

const applyFilters = (filters) => {
  searchQuery.value = filters.search || ''
  statusFilter.value = filters.status || ''
  dateFilter.value = filters.dateRange || ''
  currentPage.value = 1 // Reset to first page
  showFilterModal.value = false
}

// Lifecycle
onMounted(() => {
  const tenantId = localStorage.getItem('ACTIVE_TENANT_ID')
  if (tenantId) {
    billingStore.fetchInvoices(tenantId)
  }
})
</script>

<style scoped>
.invoices-list {
  max-width: 1400px;
  margin: 0 auto;
}
</style>
