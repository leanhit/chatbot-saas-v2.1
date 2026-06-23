// src/api/presenceApi.js
import axios from '@/plugins/axios';

export const presenceApi = {
  // -------------------------
  // Get Online Members for Tenant
  // ENDPOINT: GET /api/presence/tenants/key/{tenantKey}/members/online
  // -------------------------
  getOnlineMembers(tenantKey) {
    return axios.get(`/presence/tenants/key/${tenantKey}/members/online`);
  }
};

export default presenceApi;
