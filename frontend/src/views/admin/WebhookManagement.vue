<template>
  <div class="webhook-management p-4">
    <!-- Header -->
    <div class="mt-2 w-full">
      <div class="lg:flex grid-cols-1 lg:space-y-0 space-y-3 gap-5 justify-between">
        <div>
          <p class="uppercase text-xs text-gray-700 dark:text-gray-400 font-semibold">Admin</p>
          <h1 class="text-2xl text-gray-900 dark:text-gray-200 font-medium">
            {{ $t('admin.webhook.title') }}
          </h1>
        </div>
        <div class="flex gap-2">
          <button
            @click="loadWebhooks"
            :disabled="loading"
            class="bg-white dark:bg-gray-800 hover:border-gray-200 dark:hover:bg-gray-700 dark:text-white dark:border-gray-700 border rounded py-2 px-5 flex items-center gap-2"
          >
            <Icon icon="mdi:refresh" :class="{'animate-spin': loading}" class="text-lg" />
            Refresh
          </button>
          <button
            @click="showCreateModal = true"
            class="bg-primary border flex gap-2 text-white hover:bg-primary/80 dark:border-gray-700 rounded py-3 px-5"
          >
            <span class="icon text-2xl"><Icon icon="ic:twotone-plus" /></span>
            <span class="text">{{ $t('admin.webhook.add') }}</span>
          </button>
        </div>
      </div>
    </div>

    <div v-if="loading" class="p-8 text-center mt-6">
      <Icon icon="eos-icons:loading" class="animate-spin text-6xl text-gray-300 dark:text-gray-600 mx-auto" />
      <p class="mt-2 text-gray-500 dark:text-gray-400">Loading webhooks...</p>
    </div>

    <div v-else class="bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden mt-6 border dark:border-gray-700">
      <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
        <thead class="bg-gray-50 dark:bg-gray-700">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase">{{ $t('admin.webhook.name') }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase">{{ $t('admin.webhook.url') }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase">{{ $t('admin.webhook.events') }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase">{{ $t('admin.webhook.status') }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase">{{ $t('admin.webhook.actions') }}</th>
          </tr>
        </thead>
        <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
          <tr v-for="webhook in webhooks" :key="webhook.id" class="hover:bg-gray-50 dark:hover:bg-gray-700">
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
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-md border dark:border-gray-700">
        <h2 class="text-xl font-bold text-gray-900 dark:text-gray-200 mb-4">
          {{ editingWebhook ? $t('admin.webhook.edit') : $t('admin.webhook.add') }}
        </h2>
        <form @submit.prevent="saveWebhook">
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('admin.webhook.name') }}</label>
              <input v-model="formData.name" type="text" required class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded-md" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('admin.webhook.url') }}</label>
              <input v-model="formData.url" type="url" required class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded-md" />
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
              <input v-model="formData.secret" type="text" class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded-md" :placeholder="$t('admin.webhook.secretPlaceholder')" />
            </div>
          </div>
          <div class="mt-6 flex justify-end gap-3">
            <button type="button" @click="closeModal" class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md hover:bg-gray-100 dark:hover:bg-gray-700 dark:text-white">{{ $t('common.cancel') }}</button>
            <button type="submit" class="px-4 py-2 bg-primary text-white rounded-md hover:bg-primary/80">{{ $t('common.save') }}</button>
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
