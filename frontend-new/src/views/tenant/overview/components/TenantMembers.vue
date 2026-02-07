<template>
  <div class="bg-white dark:bg-gray-800 shadow rounded-lg p-6">
    <div class="flex justify-between items-center mb-6">
      <h3 class="text-lg font-medium text-gray-900 dark:text-white">{{ $t('tenant.overview.members') }}</h3>
      <div class="flex gap-2">
        <button
          @click="handleInvite"
          class="inline-flex items-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500"
        >
          <Icon icon="mdi:account-plus" class="h-4 w-4 mr-1" />
          {{ $t('tenant.overview.inviteMember') }}
        </button>
        <button
          @click="handleRefresh"
          class="inline-flex items-center px-3 py-2 border border-gray-300 text-sm leading-4 font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary dark:bg-gray-700 dark:text-gray-300 dark:border-gray-600 dark:hover:bg-gray-600"
        >
          <Icon icon="mdi:refresh" class="h-4 w-4 mr-1" />
          {{ $t('tenant.overview.refresh') }}
        </button>
      </div>
    </div>

    <div v-if="loading" class="text-center py-8">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">{{ $t('tenant.overview.loading') }}</p>
    </div>

    <div v-else-if="members.length === 0" class="text-center py-8">
      <Icon icon="mdi:account-group" class="mx-auto h-12 w-12 text-gray-400" />
      <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">{{ $t('tenant.overview.noMembers') }}</p>
      <button
        @click="handleInvite"
        class="mt-4 inline-flex items-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md text-white bg-primary hover:bg-primary/80 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary"
      >
        <Icon icon="mdi:account-plus" class="h-4 w-4 mr-1" />
        {{ $t('tenant.overview.inviteFirstMember') }}
      </button>
    </div>

    <div v-else>
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
              :placeholder="$t('tenant.overview.searchMembers')"
              class="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md leading-5 bg-white placeholder-gray-500 focus:outline-none focus:placeholder-gray-400 focus:ring-1 focus:ring-primary focus:border-primary dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white"
            />
          </div>
        </div>
        <div class="flex gap-2">
          <select
            v-model="roleFilter"
            class="block px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary focus:border-primary dark:bg-gray-700 dark:border-gray-600 dark:text-white"
          >
            <option value="">{{ $t('tenant.overview.allRoles') }}</option>
            <option value="ADMIN">Admin</option>
            <option value="MANAGER">Manager</option>
            <option value="MEMBER">Member</option>
          </select>
          <select
            v-model="statusFilter"
            class="block px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary focus:border-primary dark:bg-gray-700 dark:border-gray-600 dark:text-white"
          >
            <option value="">{{ $t('tenant.overview.allStatuses') }}</option>
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
            <option value="PENDING">Pending</option>
          </select>
        </div>
      </div>

      <!-- Members Grid -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div
          v-for="member in filteredMembers"
          :key="member.id"
          class="border border-gray-200 dark:border-gray-700 rounded-lg p-4 hover:shadow-md transition-shadow"
        >
          <div class="flex items-start justify-between">
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
                @click="handleEditMember(member)"
                class="p-1 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
                title="Edit Member"
              >
                <Icon icon="mdi:pencil" class="h-4 w-4" />
              </button>
              <button
                @click="handleRemoveMember(member)"
                class="p-1 text-gray-400 hover:text-red-600 dark:hover:text-red-400"
                title="Remove Member"
              >
                <Icon icon="mdi:delete" class="h-4 w-4" />
              </button>
            </div>
          </div>
          
          <div class="mt-3 space-y-2">
            <div class="flex items-center justify-between">
              <span class="text-xs text-gray-500 dark:text-gray-400">{{ $t('tenant.overview.role') }}</span>
              <span :class="getRoleBadgeClass(member.role)">
                {{ getRoleLabel(member.role) }}
              </span>
            </div>
            
            <div class="flex items-center justify-between">
              <span class="text-xs text-gray-500 dark:text-gray-400">{{ $t('tenant.overview.status') }}</span>
              <span :class="getStatusBadgeClass(member.status)">
                {{ getStatusLabel(member.status) }}
              </span>
            </div>
            
            <div v-if="member.joinedAt" class="flex items-center justify-between">
              <span class="text-xs text-gray-500 dark:text-gray-400">{{ $t('tenant.overview.joinedAt') }}</span>
              <span class="text-xs text-gray-900 dark:text-white">{{ formatDate(member.joinedAt) }}</span>
            </div>
            
            <div v-if="member.lastActiveAt" class="flex items-center justify-between">
              <span class="text-xs text-gray-500 dark:text-gray-400">{{ $t('tenant.overview.lastActive') }}</span>
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
            Previous
          </button>
          <button
            @click="currentPage++"
            :disabled="currentPage === totalPages"
            class="px-3 py-1 text-sm border border-gray-300 rounded-md disabled:opacity-50 disabled:cursor-not-allowed dark:border-gray-600 dark:text-white"
          >
            Next
          </button>
        </div>
      </div>
    </div>

    <!-- Invite Member Modal -->
    <InviteMemberModal
      v-if="showInviteModal"
      :visible="showInviteModal"
      :tenant-key="tenantKey || tenantId"
      @close="showInviteModal = false"
      @invited="handleInvited"
    />

    <!-- Edit Member Modal -->
    <EditMemberModal
      v-if="showEditModal"
      :visible="showEditModal"
      :tenant-key="tenantKey || tenantId"
      :member="selectedMember"
      @close="showEditModal = false"
      @updated="handleMemberUpdated"
    />
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { tenantApi } from '@/api/tenantApi'
import InviteMemberModal from './InviteMemberModal.vue'
import EditMemberModal from './EditMemberModal.vue'
import defaultAvatar from '@/assets/img/user.jpg'

