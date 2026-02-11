import { createApp } from 'vue'
import Toast from '@/components/Toast.vue'
// Toast plugin for global toast notifications
const ToastPlugin = {
  install(app) {
    const toastContainer = document.createElement('div')
    toastContainer.id = 'toast-container'
    document.body.appendChild(toastContainer)
    const toastApp = createApp(Toast)
    const toastInstance = toastApp.mount(toastContainer)
    app.config.globalProperties.$toast = {
      show(message, type = 'info', duration = 3000) {
        // Create a new toast instance for each message
        const newToastContainer = document.createElement('div')
        newToastContainer.id = 'toast-container-' + Date.now()
        document.body.appendChild(newToastContainer)
        const newToastApp = createApp(Toast, {
          message,
          type,
          duration
        })
        const newToastInstance = newToastApp.mount(newToastContainer)
        
        newToastInstance.showToast()
        
        // Clean up after duration
        if (duration > 0) {
          setTimeout(() => {
            newToastApp.unmount()
            document.body.removeChild(newToastContainer)
          }, duration + 500) // Add buffer time for animation
        }
      },
      success(message, duration = 3000) {
        this.show(message, 'success', duration)
      },
      error(message, duration = 3000) {
        this.show(message, 'error', duration)
      },
      warning(message, duration = 3000) {
        this.show(message, 'warning', duration)
      },
      info(message, duration = 3000) {
        this.show(message, 'info', duration)
      }
    }
  }
}
export default ToastPlugin
