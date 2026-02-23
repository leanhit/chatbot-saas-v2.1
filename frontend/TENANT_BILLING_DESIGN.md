# ========================================
# TENANT-CENTRIC BILLING DESIGN
# ========================================

## 🎯 **Thiết kế Tenant quản lý thanh toán**

### **Concept: Tenant là trung tâm billing**

Thay vì Billing Hub riêng biệt, **Tenant Hub** sẽ là trung tâm quản lý tất cả operations liên quan đến thanh toán:

```
Tenant Hub (Mở rộng)
├── Tenant Management
├── Membership Management  
├── 🆕 Billing Management
│   ├── Billing Accounts
│   ├── Subscriptions
│   ├── Payment Methods
│   └── Invoices
├── 🆕 Usage & Entitlements
│   ├── Feature Entitlements
│   ├── Usage Tracking
│   └── Limits Management
└── 🆕 Financial Operations
    ├── Payments
    ├── Refunds
    └── Billing Reports
```

## 🏗️ **Backend Entity Relationships**

```java
// Tenant là trung tâm
Tenant (1) -> (1) BillingAccount
Tenant (1) -> (N) BillingSubscription  
Tenant (1) -> (N) Entitlement
Tenant (1) -> (N) PaymentMethod
Tenant (1) -> (N) Invoice

// Mọi billing entity đều có tenant_id
@Entity
public class BillingAccount extends BaseTenantEntity {
    // Tenant sở hữu billing account
}

@Entity  
public class Entitlement extends BaseTenantEntity {
    // Tenant có các entitlements riêng
}
```

## 🎨 **Frontend Tenant-Centric Design**

### **1. Enhanced Tenant Gateway**
```vue
<!-- views/tenant/gateway/Gateway.vue -->
<template>
  <div class="tenant-gateway">
    <!-- Tenant Selection với Billing Info -->
    <div class="tenant-cards">
      <div v-for="tenant in tenants" :key="tenant.id" class="tenant-card">
        <div class="tenant-info">
          <h3>{{ tenant.name }}</h3>
          <p>{{ tenant.description }}</p>
        </div>
        
        <!-- Billing Status -->
        <div class="billing-status">
          <div class="subscription-info">
            <span class="plan">{{ tenant.subscription?.plan || 'Free' }}</span>
            <span class="status" :class="tenant.billingStatus">
              {{ tenant.billingStatus }}
            </span>
          </div>
          <div class="next-payment">
            Next: {{ formatDate(tenant.nextPaymentDate) }}
          </div>
        </div>
        
        <div class="tenant-actions">
          <button @click="selectTenant(tenant)">Select</button>
          <button @click="manageBilling(tenant)">Billing</button>
        </div>
      </div>
    </div>
    
    <!-- Quick Billing Overview -->
    <div class="billing-overview">
      <h3>Billing Overview</h3>
      <div class="metrics">
        <div class="metric">
          <span>Total Active Tenants</span>
          <strong>{{ activeTenants }}</strong>
        </div>
        <div class="metric">
          <span>Monthly Revenue</span>
          <strong>${{ monthlyRevenue }}</strong>
        </div>
        <div class="metric">
          <span>Pending Payments</span>
          <strong>{{ pendingPayments }}</strong>
        </div>
      </div>
    </div>
  </div>
</template>
```

