<template>
  <div class="payment-method-card bg-white dark:bg-gray-800 rounded-lg border dark:border-gray-700 p-4">
    <div class="flex items-center justify-between">
      <!-- Payment Method Info -->
      <div class="flex items-center space-x-3">
        <div class="w-10 h-10 rounded-full flex items-center justify-center"
             :class="getIconBackgroundClass()">
          <Icon :icon="getPaymentIcon()" class="w-5 h-5" :class="getIconClass()" />
        </div>
        <div>
          <div class="flex items-center space-x-2">
            <p class="font-medium text-gray-900 dark:text-white">
              {{ method.type }}
            </p>
            <span v-if="method.isDefault" 
                  class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200">
              Default
            </span>
          </div>
          <p class="text-sm text-gray-500 dark:text-gray-400">
            {{ getPaymentDescription() }}
          </p>
          <p class="text-xs text-gray-400 dark:text-gray-500">
            Added {{ formatDate(method.createdAt) }}
          </p>
        </div>
      </div>

      <!-- Actions -->
      <div class="flex items-center space-x-2">
        <button
          v-if="!method.isDefault"
          @click="$emit('default', method.id)"
          class="p-2 text-gray-400 hover:text-gray-500 dark:hover:text-gray-300"
          title="Set as default"
        >
          <Icon icon="mdi:star-outline" class="w-4 h-4" />
        </button>
        <button
          @click="showDetails = !showDetails"
          class="p-2 text-gray-400 hover:text-gray-500 dark:hover:text-gray-300"
          title="View details"
        >
          <Icon :icon="showDetails ? 'mdi:chevron-up' : 'mdi:chevron-down'" class="w-4 h-4" />
        </button>
        <button
          @click="showDeleteConfirm = true"
          class="p-2 text-red-400 hover:text-red-500 dark:hover:text-red-300"
          title="Remove payment method"
        >
          <Icon icon="mdi:delete-outline" class="w-4 h-4" />
        </button>
      </div>
    </div>

    <!-- Expandable Details -->
    <div v-if="showDetails" class="mt-4 pt-4 border-t border-gray-200 dark:border-gray-700">
      <div class="space-y-2 text-sm">
        <div class="flex justify-between">
          <span class="text-gray-500 dark:text-gray-400">Payment Method ID:</span>
          <span class="font-mono text-gray-900 dark:text-white">#{{ method.id }}</span>
        </div>
        <div class="flex justify-between">
          <span class="text-gray-500 dark:text-gray-400">Status:</span>
          <span class="text-gray-900 dark:text-white">{{ method.status || 'Active' }}</span>
        </div>
        <div class="flex justify-between">
          <span class="text-gray-500 dark:text-gray-400">Expiry:</span>
          <span class="text-gray-900 dark:text-white">{{ method.expiry || 'N/A' }}</span>
        </div>
        <div v-if="method.billingAddress" class="flex justify-between">
          <span class="text-gray-500 dark:text-gray-400">Billing Address:</span>
          <span class="text-gray-900 dark:text-white text-right">{{ method.billingAddress }}</span>
        </div>
      </div>
    </div>

    <!-- Delete Confirmation -->
    <div v-if="showDeleteConfirm" class="mt-4 p-3 bg-red-50 dark:bg-red-900/20 rounded-lg">
      <p class="text-sm text-red-800 dark:text-red-200 mb-3">
        Are you sure you want to remove this payment method?
      </p>
      <div class="flex space-x-2">
        <button
          @click="showDeleteConfirm = false"
          class="flex-1 px-3 py-1 text-sm border border-red-300 dark:border-red-600 rounded text-red-700 dark:text-red-300 hover:bg-red-100 dark:hover:bg-red-900/30"
        >
          Cancel
        </button>
        <button
          @click="handleDelete"
          class="flex-1 px-3 py-1 text-sm bg-red-600 text-white rounded hover:bg-red-700"
        >
          Remove
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  method: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['default', 'remove'])

const showDetails = ref(false)
const showDeleteConfirm = ref(false)

// Payment method configurations
const paymentMethodConfig = {
  'Credit Card': {
    icon: 'mdi:credit-card',
    iconBg: 'bg-blue-100 dark:bg-blue-900',
    iconColor: 'text-blue-600 dark:text-blue-400'
  },
  'Debit Card': {
    icon: 'mdi:credit-card',
    iconBg: 'bg-green-100 dark:bg-green-900',
    iconColor: 'text-green-600 dark:text-green-400'
  },
  'PayPal': {
    icon: 'mdi:paypal',
    iconBg: 'bg-blue-100 dark:bg-blue-900',
    iconColor: 'text-blue-600 dark:text-blue-400'
  },
  'Bank Transfer': {
    icon: 'mdi:bank',
    iconBg: 'bg-purple-100 dark:bg-purple-900',
    iconColor: 'text-purple-600 dark:text-purple-400'
  },
  'Apple Pay': {
    icon: 'mdi:apple',
    iconBg: 'bg-gray-100 dark:bg-gray-900',
    iconColor: 'text-gray-600 dark:text-gray-400'
  },
  'Google Pay': {
    icon: 'mdi:google',
    iconBg: 'bg-gray-100 dark:bg-gray-900',
    iconColor: 'text-gray-600 dark:text-gray-400'
  }
}

const getPaymentIcon = () => {
  const config = paymentMethodConfig[props.method.type]
  return config?.icon || 'mdi:credit-card'
}

const getIconBackgroundClass = () => {
  const config = paymentMethodConfig[props.method.type]
  return config?.iconBg || 'bg-gray-100 dark:bg-gray-900'
}

const getIconClass = () => {
  const config = paymentMethodConfig[props.method.type]
  return config?.iconColor || 'text-gray-600 dark:text-gray-400'
}

const getPaymentDescription = () => {
  switch (props.method.type) {
    case 'Credit Card':
    case 'Debit Card':
      return `••••• ${props.method.last4}`
    case 'PayPal':
      return props.method.email || props.method.last4
    case 'Bank Transfer':
      return `Account ${props.method.last4}`
    default:
      return props.method.last4 || 'Payment method'
  }
}

const formatDate = (date) => {
  if (!date) return 'N/A'
  return new Intl.DateTimeFormat('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric'
  }).format(new Date(date))
}

const handleDelete = () => {
  emit('remove', props.method.id)
  showDeleteConfirm.value = false
}
</script>
