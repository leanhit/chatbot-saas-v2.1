<script setup lang="ts">
import { ref, computed } from 'vue';
import { Search } from '@element-plus/icons-vue';
import { useI18n } from 'vue-i18n';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { Conversation, TakeoverMessage } from '@/stores/takeoverStore';
import { useTakeoverStore } from '@/stores/takeoverStore';

// Định nghĩa Props và Emits
import type { PropType } from 'vue';

const props = defineProps({
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
});

const emit = defineEmits<{
    (e: 'update:activeTab', tab: string): void;
    (e: 'update:searchQuery', query: string): void;
    (e: 'select-conversation', conversationId: string): void;
    (e: 'delete-conversation', conversation: Conversation): void;
    (e: 'delete-conversations', conversationIds: number[]): void;
    (e: 'update:selected-conversations', ids: string[]): void;
    (e: 'refresh-conversations', resetSelection?: boolean): void;
    (e: 'takeover'): void;
}>();

// Expose commonly used props to the template as local refs/computeds
const activeTab = computed(() => props.activeTab);
const activeConversationId = computed(() => props.activeConversationId);
const searchQuery = computed(() => props.searchQuery);
const getContactName = props.getContactName;
const getAvatar = props.getAvatar;
const getTimeSince = props.getTimeSince;

// Track selected conversations using a Set for better performance with multiple selections
const selectedConversations = ref<Set<string>>(new Set());
const selectedConversationsArray = computed(() => Array.from(selectedConversations.value));
const selectAll = ref(false);

// Access filteredConversations from props
const filteredConversations = computed(() => props.filteredConversations);

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

// Handle individual conversation selection
const toggleConversation = (conversationId: string) => {
    if (selectedConversations.value.has(conversationId)) {
        selectedConversations.value.delete(conversationId);
    } else {
        selectedConversations.value.add(conversationId);
    }
    updateSelectAll();
    emit('update:selected-conversations', Array.from(selectedConversations.value));
};

// Handle delete selected conversations
const takeoverStore = useTakeoverStore();

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
        emit('update:selected-conversations', []);
        
        // Tải lại danh sách hội thoại
        emit('refresh-conversations');
        
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
    emit('update:selected-conversations', Array.from(selectedConversations.value));
};

const { t } = useI18n(); 

// Tính toán text cho nút dựa trên trạng thái isTakenOverByAgent
const buttonText = computed(() => {
    if (!props.activeConversationId) return 'Tiếp nhận';
    const currentConversation = props.filteredConversations.find(
        conv => conv.conversationId === props.activeConversationId
    );
    return currentConversation?.isTakenOverByAgent ? 'Hủy tiếp nhận' : 'Tiếp nhận';
});

// HÀM SỬA LỖI: Chỉ gọi API và refresh sau đó
const handleTakeOver = async () => {
    if (!props.activeConversationId) {
        ElMessage.warning('Vui lòng chọn một cuộc hội thoại');
        return;
    }

    const currentConversation = props.filteredConversations.find(
        (conv: any) => conv.conversationId === props.activeConversationId
    );
    
    if (!currentConversation) {
        ElMessage.error('Không tìm thấy cuộc hội thoại');
        return;
    }

    const newStatus = !currentConversation.isTakenOverByAgent;

    try {
        // 1. Gọi API để cập nhật lên server
        await takeoverStore.updateTakenOverStatus(
            Number(props.activeConversationId),
            newStatus
        );
        
        // 2. Nếu thành công, hiển thị thông báo
        ElMessage.success(`Đã ${newStatus ? 'tiếp nhận' : 'hủy tiếp nhận'} cuộc hội thoại`);
        
        // 3. Làm mới danh sách từ server để cập nhật UI
        emit('refresh-conversations'); 
        
    } catch (apiError) {
        console.error('Lỗi API khi cập nhật trạng thái tiếp nhận:', apiError);
        ElMessage.error('Có lỗi xảy ra khi cập nhật trạng thái tiếp nhận');
    }
};

