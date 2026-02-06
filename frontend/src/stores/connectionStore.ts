import { ref, reactive } from 'vue';
import { defineStore } from 'pinia';
import { fbConnectionApi } from '@/api/fbConnectionApi';

// ===================================
// ## Định nghĩa Interfaces/Types dựa trên DTO Server 💡
// ===================================

/**
 * Định nghĩa cấu trúc đối tượng Pagination gửi lên API
 */
export interface PagePagination {
    page: number;
    size: number;
}

/**
 * Định nghĩa cấu trúc của một đối tượng Connection (Dựa trên FacebookConnectionResponse.java)
 */
export interface Connection {
    // Java UUID sẽ được truyền thành string trong JSON
    id: string; 
    botId: string;
    botName: string;
    pageId: string;
    fanpageUrl: string;
    pageAccessToken: string;
    isEnabled: boolean;
    isActive: boolean;

    // LocalDateTime với JsonFormat sẽ là string theo format 'yyyy-MM-dd'T'HH:mm:ss.SSS'
    createdAt: string;
    updatedAt: string;
}

/**
 * Định nghĩa cấu trúc phản hồi API (Có phân trang)
 * Giả định API trả về một đối tượng chứa danh sách Connection và tổng số mục.
 */
export interface ConnectionPaginationResponse {
    data: Connection[]; // Danh sách các đối tượng Connection
    total: number; // Tổng số mục
    page: number;
    size: number;
    // Có thể thêm trường totalPages nếu API có trả về
}


// ===================================
// ## Store Pinia
// ===================================

export const useDataconnectionStore = defineStore('connectionStore', () => {
    
    // Sử dụng interface ConnectionPaginationResponse đã định nghĩa ở trên
    const connection = ref<ConnectionPaginationResponse | null>(null);

    async function getAllConnections(pagePagination: PagePagination) {
        try {
            const response = await fbConnectionApi.getAllConnections(
                pagePagination
            );
            
            if (response.status == 200) {
                // Ép kiểu dữ liệu nhận được để đảm bảo khớp
                connection.value = response.data as ConnectionPaginationResponse;
                //console.log("=====>", connection.value.content)
            } else {
                console.log('Error:', response.status);
            }
        } catch (err) {
            console.log('Error:', err);
        }
    }

    async function getConnectionsAll(pagePagination: PagePagination) {
        try {
            const response = await fbConnectionApi.getConnectionsAll(
                pagePagination
            );
            
            if (response.status == 200) {
                // Ép kiểu dữ liệu nhận được để đảm bảo khớp
                connection.value = response.data as ConnectionPaginationResponse;
            } else {
                console.log('Error:', response.status);
            }
        } catch (err) {
            console.log('Error:', err);
        }
    }    

    return {
        connection,
        getAllConnections,
        getConnectionsAll
    }; 
});