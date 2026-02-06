<script lang="ts">
import { defineComponent, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

import { useGatewayTenantStore } from '@/stores/tenant/gateway/myTenantStore'
import type { TenantDetailResponse } from '@/types/tenant'
import { formatDateTime } from '@/utils/search'

export default defineComponent({
  name: 'MyTenantTab',
  setup() {
    const { t } = useI18n()
    const router = useRouter()
    const tenantStore = useGatewayTenantStore()

    const tenantList = computed(() => tenantStore.userTenants)
    const loading = computed(() => tenantStore.loadingTenants)

    const enterWorkspace = async (tenant: TenantDetailResponse) => {
      if (tenant.status !== 'ACTIVE') return

      await tenantStore.switchTenant(String(tenant.id))
      router.push('/home') // chỉnh route theo app của bạn
    }

    // (optional) nếu backend hỗ trợ
    const suspendTenant = async (id: number) => {
      console.warn('Suspend tenant not implemented', id)
      await tenantStore.suspendTenant(id)
      await tenantStore.fetchUserTenants()
    }

    const activateTenant = async (id: number) => {
      console.warn('Activate tenant not implemented', id)
      await tenantStore.activateTenant(id)
      await tenantStore.fetchUserTenants()
    }

    return {
      t,
      loading,
      tenantList,
      enterWorkspace,
      suspendTenant,
      activateTenant,
      formatDateTime
    }
  }
})
</script>

<template>
  <div class="my-tenant-tab">
    <div v-if="loading" class="loading-state">
      <el-row :gutter="20">
        <el-col v-for="i in 3" :key="i" :xs="24" :sm="12" :md="8">
          <el-skeleton style="margin-bottom: 20px" animated>
            <template #template>
              <el-card shadow="never">
                <el-skeleton-item
                  variant="image"
                  style="width: 40px; height: 40px; border-radius: 50%"
                />
                <div style="padding: 14px 0">
                  <el-skeleton-item variant="p" style="width: 50%" />
                  <el-skeleton-item variant="text" />
                </div>
              </el-card>
            </template>
          </el-skeleton>
        </el-col>
      </el-row>
    </div>

    <template v-else>
      <el-empty
        v-if="tenantList.length === 0"
        :description="t('Empty')"
      />

      <el-row v-else :gutter="20" class="tenant-grid">
        <el-col
          v-for="tenant in tenantList"
          :key="tenant.id"
          :xs="24"
          :sm="12"
          :lg="8"
          class="mb-4"
        >
          <el-card class="tenant-card" shadow="hover" :body-style="{ padding: '20px' }">
            <div class="card-header">
              <el-avatar
                :size="48"
                :src="tenant.profile?.logoUrl"
                :class="['tenant-avatar', { 'is-inactive': tenant.status !== 'ACTIVE' }]"
              >
                {{ tenant.name.charAt(0).toUpperCase() }}
              </el-avatar>

              <div class="header-main">
                <h3 class="tenant-name text-truncate" :title="tenant.name">
                  {{ tenant.name }}
                </h3>
                <el-tag
                  :type="tenant.status === 'ACTIVE' ? 'success' : 'info'"
                  size="small"
                  effect="light"
                >
                  {{ tenant.status === 'ACTIVE'
                    ? t('Active')
                    : t('Inactive') }}
                </el-tag>
              </div>
            </div>

            <div class="card-content">
              <div class="info-item">
                <span class="label">ID:</span>
                <span class="value">#{{ tenant.id }}</span>
              </div>
              <div v-if="tenant.profile?.contactEmail" class="info-item">
                <span class="label">Email:</span>
                <span class="value text-truncate">
                  {{ tenant.profile.contactEmail }}
                </span>
              </div>
              <div class="info-item">
                <span class="label">{{ t('Expire') }}:</span>
                <span class="value">
                  {{ formatDateTime(tenant.expiresAt) }}
                </span>
              </div>
            </div>

            <div class="card-footer">
              <div class="action-buttons">
                <el-button
                  v-if="tenant.status === 'ACTIVE'"
                  type="danger"
                  link
                  @click="suspendTenant(tenant.id)"
                >
                  {{ t('Suspend') }}
                </el-button>

                <el-button
                  v-else
                  type="success"
                  link
                  @click="activateTenant(tenant.id)"
                >
                  {{ t('Activate') }}
                </el-button>

                <el-button
                  type="primary"
                  :disabled="tenant.status !== 'ACTIVE'"
                  @click="enterWorkspace(tenant)"
                >
                  {{ t('Enter') }}
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<style scoped>
.tenant-grid {
  margin-top: 10px;
}

.mb-4 {
  margin-bottom: 20px;
}

.tenant-card {
  transition: all 0.3s;
  border-radius: 12px;
  border: 1px solid #ebeef5;
}

.tenant-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.05);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}

.header-main {
  flex: 1;
  overflow: hidden;
}

.tenant-name {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-content {
  background-color: #f8f9fb;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  font-size: 13px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.label {
  color: #909399;
}

.value {
  color: #606266;
  font-weight: 500;
}

.card-footer {
  border-top: 1px solid #f0f0f0;
  padding-top: 15px;
}

.action-buttons {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tenant-avatar.is-inactive {
  filter: grayscale(1);
  opacity: 0.6;
}

.text-truncate {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>