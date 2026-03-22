import axios from '@/plugins/axios'

// Use process.env for Vue CLI compatibility
const API_BASE = process.env.VITE_API_URL || 'http://localhost:8080/api'

export const walletApi = {
  /**
   * Get all wallets for a user in a tenant
   */
  async getUserWallets(userId, tenantKey) {
    const response = await axios.get(`${API_BASE}/wallets/user/${userId}/tenant/${tenantKey}`)
    return response.data
  },

  /**
   * Get wallet balance
   */
  async getBalance(walletId) {
    const response = await axios.get(`${API_BASE}/wallet/${walletId}/balance`)
    return response.data
  },

  /**
   * Create new wallet
   */
  async createWallet(walletData) {
    const response = await axios.post(`${API_BASE}/wallet/create`, walletData)
    return response.data
  },

  /**
   * Top up wallet
   */
  async topup(topupData) {
    const response = await axios.post(`${API_BASE}/transactions/topup`, topupData)
    return response.data
  },

  /**
   * Transfer funds between wallets
   * TODO: Backend transfer endpoint not implemented yet
   */
  async transfer(walletId, transferData) {
    // Temporary: use old endpoint until backend implements transfer
    const response = await axios.post(`${API_BASE}/wallet/${walletId}/transfer`, transferData)
    return response.data
  },

  /**
   * Get transaction history
   */
  async getTransactions(walletId, page = 0, size = 20) {
    const response = await axios.get(`${API_BASE}/transactions/wallet/${walletId}/paged`, {
      params: { page, size }
    })
    return response.data
  },

  /**
   * Get transaction by reference
   */
  async getTransactionByReference(reference) {
    const response = await axios.get(`${API_BASE}/transactions/reference/${reference}`)
    return response.data
  },

  /**
   * Get all balances for a user
   */
  async getAllBalances(userId, tenantKey) {
    const response = await axios.get(`${API_BASE}/wallets/user/${userId}/tenant/${tenantKey}/balances`)
    return response.data
  },

  /**
   * Suspend wallet
   */
  async suspendWallet(walletId, reason) {
    const response = await axios.post(`${API_BASE}/wallet/${walletId}/suspend`, { reason })
    return response.data
  },

  /**
   * Activate wallet
   */
  async activateWallet(walletId) {
    const response = await axios.post(`${API_BASE}/wallet/${walletId}/activate`)
    return response.data
  },

  /**
   * Get wallet statement
   */
  async getStatement(walletId, startDate, endDate) {
    const response = await axios.get(`${API_BASE}/wallet/${walletId}/statement`, {
      params: { startDate, endDate }
    })
    return response.data
  },

  /**
   * Download wallet statement as PDF
   */
  async downloadStatement(walletId, startDate, endDate) {
    const response = await axios.get(`${API_BASE}/wallet/${walletId}/statement/download`, {
      params: { startDate, endDate },
      responseType: 'blob'
    })
    return response.data
  }
}
