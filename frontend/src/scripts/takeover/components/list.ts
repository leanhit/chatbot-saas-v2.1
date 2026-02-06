import { useI18n } from 'vue-i18n';
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue';
import { useTakeoverStore } from '@/stores/takeoverStore';
import { useDataconnectionStore } from '@/stores/connectionStore'; // <<< BỔ SUNG
import type { TakeoverMessage, Conversation } from '@/stores/takeoverStore';
import type { Connection } from '@/stores/connectionStore'; // Giả định interface Connection
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import defaultAvatar from '@/assets/default-avatar.png';
import agentAvatar from '@/assets/agent-avatar.png';
import botpressAvatar from '@/assets/logo.png';
import Conversations from '@/views/takeover/components/Conversations.vue'; 
import ChatBox from '@/views/takeover/components/ChatBox.vue'; 

// Dữ liệu giả định để mô phỏng danh bạ
const FAKE_CONTACTS: { [key: string]: { name: string, avatar: string } } = {
    '1': { name: 'Sa Mạn (ID 1)', avatar: '@/assets/default-avatar.png' },
    '2': { name: 'Nguyễn Hoài (ID 2)', avatar: '@/assets/default-avatar.png' },
    '3': { name: 'Shopee User (ID 3)', avatar: '@/assets/default-avatar.png' },
    '0988990092': { name: '0988990092', avatar: '@/assets/default-avatar.png' },
};

