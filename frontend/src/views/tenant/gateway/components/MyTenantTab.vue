<template>
  <div class="my-tenant-tab">
    <div v-if="loading" class="loading-state">
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
        <div v-for="i in 3" :key="i" class="skeleton-card">
          <div class="animate-pulse">
            <div class="flex items-center space-x-4 mb-4">
              <div class="w-12 h-12 bg-gray-300 dark:bg-gray-600 rounded-full"></div>
              <div class="flex-1 space-y-2">
                <template>
                  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 py-12 px-4 sm:px-6 lg:px-8">
                    <div class="max-w-7xl mx-auto sm:px-6 lg:px-8">
                      <div class="bg-white dark:bg-gray-800 overflow-hidden shadow-xl rounded-lg">
                        <div class="px-4 py-5 sm:p-6 lg:p-8">
                          <div class="flex justify-between items-center mb-6">
                            <h3 class="text-lg font-medium text-gray-900 dark:text-white">{{ $t('tenant.member.myTenants') }}</h3>
                            <router-link to="/tenant/gateway" class="text-blue-600 hover:text-blue-500">
                              {{ $t('tenantGateway.title') }}
                            </router-link>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </template>
              </div>
            </div>
            <div class="space-y-2">
              <div class="h-3 bg-gray-300 dark:bg-gray-600 rounded"></div>
              <div class="h-3 bg-gray-300 dark:bg-gray-600 rounded w-5/6"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <template v-else>
      <div v-if="tenantList.length === 0" class="empty-state">
        <div class="text-center py-12">
          <Icon icon="mdi:office-building" class="h-16 w-16 text-gray-400 mx-auto mb-4" />
          <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">
            {{ $t('Empty') }}
          </h3>
          <p class="text-gray-600 dark:text-gray-400">
            You don't have any workspaces yet.
          </p>
        </div>
      </div>
      <div v-else class="tenant-grid">
        <div
          v-for="tenant in tenantList"
          :key="tenant.id"
          class="bg-white dark:bg-gray-800 rounded-lg shadow-md border border-gray-200 dark:border-gray-700 hover:shadow-lg transition-shadow duration-200 p-6"
        >
          <div class="card-header">
            <div class="tenant-avatar" :class="{ 'is-inactive': tenant.status !== 'ACTIVE' }">
              <img v-if="tenant.profile?.logoUrl" :src="tenant.profile.logoUrl" :alt="tenant.name" />
              <div v-else class="avatar-fallback">
                {{ tenant.name.charAt(0).toUpperCase() }}
              </div>
            </div>
            <div class="header-main">
              <h3 class="tenant-name" :title="tenant.name">
                {{ tenant.name }}
              </h3>
              <span
                :class="[
                  'text-xs py-1 px-4 rounded-md',
                  tenant.status === 'ACTIVE' 
                    ? 'bg-green-600 text-white' 
                    : 'bg-red-600 text-white'
                ]"
              >
                {{ tenant.status === 'ACTIVE' ? $t('Active') : $t('Inactive') }}
              </span>
            </div>
          </div>
          <div class="card-content">
            <div class="info-item">
              <span class="label">Name:</span>
              <span class="value">{{ tenant.name }}</span>
            </div>
            <div class="info-item">
              <span class="label">Created:</span>
              <span class="value">{{ formatDateTime(tenant.createdAt) }}</span>
            </div>
            <div v-if="tenant.profile?.contactEmail" class="info-item">
              <span class="label">Email:</span>
              <span class="value text-truncate">
                {{ tenant.profile.contactEmail }}
              </span>
            </div>
            <div class="info-item">
              <span class="label">{{ $t('Expire') }}:</span>
              <span class="value">
                {{ formatDateTime(tenant.expiresAt) }}
              </span>
            </div>
          </div>
          <div class="card-footer">
            <div class="action-buttons">
              <button
                v-if="tenant.status === 'ACTIVE'"
                @click="suspendTenant(tenant.id)"
                class="text-red-600 dark:text-red-400 hover:text-red-700 dark:hover:text-red-300 text-sm font-medium"
              >
                {{ $t('Suspend') }}
              </button>
              <button
                v-else
                @click="activateTenant(tenant.id)"
                class="text-green-600 dark:text-green-400 hover:text-green-700 dark:hover:text-green-300 text-sm font-medium"
              >
                {{ $t('Activate') }}
              </button>
              <button
                @click="enterWorkspace(tenant)"
                :disabled="tenant.status !== 'ACTIVE'"
                class="inline-flex items-center px-3 py-1.5 bg-primary text-white text-sm font-medium rounded-md hover:bg-primary/80 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {{ $t('Enter') }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
