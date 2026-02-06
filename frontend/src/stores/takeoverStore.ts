import { defineStore } from 'pinia'
import { ref } from 'vue'
// Giả định appApi (trước đây là takeoverApi) đã được sửa đổi và chứa các hàm mới
import { appApi } from '@/api/takeoverApi'

// ===================================
// KHAI BÁO INTERFACES 
// ===================================

export interface TakeoverMessage {
    conversationId: string // Key để lọc WS và Redis (Ví dụ: '12345')
    sender: 'user' | 'bot' | 'agent'
    content: string
    timestamp: number
    messageType?: string
}

export interface Conversation {
    id: number // ID chính của Conversation (Long in Java)
    conversationId: string // Key dùng để lọc WS và Redis (Ví dụ: '12345')
    connectionId: string
    externalUserId: string
    ownerId: string
    pageName: string
    lastMessage?: string
    status: string // open | closed | assigned | pending
    isClosedByAgent: boolean
    isTakenOverByAgent?: boolean
    userName?: string // Tên người dùng
    userAvatar?: string // URL avatar người dùng
    lastMessageTimestamp?: number // Thời gian tin nhắn cuối cùng
    // ... thêm các trường khác nếu cần từ ConversationDTO
}

export interface ConversationFilterParams {
    page?: number;
    size?: number;
    connectionId?: string;
    ownerId?: string;
    status?: string;
}

// ===================================
// PINIA STORE
// ===================================

