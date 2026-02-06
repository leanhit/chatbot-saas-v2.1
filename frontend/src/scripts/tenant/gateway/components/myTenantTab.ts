import { defineComponent, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

import { useGatewayTenantStore } from '@/stores/tenant/gateway/myTenantStore'
import type { TenantDetailResponse } from '@/types/tenant'
import { formatDateTime } from '@/utils/search'

export default defineComponent({
  name: 'MyTenantTab',
  setup() {
    const { t } = useI18n()
    const router = useRouter()
    const tenantStore = useGatewayTenantStore()

    const tenantList = computed(() => tenantStore.userTenants)
    const loading = computed(() => tenantStore.loadingTenants)

    const enterWorkspace = async (tenant: TenantDetailResponse) => {
      if (tenant.status !== 'ACTIVE') return

      await tenantStore.switchTenant(String(tenant.id))
      router.push('/home') // chỉnh route theo app của bạn
    }

    // (optional) nếu backend hỗ trợ
    const suspendTenant = async (id: number) => {
      console.warn('Suspend tenant not implemented', id)
      // await tenantApi.suspendTenant(id)
      // await tenantStore.fetchUserTenants()
    }

    const activateTenant = async (id: number) => {
      console.warn('Activate tenant not implemented', id)
      // await tenantApi.activateTenant(id)
      // await tenantStore.fetchUserTenants()
    }

    return {
      t,
      loading,
      tenantList,
      enterWorkspace,
      suspendTenant,
      activateTenant,
      formatDateTime
    }
  }
})