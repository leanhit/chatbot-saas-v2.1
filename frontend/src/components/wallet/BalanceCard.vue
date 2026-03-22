<template>
  <div class="balance-card bg-white dark:bg-gray-800 rounded-lg shadow-lg overflow-hidden hover:shadow-xl transition-shadow duration-300">
    <!-- Card Header -->
    <div class="relative h-32" :class="getCurrencyGradient()">
      <div class="absolute inset-0 bg-black opacity-10"></div>
      <div class="relative p-6 h-full flex flex-col justify-between">
        <div class="flex items-center justify-between">
          <div class="flex items-center space-x-3">
            <div class="w-10 h-10 bg-white bg-opacity-20 rounded-full flex items-center justify-center">
              <Icon :icon="getCurrencyIcon()" class="w-6 h-6 text-white" />
            </div>
            <div>
              <p class="text-white text-xs font-medium opacity-90">{{ balance.currency }}</p>
              <p class="text-white text-xs opacity-75">{{ getCurrencyName() }}</p>
            </div>
          </div>
          <div class="flex items-center space-x-2">
            <button
              @click="$emit('topup', balance)"
              class="p-2 bg-white bg-opacity-20 rounded-lg hover:bg-opacity-30 transition-colors"
              title="Top Up"
            >
              <Icon icon="mdi:plus" class="w-4 h-4 text-white" />
            </button>
            <button
              @click="$emit('transfer', balance)"
              class="p-2 bg-white bg-opacity-20 rounded-lg hover:bg-opacity-30 transition-colors"
              title="Transfer"
            >
              <Icon icon="mdi:swap-horizontal" class="w-4 h-4 text-white" />
            </button>
          </div>
        </div>
        
        <div class="text-right">
          <p class="text-white text-xs opacity-75">Available Balance</p>
          <p class="text-white text-2xl font-bold">
            {{ formatCurrency(balance.amount, balance.currency) }}
          </p>
        </div>
      </div>
    </div>

    <!-- Card Body -->
    <div class="p-4">
      <!-- Status Badge -->
      <div class="flex items-center justify-between mb-3">
        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
              :class="getStatusClass()">
          <Icon :icon="getStatusIcon()" class="w-3 h-3 mr-1" />
          {{ balance.status }}
        </span>
        <span class="text-xs text-gray-500 dark:text-gray-400">
          Updated {{ formatLastUpdated(balance.lastUpdated) }}
        </span>
      </div>

      <!-- Quick Stats -->
      <div class="grid grid-cols-2 gap-3 mb-4">
        <div class="text-center p-2 bg-gray-50 dark:bg-gray-700 rounded">
          <p class="text-xs text-gray-500 dark:text-gray-400">Today</p>
          <p class="text-sm font-semibold text-gray-900 dark:text-white">
            {{ formatCurrency(balance.todayChange || 0, balance.currency) }}
          </p>
        </div>
        <div class="text-center p-2 bg-gray-50 dark:bg-gray-700 rounded">
          <p class="text-xs text-gray-500 dark:text-gray-400">This Week</p>
          <p class="text-sm font-semibold text-gray-900 dark:text-white">
            {{ formatCurrency(balance.weekChange || 0, balance.currency) }}
          </p>
        </div>
      </div>

      <!-- Action Buttons -->
      <div class="flex space-x-2">
        <button
          @click="$emit('topup', balance)"
          class="flex-1 flex items-center justify-center px-3 py-2 bg-primary-600 text-white text-sm font-medium rounded hover:bg-primary-700 transition-colors"
        >
          <Icon icon="mdi:bank-transfer-in" class="w-4 h-4 mr-1" />
          Top Up
        </button>
        <button
          @click="$emit('transfer', balance)"
          class="flex-1 flex items-center justify-center px-3 py-2 border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 text-sm font-medium rounded hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
        >
          <Icon icon="mdi:bank-transfer-out" class="w-4 h-4 mr-1" />
          Transfer
        </button>
      </div>
    </div>

    <!-- Card Footer -->
    <div class="px-4 py-3 bg-gray-50 dark:bg-gray-700 border-t border-gray-200 dark:border-gray-600">
      <div class="flex items-center justify-between">
        <button
          @click="showDetails = !showDetails"
          class="text-xs text-primary-600 dark:text-primary-400 hover:text-primary-700 dark:hover:text-primary-300 font-medium"
        >
          {{ showDetails ? 'Hide' : 'Show' }} Details
          <Icon :icon="showDetails ? 'mdi:chevron-up' : 'mdi:chevron-down'" class="w-3 h-3 ml-1 inline" />
        </button>
        <div class="flex items-center space-x-2">
          <button
            @click="downloadStatement"
            class="text-xs text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300"
            title="Download Statement"
          >
            <Icon icon="mdi:download" class="w-4 h-4" />
          </button>
          <button
            @click="refreshBalance"
            class="text-xs text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300"
            title="Refresh"
          >
            <Icon icon="mdi:refresh" class="w-4 h-4" />
          </button>
        </div>
      </div>

      <!-- Expandable Details -->
      <div v-if="showDetails" class="mt-3 pt-3 border-t border-gray-200 dark:border-gray-600">
        <div class="space-y-2 text-xs">
          <div class="flex justify-between">
            <span class="text-gray-500 dark:text-gray-400">Wallet ID:</span>
            <span class="text-gray-900 dark:text-white font-mono">#{{ balance.walletId }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-500 dark:text-gray-400">Account Number:</span>
            <span class="text-gray-900 dark:text-white font-mono">{{ balance.accountNumber }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-500 dark:text-gray-400">Created:</span>
            <span class="text-gray-900 dark:text-white">{{ formatDate(balance.createdAt) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  balance: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['topup', 'transfer'])

const showDetails = ref(false)

// Currency configurations
const currencyConfig = {
  USD: {
    name: 'US Dollar',
    icon: 'mdi:currency-usd',
    gradient: 'bg-gradient-to-br from-green-500 to-green-600'
  },
  EUR: {
    name: 'Euro',
    icon: 'mdi:currency-eur',
    gradient: 'bg-gradient-to-br from-blue-500 to-blue-600'
  },
  GBP: {
    name: 'British Pound',
    icon: 'mdi:currency-gbp',
    gradient: 'bg-gradient-to-br from-purple-500 to-purple-600'
  },
  JPY: {
    name: 'Japanese Yen',
    icon: 'mdi:currency-jpy',
    gradient: 'bg-gradient-to-br from-red-500 to-red-600'
  },
  VND: {
    name: 'Vietnamese Dong',
    icon: 'mdi:currency-sign',
    gradient: 'bg-gradient-to-br from-orange-500 to-orange-600'
  }
}

const getCurrencyGradient = () => {
  return currencyConfig[props.balance.currency]?.gradient || 'bg-gradient-to-br from-gray-500 to-gray-600'
}

const getCurrencyIcon = () => {
  return currencyConfig[props.balance.currency]?.icon || 'mdi:currency-sign'
}

const getCurrencyName = () => {
  return currencyConfig[props.balance.currency]?.name || props.balance.currency
}

const getStatusClass = () => {
  const statusClasses = {
    ACTIVE: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
    SUSPENDED: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200',
    FROZEN: 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200',
    PENDING: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200'
  }
  return statusClasses[props.balance.status] || 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
}

const getStatusIcon = () => {
  const statusIcons = {
    ACTIVE: 'mdi:check-circle',
    SUSPENDED: 'mdi:pause-circle',
    FROZEN: 'mdi:snowflake',
    PENDING: 'mdi:clock'
  }
  return statusIcons[props.balance.status] || 'mdi:help-circle'
}

const formatCurrency = (amount, currency) => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: currency,
    minimumFractionDigits: currency === 'JPY' || currency === 'VND' ? 0 : 2
  }).format(amount)
}

const formatLastUpdated = (date) => {
  if (!date) return 'Never'
  
  const now = new Date()
  const updated = new Date(date)
  const diffMs = now - updated
  const diffMins = Math.floor(diffMs / 60000)
  
  if (diffMins < 1) return 'Just now'
  if (diffMins < 60) return `${diffMins}m ago`
  
  const diffHours = Math.floor(diffMins / 60)
  if (diffHours < 24) return `${diffHours}h ago`
  
  const diffDays = Math.floor(diffHours / 24)
  return `${diffDays}d ago`
}

const formatDate = (date) => {
  if (!date) return 'N/A'
  return new Intl.DateTimeFormat('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  }).format(new Date(date))
}

const downloadStatement = () => {
  // Implement statement download
  console.log('Download statement for wallet:', props.balance.walletId)
}

const refreshBalance = () => {
  // Implement balance refresh
  console.log('Refresh balance for wallet:', props.balance.walletId)
}
</script>
