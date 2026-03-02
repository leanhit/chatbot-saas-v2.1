<template>
  <div class="penny-bot-modal-backdrop" @click="closeOnBackdrop">
    <div class="penny-bot-modal" @click.stop>
      <div class="modal-header">
        <h2 class="modal-title">
          {{ bot ? $t('Edit Penny Bot') : $t('Create Penny Bot') }}
        </h2>
        <button @click="$emit('close')" class="close-button">
          <Icon icon="mdi:close" class="h-5 w-5" />
        </button>
      </div>

      <form @submit.prevent="handleSubmit" class="modal-body">
        <!-- Bot Name -->
        <div class="form-group">
          <label for="botName" class="form-label">
            {{ $t('Bot Name') }} <span class="required">*</span>
          </label>
          <input
            id="botName"
            v-model="formData.botName"
            type="text"
            class="form-input"
            :placeholder="$t('Enter bot name')"
            required
          />
        </div>

        <!-- Bot Type -->
        <div class="form-group">
          <label for="botType" class="form-label">
            {{ $t('Bot Type') }} <span class="required">*</span>
          </label>
          <select
            id="botType"
            v-model="formData.botType"
            class="form-select"
            required
          >
            <option value="" disabled>{{ $t('Select bot type') }}</option>
            <option
              v-for="type in botTypes"
              :key="type.value"
              :value="type.value"
            >
              {{ type.label }}
            </option>
          </select>
        </div>

        <!-- Description -->
        <div class="form-group">
          <label for="description" class="form-label">
            {{ $t('Description') }}
          </label>
          <textarea
            id="description"
            v-model="formData.description"
            rows="3"
            class="form-textarea"
            :placeholder="$t('Enter bot description (optional)')"
          ></textarea>
        </div>

        <!-- Bot Type Info -->
        <div v-if="formData.botType" class="bot-type-info">
          <div class="info-header">
            <Icon :icon="getBotTypeIcon(formData.botType)" class="h-5 w-5 mr-2" />
            <span>{{ getBotTypeDisplayName(formData.botType) }}</span>
          </div>
          <div class="info-description">
            {{ getBotTypeDescription(formData.botType) }}
          </div>
          <div class="info-details">
            <div class="detail-item">
              <span class="detail-label">{{ $t('Botpress ID') }}:</span>
              <span class="detail-value">{{ getBotpressBotId(formData.botType) }}</span>
            </div>
          </div>
        </div>
      </form>

      <div class="modal-footer">
        <button
          type="button"
          @click="$emit('close')"
          class="btn btn-secondary"
        >
          {{ $t('Cancel') }}
        </button>
        <button
          type="submit"
          @click="handleSubmit"
          :disabled="submitting"
          class="btn btn-primary"
        >
          <Icon v-if="submitting" icon="mdi:loading" class="animate-spin h-4 w-4 mr-2" />
          {{ bot ? $t('Update Bot') : $t('Create Bot') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { useI18n } from 'vue-i18n'
import { usePennyBotStore } from '@/stores/pennyBotStore'

export default {
  name: 'PennyBotModal',
  components: {
    Icon
  },
  props: {
    bot: {
      type: Object,
      default: null
    }
  },
  emits: ['close', 'saved'],
  setup(props, { emit }) {
    const { t } = useI18n()
    const pennyBotStore = usePennyBotStore()

    const submitting = ref(false)
    const formData = ref({
      botName: '',
      botType: '',
      description: ''
    })

    const botTypes = [
      { value: 'CUSTOMER_SERVICE', label: 'Customer Service' },
      { value: 'SALES', label: 'Sales' },
      { value: 'SUPPORT', label: 'Technical Support' },
      { value: 'MARKETING', label: 'Marketing' },
      { value: 'HR', label: 'Human Resources' },
      { value: 'FINANCE', label: 'Finance' },
      { value: 'GENERAL', label: 'General Purpose' }
    ]

    // Initialize form data when bot prop changes
    watch(() => props.bot, (newBot) => {
      if (newBot) {
        formData.value = {
          botName: newBot.botName || '',
          botType: newBot.botType || '',
          description: newBot.description || ''
        }
      } else {
        formData.value = {
          botName: '',
          botType: '',
          description: ''
        }
      }
    }, { immediate: true })

    const handleSubmit = async () => {
      if (!formData.value.botName || !formData.value.botType) {
        return
      }

      submitting.value = true
      try {
        if (props.bot) {
          // Update existing bot
          await pennyBotStore.updatePennyBot(props.bot.botId, {
            botName: formData.value.botName,
            botType: formData.value.botType,
            description: formData.value.description
          })
        } else {
          // Create new bot
          await pennyBotStore.createPennyBot({
            botName: formData.value.botName,
            botType: formData.value.botType,
            botDescription: formData.value.description
          })
        }
        emit('saved')
      } catch (error) {
        console.error('Failed to save bot:', error)
        // You could show an error message here
      } finally {
        submitting.value = false
      }
    }

    const closeOnBackdrop = (event) => {
      if (event.target === event.currentTarget) {
        emit('close')
      }
    }

    const getBotTypeIcon = (botType) => {
      const icons = {
        'CUSTOMER_SERVICE': 'mdi:headset',
        'SALES': 'mdi:cash-register',
        'SUPPORT': 'mdi:tools',
        'MARKETING': 'mdi:bullhorn',
        'HR': 'mdi:account-tie',
        'FINANCE': 'mdi:currency-usd',
        'GENERAL': 'mdi:robot'
      }
      return icons[botType] || 'mdi:robot'
    }

    const getBotTypeDisplayName = (botType) => {
      const names = {
        'CUSTOMER_SERVICE': 'Customer Service',
        'SALES': 'Sales',
        'SUPPORT': 'Technical Support',
        'MARKETING': 'Marketing',
        'HR': 'Human Resources',
        'FINANCE': 'Finance',
        'GENERAL': 'General Purpose'
      }
      return names[botType] || botType
    }

    const getBotTypeDescription = (botType) => {
      const descriptions = {
        'CUSTOMER_SERVICE': 'Handle customer inquiries, support requests, and provide assistance with products or services.',
        'SALES': 'Engage with potential customers, provide product information, and assist with sales processes.',
        'SUPPORT': 'Provide technical support, troubleshooting assistance, and help resolve technical issues.',
        'MARKETING': 'Promote products, run marketing campaigns, and engage with customers for marketing purposes.',
        'HR': 'Handle HR-related inquiries, employee assistance, and provide HR policy information.',
        'FINANCE': 'Assist with financial inquiries, billing questions, and provide financial guidance.',
        'GENERAL': 'Handle general inquiries and provide versatile assistance across multiple domains.'
      }
      return descriptions[botType] || 'General purpose bot for various tasks.'
    }

    const getBotpressBotId = (botType) => {
      const botpressIds = {
        'CUSTOMER_SERVICE': 'botpress-customer-service-001',
        'SALES': 'botpress-sales-001',
        'SUPPORT': 'botpress-support-001',
        'MARKETING': 'botpress-marketing-001',
        'HR': 'botpress-hr-001',
        'FINANCE': 'botpress-finance-001',
        'GENERAL': 'botpress-general-001'
      }
      return botpressIds[botType] || 'botpress-general-001'
    }

    return {
      formData,
      submitting,
      botTypes,
      handleSubmit,
      closeOnBackdrop,
      getBotTypeIcon,
      getBotTypeDisplayName,
      getBotTypeDescription,
      getBotpressBotId
    }
  }
}
</script>

<style scoped>
.penny-bot-modal-backdrop {
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
  padding: 20px;
}

.penny-bot-modal {
  background: white;
  border-radius: 12px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  max-width: 500px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
}

.dark .penny-bot-modal {
  background: #1f2937;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px;
  border-bottom: 1px solid #e5e7eb;
}

.dark .modal-header {
  border-bottom-color: #374151;
}

.modal-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.dark .modal-title {
  color: white;
}

.close-button {
  background: none;
  border: none;
  padding: 8px;
  border-radius: 6px;
  cursor: pointer;
  color: #6b7280;
  transition: all 0.2s;
}

.close-button:hover {
  background-color: #f3f4f6;
  color: #1f2937;
}

.dark .close-button {
  color: #9ca3af;
}

.dark .close-button:hover {
  background-color: #374151;
  color: white;
}

.modal-body {
  padding: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  margin-bottom: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.dark .form-label {
  color: #d1d5db;
}

.required {
  color: #ef4444;
}

.form-input,
.form-select,
.form-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.dark .form-input,
.dark .form-select,
.dark .form-textarea {
  background: #374151;
  border-color: #4b5563;
  color: white;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.bot-type-info {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px;
  margin-top: 20px;
}

.dark .bot-type-info {
  background: #374151;
  border-color: #4b5563;
}

.info-header {
  display: flex;
  align-items: center;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.dark .info-header {
  color: white;
}

.info-description {
  color: #6b7280;
  font-size: 14px;
  margin-bottom: 12px;
  line-height: 1.5;
}

.dark .info-description {
  color: #9ca3af;
}

.info-details {
  border-top: 1px solid #e2e8f0;
  padding-top: 12px;
}

.dark .info-details {
  border-top-color: #4b5563;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}

.detail-label {
  color: #6b7280;
  font-weight: 500;
}

.dark .detail-label {
  color: #9ca3af;
}

.detail-value {
  color: #1f2937;
  font-family: monospace;
  font-size: 12px;
}

.dark .detail-value {
  color: #d1d5db;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 24px;
  border-top: 1px solid #e5e7eb;
}

.dark .modal-footer {
  border-top-color: #374151;
}

.btn {
  padding: 10px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
  display: inline-flex;
  align-items: center;
}

.btn-secondary {
  background: #f3f4f6;
  color: #374151;
}

.btn-secondary:hover {
  background: #e5e7eb;
}

.dark .btn-secondary {
  background: #374151;
  color: #d1d5db;
}

.dark .btn-secondary:hover {
  background: #4b5563;
}

.btn-primary {
  background: #3b82f6;
  color: white;
}

.btn-primary:hover {
  background: #2563eb;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.animate-spin {
  animation: spin 1s linear infinite;
}
</style>
