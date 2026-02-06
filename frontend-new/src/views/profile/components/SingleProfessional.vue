<template>
  <div class="bg-white dark:bg-gray-800 shadow rounded-lg p-6">
    <div class="flex justify-between items-center mb-6">
      <h3 class="text-lg font-medium text-gray-900 dark:text-white">{{ $t('profile.professional') }}</h3>
      <button
        @click="handleEdit"
        class="inline-flex items-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md text-white bg-primary hover:bg-primary/80 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary"
      >
        <Icon icon="mdi:pencil" class="h-4 w-4 mr-1" />
        {{ $t('profile.editProfessional') }}
      </button>
    </div>

    <div v-if="loading" class="text-center py-8">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">{{ $t('profile.loading') }}</p>
    </div>

    <div v-else-if="!professional" class="text-center py-8">
      <Icon icon="mdi:briefcase" class="mx-auto h-12 w-12 text-gray-400" />
      <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">{{ $t('profile.noData') }}</p>
      <button
        @click="handleCreate"
        class="mt-4 inline-flex items-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md text-white bg-primary hover:bg-primary/80 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary"
      >
        <Icon icon="mdi:plus" class="h-4 w-4 mr-1" />
        {{ $t('profile.addProfessional') }}
      </button>
    </div>

    <div v-else class="space-y-4">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div v-if="professional.jobTitle">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.jobTitle') }}</label>
          <p class="text-sm text-gray-900 dark:text-white">{{ professional.jobTitle }}</p>
        </div>
        
        <div v-if="professional.company">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.company') }}</label>
          <p class="text-sm text-gray-900 dark:text-white">{{ professional.company }}</p>
        </div>
        
        <div v-if="professional.department">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.department') }}</label>
          <p class="text-sm text-gray-900 dark:text-white">{{ professional.department }}</p>
        </div>
        
        <div v-if="professional.experience">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.experience') }}</label>
          <p class="text-sm text-gray-900 dark:text-white">{{ professional.experience }}</p>
        </div>
        
        <div v-if="professional.skills">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.skills') }}</label>
          <p class="text-sm text-gray-900 dark:text-white">{{ professional.skills }}</p>
        </div>
        
        <div v-if="professional.education">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.education') }}</label>
          <p class="text-sm text-gray-900 dark:text-white">{{ professional.education }}</p>
        </div>
        
        <div v-if="professional.certifications">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.certifications') }}</label>
          <p class="text-sm text-gray-900 dark:text-white">{{ professional.certifications }}</p>
        </div>
        
        <div v-if="professional.languages">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.languages') }}</label>
          <p class="text-sm text-gray-900 dark:text-white">{{ professional.languages }}</p>
        </div>
        
        <div v-if="professional.availability">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.availability') }}</label>
          <p class="text-sm text-gray-900 dark:text-white">{{ professional.availability }}</p>
        </div>
        
        <div v-if="professional.hourlyRate">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.hourlyRate') }}</label>
          <p class="text-sm text-gray-900 dark:text-white">{{ professional.hourlyRate }}</p>
        </div>
      </div>

      <!-- Professional Summary -->
      <div v-if="professional.summary" class="mt-6 p-4 bg-gray-50 dark:bg-gray-700 rounded-md">
        <p class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.professionalSummary') }}</p>
        <p class="text-sm text-gray-600 dark:text-gray-400">{{ professional.summary }}</p>
      </div>
    </div>

    <!-- Professional Form Modal/Overlay -->
    <div v-if="showForm" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-2xl max-h-screen overflow-y-auto">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-lg font-medium text-gray-900 dark:text-white">
            {{ isEdit ? $t('profile.editProfessional') : $t('profile.addProfessional') }}
          </h3>
          <button
            @click="handleCancel"
            class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
          >
            <Icon icon="mdi:close" class="h-6 w-6" />
          </button>
        </div>
        
        <ProfessionalForm
          :professional="editingProfessional"
          :owner-type="ownerType"
          :owner-id="ownerId"
          @submit="handleSubmit"
          @cancel="handleCancel"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import { useI18n } from 'vue-i18n'
import { usersApi } from '@/api/usersApi'
import { useAuthStore } from '@/stores/authStore'
import ProfessionalForm from './ProfessionalForm.vue'

export default {
  name: 'SingleProfessional',
  components: {
    Icon,
    ProfessionalForm
  },
  props: {
    ownerType: {
      type: String,
      required: true
    },
    ownerId: {
      type: Number,
      required: true
    }
  },
  emits: ['updated'],
  setup(props, { emit }) {
    const { t } = useI18n()
    const authStore = useAuthStore()
    const professional = ref(null)
    const loading = ref(false)
    const showForm = ref(false)
    const editingProfessional = ref(null)

    const isEdit = computed(() => !!editingProfessional.value)

    const loadProfessional = async () => {
      loading.value = true
      try {
        // Use current user from authStore instead of props.ownerId
        const currentUser = authStore.currentUser
        if (!currentUser) {
          console.error('No current user found in authStore')
          professional.value = null
          return
        }

        const response = await usersApi.getProfile()
        // Extract professional data from user profile
        const userData = response.data
        const profile = userData.profile || {}
        
        // Create professional object from profile data (only fields supported by backend)
        const professionalData = {
          jobTitle: profile.jobTitle,
          company: profile.company,
          department: profile.department,
          experience: profile.experience,
          skills: profile.skills,
          qualification: profile.education,
          certification: profile.certifications,
          languages: profile.languages,
          availability: profile.availability,
          hourlyRate: profile.hourlyRate,
          linkedinUrl: profile.linkedinUrl,
          website: profile.website,
          location: profile.location,
          portfolioUrl: profile.portfolioUrl
        }
        
        // Filter out empty fields
        professional.value = Object.fromEntries(
          Object.entries(professionalData).filter(([_, value]) => value && value.toString().trim() !== '')
        )
        
        // If no professional data exists, set to null
        if (!professional.value || Object.keys(professional.value).length === 0) {
          professional.value = null
        }
      } catch (error) {
        console.error('Error loading professional data:', error)
        professional.value = null
      } finally {
        loading.value = false
      }
    }

    const handleEdit = () => {
      editingProfessional.value = professional.value
      showForm.value = true
    }

    const handleCreate = () => {
      editingProfessional.value = null
      showForm.value = true
    }

    const handleCancel = () => {
      showForm.value = false
      editingProfessional.value = null
    }

    const handleSubmit = async (formData) => {
      try {
        // Map form data to match backend DTO
        const professionalData = {
          jobTitle: formData.jobTitle,
          company: formData.company,
          department: formData.department,
          experience: formData.experience,
          skills: formData.skills,
          education: formData.qualification,
          certifications: formData.certification,
          industry: formData.industry,
          summary: formData.summary
        }

        await usersApi.updateProfessionalInfo(professionalData)
        
        // Reload professional data
        await loadProfessional()
        
        // Close form
        handleCancel()
        
        // Emit update event
        emit('updated')
      } catch (error) {
        console.error('Error saving professional data:', error)
        // Handle error (show toast, etc.)
      }
    }

    onMounted(() => {
      loadProfessional()
    })

    return {
      professional,
      loading,
      showForm,
      editingProfessional,
      isEdit,
      handleEdit,
      handleCreate,
      handleCancel,
      handleSubmit,
      t
    }
  }
}
</script>
