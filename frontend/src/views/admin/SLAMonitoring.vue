<template>
  <div class="sla-monitoring p-4">
    <!-- Header -->
    <div class="mt-2 w-full">
      <div class="lg:flex grid-cols-1 lg:space-y-0 space-y-3 gap-5 justify-between">
        <div>
          <p class="uppercase text-xs text-gray-700 dark:text-gray-400 font-semibold">Admin</p>
          <h1 class="text-2xl text-gray-900 dark:text-gray-200 font-medium">
            SLA Monitoring
          </h1>
        </div>
        <div class="flex gap-2">
          <button
            @click="createDefaultConfigs"
            class="bg-white dark:bg-gray-800 hover:border-gray-200 dark:hover:bg-gray-700 dark:text-white dark:border-gray-700 border rounded py-2 px-5 flex items-center gap-2"
          >
            <Icon icon="mdi:refresh" class="text-lg" />
            Create Defaults
          </button>
          <button
            @click="showCreateModal = true"
            class="bg-primary border flex gap-2 text-white hover:bg-primary/80 dark:border-gray-700 rounded py-3 px-5"
          >
            <span class="icon text-2xl"><Icon icon="ic:twotone-plus" /></span>
            <span class="text">Add Configuration</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Statistics Cards -->
    <div class="wrapper-card grid lg:grid-cols-4 grid-cols-1 md:grid-cols-2 gap-4 mt-6">
      <div class="card bg-white dark:bg-gray-800 w-full rounded-md p-5 border dark:border-gray-700 flex">
        <div class="p-2 max-w-sm">
          <div class="bg-blue-200 rounded-full w-14 h-14 text-lg p-3 text-blue-600 mx-auto">
            <Icon icon="mdi:cog" class="text-2xl" />
          </div>
        </div>
        <div class="block p-2 w-full">
          <p class="font-semibold text-gray-900 dark:text-gray-200 text-xl">
            {{ configs.length }}
          </p>
          <h2 class="font-normal text-gray-400 text-md mt-1">Total Configurations</h2>
          <div class="flex items-center mt-2">
            <span class="text-gray-400 text-sm">All SLA configs</span>
          </div>
        </div>
      </div>
      <div class="card bg-white dark:bg-gray-800 w-full rounded-md p-5 border dark:border-gray-700 flex">
        <div class="p-2 max-w-sm">
          <div class="bg-green-200 rounded-full w-14 h-14 text-lg p-3 text-green-600 mx-auto">
            <Icon icon="mdi:check-circle" class="text-2xl" />
          </div>
        </div>
        <div class="block p-2 w-full">
          <p class="font-semibold text-gray-900 dark:text-gray-200 text-xl">
            {{ activeConfigsCount }}
          </p>
          <h2 class="font-normal text-gray-400 text-md mt-1">Active Configs</h2>
          <div class="flex items-center mt-2">
            <span class="text-gray-400 text-sm">Currently active</span>
          </div>
        </div>
      </div>
      <div class="card bg-white dark:bg-gray-800 w-full rounded-md p-5 border dark:border-gray-700 flex">
        <div class="p-2 max-w-sm">
          <div class="bg-purple-200 rounded-full w-14 h-14 text-lg p-3 text-purple-600 mx-auto">
            <Icon icon="mdi:clock" class="text-2xl" />
          </div>
        </div>
        <div class="block p-2 w-full">
          <p class="font-semibold text-gray-900 dark:text-gray-200 text-xl">
            {{ avgResponseTime }}m
          </p>
          <h2 class="font-normal text-gray-400 text-md mt-1">Avg Response Time</h2>
          <div class="flex items-center mt-2">
            <span class="text-gray-400 text-sm">Average response</span>
          </div>
        </div>
      </div>
      <div class="card bg-white dark:bg-gray-800 w-full rounded-md p-5 border dark:border-gray-700 flex">
        <div class="p-2 max-w-sm">
          <div class="bg-orange-200 rounded-full w-14 h-14 text-lg p-3 text-orange-600 mx-auto">
            <Icon icon="mdi:alert" class="text-2xl" />
          </div>
        </div>
        <div class="block p-2 w-full">
          <p class="font-semibold text-gray-900 dark:text-gray-200 text-xl">
            {{ maxBreachCount }}
          </p>
          <h2 class="font-normal text-gray-400 text-md mt-1">Max Breach Count</h2>
          <div class="flex items-center mt-2">
            <span class="text-gray-400 text-sm">Highest breach limit</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Configurations List -->
    <div v-if="loading" class="p-8 text-center mt-6">
      <Icon icon="mdi:loading" class="animate-spin text-6xl text-gray-300 dark:text-gray-600 mx-auto" />
      <p class="mt-2 text-gray-500 dark:text-gray-400">Loading SLA configurations...</p>
    </div>

    <div v-else-if="configs.length === 0" class="p-8 text-center mt-6">
      <Icon icon="mdi:clock-outline" class="text-6xl text-gray-300 dark:text-gray-600 mx-auto" />
      <p class="mt-2 text-sm text-gray-500 dark:text-gray-400">No SLA configurations found.</p>
      <button
        @click="createDefaultConfigs"
        class="mt-4 px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/80"
      >
        Create Default Configurations
      </button>
    </div>

    <div v-else class="space-y-4 mt-6">
      <div
        v-for="config in configs"
        :key="config.id"
        :class="[
          'p-4 border rounded-lg',
          config.active ? 'border-blue-200 bg-blue-50 dark:bg-blue-900/20 dark:border-blue-800' : 'border-gray-200 bg-gray-50 dark:bg-gray-700 dark:border-gray-600'
        ]"
      >
        <div class="flex justify-between items-start">
          <div class="flex-1">
            <div class="flex items-center space-x-2">
              <h3 class="font-semibold text-gray-900 dark:text-gray-200">{{ config.customerTier }} Tier</h3>
              <span
                :class="[
                  'px-2 py-1 text-xs rounded-full',
                  config.active ? 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400' : 'bg-gray-100 text-gray-600 dark:bg-gray-700 dark:text-gray-400'
                ]"
              >
                {{ config.active ? 'Active' : 'Inactive' }}
              </span>
            </div>
            <p class="mt-1 text-sm text-gray-600 dark:text-gray-400">{{ config.description }}</p>
            <div class="mt-3 grid grid-cols-3 gap-4 text-sm">
              <div>
                <span class="font-medium text-gray-700 dark:text-gray-300">Expected Response:</span>
                <span class="ml-2 text-blue-600 dark:text-blue-400 font-semibold">{{ config.expectedResponseTime }} minutes</span>
              </div>
              <div>
                <span class="font-medium text-gray-700 dark:text-gray-300">Max Breaches:</span>
                <span class="ml-2 text-orange-600 dark:text-orange-400 font-semibold">{{ config.maxBreachCount }}</span>
              </div>
              <div>
                <span class="font-medium text-gray-700 dark:text-gray-300">Created:</span>
                <span class="ml-2 text-gray-600 dark:text-gray-400">{{ formatDate(config.createdAt) }}</span>
              </div>
            </div>
          </div>
          <div class="flex space-x-2 ml-4">
            <button
              @click="editConfig(config)"
              class="p-2 text-blue-600 hover:bg-blue-100 dark:hover:bg-blue-900/30 rounded-lg transition-colors"
            >
              <Icon icon="mdi:pencil" class="text-lg" />
            </button>
            <button
              @click="toggleConfigStatus(config)"
              :class="[
                'p-2 rounded-lg transition-colors',
                config.active ? 'text-green-600 hover:bg-green-100 dark:hover:bg-green-900/30' : 'text-gray-600 hover:bg-gray-200 dark:hover:bg-gray-600'
              ]"
            >
              <Icon :icon="config.active ? 'mdi:toggle-switch' : 'mdi:toggle-switch-off'" class="text-lg" />
            </button>
            <button
              @click="deleteConfig(config.id)"
              class="p-2 text-red-600 hover:bg-red-100 dark:hover:bg-red-900/30 rounded-lg transition-colors"
            >
              <Icon icon="mdi:delete" class="text-lg" />
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <div
      v-if="showCreateModal || showEditModal"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
    >
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-lg border dark:border-gray-700">
        <h2 class="text-xl font-bold mb-4 text-gray-900 dark:text-gray-200">
          {{ showCreateModal ? 'Create SLA Configuration' : 'Edit SLA Configuration' }}
        </h2>
        <form @submit.prevent="saveConfig">
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Customer Tier</label>
              <select
                v-model="currentConfig.customerTier"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded-lg focus:ring-2 focus:ring-blue-500"
                required
              >
                <option value="Standard">Standard</option>
                <option value="VIP">VIP</option>
                <option value="Enterprise">Enterprise</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Description</label>
              <textarea
                v-model="currentConfig.description"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded-lg focus:ring-2 focus:ring-blue-500"
                rows="2"
              />
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Expected Response Time (minutes)</label>
                <input
                  v-model.number="currentConfig.expectedResponseTime"
                  type="number"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded-lg focus:ring-2 focus:ring-blue-500"
                  required
                  min="1"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Max Breach Count</label>
                <input
                  v-model.number="currentConfig.maxBreachCount"
                  type="number"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded-lg focus:ring-2 focus:ring-blue-500"
                  required
                  min="1"
                />
              </div>
            </div>
            <div class="flex items-center">
              <input
                v-model="currentConfig.active"
                type="checkbox"
                id="active"
                class="w-4 h-4 text-blue-600 border-gray-300 dark:border-gray-600 rounded focus:ring-blue-500 dark:bg-gray-700"
              />
              <label for="active" class="ml-2 text-sm text-gray-700 dark:text-gray-300">Active</label>
            </div>
          </div>
          <div class="flex justify-end space-x-2 mt-6">
            <button
              type="button"
              @click="closeModal"
              class="px-4 py-2 bg-gray-200 dark:bg-gray-700 text-gray-800 dark:text-gray-200 rounded-lg hover:bg-gray-300 dark:hover:bg-gray-600"
            >
              Cancel
            </button>
            <button
              type="submit"
              class="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/80"
            >
              Save
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import slaConfigurationApi from '@/api/slaConfigurationApi'

