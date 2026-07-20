package com.example.social_network.Kafka;

import com.example.social_network.Service.PostEmbeddingService;
import com.example.social_network.models.Dto.PostEmbeddingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * CONSUMER embedding: lắng nghe topic "post-embeddings", với mỗi bài mới
 * gọi embedAndSave (gọi Gemini + lưu vector) trên luồng nền — tách khỏi luồng đăng bài.
 */
@Component
public class KafkaEmbeddingListener {

    private static final Logger logger = LoggerFactory.getLogger(KafkaEmbeddingListener.class);

    @Autowired
    private PostEmbeddingService postEmbeddingService;

    @KafkaListener(topics = "post-embeddings", groupId = "social-network-embedding-group")
    public void listen(PostEmbeddingEvent event) {
        if (event == null || event.getPostId() == null) return;
        logger.info("Nhận yêu cầu embedding từ Kafka cho post {}", event.getPostId());
        postEmbeddingService.embedAndSave(event.getPostId());
    }
}
