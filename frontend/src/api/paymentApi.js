// Payment API Service
// This service handles all payment related API calls
import axios from '@/plugins/axios'

class PaymentAPI {
  // Create deposit request
  async createDeposit(depositRequest) {
    try {
      const response = await axios.post('/simple-payment/deposit', depositRequest)
      return response
    } catch (error) {
      throw error
    }
  }

  // Check payment status
  async checkPaymentStatus(referenceCode) {
    try {
      const response = await axios.get(`/simple-payment/status/${referenceCode}`)
      return response
    } catch (error) {
      throw error
    }
  }

  // Get payment history
  async getPaymentHistory(params = {}) {
    try {
      // Add cache-busting timestamp
      const cacheParams = { ...params, _t: Date.now() }
      const response = await axios.get('/simple-payment/history', { params: cacheParams })
      return response
    } catch (error) {
      throw error
    }
  }

  // Get bank information
  async getBankInfo() {
    try {
      const response = await axios.get('/simple-payment/bank-info')
      return response
    } catch (error) {
      throw error
    }
  }

  // Update bank information (admin only)
  async updateBankInfo(bankInfo) {
    try {
      const response = await axios.put('/simple-payment/admin/bank-info', bankInfo)
      return response
    } catch (error) {
      throw error
    }
  }

  // Simulate payment (test endpoint)
  async simulatePayment(referenceCode, amount) {
    try {
      const response = await axios.post('/simple-payment/test/simulate-payment', {
        referenceCode,
        amount
      })
      return response
    } catch (error) {
      throw error
    }
  }

  // Manual complete payment (admin only)
  async manualCompletePayment(referenceCode, bankTransactionId) {
    try {
      const response = await axios.post(`/simple-payment/admin/complete/${referenceCode}`, {
        bankTransactionId
      })
      return response
    } catch (error) {
      throw error
    }
  }

  // Health check
  async healthCheck() {
    try {
      const response = await axios.get('/simple-payment/health')
      return response
    } catch (error) {
      throw error
    }
  }

  // Legacy methods for compatibility (mapped to new endpoints)
  async getPaymentByReferenceCode(referenceCode) {
    return this.checkPaymentStatus(referenceCode)
  }

  async getUserPayments(params = {}) {
    return this.getPaymentHistory(params)
  }

  async cancelPayment(referenceCode, reason) {
    try {
      const response = await axios.post(`/simple-payment/cancel/${referenceCode}`, { reason })
      return response
    } catch (error) {
      throw error
    }
  }

  async validateAmount(amount) {
    // Not implemented in backend yet
    throw new Error('Validate amount not implemented')
  }

  async getPaymentStats(params = {}) {
    // Not implemented in backend yet
    throw new Error('Payment stats not implemented')
  }

  async exportPaymentHistory(params = {}) {
    // Not implemented in backend yet
    throw new Error('Export payment history not implemented')
  }

  async refundPayment(referenceCode, reason) {
    try {
      const response = await axios.post(`/simple-payment/admin/refund/${referenceCode}`, { reason })
      return response
    } catch (error) {
      throw error
    }
  }

  // Retry payment
  async retryPayment(referenceCode) {
    try {
      const response = await axios.post(`/simple-payment/retry/${referenceCode}`)
      return response
    } catch (error) {
      throw error
    }
  }

  // Validate discount code
  async validateDiscount(code, amount, packageId) {
    try {
      const response = await axios.get('/v1/discounts/validate', {
        params: { code, amount, packageId }
      })
      return response
    } catch (error) {
      throw error
    }
  }

  // Get user invoices
  async getUserInvoices(userId) {
    try {
      const response = await axios.get(`/v1/invoices/user/${userId}`)
      return response
    } catch (error) {
      throw error
    }
  }

  // Get invoice by number
  async getInvoiceByNumber(invoiceNumber) {
    try {
      const response = await axios.get(`/v1/invoices/number/${invoiceNumber}`)
      return response
    } catch (error) {
      throw error
    }
  }

  // Get payment analytics (admin)
  async getRevenueSummary(startDate, endDate) {
    try {
      const response = await axios.get('/v1/analytics/payments/revenue-summary', {
        params: { startDate, endDate }
      })
      return response
    } catch (error) {
      throw error
    }
  }

  async getDailyRevenue(year, month) {
    try {
      const response = await axios.get('/v1/analytics/payments/daily-revenue', {
        params: { year, month }
      })
      return response
    } catch (error) {
      throw error
    }
  }

  async getPaymentTrends(days) {
    try {
      const response = await axios.get('/v1/analytics/payments/trends', {
        params: { days }
      })
      return response
    } catch (error) {
      throw error
    }
  }

  async getPackagePerformance(startDate, endDate) {
    try {
      const response = await axios.get('/v1/analytics/payments/package-performance', {
        params: { startDate, endDate }
      })
      return response
    } catch (error) {
      throw error
    }
  }

  async getTopUsers(limit, startDate, endDate) {
    try {
      const response = await axios.get('/v1/analytics/payments/top-users', {
        params: { limit, startDate, endDate }
      })
      return response
    } catch (error) {
      throw error
    }
  }

  async getDashboardAnalytics() {
    try {
      const response = await axios.get('/v1/analytics/payments/dashboard')
      return response
    } catch (error) {
      throw error
    }
  }

  // Discount management (admin)
  async getActiveDiscounts() {
    try {
      const response = await axios.get('/v1/discounts/active')
      return response
    } catch (error) {
      throw error
    }
  }

  async createDiscount(discount) {
    try {
      const response = await axios.post('/v1/discounts', discount)
      return response
    } catch (error) {
      throw error
    }
  }

  async updateDiscount(id, discount) {
    try {
      const response = await axios.put(`/v1/discounts/${id}`, discount)
      return response
    } catch (error) {
      throw error
    }
  }

  async deleteDiscount(id) {
    try {
      const response = await axios.delete(`/v1/discounts/${id}`)
      return response
    } catch (error) {
      throw error
    }
  }

  // Webhook management (admin)
  async getActiveWebhooks() {
    try {
      const response = await axios.get('/v1/webhooks')
      return response
    } catch (error) {
      throw error
    }
  }

  async createWebhook(webhook) {
    try {
      const response = await axios.post('/v1/webhooks', webhook)
      return response
    } catch (error) {
      throw error
    }
  }

  async updateWebhook(id, webhook) {
    try {
      const response = await axios.put(`/v1/webhooks/${id}`, webhook)
      return response
    } catch (error) {
      throw error
    }
  }

  async deleteWebhook(id) {
    try {
      const response = await axios.delete(`/v1/webhooks/${id}`)
      return response
    } catch (error) {
      throw error
    }
  }

  async testWebhook(id) {
    try {
      const response = await axios.post(`/v1/webhooks/${id}/test`)
      return response
    } catch (error) {
      throw error
    }
  }
}

// Create and export singleton instance
const paymentAPI = new PaymentAPI()
export default paymentAPI
