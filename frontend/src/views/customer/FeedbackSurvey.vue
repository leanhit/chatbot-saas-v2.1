<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-900 dark:text-white">{{ t('feedback.title') }}</h1>
      <button
        @click="openCreateModal"
        class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
      >
        <Icon icon="mdi:plus" class="inline-block mr-2" />
        {{ t('feedback.addFeedback') }}
      </button>
    </div>

    <!-- Filters -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-4 mb-6">
      <div class="flex flex-wrap gap-4">
        <div class="flex-1 min-w-[200px]">
          <input
            v-model="searchQuery"
            type="text"
            :placeholder="t('feedback.searchPlaceholder')"
            class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg dark:bg-gray-700 dark:text-white"
          />
        </div>
        <select
          v-model="filterStatus"
          class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg dark:bg-gray-700 dark:text-white"
        >
          <option value="">{{ t('common.all') }}</option>
          <option value="PENDING">{{ t('feedback.status.pending') }}</option>
          <option value="REVIEWED">{{ t('feedback.status.reviewed') }}</option>
          <option value="RESPONDED">{{ t('feedback.status.responded') }}</option>
          <option value="CLOSED">{{ t('feedback.status.closed') }}</option>
        </select>
        <select
          v-model="filterType"
          class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg dark:bg-gray-700 dark:text-white"
        >
          <option value="">{{ t('common.all') }}</option>
          <option value="CONVERSATION">{{ t('feedback.type.conversation') }}</option>
          <option value="AGENT_PERFORMANCE">{{ t('feedback.type.agentPerformance') }}</option>
          <option value="BOT_RESPONSE">{{ t('feedback.type.botResponse') }}</option>
          <option value="GENERAL">{{ t('feedback.type.general') }}</option>
        </select>
      </div>
    </div>

    <!-- Feedback List -->
    <div v-if="loading" class="text-center py-8">
      <Icon icon="mdi:loading" class="animate-spin text-4xl text-blue-600" />
      <p class="mt-2 text-gray-600 dark:text-gray-400">{{ t('common.loading') }}</p>
    </div>

    <div v-else-if="error" class="bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-200 p-4 rounded-lg">
      {{ error }}
    </div>

    <div v-else-if="filteredFeedback.length === 0" class="text-center py-8 text-gray-600 dark:text-gray-400">
      <Icon icon="mdi:comment-off" class="text-4xl mb-2" />
      <p>{{ t('feedback.noFeedback') }}</p>
    </div>

    <div v-else class="space-y-4">
      <div
        v-for="feedback in filteredFeedback"
        :key="feedback.id"
        class="bg-white dark:bg-gray-800 rounded-lg shadow p-6"
      >
        <div class="flex justify-between items-start">
          <div class="flex-1">
            <div class="flex items-center gap-3 mb-2">
              <span :class="getStatusClass(feedback.status)" class="px-2 py-1 text-xs font-medium rounded-full">
                {{ t(`feedback.status.${feedback.status?.toLowerCase()}`) }}
              </span>
              <span class="text-sm text-gray-500 dark:text-gray-400">
                {{ t(`feedback.type.${feedback.type?.toLowerCase()}`) }}
              </span>
              <span class="text-sm text-gray-500 dark:text-gray-400">
                {{ formatDate(feedback.submittedAt) }}
              </span>
            </div>
            <div class="flex items-center mb-2">
              <div class="flex">
                <Icon
                  v-for="i in 5"
                  :key="i"
                  :icon="i <= feedback.rating ? 'mdi:star' : 'mdi:star-outline'"
                  :class="i <= feedback.rating ? 'text-yellow-500' : 'text-gray-300 dark:text-gray-600'"
                  class="text-lg"
                />
              </div>
              <span class="ml-2 text-sm text-gray-600 dark:text-gray-400">{{ feedback.rating }}/5</span>
            </div>
            <p v-if="feedback.comment" class="text-gray-900 dark:text-white mb-2">{{ feedback.comment }}</p>
            <p v-if="feedback.category" class="text-sm text-gray-500 dark:text-gray-400">
              {{ t('feedback.category') }}: {{ feedback.category }}
            </p>
            <div v-if="feedback.adminResponse" class="mt-3 p-3 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
              <p class="text-sm text-gray-700 dark:text-gray-300">
                <strong>{{ t('feedback.adminResponse') }}:</strong> {{ feedback.adminResponse }}
              </p>
            </div>
          </div>
          <div class="flex space-x-2">
            <button @click="viewFeedback(feedback)" class="text-blue-600 hover:text-blue-900 dark:text-blue-400 dark:hover:text-blue-300">
              <Icon icon="mdi:eye" class="text-xl" />
            </button>
            <button @click="editFeedback(feedback)" class="text-indigo-600 hover:text-indigo-900 dark:text-indigo-400 dark:hover:text-indigo-300">
              <Icon icon="mdi:pencil" class="text-xl" />
            </button>
            <button @click="deleteFeedback(feedback)" class="text-red-600 hover:text-red-900 dark:text-red-400 dark:hover:text-red-300">
              <Icon icon="mdi:delete" class="text-xl" />
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <div v-if="showModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow-xl max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
        <div class="p-6">
          <h2 class="text-xl font-bold text-gray-900 dark:text-white mb-4">
            {{ isEdit ? t('feedback.editFeedback') : t('feedback.addFeedback') }}
          </h2>
          <form @submit.prevent="saveFeedback">
            <div class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  {{ t('feedback.type') }}
                </label>
                <select
                  v-model="formData.type"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg dark:bg-gray-700 dark:text-white"
                >
                  <option value="CONVERSATION">{{ t('feedback.type.conversation') }}</option>
                  <option value="AGENT_PERFORMANCE">{{ t('feedback.type.agentPerformance') }}</option>
                  <option value="BOT_RESPONSE">{{ t('feedback.type.botResponse') }}</option>
                  <option value="GENERAL">{{ t('feedback.type.general') }}</option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  {{ t('feedback.rating') }} *
                </label>
                <div class="flex gap-2">
                  <button
                    v-for="i in 5"
                    :key="i"
                    type="button"
                    @click="formData.rating = i"
                    class="text-2xl"
                  >
                    <Icon
                      :icon="i <= formData.rating ? 'mdi:star' : 'mdi:star-outline'"
                      :class="i <= formData.rating ? 'text-yellow-500' : 'text-gray-300 dark:text-gray-600'"
                    />
                  </button>
                </div>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  {{ t('feedback.comment') }}
                </label>
                <textarea
                  v-model="formData.comment"
                  rows="3"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg dark:bg-gray-700 dark:text-white"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  {{ t('feedback.category') }}
                </label>
                <input
                  v-model="formData.category"
                  type="text"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg dark:bg-gray-700 dark:text-white"
                />
              </div>
              <div class="flex items-center">
                <input
                  v-model="formData.isAnonymous"
                  type="checkbox"
                  id="anonymous"
                  class="mr-2"
                />
                <label for="anonymous" class="text-sm text-gray-700 dark:text-gray-300">
                  {{ t('feedback.isAnonymous') }}
                </label>
              </div>
            </div>
            <div class="mt-6 flex justify-end space-x-3">
              <button
                type="button"
                @click="closeModal"
                class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700"
              >
                {{ t('common.cancel') }}
              </button>
              <button
                type="submit"
                class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
              >
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
import { Icon } from '@iconify/vue'
import { useI18n } from 'vue-i18n'
import feedbackApi from '@/api/feedbackApi'

