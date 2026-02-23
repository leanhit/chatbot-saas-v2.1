<template>
  <div class="entitlement-card bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700 hover:shadow-lg transition-shadow">
    <!-- Header -->
    <div class="flex justify-between items-start mb-4">
      <div class="flex-1">
        <div class="flex items-center gap-2 mb-2">
          <div :class="featureIconClass" class="p-2 rounded-lg">
            <Icon :icon="featureIcon" class="text-xl" />
          </div>
          <div>
            <h3 class="font-semibold text-gray-900 dark:text-gray-100">
              {{ formatFeatureName(entitlement.feature) }}
            </h3>
            <p class="text-sm text-gray-600 dark:text-gray-400">
              {{ entitlement.description || 'Feature usage tracking' }}
            </p>
          </div>
        </div>
      </div>
      
      <!-- Status Badge -->
      <div class="flex flex-col items-end gap-2">
        <span :class="statusClass" class="px-3 py-1 rounded-full text-xs font-medium">
          {{ entitlement.isEnabled ? 'Active' : 'Inactive' }}
        </span>
        <span v-if="entitlement.isUnlimited" class="text-xs text-purple-600 dark:text-purple-400">
          Unlimited
        </span>
      </div>
    </div>
    
    <!-- Usage Meter -->
    <div class="mb-4" v-if="!entitlement.isUnlimited">
      <div class="flex justify-between items-center mb-2">
        <span class="text-sm font-medium text-gray-700 dark:text-gray-300">Usage</span>
        <span class="text-sm text-gray-600 dark:text-gray-400">
          {{ formatNumber(entitlement.currentUsage) }} / {{ formatNumber(entitlement.limitValue) }}
        </span>
      </div>
      
      <!-- Progress Bar -->
      <div class="relative">
        <div class="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-3">
          <div 
            :class="usageBarClass" 
            :style="{ width: usagePercentage + '%' }"
            class="h-3 rounded-full transition-all duration-300 ease-out"
          ></div>
        </div>
        
        <!-- Warning Threshold Indicator -->
        <div 
          v-if="entitlement.warningThresholdPercentage"
          :style="{ left: entitlement.warningThresholdPercentage + '%' }"
          class="absolute top-0 w-0.5 h-3 bg-yellow-400"
        ></div>
      </div>
      
      <!-- Usage Percentage -->
      <div class="flex justify-between items-center mt-2">
        <span class="text-xs text-gray-500 dark:text-gray-400">
          {{ usagePercentage.toFixed(1) }}% used
        </span>
        <span v-if="entitlement.needsReset" class="text-xs text-orange-600 dark:text-orange-400">
          Reset soon
        </span>
      </div>
    </div>
    
    <!-- Unlimited Usage Display -->
    <div class="mb-4" v-else>
      <div class="flex items-center gap-2 p-3 bg-purple-50 dark:bg-purple-900/20 rounded-lg">
        <Icon icon="ic:baseline-infinity" class="text-purple-600 dark:text-purple-400 text-xl" />
        <span class="text-sm font-medium text-purple-700 dark:text-purple-300">
          Unlimited Usage
        </span>
      </div>
    </div>
    
    <!-- Additional Info -->
    <div class="grid grid-cols-2 gap-4 text-xs text-gray-600 dark:text-gray-400">
      <div v-if="entitlement.resetPeriod">
        <span class="font-medium">Reset:</span>
        <span class="ml-1">{{ entitlement.resetPeriod }}</span>
      </div>
      <div v-if="entitlement.nextResetAt">
        <span class="font-medium">Next reset:</span>
        <span class="ml-1">{{ formatDate(entitlement.nextResetAt) }}</span>
      </div>
      <div v-if="entitlement.overageAllowed">
        <span class="font-medium">Overage:</span>
        <span class="ml-1">${{ entitlement.overageRate }}/unit</span>
      </div>
      <div v-if="entitlement.softLimit">
        <span class="font-medium">Soft limit:</span>
        <span class="ml-1 text-green-600">Enabled</span>
      </div>
    </div>
    
    <!-- Actions -->
    <div class="flex gap-2 mt-4 pt-4 border-t border-gray-200 dark:border-gray-700">
      <button 
        @click="viewDetails"
        class="flex-1 px-3 py-2 text-sm bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors"
      >
        View Details
      </button>
      <button 
        v-if="entitlement.isNearLimit && !entitlement.isUnlimited"
        @click="upgradeLimit"
        class="flex-1 px-3 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
      >
        Upgrade Limit
      </button>
    </div>
    
    <!-- Warning Alert -->
    <div 
      v-if="entitlement.shouldSendWarning" 
      class="mt-4 p-3 bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 rounded-lg"
    >
      <div class="flex items-center gap-2">
        <Icon icon="ic:baseline-warning" class="text-yellow-600 dark:text-yellow-400 text-lg" />
        <span class="text-sm text-yellow-800 dark:text-yellow-200">
          You're approaching your usage limit ({{ usagePercentage.toFixed(1) }}%)
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  entitlement: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['view-details', 'upgrade-limit'])

