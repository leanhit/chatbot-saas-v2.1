<template>
  <div class="space-y-6">
    <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-6">
      <div class="flex justify-between items-center mb-6">
        <h3 class="text-lg font-medium text-gray-900 dark:text-white">Profile Information</h3>
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
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Description</label>
          <p class="text-gray-900 dark:text-white">{{ tenant?.profile?.description || 'Not provided' }}</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Industry</label>
          <p class="text-gray-900 dark:text-white">{{ tenant?.profile?.industry || 'Not provided' }}</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Plan</label>
          <p class="text-gray-900 dark:text-white">{{ tenant?.profile?.plan || 'Not provided' }}</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Company Size</label>
          <p class="text-gray-900 dark:text-white">{{ tenant?.profile?.companySize || 'Not provided' }}</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Legal Name</label>
          <p class="text-gray-900 dark:text-white">{{ tenant?.profile?.legalName || 'Not provided' }}</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Tax Code</label>
          <p class="text-gray-900 dark:text-white">{{ tenant?.profile?.taxCode || 'Not provided' }}</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Contact Email</label>
          <p class="text-gray-900 dark:text-white">{{ tenant?.profile?.contactEmail || 'Not provided' }}</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Contact Phone</label>
          <p class="text-gray-900 dark:text-white">{{ tenant?.profile?.contactPhone || 'Not provided' }}</p>
        </div>
        <div class="md:col-span-2">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Logo URL</label>
          <p class="text-gray-900 dark:text-white break-all">{{ tenant?.profile?.logoUrl || 'Not provided' }}</p>
        </div>
        <div class="md:col-span-2">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Favicon URL</label>
          <p class="text-gray-900 dark:text-white break-all">{{ tenant?.profile?.faviconUrl || 'Not provided' }}</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Primary Color</label>
          <div class="flex items-center space-x-2">
            <div 
              v-if="tenant?.profile?.primaryColor" 
              class="w-6 h-6 rounded border border-gray-300"
              :style="{ backgroundColor: tenant.profile.primaryColor }"
            ></div>
            <p class="text-gray-900 dark:text-white">{{ tenant?.profile?.primaryColor || 'Not provided' }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Icon } from '@iconify/vue'

export default {
  name: 'ContactTab',
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
  emits: ['edit']
}
</script>
