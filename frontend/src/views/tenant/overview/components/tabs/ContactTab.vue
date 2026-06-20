<template>
  <div class="space-y-6">
    <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-6">
      <div class="flex justify-between items-center mb-6">
        <h3 class="text-lg font-medium text-gray-900 dark:text-white">{{ $t('tenant.overview.profileInfo') }}</h3>
        <button
          v-if="canEdit"
          @click="$emit('edit')"
          :disabled="loading"
          class="inline-flex items-center px-3 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary dark:bg-gray-600 dark:text-gray-300 dark:border-gray-500 dark:hover:bg-gray-500 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <Icon v-if="!loading" icon="mdi:pencil" class="h-4 w-4 mr-2" />
          <Icon v-else icon="mdi:loading" class="h-4 w-4 mr-2 animate-spin" />
          {{ loading ? $t('tenant.settings.saving') : $t('common.edit') || 'Edit' }}
        </button>
      </div>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('tenant.overview.description') }}</label>
          <p class="text-gray-900 dark:text-white">{{ tenant?.profile?.description || $t('tenant.overview.notProvided') }}</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('tenant.overview.industry') }}</label>
          <p class="text-gray-900 dark:text-white">{{ tenant?.profile?.industry || $t('tenant.overview.notProvided') }}</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('tenant.overview.plan') }}</label>
          <p class="text-gray-900 dark:text-white">{{ tenant?.profile?.plan || $t('tenant.overview.notProvided') }}</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('tenant.overview.companySize') }}</label>
          <p class="text-gray-900 dark:text-white">{{ tenant?.profile?.companySize || $t('tenant.overview.notProvided') }}</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('tenant.overview.legalName') }}</label>
          <p class="text-gray-900 dark:text-white">{{ tenant?.profile?.legalName || $t('tenant.overview.notProvided') }}</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('tenant.overview.taxCode') }}</label>
          <p class="text-gray-900 dark:text-white">{{ tenant?.profile?.taxCode || $t('tenant.overview.notProvided') }}</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('tenant.overview.contactEmail') }}</label>
          <p class="text-gray-900 dark:text-white">{{ tenant?.profile?.contactEmail || $t('tenant.overview.notProvided') }}</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('tenant.overview.contactPhone') }}</label>
          <p class="text-gray-900 dark:text-white">{{ tenant?.profile?.contactPhone || $t('tenant.overview.notProvided') }}</p>
        </div>
        <div class="md:col-span-2">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('tenant.overview.logoUrl') }}</label>
          <p class="text-gray-900 dark:text-white break-all">{{ tenant?.profile?.logoUrl || $t('tenant.overview.notProvided') }}</p>
        </div>
        <div class="md:col-span-2">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('tenant.overview.faviconUrl') }}</label>
          <p class="text-gray-900 dark:text-white break-all">{{ tenant?.profile?.faviconUrl || $t('tenant.overview.notProvided') }}</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ $t('tenant.overview.primaryColor') }}</label>
          <div class="flex items-center space-x-2">
            <div 
              v-if="tenant?.profile?.primaryColor" 
              class="w-6 h-6 rounded border border-gray-300"
              :style="{ backgroundColor: tenant.profile.primaryColor }"
            ></div>
            <p class="text-gray-900 dark:text-white">{{ tenant?.profile?.primaryColor || $t('tenant.overview.notProvided') }}</p>
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
    },
    canEdit: {
      type: Boolean,
      default: false
    }
  },
  emits: ['edit']
}
</script>
