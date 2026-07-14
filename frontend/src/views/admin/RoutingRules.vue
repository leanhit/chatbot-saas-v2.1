<template>
  <div class="routing-rules p-4">
    <!-- Header -->
    <div class="mt-2 w-full">
      <div class="lg:flex grid-cols-1 lg:space-y-0 space-y-3 gap-5 justify-between">
        <div>
          <p class="uppercase text-xs text-gray-700 dark:text-gray-400 font-semibold">Admin</p>
          <h1 class="text-2xl text-gray-900 dark:text-gray-200 font-medium">
            Routing Rules
          </h1>
        </div>
        <div class="flex gap-2">
          <button
            @click="createDefaultRules"
            class="bg-white dark:bg-gray-800 hover:border-gray-200 dark:hover:bg-gray-700 dark:text-white dark:border-gray-700 border rounded py-2 px-5 flex items-center gap-2"
          >
            <Icon icon="mdi:refresh" class="text-lg" />
            Create Defaults
          </button>
          <button
            @click="showCreateModal = true"
            class="bg-primary border flex gap-2 text-white hover:bg-primary/80 dark:border-gray-700 rounded py-3 px-5"
          >
            <span class="icon text-2xl"><Icon icon="ic:twotone-plus" /></span>
            <span class="text">Add Rule</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Rules List -->
    <div v-if="loading" class="p-8 text-center mt-6">
      <Icon icon="mdi:loading" class="animate-spin text-6xl text-gray-300 dark:text-gray-600 mx-auto" />
      <p class="mt-2 text-gray-500 dark:text-gray-400">Loading routing rules...</p>
    </div>

    <div v-else-if="rules.length === 0" class="p-8 text-center mt-6">
      <Icon icon="mdi:route" class="text-6xl text-gray-300 dark:text-gray-600 mx-auto" />
      <p class="mt-2 text-sm text-gray-500 dark:text-gray-400">No routing rules found.</p>
      <button
        @click="createDefaultRules"
        class="mt-4 px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/80"
      >
        Create Default Rules
      </button>
    </div>

    <div v-else class="space-y-4 mt-6">
      <div
        v-for="rule in sortedRules"
        :key="rule.id"
        :class="[
          'p-4 border rounded-lg',
          rule.active ? 'border-blue-200 bg-blue-50 dark:bg-blue-900/20 dark:border-blue-800' : 'border-gray-200 bg-gray-50 dark:bg-gray-700 dark:border-gray-600'
        ]"
      >
        <div class="flex justify-between items-start">
          <div class="flex-1">
            <div class="flex items-center space-x-2">
              <h3 class="font-semibold text-gray-900 dark:text-gray-200">{{ rule.name }}</h3>
              <span
                :class="[
                  'px-2 py-1 text-xs rounded-full',
                  rule.active ? 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400' : 'bg-gray-100 text-gray-600 dark:bg-gray-700 dark:text-gray-400'
                ]"
              >
                {{ rule.active ? 'Active' : 'Inactive' }}
              </span>
              <span class="px-2 py-1 text-xs bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400 rounded-full">
                Priority: {{ rule.priority }}
              </span>
              <span class="px-2 py-1 text-xs bg-purple-100 text-purple-800 dark:bg-purple-900/30 dark:text-purple-400 rounded-full">
                {{ rule.ruleType }}
              </span>
            </div>
            <p class="mt-1 text-sm text-gray-600 dark:text-gray-400">{{ rule.description }}</p>
            <div class="mt-2 text-sm">
              <span class="font-medium text-gray-700 dark:text-gray-300">Conditions:</span>
              <code class="ml-2 bg-gray-100 dark:bg-gray-700 px-2 py-1 rounded text-xs text-gray-800 dark:text-gray-200">
                {{ JSON.stringify(rule.conditions) }}
              </code>
            </div>
            <div class="mt-1 text-sm">
              <span class="font-medium text-gray-700 dark:text-gray-300">Action:</span>
              <code class="ml-2 bg-gray-100 dark:bg-gray-700 px-2 py-1 rounded text-xs text-gray-800 dark:text-gray-200">
                {{ JSON.stringify(rule.action) }}
              </code>
            </div>
          </div>
          <div class="flex space-x-2 ml-4">
            <button
              @click="editRule(rule)"
              class="p-2 text-blue-600 hover:bg-blue-100 dark:hover:bg-blue-900/30 rounded-lg transition-colors"
            >
              <Icon icon="mdi:pencil" class="text-lg" />
            </button>
            <button
              @click="toggleRuleStatus(rule)"
              :class="[
                'p-2 rounded-lg transition-colors',
                rule.active ? 'text-green-600 hover:bg-green-100 dark:hover:bg-green-900/30' : 'text-gray-600 hover:bg-gray-200 dark:hover:bg-gray-600'
              ]"
            >
              <Icon :icon="rule.active ? 'mdi:toggle-switch' : 'mdi:toggle-switch-off'" class="text-lg" />
            </button>
            <button
              @click="deleteRule(rule.id)"
              class="p-2 text-red-600 hover:bg-red-100 dark:hover:bg-red-900/30 rounded-lg transition-colors"
            >
              <Icon icon="mdi:delete" class="text-lg" />
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <div
      v-if="showCreateModal || showEditModal"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
    >
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto border dark:border-gray-700">
        <h2 class="text-xl font-bold mb-4 text-gray-900 dark:text-gray-200">
          {{ showCreateModal ? 'Create Routing Rule' : 'Edit Routing Rule' }}
        </h2>
        <form @submit.prevent="saveRule">
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Name</label>
              <input
                v-model="currentRule.name"
                type="text"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded-lg focus:ring-2 focus:ring-blue-500"
                required
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Description</label>
              <textarea
                v-model="currentRule.description"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded-lg focus:ring-2 focus:ring-blue-500"
                rows="2"
              />
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Priority</label>
                <input
                  v-model.number="currentRule.priority"
                  type="number"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded-lg focus:ring-2 focus:ring-blue-500"
                  required
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Rule Type</label>
                <select
                  v-model="currentRule.ruleType"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded-lg focus:ring-2 focus:ring-blue-500"
                  required
                >
                  <option value="AUTO_ASSIGN">Auto Assign</option>
                  <option value="ROUTE_TO_QUEUE">Route to Queue</option>
                  <option value="ESCALATE">Escalate</option>
                  <option value="BLOCK">Block</option>
                  <option value="CUSTOM">Custom</option>
                </select>
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Conditions (JSON)</label>
              <textarea
                v-model="conditionsJson"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded-lg focus:ring-2 focus:ring-blue-500 font-mono text-sm"
                rows="4"
                placeholder='{"customerTier": "VIP", "language": "en"}'
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Action (JSON)</label>
              <textarea
                v-model="actionJson"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded-lg focus:ring-2 focus:ring-blue-500 font-mono text-sm"
                rows="4"
                placeholder='{"action": "assign_to_agent", "agentId": 123}'
              />
            </div>
            <div class="flex items-center">
              <input
                v-model="currentRule.active"
                type="checkbox"
                id="active"
                class="w-4 h-4 text-blue-600 border-gray-300 dark:border-gray-600 rounded focus:ring-blue-500 dark:bg-gray-700"
              />
              <label for="active" class="ml-2 text-sm text-gray-700 dark:text-gray-300">Active</label>
            </div>
          </div>
          <div class="flex justify-end space-x-2 mt-6">
            <button
              type="button"
              @click="closeModal"
              class="px-4 py-2 bg-gray-200 dark:bg-gray-700 text-gray-800 dark:text-gray-200 rounded-lg hover:bg-gray-300 dark:hover:bg-gray-600"
            >
              Cancel
            </button>
            <button
              type="submit"
              class="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/80"
            >
              Save
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import routingRuleApi from '@/api/routingRuleApi'

