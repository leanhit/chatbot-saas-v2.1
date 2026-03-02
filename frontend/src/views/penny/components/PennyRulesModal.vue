<template>
  <div class="penny-rules-modal-backdrop" @click="closeOnBackdrop">
    <div class="penny-rules-modal" @click.stop>
      <div class="modal-header">
        <div class="header-content">
          <div class="rules-info">
            <Icon icon="mdi:script-text" class="h-6 w-6 mr-3" />
            <div>
              <h2 class="modal-title">{{ $t('Rules Management') }}</h2>
              <p class="bot-name">{{ bot?.botName }}</p>
            </div>
          </div>
          <div class="header-actions">
            <button
              @click="createNewRule"
              class="create-rule-btn"
            >
              <Icon icon="mdi:plus" class="h-4 w-4 mr-2" />
              {{ $t('Create Rule') }}
            </button>
            <button @click="$emit('close')" class="close-button">
              <Icon icon="mdi:close" class="h-5 w-5" />
            </button>
          </div>
        </div>
      </div>

      <div class="modal-body">
        <!-- Rules List -->
        <div v-if="loadingRules" class="loading-rules">
          <div class="animate-pulse">
            <div v-for="i in 3" :key="i" class="rule-skeleton">
              <div class="skeleton-header">
                <div class="skeleton-line long"></div>
                <div class="skeleton-line short"></div>
              </div>
              <div class="skeleton-content">
                <div class="skeleton-line"></div>
                <div class="skeleton-line medium"></div>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="rules.length === 0" class="no-rules">
          <Icon icon="mdi:script-text-outline" class="h-16 w-16 text-gray-400 mb-4" />
          <h3 class="no-rules-title">{{ $t('No Rules Yet') }}</h3>
          <p class="no-rules-text">{{ $t('Create your first rule to get started') }}</p>
          <button @click="createNewRule" class="create-first-rule-btn">
            <Icon icon="mdi:plus" class="h-5 w-5 mr-2" />
            {{ $t('Create Your First Rule') }}
          </button>
        </div>

        <div v-else class="rules-list">
          <!-- Statistics Cards -->
          <div class="stats-cards">
            <div class="stat-card">
              <div class="stat-icon">
                <Icon icon="mdi:script-text" />
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ rules.length }}</div>
                <div class="stat-label">{{ $t('Total Rules') }}</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon active">
                <Icon icon="mdi:check-circle" />
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ activeRules.length }}</div>
                <div class="stat-label">{{ $t('Active Rules') }}</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">
                <Icon icon="mdi:play-circle" />
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ totalExecutions }}</div>
                <div class="stat-label">{{ $t('Total Executions') }}</div>
              </div>
            </div>
          </div>

          <!-- Rules Table -->
          <div class="rules-table">
            <div class="table-header">
              <h3 class="table-title">{{ $t('Rules List') }}</h3>
              <div class="table-actions">
                <select v-model="filterType" class="filter-select">
                  <option value="">{{ $t('All Types') }}</option>
                  <option value="INTENT">{{ $t('Intent Based') }}</option>
                  <option value="KEYWORD">{{ $t('Keyword Based') }}</option>
                  <option value="REGEX">{{ $t('Regex Pattern') }}</option>
                  <option value="CONDITION">{{ $t('Custom Condition') }}</option>
                  <option value="ALWAYS">{{ $t('Always Trigger') }}</option>
                </select>
                <button @click="refreshRules" class="refresh-btn">
                  <Icon icon="mdi:refresh" class="h-4 w-4" />
                </button>
              </div>
            </div>

            <div class="table-container">
              <table class="rules-table-content">
                <thead>
                  <tr>
                    <th>{{ $t('Name') }}</th>
                    <th>{{ $t('Type') }}</th>
                    <th>{{ $t('Trigger') }}</th>
                    <th>{{ $t('Priority') }}</th>
                    <th>{{ $t('Status') }}</th>
                    <th>{{ $t('Executions') }}</th>
                    <th>{{ $t('Actions') }}</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="rule in filteredRules"
                    :key="rule.id"
                    :class="{ 'rule-inactive': !rule.isActive }"
                  >
                    <td class="rule-name-cell">
                      <div class="rule-name">
                        {{ rule.name }}
                      </div>
                      <div class="rule-description" v-if="rule.description">
                        {{ rule.description }}
                      </div>
                    </td>
                    <td>
                      <span class="rule-type-badge" :class="rule.ruleType.toLowerCase()">
                        {{ getRuleTypeDisplayName(rule.ruleType) }}
                      </span>
                    </td>
                    <td>
                      <span class="trigger-badge">
                        {{ getTriggerTypeDisplayName(rule.triggerType) }}
                      </span>
                      <div v-if="rule.triggerValue" class="trigger-value">
                        {{ rule.triggerValue }}
                      </div>
                    </td>
                    <td>
                      <span class="priority-badge" :class="getPriorityClass(rule.priority)">
                        {{ rule.priority }}
                      </span>
                    </td>
                    <td>
                      <div class="status-toggle">
                        <label class="switch">
                          <input
                            type="checkbox"
                            v-model="rule.isActive"
                            @change="toggleRuleStatus(rule)"
                            class="switch-input"
                          />
                          <span class="switch-slider"></span>
                        </label>
                        <span class="status-text" :class="{ active: rule.isActive }">
                          {{ rule.isActive ? $t('Active') : $t('Inactive') }}
                        </span>
                      </div>
                    </td>
                    <td class="executions-cell">
                      <span class="executions-number">{{ rule.executionCount || 0 }}</span>
                    </td>
                    <td class="actions-cell">
                      <div class="action-buttons">
                        <button
                          @click="testRule(rule)"
                          :disabled="testingRule"
                          class="action-btn test"
                          :title="$t('Test Rule')"
                        >
                          <Icon icon="mdi:test-tube" class="h-4 w-4" />
                        </button>
                        <button
                          @click="editRule(rule)"
                          class="action-btn edit"
                          :title="$t('Edit Rule')"
                        >
                          <Icon icon="mdi:pencil" class="h-4 w-4" />
                        </button>
                        <button
                          @click="deleteRuleConfirm(rule)"
                          class="action-btn delete"
                          :title="$t('Delete Rule')"
                        >
                          <Icon icon="mdi:delete" class="h-4 w-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- Rule Modal -->
  <PennyRuleModal
    v-if="showRuleModal"
    :bot="bot"
    :rule="editingRule"
    @close="showRuleModal = false"
    @saved="onRuleSaved"
  />
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { useI18n } from 'vue-i18n'
import { usePennyRuleStore } from '@/stores/pennyRuleStore'
import PennyRuleModal from './PennyRuleModal.vue'

