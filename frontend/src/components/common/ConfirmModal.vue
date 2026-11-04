<template>
  <Teleport to="body">
    <Transition name="modal-fade">
      <div
        v-if="modelValue"
        class="fixed inset-0 z-[9999] flex items-center justify-center p-4"
        @click.self="onCancel"
      >
        <!-- Backdrop -->
        <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" />

        <!-- Dialog -->
        <div
          class="relative bg-white dark:bg-gray-800 rounded-2xl shadow-2xl max-w-md w-full overflow-hidden border border-gray-200 dark:border-gray-700"
          role="dialog"
          aria-modal="true"
          :aria-labelledby="`confirm-title-${uid}`"
        >
          <!-- Top accent bar -->
          <div :class="accentClass" class="h-1 w-full" />

          <div class="p-6">
            <!-- Icon + Title -->
            <div class="flex items-start gap-4 mb-4">
              <div
                :class="iconBgClass"
                class="flex-shrink-0 w-11 h-11 rounded-full flex items-center justify-center"
              >
                <Icon :icon="iconName" :class="iconColorClass" class="text-xl" />
              </div>
              <div class="flex-1 min-w-0">
                <h3
                  :id="`confirm-title-${uid}`"
                  class="text-base font-semibold text-gray-900 dark:text-white leading-snug"
                >
                  {{ title }}
                </h3>
                <p v-if="message" class="mt-1 text-sm text-gray-500 dark:text-gray-400 leading-relaxed">
                  {{ message }}
                </p>
              </div>
            </div>

            <!-- Slot for custom content -->
            <slot />

            <!-- Actions -->
            <div class="flex justify-end gap-3 mt-6">
              <button
                type="button"
                :id="`confirm-cancel-${uid}`"
                class="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-lg transition-colors"
                @click="onCancel"
              >
                {{ cancelText || 'Hủy' }}
              </button>
              <button
                type="button"
                :id="`confirm-ok-${uid}`"
                :class="confirmBtnClass"
                class="px-4 py-2 text-sm font-medium rounded-lg transition-all focus:ring-2 focus:ring-offset-2"
                @click="onConfirm"
              >
                {{ confirmText || 'Xác nhận' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { computed } from 'vue'
import { Icon } from '@iconify/vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: 'Xác nhận' },
  message: { type: String, default: '' },
  confirmText: { type: String, default: 'Xác nhận' },
  cancelText: { type: String, default: 'Hủy' },
  /**
   * 'danger' | 'warning' | 'info'
   */
  variant: { type: String, default: 'danger' },
})

const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

// Unique id to avoid duplicate aria IDs when multiple modals mount
const uid = Math.random().toString(36).slice(2, 8)

const variantMap = {
  danger: {
    accent: 'bg-red-500',
    iconBg: 'bg-red-100 dark:bg-red-900/40',
    iconColor: 'text-red-600 dark:text-red-400',
    icon: 'mdi:alert-circle',
    btn: 'bg-red-600 hover:bg-red-700 text-white focus:ring-red-500',
  },
  warning: {
    accent: 'bg-amber-500',
    iconBg: 'bg-amber-100 dark:bg-amber-900/40',
    iconColor: 'text-amber-600 dark:text-amber-400',
    icon: 'mdi:alert',
    btn: 'bg-amber-500 hover:bg-amber-600 text-white focus:ring-amber-400',
  },
  info: {
    accent: 'bg-blue-500',
    iconBg: 'bg-blue-100 dark:bg-blue-900/40',
    iconColor: 'text-blue-600 dark:text-blue-400',
    icon: 'mdi:information',
    btn: 'bg-blue-600 hover:bg-blue-700 text-white focus:ring-blue-500',
  },
}

const v = computed(() => variantMap[props.variant] || variantMap.danger)
const accentClass = computed(() => v.value.accent)
const iconBgClass = computed(() => v.value.iconBg)
const iconColorClass = computed(() => v.value.iconColor)
const iconName = computed(() => v.value.icon)
const confirmBtnClass = computed(() => v.value.btn)

function onConfirm() {
  emit('confirm')
  emit('update:modelValue', false)
}

function onCancel() {
  emit('cancel')
  emit('update:modelValue', false)
}
</script>

<style scoped>
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.2s ease;
}
.modal-fade-enter-active .relative,
.modal-fade-leave-active .relative {
  transition: transform 0.2s ease, opacity 0.2s ease;
}
.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}
.modal-fade-enter-from .relative {
  transform: scale(0.95) translateY(-8px);
  opacity: 0;
}
.modal-fade-leave-to .relative {
  transform: scale(0.95) translateY(-8px);
  opacity: 0;
}
</style>
