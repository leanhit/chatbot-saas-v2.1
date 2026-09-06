import axios from '@/plugins/axios';
import router from '@/router';
import {
  PennyBotDto,
  PennyBotRequest,
  PennyBotResponse,
  MiddlewareRequest,
  MiddlewareResponse
} from '@/types/penny';

// Hàm xử lý lỗi chung cho các request
const handleApiError = (error) => {
    if (error.response && error.response.status === 401) {
        alert('Phiên đăng nhập của bạn đã hết hạn. Vui lòng đăng nhập lại.');
        router.push('/login');
    }
    throw error;
};

export const pennyApi = {
    /**
     * Lấy danh sách tất cả các Penny bots của người dùng hiện tại
     */
    getMyPennyBots() {
        return axios.get('/penny/bots')
            .then(response => {
                // Convert API responses to DTOs
                response.data = response.data.map(bot => new PennyBotDto(bot));
                return response;
            })
            .catch(handleApiError);
    },

    /**
     * Lấy thông tin chi tiết của một Penny bot theo ID
     */
    getPennyBotById(botId) {
        return axios.get(`/penny/bots/${botId}`)
            .then(response => {
                // Convert API response to DTO
                response.data = new PennyBotDto(response.data);
                return response;
            })
            .catch(handleApiError);
    },

    /**
     * Tạo một Penny bot mới
     */
    createPennyBot(botData) {
        // Backend expects Map<String, String>, not PennyBotRequest
        // Remove validation and DTO conversion for now
        return axios.post('/penny/bots', botData)
            .then(response => {
                // Convert API response to DTO
                response.data = new PennyBotDto(response.data);
                return response;
            })
            .catch(handleApiError);
    },

    /**
     * Cập nhật thông tin Penny bot
     */
    updatePennyBot(botId, botData) {
        // Backend expects Map<String, String>, not PennyBotRequest
        return axios.put(`/penny/bots/${botId}`, botData)
            .then(response => {
                // Convert API response to DTO
                response.data = new PennyBotDto(response.data);
                return response;
            })
            .catch(handleApiError);
    },

    /**
     * Xóa một Penny bot
     */
    deletePennyBot(botId) {
        return axios.delete(`/penny/bots/${botId}`)
            .catch(handleApiError);
    },

    /**
     * Toggle trạng thái Penny bot (active/inactive)
     */
    togglePennyBotStatus(botId, enabled) {
        return axios.put(`/penny/bots/${botId}/toggle`, null, {
            params: { enabled }
        })
            .then(response => {
                // Convert API response to DTO
                response.data = new PennyBotDto(response.data);
                return response;
            })
            .catch(handleApiError);
    },

    /**
     * Lấy health status của Penny bot
     */
    getPennyBotHealth(botId) {
        return axios.get(`/penny/bots/${botId}/health`)
            .catch(handleApiError);
    },

    /**
     * Lấy analytics của Penny bot
     */
    getPennyBotAnalytics(botId, timeRange = '7days') {
        return axios.get(`/penny/bots/${botId}/analytics`, {
            params: { timeRange }
        })
            .catch(handleApiError);
    },

    /**
     * Chat với Penny bot (cần authentication)
     */
    chatWithPennyBot(botId, message, isTestMode = false) {
        // Backend expects simple Map<String, String> with message and testMode
        const request = {
            message: message,
            testMode: isTestMode ? 'true' : 'false'
        };
        
        return axios.post(`/penny/bots/${botId}/chat`, request)
            .then(response => {
                // Return response directly as is
                return response;
            })
            .catch(handleApiError);
    },

    /**
     * Chat với Penny bot (public, không cần authentication)
     */
    chatWithPennyBotPublic(botId, message, apiKey) {
        // Create middleware request
        const middlewareRequest = new MiddlewareRequest({
            userId: 'public-user',
            platform: 'web',
            message: message,
            botId: botId
        });
        
        return axios.post(`/penny/bots/${botId}/chat/public`, middlewareRequest.toApiRequest(), {
            headers: {
                'X-Public-API-Key': apiKey
            }
        })
            .then(response => {
                // Convert API response to DTO
                response.data = new MiddlewareResponse(response.data);
                return response;
            })
            .catch(handleApiError);
    },

    /**
     * Auto-create Penny bot cho Facebook connection
     */
    autoCreatePennyBot(pageId) {
        return axios.post('/penny/bots/auto', { pageId })
            .then(response => {
                // Convert API response to DTO
                response.data = new PennyBotResponse(response.data);
                return response;
            })
            .catch(handleApiError);
    },

    // ========================================
    // Knowledge Base API
    // ========================================

    /**
     * Get all knowledge articles for a bot (paginated)
     */
    getKnowledgeArticles(botId, page = 0, size = 20, sortBy = 'updatedAt', sortDir = 'desc') {
        return axios.get(`/penny/bots/${botId}/kb/articles`, {
            params: { page, size, sortBy, sortDir }
        }).catch(handleApiError);
    },

    /**
     * Get a specific knowledge article
     */
    getKnowledgeArticle(botId, articleId) {
        return axios.get(`/penny/bots/${botId}/kb/articles/${articleId}`)
            .catch(handleApiError);
    },

    /**
     * Create a new knowledge article
     */
    createKnowledgeArticle(botId, articleData) {
        return axios.post(`/penny/bots/${botId}/kb/articles`, articleData)
            .catch(handleApiError);
    },

    /**
     * Update a knowledge article
     */
    updateKnowledgeArticle(botId, articleId, articleData) {
        return axios.put(`/penny/bots/${botId}/kb/articles/${articleId}`, articleData)
            .catch(handleApiError);
    },

    /**
     * Delete a knowledge article
     */
    deleteKnowledgeArticle(botId, articleId) {
        return axios.delete(`/penny/bots/${botId}/kb/articles/${articleId}`)
            .catch(handleApiError);
    },

    /**
     * Import multiple knowledge articles in bulk
     */
    importKnowledgeArticles(botId, articles) {
        return axios.post(`/penny/bots/${botId}/kb/import`, articles)
            .catch(handleApiError);
    },

    /**
     * Re-generate embedding for an article
     */
    reembedKnowledgeArticle(botId, articleId) {
        return axios.post(`/penny/bots/${botId}/kb/articles/${articleId}/reembed`)
            .catch(handleApiError);
    },

    /**
     * Test knowledge base search
     */
    testKnowledgeBaseSearch(botId, query) {
        return axios.get(`/penny/bots/${botId}/kb/search`, {
            params: { q: query }
        }).catch(handleApiError);
    },

    /**
     * Get knowledge base statistics
     */
    getKnowledgeBaseStats(botId) {
        return axios.get(`/penny/bots/${botId}/kb/stats`)
            .catch(handleApiError);
    },

    // ========================================
    // Metrics API
    // ========================================

    /**
     * Get system metrics
     */
    getSystemMetrics() {
        return axios.get('/penny/admin/metrics')
            .catch(handleApiError);
    },

    /**
     * Get metrics for a specific bot
     */
    getBotMetrics(botId) {
        return axios.get(`/penny/admin/metrics/bot/${botId}`)
            .catch(handleApiError);
    },

    /**
     * Get provider health metrics
     */
    getProviderMetrics() {
        return axios.get('/penny/admin/metrics/providers')
            .catch(handleApiError);
    },

    /**
     * Get knowledge base metrics
     */
    getKnowledgeBaseMetrics() {
        return axios.get('/penny/admin/metrics/knowledge-base')
            .catch(handleApiError);
    },

    // ========================================
    // Escalation Tickets API
    // ========================================

    /**
     * Get all escalation tickets for a bot (paginated)
     */
    getEscalationTickets(botId, page = 0, size = 20, sortBy = 'createdAt', sortDir = 'desc') {
        return axios.get(`/penny/bots/${botId}/escalation/tickets`, {
            params: { page, size, sortBy, sortDir }
        }).catch(handleApiError);
    },

    /**
     * Get tickets by status for a bot
     */
    getEscalationTicketsByStatus(botId, status) {
        return axios.get(`/penny/bots/${botId}/escalation/tickets/status/${status}`)
            .catch(handleApiError);
    },

    /**
     * Get a specific escalation ticket
     */
    getEscalationTicket(botId, ticketId) {
        return axios.get(`/penny/bots/${botId}/escalation/tickets/${ticketId}`)
            .catch(handleApiError);
    },

    /**
     * Create a new escalation ticket
     */
    createEscalationTicket(botId, ticketData) {
        return axios.post(`/penny/bots/${botId}/escalation/tickets`, ticketData)
            .catch(handleApiError);
    },

    /**
     * Update an escalation ticket
     */
    updateEscalationTicket(botId, ticketId, ticketData) {
        return axios.put(`/penny/bots/${botId}/escalation/tickets/${ticketId}`, ticketData)
            .catch(handleApiError);
    },

    /**
     * Delete an escalation ticket
     */
    deleteEscalationTicket(botId, ticketId) {
        return axios.delete(`/penny/bots/${botId}/escalation/tickets/${ticketId}`)
            .catch(handleApiError);
    },

    /**
     * Assign ticket to an agent
     */
    assignEscalationTicket(botId, ticketId, agentId) {
        return axios.post(`/penny/bots/${botId}/escalation/tickets/${ticketId}/assign`, { agentId })
            .catch(handleApiError);
    },

    /**
     * Resolve a ticket
     */
    resolveEscalationTicket(botId, ticketId, notes) {
        return axios.post(`/penny/bots/${botId}/escalation/tickets/${ticketId}/resolve`, { notes })
            .catch(handleApiError);
    },

    /**
     * Cancel a ticket
     */
    cancelEscalationTicket(botId, ticketId) {
        return axios.post(`/penny/bots/${botId}/escalation/tickets/${ticketId}/cancel`)
            .catch(handleApiError);
    },

    /**
     * Get pending tickets for a bot
     */
    getPendingEscalationTickets(botId) {
        return axios.get(`/penny/bots/${botId}/escalation/tickets/pending`)
            .catch(handleApiError);
    },

    /**
     * Get escalation ticket statistics
     */
    getEscalationStats(botId) {
        return axios.get(`/penny/bots/${botId}/escalation/stats`)
            .catch(handleApiError);
    },

    // ========================================
    // Monitoring & Metrics API (New)
    // ========================================

    /**
     * Get Penny metrics summary
     */
    getPennyMetricsSummary() {
        return axios.get('/penny/admin/metrics/summary')
            .catch(handleApiError);
    },

    /**
     * Get circuit breaker status
     */
    getCircuitBreakerStatus() {
        return axios.get('/penny/admin/circuit-breaker/status')
            .catch(handleApiError);
    },

    /**
     * Get analytics events for a bot
     */
    getAnalyticsEvents(botId, timeRange = '7days', page = 0, size = 50) {
        return axios.get(`/penny/admin/analytics/events`, {
            params: { botId, timeRange, page, size }
        }).catch(handleApiError);
    },

    /**
     * Get analytics summary for a bot
     */
    getAnalyticsSummary(botId, timeRange = '7days') {
        return axios.get(`/penny/admin/analytics/summary`, {
            params: { botId, timeRange }
        }).catch(handleApiError);
    },

    // ========================================
    // Knowledge Document API (New for Penny AI Engine 2.0)
    // ========================================

    /**
     * Upload a knowledge document
     */
    uploadKnowledgeDocument(file, botId, tenantId, documentName, uploadedBy) {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('botId', botId);
        formData.append('tenantId', tenantId);
        formData.append('documentName', documentName);
        if (uploadedBy) {
            formData.append('uploadedBy', uploadedBy);
        }

        return axios.post('/penny/knowledge-base/upload', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        }).catch(handleApiError);
    },

    /**
     * Get all documents for a bot
     */
    getKnowledgeDocuments(botId, tenantId) {
        return axios.get('/penny/knowledge-base/documents', {
            params: { botId, tenantId }
        }).catch(handleApiError);
    },

    /**
     * Get a specific document by ID
     */
    getKnowledgeDocument(documentId) {
        return axios.get(`/penny/knowledge-base/documents/${documentId}`)
            .catch(handleApiError);
    },

    /**
     * Delete a document
     */
    deleteKnowledgeDocument(documentId) {
        return axios.delete(`/penny/knowledge-base/documents/${documentId}`)
            .catch(handleApiError);
    },

    /**
     * Delete all documents for a bot
     */
    deleteKnowledgeDocumentsByBot(botId) {
        return axios.delete(`/penny/knowledge-base/documents/bot/${botId}`)
            .catch(handleApiError);
    },

    /**
     * Re-process a document
     */
    reprocessKnowledgeDocument(documentId, file) {
        const formData = new FormData();
        formData.append('file', file);

        return axios.post(`/penny/knowledge-base/documents/${documentId}/reprocess`, formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        }).catch(handleApiError);
    }
};
