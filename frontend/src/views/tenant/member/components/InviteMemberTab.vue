<template>
  <div class="pending-invitations-container">
    <!-- Search and Filter -->
    <div class="mb-6 flex flex-col sm:flex-row gap-4">
      <div class="flex-1">
        <div class="relative">
          <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <Icon icon="mdi:magnify" class="h-5 w-5 text-gray-400" />
          </div>
          <input
            v-model="searchQuery"
            type="text"
            :placeholder="$t('tenant.member.searchInvitations')"
            class="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md leading-5 bg-white placeholder-gray-500 focus:outline-none focus:placeholder-gray-400 focus:ring-1 focus:ring-primary focus:border-primary dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white"
          />
        </div>
      </div>
      <div class="flex gap-2">
        <select
          v-model="statusFilter"
          class="block px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary focus:border-primary dark:bg-gray-700 dark:border-gray-600 dark:text-white"
        >
          <option value="">{{ $t('tenant.member.allStatuses') }}</option>
          <option value="PENDING">Pending</option>
          <option value="EXPIRED">Expired</option>
        </select>
      </div>
    </div>
    <!-- Loading State -->
    <div v-if="loading" class="text-center py-12">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">{{ $t('tenant.member.loading') }}</p>
    </div>
    <!-- Empty State -->
    <div v-else-if="filteredInvitations.length === 0" class="text-center py-12">
      <Icon icon="mdi:email-send" class="mx-auto h-12 w-12 text-gray-400" />
      <h3 class="mt-2 text-sm font-medium text-gray-900 dark:text-white">{{ $t('tenant.member.noPendingInvitations') }}</h3>
      <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">{{ $t('tenant.member.noPendingInvitationsDescription') }}</p>
    </div>
    <!-- Invitations Table -->
    <div v-else class="overflow-hidden shadow ring-1 ring-black ring-opacity-5 md:rounded-lg">
      <table class="min-w-full divide-y divide-gray-300 dark:divide-gray-700">
        <thead class="bg-gray-50 dark:bg-gray-800">
          <tr>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
              {{ $t('tenant.member.email') }}
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
              {{ $t('tenant.member.role') }}
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
              {{ $t('tenant.member.invitedBy') }}
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
              {{ $t('tenant.member.invitedAt') }}
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
              {{ $t('tenant.member.expiresAt') }}
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
              {{ $t('tenant.member.status') }}
            </th>
            <th scope="col" class="relative px-6 py-3">
              <span class="sr-only">{{ $t('tenant.member.actions') }}</span>
            </th>
          </tr>
        </thead>
        <tbody class="bg-white dark:bg-gray-900 divide-y divide-gray-200 dark:divide-gray-700">
          <tr v-for="invitation in filteredInvitations" :key="invitation.id" class="hover:bg-gray-50 dark:hover:bg-gray-800">
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="text-sm text-gray-900 dark:text-white">{{ invitation.email }}</div>
              <div v-if="invitation.message" class="text-sm text-gray-500 dark:text-gray-400 truncate max-w-xs">
                {{ invitation.message }}
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200">
                {{ invitation.role }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="flex items-center">
                <div class="flex-shrink-0 h-8 w-8">
                  <img
                    :src="invitation.invitedByAvatar || defaultAvatar"
                    :alt="invitation.invitedByName"
                    class="h-8 w-8 rounded-full object-cover"
                    @error="handleAvatarError"
                  />
                </div>
                <div class="ml-3">
                  <div class="text-sm text-gray-900 dark:text-white">{{ invitation.invitedByName }}</div>
                </div>
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
              {{ formatDate(invitation.invitedAt) }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="flex items-center">
                <span class="text-sm text-gray-900 dark:text-white">{{ formatDate(invitation.expiresAt) }}</span>
                <span v-if="isExpiringSoon(invitation.expiresAt)" class="ml-2 inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200">
                  {{ $t('tenant.member.expiringSoon') }}
                </span>
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span :class="getStatusBadgeClass(invitation.status)">
                {{ getStatusLabel(invitation.status) }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
              <div class="flex justify-end space-x-2">
                <button
                  @click="resendInvitation(invitation)"
                  v-if="invitation.status === 'PENDING'"
                  class="text-blue-600 hover:text-blue-800 dark:text-blue-400 dark:hover:text-blue-300"
                  :title="$t('tenant.member.resend')"
                >
                  <Icon icon="mdi:email-send" class="h-4 w-4" />
                </button>
                <button
                  @click="revokeInvitation(invitation)"
                  v-if="invitation.status === 'PENDING'"
                  class="text-red-600 hover:text-red-800 dark:text-red-400 dark:hover:text-red-300"
                  :title="$t('tenant.member.revoke')"
                >
                  <Icon icon="mdi:delete" class="h-4 w-4" />
                </button>
                <button
                  @click="viewInvitation(invitation)"
                  class="text-primary hover:text-primary/80 dark:text-primary-400 dark:hover:text-primary-300"
                  :title="$t('tenant.member.viewDetails')"
                >
                  <Icon icon="mdi:eye" class="h-4 w-4" />
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <!-- Pagination -->
    <div v-if="totalPages > 1" class="mt-6 flex items-center justify-between">
      <div class="text-sm text-gray-700 dark:text-gray-300">
        Showing {{ (currentPage - 1) * pageSize + 1 }} to {{ Math.min(currentPage * pageSize, totalInvitations) }} of {{ totalInvitations }} invitations
      </div>
      <div class="flex space-x-2">
        <button
          @click="currentPage--"
          :disabled="currentPage === 1"
          class="px-3 py-1 text-sm border border-gray-300 rounded-md disabled:opacity-50 disabled:cursor-not-allowed dark:border-gray-600 dark:text-white"
        >
          {{ $t('common.previous') }}
        </button>
        <button
          @click="currentPage++"
          :disabled="currentPage === totalPages"
          class="px-3 py-1 text-sm border border-gray-300 rounded-md disabled:opacity-50 disabled:cursor-not-allowed dark:border-gray-600 dark:text-white"
        >
          {{ $t('common.next') }}
        </button>
      </div>
    </div>
  </div>
</template>
<script>
import { ref, computed, onMounted, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { tenantApi } from '@/api/tenantApi'
import { formatDate, isExpiringSoon } from '@/utils/dateUtils'
import { useGatewayTenantStore } from '@/stores/tenant/gateway/myTenantStore'
import defaultAvatar from '@/assets/img/user.jpg'
export default {
  name: 'InviteMemberTab',
  components: {
    Icon
  },
  emits: ['invitation-revoked'],
  setup(props, { emit }) {
    const tenantStore = useGatewayTenantStore()
    const loading = ref(false)
    const invitations = ref([])
    const searchQuery = ref('')
    const statusFilter = ref('')
    const currentPage = ref(1)
    const pageSize = ref(10)
    const totalInvitations = ref(0)
    const totalPages = computed(() => Math.ceil(totalInvitations.value / pageSize.value))
    const filteredInvitations = computed(() => {
      let filtered = invitations.value
      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        filtered = filtered.filter(invitation => 
          invitation.email.toLowerCase().includes(query) ||
          (invitation.message && invitation.message.toLowerCase().includes(query))
        )
      }
      if (statusFilter.value) {
        filtered = filtered.filter(invitation => invitation.status === statusFilter.value)
      }
      return filtered
    })
    const loadInvitations = async () => {
      loading.value = true
      try {
        // Mock data - replace with actual API call
        const mockInvitations = [
          {
            id: 1,
            email: 'newuser1@example.com',
            role: 'MEMBER',
            status: 'PENDING',
            message: 'Welcome to our team! Looking forward to working with you.',
            invitedBy: 'John Doe',
            invitedByName: 'John Doe',
            invitedByAvatar: null,
            invitedAt: '2024-01-18T10:30:00Z',
            expiresAt: '2024-01-25T10:30:00Z'
          },
          {
            id: 2,
            email: 'newuser2@example.com',
            role: 'EDITOR',
            status: 'PENDING',
            message: 'Join us as an editor to help manage content.',
            invitedBy: 'Jane Smith',
            invitedByName: 'Jane Smith',
            invitedByAvatar: null,
            invitedAt: '2024-01-17T14:15:00Z',
            expiresAt: '2024-01-24T14:15:00Z'
          },
          {
            id: 3,
            email: 'expired@example.com',
            role: 'VIEWER',
            status: 'EXPIRED',
            message: 'This invitation has expired.',
            invitedBy: 'John Doe',
            invitedByName: 'John Doe',
            invitedByAvatar: null,
            invitedAt: '2024-01-10T09:00:00Z',
            expiresAt: '2024-01-17T09:00:00Z'
          }
        ]
        invitations.value = mockInvitations
        totalInvitations.value = mockInvitations.length
        // In real implementation:
        // const response = await tenantApi.getPendingInvitations(tenantStore.currentTenant.id, {
        //   page: currentPage.value,
        //   size: pageSize.value,
        //   search: searchQuery.value,
        //   status: statusFilter.value
        // })
        // invitations.value = response.data.content || response.data
        // totalInvitations.value = response.data.totalElements || response.data.length
      } catch (error) {
      } finally {
        loading.value = false
      }
    }
    const resendInvitation = async (invitation) => {
      if (!confirm(`Resend invitation to ${invitation.email}?`)) {
        return
      }
      try {
        // In real implementation:
        // await tenantApi.resendInvitation(tenantStore.currentTenant.id, invitation.id)
        alert('Invitation resent successfully!')
      } catch (error) {
        alert('Failed to resend invitation. Please try again.')
      }
    }
    const revokeInvitation = async (invitation) => {
      if (!confirm(`Are you sure you want to revoke the invitation to ${invitation.email}?`)) {
        return
      }
      try {
        // In real implementation:
        // await tenantApi.revokeInvitation(tenantStore.currentTenant.id, invitation.id)
        invitations.value = invitations.value.filter(inv => inv.id !== invitation.id)
        totalInvitations.value -= 1
        emit('invitation-revoked', invitation)
      } catch (error) {
        alert('Failed to revoke invitation. Please try again.')
      }
    }
    const viewInvitation = (invitation) => {
      // In real implementation, open invitation details modal
    }
    const handleAvatarError = (event) => {
      event.target.src = defaultAvatar
    }
    const getStatusBadgeClass = (status) => {
      switch (status) {
        case 'PENDING':
          return 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200'
        case 'ACCEPTED':
          return 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
        case 'EXPIRED':
          return 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
        case 'REVOKED':
          return 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
        default:
          return 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
      }
    }
    const getStatusLabel = (status) => {
      switch (status) {
        case 'PENDING': return 'Pending'
        case 'ACCEPTED': return 'Accepted'
        case 'EXPIRED': return 'Expired'
        case 'REVOKED': return 'Revoked'
        default: return status
      }
    }
    // Watch for filter changes
    watch([searchQuery, statusFilter], () => {
      currentPage.value = 1
    })
    onMounted(() => {
      loadInvitations()
    })
    return {
      loading,
      invitations,
      searchQuery,
      statusFilter,
      currentPage,
      pageSize,
      totalInvitations,
      totalPages,
      filteredInvitations,
      defaultAvatar,
      resendInvitation,
      revokeInvitation,
      viewInvitation,
      handleAvatarError,
      isExpiringSoon,
      getStatusBadgeClass,
      getStatusLabel,
      formatDate
    }
  }
}
</script>
