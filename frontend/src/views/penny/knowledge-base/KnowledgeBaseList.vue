<template>
  <div class="knowledge-base-list">
    <div class="page-header">
      <h1>Knowledge Base</h1>
      <button @click="showCreateModal = true" class="btn btn-primary">
        <i class="fas fa-plus"></i> Add Article
      </button>
    </div>

    <!-- Stats Cards -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-label">Total Articles</div>
        <div class="stat-value">{{ stats.totalArticles || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">RAG Enabled</div>
        <div class="stat-value" :class="{ 'text-success': stats.ragEnabled }">
          {{ stats.ragEnabled ? 'Yes' : 'No' }}
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-label">Embedding Model</div>
        <div class="stat-value">{{ stats.embeddingModel || 'N/A' }}</div>
      </div>
    </div>

    <!-- Search and Filter -->
    <div class="search-bar">
      <input
        v-model="searchQuery"
        type="text"
        placeholder="Search articles..."
        class="search-input"
        @keyup.enter="handleSearch"
      />
      <button @click="handleSearch" class="btn btn-secondary">Search</button>
      <button @click="showTestSearch = true" class="btn btn-info">
        <i class="fas fa-search"></i> Test Search
      </button>
    </div>

    <!-- Articles Table -->
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>Title</th>
            <th>Category</th>
            <th>Priority</th>
            <th>Status</th>
            <th>Updated</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="6" class="text-center">Loading...</td>
          </tr>
          <tr v-else-if="articles.length === 0">
            <td colspan="6" class="text-center">No articles found</td>
          </tr>
          <tr v-for="article in articles" :key="article.id">
            <td>{{ article.title }}</td>
            <td>{{ article.category || '-' }}</td>
            <td>{{ article.priority || 0 }}</td>
            <td>
              <span :class="['status-badge', article.isActive ? 'active' : 'inactive']">
                {{ article.isActive ? 'Active' : 'Inactive' }}
              </span>
            </td>
            <td>{{ formatDate(article.updatedAt) }}</td>
            <td class="actions">
              <button @click="editArticle(article)" class="btn btn-sm btn-primary">
                <i class="fas fa-edit"></i>
              </button>
              <button @click="reembedArticle(article)" class="btn btn-sm btn-info" title="Re-embed">
                <i class="fas fa-sync"></i>
              </button>
              <button @click="deleteArticle(article)" class="btn btn-sm btn-danger">
                <i class="fas fa-trash"></i>
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- Pagination -->
      <div class="pagination" v-if="totalPages > 1">
        <button
          @click="changePage(currentPage - 1)"
          :disabled="currentPage === 0"
          class="btn btn-sm"
        >
          Previous
        </button>
        <span>Page {{ currentPage + 1 }} of {{ totalPages }}</span>
        <button
          @click="changePage(currentPage + 1)"
          :disabled="currentPage === totalPages - 1"
          class="btn btn-sm"
        >
          Next
        </button>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <div v-if="showCreateModal || showEditModal" class="modal-overlay">
      <div class="modal">
        <div class="modal-header">
          <h2>{{ showEditModal ? 'Edit Article' : 'Create Article' }}</h2>
          <button @click="closeModal" class="btn-close">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveArticle">
            <div class="form-group">
              <label>Title *</label>
              <input v-model="articleForm.title" type="text" required class="form-control" />
            </div>
            <div class="form-group">
              <label>Category</label>
              <select v-model="articleForm.category" class="form-control">
                <option value="">Select category</option>
                <option value="faq">FAQ</option>
                <option value="product">Product</option>
                <option value="policy">Policy</option>
                <option value="shipping">Shipping</option>
                <option value="price">Price</option>
              </select>
            </div>
            <div class="form-group">
              <label>Content *</label>
              <textarea v-model="articleForm.content" required class="form-control" rows="6"></textarea>
            </div>
            <div class="form-group">
              <label>Tags (comma-separated)</label>
              <input v-model="articleForm.tags" type="text" class="form-control" />
            </div>
            <div class="form-group">
              <label>Source URL</label>
              <input v-model="articleForm.sourceUrl" type="url" class="form-control" />
            </div>
            <div class="form-group">
              <label>Priority</label>
              <input v-model="articleForm.priority" type="number" class="form-control" min="0" />
            </div>
            <div class="form-group">
              <label>
                <input v-model="articleForm.isActive" type="checkbox" />
                Active
              </label>
            </div>
            <div class="modal-footer">
              <button type="button" @click="closeModal" class="btn btn-secondary">Cancel</button>
              <button type="submit" class="btn btn-primary">Save</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Test Search Modal -->
    <div v-if="showTestSearch" class="modal-overlay">
      <div class="modal">
        <div class="modal-header">
          <h2>Test Knowledge Base Search</h2>
          <button @click="showTestSearch = false" class="btn-close">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>Search Query</label>
            <input v-model="testQuery" type="text" class="form-control" />
          </div>
          <button @click="handleTestSearch" class="btn btn-primary">Search</button>
          
          <div v-if="testResults" class="test-results">
            <h3>Results ({{ testResults.count }})</h3>
            <div v-for="article in testResults.articles" :key="article.id" class="result-item">
              <h4>{{ article.title }}</h4>
              <p>{{ article.content.substring(0, 200) }}...</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { pennyApi } from '@/api/pennyApi';

export default {
  name: 'KnowledgeBaseList',
  props: {
    botId: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      articles: [],
      stats: {},
      loading: false,
      currentPage: 0,
      totalPages: 0,
      pageSize: 20,
      searchQuery: '',
      
      // Modals
      showCreateModal: false,
      showEditModal: false,
      showTestSearch: false,
      
      // Form
      articleForm: {
        title: '',
        category: '',
        content: '',
        tags: '',
        sourceUrl: '',
        priority: 0,
        isActive: true
      },
      editingArticle: null,
      
      // Test search
      testQuery: '',
      testResults: null
    };
  },
  mounted() {
    this.loadArticles();
    this.loadStats();
  },
  methods: {
    async loadArticles() {
      this.loading = true;
      try {
        const response = await pennyApi.getKnowledgeArticles(
          this.botId,
          this.currentPage,
          this.pageSize
        );
        this.articles = response.data.content || response.data;
        this.totalPages = response.data.totalPages || 1;
      } catch (error) {
        console.error('Error loading articles:', error);
        this.$toast.error('Failed to load articles');
      } finally {
        this.loading = false;
      }
    },
    
    async loadStats() {
      try {
        const response = await pennyApi.getKnowledgeBaseStats(this.botId);
        this.stats = response.data;
      } catch (error) {
        console.error('Error loading stats:', error);
      }
    },
    
    handleSearch() {
      this.currentPage = 0;
      this.loadArticles();
    },
    
    changePage(page) {
      this.currentPage = page;
      this.loadArticles();
    },
    
    editArticle(article) {
      this.editingArticle = article;
      this.articleForm = {
        title: article.title,
        category: article.category,
        content: article.content,
        tags: article.tags,
        sourceUrl: article.sourceUrl,
        priority: article.priority,
        isActive: article.isActive
      };
      this.showEditModal = true;
    },
    
    async saveArticle() {
      try {
        if (this.showEditModal) {
          await pennyApi.updateKnowledgeArticle(this.botId, this.editingArticle.id, this.articleForm);
          this.$toast.success('Article updated successfully');
        } else {
          await pennyApi.createKnowledgeArticle(this.botId, this.articleForm);
          this.$toast.success('Article created successfully');
        }
        this.closeModal();
        this.loadArticles();
        this.loadStats();
      } catch (error) {
        console.error('Error saving article:', error);
        this.$toast.error('Failed to save article');
      }
    },
    
    async deleteArticle(article) {
      if (!confirm('Are you sure you want to delete this article?')) return;
      
      try {
        await pennyApi.deleteKnowledgeArticle(this.botId, article.id);
        this.$toast.success('Article deleted successfully');
        this.loadArticles();
        this.loadStats();
      } catch (error) {
        console.error('Error deleting article:', error);
        this.$toast.error('Failed to delete article');
      }
    },
    
    async reembedArticle(article) {
      try {
        await pennyApi.reembedKnowledgeArticle(this.botId, article.id);
        this.$toast.success('Embedding regenerated successfully');
      } catch (error) {
        console.error('Error re-embedding article:', error);
        this.$toast.error('Failed to regenerate embedding');
      }
    },
    
    async handleTestSearch() {
      if (!this.testQuery) return;
      
      try {
        const response = await pennyApi.testKnowledgeBaseSearch(this.botId, this.testQuery);
        this.testResults = response.data;
      } catch (error) {
        console.error('Error testing search:', error);
        this.$toast.error('Search failed');
      }
    },
    
    closeModal() {
      this.showCreateModal = false;
      this.showEditModal = false;
      this.editingArticle = null;
      this.articleForm = {
        title: '',
        category: '',
        content: '',
        tags: '',
        sourceUrl: '',
        priority: 0,
        isActive: true
      };
    },
    
    formatDate(date) {
      if (!date) return '-';
      return new Date(date).toLocaleDateString();
    }
  }
};
</script>

<style scoped>
.knowledge-base-list {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
}

.text-success {
  color: #28a745;
}

.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.search-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.table-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.data-table th {
  background: #f8f9fa;
  font-weight: 600;
}

.actions {
  display: flex;
  gap: 5px;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.status-badge.active {
  background: #d4edda;
  color: #155724;
}

.status-badge.inactive {
  background: #f8d7da;
  color: #721c24;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  padding: 15px;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 8px;
  max-width: 600px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h2 {
  margin: 0;
}

.btn-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
}

.form-control {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
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

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-info {
  background: #17a2b8;
  color: white;
}

.btn-danger {
  background: #dc3545;
  color: white;
}

.btn-sm {
  padding: 4px 8px;
  font-size: 12px;
}

.test-results {
  margin-top: 20px;
  border-top: 1px solid #eee;
  padding-top: 15px;
}

.result-item {
  padding: 10px;
  border: 1px solid #eee;
  border-radius: 4px;
  margin-bottom: 10px;
}

.result-item h4 {
  margin: 0 0 5px 0;
}

.result-item p {
  margin: 0;
  color: #666;
}
</style>
