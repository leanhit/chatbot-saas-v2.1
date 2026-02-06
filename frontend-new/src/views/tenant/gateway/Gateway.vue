<template>
  <div class="tenant-gateway">
    <div class="gateway-container">
      <!-- Header -->
      <div class="header-row">
        <div class="header-text">
          <h2>{{ $t('Title') }}</h2>
          <p class="header-subtitle">
            {{ $t('Subtitle') }}
          </p>
        </div>
        <div class="button-group">
          <button
            @click="showCreateModal = true"
            class="inline-flex items-center px-5 py-2.5 text-sm font-medium text-white bg-blue-700 rounded-lg hover:bg-blue-800 focus:ring-4 focus:outline-none focus:ring-blue-300 dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800"
          >
            <Icon icon="mdi:plus" class="h-4 w-4 mr-2" />
            {{ $t('Create') }}
          </button>
          <button
            @click="handleLogout"
            class="inline-flex items-center px-5 py-2.5 text-sm font-medium text-gray-900 bg-white rounded-lg border border-gray-200 hover:bg-gray-100 hover:text-blue-700 focus:z-10 focus:ring-4 focus:outline-none focus:ring-gray-200 dark:bg-gray-800 dark:text-gray-400 dark:border-gray-600 dark:hover:text-white dark:hover:bg-gray-700"
          >
            <Icon icon="mdi:logout" class="h-4 w-4 mr-2" />
            {{ $t('Logout') }}
          </button>
        </div>
      </div>
      
      <!-- Card with Tabs -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow-md border border-gray-200 dark:border-gray-700">
        <!-- Tabs -->
        <div class="tenant-tabs">
          <div class="border-b border-gray-200 dark:border-gray-700">
            <nav class="flex -mb-px">
              <button
                v-for="tab in tabs"
                :key="tab.name"
                @click="activeTab = tab.name"
                :class="[
                  'py-4 px-6 text-sm font-medium border-b-2 transition-colors',
                  activeTab === tab.name
                    ? 'border-primary text-primary dark:text-primary-400'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 dark:text-gray-400 dark:hover:text-gray-300'
                ]"
              >
                <Icon :icon="tab.icon" class="h-4 w-4 mr-2" />
                {{ $t(tab.label) }}
              </button>
            </nav>
          </div>

          <!-- Tab Content -->
          <div class="tab-content">
            <!-- My Tenants Tab -->
            <MyTenantTab v-if="activeTab === 'my'" @tenant-entered="onTenantEntered" />
            
            <!-- Search Tenants Tab -->
            <SearchTenantTab v-if="activeTab === 'search'" />
            
            <!-- Pending Tenants Tab -->
            <PendingTenantTab v-if="activeTab === 'pending'" />
            
            <!-- My Invitations Tab -->
            <UserInvitationsTab v-if="activeTab === 'my-invitations'" />
          </div>
        </div>
      </div>
    </div>
    
    <!-- Create Tenant Modal -->
    <CreateTenantModal
      :visible="showCreateModal"
      @close="showCreateModal = false"
      @created="onTenantCreated"
    />
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue'
import { Icon } from '@iconify/vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/authStore'
import { useGatewayTenantStore } from '@/stores/tenant/gateway/myTenantStore'
import MyTenantTab from './components/MyTenantTab.vue'
import SearchTenantTab from './components/SearchTenantTab.vue'
import PendingTenantTab from './components/PendingTenantTab.vue'
import UserInvitationsTab from './components/UserInvitationsTab.vue'
import CreateTenantModal from './components/CreateTenantModal.vue'

export default {
  name: 'Tenant',
  components: {
    Icon,
    MyTenantTab,
    SearchTenantTab,
    PendingTenantTab,
    UserInvitationsTab,
    CreateTenantModal
  },
  setup() {
    const { t } = useI18n()
    const router = useRouter()
    const authStore = useAuthStore()
    const tenantStore = useGatewayTenantStore()
    
    const activeTab = ref('my')
    const showCreateModal = ref(false)

    const tabs = [
      { name: 'my', label: t('Tenant'), icon: 'mdi:office-building' },
      { name: 'search', label: t('Search'), icon: 'mdi:search' },
      { name: 'pending', label: t('Pending'), icon: 'mdi:clock' },
      { name: 'my-invitations', label: t('My Invitations'), icon: 'mdi:email' }
    ]

    const onTenantEntered = () => {
      // Tenant entered successfully, can add any additional logic here
      console.log('Tenant entered successfully')
    }

    const onTenantCreated = async () => {
      try {
        showCreateModal.value = false
        await tenantStore.fetchUserTenants()
      } catch (error) {
        console.error('Failed to refresh tenants after creation:', error)
      }
    }

    const handleLogout = async () => {
      try {
        await authStore.logout()
        router.push('/login')
      } catch (error) {
        console.error('Logout failed:', error)
      }
    }

    let mounted = true

    onMounted(async () => {
      try {
        if (mounted) {
          await tenantStore.fetchUserTenants()
        }
      } catch (error) {
        console.error('Failed to fetch tenants on mount:', error)
      }
    })

    onUnmounted(() => {
      mounted = false
    })

    return {
      activeTab,
      showCreateModal,
      tabs,
      onTenantEntered,
      onTenantCreated,
      handleLogout
    }
  }
}
</script>

<style scoped>
.tenant-gateway {
  display: flex;
  justify-content: center;
  padding-top: 60px;
  min-height: 100vh;
  background-color: #f5f5f5;
  dark:bg-gray-900;
}

.gateway-container {
  width: 100%;
  max-width: 800px;
  margin: 0 20px;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding: 24px 0;
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
  dark:color-white;
}

.header-subtitle {
  margin: 4px 0 0;
  font-size: 14px;
  color: #909399;
  dark:color-gray-400;
  line-height: 1.5;
}

.card {
  background: white;
  dark:bg-gray-800;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  dark:border-gray-700;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.tenant-tabs {
  margin-top: 0;
}

.tab-content {
  padding: 20px 24px 24px;
}

/* Dark mode adjustments */
@media (prefers-color-scheme: dark) {
  .tenant-gateway {
    background-color: #1a1a1a;
  }
  
  .card {
    background: #1f2937;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.3);
  }
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .gateway-container {
    margin: 0 16px;
  }
  
  .header-row {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .button-group {
    justify-content: center;
  }
  
  .header-text {
    text-align: center;
  }
}
</style>