export const useTakeoverStore = defineStore('takeoverStore', () => {
    // ------------------------
    // STATE
    // ------------------------
    const isInitialized = ref(false);
    const isLoading = ref(false);
    const conversations = ref<Conversation[]>([]);
    const messages = ref<TakeoverMessage[]>([]);
    const activeConversationId = ref<string | null>(null);
    const activeConversationDbId = ref<number | null>(null); // ID Long trong DB

    // Trạng thái phân trang/lọc
    const currentPage = ref(0)
    const totalPages = ref(1)
    const totalElements = ref(0)
    const activeFilterParams = ref<ConversationFilterParams>({ page: 0, size: 20 })

    // Giả định Agent ID của người dùng đăng nhập
    const currentAgentId = 101


    // ------------------------
    // 1. Conversations (REST API)
    // ------------------------
    const loadConversations = async (params: ConversationFilterParams = {}) => {
        const finalParams = { ...activeFilterParams.value, ...params };
        activeFilterParams.value = finalParams;

        // ... (Logic gọi API load conversations giữ nguyên) ...
        let apiCall;
        let apiParams: any = { page: finalParams.page, size: finalParams.size };

        try {
            if (finalParams.connectionId) {
                apiParams.connectionId = finalParams.connectionId;
                apiCall = appApi.getConversationsByConnectionId(finalParams.connectionId, apiParams);
            } else {
                apiCall = appApi.getConversationsByOwnerId(apiParams);
            }

            const res = await apiCall;
            const content = res.data.content;
            const pageData = res.data;

            // Cập nhật trạng thái phân trang
            currentPage.value = pageData.number;
            totalPages.value = pageData.totalPages;
            totalElements.value = pageData.totalElements;

            // Chuyển đổi DTO từ Java sang Conversation
            conversations.value = content.map((c: any) => ({
                id: c.id,
                connectionId: c.connectionId,
                externalUserId: c.externalUserId,
                ownerId: c.ownerId,
                pageName: c.pageName || c.channel || 'Kênh chưa xác định',
                lastMessage: c.lastMessage,
                lastMessageTimestamp: c.lastMessageTimestamp || (c.lastMessageAt ? new Date(c.lastMessageAt).getTime() : Date.now()),
                status: c.status,
                isClosedByAgent: c.isClosedByAgent,
                isTakenOverByAgent: c.isTakenOverByAgent,
                userName: c.userName || `Người dùng ${c.externalUserId?.slice(-4) || ''}`, // Thêm tên mặc định nếu không có
                userAvatar: c.userAvatar, // Thêm trường avatar người dùng
                conversationId: String(c.id), // Lấy ID DB làm key cho WS/Client
            })) as Conversation[]

        } catch (err) {
            console.error('Failed to load conversations', err)
        }
    }

    // 🌟 Đã sửa: Bổ sung logic gửi ID qua WebSocket để đăng ký theo dõi
    const selectConversation = async (conversationId: string) => {
        activeConversationId.value = conversationId
        const conv = conversations.value.find(c => c.conversationId === conversationId)
        activeConversationDbId.value = conv ? conv.id : null

        if (activeConversationDbId.value) {
            await loadMessages(activeConversationDbId.value)

            // BƯỚC QUAN TRỌNG: Gửi ID cuộc hội thoại đến Server qua WebSocket
            if (ws.value && ws.value.readyState === WebSocket.OPEN) {
                // Server mong đợi tin nhắn TEXT chứa conversationId để đăng ký theo dõi
                ws.value.send(conversationId);
                console.log(`[WS] Subscribed to conversation ID: ${conversationId}`);
            } else if (ws.value && ws.value.readyState === WebSocket.CONNECTING) {
                console.warn('[WS] WebSocket is connecting, subscription may be handled by onopen.');
            } else {
                console.error('[WS] WebSocket is not connected. Tin nhắn tức thời sẽ không hoạt động.');
            }

        } else {
            messages.value = []
        }
    }

    // ------------------------
    // 2. Messages (REST API)
    // ------------------------
    const loadMessages = async (conversationDbId: number) => {
        try {
            const res = await appApi.getMessagesHistory(conversationDbId);
            
            // Chỉ giữ lại các trường cần thiết từ message
            messages.value = res.data.content.map((m: any) => ({
                id: m.id,
                conversationId: String(m.conversationId),
                sender: m.sender,
                content: m.content,
                timestamp: new Date(m.createdAt).getTime(),
                messageType: m.messageType || 'text',
                rawPayload: m.rawPayload || null
            })) as TakeoverMessage[];

            messages.value.reverse(); // Đảo ngược để hiển thị tin nhắn mới nhất ở dưới
        } catch (err) {
            console.error('Failed to load messages from DB', err);
            throw err; // Ném lỗi để component cha có thể xử lý
        }
    }

    // GỬI TIN NHẮN (Tự động take over nếu cần)
    const sendMessage = async (content: string) => {
        if (!activeConversationDbId.value || !activeConversationId.value) return

        try {
            // Lấy thông tin cuộc hội thoại hiện tại
            const conv = conversations.value.find(c => c.conversationId === activeConversationId.value);
            if (!conv) return;

            // Nếu chưa take over, tự động take over trước khi gửi tin nhắn
            if (!conv.isTakenOverByAgent) {
                try {
                    await takeoverConversation(conv.id);
                    // Cập nhật trạng thái trong store
                    conv.isTakenOverByAgent = true;
                    conv.status = 'active_agent';
                } catch (err) {
                    console.error('Không thể tiếp quản cuộc hội thoại:', err);
                    throw new Error('Không thể tiếp quản cuộc hội thoại');
                }
            }

            // Gọi API gửi tin nhắn
            await appApi.sendMessage({
                conversationId: activeConversationDbId.value,
                sender: 'agent',
                content: content,
                messageType: 'text',
                rawPayload: null
            });
            
            // Cập nhật last message trong danh sách hội thoại
            conv.lastMessage = content;
            // Đẩy lên đầu danh sách
            const index = conversations.value.indexOf(conv);
            if (index > 0) {
                conversations.value.splice(index, 1);
                conversations.value.unshift(conv);
            }
        } catch (err) {
            console.error('Gửi tin nhắn thất bại', err);
            throw err; // Ném lỗi để component có thể xử lý hiển thị thông báo
        }
    }

    // ------------------------
    // 4. WebSocket API
    // ------------------------
    const ws = ref<WebSocket | null>(null)

    // Biến quản lý kết nối lại
    let reconnectAttempts = 0;
    const MAX_RECONNECT_ATTEMPTS = 5;
    let reconnectTimeout: number | null = null;
    const RECONNECT_DELAY_BASE = 1000; // 1 giây
    const MAX_RECONNECT_DELAY = 30000; // 30 giây

    // Hàm tính toán thời gian chờ kết nối lại
    const getReconnectDelay = (attempt: number): number => {
        return Math.min(RECONNECT_DELAY_BASE * Math.pow(2, attempt), MAX_RECONNECT_DELAY);
    };

    // Hàm đóng kết nối an toàn
    const safeCloseWebSocket = () => {
        if (ws.value) {
            // Xóa các sự kiện để tránh rò rỉ bộ nhớ
            ws.value.onopen = null;
            ws.value.onmessage = null;
            ws.value.onerror = null;
            ws.value.onclose = null;
            
            // Chỉ đóng nếu đang mở hoặc đang kết nối
            if (ws.value.readyState === WebSocket.OPEN || ws.value.readyState === WebSocket.CONNECTING) {
                ws.value.close();
            }
            ws.value = null;
        }
    };

    // Hàm kết nối WebSocket
    const connectWS = () => {
        // Đóng kết nối cũ nếu có
        safeCloseWebSocket();

        // Xóa timeout kết nối lại nếu có
        if (reconnectTimeout !== null) {
            clearTimeout(reconnectTimeout);
            reconnectTimeout = null;
        }

        // Tạo URL WebSocket dựa trên host hiện tại
        const protocol = window.location.protocol === 'https:' ? 'wss://' : 'ws://';
        const wsUrl = import.meta.env.VITE_WS_URL || 
             `${protocol}${window.location.host}/ws/takeover`;
        
        console.log('🔄 Đang kết nối tới WebSocket:', wsUrl);
        
        try {
            ws.value = new WebSocket(wsUrl);

            ws.value.onmessage = (event) => {
                try {
                    const msg: TakeoverMessage = JSON.parse(event.data);
                    console.log('📩 Nhận tin nhắn WebSocket:', msg);

                    // Kiểm tra xem tin nhắn đã tồn tại chưa (dựa trên timestamp, nội dung và người gửi)
                    const isDuplicate = messages.value.some(m => 
                        m.timestamp === msg.timestamp && 
                        m.content === msg.content && 
                        m.sender === msg.sender
                    );

                    // Chỉ thêm nếu không trùng lặp và thuộc hội thoại đang active
                    if (!isDuplicate && activeConversationId.value && 
                        msg.conversationId === activeConversationId.value) {
                        messages.value.push(msg);
                    }

                    // Cập nhật lastMessage & Đẩy lên đầu danh sách
                    const conv = conversations.value.find(c => c.conversationId === msg.conversationId);
                    if (conv) {
                        conv.lastMessage = msg.content;
                        const index = conversations.value.indexOf(conv);
                        if (index > 0) {
                            conversations.value.splice(index, 1);
                            conversations.value.unshift(conv);
                        }
                    }
                } catch (e) {
                    console.error('❌ Lỗi phân tích tin nhắn WebSocket:', e, event.data);
                }
            };

            ws.value.onopen = () => {
                console.log('✅ Đã kết nối WebSocket thành công');
                reconnectAttempts = 0; // Đặt lại số lần thử kết nối lại
                
                // Gửi ID conversation đang hoạt động (nếu có)
                if (activeConversationId.value) {
                    ws.value?.send(activeConversationId.value);
                    console.log(`📤 Đã gửi đăng ký theo dõi hội thoại: ${activeConversationId.value}`);
                }
            };

            ws.value.onclose = (event) => {
                console.log(`🔌 Mất kết nối WebSocket. Mã: ${event.code}, Lý do: ${event.reason || 'Không rõ'}`);
                
                // Không thử kết nối lại nếu đã đóng có chủ đích (code 1000)
                if (event.code === 1000) {
                    console.log('Kết nối đã đóng bình thường');
                    return;
                }
                
                // Thử kết nối lại nếu chưa vượt quá số lần thử tối đa
                if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                    const delay = getReconnectDelay(reconnectAttempts);
                    console.log(`⏳ Thử kết nối lại sau ${delay}ms... (Lần thử ${reconnectAttempts + 1}/${MAX_RECONNECT_ATTEMPTS})`);
                    
                    reconnectTimeout = window.setTimeout(() => {
                        reconnectAttempts++;
                        connectWS();
                    }, delay);
                } else {
                    console.error(`❌ Đã thử kết nối lại ${MAX_RECONNECT_ATTEMPTS} lần nhưng không thành công`);
                    // Có thể thêm thông báo cho người dùng ở đây
                }
            };

            ws.value.onerror = (error) => {
                console.error('❌ Lỗi WebSocket:', error);
                // Lỗi sẽ tự động kích hoạt sự kiện onclose
            };

        } catch (error) {
            console.error('❌ Lỗi khi tạo kết nối WebSocket:', error);
            // Thử kết nối lại sau khi gặp lỗi
            if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                const delay = getReconnectDelay(reconnectAttempts);
                reconnectTimeout = window.setTimeout(() => {
                    reconnectAttempts++;
                    connectWS();
                }, delay);
            }
        }
    };

    // Hàm đóng kết nối WebSocket
    const disconnectWS = () => {
        console.log('🛑 Đang đóng kết nối WebSocket...');
        reconnectAttempts = MAX_RECONNECT_ATTEMPTS; // Ngăn chặn tự động kết nối lại
        
        if (reconnectTimeout !== null) {
            clearTimeout(reconnectTimeout);
            reconnectTimeout = null;
        }
        
        safeCloseWebSocket();
    };

    const takeoverConversation = async (conversationId: number) => {
    if (!conversationId) {
        console.error('No conversation ID provided for takeover');
        return;
    }

    try {
        // Call the API to take over the conversation
        await appApi.takeoverConversation(conversationId, currentAgentId);
        
        // Update the conversation status in the local state
        const conversation = conversations.value.find(c => c.id === conversationId);
        if (conversation) {
            conversation.status = 'taken';
            // Move the conversation to the top of the list
            const index = conversations.value.indexOf(conversation);
            if (index > 0) {
                conversations.value.splice(index, 1);
                conversations.value.unshift(conversation);
            }
        }
        
        console.log(`Successfully took over conversation ${conversationId}`);
    } catch (error) {
        console.error('Failed to take over conversation:', error);
        throw error;
    }
};

