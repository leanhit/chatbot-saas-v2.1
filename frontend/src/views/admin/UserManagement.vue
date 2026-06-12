<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <div class="flex items-center">
        <Icon icon="mdi:account-group" class="text-2xl text-blue-600 dark:text-blue-400 mr-3" />
        <h1 class="text-2xl font-bold text-gray-800 dark:text-white">
          {{ $t('sidebar.userManagement') }}
        </h1>
      </div>
      <div class="flex gap-2">
        <button
          @click="refreshData"
          :disabled="loading"
          class="bg-white dark:bg-gray-800 hover:border-gray-200 dark:hover:bg-gray-700 dark:text-white dark:border-gray-700 border rounded py-2 px-4 flex items-center gap-2"
        >
          <Icon v-if="loading" icon="mdi:loading" class="animate-spin" />
          <Icon v-else icon="mdi:refresh" />
          {{ $t('common.refresh') }}
        </button>
      </div>
    </div>

    <!-- Alert Messages -->
    <div v-if="message" class="mb-4 p-4 rounded-lg" :class="getMessageClass()">
      {{ message }}
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="text-center py-8">
      <Icon icon="eos-icons:loading" class="text-4xl text-blue-600 dark:text-blue-400 animate-spin mb-4" />
      <p class="text-gray-600 dark:text-gray-400">{{ $t('admin.user.loadingUsers') }}</p>
    </div>

    <!-- Users Table -->
    <div v-else class="bg-white dark:bg-gray-900 rounded-lg shadow overflow-hidden">
      <div class="p-4 border-b dark:border-gray-700">
        <div class="flex justify-end items-center gap-4">
          <select
            v-model="roleFilter"
            @change="applyFilters"
            class="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800 dark:text-white w-48"
          >
            <option value="">{{ $t('admin.user.allRoles') }}</option>
            <option value="USER">USER</option>
            <option value="ADMIN">ADMIN</option>
          </select>
        </div>
      </div>

      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
          <thead class="bg-gray-50 dark:bg-gray-800">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                ID
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                {{ $t('admin.user.email') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                {{ $t('admin.user.systemRole') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                {{ $t('admin.user.status') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                {{ $t('admin.user.createdAt') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                {{ $t('admin.user.actions') }}
              </th>
            </tr>
          </thead>
          <tbody class="bg-white dark:bg-gray-900 divide-y divide-gray-200 dark:divide-gray-700">
            <tr v-for="user in users" :key="user.id" class="hover:bg-gray-50 dark:hover:bg-gray-800">
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                {{ user.id }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                {{ user.email }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span
                  class="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full"
                  :class="getRoleClass(user.systemRole)"
                >
                  {{ user.systemRole }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span
                  class="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full"
                  :class="user.isActive ? 'bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200' : 'bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-200'"
                >
                  {{ user.isActive ? $t('admin.user.active') : $t('admin.user.inactive') }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                {{ formatDate(user.createdAt) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                <button
                  @click="openRoleModal(user)"
                  class="text-blue-600 dark:text-blue-400 hover:text-blue-900 dark:hover:text-blue-300 mr-3"
                  :title="$t('admin.user.changeRole')"
                >
                  <Icon icon="mdi:shield-account" class="w-4 h-4" />
                </button>
                <button
                  @click="toggleUserStatus(user)"
                  :class="user.isActive ? 'text-red-600 dark:text-red-400 hover:text-red-900 dark:hover:text-red-300' : 'text-green-600 dark:text-green-400 hover:text-green-900 dark:hover:text-green-300'"
                  :title="user.isActive ? $t('admin.user.deactivate') : $t('admin.user.activate')"
                >
                  <Icon :icon="user.isActive ? 'mdi:account-off' : 'mdi:account-check'" class="w-4 h-4" />
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination Controls -->
      <div v-if="totalPages > 1" class="px-6 py-4 border-t dark:border-gray-700 flex items-center justify-between bg-gray-50 dark:bg-gray-800">
        <div class="text-sm text-gray-500 dark:text-gray-400">
          Hiển thị {{ users.length }} / {{ totalElements }} người dùng
        </div>
        <div class="flex gap-2">
          <button 
            @click="prevPage" 
            :disabled="currentPage === 0"
            class="px-3 py-1 border border-gray-300 rounded hover:bg-gray-100 disabled:opacity-50 dark:border-gray-600 dark:hover:bg-gray-700 dark:text-white"
          >
            Trang trước
          </button>
          <span class="px-3 py-1 dark:text-white">Trang {{ currentPage + 1 }} / {{ totalPages }}</span>
          <button 
            @click="nextPage" 
            :disabled="currentPage >= totalPages - 1"
            class="px-3 py-1 border border-gray-300 rounded hover:bg-gray-100 disabled:opacity-50 dark:border-gray-600 dark:hover:bg-gray-700 dark:text-white"
          >
            Trang sau
          </button>
        </div>
      </div>

      <!-- No Results -->
      <div v-if="users.length === 0" class="text-center py-8">
        <Icon icon="mdi:account-search" class="text-4xl text-gray-300 mb-2" />
        <p class="text-gray-500 dark:text-gray-400">{{ $t('admin.user.noUsersFound') }}</p>
      </div>
    </div>

    <!-- Change Role Modal -->
    <div v-if="showRoleModal" class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
      <div class="relative top-20 mx-auto p-5 border w-11/12 md:w-1/2 lg:w-1/3 shadow-lg rounded-md bg-white dark:bg-gray-900">
        <div class="mt-3">
          <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-4">
            {{ $t('admin.user.changeRole') }}
          </h3>
          
          <div class="mb-4">
            <p class="text-sm text-gray-600 dark:text-gray-400 mb-2">
              {{ $t('admin.user.user') }}: <span class="font-semibold">{{ selectedUser?.email }}</span>
            </p>
            <p class="text-sm text-gray-600 dark:text-gray-400 mb-4">
              {{ $t('admin.user.currentRole') }}: <span class="font-semibold">{{ selectedUser?.systemRole }}</span>
            </p>
          </div>

          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              {{ $t('admin.user.newRole') }}
            </label>
            <select
              v-model="newRole"
              class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800 dark:text-white"
            >
              <option value="USER">USER</option>
              <option value="ADMIN">ADMIN</option>
            </select>
          </div>
          
          <div class="flex justify-end space-x-3 pt-4">
            <button
              type="button"
              @click="closeRoleModal"
              class="px-4 py-2 bg-gray-300 dark:bg-gray-600 text-gray-700 dark:text-gray-300 rounded hover:bg-gray-400 dark:hover:bg-gray-500"
            >
              {{ $t('common.cancel') }}
            </button>
            <button
              type="button"
              @click="changeUserRole"
              :disabled="changingRole"
              class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
            >
              <span v-if="changingRole" class="flex items-center">
                <Icon icon="eos-icons:loading" class="animate-spin mr-2" />
                {{ $t('admin.user.changing') }}
              </span>
              <span v-else>
                {{ $t('admin.user.change') }}
              </span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Icon } from '@iconify/vue'
import { useSearchStore } from '@/stores/searchStore'
import { onMounted, onUnmounted, ref, computed, watch } from 'vue'
import axios from '@/plugins/axios'

export default {
  name: 'UserManagement',
  components: {
    Icon
  },
  setup() {
    const searchStore = useSearchStore()
    const users = ref([])
    const loading = ref(false)
    const message = ref('')
    const messageType = ref('success')
    const showRoleModal = ref(false)
    const selectedUser = ref(null)
    const newRole = ref('USER')
    const changingRole = ref(false)
    const localSearchQuery = ref('')
    const roleFilter = ref('')
    
    // Pagination state
    const currentPage = ref(0)
    const totalPages = ref(0)
    const totalElements = ref(0)
    const pageSize = ref(20)
    let searchTimeout = null

    // Set search context when component mounts
    onMounted(() => {
      searchStore.setSearchContext('users')
      loadUsers()
      
      // Watch for changes in the global search store
      watch(() => searchStore.searchQuery, (newQuery) => {
        localSearchQuery.value = newQuery
      })
    })

    // Watch filters to trigger debounced search
    watch([localSearchQuery, roleFilter], () => {
      currentPage.value = 0 // Reset to first page on search
      if (searchTimeout) clearTimeout(searchTimeout)
      searchTimeout = setTimeout(() => {
        loadUsers()
      }, 500)
    })

    // Clear search context when component unmounts
    onUnmounted(() => {
      searchStore.resetSearch()
      if (searchTimeout) clearTimeout(searchTimeout)
    })

    const loadUsers = async () => {
      loading.value = true
      try {
        const response = await axios.get('/users', {
          params: {
            search: localSearchQuery.value || null,
            role: roleFilter.value || null,
            page: currentPage.value,
            size: pageSize.value
          }
        })
        const pageData = response.data.data || response.data
        users.value = pageData.content || []
        totalPages.value = pageData.totalPages || 0
        totalElements.value = pageData.totalElements || 0
      } catch (error) {
        console.error('Error loading users:', error)
        setMessage('Error loading users: ' + (error.message || 'Unknown error'), 'error')
      } finally {
        loading.value = false
      }
    }

    const refreshData = () => {
      loadUsers()
    }

    const applyFilters = () => {
      // Filters are watched automatically
    }

    const nextPage = () => {
      if (currentPage.value < totalPages.value - 1) {
        currentPage.value++
        loadUsers()
      }
    }

    const prevPage = () => {
      if (currentPage.value > 0) {
        currentPage.value--
        loadUsers()
      }
    }

    const openRoleModal = (user) => {
      selectedUser.value = user
      newRole.value = user.systemRole
      showRoleModal.value = true
    }

    const closeRoleModal = () => {
      showRoleModal.value = false
      selectedUser.value = null
      newRole.value = 'USER'
    }

    const changeUserRole = async () => {
      if (!selectedUser.value) return

      changingRole.value = true
      try {
        const response = await axios.post('/auth/change-role', {
          userId: selectedUser.value.id,
          newRole: newRole.value
        })

        // Update local user data
        const userIndex = users.value.findIndex(u => u.id === selectedUser.value.id)
        if (userIndex !== -1) {
          users.value[userIndex].systemRole = newRole.value
        }

        setMessage('Role changed successfully', 'success')
        closeRoleModal()
      } catch (error) {
        console.error('Error changing role:', error)
        setMessage('Error changing role: ' + (error.response?.data?.message || error.message || 'Unknown error'), 'error')
      } finally {
        changingRole.value = false
      }
    }

    const toggleUserStatus = async (user) => {
      try {
        const newStatus = !user.isActive
        const response = await axios.put(`/users/${user.id}/status`, {
          isActive: newStatus
        })

        // Update local user data
        const userIndex = users.value.findIndex(u => u.id === user.id)
        if (userIndex !== -1) {
          users.value[userIndex].isActive = newStatus
        }

        setMessage(`User ${newStatus ? 'activated' : 'deactivated'} successfully`, 'success')
      } catch (error) {
        console.error('Error toggling user status:', error)
        setMessage('Error toggling user status: ' + (error.response?.data?.message || error.message || 'Unknown error'), 'error')
      }
    }

    const getRoleClass = (role) => {
      const classes = {
        'USER': 'bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-200',
        'ADMIN': 'bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-200',
        'SYSTEM_ADMIN': 'bg-purple-100 dark:bg-purple-900 text-purple-800 dark:text-purple-200'
      }
      return classes[role] || 'bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-200'
    }

    const formatDate = (dateString) => {
      if (!dateString) return '-'
      const date = new Date(dateString)
      return date.toLocaleDateString('vi-VN')
    }

    const setMessage = (msg, type = 'success') => {
      message.value = msg
      messageType.value = type
      
      setTimeout(() => {
        if (message.value === msg) {
          message.value = ''
        }
      }, 5000)
    }

    const getMessageClass = () => {
      const baseClasses = 'p-4 rounded-lg mb-4'
      const typeClasses = {
        success: 'bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200',
        error: 'bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-200',
        warning: 'bg-yellow-100 dark:bg-yellow-900 text-yellow-800 dark:text-yellow-200',
        info: 'bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-200'
      }
      return `${baseClasses} ${typeClasses[messageType.value] || typeClasses.info}`
    }

    return {
      users,
      loading,
      message,
      messageType,
      showRoleModal,
      selectedUser,
      newRole,
      changingRole,
      localSearchQuery,
      roleFilter,
      currentPage,
      totalPages,
      totalElements,
      refreshData,
      applyFilters,
      nextPage,
      prevPage,
      openRoleModal,
      closeRoleModal,
      changeUserRole,
      toggleUserStatus,
      getRoleClass,
      formatDate,
      setMessage,
      getMessageClass
    }
  }
}
</script>
