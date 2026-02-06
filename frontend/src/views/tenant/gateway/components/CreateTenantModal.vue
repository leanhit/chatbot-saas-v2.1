<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="emit('update:visible', $event)"
    :title="t('Create Tenant')"
    width="420px"
    destroy-on-close
  >
    <el-form label-position="top">
      <el-form-item :label="t('Tenant Name')" required>
        <el-input
          v-model="form.name"
          :placeholder="t('Enter Tenant Name')"
        />
      </el-form-item>

      <el-form-item :label="t('Status')" required>
        <el-radio-group v-model="form.visibility">
          <el-radio label="PUBLIC">{{ t('Public')}}</el-radio>
          <el-radio label="PRIVATE">{{ t('Private')}}</el-radio>
        </el-radio-group>

        <div class="tip">
          {{
            form.visibility === 'PUBLIC'
              ? 'Mọi người có thể tìm thấy workspace này.'
              : 'Workspace này sẽ bị ẩn khỏi tìm kiếm.'
          }}
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="emit('update:visible', false)">
        Hủy
      </el-button>

      <el-button
        type="primary"
        :loading="loading"
        :disabled="!form.name.trim()"
        @click="handleSubmit"
      >
        Tạo ngay
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { reactive } from 'vue'
import type { TenantVisibility } from '@/types/tenant'
import { useGatewayCreateTenantStore } from '@/stores/tenant/gateway/createTenantStore'
import { useGatewayTenantStore } from '@/stores/tenant/gateway/myTenantStore'

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits(['update:visible'])

const { t } = useI18n()
const createTenantStore = useGatewayCreateTenantStore()
const { loading } = createTenantStore
const gatewayTenantStore = useGatewayTenantStore()

const form = reactive({
  name: '',
  visibility: 'PUBLIC' as TenantVisibility
})

const resetForm = () => {
  form.name = ''
  form.visibility = 'PUBLIC'
}

const handleSubmit = async () => {
  try {
    const success = await createTenantStore.createTenant({
      name: form.name.trim(),
      visibility: form.visibility
    })
    
    if (success) {
      // Refresh the tenant list after successful creation
      await gatewayTenantStore.fetchUserTenants()
      resetForm()
      emit('update:visible', false)
    }
  } catch {
    // lỗi đã được handle trong store
  }
}
</script>

<style scoped>
.tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
