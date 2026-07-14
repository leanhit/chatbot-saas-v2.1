<template>
  <div class="escalation-tiers p-4">
    <!-- Header -->
    <div class="mt-2 w-full">
      <div class="lg:flex grid-cols-1 lg:space-y-0 space-y-3 gap-5 justify-between">
        <div>
          <p class="uppercase text-xs text-gray-700 dark:text-gray-400 font-semibold">Admin</p>
          <h1 class="text-2xl text-gray-900 dark:text-gray-200 font-medium">
            Escalation Tiers
          </h1>
        </div>
        <div class="flex gap-2">
          <button
            @click="createDefaultTiers"
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
            <span class="text">Add Tier</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Tiers List -->
    <div v-if="loading" class="p-8 text-center mt-6">
      <Icon icon="mdi:loading" class="animate-spin text-6xl text-gray-300 dark:text-gray-600 mx-auto" />
      <p class="mt-2 text-gray-500 dark:text-gray-400">Loading escalation tiers...</p>
    </div>

    <div v-else-if="tiers.length === 0" class="p-8 text-center mt-6">
      <Icon icon="mdi:layers" class="text-6xl text-gray-300 dark:text-gray-600 mx-auto" />
      <p class="mt-2 text-sm text-gray-500 dark:text-gray-400">No escalation tiers found.</p>
      <button
        @click="createDefaultTiers"
        class="mt-4 px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/80"
      >
        Create Default Tiers
      </button>
    </div>

    <div v-else class="space-y-4 mt-6">
      <div
        v-for="tier in sortedTiers"
        :key="tier.id"
        :class="[
          'p-4 border rounded-lg',
          tier.active ? 'border-blue-200 bg-blue-50 dark:bg-blue-900/20 dark:border-blue-800' : 'border-gray-200 bg-gray-50 dark:bg-gray-700 dark:border-gray-600'
        ]"
      >
        <div class="flex justify-between items-start">
          <div class="flex-1">
            <div class="flex items-center space-x-2">
              <div class="flex items-center justify-center w-8 h-8 rounded-full bg-blue-600 text-white font-bold">
                {{ tier.level }}
              </div>
              <h3 class="font-semibold text-gray-900 dark:text-gray-200">{{ tier.name }}</h3>
              <span
                :class="[
                  'px-2 py-1 text-xs rounded-full',
                  tier.active ? 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400' : 'bg-gray-100 text-gray-600 dark:bg-gray-700 dark:text-gray-400'
                ]"
              >
                {{ tier.active ? 'Active' : 'Inactive' }}
              </span>
              <span v-if="tier.requiredRole" class="px-2 py-1 text-xs bg-purple-100 text-purple-800 dark:bg-purple-900/30 dark:text-purple-400 rounded-full">
                {{ tier.requiredRole }}
              </span>
            </div>
            <p class="mt-1 text-sm text-gray-600 dark:text-gray-400">{{ tier.description }}</p>
            <div class="mt-3 grid grid-cols-2 gap-4 text-sm">
              <div>
                <span class="font-medium text-gray-700 dark:text-gray-300">Timeout:</span>
                <span class="ml-2 text-orange-600 dark:text-orange-400 font-semibold">{{ formatTimeout(tier.timeoutSeconds) }}</span>
              </div>
              <div>
                <span class="font-medium text-gray-700 dark:text-gray-300">Created:</span>
                <span class="ml-2 text-gray-600 dark:text-gray-400">{{ formatDate(tier.createdAt) }}</span>
              </div>
            </div>
          </div>
          <div class="flex space-x-2 ml-4">
            <button
              @click="editTier(tier)"
              class="p-2 text-blue-600 hover:bg-blue-100 dark:hover:bg-blue-900/30 rounded-lg transition-colors"
            >
              <Icon icon="mdi:pencil" class="text-lg" />
            </button>
            <button
              @click="toggleTierStatus(tier)"
              :class="[
                'p-2 rounded-lg transition-colors',
                tier.active ? 'text-green-600 hover:bg-green-100 dark:hover:bg-green-900/30' : 'text-gray-600 hover:bg-gray-200 dark:hover:bg-gray-600'
              ]"
            >
              <Icon :icon="tier.active ? 'mdi:toggle-switch' : 'mdi:toggle-switch-off'" class="text-lg" />
            </button>
            <button
              @click="deleteTier(tier.id)"
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
          {{ showCreateModal ? 'Create Escalation Tier' : 'Edit Escalation Tier' }}
        </h2>
        <form @submit.prevent="saveTier">
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Level</label>
              <input
                v-model.number="currentTier.level"
                type="number"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded-lg focus:ring-2 focus:ring-blue-500"
                required
                min="1"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Name</label>
              <input
                v-model="currentTier.name"
                type="text"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded-lg focus:ring-2 focus:ring-blue-500"
                required
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Description</label>
              <textarea
                v-model="currentTier.description"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded-lg focus:ring-2 focus:ring-blue-500"
                rows="2"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Timeout (seconds)</label>
              <input
                v-model.number="currentTier.timeoutSeconds"
                type="number"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded-lg focus:ring-2 focus:ring-blue-500"
                required
                min="60"
              />
              <p class="mt-1 text-xs text-gray-500 dark:text-gray-400">
                {{ formatTimeout(currentTier.timeoutSeconds) }}
              </p>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Required Role (optional)</label>
              <input
                v-model="currentTier.requiredRole"
                type="text"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded-lg focus:ring-2 focus:ring-blue-500"
                placeholder="e.g., TEAM_LEAD, SUPERVISOR"
              />
            </div>
            <div class="flex items-center">
              <input
                v-model="currentTier.active"
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
import escalationTierApi from '@/api/escalationTierApi'

