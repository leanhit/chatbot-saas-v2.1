<template>
  <div class="action-button">
    <!-- Primary Action: Open App -->
    <button
      v-if="app.isUsable"
      @click="handleOpenApp"
      class="btn btn--primary"
    >
      Open App
    </button>

    <!-- Secondary Actions: Enable/Disable (basic role check) -->
    <div v-else class="secondary-actions">
      <button
        v-if="!app.enabled && canManageApps"
        @click="handleEnableApp"
        class="btn btn--secondary"
        :disabled="isActionLoading"
      >
        {{ isActionLoading ? 'Enabling...' : 'Enable' }}
      </button>
      
      <button
        v-if="app.enabled && canManageApps"
        @click="handleDisableApp"
        class="btn btn--secondary btn--danger"
        :disabled="isActionLoading"
      >
        {{ isActionLoading ? 'Disabling...' : 'Disable' }}
      </button>

      <!-- Info button for non-manageable users -->
      <button
        v-if="!canManageApps"
        @click="handleShowInfo"
        class="btn btn--info"
      >
        Info
      </button>
    </div>

    <!-- Status Reason (shown when app is not usable) -->
    <div v-if="!app.isUsable && showStatusReason" class="status-reason">
      {{ app.statusMessage }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useAppStore } from '@/stores/app.store'
import { useUserStore } from '@/stores/user.store' // Assuming user store exists

/**
 * App Action Button Component
 * Responsibility: Handle app actions (open, enable, disable)
 * Note: Basic role check only - backend will validate all actions
 */
const props = defineProps({
  app: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['click'])

// Stores
const appStore = useAppStore()
const userStore = useUserStore() // For basic role checking

// State
const isActionLoading = ref(false)
const showStatusReason = ref(false)

// Computed properties
const canManageApps = computed(() => {
  // Basic role check - backend will validate properly
  // v0.1: Simple check, can be extended later
  return userStore.hasRole?.('ADMIN') || userStore.hasRole?.('OWNER') || false
})

// Methods
async function handleOpenApp() {
  emit('click', props.app.appCode)
}

async function handleEnableApp() {
  if (!canManageApps.value) return
  
  isActionLoading.value = true
  try {
    // Backend will validate permissions and tenant context
    await appStore.enableApp(props.app.appCode)
    emit('click', props.app.appCode) // Refresh UI
  } catch (error) {
    console.error('Failed to enable app:', error)
    // Error handling could be enhanced with toast notifications
  } finally {
    isActionLoading.value = false
  }
}

async function handleDisableApp() {
  if (!canManageApps.value) return
  
  isActionLoading.value = true
  try {
    // Backend will validate permissions and tenant context
    await appStore.disableApp(props.app.appCode)
    emit('click', props.app.appCode) // Refresh UI
  } catch (error) {
    console.error('Failed to disable app:', error)
    // Error handling could be enhanced with toast notifications
  } finally {
    isActionLoading.value = false
  }
}

function handleShowInfo() {
  showStatusReason.value = !showStatusReason.value
}
</script>

<style scoped>
.action-button {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 0.375rem;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: center;
  min-width: 80px;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn--primary {
  background-color: #3b82f6;
  color: white;
}

.btn--primary:hover:not(:disabled) {
  background-color: #2563eb;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(59, 130, 246, 0.3);
}

.btn--secondary {
  background-color: #6b7280;
  color: white;
}

.btn--secondary:hover:not(:disabled) {
  background-color: #4b5563;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(107, 114, 128, 0.3);
}

.btn--danger {
  background-color: #dc2626;
  color: white;
}

.btn--danger:hover:not(:disabled) {
  background-color: #b91c1c;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(220, 38, 38, 0.3);
}

.btn--info {
  background-color: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
}

.btn--info:hover:not(:disabled) {
  background-color: #e5e7eb;
  transform: translateY(-1px);
}

.secondary-actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.status-reason {
  font-size: 0.75rem;
  color: #6b7280;
  text-align: center;
  padding: 0.25rem;
  background-color: #f9fafb;
  border-radius: 0.25rem;
  border: 1px solid #e5e7eb;
  line-height: 1.3;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .btn {
    padding: 0.375rem 0.75rem;
    font-size: 0.8rem;
    min-width: 70px;
  }
  
  .secondary-actions {
    flex-direction: column;
  }
  
  .status-reason {
    font-size: 0.7rem;
  }
}
</style>
