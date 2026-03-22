<template>
  <div class="fixed inset-0 z-50 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
      <!-- Background overlay -->
      <div class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" @click="$emit('close')"></div>

      <!-- Modal panel -->
      <div class="inline-block align-bottom bg-white dark:bg-gray-800 rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-4xl sm:w-full">
        <!-- Header -->
        <div class="bg-white dark:bg-gray-800 px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
          <div class="flex items-center justify-between">
            <div>
              <h3 class="text-lg leading-6 font-medium text-gray-900 dark:text-white">
                Upgrade Your Plan
              </h3>
              <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
                Choose the perfect plan for your needs
              </p>
            </div>
            <button
              @click="$emit('close')"
              class="text-gray-400 hover:text-gray-500 dark:hover:text-gray-300"
            >
              <Icon icon="mdi:close" class="w-6 h-6" />
            </button>
          </div>
        </div>

        <!-- Body -->
        <div class="bg-white dark:bg-gray-800 px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
          <!-- Current Plan -->
          <div v-if="currentPlan" class="mb-6 p-4 bg-gray-50 dark:bg-gray-700 rounded-lg">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-gray-500 dark:text-gray-400">Current Plan</p>
                <p class="text-lg font-semibold text-gray-900 dark:text-white">{{ currentPlan.name }}</p>
              </div>
              <div class="text-right">
                <p class="text-sm text-gray-500 dark:text-gray-400">Current Price</p>
                <p class="text-lg font-semibold text-gray-900 dark:text-white">
                  {{ formatCurrency(currentPlan.price, currentPlan.currency) }}/{{ currentPlan.billingCycle }}
                </p>
              </div>
            </div>
          </div>

          <!-- Available Plans -->
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div
              v-for="plan in availablePlans"
              :key="plan.id"
              @click="selectPlan(plan)"
              :class="[
                'p-4 border-2 rounded-lg cursor-pointer transition-all',
                selectedPlan?.id === plan.id
                  ? 'border-primary-500 bg-primary-50 dark:bg-primary-900/20'
                  : 'border-gray-200 dark:border-gray-600 hover:border-gray-300 dark:hover:border-gray-500'
              ]"
            >
              <!-- Plan Header -->
              <div class="text-center mb-4">
                <h4 class="text-lg font-semibold text-gray-900 dark:text-white">{{ plan.name }}</h4>
                <p class="text-sm text-gray-500 dark:text-gray-400">{{ plan.description }}</p>
              </div>

              <!-- Price -->
              <div class="text-center mb-4">
                <p class="text-3xl font-bold text-gray-900 dark:text-white">
                  {{ formatCurrency(plan.price, plan.currency) }}
                </p>
                <p class="text-sm text-gray-500 dark:text-gray-400">per {{ plan.billingCycle }}</p>
              </div>

              <!-- Features -->
              <div class="space-y-2 mb-4">
                <div v-for="feature in plan.features" :key="feature" class="flex items-center text-sm">
                  <Icon icon="mdi:check-circle" class="w-4 h-4 text-green-500 mr-2 flex-shrink-0" />
                  <span class="text-gray-700 dark:text-gray-300">{{ feature }}</span>
                </div>
              </div>

              <!-- Popular Badge -->
              <div v-if="plan.popular" class="text-center">
                <span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-primary-100 text-primary-800 dark:bg-primary-900 dark:text-primary-200">
                  Most Popular
                </span>
              </div>
            </div>
          </div>

          <!-- Comparison -->
          <div v-if="selectedPlan && currentPlan" class="mt-6 p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
            <h4 class="text-sm font-medium text-blue-900 dark:text-blue-200 mb-3">What you'll get:</h4>
            <div class="space-y-2 text-sm">
              <div class="flex justify-between">
                <span class="text-blue-700 dark:text-blue-300">Additional Users:</span>
                <span class="font-medium text-blue-900 dark:text-blue-100">
                  {{ selectedPlan.maxUsers - currentPlan.maxUsers }} more
                </span>
              </div>
              <div class="flex justify-between">
                <span class="text-blue-700 dark:text-blue-300">Storage:</span>
                <span class="font-medium text-blue-900 dark:text-blue-100">
                  {{ formatStorage(selectedPlan.maxStorageMb) }} vs {{ formatStorage(currentPlan.maxStorageMb) }}
                </span>
              </div>
              <div class="flex justify-between">
                <span class="text-blue-700 dark:text-blue-300">API Calls:</span>
                <span class="font-medium text-blue-900 dark:text-blue-100">
                  {{ selectedPlan.maxApiCallsPerMonth?.toLocaleString() }} vs {{ currentPlan.maxApiCallsPerMonth?.toLocaleString() }}
                </span>
              </div>
            </div>
          </div>

          <!-- Action Buttons -->
          <div class="flex space-x-3 mt-6">
            <button
              type="button"
              @click="$emit('close')"
              class="flex-1 px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
            >
              Cancel
            </button>
            <button
              type="button"
              @click="handleUpgrade"
              :disabled="!selectedPlan || loading"
              class="flex-1 px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-primary-600 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <Icon v-if="loading" icon="mdi:loading" class="w-4 h-4 mr-2 animate-spin" />
              {{ loading ? 'Upgrading...' : 'Upgrade Now' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  currentPlan: {
    type: Object,
    required: true
  },
  availablePlans: {
    type: Array,
    required: true
  }
})

const emit = defineEmits(['close', 'upgrade'])

const selectedPlan = ref(null)
const loading = ref(false)

// Methods
const selectPlan = (plan) => {
  selectedPlan.value = plan
}

const formatCurrency = (amount, currency) => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: currency
  }).format(amount)
}

const formatStorage = (mb) => {
  if (mb < 1024) {
    return `${mb} MB`
  }
  return `${(mb / 1024).toFixed(1)} GB`
}

const handleUpgrade = async () => {
  if (!selectedPlan.value) return

  loading.value = true

  try {
    emit('upgrade', selectedPlan.value.id)
  } catch (error) {
    console.error('Upgrade failed:', error)
  } finally {
    loading.value = false
  }
}
</script>
