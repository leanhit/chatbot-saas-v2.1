<template>
  <div class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full z-50">
    <div class="relative min-h-screen flex items-center justify-center p-4">
      <div class="relative bg-white dark:bg-gray-800 rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:w-full sm:max-w-3xl">
        <!-- Header -->
        <div class="bg-white dark:bg-gray-800 px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
          <div class="flex items-center justify-between">
            <h3 class="text-lg leading-6 font-medium text-gray-900 dark:text-white">
              {{ rule ? $t('Edit Rule') : $t('Create Rule') }}
            </h3>
            <button
              @click="$emit('close')"
              class="rounded-md text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              <Icon icon="mdi:close" class="h-6 w-6" />
            </button>
          </div>
        </div>

        <!-- Body -->
        <form @submit.prevent="handleSubmit" class="bg-white dark:bg-gray-800 px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
          <div class="space-y-6">
            <!-- Basic Information -->
            <div>
              <h4 class="text-md font-medium text-gray-900 dark:text-white mb-4">
                {{ $t('Basic Information') }}
              </h4>
              
              <!-- Rule Name -->
              <div class="mb-4">
                <label for="rule-name" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                  {{ $t('Rule Name') }}
                </label>
                <div class="mt-1">
                  <input
                    id="rule-name"
                    v-model="form.name"
                    type="text"
                    required
                    :placeholder="$t('Enter rule name')"
                    class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  />
                </div>
              </div>

              <!-- Priority -->
              <div class="mb-4">
                <label for="rule-priority" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                  {{ $t('Priority') }}
                </label>
                <div class="mt-1">
                  <input
                    id="rule-priority"
                    v-model.number="form.priority"
                    type="number"
                    min="1"
                    max="100"
                    required
                    :placeholder="$t('Enter priority')"
                    class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  />
                  <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
                    {{ $t('Higher priority rules are executed first') }}
                  </p>
                </div>
              </div>

              <!-- Description -->
              <div class="mb-4">
                <label for="rule-description" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                  {{ $t('Description') }}
                </label>
                <div class="mt-1">
                  <textarea
                    id="rule-description"
                    v-model="form.description"
                    rows="3"
                    :placeholder="$t('Describe what this rule does')"
                    class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  />
                </div>
              </div>
            </div>

            <!-- Trigger Configuration -->
            <div>
              <h4 class="text-md font-medium text-gray-900 dark:text-white mb-4">
                {{ $t('Trigger Configuration') }}
              </h4>
              
              <!-- Trigger Type -->
              <div class="mb-4">
                <label for="trigger-type" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                  {{ $t('Trigger Type') }}
                </label>
                <div class="mt-1">
                  <select
                    id="trigger-type"
                    v-model="form.triggerType"
                    required
                    class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  >
                    <option value="">{{ $t('Select trigger type') }}</option>
                    <option value="intent">{{ $t('Intent Based') }}</option>
                    <option value="keyword">{{ $t('Keyword Based') }}</option>
                    <option value="regex">{{ $t('Regex Pattern') }}</option>
                    <option value="custom">{{ $t('Custom Condition') }}</option>
                    <option value="always">{{ $t('Always Trigger') }}</option>
                  </select>
                </div>
              </div>

              <!-- Trigger Value -->
              <div v-if="form.triggerType && form.triggerType !== 'always'" class="mb-4">
                <label for="trigger-value" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                  {{ getTriggerValueLabel() }}
                </label>
                <div class="mt-1">
                  <input
                    id="trigger-value"
                    v-model="form.triggerValue"
                    type="text"
                    :placeholder="getTriggerValuePlaceholder()"
                    class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  />
                  <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
                    {{ getTriggerValueHelp() }}
                  </p>
                </div>
              </div>

              <!-- Condition -->
              <div v-if="form.triggerType === 'custom'" class="mb-4">
                <label for="condition" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                  {{ $t('Condition') }}
                </label>
                <div class="mt-1">
                  <textarea
                    id="condition"
                    v-model="form.condition"
                    rows="3"
                    :placeholder="$t('Enter condition expression')"
                    class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  />
                  <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
                    {{ $t('conditionHelp') }}
                  </p>
                </div>
              </div>
            </div>

            <!-- Rule Configuration -->
            <div>
              <h4 class="text-md font-medium text-gray-900 dark:text-white mb-4">
                {{ $t('Rule Configuration') }}
              </h4>
              
              <!-- Rule Type -->
              <div class="mb-4">
                <label for="rule-type" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                  {{ $t('Rule Type') }}
                </label>
                <div class="mt-1">
                  <select
                    id="rule-type"
                    v-model="form.ruleType"
                    required
                    class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  >
                    <option value="">{{ $t('Select rule type') }}</option>
                    <option value="response">{{ $t('Direct Response') }}</option>
                    <option value="flow">{{ $t('Redirect to Flow') }}</option>
                    <option value="webhook">{{ $t('Call Webhook') }}</option>
                    <option value="script">{{ $t('Execute Script') }}</option>
                  </select>
                </div>
              </div>

              <!-- Action -->
              <div class="mb-4">
                <label for="action" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                  {{ $t('Action') }}
                </label>
                <div class="mt-1">
                  <textarea
                    id="action"
                    v-model="form.action"
                    rows="4"
                    :placeholder="getActionPlaceholder()"
                    class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  />
                </div>
              </div>

              <!-- Status -->
              <div class="mb-4">
                <label class="flex items-center">
                  <input
                    v-model="form.isActive"
                    type="checkbox"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded dark:bg-gray-700 dark:border-gray-600"
                  />
                  <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">
                    {{ $t('Active') }}
                  </span>
                </label>
              </div>
            </div>
          </div>
        </form>

        <!-- Footer -->
        <div class="bg-gray-50 dark:bg-gray-700 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
          <button
            type="button"
            @click="$emit('close')"
            class="w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 text-base font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 dark:bg-gray-800 dark:border-gray-600 dark:text-white dark:hover:bg-gray-700 sm:w-auto sm:ml-3"
          >
            {{ $t('Cancel') }}
          </button>
          <button
            type="submit"
            @click="handleSubmit"
            :disabled="loading"
            class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 text-base font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 sm:w-auto"
          >
            <Icon v-if="loading" icon="mdi:loading" class="animate-spin h-4 w-4 mr-2" />
            {{ rule ? $t('Update Rule') : $t('Create Rule') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Icon } from '@iconify/vue'

const { t } = useI18n()

const props = defineProps({
  rule: {
    type: Object,
    default: null
  },
  botId: {
    type: String,
    required: true
  },
  show: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close', 'saved'])

// State
const loading = ref(false)
const form = ref({
  name: '',
  priority: 1,
  description: '',
  triggerType: '',
  triggerValue: '',
  condition: '',
  ruleType: '',
  action: '',
  isActive: true
})

// Methods
const getTriggerValueLabel = () => {
  const labels = {
    intent: t('Intent'),
    keyword: t('Keyword'),
    regex: t('Regex Pattern'),
    custom: t('Condition')
  }
  return labels[form.value.triggerType] || t('Trigger Value')
}

const getTriggerValuePlaceholder = () => {
  const placeholders = {
    intent: 'e.g., greeting, goodbye, help',
    keyword: 'e.g., price, product, support',
    regex: 'e.g., ^\\d{3}-\\d{3}-\\d{4}$',
    custom: 'e.g., message.includes("urgent") && user.level > 2'
  }
  return placeholders[form.value.triggerType] || t('Enter trigger value')
}

const getTriggerValueHelp = () => {
  const helps = {
    intent: 'Enter the intent name that should trigger this rule',
    keyword: 'Enter keywords separated by commas',
    regex: 'Enter a regular expression pattern',
    custom: 'Enter a custom JavaScript condition'
  }
  return helps[form.value.triggerType] || ''
}

const getActionPlaceholder = () => {
  const placeholders = {
    response: 'Enter the response message',
    flow: 'Enter the flow ID or name',
    webhook: 'Enter the webhook URL',
    script: 'Enter the script code to execute'
  }
  return placeholders[form.value.ruleType] || t('Enter action')
}

const handleSubmit = async () => {
  loading.value = true
  try {
    // TODO: Replace with actual API call
    // const ruleData = {
    //   ...form.value,
    //   id: props.rule?.id
    // }
    // 
    // if (props.rule) {
    //   await pennyApi.updateRule(props.botId, props.rule.id, ruleData)
    // } else {
    //   const response = await pennyApi.createRule(props.botId, ruleData)
    //   ruleData.id = response.data.id
    // }
    
    // Mock API call
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    const savedRule = {
      id: props.rule?.id || Date.now().toString(),
      ...form.value,
      botId: props.botId,
      executions: props.rule?.executions || 0,
      createdAt: props.rule?.createdAt || new Date(),
      updatedAt: new Date()
    }
    
    emit('saved', savedRule)
  } catch (error) {
    console.error('Failed to save rule:', error)
  } finally {
    loading.value = false
  }
}

// Watch for rule prop changes
watch(() => props.rule, (newRule) => {
  if (newRule) {
    form.value = {
      name: newRule.name || '',
      priority: newRule.priority || 1,
      description: newRule.description || '',
      triggerType: newRule.triggerType || '',
      triggerValue: newRule.triggerValue || '',
      condition: newRule.condition || '',
      ruleType: newRule.ruleType || '',
      action: newRule.action || '',
      isActive: newRule.isActive ?? true
    }
  } else {
    form.value = {
      name: '',
      priority: 1,
      description: '',
      triggerType: '',
      triggerValue: '',
      condition: '',
      ruleType: '',
      action: '',
      isActive: true
    }
  }
}, { immediate: true })

// Close modal when ESC key is pressed
onMounted(() => {
  const handleEscape = (e) => {
    if (e.key === 'Escape' && props.show) {
      emit('close')
    }
  }
  document.addEventListener('keydown', handleEscape)
  
  return () => {
    document.removeEventListener('keydown', handleEscape)
  }
})
</script>

<style scoped>
.animate-spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
