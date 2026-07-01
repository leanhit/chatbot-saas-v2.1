<template>
  <div class="routing-rules">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-900">Routing Rules</h1>
      <div class="space-x-2">
        <button
          @click="createDefaultRules"
          class="px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 transition-colors"
        >
          <Icon icon="mdi:refresh" class="inline-block mr-1" />
          Create Defaults
        </button>
        <button
          @click="showCreateModal = true"
          class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          <Icon icon="mdi:plus" class="inline-block mr-1" />
          Add Rule
        </button>
      </div>
    </div>

    <!-- Rules List -->
    <div v-if="loading" class="p-8 text-center">
      <Icon icon="mdi:loading" class="animate-spin text-2xl text-gray-400" />
      <p class="mt-2 text-sm text-gray-500">Loading routing rules...</p>
    </div>

    <div v-else-if="rules.length === 0" class="p-8 text-center">
      <Icon icon="mdi:route" class="text-4xl text-gray-300 mx-auto" />
      <p class="mt-2 text-sm text-gray-500">No routing rules found.</p>
      <button
        @click="createDefaultRules"
        class="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
      >
        Create Default Rules
      </button>
    </div>

    <div v-else class="space-y-4">
      <div
        v-for="rule in sortedRules"
        :key="rule.id"
        :class="[
          'p-4 border rounded-lg',
          rule.active ? 'border-blue-200 bg-blue-50' : 'border-gray-200 bg-gray-50'
        ]"
      >
        <div class="flex justify-between items-start">
          <div class="flex-1">
            <div class="flex items-center space-x-2">
              <h3 class="font-semibold text-gray-900">{{ rule.name }}</h3>
              <span
                :class="[
                  'px-2 py-1 text-xs rounded-full',
                  rule.active ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-600'
                ]"
              >
                {{ rule.active ? 'Active' : 'Inactive' }}
              </span>
              <span class="px-2 py-1 text-xs bg-blue-100 text-blue-800 rounded-full">
                Priority: {{ rule.priority }}
              </span>
              <span class="px-2 py-1 text-xs bg-purple-100 text-purple-800 rounded-full">
                {{ rule.ruleType }}
              </span>
            </div>
            <p class="mt-1 text-sm text-gray-600">{{ rule.description }}</p>
            <div class="mt-2 text-sm">
              <span class="font-medium text-gray-700">Conditions:</span>
              <code class="ml-2 bg-gray-100 px-2 py-1 rounded text-xs">
                {{ JSON.stringify(rule.conditions) }}
              </code>
            </div>
            <div class="mt-1 text-sm">
              <span class="font-medium text-gray-700">Action:</span>
              <code class="ml-2 bg-gray-100 px-2 py-1 rounded text-xs">
                {{ JSON.stringify(rule.action) }}
              </code>
            </div>
          </div>
          <div class="flex space-x-2 ml-4">
            <button
              @click="editRule(rule)"
              class="p-2 text-blue-600 hover:bg-blue-100 rounded-lg transition-colors"
            >
              <Icon icon="mdi:pencil" class="text-lg" />
            </button>
            <button
              @click="toggleRuleStatus(rule)"
              :class="[
                'p-2 rounded-lg transition-colors',
                rule.active ? 'text-green-600 hover:bg-green-100' : 'text-gray-600 hover:bg-gray-200'
              ]"
            >
              <Icon :icon="rule.active ? 'mdi:toggle-switch' : 'mdi:toggle-switch-off'" class="text-lg" />
            </button>
            <button
              @click="deleteRule(rule.id)"
              class="p-2 text-red-600 hover:bg-red-100 rounded-lg transition-colors"
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
      <div class="bg-white rounded-lg p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <h2 class="text-xl font-bold mb-4">
          {{ showCreateModal ? 'Create Routing Rule' : 'Edit Routing Rule' }}
        </h2>
        <form @submit.prevent="saveRule">
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Name</label>
              <input
                v-model="currentRule.name"
                type="text"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                required
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Description</label>
              <textarea
                v-model="currentRule.description"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                rows="2"
              />
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Priority</label>
                <input
                  v-model.number="currentRule.priority"
                  type="number"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  required
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Rule Type</label>
                <select
                  v-model="currentRule.ruleType"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
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
              <label class="block text-sm font-medium text-gray-700 mb-1">Conditions (JSON)</label>
              <textarea
                v-model="conditionsJson"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 font-mono text-sm"
                rows="4"
                placeholder='{"customerTier": "VIP", "language": "en"}'
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Action (JSON)</label>
              <textarea
                v-model="actionJson"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 font-mono text-sm"
                rows="4"
                placeholder='{"action": "assign_to_agent", "agentId": 123}'
              />
            </div>
            <div class="flex items-center">
              <input
                v-model="currentRule.active"
                type="checkbox"
                id="active"
                class="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
              />
              <label for="active" class="ml-2 text-sm text-gray-700">Active</label>
            </div>
          </div>
          <div class="flex justify-end space-x-2 mt-6">
            <button
              type="button"
              @click="closeModal"
              class="px-4 py-2 bg-gray-200 text-gray-800 rounded-lg hover:bg-gray-300"
            >
              Cancel
            </button>
            <button
              type="submit"
              class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
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
