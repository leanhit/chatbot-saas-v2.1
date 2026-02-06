import { defineComponent, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'

import { useTenantAdminContextStore } from '@/stores/tenant/admin/tenantContextStore'
import { useTenantAdminSettingsStore } from '@/stores/tenant/admin/tenantSettingsStore'

export default defineComponent({
  name: 'TenantAvatar',
  emits: ['onChangeView'],
  components: {
    Camera
  },
  setup() {
    const contextStore = useTenantAdminContextStore()
    const settingsStore = useTenantAdminSettingsStore()

    /* ---------------- computed ---------------- */
    const loading = computed(() => contextStore.loading)
    const tenant = computed(() => contextStore.tenant)
    
    // Log để debug
    // onMounted(() => {
    //   console.log('Tenant data in Avatar:', tenant.value)
    //   console.log('Loading state:', loading.value)
    // })

    const avatarUrl = computed(() => {
      if (!tenant.value?.profile) {
        console.log('No profile data available')
        return ''
      }
      console.log('Avatar URL:', tenant.value.profile.logoUrl)
      return tenant.value.profile.logoUrl || ''
    })

    const fallback = computed(() => {
      if (!tenant.value?.name) return ''
      return tenant.value.name.charAt(0).toUpperCase()
    })

    /* ---------------- upload guards ---------------- */
    const beforeUpload = (file: File) => {
      if (!file.type.startsWith('image/')) {
        ElMessage.error('Chỉ chấp nhận định dạng ảnh')
        return false
      }
      if (file.size / 1024 / 1024 > 2) {
        ElMessage.error('Dung lượng ảnh phải nhỏ hơn 2MB')
        return false
      }
      return true
    }

    /* ---------------- actions ---------------- */
    const onChange = async (file: any) => {
      if (!file?.raw || !tenant.value) return

      try {
        await settingsStore.updateProfile(
          tenant.value.id.toString(),
          {
            logoUrl: file.raw // backend nhận MultipartFile
          } as any
        )

        // reload tenant context
        await contextStore.loadTenant()

        //ElMessage.success('Cập nhật logo thành công')
      } catch (error) {
        console.error(error)
        //ElMessage.error('Không thể upload ảnh')
      }
    }

    return {
      loading,
      tenant,
      avatarUrl,
      fallback,
      beforeUpload,
      onChange
    }
  }
})
