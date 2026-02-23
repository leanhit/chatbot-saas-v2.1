import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from '@/plugins/axios'

export const useWalletStore = defineStore('wallet', () => {
  // State
  const wallets = ref([])
  const transactions = ref([])
  const currentWallet = ref(null)
  const isLoading = ref(false)
  const error = ref(null)

  // Getters
  const totalBalance = computed(() => {
    if (!currentWallet.value) return 0
    return currentWallet.value.balance || 0
  })

  const availableBalance = computed(() => {
    if (!currentWallet.value) return 0
    return currentWallet.value.availableBalance || currentWallet.value.balance || 0
  })

  const frozenBalance = computed(() => {
    if (!currentWallet.value) return 0
    return (currentWallet.value.frozenBalance || 0)
  })

  const recentTransactions = computed(() => 
    transactions.value.slice(0, 10)
  )

  const transactionsByType = computed(() => {
    const grouped = {}
    transactions.value.forEach(tx => {
      if (!grouped[tx.transactionType]) {
        grouped[tx.transactionType] = []
      }
      grouped[tx.transactionType].push(tx)
    })
    return grouped
  })

  const monthlySpending = computed(() => {
    const currentMonth = new Date().getMonth()
    const currentYear = new Date().getFullYear()
    
    return transactions.value
      .filter(tx => {
        const txDate = new Date(tx.createdAt)
        return tx.transactionType === 'PURCHASE' && 
               txDate.getMonth() === currentMonth && 
               txDate.getFullYear() === currentYear
      })
      .reduce((total, tx) => total + (tx.amount || 0), 0)
  })

  const monthlyTopup = computed(() => {
    const currentMonth = new Date().getMonth()
    const currentYear = new Date().getFullYear()
    
    return transactions.value
      .filter(tx => {
        const txDate = new Date(tx.createdAt)
        return tx.transactionType === 'TOPUP' && 
               txDate.getMonth() === currentMonth && 
               txDate.getFullYear() === currentYear
      })
      .reduce((total, tx) => total + (tx.amount || 0), 0)
  })

  // Actions
  const fetchWallets = async (tenantId) => {
    isLoading.value = true
    error.value = null
    
    try {
      const response = await axios.get(`/wallet/wallets?tenantId=${tenantId}`)
      wallets.value = response.data || []
      
      // Set first wallet as current if none selected
      if (wallets.value.length > 0 && !currentWallet.value) {
        currentWallet.value = wallets.value[0]
      }
      
    } catch (err) {
      error.value = err.response?.data?.message || err.message
      console.error('Failed to fetch wallets:', err)
    } finally {
      isLoading.value = false
    }
  }

  const fetchTransactions = async (walletId, limit = 50) => {
    isLoading.value = true
    error.value = null
    
    try {
      const response = await axios.get(`/wallet/transactions?walletId=${walletId}&limit=${limit}`)
      transactions.value = response.data || []
      
    } catch (err) {
      error.value = err.response?.data?.message || err.message
      console.error('Failed to fetch transactions:', err)
    } finally {
      isLoading.value = false
    }
  }

  const createWallet = async (tenantId, walletData) => {
    isLoading.value = true
    error.value = null
    
    try {
      const response = await axios.post(`/wallet/wallets`, {
        tenantId,
        ...walletData
      })
      
      wallets.value.push(response.data)
      
      // Set as current if first wallet
      if (wallets.value.length === 1) {
        currentWallet.value = response.data
      }
      
      return response.data
    } catch (err) {
      error.value = err.response?.data?.message || err.message
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const topup = async (walletId, amount, paymentMethod) => {
    isLoading.value = true
    error.value = null
    
    try {
      const response = await axios.post(`/wallet/wallets/${walletId}/topup`, {
        amount,
        paymentMethod
      })
      
      // Update wallet balance
      if (currentWallet.value && currentWallet.value.id === walletId) {
        currentWallet.value.balance = response.data.newBalance
      }
      
      // Add transaction to history
      transactions.value.unshift(response.data.transaction)
      
      return response.data
    } catch (err) {
      error.value = err.response?.data?.message || err.message
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const purchase = async (walletId, amount, description, metadata = {}) => {
    isLoading.value = true
    error.value = null
    
    try {
      const response = await axios.post(`/wallet/wallets/${walletId}/purchase`, {
        amount,
        description,
        metadata
      })
      
      // Update wallet balance
      if (currentWallet.value && currentWallet.value.id === walletId) {
        currentWallet.value.balance = response.data.newBalance
      }
      
      // Add transaction to history
      transactions.value.unshift(response.data.transaction)
      
      return response.data
    } catch (err) {
      error.value = err.response?.data?.message || err.message
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const transfer = async (fromWalletId, toWalletId, amount, description) => {
    isLoading.value = true
    error.value = null
    
    try {
      const response = await axios.post(`/wallet/wallets/${fromWalletId}/transfer`, {
        toWalletId,
        amount,
        description
      })
      
      // Update wallet balances
      if (currentWallet.value && currentWallet.value.id === fromWalletId) {
        currentWallet.value.balance = response.data.fromBalance
      }
      
      // Add transaction to history
      transactions.value.unshift(response.data.transaction)
      
      return response.data
    } catch (err) {
      error.value = err.response?.data?.message || err.message
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const getWalletSummary = async (walletId) => {
    try {
      const response = await axios.get(`/wallet/wallets/${walletId}/summary`)
      return response.data
    } catch (err) {
      error.value = err.response?.data?.message || err.message
      throw err
    }
  }

  const getTransactionHistory = async (walletId, filters = {}) => {
    try {
      const params = new URLSearchParams(filters)
      const response = await axios.get(`/wallet/wallets/${walletId}/transactions?${params}`)
      return response.data
    } catch (err) {
      error.value = err.response?.data?.message || err.message
      throw err
    }
  }

  const setCurrentWallet = (wallet) => {
    currentWallet.value = wallet
  }

  // Reset state
  const resetWalletState = () => {
    wallets.value = []
    transactions.value = []
    currentWallet.value = null
    error.value = null
  }

  return {
    // State
    wallets,
    transactions,
    currentWallet,
    isLoading,
    error,
    
    // Getters
    totalBalance,
    availableBalance,
    frozenBalance,
    recentTransactions,
    transactionsByType,
    monthlySpending,
    monthlyTopup,
    
    // Actions
    fetchWallets,
    fetchTransactions,
    createWallet,
    topup,
    purchase,
    transfer,
    getWalletSummary,
    getTransactionHistory,
    setCurrentWallet,
    resetWalletState
  }
})
