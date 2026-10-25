<template>
  <div class="changelog-panel">
    <div class="panel-header">
      <h2 class="panel-title">{{ $t('penny.rules.changelog.title') }}</h2>
      <p class="panel-description">{{ $t('penny.rules.changelog.description') }}</p>
    </div>

    <!-- Filters -->
    <div class="filters">
      <div class="filter-group">
        <label class="filter-label">{{ $t('penny.rules.changelog.operationType') }}:</label>
        <select v-model="filters.operationType" class="filter-select">
          <option value="">{{ $t('penny.rules.changelog.all') }}</option>
          <option value="IMPORT">{{ $t('penny.rules.changelog.import') }}</option>
          <option value="EXPORT">{{ $t('penny.rules.changelog.export') }}</option>
        </select>
      </div>
      <div class="filter-group">
        <label class="filter-label">{{ $t('penny.rules.changelog.status') }}:</label>
        <select v-model="filters.status" class="filter-select">
          <option value="">{{ $t('penny.rules.changelog.all') }}</option>
          <option value="SUCCESS">{{ $t('penny.rules.changelog.success') }}</option>
          <option value="FAILED">{{ $t('penny.rules.changelog.failed') }}</option>
          <option value="PARTIAL">{{ $t('penny.rules.changelog.partial') }}</option>
          <option value="DRY_RUN">{{ $t('penny.rules.changelog.dryRun') }}</option>
        </select>
      </div>
      <div class="filter-group">
        <button @click="refreshChangelog" class="btn btn-secondary">
          <Icon icon="mdi:refresh" />
          {{ $t('penny.rules.changelog.refresh') }}
        </button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>{{ $t('penny.rules.changelog.loading') }}</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="!loading && filteredChangelogs.length === 0" class="empty-state">
      <Icon icon="mdi:history" class="empty-icon" />
      <h3 class="empty-title">{{ $t('penny.rules.changelog.noHistory') }}</h3>
      <p class="empty-description">{{ $t('penny.rules.changelog.noHistoryDesc') }}</p>
    </div>

    <!-- Changelog Table -->
    <div v-else class="changelog-table">
      <table class="table">
        <thead>
          <tr>
            <th>{{ $t('penny.rules.changelog.date') }}</th>
            <th>{{ $t('penny.rules.changelog.operation') }}</th>
            <th>{{ $t('penny.rules.changelog.source') }}</th>
            <th>{{ $t('penny.rules.changelog.strategy') }}</th>
            <th>{{ $t('penny.rules.changelog.statusLabel') }}</th>
            <th>{{ $t('penny.rules.changelog.items') }}</th>
            <th>Imported/Updated</th>
            <th>{{ $t('penny.rules.changelog.errors') }}</th>
            <th>{{ $t('penny.rules.changelog.performer') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="entry in filteredChangelogs" :key="entry.id">
            <td class="date-cell">
              {{ formatDate(entry.performedAt) }}
              <span class="time">{{ formatTime(entry.performedAt) }}</span>
            </td>
            <td>
              <span :class="['operation-badge', entry.operationType.toLowerCase()]">
                {{ entry.operationType }}
              </span>
            </td>
            <td>{{ entry.sourceType }}</td>
            <td>{{ entry.strategy || '-' }}</td>
            <td>
              <span :class="['status-badge', entry.status.toLowerCase().replace('_', '-')]">
                {{ entry.status }}
              </span>
            </td>
            <td>{{ entry.itemCount || 0 }}</td>
            <td>
              <span class="counts">
                <span class="imported">{{ entry.importedCount || 0 }}</span> /
                <span class="updated">{{ entry.updatedCount || 0 }}</span>
              </span>
            </td>
            <td>
              <span v-if="entry.errorCount > 0" class="error-count">
                {{ entry.errorCount }}
              </span>
              <span v-else class="no-error">0</span>
            </td>
            <td>{{ entry.performedBy }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Error Details Modal -->
    <div v-if="selectedEntry" class="error-modal-backdrop" @click="closeErrorModal">
      <div class="error-modal" @click.stop>
        <div class="error-modal-header">
          <h3 class="error-modal-title">Error Details</h3>
          <button @click="closeErrorModal" class="close-button">
            <Icon icon="mdi:close" />
          </button>
        </div>
        <div class="error-modal-body">
          <p class="error-message">{{ selectedEntry.errorMessage }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { Icon } from '@iconify/vue'
import { pennyApi } from '@/api/pennyApi'
import { useToast } from '@/composables/useToast'

export default {
  name: 'ChangelogPanel',
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
    const loading = ref(false)
    const changelogs = ref([])
    const selectedEntry = ref(null)

    const filters = ref({
      operationType: '',
      status: ''
    })

    const filteredChangelogs = computed(() => {
      return changelogs.value.filter(entry => {
        if (filters.value.operationType && entry.operationType !== filters.value.operationType) {
          return false
        }
        if (filters.value.status && entry.status !== filters.value.status) {
          return false
        }
        return true
      })
    })

    const fetchChangelog = async () => {
      loading.value = true
      try {
        const response = await pennyApi.getChangelog(props.bot.id)
        changelogs.value = response.data.changelogs || []
      } catch (error) {
        console.error('Failed to fetch changelog:', error)
        toast.error(error, 'Failed to load changelog')
      } finally {
        loading.value = false
      }
    }

    const refreshChangelog = () => {
      fetchChangelog()
    }

    const formatDate = (dateString) => {
      if (!dateString) return '-'
      const date = new Date(dateString)
      return date.toLocaleDateString()
    }

    const formatTime = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleTimeString()
    }

    const closeErrorModal = () => {
      selectedEntry.value = null
    }

    // Watch for bot changes
    watch(() => props.bot.id, () => {
      if (props.bot.id) {
        fetchChangelog()
      }
    }, { immediate: true })

    onMounted(() => {
      if (props.bot.id) {
        fetchChangelog()
      }
    })

    return {
      loading,
      changelogs,
      filteredChangelogs,
      filters,
      selectedEntry,
      refreshChangelog,
      formatDate,
      formatTime,
      closeErrorModal
    }
  }
}
</script>

<style scoped>
.changelog-panel {
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

.filters {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  align-items: flex-end;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.filter-label {
  font-size: 13px;
  font-weight: 500;
  color: #6b7280;
}

.dark .filter-label {
  color: #9ca3af;
}

.filter-select {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  background: white;
  color: #1f2937;
}

.dark .filter-select {
  background: #374151;
  border-color: #4b5563;
  color: white;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
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

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  gap: 16px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #e5e7eb;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-state p {
  color: #6b7280;
  margin: 0;
}

.dark .loading-state p {
  color: #9ca3af;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
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

.changelog-table {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.dark .changelog-table {
  background: #1f2937;
  border-color: #374151;
}

.table {
  width: 100%;
  border-collapse: collapse;
}

.table th {
  padding: 12px 16px;
  text-align: left;
  font-weight: 600;
  color: #374151;
  border-bottom: 1px solid #e5e7eb;
  font-size: 13px;
  background: #f9fafb;
}

.dark .table th {
  color: #d1d5db;
  border-bottom-color: #374151;
  background: #374151;
}

.table td {
  padding: 12px 16px;
  border-bottom: 1px solid #e5e7eb;
  font-size: 13px;
  color: #374151;
}

.dark .table td {
  border-bottom-color: #374151;
  color: #d1d5db;
}

.date-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.time {
  font-size: 11px;
  color: #9ca3af;
}

.operation-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
  text-transform: uppercase;
}

.operation-badge.import {
  background: #d1fae5;
  color: #065f46;
}

.operation-badge.export {
  background: #dbeafe;
  color: #1e40af;
}

.status-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
  text-transform: uppercase;
}

.status-badge.success {
  background: #d1fae5;
  color: #065f46;
}

.status-badge.failed {
  background: #fee2e2;
  color: #991b1b;
}

.status-badge.partial {
  background: #fef3c7;
  color: #92400e;
}

.status-badge.dry-run {
  background: #e0e7ff;
  color: #3730a3;
}

.counts {
  display: flex;
  gap: 4px;
}

.imported {
  color: #059669;
  font-weight: 500;
}

.updated {
  color: #d97706;
  font-weight: 500;
}

.error-count {
  color: #dc2626;
  font-weight: 600;
  cursor: pointer;
}

.error-count:hover {
  text-decoration: underline;
}

.no-error {
  color: #9ca3af;
}

.error-modal-backdrop {
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

.error-modal {
  background: white;
  border-radius: 12px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
  max-width: 500px;
  width: 100%;
}

.dark .error-modal {
  background: #1f2937;
}

.error-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  border-bottom: 1px solid #e5e7eb;
}

.dark .error-modal-header {
  border-bottom-color: #374151;
}

.error-modal-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.dark .error-modal-title {
  color: white;
}

.close-button {
  background: none;
  border: none;
  padding: 8px;
  border-radius: 6px;
  cursor: pointer;
  color: #6b7280;
}

.close-button:hover {
  background: #f3f4f6;
  color: #1f2937;
}

.dark .close-button {
  color: #9ca3af;
}

.dark .close-button:hover {
  background: #374151;
  color: white;
}

.error-modal-body {
  padding: 20px;
}

.error-message {
  font-size: 14px;
  color: #374151;
  white-space: pre-wrap;
  margin: 0;
}

.dark .error-message {
  color: #d1d5db;
}
</style>
