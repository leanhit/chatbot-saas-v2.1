<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-900 dark:text-white">{{ t('customer.title') }}</h1>
      <button
        @click="openCreateModal"
        class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
      >
        <Icon icon="mdi:plus" class="inline-block mr-2" />
        {{ t('customer.addCustomer') }}
      </button>
    </div>

    <!-- Filters -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-4 mb-6">
      <div class="flex flex-wrap gap-4">
        <div class="flex-1 min-w-[200px]">
          <input
            v-model="searchQuery"
            type="text"
            :placeholder="t('customer.searchPlaceholder')"
            class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg dark:bg-gray-700 dark:text-white"
          />
        </div>
        <select
          v-model="filterStatus"
          class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg dark:bg-gray-700 dark:text-white"
        >
          <option value="">{{ t('common.all') }}</option>
          <option value="ACTIVE">{{ t('customer.status.active') }}</option>
          <option value="INACTIVE">{{ t('customer.status.inactive') }}</option>
          <option value="BLOCKED">{{ t('customer.status.blocked') }}</option>
          <option value="CHURNED">{{ t('customer.status.churned') }}</option>
        </select>
        <select
          v-model="filterTier"
          class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg dark:bg-gray-700 dark:text-white"
        >
          <option value="">{{ t('common.all') }}</option>
          <option value="FREE">{{ t('customer.tier.free') }}</option>
          <option value="BASIC">{{ t('customer.tier.basic') }}</option>
          <option value="PREMIUM">{{ t('customer.tier.premium') }}</option>
          <option value="ENTERPRISE">{{ t('customer.tier.enterprise') }}</option>
        </select>
      </div>
    </div>

    <!-- Customer List -->
    <div v-if="loading" class="text-center py-8">
      <Icon icon="mdi:loading" class="animate-spin text-4xl text-blue-600" />
      <p class="mt-2 text-gray-600 dark:text-gray-400">{{ t('common.loading') }}</p>
    </div>

    <div v-else-if="error" class="bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-200 p-4 rounded-lg">
      {{ error }}
    </div>

    <div v-else-if="filteredCustomers.length === 0" class="text-center py-8 text-gray-600 dark:text-gray-400">
      <Icon icon="mdi:account-off" class="text-4xl mb-2" />
      <p>{{ t('customer.noCustomers') }}</p>
    </div>

    <div v-else class="bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden">
      <table class="w-full">
        <thead class="bg-gray-50 dark:bg-gray-700">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ t('customer.name') }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ t('customer.email') }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ t('customer.status') }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ t('customer.tier') }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ t('customer.conversations') }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ t('customer.lastContact') }}
            </th>
            <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ t('common.actions') }}
            </th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200 dark:divide-gray-700">
          <tr v-for="customer in filteredCustomers" :key="customer.id" class="hover:bg-gray-50 dark:hover:bg-gray-700">
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="flex items-center">
                <img v-if="customer.avatarUrl" :src="customer.avatarUrl" class="h-10 w-10 rounded-full" />
                <div v-else class="h-10 w-10 rounded-full bg-gray-300 dark:bg-gray-600 flex items-center justify-center">
                  <Icon icon="mdi:account" class="text-gray-500 dark:text-gray-400" />
                </div>
                <div class="ml-4">
                  <div class="text-sm font-medium text-gray-900 dark:text-white">{{ customer.name }}</div>
                  <div class="text-sm text-gray-500 dark:text-gray-400">{{ customer.externalId }}</div>
                </div>
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
              {{ customer.email || '-' }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span :class="getStatusClass(customer.status)" class="px-2 py-1 text-xs font-medium rounded-full">
                {{ t(`customer.status.${customer.status?.toLowerCase()}`) }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
              {{ t(`customer.tier.${customer.tier?.toLowerCase()}`) }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
              {{ customer.totalConversations || 0 }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
              {{ formatDate(customer.lastContactAt) }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
              <button @click="viewCustomer(customer)" class="text-blue-600 hover:text-blue-900 dark:text-blue-400 dark:hover:text-blue-300 mr-3">
                <Icon icon="mdi:eye" class="inline-block" />
              </button>
              <button @click="editCustomer(customer)" class="text-indigo-600 hover:text-indigo-900 dark:text-indigo-400 dark:hover:text-indigo-300 mr-3">
                <Icon icon="mdi:pencil" class="inline-block" />
              </button>
              <button @click="deleteCustomer(customer)" class="text-red-600 hover:text-red-900 dark:text-red-400 dark:hover:text-red-300">
                <Icon icon="mdi:delete" class="inline-block" />
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Create/Edit Modal -->
    <div v-if="showModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow-xl max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
        <div class="p-6">
          <h2 class="text-xl font-bold text-gray-900 dark:text-white mb-4">
            {{ isEdit ? t('customer.editCustomer') : t('customer.addCustomer') }}
          </h2>
          <form @submit.prevent="saveCustomer">
            <div class="grid grid-cols-2 gap-4">
              <div class="col-span-2">
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  {{ t('customer.name') }} *
                </label>
                <input
                  v-model="formData.name"
                  type="text"
                  required
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg dark:bg-gray-700 dark:text-white"
                />
              </div>
              <div class="col-span-2">
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  {{ t('customer.externalId') }} *
                </label>
                <input
                  v-model="formData.externalId"
                  type="text"
                  required
                  :disabled="isEdit"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg dark:bg-gray-700 dark:text-white"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  {{ t('customer.email') }}
                </label>
                <input
                  v-model="formData.email"
                  type="email"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg dark:bg-gray-700 dark:text-white"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  {{ t('customer.phone') }}
                </label>
                <input
                  v-model="formData.phone"
                  type="text"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg dark:bg-gray-700 dark:text-white"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  {{ t('customer.status') }}
                </label>
                <select
                  v-model="formData.status"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg dark:bg-gray-700 dark:text-white"
                >
                  <option value="ACTIVE">{{ t('customer.status.active') }}</option>
                  <option value="INACTIVE">{{ t('customer.status.inactive') }}</option>
                  <option value="BLOCKED">{{ t('customer.status.blocked') }}</option>
                  <option value="CHURNED">{{ t('customer.status.churned') }}</option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  {{ t('customer.tier') }}
                </label>
                <select
                  v-model="formData.tier"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg dark:bg-gray-700 dark:text-white"
                >
                  <option value="FREE">{{ t('customer.tier.free') }}</option>
                  <option value="BASIC">{{ t('customer.tier.basic') }}</option>
                  <option value="PREMIUM">{{ t('customer.tier.premium') }}</option>
                  <option value="ENTERPRISE">{{ t('customer.tier.enterprise') }}</option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  {{ t('customer.language') }}
                </label>
                <input
                  v-model="formData.preferredLanguage"
                  type="text"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg dark:bg-gray-700 dark:text-white"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  {{ t('customer.timezone') }}
                </label>
                <input
                  v-model="formData.timezone"
                  type="text"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg dark:bg-gray-700 dark:text-white"
                />
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
import axios from '@/plugins/axios'

const { t } = useI18n()

const customers = ref([])
const loading = ref(true)
const error = ref(null)
const searchQuery = ref('')
const filterStatus = ref('')
const filterTier = ref('')
const showModal = ref(false)
const isEdit = ref(false)
const formData = ref({
  externalId: '',
  name: '',
  email: '',
  phone: '',
  status: 'ACTIVE',
  tier: 'FREE',
  preferredLanguage: '',
  timezone: ''
})

const filteredCustomers = computed(() => {
  return customers.value.filter(customer => {
    if (searchQuery.value && !customer.name.toLowerCase().includes(searchQuery.value.toLowerCase()) && !customer.email?.toLowerCase().includes(searchQuery.value.toLowerCase())) return false
    if (filterStatus.value && customer.status !== filterStatus.value) return false
    if (filterTier.value && customer.tier !== filterTier.value) return false
    return true
  })
})

const loadCustomers = async () => {
  try {
    loading.value = true
    const response = await axios.get('/api/customer/profiles')
    customers.value = response.data || []
  } catch (err) {
    error.value = err.message
    console.error('Load customers error:', err)
  } finally {
    loading.value = false
  }
}

const getStatusClass = (status) => {
  const classes = {
    ACTIVE: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
    INACTIVE: 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-200',
    BLOCKED: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200',
    CHURNED: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200'
  }
  return classes[status] || 'bg-gray-100 text-gray-800'
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString()
}

const openCreateModal = () => {
  isEdit.value = false
  formData.value = {
    externalId: '',
    name: '',
    email: '',
    phone: '',
    status: 'ACTIVE',
    tier: 'FREE',
    preferredLanguage: '',
    timezone: ''
  }
  showModal.value = true
}

const editCustomer = (customer) => {
  isEdit.value = true
  formData.value = {
    externalId: customer.externalId,
    name: customer.name,
    email: customer.email,
    phone: customer.phone,
    status: customer.status,
    tier: customer.tier,
    preferredLanguage: customer.preferredLanguage,
    timezone: customer.timezone
  }
  showModal.value = true
}

const viewCustomer = (customer) => {
  console.log('View customer:', customer)
  // TODO: Navigate to customer detail page
}

const saveCustomer = async () => {
  try {
    if (isEdit.value) {
      await axios.put(`/api/customer/profiles/${formData.value.id}`, formData.value)
    } else {
      await axios.post('/api/customer/profiles', formData.value)
    }
    closeModal()
    await loadCustomers()
  } catch (err) {
    error.value = err.message
    console.error('Save customer error:', err)
  }
}

const deleteCustomer = async (customer) => {
  if (!confirm(t('customer.confirmDelete'))) return
  try {
    await axios.delete(`/api/customer/profiles/${customer.id}`)
    await loadCustomers()
  } catch (err) {
    error.value = err.message
    console.error('Delete customer error:', err)
  }
}

const closeModal = () => {
  showModal.value = false
  formData.value = {
    externalId: '',
    name: '',
    email: '',
    phone: '',
    status: 'ACTIVE',
    tier: 'FREE',
    preferredLanguage: '',
    timezone: ''
  }
}

onMounted(() => {
  loadCustomers()
})
</script>
