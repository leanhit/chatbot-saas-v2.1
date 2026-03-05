<template>
  <div class="typed-member-tab">
    <!-- Role Filter -->
    <div class="mb-6 flex justify-end">
      <div class="flex gap-2">
        <select
          v-model="roleFilter"
          class="block px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary focus:border-primary dark:bg-gray-700 dark:border-gray-600 dark:text-white"
        >
          <option value="">{{ $t('tenant.member.allRoles') }}</option>
          <option value="OWNER">Owner</option>
          <option value="ADMIN">Admin</option>
          <option value="EDITOR">Editor</option>
          <option value="MEMBER">Member</option>
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
      <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">
        {{ $t('tenant.member.noMembers') }}
      </p>
      <p class="text-xs text-gray-500 dark:text-gray-500">
        {{ $t('tenant.member.noMembersDescription') }}
      </p>
    </div>

    <!-- Members List -->
    <div v-else class="space-y-4">
      <div
        v-for="member in filteredMembers"
        :key="member.id"
        class="bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg p-4 hover:shadow-md transition-shadow"
      >
        <!-- Member Header -->
        <div class="flex items-start justify-between mb-4">
          <div class="flex items-center space-x-3">
            <img
              :src="member.avatar || defaultAvatar"
              :alt="member.name"
              class="h-10 w-10 rounded-full object-cover"
              @error="handleAvatarError"
            />
            <div>
              <h4 class="text-sm font-medium text-gray-900 dark:text-white">
                {{ member.name || 'Unknown' }}
              </h4>
              <p class="text-xs text-gray-500 dark:text-gray-400">
                {{ member.email || 'No email' }}
              </p>
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
              v-if="canRemoveMember(member)"
              @click="removeMember(member)"
              class="p-1 text-gray-400 hover:text-red-600 dark:hover:text-red-400"
              :title="$t('tenant.member.removeMember')"
            >
              <Icon icon="mdi:delete" class="h-4 w-4" />
            </button>
          </div>
        </div>

        <!-- Member Details -->
        <div class="space-y-3">
          <!-- Role -->
          <div class="flex items-center justify-between">
            <span class="text-xs font-medium text-gray-700 dark:text-gray-300">
              {{ $t('tenant.member.role') }}
            </span>
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
              <option value="EDITOR">Editor</option>
              <option value="MEMBER">Member</option>
            </select>
          </div>

          <!-- Status -->
          <div class="flex items-center justify-between">
            <span class="text-xs font-medium text-gray-700 dark:text-gray-300">
              {{ $t('tenant.member.status') }}
            </span>
            <span :class="getStatusBadgeClass(member.status)">
              {{ getStatusLabel(member.status) }}
            </span>
          </div>

          <!-- Joined Date -->
          <div class="flex items-center justify-between">
            <span class="text-xs font-medium text-gray-700 dark:text-gray-300">
              {{ $t('tenant.member.joinedAt') }}
            </span>
            <span class="text-xs text-gray-500 dark:text-gray-400">
              {{ formatDate(member.joinedAt) }}
            </span>
          </div>

          <!-- Additional Info -->
          <div v-if="member.department || member.position" class="flex items-center justify-between">
            <span class="text-xs font-medium text-gray-700 dark:text-gray-300">
              Position
            </span>
            <span class="text-xs text-gray-500 dark:text-gray-400">
              {{ member.position || '' }}{{ member.department ? ` - ${member.department}` : '' }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="mt-6 flex justify-center">
      <nav class="flex items-center space-x-2">
        <button
          @click="currentPage = Math.max(1, currentPage - 1)"
          :disabled="currentPage === 1"
          class="px-3 py-1 text-sm border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed dark:border-gray-600 dark:hover:bg-gray-700"
        >
          Previous
        </button>
        <span class="text-sm text-gray-600 dark:text-gray-400">
          Page {{ currentPage }} of {{ totalPages }}
        </span>
        <button
          @click="currentPage = Math.min(totalPages, currentPage + 1)"
          :disabled="currentPage === totalPages"
          class="px-3 py-1 text-sm border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed dark:border-gray-600 dark:hover:bg-gray-700"
        >
          Next
        </button>
      </nav>
    </div>
  </div>
</template>

<script lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { tenantApi } from '@/api/tenantApi'
import { formatDate, getRelativeTime } from '@/utils/dateUtils'
import { useGatewayTenantStore } from '@/stores/tenant/gateway/myTenantStore'
import type { MemberResponse, TenantRole, MembershipStatus } from '@/types/tenant'
import { 
  sanitizeMemberArray, 
  getRoleLabel, 
  getStatusLabel, 
  getStatusBadgeClass,
  getRoleBadgeClass,
  canModifyRole,
  filterMembersByQuery,
  filterMembersByRole,
  isValidTenantRole,
  isValidMembershipStatus
} from '@/utils/tenantValidators'
import defaultAvatar from '@/assets/img/user.jpg'

export default {
  name: 'TypedMemberTab',
  props: {
    searchQuery: {
      type: String,
      default: ''
    }
  },
  components: {
    Icon
  },
  emits: ['member-removed', 'member-updated'],
  setup(props, { emit }) {
    const tenantStore = useGatewayTenantStore()
    const loading = ref(false)
    const members = ref([])
    const roleFilter = ref('')
    const currentPage = ref(1)
    const pageSize = ref(12)
    const totalMembers = ref(0)
    
    // Defensive check for tenantStore
    if (!tenantStore) {
      console.error('TenantStore is not available')
      return {
        loading,
        members,
        roleFilter,
        currentPage,
        pageSize,
        totalMembers,
        totalPages: computed(() => 0),
        filteredMembers: computed(() => []),
        handleRoleChange: () => {},
        removeMember: () => {},
        viewDetails: () => {},
        handleAvatarError: () => {},
        canChangeRole: () => false,
        getStatusBadgeClass: () => '',
        getStatusLabel: () => '',
        formatDate: () => '',
        getRelativeTime: () => '',
        canRemoveMember: () => false
      }
    }
    
    const totalPages = computed(() => Math.ceil(totalMembers.value / pageSize.value))
    
    // Computed filtered members with type safety
    const filteredMembers = computed(() => {
      let filtered = members.value
      
      // Apply search query with validator
      if (props.searchQuery) {
        filtered = filterMembersByQuery(filtered, props.searchQuery)
      }
      
      // Apply role filter with type safety
      if (roleFilter.value) {
        filtered = filterMembersByRole(filtered, roleFilter.value)
      }
      
      return filtered
    })
    
    // Type-safe API call
    const loadMembers = async () => {
      loading.value = true
      try {
        // Get tenantKey from current tenant
        const tenantKey = tenantStore.currentTenant?.tenantKey
        if (!tenantKey) {
          console.warn('No tenant selected or tenant not loaded')
          members.value = []
          totalMembers.value = 0
          return
        }

        // Call API with typed response
        const response = await tenantApi.getTenantMembers(tenantKey)
        
        // Sanitize and validate member data
        const sanitizedMembers = sanitizeMemberArray(response.data)
        members.value = sanitizedMembers
        totalMembers.value = sanitizedMembers.length
        
        console.log(`Loaded ${sanitizedMembers.length} members for tenant ${tenantKey}`)
      } catch (error) {
        console.error('Failed to load members:', error)
        console.error('Error details:', {
          message: error.message,
          status: error.response?.status,
          statusText: error.response?.statusText,
          data: error.response?.data,
          config: error.config
        })
        
        // Show user-friendly error message
        if (error.response?.status === 409) {
          console.warn('Conflict error - possibly duplicate request or state conflict')
        } else if (error.response?.status === 401) {
          console.warn('Unauthorized - token may be expired')
        } else if (error.response?.status === 403) {
          console.warn('Forbidden - insufficient permissions')
        } else if (error.response?.status === 404) {
          console.warn('Not found - tenant or endpoint not found')
        }
        
        members.value = []
        totalMembers.value = 0
      } finally {
        loading.value = false
      }
    }
    
    // Type-safe role change handler
    const handleRoleChange = async (member, newRole) => {
      // Validate new role
      if (!isValidTenantRole(newRole)) {
        console.error(`Invalid role: ${newRole}`)
        return
      }
      
      // Check permissions
      if (!canChangeRole(member.role)) {
        console.warn('Cannot change role - insufficient permissions')
        return
      }
      
      try {
        // Get tenantKey from current tenant
        const tenantKey = tenantStore.currentTenant?.tenantKey
        if (!tenantKey) {
          alert('No tenant selected')
          return
        }

        // Call API with typed parameters
        await tenantApi.updateMemberRole(tenantKey, member.id, newRole as TenantRole)
        
        // Update local member data
        member.role = newRole as TenantRole
        emit('member-updated', member)
        
        console.log(`Updated role for member ${member.id} to ${newRole}`)
      } catch (error) {
        console.error('Failed to update member role:', error)
        alert('Failed to update member role. Please try again.')
      }
    }
    
    // Type-safe member removal
    const removeMember = async (member) => {
      if (!confirm(`Are you sure you want to remove ${member.name || 'this member'} from this tenant?`)) {
        return
      }
      
      try {
        // Get tenantKey from current tenant
        const tenantKey = tenantStore.currentTenant?.tenantKey
        if (!tenantKey) {
          alert('No tenant selected')
          return
        }

        // Call API
        await tenantApi.removeMember(tenantKey, member.id)
        
        // Update local state
        const index = members.value.findIndex(m => m.id === member.id)
        if (index > -1) {
          members.value.splice(index, 1)
          totalMembers.value -= 1
        }
        
        emit('member-removed', member)
        console.log(`Removed member ${member.id} from tenant`)
      } catch (error) {
        console.error('Failed to remove member:', error)
        alert('Failed to remove member. Please try again.')
      }
    }
    
    const viewDetails = (member) => {
      // In real implementation, open member details modal or navigate to details page
      console.log('View member details:', member)
    }
    
    const handleAvatarError = (event) => {
      if (event?.target) {
        event.target.src = defaultAvatar
      }
    }
    
    // Permission checks
    const canChangeRole = (role) => {
      if (!role) return false
      return canModifyRole('ADMIN', role) // Assuming current user is ADMIN
    }
    
    const canRemoveMember = (member) => {
      return member.role !== 'OWNER' && canChangeRole(member.role)
    }
    
    // Watch for filter changes
    watch([() => props.searchQuery, roleFilter], () => {
      currentPage.value = 1
    })
    
    // Load data on mount
    onMounted(() => {
      loadMembers()
    })
    
    return {
      loading,
      members,
      roleFilter,
      currentPage,
      pageSize,
      totalMembers,
      totalPages,
      filteredMembers,
      handleRoleChange,
      removeMember,
      viewDetails,
      handleAvatarError,
      canChangeRole,
      getStatusBadgeClass,
      getStatusLabel,
      formatDate,
      getRelativeTime,
      canRemoveMember
    }
  }
}
</script>

<style scoped>
.typed-member-tab {
  /* Component-specific styles */
}
</style>
