<script lang="ts">
import { defineComponent, ref, watch, onUnmounted, computed } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'

import { useGatewaySearchStore } from '@/stores/tenant/gateway/searchTenantStore'
import type { TenantSearchResponse } from '@/types/tenant'

export default defineComponent({
  name: 'SearchTenantTab',
  components: { Search },

  setup() {
    const { t } = useI18n()
    const searchStore = useGatewaySearchStore()

    const keyword = ref('')
    let debounceTimer: number | null = null

    const results = computed(() => searchStore.results)
    const loading = computed(() => searchStore.loading)

    const executeSearch = async () => {
      await searchStore.searchTenants(keyword.value.trim())
    }

    const handleManualSearch = async () => {
      if (!keyword.value.trim()) return
      await executeSearch()
    }

    const onJoinClick = async (tenantId: number) => {
      try {
        await searchStore.requestJoinTenant(String(tenantId))
        ElMessage.success(t('Tenant.JoinRequested'))

        // update UI immediately
        const tenant = searchStore.results.find(t => t.id === tenantId)
        if (tenant) {
          tenant.membershipStatus = 'PENDING'
        }
      } catch {
        ElMessage.error(t('Tenant.JoinFailed'))
      }
    }

    const maskEmail = (email?: string | null) => {
      if (!email) return ''
      const [name, domain] = email.split('@')
      if (!domain) return email
      return `${name.slice(0, 2)}***@${domain}`
    }

    watch(keyword, (newVal) => {
      if (debounceTimer) clearTimeout(debounceTimer)

      if (!newVal.trim()) {
        searchStore.clearResults()
        return
      }

      if (newVal.trim().length >= 2) {
        debounceTimer = window.setTimeout(() => {
          executeSearch()
        }, 500)
      }
    })

    onUnmounted(() => {
      if (debounceTimer) clearTimeout(debounceTimer)
      searchStore.clearResults()
    })

    return {
      t,
      Search,
      keyword,
      loading,
      results,
      handleManualSearch,
      onJoinClick,
      maskEmail
    }
  }
})
</script>

<template>
  <div class="search-tenant-container">
    <!-- Search box -->
    <div class="search-box">
      <el-input
        v-model="keyword"
        :placeholder="t('Search Tenant')"
        class="search-input-large"
        clearable
        @keyup.enter="handleManualSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #append>
          <el-button :loading="loading" @click="handleManualSearch">
            {{ t('Search') }}
          </el-button>
        </template>
      </el-input>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="3" animated />
    </div>

    <!-- Result -->
    <template v-else>
      <el-empty
        v-if="results.length === 0"
        :description="keyword
          ? t('Search Empty')
          : t('Search Hint')"
      />

      <div v-else class="search-results-grid">
        <el-row :gutter="20">
          <el-col
            v-for="tenant in results"
            :key="tenant.id"
            :xs="24"
            :sm="12"
          >
            <el-card class="search-item-card" shadow="hover">
              <div class="search-item-content">
                <!-- LEFT -->
                <div class="info-side">
                  <el-avatar
                    :size="45"
                    class="tenant-avatar-search"
                    :src="tenant.logoUrl || undefined"
                  >
                    {{ tenant.name?.charAt(0).toUpperCase() }}
                  </el-avatar>

                  <div class="text-info">
                    <div class="tenant-name">
                      {{ tenant.name }}
                    </div>

                    <div class="tenant-meta">
                      <span>ID: {{ tenant.id }}</span><br />
                      <span v-if="tenant.province">
                        {{ t('Province') }}: {{ tenant.province }}
                      </span>
                    </div>

                    <el-tooltip
                      v-if="tenant.contactEmail"
                      :content="tenant.contactEmail"
                    >
                      <div class="tenant-email">
                        {{ t('ContactEmail') }}:
                        {{ maskEmail(tenant.contactEmail) }}
                      </div>
                    </el-tooltip>
                  </div>
                </div>

                <!-- RIGHT -->
                <div class="action-side">
                  <el-button
                    v-if="tenant.membershipStatus === 'NONE'"
                    type="primary"
                    plain
                    size="small"
                    @click="onJoinClick(tenant.id)"
                  >
                    {{ t('Join') }}
                  </el-button>

                  <el-tag
                    v-else-if="tenant.membershipStatus === 'PENDING'"
                    type="warning"
                    effect="dark"
                  >
                    {{ t('Pending') }}
                  </el-tag>

                  <el-tag
                    v-else-if="tenant.membershipStatus === 'APPROVED'"
                    type="success"
                  >
                    {{ t('Member') }}
                  </el-tag>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </template>
  </div>
</template>

<style scoped>
.search-box {
  margin-bottom: 30px;
  display: flex;
  justify-content: center;
}

.search-input-large {
  max-width: 600px;
}

.search-item-card {
  margin-bottom: 16px;
  border-radius: 10px;
}

.search-item-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-side {
  display: flex;
  align-items: center;
  gap: 12px;
}

.text-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.tenant-name {
  font-weight: 600;
  color: #2c3e50;
}

.tenant-meta {
  font-size: 12px;
  color: #909399;
}

.tenant-email {
  font-size: 12px;
  color: #606266;
}

.tenant-avatar-search {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}
</style>
