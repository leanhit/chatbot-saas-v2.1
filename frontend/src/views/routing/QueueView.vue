<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-900 dark:text-white">{{ $t('routing.queue.title') }}</h1>
      <button
        @click="openCreateModal"
        class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
      >
        <Icon icon="mdi:plus" class="inline-block mr-1" />
        {{ $t('routing.queue.addRule') }}
      </button>
    </div>

    <!-- Filters -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-4 mb-6">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('routing.queue.filterByType') }}
          </label>
          <select
            v-model="filters.ruleType"
            @change="loadRoutingRules"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
          >
            <option value="">{{ $t('routing.queue.allTypes') }}</option>
            <option value="SKILL_BASED">{{ $t('routing.ruleTypes.skillBased') }}</option>
            <option value="PRIORITY_BASED">{{ $t('routing.ruleTypes.priorityBased') }}</option>
            <option value="ROUND_ROBIN">{{ $t('routing.ruleTypes.roundRobin') }}</option>
            <option value="LEAST_BUSY">{{ $t('routing.ruleTypes.leastBusy') }}</option>
            <option value="CUSTOM">{{ $t('routing.ruleTypes.custom') }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('routing.queue.filterByTarget') }}
          </label>
          <select
            v-model="filters.targetType"
            @change="loadRoutingRules"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
          >
            <option value="">{{ $t('routing.queue.allTargets') }}</option>
            <option value="AGENT">{{ $t('routing.targetTypes.agent') }}</option>
            <option value="TEAM">{{ $t('routing.targetTypes.team') }}</option>
            <option value="SKILL">{{ $t('routing.targetTypes.skill') }}</option>
            <option value="QUEUE">{{ $t('routing.targetTypes.queue') }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('routing.queue.status') }}
          </label>
          <select
            v-model="filters.isActive"
            @change="loadRoutingRules"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
          >
            <option value="">{{ $t('routing.queue.allStatus') }}</option>
            <option value="true">{{ $t('routing.queue.active') }}</option>
            <option value="false">{{ $t('routing.queue.inactive') }}</option>
          </select>
        </div>
        <div class="flex items-end">
          <button
            @click="resetFilters"
            class="w-full px-4 py-2 bg-gray-200 dark:bg-gray-600 text-gray-900 dark:text-white rounded-md hover:bg-gray-300 dark:hover:bg-gray-500 transition-colors"
          >
            {{ $t('routing.queue.resetFilters') }}
          </button>
        </div>
      </div>
    </div>

    <!-- Routing Rules Table -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
        <thead class="bg-gray-50 dark:bg-gray-700">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ $t('routing.queue.name') }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ $t('routing.queue.type') }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ $t('routing.queue.target') }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ $t('routing.queue.priority') }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ $t('routing.queue.status') }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ $t('routing.queue.actions') }}
            </th>
          </tr>
        </thead>
        <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
          <tr v-if="loading">
            <td colspan="6" class="px-6 py-4 text-center text-gray-500 dark:text-gray-400">
              {{ $t('routing.queue.loading') }}
            </td>
          </tr>
          <tr v-else-if="routingRules.length === 0">
            <td colspan="6" class="px-6 py-4 text-center text-gray-500 dark:text-gray-400">
              {{ $t('routing.queue.noRules') }}
            </td>
          </tr>
          <tr v-else v-for="rule in routingRules" :key="rule.id">
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
              {{ rule.name }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
              {{ $t(`routing.ruleTypes.${rule.ruleType.toLowerCase()}`) }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
              {{ $t(`routing.targetTypes.${rule.targetType.toLowerCase()}`) }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
              {{ rule.priority }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span
                :class="rule.isActive ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'"
                class="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full"
              >
                {{ rule.isActive ? $t('routing.queue.active') : $t('routing.queue.inactive') }}
              </span>
              <span
                v-if="rule.isDefault"
                class="ml-2 px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full bg-blue-100 text-blue-800"
              >
                {{ $t('routing.queue.default') }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
              <button
                @click="editRule(rule)"
                class="text-blue-600 hover:text-blue-900 dark:text-blue-400 dark:hover:text-blue-300 mr-3"
              >
                <Icon icon="mdi:pencil" class="inline-block" />
              </button>
              <button
                @click="deleteRule(rule)"
                class="text-red-600 hover:text-red-900 dark:text-red-400 dark:hover:text-red-300"
              >
                <Icon icon="mdi:delete" class="inline-block" />
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Create/Edit Modal -->
    <div v-if="showModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow-xl w-full max-w-2xl mx-4">
        <div class="flex justify-between items-center p-6 border-b border-gray-200 dark:border-gray-700">
          <h2 class="text-xl font-bold text-gray-900 dark:text-white">
            {{ isEdit ? $t('routing.queue.editRule') : $t('routing.queue.createRule') }}
          </h2>
          <button
            @click="closeModal"
            class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
          >
            <Icon icon="mdi:close" class="text-2xl" />
          </button>
        </div>
        <form @submit.prevent="saveRule" class="p-6">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="col-span-2">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                {{ $t('routing.queue.name') }} *
              </label>
              <input
                v-model="formData.name"
                type="text"
                required
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              />
            </div>
            <div class="col-span-2">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                {{ $t('routing.queue.description') }}
              </label>
              <textarea
                v-model="formData.description"
                rows="3"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                {{ $t('routing.queue.type') }} *
              </label>
              <select
                v-model="formData.ruleType"
                required
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              >
                <option value="SKILL_BASED">{{ $t('routing.ruleTypes.skillBased') }}</option>
                <option value="PRIORITY_BASED">{{ $t('routing.ruleTypes.priorityBased') }}</option>
                <option value="ROUND_ROBIN">{{ $t('routing.ruleTypes.roundRobin') }}</option>
                <option value="LEAST_BUSY">{{ $t('routing.ruleTypes.leastBusy') }}</option>
                <option value="CUSTOM">{{ $t('routing.ruleTypes.custom') }}</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                {{ $t('routing.queue.priority') }} *
              </label>
              <input
                v-model.number="formData.priority"
                type="number"
                required
                min="1"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                {{ $t('routing.queue.target') }} *
              </label>
              <select
                v-model="formData.targetType"
                required
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              >
                <option value="AGENT">{{ $t('routing.targetTypes.agent') }}</option>
                <option value="TEAM">{{ $t('routing.targetTypes.team') }}</option>
                <option value="SKILL">{{ $t('routing.targetTypes.skill') }}</option>
                <option value="QUEUE">{{ $t('routing.targetTypes.queue') }}</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                {{ $t('routing.queue.maxConcurrent') }}
              </label>
              <input
                v-model.number="formData.maxConcurrentConversations"
                type="number"
                min="1"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              />
            </div>
            <div class="flex items-center">
              <input
                v-model="formData.isActive"
                type="checkbox"
                id="isActive"
                class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              />
              <label for="isActive" class="ml-2 block text-sm text-gray-900 dark:text-white">
                {{ $t('routing.queue.active') }}
              </label>
            </div>
            <div class="flex items-center">
              <input
                v-model="formData.isDefault"
                type="checkbox"
                id="isDefault"
                class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              />
              <label for="isDefault" class="ml-2 block text-sm text-gray-900 dark:text-white">
                {{ $t('routing.queue.default') }}
              </label>
            </div>
          </div>
          <div class="mt-6 flex justify-end space-x-3">
            <button
              type="button"
              @click="closeModal"
              class="px-4 py-2 bg-gray-200 dark:bg-gray-600 text-gray-900 dark:text-white rounded-md hover:bg-gray-300 dark:hover:bg-gray-500 transition-colors"
            >
              {{ $t('routing.queue.cancel') }}
            </button>
            <button
              type="submit"
              class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
            >
              {{ $t('routing.queue.save') }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import { useI18n } from 'vue-i18n'
import routingApi from '@/api/routingApi'

const { t } = useI18n()

const routingRules = ref([])
const loading = ref(false)
const showModal = ref(false)
const isEdit = ref(false)
const filters = ref({
  ruleType: '',
  targetType: '',
  isActive: ''
})

const formData = ref({
  name: '',
  description: '',
  ruleType: 'SKILL_BASED',
  priority: 1,
  targetType: 'AGENT',
  maxConcurrentConversations: null,
  isActive: true,
  isDefault: false
})

const loadRoutingRules = async () => {
  try {
    loading.value = true
    const params = {}
    if (filters.value.ruleType) params.ruleType = filters.value.ruleType
    if (filters.value.targetType) params.targetType = filters.value.targetType
    if (filters.value.isActive !== '') params.isActive = filters.value.isActive === 'true'
    routingRules.value = await routingApi.getRoutingRules(params)
  } catch (err) {
    console.error('Load routing rules error:', err)
  } finally {
    loading.value = false
  }
}

const openCreateModal = () => {
  isEdit.value = false
  formData.value = {
    name: '',
    description: '',
    ruleType: 'SKILL_BASED',
    priority: 1,
    targetType: 'AGENT',
    maxConcurrentConversations: null,
    isActive: true,
    isDefault: false
  }
  showModal.value = true
}

const editRule = (rule) => {
  isEdit.value = true
  formData.value = { ...rule }
  showModal.value = true
}

const saveRule = async () => {
  try {
    if (isEdit.value) {
      await routingApi.updateRoutingRule(formData.value.id, formData.value)
    } else {
      await routingApi.createRoutingRule(formData.value)
    }
    closeModal()
    await loadRoutingRules()
  } catch (err) {
    console.error('Save rule error:', err)
  }
}

const deleteRule = async (rule) => {
  if (!confirm(t('routing.queue.confirmDelete'))) return
  try {
    await routingApi.deleteRoutingRule(rule.id)
    await loadRoutingRules()
  } catch (err) {
    console.error('Delete rule error:', err)
  }
}

const closeModal = () => {
  showModal.value = false
}

const resetFilters = () => {
  filters.value = {
    ruleType: '',
    targetType: '',
    isActive: ''
  }
  loadRoutingRules()
}

onMounted(() => {
  loadRoutingRules()
})
</script>
