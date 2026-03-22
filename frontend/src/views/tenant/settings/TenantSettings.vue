<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 py-8">
    <!-- Loading Overlay -->
    <div v-if="loading" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 flex items-center space-x-3">
        <Icon icon="mdi:loading" class="h-6 w-6 animate-spin text-primary" />
        <span class="text-gray-900 dark:text-white">{{ $t('tenant.settings.loading') }}</span>
      </div>
    </div>
    
    <!-- Main Content -->
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900 dark:text-white">{{ $t('tenant.settings.title') }}</h1>
        <p class="mt-2 text-gray-600 dark:text-gray-400">{{ $t('tenant.settings.subtitle') }}</p>
      </div>
      
      <div class="space-y-6">
        <!-- Currency Settings -->
        <div class="bg-white dark:bg-gray-800 shadow rounded-lg">
          <div class="p-6 border-b border-gray-200 dark:border-gray-700">
            <h2 class="text-lg font-medium text-gray-900 dark:text-white">{{ $t('tenant.settings.currencySettings') }}</h2>
            <p class="mt-1 text-sm text-gray-600 dark:text-gray-400">{{ $t('tenant.settings.currencySettingsSubtitle') }}</p>
          </div>
          <div class="p-6 space-y-6">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('tenant.settings.defaultCurrency') }}</label>
                <select
                  v-model="settings.defaultCurrency"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                >
                  <option value="USD">USD - US Dollar ($)</option>
                  <option value="VND">VND - Vietnamese Đồng (₫)</option>
                  <option value="EUR">EUR - Euro (€)</option>
                  <option value="GBP">GBP - British Pound (£)</option>
                  <option value="JPY">JPY - Japanese Yen (¥)</option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('tenant.settings.displayCurrency') }}</label>
                <select
                  v-model="settings.displayCurrency"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                >
                  <option value="USD">USD - US Dollar ($)</option>
                  <option value="VND">VND - Vietnamese Đồng (₫)</option>
                  <option value="EUR">EUR - Euro (€)</option>
                  <option value="GBP">GBP - British Pound (£)</option>
                  <option value="JPY">JPY - Japanese Yen (¥)</option>
                </select>
              </div>
            </div>
            
            <!-- Currency Conversion Info -->
            <div class="bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg p-4">
              <div class="flex items-start">
                <Icon icon="mdi:information" class="h-5 w-5 text-blue-600 dark:text-blue-400 mt-0.5 mr-3" />
                <div class="text-sm text-blue-800 dark:text-blue-200">
                  <p class="font-medium mb-1">{{ $t('tenant.settings.currencyInfo') }}</p>
                  <ul class="list-disc list-inside space-y-1 text-blue-700 dark:text-blue-300">
                    <li>{{ $t('tenant.settings.currencyInfo1') }}</li>
                    <li>{{ $t('tenant.settings.currencyInfo2') }}</li>
                    <li>{{ $t('tenant.settings.currencyInfo3') }}</li>
                  </ul>
                </div>
              </div>
            </div>
            
            <!-- Save Button -->
            <div class="flex justify-end">
              <button
                @click="saveSettings"
                :disabled="loading"
                class="px-4 py-2 text-white bg-primary rounded-md hover:bg-primary/80 disabled:opacity-50"
              >
                <Icon v-if="!loading" icon="mdi:content-save" class="h-4 w-4 mr-2" />
                <Icon v-else icon="mdi:loading" class="h-4 w-4 mr-2 animate-spin" />
                {{ loading ? $t('tenant.settings.saving') : $t('tenant.settings.saveSettings') }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import { useRouter } from 'vue-router'
import { useTenantAdminContextStore } from '@/stores/tenant/admin/tenantContextStore'
import { tenantApi } from '@/api/tenantApi'
import { formatDate, formatDateTimeLocal, dateTimeLocalToIso } from '@/utils/dateUtils'
import { getCurrentInstance } from 'vue'
import { useI18n } from 'vue-i18n'

export default {
  name: 'TenantSettings',
  components: {
    Icon
  },
  setup() {
    const router = useRouter()
    const tenantStore = useTenantAdminContextStore()
    const instance = getCurrentInstance()
    const toast = instance?.appContext.config.globalProperties.$toast
    const { t } = useI18n()
    
    // Reactive state
    const loading = ref(false)
    const memberCount = ref(0)
    
    const settings = ref({
      defaultCurrency: 'USD',
      displayCurrency: 'USD'
    })
    
    // Methods
    const loadTenantData = async () => {
      try {
        loading.value = true
        const tenant = tenantStore.tenant
        
        if (tenant) {
          settings.value = {
            defaultCurrency: tenant.defaultCurrency || 'USD',
            displayCurrency: tenant.displayCurrency || 'USD'
          }
        }
      } catch (error) {
        toast?.error(t('tenant.settings.error.loadDataError'))
      } finally {
        loading.value = false
      }
    }
    
    const saveSettings = async () => {
      try {
        loading.value = true
        
        const updateData = {
          defaultCurrency: settings.value.defaultCurrency,
          displayCurrency: settings.value.displayCurrency
        }
        
        const response = await tenantApi.updateTenant(tenantStore.activeTenantId, updateData)
        
        // Update local data
        if (response.data) {
          Object.keys(response.data).forEach(key => {
            if (key !== 'expiresAt') {
              settings.value[key] = response.data[key]
            }
          })
        }
        
        toast?.success(t('tenant.settings.success.settingsSaved'))
      } catch (error) {
        toast?.error(t('tenant.settings.error.saveSettingsError'))
      } finally {
        loading.value = false
      }
    }
    
    const suspendTenant = async () => {
      try {
        loading.value = true
        await tenantApi.suspendTenant(tenantStore.activeTenantId)
        settings.value.status = 'SUSPENDED'
        toast?.success(t('tenant.settings.success.tenantSuspended'))
      } catch (error) {
        toast?.error(t('tenant.settings.error.suspendError'))
      } finally {
        loading.value = false
      }
    }
    
    const activateTenant = async () => {
      try {
        loading.value = true
        await tenantApi.activateTenant(tenantStore.activeTenantId)
        settings.value.status = 'ACTIVE'
        toast?.success(t('tenant.settings.success.tenantActivated'))
      } catch (error) {
        toast?.error(t('tenant.settings.error.activateError'))
      } finally {
        loading.value = false
      }
    }
    
    const deactivateTenant = async () => {
      try {
        loading.value = true
        await tenantApi.deactivateTenant(tenantStore.activeTenantId)
        settings.value.status = 'INACTIVE'
        toast?.success(t('tenant.settings.success.tenantDeactivated'))
      } catch (error) {
        toast?.error(t('tenant.settings.error.deactivateError'))
      } finally {
        loading.value = false
      }
    }
    
    const copyTenantKey = () => {
      navigator.clipboard.writeText(settings.value.tenantKey)
      toast?.success(t('tenant.settings.success.tenantKeyCopied'))
    }
    
    // Load data on mount
    onMounted(async () => {
      await loadTenantData()
      // Load real member count from API
      try {
        const tenantKey = settings.value?.tenantKey || localStorage.getItem('active_tenant_id')
        if (tenantKey) {
          const response = await tenantApi.getTenantMembers(tenantKey)
          memberCount.value = (response.data?.content || response.data || []).length
        }
      } catch (error) {
        console.error('Failed to load member count:', error)
        memberCount.value = 0
      }
    })
    
    return {
      loading,
      settings,
      saveSettings,
      loadTenantData
    }
  }
}
</script>
