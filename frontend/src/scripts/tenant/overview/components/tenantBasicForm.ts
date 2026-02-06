import { defineComponent, reactive, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'

import { useTenantAdminContextStore } from '@/stores/tenant/admin/tenantContextStore'
import { useTenantAdminSettingsStore } from '@/stores/tenant/admin/tenantSettingsStore'
import type { TenantBasicInfoRequest } from '@/types/tenant'

export default defineComponent({
  name: 'TenantBasicForm',
  emits: ['onChangeView'],
  setup() {
    const { t } = useI18n()
    const contextStore = useTenantAdminContextStore()
    const settingsStore = useTenantAdminSettingsStore()

    /* ---------------- state ---------------- */
    const form = reactive<TenantBasicInfoRequest>({
      name: '',
      status: 'ACTIVE',
      expiresAt: null,
      visibility: 'PUBLIC'
    })

    /* ---------------- sync tenant → form ---------------- */
    watch(
      () => contextStore.tenant,
      tenant => {
        if (!tenant) return
        form.name = tenant.name
        form.status = tenant.status
        form.expiresAt = tenant.expiresAt
        form.visibility = tenant.visibility || 'PRIVATE'
      },
      { immediate: true }
    )

    /* ---------------- actions ---------------- */
    const save = async () => {
      const tenant = contextStore.tenant
      if (!tenant) return

      try {
        await settingsStore.updateBasicInfo(
          tenant.id.toString(),
          {
            name: form.name,
            status: form.status,
            expiresAt: form.expiresAt,
            visibility: form.visibility
          }
        )

        await contextStore.loadTenant()
        //ElMessage.success(t('Tenant updated'))
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
