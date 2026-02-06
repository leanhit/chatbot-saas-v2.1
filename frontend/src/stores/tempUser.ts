import { ref } from 'vue';
import { defineStore } from 'pinia';
import { tempUsersApi } from '@/api/tempUserApi';

export const useTempUsersStore = defineStore('useTempUsersStore', () => {
  const tempUsers = ref([]);
  const total = ref(0);
  const isLoading = ref(false);
  const error = ref(null);

  // Vẫn giữ đối tượng pagination để lưu trạng thái và chuẩn bị cho tương lai
  const pagination = ref({
    page: 1,
    limit: 10,
    sortBy: 'created_at',
    sortOrder: 'desc',
    search: ''
  });

  /**
   * Lấy danh sách người dùng tạm thời.
   * LƯU Ý: Backend hiện tại chỉ trả về TOÀN BỘ danh sách, không dùng tham số phân trang.
   * Tham số 'overrides' được giữ lại cho mục đích chuẩn bị nâng cấp sau này.
   */
  async function fetchTempUsers(overrides = {}) {
    isLoading.value = true;
    error.value = null;

    try {
      // **CHỈNH SỬA TẠI ĐÂY:** // Gọi API mà KHÔNG TRUYỀN các tham số phân trang (page, limit,...)
      // vì Backend hiện tại (TempUserController.java) không nhận chúng.
      const response = await tempUsersApi.getAllTempUser();

      //console.log('API fetchPhones response:', response.data);
      let data = [];
      // Chuẩn hóa dữ liệu trả về (dự kiến là List<FbCustomerStaging>)
      if (Array.isArray(response.data)) {
        data = response.data;
      } else if (response.data?.data) {
        data = response.data.data;
      }

      // Decode JSON fields an toàn
      data = data.map(item => {
        let parsedJson = {};
        if (typeof item.dataJson === 'string' && item.dataJson.trim().startsWith('{')) {
          try {
            parsedJson = JSON.parse(item.dataJson);
          } catch (e) {
            console.warn(`⚠️ Lỗi parse JSON cho PSID ${item.psid}:`, e, item.dataJson);
          }
        }

        // Chuẩn hóa thời gian (không cần thiết phải convert sang Date object 
        // trừ khi cần xử lý Date phức tạp ở frontend)

        return {
          ...item,
          ...parsedJson, // merge decoded fields (e.g., phone, name)
        };
      });

      tempUsers.value = data;
      // Vì không có phân trang, total chính là số lượng bản ghi nhận được
      total.value = data.length;

      // Cập nhật lại pagination sau khi fetch thành công (chỉ cập nhật limit/page
      // dựa trên dữ liệu hiện tại, vì không có thông tin tổng thể từ server)
      if (data.length > 0) {
        // Cập nhật limit = số lượng record đã load
        pagination.value.limit = data.length;
      }
      pagination.value.page = 1; // Vì luôn load toàn bộ

    } catch (err) {
      console.error('❌ Lỗi khi fetch temp users:', err);
      error.value = err.message || 'Lỗi không xác định khi tải dữ liệu người dùng tạm thời.';
    } finally {
      isLoading.value = false;
    }
  }

  async function fetchPhones(overrides = {}) {
    isLoading.value = true;
    error.value = null;

    try {
      // 1. Gọi API (API bây giờ trả về List<FbCapturedPhone>)
      const response = await tempUsersApi.getAllPhone();
      let data = [];

      // 2. Chuẩn hóa dữ liệu trả về (Dữ liệu dự kiến: List<FbCapturedPhone>)
      // Logic này tương tự như fetchTempUsers vì cả hai đều trả về List<Object>
      if (Array.isArray(response.data)) {
        data = response.data;
      } else if (response.data?.data && Array.isArray(response.data.data)) {
        data = response.data.data;
      } else {
        console.warn("⚠️ API fetchPhones trả về không phải mảng đối tượng FbCapturedPhone trực tiếp.", response);
        data = [];
      }

      // 3. Xử lý/Chuẩn hóa dữ liệu (Nếu cần thiết, ví dụ chuyển đổi Date/Time, nhưng không cần parse dataJson)
      const phoneRecords = data.map(item => {
        // Vì item.dataJson không còn là nguồn dữ liệu chính, ta chỉ cần chuẩn hóa item
        // Nếu bạn muốn hiển thị ngày giờ đẹp hơn, có thể làm ở đây:
        // item.createdAtDisplay = new Date(item.createdAt).toLocaleString();

        return item; // Trả về đối tượng FbCapturedPhone đầy đủ
      });

      // Cập nhật state
      tempUsers.value = phoneRecords; // Lưu danh sách đối tượng FbCapturedPhone
      total.value = phoneRecords.length;

      // Cập nhật phân trang
      if (phoneRecords.length > 0) {
        pagination.value.limit = phoneRecords.length;
      }
      pagination.value.page = 1;

    } catch (err) {
      console.error('❌ Lỗi khi fetch phone records:', err);
      error.value = err.message || 'Lỗi không xác định khi tải dữ liệu bản ghi số điện thoại.';
    } finally {
      isLoading.value = false;
    }
  }

  function setPagination(newParams) {
    pagination.value = { ...pagination.value, ...newParams };
  }

  return {
    tempUsers,
    total,
    isLoading,
    error,
    pagination,
    fetchTempUsers,
    setPagination,
    fetchPhones
  };
});
