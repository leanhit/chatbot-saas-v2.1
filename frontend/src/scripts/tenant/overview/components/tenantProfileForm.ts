import { defineComponent, reactive, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'

import { useTenantAdminContextStore } from '@/stores/tenant/admin/tenantContextStore'
import { useTenantAdminSettingsStore } from '@/stores/tenant/admin/tenantSettingsStore'

export default defineComponent({
  name: 'TenantProfileForm',
  emits: ['onChangeView'],
  setup() {
    const { t } = useI18n()
    const contextStore = useTenantAdminContextStore()
    const settingsStore = useTenantAdminSettingsStore()

    /* ---------------- state ---------------- */
    const form = reactive<{
      taxCode?: string
      contactEmail?: string
      contactPhone?: string
    }>({
      taxCode: '',
      contactEmail: '',
      contactPhone: ''
    })

    /* ---------------- sync tenant → form ---------------- */
    watch(
      () => contextStore.tenant,
      tenant => {
        if (!tenant?.profile) return

        form.taxCode = tenant.profile.taxCode || ''
        form.contactEmail = tenant.profile.contactEmail || ''
        form.contactPhone = tenant.profile.contactPhone || ''
      },
      { immediate: true }
    )

    /* ---------------- actions ---------------- */
    const save = async () => {
      const tenant = contextStore.tenant
      if (!tenant) return

      try {
        await settingsStore.updateProfile(
          tenant.id.toString(),
          {
            taxCode: form.taxCode,
            contactEmail: form.contactEmail,
            contactPhone: form.contactPhone
          }
        )

        await contextStore.loadTenant()
        //ElMessage.success(t('Profile updated'))
      } catch (error) {
        console.error(error)
        //ElMessage.error(t('Update failed'))
      }
    }

    return {
      t,
      form,
      save
    }
  }
})
