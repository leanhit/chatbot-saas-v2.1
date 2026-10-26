<template>
  <div class="condition-builder-container">
    <div class="condition-header">
      <label class="form-label">{{ t('penny.rules.conditionBuilder.title') }}</label>
      <div class="header-actions">
        <button @click="addCondition" class="btn-add">
          <Icon icon="mdi:plus" class="h-4 w-4 mr-1" />
          {{ t('penny.rules.conditionBuilder.addCondition') }}
        </button>
        <button @click="addGroup" class="btn-add">
          <Icon icon="mdi:folder-plus" class="h-4 w-4 mr-1" />
          {{ t('penny.rules.conditionBuilder.addGroup') }}
        </button>
      </div>
    </div>

    <div class="condition-tree">
      <ConditionNode
        v-model="conditionData"
        :available-variables="availableVariables"
        @remove="handleRemove"
      />
    </div>

    <div class="condition-preview">
      <label class="form-label">{{ t('penny.rules.conditionBuilder.expressionPreview') }}</label>
      <div class="expression-output">
        <code>{{ generatedExpression }}</code>
        <button @click="copyExpression" class="copy-btn" title="Copy expression">
          <Icon icon="mdi:content-copy" class="h-4 w-4" />
        </button>
      </div>
    </div>

    <div class="condition-examples">
      <label class="form-label">{{ t('penny.rules.conditionBuilder.quickTemplates') }}</label>
      <div class="examples-grid">
        <button
          v-for="example in conditionTemplates"
          :key="example.name"
          @click="applyTemplate(example)"
          class="example-btn"
        >
          <Icon :icon="example.icon" class="h-4 w-4 mr-2" />
          {{ example.name }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { Icon } from '@iconify/vue'
import { useToast } from '@/composables/useToast'
import ConditionNode from './ConditionNode.vue'

export default {
  name: 'ConditionBuilder',
  components: {
    Icon,
    ConditionNode
  },
  props: {
    modelValue: {
      type: [String, Object],
      default: ''
    }
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    const { t } = useI18n()
    const toast = useToast()
    const conditionData = ref({
      type: 'group',
      operator: 'AND',
      conditions: [
        {
          type: 'condition',
          field: '',
          operator: 'equals',
          value: ''
        }
      ]
    })

    const availableVariables = [
      { name: 'message', label: 'Message Text', type: 'string' },
      { name: 'intent', label: 'Intent Name', type: 'string' },
      { name: 'userId', label: 'User ID', type: 'string' },
      { name: 'userName', label: 'User Name', type: 'string' },
      { name: 'platform', label: 'Platform', type: 'string' },
      { name: 'time', label: 'Current Time', type: 'datetime' },
      { name: 'dayOfWeek', label: 'Day of Week', type: 'number' },
      { name: 'conversationCount', label: 'Conversation Count', type: 'number' }
    ]

    const conditionTemplates = [
      {
        name: 'Message Contains',
        icon: 'mdi:message-text',
        data: {
          type: 'condition',
          field: 'message',
          operator: 'contains',
          value: 'keyword'
        }
      },
      {
        name: 'Specific Intent',
        icon: 'mdi:brain',
        data: {
          type: 'condition',
          field: 'intent',
          operator: 'equals',
          value: 'greeting'
        }
      },
      {
        name: 'Business Hours',
        icon: 'mdi:clock',
        data: {
          type: 'group',
          operator: 'AND',
          conditions: [
            {
              type: 'condition',
              field: 'dayOfWeek',
              operator: 'greaterThanOrEqual',
              value: '1'
            },
            {
              type: 'condition',
              field: 'dayOfWeek',
              operator: 'lessThanOrEqual',
              value: '5'
            },
            {
              type: 'condition',
              field: 'time',
              operator: 'greaterThanOrEqual',
              value: '09:00'
            },
            {
              type: 'condition',
              field: 'time',
              operator: 'lessThanOrEqual',
              value: '17:00'
            }
          ]
        }
      },
      {
        name: 'New User',
        icon: 'mdi:account-plus',
        data: {
          type: 'condition',
          field: 'conversationCount',
          operator: 'equals',
          value: '1'
        }
      },
      {
        name: 'Platform Filter',
        icon: 'mdi:web',
        data: {
          type: 'condition',
          field: 'platform',
          operator: 'equals',
          value: 'facebook'
        }
      }
    ]

    const generatedExpression = computed(() => {
      return generateExpression(conditionData.value)
    })

    const generateExpression = (node) => {
      if (node.type === 'condition') {
        const operatorMap = {
          'equals': '==',
          'notEquals': '!=',
          'contains': 'contains',
          'notContains': '!contains',
          'startsWith': 'startsWith',
          'endsWith': 'endsWith',
          'greaterThan': '>',
          'lessThan': '<',
          'greaterThanOrEqual': '>=',
          'lessThanOrEqual': '<=',
          'matches': 'matches',
          'in': 'in'
        }
        const op = operatorMap[node.operator] || node.operator
        return `${node.field} ${op} "${node.value}"`
      } else if (node.type === 'group') {
        const conditions = node.conditions.map(c => generateExpression(c))
        const op = node.operator === 'AND' ? '&&' : '||'
        return `(${conditions.join(` ${op} `)})`
      }
      return ''
    }

    const addCondition = () => {
      if (conditionData.value.type === 'group') {
        conditionData.value.conditions.push({
          type: 'condition',
          field: '',
          operator: 'equals',
          value: ''
        })
      }
    }

    const addGroup = () => {
      if (conditionData.value.type === 'group') {
        conditionData.value.conditions.push({
          type: 'group',
          operator: 'AND',
          conditions: [
            {
              type: 'condition',
              field: '',
              operator: 'equals',
              value: ''
            }
          ]
        })
      }
    }

    const handleRemove = (path) => {
      // Handle removal of nodes
      // This would need to be implemented with a path-based removal
    }

    const applyTemplate = (template) => {
      conditionData.value = JSON.parse(JSON.stringify(template.data))
    }

    const copyExpression = () => {
      navigator.clipboard.writeText(generatedExpression.value).then(() => {
        toast.success(t('penny.rules.conditionBuilder.copied'))
      }).catch(() => {
        toast.error(t('penny.rules.conditionBuilder.copyFailed'))
      })
    }

    watch(conditionData, (newValue) => {
      emit('update:modelValue', generatedExpression.value)
    }, { deep: true })

    return {
      conditionData,
      availableVariables,
      conditionTemplates,
      generatedExpression,
      addCondition,
      addGroup,
      handleRemove,
      applyTemplate,
      copyExpression
    }
  }
}
</script>

<style scoped>
.condition-builder-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.dark .condition-builder-container {
  background: #1f2937;
  border-color: #374151;
}

.condition-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.form-label {
  font-size: 13px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 6px;
  display: block;
}

.dark .form-label {
  color: #d1d5db;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.btn-add {
  padding: 6px 12px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
}

.btn-add:hover {
  background: #2563eb;
}

.condition-tree {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.condition-preview {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.expression-output {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
  color: #059669;
}

.dark .expression-output {
  background: #374151;
  border-color: #4b5563;
  color: #34d399;
}

.expression-output code {
  flex: 1;
  word-break: break-all;
}

.copy-btn {
  background: none;
  border: none;
  padding: 4px;
  border-radius: 4px;
  cursor: pointer;
  color: #6b7280;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.copy-btn:hover {
  background: #f3f4f6;
  color: #3b82f6;
}

.dark .copy-btn {
  color: #9ca3af;
}

.dark .copy-btn:hover {
  background: #4b5563;
  color: white;
}

.condition-examples {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.examples-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 8px;
}

.example-btn {
  padding: 10px 12px;
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 12px;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dark .example-btn {
  background: #374151;
  border-color: #4b5563;
  color: #d1d5db;
}

.example-btn:hover {
  background: #3b82f6;
  border-color: #3b82f6;
  color: white;
}
</style>
