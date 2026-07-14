<template>
  <div class="agent-management p-4">
    <!-- Header -->
    <div class="mt-2 w-full">
      <div class="lg:flex grid-cols-1 lg:space-y-0 space-y-3 gap-5 justify-between">
        <div>
          <p class="uppercase text-xs text-gray-700 dark:text-gray-400 font-semibold">Admin</p>
          <h1 class="text-2xl text-gray-900 dark:text-gray-200 font-medium">
            Agent Management
          </h1>
        </div>
        <div class="flex gap-2">
          <button
            @click="loadAgents"
            :disabled="loading"
            class="bg-white dark:bg-gray-800 hover:border-gray-200 dark:hover:bg-gray-700 dark:text-white dark:border-gray-700 border rounded py-2 px-5 flex items-center gap-2"
          >
            <Icon icon="mdi:refresh" :class="{'animate-spin': loading}" class="text-lg" />
            Refresh
          </button>
          <button
            @click="showCreateModal = true"
            class="bg-primary border flex gap-2 text-white hover:bg-primary/80 dark:border-gray-700 rounded py-3 px-5"
          >
            <span class="icon text-2xl"><Icon icon="ic:twotone-plus" /></span>
            <span class="text">Add Agent</span>
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
            {{ agents.length }}
          </p>
          <h2 class="font-normal text-gray-400 text-md mt-1">Total Agents</h2>
          <div class="flex items-center mt-2">
            <span class="text-gray-400 text-sm">All registered agents</span>
          </div>
        </div>
      </div>

      <!-- Online Agents -->
      <div class="card bg-white dark:bg-gray-800 w-full rounded-md p-5 border dark:border-gray-700 flex">
        <div class="p-2 max-w-sm">
          <div class="bg-green-200 rounded-full w-14 h-14 text-lg p-3 text-green-600 mx-auto">
            <Icon icon="mdi:account-check" class="text-2xl" />
          </div>
        </div>
        <div class="block p-2 w-full">
          <p class="font-semibold text-gray-900 dark:text-gray-200 text-xl">
            {{ onlineAgentsCount }}
          </p>
          <h2 class="font-normal text-gray-400 text-md mt-1">Online Agents</h2>
          <div class="flex items-center mt-2">
            <span :class="onlineAgentsCount > 0 ? 'text-green-500' : 'text-gray-400'" class="text-sm flex items-center">
              <Icon :icon="onlineAgentsCount > 0 ? 'mdi:check-circle' : 'mdi:information'" class="mr-1" />
              {{ onlineAgentsCount > 0 ? 'Active now' : 'No agents online' }}
            </span>
          </div>
        </div>
      </div>

      <!-- Available Agents -->
      <div class="card bg-white dark:bg-gray-800 w-full rounded-md p-5 border dark:border-gray-700 flex">
        <div class="p-2 max-w-sm">
          <div class="bg-purple-200 rounded-full w-14 h-14 text-lg p-3 text-purple-600 mx-auto">
            <Icon icon="mdi:account-circle" class="text-2xl" />
          </div>
        </div>
        <div class="block p-2 w-full">
          <p class="font-semibold text-gray-900 dark:text-gray-200 text-xl">
            {{ availableAgentsCount }}
          </p>
          <h2 class="font-normal text-gray-400 text-md mt-1">Available Agents</h2>
          <div class="flex items-center mt-2">
            <span :class="availableAgentsCount > 0 ? 'text-green-500' : 'text-gray-400'" class="text-sm flex items-center">
              <Icon :icon="availableAgentsCount > 0 ? 'mdi:check-circle' : 'mdi:information'" class="mr-1" />
              {{ availableAgentsCount > 0 ? 'Ready for assignments' : 'All agents busy' }}
            </span>
          </div>
        </div>
      </div>

      <!-- Avg Load -->
      <div class="card bg-white dark:bg-gray-800 w-full rounded-md p-5 border dark:border-gray-700 flex">
        <div class="p-2 max-w-sm">
          <div class="bg-orange-200 rounded-full w-14 h-14 text-lg p-3 text-orange-600 mx-auto">
            <Icon icon="mdi:chart-line" class="text-2xl" />
          </div>
        </div>
        <div class="block p-2 w-full">
          <p class="font-semibold text-gray-900 dark:text-gray-200 text-xl">
            {{ avgLoad }}%
          </p>
          <h2 class="font-normal text-gray-400 text-md mt-1">Avg Load</h2>
          <div class="flex items-center mt-2">
            <span :class="avgLoad < 50 ? 'text-green-500' : avgLoad < 80 ? 'text-yellow-500' : 'text-red-500'" class="text-sm flex items-center">
              <Icon :icon="avgLoad < 50 ? 'mdi:check-circle' : avgLoad < 80 ? 'mdi:alert' : 'mdi:alert-circle'" class="mr-1" />
              {{ avgLoad < 50 ? 'Good load' : avgLoad < 80 ? 'Moderate load' : 'High load' }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Agents List -->
    <div v-if="loading" class="p-8 text-center">
      <Icon icon="mdi:loading" class="animate-spin text-6xl text-gray-300 dark:text-gray-600 mx-auto" />
      <p class="mt-2 text-gray-500 dark:text-gray-400">Loading agents...</p>
    </div>

    <div v-else-if="agents.length === 0" class="p-8 text-center">
      <Icon icon="mdi:account-off" class="text-6xl text-gray-300 dark:text-gray-600 mx-auto" />
      <p class="mt-2 text-sm text-gray-500 dark:text-gray-400">No agents found.</p>
      <button
        @click="showCreateModal = true"
        class="mt-4 px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/80"
      >
        Add First Agent
      </button>
    </div>

    <div v-else class="space-y-4">
      <div
        v-for="agent in agents"
        :key="agent.id"
        :class="[
          'p-4 border rounded-lg',
          agent.active ? 'border-blue-200 bg-blue-50 dark:bg-blue-900/20 dark:border-blue-800' : 'border-gray-200 bg-gray-50 dark:bg-gray-700 dark:border-gray-600'
        ]"
      >
        <div class="flex justify-between items-start">
          <div class="flex-1">
            <div class="flex items-center space-x-2">
              <div class="flex items-center justify-center w-10 h-10 rounded-full bg-blue-600 text-white font-bold">
                {{ agent.name.charAt(0).toUpperCase() }}
              </div>
              <div>
                <h3 class="font-semibold text-gray-900 dark:text-gray-200">{{ agent.name }}</h3>
                <p class="text-sm text-gray-600 dark:text-gray-400">{{ agent.email }}</p>
              </div>
              <span
                :class="[
                  'px-2 py-1 text-xs rounded-full',
                  agent.status === 'ONLINE' ? 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400' : 'bg-gray-100 text-gray-600 dark:bg-gray-700 dark:text-gray-400'
                ]"
              >
                {{ agent.status }}
              </span>
              <span
                :class="[
                  'px-2 py-1 text-xs rounded-full',
                  agent.active ? 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400' : 'bg-gray-100 text-gray-600 dark:bg-gray-700 dark:text-gray-400'
                ]"
              >
                {{ agent.active ? 'Active' : 'Inactive' }}
              </span>
              <span class="px-2 py-1 text-xs bg-purple-100 text-purple-800 dark:bg-purple-900/30 dark:text-purple-400 rounded-full">
                {{ agent.role }}
              </span>
            </div>
            <div class="mt-3 grid grid-cols-4 gap-4 text-sm">
              <div>
                <span class="font-medium text-gray-700 dark:text-gray-300">Current Load:</span>
                <span class="ml-2 text-blue-600 dark:text-blue-400 font-semibold">{{ agent.currentLoad }}/{{ agent.maxConcurrentConversations }}</span>
              </div>
              <div>
                <span class="font-medium text-gray-700 dark:text-gray-300">Skills:</span>
                <span class="ml-2 text-gray-600 dark:text-gray-400">{{ agent.skills?.join(', ') || 'None' }}</span>
              </div>
              <div>
                <span class="font-medium text-gray-700 dark:text-gray-300">Last Activity:</span>
                <span class="ml-2 text-gray-600 dark:text-gray-400">{{ formatDate(agent.lastActivityAt) }}</span>
              </div>
              <div>
                <span class="font-medium text-gray-700 dark:text-gray-300">Created:</span>
                <span class="ml-2 text-gray-600 dark:text-gray-400">{{ formatDate(agent.createdAt) }}</span>
              </div>
            </div>
          </div>
          <div class="flex space-x-2 ml-4">
            <button
              @click="setAgentOnline(agent.id)"
              :disabled="agent.status === 'ONLINE'"
              class="p-2 text-green-600 hover:bg-green-100 dark:hover:bg-green-900/30 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              title="Set Online"
            >
              <Icon icon="mdi:check-circle" class="text-lg" />
            </button>
            <button
              @click="setAgentOffline(agent.id)"
              :disabled="agent.status === 'OFFLINE'"
              class="p-2 text-gray-600 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              title="Set Offline"
            >
              <Icon icon="mdi:close-circle" class="text-lg" />
            </button>
            <button
              @click="editAgent(agent)"
              class="p-2 text-blue-600 hover:bg-blue-100 dark:hover:bg-blue-900/30 rounded-lg transition-colors"
              title="Edit"
            >
              <Icon icon="mdi:pencil" class="text-lg" />
            </button>
            <button
              @click="deleteAgent(agent.id)"
              class="p-2 text-red-600 hover:bg-red-100 dark:hover:bg-red-900/30 rounded-lg transition-colors"
              title="Delete"
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
          {{ showCreateModal ? 'Create Agent' : 'Edit Agent' }}
        </h2>
        <form @submit.prevent="saveAgent">
          <div class="space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Name</label>
                <input
                  v-model="currentAgent.name"
                  type="text"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded-lg focus:ring-2 focus:ring-blue-500"
                  required
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Email</label>
                <input
                  v-model="currentAgent.email"
                  type="email"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded-lg focus:ring-2 focus:ring-blue-500"
                  required
                />
              </div>
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Role</label>
                <select
                  v-model="currentAgent.role"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded-lg focus:ring-2 focus:ring-blue-500"
                  required
                >
                  <option value="AGENT">Agent</option>
                  <option value="TEAM_LEAD">Team Lead</option>
                  <option value="SUPERVISOR">Supervisor</option>
                  <option value="ADMIN">Admin</option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Status</label>
                <select
                  v-model="currentAgent.status"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded-lg focus:ring-2 focus:ring-blue-500"
                  required
                >
                  <option value="ONLINE">Online</option>
                  <option value="OFFLINE">Offline</option>
                  <option value="AWAY">Away</option>
                  <option value="BUSY">Busy</option>
                </select>
              </div>
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Max Concurrent Conversations</label>
                <input
                  v-model.number="currentAgent.maxConcurrentConversations"
                  type="number"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded-lg focus:ring-2 focus:ring-blue-500"
                  required
                  min="1"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Current Load</label>
                <input
                  v-model.number="currentAgent.currentLoad"
                  type="number"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded-lg focus:ring-2 focus:ring-blue-500"
                  required
                  min="0"
                />
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Skills (comma-separated)</label>
              <input
                v-model="skillsString"
                type="text"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded-lg focus:ring-2 focus:ring-blue-500"
                placeholder="e.g., vip_support, technical_support, billing"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Bio</label>
              <textarea
                v-model="currentAgent.bio"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded-lg focus:ring-2 focus:ring-blue-500"
                rows="2"
              />
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Phone Number</label>
                <input
                  v-model="currentAgent.phoneNumber"
                  type="text"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded-lg focus:ring-2 focus:ring-blue-500"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Avatar URL</label>
                <input
                  v-model="currentAgent.avatarUrl"
                  type="text"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded-lg focus:ring-2 focus:ring-blue-500"
                />
              </div>
            </div>
            <div class="flex items-center">
              <input
                v-model="currentAgent.active"
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
import agentApi from '@/api/agentApi'

