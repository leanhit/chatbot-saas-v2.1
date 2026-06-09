<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <div class="flex items-center">
        <Icon icon="mdi:file-document" class="text-2xl text-blue-600 dark:text-blue-400 mr-3" />
        <h1 class="text-2xl font-bold text-gray-800 dark:text-white">Hóa đơn thanh toán</h1>
      </div>
    </div>

    <div v-if="loading" class="flex justify-center py-8">
      <Icon icon="eos-icons:loading" class="animate-spin text-4xl text-blue-600" />
    </div>

    <div v-else-if="invoices.length === 0" class="text-center py-8">
      <Icon icon="mdi:file-document-outline" class="text-6xl text-gray-300 dark:text-gray-600 mb-4" />
      <p class="text-gray-600 dark:text-gray-400">Chưa có hóa đơn nào</p>
    </div>

    <div v-else class="bg-white dark:bg-gray-900 rounded-lg shadow overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
        <thead class="bg-gray-50 dark:bg-gray-800">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Mã hóa đơn</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Ngày tạo</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Số tiền</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Trạng thái</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Hành động</th>
          </tr>
        </thead>
        <tbody class="bg-white dark:bg-gray-900 divide-y divide-gray-200 dark:divide-gray-700">
          <tr v-for="invoice in invoices" :key="invoice.id">
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900 dark:text-white">{{ invoice.invoiceNumber }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{{ formatDate(invoice.createdAt) }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">{{ formatCurrency(invoice.totalAmount) }}</td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full" :class="getStatusClass(invoice.status)">
                {{ invoice.status }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
              <button @click="viewInvoice(invoice)" class="text-blue-600 hover:text-blue-900 dark:text-blue-400 dark:hover:text-blue-300">Xem chi tiết</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
import { Icon } from '@iconify/vue'
import paymentAPI from '@/api/paymentApi'
import { ref, onMounted } from 'vue'

export default {
  name: 'PaymentInvoices',
  components: { Icon },
  setup() {
    const invoices = ref([])
    const loading = ref(false)

    const loadInvoices = async () => {
      loading.value = true
      try {
        const response = await paymentAPI.getUserInvoices()
        invoices.value = response.data || []
      } catch (error) {
        console.error('Error loading invoices:', error)
      } finally {
        loading.value = false
      }
    }

    const formatDate = (date) => {
      return new Date(date).toLocaleDateString('vi-VN')
    }

    const formatCurrency = (amount) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(amount)
    }

    const getStatusClass = (status) => {
      const classes = {
        PAID: 'bg-green-100 text-green-800',
        PENDING: 'bg-yellow-100 text-yellow-800',
        CANCELLED: 'bg-red-100 text-red-800'
      }
      return classes[status] || 'bg-gray-100 text-gray-800'
    }

    const viewInvoice = (invoice) => {
      console.log('View invoice:', invoice)
    }

    onMounted(() => {
      loadInvoices()
    })

    return {
      invoices,
      loading,
      formatDate,
      formatCurrency,
      getStatusClass,
      viewInvoice
    }
  }
}
</script>
