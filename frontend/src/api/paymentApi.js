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

  async cancelPayment(referenceCode) {
    // Not implemented in backend yet
    throw new Error('Cancel payment not implemented')
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
    // Not implemented in backend yet
    throw new Error('Refund payment not implemented')
  }
}

// Create and export singleton instance
const paymentAPI = new PaymentAPI()
export default paymentAPI
