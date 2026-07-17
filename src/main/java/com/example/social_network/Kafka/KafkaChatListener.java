package com.example.social_network.Kafka;

import com.example.social_network.Payload.Request.MessageRequest;
import com.example.social_network.Service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * BƯỚC 2 — CONSUMER của chat.
 * Lắng nghe topic "chat-messages": mỗi khi có event tin nhắn (do sendMessage produce),
 * gọi processAndDeliver để LƯU DB + ĐẨY WebSocket. Đây là phần chạy nền (async).
 */
@Component
public class KafkaChatListener {

    private static final Logger logger = LoggerFactory.getLogger(KafkaChatListener.class);

    @Autowired
    private MessageService messageService;

    @KafkaListener(topics = "chat-messages", groupId = "social-network-chat-group")
    public void listen(MessageRequest dto) {
        logger.info("Nhận event chat từ Kafka: sender={}, conversation={}",
                dto != null ? dto.getSenderId() : null,
                dto != null ? dto.getConversationId() : null);
        messageService.processAndDeliver(dto);
    }
}