const loading = ref(false)
const configs = ref([])
const showCreateModal = ref(false)
const showEditModal = ref(false)
const currentConfig = ref({
  customerTier: 'Standard',
  description: '',
  expectedResponseTime: 30,
  maxBreachCount: 3,
  active: true
})

const activeConfigsCount = computed(() => {
  return configs.value.filter(c => c.active).length
})

const avgResponseTime = computed(() => {
  if (configs.value.length === 0) return 0
  const total = configs.value.reduce((sum, c) => sum + c.expectedResponseTime, 0)
  return Math.round(total / configs.value.length)
})

const maxBreachCount = computed(() => {
  if (configs.value.length === 0) return 0
  return Math.max(...configs.value.map(c => c.maxBreachCount))
})

const loadConfigs = async () => {
  loading.value = true
  try {
    const response = await slaConfigurationApi.getSLAConfigurations()
    configs.value = response.data || []
  } catch (error) {
    console.error('Error loading SLA configurations:', error)
  } finally {
    loading.value = false
  }
}

const createDefaultConfigs = async () => {
  try {
    await slaConfigurationApi.createDefaultSLAConfigurations()
    await loadConfigs()
  } catch (error) {
    console.error('Error creating default configurations:', error)
  }
}

const editConfig = (config) => {
  currentConfig.value = { ...config }
  showEditModal.value = true
}

