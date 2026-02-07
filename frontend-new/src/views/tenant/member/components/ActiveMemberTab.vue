<template>
  <div class="active-members-container">
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
            :placeholder="$t('tenant.member.searchMembers')"
            class="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md leading-5 bg-white placeholder-gray-500 focus:outline-none focus:placeholder-gray-400 focus:ring-1 focus:ring-primary focus:border-primary dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white"
          />
        </div>
      </div>
      <div class="flex gap-2">
        <select
          v-model="roleFilter"
          class="block px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary focus:border-primary dark:bg-gray-700 dark:border-gray-600 dark:text-white"
        >
          <option value="">{{ $t('tenant.member.allRoles') }}</option>
          <option value="OWNER">Owner</option>
          <option value="ADMIN">Admin</option>
          <option value="MANAGER">Manager</option>
          <option value="MEMBER">Member</option>
          <option value="EDITOR">Editor</option>
          <option value="VIEWER">Viewer</option>
        </select>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="text-center py-12">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">{{ $t('tenant.member.loading') }}</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="filteredMembers.length === 0" class="text-center py-12">
      <Icon icon="mdi:account-group" class="mx-auto h-12 w-12 text-gray-400" />
      <h3 class="mt-2 text-sm font-medium text-gray-900 dark:text-white">{{ $t('tenant.member.noMembers') }}</h3>
      <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">{{ $t('tenant.member.noMembersDescription') }}</p>
    </div>

    <!-- Members Grid -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <div
        v-for="member in filteredMembers"
        :key="member.id"
        class="bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg p-4 hover:shadow-md transition-shadow"
      >
        <div class="flex items-start justify-between mb-4">
          <div class="flex items-center space-x-3">
            <img
              :src="member.avatar || defaultAvatar"
              :alt="member.name"
              class="h-10 w-10 rounded-full object-cover"
              @error="handleAvatarError"
            />
            <div>
              <h4 class="text-sm font-medium text-gray-900 dark:text-white">{{ member.name }}</h4>
              <p class="text-xs text-gray-500 dark:text-gray-400">{{ member.email }}</p>
            </div>
          </div>
          <div class="flex items-center space-x-1">
            <button
              @click="viewDetails(member)"
              class="p-1 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
              :title="$t('tenant.member.viewDetails')"
            >
              <Icon icon="mdi:eye" class="h-4 w-4" />
            </button>
            <button
              v-if="member.role !== 'OWNER'"
              @click="handleRemove(member)"
              class="p-1 text-gray-400 hover:text-red-600 dark:hover:text-red-400"
              :title="$t('tenant.member.removeMember')"
            >
              <Icon icon="mdi:delete" class="h-4 w-4" />
            </button>
          </div>
        </div>
        
        <div class="space-y-3">
          <!-- Role Selection -->
          <div class="flex items-center justify-between">
            <span class="text-xs font-medium text-gray-700 dark:text-gray-300">{{ $t('tenant.member.role') }}</span>
            <div v-if="member.role === 'OWNER'" class="flex items-center">
              <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200">
                <Icon icon="mdi:crown" class="h-3 w-3 mr-1" />
                OWNER
              </span>
            </div>
            <select
              v-else
              v-model="member.role"
              @change="handleRoleChange(member, $event.target.value)"
              :disabled="!canChangeRole(member.role)"
              class="text-xs border border-gray-300 rounded px-2 py-1 focus:outline-none focus:ring-1 focus:ring-primary dark:bg-gray-700 dark:border-gray-600 dark:text-white"
            >
              <option value="ADMIN">Admin</option>
              <option value="MANAGER">Manager</option>
              <option value="MEMBER">Member</option>
              <option value="EDITOR">Editor</option>
              <option value="VIEWER">Viewer</option>
            </select>
          </div>
          
          <!-- Status -->
          <div class="flex items-center justify-between">
            <span class="text-xs font-medium text-gray-700 dark:text-gray-300">{{ $t('tenant.member.status') }}</span>
            <span :class="getStatusBadgeClass(member.status)">
              {{ getStatusLabel(member.status) }}
            </span>
          </div>
          
          <!-- Joined Date -->
          <div v-if="member.joinedAt" class="flex items-center justify-between">
            <span class="text-xs font-medium text-gray-700 dark:text-gray-300">{{ $t('tenant.member.joinedAt') }}</span>
            <span class="text-xs text-gray-900 dark:text-white">{{ formatDate(member.joinedAt) }}</span>
          </div>
          
          <!-- Last Active -->
          <div v-if="member.lastActiveAt" class="flex items-center justify-between">
            <span class="text-xs font-medium text-gray-700 dark:text-gray-300">{{ $t('tenant.member.lastActive') }}</span>
            <span class="text-xs text-gray-900 dark:text-white">{{ formatLastActive(member.lastActiveAt) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="mt-6 flex items-center justify-between">
      <div class="text-sm text-gray-700 dark:text-gray-300">
        Showing {{ (currentPage - 1) * pageSize + 1 }} to {{ Math.min(currentPage * pageSize, totalMembers) }} of {{ totalMembers }} members
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
  name: 'ActiveMemberTab',
  components: {
    Icon
  },
  emits: ['member-removed', 'member-updated'],
  setup(props, { emit }) {
    const tenantStore = useGatewayTenantStore()
    
    const loading = ref(false)
    const members = ref([])
    const searchQuery = ref('')
    const roleFilter = ref('')
    const currentPage = ref(1)
    const pageSize = ref(12)
    const totalMembers = ref(0)

    const totalPages = computed(() => Math.ceil(totalMembers.value / pageSize.value))

    const filteredMembers = computed(() => {
      let filtered = members.value

      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        filtered = filtered.filter(member => 
          member.name.toLowerCase().includes(query) ||
          member.email.toLowerCase().includes(query)
        )
      }

      if (roleFilter.value) {
        filtered = filtered.filter(member => member.role === roleFilter.value)
      }

      return filtered
    })

    const loadMembers = async () => {
      if (!tenantStore.currentTenant?.id) return
      
      loading.value = true
      try {
        // Call real API
        const response = await tenantApi.getTenantMembers(tenantStore.currentTenant.id)
        
        // Map backend response to frontend format
        const membersData = Array.isArray(response.data) 
          ? response.data 
          : response.data?.content || []
        
        members.value = membersData.map(member => ({
          id: member.userId,
          name: member.email.split('@')[0], // Extract name from email
          email: member.email,
          role: member.role,
          avatar: defaultAvatar,
          joinedAt: member.joinedAt,
          status: 'ACTIVE'
        }))
        
        totalMembers.value = members.value.length
        
      } catch (error) {
        console.error('Error loading members:', error)
        members.value = []
        totalMembers.value = 0
      } finally {
        loading.value = false
      }
    }

    const handleRoleChange = async (member, newRole) => {
      try {
        // Call real API
        await tenantApi.updateTenantMemberRole(tenantStore.currentTenant.id, member.id, newRole)
        
        // Update local state
        const memberIndex = members.value.findIndex(m => m.id === member.id)
        if (memberIndex !== -1) {
          members.value[memberIndex].role = newRole
        }
        
        emit('member-updated', { ...member, role: newRole })
      } catch (error) {
        console.error('Error updating member role:', error)
        alert('Failed to update member role. Please try again.')
      }
    }

    const handleRemove = async (member) => {
      if (!confirm(`Are you sure you want to remove ${member.name} from the tenant?`)) {
        return
      }

      try {
        // Call real API
        await tenantApi.removeTenantMember(tenantStore.currentTenant.id, member.id)
        
        // Update local state
        members.value = members.value.filter(m => m.id !== member.id)
        totalMembers.value = members.value.length
        
        emit('member-removed', member)
      } catch (error) {
        console.error('Error removing member:', error)
        alert('Failed to remove member. Please try again.')
      }
    }

    const viewDetails = (member) => {
      // In real implementation, open member details modal or navigate to details page
      console.log('View details for:', member)
    }

    const handleAvatarError = (event) => {
      event.target.src = defaultAvatar
    }

    const canChangeRole = (role) => {
      // Only admin and above can change roles
      // In real implementation, check current user permissions
      return true
    }

    const getStatusBadgeClass = (status) => {
      switch (status) {
        case 'ACTIVE':
          return 'inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
        case 'INACTIVE':
          return 'inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
        default:
          return 'inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
      }
    }

    const getStatusLabel = (status) => {
      switch (status) {
        case 'ACTIVE': return 'Active'
        case 'INACTIVE': return 'Inactive'
        default: return status
      }
    }

    const formatDate = (dateString) => {
      if (!dateString) return 'N/A'
      return new Date(dateString).toLocaleDateString()
    }

    const formatLastActive = (dateString) => {
      if (!dateString) return 'Never'
      const date = new Date(dateString)
      const now = new Date()
      const diffMs = now - date
      const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))
      
      if (diffDays === 0) return 'Today'
      if (diffDays === 1) return 'Yesterday'
      if (diffDays < 7) return `${diffDays} days ago`
      if (diffDays < 30) return `${Math.floor(diffDays / 7)} weeks ago`
      return formatDate(dateString)
    }

    // Watch for filter changes
    watch([searchQuery, roleFilter], () => {
      currentPage.value = 1
    })

    onMounted(() => {
      loadMembers()
    })

    return {
      loading,
      members,
      searchQuery,
      roleFilter,
      currentPage,
      pageSize,
      totalMembers,
      totalPages,
      filteredMembers,
      defaultAvatar,
      handleRoleChange,
      handleRemove,
      viewDetails,
      handleAvatarError,
      canChangeRole,
      getStatusBadgeClass,
      getStatusLabel,
      formatDate,
      formatLastActive
    }
  }
}
</script>