export default {
  name: 'PennyRulesModal',
  components: {
    Icon,
    PennyRuleModal
  },
  props: {
    bot: {
      type: Object,
      required: true
    }
  },
  emits: ['close'],
  setup(props, { emit }) {
    const { t } = useI18n()
    const pennyRuleStore = usePennyRuleStore()

    // State
    const showRuleModal = ref(false)
    const editingRule = ref(null)
    const filterType = ref('')
    const testingRule = ref(false)

    // Computed
    const rules = computed(() => pennyRuleStore.rules)
    const loadingRules = computed(() => pennyRuleStore.loadingRules)
    const activeRules = computed(() => rules.value.filter(rule => rule.isActive))
    const totalExecutions = computed(() => rules.value.reduce((sum, rule) => sum + (rule.executionCount || 0), 0))

    const filteredRules = computed(() => {
      if (!filterType.value) return rules.value
      return rules.value.filter(rule => rule.triggerType === filterType.value)
    })

    // Methods
    const fetchRules = async () => {
      try {
        await pennyRuleStore.fetchRules(props.bot.id)
      } catch (error) {
        console.error('Failed to fetch rules:', error)
      }
    }

    const refreshRules = () => {
      fetchRules()
    }

    const createNewRule = () => {
      editingRule.value = null
      showRuleModal.value = true
    }

    const editRule = (rule) => {
      editingRule.value = rule
      showRuleModal.value = true
    }

    const deleteRuleConfirm = (rule) => {
      if (confirm(`Are you sure you want to delete rule "${rule.name}"?`)) {
        deleteRule(rule)
      }
    }

    const deleteRule = async (rule) => {
      try {
        await pennyRuleStore.deleteRule(props.bot.id, rule.id)
        console.log('Rule deleted successfully')
      } catch (error) {
        console.error('Failed to delete rule:', error)
        alert('Failed to delete rule: ' + error.message)
      }
    }

    const toggleRuleStatus = async (rule) => {
      try {
        await pennyRuleStore.updateRule(props.bot.id, rule.id, {
          isActive: rule.isActive
        })
      } catch (error) {
        console.error('Failed to toggle rule status:', error)
        // Revert the change
        rule.isActive = !rule.isActive
        alert('Failed to update rule status: ' + error.message)
      }
    }

    const testRule = async (rule) => {
      testingRule.value = true
      try {
        const testData = {
          intent: rule.triggerType === 'INTENT' ? rule.triggerValue : 'test_intent',
          message: rule.triggerType === 'KEYWORD' || rule.triggerType === 'REGEX' 
            ? rule.triggerValue 
            : 'test message',
          context: {
            userId: 'test_user',
            platform: 'test'
          }
        }

        const result = await pennyRuleStore.testRule(props.bot.id, rule.id, testData)
        
        if (result.matches) {
          alert(`✅ Rule matched!\n\nRule: ${result.ruleName}\nTest data: ${JSON.stringify(testData, null, 2)}`)
        } else {
          alert(`❌ Rule did not match.\n\nTest data: ${JSON.stringify(testData, null, 2)}`)
        }
      } catch (error) {
        console.error('Failed to test rule:', error)
        alert('Failed to test rule: ' + (error.response?.data?.error || error.message))
      } finally {
        testingRule.value = false
      }
    }

    const getRuleTypeDisplayName = (type) => {
      const names = {
        'RESPONSE': 'Response',
        'REDIRECT': 'Redirect',
        'WEBHOOK': 'Webhook',
        'SCRIPT': 'Script'
      }
      return names[type] || type
    }

    const getTriggerTypeDisplayName = (type) => {
      const names = {
        'INTENT': 'Intent',
        'KEYWORD': 'Keyword',
        'REGEX': 'Regex',
        'CONDITION': 'Condition',
        'ALWAYS': 'Always'
      }
      return names[type] || type
    }

    const getPriorityClass = (priority) => {
      if (priority >= 80) return 'high'
      if (priority >= 50) return 'medium'
      return 'low'
    }

    const closeOnBackdrop = (event) => {
      if (event.target === event.currentTarget) {
        emit('close')
      }
    }

    const onRuleSaved = () => {
      showRuleModal.value = false
      editingRule.value = null
      fetchRules() // Refresh rules list
    }

    // Lifecycle
    onMounted(() => {
      fetchRules()
    })

    return {
      // State
      showRuleModal,
      editingRule,
      filterType,
      testingRule,

      // Computed
      rules,
      loadingRules,
      activeRules,
      totalExecutions,
      filteredRules,

      // Methods
      fetchRules,
      refreshRules,
      createNewRule,
      editRule,
      deleteRuleConfirm,
      deleteRule,
      toggleRuleStatus,
      testRule,
      getRuleTypeDisplayName,
      getTriggerTypeDisplayName,
      getPriorityClass,
      closeOnBackdrop,
      onRuleSaved
    }
  }
}
</script>

