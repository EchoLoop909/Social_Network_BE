package com.example.social_network.Service;

/**
 * Gọi Gemini Embedding API để biến 1 đoạn văn bản thành vector (mảng số).
 * Vector này dùng để đo độ giống nhau về NGHĨA giữa các bài viết / sở thích user,
 * phục vụ chức năng gợi ý bài viết lên feed.
 */
public interface EmbeddingService {

    /**
     * Biến text thành vector embedding.
     * @param text nội dung cần embed (vd: caption + hashtag của bài viết)
     * @return mảng float (text-embedding-004 trả 768 chiều); null nếu lỗi hoặc text rỗng
     */
    float[] embed(String text);
}
