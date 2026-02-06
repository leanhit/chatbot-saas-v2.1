// @/api/takeoverWS.ts
import { ref } from 'vue';
import { TakeoverMessage } from '@/types/takeover'; // Giả định TakeoverMessage là kiểu dữ liệu

export function useTakeoverWebSocket(conversationId: string) { // conversationId là String để khớp TakeoverMessage
  const messages = ref<TakeoverMessage[]>([]);
  let ws: WebSocket | null = null;

  const connect = () => {
    // Cần thay đổi URL WebSocket nếu production hoặc thay đổi port
    ws = new WebSocket('ws://localhost:8080/ws/takeover'); 

    ws.onopen = () => console.log('WebSocket connected');
    ws.onmessage = (event) => {
      const msg: TakeoverMessage = JSON.parse(event.data);
      
      // 🌟 LOGIC LỌC TIN NHẮN TẠI FRONTEND (Rất quan trọng)
      if (msg.conversationId === conversationId) { 
        messages.value.push(msg);
      }
    };
    ws.onclose = () => console.log('WebSocket disconnected');
    ws.onerror = (err) => console.error('WebSocket error', err);
  };

  const disconnect = () => {
    if (ws) ws.close();
  };

  // Thêm hàm để clear messages khi chuyển đổi conversation
  const clearMessages = () => {
      messages.value = [];
  }

  return {
    messages,
    connect,
    disconnect,
    clearMessages // Tiện ích mới
  };
}