<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white dark:bg-gray-800 rounded-lg max-w-4xl w-full mx-4 max-h-[90vh] overflow-y-auto">
      <!-- Header -->
      <div class="p-6 border-b border-gray-200 dark:border-gray-700">
        <div class="flex justify-between items-center">
          <h3 class="text-xl font-semibold text-gray-900 dark:text-gray-100">
            Select a Bot
          </h3>
          <button 
            @click="$emit('close')"
            class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
          >
            <Icon icon="ic:baseline-close" class="text-xl" />
          </button>
        </div>
      </div>

      <!-- Search and Filters -->
      <div class="p-6 border-b border-gray-200 dark:border-gray-700">
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
            v-model="selectedType"
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
            v-model="selectedStatus"
            class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            <option value="">All Status</option>
            <option value="active">Active</option>
            <option value="inactive">Inactive</option>
          </select>
        </div>
      </div>

      <!-- Bots Grid -->
      <div class="p-6">
        <div v-if="filteredBots.length === 0" class="text-center py-12">
          <Icon icon="ic:baseline-robot" class="text-4xl text-gray-400" />
          <h3 class="text-lg font-medium text-gray-900 dark:text-gray-100 mt-4">
            No bots found
          </h3>
          <p class="text-gray-600 dark:text-gray-400 mt-2">
            {{ searchQuery ? 'Try adjusting your search' : 'No bots available' }}
          </p>
        </div>

        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <div 
            v-for="bot in filteredBots" 
            :key="bot.id"
            @click="selectBot(bot)"
            class="bot-card bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-4 cursor-pointer hover:border-blue-500 hover:shadow-lg transition-all duration-200"
          >
            <!-- Bot Header -->
            <div class="flex items-start justify-between mb-3">
              <div class="flex items-center gap-3">
                <div :class="getBotTypeIconClass(bot.botType)" class="p-2 rounded-lg">
                  <Icon :icon="getBotTypeIcon(bot.botType)" class="text-lg" />
                </div>
                <div>
                  <h4 class="font-semibold text-gray-900 dark:text-gray-100">
                    {{ bot.botName }}
                  </h4>
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

            <!-- Bot Description -->
            <div class="mb-3">
              <p class="text-sm text-gray-600 dark:text-gray-400 line-clamp-2">
                {{ bot.description || 'No description available' }}
              </p>
            </div>

            <!-- Bot Info -->
            <div class="space-y-1 text-xs text-gray-600 dark:text-gray-400 mb-3">
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

            <!-- Quick Stats -->
            <div class="flex gap-4 text-xs text-gray-600 dark:text-gray-400">
              <div class="flex items-center gap-1">
                <Icon icon="ic:baseline-chat" class="text-blue-500" />
                <span>{{ bot.messageCount || 0 }} messages</span>
              </div>
              <div class="flex items-center gap-1">
                <Icon icon="ic:baseline-speed" class="text-green-500" />
                <span>{{ bot.avgResponseTime || 0 }}ms</span>
              </div>
              <div class="flex items-center gap-1">
                <Icon icon="ic:baseline-thumb-up" class="text-purple-500" />
                <span>{{ bot.satisfactionRate || 0 }}%</span>
              </div>
            </div>

            <!-- Action Buttons -->
            <div class="flex gap-2 mt-4">
              <button 
                @click.stop="chatWithBot(bot)"
                :disabled="!isBotActive(bot)"
                class="flex-1 px-3 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center justify-center gap-2"
              >
                <Icon icon="ic:baseline-chat" />
                Chat
              </button>
              <button 
                @click.stop="viewDetails(bot)"
                class="flex-1 px-3 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors flex items-center justify-center gap-2"
              >
                <Icon icon="ic:baseline-visibility" />
                Details
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Footer -->
      <div class="p-6 border-t border-gray-200 dark:border-gray-700">
        <div class="flex justify-between items-center">
          <p class="text-sm text-gray-600 dark:text-gray-400">
            Showing {{ filteredBots.length }} of {{ bots.length }} bots
          </p>
          
          <div class="flex gap-3">
            <button 
              @click="$emit('close')"
              class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-600 transition-colors"
            >
              Cancel
            </button>
            <button 
              @click="createNewBot"
              class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors flex items-center gap-2"
            >
              <Icon icon="ic:baseline-add" />
              Create New Bot
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  bots: {
    type: Array,
    required: true
  }
})

