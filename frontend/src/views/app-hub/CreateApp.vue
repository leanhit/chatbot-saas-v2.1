<template>
  <div class="create-app-wizard">
    <div class="wizard-header">
      <div class="header-content">
        <h1>Create New Application</h1>
        <p class="header-description">Register a new application in the App Hub</p>
      </div>
      <div class="progress-bar">
        <div class="step" :class="{ active: currentStep >= 1, completed: currentStep > 1 }">
          <div class="step-number">1</div>
          <div class="step-label">Basic Info</div>
        </div>
        <div class="step" :class="{ active: currentStep >= 2, completed: currentStep > 2 }">
          <div class="step-number">2</div>
          <div class="step-label">Configuration</div>
        </div>
        <div class="step" :class="{ active: currentStep >= 3, completed: currentStep > 3 }">
          <div class="step-number">3</div>
          <div class="step-label">Review</div>
        </div>
      </div>
    </div>
    
    <div class="wizard-content">
      <!-- Step 1: Basic Information -->
      <div v-if="currentStep === 1" class="step-content">
        <div class="step-header">
          <h2>Basic Information</h2>
          <p>Provide the basic details about your application</p>
        </div>
        
        <form @submit.prevent="validateAndNext" class="app-form">
          <div class="form-grid">
            <div class="form-group">
              <label for="name">App Name *</label>
              <input 
                id="name"
                v-model="app.name" 
                type="text" 
                required
                placeholder="my-awesome-app"
                @blur="validateName"
              />
              <span class="form-help">Unique identifier for your app (lowercase, hyphens only)</span>
              <span v-if="errors.name" class="error-message">{{ errors.name }}</span>
            </div>
            
            <div class="form-group">
              <label for="displayName">Display Name *</label>
              <input 
                id="displayName"
                v-model="app.displayName" 
                type="text" 
                required
                placeholder="My Awesome App"
              />
              <span class="form-help">Human-readable name for your app</span>
              <span v-if="errors.displayName" class="error-message">{{ errors.displayName }}</span>
            </div>
            
            <div class="form-group full-width">
              <label for="description">Description</label>
              <textarea 
                id="description"
                v-model="app.description" 
                rows="3"
                placeholder="Describe what your application does..."
              ></textarea>
              <span class="form-help">Brief description of your app's functionality</span>
            </div>
            
            <div class="form-group">
              <label for="appType">App Type *</label>
              <select id="appType" v-model="app.appType" required @change="validateAppType">
                <option value="">Select Type</option>
                <option value="CHATBOT">Chatbot</option>
                <option value="INTEGRATION">Integration</option>
                <option value="ANALYTICS">Analytics</option>
                <option value="AUTOMATION">Automation</option>
                <option value="API">API Service</option>
                <option value="WEBHOOK">Webhook Service</option>
              </select>
              <span class="form-help">Category of your application</span>
              <span v-if="errors.appType" class="error-message">{{ errors.appType }}</span>
            </div>
            
            <div class="form-group">
              <label for="version">Version</label>
              <input 
                id="version"
                v-model="app.version" 
                type="text" 
                placeholder="1.0.0"
              />
              <span class="form-help">Semantic version (e.g., 1.0.0)</span>
            </div>
            
            <div class="form-group">
              <label class="checkbox-label">
                <input type="checkbox" v-model="app.isPublic" />
                <span>Make this app public</span>
              </label>
              <span class="form-help">Public apps can be discovered and subscribed by other tenants</span>
            </div>
          </div>
        </form>
      </div>
      
      <!-- Step 2: Configuration -->
      <div v-if="currentStep === 2" class="step-content">
        <div class="step-header">
          <h2>Configuration</h2>
          <p>Set up the technical configuration for your application</p>
        </div>
        
        <form @submit.prevent="validateAndNext" class="app-form">
          <div class="form-grid">
            <div class="form-group full-width">
              <label for="apiEndpoint">API Endpoint</label>
              <input 
                id="apiEndpoint"
                v-model="app.apiEndpoint" 
                type="url" 
                placeholder="https://api.example.com"
              />
              <span class="form-help">Base URL for your app's API</span>
            </div>
            
            <div class="form-group full-width">
              <label for="webhookUrl">Webhook URL</label>
              <input 
                id="webhookUrl"
                v-model="app.webhookUrl" 
                type="url" 
                placeholder="https://webhook.example.com"
              />
              <span class="form-help">URL to receive webhook notifications</span>
            </div>
            
            <div class="form-group full-width">
              <label for="configSchema">Configuration Schema (JSON)</label>
              <div class="json-editor">
                <textarea 
                  id="configSchema"
                  v-model="app.configSchema" 
                  rows="12"
                  placeholder='{
  "type": "object",
  "properties": {
    "apiKey": {
      "type": "string",
      "description": "API key for authentication"
    },
    "webhookSecret": {
      "type": "string",
      "description": "Secret for webhook validation"
    }
  },
  "required": ["apiKey"]
}'
                  @blur="validateConfigSchema"
                ></textarea>
                <div class="json-actions">
                  <button type="button" @click="formatJson('configSchema')" class="btn btn-sm btn-outline">
                    <Icon icon="mdi:format-align-left" />
                    Format
                  </button>
                  <button type="button" @click="validateJson('configSchema')" class="btn btn-sm btn-outline">
                    <Icon icon="mdi:check" />
                    Validate
                  </button>
                </div>
              </div>
              <span class="form-help">JSON schema for app configuration (optional)</span>
              <span v-if="errors.configSchema" class="error-message">{{ errors.configSchema }}</span>
            </div>
            
            <div class="form-group full-width">
              <label for="defaultConfig">Default Configuration (JSON)</label>
              <div class="json-editor">
                <textarea 
                  id="defaultConfig"
                  v-model="app.defaultConfig" 
                  rows="8"
                  placeholder='{
  "apiKey": "",
  "webhookSecret": "",
  "enableLogging": true,
  "timeout": 30000
}'
                  @blur="validateDefaultConfig"
                ></textarea>
                <div class="json-actions">
                  <button type="button" @click="formatJson('defaultConfig')" class="btn btn-sm btn-outline">
                    <Icon icon="mdi:format-align-left" />
                    Format
                  </button>
                  <button type="button" @click="validateJson('defaultConfig')" class="btn btn-sm btn-outline">
                    <Icon icon="mdi:check" />
                    Validate
                  </button>
                </div>
              </div>
              <span class="form-help">Default values for app configuration (optional)</span>
              <span v-if="errors.defaultConfig" class="error-message">{{ errors.defaultConfig }}</span>
            </div>
          </div>
        </form>
      </div>
      
      <!-- Step 3: Review -->
      <div v-if="currentStep === 3" class="step-content">
        <div class="step-header">
          <h2>Review & Create</h2>
          <p>Review your application details before creating</p>
        </div>
        
        <div class="review-section">
          <div class="review-card">
            <h3>Application Details</h3>
            <div class="review-grid">
              <div class="review-item">
                <label>Name:</label>
                <span>{{ app.name }}</span>
              </div>
              <div class="review-item">
                <label>Display Name:</label>
                <span>{{ app.displayName }}</span>
              </div>
              <div class="review-item">
                <label>Type:</label>
                <span class="app-type" :class="getTypeClass(app.appType)">{{ app.appType }}</span>
              </div>
              <div class="review-item">
                <label>Version:</label>
                <span>{{ app.version || '1.0.0' }}</span>
              </div>
              <div class="review-item">
                <label>Visibility:</label>
                <span class="visibility" :class="{ 'public': app.isPublic }">
                  {{ app.isPublic ? 'Public' : 'Private' }}
                </span>
              </div>
              <div class="review-item full-width">
                <label>Description:</label>
                <span>{{ app.description || 'No description provided' }}</span>
              </div>
            </div>
          </div>
          
          <div class="review-card" v-if="app.apiEndpoint || app.webhookUrl">
            <h3>Endpoints</h3>
            <div class="review-grid">
              <div class="review-item full-width" v-if="app.apiEndpoint">
                <label>API Endpoint:</label>
                <span class="url">{{ app.apiEndpoint }}</span>
              </div>
              <div class="review-item full-width" v-if="app.webhookUrl">
                <label>Webhook URL:</label>
                <span class="url">{{ app.webhookUrl }}</span>
              </div>
            </div>
          </div>
          
          <div class="review-card" v-if="app.configSchema || app.defaultConfig">
            <h3>Configuration</h3>
            <div class="config-preview">
              <div v-if="app.configSchema" class="config-section">
                <h4>Schema</h4>
                <pre>{{ formatJsonDisplay(app.configSchema) }}</pre>
              </div>
              <div v-if="app.defaultConfig" class="config-section">
                <h4>Default Values</h4>
                <pre>{{ formatJsonDisplay(app.defaultConfig) }}</pre>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="wizard-actions">
      <button @click="prevStep" v-if="currentStep > 1" class="btn btn-outline">
        <Icon icon="mdi:chevron-left" />
        Previous
      </button>
      
      <div class="action-spacer"></div>
      
      <button @click="cancel" class="btn btn-outline">
        <Icon icon="mdi:close" />
        Cancel
      </button>
      
      <button @click="validateAndNext" v-if="currentStep < 3" class="btn btn-primary">
        Next
        <Icon icon="mdi:chevron-right" />
      </button>
      
      <button @click="createApp" v-if="currentStep === 3" class="btn btn-primary" :disabled="submitting">
        <Icon icon="mdi:check" v-if="!submitting" />
        <Icon icon="mdi:loading" class="spin" v-if="submitting" />
        {{ submitting ? 'Creating...' : 'Create App' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAppStore } from '@/stores/appStore'

const router = useRouter()
const appStore = useAppStore()

const currentStep = ref(1)
const submitting = ref(false)

const app = reactive({
  name: '',
  displayName: '',
  description: '',
  appType: '',
  version: '1.0.0',
  apiEndpoint: '',
  webhookUrl: '',
  configSchema: '',
  defaultConfig: '',
  isPublic: false
})

const errors = reactive({})

const validateName = () => {
  if (!app.name.trim()) {
    errors.name = 'App name is required'
  } else if (!/^[a-z0-9-]+$/.test(app.name)) {
    errors.name = 'App name must contain only lowercase letters, numbers, and hyphens'
  } else {
    delete errors.name
  }
}

const validateDisplayName = () => {
  if (!app.displayName.trim()) {
    errors.displayName = 'Display name is required'
  } else {
    delete errors.displayName
  }
}

const validateAppType = () => {
  if (!app.appType) {
    errors.appType = 'App type is required'
  } else {
    delete errors.appType
  }
}

const validateConfigSchema = () => {
  if (app.configSchema && app.configSchema.trim()) {
    try {
      JSON.parse(app.configSchema)
      delete errors.configSchema
    } catch (e) {
      errors.configSchema = 'Invalid JSON format'
    }
  } else {
    delete errors.configSchema
  }
}

const validateDefaultConfig = () => {
  if (app.defaultConfig && app.defaultConfig.trim()) {
    try {
      JSON.parse(app.defaultConfig)
      delete errors.defaultConfig
    } catch (e) {
      errors.defaultConfig = 'Invalid JSON format'
    }
  } else {
    delete errors.defaultConfig
  }
}

const validateJson = (field) => {
  if (field === 'configSchema') {
    validateConfigSchema()
  } else if (field === 'defaultConfig') {
    validateDefaultConfig()
  }
}

const formatJson = (field) => {
  try {
    const parsed = JSON.parse(app[field])
    app[field] = JSON.stringify(parsed, null, 2)
    delete errors[field]
  } catch (e) {
    // Keep current value if invalid
  }
}

const formatJsonDisplay = (jsonString) => {
  try {
    return JSON.stringify(JSON.parse(jsonString), null, 2)
  } catch (e) {
    return jsonString
  }
}

const validateAndNext = () => {
  // Validate current step
  if (currentStep.value === 1) {
    validateName()
    validateDisplayName()
    validateAppType()
    
    if (Object.keys(errors).length > 0) {
      return
    }
  } else if (currentStep.value === 2) {
    validateConfigSchema()
    validateDefaultConfig()
    
    if (Object.keys(errors).length > 0) {
      return
    }
  }
  
  // Move to next step
  currentStep.value++
}

const prevStep = () => {
  if (currentStep.value > 1) {
    currentStep.value--
  }
}

const cancel = () => {
  if (confirm('Are you sure you want to cancel? All unsaved changes will be lost.')) {
    router.push('/app-hub/apps')
  }
}

const createApp = async () => {
  submitting.value = true
  
  try {
    const appData = {
      ...app,
      configSchema: app.configSchema || null,
      defaultConfig: app.defaultConfig || null
    }
    
    const response = await appStore.createApp(appData)
    
    // Show success message and redirect
    alert('Application created successfully!')
    router.push(`/app-hub/apps/${response.id}`)
  } catch (error) {
    alert('Failed to create application: ' + (error.response?.data?.message || error.message))
  } finally {
    submitting.value = false
  }
}

const getTypeClass = (type) => {
  return {
    'type-chatbot': type === 'CHATBOT',
    'type-integration': type === 'INTEGRATION',
    'type-analytics': type === 'ANALYTICS',
    'type-automation': type === 'AUTOMATION',
    'type-api': type === 'API',
    'type-webhook': type === 'WEBHOOK'
  }
}
</script>

<style scoped>
.create-app-wizard {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
}

.wizard-header {
  margin-bottom: 32px;
}

.header-content h1 {
  font-size: 2rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.header-description {
  color: #6b7280;
  margin: 0 0 24px 0;
}

.progress-bar {
  display: flex;
  justify-content: space-between;
  position: relative;
}

.progress-bar::before {
  content: '';
  position: absolute;
  top: 20px;
  left: 0;
  right: 0;
  height: 2px;
  background: #e5e7eb;
  z-index: 0;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 1;
}

.step-number {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #e5e7eb;
  color: #6b7280;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  margin-bottom: 8px;
  transition: all 0.3s;
}

.step.active .step-number {
  background: #3b82f6;
  color: white;
}

.step.completed .step-number {
  background: #10b981;
  color: white;
}

.step-label {
  font-size: 0.875rem;
  color: #6b7280;
  text-align: center;
}

.step.active .step-label {
  color: #3b82f6;
  font-weight: 500;
}

.step.completed .step-label {
  color: #10b981;
  font-weight: 500;
}

.step-content {
  background: white;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
}

.step-header h2 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.step-header p {
  color: #6b7280;
  margin: 0 0 32px 0;
}

.app-form {
  max-width: 600px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-group label {
  font-weight: 500;
  color: #374151;
  margin-bottom: 6px;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 10px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-size: 0.875rem;
  transition: border-color 0.2s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-help {
  font-size: 0.75rem;
  color: #6b7280;
  margin-top: 4px;
}

.error-message {
  font-size: 0.75rem;
  color: #dc2626;
  margin-top: 4px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-weight: 500;
  color: #374151;
}

.checkbox-label input[type="checkbox"] {
  width: auto;
  margin: 0;
}

.json-editor {
  position: relative;
}

.json-editor textarea {
  font-family: 'Courier New', monospace;
  font-size: 0.875rem;
}

.json-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  gap: 4px;
}

.review-section {
  display: grid;
  gap: 24px;
}

.review-card {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 24px;
}

.review-card h3 {
  font-size: 1.125rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 16px 0;
}

.review-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.review-item.full-width {
  grid-column: 1 / -1;
}

.review-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.review-item label {
  font-weight: 500;
  color: #6b7280;
  font-size: 0.875rem;
}

.review-item span {
  color: #1f2937;
}

.app-type {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 500;
}

.type-chatbot { background: #dbeafe; color: #1e40af; }
.type-integration { background: #f3e8ff; color: #6b21a8; }
.type-analytics { background: #fef3c7; color: #92400e; }
.type-automation { background: #ecfdf5; color: #059669; }
.type-api { background: #e0e7ff; color: #3730a3; }
.type-webhook { background: #fef2f2; color: #b91c1c; }

.visibility {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.75rem;
  background: #f3f4f6;
  color: #6b7280;
}

.visibility.public {
  background: #dbeafe;
  color: #1e40af;
}

.url {
  color: #3b82f6;
  word-break: break-all;
}

.config-preview {
  display: grid;
  gap: 16px;
}

.config-section h4 {
  font-size: 1rem;
  font-weight: 500;
  color: #374151;
  margin: 0 0 8px 0;
}

.config-section pre {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 12px;
  font-size: 0.875rem;
  overflow-x: auto;
  margin: 0;
}

.wizard-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.action-spacer {
  flex: 1;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-primary {
  background: #3b82f6;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #2563eb;
}

.btn-outline {
  background: white;
  color: #374151;
  border: 1px solid #e5e7eb;
}

.btn-outline:hover:not(:disabled) {
  background: #f9fafb;
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .create-app-wizard {
    padding: 16px;
  }
  
  .form-grid {
    grid-template-columns: 1fr;
  }
  
  .review-grid {
    grid-template-columns: 1fr;
  }
  
  .wizard-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .action-spacer {
    display: none;
  }
}
</style>
