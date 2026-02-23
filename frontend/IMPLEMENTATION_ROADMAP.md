# ========================================
# FRONTEND IMPLEMENTATION ROADMAP
# ========================================

## 🎯 **Phase 1: Core Hub Integration (Week 1-2)**

### **Day 1-2: Billing Hub Foundation**

#### **1.1 Create Billing Store Structure**
```javascript
// stores/billing/entitlementStore.js
export const useEntitlementStore = defineStore('billing.entitlement', () => {
  const entitlements = ref([])
  const usage = ref({})
  const features = ref([])
  
  // Actions matching backend Entitlement entity
  const fetchEntitlements = async (tenantId) => {
    const response = await billingApi.getEntitlements(tenantId)
    entitlements.value = response.data
  }
  
  const checkUsage = async (feature, amount) => {
    return await billingApi.checkUsage(feature, amount)
  }
  
  const addUsage = async (feature, amount) => {
    return await billingApi.addUsage(feature, amount)
  }
  
  return {
    entitlements,
    usage,
    features,
    fetchEntitlements,
    checkUsage,
    addUsage
  }
})
```

#### **1.2 Billing API Integration**
```javascript
// api/hubs/billing/entitlementApi.js
import axios from '@/plugins/axios'

export const entitlementApi = {
  // Match backend EntitlementController endpoints
  getEntitlements: (tenantId) => 
    axios.get(`/billing/entitlements?tenantId=${tenantId}`),
  
  checkUsage: (feature, amount) =>
    axios.post(`/billing/entitlements/check`, { feature, amount }),
  
  addUsage: (feature, amount) =>
    axios.post(`/billing/entitlements/usage`, { feature, amount }),
  
  getUsageHistory: (feature, period) =>
    axios.get(`/billing/entitlements/usage/${feature}?period=${period}`)
}
```

#### **1.3 Billing Components**
```vue
<!-- components/billing/EntitlementCard.vue -->
<template>
  <div class="entitlement-card bg-white dark:bg-gray-800 rounded-lg p-6 border">
    <div class="flex justify-between items-start mb-4">
      <div>
        <h3 class="text-lg font-semibold">{{ entitlement.feature }}</h3>
        <p class="text-sm text-gray-600">{{ entitlement.description }}</p>
      </div>
      <span :class="statusClass" class="px-2 py-1 rounded-full text-xs">
        {{ entitlement.isEnabled ? 'Active' : 'Inactive' }}
      </span>
    </div>
    
    <!-- Usage Meter -->
    <div class="mb-4">
      <div class="flex justify-between text-sm mb-1">
        <span>Usage</span>
        <span>{{ entitlement.currentUsage }} / {{ entitlement.limitValue || '∞' }}</span>
      </div>
      <div class="w-full bg-gray-200 rounded-full h-2">
        <div 
          :class="usageBarClass" 
          :style="{ width: usagePercentage + '%' }"
          class="h-2 rounded-full transition-all"
        ></div>
      </div>
    </div>
    
    <!-- Reset Info -->
    <div class="text-xs text-gray-500">
      <span v-if="entitlement.resetPeriod">
        Resets: {{ formatDate(entitlement.nextResetAt) }}
      </span>
      <span v-if="entitlement.overageAllowed">
        Overage rate: ${{ entitlement.overageRate }}/unit
      </span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  entitlement: Object
})

const usagePercentage = computed(() => {
  if (props.entitlement.isUnlimited) return 0
  return (props.entitlement.currentUsage / props.entitlement.limitValue) * 100
})

const statusClass = computed(() => {
  return props.entitlement.isEnabled 
    ? 'bg-green-100 text-green-800' 
    : 'bg-gray-100 text-gray-800'
})

const usageBarClass = computed(() => {
  const percentage = usagePercentage.value
  if (percentage >= 90) return 'bg-red-500'
  if (percentage >= 80) return 'bg-yellow-500'
  return 'bg-green-500'
})
</script>
```

