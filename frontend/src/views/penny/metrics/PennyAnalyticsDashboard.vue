<template>
  <div class="dashboard p-4">
    <!-- Header -->
    <div class="mt-2 w-full">
      <div class="lg:flex grid-cols-1 lg:space-y-0 space-y-3 gap-5 justify-between">
        <div>
          <p class="uppercase text-xs text-gray-700 font-semibold">Penny Analytics</p>
          <h1 class="text-2xl text-gray-900 dark:text-gray-200 font-medium">
            Analytics Dashboard
          </h1>
        </div>
        <div class="flex gap-2">
          <select
            v-model="selectedBotId"
            @change="loadAnalytics"
            class="bg-white dark:bg-gray-800 hover:border-gray-200 dark:hover:bg-gray-700 dark:text-white dark:border-gray-700 border rounded py-2 px-3"
          >
            <option value="">All Bots</option>
            <option v-for="bot in availableBots" :key="bot.id" :value="bot.id">
              {{ bot.botName }}
            </option>
          </select>
          <select
            v-model="timeRange"
            @change="loadAnalytics"
            class="bg-white dark:bg-gray-800 hover:border-gray-200 dark:hover:bg-gray-700 dark:text-white dark:border-gray-700 border rounded py-2 px-3"
          >
            <option value="1day">Last 24 Hours</option>
            <option value="7days">Last 7 Days</option>
            <option value="30days">Last 30 Days</option>
            <option value="90days">Last 90 Days</option>
          </select>
          <button
            @click="loadAnalytics"
            :disabled="loading"
            class="bg-white dark:bg-gray-800 hover:border-gray-200 dark:hover:bg-gray-700 dark:text-white dark:border-gray-700 border rounded py-2 px-5 flex items-center gap-2"
          >
            <Icon icon="mdi:refresh" :class="{'animate-spin': loading}" class="text-lg" />
            Refresh
          </button>
        </div>
      </div>
    </div>

    <!-- Analytics Summary Cards -->
    <div class="wrapper-card grid lg:grid-cols-4 grid-cols-1 md:grid-cols-2 gap-4 mt-6">
      <div class="card bg-white dark:bg-gray-800 w-full rounded-md p-5 border dark:border-gray-700 flex">
        <div class="p-2 max-w-sm">
          <div class="bg-blue-200 rounded-full w-14 h-14 text-lg p-3 text-blue-600 mx-auto">
            <Icon icon="mdi:message-text" class="text-2xl" />
          </div>
        </div>
        <div class="block p-2 w-full">
          <p class="font-semibold text-gray-900 dark:text-gray-200 text-xl">
            {{ formatNumber(analyticsSummary.totalMessages) }}
          </p>
          <h2 class="font-normal text-gray-400 text-md mt-1">Total Messages</h2>
          <div class="flex items-center mt-2">
            <span class="text-blue-500 text-sm flex items-center">
              <Icon icon="mdi:check-circle" class="mr-1" />
              Tracking
            </span>
          </div>
        </div>
      </div>

      <div class="card bg-white dark:bg-gray-800 w-full rounded-md p-5 border dark:border-gray-700 flex">
        <div class="p-2 max-w-sm">
          <div class="bg-green-200 rounded-full w-14 h-14 text-lg p-3 text-green-600 mx-auto">
            <Icon icon="mdi:alert" class="text-2xl" />
          </div>
        </div>
        <div class="block p-2 w-full">
          <p class="font-semibold text-gray-900 dark:text-gray-200 text-xl">
            {{ formatNumber(analyticsSummary.totalErrors) }}
          </p>
          <h2 class="font-normal text-gray-400 text-md mt-1">Total Errors</h2>
          <div class="flex items-center mt-2">
            <span :class="analyticsSummary.errorRate < 0.05 ? 'text-green-500' : 'text-red-500'" class="text-sm flex items-center">
              <Icon :icon="analyticsSummary.errorRate < 0.05 ? 'mdi:check-circle' : 'mdi:alert'" class="mr-1" />
              {{ analyticsSummary.errorRate < 0.05 ? 'Good' : 'High' }}
            </span>
          </div>
        </div>
      </div>

      <div class="card bg-white dark:bg-gray-800 w-full rounded-md p-5 border dark:border-gray-700 flex">
        <div class="p-2 max-w-sm">
          <div class="bg-purple-200 rounded-full w-14 h-14 text-lg p-3 text-purple-600 mx-auto">
            <Icon icon="mdi:speedometer" class="text-2xl" />
          </div>
        </div>
        <div class="block p-2 w-full">
          <p class="font-semibold text-gray-900 dark:text-gray-200 text-xl">
            {{ formatNumber(analyticsSummary.averageProcessingTime) }}ms
          </p>
          <h2 class="font-normal text-gray-400 text-md mt-1">Avg Response Time</h2>
          <div class="flex items-center mt-2">
            <span class="text-blue-500 text-sm flex items-center">
              <Icon icon="mdi:speedometer" class="mr-1" />
              Performance
            </span>
          </div>
        </div>
      </div>

      <div class="card bg-white dark:bg-gray-800 w-full rounded-md p-5 border dark:border-gray-700 flex">
        <div class="p-2 max-w-sm">
          <div class="bg-orange-200 rounded-full w-14 h-14 text-lg p-3 text-orange-600 mx-auto">
            <Icon icon="mdi:chart-bar" class="text-2xl" />
          </div>
        </div>
        <div class="block p-2 w-full">
          <p class="font-semibold text-gray-900 dark:text-gray-200 text-xl truncate">
            {{ analyticsSummary.mostUsedProvider || 'N/A' }}
          </p>
          <h2 class="font-normal text-gray-400 text-md mt-1">Most Used Provider</h2>
          <div class="flex items-center mt-2">
            <span class="text-green-500 text-sm flex items-center">
              <Icon icon="mdi:check-circle" class="mr-1" />
              Active
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Analytics Events Table -->
    <div class="mt-6 bg-white dark:bg-gray-800 p-6 rounded-md border dark:border-gray-700">
      <div class="flex justify-between items-center mb-4">
        <h2 class="font-medium text-sm text-gray-800 dark:text-gray-200">Recent Analytics Events</h2>
      </div>
      <div v-if="loadingEvents" class="text-center py-4">
        <Icon icon="mdi:loading" class="text-6xl text-gray-300 animate-spin mx-auto" />
        <p class="mt-2 text-gray-500">Loading analytics events...</p>
      </div>
      <div v-else-if="analyticsEvents.length === 0" class="text-center py-4 text-gray-500">
        No analytics events found
      </div>
      <div v-else class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
          <thead class="bg-gray-50 dark:bg-gray-700">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                Timestamp
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                Event Type
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                Intent
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                Provider
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                Processing Time
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                Status
              </th>
            </tr>
          </thead>
          <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
            <tr v-for="event in analyticsEvents" :key="event.id">
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                {{ formatDateTime(event.timestamp) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                {{ event.eventType }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                {{ event.intent || '-' }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                {{ event.providerUsed || '-' }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                {{ event.processingTimeMs ? event.processingTimeMs + 'ms' : '-' }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span
                  :class="[
                    'px-2 py-1 text-xs rounded-full',
                    event.hasError
                      ? 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
                      : 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
                  ]"
                >
                  {{ event.hasError ? 'Error' : 'Success' }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <!-- Pagination -->
      <div v-if="totalPages > 1" class="flex justify-center mt-4 space-x-2">
        <button
          @click="loadEvents(currentPage - 1)"
          :disabled="currentPage === 1"
          class="px-3 py-1 border border-gray-300 dark:border-gray-600 rounded bg-white dark:bg-gray-700 text-gray-900 dark:text-white disabled:opacity-50"
        >
          Previous
        </button>
        <span class="px-3 py-1 text-gray-900 dark:text-white">
          Page {{ currentPage }} of {{ totalPages }}
        </span>
        <button
          @click="loadEvents(currentPage + 1)"
          :disabled="currentPage === totalPages"
          class="px-3 py-1 border border-gray-300 dark:border-gray-600 rounded bg-white dark:bg-gray-700 text-gray-900 dark:text-white disabled:opacity-50"
        >
          Next
        </button>
      </div>
    </div>

    <!-- Intent Distribution Chart -->
    <div class="mt-6 bg-white dark:bg-gray-800 p-6 rounded-md border dark:border-gray-700">
      <div class="flex justify-between items-center mb-4">
        <h2 class="font-medium text-sm text-gray-800 dark:text-gray-200">Intent Distribution</h2>
      </div>
      <div v-if="loadingAnalytics" class="text-center py-4">
        <Icon icon="mdi:loading" class="text-6xl text-gray-300 animate-spin mx-auto" />
        <p class="mt-2 text-gray-500">Loading intent distribution...</p>
      </div>
      <div v-else class="space-y-3">
        <div
          v-for="(count, intent) in intentDistribution"
          :key="intent"
          class="flex items-center p-3 bg-gray-50 dark:bg-gray-700 rounded-lg"
        >
          <span class="w-40 text-sm text-gray-600 dark:text-gray-400">{{ intent }}</span>
          <div class="flex-1 mx-4">
            <div class="w-full bg-gray-200 dark:bg-gray-600 rounded-full h-3">
              <div
                class="bg-indigo-500 h-3 rounded-full transition-all duration-300"
                :style="{ width: calculateIntentPercentage(count) + '%' }"
              ></div>
            </div>
          </div>
          <span class="text-sm font-medium text-gray-900 dark:text-gray-200 w-16 text-right">
            {{ formatNumber(count) }}
          </span>
          <span class="text-sm text-gray-500 dark:text-gray-400 w-16 text-right">
            {{ calculateIntentPercentage(count).toFixed(1) }}%
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Icon } from '@iconify/vue';
import { pennyApi } from '@/api/pennyApi';

export default {
  name: 'PennyAnalyticsDashboard',
  components: {
    Icon
  },
  data() {
    return {
      loading: false,
      loadingAnalytics: false,
      loadingEvents: false,
      selectedBotId: '',
      timeRange: '7days',
      currentPage: 1,
      pageSize: 50,
      totalPages: 1,
      availableBots: [],
      analyticsSummary: {
        totalMessages: 0,
        totalErrors: 0,
        errorRate: 0,
        averageProcessingTime: 0,
        mostUsedProvider: null,
        mostCommonIntent: null
      },
      analyticsEvents: [],
      intentDistribution: {}
    };
  },
  mounted() {
    this.loadAvailableBots();
    this.loadAnalytics();
  },
  methods: {
    async loadAvailableBots() {
      try {
        const response = await pennyApi.getMyPennyBots();
        this.availableBots = response.data;
      } catch (error) {
        console.error('Error loading available bots:', error);
      }
    },
    async loadAnalytics() {
      this.currentPage = 1;
      await Promise.all([
        this.loadAnalyticsSummary(),
        this.loadEvents()
      ]);
    },
    async loadAnalyticsSummary() {
      try {
        this.loadingAnalytics = true;
        const response = await pennyApi.getAnalyticsSummary(
          this.selectedBotId,
          this.timeRange
        );
        this.analyticsSummary = response.data;
        this.intentDistribution = response.data.intentCounts || {};
      } catch (error) {
        console.error('Error loading analytics summary:', error);
      } finally {
        this.loadingAnalytics = false;
      }
    },
    async loadEvents(page = 1) {
      try {
        this.loadingEvents = true;
        this.currentPage = page;
        const response = await pennyApi.getAnalyticsEvents(
          this.selectedBotId,
          this.timeRange,
          page - 1,
          this.pageSize
        );
        this.analyticsEvents = response.data.content || [];
        this.totalPages = response.data.totalPages || 1;
      } catch (error) {
        console.error('Error loading analytics events:', error);
      } finally {
        this.loadingEvents = false;
      }
    },
    formatNumber(num) {
      if (num === null || num === undefined) return 0;
      return num.toLocaleString();
    },
    formatDateTime(date) {
      if (!date) return '-';
      return new Date(date).toLocaleString();
    },
    calculateIntentPercentage(count) {
      const total = Object.values(this.intentDistribution).reduce((a, b) => a + b, 0);
      if (total === 0) return 0;
      return (count / total) * 100;
    }
  }
};
</script>

<style scoped>
.animate-spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
