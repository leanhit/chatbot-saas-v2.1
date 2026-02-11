<template>
  <div class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full z-50">
    <div class="relative min-h-screen flex items-center justify-center p-4">
      <div class="relative bg-white dark:bg-gray-800 rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:w-full sm:max-w-lg">
        <!-- Header -->
        <div class="bg-white dark:bg-gray-800 px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
          <div class="flex items-center justify-between">
            <h3 class="text-lg leading-6 font-medium text-gray-900 dark:text-white">
              {{ bot ? $t('Edit Penny Bot') : $t('Create Penny Bot') }}
            </h3>
            <button
              @click="$emit('close')"
              class="rounded-md text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              <Icon icon="mdi:close" class="h-6 w-6" />
            </button>
          </div>
        </div>

        <!-- Body -->
        <form @submit.prevent="handleSubmit" class="bg-white dark:bg-gray-800 px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
          <div class="space-y-6">
            <!-- Bot Name -->
            <div>
              <label for="bot-name" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                {{ $t('Bot Name') }}
              </label>
              <div class="mt-1">
                <input
                  id="bot-name"
                  v-model="form.name"
                  type="text"
                  required
                  :placeholder="$t('Enter bot name')"
                  class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                />
              </div>
            </div>

            <!-- Bot Type -->
            <div>
              <label for="bot-type" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                {{ $t('Bot Type') }}
              </label>
              <div class="mt-1">
                <select
                  id="bot-type"
                  v-model="form.type"
                  required
                  class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                >
                  <option value="">{{ $t('Select bot type') }}</option>
                  <option value="customer-service">{{ $t('Customer Service') }}</option>
                  <option value="sales">{{ $t('Sales Assistant') }}</option>
                  <option value="support">{{ $t('Technical Support') }}</option>
                  <option value="general">{{ $t('General Purpose') }}</option>
                </select>
              </div>
            </div>

            <!-- Description -->
            <div>
              <label for="bot-description" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                {{ $t('Description') }}
              </label>
              <div class="mt-1">
                <textarea
                  id="bot-description"
                  v-model="form.description"
                  rows="3"
                  :placeholder="$t('Enter bot description (optional)')"
                  class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                />
              </div>
            </div>

            <!-- Botpress ID -->
            <div>
              <label for="botpress-id" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                {{ $t('Botpress ID') }}
              </label>
              <div class="mt-1">
                <input
                  id="botpress-id"
                  v-model="form.botpressId"
                  type="text"
                  :placeholder="$t('Enter Botpress ID')"
                  class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                />
              </div>
            </div>
          </div>
        </form>

        <!-- Footer -->
        <div class="bg-gray-50 dark:bg-gray-700 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
          <button
            type="button"
            @click="$emit('close')"
            class="w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 text-base font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 dark:bg-gray-800 dark:border-gray-600 dark:text-white dark:hover:bg-gray-700 sm:w-auto sm:ml-3"
          >
            {{ $t('Cancel') }}
          </button>
          <button
            type="submit"
            @click="handleSubmit"
            :disabled="loading"
            class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 text-base font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 sm:w-auto"
          >
            <Icon v-if="loading" icon="mdi:loading" class="animate-spin h-4 w-4 mr-2" />
            {{ bot ? $t('Update Bot') : $t('Create Bot') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Icon } from '@iconify/vue'

const { t } = useI18n()

const props = defineProps({
  bot: {
    type: Object,
    default: null
  },
  show: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close', 'saved'])

// State
const loading = ref(false)
const form = ref({
  name: '',
  type: '',
  description: '',
  botpressId: ''
})

// Methods
const handleSubmit = async () => {
  loading.value = true
  try {
    // TODO: Replace with actual API call
    // const botData = {
    //   ...form.value,
    //   id: props.bot?.id
    // }
    // 
    // if (props.bot) {
    //   await pennyApi.updateBot(props.bot.id, botData)
    // } else {
    //   const response = await pennyApi.createBot(botData)
    //   botData.id = response.data.id
    // }
    
    // Mock API call
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    const savedBot = {
      id: props.bot?.id || Date.now().toString(),
      ...form.value,
      isActive: props.bot?.isActive ?? true,
      createdAt: props.bot?.createdAt || new Date()
    }
    
    emit('saved', savedBot)
  } catch (error) {
    console.error('Failed to save bot:', error)
  } finally {
    loading.value = false
  }
}

// Watch for bot prop changes
watch(() => props.bot, (newBot) => {
  if (newBot) {
    form.value = {
      name: newBot.name || '',
      type: newBot.type || '',
      description: newBot.description || '',
      botpressId: newBot.botpressId || ''
    }
  } else {
    form.value = {
      name: '',
      type: '',
      description: '',
      botpressId: ''
    }
  }
}, { immediate: true })

// Close modal when ESC key is pressed
onMounted(() => {
  const handleEscape = (e) => {
    if (e.key === 'Escape' && props.show) {
      emit('close')
    }
  }
  document.addEventListener('keydown', handleEscape)
  
  return () => {
    document.removeEventListener('keydown', handleEscape)
  }
})
</script>

<style scoped>
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
