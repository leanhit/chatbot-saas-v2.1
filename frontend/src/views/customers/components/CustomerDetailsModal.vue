<template>
  <div class="modal-overlay" @click="$emit('close')">
    <div class="modal-content" @click.stop>
      <!-- Modal Header -->
      <div class="modal-header">
        <h2 class="text-xl font-bold text-gray-900 dark:text-white">
          {{ $t('customers.details.title') }}
        </h2>
        <button
          @click="$emit('close')"
          class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
        >
          <Icon icon="mdi:close" class="text-xl" />
        </button>
      </div>

      <!-- Modal Body -->
      <div class="modal-body">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- Customer Info Section -->
          <div class="space-y-4">
            <h3 class="font-semibold text-gray-900 dark:text-white border-b pb-2">
              {{ $t('customers.details.customerInfo') }}
            </h3>
            
            <!-- Profile -->
            <div class="flex items-center space-x-4">
              <div class="flex-shrink-0">
                <img
                  v-if="customer.displayAvatar"
                  :src="customer.displayAvatar"
                  :alt="customer.displayName"
                  class="h-16 w-16 rounded-full object-cover"
                />
                <div
                  v-else
                  class="h-16 w-16 rounded-full bg-gray-300 dark:bg-gray-600 flex items-center justify-center"
                >
                  <Icon icon="mdi:account" class="text-2xl text-gray-500" />
                </div>
              </div>
              <div>
                <div class="font-medium text-gray-900 dark:text-white">
                  {{ customer.displayName }}
                </div>
                <div class="text-sm text-gray-500 dark:text-gray-400">
                  PSID: {{ customer.psid }}
                </div>
              </div>
            </div>

            <!-- Contact Info -->
            <div class="space-y-2">
              <div v-if="customer.primaryPhone" class="flex items-center space-x-2">
                <Icon icon="mdi:phone" class="text-gray-400" />
                <span class="text-gray-900 dark:text-white">{{ customer.primaryPhone }}</span>
              </div>
              <div class="flex items-center space-x-2">
                <Icon icon="mdi:identifier" class="text-gray-400" />
                <span class="text-gray-900 dark:text-white">Page ID: {{ customer.pageId }}</span>
              </div>
              <div class="flex items-center space-x-2">
                <Icon icon="mdi:account-circle" class="text-gray-400" />
                <span class="text-gray-900 dark:text-white">Owner: {{ customer.ownerId }}</span>
              </div>
            </div>
          </div>

          <!-- Status & Sync Section -->
          <div class="space-y-4">
            <h3 class="font-semibold text-gray-900 dark:text-white border-b pb-2">
              {{ $t('customers.details.statusInfo') }}
            </h3>
            
            <!-- Status -->
            <div class="space-y-2">
              <div class="flex items-center justify-between">
                <span class="text-gray-600 dark:text-gray-400">{{ $t('customers.details.status') }}</span>
                <span
                  :class="getStatusClass(customer.status)"
                  class="px-2 py-1 text-xs font-semibold rounded-full"
                >
                  {{ $t(`customers.status.${customer.status}`) }}
                </span>
              </div>
              
              <div v-if="customer.isSyncedWithOdoo" class="flex items-center justify-between">
                <span class="text-gray-600 dark:text-gray-400">{{ $t('customers.details.odooSync') }}</span>
                <span class="text-green-600 flex items-center gap-1">
                  <Icon icon="mdi:check-circle" />
                  {{ $t('customers.details.synced') }}
                </span>
              </div>
              
              <div v-if="customer.odooId" class="flex items-center justify-between">
                <span class="text-gray-600 dark:text-gray-400">Odoo ID</span>
                <span class="text-gray-900 dark:text-white">{{ customer.odooId }}</span>
              </div>
              
              <div v-if="customer.odooPartnerId" class="flex items-center justify-between">
                <span class="text-gray-600 dark:text-gray-400">Odoo Partner ID</span>
                <span class="text-gray-900 dark:text-white">{{ customer.odooPartnerId }}</span>
              </div>
            </div>

            <!-- Timestamps -->
            <div class="space-y-2 text-sm">
              <div class="flex justify-between">
                <span class="text-gray-600 dark:text-gray-400">{{ $t('customers.details.createdAt') }}</span>
                <span class="text-gray-900 dark:text-white">{{ formatDate(customer.createdAt) }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600 dark:text-gray-400">{{ $t('customers.details.lastUpdated') }}</span>
                <span class="text-gray-900 dark:text-white">{{ formatDate(customer.updatedAt) }}</span>
              </div>
              <div v-if="customer.lastInteraction" class="flex justify-between">
                <span class="text-gray-600 dark:text-gray-400">{{ $t('customers.details.lastInteraction') }}</span>
                <span class="text-gray-900 dark:text-white">{{ formatDate(customer.lastInteraction) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Phone Numbers Section -->
        <div v-if="customer.phones && customer.phones.length > 0" class="mt-6">
          <h3 class="font-semibold text-gray-900 dark:text-white border-b pb-2 mb-4">
            {{ $t('customers.details.phoneNumbers') }} ({{ customer.totalPhones }})
          </h3>
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
            <div
              v-for="(phone, index) in Array.from(customer.phones)"
              :key="index"
              class="flex items-center space-x-2 p-3 bg-gray-50 dark:bg-gray-700 rounded-lg"
            >
              <Icon icon="mdi:phone" class="text-gray-400" />
              <span class="text-gray-900 dark:text-white">{{ phone }}</span>
              <span v-if="index === 0" class="text-xs bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200 px-2 py-1 rounded">
                {{ $t('customers.details.primary') }}
              </span>
            </div>
          </div>
        </div>

        <!-- Captured Phones Section -->
        <div v-if="customer.capturedPhones && customer.capturedPhones.length > 0" class="mt-6">
          <h3 class="font-semibold text-gray-900 dark:text-white border-b pb-2 mb-4">
            {{ $t('customers.details.capturedPhones') }} ({{ customer.capturedPhones.length }})
          </h3>
          <div class="overflow-x-auto">
            <table class="w-full text-sm">
              <thead class="bg-gray-50 dark:bg-gray-700">
                <tr>
                  <th class="px-4 py-2 text-left text-gray-700 dark:text-gray-300">
                    {{ $t('customers.details.phoneNumber') }}
                  </th>
                  <th class="px-4 py-2 text-left text-gray-700 dark:text-gray-300">
                    {{ $t('customers.details.capturedAt') }}
                  </th>
                  <th class="px-4 py-2 text-left text-gray-700 dark:text-gray-300">
                    {{ $t('customers.details.owner') }}
                  </th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-200 dark:divide-gray-700">
                <tr v-for="capturedPhone in customer.capturedPhones" :key="capturedPhone.phoneNumber">
                  <td class="px-4 py-2">{{ capturedPhone.phoneNumber }}</td>
                  <td class="px-4 py-2">{{ formatDate(capturedPhone.capturedAt) }}</td>
                  <td class="px-4 py-2">{{ capturedPhone.ownerId }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Data JSON Section -->
        <div v-if="customer.dataJson" class="mt-6">
          <h3 class="font-semibold text-gray-900 dark:text-white border-b pb-2 mb-4">
            {{ $t('customers.details.extractedData') }}
          </h3>
          <div class="bg-gray-50 dark:bg-gray-700 p-4 rounded-lg">
            <pre class="text-xs text-gray-700 dark:text-gray-300 whitespace-pre-wrap">{{ formatJson(customer.dataJson) }}</pre>
          </div>
        </div>
      </div>

      <!-- Modal Footer -->
      <div class="modal-footer">
        <button
          @click="$emit('close')"
          class="px-4 py-2 bg-gray-200 text-gray-800 rounded-lg hover:bg-gray-300 dark:bg-gray-700 dark:text-gray-200 dark:hover:bg-gray-600"
        >
          {{ $t('common.close') }}
        </button>
        <button
          v-if="customer.status === 'PENDING'"
          @click="processCustomer"
          class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 ml-3"
        >
          {{ $t('customers.process') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { Icon } from '@iconify/vue'
import { useI18n } from 'vue-i18n'

export default {
  name: 'CustomerDetailsModal',
  components: {
    Icon
  },
  props: {
    customer: {
      type: Object,
      required: true
    }
  },
  emits: ['close'],
  setup(props, { emit }) {
    const { t } = useI18n()

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

    const formatJson = (jsonString) => {
      try {
        const parsed = JSON.parse(jsonString)
        return JSON.stringify(parsed, null, 2)
      } catch (e) {
        return jsonString
      }
    }

    const processCustomer = () => {
      // TODO: Implement customer processing logic
      console.log('Processing customer:', props.customer)
      emit('close')
    }

    return {
      getStatusClass,
      formatDate,
      formatJson,
      processCustomer,
      t
    }
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}

.modal-content {
  background: white;
  dark:bg-gray-800;
  border-radius: 0.5rem;
  max-width: 4xl;
  max-height: 90vh;
  width: 100%;
  overflow-y: auto;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
  dark:border-gray-700;
}

.modal-body {
  padding: 1.5rem;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  padding: 1.5rem;
  border-top: 1px solid #e5e7eb;
  dark:border-gray-700;
  background-color: #f9fafb;
  dark:bg-gray-800;
}
</style>
