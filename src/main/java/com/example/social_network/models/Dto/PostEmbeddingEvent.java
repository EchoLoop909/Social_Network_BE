package com.example.social_network.models.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sự kiện Kafka (topic "post-embeddings"): yêu cầu tạo/cập nhật vector cho 1 bài viết.
 * Producer: PostServiceImpl khi đăng bài. Consumer: KafkaEmbeddingListener -> embedAndSave.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostEmbeddingEvent {
    private String postId;
}