export default {
  name: 'TenantMembers',
  components: {
    Icon,
    InviteMemberModal,
    EditMemberModal
  },
  props: {
    tenantKey: {
      type: String,
      required: false
    },
    tenantId: {
      type: String,
      required: false
    }
  },
  emits: ['updated'],
  setup(props, { emit }) {
    const loading = ref(false)
    const members = ref([])
    const searchQuery = ref('')
    const roleFilter = ref('')
    const statusFilter = ref('')
    const currentPage = ref(1)
    const pageSize = ref(12)
    const totalMembers = ref(0)
    const showInviteModal = ref(false)
    const showEditModal = ref(false)
    const selectedMember = ref(null)

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

      if (statusFilter.value) {
        filtered = filtered.filter(member => member.status === statusFilter.value)
      }

      return filtered
    })

    const loadMembers = async () => {
      loading.value = true
      try {
        const response = await tenantApi.getTenantMembers(props.tenantKey || props.tenantId, {
          page: currentPage.value,
          size: pageSize.value,
          search: searchQuery.value,
          role: roleFilter.value,
          status: statusFilter.value
        })
        members.value = response.data.content || response.data
        totalMembers.value = response.data.totalElements || response.data.length
      } catch (error) {
        console.error('Error loading tenant members:', error)
      } finally {
        loading.value = false
      }
    }

    const handleInvite = () => {
      showInviteModal.value = true
    }

    const handleEditMember = (member) => {
      selectedMember.value = member
      showEditModal.value = true
    }

    const handleRemoveMember = async (member) => {
      if (!confirm(`Are you sure you want to remove ${member.name} from the tenant?`)) {
        return
      }

      try {
        await tenantApi.removeTenantMember(props.tenantKey || props.tenantId, member.id)
        await loadMembers()
        emit('updated')
      } catch (error) {
        console.error('Error removing member:', error)
        alert('Failed to remove member. Please try again.')
      }
    }

    const handleRefresh = () => {
      loadMembers()
    }

    const handleInvited = () => {
      showInviteModal.value = false
      loadMembers()
      emit('updated')
    }

    const handleMemberUpdated = () => {
      showEditModal.value = false
      loadMembers()
      emit('updated')
    }

    const handleAvatarError = (event) => {
      event.target.src = defaultAvatar
    }

    const getRoleBadgeClass = (role) => {
      switch (role) {
        case 'ADMIN':
          return 'inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
        case 'MANAGER':
          return 'inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200'
        case 'MEMBER':
          return 'inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
        default:
          return 'inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
      }
    }

    const getRoleLabel = (role) => {
      switch (role) {
        case 'ADMIN': return 'Admin'
        case 'MANAGER': return 'Manager'
        case 'MEMBER': return 'Member'
        default: return role
      }
    }

    const getStatusBadgeClass = (status) => {
      switch (status) {
        case 'ACTIVE':
          return 'inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
        case 'INACTIVE':
          return 'inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
        case 'PENDING':
          return 'inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200'
        default:
          return 'inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
      }
    }

    const getStatusLabel = (status) => {
      switch (status) {
        case 'ACTIVE': return 'Active'
        case 'INACTIVE': return 'Inactive'
        case 'PENDING': return 'Pending'
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
    watch([searchQuery, roleFilter, statusFilter], () => {
      currentPage.value = 1
      loadMembers()
    })

    watch(currentPage, () => {
      loadMembers()
    })

    onMounted(() => {
      loadMembers()
    })

    return {
      loading,
      members,
      searchQuery,
      roleFilter,
      statusFilter,
      currentPage,
      pageSize,
      totalMembers,
      totalPages,
      filteredMembers,
      showInviteModal,
      showEditModal,
      selectedMember,
      defaultAvatar,
      handleInvite,
      handleEditMember,
      handleRemoveMember,
      handleRefresh,
      handleInvited,
      handleMemberUpdated,
      handleAvatarError,
      getRoleBadgeClass,
      getRoleLabel,
      getStatusBadgeClass,
      getStatusLabel,
      formatDate,
      formatLastActive
    }
  }
}
</script>
