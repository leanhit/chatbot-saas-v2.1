<template>
  <div class="training-library p-4">
    <!-- Header -->
    <div class="mt-2 w-full">
      <div class="lg:flex grid-cols-1 lg:space-y-0 space-y-3 gap-5 justify-between">
        <div>
          <p class="uppercase text-xs text-gray-700 font-semibold">{{ $t('training.library') }}</p>
          <h1 class="text-2xl text-gray-900 dark:text-gray-200 font-medium">
            {{ $t('training.title') }}
          </h1>
        </div>
        <div class="flex gap-2">
          <button
            @click="openCreateModal"
            class="bg-primary border flex gap-2 text-white hover:bg-primary/80 dark:border-gray-700 rounded py-3 px-5"
          >
            <span class="icon text-2xl"><Icon icon="ic:twotone-plus" /></span>
            <span class="text">{{ $t('training.createMaterial') }}</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Filters -->
    <div class="mt-6 bg-white dark:bg-gray-800 rounded-md p-4 border dark:border-gray-700">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('training.category') }}
          </label>
          <select v-model="filters.category" class="w-full border rounded-md p-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white">
            <option value="">{{ $t('common.all') }}</option>
            <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('training.type') }}
          </label>
          <select v-model="filters.type" class="w-full border rounded-md p-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white">
            <option value="">{{ $t('common.all') }}</option>
            <option value="DOCUMENT">{{ $t('training.types.document') }}</option>
            <option value="VIDEO">{{ $t('training.types.video') }}</option>
            <option value="QUIZ">{{ $t('training.types.quiz') }}</option>
            <option value="INTERACTIVE">{{ $t('training.types.interactive') }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('training.status') }}
          </label>
          <select v-model="filters.status" class="w-full border rounded-md p-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white">
            <option value="">{{ $t('common.all') }}</option>
            <option value="DRAFT">{{ $t('training.statuses.draft') }}</option>
            <option value="PUBLISHED">{{ $t('training.statuses.published') }}</option>
            <option value="ARCHIVED">{{ $t('training.statuses.archived') }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('training.search') }}
          </label>
          <input
            v-model="filters.search"
            type="text"
            :placeholder="$t('training.searchPlaceholder')"
            class="w-full border rounded-md p-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
          />
        </div>
      </div>
    </div>

    <!-- Training Materials List -->
    <div class="mt-6 bg-white dark:bg-gray-800 rounded-md border dark:border-gray-700">
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead class="bg-gray-50 dark:bg-gray-700">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">
                {{ $t('training.title') }}
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">
                {{ $t('training.category') }}
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">
                {{ $t('training.type') }}
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">
                {{ $t('training.status') }}
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">
                {{ $t('training.duration') }}
              </th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">
                {{ $t('training.actions') }}
              </th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200 dark:divide-gray-700">
            <tr v-for="material in filteredMaterials" :key="material.id" class="hover:bg-gray-50 dark:hover:bg-gray-700">
              <td class="px-4 py-3">
                <div class="font-medium text-gray-900 dark:text-white">{{ material.title }}</div>
                <div class="text-sm text-gray-500 dark:text-gray-400">{{ material.description }}</div>
              </td>
              <td class="px-4 py-3 text-gray-700 dark:text-gray-300">
                {{ getCategoryName(material.categoryId) }}
              </td>
              <td class="px-4 py-3">
                <span class="px-2 py-1 text-xs rounded-full" :class="getTypeClass(material.type)">
                  {{ $t(`training.types.${material.type.toLowerCase()}`) }}
                </span>
              </td>
              <td class="px-4 py-3">
                <span class="px-2 py-1 text-xs rounded-full" :class="getStatusClass(material.status)">
                  {{ $t(`training.statuses.${material.status.toLowerCase()}`) }}
                </span>
              </td>
              <td class="px-4 py-3 text-gray-700 dark:text-gray-300">
                {{ material.estimatedDurationMinutes }} {{ $t('training.minutes') }}
              </td>
              <td class="px-4 py-3">
                <div class="flex gap-2">
                  <button @click="viewMaterial(material)" class="text-blue-600 hover:text-blue-800 dark:text-blue-400">
                    <Icon icon="mdi:eye" class="text-lg" />
                  </button>
                  <button @click="editMaterial(material)" class="text-green-600 hover:text-green-800 dark:text-green-400">
                    <Icon icon="mdi:pencil" class="text-lg" />
                  </button>
                  <button @click="deleteMaterial(material)" class="text-red-600 hover:text-red-800 dark:text-red-400">
                    <Icon icon="mdi:delete" class="text-lg" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Icon } from '@iconify/vue'

const materials = ref([])
const categories = ref([])

const filters = ref({
  category: '',
  type: '',
  status: '',
  search: ''
})

const filteredMaterials = computed(() => {
  return materials.value.filter(material => {
    if (filters.value.category && material.categoryId !== filters.value.category) return false
    if (filters.value.type && material.type !== filters.value.type) return false
    if (filters.value.status && material.status !== filters.value.status) return false
    if (filters.value.search && !material.title.toLowerCase().includes(filters.value.search.toLowerCase())) return false
    return true
  })
})

const getCategoryName = (categoryId) => {
  const category = categories.value.find(c => c.id === categoryId)
  return category ? category.name : '-'
}

const getTypeClass = (type) => {
  const classes = {
    DOCUMENT: 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200',
    VIDEO: 'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200',
    QUIZ: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
    INTERACTIVE: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200'
  }
  return classes[type] || 'bg-gray-100 text-gray-800'
}

const getStatusClass = (status) => {
  const classes = {
    DRAFT: 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-200',
    PUBLISHED: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
    ARCHIVED: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
  }
  return classes[status] || 'bg-gray-100 text-gray-800'
}

const openCreateModal = () => {
  // TODO: Open create modal
  console.log('Open create modal')
}

const viewMaterial = (material) => {
  // TODO: Navigate to detail page
  console.log('View material:', material)
}

const editMaterial = (material) => {
  // TODO: Open edit modal
  console.log('Edit material:', material)
}

const deleteMaterial = (material) => {
  // TODO: Delete material
  console.log('Delete material:', material)
}

onMounted(() => {
  // TODO: Load materials and categories from API
  materials.value = []
  categories.value = []
})
</script>
