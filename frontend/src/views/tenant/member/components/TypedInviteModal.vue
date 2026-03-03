<template>
  <div
    v-if="visible"
    class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50"
    @click.self="closeModal"
  >
    <div class="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white dark:bg-gray-800 dark:border-gray-700">
      <!-- Header -->
      <div class="mt-3">
        <h3 class="text-lg leading-6 font-medium text-gray-900 dark:text-white">
          {{ $t('tenant.member.inviteMember') }}
        </h3>
        <div class="mt-2 px-7 py-3">
          <p class="text-sm text-gray-500 dark:text-gray-400">
            Invite a new member to join this tenant
          </p>
        </div>
      </div>

      <!-- Form -->
      <form @submit.prevent="handleSubmit" class="mt-4">
        <!-- Email -->
        <div class="mb-4">
          <label for="email" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Email Address <span class="text-red-500">*</span>
          </label>
          <input
            id="email"
            v-model="formData.email"
            type="email"
            required
            :placeholder="$t('tenant.member.emailPlaceholder')"
            class="mt-1 block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
            :class="{ 'border-red-500': errors.email }"
          />
          <p v-if="errors.email" class="mt-1 text-sm text-red-600 dark:text-red-400">
            {{ errors.email }}
          </p>
        </div>

        <!-- Role -->
        <div class="mb-4">
          <label for="role" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('tenant.member.assignRole') }}
          </label>
          <select
            id="role"
            v-model="formData.role"
            required
            class="mt-1 block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
          >
            <option :value="TenantRole.OWNER">Owner</option>
            <option :value="TenantRole.ADMIN">Admin</option>
            <option :value="TenantRole.MEMBER">Member</option>
          </select>
        </div>

        <!-- Message -->
        <div class="mb-4">
          <label for="message" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Invitation Message (Optional)
          </label>
          <textarea
            id="message"
            v-model="formData.message"
            rows="3"
            :placeholder="$t('tenant.member.messagePlaceholder')"
            class="mt-1 block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
          ></textarea>
        </div>

        <!-- Expiry Days -->
        <div class="mb-4">
          <label for="expiryDays" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('tenant.member.invitationExpiry') }}
          </label>
          <div class="flex items-center space-x-2">
            <input
              id="expiryDays"
              v-model.number="formData.expiryDays"
              type="number"
              min="1"
              max="30"
              class="w-20 px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
            />
            <span class="text-sm text-gray-500 dark:text-gray-400">days</span>
          </div>
        </div>

        <!-- Error Message -->
        <div v-if="errorMessage" class="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded-md dark:bg-red-900 dark:border-red-600 dark:text-red-200">
          {{ errorMessage }}
        </div>

        <!-- Actions -->
        <div class="flex justify-end space-x-3">
          <button
            type="button"
            @click="closeModal"
            class="px-4 py-2 bg-gray-300 text-gray-800 rounded-md hover:bg-gray-400 focus:outline-none focus:ring-2 focus:ring-gray-500 dark:bg-gray-600 dark:text-gray-200 dark:hover:bg-gray-500"
          >
            {{ $t('common.cancel') }}
          </button>
          <button
            type="submit"
            :disabled="loading"
            class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span v-if="loading">
              <Icon icon="mdi:loading" class="animate-spin h-4 w-4 mr-2" />
              {{ $t('common.sending') }}
            </span>
            <span v-else>{{ $t('common.send') }}</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script lang="ts">
import { ref, reactive, computed } from 'vue'
import { Icon } from '@iconify/vue'
import { tenantApi } from '@/api/tenantApi'
import { useGatewayTenantStore } from '@/stores/tenant/gateway/myTenantStore'
import { 
  InviteMemberRequest, 
  TenantRole,
  validateInviteMemberRequest
} from '@/types/tenant'

export default {
  name: 'TypedInviteModal',
  components: {
    Icon
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  emits: ['close', 'invited'],
  setup(props, { emit }) {
    const tenantStore = useGatewayTenantStore()
    const loading = ref(false)
    const errorMessage = ref('')
    
    // Typed form data
    const formData = reactive({
      ...InviteMemberRequest
    })
    
    // Form validation errors
    const errors = reactive<Record<string, string>>({
      email: '',
      role: ''
    })
    
    // Computed properties
    const currentTenant = computed(() => tenantStore.currentTenant)
    
    // Validation functions
    const validateForm = (): boolean => {
      errors.email = ''
      errors.role = ''
      
      // Use tenant types validation
      const validationErrors = validateInviteMemberRequest(formData)
      if (validationErrors.length > 0) {
        validationErrors.forEach(error => {
          if (error.toLowerCase().includes('email')) {
            errors.email = error
          } else if (error.toLowerCase().includes('role')) {
            errors.role = error
          }
        })
        return false
      }
      
      return true
    }
    
    // Reset form
    const resetForm = (): void => {
      Object.assign(formData, InviteMemberRequest)
      errorMessage.value = ''
      Object.keys(errors).forEach(key => {
        errors[key] = ''
      })
    }
    
    // Close modal
    const closeModal = (): void => {
      resetForm()
      emit('close')
    }
    
    // Handle form submission
    const handleSubmit = async (): Promise<void> => {
      if (!validateForm()) {
        return
      }
      
      const tenantKey = currentTenant.value?.tenantKey
      if (!tenantKey) {
        errorMessage.value = 'No tenant selected'
        return
      }
      
      loading.value = true
      errorMessage.value = ''
      
      try {
        // Create typed invite request
        const inviteRequest: InviteMemberRequest = {
          email: formData.email.trim(),
          role: formData.role as TenantRole,
          message: formData.message.trim() || undefined,
          expiryDays: formData.expiryDays || 7
        }
        
        // Call API with typed response
        const response = await tenantApi.inviteMember(tenantKey, inviteRequest)
        
        console.log('Invitation sent successfully:', response)
        
        // Emit success event
        emit('invited', inviteRequest)
        
        // Close modal
        closeModal()
        
      } catch (error: any) {
        console.error('Failed to send invitation:', error)
        
        // Handle different error types
        if (error.response?.status === 409) {
          errorMessage.value = 'This user has already been invited or is already a member'
        } else if (error.response?.status === 400) {
          errorMessage.value = error.response?.data?.message || 'Invalid request data'
        } else if (error.response?.status === 403) {
          errorMessage.value = 'You do not have permission to invite members'
        } else if (error.response?.status === 404) {
          errorMessage.value = 'Tenant not found'
        } else {
          errorMessage.value = 'Failed to send invitation. Please try again.'
        }
      } finally {
        loading.value = false
      }
    }
    
    return {
      loading,
      formData,
      errors,
      errorMessage,
      currentTenant,
      closeModal,
      handleSubmit,
      TenantRole
    }
  }
}
</script>

<style scoped>
/* Component-specific styles */
</style>
