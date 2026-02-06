<template>
  <div class="bg-white dark:bg-gray-800 shadow rounded-lg p-6">
    <div class="flex justify-between items-center mb-6">
      <h3 class="text-lg font-medium text-gray-900 dark:text-white">{{ $t('tenant.overview.basicInfo') }}</h3>
      <button
        @click="handleEdit"
        class="inline-flex items-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md text-white bg-primary hover:bg-primary/80 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary"
      >
        <Icon icon="mdi:pencil" class="h-4 w-4 mr-1" />
        {{ $t('tenant.overview.editBasicInfo') }}
      </button>
    </div>

    <div v-if="loading" class="text-center py-8">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">{{ $t('tenant.overview.loading') }}</p>
    </div>

    <div v-else-if="!basicInfo" class="text-center py-8">
      <Icon icon="mdi:office-building" class="mx-auto h-12 w-12 text-gray-400" />
      <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">{{ $t('tenant.overview.noData') }}</p>
      <button
        @click="handleCreate"
        class="mt-4 inline-flex items-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md text-white bg-primary hover:bg-primary/80 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary"
      >
        <Icon icon="mdi:plus" class="h-4 w-4 mr-1" />
        {{ $t('tenant.overview.addBasicInfo') }}
      </button>
    </div>

    <div v-else class="space-y-4">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div v-if="basicInfo.name">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.tenantName') }}</label>
          <p class="text-sm text-gray-900 dark:text-white">{{ basicInfo.name }}</p>
        </div>
        
        <div v-if="basicInfo.tenantKey">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.tenantKey') }}</label>
          <p class="text-sm text-gray-900 dark:text-white font-mono">{{ basicInfo.tenantKey }}</p>
        </div>
        
        <div v-if="basicInfo.industry">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.industry') }}</label>
          <p class="text-sm text-gray-900 dark:text-white">{{ basicInfo.industry }}</p>
        </div>
        
        <div v-if="basicInfo.size">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.companySize') }}</label>
          <p class="text-sm text-gray-900 dark:text-white">{{ getCompanySizeLabel(basicInfo.size) }}</p>
        </div>
        
        <div v-if="basicInfo.contactEmail">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.contactEmail') }}</label>
          <p class="text-sm text-gray-900 dark:text-white">{{ basicInfo.contactEmail }}</p>
        </div>
        
        <div v-if="basicInfo.contactPhone">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.contactPhone') }}</label>
          <p class="text-sm text-gray-900 dark:text-white">{{ basicInfo.contactPhone }}</p>
        </div>
        
        <div v-if="basicInfo.website">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.website') }}</label>
          <a :href="basicInfo.website" target="_blank" class="text-sm text-blue-600 hover:text-blue-800 dark:text-blue-400 dark:hover:text-blue-300">
            {{ basicInfo.website }}
          </a>
        </div>
        
        <div v-if="basicInfo.status">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.status') }}</label>
          <span :class="getStatusBadgeClass(basicInfo.status)">
            {{ getStatusLabel(basicInfo.status) }}
          </span>
        </div>
      </div>
      
      <div v-if="basicInfo.description" class="mt-6">
        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.description') }}</label>
        <p class="text-sm text-gray-900 dark:text-white whitespace-pre-wrap">{{ basicInfo.description }}</p>
      </div>
      
      <div v-if="basicInfo.address" class="mt-6">
        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.address') }}</label>
        <p class="text-sm text-gray-900 dark:text-white">{{ basicInfo.address }}</p>
      </div>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mt-6">
        <div v-if="basicInfo.createdAt">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.createdAt') }}</label>
          <p class="text-sm text-gray-900 dark:text-white">{{ formatDate(basicInfo.createdAt) }}</p>
        </div>
        
        <div v-if="basicInfo.expiresAt">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.expiresAt') }}</label>
          <p class="text-sm text-gray-900 dark:text-white">{{ formatDate(basicInfo.expiresAt) }}</p>
        </div>
      </div>
    </div>

    <!-- Edit Modal -->
    <TenantBasicInfoForm
      v-if="showEditModal"
      :visible="showEditModal"
      :tenant-key="tenantKey"
      :basic-info="basicInfo"
      @close="showEditModal = false"
      @updated="handleUpdated"
    />
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import { tenantApi } from '@/api/tenantApi'
import TenantBasicInfoForm from './TenantBasicInfoForm.vue'

export default {
  name: 'TenantBasicInfo',
  components: {
    Icon,
    TenantBasicInfoForm
  },
  props: {
    tenantKey: {
      type: String,
      required: true
    }
  },
  emits: ['updated'],
  setup(props, { emit }) {
    const loading = ref(false)
    const basicInfo = ref(null)
    const showEditModal = ref(false)
    const isEdit = ref(false)

    const loadBasicInfo = async () => {
      loading.value = true
      try {
        const response = await tenantApi.getTenantDetail(props.tenantKey)
        basicInfo.value = response.data
      } catch (error) {
        console.error('Error loading tenant basic info:', error)
      } finally {
        loading.value = false
      }
    }

    const handleEdit = () => {
      isEdit.value = true
      showEditModal.value = true
    }

    const handleCreate = () => {
      isEdit.value = false
      showEditModal.value = true
    }

    const handleSaved = () => {
      showEditModal.value = false
      loadBasicInfo()
      emit('updated')
    }

    const handleSubmit = async (formData) => {
      loading.value = true
      try {
        // Use new tenant API to update basic info
        const response = await tenantApi.updateTenantBasicInfo(props.tenantKey, {
          name: formData.companyName,
          description: formData.description,
          // Map other fields as needed
          contactEmail: formData.email,
          contactPhone: formData.phone,
          address: formData.address
        })
        
        console.log('Tenant basic info updated:', response.data)
        
        // Close modal and refresh data
        handleSaved()
      } catch (error) {
        console.error('Error updating tenant basic info:', error)
        alert('Failed to update tenant information. Please try again.')
      } finally {
        loading.value = false
      }
    }

    const getStatusBadgeClass = (status) => {
      switch (status) {
        case 'ACTIVE':
          return 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
        case 'INACTIVE':
          return 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
        case 'SUSPENDED':
          return 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200'
        default:
          return 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
      }
    }

    const getStatusLabel = (status) => {
      switch (status) {
        case 'ACTIVE': return 'Active'
        case 'INACTIVE': return 'Inactive'
        case 'SUSPENDED': return 'Suspended'
        default: return 'Unknown'
      }
    }

    const getCompanySizeLabel = (size) => {
      switch (size) {
        case 'SMALL': return 'Small (1-50)'
        case 'MEDIUM': return 'Medium (51-200)'
        case 'LARGE': return 'Large (201-1000)'
        case 'ENTERPRISE': return 'Enterprise (1000+)'
        default: return size
      }
    }

    const formatDate = (dateString) => {
      if (!dateString) return 'N/A'
      return new Date(dateString).toLocaleDateString()
    }

    onMounted(() => {
      loadBasicInfo()
    })

    return {
      loading,
      basicInfo,
      showEditModal,
      isEdit,
      handleEdit,
      handleCreate,
      handleSaved,
      handleSubmit,
      getStatusBadgeClass,
      getStatusLabel,
      getCompanySizeLabel,
      formatDate
    }
  }
}
</script>
