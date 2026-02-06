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
        toastInstance.message = message
        toastInstance.type = type
        toastInstance.duration = duration
        toastInstance.showToast()
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
