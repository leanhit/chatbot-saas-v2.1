<template>
  <div v-if="visible" class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
    <div class="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white dark:bg-gray-800">
      <!-- Header -->
      <div class="mb-6">
        <h3 class="text-lg font-medium text-gray-900 dark:text-white">
          {{ $t('profile.editProfessional') }}
        </h3>
        <p class="mt-1 text-sm text-gray-600 dark:text-gray-400">
          {{ $t('profile.updateProfessional') }}
        </p>
      </div>

      <!-- Form -->
      <form @submit.prevent="handleSubmit" class="space-y-4">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              Job Title
            </label>
            <input
              v-model="form.jobTitle"
              type="text"
              class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white"
              placeholder="Enter job title"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              Company
            </label>
            <input
              v-model="form.company"
              type="text"
              class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white"
              placeholder="Enter company name"
            />
          </div>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Skills
          </label>
          <input
            v-model="form.skills"
            type="text"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white"
            placeholder="Enter your skills (comma separated)"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            LinkedIn
          </label>
          <input
            v-model="form.linkedinUrl"
            type="url"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white"
            placeholder="https://linkedin.com/in/yourprofile"
          />
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
  name: 'UserProfessionalModalSimple',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    userData: {
      type: Object,
      default: () => ({})
    }
  },
  emits: ['close', 'submit'],
  setup(props, { emit }) {
    const { t } = useI18n()
    const loading = ref(false)

    const form = ref({
      jobTitle: '',
      company: '',
      skills: '',
      linkedinUrl: ''
    })

    // Initialize form with user data
    watch(() => props.userData, (newData) => {
      if (newData) {
        form.value = {
          jobTitle: newData.jobTitle || '',
          company: newData.company || '',
          skills: newData.skills || '',
          linkedinUrl: newData.linkedinUrl || ''
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
