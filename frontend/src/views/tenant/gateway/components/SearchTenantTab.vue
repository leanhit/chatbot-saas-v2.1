<template>
  <div class="search-tenant-tab">
    <!-- Search box -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow-md border border-gray-200 dark:border-gray-700 p-6 mb-6">
      <div class="flex items-center space-x-2">
        <div class="relative flex-1">
          <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <Icon icon="mdi:search" class="h-5 w-5 text-gray-400" />
          </div>
          <input
            v-model="keyword"
            type="text"
            placeholder="Search Tenant"
            class="block w-full pl-10 pr-10 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-500 dark:placeholder-gray-400 focus:ring-blue-500 focus:border-blue-500"
            @keyup.enter="handleManualSearch"
          />
          <div v-if="keyword" class="absolute inset-y-0 right-0 pr-3 flex items-center">
            <button
              @click="keyword = ''"
              class="text-gray-400 hover:text-gray-500"
            >
              <Icon icon="mdi:close" class="h-4 w-4" />
            </button>
          </div>
        </div>
        <button
          @click="handleManualSearch"
          :disabled="loading"
          class="inline-flex items-center px-5 py-2.5 text-sm font-medium text-white bg-blue-700 rounded-lg hover:bg-blue-800 focus:ring-4 focus:outline-none focus:ring-blue-300 dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <Icon v-if="loading" icon="mdi:loading" class="animate-spin h-4 w-4 mr-2" />
          <span v-else>Search</span>
        </button>
      </div>
    </div>
    <!-- Loading -->
    <div v-if="loading" class="loading-state">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div v-for="i in 3" :key="i" class="bg-white dark:bg-gray-800 rounded-lg shadow-md border border-gray-200 dark:border-gray-700 p-6">
          <div class="animate-pulse">
            <div class="flex items-center space-x-4 mb-4">
              <div class="w-12 h-12 bg-gray-300 dark:bg-gray-600 rounded-full"></div>
              <div class="flex-1 space-y-2">
                <div class="h-4 bg-gray-300 dark:bg-gray-600 rounded w-3/4"></div>
                <div class="h-3 bg-gray-300 dark:bg-gray-600 rounded w-1/2"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!-- Results -->
    <template v-else>
      <div v-if="results.length === 0" class="empty-state">
        <div class="text-center py-12">
          <Icon icon="mdi:search" class="h-16 w-16 text-gray-400 mx-auto mb-4" />
          <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">
            {{ keyword ? 'No results found' : 'Search for workspaces' }}
          </h3>
          <p class="text-gray-600 dark:text-gray-400">
            {{ keyword ? 'Try different keywords' : 'Enter at least 2 characters to search' }}
          </p>
        </div>
      </div>
      <div v-else class="search-results-grid">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div
            v-for="tenant in results"
            :key="tenant.id"
            class="search-item-card"
          >
            <div class="search-item-content">
              <!-- LEFT -->
              <div class="info-side">
                <div class="tenant-avatar-search">
                  <img v-if="tenant.logoUrl" :src="tenant.logoUrl" :alt="tenant.name" />
                  <div v-else class="avatar-fallback">
                    {{ tenant.name?.charAt(0).toUpperCase() }}
                  </div>
                </div>
                <div class="text-info">
                  <div class="tenant-name">
                    {{ tenant.name }}
                  </div>
                  <div class="tenant-meta">
                    <span>ID: {{ tenant.id }}</span><br />
                    <span v-if="tenant.province">
                      Province: {{ tenant.province }}
                    </span>
                  </div>
                  <div v-if="tenant.contactEmail" class="tenant-email">
                    Contact Email: {{ maskEmail(tenant.contactEmail) }}
                  </div>
                </div>
              </div>
              <!-- RIGHT -->
              <div class="action-side">
                <button
                  v-if="tenant.membershipStatus === 'NONE'"
                  @click="onJoinClick(tenant.id)"
                  class="join-button"
                >
                  Join
                </button>
                <span
                  v-else-if="tenant.membershipStatus === 'PENDING'"
                  class="pending-tag"
                >
                  Pending
                </span>
                <span
                  v-else-if="tenant.membershipStatus === 'APPROVED'"
                  class="member-tag"
                >
                  Member
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
<script>
import { ref, watch, onUnmounted, computed } from 'vue'
import { Icon } from '@iconify/vue'
import { useI18n } from 'vue-i18n'
import { useGatewaySearchTenantStore } from '@/stores/tenant/gateway/searchTenantStore'
import { secureImageUrl } from '@/utils/imageUtils'
export default {
  name: 'SearchTenantTab',
  components: {
    Icon
  },
  setup() {
    const { t } = useI18n()
    const searchStore = useGatewaySearchTenantStore()
    const keyword = ref('')
    let debounceTimer = null
    const results = computed(() => searchStore.searchResults || [])
    const loading = computed(() => searchStore.loading)
    const executeSearch = async () => {
      await searchStore.searchTenants(keyword.value.trim())
    }
    const handleManualSearch = async () => {
      if (!keyword.value.trim()) return
      await executeSearch()
    }
    const onJoinClick = async (tenantId) => {
      try {
        // TODO: Implement join tenant functionality
        // await searchStore.requestJoinTenant(String(tenantId))
        // Update UI immediately
        // const tenant = searchStore.searchResults.find(t => t.id === tenantId)
        // if (tenant) {
        //   tenant.membershipStatus = 'PENDING'
        // }
      } catch (error) {
      }
    }
    const maskEmail = (email) => {
      if (!email) return ''
      const [name, domain] = email.split('@')
      if (!domain) return email
      return `${name.slice(0, 2)}***@${domain}`
    }
    watch(keyword, (newVal) => {
      if (debounceTimer) clearTimeout(debounceTimer)
      if (!newVal.trim()) {
        searchStore.clearResults()
        return
      }
      if (newVal.trim().length >= 2) {
        debounceTimer = setTimeout(() => {
          executeSearch()
        }, 500)
      }
    })
    onUnmounted(() => {
      if (debounceTimer) clearTimeout(debounceTimer)
      searchStore.clearResults()
    })
    return {
      keyword,
      loading,
      results,
      handleManualSearch,
      onJoinClick,
      maskEmail,
      secureImageUrl
    }
  }
}
</script>
<style scoped>
.search-tenant-tab {
  width: 100%;
}
.search-box {
  margin-bottom: 30px;
  display: flex;
  justify-content: center;
}
.search-input-container {
  display: flex;
  align-items: center;
  max-width: 600px;
  width: 100%;
  border: 1px solid #d1d5db;
  dark:border-gray-600;
  border-radius: 8px;
  background: white;
  dark:bg-gray-800;
  overflow: hidden;
}
.search-icon {
  color: #6b7280;
  dark:color-gray-400;
  margin: 0 12px;
  flex-shrink: 0;
}
.search-input {
  flex: 1;
  padding: 12px 0;
  border: none;
  outline: none;
  background: transparent;
  color: #1f2937;
  dark:color-white;
  font-size: 14px;
}
.search-input::placeholder {
  color: #9ca3af;
  dark:color-gray-500;
}
.clear-button {
  margin-right: 8px;
  padding: 4px;
  border: none;
  background: transparent;
  color: #6b7280;
  dark:color-gray-400;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.2s;
}
.clear-button:hover {
  background: #f3f4f6;
  dark:bg-gray-700;
}
.search-button {
  margin: 4px;
  padding: 8px 16px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
  display: flex;
  align-items: center;
  gap: 4px;
}
.search-button:hover:not(:disabled) {
  background: #2563eb;
}
.search-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.loading-state {
  padding: 20px 0;
}
.skeleton-card {
  background: white;
  dark:bg-gray-800;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  dark:border-gray-700;
  padding: 20px;
  margin-bottom: 16px;
}
.empty-state {
  text-align: center;
  padding: 40px 0;
}
.search-results-grid {
  margin-top: 20px;
}
.search-item-card {
  background: white;
  dark:bg-gray-800;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  dark:border-gray-700;
  padding: 20px;
  margin-bottom: 16px;
  transition: all 0.3s ease;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
}
.search-item-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px 0 rgba(0, 0, 0, 0.1);
}
.search-item-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.info-side {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}
.tenant-avatar-search {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}
.tenant-avatar-search img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.avatar-fallback {
  color: white;
  font-weight: 600;
  font-size: 16px;
}
.text-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
  min-width: 0;
}
.tenant-name {
  font-weight: 600;
  color: #2c3e50;
  dark:color-white;
  font-size: 16px;
}
.tenant-meta {
  font-size: 12px;
  color: #909399;
  dark:color-gray-400;
}
.tenant-email {
  font-size: 12px;
  color: #606266;
  dark:color-gray-500;
}
.action-side {
  flex-shrink: 0;
  margin-left: 12px;
}
.join-button {
  padding: 6px 12px;
  background: transparent;
  color: #3b82f6;
  border: 1px solid #3b82f6;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}
.join-button:hover {
  background: #3b82f6;
  color: white;
}
.pending-tag {
  padding: 6px 12px;
  background: #fef3c7;
  color: #d97706;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}
.member-tag {
  padding: 6px 12px;
  background: #dcfce7;
  color: #16a34a;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
.animate-spin {
  animation: spin 1s linear infinite;
}
</style>
