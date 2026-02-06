<template>
  <div v-if="visible" class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50" @click="handleOverlayClick">
    <div class="relative top-20 mx-auto p-4 border w-full max-w-md shadow-lg rounded-md bg-white dark:bg-gray-800 border-gray-200 dark:border-gray-700" @click.stop>
      <div class="flex items-center justify-between border-b border-gray-200 dark:border-gray-700 pb-4">
        <h3 class="text-lg font-medium text-gray-900 dark:text-white">{{ $t('tenant.member.inviteNewMember') }}</h3>
        <button @click="handleClose" class="text-gray-400 hover:text-gray-500">
          <Icon icon="mdi:close" class="h-6 w-6" />
        </button>
      </div>

      <form @submit.prevent="handleSubmit" class="mt-4">
        <!-- Email Address -->
        <div class="mb-4">
          <label for="email" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('tenant.member.emailAddress') }} *
          </label>
          <input
            id="email"
            v-model="formData.email"
            type="email"
            class="mt-1 block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
            :placeholder="$t('tenant.member.emailPlaceholder')"
            required
          />
        </div>

        <!-- Assign Role -->
        <div class="mb-4">
          <label for="role" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('tenant.member.assignRole') }}
          </label>
          <select
            id="role"
            v-model="formData.role"
            class="mt-1 block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
          >
            <option value="ADMIN">{{ $t('tenant.member.admin') }}</option>
            <option value="MANAGER">{{ $t('tenant.member.manager') }}</option>
            <option value="MEMBER">{{ $t('tenant.member.member') }}</option>
            <option value="EDITOR">{{ $t('tenant.member.editor') }}</option>
            <option value="VIEWER">{{ $t('tenant.member.viewer') }}</option>
          </select>
        </div>

        <!-- Invitation Expiry -->
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
              class="mt-1 block flex-1 px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
            />
            <span class="text-sm text-gray-600 dark:text-gray-400">{{ $t('tenant.member.days') }}</span>
          </div>
          <p class="mt-1 text-xs text-gray-500 dark:text-gray-400">{{ $t('tenant.member.expiryDescription') }}</p>
        </div>

        <!-- Personal Message -->
        <div class="mb-6">
          <label for="message" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('tenant.member.personalMessage') }}
          </label>
          <textarea
            id="message"
            v-model="formData.message"
            rows="3"
            class="mt-1 block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
            :placeholder="$t('tenant.member.messagePlaceholder')"
          ></textarea>
        </div>

        <!-- Form Actions -->
        <div class="flex justify-end space-x-3">
          <button
            type="button"
            @click="handleClose"
            class="px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary dark:bg-gray-700 dark:border-gray-600 dark:text-gray-300 dark:hover:bg-gray-600"
          >
            {{ $t('common.cancel') }}
          </button>
          <button
            type="submit"
            :disabled="loading"
            class="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-primary hover:bg-primary/80 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <div v-if="loading" class="flex items-center">
              <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
              {{ $t('tenant.member.sending') }}
            </div>
            <span v-else>{{ $t('tenant.member.sendInvitation') }}</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { ref, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { tenantApi } from '@/api/tenantApi'
import { useGatewayTenantStore } from '@/stores/tenant/gateway/myTenantStore'

export default {
  name: 'InviteMemberModal',
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
        // In real implementation:
        // await tenantApi.inviteTenantMember(tenantStore.currentTenant.id, formData.value)
        
        console.log('Inviting member:', formData.value)
        
        // Simulate API call
        await new Promise(resolve => setTimeout(resolve, 1500))
        
        emit('invited', formData.value)
        handleClose()
      } catch (error) {
        console.error('Error inviting member:', error)
        alert('Failed to send invitation. Please try again.')
      } finally {
        loading.value = false
      }
    }

    const handleClose = () => {
      emit('close')
    }

    const handleOverlayClick = () => {
      handleClose()
    }

    return {
      loading,
      formData,
      handleSubmit,
      handleClose,
      handleOverlayClick
    }
  }
}
</script>
