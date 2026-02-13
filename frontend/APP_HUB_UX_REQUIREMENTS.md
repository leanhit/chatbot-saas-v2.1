# ========================================
# FRONTEND APP HUB MANAGEMENT - UX REQUIREMENTS
# ========================================

## 🏗️ Pages/Views cần tạo

### 1. App Hub Dashboard (`/app-hub`)
- **Mục đích**: Tổng quan về tất cả apps
- **Features**:
  - Tổng số apps: active/inactive/public/private
  - Chart thống kê apps theo loại (AppType)
  - Apps mới đăng ký gần đây
  - Subscription statistics
  - Quick actions: Create App, Search Apps

### 2. App Registry (`/app-hub/apps`)
- **Mục đích**: Quản lý danh sách ứng dụng
- **Features**:
  - Table/List apps với pagination
  - Search & Filter (name, type, status, visibility)
  - Bulk actions (activate/deactivate)
  - Sort options (name, created date, status)
  - Export to CSV/Excel

### 3. App Detail (`/app-hub/apps/:id`)
- **Mục đích**: Chi tiết một ứng dụng
- **Features**:
  - App Information (name, description, type, status)
  - Configuration Schema & Default Config
  - API Endpoint & Webhook URL
  - Subscription Management
  - Analytics & Usage Statistics
  - Edit/Delete actions

### 4. Create App (`/app-hub/apps/create`)
- **Mục đích**: Đăng ký ứng dụng mới
- **Features**:
  - Form validation
  - Dynamic config schema builder
  - Preview app before submit
  - Auto-generate API keys
  - Step-by-step wizard

### 5. App Subscription Management (`/app-hub/subscriptions`)
- **Mục đích**: Quản lý subscriptions
- **Features**:
  - List subscriptions theo app/tenant/user
  - Subscription status (active/expired/trial)
  - Plan management (free/premium/enterprise)
  - Renewal settings
  - Payment integration

### 6. App Marketplace (`/app-hub/marketplace`)
- **Mục đích**: Khám phá apps có sẵn
- **Features**:
  - Browse public apps
  - App categories
  - Rating & reviews
  - One-click subscribe
  - App details & documentation

## 🧩 Components cần tạo

### 1. AppCard.vue
```vue
<template>
  <div class="app-card">
    <div class="app-header">
      <img :src="app.logo" :alt="app.name" class="app-logo">
      <div class="app-info">
        <h3>{{ app.name }}</h3>
        <p>{{ app.description }}</p>
      </div>
      <div class="app-status">
        <span :class="statusClass">{{ app.status }}</span>
      </div>
    </div>
    <div class="app-body">
      <div class="app-meta">
        <span class="app-type">{{ app.appType }}</span>
        <span class="app-version">v{{ app.version }}</span>
      </div>
      <div class="app-actions">
        <button @click="viewDetails">View Details</button>
        <button @click="subscribe" v-if="app.isPublic">Subscribe</button>
      </div>
    </div>
  </div>
</template>
```

### 2. AppTable.vue
```vue
<template>
  <div class="app-table">
    <div class="table-header">
      <div class="search-filter">
        <input v-model="searchQuery" placeholder="Search apps...">
        <select v-model="filterType">
          <option value="">All Types</option>
          <option value="CHATBOT">Chatbot</option>
          <option value="INTEGRATION">Integration</option>
        </select>
        <select v-model="filterStatus">
          <option value="">All Status</option>
          <option value="ACTIVE">Active</option>
          <option value="INACTIVE">Inactive</option>
        </select>
      </div>
      <div class="table-actions">
        <button @click="createApp">Create App</button>
        <button @click="exportApps">Export</button>
      </div>
    </div>
    <table>
      <thead>
        <tr>
          <th>Name</th>
          <th>Type</th>
          <th>Status</th>
          <th>Visibility</th>
          <th>Subscriptions</th>
          <th>Created</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="app in filteredApps" :key="app.id">
          <td>{{ app.name }}</td>
          <td>{{ app.appType }}</td>
          <td><span :class="getStatusClass(app.status)">{{ app.status }}</span></td>
          <td>{{ app.isPublic ? 'Public' : 'Private' }}</td>
          <td>{{ app.subscriptionCount }}</td>
          <td>{{ formatDate(app.createdAt) }}</td>
          <td>
            <button @click="editApp(app)">Edit</button>
            <button @click="viewSubscriptions(app)">Subscriptions</button>
            <button @click="deleteApp(app)" class="danger">Delete</button>
          </td>
        </tr>
      </tbody>
    </table>
    <div class="pagination">
      <button @click="prevPage" :disabled="currentPage === 1">Previous</button>
      <span>Page {{ currentPage }} of {{ totalPages }}</span>
      <button @click="nextPage" :disabled="currentPage === totalPages">Next</button>
    </div>
  </div>
</template>
```

