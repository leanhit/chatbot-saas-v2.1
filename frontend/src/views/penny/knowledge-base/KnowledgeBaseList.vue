<template>
  <div class="penny-knowledge-base">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6 pb-4 border-b border-gray-200 dark:border-gray-700">
      <div>
        <div class="flex items-center gap-3">
          <div class="p-2.5 bg-gradient-to-br from-purple-500 to-indigo-600 rounded-xl text-white shadow-md">
            <Icon icon="mdi:database-search-outline" class="text-2xl" />
          </div>
          <div>
            <h1 class="text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
              {{ $t('penny.knowledgeBase.title') }}
              <span v-if="selectedBot" class="px-2.5 py-0.5 text-xs font-semibold rounded-full bg-purple-100 text-purple-800 dark:bg-purple-900/40 dark:text-purple-300 border border-purple-200 dark:border-purple-700">
                {{ selectedBot.botName }}
              </span>
            </h1>
            <p class="text-sm text-gray-500 dark:text-gray-400 mt-0.5">
              {{ $t('penny.knowledgeBase.subtitle') }}
            </p>
          </div>
        </div>
      </div>
      <div class="flex items-center space-x-3">
        <!-- Bot Selector Dropdown -->
        <div v-if="availableBots.length > 0" class="flex items-center space-x-2">
          <select
            v-model="selectedBotId"
            @change="handleBotChange(selectedBotId)"
            class="px-3.5 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-800 text-gray-900 dark:text-white text-sm font-medium focus:ring-2 focus:ring-primary focus:outline-none shadow-sm"
          >
            <option value="" disabled>-- Chọn Penny Bot --</option>
            <option
              v-for="bot in availableBots"
              :key="bot.id || bot.botId"
              :value="bot.id || bot.botId"
            >
              {{ bot.botName }} - {{ getBotTypeDisplayName(bot.botType) }}
            </option>
          </select>
        </div>
        <div v-if="activeTab === 'articles'">
          <button
            @click="showCreateModal = true"
            :disabled="!effectiveBotId"
            class="inline-flex items-center px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/80 transition-colors text-sm font-medium shadow-sm disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <Icon icon="mdi:plus" class="mr-2" />
            {{ $t('penny.knowledgeBase.addArticle') }}
          </button>
        </div>
      </div>
    </div>

    <!-- Tabs -->
    <div class="border-b border-gray-200 dark:border-gray-700 mb-6">
      <nav class="flex space-x-8">
        <button
          @click="activeTab = 'articles'"
          :class="[
            'py-2 px-1 border-b-2 text-sm font-medium transition-colors',
            activeTab === 'articles'
              ? 'border-primary text-primary'
              : 'border-transparent text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300'
          ]"
        >
          <Icon icon="mdi:file-document" class="mr-2" />
          {{ $t('penny.knowledgeBase.title') }}
        </button>
        <button
          @click="activeTab = 'documents'"
          :class="[
            'py-2 px-1 border-b-2 text-sm font-medium transition-colors',
            activeTab === 'documents'
              ? 'border-primary text-primary'
              : 'border-transparent text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300'
          ]"
        >
          <Icon icon="mdi:cloud-upload" class="mr-2" />
          {{ $t('penny.documentUpload.title') }}
        </button>
      </nav>
    </div>

    <!-- Articles Tab -->
    <div v-if="activeTab === 'articles'">
      <!-- Warning Banner when no valid Bot ID -->
      <div v-if="!effectiveBotId" class="p-4 mb-6 bg-amber-50 dark:bg-amber-900/30 text-amber-800 dark:text-amber-200 rounded-md border border-amber-200 dark:border-amber-700 flex items-center justify-between">
        <div class="flex items-center">
          <Icon icon="mdi:alert-circle-outline" class="text-2xl mr-3 text-amber-600 dark:text-amber-400 flex-shrink-0" />
          <div>
            <h4 class="font-medium text-amber-900 dark:text-amber-100">Chưa chọn Bot</h4>
            <p class="text-sm">Vui lòng chọn một Penny Bot từ danh sách quản lý Bot để xem và quản lý Knowledge Base.</p>
          </div>
        </div>
        <router-link to="/penny/bots" class="ml-4 px-4 py-2 bg-amber-600 hover:bg-amber-700 text-white text-sm font-medium rounded-md transition-colors whitespace-nowrap">
          Đi đến danh sách Bot
        </router-link>
      </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
      <!-- Total Articles -->
      <div class="bg-white dark:bg-gray-800 p-5 rounded-md border border-gray-200 dark:border-gray-700 flex items-center shadow-sm">
        <div class="p-3 bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 rounded-full mr-4">
          <Icon icon="mdi:file-document-outline" class="text-2xl" />
        </div>
        <div>
          <p class="text-sm font-medium text-gray-500 dark:text-gray-400">
            {{ $t('penny.knowledgeBase.totalArticles') }}
          </p>
          <p class="text-2xl font-bold text-gray-900 dark:text-white mt-1">
            {{ stats.totalArticles || 0 }}
          </p>
        </div>
      </div>

      <!-- RAG Enabled -->
      <div class="bg-white dark:bg-gray-800 p-5 rounded-md border border-gray-200 dark:border-gray-700 flex items-center shadow-sm">
        <div class="p-3 bg-green-100 dark:bg-green-900/30 text-green-600 dark:text-green-400 rounded-full mr-4">
          <Icon icon="mdi:brain" class="text-2xl" />
        </div>
        <div>
          <p class="text-sm font-medium text-gray-500 dark:text-gray-400">
            {{ $t('penny.knowledgeBase.ragEnabled') }}
          </p>
          <p class="text-2xl font-bold mt-1" :class="stats.ragEnabled ? 'text-green-600 dark:text-green-400' : 'text-red-500'">
            {{ stats.ragEnabled ? $t('common.yes') || 'Yes' : $t('common.no') || 'No' }}
          </p>
        </div>
      </div>

      <!-- Embedding Model -->
      <div class="bg-white dark:bg-gray-800 p-5 rounded-md border border-gray-200 dark:border-gray-700 flex items-center shadow-sm">
        <div class="p-3 bg-purple-100 dark:bg-purple-900/30 text-purple-600 dark:text-purple-400 rounded-full mr-4">
          <Icon icon="mdi:vector-difference" class="text-2xl" />
        </div>
        <div>
          <p class="text-sm font-medium text-gray-500 dark:text-gray-400">
            {{ $t('penny.knowledgeBase.embeddingModel') }}
          </p>
          <p class="text-lg font-bold text-gray-900 dark:text-white mt-1 truncate max-w-[200px]" :title="stats.embeddingModel">
            {{ stats.embeddingModel || 'N/A' }}
          </p>
        </div>
      </div>
    </div>

    <!-- Search and Filter -->
    <div class="flex flex-col md:flex-row gap-3 mb-6">
      <div class="flex-1 relative">
        <input
          v-model="searchQuery"
          type="text"
          :placeholder="$t('penny.knowledgeBase.searchPlaceholder')"
          class="w-full pl-10 pr-4 py-2 border border-gray-300 dark:border-gray-700 rounded-md bg-white dark:bg-gray-900 text-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary text-sm"
          @keyup.enter="handleSearch"
        />
        <Icon icon="mdi:magnify" class="absolute left-3 top-2.5 text-gray-400 text-lg" />
      </div>
      <div class="flex gap-2">
        <button
          @click="handleSearch"
          class="px-4 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-md text-sm font-medium transition-colors"
        >
          {{ $t('penny.knowledgeBase.search') }}
        </button>
        <button
          @click="showTestSearch = true"
          class="inline-flex items-center px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-md text-sm font-medium transition-colors"
        >
          <Icon icon="mdi:file-search" class="mr-2" />
          {{ $t('penny.knowledgeBase.testSearch') }}
        </button>
      </div>
    </div>

    <!-- Articles Table -->
    <div class="bg-white dark:bg-gray-800 rounded-md border border-gray-200 dark:border-gray-700 overflow-hidden shadow-sm">
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
          <thead class="bg-gray-50 dark:bg-gray-700">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-semibold text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                {{ $t('penny.knowledgeBase.articleTitle') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-semibold text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                {{ $t('penny.knowledgeBase.category') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-semibold text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                {{ $t('penny.knowledgeBase.priority') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-semibold text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                {{ $t('penny.knowledgeBase.status') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-semibold text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                {{ $t('penny.knowledgeBase.updated') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-semibold text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                {{ $t('penny.knowledgeBase.actions') }}
              </th>
            </tr>
          </thead>
          <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
            <tr v-if="loading">
              <td colspan="6" class="px-6 py-10 text-center text-gray-500">
                <Icon icon="mdi:loading" class="text-4xl text-gray-300 animate-spin mx-auto mb-2" />
                {{ $t('penny.knowledgeBase.loading') }}
              </td>
            </tr>
            <tr v-else-if="articles.length === 0">
              <td colspan="6" class="px-6 py-10 text-center text-gray-500">
                {{ $t('penny.knowledgeBase.noArticles') }}
              </td>
            </tr>
            <tr v-for="article in articles" :key="article.id" v-else>
              <td class="px-6 py-4 text-sm text-gray-900 dark:text-white font-medium">
                {{ article.title }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                {{ article.category ? $t('penny.knowledgeBase.categories.' + article.category.toLowerCase()) : '-' }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                {{ article.priority || 0 }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm">
                <span
                  :class="[
                    'px-2.5 py-1 rounded-full text-xs font-medium',
                    article.isActive
                      ? 'bg-green-100 text-green-800 dark:bg-green-900/50 dark:text-green-300'
                      : 'bg-red-100 text-red-800 dark:bg-red-900/50 dark:text-red-300'
                  ]"
                >
                  {{ article.isActive ? $t('penny.knowledgeBase.activeLabel') : $t('penny.knowledgeBase.categories.inactive') || 'Inactive' }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                {{ formatDate(article.updatedAt) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm">
                <div class="flex gap-2">
                  <button
                    @click="editArticle(article)"
                    class="inline-flex items-center p-1.5 bg-blue-100 text-blue-700 dark:bg-blue-900/50 dark:text-blue-300 rounded hover:bg-blue-200 dark:hover:bg-blue-900 transition-colors"
                    :title="$t('penny.knowledgeBase.edit')"
                  >
                    <Icon icon="mdi:pencil" class="text-lg" />
                  </button>
                  <button
                    @click="reembedArticle(article)"
                    class="inline-flex items-center p-1.5 bg-indigo-100 text-indigo-700 dark:bg-indigo-900/50 dark:text-indigo-300 rounded hover:bg-indigo-200 dark:hover:bg-indigo-900 transition-colors"
                    :title="$t('penny.knowledgeBase.reembed')"
                  >
                    <Icon icon="mdi:refresh" class="text-lg" />
                  </button>
                  <button
                    @click="deleteArticle(article)"
                    class="inline-flex items-center p-1.5 bg-red-100 text-red-700 dark:bg-red-900/50 dark:text-red-300 rounded hover:bg-red-200 dark:hover:bg-red-900 transition-colors"
                    :title="$t('penny.knowledgeBase.delete')"
                  >
                    <Icon icon="mdi:delete" class="text-lg" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="flex justify-center items-center py-4 border-t border-gray-200 dark:border-gray-700 space-x-2 bg-gray-50 dark:bg-gray-800">
        <button
          @click="changePage(currentPage - 1)"
          :disabled="currentPage === 0"
          class="px-3 py-1.5 border border-gray-300 dark:border-gray-650 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white disabled:opacity-50 text-sm font-medium transition-colors hover:bg-gray-50 dark:hover:bg-gray-600"
        >
          {{ $t('penny.knowledgeBase.previous') }}
        </button>
        <span class="px-3 py-1.5 text-gray-900 dark:text-white text-sm">
          {{ $t('penny.knowledgeBase.pageOf', { page: currentPage + 1, total: totalPages }) }}
        </span>
        <button
          @click="changePage(currentPage + 1)"
          :disabled="currentPage === totalPages - 1"
          class="px-3 py-1.5 border border-gray-300 dark:border-gray-650 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white disabled:opacity-50 text-sm font-medium transition-colors hover:bg-gray-50 dark:hover:bg-gray-600"
        >
          {{ $t('penny.knowledgeBase.next') }}
        </button>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <div v-if="showCreateModal || showEditModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
      <div class="bg-white dark:bg-gray-800 rounded-lg max-w-2xl w-full border border-gray-200 dark:border-gray-700 shadow-xl overflow-hidden max-h-[90vh] overflow-y-auto">
        <div class="flex justify-between items-center px-6 py-4 border-b border-gray-200 dark:border-gray-700">
          <h2 class="text-lg font-bold text-gray-900 dark:text-white">
            {{ showEditModal ? $t('penny.knowledgeBase.editArticle') : $t('penny.knowledgeBase.createArticle') }}
          </h2>
          <button @click="closeModal" class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-250">
            <Icon icon="mdi:close" class="text-xl" />
          </button>
        </div>
        <div class="p-6">
          <form @submit.prevent="saveArticle" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                {{ $t('penny.knowledgeBase.titleLabel') }}
              </label>
              <input
                v-model="articleForm.title"
                type="text"
                required
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-md bg-white dark:bg-gray-900 text-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary text-sm"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                {{ $t('penny.knowledgeBase.categoryLabel') }}
              </label>
              <select
                v-model="articleForm.category"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-md bg-white dark:bg-gray-900 text-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary text-sm"
              >
                <option value="">{{ $t('penny.knowledgeBase.selectCategory') }}</option>
                <option value="faq">{{ $t('penny.knowledgeBase.categories.faq') }}</option>
                <option value="product">{{ $t('penny.knowledgeBase.categories.product') }}</option>
                <option value="policy">{{ $t('penny.knowledgeBase.categories.policy') }}</option>
                <option value="shipping">{{ $t('penny.knowledgeBase.categories.shipping') }}</option>
                <option value="price">{{ $t('penny.knowledgeBase.categories.price') }}</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                {{ $t('penny.knowledgeBase.contentLabel') }}
              </label>
              <textarea
                v-model="articleForm.content"
                required
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-md bg-white dark:bg-gray-900 text-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary text-sm"
                rows="6"
              ></textarea>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                {{ $t('penny.knowledgeBase.tagsLabel') }}
              </label>
              <input
                v-model="articleForm.tags"
                type="text"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-md bg-white dark:bg-gray-900 text-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary text-sm"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                {{ $t('penny.knowledgeBase.sourceUrlLabel') }}
              </label>
              <input
                v-model="articleForm.sourceUrl"
                type="url"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-md bg-white dark:bg-gray-900 text-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary text-sm"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                {{ $t('penny.knowledgeBase.priorityLabel') }}
              </label>
              <input
                v-model="articleForm.priority"
                type="number"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-md bg-white dark:bg-gray-900 text-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary text-sm"
                min="0"
              />
            </div>
            <div class="flex items-center">
              <input
                v-model="articleForm.isActive"
                type="checkbox"
                id="article-active"
                class="w-4 h-4 text-primary border-gray-300 dark:border-gray-700 rounded focus:ring-primary bg-white dark:bg-gray-900"
              />
              <label for="article-active" class="ml-2 text-sm font-medium text-gray-700 dark:text-gray-300">
                {{ $t('penny.knowledgeBase.activeLabel') }}
              </label>
            </div>
            <div class="flex justify-end gap-3 mt-6">
              <button
                type="button"
                @click="closeModal"
                class="px-4 py-2 border border-gray-300 dark:border-gray-700 rounded-md bg-white dark:bg-gray-900 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-800 text-sm font-medium transition-colors"
              >
                {{ $t('penny.knowledgeBase.cancel') }}
              </button>
              <button
                type="submit"
                class="px-4 py-2 bg-primary text-white rounded-md hover:bg-primary/80 text-sm font-medium transition-colors"
              >
                {{ $t('penny.knowledgeBase.save') }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Test Search Modal -->
    <div v-if="showTestSearch" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
      <div class="bg-white dark:bg-gray-800 rounded-lg max-w-lg w-full border border-gray-200 dark:border-gray-700 shadow-xl overflow-hidden max-h-[90vh] overflow-y-auto">
        <div class="flex justify-between items-center px-6 py-4 border-b border-gray-200 dark:border-gray-700">
          <h2 class="text-lg font-bold text-gray-900 dark:text-white">
            {{ $t('penny.knowledgeBase.testSearchTitle') }}
          </h2>
          <button @click="showTestSearch = false" class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-250">
            <Icon icon="mdi:close" class="text-xl" />
          </button>
        </div>
        <div class="p-6">
          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              {{ $t('penny.knowledgeBase.searchQueryLabel') }}
            </label>
            <input
              v-model="testQuery"
              type="text"
              class="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-md bg-white dark:bg-gray-900 text-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary text-sm"
              @keyup.enter="handleTestSearch"
            />
          </div>
          <button
            @click="handleTestSearch"
            class="px-4 py-2 bg-primary text-white rounded-md hover:bg-primary/80 text-sm font-medium transition-colors"
          >
            {{ $t('penny.knowledgeBase.search') }}
          </button>

          <div v-if="testResults" class="mt-6 border-t border-gray-200 dark:border-gray-700 pt-4">
            <h3 class="text-sm font-semibold text-gray-900 dark:text-white mb-3">
              {{ $t('penny.knowledgeBase.results', { count: testResults.count }) }}
            </h3>
            <div class="space-y-3 max-h-[40vh] overflow-y-auto pr-2">
              <div v-for="article in testResults.articles" :key="article.id" class="p-3 border border-gray-200 dark:border-gray-700 rounded bg-gray-50 dark:bg-gray-700/30">
                <h4 class="font-medium text-sm text-gray-900 dark:text-white mb-1">{{ article.title }}</h4>
                <p class="text-xs text-gray-500 dark:text-gray-400 line-clamp-3">{{ article.content }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    </div>

    <!-- Documents Tab -->
    <div v-if="activeTab === 'documents'">
      <DocumentUpload :bot-id="effectiveBotId" :tenant-id="tenantId" />
    </div>
  </div>
</template>

<script>
import { Icon } from '@iconify/vue';
import { pennyApi } from '@/api/pennyApi';
import { usePennyBotStore } from '@/stores/pennyBotStore';
import DocumentUpload from './DocumentUpload.vue';

export default {
  name: 'KnowledgeBaseList',
  components: {
    Icon,
    DocumentUpload
  },
  props: {
    botId: {
      type: String,
      required: false
    },
    tenantId: {
      type: [Number, String],
      required: false
    }
  },
  data() {
    return {
      activeTab: 'articles',
      selectedBotId: null,
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
  computed: {
    pennyBotStore() {
      return usePennyBotStore();
    },
    availableBots() {
      return this.pennyBotStore.pennyBots || [];
    },
    effectiveBotId() {
      const clean = (val) => {
        if (!val) return null;
        const s = String(val).trim();
        if (!s || s === 'undefined' || s === 'null') return null;
        const uuidRegex = /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/;
        return uuidRegex.test(s) ? s : null;
      };

      return clean(this.botId) || clean(this.$route?.params?.botId) || clean(this.selectedBotId) || clean(this.pennyBotStore.currentBotId);
    }
  },
  watch: {
    availableBots: {
      immediate: true,
      handler(bots) {
        if (bots && bots.length > 0) {
          const currentTarget = this.effectiveBotId;
          if (currentTarget && bots.some(b => (b.id || b.botId) === currentTarget)) {
            this.selectedBotId = currentTarget;
          } else if (!this.selectedBotId) {
            const firstValidBot = bots.find(b => (b.id || b.botId));
            if (firstValidBot) {
              const firstId = firstValidBot.id || firstValidBot.botId;
              this.selectedBotId = firstId;
              this.pennyBotStore.setCurrentBotId(firstId);
            }
          }
        }
      }
    },
    effectiveBotId: {
      immediate: true,
      handler(newBotId) {
        if (newBotId) {
          if (this.selectedBotId !== newBotId) {
            this.selectedBotId = newBotId;
          }
          this.pennyBotStore.setCurrentBotId(newBotId);
          if (this.$route?.params?.botId === 'undefined' || (this.$route?.name === 'penny-knowledge-base' && this.$route.params?.botId !== newBotId)) {
            this.$router.replace({ name: 'penny-knowledge-base', params: { botId: newBotId } }).catch(() => {});
          }
          this.loadArticles();
          this.loadStats();
        }
      }
    }
  },
  async mounted() {
    if (this.availableBots.length === 0) {
      try {
        await this.pennyBotStore.fetchPennyBots();
      } catch (err) {
        console.error('Failed to fetch Penny bots:', err);
      }
    }
  },
  methods: {
    getBotTypeDisplayName(botType) {
      const names = {
        'GENERAL': 'General Purpose',
        'SUPPORT': 'Customer Support',
        'BUSINESS': 'Business & Sales',
        'BOTPRESS': 'Botpress Integration'
      };
      return names[botType] || botType || 'General';
    },
    handleBotChange(newBotId) {
      this.selectedBotId = newBotId;
      if (newBotId) {
        this.pennyBotStore.setCurrentBotId(newBotId);
        if (this.$route?.name === 'penny-knowledge-base' && this.$route.params?.botId !== newBotId) {
          this.$router.replace({ name: 'penny-knowledge-base', params: { botId: newBotId } }).catch(() => {});
        }
      }
      this.loadArticles();
      this.loadStats();
    },
    async loadArticles() {
      if (!this.effectiveBotId) {
        console.warn('Cannot load articles: effectiveBotId is missing or undefined');
        return;
      }
      this.loading = true;
      try {
        const response = await pennyApi.getKnowledgeArticles(
          this.effectiveBotId,
          this.currentPage,
          this.pageSize
        );
        this.articles = response.data?.content || response.data || [];
        this.totalPages = response.data?.totalPages || 1;
      } catch (error) {
        console.error('Error loading articles:', error);
        if (this.$toast) this.$toast.error(this.$t('penny.knowledgeBase.failedToLoad'));
      } finally {
        this.loading = false;
      }
    },
    
    async loadStats() {
      if (!this.effectiveBotId) {
        console.warn('Cannot load stats: effectiveBotId is missing or undefined');
        return;
      }
      try {
        const response = await pennyApi.getKnowledgeBaseStats(this.effectiveBotId);
        this.stats = response.data || {};
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
      if (!this.effectiveBotId) return;
      try {
        if (this.showEditModal) {
          await pennyApi.updateKnowledgeArticle(this.effectiveBotId, this.editingArticle.id, this.articleForm);
          if (this.$toast) this.$toast.success(this.$t('penny.knowledgeBase.articleUpdated'));
        } else {
          await pennyApi.createKnowledgeArticle(this.effectiveBotId, this.articleForm);
          if (this.$toast) this.$toast.success(this.$t('penny.knowledgeBase.articleCreated'));
        }
        this.closeModal();
        this.loadArticles();
        this.loadStats();
      } catch (error) {
        console.error('Error saving article:', error);
        if (this.$toast) this.$toast.error(this.$t('penny.knowledgeBase.failedToSave'));
      }
    },
    
    async deleteArticle(article) {
      if (!this.effectiveBotId) return;
      if (!confirm(this.$t('penny.knowledgeBase.deleteConfirm'))) return;
      
      try {
        await pennyApi.deleteKnowledgeArticle(this.effectiveBotId, article.id);
        if (this.$toast) this.$toast.success(this.$t('penny.knowledgeBase.articleDeleted'));
        this.loadArticles();
        this.loadStats();
      } catch (error) {
        console.error('Error deleting article:', error);
        if (this.$toast) this.$toast.error(this.$t('penny.knowledgeBase.failedToDelete'));
      }
    },
    
    async reembedArticle(article) {
      if (!this.effectiveBotId) return;
      try {
        await pennyApi.reembedKnowledgeArticle(this.effectiveBotId, article.id);
        if (this.$toast) this.$toast.success(this.$t('penny.knowledgeBase.embeddingRegenerated'));
      } catch (error) {
        console.error('Error re-embedding article:', error);
        if (this.$toast) this.$toast.error(this.$t('penny.knowledgeBase.failedToReembed'));
      }
    },
    
    async handleTestSearch() {
      if (!this.testQuery || !this.effectiveBotId) return;
      
      try {
        const response = await pennyApi.testKnowledgeBaseSearch(this.effectiveBotId, this.testQuery);
        this.testResults = response.data;
      } catch (error) {
        console.error('Error testing search:', error);
        if (this.$toast) this.$toast.error(this.$t('penny.knowledgeBase.failedToSearch'));
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
.penny-knowledge-base {
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
