<template>
  <div class="penny-dashboard p-6">
    <!-- Header -->
    <div class="mb-8">
      <div class="flex justify-between items-center">
        <div>
          <h1 class="text-3xl font-bold text-gray-900 dark:text-gray-100 mb-2">
            Penny Bot Management
          </h1>
          <p class="text-gray-600 dark:text-gray-400">
            Manage your AI-powered customer service bots
          </p>
        </div>
        
        <div class="flex gap-3">
          <button 
            @click="showCreateBotModal = true"
            class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center gap-2"
          >
            <Icon icon="ic:baseline-add" />
            Create Bot
          </button>
          <button 
            @click="refreshBots"
            :disabled="isLoading"
            class="px-4 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors flex items-center gap-2"
          >
            <Icon v-if="!isLoading" icon="ic:baseline-refresh" />
            <Icon v-else icon="ic:baseline-refresh" class="animate-spin" />
            Refresh
          </button>
        </div>
      </div>
    </div>

    <!-- Stats Overview -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <!-- Total Bots -->
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700">
        <div class="flex items-center gap-3">
          <div class="p-3 bg-blue-100 dark:bg-blue-900/20 rounded-lg">
            <Icon icon="ic:baseline-robot" class="text-blue-600 dark:text-blue-400 text-2xl" />
          </div>
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Total Bots</p>
            <p class="text-2xl font-bold text-gray-900 dark:text-gray-100">{{ totalBots }}</p>
          </div>
        </div>
      </div>

      <!-- Active Bots -->
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700">
        <div class="flex items-center gap-3">
          <div class="p-3 bg-green-100 dark:bg-green-900/20 rounded-lg">
            <Icon icon="ic:baseline-check-circle" class="text-green-600 dark:text-green-400 text-2xl" />
          </div>
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Active Bots</p>
            <p class="text-2xl font-bold text-green-600 dark:text-green-400">{{ activeBots }}</p>
          </div>
        </div>
      </div>

      <!-- Inactive Bots -->
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700">
        <div class="flex items-center gap-3">
          <div class="p-3 bg-gray-100 dark:bg-gray-700 rounded-lg">
            <Icon icon="ic:baseline-pause-circle" class="text-gray-600 dark:text-gray-400 text-2xl" />
          </div>
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Inactive Bots</p>
            <p class="text-2xl font-bold text-gray-600 dark:text-gray-400">{{ inactiveBots }}</p>
          </div>
        </div>
      </div>

      <!-- Total Messages -->
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700">
        <div class="flex items-center gap-3">
          <div class="p-3 bg-purple-100 dark:bg-purple-900/20 rounded-lg">
            <Icon icon="ic:baseline-chat" class="text-purple-600 dark:text-purple-400 text-2xl" />
          </div>
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Total Messages</p>
            <p class="text-2xl font-bold text-gray-900 dark:text-gray-100">{{ totalMessages }}</p>
          </div>
        </div>
      </div>

      <!-- Response Time -->
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700">
        <div class="flex items-center gap-3">
          <div class="p-3 bg-orange-100 dark:bg-orange-900/20 rounded-lg">
            <Icon icon="ic:baseline-speed" class="text-orange-600 dark:text-orange-400 text-2xl" />
          </div>
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Avg Response Time</p>
            <p class="text-2xl font-bold text-gray-900 dark:text-gray-100">{{ averageResponseTime }}ms</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Bot Type Distribution -->
    <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700 mb-8">
      <h2 class="text-xl font-semibold text-gray-900 dark:text-gray-100 mb-4">Bot Distribution</h2>
      <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
        <div 
          v-for="(type, count) in Object.entries(botsByType)" 
          :key="type"
          class="text-center"
        >
          <div class="p-4 bg-gray-50 dark:bg-gray-700 rounded-lg">
            <div :class="getBotTypeIconClass(type)" class="text-3xl mb-2">
              <Icon :icon="getBotTypeIcon(type)" />
            </div>
            <p class="text-sm font-medium text-gray-900 dark:text-gray-100">{{ getBotTypeDisplayName(type) }}</p>
            <p class="text-2xl font-bold text-gray-600 dark:text-gray-400">{{ count.length }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Search and Filters -->
    <div class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-6 mb-8">
      <div class="flex flex-col lg:flex-row gap-4">
        <!-- Search -->
        <div class="flex-1">
          <div class="relative">
            <Icon icon="ic:baseline-search" class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
            <input 
              v-model="searchQuery"
              type="text"
              placeholder="Search bots..."
              class="w-full pl-10 pr-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
        </div>

        <!-- Bot Type Filter -->
        <select 
          v-model="selectedBotType"
          class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        >
          <option value="">All Types</option>
          <option value="CUSTOMER_SERVICE">Customer Service</option>
          <option value="SALES">Sales</option>
          <option value="SUPPORT">Technical Support</option>
          <option value="MARKETING">Marketing</option>
          <option value="HR">Human Resources</option>
          <option value="FINANCE">Finance</option>
          <option value="GENERAL">General Purpose</option>
        </select>

        <!-- Status Filter -->
        <select 
          v-model="statusFilter"
          class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        >
          <option value="">All Status</option>
          <option value="active">Active</option>
          <option value="inactive">Inactive</option>
          <option value="enabled">Enabled</option>
          <option value="disabled">Disabled</option>
        </select>

        <!-- Sort Options -->
        <select 
          v-model="sortBy"
          class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        >
          <option value="createdAt">Created Date</option>
          <option value="updatedAt">Updated Date</option>
          <option value="lastUsedAt">Last Used</option>
          <option value="botName">Bot Name</option>
        </select>

        <!-- Clear Filters -->
        <button 
          @click="resetFilters"
          class="px-4 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors"
        >
          Clear Filters
        </button>
      </div>
    </div>

    <!-- Bots Grid -->
    <div v-if="isLoading" class="text-center py-12">
      <Icon icon="ic:baseline-refresh" class="animate-spin text-4xl text-gray-400" />
      <p class="text-gray-600 dark:text-gray-400 mt-2">Loading bots...</p>
    </div>

    <div v-else-if="paginatedBots.length === 0" class="text-center py-12">
      <Icon icon="ic:baseline-robot" class="text-4xl text-gray-400" />
      <h3 class="text-lg font-medium text-gray-900 dark:text-gray-100 mt-4">
        No bots found
      </h3>
      <p class="text-gray-600 dark:text-gray-400 mt-2">
        {{ searchQuery ? 'Try adjusting your search' : 'Create your first Penny bot to get started' }}
      </p>
      <button 
        v-if="!searchQuery"
        @click="showCreateBotModal = true"
        class="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
      >
        Create Your First Bot
      </button>
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div 
        v-for="bot in paginatedBots" 
        :key="bot.id"
        class="bot-card bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 hover:shadow-lg transition-all duration-200 hover:scale-[1.02] cursor-pointer"
        @click="selectBot(bot)"
      >
        <!-- Card Header -->
        <div class="p-6 border-b border-gray-200 dark:border-gray-700">
          <div class="flex justify-between items-start mb-4">
            <div class="flex items-center gap-3">
              <div :class="getBotTypeIconClass(bot.botType)" class="p-2 rounded-lg">
                <Icon :icon="getBotTypeIcon(bot.botType)" class="text-xl" />
              </div>
              <div>
                <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100">
                  {{ bot.botName }}
                </h3>
                <p class="text-sm text-gray-600 dark:text-gray-400">
                  {{ getBotTypeDisplayName(bot.botType) }}
                </p>
              </div>
            </div>
            
            <!-- Status Badge -->
            <span :class="getStatusBadgeClass(bot)" class="px-2 py-1 rounded-full text-xs font-medium">
              {{ getStatusText(bot) }}
            </span>
          </div>
        </div>

        <!-- Card Body -->
        <div class="p-6">
          <!-- Description -->
          <div class="mb-4">
            <p class="text-sm text-gray-600 dark:text-gray-400 line-clamp-2">
              {{ bot.description || 'No description available' }}
            </p>
          </div>

          <!-- Bot Info -->
          <div class="space-y-2 text-sm text-gray-600 dark:text-gray-400 mb-4">
            <div class="flex justify-between">
              <span>Botpress ID:</span>
              <span class="font-mono text-gray-900 dark:text-gray-100">{{ bot.botpressBotId || 'Not connected' }}</span>
            </div>
            <div class="flex justify-between">
              <span>Created:</span>
              <span>{{ formatDate(bot.createdAt) }}</span>
            </div>
            <div class="flex justify-between">
              <span>Last Used:</span>
              <span>{{ bot.lastUsedAt ? formatDate(bot.lastUsedAt) : 'Never' }}</span>
            </div>
          </div>

          <!-- Quick Actions -->
          <div class="flex gap-2">
            <button 
              @click="chatWithBot(bot)"
              class="flex-1 px-3 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center justify-center gap-2"
            >
              <Icon icon="ic:baseline-chat" />
              Chat
            </button>
            <button 
              @click="viewBotDetails(bot)"
              class="flex-1 px-3 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors flex items-center justify-center gap-2"
            >
              <Icon icon="ic:baseline-visibility" />
              Details
            </button>
          </div>
        </div>

        <!-- Status Indicators -->
        <div class="flex gap-4 mt-4 pt-4 border-t border-gray-200 dark:border-gray-700">
          <div class="flex items-center gap-2">
            <Icon :icon="bot.isActive ? 'ic:baseline-check-circle' : 'ic:baseline-pause-circle'" 
                   :class="bot.isActive ? 'text-green-600 dark:text-green-400' : 'text-gray-400'" />
            <span class="text-sm text-gray-600 dark:text-gray-400">
              {{ bot.isActive ? 'Active' : 'Inactive' }}
            </span>
          </div>
          <div class="flex items-center gap-2">
            <Icon :icon="bot.isEnabled ? 'ic:baseline-toggle-on' : 'ic:baseline-toggle-off'" 
                   :class="bot.isEnabled ? 'text-green-600 dark:text-green-400' : 'text-gray-400'" />
            <span class="text-sm text-gray-600 dark:text-gray-400">
              {{ bot.isEnabled ? 'Enabled' : 'Disabled' }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="flex justify-between items-center p-6">
      <div class="text-sm text-gray-700 dark:text-gray-300">
        Showing {{ (currentPage - 1) * pageSize + 1 }} to {{ Math.min(currentPage * pageSize, filteredBots.length) }} of {{ filteredBots.length }} bots
      </div>
      <div class="flex gap-2">
        <button 
          @click="currentPage = Math.max(1, currentPage - 1)"
          :disabled="currentPage === 1"
          class="px-3 py-1 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          Previous
        </button>
        <span class="px-3 py-1 text-gray-700 dark:text-gray-300">
          {{ currentPage }} / {{ totalPages }}
        </span>
        <button 
          @click="currentPage = Math.min(totalPages, currentPage + 1)"
          :disabled="currentPage === totalPages"
          class="px-3 py-1 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          Next
        </button>
      </div>
    </div>

    <!-- Create Bot Modal -->
    <CreateBotModal 
      v-if="showCreateBotModal"
      @close="showCreateBotModal = false"
      @created="onBotCreated"
    />

    <!-- Bot Details Modal -->
    <BotDetailsModal 
      v-if="showDetailsModal"
      :bot="selectedBot"
      @close="showDetailsModal = false"
      @updated="onBotUpdated"
      @deleted="onBotDeleted"
      @toggled="onBotToggled"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { usePennyBotStore } from '@/stores/penny/pennyBotStore'
import { useRouter } from 'vue-router'
import CreateBotModal from '@/components/penny/CreateBotModal.vue'
import BotDetailsModal from '@/components/penny/BotDetailsModal.vue'

const router = useRouter()
const pennyBotStore = usePennyBotStore()

// State
const showCreateBotModal = ref(false)
const showDetailsModal = ref(false)

// Computed
const {
  bots,
  currentBot,
  isLoading,
  error,
  searchQuery,
  statusFilter,
  currentPage,
  pageSize,
  filteredBots,
  activeBots,
  inactiveBots,
  totalBots,
  totalPages,
  paginatedBots,
  botsByType
} = pennyBotStore

// Mock data for demonstration
const totalMessages = ref(12543)
const averageResponseTime = ref(850)

// Methods
const refreshBots = () => {
  pennyBotStore.fetchBots()
}

const selectBot = (bot) => {
  pennyBotStore.setCurrentBot(bot)
  router.push(`/penny/bots/${bot.id}`)
}

const viewBotDetails = (bot) => {
  pennyBotStore.setCurrentBot(bot)
  showDetailsModal.value = true
}

const chatWithBot = (bot) => {
  router.push(`/penny/chat/${bot.id}`)
}

const resetFilters = () => {
  pennyBotStore.resetFilters()
}

const onBotCreated = (bot) => {
  showCreateBotModal.value = false
  // Show success message
  console.log('Bot created:', bot)
}

const onBotUpdated = (bot) => {
  showDetailsModal.value = false
  // Show success message
  console.log('Bot updated:', bot)
}

const onBotDeleted = (bot) => {
  showDetailsModal.value = false
  // Show success message
  console.log('Bot deleted:', bot)
}

const onBotToggled = (bot) => {
  // Show success message
  console.log('Bot status toggled:', bot)
}

// Helper methods from store
const getBotTypeDisplayName = pennyBotStore.getBotTypeDisplayName
const getBotTypeIcon = pennyBotStore.getBotTypeIcon
const getBotTypeIconClass = pennyBotStore.getBotTypeColor
const getStatusBadgeClass = pennyBotStore.getStatusBadgeClass
const getStatusText = pennyBotStore.getStatusText
const formatDate = pennyBotStore.formatDate

// Lifecycle
onMounted(() => {
  refreshBots()
})
</script>

<style scoped>
.penny-dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

.bot-card {
  transition: all 0.2s ease-in-out;
}

.bot-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}
</style>