### 3. CreateAppWizard.vue
```vue
<template>
  <div class="create-app-wizard">
    <div class="wizard-header">
      <h2>Create New Application</h2>
      <div class="progress-bar">
        <div class="step" :class="{ active: currentStep >= 1 }">Basic Info</div>
        <div class="step" :class="{ active: currentStep >= 2 }">Configuration</div>
        <div class="step" :class="{ active: currentStep >= 3 }">Review</div>
      </div>
    </div>
    
    <div class="wizard-content">
      <!-- Step 1: Basic Information -->
      <div v-if="currentStep === 1" class="step-content">
        <h3>Basic Information</h3>
        <form @submit.prevent="nextStep">
          <div class="form-group">
            <label>App Name *</label>
            <input v-model="app.name" required>
          </div>
          <div class="form-group">
            <label>Display Name *</label>
            <input v-model="app.displayName" required>
          </div>
          <div class="form-group">
            <label>Description</label>
            <textarea v-model="app.description"></textarea>
          </div>
          <div class="form-group">
            <label>App Type *</label>
            <select v-model="app.appType" required>
              <option value="">Select Type</option>
              <option value="CHATBOT">Chatbot</option>
              <option value="INTEGRATION">Integration</option>
              <option value="ANALYTICS">Analytics</option>
            </select>
          </div>
          <div class="form-group">
            <label>Visibility</label>
            <label class="checkbox">
              <input type="checkbox" v-model="app.isPublic">
              Make this app public
            </label>
          </div>
        </form>
      </div>
      
      <!-- Step 2: Configuration -->
      <div v-if="currentStep === 2" class="step-content">
        <h3>Configuration</h3>
        <form @submit.prevent="nextStep">
          <div class="form-group">
            <label>Version</label>
            <input v-model="app.version" placeholder="1.0.0">
          </div>
          <div class="form-group">
            <label>API Endpoint</label>
            <input v-model="app.apiEndpoint" placeholder="https://api.example.com">
          </div>
          <div class="form-group">
            <label>Webhook URL</label>
            <input v-model="app.webhookUrl" placeholder="https://webhook.example.com">
          </div>
          <div class="form-group">
            <label>Configuration Schema (JSON)</label>
            <textarea v-model="app.configSchema" rows="10"></textarea>
          </div>
          <div class="form-group">
            <label>Default Configuration (JSON)</label>
            <textarea v-model="app.defaultConfig" rows="10"></textarea>
          </div>
        </form>
      </div>
      
      <!-- Step 3: Review -->
      <div v-if="currentStep === 3" class="step-content">
        <h3>Review & Create</h3>
        <div class="review-section">
          <h4>Application Details</h4>
          <div class="review-item">
            <strong>Name:</strong> {{ app.name }}
          </div>
          <div class="review-item">
            <strong>Type:</strong> {{ app.appType }}
          </div>
          <div class="review-item">
            <strong>Visibility:</strong> {{ app.isPublic ? 'Public' : 'Private' }}
          </div>
        </div>
      </div>
    </div>
    
    <div class="wizard-actions">
      <button @click="prevStep" v-if="currentStep > 1">Previous</button>
      <button @click="nextStep" v-if="currentStep < 3">Next</button>
      <button @click="createApp" v-if="currentStep === 3" class="primary">Create App</button>
    </div>
  </div>
</template>
```

