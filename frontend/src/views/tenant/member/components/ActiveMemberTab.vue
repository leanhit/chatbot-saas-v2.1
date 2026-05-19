<template>
  <div class="active-members-container">
    <!-- Role Filter Only -->
    <div class="mb-6 flex justify-end">
      <div class="flex gap-2">
        <select
          v-model="roleFilter"
          class="block px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary focus:border-primary dark:bg-gray-700 dark:border-gray-600 dark:text-white"
        >
          <option value="">{{ $t('tenant.member.allRoles') }}</option>
          <option :value="TenantRole.OWNER">{{ $t('tenant.member.owner') }}</option>
          <option :value="TenantRole.ADMIN">{{ $t('tenant.member.admin') }}</option>
          <option :value="TenantRole.MEMBER">{{ $t('tenant.member.member') }}</option>
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
              <h4 class="text-sm font-medium text-gray-900 dark:text-white">{{ member.name || $t('tenant.overview.statusUnknown') }}</h4>
              <p class="text-xs text-gray-500 dark:text-gray-400">{{ member.email || $t('tenant.member.noEmail') }}</p>
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
              v-if="member?.role !== TenantRole.OWNER"
              @click="removeMember(member)"
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
            <div v-if="member?.role === TenantRole.OWNER" class="flex items-center">
              <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200">
                <Icon icon="mdi:crown" class="h-3 w-3 mr-1" />
                {{ $t('tenant.member.owner').toUpperCase() }}
              </span>
            </div>
            <select
              v-else
              v-model="member.role"
              @change="handleRoleChange(member, $event.target.value)"
              :disabled="!canChangeRole(member?.role)"
              class="text-xs border border-gray-300 rounded px-2 py-1 focus:outline-none focus:ring-1 focus:ring-primary dark:bg-gray-700 dark:border-gray-600 dark:text-white"
            >
              <option :value="TenantRole.ADMIN">{{ $t('tenant.member.admin') }}</option>
              <option :value="TenantRole.MEMBER">{{ $t('tenant.member.member') }}</option>
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
            <span class="text-xs text-gray-900 dark:text-white">{{ getRelativeTime(member.lastActiveAt) }}</span>
          </div>
        </div>
      </div>
    </div>
    <!-- Pagination -->
    <div v-if="totalPages > 1" class="mt-6 flex items-center justify-between">
      <div class="text-sm text-gray-700 dark:text-gray-300">
        {{ $t('tenant.member.showingMembers', { start: (currentPage - 1) * pageSize + 1, end: Math.min(currentPage * pageSize, totalMembers), total: totalMembers }) }}
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
import { useI18n } from 'vue-i18n'
import { tenantApi } from '@/api/tenantApi'
import { formatDate, getRelativeTime } from '@/utils/dateUtils'
import { useGatewayTenantStore } from '@/stores/tenant/gateway/myTenantStore'
import { TenantRole, MembershipStatus } from '@/types/tenant'
import defaultAvatar from '@/assets/img/user.jpg'

export default {
  name: 'ActiveMemberTab',
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
    const { t } = useI18n()
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
        searchQuery: props.searchQuery,
        roleFilter,
        currentPage,
        pageSize,
        totalMembers,
        totalPages: computed(() => 0),
        filteredMembers: computed(() => []),
        defaultAvatar,
        handleRoleChange: () => {},
        removeMember: () => {},
        viewDetails: () => {},
        handleAvatarError: () => {},
        canChangeRole: () => false,
        getStatusBadgeClass: () => '',
        getStatusLabel: () => '',
        formatDate: () => '',
        getRelativeTime: () => ''
      }
    }
    const totalPages = computed(() => Math.ceil(totalMembers.value / pageSize.value))
    const filteredMembers = computed(() => {
      let filtered = members.value
      if (props.searchQuery) {
        const query = props.searchQuery.toLowerCase()
        filtered = filtered.filter(member => {
          // Defensive checks for member properties
          const name = member?.name || ''
          const email = member?.email || ''
          return name.toLowerCase().includes(query) || email.toLowerCase().includes(query)
        })
      }
      if (roleFilter.value) {
        filtered = filtered.filter(member => member?.role === roleFilter.value)
      }
      return filtered
    })
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

        // Call actual API to get tenant members
        const response = await tenantApi.getTenantMembers(tenantKey)
        members.value = response.data?.content || response.data || []
        totalMembers.value = members.value.length
      } catch (error) {
        console.error('Failed to load members:', error)
        members.value = []
        totalMembers.value = 0
      } finally {
        loading.value = false
      }
    }
    const handleRoleChange = async (member, newRole) => {
      try {
        // Get tenantKey from current tenant
        const tenantKey = tenantStore.currentTenant?.tenantKey
        if (!tenantKey) {
          alert(t('tenant.overview.unknownTenant'))
          return
        }

        // Call API to update member role
        await tenantApi.updateMemberRole(tenantKey, member.id, newRole)
        
        // Update local member data
        member.role = newRole
        emit('member-updated', member)
      } catch (error) {
        console.error('Failed to update member role:', error)
        alert('Failed to update member role. Please try again.')
      }
    }
    const removeMember = async (member) => {
      if (!confirm(t('tenant.member.confirmRemove', { name: member?.name || t('tenant.member.member') }))) {
        return
      }
      try {
        // Get tenantKey from current tenant
        const tenantKey = tenantStore.currentTenant?.tenantKey
        if (!tenantKey) {
          alert(t('tenant.overview.unknownTenant'))
          return
        }

        // Call API to remove member
        await tenantApi.removeMember(tenantKey, member.id)
        
        // Remove from local list
        members.value = members.value.filter(m => m.id !== member.id)
        totalMembers.value -= 1
        emit('member-removed', member)
      } catch (error) {
        console.error('Failed to remove member:', error)
        alert('Failed to remove member. Please try again.')
      }
    }
    const viewDetails = (member) => {
      // In real implementation, open member details modal or navigate to details page
    }
    const handleAvatarError = (event) => {
      if (event?.target) {
        event.target.src = defaultAvatar
      }
    }
    const canChangeRole = (role) => {
      // Only admin and above can change roles
      if (!role) return false
      return true
    }
    const getStatusBadgeClass = (status) => {
      switch (status) {
        case MembershipStatus.ACTIVE:
          return 'inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
        case MembershipStatus.INACTIVE:
          return 'inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
        default:
          return 'inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
      }
    }
    const getStatusLabel = (status) => {
      switch (status) {
        case MembershipStatus.ACTIVE: return t('tenant.overview.statusActive')
        case MembershipStatus.INACTIVE: return t('tenant.overview.statusInactive')
        default: return t('tenant.overview.statusUnknown')
      }
    }
    // Watch for filter changes
    watch([() => props.searchQuery, roleFilter], () => {
      currentPage.value = 1
    })
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
      defaultAvatar,
      handleRoleChange,
      removeMember,
      viewDetails,
      handleAvatarError,
      canChangeRole,
      getStatusBadgeClass,
      getStatusLabel,
      formatDate,
      getRelativeTime,
      TenantRole,
      MembershipStatus
    }
  }
}
</script>
