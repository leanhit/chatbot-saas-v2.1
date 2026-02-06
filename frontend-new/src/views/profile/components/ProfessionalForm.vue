<template>
  <div class="bg-white dark:bg-gray-800 shadow rounded-lg p-6">
    <div class="mb-6">
      <h3 class="text-lg font-medium text-gray-900 dark:text-white">
        {{ isEdit ? 'Edit Professional Information' : 'Add Professional Information' }}
      </h3>
      <p class="mt-1 text-sm text-gray-600 dark:text-gray-400">
        {{ isEdit ? 'Update your professional information' : 'Enter your professional details' }}
      </p>
    </div>

    <form @submit.prevent="handleSubmit" class="space-y-6">
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

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Department
          </label>
          <input
            v-model="form.department"
            type="text"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white"
            placeholder="Enter department"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Experience
          </label>
          <input
            v-model="form.experience"
            type="text"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white"
            placeholder="e.g., 5 years, 3+ years"
          />
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Skills
          </label>
          <input
            v-model="form.skills"
            type="text"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white"
            placeholder="e.g., Vue.js, Python, Project Management"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Qualification
          </label>
          <input
            v-model="form.qualification"
            type="text"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white"
            placeholder="e.g., Bachelor's Degree, Master's Degree"
          />
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Certification
          </label>
          <input
            v-model="form.certification"
            type="text"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white"
            placeholder="e.g., PMP, AWS, Google Certified"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Industry
          </label>
          <input
            v-model="form.industry"
            type="text"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white"
            placeholder="e.g., Technology, Finance, Healthcare"
          />
        </div>
      </div>

      <div>
        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
          Professional Summary
        </label>
        <textarea
          v-model="form.summary"
          rows="4"
          class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white"
          placeholder="Brief description of your professional background and expertise"
        ></textarea>
      </div>

      <div class="flex justify-end space-x-3">
        <button
          type="button"
          @click="$emit('cancel')"
          class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-700 hover:bg-gray-50 dark:hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary"
        >
          Cancel
        </button>
        <button
          type="submit"
          :disabled="loading"
          class="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-primary hover:bg-primary/80 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <span v-if="!loading">{{ isEdit ? 'Update Professional' : 'Create Professional' }}</span>
          <span v-else>{{ isEdit ? 'Updating...' : 'Creating...' }}</span>
        </button>
      </div>
    </form>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'

export default {
  name: 'ProfessionalForm',
  props: {
    professional: {
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
    const loading = ref(false)

    const form = ref({
      ownerType: props.ownerType,
      ownerId: props.ownerId,
      jobTitle: '',
      company: '',
      department: '',
      experience: '',
      skills: '',
      qualification: '',
      certification: '',
      industry: '',
      summary: ''
    })

    const isEdit = computed(() => !!props.professional)

    const handleSubmit = () => {
      emit('submit', { ...form.value })
    }

    // Initialize form with professional data if editing
    const initializeForm = () => {
      if (props.professional) {
        // Editing existing professional - populate with existing data
        form.value = {
          ownerType: props.ownerType,
          ownerId: props.ownerId,
          jobTitle: props.professional.jobTitle || '',
          company: props.professional.company || '',
          department: props.professional.department || '',
          experience: props.professional.experience || '',
          skills: props.professional.skills || '',
          qualification: props.professional.qualification || '',
          certification: props.professional.certification || '',
          industry: props.professional.industry || '',
          summary: props.professional.summary || ''
        }
      } else {
        // Creating new professional - keep empty form
        form.value = {
          ownerType: props.ownerType,
          ownerId: props.ownerId,
          jobTitle: '',
          company: '',
          department: '',
          experience: '',
          skills: '',
          qualification: '',
          certification: '',
          industry: '',
          summary: ''
        }
      }
    }

    // Watch for professional prop changes
    watch(() => props.professional, initializeForm, { immediate: true })

    onMounted(() => {
      initializeForm()
    })

    return {
      form,
      loading,
      isEdit,
      handleSubmit
    }
  }
}
</script>
