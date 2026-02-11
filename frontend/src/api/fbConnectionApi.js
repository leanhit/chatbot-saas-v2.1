// src/api/fbConnectionApi.js
import axios from '@/plugins/axios';
export const fbConnectionApi = {
    getConfigByID(connectionId) {
        return axios.get(`/connection/facebook/${connectionId}`);
    },
    updateConfig(connectionId, params) {
        return axios.put(`/connection/facebook/${connectionId}`, params);
    },
    deleteConfig(connectionId) {
        return axios.delete(`/connection/facebook/${connectionId}`);
    },
    getAllConnections(params = {}) {
        return axios.get(`/connection/facebook`, { params });
    },
    getConnectionsAll(params = {}) {
        return axios.get(`/connection/facebook/all`, { params });
    },
    addConnection(params) {
        return axios.post(`/connection/facebook`, params);
    },
    // API mới để thêm nhiều kết nối, làm trên server 
    addConnections(params) {
        // Gửi một token tới server
        return axios.post(`/connection/facebook/auto-connect`, params);
    },
    // Test Facebook connection
    testConnection(connectionId) {
        return axios.post(`/connection/facebook/${connectionId}/test`);
    },
    // Get Facebook pages info
    getFacebookPages(accessToken) {
        return axios.get(`/connection/facebook/pages`, {
            params: { access_token: accessToken }
        });
    },
    // Connect Facebook page to bot
    connectPageToBot(pageId, botId) {
        return axios.post(`/connection/facebook/connect-page`, {
            pageId,
            botId
        });
    },
    // Disconnect Facebook page
    disconnectPage(pageId) {
        return axios.post(`/connection/facebook/disconnect-page/${pageId}`);
    },
    // Get connection status
    getConnectionStatus(connectionId) {
        return axios.get(`/connection/facebook/${connectionId}/status`);
    }
};
