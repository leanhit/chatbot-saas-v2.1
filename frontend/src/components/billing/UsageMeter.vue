<template>
  <div class="usage-meter">
    <!-- Header -->
    <div class="flex justify-between items-center mb-2">
      <div class="flex items-center gap-2">
        <div :class="iconClass" class="p-1.5 rounded-lg">
          <Icon :icon="icon" class="text-lg" />
        </div>
        <span class="text-sm font-medium text-gray-700 dark:text-gray-300">{{ label }}</span>
      </div>
      <span class="text-sm text-gray-600 dark:text-gray-400">
        {{ formatNumber(current) }} / {{ formatNumber(max) || '∞' }}
      </span>
    </div>
    
    <!-- Progress Bar -->
    <div class="relative">
      <div class="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
        <div 
          :class="progressClass" 
          :style="{ width: percentage + '%' }"
          class="h-2 rounded-full transition-all duration-300 ease-out"
        ></div>
      </div>
      
      <!-- Warning Threshold -->
      <div 
        v-if="warningThreshold && warningThreshold < 100"
        :style="{ left: warningThreshold + '%' }"
        class="absolute top-0 w-0.5 h-2 bg-yellow-400"
      ></div>
    </div>
    
    <!-- Footer Info -->
    <div class="flex justify-between items-center mt-2">
      <span class="text-xs text-gray-500 dark:text-gray-400">
        {{ percentage.toFixed(1) }}% used
      </span>
      <div class="flex items-center gap-2">
        <span v-if="isNearLimit" class="text-xs text-yellow-600 dark:text-yellow-400">
          <Icon icon="ic:baseline-warning" class="inline mr-1" />
          Near limit
        </span>
        <span v-if="isOverLimit" class="text-xs text-red-600 dark:text-red-400">
          <Icon icon="ic:baseline-error" class="inline mr-1" />
          Over limit
        </span>
        <span v-if="resetPeriod" class="text-xs text-gray-500 dark:text-gray-400">
          Resets {{ resetPeriod }}
        </span>
      </div>
    </div>
    
    <!-- Tooltip -->
    <div 
      v-if="showTooltip"
      class="absolute z-10 px-3 py-2 text-sm text-white bg-gray-900 rounded-lg shadow-lg -top-8 left-1/2 transform -translate-x-1/2"
    >
      <div class="text-center">
        <div>{{ formatNumber(current) }} of {{ formatNumber(max) }}</div>
        <div class="text-xs text-gray-300">{{ formatNumber(remaining) }} remaining</div>
      </div>
      <div class="absolute -bottom-1 left-1/2 transform -translate-x-1/2 w-2 h-2 bg-gray-900 rotate-45"></div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  label: {
    type: String,
    required: true
  },
  current: {
    type: Number,
    default: 0
  },
  max: {
    type: Number,
    default: null
  },
  icon: {
    type: String,
    default: 'ic:baseline-bar-chart'
  },
  iconColor: {
    type: String,
    default: 'blue'
  },
  warningThreshold: {
    type: Number,
    default: 80
  },
  resetPeriod: {
    type: String,
    default: null
  },
  showTooltip: {
    type: Boolean,
    default: false
  }
})

// Computed properties
const percentage = computed(() => {
  if (!props.max || props.max === 0) return 0
  return Math.min((props.current / props.max) * 100, 100)
})

const remaining = computed(() => {
  if (!props.max) return Infinity
  return Math.max(props.max - props.current, 0)
})

const isNearLimit = computed(() => {
  if (!props.max) return false
  return percentage.value >= props.warningThreshold && percentage.value < 100
})

const isOverLimit = computed(() => {
  if (!props.max) return false
  return props.current > props.max
})

const progressClass = computed(() => {
  const pct = percentage.value
  if (pct >= 90) return 'bg-red-500'
  if (pct >= 80) return 'bg-yellow-500'
  if (pct >= 60) return 'bg-blue-500'
  return 'bg-green-500'
})

const iconClass = computed(() => {
  const colorMap = {
    blue: 'bg-blue-100 text-blue-600 dark:bg-blue-900/20 dark:text-blue-400',
    green: 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400',
    yellow: 'bg-yellow-100 text-yellow-600 dark:bg-yellow-900/20 dark:text-yellow-400',
    red: 'bg-red-100 text-red-600 dark:bg-red-900/20 dark:text-red-400',
    purple: 'bg-purple-100 text-purple-600 dark:bg-purple-900/20 dark:text-purple-400',
    orange: 'bg-orange-100 text-orange-600 dark:bg-orange-900/20 dark:text-orange-400'
  }
  return colorMap[props.iconColor] || colorMap.blue
})

// Methods
const formatNumber = (num) => {
  if (!num && num !== 0) return '0'
  if (num === Infinity) return '∞'
  return new Intl.NumberFormat().format(num)
}
</script>

<style scoped>
.usage-meter {
  position: relative;
}

.usage-meter:hover .usage-tooltip {
  opacity: 1;
  visibility: visible;
}
</style>
