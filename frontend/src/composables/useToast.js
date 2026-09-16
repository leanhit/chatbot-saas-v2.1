import { useNotificationStore } from '@/stores/notification/notificationStore'
import { handleApiError } from '@/utils/errorHandler'

/**
 * Composable for triggering toast notifications linked to Pinia notificationStore
 * @returns {Object} Toast helper methods (success, error, warning, info, show)
 */
export function useToast() {
  const store = useNotificationStore()

  const show = (message, type = 'info', title = null, options = {}) => {
    const defaultTitles = {
      success: 'Success',
      error: 'Error',
      warning: 'Warning',
      info: 'Info'
    }

    store.addNotification({
      type,
      title: title || defaultTitles[type] || 'Notification',
      message: typeof message === 'string' ? message : String(message),
      priority: options.priority || (type === 'error' ? 'high' : 'medium'),
      actions: options.actions || []
    })
  }

  const success = (message, title = null, options = {}) => {
    show(message, 'success', title, options)
  }

  const error = (errOrMessage, fallback = null, title = null, options = {}) => {
    let message = ''
    if (typeof errOrMessage === 'string') {
      message = errOrMessage
    } else {
      message = handleApiError(errOrMessage, { fallback: fallback || 'An unexpected error occurred' })
    }
    show(message, 'error', title || 'Error', options)
  }

  const warning = (message, title = null, options = {}) => {
    show(message, 'warning', title, options)
  }

  const info = (message, title = null, options = {}) => {
    show(message, 'info', title, options)
  }

  return {
    show,
    success,
    error,
    warning,
    info
  }
}

export default useToast
