<template>
  <div class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full z-50">
    <div class="relative min-h-screen flex items-center justify-center p-4">
      <div class="relative bg-white dark:bg-gray-800 rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:w-full sm:max-w-3xl">
        <!-- Header -->
        <div class="bg-white dark:bg-gray-800 px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
          <div class="flex items-center justify-between">
            <h3 class="text-lg leading-6 font-medium text-gray-900 dark:text-white">
              {{ connection ? $t('Edit Connection') : $t('Create Connection') }}
            </h3>
            <button
              @click="$emit('close')"
              class="rounded-md text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              <Icon icon="mdi:close" class="h-6 w-6" />
            </button>
          </div>
        </div>

        <!-- Body -->
        <form @submit.prevent="handleSubmit" class="bg-white dark:bg-gray-800 px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
          <div class="space-y-6">
            <!-- Connection Name -->
            <div>
              <label for="connection-name" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                {{ $t('Connection Name') }}
              </label>
              <div class="mt-1">
                <input
                  id="connection-name"
                  v-model="form.name"
                  type="text"
                  required
                  :placeholder="$t('Enter connection name')"
                  class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                />
              </div>
            </div>

            <!-- Connection Type -->
            <div>
              <label for="connection-type" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                {{ $t('Connection Type') }}
              </label>
              <div class="mt-1">
                <select
                  id="connection-type"
                  v-model="form.type"
                  required
                  @change="onTypeChange"
                  class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                >
                  <option value="">{{ $t('Select connection type') }}</option>
                  <option value="facebook">{{ $t('Facebook Messenger') }}</option>
                  <option value="webhook">{{ $t('Webhook') }}</option>
                  <option value="api">{{ $t('REST API') }}</option>
                  <option value="database">{{ $t('Database') }}</option>
                </select>
              </div>
            </div>

            <!-- Facebook Messenger Fields -->
            <div v-if="form.type === 'facebook'" class="space-y-4">
              <div>
                <label for="page-id" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                  {{ $t('Page ID') }}
                </label>
                <div class="mt-1">
                  <input
                    id="page-id"
                    v-model="form.pageId"
                    type="text"
                    required
                    :placeholder="$t('Enter Facebook Page ID')"
                    class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  />
                </div>
              </div>

              <div>
                <label for="fanpage-url" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                  {{ $t('Fanpage URL') }}
                </label>
                <div class="mt-1">
                  <input
                    id="fanpage-url"
                    v-model="form.fanpageUrl"
                    type="url"
                    :placeholder="$t('Enter fanpage URL')"
                    class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  />
                </div>
              </div>

              <div>
                <label for="page-access-token" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                  {{ $t('Page Access Token') }}
                </label>
                <div class="mt-1">
                  <input
                    id="page-access-token"
                    v-model="form.pageAccessToken"
                    type="password"
                    required
                    :placeholder="$t('Enter page access token')"
                    class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  />
                </div>
              </div>

              <div>
                <label for="app-secret" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                  {{ $t('App Secret') }}
                </label>
                <div class="mt-1">
                  <input
                    id="app-secret"
                    v-model="form.appSecret"
                    type="password"
                    :placeholder="$t('Enter app secret')"
                    class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  />
                </div>
              </div>
            </div>

            <!-- Webhook Fields -->
            <div v-if="form.type === 'webhook'" class="space-y-4">
              <div>
                <label for="webhook-url" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                  {{ $t('Webhook URL') }}
                </label>
                <div class="mt-1">
                  <input
                    id="webhook-url"
                    v-model="form.webhookUrl"
                    type="url"
                    required
                    :placeholder="$t('Enter webhook URL')"
                    class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  />
                </div>
              </div>

              <div>
                <label for="verify-token" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                  {{ $t('Verify Token') }}
                </label>
                <div class="mt-1">
                  <input
                    id="verify-token"
                    v-model="form.verifyToken"
                    type="text"
                    :placeholder="$t('Enter verify token')"
                    class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  />
                </div>
              </div>
            </div>

            <!-- REST API Fields -->
            <div v-if="form.type === 'api'" class="space-y-4">
              <div>
                <label for="api-url" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                  {{ $t('API URL') }}
                </label>
                <div class="mt-1">
                  <input
                    id="api-url"
                    v-model="form.apiUrl"
                    type="url"
                    required
                    :placeholder="$t('Enter API URL')"
                    class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  />
                </div>
              </div>

              <div>
                <label for="api-key" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                  {{ $t('API Key') }}
                </label>
                <div class="mt-1">
                  <input
                    id="api-key"
                    v-model="form.apiKey"
                    type="password"
                    :placeholder="$t('Enter API key')"
                    class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  />
                </div>
              </div>

              <div>
                <label for="api-version" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                  {{ $t('API Version') }}
                </label>
                <div class="mt-1">
                  <input
                    id="api-version"
                    v-model="form.apiVersion"
                    type="text"
                    :placeholder="$t('Enter API version')"
                    class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  />
                </div>
              </div>
            </div>

            <!-- Database Fields -->
            <div v-if="form.type === 'database'" class="space-y-4">
              <div>
                <label for="database-host" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                  {{ $t('Database Host') }}
                </label>
                <div class="mt-1">
                  <input
                    id="database-host"
                    v-model="form.databaseHost"
                    type="text"
                    required
                    :placeholder="$t('Enter database host')"
                    class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  />
                </div>
              </div>

              <div>
                <label for="database-port" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                  {{ $t('Database Port') }}
                </label>
                <div class="mt-1">
                  <input
                    id="database-port"
                    v-model="form.databasePort"
                    type="number"
                    required
                    :placeholder="$t('Enter database port')"
                    class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  />
                </div>
              </div>

              <div>
                <label for="database-name" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                  {{ $t('Database Name') }}
                </label>
                <div class="mt-1">
                  <input
                    id="database-name"
                    v-model="form.databaseName"
                    type="text"
                    required
                    :placeholder="$t('Enter database name')"
                    class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  />
                </div>
              </div>

              <div>
                <label for="username" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                  {{ $t('Username') }}
                </label>
                <div class="mt-1">
                  <input
                    id="username"
                    v-model="form.username"
                    type="text"
                    required
                    :placeholder="$t('Enter username')"
                    class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  />
                </div>
              </div>

              <div>
                <label for="password" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                  {{ $t('Password') }}
                </label>
                <div class="mt-1">
                  <input
                    id="password"
                    v-model="form.password"
                    type="password"
                    required
                    :placeholder="$t('Enter password')"
                    class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  />
                </div>
              </div>
            </div>

            <!-- Common Fields -->
            <div class="space-y-4">
              <div v-if="form.type === 'webhook' || form.type === 'api'">
                <label for="http-method" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                  {{ $t('HTTP Method') }}
                </label>
                <div class="mt-1">
                  <select
                    id="http-method"
                    v-model="form.httpMethod"
                    class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  >
                    <option value="GET">GET</option>
                    <option value="POST">POST</option>
                    <option value="PUT">PUT</option>
                    <option value="DELETE">DELETE</option>
                  </select>
                </div>
              </div>

              <div v-if="form.type === 'webhook' || form.type === 'api'">
                <label for="timeout" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                  {{ $t('Timeout (seconds)') }}
                </label>
                <div class="mt-1">
                  <input
                    id="timeout"
                    v-model.number="form.timeout"
                    type="number"
                    min="1"
                    max="300"
                    :placeholder="$t('Enter timeout in seconds')"
                    class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  />
                </div>
              </div>

              <div v-if="form.type === 'webhook' || form.type === 'api'">
                <label for="headers" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                  {{ $t('Headers (JSON)') }}
                </label>
                <div class="mt-1">
                  <textarea
                    id="headers"
                    v-model="form.headers"
                    rows="3"
                    :placeholder='$t("Enter headers in JSON format")'
                    class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  />
                </div>
              </div>

              <div>
                <label class="flex items-center">
                  <input
                    v-model="form.isEnabled"
                    type="checkbox"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded dark:bg-gray-700 dark:border-gray-600"
                  />
                  <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">
                    {{ $t('Enabled') }}
                  </span>
                </label>
              </div>
            </div>
          </div>
        </form>

        <!-- Footer -->
        <div class="bg-gray-50 dark:bg-gray-700 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
          <button
            type="button"
            @click="$emit('close')"
            class="w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 text-base font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 dark:bg-gray-800 dark:border-gray-600 dark:text-white dark:hover:bg-gray-700 sm:w-auto sm:ml-3"
          >
            {{ $t('Cancel') }}
          </button>
          <button
            type="submit"
            @click="handleSubmit"
            :disabled="loading"
            class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 text-base font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 sm:w-auto"
          >
            <Icon v-if="loading" icon="mdi:loading" class="animate-spin h-4 w-4 mr-2" />
            {{ connection ? $t('Update Connection') : $t('Create Connection') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Icon } from '@iconify/vue'

const { t } = useI18n()

const props = defineProps({
  connection: {
    type: Object,
    default: null
  },
  botId: {
    type: String,
    required: true
  },
  show: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close', 'saved'])

// State
const loading = ref(false)
const form = ref({
  name: '',
  type: '',
  // Facebook fields
  pageId: '',
  fanpageUrl: '',
  pageAccessToken: '',
  appSecret: '',
  // Webhook fields
  webhookUrl: '',
  verifyToken: '',
  // API fields
  apiUrl: '',
  apiKey: '',
  apiVersion: '',
  // Database fields
  databaseHost: '',
  databasePort: '',
  databaseName: '',
  username: '',
  password: '',
  // Common fields
  httpMethod: 'POST',
  timeout: 30,
  headers: '',
  isEnabled: true
})

// Methods
const onTypeChange = () => {
  // Reset form when type changes
  form.value = {
    ...form.value,
    // Reset type-specific fields
    pageId: '',
    fanpageUrl: '',
    pageAccessToken: '',
    appSecret: '',
    webhookUrl: '',
    verifyToken: '',
    apiUrl: '',
    apiKey: '',
    apiVersion: '',
    databaseHost: '',
    databasePort: '',
    databaseName: '',
    username: '',
    password: ''
  }
}

const handleSubmit = async () => {
  loading.value = true
  try {
    // TODO: Replace with actual API call
    // const connectionData = {
    //   ...form.value,
    //   id: props.connection?.id
    // }
    // 
    // if (props.connection) {
    //   await pennyApi.updateConnection(props.botId, props.connection.id, connectionData)
    // } else {
    //   const response = await pennyApi.createConnection(props.botId, connectionData)
    //   connectionData.id = response.data.id
    // }
    
    // Mock API call
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    const savedConnection = {
      id: props.connection?.id || Date.now().toString(),
      ...form.value,
      botId: props.botId,
      health: 'Healthy',
      lastUsed: new Date(),
      createdAt: props.connection?.createdAt || new Date(),
      updatedAt: new Date()
    }
    
    emit('saved', savedConnection)
  } catch (error) {
    console.error('Failed to save connection:', error)
  } finally {
    loading.value = false
  }
}

// Watch for connection prop changes
watch(() => props.connection, (newConnection) => {
  if (newConnection) {
    form.value = {
      name: newConnection.name || '',
      type: newConnection.type || '',
      pageId: newConnection.pageId || '',
      fanpageUrl: newConnection.fanpageUrl || '',
      pageAccessToken: newConnection.pageAccessToken || '',
      appSecret: newConnection.appSecret || '',
      webhookUrl: newConnection.webhookUrl || '',
      verifyToken: newConnection.verifyToken || '',
      apiUrl: newConnection.apiUrl || '',
      apiKey: newConnection.apiKey || '',
      apiVersion: newConnection.apiVersion || '',
      databaseHost: newConnection.databaseHost || '',
      databasePort: newConnection.databasePort || '',
      databaseName: newConnection.databaseName || '',
      username: newConnection.username || '',
      password: newConnection.password || '',
      httpMethod: newConnection.httpMethod || 'POST',
      timeout: newConnection.timeout || 30,
      headers: newConnection.headers || '',
      isEnabled: newConnection.isEnabled ?? true
    }
  } else {
    form.value = {
      name: '',
      type: '',
      pageId: '',
      fanpageUrl: '',
      pageAccessToken: '',
      appSecret: '',
      webhookUrl: '',
      verifyToken: '',
      apiUrl: '',
      apiKey: '',
      apiVersion: '',
      databaseHost: '',
      databasePort: '',
      databaseName: '',
      username: '',
      password: '',
      httpMethod: 'POST',
      timeout: 30,
      headers: '',
      isEnabled: true
    }
  }
}, { immediate: true })

// Close modal when ESC key is pressed
onMounted(() => {
  const handleEscape = (e) => {
    if (e.key === 'Escape' && props.show) {
      emit('close')
    }
  }
  document.addEventListener('keydown', handleEscape)
  
  return () => {
    document.removeEventListener('keydown', handleEscape)
  }
})
</script>

<style scoped>
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
