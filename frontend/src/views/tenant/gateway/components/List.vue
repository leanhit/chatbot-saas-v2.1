<template>
  <div class="tenant-gateway">
    <el-card class="card">
      <div class="header-row">
        <div class="header-text">
          <h2>{{ t('Title') }}</h2>
          <p class="header-subtitle">
            {{ t('Subtitle') }}
          </p>
        </div>

        <div class="button-group">
          <el-button
            type="primary"
            icon="Plus"
            @click="showCreateModal = true"
          >
            {{ t('Create') }}
          </el-button>
          <el-button
            type="danger"
            icon="SwitchButton"
            @click="handleLogout"
          >
            {{ t('Logout') }}
          </el-button>
        </div>
      </div>

      <el-tabs
        v-model="activeTab"
        class="tenant-tabs"
        @tab-change="onTabChange"
      >
        <el-tab-pane :label="t('Tenant')" name="my">
          <MyTenantTab v-if="activeTab === 'my'" />
        </el-tab-pane>

        <el-tab-pane :label="t('Search')" name="search">
          <SearchTenantTab v-if="activeTab === 'search'" />
        </el-tab-pane>

        <el-tab-pane :label="t('Pending')" name="pending">
          <PendingTenantTab v-if="activeTab === 'pending'" />
        </el-tab-pane>
        
        <el-tab-pane :label="t('My Invitations')" name="my-invitations">
          <UserInvitationsTab  v-if="activeTab === 'my-invitations'" />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <CreateTenantModal
      v-model:visible="showCreateModal"
      @created="onTenantCreated"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { useGatewayTenantStore } from '@/stores/tenant/gateway/myTenantStore'

import MyTenantTab from '@/views/tenant/gateway/components/MyTenantTab.vue'
import CreateTenantModal from '@/views/tenant/gateway/components/CreateTenantModal.vue'
import PendingTenantTab from '@/views/tenant/gateway/components/PendingTenantTab.vue'
import SearchTenantTab from '@/views/tenant/gateway/components/SearchTenantTab.vue'
import UserInvitationsTab from '@/views/tenant/gateway/components/UserInvitationsTab.vue'

export default defineComponent({
  name: 'TenantGateway',
  components: {
    MyTenantTab,
    CreateTenantModal,
    PendingTenantTab,
    SearchTenantTab,
    UserInvitationsTab
  },
  setup() {
    const { t } = useI18n()
    const router = useRouter()
    const authStore = useAuthStore()
    const tenantStore = useGatewayTenantStore()

    console.log('🚀 List.vue setup() called!')
    console.log('🏪 Components available:', { MyTenantTab, CreateTenantModal, PendingTenantTab, SearchTenantTab, UserInvitationsTab })

    const activeTab = ref<'my' | 'search' | 'pending' | 'my-invitations'>('search')
    const showCreateModal = ref(false)

    onMounted(() => {
      console.log('🚀 List.vue onMounted() called!')
      console.log('📋 Active tab on mount:', activeTab.value)
      tenantStore.fetchUserTenants()
      
      // Check if SearchTenantTab component is available
      console.log('🔍 Checking SearchTenantTab component...')
      if (SearchTenantTab) {
        console.log('✅ SearchTenantTab component is available')
      } else {
        console.log('❌ SearchTenantTab component is NOT available')
      }
    })

    const onTabChange = (tab: string) => {
      activeTab.value = tab as any
    }

    const onTenantCreated = async () => {
      showCreateModal.value = false
      await tenantStore.fetchUserTenants()
    }

    const handleLogout = async () => {
      try {
        await authStore.logout()
        router.push('/login')
      } catch (error) {
        console.error('Logout failed:', error)
      }
    }

    return {
      t,
      activeTab,
      showCreateModal,
      onTabChange,
      onTenantCreated,
      handleLogout
    }
  }
})
</script>

<style scoped>
.tenant-gateway {
  display: flex;
  justify-content: center;
  padding-top: 60px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.card {
  width: 800px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.button-group {
  display: flex;
  gap: 10px;
}

.header-text h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.header-subtitle {
  margin: 4px 0 0;
  font-size: 14px;
  color: #909399;
  line-height: 1.5;
}

.tenant-tabs {
  margin-top: 10px;
}

:deep(.el-tabs__item) {
  font-weight: 600;
  font-size: 15px;
}
</style>
