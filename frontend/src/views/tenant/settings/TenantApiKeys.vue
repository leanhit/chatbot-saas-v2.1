<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 py-8">
    <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="mb-8 flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold text-gray-900 dark:text-white">API Keys</h1>
          <p class="mt-2 text-gray-600 dark:text-gray-400">
            Programmatic access to the tenant API (<code>/api/tenant-api/**</code>) via <code>X-API-Key</code> header.
          </p>
        </div>
        <button
          @click="openCreateModal"
          class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-primary hover:bg-primary/80"
        >
          <Icon icon="mdi:plus" class="h-4 w-4 mr-2" />
          Create API Key
        </button>
      </div>

      <div class="mb-6 bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg p-4">
        <div class="flex items-start">
          <Icon icon="mdi:information" class="h-5 w-5 text-blue-600 dark:text-blue-400 mt-0.5 mr-3" />
          <div class="text-sm text-blue-800 dark:text-blue-200">
            <p class="font-medium mb-1">Usage</p>
            <p>Send <code class="bg-blue-100 dark:bg-blue-800 px-1 rounded">X-API-Key: cbk_...</code> with requests to <code class="bg-blue-100 dark:bg-blue-800 px-1 rounded">/api/tenant-api/conversations</code>. The raw key is shown only once at creation.</p>
          </div>
        </div>
      </div>

      <div v-if="loading" class="flex justify-center py-12">
        <Icon icon="mdi:loading" class="h-8 w-8 animate-spin text-primary" />
      </div>

      <div v-else-if="keys.length === 0" class="bg-white dark:bg-gray-800 shadow rounded-lg p-12 text-center">
        <Icon icon="mdi:key-outline" class="h-12 w-12 text-gray-400 mx-auto mb-4" />
        <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">No API keys</h3>
        <p class="text-gray-500 dark:text-gray-400 mb-4">Create a key to access the tenant API programmatically.</p>
        <button @click="openCreateModal" class="text-primary hover:underline text-sm font-medium">Create your first key</button>
      </div>

      <div v-else class="space-y-4">
        <div v-for="k in keys" :key="k.id" class="bg-white dark:bg-gray-800 shadow rounded-lg p-6">
          <div class="flex items-start justify-between">
            <div class="flex-1 min-w-0">
              <div class="flex items-center space-x-3">
                <span :class="k.isActive
                  ? 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300'
                  : 'bg-gray-100 text-gray-600 dark:bg-gray-700 dark:text-gray-300'"
                  class="px-2 py-0.5 rounded-full text-xs font-semibold uppercase">
                  {{ k.isActive ? 'Active' : 'Revoked' }}
                </span>
                <h3 class="text-base font-medium text-gray-900 dark:text-white">{{ k.name }}</h3>
                <span class="text-xs text-gray-500 font-mono">{{ k.keyPrefix }}…</span>
              </div>
              <div class="mt-2 flex flex-wrap items-center gap-x-4 gap-y-1 text-sm text-gray-500 dark:text-gray-400">
                <span class="inline-flex items-center"><Icon icon="mdi:shield-account-outline" class="h-4 w-4 mr-1" />{{ k.role }}</span>
                <span class="inline-flex items-center"><Icon icon="mdi:counter" class="h-4 w-4 mr-1" />{{ k.usageCount || 0 }} uses</span>
                <span v-if="k.lastUsedAt" class="inline-flex items-center"><Icon icon="mdi:clock-outline" class="h-4 w-4 mr-1" />Last used {{ formatDate(k.lastUsedAt) }}</span>
                <span v-if="k.expiresAt" class="inline-flex items-center"><Icon icon="mdi:calendar-remove" class="h-4 w-4 mr-1" />Expires {{ formatDate(k.expiresAt) }}</span>
              </div>
            </div>
            <button v-if="k.isActive" @click="confirmRevoke(k)" class="p-2 text-gray-500 hover:text-red-600" title="Revoke">
              <Icon icon="mdi:close-circle-outline" class="h-5 w-5" />
            </button>
          </div>
        </div>
      </div>

      <!-- Create Modal -->
      <div v-if="showModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow-xl w-full max-w-lg">
          <div class="p-6 border-b border-gray-200 dark:border-gray-700">
            <h3 class="text-lg font-medium text-gray-900 dark:text-white">Create API Key</h3>
          </div>
          <div class="p-6 space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Name *</label>
              <input v-model="form.name" type="text" placeholder="e.g. CRM integration" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary dark:bg-gray-700 dark:border-gray-600 dark:text-white" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Role</label>
              <select v-model="form.role" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary dark:bg-gray-700 dark:border-gray-600 dark:text-white">
                <option value="MEMBER">MEMBER — read-only access</option>
                <option value="EDITOR">EDITOR</option>
                <option value="OWNER">OWNER</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Expires at (optional)</label>
              <input v-model="form.expiresAt" type="datetime-local" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary dark:bg-gray-700 dark:border-gray-600 dark:text-white" />
            </div>
          </div>
          <div class="p-6 border-t border-gray-200 dark:border-gray-700 flex justify-end space-x-3">
            <button @click="showModal = false" class="px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-md">Cancel</button>
            <button @click="createKey" :disabled="saving || !form.name" class="px-4 py-2 text-sm text-white bg-primary rounded-md hover:bg-primary/80 disabled:opacity-50">
              <Icon v-if="saving" icon="mdi:loading" class="h-4 w-4 mr-2 animate-spin inline" />
              Create
            </button>
          </div>
        </div>
      </div>

      <!-- Raw key reveal modal -->
      <div v-if="rawKey" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow-xl w-full max-w-lg">
          <div class="p-6 border-b border-gray-200 dark:border-gray-700">
            <h3 class="text-lg font-medium text-gray-900 dark:text-white">API Key Created</h3>
          </div>
          <div class="p-6">
            <div class="bg-amber-50 dark:bg-amber-900/20 border border-amber-200 dark:border-amber-800 rounded-lg p-3 mb-4 text-sm text-amber-800 dark:text-amber-200">
              Copy this key now — it will not be shown again.
            </div>
            <div class="flex items-center space-x-2">
              <code class="flex-1 px-3 py-2 bg-gray-100 dark:bg-gray-700 rounded text-sm font-mono break-all">{{ rawKey }}</code>
              <button @click="copyKey" class="px-3 py-2 text-sm bg-gray-200 dark:bg-gray-600 rounded hover:bg-gray-300 dark:hover:bg-gray-500">
                <Icon icon="mdi:content-copy" class="h-4 w-4" />
              </button>
            </div>
          </div>
          <div class="p-6 border-t border-gray-200 dark:border-gray-700 flex justify-end">
            <button @click="rawKey = null" class="px-4 py-2 text-sm text-white bg-primary rounded-md hover:bg-primary/80">Done</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import { tenantApiKeyApi } from '@/api/tenantApiKeyApi'
import { useToast } from '@/composables/useToast'

export default {
  name: 'TenantApiKeys',
  components: { Icon },
  setup() {
    const toast = useToast()
    const keys = ref([])
    const loading = ref(false)
    const saving = ref(false)
    const showModal = ref(false)
    const rawKey = ref(null)
    const form = ref({ name: '', role: 'MEMBER', expiresAt: null })

    const loadKeys = async () => {
      loading.value = true
      try {
        keys.value = await tenantApiKeyApi.listApiKeys()
      } catch (e) {
        toast?.error('Failed to load API keys: ' + (e.response?.data?.message || e.message))
      } finally {
        loading.value = false
      }
    }

    const openCreateModal = () => {
      form.value = { name: '', role: 'MEMBER', expiresAt: null }
      showModal.value = true
    }

    const createKey = async () => {
      saving.value = true
      try {
        const payload = { name: form.value.name, role: form.value.role }
        if (form.value.expiresAt) {
          payload.expiresAt = new Date(form.value.expiresAt).toISOString()
        }
        const result = await tenantApiKeyApi.createApiKey(payload)
        rawKey.value = result.rawKey
        showModal.value = false
        await loadKeys()
      } catch (e) {
        toast?.error('Create failed: ' + (e.response?.data?.message || e.message))
      } finally {
        saving.value = false
      }
    }

    const confirmRevoke = async (k) => {
      if (!confirm(`Revoke API key "${k.name}"? This cannot be undone.`)) return
      try {
        await tenantApiKeyApi.revokeApiKey(k.id)
        toast?.success('API key revoked')
        await loadKeys()
      } catch (e) {
        toast?.error('Revoke failed: ' + (e.response?.data?.message || e.message))
      }
    }

    const copyKey = () => {
      navigator.clipboard.writeText(rawKey.value)
      toast?.success('Copied to clipboard')
    }

    const formatDate = (d) => {
      if (!d) return ''
      try { return new Date(d).toLocaleDateString() } catch { return d }
    }

    onMounted(loadKeys)

    return {
      keys, loading, saving, showModal, rawKey, form,
      loadKeys, openCreateModal, createKey, confirmRevoke, copyKey, formatDate
    }
  }
}
</script>
