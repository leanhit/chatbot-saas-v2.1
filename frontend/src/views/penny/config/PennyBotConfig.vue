<template>
  <div class="penny-bot-config">
    <!-- Header -->
    <div class="flex justify-between items-center mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white">
          {{ $t('penny.config.title') }}
        </h1>
      </div>
      <div class="flex items-center space-x-4">
        <button
          @click="saveConfig"
          :disabled="loading"
          class="inline-flex items-center px-4 py-2 bg-primary text-white rounded-md hover:bg-primary/80 transition-colors disabled:opacity-50 text-sm font-medium"
        >
          <Icon v-if="loading" icon="mdi:loading" class="animate-spin mr-2" />
          <Icon v-else icon="mdi:content-save" class="mr-2" />
          {{ $t('penny.config.saveChanges') }}
        </button>
      </div>
    </div>

    <div class="space-y-6">
      <!-- Basic Info -->
      <div class="bg-white dark:bg-gray-800 p-6 rounded-md border border-gray-200 dark:border-gray-700">
        <h2 class="font-semibold text-lg text-gray-900 dark:text-white mb-4">{{ $t('penny.config.basicInfo') }}</h2>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5 block">{{ $t('penny.config.botName') }}</label>
            <input v-model="config.botName" type="text" class="w-full px-3 py-2 border border-gray-300 dark:border-gray-750 rounded-md dark:bg-gray-900 dark:text-white disabled:opacity-50 disabled:bg-gray-100 dark:disabled:bg-gray-850" disabled />
          </div>
          <div>
            <label class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5 block">{{ $t('penny.config.businessName') }}</label>
            <input v-model="config.businessName" type="text" class="w-full px-3 py-2 border border-gray-300 dark:border-gray-750 rounded-md dark:bg-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary" :placeholder="$t('penny.config.businessNamePlaceholder')" />
          </div>
          <div class="md:col-span-2">
            <label class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5 block">{{ $t('penny.config.businessDescription') }}</label>
            <textarea v-model="config.businessDescription" class="w-full px-3 py-2 border border-gray-300 dark:border-gray-750 rounded-md dark:bg-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary" rows="3" :placeholder="$t('penny.config.businessDescriptionPlaceholder')"></textarea>
          </div>
        </div>
      </div>

      <!-- AI Configuration -->
      <div class="bg-white dark:bg-gray-800 p-6 rounded-md border border-gray-200 dark:border-gray-700">
        <h2 class="font-semibold text-lg text-gray-900 dark:text-white mb-4">{{ $t('penny.config.aiConfig') }}</h2>
        <div class="space-y-4">
          <div>
            <label class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5 block">{{ $t('penny.config.systemPrompt') }}</label>
            <textarea v-model="config.systemPrompt" class="w-full px-3 py-2 border border-gray-300 dark:border-gray-750 rounded-md dark:bg-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary" rows="6" :placeholder="$t('penny.config.systemPromptPlaceholder')"></textarea>
            <small class="text-xs text-gray-500 dark:text-gray-400 mt-1 block">{{ $t('penny.config.systemPromptHelp') }}</small>
          </div>
          <div>
            <label class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5 block">{{ $t('penny.config.confidenceThreshold') }}</label>
            <input v-model.number="config.confidenceThreshold" type="number" class="w-full md:w-1/3 px-3 py-2 border border-gray-300 dark:border-gray-750 rounded-md dark:bg-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary" step="0.1" min="0" max="1" />
            <small class="text-xs text-gray-500 dark:text-gray-400 mt-1 block">{{ $t('penny.config.confidenceThresholdHelp') }}</small>
          </div>
        </div>
      </div>

      <!-- RAG Configuration -->
      <div class="bg-white dark:bg-gray-800 p-6 rounded-md border border-gray-200 dark:border-gray-700">
        <h2 class="font-semibold text-lg text-gray-900 dark:text-white mb-4">{{ $t('penny.config.ragTitle') }}</h2>
        <div class="space-y-4">
          <div class="flex items-start">
            <div class="flex items-center h-5">
              <input v-model="config.ragEnabled" type="checkbox" class="focus:ring-primary h-4 w-4 text-primary border-gray-300 dark:border-gray-750 rounded dark:bg-gray-900" />
            </div>
            <div class="ml-3 text-sm">
              <label class="font-medium text-gray-700 dark:text-gray-300">{{ $t('penny.config.enableRag') }}</label>
              <p class="text-xs text-gray-500 dark:text-gray-400">{{ $t('penny.config.ragHelp') }}</p>
            </div>
          </div>
          
          <div v-if="config.ragEnabled" class="grid grid-cols-1 md:grid-cols-3 gap-4 pt-4 border-t border-gray-100 dark:border-gray-700">
            <div>
              <label class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5 block">{{ $t('penny.config.topK') }}</label>
              <input v-model.number="config.ragTopK" type="number" class="w-full px-3 py-2 border border-gray-300 dark:border-gray-750 rounded-md dark:bg-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary" min="1" max="10" />
              <small class="text-xs text-gray-500 dark:text-gray-400 mt-1 block">{{ $t('penny.config.topKHelp') }}</small>
            </div>
            <div>
              <label class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5 block">{{ $t('penny.config.similarityThreshold') }}</label>
              <input v-model.number="config.ragSimilarityThreshold" type="number" class="w-full px-3 py-2 border border-gray-300 dark:border-gray-750 rounded-md dark:bg-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary" step="0.1" min="0" max="1" />
              <small class="text-xs text-gray-500 dark:text-gray-400 mt-1 block">{{ $t('penny.config.similarityThresholdHelp') }}</small>
            </div>
            <div>
              <label class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5 block">{{ $t('penny.config.maxContextTokens') }}</label>
              <input v-model.number="config.ragMaxContextTokens" type="number" class="w-full px-3 py-2 border border-gray-300 dark:border-gray-750 rounded-md dark:bg-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary" min="100" max="4000" />
              <small class="text-xs text-gray-500 dark:text-gray-400 mt-1 block">{{ $t('penny.config.maxContextTokensHelp') }}</small>
            </div>
          </div>
        </div>
      </div>

      <!-- Provider Configuration -->
      <div class="bg-white dark:bg-gray-800 p-6 rounded-md border border-gray-200 dark:border-gray-700">
        <h2 class="font-semibold text-lg text-gray-900 dark:text-white mb-4">{{ $t('penny.config.aiProviderConfig') }}</h2>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- OpenAI Card -->
          <div class="p-4 border border-gray-200 dark:border-gray-700 rounded-md bg-gray-50/50 dark:bg-gray-900/30">
            <h3 class="font-semibold text-md text-gray-900 dark:text-white mb-3">OpenAI (GPT)</h3>
            <div class="flex items-center mb-4">
              <input v-model="config.gptEnabled" type="checkbox" class="focus:ring-primary h-4 w-4 text-primary border-gray-300 dark:border-gray-750 rounded dark:bg-gray-900" />
              <label class="ml-2 text-sm font-medium text-gray-700 dark:text-gray-300">{{ $t('penny.config.enableGpt') }}</label>
            </div>
            <div v-if="config.gptEnabled" class="space-y-3 pt-3 border-t border-gray-200 dark:border-gray-700">
              <div>
                <label class="text-xs font-medium text-gray-500 dark:text-gray-400 mb-1 block">{{ $t('penny.config.model') }}</label>
                <select v-model="config.gptModel" class="w-full px-3 py-2 border border-gray-300 dark:border-gray-750 rounded-md dark:bg-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary text-sm">
                  <option value="gpt-4o-mini">GPT-4o Mini</option>
                  <option value="gpt-4o">GPT-4o</option>
                  <option value="gpt-3.5-turbo">GPT-3.5 Turbo</option>
                </select>
              </div>
              <div>
                <label class="text-xs font-medium text-gray-500 dark:text-gray-400 mb-1 block">{{ $t('penny.config.maxTokens') }}</label>
                <input v-model.number="config.gptMaxTokens" type="number" class="w-full px-3 py-2 border border-gray-300 dark:border-gray-750 rounded-md dark:bg-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary text-sm" min="100" max="4000" />
              </div>
              <div>
                <label class="text-xs font-medium text-gray-500 dark:text-gray-400 mb-1 block">{{ $t('penny.config.temperature') }}</label>
                <input v-model.number="config.gptTemperature" type="number" class="w-full px-3 py-2 border border-gray-300 dark:border-gray-750 rounded-md dark:bg-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary text-sm" step="0.1" min="0" max="2" />
              </div>
            </div>
          </div>

          <!-- Anthropic Card -->
          <div class="p-4 border border-gray-200 dark:border-gray-700 rounded-md bg-gray-50/50 dark:bg-gray-900/30">
            <h3 class="font-semibold text-md text-gray-900 dark:text-white mb-3">Anthropic (Claude)</h3>
            <div class="flex items-center mb-4">
              <input v-model="config.claudeEnabled" type="checkbox" class="focus:ring-primary h-4 w-4 text-primary border-gray-300 dark:border-gray-750 rounded dark:bg-gray-900" />
              <label class="ml-2 text-sm font-medium text-gray-700 dark:text-gray-300">{{ $t('penny.config.enableClaude') }}</label>
            </div>
            <div v-if="config.claudeEnabled" class="space-y-3 pt-3 border-t border-gray-200 dark:border-gray-700">
              <div>
                <label class="text-xs font-medium text-gray-500 dark:text-gray-400 mb-1 block">{{ $t('penny.config.model') }}</label>
                <select v-model="config.claudeModel" class="w-full px-3 py-2 border border-gray-300 dark:border-gray-750 rounded-md dark:bg-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary text-sm">
                  <option value="claude-3-haiku-20240307">Claude 3 Haiku</option>
                  <option value="claude-3-sonnet-20240229">Claude 3 Sonnet</option>
                  <option value="claude-3-opus-20240229">Claude 3 Opus</option>
                </select>
              </div>
              <div>
                <label class="text-xs font-medium text-gray-500 dark:text-gray-400 mb-1 block">{{ $t('penny.config.maxTokens') }}</label>
                <input v-model.number="config.claudeMaxTokens" type="number" class="w-full px-3 py-2 border border-gray-300 dark:border-gray-750 rounded-md dark:bg-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary text-sm" min="100" max="4000" />
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Rate Limiting -->
      <div class="bg-white dark:bg-gray-800 p-6 rounded-md border border-gray-200 dark:border-gray-700">
        <h2 class="font-semibold text-lg text-gray-900 dark:text-white mb-4">{{ $t('penny.config.rateLimiting') }}</h2>
        <div class="space-y-4">
          <div class="flex items-start">
            <div class="flex items-center h-5">
              <input v-model="config.rateLimitEnabled" type="checkbox" class="focus:ring-primary h-4 w-4 text-primary border-gray-300 dark:border-gray-750 rounded dark:bg-gray-900" />
            </div>
            <div class="ml-3 text-sm">
              <label class="font-medium text-gray-700 dark:text-gray-300">{{ $t('penny.config.enableRateLimiting') }}</label>
              <p class="text-xs text-gray-500 dark:text-gray-400">{{ $t('penny.config.rateLimitingHelp') }}</p>
            </div>
          </div>
          
          <div v-if="config.rateLimitEnabled" class="grid grid-cols-1 md:grid-cols-2 gap-4 pt-4 border-t border-gray-100 dark:border-gray-700">
            <div>
              <label class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5 block">{{ $t('penny.config.userMessagesPerMinute') }}</label>
              <input v-model.number="config.userMessagesPerMinute" type="number" class="w-full px-3 py-2 border border-gray-300 dark:border-gray-750 rounded-md dark:bg-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary animate-none" min="1" max="1000" />
            </div>
            <div>
              <label class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5 block">{{ $t('penny.config.tenantMessagesPerMinute') }}</label>
              <input v-model.number="config.tenantMessagesPerMinute" type="number" class="w-full px-3 py-2 border border-gray-300 dark:border-gray-750 rounded-md dark:bg-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary animate-none" min="1" max="10000" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { pennyApi } from '@/api/pennyApi';
