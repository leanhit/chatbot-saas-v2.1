import { defineComponent, reactive, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { TenantVisibility } from '@/types/tenant'
import { useGatewayCreateTenantStore } from '@/stores/tenant/gateway/createTenantStore'
import { useGatewayTenantStore } from '@/stores/tenant/gateway/myTenantStore'

export default defineComponent({
  name: 'CreateTenantModal', // Bạn có thể đặt tên tùy chọn
  props: {
    visible: {
      type: Boolean,
      required: true
    }
  },
  emits: ['update:visible'],
  
  setup(props, { emit }) {
    const { t } = useI18n()
    
    /* ---------------- Stores ---------------- */
    const createTenantStore = useGatewayCreateTenantStore()
    const gatewayTenantStore = useGatewayTenantStore()

    /* ---------------- State ---------------- */
    const form = reactive({
      name: '',
      visibility: 'PUBLIC' as TenantVisibility
    })

    /* ---------------- Computed ---------------- */
    // Đảm bảo tính reactive cho loading từ store
    const loading = computed(() => createTenantStore.loading)

    /* ---------------- Methods ---------------- */
    const resetForm = () => {
      form.name = ''
      form.visibility = 'PUBLIC'
    }

    const handleCancel = () => {
      resetForm()
      emit('update:visible', false)
    }

    const handleSubmit = async () => {
      if (!form.name.trim()) return

      try {
        const success = await createTenantStore.createTenant({
          name: form.name.trim(),
          visibility: form.visibility
        })
        
        if (success) {
          // Refresh danh sách sau khi tạo thành công
          await gatewayTenantStore.fetchUserTenants()
          resetForm()
          emit('update:visible', false)
        }
      } catch (error) {
        // Lỗi thường đã được xử lý trong store (hiển thị thông báo)
        console.error('Error creating tenant:', error)
      }
    }

    return {
      t,
      form,
      loading,
      handleSubmit,
      handleCancel,
      resetForm
    }
  }
})