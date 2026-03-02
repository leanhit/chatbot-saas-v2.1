<template>
  <div class="space-y-6">
    <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-6">
      <div class="flex justify-between items-center mb-6">
        <h3 class="text-lg font-medium text-gray-900 dark:text-white">Basic Information</h3>
        <button
          @click="$emit('edit')"
          :disabled="loading"
          class="inline-flex items-center px-3 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary dark:bg-gray-600 dark:text-gray-300 dark:border-gray-500 dark:hover:bg-gray-500 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <Icon v-if="!loading" icon="mdi:pencil" class="h-4 w-4 mr-2" />
          <Icon v-else icon="mdi:loading" class="h-4 w-4 mr-2 animate-spin" />
          {{ loading ? 'Saving...' : 'Edit' }}
        </button>
      </div>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Tenant Name</label>
          <p class="text-gray-900 dark:text-white">{{ tenant?.name || 'Not provided' }}</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Tenant Key</label>
          <p class="text-gray-900 dark:text-white font-mono text-sm">{{ tenant?.tenantKey || 'Not provided' }}</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Status</label>
          <span :class="getStatusBadgeClass()">
            {{ getStatusLabel() }}
          </span>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Visibility</label>
          <span :class="getVisibilityBadgeClass()">
            {{ getVisibilityLabel() }}
          </span>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Expires At</label>
          <p class="text-gray-900 dark:text-white">{{ formatDate(tenant?.expiresAt) || 'Not provided' }}</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Created At</label>
          <p class="text-gray-900 dark:text-white">{{ formatDate(tenant?.createdAt) || 'Not provided' }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Icon } from '@iconify/vue'
import { formatDate } from '@/utils/dateUtils'

export default {
  name: 'BasicInfoTab',
  components: {
    Icon
  },
  props: {
    tenant: {
      type: Object,
      default: () => ({})
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  emits: ['edit'],
  setup(props) {
    const getStatusBadgeClass = () => {
      const status = props.tenant?.status
      switch (status) {
        case 'ACTIVE':
          return 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
        case 'INACTIVE':
          return 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
        case 'PENDING':
          return 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200'
        default:
          return 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
      }
    }
    
    const getStatusLabel = () => {
      const status = props.tenant?.status
      switch (status) {
        case 'ACTIVE': return 'Active'
        case 'INACTIVE': return 'Inactive'
        case 'PENDING': return 'Pending'
        default: return 'Unknown'
      }
    }
    
    const getVisibilityBadgeClass = () => {
      const visibility = props.tenant?.visibility
      switch (visibility) {
        case 'PUBLIC':
          return 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200'
        case 'PRIVATE':
          return 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
        default:
          return 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
      }
    }
    
    const getVisibilityLabel = () => {
      const visibility = props.tenant?.visibility
      switch (visibility) {
        case 'PUBLIC': return 'Public'
        case 'PRIVATE': return 'Private'
        default: return 'Unknown'
      }
    }
    
    return {
      getStatusBadgeClass,
      getStatusLabel,
      getVisibilityBadgeClass,
      getVisibilityLabel,
      formatDate
    }
  }
}
</script>
