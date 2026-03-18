<template>
  <div
    @click="$emit('select')"
    :class="[
      'p-4 border-b dark:border-gray-700 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors',
      isSelected ? 'bg-blue-50 dark:bg-blue-900/20 border-l-4 border-l-blue-500' : ''
    ]"
  >
    <div class="flex items-start justify-between">
      <div class="flex items-start gap-3 flex-1 min-w-0">
        <!-- Selection Checkbox -->
        <div class="flex-shrink-0 mt-1">
          <input
            type="checkbox"
            :checked="isSelectedForProcessing"
            @change="$emit('toggle-select')"
            @click.stop
            class="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
          />
        </div>
        
        <!-- Customer Avatar -->
        <div class="flex-shrink-0">
          <img 
            v-if="customer.displayAvatar"
            :src="customer.displayAvatar" 
            :alt="customer.displayName || customer.psid"
            class="w-10 h-10 rounded-full object-cover"
            @error="handleImageError"
          />
          <div 
            v-else
            class="w-10 h-10 rounded-full bg-gray-300 dark:bg-gray-600 flex items-center justify-center"
          >
            <Icon icon="mdi:account" class="text-gray-600 dark:text-gray-300 text-xl" />
          </div>
        </div>
        
        <!-- Customer Content -->
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-2 mb-1">
            <h3 class="font-medium text-gray-900 dark:text-gray-200 truncate">
              {{ customer.displayName || customer.psid || $t('customers.unknownCustomer') }}
            </h3>
            <span 
              :class="getStatusClass(customer.status)"
              class="text-xs px-2 py-1 rounded-full"
            >
              {{ $t(`customers.status.${customer.status}`) }}
            </span>
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400 truncate">
            {{ customer.primaryPhone || $t('customers.noPhone') }}
          </p>
          <div class="flex items-center gap-2 mt-1">
            <span class="text-xs text-gray-500">
              {{ formatDate(customer.updatedAt) }}
            </span>
            <span v-if="customer.isSyncedWithOdoo" class="text-xs text-green-600">
              <Icon icon="mdi:check-circle" class="inline mr-1" />
              {{ $t('customers.synced') }}
            </span>
          </div>
        </div>
      </div>
      <div class="flex flex-col items-end gap-1">
        <Icon icon="mdi:account-details" class="text-gray-400" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { Icon } from '@iconify/vue'

const props = defineProps({
  customer: {
    type: Object,
    required: true
  },
  isSelected: {
    type: Boolean,
    default: false
  },
  isSelectedForProcessing: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['select', 'toggle-select'])

const getStatusClass = (status) => {
  const classes = {
    'PENDING': 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200',
    'COMPLETED': 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200',
    'PUSHED_TO_ODOO': 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
    'FAILED': 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
  }
  return classes[status] || 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleDateString()
}

const handleImageError = (event) => {
  // Fallback to default avatar if image fails to load
  event.target.style.display = 'none'
  const parent = event.target.parentElement
  if (parent) {
    const fallback = parent.querySelector('.bg-gray-300, .dark\\:bg-gray-600')
    if (fallback) {
      fallback.style.display = 'flex'
    }
  }
}
</script>
