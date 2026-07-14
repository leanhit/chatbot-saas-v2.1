<template>
  <div class="penny-bot-config">
    <div class="page-header">
      <h1>Penny Bot Configuration</h1>
      <button @click="saveConfig" class="btn btn-primary">
        <i class="fas fa-save"></i> Save Changes
      </button>
    </div>

    <div class="config-container">
      <!-- Basic Info -->
      <div class="config-section">
        <h2>Basic Information</h2>
        <div class="form-group">
          <label>Bot Name</label>
          <input v-model="config.botName" type="text" class="form-control" disabled />
        </div>
        <div class="form-group">
          <label>Business Name</label>
          <input v-model="config.businessName" type="text" class="form-control" placeholder="Enter business name" />
        </div>
        <div class="form-group">
          <label>Business Description</label>
          <textarea v-model="config.businessDescription" class="form-control" rows="3" placeholder="Describe your business"></textarea>
        </div>
      </div>

      <!-- AI Configuration -->
      <div class="config-section">
        <h2>AI Configuration</h2>
        <div class="form-group">
          <label>System Prompt</label>
          <textarea v-model="config.systemPrompt" class="form-control" rows="6" placeholder="Define the bot's personality and behavior"></textarea>
          <small class="help-text">This prompt defines how the bot should behave and respond to users.</small>
        </div>
        <div class="form-group">
          <label>Confidence Threshold</label>
          <input v-model.number="config.confidenceThreshold" type="number" class="form-control" step="0.1" min="0" max="1" />
          <small class="help-text">If AI confidence falls below this threshold, the conversation will be escalated to a human agent.</small>
        </div>
      </div>

      <!-- RAG Configuration -->
      <div class="config-section">
        <h2>RAG (Retrieval-Augmented Generation)</h2>
        <div class="toggle-group">
          <label class="toggle-label">
            <input v-model="config.ragEnabled" type="checkbox" />
            <span class="toggle-slider"></span>
            Enable RAG
          </label>
          <small class="help-text">When enabled, the bot will search the knowledge base for relevant information to answer questions.</small>
        </div>
        
        <div v-if="config.ragEnabled" class="rag-settings">
          <div class="form-group">
            <label>Top-K Results</label>
            <input v-model.number="config.ragTopK" type="number" class="form-control" min="1" max="10" />
            <small class="help-text">Number of knowledge articles to retrieve for each query.</small>
          </div>
          <div class="form-group">
            <label>Similarity Threshold</label>
            <input v-model.number="config.ragSimilarityThreshold" type="number" class="form-control" step="0.1" min="0" max="1" />
            <small class="help-text">Minimum similarity score for knowledge articles to be included.</small>
          </div>
          <div class="form-group">
            <label>Max Context Tokens</label>
            <input v-model.number="config.ragMaxContextTokens" type="number" class="form-control" min="100" max="4000" />
            <small class="help-text">Maximum number of tokens to use for knowledge context in the prompt.</small>
          </div>
        </div>
      </div>

      <!-- Provider Configuration -->
      <div class="config-section">
        <h2>AI Provider Configuration</h2>
        <div class="provider-group">
          <div class="provider-card">
            <h3>OpenAI (GPT)</h3>
            <div class="toggle-group">
              <label class="toggle-label">
                <input v-model="config.gptEnabled" type="checkbox" />
                <span class="toggle-slider"></span>
                Enable GPT
              </label>
            </div>
            <div v-if="config.gptEnabled" class="provider-settings">
              <div class="form-group">
                <label>Model</label>
                <select v-model="config.gptModel" class="form-control">
                  <option value="gpt-4o-mini">GPT-4o Mini</option>
                  <option value="gpt-4o">GPT-4o</option>
                  <option value="gpt-3.5-turbo">GPT-3.5 Turbo</option>
                </select>
              </div>
              <div class="form-group">
                <label>Max Tokens</label>
                <input v-model.number="config.gptMaxTokens" type="number" class="form-control" min="100" max="4000" />
              </div>
              <div class="form-group">
                <label>Temperature</label>
                <input v-model.number="config.gptTemperature" type="number" class="form-control" step="0.1" min="0" max="2" />
              </div>
            </div>
          </div>

          <div class="provider-card">
            <h3>Anthropic (Claude)</h3>
            <div class="toggle-group">
              <label class="toggle-label">
                <input v-model="config.claudeEnabled" type="checkbox" />
                <span class="toggle-slider"></span>
                Enable Claude
              </label>
            </div>
            <div v-if="config.claudeEnabled" class="provider-settings">
              <div class="form-group">
                <label>Model</label>
                <select v-model="config.claudeModel" class="form-control">
                  <option value="claude-3-haiku-20240307">Claude 3 Haiku</option>
                  <option value="claude-3-sonnet-20240229">Claude 3 Sonnet</option>
                  <option value="claude-3-opus-20240229">Claude 3 Opus</option>
                </select>
              </div>
              <div class="form-group">
                <label>Max Tokens</label>
                <input v-model.number="config.claudeMaxTokens" type="number" class="form-control" min="100" max="4000" />
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Rate Limiting -->
      <div class="config-section">
        <h2>Rate Limiting</h2>
        <div class="toggle-group">
          <label class="toggle-label">
            <input v-model="config.rateLimitEnabled" type="checkbox" />
            <span class="toggle-slider"></span>
            Enable Rate Limiting
          </label>
          <small class="help-text">Protect the bot from abuse by limiting message rates.</small>
        </div>
        
        <div v-if="config.rateLimitEnabled" class="rate-limit-settings">
          <div class="form-group">
            <label>User Messages per Minute</label>
            <input v-model.number="config.userMessagesPerMinute" type="number" class="form-control" min="1" max="1000" />
          </div>
          <div class="form-group">
            <label>Tenant Messages per Minute</label>
            <input v-model.number="config.tenantMessagesPerMinute" type="number" class="form-control" min="1" max="10000" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { pennyApi } from '@/api/pennyApi';

export default {
  name: 'PennyBotConfig',
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
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.page-header h1 {
  margin: 0;
}

.config-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.config-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.config-section h2 {
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 18px;
  color: #333;
}

.config-section h3 {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 16px;
  color: #555;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

.form-control {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-control:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
}

.help-text {
  display: block;
  margin-top: 5px;
  font-size: 12px;
  color: #666;
}

.toggle-group {
  margin-bottom: 20px;
}

.toggle-label {
  display: flex;
  align-items: center;
  cursor: pointer;
  font-weight: 500;
}

.toggle-label input[type="checkbox"] {
  display: none;
}

.toggle-slider {
  position: relative;
  width: 50px;
  height: 26px;
  background: #ccc;
  border-radius: 13px;
  margin-right: 10px;
  transition: background 0.3s;
}

.toggle-slider::before {
  content: '';
  position: absolute;
  width: 20px;
  height: 20px;
  background: white;
  border-radius: 50%;
  top: 3px;
  left: 3px;
  transition: transform 0.3s;
}

.toggle-label input:checked + .toggle-slider {
  background: #007bff;
}

.toggle-label input:checked + .toggle-slider::before {
  transform: translateX(24px);
}

.rag-settings,
 .provider-settings,
 .rate-limit-settings {
  margin-top: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 6px;
}

.provider-group {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.provider-card {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 6px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
}

.btn-primary {
  background: #007bff;
  color: white;
}

.btn-primary:hover {
  background: #0056b3;
}
</style>
