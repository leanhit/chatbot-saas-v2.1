<template>
  <div class="agent-management p-4">
    <!-- Header -->
    <div class="mt-2 w-full">
      <div class="lg:flex grid-cols-1 lg:space-y-0 space-y-3 gap-5 justify-between">
        <div>
          <p class="uppercase text-xs text-gray-700 dark:text-gray-400 font-semibold">Tenant</p>
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
        </div>
      </div>
    </div>

    <!-- Info Banner -->
    <div class="mt-4 flex items-start gap-3 p-4 bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg">
      <Icon icon="mdi:information-outline" class="text-blue-500 text-xl mt-0.5 flex-shrink-0" />
      <div>
        <p class="text-sm font-medium text-blue-800 dark:text-blue-300">Agent được tạo tự động</p>
        <p class="text-xs text-blue-600 dark:text-blue-400 mt-0.5">
          Agent profile sẽ được khởi tạo tự động khi một thành viên tham gia tenant (accept invitation hoặc được duyệt join request).
          Tại đây bạn chỉ cần chỉnh sửa thông số của từng agent.
        </p>
      </div>
    </div>

    <!-- Leaderboard Panel (OWNER only) -->
    <div v-if="isOwner" class="mt-6 bg-white dark:bg-gray-800 rounded-lg p-6 border dark:border-gray-700">
      <div class="flex justify-between items-center mb-4">
        <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-200 flex items-center gap-2">
          <Icon icon="mdi:trophy" class="text-yellow-500 text-xl" />
          {{ t('agentStats.leaderboard.title') }}
        </h3>
        <div class="flex gap-2">
          <button
            v-for="p in ['7d', '30d', '90d']"
            :key="p"
            @click="changeLeaderboardPeriod(p)"
            :class="[
              'px-3 py-1 rounded-lg text-sm font-medium transition',
              leaderboardPeriod === p
                ? 'bg-blue-600 text-white'
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200 dark:bg-gray-700 dark:text-gray-300 dark:hover:bg-gray-600'
            ]"
          >
            {{ t(`agentStats.period.${p}`) }}
          </button>
        </div>
      </div>
      <div v-if="loadingLeaderboard" class="text-center py-4">
        <Icon icon="mdi:loading" class="animate-spin text-2xl text-gray-400" />
      </div>
      <div v-else-if="leaderboard.length === 0" class="text-center py-4 text-gray-500 dark:text-gray-400">
        {{ t('agentStats.leaderboard.noData') }}
      </div>
      <div v-else class="overflow-x-auto">
        <table class="w-full">
          <thead>
            <tr class="border-b border-gray-200 dark:border-gray-700">
              <th class="text-left py-2 px-3 text-sm font-medium text-gray-700 dark:text-gray-300">{{ t('agentStats.leaderboard.rank') }}</th>
              <th class="text-left py-2 px-3 text-sm font-medium text-gray-700 dark:text-gray-300">{{ t('agentStats.leaderboard.agent') }}</th>
              <th class="text-right py-2 px-3 text-sm font-medium text-gray-700 dark:text-gray-300">{{ t('agentStats.leaderboard.resolvedCount') }}</th>
              <th class="text-right py-2 px-3 text-sm font-medium text-gray-700 dark:text-gray-300">{{ t('agentStats.leaderboard.resolutionRate') }}</th>
              <th class="text-right py-2 px-3 text-sm font-medium text-gray-700 dark:text-gray-300">{{ t('agentStats.leaderboard.csat') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="(agent, index) in leaderboard"
              :key="agent.agentId"
              class="border-b border-gray-100 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700/50"
            >
              <td class="py-2 px-3">
                <span v-if="index === 0" class="text-2xl">🥇</span>
                <span v-else-if="index === 1" class="text-2xl">🥈</span>
                <span v-else-if="index === 2" class="text-2xl">🥉</span>
                <span v-else class="text-gray-600 dark:text-gray-400">{{ index + 1 }}</span>
              </td>
              <td class="py-2 px-3 font-medium text-gray-900 dark:text-gray-200">{{ agent.agentName }}</td>
              <td class="py-2 px-3 text-right text-gray-700 dark:text-gray-300">{{ agent.resolvedCount }}</td>
              <td class="py-2 px-3 text-right text-gray-700 dark:text-gray-300">{{ agent.resolutionRate?.toFixed(1) }}%</td>
              <td class="py-2 px-3 text-right">
                <span class="flex items-center justify-end gap-1">
                  <Icon icon="mdi:star" class="text-yellow-500" />
                  <span class="text-gray-700 dark:text-gray-300">{{ agent.csatScore?.toFixed(1) || 'N/A' }}</span>
                </span>
              </td>
            </tr>
          </tbody>
        </table>
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
            {{ totalElements }}
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
      <p class="mt-2 text-sm text-gray-500 dark:text-gray-400">Chưa có agent nào.</p>
      <p class="mt-1 text-xs text-gray-400 dark:text-gray-500">
        Agent sẽ được tạo tự động khi thành viên tham gia tenant.
      </p>
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
              v-if="isOwner"
              @click="viewStats(agent)"
              class="p-2 text-purple-600 hover:bg-purple-100 dark:hover:bg-purple-900/30 rounded-lg transition-colors"
              :title="t('agentStats.viewStats')"
            >
              <Icon icon="mdi:chart-bar" class="text-lg" />
            </button>
            <button
              v-if="canChangeStatus(agent)"
              @click="setAgentOnline(agent.id)"
              :disabled="agent.status === 'ONLINE'"
              class="p-2 text-green-600 hover:bg-green-100 dark:hover:bg-green-900/30 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              title="Set Online"
            >
              <Icon icon="mdi:check-circle" class="text-lg" />
            </button>
            <button
              v-if="canChangeStatus(agent)"
              @click="setAgentOffline(agent.id)"
              :disabled="agent.status === 'OFFLINE'"
              class="p-2 text-gray-600 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              title="Set Offline"
            >
              <Icon icon="mdi:close-circle" class="text-lg" />
            </button>
            <button
              v-if="canEditAgent(agent)"
              @click="editAgent(agent)"
              class="p-2 text-blue-600 hover:bg-blue-100 dark:hover:bg-blue-900/30 rounded-lg transition-colors"
              title="Edit"
            >
              <Icon icon="mdi:pencil" class="text-lg" />
            </button>
            <button
              v-if="canEditAgent(agent)"
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

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="mt-6 flex justify-between items-center">
      <div class="text-sm text-gray-600 dark:text-gray-400">
        Showing {{ currentPage * pageSize + 1 }} to {{ Math.min((currentPage + 1) * pageSize, totalElements) }} of {{ totalElements }} agents
      </div>
      <div class="flex space-x-2">
        <button
          @click="goToPage(currentPage - 1)"
          :disabled="currentPage === 0"
          class="px-4 py-2 bg-white dark:bg-gray-800 border dark:border-gray-700 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed text-gray-700 dark:text-gray-300"
        >
          Previous
        </button>
        <span class="px-4 py-2 text-gray-700 dark:text-gray-300">
          Page {{ currentPage + 1 }} of {{ totalPages }}
        </span>
        <button
          @click="goToPage(currentPage + 1)"
          :disabled="currentPage >= totalPages - 1"
          class="px-4 py-2 bg-white dark:bg-gray-800 border dark:border-gray-700 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed text-gray-700 dark:text-gray-300"
        >
          Next
        </button>
      </div>
    </div>

    <!-- Edit Modal -->
    <div
      v-if="showEditModal"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
    >
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto border dark:border-gray-700">
        <h2 class="text-xl font-bold mb-4 text-gray-900 dark:text-gray-200">
          Edit Agent
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
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded-lg focus:ring-2 focus:ring-blue-500 opacity-60 cursor-not-allowed"
                  disabled
                />
                <p class="text-xs text-gray-400 mt-1">Email liên kết với user account, không thể thay đổi.</p>
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

    <!-- Agent Stats Modal -->
    <AgentStatsModal
      :is-open="showStatsModal"
      :agent-id="selectedAgentForStats?.id"
      :agent-name="selectedAgentForStats?.name"
      @close="showStatsModal = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Icon } from '@iconify/vue'
import agentApi from '@/api/agentApi'
import { useToast } from '@/composables/useToast'
import { useAuthStore } from '@/stores/authStore'
import { useGatewayTenantStore } from '@/stores/tenant/gateway/myTenantStore'
import AgentStatsModal from '@/components/AgentStatsModal.vue'

const { t } = useI18n()

const toast = useToast()
const authStore = useAuthStore()
const tenantStore = useGatewayTenantStore()

const loading = ref(false)
const agents = ref([])
const showEditModal = ref(false)
const currentAgent = ref({})
const skillsString = ref('')

// Stats modal state
const showStatsModal = ref(false)
const selectedAgentForStats = ref(null)
const leaderboardPeriod = ref('7d')
const leaderboard = ref([])
const loadingLeaderboard = ref(false)

// Pagination state
const currentPage = ref(0)
const pageSize = ref(20)
const totalPages = ref(0)
const totalElements = ref(0)
const sortBy = ref('createdAt')
const sortDir = ref('desc')

// Permission checks
const isOwner = computed(() => {
  return tenantStore.currentTenant?.role === 'OWNER'
})

const currentUserId = computed(() => {
  return authStore.userId
})

const canEditAgent = (agent) => {
  return isOwner.value
}

const canChangeStatus = (agent) => {
  // OWNER or the agent themselves
  return isOwner.value || (agent.userId && agent.userId === currentUserId.value)
}

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
    const response = await agentApi.getAgents(currentPage.value, pageSize.value, sortBy.value, sortDir.value)
    agents.value = response.data.content || []
    totalPages.value = response.data.totalPages || 0
    totalElements.value = response.data.totalElements || 0
  } catch (error) {
    console.error('Error loading agents:', error)
    toast.error(error, 'Error loading agents')
  } finally {
    loading.value = false
  }
}

