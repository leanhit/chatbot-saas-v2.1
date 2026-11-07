<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Custom Reports</h1>
      <button @click="openForm(null)"
        class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm">
        + New Report
      </button>
    </div>

    <div v-if="loading" class="text-center py-12">
      <Icon icon="mdi:loading" class="animate-spin text-4xl text-blue-600" />
    </div>

    <div v-else class="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <div v-for="report in reports" :key="report.id"
        class="bg-white dark:bg-gray-800 rounded-lg shadow p-4">
        <div class="flex items-start justify-between">
          <div>
            <h3 class="font-semibold text-gray-900 dark:text-white">{{ report.name }}</h3>
            <p class="text-sm text-gray-500">{{ report.reportType }} · last {{ report.dateRangeDays }} days</p>
            <p v-if="report.lastGeneratedAt" class="text-xs text-gray-400 mt-1">
              Generated {{ formatDate(report.lastGeneratedAt) }}
            </p>
          </div>
          <div class="flex gap-2">
            <button @click="generate(report.id)" class="text-blue-600 hover:underline text-sm">Generate</button>
            <button @click="viewResult(report.id)" class="text-gray-600 hover:underline text-sm">View</button>
            <button @click="openForm(report)" class="text-gray-600 hover:underline text-sm">Edit</button>
            <button @click="remove(report.id)" class="text-red-600 hover:underline text-sm">Delete</button>
          </div>
        </div>
      </div>
      <div v-if="reports.length === 0" class="col-span-2 text-center py-12 text-gray-500">
        No reports yet.
      </div>
    </div>

    <!-- Result modal -->
    <div v-if="result" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-2xl max-h-[80vh] overflow-auto">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-lg font-semibold">Report Result</h2>
          <button @click="result = null" class="text-gray-500">✕</button>
        </div>
        <pre class="text-xs bg-gray-100 dark:bg-gray-900 rounded p-4 overflow-auto">{{ JSON.stringify(result, null, 2) }}</pre>
      </div>
    </div>

    <!-- Form modal -->
    <div v-if="showForm" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-md">
        <h2 class="text-lg font-semibold mb-4">{{ editingId ? 'Edit' : 'New' }} Report</h2>
        <div class="space-y-3">
          <input v-model="form.name" placeholder="Report name" class="w-full border rounded px-3 py-2 dark:bg-gray-700 dark:border-gray-600" />
          <select v-model="form.reportType" class="w-full border rounded px-3 py-2 dark:bg-gray-700 dark:border-gray-600">
            <option value="CONVERSATIONS">Conversations</option>
            <option value="SENTIMENT">Sentiment</option>
            <option value="AI_USAGE">AI Usage</option>
            <option value="FEEDBACK">Feedback</option>
            <option value="FUNNEL">Funnel</option>
          </select>
          <input v-model.number="form.dateRangeDays" type="number" placeholder="Date range (days)" class="w-full border rounded px-3 py-2 dark:bg-gray-700 dark:border-gray-600" />
        </div>
        <div class="flex justify-end gap-2 mt-6">
          <button @click="showForm = false" class="px-4 py-2 text-gray-600">Cancel</button>
          <button @click="save" class="px-4 py-2 bg-blue-600 text-white rounded-lg">Save</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Icon } from '@iconify/vue'
import analyticsApi from '@/api/analyticsApi'

export default {
  name: 'CustomReports',
  components: { Icon },
  data() {
    return {
      loading: true,
      reports: [],
      result: null,
      showForm: false,
      editingId: null,
      form: { name: '', reportType: 'CONVERSATIONS', dateRangeDays: 30 },
    }
  },
  async mounted() {
    await this.load()
  },
  methods: {
    async load() {
      this.loading = true
      try {
        this.reports = await analyticsApi.listReports()
      } finally {
        this.loading = false
      }
    },
    openForm(report) {
      if (report) {
        this.editingId = report.id
        this.form = { name: report.name, reportType: report.reportType, dateRangeDays: report.dateRangeDays }
      } else {
        this.editingId = null
        this.form = { name: '', reportType: 'CONVERSATIONS', dateRangeDays: 30 }
      }
      this.showForm = true
    },
    async save() {
      if (this.editingId) {
        await analyticsApi.updateReport(this.editingId, this.form)
      } else {
        await analyticsApi.createReport(this.form)
      }
      this.showForm = false
      await this.load()
    },
    async generate(id) {
      this.result = await analyticsApi.generateReport(id)
      await this.load()
    },
    async viewResult(id) {
      this.result = await analyticsApi.getReportResult(id)
    },
    async remove(id) {
      if (!confirm('Delete this report?')) return
      await analyticsApi.deleteReport(id)
      await this.load()
    },
    formatDate(d) {
      return d ? new Date(d).toLocaleString() : ''
    },
  },
}
</script>
