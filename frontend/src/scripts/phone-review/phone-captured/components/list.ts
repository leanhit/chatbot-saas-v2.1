import { useI18n } from 'vue-i18n';
import { ref, reactive, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
// Đã sử dụng các hàm này từ file bạn cung cấp:
import { filterDataFunction, splitData, formatDateTime } from '@/utils/search';
import { usePhonesStore } from '@/stores/phonesStore';
import { useSearchStore } from '@/stores/search';

export default {
    props: ['viewSettings'],
    emits: ['onChangeView'],
    setup(props, context) {
        const { t } = useI18n();
        const phonesStore = usePhonesStore();
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
                await phonesStore.fetchPhones();

                // 2. Gán dữ liệu gốc
                tempList.value = phonesStore.phonesList;

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

        // Hàm xử lý logic tìm kiếm (tách ra để có thể gọi lại)
        const watchSearchQuery = (newVal) => {
            pagePagination.currentPage = 1; // ✨ QUAN TRỌNG: Reset về trang 1

            if (!newVal) {
                // Không có query: Dùng danh sách gốc
                paginatedData.value = tempList.value;
            } else {
                //console.log("1=============>",newVal,tempList.value)
                // Có query: Lọc dữ liệu trên client
                paginatedData.value = filterDataFunction(
                    newVal,
                    tempList.value
                );
                //console.log("2=============>",paginatedData.value)
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
            formatDateTime,
        };
    },
};