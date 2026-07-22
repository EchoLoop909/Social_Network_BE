package com.example.social_network.Service;

import com.example.social_network.Payload.Request.ConversationRequest;
import com.example.social_network.Payload.Request.MessageRequest;
import org.springframework.http.ResponseEntity;

/**
 * Chat BƯỚC 1 (chưa Kafka): Controller -> Service (lưu DB + đẩy WebSocket).
 * userId (người thao tác) luôn lấy từ token, KHÔNG nhận từ request body.
 */
public interface MessageService {

    // Tạo/mở hội thoại. 1 người -> SINGLE (mở lại nếu đã có); nhiều người -> GROUP.
    ResponseEntity<?> createConversation(ConversationRequest dto, String userId, String ip);

    // Gửi tin nhắn (BƯỚC 2): validate + NÉM event vào Kafka topic, trả về ngay.
    ResponseEntity<?> sendMessage(MessageRequest dto, String userId, String ip);

    // CONSUMER Kafka gọi: lưu DB + đẩy WebSocket. senderId lấy từ chính event.
    void processAndDeliver(MessageRequest dto);

    // Lịch sử tin nhắn của 1 hội thoại (phân trang, mới nhất trước).
    Object getMessages(String conversationId, String userId, int pageIdx, int pageSize);

    // Danh sách hội thoại của người đang đăng nhập (Inbox), sắp theo tin mới nhất.
    Object getConversations(String userId, int pageIdx, int pageSize);

    // Đánh dấu ĐÃ ĐỌC tới tin mới nhất của hội thoại (cập nhật last_read_message_id của mình).
    ResponseEntity<?> markRead(String conversationId, String userId, String ip);

    // Trạng thái đã đọc của các thành viên KHÁC (để hiện "Đã xem" như FB).
    Object getReadState(String conversationId, String userId);
}
