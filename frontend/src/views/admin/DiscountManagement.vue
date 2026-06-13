<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <div class="flex items-center">
        <Icon icon="mdi:tag" class="text-2xl text-blue-600 dark:text-blue-400 mr-3" />
        <h1 class="text-2xl font-bold text-gray-800 dark:text-white">{{ $t('admin.discount.title') }}</h1>
      </div>
      <button @click="showCreateModal = true" class="inline-flex items-center bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700">
        <Icon icon="mdi:plus" class="mr-2" />
        {{ $t('admin.discount.add') }}
      </button>
    </div>

    <div v-if="loading" class="flex justify-center py-8">
      <Icon icon="eos-icons:loading" class="animate-spin text-4xl text-blue-600" />
    </div>

    <div v-else class="bg-white dark:bg-gray-900 rounded-lg shadow overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
        <thead class="bg-gray-50 dark:bg-gray-800">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase">{{ $t('admin.discount.code') }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase">{{ $t('admin.discount.type') }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase">{{ $t('admin.discount.value') }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase">{{ $t('admin.discount.usage') }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase">{{ $t('admin.discount.expires') }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase">{{ $t('admin.discount.status') }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase">{{ $t('admin.discount.actions') }}</th>
          </tr>
        </thead>
        <tbody class="bg-white dark:bg-gray-900 divide-y divide-gray-200 dark:divide-gray-700">
          <tr v-for="discount in discounts" :key="discount.id">
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900 dark:text-white">{{ discount.code }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{{ discount.type }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
              {{ discount.type === 'PERCENTAGE' ? discount.value + '%' : formatCurrency(discount.value) }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
              {{ discount.usedCount }}/{{ discount.maxUses || '∞' }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{{ formatDate(discount.expiresAt) }}</td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full" :class="getStatusClass(discount)">
                {{ discount.isActive ? $t('common.active') : $t('common.inactive') }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
              <button @click="editDiscount(discount)" class="text-blue-600 hover:text-blue-900 dark:text-blue-400 dark:hover:text-blue-300 mr-3">{{ $t('admin.discount.editBtn') }}</button>
              <button @click="deleteDiscount(discount.id)" class="text-red-600 hover:text-red-900 dark:text-red-400 dark:hover:text-red-300">{{ $t('admin.discount.deleteBtn') }}</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Create/Edit Modal -->
    <div v-if="showCreateModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
      <div class="bg-white dark:bg-gray-900 rounded-lg p-6 w-full max-w-md">
        <h2 class="text-xl font-bold text-gray-800 dark:text-white mb-4">
          {{ editingDiscount ? $t('admin.discount.edit') : $t('admin.discount.add') }}
        </h2>
        <form @submit.prevent="saveDiscount">
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('admin.discount.code') }}</label>
              <input v-model="formData.code" type="text" required class="w-full px-3 py-2 border rounded-md dark:bg-gray-800 dark:text-white" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('admin.discount.type') }}</label>
              <select v-model="formData.type" class="w-full px-3 py-2 border rounded-md dark:bg-gray-800 dark:text-white">
                <option value="PERCENTAGE">{{ $t('admin.discount.percentage') }}</option>
                <option value="FIXED_AMOUNT">{{ $t('admin.discount.fixedAmount') }}</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('admin.discount.value') }}</label>
              <input v-model.number="formData.value" type="number" required class="w-full px-3 py-2 border rounded-md dark:bg-gray-800 dark:text-white" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('admin.discount.maxUses') }}</label>
              <input v-model.number="formData.maxUses" type="number" class="w-full px-3 py-2 border rounded-md dark:bg-gray-800 dark:text-white" :placeholder="$t('admin.discount.maxUsesPlaceholder')" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('admin.discount.expiresAt') }}</label>
              <input v-model="formData.expiresAt" type="datetime-local" class="w-full px-3 py-2 border rounded-md dark:bg-gray-800 dark:text-white" />
            </div>
          </div>
          <div class="mt-6 flex justify-end gap-3">
            <button type="button" @click="closeModal" class="px-4 py-2 border rounded-md hover:bg-gray-100 dark:hover:bg-gray-800">{{ $t('common.cancel') }}</button>
            <button type="submit" class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700">{{ $t('common.save') }}</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { Icon } from '@iconify/vue'
import paymentAPI from '@/api/paymentApi'
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'

export default {
  name: 'DiscountManagement',
  components: { Icon },
  setup() {
    const { t } = useI18n()
    const discounts = ref([])
    const loading = ref(false)
    const showCreateModal = ref(false)
    const editingDiscount = ref(null)
    const formData = ref({
      code: '',
      type: 'PERCENTAGE',
      value: 0,
      maxUses: null,
      expiresAt: ''
    })

    const loadDiscounts = async () => {
      loading.value = true
      try {
        const response = await paymentAPI.getActiveDiscounts()
        discounts.value = response.data || []
      } catch (error) {
        console.error('Error loading discounts:', error)
      } finally {
        loading.value = false
      }
    }

    const saveDiscount = async () => {
      try {
        if (editingDiscount.value) {
          await paymentAPI.updateDiscount(editingDiscount.value.id, formData.value)
        } else {
          await paymentAPI.createDiscount(formData.value)
        }
        closeModal()
        loadDiscounts()
      } catch (error) {
        console.error('Error saving discount:', error)
      }
    }

    const editDiscount = (discount) => {
      editingDiscount.value = discount
      formData.value = { ...discount }
      showCreateModal.value = true
    }

    const deleteDiscount = async (id) => {
      if (confirm(t('admin.discount.deleteConfirm'))) {
        try {
          await paymentAPI.deleteDiscount(id)
          loadDiscounts()
        } catch (error) {
          console.error('Error deleting discount:', error)
        }
      }
    }

    const closeModal = () => {
      showCreateModal.value = false
      editingDiscount.value = null
      formData.value = {
        code: '',
        type: 'PERCENTAGE',
        value: 0,
        maxUses: null,
        expiresAt: ''
      }
    }

    const formatDate = (date) => {
      return date ? new Date(date).toLocaleDateString('vi-VN') : 'N/A'
    }

    const formatCurrency = (amount) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(amount)
    }

    const getStatusClass = (discount) => {
      return discount.isActive ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
    }

    onMounted(() => {
      loadDiscounts()
    })

    return {
      t,
      discounts,
      loading,
      showCreateModal,
      editingDiscount,
      formData,
      saveDiscount,
      editDiscount,
      deleteDiscount,
      closeModal,
      formatDate,
      formatCurrency,
      getStatusClass
    }
  }
}
</script>
