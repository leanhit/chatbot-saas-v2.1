<template>
  <div class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full z-50">
    <div class="relative min-h-screen flex items-center justify-center p-4">
      <div class="relative bg-white dark:bg-gray-800 rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:w-full sm:max-w-5xl">
        <!-- Header -->
        <div class="bg-white dark:bg-gray-800 px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
          <div class="flex items-center justify-between">
            <h3 class="text-lg leading-6 font-medium text-gray-900 dark:text-white">
              {{ $t('Rules Management') }} - {{ bot.name }}
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
        <div class="bg-white dark:bg-gray-800 px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
          <!-- Stats Cards -->
          <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
              <div class="flex items-center">
                <div class="flex-shrink-0">
                  <Icon icon="mdi:book-open-variant" class="h-8 w-8 text-blue-600 dark:text-blue-400" />
                </div>
                <div class="ml-4">
                  <p class="text-sm font-medium text-gray-500 dark:text-gray-400">
                    {{ $t('Total Rules') }}
                  </p>
                  <p class="text-2xl font-semibold text-gray-900 dark:text-white">
                    {{ stats.total }}
                  </p>
                </div>
              </div>
            </div>

            <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
              <div class="flex items-center">
                <div class="flex-shrink-0">
                  <Icon icon="mdi:check-circle" class="h-8 w-8 text-green-600 dark:text-green-400" />
                </div>
                <div class="ml-4">
                  <p class="text-sm font-medium text-gray-500 dark:text-gray-400">
                    {{ $t('Active Rules') }}
                  </p>
                  <p class="text-2xl font-semibold text-gray-900 dark:text-white">
                    {{ stats.active }}
                  </p>
                </div>
              </div>
            </div>

            <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
              <div class="flex items-center">
                <div class="flex-shrink-0">
                  <Icon icon="mdi:play-circle" class="h-8 w-8 text-purple-600 dark:text-purple-400" />
                </div>
                <div class="ml-4">
                  <p class="text-sm font-medium text-gray-500 dark:text-gray-400">
                    {{ $t('Total Executions') }}
                  </p>
                  <p class="text-2xl font-semibold text-gray-900 dark:text-white">
                    {{ stats.executions.toLocaleString() }}
                  </p>
                </div>
              </div>
            </div>
          </div>

          <!-- Search and Filter -->
          <div class="bg-white dark:bg-gray-800 shadow rounded-lg mb-6">
            <div class="p-4">
              <div class="flex flex-col sm:flex-row gap-4">
                <div class="flex-1">
                  <input
                    v-model="searchQuery"
                    type="text"
                    :placeholder="$t('Search rules...')"
                    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  />
                </div>
                <select
                  v-model="typeFilter"
                  class="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                >
                  <option value="">{{ $t('All Types') }}</option>
                  <option value="intent">{{ $t('Intent Based') }}</option>
                  <option value="keyword">{{ $t('Keyword Based') }}</option>
                  <option value="regex">{{ $t('Regex Pattern') }}</option>
                  <option value="custom">{{ $t('Custom Condition') }}</option>
                  <option value="always">{{ $t('Always Trigger') }}</option>
                </select>
              </div>
            </div>
          </div>

          <!-- Rules List -->
          <div class="bg-white dark:bg-gray-800 shadow rounded-lg">
            <div class="px-4 py-5 sm:px-6">
              <div class="flex items-center justify-between">
                <h3 class="text-lg leading-6 font-medium text-gray-900 dark:text-white">
                  {{ $t('Rules List') }}
                </h3>
                <button
                  @click="showCreateModal = true"
                  class="inline-flex items-center px-3 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                >
                  <Icon icon="mdi:plus" class="h-4 w-4 mr-2" />
                  {{ $t('Create Rule') }}
                </button>
              </div>
            </div>
            <div class="border-t border-gray-200 dark:border-gray-700">
              <div v-if="loading" class="px-4 py-8 text-center">
                <div class="inline-flex items-center">
                  <Icon icon="mdi:loading" class="animate-spin h-5 w-5 mr-3" />
                  {{ $t('Loading...') }}
                </div>
              </div>
              
              <div v-else-if="filteredRules.length === 0" class="px-4 py-8 text-center">
                <Icon icon="mdi:book-off" class="h-12 w-12 text-gray-400 mx-auto mb-4" />
                <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">
                  {{ $t('No Rules Yet') }}
                </h3>
                <p class="text-gray-500 dark:text-gray-400">
                  {{ $t('Create Your First Rule') }}
                </p>
                <div class="mt-6">
                  <button
                    @click="showCreateModal = true"
                    class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
                  >
                    <Icon icon="mdi:plus" class="mr-2" />
                    {{ $t('Create First Rule') }}
                  </button>
                </div>
              </div>

              <div v-else class="divide-y divide-gray-200 dark:divide-gray-700">
                <div
                  v-for="rule in filteredRules"
                  :key="rule.id"
                  class="px-4 py-4 sm:px-6 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
                >
                  <div class="flex items-center justify-between">
                    <div class="flex items-center">
                      <div class="flex-shrink-0 h-10 w-10">
                        <div class="h-10 w-10 rounded-full bg-blue-100 dark:bg-blue-900 flex items-center justify-center">
                          <Icon :icon="getRuleIcon(rule.type)" class="h-6 w-6 text-blue-600 dark:text-blue-400" />
                        </div>
                      </div>
                      <div class="ml-4">
                        <div class="flex items-center">
                          <h4 class="text-lg font-medium text-gray-900 dark:text-white">
                            {{ rule.name }}
                          </h4>
                          <span
                            :class="[
                              'ml-2 inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
                              rule.isActive ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200' : 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
                            ]"
                          >
                            {{ rule.isActive ? $t('Active') : $t('Inactive') }}
                          </span>
                        </div>
                        <p class="text-sm text-gray-500 dark:text-gray-400">
                          {{ getRuleTypeLabel(rule.type) }} • {{ $t('Priority') }}: {{ rule.priority }}
                        </p>
                        <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">
                          {{ rule.description }}
                        </p>
                        <div class="mt-2 flex items-center text-sm text-gray-500 dark:text-gray-400">
                          <Icon icon="mdi:play-circle" class="h-4 w-4 mr-1" />
                          {{ $t('Executions') }}: {{ rule.executions.toLocaleString() }}
                        </div>
                      </div>
                    </div>

                    <div class="flex items-center space-x-2">
                      <button
                        @click="testRule(rule)"
                        class="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white dark:hover:bg-gray-600"
                      >
                        <Icon icon="mdi:test-tube" class="h-4 w-4 mr-1" />
                        {{ $t('Test Rule') }}
                      </button>

                      <div class="relative">
                        <button
                          @click.stop="toggleDropdown(rule.id)"
                          class="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white dark:hover:bg-gray-600"
                        >
                          <Icon icon="mdi:dots-vertical" class="h-4 w-4" />
                        </button>

                        <!-- Dropdown Menu -->
                        <div
                          v-if="dropdownOpen === rule.id"
                          class="absolute right-0 z-10 mt-2 w-48 rounded-md shadow-lg bg-white dark:bg-gray-800 ring-1 ring-black ring-opacity-5"
                        >
                          <div class="py-1">
                            <button
                              @click.stop="editRule(rule)"
                              class="flex items-center w-full px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-gray-700"
                            >
                              <Icon icon="mdi:pencil" class="h-4 w-4 mr-2" />
                              {{ $t('Edit Rule') }}
                            </button>
                            <button
                              @click.stop="toggleRuleStatus(rule)"
                              class="flex items-center w-full px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-gray-700"
                            >
                              <Icon :icon="rule.isActive ? 'mdi:pause' : 'mdi:play'" class="h-4 w-4 mr-2" />
                              {{ rule.isActive ? $t('Disable') : $t('Enable') }}
                            </button>
                            <button
                              @click.stop="deleteRule(rule)"
                              class="flex items-center w-full px-4 py-2 text-sm text-red-600 hover:bg-gray-100 dark:text-red-400 dark:hover:bg-gray-700"
                            >
                              <Icon icon="mdi:delete" class="h-4 w-4 mr-2" />
                              {{ $t('Delete') }}
                            </button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Create/Edit Rule Modal -->
    <PennyRuleModal
      v-if="showCreateModal"
      :rule="editingRule"
      :bot-id="bot.id"
      :show="showCreateModal"
      @close="closeRuleModal"
      @saved="handleRuleSaved"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { Icon } from '@iconify/vue'
