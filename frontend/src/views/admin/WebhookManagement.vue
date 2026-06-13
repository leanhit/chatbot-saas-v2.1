<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <div class="flex items-center">
        <Icon icon="mdi:webhook" class="text-2xl text-blue-600 dark:text-blue-400 mr-3" />
        <h1 class="text-2xl font-bold text-gray-800 dark:text-white">{{ $t('admin.webhook.title') }}</h1>
      </div>
      <button @click="showCreateModal = true" class="inline-flex items-center bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700">
        <Icon icon="mdi:plus" class="mr-2" />
        {{ $t('admin.webhook.add') }}
      </button>
    </div>

    <div v-if="loading" class="flex justify-center py-8">
      <Icon icon="eos-icons:loading" class="animate-spin text-4xl text-blue-600" />
    </div>

    <div v-else class="bg-white dark:bg-gray-900 rounded-lg shadow overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
        <thead class="bg-gray-50 dark:bg-gray-800">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase">{{ $t('admin.webhook.name') }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase">{{ $t('admin.webhook.url') }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase">{{ $t('admin.webhook.events') }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase">{{ $t('admin.webhook.status') }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase">{{ $t('admin.webhook.actions') }}</th>
          </tr>
        </thead>
        <tbody class="bg-white dark:bg-gray-900 divide-y divide-gray-200 dark:divide-gray-700">
          <tr v-for="webhook in webhooks" :key="webhook.id">
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900 dark:text-white">{{ webhook.name }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400 max-w-xs truncate">{{ webhook.url }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{{ webhook.eventTypes ? webhook.eventTypes.join(', ') : '-' }}</td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full" :class="getStatusClass(webhook)">
                {{ webhook.isActive ? $t('common.active') : $t('common.inactive') }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
              <button @click="testWebhook(webhook.id)" class="text-green-600 hover:text-green-900 dark:text-green-400 dark:hover:text-green-300 mr-3">{{ $t('admin.webhook.test') }}</button>
              <button @click="editWebhook(webhook)" class="text-blue-600 hover:text-blue-900 dark:text-blue-400 dark:hover:text-blue-300 mr-3">{{ $t('admin.webhook.editBtn') }}</button>
              <button @click="deleteWebhook(webhook.id)" class="text-red-600 hover:text-red-900 dark:text-red-400 dark:hover:text-red-300">{{ $t('admin.webhook.deleteBtn') }}</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Create/Edit Modal -->
    <div v-if="showCreateModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
      <div class="bg-white dark:bg-gray-900 rounded-lg p-6 w-full max-w-md">
        <h2 class="text-xl font-bold text-gray-800 dark:text-white mb-4">
          {{ editingWebhook ? $t('admin.webhook.edit') : $t('admin.webhook.add') }}
        </h2>
        <form @submit.prevent="saveWebhook">
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('admin.webhook.name') }}</label>
              <input v-model="formData.name" type="text" required class="w-full px-3 py-2 border rounded-md dark:bg-gray-800 dark:text-white" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('admin.webhook.url') }}</label>
              <input v-model="formData.url" type="url" required class="w-full px-3 py-2 border rounded-md dark:bg-gray-800 dark:text-white" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('admin.webhook.events') }}</label>
              <div class="space-y-2">
                <label v-for="event in eventTypes" :key="event" class="flex items-center">
                  <input type="checkbox" v-model="formData.eventTypes" :value="event" class="mr-2" />
                  <span class="text-sm text-gray-700 dark:text-gray-300">{{ event }}</span>
                </label>
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('admin.webhook.secret') }}</label>
              <input v-model="formData.secret" type="text" class="w-full px-3 py-2 border rounded-md dark:bg-gray-800 dark:text-white" :placeholder="$t('admin.webhook.secretPlaceholder')" />
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
  name: 'WebhookManagement',
  components: { Icon },
  setup() {
    const { t } = useI18n()
    const webhooks = ref([])
    const loading = ref(false)
    const showCreateModal = ref(false)
    const editingWebhook = ref(null)
    const eventTypes = ['PAYMENT_COMPLETED', 'PAYMENT_FAILED', 'PAYMENT_CANCELLED', 'PAYMENT_REFUNDED']
    const formData = ref({
      name: '',
      url: '',
      eventTypes: [],
      secret: ''
    })

    const loadWebhooks = async () => {
      loading.value = true
      try {
        const response = await paymentAPI.getActiveWebhooks()
        webhooks.value = response.data || []
      } catch (error) {
        console.error('Error loading webhooks:', error)
      } finally {
        loading.value = false
      }
    }

    const saveWebhook = async () => {
      try {
        if (editingWebhook.value) {
          await paymentAPI.updateWebhook(editingWebhook.value.id, formData.value)
        } else {
          await paymentAPI.createWebhook(formData.value)
        }
        closeModal()
        loadWebhooks()
      } catch (error) {
        console.error('Error saving webhook:', error)
      }
    }

    const editWebhook = (webhook) => {
      editingWebhook.value = webhook
      formData.value = { ...webhook }
      showCreateModal.value = true
    }

    const deleteWebhook = async (id) => {
      if (confirm(t('admin.webhook.deleteConfirm'))) {
        try {
          await paymentAPI.deleteWebhook(id)
          loadWebhooks()
        } catch (error) {
          console.error('Error deleting webhook:', error)
        }
      }
    }

    const testWebhook = async (id) => {
      try {
        await paymentAPI.testWebhook(id)
        alert(t('admin.webhook.testSuccess'))
      } catch (error) {
        console.error('Error testing webhook:', error)
        alert(t('admin.webhook.testFailed') + (error.response?.data?.message || error.message))
      }
    }

    const closeModal = () => {
      showCreateModal.value = false
      editingWebhook.value = null
      formData.value = {
        name: '',
        url: '',
        eventTypes: [],
        secret: ''
      }
    }

    const getStatusClass = (webhook) => {
      return webhook.isActive ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
    }

    onMounted(() => {
      loadWebhooks()
    })

    return {
      t,
      webhooks,
      loading,
      showCreateModal,
      editingWebhook,
      eventTypes,
      formData,
      saveWebhook,
      editWebhook,
      deleteWebhook,
      testWebhook,
      closeModal,
      getStatusClass
    }
  }
}
</script>
