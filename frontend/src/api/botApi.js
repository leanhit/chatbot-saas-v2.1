import axios from '@/plugins/axios';
import router from '@/router';
// Hàm xử lý lỗi chung cho các request
const handleApiError = (error) => {
    if (error.response && error.response.status === 401) {
        alert('Phiên đăng nhập của bạn đã hết hạn. Vui lòng đăng nhập lại.');
        // Chuyển hướng người dùng đến trang đăng nhập
        router.push('/login');
    }
    // Ném lại lỗi để các component gọi hàm này có thể xử lý
    throw error;
};
export const botApi = {
    /**
     * Lấy danh sách tất cả các bot của người dùng hiện tại.
     */
    getMyBots() {
        return axios.get('/bots/my')
            .catch(handleApiError);
    },
    /**
     * Lấy thông tin chi tiết của một bot theo ID.
     */
    getBotById(botId) {
        return axios.get(`/bots/${botId}`)
            .catch(handleApiError);
    },
    /**
     * Tạo một bot mới.
     */
    createBot(botData) {
        return axios.post('/bots', botData)
            .catch(handleApiError);
    },
    /**
     * Cập nhật thông tin một bot.
     */
    updateBot(botId, botData) {
        return axios.put(`/bots/${botId}`, botData)
            .catch(handleApiError);
    },
    /**
     * Xóa một bot.
     */
    deleteBot(botId) {
        return axios.delete(`/bots/${botId}`)
            .catch(handleApiError);
    },
    /**
     * Kích hoạt một bot.
     */
    activateBot(botId) {
        return axios.post(`/bots/${botId}/activate`)
            .catch(handleApiError);
    },
    /**
     * Vô hiệu hóa một bot.
     */
    deactivateBot(botId) {
        return axios.post(`/bots/${botId}/deactivate`)
            .catch(handleApiError);
    },
    /**
     * Lấy thống kê của bot.
     */
    getBotStatistics(botId) {
        return axios.get(`/bots/${botId}/statistics`)
            .catch(handleApiError);
    },
    /**
     * Lấy danh sách templates cho bot.
     */
    getBotTemplates() {
        return axios.get('/bots/templates')
            .catch(handleApiError);
    },
    /**
     * Tạo bot từ template.
     */
    createBotFromTemplate(templateId, botData) {
        return axios.post(`/bots/templates/${templateId}/create`, botData)
            .catch(handleApiError);
    },
    /**
     * Lấy mã nhúng web cho bot.
     */
    getWebEmbedCode(botId) {
        return axios.get(`/bots/${botId}/embed`)
            .catch(handleApiError);
    },
    /**
     * Kiểm tra tên bot có tồn tại không.
     */
    checkBotNameExists(name) {
        return axios.get(`/bots/check-name?name=${encodeURIComponent(name)}`)
            .catch(handleApiError);
    },
    /**
     * Tạo rule cho bot.
     */
    createBotRule(botId, ruleData) {
        return axios.post(`/bots/${botId}/rules`, ruleData)
            .catch(handleApiError);
    },
    /**
     * Lấy danh sách rules của bot.
     */
    getBotRules(botId) {
        return axios.get(`/bots/${botId}/rules`)
            .catch(handleApiError);
    },
    /**
     * Cập nhật rule của bot.
     */
    updateBotRule(botId, ruleId, ruleData) {
        return axios.put(`/bots/${botId}/rules/${ruleId}`, ruleData)
            .catch(handleApiError);
    },
    /**
     * Xóa rule của bot.
     */
    deleteBotRule(botId, ruleId) {
        return axios.delete(`/bots/${botId}/rules/${ruleId}`)
            .catch(handleApiError);
    },
    /**
     * Test rule của bot.
     */
    testBotRule(botId, ruleId, testData) {
        return axios.post(`/bots/${botId}/rules/${ruleId}/test`, testData)
            .catch(handleApiError);
    }
};