<style scoped>
.penny-rules-modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  z-index: 1000;
  padding: 40px 20px 20px;
}

.penny-rules-modal {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  max-width: 1200px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  margin-top: 20px;
}

.dark .penny-rules-modal {
  background: #1f2937;
}

.modal-header {
  padding: 24px;
  border-bottom: 1px solid #e5e7eb;
}

.dark .modal-header {
  border-bottom-color: #374151;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.rules-info {
  display: flex;
  align-items: center;
  flex: 1;
}

.modal-title {
  margin: 0 0 4px 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.dark .modal-title {
  color: white;
}

.bot-name {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
}

.dark .bot-name {
  color: #9ca3af;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.create-rule-btn {
  padding: 8px 16px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
}

.create-rule-btn:hover {
  background: #2563eb;
}

.close-button {
  background: none;
  border: none;
  padding: 8px;
  border-radius: 6px;
  cursor: pointer;
  color: #6b7280;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
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

.loading-rules {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.rule-skeleton {
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.skeleton-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.skeleton-line {
  height: 16px;
  background: #e5e7eb;
  border-radius: 4px;
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.skeleton-line.long {
  width: 60%;
}

.skeleton-line.short {
  width: 30%;
}

.skeleton-line.medium {
  width: 80%;
}

.skeleton-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.no-rules {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.no-rules-title {
  margin: 16px 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #374151;
}

.dark .no-rules-title {
  color: #d1d5db;
}

.no-rules-text {
  margin: 0 0 24px 0;
  color: #6b7280;
}

.dark .no-rules-text {
  color: #9ca3af;
}

.create-first-rule-btn {
  padding: 12px 24px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
}

.create-first-rule-btn:hover {
  background: #2563eb;
}

.rules-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 16px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.dark .stat-card {
  background: #374151;
  border-color: #4b5563;
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #3b82f6;
  color: white;
  margin-right: 12px;
}

.stat-icon.active {
  background: #10b981;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.dark .stat-number {
  color: white;
}

.stat-label {
  font-size: 12px;
  color: #6b7280;
}

.dark .stat-label {
  color: #9ca3af;
}

.rules-table {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.dark .rules-table {
  background: #1f2937;
  border-color: #374151;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
}

.dark .table-header {
  border-bottom-color: #374151;
}

.table-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.dark .table-title {
  color: white;
}

.table-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-select {
  padding: 6px 12px;
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

.refresh-btn {
  padding: 8px;
  background: none;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  cursor: pointer;
  color: #6b7280;
  transition: all 0.2s;
}

.refresh-btn:hover {
  background: #f3f4f6;
  color: #1f2937;
}

.dark .refresh-btn {
  border-color: #4b5563;
  color: #9ca3af;
}

.dark .refresh-btn:hover {
  background: #374151;
  color: white;
}

.table-container {
  overflow-x: auto;
}

.rules-table-content {
  width: 100%;
  border-collapse: collapse;
}

.rules-table-content th {
  padding: 12px 16px;
  text-align: left;
  font-weight: 600;
  color: #374151;
  border-bottom: 1px solid #e5e7eb;
  font-size: 14px;
}

.dark .rules-table-content th {
  color: #d1d5db;
  border-bottom-color: #374151;
}

.rules-table-content td {
  padding: 12px 16px;
  border-bottom: 1px solid #e5e7eb;
  font-size: 14px;
  vertical-align: middle;
}

.dark .rules-table-content td {
  border-bottom-color: #374151;
  color: #d1d5db;
}

.rule-inactive {
  opacity: 0.6;
}

.rule-name-cell {
  max-width: 200px;
}

.rule-name {
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 4px;
}

.dark .rule-name {
  color: white;
}

.rule-description {
  font-size: 12px;
  color: #6b7280;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dark .rule-description {
  color: #9ca3af;
}

.rule-type-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  text-transform: uppercase;
}

.rule-type-badge.response {
  background: #dbeafe;
  color: #1e40af;
}

.rule-type-badge.redirect {
  background: #fef3c7;
  color: #92400e;
}

.rule-type-badge.webhook {
  background: #dcfce7;
  color: #166534;
}

.rule-type-badge.script {
  background: #f3e8ff;
  color: #6b21a8;
}

.trigger-badge {
  display: inline-block;
  padding: 2px 6px;
  background: #e5e7eb;
  border-radius: 4px;
  font-size: 11px;
  color: #374151;
  margin-bottom: 4px;
}

.dark .trigger-badge {
  background: #4b5563;
  color: #d1d5db;
}

.trigger-value {
  font-size: 12px;
  color: #6b7280;
  font-family: monospace;
}

.dark .trigger-value {
  color: #9ca3af;
}

.priority-badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.priority-badge.high {
  background: #fecaca;
  color: #dc2626;
}

.priority-badge.medium {
  background: #fed7aa;
  color: #d97706;
}

.priority-badge.low {
  background: #dbeafe;
  color: #2563eb;
}

.status-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
}

.switch {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 24px;
}

.switch-input {
  opacity: 0;
  width: 0;
  height: 0;
}

.switch-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: 0.4s;
  border-radius: 24px;
}

.switch-slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: 0.4s;
  border-radius: 50%;
}

.switch-input:checked + .switch-slider {
  background-color: #3b82f6;
}

.switch-input:checked + .switch-slider:before {
  transform: translateX(20px);
}

.status-text {
  font-size: 12px;
  font-weight: 500;
}

.status-text.active {
  color: #10b981;
}

.status-text:not(.active) {
  color: #6b7280;
}

.executions-cell {
  text-align: center;
}

.executions-number {
  font-weight: 500;
  color: #1f2937;
}

.dark .executions-number {
  color: white;
}

.actions-cell {
  padding: 8px 16px;
}

.action-buttons {
  display: flex;
  gap: 4px;
}

.action-btn {
  padding: 6px;
  background: none;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-btn:hover {
  background: #f3f4f6;
}

.dark .action-btn {
  border-color: #4b5563;
  color: #9ca3af;
}

.dark .action-btn:hover {
  background: #374151;
}

.action-btn.test:hover {
  border-color: #3b82f6;
  color: #3b82f6;
}

.action-btn.edit:hover {
  border-color: #10b981;
  color: #10b981;
}

.action-btn.delete:hover {
  border-color: #ef4444;
  color: #ef4444;
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
