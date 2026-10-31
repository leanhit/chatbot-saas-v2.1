<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-900 dark:text-white">{{ $t('routing.escalation.title') }}</h1>
      <button
        @click="openCreateModal"
        class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
      >
        <Icon icon="mdi:plus" class="inline-block mr-1" />
        {{ $t('routing.escalation.addRule') }}
      </button>
    </div>

    <!-- Filters -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-4 mb-6">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('routing.escalation.filterByTrigger') }}
          </label>
          <select
            v-model="filters.triggerType"
            @change="loadEscalationRules"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
          >
            <option value="">{{ $t('routing.escalation.allTriggers') }}</option>
            <option value="WAIT_TIME_EXCEEDED">{{ $t('routing.triggerTypes.waitTimeExceeded') }}</option>
            <option value="NO_RESPONSE">{{ $t('routing.triggerTypes.noResponse') }}</option>
            <option value="LOW_CUSTOMER_SATISFACTION">{{ $t('routing.triggerTypes.lowCustomerSatisfaction') }}</option>
            <option value="HIGH_PRIORITY_CUSTOMER">{{ $t('routing.triggerTypes.highPriorityCustomer') }}</option>
            <option value="CUSTOMER_TIER">{{ $t('routing.triggerTypes.customerTier') }}</option>
            <option value="CUSTOM_CONDITION">{{ $t('routing.triggerTypes.customCondition') }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('routing.escalation.filterByAction') }}
          </label>
          <select
            v-model="filters.escalationAction"
            @change="loadEscalationRules"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
          >
            <option value="">{{ $t('routing.escalation.allActions') }}</option>
            <option value="ASSIGN_TO_AGENT">{{ $t('routing.escalationActions.assignToAgent') }}</option>
            <option value="ASSIGN_TO_TEAM">{{ $t('routing.escalationActions.assignToTeam') }}</option>
            <option value="ASSIGN_TO_SUPERVISOR">{{ $t('routing.escalationActions.assignToSupervisor') }}</option>
            <option value="MOVE_TO_QUEUE">{{ $t('routing.escalationActions.moveToQueue') }}</option>
            <option value="SEND_NOTIFICATION">{{ $t('routing.escalationActions.sendNotification') }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('routing.escalation.status') }}
          </label>
          <select
            v-model="filters.isActive"
            @change="loadEscalationRules"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
          >
            <option value="">{{ $t('routing.escalation.allStatus') }}</option>
            <option value="true">{{ $t('routing.escalation.active') }}</option>
            <option value="false">{{ $t('routing.escalation.inactive') }}</option>
          </select>
        </div>
        <div class="flex items-end">
          <button
            @click="resetFilters"
            class="w-full px-4 py-2 bg-gray-200 dark:bg-gray-600 text-gray-900 dark:text-white rounded-md hover:bg-gray-300 dark:hover:bg-gray-500 transition-colors"
          >
            {{ $t('routing.escalation.resetFilters') }}
          </button>
        </div>
      </div>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="p-3 bg-blue-100 dark:bg-blue-900 rounded-full">
            <Icon icon="mdi:alert-circle" class="text-2xl text-blue-600 dark:text-blue-400" />
          </div>
          <div class="ml-4">
            <p class="text-sm text-gray-500 dark:text-gray-400">{{ $t('routing.escalation.totalRules') }}</p>
            <p class="text-2xl font-bold text-gray-900 dark:text-white">{{ escalationRules.length }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="p-3 bg-green-100 dark:bg-green-900 rounded-full">
            <Icon icon="mdi:check-circle" class="text-2xl text-green-600 dark:text-green-400" />
          </div>
          <div class="ml-4">
            <p class="text-sm text-gray-500 dark:text-gray-400">{{ $t('routing.escalation.activeRules') }}</p>
            <p class="text-2xl font-bold text-gray-900 dark:text-white">{{ activeRulesCount }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="p-3 bg-red-100 dark:bg-red-900 rounded-full">
            <Icon icon="mdi:arrow-up" class="text-2xl text-red-600 dark:text-red-400" />
          </div>
          <div class="ml-4">
            <p class="text-sm text-gray-500 dark:text-gray-400">{{ $t('routing.escalation.autoEscalate') }}</p>
            <p class="text-2xl font-bold text-gray-900 dark:text-white">{{ autoEscalateCount }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Escalation Rules Table -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
        <thead class="bg-gray-50 dark:bg-gray-700">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ $t('routing.escalation.name') }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ $t('routing.escalation.trigger') }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ $t('routing.escalation.action') }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ $t('routing.escalation.threshold') }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ $t('routing.escalation.priority') }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ $t('routing.escalation.status') }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ $t('routing.escalation.actions') }}
            </th>
          </tr>
        </thead>
        <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
          <tr v-if="loading">
            <td colspan="7" class="px-6 py-4 text-center text-gray-500 dark:text-gray-400">
              {{ $t('routing.escalation.loading') }}
            </td>
          </tr>
          <tr v-else-if="escalationRules.length === 0">
            <td colspan="7" class="px-6 py-4 text-center text-gray-500 dark:text-gray-400">
              {{ $t('routing.escalation.noRules') }}
            </td>
          </tr>
          <tr v-else v-for="rule in escalationRules" :key="rule.id">
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
              {{ rule.name }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
              {{ $t(`routing.triggerTypes.${rule.triggerType.toLowerCase().replace('_', '')}`) }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
              {{ $t(`routing.escalationActions.${rule.escalationAction.toLowerCase().replace('_', '')}`) }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
              <span v-if="rule.thresholdValue">{{ rule.thresholdValue }}</span>
              <span v-else-if="rule.timeThresholdMinutes">{{ rule.timeThresholdMinutes }}m</span>
              <span v-else>-</span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
              {{ rule.priority }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span
                :class="rule.isActive ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'"
                class="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full"
              >
                {{ rule.isActive ? $t('routing.escalation.active') : $t('routing.escalation.inactive') }}
              </span>
              <span
                v-if="rule.notifySupervisor"
                class="ml-2 px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full bg-blue-100 text-blue-800"
              >
                {{ $t('routing.escalation.notify') }}
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
            {{ isEdit ? $t('routing.escalation.editRule') : $t('routing.escalation.createRule') }}
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
                {{ $t('routing.escalation.name') }} *
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
                {{ $t('routing.escalation.description') }}
              </label>
              <textarea
                v-model="formData.description"
                rows="3"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                {{ $t('routing.escalation.trigger') }} *
              </label>
              <select
                v-model="formData.triggerType"
                required
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              >
                <option value="WAIT_TIME_EXCEEDED">{{ $t('routing.triggerTypes.waitTimeExceeded') }}</option>
                <option value="NO_RESPONSE">{{ $t('routing.triggerTypes.noResponse') }}</option>
                <option value="LOW_CUSTOMER_SATISFACTION">{{ $t('routing.triggerTypes.lowCustomerSatisfaction') }}</option>
                <option value="HIGH_PRIORITY_CUSTOMER">{{ $t('routing.triggerTypes.highPriorityCustomer') }}</option>
                <option value="CUSTOMER_TIER">{{ $t('routing.triggerTypes.customerTier') }}</option>
                <option value="CUSTOM_CONDITION">{{ $t('routing.triggerTypes.customCondition') }}</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                {{ $t('routing.escalation.action') }} *
              </label>
              <select
                v-model="formData.escalationAction"
                required
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              >
                <option value="ASSIGN_TO_AGENT">{{ $t('routing.escalationActions.assignToAgent') }}</option>
                <option value="ASSIGN_TO_TEAM">{{ $t('routing.escalationActions.assignToTeam') }}</option>
                <option value="ASSIGN_TO_SUPERVISOR">{{ $t('routing.escalationActions.assignToSupervisor') }}</option>
                <option value="MOVE_TO_QUEUE">{{ $t('routing.escalationActions.moveToQueue') }}</option>
                <option value="SEND_NOTIFICATION">{{ $t('routing.escalationActions.sendNotification') }}</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                {{ $t('routing.escalation.thresholdValue') }}
              </label>
              <input
                v-model.number="formData.thresholdValue"
                type="number"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                {{ $t('routing.escalation.timeThreshold') }} (min)
              </label>
              <input
                v-model.number="formData.timeThresholdMinutes"
                type="number"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                {{ $t('routing.escalation.priority') }} *
              </label>
              <input
                v-model.number="formData.priority"
                type="number"
                required
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
                {{ $t('routing.escalation.active') }}
              </label>
            </div>
            <div class="flex items-center">
              <input
                v-model="formData.notifySupervisor"
                type="checkbox"
                id="notifySupervisor"
                class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              />
              <label for="notifySupervisor" class="ml-2 block text-sm text-gray-900 dark:text-white">
                {{ $t('routing.escalation.notifySupervisor') }}
              </label>
            </div>
          </div>
          <div class="mt-6 flex justify-end space-x-3">
            <button
              type="button"
              @click="closeModal"
              class="px-4 py-2 bg-gray-200 dark:bg-gray-600 text-gray-900 dark:text-white rounded-md hover:bg-gray-300 dark:hover:bg-gray-500 transition-colors"
            >
              {{ $t('routing.escalation.cancel') }}
            </button>
            <button
              type="submit"
              class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
            >
              {{ $t('routing.escalation.save') }}
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
import { useI18n } from 'vue-i18n'
import routingApi from '@/api/routingApi'

const { t } = useI18n()

const escalationRules = ref([])
const loading = ref(false)
const showModal = ref(false)
const isEdit = ref(false)
const filters = ref({
  triggerType: '',
  escalationAction: '',
  isActive: ''
})

const formData = ref({
  name: '',
  description: '',
  triggerType: 'WAIT_TIME_EXCEEDED',
  escalationAction: 'ASSIGN_TO_SUPERVISOR',
  thresholdValue: null,
  timeThresholdMinutes: null,
  priority: 1,
  isActive: true,
  notifySupervisor: true
})

const activeRulesCount = computed(() => escalationRules.value.filter(r => r.isActive).length)
const autoEscalateCount = computed(() => escalationRules.value.filter(r => r.escalationAction === 'ASSIGN_TO_SUPERVISOR' || r.escalationAction === 'ASSIGN_TO_AGENT').length)

const loadEscalationRules = async () => {
  try {
    loading.value = true
    const params = {}
    if (filters.value.triggerType) params.triggerType = filters.value.triggerType
    if (filters.value.escalationAction) params.escalationAction = filters.value.escalationAction
    if (filters.value.isActive !== '') params.isActive = filters.value.isActive === 'true'
    escalationRules.value = await routingApi.getEscalationRules(params)
  } catch (err) {
    console.error('Load escalation rules error:', err)
  } finally {
    loading.value = false
  }
}

const openCreateModal = () => {
  isEdit.value = false
  formData.value = {
    name: '',
    description: '',
    triggerType: 'WAIT_TIME_EXCEEDED',
    escalationAction: 'ASSIGN_TO_SUPERVISOR',
    thresholdValue: null,
    timeThresholdMinutes: null,
    priority: 1,
    isActive: true,
    notifySupervisor: true
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
      await routingApi.updateEscalationRule(formData.value.id, formData.value)
    } else {
      await routingApi.createEscalationRule(formData.value)
    }
    closeModal()
    await loadEscalationRules()
  } catch (err) {
    console.error('Save rule error:', err)
  }
}

const deleteRule = async (rule) => {
  if (!confirm(t('routing.escalation.confirmDelete'))) return
  try {
    await routingApi.deleteEscalationRule(rule.id)
    await loadEscalationRules()
  } catch (err) {
    console.error('Delete rule error:', err)
  }
}

const closeModal = () => {
  showModal.value = false
}

const resetFilters = () => {
  filters.value = {
    triggerType: '',
    escalationAction: '',
    isActive: ''
  }
  loadEscalationRules()
}

onMounted(() => {
  loadEscalationRules()
})
</script>
