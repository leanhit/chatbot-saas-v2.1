// Compliance API — 2FA, GDPR export, data deletion requests
import axios from '@/plugins/axios'

export const complianceApi = {
  // ─── 2FA (TOTP) ───
  get2faStatus: () => axios.get('/auth/2fa/status'),
  setup2fa: () => axios.post('/auth/2fa/setup'),
  enable2fa: (code) => axios.post('/auth/2fa/enable', { code }),
  disable2fa: (code) => axios.post('/auth/2fa/disable', { code }),

  // ─── GDPR export ───
  exportTenantData: () => axios.get('/tenant/export/data', { responseType: 'blob' }),

  // ─── GDPR deletion requests ───
  listDeletionRequests: () => axios.get('/tenant/deletion-requests'),
  createDeletionRequest: (reason) => axios.post('/tenant/deletion-requests', { reason }),
  cancelDeletionRequest: (id) => axios.post(`/tenant/deletion-requests/${id}/cancel`),
  processDeletionRequest: (id) => axios.post(`/tenant/deletion-requests/${id}/process`),
}

export default complianceApi
