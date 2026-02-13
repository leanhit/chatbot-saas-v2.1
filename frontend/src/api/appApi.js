import axios from './index';

const appApi = {
  // App Registry
  getApps: (params = {}) => {
    const queryParams = new URLSearchParams();
    if (params.page) queryParams.append('page', params.page);
    if (params.size) queryParams.append('size', params.size);
    if (params.sortBy) queryParams.append('sortBy', params.sortBy);
    if (params.sortDirection) queryParams.append('sortDirection', params.sortDirection);
    if (params.nameFilter) queryParams.append('nameFilter', params.nameFilter);
    if (params.appType) queryParams.append('appType', params.appType);
    if (params.status) queryParams.append('status', params.status);
    if (params.isActive !== undefined) queryParams.append('isActive', params.isActive);
    
    const url = queryParams.toString() ? `/api/v1/apps?${queryParams.toString()}` : '/api/v1/apps';
    return axios.get(url);
  },
  
  getApp: (id) => axios.get(`/api/v1/apps/${id}`),
  
  createApp: (data) => {
    return axios.post('/api/v1/apps', data);
  },
  
  updateApp: (id, data) => {
    return axios.put(`/api/v1/apps/${id}`, data);
  },
  
  deleteApp: (id) => {
    return axios.delete(`/api/v1/apps/${id}`);
  },
  
  searchApps: (params) => {
    const queryParams = new URLSearchParams();
    if (params.nameFilter) queryParams.append('nameFilter', params.nameFilter);
    if (params.appType) queryParams.append('appType', params.appType);
    if (params.status) queryParams.append('status', params.status);
    if (params.isActive !== undefined) queryParams.append('isActive', params.isActive);
    if (params.page) queryParams.append('page', params.page);
    if (params.size) queryParams.append('size', params.size);
    
    const url = queryParams.toString() ? `/api/v1/apps/search?${queryParams.toString()}` : '/api/v1/apps/search';
    return axios.get(url);
  },
  
  // Subscriptions
  getSubscriptions: (params = {}) => {
    const queryParams = new URLSearchParams();
    if (params.page) queryParams.append('page', params.page);
    if (params.size) queryParams.append('size', params.size);
    if (params.appId) queryParams.append('appId', params.appId);
    if (params.tenantId) queryParams.append('tenantId', params.tenantId);
    if (params.status) queryParams.append('status', params.status);
    
    const url = queryParams.toString() ? `/api/v1/subscriptions?${queryParams.toString()}` : '/api/v1/subscriptions';
    return axios.get(url);
  },
  
  getSubscription: (id) => axios.get(`/api/v1/subscriptions/${id}`),
  
  subscribeToApp: (data) => {
    return axios.post('/api/v1/subscriptions', data);
  },
  
  cancelSubscription: (id) => {
    return axios.delete(`/api/v1/subscriptions/${id}`);
  },
  
  renewSubscription: (id) => {
    return axios.post(`/api/v1/subscriptions/${id}/renew`);
  },
  
  // App Marketplace
  getPublicApps: (params = {}) => {
    const queryParams = new URLSearchParams();
    if (params.page) queryParams.append('page', params.page);
    if (params.size) queryParams.append('size', params.size);
    if (params.nameFilter) queryParams.append('nameFilter', params.nameFilter);
    if (params.appType) queryParams.append('appType', params.appType);
    
    const url = queryParams.toString() ? `/api/v1/apps/public?${queryParams.toString()}` : '/api/v1/apps/public';
    return axios.get(url);
  },
  
  getAppDetails: (id) => axios.get(`/api/v1/apps/public/${id}`),
  
  // Analytics
  getAppAnalytics: (id) => axios.get(`/api/v1/apps/${id}/analytics`),
  
  getSubscriptionAnalytics: () => axios.get('/api/v1/subscriptions/analytics'),
  
  // Guard Evaluation
  evaluateGuard: (appId, context, requestData) => {
    return axios.post('/api/v1/apps/evaluate-guard', {
      appId,
      context,
      requestData
    });
  }
};

export default appApi;
