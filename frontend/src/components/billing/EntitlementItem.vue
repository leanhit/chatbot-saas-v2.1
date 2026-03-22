<template>
  <div class="entitlement-item p-6">
    <div class="flex items-start justify-between">
      <!-- Feature Info -->
      <div class="flex items-start space-x-4">
        <div class="w-10 h-10 rounded-full flex items-center justify-center"
             :class="getIconBackgroundClass()">
          <Icon :icon="getFeatureIcon()" class="w-5 h-5" :class="getIconClass()" />
        </div>
        <div class="flex-1">
          <div class="flex items-center space-x-2">
            <h3 class="text-sm font-medium text-gray-900 dark:text-white">
              {{ getFeatureName() }}
            </h3>
            <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium"
                  :class="getStatusClass()">
              {{ getStatusText() }}
            </span>
          </div>
          <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">
            {{ getFeatureDescription() }}
          </p>
        </div>
      </div>

      <!-- Actions -->
      <div class="flex items-center space-x-2">
        <button
          v-if="!entitlement.isEnabled"
          @click="$emit('upgrade')"
          class="px-3 py-1 text-xs bg-primary-100 text-primary-700 dark:bg-primary-900 dark:text-primary-300 rounded hover:bg-primary-200 dark:hover:bg-primary-800"
        >
          Enable
        </button>
        <button
          @click="showDetails = !showDetails"
          class="p-2 text-gray-400 hover:text-gray-500 dark:hover:text-gray-300"
        >
          <Icon :icon="showDetails ? 'mdi:chevron-up' : 'mdi:chevron-down'" class="w-4 h-4" />
        </button>
      </div>
    </div>

    <!-- Usage Progress -->
    <div v-if="entitlement.isEnabled && entitlement.limitValue > 0" class="mt-4">
      <div class="flex items-center justify-between text-sm mb-2">
        <span class="text-gray-600 dark:text-gray-400">Usage</span>
        <span class="text-gray-900 dark:text-white">
          {{ currentUsage }} / {{ entitlement.limitValue }}
        </span>
      </div>
      <div class="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
        <div
          class="h-2 rounded-full transition-all duration-300"
          :class="getProgressBarClass()"
          :style="{ width: `${Math.min(usagePercentage, 100)}%` }"
        />
      </div>
      <div class="flex items-center justify-between text-xs mt-1">
        <span class="text-gray-500 dark:text-gray-400">
          {{ getUsageText() }}
        </span>
        <span v-if="entitlement.overageAllowed" class="text-gray-500 dark:text-gray-400">
          Overage: {{ formatCurrency(entitlement.overageRate) }}/{{ getUnit() }}
        </span>
      </div>
    </div>

    <!-- Expandable Details -->
    <div v-if="showDetails" class="mt-4 pt-4 border-t border-gray-200 dark:border-gray-700">
      <div class="grid grid-cols-2 gap-4 text-sm">
        <div>
          <span class="text-gray-500 dark:text-gray-400">Feature Code:</span>
          <span class="ml-2 font-mono text-gray-900 dark:text-white">{{ entitlement.feature }}</span>
        </div>
        <div>
          <span class="text-gray-500 dark:text-gray-400">Limit Type:</span>
          <span class="ml-2 text-gray-900 dark:text-white">{{ entitlement.usageLimitType }}</span>
        </div>
        <div>
          <span class="text-gray-500 dark:text-gray-400">Limit Value:</span>
          <span class="ml-2 text-gray-900 dark:text-white">
            {{ entitlement.limitValue === 0 ? 'Unlimited' : entitlement.limitValue }}
          </span>
        </div>
        <div>
          <span class="text-gray-500 dark:text-gray-400">Reset Period:</span>
          <span class="ml-2 text-gray-900 dark:text-white">{{ getResetPeriod() }}</span>
        </div>
        <div>
          <span class="text-gray-500 dark:text-gray-400">Last Reset:</span>
          <span class="ml-2 text-gray-900 dark:text-white">{{ formatDate(entitlement.lastReset) }}</span>
        </div>
        <div>
          <span class="text-gray-500 dark:text-gray-400">Next Reset:</span>
          <span class="ml-2 text-gray-900 dark:text-white">{{ formatDate(entitlement.nextReset) }}</span>
        </div>
        <div>
          <span class="text-gray-500 dark:text-gray-400">Warning Threshold:</span>
          <span class="ml-2 text-gray-900 dark:text-white">{{ entitlement.warningThreshold || 80 }}%</span>
        </div>
        <div>
          <span class="text-gray-500 dark:text-gray-400">Overage Allowed:</span>
          <span class="ml-2 text-gray-900 dark:text-white">{{ entitlement.overageAllowed ? 'Yes' : 'No' }}</span>
        </div>
      </div>
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
const usagePercentage = computed(() => {
  if (props.entitlement.limitValue === 0) return 0
  return Math.round((currentUsage.value / props.entitlement.limitValue) * 100)
})