const emit = defineEmits(['close', 'selected', 'createNew'])

// State
const searchQuery = ref('')
const selectedType = ref('')
const selectedStatus = ref('')

// Computed
const filteredBots = computed(() => {
  let filtered = props.bots

  // Apply search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(bot => 
      bot.botName.toLowerCase().includes(query) ||
      bot.description?.toLowerCase().includes(query) ||
      bot.botType?.toLowerCase().includes(query)
    )
  }

  // Apply type filter
  if (selectedType.value) {
    filtered = filtered.filter(bot => bot.botType === selectedType.value)
  }

  // Apply status filter
  if (selectedStatus.value) {
    filtered = filtered.filter(bot => {
      if (selectedStatus.value === 'active') return bot.isActive && bot.isEnabled
      if (selectedStatus.value === 'inactive') return !bot.isActive || !bot.isEnabled
      return true
    })
  }

  return filtered
})

// Methods
const getBotTypeIcon = (botType) => {
  const iconMap = {
    'CUSTOMER_SERVICE': 'ic:baseline-support-agent',
    'SALES': 'ic:baseline-shopping-cart',
    'SUPPORT': 'ic:baseline-headset-mic',
    'MARKETING': 'ic:baseline-campaign',
    'HR': 'ic:baseline-people',
    'FINANCE': 'ic:baseline-account-balance',
    'GENERAL': 'ic:baseline-settings'
  }
  return iconMap[botType] || 'ic:baseline-settings'
}

const getBotTypeIconClass = (botType) => {
  const classMap = {
    'CUSTOMER_SERVICE': 'bg-blue-100 text-blue-600 dark:bg-blue-900/20 dark:text-blue-400',
    'SALES': 'bg-emerald-100 text-emerald-600 dark:bg-emerald-900/20 dark:text-emerald-400',
    'SUPPORT': 'bg-amber-100 text-amber-600 dark:bg-amber-900/20 dark:text-amber-400',
    'MARKETING': 'bg-purple-100 text-purple-600 dark:bg-purple-900/20 dark:text-purple-400',
    'HR': 'bg-pink-100 text-pink-600 dark:bg-pink-900/20 dark:text-pink-400',
    'FINANCE': 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400',
    'GENERAL': 'bg-gray-100 text-gray-600'
  }
  return classMap[botType] || 'bg-gray-100 text-gray-600'
}

const getBotTypeDisplayName = (botType) => {
  const nameMap = {
    'CUSTOMER_SERVICE': 'Customer Service',
    'SALES': 'Sales',
    'SUPPORT': 'Technical Support',
    'MARKETING': 'Marketing',
    'HR': 'Human Resources',
    'FINANCE': 'Finance',
    'GENERAL': 'General Purpose'
  }
  return nameMap[botType] || botType
}

const getStatusBadgeClass = (bot) => {
  if (!bot.isActive || !bot.isEnabled) {
    return 'bg-gray-100 text-gray-600 dark:bg-gray-900/20 dark:text-gray-400'
  }
  return 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400'
}

const getStatusText = (bot) => {
  if (!bot.isActive) return 'Inactive'
  if (!bot.isEnabled) return 'Disabled'
  return 'Active'
}

const isBotActive = (bot) => {
  return bot.isActive && bot.isEnabled
}

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

const selectBot = (bot) => {
  emit('selected', bot)
}

const chatWithBot = (bot) => {
  if (isBotActive(bot)) {
    emit('selected', bot)
  }
}

const viewDetails = (bot) => {
  // Navigate to bot details
  console.log('View details for:', bot)
}

const createNewBot = () => {
  emit('createNew')
}
</script>

<style scoped>
.modal-backdrop {
  backdrop-filter: blur(4px);
}

.bot-card {
  transition: all 0.2s ease-in-out;
}

.bot-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