const loading = ref(false)
const rules = ref([])
const showCreateModal = ref(false)
const showEditModal = ref(false)
const currentRule = ref({
  name: '',
  description: '',
  priority: 0,
  ruleType: 'AUTO_ASSIGN',
  conditions: {},
  action: {},
  active: true
})
const conditionsJson = ref('{}')
const actionJson = ref('{}')

const sortedRules = computed(() => {
  return [...rules.value].sort((a, b) => b.priority - a.priority)
})

const loadRules = async () => {
  loading.value = true
  try {
    const response = await routingRuleApi.getRoutingRules()
    rules.value = response.data || []
  } catch (error) {
    console.error('Error loading routing rules:', error)
  } finally {
    loading.value = false
  }
}

const createDefaultRules = async () => {
  try {
    await routingRuleApi.createDefaultRoutingRules()
    await loadRules()
  } catch (error) {
    console.error('Error creating default rules:', error)
  }
}

const editRule = (rule) => {
  currentRule.value = { ...rule }
  conditionsJson.value = JSON.stringify(rule.conditions, null, 2)
  actionJson.value = JSON.stringify(rule.action, null, 2)
  showEditModal.value = true
}

const toggleRuleStatus = async (rule) => {
  try {
    const updatedRule = { ...rule, active: !rule.active }
    await routingRuleApi.updateRoutingRule(rule.id, updatedRule)
    await loadRules()
  } catch (error) {
    console.error('Error toggling rule status:', error)
  }
}

const deleteRule = async (id) => {
  if (confirm('Are you sure you want to delete this rule?')) {
    try {
      await routingRuleApi.deleteRoutingRule(id)
      await loadRules()
    } catch (error) {
      console.error('Error deleting rule:', error)
    }
  }
}

const saveRule = async () => {
  try {
    currentRule.value.conditions = JSON.parse(conditionsJson.value)
    currentRule.value.action = JSON.parse(actionJson.value)
    
    if (showCreateModal.value) {
      await routingRuleApi.createRoutingRule(currentRule.value)
    } else {
      await routingRuleApi.updateRoutingRule(currentRule.value.id, currentRule.value)
    }
    
    closeModal()
    await loadRules()
  } catch (error) {
    console.error('Error saving rule:', error)
    alert('Invalid JSON in conditions or action')
  }
}

const closeModal = () => {
  showCreateModal.value = false
  showEditModal.value = false
  currentRule.value = {
    name: '',
    description: '',
    priority: 0,
    ruleType: 'AUTO_ASSIGN',
    conditions: {},
    action: {},
    active: true
  }
  conditionsJson.value = '{}'
  actionJson.value = '{}'
}

onMounted(() => {
  loadRules()
})
</script>
