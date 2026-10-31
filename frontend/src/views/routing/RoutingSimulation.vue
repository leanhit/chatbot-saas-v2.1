<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-900 dark:text-white">{{ $t('routing.simulation.title') }}</h1>
      <button
        @click="runSimulation"
        :disabled="simulating"
        class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors disabled:opacity-50"
      >
        <Icon icon="mdi:play" class="inline-block mr-1" />
        {{ simulating ? $t('routing.simulation.running') : $t('routing.simulation.runSimulation') }}
      </button>
    </div>

    <!-- Simulation Configuration -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6 mb-6">
      <h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">{{ $t('routing.simulation.configuration') }}</h2>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('routing.simulation.conversationCount') }}
          </label>
          <input
            v-model.number="config.conversationCount"
            type="number"
            min="1"
            max="100"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('routing.simulation.agentCount') }}
          </label>
          <input
            v-model.number="config.agentCount"
            type="number"
            min="1"
            max="20"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('routing.simulation.routingType') }}
          </label>
          <select
            v-model="config.routingType"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
          >
            <option value="SKILL_BASED">{{ $t('routing.ruleTypes.skillBased') }}</option>
            <option value="PRIORITY_BASED">{{ $t('routing.ruleTypes.priorityBased') }}</option>
            <option value="ROUND_ROBIN">{{ $t('routing.ruleTypes.roundRobin') }}</option>
            <option value="LEAST_BUSY">{{ $t('routing.ruleTypes.leastBusy') }}</option>
          </select>
        </div>
      </div>
    </div>

    <!-- Simulation Results -->
    <div v-if="results.length > 0" class="space-y-6">
      <!-- Stats -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="p-3 bg-blue-100 dark:bg-blue-900 rounded-full">
              <Icon icon="mdi:message-text" class="text-2xl text-blue-600 dark:text-blue-400" />
            </div>
            <div class="ml-4">
              <p class="text-sm text-gray-500 dark:text-gray-400">{{ $t('routing.simulation.totalConversations') }}</p>
              <p class="text-2xl font-bold text-gray-900 dark:text-white">{{ results.length }}</p>
            </div>
          </div>
        </div>
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="p-3 bg-green-100 dark:bg-green-900 rounded-full">
              <Icon icon="mdi:check-circle" class="text-2xl text-green-600 dark:text-green-400" />
            </div>
            <div class="ml-4">
              <p class="text-sm text-gray-500 dark:text-gray-400">{{ $t('routing.simulation.assigned') }}</p>
              <p class="text-2xl font-bold text-gray-900 dark:text-white">{{ assignedCount }}</p>
            </div>
          </div>
        </div>
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="p-3 bg-yellow-100 dark:bg-yellow-900 rounded-full">
              <Icon icon="mdi:clock" class="text-2xl text-yellow-600 dark:text-yellow-400" />
            </div>
            <div class="ml-4">
              <p class="text-sm text-gray-500 dark:text-gray-400">{{ $t('routing.simulation.queued') }}</p>
              <p class="text-2xl font-bold text-gray-900 dark:text-white">{{ queuedCount }}</p>
            </div>
          </div>
        </div>
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="p-3 bg-purple-100 dark:bg-purple-900 rounded-full">
              <Icon icon="mdi:timer" class="text-2xl text-purple-600 dark:text-purple-400" />
            </div>
            <div class="ml-4">
              <p class="text-sm text-gray-500 dark:text-gray-400">{{ $t('routing.simulation.avgWaitTime') }}</p>
              <p class="text-2xl font-bold text-gray-900 dark:text-white">{{ avgWaitTime }}s</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Agent Distribution -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow">
        <div class="p-6 border-b border-gray-200 dark:border-gray-700">
          <h2 class="text-lg font-semibold text-gray-900 dark:text-white">{{ $t('routing.simulation.agentDistribution') }}</h2>
        </div>
        <div class="p-6">
          <div class="space-y-4">
            <div v-for="(count, agentId) in agentDistribution" :key="agentId" class="flex items-center">
              <div class="w-32 text-sm text-gray-900 dark:text-white">Agent {{ agentId }}</div>
              <div class="flex-1 mx-4 bg-gray-200 dark:bg-gray-700 rounded-full h-4">
                <div
                  :style="{ width: `${(count / results.length) * 100}%` }"
                  class="bg-blue-600 h-4 rounded-full"
                />
              </div>
              <div class="w-20 text-sm text-gray-900 dark:text-white text-right">{{ count }} ({{ Math.round((count / results.length) * 100) }}%)</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Results Table -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden">
        <div class="p-6 border-b border-gray-200 dark:border-gray-700">
          <h2 class="text-lg font-semibold text-gray-900 dark:text-white">{{ $t('routing.simulation.results') }}</h2>
        </div>
        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
            <thead class="bg-gray-50 dark:bg-gray-700">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                  {{ $t('routing.simulation.conversationId') }}
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                  {{ $t('routing.simulation.status') }}
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                  {{ $t('routing.simulation.assignedAgent') }}
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                  {{ $t('routing.simulation.priority') }}
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                  {{ $t('routing.simulation.waitTime') }}
                </th>
              </tr>
            </thead>
            <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
              <tr v-for="result in results" :key="result.id">
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                  #{{ result.conversationId }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span
                    :class="getStatusClass(result.status)"
                    class="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full"
                  >
                    {{ $t(`routing.routingStatuses.${result.status.toLowerCase()}`) }}
                  </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                  {{ result.assignedAgentId ? `Agent ${result.assignedAgentId}` : '-' }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                  {{ result.priority }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                  {{ result.waitTime }}s
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="bg-white dark:bg-gray-800 rounded-lg shadow p-12 text-center">
      <Icon icon="mdi:chart-box-outline" class="text-6xl text-gray-400 mx-auto mb-4" />
      <p class="text-gray-500 dark:text-gray-400">{{ $t('routing.simulation.noResults') }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Icon } from '@iconify/vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const simulating = ref(false)
const results = ref([])

const config = ref({
  conversationCount: 10,
  agentCount: 3,
  routingType: 'ROUND_ROBIN'
})

const assignedCount = computed(() => results.value.filter(r => r.status === 'ASSIGNED').length)
const queuedCount = computed(() => results.value.filter(r => r.status === 'QUEUED').length)
const avgWaitTime = computed(() => {
  if (results.value.length === 0) return 0
  const total = results.value.reduce((sum, r) => sum + r.waitTime, 0)
  return Math.round(total / results.value.length)
})

const agentDistribution = computed(() => {
  const distribution = {}
  results.value.forEach(r => {
    if (r.assignedAgentId) {
      distribution[r.assignedAgentId] = (distribution[r.assignedAgentId] || 0) + 1
    }
  })
  return distribution
})

const runSimulation = () => {
  simulating.value = true
  results.value = []

  setTimeout(() => {
    const { conversationCount, agentCount, routingType } = config.value
    let currentAgent = 1

    for (let i = 1; i <= conversationCount; i++) {
      let assignedAgentId = null
      let status = 'QUEUED'
      let waitTime = Math.floor(Math.random() * 30)

      if (routingType === 'ROUND_ROBIN') {
        assignedAgentId = currentAgent
        status = 'ASSIGNED'
        currentAgent = currentAgent >= agentCount ? 1 : currentAgent + 1
      } else if (routingType === 'LEAST_BUSY') {
        assignedAgentId = Math.floor(Math.random() * agentCount) + 1
        status = 'ASSIGNED'
      } else if (routingType === 'PRIORITY_BASED') {
        if (i % 3 === 0) {
          assignedAgentId = 1
          status = 'ASSIGNED'
          waitTime = 0
        }
      } else {
        if (Math.random() > 0.3) {
          assignedAgentId = Math.floor(Math.random() * agentCount) + 1
          status = 'ASSIGNED'
        }
      }

      results.value.push({
        id: i,
        conversationId: 1000 + i,
        status,
        assignedAgentId,
        priority: Math.floor(Math.random() * 10) + 1,
        waitTime
      })
    }

    simulating.value = false
  }, 1000)
}

const getStatusClass = (status) => {
  switch (status) {
    case 'ASSIGNED':
      return 'bg-green-100 text-green-800'
    case 'QUEUED':
      return 'bg-yellow-100 text-yellow-800'
    case 'ESCALATED':
      return 'bg-red-100 text-red-800'
    default:
      return 'bg-gray-100 text-gray-800'
  }
}
</script>
