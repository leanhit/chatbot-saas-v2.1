<script setup lang="ts">
import { defineProps, defineEmits, ref, onMounted, onUpdated } from 'vue';
import { useI18n } from 'vue-i18n';
import type { TakeoverMessage, Conversation } from '@/stores/takeoverStore'; // Thay đổi đường dẫn type thực tế nếu cần

// Định nghĩa Props và Emits (Giữ nguyên)
const props = defineProps<{
    activeConversation: Conversation | undefined;
    messages: TakeoverMessage[]; // Giả sử đây là messages mà component nhận vào
    agentAvatar: string; 
    getContactName: (id: string) => string;
    getAvatar: (id: string, senderType?: 'user' | 'agent' | 'botpress') => string; // Hàm lấy avatar theo loại người dùng
}>();

const emit = defineEmits<{
    (e: 'send-message', content: string): void;
    (e: 'delete-message', message: TakeoverMessage): void;
    (e: 'delete-all-messages', conversation: Conversation): void;
    (e: 'delete-conversation', conversation: Conversation): void;
}>();

const { t } = useI18n(); 

// State cho input tin nhắn (Giữ nguyên)
const inputContent = ref('');

/**
 * Gửi tin nhắn và làm trống input. (Giữ nguyên)
 */
const handleSendMessage = () => {
    //console.log(">>======>",inputContent.value)
    if (!inputContent.value.trim() || !props.activeConversation) return;

    const contentToSend = inputContent.value;
    inputContent.value = ''; 
    emit('send-message', contentToSend);
};

// Hàm giả định để phù hợp với template của bạn (nếu bạn đang dùng store)
const deleteMessage = (msg: TakeoverMessage) => {
    emit('delete-message', msg);
};

// Reference to messages container for auto-scrolling
const messagesEndRef = ref<HTMLElement | null>(null);

// Function to scroll to bottom
const scrollToBottom = () => {
    messagesEndRef.value?.scrollIntoView({ behavior: 'smooth' });
};

// Scroll to bottom when component mounts or updates
onMounted(() => {
    scrollToBottom();
});

onUpdated(() => {
    scrollToBottom();
});
</script>

<template>
    <div class="chat-panel flex-grow-1 d-flex flex-column">
        <template v-if="activeConversation">

            <div class="chat-header p-3 border-bottom d-flex align-items-center justify-content-between">
                <div class="d-flex align-items-center">
                    <img 
                        :src="activeConversation.userAvatar || getAvatar(activeConversation.externalUserId, 'user')" 
                        :alt="activeConversation.userName || 'User'"
                        class="avatar-medium me-2" 
                        onerror="this.src='/src/assets/default-avatar.png'"
                    />
                    <div class="px-3">
                        <h5 class="mb-0">{{ activeConversation.userName || getContactName(activeConversation.externalUserId) }}</h5>
                        <small class="text-muted">{{ t('Assign this conversation') }} ▾</small>
                    </div>
                </div>
                <div>
                    <el-dropdown trigger="click">
                        <el-button link circle>
                            <i class="fas fa-ellipsis-v"></i>
                        </el-button>
                        <template #dropdown>
                            <el-dropdown-menu>
                                <el-dropdown-item @click="emit('delete-all-messages', activeConversation)">
                                    <i class="fas fa-trash me-2 text-danger"></i> {{ t('Delete all messages') }}
                                </el-dropdown-item>
                                <el-dropdown-item @click="emit('delete-conversation', activeConversation)" divided>
                                    <i class="fas fa-trash-alt me-2 text-danger"></i> {{ t('Delete conversation') }}
                                </el-dropdown-item>
                            </el-dropdown-menu>
                        </template>
                    </el-dropdown>
                </div>
            </div>

            <div class="messages-area flex-grow-1 overflow-auto p-3">
                <div 
                    v-for="msg in messages" 
                    :key="msg.timestamp + msg.content" 
                    class="d-flex mb-3 message-container"
                    
                    :class="{ 
                        'justify-content-end': msg.sender !== 'user', 
                        'justify-content-start': msg.sender === 'user' 
                    }">

                    <div v-if="msg.sender !== 'agent'" class="me-2 avatar-side">
                        <img 
                            :src="getAvatar(
                                activeConversation.externalUserId, 
                                msg.sender === 'bot' ? 'botpress' : 'user'
                            )" 
                            :alt="msg.sender === 'bot' ? 'Bot Avatar' : 'User Avatar'" 
                            class="avatar-small"
                            onerror="this.src='/src/assets/default-avatar.png'"
                        />
                    </div>
                    
                    <div class="message-bubble p-2 position-relative"
                        :class="{ 
                            'bg-primary text-white': (msg.sender === 'agent') || (msg.sender === 'bot'), 
                            'bg-light border': msg.sender !== 'agent' 
                        }">
                        
                        <div class="d-flex justify-content-between align-items-start">
                            <div>
                                <div class="text-muted small mb-1">
                                    <template v-if="msg.sender === 'agent'">
                                        {{t("Nhân viên")}}
                                    </template>
                                    <template v-else-if="msg.sender === 'bot'">
                                        {{t("Botpress")}}
                                    </template>
                                    <template v-else>
                                        {{ activeConversation.userName || getContactName(activeConversation.externalUserId) }}
                                    </template>
                                </div>
                                <p class="mb-0">{{ msg.content }}</p>
                            </div>
                            
                            <el-popconfirm
                                :title="t('Delete this message?')"
                                @confirm="deleteMessage(msg)"
                                v-if="msg.sender === 'agent'"
                            >
                                <template #reference>
                                    <el-button link size="small" class="message-delete-btn" @click.stop>
                                        <i class="fas fa-times"></i>
                                    </el-button>
                                </template>
                            </el-popconfirm>
                        </div>

                        <div v-if="msg.type === 'order'" class="order-card mt-2 p-2 border rounded bg-white text-dark">
                            <strong>{{ t('Gửi chi tiết đặt hàng cho khách hàng?') }}</strong>
                            <p class="small mb-2">{{ t('Bạn có thể thêm giá trị và mô tả đơn đặt hàng để khách hàng xem lại') }}</p>
                            <div class="d-flex justify-content-end">
                                <el-button size="small">{{ t('Lúc khác') }}</el-button>
                                <el-button size="small" type="primary">{{ t('Tiếp tục') }}</el-button>
                            </div>
                        </div>
                    </div>

                    <template v-if="msg.sender === 'agent'">
                        <div class="ms-2 avatar-side">
                            <img :src="agentAvatar" alt="Agent Avatar" class="avatar-small" />
                        </div>
                    </template>

                </div>

                <div v-if="!messages.length" class="text-center text-muted">
                    {{ t('There are no message') }}
                </div>
                <div ref="messagesEndRef" />
            </div>

            <div class="chat-input p-3 border-top">
                <div class="text-muted small mb-2">{{ t('Type your message...') }}</div>
                <el-input
                    v-model="inputContent"
                    :placeholder="t('Enter message')"
                    @keyup.enter="handleSendMessage"
                    size="large">
                    <template #append>
                        <el-button @click="handleSendMessage" />{{ t('Send') }}
                    </template>
                </el-input>
                <h1>&nbsp;</h1>
            </div>

        </template>

        <div v-else class="h-100 d-flex justify-content-center align-items-center text-muted">
            {{ t('Vui lòng chọn một cuộc hội thoại.') }}
        </div>
    </div>
