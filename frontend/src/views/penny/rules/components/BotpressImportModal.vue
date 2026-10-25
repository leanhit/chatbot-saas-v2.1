<template>
  <div class="botpress-import-modal-backdrop" @click="closeOnBackdrop">
    <div class="botpress-import-modal" @click.stop>
      <div class="modal-header">
        <h2 class="modal-title">Import from Botpress</h2>
        <button @click="$emit('close')" class="close-button">
          <Icon icon="mdi:close" class="h-5 w-5" />
        </button>
      </div>

      <div class="modal-body">
        <form @submit.prevent="handleSubmit" class="import-form">
          <!-- File Upload -->
          <div class="form-group">
            <label class="form-label">
              Select Botpress JSON File <span class="required">*</span>
            </label>
            <div class="file-upload" @click="$refs.fileInput.click()">
              <input
                ref="fileInput"
                type="file"
                accept=".json"
                @change="handleFileChange"
                class="hidden"
              />
              <div v-if="!selectedFile" class="upload-placeholder">
                <Icon icon="mdi:upload" class="upload-icon" />
                <p class="upload-text">Click to select Botpress JSON file</p>
                <p class="upload-hint">or drag and drop here</p>
              </div>
              <div v-else class="file-selected">
                <Icon icon="mdi:file-json" class="file-icon" />
                <span class="file-name">{{ selectedFile.name }}</span>
                <button type="button" @click.stop="clearFile" class="clear-file">
                  <Icon icon="mdi:close" />
                </button>
              </div>
            </div>
          </div>

          <!-- Import Options -->
          <div class="form-group">
            <label class="checkbox-label">
              <input type="checkbox" v-model="importQnA" class="checkbox-input" />
              <span class="checkbox-text">Import QnA items</span>
            </label>
          </div>

          <div class="form-group">
            <label class="checkbox-label">
              <input type="checkbox" v-model="importIntents" class="checkbox-input" />
              <span class="checkbox-text">Import Intents</span>
            </label>
          </div>

          <div class="form-group">
            <label class="checkbox-label">
              <input type="checkbox" v-model="importFlows" class="checkbox-input" />
              <span class="checkbox-text">Import Flows</span>
            </label>
          </div>

          <!-- Info Box -->
          <div class="info-box">
            <h4 class="info-title">Migration Info:</h4>
            <p class="info-text">
              This will convert Botpress QnA, Intents, and Flows to Penny rules.
              QnA items will be converted to response rules, Intents to keyword rules,
              and Flows to condition rules.
            </p>
          </div>
        </form>
      </div>

      <div class="modal-footer">
        <button type="button" @click="$emit('close')" class="btn btn-secondary">
          Cancel
        </button>
        <button
          type="submit"
          @click="handleSubmit"
          :disabled="!selectedFile || importing"
          class="btn btn-primary"
        >
          <Icon v-if="importing" icon="mdi:loading" class="animate-spin" />
          <Icon v-else icon="mdi:import" />
          Import from Botpress
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { Icon } from '@iconify/vue'
import { pennyApi } from '@/api/pennyApi'
import { useToast } from '@/composables/useToast'

export default {
  name: 'BotpressImportModal',
  components: {
    Icon
  },
  props: {
    bot: {
      type: Object,
      required: true
    }
  },
  emits: ['close', 'imported'],
  setup(props, { emit }) {
    const toast = useToast()
    const selectedFile = ref(null)
    const importing = ref(false)
    const importQnA = ref(true)
    const importIntents = ref(true)
    const importFlows = ref(true)

    const handleFileChange = (event) => {
      const file = event.target.files[0]
      if (file && file.type === 'application/json') {
        selectedFile.value = file
      } else {
        toast.error('Please select a valid JSON file')
      }
    }

    const clearFile = () => {
      selectedFile.value = null
    }

    const handleSubmit = async () => {
      if (!selectedFile.value) {
        toast.warning('Please select a file first')
        return
      }

      importing.value = true
      try {
        const fileContent = await selectedFile.value.text()
        const botpressData = JSON.parse(fileContent)

        const response = await pennyApi.importFromBotpress(props.bot.id, botpressData)
        
        toast.success('Botpress data imported successfully')
        emit('imported')
        emit('close')
      } catch (error) {
        console.error('Botpress import failed:', error)
        toast.error(error, 'Failed to import from Botpress')
      } finally {
        importing.value = false
      }
    }

    const closeOnBackdrop = (event) => {
      if (event.target === event.currentTarget) {
        emit('close')
      }
    }

    return {
      selectedFile,
      importing,
      importQnA,
      importIntents,
      importFlows,
      handleFileChange,
      clearFile,
      handleSubmit,
      closeOnBackdrop
    }
  }
}
</script>

