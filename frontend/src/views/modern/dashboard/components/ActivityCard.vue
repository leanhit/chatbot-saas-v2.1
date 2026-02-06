<template>
  <div class="activity-card">
    <div class="card-header">
      <h3 class="card-title">{{ title }}</h3>
      <el-button type="text" class="view-all-btn">
        View All
        <i class="mdi mdi-arrow-right"></i>
      </el-button>
    </div>
    
    <div class="activity-list">
      <div 
        v-for="activity in activities" 
        :key="activity.id"
        class="activity-item"
      >
        <div class="activity-icon" :style="{ background: activity.color + '15' }">
          <i :class="activity.icon" :style="{ color: activity.color }"></i>
        </div>
        
        <div class="activity-content">
          <h4 class="activity-title">{{ activity.title }}</h4>
          <p class="activity-description">{{ activity.description }}</p>
        </div>
        
        <div class="activity-time">
          <span>{{ activity.time }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Activity {
  id: number
  type: string
  title: string
  description: string
  time: string
  icon: string
  color: string
}

interface Props {
  title: string
  activities: Activity[]
}

defineProps<Props>()
</script>

<style scoped>
.activity-card {
  background: white;
  border-radius: 16px;
  padding: 28px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05), 0 4px 12px rgba(0, 0, 0, 0.08);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f1f5f9;
}

.card-title {
  font-size: 20px;
  font-weight: 700;
  color: #111827;
  margin: 0;
  letter-spacing: -0.025em;
}

.view-all-btn {
  color: #6366f1;
  font-weight: 600;
  padding: 10px 16px;
  display: flex;
  align-items: center;
  gap: 6px;
  border-radius: 8px;
  font-size: 14px;
}

.view-all-btn:hover {
  background: #f0f4ff;
}

.activity-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  padding: 20px;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.activity-item:hover {
  background: #fafbfc;
  transform: translateX(4px);
}

.activity-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.activity-icon i {
  font-size: 24px;
}

.activity-content {
  flex: 1;
  min-width: 0;
}

.activity-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 6px 0;
  line-height: 1.4;
  letter-spacing: -0.025em;
}

.activity-description {
  font-size: 14px;
  color: #64748b;
  margin: 0;
  line-height: 1.5;
  font-weight: 400;
}

.activity-time {
  font-size: 13px;
  color: #94a3b8;
  white-space: nowrap;
  flex-shrink: 0;
  font-weight: 500;
}

/* Responsive */
@media (max-width: 768px) {
  .activity-card {
    padding: 24px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .activity-item {
    gap: 16px;
    padding: 16px;
  }
  
  .activity-icon {
    width: 40px;
    height: 40px;
  }
  
  .activity-icon i {
    font-size: 20px;
  }
}
</style>
