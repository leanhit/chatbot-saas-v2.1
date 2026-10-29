<template>
  <div class="training-detail p-4">
    <!-- Header -->
    <div class="mt-2 w-full">
      <div class="lg:flex grid-cols-1 lg:space-y-0 space-y-3 gap-5 justify-between">
        <div>
          <button @click="goBack" class="flex items-center gap-2 text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white mb-2">
            <Icon icon="mdi:arrow-left" class="text-lg" />
            {{ $t('common.back') }}
          </button>
          <p class="uppercase text-xs text-gray-700 font-semibold">{{ $t('training.detail') }}</p>
          <h1 class="text-2xl text-gray-900 dark:text-gray-200 font-medium">
            {{ material?.title || $t('training.loading') }}
          </h1>
        </div>
        <div class="flex gap-2">
          <button
            @click="editMaterial"
            class="bg-white dark:bg-gray-800 hover:border-gray-200 dark:hover:bg-gray-700 dark:text-white dark:border-gray-700 border rounded py-2 px-5 flex items-center gap-2"
          >
            <Icon icon="mdi:pencil" class="text-lg" />
            {{ $t('training.edit') }}
          </button>
          <button
            @click="publishMaterial"
            v-if="material?.status === 'DRAFT'"
            class="bg-green-600 hover:bg-green-700 text-white rounded py-2 px-5 flex items-center gap-2"
          >
            <Icon icon="mdi:publish" class="text-lg" />
            {{ $t('training.publish') }}
          </button>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="mt-6 flex justify-center">
      <Icon icon="mdi:loading" class="text-4xl animate-spin text-gray-500" />
    </div>

    <!-- Content -->
    <div v-else-if="material" class="mt-6 space-y-6">
      <!-- Overview Card -->
      <div class="bg-white dark:bg-gray-800 rounded-md p-6 border dark:border-gray-700">
        <h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">{{ $t('training.overview') }}</h2>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-500 dark:text-gray-400">{{ $t('training.category') }}</label>
            <p class="text-gray-900 dark:text-white">{{ getCategoryName(material.categoryId) }}</p>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-500 dark:text-gray-400">{{ $t('training.type') }}</label>
            <p class="text-gray-900 dark:text-white">{{ $t(`training.types.${material.type.toLowerCase()}`) }}</p>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-500 dark:text-gray-400">{{ $t('training.status') }}</label>
            <span class="px-2 py-1 text-xs rounded-full" :class="getStatusClass(material.status)">
              {{ $t(`training.statuses.${material.status.toLowerCase()}`) }}
            </span>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-500 dark:text-gray-400">{{ $t('training.duration') }}</label>
            <p class="text-gray-900 dark:text-white">{{ material.estimatedDurationMinutes }} {{ $t('training.minutes') }}</p>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-500 dark:text-gray-400">{{ $t('training.difficulty') }}</label>
            <p class="text-gray-900 dark:text-white">{{ $t(`training.difficulty.${material.difficultyLevel?.toLowerCase()}`) }}</p>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-500 dark:text-gray-400">{{ $t('training.mandatory') }}</label>
            <p class="text-gray-900 dark:text-white">{{ material.isMandatory ? $t('common.yes') : $t('common.no') }}</p>
          </div>
        </div>
      </div>

      <!-- Description Card -->
      <div class="bg-white dark:bg-gray-800 rounded-md p-6 border dark:border-gray-700">
        <h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">{{ $t('training.description') }}</h2>
        <p class="text-gray-700 dark:text-gray-300 whitespace-pre-wrap">{{ material.description }}</p>
      </div>

      <!-- Content Card -->
      <div class="bg-white dark:bg-gray-800 rounded-md p-6 border dark:border-gray-700">
        <h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">{{ $t('training.content') }}</h2>
        <div class="text-gray-700 dark:text-gray-300 whitespace-pre-wrap">{{ material.content }}</div>
      </div>

      <!-- Required Skills -->
      <div class="bg-white dark:bg-gray-800 rounded-md p-6 border dark:border-gray-700">
        <h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">{{ $t('training.requiredSkills') }}</h2>
        <div class="flex flex-wrap gap-2">
          <span v-for="skill in material.requiredSkills" :key="skill" class="px-3 py-1 bg-blue-100 text-blue-800 rounded-full text-sm dark:bg-blue-900 dark:text-blue-200">
            {{ skill }}
          </span>
        </div>
      </div>

      <!-- Quiz Configuration (if applicable) -->
      <div v-if="material.type === 'QUIZ'" class="bg-white dark:bg-gray-800 rounded-md p-6 border dark:border-gray-700">
        <h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">{{ $t('training.quizConfig') }}</h2>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-500 dark:text-gray-400">{{ $t('training.passScore') }}</label>
            <p class="text-gray-900 dark:text-white">{{ material.passScoreThreshold }}%</p>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-500 dark:text-gray-400">{{ $t('training.quizQuestions') }}</label>
            <p class="text-gray-900 dark:text-white">{{ material.quizQuestions?.length || 0 }} {{ $t('training.questions') }}</p>
          </div>
        </div>
      </div>

      <!-- Agent Progress -->
      <div class="bg-white dark:bg-gray-800 rounded-md p-6 border dark:border-gray-700">
        <h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">{{ $t('training.agentProgress') }}</h2>
        <div class="overflow-x-auto">
          <table class="w-full">
            <thead class="bg-gray-50 dark:bg-gray-700">
              <tr>
                <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">
                  {{ $t('training.agent') }}
                </th>
                <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">
                  {{ $t('training.status') }}
                </th>
                <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">
                  {{ $t('training.progress') }}
                </th>
                <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">
                  {{ $t('training.quizScore') }}
                </th>
                <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">
                  {{ $t('training.completedAt') }}
                </th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-200 dark:divide-gray-700">
              <tr v-for="progress in agentProgress" :key="progress.id" class="hover:bg-gray-50 dark:hover:bg-gray-700">
                <td class="px-4 py-3 text-gray-900 dark:text-white">{{ progress.agentName }}</td>
                <td class="px-4 py-3">
                  <span class="px-2 py-1 text-xs rounded-full" :class="getProgressStatusClass(progress.status)">
                    {{ $t(`training.progressStatus.${progress.status.toLowerCase()}`) }}
                  </span>
                </td>
                <td class="px-4 py-3 text-gray-700 dark:text-gray-300">{{ progress.progress }}%</td>
                <td class="px-4 py-3 text-gray-700 dark:text-gray-300">{{ progress.quizScore || '-' }}</td>
                <td class="px-4 py-3 text-gray-700 dark:text-gray-300">{{ progress.completedAt || '-' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'

const route = useRoute()
const router = useRouter()

const material = ref(null)
const categories = ref([])
const agentProgress = ref([])
const loading = ref(true)

const getCategoryName = (categoryId) => {
  const category = categories.value.find(c => c.id === categoryId)
  return category ? category.name : '-'
}

const getStatusClass = (status) => {
  const classes = {
    DRAFT: 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-200',
    PUBLISHED: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
    ARCHIVED: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
  }
  return classes[status] || 'bg-gray-100 text-gray-800'
}

const getProgressStatusClass = (status) => {
  const classes = {
    NOT_STARTED: 'bg-gray-100 text-gray-800',
    IN_PROGRESS: 'bg-blue-100 text-blue-800',
    COMPLETED: 'bg-green-100 text-green-800',
    FAILED: 'bg-red-100 text-red-800'
  }
  return classes[status] || 'bg-gray-100 text-gray-800'
}

const goBack = () => {
  router.push('/training/library')
}

const editMaterial = () => {
  // TODO: Open edit modal
  console.log('Edit material:', material.value)
}

const publishMaterial = () => {
  // TODO: Publish material
  console.log('Publish material:', material.value)
}

onMounted(async () => {
  const materialId = route.params.id
  // TODO: Load material details from API
  loading.value = false
})
</script>
