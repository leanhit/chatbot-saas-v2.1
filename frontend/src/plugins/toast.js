import { useToast } from '@/composables/useToast'

/**
 * Toast plugin bridging global $toast property to useToast and Pinia notificationStore
 */
const ToastPlugin = {
  install(app) {
    app.config.globalProperties.$toast = {
      show(message, type = 'info', title = null, options = {}) {
        useToast().show(message, type, title, options)
      },
      success(message, title = null, options = {}) {
        useToast().success(message, title, options)
      },
      error(errOrMessage, fallback = null, title = null, options = {}) {
        useToast().error(errOrMessage, fallback, title, options)
      },
      warning(message, title = null, options = {}) {
        useToast().warning(message, title, options)
      },
      info(message, title = null, options = {}) {
        useToast().info(message, title, options)
      }
    }
  }
}

export default ToastPlugin