const setAgentOnline = async (id) => {
  try {
    await agentApi.setAgentOnline(id)
    toast.success('Agent status set to online')
    await loadAgents()
  } catch (error) {
    console.error('Error setting agent online:', error)
    toast.error(error, 'Error setting agent online')
  }
}

const setAgentOffline = async (id) => {
  try {
    await agentApi.setAgentOffline(id)
    toast.success('Agent status set to offline')
    await loadAgents()
  } catch (error) {
    console.error('Error setting agent offline:', error)
    toast.error(error, 'Error setting agent offline')
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
      toast.success('Agent deleted successfully')
      await loadAgents()
    } catch (error) {
      console.error('Error deleting agent:', error)
      toast.error(error, 'Error deleting agent')
    }
  }
}

const saveAgent = async () => {
  try {
    currentAgent.value.skills = skillsString.value.split(',').map(s => s.trim()).filter(s => s)
    await agentApi.updateAgent(currentAgent.value.id, currentAgent.value)
    toast.success('Agent updated successfully')
    closeModal()
    await loadAgents()
  } catch (error) {
    console.error('Error saving agent:', error)
    toast.error(error, 'Error saving agent. Please check your input.')
  }
}

const closeModal = () => {
  showEditModal.value = false
  currentAgent.value = {}
  skillsString.value = ''
}

const goToPage = (page) => {
  if (page >= 0 && page < totalPages.value) {
    currentPage.value = page
    loadAgents()
  }
}

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleString()
}

// Leaderboard functions
const loadLeaderboard = async () => {
  if (!isOwner.value) return
  loadingLeaderboard.value = true
  try {
    const response = await agentApi.getAgentLeaderboard(leaderboardPeriod.value)
    leaderboard.value = response.data || []
  } catch (error) {
    console.error('Error loading leaderboard:', error)
    toast.error(error, 'Error loading leaderboard')
  } finally {
    loadingLeaderboard.value = false
  }
}

const changeLeaderboardPeriod = (period) => {
  leaderboardPeriod.value = period
  loadLeaderboard()
}

// Stats modal functions
const viewStats = (agent) => {
  selectedAgentForStats.value = agent
  showStatsModal.value = true
}

onMounted(() => {
  loadAgents()
  if (isOwner.value) {
    loadLeaderboard()
  }
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
