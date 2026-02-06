<script lang="ts">
import { defineComponent, computed, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'

import { useGatewayPendingStore } from '@/stores/tenant/gateway/pendingTenantStore'
import type { TenantPendingResponse } from '@/types/tenant'

export default defineComponent({
  name: 'PendingTenantTab',
  setup() {
    const { t } = useI18n()
    const pendingStore = useGatewayPendingStore()

    const tenants = computed<TenantPendingResponse[]>(
      () => pendingStore.pendingTenants
    )
    const loading = computed(() => pendingStore.loading)

    onMounted(() => {
      pendingStore.fetchMyPendingTenants()
    })

    onUnmounted(() => {
      pendingStore.clearPending()
    })

    const formatDate = (value: string) => {
      if (!value) return ''
      try {
        return new Date(value).toLocaleString('vi-VN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        })
      } catch {
        return value
      }
    }

    const getVisibilityType = (visibility: string) => {
      return visibility === 'PUBLIC' ? 'success' : 'info'
    }

    return {
      t,
      tenants,
      loading,
      formatDate,
      getVisibilityType
    }
  }
})
</script>

<template>
  <!-- Loading -->
  <el-skeleton v-if="loading" :rows="3" animated />

  <!-- Empty -->
  <el-empty
    v-else-if="tenants.length === 0"
    :description="t('Empty')"
  />

  <!-- List -->
  <el-space
    v-else
    direction="vertical"
    style="width: 100%"
  >
    <el-card
      v-for="tenant in tenants"
      :key="tenant.id"
      shadow="never"
      class="pending-card"
    >
      <div class="item-content">
        <div class="info">
          <div class="name">
            {{ tenant.name }}
          </div>

          <div class="meta">
            <el-tag
              size="small"
              effect="plain"
              :type="getVisibilityType(tenant.visibility)"
            >
              {{ tenant.visibility }}
            </el-tag>

            <span class="time">
              {{ t('RequestedAt') }}:
              {{ formatDate(tenant.requestedAt) }}
            </span>
          </div>
        </div>

        <el-tag type="warning" effect="light">
          {{ t('Pending') }}
        </el-tag>
      </div>
    </el-card>
  </el-space>
</template>

<style scoped>
.pending-card {
  width: 100%;
}

.item-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.name {
  font-weight: 600;
  font-size: 14px;
}

.meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
