<template>
  <div class="json-editor-container">
    <div ref="editorContainer" class="json-editor"></div>
    <div v-if="error" class="json-error">
      <Icon icon="mdi:alert-circle" class="h-4 w-4 mr-1" />
      {{ error }}
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { EditorView, basicSetup } from 'codemirror'
import { javascript } from '@codemirror/lang-javascript'
import { EditorState } from '@codemirror/state'
import { Icon } from '@iconify/vue'

export default {
  name: 'JsonEditor',
  components: {
    Icon
  },
  props: {
    modelValue: {
      type: String,
      default: ''
    },
    placeholder: {
      type: String,
      default: ''
    },
    readonly: {
      type: Boolean,
      default: false
    }
  },
  emits: ['update:modelValue', 'valid', 'invalid'],
  setup(props, { emit }) {
    const { t } = useI18n()
    const editorContainer = ref(null)
    const editorView = ref(null)
    const error = ref('')

    const validateJson = (jsonString) => {
      if (!jsonString || jsonString.trim() === '') {
        error.value = ''
        emit('valid')
        return true
      }
      try {
        JSON.parse(jsonString)
        error.value = ''
        emit('valid')
        return true
      } catch (e) {
        error.value = t('penny.rules.jsonEditor.invalidJson') + ': ' + e.message
        emit('invalid')
        return false
      }
    }

    const createEditor = () => {
      if (!editorContainer.value) return

      const startState = EditorState.create({
        doc: props.modelValue,
        extensions: [
          basicSetup,
          javascript(),
          EditorView.theme({
            '&': {
              fontSize: '14px',
              fontFamily: 'Monaco, Menlo, "Ubuntu Mono", monospace'
            },
            '.cm-content': {
              minHeight: '120px',
              padding: '10px'
            },
            '.cm-editor': {
              border: '1px solid #d1d5db',
              borderRadius: '8px'
            },
            '.cm-focused': {
              outline: 'none',
              borderColor: '#3b82f6'
            },
            '.dark .cm-editor': {
              borderColor: '#4b5563',
              background: '#374151'
            },
            '.dark .cm-content': {
              background: '#374151',
              color: 'white'
            }
          }),
          EditorView.updateListener.of((update) => {
            if (update.docChanged) {
              const newValue = update.state.doc.toString()
              validateJson(newValue)
              emit('update:modelValue', newValue)
            }
          }),
          EditorState.readOnly.of(props.readonly)
        ]
      })

      editorView.value = new EditorView({
        state: startState,
        parent: editorContainer.value
      })

      // Validate initial value
      validateJson(props.modelValue)
    }

    const updateEditorContent = (newValue) => {
      if (editorView.value) {
        const transaction = editorView.value.state.update({
          changes: {
            from: 0,
            to: editorView.value.state.doc.length,
            insert: newValue
          }
        })
        editorView.value.dispatch(transaction)
        validateJson(newValue)
      }
    }

    watch(() => props.modelValue, (newValue) => {
      if (editorView.value && editorView.value.state.doc.toString() !== newValue) {
        updateEditorContent(newValue)
      }
    })

    onMounted(() => {
      createEditor()
    })

    onBeforeUnmount(() => {
      if (editorView.value) {
        editorView.value.destroy()
      }
    })

    return {
      editorContainer,
      error
    }
  }
}
</script>

<style scoped>
.json-editor-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.json-editor {
  width: 100%;
}

.json-error {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #ef4444;
  background: #fef2f2;
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #fecaca;
}

.dark .json-error {
  background: #7f1d1d;
  border-color: #991b1b;
  color: #fecaca;
}
</style>
