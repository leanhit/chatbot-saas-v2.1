<template>
  <div v-if="show" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-md max-h-[90vh] overflow-y-auto border border-gray-200 dark:border-gray-700">
      <div class="flex justify-between items-center mb-4">
        <h3 class="text-lg font-medium text-gray-900 dark:text-white">{{ $t('tenant.overview.editBasicInfo') }}</h3>
        <button
          @click="$emit('close')"
          class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
        >
          <Icon icon="mdi:close" class="h-6 w-6" />
        </button>
      </div>
      <form @submit.prevent="handleSubmit" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.tenantName') }}</label>
          <input
            v-model="form.name"
            type="text"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary dark:bg-gray-700 dark:border-gray-600 dark:text-white"
            required
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.status') }}</label>
          <select
            v-model="form.status"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary dark:bg-gray-700 dark:border-gray-600 dark:text-white"
          >
            <option value="ACTIVE">{{ $t('tenant.overview.statusActive') }}</option>
            <option value="INACTIVE">{{ $t('tenant.overview.statusInactive') }}</option>
            <option value="PENDING">{{ $t('tenant.overview.statusPending') }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.visibility') }}</label>
          <select
            v-model="form.visibility"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary dark:bg-gray-700 dark:border-gray-600 dark:text-white"
          >
            <option value="PUBLIC">{{ $t('tenant.settings.public') || 'Public' }}</option>
            <option value="PRIVATE">{{ $t('tenant.settings.private') || 'Private' }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.expiresAt') }}</label>
          <input
            v-model="form.expiresAt"
            type="datetime-local"
            disabled
            class="w-full px-3 py-2 border border-gray-200 rounded-md bg-gray-100 dark:bg-gray-700 dark:border-gray-600 dark:text-gray-400 text-gray-500 cursor-not-allowed"
          />
          <p class="mt-1 text-xs text-gray-400 dark:text-gray-500">
            <Icon icon="mdi:information-outline" class="inline mr-1" />
            {{ $t('tenant.overview.expiresAtNote') || 'Managed automatically by the payment system' }}
          </p>
        </div>
        <div class="flex justify-end space-x-3 pt-4">
          <button
            type="button"
            @click="$emit('close')"
            class="px-4 py-2 text-gray-700 bg-gray-200 rounded-md hover:bg-gray-300 dark:bg-gray-600 dark:text-gray-300 dark:hover:bg-gray-500"
          >
            {{ $t('common.cancel') }}
          </button>
          <button
            type="submit"
            :disabled="loading"
            class="px-4 py-2 text-white bg-primary rounded-md hover:bg-primary/80 disabled:opacity-50"
          >
            {{ loading ? $t('tenant.settings.saving') : $t('common.save') || 'Save' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { ref, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { formatDateTimeLocal } from '@/utils/dateUtils'

export default {
  name: 'BasicInfoModal',
  components: {
    Icon
  },
  props: {
    show: {
      type: Boolean,
      default: false
    },
    tenant: {
      type: Object,
      default: () => ({})
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  emits: ['close', 'submit'],
  setup(props, { emit }) {
    const form = ref({
      name: '',
      status: 'ACTIVE',
      visibility: 'PUBLIC',
      expiresAt: ''
    })
    
    // Watch for tenant changes to populate form
    watch(() => props.tenant, (newTenant) => {
      if (newTenant) {
        form.value = {
          name: newTenant?.name || '',
          status: newTenant?.status || 'ACTIVE',
          visibility: newTenant?.visibility || 'PUBLIC',
          expiresAt: formatDateTimeLocal(newTenant?.expiresAt)
        }
      }
    }, { immediate: true })
    
    const handleSubmit = () => {
      emit('submit', { ...form.value })
    }
    
    return {
      form,
      handleSubmit
    }
  }
}
</script>