import PennyRuleModal from './PennyRuleModal.vue'

const { t } = useI18n()

const props = defineProps({
  bot: {
    type: Object,
    required: true
  },
  show: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close'])

// State
const rules = ref([])
const loading = ref(false)
const searchQuery = ref('')
const typeFilter = ref('')
const showCreateModal = ref(false)
const editingRule = ref(null)
const dropdownOpen = ref(null)

// Mock data
const mockRules = [
  {
    id: '1',
    name: 'Greeting Rule',
    type: 'intent',
    description: 'Handles initial greetings and welcome messages',
    priority: 1,
    isActive: true,
    executions: 15234
  },
  {
    id: '2',
    name: 'Product Information',
    type: 'keyword',
    description: 'Provides information about products and services',
    priority: 2,
    isActive: true,
    executions: 8921
  },
  {
    id: '3',
    name: 'Support Escalation',
    type: 'custom',
    description: 'Escalates complex issues to human support',
    priority: 10,
    isActive: false,
    executions: 567
  }
]

// Computed
const stats = computed(() => ({
  total: rules.value.length,
  active: rules.value.filter(rule => rule.isActive).length,
  executions: rules.value.reduce((sum, rule) => sum + rule.executions, 0)
}))

const filteredRules = computed(() => {
  let filtered = rules.value

  if (searchQuery.value) {
    filtered = filtered.filter(rule => 
      rule.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      rule.description.toLowerCase().includes(searchQuery.value.toLowerCase())
    )
  }

  if (typeFilter.value) {
    filtered = filtered.filter(rule => rule.type === typeFilter.value)
  }

  return filtered.sort((a, b) => a.priority - b.priority)
})

// Methods
const loadRules = async () => {
  loading.value = true
  try {
    // TODO: Replace with actual API call
    // const response = await pennyApi.getRules(props.bot.id)
    // rules.value = response.data
    
    // Mock data for now
    await new Promise(resolve => setTimeout(resolve, 1000))
    rules.value = mockRules
  } catch (error) {
    console.error('Failed to load rules:', error)
  } finally {
    loading.value = false
  }
}

const getRuleIcon = (type) => {
  const icons = {
    intent: 'mdi:brain',
    keyword: 'mdi:key',
    regex: 'mdi:regex',
    custom: 'mdi:code-braces',
    always: 'mdi:play-circle'
  }
  return icons[type] || 'mdi:book-open-variant'
}

const getRuleTypeLabel = (type) => {
  const labels = {
    intent: t('Intent Based'),
    keyword: t('Keyword Based'),
    regex: t('Regex Pattern'),
    custom: t('Custom Condition'),
    always: t('Always Trigger')
  }
  return labels[type] || type
}

const toggleDropdown = (ruleId) => {
  dropdownOpen.value = dropdownOpen.value === ruleId ? null : ruleId
}

const closeDropdowns = () => {
  dropdownOpen.value = null
}

const testRule = async (rule) => {
  try {
    // TODO: Replace with actual API call
    // await pennyApi.testRule(rule.id)
    console.log('Testing rule:', rule.name)
  } catch (error) {
    console.error('Failed to test rule:', error)
  }
  closeDropdowns()
}

const editRule = (rule) => {
  editingRule.value = rule
  showCreateModal.value = true
  closeDropdowns()
}

const closeRuleModal = () => {
  showCreateModal.value = false
  editingRule.value = null
}

const handleRuleSaved = (savedRule) => {
  if (editingRule.value) {
    // Update existing rule
    const index = rules.value.findIndex(r => r.id === savedRule.id)
    if (index !== -1) {
      rules.value[index] = savedRule
    }
  } else {
    // Add new rule
    rules.value.push(savedRule)
  }
  closeRuleModal()
}

const toggleRuleStatus = async (rule) => {
  try {
    // TODO: Replace with actual API call
    // await pennyApi.toggleRuleStatus(rule.id, !rule.isActive)
    rule.isActive = !rule.isActive
  } catch (error) {
    console.error('Failed to toggle rule status:', error)
  }
  closeDropdowns()
}

const deleteRule = async (rule) => {
  if (confirm(`Are you sure you want to delete ${rule.name}?`)) {
    try {
      // TODO: Replace with actual API call
      // await pennyApi.deleteRule(rule.id)
      rules.value = rules.value.filter(r => r.id !== rule.id)
    } catch (error) {
      console.error('Failed to delete rule:', error)
    }
  }
  closeDropdowns()
}

// Lifecycle
onMounted(() => {
  if (props.show) {
    loadRules()
  }
  
  // Close dropdowns when clicking outside
  document.addEventListener('click', closeDropdowns)
})

// Watch for show prop changes
watch(() => props.show, (newShow) => {
  if (newShow) {
    loadRules()
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
