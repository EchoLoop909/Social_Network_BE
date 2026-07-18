package com.example.social_network.Payload.Response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Dữ liệu thông báo trả về cho FE — dùng cho cả REST (danh sách) lẫn đẩy real-time WebSocket.
 * Ánh xạ từ entity Notification + kèm thông tin người gây sự kiện (actor) cho FE dễ hiển thị.
 */
@Data
public class NotificationResponse {
    private String id;
    private String type;        // MESSAGE / LIKE / COMMENT / ...
    private String actorId;
    private String actorName;
    private String actorPhoto;
    private String targetId;    // để FE điều hướng khi bấm vào
    private String message;     // đoạn chữ ngắn (chỉ có khi đẩy real-time; null với bản lấy từ DB)
    private Boolean isRead;
    private LocalDateTime createTime;
}
