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
      <!-- Loading State -->
      <div v-if="loading" class="p-8 text-center text-gray-500 dark:text-gray-400">
        {{ $t('training.loading') }}
      </div>
      
      <!-- Error State -->
      <div v-else-if="error" class="p-8 text-center text-red-500 dark:text-red-400">
        {{ error }}
      </div>
      
      <!-- Empty State -->
      <div v-else-if="filteredMaterials.length === 0" class="p-8 text-center text-gray-500 dark:text-gray-400">
        No training materials found
      </div>
      
      <!-- Data Table -->
      <div v-else class="overflow-x-auto">
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

    <!-- Create Material Modal -->
    <div v-if="showCreateModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-2xl mx-4">
        <h2 class="text-xl font-semibold mb-4 text-gray-900 dark:text-white">{{ $t('training.createMaterial') }}</h2>
        <form @submit.prevent="saveMaterial">
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('training.title') }}</label>
              <input v-model="formData.title" type="text" required class="w-full border rounded-md p-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('training.description') }}</label>
              <textarea v-model="formData.description" rows="3" class="w-full border rounded-md p-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white"></textarea>
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('training.type') }}</label>
                <select v-model="formData.type" class="w-full border rounded-md p-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white">
                  <option value="DOCUMENT">{{ $t('training.types.document') }}</option>
                  <option value="VIDEO">{{ $t('training.types.video') }}</option>
                  <option value="QUIZ">{{ $t('training.types.quiz') }}</option>
                  <option value="INTERACTIVE">{{ $t('training.types.interactive') }}</option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('training.category') }}</label>
                <select v-model="formData.categoryId" class="w-full border rounded-md p-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white">
                  <option value="">Select category</option>
                  <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
                </select>
              </div>
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('training.status') }}</label>
                <select v-model="formData.status" class="w-full border rounded-md p-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white">
                  <option value="DRAFT">{{ $t('training.statuses.draft') }}</option>
                  <option value="PUBLISHED">{{ $t('training.statuses.published') }}</option>
                  <option value="ARCHIVED">{{ $t('training.statuses.archived') }}</option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('training.duration') }} ({{ $t('training.minutes') }})</label>
                <input v-model.number="formData.estimatedDurationMinutes" type="number" min="0" class="w-full border rounded-md p-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white" />
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('training.content') }}</label>
              <textarea v-model="formData.content" rows="5" class="w-full border rounded-md p-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white"></textarea>
            </div>
          </div>
          <div class="flex justify-end gap-2 mt-6">
            <button type="button" @click="closeCreateModal" class="px-4 py-2 border rounded-md hover:bg-gray-100 dark:hover:bg-gray-700 dark:border-gray-600 dark:text-white">
              {{ $t('common.cancel') }}
            </button>
            <button type="submit" class="px-4 py-2 bg-primary text-white rounded-md hover:bg-primary/80">
              {{ $t('common.save') }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Edit Material Modal -->
    <div v-if="showEditModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-2xl mx-4">
        <h2 class="text-xl font-semibold mb-4 text-gray-900 dark:text-white">{{ $t('training.edit') }}</h2>
        <form @submit.prevent="saveMaterial">
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('training.title') }}</label>
              <input v-model="formData.title" type="text" required class="w-full border rounded-md p-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('training.description') }}</label>
              <textarea v-model="formData.description" rows="3" class="w-full border rounded-md p-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white"></textarea>
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('training.type') }}</label>
                <select v-model="formData.type" class="w-full border rounded-md p-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white">
                  <option value="DOCUMENT">{{ $t('training.types.document') }}</option>
                  <option value="VIDEO">{{ $t('training.types.video') }}</option>
                  <option value="QUIZ">{{ $t('training.types.quiz') }}</option>
                  <option value="INTERACTIVE">{{ $t('training.types.interactive') }}</option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('training.category') }}</label>
                <select v-model="formData.categoryId" class="w-full border rounded-md p-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white">
                  <option value="">Select category</option>
                  <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
                </select>
              </div>
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('training.status') }}</label>
                <select v-model="formData.status" class="w-full border rounded-md p-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white">
                  <option value="DRAFT">{{ $t('training.statuses.draft') }}</option>
                  <option value="PUBLISHED">{{ $t('training.statuses.published') }}</option>
                  <option value="ARCHIVED">{{ $t('training.statuses.archived') }}</option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('training.duration') }} ({{ $t('training.minutes') }})</label>
                <input v-model.number="formData.estimatedDurationMinutes" type="number" min="0" class="w-full border rounded-md p-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white" />
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('training.content') }}</label>
              <textarea v-model="formData.content" rows="5" class="w-full border rounded-md p-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white"></textarea>
            </div>
          </div>
          <div class="flex justify-end gap-2 mt-6">
            <button type="button" @click="closeEditModal" class="px-4 py-2 border rounded-md hover:bg-gray-100 dark:hover:bg-gray-700 dark:border-gray-600 dark:text-white">
              {{ $t('common.cancel') }}
            </button>
            <button type="submit" class="px-4 py-2 bg-primary text-white rounded-md hover:bg-primary/80">
              {{ $t('common.save') }}
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
import { useRouter } from 'vue-router'
import { trainingApi } from '@/api/trainingApi'

