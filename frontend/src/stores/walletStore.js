import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { walletApi } from '@/api/walletApi'
import { ENV_DEBUG } from '@/utils/env.js'
import { ACTIVE_TENANT_ID } from '@/utils/constant'
import { useAuthStore } from '@/stores/authStore'

// Debug environment variables
console.log('Wallet Store - ENV_DEBUG:', ENV_DEBUG)
console.log('Wallet Store - ACTIVE_TENANT_ID:', localStorage.getItem(ACTIVE_TENANT_ID))

export const useWalletStore = defineStore('wallet', () => {
  // State
  const wallets = ref([])
  const balances = ref([])
  const transactions = ref([])
  const recentTransactions = ref([])
  const loading = ref(false)
  const error = ref(null)
  const unreadCount = ref(0)
  
  // Add loading flags to prevent duplicate calls
  const isFetchingWallets = ref(false)
  const isFetchingBalances = ref(false)
  const isFetchingTransactions = ref(false)
  
  // Global initialization flag
  const isInitialized = ref(false)

  // Get auth store
  const authStore = useAuthStore()

  // Getters
  const totalBalance = computed(() => {
    if (!balances.value || balances.value.length === 0) {
      return 0
    }
    return balances.value.reduce((total, balance) => {
      // Convert to USD for total (simplified - in real app would use exchange rates)
      const usdAmount = balance.currency === 'USD' 
        ? balance.amount 
        : balance.amount * 0.000041 // Rough VND to USD conversion
      return total + usdAmount
    }, 0)
  })

  const getWalletByCurrency = computed(() => (currency) => {
    if (!wallets.value || wallets.value.length === 0) {
      return null
    }
    return wallets.value.find(w => w.currency === currency)
  })

  const monthlyChange = computed(() => {
    // Calculate monthly change from transactions
    if (!transactions.value || transactions.value.length === 0) {
      return 0
    }
    
    const currentMonth = new Date().getMonth()
    const currentYear = new Date().getFullYear()
    
    return transactions.value
      .filter(t => {
        const date = new Date(t.createdAt)
        return date.getMonth() === currentMonth && date.getFullYear() === currentYear
      })
      .reduce((total, t) => {
        return t.transactionType === 'TOPUP' ? total + t.amount : total - t.amount
      }, 0)
  })

  // Actions
  const fetchWallets = async () => {
    // Prevent duplicate calls
    if (isFetchingWallets.value) {
      console.log('Already fetching wallets, skipping...')
      return
    }
    
    // Skip if already loaded and initialized
    if (isInitialized.value && wallets.value.length > 0) {
      console.log('Wallets already initialized, skipping fetch')
      return
    }
    
    const tenantKey = localStorage.getItem(ACTIVE_TENANT_ID)
    const userId = authStore.user?.id
    
    if (!tenantKey) {
      error.value = 'No tenant selected'
      return
    }
    
    if (!userId) {
      error.value = 'User not authenticated'
      return
    }

    isFetchingWallets.value = true
    loading.value = true
    error.value = null
    try {
      console.log('Fetching wallets for user:', userId, 'tenantKey:', tenantKey)
      const response = await walletApi.getUserWallets(userId, tenantKey)
      wallets.value = response.data || []
      await fetchBalances()
      isInitialized.value = true
    } catch (err) {
      error.value = err.message || 'Failed to fetch wallets'
      throw err
    } finally {
      isFetchingWallets.value = false
      loading.value = false
    }
  }

  const fetchBalances = async () => {
    // Prevent duplicate calls
    if (isFetchingBalances.value) {
      console.log('Already fetching balances, skipping...')
      return
    }
    
    const tenantKey = localStorage.getItem(ACTIVE_TENANT_ID)
    const userId = authStore.user?.id
    
    if (!tenantKey || !userId) {
      error.value = 'Missing tenant or user context'
      return
    }
    
    isFetchingBalances.value = true
    try {
      console.log('Fetching balances for user:', userId, 'tenantKey:', tenantKey)
      const response = await walletApi.getAllBalances(userId, tenantKey)
      balances.value = response.data || []
    } catch (err) {
      error.value = err.message || 'Failed to fetch balances'
      throw err
    } finally {
      isFetchingBalances.value = false
    }
  }

  const createWallet = async (walletData) => {
    const tenantKey = localStorage.getItem(ACTIVE_TENANT_ID)
    if (!tenantKey) {
      error.value = 'No tenant selected'
      return
    }

    loading.value = true
    try {
      const walletDataWithTenant = { ...walletData, tenantKey }
      const response = await walletApi.createWallet(walletDataWithTenant)
      wallets.value.push(response.data)
      await fetchBalances()
      return response.data
    } catch (err) {
      error.value = err.message || 'Failed to create wallet'
      throw err
    } finally {
      loading.value = false
    }
  }

  const fetchTransactions = async (walletId, page = 0, size = 20) => {
    loading.value = true
    try {
      const response = await walletApi.getTransactions(walletId, page, size)
      transactions.value = (response.data?.content || response.data || [])
      return response.data || {}
    } catch (err) {
      error.value = err.message || 'Failed to fetch transactions'
      throw err
    } finally {
      loading.value = false
    }
  }

  const fetchRecentTransactions = async () => {
    // Prevent duplicate calls
    if (isFetchingTransactions.value) {
      console.log('Already fetching transactions, skipping...')
      return
    }
    
    try {
      isFetchingTransactions.value = true
      // Get recent transactions from all wallets
      if (!wallets.value || wallets.value.length === 0) {
        console.log('No wallets available, skipping transaction fetch')
        return
      }
      
      const transactionPromises = wallets.value.map(wallet => 
        walletApi.getTransactions(wallet.id, 0, 5)
      )
      const responses = await Promise.all(transactionPromises)
      
      // Combine and sort by date
      const allTransactions = responses.flatMap(response => 
        (response.data?.content || response.data || [])
      )
      
      recentTransactions.value = allTransactions
        .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
        .slice(0, 10) // Keep only top 10
    } catch (err) {
      error.value = err.message || 'Failed to fetch recent transactions'
      throw err
    } finally {
      isFetchingTransactions.value = false
    }
  }

  const topup = async (walletId, amount, paymentMethod) => {
    loading.value = true
    try {
      const response = await walletApi.topup({
        walletId,
        amount,
        paymentMethod,
        description: `Top up ${amount} ${getWalletCurrency(walletId)}`
      })
      
      // Update balance directly instead of re-fetching
      const wallet = getWalletById(walletId)
      if (wallet) {
        const balanceIndex = balances.value.findIndex(b => b.walletId === walletId)
        if (balanceIndex !== -1) {
          balances.value[balanceIndex] = {
            ...balances.value[balanceIndex],
            amount: (balances.value[balanceIndex].amount || 0) + amount
          }
        }
      }
      
      // Add transaction directly instead of re-fetching
      if (response.data) {
        recentTransactions.value.unshift({
          ...response.data,
          transactionType: 'TOPUP',
          amount: amount,
          currency: getWalletCurrency(walletId),
          createdAt: new Date().toISOString()
        })
        
        // Keep only top 10
        if (recentTransactions.value.length > 10) {
          recentTransactions.value = recentTransactions.value.slice(0, 10)
        }
      }
      
      return response.data
    } catch (err) {
      error.value = err.message || 'Failed to top up wallet'
      throw err
    } finally {
      loading.value = false
    }
  }

  const transfer = async (fromWalletId, toWalletId, amount, description) => {
    loading.value = true
    try {
      const response = await walletApi.transfer(fromWalletId, {
        toWalletId,
        amount,
        currency: getWalletCurrency(fromWalletId),
        description: description || `Transfer ${amount} ${getWalletCurrency(fromWalletId)}`
      })
      
      // Update balances directly instead of re-fetching
      const fromWallet = getWalletById(fromWalletId)
      const toWallet = getWalletById(toWalletId)
      
      if (fromWallet) {
        const fromBalanceIndex = balances.value.findIndex(b => b.walletId === fromWalletId)
        if (fromBalanceIndex !== -1) {
          balances.value[fromBalanceIndex] = {
            ...balances.value[fromBalanceIndex],
            amount: (balances.value[fromBalanceIndex].amount || 0) - amount
          }
        }
      }
      
      if (toWallet) {
        const toBalanceIndex = balances.value.findIndex(b => b.walletId === toWalletId)
        if (toBalanceIndex !== -1) {
          balances.value[toBalanceIndex] = {
            ...balances.value[toBalanceIndex],
            amount: (balances.value[toBalanceIndex].amount || 0) + amount
          }
        }
      }
      
      // Add transactions directly instead of re-fetching
      if (response.data) {
        const now = new Date().toISOString()
        
        // Add outgoing transaction
        recentTransactions.value.unshift({
          ...response.data.outgoingTransaction,
          transactionType: 'TRANSFER_OUT',
          amount: amount,
          currency: getWalletCurrency(fromWalletId),
          createdAt: now
        })
        
        // Add incoming transaction
        recentTransactions.value.unshift({
          ...response.data.incomingTransaction,
          transactionType: 'TRANSFER_IN',
          amount: amount,
          currency: getWalletCurrency(toWalletId),
          createdAt: now
        })
        
        // Keep only top 10
        if (recentTransactions.value.length > 10) {
          recentTransactions.value = recentTransactions.value.slice(0, 10)
        }
      }
      
      return response.data
    } catch (err) {
      error.value = err.message || 'Failed to transfer funds'
      throw err
    } finally {
      loading.value = false
    }
  }

  const getWalletById = (walletId) => {
    if (!wallets.value || wallets.value.length === 0) {
      return null
    }
    return wallets.value.find(w => w.id === walletId)
  }

  const getWalletCurrency = (walletId) => {
    const wallet = getWalletById(walletId)
    return wallet?.currency || 'USD'
  }

  const clearError = () => {
    error.value = null
  }

  // WebSocket integration for real-time updates
  const handleBalanceUpdate = (data) => {
    if (!balances.value || balances.value.length === 0) {
      return
    }
    const balanceIndex = balances.value.findIndex(b => b.walletId === data.walletId)
    if (balanceIndex !== -1) {
      balances.value[balanceIndex] = { ...balances.value[balanceIndex], ...data }
    }
  }

  const handleTransactionUpdate = (data) => {
    if (!recentTransactions.value) {
      recentTransactions.value = []
    }
    // Add new transaction to recent transactions
    recentTransactions.value.unshift(data)
    
    // Keep only top 10
    if (recentTransactions.value.length > 10) {
      recentTransactions.value = recentTransactions.value.slice(0, 10)
    }
  }

  return {
    // State
    wallets,
    balances,
    transactions,
    recentTransactions,
    loading,
    error,
    unreadCount,
    isInitialized,
    
    // Getters
    totalBalance,
    getWalletByCurrency,
    monthlyChange,
    
    // Actions
    fetchWallets,
    fetchBalances,
    createWallet,
    fetchTransactions,
    fetchRecentTransactions,
    topup,
    transfer,
    getWalletById,
    getWalletCurrency,
    clearError,
    handleBalanceUpdate,
    handleTransactionUpdate
  }
})
