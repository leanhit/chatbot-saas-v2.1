<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <div class="flex items-center">
        <Icon icon="mdi:history" class="text-2xl text-blue-600 dark:text-blue-400 mr-3" />
        <h1 class="text-2xl font-bold text-gray-800 dark:text-white">
          {{ $t('payment.history.title') }}
        </h1>
      </div>
      <button
        @click="paymentStore.loadPaymentHistory()"
        :disabled="paymentStore.loading"
        class="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600 disabled:opacity-50"
      >
        <span v-if="paymentStore.loading" class="flex items-center">
          <Icon icon="eos-icons:loading" class="animate-spin mr-2" />
          {{ $t('payment.history.loading') }}
        </span>
        <span v-else>{{ $t('payment.history.refresh') }}</span>
      </button>
    </div>

    <!-- Alert Messages -->
    <div v-if="paymentStore.message" class="mt-4 p-4 rounded-lg" :class="paymentStore.getMessageClass()">
      {{ paymentStore.message }}
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4 mb-6">
      <div class="bg-white dark:bg-gray-900 rounded-lg shadow p-4">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">{{ $t('payment.history.totalAmount') }}</p>
            <p class="text-xl font-bold text-green-600 dark:text-green-400">
              {{ formatCurrency(paymentStore.paymentStats.totalAmount) }}
            </p>
          </div>
          <Icon icon="mdi:cash-multiple" class="text-2xl text-green-500 dark:text-green-400" />
        </div>
      </div>
      
      <div class="bg-white dark:bg-gray-900 rounded-lg shadow p-4">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">{{ $t('payment.history.completed') }}</p>
            <p class="text-xl font-bold text-green-600 dark:text-green-400">
              {{ paymentStore.paymentStats.completed }}
            </p>
          </div>
          <Icon icon="mdi:check-circle" class="text-2xl text-green-500 dark:text-green-400" />
        </div>
      </div>
      
      <div class="bg-white dark:bg-gray-900 rounded-lg shadow p-4">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">{{ $t('payment.history.pending') }}</p>
            <p class="text-xl font-bold text-yellow-600 dark:text-yellow-400">
              {{ paymentStore.paymentStats.pending }}
            </p>
          </div>
          <Icon icon="mdi:clock" class="text-2xl text-yellow-500 dark:text-yellow-400" />
        </div>
      </div>
      
      <div class="bg-white dark:bg-gray-900 rounded-lg shadow p-4">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">{{ $t('payment.history.failed') }}</p>
            <p class="text-xl font-bold text-red-600 dark:text-red-400">
              {{ paymentStore.paymentStats.failed }}
            </p>
          </div>
          <Icon icon="mdi:close-circle" class="text-2xl text-red-500 dark:text-red-400" />
        </div>
      </div>
      
      <div class="bg-white dark:bg-gray-900 rounded-lg shadow p-4">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">{{ $t('payment.history.expired') }}</p>
            <p class="text-xl font-bold text-gray-600 dark:text-gray-400">
              {{ paymentStore.paymentStats.expired }}
            </p>
          </div>
          <Icon icon="mdi:timer-off" class="text-2xl text-gray-500 dark:text-gray-400" />
        </div>
      </div>
    </div>

    <!-- Filter -->
    <div class="bg-white dark:bg-gray-900 rounded-lg shadow p-4 mb-6">
      <div class="flex items-center justify-between">
        <div class="flex items-center space-x-4">
          <label class="text-sm font-medium text-gray-700 dark:text-gray-300">{{ $t('payment.history.filterByStatus') }}</label>
          <select 
            v-model="paymentStore.filterStatus" 
            @change="paymentStore.setFilterStatus(paymentStore.filterStatus)"
            class="border border-gray-300 dark:border-gray-600 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-800 dark:text-white"
          >
            <option value="">{{ $t('payment.history.all') }}</option>
            <option value="COMPLETED">{{ $t('payment.history.completed') }}</option>
            <option value="PENDING">{{ $t('payment.history.pending') }}</option>
            <option value="FAILED">{{ $t('payment.history.failed') }}</option>
            <option value="EXPIRED">{{ $t('payment.history.expired') }}</option>
          </select>
        </div>
        <div class="text-sm text-gray-600 dark:text-gray-400">
          {{ $t('payment.history.displayCount', { count: paymentStore.filteredPayments.length, total: paymentStore.paymentStats.total }) }}
        </div>
      </div>
    </div>

    <!-- Payment History Table -->
    <div class="bg-white dark:bg-gray-900 rounded-lg shadow overflow-hidden">
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
          <thead class="bg-gray-50 dark:bg-gray-800">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                {{ $t('payment.history.table.referenceCode') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                {{ $t('payment.history.table.amount') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                {{ $t('payment.history.table.status') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                {{ $t('payment.history.table.description') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                {{ $t('payment.history.table.createdAt') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                {{ $t('payment.history.table.completedAt') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                {{ $t('payment.history.table.actions') }}
              </th>
            </tr>
          </thead>
          <tbody class="bg-white dark:bg-gray-900 divide-y divide-gray-200 dark:divide-gray-700">
            <tr v-if="paymentStore.loading">
              <td colspan="7" class="px-6 py-4 text-center">
                <Icon icon="eos-icons:loading" class="animate-spin text-2xl text-blue-500" />
                <span class="ml-2 text-gray-600 dark:text-gray-400">{{ $t('payment.history.loading') }}</span>
              </td>
            </tr>
            
            <tr v-else-if="paymentStore.filteredPayments.length === 0">
              <td colspan="7" class="px-6 py-4 text-center text-gray-500 dark:text-gray-400">
                {{ $t('payment.history.noTransactions') }}
              </td>
            </tr>
            
            <tr v-for="payment in paymentStore.filteredPayments" :key="payment.referenceCode">
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="font-mono text-sm text-gray-900 dark:text-white">
                  {{ payment.referenceCode }}
                </div>
              </td>
              
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm font-semibold text-gray-900 dark:text-white">
                  {{ formatCurrency(payment.amount) }}
                </div>
              </td>
              
              <td class="px-6 py-4 whitespace-nowrap">
                <span 
                  class="px-3 py-1 rounded-full text-xs font-semibold"
                  :class="paymentStore.getStatusClass(payment.status)"
                >
                  {{ paymentStore.getStatusText(payment.status) }}
                </span>
              </td>
              
              <td class="px-6 py-4">
                <span class="text-sm text-gray-600 dark:text-gray-400">
                  {{ payment.description || $t('payment.deposit.title') }}
                </span>
              </td>
              
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-600 dark:text-gray-400">
                {{ formatPaymentDateTime(payment.createdAtFormatted) }}
              </td>
              
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-600 dark:text-gray-400">
                {{ payment.completedAt ? formatPaymentDateTime(payment.completedAtFormatted) : '-' }}
              </td>
              
              <td class="px-6 py-4 whitespace-nowrap text-sm">
                <div class="flex space-x-2">
                  <button
                    @click="checkPaymentStatus(payment.referenceCode)"
                    class="text-blue-600 hover:text-blue-800 dark:text-blue-400 dark:hover:text-blue-300"
                    :title="$t('payment.history.actions.checkStatus')"
                  >
                    <Icon icon="mdi:refresh" />
                  </button>
                  <button
                    v-if="payment.status === 'PENDING'"
                    @click="simulatePayment(payment.referenceCode, payment.amount)"
                    class="text-green-600 hover:text-green-800 dark:text-green-400 dark:hover:text-green-300"
                    :title="$t('payment.history.actions.simulate')"
                  >
                    <Icon icon="mdi:play-circle" />
                  </button>
                  <button
                    v-if="payment.status === 'COMPLETED' && isAdmin"
                    @click="refundPayment(payment.referenceCode)"
                    class="text-red-600 hover:text-red-800 dark:text-red-400 dark:hover:text-red-300"
                    title="Hoàn tiền (Admin)"
                  >
                    <Icon icon="mdi:cash-refund" />
                  </button>
                  <button
                    @click="paymentStore.copyReferenceCode(payment.referenceCode)"
                    class="text-gray-600 hover:text-gray-800 dark:text-gray-400 dark:hover:text-gray-300"
                    :title="$t('payment.history.actions.copyCode')"
                  >
                    <Icon icon="mdi:content-copy" />
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

<script>
import { Icon } from '@iconify/vue'
import { formatDateTime } from '@/utils/dateUtils'
import { usePaymentStore } from '@/stores/paymentStore'
import axios from '@/plugins/axios'
import paymentAPI from '@/api/paymentApi'
import { useI18n } from 'vue-i18n'
import { ref } from 'vue'

export default {
  name: 'PaymentHistory',
  components: {
    Icon
  },
  setup() {
    const paymentStore = usePaymentStore()
    const { t } = useI18n()

    // Check if user is admin
    const isAdmin = ref(false)
    const checkAdminRole = () => {
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      isAdmin.value = user.systemRole === 'SYSTEM_ADMIN'
    }

    // Load payment history on mount
    paymentStore.loadPaymentHistory()
    checkAdminRole()

    // Methods
    const formatCurrency = (amount) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(amount)
    }

    const formatPaymentDateTime = (dateString) => {
      return formatDateTime(dateString, {
        fallback: '-',
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    }

    const checkPaymentStatus = async (referenceCode) => {
      try {
        const response = await axios.get(`/api/public/simple-payment/status/${referenceCode}`)
        
        if (response.data) {
          paymentStore.updatePaymentInHistory(response.data)
          paymentStore.setMessage(t('payment.history.messages.statusUpdated'), 'success')
        } else {
          throw new Error('Failed to check status')
        }
      } catch (error) {
        paymentStore.setMessage(t('payment.history.messages.checkFailed'), 'error')
        console.error('Error checking status:', error)
      }
    }

    const simulatePayment = async (referenceCode, amount) => {
      try {
        const response = await axios.post('/api/public/simple-payment/test/simulate-payment', {
          referenceCode,
          amount
        })

        if (response.data || response.status === 200) {
          paymentStore.setMessage(t('payment.history.messages.simulateSuccess'), 'success')
          
          // Auto check status after 15 seconds
          setTimeout(() => {
            checkPaymentStatus(referenceCode)
          }, 15000)
        } else {
          throw new Error('Simulation failed')
        }
      } catch (error) {
        paymentStore.setMessage(t('payment.history.messages.simulateFailed'), 'error')
        console.error('Error simulating payment:', error)
      }
    }

    const refundPayment = async (referenceCode) => {
      const reason = prompt('Lý do hoàn tiền:')
      if (!reason) return

      try {
        await paymentAPI.refundPayment(referenceCode, reason)
        paymentStore.setMessage('Hoàn tiền thành công', 'success')
        paymentStore.loadPaymentHistory()
      } catch (error) {
        paymentStore.setMessage('Lỗi hoàn tiền: ' + (error.response?.data?.message || error.message), 'error')
        console.error('Error refunding payment:', error)
      }
    }

    return {
      paymentStore,
      isAdmin,
      formatCurrency,
      formatPaymentDateTime,
      checkPaymentStatus,
      simulatePayment,
      refundPayment
    }
  }
}
</script>
