<template>
  <div v-if="visible" class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50" @click="handleOverlayClick">
    <div class="relative top-20 mx-auto p-4 border w-96 shadow-lg rounded-md bg-white dark:bg-gray-800 border-gray-200 dark:border-gray-700" @click.stop>
      <div class="flex items-center justify-between border-b border-gray-200 dark:border-gray-700 pb-4">
        <h3 class="text-lg font-medium text-blue-600 dark:text-blue-400">{{ $t('tenantGateway.createNewWorkspace') }}</h3>
        <router-link to="/tenant/gateway" class="text-blue-600 hover:text-blue-500">
          {{ $t('tenantGateway.title') }}
        </router-link>
      </div>
      <form @submit.prevent="handleSubmit" class="mt-4">
        <div class="mb-4">
          <label for="name" class="block text-sm font-medium text-gray-700 dark:text-gray-300">{{ $t('Workspace Name') }} *</label>
          <input
            id="name"
            v-model="formData.name"
            type="text"
            class="mt-1 block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
            :placeholder="$t('Enter workspace name')"
            required
          />
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('Visibility') }} *</label>
          <div class="radio-group">
            <label class="radio-label">
              <input
                v-model="formData.visibility"
                type="radio"
                value="PUBLIC"
                class="radio-input"
              />
              <span class="radio-text">{{ $t('Public') }}</span>
              <span class="radio-description">{{ $t('Anyone can find and join') }}</span>
            </label>
            <label class="radio-label">
              <input
                v-model="formData.visibility"
                type="radio"
                value="PRIVATE"
                class="radio-input"
              />
              <span class="radio-text">{{ $t('Private') }}</span>
              <span class="radio-description">{{ $t('Invite only') }}</span>
            </label>
          </div>
        </div>
      </form>
      <div class="modal-footer">
        <button
          type="button"
          @click="handleClose"
          class="btn btn-secondary"
          :disabled="loading"
        >
          {{ $t('Cancel') }}
        </button>
        <button
          type="submit"
          @click="handleSubmit"
          class="btn btn-primary"
          :disabled="loading || !isFormValid"
        >
          <Icon v-if="loading" icon="mdi:loading" class="animate-spin h-4 w-4 mr-2" />
          {{ $t('Create') }}
        </button>
      </div>
    </div>
  </div>
</template>
<script>
import { ref, computed, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { useI18n } from 'vue-i18n'
import { useGatewayCreateTenantStore } from '@/stores/tenant/gateway/createTenantStore'
export default {
  name: 'CreateTenantModal',
  components: {
    Icon
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  emits: ['close', 'created'],
  setup(props, { emit }) {
    const { t } = useI18n()
    const createTenantStore = useGatewayCreateTenantStore()
    const formData = ref({
      name: '',
      visibility: 'PUBLIC'
    })
    const loading = computed(() => createTenantStore.loading)
    const error = computed(() => createTenantStore.error)
    const isFormValid = computed(() => {
      return formData.value.name.trim() !== '' && formData.value.visibility !== ''
    })
    const handleClose = () => {
      if (!loading.value) {
        resetForm()
        emit('close')
      }
    }
    const handleOverlayClick = () => {
      handleClose()
    }
    const handleSubmit = async () => {
      if (!isFormValid.value) return
      try {
        const result = await createTenantStore.createTenant({
          name: formData.value.name.trim(),
          visibility: formData.value.visibility
        })
        if (result.success) {
          emit('created')
          resetForm()
        }
      } catch (error) {
      }
    }
    const resetForm = () => {
      formData.value = {
        name: '',
        visibility: 'PUBLIC'
      }
      createTenantStore.clearError()
    }
    // Watch for visibility changes to reset form when modal opens
    watch(() => props.visible, (newVal) => {
      if (newVal) {
        resetForm()
      }
    })
    return {
      formData,
      loading,
      error,
      isFormValid,
      handleClose,
      handleOverlayClick,
      handleSubmit
    }
  }
}
</script>
<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}
.modal-container {
  background: white;
  dark:bg-gray-800;
  border-radius: 12px;
  width: 100%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 24px 20px;
  border-bottom: 1px solid #e5e7eb;
  dark:border-gray-700;
}
.modal-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  dark:color-white;
}
.close-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 6px;
  border: none;
  background: transparent;
  color: #6b7280;
  dark:color-gray-400;
  cursor: pointer;
  transition: background-color 0.2s;
}
.close-button:hover {
  background-color: #f3f4f6;
  dark:bg-gray-700;
}
.modal-body {
  padding: 24px;
}
.form-group {
  margin-bottom: 20px;
}
.form-label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  dark:color-gray-300;
}
.form-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  dark:border-gray-600;
  border-radius: 6px;
  font-size: 14px;
  background: white;
  dark:bg-gray-700;
  color: #1f2937;
  dark:color-white;
  transition: border-color 0.2s;
}
.form-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}
.radio-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.radio-label {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  border: 1px solid #e5e7eb;
  dark:border-gray-600;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.radio-label:hover {
  border-color: #3b82f6;
  background-color: #f8fafc;
  dark:bg-gray-700;
}
.radio-input {
  margin-top: 2px;
}
.radio-text {
  font-weight: 500;
  color: #1f2937;
  dark:color-white;
  display: block;
  margin-bottom: 2px;
}
.radio-description {
  font-size: 13px;
  color: #6b7280;
  dark:color-gray-400;
}
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid #e5e7eb;
  dark:border-gray-700;
}
.btn {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
  display: inline-flex;
  align-items: center;
}
.btn-secondary {
  background: white;
  dark:bg-gray-700;
  color: #6b7280;
  dark:color-gray-300;
  border-color: #d1d5db;
  dark:border-gray-600;
}
.btn-secondary:hover:not(:disabled) {
  background: #f9fafb;
  dark:bg-gray-600;
}
.btn-primary {
  background: #3b82f6;
  color: white;
}
.btn-primary:hover:not(:disabled) {
  background: #2563eb;
}
.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
.animate-spin {
  animation: spin 1s linear infinite;
}
</style>
