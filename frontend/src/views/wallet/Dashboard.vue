<template>
  <div class="wallet-dashboard min-h-screen bg-gray-50 dark:bg-gray-900">
    <!-- Header Section -->
    <div class="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Wallet</h1>
            <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
              Manage your balances and transactions
            </p>
          </div>
          <div class="flex items-center space-x-3">
            <button
              @click="showNotifications = !showNotifications"
              class="relative p-2 text-gray-400 hover:text-gray-500 dark:hover:text-gray-300"
            >
              <Icon icon="mdi:bell-outline" class="w-6 h-6" />
              <span
                v-if="unreadCount > 0"
                class="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full"
              />
            </button>
            <button
              @click="openTopupModal"
              class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
            >
              <Icon icon="mdi:plus" class="w-4 h-4 mr-2" />
              Top Up
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Balance Cards Grid -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
        <BalanceCard
          v-for="balance in (balances || [])"
          :key="balance.currency"
          :balance="balance"
          @topup="openTopupModal"
          @transfer="openTransferModal"
        />
        
        <!-- Add New Wallet Card -->
        <div
          @click="showAddWalletModal = true"
          class="bg-gray-100 dark:bg-gray-800 border-2 border-dashed border-gray-300 dark:border-gray-600 rounded-lg p-6 flex flex-col items-center justify-center cursor-pointer hover:border-gray-400 dark:hover:border-gray-500 transition-colors"
        >
          <Icon icon="mdi:plus-circle-outline" class="w-12 h-12 text-gray-400 mb-3" />
          <span class="text-sm font-medium text-gray-600 dark:text-gray-400">
            Add New Wallet
          </span>
          <span class="text-xs text-gray-500 dark:text-gray-500 mt-1">
            Support for multiple currencies
          </span>
        </div>
      </div>

      <!-- Quick Actions & Stats -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
        <!-- Quick Actions -->
        <div class="lg:col-span-2">
          <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">
              Quick Actions
            </h2>
            <div class="grid grid-cols-2 sm:grid-cols-4 gap-4">
              <button
                @click="openTopupModal"
                class="flex flex-col items-center p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg hover:bg-blue-100 dark:hover:bg-blue-900/30 transition-colors"
              >
                <Icon icon="mdi:bank-transfer-in" class="w-8 h-8 text-blue-600 dark:text-blue-400 mb-2" />
                <span class="text-sm font-medium text-gray-900 dark:text-white">Top Up</span>
              </button>
              
              <button
                @click="openTransferModal"
                class="flex flex-col items-center p-4 bg-green-50 dark:bg-green-900/20 rounded-lg hover:bg-green-100 dark:hover:bg-green-900/30 transition-colors"
              >
                <Icon icon="mdi:bank-transfer-out" class="w-8 h-8 text-green-600 dark:text-green-400 mb-2" />
                <span class="text-sm font-medium text-gray-900 dark:text-white">Transfer</span>
              </button>
              
              <button
                @click="$router.push('/wallet/transactions')"
                class="flex flex-col items-center p-4 bg-purple-50 dark:bg-purple-900/20 rounded-lg hover:bg-purple-100 dark:hover:bg-purple-900/30 transition-colors"
              >
                <Icon icon="mdi:history" class="w-8 h-8 text-purple-600 dark:text-purple-400 mb-2" />
                <span class="text-sm font-medium text-gray-900 dark:text-white">History</span>
              </button>
              
              <button
                @click="$router.push('/wallet/statements')"
                class="flex flex-col items-center p-4 bg-orange-50 dark:bg-orange-900/20 rounded-lg hover:bg-orange-100 dark:hover:bg-orange-900/30 transition-colors"
              >
                <Icon icon="mdi:file-document-outline" class="w-8 h-8 text-orange-600 dark:text-orange-400 mb-2" />
                <span class="text-sm font-medium text-gray-900 dark:text-white">Statements</span>
              </button>
            </div>
          </div>
        </div>

        <!-- Wallet Stats -->
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">
            Overview
          </h2>
          <div class="space-y-4">
            <div class="flex justify-between items-center">
              <span class="text-sm text-gray-600 dark:text-gray-400">Total Balance</span>
              <span class="text-lg font-semibold text-gray-900 dark:text-white">
                {{ formatCurrency(totalBalance, 'USD') }}
              </span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-sm text-gray-600 dark:text-gray-400">This Month</span>
              <span class="text-sm font-medium text-green-600 dark:text-green-400">
                +{{ formatCurrency(monthlyChange, 'USD') }}
              </span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-sm text-gray-600 dark:text-gray-400">Active Wallets</span>
              <span class="text-sm font-medium text-gray-900 dark:text-white">
                {{ balances.length }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Recent Transactions -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow">
        <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
          <div class="flex items-center justify-between">
            <h2 class="text-lg font-semibold text-gray-900 dark:text-white">
              Recent Transactions
            </h2>
            <button
              @click="$router.push('/wallet/transactions')"
              class="text-sm text-primary-600 dark:text-primary-400 hover:text-primary-700 dark:hover:text-primary-300 font-medium"
            >
              View All
              <Icon icon="mdi:arrow-right" class="w-4 h-4 ml-1 inline" />
            </button>
          </div>
        </div>
        
        <div class="p-6">
          <div v-if="recentTransactions.length === 0" class="text-center py-8">
            <Icon icon="mdi:receipt-text-outline" class="w-12 h-12 text-gray-400 mx-auto mb-3" />
            <p class="text-gray-500 dark:text-gray-400">No transactions yet</p>
            <button
              @click="openTopupModal"
              class="mt-4 text-primary-600 dark:text-primary-400 hover:text-primary-700 dark:hover:text-primary-300 font-medium"
            >
              Make your first transaction
            </button>
          </div>
          
          <div v-else class="space-y-4">
            <TransactionItem
              v-for="transaction in recentTransactions.slice(0, 5)"
              :key="transaction.id"
              :transaction="transaction"
              compact
            />
          </div>
        </div>
      </div>
    </div>

    <!-- Modals -->
    <TopupModal
      v-if="showTopupModal"
      @close="showTopupModal = false"
      @success="handleTopupSuccess"
    />
    
    <TransferModal
      v-if="showTransferModal"
      @close="showTransferModal = false"
      @success="handleTransferSuccess"
    />
    
    <AddWalletModal
      v-if="showAddWalletModal"
      @close="showAddWalletModal = false"
      @success="handleAddWalletSuccess"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useWalletStore } from '@/stores/walletStore'
