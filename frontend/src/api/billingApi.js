import axios from '@/plugins/axios'

// Use process.env for Vue CLI compatibility
const API_BASE = process.env.VITE_API_URL || 'http://localhost:8080/api'

export const billingApi = {
  /**
   * Get billing account for tenant
   */
  async getBillingAccount(tenantKey) {
    const response = await axios.get(`${API_BASE}/billing/account/${tenantKey}`)
    return response.data
  },

  /**
   * Create billing account
   */
  async createBillingAccount(tenantKey, accountData) {
    const response = await axios.post(`${API_BASE}/billing/account/${tenantKey}`, accountData)
    return response.data
  },

  /**
   * Update billing account
   */
  async updateBillingAccount(accountId, accountData) {
    const response = await axios.put(`${API_BASE}/billing/account/${accountId}`, accountData)
    return response.data
  },

  /**
   * Get subscription for tenant
   */
  async getSubscription(tenantKey) {
    const response = await axios.get(`${API_BASE}/billing/subscription/${tenantKey}`)
    return response.data
  },

  /**
   * Create subscription
   */
  async createSubscription(tenantKey, subscriptionData) {
    const response = await axios.post(`${API_BASE}/billing/subscription/${tenantKey}`, subscriptionData)
    return response.data
  },

  /**
   * Update subscription
   */
  async updateSubscription(subscriptionId, subscriptionData) {
    const response = await axios.put(`${API_BASE}/billing/subscription/${subscriptionId}`, subscriptionData)
    return response.data
  },

  /**
   * Cancel subscription
   */
  async cancelSubscription(reason) {
    const response = await axios.post(`${API_BASE}/billing/subscription/cancel`, { reason })
    return response.data
  },

  /**
   * Upgrade subscription
   */
  async upgradeSubscription(planId) {
    const response = await axios.post(`${API_BASE}/billing/subscription/upgrade`, { planId })
    return response.data
  },

  /**
   * Get entitlements for tenant
   */
  async getEntitlements(tenantKey) {
    const response = await axios.get(`${API_BASE}/billing/entitlements/${tenantKey}`)
    return response.data
  },

  /**
   * Check feature access
   */
  async checkFeatureAccess(feature) {
    const response = await axios.post(`${API_BASE}/billing/entitlements/check`, { feature })
    return response.data
  },

  /**
   * Get usage data
   */
  async getUsage(tenantKey) {
    const response = await axios.get(`${API_BASE}/billing/usage/${tenantKey}`)
    return response.data
  },

  /**
   * Consume usage
   */
  async consumeUsage(tenantKey, feature, amount) {
    const response = await axios.post(`${API_BASE}/billing/usage/consume`, { tenantKey, feature, amount })
    return response.data
  },

  /**
   * Get payment methods
   */
  async getPaymentMethods(tenantKey) {
    const response = await axios.get(`${API_BASE}/billing/payment-methods/${tenantKey}`)
    return response.data
  },

  /**
   * Add payment method
   */
  async addPaymentMethod(paymentData) {
    const response = await axios.post(`${API_BASE}/billing/payment-methods`, paymentData)
    return response.data
  },

  /**
   * Remove payment method
   */
  async removePaymentMethod(methodId) {
    const response = await axios.delete(`${API_BASE}/billing/payment-methods/${methodId}`)
    return response.data
  },

  /**
   * Set default payment method
   */
  async setDefaultPaymentMethod(methodId) {
    const response = await axios.post(`${API_BASE}/billing/payment-methods/${methodId}/default`)
    return response.data
  },

  /**
   * Get invoices
   */
  async getInvoices(tenantKey, page = 0, size = 20) {
    const response = await axios.get(`${API_BASE}/billing/invoices/${tenantKey}`, {
      params: { page, size }
    })
    return response.data
  },

  /**
   * Get recent invoices
   */
  async getRecentInvoices(tenantKey) {
    const response = await axios.get(`${API_BASE}/billing/invoices/${tenantKey}/recent`)
    return response.data
  },

  /**
   * Get invoice by ID
   */
  async getInvoice(invoiceId) {
    const response = await axios.get(`${API_BASE}/billing/invoices/${invoiceId}`)
    return response.data
  },

  /**
   * Download invoice PDF
   */
  async downloadInvoice(invoiceId) {
    const response = await axios.get(`${API_BASE}/billing/invoices/${invoiceId}/download`, {
      responseType: 'blob'
    })
    return response.data
  },

  /**
   * Get available plans
   */
  async getAvailablePlans() {
    const response = await axios.get(`${API_BASE}/billing/plans`)
    return response.data
  },

  /**
   * Toggle auto renewal
   */
  async toggleAutoRenewal(enabled) {
    const response = await axios.post(`${API_BASE}/billing/subscription/auto-renewal`, { enabled })
    return response.data
  },

  /**
   * Get billing statistics
   */
  async getBillingStats(tenantKey) {
    const response = await axios.get(`${API_BASE}/billing/stats/${tenantKey}`)
    return response.data
  },

  // Currency APIs
  async getCurrencySettings(tenantKey) {
    const response = await axios.get(`${API_BASE}/billing/currency/settings?tenantKey=${tenantKey}`)
    return response.data
  },

  async updateCurrencySettings(tenantKey, settings) {
    const response = await axios.put(`${API_BASE}/billing/currency/settings?tenantKey=${tenantKey}`, settings)
    return response.data
  },

  async convertCurrency(tenantKey, amount, from, to) {
    const response = await axios.post(`${API_BASE}/billing/currency/convert`, null, {
      params: { tenantKey, amount, from, to }
    })
    return response.data
  },

  async getExchangeRate(tenantKey, from, to) {
    const response = await axios.get(`${API_BASE}/billing/currency/rate`, {
      params: { tenantKey, from, to }
    })
    return response.data
  },

  async getSupportedCurrencies() {
    const response = await axios.get(`${API_BASE}/billing/currency/supported`)
    return response.data
  },

  async getAllExchangeRates() {
    const response = await axios.get(`${API_BASE}/billing/currency/rates`)
    return response.data
  }
}
