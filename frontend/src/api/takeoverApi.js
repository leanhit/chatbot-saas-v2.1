// src/api/takeoverApi.js
import axios from '@/plugins/axios';
export const appApi = {
    // -------------------------
    // 1. Lấy Danh sách Conversations (MẶC ĐỊNH - Lấy tất cả)
    // ENDPOINT: GET /api/conversations
    // -------------------------
    getConversations(params) {
        // Gọi đến ConversationController
        return axios.get('/conversations', { params });
    },
    // -------------------------
    // 1.1. LỌC THEO CONNECTION ID
    // ENDPOINT: GET /api/conversations/by-owner-connection?ownerId={id}&connectionId={id}
    // -------------------------
    getConversationsByConnectionId(connectionId, params) {
        return axios.get('/conversations/by-owner-connection', {
            params: { connectionId, ...params }
        });
    },
    // -------------------------
    // 1.2. LỌC THEO OWNER ID
    // ENDPOINT: GET /api/conversations/by-owner?ownerId={id}
    // -------------------------
    getConversationsByOwnerId(params) {
        return axios.get('/conversations/by-owner', { params });
    },
    // -------------------------
    // 2. Lấy Lịch sử Tin nhắn DB (messStore)
    // ENDPOINT: GET /api/messages?conversationId={id}
    // -------------------------
    getMessagesByConversationId(conversationId, params) {
        return axios.get('/messages', {
            params: { conversationId, ...params }
        });
    },
    // -------------------------
    // 3. Gửi Tin nhắn qua Takeover
    // ENDPOINT: POST /api/takeover/send
    // -------------------------
    sendMessage(payload) {
        return axios.post('/takeover/send', payload);
    },
    // -------------------------
    // 4. Takeover Conversation
    // ENDPOINT: POST /api/takeover/take/{conversationId}
    // -------------------------
    takeOverConversation(conversationId) {
        return axios.post(`/takeover/take/${conversationId}`);
    },
    // -------------------------
    // 5. Release Conversation
    // ENDPOINT: POST /api/takeover/release/{conversationId}
    // -------------------------
    releaseConversation(conversationId) {
        return axios.post(`/takeover/release/${conversationId}`);
    },
    // -------------------------
    // 6. Get Takeover Status
    // ENDPOINT: GET /api/takeover/status/{conversationId}
    // -------------------------
    getTakeoverStatus(conversationId) {
        return axios.get(`/takeover/status/${conversationId}`);
    },
    // -------------------------
    // 7. Get Active Takeovers
    // ENDPOINT: GET /api/takeover/active
    // -------------------------
    getActiveTakeovers() {
        return axios.get('/takeover/active');
    },
    // -------------------------
    // 8. Get Conversation Statistics
    // ENDPOINT: GET /api/conversations/statistics
    // -------------------------
    getConversationStatistics(params) {
        return axios.get('/conversations/statistics', { params });
    },
    // -------------------------
    // 9. Search Conversations
    // ENDPOINT: GET /api/conversations/search
    // -------------------------
    searchConversations(searchParams) {
        return axios.get('/conversations/search', { params: searchParams });
    },
    // -------------------------
    // 10. Get Conversation Details
    // ENDPOINT: GET /api/conversations/{id}
    // -------------------------
    getConversationById(conversationId) {
        return axios.get(`/conversations/${conversationId}`);
    },
    // -------------------------
    // 11. Update Conversation
    // ENDPOINT: PUT /api/conversations/{id}
    // -------------------------
    updateConversation(conversationId, updateData) {
        return axios.put(`/conversations/${conversationId}`, updateData);
    },
    // -------------------------
    // 12. Delete Conversation
    // ENDPOINT: DELETE /api/conversations/{id}
    // -------------------------
    deleteConversation(conversationId) {
        return axios.delete(`/conversations/${conversationId}`);
    }
};
