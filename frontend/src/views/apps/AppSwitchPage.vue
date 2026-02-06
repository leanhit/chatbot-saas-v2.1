<template>
  <div class="app-switch-page">
    <!-- Header -->
    <div class="page-header">
      <h1>App Switch</h1>
      <p class="page-description">
        Choose an application to work with
      </p>
    </div>

    <!-- Loading State -->
    <div v-if="isLoading" class="loading-state">
      <div class="loading-spinner">Loading apps...</div>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-state">
      <div class="error-message">
        Failed to load apps: {{ error }}
      </div>
      <button @click="loadAppSwitchData" class="retry-button">
        Retry
      </button>
    </div>

    <!-- App Grid -->
    <div v-else class="app-grid-container">
      <AppGrid 
        :apps="appsWithStatus"
        :loading="isLoading"
        @app-click="handleAppClick"
      />
    </div>

    <!-- Debug Info (development only) -->
    <div v-if="import.meta.env.DEV" class="debug-info">
      <h3>Debug Information</h3>
      <details>
        <summary>Raw App Data</summary>
        <pre>{{ JSON.stringify(appsWithStatus, null, 2) }}</pre>
      </details>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAppSwitchStore } from '@/stores/appSwitch.store'
import AppGrid from '@/components/app/AppGrid.vue'

/**
 * App Switch Page
 * Responsibility: Display available apps and handle navigation
 * Note: All validation is done by backend, frontend only shows UI state
 */
const router = useRouter()
const appSwitchStore = useAppSwitchStore()

// Destructure store properties
const { 
  appsWithStatus, 
  isLoading, 
  error, 
  loadAppSwitchData,
  navigateToApp 
} = appSwitchStore

// Load data on component mount
onMounted(() => {
  loadAppSwitchData()
})

// Handle app click - delegate to store for consistency
function handleAppClick(appCode) {
  navigateToApp(appCode, router)
}
</script>

<style scoped>
.app-switch-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.page-header {
  text-align: center;
  margin-bottom: 2rem;
}

.page-header h1 {
  font-size: 2rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: #1f2937;
}

.page-description {
  color: #6b7280;
  font-size: 1rem;
}

.loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

.loading-spinner {
  color: #6b7280;
  font-size: 1.1rem;
}

.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  min-height: 200px;
  justify-content: center;
}

.error-message {
  color: #dc2626;
  background-color: #fee2e2;
  padding: 1rem;
  border-radius: 0.5rem;
  border: 1px solid #fecaca;
}

.retry-button {
  background-color: #3b82f6;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 0.375rem;
  cursor: pointer;
  font-size: 0.9rem;
}

.retry-button:hover {
  background-color: #2563eb;
}

.app-grid-container {
  margin-top: 2rem;
}

.debug-info {
  margin-top: 3rem;
  padding: 1rem;
  background-color: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
}

.debug-info h3 {
  margin-bottom: 1rem;
  color: #374151;
}

.debug-info details {
  margin-top: 0.5rem;
}

.debug-info summary {
  cursor: pointer;
  color: #6b7280;
  font-weight: 500;
}

.debug-info pre {
  background-color: #1f2937;
  color: #f9fafb;
  padding: 1rem;
  border-radius: 0.375rem;
  overflow-x: auto;
  font-size: 0.8rem;
  line-height: 1.4;
}
</style>
