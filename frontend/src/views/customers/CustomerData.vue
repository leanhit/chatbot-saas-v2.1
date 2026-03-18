<template>
  <div class="customer-data-page p-4">
    <!-- Header -->
    <div class="mb-6">
      <div class="flex justify-between items-center">
        <div>
          <p class="uppercase text-xs text-gray-700 dark:text-gray-300 font-semibold">{{ $t('customers.overview') }}</p>
          <h1 class="text-2xl text-gray-900 dark:text-gray-200 font-medium">
            {{ $t('customers.title') }}
          </h1>
        </div>
        <div class="flex gap-2">
          <button
            @click="refreshData"
            :disabled="loading"
            class="bg-white dark:bg-gray-800 hover:border-gray-200 dark:hover:bg-gray-700 dark:text-white dark:border-gray-700 border rounded py-2 px-5 flex items-center gap-2"
          >
            <Icon v-if="loading" icon="mdi:loading" class="animate-spin" />
            <Icon v-else icon="mdi:refresh" />
            {{ $t('common.refresh') }}
          </button>
          <button
            @click="showSearchModal = true"
            class="bg-primary border flex gap-2 text-white hover:bg-primary/80 dark:border-gray-700 rounded py-3 px-5"
          >
            <Icon icon="mdi:magnify" />
            {{ $t('common.search') }}
          </button>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="flex gap-4" style="min-height: 600px;">
      <!-- Customers List -->
      <div class="w-full lg:w-1/3 bg-white dark:bg-gray-800 rounded-lg border dark:border-gray-700">
        <div class="p-4 border-b dark:border-gray-700">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-lg font-semibold text-gray-900 dark:text-gray-200">{{ $t('customers.list') }}</h2>
            <div class="flex items-center gap-2">
              <!-- Status Filter -->
              <select v-model="selectedStatus" @change="onStatusChange" 
                class="text-sm border rounded px-2 py-1 dark:bg-gray-700 dark:border-gray-600 dark:text-white">
                <option value="">{{ $t('customers.allStatuses') }}</option>
                <option v-for="status in availableStatuses" :key="status" :value="status">
                  {{ $t(`customers.status.${status}`) }}
                </option>
              </select>
            </div>
          </div>
          
          <!-- Selection Controls -->
          <div v-if="customers.length > 0" class="flex items-center justify-between">
            <!-- Select All -->
            <div class="flex items-center gap-2">
              <input
                type="checkbox"
                :checked="allSelected"
                @change="toggleSelectAll"
                class="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
              />
              <label 
                @click="toggleSelectAll"
                class="text-sm text-gray-600 dark:text-gray-400 cursor-pointer hover:text-gray-800 dark:hover:text-gray-200"
              >
                select all
              </label>
            </div>
            
            <!-- Process with Count -->
            <div v-if="selectedCustomers.size > 0" class="flex items-center gap-2">
              <button
                @click="processSelectedCustomers"
                class="text-sm bg-green-600 hover:bg-green-700 text-white px-3 py-1 rounded flex items-center gap-1"
              >
                <Icon icon="mdi:check" />
                process
              </button>
              <span class="text-sm text-gray-600 dark:text-gray-400">
                - {{ selectedCustomers.size }} customers selected
              </span>
            </div>
          </div>
        </div>
        
        <!-- Customers List -->
        <div class="overflow-y-auto" style="max-height: 600px;">
          <div v-if="loading" class="p-8 text-center">
            <Icon icon="mdi:loading" class="animate-spin text-2xl text-gray-400" />
            <p class="mt-2 text-gray-500">{{ $t('customers.loadingCustomers') }}</p>
          </div>
          
          <div v-else-if="customers.length === 0" class="p-8 text-center">
            <Icon icon="mdi:account-search" class="text-4xl text-gray-300" />
            <p class="mt-2 text-gray-500">{{ $t('customers.noCustomersFound') }}</p>
          </div>
          
          <div v-else>
            <CustomerItem
              v-for="customer in customers"
              :key="customer.psid"
              :customer="customer"
              :is-selected="selectedCustomer?.psid === customer.psid"
              :is-selected-for-processing="isCustomerSelected(customer.psid)"
              @select="selectCustomer(customer)"
              @toggle-select="toggleCustomerSelection(customer.psid)"
            />
          </div>
          
          <!-- Pagination -->
          <div v-if="totalPages > 1" class="p-4 border-t dark:border-gray-700 bg-gray-50 dark:bg-gray-700">
            <div class="flex items-center justify-between">
              <div class="text-sm text-gray-600 dark:text-gray-400">
                Showing {{ customers.length }} of {{ totalElements }} customers
              </div>
              <div class="flex items-center gap-2">
                <!-- First Page -->
                <button
                  @click="firstPage"
                  :disabled="currentPage === 0"
                  class="p-1 rounded hover:bg-gray-200 dark:hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed"
                  title="First page"
                >
                  <Icon icon="mdi:page-first" class="text-gray-600 dark:text-gray-400" />
                </button>
                
                <!-- Previous Page -->
                <button
                  @click="prevPage"
                  :disabled="currentPage === 0"
                  class="p-1 rounded hover:bg-gray-200 dark:hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed"
                  title="Previous page"
                >
                  <Icon icon="mdi:chevron-left" class="text-gray-600 dark:text-gray-400" />
                </button>
                
                <!-- Page Info -->
                <span class="text-sm text-gray-600 dark:text-gray-400 px-2">
                  Page {{ currentPage + 1 }} of {{ totalPages }}
                </span>
                
                <!-- Next Page -->
                <button
                  @click="nextPage"
                  :disabled="currentPage >= totalPages - 1"
                  class="p-1 rounded hover:bg-gray-200 dark:hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed"
                  title="Next page"
                >
                  <Icon icon="mdi:chevron-right" class="text-gray-600 dark:text-gray-400" />
                </button>
                
                <!-- Last Page -->
                <button
                  @click="lastPage"
                  :disabled="currentPage >= totalPages - 1"
                  class="p-1 rounded hover:bg-gray-200 dark:hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed"
                  title="Last page"
                >
                  <Icon icon="mdi:page-last" class="text-gray-600 dark:text-gray-400" />
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Customer Details -->
      <div class="flex-1 bg-white dark:bg-gray-800 rounded-lg border dark:border-gray-700 flex flex-col" style="min-height: 600px;">
        <div v-if="!selectedCustomer" class="h-full flex items-center justify-center">
          <div class="text-center">
            <Icon icon="mdi:account" class="text-6xl text-gray-300" />
            <p class="mt-4 text-gray-500">Select a customer to view details</p>
          </div>
        </div>
        
        <div v-else class="h-full flex flex-col">
          <!-- Customer Header -->
          <div class="p-4 border-b dark:border-gray-700 flex items-center justify-between">
            <div class="flex items-center gap-3">
              <!-- Customer Avatar -->
              <div class="flex-shrink-0">
                <img 
                  v-if="selectedCustomer.displayAvatar"
                  :src="selectedCustomer.displayAvatar" 
                  :alt="selectedCustomer.displayName || selectedCustomer.psid"
                  class="w-12 h-12 rounded-full object-cover"
                  @error="handleImageError"
                />
                <div 
                  v-else
                  class="w-12 h-12 rounded-full bg-gray-300 dark:bg-gray-600 flex items-center justify-center"
                >
                  <Icon icon="mdi:account" class="text-gray-600 dark:text-gray-300 text-2xl" />
                </div>
              </div>
              
              <div>
                <h3 class="font-semibold text-gray-900 dark:text-gray-200">
                  {{ selectedCustomer.displayName || selectedCustomer.psid || 'Unknown Customer' }}
                </h3>
                <p class="text-sm text-gray-500">
                  PSID: {{ selectedCustomer.psid.substring(0, 8) }}... • {{ formatDate(selectedCustomer.updatedAt) }}
                </p>
              </div>
            </div>
            
            <div class="flex items-center gap-2">
              <span 
                :class="getStatusClass(selectedCustomer.status)"
                class="px-3 py-1 rounded-full text-sm"
              >
                {{ $t(`customers.status.${selectedCustomer.status}`) }}
              </span>
              
              <button
                v-if="selectedCustomer.status === 'PENDING'"
                @click="processCustomer"
                :disabled="processing"
                class="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-lg flex items-center gap-2 disabled:opacity-50"
              >
                <Icon v-if="processing" icon="mdi:loading" class="animate-spin" />
                <Icon v-else icon="mdi:check" />
                Process
              </button>
            </div>
          </div>
          
          <!-- Customer Details Area -->
          <div class="flex-1 overflow-y-auto p-4 bg-white dark:bg-gray-800">
            <!-- Stats Cards -->
            <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
              <div class="bg-gray-50 dark:bg-gray-700 p-4 rounded-lg">
                <div class="text-sm text-gray-600 dark:text-gray-400">{{ $t('customers.stats.total') }}</div>
                <div class="text-2xl font-bold text-blue-600">{{ stats.totalCustomers || 0 }}</div>
              </div>
              <div class="bg-gray-50 dark:bg-gray-700 p-4 rounded-lg">
                <div class="text-sm text-gray-600 dark:text-gray-400">{{ $t('customers.stats.pending') }}</div>
                <div class="text-2xl font-bold text-yellow-600">{{ stats.pendingCustomers || 0 }}</div>
              </div>
              <div class="bg-gray-50 dark:bg-gray-700 p-4 rounded-lg">
                <div class="text-sm text-gray-600 dark:text-gray-400">{{ $t('customers.stats.completed') }}</div>
                <div class="text-2xl font-bold text-green-600">{{ stats.completedCustomers || 0 }}</div>
              </div>
              <div class="bg-gray-50 dark:bg-gray-700 p-4 rounded-lg">
                <div class="text-sm text-gray-600 dark:text-gray-400">{{ $t('customers.stats.synced') }}</div>
                <div class="text-2xl font-bold text-purple-600">{{ stats.syncedCustomers || 0 }}</div>
              </div>
            </div>

            <!-- Customer Information -->
            <div class="space-y-6">
              <!-- Contact Information -->
              <div class="bg-gray-50 dark:bg-gray-700 p-4 rounded-lg">
                <h4 class="font-medium text-gray-900 dark:text-gray-200 mb-3">{{ $t('customers.contactInfo') }}</h4>
                <div class="space-y-2">
                  <div v-if="selectedCustomer.primaryPhone" class="flex items-center gap-2">
                    <Icon icon="mdi:phone" class="text-gray-400" />
                    <span class="text-gray-900 dark:text-gray-200">{{ selectedCustomer.primaryPhone }}</span>
                  </div>
                  <div v-if="selectedCustomer.totalPhones > 1" class="text-sm text-gray-500">
                    +{{ selectedCustomer.totalPhones - 1 }} {{ $t('customers.morePhones') }}
                  </div>
                  <div v-if="!selectedCustomer.primaryPhone" class="text-gray-400">
                    {{ $t('customers.noPhone') }}
                  </div>
                </div>
              </div>

              <!-- Status Information -->
              <div class="bg-gray-50 dark:bg-gray-700 p-4 rounded-lg">
                <h4 class="font-medium text-gray-900 dark:text-gray-200 mb-3">{{ $t('customers.statusInfo') }}</h4>
                <div class="space-y-2">
                  <div class="flex items-center justify-between">
                    <span class="text-gray-600 dark:text-gray-400">{{ $t('customers.currentStatus') }}:</span>
                    <span 
                      :class="getStatusClass(selectedCustomer.status)"
                      class="px-2 py-1 rounded-full text-xs"
                    >
                      {{ $t(`customers.status.${selectedCustomer.status}`) }}
                    </span>
                  </div>
                  <div v-if="selectedCustomer.isSyncedWithOdoo" class="flex items-center gap-2 text-green-600">
                    <Icon icon="mdi:check-circle" />
                    <span>{{ $t('customers.syncedWithOdoo') }}</span>
                  </div>
                </div>
              </div>

              <!-- Timestamp Information -->
              <div class="bg-gray-50 dark:bg-gray-700 p-4 rounded-lg">
                <h4 class="font-medium text-gray-900 dark:text-gray-200 mb-3">{{ $t('customers.timestamps') }}</h4>
                <div class="space-y-2">
                  <div class="flex items-center justify-between">
                    <span class="text-gray-600 dark:text-gray-400">{{ $t('customers.lastUpdated') }}:</span>
                    <span class="text-gray-900 dark:text-gray-200">{{ formatDate(selectedCustomer.updatedAt) }}</span>
                  </div>
                </div>
              </div>
            </div>
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
import { ref, onMounted, computed } from 'vue'
import { Icon } from '@iconify/vue'
import CustomerDetailsModal from './components/CustomerDetailsModal.vue'
import CustomerItem from './components/CustomerItem.vue'
import { useI18n } from 'vue-i18n'
import customerApi from '@/api/customerApi'