#### **1.4 Billing Pages**
```vue
<!-- views/billing/Entitlements.vue -->
<template>
  <div class="billing-entitlements">
    <div class="mb-6">
      <h1 class="text-2xl font-bold">Feature Entitlements</h1>
      <p class="text-gray-600">Manage your feature usage and limits</p>
    </div>
    
    <!-- Usage Overview -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
      <div class="bg-white dark:bg-gray-800 p-6 rounded-lg border">
        <h3 class="text-sm font-medium text-gray-600">Active Features</h3>
        <p class="text-2xl font-bold">{{ activeFeatures }}</p>
      </div>
      <div class="bg-white dark:bg-gray-800 p-6 rounded-lg border">
        <h3 class="text-sm font-medium text-gray-600">Near Limit</h3>
        <p class="text-2xl font-bold text-yellow-600">{{ nearLimitFeatures }}</p>
      </div>
      <div class="bg-white dark:bg-gray-800 p-6 rounded-lg border">
        <h3 class="text-sm font-medium text-gray-600">Over Limit</h3>
        <p class="text-2xl font-bold text-red-600">{{ overLimitFeatures }}</p>
      </div>
    </div>
    
    <!-- Entitlements Grid -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <EntitlementCard 
        v-for="entitlement in entitlements" 
        :key="entitlement.id"
        :entitlement="entitlement"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, computed } from 'vue'
import { useEntitlementStore } from '@/stores/billing/entitlementStore'
import EntitlementCard from '@/components/billing/EntitlementCard.vue'

const entitlementStore = useEntitlementStore()

const entitlements = computed(() => entitlementStore.entitlements)
const activeFeatures = computed(() => 
  entitlements.value.filter(e => e.isEnabled).length
)
const nearLimitFeatures = computed(() => 
  entitlements.value.filter(e => e.isNearLimit()).length
)
const overLimitFeatures = computed(() => 
  entitlements.value.filter(e => e.isOverLimit()).length
)

onMounted(() => {
  entitlementStore.fetchEntitlements()
})
</script>
```

### **Day 3-4: Wallet Hub Implementation**

#### **1.5 Wallet Store**
```javascript
// stores/wallet/walletStore.js
export const useWalletStore = defineStore('wallet', () => {
  const wallets = ref([])
  const transactions = ref([])
  const currentWallet = ref(null)
  
  const fetchWallets = async () => {
    const response = await walletApi.getWallets()
    wallets.value = response.data
  }
  
  const fetchTransactions = async (walletId) => {
    const response = await walletApi.getTransactions(walletId)
    transactions.value = response.data
  }
  
  const topup = async (walletId, amount, paymentMethod) => {
    return await walletApi.topup(walletId, amount, paymentMethod)
  }
  
  const getBalance = (walletId) => {
    const wallet = wallets.value.find(w => w.id === walletId)
    return wallet?.balance || 0
  }
  
  return {
    wallets,
    transactions,
    currentWallet,
    fetchWallets,
    fetchTransactions,
    topup,
    getBalance
  }
})
```

#### **1.6 Wallet Components**
```vue
<!-- components/wallet/WalletBalance.vue -->
<template>
  <div class="wallet-balance bg-gradient-to-r from-blue-500 to-purple-600 rounded-lg p-6 text-white">
    <div class="flex justify-between items-start">
      <div>
        <p class="text-sm opacity-90">Current Balance</p>
        <p class="text-3xl font-bold">${{ formatCurrency(balance) }}</p>
        <p class="text-sm opacity-75 mt-1">{{ wallet.currency }}</p>
      </div>
      <div class="text-right">
        <span :class="statusClass" class="px-3 py-1 rounded-full text-xs">
          {{ wallet.status }}
        </span>
      </div>
    </div>
    
    <!-- Quick Actions -->
    <div class="flex gap-2 mt-6">
      <button 
        @click="$emit('topup')"
        class="bg-white text-blue-600 px-4 py-2 rounded-lg text-sm font-medium hover:bg-gray-100"
      >
        Top Up
      </button>
      <button 
        @click="$emit('transactions')"
        class="bg-white/20 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-white/30"
      >
        Transactions
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  wallet: Object
})

const emit = defineEmits(['topup', 'transactions'])

const balance = computed(() => props.wallet?.balance || 0)

const statusClass = computed(() => {
  switch (props.wallet?.status) {
    case 'ACTIVE': return 'bg-green-500'
    case 'FROZEN': return 'bg-yellow-500'
    case 'CLOSED': return 'bg-red-500'
    default: return 'bg-gray-500'
  }
})

const formatCurrency = (amount) => {
  return new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(amount / 100) // Assuming cents
}
</script>
```

### **Day 5-7: App Hub Enhancement**

#### **1.7 Enhanced App Registry**
```javascript
// stores/app/subscriptionStore.js
export const useSubscriptionStore = defineStore('app.subscription', () => {
  const subscriptions = ref([])
  const plans = ref([])
  
  const fetchSubscriptions = async () => {
    const response = await appApi.getSubscriptions()
    subscriptions.value = response.data
  }
  
  const subscribeToApp = async (appId, planId) => {
    return await appApi.subscribe(appId, planId)
  }
  
  const cancelSubscription = async (subscriptionId) => {
    return await appApi.cancelSubscription(subscriptionId)
  }
  
  return {
    subscriptions,
    plans,
    fetchSubscriptions,
    subscribeToApp,
    cancelSubscription
  }
})
```

