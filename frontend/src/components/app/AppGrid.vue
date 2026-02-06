<template>
  <div class="app-grid">
    <div class="grid-container">
      <AppCard
        v-for="app in apps"
        :key="app.appCode"
        :app="app"
        @click="handleAppClick"
      />
    </div>

    <!-- Empty State -->
    <div v-if="apps.length === 0 && !loading" class="empty-state">
      <div class="empty-icon">📱</div>
      <h3>No apps available</h3>
      <p>No applications are configured for your workspace.</p>
    </div>
  </div>
</template>

<script setup>
import AppCard from './AppCard.vue'

/**
 * App Grid Component
 * Responsibility: Display apps in a responsive grid layout
 * Note: Pure presentation component, no business logic
 */
defineProps({
  apps: {
    type: Array,
    required: true,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['app-click'])

function handleAppClick(appCode) {
  emit('app-click', appCode)
}
</script>

<style scoped>
.app-grid {
  width: 100%;
}

.grid-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  color: #6b7280;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.empty-state h3 {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: #374151;
}

.empty-state p {
  font-size: 1rem;
  max-width: 400px;
  margin: 0 auto;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .grid-container {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 1rem;
  }
}

@media (max-width: 480px) {
  .grid-container {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
}
</style>
