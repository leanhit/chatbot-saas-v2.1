<template>
  <div class="pending-members-container">
    <!-- Status Filter Only -->
    <div class="mb-6 flex justify-end">
      <div class="flex gap-2">
        <select
          v-model="statusFilter"
          class="block px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary focus:border-primary dark:bg-gray-700 dark:border-gray-600 dark:text-white"
        >
          <option value="">{{ $t('tenant.member.allStatuses') }}</option>
          <option value="PENDING">Pending</option>
          <option value="REVIEWING">Reviewing</option>
        </select>
      </div>
    </div>
    <!-- Loading State -->
    <div v-if="loading" class="text-center py-12">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">{{ $t('tenant.member.loading') }}</p>
    </div>
    <!-- Empty State -->
    <div v-else-if="filteredRequests.length === 0" class="text-center py-12">
      <Icon icon="mdi:account-clock" class="mx-auto h-12 w-12 text-gray-400" />
      <h3 class="mt-2 text-sm font-medium text-gray-900 dark:text-white">{{ $t('tenant.member.noPendingRequests') }}</h3>
      <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">{{ $t('tenant.member.noPendingRequestsDescription') }}</p>
    </div>
    <!-- Requests Table -->
    <div v-else class="overflow-hidden shadow ring-1 ring-black ring-opacity-5 md:rounded-lg">
      <table class="min-w-full divide-y divide-gray-300 dark:divide-gray-700">
        <thead class="bg-gray-50 dark:bg-gray-800">
          <tr>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
              {{ $t('tenant.member.requester') }}
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
              {{ $t('tenant.member.email') }}
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
              {{ $t('tenant.member.requestedRole') }}
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
              {{ $t('tenant.member.requestedAt') }}
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
          <tr v-for="request in filteredRequests" :key="request.id" class="hover:bg-gray-50 dark:hover:bg-gray-800">
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="flex items-center">
                <div class="flex-shrink-0 h-10 w-10">
                  <img
                    :src="request.avatar || defaultAvatar"
                    :alt="request.name"
                    class="h-10 w-10 rounded-full object-cover"
                    @error="handleAvatarError"
                  />
                </div>
                <div class="ml-4">
                  <div class="text-sm font-medium text-gray-900 dark:text-white">{{ request.name }}</div>
                  <div class="text-sm text-gray-500 dark:text-gray-400">{{ request.message }}</div>
                </div>
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="text-sm text-gray-900 dark:text-white">{{ request.email }}</div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200">
                {{ request.requestedRole }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
              {{ formatDate(request.requestedAt) }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span :class="getStatusBadgeClass(request.status)">
                {{ getStatusLabel(request.status) }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
              <div class="flex justify-end space-x-2">
                <button
                  @click="viewRequest(request)"
                  class="text-primary hover:text-primary/80 dark:text-primary-400 dark:hover:text-primary-300"
                  :title="$t('tenant.member.viewDetails')"
                >
                  <Icon icon="mdi:eye" class="h-4 w-4" />
                </button>
                <button
                  v-if="request.status === 'PENDING'"
                  @click="approveRequest(request)"
                  class="text-green-600 hover:text-green-800 dark:text-green-400 dark:hover:text-green-300"
                  :title="$t('tenant.member.approve')"
                >
                  <Icon icon="mdi:check" class="h-4 w-4" />
                </button>
                <button
                  v-if="request.status === 'PENDING'"
                  @click="rejectRequest(request)"
                  class="text-red-600 hover:text-red-800 dark:text-red-400 dark:hover:text-red-300"
                  :title="$t('tenant.member.reject')"
                >
                  <Icon icon="mdi:close" class="h-4 w-4" />
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
        Showing {{ (currentPage - 1) * pageSize + 1 }} to {{ Math.min(currentPage * pageSize, totalRequests) }} of {{ totalRequests }} requests
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
import { formatDate } from '@/utils/dateUtils'
import { useGatewayTenantStore } from '@/stores/tenant/gateway/myTenantStore'
import defaultAvatar from '@/assets/img/user.jpg'
export default {
  name: 'PendingMemberTab',
  props: {
    searchQuery: {
      type: String,
      default: ''
    }
  },
  components: {
    Icon
  },
  emits: ['request-approved', 'request-rejected'],
  setup(props, { emit }) {
    const tenantStore = useGatewayTenantStore()
    const loading = ref(false)
    const requests = ref([])
    const statusFilter = ref('')
    const currentPage = ref(1)
    const pageSize = ref(10)
    const totalRequests = ref(0)
    const totalPages = computed(() => Math.ceil(totalRequests.value / pageSize.value))
    const filteredRequests = computed(() => {
      let filtered = requests.value
      if (props.searchQuery) {
        const query = props.searchQuery.toLowerCase()
        filtered = filtered.filter(request => 
          request.name.toLowerCase().includes(query) ||
          request.email.toLowerCase().includes(query) ||
          request.message.toLowerCase().includes(query)
        )
      }
      if (statusFilter.value) {
        filtered = filtered.filter(request => request.status === statusFilter.value)
      }
      return filtered
    })
    const loadRequests = async () => {
      loading.value = true
      try {
        // Get tenantKey from current tenant
        const tenantKey = tenantStore.currentTenant?.tenantKey
        if (!tenantKey) {
          console.error('No tenant selected')
          return
        }

        // Call actual API to get pending join requests
        const response = await tenantApi.getJoinRequests(tenantKey)
        requests.value = response.data || []
        totalRequests.value = requests.value.length
      } catch (error) {
        console.error('Failed to load pending requests:', error)
        requests.value = []
        totalRequests.value = 0
      } finally {
        loading.value = false
      }
    }
    const approveRequest = async (request) => {
      if (!confirm(`Are you sure you want to approve ${request.name}'s request?`)) {
        return
      }
      try {
        // Get tenantKey from current tenant
        const tenantKey = tenantStore.currentTenant?.tenantKey
        if (!tenantKey) {
          alert('No tenant selected')
          return
        }

        // Call API to approve request
        await tenantApi.updateJoinRequestStatus(tenantKey, request.id, 'APPROVED')
        
        // Remove from local list
        requests.value = requests.value.filter(r => r.id !== request.id)
        emit('request-approved', request)
      } catch (error) {
        console.error('Failed to approve request:', error)
        alert('Failed to approve request. Please try again.')
      }
    }
    const rejectRequest = async (request) => {
      const reason = prompt(`Why are you rejecting ${request.name}'s request? (Optional)`)
      if (reason === null) return // User cancelled
      try {
        // Get tenantKey from current tenant
        const tenantKey = tenantStore.currentTenant?.tenantKey
        if (!tenantKey) {
          alert('No tenant selected')
          return
        }

        // Call API to reject request
        await tenantApi.updateJoinRequestStatus(tenantKey, request.id, 'REJECTED')
        
        // Remove from local list
        requests.value = requests.value.filter(r => r.id !== request.id)
        emit('request-rejected', request)
      } catch (error) {
        console.error('Failed to reject request:', error)
        alert('Failed to reject request. Please try again.')
      }
    }
    const viewRequest = (request) => {
      // In real implementation, open request details modal
    }
    const handleAvatarError = (event) => {
      event.target.src = defaultAvatar
    }
    const getStatusBadgeClass = (status) => {
      switch (status) {
        case 'PENDING':
          return 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200'
        case 'REVIEWING':
          return 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200'
        case 'APPROVED':
          return 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
        case 'REJECTED':
          return 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
        default:
          return 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
      }
    }
    const getStatusLabel = (status) => {
      switch (status) {
        case 'PENDING': return 'Pending'
        case 'REVIEWING': return 'Reviewing'
        case 'APPROVED': return 'Approved'
        case 'REJECTED': return 'Rejected'
        default: return status
      }
    }
    // Watch for filter changes
    watch([() => props.searchQuery, statusFilter], () => {
      currentPage.value = 1
    })
    onMounted(() => {
      loadRequests()
    })
    return {
      loading,
      requests,
      statusFilter,
      currentPage,
      pageSize,
      totalRequests,
      totalPages,
      filteredRequests,
      defaultAvatar,
      approveRequest,
      rejectRequest,
      viewRequest,
      handleAvatarError,
      getStatusBadgeClass,
      getStatusLabel,
      formatDate
    }
  }
}
</script>
