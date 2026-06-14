<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <div class="flex items-center">
        <Icon icon="mdi:bank" class="text-2xl text-blue-600 dark:text-blue-400 mr-3" />
        <h1 class="text-2xl font-bold text-gray-800 dark:text-white">
          {{ $t('admin.bank.title') }}
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
      <p class="text-gray-600 dark:text-gray-400">{{ $t('admin.bank.loading') }}</p>
    </div>

    <!-- Bank Account Form -->
    <div v-else class="bg-white dark:bg-gray-900 rounded-lg shadow p-6">
      <form @submit.prevent="saveBankInfo" class="space-y-6">
        
        <!-- Bank Account Info Section -->
        <div class="border border-gray-200 dark:border-gray-700 rounded-lg">
          <button
            type="button"
            @click="toggleSection('bankAccount')"
            class="w-full px-4 py-3 flex items-center justify-between bg-gray-50 dark:bg-gray-800 rounded-t-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
          >
            <div class="flex items-center">
              <Icon icon="mdi:bank" class="text-xl text-blue-600 dark:text-blue-400 mr-3" />
              <h3 class="text-lg font-semibold text-gray-800 dark:text-white">
                {{ $t('admin.bank.bankAccountInfoTitle') }}
              </h3>
            </div>
            <Icon :icon="sections.bankAccount ? 'mdi:chevron-up' : 'mdi:chevron-down'" class="text-gray-500" />
          </button>
          
          <div v-show="sections.bankAccount" class="p-4 space-y-4">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  {{ $t('admin.bank.bankName') }}
                </label>
                <input
                  v-model="formData.bankName"
                  type="text"
                  required
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800 dark:text-white"
                  :placeholder="$t('admin.bank.bankNamePlaceholder')"
                />
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  {{ $t('admin.bank.accountNumber') }}
                </label>
                <input
                  v-model="formData.accountNumber"
                  type="text"
                  required
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800 dark:text-white font-mono"
                  :placeholder="$t('admin.bank.accountNumberPlaceholder')"
                />
              </div>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                {{ $t('admin.bank.accountName') }}
              </label>
              <input
                v-model="formData.accountName"
                type="text"
                required
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800 dark:text-white"
                :placeholder="$t('admin.bank.accountNamePlaceholder')"
              />
            </div>
          </div>
        </div>

        <!-- Bank API Configuration Section -->
        <div class="border border-gray-200 dark:border-gray-700 rounded-lg">
          <button
            type="button"
            @click="toggleSection('apiConfig')"
            class="w-full px-4 py-3 flex items-center justify-between bg-gray-50 dark:bg-gray-800 rounded-t-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
          >
            <div class="flex items-center">
              <Icon icon="mdi:api" class="text-xl text-blue-600 dark:text-blue-400 mr-3" />
              <h3 class="text-lg font-semibold text-gray-800 dark:text-white">
                {{ $t('admin.bank.apiConfigTitle') }}
              </h3>
            </div>
            <Icon :icon="sections.apiConfig ? 'mdi:chevron-up' : 'mdi:chevron-down'" class="text-gray-500" />
          </button>
          
          <div v-show="sections.apiConfig" class="p-4 space-y-4">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  {{ $t('admin.bank.provider') }}
                </label>
                <select
                  v-model="formData.provider"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800 dark:text-white"
                >
                  <option value="mock">{{ $t('admin.bank.providerMock') }}</option>
                  <option value="vietqr">{{ $t('admin.bank.providerVietQR') }}</option>
                  <option value="nganluong">{{ $t('admin.bank.providerNganLuong') }}</option>
                  <option value="custom">{{ $t('admin.bank.providerCustom') }}</option>
                </select>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  {{ $t('admin.bank.apiUrl') }}
                </label>
                <input
                  v-model="formData.apiUrl"
                  type="url"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800 dark:text-white font-mono"
                  :placeholder="$t('admin.bank.apiUrlPlaceholder')"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  {{ $t('admin.bank.apiKey') }}
                </label>
                <div class="relative">
                  <input
                    v-model="formData.apiKey"
                    :type="showApiKey ? 'text' : 'password'"
                    class="w-full px-3 py-2 pr-10 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800 dark:text-white font-mono"
                    :placeholder="$t('admin.bank.apiKeyPlaceholder')"
                  />
                  <button
                    type="button"
                    @click="showApiKey = !showApiKey"
                    class="absolute right-2 top-1/2 transform -translate-y-1/2 text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
                  >
                    <Icon :icon="showApiKey ? 'mdi:eye-off' : 'mdi:eye'" />
                  </button>
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  {{ $t('admin.bank.timeout') }}
                </label>
                <input
                  v-model.number="formData.timeout"
                  type="number"
                  min="1000"
                  step="1000"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800 dark:text-white"
                  :placeholder="$t('admin.bank.timeoutPlaceholder')"
                />
                <p class="text-xs text-gray-500 dark:text-gray-400 mt-1">{{ $t('admin.bank.timeoutHint') }}</p>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  {{ $t('admin.bank.retryAttempts') }}
                </label>
                <input
                  v-model.number="formData.retryAttempts"
                  type="number"
                  min="0"
                  max="10"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800 dark:text-white"
                  :placeholder="$t('admin.bank.retryAttemptsPlaceholder')"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  {{ $t('admin.bank.retryDelay') }}
                </label>
                <input
                  v-model.number="formData.retryDelay"
                  type="number"
                  min="0"
                  step="100"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800 dark:text-white"
                  :placeholder="$t('admin.bank.retryDelayPlaceholder')"
                />
                <p class="text-xs text-gray-500 dark:text-gray-400 mt-1">{{ $t('admin.bank.retryDelayHint') }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Preview Section -->
        <div class="mt-8 p-6 bg-gray-50 dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700">
          <h3 class="text-lg font-semibold text-gray-800 dark:text-white mb-4 flex items-center">
            <Icon icon="mdi:eye" class="mr-2" />
            {{ $t('admin.bank.previewTitle') }}
          </h3>
          <div class="space-y-3">
            <div class="flex justify-between items-center py-2 border-b border-gray-200 dark:border-gray-700">
              <span class="text-gray-600 dark:text-gray-400">{{ $t('admin.bank.bankName') }}:</span>
              <span class="font-semibold text-gray-800 dark:text-white">{{ formData.bankName || '-' }}</span>
            </div>
            <div class="flex justify-between items-center py-2 border-b border-gray-200 dark:border-gray-700">
              <span class="text-gray-600 dark:text-gray-400">{{ $t('admin.bank.accountNumber') }}:</span>
              <span class="font-mono font-semibold text-gray-800 dark:text-white">{{ formData.accountNumber || '-' }}</span>
            </div>
            <div class="flex justify-between items-center py-2 border-b border-gray-200 dark:border-gray-700">
              <span class="text-gray-600 dark:text-gray-400">{{ $t('admin.bank.accountName') }}:</span>
              <span class="font-semibold text-gray-800 dark:text-white">{{ formData.accountName || '-' }}</span>
            </div>
            <div class="flex justify-between items-center py-2 border-b border-gray-200 dark:border-gray-700">
              <span class="text-gray-600 dark:text-gray-400">{{ $t('admin.bank.provider') }}:</span>
              <span class="font-semibold text-gray-800 dark:text-white">{{ formData.provider || '-' }}</span>
            </div>
            <div class="flex justify-between items-center py-2 border-b border-gray-200 dark:border-gray-700">
              <span class="text-gray-600 dark:text-gray-400">{{ $t('admin.bank.apiUrl') }}:</span>
              <span class="font-mono text-sm text-gray-800 dark:text-white">{{ formData.apiUrl || '-' }}</span>
            </div>
            <div class="flex justify-between items-center py-2 border-b border-gray-200 dark:border-gray-700">
              <span class="text-gray-600 dark:text-gray-400">{{ $t('admin.bank.apiKey') }}:</span>
              <span class="font-mono text-sm text-gray-800 dark:text-white">{{ formData.apiKey ? '••••••••' : '-' }}</span>
            </div>
            <div class="flex justify-between items-center py-2 border-b border-gray-200 dark:border-gray-700">
              <span class="text-gray-600 dark:text-gray-400">{{ $t('admin.bank.timeout') }}:</span>
              <span class="font-semibold text-gray-800 dark:text-white">{{ formData.timeout ? formData.timeout + 'ms' : '-' }}</span>
            </div>
            <div class="flex justify-between items-center py-2 border-b border-gray-200 dark:border-gray-700">
              <span class="text-gray-600 dark:text-gray-400">{{ $t('admin.bank.retryAttempts') }}:</span>
              <span class="font-semibold text-gray-800 dark:text-white">{{ formData.retryAttempts || '-' }}</span>
            </div>
            <div class="flex justify-between items-center py-2">
              <span class="text-gray-600 dark:text-gray-400">{{ $t('admin.bank.retryDelay') }}:</span>
              <span class="font-semibold text-gray-800 dark:text-white">{{ formData.retryDelay ? formData.retryDelay + 'ms' : '-' }}</span>
            </div>
          </div>
        </div>

        <!-- Warning Notice -->
        <div class="mt-6 p-4 bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 rounded-lg">
          <div class="flex items-start">
            <Icon icon="mdi:alert" class="text-yellow-600 dark:text-yellow-400 mr-2 mt-0.5" />
            <div class="text-sm text-yellow-800 dark:text-yellow-200">
              <p class="font-semibold mb-1">{{ $t('admin.bank.warningTitle') }}</p>
              <ul class="list-disc list-inside space-y-1">
                <li>{{ $t('admin.bank.warning1') }}</li>
                <li>{{ $t('admin.bank.warning2') }}</li>
                <li>{{ $t('admin.bank.warning3') }}</li>
              </ul>
            </div>
          </div>
        </div>

        <div class="flex justify-end space-x-3 pt-4">
          <button
            type="button"
            @click="resetForm"
            class="inline-flex items-center px-4 py-2 bg-gray-300 dark:bg-gray-600 text-gray-700 dark:text-gray-300 rounded hover:bg-gray-400 dark:hover:bg-gray-500"
          >
            <Icon icon="mdi:refresh" class="mr-2" />
            {{ $t('admin.bank.reset') }}
          </button>
          <button
            type="submit"
            :disabled="saving"
            class="inline-flex items-center px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
          >
            <span v-if="saving" class="inline-flex items-center">
              <Icon icon="eos-icons:loading" class="animate-spin mr-2" />
              {{ $t('admin.bank.saving') }}
            </span>
            <span v-else class="inline-flex items-center">
              <Icon icon="mdi:content-save" class="mr-2" />
              {{ $t('admin.bank.save') }}
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
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'

export default {
  name: 'BankAccountManagement',
  components: {
    Icon
  },
  setup() {
    const { t } = useI18n()
    const loading = ref(false)
    const saving = ref(false)
    const message = ref('')
    const messageType = ref('success')
    const showApiKey = ref(false)
    const sections = ref({
      bankAccount: true,
      apiConfig: false
    })
    const formData = ref({
      bankName: '',
      accountNumber: '',
      accountName: '',
      provider: 'mock',
      apiUrl: '',
      apiKey: '',
      timeout: 30000,
      retryAttempts: 3,
      retryDelay: 1000
    })
    const originalData = ref({
      bankName: '',
      accountNumber: '',
      accountName: '',
      provider: 'mock',
      apiUrl: '',
      apiKey: '',
      timeout: 30000,
      retryAttempts: 3,
      retryDelay: 1000
    })

    const loadBankInfo = async () => {
      loading.value = true
      try {
        const response = await paymentAPI.getBankInfo()
        formData.value = {
          bankName: response.data.bankName || '',
          accountNumber: response.data.accountNumber || '',
          accountName: response.data.accountName || '',
          provider: response.data.provider || 'mock',
          apiUrl: response.data.apiUrl || '',
          apiKey: response.data.apiKey || '',
          timeout: response.data.timeout || 30000,
          retryAttempts: response.data.retryAttempts || 3,
          retryDelay: response.data.retryDelay || 1000
        }
        originalData.value = { ...formData.value }
      } catch (error) {
        console.error('Error loading bank info:', error)
        setMessage(t('admin.bank.loading') + ': ' + (error.message || 'Unknown error'), 'error')
      } finally {
        loading.value = false
      }
    }

    const saveBankInfo = async () => {
      saving.value = true
      try {
        await paymentAPI.updateBankInfo(formData.value)
        setMessage(t('dashboard.alerts.success'), 'success')
        originalData.value = { ...formData.value }
      } catch (error) {
        console.error('Error saving bank info:', error)
        setMessage(t('dashboard.alerts.error') + ': ' + (error.response?.data?.message || error.message || 'Unknown error'), 'error')
      } finally {
        saving.value = false
      }
    }

    const resetForm = () => {
      formData.value = { ...originalData.value }
      setMessage(t('admin.bank.reset'), 'info')
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

    const toggleSection = (sectionName) => {
      sections.value[sectionName] = !sections.value[sectionName]
    }

    onMounted(() => {
      loadBankInfo()
    })

    return {
      t,
      loading,
      saving,
      message,
      messageType,
      showApiKey,
      sections,
      formData,
      saveBankInfo,
      resetForm,
      getMessageClass,
      toggleSection
    }
  }
}
</script>
