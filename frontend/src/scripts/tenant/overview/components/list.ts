import { defineComponent, ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'

import { useTenantAdminContextStore } from '@/stores/tenant/admin/tenantContextStore'

import TenantAvatar from '@/views/tenant/overview/components/TenantAvatar.vue'
import TenantBasicForm from '@/views/tenant/overview/components/TenantBasicForm.vue'
import TenantProfileForm from '@/views/tenant/overview/components/TenantProfileForm.vue'
import TenantAddressForm from '@/views/tenant/overview/components/TenantAddressForm.vue'

export default defineComponent({
  name: 'TenantAdminOverview',
  components: {
    TenantAvatar,
    TenantBasicForm,
    TenantProfileForm,
    TenantAddressForm
  },
  props: ['viewSettings'],
  emits: ['onChangeView'],
  setup() {
    const { t } = useI18n()

    const contextStore = useTenantAdminContextStore()
    const activeTab = ref<'basic' | 'profile' | 'address'>('basic')

    onMounted(() => {
      refreshDataFn();
    })

    const refreshDataFn =()=> {
      contextStore.loadTenant();
    }

    return {
      t,
      activeTab,
      tenant: computed(() => contextStore.tenant),
      loading: computed(() => contextStore.loading),
      onTabChange: (tab: string) => (activeTab.value = tab as any),
      refreshDataFn
    }
  }
})