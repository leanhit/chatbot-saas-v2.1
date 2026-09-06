<template>
  <div class="ai-bot-config">
    <div class="config-header">
      <h2 class="config-title">{{ $t('penny.aiConfig.title') }}</h2>
      <p class="config-subtitle">{{ $t('penny.aiConfig.subtitle') }}</p>
    </div>

    <div class="config-form">
      <!-- LLM Provider Selection -->
      <div class="form-section">
        <label class="form-label">{{ $t('penny.aiConfig.provider') }}</label>
        <select v-model="config.providerType" class="form-select">
          <option value="OPENAI">OpenAI</option>
          <option value="CLAUDE">Claude (Anthropic)</option>
          <option value="GEMINI">Google Gemini</option>
          <option value="OLLAMA">Ollama (Local)</option>
        </select>
      </div>

      <!-- Model Selection -->
      <div class="form-section">
        <label class="form-label">{{ $t('penny.aiConfig.model') }}</label>
        <select v-model="config.modelName" class="form-select">
          <option v-for="model in availableModels" :key="model" :value="model">
            {{ model }}
          </option>
        </select>
      </div>

      <!-- Temperature Slider -->
      <div class="form-section">
        <label class="form-label">
          {{ $t('penny.aiConfig.temperature') }}
          <span class="value-display">{{ config.temperature }}</span>
        </label>
        <input
          v-model="config.temperature"
          type="range"
          min="0"
          max="1"
          step="0.1"
          class="form-range"
        />
        <div class="range-labels">
          <span>{{ $t('penny.aiConfig.focused') }}</span>
          <span>{{ $t('penny.aiConfig.creative') }}</span>
        </div>
      </div>

      <!-- Persona Style -->
      <div class="form-section">
        <label class="form-label">{{ $t('penny.aiConfig.personaStyle') }}</label>
        <div class="persona-options">
          <label v-for="style in personaStyles" :key="style.value" class="persona-option">
            <input
              v-model="config.personaStyle"
              type="radio"
              :value="style.value"
              class="persona-radio"
            />
            <div class="persona-card" :class="{ selected: config.personaStyle === style.value }">
              <Icon :icon="style.icon" class="persona-icon" />
              <span class="persona-name">{{ style.label }}</span>
              <span class="persona-desc">{{ style.description }}</span>
            </div>
          </label>
        </div>
      </div>

      <!-- Custom Instructions -->
      <div class="form-section">
        <label class="form-label">{{ $t('penny.aiConfig.customInstructions') }}</label>
        <textarea
          v-model="config.customInstructions"
          class="form-textarea"
          :placeholder="$t('penny.aiConfig.customInstructionsPlaceholder')"
          rows="4"
        />
        <p class="form-hint">{{ $t('penny.aiConfig.customInstructionsHint') }}</p>
      </div>

      <!-- Greeting Message -->
      <div class="form-section">
        <label class="form-label">{{ $t('penny.aiConfig.greetingMessage') }}</label>
        <input
          v-model="config.greetingMessage"
          type="text"
          class="form-input"
          :placeholder="$t('penny.aiConfig.greetingMessagePlaceholder')"
        />
      </div>

      <!-- Fallback Message -->
      <div class="form-section">
        <label class="form-label">{{ $t('penny.aiConfig.fallbackMessage') }}</label>
        <textarea
          v-model="config.fallbackMessage"
          class="form-textarea"
          :placeholder="$t('penny.aiConfig.fallbackMessagePlaceholder')"
          rows="2"
        />
      </div>

      <!-- Save Button -->
      <div class="form-actions">
        <button
          @click="saveConfig"
          class="save-button"
          :disabled="saving"
        >
          <Icon v-if="saving" icon="mdi:loading" class="animate-spin mr-2" />
          <Icon v-else icon="mdi:content-save" class="mr-2" />
          {{ saving ? $t('penny.aiConfig.saving') : $t('penny.aiConfig.save') }}
        </button>
        <button @click="resetConfig" class="reset-button" :disabled="saving">
          {{ $t('penny.aiConfig.reset') }}
        </button>
      </div>
    </div>

    <!-- Status Message -->
    <div v-if="statusMessage" class="status-message" :class="statusMessage.type">
      <Icon :icon="statusMessage.icon" class="status-icon" />
      <p>{{ statusMessage.message }}</p>
    </div>
  </div>
</template>

<script>
import { Icon } from '@iconify/vue';
import { pennyApi } from '@/api/pennyApi';

