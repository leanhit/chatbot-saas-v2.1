<template>
  <div class="onboarding-wizard p-4">
    <!-- Header -->
    <div class="mt-2 w-full">
      <div class="flex flex-wrap justify-between items-center gap-3">
        <div>
          <p class="uppercase text-xs text-gray-700 dark:text-gray-300 font-semibold">Training</p>
          <h1 class="text-2xl text-gray-900 dark:text-gray-200 font-medium">Onboarding Wizard</h1>
        </div>
        <div class="flex items-center gap-2">
          <label class="text-sm text-gray-600 dark:text-gray-300">Agent:</label>
          <select
            v-model="selectedAgentId"
            class="border rounded-md px-3 py-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
          >
            <option :value="null" disabled>Select an agent</option>
            <option v-for="a in agents" :key="a.agentId" :value="a.agentId">
              {{ a.agentName }} ({{ a.agentEmail }})
            </option>
          </select>
        </div>
      </div>
    </div>

    <div v-if="!selectedAgentId" class="mt-8 text-center text-gray-500">
      <Icon icon="mdi:account-arrow-right" class="text-5xl text-gray-300" />
      <p class="mt-3">Select an agent to view their onboarding progress</p>
    </div>

    <template v-else>
      <!-- Progress summary -->
      <div class="mt-6 grid grid-cols-1 md:grid-cols-3 gap-4">
        <div class="bg-white dark:bg-gray-800 rounded-md border dark:border-gray-700 p-5">
          <p class="text-sm text-gray-500 dark:text-gray-400">Completed</p>
          <p class="text-2xl font-semibold text-green-600">{{ completedCount }}/{{ tasks.length }}</p>
        </div>
        <div class="bg-white dark:bg-gray-800 rounded-md border dark:border-gray-700 p-5">
          <p class="text-sm text-gray-500 dark:text-gray-400">In progress</p>
          <p class="text-2xl font-semibold text-blue-600">{{ inProgressCount }}</p>
        </div>
        <div class="bg-white dark:bg-gray-800 rounded-md border dark:border-gray-700 p-5">
          <p class="text-sm text-gray-500 dark:text-gray-400">Not started</p>
          <p class="text-2xl font-semibold text-gray-500">{{ pendingCount }}</p>
        </div>
      </div>

      <!-- Task list -->
      <div class="mt-6 bg-white dark:bg-gray-800 rounded-md border dark:border-gray-700">
        <div class="p-4 border-b dark:border-gray-700 flex items-center justify-between">
          <h2 class="text-lg font-semibold text-gray-900 dark:text-white">Onboarding Tasks</h2>
          <button @click="loadAll" :disabled="loading" class="text-sm text-blue-600 hover:underline flex items-center gap-1">
            <Icon icon="mdi:refresh" :class="{'animate-spin': loading}" /> Refresh
          </button>
        </div>
        <div v-if="loading" class="p-8 text-center text-gray-500">
          <Icon icon="mdi:loading" class="animate-spin text-2xl" />
        </div>
        <div v-else-if="tasks.length === 0" class="p-8 text-center text-gray-500">No onboarding tasks configured.</div>
        <ul v-else class="divide-y divide-gray-200 dark:divide-gray-700">
          <li v-for="task in sortedTasks" :key="task.id" class="p-4 flex items-start gap-4">
            <div class="flex-shrink-0 mt-1">
              <Icon
                :icon="statusIcon(progressMap[task.id]?.status)"
                :class="statusClass(progressMap[task.id]?.status)"
                class="text-2xl"
              />
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 flex-wrap">
                <h3 class="font-medium text-gray-900 dark:text-white">{{ task.title }}</h3>
                <span class="text-xs px-2 py-0.5 rounded-full bg-gray-100 text-gray-600 dark:bg-gray-700 dark:text-gray-300">
                  {{ task.type }}
                </span>
                <span v-if="task.required" class="text-xs px-2 py-0.5 rounded-full bg-red-100 text-red-700 dark:bg-red-900/40 dark:text-red-300">Required</span>
              </div>
              <p v-if="task.description" class="text-sm text-gray-500 dark:text-gray-400 mt-1">{{ task.description }}</p>
              <p v-if="task.estimatedDurationMinutes" class="text-xs text-gray-400 mt-1">~{{ task.estimatedDurationMinutes }} min</p>
              <div v-if="progressMap[task.id]" class="text-xs text-gray-500 dark:text-gray-400 mt-1">
                <span v-if="progressMap[task.id].startedAt">Started: {{ formatDate(progressMap[task.id].startedAt) }}</span>
                <span v-if="progressMap[task.id].completedAt"> • Completed: {{ formatDate(progressMap[task.id].completedAt) }}</span>
              </div>
            </div>
            <div class="flex-shrink-0 flex gap-2">
              <a
                v-if="task.externalUrl"
                :href="task.externalUrl"
                target="_blank"
                class="text-sm text-blue-600 hover:underline flex items-center gap-1"
              >
                <Icon icon="mdi:open-in-new" /> Open
              </a>
              <button
                v-if="!progressMap[task.id] || progressMap[task.id].status === 'PENDING'"
                @click="startTask(task)"
                :disabled="acting === task.id"
                class="text-sm px-3 py-1.5 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
              >Start</button>
              <button
                v-if="progressMap[task.id]?.status === 'IN_PROGRESS'"
                @click="completeTask(task)"
                :disabled="acting === task.id"
                class="text-sm px-3 py-1.5 bg-green-600 text-white rounded hover:bg-green-700 disabled:opacity-50"
              >Complete</button>
            </div>
          </li>
        </ul>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import { trainingApi } from '@/api/trainingApi'

