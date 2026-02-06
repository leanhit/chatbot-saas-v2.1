<template>
  <div 
    class="app-card"
    :class="cardClasses"
    @click="handleCardClick"
  >
    <!-- App Icon -->
    <div class="app-icon">
      <div class="icon-wrapper">
        {{ getAppIcon(app.appCode) }}
      </div>
    </div>

    <!-- App Info -->
    <div class="app-info">
      <h3 class="app-name">{{ app.appDisplayName || app.appCode }}</h3>
      <p class="app-description">{{ app.appDescription }}</p>
    </div>

    <!-- Status Badge -->
    <div class="app-status">
      <AppStatusBadge
        :status-type="app.statusType"
        :status-message="app.statusMessage"
        :enabled="app.enabled"
      />
    </div>

    <!-- Action Button -->
    <div class="app-action">
      <AppActionButton
        :app="app"
        @click="handleActionClick"
      />
    </div>

    <!-- Debug Info (development only) -->
    <div v-if="import.meta.env.DEV && app._debug" class="debug-info">
      <details>
        <summary>Debug</summary>
        <pre>{{ JSON.stringify(app._debug, null, 2) }}</pre>
      </details>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import AppStatusBadge from './AppStatusBadge.vue'
import AppActionButton from './AppActionButton.vue'

/**
 * App Card Component
 * Responsibility: Display individual app information and actions
 * Note: Pure presentation component, no business logic
 */
const props = defineProps({
  app: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['click'])

// Computed properties
const cardClasses = computed(() => {
  return {
    'app-card--usable': props.app.isUsable,
    'app-card--disabled': !props.app.isUsable,
    'app-card--clickable': props.app.isUsable
  }
})

// Methods
function getAppIcon(appCode) {
  // Simple icon mapping - can be extended
  const iconMap = {
    'CHATBOT': '🤖',
    'CRM': '👥',
    'ERP': '📊'
  }
  return iconMap[appCode] || '📱'
}

function handleCardClick() {
  if (props.app.isUsable) {
    emit('click', props.app.appCode)
  }
}

function handleActionClick(event) {
  // Stop propagation to avoid card click
  event.stopPropagation()
  emit('click', props.app.appCode)
}
</script>

<style scoped>
.app-card {
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 0.75rem;
  padding: 1.5rem;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  min-height: 200px;
  display: flex;
  flex-direction: column;
}

.app-card--clickable:hover {
  border-color: #3b82f6;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
  transform: translateY(-2px);
}

.app-card--disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.app-card--disabled:hover {
  transform: none;
  box-shadow: none;
  border-color: #e5e7eb;
}

.app-icon {
  margin-bottom: 1rem;
}

.icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 0.5rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
}

.app-info {
  flex: 1;
  margin-bottom: 1rem;
}

.app-name {
  font-size: 1.125rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 0.5rem;
  line-height: 1.4;
}

.app-description {
  font-size: 0.875rem;
  color: #6b7280;
  line-height: 1.5;
  margin: 0;
}

.app-status {
  margin-bottom: 1rem;
}

.app-action {
  margin-top: auto;
}

.debug-info {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 0.25rem;
  border-radius: 0.25rem;
  font-size: 0.75rem;
  z-index: 10;
}

.debug-info summary {
  cursor: pointer;
  font-weight: 500;
}

.debug-info pre {
  margin: 0.5rem 0 0 0;
  font-size: 0.7rem;
  line-height: 1.3;
  white-space: pre-wrap;
  max-width: 200px;
  overflow-x: auto;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .app-card {
    padding: 1rem;
    min-height: 180px;
  }
  
  .icon-wrapper {
    width: 40px;
    height: 40px;
    font-size: 1.25rem;
  }
  
  .app-name {
    font-size: 1rem;
  }
  
  .app-description {
    font-size: 0.8rem;
  }
}
</style>