const releaseConversation = async (conversationId: number) => {
    if (!conversationId) {
        console.error('No conversation ID provided for release');
        return;
    }

    try {
        // Call the API to release the conversation
        await appApi.releaseConversation(conversationId);
        
        // Update the conversation status in the local state
        const conversation = conversations.value.find(c => c.id === conversationId);
        if (conversation) {
            conversation.status = 'open';
            // Optionally move the conversation down in the list
            const index = conversations.value.indexOf(conversation);
            if (index >= 0 && index < conversations.value.length - 1) {
                conversations.value.splice(index, 1);
                conversations.value.push(conversation);
            }
        }
        
        console.log(`Successfully released conversation ${conversationId}`);
    } catch (error) {
        console.error('Failed to release conversation:', error);
        throw error;
    }
};


    // ------------------------
    // Message Deletion
    // ------------------------
    const deleteMessage = async (messageId: number) => {
        try {
            await appApi.deleteMessage(messageId);
            // Remove the message from local state if it exists
            messages.value = messages.value.filter(msg =>
                'id' in msg ? msg.id !== messageId : true
            );
        } catch (error) {
            console.error('Failed to delete message:', error);
            throw error;
        }
    };

    const deleteMessages = async (messageIds: number[]) => {
        try {
            await appApi.deleteMessages(messageIds);
            // Remove deleted messages from local state
            messages.value = messages.value.filter(msg =>
                'id' in msg ? !messageIds.includes(msg.id) : true
            );
        } catch (error) {
            console.error('Failed to delete messages:', error);
            throw error;
        }
    };

    const deleteAllMessages = async (conversationId: number) => {
        try {
            await appApi.deleteAllMessages(conversationId);
            // Clear messages for the current conversation if it matches
            if (activeConversationDbId.value === conversationId) {
                messages.value = [];
            }
        } catch (error) {
            console.error('Failed to delete all messages:', error);
            throw error;
        }
    };

    // ------------------------
    // Conversation Deletion
    // ------------------------
    const deleteConversation = async (conversationId: number) => {
        try {
            await appApi.deleteConversation(conversationId);
            // Remove the conversation from the list
            conversations.value = conversations.value.filter(
                conv => conv.id !== conversationId
            );

            // If the deleted conversation was active, clear the active conversation
            if (activeConversationDbId.value === conversationId) {
                activeConversationId.value = null;
                activeConversationDbId.value = null;
                messages.value = [];
            }
        } catch (error) {
            console.error('Failed to delete conversation:', error);
            throw error;
        }
    };

    const deleteConversations = async (conversationIds: number[]) => {
        try {
            await appApi.deleteConversations(conversationIds);

            // Remove deleted conversations from the list
            conversations.value = conversations.value.filter(
                conv => !conversationIds.includes(conv.id)
            );

            // If the active conversation was deleted, clear it
            if (activeConversationDbId.value &&
                conversationIds.includes(activeConversationDbId.value)) {
                activeConversationId.value = null;
                activeConversationDbId.value = null;
                messages.value = [];
            }
        } catch (error) {
            console.error('Failed to delete conversations:', error);
            throw error;
        }
    };

    // Initialize store
    const initialize = async () => {
        if (isInitialized.value) return;
        
        isLoading.value = true;
        try {
            // Load initial data
            await loadConversations({ page: 0, size: 20 });
            
            // If we have conversations, select the first one
            if (conversations.value.length > 0) {
                await selectConversation(conversations.value[0].conversationId);
            }
            
            isInitialized.value = true;
        } catch (error) {
            console.error('Failed to initialize takeover store:', error);
            throw error;
        } finally {
            isLoading.value = false;
        }
    };

    // Cập nhật trạng thái tiếp nhận cuộc hội thoại
    const updateTakenOverStatus = async (conversationId: number, isTakenOver: boolean) => {
        try {
            // Gọi API để cập nhật trạng thái
            await appApi.updateTakenOverStatus(conversationId, isTakenOver);
            
            // Cập nhật trạng thái cục bộ
            const conversation = conversations.value.find(c => c.id === conversationId);
            if (conversation) {
                // Cập nhật cả status và isTakenOverByAgent
                conversation.status = isTakenOver ? 'active_agent' : 'open';
                conversation.isTakenOverByAgent = isTakenOver;
            }
            
            return true;
        } catch (error) {
            console.error('Lỗi khi cập nhật trạng thái tiếp nhận:', error);
            throw error;
        }
    };

    // ------------------------
    // RETURN
    // ------------------------
    return {
        // State
        isInitialized,
        isLoading,
        conversations,
        messages,
        activeConversationId,
        activeConversationDbId,
        currentPage,
        totalPages,
        totalElements,
        activeFilterParams,

        loadConversations,
        selectConversation, // 🌟 Đã sửa
        loadMessages,
        sendMessage,
        connectWS, // 🌟 Đã sửa
        disconnectWS,
        takeoverConversation,
        releaseConversation,

        closeConversation: appApi.closeConversation,
        deleteMessage,
        deleteMessages,
        deleteAllMessages,
        deleteConversation,
        deleteConversations,
        updateTakenOverStatus,
        
        // Initialization
        initialize
    }
})