<style scoped>
.botpress-import-modal-backdrop {
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

.botpress-import-modal {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
  max-width: 500px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
}

.dark .botpress-import-modal {
  background: #1f2937;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px;
  border-bottom: 1px solid #e5e7eb;
}

.dark .modal-header {
  border-bottom-color: #374151;
}

.modal-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.dark .modal-title {
  color: white;
}

.close-button {
  background: none;
  border: none;
  padding: 8px;
  border-radius: 6px;
  cursor: pointer;
  color: #6b7280;
  transition: all 0.2s;
}

.close-button:hover {
  background-color: #f3f4f6;
  color: #1f2937;
}

.dark .close-button {
  color: #9ca3af;
}

.dark .close-button:hover {
  background-color: #374151;
  color: white;
}

.modal-body {
  padding: 24px;
}

.import-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.dark .form-label {
  color: #d1d5db;
}

.required {
  color: #ef4444;
}

.file-upload {
  border: 2px dashed #d1d5db;
  border-radius: 8px;
  padding: 32px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
}

.file-upload:hover {
  border-color: #3b82f6;
  background: #f9fafb;
}

.dark .file-upload {
  border-color: #4b5563;
}

.dark .file-upload:hover {
  border-color: #3b82f6;
  background: #374151;
}

.hidden {
  display: none;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.upload-icon {
  font-size: 32px;
  color: #9ca3af;
}

.upload-text {
  font-size: 14px;
  color: #374151;
  margin: 0;
}

.dark .upload-text {
  color: #d1d5db;
}

.upload-hint {
  font-size: 12px;
  color: #9ca3af;
  margin: 0;
}

.file-selected {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #dbeafe;
  border-radius: 8px;
}

.dark .file-selected {
  background: #1e3a8a;
}

.file-icon {
  font-size: 24px;
  color: #3b82f6;
}

.file-name {
  flex: 1;
  font-size: 14px;
  color: #1f2937;
  font-weight: 500;
}

.dark .file-name {
  color: white;
}

.clear-file {
  background: none;
  border: none;
  padding: 4px;
  border-radius: 4px;
  cursor: pointer;
  color: #6b7280;
}

.clear-file:hover {
  background: rgba(0, 0, 0, 0.1);
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.checkbox-input {
  width: 16px;
  height: 16px;
  accent-color: #3b82f6;
}

.checkbox-text {
  font-size: 14px;
  color: #374151;
}

.dark .checkbox-text {
  color: #d1d5db;
}

.info-box {
  padding: 16px;
  background: #dbeafe;
  border: 1px solid #93c5fd;
  border-radius: 8px;
}

.dark .info-box {
  background: #1e3a8a;
  border-color: #3b82f6;
}

.info-title {
  font-size: 14px;
  font-weight: 600;
  color: #1e40af;
  margin: 0 0 8px 0;
}

.dark .info-title {
  color: #93c5fd;
}

.info-text {
  font-size: 13px;
  color: #1e40af;
  margin: 0;
  line-height: 1.5;
}

.dark .info-text {
  color: #93c5fd;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 24px;
  border-top: 1px solid #e5e7eb;
}

.dark .modal-footer {
  border-top-color: #374151;
}

.btn {
  padding: 10px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary {
  background: #f3f4f6;
  color: #374151;
}

.btn-secondary:hover {
  background: #e5e7eb;
}

.dark .btn-secondary {
  background: #374151;
  color: #d1d5db;
}

.dark .btn-secondary:hover {
  background: #4b5563;
}

.btn-primary {
  background: #3b82f6;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #2563eb;
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
