<template>
  <div class="pending-tenant-tab">
    <div v-if="loading" class="loading-state">
      <div class="text-center py-12">
        <Icon icon="mdi:loading" class="animate-spin h-8 w-8 text-gray-400 mx-auto mb-4" />
        <p class="text-gray-600 dark:text-gray-400">Loading pending requests...</p>
      </div>
    </div>
    <div v-else-if="pendingRequests.length === 0" class="empty-state">
      <div class="text-center py-12">
        <Icon icon="mdi:clock" class="h-16 w-16 text-gray-400 mx-auto mb-4" />
        <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">
          {{ $t('Pending Requests') }}
        </h3>
        <p class="text-gray-600 dark:text-gray-400">
          No pending requests at the moment.
        </p>
      </div>
    </div>
    <div v-else class="pending-list">
      <div 
        v-for="request in pendingRequests" 
        :key="request.id"
        class="bg-white dark:bg-gray-800 rounded-lg shadow-md border border-gray-200 dark:border-gray-700 p-6 mb-4"
      >
        <div class="flex justify-between items-center">
          <div class="flex items-center space-x-4">
            <div class="tenant-avatar-pending">
              <img v-if="request.logoUrl" :src="secureImageUrl(request.logoUrl)" :alt="request.name" />
              <div v-else class="avatar-fallback">
                {{ request.name?.charAt(0)?.toUpperCase() || '?' }}
              </div>
            </div>
            <div>
              <h4 class="font-medium text-gray-900 dark:text-white">{{ request.name }}</h4>
              <p class="text-sm text-gray-600 dark:text-gray-400">Requested: {{ formatDateArray(request.requestedAt) }}</p>
            </div>
          </div>
          <div class="flex gap-2">
            <button
              @click="cancelRequest(request.id)"
              :disabled="cancelling === request.id"
              class="px-3 py-1 bg-red-600 text-white text-sm rounded hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <Icon v-if="cancelling === request.id" icon="mdi:loading" class="animate-spin h-4 w-4 mr-1" />
              {{ $t('Cancel Request') }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import { ref, computed, onMounted, getCurrentInstance } from 'vue'
import { Icon } from '@iconify/vue'
import { useI18n } from 'vue-i18n'
import { useGatewayPendingTenantStore } from '@/stores/tenant/gateway/pendingTenantStore'
import { formatDateTime } from '@/utils/dateUtils'
import { secureImageUrl } from '@/utils/imageUtils'
import { MembershipStatus } from '@/types/tenant'
export default {
  name: 'PendingTenantTab',
  components: {
    Icon
  },
  setup() {
    const { t } = useI18n()
    const pendingStore = useGatewayPendingTenantStore()
    
    const pendingRequests = computed(() => pendingStore.pendingRequests)
    const loading = computed(() => pendingStore.loading)
    const cancelling = ref(null)
    
    const cancelRequest = async (requestId) => {
      try {
        cancelling.value = requestId
        await pendingStore.rejectRequest(requestId)
        // Show success message
        const instance = getCurrentInstance()
        const toast = instance?.appContext.config.globalProperties.$toast
        toast?.success('Join request cancelled successfully')
      } catch (error) {
        console.error('Failed to cancel request:', error)
        // Show error message
        const instance = getCurrentInstance()
        const toast = instance?.appContext.config.globalProperties.$toast
        toast?.error('Failed to cancel request')
      } finally {
        cancelling.value = null
      }
    }
    
    const formatDateArray = (dateArray) => {
      if (!dateArray || !Array.isArray(dateArray) || dateArray.length < 7) {
        return 'N/A'
      }
      // Backend returns: [year, month, day, hour, minute, second, nanos]
      const [year, month, day, hour, minute, second] = dateArray
      const date = new Date(year, month - 1, day, hour, minute, second)
      return date.toLocaleString()
    }
    
    onMounted(() => {
      pendingStore.fetchPendingRequests()
    })
    
    return {
      t,
      pendingRequests,
      loading,
      cancelling,
      cancelRequest,
      formatDateTime,
      formatDateArray,
      secureImageUrl
    }
  }
}
</script>
<style scoped>
.pending-tenant-tab {
  width: 100%;
}
.loading-state {
  text-align: center;
  padding: 40px 0;
}
.empty-state {
  text-align: center;
  padding: 40px 0;
}
.pending-list {
  margin-top: 20px;
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
<style scoped>
.tenant-avatar-pending {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.tenant-avatar-pending img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-fallback {
  color: white;
  font-weight: 600;
  font-size: 18px;
}
</style>
