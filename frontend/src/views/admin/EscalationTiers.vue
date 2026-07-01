<template>
  <div class="escalation-tiers">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-900">Escalation Tiers</h1>
      <div class="space-x-2">
        <button
          @click="createDefaultTiers"
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
          Add Tier
        </button>
      </div>
    </div>

    <!-- Tiers List -->
    <div v-if="loading" class="p-8 text-center">
      <Icon icon="mdi:loading" class="animate-spin text-2xl text-gray-400" />
      <p class="mt-2 text-sm text-gray-500">Loading escalation tiers...</p>
    </div>

    <div v-else-if="tiers.length === 0" class="p-8 text-center">
      <Icon icon="mdi:layers" class="text-4xl text-gray-300 mx-auto" />
      <p class="mt-2 text-sm text-gray-500">No escalation tiers found.</p>
      <button
        @click="createDefaultTiers"
        class="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
      >
        Create Default Tiers
      </button>
    </div>

    <div v-else class="space-y-4">
      <div
        v-for="tier in sortedTiers"
        :key="tier.id"
        :class="[
          'p-4 border rounded-lg',
          tier.active ? 'border-blue-200 bg-blue-50' : 'border-gray-200 bg-gray-50'
        ]"
      >
        <div class="flex justify-between items-start">
          <div class="flex-1">
            <div class="flex items-center space-x-2">
              <div class="flex items-center justify-center w-8 h-8 rounded-full bg-blue-600 text-white font-bold">
                {{ tier.level }}
              </div>
              <h3 class="font-semibold text-gray-900">{{ tier.name }}</h3>
              <span
                :class="[
                  'px-2 py-1 text-xs rounded-full',
                  tier.active ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-600'
                ]"
              >
                {{ tier.active ? 'Active' : 'Inactive' }}
              </span>
              <span v-if="tier.requiredRole" class="px-2 py-1 text-xs bg-purple-100 text-purple-800 rounded-full">
                {{ tier.requiredRole }}
              </span>
            </div>
            <p class="mt-1 text-sm text-gray-600">{{ tier.description }}</p>
            <div class="mt-3 grid grid-cols-2 gap-4 text-sm">
              <div>
                <span class="font-medium text-gray-700">Timeout:</span>
                <span class="ml-2 text-orange-600 font-semibold">{{ formatTimeout(tier.timeoutSeconds) }}</span>
              </div>
              <div>
                <span class="font-medium text-gray-700">Created:</span>
                <span class="ml-2 text-gray-600">{{ formatDate(tier.createdAt) }}</span>
              </div>
            </div>
          </div>
          <div class="flex space-x-2 ml-4">
            <button
              @click="editTier(tier)"
              class="p-2 text-blue-600 hover:bg-blue-100 rounded-lg transition-colors"
            >
              <Icon icon="mdi:pencil" class="text-lg" />
            </button>
            <button
              @click="toggleTierStatus(tier)"
              :class="[
                'p-2 rounded-lg transition-colors',
                tier.active ? 'text-green-600 hover:bg-green-100' : 'text-gray-600 hover:bg-gray-200'
              ]"
            >
              <Icon :icon="tier.active ? 'mdi:toggle-switch' : 'mdi:toggle-switch-off'" class="text-lg" />
            </button>
            <button
              @click="deleteTier(tier.id)"
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
          {{ showCreateModal ? 'Create Escalation Tier' : 'Edit Escalation Tier' }}
        </h2>
        <form @submit.prevent="saveTier">
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Level</label>
              <input
                v-model.number="currentTier.level"
                type="number"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                required
                min="1"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Name</label>
              <input
                v-model="currentTier.name"
                type="text"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                required
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Description</label>
              <textarea
                v-model="currentTier.description"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                rows="2"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Timeout (seconds)</label>
              <input
                v-model.number="currentTier.timeoutSeconds"
                type="number"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                required
                min="60"
              />
              <p class="mt-1 text-xs text-gray-500">
                {{ formatTimeout(currentTier.timeoutSeconds) }}
              </p>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Required Role (optional)</label>
              <input
                v-model="currentTier.requiredRole"
                type="text"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                placeholder="e.g., TEAM_LEAD, SUPERVISOR"
              />
            </div>
            <div class="flex items-center">
              <input
                v-model="currentTier.active"
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
