package com.example.social_network.Service;

/**
 * Quản lý vector embedding của bài viết (phục vụ gợi ý feed).
 * - publishEmbedRequest: đẩy yêu cầu embed 1 bài lên Kafka (gọi khi đăng bài).
 * - embedAndSave: thực sự gọi Gemini + lưu vector (consumer Kafka & backfill dùng chung).
 * - backfillMissing: embed toàn bộ bài CHƯA có vector (chạy 1 lần cho dữ liệu cũ), có nghỉ giữa các lượt.
 */
public interface PostEmbeddingService {

    /** Đẩy 1 PostEmbeddingEvent lên topic "post-embeddings". */
    void publishEmbedRequest(String postId);

    /** Load bài + hashtag -> ghép text -> gọi Gemini -> lưu post_embedding. Trả true nếu lưu được. */
    boolean embedAndSave(String postId);

    /**
     * Embed những bài CHƯA có vector, nghỉ delayMs giữa mỗi lượt (tránh vượt quota free).
     * Chạy nền (được gọi trong 1 thread riêng). Idempotent: chạy lại chỉ bù bài còn thiếu.
     */
    void backfillMissing(int delayMs);
}
