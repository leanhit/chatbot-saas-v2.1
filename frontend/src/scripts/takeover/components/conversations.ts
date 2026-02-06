
import { ref, computed } from 'vue';
import { Search } from '@element-plus/icons-vue';
import { useI18n } from 'vue-i18n';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { Conversation, TakeoverMessage } from '@/stores/takeoverStore';
import { useTakeoverStore } from '@/stores/takeoverStore';

// Định nghĩa Props và Emits
import type { PropType } from 'vue';
// ******************************************************************
// CHÚ Ý: Cần import hoặc định nghĩa các component con (Conversations, ChatBox) 
// nếu muốn sử dụng chúng trong template của component này.
// Tuy nhiên, dựa trên code gốc, đây là component sidebar, không chứa 2 component kia, 
// nên tôi chỉ giữ lại cấu trúc export default với setup.
// ******************************************************************

// Hàm setup sẽ nhận props và context (chứa emit)
export default {
    components: {
        Conversations,
        ChatBox,
    },
    
    // Định nghĩa props
    props: {
        filteredConversations: {
            type: Array as PropType<Conversation[]>,
            required: true,
            default: () => []
        },
        activeConversationId: {
            type: [String, Number, null, undefined],
            default: null
        },
        activeTab: {
            type: String,
            default: 'all'
        },
        searchQuery: {
            type: String,
            default: ''
        },
        getContactName: {
            type: Function as PropType<(id: string) => string>,
            required: true
        },
        getAvatar: {
            type: Function as PropType<(id: string, senderType?: 'user' | 'agent' | 'botpress') => string>,
            required: true
        },
        getTimeSince: {
            type: Function as PropType<(timestamp?: number) => string>,
            required: true
        },
        'refresh-conversations': {
            type: Function as PropType<(resetSelection?: boolean) => void>,
            required: false,
            default: undefined
        },
        connectionsList: {
            type: Array as PropType<Array<{id: number, botName?: string, botId: string}>>,
            default: () => []
        },
        selectedConnectionId: {
            type: [String, Number, undefined],
            default: undefined
        },
        isAgentMode: {
            type: Boolean,
            default: true
        }
    },
    
    // Định nghĩa emits
    emits: [
        'update:activeTab',
        'update:searchQuery',
        'select-conversation',
        'delete-conversation',
        'delete-conversations',
        'update:selected-conversations',
        'refresh-conversations',
        'takeover'
    ],
    
    // Hàm setup
    setup(props: { 
        filteredConversations: Conversation[],
        activeConversationId: string | number | null | undefined,
        activeTab: string,
        searchQuery: string,
        getContactName: (id: string) => string,
        getAvatar: (id: string, senderType?: 'user' | 'agent' | 'botpress') => string,
        getTimeSince: (timestamp?: number) => string,
        'refresh-conversations'?: (resetSelection?: boolean) => void,
        connectionsList: Array<{id: number, botName?: string, botId: string}>,
        selectedConnectionId: string | number | undefined,
        isAgentMode: boolean
    }, context) {
        // Khởi tạo i18n
        const { t } = useI18n(); 
        
        // Khởi tạo store
        const takeoverStore = useTakeoverStore();
        
        // State cục bộ (từ ref)
        const selectedConversations = ref<Set<string>>(new Set());
        const selectAll = ref(false);

        // Computed Properties (từ computed)
        const selectedConversationsArray = computed(() => Array.from(selectedConversations.value));
        const filteredConversations = computed(() => props.filteredConversations);
        const activeConversationId = computed(() => props.activeConversationId);
        const searchQuery = computed(() => props.searchQuery);

        // Tính toán text cho nút dựa trên trạng thái isTakenOverByAgent
        const buttonText = computed(() => {
            if (!activeConversationId.value) return 'Agent';
            const currentConversation = filteredConversations.value.find(
                conv => conv.conversationId === activeConversationId.value
            );
            return currentConversation?.isTakenOverByAgent ? 'Hủy tiếp nhận' : 'Tiếp nhận';
        });

        // Watchers (dùng watch để thay thế cho việc theo dõi trong script setup)
        // Watch for changes in filtered conversations to update select all state
        const updateSelectAll = () => {
            if (filteredConversations.value.length === 0) {
                selectAll.value = false;
                return;
            }
            // Check if all filtered conversations are selected
            const allFilteredSelected = filteredConversations.value.every((conv: Conversation) => 
                selectedConversations.value.has(conv.conversationId)
            );
            selectAll.value = allFilteredSelected;
        };

        watch(filteredConversations, updateSelectAll, { deep: true });


        // Methods
        
        // Handle individual conversation selection
        const toggleConversation = (conversationId: string) => {
            if (selectedConversations.value.has(conversationId)) {
                selectedConversations.value.delete(conversationId);
            } else {
                selectedConversations.value.add(conversationId);
            }
            updateSelectAll();
            context.emit('update:selected-conversations', Array.from(selectedConversations.value));
        };

        // Handle delete selected conversations
        const handleDeleteSelected = async () => {
            if (selectedConversations.value.size === 0) return;
            
            try {
                await ElMessageBox.confirm(
                    `Bạn có chắc chắn muốn xóa ${selectedConversations.value.size} cuộc hội thoại đã chọn?`,
                    'Xác nhận xóa',
                    {
                        confirmButtonText: 'Xóa',
                        cancelButtonText: 'Hủy',
                        type: 'warning',
                        confirmButtonClass: 'el-button--danger',
                    }
                );
                
                // Gọi API xóa
                await Promise.all(
                    Array.from(selectedConversations.value).map(async (conversationId: string) => {
                        await takeoverStore.deleteConversation(conversationId);
                    })
                );

                // Xóa khỏi danh sách đã chọn
                selectedConversations.value.clear();
                selectAll.value = false;
                
                // Cập nhật lại danh sách đã chọn
                context.emit('update:selected-conversations', []);
                
                // Tải lại danh sách hội thoại
                context.emit('refresh-conversations');
                
                ElMessage.success(`Đã xóa thành công ${selectedConversations.value.size} cuộc hội thoại`);
            } catch (error) {
                if (error !== 'cancel') {
                    console.error('Lỗi khi xóa hội thoại:', error);
                    ElMessage.error('Có lỗi xảy ra khi xóa cuộc hội thoại');
                }
            }
        };

        // Handle select all conversations
        const handleSelectAll = () => {
            if (selectAll.value) {
                // Select all conversations in the current filter
                filteredConversations.value.forEach((conv: Conversation) => {
                    selectedConversations.value.add(conv.conversationId);
                });
            } else {
                // Deselect all conversations in the current filter
                const filteredIds = new Set(filteredConversations.value.map(conv => conv.conversationId));
                filteredIds.forEach(id => {
                    selectedConversations.value.delete(id);
                });
            }
            context.emit('update:selected-conversations', Array.from(selectedConversations.value));
        };

        // Hàm xử lý khi nhấn nút Thêm (Take Over)
        const handleTakeOver = async () => {
            if (!activeConversationId.value) {
                ElMessage.warning('Vui lòng chọn một cuộc hội thoại');
                return;
            }
            
            try {
                // Tìm cuộc hội thoại hiện tại
                const currentConversation = filteredConversations.value.find(
                    (conv: any) => conv.conversationId === activeConversationId.value
                );
                
                if (!currentConversation) {
                    throw new Error('Không tìm thấy cuộc hội thoại');
                }
                
                const oldStatus = currentConversation.isTakenOverByAgent;
                const newStatus = !oldStatus;
                
                try {
                    // Cập nhật trạng thái trên server
                    await takeoverStore.updateTakenOverStatus(
                        Number(activeConversationId.value),
                        newStatus
                    );
                    
                    // Nếu thành công, hiển thị thông báo
                    ElMessage.success(`Đã ${newStatus ? 'tiếp nhận' : 'hủy tiếp nhận'} cuộc hội thoại`);
                    
                    // Làm mới danh sách từ server
                    context.emit('refresh-conversations');
                    
                } catch (apiError) {
                    console.error('Lỗi API:', apiError);
                    throw apiError;
                }
                
            } catch (error) {
                console.error('Lỗi khi cập nhật trạng thái tiếp nhận:', error);
                ElMessage.error('Có lỗi xảy ra khi cập nhật trạng thái tiếp nhận');
            }
        };

        // Hàm xử lý khi tab thay đổi
        const handleTabChange = (name: string) => {
            context.emit('update:activeTab', name);
        };

        // Hàm xử lý xóa tất cả cuộc hội thoại
        const handleDeleteAll = async () => {
            try {
                await ElMessageBox.confirm(
                    'Bạn có chắc chắn muốn xóa tất cả cuộc hội thoại? Hành động này không thể hoàn tác.',
                    'Xác nhận xóa tất cả',
                    {
                        confirmButtonText: 'Xóa tất cả',
                        cancelButtonText: 'Hủy',
                        type: 'error',
                    }
                );
                
                // Lấy danh sách tất cả ID cần xóa
                const allConversationIds = filteredConversations.value.map(conv => conv.id);
                
                if (allConversationIds.length > 0) {
                    // Gọi store để xóa tất cả conversations
                    await takeoverStore.deleteConversations(allConversationIds);
                    
                    // Xóa tất cả các cuộc hội thoại đã chọn khỏi danh sách
                    selectedConversations.value.clear();
                    selectAll.value = false;
                    
                    // Cập nhật lại danh sách đã chọn
                    context.emit('update:selected-conversations', []);
                    
                    // Tải lại danh sách hội thoại
                    context.emit('refresh-conversations', true);
                    
                    ElMessage.success(`Đã xóa thành công ${allConversationIds.length} cuộc hội thoại`);
                } else {
                    ElMessage.warning('Không có cuộc hội thoại nào để xóa');
                }
            } catch (error) {
                if (error !== 'cancel') {
                    console.error('Lỗi khi xóa tất cả hội thoại:', error);
                    ElMessage.error('Có lỗi xảy ra khi xóa tất cả cuộc hội thoại');
                }
            }
        };

        // Trả về các biến và hàm cần thiết cho template
        return {
            t,
            Search,
            // Refs/Computed
            activeTab,
            activeConversationId,
            searchQuery,
            selectAll,
            selectedConversations,
            selectedConversationsArray,
            filteredConversations,
            buttonText,
            
            // Methods
            getContactName: props.getContactName,
            getAvatar: props.getAvatar,
            getTimeSince: props.getTimeSince,
            handleTabChange,
            handleSelectAll,
            handleDeleteSelected,
            toggleConversation,
            handleTakeOver,
            handleDeleteAll,
        };
    },
};