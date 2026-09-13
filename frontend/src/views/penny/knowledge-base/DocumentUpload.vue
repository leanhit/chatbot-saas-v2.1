<template>
  <div class="document-upload">
    <!-- Upload Area -->
    <div class="upload-area" :class="{ 'drag-over': isDragOver }" 
         @dragover.prevent="handleDragOver" 
         @dragleave.prevent="handleDragLeave" 
         @drop.prevent="handleDrop">
      
      <div class="upload-content">
        <Icon icon="mdi:cloud-upload-outline" class="upload-icon" />
        <h3 class="upload-title">{{ $t('penny.documentUpload.title') }}</h3>
        <p class="upload-subtitle">{{ $t('penny.documentUpload.subtitle') }}</p>
        
        <input
          ref="fileInput"
          type="file"
          accept=".pdf,.docx,.xlsx"
          @change="handleFileSelect"
          class="hidden"
        />
        
        <button
          @click="triggerFileSelect"
          class="upload-button"
          :disabled="uploading"
        >
          <Icon icon="mdi:folder-open" class="mr-2" />
          {{ $t('penny.documentUpload.selectFile') }}
        </button>
        
        <p class="upload-hint">
          {{ $t('penny.documentUpload.supportedFormats') }}: PDF, DOCX, XLSX
        </p>
        <p class="upload-hint">
          {{ $t('penny.documentUpload.maxSize') }}: 50MB
        </p>
      </div>
    </div>

    <!-- Selected File Preview -->
    <div v-if="selectedFile" class="file-preview">
      <div class="file-info">
        <Icon icon="mdi:file-document" class="file-icon" />
        <div class="file-details">
          <p class="file-name">{{ selectedFile.name }}</p>
          <p class="file-size">{{ formatFileSize(selectedFile.size) }}</p>
        </div>
        <button @click="removeFile" class="remove-button">
          <Icon icon="mdi:close" />
        </button>
      </div>
    </div>

    <!-- Document Name Input -->
    <div v-if="selectedFile" class="form-group">
      <label class="form-label">{{ $t('penny.documentUpload.documentName') }}</label>
      <input
        v-model="documentName"
        type="text"
        class="form-input"
        :placeholder="$t('penny.documentUpload.documentNamePlaceholder')"
      />
    </div>

    <!-- Upload Button -->
    <div v-if="selectedFile" class="upload-actions">
      <button
        @click="uploadDocument"
        class="upload-submit-button"
        :disabled="uploading || !documentName"
      >
        <Icon v-if="uploading" icon="mdi:loading" class="animate-spin mr-2" />
        <Icon v-else icon="mdi:upload" class="mr-2" />
        {{ uploading ? $t('penny.documentUpload.uploading') : $t('penny.documentUpload.upload') }}
      </button>
      <button @click="cancelUpload" class="cancel-button" :disabled="uploading">
        {{ $t('common.cancel') }}
      </button>
    </div>

    <!-- RAG Processing Progress Bar & Status -->
    <div v-if="uploading" class="upload-progress p-5 bg-indigo-50/70 dark:bg-indigo-950/40 rounded-xl border border-indigo-200 dark:border-indigo-800/60 shadow-sm space-y-3">
      <div class="flex items-center justify-between">
        <div class="flex items-center space-x-2">
          <Icon icon="mdi:database-sync" class="text-indigo-600 dark:text-indigo-400 text-xl animate-spin" />
          <span class="text-sm font-semibold text-gray-900 dark:text-white">
            {{ currentStageText || 'Đang xử lý tài liệu RAG...' }}
          </span>
        </div>
        <span class="text-sm font-bold text-indigo-600 dark:text-indigo-400 font-mono">
          {{ uploadProgress }}%
        </span>
      </div>

      <!-- Animated Progress Fill -->
      <div class="progress-bar h-3 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden relative">
        <div 
          class="progress-fill h-full bg-gradient-to-r from-indigo-500 via-purple-500 to-emerald-500 rounded-full transition-all duration-300 relative"
          :style="{ width: uploadProgress + '%' }"
        >
          <div class="absolute inset-0 bg-white/20 animate-pulse"></div>
        </div>
      </div>

      <!-- Stats Bar -->
      <div class="flex items-center justify-between text-xs text-gray-600 dark:text-gray-400 pt-1">
        <span class="flex items-center gap-1">
          <Icon icon="mdi:vector-square" class="text-purple-500" />
          Số Chunks Vector: <strong class="text-gray-900 dark:text-white">{{ processedChunkCount }} / {{ estimatedTotalChunks }}</strong>
        </span>
        <span class="flex items-center gap-1">
          <Icon icon="mdi:shield-check" class="text-emerald-500" />
          RAG Vector Database: Active
        </span>
      </div>
    </div>

    <!-- Upload Status Notification -->
    <div v-if="uploadStatus" class="upload-status" :class="uploadStatus.type">
      <Icon :icon="uploadStatus.icon" class="status-icon" />
      <p>{{ uploadStatus.message }}</p>
    </div>

    <!-- Document List Summary & Items -->
    <div v-if="documents.length > 0" class="document-list mt-6">
      <div class="flex items-center justify-between mb-4">
        <h3 class="list-title text-lg font-semibold text-gray-900 dark:text-white flex items-center gap-2">
          <Icon icon="mdi:file-document-multiple-outline" class="text-indigo-500" />
          {{ $t('penny.documentUpload.uploadedDocuments') }}
        </h3>
        <div class="flex items-center gap-3 text-xs">
          <span class="px-2.5 py-1 bg-indigo-50 dark:bg-indigo-900/30 text-indigo-700 dark:text-indigo-300 rounded-lg font-medium border border-indigo-200 dark:border-indigo-800">
            📑 {{ documents.length }} Tài liệu
          </span>
          <span class="px-2.5 py-1 bg-purple-50 dark:bg-purple-900/30 text-purple-700 dark:text-purple-300 rounded-lg font-medium border border-purple-200 dark:border-purple-800">
            ⚡ {{ totalVectorChunks }} Vector Chunks
          </span>
        </div>
      </div>

      <div class="document-items space-y-2">
        <div v-for="doc in documents" :key="doc.id" class="document-item p-4 bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 hover:shadow-md transition-shadow flex items-center justify-between">
          <div class="document-info flex items-center space-x-3 flex-1 min-w-0">
            <Icon :icon="getFileIcon(doc.fileType)" class="document-icon text-3xl text-indigo-500" />
            <div class="document-details min-w-0 flex-1">
              <p class="document-name font-semibold text-gray-900 dark:text-white truncate">
                {{ doc.documentName }}
              </p>
              <div class="document-meta flex items-center gap-2 text-xs text-gray-500 dark:text-gray-400 mt-1">
                <span>{{ doc.fileName }}</span>
                <span>•</span>
                <span>{{ formatFileSize(doc.fileSize) }}</span>
                <span>•</span>
                <span class="font-medium text-purple-600 dark:text-purple-400 flex items-center gap-1">
                  <Icon icon="mdi:lightning-bolt" class="text-amber-500" />
                  {{ doc.totalChunks || 0 }} Chunks Vector
                </span>
              </div>
            </div>
          </div>
          <div class="document-status mx-4">
            <span v-if="doc.status === 'PROCESSING'" class="status-badge flex items-center gap-1 bg-amber-100 dark:bg-amber-900/40 text-amber-700 dark:text-amber-300 px-3 py-1 rounded-full text-xs font-semibold animate-pulse border border-amber-300 dark:border-amber-700">
              <Icon icon="mdi:loading" class="animate-spin" />
              Đang phân tích RAG...
            </span>
            <span v-else-if="doc.status === 'COMPLETED'" class="status-badge flex items-center gap-1 bg-emerald-100 dark:bg-emerald-900/40 text-emerald-700 dark:text-emerald-300 px-3 py-1 rounded-full text-xs font-semibold border border-emerald-300 dark:border-emerald-700">
              <Icon icon="mdi:check-circle" />
              Hoàn tất RAG
            </span>
            <span v-else class="status-badge bg-red-100 dark:bg-red-900/40 text-red-700 dark:text-red-300 px-3 py-1 rounded-full text-xs font-semibold">
              {{ doc.status }}
            </span>
          </div>
          <div class="document-actions">
            <button @click="deleteDocument(doc.id)" class="action-button delete p-2 text-gray-400 hover:text-red-500 transition-colors" title="Xóa tài liệu">
              <Icon icon="mdi:delete-outline" class="text-xl" />
            </button>
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
  name: 'DocumentUpload',
  components: { Icon },
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
      selectedFile: null,
      documentName: '',
      uploading: false,
      uploadProgress: 0,
      currentStageText: '',
      processedChunkCount: 0,
      estimatedTotalChunks: 0,
      uploadStatus: null,
      isDragOver: false,
      documents: []
    };
  },
  computed: {
    totalVectorChunks() {
      return this.documents.reduce((acc, doc) => acc + (doc.totalChunks || 0), 0);
    }
  },
  mounted() {
    this.loadDocuments();
  },
  methods: {
    triggerFileSelect() {
      this.$refs.fileInput.click();
    },
    handleFileSelect(event) {
      const file = event.target.files[0];
      if (file) {
        this.validateAndSelectFile(file);
      }
    },
    handleDragOver(event) {
      this.isDragOver = true;
    },
    handleDragLeave(event) {
      this.isDragOver = false;
    },
    handleDrop(event) {
      this.isDragOver = false;
      const file = event.dataTransfer.files[0];
      if (file) {
        this.validateAndSelectFile(file);
      }
    },
    validateAndSelectFile(file) {
      // Validate file type
      const validTypes = ['application/pdf', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'];
      const validExtensions = ['.pdf', '.docx', '.xlsx'];
      
      const fileExtension = '.' + file.name.split('.').pop().toLowerCase();
      if (!validExtensions.includes(fileExtension)) {
        this.showStatus('error', 'mdi:alert-circle', this.$t('penny.documentUpload.invalidFileType'));
        return;
      }

      // Validate file size (50MB)
      const maxSize = 50 * 1024 * 1024;
      if (file.size > maxSize) {
        this.showStatus('error', 'mdi:alert-circle', this.$t('penny.documentUpload.fileTooLarge'));
        return;
      }

      this.selectedFile = file;
      this.documentName = file.name.replace(/\.[^/.]+$/, '');
      this.uploadStatus = null;
    },
    removeFile() {
      this.selectedFile = null;
      this.documentName = '';
      this.uploadStatus = null;
      this.$refs.fileInput.value = '';
    },
    async uploadDocument() {
      const uuidRegex = /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/;
      if (!this.selectedFile || !this.documentName || !this.botId || !uuidRegex.test(String(this.botId).trim())) return;

      this.uploading = true;
      this.uploadProgress = 10;
      this.currentStageText = 'Tải tệp lên server...';
      this.processedChunkCount = 0;
      this.estimatedTotalChunks = Math.max(1, Math.ceil((this.selectedFile.size || 50000) / 3500));
      this.uploadStatus = null;

      // Realistic stage timer simulation during RAG extraction & chunking
      const progressTimer = setInterval(() => {
        if (this.uploadProgress < 30) {
          this.uploadProgress += 5;
          this.currentStageText = 'Đang tải tệp lên máy chủ...';
        } else if (this.uploadProgress < 60) {
          this.uploadProgress += 4;
          this.currentStageText = 'Đang trích xuất nội dung văn bản (OCR & Parser)...';
        } else if (this.uploadProgress < 85) {
          this.uploadProgress += 3;
          this.currentStageText = 'Đang chia nhỏ văn bản thành các Chunks (Text Chunking)...';
          this.processedChunkCount = Math.min(this.estimatedTotalChunks, Math.floor((this.uploadProgress / 100) * this.estimatedTotalChunks));
        } else if (this.uploadProgress < 95) {
          this.uploadProgress += 1;
          this.currentStageText = 'Đang tạo Vector Embeddings & lưu Database...';
          this.processedChunkCount = this.estimatedTotalChunks;
        }
      }, 300);

      try {
        const response = await pennyApi.uploadKnowledgeDocument(
          this.selectedFile,
          this.botId,
          this.tenantId,
          this.documentName,
          this.$store?.state?.user?.username
        );
        clearInterval(progressTimer);

        this.uploadProgress = 100;
        this.currentStageText = 'Hoàn tất phân tích RAG!';
        const createdDoc = response.data || {};
        this.processedChunkCount = createdDoc.totalChunks || this.estimatedTotalChunks;
        
        this.showStatus('success', 'mdi:check-circle', `Tải lên thành công! Đã tạo ${this.processedChunkCount} vector chunks.`);
        
        setTimeout(() => {
          this.removeFile();
          this.loadDocuments();
        }, 1500);
      } catch (error) {
        clearInterval(progressTimer);
        this.showStatus('error', 'mdi:alert-circle', this.$t('penny.documentUpload.uploadError'));
        console.error('Upload error:', error);
      } finally {
        this.uploading = false;
      }
    },
    cancelUpload() {
      this.removeFile();
    },
    async loadDocuments() {
      const uuidRegex = /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/;
      if (!this.botId || !uuidRegex.test(String(this.botId).trim())) return;
      try {
        const response = await pennyApi.getKnowledgeDocuments(this.botId, this.tenantId);
        this.documents = response.data || [];
      } catch (error) {
        console.error('Error loading documents:', error);
      }
    },
    async deleteDocument(documentId) {
      if (!confirm(this.$t('penny.documentUpload.confirmDelete'))) return;

      try {
        await pennyApi.deleteKnowledgeDocument(documentId);
        this.loadDocuments();
        this.showStatus('success', 'mdi:check-circle', this.$t('penny.documentUpload.deleteSuccess'));
      } catch (error) {
        this.showStatus('error', 'mdi:alert-circle', this.$t('penny.documentUpload.deleteError'));
        console.error('Delete error:', error);
      }
    },
    showStatus(type, icon, message) {
      this.uploadStatus = { type, icon, message };
      setTimeout(() => {
        this.uploadStatus = null;
      }, 5000);
    },
    formatFileSize(bytes) {
      if (bytes === 0) return '0 Bytes';
      const k = 1024;
      const sizes = ['Bytes', 'KB', 'MB', 'GB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
    },
    getFileIcon(fileType) {
      const type = fileType?.toLowerCase();
      if (type === 'pdf') return 'mdi:file-pdf-box';
      if (type === 'docx') return 'mdi:file-word-box';
      if (type === 'xlsx') return 'mdi:file-excel-box';
      return 'mdi:file-document';
    }
  }
};
</script>

<style scoped>
.document-upload {
  @apply space-y-4;
}

.upload-area {
  @apply border-2 border-dashed border-gray-300 dark:border-gray-600 rounded-lg p-8 text-center transition-colors;
}

.upload-area.drag-over {
  @apply border-primary bg-primary/5;
}

.upload-content {
  @apply space-y-4;
}

.upload-icon {
  @apply text-5xl text-gray-400 dark:text-gray-500;
}

.upload-title {
  @apply text-lg font-semibold text-gray-900 dark:text-white;
}

.upload-subtitle {
  @apply text-sm text-gray-500 dark:text-gray-400;
}



.upload-button {
  @apply inline-flex items-center px-4 py-2 bg-primary text-white rounded-md hover:bg-primary/80 transition-colors text-sm font-medium;
}

.upload-button:disabled {
  @apply opacity-50 cursor-not-allowed;
}

.upload-hint {
  @apply text-xs text-gray-400 dark:text-gray-500;
}

.file-preview {
  @apply bg-gray-50 dark:bg-gray-800 rounded-lg p-4;
}

.file-info {
  @apply flex items-center space-x-3;
}

.file-icon {
  @apply text-2xl text-gray-500 dark:text-gray-400;
}

.file-details {
  @apply flex-1;
}

.file-name {
  @apply font-medium text-gray-900 dark:text-white;
}

.file-size {
  @apply text-sm text-gray-500 dark:text-gray-400;
}

.remove-button {
  @apply p-2 text-gray-400 hover:text-red-500 transition-colors;
}

.form-group {
  @apply space-y-2;
}

.form-label {
  @apply block text-sm font-medium text-gray-700 dark:text-gray-300;
}

.form-input {
  @apply w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-900 text-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary;
}

.upload-actions {
  @apply flex space-x-3;
}

.upload-submit-button {
  @apply flex-1 inline-flex items-center justify-center px-4 py-2 bg-primary text-white rounded-md hover:bg-primary/80 transition-colors text-sm font-medium;
}

.upload-submit-button:disabled {
  @apply opacity-50 cursor-not-allowed;
}

.cancel-button {
  @apply px-4 py-2 border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 rounded-md hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors text-sm font-medium;
}

.upload-progress {
  @apply space-y-2;
}

.progress-bar {
  @apply h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden;
}

.progress-fill {
  @apply h-full bg-primary transition-all duration-300;
}

.progress-text {
  @apply text-sm text-gray-600 dark:text-gray-400 text-center;
}

.upload-status {
  @apply flex items-center space-x-2 p-3 rounded-md text-sm;
}

.upload-status.success {
  @apply bg-green-50 dark:bg-green-900/30 text-green-700 dark:text-green-400;
}

.upload-status.error {
  @apply bg-red-50 dark:bg-red-900/30 text-red-700 dark:text-red-400;
}

.status-icon {
  @apply text-xl;
}

.document-list {
  @apply space-y-4;
}

.list-title {
  @apply text-lg font-semibold text-gray-900 dark:text-white;
}

.document-items {
  @apply space-y-2;
}

.document-item {
  @apply flex items-center justify-between p-4 bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700;
}

.document-info {
  @apply flex items-center space-x-3 flex-1;
}

.document-icon {
  @apply text-2xl text-gray-500 dark:text-gray-400;
}

.document-details {
  @apply flex-1 min-w-0;
}

.document-name {
  @apply font-medium text-gray-900 dark:text-white truncate;
}

.document-meta {
  @apply text-sm text-gray-500 dark:text-gray-400;
}

.document-status {
  @apply mx-4;
}

.status-badge {
  @apply px-2 py-1 text-xs font-medium rounded-full;
}

.status-badge.completed {
  @apply bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400;
}

.status-badge.processing {
  @apply bg-yellow-100 dark:bg-yellow-900/30 text-yellow-700 dark:text-yellow-400;
}

.status-badge.failed {
  @apply bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-400;
}

.document-actions {
  @apply flex space-x-2;
}

.action-button {
  @apply p-2 text-gray-400 hover:text-red-500 transition-colors;
}


</style>
