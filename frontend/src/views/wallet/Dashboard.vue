<template>
  <div class="wallet-dashboard p-6">
    <!-- Header -->
    <div class="mb-6">
      <div class="flex justify-between items-center">
        <div>
          <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">
            Wallet Dashboard
          </h1>
          <p class="text-gray-600 dark:text-gray-400">
            Manage your wallets and financial operations
          </p>
        </div>
        
        <div class="flex gap-3">
          <button 
            @click="showCreateWallet = true"
            class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center gap-2"
          >
            <Icon icon="ic:baseline-add" />
            Create Wallet
          </button>
          <button 
            @click="refreshWallets"
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

    <!-- Wallet Overview Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700">
        <div class="flex items-center gap-3">
          <div class="p-3 bg-blue-100 dark:bg-blue-900/20 rounded-lg">
            <Icon icon="ic:baseline-account-balance-wallet" class="text-blue-600 dark:text-blue-400 text-2xl" />
          </div>
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Total Balance</p>
            <p class="text-2xl font-bold text-gray-900 dark:text-gray-100">${{ formatCurrency(totalBalance) }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700">
        <div class="flex items-center gap-3">
          <div class="p-3 bg-green-100 dark:bg-green-900/20 rounded-lg">
            <Icon icon="ic:baseline-account-balance" class="text-green-600 dark:text-green-400 text-2xl" />
          </div>
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Active Wallets</p>
            <p class="text-2xl font-bold text-green-600 dark:text-green-400">{{ activeWallets }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700">
        <div class="flex items-center gap-3">
          <div class="p-3 bg-yellow-100 dark:bg-yellow-900/20 rounded-lg">
            <Icon icon="ic:baseline-trending-up" class="text-yellow-600 dark:text-yellow-400 text-2xl" />
          </div>
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Monthly Spending</p>
            <p class="text-2xl font-bold text-yellow-600 dark:text-yellow-400">${{ formatCurrency(monthlySpending) }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700">
        <div class="flex items-center gap-3">
          <div class="p-3 bg-purple-100 dark:bg-purple-900/20 rounded-lg">
            <Icon icon="ic:baseline-swap-horiz" class="text-purple-600 dark:text-purple-400 text-2xl" />
          </div>
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Transactions</p>
            <p class="text-2xl font-bold text-purple-600 dark:text-purple-400">{{ totalTransactions }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <button 
        @click="navigateToTransactions"
        class="p-4 bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 hover:border-blue-500 hover:shadow-lg transition-all duration-200"
      >
        <div class="flex flex-col items-center text-center">
          <Icon icon="ic:baseline-receipt-long" class="text-3xl text-blue-600 dark:text-blue-400 mb-2" />
          <h3 class="font-semibold text-gray-900 dark:text-gray-100">Transactions</h3>
          <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">View transaction history</p>
        </div>
      </button>

      <button 
        @click="showTopupModal = true"
        class="p-4 bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 hover:border-blue-500 hover:shadow-lg transition-all duration-200"
      >
        <div class="flex flex-col items-center text-center">
          <Icon icon="ic:baseline-add-circle" class="text-3xl text-green-600 dark:text-green-400 mb-2" />
          <h3 class="font-semibold text-gray-900 dark:text-gray-100">Add Funds</h3>
          <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">Top up your wallet</p>
        </div>
      </button>

      <button 
        @click="showTransferModal = true"
        class="p-4 bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 hover:border-blue-500 hover:shadow-lg transition-all duration-200"
      >
        <div class="flex flex-col items-center text-center">
          <Icon icon="ic:baseline-swap-horiz" class="text-3xl text-purple-600 dark:text-purple-400 mb-2" />
          <h3 class="font-semibold text-gray-900 dark:text-gray-100">Transfer</h3>
          <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">Transfer funds</p>
        </div>
      </button>

      <button 
        @click="showCreateWallet = true"
        class="p-4 bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 hover:border-blue-500 hover:shadow-lg transition-all duration-200"
      >
        <div class="flex flex-col items-center text-center">
          <Icon icon="ic:baseline-account-balance-wallet" class="text-3xl text-orange-600 dark:text-orange-400 mb-2" />
          <h3 class="font-semibold text-gray-900 dark:text-gray-100">New Wallet</h3>
          <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">Create new wallet</p>
        </div>
      </button>
    </div>

    <!-- Wallets List -->
    <div v-if="isLoading" class="text-center py-12">
      <Icon icon="ic:baseline-refresh" class="animate-spin text-4xl text-gray-400" />
      <p class="text-gray-600 dark:text-gray-400 mt-2">Loading wallets...</p>
    </div>

    <div v-else-if="wallets.length === 0" class="text-center py-12">
      <Icon icon="ic:baseline-account-balance-wallet" class="text-4xl text-gray-400" />
      <h3 class="text-lg font-medium text-gray-900 dark:text-gray-100 mt-4">
        No Wallets
      </h3>
      <p class="text-gray-600 dark:text-gray-400 mt-2">
        Get started by creating your first wallet
      </p>
      <button 
        @click="showCreateWallet = true"
        class="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
      >
        Create Your First Wallet
      </button>
    </div>

    <div v-else class="space-y-6">
      <div 
        v-for="wallet in wallets" 
        :key="wallet.id"
        class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 overflow-hidden hover:shadow-lg transition-shadow"
      >
        <!-- Wallet Header -->
        <div class="p-6 border-b border-gray-200 dark:border-gray-700">
          <div class="flex justify-between items-start">
            <div class="flex items-center gap-4">
              <div :class="getWalletIconClass(wallet.type)" class="p-3 rounded-lg">
                <Icon :icon="getWalletIcon(wallet.type)" class="text-xl" />
              </div>
              <div>
                <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100">
                  {{ wallet.name }}
                </h3>
                <p class="text-sm text-gray-600 dark:text-gray-400">
                  {{ wallet.description }}
                </p>
                <div class="flex items-center gap-2 mt-2">
                  <span :class="getStatusClass(wallet.status)" class="px-2 py-1 rounded-full text-xs font-medium">
                    {{ wallet.status }}
                  </span>
                  <span class="px-2 py-1 bg-gray-100 dark:bg-gray-700 rounded-full text-xs font-medium text-gray-600 dark:text-gray-400">
                    {{ wallet.currency }}
                  </span>
                </div>
              </div>
            </div>
            
            <!-- Actions -->
            <div class="flex gap-2">
              <button 
                @click="topupWallet(wallet)"
                class="p-2 text-green-600 dark:text-green-400 hover:text-green-700 dark:hover:text-green-300 transition-colors"
                title="Add Funds"
              >
                <Icon icon="ic:baseline-add-circle" />
              </button>
              <button 
                @click="transferFromWallet(wallet)"
                class="p-2 text-purple-600 dark:text-purple-400 hover:text-purple-700 dark:hover:text-purple-300 transition-colors"
                title="Transfer Funds"
              >
                <Icon icon="ic:baseline-swap-horiz" />
              </button>
              <button 
                @click="viewWalletDetails(wallet)"
                class="p-2 text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 transition-colors"
                title="View Details"
              >
                <Icon icon="ic:baseline-visibility" />
              </button>
            </div>
          </div>
        </div>

        <!-- Wallet Details -->
        <div class="p-6">
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <!-- Balance -->
            <div>
              <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Balance</h4>
              <p class="text-2xl font-bold text-gray-900 dark:text-gray-100">
                {{ wallet.currency }} {{ formatCurrency(wallet.balance) }}
              </p>
              <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">
                ${{ formatCurrency(wallet.balanceUSD) }} USD
              </p>
            </div>

            <!-- Auto Top-up -->
            <div>
              <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Auto Top-up</h4>
              <div class="space-y-1 text-sm">
                <div class="flex justify-between">
                  <span class="text-gray-600 dark:text-gray-400">Enabled:</span>
                  <span :class="wallet.autoTopupEnabled ? 'text-green-600 dark:text-green-400' : 'text-gray-600 dark:text-gray-400'">
                    {{ wallet.autoTopupEnabled ? 'Yes' : 'No' }}
                  </span>
                </div>
                <div v-if="wallet.autoTopupEnabled">
                  <div class="flex justify-between">
                    <span class="text-gray-600 dark:text-gray-400">Threshold:</span>
                    <span class="text-gray-900 dark:text-gray-100">
                      {{ wallet.currency }} {{ formatCurrency(wallet.autoTopupThreshold) }}
                    </span>
                  </div>
                  <div class="flex justify-between">
                    <span class="text-gray-600 dark:text-gray-400">Amount:</span>
                    <span class="text-gray-900 dark:text-gray-100">
                      {{ wallet.currency }} {{ formatCurrency(wallet.autoTopupAmount) }}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Recent Activity -->
            <div>
              <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Recent Activity</h4>
              <div class="space-y-1 text-sm">
                <div class="flex justify-between">
                  <span class="text-gray-600 dark:text-gray-400">Last Transaction:</span>
                  <span class="text-gray-900 dark:text-gray-100">
                    {{ wallet.lastTransaction ? formatDate(wallet.lastTransaction.date) : 'No activity' }}
                  </span>
                </div>
                <div v-if="wallet.lastTransaction">
                  <div class="flex justify-between">
                    <span class="text-gray-600 dark:text-gray-400">Amount:</span>
                    <span :class="getTransactionAmountClass(wallet.lastTransaction)">
                      {{ wallet.lastTransaction.type === 'CREDIT' ? '+' : '-' }}{{ wallet.currency }} {{ formatCurrency(wallet.lastTransaction.amount) }}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Features -->
            <div>
              <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Features</h4>
              <div class="space-y-1 text-sm">
                <div v-for="feature in wallet.features.slice(0, 3)" :key="feature" class="flex items-center gap-1">
                  <Icon icon="ic:baseline-check" class="text-green-500" />
                  <span class="text-gray-600 dark:text-gray-400">{{ feature }}</span>
                </div>
                <div v-if="wallet.features.length > 3" class="text-gray-500 dark:text-gray-400">
                  +{{ wallet.features.length - 3 }} more features
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Create Wallet Modal -->
    <CreateWalletModal 
      v-if="showCreateWallet"
      @close="showCreateWallet = false"
      @created="onWalletCreated"
    />

    <!-- Topup Modal -->
    <TopupModal 
      v-if="showTopupModal"
      :wallet="selectedWallet"
      @close="showTopupModal = false"
      @topped="onWalletTopped"
    />

    <!-- Transfer Modal -->
    <TransferModal 
      v-if="showTransferModal"
      :from-wallet="selectedWallet"
      @close="showTransferModal = false"
      @transferred="onTransferCompleted"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useWalletStore } from '@/stores/wallet/walletStore'
import CreateWalletModal from '@/components/wallet/CreateWalletModal.vue'
import TopupModal from '@/components/wallet/TopupModal.vue'
import TransferModal from '@/components/wallet/TransferModal.vue'

const router = useRouter()
const walletStore = useWalletStore()

// State
const isLoading = ref(false)
const showCreateWallet = ref(false)
const showTopupModal = ref(false)
const showTransferModal = ref(false)
const selectedWallet = ref(null)

// Mock wallets data
const wallets = ref([
  {
    id: '1',
    name: 'Primary Wallet',
    description: 'Main wallet for daily transactions',
    type: 'PERSONAL',
    currency: 'USD',
    balance: 5000.00,
    balanceUSD: 5000.00,
    status: 'ACTIVE',
    autoTopupEnabled: true,
    autoTopupThreshold: 100.00,
    autoTopupAmount: 500.00,
    features: ['Auto Top-up', 'Instant Transfer', 'Multi-currency', 'API Access'],
    lastTransaction: {
      type: 'CREDIT',
      amount: 250.00,
      date: '2024-01-20T14:30:00Z'
    }
  },
  {
    id: '2',
    name: 'Business Wallet',
    description: 'Business expenses and operations',
    type: 'BUSINESS',
    currency: 'USD',
    balance: 12500.00,
    balanceUSD: 12500.00,
    status: 'ACTIVE',
    autoTopupEnabled: false,
    autoTopupThreshold: 0,
    autoTopupAmount: 0,
    features: ['Multi-user Access', 'Expense Tracking', 'Reporting', 'Integration'],
    lastTransaction: {
      type: 'DEBIT',
      amount: 750.00,
      date: '2024-01-19T10:15:00Z'
    }
  },
  {
    id: '3',
    name: 'Savings Wallet',
    description: 'Long-term savings and investments',
    type: 'SAVINGS',
    currency: 'USD',
    balance: 25000.00,
    balanceUSD: 25000.00,
    status: 'ACTIVE',
    autoTopupEnabled: true,
    autoTopupThreshold: 1000.00,
    autoTopupAmount: 1000.00,
    features: ['High Interest', 'Locked Savings', 'Goal Setting', 'Auto-save'],
    lastTransaction: {
      type: 'CREDIT',
      amount: 1000.00,
      date: '2024-01-15T09:00:00Z'
    }
  }
])

// Computed
const totalBalance = computed(() => {
  return wallets.value.reduce((total, wallet) => total + wallet.balanceUSD, 0)
})

const activeWallets = computed(() => {
  return wallets.value.filter(wallet => wallet.status === 'ACTIVE').length
})

const monthlySpending = computed(() => {
  return walletStore.monthlySpending || 2500.00
})

const totalTransactions = computed(() => {
  return walletStore.totalTransactions || 156
})

// Methods
const getWalletIcon = (type) => {
  const iconMap = {
    'PERSONAL': 'ic:baseline-person',
    'BUSINESS': 'ic:baseline-business',
    'SAVINGS': 'ic:baseline-savings',
    'INVESTMENT': 'ic:baseline-trending-up'
  }
  return iconMap[type] || 'ic:baseline-account-balance-wallet'
}

const getWalletIconClass = (type) => {
  const classMap = {
    'PERSONAL': 'bg-blue-100 text-blue-600 dark:bg-blue-900/20 dark:text-blue-400',
    'BUSINESS': 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400',
    'SAVINGS': 'bg-purple-100 text-purple-600 dark:bg-purple-900/20 dark:text-purple-400',
    'INVESTMENT': 'bg-orange-100 text-orange-600 dark:bg-orange-900/20 dark:text-orange-400'
  }
  return classMap[type] || 'bg-gray-100 text-gray-600'
}

const getStatusClass = (status) => {
  switch (status?.toUpperCase()) {
    case 'ACTIVE':
      return 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400'
    case 'INACTIVE':
      return 'bg-gray-100 text-gray-600 dark:bg-gray-900/20 dark:text-gray-400'
    case 'FROZEN':
      return 'bg-red-100 text-red-600 dark:bg-red-900/20 dark:text-red-400'
    default:
      return 'bg-gray-100 text-gray-600'
  }
}

const getTransactionAmountClass = (transaction) => {
  return transaction.type === 'CREDIT' 
    ? 'text-green-600 dark:text-green-400' 
    : 'text-red-600 dark:text-red-400'
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

const refreshWallets = async () => {
  isLoading.value = true
  
  try {
    await walletStore.fetchWallets()
    // Update local state with store data
    // wallets.value = walletStore.wallets
  } catch (error) {
    console.error('Failed to refresh wallets:', error)
  } finally {
    isLoading.value = false
  }
}

const navigateToTransactions = () => {
  router.push('/wallet/transactions')
}

const topupWallet = (wallet) => {
  selectedWallet.value = wallet
  showTopupModal.value = true
}

const transferFromWallet = (wallet) => {
  selectedWallet.value = wallet
  showTransferModal.value = true
}

const viewWalletDetails = (wallet) => {
  // Navigate to wallet details or show modal
  console.log('View wallet details:', wallet)
}

const onWalletCreated = (newWallet) => {
  showCreateWallet.value = false
  wallets.value.push(newWallet)
  refreshWallets()
}

const onWalletTopped = (updatedWallet) => {
  showTopupModal.value = false
  selectedWallet.value = null
  
  // Update in local state
  const index = wallets.value.findIndex(w => w.id === updatedWallet.id)
  if (index !== -1) {
    wallets.value[index] = updatedWallet
  }
}

const onTransferCompleted = (transferData) => {
  showTransferModal.value = false
  selectedWallet.value = null
  
  // Update wallet balances based on transfer
  // This would typically come from the API response
  refreshWallets()
}

// Lifecycle
onMounted(() => {
  refreshWallets()
})
</script>

<style scoped>
.wallet-dashboard {
  max-width: 1400px;
  margin: 0 auto;
}
</style>