<script>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import { useI18n } from 'vue-i18n'
import { useGatewayTenantStore } from '@/stores/tenant/gateway/myTenantStore'
import { secureImageUrl } from '@/utils/imageUtils'
export default {
  name: 'MyTenantTab',
  components: {
    Icon
  },
  emits: ['tenant-entered'],
  setup(props, { emit }) {
    const { t } = useI18n()
    const router = useRouter()
    const tenantStore = useGatewayTenantStore()
    const tenantList = computed(() => tenantStore.userTenants)
    const loading = computed(() => tenantStore.loadingTenants)
    const enterWorkspace = async (tenant) => {
      if (tenant.status !== 'ACTIVE') return
      try {
        await tenantStore.switchTenant(tenant.tenantKey) // ✅ Use tenantKey from new backend
        router.push('/dashboard')
        emit('tenant-entered')
      } catch (error) {
        // Optionally show error message to user
        // ElMessage.error('Failed to enter workspace. Please try again.')
      }
    }
    const suspendTenant = async (id) => {
      await tenantStore.suspendTenant(id)
      await tenantStore.fetchUserTenants()
    }
    const activateTenant = async (id) => {
      await tenantStore.activateTenant(id)
      await tenantStore.fetchUserTenants()
    }
    const formatDateTime = (dateString) => {
      if (!dateString) return 'N/A'
      return new Date(dateString).toLocaleDateString()
    }
    return {
      tenantList,
      loading,
      enterWorkspace,
      suspendTenant,
      activateTenant,
      formatDateTime,
      secureImageUrl
    }
  }
}
</script>
<style scoped>
.my-tenant-tab {
  width: 100%;
}
.tenant-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-top: 10px;
}
.tenant-card {
  background: white;
  dark:bg-gray-800;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  dark:border-gray-700;
  padding: 20px;
  transition: all 0.3s ease;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
}
.tenant-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.05);
}
.card-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}
.tenant-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}
.tenant-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.avatar-fallback {
  width: 100%;
  height: 100%;
  background: #3b82f6;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 18px;
}
.tenant-avatar.is-inactive {
  filter: grayscale(1);
  opacity: 0.6;
}
.header-main {
  flex: 1;
  overflow: hidden;
}
.tenant-name {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  dark:color-white;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.status-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}
.status-active {
  background: #dcfce7;
  color: #16a34a;
}
.status-inactive {
  background: #f3f4f6;
  color: #6b7280;
}
.card-content {
  background-color: #f9fafb;
  dark:bg-gray-700;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 20px;
}
.info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  font-size: 13px;
}
.info-item:last-child {
  margin-bottom: 0;
}
.label {
  color: #6b7280;
  dark:color-gray-400;
}
.value {
  color: #374151;
  dark:color-gray-300;
  font-weight: 500;
}
.text-truncate {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.card-footer {
  border-top: 1px solid #f3f4f6;
  dark:border-gray-700;
  padding-top: 15px;
}
.action-buttons {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.skeleton-card {
  background: white;
  dark:bg-gray-800;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  dark:border-gray-700;
  padding: 20px;
}
.empty-state {
  text-align: center;
  padding: 40px 0;
}
</style>