// Computed properties
const usagePercentage = computed(() => {
  if (props.entitlement.isUnlimited || !props.entitlement.limitValue) return 0
  return (props.entitlement.currentUsage / props.entitlement.limitValue) * 100
})

const statusClass = computed(() => {
  if (!props.entitlement.isEnabled) {
    return 'bg-gray-100 text-gray-600 dark:bg-gray-700 dark:text-gray-400'
  }
  if (props.entitlement.isOverLimit) {
    return 'bg-red-100 text-red-600 dark:bg-red-900/20 dark:text-red-400'
  }
  if (props.entitlement.isNearLimit) {
    return 'bg-yellow-100 text-yellow-600 dark:bg-yellow-900/20 dark:text-yellow-400'
  }
  return 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400'
})

const usageBarClass = computed(() => {
  const percentage = usagePercentage.value
  if (percentage >= 90) return 'bg-red-500'
  if (percentage >= 80) return 'bg-yellow-500'
  if (percentage >= 60) return 'bg-blue-500'
  return 'bg-green-500'
})

const featureIcon = computed(() => {
  const feature = props.entitlement.feature
  switch (feature) {
    case 'API_CALLS': return 'ic:baseline-api'
    case 'STORAGE': return 'ic:baseline-storage'
    case 'USERS': return 'ic:baseline-group'
    case 'MESSAGES': return 'ic:baseline-message'
    case 'BANDWIDTH': return 'ic:baseline-speed'
    default: return 'ic:baseline-settings'
  }
})

const featureIconClass = computed(() => {
  const feature = props.entitlement.feature
  switch (feature) {
    case 'API_CALLS': return 'bg-blue-100 text-blue-600 dark:bg-blue-900/20 dark:text-blue-400'
    case 'STORAGE': return 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400'
    case 'USERS': return 'bg-purple-100 text-purple-600 dark:bg-purple-900/20 dark:text-purple-400'
    case 'MESSAGES': return 'bg-orange-100 text-orange-600 dark:bg-orange-900/20 dark:text-orange-400'
    case 'BANDWIDTH': return 'bg-pink-100 text-pink-600 dark:bg-pink-900/20 dark:text-pink-400'
    default: return 'bg-gray-100 text-gray-600 dark:bg-gray-900/20 dark:text-gray-400'
  }
})

// Methods
const formatFeatureName = (feature) => {
  return feature ? feature.replace(/_/g, ' ').toLowerCase() : 'Unknown Feature'
}

const formatNumber = (num) => {
  if (!num) return '0'
  return new Intl.NumberFormat().format(num)
}

const formatDate = (dateString) => {
  if (!dateString) return 'Never'
  return new Date(dateString).toLocaleDateString()
}

const viewDetails = () => {
  emit('view-details', props.entitlement)
}

const upgradeLimit = () => {
  emit('upgrade-limit', props.entitlement)
}
</script>

<style scoped>
.entitlement-card {
  transition: all 0.2s ease-in-out;
}

.entitlement-card:hover {
  transform: translateY(-2px);
}
</style>
