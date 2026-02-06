import { defineComponent, ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'

import { ACTIVE_TENANT_ID } from '@/utils/constant'
import { useTenantAdminContextStore } from '@/stores/tenant/admin/tenantContextStore'
import { useTenantAdminMembersStore } from '@/stores/tenant/admin/tenantMembersStore'

import ActiveMemberTab from '@/views/tenant/member/components/ActiveMemberTab.vue'
import PendingMemberTab from '@/views/tenant/member/components/PendingMemberTab.vue'
import InviteMemberTab from '@/views/tenant/member/components/InviteMemberTab.vue'
import InviteMemberModal from '@/views/tenant/member/components/InviteMemberModal.vue'

export default defineComponent({
  name: 'TenantAdminMemberList',
  components: {
    ActiveMemberTab,
    PendingMemberTab,
    InviteMemberTab,
    InviteMemberModal
  },
  props: ['viewSettings'],
  emits: ['onChangeView'],
  setup() {
    const { t } = useI18n()

    const membersStore = useTenantAdminMembersStore()
    const activeTab = ref<'active-members' | 'pending-request' | 'pending-invitations'>('active-members')
    const inviteModalRef = ref<any>(null)
    onMounted(() => {
      refreshDataFn();
    })

    const refreshDataFn =()=> {
      membersStore.fetchMembers();
    }
    
    const openInviteModal = () => {
      inviteModalRef.value?.open()
    }

    return {
      t,
      ACTIVE_TENANT_ID,
      activeTab,
      members: computed(() => membersStore.members),
      loading: computed(() => membersStore.loading),
      onTabChange: (tab: string) => (activeTab.value = tab as any),
      refreshDataFn,
      openInviteModal,
      inviteModalRef
    }
  }
})