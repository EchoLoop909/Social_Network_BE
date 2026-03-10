package com.example.social_network.Payload.Response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageResponse {
    private String id;
    private String conversationId;
    private String senderId;
    private String senderName; // Có thể lấy thêm tên người gửi để frontend dễ hiển thị
    private String text;
    private String photo;
    private String messageType;
    private LocalDateTime createTime;
}