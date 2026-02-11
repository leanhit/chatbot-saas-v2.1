<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <!-- Header -->
    <div class="bg-white dark:bg-gray-800 shadow-sm border-b border-gray-200 dark:border-gray-700">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-16">
          <div class="flex items-center">
            <h1 class="text-xl font-semibold text-gray-900 dark:text-white">
              {{ $t('Penny Bot Management') }}
            </h1>
          </div>
          <div class="flex items-center space-x-4">
            <button
              @click="showCreateModal = true"
              class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              <Icon icon="mdi:plus" class="mr-2" />
              {{ $t('Create Penny Bot') }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Stats Cards -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div class="bg-white dark:bg-gray-800 overflow-hidden shadow rounded-lg">
          <div class="p-5">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <Icon icon="mdi:robot" class="h-6 w-6 text-gray-400" />
              </div>
              <div class="ml-5 w-0 flex-1">
                <dl>
                  <dt class="text-sm font-medium text-gray-500 dark:text-gray-400 truncate">
                    {{ $t('Total Bots') }}
                  </dt>
                  <dd class="text-lg font-medium text-gray-900 dark:text-white">
                    {{ stats.total }}
                  </dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div class="bg-white dark:bg-gray-800 overflow-hidden shadow rounded-lg">
          <div class="p-5">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <Icon icon="mdi:check-circle" class="h-6 w-6 text-green-400" />
              </div>
              <div class="ml-5 w-0 flex-1">
                <dl>
                  <dt class="text-sm font-medium text-gray-500 dark:text-gray-400 truncate">
                    {{ $t('Active Bots') }}
                  </dt>
                  <dd class="text-lg font-medium text-gray-900 dark:text-white">
                    {{ stats.active }}
                  </dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div class="bg-white dark:bg-gray-800 overflow-hidden shadow rounded-lg">
          <div class="p-5">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <Icon icon="mdi:pause-circle" class="h-6 w-6 text-yellow-400" />
              </div>
              <div class="ml-5 w-0 flex-1">
                <dl>
                  <dt class="text-sm font-medium text-gray-500 dark:text-gray-400 truncate">
                    {{ $t('Inactive Bots') }}
                  </dt>
                  <dd class="text-lg font-medium text-gray-900 dark:text-white">
                    {{ stats.inactive }}
                  </dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div class="bg-white dark:bg-gray-800 overflow-hidden shadow rounded-lg">
          <div class="p-5">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <Icon icon="mdi:chart-line" class="h-6 w-6 text-blue-400" />
              </div>
              <div class="ml-5 w-0 flex-1">
                <dl>
                  <dt class="text-sm font-medium text-gray-500 dark:text-gray-400 truncate">
                    {{ $t('Online') }}
                  </dt>
                  <dd class="text-lg font-medium text-gray-900 dark:text-white">
                    {{ stats.online }}
                  </dd>
                </dl>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Search and Filter -->
      <div class="bg-white dark:bg-gray-800 shadow rounded-lg mb-6">
        <div class="p-4">
          <div class="flex flex-col sm:flex-row gap-4">
            <div class="flex-1">
              <input
                v-model="searchQuery"
                type="text"
                :placeholder="$t('Search bots...')"
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
              />
            </div>
            <select
              v-model="statusFilter"
              class="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
            >
              <option value="">{{ $t('All Types') }}</option>
              <option value="active">{{ $t('Active') }}</option>
              <option value="inactive">{{ $t('Inactive') }}</option>
            </select>
          </div>
        </div>
      </div>

      <!-- Bots List -->
      <div class="bg-white dark:bg-gray-800 shadow rounded-lg">
        <div class="px-4 py-5 sm:px-6">
          <h3 class="text-lg leading-6 font-medium text-gray-900 dark:text-white">
            {{ $t('Bots List') }}
          </h3>
        </div>
        <div class="border-t border-gray-200 dark:border-gray-700">
          <div v-if="loading" class="px-4 py-8 text-center">
            <div class="inline-flex items-center">
              <Icon icon="mdi:loading" class="animate-spin h-5 w-5 mr-3" />
              {{ $t('Loading...') }}
            </div>
          </div>
          
          <div v-else-if="filteredBots.length === 0" class="px-4 py-8 text-center">
            <Icon icon="mdi:robot-off" class="h-12 w-12 text-gray-400 mx-auto mb-4" />
            <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">
              {{ $t('No Penny Bots Yet') }}
            </h3>
            <p class="text-gray-500 dark:text-gray-400">
              {{ $t('Create your first intelligent Penny bot to get started') }}
            </p>
            <div class="mt-6">
              <button
                @click="showCreateModal = true"
                class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
              >
                <Icon icon="mdi:plus" class="mr-2" />
                {{ $t('Create Your First Penny Bot') }}
              </button>
            </div>
          </div>

          <div v-else class="divide-y divide-gray-200 dark:divide-gray-700">
            <div
              v-for="bot in filteredBots"
              :key="bot.id"
              class="px-4 py-4 sm:px-6 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
            >
              <div class="flex items-center justify-between">
                <div class="flex items-center">
                  <div class="flex-shrink-0 h-10 w-10">
                    <div class="h-10 w-10 rounded-full bg-blue-100 dark:bg-blue-900 flex items-center justify-center">
                      <Icon icon="mdi:robot" class="h-6 w-6 text-blue-600 dark:text-blue-400" />
                    </div>
                  </div>
                  <div class="ml-4">
                    <div class="flex items-center">
                      <h4 class="text-lg font-medium text-gray-900 dark:text-white">
                        {{ bot.name }}
                      </h4>
                      <span
                        :class="[
                          'ml-2 inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
                          bot.isActive ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200' : 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
                        ]"
                      >
                        {{ bot.isActive ? $t('Online') : $t('Offline') }}
                      </span>
                    </div>
                    <p class="text-sm text-gray-500 dark:text-gray-400">
                      {{ bot.description }}
                    </p>
                    <div class="mt-2 flex items-center text-sm text-gray-500 dark:text-gray-400">
                      <Icon icon="mdi:calendar" class="h-4 w-4 mr-1" />
                      {{ $t('Created') }}: {{ formatDate(bot.createdAt) }}
                    </div>
                  </div>
                </div>

                <div class="flex items-center space-x-2">
                  <button
                    @click="openConnections(bot)"
                    class="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white dark:hover:bg-gray-600"
                  >
                    <Icon icon="mdi:link-variant" class="h-4 w-4 mr-1" />
                    {{ $t('Connection') }}
                  </button>
                  
                  <button
                    @click="openRules(bot)"
                    class="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white dark:hover:bg-gray-600"
                  >
                    <Icon icon="mdi:book-open-variant" class="h-4 w-4 mr-1" />
                    {{ $t('Rule') }}
                  </button>
                  
                  <button
                    @click="openAnalytics(bot)"
                    class="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white dark:hover:bg-gray-600"
                  >
                    <Icon icon="mdi:chart-line" class="h-4 w-4 mr-1" />
                    {{ $t('Analytics') }}
                  </button>
                  
                  <button
                    @click="openChat(bot)"
                    class="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white dark:hover:bg-gray-600"
                  >
                    <Icon icon="mdi:message-text" class="h-4 w-4 mr-1" />
                    {{ $t('Chat') }}
                  </button>

                  <div class="relative">
                    <button
                      @click.stop="toggleDropdown(bot.id)"
                      class="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white dark:hover:bg-gray-600"
                    >
                      <Icon icon="mdi:dots-vertical" class="h-4 w-4" />
                    </button>

                    <!-- Dropdown Menu -->
                    <div
                      v-if="dropdownOpen === bot.id"
                      class="absolute right-0 z-10 mt-2 w-48 rounded-md shadow-lg bg-white dark:bg-gray-800 ring-1 ring-black ring-opacity-5"
                    >
                      <div class="py-1">
                        <button
                          @click.stop="editBot(bot)"
                          class="flex items-center w-full px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-gray-700"
                        >
                          <Icon icon="mdi:pencil" class="h-4 w-4 mr-2" />
                          {{ $t('Edit') }}
                        </button>
                        <button
                          @click.stop="toggleBotStatus(bot)"
                          class="flex items-center w-full px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-gray-700"
                        >
                          <Icon :icon="bot.isActive ? 'mdi:pause' : 'mdi:play'" class="h-4 w-4 mr-2" />
                          {{ bot.isActive ? $t('Disable') : $t('Enable') }}
                        </button>
                        <button
                          @click.stop="deleteBot(bot)"
                          class="flex items-center w-full px-4 py-2 text-sm text-red-600 hover:bg-gray-100 dark:text-red-400 dark:hover:bg-gray-700"
                        >
                          <Icon icon="mdi:delete" class="h-4 w-4 mr-2" />
                          {{ $t('Delete') }}
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Create/Edit Bot Modal -->
    <PennyBotModal
      v-if="showCreateModal"
      :bot="editingBot"
      :show="showCreateModal"
      @close="closeModal"
      @saved="handleBotSaved"
    />

    <!-- Analytics Modal -->
    <PennyBotAnalyticsModal
      v-if="showAnalyticsModal"
      :bot="selectedBot"
      :show="showAnalyticsModal"
      @close="showAnalyticsModal = false"
    />

    <!-- Chat Modal -->
    <PennyBotChatModal
      v-if="showChatModal"
      :bot="selectedBot"
      :show="showChatModal"
      @close="showChatModal = false"
    />

    <!-- Connections Modal -->
    <PennyConnectionsModal
      v-if="showConnectionsModal"
      :bot="selectedBot"
      :show="showConnectionsModal"
      @close="showConnectionsModal = false"
    />

    <!-- Rules Modal -->
    <PennyRulesModal
      v-if="showRulesModal"
      :bot="selectedBot"
      :show="showRulesModal"
      @close="showRulesModal = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Icon } from '@iconify/vue'
