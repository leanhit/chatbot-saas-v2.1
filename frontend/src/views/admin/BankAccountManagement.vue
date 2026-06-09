<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <div class="flex items-center">
        <Icon icon="mdi:bank" class="text-2xl text-blue-600 dark:text-blue-400 mr-3" />
        <h1 class="text-2xl font-bold text-gray-800 dark:text-white">
          Quản Lý Tài Khoản Thụ Hưởng
        </h1>
      </div>
    </div>

    <!-- Alert Messages -->
    <div v-if="message" class="mb-4 p-4 rounded-lg" :class="getMessageClass()">
      {{ message }}
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="text-center py-8">
      <Icon icon="eos-icons:loading" class="text-4xl text-blue-600 dark:text-blue-400 animate-spin mb-4" />
      <p class="text-gray-600 dark:text-gray-400">Đang tải dữ liệu...</p>
    </div>

    <!-- Bank Account Form -->
    <div v-else class="bg-white dark:bg-gray-900 rounded-lg shadow p-6">
      <form @submit.prevent="saveBankInfo" class="space-y-6">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Tên ngân hàng *
            </label>
            <input
              v-model="formData.bankName"
              type="text"
              required
              class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800 dark:text-white"
              placeholder="VD: Vietcombank, MB Bank, Techcombank"
            />
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Số tài khoản *
            </label>
            <input
              v-model="formData.accountNumber"
              type="text"
              required
              class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800 dark:text-white font-mono"
              placeholder="VD: 1234567890"
            />
          </div>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Chủ tài khoản *
          </label>
          <input
            v-model="formData.accountName"
            type="text"
            required
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800 dark:text-white"
            placeholder="VD: CONG TY CHATBOT VIETNAM"
          />
        </div>

        <!-- Preview Section -->
        <div class="mt-8 p-6 bg-gray-50 dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700">
          <h3 class="text-lg font-semibold text-gray-800 dark:text-white mb-4 flex items-center">
            <Icon icon="mdi:eye" class="mr-2" />
            Xem trước thông tin hiển thị
          </h3>
          <div class="space-y-3">
            <div class="flex justify-between items-center py-2 border-b border-gray-200 dark:border-gray-700">
              <span class="text-gray-600 dark:text-gray-400">Tên ngân hàng:</span>
              <span class="font-semibold text-gray-800 dark:text-white">{{ formData.bankName || '-' }}</span>
            </div>
            <div class="flex justify-between items-center py-2 border-b border-gray-200 dark:border-gray-700">
              <span class="text-gray-600 dark:text-gray-400">Số tài khoản:</span>
              <span class="font-mono font-semibold text-gray-800 dark:text-white">{{ formData.accountNumber || '-' }}</span>
            </div>
            <div class="flex justify-between items-center py-2">
              <span class="text-gray-600 dark:text-gray-400">Chủ tài khoản:</span>
              <span class="font-semibold text-gray-800 dark:text-white">{{ formData.accountName || '-' }}</span>
            </div>
          </div>
        </div>

        <!-- Warning Notice -->
        <div class="mt-6 p-4 bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 rounded-lg">
          <div class="flex items-start">
            <Icon icon="mdi:alert" class="text-yellow-600 dark:text-yellow-400 mr-2 mt-0.5" />
            <div class="text-sm text-yellow-800 dark:text-yellow-200">
              <p class="font-semibold mb-1">Lưu ý quan trọng:</p>
              <ul class="list-disc list-inside space-y-1">
                <li>Thông tin tài khoản thụ hưởng sẽ được hiển thị cho tất cả người dùng khi nạp tiền</li>
                <li>Vui lòng kiểm tra kỹ thông tin trước khi lưu</li>
                <li>Thay đổi sẽ có hiệu lực ngay lập tức</li>
              </ul>
            </div>
          </div>
        </div>

        <div class="flex justify-end space-x-3 pt-4">
          <button
            type="button"
            @click="resetForm"
            class="px-4 py-2 bg-gray-300 dark:bg-gray-600 text-gray-700 dark:text-gray-300 rounded hover:bg-gray-400 dark:hover:bg-gray-500"
          >
            <Icon icon="mdi:refresh" class="mr-2" />
            Đặt lại
          </button>
          <button
            type="submit"
            :disabled="saving"
            class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
          >
            <span v-if="saving" class="flex items-center">
              <Icon icon="eos-icons:loading" class="animate-spin mr-2" />
              Đang lưu...
            </span>
            <span v-else class="flex items-center">
              <Icon icon="mdi:content-save" class="mr-2" />
              Lưu thông tin
            </span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { Icon } from '@iconify/vue'
import paymentAPI from '@/api/paymentApi'

export default {
  name: 'BankAccountManagement',
  components: {
    Icon
  },
  data() {
    return {
      loading: false,
      saving: false,
      message: '',
      messageType: 'success',
      formData: {
        bankName: '',
        accountNumber: '',
        accountName: ''
      },
      originalData: {
        bankName: '',
        accountNumber: '',
        accountName: ''
      }
    }
  },
  async mounted() {
    await this.loadBankInfo()
  },
  methods: {
    async loadBankInfo() {
      this.loading = true
      try {
        const response = await paymentAPI.getBankInfo()
        this.formData = {
          bankName: response.data.bankName || '',
          accountNumber: response.data.accountNumber || '',
          accountName: response.data.accountName || ''
        }
        this.originalData = { ...this.formData }
      } catch (error) {
        console.error('Error loading bank info:', error)
        this.setMessage('Error loading bank info: ' + (error.message || 'Unknown error'), 'error')
      } finally {
        this.loading = false
      }
    },

    async saveBankInfo() {
      this.saving = true
      try {
        await paymentAPI.updateBankInfo(this.formData)
        this.setMessage('Bank information updated successfully!', 'success')
        this.originalData = { ...this.formData }
      } catch (error) {
        console.error('Error saving bank info:', error)
        this.setMessage('Error saving bank info: ' + (error.response?.data?.message || error.message || 'Unknown error'), 'error')
      } finally {
        this.saving = false
      }
    },

    resetForm() {
      this.formData = { ...this.originalData }
      this.setMessage('Form reset to original values', 'info')
    },

    setMessage(message, type = 'success') {
      this.message = message
      this.messageType = type
      
      setTimeout(() => {
        if (this.message === message) {
          this.message = ''
        }
      }, 5000)
    },

    getMessageClass() {
      const baseClasses = 'p-4 rounded-lg mb-4'
      const typeClasses = {
        success: 'bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200',
        error: 'bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-200',
        warning: 'bg-yellow-100 dark:bg-yellow-900 text-yellow-800 dark:text-yellow-200',
        info: 'bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-200'
      }
      return `${baseClasses} ${typeClasses[this.messageType] || typeClasses.info}`
    }
  }
}
</script>