import { Icon } from '@iconify/vue';

export default {
  name: 'PennyBotConfig',
  components: {
    Icon
  },
  props: {
    botId: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      config: {
        botName: '',
        businessName: '',
        businessDescription: '',
        systemPrompt: '',
        confidenceThreshold: 0.6,
        ragEnabled: false,
        ragTopK: 3,
        ragSimilarityThreshold: 0.7,
        ragMaxContextTokens: 1500,
        gptEnabled: false,
        gptModel: 'gpt-4o-mini',
        gptMaxTokens: 800,
        gptTemperature: 0.7,
        claudeEnabled: false,
        claudeModel: 'claude-3-haiku-20240307',
        claudeMaxTokens: 800,
        rateLimitEnabled: true,
        userMessagesPerMinute: 60,
        tenantMessagesPerMinute: 1000
      },
      loading: false
    };
  },
  mounted() {
    this.loadConfig();
  },
  methods: {
    async loadConfig() {
      this.loading = true;
      try {
        const response = await pennyApi.getPennyBotById(this.botId);
        const bot = response.data;
        
        this.config = {
          botName: bot.botName || '',
          businessName: bot.businessName || '',
          businessDescription: bot.businessDescription || '',
          systemPrompt: bot.systemPrompt || '',
          confidenceThreshold: bot.confidenceThreshold || 0.6,
          ragEnabled: true, // Default to enabled
          ragTopK: 3,
          ragSimilarityThreshold: 0.7,
          ragMaxContextTokens: 1500,
          gptEnabled: true,
          gptModel: 'gpt-4o-mini',
          gptMaxTokens: 800,
          gptTemperature: 0.7,
          claudeEnabled: false,
          claudeModel: 'claude-3-haiku-20240307',
          claudeMaxTokens: 800,
          rateLimitEnabled: true,
          userMessagesPerMinute: 60,
          tenantMessagesPerMinute: 1000
        };
      } catch (error) {
        console.error('Error loading config:', error);
        this.$toast.error('Failed to load bot configuration');
      } finally {
        this.loading = false;
      }
    },
    
    async saveConfig() {
      try {
        const updateData = {
          businessName: this.config.businessName,
          businessDescription: this.config.businessDescription,
          systemPrompt: this.config.systemPrompt,
          confidenceThreshold: this.config.confidenceThreshold
        };
        
        await pennyApi.updatePennyBot(this.botId, updateData);
        this.$toast.success('Configuration saved successfully');
      } catch (error) {
        console.error('Error saving config:', error);
        this.$toast.error('Failed to save configuration');
      }
    }
  }
};
</script>

<style scoped>
.penny-bot-config {
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
