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
              <!-- Bot Selection -->
              <div class="form-group full-width">
                <label for="botId" class="form-label">
                  {{ $t('Select Bot') }} <span class="required">*</span>
                </label>
                <select
                  id="botId"
                  v-model="formData.botId"
                  class="form-select"
                  :disabled="!!props.bot"
                  required
                >
                  <option value="" disabled>{{ $t('Select a bot') }}</option>
                  <option
                    v-for="bot in availableBots"
                    :key="bot.id"
                    :value="bot.id"
                  >
                    {{ bot.botName }} - {{ getBotTypeDisplayName(bot.botType) }}
                  </option>
                </select>
                <p v-if="!props.bot" class="form-help">
                  {{ $t('Rules must be linked to a specific bot') }}
                </p>
                <p v-else class="form-help">
                  {{ $t('Rule is linked to: {botName}', { botName: props.bot.botName }) }}
                </p>
              </div>
              
              <div class="form-group full-width">
                <label for="ruleName" class="form-label">
                  {{ $t('Rule Name') }} <span class="required">*</span>
                </label>
                <input
                  id="ruleName"
                  v-model="formData.ruleName"
                  type="text"
                  class="form-input"
                  :placeholder="$t('Enter rule name')"
                  required
                />
              </div>
              
              <div class="form-group">
                <label for="priority" class="form-label">
                  {{ $t('Priority') }}
                </label>
                <select
                  id="priority"
                  v-model.number="formData.priority"
                  class="form-select"
                >
                  <option
                    v-for="level in priorityLevels"
                    :key="level.value"
                    :value="level.value"
                  >
                    {{ level.label }}
                  </option>
                </select>
                <p class="form-help">
                  {{ getPriorityDescription(formData.priority) }}
                </p>
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
              <div v-if="formData.triggerType !== RuleTriggerType.ALWAYS" class="form-group">
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
            <div v-if="formData.triggerType === RuleTriggerType.CONDITION" class="form-group full-width">
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
              <p class="form-help">{{ $t('Example: message.text.includes(\"hello\")') }}</p>
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
import { usePennyBotStore } from '@/stores/pennyBotStore'
import {
  RuleType,
  RuleTriggerType,
  RuleRequest,
  RuleDto
} from '@/types/penny'

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
    const pennyBotStore = usePennyBotStore()
    const submitting = ref(false)
    const availableBots = ref([])

    const formData = ref({
      botId: props.bot?.id || '',
      ruleName: '',
      description: '',
      condition: '',
      action: '',
      ruleType: RuleType.RESPONSE,
      triggerType: RuleTriggerType.INTENT,
      triggerValue: '',
      priority: 50,
      isActive: true
    })

    const triggerTypes = [
      { value: RuleTriggerType.INTENT, label: 'Intent Based' },
      { value: RuleTriggerType.KEYWORD, label: 'Keyword Based' },
      { value: RuleTriggerType.REGEX, label: 'Regex Pattern' },
      { value: RuleTriggerType.CONDITION, label: 'Custom Condition' },
      { value: RuleTriggerType.ALWAYS, label: 'Always Trigger' }
    ]

    const ruleTypes = [
      { value: RuleType.RESPONSE, label: 'Direct Response' },
      { value: RuleType.REDIRECT, label: 'Redirect to Flow' },
      { value: RuleType.WEBHOOK, label: 'Call Webhook' },
      { value: RuleType.SCRIPT, label: 'Execute Script' },
      { value: RuleType.AI, label: 'AI Rule' },
      { value: RuleType.KEYWORD, label: 'Keyword Rule' },
      { value: RuleType.INTENT, label: 'Intent Rule' }
    ]

    const priorityLevels = [
      { value: 90, label: 'Critical (90)', description: 'Highest priority, executed first' },
      { value: 70, label: 'High (70)', description: 'High priority rules' },
      { value: 50, label: 'Medium (50)', description: 'Normal priority rules' },
      { value: 30, label: 'Low (30)', description: 'Low priority rules' },
      { value: 10, label: 'Very Low (10)', description: 'Lowest priority, executed last' }
    ]

    const isEditMode = computed(() => !!props.rule)

    const canTestRule = computed(() => {
      return formData.value.ruleName && 
             formData.value.action && 
             (formData.value.triggerType === RuleTriggerType.ALWAYS || formData.value.triggerValue)
    })

    const onTriggerTypeChange = () => {
      // Reset trigger value when type changes
      if (formData.value.triggerType === RuleTriggerType.ALWAYS) {
        formData.value.triggerValue = ''
      }
    }

    const onRuleTypeChange = () => {
      // Reset action when rule type changes
      formData.value.action = ''
    }

    const getTriggerValuePlaceholder = () => {
      const placeholders = {
        [RuleTriggerType.INTENT]: 'Enter intent name (e.g., greeting)',
        [RuleTriggerType.KEYWORD]: 'Enter keyword (e.g., hello)',
        [RuleTriggerType.REGEX]: 'Enter regex pattern (e.g., ^hello.*$)',
        [RuleTriggerType.CONDITION]: 'Enter condition expression'
      }
      return placeholders[formData.value.triggerType] || ''
    }

    const getTriggerValueHelp = () => {
      const helps = {
        [RuleTriggerType.INTENT]: 'The intent that will trigger this rule',
        [RuleTriggerType.KEYWORD]: 'The keyword that will trigger this rule',
        [RuleTriggerType.REGEX]: 'The regex pattern that will trigger this rule',
        [RuleTriggerType.CONDITION]: 'The condition expression that will trigger this rule'
      }
      return helps[formData.value.triggerType] || ''
    }

    const getBotTypeDisplayName = (botType) => {
      const names = {
        'CUSTOMER_SERVICE': 'Customer Service',
        'SALES': 'Sales',
        'SUPPORT': 'Support',
        'MARKETING': 'Marketing',
        'HR': 'HR',
        'FINANCE': 'Finance',
        'GENERAL': 'General'
      }
      return names[botType] || botType
    }

    const getPriorityDescription = (priority) => {
      const level = priorityLevels.find(l => l.value === priority)
      return level ? level.description : ''
    }

    const loadAvailableBots = async () => {
      try {
        await pennyBotStore.fetchPennyBots()
        availableBots.value = pennyBotStore.pennyBots.filter(bot => bot.isFullyActive())
      } catch (error) {
        console.error('Failed to load bots:', error)
      }
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
      // Validate required fields
      if (!formData.value.botId) {
        alert('Please select a bot for this rule')
        return
      }

      if (!formData.value.ruleName || !formData.value.ruleType || !formData.value.triggerType) {
        alert('Please fill in all required fields')
        return
      }

      if (formData.value.triggerType !== RuleTriggerType.ALWAYS && !formData.value.triggerValue) {
        alert('Please provide trigger value')
        return
      }

      if (!formData.value.action) {
        alert('Please provide rule action')
        return
      }

      submitting.value = true
      try {
        // Create rule request object
        const ruleRequest = new RuleRequest({
          ruleName: formData.value.ruleName,
          ruleType: formData.value.ruleType,
          triggerType: formData.value.triggerType,
          triggerValue: formData.value.triggerValue,
          condition: formData.value.condition,
          action: formData.value.action,
          botId: formData.value.botId,
          description: formData.value.description,
          priority: formData.value.priority,
          config: {
            // Add any additional config here
          }
        })

        // Validate request
        const validation = ruleRequest.validate()
        if (!validation.isValid) {
          alert('Validation errors: ' + validation.errors.join(', '))
          return
        }

        if (props.rule) {
          // Update existing rule
          await pennyRuleApi.updateRule(props.rule.id, ruleRequest.toApiRequest())
        } else {
          // Create new rule
          await pennyRuleApi.createRule(ruleRequest.toApiRequest())
        }
        emit('saved')
      } catch (error) {
        console.error('Failed to save rule:', error)
        alert('Failed to save rule: ' + error.message)
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
            name: formData.value.ruleName,
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

    // Lifecycle
    onMounted(() => {
      loadAvailableBots()
      
      if (props.rule) {
        // Populate form with existing rule data
        Object.assign(formData.value, props.rule)
      } else if (props.bot) {
        // Set bot ID if bot is provided
        formData.value.botId = props.bot.id
      }
    })

    return {
      formData,
      submitting,
      availableBots,
      isEditMode,
      canTestRule,
      triggerTypes,
      ruleTypes,
      priorityLevels,
      onTriggerTypeChange,
      onRuleTypeChange,
      copyToClipboard,
      handleSubmit,
      testRule,
      getBotTypeDisplayName,
      getPriorityDescription,
      getTriggerValuePlaceholder,
      getTriggerValueHelp,
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
