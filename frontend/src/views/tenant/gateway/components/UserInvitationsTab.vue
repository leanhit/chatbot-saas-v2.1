<template>
  <div class="user-invitations-tab">
    <div v-if="loading" class="loading-state">
      <div class="text-center py-12">
        <Icon icon="mdi:loading" class="animate-spin h-8 w-8 text-gray-400 mx-auto mb-4" />
        <p class="text-gray-600 dark:text-gray-400">Loading invitations...</p>
      </div>
    </div>
    <div v-else-if="invitations.length === 0" class="empty-state">
      <div class="text-center py-12">
        <Icon icon="mdi:email" class="h-16 w-16 text-gray-400 mx-auto mb-4" />
        <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">
          {{ $t('My Invitations') }}
        </h3>
        <p class="text-gray-600 dark:text-gray-400">
          No invitations available.
        </p>
      </div>
    </div>
    <div v-else class="invitations-list">
      <div 
        v-for="invitation in invitations" 
        :key="invitation.id"
        class="bg-white dark:bg-gray-800 rounded-lg shadow-md border border-gray-200 dark:border-gray-700 p-6 mb-4"
      >
        <div class="flex justify-between items-center">
          <div class="flex items-center space-x-4">
            <div class="tenant-avatar-invitation">
              <div class="avatar-fallback">
                {{ invitation.name?.charAt(0)?.toUpperCase() || '?' }}
              </div>
            </div>
            <div>
              <h4 class="font-medium text-gray-900 dark:text-white">{{ invitation.name }}</h4>
              <p class="text-sm text-gray-600 dark:text-gray-400">Invited by: {{ invitation.invitedByName }}</p>
              <p class="text-sm text-gray-600 dark:text-gray-400">Role: {{ invitation.role }}</p>
            </div>
          </div>
          <div class="flex gap-2">
            <button
              @click="acceptInvitation(invitation.id, invitation.token)"
              class="px-3 py-1 bg-green-600 text-white text-sm rounded hover:bg-green-700"
            >
              {{ $t('Accept') }}
            </button>
            <button
              @click="rejectInvitation(invitation.id, invitation.token)"
              class="px-3 py-1 bg-red-600 text-white text-sm rounded hover:bg-red-700"
            >
              {{ $t('Reject') }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import { ref, computed, onMounted, getCurrentInstance } from 'vue'
import { Icon } from '@iconify/vue'
import { useI18n } from 'vue-i18n'
import { useGatewayUserInvitationStore } from '@/stores/tenant/gateway/userInvitationStore'
import { formatDateTime } from '@/utils/dateUtils'
import { secureImageUrl } from '@/utils/imageUtils'
import { InvitationStatus, TenantRole } from '@/types/tenant'

export default {
  name: 'UserInvitationsTab',
  components: {
    Icon
  },
  setup() {
    const { t } = useI18n()
    const invitationStore = useGatewayUserInvitationStore()
    
    const invitations = computed(() => invitationStore.invitations)
    const loading = computed(() => invitationStore.loading)
    
    const acceptInvitation = async (invitationId, token) => {
      try {
        await invitationStore.acceptInvitation(invitationId, token)
        // Show success message
        const instance = getCurrentInstance()
        const toast = instance?.appContext.config.globalProperties.$toast
        toast?.success('Invitation accepted successfully')
      } catch (error) {
        console.error('Failed to accept invitation:', error)
        // Show error message
        const instance = getCurrentInstance()
        const toast = instance?.appContext.config.globalProperties.$toast
        toast?.error('Failed to accept invitation')
      }
    }
    
    const rejectInvitation = async (invitationId, token) => {
      try {
        await invitationStore.rejectInvitation(invitationId, token)
        // Show success message
        const instance = getCurrentInstance()
        const toast = instance?.appContext.config.globalProperties.$toast
        toast?.success('Invitation rejected successfully')
      } catch (error) {
        console.error('Failed to reject invitation:', error)
        // Show error message
        const instance = getCurrentInstance()
        const toast = instance?.appContext.config.globalProperties.$toast
        toast?.error('Failed to reject invitation')
      }
    }
    
    onMounted(() => {
      invitationStore.fetchUserInvitations()
    })
    
    return {
      t,
      invitations,
      loading,
      acceptInvitation,
      rejectInvitation,
      formatDateTime
    }
  }
}
</script>
<style scoped>
.user-invitations-tab {
  width: 100%;
}
.loading-state {
  text-align: center;
  padding: 40px 0;
}
.empty-state {
  text-align: center;
  padding: 40px 0;
}
.invitations-list {
  margin-top: 20px;
}
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
.animate-spin {
  animation: spin 1s linear infinite;
}
</style>
<style scoped>
.tenant-avatar-invitation {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-fallback {
  color: white;
  font-weight: 600;
  font-size: 18px;
}
</style>
