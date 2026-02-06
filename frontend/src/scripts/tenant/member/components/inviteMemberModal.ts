import { defineComponent, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useTenantAdminMembersStore } from '@/stores/tenant/admin/tenantMembersStore'
import type { TenantRole } from '@/types/tenant'

export default defineComponent({
  name: 'InviteMemberModal',
  setup(props, { expose }) {
    const { t } = useI18n()
    const store = useTenantAdminMembersStore()
    
    const visible = ref(false)
    const loading = ref(false)
    const formRef = ref()

    const form = ref({
      email: '',
      role: 'MEMBER' as TenantRole,
      expiryDays: 7
    })

    // Phương thức mở modal từ component cha
    const open = () => {
      visible.value = true
    }

    // Reset form khi đóng modal
    const resetForm = () => {
      if (formRef.value) {
        formRef.value.resetFields()
      }
      form.value = { 
        email: '', 
        role: 'MEMBER' as TenantRole, 
        expiryDays: 7 
      }
    }

    const handleInvite = async () => {
      if (!formRef.value) return
      
      await formRef.value.validate(async (valid: boolean) => {
        if (valid) {
          loading.value = true
          try {
            // Đảm bảo truyền đúng payload mà Store yêu cầu
            await store.inviteUser({
              email: form.value.email,
              role: form.value.role,
              expiryDays: form.value.expiryDays
            })
            visible.value = false
          } finally {
            loading.value = false
          }
        }
      })
    }

    // Expose phương thức open để component cha có thể gọi thông qua Ref
    expose({
      open
    })

    return {
      t,
      visible,
      loading,
      form,
      formRef,
      open,
      resetForm,
      handleInvite
    }
  }
})