// Hàm xử lý khi tab thay đổi
const handleTabChange = (name: string) => {
    emit('update:activeTab', name);
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
        const allConversationIds = props.filteredConversations.map(conv => conv.id);
        
        if (allConversationIds.length > 0) {
            // Gọi store để xóa tất cả conversations
            await takeoverStore.deleteConversations(allConversationIds);
            
            // Xóa tất cả các cuộc hội thoại đã chọn khỏi danh sách
            selectedConversations.value.clear();
            selectAll.value = false;
            
            // Cập nhật lại danh sách đã chọn
            emit('update:selected-conversations', []);
            
            // Tải lại danh sách hội thoại
            emit('refresh-conversations', true);
            
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
</script>

<template>
    <div class="conversation-sidebar border-end d-flex flex-column w-25 min-w-350px bg-light h-100">

        <div class="select-all-controls p-2 border-bottom d-flex align-items-center justify-content-between bg-white">
            <el-checkbox 
                v-model="selectAll" 
                @change="handleSelectAll"
                :indeterminate="selectedConversations.size > 0 && !selectAll"
                class="me-3"
            >
                {{ t('Select All') }}
            </el-checkbox>
            
            <div class="d-flex align-items-center ms-auto">
                <el-button 
                    v-if="selectedConversations.size > 0"
                    type="danger" 
                    size="small" 
                    :disabled="selectedConversations.size === 0"
                    @click="handleDeleteSelected"
                >
                    <i class="fas fa-trash-alt me-1"></i> Xóa đã chọn ({{ selectedConversations.size }})
                </el-button>
            </div>
        </div>

        <div class="conversation-list flex-grow-1 overflow-auto p-0">
            <div 
                v-for="conv in filteredConversations" 
                    :key="conv.conversationId" 
                    class="d-flex p-3 conversation-item"
                    :class="{
                        'is-active-conversation border-start border-3 border-primary': // 👈 THAY ĐỔI TÊN LỚP TẠI ĐÂY
                            conv.conversationId === activeConversationId,
                        'is-checkbox-selected': selectedConversations.has(conv.conversationId)
                    }"
                @click="(event) => {
                    const target = event.target as HTMLElement; // Ép kiểu để sử dụng closest
                    // Chỉ trigger nếu click không phải trên checkbox hoặc nhãn của nó
                    if (!target || !target.closest || !target.closest('.conversation-checkbox')) {
                        emit('select-conversation', conv.conversationId);
                    }
                }"
            >
                
                <div class="conversation-checkbox d-flex align-items-center me-2 pt-1" @click.stop>
                    <el-checkbox 
                        :model-value="selectedConversations.has(conv.conversationId)"
                        @change="toggleConversation(conv.conversationId)"
                        @click.stop
                    />
                </div>
                <div class=" px-2">
                    <img 
                        :src="conv.userAvatar || getAvatar(conv.externalUserId, 'user')" 
                        :alt="conv.userName || 'User'" 
                        class="rounded-circle me-3 avatar-small" 
                        onerror="this.src='/src/assets/default-avatar.png'"
                    />
                </div>
                <div class="flex-grow-1 overflow-hidden">
                    <div class="d-flex justify-content-between align-items-start">
                        <strong class="text-truncate d-block me-2">
                            {{ conv.userName || getContactName(conv.externalUserId) }}
                            <i v-if="conv.isTakenOverByAgent" class="fas fa-handshake text-info ms-1 small" :title="t('Tiếp nhận bởi Agent')"></i>
                        </strong>
                        
                        <div class="d-flex align-items-center flex-shrink-0">
                            <small class="text-muted me-2">{{ getTimeSince(conv.lastMessageTimestamp) }}</small>
                            <el-button
                                type="danger"
                                link
                                size="small"
                                class="p-0 delete-btn"
                                @click.stop="emit('delete-conversation', conv)"
                            >
                                <i class="fas fa-trash small"></i>
                            </el-button>
                        </div>
                    </div>
                    
                    <div class="text-truncate text-muted message-preview small">
                        <span v-if="conv.lastMessage">{{ conv.lastMessage }}</span>
                        <span v-else>{{ t('No messages yet') }}</span>
                    </div>
                </div>
            </div>

            <div v-if="!filteredConversations.length" class="text-center text-muted p-4">
                {{ t('No conversations') }}
            </div>
        </div>

        
        <div class="mt-3 p-3 border-top">
            <el-button type="primary" @click="handleTakeOver">
                {{ buttonText }}
            </el-button>
        </div>
    </div>
</template>

<style scoped>
/* Thêm CSS thủ công tối thiểu để bổ sung cho Bootstrap */

/* Định nghĩa chiều rộng tối thiểu cho Sidebar (Bổ sung cho w-25 của Bootstrap) */
.min-w-350px {
    min-width: 350px;
}

/* Style cho item hội thoại */
.conversation-item {
    cursor: pointer;
    transition: background-color 0.1s;
    border-bottom: 1px solid #eee;
}

.conversation-item:hover { 
    background-color: #e9ecef; /* hover màu xám nhạt của Bootstrap */
}


/* Active dùng đúng màu hover */
.is-active-conversation {
    background-color: #5aa1e7 !important;
    color: white; /* Đảm bảo chữ trắng khi active */
}

/* Các text và icon bên trong item active */
.is-active-conversation strong,
.is-active-conversation small,
.is-active-conversation .text-muted {
    color: white !important;
}

/* Hover không làm thay đổi active (vẫn giữ màu như hover) */
.is-active-conversation:hover {
    background-color: #077ef5 !important;
}

/* Avatar nhỏ */
.avatar-small { 
    width: 40px; 
    height: 40px; 
    object-fit: cover; 
}

/* Nút xóa nhỏ hơn và chỉ dùng icon */
.delete-btn {
    opacity: 0;
    transition: opacity 0.1s;
}

/* Hiện nút xóa khi hover item */
.conversation-item:hover .delete-btn {
    opacity: 1;
}

/* Đảm bảo Element Plus tabs không bị tràn */
:deep(.el-tabs__item) {
    padding: 0 10px !important;
}
</style>