import PennyBotModal from './components/PennyBotModal.vue'
import PennyBotAnalyticsModal from './components/PennyBotAnalyticsModal.vue'
import PennyBotChatModal from './components/PennyBotChatModal.vue'
import PennyConnectionsModal from './components/PennyConnectionsModal.vue'
import PennyRulesModal from './components/PennyRulesModal.vue'

const { t } = useI18n()

// State
const bots = ref([])
const loading = ref(false)
const searchQuery = ref('')
const statusFilter = ref('')
const showCreateModal = ref(false)
const showAnalyticsModal = ref(false)
const showChatModal = ref(false)
const showConnectionsModal = ref(false)
const showRulesModal = ref(false)
const editingBot = ref(null)
const selectedBot = ref(null)
const dropdownOpen = ref(null)

// Computed
const stats = computed(() => ({
  total: bots.value.length,
  active: bots.value.filter(bot => bot.isActive).length,
  inactive: bots.value.filter(bot => !bot.isActive).length,
  online: bots.value.filter(bot => bot.isActive).length
}))

const filteredBots = computed(() => {
  let filtered = bots.value

  // Filter by search query
  if (searchQuery.value) {
    filtered = filtered.filter(bot => 
      bot.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      bot.description.toLowerCase().includes(searchQuery.value.toLowerCase())
    )
  }

  // Filter by status
  if (statusFilter.value) {
    filtered = filtered.filter(bot => {
      if (statusFilter.value === 'active') return bot.isActive
      if (statusFilter.value === 'inactive') return !bot.isActive
      return true
    })
  }

  return filtered
})

