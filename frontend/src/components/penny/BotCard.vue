<template>
  <div 
    class="bot-card bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 hover:shadow-lg transition-all duration-200 hover:scale-[1.02] cursor-pointer"
    @click="$emit('select')"
  >
    <!-- Card Header -->
    <div class="p-6 border-b border-gray-200 dark:border-gray-700">
      <div class="flex justify-between items-start mb-4">
        <div class="flex items-center gap-3">
          <div :class="getBotTypeIconClass(bot.botType)" class="p-3 rounded-lg">
            <Icon :icon="getBotTypeIcon(bot.botType)" class="text-xl" />
          </div>
          <div class="flex-1">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100 truncate">
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

      <!-- Configuration Preview -->
      <div v-if="bot.configuration" class="mb-4">
        <h4 class="text-sm font-medium text-gray-900 dark:text-gray-100 mb-2">Configuration</h4>
        <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-3">
          <pre class="text-xs text-gray-700 dark:text-gray-300 overflow-x-auto">{{ formatConfiguration(bot.configuration) }}</pre>
        </div>
      </div>

      <!-- Quick Actions -->
      <div class="flex gap-2">
        <button 
          @click="$emit('chat')"
          class="flex-1 px-3 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center justify-center gap-2"
          :disabled="!isBotActive(bot)"
        >
          <Icon icon="ic:baseline-chat" />
          Chat
        </button>
        <button 
          @click="$emit('details')"
          class="flex-1 px-3 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors flex items-center justify-center gap-2"
        >
          <Icon icon="ic:baseline-visibility" />
          Details
        </button>
        <button 
          @click="$emit('toggle')"
          class="px-3 py-2 rounded-lg transition-colors flex items-center justify-center gap-2"
          :class="getToggleButtonClass(bot)"
        >
          <Icon :icon="bot.isEnabled ? 'ic:baseline-toggle-on' : 'ic:baseline-toggle-off'" />
          <span class="text-sm">{{ bot.isEnabled ? 'Enabled' : 'Disabled' }}</span>
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
      <div class="flex items-center gap-2">
        <Icon icon="ic:baseline-health-check" 
               :class="getHealthIconClass(bot)" />
        <span class="text-sm text-gray-600 dark:text-gray-400">
          {{ getHealthText(bot) }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  bot: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['select', 'chat', 'details', 'toggle'])

// Bot type configurations
const botTypeConfig = {
  'CUSTOMER_SERVICE': {
    icon: 'ic:baseline-support-agent',
    color: 'bg-blue-100 text-blue-600 dark:bg-blue-900/20 dark:text-blue-400',
    displayName: 'Customer Service'
  },
  'SALES': {
    icon: 'ic:baseline-shopping-cart',
    color: 'bg-emerald-100 text-emerald-600 dark:bg-emerald-900/20 dark:text-emerald-400',
    displayName: 'Sales'
  },
  'SUPPORT': {
    icon: 'ic:baseline-headset-mic',
    color: 'bg-amber-100 text-amber-600 dark:bg-amber-900/20 dark:text-amber-400',
    displayName: 'Technical Support'
  },
  'MARKETING': {
    icon: 'ic:baseline-campaign',
    color: 'bg-purple-100 text-purple-600 dark:bg-purple-900/20 dark:text-purple-400',
    displayName: 'Marketing'
  },
  'HR': {
    icon: 'ic:baseline-people',
    color: 'bg-pink-100 text-pink-600 dark:bg-pink-900/20 dark:text-pink-400',
    displayName: 'Human Resources'
  },
  'FINANCE': {
    icon: 'ic:baseline-account-balance',
    color: 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400',
    displayName: 'Finance'
  },
  'GENERAL': {
    icon: 'ic:baseline-settings',
    color: 'bg-gray-100 text-gray-600 dark:bg-gray-900/20 dark:text-gray-400',
    displayName: 'General Purpose'
  }
}

// Computed properties
const isBotActive = computed(() => {
  return props.bot.isActive && props.bot.isEnabled
})

const getBotTypeConfig = computed(() => {
  return botTypeConfig[props.bot.botType] || botTypeConfig.GENERAL
})

// Methods
const getBotTypeIcon = (botType) => {
  return getBotTypeConfig(botType)?.icon || 'ic:baseline-settings'
}

const getBotTypeIconClass = (botType) => {
  return getBotTypeConfig(botType)?.color || 'bg-gray-100 text-gray-600'
}

const getBotTypeDisplayName = (botType) => {
  return getBotTypeConfig(botType)?.displayName || botType
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

const getToggleButtonClass = (bot) => {
  return bot.isEnabled 
    ? 'bg-green-600 text-white hover:bg-green-700' 
    : 'bg-gray-600 text-white hover:bg-gray-700'
}

const getHealthIconClass = (bot) => {
  // This would typically come from bot health data
  // For now, assume healthy if active
  if (!bot.isActive || !bot.isEnabled) {
    return 'text-gray-400'
  }
  return 'text-green-600'
}

const getHealthText = (bot) => {
  if (!bot.isActive || !bot.isEnabled) {
    return 'Offline'
  }
  return 'Online'
}

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

const formatConfiguration = (config) => {
  try {
    const parsed = JSON.parse(config)
    return JSON.stringify(parsed, null, 2)
  } catch {
    return config
  }
}
</script>

<style scoped>
.bot-card {
  transition: all 0.2s ease-in-out;
}

.bot-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}
</style>
