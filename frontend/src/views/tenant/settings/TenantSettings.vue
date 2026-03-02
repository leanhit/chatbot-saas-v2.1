<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 py-8">
    <!-- Loading Overlay -->
    <div v-if="loading" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 flex items-center space-x-3">
        <Icon icon="mdi:loading" class="h-6 w-6 animate-spin text-primary" />
        <span class="text-gray-900 dark:text-white">Loading...</span>
      </div>
    </div>
    
    <!-- Main Content -->
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900 dark:text-white">Cài đặt Tenant</h1>
        <p class="mt-2 text-gray-600 dark:text-gray-400">Quản lý thông tin và cấu hình tenant của bạn</p>
      </div>
      
      <div class="space-y-6">
        <!-- Cài đặt chung -->
        <div class="bg-white dark:bg-gray-800 shadow rounded-lg">
          <div class="p-6 border-b border-gray-200 dark:border-gray-700">
            <h2 class="text-lg font-medium text-gray-900 dark:text-white">Cài đặt chung</h2>
          </div>
          <div class="p-6 space-y-6">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Tên Tenant</label>
                <input
                  v-model="settings.name"
                  type="text"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Trạng thái</label>
                <select
                  v-model="settings.status"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                >
                  <option value="ACTIVE">Hoạt động</option>
                  <option value="INACTIVE">Không hoạt động</option>
                  <option value="SUSPENDED">Tạm dừng</option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Hiển thị</label>
                <select
                  v-model="settings.visibility"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                >
                  <option value="PUBLIC">Công khai</option>
                  <option value="PRIVATE">Riêng tư</option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Hết hạn</label>
                <input
                  v-model="settings.expiresAt"
                  type="datetime-local"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                />
              </div>
            </div>
            <div class="flex justify-end">
              <button
                @click="saveSettings"
                :disabled="loading"
                class="px-4 py-2 text-white bg-primary rounded-md hover:bg-primary/80 disabled:opacity-50"
              >
                <Icon v-if="!loading" icon="mdi:content-save" class="h-4 w-4 mr-2" />
                <Icon v-else icon="mdi:loading" class="h-4 w-4 mr-2 animate-spin" />
                {{ loading ? 'Đang lưu...' : 'Lưu cài đặt' }}
              </button>
            </div>
          </div>
        </div>

        <!-- Quản lý trạng thái -->
        <div class="bg-white dark:bg-gray-800 shadow rounded-lg">
          <div class="p-6 border-b border-gray-200 dark:border-gray-700">
            <h2 class="text-lg font-medium text-gray-900 dark:text-white">Quản lý trạng thái</h2>
          </div>
          <div class="p-6">
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <button
                @click="suspendTenant"
                :disabled="loading || settings.status === 'SUSPENDED'"
                class="px-4 py-2 text-yellow-700 bg-yellow-100 rounded-md hover:bg-yellow-200 dark:bg-yellow-900 dark:text-yellow-200 disabled:opacity-50"
              >
                <Icon icon="mdi:pause" class="h-4 w-4 mr-2" />
                Tạm dừng
              </button>
              <button
                @click="activateTenant"
                :disabled="loading || settings.status === 'ACTIVE'"
                class="px-4 py-2 text-green-700 bg-green-100 rounded-md hover:bg-green-200 dark:bg-green-900 dark:text-green-200 disabled:opacity-50"
              >
                <Icon icon="mdi:play" class="h-4 w-4 mr-2" />
                Kích hoạt
              </button>
              <button
                @click="deactivateTenant"
                :disabled="loading || settings.status === 'INACTIVE'"
                class="px-4 py-2 text-red-700 bg-red-100 rounded-md hover:bg-red-200 dark:bg-red-900 dark:text-red-200 disabled:opacity-50"
              >
                <Icon icon="mdi:stop" class="h-4 w-4 mr-2" />
                Vô hiệu hóa
              </button>
            </div>
          </div>
        </div>

        <!-- Thông tin tenant -->
        <div class="bg-white dark:bg-gray-800 shadow rounded-lg">
          <div class="p-6 border-b border-gray-200 dark:border-gray-700">
            <h2 class="text-lg font-medium text-gray-900 dark:text-white">Thông tin Tenant</h2>
          </div>
          <div class="p-6">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Tenant Key</label>
                <div class="flex items-center">
                  <input
                    :value="settings.tenantKey"
                    type="text"
                    readonly
                    class="flex-1 px-3 py-2 border border-gray-300 rounded-md bg-gray-50 dark:bg-gray-700 dark:border-gray-600 dark:text-gray-400"
                  />
                  <button
                    @click="copyTenantKey"
                    class="ml-2 p-2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
                    title="Sao chép"
                  >
                    <Icon icon="mdi:content-copy" class="h-4 w-4" />
                  </button>
                </div>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Ngày tạo</label>
                <input
                  :value="formatDate(settings.createdAt)"
                  type="text"
                  readonly
                  class="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50 dark:bg-gray-700 dark:border-gray-600 dark:text-gray-400"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Ngày cập nhật</label>
                <input
                  :value="formatDate(settings.updatedAt)"
                  type="text"
                  readonly
                  class="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50 dark:bg-gray-700 dark:border-gray-600 dark:text-gray-400"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Số lượng thành viên</label>
                <input
                  :value="memberCount"
                  type="text"
                  readonly
                  class="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50 dark:bg-gray-700 dark:border-gray-600 dark:text-gray-400"
                />
              </div>
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
    
    // Reactive state
    const loading = ref(false)
    const memberCount = ref(0)
    
    const settings = ref({
      name: '',
      status: 'ACTIVE',
      visibility: 'PUBLIC',
      expiresAt: '',
      tenantKey: '',
      createdAt: '',
      updatedAt: ''
    })
    
    // Methods
    const loadTenantData = async () => {
      try {
        loading.value = true
        const tenant = tenantStore.tenant
        
        if (tenant) {
          settings.value = {
            name: tenant.name || '',
            status: tenant.status || 'ACTIVE',
            visibility: tenant.visibility || 'PUBLIC',
            expiresAt: formatDateTimeLocal(tenant.expiresAt) || '',
            tenantKey: tenant.tenantKey || '',
            createdAt: tenant.createdAt || '',
            updatedAt: tenant.updatedAt || ''
          }
        }
      } catch (error) {
        toast?.error('Lỗi khi tải dữ liệu tenant')
      } finally {
        loading.value = false
      }
    }
    
    const saveSettings = async () => {
      try {
        loading.value = true
        
        const updateData = {
          name: settings.value.name,
          status: settings.value.status,
          visibility: settings.value.visibility,
          expiresAt: dateTimeLocalToIso(settings.value.expiresAt)
        }
        
        const response = await tenantApi.updateTenantBasicInfo(tenantStore.activeTenantId, updateData)
        
        // Update local data
        if (response.data) {
          Object.keys(response.data).forEach(key => {
            if (key !== 'expiresAt') {
              settings.value[key] = response.data[key]
            }
          })
        }
        
        toast?.success('Cài đặt đã được lưu thành công')
      } catch (error) {
        toast?.error('Lỗi khi lưu cài đặt')
      } finally {
        loading.value = false
      }
    }
    
    const suspendTenant = async () => {
      try {
        loading.value = true
        await tenantApi.suspendTenant(tenantStore.activeTenantId)
        settings.value.status = 'SUSPENDED'
        toast?.success('Tenant đã được tạm dừng')
      } catch (error) {
        toast?.error('Lỗi khi tạm dừng tenant')
      } finally {
        loading.value = false
      }
    }
    
    const activateTenant = async () => {
      try {
        loading.value = true
        await tenantApi.activateTenant(tenantStore.activeTenantId)
        settings.value.status = 'ACTIVE'
        toast?.success('Tenant đã được kích hoạt')
      } catch (error) {
        toast?.error('Lỗi khi kích hoạt tenant')
      } finally {
        loading.value = false
      }
    }
    
    const deactivateTenant = async () => {
      try {
        loading.value = true
        await tenantApi.deactivateTenant(tenantStore.activeTenantId)
        settings.value.status = 'INACTIVE'
        toast?.success('Tenant đã được vô hiệu hóa')
      } catch (error) {
        toast?.error('Lỗi khi vô hiệu hóa tenant')
      } finally {
        loading.value = false
      }
    }
    
    const copyTenantKey = () => {
      navigator.clipboard.writeText(settings.value.tenantKey)
      toast?.success('Tenant Key đã được sao chép')
    }
    
    // Load data on mount
    onMounted(async () => {
      await loadTenantData()
      // TODO: Load member count from API
      memberCount.value = Math.floor(Math.random() * 50) + 10 // Mock data
    })
    
    return {
      loading,
      settings,
      memberCount,
      saveSettings,
      suspendTenant,
      activateTenant,
      deactivateTenant,
      copyTenantKey,
      formatDate
    }
  }
}
</script>
