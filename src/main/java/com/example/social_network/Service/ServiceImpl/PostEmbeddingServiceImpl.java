package com.example.social_network.Service.ServiceImpl;

import com.example.social_network.Repository.PostEmbeddingRepository;
import com.example.social_network.Repository.PostHashtagRepository;
import com.example.social_network.Repository.PostRepository;
import com.example.social_network.Service.EmbeddingService;
import com.example.social_network.Service.PostEmbeddingService;
import com.example.social_network.models.Dto.PostEmbeddingEvent;
import com.example.social_network.models.Entity.Post;
import com.example.social_network.models.Entity.PostEmbedding;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostEmbeddingServiceImpl implements PostEmbeddingService {

    private static final Logger logger = LoggerFactory.getLogger(PostEmbeddingServiceImpl.class);
    public static final String EMBEDDING_TOPIC = "post-embeddings";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostHashtagRepository postHashtagRepository;

    @Autowired
    private PostEmbeddingRepository postEmbeddingRepository;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${gemini.model:gemini-embedding-001}")
    private String model;

    // ===== PRODUCER =====
    @Override
    public void publishEmbedRequest(String postId) {
        if (postId == null || postId.trim().isEmpty()) return;
        kafkaTemplate.send(EMBEDDING_TOPIC, postId, new PostEmbeddingEvent(postId));
        logger.info("Đẩy yêu cầu embedding cho post {} vào Kafka topic '{}'", postId, EMBEDDING_TOPIC);
    }

    // ===== EMBED 1 BÀI + LƯU =====
    @Override
    public boolean embedAndSave(String postId) {
        try {
            Post post = postRepository.findById(postId).orElse(null);
            if (post == null) {
                logger.warn("Bỏ qua embedding: post {} không tồn tại", postId);
                return false;
            }

            // Ghép caption + tên hashtag -> cho vector "hiểu chủ đề" rõ hơn.
            // Lấy THẲNG tên hashtag (tránh lazy-load Hashtag khi session đã đóng).
            String caption = post.getText() != null ? post.getText().trim() : "";
            List<String> tagNames = postHashtagRepository.findHashtagNamesByPostId(postId);
            String tags = String.join(" ", tagNames);
            String text = (caption + " " + tags).trim();

            if (text.isEmpty()) {
                logger.info("Bỏ qua embedding: post {} không có caption lẫn hashtag", postId);
                return false;
            }

            float[] vector = embeddingService.embed(text);
            if (vector == null || vector.length == 0) {
                logger.warn("Bỏ qua embedding: Gemini trả về rỗng cho post {}", postId);
                return false;
            }

            PostEmbedding pe = new PostEmbedding();
            pe.setIdPost(postId);
            pe.setVector(MAPPER.writeValueAsString(vector));
            pe.setModel(model);
            pe.setUpdatedAt(LocalDateTime.now());
            postEmbeddingRepository.save(pe); // save = insert hoặc update (idempotent)

            return true;
        } catch (Exception e) {
            logger.error("Lỗi embedAndSave cho post {}: {}", postId, e.getMessage());
            return false;
        }
    }

    // ===== BACKFILL BÀI CŨ =====
    @Override
    public void backfillMissing(int delayMs) {
        List<Post> all = postRepository.findAll();
        int total = all.size();
        int done = 0, skip = 0, fail = 0;
        logger.info("BẮT ĐẦU backfill embedding: {} bài, delay {}ms/lượt", total, delayMs);

        for (Post p : all) {
            if (postEmbeddingRepository.existsById(p.getId())) {
                skip++;
                continue;
            }
            boolean ok = embedAndSave(p.getId());
            if (ok) done++; else fail++;

            if ((done + fail) % 100 == 0) {
                logger.info("Backfill tiến độ: mới={} lỗi={} bỏ-qua={} / tổng {}", done, fail, skip, total);
            }
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                logger.warn("Backfill bị ngắt.");
                break;
            }
        }
        logger.info("XONG backfill embedding: mới={} lỗi={} bỏ-qua(đã có)={} / tổng {}", done, fail, skip, total);
    }
}
