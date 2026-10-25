<template>
  <div v-if="show" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-2xl w-full max-w-2xl mx-4 max-h-[90vh] overflow-hidden flex flex-col">
      <!-- Header -->
      <div class="p-6 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between">
        <div class="flex items-center gap-3">
          <div class="p-2 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-lg text-white">
            <Icon icon="mdi:file-import-export" class="text-xl" />
          </div>
          <div>
            <h2 class="text-xl font-bold text-gray-900 dark:text-white">
              {{ $t('penny.config.importExportTitle') }}
            </h2>
            <p class="text-sm text-gray-500 dark:text-gray-400">
              {{ $t('penny.config.importExportSubtitle') }}
            </p>
          </div>
        </div>
        <button
          @click="close"
          class="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors text-gray-500 dark:text-gray-400"
          :title="$t('common.close')"
        >
          <Icon icon="mdi:close" class="text-xl" />
        </button>
      </div>

      <!-- Content -->
      <div class="p-6 overflow-y-auto flex-1">
        <!-- Tabs -->
        <div class="flex gap-2 mb-6">
          <button
            @click="activeTab = 'import'"
            :class="[
              'px-4 py-2 rounded-lg font-medium text-sm transition-all',
              activeTab === 'import'
                ? 'bg-indigo-600 text-white'
                : 'bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'
            ]"
          >
            <Icon icon="mdi:file-import" class="mr-2" />
            {{ $t('penny.config.importTab') }}
          </button>
          <button
            @click="activeTab = 'export'"
            :class="[
              'px-4 py-2 rounded-lg font-medium text-sm transition-all',
              activeTab === 'export'
                ? 'bg-indigo-600 text-white'
                : 'bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'
            ]"
          >
            <Icon icon="mdi:file-export" class="mr-2" />
            {{ $t('penny.config.exportTab') }}
          </button>
        </div>

        <!-- Import Tab -->
        <div v-if="activeTab === 'import'" class="space-y-6">
          <!-- JSON Import -->
          <div class="p-4 border border-gray-200 dark:border-gray-700 rounded-xl bg-gray-50 dark:bg-gray-900/40">
            <h3 class="font-semibold text-gray-900 dark:text-white mb-3 flex items-center gap-2">
              <Icon icon="mdi:code-json" class="text-indigo-500" />
              {{ $t('penny.config.importJsonTitle') }}
            </h3>
            <p class="text-sm text-gray-600 dark:text-gray-400 mb-3">
              {{ $t('penny.config.importJsonHelp') }}
            </p>
            <div class="flex gap-3">
              <input
                ref="jsonFileInput"
                type="file"
                accept=".json"
                @change="handleJsonFileSelect"
                class="hidden"
              />
              <button
                @click="$refs.jsonFileInput.click()"
                :disabled="importing"
                class="flex-1 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-400 text-white rounded-lg font-medium transition-colors flex items-center justify-center gap-2"
              >
                <Icon v-if="importing" icon="mdi:loading" class="animate-spin" />
                <Icon v-else icon="mdi:upload" />
                {{ importing ? $t('penny.config.importing') : $t('penny.config.selectJsonFile') }}
              </button>
            </div>
          </div>

          <!-- CSV Import -->
          <div class="p-4 border border-gray-200 dark:border-gray-700 rounded-xl bg-gray-50 dark:bg-gray-900/40">
            <h3 class="font-semibold text-gray-900 dark:text-white mb-3 flex items-center gap-2">
              <Icon icon="mdi:file-table" class="text-emerald-500" />
              {{ $t('penny.config.importCsvTitle') }}
            </h3>
            <p class="text-sm text-gray-600 dark:text-gray-400 mb-3">
              {{ $t('penny.config.importCsvHelp') }}
            </p>
            <div class="flex gap-3">
              <input
                ref="csvFileInput"
                type="file"
                accept=".csv"
                @change="handleCsvFileSelect"
                class="hidden"
              />
              <button
                @click="$refs.csvFileInput.click()"
                :disabled="importing"
                class="flex-1 px-4 py-2 bg-emerald-600 hover:bg-emerald-700 disabled:bg-emerald-400 text-white rounded-lg font-medium transition-colors flex items-center justify-center gap-2"
              >
                <Icon v-if="importing" icon="mdi:loading" class="animate-spin" />
                <Icon v-else icon="mdi:upload" />
                {{ importing ? $t('penny.config.importing') : $t('penny.config.selectCsvFile') }}
              </button>
              <button
                @click="downloadCsvTemplate"
                class="px-4 py-2 border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 rounded-lg font-medium hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors flex items-center gap-2"
              >
                <Icon icon="mdi:download" />
                {{ $t('penny.config.downloadTemplate') }}
              </button>
            </div>
          </div>

          <!-- Botpress JSON Import -->
          <div class="p-4 border border-gray-200 dark:border-gray-700 rounded-xl bg-gray-50 dark:bg-gray-900/40">
            <h3 class="font-semibold text-gray-900 dark:text-white mb-3 flex items-center gap-2">
              <Icon icon="mdi:robot" class="text-purple-500" />
              {{ $t('penny.config.importBotpressTitle') }}
            </h3>
            <p class="text-sm text-gray-600 dark:text-gray-400 mb-3">
              {{ $t('penny.config.importBotpressHelp') }}
            </p>
            <div class="flex gap-3">
              <input
                ref="botpressFileInput"
                type="file"
                accept=".json"
                @change="handleBotpressFileSelect"
                class="hidden"
              />
              <button
                @click="$refs.botpressFileInput.click()"
                :disabled="importing"
                class="flex-1 px-4 py-2 bg-purple-600 hover:bg-purple-700 disabled:bg-purple-400 text-white rounded-lg font-medium transition-colors flex items-center justify-center gap-2"
              >
                <Icon v-if="importing" icon="mdi:loading" class="animate-spin" />
                <Icon v-else icon="mdi:upload" />
                {{ importing ? $t('penny.config.importing') : $t('penny.config.selectBotpressFile') }}
              </button>
            </div>
          </div>

          <!-- Import Result -->
          <div v-if="importResult" class="p-4 rounded-xl border" :class="importResult.success ? 'bg-emerald-50 border-emerald-200 dark:bg-emerald-900/20 dark:border-emerald-800' : 'bg-red-50 border-red-200 dark:bg-red-900/20 dark:border-red-800'">
            <div class="flex items-start gap-3">
              <Icon :icon="importResult.success ? 'mdi:check-circle' : 'mdi:alert-circle'" :class="importResult.success ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-600 dark:text-red-400'" class="text-xl mt-0.5" />
              <div class="flex-1">
                <h4 class="font-semibold" :class="importResult.success ? 'text-emerald-900 dark:text-emerald-100' : 'text-red-900 dark:text-red-100'">
                  {{ importResult.message }}
                </h4>
                <div v-if="importResult.details" class="mt-2 text-sm" :class="importResult.success ? 'text-emerald-700 dark:text-emerald-300' : 'text-red-700 dark:text-red-300'">
                  <div v-if="importResult.details.importedCount !== undefined">
                    {{ $t('penny.config.importedCount') }}: {{ importResult.details.importedCount }}
                  </div>
                  <div v-if="importResult.details.failedCount !== undefined && importResult.details.failedCount > 0">
                    {{ $t('penny.config.failedCount') }}: {{ importResult.details.failedCount }}
                  </div>
                  <div v-if="importResult.details.errors && importResult.details.errors.length > 0" class="mt-2">
                    <details class="cursor-pointer">
                      <summary class="font-medium">{{ $t('penny.config.viewErrors') }}</summary>
                      <ul class="mt-2 space-y-1 pl-4">
                        <li v-for="(error, idx) in importResult.details.errors.slice(0, 10)" :key="idx" class="text-xs">
                          {{ error }}
                        </li>
                        <li v-if="importResult.details.errors.length > 10" class="text-xs">
                          {{ $t('penny.config.moreErrors', { count: importResult.details.errors.length - 10 }) }}
                        </li>
                      </ul>
                    </details>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Export Tab -->
        <div v-if="activeTab === 'export'" class="space-y-6">
          <div class="p-4 border border-gray-200 dark:border-gray-700 rounded-xl bg-gray-50 dark:bg-gray-900/40">
            <h3 class="font-semibold text-gray-900 dark:text-white mb-3 flex items-center gap-2">
              <Icon icon="mdi:file-download" class="text-indigo-500" />
              {{ $t('penny.config.exportJsonTitle') }}
            </h3>
            <p class="text-sm text-gray-600 dark:text-gray-400 mb-3">
              {{ $t('penny.config.exportJsonHelp') }}
            </p>
            <button
              @click="handleExport"
              :disabled="exporting"
              class="w-full px-4 py-2 bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-400 text-white rounded-lg font-medium transition-colors flex items-center justify-center gap-2"
            >
              <Icon v-if="exporting" icon="mdi:loading" class="animate-spin" />
              <Icon v-else icon="mdi:download" />
              {{ exporting ? $t('penny.config.exporting') : $t('penny.config.exportJson') }}
            </button>
          </div>

          <!-- Export Result -->
          <div v-if="exportResult" class="p-4 rounded-xl bg-emerald-50 border border-emerald-200 dark:bg-emerald-900/20 dark:border-emerald-800">
            <div class="flex items-start gap-3">
              <Icon icon="mdi:check-circle" class="text-emerald-600 dark:text-emerald-400 text-xl mt-0.5" />
              <div class="flex-1">
                <h4 class="font-semibold text-emerald-900 dark:text-emerald-100">
                  {{ exportResult.message }}
                </h4>
                <div v-if="exportResult.details" class="mt-2 text-sm text-emerald-700 dark:text-emerald-300">
                  <div v-if="exportResult.details.rules !== undefined">
                    {{ $t('penny.config.exportedRules') }}: {{ exportResult.details.rules }}
                  </div>
                  <div v-if="exportResult.details.templates !== undefined">
                    {{ $t('penny.config.exportedTemplates') }}: {{ exportResult.details.templates }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Footer -->
      <div class="p-6 border-t border-gray-200 dark:border-gray-700 flex justify-end">
        <button
          @click="close"
          class="px-6 py-2 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-300 rounded-lg font-medium transition-colors"
        >
          {{ $t('common.close') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { pennyApi } from '@/api/pennyApi';
import { Icon } from '@iconify/vue';

export default {
  name: 'ImportExportModal',
  components: { Icon },
  props: {
    show: {
      type: Boolean,
      required: true
    },
    botId: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      activeTab: 'import',
      importing: false,
      exporting: false,
      importResult: null,
      exportResult: null
    };
  },
  methods: {
    close() {
      this.$emit('close');
      this.importResult = null;
      this.exportResult = null;
    },
    
    async handleJsonFileSelect(event) {
      const file = event.target.files[0];
      if (!file) return;
      
      this.importing = true;
      this.importResult = null;
      
      try {
        const content = await this.readFile(file);
        const importData = JSON.parse(content);
        
        // Use full bot config import API for PennyBotConfig page
        const response = await pennyApi.importFullBotConfig(this.botId, importData);
        
        this.importResult = {
          success: true,
          message: response.data.message || $t('penny.config.importSuccessful'),
          details: {
            importedCount: (response.data.importedRules || 0) + (response.data.importedTemplates || 0),
            failedCount: response.data.errors || 0,
            importedRules: response.data.importedRules,
            importedTemplates: response.data.importedTemplates
          }
        };
        
        this.$emit('imported');
      } catch (error) {
        console.error('Import error:', error);
        this.importResult = {
          success: false,
          message: $t('penny.config.importFailed'),
          details: {
            errors: [error.response?.data?.error || error.message]
          }
        };
      } finally {
        this.importing = false;
        event.target.value = '';
      }
    },
    
    async handleCsvFileSelect(event) {
      const file = event.target.files[0];
      if (!file) return;
      
      this.importing = true;
      this.importResult = null;
      
      try {
        const response = await pennyApi.importRulesFromCSV(this.botId, file);
        
        this.importResult = {
          success: true,
          message: response.data.message || $t('penny.config.csvImportCompleted'),
          details: {
            importedCount: response.data.importedCount,
            failedCount: response.data.failedCount,
            errors: response.data.errors || []
          }
        };
        
        this.$emit('imported');
      } catch (error) {
        console.error('CSV import error:', error);
        this.importResult = {
          success: false,
          message: $t('penny.config.csvImportFailed'),
          details: {
            errors: [error.response?.data?.error || error.message]
          }
        };
      } finally {
        this.importing = false;
        event.target.value = '';
      }
    },
    
    async handleBotpressFileSelect(event) {
      const file = event.target.files[0];
      if (!file) return;
      
      this.importing = true;
      this.importResult = null;
      
      try {
        const content = await this.readFile(file);
        const botpressData = JSON.parse(content);
        
        const response = await pennyApi.importFromBotpress(this.botId, botpressData);
        
        this.importResult = {
          success: true,
          message: response.data.message || $t('penny.config.botpressImportCompleted'),
          details: {
            importedCount: response.data.importedRules,
            failedCount: response.data.errorCount || 0,
            errors: response.data.errors || []
          }
        };
        
        this.$emit('imported');
      } catch (error) {
        console.error('Botpress import error:', error);
        this.importResult = {
          success: false,
          message: $t('penny.config.botpressImportFailed'),
          details: {
            errors: [error.response?.data?.error || error.message]
          }
        };
      } finally {
        this.importing = false;
        event.target.value = '';
      }
    },
    
    async handleExport() {
      this.exporting = true;
      this.exportResult = null;
      
      try {
        // Use full bot config export API for PennyBotConfig page
        const response = await pennyApi.exportFullBotConfig(this.botId);
        
        const blob = new Blob([JSON.stringify(response.data, null, 2)], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `penny-full-config-${this.botId}-${Date.now()}.json`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(url);
        
        this.exportResult = {
          success: true,
          message: $t('penny.config.exportSuccessful'),
          details: {
            rules: response.data.rulesCount || 0,
            templates: response.data.templatesCount || 0,
            botConfig: true
          }
        };
      } catch (error) {
        console.error('Export error:', error);
        this.exportResult = {
          success: false,
          message: $t('penny.config.exportFailed'),
          details: {
            errors: [error.response?.data?.error || error.message]
          }
        };
      } finally {
        this.exporting = false;
      }
    },
    
    downloadCsvTemplate() {
      const headers = 'name,description,ruleType,triggerType,triggerValue,condition,action,priority,isActive';
      const example = 'Greeting Rule,Welcome message,RESPONSE,INTENT,greeting,,{"text":"Hello! Welcome to our store!"},0,true';
      const csvContent = `${headers}\n${example}`;
      
      const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'penny-rules-template.csv';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);
    },
    
    readFile(file) {
      return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = (e) => resolve(e.target.result);
        reader.onerror = (e) => reject(e);
        reader.readAsText(file);
      });
    }
  }
};
</script>