### 4. AppSubscriptionManager.vue
```vue
<template>
  <div class="subscription-manager">
    <div class="manager-header">
      <h2>Subscription Management</h2>
      <div class="filters">
        <select v-model="filterApp">
          <option value="">All Apps</option>
          <option v-for="app in apps" :key="app.id" :value="app.id">
            {{ app.name }}
          </option>
        </select>
        <select v-model="filterStatus">
          <option value="">All Status</option>
          <option value="ACTIVE">Active</option>
          <option value="EXPIRED">Expired</option>
          <option value="TRIAL">Trial</option>
        </select>
      </div>
    </div>
    
    <div class="subscriptions-grid">
      <div v-for="subscription in filteredSubscriptions" :key="subscription.id" class="subscription-card">
        <div class="subscription-header">
          <h3>{{ subscription.app.name }}</h3>
          <span :class="getStatusClass(subscription.status)">{{ subscription.status }}</span>
        </div>
        <div class="subscription-body">
          <div class="subscription-info">
            <p><strong>Plan:</strong> {{ subscription.subscriptionPlan }}</p>
            <p><strong>Tenant:</strong> {{ subscription.tenantId }}</p>
            <p><strong>Started:</strong> {{ formatDate(subscription.subscriptionStart) }}</p>
            <p><strong>Expires:</strong> {{ formatDate(subscription.subscriptionEnd) }}</p>
            <p><strong>Auto-renew:</strong> {{ subscription.autoRenew ? 'Yes' : 'No' }}</p>
          </div>
          <div class="subscription-actions">
            <button @click="renewSubscription(subscription)">Renew</button>
            <button @click="cancelSubscription(subscription)" class="danger">Cancel</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
```

## 🔄 API Integration cần tạo

### 1. appApi.js
```javascript
import axios from './index';

const appApi = {
  // App Registry
  getApps: (params = {}) => axios.get('/api/v1/apps', { params }),
  getApp: (id) => axios.get(`/api/v1/apps/${id}`),
  createApp: (data) => axios.post('/api/v1/apps', data),
  updateApp: (id, data) => axios.put(`/api/v1/apps/${id}`, data),
  deleteApp: (id) => axios.delete(`/api/v1/apps/${id}`),
  searchApps: (params) => axios.get('/api/v1/apps/search', { params }),
  
  // Subscriptions
  getSubscriptions: (params = {}) => axios.get('/api/v1/subscriptions', { params }),
  getSubscription: (id) => axios.get(`/api/v1/subscriptions/${id}`),
  subscribeToApp: (data) => axios.post('/api/v1/subscriptions', data),
  cancelSubscription: (id) => axios.delete(`/api/v1/subscriptions/${id}`),
  renewSubscription: (id) => axios.post(`/api/v1/subscriptions/${id}/renew`),
  
  // App Marketplace
  getPublicApps: (params = {}) => axios.get('/api/v1/apps/public', { params }),
  getAppDetails: (id) => axios.get(`/api/v1/apps/public/${id}`),
  
  // Analytics
  getAppAnalytics: (id) => axios.get(`/api/v1/apps/${id}/analytics`),
  getSubscriptionAnalytics: () => axios.get('/api/v1/subscriptions/analytics'),
};

export default appApi;
```

### 2. App Store (Pinia)
```javascript
import { defineStore } from 'pinia';
import appApi from '@/api/appApi';

export const useAppStore = defineStore('app', {
  state: () => ({
    apps: [],
    currentApp: null,
    subscriptions: [],
    publicApps: [],
    loading: false,
    error: null,
  }),
  
  actions: {
    async fetchApps(params = {}) {
      this.loading = true;
      try {
        const response = await appApi.getApps(params);
        this.apps = response.data;
      } catch (error) {
        this.error = error.message;
      } finally {
        this.loading = false;
      }
    },
    
    async createApp(appData) {
      this.loading = true;
      try {
        const response = await appApi.createApp(appData);
        this.apps.push(response.data);
        return response.data;
      } catch (error) {
        this.error = error.message;
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    async subscribeToApp(subscriptionData) {
      this.loading = true;
      try {
        const response = await appApi.subscribeToApp(subscriptionData);
        this.subscriptions.push(response.data);
        return response.data;
      } catch (error) {
        this.error = error.message;
        throw error;
      } finally {
        this.loading = false;
      }
    },
  },
});
```

