import { useI18n } from 'vue-i18n';
import { ref, reactive, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
// Đã sử dụng các hàm này từ file bạn cung cấp:
import { filterDataFunction, splitData, formatDateTime } from '@/utils/search';
import { tempUsersApi } from '@/api/tempUserApi'; // API cho Temp Users
import { useTempUsersStore } from '@/stores/tempUser';
import { useSearchStore } from '@/stores/search';
import PhoneListModal from '@/views/phone-review/temp-user/components/PhoneListModal.vue';

export default {
    components: { PhoneListModal },
    props: ['viewSettings'],
    emits: ['onChangeView'],
    setup(props, context) {
        const { t } = useI18n();
        const tempUserStore = useTempUsersStore();
        const searchStore = useSearchStore();

        const filterData = ref('');
        const filter = ref('ALL');
        const listItems = ref([]);
        const isLoading = ref(false);
        const tempList = ref([]); // Danh sách đầy đủ từ API (dữ liệu gốc)

        // ✨ THÊM MỚI: Dữ liệu hiện tại cần áp dụng phân trang (gốc HOẶC đã lọc)
        const paginatedData = ref([]);

        const pagePagination = reactive({
            pageSize: 15,
            currentPage: 1,
            totalItems: 0,
        });

        const showPhoneModal = ref(false);
        const selectedPhones = ref<any[]>([]); // Đổi kiểu dữ liệu thành Array<any> hoặc Array<Object>

        function openPhoneModal(phones: any) {
            //console.log('--- LOG CHA: Opening phone modal with RAW phones:', phones);

            const phonesToDisplay: { phone: string }[] = [];
            let processedPhones = phones;

            // 💡 BƯỚC THÊM VÀO: Kiểm tra và phân tích chuỗi JSON nếu cần
            if (typeof phones === 'string') {
                try {
                    const parsed = JSON.parse(phones);
                    // Chỉ sử dụng kết quả phân tích nếu nó là một mảng
                    if (Array.isArray(parsed)) {
                        processedPhones = parsed;
                    }
                } catch (e) {
                    // Trường hợp chuỗi không phải là JSON hợp lệ (ví dụ: chỉ là một số điện thoại đơn lẻ)
                    console.warn('--- LOG CHA: Lỗi khi phân tích chuỗi điện thoại:', e);
                    // Có thể thêm logic xử lý chuỗi đơn lẻ ở đây nếu muốn
                }
            }

            // Logic xử lý mảng như ban đầu, sử dụng processedPhones
            if (Array.isArray(processedPhones)) {
                for (const phoneString of processedPhones) {
                    // Đảm bảo phần tử trong mảng là chuỗi và không rỗng
                    if (typeof phoneString === 'string' && phoneString.trim() !== '') {
                        phonesToDisplay.push({ phone: phoneString });
                    }
                }
            }

            // (Tùy chọn) Xử lý trường hợp đầu vào là chuỗi điện thoại đơn lẻ (không phải mảng/JSON)
            else if (typeof processedPhones === 'string' && processedPhones.trim() !== '') {
                phonesToDisplay.push({ phone: processedPhones });
            }

            selectedPhones.value = phonesToDisplay;
            showPhoneModal.value = true;

            //console.log('--- LOG CHA: Transformed phones for modal:', selectedPhones.value);
        }

        function closePhoneModal() {
            showPhoneModal.value = false;
        }

        // ---------------------------------------------
        // HÀM CHÍNH ĐỂ ÁP DỤNG PHÂN TRANG
        // ---------------------------------------------
        /**
         * Áp dụng phân trang (client-side) lên một tập hợp dữ liệu.
         * @param listToPaginate Mảng dữ liệu cần phân trang (dữ liệu gốc hoặc đã lọc).
         */
        function applyPagination(listToPaginate: any[]) {
            // 1. Cập nhật tổng số mục
            pagePagination.totalItems = listToPaginate.length;

            // 2. Áp dụng hàm splitData để lấy slice cần thiết
            listItems.value = splitData(
                listToPaginate,
                pagePagination
            );
        }

        // Tự động load dữ liệu khi component được setup
        refreshDataFn();

        /**
         * Lấy dữ liệu mới nhất từ Store
         */
        async function refreshDataFn() {
            isLoading.value = true;
            tempList.value = [];
            listItems.value = [];

            try {
                // 1. GỌI STORE ĐỂ FETCH DỮ LIỆU
                await tempUserStore.fetchTempUsers();

                // 2. Gán dữ liệu gốc
                tempList.value = tempUserStore.tempUsers;

                // ✨ SỬA LỖI QUAN TRỌNG: Thiết lập dữ liệu cần phân trang ban đầu
                paginatedData.value = tempList.value;

                // Reset về trang 1 và áp dụng phân trang lên dữ liệu gốc
                pagePagination.currentPage = 1;
                applyPagination(paginatedData.value);

            } catch (error) {
                console.error("Lỗi khi load dữ liệu temp users:", error);
                ElMessage.error(t('Failed to load temp users data.'));
            } finally {
                isLoading.value = false;
            }
        }

        /**
         * Xóa một Temp User (sử dụng psid)
         */
        const deleteTempUser = (psid: string) => {
            console.log('Attempting to delete temp user with psid:', psid);
            ElMessageBox.confirm(
                t('Are you sure you want to delete this temporary user?'),
                t('Warning'),
                {
                    confirmButtonText: t('Yes'),
                    cancelButtonText: t('No'),
                    type: 'warning',
                }
            )
                .then(async () => {
                    isLoading.value = true;
                    try {
                        await tempUsersApi.deleteTempUser(psid);
                        ElMessage.success(t('User deleted successfully'));

                        // Cập nhật lại danh sách
                        await refreshDataFn();
                        // Kích hoạt lại watch để cập nhật phân trang nếu có tìm kiếm
                        // (Mặc dù refreshDataFn đã bao gồm applyPagination, 
                        // việc này đảm bảo logic tìm kiếm được duy trì)
                        watchSearchQuery(searchStore.query);

                    } catch (error) {
                        console.error("Lỗi khi xóa user:", error);
                        ElMessage.error(t('Failed to delete user'));
                    } finally {
                        isLoading.value = false;
                    }
                })
                .catch(() => {
                    ElMessage.info(t('Delete action cancelled'));
                });
        };

        /**
         * Cập nhật trạng thái
         */
        const updateTempUserStatus = async (itemData: any, newStatus: boolean) => {
            try {
                isLoading.value = true;
                const updatedData = { ...itemData, enabled: newStatus };

                // Giả định tempUsersApi.upsertTempUser(psid, data) hoạt động
                const res = await tempUsersApi.upsertTempUser(
                    itemData.psid,
                    updatedData
                );

                if (res.data) {
                    itemData.enabled = newStatus;
                    ElMessage.success(t('Status updated successfully'));
                } else {
                    ElMessage.error(t('Failed to update status'));
                }
            } catch (err) {
                console.error(err);
                ElMessage.error(t('Error updating status'));
            } finally {
                isLoading.value = false;
            }
        };

        // Hàm xử lý logic tìm kiếm (tách ra để có thể gọi lại)
        const watchSearchQuery = (newVal) => {
            pagePagination.currentPage = 1; // ✨ QUAN TRỌNG: Reset về trang 1

            if (!newVal) {
                // Không có query: Dùng danh sách gốc
                paginatedData.value = tempList.value;
            } else {
                // Có query: Lọc dữ liệu trên client
                paginatedData.value = filterDataFunction(
                    newVal,
                    tempList.value
                );
            }

            // Áp dụng phân trang lên dữ liệu mới (gốc hoặc đã lọc)
            applyPagination(paginatedData.value);
        }

        // Theo dõi thay đổi của thanh tìm kiếm
        watch(
            () => searchStore.query,
            watchSearchQuery
        );

        watch(
            () => [pagePagination.pageSize, pagePagination.currentPage],
            () => applyPagination(paginatedData.value)
        );


        // SỬA LỖI PHÂN TRANG: Luôn sử dụng paginatedData.value
        const handleSizeChange = (size: number) => {
            pagePagination.pageSize = size;
            pagePagination.currentPage = 1; // Reset về trang 1 khi thay đổi kích thước
            applyPagination(paginatedData.value); // Phân trang trên dữ liệu đã lọc/gốc
        };

        const handleCurrentChange = (page: number) => {
            pagePagination.currentPage = page;
            applyPagination(paginatedData.value); // Phân trang trên dữ liệu đã lọc/gốc
        };

        return {
            t,
            pagePagination,
            handleCurrentChange,
            handleSizeChange,
            isLoading,
            listItems,
            filterData,
            refreshDataFn,
            filter,
            deleteTempUser,
            formatDateTime,
            updateTempUserStatus,
            showPhoneModal,
            selectedPhones,
            openPhoneModal,
            closePhoneModal
        };
    },
};