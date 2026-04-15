import { defineStore } from 'pinia'
import { formatCurrency, formatDateTime } from '@/utils/dateUtils'
import packageApi from '@/api/packageApi'
import paymentAPI from '@/api/paymentApi'
import tenantApi from '@/api/tenantApi'

/**
 * Payment Store - Quản lý state cho các giao dịch thanh toán
 * Cung cấp state management, caching, và computed properties cho payment pages
 */
export const usePaymentStore = defineStore('payment', {
  state: () => ({
    // Payment data
    currentPayment: null,
    paymentHistory: [],
    bankInfo: null,
    
    // Package data
    packages: [],
    packagesLoading: false,
    currentPackage: null,
    
    // UI state
    loading: false,
    checkingStatus: false,
    
    // Filters
    filterStatus: '',
    
    // Selected package
    selectedPackage: null,
    customAmount: '',
    customDescription: 'Nạp tiền vào tài khoản',
    
    // Messages
    message: '',
    messageType: 'success', // 'success' | 'error'
  }),

  getters: {
    /**
     * Lọc lịch sử theo trạng thái
     */
    filteredPayments: (state) => {
      if (!state.filterStatus) return state.paymentHistory
      return state.paymentHistory.filter(payment => 
        payment.status === state.filterStatus
      )
    },

    /**
     * Thống kê các giao dịch
     */
    paymentStats: (state) => {
      const payments = state.paymentHistory
      return {
        totalAmount: payments
          .filter(p => p.status === 'COMPLETED')
          .reduce((sum, p) => sum + (p.amount || 0), 0),
        completedCount: payments.filter(p => p.status === 'COMPLETED').length,
        pendingCount: payments.filter(p => p.status === 'PENDING').length,
        expiredCount: payments.filter(p => p.status === 'EXPIRED').length
      }
    },

    /**
     * Format số tiền
     */
    formattedAmount: (state) => {
      return state.currentPayment ? 
        formatCurrency(state.currentPayment.amount, 'VND') : 
        '0 ₫'
    },

    /**
     * Format ngày giờ hiển thị
     */
    formattedCreatedAt: (state) => {
      return state.currentPayment ? 
        formatDateTime(state.currentPayment.createdAtFormatted, { fallback: 'N/A' }) : 
        null
    },

    /**
     * Format ngày hết hạn
     */
    formattedExpiresAt: (state) => {
      return state.currentPayment ? 
        formatDateTime(state.currentPayment.expiresAtFormatted, { fallback: 'N/A' }) : 
        null
    },

    /**
     * Format ngày hoàn thành
     */
    formattedCompletedAt: (state) => {
      return state.currentPayment ? 
        formatDateTime(state.currentPayment.completedAtFormatted, { fallback: '-' }) : 
        null
    },

    /**
     * Check if user can create payment
     */
    canCreatePayment: (state) => {
      return state.selectedPackage && !state.loading
    },

    /**
     * Check if user can check status
     */
    canCheckStatus: (state) => {
      return state.currentPayment && !state.checkingStatus
    },

    /**
     * Check if has pending payment
     */
    hasPendingPayment: (state) => {
      return state.currentPayment && state.currentPayment.status === 'PENDING'
    }
  },

  actions: {
    /**
     * Load packages from API
     */
    async loadPackages() {
      try {
        this.packagesLoading = true
        const response = await packageApi.getActivePackages()
        this.packages = response.data || []
        console.log('✅ Loaded packages from API:', this.packages)
      } catch (error) {
        console.error('❌ Error loading packages:', error)
        this.setMessage('Error loading packages: ' + (error.message || 'Unknown error'), 'error')
        // Fallback to empty packages
        this.packages = []
      } finally {
        this.packagesLoading = false
      }
    },

    /**
     * Reset state về giá trị mặc định
     */
    resetState() {
      this.currentPayment = null
      this.paymentHistory = []
      this.bankInfo = null
      this.packages = []
      this.currentPackage = null
      this.loading = false
      this.checkingStatus = false
      this.filterStatus = ''
      this.selectedPackage = null
      this.customAmount = ''
      this.customDescription = 'Nạp tiền vào tài khoản'
      this.message = ''
      this.messageType = 'success'
    },

    /**
     * Thiết lập thông tin ngân hàng
     */
    setBankInfo(bankInfo) {
      this.bankInfo = bankInfo
    },

    /**
     * Thiết lập gói hiện tại của user (từ backend)
     */
    setCurrentPackage(packageId) {
      console.log('Setting current package:', packageId)
      const foundPackage = this.packages.find(pkg => pkg.packageId === packageId)
      if (foundPackage) {
        this.currentPackage = {
          id: foundPackage.packageId,
          name: foundPackage.name,
          price: foundPackage.price,
          duration: foundPackage.duration,
          features: this.getPackageFeatures(foundPackage)
        }
        console.log('✅ Current package set:', this.currentPackage)
      } else {
        console.warn('⚠️ Package not found:', packageId)
        this.currentPackage = null
      }
    },

    /**
     * Get package features as array with i18n support
     */
    getPackageFeatures(packageData) {
      const features = []
      
      // Handle both frontend package format and backend package format
      const pkg = packageData.packageId ? packageData : packageData
      
      // Note: This method in store doesn't have access to $t, 
      // so we'll keep the Vietnamese text here and let the component handle i18n
      // Message limit
      if (pkg.messageLimit && pkg.messageLimit > 0) {
        if (pkg.messageLimit >= 2147483647) {
          features.push('Unlimited tin nhắn')
        } else {
          features.push(`${pkg.messageLimit.toLocaleString()} tin nhắn/tháng`)
        }
      }
      
      // Chatbot limit
      if (pkg.chatbotLimit && pkg.chatbotLimit > 0) {
        if (pkg.chatbotLimit >= 2147483647) {
          features.push('Unlimited chatbots')
        } else {
          features.push(`${pkg.chatbotLimit} chatbots`)
        }
      }
      
      // Connection limit
      if (pkg.connectionLimit && pkg.connectionLimit > 0) {
        if (pkg.connectionLimit >= 2147483647) {
          features.push('Unlimited connections')
        } else {
          features.push(`${pkg.connectionLimit} connections`)
        }
      }
      
      // User tenant creation limit (per user)
      features.push('Tối đa 4 tenant/user')
      
      // Support features
      if (pkg.hasPrioritySupport) {
        features.push('Support ưu tiên')
      }
      
      if (pkg.hasDedicatedSupport) {
        features.push('Support 24/7')
      }
      
      if (pkg.hasAnalytics) {
        features.push('Analytics cơ bản')
      }
      
      if (pkg.hasAdvancedAnalytics) {
        features.push('Analytics nâng cao')
      }
      
      if (pkg.hasCustomIntegrations) {
        features.push('Custom integrations')
      }
      
      if (pkg.hasCustomFeatures) {
        features.push('Custom features')
      }
      
      if (pkg.hasSlaGuarantee) {
        features.push('SLA guarantee')
      }
      
      // Add basic support for free packages
      if (pkg.price === 0 || pkg.packageId === 'free') {
        features.push('Support cơ bản')
      }
      
      return features
    },

    /**
     * Chọn gói dịch vụ để thanh toán
     */
    selectPackage(packageId) {
      const selectedPkg = this.packages.find(pkg => pkg.packageId === packageId)
      if (selectedPkg) {
        this.selectedPackage = {
          id: selectedPkg.packageId,
          name: selectedPkg.name,
          price: selectedPkg.price,
          duration: selectedPkg.duration,
          features: this.getPackageFeatures(selectedPkg),
          badge: selectedPkg.badge
        }
        console.log('✅ Package selected:', this.selectedPackage)
      } else {
        console.error('❌ Package not found:', packageId)
      }
    },

    /**
     * Tạo yêu cầu nạp tiền
     */
    async createDeposit() {
      if (!this.selectedPackage) {
        this.setMessage('Please select a service package', 'error')
        return
      }

      this.loading = true
      this.message = ''

      try {
        const depositRequest = {
          amount: this.selectedPackage.price,
          currency: 'VND',
          description: `Thanh toán gói ${this.selectedPackage.name}`,
          targetPackageId: this.selectedPackage.id // Add targetPackageId
        }

        const response = await paymentAPI.createDeposit(depositRequest)
        this.currentPayment = response.data
        
        this.setMessage('Payment request created successfully!', 'success')
        console.log('✅ Deposit created:', this.currentPayment)
        
        return this.currentPayment
      } catch (error) {
        console.error('❌ Error creating deposit:', error)
        this.setMessage('Lỗi tạo yêu cầu nạp tiền: ' + (error.response?.data?.message || error.message || 'Unknown error'), 'error')
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * Kiểm tra trạng thái thanh toán
     */
    async checkPaymentStatus() {
      if (!this.currentPayment?.referenceCode) {
        this.setMessage('Không có mã thanh toán để kiểm tra', 'error')
        return
      }

      this.checkingStatus = true
      try {
        const response = await paymentAPI.checkPaymentStatus(this.currentPayment.referenceCode)
        this.currentPayment = response.data
        
        if (this.currentPayment.status === 'COMPLETED') {
          this.setMessage('Thanh toán thành công! Gói dịch vụ đã được kích hoạt.', 'success')
          // Refresh current package after successful payment
          await this.loadCurrentPackage()
        } else if (this.currentPayment.status === 'EXPIRED') {
          this.setMessage('Yêu cầu thanh toán đã hết hạn. Vui lòng tạo yêu cầu mới.', 'error')
        }
        
        console.log('✅ Payment status checked:', this.currentPayment)
        return this.currentPayment
      } catch (error) {
        console.error('❌ Error checking payment status:', error)
        this.setMessage('Lỗi kiểm tra trạng thái thanh toán: ' + (error.response?.data?.message || error.message || 'Unknown error'), 'error')
        throw error
      } finally {
        this.checkingStatus = false
      }
    },

    /**
     * Giả lập thanh toán (cho development)
     */
    async simulatePayment() {
      if (!this.currentPayment?.referenceCode) {
        this.setMessage('No pending payment to simulate', 'error')
        return
      }

      this.loading = true
      try {
        // Pass amount to simulate payment
        await paymentAPI.simulatePayment(this.currentPayment.referenceCode, this.currentPayment.amount)
        
        // Check status after simulation
        await this.checkPaymentStatus()
        
        // Refresh current package to get updated package after payment
        await this.loadCurrentPackage()
        
        this.setMessage('Giả lập thanh toán thành công! Gói dịch vụ đã được kích hoạt.', 'success')
      } catch (error) {
        console.error('❌ Error simulating payment:', error)
        this.setMessage('Lỗi giả lập thanh toán: ' + (error.response?.data?.message || error.message || 'Unknown error'), 'error')
      } finally {
        this.loading = false
      }
    },

    /**
     * Kích hoạt gói miễn phí
     */
    async activateFreePackage() {
      if (!this.selectedPackage || this.selectedPackage.price !== 0) {
        this.setMessage('Gói đã chọn không phải gói miễn phí', 'error')
        return
      }

      this.loading = true
      try {
        // For free packages, we can directly activate without payment
        this.currentPackage = { ...this.selectedPackage }
        this.selectedPackage = null
        
        this.setMessage('Gói miễn phí đã được kích hoạt thành công!', 'success')
        console.log('✅ Free package activated:', this.currentPackage)
      } catch (error) {
        console.error('❌ Error activating free package:', error)
        this.setMessage('Lỗi kích hoạt gói miễn phí: ' + (error.response?.data?.message || error.message || 'Unknown error'), 'error')
      } finally {
        this.loading = false
      }
    },

    /**
     * Load bank information
     */
    async loadBankInfo() {
      try {
        const response = await paymentAPI.getBankInfo()
        this.bankInfo = response.data
        console.log('✅ Bank info loaded:', this.bankInfo)
      } catch (error) {
        console.error('❌ Error loading bank info:', error)
        // Set default bank info if API fails
        this.bankInfo = {
          bankName: 'Vietcombank',
          accountNumber: '1234567890',
          accountName: 'CHATBOT SaaS',
          bankCode: 'VCB',
          branch: 'Chi nhánh TP.HCM'
        }
      }
    },

    /**
     * Load current user package from tenant API
     */
    async loadCurrentPackage() {
      try {
        console.log('Loading current package from tenant API...')
        
        // Get current tenant from gateway store
        const { useGatewayTenantStore } = await import('@/stores/tenant/gateway/myTenantStore')
        const gatewayStore = useGatewayTenantStore()
        
        if (gatewayStore.currentTenant?.tenantKey) {
          // Get specific tenant data with cache-busting
          const response = await tenantApi.getTenant(gatewayStore.currentTenant.tenantKey)
          const tenant = response.data
          
          console.log('Current tenant data:', tenant)
          
          if (tenant.currentPackageId && tenant.currentPackageName) {
            // Map backend tenant data to frontend package format
            this.currentPackage = {
              id: tenant.currentPackageId,
              name: tenant.currentPackageName,
              packageId: tenant.currentPackageId,
              // Try to find full package details from packages list
              ...(this.packages.find(pkg => pkg.packageId === tenant.currentPackageId) || {
                price: 0,
                duration: 'unlimited',
                currency: 'VND',
                description: `${tenant.currentPackageName} package`
              })
            }
            console.log('Current package loaded:', this.currentPackage)
          } else {
            console.warn('No package info found in tenant data')
            // Fallback to free package if available
            if (this.packages.length > 0) {
              const freePackage = this.packages.find(pkg => pkg.packageId === 'free')
              if (freePackage) {
                this.setCurrentPackage('free')
                console.log('Fallback: Set free package as default')
              }
            }
          }
        } else {
          console.warn('No current tenant found')
          // Fallback to free package
          if (this.packages.length > 0) {
            const freePackage = this.packages.find(pkg => pkg.packageId === 'free')
            if (freePackage) {
              this.setCurrentPackage('free')
              console.log('No tenant fallback: Set free package as default')
            }
          }
        }
      } catch (error) {
        console.error('Error loading current package:', error)
        // Fallback to free package if available
        if (this.packages.length > 0) {
          const freePackage = this.packages.find(pkg => pkg.packageId === 'free')
          if (freePackage) {
            this.setCurrentPackage('free')
            console.log('Error fallback: Set free package as default')
          }
        }
      }
    },

    /**
     * Load lịch sử thanh toán
     */
    async loadPaymentHistory() {
      this.loading = true
      try {
        const response = await paymentAPI.getPaymentHistory()
        this.paymentHistory = response.data || []
        console.log('✅ Payment history loaded:', this.paymentHistory.length, 'records')
      } catch (error) {
        console.error('❌ Error loading payment history:', error)
        this.setMessage('Lỗi tải lịch sử thanh toán: ' + (error.response?.data?.message || error.message || 'Unknown error'), 'error')
      } finally {
        this.loading = false
      }
    },

    /**
     * Thiết lập message
     */
    setMessage(message, type = 'success') {
      this.message = message
      this.messageType = type
      
      // Auto clear message after 5 seconds
      setTimeout(() => {
        if (this.message === message) {
          this.message = ''
        }
      }, 5000)
    },

    /**
     * Get message class for styling
     */
    getMessageClass() {
      const baseClasses = 'p-4 rounded-lg mb-4'
      const typeClasses = {
        success: 'bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200',
        error: 'bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-200',
        warning: 'bg-yellow-100 dark:bg-yellow-900 text-yellow-800 dark:text-yellow-200',
        info: 'bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-200'
      }
      return `${baseClasses} ${typeClasses[this.messageType] || typeClasses.info}`
    },

    /**
     * Get status class for payment status
     */
    getStatusClass(status) {
      const statusClasses = {
        PENDING: 'bg-yellow-100 dark:bg-yellow-900 text-yellow-800 dark:text-yellow-200',
        COMPLETED: 'bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200',
        EXPIRED: 'bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-200',
        FAILED: 'bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-200'
      }
      return statusClasses[status] || 'bg-gray-100 dark:bg-gray-900 text-gray-800 dark:text-gray-200'
    },

    /**
     * Get status text in English (fallback)
     */
    getStatusText(status) {
      const statusTexts = {
        PENDING: 'Pending Payment',
        COMPLETED: 'Completed',
        EXPIRED: 'Expired',
        FAILED: 'Failed'
      }
      return statusTexts[status] || status
    },

    /**
     * Copy reference code to clipboard
     */
    async copyReferenceCode(referenceCode) {
      try {
        await navigator.clipboard.writeText(referenceCode)
        this.setMessage('Reference code copied successfully!', 'success')
      } catch (error) {
        console.error('❌ Error copying to clipboard:', error)
        this.setMessage('Lỗi sao chép mã tham chiếu', 'error')
      }
    }
  }
})
