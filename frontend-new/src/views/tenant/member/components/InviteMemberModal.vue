<template>
  <div class="fixed inset-0 z-50 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
      <div class="fixed inset-0 transition-opacity" aria-hidden="true">
        <div class="absolute inset-0 bg-gray-500 opacity-75" @click="handleClose"></div>
      </div>

      <div class="inline-block align-bottom bg-white dark:bg-gray-800 rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
        <form @submit.prevent="handleSubmit">
          <div class="bg-white dark:bg-gray-800 px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
            <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-4">
              {{ $t('tenant.member.inviteNewMember') }}
            </h3>

            <div class="mb-4">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                {{ $t('tenant.member.emailAddress') }} *
              </label>
              <input
                v-model="formData.email"
                type="email"
                required
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white"
                :placeholder="$t('tenant.member.emailPlaceholder')"
              />
            </div>

            <div class="mb-4">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                {{ $t('tenant.member.assignRole') }}
              </label>
              <select
                v-model="formData.role"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white"
              >
                <option value="ADMIN">{{ $t('tenant.member.admin') }}</option>
                <option value="MANAGER">{{ $t('tenant.member.manager') }}</option>
                <option value="MEMBER">{{ $t('tenant.member.member') }}</option>
                <option value="EDITOR">{{ $t('tenant.member.editor') }}</option>
                <option value="VIEWER">{{ $t('tenant.member.viewer') }}</option>
              </select>
            </div>

            <div class="mb-4">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                {{ $t('tenant.member.invitationExpiry') }}
              </label>
              <div class="flex items-center space-x-2">
                <input
                  v-model.number="formData.expiryDays"
                  type="number"
                  min="1"
                  max="30"
                  class="flex-1 px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white"
                />
                <span class="text-sm text-gray-600 dark:text-gray-400">{{ $t('tenant.member.days') }}</span>
              </div>
              <p class="mt-1 text-xs text-gray-500 dark:text-gray-400">{{ $t('tenant.member.expiryDescription') }}</p>
            </div>

            <div class="mb-4">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                {{ $t('tenant.member.personalMessage') }}
              </label>
              <textarea
                v-model="formData.message"
                rows="3"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-primary focus:border-primary dark:bg-gray-700 dark:text-white"
                :placeholder="$t('tenant.member.messagePlaceholder')"
              ></textarea>
            </div>
          </div>

          <div class="bg-gray-50 dark:bg-gray-700 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
            <button
              type="submit"
              :disabled="loading"
              class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-primary text-base font-medium text-white hover:bg-primary/80 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary sm:ml-3 sm:w-auto sm:text-sm disabled:opacity-50"
            >
              <div v-if="loading" class="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
              {{ $t('tenant.member.sendInvitation') }}
            </button>
            <button
              type="button"
              @click="handleClose"
              class="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 dark:border-gray-600 shadow-sm px-4 py-2 bg-white dark:bg-gray-800 text-base font-medium text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
            >
              {{ $t('common.cancel') }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, watch } from 'vue'
import { useGatewayTenantStore } from '@/stores/tenant/gateway/myTenantStore'
import { tenantApi } from '@/api/tenantApi'

export default {
  name: 'InviteMemberModal',
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
    const formData = ref({
      email: '',
      role: 'MEMBER',
      expiryDays: 7,
      message: ''
    })

    // Reset form when modal opens
    watch(() => props.visible, (isVisible) => {
      if (isVisible) {
        formData.value = {
          email: '',
          role: 'MEMBER',
          expiryDays: 7,
          message: `Hi there!\n\nYou've been invited to join ${tenantStore.currentTenant?.name || 'our workspace'}.\n\nClick the link in the invitation email to get started.\n\nBest regards`
        }
      }
    })

    const handleSubmit = async () => {
      if (!formData.value.email) {
        alert('Please enter an email address')
        return
      }

      loading.value = true
      try {
        // Prepare data matching backend InviteMemberRequest DTO
        const inviteData = {
          email: formData.value.email,
          role: formData.value.role,
          expiryDays: formData.value.expiryDays
        }

        // Call real API
        await tenantApi.inviteTenantMember(tenantStore.currentTenant?.id, inviteData)
        
        console.log('Invitation sent successfully:', inviteData)
        
        emit('invited', formData.value)
        handleClose()
      } catch (error) {
        console.error('Error inviting member:', error)
        const errorMessage = error.response?.data?.message || 'Failed to send invitation. Please try again.'
        alert(errorMessage)
      } finally {
        loading.value = false
      }
    }

    const handleClose = () => {
      emit('close')
    }

    return {
      loading,
      formData,
      handleSubmit,
      handleClose
    }
  }
}
</script>
