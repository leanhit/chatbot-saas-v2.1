import axios from '@/plugins/axios';
export const tempUsersApi = {
    /**
     * Lấy toàn bộ danh sách người dùng tạm thời của Owner hiện tại.
     * Lưu ý: Controller hiện tại không hỗ trợ phân trang/tìm kiếm.
     */
    getAllTempUser() {
        // Gọi thẳng endpoint GET /api/temp-users.
        // Backend sẽ tự động xác định OwnerId từ token.
        return axios.get(`/temp-users`);
    },
    /**
     * Lấy thông tin người dùng tạm thời cụ thể theo PSID.
     * @param {string} psid - PSID của khách hàng.
     */
    getByPsid(psid) {
        return axios.get(`/temp-users/${psid}`);
    },
    /**
     * Tạo mới hoặc cập nhật người dùng tạm thời.
     * @param {Object} customer - Đối tượng FbCustomerStaging.
     */
    upsertTempUser(customer) {
        return axios.post(`/temp-users`, customer);
    },
    /**
     * Xóa người dùng tạm thời theo PSID.
     * @param {string} psid - PSID của khách hàng.
     */
    deleteTempUser(psid) {
        return axios.delete(`/temp-users/${psid}`);
    },
    /**
     * Cập nhật người dùng tạm thời.
     */
    updateData(psid, data) {
        return axios.patch(`/temp-users/${psid}`, data);
    },
    getAllPhone() {
        // Gọi thẳng endpoint GET /api/phone-captured.
        // Backend sẽ tự động xác định OwnerId từ token.
        return axios.get(`/phone-captured`);
    },
    /**
     * Get temp users by connection
     */
    getTempUsersByConnection(connectionId) {
        return axios.get(`/temp-users/connection/${connectionId}`);
    },
    /**
     * Search temp users
     */
    searchTempUsers(searchParams) {
        return axios.get(`/temp-users/search`, { params: searchParams });
    },
    /**
     * Get temp user statistics
     */
    getTempUserStatistics() {
        return axios.get(`/temp-users/statistics`);
    }
};
