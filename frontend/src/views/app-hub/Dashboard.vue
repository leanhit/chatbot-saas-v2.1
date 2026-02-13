<template>
  <div class="app-hub-dashboard">
    <div class="dashboard-header">
      <h1>App Hub Dashboard</h1>
      <div class="header-actions">
        <button @click="createApp" class="btn btn-primary">
          <Icon icon="mdi:plus" />
          Create App
        </button>
        <button @click="refreshData" class="btn btn-secondary">
          <Icon icon="mdi:refresh" />
          Refresh
        </button>
      </div>
    </div>

    <!-- Statistics Cards -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">
          <Icon icon="mdi:application" />
        </div>
        <div class="stat-content">
          <h3>{{ stats.totalApps }}</h3>
          <p>Total Apps</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon active">
          <Icon icon="mdi:check-circle" />
        </div>
        <div class="stat-content">
          <h3>{{ stats.activeApps }}</h3>
          <p>Active Apps</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">
          <Icon icon="mdi:account-group" />
        </div>
        <div class="stat-content">
          <h3>{{ stats.totalSubscriptions }}</h3>
          <p>Subscriptions</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">
          <Icon icon="mdi:public" />
        </div>
        <div class="stat-content">
          <h3>{{ stats.publicApps }}</h3>
          <p>Public Apps</p>
        </div>
      </div>
    </div>

    <!-- Recent Apps -->
    <div class="dashboard-content">
      <div class="content-section">
        <div class="section-header">
          <h2>Recent Apps</h2>
          <router-link to="/app-hub/apps" class="view-all">View All</router-link>
        </div>
        <div class="recent-apps-grid">
          <div v-for="app in recentApps" :key="app.id" class="app-card">
            <div class="app-header">
              <div class="app-icon">
                <Icon icon="mdi:application" />
              </div>
              <div class="app-info">
                <h3>{{ app.name }}</h3>
                <p>{{ app.displayName }}</p>
              </div>
              <div class="app-status">
                <span :class="getStatusClass(app.status)">{{ app.status }}</span>
              </div>
            </div>
            <div class="app-body">
              <div class="app-meta">
                <span class="app-type">{{ app.appType }}</span>
                <span class="app-version">v{{ app.version }}</span>
                <span class="app-visibility" :class="{ 'public': app.isPublic }">
                  {{ app.isPublic ? 'Public' : 'Private' }}
                </span>
              </div>
              <div class="app-actions">
                <button @click="viewApp(app)" class="btn btn-sm">View</button>
                <button @click="editApp(app)" class="btn btn-sm btn-secondary">Edit</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Quick Actions -->
      <div class="content-section">
        <h2>Quick Actions</h2>
        <div class="quick-actions-grid">
          <div class="action-card" @click="createApp">
            <div class="action-icon">
              <Icon icon="mdi:plus-circle" />
            </div>
            <h3>Create New App</h3>
            <p>Register a new application</p>
          </div>
          
          <div class="action-card" @click="navigateToMarketplace">
            <div class="action-icon">
              <Icon icon="mdi:store" />
            </div>
            <h3>Browse Marketplace</h3>
            <p>Discover public apps</p>
          </div>
          
          <div class="action-card" @click="navigateToSubscriptions">
            <div class="action-icon">
              <Icon icon="mdi:credit-card" />
            </div>
            <h3>Manage Subscriptions</h3>
            <p>View and manage subscriptions</p>
          </div>
          
          <div class="action-card" @click="navigateToAnalytics">
            <div class="action-icon">
              <Icon icon="mdi:chart-line" />
            </div>
            <h3>View Analytics</h3>
            <p>App usage statistics</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAppStore } from '@/stores/appStore'

const router = useRouter()
const appStore = useAppStore()

const stats = ref({
  totalApps: 0,
  activeApps: 0,
  totalSubscriptions: 0,
  publicApps: 0
})

const recentApps = ref([])

onMounted(async () => {
  await loadDashboardData()
})