// Feature configurations
const featureConfig = {
  USER_MANAGEMENT: { 
    name: 'User Management', 
    icon: 'mdi:account-group', 
    description: 'Manage user accounts and permissions',
    iconBg: 'bg-blue-100 dark:bg-blue-900',
    iconColor: 'text-blue-600 dark:text-blue-400'
  },
  TENANT_MANAGEMENT: { 
    name: 'Tenant Management', 
    icon: 'mdi:office-building', 
    description: 'Manage tenant organizations',
    iconBg: 'bg-purple-100 dark:bg-purple-900',
    iconColor: 'text-purple-600 dark:text-purple-400'
  },
  API_ACCESS: { 
    name: 'API Access', 
    icon: 'mdi:api', 
    description: 'Access to API endpoints',
    iconBg: 'bg-green-100 dark:bg-green-900',
    iconColor: 'text-green-600 dark:text-green-400'
  },
  CHATBOT_CREATION: { 
    name: 'Chatbot Creation', 
    icon: 'mdi:robot', 
    description: 'Create and manage chatbots',
    iconBg: 'bg-orange-100 dark:bg-orange-900',
    iconColor: 'text-orange-600 dark:text-orange-400'
  },
  CHATBOT_CUSTOMIZATION: { 
    name: 'Chatbot Customization', 
    icon: 'mdi:palette', 
    description: 'Customize chatbot appearance and behavior',
    iconBg: 'bg-pink-100 dark:bg-pink-900',
    iconColor: 'text-pink-600 dark:text-pink-400'
  },
  MULTILINGUAL_SUPPORT: { 
    name: 'Multilingual Support', 
    icon: 'mdi:translate', 
    description: 'Support for multiple languages',
    iconBg: 'bg-indigo-100 dark:bg-indigo-900',
    iconColor: 'text-indigo-600 dark:text-indigo-400'
  },
  FACEBOOK_INTEGRATION: { 
    name: 'Facebook Integration', 
    icon: 'mdi:facebook', 
    description: 'Connect with Facebook Messenger',
    iconBg: 'bg-blue-100 dark:bg-blue-900',
    iconColor: 'text-blue-600 dark:text-blue-400'
  },
  WHATSAPP_INTEGRATION: { 
    name: 'WhatsApp Integration', 
    icon: 'mdi:whatsapp', 
    description: 'Connect with WhatsApp Business',
    iconBg: 'bg-green-100 dark:bg-green-900',
    iconColor: 'text-green-600 dark:text-green-400'
  },
  WEBSITE_WIDGET: { 
    name: 'Website Widget', 
    icon: 'mdi:web', 
    description: 'Embed chat widget on websites',
    iconBg: 'bg-gray-100 dark:bg-gray-900',
    iconColor: 'text-gray-600 dark:text-gray-400'
  },
  FILE_UPLOAD: { 
    name: 'File Upload', 
    icon: 'mdi:upload', 
    description: 'Upload files and media',
    iconBg: 'bg-yellow-100 dark:bg-yellow-900',
    iconColor: 'text-yellow-600 dark:text-yellow-400'
  },
  CLOUD_STORAGE: { 
    name: 'Cloud Storage', 
    icon: 'mdi:cloud', 
    description: 'Store files in the cloud',
    iconBg: 'bg-cyan-100 dark:bg-cyan-900',
    iconColor: 'text-cyan-600 dark:text-cyan-400'
  },
  ANALYTICS: { 
    name: 'Analytics', 
    icon: 'mdi:chart-line', 
    description: 'View analytics and reports',
    iconBg: 'bg-red-100 dark:bg-red-900',
    iconColor: 'text-red-600 dark:text-red-400'
  },
  CUSTOM_REPORTS: { 
    name: 'Custom Reports', 
    icon: 'mdi:file-chart', 
    description: 'Generate custom reports',
    iconBg: 'bg-teal-100 dark:bg-teal-900',
    iconColor: 'text-teal-600 dark:text-teal-400'
  }
}

const getFeatureName = () => {
  const config = featureConfig[props.entitlement.feature]
  return config?.name || props.entitlement.feature?.replace(/_/g, ' ')
}

const getFeatureIcon = () => {
  const config = featureConfig[props.entitlement.feature]
  return config?.icon || 'mdi:help-circle'
}

const getIconBackgroundClass = () => {
  const config = featureConfig[props.entitlement.feature]
  return config?.iconBg || 'bg-gray-100 dark:bg-gray-900'
}

const getIconClass = () => {
  const config = featureConfig[props.entitlement.feature]
  return config?.iconColor || 'text-gray-600 dark:text-gray-400'
}

const getFeatureDescription = () => {
  const config = featureConfig[props.entitlement.feature]
  return config?.description || 'Feature description not available'
}

const getStatusClass = () => {
  if (!props.entitlement.isEnabled) {
    return 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
  }
  
  if (props.entitlement.limitValue === 0) {
    return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
  }
  
  if (usagePercentage.value >= 100) {
    return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
  }
  
  if (usagePercentage.value >= 80) {
    return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200'
  }
  
  return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
}

const getStatusText = () => {
  if (!props.entitlement.isEnabled) return 'Disabled'
  if (props.entitlement.limitValue === 0) return 'Unlimited'
  if (usagePercentage.value >= 100) return 'Limit Reached'
  if (usagePercentage.value >= 80) return 'Warning'
  return 'Active'
}

const getProgressBarClass = () => {
  if (props.entitlement.limitValue === 0) return 'bg-gray-400'
  if (usagePercentage.value >= 100) return 'bg-red-500'
  if (usagePercentage.value >= 80) return 'bg-yellow-500'
  return 'bg-green-500'
}

const getUsageText = () => {
  if (props.entitlement.limitValue === 0) return 'Unlimited usage'
  return `${usagePercentage.value}% used`
}

const getUnit = () => {
  // Extract unit from usage limit type
  const limitType = props.entitlement.usageLimitType.toLowerCase()
  if (limitType.includes('message')) return 'message'
  if (limitType.includes('api')) return 'call'
  if (limitType.includes('storage') || limitType.includes('file')) return 'MB'
  if (limitType.includes('user')) return 'user'
  if (limitType.includes('bot')) return 'bot'
  return 'unit'
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
