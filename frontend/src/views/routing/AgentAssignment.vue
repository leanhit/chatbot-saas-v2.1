<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-900 dark:text-white">{{ $t('routing.assignment.title') }}</h1>
      <button
        @click="openAssignModal"
        class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
      >
        <Icon icon="mdi:account-plus" class="inline-block mr-1" />
        {{ $t('routing.assignment.assignConversation') }}
      </button>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-6">
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="p-3 bg-blue-100 dark:bg-blue-900 rounded-full">
            <Icon icon="mdi:account-group" class="text-2xl text-blue-600 dark:text-blue-400" />
          </div>
          <div class="ml-4">
            <p class="text-sm text-gray-500 dark:text-gray-400">{{ $t('routing.assignment.totalAgents') }}</p>
            <p class="text-2xl font-bold text-gray-900 dark:text-white">{{ agents.length }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="p-3 bg-green-100 dark:bg-green-900 rounded-full">
            <Icon icon="mdi:account-check" class="text-2xl text-green-600 dark:text-green-400" />
          </div>
          <div class="ml-4">
            <p class="text-sm text-gray-500 dark:text-gray-400">{{ $t('routing.assignment.availableAgents') }}</p>
            <p class="text-2xl font-bold text-gray-900 dark:text-white">{{ availableAgentsCount }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="p-3 bg-yellow-100 dark:bg-yellow-900 rounded-full">
            <Icon icon="mdi:account-clock" class="text-2xl text-yellow-600 dark:text-yellow-400" />
          </div>
          <div class="ml-4">
            <p class="text-sm text-gray-500 dark:text-gray-400">{{ $t('routing.assignment.busyAgents') }}</p>
            <p class="text-2xl font-bold text-gray-900 dark:text-white">{{ busyAgentsCount }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="p-3 bg-red-100 dark:bg-red-900 rounded-full">
            <Icon icon="mdi:account-off" class="text-2xl text-red-600 dark:text-red-400" />
          </div>
          <div class="ml-4">
            <p class="text-sm text-gray-500 dark:text-gray-400">{{ $t('routing.assignment.offlineAgents') }}</p>
            <p class="text-2xl font-bold text-gray-900 dark:text-white">{{ offlineAgentsCount }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Filters -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-4 mb-6">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('routing.assignment.filterByStatus') }}
          </label>
          <select
            v-model="filters.status"
            @change="loadAgents"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
          >
            <option value="">{{ $t('routing.assignment.allStatuses') }}</option>
            <option value="AVAILABLE">{{ $t('routing.assignment.status.available') }}</option>
            <option value="BUSY">{{ $t('routing.assignment.status.busy') }}</option>
            <option value="OFFLINE">{{ $t('routing.assignment.status.offline') }}</option>
            <option value="AWAY">{{ $t('routing.assignment.status.away') }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('routing.assignment.searchAgent') }}
          </label>
          <input
            v-model="filters.search"
            type="text"
            @input="loadAgents"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
            :placeholder="$t('routing.assignment.searchPlaceholder')"
          />
        </div>
        <div class="flex items-end">
          <button
            @click="resetFilters"
            class="w-full px-4 py-2 bg-gray-200 dark:bg-gray-600 text-gray-900 dark:text-white rounded-md hover:bg-gray-300 dark:hover:bg-gray-500 transition-colors"
          >
            {{ $t('routing.assignment.resetFilters') }}
          </button>
        </div>
      </div>
    </div>

    <!-- Agents Table -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
        <thead class="bg-gray-50 dark:bg-gray-700">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ $t('routing.assignment.agent') }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ $t('routing.assignment.status') }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ $t('routing.assignment.activeConversations') }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ $t('routing.assignment.maxConversations') }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ $t('routing.assignment.workload') }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ $t('routing.assignment.priority') }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ $t('routing.assignment.actions') }}
            </th>
          </tr>
        </thead>
        <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
          <tr v-if="loading">
            <td colspan="7" class="px-6 py-4 text-center text-gray-500 dark:text-gray-400">
              {{ $t('routing.assignment.loading') }}
            </td>
          </tr>
          <tr v-else-if="agents.length === 0">
            <td colspan="7" class="px-6 py-4 text-center text-gray-500 dark:text-gray-400">
              {{ $t('routing.assignment.noAgents') }}
            </td>
          </tr>
          <tr v-else v-for="agent in agents" :key="agent.id">
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="flex items-center">
                <div class="flex-shrink-0 h-10 w-10">
                  <div class="h-10 w-10 rounded-full bg-gray-300 dark:bg-gray-600 flex items-center justify-center">
                    <Icon icon="mdi:account" class="text-gray-500 dark:text-gray-400" />
                  </div>
                </div>
                <div class="ml-4">
                  <div class="text-sm font-medium text-gray-900 dark:text-white">
                    {{ agent.agentName || `Agent ${agent.agentId}` }}
                  </div>
                  <div class="text-sm text-gray-500 dark:text-gray-400">
                    {{ agent.teamName || $t('routing.assignment.noTeam') }}
                  </div>
                </div>
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span
                :class="getStatusClass(agent.status)"
                class="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full"
              >
                {{ $t(`routing.assignment.status.${agent.status.toLowerCase()}`) }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
              {{ agent.activeConversations }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
              {{ agent.maxConcurrentConversations }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="flex items-center">
                <div class="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2.5">
                  <div
                    :class="getWorkloadColor(agent.workloadPercentage)"
                    class="h-2.5 rounded-full"
                    :style="{ width: `${agent.workloadPercentage}%` }"
                  ></div>
                </div>
                <span class="ml-2 text-sm text-gray-900 dark:text-white">{{ agent.workloadPercentage.toFixed(0) }}%</span>
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
              {{ agent.priorityScore }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
              <button
                @click="viewAgentDetails(agent)"
                class="text-blue-600 hover:text-blue-900 dark:text-blue-400 dark:hover:text-blue-300 mr-3"
                :title="$t('routing.assignment.viewDetails')"
              >
                <Icon icon="mdi:eye" class="inline-block" />
              </button>
              <button
                @click="assignConversation(agent)"
                :disabled="!agent.isAvailable"
                class="text-green-600 hover:text-green-900 dark:text-green-400 dark:hover:text-green-300 disabled:opacity-50 disabled:cursor-not-allowed"
                :title="$t('routing.assignment.assignToAgent')"
              >
                <Icon icon="mdi:account-arrow-right" class="inline-block" />
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Assign Conversation Modal -->
    <div v-if="showAssignModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow-xl w-full max-w-md mx-4">
        <div class="flex justify-between items-center p-6 border-b border-gray-200 dark:border-gray-700">
          <h2 class="text-xl font-bold text-gray-900 dark:text-white">
            {{ $t('routing.assignment.assignConversation') }}
          </h2>
          <button
            @click="closeAssignModal"
            class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
          >
            <Icon icon="mdi:close" class="text-2xl" />
          </button>
        </div>
        <form @submit.prevent="assignConversationToAgent" class="p-6">
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                {{ $t('routing.assignment.conversationId') }} *
              </label>
              <input
                v-model.number="assignForm.conversationId"
                type="number"
                required
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                {{ $t('routing.assignment.selectAgent') }} *
              </label>
              <select
                v-model="assignForm.agentId"
                required
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              >
                <option value="">{{ $t('routing.assignment.selectAgentPlaceholder') }}</option>
                <option
                  v-for="agent in availableAgents"
                  :key="agent.agentId"
                  :value="agent.agentId"
                >
                  {{ agent.agentName || `Agent ${agent.agentId}` }} ({{ agent.activeConversations }}/{{ agent.maxConcurrentConversations }})
                </option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                {{ $t('routing.assignment.reason') }}
              </label>
              <textarea
                v-model="assignForm.reason"
                rows="3"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              />
            </div>
          </div>
          <div class="mt-6 flex justify-end space-x-3">
            <button
              type="button"
              @click="closeAssignModal"
              class="px-4 py-2 bg-gray-200 dark:bg-gray-600 text-gray-900 dark:text-white rounded-md hover:bg-gray-300 dark:hover:bg-gray-500 transition-colors"
            >
              {{ $t('routing.assignment.cancel') }}
            </button>
            <button
              type="submit"
              class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
            >
              {{ $t('routing.assignment.assign') }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Agent Details Modal -->
    <div v-if="showDetailsModal && selectedAgent" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow-xl w-full max-w-2xl mx-4">
        <div class="flex justify-between items-center p-6 border-b border-gray-200 dark:border-gray-700">
          <h2 class="text-xl font-bold text-gray-900 dark:text-white">
            {{ $t('routing.assignment.agentDetails') }}
          </h2>
          <button
            @click="closeDetailsModal"
            class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
          >
            <Icon icon="mdi:close" class="text-2xl" />
          </button>
        </div>
        <div class="p-6">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">{{ $t('routing.assignment.basicInfo') }}</h3>
              <div class="space-y-3">
                <div>
                  <span class="text-sm text-gray-500 dark:text-gray-400">{{ $t('routing.assignment.agentId') }}:</span>
                  <span class="ml-2 text-sm font-medium text-gray-900 dark:text-white">{{ selectedAgent.agentId }}</span>
                </div>
                <div>
                  <span class="text-sm text-gray-500 dark:text-gray-400">{{ $t('routing.assignment.agentName') }}:</span>
                  <span class="ml-2 text-sm font-medium text-gray-900 dark:text-white">{{ selectedAgent.agentName || 'N/A' }}</span>
                </div>
                <div>
                  <span class="text-sm text-gray-500 dark:text-gray-400">{{ $t('routing.assignment.team') }}:</span>
                  <span class="ml-2 text-sm font-medium text-gray-900 dark:text-white">{{ selectedAgent.teamName || $t('routing.assignment.noTeam') }}</span>
                </div>
                <div>
                  <span class="text-sm text-gray-500 dark:text-gray-400">{{ $t('routing.assignment.status') }}:</span>
                  <span class="ml-2">
                    <span
                      :class="getStatusClass(selectedAgent.status)"
                      class="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full"
                    >
                      {{ $t(`routing.assignment.status.${selectedAgent.status.toLowerCase()}`) }}
                    </span>
                  </span>
                </div>
              </div>
            </div>
            <div>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">{{ $t('routing.assignment.workloadInfo') }}</h3>
              <div class="space-y-3">
                <div>
                  <span class="text-sm text-gray-500 dark:text-gray-400">{{ $t('routing.assignment.activeConversations') }}:</span>
                  <span class="ml-2 text-sm font-medium text-gray-900 dark:text-white">{{ selectedAgent.activeConversations }}</span>
                </div>
                <div>
                  <span class="text-sm text-gray-500 dark:text-gray-400">{{ $t('routing.assignment.maxConversations') }}:</span>
                  <span class="ml-2 text-sm font-medium text-gray-900 dark:text-white">{{ selectedAgent.maxConcurrentConversations }}</span>
                </div>
                <div>
                  <span class="text-sm text-gray-500 dark:text-gray-400">{{ $t('routing.assignment.workloadPercentage') }}:</span>
                  <span class="ml-2 text-sm font-medium text-gray-900 dark:text-white">{{ selectedAgent.workloadPercentage.toFixed(1) }}%</span>
                </div>
                <div>
                  <span class="text-sm text-gray-500 dark:text-gray-400">{{ $t('routing.assignment.priorityScore') }}:</span>
                  <span class="ml-2 text-sm font-medium text-gray-900 dark:text-white">{{ selectedAgent.priorityScore }}</span>
                </div>
              </div>
            </div>
          </div>
          <div class="mt-6 flex justify-end">
            <button
              @click="closeDetailsModal"
              class="px-4 py-2 bg-gray-200 dark:bg-gray-600 text-gray-900 dark:text-white rounded-md hover:bg-gray-300 dark:hover:bg-gray-500 transition-colors"
            >
              {{ $t('routing.assignment.close') }}
            </button>
          </div>
        </div>
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

const agents = ref([])
const loading = ref(false)
const showAssignModal = ref(false)
const showDetailsModal = ref(false)
const selectedAgent = ref(null)
const filters = ref({
  status: '',
  search: ''
})

const assignForm = ref({
  conversationId: null,
  agentId: null,
  reason: ''
})

const availableAgents = computed(() => agents.value.filter(a => a.isAvailable))
const availableAgentsCount = computed(() => agents.value.filter(a => a.status === 'AVAILABLE').length)
const busyAgentsCount = computed(() => agents.value.filter(a => a.status === 'BUSY').length)
const offlineAgentsCount = computed(() => agents.value.filter(a => a.status === 'OFFLINE').length)

const loadAgents = async () => {
  try {
    loading.value = true
    const params = {}
    if (filters.value.status) params.status = filters.value.status
    if (filters.value.search) params.search = filters.value.search
    agents.value = await routingApi.getAgentWorkloads(params)
  } catch (err) {
    console.error('Load agents error:', err)
  } finally {
    loading.value = false
  }
}

const openAssignModal = () => {
  assignForm.value = {
    conversationId: null,
    agentId: null,
    reason: ''
  }
  showAssignModal.value = true
}

const assignConversationToAgent = async () => {
  try {
    await routingApi.assignConversation({
      conversationId: assignForm.value.conversationId,
      agentId: assignForm.value.agentId,
      reason: assignForm.value.reason
    })
    closeAssignModal()
    await loadAgents()
  } catch (err) {
    console.error('Assign conversation error:', err)
  }
}

const viewAgentDetails = (agent) => {
  selectedAgent.value = agent
  showDetailsModal.value = true
}

const assignConversation = (agent) => {
  assignForm.value.agentId = agent.agentId
  showAssignModal.value = true
}

const closeAssignModal = () => {
  showAssignModal.value = false
}

const closeDetailsModal = () => {
  showDetailsModal.value = false
  selectedAgent.value = null
}

const resetFilters = () => {
  filters.value = {
    status: '',
    search: ''
  }
  loadAgents()
}

const getStatusClass = (status) => {
  switch (status) {
    case 'AVAILABLE': return 'bg-green-100 text-green-800'
    case 'BUSY': return 'bg-yellow-100 text-yellow-800'
    case 'OFFLINE': return 'bg-red-100 text-red-800'
    case 'AWAY': return 'bg-gray-100 text-gray-800'
    default: return 'bg-gray-100 text-gray-800'
  }
}

const getWorkloadColor = (percentage) => {
  if (percentage < 50) return 'bg-green-500'
  if (percentage < 80) return 'bg-yellow-500'
  return 'bg-red-500'
}

onMounted(() => {
  loadAgents()
})
</script>
