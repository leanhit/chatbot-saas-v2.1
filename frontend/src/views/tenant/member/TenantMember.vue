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
        <div v-else-if="activeTab === 'active-members'" class="p-6">
          <ActiveMemberTab 
            :search-query="searchStore.searchQuery"
            @member-removed="handleMemberRemoved"
            @member-updated="handleMemberUpdated"
          />
        </div>
          <!-- Pending Requests Tab -->
        <div v-else-if="activeTab === 'pending-requests'" class="p-6">
          <PendingMemberTab 
            :search-query="searchStore.searchQuery"
            @request-approved="handleRequestApproved"
            @request-rejected="handleRequestRejected"
          />
        </div>
          <!-- Pending Invitations Tab -->
          <div v-else-if="activeTab === 'pending-invitations'" class="p-6">
            <InviteMemberTab 
              :search-query="searchStore.searchQuery"
              @invitation-revoked="handleInvitationRevoked"
              @invitation-sent="handleMemberInvited"
            />
          </div>
        </div>
      </div>
    </div>
    <!-- Invite Member Modal -->
    <InviteMemberModal
      :visible="showInviteModal"
      @close="showInviteModal = false"
      @invited="handleInviteMember"
    />
  </div>
</template>
<script>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Icon } from '@iconify/vue'
import { useGatewayTenantStore } from '@/stores/tenant/gateway/myTenantStore'
import { useSearchStore } from '@/stores/searchStore'
import { tenantApi } from '@/api/tenantApi'
import ActiveMemberTab from './components/ActiveMemberTab.vue'
import PendingMemberTab from './components/PendingMemberTab.vue'
import InviteMemberModal from './components/InviteMemberModal.vue'
import InviteMemberTab from './components/InviteMemberTab.vue'

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
    const searchStore = useSearchStore()
    const activeTab = ref('active-members')
    const loading = ref(false)
    const showInviteModal = ref(false)
    // Real counts from API calls
    const activeMembersCount = ref(0)
    const pendingRequestsCount = ref(0)
    const pendingInvitationsCount = ref(0)
    const currentTenant = computed(() => tenantStore.currentTenant)
    
    // Set search context when component mounts
    onMounted(() => {
      searchStore.setSearchContext('members')
      searchStore.clearFilters() // Reset any existing filters
    })
    const openInviteModal = () => {
      showInviteModal.value = true
    }
    const handleInviteMember = async (inviteData) => {
      try {
        // Use tenantKey from currentTenant object
        const tenantKey = currentTenant.value?.tenantKey
        if (!tenantKey) {
          alert('No tenant selected')
          return
        }
        
        // Call API with tenantKey instead of ID
        await tenantApi.inviteMember(tenantKey, {
          email: inviteData.email,
          role: inviteData.role
        })
        
        // Show success message
        alert(`Invitation sent to ${inviteData.email}`)
        // Reset and close modal
        showInviteModal.value = false
        // Refresh data
        await refreshData()
      } catch (error) {
        alert('Failed to send invitation. Please try again.')
      }
    }
    const refreshData = async () => {
      loading.value = true
      try {
        // Get tenantKey from current tenant
        const tenantKey = currentTenant.value?.tenantKey
        if (!tenantKey) {
          console.error('No tenant selected')
          return
        }

        // Load real data from APIs
        await Promise.all([
          updateCounts(tenantKey),
          // Refresh child components data by emitting events
          refreshChildComponents()
        ])
      } catch (error) {
        console.error('Failed to refresh data:', error)
        console.error('Error details:', {
          message: error.message,
          status: error.response?.status,
          statusText: error.response?.statusText,
          data: error.response?.data
        })
        
        // Show user-friendly error message
        if (error.response?.status === 409) {
          console.warn('Conflict error - possibly duplicate request')
        } else if (error.response?.status === 401) {
          console.warn('Unauthorized - token may be expired')
        } else if (error.response?.status === 403) {
          console.warn('Forbidden - insufficient permissions')
        }
      } finally {
        loading.value = false
      }
    }
    const updateCounts = async (tenantKey) => {
      try {
        // Get real counts from API calls
        const [membersResponse, requestsResponse, invitationsResponse] = await Promise.all([
          tenantApi.getTenantMembers(tenantKey),
          tenantApi.getJoinRequests(tenantKey),
          tenantApi.getTenantInvitations(tenantKey)
        ])

        // Update counts with real data
        activeMembersCount.value = (membersResponse.data?.content || membersResponse.data || []).length
        pendingRequestsCount.value = (requestsResponse.data || []).length
        pendingInvitationsCount.value = (invitationsResponse.data || []).length
      } catch (error) {
        console.error('Failed to update counts:', error)
        console.error('Error details:', {
          message: error.message,
          status: error.response?.status,
          statusText: error.response?.statusText,
          data: error.response?.data
        })
        
        // Show user-friendly error message
        if (error.response?.status === 409) {
          console.warn('Conflict error - possibly duplicate request')
        } else if (error.response?.status === 401) {
          console.warn('Unauthorized - token may be expired')
        } else if (error.response?.status === 403) {
          console.warn('Forbidden - insufficient permissions')
        }
        
        // Set to 0 on error
        activeMembersCount.value = 0
        pendingRequestsCount.value = 0
        pendingInvitationsCount.value = 0
      }
    }
    const refreshChildComponents = () => {
      // Trigger refresh in child components through refs or events
      // This will be handled by the child components' onMounted lifecycle
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
    onMounted(async () => {
      // Load real data on component mount
      await refreshData()
    })
    return {
      activeTab,
      loading,
      showInviteModal,
      searchStore,
      activeMembersCount,
      pendingRequestsCount,
      pendingInvitationsCount,
      currentTenant,
      openInviteModal,
      handleInviteMember,
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
