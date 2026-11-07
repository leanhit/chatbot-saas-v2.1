<template>
  <div class="certification-overview p-4">
    <div class="mt-2 w-full">
      <div class="flex flex-wrap justify-between items-center gap-3">
        <div>
          <p class="uppercase text-xs text-gray-700 dark:text-gray-300 font-semibold">Training</p>
          <h1 class="text-2xl text-gray-900 dark:text-gray-200 font-medium">Certification Overview</h1>
        </div>
        <div class="flex items-center gap-2">
          <label class="text-sm text-gray-600 dark:text-gray-300">Agent:</label>
          <select
            v-model="selectedAgentId"
            class="border rounded-md px-3 py-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
          >
            <option :value="null">— All agents —</option>
            <option v-for="a in agents" :key="a.agentId" :value="a.agentId">
              {{ a.agentName }} ({{ a.agentEmail }})
            </option>
          </select>
          <button
            v-if="selectedAgentId"
            @click="showAwardModal = true"
            class="px-3 py-2 bg-blue-600 text-white rounded-md text-sm hover:bg-blue-700 flex items-center gap-1"
          >
            <Icon icon="mdi:plus" /> Award
          </button>
        </div>
      </div>
    </div>

    <!-- Certification levels -->
    <div class="mt-6 bg-white dark:bg-gray-800 rounded-md border dark:border-gray-700">
      <div class="p-4 border-b dark:border-gray-700">
        <h2 class="text-lg font-semibold text-gray-900 dark:text-white">Levels</h2>
      </div>
      <div v-if="loadingLevels" class="p-8 text-center text-gray-500">
        <Icon icon="mdi:loading" class="animate-spin text-2xl" />
      </div>
      <div v-else-if="levels.length === 0" class="p-8 text-center text-gray-500">No certification levels defined.</div>
      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 p-4">
        <div
          v-for="level in levels"
          :key="level.id"
          class="border dark:border-gray-700 rounded-lg p-4"
          :style="level.badgeColor ? { borderLeft: `4px solid ${level.badgeColor}` } : {}"
        >
          <div class="flex items-center gap-2">
            <Icon :icon="level.badgeIcon || 'mdi:certificate'" class="text-2xl" :style="level.badgeColor ? { color: level.badgeColor } : {}" />
            <h3 class="font-semibold text-gray-900 dark:text-white">{{ level.levelName }}</h3>
          </div>
          <p v-if="level.description" class="text-sm text-gray-500 dark:text-gray-400 mt-2">{{ level.description }}</p>
          <p v-if="level.validityDays" class="text-xs text-gray-400 mt-2">Valid for {{ level.validityDays }} days</p>
        </div>
      </div>
    </div>

    <!-- Agent certifications -->
    <div v-if="selectedAgentId" class="mt-6 bg-white dark:bg-gray-800 rounded-md border dark:border-gray-700">
      <div class="p-4 border-b dark:border-gray-700 flex items-center justify-between">
        <h2 class="text-lg font-semibold text-gray-900 dark:text-white">Agent Certifications</h2>
        <button @click="loadCertifications" :disabled="loadingCerts" class="text-sm text-blue-600 hover:underline flex items-center gap-1">
          <Icon icon="mdi:refresh" :class="{'animate-spin': loadingCerts}" /> Refresh
        </button>
      </div>
      <div v-if="loadingCerts" class="p-8 text-center text-gray-500">
        <Icon icon="mdi:loading" class="animate-spin text-2xl" />
      </div>
      <div v-else-if="certifications.length === 0" class="p-8 text-center text-gray-500">No certifications awarded yet.</div>
      <table v-else class="w-full">
        <thead class="bg-gray-50 dark:bg-gray-700">
          <tr>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">Level</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">Status</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">Certificate #</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">Issued</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">Expires</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">Issued by</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200 dark:divide-gray-700">
          <tr v-for="c in certifications" :key="c.id" class="hover:bg-gray-50 dark:hover:bg-gray-700">
            <td class="px-4 py-3 font-medium text-gray-900 dark:text-white">
              {{ levelName(c.certificationLevelId) }}
            </td>
            <td class="px-4 py-3">
              <span class="px-2 py-1 text-xs rounded-full" :class="certStatusClass(c.status)">{{ c.status }}</span>
            </td>
            <td class="px-4 py-3 text-sm text-gray-600 dark:text-gray-300">{{ c.certificateNumber || '—' }}</td>
            <td class="px-4 py-3 text-sm text-gray-600 dark:text-gray-300">{{ formatDate(c.issuedAt) }}</td>
            <td class="px-4 py-3 text-sm text-gray-600 dark:text-gray-300">{{ formatDate(c.expiresAt) }}</td>
            <td class="px-4 py-3 text-sm text-gray-600 dark:text-gray-300">{{ c.issuedBy || '—' }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Award modal -->
    <div v-if="showAwardModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-md">
        <h3 class="text-lg font-semibold mb-4 text-gray-900 dark:text-white">Award Certification</h3>
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium mb-1 text-gray-700 dark:text-gray-300">Level</label>
            <select v-model="awardForm.levelId" class="w-full border rounded-md px-3 py-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white">
              <option :value="null" disabled>Select level</option>
              <option v-for="l in levels" :key="l.id" :value="l.id">{{ l.levelName }}</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium mb-1 text-gray-700 dark:text-gray-300">Issued by</label>
            <input v-model="awardForm.issuedBy" type="text" class="w-full border rounded-md px-3 py-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white" placeholder="Your name or email" />
          </div>
        </div>
        <div class="flex justify-end gap-2 mt-6">
          <button @click="showAwardModal = false" class="px-4 py-2 border rounded-md text-sm dark:border-gray-600 dark:text-gray-300">Cancel</button>
          <button
            @click="award"
            :disabled="awarding || !awardForm.levelId || !awardForm.issuedBy"
            class="px-4 py-2 bg-blue-600 text-white rounded-md text-sm hover:bg-blue-700 disabled:opacity-50"
          >{{ awarding ? 'Awarding...' : 'Award' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { trainingApi } from '@/api/trainingApi'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
const agents = ref([])
const selectedAgentId = ref(null)
const levels = ref([])
const certifications = ref([])
const loadingLevels = ref(false)
const loadingCerts = ref(false)
const showAwardModal = ref(false)
const awarding = ref(false)
const awardForm = ref({ levelId: null, issuedBy: '' })

function levelName(id) {
  return levels.value.find(l => l.id === id)?.levelName || `Level #${id}`
}
function certStatusClass(status) {
  return {
    ACTIVE: 'bg-green-100 text-green-800 dark:bg-green-900/40 dark:text-green-200',
    EXPIRED: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/40 dark:text-yellow-200',
    REVOKED: 'bg-red-100 text-red-800 dark:bg-red-900/40 dark:text-red-200',
  }[status] || 'bg-gray-100 text-gray-600'
}
function formatDate(v) {
  if (!v) return '—'
  const d = new Date(v)
  return isNaN(d) ? v : d.toLocaleDateString()
}

async function loadAgents() {
  try {
    const { data } = await trainingApi.getAgentsWithTrainingStats()
    agents.value = Array.isArray(data) ? data : []
  } catch (e) {
    console.error('Failed to load agents', e)
  }
}

async function loadLevels() {
  loadingLevels.value = true
  try {
    const { data } = await trainingApi.getCertificationLevels()
    levels.value = Array.isArray(data) ? data : []
  } catch (e) {
    console.error('Failed to load levels', e)
  } finally {
    loadingLevels.value = false
  }
}

async function loadCertifications() {
  if (!selectedAgentId.value) return
  loadingCerts.value = true
  try {
    const { data } = await trainingApi.getAgentCertifications(selectedAgentId.value)
    certifications.value = Array.isArray(data) ? data : []
  } catch (e) {
    console.error('Failed to load certifications', e)
  } finally {
    loadingCerts.value = false
  }
}

async function award() {
  awarding.value = true
  try {
    await trainingApi.awardCertification({
      agentId: selectedAgentId.value,
      certificationLevelId: awardForm.value.levelId,
      issuedBy: awardForm.value.issuedBy,
    })
    showAwardModal.value = false
    awardForm.value = { levelId: null, issuedBy: '' }
    await loadCertifications()
  } catch (e) {
    console.error('Award failed', e)
  } finally {
    awarding.value = false
  }
}

watch(selectedAgentId, (id) => { if (id) loadCertifications() })

onMounted(() => {
  loadAgents()
  loadLevels()
  awardForm.value.issuedBy = authStore.user?.email || ''
})
</script>
