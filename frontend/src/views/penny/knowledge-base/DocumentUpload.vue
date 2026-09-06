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

    <!-- Upload Progress -->
    <div v-if="uploading" class="upload-progress">
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: uploadProgress + '%' }"></div>
      </div>
      <p class="progress-text">{{ uploadProgress }}%</p>
    </div>

    <!-- Upload Status -->
    <div v-if="uploadStatus" class="upload-status" :class="uploadStatus.type">
      <Icon :icon="uploadStatus.icon" class="status-icon" />
      <p>{{ uploadStatus.message }}</p>
    </div>

    <!-- Document List -->
    <div v-if="documents.length > 0" class="document-list">
      <h3 class="list-title">{{ $t('penny.documentUpload.uploadedDocuments') }}</h3>
      <div class="document-items">
        <div v-for="doc in documents" :key="doc.id" class="document-item">
          <div class="document-info">
            <Icon :icon="getFileIcon(doc.fileType)" class="document-icon" />
            <div class="document-details">
              <p class="document-name">{{ doc.documentName }}</p>
              <p class="document-meta">
                {{ doc.fileName }} • {{ formatFileSize(doc.fileSize) }} • 
                {{ doc.totalChunks }} {{ $t('penny.documentUpload.chunks') }}
              </p>
            </div>
          </div>
          <div class="document-status">
            <span :class="['status-badge', doc.status.toLowerCase()]">
              {{ doc.status }}
            </span>
          </div>
          <div class="document-actions">
            <button @click="deleteDocument(doc.id)" class="action-button delete">
              <Icon icon="mdi:delete" />
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
      required: true
    },
    tenantId: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      selectedFile: null,
      documentName: '',
      uploading: false,
      uploadProgress: 0,
      uploadStatus: null,
      isDragOver: false,
      documents: []
    };
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
      if (!this.selectedFile || !this.documentName) return;

      this.uploading = true;
      this.uploadProgress = 0;
      this.uploadStatus = null;

      try {
        const response = await pennyApi.uploadKnowledgeDocument(
          this.selectedFile,
          this.botId,
          this.tenantId,
          this.documentName,
          this.$store.state.user?.username
        );

        this.uploadProgress = 100;
        this.showStatus('success', 'mdi:check-circle', this.$t('penny.documentUpload.uploadSuccess'));
        
        this.removeFile();
        this.loadDocuments();
      } catch (error) {
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
