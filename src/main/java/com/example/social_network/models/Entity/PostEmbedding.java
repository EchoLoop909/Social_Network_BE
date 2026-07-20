package com.example.social_network.models.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Vector embedding của 1 bài viết (dùng cho gợi ý feed).
 * Quan hệ 1-1 với Post: id_post vừa là khóa chính vừa là khóa ngoại (ON DELETE CASCADE).
 * vector lưu dạng chuỗi JSON "[0.01,-0.03,...]" (gemini-embedding-001 = 3072 số).
 */
@Entity
@Table(name = "post_embedding")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PostEmbedding implements Serializable {

    @Id
    @Column(name = "id_post", length = 36, nullable = false, updatable = false)
    private String idPost;

    // Mảng số dạng JSON. LONGTEXT vì 3072 số ~ vài chục KB.
    @Column(name = "vector", columnDefinition = "LONGTEXT", nullable = false)
    private String vector;

    @Column(name = "model", length = 50)
    private String model;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
