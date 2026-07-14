<template>
  <div class="penny-metrics">
    <div class="page-header">
      <h1>Penny Bot Metrics</h1>
      <button @click="refreshMetrics" class="btn btn-primary">
        <i class="fas fa-sync"></i> Refresh
      </button>
    </div>

    <!-- System Overview -->
    <div class="section">
      <h2>System Overview</h2>
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-label">Total Bots</div>
          <div class="stat-value">{{ systemMetrics.bots?.total || 0 }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Active Bots</div>
          <div class="stat-value">{{ systemMetrics.bots?.active || 0 }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">KB Articles</div>
          <div class="stat-value">{{ systemMetrics.knowledgeBase?.totalArticles || 'N/A' }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">RAG Enabled</div>
          <div class="stat-value" :class="{ 'text-success': systemMetrics.knowledgeBase?.ragEnabled }">
            {{ systemMetrics.knowledgeBase?.ragEnabled ? 'Yes' : 'No' }}
          </div>
        </div>
      </div>
    </div>

    <!-- Provider Health -->
    <div class="section">
      <h2>Provider Health</h2>
      <div v-if="loading" class="text-center">Loading...</div>
      <div v-else class="providers-grid">
        <div
          v-for="(provider, name) in providerMetrics"
          :key="name"
          class="provider-card"
          :class="{ 'healthy': provider.healthy, 'unhealthy': !provider.healthy }"
        >
          <div class="provider-name">{{ name.toUpperCase() }}</div>
          <div class="provider-status">
            <span :class="['status-dot', provider.healthy ? 'green' : 'red']"></span>
            {{ provider.healthy ? 'Healthy' : 'Unhealthy' }}
          </div>
          <div class="provider-details">
            <div v-if="provider.lastMessage" class="detail-item">
              <span class="label">Last Message:</span>
              <span class="value">{{ provider.lastMessage }}</span>
            </div>
            <div v-if="provider.lastCheck" class="detail-item">
              <span class="label">Last Check:</span>
              <span class="value">{{ formatDate(provider.lastCheck) }}</span>
            </div>
            <div v-if="provider.consecutiveFailures !== undefined" class="detail-item">
              <span class="label">Failures:</span>
              <span class="value">{{ provider.consecutiveFailures }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Knowledge Base Metrics -->
    <div class="section">
      <h2>Knowledge Base</h2>
      <div class="kb-metrics">
        <div class="metric-row">
          <span class="metric-label">Total Articles:</span>
          <span class="metric-value">{{ kbMetrics.totalArticles || 'N/A' }}</span>
        </div>
        <div class="metric-row">
          <span class="metric-label">RAG Enabled:</span>
          <span class="metric-value" :class="{ 'text-success': kbMetrics.ragEnabled }">
            {{ kbMetrics.ragEnabled ? 'Yes' : 'No' }}
          </span>
        </div>
        <div class="metric-row">
          <span class="metric-label">Embedding Model:</span>
          <span class="metric-value">{{ kbMetrics.embeddingModel || 'N/A' }}</span>
        </div>
        <div class="metric-row">
          <span class="metric-label">Embedding Dimensions:</span>
          <span class="metric-value">{{ kbMetrics.embeddingDimensions || 'N/A' }}</span>
        </div>
      </div>
    </div>

    <!-- System Info -->
    <div class="section">
      <h2>System Information</h2>
      <div class="system-info">
        <div class="info-row">
          <span class="info-label">Version:</span>
          <span class="info-value">{{ systemMetrics.system?.version || 'N/A' }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">Environment:</span>
          <span class="info-value">{{ systemMetrics.system?.environment || 'N/A' }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">Timestamp:</span>
          <span class="info-value">{{ formatDateTime(systemMetrics.system?.timestamp) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { pennyApi } from '@/api/pennyApi';

export default {
  name: 'PennyMetrics',
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
        this.systemMetrics =	response.data;
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
    }
  }
};
</script>

<style scoped>
.penny-metrics {
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

.section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.section h2 {
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 20px;
  color: #333;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
}

.stat-card {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.text-success {
  color: #28a745;
}

.providers-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 15px;
}

.provider-card {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  border-left: 4px solid #ccc;
}

.provider-card.healthy {
  border-left-color: #28a745;
}

.provider-card.unhealthy {
  border-left-color: #dc3545;
}

.provider-name {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 10px;
}

.provider-status {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 15px;
  font-weight: 500;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.status-dot.green {
  background: #28a745;
}

.status-dot.red {
  background: #dc3545;
}

.provider-details {
  font-size: 13px;
  color: #666;
}

.detail-item {
  margin-bottom: 8px;
}

.detail-item .label {
  font-weight: 500;
  margin-right: 5px;
}

.kb-metrics {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.metric-row {
  display: flex;
  justify-content: space-between;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 4px;
}

.metric-label {
  font-weight: 500;
  color: #666;
}

.metric-value {
  font-weight: bold;
  color: #333;
}

.system-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}

.info-label {
  font-weight: 500;
  color: #666;
}

.info-value {
  color: #333;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-primary {
  background: #007bff;
  color: white;
}

.btn-primary:hover {
  background: #0056b3;
}

.text-center {
  text-align: center;
  padding: 20px;
  color: #666;
}
</style>
