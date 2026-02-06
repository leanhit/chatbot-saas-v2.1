import { defineComponent, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessageBox, ElMessage } from 'element-plus'
import { View, Delete } from '@element-plus/icons-vue'
import { useTenantAdminMembersStore } from '@/stores/tenant/admin/tenantMembersStore'
import type { TenantRole } from '@/types/tenant'

export default defineComponent({
  name: 'ActiveMemberTab',
  components: { View, Delete },
  setup() {
    const { t } = useI18n()
    const store = useTenantAdminMembersStore()

    const members = computed(() =>
      store.activeMembers.filter(m => m.status === 'ACTIVE' || m.status === null)
    )

    const loading = computed(() => store.loading)

    const canChangeRole = (role: TenantRole) => role !== 'OWNER'

    const handleRoleChange = async (row: any, newRole: TenantRole) => {
      try {
        await ElMessageBox.confirm(
          t('Are you sure you want to change this member\'s role to {role}?', { role: newRole }),
          t('Confirm Change'),
          { type: 'warning' }
        )
        await store.updateRole(row.userId, newRole)
        ElMessage.success(t('Role updated successfully'))
      } catch {
        // Trả lại role cũ nếu user cancel confirm
        await store.fetchMembers()
      }
    }

    const handleRemove = async (row: any) => {
      try {
        await ElMessageBox.confirm(
          t('Are you sure you want to remove this member from the organization?'),
          t('Confirm Removal'),
          { 
            type: 'error',
            confirmButtonClass: 'el-button--danger'
          }
        )
        await store.removeMember(row.userId)
        ElMessage.success(t('Member removed successfully'))
      } catch {
        // cancel
      }
    }

    const viewDetails = (row: any) => {
      console.log('Member Details:', row)
      ElMessage.info(t('Feature under development'))
    }

    return {
      t,
      members,
      loading,
      canChangeRole,
      handleRoleChange,
      handleRemove,
      viewDetails
    }
  }
})