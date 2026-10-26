<template>
  <div class="rule-templates-container">
    <div class="templates-header">
      <label class="form-label">{{ $t('penny.rules.templates.title') }}</label>
      <div class="template-categories">
        <button
          v-for="category in categories"
          :key="category.id"
          @click="selectedCategory = category.id"
          :class="['category-btn', { active: selectedCategory === category.id }]"
        >
          <Icon :icon="category.icon" class="h-4 w-4 mr-1" />
          {{ $t(category.nameKey) }}
        </button>
      </div>
    </div>

    <div class="templates-grid">
      <div
        v-for="template in filteredTemplates"
        :key="template.id"
        class="template-card"
        @click="applyTemplate(template)"
      >
        <div class="template-icon">
          <Icon :icon="template.icon" class="h-6 w-6" />
        </div>
        <div class="template-content">
          <h4 class="template-name">{{ template.name }}</h4>
          <p class="template-description">{{ template.description }}</p>
          <div class="template-tags">
            <span
              v-for="tag in template.tags"
              :key="tag"
              class="template-tag"
            >
              {{ tag }}
            </span>
          </div>
        </div>
        <div class="template-action">
          <Icon icon="mdi:arrow-right" class="h-5 w-5" />
        </div>
      </div>
    </div>

    <div v-if="filteredTemplates.length === 0" class="no-templates">
      <Icon icon="mdi:information" class="h-8 w-8 mr-2" />
      {{ $t('penny.rules.templates.noTemplates') }}
    </div>
  </div>
</template>

<script>
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { Icon } from '@iconify/vue'
import { useToast } from '@/composables/useToast'
import { RuleType, RuleTriggerType } from '@/types/penny'

export default {
  name: 'RuleTemplates',
  components: {
    Icon
  },
  props: {
    botId: {
      type: String,
      required: true
    }
  },
  emits: ['apply'],
  setup(props, { emit }) {
    const { t } = useI18n()
    const toast = useToast()
    const selectedCategory = ref('all')

    const categories = [
      { id: 'all', nameKey: 'penny.rules.templates.categories.all', icon: 'mdi:apps' },
      { id: 'greeting', nameKey: 'penny.rules.templates.categories.greeting', icon: 'mdi:hand-wave' },
      { id: 'support', nameKey: 'penny.rules.templates.categories.support', icon: 'mdi:headset' },
      { id: 'sales', nameKey: 'penny.rules.templates.categories.sales', icon: 'mdi:cart' },
      { id: 'faq', nameKey: 'penny.rules.templates.categories.faq', icon: 'mdi:help-circle' },
      { id: 'advanced', nameKey: 'penny.rules.templates.categories.advanced', icon: 'mdi:cogs' }
    ]

    const templates = [
      // Greeting Templates
      {
        id: 'greeting-1',
        category: 'greeting',
        name: 'Simple Greeting',
        description: 'Responds to common greeting messages',
        icon: 'mdi:hand-wave',
        tags: ['Intent', 'Response'],
        data: {
          ruleName: 'Simple Greeting',
          description: 'Responds to hello, hi, hey messages',
          triggerType: RuleTriggerType.INTENT,
          triggerValue: 'greeting',
          ruleType: RuleType.RESPONSE,
          action: 'Hello! How can I help you today?',
          priority: 90
        }
      },
      {
        id: 'greeting-2',
        category: 'greeting',
        name: 'Time-based Greeting',
        description: 'Different greetings based on time of day',
        icon: 'mdi:clock',
        tags: ['Condition', 'Response'],
        data: {
          ruleName: 'Time-based Greeting',
          description: 'Good morning/afternoon/evening based on time',
          triggerType: RuleTriggerType.CONDITION,
          condition: 'time >= "06:00" && time < "12:00"',
          ruleType: RuleType.RESPONSE,
          action: 'Good morning! How can I assist you?',
          priority: 85
        }
      },
      // Support Templates
      {
        id: 'support-1',
        category: 'support',
        name: 'Human Handoff',
        description: 'Escalate to human agent on request',
        icon: 'mdi:human-handsup',
        tags: ['Keyword', 'Redirect'],
        data: {
          ruleName: 'Human Handoff',
          description: 'Transfer to human when user requests',
          triggerType: RuleTriggerType.KEYWORD,
          triggerValue: 'human|agent|support|help',
          ruleType: RuleType.REDIRECT,
          action: 'human_agent_flow',
          priority: 95
        }
      },
      {
        id: 'support-2',
        category: 'support',
        name: 'FAQ Handler',
        description: 'Handle frequently asked questions',
        icon: 'mdi:help-circle',
        tags: ['Intent', 'Response'],
        data: {
          ruleName: 'FAQ Handler',
          description: 'Answers common questions',
          triggerType: RuleTriggerType.INTENT,
          triggerValue: 'faq',
          ruleType: RuleType.RESPONSE,
          action: 'I can help with that. Please specify your question.',
          priority: 70
        }
      },
      // Sales Templates
      {
        id: 'sales-1',
        category: 'sales',
        name: 'Product Inquiry',
        description: 'Handle product-related questions',
        icon: 'mdi:package-variant',
        tags: ['Keyword', 'Webhook'],
        data: {
          ruleName: 'Product Inquiry',
          description: 'Fetch product information via webhook',
          triggerType: RuleTriggerType.KEYWORD,
          triggerValue: 'product|price|cost',
          ruleType: RuleType.WEBHOOK,
          action: JSON.stringify({
            url: 'https://api.example.com/products',
            method: 'POST',
            headers: {
              'Content-Type': 'application/json'
            },
            body: {
              query: '{{message}}'
            }
          }),
          priority: 75
        }
      },
      {
        id: 'sales-2',
        category: 'sales',
        name: 'Lead Capture',
        description: 'Capture user information for leads',
        icon: 'mdi:clipboard-account',
        tags: ['Regex', 'Webhook'],
        data: {
          ruleName: 'Lead Capture',
          description: 'Capture email/phone from messages',
          triggerType: RuleTriggerType.REGEX,
          triggerValue: '[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}',
          ruleType: RuleType.WEBHOOK,
          action: JSON.stringify({
            url: 'https://api.example.com/leads',
            method: 'POST',
            headers: {
              'Content-Type': 'application/json'
            },
            body: {
              email: '{{match}}',
              userId: '{{userId}}'
            }
          }),
          priority: 80
        }
      },
      // FAQ Templates
      {
        id: 'faq-1',
        category: 'faq',
        name: 'Contact Info',
        description: 'Provide contact information',
        icon: 'mdi:contact-mail',
        tags: ['Keyword', 'Response'],
        data: {
          ruleName: 'Contact Info',
          description: 'Share contact details when requested',
          triggerType: RuleTriggerType.KEYWORD,
          triggerValue: 'contact|phone|email|address',
          ruleType: RuleType.RESPONSE,
          action: 'You can reach us at:\n📧 Email: support@example.com\n📞 Phone: +1-234-567-8900\n📍 Address: 123 Main St, City',
          priority: 65
        }
      },
      {
        id: 'faq-2',
        category: 'faq',
        name: 'Business Hours',
        description: 'Inform about business hours',
        icon: 'mdi:clock-outline',
        tags: ['Keyword', 'Response'],
        data: {
          ruleName: 'Business Hours',
          description: 'Share operating hours',
          triggerType: RuleTriggerType.KEYWORD,
          triggerValue: 'hours|open|close|time',
          ruleType: RuleType.RESPONSE,
          action: 'Our business hours:\n🗓️ Monday - Friday: 9:00 AM - 6:00 PM\n🗓️ Saturday: 10:00 AM - 4:00 PM\n🗓️ Sunday: Closed',
          priority: 65
        }
      },
      // Advanced Templates
      {
        id: 'advanced-1',
        category: 'advanced',
        name: 'Sentiment Analysis',
        description: 'Route based on message sentiment',
        icon: 'mdi:emotion-happy',
        tags: ['Script', 'Redirect'],
        data: {
          ruleName: 'Sentiment Analysis',
          description: 'Analyze sentiment and route accordingly',
          triggerType: RuleTriggerType.SCRIPT,
          triggerValue: 'sentimentAnalysis',
          ruleType: RuleType.SCRIPT,
          action: 'const sentiment = analyzeSentiment(message);\nif (sentiment < 0.3) {\n  return "escalate_flow";\n} else if (sentiment > 0.7) {\n  return "positive_flow";\n}\nreturn "default_flow";',
          priority: 60
        }
      },
      {
        id: 'advanced-2',
        category: 'advanced',
        name: 'Multi-language',
        description: 'Detect language and respond accordingly',
        icon: 'mdi:translate',
        tags: ['Condition', 'Response'],
        data: {
          ruleName: 'Multi-language',
          description: 'Detect user language and respond',
          triggerType: RuleTriggerType.CONDITION,
          condition: 'language == "vi"',
          ruleType: RuleType.RESPONSE,
          action: 'Xin chào! Tôi có thể giúp gì cho bạn?',
          priority: 70
        }
      }
    ]

    const filteredTemplates = computed(() => {
      if (selectedCategory.value === 'all') {
        return templates
      }
      return templates.filter(t => t.category === selectedCategory.value)
    })

    const applyTemplate = (template) => {
      emit('apply', template.data)
      toast.success(t('penny.rules.templates.applied'))
    }

    return {
      categories,
      selectedCategory,
      filteredTemplates,
      applyTemplate
    }
  }
}
</script>

