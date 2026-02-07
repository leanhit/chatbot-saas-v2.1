<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 py-8">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- Header -->
      <div class="mb-8">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-3xl font-bold text-gray-900 dark:text-white">{{ $t('tenant.member.title') }}</h1>
            <p class="mt-2 text-gray-600 dark:text-gray-400">{{ $t('tenant.member.subtitle') }}</p>
          </div>
          <div class="flex gap-3">
            <button
              @click="openInviteModal"
              class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500"
            >
              <Icon icon="mdi:account-plus" class="h-4 w-4 mr-2" />
              {{ $t('tenant.member.inviteMember') }}
            </button>
            <button
              @click="refreshData"
              class="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary dark:bg-gray-700 dark:text-gray-300 dark:border-gray-600 dark:hover:bg-gray-600"
            >
              <Icon icon="mdi:refresh" class="h-4 w-4 mr-2" />
              {{ $t('tenant.member.refresh') }}
            </button>
          </div>
        </div>
      </div>

      <!-- Tab Navigation -->
      <div class="bg-white dark:bg-gray-800 shadow rounded-lg">
        <div class="border-b border-gray-200 dark:border-gray-700">
          <nav class="-mb-px flex space-x-8" aria-label="Tabs">
            <button
              @click="activeTab = 'active-members'"
              :class="[
                activeTab === 'active-members'
                  ? 'border-primary text-primary'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 dark:text-gray-400 dark:hover:text-gray-300',
                'whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm transition-colors duration-200'
              ]"
            >
              Active Members
            </button>
            <button
              @click="activeTab = 'pending-requests'"
              :class="[
                activeTab === 'pending-requests'
                  ? 'border-primary text-primary'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 dark:text-gray-400 dark:hover:text-gray-300',
                'whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm transition-colors duration-200'
              ]"
            >
              Pending Requests
            </button>
            <button
              @click="activeTab = 'pending-invitations'"
              :class="[
                activeTab === 'pending-invitations'
                  ? 'border-primary text-primary'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 dark:text-gray-400 dark:hover:text-gray-300',
                'whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm transition-colors duration-200'
              ]"
            >
              Pending Invitations
            </button>
          </nav>
        </div>

        <!-- Tab Content -->
        <div class="p-6">
          <!-- Loading State -->
          <div v-if="loading" class="text-center py-12">
            <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
            <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">{{ $t('tenant.member.loading') }}</p>
          </div>

          <!-- Active Members Tab -->
          <ActiveMemberTab 
            v-if="activeTab === 'active-members'"
            @member-removed="handleMemberRemoved"
            @member-updated="handleMemberUpdated"
          />

          <!-- Pending Requests Tab -->
          <PendingMemberTab 
            v-if="activeTab === 'pending-requests'"
            @request-approved="handleRequestApproved"
            @request-rejected="handleRequestRejected"
          />

          <!-- Pending Invitations Tab -->
          <InviteMemberTab 
            v-if="activeTab === 'pending-invitations'"
            @invitation-revoked="handleInvitationRevoked"
            @member-invited="handleMemberInvited"
          />
        </div>
      </div>
    </div>

    <!-- Invite Member Modal -->
    <InviteMemberModal
      v-if="showInviteModal"
      :visible="showInviteModal"
      @close="showInviteModal = false"
      @invited="handleInvited"
    />
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import { useI18n } from 'vue-i18n'
import { useGatewayTenantStore } from '@/stores/tenant/gateway/myTenantStore'
import ActiveMemberTab from './components/ActiveMemberTab.vue'
import PendingMemberTab from './components/PendingMemberTab.vue'
import InviteMemberTab from './components/InviteMemberTab.vue'
import InviteMemberModal from './components/InviteMemberModal.vue'

export default {
  name: 'TenantMemberIndex',
  components: {
    Icon,
    ActiveMemberTab,
    PendingMemberTab,
    InviteMemberTab,
    InviteMemberModal
  },
  setup() {
    const { t } = useI18n()
    const tenantStore = useGatewayTenantStore()
    
    const activeTab = ref('active-members')
    const loading = ref(false)
    const showInviteModal = ref(false)
    
    // Mock counts - in real app, these would come from API
    const activeMembersCount = ref(0)
    const pendingRequestsCount = ref(0)
    const pendingInvitationsCount = ref(0)

    const currentTenant = computed(() => tenantStore.currentTenant)

    const openInviteModal = () => {
      showInviteModal.value = true
    }

    const handleInvited = (inviteData) => {
      console.log('Member invited:', inviteData)
      showInviteModal.value = false
      pendingInvitationsCount.value += 1
      // Refresh data
      refreshData()
    }

    const refreshData = async () => {
      loading.value = true
      try {
        // Refresh data based on current tab
        // In real implementation, this would call appropriate API methods
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        // Update counts
        await updateCounts()
      } catch (error) {
        console.error('Error refreshing data:', error)
      } finally {
        loading.value = false
      }
    }

    const updateCounts = async () => {
      try {
        // In real implementation, these would be API calls
        // For now, using mock data
        activeMembersCount.value = 5
        pendingRequestsCount.value = 2
        pendingInvitationsCount.value = 3
      } catch (error) {
        console.error('Error updating counts:', error)
      }
    }

    const handleMemberRemoved = () => {
      activeMembersCount.value = Math.max(0, activeMembersCount.value - 1)
    }

    const handleMemberUpdated = () => {
      // Member updated - no count change needed
    }

    const handleRequestApproved = () => {
      activeMembersCount.value += 1
      pendingRequestsCount.value = Math.max(0, pendingRequestsCount.value - 1)
    }

    const handleRequestRejected = () => {
      pendingRequestsCount.value = Math.max(0, pendingRequestsCount.value - 1)
    }

    const handleInvitationRevoked = () => {
      pendingInvitationsCount.value = Math.max(0, pendingInvitationsCount.value - 1)
    }

    const handleMemberInvited = () => {
      pendingInvitationsCount.value += 1
    }

    onMounted(() => {
      updateCounts()
    })

    return {
      activeTab,
      loading,
      showInviteModal,
      activeMembersCount,
      pendingRequestsCount,
      pendingInvitationsCount,
      currentTenant,
      openInviteModal,
      handleInvited,
      refreshData,
      handleMemberRemoved,
      handleMemberUpdated,
      handleRequestApproved,
      handleRequestRejected,
      handleInvitationRevoked,
      handleMemberInvited
    }
  }
}
</script>
