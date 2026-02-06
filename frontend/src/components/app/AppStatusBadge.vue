<template>
  <div class="status-badge" :class="badgeClasses">
    <div class="status-indicator"></div>
    <span class="status-text">{{ statusMessage }}</span>
  </div>
</template>

<script setup>
import { computed } from 'vue'

/**
 * App Status Badge Component
 * Responsibility: Display app status with appropriate styling
 * Note: Pure presentation component, no business logic
 */
const props = defineProps({
  statusType: {
    type: String,
    default: 'success', // success, warning, error
    validator: (value) => ['success', 'warning', 'error'].includes(value)
  },
  statusMessage: {
    type: String,
    required: true
  },
  enabled: {
    type: Boolean,
    default: false
  }
})

// Computed properties
const badgeClasses = computed(() => {
  return {
    'status-badge--success': props.statusType === 'success',
    'status-badge--warning': props.statusType === 'warning',
    'status-badge--error': props.statusType === 'error',
    'status-badge--enabled': props.enabled,
    'status-badge--disabled': !props.enabled
  }
})
</script>

<style scoped>
.status-badge {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.375rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 500;
  line-height: 1;
  transition: all 0.2s ease;
}

.status-indicator {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

.status-text {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 150px;
}

/* Success state (enabled and usable) */
.status-badge--success {
  background-color: #dcfce7;
  color: #166534;
  border: 1px solid #bbf7d0;
}

.status-badge--success .status-indicator {
  background-color: #22c55e;
}

/* Warning state (disabled but potentially fixable) */
.status-badge--warning {
  background-color: #fef3c7;
  color: #92400e;
  border: 1px solid #fde68a;
}

.status-badge--warning .status-indicator {
  background-color: #f59e0b;
}

/* Error state (blocked by plan or other issues) */
.status-badge--error {
  background-color: #fee2e2;
  color: #991b1b;
  border: 1px solid #fecaca;
}

.status-badge--error .status-indicator {
  background-color: #ef4444;
}

/* Enabled/Disabled specific styling */
.status-badge--enabled {
  border-left: 3px solid #22c55e;
}

.status-badge--disabled {
  border-left: 3px solid #6b7280;
}

/* Hover effects */
.status-badge:hover {
  transform: scale(1.02);
}

.status-badge--success:hover {
  box-shadow: 0 2px 4px rgba(34, 197, 94, 0.2);
}

.status-badge--warning:hover {
  box-shadow: 0 2px 4px rgba(245, 158, 11, 0.2);
}

.status-badge--error:hover {
  box-shadow: 0 2px 4px rgba(239, 68, 68, 0.2);
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .status-badge {
    padding: 0.25rem 0.5rem;
    font-size: 0.7rem;
  }
  
  .status-text {
    max-width: 120px;
  }
}
</style>