const toggleConfigStatus = async (config) => {
  try {
    const updatedConfig = { ...config, active: !config.active }
    await slaConfigurationApi.updateSLAConfiguration(config.id, updatedConfig)
    await loadConfigs()
  } catch (error) {
    console.error('Error toggling config status:', error)
  }
}

const deleteConfig = async (id) => {
  if (confirm('Are you sure you want to delete this configuration?')) {
    try {
      await slaConfigurationApi.deleteSLAConfiguration(id)
      await loadConfigs()
    } catch (error) {
      console.error('Error deleting configuration:', error)
    }
  }
}

const saveConfig = async () => {
  try {
    if (showCreateModal.value) {
      await slaConfigurationApi.createSLAConfiguration(currentConfig.value)
    } else {
      await slaConfigurationApi.updateSLAConfiguration(currentConfig.value.id, currentConfig.value)
    }
    
    closeModal()
    await loadConfigs()
  } catch (error) {
    console.error('Error saving configuration:', error)
  }
}

const closeModal = () => {
  showCreateModal.value = false
  showEditModal.value = false
  currentConfig.value = {
    customerTier: 'Standard',
    description: '',
    expectedResponseTime: 30,
    maxBreachCount: 3,
    active: true
  }
}

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString()
}

onMounted(() => {
  loadConfigs()
})
</script>
