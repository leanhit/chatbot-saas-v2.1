<template>
  <div class="quick-actions-card">
    <div class="card-header">
      <h3 class="card-title">{{ title }}</h3>
    </div>
    
    <div class="actions-grid">
      <div 
        v-for="action in actions" 
        :key="action.id"
        class="action-item"
        @click="handleActionClick(action)"
      >
        <div class="action-icon" :style="{ background: action.color + '15' }">
          <i :class="action.icon" :style="{ color: action.color }"></i>
        </div>
        
        <div class="action-content">
          <h4 class="action-title">{{ action.title }}</h4>
          <p class="action-description">{{ action.description }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Action {
  id: number
  title: string
  description: string
  icon: string
  color: string
  action: string
}

interface Props {
  title: string
  actions: Action[]
}

const props = defineProps<Props>()

const handleActionClick = (action: Action) => {
  console.log(`Action clicked: ${action.action}`)
  // Read-only - no actual navigation or API calls
}
</script>

<style scoped>
.quick-actions-card {
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

.actions-grid {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #f1f5f9;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-item:hover {
  background: #f8fafc;
  border-color: #e2e8f0;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.action-icon i {
  font-size: 24px;
}

.action-content {
  flex: 1;
  min-width: 0;
}

.action-title {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 4px 0;
  line-height: 1.4;
}

.action-description {
  font-size: 13px;
  color: #64748b;
  margin: 0;
  line-height: 1.4;
}

/* Responsive */
@media (max-width: 768px) {
  .quick-actions-card {
    padding: 20px;
  }
  
  .action-item {
    gap: 12px;
    padding: 12px;
  }
  
  .action-icon {
    width: 40px;
    height: 40px;
  }
  
  .action-icon i {
    font-size: 20px;
  }
}
</style>
