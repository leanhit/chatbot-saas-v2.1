<template>
  <div class="dashboard-container">
    <!-- Dashboard Header -->
    <div class="dashboard-header">
      <div class="header-content">
        <h1 class="dashboard-title">Dashboard Overview</h1>
        <p class="dashboard-subtitle">Welcome back! Here's what's happening with your bots today.</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" class="action-button">
          <i class="mdi mdi-plus"></i>
          Create New Bot
        </el-button>
      </div>
    </div>

    <!-- Stats Grid -->
    <div class="stats-grid">
      <StatCard
        v-for="stat in statsData"
        :key="stat.id"
        :title="stat.title"
        :value="stat.value"
        :change="stat.change"
        :icon="stat.icon"
        :color="stat.color"
      />
    </div>

    <!-- Main Content Grid -->
    <div class="content-grid">
      <!-- Recent Activity -->
      <div class="grid-item grid-item-large">
        <ActivityCard
          title="Recent Activity"
          :activities="recentActivities"
        />
      </div>

      <!-- Quick Actions -->
      <div class="grid-item">
        <QuickActionsCard
          title="Quick Actions"
          :actions="quickActions"
        />
      </div>

      <!-- Bot Status -->
      <div class="grid-item">
        <BotStatusCard
          title="Bot Status"
          :bots="botStatusData"
        />
      </div>

      <!-- Performance Chart -->
      <div class="grid-item grid-item-wide">
        <PerformanceCard
          title="Performance Overview"
          :chart-data="performanceData"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import StatCard from './components/StatCard.vue'
import ActivityCard from './components/ActivityCard.vue'
import QuickActionsCard from './components/QuickActionsCard.vue'
import BotStatusCard from './components/BotStatusCard.vue'
import PerformanceCard from './components/PerformanceCard.vue'

// Mock data - no API calls, no stores
const statsData = ref([
  {
    id: 1,
    title: 'Total Bots',
    value: '24',
    change: '+12%',
    icon: 'mdi-robot',
    color: '#6366f1'
  },
  {
    id: 2,
    title: 'Active Conversations',
    value: '1,429',
    change: '+23%',
    icon: 'mdi-message-text',
    color: '#10b981'
  },
  {
    id: 3,
    title: 'Response Rate',
    value: '94.2%',
    change: '+5%',
    icon: 'mdi-speedometer',
    color: '#f59e0b'
  },
  {
    id: 4,
    title: 'Total Users',
    value: '8,392',
    change: '+18%',
    icon: 'mdi-account-group',
    color: '#8b5cf6'
  }
])

const recentActivities = ref([
  {
    id: 1,
    type: 'bot_created',
    title: 'New bot created',
    description: 'Customer Support Bot v2.0',
    time: '2 hours ago',
    icon: 'mdi-robot',
    color: '#6366f1'
  },
  {
    id: 2,
    type: 'conversation',
    title: 'High conversation volume',
    description: 'Sales bot handled 150+ conversations',
    time: '4 hours ago',
    icon: 'mdi-message-text',
    color: '#10b981'
  },
  {
    id: 3,
    type: 'update',
    title: 'Bot configuration updated',
    description: 'E-commerce bot settings modified',
    time: '6 hours ago',
    icon: 'mdi-cog',
    color: '#f59e0b'
  },
  {
    id: 4,
    type: 'alert',
    title: 'Performance alert',
    description: 'Support bot response time increased',
    time: '8 hours ago',
    icon: 'mdi-alert',
    color: '#ef4444'
  }
])

const quickActions = ref([
  {
    id: 1,
    title: 'Create Bot',
    description: 'Build a new chatbot',
    icon: 'mdi-robot-plus',
    color: '#6366f1',
    action: 'create-bot'
  },
  {
    id: 2,
    title: 'View Analytics',
    description: 'Check performance metrics',
    icon: 'mdi-chart-line',
    color: '#10b981',
    action: 'analytics'
  },
  {
    id: 3,
    title: 'Manage Users',
    description: 'User management',
    icon: 'mdi-account-group',
    color: '#8b5cf6',
    action: 'users'
  },
  {
    id: 4,
    title: 'Settings',
    description: 'System configuration',
    icon: 'mdi-cog',
    color: '#64748b',
    action: 'settings'
  }
])

const botStatusData = ref([
  {
    id: 1,
    name: 'Customer Support',
    status: 'active',
    conversations: 342,
    uptime: '99.9%'
  },
  {
    id: 2,
    name: 'Sales Assistant',
    status: 'active',
    conversations: 156,
    uptime: '98.5%'
  },
  {
    id: 3,
    name: 'E-commerce Helper',
    status: 'maintenance',
    conversations: 0,
    uptime: '0%'
  },
  {
    id: 4,
    name: 'HR Bot',
    status: 'active',
    conversations: 89,
    uptime: '99.2%'
  }
])

const performanceData = ref({
  labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
  datasets: [
    {
      label: 'Conversations',
      data: [1200, 1900, 1500, 2100, 2400, 1800, 2200],
      color: '#6366f1'
    },
    {
      label: 'Response Rate',
      data: [92, 94, 91, 95, 93, 96, 94],
      color: '#10b981'
    }
  ]
})
</script>

<style scoped>
.dashboard-container {
  padding: 32px;
  background: #fafbfc;
  min-height: 100vh;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 40px;
  gap: 32px;
}

.header-content {
  flex: 1;
}

.dashboard-title {
  font-size: 36px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 12px 0;
  line-height: 1.1;
  letter-spacing: -0.025em;
}

.dashboard-subtitle {
  font-size: 18px;
  color: #64748b;
  margin: 0;
  line-height: 1.5;
  font-weight: 400;
}

.header-actions {
  flex-shrink: 0;
}

.action-button {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 24px;
  font-weight: 600;
  font-size: 15px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.15);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 40px;
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 32px;
  grid-auto-rows: minmax(240px, auto);
}

.grid-item {
  background: white;
  border-radius: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05), 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.grid-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.grid-item-large {
  grid-column: span 2;
  grid-row: span 2;
}

.grid-item-wide {
  grid-column: span 2;
}

/* Responsive Design */
@media (max-width: 1200px) {
  .dashboard-container {
    padding: 24px;
  }
  
  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
    gap: 20px;
  }
  
  .content-grid {
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 24px;
  }
}

@media (max-width: 1024px) {
  .dashboard-header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .header-actions {
    align-self: flex-start;
  }
  
  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
    gap: 16px;
  }
  
  .content-grid {
    grid-template-columns: 1fr;
  }
  
  .grid-item-large,
  .grid-item-wide {
    grid-column: span 1;
  }
}

@media (max-width: 768px) {
  .dashboard-container {
    padding: 20px;
  }
  
  .dashboard-title {
    font-size: 28px;
  }
  
  .dashboard-subtitle {
    font-size: 16px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .content-grid {
    gap: 20px;
  }
  
  .action-button {
    padding: 14px 20px;
    font-size: 14px;
  }
}
</style>
