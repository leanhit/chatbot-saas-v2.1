import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from '@/plugins/axios'

const NOTIFICATION_STORAGE_KEY = 'notification_history'
const MAX_STORED_NOTIFICATIONS = 50

// Sound notification
let audioContext = null
const playNotificationSound = () => {
  try {
    if (!audioContext) {
      audioContext = new (window.AudioContext || window.webkitAudioContext)()
    }
    
    // Try to play notification sound from public folder
    const audio = new Audio('/sounds/notification.mp3')
    audio.volume = 0.5
    audio.play().catch(err => {
      console.log('Could not play notification sound:', err)
    })
  } catch (error) {
    console.log('Audio not supported:', error)
  }
}

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref([])
  const unreadCount = ref(0)
  const isConnected = ref(false)
  const soundEnabled = ref(true)

  // Load notifications from localStorage on initialization
  const loadFromStorage = () => {
    try {
      const stored = localStorage.getItem(NOTIFICATION_STORAGE_KEY)
      if (stored) {
        const parsed = JSON.parse(stored)
        notifications.value = parsed.map(n => ({
          ...n,
          timestamp: new Date(n.timestamp)
        }))
        unreadCount.value = notifications.value.filter(n => !n.read).length
      }
    } catch (error) {
      console.error('Failed to load notifications from storage:', error)
    }
  }

  // Save notifications to localStorage
  const saveToStorage = () => {
    try {
      const toStore = notifications.value.slice(0, MAX_STORED_NOTIFICATIONS)
      localStorage.setItem(NOTIFICATION_STORAGE_KEY, JSON.stringify(toStore))
    } catch (error) {
      console.error('Failed to save notifications to storage:', error)
    }
  }

  // Initialize from storage
  loadFromStorage()

  // Add a new notification
  const addNotification = (notification) => {
    const newNotification = {
      id: Date.now() + Math.random(),
      ...notification,
      timestamp: new Date(),
      read: false,
      priority: notification.priority || 'medium' // Default priority: medium
    }

    notifications.value.unshift(newNotification)
    unreadCount.value++

    // Save to storage
    saveToStorage()

    // Play sound if enabled and not a toast notification
    // Play different sound/volume based on priority
    if (soundEnabled.value && notification.type !== 'toast') {
      if (newNotification.priority === 'urgent' || newNotification.priority === 'high') {
        // Play sound at higher volume for urgent/high priority
        playNotificationSoundWithVolume(0.8)
      } else {
        playNotificationSound()
      }
    }

    // Auto-remove after 10 seconds for toast notifications
    if (notification.type === 'toast') {
      setTimeout(() => {
        removeNotification(newNotification.id)
      }, 10000)
    }
  }

  // Play notification sound with custom volume
  const playNotificationSoundWithVolume = (volume = 0.5) => {
    try {
      if (!audioContext) {
        audioContext = new (window.AudioContext || window.webkitAudioContext)()
      }

      const audio = new Audio('/sounds/notification.mp3')
      audio.volume = volume
      audio.play().catch(err => {
        console.log('Could not play notification sound:', err)
      })
    } catch (error) {
      console.log('Audio not supported:', error)
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
      saveToStorage()
    }
  }

  // Mark as read
  const markAsRead = (id) => {
    const notification = notifications.value.find(n => n.id === id)
    if (notification && !notification.read) {
      notification.read = true
      unreadCount.value--
      saveToStorage()
    }
  }

  // Mark all as read
  const markAllAsRead = () => {
    notifications.value.forEach(notification => {
      notification.read = true
    })
    unreadCount.value = 0
    saveToStorage()
  }

  // Acknowledge a notification on the backend (Redis-backed, 24h TTL)
  const acknowledgeNotification = async (notificationId) => {
    try {
      await axios.post(`/api/notifications/${notificationId}/ack`)
      markAsRead(notificationId)
    } catch (error) {
      console.error('Failed to acknowledge notification:', error)
      // Still mark as read locally even if backend fails
      markAsRead(notificationId)
    }
  }

  // Acknowledge all notifications on the backend
  const acknowledgeAll = async () => {
    try {
      const ids = notifications.value
        .filter(n => !n.read)
        .map(n => String(n.id))
      if (ids.length > 0) {
        await axios.post('/api/notifications/ack-all', { notificationIds: ids })
      }
      markAllAsRead()
    } catch (error) {
      console.error('Failed to acknowledge all notifications:', error)
      // Still mark all as read locally
      markAllAsRead()
    }
  }

  // Clear all notifications
  const clearAll = () => {
    notifications.value = []
    unreadCount.value = 0
    saveToStorage()
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

  // Handle conversation message (real-time chat messages)
  const handleConversationMessage = (data) => {
    // This will be handled by chat component that listens for conversation updates
    // We just emit a custom event that chat components can listen to
    const event = new CustomEvent('conversationMessage', {
      detail: {
        conversationId: data.conversationId,
        sender: data.sender,
        message: data.message,
        timestamp: data.timestamp
      }
    })
    window.dispatchEvent(event)
    
    }

  return {
    notifications,
    unreadCount,
    isConnected,
    soundEnabled,
    addNotification,
    removeNotification,
    markAsRead,
    markAllAsRead,
    acknowledgeNotification,
    acknowledgeAll,
    clearAll,
    handleTenantInvitation,
    handleJoinRequest,
    handleInvitationAccepted,
    handleJoinRequestApproved,
    handleConversationMessage
  }
})
