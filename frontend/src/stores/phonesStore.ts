import { ref } from 'vue';
import { defineStore } from 'pinia';
import { tempUsersApi } from '@/api/tempUserApi';

export const usePhonesStore = defineStore('usePhonesStore', () => {
  const phonesList = ref([]);
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
      phonesList.value = phoneRecords; // Lưu danh sách đối tượng FbCapturedPhone
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
    phonesList,
    total,
    isLoading,
    error,
    pagination,
    setPagination,
    fetchPhones
  };
});