const router = useRouter()
const materials = ref([])
const categories = ref([])
const loading = ref(false)
const error = ref(null)
const showCreateModal = ref(false)
const showEditModal = ref(false)
const editingMaterial = ref(null)

const formData = ref({
  title: '',
  description: '',
  type: 'DOCUMENT',
  categoryId: '',
  status: 'DRAFT',
  estimatedDurationMinutes: 0,
  difficultyLevel: 'BEGINNER',
  isMandatory: false,
  content: ''
})

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
  formData.value = {
    title: '',
    description: '',
    type: 'DOCUMENT',
    categoryId: '',
    status: 'DRAFT',
    estimatedDurationMinutes: 0,
    difficultyLevel: 'BEGINNER',
    isMandatory: false,
    content: ''
  }
  showCreateModal.value = true
}

const viewMaterial = (material) => {
  router.push({ name: 'training-detail', params: { id: material.id } })
}

const editMaterial = (material) => {
  editingMaterial.value = material
  formData.value = {
    title: material.title,
    description: material.description,
    type: material.type,
    categoryId: material.categoryId,
    status: material.status,
    estimatedDurationMinutes: material.estimatedDurationMinutes,
    difficultyLevel: material.difficultyLevel,
    isMandatory: material.isMandatory,
    content: material.content
  }
  showEditModal.value = true
}

const deleteMaterial = async (material) => {
  if (!confirm($t('training.confirmDelete'))) return
  
  try {
    await trainingApi.deleteMaterial(material.id)
    materials.value = materials.value.filter(m => m.id !== material.id)
  } catch (err) {
    error.value = err.message
    console.error('Delete material error:', err)
  }
}

const saveMaterial = async () => {
  try {
    if (showCreateModal.value) {
      const response = await trainingApi.createMaterial(formData.value)
      materials.value.push(response.data)
      showCreateModal.value = false
    } else if (showEditModal.value && editingMaterial.value) {
      const response = await trainingApi.updateMaterial(editingMaterial.value.id, formData.value)
      const index = materials.value.findIndex(m => m.id === editingMaterial.value.id)
      if (index !== -1) {
        materials.value[index] = response.data
      }
      showEditModal.value = false
      editingMaterial.value = null
    }
  } catch (err) {
    error.value = err.message
    console.error('Save material error:', err)
  }
}

const closeCreateModal = () => {
  showCreateModal.value = false
  formData.value = {
    title: '',
    description: '',
    type: 'DOCUMENT',
    categoryId: '',
    status: 'DRAFT',
    estimatedDurationMinutes: 0,
    difficultyLevel: 'BEGINNER',
    isMandatory: false,
    content: ''
  }
}

const closeEditModal = () => {
  showEditModal.value = false
  editingMaterial.value = null
  formData.value = {
    title: '',
    description: '',
    type: 'DOCUMENT',
    categoryId: '',
    status: 'DRAFT',
    estimatedDurationMinutes: 0,
    difficultyLevel: 'BEGINNER',
    isMandatory: false,
    content: ''
  }
}

const loadMaterials = async () => {
  loading.value = true
  error.value = null
  try {
    const response = await trainingApi.getAllMaterials()
    materials.value = response.data || []
  } catch (err) {
    error.value = err.message
    console.error('Load materials error:', err)
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    const response = await trainingApi.getAllCategories()
    categories.value = response.data || []
  } catch (err) {
    console.error('Load categories error:', err)
  }
}

onMounted(() => {
  loadMaterials()
  loadCategories()
})
</script>
