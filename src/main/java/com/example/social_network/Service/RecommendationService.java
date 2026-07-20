package com.example.social_network.Service;

/**
 * Gợi ý bài viết cho feed dựa trên embedding (content-based) + độ mới + độ hot.
 * AI (Gemini) chỉ cung cấp vector; toàn bộ chọn/xếp/lọc là logic ở đây.
 */
public interface RecommendationService {

    /**
     * Trả về danh sách bài gợi ý cho 1 user (định dạng giống feed thường).
     * @param viewerId người xem
     * @param limit số bài tối đa trả về
     */
    Object recommend(String viewerId, int limit);
}
