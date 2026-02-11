<template>
  <div class="bg-white dark:bg-gray-800 shadow rounded-lg p-6">
    <div class="mb-6">
      <h3 class="text-lg font-medium text-gray-900 dark:text-white">
        {{ isEdit ? $t('profile.editBasicInfo') : $t('profile.addBasicInfo') }}
      </h3>
      <p class="mt-1 text-sm text-gray-600 dark:text-gray-400">
        {{ isEdit ? $t('profile.update') + ' ' + $t('profile.basicInfo').toLowerCase() : $t('profile.create') + ' ' + $t('profile.basicInfo').toLowerCase() }}
      </p>
    </div>
    <form @submit.prevent="handleSubmit" class="space-y-6">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('profile.fullName') }}
          </label>
          <input
            v-model="form.fullName"
            type="text"
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
      <div>
        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
          {{ $t('profile.bio') }}
        </label>
        <textarea
          v-model="form.bio"
          rows="4"
          class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white"
          :placeholder="$t('profile.bioPlaceholder') || 'Tell us about yourself...'"
        ></textarea>
      </div>
      <div class="flex justify-end space-x-3">
        <button
          type="button"
          @click="$emit('cancel')"
          class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-700 hover:bg-gray-50 dark:hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary"
        >
          {{ $t('profile.cancel') }}
        </button>
        <button
          type="submit"
          :disabled="loading"
          class="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-primary hover:bg-primary/80 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <span v-if="!loading">{{ isEdit ? $t('profile.update') + ' ' + $t('profile.basicInfo') : $t('profile.create') + ' ' + $t('profile.basicInfo') }}</span>
          <span v-else>{{ isEdit ? 'Updating...' : 'Creating...' }}</span>
        </button>
      </div>
    </form>
  </div>
</template>
<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
export default {
  name: 'BasicInfoForm',
  props: {
    basicInfo: {
      type: Object,
      default: null
    },
    ownerType: {
      type: String,
      required: true
    },
    ownerId: {
      type: Number,
      required: true
    }
  },
  emits: ['submit', 'cancel'],
  setup(props, { emit }) {
    const { t } = useI18n()
    const loading = ref(false)
    const form = ref({
      ownerType: props.ownerType,
      ownerId: props.ownerId,
      fullName: '',
      phoneNumber: '',
      gender: '',
      bio: ''
    })
    const isEdit = computed(() => !!props.basicInfo)
    const handleSubmit = () => {
      emit('submit', { ...form.value })
    }
    // Initialize form with basic info data if editing
    const initializeForm = () => {
      if (props.basicInfo) {
        // Editing existing basic info - populate with existing data
        form.value = {
          ownerType: props.ownerType,
          ownerId: props.ownerId,
          fullName: props.basicInfo.fullName || '',
          phoneNumber: props.basicInfo.phoneNumber || '',
          gender: props.basicInfo.gender || '',
          bio: props.basicInfo.bio || ''
        }
      } else {
        // Creating new basic info - keep empty form
        form.value = {
          ownerType: props.ownerType,
          ownerId: props.ownerId,
          fullName: '',
          phoneNumber: '',
          gender: '',
          bio: ''
        }
      }
    }
    // Watch for basic info prop changes
    watch(() => props.basicInfo, initializeForm, { immediate: true })
    onMounted(() => {
      initializeForm()
    })
    return {
      form,
      loading,
      isEdit,
      handleSubmit,
      t
    }
  }
}
</script>
