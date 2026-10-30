<template>
  <div class="p-6">
    <h1 class="text-2xl font-bold text-gray-900 dark:text-white mb-6">{{ t('analytics.title') }}</h1>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-6">
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">{{ t('analytics.totalCustomers') }}</p>
            <p class="text-3xl font-bold text-gray-900 dark:text-white">{{ stats.totalCustomers }}</p>
          </div>
          <div class="h-12 w-12 bg-blue-100 dark:bg-blue-900 rounded-full flex items-center justify-center">
            <Icon icon="mdi:account-group" class="text-blue-600 dark:text-blue-400 text-2xl" />
          </div>
        </div>
      </div>
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">{{ t('analytics.activeCustomers') }}</p>
            <p class="text-3xl font-bold text-gray-900 dark:text-white">{{ stats.activeCustomers }}</p>
          </div>
          <div class="h-12 w-12 bg-green-100 dark:bg-green-900 rounded-full flex items-center justify-center">
            <Icon icon="mdi:account-check" class="text-green-600 dark:text-green-400 text-2xl" />
          </div>
        </div>
      </div>
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">{{ t('analytics.avgSatisfaction') }}</p>
            <p class="text-3xl font-bold text-gray-900 dark:text-white">{{ stats.avgSatisfaction }}/5</p>
          </div>
          <div class="h-12 w-12 bg-yellow-100 dark:bg-yellow-900 rounded-full flex items-center justify-center">
            <Icon icon="mdi:star" class="text-yellow-600 dark:text-yellow-400 text-2xl" />
          </div>
        </div>
      </div>
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">{{ t('analytics.totalFeedback') }}</p>
            <p class="text-3xl font-bold text-gray-900 dark:text-white">{{ stats.totalFeedback }}</p>
          </div>
          <div class="h-12 w-12 bg-purple-100 dark:bg-purple-900 rounded-full flex items-center justify-center">
            <Icon icon="mdi:comment" class="text-purple-600 dark:text-purple-400 text-2xl" />
          </div>
        </div>
      </div>
    </div>

    <!-- Charts Section -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
      <!-- Customer Status Distribution -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">{{ t('analytics.statusDistribution') }}</h2>
        <div v-if="loading" class="text-center py-8">
          <Icon icon="mdi:loading" class="animate-spin text-4xl text-blue-600" />
        </div>
        <div v-else class="space-y-3">
          <div v-for="(count, status) in statusDistribution" :key="status" class="flex items-center">
            <div class="w-32 text-sm text-gray-600 dark:text-gray-400">{{ t(`customer.status.${status.toLowerCase()}`) }}</div>
            <div class="flex-1 mx-4 bg-gray-200 dark:bg-gray-700 rounded-full h-4">
              <div
                :class="getStatusColor(status)"
                :style="{ width: `${(count / stats.totalCustomers) * 100}%` }"
                class="h-4 rounded-full"
              />
            </div>
            <div class="text-sm font-medium text-gray-900 dark:text-white">{{ count }}</div>
          </div>
        </div>
      </div>

      <!-- Customer Tier Distribution -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">{{ t('analytics.tierDistribution') }}</h2>
        <div v-if="loading" class="text-center py-8">
          <Icon icon="mdi:loading" class="animate-spin text-4xl text-blue-600" />
        </div>
        <div v-else class="space-y-3">
          <div v-for="(count, tier) in tierDistribution" :key="tier" class="flex items-center">
            <div class="w-32 text-sm text-gray-600 dark:text-gray-400">{{ t(`customer.tier.${tier.toLowerCase()}`) }}</div>
            <div class="flex-1 mx-4 bg-gray-200 dark:bg-gray-700 rounded-full h-4">
              <div
                :class="getTierColor(tier)"
                :style="{ width: `${(count / stats.totalCustomers) * 100}%` }"
                class="h-4 rounded-full"
              />
            </div>
            <div class="text-sm font-medium text-gray-900 dark:text-white">{{ count }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Recent Feedback -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('analytics.recentFeedback') }}</h2>
        <button @click="loadRecentFeedback" class="text-blue-600 hover:text-blue-900 dark:text-blue-400 dark:hover:text-blue-300">
          <Icon icon="mdi:refresh" class="text-xl" />
        </button>
      </div>
      <div v-if="loading" class="text-center py-8">
        <Icon icon="mdi:loading" class="animate-spin text-4xl text-blue-600" />
      </div>
      <div v-else-if="recentFeedback.length === 0" class="text-center py-8 text-gray-600 dark:text-gray-400">
        <Icon icon="mdi:comment-off" class="text-4xl mb-2" />
        <p>{{ t('feedback.noFeedback') }}</p>
      </div>
      <div v-else class="space-y-4">
        <div
          v-for="feedback in recentFeedback"
          :key="feedback.id"
          class="border-b border-gray-200 dark:border-gray-700 pb-4 last:border-0"
        >
          <div class="flex items-center justify-between mb-2">
            <div class="flex items-center gap-2">
              <div class="flex">
                <Icon
                  v-for="i in 5"
                  :key="i"
                  :icon="i <= feedback.rating ? 'mdi:star' : 'mdi:star-outline'"
                  :class="i <= feedback.rating ? 'text-yellow-500' : 'text-gray-300 dark:text-gray-600'"
                  class="text-sm"
                />
              </div>
              <span class="text-sm text-gray-500 dark:text-gray-400">{{ formatDate(feedback.submittedAt) }}</span>
            </div>
            <span :class="getStatusClass(feedback.status)" class="px-2 py-1 text-xs font-medium rounded-full">
              {{ t(`feedback.status.${feedback.status?.toLowerCase()}`) }}
            </span>
          </div>
          <p v-if="feedback.comment" class="text-gray-900 dark:text-white text-sm">{{ feedback.comment }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import { useI18n } from 'vue-i18n'
import customerProfileApi from '@/api/customerProfileApi'
import feedbackApi from '@/api/feedbackApi'

const { t } = useI18n()

const loading = ref(true)
const stats = ref({
  totalCustomers: 0,
  activeCustomers: 0,
  avgSatisfaction: 0,
  totalFeedback: 0
})
const statusDistribution = ref({})
const tierDistribution = ref({})
const recentFeedback = ref([])

const loadAnalytics = async () => {
  try {
    loading.value = true
    const [customers, feedback] = await Promise.all([
      customerProfileApi.getCustomerProfiles(),
      feedbackApi.getFeedback()
    ])

    stats.value.totalCustomers = customers.length
    stats.value.activeCustomers = customers.filter(c => c.status === 'ACTIVE').length
    stats.value.totalFeedback = feedback.length

    // Calculate average satisfaction
    const ratings = feedback.filter(f => f.rating != null).map(f => f.rating)
    stats.value.avgSatisfaction = ratings.length > 0
      ? (ratings.reduce((a, b) => a + b, 0) / ratings.length).toFixed(1)
      : 0

    // Status distribution
    statusDistribution.value = customers.reduce((acc, c) => {
      acc[c.status] = (acc[c.status] || 0) + 1
      return acc
    }, {})

    // Tier distribution
    tierDistribution.value = customers.reduce((acc, c) => {
      acc[c.tier] = (acc[c.tier] || 0) + 1
      return acc
    }, {})
  } catch (err) {
    console.error('Load analytics error:', err)
  } finally {
    loading.value = false
  }
}

const loadRecentFeedback = async () => {
  try {
    recentFeedback.value = await feedbackApi.getRecentFeedback(5)
  } catch (err) {
    console.error('Load recent feedback error:', err)
  }
}

const getStatusColor = (status) => {
  const colors = {
    ACTIVE: 'bg-green-500',
    INACTIVE: 'bg-gray-500',
    BLOCKED: 'bg-red-500',
    CHURNED: 'bg-yellow-500'
  }
  return colors[status] || 'bg-gray-500'
}

const getTierColor = (tier) => {
  const colors = {
    FREE: 'bg-gray-400',
    BASIC: 'bg-blue-400',
    PREMIUM: 'bg-purple-400',
    ENTERPRISE: 'bg-yellow-400'
  }
  return colors[tier] || 'bg-gray-400'
}

const getStatusClass = (status) => {
  const classes = {
    PENDING: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200',
    REVIEWED: 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200',
    RESPONDED: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
    CLOSED: 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-200'
  }
  return classes[status] || 'bg-gray-100 text-gray-800'
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString()
}

onMounted(() => {
  loadAnalytics()
  loadRecentFeedback()
})
</script>
