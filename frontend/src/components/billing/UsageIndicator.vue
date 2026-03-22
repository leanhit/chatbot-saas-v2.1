<template>
  <div class="usage-indicator">
    <!-- Header -->
    <div class="flex items-center justify-between mb-2">
      <div class="flex items-center space-x-2">
        <Icon :icon="getFeatureIcon()" class="w-4 h-4 text-gray-400" />
        <span class="text-sm font-medium text-gray-900 dark:text-white">
          {{ getFeatureName() }}
        </span>
      </div>
      <div class="flex items-center space-x-2">
        <span class="text-xs text-gray-500 dark:text-gray-400">
          {{ currentUsage }} / {{ limitValue }}
        </span>
        <span
          class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium"
          :class="getStatusClass()"
        >
          {{ getStatusText() }}
        </span>
      </div>
    </div>

    <!-- Progress Bar -->
    <div class="relative">
      <div class="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
        <div
          class="h-2 rounded-full transition-all duration-300 ease-out"
          :class="getProgressBarClass()"
          :style="{ width: `${Math.min(percentage, 100)}%` }"
        />
      </div>
      
      <!-- Warning indicator -->
      <div
        v-if="showWarning"
        class="absolute -top-1 right-0 w-3 h-3 bg-yellow-500 rounded-full animate-pulse"
        title="Approaching limit"
      >
        <Icon icon="mdi:alert" class="w-3 h-3 text-white" />
      </div>
    </div>

    <!-- Details -->
    <div class="mt-2 flex items-center justify-between text-xs">
      <span class="text-gray-500 dark:text-gray-400">
        {{ getLimitDescription() }}
      </span>
      <span v-if="overageAllowed" class="text-gray-500 dark:text-gray-400">
        Overage: {{ formatCurrency(overageRate) }}/{{ getUnit() }}
      </span>
    </div>

    <!-- Expandable Details -->
    <div v-if="showDetails" class="mt-3 pt-3 border-t border-gray-200 dark:border-gray-600">
      <div class="space-y-2 text-xs">
        <div class="flex justify-between">
          <span class="text-gray-500 dark:text-gray-400">Reset Period:</span>
          <span class="text-gray-900 dark:text-white">{{ getResetPeriod() }}</span>
        </div>
        <div class="flex justify-between">
          <span class="text-gray-500 dark:text-gray-400">Last Reset:</span>
          <span class="text-gray-900 dark:text-white">{{ formatDate(lastReset) }}</span>
        </div>
        <div class="flex justify-between">
          <span class="text-gray-500 dark:text-gray-400">Next Reset:</span>
          <span class="text-gray-900 dark:text-white">{{ formatDate(nextReset) }}</span>
        </div>
        <div v-if="warningThreshold" class="flex justify-between">
          <span class="text-gray-500 dark:text-gray-400">Warning at:</span>
          <span class="text-gray-900 dark:text-white">{{ warningThreshold }}%</span>
        </div>
      </div>
    </div>

    <!-- Actions -->
    <div v-if="showActions" class="mt-3 flex space-x-2">
      <button
        @click="$emit('upgrade')"
        class="flex-1 text-xs text-primary-600 dark:text-primary-400 hover:text-primary-700 dark:hover:text-primary-300 font-medium"
      >
        <Icon icon="mdi:rocket-launch" class="w-3 h-3 mr-1 inline" />
        Upgrade Plan
      </button>
      <button
        @click="showDetails = !showDetails"
        class="text-xs text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300"
      >
        <Icon :icon="showDetails ? 'mdi:chevron-up' : 'mdi:chevron-down'" class="w-3 h-3 inline" />
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  entitlement: {
    type: Object,
    required: true
  },
  usage: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['upgrade'])

const showDetails = ref(false)

// Computed
const currentUsage = computed(() => props.usage || 0)
const limitValue = computed(() => props.entitlement.limitValue || 0)
const percentage = computed(() => {
  if (limitValue.value === 0) return 0
  return Math.round((currentUsage.value / limitValue.value) * 100)
})

const isUnlimited = computed(() => props.entitlement.isUnlimited)
const overageAllowed = computed(() => props.entitlement.overageAllowed)
const overageRate = computed(() => props.entitlement.overageRate || 0)
const warningThreshold = computed(() => props.entitlement.warningThreshold)

const showWarning = computed(() => {
  return !isUnlimited.value && 
         percentage.value >= (warningThreshold.value || 80) && 
         percentage.value < 100
})

const showActions = computed(() => {
  return !isUnlimited.value && percentage.value >= 90
})

const getStatusClass = () => {
  if (isUnlimited.value) return 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
  if (percentage.value >= 100) return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
  if (percentage.value >= 80) return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200'
  return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
}

const getStatusText = () => {
  if (isUnlimited.value) return 'Unlimited'
  if (percentage.value >= 100) return 'Limit Reached'
  if (percentage.value >= 80) return 'Warning'
  return 'Available'
}

const getProgressBarClass = () => {
  if (isUnlimited.value) return 'bg-gray-400'
  if (percentage.value >= 100) return 'bg-red-500'
  if (percentage.value >= 80) return 'bg-yellow-500'
  return 'bg-green-500'
}

// Feature configurations
const featureConfig = {
  MAX_USERS: { name: 'Users', icon: 'mdi:account-group', unit: 'users' },
  MAX_BOTS: { name: 'Bots', icon: 'mdi:robot', unit: 'bots' },
  MAX_MESSAGES_PER_MONTH: { name: 'Messages', icon: 'mdi:chat', unit: 'messages' },
  MAX_API_CALLS_PER_MONTH: { name: 'API Calls', icon: 'mdi:api', unit: 'calls' },
  MAX_STORAGE_MB: { name: 'Storage', icon: 'mdi:harddisk', unit: 'MB' },
  MAX_INTENTS: { name: 'Intents', icon: 'mdi:brain', unit: 'intents' },
  MAX_DIALOGS: { name: 'Dialogs', icon: 'mdi:forum', unit: 'dialogs' },
  MAX_LANGUAGES: { name: 'Languages', icon: 'mdi:translate', unit: 'languages' }
}

const getFeatureName = () => {
  const config = featureConfig[props.entitlement.usageLimitType]
  return config?.name || props.entitlement.usageLimitType?.replace(/_/g, ' ')
}

const getFeatureIcon = () => {
  const config = featureConfig[props.entitlement.usageLimitType]
  return config?.icon || 'mdi:help-circle'
}

const getUnit = () => {
  const config = featureConfig[props.entitlement.usageLimitType]
  return config?.unit || 'units'
}

const getLimitDescription = () => {
  if (isUnlimited.value) return 'Unlimited usage'
  if (limitValue.value === 0) return 'No limit set'
  return `${limitValue.value} ${getUnit()} per ${getResetPeriod()}`
}

const getResetPeriod = () => {
  const periods = {
    DAILY: 'day',
    WEEKLY: 'week',
    MONTHLY: 'month',
    YEARLY: 'year'
  }
  return periods[props.entitlement.resetPeriod] || 'month'
}

const lastReset = computed(() => props.entitlement.lastReset)
const nextReset = computed(() => props.entitlement.nextReset)

const formatDate = (date) => {
  if (!date) return 'N/A'
  return new Intl.DateTimeFormat('en-US', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(date))
}

const formatCurrency = (amount) => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD'
  }).format(amount)
}
</script>
