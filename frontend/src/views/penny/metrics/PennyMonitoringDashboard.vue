<template>
  <div class="dashboard p-4">
    <!-- Header -->
    <div class="mt-2 w-full">
      <div class="lg:flex grid-cols-1 lg:space-y-0 space-y-3 gap-5 justify-between">
        <div>
          <p class="uppercase text-xs text-gray-700 font-semibold">Penny Monitoring</p>
          <h1 class="text-2xl text-gray-900 dark:text-gray-200 font-medium">
            Monitoring Dashboard
          </h1>
        </div>
        <div class="flex gap-2">
          <button
            @click="refreshMetrics"
            :disabled="loading"
            class="bg-white dark:bg-gray-800 hover:border-gray-200 dark:hover:bg-gray-700 dark:text-white dark:border-gray-700 border rounded py-2 px-5 flex items-center gap-2"
          >
            <Icon icon="mdi:refresh" :class="{'animate-spin': loading}" class="text-lg" />
            Refresh
          </button>
        </div>
      </div>
    </div>

    <!-- System Metrics Summary -->
    <div class="wrapper-card grid lg:grid-cols-4 grid-cols-1 md:grid-cols-2 gap-4 mt-6">
      <div class="card bg-white dark:bg-gray-800 w-full rounded-md p-5 border dark:border-gray-700 flex">
        <div class="p-2 max-w-sm">
          <div class="bg-blue-200 rounded-full w-14 h-14 text-lg p-3 text-blue-600 mx-auto">
            <Icon icon="mdi:message-processing" class="text-2xl" />
          </div>
        </div>
        <div class="block p-2 w-full">
          <p class="font-semibold text-gray-900 dark:text-gray-200 text-xl">
            {{ formatNumber(metricsSummary.messagesProcessed) }}
          </p>
          <h2 class="font-normal text-gray-400 text-md mt-1">Messages Processed</h2>
          <div class="flex items-center mt-2">
            <span class="text-blue-500 text-sm flex items-center">
              <Icon icon="mdi:check-circle" class="mr-1" />
              Processing
            </span>
          </div>
        </div>
      </div>

      <div class="card bg-white dark:bg-gray-800 w-full rounded-md p-5 border dark:border-gray-700 flex">
        <div class="p-2 max-w-sm">
          <div class="bg-green-200 rounded-full w-14 h-14 text-lg p-3 text-green-600 mx-auto">
            <Icon icon="mdi:alert-circle" class="text-2xl" />
          </div>
        </div>
        <div class="block p-2 w-full">
          <p class="font-semibold text-gray-900 dark:text-gray-200 text-xl">
            {{ formatPercentage(metricsSummary.errorRate) }}
          </p>
          <h2 class="font-normal text-gray-400 text-md mt-1">Error Rate</h2>
          <div class="flex items-center mt-2">
            <span :class="metricsSummary.errorRate < 0.05 ? 'text-green-500' : 'text-red-500'" class="text-sm flex items-center">
              <Icon :icon="metricsSummary.errorRate < 0.05 ? 'mdi:check-circle' : 'mdi:alert'" class="mr-1" />
              {{ metricsSummary.errorRate < 0.05 ? 'Healthy' : 'Warning' }}
            </span>
          </div>
        </div>
      </div>

      <div class="card bg-white dark:bg-gray-800 w-full rounded-md p-5 border dark:border-gray-700 flex">
        <div class="p-2 max-w-sm">
          <div class="bg-purple-200 rounded-full w-14 h-14 text-lg p-3 text-purple-600 mx-auto">
            <Icon icon="mdi:timer" class="text-2xl" />
          </div>
        </div>
        <div class="block p-2 w-full">
          <p class="font-semibold text-gray-900 dark:text-gray-200 text-xl">
            {{ formatNumber(metricsSummary.averageProcessingTime) }}ms
          </p>
          <h2 class="font-normal text-gray-400 text-md mt-1">Avg Processing Time</h2>
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
            <Icon icon="mdi:robot" class="text-2xl" />
          </div>
        </div>
        <div class="block p-2 w-full">
          <p class="font-semibold text-gray-900 dark:text-gray-200 text-xl">
            {{ metricsSummary.activeBots }}
          </p>
          <h2 class="font-normal text-gray-400 text-md mt-1">Active Bots</h2>
          <div class="flex items-center mt-2">
            <span class="text-green-500 text-sm flex items-center">
              <Icon icon="mdi:check-circle" class="mr-1" />
              Online
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Circuit Breaker Status -->
    <div class="mt-6 bg-white dark:bg-gray-800 p-6 rounded-md border dark:border-gray-700">
      <div class="flex justify-between items-center mb-4">
        <h2 class="font-medium text-sm text-gray-800 dark:text-gray-200">Circuit Breaker Status</h2>
      </div>
      <div v-if="loadingCircuitBreaker" class="text-center py-4">
        <Icon icon="mdi:loading" class="text-6xl text-gray-300 animate-spin mx-auto" />
        <p class="mt-2 text-gray-500">Loading circuit breaker status...</p>
      </div>
      <div v-else-if="Object.keys(circuitBreakerStatus).length === 0" class="text-center py-4 text-gray-500">
        No circuit breakers configured
      </div>
      <div v-else class="space-y-3">
        <div
          v-for="(status, provider) in circuitBreakerStatus"
          :key="provider"
          class="flex items-center p-3 bg-gray-50 dark:bg-gray-700 rounded-lg"
        >
          <div
            :class="[
              'p-2 rounded-full mr-3',
              status.isOpen ? 'bg-red-500' : status.isHalfOpen ? 'bg-yellow-500' : 'bg-green-500'
            ]"
          >
            <Icon :icon="status.isOpen ? 'mdi:alert' : status.isHalfOpen ? 'mdi:alert-circle' : 'mdi:check-circle'" class="text-white text-xl" />
          </div>
          <div class="ml-3 flex-1">
            <p class="font-medium text-gray-900 dark:text-gray-200">{{ provider }}</p>
            <p class="text-sm text-gray-500">{{ status.isOpen ? 'OPEN' : status.isHalfOpen ? 'HALF_OPEN' : 'CLOSED' }}</p>
          </div>
          <div class="text-sm text-gray-400">
            Failures: {{ status.failureCount }}
          </div>
        </div>
      </div>
    </div>

    <!-- Provider Metrics -->
    <div class="mt-6 bg-white dark:bg-gray-800 p-6 rounded-md border dark:border-gray-700">
      <div class="flex justify-between items-center mb-4">
        <h2 class="font-medium text-sm text-gray-800 dark:text-gray-200">Provider Usage</h2>
      </div>
      <div v-if="loadingProviderMetrics" class="text-center py-4">
        <Icon icon="mdi:loading" class="text-6xl text-gray-300 animate-spin mx-auto" />
        <p class="mt-2 text-gray-500">Loading provider metrics...</p>
      </div>
      <div v-else class="grid lg:grid-cols-3 grid-cols-1 gap-4">
        <div
          v-for="(count, provider) in providerUsage"
          :key="provider"
          class="p-4 bg-gray-50 dark:bg-gray-700 rounded-lg"
        >
          <div class="flex items-center justify-between mb-2">
            <span class="font-medium text-gray-900 dark:text-gray-200">{{ provider }}</span>
            <span class="text-2xl font-bold text-primary">{{ formatNumber(count) }}</span>
          </div>
          <div class="w-full bg-gray-200 dark:bg-gray-600 rounded-full h-2">
            <div
              class="bg-primary h-2 rounded-full"
              :style="{ width: calculatePercentage(count) + '%' }"
            ></div>
          </div>
        </div>
      </div>
    </div>

    <!-- Intent Distribution -->
    <div class="mt-6 bg-white dark:bg-gray-800 p-6 rounded-md border dark:border-gray-700">
      <div class="flex justify-between items-center mb-4">
        <h2 class="font-medium text-sm text-gray-800 dark:text-gray-200">Intent Distribution</h2>
      </div>
      <div v-if="loadingIntentMetrics" class="text-center py-4">
        <Icon icon="mdi:loading" class="text-6xl text-gray-300 animate-spin mx-auto" />
        <p class="mt-2 text-gray-500">Loading intent metrics...</p>
      </div>
      <div v-else class="space-y-3">
        <div
          v-for="(count, intent) in intentCounts"
          :key="intent"
          class="flex items-center p-3 bg-gray-50 dark:bg-gray-700 rounded-lg"
        >
          <span class="w-32 text-sm text-gray-600 dark:text-gray-400">{{ intent }}</span>
          <div class="flex-1 mx-4">
            <div class="w-full bg-gray-200 dark:bg-gray-600 rounded-full h-2">
              <div
                class="bg-purple-500 h-2 rounded-full"
                :style="{ width: calculateIntentPercentage(count) + '%' }"
              ></div>
            </div>
          </div>
          <span class="text-sm font-medium text-gray-900 dark:text-gray-200">{{ formatNumber(count) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Icon } from '@iconify/vue';
import { pennyApi } from '@/api/pennyApi';

export default {
  name: 'PennyMonitoringDashboard',
  components: {
    Icon
  },
  data() {
    return {
      loading: false,
      loadingCircuitBreaker: false,
      loadingProviderMetrics: false,
      loadingIntentMetrics: false,
      metricsSummary: {
        messagesProcessed: 0,
        messageErrors: 0,
        errorRate: 0,
        averageProcessingTime: 0,
        activeBots: 0,
        customRulesMatched: 0,
        templatesMatched: 0,
        providerFallbacks: 0
      },
      circuitBreakerStatus: {},
      providerUsage: {},
      intentCounts: {},
      refreshInterval: null
    };
  },
  mounted() {
    this.loadAllMetrics();
    // Auto-refresh every 30 seconds
    this.refreshInterval = setInterval(() => {
      this.loadAllMetrics();
    }, 30000);
  },
  beforeUnmount() {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval);
    }
  },
  methods: {
    async loadAllMetrics() {
      await Promise.all([
        this.loadMetricsSummary(),
        this.loadCircuitBreakerStatus(),
        this.loadProviderMetrics()
      ]);
    },
    async loadMetricsSummary() {
      try {
        this.loading = true;
        const response = await pennyApi.getPennyMetricsSummary();
        this.metricsSummary = response.data;
      } catch (error) {
        console.error('Error loading metrics summary:', error);
      } finally {
        this.loading = false;
      }
    },
    async loadCircuitBreakerStatus() {
      try {
        this.loadingCircuitBreaker = true;
        const response = await pennyApi.getCircuitBreakerStatus();
        this.circuitBreakerStatus = response.data;
      } catch (error) {
        console.error('Error loading circuit breaker status:', error);
      } finally {
        this.loadingCircuitBreaker = false;
      }
    },
    async loadProviderMetrics() {
      try {
        this.loadingProviderMetrics = true;
        const response = await pennyApi.getProviderMetrics();
        this.providerUsage = response.data.providerUsage || {};
        this.intentCounts = response.data.intentCounts || {};
      } catch (error) {
        console.error('Error loading provider metrics:', error);
      } finally {
        this.loadingProviderMetrics = false;
      }
    },
    refreshMetrics() {
      this.loadAllMetrics();
    },
    formatNumber(num) {
      if (num === null || num === undefined) return 0;
      return num.toLocaleString();
    },
    formatPercentage(num) {
      if (num === null || num === undefined) return '0%';
      return (num * 100).toFixed(2) + '%';
    },
    formatDateTime(date) {
      if (!date) return '-';
      return new Date(date).toLocaleString();
    },
    calculatePercentage(count) {
      const total = Object.values(this.providerUsage).reduce((a, b) => a + b, 0);
      if (total === 0) return 0;
      return (count / total) * 100;
    },
    calculateIntentPercentage(count) {
      const total = Object.values(this.intentCounts).reduce((a, b) => a + b, 0);
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
