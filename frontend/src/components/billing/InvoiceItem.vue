<template>
  <div class="invoice-item border-b border-gray-200 dark:border-gray-700 last:border-b-0">
    <div class="p-4">
      <div class="flex items-center justify-between">
        <!-- Invoice Info -->
        <div class="flex items-center space-x-3">
          <div class="w-10 h-10 rounded-full flex items-center justify-center"
               :class="getStatusIconBackground()">
            <Icon :icon="getStatusIcon()" class="w-5 h-5" :class="getStatusIconClass()" />
          </div>
          <div>
            <div class="flex items-center space-x-2">
              <p class="font-medium text-gray-900 dark:text-white">
                Invoice #{{ invoice.invoiceNumber }}
              </p>
              <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium"
                    :class="getStatusClass()">
                {{ invoice.status }}
              </span>
            </div>
            <p class="text-sm text-gray-500 dark:text-gray-400">
              {{ formatDate(invoice.dueDate) }}
            </p>
            <p class="text-xs text-gray-400 dark:text-gray-500">
              Period: {{ formatDate(invoice.periodStart) }} - {{ formatDate(invoice.periodEnd) }}
            </p>
          </div>
        </div>

        <!-- Amount & Actions -->
        <div class="text-right">
          <p class="font-semibold text-gray-900 dark:text-white">
            {{ formatCurrency(invoice.amount, invoice.currency) }}
          </p>
          <p v-if="invoice.paidAt" class="text-sm text-green-600 dark:text-green-400">
            Paid {{ formatDate(invoice.paidAt) }}
          </p>
          <div class="flex items-center space-x-2 mt-2">
            <button
              @click="downloadInvoice"
              class="p-2 text-gray-400 hover:text-gray-500 dark:hover:text-gray-300"
              title="Download invoice"
            >
              <Icon icon="mdi:download" class="w-4 h-4" />
            </button>
            <button
              @click="showDetails = !showDetails"
              class="p-2 text-gray-400 hover:text-gray-500 dark:hover:text-gray-300"
              title="View details"
            >
              <Icon :icon="showDetails ? 'mdi:chevron-up' : 'mdi:chevron-down'" class="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>

      <!-- Expandable Details -->
      <div v-if="showDetails" class="mt-4 pt-4 border-t border-gray-200 dark:border-gray-700">
        <div class="grid grid-cols-2 gap-4 text-sm">
          <div>
            <span class="text-gray-500 dark:text-gray-400">Invoice ID:</span>
            <span class="ml-2 font-mono text-gray-900 dark:text-white">{{ invoice.id }}</span>
          </div>
          <div>
            <span class="text-gray-500 dark:text-gray-400">Subscription:</span>
            <span class="ml-2 text-gray-900 dark:text-white">{{ invoice.subscriptionName }}</span>
          </div>
          <div>
            <span class="text-gray-500 dark:text-gray-400">Billing Period:</span>
            <span class="ml-2 text-gray-900 dark:text-white">{{ invoice.billingCycle }}</span>
          </div>
          <div>
            <span class="text-gray-500 dark:text-gray-400">Payment Method:</span>
            <span class="ml-2 text-gray-900 dark:text-white">{{ invoice.paymentMethod }}</span>
          </div>
          <div>
            <span class="text-gray-500 dark:text-gray-400">Created:</span>
            <span class="ml-2 text-gray-900 dark:text-white">{{ formatDate(invoice.createdAt) }}</span>
          </div>
          <div>
            <span class="text-gray-500 dark:text-gray-400">Tax:</span>
            <span class="ml-2 text-gray-900 dark:text-white">{{ formatCurrency(invoice.tax || 0, invoice.currency) }}</span>
          </div>
        </div>

        <!-- Line Items -->
        <div v-if="invoice.lineItems && invoice.lineItems.length > 0" class="mt-4">
          <h4 class="text-sm font-medium text-gray-900 dark:text-white mb-2">Line Items</h4>
          <div class="space-y-2">
            <div v-for="item in invoice.lineItems" :key="item.id" class="flex justify-between text-sm">
              <span class="text-gray-600 dark:text-gray-400">{{ item.description }}</span>
              <span class="text-gray-900 dark:text-white">{{ formatCurrency(item.amount, invoice.currency) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  invoice: {
    type: Object,
    required: true
  }
})

const showDetails = ref(false)

// Invoice status configurations
const invoiceStatusConfig = {
  PAID: {
    icon: 'mdi:check-circle',
    iconBg: 'bg-green-100 dark:bg-green-900',
    iconColor: 'text-green-600 dark:text-green-400',
    statusClass: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
  },
  PENDING: {
    icon: 'mdi:clock-outline',
    iconBg: 'bg-yellow-100 dark:bg-yellow-900',
    iconColor: 'text-yellow-600 dark:text-yellow-400',
    statusClass: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200'
  },
  OVERDUE: {
    icon: 'mdi:alert-circle',
    iconBg: 'bg-red-100 dark:bg-red-900',
    iconColor: 'text-red-600 dark:text-red-400',
    statusClass: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
  },
  CANCELLED: {
    icon: 'mdi:close-circle',
    iconBg: 'bg-gray-100 dark:bg-gray-900',
    iconColor: 'text-gray-600 dark:text-gray-400',
    statusClass: 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
  }
}

const getStatusIcon = () => {
  const config = invoiceStatusConfig[props.invoice.status]
  return config?.icon || 'mdi:help-circle'
}

const getStatusIconBackground = () => {
  const config = invoiceStatusConfig[props.invoice.status]
  return config?.iconBg || 'bg-gray-100 dark:bg-gray-900'
}

const getStatusIconClass = () => {
  const config = invoiceStatusConfig[props.invoice.status]
  return config?.iconColor || 'text-gray-600 dark:text-gray-400'
}

const getStatusClass = () => {
  const config = invoiceStatusConfig[props.invoice.status]
  return config?.statusClass || 'bg-gray-100 text-gray-800'
}

const formatCurrency = (amount, currency) => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: currency
  }).format(amount)
}

const formatDate = (date) => {
  if (!date) return 'N/A'
  return new Intl.DateTimeFormat('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric'
  }).format(new Date(date))
}

const downloadInvoice = () => {
  // In real app, this would call API to download PDF
  console.log('Downloading invoice:', props.invoice.id)
  // For demo, create a simple text download
  const invoiceText = `
Invoice #${props.invoice.invoiceNumber}
Status: ${props.invoice.status}
Amount: ${formatCurrency(props.invoice.amount, props.invoice.currency)}
Due Date: ${formatDate(props.invoice.dueDate)}
Period: ${formatDate(props.invoice.periodStart)} - ${formatDate(props.invoice.periodEnd)}
  `.trim()

  const blob = new Blob([invoiceText], { type: 'text/plain' })
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `invoice-${props.invoice.invoiceNumber}.txt`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  window.URL.revokeObjectURL(url)
}
</script>
