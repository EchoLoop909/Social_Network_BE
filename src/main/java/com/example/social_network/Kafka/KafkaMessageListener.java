package com.example.social_network.Kafka;

import com.example.social_network.Payload.Request.MessageRequest;
import com.example.social_network.Payload.Response.MessageResponse;
import com.example.social_network.Service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(KafkaMessageListener.class);

    // Công cụ của Spring Boot dùng để gửi tin nhắn qua WebSocket
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Service bạn vừa viết để xử lý logic lưu Database
    @Autowired
    private MessageService messageService;

    // Lắng nghe topic "chat-messages".
    // groupId giúp Kafka quản lý tiến trình đọc tin nhắn của hệ thống
    @KafkaListener(topics = "chat-messages", groupId = "social-network-chat-group")
    public void listen(MessageRequest request) {
        try {
            logger.info("Nhận tin nhắn từ Kafka: User {} gửi vào Conversation {}",
                    request.getSenderId(), request.getConversationId());

            // 1. Gọi Service để lưu vào DB (bảng messages, cập nhật conversation...)
            MessageResponse savedData = messageService.processAndSaveMessage(request);

            // 2. Bắn kết quả đã lưu thành công qua WebSocket cho mọi người trong nhóm
            // Frontend (React) sẽ subscribe vào đường dẫn: /topic/conversation/{conversationId}
            messagingTemplate.convertAndSend("/topic/conversation/" + savedData.getConversationId(), savedData);

        } catch (Exception e) {
            // Cần try-catch ở đây để nếu có lỗi lưu DB (ví dụ sai ID),
            // ứng dụng không bị crash và Kafka không bị kẹt.
            logger.error("Lỗi khi xử lý tin nhắn từ Kafka: {}", e.getMessage());
        }
    }
}