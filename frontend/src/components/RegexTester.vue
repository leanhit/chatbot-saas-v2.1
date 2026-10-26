<template>
  <div class="regex-tester-container">
    <div class="regex-input-section">
      <label class="form-label">Regex Pattern</label>
      <input
        v-model="pattern"
        type="text"
        class="form-input"
        placeholder="Enter regex pattern (e.g., ^hello.*$)"
        @input="testRegex"
      />
      <div v-if="regexError" class="regex-error">
        <Icon icon="mdi:alert-circle" class="h-4 w-4 mr-1" />
        {{ regexError }}
      </div>
    </div>

    <div class="regex-test-section">
      <label class="form-label">Test Text</label>
      <textarea
        v-model="testText"
        class="form-textarea"
        rows="3"
        placeholder="Enter text to test against the pattern"
        @input="testRegex"
      ></textarea>
    </div>

    <div v-if="matches.length > 0" class="regex-results">
      <label class="form-label">Matches ({{ matches.length }})</label>
      <div class="matches-list">
        <div
          v-for="(match, index) in matches"
          :key="index"
          class="match-item"
        >
          <span class="match-index">{{ index + 1 }}.</span>
          <span class="match-text">{{ match }}</span>
          <button @click="copyMatch(match)" class="copy-btn" title="Copy">
            <Icon icon="mdi:content-copy" class="h-4 w-4" />
          </button>
        </div>
      </div>
    </div>

    <div v-if="pattern && !regexError && matches.length === 0" class="no-matches">
      <Icon icon="mdi:information" class="h-4 w-4 mr-1" />
      No matches found
    </div>

    <div class="regex-examples">
      <label class="form-label">Common Patterns</label>
      <div class="examples-grid">
        <button
          v-for="example in commonPatterns"
          :key="example.pattern"
          @click="applyPattern(example.pattern)"
          class="example-btn"
          :title="example.description"
        >
          {{ example.label }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed } from 'vue'
import { Icon } from '@iconify/vue'
import { useToast } from '@/composables/useToast'

export default {
  name: 'RegexTester',
  components: {
    Icon
  },
  props: {
    modelValue: {
      type: String,
      default: ''
    }
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    const toast = useToast()
    const pattern = ref(props.modelValue)
    const testText = ref('hello world\nhello there\nhi there')
    const matches = ref([])
    const regexError = ref('')

    const commonPatterns = [
      {
        label: 'Start with',
        pattern: '^hello',
        description: 'Matches strings starting with "hello"'
      },
      {
        label: 'End with',
        pattern: 'world$',
        description: 'Matches strings ending with "world"'
      },
      {
        label: 'Contains',
        pattern: 'hello',
        description: 'Matches strings containing "hello"'
      },
      {
        label: 'Email',
        pattern: '[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}',
        description: 'Matches email addresses'
      },
      {
        label: 'Phone',
        pattern: '\\+?[0-9]{10,15}',
        description: 'Matches phone numbers'
      },
      {
        label: 'Any word',
        pattern: '\\w+',
        description: 'Matches any word'
      },
      {
        label: 'Case insensitive',
        pattern: '(?i)hello',
        description: 'Matches "hello" case-insensitive'
      },
      {
        label: 'Multiple words',
        pattern: '(hello|hi|hey)',
        description: 'Matches any of the words'
      }
    ]

    const testRegex = () => {
      if (!pattern.value) {
        matches.value = []
        regexError.value = ''
        return
      }

      try {
        const regex = new RegExp(pattern.value, 'g')
        const text = testText.value
        const foundMatches = []
        let match

        while ((match = regex.exec(text)) !== null) {
          foundMatches.push(match[0])
          // Prevent infinite loop for zero-length matches
          if (match[0].length === 0) {
            regex.lastIndex++
          }
        }

        matches.value = foundMatches
        regexError.value = ''
      } catch (e) {
        regexError.value = 'Invalid regex: ' + e.message
        matches.value = []
      }

      emit('update:modelValue', pattern.value)
    }

    const applyPattern = (newPattern) => {
      pattern.value = newPattern
      testRegex()
    }

    const copyMatch = (match) => {
      navigator.clipboard.writeText(match).then(() => {
        toast.success('Match copied to clipboard')
      }).catch(() => {
        toast.error('Failed to copy')
      })
    }

    return {
      pattern,
      testText,
      matches,
      regexError,
      commonPatterns,
      testRegex,
      applyPattern,
      copyMatch
    }
  }
}
</script>

<style scoped>
.regex-tester-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.dark .regex-tester-container {
  background: #1f2937;
  border-color: #374151;
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

.form-input,
.form-textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 13px;
  background: white;
  color: #1f2937;
  transition: all 0.2s;
}

.dark .form-input,
.dark .form-textarea {
  background: #374151;
  border-color: #4b5563;
  color: white;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.regex-error {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #ef4444;
  background: #fef2f2;
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #fecaca;
  margin-top: 6px;
}

.dark .regex-error {
  background: #7f1d1d;
  border-color: #991b1b;
  color: #fecaca;
}

.regex-results {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.matches-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-height: 150px;
  overflow-y: auto;
}

.match-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-size: 13px;
}

.dark .match-item {
  background: #374151;
  border-color: #4b5563;
}

.match-index {
  font-weight: 600;
  color: #6b7280;
  min-width: 24px;
}

.dark .match-index {
  color: #9ca3af;
}

.match-text {
  flex: 1;
  color: #059669;
  font-family: 'Monaco', 'Menlo', monospace;
  word-break: break-all;
}

.copy-btn {
  background: none;
  border: none;
  padding: 4px;
  border-radius: 4px;
  cursor: pointer;
  color: #6b7280;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.copy-btn:hover {
  background: #f3f4f6;
  color: #3b82f6;
}

.dark .copy-btn {
  color: #9ca3af;
}

.dark .copy-btn:hover {
  background: #4b5563;
  color: white;
}

.no-matches {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #6b7280;
  background: #f3f4f6;
  padding: 8px 12px;
  border-radius: 6px;
}

.dark .no-matches {
  background: #374151;
  color: #9ca3af;
}

.regex-examples {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.examples-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 8px;
}

.example-btn {
  padding: 8px 12px;
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 12px;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s;
  text-align: center;
}

.dark .example-btn {
  background: #374151;
  border-color: #4b5563;
  color: #d1d5db;
}

.example-btn:hover {
  background: #3b82f6;
  border-color: #3b82f6;
  color: white;
}
</style>