const loadDashboardData = async () => {
  try {
    // Load recent apps
    await appStore.fetchApps({ limit: 6, sortBy: 'createdAt', sortDirection: 'desc' })
    recentApps.value = appStore.apps.slice(0, 6)
    
    // Calculate stats
    stats.value = {
      totalApps: appStore.apps.length,
      activeApps: appStore.apps.filter(app => app.status === 'ACTIVE').length,
      totalSubscriptions: appStore.subscriptions.length,
      publicApps: appStore.apps.filter(app => app.isPublic).length
    }
  } catch (error) {
    console.error('Failed to load dashboard data:', error)
  }
}

const createApp = () => {
  router.push('/app-hub/apps/create')
}

const viewApp = (app) => {
  router.push(`/app-hub/apps/${app.id}`)
}

const editApp = (app) => {
  router.push(`/app-hub/apps/${app.id}/edit`)
}

const navigateToMarketplace = () => {
  router.push('/app-hub/marketplace')
}

const navigateToSubscriptions = () => {
  router.push('/app-hub/subscriptions')
}

const navigateToAnalytics = () => {
  router.push('/app-hub/analytics')
}

const refreshData = () => {
  loadDashboardData()
}

const getStatusClass = (status) => {
  return {
    'status-active': status === 'ACTIVE',
    'status-inactive': status === 'INACTIVE',
    'status-pending': status === 'PENDING'
  }
}
</script>

<style scoped>
.app-hub-dashboard {
  padding: 24px;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.dashboard-header h1 {
  font-size: 2rem;
  font-weight: 600;
  color: #1f2937;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f3f4f6;
  color: #6b7280;
}

.stat-icon.active {
  background: #d1fae5;
  color: #059669;
}

.stat-content h3 {
  font-size: 2rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.stat-content p {
  color: #6b7280;
  margin: 4px 0 0 0;
}

.dashboard-content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 32px;
}

.content-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h2 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.view-all {
  color: #3b82f6;
  text-decoration: none;
  font-weight: 500;
}

.recent-apps-grid {
  display: grid;
  gap: 16px;
}

.app-card {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.2s;
}

.app-card:hover {
  border-color: #3b82f6;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.app-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.app-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
}

.app-info {
  flex: 1;
}

.app-info h3 {
  font-size: 1rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 4px 0;
}

.app-info p {
  color: #6b7280;
  font-size: 0.875rem;
  margin: 0;
}

.app-status {
  margin-top: 4px;
}

.status-active {
  background: #d1fae5;
  color: #059669;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
}

.status-inactive {
  background: #fee2e2;
  color: #dc2626;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
}

.app-meta {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.app-type, .app-version, .app-visibility {
  font-size: 0.75rem;
  padding: 2px 6px;
  border-radius: 4px;
  background: #f3f4f6;
  color: #6b7280;
}

.app-visibility.public {
  background: #dbeafe;
  color: #1e40af;
}

.app-actions {
  display: flex;
  gap: 8px;
}

.quick-actions-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.action-card {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
}

.action-card:hover {
  border-color: #3b82f6;
  background: #f8fafc;
}

.action-icon {
  width: 48px;
  height: 48px;
  margin: 0 auto 12px;
  border-radius: 50%;
  background: #eff6ff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #3b82f6;
}

.action-card h3 {
  font-size: 1rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.action-card p {
  color: #6b7280;
  font-size: 0.875rem;
  margin: 0;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary {
  background: #3b82f6;
  color: white;
}

.btn-primary:hover {
  background: #2563eb;
}

.btn-secondary {
  background: #f3f4f6;
  color: #374151;
}

.btn-secondary:hover {
  background: #e5e7eb;
}

.btn-sm {
  padding: 4px 8px;
  font-size: 0.875rem;
}

@media (max-width: 768px) {
  .dashboard-content {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .quick-actions-grid {
    grid-template-columns: 1fr;
  }
}
</style>
