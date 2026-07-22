package com.example.social_network.Service;

import com.example.social_network.models.Dto.NotificationEvent;
import com.example.social_network.models.Entity.Conversation;
import com.example.social_network.models.Entity.Messages;
import com.example.social_network.models.Entity.User;
import org.springframework.http.ResponseEntity;

public interface NotificationService {

    // ===== Kafka =====
    /** Producer tổng quát: đẩy 1 sự kiện thông báo vào topic "notifications". Mọi chức năng gọi hàm này. */
    void publish(NotificationEvent event);

    /** Tiện ích: sinh thông báo cho tin nhắn mới (gọi từ MessageServiceImpl sau khi lưu tin). */
    void notifyNewMessage(Conversation conv, User sender, Messages msg);

    /** Consumer: lưu Notification vào DB + đẩy WebSocket cho người nhận (KafkaNotificationListener gọi). */
    void processAndDeliver(NotificationEvent event);

    /**
     * Đẩy tín hiệu "quan hệ kết bạn thay đổi" xuống 1 user để FE tự nạp lại (vd bị hủy kết bạn).
     * Chỉ đẩy WebSocket, KHÔNG lưu DB và KHÔNG hiện chuông thông báo.
     */
    void notifyFriendshipSync(String recipientId);

    // ===== REST =====
    Object getMyNotifications(String userId, int pageIdx, int pageSize);

    Object getUnreadCount(String userId);

    ResponseEntity<?> markRead(String id, String userId, String ip);

    ResponseEntity<?> markAllRead(String userId, String ip);

    /** Xóa 1 thông báo của chính người dùng (kiểm tra quyền sở hữu). */
    ResponseEntity<?> deleteNotification(String id, String userId, String ip);
}
