import { defineComponent, ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'

import { useGatewayTenantStore } from '@/stores/tenant/gateway/myTenantStore'

import MyTenantTab from       '@/views/tenant/gateway/components/MyTenantTab.vue'
import CreateTenantModal from '@/views/tenant/gateway/components/CreateTenantModal.vue'
import PendingTenantTab from  '@/views/tenant/gateway/components/PendingTenantTab.vue'
import SearchTenantTab from   '@/views/tenant/gateway/components/SearchTenantTab.vue'
import UserInvitationsTab from '@/views/tenant/member/components/UserInvitationsTab.vue'

export default defineComponent({
  name: 'TenantGateway',
  components: {
    MyTenantTab,
    CreateTenantModal,
    PendingTenantTab,
    SearchTenantTab,
    UserInvitationsTab
  },
  setup() {
    const { t } = useI18n()
    const tenantStore = useGatewayTenantStore()

    const activeTab = ref<'my' | 'search' | 'pending'>('my')
    const showCreateModal = ref(false)

    onMounted(() => {
      tenantStore.fetchUserTenants()
    })

    const onTabChange = (tab: string) => {
      activeTab.value = tab as any
    }

    const onTenantCreated = async () => {
      showCreateModal.value = false
      await tenantStore.fetchUserTenants()
    }

    return {
      t,
      activeTab,
      showCreateModal,
      onTabChange,
      onTenantCreated
    }
  }
})
