import axios from '@/plugins/axios';

export const tempUsersApi = {
    /**
     * Lấy toàn bộ danh sách người dùng tạm thời của Owner hiện tại.
     * Lưu ý: Controller hiện tại không hỗ trợ phân trang/tìm kiếm.
     */
    getAllTempUser() {
        // Gọi thẳng endpoint GET /api/odoo/temp-users.
        // Backend sẽ tự động xác định OwnerId từ token.
        return axios.get(`/odoo/temp-users`);
    },

    /**
     * Lấy thông tin người dùng tạm thời cụ thể theo PSID.
     * @param {string} psid - PSID của khách hàng.
     */
    getByPsid(psid) {
        return axios.get(`/odoo/temp-users/${psid}`);
    },

    /**
     * Tạo mới hoặc cập nhật người dùng tạm thời.
     * @param {Object} customer - Đối tượng FbCustomerStaging.
     */
    upsertTempUser(customer) {
        return axios.post(`/odoo/temp-users`, customer);
    },

    /**
     * Xóa người dùng tạm thời theo PSID.
     * @param {string} psid - PSID của khách hàng.
     */
    deleteTempUser(psid) {
        return axios.delete(`/odoo/temp-users/${psid}`);
    },

    /**
     * Cập nhật người dùng tạm thời.
     */
    updateData(psid, data) {
        console.log('Updating temp user with psid:', psid, 'and data:', data);
        return axios.patch(`/odoo/temp-users/${psid}`, data);
    },

    
    getAllPhone() {
        // Gọi thẳng endpoint GET /api/odoo/temp-users.
        // Backend sẽ tự động xác định OwnerId từ token.
        return axios.get(`/odoo/phone-captured`);
    },
};