#### **1.8 App Guard Components**
```vue
<!-- components/app/GuardRule.vue -->
<template>
  <div class="guard-rule bg-white dark:bg-gray-800 rounded-lg p-4 border">
    <div class="flex items-center justify-between mb-3">
      <div class="flex items-center gap-3">
        <div :class="iconClass" class="p-2 rounded-lg">
          <Icon :icon="ruleIcon" class="text-xl" />
        </div>
        <div>
          <h4 class="font-medium">{{ rule.name }}</h4>
          <p class="text-sm text-gray-600">{{ rule.description }}</p>
        </div>
      </div>
      <label class="relative inline-flex items-center cursor-pointer">
        <input 
          type="checkbox" 
          v-model="rule.isEnabled"
          @change="toggleRule"
          class="sr-only peer"
        >
        <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
      </label>
    </div>
    
    <!-- Rule Configuration -->
    <div v-if="rule.isEnabled" class="mt-4 p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
      <div class="text-sm">
        <div class="flex justify-between mb-2">
          <span class="font-medium">Type:</span>
          <span>{{ rule.guardType }}</span>
        </div>
        <div class="flex justify-between">
          <span class="font-medium">Priority:</span>
          <span>{{ rule.priority }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  rule: Object
})

const emit = defineEmits('toggle')

const iconClass = computed(() => {
  switch (props.rule.guardType) {
    case 'RATE_LIMIT': return 'bg-blue-100 text-blue-600'
    case 'IP_WHITELIST': return 'bg-green-100 text-green-600'
    case 'FEATURE_FLAG': return 'bg-purple-100 text-purple-600'
    default: return 'bg-gray-100 text-gray-600'
  }
})

const ruleIcon = computed(() => {
  switch (props.rule.guardType) {
    case 'RATE_LIMIT': return 'ic:baseline-speed'
    case 'IP_WHITELIST': return 'ic:baseline-security'
    case 'FEATURE_FLAG': return 'ic:baseline-flag'
    default: return 'ic:baseline-settings'
  }
})

const toggleRule = () => {
  emit('toggle', props.rule)
}
</script>
```

## 🎯 **Phase 2: Advanced Features (Week 3-4)**

### **Day 8-10: Config Hub Implementation**

#### **2.1 Runtime Configuration**
```javascript
// stores/config/runtimeStore.js
export const useRuntimeConfigStore = defineStore('config.runtime', () => {
  const configs = ref([])
  const cacheConfigs = ref([])
  
  const fetchConfigs = async (scope = 'GLOBAL') => {
    const response = await configApi.getRuntimeConfigs(scope)
    configs.value = response.data
  }
  
  const updateConfig = async (configId, value) => {
    return await configApi.updateRuntimeConfig(configId, value)
  }
  
  const clearCache = async (cacheKey) => {
    return await configApi.clearCache(cacheKey)
  }
  
  return {
    configs,
    cacheConfigs,
    fetchConfigs,
    updateConfig,
    clearCache
  }
})
```

#### **2.2 Config Management UI**
```vue
<!-- views/config/Runtime.vue -->
<template>
  <div class="config-runtime">
    <div class="mb-6">
      <h1 class="text-2xl font-bold">Runtime Configuration</h1>
      <p class="text-gray-600">Manage system runtime settings</p>
    </div>
    
    <!-- Scope Selector -->
    <div class="bg-white dark:bg-gray-800 rounded-lg p-4 mb-6 border">
      <div class="flex gap-4">
        <button 
          v-for="scope in scopes" 
          :key="scope"
          @click="selectedScope = scope"
          :class="scope === selectedScope ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-700'"
          class="px-4 py-2 rounded-lg font-medium"
        >
          {{ scope }}
        </button>
      </div>
    </div>
    
    <!-- Configuration List -->
    <div class="space-y-4">
      <div 
        v-for="config in filteredConfigs" 
        :key="config.id"
        class="bg-white dark:bg-gray-800 rounded-lg p-6 border"
      >
        <div class="flex justify-between items-start mb-4">
          <div>
            <h3 class="font-semibold">{{ config.key }}</h3>
            <p class="text-sm text-gray-600">{{ config.description }}</p>
          </div>
          <span :class="getTypeClass(config.configType)" class="px-2 py-1 rounded text-xs">
            {{ config.configType }}
          </span>
        </div>
        
        <!-- Config Value Editor -->
        <div class="mt-4">
          <ConfigEditor 
            :config="config"
            @update="updateConfig"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRuntimeConfigStore } from '@/stores/config/runtimeStore'
import ConfigEditor from '@/components/config/ConfigEditor.vue'

const configStore = useRuntimeConfigStore()

const selectedScope = ref('GLOBAL')
const scopes = ['GLOBAL', 'TENANT', 'USER', 'APP']

const filteredConfigs = computed(() => {
  return configStore.configs.filter(config => config.scope === selectedScope.value)
})

const updateConfig = async (configId, value) => {
  await configStore.updateConfig(configId, value)
}

const getTypeClass = (type) => {
  switch (type) {
    case 'STRING': return 'bg-blue-100 text-blue-800'
    case 'NUMBER': return 'bg-green-100 text-green-800'
    case 'BOOLEAN': return 'bg-purple-100 text-purple-800'
    case 'JSON': return 'bg-orange-100 text-orange-800'
    default: return 'bg-gray-100 text-gray-800'
  }
}

onMounted(() => {
  configStore.fetchConfigs()
})
</script>
```

