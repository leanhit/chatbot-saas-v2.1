<template>
  <div class="pending-members-container">
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
            :placeholder="$t('tenant.member.searchRequests')"
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
import { useGatewayTenantStore } from '@/stores/tenant/gateway/myTenantStore'
import defaultAvatar from '@/assets/img/user.jpg'

export default {
  name: 'PendingMemberTab',
  components: {
    Icon
  },
  emits: ['request-approved', 'request-rejected'],
  setup(props, { emit }) {
    const tenantStore = useGatewayTenantStore()
    
    const loading = ref(false)
    const requests = ref([])
    const searchQuery = ref('')
    const statusFilter = ref('')
    const currentPage = ref(1)
    const pageSize = ref(10)
    const totalRequests = ref(0)

    const totalPages = computed(() => Math.ceil(totalRequests.value / pageSize.value))

    const filteredRequests = computed(() => {
      let filtered = requests.value

      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
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
      if (!tenantStore.currentTenant?.id) return
      
      loading.value = true
      try {
        // Call real API for join requests
        const response = await tenantApi.getTenantJoinRequests(tenantStore.currentTenant.id)
        
        // Map backend response to frontend format
        const requestsData = Array.isArray(response.data) 
          ? response.data 
          : response.data?.content || []
        
        requests.value = requestsData.map(request => ({
          id: request.id,
          name: request.email?.split('@')[0] || request.name || 'Unknown',
          email: request.email,
          role: request.requestedRole || 'MEMBER',
          avatar: defaultAvatar,
          requestedAt: request.createdAt || request.requestedAt,
          status: request.status || 'PENDING',
          message: request.message || ''
        }))
        
        totalRequests.value = requests.value.length
        
      } catch (error) {
        console.error('Error loading join requests:', error)
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
        // Call real API
        await tenantApi.approveJoinRequest(tenantStore.currentTenant.id, request.id)
        
        // Update local state
        requests.value = requests.value.filter(r => r.id !== request.id)
        totalRequests.value = requests.value.length
        
        emit('request-approved', request)
      } catch (error) {
        console.error('Error approving request:', error)
        alert('Failed to approve request. Please try again.')
      }
    }

    const rejectRequest = async (request) => {
      if (!confirm(`Are you sure you want to reject ${request.name}'s request?`)) {
        return
      }

      try {
        // Call real API
        await tenantApi.rejectJoinRequest(tenantStore.currentTenant.id, request.id)
        
        // Update local state
        requests.value = requests.value.filter(r => r.id !== request.id)
        totalRequests.value = requests.value.length
        
        emit('request-rejected', request)
      } catch (error) {
        console.error('Error rejecting request:', error)
        alert('Failed to reject request. Please try again.')
      }
    }

    const viewRequest = (request) => {
      // In real implementation, open request details modal
      console.log('View request details:', request)
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

    const formatDate = (dateString) => {
      if (!dateString) return 'N/A'
      return new Date(dateString).toLocaleDateString()
    }

    // Watch for filter changes
    watch([searchQuery, statusFilter], () => {
      currentPage.value = 1
    })

    onMounted(() => {
      loadRequests()
    })

    return {
      loading,
      requests,
      searchQuery,
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
