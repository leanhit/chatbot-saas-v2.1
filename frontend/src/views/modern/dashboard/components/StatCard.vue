<template>
  <div class="stat-card" :class="`stat-card-${colorVariant}`">
    <div class="stat-icon">
      <i :class="icon" :style="{ color }"></i>
    </div>
    <div class="stat-content">
      <h3 class="stat-title">{{ title }}</h3>
      <div class="stat-value">{{ value }}</div>
      <div class="stat-change" :class="changeClass">
        <i :class="changeIcon"></i>
        {{ change }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  title: string
  value: string
  change: string
  icon: string
  color: string
}

const props = defineProps<Props>()

const changeClass = computed(() => {
  return props.change.startsWith('+') ? 'positive' : 'negative'
})

const changeIcon = computed(() => {
  return props.change.startsWith('+') ? 'mdi-trending-up' : 'mdi-trending-down'
})

const colorVariant = computed(() => {
  const colorMap: Record<string, string> = {
    '#667eea': 'primary',
    '#48bb78': 'success',
    '#ed8936': 'warning',
    '#9f7aea': 'purple',
    '#f56565': 'danger'
  }
  return colorMap[props.color] || 'default'
})
</script>

<style scoped>
.stat-card {
  background: white;
  border-radius: 16px;
  padding: 28px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05), 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: v-bind(color);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  background: v-bind(color + '15');
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.stat-icon i {
  font-size: 28px;
}

.stat-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-title {
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
  margin: 0;
  text-transform: uppercase;
  letter-spacing: 0.75px;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #111827;
  margin: 0;
  line-height: 1;
  letter-spacing: -0.025em;
}

.stat-change {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  margin: 0;
}

.stat-change.positive {
  color: #059669;
}

.stat-change.negative {
  color: #dc2626;
}

.stat-change i {
  font-size: 16px;
}

/* Color variants */
.stat-card-primary::before {
  background: linear-gradient(90deg, #6366f1, #4f46e5);
}

.stat-card-success::before {
  background: linear-gradient(90deg, #10b981, #059669);
}

.stat-card-warning::before {
  background: linear-gradient(90deg, #f59e0b, #d97706);
}

.stat-card-purple::before {
  background: linear-gradient(90deg, #8b5cf6, #7c3aed);
}

.stat-card-danger::before {
  background: linear-gradient(90deg, #ef4444, #dc2626);
}

/* Responsive */
@media (max-width: 768px) {
  .stat-card {
    padding: 24px;
  }
  
  .stat-icon {
    width: 48px;
    height: 48px;
  }
  
  .stat-icon i {
    font-size: 24px;
  }
  
  .stat-value {
    font-size: 28px;
  }
}
</style>