### **Day 11-14: Message Hub Implementation**

#### **2.3 Message Router Store**
```javascript
// stores/message/routerStore.js
export const useMessageRouterStore = defineStore('message.router', () => {
  const routes = ref([])
  const rules = ref([])
  const decisions = ref([])
  
  const fetchRoutes = async () => {
    const response = await messageApi.getRoutes()
    routes.value = response.data
  }
  
  const fetchRules = async () => {
    const response = await messageApi.getRules()
    rules.value = response.data
  }
  
  const createRoute = async (route) => {
    return await messageApi.createRoute(route)
  }
  
  const testRouting = async (message) => {
    return await messageApi.testRouting(message)
  }
  
  return {
    routes,
    rules,
    decisions,
    fetchRoutes,
    fetchRules,
    createRoute,
    testRouting
  }
})
```

## 🎯 **Phase 3: Integrations (Week 5-6)**

### **Day 15-18: Facebook Spoke Integration**

#### **3.1 Facebook Integration Store**
```javascript
// stores/spokes/facebookStore.js
export const useFacebookStore = defineStore('spokes.facebook', () => {
  const connections = ref([])
  const webhooks = ref([])
  const pages = ref([])
  
  const fetchConnections = async () => {
    const response = await facebookApi.getConnections()
    connections.value = response.data
  }
  
  const createWebhook = async (pageId, webhookUrl) => {
    return await facebookApi.createWebhook(pageId, webhookUrl)
  }
  
  const subscribeToPage = async (pageId) => {
    return await facebookApi.subscribeToPage(pageId)
  }
  
  return {
    connections,
    webhooks,
    pages,
    fetchConnections,
    createWebhook,
    subscribeToPage
  }
})
```

#### **3.2 Facebook Connection UI**
```vue
<!-- views/integrations/facebook/Connections.vue -->
<template>
  <div class="facebook-connections">
    <div class="mb-6">
      <h1 class="text-2xl font-bold">Facebook Integration</h1>
      <p class="text-gray-600">Manage Facebook connections and webhooks</p>
    </div>
    
    <!-- Connection Status -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
      <div class="bg-white dark:bg-gray-800 p-6 rounded-lg border">
        <h3 class="text-sm font-medium text-gray-600">Connected Pages</h3>
        <p class="text-2xl font-bold">{{ connectedPages }}</p>
      </div>
      <div class="bg-white dark:bg-gray-800 p-6 rounded-lg border">
        <h3 class="text-sm font-medium text-gray-600">Active Webhooks</h3>
        <p class="text-2xl font-bold">{{ activeWebhooks }}</p>
      </div>
      <div class="bg-white dark:bg-gray-800 p-6 rounded-lg border">
        <h3 class="text-sm font-medium text-gray-600">Messages Today</h3>
        <p class="text-2xl font-bold">{{ messagesToday }}</p>
      </div>
    </div>
    
    <!-- Connect New Page -->
    <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border mb-6">
      <h2 class="text-lg font-semibold mb-4">Connect New Page</h2>
      <FacebookPageConnect @connected="onPageConnected" />
    </div>
    
    <!-- Existing Connections -->
    <div class="space-y-4">
      <FacebookConnectionCard 
        v-for="connection in connections" 
        :key="connection.id"
        :connection="connection"
        @disconnect="onDisconnect"
        @edit="onEdit"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useFacebookStore } from '@/stores/spokes/facebookStore'
import FacebookPageConnect from '@/components/integrations/facebook/FacebookPageConnect.vue'
import FacebookConnectionCard from '@/components/integrations/facebook/FacebookConnectionCard.vue'

const facebookStore = useFacebookStore()

const connections = computed(() => facebookStore.connections)
const connectedPages = computed(() => 
  connections.value.filter(c => c.status === 'CONNECTED').length
)
const activeWebhooks = computed(() => 
  connections.value.filter(c => c.webhookStatus === 'ACTIVE').length
)
const messagesToday = computed(() => {
  // Calculate from connection analytics
  return connections.value.reduce((total, conn) => total + (conn.messagesToday || 0), 0)
})

const onPageConnected = (page) => {
  // Handle new page connection
}

const onDisconnect = (connectionId) => {
  // Handle disconnection
}

const onEdit = (connection) => {
  // Handle connection edit
}

onMounted(() => {
  facebookStore.fetchConnections()
})
</script>
```