### **2. Tenant Billing Management**
```vue
<!-- views/tenant/billing/BillingDashboard.vue -->
<template>
  <div class="tenant-billing">
    <!-- Tenant Header -->
    <div class="tenant-header">
      <div class="tenant-info">
        <h1>{{ currentTenant.name }} - Billing</h1>
        <p>Manage billing, subscriptions, and payments</p>
      </div>
      <div class="billing-summary">
        <div class="current-plan">
          <span class="plan-name">{{ currentSubscription.plan }}</span>
          <span class="plan-price">${{ currentSubscription.price }}/month</span>
        </div>
        <button @click="upgradePlan" class="upgrade-btn">Upgrade</button>
      </div>
    </div>
    
    <!-- Billing Navigation -->
    <div class="billing-nav">
      <button 
        v-for="tab in billingTabs" 
        :key="tab.key"
        @click="activeTab = tab.key"
        :class="{ active: activeTab === tab.key }"
        class="nav-tab"
      >
        {{ tab.label }}
      </button>
    </div>
    
    <!-- Tab Content -->
    <div class="billing-content">
      <!-- Overview Tab -->
      <div v-if="activeTab === 'overview'" class="overview-tab">
        <div class="billing-metrics">
          <div class="metric-card">
            <h3>Current Balance</h3>
            <p class="amount">${{ billingAccount.balance }}</p>
          </div>
          <div class="metric-card">
            <h3>This Month Usage</h3>
            <p class="amount">${{ monthlyUsage }}</p>
          </div>
          <div class="metric-card">
            <h3>Active Subscriptions</h3>
            <p class="count">{{ activeSubscriptions }}</p>
          </div>
        </div>
        
        <!-- Usage Entitlements -->
        <div class="entitlements-section">
          <h3>Feature Usage</h3>
          <div class="entitlements-grid">
            <EntitlementCard 
              v-for="entitlement in entitlements"
              :key="entitlement.id"
              :entitlement="entitlement"
            />
          </div>
        </div>
      </div>
      
      <!-- Subscriptions Tab -->
      <div v-if="activeTab === 'subscriptions'" class="subscriptions-tab">
        <SubscriptionsManager />
      </div>
      
      <!-- Payment Methods Tab -->
      <div v-if="activeTab === 'payment-methods'" class="payment-methods-tab">
        <PaymentMethodsManager />
      </div>
      
      <!-- Invoices Tab -->
      <div v-if="activeTab === 'invoices'" class="invoices-tab">
        <InvoicesList />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useTenantStore } from '@/stores/tenant/tenantStore'
import { useBillingStore } from '@/stores/tenant/billingStore'
import EntitlementCard from '@/components/tenant/billing/EntitlementCard.vue'

const tenantStore = useTenantStore()
const billingStore = useBillingStore()

const activeTab = ref('overview')
const billingTabs = [
  { key: 'overview', label: 'Overview' },
  { key: 'subscriptions', label: 'Subscriptions' },
  { key: 'payment-methods', label: 'Payment Methods' },
  { key: 'invoices', label: 'Invoices' }
]

const currentTenant = computed(() => tenantStore.currentTenant)
const currentSubscription = computed(() => billingStore.currentSubscription)
const entitlements = computed(() => billingStore.entitlements)

onMounted(() => {
  billingStore.fetchBillingData()
})
</script>
```

### **3. Tenant Billing Store**
```javascript
// stores/tenant/billingStore.js
export const useTenantBillingStore = defineStore('tenant.billing', () => {
  const billingAccount = ref(null)
  const subscriptions = ref([])
  const entitlements = ref([])
  const paymentMethods = ref([])
  const invoices = ref([])
  
  // Fetch all billing data for current tenant
  const fetchBillingData = async () => {
    const tenantId = getCurrentTenantId()
    
    // Parallel fetch billing data
    const [billingRes, subsRes, entRes, pmRes, invRes] = await Promise.all([
      axios.get(`/tenants/${tenantId}/billing/account`),
      axios.get(`/tenants/${tenantId}/billing/subscriptions`),
      axios.get(`/tenants/${tenantId}/billing/entitlements`),
      axios.get(`/tenants/${tenantId}/billing/payment-methods`),
      axios.get(`/tenants/${tenantId}/billing/invoices`)
    ])
    
    billingAccount.value = billingRes.data
    subscriptions.value = subsRes.data
    entitlements.value = entRes.data
    paymentMethods.value = pmRes.data
    invoices.value = invRes.data
  }
  
  // Subscription management
  const upgradeSubscription = async (planId) => {
    const tenantId = getCurrentTenantId()
    return await axios.post(`/tenants/${tenantId}/billing/upgrade`, { planId })
  }
  
  const cancelSubscription = async (subscriptionId) => {
    const tenantId = getCurrentTenantId()
    return await axios.delete(`/tenants/${tenantId}/billing/subscriptions/${subscriptionId}`)
  }
  
  // Payment operations
  const addPaymentMethod = async (paymentData) => {
    const tenantId = getCurrentTenantId()
    return await axios.post(`/tenants/${tenantId}/billing/payment-methods`, paymentData)
  }
  
  const makePayment = async (amount, paymentMethodId) => {
    const tenantId = getCurrentTenantId()
    return await axios.post(`/tenants/${tenantId}/billing/payments`, {
      amount,
      paymentMethodId
    })
  }
  
  // Usage tracking
  const checkEntitlementUsage = async (feature, amount) => {
    const tenantId = getCurrentTenantId()
    return await axios.post(`/tenants/${tenantId}/billing/entitlements/check`, {
      feature,
      amount
    })
  }
  
  return {
    billingAccount,
    subscriptions,
    entitlements,
    paymentMethods,
    invoices,
    fetchBillingData,
    upgradeSubscription,
    cancelSubscription,
    addPaymentMethod,
    makePayment,
    checkEntitlementUsage
  }
})
```

