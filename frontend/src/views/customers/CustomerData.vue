<template>
  <div class="customer-data-page">
    <!-- Header -->
    <div class="page-header">
      <div class="flex justify-between items-center">
        <div>
          <h1 class="text-2xl font-bold text-gray-900 dark:text-white">
            {{ $t('customers.title') }}
          </h1>
          <p class="text-gray-600 dark:text-gray-400 mt-1">
            {{ $t('customers.subtitle') }}
          </p>
        </div>
        
        <!-- Stats Cards -->
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mt-6">
          <div class="bg-white dark:bg-gray-800 p-4 rounded-lg shadow">
            <div class="text-sm text-gray-600 dark:text-gray-400">{{ $t('customers.stats.total') }}</div>
            <div class="text-2xl font-bold text-blue-600">{{ stats.totalCustomers || 0 }}</div>
          </div>
          <div class="bg-white dark:bg-gray-800 p-4 rounded-lg shadow">
            <div class="text-sm text-gray-600 dark:text-gray-400">{{ $t('customers.stats.pending') }}</div>
            <div class="text-2xl font-bold text-yellow-600">{{ stats.pendingCustomers || 0 }}</div>
          </div>
          <div class="bg-white dark:bg-gray-800 p-4 rounded-lg shadow">
            <div class="text-sm text-gray-600 dark:text-gray-400">{{ $t('customers.stats.completed') }}</div>
            <div class="text-2xl font-bold text-green-600">{{ stats.completedCustomers || 0 }}</div>
          </div>
          <div class="bg-white dark:bg-gray-800 p-4 rounded-lg shadow">
            <div class="text-sm text-gray-600 dark:text-gray-400">{{ $t('customers.stats.synced') }}</div>
            <div class="text-2xl font-bold text-purple-600">{{ stats.syncedCustomers || 0 }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Filters and Search -->
    <div class="filters-section bg-white dark:bg-gray-800 p-4 rounded-lg shadow mb-6">
      <div class="flex flex-col md:flex-row gap-4">
        <!-- Search -->
        <div class="flex-1">
          <div class="relative">
            <Icon icon="mdi:magnify" class="absolute left-3 top-3 text-gray-400" />
            <input
              v-model="searchKeyword"
              type="text"
              :placeholder="$t('customers.searchPlaceholder')"
              class="w-full pl-10 pr-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
              @input="onSearch"
            />
          </div>
        </div>

        <!-- Status Filter -->
        <div class="w-full md:w-48">
          <select
            v-model="selectedStatus"
            class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
            @change="onStatusChange"
          >
            <option value="">{{ $t('customers.allStatuses') }}</option>
            <option v-for="status in availableStatuses" :key="status" :value="status">
              {{ $t(`customers.status.${status}`) }}
            </option>
          </select>
        </div>

        <!-- Refresh Button -->
        <button
          @click="refreshData"
          :disabled="loading"
          class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 flex items-center gap-2"
        >
          <Icon icon="mdi:refresh" :class="{ 'animate-spin': loading }" />
          {{ $t('common.refresh') }}
        </button>
      </div>
    </div>

    <!-- Customer List -->
    <div class="customer-list bg-white dark:bg-gray-800 rounded-lg shadow">
      <!-- Loading State -->
      <div v-if="loading" class="p-8 text-center">
        <Icon icon="mdi:loading" class="animate-spin text-4xl text-blue-600 mb-4" />
        <p class="text-gray-600 dark:text-gray-400">{{ $t('common.loading') }}</p>
      </div>

      <!-- Empty State -->
      <div v-else-if="customers.length === 0" class="p-8 text-center">
        <Icon icon="mdi:account-search" class="text-4xl text-gray-400 mb-4" />
        <p class="text-gray-600 dark:text-gray-400">{{ $t('customers.noData') }}</p>
      </div>

      <!-- Customer Table -->
      <div v-else class="overflow-x-auto">
        <table class="w-full">
          <thead class="bg-gray-50 dark:bg-gray-700">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                {{ $t('customers.headers.customer') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                {{ $t('customers.headers.contact') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                {{ $t('customers.headers.status') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                {{ $t('customers.headers.lastUpdated') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                {{ $t('customers.headers.actions') }}
              </th>
            </tr>
          </thead>
          <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
            <tr v-for="customer in customers" :key="customer.psid" class="hover:bg-gray-50 dark:hover:bg-gray-700">
              <!-- Customer Info -->
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex items-center">
                  <div class="flex-shrink-0 h-10 w-10">
                    <img
                      v-if="customer.displayAvatar"
                      :src="customer.displayAvatar"
                      :alt="customer.displayName"
                      class="h-10 w-10 rounded-full object-cover"
                    />
                    <div
                      v-else
                      class="h-10 w-10 rounded-full bg-gray-300 dark:bg-gray-600 flex items-center justify-center"
                    >
                      <Icon icon="mdi:account" class="text-gray-500" />
                    </div>
                  </div>
                  <div class="ml-4">
                    <div class="text-sm font-medium text-gray-900 dark:text-white">
                      {{ customer.displayName }}
                    </div>
                    <div class="text-sm text-gray-500 dark:text-gray-400">
                      PSID: {{ customer.psid.substring(0, 8) }}...
                    </div>
                  </div>
                </div>
              </td>

              <!-- Contact Info -->
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm text-gray-900 dark:text-white">
                  <div v-if="customer.primaryPhone" class="flex items-center gap-1">
                    <Icon icon="mdi:phone" class="text-gray-400" />
                    {{ customer.primaryPhone }}
                  </div>
                  <div v-if="customer.totalPhones > 1" class="text-xs text-gray-500">
                    +{{ customer.totalPhones - 1 }} {{ $t('customers.morePhones') }}
                  </div>
                  <div v-if="!customer.primaryPhone" class="text-gray-400">
                    {{ $t('customers.noPhone') }}
                  </div>
                </div>
              </td>

              <!-- Status -->
              <td class="px-6 py-4 whitespace-nowrap">
                <span
                  :class="getStatusClass(customer.status)"
                  class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full"
                >
                  {{ $t(`customers.status.${customer.status}`) }}
                </span>
                <div v-if="customer.isSyncedWithOdoo" class="text-xs text-green-600 mt-1">
                  <Icon icon="mdi:check-circle" class="inline" />
                  {{ $t('customers.syncedWithOdoo') }}
                </div>
              </td>

              <!-- Last Updated -->
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                {{ formatDate(customer.updatedAt) }}
              </td>

              <!-- Actions -->
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                <button
                  @click="viewCustomerDetails(customer)"
                  class="text-blue-600 hover:text-blue-900 dark:text-blue-400 dark:hover:text-blue-300 mr-3"
                >
                  {{ $t('common.view') }}
                </button>
                <button
                  v-if="customer.status === 'PENDING'"
                  @click="processCustomer(customer)"
                  class="text-green-600 hover:text-green-900 dark:text-green-400 dark:hover:text-green-300"
                >
                  {{ $t('customers.process') }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="px-6 py-4 border-t border-gray-200 dark:border-gray-700">
        <div class="flex items-center justify-between">
          <div class="text-sm text-gray-700 dark:text-gray-300">
            {{ $t('customers.showing', { 
              start: currentPage * pageSize + 1, 
              end: Math.min((currentPage + 1) * pageSize, totalElements), 
              total: totalElements 
            }) }}
          </div>
          <div class="flex gap-2">
            <button
              @click="previousPage"
              :disabled="currentPage === 0"
              class="px-3 py-1 border border-gray-300 dark:border-gray-600 rounded disabled:opacity-50"
            >
              {{ $t('common.previous') }}
            </button>
            <button
              @click="nextPage"
              :disabled="currentPage >= totalPages - 1"
              class="px-3 py-1 border border-gray-300 dark:border-gray-600 rounded disabled:opacity-50"
            >
              {{ $t('common.next') }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Customer Details Modal -->
    <CustomerDetailsModal
      v-if="showDetailsModal"
      :customer="selectedCustomer"
      @close="showDetailsModal = false"
    />
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import CustomerDetailsModal from './components/CustomerDetailsModal.vue'
import { useI18n } from 'vue-i18n'
import axios from 'axios'

export default {
  name: 'CustomerData',
  components: {
    Icon,
    CustomerDetailsModal
  },
  setup() {
    const { t } = useI18n()
    
    // Reactive data
    const loading = ref(false)
    const customers = ref([])
    const stats = ref({})
    const availableStatuses = ref([])
    const searchKeyword = ref('')
    const selectedStatus = ref('')
    const currentPage = ref(0)
    const pageSize = ref(20)
    const totalElements = ref(0)
    const totalPages = ref(0)
    const showDetailsModal = ref(false)
    const selectedCustomer = ref(null)

    // Computed properties
    const searchParams = computed(() => {
      const params = {
        page: currentPage.value,
        size: pageSize.value
      }
      
      if (searchKeyword.value.trim()) {
        params.keyword = searchKeyword.value.trim()
      }
      
      if (selectedStatus.value) {
        // Use status filter endpoint instead of search
        return null // Will be handled in onStatusChange
      }
      
      return params
    })

    // Methods
    const fetchCustomers = async () => {
      loading.value = true
      try {
        let response
        
        if (selectedStatus.value) {
          // Use status filter
          response = await axios.get(`/api/odoo/customers/status/${selectedStatus.value}`, {
            params: { page: currentPage.value, size: pageSize.value }
          })
        } else if (searchKeyword.value.trim()) {
          // Use search
          response = await axios.get('/api/odoo/customers/search', {
            params: { 
              keyword: searchKeyword.value.trim(),
              page: currentPage.value, 
              size: pageSize.value 
            }
          })
        } else {
          // Get all
          response = await axios.get('/api/odoo/customers', {
            params: { 
              page: currentPage.value, 
              size: pageSize.value 
            }
          })
        }

        customers.value = response.data.content || []
        totalElements.value = response.data.totalElements || 0
        totalPages.value = response.data.totalPages || 0
      } catch (error) {
        console.error('Error fetching customers:', error)
        // Show error notification
      } finally {
        loading.value = false
      }
    }

    const fetchStats = async () => {
      try {
        const response = await axios.get('/api/odoo/customers/stats')
        stats.value = response.data
      } catch (error) {
        console.error('Error fetching stats:', error)
      }
    }

    const fetchStatuses = async () => {
      try {
        const response = await axios.get('/api/odoo/customers/statuses')
        availableStatuses.value = response.data
      } catch (error) {
        console.error('Error fetching statuses:', error)
      }
    }

    const refreshData = async () => {
      await Promise.all([
        fetchCustomers(),
        fetchStats(),
        fetchStatuses()
      ])
    }

    const onSearch = () => {
      currentPage.value = 0
      fetchCustomers()
    }

    const onStatusChange = () => {
      currentPage.value = 0
      fetchCustomers()
    }

    const previousPage = () => {
      if (currentPage.value > 0) {
        currentPage.value--
        fetchCustomers()
      }
    }

    const nextPage = () => {
      if (currentPage.value < totalPages.value - 1) {
        currentPage.value++
        fetchCustomers()
      }
    }

    const viewCustomerDetails = (customer) => {
      selectedCustomer.value = customer
      showDetailsModal.value = true
    }

    const processCustomer = (customer) => {
      // TODO: Implement customer processing logic
      console.log('Processing customer:', customer)
    }

    const getStatusClass = (status) => {
      const classes = {
        'PENDING': 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200',
        'COMPLETED': 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200',
        'PUSHED_TO_ODOO': 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
        'FAILED': 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
      }
      return classes[status] || 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
    }

    const formatDate = (dateString) => {
      if (!dateString) return '-'
      const date = new Date(dateString)
      return date.toLocaleDateString() + ' ' + date.toLocaleTimeString()
    }

    // Lifecycle
    onMounted(() => {
      refreshData()
    })

    return {
      // Reactive data
      loading,
      customers,
      stats,
      availableStatuses,
      searchKeyword,
      selectedStatus,
      currentPage,
      pageSize,
      totalElements,
      totalPages,
      showDetailsModal,
      selectedCustomer,
      
      // Methods
      refreshData,
      onSearch,
      onStatusChange,
      previousPage,
      nextPage,
      viewCustomerDetails,
      processCustomer,
      getStatusClass,
      formatDate,
      
      // i18n
      t
    }
  }
}
</script>

<style scoped>
.customer-data-page {
  padding: 1.5rem;
  max-width: 100%;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 2rem;
}

.filters-section {
  margin-bottom: 1.5rem;
}

.animate-spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
