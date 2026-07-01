<template>
  <div class="skills-management">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-900">Skills Management</h1>
      <div class="flex items-center space-x-4">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Search skills or agents..."
          class="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
        />
      </div>
    </div>

    <!-- Skills Overview -->
    <div v-if="loading" class="p-8 text-center">
      <Icon icon="mdi:loading" class="animate-spin text-2xl text-gray-400" />
      <p class="mt-2 text-sm text-gray-500">Loading skills data...</p>
    </div>

    <div v-else class="space-y-6">
      <!-- Skills Statistics -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div class="bg-white p-4 rounded-lg shadow-sm border border-gray-200">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-600">Total Skills</p>
              <p class="text-2xl font-bold text-gray-900">{{ uniqueSkills.length }}</p>
            </div>
            <Icon icon="mdi:tag" class="text-2xl text-blue-600" />
          </div>
        </div>
        <div class="bg-white p-4 rounded-lg shadow-sm border border-gray-200">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-600">Agents with Skills</p>
              <p class="text-2xl font-bold text-green-600">{{ agentsWithSkillsCount }}</p>
            </div>
            <Icon icon="mdi:account-check" class="text-2xl text-green-600" />
          </div>
        </div>
        <div class="bg-white p-4 rounded-lg shadow-sm border border-gray-200">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-600">Most Common Skill</p>
              <p class="text-2xl font-bold text-purple-600">{{ mostCommonSkill || 'N/A' }}</p>
            </div>
            <Icon icon="mdi:trending-up" class="text-2xl text-purple-600" />
          </div>
        </div>
      </div>

      <!-- Skills List -->
      <div class="bg-white rounded-lg shadow-sm border border-gray-200">
        <div class="p-4 border-b border-gray-200">
          <h2 class="text-lg font-semibold text-gray-900">All Skills</h2>
        </div>
        <div class="p-4">
          <div v-if="uniqueSkills.length === 0" class="text-center py-8">
            <Icon icon="mdi:tag-off" class="text-4xl text-gray-300 mx-auto" />
            <p class="mt-2 text-sm text-gray-500">No skills found. Add skills to agents in Agent Management.</p>
          </div>
          <div v-else class="flex flex-wrap gap-2">
            <div
              v-for="skill in filteredSkills"
              :key="skill"
              class="px-4 py-2 bg-blue-100 text-blue-800 rounded-full flex items-center space-x-2"
            >
              <span class="font-medium">{{ skill }}</span>
              <span class="text-xs bg-blue-200 px-2 py-1 rounded-full">
                {{ getSkillCount(skill) }} agents
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Agents by Skill -->
      <div class="bg-white rounded-lg shadow-sm border border-gray-200">
        <div class="p-4 border-b border-gray-200">
          <h2 class="text-lg font-semibold text-gray-900">Agents by Skill</h2>
        </div>
        <div class="p-4">
          <div v-if="uniqueSkills.length === 0" class="text-center py-8">
            <p class="text-sm text-gray-500">No skills data available.</p>
          </div>
          <div v-else class="space-y-4">
            <div
              v-for="skill in filteredSkills"
              :key="skill"
              class="border-l-4 border-blue-500 pl-4"
            >
              <h3 class="font-semibold text-gray-900 mb-2">{{ skill }}</h3>
              <div class="flex flex-wrap gap-2">
                <div
                  v-for="agent in getAgentsBySkill(skill)"
                  :key="agent.id"
                  class="px-3 py-1 bg-gray-100 text-gray-700 rounded-lg text-sm flex items-center space-x-2"
                >
                  <div class="w-6 h-6 rounded-full bg-blue-600 text-white flex items-center justify-center text-xs font-bold">
                    {{ agent.name.charAt(0).toUpperCase() }}
                  </div>
                  <span>{{ agent.name }}</span>
                  <span
                    :class="[
                      'px-1 py-0.5 text-xs rounded',
                      agent.status === 'ONLINE' ? 'bg-green-100 text-green-800' : 'bg-gray-200 text-gray-600'
                    ]"
                  >
                    {{ agent.status }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Quick Assign (Link to Agent Management) -->
      <div class="bg-blue-50 rounded-lg p-6 border border-blue-200">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="font-semibold text-blue-900">Need to assign skills to agents?</h3>
            <p class="text-sm text-blue-700 mt-1">Go to Agent Management to add or edit agent skills.</p>
          </div>
          <button
            @click="goToAgentManagement"
            class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            <Icon icon="mdi:arrow-right" class="inline-block mr-1" />
            Go to Agent Management
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import { useRouter } from 'vue-router'
import agentApi from '@/api/agentApi'

const router = useRouter()
const loading = ref(false)
const agents = ref([])
const searchQuery = ref('')

const uniqueSkills = computed(() => {
  const skills = new Set()
  agents.value.forEach(agent => {
    if (agent.skills && Array.isArray(agent.skills)) {
      agent.skills.forEach(skill => skills.add(skill))
    }
  })
  return Array.from(skills).sort()
})

const filteredSkills = computed(() => {
  if (!searchQuery.value) return uniqueSkills.value
  const query = searchQuery.value.toLowerCase()
  return uniqueSkills.value.filter(skill => 
    skill.toLowerCase().includes(query)
  )
})

const agentsWithSkillsCount = computed(() => {
  return agents.value.filter(agent => 
    agent.skills && agent.skills.length > 0
  ).length
})

const mostCommonSkill = computed(() => {
  const skillCounts = {}
  agents.value.forEach(agent => {
    if (agent.skills && Array.isArray(agent.skills)) {
      agent.skills.forEach(skill => {
        skillCounts[skill] = (skillCounts[skill] || 0) + 1
      })
    }
  })
  
  let maxCount = 0
  let mostCommon = null
  for (const [skill, count] of Object.entries(skillCounts)) {
    if (count > maxCount) {
      maxCount = count
      mostCommon = skill
    }
  }
  return mostCommon
})

const loadAgents = async () => {
  loading.value = true
  try {
    const response = await agentApi.getAgents()
    agents.value = response.data || []
  } catch (error) {
    console.error('Error loading agents:', error)
  } finally {
    loading.value = false
  }
}

const getSkillCount = (skill) => {
  return agents.value.filter(agent => 
    agent.skills && agent.skills.includes(skill)
  ).length
}

const getAgentsBySkill = (skill) => {
  return agents.value.filter(agent => 
    agent.skills && agent.skills.includes(skill)
  )
}

const goToAgentManagement = () => {
  router.push({ name: 'AgentManagement' })
}

onMounted(() => {
  loadAgents()
})
</script>
