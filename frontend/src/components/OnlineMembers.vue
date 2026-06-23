<template>
  <div class="online-members-panel">
    <div class="flex items-center justify-between mb-3">
      <h3 class="text-sm font-semibold text-gray-700 dark:text-gray-300">
        <Icon icon="mdi:account-group" class="inline mr-1" />
        Online Members ({{ onlineMembers.length }})
      </h3>
      <div 
        :class="[
          'w-2 h-2 rounded-full',
          connectionStatus === 'connected' ? 'bg-green-500' : 
          connectionStatus === 'connecting' ? 'bg-yellow-500' : 'bg-red-500'
        ]"
        :title="connectionStatus"
      />
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="p-4 text-center">
      <Icon icon="mdi:loading" class="animate-spin text-xl text-gray-400" />
    </div>

    <!-- Empty State -->
    <div v-else-if="onlineMembers.length === 0" class="p-4 text-center text-sm text-gray-500 dark:text-gray-400">
      No members online
    </div>

    <!-- Online Members List -->
    <div v-else class="space-y-2 max-h-64 overflow-y-auto">
      <div
        v-for="member in onlineMembers"
        :key="member.userId"
        class="flex items-center gap-3 p-2 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
      >
        <!-- Avatar -->
        <div class="relative flex-shrink-0">
          <img 
            v-if="member.avatar"
            :src="member.avatar" 
            :alt="member.fullName"
            class="w-8 h-8 rounded-full object-cover"
            @error="handleImageError"
          />
          <div 
            v-else
            class="w-8 h-8 rounded-full bg-gray-300 dark:bg-gray-600 flex items-center justify-center"
          >
            <Icon icon="mdi:account" class="text-gray-600 dark:text-gray-300 text-sm" />
          </div>
          <!-- Online Indicator -->
          <div class="absolute bottom-0 right-0 w-2.5 h-2.5 bg-green-500 rounded-full border-2 border-white dark:border-gray-800" />
        </div>

        <!-- Member Info -->
        <div class="flex-1 min-w-0">
          <p class="text-sm font-medium text-gray-900 dark:text-gray-200 truncate">
            {{ member.fullName }}
          </p>
          <p class="text-xs text-gray-500 dark:text-gray-400 truncate">
            {{ member.email }}
          </p>
        </div>

        <!-- Role Badge -->
        <span
          :class="[
            'text-xs px-2 py-0.5 rounded',
            getRoleClass(member.role)
          ]"
        >
          {{ member.role }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { Icon } from '@iconify/vue'
import { presenceApi } from '@/api/presenceApi'
import { presenceWebSocketService } from '@/services/presenceWebSocketService'
import { useAuthStore } from '@/stores/authStore'

const props = defineProps({
  tenantKey: {
    type: String,
    required: true
  }
})

const authStore = useAuthStore()
const loading = ref(true)
const onlineMembers = ref([])
const connectionStatus = computed(() => presenceWebSocketService.getConnectionStatus())

const getRoleClass = (role) => {
  switch (role) {
    case 'OWNER':
      return 'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200'
    case 'ADMIN':
      return 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200'
    case 'AGENT':
      return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
    default:
      return 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300'
  }
}

const handleImageError = (event) => {
  event.target.style.display = 'none'
  const parent = event.target.parentElement
  if (parent) {
    const fallback = parent.querySelector('.bg-gray-300, .dark\\:bg-gray-600')
    if (fallback) {
      fallback.style.display = 'flex'
    }
  }
}

const loadOnlineMembers = async () => {
  try {
    loading.value = true
    const response = await presenceApi.getOnlineMembers(props.tenantKey)
    onlineMembers.value = response.data || []
  } catch (error) {
    console.error('Failed to load online members:', error)
  } finally {
    loading.value = false
  }
}

const handleMemberOnline = (data) => {
  // Add or update member in the list
  const existingIndex = onlineMembers.value.findIndex(m => m.userId === data.userId)
  if (existingIndex >= 0) {
    onlineMembers.value[existingIndex] = { ...onlineMembers.value[existingIndex], ...data }
  } else {
    onlineMembers.value.push(data)
  }
}

const handleMemberOffline = (data) => {
  // Remove member from the list
  onlineMembers.value = onlineMembers.value.filter(m => m.userId !== data.userId)
}

onMounted(async () => {
  // Load initial online members
  await loadOnlineMembers()

  // Connect to WebSocket for real-time updates
  if (props.tenantKey) {
    presenceWebSocketService.onMemberOnline = handleMemberOnline
    presenceWebSocketService.onMemberOffline = handleMemberOffline
    await presenceWebSocketService.connect(props.tenantKey)
  }
})

onUnmounted(() => {
  // Cleanup WebSocket connection
  presenceWebSocketService.onMemberOnline = null
  presenceWebSocketService.onMemberOffline = null
  presenceWebSocketService.disconnect()
})
</script>

<style scoped>
.online-members-panel {
  background: white;
  border-radius: 0.5rem;
  padding: 1rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.dark .online-members-panel {
  background: #1f2937;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}
</style>
