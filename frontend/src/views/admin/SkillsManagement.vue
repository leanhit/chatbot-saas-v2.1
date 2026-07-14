<template>
  <div class="skills-management p-4">
    <!-- Header -->
    <div class="mt-2 w-full">
      <div class="lg:flex grid-cols-1 lg:space-y-0 space-y-3 gap-5 justify-between">
        <div>
          <p class="uppercase text-xs text-gray-700 dark:text-gray-400 font-semibold">Admin</p>
          <h1 class="text-2xl text-gray-900 dark:text-gray-200 font-medium">
            Skills Management
          </h1>
        </div>
        <div class="flex gap-2">
          <button
            @click="loadAgents"
            :disabled="loading"
            class="bg-white dark:bg-gray-800 hover:border-gray-200 dark:hover:bg-gray-700 dark:text-white dark:border-gray-700 border rounded py-2 px-5 flex items-center gap-2"
          >
            <Icon icon="mdi:refresh" :class="{'animate-spin': loading}" class="text-lg" />
            Refresh
          </button>
        </div>
      </div>
    </div>

    <!-- Skills Overview -->
    <div v-if="loading" class="p-8 text-center">
      <Icon icon="mdi:loading" class="animate-spin text-6xl text-gray-300 dark:text-gray-600 mx-auto" />
      <p class="mt-2 text-gray-500 dark:text-gray-400">Loading skills data...</p>
    </div>

    <div v-else class="space-y-6 mt-6">
      <!-- Skills Statistics -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div class="card bg-white dark:bg-gray-800 w-full rounded-md p-5 border dark:border-gray-700 flex">
          <div class="p-2 max-w-sm">
            <div class="bg-blue-200 rounded-full w-14 h-14 text-lg p-3 text-blue-600 mx-auto">
              <Icon icon="mdi:tag" class="text-2xl" />
            </div>
          </div>
          <div class="block p-2 w-full">
            <p class="font-semibold text-gray-900 dark:text-gray-200 text-xl">
              {{ uniqueSkills.length }}
            </p>
            <h2 class="font-normal text-gray-400 text-md mt-1">Total Skills</h2>
            <div class="flex items-center mt-2">
              <span class="text-gray-400 text-sm">All unique skills</span>
            </div>
          </div>
        </div>
        <div class="card bg-white dark:bg-gray-800 w-full rounded-md p-5 border dark:border-gray-700 flex">
          <div class="p-2 max-w-sm">
            <div class="bg-green-200 rounded-full w-14 h-14 text-lg p-3 text-green-600 mx-auto">
              <Icon icon="mdi:account-check" class="text-2xl" />
            </div>
          </div>
          <div class="block p-2 w-full">
            <p class="font-semibold text-gray-900 dark:text-gray-200 text-xl">
              {{ agentsWithSkillsCount }}
            </p>
            <h2 class="font-normal text-gray-400 text-md mt-1">Agents with Skills</h2>
            <div class="flex items-center mt-2">
              <span class="text-gray-400 text-sm">Skilled agents</span>
            </div>
          </div>
        </div>
        <div class="card bg-white dark:bg-gray-800 w-full rounded-md p-5 border dark:border-gray-700 flex">
          <div class="p-2 max-w-sm">
            <div class="bg-purple-200 rounded-full w-14 h-14 text-lg p-3 text-purple-600 mx-auto">
              <Icon icon="mdi:trending-up" class="text-2xl" />
            </div>
          </div>
          <div class="block p-2 w-full">
            <p class="font-semibold text-gray-900 dark:text-gray-200 text-xl">
              {{ mostCommonSkill || 'N/A' }}
            </p>
            <h2 class="font-normal text-gray-400 text-md mt-1">Most Common Skill</h2>
            <div class="flex items-center mt-2">
              <span class="text-gray-400 text-sm">Highest frequency</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Skills List -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm border dark:border-gray-700">
        <div class="p-4 border-b dark:border-gray-700">
          <h2 class="text-lg font-semibold text-gray-900 dark:text-gray-200">All Skills</h2>
        </div>
        <div class="p-4">
          <div v-if="uniqueSkills.length === 0" class="text-center py-8">
            <Icon icon="mdi:tag-off" class="text-6xl text-gray-300 dark:text-gray-600 mx-auto" />
            <p class="mt-2 text-sm text-gray-500 dark:text-gray-400">No skills found. Add skills to agents in Agent Management.</p>
          </div>
          <div v-else class="flex flex-wrap gap-2">
            <div
              v-for="skill in uniqueSkills"
              :key="skill"
              class="px-4 py-2 bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-400 rounded-full flex items-center space-x-2"
            >
              <span class="font-medium">{{ skill }}</span>
              <span class="text-xs bg-blue-200 dark:bg-blue-800 px-2 py-1 rounded-full">
                {{ getSkillCount(skill) }} agents
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Agents by Skill -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm border dark:border-gray-700">
        <div class="p-4 border-b dark:border-gray-700">
          <h2 class="text-lg font-semibold text-gray-900 dark:text-gray-200">Agents by Skill</h2>
        </div>
        <div class="p-4">
          <div v-if="uniqueSkills.length === 0" class="text-center py-8">
            <p class="text-sm text-gray-500 dark:text-gray-400">No skills data available.</p>
          </div>
          <div v-else class="space-y-4">
            <div
              v-for="skill in uniqueSkills"
              :key="skill"
              class="border-l-4 border-blue-500 pl-4"
            >
              <h3 class="font-semibold text-gray-900 dark:text-gray-200 mb-2">{{ skill }}</h3>
              <div class="flex flex-wrap gap-2">
                <div
                  v-for="agent in getAgentsBySkill(skill)"
                  :key="agent.id"
                  class="px-3 py-1 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg text-sm flex items-center space-x-2"
                >
                  <div class="w-6 h-6 rounded-full bg-blue-600 text-white flex items-center justify-center text-xs font-bold">
                    {{ agent.name.charAt(0).toUpperCase() }}
                  </div>
                  <span>{{ agent.name }}</span>
                  <span
                    :class="[
                      'px-1 py-0.5 text-xs rounded',
                      agent.status === 'ONLINE' ? 'bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-400' : 'bg-gray-200 dark:bg-gray-600 text-gray-600 dark:text-gray-400'
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
      <div class="bg-blue-50 dark:bg-blue-900/20 rounded-lg p-6 border border-blue-200 dark:border-blue-800">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="font-semibold text-blue-900 dark:text-blue-400">Need to assign skills to agents?</h3>
            <p class="text-sm text-blue-700 dark:text-blue-500 mt-1">Go to Agent Management to add or edit agent skills.</p>
          </div>
          <button
            @click="goToAgentManagement"
            class="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/80 transition-colors"
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

const uniqueSkills = computed(() => {
  const skills = new Set()
  agents.value.forEach(agent => {
    if (agent.skills && Array.isArray(agent.skills)) {
      agent.skills.forEach(skill => skills.add(skill))
    }
  })
  return Array.from(skills).sort()
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
  router.push({ name: 'admin-agents' })
}

onMounted(() => {
  loadAgents()
})
</script>
