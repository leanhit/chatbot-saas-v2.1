import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'

/**
 * Wallet Store - Manages wallet summary information
 * Source: Wallet Service API (READ-ONLY)
 * Responsibility: Fetch and cache wallet data for UI display only
 */
export const useWalletStore = defineStore('wallet', () => {
  const wallet = ref(null)
  const loading = ref(false)
  const error = ref(null)

  // Computed properties
  const balance = computed(() => {
    return wallet.value?.balance || 0
  })

  const hasWallet = computed(() => {
    return wallet.value !== null
  })

  const hasSufficientBalance = computed(() => {
    return (amount) => {
      return balance.value >= amount
    }
  })

  const walletStatus = computed(() => {
    if (!hasWallet.value) return 'NO_WALLET'
    if (balance.value <= 0) return 'EMPTY'
    if (balance.value < 10) return 'LOW'
    return 'OK'
  })

  // Actions
  async function fetchWallet() {
    loading.value = true
    error.value = null
    
    try {
      // Backend automatically uses current tenant context
      const response = await api.get('/api/wallets/current')
      wallet.value = response.data
    } catch (err) {
      // Wallet might not exist, treat as normal
      if (err.response?.status === 404) {
        wallet.value = null
      } else {
        error.value = err.message
        console.error('Failed to fetch wallet:', err)
      }
    } finally {
      loading.value = false
    }
  }

  function getWalletStatusMessage() {
    switch (walletStatus.value) {
      case 'NO_WALLET':
        return 'Wallet not available'
      case 'EMPTY':
        return 'Wallet empty'
      case 'LOW':
        return 'Low balance'
      case 'OK':
        return 'Wallet OK'
      default:
        return 'Unknown wallet status'
    }
  }

  return {
    // State
    wallet,
    loading,
    error,
    
    // Computed
    balance,
    hasWallet,
    hasSufficientBalance,
    walletStatus,
    
    // Actions
    fetchWallet,
    getWalletStatusMessage
  }
})