const loading = ref(false)
const agents = ref([])
const showCreateModal = ref(false)
const showEditModal = ref(false)
const currentAgent = ref({
  name: '',
  email: '',
  role: 'AGENT',
  status: 'OFFLINE',
  maxConcurrentConversations: 5,
  currentLoad: 0,
  skills: [],
  bio: '',
  phoneNumber: '',
  avatarUrl: '',
  active: true
})
const skillsString = ref('')

const onlineAgentsCount = computed(() => {
  return agents.value.filter(a => a.status === 'ONLINE').length
})

const availableAgentsCount = computed(() => {
  return agents.value.filter(a => a.status === 'ONLINE' && a.active && a.currentLoad < a.maxConcurrentConversations).length
})

const avgLoad = computed(() => {
  if (agents.value.length === 0) return 0
  const totalLoad = agents.value.reduce((sum, a) => {
    const max = a.maxConcurrentConversations || 1
    const current = a.currentLoad || 0
    return sum + ((current / max) * 100)
  }, 0)
  return Math.round(totalLoad / agents.value.length)
})

const loadAgents = async () => {
  loading.value = true
  try {
    const response = await agentApi.getAgents()
    agents.value = response.data || []
  } catch (error) {
    console.error('Error loading agents:', error)
  } finally {
    loading.value = false
  }
}

