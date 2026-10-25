<template>
  <div class="diff-tool-panel">
    <div class="panel-header">
      <h2 class="panel-title">{{ $t('penny.rules.diff.title') }}</h2>
      <p class="panel-description">{{ $t('penny.rules.diff.description') }}</p>
    </div>

    <!-- Version Selection -->
    <div class="version-selection">
      <div class="version-input">
        <label class="version-label">{{ $t('penny.rules.diff.selectVersion1') }}:</label>
        <div class="version-input-group">
          <select v-model="version1" class="version-select">
            <option value="">{{ $t('penny.rules.diff.selectVersions') }}</option>
            <option v-for="version in availableVersions" :key="version" :value="version">
              {{ version }}
            </option>
          </select>
          <button @click="loadVersion1" class="btn btn-secondary" :disabled="!version1">
            <Icon icon="mdi:upload" />
            Upload
          </button>
        </div>
      </div>

      <div class="version-input">
        <label class="version-label">{{ $t('penny.rules.diff.selectVersion2') }}:</label>
        <div class="version-input-group">
          <select v-model="version2" class="version-select">
            <option value="">{{ $t('penny.rules.diff.selectVersions') }}</option>
            <option v-for="version in availableVersions" :key="version" :value="version">
              {{ version }}
            </option>
          </select>
          <button @click="loadVersion2" class="btn btn-secondary" :disabled="!version2">
            <Icon icon="mdi:upload" />
            Upload
          </button>
        </div>
      </div>

      <div class="compare-actions">
        <button @click="compareVersions" :disabled="!version1 || !version2 || comparing" class="btn btn-primary">
          <Icon v-if="comparing" icon="mdi:loading" class="animate-spin" />
          <Icon v-else icon="mdi:compare" />
          {{ $t('penny.rules.diff.compare') }}
        </button>
      </div>
    </div>

    <!-- File Upload for Custom Versions -->
    <div class="file-upload-section">
      <h3 class="section-title">{{ $t('penny.rules.diff.uploadCustom') }}</h3>
      <div class="file-uploads">
        <div class="file-upload-card">
          <label class="file-label">{{ $t('penny.rules.diff.selectVersion1') }} File:</label>
          <input
            ref="file1Input"
            type="file"
            accept=".json"
            @change="handleFile1Change"
            class="file-input"
          />
          <div v-if="file1Name" class="file-name">{{ file1Name }}</div>
          <div v-else class="file-placeholder">No file selected</div>
        </div>

        <div class="file-upload-card">
          <label class="file-label">{{ $t('penny.rules.diff.selectVersion2') }} File:</label>
          <input
            ref="file2Input"
            type="file"
            accept=".json"
            @change="handleFile2Change"
            class="file-input"
          />
          <div v-if="file2Name" class="file-name">{{ file2Name }}</div>
          <div v-else class="file-placeholder">No file selected</div>
        </div>
      </div>
    </div>

    <!-- Diff Results -->
    <div v-if="diffResult" class="diff-results">
      <h3 class="section-title">{{ $t('penny.rules.diff.summary') }}</h3>

      <!-- Summary -->
      <div class="diff-summary">
        <div class="summary-card added">
          <Icon icon="mdi:plus-circle" class="summary-icon" />
          <div class="summary-content">
            <span class="summary-number">{{ diffResult.addedRules || 0 }}</span>
            <span class="summary-label">{{ $t('penny.rules.diff.addedRules') }}</span>
          </div>
        </div>
        <div class="summary-card removed">
          <Icon icon="mdi:minus-circle" class="summary-icon" />
          <div class="summary-content">
            <span class="summary-number">{{ diffResult.removedRules || 0 }}</span>
            <span class="summary-label">{{ $t('penny.rules.diff.removedRules') }}</span>
          </div>
        </div>
        <div class="summary-card modified">
          <Icon icon="mdi:pencil-circle" class="summary-icon" />
          <div class="summary-content">
            <span class="summary-number">{{ diffResult.modifiedRules || 0 }}</span>
            <span class="summary-label">{{ $t('penny.rules.diff.modifiedRules') }}</span>
          </div>
        </div>
        <div class="summary-card templates">
          <Icon icon="mdi:file-document" class="summary-icon" />
          <div class="summary-content">
            <span class="summary-number">{{ diffResult.templateChanges || 0 }}</span>
            <span class="summary-label">{{ $t('penny.rules.diff.templateChanges') }}</span>
          </div>
        </div>
      </div>

      <!-- Detailed Changes -->
      <div class="detailed-changes">
        <div v-if="diffResult.addedRulesList && diffResult.addedRulesList.length > 0" class="change-section">
          <h4 class="change-title added">{{ $t('penny.rules.diff.addedRules') }}</h4>
          <div class="change-list">
            <div v-for="(rule, index) in diffResult.addedRulesList" :key="index" class="change-item added">
              <Icon icon="mdi:plus" class="change-icon" />
              <span class="change-name">{{ rule.name }}</span>
            </div>
          </div>
        </div>

        <div v-if="diffResult.removedRulesList && diffResult.removedRulesList.length > 0" class="change-section">
          <h4 class="change-title removed">{{ $t('penny.rules.diff.removedRules') }}</h4>
          <div class="change-list">
            <div v-for="(rule, index) in diffResult.removedRulesList" :key="index" class="change-item removed">
              <Icon icon="mdi:minus" class="change-icon" />
              <span class="change-name">{{ rule.name }}</span>
            </div>
          </div>
        </div>

        <div v-if="diffResult.modifiedRulesList && diffResult.modifiedRulesList.length > 0" class="change-section">
          <h4 class="change-title modified">{{ $t('penny.rules.diff.modifiedRules') }}</h4>
          <div class="change-list">
            <div v-for="(rule, index) in diffResult.modifiedRulesList" :key="index" class="change-item modified">
              <Icon icon="mdi:pencil" class="change-icon" />
              <span class="change-name">{{ rule.name }}</span>
              <div class="change-details">
                <span class="change-field">{{ rule.field }}</span>
                <span class="change-from">{{ rule.oldValue }}</span>
                <Icon icon="mdi:arrow-right" class="arrow-icon" />
                <span class="change-to">{{ rule.newValue }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="empty-state">
      <Icon icon="mdi:compare" class="empty-icon" />
      <h3 class="empty-title">{{ $t('penny.rules.diff.noChanges') }}</h3>
      <p class="empty-description">{{ $t('penny.rules.diff.noChangesDesc') }}</p>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { Icon } from '@iconify/vue'
import { pennyApi } from '@/api/pennyApi'
import { useToast } from '@/composables/useToast'

export default {
  name: 'DiffToolPanel',
  components: {
    Icon
  },
  props: {
    bot: {
      type: Object,
      required: true
    }
  },
  setup(props) {
    const { t } = useI18n()
    const toast = useToast()
    const version1 = ref('')
    const version2 = ref('')
    const comparing = ref(false)
    const diffResult = ref(null)
    const availableVersions = ref(['v1.0.0', 'v1.1.0', 'v1.2.0', 'v2.0.0'])
    const file1Input = ref(null)
    const file2Input = ref(null)
    const file1Name = ref('')
    const file2Name = ref('')
    const version1Data = ref(null)
    const version2Data = ref(null)

    const handleFile1Change = (event) => {
      const file = event.target.files[0]
      if (file) {
        file1Name.value = file.name
        const reader = new FileReader()
        reader.onload = (e) => {
          customVersion1.value = JSON.parse(e.target.result)
        }
        reader.readAsText(file)
      }
    }

    const handleFile2Change = (event) => {
      const file = event.target.files[0]
      if (file) {
        file2Name.value = file.name
        const reader = new FileReader()
        reader.onload = (e) => {
          customVersion2.value = JSON.parse(e.target.result)
        }
        reader.readAsText(file)
      }
    }

    const loadVersion1 = () => {
      // In real app, fetch version from backend
      toast.info(`Loading version ${version1.value}...`)
    }

    const loadVersion2 = () => {
      // In real app, fetch version from backend
      toast.info(`Loading version ${version2.value}...`)
    }

    const compareVersions = async () => {
      if (!version1.value || !version2.value) {
        toast.warning('Please select both versions to compare')
        return
      }

      comparing.value = true
      try {
        const response = await pennyApi.diffExports(props.bot.id, version1.value, version2.value)
        diffResult.value = response.data
        toast.success('Comparison completed successfully')
      } catch (error) {
        console.error('Diff failed:', error)
        toast.error(error, 'Failed to compare versions')
      } finally {
        comparing.value = false
      }
    }

    return {
      version1,
      version2,
      comparing,
      diffResult,
      file1Name,
      file2Name,
      availableVersions,
      handleFile1Change,
      handleFile2Change,
      loadVersion1,
      loadVersion2,
      compareVersions
    }
  }
}
</script>

<style scoped>
.diff-tool-panel {
  width: 100%;
}

.panel-header {
  margin-bottom: 24px;
}

.panel-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.dark .panel-title {
  color: white;
}

.panel-description {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.dark .panel-description {
  color: #9ca3af;
}

.version-selection {
  display: grid;
  grid-template-columns: 1fr 1fr auto;
  gap: 16px;
  margin-bottom: 32px;
  align-items: end;
}

.version-input {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.version-label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.dark .version-label {
  color: #d1d5db;
}

.version-input-group {
  display: flex;
  gap: 8px;
}

.version-select {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  background: white;
  color: #1f2937;
}

.dark .version-select {
  background: #374151;
  border-color: #4b5563;
  color: white;
}

.compare-actions {
  display: flex;
  align-items: center;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary {
  background: #f3f4f6;
  color: #374151;
}

.btn-secondary:hover:not(:disabled) {
  background: #e5e7eb;
}

.dark .btn-secondary {
  background: #374151;
  color: #d1d5db;
}

.dark .btn-secondary:hover:not(:disabled) {
  background: #4b5563;
}

.btn-primary {
  background: #3b82f6;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #2563eb;
}

.file-upload-section {
  margin-bottom: 32px;
  padding: 20px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.dark .file-upload-section {
  background: #374151;
  border-color: #4b5563;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 16px 0;
}

.dark .section-title {
  color: white;
}

.file-uploads {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.file-upload-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.file-label {
  font-size: 13px;
  font-weight: 500;
  color: #6b7280;
}

.dark .file-label {
  color: #9ca3af;
}

.file-input {
  padding: 8px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 13px;
}

.dark .file-input {
  background: #374151;
  border-color: #4b5563;
  color: white;
}

.file-name {
  padding: 8px 12px;
  background: #dbeafe;
  border-radius: 6px;
  font-size: 13px;
  color: #1e40af;
}

.dark .file-name {
  background: #1e3a8a;
  color: #93c5fd;
}

.file-placeholder {
  padding: 8px 12px;
  background: #f3f4f6;
  border-radius: 6px;
  font-size: 13px;
  color: #9ca3af;
}

.dark .file-placeholder {
  background: #4b5563;
  color: #6b7280;
}

.diff-results {
  margin-top: 32px;
}

.diff-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.summary-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.dark .summary-card {
  border-color: #374151;
}

.summary-card.added {
  background: #d1fae5;
  border-color: #34d399;
}

.summary-card.removed {
  background: #fee2e2;
  border-color: #f87171;
}

.summary-card.modified {
  background: #fef3c7;
  border-color: #fbbf24;
}

.summary-card.templates {
  background: #dbeafe;
  border-color: #60a5fa;
}

.summary-icon {
  font-size: 24px;
}

.summary-card.added .summary-icon {
  color: #059669;
}

.summary-card.removed .summary-icon {
  color: #dc2626;
}

.summary-card.modified .summary-icon {
  color: #d97706;
}

.summary-card.templates .summary-icon {
  color: #2563eb;
}

.summary-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.summary-number {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
}

.dark .summary-number {
  color: white;
}

.summary-label {
  font-size: 12px;
  color: #6b7280;
}

.dark .summary-label {
  color: #9ca3af;
}

.detailed-changes {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.change-section {
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.dark .change-section {
  border-color: #374151;
}

.change-title {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 12px 0;
}

.change-title.added {
  color: #059669;
}

.change-title.removed {
  color: #dc2626;
}

.change-title.modified {
  color: #d97706;
}

.change-list {
  padding-left: 0;
  margin: 0;
  list-style: none;
}

.change-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  border-radius: 4px;
  margin-bottom: 4px;
}

.change-item.added {
  background: #d1fae5;
}

.change-item.removed {
  background: #fee2e2;
}

.change-item.modified {
  background: #fef3c7;
}

.change-icon {
  font-size: 16px;
}

.change-item.added .change-icon {
  color: #059669;
}

.change-item.removed .change-icon {
  color: #dc2626;
}

.change-item.modified .change-icon {
  color: #d97706;
}

.change-name {
  flex: 1;
  font-size: 13px;
  font-weight: 500;
  color: #374151;
}

.dark .change-name {
  color: #d1d5db;
}

.change-details {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.change-field {
  color: #6b7280;
  font-weight: 500;
}

.dark .change-field {
  color: #9ca3af;
}

.change-from {
  color: #dc2626;
  text-decoration: line-through;
}

.change-to {
  color: #059669;
  font-weight: 500;
}

.arrow-icon {
  font-size: 14px;
  color: #9ca3af;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.empty-icon {
  font-size: 48px;
  color: #d1d5db;
  margin-bottom: 16px;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.dark .empty-title {
  color: white;
}

.empty-description {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.dark .empty-description {
  color: #9ca3af;
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