## 🎯 **Phase 4: Advanced Features (Week 7-8)**

### **Day 19-21: Advanced Analytics**

#### **4.1 Cross-Hub Analytics**
```javascript
// stores/analytics/crossHubStore.js
export const useCrossHubAnalytics = defineStore('analytics.crosshub', () => {
  const metrics = ref({
    userMetrics: {},
    tenantMetrics: {},
    appMetrics: {},
    billingMetrics: {},
    messageMetrics: {}
  })
  
  const fetchCrossHubMetrics = async (period = '30d') => {
    const response = await analyticsApi.getCrossHubMetrics(period)
    metrics.value = response.data
  }
  
  const getHubHealth = () => {
    // Calculate overall system health
    const health = {}
    Object.keys(metrics.value).forEach(hub => {
      health[hub] = calculateHubHealth(metrics.value[hub])
    })
    return health
  }
  
  return {
    metrics,
    fetchCrossHubMetrics,
    getHubHealth
  }
})
```

### **Day 22-24: Real-time Features**

#### **4.2 WebSocket Integration**
```javascript
// plugins/websocket.js
import { useCrossHubAnalytics } from '@/stores/analytics/crossHubStore'

class WebSocketManager {
  constructor() {
    this.socket = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
  }
  
  connect() {
    const token = localStorage.getItem('accessToken')
    const tenantId = localStorage.getItem('ACTIVE_TENANT_ID')
    
    this.socket = new WebSocket(`${process.env.VITE_WS_URL}?token=${token}&tenant=${tenantId}`)
    
    this.socket.onopen = () => {
      console.log('WebSocket connected')
      this.reconnectAttempts = 0
    }
    
    this.socket.onmessage = (event) => {
      const data = JSON.parse(event.data)
      this.handleMessage(data)
    }
    
    this.socket.onclose = () => {
      console.log('WebSocket disconnected')
      this.reconnect()
    }
  }
  
  handleMessage(data) {
    const analyticsStore = useCrossHubAnalytics()
    
    switch (data.type) {
      case 'METRICS_UPDATE':
        analyticsStore.updateMetrics(data.payload)
        break
      case 'HUB_STATUS_CHANGE':
        analyticsStore.updateHubStatus(data.payload)
        break
      case 'NEW_MESSAGE':
        // Handle real-time message
        break
    }
  }
  
  reconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      setTimeout(() => {
        this.reconnectAttempts++
        this.connect()
      }, 1000 * Math.pow(2, this.reconnectAttempts))
    }
  }
}

export const wsManager = new WebSocketManager()
```

## 📋 **Implementation Checklist**

### **Week 1-2: Core Hubs**
- [ ] Billing Hub store and components
- [ ] Wallet Hub store and components  
- [ ] Enhanced App Hub with subscriptions
- [ ] API integration for all three hubs
- [ ] Basic routing and navigation

### **Week 3-4: Advanced Features**
- [ ] Config Hub implementation
- [ ] Message Hub basic routing
- [ ] Cross-hub dashboard
- [ ] Advanced analytics components

### **Week 5-6: Integrations**
- [ ] Facebook Spoke integration
- [ ] Botpress Spoke integration
- [ ] MinIO Spoke integration
- [ ] Integration management UI

### **Week 7-8: Polish & Optimization**
- [ ] Odoo Spoke integration
- [ ] Real-time WebSocket features
- [ ] Performance optimization
- [ ] Testing and bug fixes
- [ ] Documentation completion

This roadmap provides a structured approach to implementing all missing frontend features while maintaining code quality and user experience standards.
