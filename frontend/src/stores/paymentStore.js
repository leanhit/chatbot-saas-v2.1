import { defineStore } from 'pinia'
import { formatCurrency, formatDateTime } from '@/utils/dateUtils'

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
        completed: payments.filter(p => p.status === 'COMPLETED').length,
        pending: payments.filter(p => p.status === 'PENDING').length,
        failed: payments.filter(p => p.status === 'FAILED').length,
        expired: payments.filter(p => p.status === 'EXPIRED').length,
        total: payments.length
      }
    },

    /**
     * Kiểm tra có payment đang chờ xử lý không
     */
    hasPendingPayment: (state) => {
      return state.currentPayment && state.currentPayment.status === 'PENDING'
    },

    /**
     * Lấy tổng số tiền của gói đã chọn
     */
    selectedAmount: (state) => {
      return state.customAmount ? 
        parseFloat(state.customAmount) : 
        (state.selectedPackage ? state.selectedPackage.price : 0)
    },

    /**
     * Kiểm tra có thể tạo yêu cầu không
     */
    canCreatePayment: (state) => {
      return !state.loading && state.selectedPackage && state.selectedAmount > 0
    },

    /**
     * Kiểm tra có thể kiểm tra trạng thái không
     */
    canCheckStatus: (state) => {
      return state.currentPayment && !state.checkingStatus
    },

    /**
     * Format tiền tệ theo Việt Nam
     */
    formattedAmount: (state) => {
      return formatCurrency(state.selectedAmount)
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
    }
  },

  actions: {
    /**
     * Reset state về giá trị mặc định
     */
    resetState: () => ({
      // Current payment being processed
      currentPayment: null,
      
      // Payment history
      paymentHistory: [],
      
      // Bank information
      bankInfo: null,
      
      // UI state
      loading: false,
      message: '',
      messageType: 'info', // 'success', 'error', 'info', 'warning'
      
      // Current user's active package (from backend)
      currentPackage: null,
      
      // Selected package for payment (may be different from current)
      selectedPackage: null,
      
      // Available packages
      packages: [
        {
          id: 'free',
          name: 'Free',
          price: 0,
          duration: 'Vĩnh viễn',
          features: [
            '100 tin nhắn/tháng',
            '1 chatbot',
            'Support cơ bản'
          ]
        },
        {
          id: 'pro',
          name: 'Pro',
          price: 250000,
          duration: '1 tháng',
          features: [
            '5.000 tin nhắn/tháng',
            '3 chatbots',
            'Support ưu tiên',
            'Analytics cơ bản'
          ]
        },
        {
          id: 'business',
          name: 'Business',
          price: 500000,
          duration: '1 tháng',
          features: [
            '15.000 tin nhắn/tháng',
            '10 chatbots',
            'Support 24/7',
            'Analytics nâng cao',
            'Custom integrations'
          ]
        },
        {
          id: 'enterprise',
          name: 'Enterprise',
          price: 1000000,
          duration: '1 tháng',
          features: [
            'Unlimited tin nhắn',
            'Unlimited chatbots',
            'Dedicated support',
            'Custom features',
            'SLA guarantee'
          ]
        }
      ]
    }),

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
      const packages = [
        {
          id: 'free',
          name: 'Free',
          price: 0,
          duration: 'Vĩnh viễn',
          features: [
            '100 tin nhắn/tháng',
            '1 chatbot',
            'Support cơ bản'
          ]
        },
        {
          id: 'pro',
          name: 'Pro',
          price: 250000,
          duration: '1 tháng',
          features: [
            '5.000 tin nhắn/tháng',
            '3 chatbots',
            'Support ưu tiên',
            'Analytics cơ bản'
          ]
        },
        {
          id: 'business',
          name: 'Business',
          price: 500000,
          duration: '1 tháng',
          features: [
            '15.000 tin nhắn/tháng',
            '10 chatbots',
            'Support 24/7',
            'Analytics nâng cao',
            'Custom integrations'
          ]
        },
        {
          id: 'enterprise',
          name: 'Enterprise',
          price: 1000000,
          duration: '1 tháng',
          features: [
            'Unlimited tin nhắn',
            'Unlimited chatbots',
            'Dedicated support',
            'Custom features',
            'SLA guarantee'
          ]
        }
      ]
      const pkg = packages.find(pkg => pkg.id === packageId)
      console.log('Found package:', pkg)
      this.currentPackage = pkg
    },

    /**
     * Chọn gói dịch vụ để thanh toán
     */
    selectPackage(packageId) {
      const packages = [
        {
          id: 'free',
          name: 'Free',
          price: 0,
          duration: 'Vĩnh viễn',
          features: [
            '100 tin nhắn/tháng',
            '1 chatbot',
            'Support cơ bản'
          ]
        },
        {
          id: 'pro',
          name: 'Pro',
          price: 250000,
          duration: '1 tháng',
          features: [
            '5.000 tin nhắn/tháng',
            '3 chatbots',
            'Support ưu tiên',
            'Analytics cơ bản'
          ]
        },
        {
          id: 'business',
          name: 'Business',
          price: 500000,
          duration: '1 tháng',
          features: [
            '15.000 tin nhắn/tháng',
            '10 chatbots',
            'Support 24/7',
            'Analytics nâng cao',
            'Custom integrations'
          ]
        },
        {
          id: 'enterprise',
          name: 'Enterprise',
          price: 1000000,
          duration: '1 tháng',
          features: [
            'Unlimited tin nhắn',
            'Unlimited chatbots',
            'Dedicated support',
            'Custom features',
            'SLA guarantee'
          ]
        }
      ]
      const selectedPkg = packages.find(pkg => pkg.id === packageId)
      if (selectedPkg) {
        this.selectedPackage = selectedPkg
        this.clearMessage()
      }
    },

    /**
     * Thiết lập số tiền tùy chỉnh
     */
    setCustomAmount(amount) {
      this.customAmount = amount
      this.selectedPackage = null
    },

    /**
     * Thiết lập mô tả tùy chỉnh
     */
    setCustomDescription(description) {
      this.customDescription = description
    },

    /**
     * Thiết lập bộ lọc
     */
    setFilterStatus(status) {
      this.filterStatus = status
    },

    /**
     * Thiết lập thông báo
     */
    setMessage(message, type = 'success') {
      this.message = message
      this.messageType = type
    },

    /**
     * Xóa thông báo
     */
    clearMessage() {
      this.message = ''
      this.messageType = 'success'
    },

    /**
     * Thiết lập payment hiện tại
     */
    setCurrentPayment(payment) {
      this.currentPayment = payment
    },

    /**
     * Thêm payment vào lịch sử
     */
    addToHistory(payment) {
      if (payment) {
        this.paymentHistory.unshift(payment)
        // Giới hạn lịch sử lý (ví dụ: 50 giao dịch gần nhất)
        if (this.paymentHistory.length > 50) {
          this.paymentHistory = this.paymentHistory.slice(0, 50)
        }
      }
    },

    /**
     * Cập nhật payment trong lịch sử
     */
    updatePaymentInHistory(updatedPayment) {
      const index = this.paymentHistory.findIndex(p => p.referenceCode === updatedPayment.referenceCode)
      if (index !== -1) {
        this.paymentHistory[index] = updatedPayment
      }
    },

    /**
     * Xóa payment khỏi lịch sử
     */
    removeFromHistory(referenceCode) {
      const index = this.paymentHistory.findIndex(p => p.referenceCode === referenceCode)
      if (index !== -1) {
        this.paymentHistory.splice(index, 1)
      }
    },

    /**
     * Tải lịch sử thanh toán
     */
    async loadPaymentHistory() {
      this.loading = true
      this.clearMessage()
      
      try {
        const response = await fetch('http://localhost:8080/api/public/simple-payment/history')
        
        if (response.ok) {
          const payments = await response.json()
          this.paymentHistory = payments
          this.setMessage('Tải lịch sử thành công!', 'success')
        } else {
          throw new Error('Failed to load payment history')
        }
      } catch (error) {
        this.setMessage('Tải lịch sử thất bại', 'error')
        console.error('Error loading payment history:', error)
      } finally {
        this.loading = false
      }
    },

    /**
     * Load current user package from backend
     */
    async loadCurrentPackage() {
      try {
        // Fallback to free package for now since API returns 403
        // TODO: Fix authentication for /api/users/current-package
        this.setCurrentPackage('free')
        this.setMessage('Đang sử dụng gói Free', 'success')
        
        // Get current user info from auth store or API
        // const response = await fetch('http://localhost:8080/api/users/current-package')
        // if (response.ok) {
        //   const packageData = await response.json()
        //   if (packageData.currentPackage) {
        //     this.setCurrentPackage(packageData.currentPackage.id)
        //     this.setMessage(`Đang sử dụng gói ${packageData.currentPackage.name}`, 'success')
        //   }
        // } else {
        //   // Fallback to free package if API fails
        //   this.setCurrentPackage('free')
        // }
      } catch (error) {
        console.error('Error loading current package:', error)
        // Fallback to free package
        this.setCurrentPackage('free')
      }
    },

    /**
     * Lấy thông tin ngân hàng
     */
    async loadBankInfo() {
      try {
        const response = await fetch('http://localhost:8080/api/public/simple-payment/bank-info')
        if (response.ok) {
          const bankInfo = await response.json()
          this.setBankInfo(bankInfo)
          this.setMessage('Tải thông tin ngân hàng thành công!', 'success')
        } else {
          throw new Error('Failed to load bank info')
        }
      } catch (error) {
        this.setMessage('Tải thông tin ngân hàng thất bại', 'error')
        console.error('Error loading bank info:', error)
      }
    },

    /**
     * Tạo yêu cầu nạp tiền
     */
    async createDeposit() {
      if (!this.canCreatePayment) return

      this.loading = true
      this.clearMessage()
      
      try {
        const payload = {
          amount: this.selectedAmount,
          description: this.selectedPackage ? 
            `Thanh toán gói ${this.selectedPackage.name}` : 
            this.customDescription
        }

        const response = await fetch('http://localhost:8080/api/public/simple-payment/deposit', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(payload)
        })

        if (response.ok) {
          const payment = await response.json()
          this.setCurrentPayment(payment)
          this.addToHistory(payment)
          this.setMessage('Yêu cầu nạp tiền đã được tạo thành công!', 'success')
        } else {
          throw new Error('Failed to create deposit')
        }
      } catch (error) {
        this.setMessage('Tạo yêu cầu nạp tiền thất bại. Vui lòng thử lại.', 'error')
        console.error('Error creating deposit:', error)
      } finally {
        this.loading = false
      }
    },

    /**
     * Kiểm tra trạng thái thanh toán
     */
    async checkPaymentStatus() {
      if (!this.canCheckStatus) return

      this.checkingStatus = true
      this.clearMessage()
      
      try {
        const response = await fetch(`http://localhost:8080/api/public/simple-payment/status/${this.currentPayment.referenceCode}`)
        
        if (response.ok) {
          const updatedPayment = await response.json()
          this.setCurrentPayment(updatedPayment)
          this.updatePaymentInHistory(updatedPayment)
          this.setMessage('Trạng thái đã cập nhật!', 'success')
        } else {
          throw new Error('Failed to check status')
        }
      } catch (error) {
        this.setMessage('Kiểm tra trạng thái thất bại', 'error')
        console.error('Error checking status:', error)
      } finally {
        this.checkingStatus = false
      }
    },

    /**
     * Giả lập thanh toán (test)
     */
    async simulatePayment() {
      if (!this.currentPayment || this.currentPayment.status !== 'PENDING') return

      try {
        const response = await fetch('http://localhost:8080/api/public/simple-payment/test/simulate-payment', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            referenceCode: this.currentPayment.referenceCode,
            amount: this.currentPayment.amount
          })
        })

        if (response.ok) {
          this.setMessage('Đã giả lập thanh toán! Kiểm tra trạng thái sau 10-15 giây...', 'success')
          
          // Auto check status after 15 seconds
          setTimeout(() => {
            this.checkPaymentStatus()
          }, 15000)
        } else {
          throw new Error('Failed to simulate payment')
        }
      } catch (error) {
        this.setMessage('Giả lập thanh toán thất bại', 'error')
        console.error('Error simulating payment:', error)
      }
    },

    /**
     * Kích hoạt gói miễn phí
     */
    activateFreePackage() {
      this.setMessage('Chúc mừng! Gói miễn phí đã được kích hoạt thành công!', 'success')
      
      // Reset selected package sau khi kích hoạt
      setTimeout(() => {
        this.selectedPackage = null
        this.currentPayment = null
      }, 3000)
    },

    /**
     * Copy mã tham chiếu
     */
    copyReferenceCode(referenceCode) {
      navigator.clipboard.writeText(referenceCode).then(() => {
        this.setMessage(`Mã tham chiếu ${referenceCode} đã được sao chép!`, 'success')
      }).catch(() => {
        this.setMessage('Sao chép mã tham chiếu thất bại', 'error')
      })
    },

    /**
     * Lấy text trạng thái
     */
    getStatusText(status) {
      const statusMap = {
        'COMPLETED': 'Đã hoàn thành',
        'PENDING': 'Chờ thanh toán',
        'FAILED': 'Thất bại',
        'EXPIRED': 'Hết hạn'
      }
      return statusMap[status] || status
    },

    /**
     * Lấy class CSS cho trạng thái
     */
    getStatusClass(status) {
      const classMap = {
        'COMPLETED': 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
        'PENDING': 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200',
        'FAILED': 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200',
        'EXPIRED': 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
      }
      return classMap[status] || 'bg-gray-100 text-gray-800'
    },

    /**
     * Lấy class CSS cho thông báo
     */
    getMessageClass() {
      return this.messageType === 'success' 
        ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
        : 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
    }
  }
})
