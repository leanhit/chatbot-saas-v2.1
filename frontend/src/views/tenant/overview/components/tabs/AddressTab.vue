<template>
  <div class="space-y-6">
    <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-6">
      <div class="flex justify-between items-center mb-6">
        <h3 class="text-lg font-medium text-gray-900 dark:text-white">Address Information</h3>
        <button
          @click="$emit('edit')"
          :disabled="loading || !tenantAddress"
          class="inline-flex items-center px-3 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary dark:bg-gray-600 dark:text-gray-300 dark:border-gray-500 dark:hover:bg-gray-500 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <Icon v-if="!loading" icon="mdi:pencil" class="h-4 w-4 mr-2" />
          <Icon v-else icon="mdi:loading" class="h-4 w-4 mr-2 animate-spin" />
          {{ loading ? 'Saving...' : 'Edit Address' }}
        </button>
      </div>
      <div v-if="tenantAddress" class="space-y-4">
        <!-- First row: 2 columns -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">House Number</label>
            <p class="text-gray-900 dark:text-white">{{ tenantAddress.houseNumber || 'Not provided' }}</p>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Street</label>
            <p class="text-gray-900 dark:text-white">{{ tenantAddress.street || 'Not provided' }}</p>
          </div>
        </div>
        
        <!-- Second row: 2 columns with 4 fields -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Ward</label>
              <p class="text-gray-900 dark:text-white">{{ tenantAddress.ward || 'Not provided' }}</p>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">District</label>
              <p class="text-gray-900 dark:text-white">{{ tenantAddress.district || 'Not provided' }}</p>
            </div>
          </div>
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Province</label>
              <p class="text-gray-900 dark:text-white">{{ tenantAddress.province || 'Not provided' }}</p>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Country</label>
              <p class="text-gray-900 dark:text-white">{{ tenantAddress.country || 'Not provided' }}</p>
            </div>
          </div>
        </div>
        
        <!-- Full width field -->
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Full Address</label>
          <p class="text-gray-900 dark:text-white">{{ tenantAddress.fullAddress || 'Not provided' }}</p>
        </div>
      </div>
      <div v-else class="text-center py-8 text-gray-500 dark:text-gray-400">
        <Icon icon="mdi:map-marker-off" class="h-12 w-12 mx-auto mb-2" />
        <p>No address information available</p>
      </div>
    </div>
  </div>
</template>

<script>
import { Icon } from '@iconify/vue'

export default {
  name: 'AddressTab',
  components: {
    Icon
  },
  props: {
    tenantAddress: {
      type: Object,
      default: null
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  emits: ['edit']
}
</script>
