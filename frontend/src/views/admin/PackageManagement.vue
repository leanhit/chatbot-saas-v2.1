<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <div class="flex items-center">
        <Icon icon="mdi:package-variant-closed" class="text-2xl text-blue-600 dark:text-blue-400 mr-3" />
        <h1 class="text-2xl font-bold text-gray-800 dark:text-white">
          Quản Lý Gói Dịch Vụ
        </h1>
      </div>
      <div class="flex gap-3">
        <button
          @click="initializePackages"
          :disabled="loading"
          class="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 disabled:opacity-50"
        >
          <Icon icon="mdi:database-plus" class="mr-2" />
          Khởi tạo gói mặc định
        </button>
        <button
          @click="showCreateModal = true"
          class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
        >
          <Icon icon="mdi:plus" class="mr-2" />
          Thêm gói mới
        </button>
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

    <!-- Packages Table -->
    <div v-else class="bg-white dark:bg-gray-900 rounded-lg shadow overflow-hidden">
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
          <thead class="bg-gray-50 dark:bg-gray-800">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                ID
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                Tên gói
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                Giá
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                Thời hạn
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                Tin nhắn
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                Chatbots
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                Trạng thái
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                Thao tác
              </th>
            </tr>
          </thead>
          <tbody class="bg-white dark:bg-gray-900 divide-y divide-gray-200 dark:divide-gray-700">
            <tr v-for="pkg in packages" :key="pkg.id" class="hover:bg-gray-50 dark:hover:bg-gray-800">
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                {{ pkg.packageId }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex items-center">
                  <span class="text-sm font-medium text-gray-900 dark:text-white">{{ pkg.name }}</span>
                  <span v-if="pkg.badge" class="ml-2 px-2 py-1 text-xs font-semibold rounded-full bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-200">
                    {{ pkg.badge }}
                  </span>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                {{ pkg.price === 0 ? 'Miễn phí' : formatCurrency(pkg.price) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                {{ pkg.duration }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                {{ pkg.messageLimit === 2147483647 ? 'Unlimited' : pkg.messageLimit?.toLocaleString() }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                {{ pkg.chatbotLimit === 2147483647 ? 'Unlimited' : pkg.chatbotLimit?.toLocaleString() }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full" :class="pkg.isActive ? 'bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200' : 'bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-200'">
                  {{ pkg.isActive ? 'Hoạt động' : 'Ngừng hoạt động' }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                <button
                  @click="editPackage(pkg)"
                  class="text-blue-600 dark:text-blue-400 hover:text-blue-900 dark:hover:text-blue-300 mr-3"
                >
                  <Icon icon="mdi:pencil" class="w-4 h-4" />
                </button>
                <button
                  @click="deletePackage(pkg)"
                  class="text-red-600 dark:text-red-400 hover:text-red-900 dark:hover:text-red-300"
                >
                  <Icon icon="mdi:delete" class="w-4 h-4" />
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <div v-if="showCreateModal || showEditModal" class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
      <div class="relative top-20 mx-auto p-5 border w-11/12 md:w-3/4 lg:w-1/2 shadow-lg rounded-md bg-white dark:bg-gray-900">
        <div class="mt-3">
          <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-4">
            {{ showEditModal ? 'Chỉnh sửa gói dịch vụ' : 'Thêm gói dịch vụ mới' }}
          </h3>
          
          <form @submit.prevent="savePackage" class="space-y-4">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  ID gói dịch vụ *
                </label>
                <input
                  v-model="formData.packageId"
                  type="text"
                  required
                  :disabled="showEditModal"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800 dark:text-white"
                  placeholder="VD: free, 3months, 6months, 12months"
                />
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  Tên gói *
                </label>
                <input
                  v-model="formData.name"
                  type="text"
                  required
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800 dark:text-white"
                  placeholder="VD: Free, 3 Months, 6 Months, 12 Months"
                />
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  Giá (₫) *
                </label>
                <input
                  v-model.number="formData.price"
                  type="number"
                  min="0"
                  step="1000"
                  required
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800 dark:text-white"
                  placeholder="250000"
                />
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  Thời hạn *
                </label>
                <input
                  v-model="formData.duration"
                  type="text"
                  required
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800 dark:text-white"
                  placeholder="VD: 1 month, 3 months, 6 months, 12 months"
                />
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  Giới hạn tin nhắn
                </label>
                <input
                  v-model.number="formData.messageLimit"
                  type="number"
                  min="1"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800 dark:text-white"
                  placeholder="1000 (để trống cho unlimited)"
                />
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  Giới hạn chatbots
                </label>
                <input
                  v-model.number="formData.chatbotLimit"
                  type="number"
                  min="1"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800 dark:text-white"
                  placeholder="5 (để trống cho unlimited)"
                />
              </div>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Mô tả
              </label>
              <textarea
                v-model="formData.description"
                rows="3"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800 dark:text-white"
                placeholder="Mô tả gói dịch vụ"
              ></textarea>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Badge (nhãn)
              </label>
              <input
                v-model="formData.badge"
                type="text"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800 dark:text-white"
                placeholder="VD: POPULAR, RECOMMENDED"
              />
            </div>
            
            <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
              <label class="flex items-center">
                <input
                  v-model="formData.hasPrioritySupport"
                  type="checkbox"
                  class="mr-2"
                />
                <span class="text-sm text-gray-700 dark:text-gray-300">Support ưu tiên</span>
              </label>
              
              <label class="flex items-center">
                <input
                  v-model="formData.hasDedicatedSupport"
                  type="checkbox"
                  class="mr-2"
                />
                <span class="text-sm text-gray-700 dark:text-gray-300">Support 24/7</span>
              </label>
              
              <label class="flex items-center">
                <input
                  v-model="formData.hasAnalytics"
                  type="checkbox"
                  class="mr-2"
                />
                <span class="text-sm text-gray-700 dark:text-gray-300">Analytics cơ bản</span>
              </label>
              
              <label class="flex items-center">
                <input
                  v-model="formData.hasAdvancedAnalytics"
                  type="checkbox"
                  class="mr-2"
                />
                <span class="text-sm text-gray-700 dark:text-gray-300">Analytics nâng cao</span>
              </label>
              
              <label class="flex items-center">
                <input
                  v-model="formData.hasCustomIntegrations"
                  type="checkbox"
                  class="mr-2"
                />
                <span class="text-sm text-gray-700 dark:text-gray-300">Custom integrations</span>
              </label>
              
              <label class="flex items-center">
                <input
                  v-model="formData.hasCustomFeatures"
                  type="checkbox"
                  class="mr-2"
                />
                <span class="text-sm text-gray-700 dark:text-gray-300">Custom features</span>
              </label>
              
              <label class="flex items-center">
                <input
                  v-model="formData.hasSlaGuarantee"
                  type="checkbox"
                  class="mr-2"
                />
                <span class="text-sm text-gray-700 dark:text-gray-300">SLA guarantee</span>
              </label>
              
              <label class="flex items-center">
                <input
                  v-model="formData.isActive"
                  type="checkbox"
                  class="mr-2"
                />
                <span class="text-sm text-gray-700 dark:text-gray-300">Hoạt động</span>
              </label>
            </div>
            
            <div class="flex justify-end space-x-3 pt-4">
              <button
                type="button"
                @click="closeModal"
                class="px-4 py-2 bg-gray-300 dark:bg-gray-600 text-gray-700 dark:text-gray-300 rounded hover:bg-gray-400 dark:hover:bg-gray-500"
              >
                Hủy
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
                <span v-else>
                  {{ showEditModal ? 'Cập nhật' : 'Tạo' }}
                </span>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Icon } from '@iconify/vue'
import packageApi from '@/api/packageApi'

export default {
  name: 'PackageManagement',
  components: {
    Icon
  },
  data() {
    return {
      packages: [],
      loading: false,
      saving: false,
      message: '',
      messageType: 'success',
      showCreateModal: false,
      showEditModal: false,
      editingPackage: null,
      formData: {
        packageId: '',
        name: '',
        price: 0,
        currency: 'VND',
        duration: '',
        description: '',
        messageLimit: null,
        chatbotLimit: null,
        hasPrioritySupport: false,
        hasDedicatedSupport: false,
        hasAnalytics: false,
        hasAdvancedAnalytics: false,
        hasCustomIntegrations: false,
        hasCustomFeatures: false,
        hasSlaGuarantee: false,
        isActive: true,
        sortOrder: 1,
        badge: ''
      }
    }
  },
  async mounted() {
    await this.loadPackages()
  },
  methods: {
    async loadPackages() {
      this.loading = true
      try {
        const response = await packageApi.getAllPackages()
        this.packages = response.data || []
      } catch (error) {
        console.error('Error loading packages:', error)
        this.setMessage('Error loading packages: ' + (error.message || 'Unknown error'), 'error')
      } finally {
        this.loading = false
      }
    },

    async initializePackages() {
      try {
        await packageApi.initializeDefaultPackages()
        this.setMessage('Default packages initialized successfully!', 'success')
        await this.loadPackages()
      } catch (error) {
        console.error('Error initializing packages:', error)
        this.setMessage('Error initializing packages: ' + (error.message || 'Unknown error'), 'error')
      }
    },

    editPackage(pkg) {
      this.editingPackage = pkg
      this.formData = { ...pkg }
      this.showEditModal = true
    },

    async savePackage() {
      this.saving = true
      try {
        // Set unlimited values if empty
        if (!this.formData.messageLimit) {
          this.formData.messageLimit = 2147483647
        }
        if (!this.formData.chatbotLimit) {
          this.formData.chatbotLimit = 2147483647
        }

        if (this.showEditModal) {
          await packageApi.updatePackage(this.editingPackage.id, this.formData)
          this.setMessage('Package updated successfully!', 'success')
        } else {
          await packageApi.createPackage(this.formData)
          this.setMessage('Package created successfully!', 'success')
        }
        
        this.closeModal()
        await this.loadPackages()
      } catch (error) {
        console.error('Error saving package:', error)
        this.setMessage('Error saving package: ' + (error.response?.data?.message || error.message || 'Unknown error'), 'error')
      } finally {
        this.saving = false
      }
    },

    async deletePackage(pkg) {
      if (!confirm(`Are you sure you want to delete package "${pkg.name}"?`)) {
        return
      }

      try {
        await packageApi.deletePackage(pkg.id)
        this.setMessage('Package deleted successfully!', 'success')
        await this.loadPackages()
      } catch (error) {
        console.error('Error deleting package:', error)
        this.setMessage('Error deleting package: ' + (error.message || 'Unknown error'), 'error')
      }
    },

    closeModal() {
      this.showCreateModal = false
      this.showEditModal = false
      this.editingPackage = null
      this.formData = {
        packageId: '',
        name: '',
        price: 0,
        currency: 'VND',
        duration: '',
        description: '',
        messageLimit: null,
        chatbotLimit: null,
        hasPrioritySupport: false,
        hasDedicatedSupport: false,
        hasAnalytics: false,
        hasAdvancedAnalytics: false,
        hasCustomIntegrations: false,
        hasCustomFeatures: false,
        hasSlaGuarantee: false,
        isActive: true,
        sortOrder: 1,
        badge: ''
      }
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
    },

    formatCurrency(amount) {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(amount)
    }
  }
}
</script>
