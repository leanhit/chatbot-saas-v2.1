<template>
  <div class="bg-white dark:bg-gray-800 shadow rounded-lg p-6">
    <div class="flex justify-between items-center mb-6">
      <h3 class="text-lg font-medium text-gray-900 dark:text-white">{{ $t('tenant.overview.settings') }}</h3>
      <button
        @click="handleEdit"
        class="inline-flex items-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md text-white bg-primary hover:bg-primary/80 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary"
      >
        <Icon icon="mdi:pencil" class="h-4 w-4 mr-1" />
        {{ $t('tenant.overview.editSettings') }}
      </button>
    </div>

    <div v-if="loading" class="text-center py-8">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">{{ $t('tenant.overview.loading') }}</p>
    </div>

    <div v-else-if="!settings" class="text-center py-8">
      <Icon icon="mdi:cog" class="mx-auto h-12 w-12 text-gray-400" />
      <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">{{ $t('tenant.overview.noData') }}</p>
      <button
        @click="handleCreate"
        class="mt-4 inline-flex items-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md text-white bg-primary hover:bg-primary/80 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary"
      >
        <Icon icon="mdi:plus" class="h-4 w-4 mr-1" />
        {{ $t('tenant.overview.addSettings') }}
      </button>
    </div>

    <div v-else class="space-y-6">
      <!-- General Settings -->
      <div class="border-b dark:border-gray-700 pb-6">
        <h4 class="text-md font-medium text-gray-900 dark:text-white mb-4">{{ $t('tenant.overview.generalSettings') }}</h4>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div v-if="settings.timezone">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.timezone') }}</label>
            <p class="text-sm text-gray-900 dark:text-white">{{ settings.timezone }}</p>
          </div>
          
          <div v-if="settings.language">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.language') }}</label>
            <p class="text-sm text-gray-900 dark:text-white">{{ getLanguageLabel(settings.language) }}</p>
          </div>
          
          <div v-if="settings.dateFormat">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.dateFormat') }}</label>
            <p class="text-sm text-gray-900 dark:text-white">{{ settings.dateFormat }}</p>
          </div>
          
          <div v-if="settings.currency">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.currency') }}</label>
            <p class="text-sm text-gray-900 dark:text-white">{{ settings.currency }}</p>
          </div>
        </div>
      </div>

      <!-- Security Settings -->
      <div class="border-b dark:border-gray-700 pb-6">
        <h4 class="text-md font-medium text-gray-900 dark:text-white mb-4">{{ $t('tenant.overview.securitySettings') }}</h4>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.twoFactorAuth') }}</label>
            <span :class="settings.twoFactorEnabled ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400'">
              {{ settings.twoFactorEnabled ? $t('tenant.overview.enabled') : $t('tenant.overview.disabled') }}
            </span>
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.sessionTimeout') }}</label>
            <p class="text-sm text-gray-900 dark:text-white">{{ settings.sessionTimeout || 30 }} {{ $t('tenant.overview.minutes') }}</p>
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.passwordPolicy') }}</label>
            <p class="text-sm text-gray-900 dark:text-white">{{ getPasswordPolicyLabel(settings.passwordPolicy) }}</p>
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.ipRestriction') }}</label>
            <span :class="settings.ipRestriction ? 'text-green-600 dark:text-green-400' : 'text-gray-600 dark:text-gray-400'">
              {{ settings.ipRestriction ? $t('tenant.overview.enabled') : $t('tenant.overview.disabled') }}
            </span>
          </div>
        </div>
      </div>

      <!-- Notification Settings -->
      <div class="border-b dark:border-gray-700 pb-6">
        <h4 class="text-md font-medium text-gray-900 dark:text-white mb-4">{{ $t('tenant.overview.notificationSettings') }}</h4>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.emailNotifications') }}</label>
            <span :class="settings.emailNotifications ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400'">
              {{ settings.emailNotifications ? $t('tenant.overview.enabled') : $t('tenant.overview.disabled') }}
            </span>
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.smsNotifications') }}</label>
            <span :class="settings.smsNotifications ? 'text-green-600 dark:text-green-400' : 'text-gray-600 dark:text-gray-400'">
              {{ settings.smsNotifications ? $t('tenant.overview.enabled') : $t('tenant.overview.disabled') }}
            </span>
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.pushNotifications') }}</label>
            <span :class="settings.pushNotifications ? 'text-green-600 dark:text-green-400' : 'text-gray-600 dark:text-gray-400'">
              {{ settings.pushNotifications ? $t('tenant.overview.enabled') : $t('tenant.overview.disabled') }}
            </span>
          </div>
          
          <div v-if="settings.notificationEmail">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.notificationEmail') }}</label>
            <p class="text-sm text-gray-900 dark:text-white">{{ settings.notificationEmail }}</p>
          </div>
        </div>
      </div>

      <!-- API Settings -->
      <div>
        <h4 class="text-md font-medium text-gray-900 dark:text-white mb-4">{{ $t('tenant.overview.apiSettings') }}</h4>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.apiAccess') }}</label>
            <span :class="settings.apiAccess ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400'">
              {{ settings.apiAccess ? $t('tenant.overview.enabled') : $t('tenant.overview.disabled') }}
            </span>
          </div>
          
          <div v-if="settings.apiRateLimit">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.apiRateLimit') }}</label>
            <p class="text-sm text-gray-900 dark:text-white">{{ settings.apiRateLimit }} {{ $t('tenant.overview.requestsPerHour') }}</p>
          </div>
          
          <div v-if="settings.webhookUrl">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.webhookUrl') }}</label>
            <p class="text-sm text-gray-900 dark:text-white break-all">{{ settings.webhookUrl }}</p>
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ $t('tenant.overview.webhookEvents') }}</label>
            <p class="text-sm text-gray-900 dark:text-white">{{ getWebhookEventsLabel(settings.webhookEvents) }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Edit Modal -->
    <TenantSettingsForm
      v-if="showEditModal"
      :visible="showEditModal"
      :tenant-key="tenantKey"
      :settings="settings"
      :is-edit="isEdit"
      @close="showEditModal = false"
      @updated="handleUpdated"
    />
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import { tenantApi } from '@/api/tenantApi'
import TenantSettingsForm from './TenantSettingsForm.vue'

export default {
  name: 'TenantSettings',
  components: {
    Icon,
    TenantSettingsForm
  },
  props: {
    tenantKey: {
      type: String,
      required: true
    }
  },
  emits: ['updated'],
  setup(props, { emit }) {
    const loading = ref(false)
    const settings = ref(null)
    const showEditModal = ref(false)
    const isEdit = ref(false)

    const loadSettings = async () => {
      loading.value = true
      try {
        const response = await tenantApi.getTenantSettings(props.tenantKey)
        settings.value = response.data
      } catch (error) {
        console.error('Error loading tenant settings:', error)
      } finally {
        loading.value = false
      }
    }

    const handleEdit = () => {
      isEdit.value = true
      showEditModal.value = true
    }

    const handleCreate = () => {
      isEdit.value = false
      showEditModal.value = true
    }

    const handleSaved = () => {
      showEditModal.value = false
      loadSettings()
      emit('updated')
    }

    const getLanguageLabel = (language) => {
      const languages = {
        'en': 'English',
        'vi': 'Vietnamese',
        'de': 'German',
        'pl': 'Polish'
      }
      return languages[language] || language
    }

    const getPasswordPolicyLabel = (policy) => {
      const policies = {
        'STRICT': 'Strict (8+ chars, mixed case, numbers, symbols)',
        'MEDIUM': 'Medium (8+ chars, mixed case, numbers)',
        'BASIC': 'Basic (6+ chars)'
      }
      return policies[policy] || policy
    }

    const getWebhookEventsLabel = (events) => {
      if (!events || events.length === 0) return 'None'
      if (events.length === 1) return events[0]
      return `${events.length} events`
    }

    onMounted(() => {
      loadSettings()
    })

    return {
      loading,
      settings,
      showEditModal,
      isEdit,
      handleEdit,
      handleCreate,
      handleSaved,
      getLanguageLabel,
      getPasswordPolicyLabel,
      getWebhookEventsLabel
    }
  }
}
</script>
