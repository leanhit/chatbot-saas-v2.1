<template>
  <div class="fixed inset-0 z-50 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
      <!-- Background overlay -->
      <div class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" @click="$emit('close')"></div>

      <!-- Modal panel -->
      <div class="inline-block align-bottom bg-white dark:bg-gray-800 rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
        <!-- Header -->
        <div class="bg-white dark:bg-gray-800 px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
          <div class="flex items-center justify-between">
            <div>
              <h3 class="text-lg leading-6 font-medium text-gray-900 dark:text-white">
                Cancel Subscription
              </h3>
              <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
                We're sorry to see you go
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
          <!-- Current Subscription Info -->
          <div class="mb-6 p-4 bg-gray-50 dark:bg-gray-700 rounded-lg">
            <div class="flex items-center space-x-3">
              <div class="w-12 h-12 bg-red-100 dark:bg-red-900 rounded-full flex items-center justify-center">
                <Icon icon="mdi:alert-circle" class="w-6 h-6 text-red-600 dark:text-red-400" />
              </div>
              <div>
                <p class="font-medium text-gray-900 dark:text-white">{{ subscription?.plan?.name || 'Current Plan' }}</p>
                <p class="text-sm text-gray-500 dark:text-gray-400">
                  {{ formatCurrency(subscription?.price || 0, subscription?.currency || 'USD') }}/{{ subscription?.billingCycle }}
                </p>
                <p class="text-xs text-gray-400 dark:text-gray-500">
                  Active until {{ formatDate(subscription?.endsAt) }}
                </p>
              </div>
            </div>
          </div>

          <!-- Cancellation Reason -->
          <div class="mb-6">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Why are you cancelling?
            </label>
            <select
              v-model="selectedReason"
              class="block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm dark:bg-gray-700 dark:text-white"
            >
              <option value="">Please select a reason</option>
              <option value="too_expensive">Too expensive</option>
              <option value="missing_features">Missing features</option>
              <option value="switching_service">Switching to another service</option>
              <option value="technical_issues">Technical issues</option>
              <option value="not_using_anymore">Not using anymore</option>
              <option value="other">Other</option>
            </select>
          </div>

          <!-- Custom Reason -->
          <div v-if="selectedReason === 'other'" class="mb-6">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Please tell us more
            </label>
            <textarea
              v-model="customReason"
              rows="3"
              placeholder="Your feedback helps us improve..."
              class="block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm dark:bg-gray-700 dark:text-white"
            ></textarea>
          </div>

          <!-- Warnings -->
          <div class="mb-6 p-4 bg-yellow-50 dark:bg-yellow-900/20 rounded-lg">
            <div class="flex">
              <div class="flex-shrink-0">
                <Icon icon="mdi:alert" class="h-5 w-5 text-yellow-400" />
              </div>
              <div class="ml-3">
                <h3 class="text-sm font-medium text-yellow-800 dark:text-yellow-200">
                  Important Information
                </h3>
                <div class="mt-2 text-sm text-yellow-700 dark:text-yellow-300">
                  <ul class="list-disc list-inside space-y-1">
                    <li>Your subscription will remain active until {{ formatDate(subscription?.endsAt) }}</li>
                    <li>You will lose access to all premium features at that time</li>
                    <li>Your data will be retained for 30 days after cancellation</li>
                    <li>You can reactivate your subscription at any time</li>
                  </ul>
                </div>
              </div>
            </div>
          </div>

          <!-- Alternatives -->
          <div class="mb-6 p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
            <div class="flex">
              <div class="flex-shrink-0">
                <Icon icon="mdi:lightbulb" class="h-5 w-5 text-blue-400" />
              </div>
              <div class="ml-3">
                <h3 class="text-sm font-medium text-blue-800 dark:text-blue-200">
                  Before you go...
                </h3>
                <div class="mt-2 text-sm text-blue-700 dark:text-blue-300">
                  <p>Consider downgrading to our Starter plan instead. You'll still get core features at a lower price.</p>
                  <button
                    type="button"
                    @click="showDowngradeOption = true"
                    class="mt-2 text-sm text-blue-600 dark:text-blue-400 hover:text-blue-800 dark:hover:text-blue-300 font-medium"
                  >
                    Learn about downgrading
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Action Buttons -->
          <div class="flex space-x-3">
            <button
              type="button"
              @click="$emit('close')"
              class="flex-1 px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
            >
              Keep Subscription
            </button>
            <button
              type="button"
              @click="handleCancel"
              :disabled="loading || !selectedReason"
              class="flex-1 px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <Icon v-if="loading" icon="mdi:loading" class="w-4 h-4 mr-2 animate-spin" />
              {{ loading ? 'Cancelling...' : 'Cancel Subscription' }}
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
  subscription: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'cancel'])

const selectedReason = ref('')
const customReason = ref('')
const loading = ref(false)
const showDowngradeOption = ref(false)

// Methods
const formatDate = (date) => {
  if (!date) return 'N/A'
  return new Intl.DateTimeFormat('en-US', {
    month: 'long',
    day: 'numeric',
    year: 'numeric'
  }).format(new Date(date))
}

const handleCancel = async () => {
  if (!selectedReason.value) return

  loading.value = true

  try {
    const reason = selectedReason.value === 'other' ? customReason.value : selectedReason.value
    emit('cancel', reason)
  } catch (error) {
    console.error('Cancellation failed:', error)
  } finally {
    loading.value = false
  }
}
</script>
