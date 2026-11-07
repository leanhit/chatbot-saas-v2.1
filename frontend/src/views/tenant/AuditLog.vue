<template>
  <div class="audit-log p-4">
    <div class="mt-2 w-full">
      <div class="flex flex-wrap justify-between items-center gap-3">
        <div>
          <p class="uppercase text-xs text-gray-700 dark:text-gray-300 font-semibold">Tenant</p>
          <h1 class="text-2xl text-gray-900 dark:text-gray-200 font-medium">Audit Log</h1>
        </div>
        <div class="flex items-center gap-2">
          <input
            v-model="actionFilter"
            @keyup.enter="applyFilter"
            type="text"
            placeholder="Filter by action..."
            class="border rounded-md px-3 py-2 text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-white"
          />
          <button @click="applyFilter" class="px-3 py-2 bg-blue-600 text-white rounded-md text-sm hover:bg-blue-700">Filter</button>
        </div>
      </div>
    </div>

    <div class="mt-6 bg-white dark:bg-gray-800 rounded-md border dark:border-gray-700">
      <div v-if="loading" class="p-8 text-center text-gray-500">
        <Icon icon="mdi:loading" class="animate-spin text-2xl" />
      </div>
      <div v-else-if="logs.length === 0" class="p-8 text-center text-gray-500">No audit logs found.</div>
      <div v-else class="overflow-x-auto">
        <table class="w-full">
          <thead class="bg-gray-50 dark:bg-gray-700">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">Time</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">User</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">Action</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">Details</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200 dark:divide-gray-700">
            <tr v-for="log in logs" :key="log.id" class="hover:bg-gray-50 dark:hover:bg-gray-700">
              <td class="px-4 py-3 text-sm text-gray-600 dark:text-gray-300 whitespace-nowrap">{{ formatDateTime(log.createdAt) }}</td>
              <td class="px-4 py-3 text-sm text-gray-900 dark:text-white">{{ log.userEmail }}</td>
              <td class="px-4 py-3">
                <span class="px-2 py-1 text-xs rounded-full bg-blue-100 text-blue-800 dark:bg-blue-900/40 dark:text-blue-200 font-mono">
                  {{ log.action }}
                </span>
              </td>
              <td class="px-4 py-3 text-sm text-gray-600 dark:text-gray-300 max-w-md truncate" :title="log.details">
                {{ log.details || '—' }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="p-4 border-t dark:border-gray-700 flex items-center justify-between">
        <span class="text-sm text-gray-500 dark:text-gray-400">
          Page {{ page + 1 }} of {{ totalPages }} ({{ totalElements }} entries)
        </span>
        <div class="flex gap-2">
          <button
            @click="goToPage(page - 1)"
            :disabled="page === 0"
            class="px-3 py-1 text-sm border rounded-md disabled:opacity-50 dark:border-gray-600 dark:text-gray-300"
          >Previous</button>
          <button
            @click="goToPage(page + 1)"
            :disabled="page >= totalPages - 1"
            class="px-3 py-1 text-sm border rounded-md disabled:opacity-50 dark:border-gray-600 dark:text-gray-300"
          >Next</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import auditApi from '@/api/auditApi'

const logs = ref([])
const loading = ref(false)
const page = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)
const actionFilter = ref('')
const appliedAction = ref('')

function formatDateTime(v) {
  if (!v) return '—'
  const d = new Date(v)
  return isNaN(d) ? v : d.toLocaleString()
}

async function loadLogs() {
  loading.value = true
  try {
    const params = { page: page.value, size: 20 }
    if (appliedAction.value) params.action = appliedAction.value
    const { data } = await auditApi.getAuditLogs(params)
    logs.value = data.content || []
    totalPages.value = data.totalPages || 0
    totalElements.value = data.totalElements || 0
  } catch (e) {
    console.error('Failed to load audit logs', e)
    logs.value = []
  } finally {
    loading.value = false
  }
}

function applyFilter() {
  appliedAction.value = actionFilter.value.trim()
  page.value = 0
  loadLogs()
}

function goToPage(p) {
  if (p < 0 || p >= totalPages.value) return
  page.value = p
  loadLogs()
}

onMounted(loadLogs)
</script>