export default {
  name: 'CustomerData',
  components: {
    Icon,
    CustomerDetailsModal,
    CustomerItem
  },
  setup() {
    const { t } = useI18n()
    
    // Reactive data
    const loading = ref(false)
    const customers = ref([])
    const stats = ref({})
    const availableStatuses = ref([])
    const selectedStatus = ref('')
    const currentPage = ref(0)
    const pageSize = ref(20)
    const totalElements = ref(0)
    const totalPages = ref(0)
    const showDetailsModal = ref(false)
    const selectedCustomer = ref(null)
    const selectedCustomers = ref(new Set())
    const processing = ref(false)
    const showSearchModal = ref(false)

    // Computed properties
    const allSelected = computed(() => {
      return customers.value.length > 0 && selectedCustomers.value.size === customers.value.length
    })

    // Methods
    const fetchCustomers = async () => {
      loading.value = true
      try {
        let response
        
        if (selectedStatus.value) {
          // Use status filter
          response = await customerApi.getCustomersByStatus(selectedStatus.value, currentPage.value, pageSize.value)
        } else {
          // Get all
          response = await customerApi.getCustomers({ page: currentPage.value, size: pageSize.value })
        }

        customers.value = response.content || []
        totalElements.value = response.totalElements || 0
        totalPages.value = response.totalPages || 0
      } catch (error) {
        console.error('Error fetching customers:', error)
      } finally {
        loading.value = false
      }
    }

    const fetchStats = async () => {
      try {
        stats.value = await customerApi.getCustomerStats()
      } catch (error) {
        console.error('Error fetching stats:', error)
      }
    }

    const fetchStatuses = async () => {
      try {
        availableStatuses.value = await customerApi.getAvailableStatuses()
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

    const onStatusChange = () => {
      currentPage.value = 0
      fetchCustomers()
    }

    const selectCustomer = (customer) => {
      selectedCustomer.value = customer
    }

    const toggleCustomerSelection = (psid) => {
      if (selectedCustomers.value.has(psid)) {
        selectedCustomers.value.delete(psid)
      } else {
        selectedCustomers.value.add(psid)
      }
    }

    const isCustomerSelected = (psid) => {
      return selectedCustomers.value.has(psid)
    }

    const toggleSelectAll = () => {
      if (allSelected.value) {
        selectedCustomers.value.clear()
      } else {
        customers.value.forEach(customer => {
          selectedCustomers.value.add(customer.psid)
        })
      }
    }

    const processSelectedCustomers = async () => {
      // TODO: Implement bulk processing when backend endpoint is available
      console.log('Processing selected customers:', Array.from(selectedCustomers.value))
      // For now, just show a message
      alert('Customer processing feature will be available soon')
    }

    const processCustomer = async () => {
      // TODO: Implement customer processing when backend endpoint is available
      console.log('Processing customer:', selectedCustomer.value)
      // For now, just show a message
      alert('Customer processing feature will be available soon')
    }

    // Pagination methods
    const firstPage = () => {
      currentPage.value = 0
      fetchCustomers()
    }

    const prevPage = () => {
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

    const lastPage = () => {
      currentPage.value = totalPages.value - 1
      fetchCustomers()
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

    const handleImageError = (event) => {
      // Fallback to default avatar if image fails to load
      event.target.style.display = 'none'
      const parent = event.target.parentElement
      if (parent) {
        const fallback = parent.querySelector('.bg-gray-300, .dark\\:bg-gray-600')
        if (fallback) {
          fallback.style.display = 'flex'
        }
      }
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
      selectedStatus,
      currentPage,
      pageSize,
      totalElements,
      totalPages,
      showDetailsModal,
      selectedCustomer,
      selectedCustomers,
      processing,
      showSearchModal,
      
      // Computed
      allSelected,
      
      // Methods
      refreshData,
      onStatusChange,
      selectCustomer,
      toggleCustomerSelection,
      isCustomerSelected,
      toggleSelectAll,
      processSelectedCustomers,
      processCustomer,
      firstPage,
      prevPage,
      nextPage,
      lastPage,
      getStatusClass,
      formatDate,
      handleImageError,
      
      // i18n
      t
    }
  }
}
</script>

<style scoped>
.customer-data-page {
  max-width: 100%;
  margin: 0 auto;
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