### **4. Enhanced Tenant Store**
```javascript
// stores/tenant/tenantStore.js (Enhanced)
export const useTenantStore = defineStore('tenant', () => {
  const tenants = ref([])
  const currentTenant = ref(null)
  
  // Enhanced tenant data with billing info
  const fetchTenants = async () => {
    const response = await axios.get('/tenants/my-list')
    tenants.value = response.data.map(tenant => ({
      ...tenant,
      // Add billing summary
      billingStatus: calculateBillingStatus(tenant),
      nextPaymentDate: getNextPaymentDate(tenant),
      subscription: tenant.subscriptions?.[0] || null
    }))
  }
  
  const createTenant = async (tenantData) => {
    const response = await axios.post('/tenants', tenantData)
    const newTenant = response.data
    
    // Auto-create billing account for new tenant
    await createBillingAccount(newTenant.id)
    
    return newTenant
  }
  
  const createBillingAccount = async (tenantId) => {
    return await axios.post(`/tenants/${tenantId}/billing/account`)
  }
  
  return {
    tenants,
    currentTenant,
    fetchTenants,
    createTenant
  }
})
```

## 🔗 **API Endpoints - Tenant-Centric**

```javascript
// All billing endpoints under tenant context
/api/tenants/{tenantId}/billing/
├── account                    # Billing account management
├── subscriptions              # Subscription management
├── entitlements              # Feature entitlements
├── payment-methods           # Payment methods
├── invoices                  # Billing invoices
├── payments                  # Payment operations
└── usage                     # Usage tracking
```

## 🎯 **Benefits của Tenant-Centric Billing**

### **1. Clear Ownership**
- Mỗi tenant tự quản lý billing của mình
- Dễ tracking revenue per tenant
- Isolation billing data

### **2. Simplified Architecture**
- Không cần Billing Hub riêng
- Giảm complexity của hệ thống
- Dễ maintain và scale

### **3. Better User Experience**
- Tenant quản lý mọi thứ trong một place
- Consistent UI/UX
- Dễ navigate

### **4. Business Logic Clear**
- Tenant là "customer" thực sự
- Billing là feature của tenant
- Dễ implement multi-level pricing

## 🚀 **Implementation Priority**

### **Phase 1: Core Tenant Billing**
1. Enhanced Tenant Gateway với billing info
2. Tenant Billing Dashboard
3. Basic subscription management
4. Payment method integration

### **Phase 2: Advanced Features**
1. Usage tracking & entitlements
2. Invoice management
3. Billing analytics
4. Multi-currency support

### **Phase 3: Enterprise Features**
1. Multi-tenant billing hierarchy
2. Custom billing plans
3. Advanced reporting
4. Integration with payment gateways

Thiết kế này đặt **Tenant** làm trung tâm của mọi hoạt động billing, phù hợp với mô hình multi-tenant SaaS và dễ implement hơn.