export default {
    // Đăng ký component con
    components: {
        Conversations,
        ChatBox,
    },
    props: ['viewSettings'],
    emits: ['onChangeView'],
    setup(props: { viewSettings: any }, context: any) {
        const { t } = useI18n();
        const takeoverStore = useTakeoverStore();
        const connectionStore = useDataconnectionStore(); // <<< BỔ SUNG: Connection Store

        // Initialize store if needed
        if (!takeoverStore.initialized) {
            takeoverStore.initialize();
        }

        // --- State ---
        const isLoading = ref(false);
        const activeTab = ref('all');
        const searchQuery = ref('');
        const inputContent = ref(''); 

        // --- Bổ sung State Connection ---
        const selectedConnectionId = ref<string | undefined>(undefined); 
        const connectionsList = computed<Connection[]>(() => {
    
            // Giả định connectionStore.connections có trường 'content' là mảng
            if (connectionStore.connection && Array.isArray(connectionStore.connection.content)) {
                return connectionStore.connection.content;
            }
            return [];
        });
        // ------------------------------

        // --- Hàm Giả định (Mô phỏng) ---
        const getContactName = (id: string) => {
            return FAKE_CONTACTS[id]?.name || takeoverStore.conversations.find(c => c.conversationId === id)?.externalUserId || id;
        };
        const getAvatar = (id: string) => {
            return FAKE_CONTACTS[id]?.avatar || defaultAvatar;
        };
        const getTimeSince = (timestamp?: number) => {
            if (!timestamp) return '';
            const date = new Date(timestamp);
            return `${date.getDate()} Thg ${date.getMonth() + 1}`;
        };

        // --- Pagination ---
        const currentPage = ref(1);
        const pageSize = ref(10);
        const totalItems = ref(0);

        // --- Computed Properties ---
        const filteredConversations = computed(() => {
            if (!takeoverStore) return [];
            return takeoverStore.conversations || [];
        });

        // Computed property for active conversation
        const activeConversation = computed<Conversation | null>(() => {
            if (!takeoverStore || !takeoverStore.activeConversationId) return null;
            return takeoverStore.conversations.find(c => c.conversationId === takeoverStore.activeConversationId) || null;
        });

        // Load conversations with pagination
        const loadConversations = async (page = 1, checkCurrentPage = false, resetSelection = false) => {
            try {
                isLoading.value = true;
                
                let targetPage = page;
                
                // Logic checkCurrentPage (giữ nguyên logic gốc)
                // ... (Phần này nên được xử lý trong logic API call để đơn giản hóa)
                
                currentPage.value = targetPage;
                
                // Gọi API với tham số phân trang và bộ lọc mới
                await takeoverStore.loadConversations({
                    page: currentPage.value - 1, // Backend thường bắt đầu từ 0
                    size: pageSize.value,
                    status: activeTab.value !== 'all' ? activeTab.value : undefined,
                    search: searchQuery.value || undefined,
                    // <<< TRUYỀN THAM SỐ CONNECTION ID MỚI >>>
                    connectionId: selectedConnectionId.value, 
                });
                
                // Cập nhật tổng số mục từ store
                totalItems.value = takeoverStore.totalElements || 0;

                // Sau khi tải lại, kiểm tra và chọn lại conversation đầu tiên nếu cần
                if (resetSelection) {
                    if (takeoverStore.conversations && takeoverStore.conversations.length > 0) {
                        const firstConvId = takeoverStore.conversations[0]?.conversationId;
                        if (firstConvId) {
                            await selectConversation(firstConvId);
                        }
                    } else {
                        takeoverStore.messages = [];
                        takeoverStore.activeConversationId = null;
                    }
                }
                
            } catch (error) {
                console.error('Lỗi khi tải danh sách hội thoại:', error);
                ElMessage.error(t('Không thể tải danh sách hội thoại.'));
            } finally {
                isLoading.value = false;
            }
        };
        
        // Bổ sung Xử lý thay đổi Connection mới
        const handleConnectionChange = (connectionId: string | undefined) => {
            selectedConnectionId.value = connectionId;
            loadConversations(1, false, true); // Reset về trang đầu tiên và chọn lại Conversation đầu tiên
        }
        // ---------------------------------------------


        // Xử lý thay đổi trang
        const handlePageChange = (page: number) => {
            loadConversations(page);
        };
        
        // Xử lý thay đổi số lượng mỗi trang
        const handleSizeChange = (size: number) => {
            pageSize.value = size;
            loadConversations(1); // Reset về trang đầu tiên
        };

        const messages = computed(() => takeoverStore.messages); // Truyền tin nhắn

        // --- Logic Xử lý (giữ nguyên logic gốc) ---
        const selectConversation = async (conversationId: string) => {
            if (takeoverStore.activeConversationId !== conversationId) {
                isLoading.value = true;
                try {
                    await takeoverStore.selectConversation(conversationId);
                } catch (e) {
                    console.error('Lỗi khi chọn hội thoại:', e);
                    ElMessage.error(t('Không thể tải tin nhắn.'));
                } finally {
                    isLoading.value = false;
                }
            }
        };

        const sendMessage = async (contentToSend: string) => { 
            // Logic gửi tin nhắn giữ nguyên
            if (!contentToSend.trim() || !takeoverStore.activeConversationId) return;

            try {
                await takeoverStore.sendMessage(contentToSend);
            } catch (err) {
                console.error('Failed to send message', err);
                ElMessage.error(t('Gửi tin nhắn thất bại.'));
            }
        };

        // --- Delete Methods (giữ nguyên logic gốc) ---
        const deleteMessage = async (message: TakeoverMessage) => {
             // ... Logic deleteMessage
             try {
                await takeoverStore.deleteMessage(message.timestamp);
                ElMessage.success(t('Đã xóa tin nhắn'));
            } catch (error) {
                console.error('Lỗi khi xóa tin nhắn:', error);
                ElMessage.error(t('Không thể xóa tin nhắn. Vui lòng thử lại.'));
            }
        };

        const deleteAllMessages = async (conversation: Conversation) => {
            // ... Logic deleteAllMessages
            try {
                await ElMessageBox.confirm(
                    t('Bạn có chắc chắn muốn xóa tất cả tin nhắn trong cuộc hội thoại này?'),
                    t('Xác nhận xóa'),
                    {
                        confirmButtonText: t('Xóa'),
                        cancelButtonText: t('Hủy'),
                        type: 'warning',
                    }
                );
                await takeoverStore.deleteAllMessages(conversation.id);
                ElMessage.success(t('Đã xóa tất cả tin nhắn'));
            } catch (error) {
                if (error !== 'cancel') {
                    console.error('Lỗi khi xóa tất cả tin nhắn:', error);
                    ElMessage.error(t('Không thể xóa tin nhắn. Vui lòng thử lại.'));
                }
            }
        };

        const deleteConversation = async (conversation: Conversation) => {
            try {
                await ElMessageBox.confirm(
                    t('Bạn có chắc chắn muốn xóa cuộc hội thoại này? Hành động này không thể hoàn tác.'),
                    t('Xác nhận xóa'),
                    {
                        confirmButtonText: t('Xóa'),
                        cancelButtonText: t('Hủy'),
                        type: 'error',
                    }
                );
                
                const wasActive = takeoverStore.activeConversationId === conversation.conversationId;
                
                await takeoverStore.deleteConversation(conversation.id);
                
                // Tải lại danh sách hội thoại sau khi xóa, không cần reset selection vì logic đã xử lý
                await loadConversations(currentPage.value, true); 
                
                ElMessage.success(t('Đã xóa cuộc hội thoại'));

                if (wasActive) {
                    const remainingConversations = takeoverStore.conversations;
                    if (remainingConversations.length > 0) {
                        await selectConversation(remainingConversations[0].conversationId);
                    } else {
                        takeoverStore.messages = [];
                        takeoverStore.activeConversationId = null;
                    }
                }

                await nextTick();
            } catch (error) {
                if (error !== 'cancel') {
                    console.error('Lỗi khi xóa cuộc hội thoại:', error);
                    ElMessage.error(t('Không thể xóa cuộc hội thoại. Vui lòng thử lại.'));
                }
            }
        };
        
        // Xóa nhiều cuộc hội thoại cùng lúc
        const deleteMultipleConversations = async (conversationIds: number[]) => {
            // Logic deleteMultipleConversations giữ nguyên
            if (!conversationIds || conversationIds.length === 0) return;
            
            try {
                await takeoverStore.deleteMultipleConversations(conversationIds);
                await loadConversations(currentPage.value, true); 
                
                if (takeoverStore.activeConversationId) {
                    const activeConvStillExists = takeoverStore.conversations.some(
                        conv => conv.conversationId === takeoverStore.activeConversationId
                    );
                    
                    if (!activeConvStillExists) {
                        takeoverStore.messages = [];
                        takeoverStore.activeConversationId = null;
                    }
                }
                
                ElMessage.success(`Đã xóa thành công ${conversationIds.length} cuộc hội thoại`);
            } catch (error) {
                console.error('Lỗi khi xóa nhiều cuộc hội thoại:', error);
                ElMessage.error('Có lỗi xảy ra khi xóa các cuộc hội thoại');
            }
        };

        // --- Lifecycle Hooks ---
        onMounted(async () => {
            try {
                // Ensure store is initialized
                if (!takeoverStore.initialized) {
                    await takeoverStore.initialize();
                }
                
                // <<< BỔ SUNG: TẢI DANH SÁCH CONNECTIONS >>>
                await connectionStore.getAllConnections({ page: 0, size: 100 }); 
                // -------------------------------------------

                await loadConversations(1, false, true); // Tải trang đầu tiên và chọn conversation đầu tiên
                await takeoverStore.connectWS();
            } catch (error) {
                console.error('Lỗi khi load dữ liệu Inbox:', error);
                ElMessage.error(t('Không thể tải dữ liệu hộp thư.'));
            }
        });

        onBeforeUnmount(() => {
            takeoverStore.disconnectWS();
        });

        // --- Return ---
        return {
            t,
            isLoading,
            activeTab,
            searchQuery,
            inputContent, 
            filteredConversations,
            activeConversation,
            messages,
            
            // <<< BỔ SUNG RETURN CHO CONNECTION >>>
            selectedConnectionId,
            connectionsList,
            handleConnectionChange,
            // ------------------------------------
            
            // Methods
            selectConversation,
            sendMessage,
            deleteMessage,
            deleteAllMessages,
            deleteConversation,
            
            // Helpers
            getContactName,
            getAvatar,
            getTimeSince,
            
            // Other
            refreshDataFn: () => loadConversations(currentPage.value), // Cập nhật để gọi loadConversations
            agentAvatar,
            botpressAvatar,
            Search,
            takeoverStore,
            
            // Pagination
            currentPage,
            pageSize,
            totalItems,
            handlePageChange,
            handleSizeChange,
            handleTabChange: (tab: string) => {
                activeTab.value = tab;
                loadConversations(1, false, true); // Reset về trang đầu tiên
            },
            handleSearch: (value: string) => {
                searchQuery.value = value;
                loadConversations(1, false, true); // Reset về trang đầu tiên
            },
            // Hàm refresh-conversations dùng trong Conversations.vue
            refreshConversations: (resetSelection = false) => loadConversations(currentPage.value, true, resetSelection),
            deleteConversations: deleteMultipleConversations, // Thêm hàm này để Conversations.vue gọi khi xóa nhiều
        };
    },
};
