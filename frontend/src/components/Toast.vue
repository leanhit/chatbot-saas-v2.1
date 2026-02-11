<template>
  <transition name="toast">
    <div
      v-if="show"
      :class="[
        'fixed top-4 right-4 z-50 p-4 rounded-lg shadow-lg max-w-sm',
        toastClasses
      ]"
    >
      <div class="flex items-center">
        <div class="flex-shrink-0">
          <Icon 
            :icon="icon" 
            :class="iconClasses"
            class="h-5 w-5"
          />
        </div>
        <div class="ml-3">
          <p class="text-sm font-medium">{{ message }}</p>
        </div>
        <div class="ml-auto pl-3">
          <button
            @click="close"
            class="inline-flex text-gray-400 hover:text-gray-600 focus:outline-none"
          >
            <Icon icon="mdi:close" class="h-4 w-4" />
          </button>
        </div>
      </div>
    </div>
  </transition>
</template>
<script>
import { Icon } from '@iconify/vue'
export default {
  name: 'Toast',
  components: {
    Icon
  },
  props: {
    message: {
      type: String,
      default: ''
    },
    type: {
      type: String,
      default: 'info',
      validator: (value) => ['success', 'error', 'warning', 'info'].includes(value)
    },
    duration: {
      type: Number,
      default: 3000
    }
  },
  data() {
    return {
      show: false
    }
  },
  computed: {
    toastClasses() {
      const baseClasses = 'text-white'
      const typeClasses = {
        success: 'bg-green-500',
        error: 'bg-red-500',
        warning: 'bg-yellow-500',
        info: 'bg-blue-500'
      }
      return `${baseClasses} ${typeClasses[this.type]}`
    },
    icon() {
      const icons = {
        success: 'mdi:check-circle',
        error: 'mdi:alert-circle',
        warning: 'mdi:alert',
        info: 'mdi:information'
      }
      return icons[this.type]
    },
    iconClasses() {
      return 'text-white'
    }
  },
  methods: {
    close() {
      this.show = false
    },
    showToast() {
      this.show = true
      if (this.duration > 0) {
        setTimeout(() => {
          this.close()
        }, this.duration)
      }
    }
  },
  watch: {
    message(newMessage) {
      if (newMessage) {
        this.showToast()
      }
    }
  },
  mounted() {
    if (this.message) {
      this.showToast()
    }
  }
}
</script>
<style scoped>
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}
.toast-enter-from {
  transform: translateX(100%);
  opacity: 0;
}
.toast-leave-to {
  transform: translateX(100%);
  opacity: 0;
}
</style>
