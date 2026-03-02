<template>
  <div class="penny-connection-modal-backdrop" @click="closeOnBackdrop">
    <div class="penny-connection-modal" @click.stop>
      <div class="modal-header">
        <div class="header-content">
          <div class="connection-info">
            <Icon icon="mdi:connection" class="h-6 w-6 mr-3" />
            <div>
              <h2 class="modal-title">{{ isEditMode ? $t('Edit Connection') : $t('Create Connection') }}</h2>
              <p class="bot-name">{{ bot?.botName }}</p>
            </div>
          </div>
          <button @click="$emit('close')" class="close-button">
            <Icon icon="mdi:close" class="h-5 w-5" />
          </button>
        </div>
      </div>

      <div class="modal-body">
        <form @submit.prevent="handleSubmit" class="connection-form">
          <!-- Basic Information -->
          <div class="form-section">
            <h3 class="section-title">{{ $t('Basic Information') }}</h3>
            <div class="form-grid">
              <div class="form-group full-width">
                <label for="connectionName" class="form-label">
                  {{ $t('Connection Name') }} <span class="required">*</span>
                </label>
                <input
                  id="connectionName"
                  v-model="formData.connectionName"
                  type="text"
                  class="form-input"
                  :placeholder="$t('Enter connection name')"
                  required
                />
              </div>
              <div class="form-group">
                <label for="connectionType" class="form-label">
                  {{ $t('Connection Type') }} <span class="required">*</span>
                </label>
                <select
                  id="connectionType"
                  v-model="formData.connectionType"
                  class="form-select"
                  @change="onConnectionTypeChange"
                  required
                >
                  <option value="" disabled>{{ $t('Select connection type') }}</option>
                  <option
                    v-for="type in connectionTypes"
                    :key="type.value"
                    :value="type.value"
                  >
                    {{ type.label }}
                  </option>
                </select>
              </div>
              <div class="form-group">
                <label for="priority" class="form-label">
                  {{ $t('Priority') }}
                </label>
                <input
                  id="priority"
                  v-model.number="formData.priority"
                  type="number"
                  min="0"
                  max="100"
                  class="form-input"
                />
                <p class="form-help">{{ $t('Higher priority connections are checked first') }}</p>
              </div>
            </div>
            <div class="form-group full-width">
              <label for="description" class="form-label">
                {{ $t('Description') }}
              </label>
              <textarea
                id="description"
                v-model="formData.description"
                rows="2"
                class="form-textarea"
                :placeholder="$t('Describe what this connection does')"
              ></textarea>
            </div>
          </div>

          <!-- Connection Configuration -->
          <div class="form-section">
            <h3 class="section-title">{{ $t('Connection Configuration') }}</h3>
            
            <!-- Facebook Configuration -->
            <div v-if="formData.connectionType === 'FACEBOOK'" class="connection-config">
              <div class="form-grid">
                <div class="form-group">
                  <label for="pageId" class="form-label">
                    {{ $t('Page ID') }} <span class="required">*</span>
                  </label>
                  <input
                    id="pageId"
                    v-model="formData.pageId"
                    type="text"
                    class="form-input"
                    :placeholder="$t('Enter Facebook Page ID')"
                    required
                  />
                </div>
                <div class="form-group">
                  <label for="fanpageUrl" class="form-label">
                    {{ $t('Fanpage URL') }}
                  </label>
                  <input
                    id="fanpageUrl"
                    v-model="formData.fanpageUrl"
                    type="url"
                    class="form-input"
                    :placeholder="$t('Enter fanpage URL')"
                  />
                  <div v-if="formData.fanpageUrl" class="form-actions">
                    <a
                      :href="formData.fanpageUrl"
                      target="_blank"
                      class="external-link"
                    >
                      <Icon icon="mdi:external-link" class="h-4 w-4 mr-1" />
                      {{ $t('Go to page') }}
                    </a>
                  </div>
                </div>
              </div>
              <div class="form-grid">
                <div class="form-group">
                  <label for="appSecret" class="form-label">
                    {{ $t('App Secret') }} <span class="required">*</span>
                  </label>
                  <div class="input-with-copy">
                    <input
                      id="appSecret"
                      v-model="formData.appSecret"
                      type="password"
                      class="form-input"
                      :placeholder="$t('Enter Facebook App Secret')"
                      required
                    />
                    <button
                      type="button"
                      @click="copyToClipboard(formData.appSecret, 'appSecret')"
                      class="copy-button"
                    >
                      <Icon icon="mdi:content-copy" class="h-4 w-4" />
                    </button>
                  </div>
                </div>
                <div class="form-group">
                  <label for="pageAccessToken" class="form-label">
                    {{ $t('Page Access Token') }} <span class="required">*</span>
                  </label>
                  <div class="input-with-copy">
                    <input
                      id="pageAccessToken"
                      v-model="formData.pageAccessToken"
                      type="password"
                      class="form-input"
                      :placeholder="$t('Enter Page Access Token')"
                      required
                    />
                    <button
                      type="button"
                      @click="copyToClipboard(formData.pageAccessToken, 'pageAccessToken')"
                      class="copy-button"
                    >
                      <Icon icon="mdi:content-copy" class="h-4 w-4" />
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Webhook Configuration -->
            <div v-else-if="formData.connectionType === 'WEBHOOK'" class="connection-config">
              <div class="form-group full-width">
                <label for="webhookUrl" class="form-label">
                  {{ $t('Webhook URL') }} <span class="required">*</span>
                </label>
                <input
                  id="webhookUrl"
                  v-model="formData.webhookUrl"
                  type="url"
                  class="form-input"
                  :placeholder="$t('Enter webhook URL')"
                  required
                />
              </div>
              <div class="form-grid">
                <div class="form-group">
                  <label for="webhookMethod" class="form-label">
                    {{ $t('HTTP Method') }}
                  </label>
                  <select
                    id="webhookMethod"
                    v-model="formData.webhookMethod"
                    class="form-select"
                  >
                    <option value="POST">POST</option>
                    <option value="GET">GET</option>
                    <option value="PUT">PUT</option>
                    <option value="DELETE">DELETE</option>
                  </select>
                </div>
                <div class="form-group">
                  <label for="webhookTimeout" class="form-label">
                    {{ $t('Timeout (seconds)') }}
                  </label>
                  <input
                    id="webhookTimeout"
                    v-model.number="formData.webhookTimeout"
                    type="number"
                    min="1"
                    max="60"
                    class="form-input"
                  />
                </div>
              </div>
              <div class="form-group full-width">
                <label for="webhookHeaders" class="form-label">
                  {{ $t('Headers (JSON)') }}
                </label>
                <textarea
                  id="webhookHeaders"
                  v-model="formData.webhookHeaders"
                  rows="3"
                  class="form-textarea"
                  :placeholder="$t('Enter webhook headers as JSON')"
                ></textarea>
                <p class="form-help">{{ $t('Example: {\"Authorization\": \"Bearer token\"}') }}</p>
              </div>
            </div>

            <!-- API Configuration -->
            <div v-else-if="formData.connectionType === 'API'" class="connection-config">
              <div class="form-group full-width">
                <label for="apiUrl" class="form-label">
                  {{ $t('API URL') }} <span class="required">*</span>
                </label>
                <input
                  id="apiUrl"
                  v-model="formData.apiUrl"
                  type="url"
                  class="form-input"
                  :placeholder="$t('Enter API endpoint URL')"
                  required
                />
              </div>
              <div class="form-grid">
                <div class="form-group">
                  <label for="apiKey" class="form-label">
                    {{ $t('API Key') }}
                  </label>
                  <div class="input-with-copy">
                    <input
                      id="apiKey"
                      v-model="formData.apiKey"
                      type="password"
                      class="form-input"
                      :placeholder="$t('Enter API key')"
                    />
                    <button
                      type="button"
                      @click="copyToClipboard(formData.apiKey, 'apiKey')"
                      class="copy-button"
                    >
                      <Icon icon="mdi:content-copy" class="h-4 w-4" />
                    </button>
                  </div>
                </div>
                <div class="form-group">
                  <label for="apiVersion" class="form-label">
                    {{ $t('API Version') }}
                  </label>
                  <input
                    id="apiVersion"
                    v-model="formData.apiVersion"
                    type="text"
                    class="form-input"
                    :placeholder="$t('Enter API version')"
                  />
                </div>
              </div>
            </div>

            <!-- Database Configuration -->
            <div v-else-if="formData.connectionType === 'DATABASE'" class="connection-config">
              <div class="form-grid">
                <div class="form-group">
                  <label for="dbHost" class="form-label">
                    {{ $t('Database Host') }} <span class="required">*</span>
                  </label>
                  <input
                    id="dbHost"
                    v-model="formData.dbHost"
                    type="text"
                    class="form-input"
                    :placeholder="$t('Enter database host')"
                    required
                  />
                </div>
                <div class="form-group">
                  <label for="dbPort" class="form-label">
                    {{ $t('Database Port') }}
                  </label>
                  <input
                    id="dbPort"
                    v-model.number="formData.dbPort"
                    type="number"
                    min="1"
                    max="65535"
                    class="form-input"
                    :placeholder="$t('Enter database port')"
                  />
                </div>
              </div>
              <div class="form-grid">
                <div class="form-group">
                  <label for="dbName" class="form-label">
                    {{ $t('Database Name') }} <span class="required">*</span>
                  </label>
                  <input
                    id="dbName"
                    v-model="formData.dbName"
                    type="text"
                    class="form-input"
                    :placeholder="$t('Enter database name')"
                    required
                  />
                </div>
                <div class="form-group">
                  <label for="dbUsername" class="form-label">
                    {{ $t('Username') }} <span class="required">*</span>
                  </label>
                  <input
                    id="dbUsername"
                    v-model="formData.dbUsername"
                    type="text"
                    class="form-input"
                    :placeholder="$t('Enter database username')"
                    required
                  />
                </div>
              </div>
              <div class="form-group">
                <label for="dbPassword" class="form-label">
                  {{ $t('Password') }} <span class="required">*</span>
                </label>
                <div class="input-with-copy">
                  <input
                    id="dbPassword"
                    v-model="formData.dbPassword"
                    type="password"
                    class="form-input"
                    :placeholder="$t('Enter database password')"
                    required
                  />
                  <button
                    type="button"
                    @click="copyToClipboard(formData.dbPassword, 'dbPassword')"
                    class="copy-button"
                  >
                    <Icon icon="mdi:content-copy" class="h-4 w-4" />
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Status (edit mode only) -->
          <div v-if="isEditMode" class="form-section">
            <h3 class="section-title">{{ $t('Status') }}</h3>
            <div class="form-group">
              <label class="checkbox-label">
                <input
                  type="checkbox"
                  v-model="formData.isEnabled"
                  class="checkbox-input"
                />
                <span class="checkbox-text">{{ formData.isEnabled ? $t('Enabled') : $t('Disabled') }}</span>
              </label>
            </div>
          </div>
        </form>
      </div>

      <div class="modal-footer">
        <button
          type="button"
          @click="$emit('close')"
          class="btn btn-secondary"
        >
          {{ $t('Cancel') }}
        </button>
        <button
          type="button"
          @click="testConnection"
          :disabled="!canTestConnection"
          class="btn btn-outline"
        >
          <Icon icon="mdi:test-tube" class="h-4 w-4 mr-2" />
          {{ $t('Test Connection') }}
        </button>
        <button
          type="submit"
          @click="handleSubmit"
          :disabled="submitting"
          class="btn btn-primary"
        >
          <Icon v-if="submitting" icon="mdi:loading" class="animate-spin h-4 w-4 mr-2" />
          <Icon v-else icon="mdi:plus" class="h-4 w-4 mr-2" />
          {{ isEditMode ? $t('Update Connection') : $t('Create Connection') }}
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
  name: 'PennyConnectionModal',
  components: {
    Icon
  },
  props: {
    bot: {
      type: Object,
      required: true
    },
    connection: {
      type: Object,
      default: null
    }
  },
  emits: ['close', 'saved'],
  setup(props, { emit }) {
    const { t } = useI18n()
    const submitting = ref(false)
    const copiedKey = ref(null)

    const formData = ref({
      connectionName: '',
      description: '',
      connectionType: '',
      priority: 0,
      isEnabled: true,
      // Facebook fields
      pageId: '',
      fanpageUrl: '',
      appSecret: '',
      pageAccessToken: '',
      verifyToken: '',
      urlCallback: 'https://chat.truyenthongviet.vn/webhooks/facebook/botpress',
      // Webhook fields
      webhookUrl: '',
      webhookMethod: 'POST',
      webhookTimeout: 30,
      webhookHeaders: '',
      // API fields
      apiUrl: '',
      apiKey: '',
      apiVersion: '',
      // Database fields
      dbHost: '',
      dbPort: 5432,
      dbName: '',
      dbUsername: '',
      dbPassword: ''
    })

    const connectionTypes = [
      { value: 'FACEBOOK', label: 'Facebook Messenger' },
      { value: 'WEBHOOK', label: 'Webhook' },
      { value: 'API', label: 'REST API' },
      { value: 'DATABASE', label: 'Database' }
    ]

    const isEditMode = computed(() => !!props.connection)

    const canTestConnection = computed(() => {
      return formData.value.connectionName && 
             formData.value.connectionType &&
             isConnectionConfigValid()
    })

    const isConnectionConfigValid = () => {
      switch (formData.value.connectionType) {
        case 'FACEBOOK':
          return formData.value.pageId && formData.value.appSecret && formData.value.pageAccessToken
        case 'WEBHOOK':
          return formData.value.webhookUrl
        case 'API':
          return formData.value.apiUrl
        case 'DATABASE':
          return formData.value.dbHost && formData.value.dbName && formData.value.dbUsername && formData.value.dbPassword
        default:
          return false
      }
    }

    const onConnectionTypeChange = () => {
      // Reset connection-specific fields when type changes
      Object.keys(formData.value).forEach(key => {
        if (key.startsWith('fb') || key.startsWith('webhook') || key.startsWith('api') || key.startsWith('db')) {
          formData.value[key] = ''
        }
      })
      
      // Set defaults for specific types
      if (formData.value.connectionType === 'WEBHOOK') {
        formData.value.webhookMethod = 'POST'
        formData.value.webhookTimeout = 30
      } else if (formData.value.connectionType === 'DATABASE') {
        formData.value.dbPort = 5432
      }
    }

    const copyToClipboard = async (text, fieldName) => {
      try {
        await navigator.clipboard.writeText(text)
        copiedKey.value = fieldName
        setTimeout(() => {
          copiedKey.value = null
        }, 2000)
      } catch (error) {
        console.error('Failed to copy to clipboard:', error)
      }
    }

    const handleSubmit = async () => {
      if (!formData.value.connectionName || !formData.value.connectionType) {
        return
      }

      submitting.value = true
      try {
        // For Facebook connections, use the old structure from frontend-old
        if (formData.value.connectionType === 'FACEBOOK') {
          const connectionData = {
            connectionName: formData.value.connectionName,
            connectionType: formData.value.connectionType,
            description: formData.value.description,
            priority: formData.value.priority || 0,
            isEnabled: formData.value.isEnabled,
            // Facebook specific fields (matching frontend-old)
            pageId: formData.value.pageId,
            fanpageUrl: formData.value.fanpageUrl,
            appSecret: formData.value.appSecret,
            pageAccessToken: formData.value.pageAccessToken,
            verifyToken: formData.value.verifyToken,
            urlCallback: formData.value.urlCallback || 'https://chat.truyenthongviet.vn/webhooks/facebook/botpress'
          }

          if (isEditMode.value) {
            // Update existing Facebook connection
            await pennyConnectionApi.updateConnection(props.bot.id, props.connection.id, connectionData)
            console.log('Facebook connection updated successfully')
          } else {
            // Create new Facebook connection
            await pennyConnectionApi.createConnection(props.bot.id, connectionData)
            console.log('Facebook connection created successfully')
          }
        } else {
          // For other connection types, use generic structure
          const connectionData = {
            connectionName: formData.value.connectionName,
            connectionType: formData.value.connectionType,
            description: formData.value.description,
            priority: formData.value.priority || 0,
            isEnabled: formData.value.isEnabled,
            // Type-specific fields
            ...(formData.value.connectionType === 'WEBHOOK' && {
              webhookUrl: formData.value.webhookUrl,
              webhookMethod: formData.value.webhookMethod,
              webhookTimeout: formData.value.webhookTimeout,
              webhookHeaders: formData.value.webhookHeaders
            }),
            ...(formData.value.connectionType === 'API' && {
              apiUrl: formData.value.apiUrl,
              apiKey: formData.value.apiKey,
              apiVersion: formData.value.apiVersion
            }),
            ...(formData.value.connectionType === 'DATABASE' && {
              dbHost: formData.value.dbHost,
              dbPort: formData.value.dbPort,
              dbName: formData.value.dbName,
              dbUsername: formData.value.dbUsername,
              dbPassword: formData.value.dbPassword
            })
          }

          if (isEditMode.value) {
            // Update existing connection
            await pennyConnectionApi.updateConnection(props.bot.id, props.connection.id, connectionData)
            console.log('Connection updated successfully')
          } else {
            // Create new connection
            await pennyConnectionApi.createConnection(props.bot.id, connectionData)
            console.log('Connection created successfully')
          }
        }
        
        emit('saved')
      } catch (error) {
        console.error('Failed to save connection:', error)
        alert('Failed to save connection: ' + (error.response?.data?.error || error.message))
      } finally {
        submitting.value = false
      }
    }

    const testConnection = async () => {
      if (!canTestConnection.value) return
      
      try {
        // For Facebook connections, use the old testing method from frontend-old
        if (formData.value.connectionType === 'FACEBOOK') {
          const testData = {
            pageId: formData.value.pageId,
            pageAccessToken: formData.value.pageAccessToken
          }

          let response
          if (isEditMode.value && props.connection) {
            // Test existing connection
            response = await pennyConnectionApi.testConnection(props.bot.id, props.connection.id, testData)
          } else {
            // For new connections, create a temporary connection for testing
            const tempConnectionData = {
              connectionName: `TEST_${Date.now()}`,
              connectionType: 'FACEBOOK',
              pageId: formData.value.pageId,
              pageAccessToken: formData.value.pageAccessToken,
              appSecret: formData.value.appSecret,
              verifyToken: formData.value.verifyToken,
              urlCallback: formData.value.urlCallback,
              isEnabled: true
            }
            
            const createdConnection = await pennyConnectionApi.createConnection(props.bot.id, tempConnectionData)
            response = await pennyConnectionApi.testConnection(props.bot.id, createdConnection.data.connectionId, testData)
            await pennyConnectionApi.deleteConnection(props.bot.id, createdConnection.data.connectionId)
          }

          console.log('Facebook connection test result:', response.data)
          
          if (response.data.success) {
            alert(`✅ Facebook connection test successful!\n\nConnection: ${formData.value.connectionName}\nPage ID: ${formData.value.pageId}\nResponse: ${response.data.message}`)
          } else {
            alert(`❌ Facebook connection test failed.\n\nError: ${response.data.error}`)
          }
        } else {
          // For other connection types, use generic testing
          const testData = {
            connectionType: formData.value.connectionType,
            config: getConnectionConfig()
          }

          let response
          if (isEditMode.value && props.connection) {
            // Test existing connection
            response = await pennyConnectionApi.testConnection(props.bot.id, props.connection.id, testData)
          } else {
            // For new connections, create a temporary connection for testing
            const tempConnectionData = {
              connectionName: `TEST_${Date.now()}`,
              connectionType: formData.value.connectionType,
              ...getConnectionConfig()
            }
            
            const createdConnection = await pennyConnectionApi.createConnection(props.bot.id, tempConnectionData)
            response = await pennyConnectionApi.testConnection(props.bot.id, createdConnection.data.connectionId, testData)
            await pennyConnectionApi.deleteConnection(props.bot.id, createdConnection.data.connectionId)
          }

          console.log('Connection test result:', response.data)
          
          if (response.data.success) {
            alert(`✅ Connection test successful!\n\nConnection: ${formData.value.connectionName}\nType: ${formData.value.connectionType}\nResponse: ${response.data.message}`)
          } else {
            alert(`❌ Connection test failed.\n\nError: ${response.data.error}`)
          }
        }
      } catch (error) {
        console.error('Failed to test connection:', error)
        alert('Failed to test connection: ' + (error.response?.data?.error || error.message))
      }
    }

    const getConnectionConfig = () => {
      const config = {}
      switch (formData.value.connectionType) {
        case 'FACEBOOK':
          return {
            pageId: formData.value.pageId,
            appSecret: formData.value.appSecret,
            pageAccessToken: formData.value.pageAccessToken,
            verifyToken: formData.value.verifyToken,
            urlCallback: formData.value.urlCallback
          }
        case 'WEBHOOK':
          return {
            url: formData.value.webhookUrl,
            method: formData.value.webhookMethod,
            timeout: formData.value.webhookTimeout,
            headers: formData.value.webhookHeaders ? JSON.parse(formData.value.webhookHeaders) : {}
          }
        case 'API':
          return {
            url: formData.value.apiUrl,
            key: formData.value.apiKey,
            version: formData.value.apiVersion
          }
        case 'DATABASE':
          return {
            host: formData.value.dbHost,
            port: formData.value.dbPort,
            database: formData.value.dbName,
            username: formData.value.dbUsername,
            password: formData.value.dbPassword
          }
        default:
          return {}
      }
    }

    const closeOnBackdrop = (event) => {
      if (event.target === event.currentTarget) {
        emit('close')
      }
    }

    // Initialize form data
    onMounted(() => {
      if (props.connection) {
        // Edit mode - populate with existing data
        Object.assign(formData.value, props.connection)
      } else {
        // Create mode - set defaults
        formData.value = {
          connectionName: '',
          description: '',
          connectionType: '',
          priority: 0,
          isEnabled: true,
          pageId: '',
          fanpageUrl: '',
          appSecret: '',
          pageAccessToken: '',
          verifyToken: '',
          urlCallback: 'https://chat.truyenthongviet.vn/webhooks/facebook/botpress',
          webhookUrl: '',
          webhookMethod: 'POST',
          webhookTimeout: 30,
          webhookHeaders: '',
          apiUrl: '',
          apiKey: '',
          apiVersion: '',
          dbHost: '',
          dbPort: 5432,
          dbName: '',
          dbUsername: '',
          dbPassword: ''
        }
      }
    })

    return {
      formData,
      submitting,
      copiedKey,
      isEditMode,
      canTestConnection,
      connectionTypes,
      onConnectionTypeChange,
      copyToClipboard,
      handleSubmit,
      testConnection,
      closeOnBackdrop
    }
  }
}
</script>