export default {
  name: 'AiBotConfig',
  components: { Icon },
  props: {
    botId: {
      type: String,
      required: true
    },
    initialConfig: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      config: {
        providerType: 'OPENAI',
        modelName: 'gpt-4o-mini',
        temperature: 0.7,
        personaStyle: 'PROFESSIONAL',
        customInstructions: '',
        greetingMessage: '',
        fallbackMessage: ''
      },
      saving: false,
      statusMessage: null
    };
  },
  computed: {
    availableModels() {
      const models = {
        OPENAI: ['gpt-4o', 'gpt-4o-mini', 'gpt-3.5-turbo'],
        CLAUDE: ['claude-3-5-sonnet-20241022', 'claude-3-5-haiku-20241022', 'claude-3-opus-20240229'],
        GEMINI: ['gemini-1.5-pro', 'gemini-1.5-flash', 'gemini-1.0-pro'],
        OLLAMA: ['llama3:8b', 'llama3:70b', 'mistral:7b', 'codellama:7b']
      };
      return models[this.config.providerType] || [];
    },
    personaStyles() {
      return [
        {
          value: 'PROFESSIONAL',
          label: this.$t('penny.aiConfig.personas.professional'),
          description: this.$t('penny.aiConfig.personas.professionalDesc'),
          icon: 'mdi:briefcase'
        },
        {
          value: 'FRIENDLY',
          label: this.$t('penny.aiConfig.personas.friendly'),
          description: this.$t('penny.aiConfig.personas.friendlyDesc'),
          icon: 'mdi:emoticon-happy'
        },
        {
          value: 'ENTHUSIASTIC',
          label: this.$t('penny.aiConfig.personas.enthusiastic'),
          description: this.$t('penny.aiConfig.personas.enthusiasticDesc'),
          icon: 'mdi:fire'
        },
        {
          value: 'HUMOROUS',
          label: this.$t('penny.aiConfig.personas.humorous'),
          description: this.$t('penny.aiConfig.personas.humorousDesc'),
          icon: 'mdi:emoticon-lol'
        },
        {
          value: 'FORMAL',
          label: this.$t('penny.aiConfig.personas.formal'),
          description: this.$t('penny.aiConfig.personas.formalDesc'),
          icon: 'mdi:account-tie'
        }
      ];
    }
  },
  watch: {
    initialConfig: {
      immediate: true,
      handler(newConfig) {
        if (newConfig && Object.keys(newConfig).length > 0) {
          this.config = { ...this.config, ...newConfig };
        }
      }
    },
    'config.providerType'() {
      // Reset model when provider changes
      this.config.modelName = this.availableModels[0] || '';
    }
  },
  methods: {
    async saveConfig() {
      this.saving = true;
      this.statusMessage = null;

      try {
        await pennyApi.updatePennyBot(this.botId, this.config);
        this.showStatus('success', 'mdi:check-circle', this.$t('penny.aiConfig.saveSuccess'));
        this.$emit('saved', this.config);
      } catch (error) {
        this.showStatus('error', 'mdi:alert-circle', this.$t('penny.aiConfig.saveError'));
        console.error('Save error:', error);
      } finally {
        this.saving = false;
      }
    },
    resetConfig() {
      this.config = {
        providerType: 'OPENAI',
        modelName: 'gpt-4o-mini',
        temperature: 0.7,
        personaStyle: 'PROFESSIONAL',
        customInstructions: '',
        greetingMessage: '',
        fallbackMessage: ''
      };
      if (this.initialConfig && Object.keys(this.initialConfig).length > 0) {
        this.config = { ...this.config, ...this.initialConfig };
      }
    },
    showStatus(type, icon, message) {
      this.statusMessage = { type, icon, message };
      setTimeout(() => {
        this.statusMessage = null;
      }, 5000);
    }
  }
};
</script>

<style scoped>
.ai-bot-config {
  @apply space-y-6;
}

.config-header {
  @apply space-y-1;
}

.config-title {
  @apply text-xl font-bold text-gray-900 dark:text-white;
}

.config-subtitle {
  @apply text-sm text-gray-500 dark:text-gray-400;
}

.config-form {
  @apply space-y-4;
}

.form-section {
  @apply space-y-2;
}

.form-label {
  @apply block text-sm font-medium text-gray-700 dark:text-gray-300;
}

.value-display {
  @apply ml-2 text-primary font-semibold;
}

.form-select,
.form-input {
  @apply w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-900 text-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary;
}

.form-range {
  @apply w-full h-2 bg-gray-200 dark:bg-gray-700 rounded-lg appearance-none cursor-pointer;
}

.range-labels {
  @apply flex justify-between text-xs text-gray-500 dark:text-gray-400;
}

.persona-options {
  @apply grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3;
}

.persona-option {
  @apply cursor-pointer;
}

.persona-radio {
  @apply hidden;
}

.persona-card {
  @apply p-4 border-2 border-gray-200 dark:border-gray-700 rounded-lg transition-all hover:border-gray-300 dark:hover:border-gray-600;
}

.persona-card.selected {
  @apply border-primary bg-primary/5;
}

.persona-icon {
  @apply text-2xl text-gray-500 dark:text-gray-400 mb-2;
}

.persona-card.selected .persona-icon {
  @apply text-primary;
}

.persona-name {
  @apply block font-medium text-gray-900 dark:text-white;
}

.persona-desc {
  @apply text-xs text-gray-500 dark:text-gray-400 mt-1;
}

.form-textarea {
  @apply w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-900 text-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary resize-none;
}

.form-hint {
  @apply text-xs text-gray-500 dark:text-gray-400;
}

.form-actions {
  @apply flex space-x-3 pt-4;
}

.save-button {
  @apply flex-1 inline-flex items-center justify-center px-4 py-2 bg-primary text-white rounded-md hover:bg-primary/80 transition-colors text-sm font-medium;
}

.save-button:disabled {
  @apply opacity-50 cursor-not-allowed;
}

.reset-button {
  @apply px-4 py-2 border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 rounded-md hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors text-sm font-medium;
}

.status-message {
  @apply flex items-center space-x-2 p-3 rounded-md text-sm;
}

.status-message.success {
  @apply bg-green-50 dark:bg-green-900/30 text-green-700 dark:text-green-400;
}

.status-message.error {
  @apply bg-red-50 dark:bg-red-900/30 text-red-700 dark:text-red-400;
}

.status-icon {
  @apply text-xl;
}


</style>