const agents = ref([])
const selectedAgentId = ref(null)
const tasks = ref([])
const progress = ref([])
const loading = ref(false)
const acting = ref(null)

const progressMap = computed(() => {
  const map = {}
  for (const p of progress.value) map[p.taskId] = p
  return map
})

const statusOf = (taskId) => progressMap.value[taskId]?.status || 'PENDING'
const completedCount = computed(() => tasks.value.filter(t => statusOf(t.id) === 'COMPLETED').length)
const inProgressCount = computed(() => tasks.value.filter(t => statusOf(t.id) === 'IN_PROGRESS').length)
const pendingCount = computed(() => tasks.value.filter(t => ['PENDING', 'SKIPPED'].includes(statusOf(t.id)) || !progressMap.value[t.id]).length)

const sortedTasks = computed(() =>
  [...tasks.value].sort((a, b) => (a.displayOrder ?? 0) - (b.displayOrder ?? 0))
)

function statusIcon(status) {
  return { COMPLETED: 'mdi:check-circle', IN_PROGRESS: 'mdi:progress-clock', SKIPPED: 'mdi:skip-next-circle', PENDING: 'mdi:circle-outline' }[status] || 'mdi:circle-outline'
}
function statusClass(status) {
  return { COMPLETED: 'text-green-500', IN_PROGRESS: 'text-blue-500', SKIPPED: 'text-gray-400', PENDING: 'text-gray-300 dark:text-gray-600' }[status] || 'text-gray-300'
}
function formatDate(v) {
  if (!v) return ''
  const d = new Date(v)
  return isNaN(d) ? v : d.toLocaleDateString()
}

async function loadAgents() {
  try {
    const { data } = await trainingApi.getAgentsWithTrainingStats()
    agents.value = Array.isArray(data) ? data : []
  } catch (e) {
    console.error('Failed to load agents', e)
  }
}

async function loadAll() {
  loading.value = true
  try {
    const [t, p] = await Promise.all([
      trainingApi.getAllOnboardingTasks(),
      trainingApi.getAgentOnboardingProgress(selectedAgentId.value),
    ])
    tasks.value = Array.isArray(t.data) ? t.data : []
    progress.value = Array.isArray(p.data) ? p.data : []
  } catch (e) {
    console.error('Failed to load onboarding data', e)
  } finally {
    loading.value = false
  }
}

async function startTask(task) {
  acting.value = task.id
  try {
    await trainingApi.startOnboardingTask(task.id, selectedAgentId.value)
    await loadAll()
  } catch (e) {
    console.error('Start task failed', e)
  } finally {
    acting.value = null
  }
}

async function completeTask(task) {
  acting.value = task.id
  try {
    await trainingApi.completeOnboardingTask(selectedAgentId.value, task.id)
    await loadAll()
  } catch (e) {
    console.error('Complete task failed', e)
  } finally {
    acting.value = null
  }
}

watch(selectedAgentId, (id) => { if (id) loadAll() })
onMounted(loadAgents)
</script>
