import { defineComponent, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useTenantAdminMembersStore } from '@/stores/tenant/admin/tenantMembersStore'
import { Delete } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

export default defineComponent({
  name: 'InviteMemberTab',
  setup() {
    const { t } = useI18n()
    const store = useTenantAdminMembersStore()

    // Lấy dữ liệu ngay khi tab được render
    onMounted(() => {
      store.fetchInvitations()
    })

    // Getters từ store
    const pendingInvitations = computed(() => store.pendingInvitations)
    const loading = computed(() => store.loading)

    const handleRevoke = async (id: number) => {
      try {
        await store.revokeInvitationAction(id)
      } catch (error) {
        console.error('Revoke invitation failed:', error)
      }
    }

    const formatDate = (date: string) => {
      if (!date) return '-'
      // Format theo kiểu Việt Nam hoặc dùng dayjs cho linh hoạt
      return dayjs(date).format('DD/MM/YYYY HH:mm')
    }

    return {
      t,
      Delete,
      loading,
      pendingInvitations,
      handleRevoke,
      formatDate
    }
  }
})