<style scoped>
.penny-connection-modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  z-index: 1000;
  padding: 40px 20px 20px;
}

.penny-connection-modal {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  max-width: 800px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  margin-top: 20px;
}

.dark .penny-connection-modal {
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

.connection-info {
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
}

.connection-form {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.form-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e5e7eb;
}

.form-section:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.section-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.dark .section-title {
  color: white;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.dark .form-label {
  color: #d1d5db;
}

.required {
  color: #ef4444;
}

.form-input,
.form-select,
.form-textarea {
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  background: white;
  color: #1f2937;
  transition: all 0.2s;
}

.dark .form-input,
.dark .form-select,
.dark .form-textarea {
  background: #374151;
  border-color: #4b5563;
  color: white;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.form-help {
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
}

.dark .form-help {
  color: #9ca3af;
}

.input-with-copy {
  position: relative;
  display: flex;
}

.input-with-copy .form-input {
  flex: 1;
  padding-right: 40px;
}

.copy-button {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  padding: 6px;
  border-radius: 4px;
  cursor: pointer;
  color: #6b7280;
  transition: all 0.2s;
}

.copy-button:hover {
  background: #f3f4f6;
  color: #1f2937;
}

.dark .copy-button {
  color: #9ca3af;
}

.dark .copy-button:hover {
  background: #4b5563;
  color: white;
}

.copy-button.copied {
  color: #10b981;
}

.form-actions {
  margin-top: 4px;
}

.external-link {
  display: inline-flex;
  align-items: center;
  font-size: 12px;
  color: #3b82f6;
  text-decoration: none;
  transition: color 0.2s;
}

.external-link:hover {
  color: #2563eb;
}

.connection-config {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.checkbox-input {
  width: 16px;
  height: 16px;
  accent-color: #3b82f6;
}

.checkbox-text {
  font-size: 14px;
  color: #374151;
}

.dark .checkbox-text {
  color: #d1d5db;
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

.btn-secondary:hover {
  background: #e5e7eb;
}

.dark .btn-secondary {
  background: #374151;
  color: #d1d5db;
}

.dark .btn-secondary:hover {
  background: #4b5563;
}

.btn-outline {
  background: transparent;
  color: #3b82f6;
  border: 1px solid #3b82f6;
}

.btn-outline:hover:not(:disabled) {
  background: #3b82f6;
  color: white;
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
