<template>
  <div class="condition-node">
    <!-- Group Node -->
    <div v-if="node.type === 'group'" class="condition-group">
      <div class="group-header">
        <select
          v-model="node.operator"
          class="operator-select"
          @change="$emit('update:modelValue', node)"
        >
          <option value="AND">{{ t('penny.rules.conditionBuilder.and') }}</option>
          <option value="OR">{{ t('penny.rules.conditionBuilder.or') }}</option>
        </select>
        <button
          v-if="showRemove"
          @click="$emit('remove')"
          class="remove-btn"
          :title="t('penny.rules.conditionBuilder.remove')"
        >
          <Icon icon="mdi:close" class="h-4 w-4" />
        </button>
      </div>
      <div class="group-conditions">
        <ConditionNode
          v-for="(condition, index) in node.conditions"
          :key="index"
          v-model="node.conditions[index]"
          :available-variables="availableVariables"
          :show-remove="node.conditions.length > 1"
          @remove="removeCondition(index)"
        />
      </div>
    </div>

    <!-- Condition Node -->
    <div v-else class="condition-item">
      <div class="condition-row">
        <select
          v-model="node.field"
          class="field-select"
          @change="$emit('update:modelValue', node)"
        >
          <option value="">{{ t('penny.rules.conditionBuilder.selectField') }}</option>
          <option
            v-for="variable in availableVariables"
            :key="variable.name"
            :value="variable.name"
          >
            {{ variable.label }}
          </option>
        </select>

        <select
          v-model="node.operator"
          class="operator-select"
          @change="$emit('update:modelValue', node)"
        >
          <option value="equals">equals</option>
          <option value="notEquals">not equals</option>
          <option value="contains">contains</option>
          <option value="notContains">not contains</option>
          <option value="startsWith">starts with</option>
          <option value="endsWith">ends with</option>
          <option value="greaterThan">greater than</option>
          <option value="lessThan">less than</option>
          <option value="greaterThanOrEqual">greater or equal</option>
          <option value="lessThanOrEqual">less or equal</option>
          <option value="matches">matches (regex)</option>
          <option value="in">in</option>
        </select>

        <input
          v-model="node.value"
          type="text"
          class="value-input"
          :placeholder="t('penny.rules.conditionBuilder.value')"
          @input="$emit('update:modelValue', node)"
        />

        <button
          v-if="showRemove"
          @click="$emit('remove')"
          class="remove-btn"
          :title="t('penny.rules.conditionBuilder.remove')"
        >
          <Icon icon="mdi:close" class="h-4 w-4" />
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { Icon } from '@iconify/vue'
import { useI18n } from 'vue-i18n'

export default {
  name: 'ConditionNode',
  components: {
    Icon
  },
  props: {
    modelValue: {
      type: Object,
      required: true
    },
    availableVariables: {
      type: Array,
      default: () => []
    },
    showRemove: {
      type: Boolean,
      default: true
    }
  },
  emits: ['update:modelValue', 'remove'],
  setup(props, { emit }) {
    const { t } = useI18n()
    const node = props.modelValue

    const removeCondition = (index) => {
      if (props.modelValue.type === 'group') {
        props.modelValue.conditions.splice(index, 1)
        emit('update:modelValue', props.modelValue)
      }
    }

    return {
      node,
      removeCondition
    }
  }
}
</script>

<style scoped>
.condition-node {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.condition-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  margin-left: 16px;
}

.dark .condition-group {
  background: #374151;
  border-color: #4b5563;
}

.group-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.group-conditions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-left: 8px;
  border-left: 2px solid #e5e7eb;
}

.dark .group-conditions {
  border-left-color: #4b5563;
}

.condition-item {
  display: flex;
  flex-direction: column;
}

.condition-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.field-select,
.operator-select,
.value-input {
  padding: 6px 10px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 12px;
  background: white;
  color: #1f2937;
  transition: all 0.2s;
}

.dark .field-select,
.dark .operator-select,
.dark .value-input {
  background: #374151;
  border-color: #4b5563;
  color: white;
}

.field-select:focus,
.operator-select:focus,
.value-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.field-select {
  min-width: 140px;
}

.operator-select {
  min-width: 120px;
}

.value-input {
  flex: 1;
  min-width: 120px;
}

.remove-btn {
  background: none;
  border: none;
  padding: 4px;
  border-radius: 4px;
  cursor: pointer;
  color: #ef4444;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.remove-btn:hover {
  background: #fef2f2;
}

.dark .remove-btn:hover {
  background: #7f1d1d;
}
</style>