const setAgentOnline = async (id) => {
  try {
    await agentApi.setAgentOnline(id)
    await loadAgents()
  } catch (error) {
    console.error('Error setting agent online:', error)
  }
}

const setAgentOffline = async (id) => {
  try {
    await agentApi.setAgentOffline(id)
    await loadAgents()
  } catch (error) {
    console.error('Error setting agent offline:', error)
  }
}

const editAgent = (agent) => {
  currentAgent.value = { ...agent }
  skillsString.value = agent.skills?.join(', ') || ''
  showEditModal.value = true
}

const deleteAgent = async (id) => {
  if (confirm('Are you sure you want to delete this agent?')) {
    try {
      await agentApi.deleteAgent(id)
      await loadAgents()
    } catch (error) {
      console.error('Error deleting agent:', error)
    }
  }
}

const saveAgent = async () => {
  try {
    currentAgent.value.skills = skillsString.value.split(',').map(s => s.trim()).filter(s => s)
    
    if (showCreateModal.value) {
      await agentApi.createAgent(currentAgent.value)
    } else {
      await agentApi.updateAgent(currentAgent.value.id, currentAgent.value)
    }
    
    closeModal()
    await loadAgents()
  } catch (error) {
    console.error('Error saving agent:', error)
    alert('Error saving agent. Please check your input.')
  }
}

const closeModal = () => {
  showCreateModal.value = false
  showEditModal.value = false
  currentAgent.value = {
    name: '',
    email: '',
    role: 'AGENT',
    status: 'OFFLINE',
    maxConcurrentConversations: 5,
    currentLoad: 0,
    skills: [],
    bio: '',
    phoneNumber: '',
    avatarUrl: '',
    active: true
  }
  skillsString.value = ''
}

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleString()
}

onMounted(() => {
  loadAgents()
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