const { t } = useI18n()

const feedbackList = ref([])
const loading = ref(true)
const error = ref(null)
const searchQuery = ref('')
const filterStatus = ref('')
const filterType = ref('')
const showModal = ref(false)
const isEdit = ref(false)
const formData = ref({
  type: 'GENERAL',
  rating: 5,
  comment: '',
  category: '',
  isAnonymous: false
})

const filteredFeedback = computed(() => {
  return feedbackList.value.filter(feedback => {
    if (searchQuery.value && !feedback.comment?.toLowerCase().includes(searchQuery.value.toLowerCase())) return false
    if (filterStatus.value && feedback.status !== filterStatus.value) return false
    if (filterType.value && feedback.type !== filterType.value) return false
    return true
  })
})

const loadFeedback = async () => {
  try {
    loading.value = true
    feedbackList.value = await feedbackApi.getFeedback()
  } catch (err) {
    error.value = err.message
    console.error('Load feedback error:', err)
  } finally {
    loading.value = false
  }
}

const getStatusClass = (status) => {
  const classes = {
    PENDING: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200',
    REVIEWED: 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200',
    RESPONDED: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
    CLOSED: 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-200'
  }
  return classes[status] || 'bg-gray-100 text-gray-800'
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString()
}

const openCreateModal = () => {
  isEdit.value = false
  formData.value = {
    type: 'GENERAL',
    rating: 5,
    comment: '',
    category: '',
    isAnonymous: false
  }
  showModal.value = true
}

const editFeedback = (feedback) => {
  isEdit.value = true
  formData.value = {
    type: feedback.type,
    rating: feedback.rating,
    comment: feedback.comment,
    category: feedback.category,
    isAnonymous: feedback.isAnonymous
  }
  showModal.value = true
}

const viewFeedback = (feedback) => {
  console.log('View feedback:', feedback)
  // TODO: Navigate to feedback detail page
}

const saveFeedback = async () => {
  try {
    if (isEdit.value) {
      await feedbackApi.updateFeedback(formData.value.id, formData.value)
    } else {
      await feedbackApi.createFeedback(formData.value)
    }
    closeModal()
    await loadFeedback()
  } catch (err) {
    error.value = err.message
    console.error('Save feedback error:', err)
  }
}

const deleteFeedback = async (feedback) => {
  if (!confirm(t('feedback.confirmDelete'))) return
  try {
    await feedbackApi.deleteFeedback(feedback.id)
    await loadFeedback()
  } catch (err) {
    error.value = err.message
    console.error('Delete feedback error:', err)
  }
}

const closeModal = () => {
  showModal.value = false
  formData.value = {
    type: 'GENERAL',
    rating: 5,
    comment: '',
    category: '',
    isAnonymous: false
  }
}

onMounted(() => {
  loadFeedback()
})
</script>
