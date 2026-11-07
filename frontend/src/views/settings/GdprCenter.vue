<template>
  <div class="max-w-4xl mx-auto p-6 space-y-6">
    <h1 class="text-2xl font-bold text-gray-900 dark:text-white">GDPR & Data Privacy</h1>

    <!-- Data export -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
      <h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-2">Export tenant data</h2>
      <p class="text-sm text-gray-500 dark:text-gray-400 mb-4">
        Download all tenant data (conversations, messages, customer profiles, feedback) as JSON — GDPR data portability.
      </p>
      <button
        @click="exportData"
        :disabled="exporting"
        class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
      >
        {{ exporting ? 'Exporting...' : 'Download export' }}
      </button>
    </div>

    <!-- Deletion requests -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
      <div class="flex items-center justify-between mb-4">
        <div>
          <h2 class="text-lg font-semibold text-gray-900 dark:text-white">Data deletion requests</h2>
          <p class="text-sm text-gray-500 dark:text-gray-400">
            Request erasure of all customer PII. A 30-day grace period applies before auto-processing.
          </p>
        </div>
        <button
          @click="showCreate = true"
          class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700"
        >
          New request
        </button>
      </div>

      <div v-if="loading" class="text-sm text-gray-500">Loading...</div>
      <div v-else-if="requests.length === 0" class="text-sm text-gray-500">No deletion requests.</div>
      <table v-else class="w-full text-sm">
        <thead>
          <tr class="text-left text-gray-500 dark:text-gray-400 border-b dark:border-gray-700">
            <th class="py-2">ID</th>
            <th>Status</th>
            <th>Scheduled</th>
            <th>Processed</th>
            <th>Result</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in requests" :key="r.id" class="border-b dark:border-gray-700">
            <td class="py-2">{{ r.id }}</td>
            <td>
              <span class="px-2 py-0.5 rounded text-xs font-medium" :class="statusClass(r.status)">
                {{ r.status }}
              </span>
            </td>
            <td>{{ formatDate(r.scheduledAt) }}</td>
            <td>{{ formatDate(r.processedAt) }}</td>
            <td class="max-w-xs truncate" :title="r.resultSummary">{{ r.resultSummary || '—' }}</td>
            <td class="text-right space-x-2">
              <button
                v-if="r.status === 'PENDING'"
                @click="cancel(r)"
                class="text-red-600 hover:underline text-xs"
              >Cancel</button>
              <button
                v-if="r.status === 'PENDING' || r.status === 'FAILED'"
                @click="process(r)"
                class="text-blue-600 hover:underline text-xs"
              >Process now</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Create modal -->
    <div v-if="showCreate" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-md">
        <h3 class="text-lg font-semibold mb-4 text-gray-900 dark:text-white">New deletion request</h3>
        <label class="block text-sm font-medium mb-1 text-gray-700 dark:text-gray-300">Reason (optional)</label>
        <textarea
          v-model="newReason"
          rows="3"
          class="w-full px-3 py-2 border rounded-lg dark:bg-gray-900 dark:border-gray-600 mb-4"
          placeholder="e.g. Customer erasure request #123"
        ></textarea>
        <p class="text-xs text-gray-500 mb-4">
          This will anonymize customer profiles and delete messages, conversations and feedback
          for this tenant after the grace period.
        </p>
        <div class="flex justify-end gap-3">
          <button @click="showCreate = false" class="px-4 py-2 text-gray-600 dark:text-gray-300">Cancel</button>
          <button
            @click="create"
            :disabled="creating"
            class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:opacity-50"
          >
            {{ creating ? 'Creating...' : 'Create request' }}
          </button>
        </div>
      </div>
    </div>

    <p v-if="message" class="text-sm" :class="messageOk ? 'text-green-600' : 'text-red-600'">{{ message }}</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import complianceApi from '@/api/complianceApi'

const requests = ref([])
const loading = ref(false)
const exporting = ref(false)
const showCreate = ref(false)
const newReason = ref('')
const creating = ref(false)
const message = ref('')
const messageOk = ref(true)

async function load() {
  loading.value = true
  try {
    const { data } = await complianceApi.listDeletionRequests()
    requests.value = data.data || data || []
  } catch (e) {
    console.error('Failed to load deletion requests', e)
  } finally {
    loading.value = false
  }
}

async function exportData() {
  exporting.value = true
  try {
    const res = await complianceApi.exportTenantData()
    const url = URL.createObjectURL(res.data)
    const a = document.createElement('a')
    a.href = url
    a.download = `tenant-export-${new Date().toISOString().slice(0, 10)}.json`
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    message.value = 'Export failed'
    messageOk.value = false
  } finally {
    exporting.value = false
  }
}

async function create() {
  creating.value = true
  message.value = ''
  try {
    await complianceApi.createDeletionRequest(newReason.value)
    showCreate.value = false
    newReason.value = ''
    message.value = 'Deletion request created'
    messageOk.value = true
    await load()
  } catch (e) {
    message.value = e.response?.data?.message || 'Failed to create request'
    messageOk.value = false
  } finally {
    creating.value = false
  }
}

async function cancel(r) {
  if (!confirm(`Cancel deletion request #${r.id}?`)) return
  try {
    await complianceApi.cancelDeletionRequest(r.id)
    await load()
  } catch (e) {
    message.value = e.response?.data?.message || 'Cancel failed'
    messageOk.value = false
  }
}

async function process(r) {
  if (!confirm(`Process deletion request #${r.id} NOW? This deletes customer data immediately.`)) return
  try {
    await complianceApi.processDeletionRequest(r.id)
    await load()
  } catch (e) {
    message.value = e.response?.data?.message || 'Process failed'
    messageOk.value = false
  }
}

function statusClass(s) {
  return {
    PENDING: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200',
    PROCESSING: 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200',
    COMPLETED: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
    CANCELLED: 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-200',
    FAILED: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200',
  }[s] || 'bg-gray-100 text-gray-800'
}

function formatDate(d) {
  if (!d) return '—'
  // backend serializes LocalDateTime as array [y,m,d,h,m] or ISO string
  if (Array.isArray(d)) {
    const [y, m, day, h = 0, min = 0] = d
    return `${y}-${String(m).padStart(2, '0')}-${String(day).padStart(2, '0')} ${String(h).padStart(2, '0')}:${String(min).padStart(2, '0')}`
  }
  return new Date(d).toLocaleString()
}

onMounted(load)
</script>
