import { defineComponent, computed, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'

import { useGatewayPendingStore } from '@/stores/tenant/gateway/pendingTenantStore'
import type { TenantPendingResponse } from '@/types/tenant'

export default defineComponent({
  name: 'PendingTenantTab',
  setup() {
    const { t } = useI18n()
    const pendingStore = useGatewayPendingStore()

    const tenants = computed<TenantPendingResponse[]>(
      () => pendingStore.pendingTenants
    )
    const loading = computed(() => pendingStore.loading)

    onMounted(() => {
      pendingStore.fetchMyPendingTenants()
    })

    onUnmounted(() => {
      pendingStore.clearPending()
    })

    const formatDate = (value: string) => {
      if (!value) return ''
      try {
        return new Date(value).toLocaleString('vi-VN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        })
      } catch {
        return value
      }
    }

    const getVisibilityType = (visibility: string) => {
      return visibility === 'PUBLIC' ? 'success' : 'info'
    }

    return {
      t,
      tenants,
      loading,
      formatDate,
      getVisibilityType
    }
  }
})