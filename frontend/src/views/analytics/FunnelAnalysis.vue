<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Funnel Analysis</h1>
      <button @click="openStageForm(null)"
        class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm">
        + Add Stage
      </button>
    </div>

    <div v-if="loading" class="text-center py-12">
      <Icon icon="mdi:loading" class="animate-spin text-4xl text-blue-600" />
    </div>

    <div v-else-if="funnel.length === 0" class="bg-white dark:bg-gray-800 rounded-lg shadow p-8 text-center text-gray-500">
      No funnel stages configured. Add stages to measure conversion.
    </div>

    <div v-else class="space-y-3">
      <div v-for="(stage, i) in funnel" :key="stage.id"
        class="bg-white dark:bg-gray-800 rounded-lg shadow p-4 flex items-center gap-4">
        <div class="w-10 h-10 rounded-full bg-blue-100 dark:bg-blue-900 flex items-center justify-center font-bold text-blue-600">
          {{ i + 1 }}
        </div>
        <div class="flex-1">
          <p class="font-semibold text-gray-900 dark:text-white">{{ stage.name }}</p>
          <p class="text-sm text-gray-500">{{ stage.count }} conversations</p>
        </div>
        <div v-if="stage.conversionFromPrevious != null" class="text-sm">
          <span class="px-2 py-1 rounded bg-green-100 dark:bg-green-900 text-green-700 dark:text-green-300">
            {{ (stage.conversionFromPrevious * 100).toFixed(1) }}% from prev
          </span>
        </div>
        <button @click="openStageForm(stage)" class="text-blue-600 hover:underline text-sm">Edit</button>
        <button @click="removeStage(stage.id)" class="text-red-600 hover:underline text-sm">Delete</button>
      </div>
    </div>

    <!-- Stage form modal -->
    <div v-if="showForm" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-md">
        <h2 class="text-lg font-semibold mb-4">{{ editingId ? 'Edit' : 'Add' }} Funnel Stage</h2>
        <div class="space-y-3">
          <input v-model="form.name" placeholder="Stage name" class="w-full border rounded px-3 py-2 dark:bg-gray-700 dark:border-gray-600" />
          <input v-model.number="form.stageOrder" type="number" placeholder="Order" class="w-full border rounded px-3 py-2 dark:bg-gray-700 dark:border-gray-600" />
          <select v-model="form.matchType" class="w-full border rounded px-3 py-2 dark:bg-gray-700 dark:border-gray-600">
            <option value="STATUS">Conversation status</option>
            <option value="ESCALATED">Escalated</option>
            <option value="FEEDBACK">Has feedback</option>
            <option value="AGENT_ASSIGNED">Agent assigned</option>
          </select>
          <input v-if="form.matchType === 'STATUS'" v-model="form.matchValue" placeholder="Status (e.g. CLOSED)" class="w-full border rounded px-3 py-2 dark:bg-gray-700 dark:border-gray-600" />
        </div>
        <div class="flex justify-end gap-2 mt-6">
          <button @click="showForm = false" class="px-4 py-2 text-gray-600">Cancel</button>
          <button @click="saveStage" class="px-4 py-2 bg-blue-600 text-white rounded-lg">Save</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Icon } from '@iconify/vue'
import analyticsApi from '@/api/analyticsApi'

export default {
  name: 'FunnelAnalysis',
  components: { Icon },
  data() {
    return {
      loading: true,
      funnel: [],
      showForm: false,
      editingId: null,
      form: { name: '', stageOrder: 1, matchType: 'STATUS', matchValue: '' },
    }
  },
  async mounted() {
    await this.load()
  },
  methods: {
    async load() {
      this.loading = true
      try {
        this.funnel = await analyticsApi.getFunnel()
      } finally {
        this.loading = false
      }
    },
    openStageForm(stage) {
      if (stage) {
        this.editingId = stage.id
        this.form = { name: stage.name, stageOrder: stage.order, matchType: stage.matchType || 'STATUS', matchValue: stage.matchValue || '' }
      } else {
        this.editingId = null
        this.form = { name: '', stageOrder: this.funnel.length + 1, matchType: 'STATUS', matchValue: '' }
      }
      this.showForm = true
    },
    async saveStage() {
      const payload = { ...this.form, matchValue: this.form.matchType === 'STATUS' ? this.form.matchValue : null }
      if (this.editingId) {
        await analyticsApi.updateFunnelStage(this.editingId, payload)
      } else {
        await analyticsApi.createFunnelStage(payload)
      }
      this.showForm = false
      await this.load()
    },
    async removeStage(id) {
      if (!confirm('Delete this stage?')) return
      await analyticsApi.deleteFunnelStage(id)
      await this.load()
    },
  },
}
</script>
