<template>
  <div class="bot-status-card">
    <div class="card-header">
      <h3 class="card-title">{{ title }}</h3>
      <el-button type="text" class="view-all-btn">
        Manage
        <i class="mdi mdi-arrow-right"></i>
      </el-button>
    </div>
    
    <div class="bot-list">
      <div 
        v-for="bot in bots" 
        :key="bot.id"
        class="bot-item"
      >
        <div class="bot-info">
          <div class="bot-name">{{ bot.name }}</div>
          <div class="bot-metrics">
            <span class="metric">
              <i class="mdi mdi-message-text"></i>
              {{ bot.conversations }}
            </span>
            <span class="metric">
              <i class="mdi mdi-pulse"></i>
              {{ bot.uptime }}
            </span>
          </div>
        </div>
        
        <div class="bot-status">
          <div 
            class="status-indicator" 
            :class="`status-${bot.status}`"
          >
            <div class="status-dot"></div>
            <span class="status-text">{{ getStatusText(bot.status) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Bot {
  id: number
  name: string
  status: 'active' | 'inactive' | 'maintenance'
  conversations: number
  uptime: string
}

interface Props {
  title: string
  bots: Bot[]
}

defineProps<Props>()

const getStatusText = (status: string): string => {
  const statusMap: Record<string, string> = {
    active: 'Active',
    inactive: 'Inactive',
    maintenance: 'Maintenance'
  }
  return statusMap[status] || status
}
</script>

<style scoped>
.bot-status-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f1f5f9;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.view-all-btn {
  color: #667eea;
  font-weight: 500;
  padding: 8px 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.view-all-btn:hover {
  background: #f0f4ff;
}

.bot-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow-y: auto;
}

.bot-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #f1f5f9;
  transition: background-color 0.2s ease;
}

.bot-item:hover {
  background: #f8fafc;
}

.bot-info {
  flex: 1;
  min-width: 0;
}

.bot-name {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 8px;
}

.bot-metrics {
  display: flex;
  gap: 16px;
}

.metric {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #64748b;
}

.metric i {
  font-size: 14px;
}

.bot-status {
  flex-shrink: 0;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-active {
  background: #dcfce7;
  color: #166534;
}

.status-inactive {
  background: #f1f5f9;
  color: #475569;
}

.status-maintenance {
  background: #fef3c7;
  color: #92400e;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.status-text {
  white-space: nowrap;
}

/* Responsive */
@media (max-width: 768px) {
  .bot-status-card {
    padding: 20px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .bot-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .bot-metrics {
    gap: 12px;
  }
}
</style>