<style scoped>
.rule-templates-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.dark .rule-templates-container {
  background: #1f2937;
  border-color: #374151;
}

.templates-header {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.form-label {
  font-size: 13px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 6px;
  display: block;
}

.dark .form-label {
  color: #d1d5db;
}

.template-categories {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.category-btn {
  padding: 6px 12px;
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 12px;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
}

.dark .category-btn {
  background: #374151;
  border-color: #4b5563;
  color: #d1d5db;
}

.category-btn:hover {
  background: #f3f4f6;
}

.dark .category-btn:hover {
  background: #4b5563;
}

.category-btn.active {
  background: #3b82f6;
  border-color: #3b82f6;
  color: white;
}

.templates-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
}

.template-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.dark .template-card {
  background: #374151;
  border-color: #4b5563;
}

.template-card:hover {
  border-color: #3b82f6;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.1);
  transform: translateY(-2px);
}

.template-icon {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #eff6ff;
  border-radius: 8px;
  color: #3b82f6;
}

.dark .template-icon {
  background: #1e3a8a;
}

.template-content {
  flex: 1;
  min-width: 0;
}

.template-name {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.dark .template-name {
  color: white;
}

.template-description {
  margin: 0 0 8px 0;
  font-size: 12px;
  color: #6b7280;
  line-height: 1.4;
}

.dark .template-description {
  color: #9ca3af;
}

.template-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.template-tag {
  padding: 2px 6px;
  background: #f3f4f6;
  border-radius: 4px;
  font-size: 10px;
  color: #6b7280;
  font-weight: 500;
}

.dark .template-tag {
  background: #4b5563;
  color: #d1d5db;
}

.template-action {
  flex-shrink: 0;
  color: #9ca3af;
  transition: all 0.2s;
}

.template-card:hover .template-action {
  color: #3b82f6;
}

.no-templates {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  color: #6b7280;
  font-size: 13px;
}

.dark .no-templates {
  color: #9ca3af;
}
</style>