const loading = ref(false)
const tiers = ref([])
const showCreateModal = ref(false)
const showEditModal = ref(false)
const currentTier = ref({
  level: 1,
  name: '',
  description: '',
  timeoutSeconds: 300,
  requiredRole: '',
  active: true
})

const sortedTiers = computed(() => {
  return [...tiers.value].sort((a, b) => a.level - b.level)
})

const loadTiers = async () => {
  loading.value = true
  try {
    const response = await escalationTierApi.getEscalationTiers()
    tiers.value = response.data || []
  } catch (error) {
    console.error('Error loading escalation tiers:', error)
  } finally {
    loading.value = false
  }
}

const createDefaultTiers = async () => {
  try {
    await escalationTierApi.createDefaultEscalationTiers()
    await loadTiers()
  } catch (error) {
    console.error('Error creating default tiers:', error)
  }
}

const editTier = (tier) => {
  currentTier.value = { ...tier }
  showEditModal.value = true
}

const toggleTierStatus = async (tier) => {
  try {
    const updatedTier = { ...tier, active: !tier.active }
    await escalationTierApi.updateEscalationTier(tier.id, updatedTier)
    await loadTiers()
  } catch (error) {
    console.error('Error toggling tier status:', error)
  }
}

const deleteTier = async (id) => {
  if (confirm('Are you sure you want to delete this tier?')) {
    try {
      await escalationTierApi.deleteEscalationTier(id)
      await loadTiers()
    } catch (error) {
      console.error('Error deleting tier:', error)
    }
  }
}

const saveTier = async () => {
  try {
    if (showCreateModal.value) {
      await escalationTierApi.createEscalationTier(currentTier.value)
    } else {
      await escalationTierApi.updateEscalationTier(currentTier.value.id, currentTier.value)
    }
    
    closeModal()
    await loadTiers()
  } catch (error) {
    console.error('Error saving tier:', error)
  }
}

const closeModal = () => {
  showCreateModal.value = false
  showEditModal.value = false
  currentTier.value = {
    level: 1,
    name: '',
    description: '',
    timeoutSeconds: 300,
    requiredRole: '',
    active: true
  }
}

const formatTimeout = (seconds) => {
  if (!seconds) return 'N/A'
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  if (minutes > 0) {
    return `${minutes}m ${remainingSeconds}s`
  }
  return `${remainingSeconds}s`
}

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString()
}

onMounted(() => {
  loadTiers()
})
</script>
