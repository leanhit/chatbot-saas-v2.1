<template>
  <div class="import-export-panel">
    <div class="panel-header">
      <h2 class="panel-title">{{ $t('penny.rules.importExport.title') }}</h2>
      <p class="panel-description">{{ $t('penny.rules.importExport.description') }}</p>
    </div>

    <!-- Export Section -->
    <div class="section">
      <h3 class="section-title">
        <Icon icon="mdi:export" class="section-icon" />
        {{ $t('penny.rules.importExport.export') }}
      </h3>
      <div class="export-actions">
        <div class="action-card">
          <div class="action-icon">
            <Icon icon="mdi:json" />
          </div>
          <div class="action-content">
            <h4 class="action-title">{{ $t('penny.rules.importExport.exportToJson') }}</h4>
            <p class="action-description">{{ $t('penny.rules.importExport.exportToJsonDesc') }}</p>
            <button @click="exportToJSON" class="btn btn-primary">
              <Icon icon="mdi:download" class="btn-icon" />
              {{ $t('penny.rules.importExport.exportJson') }}
            </button>
          </div>
        </div>

        <div class="action-card">
          <div class="action-icon">
            <Icon icon="mdi:file-excel" />
          </div>
          <div class="action-content">
            <h4 class="action-title">{{ $t('penny.rules.importExport.exportRulesToCsv') }}</h4>
            <p class="action-description">{{ $t('penny.rules.importExport.exportRulesToCsvDesc') }}</p>
            <button @click="exportRulesToCSV" class="btn btn-primary">
              <Icon icon="mdi:download" class="btn-icon" />
              {{ $t('penny.rules.importExport.exportCsv') }}
            </button>
          </div>
        </div>

        <div class="action-card">
          <div class="action-icon">
            <Icon icon="mdi:file-table" />
          </div>
          <div class="action-content">
            <h4 class="action-title">{{ $t('penny.rules.importExport.exportTemplatesToCsv') }}</h4>
            <p class="action-description">{{ $t('penny.rules.importExport.exportTemplatesToCsvDesc') }}</p>
            <button @click="exportTemplatesToCSV" class="btn btn-primary">
              <Icon icon="mdi:download" class="btn-icon" />
              {{ $t('penny.rules.importExport.exportCsv') }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Import Section -->
    <div class="section">
      <h3 class="section-title">
        <Icon icon="mdi:import" class="section-icon" />
        {{ $t('penny.rules.importExport.import') }}
      </h3>
      <div class="import-actions">
        <div class="action-card">
          <div class="action-icon">
            <Icon icon="mdi:json" />
          </div>
          <div class="action-content">
            <h4 class="action-title">{{ $t('penny.rules.importExport.importFromJson') }}</h4>
            <p class="action-description">{{ $t('penny.rules.importExport.importFromJsonDesc') }}</p>
            <button @click="showImportModal = true" class="btn btn-primary">
              <Icon icon="mdi:upload" class="btn-icon" />
              {{ $t('penny.rules.importExport.importJson') }}
            </button>
          </div>
        </div>

        <div class="action-card">
          <div class="action-icon">
            <Icon icon="mdi:file-excel" />
          </div>
          <div class="action-content">
            <h4 class="action-title">{{ $t('penny.rules.importExport.importRulesFromCsv') }}</h4>
            <p class="action-description">{{ $t('penny.rules.importExport.importRulesFromCsvDesc') }}</p>
            <button @click="showCSVImportModal = true" class="btn btn-primary">
              <Icon icon="mdi:upload" class="btn-icon" />
              {{ $t('penny.rules.importExport.importCsv') }}
            </button>
          </div>
        </div>

        <div class="action-card">
          <div class="action-icon">
            <Icon icon="mdi:robot" />
          </div>
          <div class="action-content">
            <h4 class="action-title">{{ $t('penny.rules.importExport.importFromBotpress') }}</h4>
            <p class="action-description">{{ $t('penny.rules.importExport.importFromBotpressDesc') }}</p>
            <button @click="showBotpressImportModal = true" class="btn btn-primary">
              <Icon icon="mdi:upload" class="btn-icon" />
              {{ $t('penny.rules.importExport.importBotpress') }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Import Modal -->
    <ImportModal
      v-if="showImportModal"
      :bot="bot"
      @close="showImportModal = false"
      @imported="onImported"
    />

    <!-- CSV Import Modal -->
    <CSVImportModal
      v-if="showCSVImportModal"
      :bot="bot"
      @close="showCSVImportModal = false"
      @imported="onImported"
    />

    <!-- Botpress Import Modal -->
    <BotpressImportModal
      v-if="showBotpressImportModal"
      :bot="bot"
      @close="showBotpressImportModal = false"
      @imported="onImported"
    />
  </div>
</template>

<script>
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { Icon } from '@iconify/vue'
import { pennyApi } from '@/api/pennyApi'
import { useToast } from '@/composables/useToast'
import ImportModal from './ImportModal.vue'
import CSVImportModal from './CSVImportModal.vue'
import BotpressImportModal from './BotpressImportModal.vue'

export default {
  name: 'ImportExportPanel',
  components: {
    Icon,
    ImportModal,
    CSVImportModal,
    BotpressImportModal
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
    const showImportModal = ref(false)
    const showCSVImportModal = ref(false)
    const showBotpressImportModal = ref(false)

    const exportToJSON = async () => {
      try {
        const response = await pennyApi.exportCustomLogic(props.bot.id)
        const data = response.data
        
        // Create JSON file and download
        const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
        const url = URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `penny-rules-${props.bot.botName}-${Date.now()}.json`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        URL.revokeObjectURL(url)
        
        toast.success('Rules exported successfully')
      } catch (error) {
        console.error('Export failed:', error)
        toast.error(error, 'Failed to export rules')
      }
    }

    const exportRulesToCSV = async () => {
      try {
        const response = await pennyApi.exportRulesToCSV(props.bot.id)
        const blob = new Blob([response.data], { type: 'text/csv' })
        const url = URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `penny-rules-${props.bot.botName}-${Date.now()}.csv`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        URL.revokeObjectURL(url)
        
        toast.success('Rules exported to CSV successfully')
      } catch (error) {
        console.error('CSV export failed:', error)
        toast.error(error, 'Failed to export rules to CSV')
      }
    }

    const exportTemplatesToCSV = async () => {
      try {
        const response = await pennyApi.exportTemplatesToCSV(props.bot.id)
        const blob = new Blob([response.data], { type: 'text/csv' })
        const url = URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `penny-templates-${props.bot.botName}-${Date.now()}.csv`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        URL.revokeObjectURL(url)
        
        toast.success('Templates exported to CSV successfully')
      } catch (error) {
        console.error('CSV export failed:', error)
        toast.error(error, 'Failed to export templates to CSV')
      }
    }

    const onImported = () => {
      toast.success('Import completed successfully')
    }

    return {
      showImportModal,
      showCSVImportModal,
      showBotpressImportModal,
      exportToJSON,
      exportRulesToCSV,
      exportTemplatesToCSV,
      onImported
    }
  }
}
</script>

<style scoped>
.import-export-panel {
  width: 100%;
}

.panel-header {
  margin-bottom: 32px;
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

.section {
  margin-bottom: 32px;
}

.section:last-child {
  margin-bottom: 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 16px 0;
}

.dark .section-title {
  color: white;
}

.section-icon {
  font-size: 20px;
  color: #3b82f6;
}

.export-actions,
.import-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
}

.action-card {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  transition: all 0.2s;
}

.dark .action-card {
  background: #1f2937;
  border-color: #374151;
}

.action-card:hover {
  border-color: #3b82f6;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.1);
}

.action-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #dbeafe;
  border-radius: 8px;
  color: #3b82f6;
  font-size: 24px;
  flex-shrink: 0;
}

.dark .action-icon {
  background: #1e3a8a;
}

.action-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.action-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.dark .action-title {
  color: white;
}

.action-description {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
}

.dark .action-description {
  color: #9ca3af;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.btn-icon {
  font-size: 16px;
}

.btn-primary {
  background: #3b82f6;
  color: white;
}

.btn-primary:hover {
  background: #2563eb;
}
</style>
