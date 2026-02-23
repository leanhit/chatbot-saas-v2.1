<template>
  <div class="wallet-manager">
    <!-- Header -->
    <div class="flex justify-between items-center mb-6">
      <div>
        <h2 class="text-xl font-semibold text-gray-900 dark:text-gray-100">
          Wallet Management
        </h2>
        <p class="text-gray-600 dark:text-gray-400">
          Manage your wallets and payment methods
        </p>
      </div>
      <button 
        @click="showCreateWallet = true"
        class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center gap-2"
      >
        <Icon icon="ic:baseline-add" />
        Create Wallet
      </button>
    </div>

    <!-- Wallet Selector -->
    <div v-if="wallets.length > 1" class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-4 mb-6">
      <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
        Select Wallet
      </label>
      <select 
        v-model="selectedWalletId"
        @change="onWalletChange"
        class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
      >
        <option v-for="wallet in wallets" :key="wallet.id" :value="wallet.id">
          {{ wallet.name }} - ${{ formatCurrency(wallet.balance) }} ({{ wallet.currency }})
        </option>
      </select>
    </div>

    <!-- Current Wallet Overview -->
    <div v-if="currentWallet" class="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
      <!-- Wallet Balance Card -->
      <div class="lg:col-span-2">
        <WalletBalance 
          :wallet="currentWallet"
          :monthly-stats="monthlyStats"
          @topup="showTopupModal = true"
          @transactions="showTransactionsModal = true"
        />
      </div>

      <!-- Quick Stats -->
      <div class="space-y-4">
        <div class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-6">
          <div class="flex items-center gap-3 mb-4">
            <div class="p-2 bg-green-100 dark:bg-green-900/20 rounded-lg">
              <Icon icon="ic:baseline-trending-up" class="text-green-600 dark:text-green-400 text-xl" />
            </div>
            <div>
              <p class="text-sm text-gray-600 dark:text-gray-400">Monthly Top-up</p>
              <p class="text-xl font-bold text-gray-900 dark:text-gray-100">
                ${{ formatCurrency(monthlyStats.topup) }}
              </p>
            </div>
          </div>
        </div>

        <div class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-6">
          <div class="flex items-center gap-3 mb-4">
            <div class="p-2 bg-red-100 dark:bg-red-900/20 rounded-lg">
              <Icon icon="ic:baseline-trending-down" class="text-red-600 dark:text-red-400 text-xl" />
            </div>
            <div>
              <p class="text-sm text-gray-600 dark:text-gray-400">Monthly Spending</p>
              <p class="text-xl font-bold text-gray-900 dark:text-gray-100">
                ${{ formatCurrency(monthlyStats.spent) }}
              </p>
            </div>
          </div>
        </div>

        <div class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-6">
          <div class="flex items-center gap-3 mb-4">
            <div class="p-2 bg-blue-100 dark:bg-blue-900/20 rounded-lg">
              <Icon icon="ic:baseline-receipt-long" class="text-blue-600 dark:text-blue-400 text-xl" />
            </div>
            <div>
              <p class="text-sm text-gray-600 dark:text-gray-400">Transactions</p>
              <p class="text-xl font-bold text-gray-900 dark:text-gray-100">
                {{ monthlyStats.count }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Recent Transactions -->
    <div class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700">
      <div class="p-6 border-b border-gray-200 dark:border-gray-700">
        <div class="flex justify-between items-center">
          <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100">
            Recent Transactions
          </h3>
          <button 
            @click="showTransactionsModal = true"
            class="text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 text-sm font-medium"
          >
            View All
          </button>
        </div>
      </div>

      <div v-if="transactions.length === 0" class="text-center py-8">
        <Icon icon="ic:baseline-receipt-long" class="text-4xl text-gray-400" />
        <p class="text-gray-600 dark:text-gray-400 mt-2">No transactions found</p>
      </div>

      <div v-else class="divide-y divide-gray-200 dark:divide-gray-700">
        <div 
          v-for="transaction in transactions.slice(0, 10)" 
          :key="transaction.id"
          class="p-4 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
        >
          <div class="flex justify-between items-center">
            <div class="flex items-center gap-3">
              <div :class="getTransactionIconClass(transaction.transactionType)">
                <Icon :icon="getTransactionIcon(transaction.transactionType)" class="text-lg" />
              </div>
              <div>
                <p class="font-medium text-gray-900 dark:text-gray-100">
                  {{ transaction.description || getTransactionDescription(transaction.transactionType) }}
                </p>
                <p class="text-sm text-gray-600 dark:text-gray-400">
                  {{ formatDate(transaction.createdAt) }}
                </p>
              </div>
            </div>
            <div class="text-right">
              <p :class="getAmountClass(transaction.transactionType)" class="font-semibold">
                {{ getAmountPrefix(transaction.transactionType) }}${{ formatCurrency(transaction.amount) }}
              </p>
              <p :class="getStatusClass(transaction.status)" class="text-xs">
                {{ transaction.status }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modals -->
    <CreateWalletModal 
      v-if="showCreateWallet"
      @close="showCreateWallet = false"
      @created="onWalletCreated"
    />

    <TopupModal 
      v-if="showTopupModal"
      :wallet="currentWallet"
      @close="showTopupModal = false"
      @topped-up="onToppedUp"
    />

    <TransactionsModal 
      v-if="showTransactionsModal"
      :wallet="currentWallet"
      @close="showTransactionsModal = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useWalletStore } from '@/stores/wallet/walletStore'
import WalletBalance from './WalletBalance.vue'
import CreateWalletModal from './CreateWalletModal.vue'
import TopupModal from './TopupModal.vue'
import TransactionsModal from './TransactionsModal.vue'

const walletStore = useWalletStore()

// State
const selectedWalletId = ref(null)
const showCreateWallet = ref(false)
const showTopupModal = ref(false)
const showTransactionsModal = ref(false)

// Computed
const wallets = computed(() => walletStore.wallets)
const currentWallet = computed(() => {
  if (!selectedWalletId.value && wallets.value.length > 0) {
    selectedWalletId.value = wallets.value[0].id
  }
  return wallets.value.find(w => w.id === selectedWalletId.value)
})

const transactions = computed(() => walletStore.recentTransactions)
const monthlyStats = computed(() => ({
  spent: walletStore.monthlySpending,
  topup: walletStore.monthlyTopup,
  count: walletStore.transactionsByType['TOPUP']?.length + walletStore.transactionsByType['PURCHASE']?.length || 0
}))

// Methods
const onWalletChange = () => {
  const wallet = wallets.value.find(w => w.id === selectedWalletId.value)
  if (wallet) {
    walletStore.setCurrentWallet(wallet)
    walletStore.fetchTransactions(wallet.id)
  }
}

const getTransactionIcon = (type) => {
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

const getTransactionIconClass = (type) => {
  const classMap = {
    'TOPUP': 'p-2 bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400 rounded-lg',
    'PURCHASE': 'p-2 bg-red-100 text-red-600 dark:bg-red-900/20 dark:text-red-400 rounded-lg',
    'TRANSFER_IN': 'p-2 bg-blue-100 text-blue-600 dark:bg-blue-900/20 dark:text-blue-400 rounded-lg',
    'TRANSFER_OUT': 'p-2 bg-orange-100 text-orange-600 dark:bg-orange-900/20 dark:text-orange-400 rounded-lg',
    'REFUND': 'p-2 bg-purple-100 text-purple-600 dark:bg-purple-900/20 dark:text-purple-400 rounded-lg',
    'FEE': 'p-2 bg-gray-100 text-gray-600 dark:bg-gray-900/20 dark:text-gray-400 rounded-lg'
  }
  return classMap[type] || 'p-2 bg-gray-100 text-gray-600 dark:bg-gray-900/20 dark:text-gray-400 rounded-lg'
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

const getStatusClass = (status) => {
  switch (status?.toUpperCase()) {
    case 'COMPLETED':
      return 'text-green-600 dark:text-green-400'
    case 'PENDING':
      return 'text-yellow-600 dark:text-yellow-400'
    case 'FAILED':
      return 'text-red-600 dark:text-red-400'
    default:
      return 'text-gray-600 dark:text-gray-400'
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
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const onWalletCreated = (newWallet) => {
  showCreateWallet.value = false
  selectedWalletId.value = newWallet.id
  walletStore.setCurrentWallet(newWallet)
}

const onToppedUp = (transaction) => {
  showTopupModal.value = false
  // Refresh wallet and transactions
  walletStore.fetchWallets(getCurrentTenantId())
  walletStore.fetchTransactions(selectedWalletId.value)
}

const getCurrentTenantId = () => {
  return localStorage.getItem('ACTIVE_TENANT_ID')
}

// Lifecycle
onMounted(() => {
  const tenantId = getCurrentTenantId()
  walletStore.fetchWallets(tenantId)
})

// Watch for wallet changes
watch(currentWallet, (newWallet) => {
  if (newWallet) {
    walletStore.fetchTransactions(newWallet.id)
  }
}, { immediate: true })
</script>

<style scoped>
.wallet-manager {
  max-width: 1200px;
  margin: 0 auto;
}
</style>