## 🗂️ Router Updates cần thêm

```javascript
// Add to router/index.js
import AppHubDashboard from "../views/app-hub/Dashboard.vue";
import AppRegistry from "../views/app-hub/AppRegistry.vue";
import AppDetail from "../views/app-hub/AppDetail.vue";
import CreateApp from "../views/app-hub/CreateApp.vue";
import AppSubscriptions from "../views/app-hub/AppSubscriptions.vue";
import AppMarketplace from "../views/app-hub/AppMarketplace.vue";

// Add routes
{
  path: "/app-hub",
  name: "app-hub-dashboard",
  component: AppHubDashboard,
  meta: { requiresAuth: true, title: "App Hub Dashboard" + appname },
},
{
  path: "/app-hub/apps",
  name: "app-registry",
  component: AppRegistry,
  meta: { requiresAuth: true, title: "App Registry" + appname },
},
{
  path: "/app-hub/apps/create",
  name: "create-app",
  component: CreateApp,
  meta: { requiresAuth: true, title: "Create App" + appname },
},
{
  path: "/app-hub/apps/:id",
  name: "app-detail",
  component: AppDetail,
  meta: { requiresAuth: true, title: "App Details" + appname },
},
{
  path: "/app-hub/subscriptions",
  name: "app-subscriptions",
  component: AppSubscriptions,
  meta: { requiresAuth: true, title: "App Subscriptions" + appname },
},
{
  path: "/app-hub/marketplace",
  name: "app-marketplace",
  component: AppMarketplace,
  meta: { requiresAuth: true, title: "App Marketplace" + appname },
},
```

## 🎨 Sidebar Menu Updates cần thêm

```vue
<!-- Add to Sidebar.vue -->
<div class="item mt-3">
  <menu-accordion>
    <template v-slot:icon>
      <Icon icon="mdi:application" />
    </template>
    <template v-slot:title> App Hub </template>
    <template v-slot:content>
      <router-link
        to="/app-hub"
        @click.stop
        class="w-full text-left block rounded-md p-3 hover:bg-gray-200 dark:hover:bg-gray-700"
      >
        Dashboard
      </router-link>
      <router-link
        to="/app-hub/apps"
        @click.stop
        class="w-full text-left block rounded-md p-3 hover:bg-gray-200 dark:hover:bg-gray-700"
      >
        App Registry
      </router-link>
      <router-link
        to="/app-hub/apps/create"
        @click.stop
        class="w-full text-left block rounded-md p-3 hover:bg-gray-200 dark:hover:bg-gray-700"
      >
        Create App
      </router-link>
      <router-link
        to="/app-hub/subscriptions"
        @click.stop
        class="w-full text-left block rounded-md p-3 hover:bg-gray-200 dark:hover:bg-gray-700"
      >
        Subscriptions
      </router-link>
      <router-link
        to="/app-hub/marketplace"
        @click.stop
        class="w-full text-left block rounded-md p-3 hover:bg-gray-200 dark:hover:bg-gray-700"
      >
        Marketplace
      </router-link>
    </template>
  </menu-accordion>
</div>
```

## 📱 Mobile Responsive Design

### Key Considerations:
- **Mobile-first approach** cho app management
- **Touch-friendly interfaces** cho form inputs
- **Collapsible sections** cho app details
- **Swipe actions** cho app cards
- **Bottom navigation** cho mobile app hub

## 🔐 Permissions & Security

### Role-based Access:
- **Admin**: Full access to all apps
- **Tenant Admin**: Manage apps trong tenant
- **User**: Browse marketplace, subscribe apps
- **Developer**: Register/manage own apps

## 📊 Analytics & Reporting

### Dashboard Metrics:
- App adoption rates
- Subscription revenue
- Usage statistics
- Performance metrics
- User engagement

---

**Priority**: 1. App Registry → 2. Create App → 3. App Marketplace → 4. Subscriptions
