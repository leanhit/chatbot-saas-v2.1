import { useI18n } from "vue-i18n";
import { ref, reactive, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { filterDataFunction, splitData, formatDateTime } from "@/utils/search";
import { categoryApi } from "@/api/categoryApi";
import { useCategoryStore } from "@/stores/categoryStore";
import { useSearchStore } from "@/stores/search";

export default {
    props: ["viewSettings"],
    emits: ["onChangeView"],
    setup(props: any, context: any) {
        const { t } = useI18n();
        const categoryStore = useCategoryStore();
        const searchStore = useSearchStore();

        const isLoading = ref(false);

        // Dữ liệu
        const tempCategoriesList = ref([]); // dữ liệu gốc (toàn bộ)
        const paginatedData = ref([]); // dữ liệu sau khi lọc tìm kiếm
        const categoriesList = ref([]); // dữ liệu hiển thị (trang hiện tại)

        const categoryDetail = reactive({
            id: "",
            name: "",
            description: "",
            create_at: "",
        });

        // Cấu hình phân trang
        const pagePagination = reactive({
            pageSize: 15,
            currentPage: 1,
            totalItems: 0,
        });

        /**
         * 📦 Hàm áp dụng phân trang client-side
         */
        function applyPagination(listToPaginate: any[]) {
            pagePagination.totalItems = listToPaginate.length;
            categoriesList.value = splitData(listToPaginate, pagePagination);
        }

        /**
         * 🔁 Lấy dữ liệu từ store (1 lần duy nhất)
         */
        async function refreshDataFn() {
            isLoading.value = true;
            try {
                await categoryStore.getAllCategories();
                tempCategoriesList.value = categoryStore.categories;

                // Khởi tạo dữ liệu mặc định (toàn bộ danh sách)
                paginatedData.value = tempCategoriesList.value;
                pagePagination.currentPage = 1;
                applyPagination(paginatedData.value);
            } catch (err) {
                console.error(err);
                ElMessage.error(t("Failed to load categories"));
            } finally {
                isLoading.value = false;
            }
        }

        // Gọi lần đầu khi component được tạo
        refreshDataFn();

        /**
         * 🗑️ Xóa một category
         */
        const deleteCategory = (id: any) => {
            ElMessageBox.confirm(
                t("Are you sure you want to delete this category?"),
                t("Warning"),
                {
                    confirmButtonText: t("Yes"),
                    cancelButtonText: t("No"),
                    type: "warning",
                }
            )
                .then(async () => {
                    isLoading.value = true;
                    try {
                        await categoryApi.deleteCategory(id);
                        ElMessage.success(t("Category deleted successfully"));
                        await refreshDataFn();

                        // Giữ lại logic lọc nếu đang tìm kiếm
                        watchSearchQuery(searchStore.query);
                    } catch (error) {
                        ElMessage.error(t("Failed to delete category"));
                    } finally {
                        isLoading.value = false;
                    }
                })
                .catch(() => {
                    ElMessage.info(t("Delete action cancelled"));
                });
        };

        /**
         * 🔍 Theo dõi thay đổi của ô tìm kiếm
         */
        const watchSearchQuery = (newVal: string) => {
            pagePagination.currentPage = 1; // reset về trang đầu
            if (!newVal) {
                paginatedData.value = tempCategoriesList.value;
            } else {
                paginatedData.value = filterDataFunction(newVal, tempCategoriesList.value);
            }
            applyPagination(paginatedData.value);
        };

        watch(
            () => searchStore.query,
            watchSearchQuery
        );

        /**
         * 📄 Theo dõi thay đổi trang hoặc kích thước trang
         */
        watch(
            () => [pagePagination.pageSize, pagePagination.currentPage],
            () => applyPagination(paginatedData.value)
        );

        const handleSizeChange = (size: number) => {
            pagePagination.pageSize = size;
            pagePagination.currentPage = 1;
            applyPagination(paginatedData.value);
        };

        const handleCurrentChange = (page: number) => {
            pagePagination.currentPage = page;
            applyPagination(paginatedData.value);
        };

        return {
            t,
            pagePagination,
            handleCurrentChange,
            handleSizeChange,
            isLoading,
            categoriesList,
            refreshDataFn,
            categoryDetail,
            deleteCategory,
            formatDateTime,
        };
    },
};
