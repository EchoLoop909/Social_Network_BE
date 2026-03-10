package com.example.social_network.Service;

import com.example.social_network.Payload.Request.MessageRequest;
import com.example.social_network.Payload.Response.MessageResponse;
import org.springframework.http.ResponseEntity;

public interface MessageService {

    // Phương thức này dùng cho API REST thông thường
    ResponseEntity<?> sendMessage(MessageRequest request, String ip);

    // Phương thức này chuyên dùng cho luồng Kafka/WebSocket (trả về DTO trực tiếp)
    MessageResponse processAndSaveMessage(MessageRequest request);

    Object getMessagesByConversation(String conversationId, int pageIdx, int pageSize);
}