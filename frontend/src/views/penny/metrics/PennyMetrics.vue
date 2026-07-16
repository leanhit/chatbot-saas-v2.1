<template>
  <div class="penny-metrics">
    <!-- Header -->
    <div class="flex justify-between items-center mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white">
          {{ $t('penny.metrics.title') }}
        </h1>
        <p class="text-gray-600 dark:text-gray-400 mt-1">
          {{ $t('penny.metrics.subtitle') }}
        </p>
      </div>
      <div class="flex items-center space-x-4">
        <button
          @click="refreshMetrics"
          :disabled="loading"
          class="inline-flex items-center px-4 py-2 bg-primary text-white rounded-md hover:bg-primary/80 transition-colors disabled:opacity-50"
        >
          <Icon icon="mdi:refresh" :class="{'animate-spin': loading}" class="mr-2" />
          {{ $t('penny.metrics.refresh') }}
        </button>
      </div>
    </div>

    <!-- Statistics Cards -->
    <div class="wrapper-card grid lg:grid-cols-4 grid-cols-1 md:grid-cols-2 gap-4 mt-6">
      <!-- Total Bots -->
      <div class="card bg-white dark:bg-gray-800 w-full rounded-md p-5 border border-gray-200 dark:border-gray-700 flex">
        <div class="p-2 max-w-sm">
          <div class="bg-blue-200 rounded-full w-14 h-14 text-lg p-3 text-blue-600 mx-auto flex items-center justify-center">
            <Icon icon="mdi:robot-mumble" class="text-2xl" />
          </div>
        </div>
        <div class="block p-2 w-full">
          <p class="font-semibold text-gray-900 dark:text-gray-200 text-xl">
            {{ systemMetrics.bots?.total || 0 }}
          </p>
          <h2 class="font-normal text-gray-400 text-md mt-1">{{ $t('penny.metrics.totalBots') }}</h2>
          <div class="flex items-center mt-2">
            <span class="text-blue-500 text-sm flex items-center">
              <Icon icon="mdi:check-circle" class="mr-1" />
              Configured
            </span>
          </div>
        </div>
      </div>

      <!-- Active Bots -->
      <div class="card bg-white dark:bg-gray-800 w-full rounded-md p-5 border border-gray-200 dark:border-gray-700 flex">
        <div class="p-2 max-w-sm">
          <div class="bg-green-200 rounded-full w-14 h-14 text-lg p-3 text-green-600 mx-auto flex items-center justify-center">
            <Icon icon="mdi:robot" class="text-2xl" />
          </div>
        </div>
        <div class="block p-2 w-full">
          <p class="font-semibold text-gray-900 dark:text-gray-200 text-xl">
            {{ systemMetrics.bots?.active || 0 }}
          </p>
          <h2 class="font-normal text-gray-400 text-md mt-1">{{ $t('penny.metrics.activeBots') }}</h2>
          <div class="flex items-center mt-2">
            <span class="text-green-500 text-sm flex items-center">
              <Icon icon="mdi:check-circle" class="mr-1" />
              Running
            </span>
          </div>
        </div>
      </div>

      <!-- KB Articles -->
      <div class="card bg-white dark:bg-gray-800 w-full rounded-md p-5 border border-gray-200 dark:border-gray-700 flex">
        <div class="p-2 max-w-sm">
          <div class="bg-purple-200 rounded-full w-14 h-14 text-lg p-3 text-purple-600 mx-auto flex items-center justify-center">
            <Icon icon="mdi:book-open-page-variant" class="text-2xl" />
          </div>
        </div>
        <div class="block p-2 w-full">
          <p class="font-semibold text-gray-900 dark:text-gray-200 text-xl">
            {{ systemMetrics.knowledgeBase?.totalArticles || 0 }}
          </p>
          <h2 class="font-normal text-gray-400 text-md mt-1">{{ $t('penny.metrics.kbArticles') }}</h2>
          <div class="flex items-center mt-2">
            <span class="text-purple-500 text-sm flex items-center">
              <Icon icon="mdi:database" class="mr-1" />
              Indexed
            </span>
          </div>
        </div>
      </div>

      <!-- RAG Enabled -->
      <div class="card bg-white dark:bg-gray-800 w-full rounded-md p-5 border border-gray-200 dark:border-gray-700 flex">
        <div class="p-2 max-w-sm">
          <div class="bg-orange-200 rounded-full w-14 h-14 text-lg p-3 text-orange-600 mx-auto flex items-center justify-center">
            <Icon icon="mdi:brain" class="text-2xl" />
          </div>
        </div>
        <div class="block p-2 w-full">
          <p class="font-semibold text-gray-900 dark:text-gray-200 text-xl" :class="{ 'text-green-500': systemMetrics.knowledgeBase?.ragEnabled, 'text-red-500': !systemMetrics.knowledgeBase?.ragEnabled }">
            {{ systemMetrics.knowledgeBase?.ragEnabled ? $t('common.active') : $t('common.inactive') }}
          </p>
          <h2 class="font-normal text-gray-400 text-md mt-1">{{ $t('penny.metrics.ragEnabled') }}</h2>
          <div class="flex items-center mt-2">
            <span :class="systemMetrics.knowledgeBase?.ragEnabled ? 'text-green-500' : 'text-red-500'" class="text-sm flex items-center">
              <Icon :icon="systemMetrics.knowledgeBase?.ragEnabled ? 'mdi:check-circle' : 'mdi:close-circle'" class="mr-1" />
              Status
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Provider Health -->
    <div class="mt-6 bg-white dark:bg-gray-800 p-6 rounded-md border border-gray-200 dark:border-gray-700">
      <h2 class="font-medium text-sm text-gray-800 dark:text-gray-200 mb-4">{{ $t('penny.metrics.providerHealth') }}</h2>
      <div v-if="loading" class="text-center py-4">
        <Icon icon="mdi:loading" class="text-6xl text-gray-300 animate-spin mx-auto" />
        <p class="mt-2 text-gray-500">{{ $t('penny.metrics.loading') }}</p>
      </div>
      <div v-else-if="Object.keys(providerMetrics).length === 0" class="text-center py-4 text-gray-500">
        {{ $t('penny.metrics.noProviders') }}
      </div>
      <div v-else class="grid lg:grid-cols-2 grid-cols-1 gap-4">
        <div
          v-for="(provider, name) in providerMetrics"
          :key="name"
          class="border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-700/30 rounded-lg p-4 flex flex-col justify-between transition-colors hover:bg-gray-100 dark:hover:bg-gray-700/50"
          :class="provider.healthy ? 'border-l-4 border-l-green-500' : 'border-l-4 border-l-red-500'"
        >
          <div class="flex justify-between items-center mb-3">
            <span class="font-semibold text-gray-900 dark:text-gray-200 text-lg uppercase flex items-center gap-2">
              <Icon :icon="getProviderIcon(name)" class="text-xl" :class="provider.healthy ? 'text-green-500' : 'text-red-500'" />
              {{ name }}
            </span>
            <span class="flex items-center gap-1 text-sm font-medium">
              <span class="w-2.5 h-2.5 rounded-full" :class="provider.healthy ? 'bg-green-500' : 'bg-red-500'"></span>
              <span :class="provider.healthy ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400'">
                {{ provider.healthy ? $t('penny.metrics.healthy') : $t('penny.metrics.unhealthy') }}
              </span>
            </span>
          </div>
          
          <div class="space-y-2 text-sm text-gray-600 dark:text-gray-400">
            <div v-if="provider.lastMessage" class="flex justify-between border-b border-gray-200 dark:border-gray-700 pb-1.5">
              <span class="font-medium text-gray-500 dark:text-gray-400">{{ $t('penny.metrics.lastMessage') }}:</span>
              <span class="font-semibold text-gray-800 dark:text-gray-200">{{ provider.lastMessage }}</span>
            </div>
            <div v-if="provider.lastCheck" class="flex justify-between border-b border-gray-200 dark:border-gray-700 pb-1.5">
              <span class="font-medium text-gray-500 dark:text-gray-400">{{ $t('penny.metrics.lastCheck') }}:</span>
              <span class="font-semibold text-gray-800 dark:text-gray-200">{{ formatDate(provider.lastCheck) }}</span>
            </div>
            <div v-if="provider.consecutiveFailures !== undefined" class="flex justify-between pb-1">
              <span class="font-medium text-gray-500 dark:text-gray-400">{{ $t('penny.metrics.consecutiveFailures') }}:</span>
              <span class="font-semibold text-gray-800 dark:text-gray-200" :class="{'text-red-500': provider.consecutiveFailures > 0}">{{ provider.consecutiveFailures }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Knowledge Base & System Info Section -->
    <div class="mt-6 lg:flex block lg:gap-6">
      <!-- Knowledge Base Metrics -->
      <div class="bg-white dark:bg-gray-800 p-6 lg:w-1/2 w-full rounded-md border border-gray-200 dark:border-gray-700">
        <h2 class="font-medium text-sm text-gray-800 dark:text-gray-200 mb-4">{{ $t('penny.metrics.kbMetrics') }}</h2>
        <div class="space-y-1">
          <div class="flex justify-between py-3 border-b border-gray-150 dark:border-gray-700 last:border-b-0">
            <span class="text-sm font-medium text-gray-500 dark:text-gray-400">{{ $t('penny.metrics.totalArticles') }}</span>
            <span class="text-sm font-semibold text-gray-900 dark:text-gray-200">{{ kbMetrics.totalArticles || 'N/A' }}</span>
          </div>
          <div class="flex justify-between py-3 border-b border-gray-150 dark:border-gray-700 last:border-b-0">
            <span class="text-sm font-medium text-gray-500 dark:text-gray-400">{{ $t('penny.metrics.ragEnabled') }}</span>
            <span class="text-sm font-semibold" :class="kbMetrics.ragEnabled ? 'text-green-500' : 'text-red-500'">
              {{ kbMetrics.ragEnabled ? $t('common.active') : $t('common.inactive') }}
            </span>
          </div>
          <div class="flex justify-between py-3 border-b border-gray-150 dark:border-gray-700 last:border-b-0">
            <span class="text-sm font-medium text-gray-500 dark:text-gray-400">{{ $t('penny.metrics.embeddingModel') }}</span>
            <span class="text-sm font-semibold text-gray-900 dark:text-gray-200">{{ kbMetrics.embeddingModel || 'N/A' }}</span>
          </div>
          <div class="flex justify-between py-3 border-b border-gray-150 dark:border-gray-700 last:border-b-0">
            <span class="text-sm font-medium text-gray-500 dark:text-gray-400">{{ $t('penny.metrics.embeddingDimensions') }}</span>
            <span class="text-sm font-semibold text-gray-900 dark:text-gray-200">{{ kbMetrics.embeddingDimensions || 'N/A' }}</span>
          </div>
        </div>
      </div>

      <!-- System Information -->
      <div class="bg-white dark:bg-gray-800 p-6 lg:w-1/2 w-full mt-6 lg:mt-0 rounded-md border border-gray-200 dark:border-gray-700">
        <h2 class="font-medium text-sm text-gray-800 dark:text-gray-200 mb-4">{{ $t('penny.metrics.systemInfo') }}</h2>
        <div class="space-y-1">
          <div class="flex justify-between py-3 border-b border-gray-150 dark:border-gray-700 last:border-b-0">
            <span class="text-sm font-medium text-gray-500 dark:text-gray-400">{{ $t('penny.metrics.version') }}</span>
            <span class="text-sm font-semibold text-gray-900 dark:text-gray-200">{{ systemMetrics.system?.version || 'N/A' }}</span>
          </div>
          <div class="flex justify-between py-3 border-b border-gray-150 dark:border-gray-700 last:border-b-0">
            <span class="text-sm font-medium text-gray-500 dark:text-gray-400">{{ $t('penny.metrics.environment') }}</span>
            <span class="text-sm font-semibold text-gray-900 dark:text-gray-200">{{ systemMetrics.system?.environment || 'N/A' }}</span>
          </div>
          <div class="flex justify-between py-3 border-b border-gray-150 dark:border-gray-700 last:border-b-0">
            <span class="text-sm font-medium text-gray-500 dark:text-gray-400">{{ $t('penny.metrics.timestamp') }}</span>
            <span class="text-sm font-semibold text-gray-900 dark:text-gray-200">{{ formatDateTime(systemMetrics.system?.timestamp) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Icon } from '@iconify/vue';
import { pennyApi } from '@/api/pennyApi';

export default {
  name: 'PennyMetrics',
  components: {
    Icon
  },
  data() {
    return {
      systemMetrics: {},
      providerMetrics: {},
      kbMetrics: {},
      loading: false
    };
  },
  mounted() {
    this.refreshMetrics();
  },
  methods: {
    async refreshMetrics() {
      this.loading = true;
      try {
        await Promise.all([
          this.loadSystemMetrics(),
          this.loadProviderMetrics(),
          this.loadKbMetrics()
        ]);
      } catch (error) {
        console.error('Error loading metrics:', error);
        this.$toast.error('Failed to load metrics');
      } finally {
        this.loading = false;
      }
    },

    async loadSystemMetrics() {
      try {
        const response = await pennyApi.getSystemMetrics();
        this.systemMetrics = response.data;
      } catch (error) {
        console.error('Error loading system metrics:', error);
      }
    },

    async loadProviderMetrics() {
      try {
        const response = await pennyApi.getProviderMetrics();
        this.providerMetrics = response.data;
      } catch (error) {
        console.error('Error loading provider metrics:', error);
      }
    },

    async loadKbMetrics() {
      try {
        const response = await pennyApi.getKnowledgeBaseMetrics();
        this.kbMetrics = response.data;
      } catch (error) {
        console.error('Error loading KB metrics:', error);
      }
    },

    formatDate(date) {
      if (!date) return '-';
      return new Date(date).toLocaleString();
    },

    formatDateTime(date) {
      if (!date) return '-';
      return new Date(date).toLocaleString();
    },

    getProviderIcon(name) {
      const providers = {
        openai: 'simple-icons:openai',
        anthropic: 'simple-icons:anthropic',
        gemini: 'simple-icons:google-gemini',
        cohere: 'simple-icons:cohere'
      };
      return providers[name.toLowerCase()] || 'mdi:server';
    }
  }
};
</script>

<style scoped>
.penny-metrics {
  width: 100%;
  padding: 20px;
}

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