// Methods
const loadBots = async () => {
  loading.value = true
  try {
    // TODO: Replace with actual API call
    // const response = await pennyApi.getBots()
    // bots.value = response.data
    
    // Mock data for now
    bots.value = [
      {
        id: '1',
        name: 'Customer Service Bot',
        description: 'Handles customer inquiries and support requests',
        isActive: true,
        createdAt: new Date('2024-01-15')
      },
      {
        id: '2', 
        name: 'Sales Assistant Bot',
        description: 'Helps customers with product information and purchases',
        isActive: false,
        createdAt: new Date('2024-01-10')
      }
    ]
  } catch (error) {
    console.error('Failed to load bots:', error)
  } finally {
    loading.value = false
  }
}

const formatDate = (date) => {
  return new Intl.DateTimeFormat('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  }).format(date)
}

const toggleDropdown = (botId) => {
  dropdownOpen.value = dropdownOpen.value === botId ? null : botId
}

const closeDropdowns = () => {
  dropdownOpen.value = null
}

const editBot = (bot) => {
  editingBot.value = bot
  showCreateModal.value = true
  closeDropdowns()
}

const openAnalytics = (bot) => {
  selectedBot.value = bot
  showAnalyticsModal.value = true
  closeDropdowns()
}

const openConnections = (bot) => {
  selectedBot.value = bot
  showConnectionsModal.value = true
  closeDropdowns()
}

const openRules = (bot) => {
  selectedBot.value = bot
  showRulesModal.value = true
  closeDropdowns()
}

const openChat = (bot) => {
  selectedBot.value = bot
  showChatModal.value = true
  closeDropdowns()
}

const toggleBotStatus = async (bot) => {
  try {
    // TODO: Replace with actual API call
    // await pennyApi.toggleBotStatus(bot.id, !bot.isActive)
    bot.isActive = !bot.isActive
    closeDropdowns()
  } catch (error) {
    console.error('Failed to toggle bot status:', error)
  }
}

const deleteBot = async (bot) => {
  if (confirm(`Are you sure you want to delete ${bot.name}?`)) {
    try {
      // TODO: Replace with actual API call
      // await pennyApi.deleteBot(bot.id)
      bots.value = bots.value.filter(b => b.id !== bot.id)
      closeDropdowns()
    } catch (error) {
      console.error('Failed to delete bot:', error)
    }
  }
}

const closeModal = () => {
  showCreateModal.value = false
  editingBot.value = null
}

const handleBotSaved = (savedBot) => {
  if (editingBot.value) {
    // Update existing bot
    const index = bots.value.findIndex(b => b.id === savedBot.id)
    if (index !== -1) {
      bots.value[index] = savedBot
    }
  } else {
    // Add new bot
    bots.value.push(savedBot)
  }
  closeModal()
}

// Lifecycle
onMounted(() => {
  loadBots()
  
  // Close dropdowns when clicking outside
  document.addEventListener('click', closeDropdowns)
})
</script>

<style scoped>
/* Custom animations and transitions */
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
