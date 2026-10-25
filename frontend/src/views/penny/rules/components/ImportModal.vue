<template>
  <div class="import-modal-backdrop" @click="closeOnBackdrop">
    <div class="import-modal" @click.stop>
      <div class="modal-header">
        <h2 class="modal-title">Import Rules from JSON</h2>
        <button @click="$emit('close')" class="close-button">
          <Icon icon="mdi:close" class="h-5 w-5" />
        </button>
      </div>

      <div class="modal-body">
        <form @submit.prevent="handleSubmit" class="import-form">
          <!-- File Upload -->
          <div class="form-group">
            <label class="form-label">
              Select JSON File <span class="required">*</span>
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
                <p class="upload-text">Click to select JSON file</p>
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

          <!-- Merge Strategy -->
          <div class="form-group">
            <label class="form-label">
              Merge Strategy <span class="required">*</span>
            </label>
            <select v-model="formData.strategy" class="form-select" required>
              <option value="OVERWRITE">Overwrite - Replace existing rules</option>
              <option value="SKIP">Skip - Keep existing rules</option>
              <option value="MERGE">Merge - Update existing, add new</option>
              <option value="APPEND">Append - Add new rules only</option>
            </select>
            <p class="form-help">{{ getStrategyDescription(formData.strategy) }}</p>
          </div>

          <!-- Dry Run -->
          <div class="form-group">
            <label class="checkbox-label">
              <input type="checkbox" v-model="formData.dryRun" class="checkbox-input" />
              <span class="checkbox-text">Dry Run - Preview changes without applying</span>
            </label>
            <p class="form-help">Test the import to see what would change</p>
          </div>

          <!-- Preview Results (shown after validation) -->
          <div v-if="previewResult" class="preview-section">
            <h3 class="preview-title">Import Preview</h3>
            <div class="preview-stats">
              <div class="stat-item">
                <span class="stat-label">Rules to Import:</span>
                <span class="stat-value">{{ previewResult.importedRules || 0 }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">Rules to Update:</span>
                <span class="stat-value">{{ previewResult.updatedRules || 0 }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">Rules to Skip:</span>
                <span class="stat-value">{{ previewResult.skippedRules || 0 }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">Templates to Import:</span>
                <span class="stat-value">{{ previewResult.importedTemplates || 0 }}</span>
              </div>
            </div>
            <div v-if="previewResult.previewChanges && previewResult.previewChanges.length > 0" class="preview-changes">
              <h4 class="changes-title">Changes Preview:</h4>
              <div class="changes-list">
                <div v-for="(change, index) in previewResult.previewChanges.slice(0, 10)" :key="index" class="change-item">
                  <span :class="['change-type', change.type.toLowerCase()]">{{ change.type }}</span>
                  <span class="change-name">{{ change.name }}</span>
                </div>
                <div v-if="previewResult.previewChanges.length > 10" class="more-changes">
                  +{{ previewResult.previewChanges.length - 10 }} more changes
                </div>
              </div>
            </div>
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
          {{ formData.dryRun ? 'Preview Import' : 'Import' }}
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
  name: 'ImportModal',
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
    const previewResult = ref(null)

    const formData = ref({
      strategy: 'MERGE',
      dryRun: false
    })

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
      previewResult.value = null
    }

    const getStrategyDescription = (strategy) => {
      const descriptions = {
        'OVERWRITE': 'Replace all existing rules with imported ones',
        'SKIP': 'Keep existing rules, only import new ones',
        'MERGE': 'Update existing rules and add new ones',
        'APPEND': 'Add new rules without modifying existing ones'
      }
      return descriptions[strategy] || ''
    }

    const handleSubmit = async () => {
      if (!selectedFile.value) {
        toast.warning('Please select a file first')
        return
      }

      importing.value = true
      try {
        const fileContent = await selectedFile.value.text()
        const importData = JSON.parse(fileContent)

        const response = await pennyApi.importCustomLogic(props.bot.id, {
          ...importData,
          strategy: formData.value.strategy,
          dryRun: formData.value.dryRun
        })

        previewResult.value = response.data

        if (formData.value.dryRun) {
          toast.success('Preview generated successfully')
        } else {
          toast.success('Rules imported successfully')
          emit('imported')
          emit('close')
        }
      } catch (error) {
        console.error('Import failed:', error)
        toast.error(error, 'Failed to import rules')
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
      previewResult,
      formData,
      handleFileChange,
      clearFile,
      getStrategyDescription,
      handleSubmit,
      closeOnBackdrop
    }
  }
}
</script>

<style scoped>
.import-modal-backdrop {
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

.import-modal {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
  max-width: 600px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
}

.dark .import-modal {
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

.form-select {
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  background: white;
  color: #1f2937;
}

.dark .form-select {
  background: #374151;
  border-color: #4b5563;
  color: white;
}

.form-help {
  font-size: 12px;
  color: #9ca3af;
  margin: 0;
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

.preview-section {
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.dark .preview-section {
  background: #374151;
  border-color: #4b5563;
}

.preview-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 12px 0;
}

.dark .preview-title {
  color: white;
}

.preview-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}

.stat-label {
  color: #6b7280;
}

.dark .stat-label {
  color: #9ca3af;
}

.stat-value {
  font-weight: 600;
  color: #1f2937;
}

.dark .stat-value {
  color: white;
}

.preview-changes {
  margin-top: 16px;
}

.changes-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.dark .changes-title {
  color: white;
}

.changes-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.change-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.change-type {
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
  text-transform: uppercase;
}

.change-type.create {
  background: #d1fae5;
  color: #065f46;
}

.change-type.update {
  background: #dbeafe;
  color: #1e40af;
}

.change-type.skip {
  background: #fef3c7;
  color: #92400e;
}

.change-name {
  color: #6b7280;
}

.dark .change-name {
  color: #9ca3af;
}

.more-changes {
  font-size: 12px;
  color: #9ca3af;
  font-style: italic;
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
