<template>
  <div class="penny-rule-modal-backdrop" @click="closeOnBackdrop">
    <div class="penny-rule-modal" @click.stop>
      <div class="modal-header">
        <div class="header-content">
          <div class="rule-info">
            <Icon icon="mdi:script-text" class="h-6 w-6 mr-3" />
            <div>
              <h2 class="modal-title">{{ isEditMode ? $t('Edit Rule') : $t('Create Rule') }}</h2>
              <p class="bot-name">{{ bot?.botName }}</p>
            </div>
          </div>
          <button @click="$emit('close')" class="close-button">
            <Icon icon="mdi:close" class="h-5 w-5" />
          </button>
        </div>
      </div>

      <div class="modal-body">
        <form @submit.prevent="handleSubmit" class="rule-form">
          <!-- Basic Information -->
          <div class="form-section">
            <h3 class="section-title">{{ $t('Basic Information') }}</h3>
            <div class="form-grid">
              <div class="form-group">
                <label for="ruleName" class="form-label">
                  {{ $t('Rule Name') }} <span class="required">*</span>
                </label>
                <input
                  id="ruleName"
                  v-model="formData.name"
                  type="text"
                  class="form-input"
                  :placeholder="$t('Enter rule name')"
                  required
                />
              </div>
              <div class="form-group">
                <label for="priority" class="form-label">
                  {{ $t('Priority') }} <span class="required">*</span>
                </label>
                <input
                  id="priority"
                  v-model.number="formData.priority"
                  type="number"
                  min="0"
                  max="100"
                  class="form-input"
                  required
                />
                <p class="form-help">{{ $t('Higher priority rules are executed first') }}</p>
              </div>
            </div>
            <div class="form-group full-width">
              <label for="description" class="form-label">
                {{ $t('Description') }}
              </label>
              <textarea
                id="description"
                v-model="formData.description"
                rows="2"
                class="form-textarea"
                :placeholder="$t('Describe what this rule does')"
              ></textarea>
            </div>
          </div>

          <!-- Trigger Configuration -->
          <div class="form-section">
            <h3 class="section-title">{{ $t('Trigger Configuration') }}</h3>
            <div class="form-grid">
              <div class="form-group">
                <label for="triggerType" class="form-label">
                  {{ $t('Trigger Type') }} <span class="required">*</span>
                </label>
                <select
                  id="triggerType"
                  v-model="formData.triggerType"
                  class="form-select"
                  @change="onTriggerTypeChange"
                  required
                >
                  <option value="" disabled>{{ $t('Select trigger type') }}</option>
                  <option
                    v-for="type in triggerTypes"
                    :key="type.value"
                    :value="type.value"
                  >
                    {{ type.label }}
                  </option>
                </select>
              </div>
              <div v-if="formData.triggerType !== 'ALWAYS'" class="form-group">
                <label for="triggerValue" class="form-label">
                  {{ $t('Trigger Value') }} <span class="required">*</span>
                </label>
                <input
                  id="triggerValue"
                  v-model="formData.triggerValue"
                  type="text"
                  class="form-input"
                  :placeholder="getTriggerValuePlaceholder()"
                  required
                />
                <p class="form-help">{{ getTriggerValueHelp() }}</p>
              </div>
            </div>
            
            <!-- Condition (for CONDITION trigger type) -->
            <div v-if="formData.triggerType === 'CONDITION'" class="form-group full-width">
              <label for="condition" class="form-label">
                {{ $t('Condition') }} <span class="required">*</span>
              </label>
              <textarea
                id="condition"
                v-model="formData.condition"
                rows="3"
                class="form-textarea"
                :placeholder="$t('Enter condition expression')"
                required
              ></textarea>
              <p class="form-help">{{ $t('conditionHelp') }}</p>
            </div>
          </div>

          <!-- Rule Configuration -->
          <div class="form-section">
            <h3 class="section-title">{{ $t('Rule Configuration') }}</h3>
            <div class="form-group">
              <label for="ruleType" class="form-label">
                {{ $t('Rule Type') }} <span class="required">*</span>
              </label>
              <select
                id="ruleType"
                v-model="formData.ruleType"
                class="form-select"
                @change="onRuleTypeChange"
                required
              >
                <option value="" disabled>{{ $t('Select rule type') }}</option>
                <option
                  v-for="type in ruleTypes"
                  :key="type.value"
                  :value="type.value"
                >
                  {{ type.label }}
                </option>
              </select>
            </div>
            
            <div class="form-group full-width">
              <label for="action" class="form-label">
                {{ getActionLabel() }} <span class="required">*</span>
              </label>
              <textarea
                id="action"
                v-model="formData.action"
                rows="4"
                class="form-textarea"
                :placeholder="getActionPlaceholder()"
                required
              ></textarea>
              <p class="form-help">{{ getActionHelp() }}</p>
            </div>
          </div>

          <!-- Status (edit mode only) -->
          <div v-if="isEditMode" class="form-section">
            <h3 class="section-title">{{ $t('Status') }}</h3>
            <div class="form-group">
              <label class="checkbox-label">
                <input
                  type="checkbox"
                  v-model="formData.isActive"
                  class="checkbox-input"
                />
                <span class="checkbox-text">{{ formData.isActive ? $t('Active') : $t('Inactive') }}</span>
              </label>
            </div>
          </div>
        </form>
      </div>

      <div class="modal-footer">
        <button
          type="button"
          @click="$emit('close')"
          class="btn btn-secondary"
        >
          {{ $t('Cancel') }}
        </button>
        <button
          type="button"
          @click="testRule"
          :disabled="!canTestRule"
          class="btn btn-outline"
        >
          <Icon icon="mdi:test-tube" class="h-4 w-4 mr-2" />
          {{ $t('Test Rule') }}
        </button>
        <button
          type="submit"
          @click="handleSubmit"
          :disabled="submitting"
          class="btn btn-primary"
        >
          <Icon v-if="submitting" icon="mdi:loading" class="animate-spin h-4 w-4 mr-2" />
          <Icon v-else icon="mdi:plus" class="h-4 w-4 mr-2" />
          {{ isEditMode ? $t('Update Rule') : $t('Create Rule') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import { useI18n } from 'vue-i18n'
import { pennyRuleApi } from '@/api/pennyRuleApi'

export default {
  name: 'PennyRuleModal',
  components: {
    Icon
  },
  props: {
    bot: {
      type: Object,
      required: true
    },
    rule: {
      type: Object,
      default: null
    }
  },
  emits: ['close', 'saved'],
  setup(props, { emit }) {
    const { t } = useI18n()
    const submitting = ref(false)

    const formData = ref({
      name: '',
      description: '',
      condition: '',
      action: '',
      ruleType: 'RESPONSE',
      triggerType: 'INTENT',
      triggerValue: '',
      priority: 0,
      isActive: true
    })

    const triggerTypes = [
      { value: 'INTENT', label: 'Intent Based' },
      { value: 'KEYWORD', label: 'Keyword Based' },
      { value: 'REGEX', label: 'Regex Pattern' },
      { value: 'CONDITION', label: 'Custom Condition' },
      { value: 'ALWAYS', label: 'Always Trigger' }
    ]

    const ruleTypes = [
      { value: 'RESPONSE', label: 'Direct Response' },
      { value: 'REDIRECT', label: 'Redirect to Flow' },
      { value: 'WEBHOOK', label: 'Call Webhook' },
      { value: 'SCRIPT', label: 'Execute Script' }
    ]

    const isEditMode = computed(() => !!props.rule)

    const canTestRule = computed(() => {
      return formData.value.name && 
             formData.value.action && 
             (formData.value.triggerType === 'ALWAYS' || formData.value.triggerValue)
    })

    const onTriggerTypeChange = () => {
      // Reset trigger value when type changes
      if (formData.value.triggerType === 'ALWAYS') {
        formData.value.triggerValue = ''
      }
    }

    const onRuleTypeChange = () => {
      // Reset action when rule type changes
      formData.value.action = ''
    }

    const getTriggerValuePlaceholder = () => {
      const placeholders = {
        'INTENT': 'Enter intent name (e.g., greeting)',
        'KEYWORD': 'Enter keyword (e.g., hello)',
        'REGEX': 'Enter regex pattern (e.g., ^hello.*$)',
        'CONDITION': 'Enter condition expression'
      }
      return placeholders[formData.value.triggerType] || ''
    }

    const getTriggerValueHelp = () => {
      const helps = {
        'INTENT': 'The intent that will trigger this rule',
        'KEYWORD': 'The keyword that will trigger this rule',
        'REGEX': 'The regex pattern that will trigger this rule',
        'CONDITION': 'The custom condition expression'
      }
      return helps[formData.value.triggerType] || ''
    }

    const getActionLabel = () => {
      const labels = {
        'RESPONSE': 'Response',
        'REDIRECT': 'Flow ID',
        'WEBHOOK': 'Webhook URL',
        'SCRIPT': 'Script Code'
      }
      return labels[formData.value.ruleType] || 'Action'
    }

    const getActionPlaceholder = () => {
      const placeholders = {
        'RESPONSE': 'Enter the response message',
        'REDIRECT': 'Enter flow ID to redirect to',
        'WEBHOOK': 'Enter webhook URL',
        'SCRIPT': 'Enter script code to execute'
      }
      return placeholders[formData.value.ruleType] || ''
    }

    const getActionHelp = () => {
      const helps = {
        'RESPONSE': 'The message that will be sent as response',
        'REDIRECT': 'The flow ID to redirect the conversation to',
        'WEBHOOK': 'The webhook URL that will be called',
        'SCRIPT': 'The script code that will be executed'
      }
      return helps[formData.value.ruleType] || ''
    }

    const handleSubmit = async () => {
      if (!formData.value.name || !formData.value.action) {
        return
      }

      submitting.value = true
      try {
        const ruleData = {
          name: formData.value.name,
          description: formData.value.description,
          condition: formData.value.condition,
          action: formData.value.action,
          ruleType: formData.value.ruleType,
          triggerType: formData.value.triggerType,
          triggerValue: formData.value.triggerValue,
          priority: formData.value.priority
        }

        if (isEditMode.value) {
          // Update existing rule
          await pennyRuleApi.updateRule(props.bot.id, props.rule.id, ruleData)
          console.log('Rule updated successfully')
        } else {
          // Create new rule
          await pennyRuleApi.createRule(props.bot.id, ruleData)
          console.log('Rule created successfully')
        }
        
        emit('saved')
      } catch (error) {
        console.error('Failed to save rule:', error)
        alert('Failed to save rule: ' + (error.response?.data?.error || error.message))
      } finally {
        submitting.value = false
      }
    }

    const testRule = async () => {
      if (!canTestRule.value) return
      
      try {
        const testData = {
          intent: formData.value.triggerType === 'INTENT' ? formData.value.triggerValue : 'test_intent',
          message: formData.value.triggerType === 'KEYWORD' || formData.value.triggerType === 'REGEX' 
            ? formData.value.triggerValue 
            : 'test message',
          context: {
            userId: 'test_user',
            platform: 'test'
          }
        }

        let response
        if (isEditMode.value && props.rule) {
          // Test existing rule
          response = await pennyRuleApi.testRule(props.bot.id, props.rule.id, testData)
        } else {
          // For new rules, create a temporary rule for testing
          const tempRuleData = {
            name: formData.value.name,
            description: formData.value.description,
            condition: formData.value.condition,
            action: formData.value.action,
            ruleType: formData.value.ruleType,
            triggerType: formData.value.triggerType,
            triggerValue: formData.value.triggerValue,
            priority: formData.value.priority
          }
          
          // Create rule, test it, then delete it
          const createdRule = await pennyRuleApi.createRule(props.bot.id, tempRuleData)
          response = await pennyRuleApi.testRule(props.bot.id, createdRule.data.ruleId, testData)
          await pennyRuleApi.deleteRule(props.bot.id, createdRule.data.ruleId)
        }

        console.log('Rule test result:', response.data)
        
        if (response.data.matches) {
          alert(`✅ Rule matched!\n\nRule: ${response.data.ruleName}\nTest data: ${JSON.stringify(testData, null, 2)}`)
        } else {
          alert(`❌ Rule did not match.\n\nTest data: ${JSON.stringify(testData, null, 2)}`)
        }
      } catch (error) {
        console.error('Failed to test rule:', error)
        alert('Failed to test rule: ' + (error.response?.data?.error || error.message))
      }
    }

    const closeOnBackdrop = (event) => {
      if (event.target === event.currentTarget) {
        emit('close')
      }
    }

    // Initialize form data
    onMounted(() => {
      if (props.rule) {
        // Edit mode - populate with existing data
        Object.assign(formData.value, props.rule)
      } else {
        // Create mode - set defaults
        formData.value = {
          name: '',
          description: '',
          condition: '',
          action: '',
          ruleType: 'RESPONSE',
          triggerType: 'INTENT',
          triggerValue: '',
          priority: 0,
          isActive: true
        }
      }
    })

    return {
      formData,
      submitting,
      isEditMode,
      canTestRule,
      triggerTypes,
      ruleTypes,
      onTriggerTypeChange,
      onRuleTypeChange,
      getTriggerValuePlaceholder,
      getTriggerValueHelp,
      getActionLabel,
      getActionPlaceholder,
      getActionHelp,
      handleSubmit,
      testRule,
      closeOnBackdrop
    }
  }
}
</script>

<style scoped>
.penny-rule-modal-backdrop {
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

.penny-rule-modal {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  max-width: 700px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  margin-top: 20px;
}

.dark .penny-rule-modal {
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

.rule-info {
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

.rule-form {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.form-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e5e7eb;
}

.form-section:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.section-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.dark .section-title {
  color: white;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group.full-width {
  grid-column: 1 / -1;
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

.form-input,
.form-select,
.form-textarea {
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  background: white;
  color: #1f2937;
  transition: all 0.2s;
}

.dark .form-input,
.dark .form-select,
.dark .form-textarea {
  background: #374151;
  border-color: #4b5563;
  color: white;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.form-help {
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
}

.dark .form-help {
  color: #9ca3af;
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

.btn-outline {
  background: transparent;
  color: #3b82f6;
  border: 1px solid #3b82f6;
}

.btn-outline:hover:not(:disabled) {
  background: #3b82f6;
  color: white;
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
