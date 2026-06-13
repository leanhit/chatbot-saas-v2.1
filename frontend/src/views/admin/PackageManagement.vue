<template>
  <div>
    <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <div class="flex items-center">
        <Icon icon="mdi:package-variant-closed" class="text-2xl text-blue-600 dark:text-blue-400 mr-3" />
        <h1 class="text-2xl font-bold text-gray-800 dark:text-white">
          {{ $t('admin.package.title') }}
        </h1>
      </div>
      <div class="flex gap-3">
        <button
          @click="initializePackages"
          :disabled="loading"
          class="inline-flex items-center bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 disabled:opacity-50"
        >
          <Icon icon="mdi:database-plus" class="mr-2" />
          {{ $t('admin.package.initDefault') }}
        </button>
        <button
          @click="showCreateModal = true"
          class="inline-flex items-center bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
        >
          <Icon icon="mdi:plus" class="mr-2" />
          {{ $t('admin.package.addNew') }}
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
      <p class="text-gray-600 dark:text-gray-400">{{ $t('admin.package.loadingData') }}</p>
    </div>

    <!-- Packages Table -->
    <div v-else class="bg-white dark:bg-gray-900 rounded-lg shadow overflow-hidden">
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
          <thead class="bg-gray-50 dark:bg-gray-800">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                {{ $t('admin.package.id') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                {{ $t('admin.package.name') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                {{ $t('admin.package.price') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                {{ $t('admin.package.duration') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                {{ $t('admin.package.messages') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                {{ $t('admin.package.chatbots') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                {{ $t('admin.package.status') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                {{ $t('admin.package.actions') }}
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
                {{ pkg.price === 0 ? $t('admin.package.free') : formatCurrency(pkg.price) }}
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
                  {{ pkg.isActive ? $t('admin.package.active') : $t('admin.package.inactive') }}
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
                  @click="promptDelete(pkg)"
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
            {{ showEditModal ? $t('admin.package.edit') : $t('admin.package.create') }}
          </h3>
          
          <form @submit.prevent="savePackage" class="space-y-4">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  {{ $t('admin.package.packageIdLabel') }}
                </label>
                <input
                  v-model="formData.packageId"
                  type="text"
                  required
                  :disabled="showEditModal"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800 dark:text-white"
                  :placeholder="$t('admin.package.packageIdPlaceholder')"
                />
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  {{ $t('admin.package.nameLabel') }}
                </label>
                <input
                  v-model="formData.name"
                  type="text"
                  required
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800 dark:text-white"
                  :placeholder="$t('admin.package.namePlaceholder')"
                />
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  {{ $t('admin.package.priceLabel') }}
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
                  {{ $t('admin.package.durationLabel') }}
                </label>
                <input
                  v-model="formData.duration"
                  type="text"
                  required
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800 dark:text-white"
                  :placeholder="$t('admin.package.durationPlaceholder')"
                />
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  {{ $t('admin.package.messageLimitLabel') }}
                </label>
                <input
                  v-model.number="formData.messageLimit"
                  type="number"
                  min="1"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800 dark:text-white"
                  :placeholder="$t('admin.package.messageLimitPlaceholder')"
                />
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  {{ $t('admin.package.chatbotLimitLabel') }}
                </label>
                <input
                  v-model.number="formData.chatbotLimit"
                  type="number"
                  min="1"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800 dark:text-white"
                  :placeholder="$t('admin.package.chatbotLimitPlaceholder')"
                />
              </div>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                {{ $t('admin.package.descriptionLabel') }}
              </label>
              <textarea
                v-model="formData.description"
                rows="3"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800 dark:text-white"
                :placeholder="$t('admin.package.descriptionPlaceholder')"
              ></textarea>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                {{ $t('admin.package.badgeLabel') }}
              </label>
              <input
                v-model="formData.badge"
                type="text"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800 dark:text-white"
                :placeholder="$t('admin.package.badgePlaceholder')"
              />
            </div>
            
            <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
              <label class="flex items-center">
                <input
                  v-model="formData.hasPrioritySupport"
                  type="checkbox"
                  class="mr-2"
                />
                <span class="text-sm text-gray-700 dark:text-gray-300">{{ $t('admin.package.prioritySupport') }}</span>
              </label>
              
              <label class="flex items-center">
                <input
                  v-model="formData.hasDedicatedSupport"
                  type="checkbox"
                  class="mr-2"
                />
                <span class="text-sm text-gray-700 dark:text-gray-300">{{ $t('admin.package.dedicatedSupport') }}</span>
              </label>
              
              <label class="flex items-center">
                <input
                  v-model="formData.hasAnalytics"
                  type="checkbox"
                  class="mr-2"
                />
                <span class="text-sm text-gray-700 dark:text-gray-300">{{ $t('admin.package.basicAnalytics') }}</span>
              </label>
              
              <label class="flex items-center">
                <input
                  v-model="formData.hasAdvancedAnalytics"
                  type="checkbox"
                  class="mr-2"
                />
                <span class="text-sm text-gray-700 dark:text-gray-300">{{ $t('admin.package.advancedAnalytics') }}</span>
              </label>
              
              <label class="flex items-center">
                <input
                  v-model="formData.hasCustomIntegrations"
                  type="checkbox"
                  class="mr-2"
                />
                <span class="text-sm text-gray-700 dark:text-gray-300">{{ $t('admin.package.customIntegrations') }}</span>
              </label>
              
              <label class="flex items-center">
                <input
                  v-model="formData.hasCustomFeatures"
                  type="checkbox"
                  class="mr-2"
                />
                <span class="text-sm text-gray-700 dark:text-gray-300">{{ $t('admin.package.customFeatures') }}</span>
              </label>
              
              <label class="flex items-center">
                <input
                  v-model="formData.hasSlaGuarantee"
                  type="checkbox"
                  class="mr-2"
                />
                <span class="text-sm text-gray-700 dark:text-gray-300">{{ $t('admin.package.slaGuarantee') }}</span>
              </label>
              
              <label class="flex items-center">
                <input
                  v-model="formData.isActive"
                  type="checkbox"
                  class="mr-2"
                />
                <span class="text-sm text-gray-700 dark:text-gray-300">{{ $t('admin.package.active') }}</span>
              </label>
            </div>
            
            <div class="flex justify-end space-x-3 pt-4">
              <button
                type="button"
                @click="closeModal"
                class="inline-flex items-center px-4 py-2 bg-gray-300 dark:bg-gray-600 text-gray-700 dark:text-gray-300 rounded hover:bg-gray-400 dark:hover:bg-gray-500"
              >
                {{ $t('common.cancel') }}
              </button>
              <button
                type="submit"
                :disabled="saving"
                class="inline-flex items-center px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
              >
                <span v-if="saving" class="inline-flex items-center">
                  <Icon icon="eos-icons:loading" class="animate-spin mr-2" />
                  {{ $t('common.saving') }}
                </span>
                <span v-else>
                  {{ showEditModal ? $t('common.update') : $t('common.create') }}
                </span>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>

    <div v-if="showDeleteConfirmModal" class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50 flex items-center justify-center">
      <div class="relative mx-auto p-6 border w-96 shadow-lg rounded-md bg-white dark:bg-gray-900 text-center">
        <Icon icon="mdi:alert-circle-outline" class="mx-auto text-red-500 text-5xl mb-4" />
        <h3 class="text-lg font-bold text-gray-900 dark:text-white mb-2">{{ $t('admin.package.deleteConfirmTitle') }}</h3>
        <p class="text-sm text-gray-500 dark:text-gray-400 mb-6">
          {{ $t('admin.package.deleteConfirmMessage') }} <span class="font-semibold text-gray-700 dark:text-gray-300">"{{ packageToDelete?.name }}"</span>? {{ $t('admin.package.deleteConfirmUndone') }}
        </p>
        <div class="flex justify-center space-x-3">
          <button @click="cancelDelete" class="px-4 py-2 bg-gray-300 dark:bg-gray-600 text-gray-700 dark:text-gray-300 rounded hover:bg-gray-400 dark:hover:bg-gray-500 text-sm font-medium">{{ $t('common.cancel') }}</button>
          <button @click="confirmDelete" class="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 text-sm font-medium">{{ $t('admin.package.deleteConfirmTitle') }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Icon } from '@iconify/vue'
import packageApi from '@/api/packageApi'
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'

export default {
  name: 'PackageManagement',
  components: {
    Icon
  },
  setup() {
    const { t } = useI18n()
    const packages = ref([])
    const loading = ref(false)
    const saving = ref(false)
    const message = ref('')
    const messageType = ref('success')
    const showCreateModal = ref(false)
    const showEditModal = ref(false)
    const showDeleteConfirmModal = ref(false)
    const packageToDelete = ref(null)
    const editingPackage = ref(null)
    const formData = ref({
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
    })

    const loadPackages = async () => {
      loading.value = true
      try {
        const response = await packageApi.getAllPackages()
        packages.value = response.data || []
      } catch (error) {
        console.error('Error loading packages:', error)
        setMessage(t('admin.package.loadingData') + ': ' + (error.message || 'Unknown error'), 'error')
      } finally {
        loading.value = false
      }
    }

    const initializePackages = async () => {
      try {
        await packageApi.initializeDefaultPackages()
        setMessage(t('admin.package.initSuccess'), 'success')
        await loadPackages()
      } catch (error) {
        console.error('Error initializing packages:', error)
        setMessage(t('common.error') + ': ' + (error.message || 'Unknown error'), 'error')
      }
    }

    const editPackage = (pkg) => {
      editingPackage.value = pkg
      formData.value = { ...pkg }
      showEditModal.value = true
    }

    const savePackage = async () => {
      saving.value = true
      try {
        // Set unlimited values if empty
        if (!formData.value.messageLimit) {
          formData.value.messageLimit = 2147483647
        }
        if (!formData.value.chatbotLimit) {
          formData.value.chatbotLimit = 2147483647
        }

        if (showEditModal.value) {
          await packageApi.updatePackage(editingPackage.value.id, formData.value)
          setMessage(t('admin.package.updateSuccess'), 'success')
        } else {
          await packageApi.createPackage(formData.value)
          setMessage(t('admin.package.createSuccess'), 'success')
        }
        
        closeModal()
        await loadPackages()
      } catch (error) {
        console.error('Error saving package:', error)
        setMessage(t('common.error') + ': ' + (error.response?.data?.message || error.message || 'Unknown error'), 'error')
      } finally {
        saving.value = false
      }
    }

    const promptDelete = (pkg) => {
      packageToDelete.value = pkg
      showDeleteConfirmModal.value = true
    }

    const cancelDelete = () => {
      packageToDelete.value = null
      showDeleteConfirmModal.value = false
    }

    const confirmDelete = async () => {
      if (!packageToDelete.value) return

      try {
        await packageApi.deletePackage(packageToDelete.value.id)
        setMessage(t('admin.package.deleteSuccess'), 'success')
        await loadPackages()
      } catch (error) {
        console.error('Error deleting package:', error)
        setMessage(t('common.error') + ': ' + (error.message || 'Unknown error'), 'error')
      } finally {
        cancelDelete()
      }
    }

    const closeModal = () => {
      showCreateModal.value = false
      showEditModal.value = false
      editingPackage.value = null
      formData.value = {
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

    const setMessage = (msg, type = 'success') => {
      message.value = msg
      messageType.value = type
      
      setTimeout(() => {
        if (message.value === msg) {
          message.value = ''
        }
      }, 5000)
    }

    const getMessageClass = () => {
      const baseClasses = 'p-4 rounded-lg mb-4'
      const typeClasses = {
        success: 'bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200',
        error: 'bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-200',
        warning: 'bg-yellow-100 dark:bg-yellow-900 text-yellow-800 dark:text-yellow-200',
        info: 'bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-200'
      }
      return `${baseClasses} ${typeClasses[messageType.value] || typeClasses.info}`
    }

    const formatCurrency = (amount) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(amount)
    }

    onMounted(() => {
      loadPackages()
    })

    return {
      t,
      packages,
      loading,
      saving,
      message,
      messageType,
      showCreateModal,
      showEditModal,
      showDeleteConfirmModal,
      packageToDelete,
      editingPackage,
      formData,
      loadPackages,
      initializePackages,
      editPackage,
      savePackage,
      promptDelete,
      cancelDelete,
      confirmDelete,
      closeModal,
      getMessageClass,
      formatCurrency
    }
  }
}
</script>
