<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-900 dark:text-white">{{ $t('routing.transfer.title') }}</h1>
      <button
        @click="openCreateModal"
        class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
      >
        <Icon icon="mdi:plus" class="inline-block mr-1" />
        {{ $t('routing.transfer.createTransfer') }}
      </button>
    </div>

    <!-- Filters -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-4 mb-6">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('routing.transfer.filterByStatus') }}
          </label>
          <select
            v-model="filters.status"
            @change="loadTransfers"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
          >
            <option value="">{{ $t('routing.transfer.allStatuses') }}</option>
            <option value="PENDING">{{ $t('routing.transfer.status.pending') }}</option>
            <option value="ACCEPTED">{{ $t('routing.transfer.status.accepted') }}</option>
            <option value="REJECTED">{{ $t('routing.transfer.status.rejected') }}</option>
            <option value="COMPLETED">{{ $t('routing.transfer.status.completed') }}</option>
            <option value="CANCELLED">{{ $t('routing.transfer.status.cancelled') }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('routing.transfer.filterByType') }}
          </label>
          <select
            v-model="filters.transferType"
            @change="loadTransfers"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
          >
            <option value="">{{ $t('routing.transfer.allTypes') }}</option>
            <option value="MANUAL">{{ $t('routing.transfer.type.manual') }}</option>
            <option value="AUTO_ESCALATION">{{ $t('routing.transfer.type.autoEscalation') }}</option>
            <option value="WORKLOAD_BALANCE">{{ $t('routing.transfer.type.workloadBalance') }}</option>
            <option value="SKILL_MISMATCH">{{ $t('routing.transfer.type.skillMismatch') }}</option>
            <option value="SUPERVISOR_REQUEST">{{ $t('routing.transfer.type.supervisorRequest') }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            {{ $t('routing.transfer.conversationId') }}
          </label>
          <input
            v-model="filters.conversationId"
            type="number"
            @input="loadTransfers"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
            placeholder="{{ $t('routing.transfer.allConversations') }}"
          />
        </div>
        <div class="flex items-end">
          <button
            @click="resetFilters"
            class="w-full px-4 py-2 bg-gray-200 dark:bg-gray-600 text-gray-900 dark:text-white rounded-md hover:bg-gray-300 dark:hover:bg-gray-500 transition-colors"
          >
            {{ $t('routing.transfer.resetFilters') }}
          </button>
        </div>
      </div>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-6">
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="p-3 bg-blue-100 dark:bg-blue-900 rounded-full">
            <Icon icon="mdi:swap-horizontal" class="text-2xl text-blue-600 dark:text-blue-400" />
          </div>
          <div class="ml-4">
            <p class="text-sm text-gray-500 dark:text-gray-400">{{ $t('routing.transfer.totalTransfers') }}</p>
            <p class="text-2xl font-bold text-gray-900 dark:text-white">{{ transfers.length }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="p-3 bg-yellow-100 dark:bg-yellow-900 rounded-full">
            <Icon icon="mdi:clock-outline" class="text-2xl text-yellow-600 dark:text-yellow-400" />
          </div>
          <div class="ml-4">
            <p class="text-sm text-gray-500 dark:text-gray-400">{{ $t('routing.transfer.pending') }}</p>
            <p class="text-2xl font-bold text-gray-900 dark:text-white">{{ pendingCount }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="p-3 bg-green-100 dark:bg-green-900 rounded-full">
            <Icon icon="mdi:check-circle" class="text-2xl text-green-600 dark:text-green-400" />
          </div>
          <div class="ml-4">
            <p class="text-sm text-gray-500 dark:text-gray-400">{{ $t('routing.transfer.completed') }}</p>
            <p class="text-2xl font-bold text-gray-900 dark:text-white">{{ completedCount }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="p-3 bg-red-100 dark:bg-red-900 rounded-full">
            <Icon icon="mdi:close-circle" class="text-2xl text-red-600 dark:text-red-400" />
          </div>
          <div class="ml-4">
            <p class="text-sm text-gray-500 dark:text-gray-400">{{ $t('routing.transfer.rejected') }}</p>
            <p class="text-2xl font-bold text-gray-900 dark:text-white">{{ rejectedCount }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Transfers Table -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
        <thead class="bg-gray-50 dark:bg-gray-700">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ $t('routing.transfer.conversation') }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ $t('routing.transfer.fromAgent') }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ $t('routing.transfer.toAgent') }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ $t('routing.transfer.type') }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ $t('routing.transfer.status') }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ $t('routing.transfer.createdAt') }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
              {{ $t('routing.transfer.actions') }}
            </th>
          </tr>
        </thead>
        <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
          <tr v-if="loading">
            <td colspan="7" class="px-6 py-4 text-center text-gray-500 dark:text-gray-400">
              {{ $t('routing.transfer.loading') }}
            </td>
          </tr>
          <tr v-else-if="transfers.length === 0">
            <td colspan="7" class="px-6 py-4 text-center text-gray-500 dark:text-gray-400">
              {{ $t('routing.transfer.noTransfers') }}
            </td>
          </tr>
          <tr v-else v-for="transfer in transfers" :key="transfer.id">
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
              #{{ transfer.conversationId }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
              {{ transfer.fromAgentId }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
              {{ transfer.toAgentId }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
              {{ $t(`routing.transfer.type.${transfer.transferType.toLowerCase()}`) }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span
                :class="getStatusClass(transfer.status)"
                class="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full"
              >
                {{ $t(`routing.transfer.status.${transfer.status.toLowerCase()}`) }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
              {{ formatDate(transfer.createdAt) }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
              <div class="flex space-x-2">
                <button
                  v-if="transfer.status === 'PENDING'"
                  @click="acceptTransfer(transfer)"
                  class="text-green-600 hover:text-green-900 dark:text-green-400 dark:hover:text-green-300"
                  :title="$t('routing.transfer.accept')"
                >
                  <Icon icon="mdi:check" class="inline-block" />
                </button>
                <button
                  v-if="transfer.status === 'PENDING'"
                  @click="rejectTransfer(transfer)"
                  class="text-red-600 hover:text-red-900 dark:text-red-400 dark:hover:text-red-300"
                  :title="$t('routing.transfer.reject')"
                >
                  <Icon icon="mdi:close" class="inline-block" />
                </button>
                <button
                  v-if="transfer.status === 'ACCEPTED'"
                  @click="completeTransfer(transfer)"
                  class="text-blue-600 hover:text-blue-900 dark:text-blue-400 dark:hover:text-blue-300"
                  :title="$t('routing.transfer.complete')"
                >
                  <Icon icon="mdi:check-all" class="inline-block" />
                </button>
                <button
                  v-if="transfer.status !== 'COMPLETED' && transfer.status !== 'CANCELLED'"
                  @click="cancelTransfer(transfer)"
                  class="text-gray-600 hover:text-gray-900 dark:text-gray-400 dark:hover:text-gray-300"
                  :title="$t('routing.transfer.cancel')"
                >
                  <Icon icon="mdi:cancel" class="inline-block" />
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Create Transfer Modal -->
    <div v-if="showModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow-xl w-full max-w-2xl mx-4">
        <div class="flex justify-between items-center p-6 border-b border-gray-200 dark:border-gray-700">
          <h2 class="text-xl font-bold text-gray-900 dark:text-white">
            {{ $t('routing.transfer.createTransfer') }}
          </h2>
          <button
            @click="closeModal"
            class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
          >
            <Icon icon="mdi:close" class="text-2xl" />
          </button>
        </div>
        <form @submit.prevent="saveTransfer" class="p-6">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                {{ $t('routing.transfer.conversationId') }} *
              </label>
              <input
                v-model.number="formData.conversationId"
                type="number"
                required
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                {{ $t('routing.transfer.fromAgentId') }} *
              </label>
              <input
                v-model.number="formData.fromAgentId"
                type="number"
                required
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                {{ $t('routing.transfer.toAgentId') }} *
              </label>
              <input
                v-model.number="formData.toAgentId"
                type="number"
                required
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                {{ $t('routing.transfer.transferType') }} *
              </label>
              <select
                v-model="formData.transferType"
                required
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              >
                <option value="MANUAL">{{ $t('routing.transfer.type.manual') }}</option>
                <option value="AUTO_ESCALATION">{{ $t('routing.transfer.type.autoEscalation') }}</option>
                <option value="WORKLOAD_BALANCE">{{ $t('routing.transfer.type.workloadBalance') }}</option>
                <option value="SKILL_MISMATCH">{{ $t('routing.transfer.type.skillMismatch') }}</option>
                <option value="SUPERVISOR_REQUEST">{{ $t('routing.transfer.type.supervisorRequest') }}</option>
              </select>
            </div>
            <div class="col-span-2">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                {{ $t('routing.transfer.reason') }}
              </label>
              <textarea
                v-model="formData.transferReason"
                rows="3"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              />
            </div>
            <div class="col-span-2">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                {{ $t('routing.transfer.notes') }}
              </label>
              <textarea
                v-model="formData.notes"
                rows="2"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              />
            </div>
          </div>
          <div class="mt-6 flex justify-end space-x-3">
            <button
              type="button"
              @click="closeModal"
              class="px-4 py-2 bg-gray-200 dark:bg-gray-600 text-gray-900 dark:text-white rounded-md hover:bg-gray-300 dark:hover:bg-gray-500 transition-colors"
            >
              {{ $t('routing.transfer.cancel') }}
            </button>
            <button
              type="submit"
              class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
            >
              {{ $t('routing.transfer.create') }}
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
import { useI18n } from 'vue-i18n'
import routingApi from '@/api/routingApi'

const { t } = useI18n()

const transfers = ref([])
const loading = ref(false)
const showModal = ref(false)
const filters = ref({
  status: '',
  transferType: '',
  conversationId: ''
})

const formData = ref({
  conversationId: null,
  fromAgentId: null,
  toAgentId: null,
  transferType: 'MANUAL',
  transferReason: '',
  notes: ''
})

const pendingCount = computed(() => transfers.value.filter(t => t.status === 'PENDING').length)
const completedCount = computed(() => transfers.value.filter(t => t.status === 'COMPLETED').length)
const rejectedCount = computed(() => transfers.value.filter(t => t.status === 'REJECTED').length)

const loadTransfers = async () => {
  try {
    loading.value = true
    const params = {}
    if (filters.value.status) params.status = filters.value.status
    if (filters.value.transferType) params.transferType = filters.value.transferType
    if (filters.value.conversationId) params.conversationId = filters.value.conversationId
    transfers.value = await routingApi.getTransfers(params)
  } catch (err) {
    console.error('Load transfers error:', err)
  } finally {
    loading.value = false
  }
}

const openCreateModal = () => {
  formData.value = {
    conversationId: null,
    fromAgentId: null,
    toAgentId: null,
    transferType: 'MANUAL',
    transferReason: '',
    notes: ''
  }
  showModal.value = true
}

const saveTransfer = async () => {
  try {
    await routingApi.createTransfer(formData.value)
    closeModal()
    await loadTransfers()
  } catch (err) {
    console.error('Save transfer error:', err)
  }
}

const acceptTransfer = async (transfer) => {
  try {
    await routingApi.acceptTransfer(transfer.id)
    await loadTransfers()
  } catch (err) {
    console.error('Accept transfer error:', err)
  }
}

const rejectTransfer = async (transfer) => {
  const reason = prompt(t('routing.transfer.rejectReason'))
  if (reason === null) return
  try {
    await routingApi.rejectTransfer(transfer.id, reason)
    await loadTransfers()
  } catch (err) {
    console.error('Reject transfer error:', err)
  }
}

const completeTransfer = async (transfer) => {
  try {
    await routingApi.completeTransfer(transfer.id)
    await loadTransfers()
  } catch (err) {
    console.error('Complete transfer error:', err)
  }
}

const cancelTransfer = async (transfer) => {
  if (!confirm(t('routing.transfer.confirmCancel'))) return
  try {
    await routingApi.cancelTransfer(transfer.id)
    await loadTransfers()
  } catch (err) {
    console.error('Cancel transfer error:', err)
  }
}

const closeModal = () => {
  showModal.value = false
}

const resetFilters = () => {
  filters.value = {
    status: '',
    transferType: '',
    conversationId: ''
  }
  loadTransfers()
}

const getStatusClass = (status) => {
  switch (status) {
    case 'PENDING': return 'bg-yellow-100 text-yellow-800'
    case 'ACCEPTED': return 'bg-blue-100 text-blue-800'
    case 'REJECTED': return 'bg-red-100 text-red-800'
    case 'COMPLETED': return 'bg-green-100 text-green-800'
    case 'CANCELLED': return 'bg-gray-100 text-gray-800'
    default: return 'bg-gray-100 text-gray-800'
  }
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString()
}

onMounted(() => {
  loadTransfers()
})
</script>
