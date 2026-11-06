<template>
  <div class="p-6 max-w-6xl mx-auto">
    <!-- Loading -->
    <div v-if="loading" class="flex items-center justify-center min-h-[400px]">
      <div class="text-center">
        <Icon icon="mdi:loading" class="animate-spin text-5xl text-blue-600 mb-3" />
        <p class="text-gray-500 dark:text-gray-400">{{ t('common.loading') }}</p>
      </div>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-xl p-6 text-center">
      <Icon icon="mdi:alert-circle" class="text-4xl text-red-500 mb-2" />
      <p class="text-red-700 dark:text-red-300">{{ error }}</p>
      <button @click="router.back()" class="mt-4 px-4 py-2 bg-gray-100 dark:bg-gray-700 rounded-lg text-sm">
        ← {{ t('common.back') }}
      </button>
    </div>

    <template v-else-if="customer">
      <!-- Header -->
      <div class="flex items-start justify-between mb-6">
        <div class="flex items-center gap-4">
          <button
            @click="router.back()"
            class="p-2 rounded-lg text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
          >
            <Icon icon="mdi:arrow-left" class="text-xl" />
          </button>
          <div class="flex items-center gap-4">
            <!-- Avatar -->
            <div class="relative">
              <img
                v-if="customer.avatarUrl"
                :src="customer.avatarUrl"
                class="h-16 w-16 rounded-full object-cover ring-4 ring-white dark:ring-gray-800 shadow"
              />
              <div
                v-else
                class="h-16 w-16 rounded-full bg-gradient-to-br from-blue-400 to-indigo-600 flex items-center justify-center ring-4 ring-white dark:ring-gray-800 shadow"
              >
                <span class="text-white text-2xl font-bold">{{ customer.name?.charAt(0)?.toUpperCase() || '?' }}</span>
              </div>
              <span
                :class="statusDotClass(customer.status)"
                class="absolute bottom-0 right-0 w-4 h-4 rounded-full ring-2 ring-white dark:ring-gray-800"
              />
            </div>
            <!-- Name + meta -->
            <div>
              <div class="flex items-center gap-2 flex-wrap">
                <h1 class="text-2xl font-bold text-gray-900 dark:text-white">{{ customer.name }}</h1>
                <VIPBadge :tier="customer.tier" />
                <span :class="getStatusClass(customer.status)" class="px-2 py-0.5 text-xs font-medium rounded-full">
                  {{ t(`customer.statusOptions.${customer.status?.toLowerCase()}`) }}
                </span>
              </div>
              <p class="text-sm text-gray-500 dark:text-gray-400 mt-0.5">
                ID: {{ customer.externalId }}
                <span v-if="customer.email"> · {{ customer.email }}</span>
                <span v-if="customer.phone"> · {{ customer.phone }}</span>
              </p>
            </div>
          </div>
        </div>

        <!-- Actions -->
        <div class="flex gap-2">
          <button
            @click="openEditModal"
            class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm rounded-lg transition-colors flex items-center gap-2"
          >
            <Icon icon="mdi:pencil" />
            {{ t('common.edit') }}
          </button>
        </div>
      </div>

      <!-- Stats cards -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
        <div class="bg-white dark:bg-gray-800 rounded-xl p-4 border border-gray-200 dark:border-gray-700 shadow-sm">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 bg-blue-100 dark:bg-blue-900/40 rounded-lg flex items-center justify-center">
              <Icon icon="mdi:message-text" class="text-blue-600 dark:text-blue-400" />
            </div>
            <div>
              <p class="text-xs text-gray-500 dark:text-gray-400">{{ t('customer.conversations') }}</p>
              <p class="text-xl font-bold text-gray-900 dark:text-white">{{ customer.totalConversations || 0 }}</p>
            </div>
          </div>
        </div>
        <div class="bg-white dark:bg-gray-800 rounded-xl p-4 border border-gray-200 dark:border-gray-700 shadow-sm">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 bg-green-100 dark:bg-green-900/40 rounded-lg flex items-center justify-center">
              <Icon icon="mdi:chat" class="text-green-600 dark:text-green-400" />
            </div>
            <div>
              <p class="text-xs text-gray-500 dark:text-gray-400">{{ t('customer.totalMessages') }}</p>
              <p class="text-xl font-bold text-gray-900 dark:text-white">{{ customer.totalMessages || 0 }}</p>
            </div>
          </div>
        </div>
        <div class="bg-white dark:bg-gray-800 rounded-xl p-4 border border-gray-200 dark:border-gray-700 shadow-sm">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 bg-yellow-100 dark:bg-yellow-900/40 rounded-lg flex items-center justify-center">
              <Icon icon="mdi:star" class="text-yellow-600 dark:text-yellow-400" />
            </div>
            <div>
              <p class="text-xs text-gray-500 dark:text-gray-400">{{ t('customer.satisfactionScore') }}</p>
              <p class="text-xl font-bold text-gray-900 dark:text-white">
                {{ customer.satisfactionScore ? customer.satisfactionScore.toFixed(1) : '-' }}
              </p>
            </div>
          </div>
        </div>
        <div class="bg-white dark:bg-gray-800 rounded-xl p-4 border border-gray-200 dark:border-gray-700 shadow-sm">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 bg-purple-100 dark:bg-purple-900/40 rounded-lg flex items-center justify-center">
              <Icon icon="mdi:clock-fast" class="text-purple-600 dark:text-purple-400" />
            </div>
            <div>
              <p class="text-xs text-gray-500 dark:text-gray-400">{{ t('customer.avgResponseTime') }}</p>
              <p class="text-xl font-bold text-gray-900 dark:text-white">
                {{ customer.avgResponseTimeSeconds ? formatDuration(customer.avgResponseTimeSeconds) : '-' }}
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- Tabs -->
      <div class="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm overflow-hidden">
        <!-- Tab nav -->
        <div class="flex border-b border-gray-200 dark:border-gray-700 overflow-x-auto">
          <button
            v-for="tab in tabs"
            :key="tab.id"
            @click="activeTab = tab.id"
            :class="[
              'flex items-center gap-2 px-5 py-3 text-sm font-medium whitespace-nowrap transition-colors border-b-2 -mb-px',
              activeTab === tab.id
                ? 'border-blue-600 text-blue-600 dark:text-blue-400 dark:border-blue-400'
                : 'border-transparent text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-200'
            ]"
          >
            <Icon :icon="tab.icon" />
            {{ tab.label }}
          </button>
        </div>

        <div class="p-6">
          <!-- Tab: Info -->
          <div v-if="activeTab === 'info'">
            <dl class="grid grid-cols-1 sm:grid-cols-2 gap-x-8 gap-y-4">
              <div v-for="field in infoFields" :key="field.key">
                <dt class="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wide">{{ field.label }}</dt>
                <dd class="mt-1 text-sm text-gray-900 dark:text-white">{{ field.value || '-' }}</dd>
              </div>
            </dl>
          </div>

          <!-- Tab: Timeline -->
          <div v-if="activeTab === 'timeline'">
            <div v-if="timelineLoading" class="text-center py-8">
              <Icon icon="mdi:loading" class="animate-spin text-3xl text-blue-600" />
            </div>
            <CustomerTimeline v-else :events="timelineEvents" empty-text="Chưa có lịch sử hoạt động" />
          </div>

          <!-- Tab: Feedback -->
          <div v-if="activeTab === 'feedback'">
            <div v-if="feedbackLoading" class="text-center py-8">
              <Icon icon="mdi:loading" class="animate-spin text-3xl text-blue-600" />
            </div>
            <div v-else-if="customerFeedback.length === 0" class="text-center py-12 text-gray-400 dark:text-gray-500">
              <Icon icon="mdi:star-off" class="text-4xl mb-2" />
              <p class="text-sm">{{ t('feedback.noFeedback') }}</p>
            </div>
            <div v-else class="space-y-4">
              <div
                v-for="fb in customerFeedback"
                :key="fb.id"
                class="border border-gray-200 dark:border-gray-700 rounded-xl p-4"
              >
                <div class="flex items-start justify-between gap-4">
                  <div class="flex-1">
                    <div class="flex items-center gap-2 mb-1">
                      <div class="flex">
                        <Icon
                          v-for="i in 5"
                          :key="i"
                          :icon="i <= fb.rating ? 'mdi:star' : 'mdi:star-outline'"
                          :class="i <= fb.rating ? 'text-yellow-500' : 'text-gray-300 dark:text-gray-600'"
                          class="text-base"
                        />
                      </div>
                      <span :class="getFeedbackStatusClass(fb.status)" class="px-2 py-0.5 text-[10px] font-semibold rounded-full">
                        {{ fb.status }}
                      </span>
                    </div>
                    <p v-if="fb.comment" class="text-sm text-gray-900 dark:text-white">{{ fb.comment }}</p>
                    <p v-if="fb.adminResponse" class="mt-2 text-xs text-blue-700 dark:text-blue-300 bg-blue-50 dark:bg-blue-900/20 rounded px-2 py-1">
                      <strong>Phản hồi:</strong> {{ fb.adminResponse }}
                    </p>
                  </div>
                  <time class="text-xs text-gray-400 dark:text-gray-500 whitespace-nowrap">{{ formatDate(fb.submittedAt) }}</time>
                </div>
              </div>
            </div>
          </div>

          <!-- Tab: Notes / Attributes -->
          <div v-if="activeTab === 'notes'">
            <div class="space-y-4">
              <!-- Agent notes (Phase 4) -->
              <div class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-4">
                <h3 class="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-3">Ghi chú của agent</h3>
                <div class="flex gap-2 mb-4">
                  <textarea
                    v-model="newNoteContent"
                    rows="2"
                    placeholder="Thêm ghi chú về khách hàng này..."
                    class="flex-1 px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg dark:bg-gray-700 dark:text-white text-sm"
                  ></textarea>
                  <button
                    @click="addNote"
                    :disabled="noteSaving || !newNoteContent.trim()"
                    class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 disabled:opacity-50 self-end"
                  >
                    {{ noteSaving ? 'Đang lưu...' : 'Thêm' }}
                  </button>
                </div>
                <div v-if="notesLoading" class="text-center py-4">
                  <Icon icon="mdi:loading" class="animate-spin text-2xl text-blue-600" />
                </div>
                <div v-else-if="customerNotes.length === 0" class="text-sm text-gray-400 dark:text-gray-500 text-center py-2">
                  Chưa có ghi chú nào
                </div>
                <div v-else class="space-y-2">
                  <div
                    v-for="note in customerNotes"
                    :key="note.id"
                    class="bg-gray-50 dark:bg-gray-700/50 rounded-lg px-3 py-2 flex items-start justify-between gap-2"
                  >
                    <div class="flex-1 min-w-0">
                      <p class="text-sm text-gray-900 dark:text-white whitespace-pre-wrap">{{ note.content }}</p>
                      <p class="text-xs text-gray-400 dark:text-gray-500 mt-1">
                        {{ note.authorName || 'Agent' }} · {{ formatDate(note.createdAt) }}
                      </p>
                    </div>
                    <button
                      @click="removeNote(note.id)"
                      class="text-gray-400 hover:text-red-500 shrink-0"
                      title="Xóa ghi chú"
                    >
                      <Icon icon="mdi:delete-outline" class="text-lg" />
                    </button>
                  </div>
                </div>
              </div>

              <div>
                <h3 class="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-2">Tags</h3>
                <div v-if="customer.tags && Object.keys(customer.tags).length > 0" class="flex flex-wrap gap-2">
                  <span
                    v-for="(val, key) in customer.tags"
                    :key="key"
                    class="px-2 py-1 bg-blue-100 dark:bg-blue-900/40 text-blue-700 dark:text-blue-300 text-xs rounded-full"
                  >
                    {{ key }}: {{ val }}
                  </span>
                </div>
                <p v-else class="text-sm text-gray-400 dark:text-gray-500">Chưa có tags</p>
              </div>
              <div>
                <h3 class="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-2">Thuộc tính tuỳ chỉnh</h3>
                <div v-if="customer.customAttributes && Object.keys(customer.customAttributes).length > 0">
                  <dl class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                    <div
                      v-for="(val, key) in customer.customAttributes"
                      :key="key"
                      class="bg-gray-50 dark:bg-gray-700/50 rounded-lg px-3 py-2"
                    >
                      <dt class="text-xs text-gray-500 dark:text-gray-400">{{ key }}</dt>
                      <dd class="text-sm text-gray-900 dark:text-white mt-0.5">{{ val }}</dd>
                    </div>
                  </dl>
                </div>
                <p v-else class="text-sm text-gray-400 dark:text-gray-500">Chưa có thuộc tính tuỳ chỉnh</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- Edit Modal -->
    <div v-if="showEditModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-2xl max-w-lg w-full max-h-[90vh] overflow-y-auto">
        <div class="p-6">
          <h2 class="text-lg font-bold text-gray-900 dark:text-white mb-4">{{ t('customer.editCustomer') }}</h2>
          <form @submit.prevent="saveEdit" class="space-y-3">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('customer.name') }} *</label>
              <input v-model="editForm.name" type="text" required class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg dark:bg-gray-700 dark:text-white" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('customer.email') }}</label>
              <input v-model="editForm.email" type="email" class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg dark:bg-gray-700 dark:text-white" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('customer.phone') }}</label>
              <input v-model="editForm.phone" type="text" class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg dark:bg-gray-700 dark:text-white" />
            </div>
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('customer.status') }}</label>
                <select v-model="editForm.status" class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg dark:bg-gray-700 dark:text-white">
                  <option value="ACTIVE">{{ t('customer.statusOptions.active') }}</option>
                  <option value="INACTIVE">{{ t('customer.statusOptions.inactive') }}</option>
                  <option value="BLOCKED">{{ t('customer.statusOptions.blocked') }}</option>
                  <option value="CHURNED">{{ t('customer.statusOptions.churned') }}</option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('customer.tier') }}</label>
                <select v-model="editForm.tier" class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg dark:bg-gray-700 dark:text-white">
                  <option value="FREE">{{ t('customer.tierOptions.free') }}</option>
                  <option value="BASIC">{{ t('customer.tierOptions.basic') }}</option>
                  <option value="PREMIUM">{{ t('customer.tierOptions.premium') }}</option>
                  <option value="ENTERPRISE">{{ t('customer.tierOptions.enterprise') }}</option>
                </select>
              </div>
            </div>
            <div class="flex justify-end gap-3 pt-2">
              <button type="button" @click="showEditModal = false" class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700">
                {{ t('common.cancel') }}
              </button>
              <button type="submit" class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700">
                {{ t('common.save') }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import { useI18n } from 'vue-i18n'
import customerProfileApi from '@/api/customerProfileApi'
import feedbackApi from '@/api/feedbackApi'
import VIPBadge from '@/components/customer/VIPBadge.vue'
import CustomerTimeline from '@/components/customer/CustomerTimeline.vue'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const customer = ref(null)
const loading = ref(true)
const error = ref(null)
const activeTab = ref('info')
const showEditModal = ref(false)
const editForm = ref({})

// Feedback tab
const customerFeedback = ref([])
const feedbackLoading = ref(false)

// Timeline tab
const timelineLoading = ref(false)
const timelineEvents = ref([])

// Notes tab (Phase 4)
const customerNotes = ref([])
const notesLoading = ref(false)
const newNoteContent = ref('')
const noteSaving = ref(false)

const tabs = [
  { id: 'info', label: 'Thông tin', icon: 'mdi:account-details' },
  { id: 'timeline', label: 'Lịch sử', icon: 'mdi:timeline' },
  { id: 'feedback', label: 'Feedback', icon: 'mdi:star' },
  { id: 'notes', label: 'Ghi chú', icon: 'mdi:note-text' },
]

const infoFields = computed(() => {
  if (!customer.value) return []
  return [
    { key: 'name', label: t('customer.name'), value: customer.value.name },
    { key: 'externalId', label: t('customer.externalId'), value: customer.value.externalId },
    { key: 'email', label: t('customer.email'), value: customer.value.email },
    { key: 'phone', label: t('customer.phone'), value: customer.value.phone },
    { key: 'tier', label: t('customer.tier'), value: customer.value.tier },
    { key: 'status', label: t('customer.status'), value: customer.value.status },
    { key: 'preferredLanguage', label: t('customer.language'), value: customer.value.preferredLanguage },
    { key: 'timezone', label: t('customer.timezone'), value: customer.value.timezone },
    { key: 'firstContactAt', label: 'Lần đầu liên hệ', value: formatDate(customer.value.firstContactAt) },
    { key: 'lastContactAt', label: t('customer.lastContact'), value: formatDate(customer.value.lastContactAt) },
  ]
})

async function loadCustomer() {
  try {
    loading.value = true
    error.value = null
    customer.value = await customerProfileApi.getCustomerProfileById(route.params.id)
  } catch (err) {
    error.value = err.message || 'Không tìm thấy customer'
  } finally {
    loading.value = false
  }
}

async function loadFeedback() {
  feedbackLoading.value = true
  try {
    customerFeedback.value = await feedbackApi.getFeedbackByCustomerProfileId(route.params.id)
  } catch {
    customerFeedback.value = []
  } finally {
    feedbackLoading.value = false
  }
}

async function loadTimeline() {
  timelineLoading.value = true
  try {
    // Phase 4: use backend unified timeline (conversations + messages + feedback + notes)
    timelineEvents.value = await customerProfileApi.getCustomerTimeline(route.params.id)
  } catch {
    timelineEvents.value = []
  } finally {
    timelineLoading.value = false
  }
}

async function loadNotes() {
  notesLoading.value = true
  try {
    customerNotes.value = await customerProfileApi.getCustomerNotes(route.params.id)
  } catch {
    customerNotes.value = []
  } finally {
    notesLoading.value = false
  }
}

async function addNote() {
  if (!newNoteContent.value.trim()) return
  noteSaving.value = true
  try {
    await customerProfileApi.createCustomerNote(route.params.id, {
      content: newNoteContent.value.trim(),
    })
    newNoteContent.value = ''
    await loadNotes()
    await loadTimeline()
  } catch (err) {
    console.error('Create note error:', err)
  } finally {
    noteSaving.value = false
  }
}

async function removeNote(noteId) {
  try {
    await customerProfileApi.deleteCustomerNote(noteId)
    await loadNotes()
    await loadTimeline()
  } catch (err) {
    console.error('Delete note error:', err)
  }
}

function openEditModal() {
  editForm.value = {
    name: customer.value.name,
    email: customer.value.email,
    phone: customer.value.phone,
    status: customer.value.status,
    tier: customer.value.tier,
    preferredLanguage: customer.value.preferredLanguage,
    timezone: customer.value.timezone,
  }
  showEditModal.value = true
}

async function saveEdit() {
  try {
    customer.value = await customerProfileApi.updateCustomerProfile(route.params.id, editForm.value)
    showEditModal.value = false
  } catch (err) {
    console.error('Update error:', err)
  }
}

function statusDotClass(status) {
  const map = {
    ACTIVE: 'bg-green-500',
    INACTIVE: 'bg-gray-400',
    BLOCKED: 'bg-red-500',
    CHURNED: 'bg-yellow-500',
  }
  return map[status] || 'bg-gray-400'
}

function getStatusClass(status) {
  const classes = {
    ACTIVE: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
    INACTIVE: 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-200',
    BLOCKED: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200',
    CHURNED: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200',
  }
  return classes[status] || 'bg-gray-100 text-gray-800'
}

function getFeedbackStatusClass(status) {
  const classes = {
    PENDING: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200',
    REVIEWED: 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200',
    RESPONDED: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
    CLOSED: 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-200',
  }
  return classes[status] || 'bg-gray-100 text-gray-800'
}

function formatDate(date) {
  if (!date) return null
  return new Date(date).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

function formatDuration(seconds) {
  if (seconds < 60) return `${seconds}s`
  if (seconds < 3600) return `${Math.floor(seconds / 60)}m`
  return `${Math.floor(seconds / 3600)}h ${Math.floor((seconds % 3600) / 60)}m`
}

onMounted(async () => {
  await loadCustomer()
  // Load parallel data after customer is loaded
  loadFeedback()
  loadTimeline()
  loadNotes()
})
</script>
