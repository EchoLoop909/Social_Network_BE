package com.example.social_network.Kafka;

import com.example.social_network.Service.NotificationService;
import com.example.social_network.models.Dto.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * CONSUMER của thông báo.
 * Lắng nghe topic "notifications": mỗi khi có 1 NotificationEvent (do bất kỳ chức năng nào produce),
 * gọi processAndDeliver để LƯU DB + ĐẨY WebSocket cho người nhận. Chạy nền (async).
 */
@Component
public class KafkaNotificationListener {

    private static final Logger logger = LoggerFactory.getLogger(KafkaNotificationListener.class);

    @Autowired
    private NotificationService notificationService;

    @KafkaListener(topics = "notifications", groupId = "social-network-notification-group")
    public void listen(NotificationEvent event) {
        logger.info("Nhận notification event từ Kafka: recipient={}, type={}",
                event != null ? event.getRecipientId() : null,
                event != null ? event.getType() : null);
        notificationService.processAndDeliver(event);
    }
}
