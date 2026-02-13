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
    pagination: {
      page: 1,
      size: 10,
      totalElements: 0,
      totalPages: 0
    }
  }),
  
  getters: {
    activeApps: (state) => state.apps.filter(app => app.status === 'ACTIVE'),
    inactiveApps: (state) => state.apps.filter(app => app.status === 'INACTIVE'),
    publicAppsOnly: (state) => state.apps.filter(app => app.isPublic),
    privateApps: (state) => state.apps.filter(app => !app.isPublic),
    
    getAppById: (state) => (id) => {
      return state.apps.find(app => app.id === id);
    },
    
    getSubscriptionById: (state) => (id) => {
      return state.subscriptions.find(sub => sub.id === id);
    },
    
    activeSubscriptions: (state) => {
      return state.subscriptions.filter(sub => sub.status === 'ACTIVE');
    },
    
    expiredSubscriptions: (state) => {
      return state.subscriptions.filter(sub => sub.status === 'EXPIRED');
    }
  },
  
  actions: {
    // App Management
    async fetchApps(params = {}) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await appApi.getApps(params);
        
        if (response.data.content) {
          // Paginated response
          this.apps = response.data.content;
          this.pagination = {
            page: response.data.page || 1,
            size: response.data.size || 10,
            totalElements: response.data.totalElements || 0,
            totalPages: response.data.totalPages || 0
          };
        } else {
          // Simple array response
          this.apps = Array.isArray(response.data) ? response.data : [];
        }
        
        return response.data;
      } catch (error) {
        this.error = error.response?.data?.message || error.message;
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    async getApp(id) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await appApi.getApp(id);
        this.currentApp = response.data;
        return response.data;
      } catch (error) {
        this.error = error.response?.data?.message || error.message;
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    async createApp(appData) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await appApi.createApp(appData);
        
        // Add to apps array if successful
        if (response.data) {
          this.apps.unshift(response.data);
        }
        
        return response.data;
      } catch (error) {
        this.error = error.response?.data?.message || error.message;
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    async updateApp(id, appData) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await appApi.updateApp(id, appData);
        
        // Update in apps array
        const index = this.apps.findIndex(app => app.id === id);
        if (index !== -1) {
          this.apps[index] = { ...this.apps[index], ...response.data };
        }
        
        // Update currentApp if it's the same
        if (this.currentApp && this.currentApp.id === id) {
          this.currentApp = { ...this.currentApp, ...response.data };
        }
        
        return response.data;
      } catch (error) {
        this.error = error.response?.data?.message || error.message;
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    async deleteApp(id) {
      this.loading = true;
      this.error = null;
      
      try {
        await appApi.deleteApp(id);
        
        // Remove from apps array
        this.apps = this.apps.filter(app => app.id !== id);
        
        // Clear currentApp if it's the same
        if (this.currentApp && this.currentApp.id === id) {
          this.currentApp = null;
        }
        
        return true;
      } catch (error) {
        this.error = error.response?.data?.message || error.message;
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    async searchApps(params) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await appApi.searchApps(params);
        
        if (response.data.content) {
          this.apps = response.data.content;
          this.pagination = {
            page: response.data.page || 1,
            size: response.data.size || 10,
            totalElements: response.data.totalElements || 0,
            totalPages: response.data.totalPages || 0
          };
        } else {
          this.apps = Array.isArray(response.data) ? response.data : [];
        }
        
        return response.data;
      } catch (error) {
        this.error = error.response?.data?.message || error.message;
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    // Subscription Management
    async fetchSubscriptions(params = {}) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await appApi.getSubscriptions(params);
        
        if (response.data.content) {
          this.subscriptions = response.data.content;
        } else {
          this.subscriptions = Array.isArray(response.data) ? response.data : [];
        }
        
        return response.data;
      } catch (error) {
        this.error = error.response?.data?.message || error.message;
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    async getSubscription(id) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await appApi.getSubscription(id);
        return response.data;
      } catch (error) {
        this.error = error.response?.data?.message || error.message;
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    async subscribeToApp(subscriptionData) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await appApi.subscribeToApp(subscriptionData);
        
        // Add to subscriptions array
        if (response.data) {
          this.subscriptions.unshift(response.data);
        }
        
        return response.data;
      } catch (error) {
        this.error = error.response?.data?.message || error.message;
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    async cancelSubscription(id) {
      this.loading = true;
      this.error = null;
      
      try {
        await appApi.cancelSubscription(id);
        
        // Remove from subscriptions array
        this.subscriptions = this.subscriptions.filter(sub => sub.id !== id);
        
        return true;
      } catch (error) {
        this.error = error.response?.data?.message || error.message;
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    async renewSubscription(id) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await appApi.renewSubscription(id);
        
        // Update in subscriptions array
        const index = this.subscriptions.findIndex(sub => sub.id === id);
        if (index !== -1) {
          this.subscriptions[index] = { ...this.subscriptions[index], ...response.data };
        }
        
        return response.data;
      } catch (error) {
        this.error = error.response?.data?.message || error.message;
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    // Marketplace
    async fetchPublicApps(params = {}) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await appApi.getPublicApps(params);
        
        if (response.data.content) {
          this.publicApps = response.data.content;
        } else {
          this.publicApps = Array.isArray(response.data) ? response.data : [];
        }
        
        return response.data;
      } catch (error) {
        this.error = error.response?.data?.message || error.message;
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    async getPublicAppDetails(id) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await appApi.getAppDetails(id);
        return response.data;
      } catch (error) {
        this.error = error.response?.data?.message || error.message;
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    // Analytics
    async getAppAnalytics(id) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await appApi.getAppAnalytics(id);
        return response.data;
      } catch (error) {
        this.error = error.response?.data?.message || error.message;
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    async getSubscriptionAnalytics() {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await appApi.getSubscriptionAnalytics();
        return response.data;
      } catch (error) {
        this.error = error.response?.data?.message || error.message;
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    // Guard Evaluation
    async evaluateGuard(appId, context, requestData) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await appApi.evaluateGuard(appId, context, requestData);
        return response.data;
      } catch (error) {
        this.error = error.response?.data?.message || error.message;
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    // Utility methods
    clearError() {
      this.error = null;
    },
    
    resetState() {
      this.apps = [];
      this.currentApp = null;
      this.subscriptions = [];
      this.publicApps = [];
      this.loading = false;
      this.error = null;
      this.pagination = {
        page: 1,
        size: 10,
        totalElements: 0,
        totalPages: 0
      };
    },
    
    // Bulk operations
    async bulkUpdateAppStatus(appIds, status) {
      this.loading = true;
      this.error = null;
      
      try {
        const promises = appIds.map(id => this.updateApp(id, { status }));
        await Promise.all(promises);
        return true;
      } catch (error) {
        this.error = error.response?.data?.message || error.message;
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    async bulkDeleteApps(appIds) {
      this.loading = true;
      this.error = null;
      
      try {
        const promises = appIds.map(id => this.deleteApp(id));
        await Promise.all(promises);
        return true;
      } catch (error) {
        this.error = error.response?.data?.message || error.message;
        throw error;
      } finally {
        this.loading = false;
      }
    }
  }
});
