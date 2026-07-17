package com.example.social_network.Kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * DEMO BƯỚC 3 — CONSUMER.
 * @KafkaListener: Spring tự gọi hàm này MỖI KHI có tin mới trong topic "kafka-demo".
 * Ở đây chỉ IN RA LOG để chứng minh Kafka đã thông (chưa lưu DB, chưa đẩy WebSocket).
 */
@Component
public class KafkaDemoListener {

    private static final Logger logger = LoggerFactory.getLogger(KafkaDemoListener.class);

    @KafkaListener(topics = "kafka-demo", groupId = "social-network-chat-group")
    public void listen(String msg) {
        logger.info("Nhận từ Kafka: {}", msg);
    }
}
