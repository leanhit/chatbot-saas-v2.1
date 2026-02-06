import { defineComponent, computed, onMounted } from 'vue'
import { useTenantAdminMembersStore } from '@/stores/tenant/admin/tenantMembersStore'

export default defineComponent({
  name: 'TenantJoinRequestTable',

  setup() {
    const store = useTenantAdminMembersStore()

    // ===== COMPUTED =====
    const pendingMembers = computed(() => store.pendingMembers)

    // ===== ACTIONS =====
    const approve = async (requestId: string) => {
      await store.approveJoin(requestId)
    }

    const reject = async (requestId: string) => {
      await store.rejectJoin(requestId)
    }

    // ===== LIFECYCLE =====
    onMounted(() => {
      store.fetchJoinRequests()
    })

    return {
      store,
      pendingMembers,
      approve,
      reject
    }
  }
})
