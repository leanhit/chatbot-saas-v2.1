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
    chatWithPennyBotPublic(botId, message) {
        // Create middleware request
        const middlewareRequest = new MiddlewareRequest({
            userId: 'public-user',
            platform: 'web',
            message: message,
            botId: botId
        });
        
        return axios.post(`/penny/bots/${botId}/chat/public`, middlewareRequest.toApiRequest())
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
    }
};
