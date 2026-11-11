<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 py-8">
    <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- Header -->
      <div class="mb-8 flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold text-gray-900 dark:text-white">Outbound Webhooks</h1>
          <p class="mt-2 text-gray-600 dark:text-gray-400">
            Receive conversation lifecycle events (created, closed, taken over, transferred, escalated) at your endpoints.
          </p>
        </div>
        <button
          @click="openCreateModal"
          class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-primary hover:bg-primary/80"
        >
          <Icon icon="mdi:plus" class="h-4 w-4 mr-2" />
          Add Webhook
        </button>
      </div>

      <!-- Info banner -->
      <div class="mb-6 bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg p-4">
        <div class="flex items-start">
          <Icon icon="mdi:information" class="h-5 w-5 text-blue-600 dark:text-blue-400 mt-0.5 mr-3" />
          <div class="text-sm text-blue-800 dark:text-blue-200">
            <p class="font-medium mb-1">Signature verification</p>
            <p>Each request includes <code class="bg-blue-100 dark:bg-blue-800 px-1 rounded">X-Chatbot-Signature: sha256=&lt;hmac&gt;</code> computed with your secret. Leave events empty or use <code class="bg-blue-100 dark:bg-blue-800 px-1 rounded">*</code> to receive all events.</p>
          </div>
        </div>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="flex justify-center py-12">
        <Icon icon="mdi:loading" class="h-8 w-8 animate-spin text-primary" />
      </div>

      <!-- Empty state -->
      <div v-else-if="webhooks.length === 0" class="bg-white dark:bg-gray-800 shadow rounded-lg p-12 text-center">
        <Icon icon="mdi:webhook" class="h-12 w-12 text-gray-400 mx-auto mb-4" />
        <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">No webhooks configured</h3>
        <p class="text-gray-500 dark:text-gray-400 mb-4">Add an endpoint to start receiving conversation events.</p>
        <button @click="openCreateModal" class="text-primary hover:underline text-sm font-medium">Add your first webhook</button>
      </div>

      <!-- Webhook list -->
      <div v-else class="space-y-4">
        <div
          v-for="wh in webhooks"
          :key="wh.id"
          class="bg-white dark:bg-gray-800 shadow rounded-lg p-6"
        >
          <div class="flex items-start justify-between">
            <div class="flex-1 min-w-0">
              <div class="flex items-center space-x-3">
                <span :class="wh.isActive
                  ? 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300'
                  : 'bg-gray-100 text-gray-600 dark:bg-gray-700 dark:text-gray-300'"
                  class="px-2 py-0.5 rounded-full text-xs font-semibold uppercase">
                  {{ wh.isActive ? 'Active' : 'Inactive' }}
                </span>
                <h3 class="text-base font-medium text-gray-900 dark:text-white truncate">{{ wh.url }}</h3>
              </div>
              <div class="mt-2 flex flex-wrap items-center gap-x-4 gap-y-1 text-sm text-gray-500 dark:text-gray-400">
                <span class="inline-flex items-center">
                  <Icon icon="mdi:bell-outline" class="h-4 w-4 mr-1" />
                  {{ wh.events || '*' }}
                </span>
                <span class="inline-flex items-center">
                  <Icon icon="mdi:key-outline" class="h-4 w-4 mr-1" />
                  {{ wh.secret ? 'Secret set' : 'No secret' }}
                </span>
                <span class="inline-flex items-center">
                  <Icon icon="mdi:clock-outline" class="h-4 w-4 mr-1" />
                  Created {{ formatDate(wh.createdAt) }}
                </span>
              </div>
            </div>
            <div class="flex items-center space-x-2 ml-4">
              <button @click="openEditModal(wh)" class="p-2 text-gray-500 hover:text-primary" title="Edit">
                <Icon icon="mdi:pencil" class="h-5 w-5" />
              </button>
              <button @click="confirmDelete(wh)" class="p-2 text-gray-500 hover:text-red-600" title="Delete">
                <Icon icon="mdi:delete" class="h-5 w-5" />
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Create/Edit Modal -->
      <div v-if="showModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow-xl w-full max-w-lg">
          <div class="p-6 border-b border-gray-200 dark:border-gray-700">
            <h3 class="text-lg font-medium text-gray-900 dark:text-white">
              {{ editingId ? 'Edit Webhook' : 'Add Webhook' }}
            </h3>
          </div>
          <div class="p-6 space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Endpoint URL *</label>
              <input
                v-model="form.url"
                type="url"
                placeholder="https://example.com/webhooks/chatbot"
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary dark:bg-gray-700 dark:border-gray-600 dark:text-white"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Secret (optional)</label>
              <input
                v-model="form.secret"
                type="text"
                placeholder="Used for HMAC-SHA256 signature"
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary dark:bg-gray-700 dark:border-gray-600 dark:text-white"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Events</label>
              <input
                v-model="form.events"
                type="text"
                placeholder="conversation.created, conversation.closed, ... (empty = all)"
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary dark:bg-gray-700 dark:border-gray-600 dark:text-white"
              />
              <p class="mt-1 text-xs text-gray-500 dark:text-gray-400">
                Comma-separated event types, or <code>*</code> for all.
              </p>
            </div>
            <div v-if="editingId" class="flex items-center">
              <input v-model="form.isActive" type="checkbox" id="wh-active" class="h-4 w-4 text-primary rounded" />
              <label for="wh-active" class="ml-2 text-sm text-gray-700 dark:text-gray-300">Active</label>
            </div>
          </div>
          <div class="p-6 border-t border-gray-200 dark:border-gray-700 flex justify-end space-x-3">
            <button @click="showModal = false" class="px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-md">
              Cancel
            </button>
            <button
              @click="saveWebhook"
              :disabled="saving || !form.url"
              class="px-4 py-2 text-sm text-white bg-primary rounded-md hover:bg-primary/80 disabled:opacity-50"
            >
              <Icon v-if="saving" icon="mdi:loading" class="h-4 w-4 mr-2 animate-spin inline" />
              {{ editingId ? 'Save Changes' : 'Create Webhook' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import { tenantWebhookApi } from '@/api/tenantWebhookApi'
import { useToast } from '@/composables/useToast'

export default {
  name: 'TenantWebhooks',
  components: { Icon },
  setup() {
    const toast = useToast()
    const webhooks = ref([])
    const loading = ref(false)
    const saving = ref(false)
    const showModal = ref(false)
    const editingId = ref(null)
    const form = ref({ url: '', secret: '', events: '', isActive: true })

    const loadWebhooks = async () => {
      loading.value = true
      try {
        webhooks.value = await tenantWebhookApi.listWebhooks()
      } catch (e) {
        toast?.error('Failed to load webhooks: ' + (e.response?.data?.message || e.message))
      } finally {
        loading.value = false
      }
    }

    const openCreateModal = () => {
      editingId.value = null
      form.value = { url: '', secret: '', events: '', isActive: true }
      showModal.value = true
    }

    const openEditModal = (wh) => {
      editingId.value = wh.id
      form.value = {
        url: wh.url,
        secret: wh.secret || '',
        events: wh.events || '',
        isActive: wh.isActive !== false
      }
      showModal.value = true
    }

    const saveWebhook = async () => {
      saving.value = true
      try {
        const payload = {
          url: form.value.url,
          secret: form.value.secret || null,
          events: form.value.events || null,
          isActive: form.value.isActive
        }
        if (editingId.value) {
          await tenantWebhookApi.updateWebhook(editingId.value, payload)
          toast?.success('Webhook updated')
        } else {
          await tenantWebhookApi.createWebhook(payload)
          toast?.success('Webhook created')
        }
        showModal.value = false
        await loadWebhooks()
      } catch (e) {
        toast?.error('Save failed: ' + (e.response?.data?.message || e.message))
      } finally {
        saving.value = false
      }
    }

    const confirmDelete = async (wh) => {
      if (!confirm(`Delete webhook ${wh.url}?`)) return
      try {
        await tenantWebhookApi.deleteWebhook(wh.id)
        toast?.success('Webhook deleted')
        await loadWebhooks()
      } catch (e) {
        toast?.error('Delete failed: ' + (e.response?.data?.message || e.message))
      }
    }

    const formatDate = (d) => {
      if (!d) return ''
      try { return new Date(d).toLocaleDateString() } catch { return d }
    }

    onMounted(loadWebhooks)

    return {
      webhooks, loading, saving, showModal, editingId, form,
      loadWebhooks, openCreateModal, openEditModal, saveWebhook, confirmDelete, formatDate
    }
  }
}
</script>
