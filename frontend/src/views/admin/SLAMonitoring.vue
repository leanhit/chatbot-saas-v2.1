<template>
  <div class="sla-monitoring">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-900">SLA Monitoring</h1>
      <div class="space-x-2">
        <button
          @click="createDefaultConfigs"
          class="px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 transition-colors"
        >
          <Icon icon="mdi:refresh" class="inline-block mr-1" />
          Create Defaults
        </button>
        <button
          @click="showCreateModal = true"
          class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          <Icon icon="mdi:plus" class="inline-block mr-1" />
          Add Configuration
        </button>
      </div>
    </div>

    <!-- Statistics Cards -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
      <div class="bg-white p-4 rounded-lg shadow-sm border border-gray-200">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-600">Total Configurations</p>
            <p class="text-2xl font-bold text-gray-900">{{ configs.length }}</p>
          </div>
          <Icon icon="mdi:cog" class="text-2xl text-blue-600" />
        </div>
      </div>
      <div class="bg-white p-4 rounded-lg shadow-sm border border-gray-200">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-600">Active Configs</p>
            <p class="text-2xl font-bold text-green-600">{{ activeConfigsCount }}</p>
          </div>
          <Icon icon="mdi:check-circle" class="text-2xl text-green-600" />
        </div>
      </div>
      <div class="bg-white p-4 rounded-lg shadow-sm border border-gray-200">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-600">Avg Response Time</p>
            <p class="text-2xl font-bold text-purple-600">{{ avgResponseTime }}m</p>
          </div>
          <Icon icon="mdi:clock" class="text-2xl text-purple-600" />
        </div>
      </div>
      <div class="bg-white p-4 rounded-lg shadow-sm border border-gray-200">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-600">Max Breach Count</p>
            <p class="text-2xl font-bold text-orange-600">{{ maxBreachCount }}</p>
          </div>
          <Icon icon="mdi:alert" class="text-2xl text-orange-600" />
        </div>
      </div>
    </div>

    <!-- Configurations List -->
    <div v-if="loading" class="p-8 text-center">
      <Icon icon="mdi:loading" class="animate-spin text-2xl text-gray-400" />
      <p class="mt-2 text-sm text-gray-500">Loading SLA configurations...</p>
    </div>

    <div v-else-if="configs.length === 0" class="p-8 text-center">
      <Icon icon="mdi:clock-outline" class="text-4xl text-gray-300 mx-auto" />
      <p class="mt-2 text-sm text-gray-500">No SLA configurations found.</p>
      <button
        @click="createDefaultConfigs"
        class="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
      >
        Create Default Configurations
      </button>
    </div>

    <div v-else class="space-y-4">
      <div
        v-for="config in configs"
        :key="config.id"
        :class="[
          'p-4 border rounded-lg',
          config.active ? 'border-blue-200 bg-blue-50' : 'border-gray-200 bg-gray-50'
        ]"
      >
        <div class="flex justify-between items-start">
          <div class="flex-1">
            <div class="flex items-center space-x-2">
              <h3 class="font-semibold text-gray-900">{{ config.customerTier }} Tier</h3>
              <span
                :class="[
                  'px-2 py-1 text-xs rounded-full',
                  config.active ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-600'
                ]"
              >
                {{ config.active ? 'Active' : 'Inactive' }}
              </span>
            </div>
            <p class="mt-1 text-sm text-gray-600">{{ config.description }}</p>
            <div class="mt-3 grid grid-cols-3 gap-4 text-sm">
              <div>
                <span class="font-medium text-gray-700">Expected Response:</span>
                <span class="ml-2 text-blue-600 font-semibold">{{ config.expectedResponseTime }} minutes</span>
              </div>
              <div>
                <span class="font-medium text-gray-700">Max Breaches:</span>
                <span class="ml-2 text-orange-600 font-semibold">{{ config.maxBreachCount }}</span>
              </div>
              <div>
                <span class="font-medium text-gray-700">Created:</span>
                <span class="ml-2 text-gray-600">{{ formatDate(config.createdAt) }}</span>
              </div>
            </div>
          </div>
          <div class="flex space-x-2 ml-4">
            <button
              @click="editConfig(config)"
              class="p-2 text-blue-600 hover:bg-blue-100 rounded-lg transition-colors"
            >
              <Icon icon="mdi:pencil" class="text-lg" />
            </button>
            <button
              @click="toggleConfigStatus(config)"
              :class="[
                'p-2 rounded-lg transition-colors',
                config.active ? 'text-green-600 hover:bg-green-100' : 'text-gray-600 hover:bg-gray-200'
              ]"
            >
              <Icon :icon="config.active ? 'mdi:toggle-switch' : 'mdi:toggle-switch-off'" class="text-lg" />
            </button>
            <button
              @click="deleteConfig(config.id)"
              class="p-2 text-red-600 hover:bg-red-100 rounded-lg transition-colors"
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
      <div class="bg-white rounded-lg p-6 w-full max-w-lg">
        <h2 class="text-xl font-bold mb-4">
          {{ showCreateModal ? 'Create SLA Configuration' : 'Edit SLA Configuration' }}
        </h2>
        <form @submit.prevent="saveConfig">
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Customer Tier</label>
              <select
                v-model="currentConfig.customerTier"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                required
              >
                <option value="Standard">Standard</option>
                <option value="VIP">VIP</option>
                <option value="Enterprise">Enterprise</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Description</label>
              <textarea
                v-model="currentConfig.description"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                rows="2"
              />
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Expected Response Time (minutes)</label>
                <input
                  v-model.number="currentConfig.expectedResponseTime"
                  type="number"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  required
                  min="1"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Max Breach Count</label>
                <input
                  v-model.number="currentConfig.maxBreachCount"
                  type="number"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
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
                class="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
              />
              <label for="active" class="ml-2 text-sm text-gray-700">Active</label>
            </div>
          </div>
          <div class="flex justify-end space-x-2 mt-6">
            <button
              type="button"
              @click="closeModal"
              class="px-4 py-2 bg-gray-200 text-gray-800 rounded-lg hover:bg-gray-300"
            >
              Cancel
            </button>
            <button
              type="submit"
              class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
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
