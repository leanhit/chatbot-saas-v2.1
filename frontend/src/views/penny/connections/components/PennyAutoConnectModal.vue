<template>
  <div class="penny-auto-connect-modal-backdrop" @click="closeOnBackdrop">
    <div class="penny-auto-connect-modal" @click.stop>
      <div class="modal-header">
        <div class="header-content">
          <div class="auto-connect-info">
            <Icon icon="mdi:facebook" class="h-6 w-6 mr-3 text-blue-600" />
            <div>
              <h2 class="modal-title">{{ $t('Auto Connect Facebook Pages') }}</h2>
              <p class="bot-name">{{ bot?.botName }}</p>
            </div>
          </div>
          <button @click="$emit('close')" class="close-button">
            <Icon icon="mdi:close" class="h-5 w-5" />
          </button>
        </div>
      </div>

      <div class="modal-body">
        <!-- Step 1: Facebook Login -->
        <div v-if="step === 'login'" class="login-step">
          <div class="login-content">
            <div class="login-icon">
              <Icon icon="mdi:facebook" class="h-16 w-16 text-blue-600" />
            </div>
            <h3 class="login-title">{{ $t('Connect to Facebook') }}</h3>
            <p class="login-description">
              {{ $t('We need to connect to your Facebook account to get your fanpages. This will allow us to automatically set up connections for your bot.') }}
            </p>
            <div class="permissions-info">
              <h4>{{ $t('Permissions Required:') }}</h4>
              <ul class="permissions-list">
                <li class="permission-item">
                  <Icon icon="mdi:check-circle" class="h-4 w-4 text-green-500 mr-2" />
                  {{ $t('View your Facebook pages') }}
                </li>
                <li class="permission-item">
                  <Icon icon="mdi:check-circle" class="h-4 w-4 text-green-500 mr-2" />
                  {{ $t('Manage pages and messaging') }}
                </li>
                <li class="permission-item">
                  <Icon icon="mdi:check-circle" class="h-4 w-4 text-green-500 mr-2" />
                  {{ $t('Read page engagement') }}
                </li>
              </ul>
            </div>
            <button
              @click="handleFacebookLogin"
              :disabled="loggingIn"
              class="login-button"
            >
              <Icon v-if="loggingIn" icon="mdi:loading" class="animate-spin h-5 w-5 mr-2" />
              <Icon v-else icon="mdi:facebook" class="h-5 w-5 mr-2" />
              {{ loggingIn ? $t('Connecting...') : $t('Connect to Facebook') }}
            </button>
          </div>
        </div>

        <!-- Step 2: Select Pages -->
        <div v-else-if="step === 'select'" class="select-step">
          <div class="select-header">
            <h3 class="select-title">{{ $t('Select Fanpages to Connect') }}</h3>
            <p class="select-description">
              {{ $t('We found {count} fanpages. Select which ones you want to connect to your bot.', { count: availablePages.length }) }}
            </p>
          </div>

          <div v-if="loadingPages" class="loading-pages">
            <div class="loading-spinner">
              <Icon icon="mdi:loading" class="animate-spin h-8 w-8 text-blue-600" />
              <p>{{ $t('Loading your fanpages...') }}</p>
            </div>
          </div>

          <div v-else-if="availablePages.length === 0" class="no-pages">
            <div class="empty-state">
              <Icon icon="mdi:facebook-off" class="h-12 w-12 text-gray-400 mb-4" />
              <h4>{{ $t('No Fanpages Found') }}</h4>
              <p>{{ $t('We couldn\'t find any fanpages for your account. Make sure you have admin access to Facebook pages.') }}</p>
              <button @click="handleRetry" class="retry-button">
                <Icon icon="mdi:refresh" class="h-4 w-4 mr-2" />
                {{ $t('Retry') }}
              </button>
            </div>
          </div>

          <div v-else class="pages-list">
            <div class="select-all-section">
              <label class="select-all-checkbox">
                <input
                  type="checkbox"
                  v-model="selectAll"
                  @change="toggleSelectAll"
                  class="checkbox-input"
                />
                <span class="checkbox-text">
                  {{ $t('Select All ({count} pages)', { count: availablePages.length }) }}
                </span>
              </label>
            </div>

            <div class="pages-grid">
              <div
                v-for="page in availablePages"
                :key="page.id"
                class="page-card"
                :class="{ 'page-card--selected': selectedPages.includes(page.id) }"
                @click="togglePageSelection(page)"
              >
                <div class="page-avatar">
                  <img
                    v-if="page.picture"
                    :src="page.picture"
                    :alt="page.name"
                    class="page-avatar-img"
                  />
                  <div
                    v-else
                    class="page-avatar-placeholder"
                    :style="{ backgroundColor: getPageColor(page) }"
                  >
                    {{ getPageInitial(page) }}
                  </div>
                </div>
                <div class="page-info">
                  <div class="page-name">{{ page.name }}</div>
                  <div class="page-category">{{ page.category || 'N/A' }}</div>
                  <div class="page-likes" v-if="page.likes">
                    <Icon icon="mdi:thumb-up" class="h-4 w-4 mr-1" />
                    {{ formatNumber(page.likes) }}
                  </div>
                </div>
                <div class="page-checkbox">
                  <div class="checkbox" :class="{ 'checkbox--checked': selectedPages.includes(page.id) }">
                    <Icon icon="mdi:check" class="h-4 w-4" />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Step 3: Processing -->
        <div v-else-if="step === 'processing'" class="processing-step">
          <div class="processing-content">
            <div class="processing-icon">
              <Icon icon="mdi:loading" class="animate-spin h-12 w-12 text-blue-600" />
            </div>
            <h3 class="processing-title">{{ $t('Auto-Connecting Pages') }}</h3>
            <p class="processing-description">
              {{ $t('We are connecting {count} selected fanpages to your bot. This may take a moment...', { count: selectedPages.length }) }}
            </p>
            <div class="processing-progress">
              <div class="progress-bar">
                <div
                  class="progress-fill"
                  :style="{ width: `${processingProgress}%` }"
                ></div>
              </div>
              <p class="progress-text">{{ processingProgress }}% {{ $t('Complete') }}</p>
            </div>
          </div>
        </div>

        <!-- Step 4: Results -->
        <div v-else-if="step === 'results'" class="results-step">
          <div class="results-content">
            <div class="results-icon" :class="results.success ? 'success' : 'error'">
              <Icon :icon="results.success ? 'mdi:check-circle' : 'mdi:alert-circle'" class="h-12 w-12" />
            </div>
            <h3 class="results-title">
              {{ results.success ? $t('Auto-Connect Completed!') : $t('Auto-Connect Completed with Issues') }}
            </h3>
            <p class="results-message">{{ results.message }}</p>

            <div class="results-details">
              <div v-if="results.connectedPages?.length > 0" class="result-section success">
                <h4 class="result-title">
                  <Icon icon="mdi:check-circle" class="h-5 w-5 text-green-500 mr-2" />
                  {{ $t('Connected Pages ({count})', { count: results.connectedPages.length }) }}
                </h4>
                <ul class="result-list">
                  <li v-for="page in results.connectedPages" :key="page" class="result-item">
                    {{ page }}
                  </li>
                </ul>
              </div>

              <div v-if="results.reactivatedPages?.length > 0" class="result-section warning">
                <h4 class="result-title">
                  <Icon icon="mdi:refresh" class="h-5 w-5 text-yellow-500 mr-2" />
                  {{ $t('Reactivated Pages ({count})', { count: results.reactivatedPages.length }) }}
                </h4>
                <ul class="result-list">
                  <li v-for="page in results.reactivatedPages" :key="page" class="result-item">
                    {{ page }}
                  </li>
                </ul>
              </div>

              <div v-if="results.inactivePages?.length > 0" class="result-section info">
                <h4 class="result-title">
                  <Icon icon="mdi:pause-circle" class="h-5 w-5 text-gray-500 mr-2" />
                  {{ $t('Inactive Pages ({count})', { count: results.inactivePages.length }) }}
                </h4>
                <ul class="result-list">
                  <li v-for="page in results.inactivePages" :key="page" class="result-item">
                    {{ page }}
                  </li>
                </ul>
              </div>

              <div v-if="results.errors?.length > 0" class="result-section error">
                <h4 class="result-title">
                  <Icon icon="mdi:alert-circle" class="h-5 w-5 text-red-500 mr-2" />
                  {{ $t('Errors ({count})', { count: results.errors.length }) }}
                </h4>
                <ul class="result-list">
                  <li v-for="error in results.errors" :key="error.pageName" class="result-item error-item">
                    <strong>{{ error.pageName }}:</strong> {{ error.error }}
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="modal-footer">
        <button
          v-if="step === 'login'"
          type="button"
          @click="$emit('close')"
          class="btn btn-secondary"
        >
          {{ $t('Cancel') }}
        </button>

        <button
          v-if="step === 'select'"
          type="button"
          @click="handleBack"
          class="btn btn-secondary"
        >
          {{ $t('Back') }}
        </button>

        <button
          v-if="step === 'select'"
          type="button"
          @click="handleAutoConnect"
          :disabled="selectedPages.length === 0 || processing"
          class="btn btn-primary"
        >
          <Icon v-if="processing" icon="mdi:loading" class="animate-spin h-4 w-4 mr-2" />
          {{ processing ? $t('Processing...') : $t('Connect {count} Pages', { count: selectedPages.length }) }}
        </button>

        <button
          v-if="step === 'results'"
          type="button"
          @click="$emit('close')"
          class="btn btn-primary"
        >
          {{ $t('Done') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import { useI18n } from 'vue-i18n'
import { pennyConnectionApi } from '@/api/pennyConnectionApi'

export default {
  name: 'PennyAutoConnectModal',
  components: {
    Icon
  },
  props: {
    bot: {
      type: Object,
      required: true
    }
  },
  emits: ['close', 'connected'],
  setup(props, { emit }) {
    const { t } = useI18n()

    // State
    const step = ref('login')
    const loggingIn = ref(false)
    const loadingPages = ref(false)
    const processing = ref(false)
    const processingProgress = ref(0)
    const availablePages = ref([])
    const selectedPages = ref([])
    const selectAll = ref(false)
    const results = ref({})
    const userAccessToken = ref(null)

    // Computed
    const canProceed = computed(() => {
      if (step.value === 'login') return !loggingIn.value
      if (step.value === 'select') return selectedPages.value.length > 0 && !processing.value
      return true
    })

    // Methods
    const handleFacebookLogin = async () => {
      if (typeof window.FB === 'undefined') {
        alert('Facebook SDK is not loaded. Please refresh the page and try again.')
        return
      }

      loggingIn.value = true
      try {
        const permissions = [
          'public_profile',
          'email',
          'pages_messaging',
          'pages_show_list',
          'pages_read_engagement',
          'pages_manage_posts'
        ]

        window.FB.login(
          (response) => {
            if (response.authResponse) {
              userAccessToken.value = response.authResponse.accessToken
              step.value = 'select'
              loadFacebookPages()
            } else {
              alert('Facebook login failed. Please try again.')
            }
            loggingIn.value = false
          },
          {
            scope: permissions.join(',')
          }
        )
      } catch (error) {
        console.error('Facebook login error:', error)
        alert('Failed to connect to Facebook. Please try again.')
        loggingIn.value = false
      }
    }

    const loadFacebookPages = async () => {
      loadingPages.value = true
      try {
        const response = await fetch(`https://graph.facebook.com/v18.0/me/accounts?access_token=${userAccessToken.value}`)
        const data = await response.json()
        
        if (data.data) {
          availablePages.value = data.data.map(page => ({
            id: page.id,
            name: page.name,
            category: page.category,
            picture: page.picture?.data?.url,
            likes: page.fan_count,
            access_token: page.access_token
          }))
        } else {
          console.error('No pages found:', data)
        }
      } catch (error) {
        console.error('Failed to load Facebook pages:', error)
        alert('Failed to load your Facebook pages. Please try again.')
      } finally {
        loadingPages.value = false
      }
    }

    const togglePageSelection = (page) => {
      const index = selectedPages.value.indexOf(page.id)
      if (index > -1) {
        selectedPages.value.splice(index, 1)
      } else {
        selectedPages.value.push(page.id)
      }
      selectAll.value = selectedPages.value.length === availablePages.value.length
    }

    const toggleSelectAll = () => {
      if (selectAll.value) {
        selectedPages.value = availablePages.value.map(page => page.id)
      } else {
        selectedPages.value = []
      }
    }

    const handleBack = () => {
      step.value = 'login'
      userAccessToken.value = null
      availablePages.value = []
      selectedPages.value = []
      selectAll.value = false
    }

    const handleAutoConnect = async () => {
      processing.value = true
      step.value = 'processing'
      processingProgress.value = 0

      try {
        // Simulate progress
        const progressInterval = setInterval(() => {
          if (processingProgress.value < 90) {
            processingProgress.value += 10
          }
        }, 200)

        // Prepare selected pages data for client-side API
        const selectedPagesData = availablePages.value
          .filter(page => selectedPages.value.includes(page.id))
          .map(page => ({
            botId: props.bot.id.toString(),
            botName: page.name,
            pageId: page.id,
            fanpageUrl: page.fanpageUrl || `https://www.facebook.com/${page.id}`,
            pageAccessToken: page.access_token,
            isEnabled: true
          }))

        const payload = {
          connections: selectedPagesData
        }

        const response = await pennyConnectionApi.autoConnectFacebookClient(payload)
        results.value = {
          success: true,
          message: `Successfully connected ${selectedPagesData.length} pages to your bot.`,
          connectedPages: selectedPagesData.map(page => page.botName),
          reactivatedPages: [],
          inactivePages: [],
          errors: []
        }

        // Complete progress
        clearInterval(progressInterval)
        processingProgress.value = 100

        setTimeout(() => {
          step.value = 'results'
          processing.value = false
          emit('connected', results.value)
        }, 1000)

      } catch (error) {
        console.error('Auto-connect failed:', error)
        results.value = {
          success: false,
          message: 'Failed to auto-connect pages: ' + (error.response?.data?.message || error.message),
          connectedPages: [],
          reactivatedPages: [],
          inactivePages: [],
          errors: [{ pageName: 'System', error: error.message }]
        }
        step.value = 'results'
        processing.value = false
      }
    }

    const handleRetry = () => {
      loadFacebookPages()
    }

    const getPageInitial = (page) => {
      return page.name.charAt(0).toUpperCase()
    }

    const getPageColor = (page) => {
      const colors = ['#1877F2', '#42A5F5', '#42A5F5', '#1877F2', '#42A5F5', '#1877F2']
      const index = page.id ? page.id.toString().charCodeAt(0) : 0
      return colors[index % colors.length]
    }

    const formatNumber = (num) => {
      if (num >= 1000000) return (num / 1000000).toFixed(1) + 'M'
      if (num >= 1000) return (num / 1000).toFixed(1) + 'K'
      return num.toString()
    }

    const closeOnBackdrop = (event) => {
      if (event.target === event.currentTarget) {
        emit('close')
      }
    }

    // Initialize Facebook SDK
    onMounted(() => {
      // Facebook SDK should already be loaded from index.html
      // We just need to make sure it's available
      if (typeof window.FB === 'undefined') {
        console.warn('Facebook SDK not loaded. Please check index.html.')
      }
    })

    return {
      step,
      loggingIn,
      loadingPages,
      processing,
      processingProgress,
      availablePages,
      selectedPages,
      selectAll,
      results,
      canProceed,
      handleFacebookLogin,
      togglePageSelection,
      toggleSelectAll,
      handleBack,
      handleAutoConnect,
      handleRetry,
      getPageInitial,
      getPageColor,
      formatNumber,
      closeOnBackdrop
    }
  }
}
</script>

<style scoped>
.penny-auto-connect-modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.penny-auto-connect-modal {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  max-width: 600px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
}

.dark .penny-auto-connect-modal {
  background: #1f2937;
}

.modal-header {
  padding: 24px;
  border-bottom: 1px solid #e5e7eb;
}

.dark .modal-header {
  border-bottom-color: #374151;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.auto-connect-info {
  display: flex;
  align-items: center;
  flex: 1;
}

.modal-title {
  margin: 0 0 4px 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.dark .modal-title {
  color: white;
}

.bot-name {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
}

.dark .bot-name {
  color: #9ca3af;
}

.close-button {
  background: none;
  border: none;
  padding: 8px;
  border-radius: 6px;
  cursor: pointer;
  color: #6b7280;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.close-button:hover {
  background-color: #f3f4f6;
  color: #1f2937;
}

.dark .close-button {
  color: #9ca3af;
}

.dark .close-button:hover {
  background-color: #374151;
  color: white;
}

.modal-body {
  padding: 24px;
  min-height: 400px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 24px;
  border-top: 1px solid #e5e7eb;
}

.dark .modal-footer {
  border-top-color: #374151;
}

.btn {
  padding: 10px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
  display: inline-flex;
  align-items: center;
}

.btn-secondary {
  background: #f3f4f6;
  color: #374151;
}

.btn-secondary:hover:not(:disabled) {
  background: #e5e7eb;
}

.dark .btn-secondary {
  background: #374151;
  color: #d1d5db;
}

.dark .btn-secondary:hover:not(:disabled) {
  background: #4b5563;
}

.btn-primary {
  background: #3b82f6;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #2563eb;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Login Step */
.login-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 40px 20px;
}

.login-icon {
  margin-bottom: 24px;
}

.login-title {
  margin: 0 0 16px 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.dark .login-title {
  color: white;
}

.login-description {
  margin: 0 0 32px 0;
  color: #6b7280;
  line-height: 1.6;
  max-width: 400px;
}

.dark .login-description {
  color: #9ca3af;
}

.permissions-info {
  margin-bottom: 32px;
  text-align: left;
  background: #f9fafb;
  padding: 20px;
  border-radius: 8px;
  max-width: 400px;
}

.dark .permissions-info {
  background: #374151;
}

.permissions-info h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.dark .permissions-info h4 {
  color: white;
}

.permissions-list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.permission-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  color: #374151;
}

.dark .permission-item {
  color: #d1d5db;
}

.login-button {
  padding: 12px 24px;
  background: #1877f2;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
}

.login-button:hover:not(:disabled) {
  background: #1665c7;
}

/* Select Step */
.select-step {
  padding: 20px 0;
}

.select-header {
  text-align: center;
  margin-bottom: 24px;
}

.select-title {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.dark .select-title {
  color: white;
}

.select-description {
  margin: 0;
  color: #6b7280;
}

.dark .select-description {
  color: #9ca3af;
}

.loading-pages {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px;
}

.loading-spinner {
  text-align: center;
}

.loading-spinner p {
  margin-top: 16px;
  color: #6b7280;
}

.dark .loading-spinner p {
  color: #9ca3af;
}

.no-pages {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px;
  text-align: center;
}

.empty-state {
  text-align: center;
}

.empty-state h4 {
  margin: 16px 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #374151;
}

.dark .empty-state h4 {
  color: #d1d5db;
}

.empty-state p {
  margin: 0 0 24px 0;
  color: #6b7280;
  max-width: 300px;
}

.dark .empty-state p {
  color: #9ca3af;
}

.retry-button {
  padding: 10px 16px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
}

.retry-button:hover {
  background: #2563eb;
}

.select-all-section {
  margin-bottom: 20px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
}

.dark .select-all-section {
  background: #374151;
}

.select-all-checkbox {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.checkbox-input {
  margin-right: 8px;
}

.checkbox-text {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.dark .checkbox-text {
  color: #d1d5db;
}

.pages-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.page-card {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.dark .page-card {
  border-color: #4b5563;
}

.page-card:hover {
  border-color: #3b82f6;
}

.page-card--selected {
  border-color: #3b82f6;
  background: #eff6ff;
}

.dark .page-card--selected {
  background: #1e3a8a;
}

.page-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 12px;
  flex-shrink: 0;
}

.page-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.page-avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.page-info {
  flex: 1;
  min-width: 0;
}

.page-name {
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dark .page-name {
  color: white;
}

.page-category {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 4px;
}

.dark .page-category {
  color: #9ca3af;
}

.page-likes {
  font-size: 12px;
  color: #6b7280;
  display: flex;
  align-items: center;
}

.dark .page-likes {
  color: #9ca3af;
}

.page-checkbox {
  margin-left: 12px;
}

.checkbox {
  width: 24px;
  height: 24px;
  border: 2px solid #d1d5db;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  transition: all 0.2s;
}

.dark .checkbox {
  border-color: #4b5563;
  background: #1f2937;
}

.checkbox--checked {
  border-color: #3b82f6;
  background: #3b82f6;
  color: white;
}

/* Processing Step */
.processing-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 40px 20px;
}

.processing-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.processing-icon {
  margin-bottom: 24px;
}

.processing-title {
  margin: 0 0 16px 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.dark .processing-title {
  color: white;
}

.processing-description {
  margin: 0 0 32px 0;
  color: #6b7280;
  max-width: 400px;
  line-height: 1.6;
}

.dark .processing-description {
  color: #9ca3af;
}

.processing-progress {
  width: 100%;
  max-width: 300px;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 8px;
}

.dark .progress-bar {
  background: #374151;
}

.progress-fill {
  height: 100%;
  background: #3b82f6;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 14px;
  color: #6b7280;
}

.dark .progress-text {
  color: #9ca3af;
}

/* Results Step */
.results-step {
  padding: 20px 0;
}

.results-content {
  text-align: center;
}

.results-icon {
  margin-bottom: 24px;
}

.results-icon.success {
  color: #10b981;
}

.results-icon.error {
  color: #ef4444;
}

.results-title {
  margin: 0 0 16px 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.dark .results-title {
  color: white;
}

.results-message {
  margin: 0 0 32px 0;
  color: #6b7280;
  line-height: 1.6;
}

.dark .results-message {
  color: #9ca3af;
}

.results-details {
  text-align: left;
  max-width: 500px;
  margin: 0 auto;
}

.result-section {
  margin-bottom: 24px;
  padding: 16px;
  border-radius: 8px;
}

.result-section.success {
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
}

.result-section.warning {
  background: #fffbeb;
  border: 1px solid #fed7aa;
}

.result-section.info {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.result-section.error {
  background: #fef2f2;
  border: 1px solid #fecaca;
}

.result-title {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
}

.result-list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.result-item {
  padding: 4px 0;
  color: #374151;
}

.dark .result-item {
  color: #d1d5db;
}

.error-item {
  color: #dc2626;
}

.dark .error-item {
  color: #ef4444;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.animate-spin {
  animation: spin 1s linear infinite;
}
</style>
