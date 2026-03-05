<template>
  <div class="penny-rules">
    <!-- Header -->
    <div class="flex justify-between items-center mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white">
          {{ $t('Penny Bot Rules') }}
        </h1>
        <p class="text-gray-600 dark:text-gray-400 mt-1">
          {{ $t('Manage custom rules and logic for your Penny bots') }}
        </p>
      </div>
      <div class="flex items-center space-x-4">
        <!-- Bot Selection -->
        <div v-if="availableBots.length > 0" class="flex items-center space-x-2">
          <select
            v-model="selectedBot"
            @change="selectBot(selectedBot)"
            class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-800 text-gray-900 dark:text-white"
          >
            <option value="" disabled>{{ $t('Select a bot') }}</option>
            <option
              v-for="bot in availableBots"
              :key="bot.id"
              :value="bot"
            >
              {{ bot.botName }} - {{ getBotTypeDisplayName(bot.botType) }}
            </option>
          </select>
        </div>
        <button
          @click="showCreateModal = true"
          :disabled="!selectedBot"
          class="inline-flex items-center px-4 py-2 bg-primary text-white rounded-md hover:bg-primary/80 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <Icon icon="mdi:plus" class="mr-2" />
          {{ $t('Create Rule') }}
        </button>
      </div>
    </div>

    <!-- Bot Selection Message -->
    <div v-if="!selectedBot && availableBots.length > 0" class="bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 rounded-lg p-6 mb-6">
      <div class="flex items-center">
        <Icon icon="mdi:information" class="h-5 w-5 text-yellow-600 dark:text-yellow-400 mr-3" />
        <div>
          <h3 class="text-lg font-medium text-yellow-800 dark:text-yellow-200">
            {{ $t('Select a Bot to View Rules') }}
          </h3>
          <p class="text-yellow-700 dark:text-yellow-300 mt-1">
            {{ $t('Please select a bot from the dropdown above to view and manage its rules.') }}
          </p>
        </div>
      </div>
    </div>

    <!-- No Bots Available -->
    <div v-else-if="availableBots.length === 0" class="text-center py-12">
      <Icon icon="mdi:robot-outline" class="h-16 w-16 text-gray-400 mx-auto mb-4" />
      <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">
        {{ $t('No Bots Available') }}
      </h3>
      <p class="text-gray-600 dark:text-gray-400 mb-4">
        {{ $t('Create a bot first to manage rules') }}
      </p>
      <button
        @click="$router.push('/penny-bots')"
        class="inline-flex items-center px-4 py-2 bg-primary text-white rounded-md hover:bg-primary/80 transition-colors"
      >
        <Icon icon="mdi:robot" class="mr-2" />
        {{ $t('Go to Bot Management') }}
      </button>
    </div>

    <!-- Rules List -->
    <div v-else-if="selectedBot">

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="p-3 bg-green-100 dark:bg-green-900 rounded-full">
            <Icon icon="mdi:lightning-bolt" class="h-6 w-6 text-green-600 dark:text-green-400" />
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-600 dark:text-gray-400">
              {{ $t('Active Rules') }}
            </p>
            <p class="text-2xl font-bold text-gray-900 dark:text-white">
              {{ activeRules.length }}
            </p>
          </div>
        </div>
      </div>

      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="p-3 bg-blue-100 dark:bg-blue-900 rounded-full">
            <Icon icon="mdi:brain" class="h-6 w-6 text-blue-600 dark:text-blue-400" />
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-600 dark:text-gray-400">
              {{ $t('AI Rules') }}
            </p>
            <p class="text-2xl font-bold text-gray-900 dark:text-white">
              {{ aiRules.length }}
            </p>
          </div>
        </div>
      </div>

      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="p-3 bg-purple-100 dark:bg-purple-900 rounded-full">
            <Icon icon="mdi:cog" class="h-6 w-6 text-purple-600 dark:text-purple-400" />
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-600 dark:text-gray-400">
              {{ $t('Total Rules') }}
            </p>
            <p class="text-2xl font-bold text-gray-900 dark:text-white">
              {{ rules.length }}
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loadingRules" class="loading-state">
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
        <div v-for="i in 6" :key="i" class="skeleton-card">
          <div class="animate-pulse">
            <div class="flex items-center space-x-4 mb-4">
              <div class="w-12 h-12 bg-gray-300 dark:bg-gray-600 rounded-full"></div>
              <div class="flex-1 space-y-2">
                <div class="h-4 bg-gray-300 dark:bg-gray-600 rounded w-3/4"></div>
                <div class="h-3 bg-gray-300 dark:bg-gray-600 rounded w-1/2"></div>
              </div>
            </div>
            <div class="space-y-2">
              <div class="h-3 bg-gray-300 dark:bg-gray-600 rounded"></div>
              <div class="h-3 bg-gray-300 dark:bg-gray-600 rounded w-5/6"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else-if="rules.length === 0" class="empty-state">
      <div class="text-center py-12">
        <Icon icon="mdi:lightning-bolt-outline" class="h-16 w-16 text-gray-400 mx-auto mb-4" />
        <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">
          {{ $t('No Rules Yet') }}
        </h3>
        <p class="text-gray-600 dark:text-gray-400 mb-4">
          {{ $t('Create your first custom rule to enhance your bot\'s capabilities') }}
        </p>
        <button
          @click="showCreateModal = true"
          class="inline-flex items-center px-4 py-2 bg-primary text-white rounded-md hover:bg-primary/80 transition-colors"
        >
          <Icon icon="mdi:plus" class="mr-2" />
          {{ $t('Create Your First Rule') }}
        </button>
      </div>
    </div>

    <!-- Rules Grid -->
    <div v-else class="rules-grid">
      <div
        v-for="rule in rules"
        :key="rule.id"
        class="bg-white dark:bg-gray-800 rounded-lg shadow-md border border-gray-200 dark:border-gray-700 hover:shadow-lg transition-shadow duration-200 p-6"
      >
        <div class="card-header">
          <div class="rule-avatar" :class="{ 'is-inactive': !rule.isActive }">
            <div class="avatar-content">
              <Icon :icon="getRuleTypeIcon(rule.type)" class="h-8 w-8" />
            </div>
          </div>
          <div class="header-main">
            <h3 class="rule-name" :title="rule.name">
              {{ rule.name }}
            </h3>
            <div class="flex items-center space-x-2">
              <span
                :class="[
                  'text-xs py-1 px-2 rounded-md',
                  rule.isActive
                    ? 'bg-green-600 text-white' 
                    : 'bg-red-600 text-white'
                ]"
              >
                {{ rule.isActive ? $t('Active') : $t('Inactive') }}
              </span>
              <span class="text-xs py-1 px-2 rounded-md bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-200">
                {{ getRuleTypeDisplayName(rule.type) }}
              </span>
            </div>
          </div>
        </div>

        <div class="card-content">
          <div class="info-item">
            <span class="label">{{ $t('Type') }}:</span>
            <span class="value">{{ getRuleTypeDisplayName(rule.type) }}</span>
          </div>
          <div class="info-item">
            <span class="label">{{ $t('Bot') }}:</span>
            <span class="value">{{ rule.botName }}</span>
          </div>
          <div v-if="rule.description" class="info-item">
            <span class="label">{{ $t('Description') }}:</span>
            <span class="value text-truncate">{{ rule.description }}</span>
          </div>
          <div class="info-item">
            <span class="label">{{ $t('Priority') }}:</span>
            <span class="value">{{ rule.priority }}</span>
          </div>
          <div class="info-item">
            <span class="label">{{ $t('Created') }}:</span>
            <span class="value">{{ formatDateTime(rule.createdAt) }}</span>
          </div>
        </div>

        <div class="card-footer">
          <div class="action-buttons">
            <div class="flex space-x-2">
              <button
                @click="toggleRuleStatus(rule)"
                :disabled="updatingRule"
                :class="[
                  'text-sm font-medium transition-colors',
                  rule.isActive
                    ? 'text-red-600 dark:text-red-400 hover:text-red-700 dark:hover:text-red-300'
                    : 'text-green-600 dark:text-green-400 hover:text-green-700 dark:hover:text-green-300'
                ]"
              >
                {{ rule.isActive ? $t('Disable') : $t('Enable') }}
              </button>
              <button
                @click="editRule(rule)"
                class="text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 text-sm font-medium"
              >
                {{ $t('Edit') }}
              </button>
              <button
                @click="testRule(rule)"
                :title="$t('Test this existing rule')"
                class="text-purple-600 dark:text-purple-400 hover:text-purple-700 dark:hover:text-purple-300 text-sm font-medium"
              >
                {{ $t('Test Rule') }}
              </button>
              <button
                @click="duplicateRule(rule)"
                class="text-orange-600 dark:text-orange-400 hover:text-orange-700 dark:hover:text-orange-300 text-sm font-medium"
              >
                {{ $t('Duplicate') }}
              </button>
              <button
                @click="deleteRule(rule)"
                :disabled="deletingRule"
                class="text-red-600 dark:text-red-400 hover:text-red-700 dark:hover:text-red-300 text-sm font-medium"
              >
                {{ $t('Delete') }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
    </div>

    <!-- Rule Modal -->
    <PennyRuleModal
      v-if="showCreateModal || editingRule"
      :rule="editingRule"
      :bot="selectedBot"
      @close="closeModal"
      @saved="onRuleSaved"
    />
  </div>
</template>

<script>
import { computed, ref, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import { useI18n } from 'vue-i18n'
import { formatDate, formatDateTime } from '@/utils/dateUtils'
import { usePennyRuleStore } from '@/stores/pennyRuleStore'
import { usePennyBotStore } from '@/stores/pennyBotStore'
import PennyRuleModal from './components/PennyRuleModal.vue'

export default {
  name: 'PennyRules',
  components: {
    Icon,
    PennyRuleModal
  },
  setup() {
    const { t } = useI18n()
    const pennyRuleStore = usePennyRuleStore()
    const pennyBotStore = usePennyBotStore()

    // Computed
    const rules = computed(() => pennyRuleStore.rules)
    const activeRules = computed(() => 
      rules.value.filter(rule => rule.isActive)
    )
    const aiRules = computed(() => 
      rules.value.filter(rule => rule.type === 'AI')
    )
    const loadingRules = computed(() => pennyRuleStore.loadingRules)
    const updatingRule = computed(() => pennyRuleStore.updatingRule)
    const deletingRule = computed(() => pennyRuleStore.deletingRule)
    const availableBots = computed(() => pennyBotStore.pennyBots)
    const currentBot = computed(() => pennyBotStore.currentBot)
    const currentBotId = computed(() => pennyBotStore.currentBotId)

    // Modal states
    const showCreateModal = ref(false)
    const editingRule = ref(null)
    const selectedBot = ref(null)

    // Methods
    const fetchRules = async () => {
      if (!selectedBot.value) {
        console.warn('No bot selected for fetching rules')
        return
      }
      
      try {
        await pennyRuleStore.fetchRules(selectedBot.value.id)
      } catch (error) {
        console.error('Failed to fetch rules:', error)
      }
    }

    const selectBot = (bot) => {
      selectedBot.value = bot
      fetchRules()
    }

    const toggleRuleStatus = async (rule) => {
      try {
        await pennyRuleStore.toggleRuleStatus(rule.id, !rule.isActive)
      } catch (error) {
        console.error('Failed to toggle rule status:', error)
      }
    }

    const editRule = (rule) => {
      editingRule.value = { ...rule }
    }

    const deleteRule = async (rule) => {
      if (confirm(`Are you sure you want to delete "${rule.name}"?`)) {
        try {
          await pennyRuleStore.deleteRule(rule.id)
        } catch (error) {
          console.error('Failed to delete rule:', error)
        }
      }
    }

    const testRule = async (rule) => {
      try {
        await pennyRuleStore.testRule(rule.id)
        alert('Rule test successful!')
      } catch (error) {
        console.error('Rule test failed:', error)
        alert('Rule test failed: ' + error.message)
      }
    }

    const duplicateRule = async (rule) => {
      try {
        const duplicatedRule = {
          ...rule,
          name: `${rule.name} (Copy)`,
          isActive: false
        }
        delete duplicatedRule.id
        delete duplicatedRule.createdAt
        delete duplicatedRule.updatedAt
        
        await pennyRuleStore.createRule(duplicatedRule)
        fetchRules()
      } catch (error) {
        console.error('Failed to duplicate rule:', error)
      }
    }

    const closeModal = () => {
      showCreateModal.value = false
      editingRule.value = null
    }

    const onRuleSaved = () => {
      closeModal()
      fetchRules()
    }

    const getRuleTypeIcon = (type) => {
      const icons = {
        'AI': 'mdi:brain',
        'KEYWORD': 'mdi:key',
        'INTENT': 'mdi:target-account',
        'TIME_BASED': 'mdi:clock',
        'CONDITION': 'mdi:logic',
        'WEBHOOK': 'mdi:webhook',
        'CUSTOM': 'mdi:cog'
      }
      return icons[type] || 'mdi:cog'
    }

    const getRuleTypeDisplayName = (type) => {
      const names = {
        'AI': 'AI Rule',
        'KEYWORD': 'Keyword',
        'INTENT': 'Intent',
        'TIME_BASED': 'Time Based',
        'CONDITION': 'Condition',
        'WEBHOOK': 'Webhook',
        'CUSTOM': 'Custom'
      }
      return names[type] || type
    }

    const getBotTypeDisplayName = (botType) => {
      const names = {
        'CUSTOMER_SERVICE': 'Customer Service',
        'SALES': 'Sales',
        'SUPPORT': 'Support',
        'MARKETING': 'Marketing',
        'HR': 'HR',
        'FINANCE': 'Finance',
        'GENERAL': 'General'
      }
      return names[botType] || botType
    }

    // Lifecycle
    onMounted(() => {
      // Load available bots first
      if (availableBots.value.length === 0) {
        pennyBotStore.fetchPennyBots().then(() => {
          // Auto-select current bot if available
          if (currentBot.value) {
            selectedBot.value = currentBot.value
            fetchRules()
          }
        })
      } else {
        // Auto-select current bot if available
        if (currentBot.value) {
          selectedBot.value = currentBot.value
          fetchRules()
        }
      }
    })

    return {
      // Data
      rules,
      activeRules,
      aiRules,
      loadingRules,
      updatingRule,
      deletingRule,
      showCreateModal,
      editingRule,
      selectedBot,
      availableBots,
      currentBot,
      currentBotId,

      // Methods
      fetchRules,
      selectBot,
      toggleRuleStatus,
      editRule,
      deleteRule,
      testRule,
      duplicateRule,
      closeModal,
      onRuleSaved,
      getRuleTypeIcon,
      getRuleTypeDisplayName,
      formatDate,
      formatDateTime,
      getBotTypeDisplayName
    }
  }
}
</script>

<style scoped>
.penny-rules {
  width: 100%;
  padding: 20px;
}

.rules-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  margin-top: 10px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}

.rule-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  background: #3b82f6;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.rule-avatar.is-inactive {
  filter: grayscale(1);
  opacity: 0.6;
}

.avatar-content {
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-main {
  flex: 1;
  overflow: hidden;
}

.rule-name {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dark .rule-name {
  color: white;
}

.card-content {
  background-color: #f9fafb;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 20px;
}

.dark .card-content {
  background-color: #374151;
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  font-size: 13px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.label {
  color: #6b7280;
  font-weight: 500;
}

.dark .label {
  color: #9ca3af;
}

.value {
  color: #374151;
  font-weight: 500;
}

.dark .value {
  color: #d1d5db;
}

.text-truncate {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-footer {
  border-top: 1px solid #f3f4f6;
  padding-top: 15px;
}

.dark .card-footer {
  border-top-color: #374151;
}

.action-buttons {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.skeleton-card {
  background: white;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 20px;
}

.dark .skeleton-card {
  background: #1f2937;
  border-color: #374151;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
}

.loading-state {
  margin-top: 20px;
}
</style>
