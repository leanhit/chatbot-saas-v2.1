<template>
  <div class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full z-50">
    <div class="relative min-h-screen flex items-center justify-center p-4">
      <div class="relative bg-white dark:bg-gray-800 rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:w-full sm:max-w-4xl">
        <!-- Header -->
        <div class="bg-white dark:bg-gray-800 px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
          <div class="flex items-center justify-between">
            <h3 class="text-lg leading-6 font-medium text-gray-900 dark:text-white">
              {{ $t('Bot Analytics') }} - {{ bot.name }}
            </h3>
            <button
              @click="$emit('close')"
              class="rounded-md text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              <Icon icon="mdi:close" class="h-6 w-6" />
            </button>
          </div>
        </div>

        <!-- Body -->
        <div class="bg-white dark:bg-gray-800 px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
          <!-- Stats Overview -->
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
              <div class="flex items-center">
                <div class="flex-shrink-0">
                  <Icon icon="mdi:message-text" class="h-8 w-8 text-blue-600 dark:text-blue-400" />
                </div>
                <div class="ml-4">
                  <p class="text-sm font-medium text-gray-500 dark:text-gray-400">
                    {{ $t('Total Conversations') }}
                  </p>
                  <p class="text-2xl font-semibold text-gray-900 dark:text-white">
                    {{ analytics.totalConversations.toLocaleString() }}
                  </p>
                </div>
              </div>
            </div>

            <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
              <div class="flex items-center">
                <div class="flex-shrink-0">
                  <Icon icon="mdi:email" class="h-8 w-8 text-green-600 dark:text-green-400" />
                </div>
                <div class="ml-4">
                  <p class="text-sm font-medium text-gray-500 dark:text-gray-400">
                    {{ $t('Total Messages') }}
                  </p>
                  <p class="text-2xl font-semibold text-gray-900 dark:text-white">
                    {{ analytics.totalMessages.toLocaleString() }}
                  </p>
                </div>
              </div>
            </div>

            <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
              <div class="flex items-center">
                <div class="flex-shrink-0">
                  <Icon icon="mdi:clock" class="h-8 w-8 text-yellow-600 dark:text-yellow-400" />
                </div>
                <div class="ml-4">
                  <p class="text-sm font-medium text-gray-500 dark:text-gray-400">
                    {{ $t('Avg Response Time') }}
                  </p>
                  <p class="text-2xl font-semibold text-gray-900 dark:text-white">
                    {{ analytics.avgResponseTime }}s
                  </p>
                </div>
              </div>
            </div>

            <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
              <div class="flex items-center">
                <div class="flex-shrink-0">
                  <Icon icon="mdi:emoticon-happy" class="h-8 w-8 text-purple-600 dark:text-purple-400" />
                </div>
                <div class="ml-4">
                  <p class="text-sm font-medium text-gray-500 dark:text-gray-400">
                    {{ $t('Satisfaction Rate') }}
                  </p>
                  <p class="text-2xl font-semibold text-gray-900 dark:text-white">
                    {{ analytics.satisfactionRate }}%
                  </p>
                </div>
              </div>
            </div>
          </div>

          <!-- Charts Section -->
          <div class="space-y-6">
            <!-- Conversation Trend -->
            <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700">
              <h4 class="text-lg font-medium text-gray-900 dark:text-white mb-4">
                {{ $t('Conversation Trend') }}
              </h4>
              <div class="h-64 flex items-center justify-center bg-gray-50 dark:bg-gray-700 rounded">
                <div class="text-center">
                  <Icon icon="mdi:chart-line" class="h-16 w-16 text-gray-400 mx-auto mb-4" />
                  <p class="text-gray-500 dark:text-gray-400">
                    {{ $t('Chart visualization would go here') }}
                  </p>
                </div>
              </div>
            </div>

            <!-- Response Time Distribution -->
            <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700">
              <h4 class="text-lg font-medium text-gray-900 dark:text-white mb-4">
                {{ $t('Response Time Distribution') }}
              </h4>
              <div class="h-64 flex items-center justify-center bg-gray-50 dark:bg-gray-700 rounded">
                <div class="text-center">
                  <Icon icon="mdi:chart-bar" class="h-16 w-16 text-gray-400 mx-auto mb-4" />
                  <p class="text-gray-500 dark:text-gray-400">
                    {{ $t('Chart visualization would go here') }}
                  </p>
                </div>
              </div>
            </div>
          </div>

          <!-- Bot Health Status -->
          <div class="mt-8">
            <h4 class="text-lg font-medium text-gray-900 dark:text-white mb-4">
              {{ $t('Bot Health Status') }}
            </h4>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700">
                <h5 class="text-md font-medium text-gray-900 dark:text-white mb-4">
                  {{ $t('Overall Status') }}
                </h5>
                <div class="space-y-3">
                  <div class="flex items-center justify-between">
                    <span class="text-sm text-gray-600 dark:text-gray-400">{{ $t('Context Engine') }}</span>
                    <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200">
                      {{ $t('Healthy') }}
                    </span>
                  </div>
                  <div class="flex items-center justify-between">
                    <span class="text-sm text-gray-600 dark:text-gray-400">{{ $t('Analytics System') }}</span>
                    <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200">
                      {{ $t('Healthy') }}
                    </span>
                  </div>
                </div>
              </div>

              <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700">
                <h5 class="text-md font-medium text-gray-900 dark:text-white mb-4">
                  {{ $t('Bot Information') }}
                </h5>
                <dl class="space-y-2">
                  <div class="flex justify-between">
                    <dt class="text-sm text-gray-600 dark:text-gray-400">{{ $t('Bot ID') }}</dt>
                    <dd class="text-sm font-medium text-gray-900 dark:text-white">{{ bot.id }}</dd>
                  </div>
                  <div class="flex justify-between">
                    <dt class="text-sm text-gray-600 dark:text-gray-400">{{ $t('Status') }}</dt>
                    <dd>
                      <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200">
                        {{ bot.isActive ? $t('Online') : $t('Offline') }}
                      </span>
                    </dd>
                  </div>
                  <div class="flex justify-between">
                    <dt class="text-sm text-gray-600 dark:text-gray-400">{{ $t('Last Used') }}</dt>
                    <dd class="text-sm font-medium text-gray-900 dark:text-white">{{ formatDate(bot.lastUsed) }}</dd>
                  </div>
                </dl>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Icon } from '@iconify/vue'

const { t } = useI18n()

const props = defineProps({
  bot: {
    type: Object,
    required: true
  },
  show: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close'])

// Mock analytics data
const analytics = ref({
  totalConversations: 1247,
  totalMessages: 8932,
  avgResponseTime: 2.4,
  satisfactionRate: 94
})

const formatDate = (date) => {
  if (!date) return 'Never'
  return new Intl.DateTimeFormat('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(date))
}

// Close modal when ESC key is pressed
onMounted(() => {
  const handleEscape = (e) => {
    if (e.key === 'Escape' && props.show) {
      emit('close')
    }
  }
  document.addEventListener('keydown', handleEscape)
  
  return () => {
    document.removeEventListener('keydown', handleEscape)
  }
})
</script>

<style scoped>
/* Custom styles for charts and visualizations */
</style>
