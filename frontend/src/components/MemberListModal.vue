<template>
  <div
    v-if="visible"
    class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50"
    @click.self="close"
  >
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow-xl w-full max-w-4xl max-h-[80vh] flex flex-col m-4">
      <!-- Header -->
      <div class="flex items-center justify-between p-6 border-b border-gray-200 dark:border-gray-700">
        <div>
          <h2 class="text-xl font-semibold text-gray-900 dark:text-white">
            <Icon icon="mdi:account-group" class="inline mr-2" />
            {{ $t('tenant.member.memberList') }}
          </h2>
          <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">
            {{ $t('tenant.member.totalMembers') }}: {{ members.length }}
          </p>
        </div>
        <button
          @click="close"
          class="p-2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 transition-colors"
        >
          <Icon icon="mdi:close" class="h-6 w-6" />
        </button>
      </div>

      <!-- Content -->
      <div class="flex-1 overflow-y-auto p-6">
        <!-- Loading State -->
        <div v-if="loading" class="text-center py-12">
          <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
          <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">{{ $t('tenant.member.loading') }}</p>
        </div>

        <!-- Empty State -->
        <div v-else-if="members.length === 0" class="text-center py-12">
          <Icon icon="mdi:account-group" class="mx-auto h-12 w-12 text-gray-400" />
          <h3 class="mt-2 text-sm font-medium text-gray-900 dark:text-white">{{ $t('tenant.member.noMembers') }}</h3>
          <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">{{ $t('tenant.member.noMembersDescription') }}</p>
        </div>

        <!-- Members List -->
        <div v-else class="space-y-3">
          <div
            v-for="member in members"
            :key="member.id"
            class="flex items-center justify-between p-4 bg-gray-50 dark:bg-gray-700 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-600 transition-colors"
          >
            <!-- Member Info -->
            <div class="flex items-center space-x-4">
              <div class="relative flex-shrink-0">
                <img
                  :src="member.avatar || defaultAvatar"
                  :alt="member.name"
                  class="h-12 w-12 rounded-full object-cover"
                  @error="handleAvatarError"
                />
                <!-- Online Status Indicator -->
                <div
                  v-if="isMemberOnline(member.userId)"
                  class="absolute bottom-0 right-0 w-3 h-3 bg-green-500 rounded-full border-2 border-white dark:border-gray-800"
                  :title="$t('tenant.member.online')"
                />
              </div>
              <div>
                <h4 class="text-sm font-medium text-gray-900 dark:text-white">
                  {{ member.name || $t('tenant.overview.statusUnknown') }}
                </h4>
                <p class="text-xs text-gray-500 dark:text-gray-400">
                  {{ member.email || $t('tenant.member.noEmail') }}
                </p>
              </div>
            </div>

            <!-- Role & Status -->
            <div class="flex items-center space-x-3">
              <!-- Role Badge -->
              <span
                :class="[
                  'text-xs px-3 py-1 rounded-full font-medium',
                  getRoleBadgeClass(member.role)
                ]"
              >
                <Icon v-if="member.role === TenantRole.OWNER" icon="mdi:crown" class="inline h-3 w-3 mr-1" />
                {{ getRoleLabel(member.role) }}
              </span>

              <!-- Status Badge -->
              <span
                :class="[
                  'text-xs px-3 py-1 rounded-full font-medium',
                  getStatusBadgeClass(member.status)
                ]"
              >
                {{ getStatusLabel(member.status) }}
              </span>

              <!-- Last Active -->
              <div v-if="member.lastActiveAt" class="text-xs text-gray-500 dark:text-gray-400 min-w-[80px] text-right">
                {{ getRelativeTime(member.lastActiveAt) }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Footer -->
      <div class="flex items-center justify-between p-6 border-t border-gray-200 dark:border-gray-700">
        <button
          @click="refreshData"
          :disabled="loading"
          class="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary dark:bg-gray-700 dark:text-gray-300 dark:border-gray-600 dark:hover:bg-gray-600 disabled:opacity-50"
        >
          <Icon icon="mdi:refresh" :class="{'animate-spin': loading}" class="h-4 w-4 mr-2" />
          {{ $t('tenant.member.refresh') }}
        </button>
        <button
          @click="goToMemberPage"
          class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-primary hover:bg-primary/80 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary"
        >
          <Icon icon="mdi:open-in-new" class="h-4 w-4 mr-2" />
          {{ $t('tenant.member.viewFullList') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { Icon } from '@iconify/vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { tenantApi } from '@/api/tenantApi'
import { presenceApi } from '@/api/presenceApi'
import { presenceWebSocketService } from '@/services/presenceWebSocketService'
import { useGatewayTenantStore } from '@/stores/tenant/gateway/myTenantStore'
import { getRelativeTime } from '@/utils/dateUtils'
import { TenantRole, MembershipStatus } from '@/types/tenant'
import defaultAvatar from '@/assets/img/user.jpg'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close'])

const router = useRouter()
const { t } = useI18n()
const tenantStore = useGatewayTenantStore()

const loading = ref(false)
const members = ref([])
const onlineMembers = ref(new Map()) // Use Map for O(1) lookup

const getRoleBadgeClass = (role) => {
  switch (role) {
    case TenantRole.OWNER:
      return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200'
    case TenantRole.ADMIN:
      return 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200'
    case TenantRole.EDITOR:
      return 'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200'
    case TenantRole.MEMBER:
      return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
    default:
      return 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300'
  }
}

const getRoleLabel = (role) => {
  switch (role) {
    case TenantRole.OWNER: return t('tenant.member.owner')
    case TenantRole.ADMIN: return t('tenant.member.admin')
    case TenantRole.EDITOR: return t('tenant.member.editor')
    case TenantRole.MEMBER: return t('tenant.member.member')
    default: return role
  }
}

const getStatusBadgeClass = (status) => {
  switch (status) {
    case MembershipStatus.ACTIVE:
      return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
    case MembershipStatus.INACTIVE:
      return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
    default:
      return 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
  }
}

const getStatusLabel = (status) => {
  switch (status) {
    case MembershipStatus.ACTIVE: return t('tenant.overview.statusActive')
    case MembershipStatus.INACTIVE: return t('tenant.overview.statusInactive')
    default: return t('tenant.overview.statusUnknown')
  }
}

const handleAvatarError = (event) => {
  if (event?.target) {
    event.target.src = defaultAvatar
  }
}

const isMemberOnline = (userId) => {
  return onlineMembers.value.has(userId)
}

const loadOnlineMembers = async () => {
  try {
    const tenantKey = tenantStore.currentTenant?.tenantKey
    if (!tenantKey) {
      console.warn('No tenant selected')
      return
    }

    const response = await presenceApi.getOnlineMembers(tenantKey)
    const membersList = response.data || []
    
    // Convert array to Map for O(1) lookup
    onlineMembers.value = new Map(membersList.map(m => [m.userId, m]))
  } catch (error) {
    console.error('Failed to load online members:', error)
    onlineMembers.value = new Map()
  }
}

const handleMemberOnline = (data) => {
  onlineMembers.value.set(data.userId, data)
}

const handleMemberOffline = (data) => {
  onlineMembers.value.delete(data.userId)
}

const loadMembers = async () => {
  loading.value = true
  try {
    const tenantKey = tenantStore.currentTenant?.tenantKey
    if (!tenantKey) {
      console.warn('No tenant selected')
      members.value = []
      return
    }

    const response = await tenantApi.getTenantMembers(tenantKey)
    members.value = response.data?.content || response.data || []
  } catch (error) {
    console.error('Failed to load members:', error)
    members.value = []
  } finally {
    loading.value = false
  }
}

const refreshData = () => {
  loadMembers()
}

const close = () => {
  emit('close')
}

const goToMemberPage = () => {
  close()
  router.push('/tenant/members')
}

// Load online members on component mount (for initial data)
onMounted(() => {
  loadOnlineMembers()
  // Setup WebSocket listeners
  presenceWebSocketService.onMemberOnline = handleMemberOnline
  presenceWebSocketService.onMemberOffline = handleMemberOffline
})

// Cleanup on unmount
onUnmounted(() => {
  presenceWebSocketService.onMemberOnline = null
  presenceWebSocketService.onMemberOffline = null
})

// Load members when modal opens
watch(() => props.visible, (newVal) => {
  if (newVal) {
    loadMembers()
    loadOnlineMembers()
  }
})
</script>
