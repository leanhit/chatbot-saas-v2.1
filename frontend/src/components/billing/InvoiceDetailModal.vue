<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white dark:bg-gray-800 rounded-lg max-w-4xl w-full mx-4 max-h-[90vh] overflow-y-auto">
      <!-- Header -->
      <div class="p-6 border-b border-gray-200 dark:border-gray-700">
        <div class="flex justify-between items-center">
          <div>
            <h3 class="text-xl font-semibold text-gray-900 dark:text-gray-100">
              Invoice Details
            </h3>
            <p class="text-gray-600 dark:text-gray-400">
              Invoice #{{ invoice.invoiceNumber }}
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

      <!-- Invoice Content -->
      <div class="p-6">
        <!-- Invoice Overview -->
        <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-6 mb-6">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">Invoice Information</h4>
              <div class="space-y-2 text-sm">
                <div class="flex justify-between">
                  <span class="text-gray-600 dark:text-gray-400">Invoice Number:</span>
                  <span class="font-medium text-gray-900 dark:text-gray-100">{{ invoice.invoiceNumber }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-gray-600 dark:text-gray-400">Status:</span>
                  <span :class="getStatusClass(invoice.status)" class="px-2 py-1 rounded-full text-xs font-medium">
                    {{ invoice.status }}
                  </span>
                </div>
                <div class="flex justify-between">
                  <span class="text-gray-600 dark:text-gray-400">Issue Date:</span>
                  <span class="font-medium text-gray-900 dark:text-gray-100">{{ formatDate(invoice.createdAt) }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-gray-600 dark:text-gray-400">Due Date:</span>
                  <span class="font-medium text-gray-900 dark:text-gray-100">{{ formatDate(invoice.dueDate) }}</span>
                </div>
                <div v-if="invoice.paidAt" class="flex justify-between">
                  <span class="text-gray-600 dark:text-gray-400">Paid Date:</span>
                  <span class="font-medium text-gray-900 dark:text-gray-100">{{ formatDate(invoice.paidAt) }}</span>
                </div>
              </div>
            </div>

            <div>
              <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">Amount</h4>
              <div class="space-y-2 text-sm">
                <div class="flex justify-between">
                  <span class="text-gray-600 dark:text-gray-400">Subtotal:</span>
                  <span class="font-medium text-gray-900 dark:text-gray-100">${{ formatCurrency(invoice.subtotal) }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-gray-600 dark:text-gray-400">Tax:</span>
                  <span class="font-medium text-gray-900 dark:text-gray-100">${{ formatCurrency(invoice.tax) }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-gray-600 dark:text-gray-400">Total:</span>
                  <span class="text-xl font-bold text-gray-900 dark:text-gray-100">${{ formatCurrency(invoice.amount) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Billing Information -->
        <div class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-6 mb-6">
          <h4 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">Billing Information</h4>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h5 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">Billed To</h5>
              <div class="space-y-1 text-sm">
                <p class="font-medium text-gray-900 dark:text-gray-100">{{ invoice.billingAddress.name }}</p>
                <p class="text-gray-600 dark:text-gray-400">{{ invoice.billingAddress.company }}</p>
                <p class="text-gray-600 dark:text-gray-400">{{ invoice.billingAddress.address }}</p>
                <p class="text-gray-600 dark:text-gray-400">{{ invoice.billingAddress.city }}, {{ invoice.billingAddress.state }} {{ invoice.billingAddress.zip }}</p>
                <p class="text-gray-600 dark:text-gray-400">{{ invoice.billingAddress.country }}</p>
                <p class="text-gray-600 dark:text-gray-400">{{ invoice.billingAddress.email }}</p>
              </div>
            </div>

            <div>
              <h5 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">Payment Method</h5>
              <div class="space-y-1 text-sm">
                <p class="font-medium text-gray-900 dark:text-gray-100">{{ invoice.paymentMethod.type }}</p>
                <p class="text-gray-600 dark:text-gray-400" v-if="invoice.paymentMethod.last4">
                  **** {{ invoice.paymentMethod.last4 }}
                </p>
                <p class="text-gray-600 dark:text-gray-400" v-if="invoice.paymentMethod.email">
                  {{ invoice.paymentMethod.email }}
                </p>
              </div>
            </div>
          </div>
        </div>

        <!-- Line Items -->
        <div class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 overflow-hidden">
          <div class="overflow-x-auto">
            <table class="w-full">
              <thead class="bg-gray-50 dark:bg-gray-700">
                <tr>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                    Description
                  </th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                    Quantity
                  </th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                    Unit Price
                  </th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                    Total
                  </th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-200 dark:divide-gray-700">
                <tr v-for="item in invoice.lineItems" :key="item.id">
                  <td class="px-6 py-4 whitespace-nowrap">
                    <div>
                      <p class="text-sm font-medium text-gray-900 dark:text-gray-100">{{ item.description }}</p>
                      <p v-if="item.details" class="text-xs text-gray-600 dark:text-gray-400">{{ item.details }}</p>
                    </div>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-100">
                    {{ item.quantity }}
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-100">
                    ${{ formatCurrency(item.unitPrice) }}
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900 dark:text-gray-100">
                    ${{ formatCurrency(item.total) }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Notes -->
        <div v-if="invoice.notes" class="bg-gray-50 dark:bg-gray-700 rounded-lg p-6 mb-6">
          <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Notes</h4>
          <p class="text-sm text-gray-600 dark:text-gray-400">{{ invoice.notes }}</p>
        </div>

        <!-- Actions -->
        <div class="flex justify-between items-center">
          <div class="text-sm text-gray-600 dark:text-gray-400">
            Last updated: {{ formatDate(invoice.updatedAt) }}
          </div>
          
          <div class="flex gap-3">
            <button 
              @click="downloadInvoice"
              class="px-4 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors flex items-center gap-2"
            >
              <Icon icon="ic:baseline-download" />
              Download
            </button>
            
            <button 
              v-if="invoice.status === 'PENDING'"
              @click="payInvoice"
              class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors flex items-center gap-2"
            >
              <Icon icon="ic:baseline-attach-money" />
              Pay Invoice
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  invoice: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'paid'])

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

const downloadInvoice = () => {
  // Simulate invoice download
  const invoiceData = {
    invoice: props.invoice,
    downloadedAt: new Date().toISOString()
  }

  const blob = new Blob([JSON.stringify(invoiceData, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `invoice-${props.invoice.invoiceNumber}.json`
  link.click()
  URL.revokeObjectURL(url)
}

const payInvoice = () => {
  // Simulate payment process
  const updatedInvoice = {
    ...props.invoice,
    status: 'PAID',
    paidAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  }
  
  emit('paid', updatedInvoice)
}
</script>

<style scoped>
.modal-backdrop {
  backdrop-filter: blur(4px);
}
</style>