import { formatCurrency } from '@/utils/currency'
import BalanceCard from '@/components/wallet/BalanceCard.vue'
import TransactionItem from '@/components/wallet/TransactionItem.vue'
import TopupModal from '@/components/wallet/TopupModal.vue'
import TransferModal from '@/components/wallet/TransferModal.vue'
import AddWalletModal from '@/components/wallet/AddWalletModal.vue'

const walletStore = useWalletStore()

// State
const showTopupModal = ref(false)
const showTransferModal = ref(false)
const showAddWalletModal = ref(false)
const showNotifications = ref(false)

// Computed
const balances = computed(() => walletStore.balances)
const recentTransactions = computed(() => walletStore.recentTransactions)
const totalBalance = computed(() => walletStore.totalBalance)
const monthlyChange = computed(() => walletStore.monthlyChange)
const unreadCount = computed(() => walletStore.unreadCount)

// Methods
const openTopupModal = () => {
  showTopupModal.value = true
}

const openTransferModal = () => {
  showTransferModal.value = true
}

const handleTopupSuccess = (result) => {
  showTopupModal.value = false
  // Don't fetch balances - topup already updated local state
  console.log('Topup successful:', result)
}

const handleTransferSuccess = (result) => {
  showTransferModal.value = false
  // Don't fetch balances - transfer already updated local state
  console.log('Transfer successful:', result)
}

const handleAddWalletSuccess = (wallet) => {
  showAddWalletModal.value = false
  // Only fetch wallets if new wallet was added
  walletStore.fetchWallets()
}

// Lifecycle
onMounted(async () => {
  try {
    // Initialize with defensive checks
    if (!walletStore) {
      console.error('Wallet store not initialized')
      return
    }
    
    // Only fetch if not initialized
    if (!walletStore.isInitialized) {
      await walletStore.fetchWallets()
      await walletStore.fetchRecentTransactions()
    }
  } catch (error) {
    console.error('Failed to load wallet data:', error)
    // Optionally show user-friendly error message
    alert('Failed to load wallet data')
  }
})
</script>
