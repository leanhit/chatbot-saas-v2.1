<template>
  <div class="agent-training-dashboard p-4">
    <!-- Header -->
    <div class="mt-2 w-full">
      <div class="lg:flex grid-cols-1 lg:space-y-0 space-y-3 gap-5 justify-between">
        <div>
          <p class="uppercase text-xs text-gray-700 font-semibold">{{ $t('training.agentDashboard') }}</p>
          <h1 class="text-2xl text-gray-900 dark:text-gray-200 font-medium">
            {{ $t('training.agentDashboardTitle') }}
          </h1>
        </div>
        <div class="flex gap-2">
          <button
            @click="refreshData"
            :disabled="loading"
            class="bg-white dark:bg-gray-800 hover:border-gray-200 dark:hover:bg-gray-700 dark:text-white dark:border-gray-700 border rounded py-2 px-5 flex items-center gap-2"
          >
            <Icon icon="mdi:refresh" :class="{'animate-spin': loading}" class="text-lg" />
            {{ $t('common.refresh') }}
          </button>
        </div>
      </div>
    </div>

    <!-- Statistics Cards -->
    <div class="wrapper-card grid lg:grid-cols-4 grid-cols-1 md:grid-cols-2 gap-4 mt-6">
      <!-- Total Agents -->
      <div class="card bg-white dark:bg-gray-800 w-full rounded-md p-5 border dark:border-gray-700 flex">
        <div class="p-2 max-w-sm">
          <div class="bg-blue-200 rounded-full w-14 h-14 text-lg p-3 text-blue-600 mx-auto">
            <Icon icon="mdi:account-group" class="text-2xl" />
          </div>
        </div>
        <div class="block p-2 w-full">
          <p class="font-semibold text-gray-900 dark:text-gray-200 text-xl">
            {{ stats.totalAgents }}
          </p>
          <p class="text-sm text-gray-500 dark:text-gray-400">{{ $t('training.totalAgents') }}</p>
        </div>
      </div>

      <!-- Active Training -->
      <div class="card bg-white dark:bg-gray-800 w-full rounded-md p-5 border dark:border-gray-700 flex">
        <div class="p-2 max-w-sm">
          <div class="bg-green-200 rounded-full w-14 h-14 text-lg p-3 text-green-600 mx-auto">
            <Icon icon="mdi:school" class="text-2xl" />
          </div>
        </div>
        <div class="block p-2 w-full">
          <p class="font-semibold text-gray-900 dark:text-gray-200 text-xl">
            {{ stats.activeTraining }}
          </p>
          <p class="text-sm text-gray-500 dark:text-gray-400">{{ $t('training.activeTraining') }}</p>
        </div>
      </div>

      <!-- Completed Training -->
      <div class="card bg-white dark:bg-gray-800 w-full rounded-md p-5 border dark:border-gray-700 flex">
        <div class="p-2 max-w-sm">
          <div class="bg-purple-200 rounded-full w-14 h-14 text-lg p-3 text-purple-600 mx-auto">
            <Icon icon="mdi:check-circle" class="text-2xl" />
          </div>
        </div>
        <div class="block p-2 w-full">
          <p class="font-semibold text-gray-900 dark:text-gray-200 text-xl">
            {{ stats.completedTraining }}
          </p>
          <p class="text-sm text-gray-500 dark:text-gray-400">{{ $t('training.completedTraining') }}</p>
        </div>
      </div>

      <!-- Certified Agents -->
      <div class="card bg-white dark:bg-gray-800 w-full rounded-md p-5 border dark:border-gray-700 flex">
        <div class="p-2 max-w-sm">
          <div class="bg-yellow-200 rounded-full w-14 h-14 text-lg p-3 text-yellow-600 mx-auto">
            <Icon icon="mdi:certificate" class="text-2xl" />
          </div>
        </div>
        <div class="block p-2 w-full">
          <p class="font-semibold text-gray-900 dark:text-gray-200 text-xl">
            {{ stats.certifiedAgents }}
          </p>
          <p class="text-sm text-gray-500 dark:text-gray-400">{{ $t('training.certifiedAgents') }}</p>
        </div>
      </div>
    </div>

    <!-- Agent List -->
    <div class="mt-6 bg-white dark:bg-gray-800 rounded-md border dark:border-gray-700">
      <div class="p-4 border-b dark:border-gray-700">
        <div class="flex justify-between items-center">
          <h2 class="text-lg font-semibold text-gray-900 dark:text-white">{{ $t('training.agentList') }}</h2>
          <div class="flex gap-2">
            <input
              v-model="searchQuery"
              type="text"
              :placeholder="$t('training.searchAgent')"
              class="border rounded-md p-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
            />
            <select v-model="filterStatus" class="border rounded-md p-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white">
              <option value="">{{ $t('common.all') }}</option>
              <option value="NOT_STARTED">{{ $t('training.progressStatus.not_started') }}</option>
              <option value="IN_PROGRESS">{{ $t('training.progressStatus.in_progress') }}</option>
              <option value="COMPLETED">{{ $t('training.progressStatus.completed') }}</option>
            </select>
          </div>
        </div>
      </div>
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead class="bg-gray-50 dark:bg-gray-700">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">
                {{ $t('training.agent') }}
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">
                {{ $t('training.onboardingStatus') }}
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">
                {{ $t('training.trainingProgress') }}
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">
                {{ $t('training.skillScore') }}
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">
                {{ $t('training.certification') }}
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">
                {{ $t('training.actions') }}
              </th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200 dark:divide-gray-700">
            <tr v-for="agent in filteredAgents" :key="agent.id" class="hover:bg-gray-50 dark:hover:bg-gray-700">
              <td class="px-4 py-3">
                <div class="font-medium text-gray-900 dark:text-white">{{ agent.name }}</div>
                <div class="text-sm text-gray-500 dark:text-gray-400">{{ agent.email }}</div>
              </td>
              <td class="px-4 py-3">
                <div class="flex items-center gap-2">
                  <div class="w-full bg-gray-200 rounded-full h-2 dark:bg-gray-700">
                    <div class="bg-blue-600 h-2 rounded-full" :style="{ width: agent.onboardingProgress + '%' }"></div>
                  </div>
                  <span class="text-sm text-gray-700 dark:text-gray-300">{{ agent.onboardingProgress }}%</span>
                </div>
              </td>
              <td class="px-4 py-3">
                <div class="flex items-center gap-2">
                  <div class="w-full bg-gray-200 rounded-full h-2 dark:bg-gray-700">
                    <div class="bg-green-600 h-2 rounded-full" :style="{ width: agent.trainingProgress + '%' }"></div>
                  </div>
                  <span class="text-sm text-gray-700 dark:text-gray-300">{{ agent.trainingProgress }}%</span>
                </div>
              </td>
              <td class="px-4 py-3 text-gray-700 dark:text-gray-300">
                {{ agent.averageScore }}/100
              </td>
              <td class="px-4 py-3">
                <span v-if="agent.certification" class="px-2 py-1 text-xs rounded-full bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200">
                  {{ agent.certification }}
                </span>
                <span v-else class="text-gray-500 dark:text-gray-400">-</span>
              </td>
              <td class="px-4 py-3">
                <div class="flex gap-2">
                  <button @click="viewAgentDetails(agent)" class="text-blue-600 hover:text-blue-800 dark:text-blue-400">
                    <Icon icon="mdi:eye" class="text-lg" />
                  </button>
                  <button @click="assignTraining(agent)" class="text-green-600 hover:text-green-800 dark:text-green-400">
                    <Icon icon="mdi:school" class="text-lg" />
                  </button>
                  <button @click="assessSkills(agent)" class="text-purple-600 hover:text-purple-800 dark:text-purple-400">
                    <Icon icon="mdi:chart-bar" class="text-lg" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Icon } from '@iconify/vue'

const loading = ref(false)
const searchQuery = ref('')
const filterStatus = ref('')

const stats = ref({
  totalAgents: 0,
  activeTraining: 0,
  completedTraining: 0,
  certifiedAgents: 0
})

const agents = ref([])

const filteredAgents = computed(() => {
  return agents.value.filter(agent => {
    if (searchQuery.value && !agent.name.toLowerCase().includes(searchQuery.value.toLowerCase())) return false
    if (filterStatus.value && agent.trainingStatus !== filterStatus.value) return false
    return true
  })
})

const refreshData = () => {
  loading.value = true
  // TODO: Load data from API
  setTimeout(() => {
    loading.value = false
  }, 1000)
}

const viewAgentDetails = (agent) => {
  // TODO: Navigate to agent details
  console.log('View agent details:', agent)
}

const assignTraining = (agent) => {
  // TODO: Open assign training modal
  console.log('Assign training to agent:', agent)
}

const assessSkills = (agent) => {
  // TODO: Open skill assessment modal
  console.log('Assess skills for agent:', agent)
}

onMounted(() => {
  // TODO: Load agents and stats from API
  agents.value = []
})
</script>
