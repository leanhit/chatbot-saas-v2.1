<template>
  <div class="bg-white dark:bg-gray-800 shadow rounded-lg p-6">
    <div class="flex justify-between items-center mb-6">
      <h3 class="text-lg font-medium text-gray-900 dark:text-white">{{ $t('profile.basicInfo') }}</h3>
      <button
        @click="handleEdit"
        class="inline-flex items-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md text-white bg-primary hover:bg-primary/80 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary"
      >
        <Icon icon="mdi:pencil" class="h-4 w-4 mr-1" />
        {{ $t('profile.editBasicInfo') }}
      </button>
    </div>

    <div v-if="loading" class="text-center py-8">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">{{ $t('profile.loading') }}</p>
    </div>

    <div v-else-if="!basicInfo" class="text-center py-8">
      <Icon icon="mdi:account-details" class="mx-auto h-12 w-12 text-gray-400" />
      <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">{{ $t('profile.noData') }}</p>
      <button
        @click="handleCreate"
        class="mt-4 inline-flex items-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md text-white bg-primary hover:bg-primary/80 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary"
      >
        <Icon icon="mdi:plus" class="h-4 w-4 mr-1" />
        {{ $t('profile.addBasicInfo') }}
      </button>
    </div>

    <div v-else class="space-y-4">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div v-if="basicInfo.fullName">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.fullName') }}</label>
          <p class="text-sm text-gray-900 dark:text-white">{{ basicInfo.fullName }}</p>
        </div>
        
        <div v-if="basicInfo.phoneNumber">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.phoneNumber') }}</label>
          <p class="text-sm text-gray-900 dark:text-white">{{ basicInfo.phoneNumber }}</p>
        </div>
        
        <div v-if="basicInfo.gender">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.gender') }}</label>
          <p class="text-sm text-gray-900 dark:text-white">{{ getGenderLabel(basicInfo.gender) }}</p>
        </div>
        
        <div v-if="basicInfo.email">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.email') }}</label>
          <p class="text-sm text-gray-900 dark:text-white">{{ basicInfo.email }}</p>
        </div>
      </div>

      <!-- Bio -->
      <div v-if="basicInfo.bio" class="mt-6 p-4 bg-gray-50 dark:bg-gray-700 rounded-md">
        <p class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('profile.bio') }}:</p>
        <p class="text-sm text-gray-600 dark:text-gray-400">{{ basicInfo.bio }}</p>
      </div>
    </div>

    <!-- Basic Info Form Modal/Overlay -->
    <div v-if="showForm" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-2xl max-h-screen overflow-y-auto">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-lg font-medium text-gray-900 dark:text-white">
            {{ isEdit ? $t('profile.editBasicInfo') : $t('profile.addBasicInfo') }}
          </h3>
          <button
            @click="handleCancel"
            class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
          >
            <Icon icon="mdi:close" class="h-6 w-6" />
          </button>
        </div>
        
        <BasicInfoForm
          :basic-info="editingBasicInfo"
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
import BasicInfoForm from './BasicInfoForm.vue'

export default {
  name: 'SingleBasicInfo',
  components: {
    Icon,
    BasicInfoForm
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
    const basicInfo = ref(null)
    const loading = ref(false)
    const showForm = ref(false)
    const editingBasicInfo = ref(null)

    const isEdit = computed(() => !!editingBasicInfo.value)

    const hasSocialLinks = computed(() => {
      if (!basicInfo.value) return false
      return basicInfo.value.linkedinUrl || basicInfo.value.website || basicInfo.value.portfolioUrl
    })

    const getGenderLabel = (gender) => {
      switch (gender) {
        case 'MALE': return t('profile.male')
        case 'FEMALE': return t('profile.female')
        case 'OTHER': return t('profile.other')
        default: return gender
      }
    }

    const loadBasicInfo = async () => {
      loading.value = true
      try {
        // Use current user from authStore instead of props.ownerId
        const currentUser = authStore.currentUser
        if (!currentUser) {
          console.error('No current user found in authStore')
          basicInfo.value = null
          return
        }

        const response = await usersApi.getProfile()
        // Extract basic info from user profile
        const userData = response.data
        const profile = userData.profile || {}
        
        // Create basic info object from profile data (only fields supported by backend)
        const basicData = {
          fullName: profile.fullName,
          phoneNumber: profile.phoneNumber,
          gender: profile.gender,
          email: userData.email,
          bio: profile.bio
        }
        
        // Filter out empty fields
        basicInfo.value = Object.fromEntries(
          Object.entries(basicData).filter(([_, value]) => value && value.toString().trim() !== '')
        )
        
        // If no basic info exists, set to null
        if (!basicInfo.value || Object.keys(basicInfo.value).length === 0) {
          basicInfo.value = null
        }
      } catch (error) {
        console.error('Error loading basic info:', error)
        basicInfo.value = null
      } finally {
        loading.value = false
      }
    }

    const handleEdit = () => {
      editingBasicInfo.value = basicInfo.value
      showForm.value = true
    }

    const handleCreate = () => {
      editingBasicInfo.value = null
      showForm.value = true
    }

    const handleCancel = () => {
      showForm.value = false
      editingBasicInfo.value = null
    }

    const handleSubmit = async (formData) => {
      try {
        // Use current user from authStore instead of props.ownerId
        const currentUser = authStore.currentUser
        if (!currentUser) {
          console.error('No current user found in authStore')
          return
        }

        // Map form data to match backend BasicInfoRequest DTO
        const basicData = {
          fullName: formData.fullName,
          phoneNumber: formData.phoneNumber,
          gender: formData.gender,
          bio: formData.bio
        }

        await usersApi.updateBasicInfo(basicData)
        
        // Reload basic info
        await loadBasicInfo()
        
        // Close form
        handleCancel()
        
        // Emit update event
        emit('updated')
      } catch (error) {
        console.error('Error updating basic info:', error)
      }
    }

    onMounted(() => {
      loadBasicInfo()
    })

    return {
      basicInfo,
      loading,
      showForm,
      editingBasicInfo,
      isEdit,
      hasSocialLinks,
      getGenderLabel,
      handleEdit,
      handleCreate,
      handleCancel,
      handleSubmit,
      t
    }
  }
}
</script>
