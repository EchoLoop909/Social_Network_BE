package com.example.social_network.models.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "hashtags")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Hashtag implements Serializable {

    @Id
    @Column(name = "id_hashtag", length = 36, nullable = false, updatable = false)
    private String id;

    // Tên hashtag không có dấu '#' (VD: "travel", "food")
    @Column(name = "name", unique = true, nullable = false, length = 100)
    private String name;

    // Denormalized counter — cập nhật khi bài được tạo/xóa để tránh COUNT(*) tốn kém
    @Column(name = "post_count", nullable = false)
    private Integer postCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        if (postCount == null) postCount = 0;
        createdAt = LocalDateTime.now();
    }
}
