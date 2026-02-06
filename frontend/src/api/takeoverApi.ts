// ✅ Dùng instance axios đã setup với baseURL và interceptor
import axios from '@/plugins/axios';

// Định nghĩa cấu trúc Payload chuẩn cho việc gửi tin nhắn
interface MessageSendPayload {
    conversationId: number; // Long in Java
    sender: string;         // user | bot | agent
    content: string;
    messageType?: string;   // text | image | attachment (sẽ được set là 'text' nếu gửi qua Takeover)
    rawPayload?: any;       // Dữ liệu tùy chỉnh
}

// Định nghĩa cấu trúc Params cho phân trang
interface PaginationParams {
    page?: number;
    size?: number;
}

export const appApi = { // Đã đổi tên thành appApi
    // -------------------------
    // 1. Lấy Danh sách Conversations (MẶC ĐỊNH - Lấy tất cả)
    // ENDPOINT: GET /api/conversations
    // -------------------------
    getConversations(params?: PaginationParams) {
        // Gọi đến ConversationController
        return axios.get('/conversations', { params });
    },

    // -------------------------
    // 1.1. LỌC THEO CONNECTION ID
    // ENDPOINT: GET /api/conversations/by-owner-connection?ownerId={id}&connectionId={id}
    // -------------------------
    getConversationsByConnectionId(connectionId: string, params?: PaginationParams) {
        return axios.get('/conversations/by-owner-connection', {
            params: { connectionId, ...params }
        });
    },

    // -------------------------
    // 1.2. LỌC THEO OWNER ID
    // ENDPOINT: GET /api/conversations/by-owner?ownerId={id}
    // -------------------------
    getConversationsByOwnerId(params?: PaginationParams) {
        return axios.get('/conversations/by-owner', { params});
    },

    // -------------------------
    // 2. Lấy Lịch sử Tin nhắn DB (messStore)
    // ENDPOINT: GET /api/messages?conversationId={id}
    // -------------------------
    getMessagesHistory(conversationId: number, params?: PaginationParams) {
        // Gọi đến MessageController
        return axios.get('/messages', {
            params: { conversationId, ...params }
        });
    },

    // -------------------------
    // 3. Lấy Lịch sử Tin nhắn Cache (Takeover/Redis)
    // ENDPOINT: GET /api/takeover/history/{id}
    // -------------------------
    getTakeoverCacheHistory(conversationId: string) {
        // Gọi đến TakeoverController (dùng String ID cho tham số Path Variable)
        return axios.get(`/takeover/history/${conversationId}`);
    },

    // -------------------------
    // 4. Gửi tin nhắn Agent (Lưu DB, Cache, WebSocket)
    // ENDPOINT: POST /api/takeover/send
    // -------------------------
    sendMessage(message: MessageSendPayload) {

        const payloadForTakeover = {
            conversationId: String(message.conversationId), // Chuyển Long thành String để khớp TakeoverMessage
            sender: message.sender,
            content: message.content,
            timestamp: Date.now(), // Thêm timestamp client
        };

        return axios.post('/takeover/send', payloadForTakeover);
    },

        // -------------------------
    // 4. Gửi tin nhắn Agent (Lưu DB, Cache, WebSocket VÀ GỬI RA FACEBOOK)
    // ENDPOINT MỚI: POST /api/agent/send-message
    // -------------------------
    sendMessage2(message: MessageSendPayload) {
        // payload gửi đến Backend (AgentMessageController.java) cần phải khớp với MessageSendPayload DTO
        // DTO Backend mong đợi: { conversationId (Long), sender, content, messageType, rawPayload }

        // Đảm bảo messageType là 'text' nếu không được định nghĩa
        const finalPayload = {
            conversationId: message.conversationId, // Giữ nguyên number/Long
            sender: message.sender, // 'agent'
            content: message.content,
            messageType: message.messageType || 'text', // Đảm bảo có type
            rawPayload: message.rawPayload || null,
        };

        console.log('Sending agent message payload:', finalPayload);
        // Gọi đến AgentMessageController
        return axios.post('/agent/send-message', finalPayload);
    },

    // -------------------------
    // 5. Đóng Conversation
    // ENDPOINT: POST /api/conversations/{conversationId}/close
    // -------------------------
    closeConversation(conversationId: number) {
        return axios.post(`/conversations/${conversationId}/close`);
    },

    // -------------------------
    // 6. TÌM HOẶC TẠO Conversation (Dành cho việc debug hoặc API gọi nội bộ)
    // ENDPOINT: POST /api/conversations/find-or-create
    // -------------------------
    findOrCreateConversation(connectionId: string, externalUserId: string, channel: string) {
        return axios.post('/conversations/find-or-create', null, {
            params: { connectionId, externalUserId, channel }
        });
    },

    // -------------------------
    // 7. Agent Tiếp quản (Takeover) Conversation
    // ENDPOINT: POST /api/conversations/{conversationId}/takeover?agentId={agentId}
    // -------------------------
    takeoverConversation(conversationId: number, agentId: number) {
        return axios.post(`/conversations/${conversationId}/takeover`, null, {
            params: { agentId }
        });
    },

    // -------------------------
    // 8. Agent Giải phóng (Release) Conversation
    // ENDPOINT: POST /api/conversations/{conversationId}/release
    // -------------------------
    releaseConversation(conversationId: number) {
        return axios.post(`/conversations/${conversationId}/release`);
    },

    
    // -------------------------
    // Message Deletion
    // -------------------------
    
    // Delete a single message
    deleteMessage(messageId: number) {
        return axios.delete(`/messages/${messageId}`);
    },

    // Delete multiple messages
    deleteMessages(messageIds: number[]) {
        return axios.delete('/messages', { data: messageIds });
    },

    // Delete all messages in a conversation
    deleteAllMessages(conversationId: number) {
        return axios.delete(`/messages/conversation/${conversationId}`);
    },

    // -------------------------
    // Conversation Deletion
    // -------------------------
    
    // Delete a single conversation and all its messages
    deleteConversation(conversationId: number) {
        return axios.delete(`/conversations/${conversationId}`);
    },

    // Delete multiple conversations and all their messages
    deleteConversations(conversationIds: number[]) {
        return axios.delete('/conversations/batch-delete', { data: conversationIds });
    },

    // This is the new endpoint we need to add
    updateTakenOverStatus(conversationId: number, isTakenOver: boolean) {
        return axios.patch(`/conversations/${conversationId}/taken-over?isTakenOver=${isTakenOver}`);
    }
};