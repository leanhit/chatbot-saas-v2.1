import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref([])
  const unreadCount = ref(0)
  const isConnected = ref(false)

  // Add a new notification
  const addNotification = (notification) => {
    const newNotification = {
      id: Date.now() + Math.random(),
      ...notification,
      timestamp: new Date(),
      read: false
    }
    
    notifications.value.unshift(newNotification)
    unreadCount.value++
    
    // Auto-remove after 10 seconds for toast notifications
    if (notification.type === 'toast') {
      setTimeout(() => {
        removeNotification(newNotification.id)
      }, 10000)
    }
  }

  // Remove notification
  const removeNotification = (id) => {
    const index = notifications.value.findIndex(n => n.id === id)
    if (index > -1) {
      const notification = notifications.value[index]
      if (!notification.read) {
        unreadCount.value--
      }
      notifications.value.splice(index, 1)
    }
  }

  // Mark as read
  const markAsRead = (id) => {
    const notification = notifications.value.find(n => n.id === id)
    if (notification && !notification.read) {
      notification.read = true
      unreadCount.value--
    }
  }

  // Mark all as read
  const markAllAsRead = () => {
    notifications.value.forEach(notification => {
      notification.read = true
    })
    unreadCount.value = 0
  }

  // Handle tenant invitation notification
  const handleTenantInvitation = (data) => {
    addNotification({
      type: 'tenant_invitation',
      title: 'Tenant Invitation',
      message: `You've been invited to join "${data.tenantName}" as ${data.role}`,
      data: data,
      actions: [
        { label: 'View', action: 'view_invitation', data: data }
      ]
    })
  }

  // Handle join request notification
  const handleJoinRequest = (data) => {
    addNotification({
      type: 'join_request',
      title: 'Join Request',
      message: `${data.requesterName} wants to join "${data.tenantName}"`,
      data: data,
      actions: [
        { label: 'Review', action: 'review_request', data: data }
      ]
    })
  }

  // Handle invitation accepted
  const handleInvitationAccepted = (data) => {
    addNotification({
      type: 'success',
      title: 'Invitation Accepted',
      message: `${data.memberEmail} has accepted your invitation to "${data.tenantName}"`,
      data: data
    })
  }

  // Handle join request approved
  const handleJoinRequestApproved = (data) => {
    addNotification({
      type: 'success',
      title: 'Request Approved',
      message: `Your request to join "${data.tenantName}" has been approved!`,
      data: data,
      actions: [
        { label: 'Enter Workspace', action: 'enter_workspace', data: data }
      ]
    })
  }

  return {
    notifications,
    unreadCount,
    isConnected,
    addNotification,
    removeNotification,
    markAsRead,
    markAllAsRead,
    handleTenantInvitation,
    handleJoinRequest,
    handleInvitationAccepted,
    handleJoinRequestApproved
  }
})
