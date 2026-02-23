<template>
  <div class="tenant-wallet p-6">
    <!-- Header -->
    <div class="mb-8">
      <div class="flex justify-between items-start">
        <div>
          <h1 class="text-3xl font-bold text-gray-900 dark:text-gray-100 mb-2">
            {{ currentTenant?.name }} - Wallet
          </h1>
          <p class="text-gray-600 dark:text-gray-400">
            Manage your wallet balance and transactions
          </p>
        </div>
        
        <!-- Quick Actions -->
        <div class="flex gap-3">
          <button 
            @click="showTopupModal = true"
            class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors flex items-center gap-2"
          >
            <Icon icon="ic:baseline-add-circle" />
            Top Up
          </button>
          <button 
            @click="showTransferModal = true"
            class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center gap-2"
          >
            <Icon icon="ic:baseline-swap-horiz" />
            Transfer
          </button>
        </div>
      </div>
    </div>

    <!-- Wallet Overview -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
      <!-- Main Wallet Balance -->
      <div class="lg:col-span-2">
        <WalletManager />
      </div>

      <!-- Quick Stats Sidebar -->
      <div class="space-y-6">
        <!-- Monthly Summary -->
        <div class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-6">
          <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">
            Monthly Summary
          </h3>
          <div class="space-y-4">
            <div class="flex justify-between items-center">
              <span class="text-sm text-gray-600 dark:text-gray-400">Income</span>
              <span class="font-semibold text-green-600 dark:text-green-400">
                +${{ formatCurrency(monthlyStats.topup) }}
              </span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-sm text-gray-600 dark:text-gray-400">Expenses</span>
              <span class="font-semibold text-red-600 dark:text-red-400">
                -${{ formatCurrency(monthlyStats.spent) }}
              </span>
            </div>
            <div class="border-t border-gray-200 dark:border-gray-600 pt-2">
              <div class="flex justify-between items-center">
                <span class="text-sm font-medium text-gray-900 dark:text-gray-100">Net Change</span>
                <span :class="netChangeClass" class="font-bold">
                  {{ netChangePrefix }}${{ formatCurrency(Math.abs(monthlyStats.net)) }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Transaction Types -->
        <div class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-6">
          <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">
            Transaction Types
          </h3>
          <div class="space-y-3">
            <div 
              v-for="(transactions, type) in transactionsByType" 
              :key="type"
              class="flex justify-between items-center"
            >
              <div class="flex items-center gap-2">
                <div :class="getTransactionTypeClass(type)">
                  <Icon :icon="getTransactionTypeIcon(type)" class="text-sm" />
                </div>
                <span class="text-sm text-gray-900 dark:text-gray-100">
                  {{ formatTransactionType(type) }}
                </span>
              </div>
              <span class="text-sm font-medium text-gray-900 dark:text-gray-100">
                {{ transactions.length }}
              </span>
            </div>
          </div>
        </div>

        <!-- Quick Actions -->
        <div class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-6">
          <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">
            Quick Actions
          </h3>
          <div class="space-y-2">
            <button 
              @click="downloadStatement"
              class="w-full px-4 py-2 text-left bg-gray-50 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-600 transition-colors flex items-center gap-2"
            >
              <Icon icon="ic:baseline-download" />
              Download Statement
            </button>
            <button 
              @click="showAutoTopupSettings = true"
              class="w-full px-4 py-2 text-left bg-gray-50 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-600 transition-colors flex items-center gap-2"
            >
              <Icon icon="ic:baseline-settings" />
              Auto Top-up Settings
            </button>
            <button 
              @click="exportTransactions"
              class="w-full px-4 py-2 text-left bg-gray-50 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-600 transition-colors flex items-center gap-2"
            >
              <Icon icon="ic:baseline-file-download" />
              Export Transactions
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Recent Activity Chart -->
    <div class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-6 mb-8">
      <div class="flex justify-between items-center mb-6">
        <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100">
          Activity Overview
        </h3>
        <select 
          v-model="chartPeriod"
          class="px-3 py-1 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100"
        >
          <option value="7">Last 7 Days</option>
          <option value="30">Last 30 Days</option>
          <option value="90">Last 90 Days</option>
        </select>
      </div>
      
      <!-- Simple Chart Placeholder -->
      <div class="h-64 flex items-center justify-center bg-gray-50 dark:bg-gray-700 rounded-lg">
        <div class="text-center">
          <Icon icon="ic:baseline-bar-chart" class="text-4xl text-gray-400 mb-2" />
          <p class="text-gray-600 dark:text-gray-400">
            Transaction chart will be displayed here
          </p>
        </div>
      </div>
    </div>

    <!-- Recent Transactions Table -->
    <div class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700">
      <div class="p-6 border-b border-gray-200 dark:border-gray-700">
        <div class="flex justify-between items-center">
          <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100">
            Recent Transactions
          </h3>
          <button 
            @click="$router.push('/tenant/billing/transactions')"
            class="text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 text-sm font-medium"
          >
            View All
          </button>
        </div>
      </div>

      <div v-if="transactions.length === 0" class="text-center py-12">
        <Icon icon="ic:baseline-receipt-long" class="text-4xl text-gray-400" />
        <p class="text-gray-600 dark:text-gray-400 mt-2">No transactions found</p>
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
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200 dark:divide-gray-700">
            <tr 
              v-for="transaction in transactions.slice(0, 10)" 
              :key="transaction.id"
              class="hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
            >
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-100">
                {{ formatDate(transaction.createdAt) }}
              </td>
              <td class="px-6 py-4 text-sm text-gray-900 dark:text-gray-100">
                {{ transaction.description || getTransactionDescription(transaction.transactionType) }}
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
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modals -->
    <TopupModal 
      v-if="showTopupModal"
      :wallet="currentWallet"
      @close="showTopupModal = false"
      @topped-up="onToppedUp"
    />

    <TransferModal 
      v-if="showTransferModal"
      :wallet="currentWallet"
      @close="showTransferModal = false"
      @transferred="onTransferred"
    />

    <AutoTopupSettingsModal 
      v-if="showAutoTopupSettings"
      :wallet="currentWallet"
      @close="showAutoTopupSettings = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useGatewayTenantStore } from '@/stores/tenant/gateway/myTenantStore'
import { useWalletStore } from '@/stores/wallet/walletStore'
import WalletManager from '@/components/wallet/WalletManager.vue'
import TopupModal from '@/components/wallet/TopupModal.vue'
import TransferModal from '@/components/wallet/TransferModal.vue'
import AutoTopupSettingsModal from '@/components/wallet/AutoTopupSettingsModal.vue'

const router = useRouter()
const tenantStore = useGatewayTenantStore()
const walletStore = useWalletStore()

// State
const chartPeriod = ref('30')
const showTopupModal = ref(false)
const showTransferModal = ref(false)
const showAutoTopupSettings = ref(false)

// Computed
const currentTenant = computed(() => tenantStore.currentTenant)
const currentWallet = computed(() => walletStore.currentWallet)
const transactions = computed(() => walletStore.recentTransactions)
const transactionsByType = computed(() => walletStore.transactionsByType)

const monthlyStats = computed(() => ({
  spent: walletStore.monthlySpending,
  topup: walletStore.monthlyTopup,
  net: walletStore.monthlyTopup - walletStore.monthlySpending
}))

const netChangeClass = computed(() => {
  return monthlyStats.value.net >= 0 
    ? 'text-green-600 dark:text-green-400'
    : 'text-red-600 dark:text-red-400'
})

const netChangePrefix = computed(() => {
  return monthlyStats.value.net >= 0 ? '+' : '-'
})

// Methods
const getTransactionTypeIcon = (type) => {
  const iconMap = {
    'TOPUP': 'ic:baseline-add-circle',
    'PURCHASE': 'ic:baseline-shopping-cart',
    'TRANSFER_IN': 'ic:baseline-arrow-downward',
    'TRANSFER_OUT': 'ic:baseline-arrow-upward',
    'REFUND': 'ic:baseline-refresh',
    'FEE': 'ic:baseline-receipt'
  }
  return iconMap[type] || 'ic:baseline-receipt-long'
}

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

const formatTransactionType = (type) => {
  return type.replace(/_/g, ' ').toLowerCase()
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
    day: 'numeric'
  })
}

const downloadStatement = () => {
  // Generate and download wallet statement
  console.log('Download statement')
}

const exportTransactions = () => {
  // Export transactions to CSV/Excel
  console.log('Export transactions')
}

const onToppedUp = (transaction) => {
  // Refresh wallet data
  const tenantId = localStorage.getItem('ACTIVE_TENANT_ID')
  walletStore.fetchWallets(tenantId)
  walletStore.fetchTransactions(currentWallet.value?.id)
}

const onTransferred = (transaction) => {
  // Refresh wallet data
  const tenantId = localStorage.getItem('ACTIVE_TENANT_ID')
  walletStore.fetchWallets(tenantId)
  walletStore.fetchTransactions(currentWallet.value?.id)
}

// Lifecycle
onMounted(() => {
  const tenantId = localStorage.getItem('ACTIVE_TENANT_ID')
  if (tenantId) {
    walletStore.fetchWallets(tenantId)
  }
})
</script>

<style scoped>
.tenant-wallet {
  max-width: 1400px;
  margin: 0 auto;
}
</style>
