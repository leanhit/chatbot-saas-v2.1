<template>
  <div v-if="visible" class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
    <div class="relative top-20 mx-auto p-5 border w-[768px] shadow-lg rounded-md bg-white dark:bg-gray-800">
      <!-- Header -->
      <div class="mb-6">
        <h3 class="text-lg font-medium text-gray-900 dark:text-white">
          {{ $t('profile.editBasicInfo') }}
        </h3>
        <p class="mt-1 text-sm text-gray-600 dark:text-gray-400">
          {{ $t('profile.updateBasicInfo') }}
        </p>
      </div>
      <!-- Form -->
      <form @submit.prevent="handleSubmit" class="space-y-4">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <!-- Left Column -->
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                {{ $t('profile.fullName') }}
              </label>
              <input
                v-model="form.fullName"
                type="text"
                required
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white"
                :placeholder="$t('profile.fullName')"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                {{ $t('profile.phoneNumber') }}
              </label>
              <input
                v-model="form.phoneNumber"
                type="tel"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white"
                :placeholder="$t('profile.phoneNumber')"
              />
            </div>
          </div>
          <!-- Right Column -->
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                {{ $t('profile.gender') }}
              </label>
              <select
                v-model="form.gender"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white"
              >
                <option value="">{{ $t('profile.selectGender') || 'Select gender' }}</option>
                <option value="MALE">{{ $t('profile.male') }}</option>
                <option value="FEMALE">{{ $t('profile.female') }}</option>
                <option value="OTHER">{{ $t('profile.other') }}</option>
              </select>
            </div>
          </div>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('profile.bio') }}
          </label>
          <textarea
            v-model="form.bio"
            rows="4"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white"
            :placeholder="$t('profile.bio') || 'Tell us about yourself'"
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
  name: 'UserBasicInfoModalSimple',
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
      fullName: '',
      phoneNumber: '',
      gender: '',
      bio: ''
    })
    // Initialize form with user data
    watch(() => props.userData, (newData) => {
      if (newData) {
        form.value = {
          fullName: newData.fullName || '',
          phoneNumber: newData.phoneNumber || '',
          gender: newData.gender || '',
          bio: newData.bio || ''
        }
      }
    }, { immediate: true })
    const handleSubmit = async () => {
      loading.value = true
      try {
        await emit('submit', form.value)
        emit('close')
      } catch (error) {
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
