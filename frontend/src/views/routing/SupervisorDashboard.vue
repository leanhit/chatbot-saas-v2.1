<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-900 dark:text-white">{{ $t('routing.supervisor.title') }}</h1>
      <button
        @click="openStartSessionModal"
        class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
      >
        <Icon icon="mdi:play" class="inline-block mr-1" />
        {{ $t('routing.supervisor.startSession') }}
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
            <p class="text-sm text-gray-500 dark:text-gray-400">{{ $t('routing.supervisor.activeSessions') }}</p>
            <p class="text-2xl font-bold text-gray-900 dark:text-white">{{ stats.activeSessions }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="p-3 bg-green-100 dark:bg-green-900 rounded-full">
            <Icon icon="mdi:check-circle" class="text-2xl text-green-600 dark:text-green-400" />
          </div>
          <div class="ml-4">
            <p class="text-sm text-gray-500 dark:text-gray-400">{{ $t('routing.supervisor.completedToday') }}</p>
            <p class="text-2xl font-bold text-gray-900 dark:text-white">{{ stats.completedToday }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="p-3 bg-yellow-100 dark:bg-yellow-900 rounded-full">
            <Icon icon="mdi:alert-circle" class="text-2xl text-yellow-600 dark:text-yellow-400" />
          </div>
          <div class="ml-4">
            <p class="text-sm text-gray-500 dark:text-gray-400">{{ $t('routing.supervisor.totalInterventions') }}</p>
            <p class="text-2xl font-bold text-gray-900 dark:text-white">{{ stats.totalInterventions }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="p-3 bg-purple-100 dark:bg-purple-900 rounded-full">
            <Icon icon="mdi:clock" class="text-2xl text-purple-600 dark:text-purple-400" />
          </div>
          <div class="ml-4">
            <p class="text-sm text-gray-500 dark:text-gray-400">{{ $t('routing.supervisor.avgDuration') }}</p>
            <p class="text-2xl font-bold text-gray-900 dark:text-white">{{ stats.avgDuration }}m</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Active Sessions -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow mb-6">
      <div class="p-6 border-b border-gray-200 dark:border-gray-700">
        <h2 class="text-lg font-semibold text-gray-900 dark:text-white">{{ $t('routing.supervisor.activeSessions') }}</h2>
      </div>
      <div class="p-6">
        <div v-if="loading" class="text-center text-gray-500 dark:text-gray-400">
          {{ $t('routing.supervisor.loading') }}
        </div>
        <div v-else-if="activeSessions.length === 0" class="text-center text-gray-500 dark:text-gray-400">
          {{ $t('routing.supervisor.noActiveSessions') }}
        </div>
        <div v-else class="space-y-4">
          <div
            v-for="session in activeSessions"
            :key="session.id"
            class="flex items-center justify-between p-4 bg-gray-50 dark:bg-gray-700 rounded-lg"
          >
            <div class="flex items-center space-x-4">
              <div class="p-2 bg-blue-100 dark:bg-blue-900 rounded-full">
                <Icon icon="mdi:eye" class="text-xl text-blue-600 dark:text-blue-400" />
              </div>
              <div>
                <p class="font-medium text-gray-900 dark:text-white">
                  {{ $t('routing.supervisor.conversation') }} #{{ session.conversationId }}
                </p>
                <p class="text-sm text-gray-500 dark:text-gray-400">
                  {{ $t(`routing.sessionTypes.${session.sessionType.toLowerCase()}`) }} • 
                  {{ formatDuration(session.joinedAt) }}
                </p>
              </div>
            </div>
            <div class="flex items-center space-x-2">
              <span
                :class="session.isSilent ? 'bg-gray-100 text-gray-800' : 'bg-yellow-100 text-yellow-800'"
                class="px-2 py-1 text-xs font-semibold rounded-full"
              >
                {{ session.isSilent ? $t('routing.supervisor.silent') : $t('routing.supervisor.canIntervene') }}
              </span>
              <button
                @click="endSession(session)"
                class="px-3 py-1 bg-red-600 text-white rounded-md hover:bg-red-700 transition-colors text-sm"
              >
                {{ $t('routing.supervisor.end') }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Recent Sessions History -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow">
      <div class="p-6 border-b border-gray-200 dark:border-gray-700">
        <h2 class="text-lg font-semibold text-gray-900 dark:text-white">{{ $t('routing.supervisor.recentSessions') }}</h2>
      </div>
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
          <thead class="bg-gray-50 dark:bg-gray-700">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                {{ $t('routing.supervisor.conversation') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                {{ $t('routing.supervisor.type') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                {{ $t('routing.supervisor.duration') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                {{ $t('routing.supervisor.interventions') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                {{ $t('routing.supervisor.rating') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                {{ $t('routing.supervisor.status') }}
              </th>
            </tr>
          </thead>
          <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
            <tr v-if="loadingHistory">
              <td colspan="6" class="px-6 py-4 text-center text-gray-500 dark:text-gray-400">
                {{ $t('routing.supervisor.loading') }}
              </td>
            </tr>
            <tr v-else-if="recentSessions.length === 0">
              <td colspan="6" class="px-6 py-4 text-center text-gray-500 dark:text-gray-400">
                {{ $t('routing.supervisor.noRecentSessions') }}
              </td>
            </tr>
            <tr v-else v-for="session in recentSessions" :key="session.id">
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                #{{ session.conversationId }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                {{ $t(`routing.sessionTypes.${session.sessionType.toLowerCase()}`) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                {{ formatDurationSeconds(session.durationSeconds) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                {{ session.interventionCount }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                <span v-if="session.rating">{{ session.rating }}/5</span>
                <span v-else class="text-gray-400">-</span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span
                  :class="session.sessionStatus === 'ACTIVE' ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'"
                  class="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full"
                >
                  {{ $t(`routing.sessionStatuses.${session.sessionStatus.toLowerCase()}`) }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Start Session Modal -->
    <div v-if="showStartSessionModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow-xl w-full max-w-md mx-4">
        <div class="flex justify-between items-center p-6 border-b border-gray-200 dark:border-gray-700">
          <h2 class="text-xl font-bold text-gray-900 dark:text-white">{{ $t('routing.supervisor.startSession') }}</h2>
          <button
            @click="closeStartSessionModal"
            class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
          >
            <Icon icon="mdi:close" class="text-2xl" />
          </button>
        </div>
        <form @submit.prevent="startSession" class="p-6">
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                {{ $t('routing.supervisor.conversationId') }} *
              </label>
              <input
                v-model.number="sessionForm.conversationId"
                type="number"
                required
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                {{ $t('routing.supervisor.sessionType') }} *
              </label>
              <select
                v-model="sessionForm.sessionType"
                required
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              >
                <option value="MONITORING">{{ $t('routing.sessionTypes.monitoring') }}</option>
                <option value="COACHING">{{ $t('routing.sessionTypes.coaching') }}</option>
                <option value="TAKEOVER">{{ $t('routing.sessionTypes.takeover') }}</option>
                <option value="ASSISTING">{{ $t('routing.sessionTypes.assisting') }}</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                {{ $t('routing.supervisor.monitoredAgent') }}
              </label>
              <input
                v-model.number="sessionForm.monitoredAgentId"
                type="number"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              />
            </div>
            <div class="flex items-center">
              <input
                v-model="sessionForm.isSilent"
                type="checkbox"
                id="isSilent"
                class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              />
              <label for="isSilent" class="ml-2 block text-sm text-gray-900 dark:text-white">
                {{ $t('routing.supervisor.silentMode') }}
              </label>
            </div>
            <div class="flex items-center">
              <input
                v-model="sessionForm.canIntervene"
                type="checkbox"
                id="canIntervene"
                class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              />
              <label for="canIntervene" class="ml-2 block text-sm text-gray-900 dark:text-white">
                {{ $t('routing.supervisor.allowIntervention') }}
              </label>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                {{ $t('routing.supervisor.notes') }}
              </label>
              <textarea
                v-model="sessionForm.notes"
                rows="2"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              />
            </div>
          </div>
          <div class="mt-6 flex justify-end space-x-3">
            <button
              type="button"
              @click="closeStartSessionModal"
              class="px-4 py-2 bg-gray-200 dark:bg-gray-600 text-gray-900 dark:text-white rounded-md hover:bg-gray-300 dark:hover:bg-gray-500 transition-colors"
            >
              {{ $t('routing.supervisor.cancel') }}
            </button>
            <button
              type="submit"
              class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
            >
              {{ $t('routing.supervisor.start') }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- End Session Modal -->
    <div v-if="showEndSessionModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow-xl w-full max-w-md mx-4">
        <div class="flex justify-between items-center p-6 border-b border-gray-200 dark:border-gray-700">
          <h2 class="text-xl font-bold text-gray-900 dark:text-white">{{ $t('routing.supervisor.endSession') }}</h2>
          <button
            @click="closeEndSessionModal"
            class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
          >
            <Icon icon="mdi:close" class="text-2xl" />
          </button>
        </div>
        <form @submit.prevent="confirmEndSession" class="p-6">
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                {{ $t('routing.supervisor.notes') }}
              </label>
              <textarea
                v-model="endSessionForm.notes"
                rows="3"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                {{ $t('routing.supervisor.rating') }}
              </label>
              <select
                v-model.number="endSessionForm.rating"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              >
                <option value="">-</option>
                <option value="1">1</option>
                <option value="2">2</option>
                <option value="3">3</option>
                <option value="4">4</option>
                <option value="5">5</option>
              </select>
            </div>
          </div>
          <div class="mt-6 flex justify-end space-x-3">
            <button
              type="button"
              @click="closeEndSessionModal"
              class="px-4 py-2 bg-gray-200 dark:bg-gray-600 text-gray-900 dark:text-white rounded-md hover:bg-gray-300 dark:hover:bg-gray-500 transition-colors"
            >
              {{ $t('routing.supervisor.cancel') }}
            </button>
            <button
              type="submit"
              class="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 transition-colors"
            >
              {{ $t('routing.supervisor.confirmEnd') }}
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

const activeSessions = ref([])
const recentSessions = ref([])
const loading = ref(false)
const loadingHistory = ref(false)
const showStartSessionModal = ref(false)
const showEndSessionModal = ref(false)
const currentSession = ref(null)

const stats = ref({
  activeSessions: 0,
  completedToday: 0,
  totalInterventions: 0,
  avgDuration: 0
})

const sessionForm = ref({
  conversationId: null,
  sessionType: 'MONITORING',
  monitoredAgentId: null,
  isSilent: true,
  canIntervene: false,
  notes: ''
})

const endSessionForm = ref({
  notes: '',
  rating: null
})

const loadActiveSessions = async () => {
  try {
    loading.value = true
    activeSessions.value = await routingApi.getActiveSupervisorSessions()
    stats.value.activeSessions = activeSessions.value.length
  } catch (err) {
    console.error('Load active sessions error:', err)
  } finally {
    loading.value = false
  }
}

const loadRecentSessions = async () => {
  try {
    loadingHistory.value = true
    recentSessions.value = await routingApi.getSupervisorSessions()
    stats.value.completedToday = recentSessions.value.filter(s => s.sessionStatus === 'ENDED').length
    stats.value.totalInterventions = recentSessions.value.reduce((sum, s) => sum + s.interventionCount, 0)
    const avgDuration = recentSessions.value.reduce((sum, s) => sum + (s.durationSeconds || 0), 0) / recentSessions.value.length
    stats.value.avgDuration = Math.round(avgDuration / 60)
  } catch (err) {
    console.error('Load recent sessions error:', err)
  } finally {
    loadingHistory.value = false
  }
}

const openStartSessionModal = () => {
  sessionForm.value = {
    conversationId: null,
    sessionType: 'MONITORING',
    monitoredAgentId: null,
    isSilent: true,
    canIntervene: false,
    notes: ''
  }
  showStartSessionModal.value = true
}

const closeStartSessionModal = () => {
  showStartSessionModal.value = false
}

const startSession = async () => {
  try {
    await routingApi.startSupervisorSession(sessionForm.value)
    closeStartSessionModal()
    await loadActiveSessions()
    await loadRecentSessions()
  } catch (err) {
    console.error('Start session error:', err)
  }
}

const endSession = (session) => {
  currentSession.value = session
  endSessionForm.value = {
    notes: '',
    rating: null
  }
  showEndSessionModal.value = true
}

const closeEndSessionModal = () => {
  showEndSessionModal.value = false
  currentSession.value = null
}

const confirmEndSession = async () => {
  try {
    await routingApi.endSupervisorSession(currentSession.value.id, endSessionForm.value)
    closeEndSessionModal()
    await loadActiveSessions()
    await loadRecentSessions()
  } catch (err) {
    console.error('End session error:', err)
  }
}

const formatDuration = (date) => {
  if (!date) return '-'
  const now = new Date()
  const start = new Date(date)
  const diff = Math.floor((now - start) / 1000 / 60)
  return `${diff}m`
}

const formatDurationSeconds = (seconds) => {
  if (!seconds) return '-'
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}m ${secs}s`
}

onMounted(() => {
  loadActiveSessions()
  loadRecentSessions()
})
</script>
