import axios from '@/plugins/axios';
import router from '@/router';

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
            .catch(handleApiError);
    },

    /**
     * Lấy thông tin chi tiết của một Penny bot theo ID
     */
    getPennyBotById(botId) {
        return axios.get(`/penny/bots/${botId}`)
            .catch(handleApiError);
    },

    /**
     * Tạo một Penny bot mới
     */
    createPennyBot(botData) {
        return axios.post('/penny/bots', botData)
            .catch(handleApiError);
    },

    /**
     * Cập nhật thông tin Penny bot
     */
    updatePennyBot(botId, botData) {
        return axios.put(`/penny/bots/${botId}`, botData)
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
    chatWithPennyBot(botId, message) {
        return axios.post(`/penny/bots/${botId}/chat`, { message })
            .catch(handleApiError);
    },

    /**
     * Chat với Penny bot (public, không cần authentication)
     */
    chatWithPennyBotPublic(botId, message) {
        return axios.post(`/penny/bots/${botId}/chat/public`, { message })
            .catch(handleApiError);
    },

    /**
     * Auto-create Penny bot cho Facebook connection
     */
    autoCreatePennyBot(pageId) {
        return axios.post('/penny/bots/auto', { pageId })
            .catch(handleApiError);
    }
};
