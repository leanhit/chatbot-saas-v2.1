<template>
  <div class="skill-matrix p-4">
    <div class="mt-2 w-full">
      <div class="flex flex-wrap justify-between items-center gap-3">
        <div>
          <p class="uppercase text-xs text-gray-700 dark:text-gray-300 font-semibold">Training</p>
          <h1 class="text-2xl text-gray-900 dark:text-gray-200 font-medium">Skill Matrix</h1>
        </div>
        <div class="flex items-center gap-2">
          <label class="text-sm text-gray-600 dark:text-gray-300">Agent:</label>
          <select
            v-model="selectedAgentId"
            class="border rounded-md px-3 py-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
          >
            <option :value="null" disabled>Select an agent</option>
            <option v-for="a in agents" :key="a.agentId" :value="a.agentId">
              {{ a.agentName }} ({{ a.agentEmail }})
            </option>
          </select>
        </div>
      </div>
    </div>

    <div v-if="!selectedAgentId" class="mt-8 text-center text-gray-500">
      <Icon icon="mdi:grid" class="text-5xl text-gray-300" />
      <p class="mt-3">Select an agent to view their skill matrix</p>
    </div>

    <div v-else class="mt-6 bg-white dark:bg-gray-800 rounded-md border dark:border-gray-700">
      <div class="p-4 border-b dark:border-gray-700 flex items-center justify-between">
        <h2 class="text-lg font-semibold text-gray-900 dark:text-white">Skills</h2>
        <button @click="loadSkills" :disabled="loading" class="text-sm text-blue-600 hover:underline flex items-center gap-1">
          <Icon icon="mdi:refresh" :class="{'animate-spin': loading}" /> Refresh
        </button>
      </div>
      <div v-if="loading" class="p-8 text-center text-gray-500">
        <Icon icon="mdi:loading" class="animate-spin text-2xl" />
      </div>
      <div v-else-if="skills.length === 0" class="p-8 text-center text-gray-500">No skill assessments yet.</div>
      <div v-else class="overflow-x-auto">
        <table class="w-full">
          <thead class="bg-gray-50 dark:bg-gray-700">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">Category</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">Score</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">Proficiency</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">Method</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">Assessments</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">Last assessed</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">Update</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200 dark:divide-gray-700">
            <tr v-for="s in skills" :key="s.id" class="hover:bg-gray-50 dark:hover:bg-gray-700">
              <td class="px-4 py-3 font-medium text-gray-900 dark:text-white">{{ formatCategory(s.skillCategory) }}</td>
              <td class="px-4 py-3">
                <div class="flex items-center gap-2">
                  <div class="w-24 bg-gray-200 rounded-full h-2 dark:bg-gray-700">
                    <div class="h-2 rounded-full" :class="scoreBarClass(s.score)" :style="{ width: (s.score || 0) + '%' }"></div>
                  </div>
                  <span class="text-sm text-gray-700 dark:text-gray-300">{{ s.score ?? '—' }}</span>
                </div>
              </td>
              <td class="px-4 py-3">
                <span class="px-2 py-1 text-xs rounded-full" :class="proficiencyClass(s.proficiencyLevel)">
                  {{ s.proficiencyLevel || '—' }}
                </span>
              </td>
              <td class="px-4 py-3 text-sm text-gray-600 dark:text-gray-300">{{ s.assessmentMethod || '—' }}</td>
              <td class="px-4 py-3 text-sm text-gray-600 dark:text-gray-300">{{ s.assessmentCount ?? 0 }}</td>
              <td class="px-4 py-3 text-sm text-gray-600 dark:text-gray-300">{{ formatDate(s.lastAssessedAt) }}</td>
              <td class="px-4 py-3">
                <div class="flex items-center gap-1">
                  <input
                    v-model.number="editScores[s.id]"
                    type="number" min="0" max="100"
                    class="w-16 border rounded px-2 py-1 text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                    placeholder="0-100"
                  />
                  <button
                    @click="saveScore(s)"
                    :disabled="saving === s.id || editScores[s.id] == null"
                    class="text-sm px-2 py-1 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
                  >Save</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import { trainingApi } from '@/api/trainingApi'

const agents = ref([])
const selectedAgentId = ref(null)
const skills = ref([])
const loading = ref(false)
const saving = ref(null)
const editScores = ref({})

function formatCategory(c) {
  return (c || '').replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase())
}
function proficiencyClass(level) {
  return {
    BEGINNER: 'bg-gray-100 text-gray-700 dark:bg-gray-700 dark:text-gray-200',
    INTERMEDIATE: 'bg-blue-100 text-blue-800 dark:bg-blue-900/40 dark:text-blue-200',
    ADVANCED: 'bg-purple-100 text-purple-800 dark:bg-purple-900/40 dark:text-purple-200',
    EXPERT: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/40 dark:text-yellow-200',
  }[level] || 'bg-gray-100 text-gray-600'
}
function scoreBarClass(score) {
  if (score >= 80) return 'bg-green-500'
  if (score >= 50) return 'bg-blue-500'
  return 'bg-yellow-500'
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

async function loadSkills() {
  loading.value = true
  try {
    const { data } = await trainingApi.getAgentSkills(selectedAgentId.value)
    skills.value = Array.isArray(data) ? data : []
    editScores.value = {}
  } catch (e) {
    console.error('Failed to load skills', e)
    skills.value = []
  } finally {
    loading.value = false
  }
}

async function saveScore(skill) {
  const score = editScores.value[skill.id]
  if (score == null || score < 0 || score > 100) return
  saving.value = skill.id
  try {
    await trainingApi.updateAgentSkill(skill.id, { ...skill, score })
    await loadSkills()
  } catch (e) {
    console.error('Failed to update skill', e)
  } finally {
    saving.value = null
  }
}

watch(selectedAgentId, (id) => { if (id) loadSkills() })
onMounted(loadAgents)
</script>
