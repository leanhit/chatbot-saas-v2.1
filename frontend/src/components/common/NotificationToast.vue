<template>
  <div class="notification-container">
    <transition-group name="notification" tag="div">
      <div
        v-for="notification in notifications"
        :key="notification.id"
        :class="[
          'notification-toast',
          `notification-${notification.type}`
        ]"
        @click="markAsRead(notification.id)"
      >
        <div class="notification-icon">
          <Icon v-if="getIcon(notification.type)" :icon="getIcon(notification.type)" />
        </div>
        <div class="notification-content">
          <div class="notification-title">{{ notification.title }}</div>
          <div class="notification-message">{{ notification.message }}</div>
          <div v-if="notification.actions && notification.actions.length" class="notification-actions">
            <button
              v-for="action in notification.actions"
              :key="action.label"
              @click.stop="handleAction(action, notification)"
              class="notification-action-btn"
            >
              {{ action.label }}
            </button>
          </div>
        </div>
        <button
          @click.stop="removeNotification(notification.id)"
          class="notification-close"
        >
          <Icon icon="mdi:close" />
        </button>
      </div>
    </transition-group>
  </div>
</template>

<script>
import { computed } from 'vue'
import { Icon } from '@iconify/vue'
import { useNotificationStore } from '@/stores/notification/notificationStore'

export default {
  name: 'NotificationToast',
  components: {
    Icon
  },
  setup() {
    const notificationStore = useNotificationStore()

    const notifications = computed(() => 
      notificationStore.notifications.slice(0, 5) // Show max 5 notifications
    )

    const removeNotification = (id) => {
      notificationStore.removeNotification(id)
    }

    const markAsRead = (id) => {
      notificationStore.markAsRead(id)
    }

    const handleAction = (action, notification) => {
      // Emit action to parent or handle globally
      console.log('Action clicked:', action, notification)
      
      // Handle specific actions
      if (action.action === 'enter_workspace') {
        // Navigate to workspace
        console.log('Entering workspace:', action.data.tenantName)
      } else if (action.action === 'view_invitation') {
        // Navigate to invitations tab
        console.log('Viewing invitation:', action.data)
      }
      
      markAsRead(notification.id)
    }

    const getIcon = (type) => {
      const icons = {
        tenant_invitation: 'mdi:account-plus',
        join_request: 'mdi:account-question',
        success: 'mdi:check-circle',
        error: 'mdi:alert-circle',
        warning: 'mdi:alert',
        info: 'mdi:information'
      }
      return icons[type] || icons.info
    }

    return {
      notifications,
      removeNotification,
      markAsRead,
      handleAction,
      getIcon
    }
  }
}
</script>

<style scoped>
.notification-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  max-width: 400px;
}

.notification-toast {
  display: flex;
  align-items: flex-start;
  padding: 16px;
  margin-bottom: 12px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border-left: 4px solid #3b82f6;
  cursor: pointer;
  transition: all 0.3s ease;
}

.notification-toast:hover {
  transform: translateX(-4px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

.notification-success {
  border-left-color: #10b981;
}

.notification-error {
  border-left-color: #ef4444;
}

.notification-warning {
  border-left-color: #f59e0b;
}

.notification-icon {
  margin-right: 12px;
  color: #3b82f6;
  font-size: 20px;
  flex-shrink: 0;
}

.notification-success .notification-icon {
  color: #10b981;
}

.notification-error .notification-icon {
  color: #ef4444;
}

.notification-warning .notification-icon {
  color: #f59e0b;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
  font-size: 14px;
}

.notification-message {
  color: #6b7280;
  font-size: 13px;
  line-height: 1.4;
  margin-bottom: 8px;
}

.notification-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.notification-action-btn {
  padding: 4px 12px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.notification-action-btn:hover {
  background: #2563eb;
}

.notification-close {
  margin-left: 12px;
  background: none;
  border: none;
  color: #9ca3af;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s;
}

.notification-close:hover {
  background: #f3f4f6;
  color: #6b7280;
}

/* Transitions */
.notification-enter-active {
  transition: all 0.3s ease;
}

.notification-leave-active {
  transition: all 0.3s ease;
}

.notification-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.notification-leave-to {
  opacity: 0;
  transform: translateX(100%);
}

.notification-move {
  transition: transform 0.3s ease;
}

/* Dark mode */
@media (prefers-color-scheme: dark) {
  .notification-toast {
    background: #1f2937;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  }
  
  .notification-title {
    color: #f9fafb;
  }
  
  .notification-message {
    color: #d1d5db;
  }
  
  .notification-close:hover {
    background: #374151;
    color: #9ca3af;
  }
}
</style>
