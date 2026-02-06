<template>
  <div v-if="visible" class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
    <div class="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white dark:bg-gray-800">
      <!-- Header -->
      <div class="mb-6">
        <h3 class="text-lg font-medium text-gray-900 dark:text-white">
          {{ $t('tenant.overview.editProfessional') }}
        </h3>
        <p class="mt-1 text-sm text-gray-600 dark:text-gray-400">
          {{ $t('tenant.overview.updateProfessional') }}
        </p>
      </div>

      <!-- Form -->
      <form @submit.prevent="handleSubmit" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('tenant.overview.industry') }}
          </label>
          <select
            v-model="form.industry"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white"
          >
            <option value="">{{ $t('tenant.overview.selectIndustry') }}</option>
            <option value="Technology">Technology</option>
            <option value="Healthcare">Healthcare</option>
            <option value="Finance">Finance</option>
            <option value="Education">Education</option>
            <option value="Retail">Retail</option>
            <option value="Manufacturing">Manufacturing</option>
            <option value="Other">Other</option>
          </select>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('tenant.overview.companySize') }}
          </label>
          <select
            v-model="form.companySize"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white"
          >
            <option value="">{{ $t('tenant.overview.selectSize') }}</option>
            <option value="1-10">1-10</option>
            <option value="11-50">11-50</option>
            <option value="51-200">51-200</option>
            <option value="201-500">201-500</option>
            <option value="501-1000">501-1000</option>
            <option value="1000+">1000+</option>
          </select>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('tenant.overview.website') }}
          </label>
          <input
            v-model="form.website"
            type="url"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white"
            :placeholder="$t('tenant.overview.website')"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('tenant.overview.description') }}
          </label>
          <textarea
            v-model="form.description"
            rows="3"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white"
            :placeholder="$t('tenant.overview.description')"
          ></textarea>
        </div>

        <!-- Actions -->
        <div class="flex justify-end space-x-3 pt-4 border-t dark:border-gray-700">
          <button
            type="button"
            @click="handleClose"
            class="px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary dark:bg-gray-700 dark:text-gray-300 dark:border-gray-600 dark:hover:bg-gray-600"
          >
            {{ $t('common.cancel') }}
          </button>
          <button
            type="submit"
            :disabled="loading"
            class="px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-primary hover:bg-primary/80 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary disabled:opacity-50"
          >
            <span v-if="loading">{{ $t('common.loading') }}...</span>
            <span v-else>{{ $t('common.save') }}</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

export default {
  name: 'TenantProfessionalModalSimple',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    tenantData: {
      type: Object,
      default: () => ({})
    }
  },
  emits: ['close', 'submit'],
  setup(props, { emit }) {
    const { t } = useI18n()
    const loading = ref(false)

    const form = ref({
      industry: '',
      companySize: '',
      website: '',
      description: ''
    })

    // Initialize form with tenant data
    watch(() => props.tenantData, (newData) => {
      if (newData) {
        form.value = {
          industry: newData.industry || '',
          companySize: newData.companySize || '',
          website: newData.website || '',
          description: newData.description || ''
        }
      }
    }, { immediate: true })

    const handleSubmit = async () => {
      loading.value = true
      try {
        await emit('submit', form.value)
        emit('close')
      } catch (error) {
        console.error('Error submitting form:', error)
      } finally {
        loading.value = false
      }
    }

    const handleClose = () => {
      emit('close')
    }

    return {
      form,
      loading,
      handleSubmit,
      handleClose
    }
  }
}
</script>