</template>

<style scoped>
/* --- Styles Chung --- */
.chat-panel { background-color: #fff; }
.avatar-small { width: 40px; height: 40px; border-radius: 50%; object-fit: cover; }
.avatar-medium { width: 50px; height: 50px; border-radius: 50%; object-fit: cover; }
.bg-primary { background-color: #007bff !important; }
.order-card { background-color: #f8f9fa !important; border: 1px solid #ccc !important; }

/* --- Khu vực tin nhắn và cuộn --- */
.messages-area { 
    background-color: #f8f9fa; 
    overflow-y: auto; 
    height: calc(100vh - 250px); 
    padding: 20px;
    scroll-behavior: smooth;
    -webkit-overflow-scrolling: touch;
}

/* Custom scrollbar for WebKit browsers */
.messages-area::-webkit-scrollbar {
    width: 12px;
}

.messages-area::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 3px;
}

.messages-area::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 3px;
}

.messages-area::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
}

/* --- Containers và Flexbox cho vị trí tin nhắn --- */
.message-container {
    max-width: 100%;
}
.avatar-side {
    flex-shrink: 0;
    align-self: flex-end; /* Căn avatar xuống đáy bubble */
}

/* --- Message Bubble --- */
.message-bubble {
    max-width: 70%;
    word-break: break-word;
    border-radius: 18px;
    padding: 10px 16px;
    position: relative;
    /* Loại bỏ thuộc tính position: relative để tránh xung đột với .message-delete-btn */
}

/* Style Bubble: Agent (Gửi đi, Bên phải) */
.justify-content-end .message-bubble {
    background-color: #007bff;
    color: white;
    border-top-right-radius: 4px; /* Góc nhọn */
    border-top-left-radius: 18px; 
}

/* Style Bubble: Khác (Nhận vào, Bên trái) */
.justify-content-start .message-bubble {
    background-color: #ffffff; 
    border: 1px solid #ccc;
    color: #333;
    border-top-left-radius: 4px; /* Góc nhọn */
    border-top-right-radius: 18px; 
}


/* --- Nút Delete --- */
.message-delete-btn {
    opacity: 0;
    transition: opacity 0.2s;
    padding: 2px 4px;
    position: absolute;
    top: -8px;
    background: #f56c6c;
    border-radius: 50%;
    width: 20px;
    height: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 10px;
    cursor: pointer;
}

/* Vị trí nút delete cho tin nhắn Agent (Bên phải) */
.justify-content-end .message-bubble .message-delete-btn {
    right: -8px; 
    left: auto;
}

/* Vị trí nút delete cho tin nhắn Khác (Bên trái) */
.justify-content-start .message-bubble .message-delete-btn {
    left: -8px;
    right: auto;
}

.message-bubble:hover .message-delete-btn {
    opacity: 1;
}
</style>