<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white dark:bg-gray-800 rounded-lg max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
      <!-- Header -->
      <div class="p-6 border-b border-gray-200 dark:border-gray-700">
        <div class="flex justify-between items-center">
          <h3 class="text-xl font-semibold text-gray-900 dark:text-gray-100">
            Create New Penny Bot
          </h3>
          <button 
            @click="$emit('close')"
            class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
          >
            <Icon icon="ic:baseline-close" class="text-xl" />
          </button>
        </div>
      </div>

      <!-- Body -->
      <div class="p-6">
        <!-- Progress Steps -->
        <div class="mb-8">
          <div class="flex items-center justify-between mb-8">
            <div 
              v-for="step in steps" 
              :key="step.number"
              class="flex items-center"
            >
              <div 
                :class="[
                  'w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium transition-colors',
                  currentStep >= step.number 
                    ? 'bg-blue-600 text-white' 
                    : 'bg-gray-200 dark:bg-gray-700 text-gray-600 dark:text-gray-300'
                ]"
              >
                <span v-if="currentStep > step.number">✓</span>
                <span v-else>{{ step.number }}</span>
              </div>
              <div 
                :class="[
                  'flex-1 h-0.5 bg-gray-200 dark:bg-gray-700 rounded-full transition-all',
                  currentStep >= step.number ? 'bg-blue-600' : 'bg-gray-200 dark:bg-gray-700'
                ]"
              ></div>
              <div class="flex-1 text-sm text-gray-600 dark:text-gray-400">
                {{ step.title }}
              </div>
            </div>
          </div>
        </div>

        <!-- Step 1: Basic Information -->
        <div v-show="currentStep === 1" class="space-y-6">
          <h4 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">
            Basic Information
          </h4>
          
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Bot Name *
              </label>
              <input 
                v-model="botData.botName"
                type="text"
                placeholder="Enter bot name"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                :class="{ 'border-red-300 dark:border-red-600 focus:ring-red-500': ''}"
              />
              <p v-if="errors.botName" class="text-red-600 dark:text-red-400 text-sm mt-1">
                {{ errors.botName }}
              </p>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Bot Type *
              </label>
              <select 
                v-model="botData.botType"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="">Select bot type</option>
                <option value="CUSTOMER_SERVICE">Customer Service</option>
                <option value="SALES">Sales</option>
                <option value="SUPPORT">Technical Support</option>
                <option value="MARKETING">Marketing</option>
                <option value="HR">Human Resources</option>
                <option value="FINANCE">Finance</option>
                <option value="GENERAL">General Purpose</option>
              </select>
              <div class="mt-2 p-3 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
                <Icon :icon="getBotTypeIcon(botData.botType)" class="text-blue-600 dark:text-blue-400 mr-2" />
                <span class="text-sm text-blue-800 dark:text-blue-200">
                  {{ getBotTypeDescription(botData.botType) }}
                </span>
              </div>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Description
              </label>
              <textarea 
                v-model="botData.description"
                rows="3"
                placeholder="Describe what this bot does..."
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              ></textarea>
            </div>
          </div>
        </div>

        <!-- Step 2: Botpress Integration -->
        <div v-show="currentStep === 2" class="space-y-6">
          <h4 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">
            Botpress Integration
          </h4>
          
          <div class="space-y-4">
            <div>
              <label class="flex items-center gap-2">
                <input 
                  type="radio" 
                  v-model="integrationType"
                  value="existing"
                  class="mr-2"
                />
                <span class="text-sm text-gray-700 dark:text-gray-300">Connect Existing Bot</span>
              </label>
              <div v-if="availableBots.length > 0" class="space-y-2">
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Select Botpress Bot
                </label>
                <select 
                  v-model="botData.botpressBotId"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="">Select Botpress Bot</option>
                  <option v-for="bot in availableBots" :key="bot.id" :value="bot.id">{{ bot.name }}</option>
                </select>
              </div>
              <label class="flex items-center gap-2">
                <input 
                  type="radio" 
                  v-model="integrationType"
                  value="new"
                  class="mr-2"
                />
                <span class="text-sm font-medium text-gray-700 dark:text-gray-300">
                  Create new Botpress bot
                </span>
              </label>
              
              <div v-if="integrationType === 'new'" class="space-y-4 p-4 bg-green-50 dark:bg-green-900/20 rounded-lg">
                <p class="text-sm text-green-800 dark:text-green-200">
                  <Icon icon="ic:baseline-info" class="mr-2" />
                  A new Botpress bot will be created automatically when you save this bot.
                </p>
              </div>
            </div>
          </div>
        </div>

        <!-- Step 3: Configuration -->
        <div v-show="currentStep === 3" class="space-y-6">
          <h4 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">
            Configuration
          </h4>
          
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Configuration (JSON)
              </label>
              <div class="relative">
                <textarea 
                  v-model="botData.configuration"
                  rows="8"
                  placeholder="Enter bot configuration in JSON format"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 font-mono text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                ></textarea>
                <div class="absolute top-2 right-2">
                  <button 
                    @click="formatConfiguration"
                    class="px-2 py-1 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded hover:bg-gray-200 dark:hover:bg-gray-600"
                    title="Format JSON"
                  >
                    <Icon icon="ic:baseline-auto-fix-high" />
                  </button>
                </div>
              </div>
              <p class="text-xs text-gray-500 dark:text-gray-400 mt-1">
                  Enter valid JSON configuration. Refer to documentation for available options.
                </p>
            </div>

            <!-- Configuration Templates -->
            <div class="space-y-4">
              <h5 class="text-sm font-medium text-gray-900 dark:text-gray-100 mb-3">
                Configuration Templates
              </h5>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <button 
                  v-for="template in configurationTemplates" 
                  :key="template.name"
                  @click="applyTemplate(template)"
                  class="p-3 text-left bg-gray-50 dark:bg-gray-700 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-600 transition-colors"
                >
                  <div class="font-medium text-gray-900 dark:text-gray-100">{{ template.name }}</div>
                  <div class="text-xs text-gray-600 dark:text-gray-400">{{ template.description }}</div>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Step 4: Review -->
        <div v-show="currentStep === 4" class="space-y-6">
          <h4 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">
            Review & Create
          </h4>
          
          <!-- Bot Summary -->
          <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-6 mb-6">
            <h5 class="font-medium text-gray-900 dark:text-gray-100 mb-3">Bot Summary</h5>
            <div class="space-y-2">
              <div class="flex justify-between">
                <span class="text-gray-600 dark:text-gray-400">Name:</span>
                <span class="font-medium text-gray-900 dark:text-gray-100">{{ botData.botName }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600 dark:text-gray-400">Type:</span>
                <span class="font-medium text-gray-900 dark:text-gray-100">{{ getBotTypeDisplayName(botData.botType) }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600 dark:text-gray-400">Integration:</span>
                <span class="font-medium text-gray-900 dark:text-gray-100">
                  {{ integrationType === 'existing' ? 'Existing Botpress Bot' : 'New Botpress Bot' }}
                </span>
              </div>
            </div>
            <div v-if="botData.description" class="flex justify-between">
              <span class="text-gray-600 dark:text-gray-400">Description:</span>
                <span class="font-medium text-gray-900 dark:text-gray-100">{{ botData.description }}</span>
            </div>
          </div>

          <!-- Configuration Preview -->
          <div v-if="botData.configuration" class="bg-gray-50 dark:bg-gray-700 rounded-lg p-6 mb-6">
            <h5 class="font-medium text-gray-900 dark:text-gray-100 mb-3">Configuration Preview</h5>
            <pre class="text-xs text-gray-700 dark:text-gray-300 overflow-x-auto bg-gray-900 dark:bg-gray-800 rounded p-3">{{ formatConfiguration(botData.configuration) }}</pre>
          </div>
        </div>
      </div>

      <!-- Footer -->
      <div class="p-6 border-t border-gray-200 dark:border-gray-700">
        <div class="flex justify-between">
          <button 
            @click="previousStep"
            :disabled="currentStep === 1"
            class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            Previous
          </button>
          
          <div class="flex gap-2">
            <button 
              v-if="currentStep > 1"
              @click="previousStep"
              class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-600 transition-colors"
            >
              Previous
            </button>
            
            <button 
              v-if="currentStep < 4"
              @click="nextStep"
              class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
            >
              Next
            </button>
            
            <button 
              v-if="currentStep === 4"
              @click="createBot"
              :disabled="!isFormValid"
              class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center gap-2"
            >
              <Icon v-if="!isCreating" icon="ic:baseline-refresh" class="animate-spin" />
              {{ isCreating ? 'Creating...' : 'Create Bot' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { usePennyBotStore } from '@/stores/penny/pennyBotStore'

const emit = defineEmits(['close', 'created'])

const pennyBotStore = usePennyBotStore()

// State
const currentStep = ref(1)
const isCreating = ref(false)
const integrationType = ref('existing')
const availableBots = ref([])

// Form data
const botData = ref({
  botName: '',
  botType: '',
  description: '',
  botpressBotId: '',
  configuration: '{}'
})

// Steps configuration
const steps = [
  {
    number: 1,
    title: 'Basic Information',
    description: 'Enter bot name, type, and description'
  },
  {
    number: 2,
    title: 'Botpress Integration',
    description: 'Connect to existing or create new Botpress bot'
  },
  {
    number: 3,
    title: 'Configuration',
    description: 'Configure bot behavior and settings'
  },
  {
    number: 4,
    title: 'Review & Create',
    description: 'Review bot details and create'
  }
]

// Configuration templates
const configurationTemplates = [
  {
    name: 'Customer Service Bot',
    description: 'Basic customer service bot with FAQ and escalation',
    configuration: JSON.stringify({
      welcomeMessage: "Hello! How can I help you today?",
      escalationThreshold: 3,
      autoEscalation: true,
      workingHours: "9-17, Mon-Fri",
      languages: ["en", "es", "de"]
    }, null, 2)
  },
  {
    name: 'Sales Bot',
    description: 'Sales assistant bot with product knowledge',
    configuration: JSON.stringify({
      welcomeMessage: "Hello! I can help you with our products and services.",
      productCatalog: true,
      leadCapture: true,
      appointmentBooking: true,
      followUpEnabled: true
    }, null, 2)
  },
  {
    name: 'Technical Support Bot',
    description: 'Technical support bot with troubleshooting capabilities',
    configuration: JSON.stringify({
      welcomeMessage: "Hello! I can help you with technical issues.",
      knowledgeBase: true,
      ticketCreation: true,
      diagnosticTools: true,
      escalationLogic: true
    }, null, 2)
  },
  {
    name: 'Marketing Bot',
    description: 'Marketing bot for campaigns and promotions',
    configuration: JSON.stringify({
      welcomeMessage: "Welcome! Check out our latest offers!",
      campaignManagement: true,
      leadGeneration: true,
      analytics: true
    }, null, 2)
  },
  {
    name: 'HR Bot',
    description: 'HR assistant for employee inquiries',
    configuration: JSON.stringify({
      welcomeMessage: "Hello! I can help with HR related questions.",
      employeeDirectory: true,
      leaveManagement: true,
      policyInformation: true
    }, null, 2)
  },
  {
    name: 'Finance Bot',
    description: 'Finance bot for billing and payment inquiries',
    configuration: JSON.stringify({
      welcomeMessage: "Hello! I can help with billing and payment questions.",
      accountInquiries: true,
      transactionHistory: true,
      paymentProcessing: true
    }, null, 2)
  },
  {
    name: 'General Purpose Bot',
    description: 'General purpose bot for various tasks',
    configuration: JSON.stringify({
      welcomeMessage: "Hello! How can I assist you today?",
      customCommands: true,
      multiLanguage: true
    }, null, 2)
  }
]

// Computed
const isFormValid = computed(() => {
  return botData.botName.trim() !== '' && 
         botData.botType !== '' &&
         (integrationType !== 'existing' || botData.botpressBotId !== '')
})

// Methods
const nextStep = () => {
  if (currentStep.value < 4) {
    currentStep.value++
  }
}

const previousStep = () => {
  if (currentStep.value > 1) {
    currentStep.value--
  }
}

const getBotTypeIcon = (botType) => {
  const iconMap = {
    'CUSTOMER_SERVICE': 'ic:baseline-support-agent',
    'SALES': 'ic:baseline-shopping-cart',
    'SUPPORT': 'ic:baseline-headset-mic',
    'MARKETING': 'ic:baseline-campaign',
    'HR': 'ic:baseline-people',
    'FINANCE': 'ic:baseline-account-balance',
    'GENERAL': 'ic:baseline-settings'
  }
  return iconMap[botType] || 'ic:baseline-settings'
}

const getBotTypeDisplayName = (botType) => {
  const nameMap = {
    'CUSTOMER_SERVICE': 'Customer Service',
    'SALES': 'Sales',
    'SUPPORT': 'Technical Support',
    'MARKETING': 'Marketing',
    'HR': 'Human Resources',
    'FINANCE': 'Finance',
    'GENERAL': 'General Purpose'
  }
  return nameMap[botType] || botType
}

const getBotTypeDescription = (botType) => {
  const descriptionMap = {
    'CUSTOMER_SERVICE': 'Handles customer service inquiries, FAQ, and escalations',
    'SALES': 'Assists with product information and sales process',
    'SUPPORT': 'Provides technical support and troubleshooting assistance',
    'MARKETING': 'Manages marketing campaigns and lead generation',
    'HR': 'Handles HR-related questions and employee assistance',
    'FINANCE': 'Assists with billing, payments, and account inquiries',
    'GENERAL': 'General purpose bot for various tasks'
  }
  return descriptionMap[botType] || 'General purpose bot'
}

const formatConfiguration = (config) => {
  try {
    const parsed = JSON.parse(config)
    return JSON.stringify(parsed, null, 2)
  } catch (error) {
    return config
  }
}

const applyTemplate = (template) => {
  botData.configuration = template.configuration
}

const fetchAvailableBots = async () => {
  try {
    // Mock API call to get available Botpress bots
    const response = await fetch('/api/botpress/bots')
    const data = await response.json()
    availableBots.value = data
  } catch (error) {
    console.error('Failed to fetch available bots:', error)
    availableBots.value = []
  }
}

const createBot = async () => {
  if (!isFormValid.value) return
  
  isCreating.value = true
  
  try {
    const botDataToSubmit = {
      ...botData,
      configuration: JSON.parse(botData.configuration)
    }
    
    const response = await pennyBotStore.createBot(botDataToSubmit)
    emit('created', response)
    
    // Reset form
    currentStep.value = 1
    Object.assign(botData, {
      botName: '',
      botType: '',
      description: '',
      botpressBotId: '',
      configuration: '{}'
    })
    
  } catch (error) {
    console.error('Failed to create bot:', error)
  } finally {
    isCreating.value = false
  }
}

// Lifecycle
onMounted(() => {
  fetchAvailableBots()
})
</script>

<style scoped>
.modal-backdrop {
  backdrop-filter: blur(4px);
}

.step-indicator {
  transition: all 0.3s ease-in-out;
}
</style>
