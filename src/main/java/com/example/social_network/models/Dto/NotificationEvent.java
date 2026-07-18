package com.example.social_network.models.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload sự kiện thông báo được đẩy vào Kafka topic "notifications".
 * Thiết kế TỔNG QUÁT để mọi chức năng (nhắn tin, like, comment, follow...) đều dùng chung:
 * chỉ cần producer gửi 1 NotificationEvent, consumer sẽ lưu DB + đẩy WebSocket.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private String recipientId; // ai nhận thông báo (bắt buộc)
    private String actorId;     // ai gây ra sự kiện (null nếu là hệ thống)
    private String type;        // tên NotificationType: MESSAGE / LIKE / COMMENT / ...
    private String targetId;    // id đối tượng liên quan để điều hướng (conversationId, postId...)
    private String preview;     // đoạn chữ ngắn để hiển thị nhanh (toast) — không lưu DB
}
